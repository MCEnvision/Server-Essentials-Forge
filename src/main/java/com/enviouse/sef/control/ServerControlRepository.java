package com.enviouse.sef.control;

import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.storage.InstantJsonAdapter;
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class ServerControlRepository implements StorageRepository {
    public static final int SCHEMA_VERSION = 2;
    public static final int HARD_MAXIMUM_RECORDS = 100_000;
    public static final int HARD_MAXIMUM_HISTORY = 200_000;
    public static final int HARD_MAXIMUM_EXECUTIONS = 200_000;
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantJsonAdapter())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private final Map<UUID, ControlRecord> records = new LinkedHashMap<>();
    private final List<HistoryEntry> history = new ArrayList<>();
    private final Map<UUID, ExecutionOperation> executions = new LinkedHashMap<>();
    private Path path;
    private StorageService.Document document;
    private RepositoryState state = RepositoryState.NEW;
    private boolean dirty;
    private long revision = 1L;

    @Override
    public String id() {
        return "sef:server_control";
    }

    @Override
    public String domain() {
        return "server_control";
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public synchronized Path path() {
        return path;
    }

    public synchronized ActionResult<ControlRecord> create(
            String featureId,
            UUID actorId,
            UUID subjectId,
            String title,
            String details,
            Instant expiresAt,
            Map<String, String> metadata
    ) {
        writable();
        var definition = ServerControlCatalog.require(featureId);
        prune(Instant.now());
        long featureCount = records.values().stream()
                .filter(record -> record.featureId().equals(definition.id()))
                .filter(record -> record.state() != RecordState.ARCHIVED)
                .count();
        if (records.size() >= HARD_MAXIMUM_RECORDS || featureCount >= definition.maximumRecords()) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "server control record limit reached");
        }
        UUID safeActor = Objects.requireNonNullElse(actorId, new UUID(0L, 0L));
        Instant now = Instant.now();
        if (expiresAt != null
                && (!expiresAt.isAfter(now) || expiresAt.isAfter(now.plus(Duration.ofDays(3650))))) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "server control expiry is outside bounds");
        }
        ControlRecord record;
        try {
            Map<String, String> safeMetadata = validatedMetadata(definition.id(), metadata);
            record = new ControlRecord(
                    UUID.randomUUID(),
                    definition.id(),
                    safeActor,
                    subjectId,
                    bounded(title, 128, false),
                    bounded(details, 4096, true),
                    RecordState.OPEN,
                    now,
                    now,
                    expiresAt,
                    1L,
                    safeMetadata);
        } catch (IllegalArgumentException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, exception.getMessage());
        }
        records.put(record.id(), record);
        changed();
        history(record, safeActor, "created", RecordState.OPEN, RecordState.OPEN);
        audit(safeActor, record, "create", AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        return ActionResult.success(record);
    }

    public synchronized ActionResult<ControlRecord> transition(
            UUID recordId,
            UUID actorId,
            RecordState replacement,
            long expectedRevision,
            String note
    ) {
        return transition(recordId, actorId, replacement, expectedRevision, note, false);
    }

    synchronized ActionResult<ControlRecord> transitionExecuted(
            UUID recordId,
            UUID actorId,
            RecordState replacement,
            long expectedRevision,
            String note
    ) {
        return transition(recordId, actorId, replacement, expectedRevision, note, true);
    }

    private ActionResult<ControlRecord> transition(
            UUID recordId,
            UUID actorId,
            RecordState replacement,
            long expectedRevision,
            String note,
            boolean execution
    ) {
        writable();
        Objects.requireNonNull(recordId, "recordId");
        Objects.requireNonNull(replacement, "replacement");
        ControlRecord current = records.get(recordId);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "server control record not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "server control record revision changed");
        }
        if (!execution && hasBlockingExecution(recordId)) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.CONFLICT,
                    "server control record has an incomplete execution");
        }
        var definition = ServerControlCatalog.require(current.featureId());
        if (!definition.states().contains(replacement)) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "record state is unavailable for this feature");
        }
        if (!allowedTransition(current.state(), replacement)) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "server control state transition is invalid");
        }
        ServerControlSchemaRegistry.RuntimeClass runtimeClass =
                ServerControlSchemaRegistry.require(current.featureId()).runtimeClass();
        if (!execution
                && runtimeClass != ServerControlSchemaRegistry.RuntimeClass.REVIEW_QUEUE
                && (replacement == RecordState.ACTIVE || replacement == RecordState.RESOLVED)) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.POLICY_DENIED,
                    "active and resolved states require the reviewed execution path");
        }
        if ((replacement == RecordState.ACTIVE || replacement == RecordState.APPROVED)
                && !ServerControlSchemaRegistry.require(current.featureId()).missing(current.metadata()).isEmpty()) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "required server control fields are missing");
        }
        UUID safeActor = Objects.requireNonNullElse(actorId, new UUID(0L, 0L));
        ControlRecord updated = new ControlRecord(
                current.id(),
                current.featureId(),
                current.ownerId(),
                current.subjectId(),
                current.title(),
                current.details(),
                replacement,
                current.createdAt(),
                Instant.now(),
                current.expiresAt(),
                Math.addExact(current.revision(), 1L),
                current.metadata());
        records.put(updated.id(), updated);
        changed();
        history(updated, safeActor, bounded(note, 512, true), current.state(), replacement);
        audit(safeActor, updated, "transition", AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        return ActionResult.success(updated);
    }

    public synchronized ActionResult<ControlRecord> update(
            UUID recordId,
            UUID actorId,
            String title,
            String details,
            long expectedRevision
    ) {
        writable();
        ControlRecord current = records.get(Objects.requireNonNull(recordId, "recordId"));
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "server control record not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "server control record revision changed");
        }
        if (hasBlockingExecution(recordId)) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.CONFLICT,
                    "server control record has an incomplete execution");
        }
        ControlRecord updated;
        try {
            updated = new ControlRecord(
                    current.id(),
                    current.featureId(),
                    current.ownerId(),
                    current.subjectId(),
                    bounded(title, 128, false),
                    bounded(details, 4096, true),
                    current.state(),
                    current.createdAt(),
                    Instant.now(),
                    current.expiresAt(),
                    Math.addExact(current.revision(), 1L),
                    current.metadata());
        } catch (IllegalArgumentException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, exception.getMessage());
        }
        UUID safeActor = Objects.requireNonNullElse(actorId, new UUID(0L, 0L));
        records.put(updated.id(), updated);
        changed();
        history(updated, safeActor, "updated", current.state(), current.state());
        audit(safeActor, updated, "update", AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        return ActionResult.success(updated);
    }

    public synchronized ActionResult<ControlRecord> configure(
            UUID recordId,
            UUID actorId,
            String fieldId,
            String value,
            boolean remove,
            long expectedRevision
    ) {
        writable();
        ControlRecord current = records.get(Objects.requireNonNull(recordId, "recordId"));
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "server control record not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "server control record revision changed");
        }
        if (hasBlockingExecution(recordId)) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.CONFLICT,
                    "server control record has an incomplete execution");
        }
        if (current.state() == RecordState.ACTIVE
                || current.state() == RecordState.ARCHIVED
                || current.state() == RecordState.EXPIRED) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.POLICY_DENIED,
                    "pause or restore the record before changing its fields");
        }
        ServerControlSchemaRegistry.FeatureSchema schema =
                ServerControlSchemaRegistry.require(current.featureId());
        ServerControlSchemaRegistry.FieldDefinition field;
        try {
            field = schema.field(fieldId);
        } catch (IllegalArgumentException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, exception.getMessage());
        }
        if (remove && field.required()) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "required field cannot be removed");
        }
        Map<String, String> metadata = new LinkedHashMap<>(current.metadata());
        String metadataKey = "field." + field.id();
        try {
            if (remove) {
                metadata.remove(metadataKey);
            } else {
                metadata.put(metadataKey, field.validate(value));
            }
            metadata = boundedMetadata(metadata);
        } catch (IllegalArgumentException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, exception.getMessage());
        }
        ControlRecord updated = new ControlRecord(
                current.id(),
                current.featureId(),
                current.ownerId(),
                current.subjectId(),
                current.title(),
                current.details(),
                current.state(),
                current.createdAt(),
                Instant.now(),
                current.expiresAt(),
                Math.addExact(current.revision(), 1L),
                metadata);
        UUID safeActor = Objects.requireNonNullElse(actorId, new UUID(0L, 0L));
        records.put(updated.id(), updated);
        changed();
        history(
                updated,
                safeActor,
                remove ? "removed field " + field.id() : "configured field " + field.id(),
                current.state(),
                current.state());
        audit(safeActor, updated, "configure", AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        return ActionResult.success(updated);
    }

    public synchronized ActionResult<ControlRecord> configureAll(
            UUID recordId,
            UUID actorId,
            String title,
            String details,
            Map<String, String> fieldValues,
            long expectedRevision
    ) {
        writable();
        ControlRecord current = records.get(Objects.requireNonNull(recordId, "recordId"));
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "server control record not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "server control record revision changed");
        }
        if (hasBlockingExecution(recordId)) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.CONFLICT,
                    "server control record has an incomplete execution");
        }
        if (current.state() == RecordState.ACTIVE
                || current.state() == RecordState.ARCHIVED
                || current.state() == RecordState.EXPIRED) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.POLICY_DENIED,
                    "pause or restore the record before changing its fields");
        }
        Map<String, String> requested = Objects.requireNonNull(fieldValues, "fieldValues");
        ServerControlSchemaRegistry.FeatureSchema schema =
                ServerControlSchemaRegistry.require(current.featureId());
        if (requested.size() > schema.fields().size()) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.INVALID_INPUT,
                    "server control field count is outside bounds");
        }
        Map<String, String> metadata = new LinkedHashMap<>(current.metadata());
        try {
            for (Map.Entry<String, String> entry : requested.entrySet()) {
                ServerControlSchemaRegistry.FieldDefinition field = schema.field(entry.getKey());
                String value = Objects.requireNonNullElse(entry.getValue(), "").strip();
                String metadataKey = "field." + field.id();
                if (value.isEmpty() && !field.required()) {
                    metadata.remove(metadataKey);
                } else {
                    metadata.put(metadataKey, field.validate(value));
                }
            }
            metadata = boundedMetadata(metadata);
            title = bounded(title, 128, false);
            details = bounded(details, 4096, true);
        } catch (IllegalArgumentException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, exception.getMessage());
        }
        ControlRecord updated = new ControlRecord(
                current.id(),
                current.featureId(),
                current.ownerId(),
                current.subjectId(),
                title,
                details,
                current.state(),
                current.createdAt(),
                Instant.now(),
                current.expiresAt(),
                Math.addExact(current.revision(), 1L),
                metadata);
        UUID safeActor = Objects.requireNonNullElse(actorId, new UUID(0L, 0L));
        records.put(updated.id(), updated);
        changed();
        history(updated, safeActor, "configured record", current.state(), current.state());
        audit(safeActor, updated, "configure_all", AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        return ActionResult.success(updated);
    }

    public synchronized Optional<ControlRecord> find(UUID id) {
        prune(Instant.now());
        return Optional.ofNullable(records.get(Objects.requireNonNull(id, "id")));
    }

    public synchronized List<ControlRecord> records(String featureId) {
        String normalized = ServerControlCatalog.require(featureId).id();
        prune(Instant.now());
        return records.values().stream()
                .filter(record -> record.featureId().equals(normalized))
                .sorted(Comparator.comparing(ControlRecord::updatedAt).reversed())
                .toList();
    }

    public synchronized List<ControlRecord> recordsFor(UUID ownerId) {
        Objects.requireNonNull(ownerId, "ownerId");
        prune(Instant.now());
        return records.values().stream()
                .filter(record -> record.ownerId().equals(ownerId)
                        || ownerId.equals(record.subjectId()))
                .sorted(Comparator.comparing(ControlRecord::updatedAt).reversed())
                .toList();
    }

    public synchronized List<HistoryEntry> history(UUID recordId) {
        Objects.requireNonNull(recordId, "recordId");
        return history.stream()
                .filter(entry -> entry.recordId().equals(recordId))
                .sorted(Comparator.comparing(HistoryEntry::occurredAt).reversed())
                .toList();
    }

    public synchronized ActionResult<ExecutionOperation> prepareExecution(
            UUID recordId,
            UUID actorId,
            RecordState destination,
            long expectedRevision
    ) {
        writable();
        Objects.requireNonNull(recordId, "recordId");
        Objects.requireNonNull(actorId, "actorId");
        Objects.requireNonNull(destination, "destination");
        ControlRecord record = records.get(recordId);
        if (record == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "server control record not found");
        }
        if (record.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "server control record revision changed");
        }
        if (hasBlockingExecution(recordId)) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.CONFLICT,
                    "server control record has an incomplete execution");
        }
        if (!ServerControlCatalog.require(record.featureId()).states().contains(destination)
                || !allowedTransition(record.state(), destination)) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.POLICY_DENIED,
                    "server control execution destination is invalid");
        }
        if (executions.size() >= HARD_MAXIMUM_EXECUTIONS) {
            pruneExecutions();
        }
        if (executions.size() >= HARD_MAXIMUM_EXECUTIONS) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.QUOTA_EXCEEDED,
                    "server control execution history limit reached");
        }
        Instant now = Instant.now();
        UUID operationId = UUID.randomUUID();
        ExecutionOperation operation = new ExecutionOperation(
                operationId,
                "sef:control:" + operationId,
                record.id(),
                record.featureId(),
                actorId,
                expectedRevision,
                destination,
                ExecutionStatus.PREPARED,
                now,
                now,
                "");
        executions.put(operation.id(), operation);
        changed();
        return ActionResult.success(operation);
    }

    public synchronized ActionResult<ExecutionOperation> beginExecution(UUID operationId) {
        writable();
        ExecutionOperation current = executions.get(Objects.requireNonNull(operationId, "operationId"));
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "server control execution not found");
        }
        if (current.status() != ExecutionStatus.PREPARED) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.CONFLICT,
                    "server control execution is not prepared");
        }
        ExecutionOperation updated = replaceExecution(
                current,
                ExecutionStatus.EXECUTING,
                "handler invocation claimed");
        executions.put(updated.id(), updated);
        changed();
        return ActionResult.success(updated);
    }

    public synchronized ActionResult<ExecutionOperation> failExecution(
            UUID operationId,
            String detail
    ) {
        writable();
        ExecutionOperation current = executions.get(Objects.requireNonNull(operationId, "operationId"));
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "server control execution not found");
        }
        if (current.status() != ExecutionStatus.PREPARED
                && current.status() != ExecutionStatus.EXECUTING) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.CONFLICT,
                    "server control execution is already terminal");
        }
        ExecutionOperation updated = replaceExecution(current, ExecutionStatus.FAILED, detail);
        executions.put(updated.id(), updated);
        changed();
        return ActionResult.success(updated);
    }

    public synchronized ActionResult<ExecutionOperation> markOutcomeUnknown(
            UUID operationId,
            String detail
    ) {
        writable();
        ExecutionOperation current = executions.get(Objects.requireNonNull(operationId, "operationId"));
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "server control execution not found");
        }
        if (current.status() == ExecutionStatus.EXECUTED
                || current.status() == ExecutionStatus.FAILED) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.CONFLICT,
                    "server control execution is already terminal");
        }
        ExecutionOperation updated = replaceExecution(current, ExecutionStatus.OUTCOME_UNKNOWN, detail);
        executions.put(updated.id(), updated);
        changed();
        return ActionResult.success(updated);
    }

    public synchronized ActionResult<ControlRecord> completeExecution(
            UUID operationId,
            String detail
    ) {
        writable();
        ExecutionOperation operation = executions.get(Objects.requireNonNull(operationId, "operationId"));
        if (operation == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "server control execution not found");
        }
        if (operation.status() == ExecutionStatus.EXECUTED) {
            return find(operation.recordId())
                    .map(ActionResult::success)
                    .orElseGet(() -> ActionResult.failure(
                            ActionResult.ReasonCode.NOT_FOUND,
                            "server control record not found"));
        }
        if (operation.status() != ExecutionStatus.EXECUTING) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.CONFLICT,
                    "server control execution is not active");
        }
        ControlRecord current = records.get(operation.recordId());
        if (current == null || current.revision() != operation.recordRevision()) {
            markOutcomeUnknown(operation.id(), "record changed before terminal commit");
            return ActionResult.failure(
                    ActionResult.ReasonCode.CONFLICT,
                    "server control record changed before terminal commit");
        }
        ControlRecord updated = replaceRecordState(current, operation.destination());
        records.put(updated.id(), updated);
        executions.put(
                operation.id(),
                replaceExecution(operation, ExecutionStatus.EXECUTED, detail));
        changed();
        history(
                updated,
                operation.actorId(),
                "executed operation " + operation.id(),
                current.state(),
                updated.state());
        audit(
                operation.actorId(),
                updated,
                "execute",
                AuditService.Result.SUCCESS,
                ActionResult.ReasonCode.SUCCESS);
        return ActionResult.success(updated);
    }

    public synchronized ActionResult<ExecutionOperation> reconcileExecution(
            UUID operationId,
            UUID actorId,
            boolean effectOccurred,
            String note
    ) {
        writable();
        ExecutionOperation operation = executions.get(Objects.requireNonNull(operationId, "operationId"));
        if (operation == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "server control execution not found");
        }
        if (operation.status() != ExecutionStatus.OUTCOME_UNKNOWN) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.CONFLICT,
                    "server control execution does not require reconciliation");
        }
        String boundedNote = bounded(note, 512, false);
        if (!effectOccurred) {
            ExecutionOperation failed = replaceExecution(
                    operation,
                    ExecutionStatus.FAILED,
                    "operator confirmed not applied, " + boundedNote);
            executions.put(failed.id(), failed);
            changed();
            return ActionResult.success(failed);
        }
        ControlRecord current = records.get(operation.recordId());
        if (current == null || current.revision() != operation.recordRevision()) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.CONFLICT,
                    "server control record changed before reconciliation");
        }
        ControlRecord updated = replaceRecordState(current, operation.destination());
        records.put(updated.id(), updated);
        ExecutionOperation completed = replaceExecution(
                operation,
                ExecutionStatus.EXECUTED,
                "operator confirmed applied, " + boundedNote);
        executions.put(completed.id(), completed);
        changed();
        UUID safeActor = Objects.requireNonNullElse(actorId, new UUID(0L, 0L));
        history(
                updated,
                safeActor,
                "reconciled operation " + operation.id(),
                current.state(),
                updated.state());
        audit(
                safeActor,
                updated,
                "reconcile",
                AuditService.Result.SUCCESS,
                ActionResult.ReasonCode.SUCCESS);
        return ActionResult.success(completed);
    }

    public synchronized Optional<ExecutionOperation> execution(UUID operationId) {
        return Optional.ofNullable(executions.get(Objects.requireNonNull(operationId, "operationId")));
    }

    public synchronized List<ExecutionOperation> executions(ExecutionStatus status) {
        return executions.values().stream()
                .filter(operation -> status == null || operation.status() == status)
                .sorted(Comparator.comparing(ExecutionOperation::updatedAt).reversed())
                .toList();
    }

    public synchronized Diagnostic diagnostic() {
        prune(Instant.now());
        long active = records.values().stream()
                .filter(record -> record.state() == RecordState.ACTIVE || record.state() == RecordState.OPEN)
                .count();
        long incompleteExecutions = executions.values().stream()
                .filter(operation -> operation.status() == ExecutionStatus.PREPARED
                        || operation.status() == ExecutionStatus.EXECUTING
                        || operation.status() == ExecutionStatus.OUTCOME_UNKNOWN)
                .count();
        return new Diagnostic(
                records.size(),
                history.size(),
                active,
                executions.size(),
                incompleteExecutions,
                revision,
                state,
                dirty);
    }

    @Override
    public synchronized LoadResult load(Path managedRoot) {
        path = Objects.requireNonNull(managedRoot, "managedRoot").resolve("server-control.json");
        clear();
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(null);
        if (document == null) {
            state = Files.exists(path, java.nio.file.LinkOption.NOFOLLOW_LINKS)
                    ? RepositoryState.RECOVERY
                    : RepositoryState.MISSING;
            return new LoadResult(state, state == RepositoryState.MISSING
                    ? "new server control repository"
                    : "server control storage unavailable");
        }
        try {
            Snapshot snapshot = GSON.fromJson(document.data(), Snapshot.class);
            if (snapshot == null
                    || snapshot.revision() < 1L
                    || snapshot.records().size() > HARD_MAXIMUM_RECORDS
                    || snapshot.history().size() > HARD_MAXIMUM_HISTORY
                    || snapshot.executions().size() > HARD_MAXIMUM_EXECUTIONS) {
                throw new IllegalStateException("server control snapshot is outside bounds");
            }
            for (ControlRecord record : snapshot.records()) {
                validate(record);
                if (records.putIfAbsent(record.id(), record) != null) {
                    throw new IllegalStateException("duplicate server control record");
                }
            }
            for (HistoryEntry entry : snapshot.history()) {
                validate(entry);
                history.add(entry);
            }
            boolean reconciled = false;
            for (ExecutionOperation operation : snapshot.executions()) {
                validate(operation);
                ExecutionOperation loaded = switch (operation.status()) {
                    case PREPARED -> replaceExecution(
                            operation,
                            ExecutionStatus.FAILED,
                            "process stopped before handler invocation");
                    case EXECUTING -> replaceExecution(
                            operation,
                            ExecutionStatus.OUTCOME_UNKNOWN,
                            "process stopped during handler invocation");
                    default -> operation;
                };
                reconciled |= loaded != operation;
                if (executions.putIfAbsent(loaded.id(), loaded) != null) {
                    throw new IllegalStateException("duplicate server control execution");
                }
            }
            revision = reconciled
                    ? Math.addExact(snapshot.revision(), 1L)
                    : snapshot.revision();
            prune(Instant.now());
            state = RepositoryState.READY;
            dirty = document.migrated() || reconciled;
            return new LoadResult(state, "loaded server control data");
        } catch (RuntimeException exception) {
            clear();
            state = RepositoryState.RECOVERY;
            return new LoadResult(state, exception.getClass().getSimpleName());
        }
    }

    @Override
    public synchronized void flush() throws IOException {
        writable();
        Snapshot snapshot = new Snapshot(
                revision,
                List.copyOf(records.values()),
                List.copyOf(history),
                List.copyOf(executions.values()));
        StorageService.write(path, domain(), SCHEMA_VERSION, GSON.toJsonTree(snapshot), document);
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(document);
        dirty = false;
        state = RepositoryState.READY;
    }

    @Override
    public synchronized boolean dirty() {
        return dirty;
    }

    @Override
    public synchronized RepositoryState state() {
        return state;
    }

    private void prune(Instant now) {
        List<ControlRecord> expired = records.values().stream()
                .filter(record -> record.expiresAt() != null && !record.expiresAt().isAfter(now))
                .filter(record -> record.state() != RecordState.EXPIRED
                        && record.state() != RecordState.ARCHIVED
                        && record.state() != RecordState.CANCELLED
                        && record.state() != RecordState.RESOLVED)
                .toList();
        for (ControlRecord current : expired) {
            ControlRecord replacement = new ControlRecord(
                    current.id(),
                    current.featureId(),
                    current.ownerId(),
                    current.subjectId(),
                    current.title(),
                    current.details(),
                    RecordState.EXPIRED,
                    current.createdAt(),
                    now,
                    current.expiresAt(),
                    Math.addExact(current.revision(), 1L),
                    current.metadata());
            records.put(replacement.id(), replacement);
            history(replacement, new UUID(0L, 0L), "expired", current.state(), RecordState.EXPIRED);
            changed();
        }
    }

    private void history(
            ControlRecord record,
            UUID actorId,
            String note,
            RecordState before,
            RecordState after
    ) {
        if (history.size() >= HARD_MAXIMUM_HISTORY) {
            history.removeFirst();
        }
        history.add(new HistoryEntry(
                UUID.randomUUID(),
                record.id(),
                record.featureId(),
                actorId,
                before,
                after,
                bounded(note, 512, true),
                Instant.now(),
                record.revision()));
    }

    private void changed() {
        dirty = true;
        revision = Math.addExact(revision, 1L);
    }

    private boolean hasBlockingExecution(UUID recordId) {
        return executions.values().stream()
                .filter(operation -> operation.recordId().equals(recordId))
                .anyMatch(operation -> operation.status() == ExecutionStatus.PREPARED
                        || operation.status() == ExecutionStatus.EXECUTING
                        || operation.status() == ExecutionStatus.OUTCOME_UNKNOWN);
    }

    private void pruneExecutions() {
        List<UUID> removable = executions.values().stream()
                .filter(operation -> operation.status() == ExecutionStatus.EXECUTED
                        || operation.status() == ExecutionStatus.FAILED)
                .sorted(Comparator.comparing(ExecutionOperation::updatedAt))
                .limit(Math.max(1L, executions.size() - HARD_MAXIMUM_EXECUTIONS + 1L))
                .map(ExecutionOperation::id)
                .toList();
        removable.forEach(executions::remove);
        if (!removable.isEmpty()) {
            changed();
        }
    }

    private static ControlRecord replaceRecordState(ControlRecord current, RecordState replacement) {
        return new ControlRecord(
                current.id(),
                current.featureId(),
                current.ownerId(),
                current.subjectId(),
                current.title(),
                current.details(),
                replacement,
                current.createdAt(),
                Instant.now(),
                current.expiresAt(),
                Math.addExact(current.revision(), 1L),
                current.metadata());
    }

    private static ExecutionOperation replaceExecution(
            ExecutionOperation current,
            ExecutionStatus status,
            String detail
    ) {
        return new ExecutionOperation(
                current.id(),
                current.idempotencyKey(),
                current.recordId(),
                current.featureId(),
                current.actorId(),
                current.recordRevision(),
                current.destination(),
                status,
                current.createdAt(),
                Instant.now(),
                bounded(detail, 512, true));
    }

    private void writable() {
        if (path == null
                || state == RepositoryState.RECOVERY
                || state == RepositoryState.UNSUPPORTED
                || state == RepositoryState.ERROR
                || state == RepositoryState.CLOSED) {
            throw new IllegalStateException("server control repository is not writable");
        }
    }

    private void clear() {
        records.clear();
        history.clear();
        executions.clear();
        revision = 1L;
        dirty = false;
    }

    private static Map<String, String> boundedMetadata(Map<String, String> metadata) {
        Map<String, String> source = Objects.requireNonNullElse(metadata, Map.of());
        if (source.size() > 64) {
            throw new IllegalArgumentException("server control metadata limit reached");
        }
        Map<String, String> result = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            String normalized = Objects.requireNonNull(key, "metadata key")
                    .trim()
                    .toLowerCase(Locale.ROOT);
            if (!normalized.matches("[a-z0-9][a-z0-9_.-]{0,63}")) {
                throw new IllegalArgumentException("server control metadata key is invalid");
            }
            result.put(
                    normalized,
                    bounded(value, ServerControlSchemaRegistry.MAXIMUM_VALUE_LENGTH, true));
        });
        return Map.copyOf(result);
    }

    private static Map<String, String> validatedMetadata(
            String featureId,
            Map<String, String> metadata
    ) {
        Map<String, String> result = boundedMetadata(metadata);
        ServerControlSchemaRegistry.FeatureSchema schema =
                ServerControlSchemaRegistry.require(featureId);
        for (Map.Entry<String, String> entry : result.entrySet()) {
            if (entry.getKey().startsWith("field.")) {
                schema.field(entry.getKey().substring("field.".length())).validate(entry.getValue());
            }
        }
        return result;
    }

    private static String bounded(String value, int maximum, boolean allowBlank) {
        String result = Objects.requireNonNullElse(value, "").strip();
        if ((!allowBlank && result.isBlank())
                || result.length() > maximum
                || result.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("server control text is outside bounds");
        }
        return result;
    }

    private static void validate(ControlRecord record) {
        Objects.requireNonNull(record, "record");
        ServerControlCatalog.require(record.featureId());
        Objects.requireNonNull(record.id(), "record id");
        Objects.requireNonNull(record.ownerId(), "record owner");
        bounded(record.title(), 128, false);
        bounded(record.details(), 4096, true);
        Objects.requireNonNull(record.state(), "record state");
        Objects.requireNonNull(record.createdAt(), "record created time");
        Objects.requireNonNull(record.updatedAt(), "record updated time");
        validatedMetadata(record.featureId(), record.metadata());
        if (record.revision() < 1L
                || record.updatedAt().isBefore(record.createdAt())
                || record.expiresAt() != null && !record.expiresAt().isAfter(record.createdAt())) {
            throw new IllegalArgumentException("server control record is invalid");
        }
    }

    private static void validate(HistoryEntry entry) {
        Objects.requireNonNull(entry, "history entry");
        Objects.requireNonNull(entry.id(), "history id");
        Objects.requireNonNull(entry.recordId(), "history record");
        ServerControlCatalog.require(entry.featureId());
        Objects.requireNonNull(entry.actorId(), "history actor");
        Objects.requireNonNull(entry.before(), "history before state");
        Objects.requireNonNull(entry.after(), "history after state");
        bounded(entry.note(), 512, true);
        Objects.requireNonNull(entry.occurredAt(), "history time");
        if (entry.recordRevision() < 1L) {
            throw new IllegalArgumentException("server control history revision is invalid");
        }
    }

    private static void validate(ExecutionOperation operation) {
        Objects.requireNonNull(operation, "execution operation");
        Objects.requireNonNull(operation.id(), "execution id");
        if (!("sef:control:" + operation.id()).equals(operation.idempotencyKey())) {
            throw new IllegalArgumentException("server control idempotency key is invalid");
        }
        Objects.requireNonNull(operation.recordId(), "execution record");
        ServerControlCatalog.require(operation.featureId());
        Objects.requireNonNull(operation.actorId(), "execution actor");
        Objects.requireNonNull(operation.destination(), "execution destination");
        Objects.requireNonNull(operation.status(), "execution status");
        Objects.requireNonNull(operation.createdAt(), "execution created time");
        Objects.requireNonNull(operation.updatedAt(), "execution updated time");
        bounded(operation.detail(), 512, true);
        if (operation.recordRevision() < 1L
                || operation.updatedAt().isBefore(operation.createdAt())) {
            throw new IllegalArgumentException("server control execution is invalid");
        }
    }

    private static boolean allowedTransition(RecordState before, RecordState after) {
        if (before == after) {
            return true;
        }
        return switch (before) {
            case OPEN -> Set.of(
                    RecordState.ACTIVE,
                    RecordState.APPROVED,
                    RecordState.DENIED,
                    RecordState.RESOLVED,
                    RecordState.CANCELLED,
                    RecordState.ARCHIVED).contains(after);
            case ACTIVE -> Set.of(
                    RecordState.PAUSED,
                    RecordState.RESOLVED,
                    RecordState.CANCELLED,
                    RecordState.ARCHIVED).contains(after);
            case PAUSED -> Set.of(
                    RecordState.ACTIVE,
                    RecordState.RESOLVED,
                    RecordState.CANCELLED,
                    RecordState.ARCHIVED).contains(after);
            case APPROVED -> Set.of(
                    RecordState.ACTIVE,
                    RecordState.RESOLVED,
                    RecordState.CANCELLED,
                    RecordState.ARCHIVED).contains(after);
            case DENIED, RESOLVED, CANCELLED, EXPIRED -> after == RecordState.ARCHIVED
                    || after == RecordState.OPEN;
            case ARCHIVED -> after == RecordState.OPEN;
        };
    }

    private static void audit(
            UUID actorId,
            ControlRecord record,
            String operation,
            AuditService.Result result,
            ActionResult.ReasonCode reason
    ) {
        AuditService.record(AuditService.Event.metadata(
                UUID.randomUUID(),
                actorId,
                actorId.equals(new UUID(0L, 0L)) ? "console" : actorId.toString(),
                actorId.equals(new UUID(0L, 0L)) ? "console" : "player",
                "sef:control." + record.featureId() + "." + operation,
                record.subjectId() == null ? List.of() : List.of(record.subjectId()),
                result,
                reason,
                "server_control",
                ServerControlCatalog.require(record.featureId()).dangerous()
                        ? AuditService.AuditClass.DESTRUCTIVE
                        : AuditService.AuditClass.ADMIN_ACTION));
    }

    public enum RecordState {
        OPEN,
        ACTIVE,
        PAUSED,
        APPROVED,
        DENIED,
        RESOLVED,
        CANCELLED,
        ARCHIVED,
        EXPIRED
    }

    public record ControlRecord(
            UUID id,
            String featureId,
            UUID ownerId,
            UUID subjectId,
            String title,
            String details,
            RecordState state,
            Instant createdAt,
            Instant updatedAt,
            Instant expiresAt,
            long revision,
            Map<String, String> metadata
    ) {
        public ControlRecord {
            metadata = Map.copyOf(Objects.requireNonNullElse(metadata, Map.of()));
        }
    }

    public record HistoryEntry(
            UUID id,
            UUID recordId,
            String featureId,
            UUID actorId,
            RecordState before,
            RecordState after,
            String note,
            Instant occurredAt,
            long recordRevision
    ) {
    }

    public enum ExecutionStatus {
        PREPARED,
        EXECUTING,
        EXECUTED,
        FAILED,
        OUTCOME_UNKNOWN
    }

    public record ExecutionOperation(
            UUID id,
            String idempotencyKey,
            UUID recordId,
            String featureId,
            UUID actorId,
            long recordRevision,
            RecordState destination,
            ExecutionStatus status,
            Instant createdAt,
            Instant updatedAt,
            String detail
    ) {
    }

    private record Snapshot(
            long revision,
            List<ControlRecord> records,
            List<HistoryEntry> history,
            List<ExecutionOperation> executions
    ) {
        private Snapshot {
            records = records == null ? List.of() : List.copyOf(records);
            history = history == null ? List.of() : List.copyOf(history);
            executions = executions == null ? List.of() : List.copyOf(executions);
        }
    }

    public record Diagnostic(
            int records,
            int historyEntries,
            long activeRecords,
            int executions,
            long incompleteExecutions,
            long revision,
            RepositoryState state,
            boolean dirty
    ) {
    }
}
