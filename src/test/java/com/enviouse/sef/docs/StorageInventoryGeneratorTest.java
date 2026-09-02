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

class StorageInventoryGeneratorTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void generatedInventoryReconcilesImplementationsRegistrationsAndWriters() throws Exception {
        JsonObject inventory = StorageInventoryGenerator.generate(repositoryRoot());
        AuditEvidenceContract.validateInventorySet(inventory.getAsJsonArray("inventoryRows"));
        assertTrue(inventory.get("implementationCount").getAsInt() >= 30);
        assertTrue(inventory.get("runtimeRegisteredCount").getAsInt() > 0);
        assertTrue(inventory.get("durableWriterCount").getAsInt() > 0);
        assertTrue(inventory.get("nonRepositoryOwnerCount").getAsInt() >= 8);
        assertEquals(
                inventory.get("rows").getAsInt(),
                inventory.getAsJsonArray("inventoryRows").size());
    }

    @Test
    void generationIsDeterministicAndPreservesAllStorageRows() throws Exception {
        JsonObject first = StorageInventoryGenerator.generate(repositoryRoot());
        JsonObject second = StorageInventoryGenerator.generate(repositoryRoot());
        assertEquals(first.toString(), second.toString());
        Path output = StorageInventoryGenerator.write(
                temporaryDirectory.resolve("inventory"), "storage.json", repositoryRoot());
        JsonObject persisted = JsonParser.parseString(
                Files.readString(output, StandardCharsets.UTF_8)).getAsJsonObject();
        assertEquals(
                first.getAsJsonArray("inventoryRows").size(),
                persisted.getAsJsonArray("rows").size());
        assertTrue(persisted.get("inventoryRows") == null);

        String evidenceRoot = System.getProperty("sef.audit.evidenceRoot", "").trim();
        if (!evidenceRoot.isEmpty()) {
            StorageInventoryGenerator.write(Path.of(evidenceRoot), "storage-inventory.json", repositoryRoot());
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
