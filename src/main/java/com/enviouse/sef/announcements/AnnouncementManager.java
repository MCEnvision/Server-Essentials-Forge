package com.enviouse.sef.announcements;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.audit.SecurityAuditService;
import com.enviouse.sef.commands.CommandRootPolicy;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.storage.StorageService;
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
    private long tickCounter;

    public void load(MinecraftServer server) {
        Path directory = server.getServerDirectory().resolve("serverconfig").resolve("sef");
        filePath = directory.resolve("announcements.json");
        prefsPath = directory.resolve("announcement_prefs.json");
        loadAnnouncements();
        loadPreferences();
        rescheduleAll();
    }

    private void loadAnnouncements() {
        announcements.clear();
        announcementDocument = StorageService.read(
                filePath,
                "announcements",
                ANNOUNCEMENT_SCHEMA_VERSION).orElse(null);
        if (announcementDocument == null) {
            return;
        }

        if (announcementDocument.schemaVersion() == 0) {
            loadLegacyAnnouncements(announcementDocument.data());
        } else {
            loadTypedAnnouncements(announcementDocument.data());
        }
        if (announcementDocument.migrated()) {
            save();
        }
        ServerEssentialsForge.LOGGER.info(
                "[SEF] Loaded {} text and {} command announcement or announcements",
                getTextAnnouncements().size(),
                getCommandAnnouncements().size());
    }

    private void loadLegacyAnnouncements(JsonElement data) {
        List<LegacyAnnouncement> loaded = GSON.fromJson(data, LEGACY_LIST_TYPE);
        if (loaded == null) {
            return;
        }
        for (LegacyAnnouncement legacy : loaded) {
            ScheduledAnnouncement migrated = migrateLegacy(legacy);
            if (migrated != null && getById(migrated.id()) == null) {
                announcements.add(migrated);
            }
        }
    }

    private void loadTypedAnnouncements(JsonElement data) {
        if (data == null || !data.isJsonObject()) {
            ServerEssentialsForge.LOGGER.error("[SEF] Typed announcement data is not an object");
            return;
        }
        JsonObject object = data.getAsJsonObject();
        List<TextAnnouncement> textAnnouncements = object.has("text")
                ? GSON.fromJson(object.get("text"), TEXT_LIST_TYPE)
                : List.of();
        List<CommandAnnouncement> commandAnnouncements = object.has("commands")
                ? GSON.fromJson(object.get("commands"), COMMAND_LIST_TYPE)
                : List.of();
        if (textAnnouncements != null) {
            for (TextAnnouncement announcement : textAnnouncements) {
                if (valid(announcement) && getById(announcement.id()) == null) {
                    announcements.add(announcement);
                }
            }
        }
        if (commandAnnouncements != null) {
            for (CommandAnnouncement announcement : commandAnnouncements) {
                if (valid(announcement) && getById(announcement.id()) == null) {
                    announcements.add(announcement);
                }
            }
        }
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
                && announcement.target() != null
                && announcement.intervalSeconds() > 0
                && announcement.offsetSeconds() >= 0;
    }

    private boolean valid(CommandAnnouncement announcement) {
        if (announcement == null
                || invalidIdentity(announcement.id())
                || announcement.command() == null
                || announcement.intervalSeconds() <= 0
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
        playerToggles.clear();
        preferenceDocument = StorageService.read(
                prefsPath,
                "announcement preferences",
                PREFERENCE_SCHEMA_VERSION).orElse(null);
        if (preferenceDocument == null) {
            return;
        }
        Map<String, List<String>> loaded = GSON.fromJson(preferenceDocument.data(), PREFERENCE_TYPE);
        if (loaded != null) {
            loaded.forEach((uuidText, ids) -> {
                try {
                    if (ids != null) {
                        Set<String> normalizedIds = new HashSet<>();
                        ids.stream()
                                .filter(id -> id != null && !id.isBlank())
                                .map(id -> id.toLowerCase(Locale.ROOT))
                                .limit(1024)
                                .forEach(normalizedIds::add);
                        if (!normalizedIds.isEmpty()) {
                            playerToggles.put(UUID.fromString(uuidText), normalizedIds);
                        }
                    }
                } catch (IllegalArgumentException ignored) {
                }
            });
        }
        if (preferenceDocument.migrated()) {
            savePreferences();
        }
    }

    public void save() {
        if (filePath == null) {
            return;
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
        } catch (IOException exception) {
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to save announcements", exception);
        }
    }

    private void savePreferences() {
        if (prefsPath == null) {
            return;
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
        } catch (IOException exception) {
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to save announcement preferences", exception);
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
        SecurityAuditService.record(SecurityAuditService.AuditEvent.create(
                "announcement",
                "execute command",
                announcement.createdBy(),
                announcement.id(),
                root,
                result,
                reason));
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
        if (!valid || getById(announcement.id()) != null) {
            return false;
        }
        announcements.add(announcement);
        scheduleInitial(announcement);
        save();
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
        boolean removed = announcements.remove(existing);
        if (removed) {
            nextFireAt.remove(key(id));
            save();
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
                save();
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
        savePreferences();
        return enabled;
    }

    public List<TextAnnouncement> getToggleable() {
        return getTextAnnouncements().stream().filter(TextAnnouncement::toggleable).toList();
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
