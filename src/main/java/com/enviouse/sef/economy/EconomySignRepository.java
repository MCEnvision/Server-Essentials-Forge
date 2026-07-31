package com.enviouse.sef.economy;

import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class EconomySignRepository implements StorageRepository {
    private static final int SCHEMA_VERSION = 1;
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private final int maximumSigns;
    private final Map<SignKey, SignRecord> signs = new LinkedHashMap<>();
    private Path path;
    private StorageService.Document document;
    private RepositoryState state = RepositoryState.MISSING;
    private long revision;
    private long flushedRevision;

    public EconomySignRepository(int maximumSigns) {
        if (maximumSigns < 1 || maximumSigns > 1_000_000) {
            throw new IllegalArgumentException("Maximum economy signs is outside hard bounds");
        }
        this.maximumSigns = maximumSigns;
    }

    @Override
    public String id() {
        return "sef:economy_signs";
    }

    @Override
    public String domain() {
        return "economy_signs";
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
                .resolve("economy-signs.json")
                .toAbsolutePath()
                .normalize();
        signs.clear();
        revision = 0L;
        flushedRevision = 0L;
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(null);
        if (document == null) {
            state = Files.exists(path, java.nio.file.LinkOption.NOFOLLOW_LINKS)
                    ? RepositoryState.RECOVERY
                    : RepositoryState.MISSING;
            return new LoadResult(state, state == RepositoryState.MISSING
                    ? "new repository"
                    : "storage unavailable");
        }
        try {
            Snapshot snapshot = GSON.fromJson(document.data(), Snapshot.class);
            if (snapshot == null || snapshot.signs() == null) {
                throw new IllegalStateException("Economy sign snapshot is missing");
            }
            if (snapshot.signs().size() > maximumSigns) {
                throw new IllegalStateException("Economy sign snapshot exceeds configured bounds");
            }
            for (SignRecord record : snapshot.signs()) {
                validate(record);
                if (signs.putIfAbsent(record.key(), record) != null) {
                    throw new IllegalStateException("Duplicate economy sign key");
                }
            }
            state = RepositoryState.READY;
            if (document.migrated()) {
                revision++;
            }
            return new LoadResult(state, "loaded " + signs.size() + " economy signs");
        } catch (RuntimeException exception) {
            signs.clear();
            state = RepositoryState.RECOVERY;
            return new LoadResult(state, exception.getClass().getSimpleName());
        }
    }

    public synchronized Optional<SignRecord> find(SignKey key) {
        return Optional.ofNullable(signs.get(Objects.requireNonNull(key, "key")));
    }

    public synchronized Optional<SignRecord> find(String stableId) {
        String normalized = Objects.requireNonNullElse(stableId, "").strip();
        return signs.values().stream()
                .filter(record -> record.key().stableId().equals(normalized))
                .findFirst();
    }

    public synchronized SignRecord put(
            SignKey key,
            UUID creatorId,
            EconomySignParser.Definition definition
    ) {
        writable();
        validate(key);
        Objects.requireNonNull(creatorId, "creatorId");
        Objects.requireNonNull(definition, "definition");
        SignRecord current = signs.get(key);
        if (current == null && signs.size() >= maximumSigns) {
            throw new IllegalStateException("Economy sign capacity is full");
        }
        long nextRevision = current == null ? 1L : Math.addExact(current.revision(), 1L);
        SignRecord replacement = new SignRecord(
                key,
                creatorId,
                definition.type(),
                definition.arguments(),
                definition.fingerprint(),
                nextRevision,
                System.currentTimeMillis());
        validate(replacement);
        signs.put(key, replacement);
        changed();
        return replacement;
    }

    public synchronized boolean remove(SignKey key) {
        writable();
        if (signs.remove(Objects.requireNonNull(key, "key")) == null) {
            return false;
        }
        changed();
        return true;
    }

    public synchronized int removeAt(String dimensionId, int x, int y, int z) {
        writable();
        int before = signs.size();
        signs.keySet().removeIf(key -> key.dimensionId().equals(dimensionId)
                && key.x() == x
                && key.y() == y
                && key.z() == z);
        int removed = before - signs.size();
        if (removed > 0) {
            changed();
        }
        return removed;
    }

    public synchronized List<SignRecord> entries() {
        return signs.values().stream()
                .sorted(Comparator.comparing(record -> record.key().stableId()))
                .toList();
    }

    public synchronized int size() {
        return signs.size();
    }

    public synchronized SignRecord adopt(String stableId, UUID creatorId) {
        writable();
        SignRecord current = find(stableId)
                .orElseThrow(() -> new IllegalArgumentException("Economy sign was not found"));
        SignRecord replacement = new SignRecord(
                current.key(),
                Objects.requireNonNull(creatorId, "creatorId"),
                current.type(),
                current.arguments(),
                current.fingerprint(),
                Math.addExact(current.revision(), 1L),
                System.currentTimeMillis());
        validate(replacement);
        signs.put(current.key(), replacement);
        changed();
        return replacement;
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
            snapshot = new Snapshot(new ArrayList<>(signs.values()));
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
                Set.of("/signs"));
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

    private void writable() {
        if (state == RepositoryState.RECOVERY
                || state == RepositoryState.ERROR
                || state == RepositoryState.UNSUPPORTED) {
            throw new IllegalStateException("Economy sign storage is unavailable");
        }
    }

    private void changed() {
        revision = Math.addExact(revision, 1L);
    }

    private static void validate(SignRecord record) {
        Objects.requireNonNull(record, "record");
        validate(record.key());
        Objects.requireNonNull(record.creatorId(), "creatorId");
        Objects.requireNonNull(record.type(), "type");
        if (record.arguments() == null
                || record.arguments().size() > 4
                || record.arguments().stream().anyMatch(value -> invalidText(value, 128))
                || invalidFingerprint(record.fingerprint())
                || record.revision() < 1L
                || record.updatedAtEpochMillis() < 0L) {
            throw new IllegalStateException("Economy sign record is invalid");
        }
    }

    private static void validate(SignKey key) {
        Objects.requireNonNull(key, "key");
        if (invalidText(key.dimensionId(), 128)
                || !key.dimensionId().matches("[a-z0-9_.-]+:[a-z0-9_./-]+")
                || Math.abs((long) key.x()) > 30_000_000L
                || key.y() < -4096
                || key.y() > 4096
                || Math.abs((long) key.z()) > 30_000_000L) {
            throw new IllegalArgumentException("Economy sign key is invalid");
        }
    }

    private static boolean invalidText(String value, int maximumLength) {
        return value == null
                || value.isBlank()
                || value.length() > maximumLength
                || value.codePoints().anyMatch(Character::isISOControl);
    }

    private static boolean invalidFingerprint(String value) {
        return value == null || !value.matches("[0-9a-f]{64}");
    }

    private record Snapshot(List<SignRecord> signs) {
    }

    public record SignKey(String dimensionId, int x, int y, int z, boolean front) {
        public SignKey {
            Objects.requireNonNull(dimensionId, "dimensionId");
        }

        public String stableId() {
            return dimensionId + ":" + x + ":" + y + ":" + z + ":" + (front ? "front" : "back");
        }
    }

    public record SignRecord(
            SignKey key,
            UUID creatorId,
            EconomySignParser.SignType type,
            List<String> arguments,
            String fingerprint,
            long revision,
            long updatedAtEpochMillis
    ) {
        public SignRecord {
            arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments"));
        }
    }
}
