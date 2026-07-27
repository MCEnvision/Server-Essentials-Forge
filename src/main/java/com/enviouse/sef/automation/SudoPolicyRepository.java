package com.enviouse.sef.automation;

import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.storage.InstantJsonAdapter;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class SudoPolicyRepository implements StorageRepository {
    public static final int SCHEMA_VERSION = 1;
    private static final int MAXIMUM_RECORDS = 10_000;
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantJsonAdapter())
            .create();

    private final Map<UUID, Policy> policies = new LinkedHashMap<>();
    private StorageService.Document document;
    private RepositoryState state = RepositoryState.NEW;
    private Path path;
    private long revision;
    private long flushedRevision;

    @Override
    public String id() {
        return "sef:sudo_policy";
    }

    @Override
    public String domain() {
        return "sudo policy";
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
                .resolve("sudo-policy.json")
                .toAbsolutePath()
                .normalize();
        policies.clear();
        revision = 0L;
        flushedRevision = 0L;
        boolean existed = Files.exists(path);
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(null);
        if (document == null) {
            state = existed ? RepositoryState.RECOVERY : RepositoryState.MISSING;
            return new LoadResult(state, existed ? "storage unavailable" : "new repository");
        }
        try {
            Snapshot snapshot = GSON.fromJson(document.data(), Snapshot.class);
            if (snapshot == null || snapshot.policies().size() > MAXIMUM_RECORDS) {
                throw new IllegalStateException("Sudo policy collection is outside bounds");
            }
            for (Policy policy : snapshot.policies()) {
                if (policies.putIfAbsent(policy.playerId(), policy) != null) {
                    throw new IllegalStateException("Duplicate sudo policy identity");
                }
            }
            state = RepositoryState.READY;
            if (document.migrated()) {
                revision++;
            }
            return new LoadResult(state, "loaded " + policies.size() + " sudo policies");
        } catch (RuntimeException exception) {
            policies.clear();
            state = RepositoryState.RECOVERY;
            return new LoadResult(state, exception.getClass().getSimpleName());
        }
    }

    public synchronized Policy policy(UUID playerId) {
        return policies.getOrDefault(
                Objects.requireNonNull(playerId, "playerId"),
                Policy.defaults(playerId));
    }

    public synchronized ActionResult<Policy> setConsent(
            UUID playerId,
            boolean consent,
            long expectedRevision
    ) {
        writable();
        Policy current = policy(playerId);
        if (expectedRevision > 0L && current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "sudo consent revision changed");
        }
        Policy replacement = new Policy(
                current.playerId(),
                consent,
                current.locked(),
                current.lockReason(),
                current.lockedBy(),
                Instant.now(),
                Math.addExact(current.revision(), 1L));
        put(replacement);
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<Policy> setLock(
            UUID playerId,
            boolean locked,
            String reason,
            UUID actorId,
            long expectedRevision
    ) {
        writable();
        Policy current = policy(playerId);
        if (expectedRevision > 0L && current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "sudo lock revision changed");
        }
        String boundedReason = Objects.requireNonNullElse(reason, "").trim();
        if (boundedReason.length() > 256
                || boundedReason.codePoints().anyMatch(Character::isISOControl)) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "sudo lock reason is outside bounds");
        }
        Policy replacement = new Policy(
                current.playerId(),
                current.consent(),
                locked,
                locked ? boundedReason : "",
                locked ? Objects.requireNonNull(actorId, "actorId") : null,
                Instant.now(),
                Math.addExact(current.revision(), 1L));
        put(replacement);
        return ActionResult.success(replacement);
    }

    public synchronized Decision decide(
            UUID playerId,
            boolean bypassConsent,
            boolean bypassLock
    ) {
        Policy policy = policy(playerId);
        if (policy.locked() && !bypassLock) {
            return new Decision(false, "targeted actor execution is locked", policy);
        }
        if (!policy.consent() && !bypassConsent) {
            return new Decision(false, "target has not granted targeted actor consent", policy);
        }
        return new Decision(true, "targeted actor policy accepted", policy);
    }

    public synchronized List<Policy> entries() {
        return policies.values().stream()
                .sorted(Comparator.comparing(policy -> policy.playerId().toString()))
                .toList();
    }

    @Override
    public void flush() throws IOException {
        Snapshot snapshot;
        Path destination;
        StorageService.Document previous;
        long snapshotRevision;
        synchronized (this) {
            if (path == null || !dirty()) {
                return;
            }
            writable();
            snapshot = new Snapshot(new ArrayList<>(policies.values()));
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
                Set.of("/policies"));
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

    private void put(Policy policy) {
        if (!policies.containsKey(policy.playerId()) && policies.size() >= MAXIMUM_RECORDS) {
            throw new IllegalStateException("Sudo policy capacity is full");
        }
        policies.put(policy.playerId(), policy);
        revision++;
    }

    private void writable() {
        if (state == RepositoryState.RECOVERY
                || state == RepositoryState.UNSUPPORTED
                || state == RepositoryState.ERROR
                || state == RepositoryState.CLOSED) {
            throw new IllegalStateException("Sudo policy storage is unavailable");
        }
    }

    public record Policy(
            UUID playerId,
            boolean consent,
            boolean locked,
            String lockReason,
            UUID lockedBy,
            Instant changedAt,
            long revision
    ) {
        public Policy {
            Objects.requireNonNull(playerId, "playerId");
            lockReason = Objects.requireNonNullElse(lockReason, "").trim();
            Objects.requireNonNull(changedAt, "changedAt");
            if (revision < 1L
                    || lockReason.length() > 256
                    || lockReason.codePoints().anyMatch(Character::isISOControl)
                    || (locked && lockedBy == null)
                    || (!locked && (!lockReason.isEmpty() || lockedBy != null))) {
                throw new IllegalArgumentException("Sudo policy is invalid");
            }
        }

        public static Policy defaults(UUID playerId) {
            return new Policy(playerId, false, false, "", null, Instant.EPOCH, 1L);
        }
    }

    public record Decision(boolean allowed, String detail, Policy policy) {
        public Decision {
            detail = Objects.requireNonNull(detail, "detail");
            Objects.requireNonNull(policy, "policy");
        }
    }

    private record Snapshot(List<Policy> policies) {
        private Snapshot {
            policies = List.copyOf(policies == null ? List.of() : policies);
        }
    }
}
