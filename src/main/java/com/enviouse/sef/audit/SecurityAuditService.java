package com.enviouse.sef.audit;

import com.enviouse.sef.ServerEssentialsForge;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class SecurityAuditService {
    public record AuditEvent(
            int schemaVersion,
            String eventId,
            String timestamp,
            String serverSessionId,
            String actorUuid,
            String actorUsername,
            String sourceType,
            String actionId,
            List<String> targetUuids,
            Map<String, String> normalizedParameters,
            String result,
            String reasonCode,
            long durationMillis,
            String origin,
            String parentJobId,
            String stepCorrelationId,
            long definitionRevision,
            long policyRevision,
            Map<String, String> providerContext,
            String redactionClass,
            List<String> appliedRedactionRuleIds,
            String observerUuid,
            String previousEventHash,
            String auditClass
    ) {
        public AuditEvent {
            if (schemaVersion != 1) {
                throw new IllegalArgumentException("Unsupported security audit schema version");
            }
            eventId = uuid(eventId, "event id");
            timestamp = instant(timestamp);
            serverSessionId = uuid(serverSessionId, "server session id");
            actorUuid = optionalUuid(actorUuid, "actor id");
            actorUsername = bounded(actorUsername, 64);
            sourceType = normalized(sourceType, 64);
            actionId = normalized(actionId, 128);
            targetUuids = boundedUuidList(targetUuids, 100);
            normalizedParameters = boundedMap(normalizedParameters);
            result = normalized(result, 64);
            reasonCode = normalized(reasonCode, 128);
            if (durationMillis < 0L) {
                throw new IllegalArgumentException("Audit duration cannot be negative");
            }
            origin = normalized(origin, 64);
            parentJobId = optionalUuid(parentJobId, "parent job id");
            stepCorrelationId = optionalUuid(stepCorrelationId, "step correlation id");
            if (definitionRevision < 0L || policyRevision < 0L) {
                throw new IllegalArgumentException("Audit revisions cannot be negative");
            }
            providerContext = boundedMap(providerContext);
            redactionClass = normalized(redactionClass, 64);
            appliedRedactionRuleIds = boundedList(appliedRedactionRuleIds, 32, 128);
            observerUuid = optionalUuid(observerUuid, "observer id");
            previousEventHash = normalized(previousEventHash, 128);
            auditClass = normalized(auditClass, 64);
        }

        public static AuditEvent from(AuditService.Event event) {
            Objects.requireNonNull(event, "event");
            return new AuditEvent(
                    event.schemaVersion(),
                    event.eventId().toString(),
                    event.timestamp().toString(),
                    event.sessionId().toString(),
                    value(event.actorId()),
                    event.actorName(),
                    event.sourceType(),
                    event.actionId(),
                    event.targetIds().stream().map(UUID::toString).toList(),
                    event.normalizedParameters(),
                    event.result().name(),
                    event.reason().name(),
                    event.durationMillis(),
                    event.origin(),
                    value(event.parentJobId()),
                    value(event.stepCorrelationId()),
                    event.definitionRevision(),
                    event.policyRevision(),
                    event.providerContext(),
                    event.redactionClass().name(),
                    event.appliedRedactionRuleIds(),
                    value(event.observerId()),
                    event.previousEventHash(),
                    event.auditClass().name());
        }

        public static AuditEvent create(
                String category,
                String action,
                String issuer,
                String target,
                String commandRoot,
                String result,
                String reason
        ) {
            Map<String, String> parameters = target == null || target.isBlank()
                    ? Map.of()
                    : Map.of("target", bounded(target, 256));
            return new AuditEvent(
                    1,
                    UUID.randomUUID().toString(),
                    Instant.now().toString(),
                    sessionId.toString(),
                    "",
                    issuer,
                    "server",
                    action,
                    List.of(),
                    parameters,
                    result,
                    reason,
                    0L,
                    commandRoot,
                    "",
                    "",
                    0L,
                    0L,
                    Map.of("legacy_category", bounded(category, 64)),
                    AuditService.RedactionClass.METADATA.name(),
                    List.of(),
                    "",
                    "",
                    category);
        }

        private static String bounded(String value, int maximumLength) {
            if (value == null) {
                return "";
            }
            String sanitized = value.codePoints()
                    .filter(codePoint -> !Character.isISOControl(codePoint))
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
            return sanitized.length() <= maximumLength
                    ? sanitized
                    : sanitized.substring(0, maximumLength);
        }

        private static String normalized(String value, int maximumLength) {
            return bounded(value, maximumLength).trim().toLowerCase(Locale.ROOT);
        }

        private static String uuid(String value, String field) {
            try {
                return UUID.fromString(Objects.requireNonNull(value, field)).toString();
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Invalid audit " + field, exception);
            }
        }

        private static String optionalUuid(String value, String field) {
            return value == null || value.isBlank() ? "" : uuid(value, field);
        }

        private static String instant(String value) {
            try {
                return Instant.parse(Objects.requireNonNull(value, "timestamp")).toString();
            } catch (RuntimeException exception) {
                throw new IllegalArgumentException("Invalid audit timestamp", exception);
            }
        }

        private static List<String> boundedUuidList(List<String> values, int maximumSize) {
            Objects.requireNonNull(values, "values");
            if (values.size() > maximumSize) {
                throw new IllegalArgumentException("Audit target count exceeds limit");
            }
            return values.stream().map(value -> uuid(value, "target id")).toList();
        }

        private static List<String> boundedList(List<String> values, int maximumSize, int maximumLength) {
            Objects.requireNonNull(values, "values");
            if (values.size() > maximumSize) {
                throw new IllegalArgumentException("Audit list exceeds limit");
            }
            return values.stream().map(value -> normalized(value, maximumLength)).toList();
        }

        private static Map<String, String> boundedMap(Map<String, String> values) {
            Objects.requireNonNull(values, "values");
            if (values.size() > 32) {
                throw new IllegalArgumentException("Audit map exceeds limit");
            }
            Map<String, String> bounded = new LinkedHashMap<>();
            values.forEach((key, value) -> bounded.put(normalized(key, 64), bounded(value, 256)));
            return Map.copyOf(bounded);
        }

        private static String value(UUID value) {
            return value == null ? "" : value.toString();
        }
    }

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final int QUEUE_CAPACITY = 4096;
    private static final ArrayBlockingQueue<AuditEvent> QUEUE = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    private static final AtomicLong DROPPED = new AtomicLong();
    private static final DateTimeFormatter ROTATION_TIMESTAMP =
            DateTimeFormatter.ofPattern("uuuuMMddHHmmss").withZone(ZoneOffset.UTC);
    private static volatile boolean running;
    private static volatile Thread writerThread;
    private static volatile UUID sessionId = UUID.randomUUID();
    private static Path auditDirectory;
    private static Path activeFile;
    private static int retentionDays;
    private static long maximumFileBytes;

    private SecurityAuditService() {
    }

    public static synchronized void start(Path sefDirectory, int configuredRetentionDays, int maximumFileMiB) {
        shutdown();
        sessionId = UUID.randomUUID();
        DROPPED.set(0L);
        auditDirectory = sefDirectory.resolve("audit");
        activeFile = auditDirectory.resolve("security-audit.jsonl");
        retentionDays = Math.max(1, configuredRetentionDays);
        maximumFileBytes = Math.max(1L, maximumFileMiB) * 1024L * 1024L;
        try {
            Files.createDirectories(auditDirectory);
            pruneExpiredFiles();
        } catch (IOException exception) {
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to initialize security audit storage", exception);
            return;
        }
        running = true;
        writerThread = Thread.ofPlatform()
                .daemon(true)
                .name("sef-security-audit")
                .start(SecurityAuditService::writerLoop);
    }

    public static boolean record(AuditEvent event) {
        if (!running || event == null) {
            return false;
        }
        if (QUEUE.offer(event)) {
            return true;
        }
        long dropped = DROPPED.incrementAndGet();
        if (dropped == 1 || dropped % 100 == 0) {
            ServerEssentialsForge.LOGGER.error(
                    "[SEF] Security audit queue is full. {} event or events have been dropped",
                    dropped);
        }
        return false;
    }

    public static UUID currentSessionId() {
        return sessionId;
    }

    public static synchronized void shutdown() {
        running = false;
        Thread thread = writerThread;
        if (thread == null) {
            return;
        }
        thread.interrupt();
        try {
            thread.join(5000L);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
        writerThread = null;
        if (!QUEUE.isEmpty()) {
            ServerEssentialsForge.LOGGER.error(
                    "[SEF] Security audit shutdown left {} unwritten event or events",
                    QUEUE.size());
        }
    }

    private static void writerLoop() {
        List<AuditEvent> batch = new ArrayList<>(128);
        while (running || !QUEUE.isEmpty()) {
            try {
                AuditEvent first = QUEUE.poll(500L, TimeUnit.MILLISECONDS);
                if (first == null) {
                    continue;
                }
                batch.add(first);
                QUEUE.drainTo(batch, 127);
                writeBatch(batch);
                batch.clear();
            } catch (InterruptedException exception) {
                if (!running) {
                    continue;
                }
                Thread.currentThread().interrupt();
                return;
            } catch (IOException exception) {
                ServerEssentialsForge.LOGGER.error("[SEF] Failed to write security audit batch", exception);
                batch.clear();
            }
        }
    }

    private static void writeBatch(List<AuditEvent> batch) throws IOException {
        StringBuilder output = new StringBuilder(batch.size() * 192);
        for (AuditEvent event : batch) {
            output.append(GSON.toJson(event)).append(System.lineSeparator());
        }
        byte[] bytes = output.toString().getBytes(StandardCharsets.UTF_8);
        rotateIfRequired(bytes.length);
        Files.write(
                activeFile,
                bytes,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }

    private static void rotateIfRequired(int incomingBytes) throws IOException {
        if (!Files.exists(activeFile)) {
            return;
        }
        long size = Files.size(activeFile);
        if (size + incomingBytes <= maximumFileBytes) {
            return;
        }
        Path rotated = auditDirectory.resolve(
                "security-audit." + ROTATION_TIMESTAMP.format(Instant.now()) + ".jsonl");
        Files.move(activeFile, uniqueRotationPath(rotated));
        pruneExpiredFiles();
    }

    private static Path uniqueRotationPath(Path preferred) {
        if (!Files.exists(preferred)) {
            return preferred;
        }
        for (int counter = 1; counter < 10_000; counter++) {
            Path candidate = preferred.resolveSibling(preferred.getFileName() + "." + counter);
            if (!Files.exists(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not allocate security audit rotation path");
    }

    private static void pruneExpiredFiles() throws IOException {
        if (!Files.exists(auditDirectory)) {
            return;
        }
        Instant cutoff = Instant.now().minus(Duration.ofDays(retentionDays));
        try (var files = Files.list(auditDirectory)) {
            for (Path path : files
                    .filter(candidate -> candidate.getFileName().toString().startsWith("security-audit."))
                    .sorted(Comparator.naturalOrder())
                    .toList()) {
                if (Files.getLastModifiedTime(path).toInstant().isBefore(cutoff)) {
                    Files.deleteIfExists(path);
                }
            }
        }
    }
}
