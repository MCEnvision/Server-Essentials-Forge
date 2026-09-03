package com.enviouse.sef.docs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoundaryInventoryGeneratorTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void inventoriesEveryTrustBoundaryWithTraceability() throws Exception {
        JsonObject inventory = BoundaryInventoryGenerator.generate(repositoryRoot());
        AuditEvidenceContract.validateInventorySet(inventory.getAsJsonArray("rows"));
        AuditDriftValidator.requireTraceability(inventory.getAsJsonArray("rows"));
        assertEquals(23, inventory.get("boundaryCount").getAsInt());
        assertEquals(23, inventory.getAsJsonArray("rows").size());
        JsonObject nativeWriter = inventory.getAsJsonArray("rows").asList().stream()
                .map(element -> element.getAsJsonObject())
                .filter(row -> row.get("semanticKey").getAsString().equals("opened-object-writer"))
                .findFirst()
                .orElse(null);
        assertNotNull(nativeWriter);
        assertFalse(nativeWriter.get("sourceAvailable").getAsBoolean());
        assertEquals("finding", nativeWriter.get("disposition").getAsString());
    }

    @Test
    void generationIsDeterministicAndWritesTheCompleteBoundarySet() throws Exception {
        JsonObject first = BoundaryInventoryGenerator.generate(repositoryRoot());
        JsonObject second = BoundaryInventoryGenerator.generate(repositoryRoot());
        assertEquals(first.toString(), second.toString());
        Path output = BoundaryInventoryGenerator.write(
                temporaryDirectory.resolve("inventory"), "boundary.json", repositoryRoot());
        JsonObject persisted = JsonParser.parseString(
                Files.readString(output, StandardCharsets.UTF_8)).getAsJsonObject();
        assertEquals(23, persisted.getAsJsonArray("rows").size());
        assertTrue(persisted.getAsJsonArray("rows").asList().stream()
                .allMatch(row -> row.getAsJsonObject().has("evidenceRequired")));

        String evidenceRoot = System.getProperty("sef.audit.evidenceRoot", "").trim();
        if (!evidenceRoot.isEmpty()) {
            BoundaryInventoryGenerator.write(Path.of(evidenceRoot), "boundary-inventory.json", repositoryRoot());
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
