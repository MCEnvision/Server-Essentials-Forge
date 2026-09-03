package com.enviouse.sef.audit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Runs the native audit writer against a disposable fixture and emits only
 * synthetic, boolean evidence for the cross platform audit workflow.
 */
public final class AuditRuntimeProbe {
    private AuditRuntimeProbe() {
    }

    public static void main(String[] arguments) throws Exception {
        Path output = arguments.length == 0
                ? Path.of("build", "audit", "native-writer-runtime")
                : Path.of(arguments[0]);
        Files.createDirectories(output);
        Path fixture = Files.createTempDirectory(output, "fixture-");
        boolean appendVerified = false;
        boolean flushVerified = false;
        boolean identityValidationVerified = false;
        boolean objectSwapVerified = false;
        boolean rotationVerified = false;
        boolean restartVerified = false;
        boolean writerStopped = false;
        try {
            Path auditDirectory = fixture.resolve("audit");
            Files.createDirectories(auditDirectory);
            Path activeFile = auditDirectory.resolve("security-audit.jsonl");
            byte[] firstEvent = "probe-event-one\n".getBytes(StandardCharsets.UTF_8);
            byte[] secondEvent = "probe-event-two\n".getBytes(StandardCharsets.UTF_8);
            boolean windows = System.getProperty("os.name", "")
                    .toLowerCase(Locale.ROOT)
                    .contains("win");

            try (NativeAuditFileProvider provider = NativeAuditFileProvider.open(auditDirectory)) {
                provider.append(activeFile, firstEvent);
                provider.validate(activeFile);
                appendVerified = Files.readString(activeFile).equals("probe-event-one\n");
                flushVerified = Files.size(activeFile) == firstEvent.length;
                provider.append(activeFile, secondEvent);
                provider.validate(activeFile);
                identityValidationVerified = Files.readString(activeFile)
                        .equals("probe-event-one\nprobe-event-two\n");

                if (windows) {
                    objectSwapVerified = windowsObjectSwapCheck(auditDirectory, activeFile, provider, secondEvent);
                } else {
                    objectSwapVerified = posixObjectSwapCheck(
                            fixture.resolve("moved-audit"), auditDirectory, activeFile, provider, secondEvent);
                }
            }

            Path serviceRoot = fixture.resolve("service-root");
            SecurityAuditService.start(serviceRoot, 1, 1);
            try {
                String issuer = "probe".repeat(16);
                String target = "target".repeat(42);
                for (int index = 0; index < 3_000; index++) {
                    if (!SecurityAuditService.record(SecurityAuditService.AuditEvent.create(
                            "probe",
                            "rotation-" + index,
                            issuer,
                            target,
                            "runtime-probe",
                            "success",
                            "success"))) {
                        throw new IOException("native audit runtime probe could not enqueue event");
                    }
                }
            } finally {
                SecurityAuditService.shutdown();
            }

            Path serviceAudit = serviceRoot.resolve("audit");
            Path serviceActive = serviceAudit.resolve("security-audit.jsonl");
            try (var files = Files.list(serviceAudit)) {
                rotationVerified = Files.exists(serviceActive)
                        && Files.size(serviceActive) > 0L
                        && files.anyMatch(path -> {
                            String name = path.getFileName().toString();
                            return !name.equals("security-audit.jsonl")
                                    && name.startsWith("security-audit.")
                                    && name.endsWith(".jsonl");
                        });
            }

            SecurityAuditService.start(serviceRoot, 1, 1);
            try {
                restartVerified = SecurityAuditService.record(SecurityAuditService.AuditEvent.create(
                        "probe",
                        "restart",
                        "probe",
                        "",
                        "runtime-probe",
                        "success",
                        "success"));
            } finally {
                SecurityAuditService.shutdown();
            }
            writerStopped = !SecurityAuditService.health().writerAlive()
                    && SecurityAuditService.health().queued() == 0;
            restartVerified = restartVerified
                    && Files.readString(serviceActive).contains("restart");

            String providerName = windows ? "windows-native-handle" : "posix-native-descriptor";
            List<String> manifest = List.of(
                    "probe_schema=1",
                    "os_name=" + sanitize(System.getProperty("os.name", "unknown")),
                    "os_arch=" + sanitize(System.getProperty("os.arch", "unknown")),
                    "java_version=" + sanitize(System.getProperty("java.version", "unknown")),
                    "native_provider=" + providerName,
                    "append_verified=" + appendVerified,
                    "native_flush_verified=" + flushVerified,
                    "identity_validation_verified=" + identityValidationVerified,
                    "object_swap_control_verified=" + objectSwapVerified,
                    "rotation_verified=" + rotationVerified,
                    "restart_verified=" + restartVerified,
                    "writer_stopped=" + writerStopped,
                    "manifest_complete=true");
            Files.write(output.resolve("native-writer-runtime-manifest.txt"), manifest, StandardCharsets.UTF_8);
            if (!(appendVerified && flushVerified && identityValidationVerified && objectSwapVerified
                    && rotationVerified && restartVerified && writerStopped)) {
                throw new IllegalStateException("native audit runtime probe did not satisfy its contract");
            }
        } finally {
            deleteTree(fixture);
        }
    }

    private static boolean posixObjectSwapCheck(
            Path movedDirectory,
            Path auditDirectory,
            Path activeFile,
            NativeAuditFileProvider provider,
            byte[] secondEvent
    ) throws IOException {
        Files.move(auditDirectory, movedDirectory);
        Files.createDirectories(auditDirectory);
        Path replacement = auditDirectory.resolve(activeFile.getFileName());
        provider.append(replacement, secondEvent);
        return Files.readString(movedDirectory.resolve(activeFile.getFileName()))
                        .equals("probe-event-one\nprobe-event-two\nprobe-event-two\n")
                && !Files.exists(replacement);
    }

    private static boolean windowsObjectSwapCheck(
            Path auditDirectory,
            Path activeFile,
            NativeAuditFileProvider provider,
            byte[] secondEvent
    ) throws IOException {
        Path movedDirectory = auditDirectory.resolveSibling("moved-audit");
        try {
            Files.move(auditDirectory, movedDirectory);
        } catch (IOException expected) {
            provider.append(activeFile, secondEvent);
            return Files.readString(activeFile).endsWith("probe-event-two\nprobe-event-two\n");
        }
        Files.createDirectories(auditDirectory);
        Path replacement = auditDirectory.resolve(activeFile.getFileName());
        try {
            provider.append(replacement, secondEvent);
            return !Files.exists(replacement);
        } catch (IOException expected) {
            return true;
        }
    }

    private static String sanitize(String value) {
        return value.replaceAll("[\\r\\n\\t\\p{Cntrl}]", " ").replace(' ', '_');
    }

    private static void deleteTree(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }
}
