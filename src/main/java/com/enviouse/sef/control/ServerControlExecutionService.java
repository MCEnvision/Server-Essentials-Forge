package com.enviouse.sef.control;

import com.enviouse.sef.kernel.ActionResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class ServerControlExecutionService {
    public static final int MAXIMUM_HANDLERS = 128;
    private static final System.Logger LOGGER = System.getLogger(ServerControlExecutionService.class.getName());

    private final ServerControlRepository repository;
    private final DurableCommit durableCommit;
    private final Map<String, Handler> handlers = new LinkedHashMap<>();
    private final Map<String, String> unavailableHandlers = new LinkedHashMap<>();

    public ServerControlExecutionService(ServerControlRepository repository) {
        this(repository, repository::flush);
    }

    ServerControlExecutionService(
            ServerControlRepository repository,
            DurableCommit durableCommit
    ) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.durableCommit = Objects.requireNonNull(durableCommit, "durableCommit");
    }

    public synchronized void register(String featureId, Handler handler) {
        String normalized = ServerControlCatalog.require(featureId).id();
        Objects.requireNonNull(handler, "handler");
        if (handlers.size() + unavailableHandlers.size() >= MAXIMUM_HANDLERS
                || unavailableHandlers.containsKey(normalized)
                || handlers.putIfAbsent(normalized, handler) != null) {
            throw new IllegalStateException("server control execution handler is unavailable");
        }
    }

    public synchronized void registerUnavailable(String featureId, String detail) {
        String normalized = ServerControlCatalog.require(featureId).id();
        String normalizedDetail = Objects.requireNonNullElse(detail, "").strip();
        if (normalizedDetail.isBlank() || normalizedDetail.length() > 512) {
            throw new IllegalArgumentException("server control unavailability detail is outside bounds");
        }
        if (handlers.size() + unavailableHandlers.size() >= MAXIMUM_HANDLERS
                || handlers.containsKey(normalized)
                || unavailableHandlers.putIfAbsent(normalized, normalizedDetail) != null) {
            throw new IllegalStateException("server control execution handler is unavailable");
        }
    }

    public synchronized boolean registered(String featureId) {
        return handlers.containsKey(ServerControlCatalog.require(featureId).id());
    }

    public synchronized List<String> registeredFeatures() {
        return handlers.keySet().stream().sorted().toList();
    }

    public Preview preview(UUID recordId, long expectedRevision) {
        ServerControlRepository.ControlRecord record = repository.find(recordId).orElse(null);
        if (record == null) {
            return Preview.rejected("server control record not found");
        }
        if (record.revision() != expectedRevision) {
            return Preview.rejected("server control record revision changed");
        }
        ServerControlSchemaRegistry.FeatureSchema schema =
                ServerControlSchemaRegistry.require(record.featureId());
        List<String> missing = schema.missing(record.metadata());
        boolean handlerAvailable = registered(record.featureId());
        String unavailableDetail;
        synchronized (this) {
            unavailableDetail = unavailableHandlers.get(record.featureId());
        }
        List<String> effects = new ArrayList<>();
        effects.add("feature " + record.featureId());
        effects.add("runtime " + schema.runtimeClass().name().toLowerCase(Locale.ROOT));
        effects.add("state " + record.state().name().toLowerCase(Locale.ROOT));
        effects.add("subject " + (record.subjectId() == null ? "server" : "one player"));
        effects.add("configured fields " + record.metadata().keySet().stream()
                .filter(key -> key.startsWith("field."))
                .count());
        if (schema.hud() != ServerControlSchemaRegistry.HudPolicy.NONE) {
            effects.add("hud " + schema.hud().name().toLowerCase(Locale.ROOT));
        }
        effects.add("handler " + (handlerAvailable ? "available" : "unavailable"));
        boolean ready = missing.isEmpty() && handlerAvailable;
        return new Preview(
                ready,
                record.id(),
                record.featureId(),
                record.revision(),
                schema.runtimeClass(),
                schema.confirmationRequired(),
                schema.reversible(),
                List.copyOf(missing),
                List.copyOf(effects),
                !missing.isEmpty()
                        ? "required fields are missing"
                        : handlerAvailable
                        ? "ready"
                        : Objects.requireNonNullElse(
                                unavailableDetail,
                                "execution handler is unavailable"));
    }

    public ActionResult<Execution> execute(
            UUID recordId,
            UUID actorId,
            long expectedRevision,
            boolean confirmed,
            ExecutionContext context
    ) {
        Objects.requireNonNull(recordId, "recordId");
        Objects.requireNonNull(actorId, "actorId");
        Objects.requireNonNull(context, "context");
        Preview preview = preview(recordId, expectedRevision);
        if (!preview.ready()) {
            return ActionResult.failure(
                    preview.detail().contains("revision")
                            ? ActionResult.ReasonCode.CONFLICT
                            : preview.detail().contains("not found")
                            ? ActionResult.ReasonCode.NOT_FOUND
                            : preview.detail().contains("handler")
                            || preview.detail().contains("unavailable")
                            ? ActionResult.ReasonCode.PROVIDER_ERROR
                            : ActionResult.ReasonCode.INVALID_INPUT,
                    preview.detail());
        }
        if (preview.confirmationRequired() && !confirmed) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.CONFIRMATION_REQUIRED,
                    "server control execution requires confirmation");
        }
        ServerControlRepository.ControlRecord record = repository.find(recordId).orElse(null);
        if (record == null || record.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "server control record revision changed");
        }
        ServerControlSchemaRegistry.FeatureSchema schema =
                ServerControlSchemaRegistry.require(record.featureId());
        if (!schema.operations().contains(ServerControlSchemaRegistry.Operation.EXECUTE)) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "feature does not expose execution");
        }
        if (schema.runtimeClass() == ServerControlSchemaRegistry.RuntimeClass.TRANSACTION
                && record.state() != ServerControlRepository.RecordState.APPROVED) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.POLICY_DENIED,
                    "transaction must be approved before execution");
        }
        Handler handler;
        synchronized (this) {
            handler = handlers.get(record.featureId());
        }
        if (handler == null) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.PROVIDER_ERROR,
                    "required server control execution handler is unavailable");
        }
        ServerControlRepository.RecordState destination = destination(record, schema);
        ActionResult<ServerControlRepository.ExecutionOperation> prepared = repository.prepareExecution(
                record.id(),
                actorId,
                destination,
                expectedRevision);
        if (!prepared.successful()) {
            return ActionResult.failure(prepared.reason(), prepared.detail());
        }
        ServerControlRepository.ExecutionOperation operation = prepared.value();
        if (!flush(operation, "prepare")) {
            repository.failExecution(operation.id(), "durable preparation failed");
            flush(operation, "preparation failure");
            return ActionResult.failure(
                    ActionResult.ReasonCode.PROVIDER_ERROR,
                    "server control execution could not be prepared, operation " + operation.id());
        }
        ActionResult<ServerControlRepository.ExecutionOperation> begun =
                repository.beginExecution(operation.id());
        if (!begun.successful()) {
            return ActionResult.failure(begun.reason(), begun.detail());
        }
        operation = begun.value();
        if (!flush(operation, "execution claim")) {
            repository.failExecution(operation.id(), "durable execution claim failed");
            flush(operation, "execution claim failure");
            return ActionResult.failure(
                    ActionResult.ReasonCode.PROVIDER_ERROR,
                    "server control execution could not be claimed, operation " + operation.id());
        }
        ActionResult<String> handled;
        try {
            handled = Objects.requireNonNull(
                    handler.execute(
                            record,
                            new OperationExecutionContext(
                                    context,
                                    operation.id(),
                                    operation.idempotencyKey())),
                    "handler result");
        } catch (RuntimeException | LinkageError exception) {
            LOGGER.log(
                    System.Logger.Level.ERROR,
                    "Server control handler failed for operation " + operation.id()
                            + ", feature " + record.featureId()
                            + ", record " + record.id()
                            + ", revision " + record.revision(),
                    exception);
            repository.markOutcomeUnknown(operation.id(), "handler threw before reporting an outcome");
            flush(operation, "unknown handler outcome");
            return ActionResult.failure(
                    ActionResult.ReasonCode.PROVIDER_ERROR,
                    "server control execution outcome is unknown, operation " + operation.id());
        }
        if (!handled.successful()) {
            repository.failExecution(operation.id(), handled.detail());
            if (!flush(operation, "handler failure")) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.PROVIDER_ERROR,
                        "server control failure outcome was not durably recorded, operation " + operation.id());
            }
            return ActionResult.failure(handled.reason(), handled.detail());
        }
        ActionResult<ServerControlRepository.ControlRecord> transitioned =
                repository.completeExecution(operation.id(), handled.value());
        if (!transitioned.successful()) {
            repository.markOutcomeUnknown(operation.id(), transitioned.detail());
            flush(operation, "unknown terminal outcome");
            return ActionResult.failure(transitioned.reason(), transitioned.detail());
        }
        if (!flush(operation, "terminal outcome")) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.PROVIDER_ERROR,
                    "server control effect completed but durable confirmation failed, operation " + operation.id());
        }
        return ActionResult.success(new Execution(
                operation.id(),
                record.id(),
                record.featureId(),
                transitioned.value().revision(),
                destination,
                handled.value(),
                schema.reversible()));
    }

    private boolean flush(
            ServerControlRepository.ExecutionOperation operation,
            String phase
    ) {
        try {
            durableCommit.flush();
            return true;
        } catch (IOException | RuntimeException exception) {
            LOGGER.log(
                    System.Logger.Level.ERROR,
                    "Server control persistence failed during " + phase
                            + " for operation " + operation.id()
                            + ", feature " + operation.featureId()
                            + ", record " + operation.recordId()
                            + ", revision " + operation.recordRevision(),
                    exception);
            return false;
        }
    }

    public Diagnostic diagnostic() {
        Set<String> unavailable = new LinkedHashSet<>();
        synchronized (this) {
            unavailable.addAll(unavailableHandlers.keySet());
        }
        ServerControlSchemaRegistry.schemas().stream()
                .map(ServerControlSchemaRegistry.FeatureSchema::featureId)
                .filter(feature -> !registered(feature))
                .forEach(unavailable::add);
        return new Diagnostic(
                ServerControlSchemaRegistry.schemas().size(),
                registeredFeatures(),
                unavailable.stream().sorted().toList());
    }

    private static ServerControlRepository.RecordState destination(
            ServerControlRepository.ControlRecord record,
            ServerControlSchemaRegistry.FeatureSchema schema
    ) {
        if (Set.of(
                "playtime_rewards",
                "daily_rewards",
                "weekly_rewards",
                "invites",
                "polls",
                "knowledge").contains(record.featureId())) {
            return ServerControlRepository.RecordState.ACTIVE;
        }
        return switch (schema.runtimeClass()) {
            case DIAGNOSTIC, TRANSACTION -> ServerControlRepository.RecordState.RESOLVED;
            case LIVE_POLICY, SCHEDULED_JOB, INTEGRATION -> ServerControlRepository.RecordState.ACTIVE;
            case REVIEW_QUEUE -> ServerControlRepository.RecordState.RESOLVED;
        };
    }

    @FunctionalInterface
    public interface Handler {
        ActionResult<String> execute(
                ServerControlRepository.ControlRecord record,
                ExecutionContext context
        );
    }

    @FunctionalInterface
    interface DurableCommit {
        void flush() throws IOException;
    }

    public interface ExecutionContext {
        Object server();

        Object source();

        default UUID operationId() {
            throw new IllegalStateException("server control operation is not bound");
        }

        default String idempotencyKey() {
            throw new IllegalStateException("server control operation is not bound");
        }
    }

    private record OperationExecutionContext(
            ExecutionContext delegate,
            UUID operationId,
            String idempotencyKey
    ) implements ExecutionContext {
        private OperationExecutionContext {
            Objects.requireNonNull(delegate, "delegate");
            Objects.requireNonNull(operationId, "operationId");
            Objects.requireNonNull(idempotencyKey, "idempotencyKey");
        }

        @Override
        public Object server() {
            return delegate.server();
        }

        @Override
        public Object source() {
            return delegate.source();
        }
    }

    public record Preview(
            boolean ready,
            UUID recordId,
            String featureId,
            long revision,
            ServerControlSchemaRegistry.RuntimeClass runtimeClass,
            boolean confirmationRequired,
            boolean reversible,
            List<String> missingFields,
            List<String> effects,
            String detail
    ) {
        public Preview {
            featureId = Objects.requireNonNullElse(featureId, "");
            missingFields = List.copyOf(missingFields);
            effects = List.copyOf(effects);
            detail = Objects.requireNonNullElse(detail, "");
        }

        private static Preview rejected(String detail) {
            return new Preview(
                    false,
                    null,
                    "",
                    0L,
                    ServerControlSchemaRegistry.RuntimeClass.DIAGNOSTIC,
                    false,
                    false,
                    List.of(),
                    List.of(),
                    detail);
        }
    }

    public record Execution(
            UUID operationId,
            UUID recordId,
            String featureId,
            long revision,
            ServerControlRepository.RecordState state,
            String detail,
            boolean reversible
    ) {
    }

    public record Diagnostic(
            int schemas,
            List<String> registeredHandlers,
            List<String> unavailableIntegrations
    ) {
        public Diagnostic {
            registeredHandlers = List.copyOf(registeredHandlers);
            unavailableIntegrations = List.copyOf(unavailableIntegrations);
        }
    }
}
