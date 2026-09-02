package com.enviouse.sef.audit;

import com.enviouse.sef.kernel.ActionResult;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditServiceTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void structuredEventPersistsEveryRequiredField() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        UUID stepId = UUID.randomUUID();
        UUID observerId = UUID.randomUUID();
        AuditService.Event event = new AuditService.Event(
                1,
                eventId,
                Instant.parse("2026-07-26T12:00:00Z"),
                sessionId,
                actorId,
                "EnVy",
                "GUI",
                "sef:test.action",
                List.of(targetId),
                Map.of("mode", "safe"),
                AuditService.Result.SUCCESS,
                ActionResult.ReasonCode.SUCCESS,
                37L,
                "panel",
                parentId,
                stepId,
                4L,
                9L,
                Map.of("permission_provider", "luckperms"),
                AuditService.RedactionClass.SECRET_ARGUMENTS,
                List.of("hide.secret"),
                observerId,
                "abcdef",
                AuditService.AuditClass.ADMIN_ACTION);

        SecurityAuditService.start(temporaryDirectory, 7, 1);
        try {
            assertTrue(AuditService.record(event));
        } finally {
            SecurityAuditService.shutdown();
        }

        Path auditFile = temporaryDirectory.resolve("audit").resolve("security-audit.jsonl");
        JsonObject persisted = JsonParser.parseString(Files.readAllLines(auditFile).getFirst()).getAsJsonObject();
        assertEquals(1, persisted.get("schemaVersion").getAsInt());
        assertEquals(eventId.toString(), persisted.get("eventId").getAsString());
        assertEquals(sessionId.toString(), persisted.get("serverSessionId").getAsString());
        assertEquals(actorId.toString(), persisted.get("actorUuid").getAsString());
        assertEquals("EnVy", persisted.get("actorUsername").getAsString());
        assertEquals("gui", persisted.get("sourceType").getAsString());
        assertEquals("sef:test.action", persisted.get("actionId").getAsString());
        assertEquals(targetId.toString(), persisted.getAsJsonArray("targetUuids").get(0).getAsString());
        assertEquals("safe", persisted.getAsJsonObject("normalizedParameters").get("mode").getAsString());
        assertEquals(37L, persisted.get("durationMillis").getAsLong());
        assertEquals(parentId.toString(), persisted.get("parentJobId").getAsString());
        assertEquals(stepId.toString(), persisted.get("stepCorrelationId").getAsString());
        assertEquals(4L, persisted.get("definitionRevision").getAsLong());
        assertEquals(9L, persisted.get("policyRevision").getAsLong());
        assertEquals(
                "luckperms",
                persisted.getAsJsonObject("providerContext").get("permission_provider").getAsString());
        assertEquals("secret_arguments", persisted.get("redactionClass").getAsString());
        assertEquals(
                "hide.secret",
                persisted.getAsJsonArray("appliedRedactionRuleIds").get(0).getAsString());
        assertEquals(observerId.toString(), persisted.get("observerUuid").getAsString());
        assertEquals("abcdef", persisted.get("previousEventHash").getAsString());
        assertEquals("admin_action", persisted.get("auditClass").getAsString());
    }

    @Test
    void writerFailureStopsAcceptanceAndReportsLostEvents() throws Exception {
        Path activeFile = temporaryDirectory.resolve("audit").resolve("security-audit.jsonl");
        SecurityAuditService.start(temporaryDirectory, 7, 1);
        try {
            Files.createDirectories(activeFile);
            assertTrue(SecurityAuditService.record(SecurityAuditService.AuditEvent.create(
                    "test",
                    "writer_failure",
                    "tester",
                    "",
                    "test",
                    "attempted",
                    "test")));
            await(() -> SecurityAuditService.health().failures() > 0L
                    && !SecurityAuditService.health().writerAlive());

            SecurityAuditService.Health health = SecurityAuditService.health();
            assertFalse(health.running());
            assertFalse(health.writerAlive());
            assertTrue(health.dropped() > 0L);
            assertFalse(SecurityAuditService.record(SecurityAuditService.AuditEvent.create(
                    "test",
                    "after_failure",
                    "tester",
                    "",
                    "test",
                    "attempted",
                    "test")));
        } finally {
            SecurityAuditService.shutdown();
        }
    }

    @Test
    void activeAuditSymlinkIsRejectedWithoutWritingExternalTarget() throws Exception {
        Path external = temporaryDirectory.resolve("external.jsonl");
        Files.writeString(external, "sentinel");
        Path auditDirectory = temporaryDirectory.resolve("audit");
        Files.createDirectories(auditDirectory);
        Files.createSymbolicLink(
                auditDirectory.resolve("security-audit.jsonl"),
                external);

        SecurityAuditService.start(temporaryDirectory, 7, 1);
        try {
            assertFalse(SecurityAuditService.health().running());
            assertTrue(SecurityAuditService.health().failures() > 0L);
            assertEquals("sentinel", Files.readString(external));
        } finally {
            SecurityAuditService.shutdown();
        }
    }

    @Test
    void activeAuditHardLinkIsRejectedWithoutWritingExternalTarget() throws Exception {
        Path external = temporaryDirectory.resolve("external-hard-link.jsonl");
        Files.writeString(external, "sentinel");
        Path auditDirectory = temporaryDirectory.resolve("audit");
        Files.createDirectories(auditDirectory);
        Files.createLink(auditDirectory.resolve("security-audit.jsonl"), external);

        SecurityAuditService.start(temporaryDirectory, 7, 1);
        try {
            assertFalse(SecurityAuditService.health().running());
            assertTrue(SecurityAuditService.health().failures() > 0L);
            assertEquals("sentinel", Files.readString(external));
        } finally {
            SecurityAuditService.shutdown();
        }
    }

    @Test
    void nativeProviderRejectsSymlinkedAuditDirectory() throws Exception {
        Path target = temporaryDirectory.resolve("real-audit");
        Files.createDirectories(target);
        Path link = temporaryDirectory.resolve("audit-link");
        Files.createSymbolicLink(link, target);

        assertThrows(IOException.class, () -> NativeAuditFileProvider.open(link));
    }

    @Test
    void auditFieldsNormalizeControlAndFormatCharacters() {
        SecurityAuditService.AuditEvent event = SecurityAuditService.AuditEvent.create(
                "test",
                "normalized_fields",
                "En\u202Evy\n\tname",
                "target\u0000\nname",
                "test",
                "attempted",
                "test");

        assertEquals("En vy name", event.actorUsername());
        assertEquals("target name", event.normalizedParameters().get("target"));
    }

    @Test
    void recentAuditRingFiltersAndReturnsNewestFirst() {
        SecurityAuditService.start(temporaryDirectory, 7, 1);
        try {
            assertTrue(SecurityAuditService.record(SecurityAuditService.AuditEvent.create(
                    "test", "first", "envy", "", "fancy_tags", "success", "success")));
            assertTrue(SecurityAuditService.record(SecurityAuditService.AuditEvent.create(
                    "test", "unrelated", "envy", "", "other", "success", "success")));
            assertTrue(SecurityAuditService.record(SecurityAuditService.AuditEvent.create(
                    "test", "second", "envy", "", "fancy_tags", "success", "success")));

            var recent = SecurityAuditService.recent(
                    event -> event.origin().equals("fancy_tags"),
                    16);
            assertEquals(2, recent.size());
            assertEquals("second", recent.getFirst().actionId());
            assertEquals("first", recent.getLast().actionId());
        } finally {
            SecurityAuditService.shutdown();
        }
    }

    private static void await(java.util.function.BooleanSupplier condition) {
        long deadline = System.nanoTime() + java.time.Duration.ofSeconds(2).toNanos();
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
        assertTrue(condition.getAsBoolean());
    }
}
