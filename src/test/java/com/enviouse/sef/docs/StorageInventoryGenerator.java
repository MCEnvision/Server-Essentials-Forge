package com.enviouse.sef.docs;

import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.storage.repository.StorageCoordinator;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Derives the Phase 000 durable-owner inventory from repository implementations,
 * runtime registrations, and every source-level durable write API.
 */
public final class StorageInventoryGenerator {
    private static final Pattern IMPLEMENTATION = Pattern.compile(
            "\\bclass\\s+([A-Za-z0-9_]+)[^{]*\\bimplements\\s+StorageRepository\\b");
    private static final Pattern ID_METHOD = Pattern.compile(
            "String\\s+id\\s*\\(\\s*\\)\\s*\\{\\s*return\\s+\"([^\"]+)\"");
    private static final Pattern SCHEMA = Pattern.compile("SCHEMA_VERSION\\s*=\\s*(\\d+)");
    private static final Pattern DURABLE_WRITE = Pattern.compile(
            "\\b(StorageService\\.write|AtomicFileStore\\.write|Files\\.writeString|Files\\.write|NbtIo\\.write(?:Compressed)?)\\b");
    private static final List<DurableOwner> NON_REPOSITORY_OWNERS = List.of(
            new DurableOwner("audit-jsonl", "SecurityAuditService", "jsonl", "audit directory", "src/main/java/com/enviouse/sef/audit/SecurityAuditService.java"),
            new DurableOwner("file-command-log", "FileLogSink", "jsonl", "command log directory", "src/main/java/com/enviouse/sef/commandlog/FileLogSink.java"),
            new DurableOwner("player-profile-worker", "PlayerProfileRepository", "json", "player profile directory", "src/main/java/com/enviouse/sef/identity/PlayerProfileRepository.java"),
            new DurableOwner("fancy-tag-projects", "FancyTagProjectStore", "archive", "client project directory", "src/main/java/com/enviouse/sef/gui/client/FancyTagProjectStore.java"),
            new DurableOwner("fancy-tag-client-cache", "FancyTagClientCache", "binary cache", "client cache directory", "src/main/java/com/enviouse/sef/gui/client/FancyTagClientCache.java"),
            new DurableOwner("offline-player-inventory", "OfflinePlayerInventoryAdapter", "nbt", "player data directory", "src/main/java/com/enviouse/sef/invsee/OfflinePlayerInventoryAdapter.java"),
            new DurableOwner("module-configuration", "ModuleConfigService", "toml", "config/sef/modules", "src/main/java/com/enviouse/sef/config/modules/ModuleConfigService.java"),
            new DurableOwner("alternate-account-salt", "AltTracker", "binary", "alternate account data directory", "src/main/java/com/enviouse/sef/alts/AltTracker.java"));
    private static final List<NativeWriterContract> SECURITY_SENSITIVE_WRITERS = List.of(
            new NativeWriterContract("audit-jsonl", "SecurityAuditService", "native-audit-descriptor", "jsonl",
                    "src/main/java/com/enviouse/sef/audit/SecurityAuditService.java"),
            new NativeWriterContract("file-command-log", "FileLogSink", "atomic-file-store", "jsonl",
                    "src/main/java/com/enviouse/sef/commandlog/FileLogSink.java"),
            new NativeWriterContract("player-profile-worker", "PlayerProfileRepository", "atomic-file-store", "json",
                    "src/main/java/com/enviouse/sef/identity/PlayerProfileRepository.java"),
            new NativeWriterContract("fancy-tag-projects", "FancyTagProjectStore", "atomic-file-store", "archive",
                    "src/main/java/com/enviouse/sef/gui/client/FancyTagProjectStore.java"),
            new NativeWriterContract("fancy-tag-client-cache", "FancyTagClientCache", "atomic-file-store", "binary-cache",
                    "src/main/java/com/enviouse/sef/gui/client/FancyTagClientCache.java"),
            new NativeWriterContract("offline-player-inventory", "OfflinePlayerInventoryAdapter", "atomic-file-store", "nbt",
                    "src/main/java/com/enviouse/sef/invsee/OfflinePlayerInventoryAdapter.java"),
            new NativeWriterContract("module-configuration", "ModuleConfigService", "atomic-file-store", "toml",
                    "src/main/java/com/enviouse/sef/config/modules/ModuleConfigService.java"),
            new NativeWriterContract("alternate-account-salt", "AltTracker", "atomic-file-store", "binary",
                    "src/main/java/com/enviouse/sef/alts/AltTracker.java"));
    private static final List<String> MANDATORY_PLATFORMS = List.of("linux", "macos", "windows");

    private StorageInventoryGenerator() {
    }

    public static JsonObject generate(Path repositoryRoot) throws IOException {
        KernelServices.initialize();
        JsonArray rows = new JsonArray();
        List<StaticRepository> implementations = discoverRepositories(repositoryRoot);
        Set<String> implementationIds = new HashSet<>();
        for (StaticRepository implementation : implementations) {
            if (!implementation.id().isBlank()) {
                implementationIds.add(implementation.id());
            }
            JsonObject row = row(
                    "store-implementation",
                    implementation.className(),
                    "static",
                    implementation.sourceLocation());
            row.addProperty("repositoryId", implementation.id());
            row.addProperty("repositorySchemaVersion", implementation.schemaVersion());
            row.addProperty("pathClass", "managed-root-relative");
            row.addProperty("writerApi", "StorageRepository.flush");
            rows.add(row);
        }

        StorageCoordinator coordinator = KernelServices.storage();
        List<StorageCoordinator.Diagnostic> diagnostics = coordinator.diagnostics();
        Set<String> runtimeIds = new TreeSet<>();
        for (StorageCoordinator.Diagnostic diagnostic : diagnostics) {
            runtimeIds.add(diagnostic.id());
            if (!implementationIds.contains(diagnostic.id())) {
                throw new IllegalStateException("runtime store has no implementation row " + diagnostic.id());
            }
            JsonObject row = row(
                    "store-registration",
                    diagnostic.id(),
                    "runtime",
                    "src/main/java/com/enviouse/sef/kernel/KernelServices.java");
            row.addProperty("domain", diagnostic.domain());
            row.addProperty("repositorySchemaVersion", diagnostic.schemaVersion());
            row.addProperty("state", diagnostic.state().name().toLowerCase());
            row.addProperty("dirty", diagnostic.dirty());
            row.addProperty("registered", true);
            rows.add(row);
        }

        for (DurableOwner owner : NON_REPOSITORY_OWNERS) {
            if (!Files.isRegularFile(repositoryRoot.resolve(owner.sourceLocation()))) {
                throw new IllegalStateException("nonrepository owner source is missing " + owner.sourceLocation());
            }
            JsonObject row = row("durable-owner", owner.id(), "static", owner.sourceLocation());
            row.addProperty("ownerType", owner.ownerType());
            row.addProperty("format", owner.format());
            row.addProperty("ownedRootClass", owner.ownedRootClass());
            row.addProperty("writerApi", "owner-specific writer");
            rows.add(row);
        }
        for (NativeWriterContract writer : SECURITY_SENSITIVE_WRITERS) {
            if (!Files.isRegularFile(repositoryRoot.resolve(writer.sourceLocation()))) {
                throw new IllegalStateException("security-sensitive writer source is missing " + writer.sourceLocation());
            }
            for (String platform : MANDATORY_PLATFORMS) {
                JsonObject writerRow = row(
                        "security-sensitive-writer",
                        writer.id() + ":" + platform,
                        "runtime",
                        writer.sourceLocation());
                writerRow.addProperty("ownerId", writer.id());
                writerRow.addProperty("ownerType", writer.ownerType());
                writerRow.addProperty("format", writer.format());
                writerRow.addProperty("platform", platform);
                writerRow.addProperty("provider", writer.provider());
                writerRow.addProperty("openedObjectIdentity", "required");
                writerRow.addProperty("safeTypeAndLinkState", "required");
                writerRow.addProperty("mutationAndFlushBinding", "same-opened-object");
                writerRow.addProperty("failureBehavior", "fail-closed-preserve-prior-state");
                writerRow.addProperty("externalPrerequisite", "EXT-001");
                writerRow.addProperty("runtimeEvidence", "blocked-until-disposable-host-proof");
                rows.add(writerRow);
            }
        }

        int writerCount = 0;
        Path sourceRoot = repositoryRoot.resolve("src/main/java/com/enviouse/sef");
        try (var paths = Files.walk(sourceRoot)) {
            for (Path path : paths.filter(file -> file.toString().endsWith(".java")).sorted().toList()) {
                String relative = repositoryRoot.relativize(path).toString().replace(path.getFileSystem().getSeparator(), "/");
                String source = Files.readString(path, StandardCharsets.UTF_8);
                Matcher writes = DURABLE_WRITE.matcher(source);
                while (writes.find()) {
                    int line = source.substring(0, writes.start()).split("\\R", -1).length;
                    String semanticKey = relative + ":" + line + ":" + writes.group(1);
                    JsonObject row = row("durable-writer", semanticKey, "static", relative);
                    row.addProperty("writerApi", writes.group(1));
                    row.addProperty("line", line);
                    rows.add(row);
                    writerCount++;
                }
            }
        }

        AuditEvidenceContract.validateInventorySet(rows);
        JsonObject inventory = new JsonObject();
        inventory.addProperty("schemaVersion", AuditEvidenceContract.SCHEMA_VERSION);
        inventory.addProperty("inventoryId", "sef-storage-inventory-p000");
        inventory.addProperty("phase", "SEFAUD-PHASE-000");
        inventory.addProperty("task", "P000-TASK-005");
        inventory.addProperty("source", "storage repository implementations, coordinator diagnostics, and durable writer discovery");
        inventory.addProperty("implementationCount", implementations.size());
        inventory.addProperty("runtimeRegisteredCount", diagnostics.size());
        inventory.addProperty("nonRepositoryOwnerCount", NON_REPOSITORY_OWNERS.size());
        inventory.addProperty("durableWriterCount", writerCount);
        inventory.addProperty("securitySensitiveWriterCount", SECURITY_SENSITIVE_WRITERS.size() * MANDATORY_PLATFORMS.size());
        inventory.addProperty("recoveryCoordinator", "StorageCoordinator");
        inventory.addProperty("rows", rows.size());
        inventory.add("inventoryRows", rows);
        return inventory;
    }

    public static Path write(Path approvedExternalRoot, String fileName, Path repositoryRoot) throws IOException {
        JsonObject inventory = generate(repositoryRoot);
        JsonObject metadata = inventory.deepCopy();
        metadata.remove("inventoryRows");
        return AuditEvidenceContract.writeInventory(
                approvedExternalRoot,
                fileName,
                metadata,
                inventory.getAsJsonArray("inventoryRows"));
    }

    private static List<StaticRepository> discoverRepositories(Path repositoryRoot) throws IOException {
        List<StaticRepository> result = new ArrayList<>();
        Path sourceRoot = repositoryRoot.resolve("src/main/java/com/enviouse/sef");
        try (var paths = Files.walk(sourceRoot)) {
            for (Path path : paths.filter(file -> file.toString().endsWith(".java")).sorted().toList()) {
                String source = Files.readString(path, StandardCharsets.UTF_8).replaceAll("\\s+", " ");
                Matcher implementation = IMPLEMENTATION.matcher(source);
                while (implementation.find()) {
                    String className = implementation.group(1);
                    Matcher id = ID_METHOD.matcher(source);
                    String repositoryId = id.find() ? id.group(1) : "";
                    Matcher schema = SCHEMA.matcher(source);
                    int schemaVersion = schema.find() ? Integer.parseInt(schema.group(1)) : 0;
                    result.add(new StaticRepository(
                            className,
                            repositoryId,
                            schemaVersion,
                            repositoryRoot.relativize(path).toString().replace(path.getFileSystem().getSeparator(), "/")));
                }
            }
        }
        result.sort(Comparator.comparing(StaticRepository::className));
        return List.copyOf(result);
    }

    private static JsonObject row(String category, String semanticKey, String evidenceClass, String sourceLocation) {
        JsonObject row = new JsonObject();
        row.addProperty("schemaVersion", AuditEvidenceContract.SCHEMA_VERSION);
        row.addProperty("rowId", category + ":" + stableSuffix(semanticKey));
        row.addProperty("category", category);
        row.addProperty("semanticKey", semanticKey);
        row.addProperty("owner", "repository-audit-contract");
        row.addProperty("phase", category.startsWith("store")
                ? "SEFAUD-PHASE-002"
                : category.equals("security-sensitive-writer") ? "SEFAUD-PHASE-001" : "SEFAUD-PHASE-000");
        row.addProperty("evidenceRoute", "external restricted evidence root storage inventory");
        row.addProperty("evidenceClass", evidenceClass);
        row.addProperty("disposition", "implemented");
        JsonArray invalidatedBy = new JsonArray();
        invalidatedBy.add("storage-registration-change");
        invalidatedBy.add("schema-change");
        invalidatedBy.add("durable-writer-change");
        row.add("invalidatedBy", invalidatedBy);
        JsonArray sources = new JsonArray();
        sources.add(sourceLocation);
        row.add("sourceLocations", sources);
        return row;
    }

    private static String stableSuffix(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9_.-]+", "_");
    }

    private record StaticRepository(String className, String id, int schemaVersion, String sourceLocation) {
    }

    private record DurableOwner(String id, String ownerType, String format, String ownedRootClass, String sourceLocation) {
    }

    private record NativeWriterContract(
            String id,
            String ownerType,
            String provider,
            String format,
            String sourceLocation) {
    }
}
