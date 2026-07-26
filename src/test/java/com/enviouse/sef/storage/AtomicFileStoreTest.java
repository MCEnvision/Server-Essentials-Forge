package com.enviouse.sef.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtomicFileStoreTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void atomicWriteReplacesCompleteDocumentAndCleansTemporaryFile() throws Exception {
        Path target = temporaryDirectory.resolve("data.json");
        AtomicFileStore.write(target, "first".getBytes(StandardCharsets.UTF_8));
        AtomicFileStore.write(target, "second".getBytes(StandardCharsets.UTF_8));

        assertEquals("second", Files.readString(target));
        try (var files = Files.list(temporaryDirectory)) {
            assertFalse(files.anyMatch(path -> path.getFileName().toString().endsWith(".tmp")));
        }
    }

    @Test
    void backupPreservesOriginalBytes() throws Exception {
        Path target = temporaryDirectory.resolve("data.json");
        Files.writeString(target, "legacy");
        Clock clock = Clock.fixed(Instant.parse("2026-07-25T12:00:00Z"), ZoneOffset.UTC);

        Path backup = AtomicFileStore.backup(
                target,
                temporaryDirectory.resolve(".backups"),
                "v0.bak",
                clock);

        assertTrue(Files.exists(target));
        assertEquals("legacy", Files.readString(backup));
    }

    @Test
    void quarantineMovesBadDocumentOutOfActivePath() throws Exception {
        Path target = temporaryDirectory.resolve("data.json");
        Files.writeString(target, "bad");
        Clock clock = Clock.fixed(Instant.parse("2026-07-25T12:00:00Z"), ZoneOffset.UTC);

        Path quarantined = AtomicFileStore.quarantine(
                target,
                temporaryDirectory.resolve(".corrupt"),
                clock);

        assertFalse(Files.exists(target));
        assertEquals("bad", Files.readString(quarantined));
    }
}
