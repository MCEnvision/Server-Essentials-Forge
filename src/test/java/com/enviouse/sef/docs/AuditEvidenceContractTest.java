package com.enviouse.sef.docs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditEvidenceContractTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void schemaAcceptsSyntheticRecordAndNormalizesOutput() throws Exception {
        JsonObject raw = record();
        raw.getAsJsonObject("payload").addProperty("zeta", "second");
        raw.getAsJsonObject("payload").addProperty("alpha", "first");

        JsonObject sanitized = AuditEvidenceContract.sanitize(raw);
        AuditEvidenceContract.validateEvidenceRecord(sanitized);
        Path output = AuditEvidenceContract.write(temporaryDirectory.resolve("evidence"), "synthetic.json", raw);

        String persisted = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(persisted.indexOf("\"alpha\"") < persisted.indexOf("\"zeta\""));
        assertTrue(Files.isRegularFile(output));
        assertFalse(AuditEvidenceContract.isExpired(sanitized, Instant.parse("2026-01-01T00:00:00Z")));
    }

    @Test
    void sanitizerRemovesSecretsPersonalDataHostPathsAndUnboundedLogs() {
        JsonObject raw = record();
        JsonObject payload = raw.getAsJsonObject("payload");
        payload.addProperty("api_token", "ghp_THIS_MUST_NOT_APPEAR");
        payload.addProperty("email", "owner@example.com");
        payload.addProperty("log", "a".repeat(AuditEvidenceContract.MAX_STRING_LENGTH * 4));
        payload.addProperty("path", "/home/owner/private/server.log");
        payload.addProperty("summary", "clean result");

        String sanitized = AuditEvidenceContract.sanitize(raw).toString();
        assertFalse(sanitized.contains("ghp_THIS_MUST_NOT_APPEAR"));
        assertFalse(sanitized.contains("owner@example.com"));
        assertFalse(sanitized.contains("server.log"));
        assertFalse(sanitized.contains("/home/owner"));
        assertTrue(sanitized.contains("clean result"));
    }

    @Test
    void schemaRejectsMissingFieldsAndUnknownVersions() {
        JsonObject missingRevision = record();
        missingRevision.remove("revision");
        assertThrows(IllegalArgumentException.class,
                () -> AuditEvidenceContract.validateEvidenceRecord(missingRevision));

        JsonObject unknownVersion = record();
        unknownVersion.addProperty("schemaVersion", 2);
        assertThrows(IllegalArgumentException.class,
                () -> AuditEvidenceContract.validateEvidenceRecord(unknownVersion));

        JsonObject missingInvalidation = record();
        missingInvalidation.remove("invalidation");
        assertThrows(IllegalArgumentException.class,
                () -> AuditEvidenceContract.validateEvidenceRecord(missingInvalidation));
    }

    @Test
    void inventorySetRejectsDuplicateStableIdentityAndRequiresEvidence() {
        JsonArray rows = new JsonArray();
        rows.add(row("command:sef.test"));
        JsonObject duplicate = row("command:sef.test.duplicate");
        duplicate.addProperty("semanticKey", "command:sef.test");
        rows.add(duplicate);
        assertThrows(IllegalArgumentException.class,
                () -> AuditEvidenceContract.validateInventorySet(rows));

        JsonObject missingRoute = row("route:sef.test");
        missingRoute.remove("evidenceRoute");
        assertThrows(IllegalArgumentException.class,
                () -> AuditEvidenceContract.validateInventoryRow(missingRoute));

        JsonArray empty = new JsonArray();
        AuditEvidenceContract.validateInventorySet(empty);
    }

    @Test
    void writerRejectsSymlinkedEvidenceRootAndTarget() throws Exception {
        Path realRoot = temporaryDirectory.resolve("real");
        Files.createDirectories(realRoot);
        Path rootLink = temporaryDirectory.resolve("root-link");
        try {
            Files.createSymbolicLink(rootLink, realRoot);
        } catch (UnsupportedOperationException | java.io.IOException exception) {
            return;
        }
        assertThrows(IllegalArgumentException.class,
                () -> AuditEvidenceContract.write(rootLink, "root.json", record()));

        Path target = realRoot.resolve("target.json");
        Files.createFile(target);
        Path targetLink = realRoot.resolve("linked.json");
        Files.createSymbolicLink(targetLink, target);
        assertThrows(IllegalArgumentException.class,
                () -> AuditEvidenceContract.write(realRoot, "linked.json", record()));
    }

    private static JsonObject record() {
        JsonObject record = new JsonObject();
        record.addProperty("schemaVersion", 1);
        record.addProperty("recordId", "run-p000-task002");
        record.addProperty("phaseId", "SEFAUD-PHASE-000");
        record.addProperty("taskId", "P000-TASK-002");
        record.addProperty("owner", "repository-audit-contract");
        record.addProperty("evidenceRoute", "external://sef-p000-evidence/synthetic.json");
        record.addProperty("environmentId", "synthetic-jvm-21");
        record.addProperty("capturedAt", "2026-09-02T00:00:00Z");
        record.addProperty("expected", "schema accepts a bounded synthetic record");
        record.addProperty("actual", "schema accepted the record");
        record.addProperty("result", "pass");
        record.addProperty("sourceDigest", "sha256:synthetic");
        JsonObject revision = new JsonObject();
        revision.addProperty("commit", "0123456789abcdef0123456789abcdef01234567");
        revision.addProperty("tree", "abcdef0123456789abcdef0123456789abcdef01");
        record.add("revision", revision);
        JsonObject retention = new JsonObject();
        retention.addProperty("class", "task");
        retention.addProperty("days", 30);
        record.add("retention", retention);
        JsonObject invalidation = new JsonObject();
        invalidation.addProperty("state", "valid");
        invalidation.add("triggers", array("source-change", "plan-change", "environment-change"));
        invalidation.add("invalidatedBy", new JsonArray());
        record.add("invalidation", invalidation);
        record.add("payload", new JsonObject());
        return record;
    }

    private static JsonObject row(String rowId) {
        JsonObject row = new JsonObject();
        row.addProperty("schemaVersion", 1);
        row.addProperty("rowId", rowId);
        row.addProperty("category", "command");
        row.addProperty("semanticKey", rowId);
        row.addProperty("owner", "repository-audit-contract");
        row.addProperty("phase", "SEFAUD-PHASE-000");
        row.addProperty("evidenceRoute", "external://sef-p000-evidence/inventory.json");
        row.addProperty("evidenceClass", "static");
        row.addProperty("disposition", "implemented");
        row.add("invalidatedBy", array("command-registry-change"));
        row.add("sourceLocations", array("src/test/java/com/enviouse/sef/docs/AuditEvidenceContract.java"));
        return row;
    }

    private static JsonArray array(String... values) {
        JsonArray array = new JsonArray();
        for (String value : values) {
            array.add(value);
        }
        return array;
    }
}
