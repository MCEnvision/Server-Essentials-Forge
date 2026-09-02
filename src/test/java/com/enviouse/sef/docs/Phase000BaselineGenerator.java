package com.enviouse.sef.docs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Captures the phase 000 baseline without reading unrelated workspace state. */
public final class Phase000BaselineGenerator {
    private static final String PHASE = "SEFAUD-PHASE-000";
    private static final String TASK = "P000-TASK-001";
    private static final String LINEAGE_BASE = "0c75bf25c58622096dfa7cc65a5f4b32e6d60ac4";
    private static final List<String> REQUIRED_PLATFORMS = List.of("linux", "macos", "windows");
    private static final List<String> ARTIFACT_INPUTS = List.of(
            "gradle.properties",
            "settings.gradle",
            "build.gradle",
            "gradlew",
            "gradle/wrapper/gradle-wrapper.jar",
            "gradle/wrapper/gradle-wrapper.properties",
            "src/main/resources/META-INF/neoforge.mods.toml",
            "src/main/resources/sef.mixins.json",
            "src/main/resources/META-INF/accesstransformer.cfg");

    private Phase000BaselineGenerator() {
    }

    public static JsonObject generate(Path repositoryRoot) throws IOException {
        Path root = repositoryRoot.toAbsolutePath().normalize();
        if (!Files.isDirectory(root.resolve(".git"))) {
            throw new IOException("repository metadata is unavailable");
        }

        String commit = git(root, "rev-parse", "HEAD");
        String tree = git(root, "rev-parse", "HEAD^{tree}");
        String branch = git(root, "rev-parse", "--abbrev-ref", "HEAD");
        String origin = git(root, "remote", "get-url", "origin");
        boolean lineagePresent = isAncestor(root, LINEAGE_BASE, commit);
        boolean legacyImported = isAncestor(root, "origin/forge-1.20.1", commit);
        DirtyState dirty = dirtyState(root);

        JsonArray rows = new JsonArray();
        addPlatformRows(rows);
        addPrerequisiteRows(rows);
        addArtifactRows(rows, root);
        addRemoteRows(rows, root);

        JsonObject baseline = new JsonObject();
        baseline.addProperty("schemaVersion", AuditEvidenceContract.SCHEMA_VERSION);
        baseline.addProperty("inventoryId", "sef-phase000-execution-baseline");
        baseline.addProperty("phase", PHASE);
        baseline.addProperty("task", TASK);
        baseline.addProperty("source", "git, pinned build inputs, mandatory platform contract, and prerequisite state");
        baseline.addProperty("commit", commit);
        baseline.addProperty("tree", tree);
        baseline.addProperty("branch", branch.isBlank() || branch.equals("HEAD") ? "detached" : branch);
        baseline.addProperty("origin", origin);
        baseline.addProperty("lineageBase", LINEAGE_BASE);
        baseline.addProperty("lineageBasePresent", lineagePresent);
        baseline.addProperty("legacyForgeLineImported", legacyImported);
        baseline.addProperty("trackedTreeClean", dirty.trackedClean());
        baseline.addProperty("untrackedCount", dirty.untrackedCount());
        baseline.addProperty("preservedPlaywrightState", dirty.playwrightStatePresent());
        baseline.addProperty("playwrightStateDisposition", dirty.playwrightStatePresent()
                ? "preserved-untracked-and-not-read"
                : "not-present-at-capture");
        baseline.addProperty("mandatoryPlatformCount", REQUIRED_PLATFORMS.size());
        baseline.addProperty("externalPrerequisiteCount", 2);
        baseline.addProperty("externalPrerequisiteState", "unknown, dependent evidence blocked");
        baseline.addProperty("artifactInputCount", count(rows, "artifact-input"));
        baseline.addProperty("rowCount", rows.size());
        baseline.add("rows", rows);
        return baseline;
    }

    public static Path write(Path approvedExternalRoot, String fileName, Path repositoryRoot) throws IOException {
        JsonObject baseline = generate(repositoryRoot);
        return AuditEvidenceContract.writeInventory(
                approvedExternalRoot,
                fileName,
                baseline,
                baseline.getAsJsonArray("rows"));
    }

    private static void addPlatformRows(JsonArray rows) {
        for (String platform : REQUIRED_PLATFORMS) {
            JsonObject row = row("operating-system", platform, "runtime", "gradle.properties");
            row.addProperty("support", "mandatory");
            row.addProperty("runtime", "java-21,minecraft-1.21.1,neoforge-21.1.235");
            row.addProperty("provider", platform.equals("windows") ? "windows-native-handle" : "posix-native-descriptor");
            row.addProperty("externalPrerequisite", "EXT-001");
            row.addProperty("availability", "unknown");
            row.addProperty("dependentEvidence", "blocked");
            rows.add(row);
        }
    }

    private static void addPrerequisiteRows(JsonArray rows) {
        JsonObject ext001 = row("external-prerequisite", "EXT-001", "blocked", "docs/general/plan.md");
        ext001.addProperty("availability", "unknown");
        ext001.addProperty("dependentEvidence", "blocked");
        ext001.addProperty("consumer", "SEFAUD-REQ-002,SEFAUD-REQ-003,SEFAUD-REQ-006,SEFAUD-REQ-007,SEFAUD-REQ-008,SEFAUD-REQ-009");
        ext001.addProperty("missing", "direct disposable Linux, macOS, and Windows server, client, native-writer, restart, and failure evidence");
        rows.add(ext001);

        JsonObject ext002 = row("external-prerequisite", "EXT-002", "blocked", "docs/general/plan.md");
        ext002.addProperty("availability", "unknown");
        ext002.addProperty("dependentEvidence", "blocked");
        ext002.addProperty("consumer", "SEFAUD-REQ-003,SEFAUD-REQ-008,SEFAUD-REQ-009");
        ext002.addProperty("missing", "authoritative NeoForge runtime provenance, advisory applicability, and compatible remediation");
        rows.add(ext002);
    }

    private static void addArtifactRows(JsonArray rows, Path root) throws IOException {
        for (String relative : ARTIFACT_INPUTS) {
            Path input = root.resolve(relative).normalize();
            if (!input.startsWith(root) || !Files.isRegularFile(input, LinkOption.NOFOLLOW_LINKS)) {
                JsonObject missing = row("artifact-input", relative, "static", relative);
                missing.addProperty("availability", "missing");
                missing.addProperty("dependentEvidence", "blocked");
                rows.add(missing);
                continue;
            }
            JsonObject present = row("artifact-input", relative, "static", relative);
            present.addProperty("availability", "present");
            present.addProperty("sha256", sha256(input));
            present.addProperty("dependentEvidence", "available");
            rows.add(present);
        }
    }

    private static void addRemoteRows(JsonArray rows, Path root) throws IOException {
        List<String> refs = new ArrayList<>();
        String output = git(root, "for-each-ref", "--format=%(refname)=%(objectname)", "refs/remotes/origin");
        if (!output.isBlank()) {
            refs.addAll(Arrays.stream(output.split("\\R"))
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .sorted()
                    .toList());
        }
        for (String ref : refs) {
            String[] parts = ref.split("=", 2);
            JsonObject row = row("remote-ref", parts[0], "remote", "git remote tracking refs");
            row.addProperty("object", parts.length == 2 ? parts[1] : "unknown");
            row.addProperty("readOnly", true);
            rows.add(row);
        }
    }

    private static JsonObject row(String category, String semanticKey, String evidenceClass, String sourceLocation) {
        JsonObject row = new JsonObject();
        row.addProperty("schemaVersion", AuditEvidenceContract.SCHEMA_VERSION);
        row.addProperty("rowId", category + ":" + stableSuffix(semanticKey));
        row.addProperty("category", category);
        row.addProperty("semanticKey", semanticKey);
        row.addProperty("owner", "repository-audit-contract");
        row.addProperty("phase", PHASE);
        row.addProperty("evidenceRoute", "external restricted evidence root phase 000 baseline");
        row.addProperty("evidenceClass", evidenceClass);
        row.addProperty("disposition", evidenceClass.equals("blocked") ? "blocked" : "implemented");
        JsonArray invalidatedBy = new JsonArray();
        invalidatedBy.add("commit-change");
        invalidatedBy.add("build-input-change");
        invalidatedBy.add("platform-contract-change");
        row.add("invalidatedBy", invalidatedBy);
        JsonArray sources = new JsonArray();
        sources.add(sourceLocation);
        row.add("sourceLocations", sources);
        return row;
    }

    private static int count(JsonArray rows, String category) {
        int result = 0;
        for (var element : rows) {
            if (element.getAsJsonObject().get("category").getAsString().equals(category)) {
                result++;
            }
        }
        return result;
    }

    private static DirtyState dirtyState(Path root) throws IOException {
        String output = git(root, "status", "--porcelain=v1", "--untracked-files=all");
        boolean trackedClean = true;
        int untracked = 0;
        boolean playwright = false;
        for (String line : output.split("\\R")) {
            if (line.isBlank()) {
                continue;
            }
            if (line.startsWith("?? ")) {
                untracked++;
                String path = line.substring(3).trim().replace('\\', '/');
                if (path.equals(".playwright-mcp") || path.startsWith(".playwright-mcp/")) {
                    playwright = true;
                }
            } else {
                trackedClean = false;
            }
        }
        return new DirtyState(trackedClean, untracked, playwright);
    }

    private static boolean isAncestor(Path root, String ancestor, String descendant) throws IOException {
        ProcessBuilder builder = new ProcessBuilder("git", "merge-base", "--is-ancestor", ancestor, descendant)
                .directory(root.toFile());
        Process process = builder.start();
        try (InputStream ignored = process.getInputStream(); InputStream errors = process.getErrorStream()) {
            errors.transferTo(java.io.OutputStream.nullOutputStream());
            ignored.transferTo(java.io.OutputStream.nullOutputStream());
        }
        try {
            return process.waitFor() == 0;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("git ancestry inspection interrupted", exception);
        }
    }

    private static String git(Path root, String... arguments) throws IOException {
        List<String> command = new ArrayList<>();
        command.add("git");
        command.addAll(Arrays.asList(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String output;
        try (InputStream stream = process.getInputStream()) {
            output = new String(stream.readAllBytes(), StandardCharsets.UTF_8).trim();
        }
        try {
            int exit = process.waitFor();
            if (exit != 0) {
                throw new IOException("git inspection failed for " + arguments[0]);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("git inspection interrupted", exception);
        }
        return output;
    }

    private static String sha256(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = Files.newInputStream(path)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("sha-256 is unavailable", exception);
        }
    }

    private static String stableSuffix(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]+", "_");
    }

    private record DirtyState(boolean trackedClean, int untrackedCount, boolean playwrightStatePresent) {
    }
}
