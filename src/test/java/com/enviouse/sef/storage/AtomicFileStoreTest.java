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
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void nonAtomicFallbackKeepsACompletePreviousGeneration() throws Exception {
        Path target = temporaryDirectory.resolve("data.json");
        AtomicFileStore.write(target, "first".getBytes(StandardCharsets.UTF_8));

        AtomicFileStore.write(
                target,
                "second".getBytes(StandardCharsets.UTF_8),
                true);

        assertEquals("second", Files.readString(target));
        assertEquals("first", Files.readString(AtomicFileStore.previousPath(target)));
        Files.delete(target);
        assertTrue(AtomicFileStore.restorePrevious(target, 1_024));
        assertEquals("first", Files.readString(target));
    }

    @Test
    void writesRejectSymbolicLinkTargetsAndParents() throws Exception {
        Path external = temporaryDirectory.resolve("external.txt");
        Files.writeString(external, "secret");
        Path targetLink = temporaryDirectory.resolve("target.json");
        Files.createSymbolicLink(targetLink, external);

        assertThrows(
                AtomicFileStore.UnsafeStoragePathException.class,
                () -> AtomicFileStore.write(
                        targetLink,
                        "replacement".getBytes(StandardCharsets.UTF_8)));
        assertEquals("secret", Files.readString(external));

        Path realDirectory = temporaryDirectory.resolve("real");
        Files.createDirectory(realDirectory);
        Path directoryLink = temporaryDirectory.resolve("linked");
        Files.createSymbolicLink(directoryLink, realDirectory);
        assertThrows(
                AtomicFileStore.UnsafeStoragePathException.class,
                () -> AtomicFileStore.write(
                        directoryLink.resolve("data.json"),
                        "replacement".getBytes(StandardCharsets.UTF_8)));
    }
}
