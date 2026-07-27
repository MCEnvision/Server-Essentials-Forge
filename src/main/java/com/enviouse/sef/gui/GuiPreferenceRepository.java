package com.enviouse.sef.gui;

import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class GuiPreferenceRepository implements StorageRepository {
    private static final int SCHEMA_VERSION = 1;
    private static final int MAXIMUM_PROFILES = 1_000_000;
    private static final Gson GSON = new GsonBuilder().create();

    private final Map<UUID, Preference> preferences = new LinkedHashMap<>();
    private Path path;
    private StorageService.Document document;
    private RepositoryState state = RepositoryState.MISSING;
    private long revision;
    private long flushedRevision;

    @Override
    public String id() {
        return "sef:gui_preferences";
    }

    @Override
    public String domain() {
        return "gui_preferences";
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
                .resolve("gui-preferences.json")
                .toAbsolutePath()
                .normalize();
        preferences.clear();
        revision = 0L;
        flushedRevision = 0L;
        boolean existedBeforeRead = Files.exists(path);
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(null);
        if (document == null) {
            state = existedBeforeRead ? RepositoryState.RECOVERY : RepositoryState.MISSING;
            return new LoadResult(state, state == RepositoryState.MISSING
                    ? "new repository"
                    : "storage unavailable");
        }
        try {
            Snapshot snapshot = GSON.fromJson(document.data(), Snapshot.class);
            if (snapshot == null || snapshot.preferences() == null
                    || snapshot.preferences().size() > MAXIMUM_PROFILES) {
                throw new IllegalStateException("GUI preference collection is invalid");
            }
            for (Preference preference : snapshot.preferences()) {
                validate(preference);
                if (preferences.putIfAbsent(preference.playerId(), preference) != null) {
                    throw new IllegalStateException("Duplicate GUI preference player");
                }
            }
            state = RepositoryState.READY;
            if (document.migrated()) {
                revision++;
            }
            return new LoadResult(state, "loaded " + preferences.size() + " GUI preferences");
        } catch (RuntimeException exception) {
            preferences.clear();
            state = RepositoryState.RECOVERY;
            return new LoadResult(state, exception.getClass().getSimpleName());
        }
    }

    public synchronized Preference preference(UUID playerId) {
        return preferences.getOrDefault(
                Objects.requireNonNull(playerId, "playerId"),
                Preference.defaults(playerId));
    }

    public synchronized Preference recordReminder(UUID playerId, int reminderRevision, Instant now) {
        writable();
        Preference current = preference(playerId);
        Preference replacement = new Preference(
                playerId,
                Math.max(current.lastReminderRevision(), reminderRevision),
                current.dismissedReminderRevision(),
                Objects.requireNonNull(now, "now").toEpochMilli(),
                current.revision() + 1L);
        put(replacement);
        return replacement;
    }

    public synchronized Preference dismissReminder(UUID playerId, int reminderRevision) {
        writable();
        Preference current = preference(playerId);
        Preference replacement = new Preference(
                playerId,
                Math.max(current.lastReminderRevision(), reminderRevision),
                Math.max(current.dismissedReminderRevision(), reminderRevision),
                current.lastReminderAtEpochMillis(),
                current.revision() + 1L);
        put(replacement);
        return replacement;
    }

    public synchronized List<Preference> entries() {
        return preferences.values().stream()
                .sorted(Comparator.comparing(preference -> preference.playerId().toString()))
                .toList();
    }

    @Override
    public void flush() throws IOException {
        final Snapshot snapshot;
        final Path destination;
        final StorageService.Document previous;
        final long snapshotRevision;
        synchronized (this) {
            if (path == null || !dirty()) {
                return;
            }
            writable();
            snapshot = new Snapshot(new ArrayList<>(preferences.values()));
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
                Set.of("/preferences"));
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

    private void put(Preference preference) {
        validate(preference);
        if (!preferences.containsKey(preference.playerId()) && preferences.size() >= MAXIMUM_PROFILES) {
            throw new IllegalStateException("GUI preference capacity is full");
        }
        preferences.put(preference.playerId(), preference);
        revision = Math.addExact(revision, 1L);
    }

    private void writable() {
        if (state == RepositoryState.RECOVERY
                || state == RepositoryState.UNSUPPORTED
                || state == RepositoryState.ERROR
                || state == RepositoryState.CLOSED) {
            throw new IllegalStateException("GUI preference storage is unavailable");
        }
    }

    private static void validate(Preference preference) {
        Objects.requireNonNull(preference, "preference");
        Objects.requireNonNull(preference.playerId(), "playerId");
        if (preference.lastReminderRevision() < 0
                || preference.dismissedReminderRevision() < 0
                || preference.lastReminderAtEpochMillis() < 0L
                || preference.revision() < 1L) {
            throw new IllegalArgumentException("GUI preference is invalid");
        }
    }

    private record Snapshot(List<Preference> preferences) {
        private Snapshot {
            preferences = List.copyOf(preferences == null ? List.of() : preferences);
        }
    }

    public record Preference(
            UUID playerId,
            int lastReminderRevision,
            int dismissedReminderRevision,
            long lastReminderAtEpochMillis,
            long revision
    ) {
        public static Preference defaults(UUID playerId) {
            return new Preference(Objects.requireNonNull(playerId, "playerId"), 0, 0, 0L, 1L);
        }
    }
}
