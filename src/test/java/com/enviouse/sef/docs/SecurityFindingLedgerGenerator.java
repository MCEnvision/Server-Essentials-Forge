package com.enviouse.sef.docs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Produces the stable Phase 001 finding ledger for confirmed repaired security
 * findings. External runtime blockers remain separate from this ledger.
 */
public final class SecurityFindingLedgerGenerator {
    private static final String PHASE = "SEFAUD-PHASE-001";

    private SecurityFindingLedgerGenerator() {
    }

    public static JsonObject generate() {
        JsonArray rows = new JsonArray();
        for (Finding finding : findings()) {
            JsonObject row = new JsonObject();
            row.addProperty("schemaVersion", AuditEvidenceContract.SCHEMA_VERSION);
            row.addProperty("rowId", "security-finding:" + finding.id().toLowerCase());
            row.addProperty("category", "security-finding");
            row.addProperty("semanticKey", finding.id());
            row.addProperty("owner", "security-audit");
            row.addProperty("phase", PHASE);
            row.addProperty("evidenceRoute", "external restricted evidence root security finding ledger");
            row.addProperty("evidenceClass", "static-and-regression");
            row.addProperty("disposition", "implemented");
            row.addProperty("findingId", finding.id());
            row.addProperty("severity", finding.severity());
            row.addProperty("title", finding.title());
            row.addProperty("invariant", finding.invariant());
            row.addProperty("exploitPrecondition", finding.exploitPrecondition());
            row.addProperty("impact", finding.impact());
            row.addProperty("repairCommit", finding.repairCommit());
            row.addProperty("regressionProof", finding.regressionProof());
            row.addProperty("disclosureRoute", "private maintainer security review");
            addLocations(row, "sourceLocations", finding.sources());
            addLocations(row, "sinkLocations", finding.sinks());
            JsonArray invalidatedBy = new JsonArray();
            invalidatedBy.add("source-change");
            invalidatedBy.add("policy-change");
            invalidatedBy.add("storage-change");
            invalidatedBy.add("dependency-change");
            invalidatedBy.add("test-harness-change");
            invalidatedBy.add("artifact-change");
            row.add("invalidatedBy", invalidatedBy);
            rows.add(row);
        }
        AuditEvidenceContract.validateInventorySet(rows);

        JsonObject inventory = new JsonObject();
        inventory.addProperty("schemaVersion", AuditEvidenceContract.SCHEMA_VERSION);
        inventory.addProperty("inventoryId", "sef-security-finding-ledger-p001");
        inventory.addProperty("phase", PHASE);
        inventory.addProperty("task", "P001-TASK-002");
        inventory.addProperty("source", "confirmed repaired findings from the current security review");
        inventory.addProperty("findingCount", rows.size());
        inventory.addProperty("closedFindingCount", rows.size());
        inventory.addProperty("openFindingCount", 0);
        inventory.addProperty("externalBlockerCount", 2);
        inventory.addProperty("externalBlockers", "EXT-001, EXT-002");
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

    private static void addLocations(JsonObject row, String name, List<String> locations) {
        JsonArray values = new JsonArray();
        locations.forEach(values::add);
        row.add(name, values);
    }

    private static List<Finding> findings() {
        return List.of(
                new Finding(
                        "P001-FIND-001",
                        "high",
                        "raw admin chat content could enter the ordinary server log",
                        "sensitive chat content is redacted before ordinary logging",
                        "an administrator sends secret shaped or private text through admin chat",
                        "private command or chat content becomes available to ordinary log readers",
                        "f55b4e9",
                        "CommandRedactionPolicyTest and source canary scan",
                        List.of("src/main/java/com/enviouse/sef/events/ChatEventHandler.java", "src/main/java/com/enviouse/sef/commandlog/CommandRedactionPolicy.java"),
                        List.of("server log and optional file log")),
                new Finding(
                        "P001-FIND-002",
                        "high",
                        "an oversized existing Fancy Tags object could be read before its limit was enforced",
                        "encoded object bytes are bounded before integrity processing or native decode",
                        "a managed object exceeds the configured encoded byte limit",
                        "unbounded allocation or processing can exhaust server resources",
                        "f55b4e9",
                        "FancyTagServiceTest oversized object rejection",
                        List.of("src/main/java/com/enviouse/sef/fancytags/FancyTagObjectStore.java"),
                        List.of("object store read and decode")),
                new Finding(
                        "P001-FIND-003",
                        "high",
                        "an audit active file or rotation path could be substituted with a link",
                        "audit roots and active files reject symbolic links and detectable hard links before native append",
                        "an attacker can replace the active audit path during startup or rotation",
                        "audit records could be redirected to or appended into an external file",
                        "3c588ae, 614ec89",
                        "AuditServiceTest active symlink, hard link, and native path boundary regressions",
                        List.of("src/main/java/com/enviouse/sef/audit/NativeAuditFileProvider.java", "src/main/java/com/enviouse/sef/audit/SecurityAuditService.java"),
                        List.of("audit active file and rotation target")),
                new Finding(
                        "P001-FIND-004",
                        "high",
                        "a configuration root beneath a symbolic link could redirect module state",
                        "configuration parents, backups, migration roots, and recovery roots are safe validated directories",
                        "a configured module root contains an untrusted symbolic link",
                        "module state or backup data could be written outside the owned root",
                        "f55b4e9",
                        "ModuleConfigServiceTest configuration parent and backup root link regressions",
                        List.of("src/main/java/com/enviouse/sef/config/modules/ModuleConfigService.java", "src/main/java/com/enviouse/sef/storage/AtomicFileStore.java"),
                        List.of("module configuration, migration, and backup paths")),
                new Finding(
                        "P001-FIND-005",
                        "high",
                        "candidate dependency resolution could be mistaken for installed runtime remediation",
                        "platform supplied dependencies stay compile only in the mod and require separate runtime provenance",
                        "a development resolution override changes the candidate graph without changing NeoForge runtime artifacts",
                        "a false dependency closure could ship a vulnerable or incompatible installed runtime",
                        "f55b4e9",
                        "BuildInventoryGeneratorTest, dependency insight, and packaged JAR duplicate scan",
                        List.of("build.gradle", "settings.gradle", "src/test/java/com/enviouse/sef/docs/BuildInventoryGenerator.java"),
                        List.of("NeoForge runtime dependency graph and packaged mod JAR")));
    }

    private record Finding(
            String id,
            String severity,
            String title,
            String invariant,
            String exploitPrecondition,
            String impact,
            String repairCommit,
            String regressionProof,
            List<String> sources,
            List<String> sinks
    ) {
    }
}
