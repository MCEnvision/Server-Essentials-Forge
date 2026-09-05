package com.enviouse.sef.commandlog;

import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.kernel.observation.ObservationContracts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileLogSinkTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void disabledConfigurationCreatesNoDirectoryOrThread() {
        FileLogSink sink = new FileLogSink();

        assertTrue(sink.startConfigured(temporaryDirectory));

        assertFalse(sink.health().accepting());
        assertFalse(Files.exists(temporaryDirectory.resolve("logs").resolve("sef")));
        assertFalse(threadExists("sef-file-log"));
    }

    @Test
    void enablingRefusesASymbolicLinkForTheMinecraftLogsDirectory() throws Exception {
        Path outside = temporaryDirectory.resolve("outside");
        Files.createDirectories(outside);
        Files.createSymbolicLink(temporaryDirectory.resolve("logs"), outside);
        FileLogSink sink = new FileLogSink();

        assertTrue(sink.startConfigured(temporaryDirectory));
        assertFalse(sink.enable());
        assertFalse(Files.exists(outside.resolve("sef")));
        assertFalse(threadExists("sef-file-log"));
    }

    @Test
    void enablingRefusesASymbolicLinkInsideTheOwnedLogTree() throws Exception {
        Path outside = temporaryDirectory.resolve("outside");
        Path root = temporaryDirectory.resolve("logs").resolve("sef");
        Files.createDirectories(outside);
        Files.createDirectories(root);
        Files.createSymbolicLink(root.resolve("commands"), outside);
        FileLogSink sink = new FileLogSink();

        assertTrue(sink.startConfigured(temporaryDirectory));
        assertFalse(sink.enable());
        assertFalse(Files.exists(outside.resolve("archive")));
        assertFalse(threadExists("sef-file-log"));
    }

    @Test
    void writerRefusesASymbolicLinkForTheActiveCommandFile() throws Exception {
        Path outside = temporaryDirectory.resolve("outside.jsonl");
        Files.writeString(outside, "sentinel");
        Path current = temporaryDirectory.resolve("logs").resolve("sef")
                .resolve("commands").resolve("current.jsonl");
        Files.createDirectories(current.getParent());
        Files.createSymbolicLink(current, outside);
        FileLogSink sink = new FileLogSink();

        assertTrue(sink.startConfigured(temporaryDirectory));
        assertTrue(sink.enable());
        await(() -> sink.health().state() == FileLogSink.State.FAILED);
        await(() -> !threadExists("sef-file-log"));

        assertEquals("sentinel", Files.readString(outside));
        sink.shutdown();
    }

    @Test
    void previousIncompleteSessionRemainsDegradedUntilAcknowledged() throws Exception {
        Path state = temporaryDirectory.resolve("logs").resolve("sef").resolve("state");
        Files.createDirectories(state);
        Files.writeString(state.resolve("incomplete-session.json"), "{}");
        FileLogSink sink = new FileLogSink();

        assertTrue(sink.startConfigured(temporaryDirectory));
        assertTrue(sink.enable());
        assertEquals(FileLogSink.State.DEGRADED, sink.health().state());
        assertTrue(sink.health().detail().contains("acknowledgement"));
        assertTrue(sink.acknowledgeRepair());
        assertEquals(FileLogSink.State.HEALTHY, sink.health().state());

        sink.shutdown();
        assertFalse(threadExists("sef-file-log"));
    }

    @Test
    void writerFailureLeavesAnIncompleteSessionMarker() throws Exception {
        Path current = temporaryDirectory.resolve("logs").resolve("sef")
                .resolve("commands").resolve("current.jsonl");
        Files.createDirectories(current);
        FileLogSink sink = new FileLogSink();

        assertTrue(sink.startConfigured(temporaryDirectory));
        assertTrue(sink.enable());
        await(() -> sink.health().state() == FileLogSink.State.FAILED);
        Path marker = temporaryDirectory.resolve("logs").resolve("sef")
                .resolve("state").resolve("incomplete-session.json");
        await(() -> Files.isRegularFile(marker));
        await(() -> !threadExists("sef-file-log"));

        assertEquals(FileLogSink.State.FAILED, sink.health().state());
        assertFalse(sink.health().accepting());
        assertFalse(sink.submit(record(UUID.randomUUID(), "say", "sef:test.say")));
        assertTrue(Files.isRegularFile(marker));
        sink.shutdown();
    }

    @Test
    void connectionRecordsSerializeWithoutRawAddresses() throws Exception {
        FileLogSink sink = new FileLogSink();
        assertTrue(sink.startConfigured(temporaryDirectory));
        assertTrue(sink.enable());
        assertTrue(sink.setConnectionStreamEnabled(true));
        assertTrue(sink.submitConnection(new FileLogSink.ConnectionRecord(
                1,
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.now(),
                FileLogSink.ConnectionType.JOIN,
                UUID.randomUUID(),
                "player",
                "sha256:0123456789abcdef",
                "203.0.113.x")));

        sink.shutdown();

        Path current = temporaryDirectory.resolve("logs").resolve("sef")
                .resolve("connection_events").resolve("current.jsonl");
        String written = Files.readString(current);
        assertTrue(written.contains("\"timestamp\":\""));
        assertTrue(written.contains("sha256:0123456789abcdef"));
        assertTrue(written.contains("203.0.113.x"));
        assertFalse(written.contains("203.0.113.42"));
        assertFalse(threadExists("sef-file-log"));
    }

    @Test
    void commandRecordRemovesUnicodeFormatCharactersFromObserverText() {
        UUID actor = UUID.randomUUID();
        CommandEventJournal.CommandRecord record = new CommandEventJournal.CommandRecord(
                1,
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                Instant.now(),
                ObservationContracts.LifecycleStage.COMPLETED,
                actor,
                actor,
                "En\u202Evy",
                CommandDefinition.SourceType.PLAYER,
                "minecraft:over\u200Bworld",
                0,
                64,
                0,
                "sa\u202Ey",
                "sef:test.\u200Bsay",
                "/sa\u202Ey",
                CommandRedactionPolicy.RedactionClass.PUBLIC,
                Set.of(),
                "player\u200B",
                false,
                1,
                1L,
                "provider\u202Edetail");

        assertEquals("Envy", record.actorName());
        assertEquals("minecraft:overworld", record.dimensionId());
        assertEquals("say", record.root());
        assertEquals("sef:test.say", record.actionId());
        assertEquals("/say", record.commandDisplay());
        assertEquals("player", record.origin());
        assertEquals("providerdetail", record.detail());
    }

    @Test
    void typedCaptureFiltersApplyWithoutSuppressingSecurityRoots() {
        FileLogSink sink = new FileLogSink();
        UUID actor = UUID.randomUUID();
        CommandEventJournal.CommandRecord ordinary = record(actor, "say", "sef:test.say");

        assertTrue(sink.addCaptureFilter(
                false,
                FileLogSink.FilterKind.SOURCE,
                "player"));
        assertFalse(sink.captureFilter().matches(ordinary));

        assertTrue(sink.setCaptureFilterEnabled(
                FileLogSink.FilterKind.SOURCE,
                "player",
                true));
        assertTrue(sink.captureFilter().matches(ordinary));

        sink.resetCaptureFilter();
        assertTrue(sink.addCaptureFilter(
                true,
                FileLogSink.FilterKind.PLAYER,
                actor.toString()));
        assertTrue(sink.setCaptureMode(FileLogSink.FilterMode.INCLUDE));
        assertTrue(sink.captureFilter().matches(ordinary));
        assertFalse(sink.captureFilter().matches(record(UUID.randomUUID(), "say", "sef:test.say")));

        assertTrue(sink.addCaptureFilter(
                false,
                FileLogSink.FilterKind.ROOT,
                "silent"));
        assertTrue(sink.captureFilter().matches(record(actor, "silent", "sef:wrapper.silent")));
    }

    @Test
    void captureFilterLimitFailsClosedWithoutReplacingTheLastValidSnapshot() {
        FileLogSink sink = new FileLogSink();
        for (int index = 0; index < 128; index++) {
            assertTrue(sink.addCaptureFilter(
                    true,
                    FileLogSink.FilterKind.ROOT,
                    "command" + index));
        }

        FileLogSink.CaptureFilter before = sink.captureFilter();
        assertFalse(sink.addCaptureFilter(
                true,
                FileLogSink.FilterKind.ROOT,
                "overflow"));
        assertTrue(sink.captureFilter() == before);
    }

    @Test
    void retentionCommitRejectsAChangedPreview() {
        FileLogSink sink = new FileLogSink();
        FileLogSink.RetentionPreview current = sink.retentionPreview();

        assertEquals(0, sink.runRetention(current));
        assertEquals(-1, sink.runRetention(new FileLogSink.RetentionPreview(
                current.archives() + 1,
                current.bytes(),
                current.oldest(),
                current.newest())));
    }

    private static CommandEventJournal.CommandRecord record(UUID actor, String root, String action) {
        return new CommandEventJournal.CommandRecord(
                1,
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                Instant.now(),
                ObservationContracts.LifecycleStage.COMPLETED,
                actor,
                actor,
                "player",
                CommandDefinition.SourceType.PLAYER,
                "minecraft:overworld",
                0,
                64,
                0,
                root,
                action,
                "/" + root,
                CommandRedactionPolicy.RedactionClass.PUBLIC,
                Set.of(),
                "player",
                false,
                1,
                1L,
                "");
    }

    private static boolean threadExists(String name) {
        return Thread.getAllStackTraces().keySet().stream()
                .anyMatch(thread -> thread.isAlive() && thread.getName().equals(name));
    }

    private static void await(java.util.function.BooleanSupplier condition) {
        long deadline = System.nanoTime() + java.time.Duration.ofSeconds(2).toNanos();
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
        assertTrue(condition.getAsBoolean());
    }
}
