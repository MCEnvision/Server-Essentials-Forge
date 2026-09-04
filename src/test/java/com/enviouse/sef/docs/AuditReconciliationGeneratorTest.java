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

class AuditReconciliationGeneratorTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void reconciliationJoinsAllInventoriesAndForeignKeys() throws Exception {
        JsonObject inventory = AuditReconciliationGenerator.generate(repositoryRoot());
        AuditEvidenceContract.validateInventorySet(inventory.getAsJsonArray("rows"));
        assertEquals(8, inventory.get("sourceInventoryCount").getAsInt());
        assertTrue(inventory.get("reconciledRowCount").getAsInt() > 0);
        assertTrue(inventory.get("foreignKeyCheckCount").getAsInt() > 0);
        assertEquals(0, inventory.get("foreignKeyFailureCount").getAsInt());
        assertEquals(0, inventory.get("duplicateIdentityCount").getAsInt());
        assertEquals(0, inventory.get("unownedRowCount").getAsInt());
        assertEquals("complete", inventory.get("traceabilityStatus").getAsString());
    }

    @Test
    void generationIsDeterministicAndPreservesAllReconciledRows() throws Exception {
        JsonObject first = AuditReconciliationGenerator.generate(repositoryRoot());
        JsonObject second = AuditReconciliationGenerator.generate(repositoryRoot());
        assertEquals(first.toString(), second.toString());
        Path output = AuditReconciliationGenerator.write(
                temporaryDirectory.resolve("inventory"), "reconciliation.json", repositoryRoot());
        JsonObject persisted = JsonParser.parseString(
                Files.readString(output, StandardCharsets.UTF_8)).getAsJsonObject();
        assertEquals(first.getAsJsonArray("rows").size(), persisted.getAsJsonArray("rows").size());

        String evidenceRoot = System.getProperty("sef.audit.evidenceRoot", "").trim();
        if (!evidenceRoot.isEmpty()) {
            AuditReconciliationGenerator.write(Path.of(evidenceRoot), "reconciliation.json", repositoryRoot());
            SecurityFindingLedgerGenerator.write(Path.of(evidenceRoot), "security-finding-ledger.json");
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
