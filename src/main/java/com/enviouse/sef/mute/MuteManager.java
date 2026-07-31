package com.enviouse.sef.mute;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages persistent player mutes.
 *
 * <p>Finite mutes use an absolute expiry timestamp. Time therefore continues
 * while the server is stopped and across player disconnects.</p>
 */
public class MuteManager implements StorageRepository {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type DATA_TYPE = new TypeToken<Map<String, MuteEntry>>() { }.getType();
    private static final String DOMAIN = "mutes";
    private static final int SCHEMA_VERSION = 2;
    private static final int MAXIMUM_MUTES = 100_000;
    private static final long MAXIMUM_DURATION_TICKS = Duration.ofDays(3_650).toSeconds() * 20L;
    private static final int MAXIMUM_NAME_LENGTH = 128;
    private static final int MAXIMUM_REASON_LENGTH = 1_024;

    private final Map<String, MuteEntry> mutes = new ConcurrentHashMap<>();
    private Path filePath;
    private StorageService.Document document;
    private RepositoryState state = RepositoryState.NEW;
    private long revision;
    private long flushedRevision;

    public static class MuteEntry {
        public String playerUUID;
        public String playerName;
        public String adminName;
        public String reason;
        public long remainingTicks;
        public long originalDurationTicks;
        public long mutedAtMillis;
        public long expiresAtEpochMillis;

        public MuteEntry() {
        }

        public MuteEntry(
                String playerUUID,
                String playerName,
                String adminName,
                String reason,
                long durationTicks
        ) {
            this.playerUUID = playerUUID;
            this.playerName = playerName;
            this.adminName = adminName;
            this.reason = reason;
            this.remainingTicks = durationTicks;
            this.originalDurationTicks = durationTicks;
            this.mutedAtMillis = System.currentTimeMillis();
            this.expiresAtEpochMillis = durationTicks < 0
                    ? -1L
                    : Math.addExact(mutedAtMillis, Math.multiplyExact(durationTicks, 50L));
        }

        public boolean isPermanent() {
            return expiresAtEpochMillis < 0;
        }

        public boolean isExpired() {
            return !isPermanent() && System.currentTimeMillis() >= expiresAtEpochMillis;
        }

        public String getDurationString(long ticks) {
            if (ticks < 0) {
                return "Permanent";
            }
            long totalSeconds = ticks / 20;
            if (totalSeconds <= 0) {
                return "0s";
            }
            long hours = totalSeconds / 3_600;
            long minutes = totalSeconds % 3_600 / 60;
            long seconds = totalSeconds % 60;
            StringBuilder result = new StringBuilder();
            if (hours > 0) {
                result.append(hours).append("h ");
            }
            if (minutes > 0) {
                result.append(minutes).append("m ");
            }
            if (seconds > 0 || result.isEmpty()) {
                result.append(seconds).append("s");
            }
            return result.toString().trim();
        }

        public String getRemainingString() {
            if (isPermanent()) {
                return "Permanent";
            }
            long remainingMillis = Math.max(0L, expiresAtEpochMillis - System.currentTimeMillis());
            return getDurationString((remainingMillis + 49L) / 50L);
        }

        public String getOriginalDurationString() {
            return getDurationString(originalDurationTicks);
        }
    }

    public void load(MinecraftServer server) {
        LoadResult result = load(server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                .resolve("serverconfig")
                .resolve("sef"));
        if (result.state() == RepositoryState.READY || result.state() == RepositoryState.MISSING) {
            ServerEssentialsForge.LOGGER.info("[SEF] Loaded {} persistent mute(s)", mutes.size());
        } else {
            ServerEssentialsForge.LOGGER.error(
                    "[SEF] Mute storage is unavailable in state {}. Chat is blocked until recovery",
                    result.state());
        }
    }

    @Override
    public synchronized LoadResult load(Path managedRoot) {
        Path destination = managedRoot.resolve("mutes.json").toAbsolutePath().normalize();
        Path previousPath = filePath;
        RepositoryState previousState = state;
        filePath = destination;
        boolean existed = Files.exists(destination, java.nio.file.LinkOption.NOFOLLOW_LINKS);
        StorageService.Document loadedDocument =
                StorageService.read(destination, DOMAIN, SCHEMA_VERSION).orElse(null);
        if (loadedDocument == null) {
            RepositoryState loadedState = stateFromStorageStatus(destination);
            if (loadedState == RepositoryState.MISSING) {
                if (destination.equals(previousPath)
                        && (previousState == RepositoryState.READY || previousState == RepositoryState.MISSING)) {
                    state = RepositoryState.RECOVERY;
                    return new LoadResult(state, "mute storage disappeared after initialization");
                }
                mutes.clear();
                document = null;
                revision = 0L;
                flushedRevision = 0L;
                state = RepositoryState.MISSING;
                return new LoadResult(state, "new repository");
            }
            state = loadedState;
            return new LoadResult(state, existed ? "storage unavailable" : "storage missing");
        }

        try {
            Map<String, MuteEntry> decoded = GSON.fromJson(loadedDocument.data(), DATA_TYPE);
            if (decoded == null) {
                throw new IllegalStateException("Mute data is null");
            }
            if (decoded.size() > MAXIMUM_MUTES) {
                throw new IllegalStateException("Mute entry limit exceeded");
            }
            long now = System.currentTimeMillis();
            Map<String, MuteEntry> validated = new LinkedHashMap<>();
            boolean normalized = false;
            for (Map.Entry<String, MuteEntry> mapEntry : decoded.entrySet()) {
                MuteEntry entry = normalizeAndValidate(mapEntry.getKey(), mapEntry.getValue(), now);
                if (entry.isExpired()) {
                    normalized = true;
                    continue;
                }
                validated.put(mapEntry.getKey(), entry);
                normalized |= entry != mapEntry.getValue();
            }

            mutes.clear();
            mutes.putAll(validated);
            document = loadedDocument;
            state = RepositoryState.READY;
            revision++;
            flushedRevision = loadedDocument.migrated() || normalized ? revision - 1L : revision;
            if (dirty()) {
                flush();
            }
            return new LoadResult(state, "loaded " + validated.size() + " mutes");
        } catch (IOException | RuntimeException exception) {
            state = RepositoryState.RECOVERY;
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to load mutes", exception);
            return new LoadResult(state, exception.getClass().getSimpleName());
        }
    }

    public synchronized void save() {
        try {
            flush();
        } catch (IOException exception) {
            state = RepositoryState.ERROR;
            throw new IllegalStateException("Failed to save mute storage", exception);
        }
    }

    @Override
    public synchronized void flush() throws IOException {
        if (filePath == null || !dirty()) {
            return;
        }
        writable();
        long snapshotRevision = revision;
        Map<String, MuteEntry> snapshot = new LinkedHashMap<>();
        mutes.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> snapshot.put(entry.getKey(), entry.getValue()));
        StorageService.write(
                filePath,
                DOMAIN,
                SCHEMA_VERSION,
                GSON.toJsonTree(snapshot),
                document,
                Set.of(""));
        document = StorageService.read(filePath, DOMAIN, SCHEMA_VERSION).orElse(document);
        flushedRevision = Math.max(flushedRevision, snapshotRevision);
        state = RepositoryState.READY;
    }

    public synchronized void mutePlayer(
            ServerPlayer target,
            String adminName,
            String reason,
            long durationTicks,
            MinecraftServer server
    ) {
        writableForMutation();
        validateDuration(durationTicks);
        validateText(target.getGameProfile().getName(), "player name", MAXIMUM_NAME_LENGTH);
        validateText(adminName, "admin name", MAXIMUM_NAME_LENGTH);
        validateText(reason, "reason", MAXIMUM_REASON_LENGTH);
        String uuid = target.getUUID().toString();
        MuteEntry entry = new MuteEntry(
                uuid,
                target.getGameProfile().getName(),
                adminName,
                reason,
                durationTicks);
        MuteEntry previous = mutes.put(uuid, entry);
        revision++;
        try {
            save();
        } catch (RuntimeException exception) {
            if (previous == null) {
                mutes.remove(uuid);
            } else {
                mutes.put(uuid, previous);
            }
            revision++;
            throw exception;
        }

        String playerMessage = ConfigHandler.config.muteNotifyPlayerFormat.get()
                .replace("$admin", adminName)
                .replace("$reason", reason)
                .replace("$duration", entry.getOriginalDurationString());
        for (String line : playerMessage.split("\\\\n|\\n")) {
            target.sendSystemMessage(TextFormatter.stringToFormattedText(line));
        }

        String adminMessage = ConfigHandler.config.muteAdminNotifyFormat.get()
                .replace("$player", target.getGameProfile().getName())
                .replace("$admin", adminName)
                .replace("$reason", reason)
                .replace("$duration", entry.getOriginalDurationString());
        notifyAdmins(server, adminMessage);
        ServerEssentialsForge.LOGGER.info(
                "[MUTE] {} muted by {} for {}. Reason, {}",
                target.getGameProfile().getName(),
                adminName,
                entry.getOriginalDurationString(),
                reason);
    }

    public synchronized MuteEntry unmutePlayer(
            UUID playerUUID,
            String adminName,
            MinecraftServer server
    ) {
        writableForMutation();
        validateText(adminName, "admin name", MAXIMUM_NAME_LENGTH);
        String key = playerUUID.toString();
        MuteEntry entry = mutes.remove(key);
        if (entry == null) {
            return null;
        }
        revision++;
        try {
            save();
        } catch (RuntimeException exception) {
            mutes.put(key, entry);
            revision++;
            throw exception;
        }

        ServerPlayer target = server.getPlayerList().getPlayer(playerUUID);
        if (target != null) {
            String message = ConfigHandler.config.unmuteNotifyPlayerFormat.get()
                    .replace("$admin", adminName);
            target.sendSystemMessage(TextFormatter.stringToFormattedText(message));
        }
        String adminMessage = ConfigHandler.config.unmuteAdminNotifyFormat.get()
                .replace("$player", entry.playerName)
                .replace("$admin", adminName);
        notifyAdmins(server, adminMessage);
        ServerEssentialsForge.LOGGER.info("[MUTE] {} unmuted by {}", entry.playerName, adminName);
        return entry;
    }

    public synchronized boolean isMuted(UUID playerUUID) {
        if (!available()) {
            return true;
        }
        MuteEntry entry = mutes.get(playerUUID.toString());
        return entry != null && !entry.isExpired();
    }

    public synchronized MuteEntry getMuteEntry(UUID playerUUID) {
        return mutes.get(playerUUID.toString());
    }

    public synchronized void tick(MinecraftServer server) {
        if (!available()) {
            return;
        }
        List<MuteEntry> expired = mutes.values().stream()
                .filter(MuteEntry::isExpired)
                .sorted(Comparator.comparing(entry -> entry.playerUUID))
                .toList();
        if (expired.isEmpty()) {
            return;
        }

        for (MuteEntry entry : expired) {
            mutes.remove(entry.playerUUID, entry);
        }
        revision++;
        try {
            save();
        } catch (RuntimeException exception) {
            for (MuteEntry entry : expired) {
                mutes.put(entry.playerUUID, entry);
            }
            revision++;
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to persist expired mutes", exception);
            return;
        }

        for (MuteEntry entry : expired) {
            ServerPlayer target = server.getPlayerList().getPlayer(UUID.fromString(entry.playerUUID));
            if (target != null) {
                String message = ConfigHandler.config.unmuteNotifyPlayerFormat.get()
                        .replace("$admin", "Server (mute expired)");
                target.sendSystemMessage(TextFormatter.stringToFormattedText(message));
            }
            String adminMessage = ConfigHandler.config.unmuteAdminNotifyFormat.get()
                    .replace("$player", entry.playerName)
                    .replace("$admin", "Server (mute expired)");
            notifyAdmins(server, adminMessage);
            ServerEssentialsForge.LOGGER.info(
                    "[MUTE] {} auto unmuted because the mute expired",
                    entry.playerName);
        }
    }

    public synchronized Collection<MuteEntry> getAllMutes() {
        return Collections.unmodifiableList(new ArrayList<>(mutes.values()));
    }

    public synchronized boolean available() {
        return state == RepositoryState.READY || state == RepositoryState.MISSING;
    }

    public synchronized boolean shutdown() {
        try {
            flush();
            state = RepositoryState.CLOSED;
            return true;
        } catch (IOException | RuntimeException exception) {
            state = RepositoryState.ERROR;
            ServerEssentialsForge.LOGGER.error("[SEF] Mute shutdown flush did not complete", exception);
            return false;
        }
    }

    @Override
    public String id() {
        return "sef:mutes";
    }

    @Override
    public String domain() {
        return DOMAIN;
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public synchronized Path path() {
        return filePath;
    }

    @Override
    public synchronized boolean dirty() {
        return revision > flushedRevision;
    }

    @Override
    public synchronized RepositoryState state() {
        return state;
    }

    public static long parseDuration(String input) {
        long ticks = com.enviouse.sef.util.DurationParser.toTicks(
                com.enviouse.sef.util.DurationParser.parse(input, true));
        validateDuration(ticks);
        return ticks;
    }

    private static MuteEntry normalizeAndValidate(String key, MuteEntry source, long now) {
        if (source == null) {
            throw new IllegalStateException("Mute entry is null");
        }
        UUID keyUuid = UUID.fromString(key);
        UUID entryUuid = UUID.fromString(Objects.requireNonNull(source.playerUUID, "playerUUID"));
        if (!keyUuid.equals(entryUuid)) {
            throw new IllegalStateException("Mute key does not match its player UUID");
        }
        validateText(source.playerName, "player name", MAXIMUM_NAME_LENGTH);
        validateText(source.adminName, "admin name", MAXIMUM_NAME_LENGTH);
        validateText(source.reason, "reason", MAXIMUM_REASON_LENGTH);
        validateDuration(source.originalDurationTicks);
        if (source.remainingTicks < -1L || source.remainingTicks > MAXIMUM_DURATION_TICKS) {
            throw new IllegalStateException("Remaining mute duration is outside bounds");
        }
        if (source.mutedAtMillis < 0L || source.mutedAtMillis > now + Duration.ofDays(1).toMillis()) {
            throw new IllegalStateException("Mute timestamp is outside bounds");
        }

        if (source.expiresAtEpochMillis != 0L) {
            validateExpiry(source, now);
            return source;
        }

        MuteEntry normalized = copy(source);
        if (source.originalDurationTicks < 0L) {
            normalized.expiresAtEpochMillis = -1L;
        } else {
            long base = source.mutedAtMillis == 0L ? now : source.mutedAtMillis;
            normalized.mutedAtMillis = base;
            normalized.expiresAtEpochMillis = Math.addExact(
                    base,
                    Math.multiplyExact(source.originalDurationTicks, 50L));
        }
        validateExpiry(normalized, now);
        return normalized;
    }

    private static void validateExpiry(MuteEntry entry, long now) {
        if (entry.originalDurationTicks < 0L) {
            if (entry.expiresAtEpochMillis != -1L) {
                throw new IllegalStateException("Permanent mute has a finite expiry");
            }
            return;
        }
        if (entry.expiresAtEpochMillis <= 0L || entry.expiresAtEpochMillis < entry.mutedAtMillis) {
            throw new IllegalStateException("Mute expiry is invalid");
        }
        long maximumExpiry = Math.addExact(now, Math.multiplyExact(MAXIMUM_DURATION_TICKS, 50L));
        if (entry.expiresAtEpochMillis > maximumExpiry) {
            throw new IllegalStateException("Mute expiry is outside bounds");
        }
    }

    private static MuteEntry copy(MuteEntry source) {
        MuteEntry copy = new MuteEntry();
        copy.playerUUID = source.playerUUID;
        copy.playerName = source.playerName;
        copy.adminName = source.adminName;
        copy.reason = source.reason;
        copy.remainingTicks = source.remainingTicks;
        copy.originalDurationTicks = source.originalDurationTicks;
        copy.mutedAtMillis = source.mutedAtMillis;
        copy.expiresAtEpochMillis = source.expiresAtEpochMillis;
        return copy;
    }

    private static void validateDuration(long durationTicks) {
        if (durationTicks == -1L) {
            return;
        }
        if (durationTicks <= 0L || durationTicks > MAXIMUM_DURATION_TICKS) {
            throw new IllegalArgumentException("Mute duration is outside bounds");
        }
    }

    private static void validateText(String value, String field, int maximumLength) {
        if (value == null || value.isBlank() || value.length() > maximumLength) {
            throw new IllegalArgumentException("Invalid mute " + field);
        }
        if (value.chars().anyMatch(character -> Character.isISOControl(character)
                && character != '\n'
                && character != '\t')) {
            throw new IllegalArgumentException("Mute " + field + " contains control characters");
        }
    }

    private synchronized void writableForMutation() {
        if (!available()) {
            throw new IllegalStateException("Mute storage is unavailable in " + state + " state");
        }
    }

    private synchronized void writable() throws IOException {
        if (state == RepositoryState.RECOVERY
                || state == RepositoryState.UNSUPPORTED
                || state == RepositoryState.ERROR
                || state == RepositoryState.CLOSED) {
            throw new IOException("Mute repository is not writable in " + state + " state");
        }
    }

    private static RepositoryState stateFromStorageStatus(Path path) {
        return StorageService.statuses().stream()
                .filter(status -> status.path().equals(path))
                .findFirst()
                .map(status -> switch (status.state()) {
                    case "missing" -> RepositoryState.MISSING;
                    case "unsupported" -> RepositoryState.UNSUPPORTED;
                    case "quarantined", "quarantine failed", "rejected" -> RepositoryState.RECOVERY;
                    default -> RepositoryState.ERROR;
                })
                .orElse(RepositoryState.ERROR);
    }

    private static void notifyAdmins(MinecraftServer server, String message) {
        MutableComponent component = TextFormatter.stringToFormattedText(message);
        for (ServerPlayer operator : server.getPlayerList().getPlayers()) {
            if (PermissionsHandler.playerHasPermission(operator.getUUID(), PermissionsHandler.muteNotify)) {
                operator.sendSystemMessage(component);
            }
        }
    }
}
