package com.enviouse.sef.gui.protocol;

import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class OfflineActionRepository implements StorageRepository {
    public static final int SCHEMA_VERSION = 2;
    private static final int MAXIMUM_RECORDS = 10_000;
    private static final int MAXIMUM_PENDING_PER_TARGET = 128;
    private static final int MAXIMUM_FIELDS = 24;
    private static final Gson GSON = new GsonBuilder().create();

    private final Map<UUID, QueuedAction> actions = new LinkedHashMap<>();
    private Path path;
    private StorageService.Document document;
    private RepositoryState state = RepositoryState.MISSING;
    private long revision;
    private long flushedRevision;

    @Override
    public String id() {
        return "sef:offline_actions";
    }

    @Override
    public String domain() {
        return "offline_actions";
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
                .resolve("offline-actions.json")
                .toAbsolutePath()
                .normalize();
        actions.clear();
        revision = 0L;
        flushedRevision = 0L;
        boolean existed = Files.exists(path, java.nio.file.LinkOption.NOFOLLOW_LINKS);
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(null);
        if (document == null) {
            state = existed ? RepositoryState.RECOVERY : RepositoryState.MISSING;
            return new LoadResult(
                    state,
                    state == RepositoryState.MISSING
                            ? "new repository"
                            : "storage unavailable");
        }
        try {
            Snapshot snapshot = GSON.fromJson(document.data(), Snapshot.class);
            if (snapshot == null
                    || snapshot.actions() == null
                    || snapshot.actions().size() > MAXIMUM_RECORDS) {
                throw new IllegalStateException("Offline action collection is invalid");
            }
            boolean reconciled = false;
            Instant loadedAt = Instant.now();
            for (QueuedAction stored : snapshot.actions()) {
                QueuedAction action = normalizeLoaded(stored, loadedAt);
                reconciled |= action != stored;
                validate(action);
                if (actions.putIfAbsent(action.id(), action) != null) {
                    throw new IllegalStateException("Duplicate offline action");
                }
            }
            state = RepositoryState.READY;
            if (document.migrated() || reconciled) {
                revision++;
            }
            return new LoadResult(state, "loaded " + actions.size() + " offline actions");
        } catch (RuntimeException exception) {
            actions.clear();
            state = RepositoryState.RECOVERY;
            return new LoadResult(state, exception.getClass().getSimpleName());
        }
    }

    public synchronized QueuedAction enqueue(
            UUID actorId,
            UUID targetId,
            String actionId,
            String variantId,
            String targetFieldId,
            Map<String, String> values,
            Instant now,
            Duration lifetime
    ) {
        writable();
        Objects.requireNonNull(now, "now");
        Objects.requireNonNull(lifetime, "lifetime");
        prune(now);
        long pending = actions.values().stream()
                .filter(action -> action.state() == ActionState.PENDING)
                .filter(action -> action.targetId().equals(targetId))
                .count();
        if (pending >= MAXIMUM_PENDING_PER_TARGET) {
            throw new IllegalStateException("That player already has too many queued actions");
        }
        if (actions.size() >= MAXIMUM_RECORDS) {
            removeOldestCompleted();
        }
        if (actions.size() >= MAXIMUM_RECORDS) {
            throw new IllegalStateException("The offline action queue is full");
        }
        UUID operationId = UUID.randomUUID();
        QueuedAction action = new QueuedAction(
                operationId,
                "sef:offline:" + operationId,
                Objects.requireNonNull(actorId, "actorId"),
                Objects.requireNonNull(targetId, "targetId"),
                actionId,
                variantId,
                targetFieldId,
                Map.copyOf(values),
                now.toEpochMilli(),
                now.plus(lifetime).toEpochMilli(),
                0L,
                0L,
                0L,
                0L,
                ActionState.PENDING,
                "",
                1L);
        validate(action);
        actions.put(action.id(), action);
        changed();
        return action;
    }

    public synchronized List<QueuedAction> pendingReady(Instant now) {
        prune(Objects.requireNonNull(now, "now"));
        return actions.values().stream()
                .filter(action -> action.state() == ActionState.PENDING)
                .sorted(Comparator
                        .comparingLong(QueuedAction::createdAtEpochMillis)
                        .thenComparing(QueuedAction::id))
                .toList();
    }

    public synchronized List<QueuedAction> pendingReady(
            Instant now,
            Set<UUID> onlineTargets,
            int limit
    ) {
        prune(Objects.requireNonNull(now, "now"));
        Set<UUID> targets = Set.copyOf(Objects.requireNonNull(onlineTargets, "onlineTargets"));
        if (targets.isEmpty() || limit < 1) {
            return List.of();
        }
        return actions.values().stream()
                .filter(action -> action.state() == ActionState.PENDING)
                .filter(action -> targets.contains(action.targetId()))
                .sorted(Comparator
                        .comparingLong(QueuedAction::createdAtEpochMillis)
                        .thenComparing(QueuedAction::id))
                .limit(limit)
                .toList();
    }

    public synchronized void resolve(
            UUID actionId,
            ActionState replacementState,
            String outcome,
            Instant now
    ) {
        writable();
        if (!replacementState.terminal()) {
            throw new IllegalArgumentException("A resolved action cannot remain pending");
        }
        QueuedAction current = actions.get(actionId);
        if (current == null || current.state().terminal()) {
            return;
        }
        QueuedAction replacement = replace(
                current,
                Objects.requireNonNull(replacementState, "replacementState"),
                Objects.requireNonNull(now, "now"),
                outcome);
        validate(replacement);
        actions.put(replacement.id(), replacement);
        changed();
    }

    public synchronized QueuedAction claimAndFlush(UUID actionId, Instant now) throws IOException {
        writable();
        QueuedAction current = requireState(actionId, ActionState.PENDING);
        QueuedAction claimed = replace(current, ActionState.CLAIMED, now, "Claimed for execution.");
        actions.put(claimed.id(), claimed);
        changed();
        flush();
        return claimed;
    }

    public synchronized QueuedAction beginAndFlush(UUID actionId, Instant now) throws IOException {
        writable();
        QueuedAction current = requireState(actionId, ActionState.CLAIMED);
        QueuedAction executing = replace(
                current,
                ActionState.EXECUTING,
                now,
                "Execution started with operation " + current.id() + ".");
        actions.put(executing.id(), executing);
        changed();
        flush();
        return executing;
    }

    public synchronized QueuedAction releaseClaimAndFlush(
            UUID actionId,
            Instant now,
            String detail
    ) throws IOException {
        writable();
        QueuedAction current = requireState(actionId, ActionState.CLAIMED);
        QueuedAction pending = replace(current, ActionState.PENDING, now, detail);
        actions.put(pending.id(), pending);
        changed();
        flush();
        return pending;
    }

    public synchronized QueuedAction resolveAndFlush(
            UUID actionId,
            ActionState replacementState,
            String outcome,
            Instant now
    ) throws IOException {
        resolve(actionId, replacementState, outcome, now);
        flush();
        return actions.get(actionId);
    }

    public synchronized List<QueuedAction> pendingNotifications(Set<UUID> onlineTargets, int limit) {
        Set<UUID> targets = Set.copyOf(Objects.requireNonNull(onlineTargets, "onlineTargets"));
        if (targets.isEmpty() || limit < 1) {
            return List.of();
        }
        return actions.values().stream()
                .filter(action -> action.state().terminal())
                .filter(action -> action.notificationDeliveredAtEpochMillis() == 0L)
                .filter(action -> targets.contains(action.targetId()))
                .sorted(Comparator
                        .comparingLong(QueuedAction::resolvedAtEpochMillis)
                        .thenComparing(QueuedAction::id))
                .limit(limit)
                .toList();
    }

    public synchronized void markNotificationDeliveredAndFlush(
            UUID actionId,
            Instant now
    ) throws IOException {
        writable();
        QueuedAction current = actions.get(Objects.requireNonNull(actionId, "actionId"));
        if (current == null
                || !current.state().terminal()
                || current.notificationDeliveredAtEpochMillis() > 0L) {
            return;
        }
        QueuedAction updated = new QueuedAction(
                current.id(),
                current.idempotencyKey(),
                current.actorId(),
                current.targetId(),
                current.actionId(),
                current.variantId(),
                current.targetFieldId(),
                current.values(),
                current.createdAtEpochMillis(),
                current.expiresAtEpochMillis(),
                current.claimedAtEpochMillis(),
                current.executionStartedAtEpochMillis(),
                current.resolvedAtEpochMillis(),
                Objects.requireNonNull(now, "now").toEpochMilli(),
                current.state(),
                current.outcome(),
                Math.addExact(current.revision(), 1L));
        validate(updated);
        actions.put(updated.id(), updated);
        changed();
        flush();
    }

    public synchronized List<QueuedAction> entries() {
        return actions.values().stream()
                .sorted(Comparator
                        .comparingLong(QueuedAction::createdAtEpochMillis)
                        .thenComparing(QueuedAction::id))
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
            snapshot = new Snapshot(new ArrayList<>(actions.values()));
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
                Set.of("/actions"));
        synchronized (this) {
            document = StorageService.read(
                    destination,
                    domain(),
                    SCHEMA_VERSION).orElse(previous);
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

    private void prune(Instant now) {
        long cutoff = now.minus(Duration.ofDays(30L)).toEpochMilli();
        boolean changed = actions.values().removeIf(action ->
                action.state().terminal()
                        && action.resolvedAtEpochMillis() > 0L
                        && action.resolvedAtEpochMillis() < cutoff);
        List<QueuedAction> expired = actions.values().stream()
                .filter(action -> action.state() == ActionState.PENDING
                        || action.state() == ActionState.CLAIMED)
                .filter(action -> action.expiresAtEpochMillis() <= now.toEpochMilli())
                .toList();
        for (QueuedAction action : expired) {
            actions.put(
                    action.id(),
                    replace(
                            action,
                            ActionState.EXPIRED,
                            now,
                            "The queued action expired before it could run."));
            changed = true;
        }
        if (changed) {
            changed();
        }
    }

    private void removeOldestCompleted() {
        actions.values().stream()
                .filter(action -> action.state().terminal())
                .min(Comparator
                        .comparingLong(QueuedAction::resolvedAtEpochMillis)
                        .thenComparing(QueuedAction::id))
                .ifPresent(action -> actions.remove(action.id()));
    }

    private void changed() {
        revision = Math.addExact(revision, 1L);
    }

    private void writable() {
        if (state == RepositoryState.RECOVERY
                || state == RepositoryState.UNSUPPORTED
                || state == RepositoryState.ERROR
                || state == RepositoryState.CLOSED) {
            throw new IllegalStateException("Offline action storage is unavailable");
        }
    }

    private QueuedAction requireState(UUID actionId, ActionState required) {
        QueuedAction current = actions.get(Objects.requireNonNull(actionId, "actionId"));
        if (current == null) {
            throw new IllegalStateException("Queued action was not found");
        }
        if (current.state() != required) {
            throw new IllegalStateException("Queued action state changed");
        }
        return current;
    }

    private static QueuedAction replace(
            QueuedAction current,
            ActionState state,
            Instant now,
            String outcome
    ) {
        long timestamp = Objects.requireNonNull(now, "now").toEpochMilli();
        long claimedAt = current.claimedAtEpochMillis();
        long executionStartedAt = current.executionStartedAtEpochMillis();
        long resolvedAt = current.resolvedAtEpochMillis();
        if (state == ActionState.PENDING) {
            claimedAt = 0L;
            executionStartedAt = 0L;
            resolvedAt = 0L;
        } else if (state == ActionState.CLAIMED) {
            claimedAt = timestamp;
            executionStartedAt = 0L;
            resolvedAt = 0L;
        } else if (state == ActionState.EXECUTING) {
            claimedAt = claimedAt == 0L ? timestamp : claimedAt;
            executionStartedAt = timestamp;
            resolvedAt = 0L;
        } else if (state.terminal()) {
            resolvedAt = timestamp;
        }
        QueuedAction replacement = new QueuedAction(
                current.id(),
                current.idempotencyKey(),
                current.actorId(),
                current.targetId(),
                current.actionId(),
                current.variantId(),
                current.targetFieldId(),
                current.values(),
                current.createdAtEpochMillis(),
                current.expiresAtEpochMillis(),
                claimedAt,
                executionStartedAt,
                resolvedAt,
                current.notificationDeliveredAtEpochMillis(),
                state,
                bounded(outcome, 512),
                Math.addExact(current.revision(), 1L));
        validate(replacement);
        return replacement;
    }

    private static QueuedAction normalizeLoaded(QueuedAction stored, Instant now) {
        String idempotencyKey = Objects.requireNonNullElse(stored.idempotencyKey(), "");
        if (idempotencyKey.isBlank()) {
            idempotencyKey = "sef:offline:" + stored.id();
        }
        ActionState loadedState = Objects.requireNonNull(stored.state(), "state");
        ActionState state = switch (loadedState) {
            case COMPLETED -> ActionState.SUCCEEDED;
            case CLAIMED -> ActionState.PENDING;
            case EXECUTING -> ActionState.OUTCOME_UNKNOWN;
            default -> loadedState;
        };
        long claimedAt = stored.claimedAtEpochMillis();
        long executionStartedAt = stored.executionStartedAtEpochMillis();
        long resolvedAt = stored.resolvedAtEpochMillis();
        String outcome = stored.outcome();
        if (loadedState == ActionState.CLAIMED) {
            claimedAt = 0L;
            executionStartedAt = 0L;
            resolvedAt = 0L;
            outcome = "Recovered before execution started.";
        } else if (loadedState == ActionState.EXECUTING) {
            claimedAt = claimedAt == 0L ? stored.createdAtEpochMillis() : claimedAt;
            executionStartedAt = executionStartedAt == 0L ? claimedAt : executionStartedAt;
            resolvedAt = now.toEpochMilli();
            outcome = "Execution outcome is unknown after process restart.";
        }
        if (idempotencyKey.equals(stored.idempotencyKey())
                && state == loadedState) {
            return stored;
        }
        return new QueuedAction(
                stored.id(),
                idempotencyKey,
                stored.actorId(),
                stored.targetId(),
                stored.actionId(),
                stored.variantId(),
                stored.targetFieldId(),
                stored.values(),
                stored.createdAtEpochMillis(),
                stored.expiresAtEpochMillis(),
                claimedAt,
                executionStartedAt,
                resolvedAt,
                stored.notificationDeliveredAtEpochMillis(),
                state,
                bounded(outcome, 512),
                Math.max(1L, stored.revision() + 1L));
    }

    private static void validate(QueuedAction action) {
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(action.id(), "id");
        if (!("sef:offline:" + action.id()).equals(action.idempotencyKey())) {
            throw new IllegalArgumentException("Offline action idempotency key is invalid");
        }
        Objects.requireNonNull(action.actorId(), "actorId");
        Objects.requireNonNull(action.targetId(), "targetId");
        Objects.requireNonNull(action.state(), "state");
        if (!action.actionId().matches("[a-z][a-z0-9_.-]*:[a-z0-9_./-]+")
                || !action.variantId().matches("[a-z0-9_]{1,64}")
                || !action.targetFieldId().matches("[a-zA-Z0-9_]{1,64}")
                || action.values() == null
                || action.values().size() > MAXIMUM_FIELDS
                || action.createdAtEpochMillis() < 1L
                || action.expiresAtEpochMillis() <= action.createdAtEpochMillis()
                || action.claimedAtEpochMillis() < 0L
                || action.executionStartedAtEpochMillis() < 0L
                || action.resolvedAtEpochMillis() < 0L
                || action.notificationDeliveredAtEpochMillis() < 0L
                || action.revision() < 1L
                || action.outcome().length() > 512) {
            throw new IllegalArgumentException("Offline action is invalid");
        }
        if (action.state() == ActionState.PENDING
                && (action.claimedAtEpochMillis() != 0L
                || action.executionStartedAtEpochMillis() != 0L
                || action.resolvedAtEpochMillis() != 0L)
                || action.state() == ActionState.CLAIMED
                && (action.claimedAtEpochMillis() < action.createdAtEpochMillis()
                || action.executionStartedAtEpochMillis() != 0L
                || action.resolvedAtEpochMillis() != 0L)
                || action.state() == ActionState.EXECUTING
                && (action.claimedAtEpochMillis() < action.createdAtEpochMillis()
                || action.executionStartedAtEpochMillis() < action.claimedAtEpochMillis()
                || action.resolvedAtEpochMillis() != 0L)
                || action.state().terminal()
                && action.resolvedAtEpochMillis() < action.createdAtEpochMillis()
                || action.notificationDeliveredAtEpochMillis() > 0L
                && (!action.state().terminal()
                || action.notificationDeliveredAtEpochMillis() < action.resolvedAtEpochMillis())) {
            throw new IllegalArgumentException("Offline action state timestamps are invalid");
        }
        action.values().forEach((field, value) -> {
            if (field == null
                    || !field.matches("[a-zA-Z0-9_]{1,64}")
                    || value == null
                    || value.length() > 4096
                    || value.codePoints().anyMatch(Character::isISOControl)) {
                throw new IllegalArgumentException("Offline action field is invalid");
            }
        });
    }

    private static String bounded(String value, int maximum) {
        String normalized = Objects.requireNonNullElse(value, "").strip();
        return normalized.length() <= maximum
                ? normalized
                : normalized.substring(0, maximum);
    }

    private record Snapshot(List<QueuedAction> actions) {
        private Snapshot {
            actions = List.copyOf(actions == null ? List.of() : actions);
        }
    }

    public record QueuedAction(
            UUID id,
            String idempotencyKey,
            UUID actorId,
            UUID targetId,
            String actionId,
            String variantId,
            String targetFieldId,
            Map<String, String> values,
            long createdAtEpochMillis,
            long expiresAtEpochMillis,
            long claimedAtEpochMillis,
            long executionStartedAtEpochMillis,
            long resolvedAtEpochMillis,
            long notificationDeliveredAtEpochMillis,
            ActionState state,
            String outcome,
            long revision
    ) {
        public QueuedAction {
            idempotencyKey = Objects.requireNonNullElse(
                    idempotencyKey,
                    id == null ? "" : "sef:offline:" + id);
            values = Map.copyOf(values == null ? Map.of() : values);
            outcome = Objects.requireNonNullElse(outcome, "");
        }
    }

    public enum ActionState {
        PENDING,
        CLAIMED,
        EXECUTING,
        SUCCEEDED,
        COMPLETED,
        FAILED,
        CANCELED,
        OUTCOME_UNKNOWN,
        EXPIRED,
        REVOKED;

        public boolean terminal() {
            return switch (this) {
                case SUCCEEDED, COMPLETED, FAILED, CANCELED, OUTCOME_UNKNOWN, EXPIRED, REVOKED -> true;
                case PENDING, CLAIMED, EXECUTING -> false;
            };
        }
    }
}
