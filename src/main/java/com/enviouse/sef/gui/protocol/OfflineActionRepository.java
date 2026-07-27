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
    public static final int SCHEMA_VERSION = 1;
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
        boolean existed = Files.exists(path);
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
            for (QueuedAction action : snapshot.actions()) {
                validate(action);
                if (actions.putIfAbsent(action.id(), action) != null) {
                    throw new IllegalStateException("Duplicate offline action");
                }
            }
            state = RepositoryState.READY;
            if (document.migrated()) {
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
        QueuedAction action = new QueuedAction(
                UUID.randomUUID(),
                Objects.requireNonNull(actorId, "actorId"),
                Objects.requireNonNull(targetId, "targetId"),
                actionId,
                variantId,
                targetFieldId,
                Map.copyOf(values),
                now.toEpochMilli(),
                now.plus(lifetime).toEpochMilli(),
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

    public synchronized void resolve(
            UUID actionId,
            ActionState replacementState,
            String outcome,
            Instant now
    ) {
        writable();
        if (replacementState == ActionState.PENDING) {
            throw new IllegalArgumentException("A resolved action cannot remain pending");
        }
        QueuedAction current = actions.get(actionId);
        if (current == null || current.state() != ActionState.PENDING) {
            return;
        }
        QueuedAction replacement = new QueuedAction(
                current.id(),
                current.actorId(),
                current.targetId(),
                current.actionId(),
                current.variantId(),
                current.targetFieldId(),
                current.values(),
                current.createdAtEpochMillis(),
                current.expiresAtEpochMillis(),
                Objects.requireNonNull(now, "now").toEpochMilli(),
                Objects.requireNonNull(replacementState, "replacementState"),
                bounded(outcome, 512),
                current.revision() + 1L);
        validate(replacement);
        actions.put(replacement.id(), replacement);
        changed();
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
                action.state() != ActionState.PENDING
                        && action.resolvedAtEpochMillis() > 0L
                        && action.resolvedAtEpochMillis() < cutoff);
        List<QueuedAction> expired = actions.values().stream()
                .filter(action -> action.state() == ActionState.PENDING)
                .filter(action -> action.expiresAtEpochMillis() <= now.toEpochMilli())
                .toList();
        for (QueuedAction action : expired) {
            actions.put(action.id(), new QueuedAction(
                    action.id(),
                    action.actorId(),
                    action.targetId(),
                    action.actionId(),
                    action.variantId(),
                    action.targetFieldId(),
                    action.values(),
                    action.createdAtEpochMillis(),
                    action.expiresAtEpochMillis(),
                    now.toEpochMilli(),
                    ActionState.EXPIRED,
                    "The queued action expired before it could run.",
                    action.revision() + 1L));
            changed = true;
        }
        if (changed) {
            changed();
        }
    }

    private void removeOldestCompleted() {
        actions.values().stream()
                .filter(action -> action.state() != ActionState.PENDING)
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

    private static void validate(QueuedAction action) {
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(action.id(), "id");
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
                || action.resolvedAtEpochMillis() < 0L
                || action.revision() < 1L
                || action.outcome().length() > 512) {
            throw new IllegalArgumentException("Offline action is invalid");
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
            UUID actorId,
            UUID targetId,
            String actionId,
            String variantId,
            String targetFieldId,
            Map<String, String> values,
            long createdAtEpochMillis,
            long expiresAtEpochMillis,
            long resolvedAtEpochMillis,
            ActionState state,
            String outcome,
            long revision
    ) {
        public QueuedAction {
            values = Map.copyOf(values == null ? Map.of() : values);
            outcome = Objects.requireNonNullElse(outcome, "");
        }
    }

    public enum ActionState {
        PENDING,
        COMPLETED,
        FAILED,
        EXPIRED,
        REVOKED
    }
}
