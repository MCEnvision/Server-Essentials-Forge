package com.enviouse.sef.kernel.observation;

import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.command.CommandDefinition;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class ObservationContracts {
    private ObservationContracts() {
    }

    public record ObservationEvent(
            UUID eventId,
            UUID parentId,
            Instant timestamp,
            EventType type,
            LifecycleStage stage,
            UUID initiatorId,
            UUID effectiveActorId,
            CommandDefinition.SourceType sourceType,
            UUID targetId,
            String actionId,
            String entryRoute,
            String worldId,
            RedactionClass redactionClass,
            Map<String, String> fields
    ) {
        public ObservationEvent {
            Objects.requireNonNull(eventId, "eventId");
            Objects.requireNonNull(timestamp, "timestamp");
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(stage, "stage");
            Objects.requireNonNull(sourceType, "sourceType");
            actionId = bounded(actionId, 128);
            entryRoute = bounded(entryRoute, 128);
            worldId = bounded(worldId, 128);
            Objects.requireNonNull(redactionClass, "redactionClass");
            fields = boundedFields(fields);
        }
    }

    public record ObserverDecision(
            boolean allowed,
            UUID observerId,
            Set<FieldClass> visibleFields,
            ActionResult.ReasonCode reason,
            long policyRevision
    ) {
        public ObserverDecision {
            visibleFields = Set.copyOf(Objects.requireNonNull(visibleFields, "visibleFields"));
            Objects.requireNonNull(reason, "reason");
            if (policyRevision < 0) {
                throw new IllegalArgumentException("Policy revision cannot be negative");
            }
        }
    }

    public interface ObservationSink {
        String id();

        boolean enabled();

        boolean submit(ObservationEvent event, ObserverDecision decision);

        default void flush() {
        }
    }

    public enum EventType {
        PRIVATE_MESSAGE,
        COMMAND
    }

    public enum LifecycleStage {
        RECEIVED,
        PARSED,
        REJECTED,
        AUTHORIZED,
        STARTED,
        COMPLETED,
        FAILED,
        CANCELLED,
        OUTCOME_UNKNOWN
    }

    public enum RedactionClass {
        NONE,
        METADATA_ONLY,
        PRIVATE_CONTENT,
        SECRET_ARGUMENTS,
        NETWORK_ADDRESS
    }

    public enum FieldClass {
        IDENTITY,
        CONTENT,
        LOCATION,
        RESULT,
        SOURCE,
        TARGET,
        ROUTE
    }

    public static AuditService.Result auditResult(LifecycleStage stage) {
        return switch (stage) {
            case COMPLETED -> AuditService.Result.SUCCESS;
            case REJECTED -> AuditService.Result.REJECTED;
            case FAILED -> AuditService.Result.FAILED;
            case CANCELLED -> AuditService.Result.CANCELLED;
            default -> AuditService.Result.OUTCOME_UNKNOWN;
        };
    }

    private static Map<String, String> boundedFields(Map<String, String> fields) {
        Objects.requireNonNull(fields, "fields");
        if (fields.size() > 32) {
            throw new IllegalArgumentException("Observation field count exceeds limit");
        }
        Map<String, String> bounded = new java.util.LinkedHashMap<>();
        fields.forEach((key, value) -> {
            if (bounded.putIfAbsent(bounded(key, 64), bounded(value, 512)) != null) {
                throw new IllegalArgumentException("Duplicate bounded observation field");
            }
        });
        return Map.copyOf(bounded);
    }

    private static String bounded(String value, int maximumLength) {
        if (value == null) {
            return "";
        }
        String sanitized = value.replace("\r", "\\r").replace("\n", "\\n").trim();
        return sanitized.length() <= maximumLength ? sanitized : sanitized.substring(0, maximumLength);
    }
}
