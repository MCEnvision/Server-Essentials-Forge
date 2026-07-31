package com.enviouse.sef.storage.repository;

import com.enviouse.sef.kernel.policy.CooldownService;
import com.enviouse.sef.storage.StorageService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public final class CooldownRepository implements StorageRepository {
    private static final int SCHEMA_VERSION = 1;
    private static final String DOMAIN = "command cooldowns";
    private static final int MAXIMUM_STORED_ENTRIES = 100_000;

    private final CooldownService cooldowns;
    private final Duration minimumPersistentRemaining;
    private Path path;
    private StorageService.Document document;
    private RepositoryState state = RepositoryState.NEW;
    private final AtomicLong changeRevision = new AtomicLong();
    private volatile long flushedRevision;

    public CooldownRepository(CooldownService cooldowns, Duration minimumPersistentRemaining) {
        this.cooldowns = Objects.requireNonNull(cooldowns, "cooldowns");
        this.minimumPersistentRemaining = Objects.requireNonNull(minimumPersistentRemaining, "minimumPersistentRemaining");
        if (minimumPersistentRemaining.isNegative() || minimumPersistentRemaining.compareTo(Duration.ofDays(365)) > 0) {
            throw new IllegalArgumentException("Persistent cooldown threshold is outside bounds");
        }
        cooldowns.onChange(changeRevision::incrementAndGet);
    }

    @Override
    public String id() {
        return "sef:cooldowns";
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
    public Path path() {
        return path;
    }

    @Override
    public synchronized LoadResult load(Path managedRoot) {
        path = managedRoot.resolve("cooldowns.json").toAbsolutePath().normalize();
        document = StorageService.read(path, DOMAIN, SCHEMA_VERSION).orElse(null);
        if (document == null) {
            state = stateFromStorageStatus(path);
            flushedRevision = changeRevision.get();
            return new LoadResult(state, state == RepositoryState.MISSING ? "new repository" : "storage unavailable");
        }
        if (!document.data().isJsonObject()) {
            state = RepositoryState.RECOVERY;
            return new LoadResult(state, "cooldown data is not an object");
        }
        try {
            JsonArray entries = document.data().getAsJsonObject().getAsJsonArray("entries");
            List<CooldownService.Entry> restored = new ArrayList<>();
            if (entries == null) {
                throw new IllegalStateException("Cooldown entries collection is missing");
            }
            if (entries.size() > MAXIMUM_STORED_ENTRIES) {
                state = RepositoryState.RECOVERY;
                return new LoadResult(state, "cooldown entry limit exceeded");
            }
            for (JsonElement element : entries) {
                if (!element.isJsonObject()) {
                    throw new IllegalStateException("Cooldown entry is not an object");
                }
                JsonObject object = element.getAsJsonObject();
                restored.add(new CooldownService.Entry(
                        UUID.fromString(object.get("playerId").getAsString()),
                        object.get("actionId").getAsString(),
                        object.get("expiryEpochMillis").getAsLong()));
            }
            cooldowns.restore(restored);
            state = RepositoryState.READY;
            long loadedRevision = changeRevision.get();
            flushedRevision = document.migrated() ? loadedRevision - 1L : loadedRevision;
            return new LoadResult(state, "loaded " + restored.size() + " cooldowns");
        } catch (RuntimeException exception) {
            state = RepositoryState.RECOVERY;
            flushedRevision = changeRevision.get();
            return new LoadResult(state, exception.getClass().getSimpleName());
        }
    }

    @Override
    public synchronized void flush() throws IOException {
        if (path == null || !dirty()) {
            return;
        }
        if (state == RepositoryState.RECOVERY
                || state == RepositoryState.UNSUPPORTED
                || state == RepositoryState.ERROR) {
            throw new IOException("Cooldown repository is not writable in " + state + " state");
        }
        long snapshotRevision = changeRevision.get();
        List<CooldownService.Entry> snapshot = cooldowns.snapshotPersistent(minimumPersistentRemaining);
        JsonArray entries = new JsonArray();
        for (CooldownService.Entry entry : snapshot) {
            JsonObject object = new JsonObject();
            object.addProperty("playerId", entry.playerId().toString());
            object.addProperty("actionId", entry.actionId());
            object.addProperty("expiryEpochMillis", entry.expiryEpochMillis());
            entries.add(object);
        }
        JsonObject data = new JsonObject();
        data.add("entries", entries);
        StorageService.write(path, DOMAIN, SCHEMA_VERSION, data, document, Set.of());
        document = StorageService.read(path, DOMAIN, SCHEMA_VERSION).orElse(document);
        flushedRevision = Math.max(flushedRevision, snapshotRevision);
        state = RepositoryState.READY;
    }

    @Override
    public boolean dirty() {
        return changeRevision.get() > flushedRevision;
    }

    @Override
    public RepositoryState state() {
        return state;
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
}
