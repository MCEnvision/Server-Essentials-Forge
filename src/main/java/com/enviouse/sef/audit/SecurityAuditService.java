package com.enviouse.sef.audit;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.storage.AtomicFileStore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
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
            return sanitized.toString();
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
    private static final int RECENT_CAPACITY = 4096;
    private static final ArrayBlockingQueue<AuditEvent> QUEUE = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    private static final ArrayDeque<AuditEvent> RECENT = new ArrayDeque<>(RECENT_CAPACITY);
    private static final AtomicLong DROPPED = new AtomicLong();
    private static final AtomicLong FAILURES = new AtomicLong();
    private static final DateTimeFormatter ROTATION_TIMESTAMP =
            DateTimeFormatter.ofPattern("uuuuMMddHHmmss").withZone(ZoneOffset.UTC);
    private static volatile boolean running;
    private static volatile Thread writerThread;
    private static volatile UUID sessionId = UUID.randomUUID();
    private static volatile String failureDetail = "";
    private static Path auditDirectory;
    private static Path activeFile;
    private static NativeAuditFileProvider fileProvider;
    private static int retentionDays;
    private static long maximumFileBytes;

    private SecurityAuditService() {
    }

    public static synchronized void start(Path sefDirectory, int configuredRetentionDays, int maximumFileMiB) {
        shutdown();
        if (writerThread != null) {
            failureDetail = "the previous security audit writer did not stop";
            ServerEssentialsForge.LOGGER.error("[SEF] {}", failureDetail);
            return;
        }
        QUEUE.clear();
        synchronized (RECENT) {
            RECENT.clear();
        }
        sessionId = UUID.randomUUID();
        DROPPED.set(0L);
        FAILURES.set(0L);
        failureDetail = "";
        auditDirectory = Objects.requireNonNull(sefDirectory, "sefDirectory")
                .toAbsolutePath()
                .normalize()
                .resolve("audit")
                .normalize();
        activeFile = auditDirectory.resolve("security-audit.jsonl");
        retentionDays = Math.max(1, configuredRetentionDays);
        maximumFileBytes = Math.max(1L, maximumFileMiB) * 1024L * 1024L;
        try {
            createSafeDirectories(auditDirectory);
            fileProvider = NativeAuditFileProvider.open(auditDirectory);
            validateActiveFile();
            pruneExpiredFiles();
        } catch (IOException | RuntimeException exception) {
            FAILURES.incrementAndGet();
            failureDetail = "security audit storage initialization failed";
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to initialize security audit storage", exception);
            NativeAuditFileProvider provider = fileProvider;
            fileProvider = null;
            if (provider != null) {
                provider.close();
            }
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
            synchronized (RECENT) {
                while (RECENT.size() >= RECENT_CAPACITY) {
                    RECENT.removeFirst();
                }
                RECENT.addLast(event);
            }
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

    public static List<AuditEvent> recent(
            Predicate<AuditEvent> filter,
            int maximumRecords
    ) {
        Objects.requireNonNull(filter, "filter");
        int limit = Math.clamp(maximumRecords, 1, 128);
        List<AuditEvent> result = new ArrayList<>(limit);
        synchronized (RECENT) {
            var iterator = RECENT.descendingIterator();
            while (iterator.hasNext() && result.size() < limit) {
                AuditEvent event = iterator.next();
                if (filter.test(event)) {
                    result.add(event);
                }
            }
        }
        return List.copyOf(result);
    }

    public static Health health() {
        Thread thread = writerThread;
        return new Health(
                running,
                thread != null && thread.isAlive(),
                QUEUE.size(),
                DROPPED.get(),
                FAILURES.get(),
                failureDetail,
                sessionId);
    }

    public static synchronized void shutdown() {
        running = false;
        Thread thread = writerThread;
        if (thread == null) {
            NativeAuditFileProvider provider = fileProvider;
            fileProvider = null;
            if (provider != null) {
                provider.close();
            }
            return;
        }
        try {
            thread.join(5000L);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
        if (thread.isAlive()) {
            thread.interrupt();
            FAILURES.incrementAndGet();
            failureDetail = "security audit writer did not stop within the shutdown timeout";
            ServerEssentialsForge.LOGGER.error("[SEF] {}", failureDetail);
            return;
        }
        writerThread = null;
        NativeAuditFileProvider provider = fileProvider;
        fileProvider = null;
        if (provider != null) {
            provider.close();
        }
        if (!QUEUE.isEmpty()) {
            DROPPED.addAndGet(QUEUE.size());
            ServerEssentialsForge.LOGGER.error(
                    "[SEF] Security audit shutdown left {} unwritten event or events",
                    QUEUE.size());
            QUEUE.clear();
        }
    }

    private static void writerLoop() {
        List<AuditEvent> batch = new ArrayList<>(128);
        try {
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
                } catch (IOException | RuntimeException exception) {
                    running = false;
                    int queued = QUEUE.size();
                    long lost = batch.size() + queued;
                    DROPPED.addAndGet(lost);
                    FAILURES.incrementAndGet();
                    failureDetail = "security audit writer failed";
                    batch.clear();
                    QUEUE.clear();
                    ServerEssentialsForge.LOGGER.error(
                            "[SEF] Failed to write security audit batch. {} event or events were lost",
                            lost,
                            exception);
                    return;
                }
            }
        } finally {
            if (writerThread == Thread.currentThread()) {
                writerThread = null;
            }
        }
    }

    public record Health(
            boolean running,
            boolean writerAlive,
            int queued,
            long dropped,
            long failures,
            String detail,
            UUID sessionId
    ) {
    }

    private static void writeBatch(List<AuditEvent> batch) throws IOException {
        StringBuilder output = new StringBuilder(batch.size() * 192);
        for (AuditEvent event : batch) {
            output.append(GSON.toJson(event)).append(System.lineSeparator());
        }
        byte[] bytes = output.toString().getBytes(StandardCharsets.UTF_8);
        createSafeDirectories(auditDirectory);
        validateActiveFile();
        rotateIfRequired(bytes.length);
        appendActiveFile(bytes);
    }

    private static void appendActiveFile(byte[] bytes) throws IOException {
        NativeAuditFileProvider provider = fileProvider;
        if (provider == null) {
            throw new IOException("security audit native provider is unavailable");
        }
        provider.append(activeFile, bytes);
    }

    private static void rotateIfRequired(int incomingBytes) throws IOException {
        if (!Files.exists(activeFile, LinkOption.NOFOLLOW_LINKS)) {
            return;
        }
        validateActiveFile();
        long size = Files.readAttributes(
                activeFile,
                BasicFileAttributes.class,
                LinkOption.NOFOLLOW_LINKS).size();
        if (size + incomingBytes <= maximumFileBytes) {
            return;
        }
        Path rotated = auditDirectory.resolve(
                "security-audit." + ROTATION_TIMESTAMP.format(Instant.now()) + ".jsonl");
        Files.move(activeFile, uniqueRotationPath(rotated));
        pruneExpiredFiles();
    }

    private static Path uniqueRotationPath(Path preferred) {
        if (!Files.exists(preferred, LinkOption.NOFOLLOW_LINKS)) {
            return preferred;
        }
        for (int counter = 1; counter < 10_000; counter++) {
            Path candidate = preferred.resolveSibling(preferred.getFileName() + "." + counter);
            if (!Files.exists(candidate, LinkOption.NOFOLLOW_LINKS)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not allocate security audit rotation path");
    }

    private static void pruneExpiredFiles() throws IOException {
        if (!Files.exists(auditDirectory, LinkOption.NOFOLLOW_LINKS)) {
            return;
        }
        createSafeDirectories(auditDirectory);
        Instant cutoff = Instant.now().minus(Duration.ofDays(retentionDays));
        try (var files = Files.list(auditDirectory)) {
            for (Path path : files
                    .filter(candidate -> candidate.getFileName().toString().startsWith("security-audit."))
                    .sorted(Comparator.naturalOrder())
                    .toList()) {
                BasicFileAttributes attributes = Files.readAttributes(
                        path,
                        BasicFileAttributes.class,
                        LinkOption.NOFOLLOW_LINKS);
                if (attributes.isRegularFile()
                        && !attributes.isSymbolicLink()
                        && attributes.lastModifiedTime().toInstant().isBefore(cutoff)) {
                    fileProvider.validate(path);
                    Files.deleteIfExists(path);
                }
            }
        }
    }

    private static void validateActiveFile() throws IOException {
        if (!Files.exists(activeFile, LinkOption.NOFOLLOW_LINKS)) {
            return;
        }
        BasicFileAttributes attributes = Files.readAttributes(
                activeFile,
                BasicFileAttributes.class,
                LinkOption.NOFOLLOW_LINKS);
        if (attributes.isSymbolicLink() || !attributes.isRegularFile()) {
            throw new IOException("security audit active file is not a regular file");
        }
        fileProvider.validate(activeFile);
    }

    private static void createSafeDirectories(Path directory) throws IOException {
        AtomicFileStore.createSafeDirectories(directory);
    }
}
