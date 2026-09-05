package com.enviouse.sef.docs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestInventoryGeneratorTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void generatedInventoryCoversTestsFixturesWorkflowsClaimsAndGaps() throws Exception {
        JsonObject inventory = TestInventoryGenerator.generate(repositoryRoot());
        AuditEvidenceContract.validateInventorySet(inventory.getAsJsonArray("rows"));
        assertTrue(inventory.get("testSourceFileCount").getAsInt() > 0);
        assertTrue(inventory.get("unitTestCount").getAsInt() > 0);
        assertEquals(51, inventory.get("gameTestCount").getAsInt());
        assertTrue(inventory.get("fixtureCount").getAsInt() > 0);
        assertTrue(inventory.get("workflowCount").getAsInt() > 0);
        assertTrue(inventory.get("generatedReferenceCount").getAsInt() > 0);
        assertTrue(inventory.get("documentationClaimCount").getAsInt() > 0);
        assertTrue(inventory.get("priorFindingCount").getAsInt() > 0);
        assertTrue(inventory.get("evidenceGapCount").getAsInt() > 0);
    }

    @Test
    void generationIsDeterministicAndPreservesAllTestRows() throws Exception {
        JsonObject first = TestInventoryGenerator.generate(repositoryRoot());
        JsonObject second = TestInventoryGenerator.generate(repositoryRoot());
        assertEquals(first.toString(), second.toString());
        Path output = TestInventoryGenerator.write(
                temporaryDirectory.resolve("inventory"), "tests.json", repositoryRoot());
        JsonObject persisted = JsonParser.parseString(
                Files.readString(output, StandardCharsets.UTF_8)).getAsJsonObject();
        assertEquals(first.getAsJsonArray("rows").size(), persisted.getAsJsonArray("rows").size());

        String evidenceRoot = System.getProperty("sef.audit.evidenceRoot", "").trim();
        if (!evidenceRoot.isEmpty()) {
            TestInventoryGenerator.write(Path.of(evidenceRoot), "test-inventory.json", repositoryRoot());
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
