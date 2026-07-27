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
    public static final int SCHEMA_VERSION = 1;
    public static final int HARD_MAXIMUM_RECORDS = 100_000;
    public static final int HARD_MAXIMUM_HISTORY = 200_000;
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantJsonAdapter())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private final Map<UUID, ControlRecord> records = new LinkedHashMap<>();
    private final List<HistoryEntry> history = new ArrayList<>();
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
        var definition = ServerControlCatalog.require(current.featureId());
        if (!definition.states().contains(replacement)) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "record state is unavailable for this feature");
        }
        if (!allowedTransition(current.state(), replacement)) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "server control state transition is invalid");
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

    public synchronized Diagnostic diagnostic() {
        prune(Instant.now());
        long active = records.values().stream()
                .filter(record -> record.state() == RecordState.ACTIVE || record.state() == RecordState.OPEN)
                .count();
        return new Diagnostic(
                records.size(),
                history.size(),
                active,
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
            state = Files.exists(path) ? RepositoryState.RECOVERY : RepositoryState.MISSING;
            return new LoadResult(state, state == RepositoryState.MISSING
                    ? "new server control repository"
                    : "server control storage unavailable");
        }
        try {
            Snapshot snapshot = GSON.fromJson(document.data(), Snapshot.class);
            if (snapshot == null
                    || snapshot.revision() < 1L
                    || snapshot.records().size() > HARD_MAXIMUM_RECORDS
                    || snapshot.history().size() > HARD_MAXIMUM_HISTORY) {
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
            revision = snapshot.revision();
            prune(Instant.now());
            state = RepositoryState.READY;
            dirty = document.migrated();
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
                List.copyOf(history));
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

    private record Snapshot(
            long revision,
            List<ControlRecord> records,
            List<HistoryEntry> history
    ) {
        private Snapshot {
            records = records == null ? List.of() : List.copyOf(records);
            history = history == null ? List.of() : List.copyOf(history);
        }
    }

    public record Diagnostic(
            int records,
            int historyEntries,
            long activeRecords,
            long revision,
            RepositoryState state,
            boolean dirty
    ) {
    }
}
