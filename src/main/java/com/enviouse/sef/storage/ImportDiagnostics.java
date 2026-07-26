package com.enviouse.sef.storage;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

public final class ImportDiagnostics {
    private static final int MAXIMUM_ENTRIES = 256;
    private static final Deque<Entry> ENTRIES = new ArrayDeque<>();

    private ImportDiagnostics() {
    }

    public static synchronized void record(
            String domain,
            Path source,
            Result result,
            int importedRecords,
            String detail
    ) {
        ENTRIES.addLast(new Entry(
                Instant.now(),
                bounded(domain, 64),
                source == null ? "" : bounded(source.getFileName().toString(), 128),
                Objects.requireNonNull(result, "result"),
                Math.max(0, importedRecords),
                bounded(detail, 256)));
        while (ENTRIES.size() > MAXIMUM_ENTRIES) {
            ENTRIES.removeFirst();
        }
    }

    public static synchronized List<Entry> snapshot() {
        return List.copyOf(ENTRIES);
    }

    public static synchronized void clear() {
        ENTRIES.clear();
    }

    public record Entry(
            Instant timestamp,
            String domain,
            String sourceName,
            Result result,
            int importedRecords,
            String detail
    ) {
    }

    public enum Result {
        SUCCESS,
        PARTIAL,
        REJECTED,
        FAILED
    }

    private static String bounded(String value, int maximumLength) {
        String normalized = Objects.requireNonNullElse(value, "").trim();
        return normalized.length() <= maximumLength
                ? normalized
                : normalized.substring(0, maximumLength);
    }
}
