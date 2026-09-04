package com.enviouse.sef.audit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Runs the native audit writer against a disposable fixture and emits only
 * synthetic evidence for the cross platform audit workflow.
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
        boolean identityTraceVerified = false;
        boolean objectSwapVerified = false;
        boolean failurePreservationVerified = false;
        boolean rotationVerified = false;
        boolean restartVerified = false;
        boolean writerStopped = false;
        List<String> identityTrace = new ArrayList<>();
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
                NativeAuditFileProvider.AppendEvidence firstEvidence = provider.lastAppendEvidence();
                provider.validate(activeFile);
                appendVerified = Files.readString(activeFile).equals("probe-event-one\n");
                flushVerified = firstEvidence != null && firstEvidence.nativeFlushVerified();
                identityTraceVerified = recordIdentityTrace(identityTrace, "initial", firstEvidence);
                provider.append(activeFile, secondEvent);
                NativeAuditFileProvider.AppendEvidence secondEvidence = provider.lastAppendEvidence();
                provider.validate(activeFile);
                identityValidationVerified = Files.readString(activeFile)
                        .equals("probe-event-one\nprobe-event-two\n");
                identityTraceVerified = identityTraceVerified
                        && recordIdentityTrace(identityTrace, "append", secondEvidence);
                failurePreservationVerified = failurePreservationCheck(auditDirectory, activeFile, provider);

                if (windows) {
                    objectSwapVerified = windowsObjectSwapCheck(auditDirectory, activeFile, provider, secondEvent);
                } else {
                    objectSwapVerified = posixObjectSwapCheck(
                            fixture.resolve("moved-audit"), auditDirectory, activeFile, provider, secondEvent);
                }
                NativeAuditFileProvider.AppendEvidence swapEvidence = provider.lastAppendEvidence();
                if (swapEvidence != null && swapEvidence.success()) {
                    identityTraceVerified = identityTraceVerified
                            && recordIdentityTrace(identityTrace, "object_swap", swapEvidence);
                } else {
                    identityTrace.add("case=object_swap,rejected=true");
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
                    "probe_schema=2",
                    "os_name=" + sanitize(System.getProperty("os.name", "unknown")),
                    "os_arch=" + sanitize(System.getProperty("os.arch", "unknown")),
                    "java_version=" + sanitize(System.getProperty("java.version", "unknown")),
                    "native_provider=" + providerName,
                    "append_verified=" + appendVerified,
                    "native_flush_verified=" + flushVerified,
                    "identity_validation_verified=" + identityValidationVerified,
                    "opened_object_identity_trace_verified=" + identityTraceVerified,
                    "object_swap_control_verified=" + objectSwapVerified,
                    "failure_preservation_verified=" + failurePreservationVerified,
                    "rotation_verified=" + rotationVerified,
                    "restart_verified=" + restartVerified,
                    "writer_stopped=" + writerStopped,
                    "manifest_complete=true");
            Files.write(output.resolve("native-writer-runtime-manifest.txt"), manifest, StandardCharsets.UTF_8);
            Files.write(
                    output.resolve("native-writer-identity-trace.txt"),
                    identityTrace,
                    StandardCharsets.UTF_8);
            if (!(appendVerified && flushVerified && identityValidationVerified && objectSwapVerified
                    && identityTraceVerified && failurePreservationVerified
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

    private static boolean failurePreservationCheck(
            Path auditDirectory,
            Path activeFile,
            NativeAuditFileProvider provider
    ) throws IOException {
        Path rejectedDirectory = auditDirectory.resolve("rejected-target");
        Files.createDirectories(rejectedDirectory);
        byte[] before = Files.readAllBytes(activeFile);
        try {
            provider.append(rejectedDirectory, "rejected\n".getBytes(StandardCharsets.UTF_8));
            return false;
        } catch (IOException expected) {
            return Arrays.equals(before, Files.readAllBytes(activeFile))
                    && Files.isDirectory(rejectedDirectory);
        }
    }

    private static boolean recordIdentityTrace(
            List<String> trace,
            String label,
            NativeAuditFileProvider.AppendEvidence evidence
    ) {
        if (evidence == null) {
            return false;
        }
        trace.add("case=" + sanitize(label)
                + ",provider=" + sanitize(evidence.provider())
                + ",before_identity=" + sanitize(evidence.beforeIdentity())
                + ",after_identity=" + sanitize(evidence.afterIdentity())
                + ",before_regular=" + evidence.beforeRegular()
                + ",after_regular=" + evidence.afterRegular()
                + ",before_links=" + evidence.beforeLinks()
                + ",after_links=" + evidence.afterLinks()
                + ",native_flush_verified=" + evidence.nativeFlushVerified()
                + ",same_object=" + evidence.sameObject()
                + ",success=" + evidence.success());
        return evidence.success()
                && evidence.nativeFlushVerified()
                && evidence.sameObject()
                && evidence.beforeRegular()
                && evidence.afterRegular()
                && evidence.beforeLinks() == 1L
                && evidence.afterLinks() == 1L;
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
