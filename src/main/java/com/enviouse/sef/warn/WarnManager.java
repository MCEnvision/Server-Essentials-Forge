package com.enviouse.sef.warn;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.storage.StorageLifecycle;
import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WarnManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type DATA_TYPE = new TypeToken<Map<String, List<WarnEntry>>>() {
    }.getType();
    private static final int MAXIMUM_PLAYERS = 100_000;
    private static final int MAXIMUM_WARNINGS = 100_000;
    private static final int MAXIMUM_WARNINGS_PER_PLAYER = 10_000;
    private static final long MAXIMUM_DURATION_MILLIS = 315_576_000_000L;

    private final Map<String, List<WarnEntry>> warns = new LinkedHashMap<>();
    private Path filePath;
    private StorageService.Document document;
    private StorageRepository.RepositoryState state = StorageRepository.RepositoryState.NEW;

    public static class WarnEntry {
        public int id;
        public String reason;
        public String adminName;
        public String adminUuid;
        public String timestamp;
        public long durationMs;
        public boolean removed;

        public WarnEntry() {
        }

        public WarnEntry(int id, String reason, String adminName, String adminUuid, String timestamp, long durationMs) {
            this.id = id;
            this.reason = reason;
            this.adminName = adminName;
            this.adminUuid = adminUuid;
            this.timestamp = timestamp;
            this.durationMs = durationMs;
        }

        public boolean isExpired() {
            if (durationMs < 0) {
                return false;
            }
            return !Instant.parse(timestamp).plusMillis(durationMs).isAfter(Instant.now());
        }

        public String getDurationString() {
            if (durationMs < 0) {
                return "Permanent";
            }
            long totalSeconds = durationMs / 1000;
            if (totalSeconds < 60) {
                return totalSeconds + "s";
            }
            if (totalSeconds < 3600) {
                return totalSeconds / 60 + "m " + totalSeconds % 60 + "s";
            }
            if (totalSeconds < 86400) {
                return totalSeconds / 3600 + "h " + totalSeconds % 3600 / 60 + "m";
            }
            return totalSeconds / 86400 + "d " + totalSeconds % 86400 / 3600 + "h";
        }
    }

    public void load(MinecraftServer server) {
        load(server.getServerDirectory().resolve("serverconfig").resolve("sef"));
    }

    synchronized void load(Path directory) {
        Path candidatePath = directory.resolve("warns.json").toAbsolutePath().normalize();
        StorageService.Document candidate =
                StorageService.read(candidatePath, "warnings", 1).orElse(null);
        if (candidate == null) {
            StorageRepository.RepositoryState detected = StorageLifecycle.stateFor(candidatePath);
            state = detected == StorageRepository.RepositoryState.MISSING && warns.isEmpty()
                    ? detected
                    : StorageRepository.RepositoryState.RECOVERY;
            filePath = candidatePath;
            return;
        }
        try {
            Map<String, List<WarnEntry>> loaded = GSON.fromJson(candidate.data(), DATA_TYPE);
            Map<String, List<WarnEntry>> validated = validateSnapshot(loaded);
            warns.clear();
            warns.putAll(validated);
            filePath = candidatePath;
            document = candidate;
            state = StorageRepository.RepositoryState.READY;
            int total = warns.values().stream().mapToInt(List::size).sum();
            ServerEssentialsForge.LOGGER.info(
                    "[SEF] Loaded {} warning or warnings for {} player or players",
                    total,
                    warns.size());
            if (candidate.migrated() && !save()) {
                state = StorageRepository.RepositoryState.ERROR;
            }
        } catch (RuntimeException exception) {
            filePath = candidatePath;
            state = StorageRepository.RepositoryState.RECOVERY;
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to load warnings", exception);
        }
    }

    public synchronized boolean save() {
        if (filePath == null || !StorageLifecycle.writable(state)) {
            return false;
        }
        try {
            StorageService.write(filePath, "warnings", 1, GSON.toJsonTree(warns), document, Set.of(""));
            document = StorageService.read(filePath, "warnings", 1).orElse(document);
            state = StorageRepository.RepositoryState.READY;
            return true;
        } catch (IOException | RuntimeException exception) {
            state = StorageRepository.RepositoryState.ERROR;
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to save warnings", exception);
            return false;
        }
    }

    public synchronized WarnEntry addWarn(
            UUID playerUUID,
            String reason,
            String adminName,
            String adminUuid,
            long durationMs
    ) {
        writable();
        String key = playerUUID.toString();
        if (!warns.containsKey(key) && warns.size() >= MAXIMUM_PLAYERS) {
            throw new IllegalStateException("Warning player capacity is full");
        }
        List<WarnEntry> list = warns.computeIfAbsent(key, ignored -> new ArrayList<>());
        if (list.size() >= MAXIMUM_WARNINGS_PER_PLAYER
                || warns.values().stream().mapToInt(List::size).sum() >= MAXIMUM_WARNINGS) {
            throw new IllegalStateException("Warning capacity is full");
        }
        int nextId = Math.addExact(list.stream().mapToInt(entry -> entry.id).max().orElse(0), 1);
        WarnEntry entry = new WarnEntry(
                nextId,
                bounded(reason, 512),
                bounded(adminName, 64),
                UUID.fromString(adminUuid).toString(),
                Instant.now().toString(),
                validDuration(durationMs));
        list.add(entry);
        if (!save()) {
            list.remove(entry);
            if (list.isEmpty()) {
                warns.remove(key);
            }
            throw new IllegalStateException("Warning could not be persisted");
        }
        return copy(entry);
    }

    public synchronized boolean removeWarn(UUID playerUUID, int warnId) {
        writable();
        List<WarnEntry> list = warns.get(playerUUID.toString());
        if (list == null) {
            return false;
        }
        for (WarnEntry entry : list) {
            if (entry.id == warnId && !entry.removed) {
                entry.removed = true;
                if (!save()) {
                    entry.removed = false;
                    throw new IllegalStateException("Warning removal could not be persisted");
                }
                return true;
            }
        }
        return false;
    }

    public synchronized List<WarnEntry> getWarns(UUID playerUUID) {
        return warns.getOrDefault(playerUUID.toString(), Collections.emptyList()).stream()
                .filter(entry -> !entry.removed)
                .map(WarnManager::copy)
                .toList();
    }

    public synchronized List<WarnEntry> getActiveWarns(UUID playerUUID) {
        return getWarns(playerUUID).stream().filter(entry -> !entry.isExpired()).toList();
    }

    public synchronized StorageRepository.RepositoryState state() {
        return state;
    }

    public static long parseDuration(String input) {
        return com.enviouse.sef.util.DurationParser.toMilliseconds(
                com.enviouse.sef.util.DurationParser.parse(input, true));
    }

    private static Map<String, List<WarnEntry>> validateSnapshot(Map<String, List<WarnEntry>> loaded) {
        if (loaded == null || loaded.size() > MAXIMUM_PLAYERS) {
            throw new IllegalStateException("Warning snapshot is missing or outside bounds");
        }
        Map<String, List<WarnEntry>> validated = new LinkedHashMap<>();
        int total = 0;
        for (Map.Entry<String, List<WarnEntry>> group : loaded.entrySet()) {
            String playerId = UUID.fromString(group.getKey()).toString();
            List<WarnEntry> entries = group.getValue();
            if (entries == null || entries.size() > MAXIMUM_WARNINGS_PER_PLAYER) {
                throw new IllegalStateException("Warning group is outside bounds");
            }
            Set<Integer> ids = new HashSet<>();
            List<WarnEntry> copies = new ArrayList<>();
            for (WarnEntry entry : entries) {
                validate(entry);
                if (!ids.add(entry.id) || ++total > MAXIMUM_WARNINGS) {
                    throw new IllegalStateException("Warning identifiers or count are invalid");
                }
                copies.add(copy(entry));
            }
            validated.put(playerId, copies);
        }
        return validated;
    }

    private static void validate(WarnEntry entry) {
        if (entry == null || entry.id < 1) {
            throw new IllegalStateException("Warning record is invalid");
        }
        bounded(entry.reason, 512);
        bounded(entry.adminName, 64);
        UUID.fromString(entry.adminUuid);
        Instant.parse(entry.timestamp);
        validDuration(entry.durationMs);
    }

    private static long validDuration(long value) {
        if (value != -1L && (value < 0L || value > MAXIMUM_DURATION_MILLIS)) {
            throw new IllegalArgumentException("Warning duration is outside bounds");
        }
        return value;
    }

    private static String bounded(String value, int maximum) {
        String normalized = value == null ? "" : value.strip();
        if (normalized.isBlank()
                || normalized.length() > maximum
                || normalized.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Warning text is outside bounds");
        }
        return normalized;
    }

    private static WarnEntry copy(WarnEntry entry) {
        WarnEntry copy = new WarnEntry(
                entry.id,
                entry.reason,
                entry.adminName,
                entry.adminUuid,
                entry.timestamp,
                entry.durationMs);
        copy.removed = entry.removed;
        return copy;
    }

    private void writable() {
        if (!StorageLifecycle.writable(state)) {
            throw new IllegalStateException("Warning storage is unavailable in " + state + " state");
        }
    }
}
