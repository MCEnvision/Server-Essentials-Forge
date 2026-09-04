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

class SecurityFindingLedgerGeneratorTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void ledgerContainsStableClosedFindingsAndExternalBlockers() {
        JsonObject ledger = SecurityFindingLedgerGenerator.generate();

        assertEquals("SEFAUD-PHASE-001", ledger.get("phase").getAsString());
        assertEquals(5, ledger.get("findingCount").getAsInt());
        assertEquals(5, ledger.get("closedFindingCount").getAsInt());
        assertEquals(0, ledger.get("openFindingCount").getAsInt());
        assertEquals(2, ledger.get("externalBlockerCount").getAsInt());
        AuditEvidenceContract.validateInventorySet(ledger.getAsJsonArray("rows"));
        assertEquals(5, ledger.getAsJsonArray("rows").size());
        assertTrue(ledger.getAsJsonArray("rows").get(0).getAsJsonObject().has("sinkLocations"));
    }

    @Test
    void writeUsesTheSanitizedExternalEvidenceContract() throws Exception {
        Path output = SecurityFindingLedgerGenerator.write(
                temporaryDirectory.resolve("evidence"),
                "security-finding-ledger.json");
        JsonObject persisted = JsonParser.parseString(
                Files.readString(output, StandardCharsets.UTF_8)).getAsJsonObject();

        assertEquals("sef-security-finding-ledger-p001", persisted.get("inventoryId").getAsString());
        assertEquals(5, persisted.getAsJsonArray("rows").size());
        AuditEvidenceContract.validateInventorySet(persisted.getAsJsonArray("rows"));
    }
}
