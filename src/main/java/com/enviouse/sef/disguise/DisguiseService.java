package com.enviouse.sef.disguise;

import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.storage.InstantJsonAdapter;
import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class DisguiseService implements StorageRepository {
    public static final int SCHEMA_VERSION = 1;
    public static final int HARD_MAXIMUM_ACTIVE = 4_096;
    public static final int HARD_MAXIMUM_PRESETS = 1_024;
    public static final int HARD_MAXIMUM_PROFILE_CACHE = 2_048;

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantJsonAdapter())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    private static final String ID_PATTERN = "[a-z0-9][a-z0-9_.-]{0,63}";
    private static final String RESOURCE_PATTERN = "[a-z0-9_.-]+:[a-z0-9/._-]+";

    private final Map<String, MobAdapter> mobAdapters = new LinkedHashMap<>();
    private final Map<String, DisguisePreset> presets = new LinkedHashMap<>();
    private final Map<UUID, DisguiseRecord> active = new LinkedHashMap<>();
    private final Map<UUID, ProfileSnapshot> profileCache = new LinkedHashMap<>();
    private final Map<UUID, EnumMap<AbilitySlot, Instant>> abilityCooldowns = new LinkedHashMap<>();
    private Settings settings;
    private StorageService.Document document;
    private Path path;
    private RepositoryState state = RepositoryState.NEW;
    private long revision;
    private long flushedRevision;

    public DisguiseService(Settings settings) {
        this.settings = Objects.requireNonNull(settings, "settings");
        registerBuiltinAdapters();
    }

    @Override
    public String id() {
        return "sef:disguises";
    }

    @Override
    public String domain() {
        return "disguises";
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public synchronized Path path() {
        return path;
    }

    @Override
    public synchronized LoadResult load(Path managedRoot) {
        path = Objects.requireNonNull(managedRoot, "managedRoot")
                .resolve("disguises.json")
                .toAbsolutePath()
                .normalize();
        presets.clear();
        active.clear();
        profileCache.clear();
        abilityCooldowns.clear();
        revision = 0L;
        flushedRevision = 0L;
        boolean existed = java.nio.file.Files.exists(path);
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(null);
        if (document == null) {
            state = existed ? RepositoryState.RECOVERY : RepositoryState.MISSING;
            return new LoadResult(state, existed ? "storage unavailable" : "new repository");
        }
        try {
            Snapshot snapshot = GSON.fromJson(document.data(), Snapshot.class);
            if (snapshot == null
                    || snapshot.presets().size() > HARD_MAXIMUM_PRESETS
                    || snapshot.persistentActive().size() > settings.maximumActive()) {
                throw new IllegalStateException("disguise collections are outside bounds");
            }
            for (DisguisePreset preset : snapshot.presets()) {
                validatePreset(preset);
                if (presets.putIfAbsent(preset.id(), preset) != null) {
                    throw new IllegalStateException("duplicate disguise preset");
                }
                revision = Math.max(revision, preset.revision());
            }
            Instant now = Instant.now();
            for (DisguiseRecord record : snapshot.persistentActive()) {
                validateRecord(record);
                if (!record.persistence().persistRestart()
                        || record.expiresAt() != null && !record.expiresAt().isAfter(now)
                        || active.putIfAbsent(record.subjectId(), record) != null) {
                    continue;
                }
                revision = Math.max(revision, record.revision());
            }
            state = RepositoryState.READY;
            if (document.migrated()) {
                revision++;
            }
            return new LoadResult(
                    state,
                    "loaded " + presets.size() + " disguise presets and " + active.size() + " persistent disguises");
        } catch (RuntimeException exception) {
            presets.clear();
            active.clear();
            state = RepositoryState.RECOVERY;
            return new LoadResult(state, exception.getClass().getSimpleName());
        }
    }

    public synchronized void configure(Settings replacement) {
        settings = Objects.requireNonNull(replacement, "replacement");
        if (!settings.enabled()) {
            clearAll(ClearReason.FEATURE_DISABLED);
        }
    }

    public synchronized ActionResult<DisguiseRecord> setMob(
            UUID subjectId,
            String entityType,
            UUID actorId,
            boolean traits,
            boolean abilities,
            Instant expiresAt
    ) {
        writable();
        if (!settings.enabled()) {
            return ActionResult.failure(ActionResult.ReasonCode.FEATURE_DISABLED, "disguises are disabled");
        }
        String type = normalizeResource(entityType);
        MobAdapter adapter = mobAdapters.get(type);
        if (adapter == null || !adapter.enhancedSupported()) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "entity type is not a supported disguise");
        }
        if (!active.containsKey(subjectId) && active.size() >= settings.maximumActive()) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "active disguise limit reached");
        }
        if (traits && (!settings.traitsEnabled() || adapter.traits().isEmpty())) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "disguise traits are unavailable");
        }
        if (abilities && (!settings.abilitiesEnabled() || adapter.abilities().isEmpty())) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "disguise abilities are unavailable");
        }
        long next = nextRecordRevision(subjectId);
        DisguiseRecord record = new DisguiseRecord(
                UUID.randomUUID(),
                Objects.requireNonNull(subjectId, "subjectId"),
                DisguiseKind.MOB,
                type,
                null,
                "",
                DisplayLabelMode.NICKNAME_PLUS_DISGUISE,
                EquipmentPolicy.SHOW_REAL_EQUIPMENT,
                HitboxPolicy.PLAYER,
                ViewerPolicy.EVERYONE,
                traits,
                abilities,
                Objects.requireNonNull(actorId, "actorId"),
                Instant.now(),
                expiresAt,
                next,
                PersistencePolicy.defaults(),
                "sef");
        active.put(subjectId, record);
        changed(next);
        audit(actorId, "sef:disguise.set.mob", subjectId, AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        return ActionResult.success(record);
    }

    public synchronized ActionResult<DisguiseRecord> setPlayer(
            UUID subjectId,
            ProfileSnapshot profile,
            UUID actorId,
            Instant expiresAt
    ) {
        writable();
        if (!settings.enabled()) {
            return ActionResult.failure(ActionResult.ReasonCode.FEATURE_DISABLED, "disguises are disabled");
        }
        Objects.requireNonNull(profile, "profile");
        if (!profile.trustedTextures()) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "player disguise profile is not trusted");
        }
        cacheProfile(profile);
        if (!active.containsKey(subjectId) && active.size() >= settings.maximumActive()) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "active disguise limit reached");
        }
        long next = nextRecordRevision(subjectId);
        DisguiseRecord record = new DisguiseRecord(
                UUID.randomUUID(),
                Objects.requireNonNull(subjectId, "subjectId"),
                DisguiseKind.PLAYER,
                "minecraft:player",
                profile.profileId(),
                "",
                DisplayLabelMode.DISGUISE_PROFILE,
                EquipmentPolicy.SHOW_REAL_EQUIPMENT,
                HitboxPolicy.PLAYER,
                ViewerPolicy.EVERYONE,
                false,
                false,
                Objects.requireNonNull(actorId, "actorId"),
                Instant.now(),
                expiresAt,
                next,
                PersistencePolicy.defaults(),
                "sef");
        active.put(subjectId, record);
        changed(next);
        audit(actorId, "sef:disguise.set.player", subjectId, AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        return ActionResult.success(record);
    }

    public synchronized ActionResult<DisguiseRecord> setPreset(
            UUID subjectId,
            String presetId,
            UUID actorId,
            Instant expiresAt
    ) {
        writable();
        DisguisePreset preset = presets.get(normalizeId(presetId));
        if (preset == null || !preset.enabled()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "disguise preset not found");
        }
        if (preset.kind() == DisguiseKind.MOB) {
            ActionResult<DisguiseRecord> base = setMob(
                    subjectId,
                    preset.reference(),
                    actorId,
                    preset.traitsEnabled(),
                    preset.abilitiesEnabled(),
                    expiresAt);
            if (!base.successful()) {
                return base;
            }
            DisguiseRecord current = base.value();
            DisguiseRecord replacement = copyRecord(
                    current,
                    DisguiseKind.PRESET,
                    current.reference(),
                    current.profileId(),
                    preset.id(),
                    preset.labelMode(),
                    preset.equipmentPolicy(),
                    preset.hitboxPolicy(),
                    preset.viewerPolicy(),
                    preset.persistence(),
                    current.revision());
            active.put(subjectId, replacement);
            return ActionResult.success(replacement);
        }
        ProfileSnapshot profile = profileCache.get(preset.profileId());
        if (profile == null || profile.expired(Instant.now())) {
            return ActionResult.failure(ActionResult.ReasonCode.PROVIDER_ERROR, "preset player profile is unavailable");
        }
        ActionResult<DisguiseRecord> base = setPlayer(subjectId, profile, actorId, expiresAt);
        if (!base.successful()) {
            return base;
        }
        DisguiseRecord current = base.value();
        DisguiseRecord replacement = copyRecord(
                current,
                DisguiseKind.PRESET,
                current.reference(),
                current.profileId(),
                preset.id(),
                preset.labelMode(),
                preset.equipmentPolicy(),
                preset.hitboxPolicy(),
                preset.viewerPolicy(),
                preset.persistence(),
                current.revision());
        active.put(subjectId, replacement);
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<DisguisePreset> savePreset(
            DisguisePreset requested,
            long expectedRevision,
            UUID actorId
    ) {
        writable();
        validatePreset(requested);
        DisguisePreset current = presets.get(requested.id());
        if (current == null && presets.size() >= HARD_MAXIMUM_PRESETS) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "disguise preset limit reached");
        }
        if (current != null && current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "disguise preset revision changed");
        }
        if (requested.kind() == DisguiseKind.MOB && !mobAdapters.containsKey(requested.reference())) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, "preset entity type is unsupported");
        }
        if (requested.kind() == DisguiseKind.PLAYER && requested.profileId() == null) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, "preset player profile is missing");
        }
        long next = current == null ? 1L : Math.addExact(current.revision(), 1L);
        DisguisePreset replacement = new DisguisePreset(
                requested.id(),
                requested.displayName(),
                requested.kind(),
                requested.reference(),
                requested.profileId(),
                requested.labelMode(),
                requested.equipmentPolicy(),
                requested.hitboxPolicy(),
                requested.viewerPolicy(),
                requested.traitsEnabled(),
                requested.abilitiesEnabled(),
                requested.persistence(),
                requested.enabled(),
                next,
                actorId,
                Instant.now());
        presets.put(replacement.id(), replacement);
        changed(next);
        audit(actorId, "sef:disguise.preset.save", null, AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<Void> deletePreset(
            String presetId,
            UUID actorId,
            long expectedRevision
    ) {
        writable();
        String id = normalizeId(presetId);
        DisguisePreset current = presets.get(id);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "disguise preset not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "disguise preset revision changed");
        }
        if (active.values().stream().anyMatch(record -> record.presetId().equals(id))) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "disguise preset is active");
        }
        presets.remove(id);
        changed(Math.addExact(current.revision(), 1L));
        audit(actorId, "sef:disguise.preset.delete", null,
                AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        return ActionResult.success(null);
    }

    public synchronized ActionResult<DisguiseRecord> updateOptions(
            UUID subjectId,
            DisplayLabelMode labelMode,
            EquipmentPolicy equipmentPolicy,
            HitboxPolicy hitboxPolicy,
            PersistencePolicy persistence,
            UUID actorId,
            long expectedRevision
    ) {
        writable();
        DisguiseRecord current = active(subjectId).orElse(null);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "player is not disguised");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "disguise revision changed");
        }
        HitboxPolicy resolvedHitbox = hitboxPolicy == null ? current.hitboxPolicy() : hitboxPolicy;
        if (resolvedHitbox != HitboxPolicy.PLAYER) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.POLICY_DENIED,
                    "gameplay disguise hitboxes are unavailable for this adapter");
        }
        long next = Math.addExact(current.revision(), 1L);
        DisguiseRecord replacement = new DisguiseRecord(
                current.disguiseId(),
                current.subjectId(),
                current.kind(),
                current.reference(),
                current.profileId(),
                current.presetId(),
                labelMode == null ? current.labelMode() : labelMode,
                equipmentPolicy == null ? current.equipmentPolicy() : equipmentPolicy,
                resolvedHitbox,
                current.viewerPolicy(),
                current.traitsEnabled(),
                current.abilitiesEnabled(),
                current.createdBy(),
                current.createdAt(),
                current.expiresAt(),
                next,
                persistence == null ? current.persistence() : persistence,
                current.provider());
        active.put(subjectId, replacement);
        changed(next);
        audit(actorId, "sef:disguise.options", subjectId,
                AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<Void> clear(UUID subjectId, UUID actorId, ClearReason reason) {
        writable();
        DisguiseRecord removed = active.remove(Objects.requireNonNull(subjectId, "subjectId"));
        abilityCooldowns.remove(subjectId);
        if (removed == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "player is not disguised");
        }
        changed(Math.addExact(removed.revision(), 1L));
        audit(actorId, "sef:disguise.clear." + reason.name().toLowerCase(Locale.ROOT),
                subjectId, AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        return ActionResult.success(null);
    }

    public synchronized void clearAll(ClearReason reason) {
        if (active.isEmpty()) {
            return;
        }
        active.clear();
        abilityCooldowns.clear();
        revision++;
    }

    public synchronized void onLogout(UUID subjectId) {
        DisguiseRecord record = active.get(subjectId);
        if (record == null) {
            return;
        }
        if (settings.clearOnLogout() || !record.persistence().persistReconnect()) {
            active.remove(subjectId);
            abilityCooldowns.remove(subjectId);
            revision++;
        }
    }

    public synchronized void onDeath(UUID subjectId) {
        DisguiseRecord record = active.get(subjectId);
        if (record == null) {
            return;
        }
        if (settings.clearOnDeath() || !record.persistence().persistDeath()) {
            active.remove(subjectId);
            abilityCooldowns.remove(subjectId);
            revision++;
        }
    }

    public synchronized Optional<DisguiseRecord> active(UUID subjectId) {
        prune(Instant.now());
        return Optional.ofNullable(active.get(subjectId));
    }

    public synchronized List<DisguiseRecord> active() {
        prune(Instant.now());
        return active.values().stream()
                .sorted(Comparator.comparing(value -> value.subjectId().toString()))
                .toList();
    }

    public synchronized List<DisguisePreset> presets() {
        return presets.values().stream()
                .sorted(Comparator.comparing(DisguisePreset::id))
                .toList();
    }

    public synchronized Optional<DisguisePreset> preset(String presetId) {
        try {
            return Optional.ofNullable(presets.get(normalizeId(presetId)));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    public synchronized List<MobAdapter> supportedMobs() {
        return List.copyOf(mobAdapters.values());
    }

    public synchronized Optional<Projection> projection(
            UUID viewerId,
            UUID subjectId,
            boolean subjectVanishedFromViewer,
            boolean enhancedClient
    ) {
        return projection(viewerId, subjectId, subjectVanishedFromViewer, enhancedClient, false);
    }

    public synchronized Optional<Projection> projection(
            UUID viewerId,
            UUID subjectId,
            boolean subjectVanishedFromViewer,
            boolean enhancedClient,
            boolean viewerIsStaff
    ) {
        if (subjectVanishedFromViewer) {
            return Optional.empty();
        }
        DisguiseRecord record = active(subjectId).orElse(null);
        if (record == null) {
            return Optional.empty();
        }
        if (record.viewerPolicy() == ViewerPolicy.SELF_ONLY && !viewerId.equals(subjectId)
                || record.viewerPolicy() == ViewerPolicy.OTHERS_ONLY && viewerId.equals(subjectId)
                || record.viewerPolicy() == ViewerPolicy.STAFF_ONLY && !viewerIsStaff) {
            return Optional.empty();
        }
        MobAdapter adapter = record.reference().equals("minecraft:player")
                ? null
                : mobAdapters.get(record.reference());
        ProjectionMode mode;
        if (enhancedClient) {
            mode = ProjectionMode.ENHANCED;
        } else if (adapter != null && adapter.vanillaProxySupported() && !viewerId.equals(subjectId)) {
            mode = ProjectionMode.VANILLA_PROXY;
        } else {
            mode = ProjectionMode.TEXT_FALLBACK;
        }
        return Optional.of(new Projection(record, mode, record.revision()));
    }

    public synchronized ActionResult<AbilityAdmission> admitAbility(
            UUID subjectId,
            AbilitySlot slot,
            Instant now
    ) {
        Objects.requireNonNull(slot, "slot");
        Objects.requireNonNull(now, "now");
        DisguiseRecord record = active(subjectId).orElse(null);
        if (record == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "player is not disguised");
        }
        if (!settings.abilitiesEnabled() || !record.abilitiesEnabled()) {
            return ActionResult.failure(ActionResult.ReasonCode.FEATURE_DISABLED, "disguise abilities are disabled");
        }
        MobAdapter adapter = mobAdapters.get(record.reference());
        AbilityDefinition ability = adapter == null ? null : adapter.abilities().get(slot);
        if (ability == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "disguise ability slot is unavailable");
        }
        EnumMap<AbilitySlot, Instant> cooldowns =
                abilityCooldowns.computeIfAbsent(subjectId, ignored -> new EnumMap<>(AbilitySlot.class));
        Instant availableAt = cooldowns.get(slot);
        if (availableAt != null && availableAt.isAfter(now)) {
            return ActionResult.failure(ActionResult.ReasonCode.COOLDOWN_ACTIVE,
                    "disguise ability cooldown has " + Duration.between(now, availableAt).toSeconds() + " seconds remaining");
        }
        return ActionResult.success(new AbilityAdmission(record, ability, now));
    }

    public synchronized void commitAbility(AbilityAdmission admission) {
        commitAbility(admission, admission.ability().cooldown());
    }

    public synchronized void commitAbility(AbilityAdmission admission, Duration cooldown) {
        Objects.requireNonNull(admission, "admission");
        Objects.requireNonNull(cooldown, "cooldown");
        if (cooldown.isNegative() || cooldown.compareTo(Duration.ofDays(365)) > 0) {
            throw new IllegalArgumentException("disguise ability cooldown is outside bounds");
        }
        DisguiseRecord current = active.get(admission.record().subjectId());
        if (current == null || current.revision() != admission.record().revision()) {
            return;
        }
        abilityCooldowns.computeIfAbsent(
                        current.subjectId(),
                        ignored -> new EnumMap<>(AbilitySlot.class))
                .put(admission.ability().slot(), admission.admittedAt().plus(cooldown));
    }

    public synchronized void cacheProfile(ProfileSnapshot profile) {
        Objects.requireNonNull(profile, "profile");
        prune(Instant.now());
        while (!profileCache.containsKey(profile.profileId())
                && profileCache.size() >= HARD_MAXIMUM_PROFILE_CACHE) {
            UUID oldest = profileCache.values().stream()
                    .min(Comparator.comparing(ProfileSnapshot::expiresAt))
                    .map(ProfileSnapshot::profileId)
                    .orElse(null);
            if (oldest == null) {
                break;
            }
            profileCache.remove(oldest);
        }
        profileCache.put(profile.profileId(), profile);
    }

    public synchronized Optional<ProfileSnapshot> profile(UUID profileId) {
        prune(Instant.now());
        return Optional.ofNullable(profileCache.get(profileId));
    }

    public synchronized Optional<ProfileSnapshot> profile(String profileName) {
        prune(Instant.now());
        String normalized = Objects.requireNonNullElse(profileName, "").strip();
        return profileCache.values().stream()
                .filter(profile -> profile.profileName().equalsIgnoreCase(normalized))
                .findFirst();
    }

    public synchronized long revision() {
        return Math.max(1L, revision);
    }

    public synchronized Settings settings() {
        return settings;
    }

    @Override
    public synchronized void flush() throws IOException {
        if (!dirty()) {
            return;
        }
        StorageService.write(path, domain(), SCHEMA_VERSION, GSON.toJsonTree(snapshot()), document);
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(null);
        flushedRevision = revision;
        if (state == RepositoryState.MISSING || state == RepositoryState.NEW) {
            state = RepositoryState.READY;
        }
    }

    @Override
    public synchronized boolean dirty() {
        return revision != flushedRevision;
    }

    @Override
    public synchronized RepositoryState state() {
        return state;
    }

    private Snapshot snapshot() {
        List<DisguiseRecord> persistent = active.values().stream()
                .filter(value -> value.persistence().persistRestart())
                .toList();
        return new Snapshot(presets(), persistent);
    }

    private long nextRecordRevision(UUID subjectId) {
        DisguiseRecord current = active.get(subjectId);
        return current == null ? 1L : Math.addExact(current.revision(), 1L);
    }

    private void changed(long recordRevision) {
        revision = Math.max(Math.addExact(revision, 1L), recordRevision);
    }

    private void prune(Instant now) {
        int activeBefore = active.size();
        active.values().removeIf(value -> value.expiresAt() != null && !value.expiresAt().isAfter(now));
        if (active.size() != activeBefore) {
            revision++;
        }
        profileCache.values().removeIf(value -> value.expired(now));
        abilityCooldowns.entrySet().removeIf(entry -> !active.containsKey(entry.getKey()));
    }

    private void writable() {
        if (state == RepositoryState.RECOVERY
                || state == RepositoryState.UNSUPPORTED
                || state == RepositoryState.ERROR
                || state == RepositoryState.CLOSED) {
            throw new IllegalStateException("disguise repository is not writable");
        }
    }

    private void registerBuiltinAdapters() {
        register(new MobAdapter(
                "minecraft:blaze",
                "Blaze",
                true,
                true,
                Set.of(Trait.FIRE_RESISTANCE, Trait.WATER_VULNERABILITY),
                Map.of(
                        AbilitySlot.PRIMARY,
                        new AbilityDefinition(
                                AbilitySlot.PRIMARY,
                                "blaze_fireball",
                                "sef.disguise.ability.blaze.fireball",
                                Duration.ofSeconds(10)),
                        AbilitySlot.SECONDARY,
                        new AbilityDefinition(
                                AbilitySlot.SECONDARY,
                                "blaze_hover",
                                "sef.disguise.ability.blaze.hover",
                                Duration.ofSeconds(20)),
                        AbilitySlot.UTILITY,
                        new AbilityDefinition(
                                AbilitySlot.UTILITY,
                                "blaze_fire_resistance",
                                "sef.disguise.ability.blaze.fire_resistance",
                                Duration.ofSeconds(30)))));
        registerSimple("minecraft:snow_golem", "Snow Golem", true, Set.of(Trait.WATER_VULNERABILITY));
        registerSimple("minecraft:enderman", "Enderman", true);
        registerSimple("minecraft:spider", "Spider", true, Set.of(Trait.CLIMBING));
        registerSimple("minecraft:bee", "Bee", true, Set.of(Trait.CONTROLLED_FLIGHT));
        registerSimple("minecraft:creeper", "Creeper", true);
        registerSimple("minecraft:ghast", "Ghast", true, Set.of(Trait.CONTROLLED_FLIGHT));
        registerSimple(
                "minecraft:dolphin",
                "Dolphin",
                true,
                Set.of(Trait.WATER_BREATHING, Trait.SWIM_SPEED));
        registerSimple("minecraft:wolf", "Wolf", true);
        registerSimple("minecraft:witch", "Witch", true);
        registerSimple("minecraft:zombie", "Zombie", true, Set.of(Trait.DAYLIGHT_SENSITIVITY));
        registerSimple("minecraft:skeleton", "Skeleton", true, Set.of(Trait.DAYLIGHT_SENSITIVITY));
        registerSimple("minecraft:cow", "Cow", true);
        registerSimple("minecraft:pig", "Pig", true);
        registerSimple("minecraft:sheep", "Sheep", true);
        registerSimple("minecraft:chicken", "Chicken", true, Set.of(Trait.REDUCED_FALL_DAMAGE));
        registerSimple("minecraft:slime", "Slime", true);
        registerSimple("minecraft:bat", "Bat", true, Set.of(Trait.CONTROLLED_FLIGHT));
        registerSimple("minecraft:phantom", "Phantom", true, Set.of(Trait.CONTROLLED_FLIGHT));
    }

    private void registerSimple(String type, String name, boolean vanillaProxy) {
        registerSimple(type, name, vanillaProxy, Set.of());
    }

    private void registerSimple(
            String type,
            String name,
            boolean vanillaProxy,
            Set<Trait> traits
    ) {
        register(new MobAdapter(type, name, true, vanillaProxy, traits, Map.of()));
    }

    private void register(MobAdapter adapter) {
        if (mobAdapters.putIfAbsent(adapter.entityType(), adapter) != null) {
            throw new IllegalStateException("duplicate disguise mob adapter");
        }
    }

    private static String normalizeId(String value) {
        String result = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (!result.matches(ID_PATTERN)) {
            throw new IllegalArgumentException("disguise id is invalid");
        }
        return result;
    }

    private static String normalizeResource(String value) {
        String result = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (!result.contains(":")) {
            result = "minecraft:" + result;
        }
        if (!result.matches(RESOURCE_PATTERN) || result.length() > 128) {
            throw new IllegalArgumentException("disguise resource is invalid");
        }
        return result;
    }

    private static String bounded(String value, int maximum) {
        String result = Objects.requireNonNullElse(value, "").codePoints()
                .filter(codePoint -> !Character.isISOControl(codePoint))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString()
                .trim();
        if (result.length() > maximum) {
            throw new IllegalArgumentException("disguise text exceeds its bound");
        }
        return result;
    }

    private static void validateRecord(DisguiseRecord record) {
        Objects.requireNonNull(record, "record");
        if (record.revision() < 1L) {
            throw new IllegalArgumentException("disguise revision is invalid");
        }
        if (record.kind() == DisguiseKind.MOB || record.kind() == DisguiseKind.PRESET) {
            normalizeResource(record.reference());
        }
        if (record.kind() == DisguiseKind.PLAYER && record.profileId() == null) {
            throw new IllegalArgumentException("player disguise profile is missing");
        }
    }

    private static void validatePreset(DisguisePreset preset) {
        Objects.requireNonNull(preset, "preset");
        normalizeId(preset.id());
        if (preset.kind() == DisguiseKind.PRESET) {
            throw new IllegalArgumentException("disguise presets cannot reference another preset");
        }
        if (preset.kind() == DisguiseKind.MOB) {
            normalizeResource(preset.reference());
        }
        if (preset.revision() < 0L) {
            throw new IllegalArgumentException("disguise preset revision is invalid");
        }
    }

    private static DisguiseRecord copyRecord(
            DisguiseRecord current,
            DisguiseKind kind,
            String reference,
            UUID profileId,
            String presetId,
            DisplayLabelMode labelMode,
            EquipmentPolicy equipmentPolicy,
            HitboxPolicy hitboxPolicy,
            ViewerPolicy viewerPolicy,
            PersistencePolicy persistence,
            long revision
    ) {
        return new DisguiseRecord(
                current.disguiseId(),
                current.subjectId(),
                kind,
                reference,
                profileId,
                presetId,
                labelMode,
                equipmentPolicy,
                hitboxPolicy,
                viewerPolicy,
                current.traitsEnabled(),
                current.abilitiesEnabled(),
                current.createdBy(),
                current.createdAt(),
                current.expiresAt(),
                revision,
                persistence,
                current.provider());
    }

    private static void audit(
            UUID actorId,
            String actionId,
            UUID subjectId,
            AuditService.Result result,
            ActionResult.ReasonCode reason
    ) {
        UUID actor = actorId == null ? new UUID(0L, 0L) : actorId;
        AuditService.record(AuditService.Event.metadata(
                UUID.randomUUID(),
                actor,
                actorId == null ? "console" : actor.toString(),
                actorId == null ? "console" : "player",
                actionId,
                subjectId == null ? List.of() : List.of(subjectId),
                result,
                reason,
                "disguise",
                AuditService.AuditClass.ADMIN_ACTION));
    }

    public enum DisguiseKind {
        MOB,
        PLAYER,
        PRESET
    }

    public enum AbilitySlot {
        PRIMARY,
        SECONDARY,
        UTILITY
    }

    public enum Trait {
        FIRE_RESISTANCE,
        WATER_BREATHING,
        CLIMBING,
        REDUCED_FALL_DAMAGE,
        CONTROLLED_FLIGHT,
        SWIM_SPEED,
        WATER_VULNERABILITY,
        DAYLIGHT_SENSITIVITY
    }

    public enum EquipmentPolicy {
        SHOW_REAL_EQUIPMENT,
        HIDE_EQUIPMENT,
        HELD_ITEM_ONLY,
        PRESET_COSMETIC,
        STAFF_REVEAL
    }

    public enum HitboxPolicy {
        PLAYER,
        SUPPORTED_GAMEPLAY
    }

    public enum ViewerPolicy {
        EVERYONE,
        SELF_ONLY,
        OTHERS_ONLY,
        STAFF_ONLY
    }

    public enum DisplayLabelMode {
        NICKNAME,
        DISGUISE_TYPE,
        DISGUISE_PROFILE,
        NICKNAME_PLUS_DISGUISE,
        HIDDEN,
        STAFF_REAL_IDENTITY
    }

    public enum ProjectionMode {
        ENHANCED,
        VANILLA_PROXY,
        TEXT_FALLBACK
    }

    public enum ClearReason {
        COMMAND,
        LOGOUT,
        DEATH,
        EXPIRY,
        PERMISSION_LOSS,
        FEATURE_DISABLED,
        SHUTDOWN
    }

    public record Settings(
            boolean enabled,
            boolean traitsEnabled,
            boolean abilitiesEnabled,
            boolean clearOnLogout,
            boolean clearOnDeath,
            int maximumActive,
            Duration fallbackAbilityCooldown,
            double blazeFireballDamage,
            int blazeFireSeconds
    ) {
        public Settings {
            Objects.requireNonNull(fallbackAbilityCooldown, "fallbackAbilityCooldown");
            if (maximumActive < 1 || maximumActive > HARD_MAXIMUM_ACTIVE
                    || fallbackAbilityCooldown.isNegative()
                    || fallbackAbilityCooldown.compareTo(Duration.ofHours(1)) > 0
                    || !Double.isFinite(blazeFireballDamage)
                    || blazeFireballDamage < 0.0D
                    || blazeFireballDamage > 100.0D
                    || blazeFireSeconds < 0
                    || blazeFireSeconds > 60) {
                throw new IllegalArgumentException("disguise settings are outside hard bounds");
            }
        }

        public static Settings defaults() {
            return new Settings(false, false, false, true, true, 256, Duration.ofSeconds(10), 5.0D, 5);
        }
    }

    public record PersistencePolicy(
            boolean persistDeath,
            boolean persistReconnect,
            boolean persistRestart
    ) {
        public static PersistencePolicy defaults() {
            return new PersistencePolicy(false, false, false);
        }
    }

    public record DisguiseRecord(
            UUID disguiseId,
            UUID subjectId,
            DisguiseKind kind,
            String reference,
            UUID profileId,
            String presetId,
            DisplayLabelMode labelMode,
            EquipmentPolicy equipmentPolicy,
            HitboxPolicy hitboxPolicy,
            ViewerPolicy viewerPolicy,
            boolean traitsEnabled,
            boolean abilitiesEnabled,
            UUID createdBy,
            Instant createdAt,
            Instant expiresAt,
            long revision,
            PersistencePolicy persistence,
            String provider
    ) {
        public DisguiseRecord {
            Objects.requireNonNull(disguiseId, "disguiseId");
            Objects.requireNonNull(subjectId, "subjectId");
            Objects.requireNonNull(kind, "kind");
            reference = normalizeResource(reference);
            presetId = presetId == null || presetId.isBlank() ? "" : normalizeId(presetId);
            Objects.requireNonNull(labelMode, "labelMode");
            Objects.requireNonNull(equipmentPolicy, "equipmentPolicy");
            Objects.requireNonNull(hitboxPolicy, "hitboxPolicy");
            Objects.requireNonNull(viewerPolicy, "viewerPolicy");
            Objects.requireNonNull(createdBy, "createdBy");
            Objects.requireNonNull(createdAt, "createdAt");
            Objects.requireNonNull(persistence, "persistence");
            provider = bounded(provider, 64).toLowerCase(Locale.ROOT);
        }
    }

    public record DisguisePreset(
            String id,
            String displayName,
            DisguiseKind kind,
            String reference,
            UUID profileId,
            DisplayLabelMode labelMode,
            EquipmentPolicy equipmentPolicy,
            HitboxPolicy hitboxPolicy,
            ViewerPolicy viewerPolicy,
            boolean traitsEnabled,
            boolean abilitiesEnabled,
            PersistencePolicy persistence,
            boolean enabled,
            long revision,
            UUID modifiedBy,
            Instant modifiedAt
    ) {
        public DisguisePreset {
            id = normalizeId(id);
            displayName = bounded(displayName, 64);
            Objects.requireNonNull(kind, "kind");
            reference = normalizeResource(reference);
            Objects.requireNonNull(labelMode, "labelMode");
            Objects.requireNonNull(equipmentPolicy, "equipmentPolicy");
            Objects.requireNonNull(hitboxPolicy, "hitboxPolicy");
            Objects.requireNonNull(viewerPolicy, "viewerPolicy");
            Objects.requireNonNull(persistence, "persistence");
            Objects.requireNonNull(modifiedAt, "modifiedAt");
        }
    }

    public record MobAdapter(
            String entityType,
            String displayName,
            boolean enhancedSupported,
            boolean vanillaProxySupported,
            Set<Trait> traits,
            Map<AbilitySlot, AbilityDefinition> abilities
    ) {
        public MobAdapter {
            entityType = normalizeResource(entityType);
            displayName = bounded(displayName, 64);
            traits = Set.copyOf(traits);
            abilities = Map.copyOf(abilities);
        }
    }

    public record AbilityDefinition(
            AbilitySlot slot,
            String id,
            String permission,
            Duration cooldown
    ) {
        public AbilityDefinition {
            Objects.requireNonNull(slot, "slot");
            id = normalizeId(id);
            permission = bounded(permission, 128).toLowerCase(Locale.ROOT);
            Objects.requireNonNull(cooldown, "cooldown");
            if (cooldown.isNegative() || cooldown.compareTo(Duration.ofHours(1)) > 0) {
                throw new IllegalArgumentException("disguise ability cooldown is outside bounds");
            }
        }
    }

    public record AbilityAdmission(
            DisguiseRecord record,
            AbilityDefinition ability,
            Instant admittedAt
    ) {
    }

    public record ProfileSnapshot(
            UUID profileId,
            String profileName,
            String texturesValue,
            String texturesSignature,
            boolean trustedTextures,
            Instant resolvedAt,
            Instant expiresAt
    ) {
        public ProfileSnapshot {
            Objects.requireNonNull(profileId, "profileId");
            profileName = bounded(profileName, 16);
            texturesValue = bounded(texturesValue, 4_096);
            texturesSignature = bounded(texturesSignature, 4_096);
            Objects.requireNonNull(resolvedAt, "resolvedAt");
            Objects.requireNonNull(expiresAt, "expiresAt");
            if (!expiresAt.isAfter(resolvedAt)) {
                throw new IllegalArgumentException("disguise profile expiry is invalid");
            }
            if (trustedTextures && (texturesValue.isBlank() || texturesSignature.isBlank())
                    || texturesValue.isBlank() != texturesSignature.isBlank()) {
                throw new IllegalArgumentException("trusted disguise texture data is incomplete");
            }
        }

        public boolean expired(Instant now) {
            return !expiresAt.isAfter(now);
        }
    }

    public record Projection(DisguiseRecord record, ProjectionMode mode, long revision) {
    }

    private record Snapshot(
            List<DisguisePreset> presets,
            List<DisguiseRecord> persistentActive
    ) {
        private Snapshot {
            presets = presets == null ? List.of() : List.copyOf(presets);
            persistentActive = persistentActive == null ? List.of() : List.copyOf(persistentActive);
        }
    }
}
