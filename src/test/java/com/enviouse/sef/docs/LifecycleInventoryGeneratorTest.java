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

class LifecycleInventoryGeneratorTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void generatedInventoryCoversLifecycleSidesIntegrationsAndConfigurationSchemas() throws Exception {
        JsonObject inventory = LifecycleInventoryGenerator.generate(repositoryRoot());
        AuditEvidenceContract.validateInventorySet(inventory.getAsJsonArray("rows"));
        assertTrue(inventory.get("lifecycleHookCount").getAsInt() > 0);
        assertTrue(inventory.get("transientOwnerCount").getAsInt() > 0);
        assertTrue(inventory.get("integrationOwnerCount").getAsInt() > 0);
        assertTrue(inventory.get("clientSourceFileCount").getAsInt() > 0);
        assertTrue(inventory.get("commonOrServerSourceFileCount").getAsInt() > 0);
        assertTrue(inventory.get("configurationModuleCount").getAsInt() >= 60);
        assertTrue(inventory.get("configurationFieldCount").getAsInt() > 0);
        assertEquals(inventory.get("rowCount").getAsInt(), inventory.getAsJsonArray("rows").size());
    }

    @Test
    void generationIsDeterministicAndPreservesAllLifecycleRows() throws Exception {
        JsonObject first = LifecycleInventoryGenerator.generate(repositoryRoot());
        JsonObject second = LifecycleInventoryGenerator.generate(repositoryRoot());
        assertEquals(first.toString(), second.toString());
        Path output = LifecycleInventoryGenerator.write(
                temporaryDirectory.resolve("inventory"), "lifecycle.json", repositoryRoot());
        JsonObject persisted = JsonParser.parseString(
                Files.readString(output, StandardCharsets.UTF_8)).getAsJsonObject();
        assertEquals(first.getAsJsonArray("rows").size(), persisted.getAsJsonArray("rows").size());

        String evidenceRoot = System.getProperty("sef.audit.evidenceRoot", "").trim();
        if (!evidenceRoot.isEmpty()) {
            LifecycleInventoryGenerator.write(Path.of(evidenceRoot), "lifecycle-inventory.json", repositoryRoot());
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
