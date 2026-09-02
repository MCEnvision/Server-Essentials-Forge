package com.enviouse.sef.docs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Derives build, dependency, resource, CI, packaging, and remote-security rows
 * without resolving or changing the project configuration.
 */
public final class BuildInventoryGenerator {
    private static final Pattern DECLARATION = Pattern.compile(
            "\\b(implementation|compileOnly|runtimeOnly|testImplementation|testRuntimeOnly|minecraft|neoForge|repositories|sourceSets)\\b");
    private static final Pattern TASK = Pattern.compile("\\b(tasks\\.register|tasks\\.named|task)\\s*['\\\"]?([A-Za-z0-9_-]+)");
    private static final Pattern JNA_DECLARATION = Pattern.compile(
            "\\bcompileOnly\\s+['\\\"](net\\.java\\.dev\\.jna:(?:jna|jna-platform)):([^'\\\"]+)['\\\"]");
    private static final List<String> PLATFORM_GATES = List.of(
            "candidate-graph",
            "packaged-mod-artifact",
            "installed-runtime-artifact-linux",
            "installed-runtime-artifact-macos",
            "installed-runtime-artifact-windows",
            "affected-api-reachability",
            "authoritative-advisory-applicability",
            "authoritative-provenance",
            "compatible-remediation");

    private BuildInventoryGenerator() {
    }

    public static JsonObject generate(Path repositoryRoot) throws IOException {
        JsonArray rows = new JsonArray();
        int dependencyDeclarations = 0;
        int buildTasks = 0;
        Path buildFile = repositoryRoot.resolve("build.gradle");
        if (Files.isRegularFile(buildFile)) {
            String source = Files.readString(buildFile, StandardCharsets.UTF_8);
            Matcher declarations = DECLARATION.matcher(source);
            while (declarations.find()) {
                int line = line(source, declarations.start());
                JsonObject row = row(
                        "dependency-declaration",
                        "build.gradle:" + line + ":" + declarations.group(1) + ":" + declarations.start(),
                        "static",
                        "build.gradle");
                row.addProperty("declaration", declarations.group(1));
                rows.add(row);
                dependencyDeclarations++;
            }
            Matcher tasks = TASK.matcher(source);
            while (tasks.find()) {
                int line = line(source, tasks.start());
                JsonObject row = row(
                        "build-task",
                        "build.gradle:" + line + ":" + tasks.group(2) + ":" + tasks.start(),
                        "static",
                        "build.gradle");
                row.addProperty("taskName", tasks.group(2));
                rows.add(row);
                buildTasks++;
            }
            Matcher jna = JNA_DECLARATION.matcher(source);
            while (jna.find()) {
                int line = line(source, jna.start());
                JsonObject row = row(
                        "platform-dependency",
                        jna.group(1) + ":" + jna.group(2),
                        "static",
                        "build.gradle");
                row.addProperty("coordinate", jna.group(1) + ":" + jna.group(2));
                row.addProperty("declaration", "compileOnly");
                row.addProperty("line", line);
                row.addProperty("runtimeOwner", "net.neoforged:minecraft-dependencies:1.21.1");
                row.addProperty("runtimeSupply", "required from pinned NeoForge runtime on linux, macos, and windows");
                row.addProperty("closureStatus", "blocked, ext-002");
                rows.add(row);
            }
        }

        List<Path> inputFiles = List.of(
                repositoryRoot.resolve("gradle.properties"),
                repositoryRoot.resolve("settings.gradle"),
                repositoryRoot.resolve("gradlew"),
                repositoryRoot.resolve("gradle/wrapper/gradle-wrapper.jar"),
                repositoryRoot.resolve("gradle/wrapper/gradle-wrapper.properties"));
        for (Path input : inputFiles) {
            if (Files.isRegularFile(input)) {
                String relative = repositoryRoot.relativize(input).toString().replace(input.getFileSystem().getSeparator(), "/");
                JsonObject row = row("artifact-input", relative, "static", relative);
                row.addProperty("kind", "build-and-wrapper-input");
                rows.add(row);
            }
        }

        Path resourceRoot = repositoryRoot.resolve("src/main/resources");
        if (Files.isDirectory(resourceRoot)) {
            try (var paths = Files.walk(resourceRoot)) {
                for (Path path : paths.filter(Files::isRegularFile).sorted().toList()) {
                    String relative = repositoryRoot.relativize(path).toString().replace(path.getFileSystem().getSeparator(), "/");
                    JsonObject row = row("artifact-input", relative, "static", relative);
                    row.addProperty("kind", "packaged-resource");
                    rows.add(row);
                }
            }
        }

        Path workflowRoot = repositoryRoot.resolve(".github");
        if (Files.isDirectory(workflowRoot)) {
            try (var paths = Files.walk(workflowRoot)) {
                for (Path path : paths.filter(file -> file.toString().endsWith(".yml") || file.toString().endsWith(".yaml"))
                        .sorted().toList()) {
                    String relative = repositoryRoot.relativize(path).toString().replace(path.getFileSystem().getSeparator(), "/");
                    rows.add(row("ci-workflow", relative, "remote", relative));
                }
            }
        }
        JsonObject remote = row("remote-security", "dependabot-alerts-default-branch", "remote", "remote://github/security/dependabot");
        remote.addProperty("alertCount", 26);
        remote.addProperty("high", 12);
        remote.addProperty("moderate", 13);
        remote.addProperty("low", 1);
        remote.addProperty("disposition", "blocked");
        remote.addProperty("ownerPhase", "SEFAUD-PHASE-001");
        remote.addProperty("note", "read-only snapshot, applicability and remediation are owned by Phase 001");
        rows.add(remote);
        for (String gate : PLATFORM_GATES) {
            JsonObject gateRow = row(
                    "platform-dependency-gate",
                    "jna:" + gate,
                    "blocked",
                    "build.gradle");
            gateRow.addProperty("dependency", "net.java.dev.jna:jna and jna-platform");
            gateRow.addProperty("gate", gate);
            gateRow.addProperty("status", "blocked, ext-002");
            gateRow.addProperty("requiredPlatforms", "linux, macos, windows");
            gateRow.addProperty("cannotBeClearedBy", "compile-only declaration or absence from mod jar");
            rows.add(gateRow);
        }

        AuditEvidenceContract.validateInventorySet(rows);
        JsonObject inventory = new JsonObject();
        inventory.addProperty("schemaVersion", AuditEvidenceContract.SCHEMA_VERSION);
        inventory.addProperty("inventoryId", "sef-build-inventory-p000");
        inventory.addProperty("phase", "SEFAUD-PHASE-000");
        inventory.addProperty("task", "P000-TASK-007");
        inventory.addProperty("source", "Gradle declarations, wrapper, resources, CI workflows, and read-only remote state");
        inventory.addProperty("dependencyDeclarationCount", dependencyDeclarations);
        inventory.addProperty("buildTaskCount", buildTasks);
        inventory.addProperty("artifactInputCount", count(rows, "artifact-input"));
        inventory.addProperty("resourceInputCount", countKind(rows, "artifact-input", "packaged-resource"));
        inventory.addProperty("workflowCount", count(rows, "ci-workflow"));
        inventory.addProperty("platformDependencyDeclarationCount", count(rows, "platform-dependency"));
        inventory.addProperty("platformDependencyGateCount", count(rows, "platform-dependency-gate"));
        inventory.addProperty("remoteSecuritySnapshot", "blocked, read-only");
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
        row.addProperty("evidenceRoute", "external restricted evidence root build inventory");
        row.addProperty("evidenceClass", evidenceClass);
        row.addProperty("disposition", evidenceClass.equals("blocked") ? "blocked" : "implemented");
        JsonArray invalidatedBy = new JsonArray();
        invalidatedBy.add("build-script-change");
        invalidatedBy.add("dependency-change");
        invalidatedBy.add("resource-change");
        invalidatedBy.add("remote-security-change");
        row.add("invalidatedBy", invalidatedBy);
        JsonArray sources = new JsonArray();
        sources.add(sourceLocation);
        row.add("sourceLocations", sources);
        return row;
    }

    private static int count(JsonArray rows, String category) {
        int count = 0;
        for (var value : rows) {
            if (value.getAsJsonObject().get("category").getAsString().equals(category)) {
                count++;
            }
        }
        return count;
    }

    private static int countKind(JsonArray rows, String category, String kind) {
        int count = 0;
        for (var value : rows) {
            JsonObject row = value.getAsJsonObject();
            if (row.get("category").getAsString().equals(category)
                    && row.has("kind") && row.get("kind").getAsString().equals(kind)) {
                count++;
            }
        }
        return count;
    }

    private static int line(String source, int offset) {
        return source.substring(0, offset).split("\\R", -1).length;
    }

    private static String stableSuffix(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9_.-]+", "_");
    }
}
