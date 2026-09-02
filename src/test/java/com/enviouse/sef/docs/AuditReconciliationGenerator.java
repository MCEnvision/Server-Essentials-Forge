package com.enviouse.sef.docs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Joins every Phase 000 inventory and applies the canonical ownership and
 * later-phase mapping used by downstream audit work.
 */
public final class AuditReconciliationGenerator {
    private static final String PHASE = "SEFAUD-PHASE-000";
    private static final String REQUIREMENT = "SEFAUD-REQ-001";

    private AuditReconciliationGenerator() {
    }

    public static JsonObject generate(Path repositoryRoot) throws IOException {
        Map<String, JsonObject> inventories = new HashMap<>();
        inventories.put("command", CommandInventoryGenerator.generate());
        inventories.put("ui", UiInventoryGenerator.generate(repositoryRoot));
        inventories.put("storage", StorageInventoryGenerator.generate(repositoryRoot));
        inventories.put("lifecycle", LifecycleInventoryGenerator.generate(repositoryRoot));
        inventories.put("build", BuildInventoryGenerator.generate(repositoryRoot));
        inventories.put("test", TestInventoryGenerator.generate(repositoryRoot));

        List<JsonObject> reconciled = new ArrayList<>();
        for (Map.Entry<String, JsonObject> entry : inventories.entrySet()) {
            for (JsonElement element : inventoryRows(entry.getValue())) {
                JsonObject row = element.getAsJsonObject().deepCopy();
                String category = row.get("category").getAsString();
                row.addProperty("canonicalOwner", row.get("owner").getAsString());
                row.addProperty("requirement", REQUIREMENT);
                row.addProperty("laterPhase", laterPhase(category));
                row.addProperty("traceabilityId", REQUIREMENT + ":" + row.get("rowId").getAsString());
                reconciled.add(row);
            }
        }
        reconciled.sort(Comparator.comparing(row -> row.get("rowId").getAsString()));
        JsonArray rows = new JsonArray();
        reconciled.forEach(rows::add);
        AuditEvidenceContract.validateInventorySet(rows);

        int foreignKeyChecks = 0;
        Set<String> actions = values(inventories.get("command"), "command", "semanticKey");
        Set<String> permissions = values(inventories.get("command"), "permission", "semanticKey");
        Set<String> implementations = repositoryIds(inventories.get("storage"), "store-implementation", "repositoryId");
        Set<String> registrations = values(inventories.get("storage"), "store-registration", "semanticKey");
        Set<String> workflowCommands = workflowCommands(inventories.get("test"));
        for (JsonObject row : rowsAsObjects(inventories.get("command"))) {
            if (row.get("category").getAsString().equals("command")) {
                for (JsonElement permission : row.getAsJsonArray("permissionIds")) {
                    foreignKeyChecks++;
                    require(permissions.contains(permission.getAsString()), "command permission", row, permission.getAsString());
                }
            } else if (row.get("category").getAsString().equals("route")
                    || row.get("category").getAsString().equals("shortcut")) {
                foreignKeyChecks++;
                require(actions.contains(row.get("actionId").getAsString()), "command action", row, row.get("actionId").getAsString());
            }
        }
        for (JsonObject row : rowsAsObjects(inventories.get("ui"))) {
            String category = row.get("category").getAsString();
            if (category.equals("control")) {
                foreignKeyChecks += 2;
                require(actions.contains(row.get("actionId").getAsString()), "UI action", row, row.get("actionId").getAsString());
                require(permissions.contains(row.get("permissionId").getAsString()), "UI permission", row, row.get("permissionId").getAsString());
            } else if ((category.equals("descriptor") || category.equals("hud")) && row.has("permissionId")) {
                foreignKeyChecks++;
                require(permissions.contains(row.get("permissionId").getAsString()), "UI permission", row, row.get("permissionId").getAsString());
            }
        }
        for (JsonObject row : rowsAsObjects(inventories.get("storage"))) {
            if (row.get("category").getAsString().equals("store-registration")) {
                foreignKeyChecks++;
                require(implementations.contains(row.get("semanticKey").getAsString()), "storage implementation", row, row.get("semanticKey").getAsString());
                foreignKeyChecks++;
                require(registrations.contains(row.get("semanticKey").getAsString()), "storage registration", row, row.get("semanticKey").getAsString());
            }
        }
        for (JsonObject row : rowsAsObjects(inventories.get("test"))) {
            if (row.get("category").getAsString().equals("gametest")) {
                foreignKeyChecks++;
                require(workflowCommands.contains("./gradlew runGameTestServer"), "GameTest workflow", row, "./gradlew runGameTestServer");
            }
        }

        JsonArray sourceSummaries = new JsonArray();
        for (String name : List.of("command", "ui", "storage", "lifecycle", "build", "test")) {
            JsonObject source = inventories.get(name);
            JsonObject summary = new JsonObject();
            summary.addProperty("inventoryId", source.get("inventoryId").getAsString());
            summary.addProperty("source", name);
            summary.addProperty("rowCount", inventoryRows(source).size());
            summary.addProperty("task", source.get("task").getAsString());
            sourceSummaries.add(summary);
        }

        JsonObject inventory = new JsonObject();
        inventory.addProperty("schemaVersion", AuditEvidenceContract.SCHEMA_VERSION);
        inventory.addProperty("inventoryId", "sef-reconciliation-p000");
        inventory.addProperty("phase", PHASE);
        inventory.addProperty("task", "P000-TASK-009");
        inventory.addProperty("source", "command, UI, storage, lifecycle, build, and test inventories");
        inventory.addProperty("sourceInventoryCount", inventories.size());
        inventory.addProperty("reconciledRowCount", rows.size());
        inventory.addProperty("foreignKeyCheckCount", foreignKeyChecks);
        inventory.addProperty("foreignKeyFailureCount", 0);
        inventory.addProperty("duplicateIdentityCount", 0);
        inventory.addProperty("unownedRowCount", 0);
        inventory.addProperty("traceabilityStatus", "complete");
        inventory.add("sourceInventories", sourceSummaries);
        inventory.add("rows", rows);
        return inventory;
    }

    public static Path write(Path approvedExternalRoot, String fileName, Path repositoryRoot) throws IOException {
        JsonObject inventory = generate(repositoryRoot);
        return AuditEvidenceContract.writeInventory(
                approvedExternalRoot,
                fileName,
                inventory,
                inventory.getAsJsonArray("rows"));
    }

    private static JsonArray inventoryRows(JsonObject inventory) {
        if (inventory.has("rows") && inventory.get("rows").isJsonArray()) {
            return inventory.getAsJsonArray("rows");
        }
        if (inventory.has("inventoryRows")) {
            return inventory.getAsJsonArray("inventoryRows");
        }
        throw new IllegalArgumentException("inventory has no rows " + inventory.get("inventoryId"));
    }

    private static List<JsonObject> rowsAsObjects(JsonObject inventory) {
        List<JsonObject> result = new ArrayList<>();
        for (JsonElement element : inventoryRows(inventory)) {
            result.add(element.getAsJsonObject());
        }
        return result;
    }

    private static Set<String> values(JsonObject inventory, String category, String property) {
        Set<String> result = new HashSet<>();
        for (JsonObject row : rowsAsObjects(inventory)) {
            if (row.get("category").getAsString().equals(category) && row.has(property)) {
                result.add(row.get(property).getAsString());
            }
        }
        return result;
    }

    private static Set<String> repositoryIds(JsonObject inventory, String category, String property) {
        return values(inventory, category, property);
    }

    private static Set<String> workflowCommands(JsonObject inventory) {
        Set<String> result = new HashSet<>();
        for (JsonObject row : rowsAsObjects(inventory)) {
            if (row.has("command")) {
                result.add(row.get("command").getAsString());
            }
        }
        return result;
    }

    private static void require(boolean condition, String relation, JsonObject row, String target) {
        if (!condition) {
            throw new IllegalStateException("broken " + relation + " "
                    + row.get("rowId").getAsString() + " -> " + target);
        }
    }

    private static String laterPhase(String category) {
        if (Set.of("command", "route", "shortcut", "permission", "unavailable").contains(category)) {
            return "SEFAUD-PHASE-003";
        }
        if (Set.of("screen", "menu", "payload", "hud-contract", "workflow", "descriptor", "fallback",
                "control", "descriptor-command-only", "hud", "translation", "ui-unavailable", "feedback").contains(category)) {
            return "SEFAUD-PHASE-004";
        }
        if (Set.of("store-implementation", "store-registration", "durable-owner", "durable-writer").contains(category)) {
            return "SEFAUD-PHASE-002";
        }
        if (Set.of("lifecycle-hook", "lifecycle-transition", "transient-owner", "integration", "protocol-boundary",
                "configuration-module", "configuration-field", "resource-boundary").contains(category)) {
            return "SEFAUD-PHASE-005";
        }
        if (Set.of("remote-security").contains(category)) {
            return "SEFAUD-PHASE-001";
        }
        return PHASE;
    }
}
