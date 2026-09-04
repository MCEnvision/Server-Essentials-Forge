package com.enviouse.sef.docs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandInventoryGeneratorTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void generatedInventoryJoinsActionsRoutesShortcutsPermissionsAndNegativeFamilies() {
        JsonObject inventory = CommandInventoryGenerator.generate();
        JsonArray rows = inventory.getAsJsonArray("rows");
        AuditEvidenceContract.validateInventorySet(rows);

        Set<String> unavailable = new HashSet<>();
        int actions = 0;
        int routes = 0;
        int shortcuts = 0;
        for (var element : rows) {
            JsonObject row = element.getAsJsonObject();
            switch (row.get("category").getAsString()) {
                case "command" -> actions++;
                case "route" -> routes++;
                case "shortcut" -> shortcuts++;
                case "unavailable" -> unavailable.add(row.get("semanticKey").getAsString());
                default -> {
                }
            }
        }
        assertEquals(inventory.get("catalogActionCount").getAsInt(), actions);
        assertEquals(inventory.get("routeCount").getAsInt(), routes);
        assertEquals(inventory.get("shortcutCount").getAsInt(), shortcuts);
        assertEquals(CommandInventoryGenerator.UNAVAILABLE_FAMILIES, unavailable);
        assertTrue(actions > 0);
        assertTrue(routes > 0);
        assertTrue(shortcuts > 0);
        assertTrue(inventory.toString().contains("\"semanticKey\":\"admin_journal\""));
        assertTrue(inventory.toString().contains("\"disposition\":\"excluded\""));
    }

    @Test
    void executorCallsitesAreRecordedAndUnknownLiteralActionsFailTheInventory() {
        JsonObject inventory = CommandInventoryGenerator.generate();
        assertTrue(
                inventory.get("pipelineCallSiteCount").getAsInt() >= 100,
                "expected at least 100 executor callsites, found "
                        + inventory.get("pipelineCallSiteCount").getAsInt());
        assertTrue(inventory.get("dynamicPipelineCallSiteCount").getAsInt() > 0);
        assertTrue(inventory.get("literalPipelineActionCount").getAsInt() > 0);
        assertEquals(
                inventory.get("dynamicPipelineCallSiteCount").getAsInt(),
                inventory.getAsJsonArray("dynamicPipelineCallSites").size());
        assertEquals(
                inventory.get("pipelineCallSiteCount").getAsInt(),
                inventory.get("literalPipelineCallSiteCount").getAsInt()
                        + inventory.get("dynamicPipelineCallSiteCount").getAsInt());
        for (var element : inventory.getAsJsonArray("rows")) {
            JsonObject row = element.getAsJsonObject();
            if (row.get("category").getAsString().equals("command")) {
                assertTrue(row.has("pipelineCallSites"));
                assertTrue(row.has("pipelineCallSiteDisposition"));
            }
        }
    }

    @Test
    void generationIsDeterministicAndCanWriteOnlyToExternalRoot() throws Exception {
        JsonObject first = CommandInventoryGenerator.generate();
        JsonObject second = CommandInventoryGenerator.generate();
        assertEquals(first.toString(), second.toString());

        Path output = CommandInventoryGenerator.write(temporaryDirectory.resolve("inventory"), "commands.json");
        assertTrue(Files.isRegularFile(output));
        JsonObject persisted = JsonParser.parseString(
                Files.readString(output, StandardCharsets.UTF_8)).getAsJsonObject();
        assertEquals(first.getAsJsonArray("rows").size(), persisted.getAsJsonArray("rows").size());

        String evidenceRoot = System.getProperty("sef.audit.evidenceRoot", "").trim();
        if (!evidenceRoot.isEmpty()) {
            CommandInventoryGenerator.write(Path.of(evidenceRoot), "command-inventory.json");
        }
    }
}
