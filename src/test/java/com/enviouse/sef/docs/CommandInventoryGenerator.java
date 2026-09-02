package com.enviouse.sef.docs;

import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.permissions.PermissionManifest;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Derives the Phase 000 command, route, shortcut, permission, and unavailable
 * control inventory from the sealed runtime registries.
 */
public final class CommandInventoryGenerator {
    public static final Set<String> UNAVAILABLE_FAMILIES = Set.of(
            "admin_journal",
            "afk_zones",
            "approvals",
            "capability_leases",
            "chat_channels",
            "display_ownership",
            "display_profiles",
            "player_warp_review",
            "portal_policy",
            "resource_governor",
            "resource_worlds",
            "rollouts",
            "server_presentation",
            "spawn_ecology",
            "staff_duty",
            "waypoints");

    private CommandInventoryGenerator() {
    }

    public static JsonObject generate() {
        KernelServices.initialize();
        JsonArray rows = new JsonArray();
        Set<String> actionIds = new TreeSet<>();
        for (CommandDefinition definition : KernelServices.catalog().entries()) {
            actionIds.add(definition.id());
            JsonObject row = row(
                    "command",
                    definition.id(),
                    "SEFAUD-PHASE-003",
                    "runtime",
                    "implemented",
                    "src/main/java/com/enviouse/sef/kernel/KernelServices.java");
            row.addProperty("canonicalRoute", definition.canonicalRoute());
            row.addProperty("featureId", definition.featureId());
            row.addProperty("accessClass", definition.accessClass().name().toLowerCase());
            row.addProperty("auditClass", definition.auditClass().name().toLowerCase());
            row.addProperty("targetBehavior", definition.targetBehavior().name().toLowerCase());
            row.addProperty("pipelineEnforced", definition.pipelineEnforced());
            row.add("permissionIds", strings(definition.permissionIds()));
            row.add("sourceTypes", strings(definition.sourceTypes().stream()
                    .map(value -> value.name().toLowerCase())
                    .toList()));
            row.add("convenienceRoots", strings(definition.convenienceRoots()));
            rows.add(row);
        }

        for (Map.Entry<String, String> route : KernelServices.catalog().routes().entrySet()) {
            if (!actionIds.contains(route.getValue())) {
                throw new IllegalStateException("orphan live route " + route.getKey());
            }
            JsonObject row = row(
                    "route",
                    route.getKey(),
                    "SEFAUD-PHASE-003",
                    "runtime",
                    "implemented",
                    "src/main/java/com/enviouse/sef/kernel/command/CommandCatalog.java");
            row.addProperty("actionId", route.getValue());
            rows.add(row);
        }

        for (var shortcut : KernelServices.shortcuts().entries()) {
            if (!actionIds.contains(shortcut.actionId())) {
                throw new IllegalStateException("orphan shortcut " + shortcut.root());
            }
            JsonObject row = row(
                    "shortcut",
                    shortcut.root(),
                    "SEFAUD-PHASE-003",
                    "runtime",
                    "implemented",
                    "src/main/java/com/enviouse/sef/kernel/command/ShortcutRegistry.java");
            row.addProperty("actionId", shortcut.actionId());
            row.addProperty("adapter", shortcut.adapter().name().toLowerCase());
            row.addProperty("collisionMode", shortcut.collisionMode().name().toLowerCase());
            row.addProperty("structuralRevision", shortcut.structuralRevision());
            rows.add(row);
        }

        for (PermissionManifest.Definition permission : PermissionManifest.definitions()) {
            JsonObject row = row(
                    "permission",
                    permission.id(),
                    "SEFAUD-PHASE-003",
                    "static",
                    "implemented",
                    "src/main/java/com/enviouse/sef/permissions/PermissionManifest.java");
            row.addProperty("default", permission.defaultValue());
            row.addProperty("name", permission.name());
            rows.add(row);
        }

        for (String family : new TreeSet<>(UNAVAILABLE_FAMILIES)) {
            JsonObject row = row(
                    "unavailable",
                    family,
                    "SEFAUD-PHASE-003",
                    "negative",
                    "excluded",
                    "docs/general/plan.md");
            row.addProperty("runtimeHandler", false);
            row.addProperty("capabilityAdvertised", false);
            rows.add(row);
        }

        AuditEvidenceContract.validateInventorySet(rows);
        JsonObject inventory = new JsonObject();
        inventory.addProperty("schemaVersion", AuditEvidenceContract.SCHEMA_VERSION);
        inventory.addProperty("inventoryId", "sef-command-inventory-p000");
        inventory.addProperty("phase", "SEFAUD-PHASE-000");
        inventory.addProperty("task", "P000-TASK-003");
        inventory.addProperty("source", "sealed runtime command and permission registries");
        inventory.addProperty("catalogActionCount", KernelServices.catalog().size());
        inventory.addProperty("routeCount", KernelServices.catalog().routes().size());
        inventory.addProperty("shortcutCount", KernelServices.shortcuts().size());
        inventory.addProperty("permissionCount", PermissionManifest.definitions().size());
        inventory.addProperty("unavailableFamilyCount", UNAVAILABLE_FAMILIES.size());
        inventory.add("rows", rows);
        return inventory;
    }

    public static Path write(Path approvedExternalRoot, String fileName) throws IOException {
        JsonObject inventory = generate();
        return AuditEvidenceContract.writeInventory(
                approvedExternalRoot,
                fileName,
                inventory,
                inventory.getAsJsonArray("rows"));
    }

    private static JsonObject row(
            String category,
            String semanticKey,
            String phase,
            String evidenceClass,
            String disposition,
            String sourceLocation
    ) {
        JsonObject row = new JsonObject();
        row.addProperty("schemaVersion", AuditEvidenceContract.SCHEMA_VERSION);
        row.addProperty("rowId", category + ":" + stableSuffix(semanticKey));
        row.addProperty("category", category);
        row.addProperty("semanticKey", semanticKey);
        row.addProperty("owner", "repository-audit-contract");
        row.addProperty("phase", phase);
        row.addProperty("evidenceRoute", "external restricted evidence root command inventory");
        row.addProperty("evidenceClass", evidenceClass);
        row.addProperty("disposition", disposition);
        row.add("invalidatedBy", strings(List.of(
                "command-registry-change",
                "permission-manifest-change",
                "dispatcher-change")));
        row.add("sourceLocations", strings(List.of(sourceLocation)));
        return row;
    }

    private static JsonArray strings(Iterable<String> values) {
        JsonArray result = new JsonArray();
        values.forEach(result::add);
        return result;
    }

    private static String stableSuffix(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9_.-]+", "_");
    }
}
