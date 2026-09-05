package com.enviouse.sef.docs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
    private static final Set<String> REQUIRED_COMMAND_DIMENSIONS = Set.copyOf(DIMENSIONS.stream()
            .filter(dimension -> !dimension.equals("host_specific_runtime"))
            .toList());

    private UniversalCommandMatrixGenerator() {
    }

    public static JsonObject generate() {
        JsonObject inventory = CommandInventoryGenerator.generate();
        RuntimeEvidence runtimeEvidence = RuntimeEvidence.load();
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
            rows.add(commandRow(
                    action,
                    routes.getOrDefault(action.get("semanticKey").getAsString(), List.of()),
                    runtimeEvidence));
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
            int openDimensions = 0;
            int partialDimensions = 0;
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
                if (category.equals("command-matrix")
                        && REQUIRED_COMMAND_DIMENSIONS.contains(dimension)
                        && status.equals("not_applicable")) {
                    throw new IllegalArgumentException(
                            "executable command dimension cannot be not applicable " + dimension);
                }
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
                if (status.equals("open")) {
                    openDimensions++;
                } else if (status.equals("partial")) {
                    partialDimensions++;
                }
            }
            String expectedRowStatus = openDimensions > 0
                    ? "open"
                    : partialDimensions > 0 ? "partial" : "pass";
            if (!row.get("status").getAsString().equals(expectedRowStatus)) {
                throw new IllegalArgumentException(
                        "universal matrix row status is stronger than its dimensions for "
                                + row.get("semanticKey").getAsString());
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
                validateAuditJoin(row);
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

    private static JsonObject commandRow(
            JsonObject action,
            List<String> routeList,
            RuntimeEvidence runtimeEvidence
    ) {
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
        row.add("auditJoin", auditJoin(action));
        row.add("dimensions", commandDimensions(action, runtimeEvidence));
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

    private static JsonObject commandDimensions(JsonObject action, RuntimeEvidence runtimeEvidence) {
        JsonObject dimensions = new JsonObject();
        add(dimensions, "registration", "pass", "live catalog and dispatcher route ownership", "task-025-inventory/command-inventory-live.json");
        add(dimensions, "discovery", "pass", "catalog-wide live route resolution GameTest", "task-024-remediation-gametest.log");
        add(dimensions, "authority", "partial", "catalog-wide granted and denied permission resolution proves the shared manifest boundary, but action-level revoke and mutation joins remain open", "task-010-catalog-feedback-20260905/task-010-catalog-feedback-report.md");
        add(dimensions, "sources", "partial", "source coverage is representative and still needs action-level applicability", "task-028-universal-matrix-ledger.md");
        add(dimensions, "targets", "partial", "target policy coverage is representative and still needs action-level effect oracles", "task-028-universal-matrix-ledger.md");
        add(dimensions, "arguments", "partial", "representative parser corpus does not prove each action effect", "task-024-remediation-gametest.log");
        add(dimensions, "policy", "partial", "catalog-wide denial and grant resolution reach the shared policy boundary, but per-action policy and mutation joins remain open", "task-010-catalog-feedback-20260905/task-010-catalog-feedback-report.md");
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
        add(dimensions, "feedback", "partial", "catalog-wide rejection and shared executor outcome handling are covered, but route-specific success presentation and distinct domain failure feedback remain open", "task-126-current-matrix-20260905/task-126-current-matrix-report.md");
        add(dimensions, "audit", "partial", "catalog-wide rejection and shared executor success or failure outcomes have per-action correlation, but domain effect and sink-specific joins remain open", "task-126-current-matrix-20260905/task-126-current-matrix-report.md");
        add(dimensions, "redaction", "partial", "catalog-wide rejection and shared executor outcomes keep normalized metadata bounded, but per-action sensitive fields and success projections remain open", "task-126-current-matrix-20260905/task-126-current-matrix-report.md");
        add(dimensions, "client_fixture", "partial", "packaged client load and connection pass; full action interaction is open", "task-024-client-crash-remediation-report.md");
        add(dimensions, "linux_shared_runtime", "partial", "dedicated server, representative runtime, catalog feedback and audit boundary, and shared executor outcome checks pass; universal effects remain open", "task-126-current-matrix-20260905/task-126-current-matrix-report.md");
        add(dimensions, "host_specific_runtime", "not_applicable", "no macOS or Windows non-client host-specific path changed or exercised", "task-025-audit-inventory-report.md");
        add(dimensions, "native_dependency", "partial", "candidate dependency, native writer identity, duplicate-runtime inspection, and shared executor audit joins pass; domain action sink joins remain open", "task-126-current-matrix-20260905/task-126-current-matrix-report.md");
        if (runtimeEvidence.successful(action.get("semanticKey").getAsString())) {
            add(dimensions, "audit", "pass", "catalog-wide console execution emitted one bounded correlated audit event", "catalog-console-runtime.json");
            add(dimensions, "redaction", "pass", "catalog-wide console execution emitted metadata-only redaction without raw command parameters", "catalog-console-runtime.json");
            add(dimensions, "linux_shared_runtime", "pass", "catalog-wide console route executed on the canonical Linux runtime", "catalog-console-runtime.json");
        }
        return dimensions;
    }

    private record RuntimeEvidence(Map<String, JsonObject> successfulRows) {
        private static RuntimeEvidence load() {
            String evidenceRoot = System.getProperty("sef.audit.evidenceRoot", "").trim();
            String expectedCommit = System.getProperty("sef.audit.candidateCommit", "").trim();
            String expectedSha256 = System.getProperty("sef.audit.candidateSha256", "").trim();
            if (evidenceRoot.isEmpty() || expectedCommit.isEmpty() || expectedSha256.isEmpty()) {
                return new RuntimeEvidence(Map.of());
            }
            Path file = Path.of(evidenceRoot).toAbsolutePath().normalize()
                    .resolve("catalog-console-runtime.json");
            if (!Files.isRegularFile(file) || Files.isSymbolicLink(file)) {
                return new RuntimeEvidence(Map.of());
            }
            try {
                JsonObject record = JsonParser.parseString(
                        Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
                if (record.get("schemaVersion").getAsInt() != 1
                        || !expectedCommit.equals(record.get("candidateCommit").getAsString())
                        || !expectedSha256.equals(record.get("candidateSha256").getAsString())
                        || !record.get("candidateCommit").getAsString().matches("[0-9a-f]{40}")
                        || !record.get("candidateSha256").getAsString().matches("[0-9a-f]{64}")) {
                    throw new IllegalArgumentException("catalog runtime evidence identity does not match candidate");
                }
                Map<String, JsonObject> successful = new LinkedHashMap<>();
                for (JsonElement element : record.getAsJsonArray("rows")) {
                    JsonObject row = element.getAsJsonObject();
                    String actionId = row.get("actionId").getAsString();
                    if (!row.get("result").getAsString().equals("success")
                            || row.get("auditEventCount").getAsInt() != 1
                            || !row.get("sourceType").getAsString().equals("console")
                            || !row.get("auditResult").getAsString().equals("success")
                            || !row.get("auditClass").getAsString().equals("metadata_only")
                            || !row.get("redactionClass").getAsString().equals("metadata")
                            || !row.get("commandDigest").getAsString().matches("[0-9a-f]{64}")) {
                        continue;
                    }
                    if (successful.put(actionId, row) != null) {
                        throw new IllegalArgumentException("duplicate catalog runtime evidence action " + actionId);
                    }
                }
                return new RuntimeEvidence(Map.copyOf(successful));
            } catch (RuntimeException | IOException exception) {
                throw new IllegalStateException("catalog runtime evidence is invalid", exception);
            }
        }

        private boolean successful(String actionId) {
            return successfulRows.containsKey(actionId);
        }
    }

    private static JsonObject unavailableDimensions() {
        JsonObject dimensions = new JsonObject();
        String evidence = "task-011-current-rerun-b23-20260905/task-011-b23-report.md";
        add(dimensions, "registration", "pass", "unavailable handler absence and diagnostics", evidence);
        add(dimensions, "discovery", "pass", "unavailable family is named and not advertised", evidence);
        add(dimensions, "authority", "pass", "owner and server permission contexts fail closed", evidence);
        add(dimensions, "sources", "pass", "actor and server execution attempts fail closed", evidence);
        add(dimensions, "targets", "pass", "no target mutation occurs", evidence);
        add(dimensions, "arguments", "pass", "unavailable schema cannot become executable", evidence);
        add(dimensions, "policy", "pass", "generic activation and resolution are denied", evidence);
        add(dimensions, "preview", "pass", "preview never becomes ready", evidence);
        add(dimensions, "effect", "pass", "execution leaves the record open and unchanged", evidence);
        add(dimensions, "failure", "pass", "provider error is returned without mutation", evidence);
        add(dimensions, "persistence", "pass", "revision and state remain unchanged", evidence);
        add(dimensions, "route_equivalence", "pass", "generic and indirect transitions cannot activate the family", evidence);
        add(dimensions, "feedback", "pass", "diagnostics retain unavailable classification", evidence);
        add(dimensions, "audit", "pass", "no successful mutation audit is emitted", evidence);
        add(dimensions, "redaction", "pass", "negative result contains no sensitive execution payload", evidence);
        add(dimensions, "client_fixture", "not_applicable", "unavailable family has no client action route", "task-028-universal-matrix-ledger.md");
        add(dimensions, "linux_shared_runtime", "pass", "canonical Linux negative contract passes", evidence);
        add(dimensions, "host_specific_runtime", "not_applicable", "no host-specific unavailable path was exercised", "task-025-audit-inventory-report.md");
        add(dimensions, "native_dependency", "not_applicable", "unavailable execution does not reach a native writer", "task-028-universal-matrix-ledger.md");
        return dimensions;
    }

    private static JsonObject auditJoin(JsonObject action) {
        String auditClass = action.get("auditClass").getAsString();
        boolean required = !auditClass.equals("none");
        JsonObject join = new JsonObject();
        join.addProperty("required", required);
        join.addProperty("auditClass", auditClass);
        join.addProperty(
                "eventWriter",
                required
                        ? "AuditService.record -> SecurityAuditService.record"
                        : "not_applicable");
        join.addProperty(
                "nativeSink",
                required
                        ? "SecurityAuditService.writerLoop -> NativeAuditFileProvider.append"
                        : "not_applicable");
        join.addProperty(
                "optionalObservationSink",
                "CommandEventJournal.append -> FileLogSink.submit");
        join.addProperty("runtimeDependencyManifest", required
                ? "platform-dependency-manifest.txt"
                : "not_applicable");
        join.add("pipelineCallSites", action.getAsJsonArray("pipelineCallSites").deepCopy());
        join.addProperty(
                "pipelineCallSiteDisposition",
                action.get("pipelineCallSiteDisposition").getAsString());
        JsonArray writerSources = new JsonArray();
        writerSources.add("src/main/java/com/enviouse/sef/audit/AuditService.java");
        writerSources.add("src/main/java/com/enviouse/sef/audit/SecurityAuditService.java");
        join.add("writerSources", writerSources);
        JsonArray sinkSources = new JsonArray();
        sinkSources.add("src/main/java/com/enviouse/sef/audit/NativeAuditFileProvider.java");
        sinkSources.add("src/main/java/com/enviouse/sef/commandlog/CommandEventJournal.java");
        sinkSources.add("src/main/java/com/enviouse/sef/commandlog/FileLogSink.java");
        join.add("sinkSources", sinkSources);
        return join;
    }

    private static void validateAuditJoin(JsonObject row) {
        if (!row.has("auditJoin") || !row.get("auditJoin").isJsonObject()) {
            throw new IllegalArgumentException("universal command audit join is required");
        }
        JsonObject join = row.getAsJsonObject("auditJoin");
        if (!join.has("required") || !join.get("required").isJsonPrimitive()
                || !join.getAsJsonPrimitive("required").isBoolean()) {
            throw new IllegalArgumentException("universal command audit join requirement is invalid");
        }
        if (!join.has("auditClass") || !join.get("auditClass").isJsonPrimitive()
                || !join.getAsJsonPrimitive("auditClass").isString()
                || !join.get("auditClass").getAsString().equals(row.get("auditClass").getAsString())) {
            throw new IllegalArgumentException("universal command audit join class is invalid");
        }
        for (String field : List.of(
                "eventWriter",
                "nativeSink",
                "optionalObservationSink",
                "runtimeDependencyManifest",
                "pipelineCallSiteDisposition")) {
            if (!join.has(field) || !join.get(field).isJsonPrimitive()
                    || !join.getAsJsonPrimitive(field).isString()
                    || join.get(field).getAsString().isBlank()) {
                throw new IllegalArgumentException("universal command audit join field is invalid " + field);
            }
        }
        if (!join.has("pipelineCallSites") || !join.get("pipelineCallSites").isJsonArray()
                || !join.has("writerSources") || !join.get("writerSources").isJsonArray()
                || !join.has("sinkSources") || !join.get("sinkSources").isJsonArray()) {
            throw new IllegalArgumentException("universal command audit join sources are required");
        }
        boolean required = join.get("required").getAsBoolean();
        if (required && (join.getAsJsonArray("writerSources").isEmpty()
                || join.getAsJsonArray("sinkSources").isEmpty())) {
            throw new IllegalArgumentException("required universal command audit join has no writer or sink");
        }
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
