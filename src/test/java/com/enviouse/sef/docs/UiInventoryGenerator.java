package com.enviouse.sef.docs;

import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.command.PanelContracts;
import com.enviouse.sef.permissions.PermissionManifest;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Derives the Phase 000 graphical, protocol, fallback, and feedback inventory
 * from source discovery and the sealed runtime descriptor registries.
 */
public final class UiInventoryGenerator {
    private static final Pattern RECORD = Pattern.compile("\\brecord\\s+([A-Za-z0-9_]+)\\s*\\(");
    private static final Pattern FEEDBACK = Pattern.compile("\\b(sendSuccess|sendFailure|sendSystemMessage)\\b");

    private UiInventoryGenerator() {
    }

    public static JsonObject generate(Path repositoryRoot) throws IOException {
        KernelServices.initialize();
        JsonArray rows = new JsonArray();
        Set<String> discoveredPayloads = new TreeSet<>();
        Path sourceRoot = repositoryRoot.resolve("src/main/java/com/enviouse/sef");
        try (var paths = Files.walk(sourceRoot)) {
            for (Path path : paths.filter(file -> file.toString().endsWith(".java")).sorted().toList()) {
                String relative = repositoryRoot.relativize(path).toString().replace(path.getFileSystem().getSeparator(), "/");
                String source = Files.readString(path, StandardCharsets.UTF_8);
                boolean client = relative.contains("/gui/client/");
                boolean protocol = relative.contains("/gui/protocol/");
                String fileName = path.getFileName().toString();
                if (source.contains("extends Screen") || source.contains("extends ContainerScreen")
                        || fileName.endsWith("Screen.java")) {
                    rows.add(row("screen", relative, "static", relative));
                }
                if (source.contains("extends AbstractContainerMenu") || fileName.endsWith("Menu.java")) {
                    rows.add(row("menu", relative, "static", relative));
                }
                if (protocol && (fileName.contains("Payload") || source.contains("CustomPacketPayload"))) {
                    Matcher records = RECORD.matcher(source);
                    while (records.find()) {
                        String record = records.group(1);
                        if (discoveredPayloads.add(relative + "#" + record)) {
                            rows.add(row("payload", relative + "#" + record, "static", relative));
                        }
                    }
                }
                if (fileName.contains("Hud") || fileName.contains("HUD") || source.contains("HudTile")) {
                    rows.add(row("hud-contract", relative, "static", relative));
                }
                if (fileName.contains("Workflow") || fileName.contains("Picker")
                        || fileName.contains("Studio") || fileName.contains("Editor")) {
                    rows.add(row("workflow", relative, client ? "client" : "server", relative));
                }
                Matcher feedback = FEEDBACK.matcher(source);
                while (feedback.find()) {
                    int line = source.substring(0, feedback.start()).split("\\R", -1).length;
                    JsonObject feedbackRow = row(
                            "feedback",
                            relative + ":" + line + ":" + feedback.group(1),
                            "runtime",
                            relative);
                    feedbackRow.addProperty("feedbackKind", feedback.group(1));
                    rows.add(feedbackRow);
                }
            }
        }

        for (PanelContracts.PanelDescriptor panel : KernelServices.descriptors().panels().values()) {
            JsonObject panelRow = row(
                    "descriptor",
                    panel.id(),
                    "runtime",
                    "src/main/java/com/enviouse/sef/kernel/KernelServices.java");
            panelRow.addProperty("permissionId", panel.permissionId());
            panelRow.addProperty("fallbackRoute", panel.fallback().route());
            rows.add(panelRow);

            JsonObject fallback = row(
                    "fallback",
                    "panel:" + panel.id(),
                    "runtime",
                    "src/main/java/com/enviouse/sef/kernel/command/PanelContracts.java");
            fallback.addProperty("route", panel.fallback().route());
            fallback.addProperty("owner", "command");
            rows.add(fallback);
            for (PanelContracts.ControlDescriptor control : panel.controls()) {
                if (KernelServices.catalog().find(control.actionId()).isEmpty()) {
                    throw new IllegalStateException("orphan panel action " + control.actionId());
                }
                JsonObject controlRow = row(
                        "control",
                        panel.id() + "/" + control.id(),
                        "runtime",
                        "src/main/java/com/enviouse/sef/kernel/command/PanelContracts.java");
                controlRow.addProperty("panelId", panel.id());
                controlRow.addProperty("actionId", control.actionId());
                controlRow.addProperty("permissionId", control.permissionId());
                controlRow.addProperty("executionContext", control.executionContext().name().toLowerCase());
                controlRow.addProperty("audience", control.audience().kind().name().toLowerCase());
                rows.add(controlRow);
            }
        }
        for (String descriptor : KernelServices.descriptors().commandOnlyDescriptors()) {
            rows.add(row(
                    "descriptor-command-only",
                    descriptor,
                    "runtime",
                    "src/main/java/com/enviouse/sef/kernel/KernelServices.java"));
        }
        for (var hud : com.enviouse.sef.gui.protocol.HudContracts.phaseNineDefaults().descriptors().values()) {
            JsonObject hudRow = row(
                    "hud",
                    hud.id(),
                    "runtime",
                    "src/main/java/com/enviouse/sef/gui/protocol/HudContracts.java");
            hudRow.addProperty("permissionId", hud.permissionId());
            hudRow.addProperty("surface", hud.surface().name().toLowerCase());
            hudRow.addProperty("fallback", hud.fallback().name().toLowerCase());
            hudRow.addProperty("fallbackOwner", hud.fallbackOwner().name().toLowerCase());
            rows.add(hudRow);
            JsonObject fallback = row(
                    "fallback",
                    "hud:" + hud.id(),
                    "runtime",
                    "src/main/java/com/enviouse/sef/gui/protocol/HudContracts.java");
            fallback.addProperty("route", hud.fallback().name().toLowerCase());
            fallback.addProperty("owner", hud.fallbackOwner().name().toLowerCase());
            rows.add(fallback);
        }
        rows.add(row(
                "translation",
                "assets/sef/lang/en_us.json",
                "static",
                "src/main/resources/assets/sef/lang/en_us.json"));
        for (String family : new TreeSet<>(CommandInventoryGenerator.UNAVAILABLE_FAMILIES)) {
            JsonObject unavailable = row(
                    "ui-unavailable",
                    family,
                    "negative",
                    "docs/general/plan.md");
            unavailable.addProperty("status", "unavailable");
            unavailable.addProperty("mutationAffordance", false);
            rows.add(unavailable);
        }

        AuditEvidenceContract.validateInventorySet(rows);
        JsonObject inventory = new JsonObject();
        inventory.addProperty("schemaVersion", AuditEvidenceContract.SCHEMA_VERSION);
        inventory.addProperty("inventoryId", "sef-ui-inventory-p000");
        inventory.addProperty("phase", "SEFAUD-PHASE-000");
        inventory.addProperty("task", "P000-TASK-004");
        inventory.addProperty("source", "GUI source discovery and sealed descriptor registries");
        inventory.addProperty("screenCount", count(rows, "screen"));
        inventory.addProperty("menuCount", count(rows, "menu"));
        inventory.addProperty("payloadCount", count(rows, "payload"));
        inventory.addProperty("descriptorCount", count(rows, "descriptor") + count(rows, "descriptor-command-only"));
        inventory.addProperty("controlCount", count(rows, "control"));
        inventory.addProperty("hudCount", count(rows, "hud"));
        inventory.addProperty("feedbackCount", count(rows, "feedback"));
        inventory.addProperty("unavailableFamilyCount", count(rows, "ui-unavailable"));
        inventory.addProperty("rowCount", rows.size());
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

    private static JsonObject row(String category, String semanticKey, String evidenceClass, String sourceLocation) {
        JsonObject row = new JsonObject();
        row.addProperty("schemaVersion", AuditEvidenceContract.SCHEMA_VERSION);
        row.addProperty("rowId", category + ":" + stableSuffix(semanticKey));
        row.addProperty("category", category);
        row.addProperty("semanticKey", semanticKey);
        row.addProperty("owner", "repository-audit-contract");
        row.addProperty("phase", "SEFAUD-PHASE-000");
        row.addProperty("evidenceRoute", "external restricted evidence root UI inventory");
        row.addProperty("evidenceClass", evidenceClass);
        row.addProperty("disposition", category.equals("ui-unavailable") ? "excluded" : "implemented");
        JsonArray invalidatedBy = new JsonArray();
        invalidatedBy.add("screen-registry-change");
        invalidatedBy.add("protocol-change");
        invalidatedBy.add("feedback-source-change");
        row.add("invalidatedBy", invalidatedBy);
        JsonArray sources = new JsonArray();
        sources.add(sourceLocation);
        row.add("sourceLocations", sources);
        return row;
    }

    private static int count(JsonArray rows, String category) {
        int count = 0;
        for (var row : rows) {
            if (row.getAsJsonObject().get("category").getAsString().equals(category)) {
                count++;
            }
        }
        return count;
    }

    private static String stableSuffix(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9_.-]+", "_");
    }
}
