package com.enviouse.sef.filter;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class FilterDataStore implements StorageRepository {
    public static final int SCHEMA_VERSION = 1;
    private static final int MAXIMUM_FILTERS = 10_000;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<Map<String, FilterRecord>>(){}.getType();

    public record FilterRecord(String wordToFilter, String replacement, boolean caseSensitive) {}

    private final Map<String, FilterRecord> filters = new LinkedHashMap<>();
    private Path filePath;
    private Path loadedPath;
    private StorageService.Document document;
    private RepositoryState state = RepositoryState.NEW;
    private long revision;
    private long flushedRevision;

    @Override
    public String id() {
        return "sef:filters";
    }

    @Override
    public String domain() {
        return "filters";
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public synchronized Path path() {
        return filePath;
    }

    public synchronized void setPath(Path path) {
        filePath = Objects.requireNonNull(path, "path").toAbsolutePath().normalize();
    }

    public synchronized Map<String, FilterRecord> getFilters() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(filters));
    }

    public synchronized void put(String id, FilterRecord record) {
        writable();
        String normalized = validateId(id);
        validate(record);
        FilterRecord previous = filters.put(normalized, record);
        revision = Math.addExact(revision, 1L);
        try {
            save();
        } catch (RuntimeException exception) {
            if (previous == null) {
                filters.remove(normalized);
            } else {
                filters.put(normalized, previous);
            }
            revision = Math.addExact(revision, 1L);
            throw exception;
        }
    }

    public synchronized boolean remove(String id) {
        writable();
        String normalized = validateId(id);
        FilterRecord removed = filters.remove(normalized);
        if (removed == null) {
            return false;
        }
        revision = Math.addExact(revision, 1L);
        try {
            save();
        } catch (RuntimeException exception) {
            filters.put(normalized, removed);
            revision = Math.addExact(revision, 1L);
            throw exception;
        }
        return true;
    }

    public synchronized LoadResult load() {
        if (filePath == null) {
            state = RepositoryState.ERROR;
            return new LoadResult(state, "filter path is unavailable");
        }
        Path destination = filePath;
        Path previousPath = loadedPath;
        RepositoryState previousState = state;
        boolean existed = Files.exists(destination, java.nio.file.LinkOption.NOFOLLOW_LINKS);
        StorageService.Document candidate =
                StorageService.read(destination, domain(), SCHEMA_VERSION).orElse(null);
        if (candidate == null) {
            RepositoryState loadedState = stateFromStorageStatus(destination);
            if (loadedState == RepositoryState.MISSING) {
                if (destination.equals(previousPath)
                        && (previousState == RepositoryState.READY || previousState == RepositoryState.MISSING)) {
                    state = RepositoryState.RECOVERY;
                    return new LoadResult(state, "filter storage disappeared after initialization");
                }
                filters.clear();
                document = null;
                revision = 0L;
                flushedRevision = 0L;
                loadedPath = destination;
                state = RepositoryState.MISSING;
                return new LoadResult(state, "new filter repository");
            }
            if (!destination.equals(previousPath)) {
                filters.clear();
                document = null;
                revision = 0L;
                flushedRevision = 0L;
            }
            state = loadedState;
            return new LoadResult(
                    state,
                    existed ? "filter storage is unavailable" : "new filter repository");
        }
        try {
            Map<String, FilterRecord> loaded = GSON.fromJson(candidate.data(), MAP_TYPE);
            Map<String, FilterRecord> validated = new LinkedHashMap<>();
            if (loaded == null || loaded.size() > MAXIMUM_FILTERS) {
                throw new IllegalArgumentException("Filter collection is outside bounds");
            }
            loaded.forEach((id, record) -> {
                String normalized = validateId(id);
                validate(record);
                if (validated.putIfAbsent(normalized, record) != null) {
                    throw new IllegalArgumentException("Duplicate filter id");
                }
            });
            filters.clear();
            filters.putAll(validated);
            loadedPath = destination;
            document = candidate;
            state = RepositoryState.READY;
            revision = Math.addExact(revision, 1L);
            flushedRevision = candidate.migrated() ? revision - 1L : revision;
            if (dirty()) {
                flush();
            }
            ServerEssentialsForge.LOGGER.info("[SEF] Loaded {} word filter(s)", filters.size());
            return new LoadResult(state, "loaded filters");
        } catch (IOException | RuntimeException exception) {
            state = RepositoryState.RECOVERY;
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to load filters", exception);
            return new LoadResult(state, exception.getClass().getSimpleName());
        }
    }

    @Override
    public synchronized LoadResult load(Path managedRoot) {
        setPath(Objects.requireNonNull(managedRoot, "managedRoot").resolve("filters.json"));
        return load();
    }

    public synchronized void save() {
        writable();
        try {
            flush();
        } catch (IOException exception) {
            state = RepositoryState.ERROR;
            throw new IllegalStateException("Filter storage could not be saved", exception);
        }
    }

    @Override
    public synchronized void flush() throws IOException {
        writable();
        if (filePath == null || !dirty()) {
            return;
        }
        StorageService.write(
                filePath,
                domain(),
                SCHEMA_VERSION,
                GSON.toJsonTree(filters),
                document,
                Set.of(""));
        document = StorageService.read(filePath, domain(), SCHEMA_VERSION).orElse(document);
        flushedRevision = revision;
        state = RepositoryState.READY;
    }

    @Override
    public synchronized boolean dirty() {
        return revision > flushedRevision;
    }

    @Override
    public synchronized RepositoryState state() {
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
                .orElse(RepositoryState.ERROR);
    }

    private void writable() {
        if (filePath == null
                || state == RepositoryState.RECOVERY
                || state == RepositoryState.UNSUPPORTED
                || state == RepositoryState.ERROR
                || state == RepositoryState.CLOSED) {
            throw new IllegalStateException("Filter storage is unavailable");
        }
    }

    private static String validateId(String id) {
        String normalized = Objects.requireNonNull(id, "filter id").strip().toLowerCase(java.util.Locale.ROOT);
        if (!normalized.matches("[a-z0-9][a-z0-9_.-]{0,63}")) {
            throw new IllegalArgumentException("Filter id is invalid");
        }
        return normalized;
    }

    private static void validate(FilterRecord record) {
        Objects.requireNonNull(record, "filter record");
        validateText(record.wordToFilter(), 256, false);
        validateText(record.replacement(), 1024, true);
    }

    private static void validateText(String value, int maximum, boolean blank) {
        String safe = Objects.requireNonNull(value, "filter text");
        if ((!blank && safe.isBlank())
                || safe.length() > maximum
                || safe.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Filter text is outside bounds");
        }
    }
}
