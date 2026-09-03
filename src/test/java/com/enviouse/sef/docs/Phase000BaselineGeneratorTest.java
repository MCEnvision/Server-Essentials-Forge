package com.enviouse.sef.docs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Phase000BaselineGeneratorTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void capturesStableCommitLineagePlatformAndPrerequisiteContract() throws Exception {
        JsonObject first = Phase000BaselineGenerator.generate(repositoryRoot());
        JsonObject second = Phase000BaselineGenerator.generate(repositoryRoot());
        assertEquals(first.toString(), second.toString());
        assertEquals("SEFAUD-PHASE-000", first.get("phase").getAsString());
        assertEquals("P000-TASK-001", first.get("task").getAsString());
        assertTrue(first.has("lineageBasePresent"));
        assertFalse(first.get("legacyForgeLineImported").getAsBoolean());
        assertTrue(first.has("trackedTreeClean"));
        if (Files.exists(repositoryRoot().resolve(".playwright-mcp"))) {
            assertTrue(first.get("preservedPlaywrightState").getAsBoolean());
        }

        Set<String> platforms = first.getAsJsonArray("rows").asList().stream()
                .map(value -> value.getAsJsonObject())
                .filter(row -> row.get("category").getAsString().equals("operating-system"))
                .map(row -> row.get("semanticKey").getAsString())
                .collect(Collectors.toSet());
        assertEquals(Set.of("linux", "macos", "windows"), platforms);
        assertEquals(2, first.get("externalPrerequisiteCount").getAsInt());
        assertEquals("unknown, dependent evidence blocked", first.get("externalPrerequisiteState").getAsString());

        JsonObject modMetadata = first.getAsJsonArray("rows").asList().stream()
                .map(value -> value.getAsJsonObject())
                .filter(row -> row.get("category").getAsString().equals("artifact-input"))
                .filter(row -> row.get("semanticKey").getAsString()
                        .equals("src/main/templates/META-INF/neoforge.mods.toml"))
                .findFirst()
                .orElseThrow();
        assertEquals("present", modMetadata.get("availability").getAsString());
        assertEquals("available", modMetadata.get("dependentEvidence").getAsString());
    }

    @Test
    void writesValidatedBaselineToTheConfiguredExternalEvidenceRoot() throws Exception {
        Path output = Phase000BaselineGenerator.write(
                temporaryDirectory.resolve("evidence"), "phase000-baseline.json", repositoryRoot());
        JsonObject persisted = JsonParser.parseString(
                Files.readString(output, StandardCharsets.UTF_8)).getAsJsonObject();
        AuditEvidenceContract.validateInventorySet(persisted.getAsJsonArray("rows"));
        assertEquals("sef-phase000-execution-baseline", persisted.get("inventoryId").getAsString());
        assertTrue(persisted.getAsJsonArray("rows").size() > 10);
    }

    @Test
    void writesTheBaselineWhenTheAuditEvidencePropertyIsSet() throws Exception {
        String evidenceRoot = System.getProperty("sef.audit.evidenceRoot", "").trim();
        if (!evidenceRoot.isEmpty()) {
            Path output = Phase000BaselineGenerator.write(
                    Path.of(evidenceRoot), "phase000-baseline.json", repositoryRoot());
            assertTrue(Files.isRegularFile(output));
        }
    }

    private static Path repositoryRoot() {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("settings.gradle"))
                    && Files.isDirectory(current.resolve("src"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("repository root is unavailable");
    }
}
