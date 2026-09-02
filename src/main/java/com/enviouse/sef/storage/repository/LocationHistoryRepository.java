package com.enviouse.sef.storage.repository;

import com.enviouse.sef.storage.StorageService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class LocationHistoryRepository implements StorageRepository {
    private static final int SCHEMA_VERSION = 1;
    private static final String DOMAIN = "location history";
    private static final int MAXIMUM_STORED_PLAYERS = 100_000;

    private final int maximumEntriesPerPlayer;
    private final Map<UUID, Deque<LocationRecord>> records = new LinkedHashMap<>();
    private Path path;
    private StorageService.Document document;
    private RepositoryState state = RepositoryState.NEW;
    private long revision;
    private long flushedRevision;

    public LocationHistoryRepository(int maximumEntriesPerPlayer) {
        if (maximumEntriesPerPlayer < 1 || maximumEntriesPerPlayer > 1000) {
            throw new IllegalArgumentException("Location history limit is outside hard bounds");
        }
        this.maximumEntriesPerPlayer = maximumEntriesPerPlayer;
    }

    @Override
    public String id() {
        return "sef:location_history";
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
        return path;
    }

    @Override
    public synchronized LoadResult load(Path managedRoot) {
        path = managedRoot.resolve("location-history.json").toAbsolutePath().normalize();
        records.clear();
        document = StorageService.read(path, DOMAIN, SCHEMA_VERSION).orElse(null);
        if (document == null) {
            state = stateFromStorageStatus(path);
            return new LoadResult(state, state == RepositoryState.MISSING ? "new repository" : "storage unavailable");
        }
        if (!document.data().isJsonObject()) {
            state = RepositoryState.RECOVERY;
            return new LoadResult(state, "location history data is not an object");
        }
        try {
            JsonObject players = document.data().getAsJsonObject().getAsJsonObject("players");
            if (players == null) {
                throw new IllegalStateException("Location history players collection is missing");
            }
            if (players.size() > MAXIMUM_STORED_PLAYERS) {
                state = RepositoryState.RECOVERY;
                return new LoadResult(state, "location history player limit exceeded");
            }
            for (Map.Entry<String, JsonElement> playerEntry : players.entrySet()) {
                UUID playerId = UUID.fromString(playerEntry.getKey());
                if (!playerEntry.getValue().isJsonArray()) {
                    throw new IllegalStateException("Location history player entry is not an array");
                }
                Deque<LocationRecord> history = new ArrayDeque<>();
                for (JsonElement element : playerEntry.getValue().getAsJsonArray()) {
                    if (!element.isJsonObject()) {
                        throw new IllegalStateException("Location history record is not an object");
                    }
                    LocationRecord record = decode(element.getAsJsonObject());
                    history.addLast(record);
                    while (history.size() > maximumEntriesPerPlayer) {
                        history.removeFirst();
                    }
                }
                if (!history.isEmpty()) {
                    records.put(playerId, history);
                }
            }
            state = RepositoryState.READY;
            if (document.migrated()) {
                revision++;
            }
            return new LoadResult(state, "loaded " + records.size() + " player histories");
        } catch (RuntimeException exception) {
            records.clear();
            state = RepositoryState.RECOVERY;
            return new LoadResult(state, exception.getClass().getSimpleName());
        }
    }

    public synchronized void record(UUID playerId, LocationRecord record) {
        if (state == RepositoryState.RECOVERY
                || state == RepositoryState.UNSUPPORTED
                || state == RepositoryState.ERROR) {
            throw new IllegalStateException("Location history repository is not writable in " + state + " state");
        }
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(record, "record");
        Deque<LocationRecord> history = records.computeIfAbsent(playerId, ignored -> new ArrayDeque<>());
        history.addLast(record);
        while (history.size() > maximumEntriesPerPlayer) {
            history.removeFirst();
        }
        revision++;
    }

    public synchronized List<LocationRecord> history(UUID playerId) {
        Deque<LocationRecord> history = records.get(playerId);
        return history == null ? List.of() : List.copyOf(history);
    }

    public synchronized Map<UUID, List<LocationRecord>> snapshot() {
        Map<UUID, List<LocationRecord>> snapshot = new LinkedHashMap<>();
        records.forEach((playerId, history) -> snapshot.put(playerId, List.copyOf(history)));
        return Map.copyOf(snapshot);
    }

    @Override
    public void flush() throws IOException {
        final Map<UUID, List<LocationRecord>> snapshot;
        final long snapshotRevision;
        final StorageService.Document previous;
        final Path destination;
        synchronized (this) {
            if (path == null || !dirty()) {
                return;
            }
            if (state == RepositoryState.RECOVERY
                    || state == RepositoryState.UNSUPPORTED
                    || state == RepositoryState.ERROR) {
                throw new IOException("Location history repository is not writable in " + state + " state");
            }
            snapshot = snapshot();
            snapshotRevision = revision;
            previous = document;
            destination = path;
        }

        JsonObject players = new JsonObject();
        snapshot.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(UUID::toString)))
                .forEach(entry -> {
                    JsonArray history = new JsonArray();
                    entry.getValue().forEach(record -> history.add(encode(record)));
                    players.add(entry.getKey().toString(), history);
                });
        JsonObject data = new JsonObject();
        data.add("players", players);
        StorageService.write(destination, DOMAIN, SCHEMA_VERSION, data, previous, Set.of("/players"));
        synchronized (this) {
            document = StorageService.read(destination, DOMAIN, SCHEMA_VERSION).orElse(previous);
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

    private static JsonObject encode(LocationRecord record) {
        JsonObject object = new JsonObject();
        object.addProperty("dimension", record.dimensionId());
        object.addProperty("x", record.x());
        object.addProperty("y", record.y());
        object.addProperty("z", record.z());
        object.addProperty("yaw", record.yaw());
        object.addProperty("pitch", record.pitch());
        object.addProperty("recordedAt", record.recordedAt().toString());
        object.addProperty("reason", record.reason());
        return object;
    }

    private static LocationRecord decode(JsonObject object) {
        return new LocationRecord(
                object.get("dimension").getAsString(),
                object.get("x").getAsDouble(),
                object.get("y").getAsDouble(),
                object.get("z").getAsDouble(),
                object.get("yaw").getAsFloat(),
                object.get("pitch").getAsFloat(),
                Instant.parse(object.get("recordedAt").getAsString()),
                object.get("reason").getAsString());
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
                .orElse(RepositoryState.MISSING);
    }

    public record LocationRecord(
            String dimensionId,
            double x,
            double y,
            double z,
            float yaw,
            float pitch,
            Instant recordedAt,
            String reason
    ) {
        public LocationRecord {
            dimensionId = Objects.requireNonNull(dimensionId, "dimensionId").trim().toLowerCase(Locale.ROOT);
            Objects.requireNonNull(recordedAt, "recordedAt");
            reason = Objects.requireNonNull(reason, "reason").trim();
            if (dimensionId.isBlank() || dimensionId.length() > 128
                    || !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                    || !Float.isFinite(yaw) || !Float.isFinite(pitch)
                    || reason.length() > 64) {
                throw new IllegalArgumentException("Location record is outside bounds");
            }
        }
    }
}
