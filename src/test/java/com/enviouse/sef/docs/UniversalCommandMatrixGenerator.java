package com.enviouse.sef.docs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Builds the machine checked per action behavioral matrix from the sealed command
 * inventory. A row may be incomplete, but no required dimension may be omitted or
 * represented by an unqualified parser-only pass.
 */
public final class UniversalCommandMatrixGenerator {
    public static final List<String> DIMENSIONS = List.of(
            "registration",
            "discovery",
            "authority",
            "sources",
            "targets",
            "arguments",
            "policy",
            "preview",
            "effect",
            "failure",
            "persistence",
            "route_equivalence",
            "feedback",
            "audit",
            "redaction",
            "client_fixture",
            "linux_shared_runtime",
            "host_specific_runtime",
            "native_dependency");
    private static final Set<String> STATUSES = Set.of("pass", "partial", "open", "not_applicable");

    private UniversalCommandMatrixGenerator() {
    }

    public static JsonObject generate() {
        JsonObject inventory = CommandInventoryGenerator.generate();
        JsonArray sourceRows = inventory.getAsJsonArray("rows");
        Map<String, JsonObject> actions = new LinkedHashMap<>();
        Map<String, List<String>> routes = new LinkedHashMap<>();
        for (JsonElement element : sourceRows) {
            JsonObject row = element.getAsJsonObject();
            String category = row.get("category").getAsString();
            if (category.equals("command")) {
                actions.put(row.get("semanticKey").getAsString(), row);
            } else if (category.equals("route")) {
                routes.computeIfAbsent(row.get("actionId").getAsString(), ignored -> new ArrayList<>())
                        .add(row.get("semanticKey").getAsString());
            }
        }

        JsonArray rows = new JsonArray();
        for (JsonObject action : actions.values()) {
            rows.add(commandRow(action, routes.getOrDefault(action.get("semanticKey").getAsString(), List.of())));
        }
        for (JsonElement element : sourceRows) {
            JsonObject row = element.getAsJsonObject();
            if (row.get("category").getAsString().equals("unavailable")) {
                rows.add(unavailableRow(row));
            }
        }
        int openRows = 0;
        int partialRows = 0;
        int passedRows = 0;
        for (JsonElement element : rows) {
            String status = element.getAsJsonObject().get("status").getAsString();
            switch (status) {
                case "pass" -> passedRows++;
                case "partial" -> partialRows++;
                case "open" -> openRows++;
                default -> {
                }
            }
        }

        JsonObject result = new JsonObject();
        result.addProperty("schemaVersion", AuditEvidenceContract.SCHEMA_VERSION);
        result.addProperty("inventoryId", "sef-universal-command-matrix-p003");
        result.addProperty("phase", "SEFAUD-PHASE-003");
        result.addProperty("task", "P003-TASK-002");
        result.addProperty("source", "sealed command inventory with explicit behavioral evidence classification");
        result.addProperty("candidateContract", "same artifact, Minecraft Java client fixture, canonical Linux runtime");
        result.addProperty("rowCount", rows.size());
        result.addProperty("commandRowCount", actions.size());
        result.addProperty("unavailableRowCount", countCategory(rows, "unavailable-matrix"));
        result.addProperty("passedRowCount", passedRows);
        result.addProperty("partialRowCount", partialRows);
        result.addProperty("openRowCount", openRows);
        result.addProperty("complete", openRows == 0 && partialRows == 0);
        result.add("dimensions", strings(DIMENSIONS));
        result.add("rows", rows);
        validate(result);
        return result;
    }

    public static void validate(JsonObject matrix) {
        if (matrix == null || !matrix.has("rows") || !matrix.get("rows").isJsonArray()) {
            throw new IllegalArgumentException("universal command matrix rows are required");
        }
        if (!matrix.has("dimensions") || !matrix.get("dimensions").isJsonArray()
                || matrix.getAsJsonArray("dimensions").size() != DIMENSIONS.size()) {
            throw new IllegalArgumentException("universal command matrix dimensions are required");
        }
        Set<String> matrixDimensions = new java.util.HashSet<>();
        for (JsonElement dimension : matrix.getAsJsonArray("dimensions")) {
            if (!dimension.isJsonPrimitive() || !dimension.getAsJsonPrimitive().isString()
                    || !matrixDimensions.add(dimension.getAsString())
                    || !DIMENSIONS.contains(dimension.getAsString())) {
                throw new IllegalArgumentException("universal command matrix dimensions are invalid");
            }
        }
        JsonArray rows = matrix.getAsJsonArray("rows");
        AuditEvidenceContract.validateInventorySet(rows);
        Set<String> actionIds = new java.util.HashSet<>();
        int commands = 0;
        int unavailable = 0;
        for (JsonElement element : rows) {
            JsonObject row = element.getAsJsonObject();
            String category = row.get("category").getAsString();
            if (!category.equals("command-matrix") && !category.equals("unavailable-matrix")) {
                throw new IllegalArgumentException("unknown universal matrix row category " + category);
            }
            if (!row.has("status") || !row.get("status").isJsonPrimitive()
                    || !row.getAsJsonPrimitive("status").isString()
                    || !STATUSES.contains(row.get("status").getAsString())) {
                throw new IllegalArgumentException("invalid universal matrix row status");
            }
            if (!row.has("dimensions") || !row.get("dimensions").isJsonObject()) {
                throw new IllegalArgumentException("universal matrix dimensions are required");
            }
            JsonObject dimensions = row.getAsJsonObject("dimensions");
            for (String dimension : DIMENSIONS) {
                if (!dimensions.has(dimension) || !dimensions.get(dimension).isJsonObject()) {
                    throw new IllegalArgumentException("missing universal matrix dimension " + dimension);
                }
                JsonObject evidence = dimensions.getAsJsonObject(dimension);
                if (!evidence.has("status") || !evidence.get("status").isJsonPrimitive()
                        || !evidence.getAsJsonPrimitive("status").isString()
                        || !STATUSES.contains(evidence.get("status").getAsString())) {
                    throw new IllegalArgumentException("invalid universal matrix dimension status " + dimension);
                }
                String status = evidence.get("status").getAsString();
                if (status.equals("pass") && (!evidence.has("evidence")
                        || !evidence.get("evidence").isJsonArray()
                        || evidence.getAsJsonArray("evidence").isEmpty())) {
                    throw new IllegalArgumentException("passed universal matrix dimension has no evidence " + dimension);
                }
                if (!status.equals("pass") && (!evidence.has("reason")
                        || !evidence.get("reason").isJsonPrimitive()
                        || evidence.get("reason").getAsString().isBlank())) {
                    throw new IllegalArgumentException("open universal matrix dimension has no reason " + dimension);
                }
            }
            if (category.equals("command-matrix")) {
                commands++;
                if (!actionIds.add(row.get("semanticKey").getAsString())) {
                    throw new IllegalArgumentException("duplicate universal command action");
                }
                if (!row.has("actionId") || !row.has("canonicalRoute") || !row.has("permissionIds")
                        || !row.has("sourceTypes") || !row.has("orderedRoutes")) {
                    throw new IllegalArgumentException("universal command ownership fields are missing");
                }
                if (!row.getAsJsonArray("orderedRoutes").toString().contains(
                        row.get("canonicalRoute").getAsString())) {
                    throw new IllegalArgumentException("canonical route is absent from ordered routes");
                }
            } else {
                unavailable++;
                if (row.get("status").getAsString().equals("open")) {
                    throw new IllegalArgumentException("unavailable family cannot be open");
                }
            }
        }
        if (!matrix.has("rowCount") || matrix.get("rowCount").getAsInt() != rows.size()
                || !matrix.has("commandRowCount") || matrix.get("commandRowCount").getAsInt() != commands
                || !matrix.has("unavailableRowCount")
                || matrix.get("unavailableRowCount").getAsInt() != unavailable) {
            throw new IllegalArgumentException("universal matrix counts do not match rows");
        }
        int openRows = 0;
        int partialRows = 0;
        int passedRows = 0;
        for (JsonElement element : rows) {
            switch (element.getAsJsonObject().get("status").getAsString()) {
                case "open" -> openRows++;
                case "partial" -> partialRows++;
                case "pass" -> passedRows++;
                default -> {
                }
            }
        }
        if (!matrix.has("openRowCount") || matrix.get("openRowCount").getAsInt() != openRows
                || !matrix.has("partialRowCount") || matrix.get("partialRowCount").getAsInt() != partialRows
                || !matrix.has("passedRowCount") || matrix.get("passedRowCount").getAsInt() != passedRows) {
            throw new IllegalArgumentException("universal matrix status counts do not match rows");
        }
        boolean complete = rows.asList().stream()
                .allMatch(value -> value.getAsJsonObject().get("status").getAsString().equals("pass"));
        if (matrix.get("complete").getAsBoolean() != complete) {
            throw new IllegalArgumentException("universal matrix completion status does not match rows");
        }
    }

    public static Path write(Path approvedExternalRoot, String fileName) throws IOException {
        JsonObject matrix = generate();
        return AuditEvidenceContract.writeInventory(
                approvedExternalRoot,
                fileName,
                matrix,
                matrix.getAsJsonArray("rows"));
    }

    private static JsonObject commandRow(JsonObject action, List<String> routeList) {
        String actionId = action.get("semanticKey").getAsString();
        JsonObject row = baseRow("command-matrix", actionId, "static", "incomplete");
        row.addProperty("actionId", actionId);
        row.addProperty("canonicalRoute", action.get("canonicalRoute").getAsString());
        row.addProperty("featureId", action.get("featureId").getAsString());
        row.addProperty("accessClass", action.get("accessClass").getAsString());
        row.addProperty("auditClass", action.get("auditClass").getAsString());
        row.addProperty("targetBehavior", action.get("targetBehavior").getAsString());
        row.add("sourceLocations", action.getAsJsonArray("sourceLocations").deepCopy());
        row.add("permissionIds", action.getAsJsonArray("permissionIds").deepCopy());
        row.add("sourceTypes", action.getAsJsonArray("sourceTypes").deepCopy());
        JsonArray orderedRoutes = new JsonArray();
        orderedRoutes.add(action.get("canonicalRoute").getAsString());
        routeList.stream().sorted().filter(route -> !route.equals(action.get("canonicalRoute").getAsString()))
                .forEach(orderedRoutes::add);
        action.getAsJsonArray("convenienceRoots").forEach(orderedRoutes::add);
        row.add("orderedRoutes", orderedRoutes);
        row.add("dimensions", commandDimensions(action));
        row.addProperty("status", "open");
        return row;
    }

    private static JsonObject unavailableRow(JsonObject source) {
        String family = source.get("semanticKey").getAsString();
        JsonObject row = baseRow("unavailable-matrix", family, "runtime", "implemented");
        row.add("sourceLocations", source.getAsJsonArray("sourceLocations").deepCopy());
        row.addProperty("actionId", family);
        row.addProperty("canonicalRoute", "unavailable:" + family);
        row.add("orderedRoutes", new JsonArray());
        row.add("dimensions", unavailableDimensions());
        row.addProperty("status", "pass");
        return row;
    }

    private static JsonObject commandDimensions(JsonObject action) {
        JsonObject dimensions = new JsonObject();
        add(dimensions, "registration", "pass", "live catalog and dispatcher route ownership", "task-025-inventory/command-inventory-live.json");
        add(dimensions, "discovery", "pass", "catalog-wide live route resolution GameTest", "task-024-remediation-gametest.log");
        add(dimensions, "authority", "partial", "shared policy tests are not an action-level grant and revoke join", "task-028-universal-matrix-ledger.md");
        add(dimensions, "sources", "partial", "source coverage is representative and still needs action-level applicability", "task-028-universal-matrix-ledger.md");
        add(dimensions, "targets", "partial", "target policy coverage is representative and still needs action-level effect oracles", "task-028-universal-matrix-ledger.md");
        add(dimensions, "arguments", "partial", "representative parser corpus does not prove each action effect", "task-024-remediation-gametest.log");
        add(dimensions, "policy", "partial", "shared pipeline contracts pass; per-action policy joins remain open", "task-028-universal-matrix-ledger.md");
        add(dimensions, "preview", "partial", "confirmation and revision contracts are covered for selected routes", "task-028-universal-matrix-ledger.md");
        String auditClass = action.get("auditClass").getAsString();
        if (auditClass.equals("metadata_only")) {
            add(dimensions, "effect", "partial", "358 read-only routes executed but not mapped to every action row", "task-024-remediation-gametest.log");
        } else {
            add(dimensions, "effect", "open", "action-specific mutation oracle is required", "task-028-universal-matrix-ledger.md");
        }
        add(dimensions, "failure", "open", "action-specific zero-side-effect and failure cut-point oracle is required", "task-028-universal-matrix-ledger.md");
        add(dimensions, "persistence", "partial", "Phase 002 owner contracts exist but each command effect needs a direct join", "task-028-universal-matrix-ledger.md");
        add(dimensions, "route_equivalence", "partial", "indirect route contracts pass selected equality cases", "task-028-universal-matrix-ledger.md");
        add(dimensions, "feedback", "partial", "feedback classes pass focused tests but are not joined to every action", "task-024-security-review.md");
        add(dimensions, "audit", "partial", "native writer and audit sink classes pass; per-action correlation remains open", "task-024-security-review.md");
        add(dimensions, "redaction", "partial", "redaction contract passes focused tests; per-action sensitive fields remain open", "task-024-security-review.md");
        add(dimensions, "client_fixture", "partial", "packaged client load and connection pass; full action interaction is open", "task-024-client-crash-remediation-report.md");
        add(dimensions, "linux_shared_runtime", "partial", "dedicated server and representative runtime proofs pass; universal effects remain open", "task-024-remediation-gametest.log");
        add(dimensions, "host_specific_runtime", "not_applicable", "no macOS or Windows non-client host-specific path changed or exercised", "task-025-audit-inventory-report.md");
        add(dimensions, "native_dependency", "partial", "candidate dependency and native writer identity pass; action-level joins remain open", "task-024-remediation-dependency-manifest.txt");
        return dimensions;
    }

    private static JsonObject unavailableDimensions() {
        JsonObject dimensions = new JsonObject();
        add(dimensions, "registration", "pass", "unavailable handler absence and diagnostics", "task-019-unavailable-families-rerun-report.md");
        add(dimensions, "discovery", "pass", "unavailable family is named and not advertised", "task-019-unavailable-families-rerun-report.md");
        add(dimensions, "authority", "pass", "owner and server permission contexts fail closed", "task-019-unavailable-families-rerun-report.md");
        add(dimensions, "sources", "pass", "actor and server execution attempts fail closed", "task-019-unavailable-families-rerun-report.md");
        add(dimensions, "targets", "pass", "no target mutation occurs", "task-019-unavailable-families-rerun-report.md");
        add(dimensions, "arguments", "pass", "unavailable schema cannot become executable", "task-019-unavailable-families-rerun-report.md");
        add(dimensions, "policy", "pass", "generic activation and resolution are denied", "task-019-unavailable-families-rerun-report.md");
        add(dimensions, "preview", "pass", "preview never becomes ready", "task-019-unavailable-families-rerun-report.md");
        add(dimensions, "effect", "pass", "execution leaves the record open and unchanged", "task-019-unavailable-families-rerun-report.md");
        add(dimensions, "failure", "pass", "provider error is returned without mutation", "task-019-unavailable-families-rerun-report.md");
        add(dimensions, "persistence", "pass", "revision and state remain unchanged", "task-019-unavailable-families-rerun-report.md");
        add(dimensions, "route_equivalence", "pass", "generic and indirect transitions cannot activate the family", "task-019-unavailable-families-rerun-report.md");
        add(dimensions, "feedback", "pass", "diagnostics retain unavailable classification", "task-019-unavailable-families-rerun-report.md");
        add(dimensions, "audit", "pass", "no successful mutation audit is emitted", "task-019-unavailable-families-rerun-report.md");
        add(dimensions, "redaction", "pass", "negative result contains no sensitive execution payload", "task-019-unavailable-families-rerun-report.md");
        add(dimensions, "client_fixture", "not_applicable", "unavailable family has no client action route", "task-028-universal-matrix-ledger.md");
        add(dimensions, "linux_shared_runtime", "pass", "canonical Linux negative contract passes", "task-019-unavailable-families-rerun-report.md");
        add(dimensions, "host_specific_runtime", "not_applicable", "no host-specific unavailable path was exercised", "task-025-audit-inventory-report.md");
        add(dimensions, "native_dependency", "not_applicable", "unavailable execution does not reach a native writer", "task-028-universal-matrix-ledger.md");
        return dimensions;
    }

    private static void add(JsonObject dimensions, String name, String status, String reason, String evidence) {
        JsonObject value = new JsonObject();
        value.addProperty("status", status);
        if (status.equals("pass")) {
            JsonArray references = new JsonArray();
            references.add(evidence);
            value.add("evidence", references);
        } else {
            value.addProperty("reason", reason);
            JsonArray references = new JsonArray();
            references.add(evidence);
            value.add("evidence", references);
        }
        dimensions.add(name, value);
    }

    private static JsonObject baseRow(String category, String semanticKey, String evidenceClass, String disposition) {
        JsonObject row = new JsonObject();
        row.addProperty("schemaVersion", AuditEvidenceContract.SCHEMA_VERSION);
        row.addProperty("rowId", "matrix:" + stableId(category + ":" + semanticKey));
        row.addProperty("category", category);
        row.addProperty("semanticKey", semanticKey);
        row.addProperty("owner", "command-policy-kernel");
        row.addProperty("phase", "SEFAUD-PHASE-003");
        row.addProperty("evidenceRoute", "external restricted evidence root universal command matrix");
        row.addProperty("evidenceClass", evidenceClass);
        row.addProperty("disposition", disposition);
        JsonArray invalidatedBy = new JsonArray();
        invalidatedBy.add("command-registry-change");
        invalidatedBy.add("policy-change");
        invalidatedBy.add("domain-effect-change");
        invalidatedBy.add("audit-provider-change");
        row.add("invalidatedBy", invalidatedBy);
        JsonArray sources = new JsonArray();
        sources.add("src/main/java/com/enviouse/sef/kernel/KernelServices.java");
        row.add("sourceLocations", sources);
        return row;
    }

    private static JsonArray strings(List<String> values) {
        JsonArray result = new JsonArray();
        values.forEach(result::add);
        return result;
    }

    private static int countCategory(JsonArray rows, String category) {
        int count = 0;
        for (JsonElement element : rows) {
            if (element.getAsJsonObject().get("category").getAsString().equals(category)) {
                count++;
            }
        }
        return count;
    }

    private static String stableId(String value) {
        String normalized = value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]+", "_");
        if (normalized.length() > 96) {
            normalized = normalized.substring(0, 96);
        }
        return normalized;
    }
}
