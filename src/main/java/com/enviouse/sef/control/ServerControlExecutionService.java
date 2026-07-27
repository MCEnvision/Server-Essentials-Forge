package com.enviouse.sef.control;

import com.enviouse.sef.kernel.ActionResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class ServerControlExecutionService {
    public static final int MAXIMUM_HANDLERS = 128;

    private final ServerControlRepository repository;
    private final Map<String, Handler> handlers = new LinkedHashMap<>();

    public ServerControlExecutionService(ServerControlRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public synchronized void register(String featureId, Handler handler) {
        String normalized = ServerControlCatalog.require(featureId).id();
        Objects.requireNonNull(handler, "handler");
        if (handlers.size() >= MAXIMUM_HANDLERS || handlers.putIfAbsent(normalized, handler) != null) {
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
                        : "execution handler is unavailable");
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
        ActionResult<String> handled;
        if (handler == null) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.PROVIDER_ERROR,
                    "required server control execution handler is unavailable");
        } else {
            try {
                handled = Objects.requireNonNull(handler.execute(record, context), "handler result");
            } catch (RuntimeException | LinkageError exception) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.PROVIDER_ERROR,
                        "server control execution handler failed");
            }
        }
        if (!handled.successful()) {
            return ActionResult.failure(handled.reason(), handled.detail());
        }
        ServerControlRepository.RecordState destination = destination(record, schema);
        ActionResult<ServerControlRepository.ControlRecord> transitioned = repository.transition(
                record.id(),
                actorId,
                destination,
                expectedRevision,
                "executed");
        if (!transitioned.successful()) {
            return ActionResult.failure(transitioned.reason(), transitioned.detail());
        }
        return ActionResult.success(new Execution(
                record.id(),
                record.featureId(),
                transitioned.value().revision(),
                destination,
                handled.value(),
                schema.reversible()));
    }

    public Diagnostic diagnostic() {
        List<String> unavailable = ServerControlSchemaRegistry.schemas().stream()
                .map(ServerControlSchemaRegistry.FeatureSchema::featureId)
                .filter(feature -> !registered(feature))
                .sorted()
                .toList();
        return new Diagnostic(
                ServerControlSchemaRegistry.schemas().size(),
                registeredFeatures(),
                unavailable);
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

    public interface ExecutionContext {
        Object server();

        Object source();
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
