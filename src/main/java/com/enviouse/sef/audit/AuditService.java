package com.enviouse.sef.audit;

import com.enviouse.sef.kernel.ActionResult;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class AuditService {
    private AuditService() {
    }

    public static boolean record(Event event) {
        Objects.requireNonNull(event, "event");
        if (event.auditClass() == AuditClass.NONE) {
            return true;
        }
        return SecurityAuditService.record(SecurityAuditService.AuditEvent.create(
                event.auditClass().name().toLowerCase(Locale.ROOT),
                event.actionId(),
                event.actorName().isBlank() ? value(event.actorId()) : event.actorName(),
                event.targetIds().stream().map(UUID::toString).limit(16).reduce((left, right) -> left + "," + right).orElse(""),
                event.origin(),
                event.result().name().toLowerCase(Locale.ROOT),
                event.reason().name().toLowerCase(Locale.ROOT)));
    }

    public record Event(
            int schemaVersion,
            UUID eventId,
            Instant timestamp,
            UUID sessionId,
            UUID actorId,
            String actorName,
            String sourceType,
            String actionId,
            List<UUID> targetIds,
            Map<String, String> normalizedParameters,
            Result result,
            ActionResult.ReasonCode reason,
            long durationMillis,
            String origin,
            UUID parentId,
            long definitionRevision,
            long policyRevision,
            RedactionClass redactionClass,
            AuditClass auditClass
    ) {
        public Event {
            if (schemaVersion != 1) {
                throw new IllegalArgumentException("Unsupported audit schema version");
            }
            Objects.requireNonNull(eventId, "eventId");
            Objects.requireNonNull(timestamp, "timestamp");
            Objects.requireNonNull(sessionId, "sessionId");
            actorName = bounded(actorName, 64);
            sourceType = normalized(sourceType, 64);
            actionId = normalized(actionId, 128);
            Objects.requireNonNull(targetIds, "targetIds");
            if (targetIds.size() > 100) {
                throw new IllegalArgumentException("Audit target count exceeds limit");
            }
            targetIds = List.copyOf(targetIds);
            normalizedParameters = boundedMap(normalizedParameters);
            Objects.requireNonNull(result, "result");
            Objects.requireNonNull(reason, "reason");
            origin = normalized(origin, 64);
            Objects.requireNonNull(redactionClass, "redactionClass");
            Objects.requireNonNull(auditClass, "auditClass");
            if (durationMillis < 0) {
                throw new IllegalArgumentException("Audit duration cannot be negative");
            }
            if (definitionRevision < 0 || policyRevision < 0) {
                throw new IllegalArgumentException("Audit revisions cannot be negative");
            }
        }

        public static Event metadata(
                UUID sessionId,
                UUID actorId,
                String actorName,
                String sourceType,
                String actionId,
                List<UUID> targets,
                Result result,
                ActionResult.ReasonCode reason,
                String origin,
                AuditClass auditClass
        ) {
            return new Event(
                    1,
                    UUID.randomUUID(),
                    Instant.now(),
                    sessionId,
                    actorId,
                    actorName,
                    sourceType,
                    actionId,
                    targets,
                    Map.of(),
                    result,
                    reason,
                    0L,
                    origin,
                    null,
                    0L,
                    0L,
                    RedactionClass.METADATA,
                    auditClass);
        }
    }

    public enum AuditClass {
        NONE,
        METADATA_ONLY,
        ADMIN_ACTION,
        SENSITIVE_ACCESS,
        DESTRUCTIVE,
        DELEGATED_EXECUTION,
        WORKFLOW_EXECUTION,
        CONFIG_DEFINITION,
        NETWORK_ADDRESS_ACTION,
        PRIVATE_MESSAGE_OBSERVATION,
        COMMAND_OBSERVATION,
        FILE_LOG_CONTROL,
        ECONOMY_TRANSACTION
    }

    public enum Result {
        SUCCESS,
        REJECTED,
        FAILED,
        CANCELLED,
        OUTCOME_UNKNOWN
    }

    public enum RedactionClass {
        NONE,
        METADATA,
        SECRET_ARGUMENTS,
        PRIVATE_CONTENT,
        NETWORK_ADDRESS,
        ITEM_METADATA
    }

    private static Map<String, String> boundedMap(Map<String, String> source) {
        Objects.requireNonNull(source, "source");
        if (source.size() > 32) {
            throw new IllegalArgumentException("Audit parameter count exceeds limit");
        }
        Map<String, String> result = new java.util.LinkedHashMap<>();
        source.forEach((key, value) -> result.put(normalized(key, 64), bounded(value, 256)));
        return Map.copyOf(result);
    }

    private static String normalized(String value, int maximumLength) {
        return bounded(value, maximumLength).toLowerCase(Locale.ROOT);
    }

    private static String bounded(String value, int maximumLength) {
        if (value == null) {
            return "";
        }
        String sanitized = value.codePoints()
                .filter(codePoint -> !Character.isISOControl(codePoint))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString()
                .trim();
        return sanitized.length() <= maximumLength ? sanitized : sanitized.substring(0, maximumLength);
    }

    private static String value(UUID id) {
        return id == null ? "" : id.toString();
    }
}
