package com.enviouse.sef.automation;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.identity.IdentityService;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.message.MessageService;
import com.enviouse.sef.storage.InstantJsonAdapter;
import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.enviouse.sef.vanish.VanishUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public final class FakeIdentityService implements StorageRepository {
    public static final int SCHEMA_VERSION = 1;
    private static final int MAXIMUM_PROFILES = 256;
    private static final int MAXIMUM_SCENES = 128;
    private static final int MAXIMUM_SCENE_EVENTS = 64;
    private static final int MAXIMUM_SCHEDULES = 256;
    private static final Pattern ID = Pattern.compile("[a-z][a-z0-9_.-]{0,63}");
    private static final Set<String> CHAT_PLACEHOLDERS =
            Set.of("prefix", "suffix", "username", "nickname", "message");
    private static final Set<String> CONNECTION_PLACEHOLDERS =
            Set.of("prefix", "suffix", "username", "nickname");
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantJsonAdapter())
            .create();

    private final IdentityService identities;
    private final MessageService messages;
    private final Map<String, FakeProfile> drafts = new LinkedHashMap<>();
    private final Map<String, FakeProfile> published = new LinkedHashMap<>();
    private final Map<String, List<FakeProfile>> history = new LinkedHashMap<>();
    private final Map<String, Scene> scenes = new LinkedHashMap<>();
    private final Map<UUID, ScheduledScene> schedules = new LinkedHashMap<>();
    private StorageService.Document document;
    private RepositoryState state = RepositoryState.NEW;
    private Path path;
    private long revision;
    private long flushedRevision;
    private volatile Formats formats = Formats.defaults();

    public FakeIdentityService(IdentityService identities, MessageService messages) {
        this.identities = Objects.requireNonNull(identities, "identities");
        this.messages = Objects.requireNonNull(messages, "messages");
    }

    @Override
    public String id() {
        return "sef:fake_identity";
    }

    @Override
    public String domain() {
        return "fake identity";
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public synchronized Path path() {
        return path;
    }

    public void configure(Formats replacement) {
        formats = Objects.requireNonNull(replacement, "replacement");
    }

    public Formats formats() {
        return formats;
    }

    @Override
    public synchronized LoadResult load(Path managedRoot) {
        path = Objects.requireNonNull(managedRoot, "managedRoot")
                .resolve("fake-identities.json")
                .toAbsolutePath()
                .normalize();
        drafts.clear();
        published.clear();
        history.clear();
        scenes.clear();
        schedules.clear();
        revision = 0L;
        flushedRevision = 0L;
        boolean existed = Files.exists(path);
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(null);
        if (document == null) {
            state = existed ? RepositoryState.RECOVERY : RepositoryState.MISSING;
            return new LoadResult(state, existed ? "storage unavailable" : "new repository");
        }
        try {
            Snapshot snapshot = GSON.fromJson(document.data(), Snapshot.class);
            if (snapshot == null
                    || snapshot.drafts().size() + snapshot.published().size() > MAXIMUM_PROFILES
                    || snapshot.history().size() > MAXIMUM_PROFILES
                    || snapshot.scenes().size() > MAXIMUM_SCENES
                    || snapshot.schedules().size() > MAXIMUM_SCHEDULES) {
                throw new IllegalStateException("Fake identity collections are outside bounds");
            }
            for (FakeProfile profile : snapshot.drafts()) {
                validateProfile(profile, ProfileState.DRAFT);
                putUnique(drafts, profile);
            }
            for (FakeProfile profile : snapshot.published()) {
                validateProfile(profile, ProfileState.PUBLISHED);
                putUnique(published, profile);
            }
            for (HistoryRecord record : snapshot.history()) {
                String id = normalizeId(record.id());
                if (record.revisions().size() > 32) {
                    throw new IllegalStateException("Fake identity history limit exceeded");
                }
                for (FakeProfile profile : record.revisions()) {
                    validateProfile(profile, ProfileState.PUBLISHED);
                    if (!profile.id().equals(id)) {
                        throw new IllegalStateException("Fake identity history mismatch");
                    }
                }
                history.put(id, record.revisions());
            }
            for (Scene scene : snapshot.scenes()) {
                validateScene(scene);
                if (scenes.putIfAbsent(scene.id(), scene) != null) {
                    throw new IllegalStateException("Duplicate fake scene id");
                }
            }
            for (ScheduledScene schedule : snapshot.schedules()) {
                if (!scenes.containsKey(schedule.sceneId())
                        || schedules.putIfAbsent(schedule.scheduleId(), schedule) != null) {
                    throw new IllegalStateException("Invalid fake scene schedule");
                }
            }
            state = RepositoryState.READY;
            if (document.migrated()) {
                revision++;
            }
            return new LoadResult(state, "loaded " + published.size() + " fake profiles");
        } catch (RuntimeException exception) {
            drafts.clear();
            published.clear();
            history.clear();
            scenes.clear();
            schedules.clear();
            state = RepositoryState.RECOVERY;
            return new LoadResult(state, exception.getClass().getSimpleName());
        }
    }

    public ActionResult<ResolvedIdentity> resolve(String input, ServerPlayer viewer) {
        ActionResult<IdentityService.Identity> real = identities.resolve(input, viewer, true);
        if (real.successful()) {
            IdentityService.Identity identity = real.value();
            return ActionResult.success(new ResolvedIdentity(
                    identity.playerId(),
                    identity.authenticatedUsername(),
                    identity.nickname().isBlank() ? identity.authenticatedUsername() : identity.nickname(),
                    identity.prefix(),
                    identity.suffix(),
                    identity.online(),
                    identity.provenance().name().toLowerCase(Locale.ROOT)));
        }
        FakeProfile profile;
        synchronized (this) {
            profile = published.get(normalizeOptionalId(input));
        }
        if (profile != null && profile.enabled()) {
            return ActionResult.success(profile.identity());
        }
        try {
            IdentityService.Identity synthetic = identities.synthetic(input, "", "", input);
            return ActionResult.success(new ResolvedIdentity(
                    null,
                    synthetic.authenticatedUsername(),
                    synthetic.nickname(),
                    synthetic.prefix(),
                    synthetic.suffix(),
                    false,
                    "default"));
        } catch (IllegalArgumentException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "fake identity is invalid");
        }
    }

    public ResolvedIdentity rank(String prefix, String suffix, String username) {
        IdentityService.Identity synthetic = identities.synthetic(username, prefix, suffix, username);
        return new ResolvedIdentity(
                null,
                synthetic.authenticatedUsername(),
                synthetic.nickname(),
                synthetic.prefix(),
                synthetic.suffix(),
                false,
                "rank_lab");
    }

    public ActionResult<Component> renderChat(ResolvedIdentity identity, String message) {
        String boundedMessage = bounded(message, formats.maximumMessageLength(), false);
        return render(
                formats.chat(),
                CHAT_PLACEHOLDERS,
                values(identity, boundedMessage));
    }

    public ActionResult<Component> renderJoin(ResolvedIdentity identity) {
        return render(formats.join(), CONNECTION_PLACEHOLDERS, values(identity, ""));
    }

    public ActionResult<Component> renderLeave(ResolvedIdentity identity) {
        return render(formats.leave(), CONNECTION_PLACEHOLDERS, values(identity, ""));
    }

    public int broadcast(
            MinecraftServer server,
            ResolvedIdentity identity,
            Component message,
            Audience audience,
            ServerPlayer issuer
    ) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(identity, "identity");
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(audience, "audience");
        int delivered = 0;
        ServerPlayer subject = identity.playerId() == null
                ? null
                : server.getPlayerList().getPlayer(identity.playerId());
        for (ServerPlayer receiver : server.getPlayerList().getPlayers()) {
            if (audience == Audience.STAFF
                    && (issuer == null || !receiver.getUUID().equals(issuer.getUUID()))
                    && !com.enviouse.sef.permissions.PermissionService.has(
                    receiver,
                    com.enviouse.sef.config.PermissionsHandler.phasePermission("commands.fake.scene"))) {
                continue;
            }
            if (subject != null && VanishUtil.isVanished(subject, receiver)) {
                continue;
            }
            receiver.sendSystemMessage(message);
            delivered++;
        }
        return delivered;
    }

    public synchronized ActionResult<FakeProfile> createDraft(
            String id,
            String username,
            String nickname,
            String prefix,
            String suffix,
            UUID actorId
    ) {
        writable();
        String normalizedId = normalizeId(id);
        if (drafts.containsKey(normalizedId) || published.containsKey(normalizedId)) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "fake profile id already exists");
        }
        if (drafts.size() + published.size() >= MAXIMUM_PROFILES) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "fake profile limit reached");
        }
        FakeProfile profile = new FakeProfile(
                SCHEMA_VERSION,
                normalizedId,
                1L,
                ProfileState.DRAFT,
                true,
                bounded(username, 16, false),
                bounded(nickname, 64, true),
                bounded(prefix, 128, true),
                bounded(suffix, 128, true),
                Objects.requireNonNull(actorId, "actorId"),
                Instant.now());
        drafts.put(normalizedId, profile);
        revision++;
        return ActionResult.success(profile);
    }

    public synchronized ActionResult<FakeProfile> publish(
            String id,
            long expectedRevision,
            UUID actorId
    ) {
        writable();
        String normalizedId = normalizeId(id);
        FakeProfile draft = drafts.get(normalizedId);
        if (draft == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "fake profile draft not found");
        }
        if (draft.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "fake profile draft revision changed");
        }
        FakeProfile current = published.get(normalizedId);
        long next = Math.max(draft.revision(), current == null ? 0L : current.revision()) + 1L;
        FakeProfile publication = copy(
                draft,
                next,
                ProfileState.PUBLISHED,
                true,
                Objects.requireNonNull(actorId, "actorId"));
        if (current != null) {
            appendHistory(current);
        }
        appendHistory(publication);
        published.put(normalizedId, publication);
        drafts.remove(normalizedId);
        revision++;
        return ActionResult.success(publication);
    }

    public synchronized ActionResult<FakeProfile> rollback(
            String id,
            long expectedRevision,
            long historyRevision,
            UUID actorId
    ) {
        writable();
        String normalizedId = normalizeId(id);
        FakeProfile current = published.get(normalizedId);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "fake profile not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "fake profile revision changed");
        }
        FakeProfile historical = history.getOrDefault(normalizedId, List.of()).stream()
                .filter(profile -> profile.revision() == historyRevision)
                .findFirst()
                .orElse(null);
        if (historical == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "fake profile history not found");
        }
        FakeProfile replacement = copy(
                historical,
                Math.addExact(current.revision(), 1L),
                ProfileState.PUBLISHED,
                historical.enabled(),
                Objects.requireNonNull(actorId, "actorId"));
        appendHistory(replacement);
        published.put(normalizedId, replacement);
        revision++;
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<Scene> saveScene(Scene scene, long expectedRevision) {
        writable();
        validateScene(scene);
        Scene current = scenes.get(scene.id());
        if (current != null && current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "fake scene revision changed");
        }
        if (current == null && scenes.size() >= MAXIMUM_SCENES) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "fake scene limit reached");
        }
        Scene replacement = new Scene(
                scene.id(),
                current == null ? 1L : Math.addExact(current.revision(), 1L),
                scene.events(),
                scene.audience(),
                scene.enabled());
        scenes.put(replacement.id(), replacement);
        revision++;
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<ScheduledScene> schedule(
            String sceneId,
            long expectedRevision,
            Instant runAt,
            UUID actorId
    ) {
        writable();
        Scene scene = scenes.get(normalizeId(sceneId));
        if (scene == null || !scene.enabled()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "enabled fake scene not found");
        }
        if (scene.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "fake scene revision changed");
        }
        if (schedules.size() >= MAXIMUM_SCHEDULES
                || runAt.isBefore(Instant.now())
                || runAt.isAfter(Instant.now().plus(java.time.Duration.ofDays(365)))) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "fake scene schedule is outside bounds");
        }
        ScheduledScene schedule = new ScheduledScene(
                UUID.randomUUID(),
                scene.id(),
                scene.revision(),
                runAt,
                Objects.requireNonNull(actorId, "actorId"),
                ScheduleState.QUEUED,
                0);
        schedules.put(schedule.scheduleId(), schedule);
        revision++;
        return ActionResult.success(schedule);
    }

    public synchronized List<ScheduledScene> due(Instant now, int maximum) {
        return schedules.values().stream()
                .filter(schedule -> schedule.state() == ScheduleState.QUEUED)
                .filter(schedule -> !schedule.runAt().isAfter(now))
                .sorted(Comparator.comparing(ScheduledScene::runAt))
                .limit(Math.clamp(maximum, 1, 32))
                .toList();
    }

    public synchronized List<DueSceneEvent> pollDueEvents(Instant now, int maximum) {
        writable();
        int limit = Math.clamp(maximum, 1, 32);
        List<DueSceneEvent> due = new ArrayList<>();
        for (ScheduledScene schedule : new ArrayList<>(schedules.values())) {
            if (due.size() >= limit
                    || schedule.state() == ScheduleState.COMPLETED
                    || schedule.state() == ScheduleState.CANCELLED
                    || schedule.state() == ScheduleState.FAILED) {
                continue;
            }
            Scene scene = scenes.get(schedule.sceneId());
            if (scene == null || !scene.enabled() || scene.revision() != schedule.sceneRevision()) {
                schedules.put(schedule.scheduleId(), new ScheduledScene(
                        schedule.scheduleId(),
                        schedule.sceneId(),
                        schedule.sceneRevision(),
                        schedule.runAt(),
                        schedule.actorId(),
                        ScheduleState.FAILED,
                        schedule.nextEvent()));
                revision++;
                continue;
            }
            int next = schedule.nextEvent();
            while (next < scene.events().size() && due.size() < limit) {
                SceneEvent event = scene.events().get(next);
                Instant eventTime = schedule.runAt().plusMillis(event.offsetTicks() * 50L);
                if (eventTime.isAfter(now)) {
                    break;
                }
                due.add(new DueSceneEvent(schedule.scheduleId(), scene, event, schedule.actorId()));
                next++;
            }
            ScheduleState nextState = next >= scene.events().size()
                    ? ScheduleState.COMPLETED
                    : next > schedule.nextEvent() || schedule.state() == ScheduleState.RUNNING
                    ? ScheduleState.RUNNING
                    : schedule.state();
            if (next != schedule.nextEvent() || nextState != schedule.state()) {
                schedules.put(schedule.scheduleId(), new ScheduledScene(
                        schedule.scheduleId(),
                        schedule.sceneId(),
                        schedule.sceneRevision(),
                        schedule.runAt(),
                        schedule.actorId(),
                        nextState,
                        next));
                revision++;
            }
        }
        return List.copyOf(due);
    }

    public synchronized void updateSchedule(ScheduledScene schedule) {
        writable();
        if (!schedules.containsKey(schedule.scheduleId())) {
            throw new IllegalArgumentException("Unknown fake scene schedule");
        }
        schedules.put(schedule.scheduleId(), schedule);
        revision++;
    }

    public synchronized Optional<Scene> scene(String id) {
        return Optional.ofNullable(scenes.get(normalizeId(id)));
    }

    public synchronized List<FakeProfile> profiles() {
        return published.values().stream().sorted(Comparator.comparing(FakeProfile::id)).toList();
    }

    public synchronized List<Scene> scenes() {
        return scenes.values().stream().sorted(Comparator.comparing(Scene::id)).toList();
    }

    @Override
    public void flush() throws IOException {
        Snapshot snapshot;
        Path destination;
        StorageService.Document previous;
        long snapshotRevision;
        synchronized (this) {
            if (path == null || !dirty()) {
                return;
            }
            writable();
            snapshot = new Snapshot(
                    new ArrayList<>(drafts.values()),
                    new ArrayList<>(published.values()),
                    history.entrySet().stream()
                            .map(entry -> new HistoryRecord(entry.getKey(), entry.getValue()))
                            .toList(),
                    new ArrayList<>(scenes.values()),
                    new ArrayList<>(schedules.values()));
            destination = path;
            previous = document;
            snapshotRevision = revision;
        }
        StorageService.write(
                destination,
                domain(),
                SCHEMA_VERSION,
                GSON.toJsonTree(snapshot),
                previous,
                Set.of("/drafts", "/published", "/history", "/scenes", "/schedules"));
        synchronized (this) {
            document = StorageService.read(destination, domain(), SCHEMA_VERSION).orElse(previous);
            flushedRevision = Math.max(flushedRevision, snapshotRevision);
            state = RepositoryState.READY;
        }
    }

    @Override
    public synchronized boolean dirty() {
        return revision > flushedRevision;
    }

    @Override
    public synchronized RepositoryState state() {
        return state;
    }

    private ActionResult<Component> render(
            String template,
            Set<String> placeholders,
            Map<String, Component> values
    ) {
        ActionResult<MessageService.Template> compiled = messages.compile(template, placeholders);
        if (!compiled.successful()) {
            return ActionResult.failure(compiled.reason(), compiled.detail());
        }
        return messages.render(compiled.value(), values);
    }

    private static Map<String, Component> values(ResolvedIdentity identity, String message) {
        return Map.of(
                "prefix", TextFormatter.stringToFormattedText(identity.prefix()),
                "suffix", TextFormatter.stringToFormattedText(identity.suffix()),
                "username", Component.literal(identity.username()),
                "nickname", Component.literal(identity.nickname()),
                "message", Component.literal(message));
    }

    private void appendHistory(FakeProfile profile) {
        List<FakeProfile> revisions = new ArrayList<>(history.getOrDefault(profile.id(), List.of()));
        revisions.removeIf(existing -> existing.revision() == profile.revision());
        revisions.add(profile);
        revisions.sort(Comparator.comparingLong(FakeProfile::revision));
        while (revisions.size() > 32) {
            revisions.removeFirst();
        }
        history.put(profile.id(), List.copyOf(revisions));
    }

    private void writable() {
        if (state == RepositoryState.RECOVERY
                || state == RepositoryState.UNSUPPORTED
                || state == RepositoryState.ERROR
                || state == RepositoryState.CLOSED) {
            throw new IllegalStateException("Fake identity storage is unavailable");
        }
    }

    private static void validateProfile(FakeProfile profile, ProfileState state) {
        Objects.requireNonNull(profile, "profile");
        if (profile.schemaVersion() != SCHEMA_VERSION
                || profile.state() != state
                || profile.revision() < 1L
                || !ID.matcher(profile.id()).matches()) {
            throw new IllegalArgumentException("Fake profile is invalid");
        }
    }

    private static void validateScene(Scene scene) {
        Objects.requireNonNull(scene, "scene");
        if (scene.revision() < 1L
                || scene.events().isEmpty()
                || scene.events().size() > MAXIMUM_SCENE_EVENTS) {
            throw new IllegalArgumentException("Fake scene is invalid");
        }
        long previousOffset = -1L;
        for (SceneEvent event : scene.events()) {
            if (event.offsetTicks() < previousOffset || event.offsetTicks() > 72_000L) {
                throw new IllegalArgumentException("Fake scene event order is invalid");
            }
            previousOffset = event.offsetTicks();
        }
    }

    private static void putUnique(Map<String, FakeProfile> destination, FakeProfile profile) {
        if (destination.putIfAbsent(profile.id(), profile) != null) {
            throw new IllegalStateException("Duplicate fake profile id");
        }
    }

    private static FakeProfile copy(
            FakeProfile source,
            long revision,
            ProfileState state,
            boolean enabled,
            UUID actorId
    ) {
        return new FakeProfile(
                SCHEMA_VERSION,
                source.id(),
                revision,
                state,
                enabled,
                source.username(),
                source.nickname(),
                source.prefix(),
                source.suffix(),
                actorId,
                Instant.now());
    }

    private static String normalizeId(String value) {
        String normalized = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (!ID.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid fake identity id");
        }
        return normalized;
    }

    private static String normalizeOptionalId(String value) {
        String normalized = Objects.requireNonNullElse(value, "").trim().toLowerCase(Locale.ROOT);
        return ID.matcher(normalized).matches() ? normalized : "";
    }

    private static String bounded(String value, int maximumLength, boolean allowBlank) {
        String bounded = Objects.requireNonNullElse(value, "").trim();
        if ((!allowBlank && bounded.isBlank())
                || bounded.length() > maximumLength
                || bounded.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Fake identity text is outside bounds");
        }
        return bounded;
    }

    public record Formats(String chat, String join, String leave, int maximumMessageLength) {
        public Formats {
            chat = bounded(chat, 512, false);
            join = bounded(join, 512, false);
            leave = bounded(leave, 512, false);
            if (maximumMessageLength < 1 || maximumMessageLength > 2048) {
                throw new IllegalArgumentException("Fake message length is outside bounds");
            }
        }

        public static Formats defaults() {
            return new Formats(
                    "{prefix}{nickname}{suffix}&7: &f{message}",
                    "&e{nickname} joined the game",
                    "&e{nickname} left the game",
                    256);
        }
    }

    public record ResolvedIdentity(
            UUID playerId,
            String username,
            String nickname,
            String prefix,
            String suffix,
            boolean online,
            String provenance
    ) {
        public ResolvedIdentity {
            username = bounded(username, 16, false);
            nickname = bounded(nickname, 64, false);
            prefix = bounded(prefix, 128, true);
            suffix = bounded(suffix, 128, true);
            provenance = bounded(provenance, 32, false);
        }
    }

    public record FakeProfile(
            int schemaVersion,
            String id,
            long revision,
            ProfileState state,
            boolean enabled,
            String username,
            String nickname,
            String prefix,
            String suffix,
            UUID changedBy,
            Instant changedAt
    ) {
        public FakeProfile {
            id = normalizeId(id);
            Objects.requireNonNull(state, "state");
            username = bounded(username, 16, false);
            nickname = bounded(nickname, 64, false);
            prefix = bounded(prefix, 128, true);
            suffix = bounded(suffix, 128, true);
            Objects.requireNonNull(changedBy, "changedBy");
            Objects.requireNonNull(changedAt, "changedAt");
        }

        ResolvedIdentity identity() {
            return new ResolvedIdentity(null, username, nickname, prefix, suffix, false, "profile");
        }
    }

    public record Scene(
            String id,
            long revision,
            List<SceneEvent> events,
            Audience audience,
            boolean enabled
    ) {
        public Scene {
            id = normalizeId(id);
            events = List.copyOf(events);
            Objects.requireNonNull(audience, "audience");
        }
    }

    public record SceneEvent(
            long offsetTicks,
            EventType type,
            String identity,
            String message
    ) {
        public SceneEvent {
            Objects.requireNonNull(type, "type");
            identity = bounded(identity, 64, false);
            message = bounded(message, 2048, type != EventType.MESSAGE);
            if (offsetTicks < 0L || offsetTicks > 72_000L) {
                throw new IllegalArgumentException("Fake scene offset is outside bounds");
            }
        }
    }

    public record ScheduledScene(
            UUID scheduleId,
            String sceneId,
            long sceneRevision,
            Instant runAt,
            UUID actorId,
            ScheduleState state,
            int nextEvent
    ) {
        public ScheduledScene {
            Objects.requireNonNull(scheduleId, "scheduleId");
            sceneId = normalizeId(sceneId);
            Objects.requireNonNull(runAt, "runAt");
            Objects.requireNonNull(actorId, "actorId");
            Objects.requireNonNull(state, "state");
            if (sceneRevision < 1L || nextEvent < 0 || nextEvent > MAXIMUM_SCENE_EVENTS) {
                throw new IllegalArgumentException("Fake scene schedule is invalid");
            }
        }
    }

    public record DueSceneEvent(
            UUID scheduleId,
            Scene scene,
            SceneEvent event,
            UUID actorId
    ) {
        public DueSceneEvent {
            Objects.requireNonNull(scheduleId, "scheduleId");
            Objects.requireNonNull(scene, "scene");
            Objects.requireNonNull(event, "event");
            Objects.requireNonNull(actorId, "actorId");
        }
    }

    public enum ProfileState {
        DRAFT,
        PUBLISHED
    }

    public enum Audience {
        SERVER,
        STAFF
    }

    public enum EventType {
        JOIN,
        LEAVE,
        MESSAGE
    }

    public enum ScheduleState {
        QUEUED,
        RUNNING,
        COMPLETED,
        CANCELLED,
        FAILED
    }

    private record Snapshot(
            List<FakeProfile> drafts,
            List<FakeProfile> published,
            List<HistoryRecord> history,
            List<Scene> scenes,
            List<ScheduledScene> schedules
    ) {
        private Snapshot {
            drafts = List.copyOf(drafts == null ? List.of() : drafts);
            published = List.copyOf(published == null ? List.of() : published);
            history = List.copyOf(history == null ? List.of() : history);
            scenes = List.copyOf(scenes == null ? List.of() : scenes);
            schedules = List.copyOf(schedules == null ? List.of() : schedules);
        }
    }

    private record HistoryRecord(String id, List<FakeProfile> revisions) {
        private HistoryRecord {
            revisions = List.copyOf(revisions == null ? List.of() : revisions);
        }
    }
}
