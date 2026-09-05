package com.enviouse.sef.announcements;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.audit.SecurityAuditService;
import com.enviouse.sef.commands.CommandRootPolicy;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.StorageLifecycle;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class AnnouncementManager {
    private static final int ANNOUNCEMENT_SCHEMA_VERSION = 2;
    private static final int PREFERENCE_SCHEMA_VERSION = 1;
    private static final int MAXIMUM_ANNOUNCEMENTS = 10_000;
    private static final int MAXIMUM_PREFERENCE_PLAYERS = 100_000;
    private static final int MAXIMUM_TOGGLES_PER_PLAYER = 1_024;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LEGACY_LIST_TYPE = new TypeToken<List<LegacyAnnouncement>>() {
    }.getType();
    private static final Type TEXT_LIST_TYPE = new TypeToken<List<TextAnnouncement>>() {
    }.getType();
    private static final Type COMMAND_LIST_TYPE = new TypeToken<List<CommandAnnouncement>>() {
    }.getType();
    private static final Type PREFERENCE_TYPE = new TypeToken<Map<String, List<String>>>() {
    }.getType();

    private final List<ScheduledAnnouncement> announcements = new ArrayList<>();
    private final Map<UUID, Set<String>> playerToggles = new HashMap<>();
    private final Map<String, Long> nextFireAt = new HashMap<>();
    private final Random random = new Random();
    private Path filePath;
    private Path prefsPath;
    private StorageService.Document announcementDocument;
    private StorageService.Document preferenceDocument;
    private StorageRepository.RepositoryState announcementState =
            StorageRepository.RepositoryState.NEW;
    private StorageRepository.RepositoryState preferenceState =
            StorageRepository.RepositoryState.NEW;
    private long tickCounter;

    public void load(MinecraftServer server) {
        load(server.getServerDirectory().resolve("serverconfig").resolve("sef"));
    }

    void load(Path directory) {
        filePath = directory.resolve("announcements.json");
        prefsPath = directory.resolve("announcement_prefs.json");
        loadAnnouncements();
        loadPreferences();
        rescheduleAll();
    }

    private void loadAnnouncements() {
        StorageService.Document candidate = StorageService.read(
                filePath,
                "announcements",
                ANNOUNCEMENT_SCHEMA_VERSION).orElse(null);
        if (candidate == null) {
            StorageRepository.RepositoryState detected = StorageLifecycle.stateFor(filePath);
            announcementState = detected == StorageRepository.RepositoryState.MISSING
                    && announcements.isEmpty()
                    ? detected
                    : StorageRepository.RepositoryState.RECOVERY;
            return;
        }
        try {
            List<ScheduledAnnouncement> loaded = candidate.schemaVersion() == 0
                    ? parseLegacyAnnouncements(candidate.data())
                    : parseTypedAnnouncements(candidate.data());
            announcements.clear();
            announcements.addAll(loaded);
            announcementDocument = candidate;
            announcementState = StorageRepository.RepositoryState.READY;
            if (candidate.migrated() && !save()) {
                announcementState = StorageRepository.RepositoryState.ERROR;
            }
            ServerEssentialsForge.LOGGER.info(
                    "[SEF] Loaded {} text and {} command announcement or announcements",
                    getTextAnnouncements().size(),
                    getCommandAnnouncements().size());
        } catch (RuntimeException exception) {
            announcementState = StorageRepository.RepositoryState.RECOVERY;
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to load announcements", exception);
        }
    }

    private List<ScheduledAnnouncement> parseLegacyAnnouncements(JsonElement data) {
        List<LegacyAnnouncement> loaded = GSON.fromJson(data, LEGACY_LIST_TYPE);
        if (loaded == null || loaded.size() > MAXIMUM_ANNOUNCEMENTS) {
            throw new IllegalStateException("Legacy announcement snapshot is outside bounds");
        }
        List<ScheduledAnnouncement> result = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (LegacyAnnouncement legacy : loaded) {
            ScheduledAnnouncement migrated = migrateLegacy(legacy);
            if (migrated == null || !ids.add(key(migrated.id()))) {
                throw new IllegalStateException("Legacy announcement record is invalid or duplicated");
            }
            result.add(migrated);
        }
        return List.copyOf(result);
    }

    private List<ScheduledAnnouncement> parseTypedAnnouncements(JsonElement data) {
        if (data == null || !data.isJsonObject()) {
            throw new IllegalStateException("Typed announcement data is not an object");
        }
        JsonObject object = data.getAsJsonObject();
        List<TextAnnouncement> textAnnouncements = object.has("text")
                ? GSON.fromJson(object.get("text"), TEXT_LIST_TYPE)
                : List.of();
        List<CommandAnnouncement> commandAnnouncements = object.has("commands")
                ? GSON.fromJson(object.get("commands"), COMMAND_LIST_TYPE)
                : List.of();
        if (textAnnouncements == null || commandAnnouncements == null
                || textAnnouncements.size() + commandAnnouncements.size() > MAXIMUM_ANNOUNCEMENTS) {
            throw new IllegalStateException("Announcement collection is outside bounds");
        }
        List<ScheduledAnnouncement> result = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (TextAnnouncement announcement : textAnnouncements) {
            if (!valid(announcement) || !ids.add(key(announcement.id()))) {
                throw new IllegalStateException("Text announcement is invalid or duplicated");
            }
            result.add(announcement);
        }
        for (CommandAnnouncement announcement : commandAnnouncements) {
            if (!valid(announcement) || !ids.add(key(announcement.id()))) {
                throw new IllegalStateException("Command announcement is invalid or duplicated");
            }
            result.add(announcement);
        }
        return List.copyOf(result);
    }

    private ScheduledAnnouncement migrateLegacy(LegacyAnnouncement legacy) {
        if (legacy == null || invalidIdentity(legacy.id) || legacy.message == null) {
            ServerEssentialsForge.LOGGER.warn("[SEF] Skipping malformed legacy announcement record");
            return null;
        }
        long interval = legacy.intervalSeconds <= 0
                ? Math.max(1, ConfigHandler.config.announcementIntervalSeconds.get())
                : legacy.intervalSeconds;
        long offset = Math.max(0L, legacy.offsetSeconds);
        String type = legacy.type == null ? "text" : legacy.type.toLowerCase(Locale.ROOT);
        if ("command".equals(type)) {
            return new CommandAnnouncement(
                    legacy.id,
                    legacy.message,
                    interval,
                    legacy.enabled,
                    offset,
                    CommandSourcePolicy.SERVER,
                    "legacy migration",
                    Instant.EPOCH.toString());
        }
        if (!"text".equals(type)) {
            ServerEssentialsForge.LOGGER.warn(
                    "[SEF] Skipping legacy announcement {} with unsupported type {}",
                    legacy.id,
                    type);
            return null;
        }
        String target = legacy.target == null || legacy.target.isBlank() ? "@a" : legacy.target;
        return new TextAnnouncement(
                legacy.id,
                legacy.message,
                interval,
                legacy.toggleable,
                target,
                legacy.enabled,
                offset);
    }

    private boolean valid(TextAnnouncement announcement) {
        return announcement != null
                && !invalidIdentity(announcement.id())
                && announcement.message() != null
                && announcement.message().length() <= 4_096
                && announcement.message().codePoints().noneMatch(Character::isISOControl)
                && announcement.target() != null
                && announcement.target().length() <= 128
                && announcement.target().codePoints().noneMatch(Character::isISOControl)
                && announcement.intervalSeconds() > 0
                && announcement.intervalSeconds() <= 31_557_600L
                && announcement.offsetSeconds() >= 0;
    }

    private boolean valid(CommandAnnouncement announcement) {
        if (announcement == null
                || invalidIdentity(announcement.id())
                || announcement.command() == null
                || announcement.command().length() > 4_096
                || announcement.intervalSeconds() <= 0
                || announcement.intervalSeconds() > 31_557_600L
                || announcement.offsetSeconds() < 0
                || announcement.sourcePolicy() != CommandSourcePolicy.SERVER) {
            return false;
        }
        return CommandRootPolicy.evaluate(
                announcement.command(),
                ConfigHandler.config.commandAnnouncementAllowedCommands.get(),
                ConfigHandler.config.commandAnnouncementDeniedCommands.get(),
                ConfigHandler.config.commandAnnouncementMaximumCommandLength.get(),
                ConfigHandler.config.commandAnnouncementAllowLeadingSlash.get(),
                ConfigHandler.config.commandAnnouncementAllowSelectors.get()).allowed();
    }

    private static boolean invalidIdentity(String id) {
        return id == null
                || id.isBlank()
                || id.length() > 64
                || id.codePoints().anyMatch(Character::isISOControl);
    }

    private void loadPreferences() {
        StorageService.Document candidate = StorageService.read(
                prefsPath,
                "announcement preferences",
                PREFERENCE_SCHEMA_VERSION).orElse(null);
        if (candidate == null) {
            StorageRepository.RepositoryState detected = StorageLifecycle.stateFor(prefsPath);
            preferenceState = detected == StorageRepository.RepositoryState.MISSING
                    && playerToggles.isEmpty()
                    ? detected
                    : StorageRepository.RepositoryState.RECOVERY;
            return;
        }
        try {
            Map<String, List<String>> loaded = GSON.fromJson(candidate.data(), PREFERENCE_TYPE);
            if (loaded == null || loaded.size() > MAXIMUM_PREFERENCE_PLAYERS) {
                throw new IllegalStateException("Announcement preference snapshot is outside bounds");
            }
            Map<UUID, Set<String>> validated = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : loaded.entrySet()) {
                UUID playerId = UUID.fromString(entry.getKey());
                List<String> ids = entry.getValue();
                if (ids == null || ids.size() > MAXIMUM_TOGGLES_PER_PLAYER) {
                    throw new IllegalStateException("Announcement preference group is outside bounds");
                }
                Set<String> normalized = new HashSet<>();
                for (String id : ids) {
                    if (invalidIdentity(id) || !normalized.add(key(id))) {
                        throw new IllegalStateException("Announcement preference is invalid or duplicated");
                    }
                }
                if (!normalized.isEmpty()) {
                    validated.put(playerId, Set.copyOf(normalized));
                }
            }
            playerToggles.clear();
            validated.forEach((playerId, ids) -> playerToggles.put(playerId, new HashSet<>(ids)));
            preferenceDocument = candidate;
            preferenceState = StorageRepository.RepositoryState.READY;
            if (candidate.migrated() && !savePreferences()) {
                preferenceState = StorageRepository.RepositoryState.ERROR;
            }
        } catch (RuntimeException exception) {
            preferenceState = StorageRepository.RepositoryState.RECOVERY;
            ServerEssentialsForge.LOGGER.error(
                    "[SEF] Failed to load announcement preferences",
                    exception);
        }
    }

    public boolean save() {
        if (filePath == null) {
            return true;
        }
        if (!StorageLifecycle.writable(announcementState)) {
            return false;
        }
        JsonObject data = new JsonObject();
        data.add("text", GSON.toJsonTree(getTextAnnouncements()));
        data.add("commands", GSON.toJsonTree(getCommandAnnouncements()));
        try {
            StorageService.write(
                    filePath,
                    "announcements",
                    ANNOUNCEMENT_SCHEMA_VERSION,
                    data,
                    announcementDocument);
            announcementDocument = StorageService.read(
                    filePath,
                    "announcements",
                    ANNOUNCEMENT_SCHEMA_VERSION).orElse(announcementDocument);
            announcementState = StorageRepository.RepositoryState.READY;
            return true;
        } catch (IOException | RuntimeException exception) {
            announcementState = StorageRepository.RepositoryState.ERROR;
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to save announcements", exception);
            return false;
        }
    }

    private boolean savePreferences() {
        if (prefsPath == null) {
            return true;
        }
        if (!StorageLifecycle.writable(preferenceState)) {
            return false;
        }
        Map<String, List<String>> output = new HashMap<>();
        playerToggles.forEach((uuid, ids) -> output.put(uuid.toString(), new ArrayList<>(ids)));
        try {
            StorageService.write(
                    prefsPath,
                    "announcement preferences",
                    PREFERENCE_SCHEMA_VERSION,
                    GSON.toJsonTree(output),
                    preferenceDocument);
            preferenceDocument = StorageService.read(
                    prefsPath,
                    "announcement preferences",
                    PREFERENCE_SCHEMA_VERSION).orElse(preferenceDocument);
            preferenceState = StorageRepository.RepositoryState.READY;
            return true;
        } catch (IOException | RuntimeException exception) {
            preferenceState = StorageRepository.RepositoryState.ERROR;
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to save announcement preferences", exception);
            return false;
        }
    }

    private void rescheduleAll() {
        nextFireAt.clear();
        for (ScheduledAnnouncement announcement : announcements) {
            scheduleInitial(announcement);
        }
    }

    private void scheduleInitial(ScheduledAnnouncement announcement) {
        long intervalTicks = secondsToTicks(announcement.intervalSeconds());
        long offsetTicks = secondsToTicks(announcement.offsetSeconds());
        if (intervalTicks < 1L || offsetTicks < 0L) {
            ServerEssentialsForge.LOGGER.warn(
                    "[SEF] Announcement {} has an interval or offset that exceeds tick storage",
                    announcement.id());
            return;
        }
        long stagger = random.nextLong(Math.max(1L, intervalTicks / 4L));
        try {
            nextFireAt.put(
                    key(announcement.id()),
                    Math.addExact(Math.addExact(tickCounter, offsetTicks), stagger));
        } catch (ArithmeticException exception) {
            ServerEssentialsForge.LOGGER.warn(
                    "[SEF] Announcement {} initial schedule exceeds tick storage",
                    announcement.id());
        }
    }

    public void tick(MinecraftServer server) {
        tickCounter++;
        for (ScheduledAnnouncement announcement : announcements) {
            if (!announcement.enabled()) {
                continue;
            }
            String key = key(announcement.id());
            Long next = nextFireAt.get(key);
            if (next == null) {
                scheduleInitial(announcement);
                continue;
            }
            if (tickCounter < next) {
                continue;
            }
            fire(server, announcement);
            long intervalTicks = secondsToTicks(announcement.intervalSeconds());
            if (intervalTicks < 1L) {
                nextFireAt.remove(key);
                continue;
            }
            try {
                nextFireAt.put(key, Math.addExact(tickCounter, intervalTicks));
            } catch (ArithmeticException exception) {
                nextFireAt.remove(key);
                ServerEssentialsForge.LOGGER.warn(
                        "[SEF] Announcement {} next schedule exceeds tick storage",
                        announcement.id());
            }
        }
    }

    private void fire(MinecraftServer server, ScheduledAnnouncement announcement) {
        if (announcement instanceof TextAnnouncement text) {
            broadcastText(server, text.message(), text.target(), text.id(), text.toggleable());
            return;
        }
        if (announcement instanceof CommandAnnouncement command) {
            fireCommand(server, command);
        }
    }

    void fireCommand(MinecraftServer server, CommandAnnouncement announcement) {
        if (!ConfigHandler.config.enableCommandAnnouncements.get()) {
            auditCommand(announcement, "", "denied", "feature disabled");
            return;
        }
        CommandRootPolicy.Decision decision = CommandRootPolicy.evaluate(
                announcement.command(),
                ConfigHandler.config.commandAnnouncementAllowedCommands.get(),
                ConfigHandler.config.commandAnnouncementDeniedCommands.get(),
                ConfigHandler.config.commandAnnouncementMaximumCommandLength.get(),
                ConfigHandler.config.commandAnnouncementAllowLeadingSlash.get(),
                ConfigHandler.config.commandAnnouncementAllowSelectors.get());
        if (!decision.allowed()) {
            auditCommand(announcement, "", "denied", decision.reason());
            ServerEssentialsForge.LOGGER.warn(
                    "[SEF] Command announcement {} denied at execution because {}",
                    announcement.id(),
                    decision.reason());
            return;
        }

        try {
            java.util.concurrent.atomic.AtomicBoolean outcomeRecorded =
                    new java.util.concurrent.atomic.AtomicBoolean();
            var source = server.createCommandSourceStack().withCallback((successful, value) -> {
                if (!outcomeRecorded.compareAndSet(false, true)) {
                    return;
                }
                auditCommand(
                        announcement,
                        decision.root(),
                        successful ? "success" : "failed",
                        successful ? "result " + value : "command reported failure");
                if (successful) {
                    ServerEssentialsForge.LOGGER.info(
                            "[SEF] Command announcement {} completed root {} with result {}",
                            announcement.id(),
                            decision.root(),
                            value);
                } else {
                    ServerEssentialsForge.LOGGER.warn(
                            "[SEF] Command announcement {} reported failure for root {}",
                            announcement.id(),
                            decision.root());
                }
            });
            switch (announcement.sourcePolicy()) {
                case SERVER -> server.getCommands().performPrefixedCommand(
                        source,
                        decision.command());
            }
            if (!outcomeRecorded.get()) {
                auditCommand(
                        announcement,
                        decision.root(),
                        "outcome_unknown",
                        "queued without synchronous result");
            }
        } catch (RuntimeException exception) {
            auditCommand(
                    announcement,
                    decision.root(),
                    "failed",
                    exception.getClass().getSimpleName());
            ServerEssentialsForge.LOGGER.error(
                    "[SEF] Command announcement {} failed for root {}",
                    announcement.id(),
                    decision.root(),
                    exception);
        }
    }

    private static void auditCommand(
            CommandAnnouncement announcement,
            String root,
            String result,
            String reason
    ) {
        UUID correlationId = UUID.nameUUIDFromBytes(
                ("sef:announcement:" + announcement.id()).getBytes(StandardCharsets.UTF_8));
        AuditService.record(AuditService.Event.interaction(
                SecurityAuditService.currentSessionId(),
                actorId(announcement.createdBy()),
                announcement.createdBy(),
                "SCHEDULED_TASK",
                "sef:announcement.command",
                List.of(),
                Map.of(
                        "announcement_id", announcement.id(),
                        "root", root,
                        "result", result,
                        "reason", reason),
                auditResult(result),
                auditReason(result, reason),
                "announcement",
                correlationId,
                AuditService.RedactionClass.METADATA,
                AuditService.AuditClass.DELEGATED_EXECUTION));
    }

    private static UUID actorId(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return UUID.nameUUIDFromBytes(("sef:announcement-owner:" + value)
                    .getBytes(StandardCharsets.UTF_8));
        }
    }

    private static AuditService.Result auditResult(String result) {
        return switch (result.toLowerCase(Locale.ROOT)) {
            case "success" -> AuditService.Result.SUCCESS;
            case "denied" -> AuditService.Result.REJECTED;
            case "outcome_unknown" -> AuditService.Result.OUTCOME_UNKNOWN;
            default -> AuditService.Result.FAILED;
        };
    }

    private static ActionResult.ReasonCode auditReason(String result, String detail) {
        if ("success".equalsIgnoreCase(result)) {
            return ActionResult.ReasonCode.SUCCESS;
        }
        String candidate = detail.toUpperCase(Locale.ROOT).replace(' ', '_');
        try {
            return ActionResult.ReasonCode.valueOf(candidate);
        } catch (IllegalArgumentException exception) {
            return switch (result.toLowerCase(Locale.ROOT)) {
                case "denied" -> ActionResult.ReasonCode.POLICY_DENIED;
                case "outcome_unknown" -> ActionResult.ReasonCode.PROVIDER_ERROR;
                default -> ActionResult.ReasonCode.PROVIDER_ERROR;
            };
        }
    }

    private static long secondsToTicks(long seconds) {
        try {
            return Math.multiplyExact(seconds, 20L);
        } catch (ArithmeticException exception) {
            return -1L;
        }
    }

    public void broadcastText(
            MinecraftServer server,
            String message,
            String target,
            String announcementId,
            boolean toggleable
    ) {
        List<ServerPlayer> recipients = resolveTargets(server, target);
        MutableComponent component = renderMultiLine(message);
        for (ServerPlayer player : recipients) {
            if (toggleable
                    && announcementId != null
                    && isToggledOff(player.getUUID(), announcementId)
                    && !PermissionsHandler.playerHasPermission(
                            player.getUUID(),
                            PermissionsHandler.announcementBypass)) {
                continue;
            }
            player.sendSystemMessage(component);
        }
    }

    private List<ServerPlayer> resolveTargets(MinecraftServer server, String target) {
        if (target == null
                || target.isBlank()
                || "@a".equals(target)
                || "@server".equalsIgnoreCase(target)
                || "all".equalsIgnoreCase(target)) {
            return new ArrayList<>(server.getPlayerList().getPlayers());
        }
        ServerPlayer player = server.getPlayerList().getPlayerByName(target);
        if (player != null) {
            return List.of(player);
        }
        ServerEssentialsForge.LOGGER.warn(
                "[SEF] Announcement target did not match an online player. Delivery skipped");
        return List.of();
    }

    public static MutableComponent renderMultiLine(String raw) {
        String normalized = raw.replaceAll("(?i)<br\\s*/?>", "\n");
        return TextFormatter.stringToFormattedText(normalized);
    }

    public List<TextAnnouncement> getTextAnnouncements() {
        return announcements.stream()
                .filter(TextAnnouncement.class::isInstance)
                .map(TextAnnouncement.class::cast)
                .toList();
    }

    public List<CommandAnnouncement> getCommandAnnouncements() {
        return announcements.stream()
                .filter(CommandAnnouncement.class::isInstance)
                .map(CommandAnnouncement.class::cast)
                .toList();
    }

    public ScheduledAnnouncement getById(String id) {
        if (id == null) {
            return null;
        }
        for (ScheduledAnnouncement announcement : announcements) {
            if (announcement.id().equalsIgnoreCase(id)) {
                return announcement;
            }
        }
        return null;
    }

    public boolean add(TextAnnouncement announcement) {
        return addTyped(announcement);
    }

    public boolean add(CommandAnnouncement announcement) {
        return addTyped(announcement);
    }

    private boolean addTyped(ScheduledAnnouncement announcement) {
        boolean valid = announcement instanceof TextAnnouncement text
                ? valid(text)
                : announcement instanceof CommandAnnouncement command && valid(command);
        if (!valid
                || getById(announcement.id()) != null
                || announcements.size() >= MAXIMUM_ANNOUNCEMENTS) {
            return false;
        }
        requireAnnouncementStorage();
        announcements.add(announcement);
        scheduleInitial(announcement);
        if (!save()) {
            announcements.remove(announcement);
            nextFireAt.remove(key(announcement.id()));
            throw new IllegalStateException("Announcement could not be persisted");
        }
        return true;
    }

    public boolean removeText(String id) {
        return removeTyped(id, TextAnnouncement.class);
    }

    public boolean removeCommand(String id) {
        return removeTyped(id, CommandAnnouncement.class);
    }

    private boolean removeTyped(String id, Class<? extends ScheduledAnnouncement> type) {
        ScheduledAnnouncement existing = getById(id);
        if (existing == null || !type.isInstance(existing)) {
            return false;
        }
        requireAnnouncementStorage();
        boolean removed = announcements.remove(existing);
        if (removed) {
            nextFireAt.remove(key(id));
            if (!save()) {
                announcements.add(existing);
                scheduleInitial(existing);
                throw new IllegalStateException("Announcement removal could not be persisted");
            }
        }
        return removed;
    }

    public boolean modifyText(
            String id,
            long intervalSeconds,
            boolean toggleable,
            String target,
            String message
    ) {
        for (int index = 0; index < announcements.size(); index++) {
            ScheduledAnnouncement current = announcements.get(index);
            if (current instanceof TextAnnouncement text
                    && text.id().equalsIgnoreCase(id)) {
                requireAnnouncementStorage();
                TextAnnouncement replacement = text.with(
                        intervalSeconds,
                        toggleable,
                        target,
                        message);
                if (!valid(replacement)) {
                    return false;
                }
                announcements.set(index, replacement);
                scheduleInitial(replacement);
                if (!save()) {
                    announcements.set(index, current);
                    scheduleInitial(current);
                    throw new IllegalStateException("Announcement modification could not be persisted");
                }
                return true;
            }
        }
        return false;
    }

    public boolean isToggledOff(UUID uuid, String announcementId) {
        Set<String> disabled = playerToggles.get(uuid);
        return disabled != null && disabled.contains(key(announcementId));
    }

    public boolean togglePlayer(UUID uuid, String announcementId) {
        if (prefsPath != null && !StorageLifecycle.writable(preferenceState)) {
            throw new IllegalStateException(
                    "Announcement preference storage is unavailable in " + preferenceState + " state");
        }
        String key = key(announcementId);
        Set<String> disabled = playerToggles.computeIfAbsent(uuid, ignored -> new HashSet<>());
        boolean enabled;
        if (disabled.remove(key)) {
            enabled = true;
        } else {
            disabled.add(key);
            enabled = false;
        }
        if (disabled.isEmpty()) {
            playerToggles.remove(uuid);
        }
        if (!savePreferences()) {
            if (enabled) {
                playerToggles.computeIfAbsent(uuid, ignored -> new HashSet<>()).add(key);
            } else {
                Set<String> rollback = playerToggles.get(uuid);
                if (rollback != null) {
                    rollback.remove(key);
                    if (rollback.isEmpty()) {
                        playerToggles.remove(uuid);
                    }
                }
            }
            throw new IllegalStateException("Announcement preference could not be persisted");
        }
        return enabled;
    }

    StorageRepository.RepositoryState announcementState() {
        return announcementState;
    }

    StorageRepository.RepositoryState preferenceState() {
        return preferenceState;
    }

    public List<TextAnnouncement> getToggleable() {
        return getTextAnnouncements().stream().filter(TextAnnouncement::toggleable).toList();
    }

    private void requireAnnouncementStorage() {
        if (filePath != null && !StorageLifecycle.writable(announcementState)) {
            throw new IllegalStateException(
                    "Announcement storage is unavailable in " + announcementState + " state");
        }
    }

    private static String key(String id) {
        return id.toLowerCase(Locale.ROOT);
    }

    private static final class LegacyAnnouncement {
        String id;
        String type;
        String message;
        long intervalSeconds;
        boolean toggleable;
        String target;
        boolean enabled;
        long offsetSeconds;
    }
}
