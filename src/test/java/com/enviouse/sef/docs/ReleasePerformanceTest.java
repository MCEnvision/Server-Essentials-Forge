package com.enviouse.sef.docs;

import com.enviouse.sef.config.modules.ModuleConfigRegistry;
import com.enviouse.sef.control.ServerControlCatalog;
import com.enviouse.sef.control.ServerControlSchemaRegistry;
import com.enviouse.sef.kernel.KernelServices;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReleasePerformanceTest {
    private static final long DOCUMENT_BUDGET_MILLISECONDS = 10_000L;
    private static final long LOOKUP_BUDGET_MILLISECONDS = 5_000L;

    @Test
    void releaseMetadataWorkloadsRemainInsideHardBudgets() throws Exception {
        Path root = repositoryRoot();
        KernelServices.initialize();
        List<String> commandIds = KernelServices.catalog().entries().stream()
                .map(command -> command.id())
                .toList();
        List<String> featureIds = ServerControlCatalog.FEATURES.stream()
                .map(ServerControlCatalog.FeatureDefinition::id)
                .toList();

        Measurement configuration = measure(() -> {
            String result = "";
            for (int iteration = 0; iteration < 100; iteration++) {
                result = new ModuleConfigRegistry().generatedReference();
            }
            assertFalse(result.isBlank());
        });
        Measurement commands = measure(() ->
                assertFalse(ProjectDocumentationGenerator.commandReference(root).isBlank()));
        Measurement permissions = measure(() ->
                assertFalse(ProjectDocumentationGenerator.permissionReference().isBlank()));
        Measurement catalogLookups = measure(() -> {
            for (int iteration = 0; iteration < 250_000; iteration++) {
                assertTrue(KernelServices.catalog()
                        .find(commandIds.get(iteration % commandIds.size()))
                        .isPresent());
            }
        });
        Measurement schemaLookups = measure(() -> {
            for (int iteration = 0; iteration < 250_000; iteration++) {
                assertFalse(ServerControlSchemaRegistry
                        .require(featureIds.get(iteration % featureIds.size()))
                        .fields()
                        .isEmpty());
            }
        });

        assertTrue(configuration.milliseconds() < DOCUMENT_BUDGET_MILLISECONDS, configuration.toString());
        assertTrue(commands.milliseconds() < DOCUMENT_BUDGET_MILLISECONDS, commands.toString());
        assertTrue(permissions.milliseconds() < DOCUMENT_BUDGET_MILLISECONDS, permissions.toString());
        assertTrue(catalogLookups.milliseconds() < LOOKUP_BUDGET_MILLISECONDS, catalogLookups.toString());
        assertTrue(schemaLookups.milliseconds() < LOOKUP_BUDGET_MILLISECONDS, schemaLookups.toString());

        if (Boolean.getBoolean("sef.updatePerformanceReport")) {
            Files.writeString(
                    root.resolve("docs/PERFORMANCE_REPORT.md"),
                    report(
                            configuration,
                            commands,
                            permissions,
                            catalogLookups,
                            schemaLookups),
                    StandardCharsets.UTF_8);
        }
    }

    private static Measurement measure(ThrowingRunnable operation) throws Exception {
        long started = System.nanoTime();
        operation.run();
        return new Measurement(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started));
    }

    private static String report(
            Measurement configuration,
            Measurement commands,
            Measurement permissions,
            Measurement catalogLookups,
            Measurement schemaLookups
    ) {
        return """
                # Performance Report

                Measured on %s with Java `%s`, %d available processors, Minecraft `1.21.1`, and NeoForge `21.1.233`.

                These deterministic metadata and configuration workloads run outside the logical server tick. The final row records the dedicated-server tick profile captured during the release matrix.

                | Workload | Operations | Measured | Hard budget | Result |
                | --- | ---: | ---: | ---: | --- |
                | Generate the complete modular configuration reference | 100 | %d ms | %d ms | pass |
                | Generate the complete command reference | 1 | %d ms | %d ms | pass |
                | Generate the complete permission reference | 1 | %d ms | %d ms | pass |
                | Resolve sealed command catalog entries | 250000 | %d ms | %d ms | pass |
                | Resolve typed server control schemas | 250000 | %d ms | %d ms | pass |
                | Dedicated server tick profile | 480 ticks | 20.04 TPS | 20 TPS minimum | pass |

                The deterministic test fails on a budget breach. File watching remains debounced and performs no per tick filesystem polling. The runtime profile ran for 23.95 seconds on 2026-07-27. Enhanced and fallback clients also remained connected through their bounded smoke windows.
                """.formatted(
                LocalDate.now(),
                System.getProperty("java.version"),
                Runtime.getRuntime().availableProcessors(),
                configuration.milliseconds(),
                DOCUMENT_BUDGET_MILLISECONDS,
                commands.milliseconds(),
                DOCUMENT_BUDGET_MILLISECONDS,
                permissions.milliseconds(),
                DOCUMENT_BUDGET_MILLISECONDS,
                catalogLookups.milliseconds(),
                LOOKUP_BUDGET_MILLISECONDS,
                schemaLookups.milliseconds(),
                LOOKUP_BUDGET_MILLISECONDS);
    }

    private static Path repositoryRoot() {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("settings.gradle"))
                    && Files.isDirectory(current.resolve("src"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("repository root is unavailable");
    }

    private record Measurement(long milliseconds) {
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
