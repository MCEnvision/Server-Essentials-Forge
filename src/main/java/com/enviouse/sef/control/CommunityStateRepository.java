package com.enviouse.sef.control;

import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.InstantJsonAdapter;
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public final class CommunityStateRepository implements StorageRepository {
    public static final int SCHEMA_VERSION = 1;
    public static final int MAXIMUM_ENTRIES = 1_000_000;
    private static final Pattern TYPE = Pattern.compile("[a-z][a-z0-9_]{0,63}");
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantJsonAdapter())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private final Map<Identity, Entry> entries = new LinkedHashMap<>();
    private Path path;
    private StorageService.Document document;
    private RepositoryState state = RepositoryState.NEW;
    private long revision;
    private long flushedRevision;

    @Override
    public String id() {
        return "sef:community_state";
    }

    @Override
    public String domain() {
        return "community_state";
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
                .resolve("community-state.json")
                .toAbsolutePath()
                .normalize();
        entries.clear();
        revision = 0L;
        flushedRevision = 0L;
        boolean existed = Files.exists(path, java.nio.file.LinkOption.NOFOLLOW_LINKS);
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(null);
        if (document == null) {
            state = existed ? RepositoryState.RECOVERY : RepositoryState.MISSING;
            return new LoadResult(state, existed ? "community state storage unavailable" : "new community state");
        }
        try {
            Snapshot snapshot = GSON.fromJson(document.data(), Snapshot.class);
            if (snapshot == null || snapshot.entries().size() > MAXIMUM_ENTRIES
                    || snapshot.revision() < 1L) {
                throw new IllegalStateException("community state snapshot is outside bounds");
            }
            Instant now = Instant.now();
            for (Entry entry : snapshot.entries()) {
                validate(entry);
                if (entry.expiresAt() != null && !entry.expiresAt().isAfter(now)) {
                    continue;
                }
                if (entries.putIfAbsent(entry.identity(), entry) != null) {
                    throw new IllegalStateException("duplicate community state entry");
                }
            }
            revision = snapshot.revision();
            flushedRevision = document.migrated() ? Math.max(0L, revision - 1L) : revision;
            state = RepositoryState.READY;
            return new LoadResult(state, "loaded " + entries.size() + " community state entries");
        } catch (RuntimeException exception) {
            entries.clear();
            state = RepositoryState.RECOVERY;
            return new LoadResult(state, exception.getClass().getSimpleName());
        }
    }

    public synchronized Entry put(
            String type,
            UUID ownerId,
            UUID subjectId,
            String key,
            String value,
            Instant expiresAt
    ) {
        return putAtomically(List.of(new Write(type, ownerId, subjectId, key, value, expiresAt))).getFirst();
    }

    public synchronized List<Entry> putAtomically(List<Write> writes) {
        List<Write> batch = List.copyOf(Objects.requireNonNull(writes, "writes"));
        MutationResult result = mutateAtomically(batch);
        if (!result.successful()) {
            throw new IllegalStateException("unconditional community state write was rejected");
        }
        return result.written();
    }

    public synchronized MutationResult mutateAtomically(List<? extends Mutation> mutations) {
        writable();
        List<? extends Mutation> batch =
                List.copyOf(Objects.requireNonNull(mutations, "mutations"));
        if (batch.isEmpty() || batch.size() > 1024) {
            throw new IllegalArgumentException("community state mutation batch is outside bounds");
        }
        Instant now = Instant.now();
        Map<Identity, Optional<Entry>> staged = new LinkedHashMap<>();
        List<Entry> results = new ArrayList<>(batch.size());
        for (Mutation mutation : batch) {
            Objects.requireNonNull(mutation, "mutation");
            Identity identity = mutation.identity();
            Entry current = staged.containsKey(identity)
                    ? staged.get(identity).orElse(null)
                    : entries.get(identity);
            if (mutation instanceof Write write) {
                Entry replacement = new Entry(
                        current == null ? UUID.randomUUID() : current.id(),
                        identity.type(),
                        identity.ownerId(),
                        write.subjectId(),
                        identity.key(),
                        bounded(write.value(), 4096, true),
                        current == null ? now : current.createdAt(),
                        now,
                        write.expiresAt(),
                        current == null ? 1L : Math.addExact(current.revision(), 1L));
                validate(replacement);
                staged.put(identity, Optional.of(replacement));
                results.add(replacement);
            } else if (mutation instanceof CompareAndRemove removal) {
                if (current == null || current.revision() != removal.expectedRevision()) {
                    return new MutationResult(false, List.of(), 0);
                }
                staged.put(identity, Optional.empty());
            } else if (mutation instanceof Remove) {
                staged.put(identity, Optional.empty());
            } else {
                throw new IllegalArgumentException("community state mutation is unavailable");
            }
        }
        long additions = staged.entrySet().stream()
                .filter(entry -> entry.getValue().isPresent() && !entries.containsKey(entry.getKey()))
                .count();
        long removals = staged.entrySet().stream()
                .filter(entry -> entry.getValue().isEmpty() && entries.containsKey(entry.getKey()))
                .count();
        if (entries.size() + additions - removals > MAXIMUM_ENTRIES) {
            throw new IllegalStateException("community state capacity is full");
        }
        int removed = 0;
        boolean changed = false;
        for (Map.Entry<Identity, Optional<Entry>> mutation : staged.entrySet()) {
            Entry replacement = mutation.getValue().orElse(null);
            Entry current = entries.get(mutation.getKey());
            if (replacement == null) {
                if (entries.remove(mutation.getKey()) != null) {
                    removed++;
                    changed = true;
                }
            } else if (!replacement.equals(current)) {
                entries.put(mutation.getKey(), replacement);
                changed = true;
            }
        }
        if (changed) {
            changed();
        }
        return new MutationResult(true, List.copyOf(results), removed);
    }

    public synchronized Optional<Entry> find(String type, UUID ownerId, String key) {
        prune();
        return Optional.ofNullable(entries.get(new Identity(
                normalizeType(type),
                Objects.requireNonNull(ownerId, "ownerId"),
                bounded(key, 128, false))));
    }

    public synchronized List<Entry> entries(String type, UUID ownerId) {
        prune();
        String normalized = normalizeType(type);
        return entries.values().stream()
                .filter(entry -> entry.type().equals(normalized))
                .filter(entry -> ownerId == null || entry.ownerId().equals(ownerId))
                .sorted(Comparator.comparing(Entry::updatedAt).reversed())
                .toList();
    }

    public synchronized List<Entry> entries(String type) {
        return entries(type, null);
    }

    public synchronized boolean remove(String type, UUID ownerId, String key) {
        writable();
        Entry removed = entries.remove(new Identity(
                normalizeType(type),
                Objects.requireNonNull(ownerId, "ownerId"),
                bounded(key, 128, false)));
        if (removed != null) {
            changed();
        }
        return removed != null;
    }

    public synchronized boolean compareAndRemove(
            String type,
            UUID ownerId,
            String key,
            long expectedRevision
    ) {
        writable();
        Identity identity = new Identity(
                normalizeType(type),
                Objects.requireNonNull(ownerId, "ownerId"),
                bounded(key, 128, false));
        Entry current = entries.get(identity);
        if (current == null || current.revision() != expectedRevision) {
            return false;
        }
        entries.remove(identity);
        changed();
        return true;
    }

    public synchronized long count(String type, String key) {
        prune();
        String normalizedType = normalizeType(type);
        String normalizedKey = bounded(key, 128, false);
        return entries.values().stream()
                .filter(entry -> entry.type().equals(normalizedType) && entry.key().equals(normalizedKey))
                .count();
    }

    public synchronized long revision() {
        prune();
        return revision;
    }

    @Override
    public void flush() throws IOException {
        final Snapshot snapshot;
        final Path destination;
        final StorageService.Document previous;
        final long snapshotRevision;
        synchronized (this) {
            prune();
            if (path == null || !dirty()) {
                return;
            }
            writable();
            snapshot = new Snapshot(revision, new ArrayList<>(entries.values()));
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
                Set.of("/entries"));
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

    private void prune() {
        Instant now = Instant.now();
        int before = entries.size();
        entries.values().removeIf(entry -> entry.expiresAt() != null && !entry.expiresAt().isAfter(now));
        if (entries.size() != before) {
            changed();
        }
    }

    private void changed() {
        revision = Math.addExact(revision, 1L);
    }

    private void writable() {
        if (state == RepositoryState.RECOVERY
                || state == RepositoryState.UNSUPPORTED
                || state == RepositoryState.ERROR
                || state == RepositoryState.CLOSED) {
            throw new IllegalStateException("community state storage is unavailable");
        }
    }

    private static void validate(Entry entry) {
        Objects.requireNonNull(entry, "entry");
        Objects.requireNonNull(entry.id(), "id");
        normalizeType(entry.type());
        Objects.requireNonNull(entry.ownerId(), "ownerId");
        bounded(entry.key(), 128, false);
        bounded(entry.value(), 4096, true);
        Objects.requireNonNull(entry.createdAt(), "createdAt");
        Objects.requireNonNull(entry.updatedAt(), "updatedAt");
        if (entry.updatedAt().isBefore(entry.createdAt()) || entry.revision() < 1L) {
            throw new IllegalArgumentException("community state entry is invalid");
        }
    }

    private static String normalizeType(String value) {
        String normalized = Objects.requireNonNull(value, "type").strip().toLowerCase(Locale.ROOT);
        if (!TYPE.matcher(normalized).matches()) {
            throw new IllegalArgumentException("community state type is invalid");
        }
        return normalized;
    }

    private static String bounded(String value, int maximum, boolean allowBlank) {
        String normalized = Objects.requireNonNullElse(value, "").strip();
        if ((!allowBlank && normalized.isBlank())
                || normalized.length() > maximum
                || normalized.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("community state text is outside bounds");
        }
        return normalized;
    }

    private record Identity(String type, UUID ownerId, String key) {
        private Identity {
            type = normalizeType(type);
            Objects.requireNonNull(ownerId, "ownerId");
            key = bounded(key, 128, false);
        }
    }

    public record Entry(
            UUID id,
            String type,
            UUID ownerId,
            UUID subjectId,
            String key,
            String value,
            Instant createdAt,
            Instant updatedAt,
            Instant expiresAt,
            long revision
    ) {
        private Identity identity() {
            return new Identity(type, ownerId, key);
        }
    }

    public sealed interface Mutation permits Write, Remove, CompareAndRemove {
        String type();

        UUID ownerId();

        String key();

        private Identity identity() {
            return new Identity(normalizeType(type()), ownerId(), bounded(key(), 128, false));
        }
    }

    public record Write(
            String type,
            UUID ownerId,
            UUID subjectId,
            String key,
            String value,
            Instant expiresAt
    ) implements Mutation {
    }

    public record Remove(String type, UUID ownerId, String key) implements Mutation {
    }

    public record CompareAndRemove(
            String type,
            UUID ownerId,
            String key,
            long expectedRevision
    ) implements Mutation {
        public CompareAndRemove {
            if (expectedRevision < 1L) {
                throw new IllegalArgumentException("community state revision is invalid");
            }
        }
    }

    public record MutationResult(boolean successful, List<Entry> written, int removed) {
        public MutationResult {
            written = List.copyOf(written);
            if (removed < 0) {
                throw new IllegalArgumentException("community state removal count is invalid");
            }
        }
    }

    private record Snapshot(long revision, List<Entry> entries) {
        private Snapshot {
            entries = List.copyOf(entries == null ? List.of() : entries);
        }
    }
}
