package com.enviouse.sef.docs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Derives the explicit trust and data flow inventory for Phase 000. */
public final class BoundaryInventoryGenerator {
    private static final String PHASE = "SEFAUD-PHASE-000";
    private static final List<Boundary> BOUNDARIES = List.of(
            boundary("command-parser", "command parsing and source classification", "SEFAUD-PHASE-003",
                    "src/main/java/com/enviouse/sef/kernel/command/CommandCatalog.java", "untrusted command text", "sealed route and source validation"),
            boundary("command-authority", "permission and policy authority", "SEFAUD-PHASE-003",
                    "src/main/java/com/enviouse/sef/permissions/PermissionService.java", "actor and target identity", "permission, hierarchy, exemption, and execution recheck"),
            boundary("delegated-authority", "sudo and delegated execution", "SEFAUD-PHASE-003",
                    "src/main/java/com/enviouse/sef/automation/SudoDelegationAudit.java", "delegation request", "bounded scope, expiry, confirmation, and audit"),
            boundary("payload-decode", "enhanced payload decoding", "SEFAUD-PHASE-004",
                    "src/main/java/com/enviouse/sef/gui/protocol/PayloadCodecSupport.java", "network bytes", "length, count, and field bounds before allocation"),
            boundary("gui-projection", "server projected GUI authority", "SEFAUD-PHASE-004",
                    "src/main/java/com/enviouse/sef/gui/protocol/SefGuiServer.java", "client action request", "session, panel, revision, permission, and target recheck"),
            boundary("stored-command-indirection", "stored command and wrapper execution", "SEFAUD-PHASE-003",
                    "src/main/java/com/enviouse/sef/kernel/command/CommandWrapperService.java", "stored route and arguments", "canonical route, source, policy, and mutation recheck"),
            boundary("configuration-migration", "configuration and migration input", "SEFAUD-PHASE-002",
                    "src/main/java/com/enviouse/sef/config/modules/ModuleConfigService.java", "TOML and migration records", "typed bounds, revision, backup, and transactional publication"),
            boundary("filesystem-path", "owned filesystem roots and paths", "SEFAUD-PHASE-002",
                    "src/main/java/com/enviouse/sef/storage/AtomicFileStore.java", "host path and file name", "containment, normalized path, type, and link checks"),
            boundary("opened-object-writer", "opened descriptor or handle audit writing", "SEFAUD-PHASE-001",
                    "src/main/java/com/enviouse/sef/audit/NativeAuditFileProvider.java", "audit event and output target", "same opened object identity for validation, append, and flush"),
            boundary("persistence-deserialization", "repository and persistent data decoding", "SEFAUD-PHASE-002",
                    "src/main/java/com/enviouse/sef/storage/repository/StorageRepository.java", "serialized durable state", "schema, version, bounds, and recovery state"),
            boundary("archive-import", "archive and image import", "SEFAUD-PHASE-002",
                    "src/main/java/com/enviouse/sef/fancytags/FancyTagProjectArchive.java", "uploaded archive bytes", "entry, depth, ratio, pixel, and duplicate limits"),
            boundary("object-storage", "content addressed object and backup storage", "SEFAUD-PHASE-002",
                    "src/main/java/com/enviouse/sef/fancytags/FancyTagObjectStore.java", "object and backup content", "hash, bounds, atomic publication, and recovery"),
            boundary("optional-integration", "optional mod and provider integration", "SEFAUD-PHASE-005",
                    "src/main/java/com/enviouse/sef/events/ExternalModLoadingEvent.java", "optional dependency and provider response", "runtime guard, failure containment, and core fallback"),
            boundary("mixin-access", "mixin and access transformer trust hooks", "SEFAUD-PHASE-005",
                    "src/main/resources/sef.mixins.json", "loader transformation", "narrow target, required injection, and side isolation"),
            boundary("audit-export", "audit and export projection", "SEFAUD-PHASE-001",
                    "src/main/java/com/enviouse/sef/storage/StorageExportService.java", "stored records and export destination", "viewer authorization, bounded projection, and safe destination"),
            boundary("audit-log", "mandatory security audit sink", "SEFAUD-PHASE-001",
                    "src/main/java/com/enviouse/sef/audit/SecurityAuditService.java", "security event metadata", "redaction, bounded fields, and durable failure handling"),
            boundary("optional-file-log", "optional ordinary file logging", "SEFAUD-PHASE-001",
                    "src/main/java/com/enviouse/sef/commandlog/FileLogSink.java", "diagnostic event", "filtering, redaction, fixed root, rotation, and retention"),
            boundary("offline-player-data", "offline player NBT and inventory data", "SEFAUD-PHASE-002",
                    "src/main/java/com/enviouse/sef/invsee/OfflinePlayerInventoryAdapter.java", "offline UUID and player data", "UUID binding, revision, backup, and conflict refusal"),
            boundary("privacy-observation", "observation and sensitive output projection", "SEFAUD-PHASE-001",
                    "src/main/java/com/enviouse/sef/kernel/observation/ObservationContracts.java", "private event and viewer identity", "viewer specific projection and redaction before storage"),
            boundary("message-output", "command feedback and message templates", "SEFAUD-PHASE-004",
                    "src/main/java/com/enviouse/sef/message/MessageService.java", "player supplied values", "typed fields, bounded text, and no raw secret interpolation"),
            boundary("runtime-dependency", "platform supplied runtime dependencies", "SEFAUD-PHASE-001",
                    "build.gradle", "dependency declaration and runtime class path", "pinned ownership, binary compatibility, and no duplicate native runtime"),
            boundary("artifact-package", "mod artifact and generated metadata", "SEFAUD-PHASE-006",
                    "src/main/templates/META-INF/neoforge.mods.toml", "build and packaging inputs", "namespace, loader metadata, resource scope, and duplicate scan"),
            boundary("evidence-custody", "sanitized audit evidence custody", "SEFAUD-PHASE-007",
                    "src/test/java/com/enviouse/sef/docs/AuditEvidenceContract.java", "command output and host evidence", "schema, sanitization, bounds, retention, and external root"));

    private BoundaryInventoryGenerator() {
    }

    public static JsonObject generate(Path repositoryRoot) throws IOException {
        JsonArray rows = new JsonArray();
        for (Boundary boundary : BOUNDARIES) {
            Path source = repositoryRoot.resolve(boundary.sourceLocation()).normalize();
            if (!source.startsWith(repositoryRoot) || !Files.isRegularFile(source)) {
                throw new IllegalStateException("trust boundary source is missing " + boundary.sourceLocation());
            }
            JsonObject row = row(boundary.id(), boundary.sourceLocation(), boundary.laterPhase());
            row.addProperty("boundary", boundary.description());
            row.addProperty("inputClass", boundary.inputClass());
            row.addProperty("validation", boundary.validation());
            row.addProperty("mutationBinding", "explicit owner required");
            row.addProperty("failureBehavior", "fail closed or preserve prior valid state");
            row.addProperty("dataFlow", "source to validation to policy to mutation to persistence to observation");
            row.addProperty("evidenceRequired", "static, unit, runtime, adversarial, and artifact as mapped by later phase");
            rows.add(row);
        }
        AuditEvidenceContract.validateInventorySet(rows);
        AuditDriftValidator.requireTraceability(rows);
        JsonObject inventory = new JsonObject();
        inventory.addProperty("schemaVersion", AuditEvidenceContract.SCHEMA_VERSION);
        inventory.addProperty("inventoryId", "sef-boundary-inventory-p000");
        inventory.addProperty("phase", PHASE);
        inventory.addProperty("task", "P000-TASK-002");
        inventory.addProperty("source", "explicit trust boundary and data flow contract sources");
        inventory.addProperty("boundaryCount", rows.size());
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

    private static JsonObject row(String id, String sourceLocation, String laterPhase) {
        JsonObject row = new JsonObject();
        row.addProperty("schemaVersion", AuditEvidenceContract.SCHEMA_VERSION);
        row.addProperty("rowId", "trust-boundary:" + id);
        row.addProperty("category", "trust-boundary");
        row.addProperty("semanticKey", id);
        row.addProperty("owner", "repository-audit-contract");
        row.addProperty("phase", laterPhase);
        row.addProperty("evidenceRoute", "external restricted evidence root trust boundary inventory");
        row.addProperty("evidenceClass", "static");
        row.addProperty("disposition", "implemented");
        row.add("invalidatedBy", strings("source-change", "trust-boundary-change", "policy-change"));
        row.add("sourceLocations", strings(sourceLocation));
        row.addProperty("laterPhase", laterPhase);
        row.addProperty("requirement", "SEFAUD-REQ-001");
        row.addProperty("canonicalOwner", "repository-audit-contract");
        row.addProperty("traceabilityId", "SEFAUD-REQ-001:trust-boundary:" + id);
        return row;
    }

    private static JsonArray strings(String... values) {
        JsonArray array = new JsonArray();
        for (String value : values) {
            array.add(value);
        }
        return array;
    }

    private static Boundary boundary(String id, String description, String laterPhase,
                                     String sourceLocation, String inputClass, String validation) {
        return new Boundary(id, description, laterPhase, sourceLocation, inputClass, validation);
    }

    private record Boundary(
            String id,
            String description,
            String laterPhase,
            String sourceLocation,
            String inputClass,
            String validation) {
    }
}
