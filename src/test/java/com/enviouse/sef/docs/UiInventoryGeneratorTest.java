package com.enviouse.sef.docs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiInventoryGeneratorTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void generatedInventoryCoversUiProtocolFallbackFeedbackAndUnavailableRows() throws Exception {
        JsonObject inventory = UiInventoryGenerator.generate(repositoryRoot());
        JsonArray rows = inventory.getAsJsonArray("rows");
        AuditEvidenceContract.validateInventorySet(rows);
        assertTrue(inventory.get("screenCount").getAsInt() > 0);
        assertTrue(inventory.get("menuCount").getAsInt() > 0);
        assertTrue(inventory.get("payloadCount").getAsInt() > 0);
        assertTrue(inventory.get("descriptorCount").getAsInt() > 0);
        assertTrue(inventory.get("controlCount").getAsInt() > 0);
        assertTrue(inventory.get("hudCount").getAsInt() > 0);
        assertTrue(inventory.get("feedbackCount").getAsInt() > 0);
        assertEquals(16, inventory.get("unavailableFamilyCount").getAsInt());
        for (var element : rows) {
            JsonObject row = element.getAsJsonObject();
            assertFalse(row.getAsJsonArray("sourceLocations").get(0).getAsString().startsWith("/"));
            if (row.get("category").getAsString().equals("control")) {
                assertTrue(row.has("actionId"));
                assertTrue(row.has("permissionId"));
            }
        }
    }

    @Test
    void generationIsDeterministicAndPreservesAllRowsWhenWritten() throws Exception {
        JsonObject first = UiInventoryGenerator.generate(repositoryRoot());
        JsonObject second = UiInventoryGenerator.generate(repositoryRoot());
        assertEquals(first.toString(), second.toString());
        Path output = UiInventoryGenerator.write(
                temporaryDirectory.resolve("inventory"), "ui.json", repositoryRoot());
        JsonObject persisted = com.google.gson.JsonParser.parseString(
                Files.readString(output, StandardCharsets.UTF_8)).getAsJsonObject();
        assertEquals(first.getAsJsonArray("rows").size(), persisted.getAsJsonArray("rows").size());

        String evidenceRoot = System.getProperty("sef.audit.evidenceRoot", "").trim();
        if (!evidenceRoot.isEmpty()) {
            UiInventoryGenerator.write(Path.of(evidenceRoot), "ui-inventory.json", repositoryRoot());
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
