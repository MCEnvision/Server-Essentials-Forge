package com.enviouse.sef.docs;

import com.enviouse.sef.config.modules.ModuleConfigRegistry;
import com.enviouse.sef.kernel.KernelServices;
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
 * Derives lifecycle, transient-state, integration, configuration, side, and
 * protocol ownership rows from source hooks and runtime configuration schemas.
 */
public final class LifecycleInventoryGenerator {
    private static final Pattern EVENT_HOOK = Pattern.compile(
            "@(SubscribeEvent|EventBusSubscriber|Mod.EventBusSubscriber)\\b");
    private static final Pattern LIFECYCLE_METHOD = Pattern.compile(
            "\\b(onServer|onPlayer|onWorld|onClient|startup|shutdown|start|stop|reload|tick|disconnect|login|logout)\\s*\\(");
    private static final Pattern TRANSIENT_STATE = Pattern.compile(
            "\\b(Map|Set|List|Queue|Deque|Atomic|Pending|Session|Cache|Revision|Executor|Thread|CompletableFuture)\\b");
    private static final Set<String> INTEGRATION_MARKERS = Set.of(
            "LuckPerms", "FTB", "Curios", "ExternalMod", "Compat", "Provider", "Optional");

    private LifecycleInventoryGenerator() {
    }

    public static JsonObject generate(Path repositoryRoot) throws IOException {
        KernelServices.initialize();
        JsonArray rows = new JsonArray();
        Path sourceRoot = repositoryRoot.resolve("src/main/java/com/enviouse/sef");
        int lifecycleHooks = 0;
        int transientOwners = 0;
        int integrationOwners = 0;
        int clientFiles = 0;
        int serverFiles = 0;
        try (var paths = Files.walk(sourceRoot)) {
            for (Path path : paths.filter(file -> file.toString().endsWith(".java")).sorted().toList()) {
                String relative = repositoryRoot.relativize(path).toString().replace(path.getFileSystem().getSeparator(), "/");
                String source = Files.readString(path, StandardCharsets.UTF_8);
                boolean client = relative.contains("/gui/client/") || relative.endsWith("/SefClientEvents.java");
                if (client) {
                    clientFiles++;
                } else {
                    serverFiles++;
                }
                if (source.contains("@SubscribeEvent") || source.contains("EventBusSubscriber")) {
                    Matcher events = EVENT_HOOK.matcher(source);
                    while (events.find()) {
                        int line = source.substring(0, events.start()).split("\\R", -1).length;
                        JsonObject row = row("lifecycle-hook", relative + ":" + line + ":" + events.group(1), "runtime", relative);
                        row.addProperty("logicalSide", client ? "client" : "common-or-server");
                        rows.add(row);
                        lifecycleHooks++;
                    }
                }
                Matcher methods = LIFECYCLE_METHOD.matcher(source);
                while (methods.find()) {
                    int line = source.substring(0, methods.start()).split("\\R", -1).length;
                    JsonObject row = row("lifecycle-transition", relative + ":" + line + ":" + methods.group(1), "static", relative);
                    row.addProperty("transition", methods.group(1).toLowerCase());
                    row.addProperty("logicalSide", client ? "client" : "common-or-server");
                    rows.add(row);
                }
                if (TRANSIENT_STATE.matcher(source).find()) {
                    JsonObject row = row("transient-owner", relative, "static", relative);
                    row.addProperty("logicalSide", client ? "client" : "common-or-server");
                    row.addProperty("threadBoundary", source.contains("Executor") || source.contains("Thread"));
                    rows.add(row);
                    transientOwners++;
                }
                for (String marker : INTEGRATION_MARKERS.stream().sorted().toList()) {
                    if (source.contains(marker)) {
                        JsonObject row = row("integration", relative + ":" + marker, "static", relative);
                        row.addProperty("marker", marker.toLowerCase());
                        row.addProperty("runtimeGuarded", source.contains("isLoaded") || source.contains("isPresent")
                                || source.contains("ModList") || source.contains("optional"));
                        rows.add(row);
                        integrationOwners++;
                    }
                }
                if (relative.contains("/gui/protocol/") || relative.endsWith("/SefNetwork.java")) {
                    JsonObject row = row("protocol-boundary", relative, "runtime", relative);
                    row.addProperty("logicalSide", client ? "client" : "common-or-server");
                    row.addProperty("validationRequired", source.contains("validate") || source.contains("Objects.requireNonNull"));
                    rows.add(row);
                }
            }
        }

        ModuleConfigRegistry registry = new ModuleConfigRegistry();
        for (ModuleConfigRegistry.ModuleDefinition module : registry.definitions()) {
            JsonObject moduleRow = row(
                    "configuration-module",
                    module.id(),
                    "runtime",
                    "src/main/java/com/enviouse/sef/config/modules/ModuleConfigRegistry.java");
            moduleRow.addProperty("fileName", module.fileName());
            moduleRow.addProperty("documentationVersion", module.documentationVersion());
            moduleRow.addProperty("applyClass", module.applyClass().id());
            moduleRow.add("dependencies", strings(module.dependencies()));
            moduleRow.add("conflicts", strings(module.conflicts()));
            rows.add(moduleRow);
            for (ModuleConfigRegistry.SettingDefinition setting : module.settings()) {
                JsonObject settingRow = row(
                        "configuration-field",
                        module.id() + "/" + setting.path(),
                        "runtime",
                        "src/main/java/com/enviouse/sef/config/modules/ModuleConfigRegistry.java");
                settingRow.addProperty("moduleId", module.id());
                settingRow.addProperty("type", setting.type().id());
                settingRow.addProperty("applyClass", setting.applyClass().id());
                settingRow.addProperty("sensitivity", setting.sensitivity().id());
                settingRow.addProperty("bounds", setting.boundsDescription());
                rows.add(settingRow);
            }
        }
        rows.add(row("resource-boundary", "sef.mixins.json", "static", "src/main/resources/sef.mixins.json"));
        rows.add(row("resource-boundary", "accesstransformer.cfg", "static", "src/main/resources/META-INF/accesstransformer.cfg"));

        AuditEvidenceContract.validateInventorySet(rows);
        JsonObject inventory = new JsonObject();
        inventory.addProperty("schemaVersion", AuditEvidenceContract.SCHEMA_VERSION);
        inventory.addProperty("inventoryId", "sef-lifecycle-inventory-p000");
        inventory.addProperty("phase", "SEFAUD-PHASE-000");
        inventory.addProperty("task", "P000-TASK-006");
        inventory.addProperty("source", "lifecycle hooks, transient owners, optional integrations, and runtime configuration schemas");
        inventory.addProperty("lifecycleHookCount", lifecycleHooks);
        inventory.addProperty("transientOwnerCount", transientOwners);
        inventory.addProperty("integrationOwnerCount", integrationOwners);
        inventory.addProperty("clientSourceFileCount", clientFiles);
        inventory.addProperty("commonOrServerSourceFileCount", serverFiles);
        inventory.addProperty("configurationModuleCount", registry.definitions().size());
        inventory.addProperty("configurationFieldCount", registry.definitions().stream()
                .mapToInt(module -> module.settings().size()).sum());
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
        row.addProperty("evidenceRoute", "external restricted evidence root lifecycle inventory");
        row.addProperty("evidenceClass", evidenceClass);
        row.addProperty("disposition", "implemented");
        JsonArray invalidatedBy = new JsonArray();
        invalidatedBy.add("lifecycle-hook-change");
        invalidatedBy.add("configuration-schema-change");
        invalidatedBy.add("integration-boundary-change");
        row.add("invalidatedBy", invalidatedBy);
        JsonArray sources = new JsonArray();
        sources.add(sourceLocation);
        row.add("sourceLocations", sources);
        return row;
    }

    private static JsonArray strings(Iterable<String> values) {
        JsonArray result = new JsonArray();
        TreeSet<String> sorted = new TreeSet<>();
        values.forEach(sorted::add);
        sorted.forEach(result::add);
        return result;
    }

    private static String stableSuffix(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9_.-]+", "_");
    }
}
