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
        return SecurityAuditService.record(SecurityAuditService.AuditEvent.from(event));
    }

    public static boolean accepting(AuditClass auditClass) {
        Objects.requireNonNull(auditClass, "auditClass");
        SecurityAuditService.Health health = SecurityAuditService.health();
        return auditClass == AuditClass.NONE || (health.running() && health.writerAlive());
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
            UUID parentJobId,
            UUID stepCorrelationId,
            long definitionRevision,
            long policyRevision,
            Map<String, String> providerContext,
            RedactionClass redactionClass,
            List<String> appliedRedactionRuleIds,
            UUID observerId,
            String previousEventHash,
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
            providerContext = boundedMap(providerContext);
            Objects.requireNonNull(redactionClass, "redactionClass");
            appliedRedactionRuleIds = boundedList(appliedRedactionRuleIds, 32, 128);
            previousEventHash = normalized(previousEventHash, 128);
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
                    null,
                    0L,
                    0L,
                    Map.of(),
                    RedactionClass.METADATA,
                    List.of(),
                    null,
                    "",
                    auditClass);
        }

        public static Event completion(
                UUID sessionId,
                UUID actorId,
                String actorName,
                String sourceType,
                String actionId,
                Map<String, String> parameters,
                Result result,
                ActionResult.ReasonCode reason,
                String origin,
                UUID parentJobId,
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
                    List.of(),
                    parameters,
                    result,
                    reason,
                    0L,
                    origin,
                    parentJobId,
                    UUID.randomUUID(),
                    0L,
                    0L,
                    Map.of("completion", "async"),
                    RedactionClass.METADATA,
                    List.of(),
                    null,
                    "",
                    auditClass);
        }

        public static Event interaction(
                UUID sessionId,
                UUID actorId,
                String actorName,
                String sourceType,
                String actionId,
                List<UUID> targetIds,
                Map<String, String> parameters,
                Result result,
                ActionResult.ReasonCode reason,
                String origin,
                RedactionClass redactionClass,
                AuditClass auditClass
        ) {
            return interaction(
                    sessionId,
                    actorId,
                    actorName,
                    sourceType,
                    actionId,
                    targetIds,
                    parameters,
                    result,
                    reason,
                    origin,
                    null,
                    redactionClass,
                    auditClass);
        }

        public static Event interaction(
                UUID sessionId,
                UUID actorId,
                String actorName,
                String sourceType,
                String actionId,
                List<UUID> targetIds,
                Map<String, String> parameters,
                Result result,
                ActionResult.ReasonCode reason,
                String origin,
                UUID parentJobId,
                RedactionClass redactionClass,
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
                    targetIds,
                    parameters,
                    result,
                    reason,
                    0L,
                    origin,
                    parentJobId,
                    null,
                    0L,
                    0L,
                    Map.of(),
                    redactionClass,
                    List.of(),
                    null,
                    "",
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
        source.forEach((key, value) -> {
            String normalizedKey = normalized(key, 64);
            if (result.putIfAbsent(normalizedKey, bounded(value, 256)) != null) {
                throw new IllegalArgumentException("Audit map keys collide after normalization");
            }
        });
        return Map.copyOf(result);
    }

    private static List<String> boundedList(List<String> source, int maximumSize, int maximumLength) {
        Objects.requireNonNull(source, "source");
        if (source.size() > maximumSize) {
            throw new IllegalArgumentException("Audit list exceeds limit");
        }
        return source.stream()
                .map(value -> normalized(value, maximumLength))
                .toList();
    }

    private static String normalized(String value, int maximumLength) {
        return bounded(value, maximumLength).toLowerCase(Locale.ROOT);
    }

    private static String bounded(String value, int maximumLength) {
        if (value == null) {
            return "";
        }
        StringBuilder sanitized = new StringBuilder(Math.min(value.length(), maximumLength));
        boolean previousWhitespace = false;
        for (int offset = 0; offset < value.length();) {
            int codePoint = value.codePointAt(offset);
            offset += Character.charCount(codePoint);
            boolean whitespace = Character.isWhitespace(codePoint)
                    || Character.isISOControl(codePoint)
                    || Character.getType(codePoint) == Character.FORMAT;
            if (whitespace) {
                if (!previousWhitespace && sanitized.length() < maximumLength) {
                    sanitized.append(' ');
                }
                previousWhitespace = true;
                continue;
            }
            if (sanitized.length() + Character.charCount(codePoint) > maximumLength) {
                break;
            }
            sanitized.appendCodePoint(codePoint);
            previousWhitespace = false;
        }
        return sanitized.toString().trim();
    }
}
