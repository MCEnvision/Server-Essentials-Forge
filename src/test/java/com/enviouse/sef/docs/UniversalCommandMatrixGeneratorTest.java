package com.enviouse.sef.docs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UniversalCommandMatrixGeneratorTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void matrixContainsEveryCommandAndUnavailableFamilyWithAllDimensions() {
        JsonObject matrix = UniversalCommandMatrixGenerator.generate();
        UniversalCommandMatrixGenerator.validate(matrix);
        assertEquals(724, matrix.get("rowCount").getAsInt());
        assertEquals(708, matrix.get("commandRowCount").getAsInt());
        assertEquals(16, matrix.get("unavailableRowCount").getAsInt());
        assertFalse(matrix.get("complete").getAsBoolean());
        assertTrue(matrix.get("openRowCount").getAsInt() > 0);
        assertEquals(UniversalCommandMatrixGenerator.DIMENSIONS.size(),
                matrix.getAsJsonArray("dimensions").size());
        assertEquals("src/main/java/com/enviouse/sef/kernel/KernelServices.java",
                matrix.getAsJsonArray("rows").get(0).getAsJsonObject()
                        .getAsJsonArray("sourceLocations").get(0).getAsString());
        matrix.getAsJsonArray("rows").asList().stream()
                .filter(row -> row.getAsJsonObject().get("category").getAsString().equals("command-matrix"))
                .forEach(row -> {
                    JsonObject command = row.getAsJsonObject();
                    JsonObject join = command.getAsJsonObject("auditJoin");
                    assertEquals(command.get("auditClass").getAsString(), join.get("auditClass").getAsString());
                    assertTrue(join.has("eventWriter"));
                    assertTrue(join.has("nativeSink"));
                    assertTrue(join.has("pipelineCallSites"));
                    assertTrue(join.has("writerSources"));
                    assertTrue(join.has("sinkSources"));
                });
    }

    @Test
    void matrixGenerationIsDeterministicAndCanWriteToExternalRoot() throws Exception {
        JsonObject first = UniversalCommandMatrixGenerator.generate();
        JsonObject second = UniversalCommandMatrixGenerator.generate();
        assertEquals(first.toString(), second.toString());
        Path output = UniversalCommandMatrixGenerator.write(
                temporaryDirectory.resolve("inventory"), "universal-command-matrix.json");
        JsonObject persisted = JsonParser.parseString(
                Files.readString(output, StandardCharsets.UTF_8)).getAsJsonObject();
        assertEquals(AuditDriftValidator.normalized(first), AuditDriftValidator.normalized(persisted));

        String evidenceRoot = System.getProperty("sef.audit.evidenceRoot", "").trim();
        if (!evidenceRoot.isEmpty()) {
            UniversalCommandMatrixGenerator.write(
                    Path.of(evidenceRoot), "universal-command-matrix.json");
        }
    }

    @Test
    void validatorRejectsStatusCountDrift() {
        JsonObject matrix = UniversalCommandMatrixGenerator.generate();
        matrix.addProperty("openRowCount", matrix.get("openRowCount").getAsInt() - 1);
        assertThrows(IllegalArgumentException.class,
                () -> UniversalCommandMatrixGenerator.validate(matrix));
    }

    @Test
    void validatorRejectsNotApplicableRequiredCommandDimension() {
        JsonObject matrix = UniversalCommandMatrixGenerator.generate();
        JsonObject firstCommand = matrix.getAsJsonArray("rows").asList().stream()
                .map(element -> element.getAsJsonObject())
                .filter(row -> row.get("category").getAsString().equals("command-matrix"))
                .findFirst()
                .orElseThrow();
        firstCommand.getAsJsonObject("dimensions")
                .getAsJsonObject("authority")
                .addProperty("status", "not_applicable");
        firstCommand.getAsJsonObject("dimensions")
                .getAsJsonObject("authority")
                .addProperty("reason", "invalid test mutation");
        assertThrows(IllegalArgumentException.class,
                () -> UniversalCommandMatrixGenerator.validate(matrix));
    }

    @Test
    void validatorRejectsRowStatusThatHidesAnOpenDimension() {
        JsonObject matrix = UniversalCommandMatrixGenerator.generate();
        JsonObject firstCommand = matrix.getAsJsonArray("rows").asList().stream()
                .map(element -> element.getAsJsonObject())
                .filter(row -> row.get("category").getAsString().equals("command-matrix"))
                .findFirst()
                .orElseThrow();
        firstCommand.addProperty("status", "partial");
        assertThrows(IllegalArgumentException.class,
                () -> UniversalCommandMatrixGenerator.validate(matrix));
    }

    @Test
    void validatorRejectsMissingAuditJoin() {
        JsonObject matrix = UniversalCommandMatrixGenerator.generate();
        JsonObject firstCommand = matrix.getAsJsonArray("rows").asList().stream()
                .map(element -> element.getAsJsonObject())
                .filter(row -> row.get("category").getAsString().equals("command-matrix"))
                .findFirst()
                .orElseThrow();
        firstCommand.remove("auditJoin");
        assertThrows(IllegalArgumentException.class,
                () -> UniversalCommandMatrixGenerator.validate(matrix));
    }

    @Test
    void matrixConsumesOnlyCandidateBoundCatalogRuntimeEvidence() throws Exception {
        Path evidence = temporaryDirectory.resolve("runtime-evidence");
        Files.createDirectories(evidence);
        JsonObject record = new JsonObject();
        record.addProperty("schemaVersion", 1);
        record.addProperty("candidateCommit", "0123456789abcdef0123456789abcdef01234567");
        record.addProperty("candidateSha256", "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
        record.addProperty("source", "test");
        record.addProperty("rowCount", 1);
        JsonObject row = new JsonObject();
        row.addProperty("actionId", "sef:core.info");
        row.addProperty("canonicalRoute", "sef info");
        row.addProperty("commandDigest", "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
        row.addProperty("result", "success");
        row.addProperty("auditEventCount", 1);
        row.addProperty("sourceType", "console");
        row.addProperty("auditResult", "success");
        row.addProperty("auditClass", "metadata_only");
        row.addProperty("redactionClass", "metadata");
        record.add("rows", new com.google.gson.JsonArray());
        record.getAsJsonArray("rows").add(row);
        Files.writeString(
                evidence.resolve("catalog-console-runtime.json"),
                record.toString(),
                StandardCharsets.UTF_8);
        JsonObject argumentRecord = record.deepCopy();
        JsonObject argumentRow = row.deepCopy();
        argumentRow.addProperty("actionId", "sef:gui.client.status");
        argumentRow.addProperty("canonicalRoute", "sef client status");
        argumentRow.addProperty("redactionSafe", true);
        argumentRecord.getAsJsonArray("rows").remove(0);
        argumentRecord.getAsJsonArray("rows").add(argumentRow);
        Files.writeString(
                evidence.resolve("catalog-console-argument-runtime.json"),
                argumentRecord.toString(),
                StandardCharsets.UTF_8);
        String oldRoot = System.getProperty("sef.audit.evidenceRoot");
        String oldCommit = System.getProperty("sef.audit.candidateCommit");
        String oldSha256 = System.getProperty("sef.audit.candidateSha256");
        try {
            System.setProperty("sef.audit.evidenceRoot", evidence.toString());
            System.setProperty("sef.audit.candidateCommit", record.get("candidateCommit").getAsString());
            System.setProperty("sef.audit.candidateSha256", record.get("candidateSha256").getAsString());
            JsonObject matrix = UniversalCommandMatrixGenerator.generate();
            JsonObject action = matrix.getAsJsonArray("rows").asList().stream()
                    .map(JsonElement::getAsJsonObject)
                    .filter(value -> value.get("semanticKey").getAsString().equals("sef:core.info"))
                    .findFirst()
                    .orElseThrow();
            assertEquals("pass", action.getAsJsonObject("dimensions")
                    .getAsJsonObject("audit").get("status").getAsString());
            assertEquals("pass", action.getAsJsonObject("dimensions")
                    .getAsJsonObject("linux_shared_runtime").get("status").getAsString());
            JsonObject argumentAction = matrix.getAsJsonArray("rows").asList().stream()
                    .map(JsonElement::getAsJsonObject)
                    .filter(value -> value.get("semanticKey").getAsString().equals("sef:gui.client.status"))
                    .findFirst()
                    .orElseThrow();
            assertEquals("pass", argumentAction.getAsJsonObject("dimensions")
                    .getAsJsonObject("redaction").get("status").getAsString());
            assertEquals(
                    "catalog-console-argument-runtime.json",
                    argumentAction.getAsJsonObject("dimensions")
                            .getAsJsonObject("redaction")
                            .getAsJsonArray("evidence")
                            .get(0)
                            .getAsString());
        } finally {
            restoreProperty("sef.audit.evidenceRoot", oldRoot);
            restoreProperty("sef.audit.candidateCommit", oldCommit);
            restoreProperty("sef.audit.candidateSha256", oldSha256);
        }
    }

    private static void restoreProperty(String key, String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }
}
