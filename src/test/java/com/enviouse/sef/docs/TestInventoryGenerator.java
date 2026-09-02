package com.enviouse.sef.docs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Derives test, fixture, workflow, documentation claim, finding, and gap rows
 * without treating prose or historical counts as executable proof.
 */
public final class TestInventoryGenerator {
    private static final Pattern JUNIT_ANNOTATION = Pattern.compile(
            "@(Test|ParameterizedTest|RepeatedTest|TestFactory|TestTemplate)\\b");
    private static final Pattern GAME_TEST_ANNOTATION = Pattern.compile("@GameTest\\b");
    private static final Pattern GAME_TEST_HOLDER = Pattern.compile("@GameTestHolder\\b");
    private static final Pattern COMMAND = Pattern.compile(
            "(?:`(\\./gradlew(?:\\.bat)?\\s+[^`]+)`|\\b(\\./gradlew(?:\\.bat)?\\s+[A-Za-z][A-Za-z0-9:_-]*))");
    private static final Pattern NUMERIC_CLAIM = Pattern.compile(
            "(?i)\\b(catalog entries|shortcut entries|permission entries|gui descriptors|"
                    + "required gametests?|unit tests?|test cases?|modules?|repositories?)\\s*[:=]\\s*(\\d+)");
    private static final Pattern GAP = Pattern.compile(
            "(?i)\\b(not verified|not run|unavailable|blocked|missing|gap|pending|todo|unknown|unresolved|cannot)");
    private static final Pattern FINDING = Pattern.compile(
            "(?i)\\b(finding|defect|issue|risk|blocker|vulnerability|regression|failure)\\b");
    private static final Set<String> GENERATED_REFERENCES = Set.of(
            "docs/COMMAND_REFERENCE.md",
            "docs/PERMISSION_REFERENCE.md",
            "docs/CONFIGURATION_REFERENCE.md",
            "docs/PERFORMANCE_REPORT.md");

    private TestInventoryGenerator() {
    }

    public static JsonObject generate(Path repositoryRoot) throws IOException {
        JsonArray rows = new JsonArray();
        int testSourceFiles = 0;
        int unitTests = 0;
        int gameTestSourceFiles = 0;
        int gameTests = 0;

        Path testSourceRoot = repositoryRoot.resolve("src/test/java");
        if (Files.isDirectory(testSourceRoot)) {
            try (var paths = Files.walk(testSourceRoot)) {
                for (Path path : paths.filter(file -> file.toString().endsWith(".java")).sorted().toList()) {
                    String relative = relative(repositoryRoot, path);
                    String source = Files.readString(path, StandardCharsets.UTF_8);
                    Matcher junit = JUNIT_ANNOTATION.matcher(source);
                    int fileUnitTests = 0;
                    while (junit.find()) {
                        int line = line(source, junit.start());
                        JsonObject row = row("unit-test", relative + ":" + line + ":" + junit.start(), "static", relative);
                        row.addProperty("annotation", junit.group(1));
                        row.addProperty("proofFidelity", "unit");
                        row.addProperty("runner", "./gradlew test");
                        rows.add(row);
                        unitTests++;
                        fileUnitTests++;
                    }
                    JsonObject suite = row("test-suite", relative, "static", relative);
                    suite.addProperty("unitTestCount", fileUnitTests);
                    suite.addProperty("runner", "./gradlew test --tests " + relative
                            .replace("src/test/java/", "")
                            .replace('/', '.')
                            .replace(".java", ""));
                    rows.add(suite);
                    testSourceFiles++;
                }
            }
        }

        for (Path sourceRoot : List.of(repositoryRoot.resolve("src/main/java"), testSourceRoot)) {
            if (!Files.isDirectory(sourceRoot)) {
                continue;
            }
            try (var paths = Files.walk(sourceRoot)) {
                for (Path path : paths.filter(file -> file.toString().endsWith(".java")).sorted().toList()) {
                    String relative = relative(repositoryRoot, path);
                    String source = Files.readString(path, StandardCharsets.UTF_8);
                    Matcher holder = GAME_TEST_HOLDER.matcher(source);
                    Matcher methods = GAME_TEST_ANNOTATION.matcher(source);
                    int holderCount = 0;
                    while (holder.find()) {
                        int line = line(source, holder.start());
                        JsonObject row = row("gametest-holder", relative + ":" + line + ":" + holder.start(), "runtime", relative);
                        row.addProperty("proofFidelity", "runtime");
                        row.addProperty("runner", "./gradlew runGameTestServer");
                        rows.add(row);
                        holderCount++;
                    }
                    int fileGameTests = 0;
                    while (methods.find()) {
                        int line = line(source, methods.start());
                        JsonObject row = row("gametest", relative + ":" + line + ":" + methods.start(), "runtime", relative);
                        row.addProperty("proofFidelity", "runtime");
                        row.addProperty("runner", "./gradlew runGameTestServer");
                        rows.add(row);
                        gameTests++;
                        fileGameTests++;
                    }
                    if (holderCount > 0 || fileGameTests > 0) {
                        gameTestSourceFiles++;
                    }
                }
            }
        }

        int fixtureCount = addFixtures(repositoryRoot, rows);
        int workflowCount = addWorkflows(repositoryRoot, rows);
        int generatedReferenceCount = addGeneratedReferences(repositoryRoot, rows);
        int documentationClaims = 0;
        int priorFindings = 0;
        int evidenceGaps = 0;
        for (Path document : documentationFiles(repositoryRoot)) {
            String relative = relative(repositoryRoot, document);
            List<String> lines = Files.readAllLines(document, StandardCharsets.UTF_8);
            int offset = 0;
            for (String text : lines) {
                Matcher claims = NUMERIC_CLAIM.matcher(text);
                while (claims.find()) {
                    JsonObject row = row("documentation-claim", relative + ":" + (line(lines, offset)) + ":" + claims.start(), "static", relative);
                    row.addProperty("claimKind", claims.group(1).toLowerCase());
                    row.addProperty("claimedValue", Integer.parseInt(claims.group(2)));
                    row.addProperty("claimText", bounded(text));
                    row.addProperty("status", "requires reconciliation");
                    rows.add(row);
                    documentationClaims++;
                }
                Matcher findings = FINDING.matcher(text);
                while (findings.find()) {
                    JsonObject row = row("prior-finding", relative + ":" + line(lines, offset) + ":" + findings.start(), "static", relative);
                    row.addProperty("findingKind", findings.group(1).toLowerCase());
                    row.addProperty("text", bounded(text));
                    rows.add(row);
                    priorFindings++;
                }
                Matcher gaps = GAP.matcher(text);
                while (gaps.find()) {
                    JsonObject row = row("evidence-gap", relative + ":" + line(lines, offset) + ":" + gaps.start(), "static", relative);
                    row.addProperty("gapKind", gaps.group(1).toLowerCase());
                    row.addProperty("text", bounded(text));
                    row.addProperty("ownerPhase", "SEFAUD-PHASE-000");
                    rows.add(row);
                    evidenceGaps++;
                }
                offset += text.length() + 1;
            }
        }

        AuditEvidenceContract.validateInventorySet(rows);
        JsonObject inventory = new JsonObject();
        inventory.addProperty("schemaVersion", AuditEvidenceContract.SCHEMA_VERSION);
        inventory.addProperty("inventoryId", "sef-test-inventory-p000");
        inventory.addProperty("phase", "SEFAUD-PHASE-000");
        inventory.addProperty("task", "P000-TASK-008");
        inventory.addProperty("source", "test sources, GameTest annotations, fixtures, workflows, generated references, and documentation claims");
        inventory.addProperty("testSourceFileCount", testSourceFiles);
        inventory.addProperty("unitTestCount", unitTests);
        inventory.addProperty("gameTestSourceFileCount", gameTestSourceFiles);
        inventory.addProperty("gameTestCount", gameTests);
        inventory.addProperty("fixtureCount", fixtureCount);
        inventory.addProperty("workflowCount", workflowCount);
        inventory.addProperty("generatedReferenceCount", generatedReferenceCount);
        inventory.addProperty("documentationClaimCount", documentationClaims);
        inventory.addProperty("priorFindingCount", priorFindings);
        inventory.addProperty("evidenceGapCount", evidenceGaps);
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

    private static int addFixtures(Path root, JsonArray rows) throws IOException {
        Path fixtureRoot = root.resolve("src/test/resources");
        if (!Files.isDirectory(fixtureRoot)) {
            return 0;
        }
        int count = 0;
        try (var paths = Files.walk(fixtureRoot)) {
            for (Path path : paths.filter(Files::isRegularFile).sorted().toList()) {
                String relative = relative(root, path);
                JsonObject row = row("fixture", relative, "static", relative);
                row.addProperty("fixtureKind", fixtureKind(relative));
                row.addProperty("proofFidelity", "fixture");
                rows.add(row);
                count++;
            }
        }
        return count;
    }

    private static int addWorkflows(Path root, JsonArray rows) throws IOException {
        int count = 0;
        Path build = root.resolve("build.gradle");
        if (Files.isRegularFile(build)) {
            String source = Files.readString(build, StandardCharsets.UTF_8);
            for (String task : List.of("test", "build", "runServer", "runClient", "runGameTestServer", "generateProjectReferences", "generatePerformanceReport")) {
                if (source.contains(task)) {
                    JsonObject row = row("verification-workflow", "gradle:" + task, "static", "build.gradle");
                    row.addProperty("command", "./gradlew " + task);
                    row.addProperty("proofFidelity", task.contains("run") ? "runtime" : "unit-or-build");
                    rows.add(row);
                    count++;
                }
            }
        }
        for (Path document : documentationFiles(root)) {
            String relative = relative(root, document);
            List<String> lines = Files.readAllLines(document, StandardCharsets.UTF_8);
            int lineNumber = 0;
            for (String text : lines) {
                Matcher commands = COMMAND.matcher(text);
                while (commands.find()) {
                    String command = commands.group(1) != null ? commands.group(1) : commands.group(2);
                    JsonObject row = row("manual-workflow", relative + ":" + lineNumber + ":" + commands.start(), "static", relative);
                    row.addProperty("command", command.trim());
                    row.addProperty("proofFidelity", command.contains("run") ? "runtime" : "manual-or-build");
                    rows.add(row);
                    count++;
                }
                lineNumber++;
            }
        }
        return count;
    }

    private static int addGeneratedReferences(Path root, JsonArray rows) {
        int count = 0;
        for (String relative : GENERATED_REFERENCES.stream().sorted().toList()) {
            Path path = root.resolve(relative);
            if (Files.isRegularFile(path)) {
                JsonObject row = row("generated-reference", relative, "static", relative);
                row.addProperty("generator", "./gradlew generateProjectReferences");
                row.addProperty("proofFidelity", "generated");
                rows.add(row);
                count++;
            }
        }
        return count;
    }

    private static List<Path> documentationFiles(Path root) throws IOException {
        List<Path> result = new java.util.ArrayList<>();
        try (var paths = Files.list(root)) {
            paths.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".md"))
                    .sorted()
                    .forEach(result::add);
        }
        Path docsRoot = root.resolve("docs");
        if (Files.isDirectory(docsRoot)) {
            try (var paths = Files.walk(docsRoot)) {
                for (Path path : paths.filter(Files::isRegularFile)
                        .filter(file -> file.toString().endsWith(".md"))
                        .sorted().toList()) {
                    String relative = relative(root, path);
                    if (relative.startsWith("docs/general/") || relative.startsWith("docs/plan/")) {
                        continue;
                    }
                    result.add(path);
                }
            }
        }
        return result;
    }

    private static JsonObject row(String category, String semanticKey, String evidenceClass, String sourceLocation) {
        JsonObject row = new JsonObject();
        row.addProperty("schemaVersion", AuditEvidenceContract.SCHEMA_VERSION);
        row.addProperty("rowId", category + ":" + stableSuffix(semanticKey));
        row.addProperty("category", category);
        row.addProperty("semanticKey", semanticKey);
        row.addProperty("owner", "repository-audit-contract");
        row.addProperty("phase", "SEFAUD-PHASE-000");
        row.addProperty("evidenceRoute", "external restricted evidence root test inventory");
        row.addProperty("evidenceClass", evidenceClass);
        row.addProperty("disposition", "implemented");
        JsonArray invalidatedBy = new JsonArray();
        invalidatedBy.add("test-source-change");
        invalidatedBy.add("fixture-change");
        invalidatedBy.add("workflow-change");
        invalidatedBy.add("documentation-change");
        invalidatedBy.add("claim-change");
        row.add("invalidatedBy", invalidatedBy);
        JsonArray sources = new JsonArray();
        sources.add(sourceLocation);
        row.add("sourceLocations", sources);
        return row;
    }

    private static String relative(Path root, Path path) {
        return root.relativize(path).toString().replace(path.getFileSystem().getSeparator(), "/");
    }

    private static int line(List<String> lines, int offset) {
        int current = 0;
        for (int index = 0; index < lines.size(); index++) {
            int next = current + lines.get(index).length() + 1;
            if (offset < next) {
                return index + 1;
            }
            current = next;
        }
        return lines.size();
    }

    private static int line(String source, int offset) {
        return source.substring(0, offset).split("\\R", -1).length;
    }

    private static String fixtureKind(String relative) {
        if (relative.contains("gametest") || relative.contains("structures")) {
            return "game-test";
        }
        if (relative.endsWith(".toml")) {
            return "configuration";
        }
        if (relative.endsWith(".json")) {
            return "json-fixture";
        }
        return "file-fixture";
    }

    private static String bounded(String value) {
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 512 ? normalized : normalized.substring(0, 512) + "[truncated]";
    }

    private static String stableSuffix(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9_.-]+", "_");
    }
}
