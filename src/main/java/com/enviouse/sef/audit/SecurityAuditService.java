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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class SecurityAuditService {
    public record AuditEvent(
            String timestamp,
            String category,
            String action,
            String issuer,
            String target,
            String commandRoot,
            String result,
            String reason
    ) {
        public static AuditEvent create(
                String category,
                String action,
                String issuer,
                String target,
                String commandRoot,
                String result,
                String reason
        ) {
            return new AuditEvent(
                    Instant.now().toString(),
                    bounded(category, 64),
                    bounded(action, 64),
                    bounded(issuer, 64),
                    bounded(target, 64),
                    bounded(commandRoot, 128),
                    bounded(result, 64),
                    bounded(reason, 256));
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
    }

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final int QUEUE_CAPACITY = 4096;
    private static final ArrayBlockingQueue<AuditEvent> QUEUE = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    private static final AtomicLong DROPPED = new AtomicLong();
    private static final DateTimeFormatter ROTATION_TIMESTAMP =
            DateTimeFormatter.ofPattern("uuuuMMddHHmmss").withZone(ZoneOffset.UTC);
    private static volatile boolean running;
    private static volatile Thread writerThread;
    private static Path auditDirectory;
    private static Path activeFile;
    private static int retentionDays;
    private static long maximumFileBytes;

    private SecurityAuditService() {
    }

    public static synchronized void start(Path sefDirectory, int configuredRetentionDays, int maximumFileMiB) {
        shutdown();
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
