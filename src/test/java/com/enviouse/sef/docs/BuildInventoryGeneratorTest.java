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

class BuildInventoryGeneratorTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void generatedInventoryCoversBuildDependenciesResourcesCiAndRemoteSnapshot() throws Exception {
        JsonObject inventory = BuildInventoryGenerator.generate(repositoryRoot());
        AuditEvidenceContract.validateInventorySet(inventory.getAsJsonArray("rows"));
        assertTrue(inventory.get("dependencyDeclarationCount").getAsInt() > 0);
        assertTrue(inventory.get("buildTaskCount").getAsInt() > 0);
        assertTrue(inventory.get("artifactInputCount").getAsInt() > 0);
        assertTrue(inventory.get("resourceInputCount").getAsInt() > 0);
        assertTrue(inventory.get("workflowCount").getAsInt() >= 0);
        assertTrue(inventory.get("platformDependencyDeclarationCount").getAsInt() >= 0);
        assertEquals(2, inventory.get("platformDependencyExpectedCount").getAsInt());
        assertEquals(2 - inventory.get("platformDependencyDeclarationCount").getAsInt(),
                inventory.get("platformDependencyMissingCount").getAsInt());
        assertEquals(9, inventory.get("platformDependencyGateCount").getAsInt());
        assertEquals("blocked, read-only", inventory.get("remoteSecuritySnapshot").getAsString());
    }

    @Test
    void generationIsDeterministicAndPreservesAllBuildRows() throws Exception {
        JsonObject first = BuildInventoryGenerator.generate(repositoryRoot());
        JsonObject second = BuildInventoryGenerator.generate(repositoryRoot());
        assertEquals(first.toString(), second.toString());
        Path output = BuildInventoryGenerator.write(
                temporaryDirectory.resolve("inventory"), "build.json", repositoryRoot());
        JsonObject persisted = JsonParser.parseString(
                Files.readString(output, StandardCharsets.UTF_8)).getAsJsonObject();
        assertEquals(first.getAsJsonArray("rows").size(), persisted.getAsJsonArray("rows").size());

        String evidenceRoot = System.getProperty("sef.audit.evidenceRoot", "").trim();
        if (!evidenceRoot.isEmpty()) {
            BuildInventoryGenerator.write(Path.of(evidenceRoot), "build-inventory.json", repositoryRoot());
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
