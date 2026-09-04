package com.enviouse.sef.control;

import com.enviouse.sef.kernel.ActionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerControlExecutionServiceTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void livePolicyNeedsTypedFieldsAndBoundConfirmation() {
        ServerControlRepository repository = new ServerControlRepository();
        repository.load(temporaryDirectory);
        UUID actor = UUID.randomUUID();
        var created = repository.create(
                "maintenance",
                actor,
                null,
                "maintenance",
                "",
                null,
                Map.of());
        ServerControlExecutionService service = new ServerControlExecutionService(repository);
        service.register("maintenance", (record, context) -> ActionResult.success("maintenance activated"));

        assertFalse(service.preview(created.value().id(), created.value().revision()).ready());
        var configured = configureRequired(repository, created.value(), actor);
        var confirmation = service.execute(
                configured.id(),
                actor,
                configured.revision(),
                false,
                context());
        assertEquals(ActionResult.ReasonCode.CONFIRMATION_REQUIRED, confirmation.reason());

        var executed = service.execute(
                configured.id(),
                actor,
                configured.revision(),
                true,
                context());
        assertTrue(executed.successful(), executed.detail());
        assertEquals(ServerControlRepository.RecordState.ACTIVE, executed.value().state());
    }

    @Test
    void missingNativeHandlerFailsClosed() {
        ServerControlRepository repository = new ServerControlRepository();
        repository.load(temporaryDirectory);
        UUID actor = UUID.randomUUID();
        var created = repository.create(
                "maintenance",
                actor,
                null,
                "maintenance",
                "",
                null,
                Map.of());
        var configured = configureRequired(repository, created.value(), actor);
        ServerControlExecutionService service = new ServerControlExecutionService(repository);

        assertFalse(service.preview(configured.id(), configured.revision()).ready());
        var result = service.execute(configured.id(), actor, configured.revision(), true, context());

        assertFalse(result.successful());
        assertEquals(ActionResult.ReasonCode.PROVIDER_ERROR, result.reason());
    }

    @Test
    void missingIntegrationProviderFailsClosed() {
        ServerControlRepository repository = new ServerControlRepository();
        repository.load(temporaryDirectory);
        UUID actor = UUID.randomUUID();
        var created = repository.create(
                "chunk_pregen",
                actor,
                null,
                "pregen",
                "",
                null,
                Map.of());
        var current = created.value();
        current = repository.configure(
                current.id(), actor, "world", "minecraft:overworld", false, current.revision()).value();
        current = repository.configure(
                current.id(), actor, "center", "0 0", false, current.revision()).value();
        current = repository.configure(
                current.id(), actor, "radius", "1000", false, current.revision()).value();
        current = repository.configure(
                current.id(), actor, "provider", "none", false, current.revision()).value();
        ServerControlExecutionService service = new ServerControlExecutionService(repository);

        var result = service.execute(current.id(), actor, current.revision(), true, context());

        assertFalse(result.successful());
        assertEquals(ActionResult.ReasonCode.PROVIDER_ERROR, result.reason());
    }

    @Test
    void unavailableRuntimeFeatureDoesNotReportSuccessOrBecomeActive() {
        ServerControlRepository repository = new ServerControlRepository();
        repository.load(temporaryDirectory);
        UUID actor = UUID.randomUUID();
        var current = repository.create(
                "resource_governor",
                actor,
                null,
                "governor",
                "",
                null,
                Map.of()).value();
        current = repository.configure(
                current.id(), actor, "maximum_tick_millis", "50", false, current.revision()).value();
        current = repository.configure(
                current.id(), actor, "maximum_entities", "1000", false, current.revision()).value();
        current = repository.configure(
                current.id(), actor, "maximum_items", "500", false, current.revision()).value();
        current = repository.configure(
                current.id(), actor, "response", "observe", false, current.revision()).value();
        ServerControlExecutionService service = new ServerControlExecutionService(repository);
        MinecraftServerControlRuntime.registerHandlers(service);

        var preview = service.preview(current.id(), current.revision());
        var result = service.execute(current.id(), actor, current.revision(), true, context());

        assertFalse(preview.ready());
        assertTrue(preview.detail().contains("runtime behavior is unavailable"));
        assertFalse(result.successful());
        assertEquals(ActionResult.ReasonCode.PROVIDER_ERROR, result.reason());
        assertEquals(
                ServerControlRepository.RecordState.OPEN,
                repository.find(current.id()).orElseThrow().state());
    }

    @Test
    void everyUnavailableRuntimeFeatureFailsClosedAcrossGenericActivationAndResolution() {
        ServerControlRepository repository = new ServerControlRepository();
        repository.load(temporaryDirectory);
        ServerControlExecutionService service = new ServerControlExecutionService(repository);
        MinecraftServerControlRuntime.registerHandlers(service);

        var diagnostic = service.diagnostic();
        assertEquals(
                MinecraftServerControlRuntime.unavailableRuntimeFeatures(),
                diagnostic.unavailableIntegrations());
        assertEquals(
                ServerControlSchemaRegistry.schemas().size(),
                diagnostic.schemas());

        for (String feature : MinecraftServerControlRuntime.unavailableRuntimeFeatures()) {
            UUID actor = UUID.randomUUID();
            var created = repository.create(
                    feature,
                    actor,
                    null,
                    feature,
                    "unavailable contract",
                    null,
                    Map.of());
            assertTrue(created.successful(), created.detail());
            var configured = configureRequiredFields(repository, created.value(), actor);

            var preview = service.preview(configured.id(), configured.revision());
            assertFalse(preview.ready(), feature);
            assertTrue(preview.detail().contains("runtime behavior is unavailable"), feature);

            for (UUID executionActor : new UUID[]{actor, new UUID(0L, 0L)}) {
                var result = service.execute(
                        configured.id(),
                        executionActor,
                        configured.revision(),
                        true,
                        context());
                assertFalse(result.successful(), feature);
                assertEquals(ActionResult.ReasonCode.PROVIDER_ERROR, result.reason(), feature);
                var unchanged = repository.find(configured.id()).orElseThrow();
                assertEquals(ServerControlRepository.RecordState.OPEN, unchanged.state(), feature);
                assertEquals(configured.revision(), unchanged.revision(), feature);
            }

            for (ServerControlRepository.RecordState forbidden : new ServerControlRepository.RecordState[]{
                    ServerControlRepository.RecordState.ACTIVE,
                    ServerControlRepository.RecordState.RESOLVED}) {
                var transition = repository.transition(
                        configured.id(),
                        actor,
                        forbidden,
                        configured.revision(),
                        "generic transition");
                assertFalse(transition.successful(), feature + " " + forbidden);
                assertEquals(ActionResult.ReasonCode.PROVIDER_ERROR, transition.reason(), feature + " " + forbidden);
                assertEquals(
                        ServerControlRepository.RecordState.OPEN,
                        repository.find(configured.id()).orElseThrow().state(),
                        feature + " " + forbidden);
            }
        }
    }

    @Test
    void executionClaimIsDurableBeforeHandlerRunsAndCarriesIdempotencyKey() {
        ServerControlRepository repository = new ServerControlRepository();
        repository.load(temporaryDirectory);
        UUID actor = UUID.randomUUID();
        var configured = configureRequired(
                repository,
                repository.create(
                        "maintenance",
                        actor,
                        null,
                        "maintenance",
                        "",
                        null,
                        Map.of()).value(),
                actor);
        ServerControlExecutionService service = new ServerControlExecutionService(repository);
        AtomicInteger effects = new AtomicInteger();
        service.register("maintenance", (record, context) -> {
            effects.incrementAndGet();
            UUID operationId = context.operationId();
            assertEquals("sef:control:" + operationId, context.idempotencyKey());
            ServerControlRepository replacement = new ServerControlRepository();
            replacement.load(temporaryDirectory);
            assertEquals(
                    ServerControlRepository.ExecutionStatus.OUTCOME_UNKNOWN,
                    replacement.execution(operationId).orElseThrow().status());
            return ActionResult.success("maintenance activated");
        });

        var result = service.execute(
                configured.id(),
                actor,
                configured.revision(),
                true,
                context());

        assertTrue(result.successful(), result.detail());
        assertEquals(1, effects.get());
        assertEquals(
                ServerControlRepository.ExecutionStatus.EXECUTED,
                repository.execution(result.value().operationId()).orElseThrow().status());
        var retry = service.execute(
                configured.id(),
                actor,
                configured.revision(),
                true,
                context());
        assertFalse(retry.successful());
        assertEquals(1, effects.get());
    }

    @Test
    void handlerExceptionBecomesDurableUnknownOutcomeAndBlocksRetry() {
        ServerControlRepository repository = new ServerControlRepository();
        repository.load(temporaryDirectory);
        UUID actor = UUID.randomUUID();
        var configured = configureRequired(
                repository,
                repository.create(
                        "maintenance",
                        actor,
                        null,
                        "maintenance",
                        "",
                        null,
                        Map.of()).value(),
                actor);
        ServerControlExecutionService service = new ServerControlExecutionService(repository);
        AtomicInteger effects = new AtomicInteger();
        service.register("maintenance", (record, context) -> {
            effects.incrementAndGet();
            throw new IllegalStateException("test failure");
        });

        var result = service.execute(
                configured.id(),
                actor,
                configured.revision(),
                true,
                context());

        assertFalse(result.successful());
        assertEquals(1, effects.get());
        UUID operationId = repository.executions(ServerControlRepository.ExecutionStatus.OUTCOME_UNKNOWN)
                .getFirst()
                .id();
        ServerControlRepository replacement = new ServerControlRepository();
        replacement.load(temporaryDirectory);
        assertEquals(
                ServerControlRepository.ExecutionStatus.OUTCOME_UNKNOWN,
                replacement.execution(operationId).orElseThrow().status());
        var retry = service.execute(
                configured.id(),
                actor,
                configured.revision(),
                true,
                context());
        assertFalse(retry.successful());
        assertEquals(1, effects.get());
    }

    @Test
    void persistenceFailuresBeforeDispatchPreventTheEffect() {
        for (int failureCall : new int[]{1, 2}) {
            Path root = temporaryDirectory.resolve("failure-" + failureCall);
            ServerControlRepository repository = new ServerControlRepository();
            repository.load(root);
            UUID actor = UUID.randomUUID();
            var configured = configureRequired(
                    repository,
                    repository.create(
                            "maintenance",
                            actor,
                            null,
                            "maintenance",
                            "",
                            null,
                            Map.of()).value(),
                    actor);
            AtomicInteger commits = new AtomicInteger();
            ServerControlExecutionService service = new ServerControlExecutionService(
                    repository,
                    () -> {
                        if (commits.incrementAndGet() == failureCall) {
                            throw new IOException("injected persistence failure");
                        }
                        repository.flush();
                    });
            AtomicInteger effects = new AtomicInteger();
            service.register("maintenance", (record, context) -> {
                effects.incrementAndGet();
                return ActionResult.success("maintenance activated");
            });

            var result = service.execute(
                    configured.id(),
                    actor,
                    configured.revision(),
                    true,
                    context());

            assertFalse(result.successful());
            assertEquals(0, effects.get());
            assertEquals(
                    ServerControlRepository.RecordState.OPEN,
                    repository.find(configured.id()).orElseThrow().state());
        }
    }

    @Test
    void terminalPersistenceFailureDoesNotRepeatTheEffect() {
        ServerControlRepository repository = new ServerControlRepository();
        repository.load(temporaryDirectory);
        UUID actor = UUID.randomUUID();
        var configured = configureRequired(
                repository,
                repository.create(
                        "maintenance",
                        actor,
                        null,
                        "maintenance",
                        "",
                        null,
                        Map.of()).value(),
                actor);
        AtomicInteger commits = new AtomicInteger();
        ServerControlExecutionService service = new ServerControlExecutionService(
                repository,
                () -> {
                    if (commits.incrementAndGet() == 3) {
                        throw new IOException("injected terminal persistence failure");
                    }
                    repository.flush();
                });
        AtomicInteger effects = new AtomicInteger();
        service.register("maintenance", (record, context) -> {
            effects.incrementAndGet();
            return ActionResult.success("maintenance activated");
        });

        var result = service.execute(
                configured.id(),
                actor,
                configured.revision(),
                true,
                context());
        var retry = service.execute(
                configured.id(),
                actor,
                configured.revision(),
                true,
                context());

        assertFalse(result.successful());
        assertFalse(retry.successful());
        assertEquals(1, effects.get());
        assertEquals(
                ServerControlRepository.ExecutionStatus.EXECUTED,
                repository.executions(null).getFirst().status());
    }

    private static ServerControlRepository.ControlRecord configureRequired(
            ServerControlRepository repository,
            ServerControlRepository.ControlRecord current,
            UUID actor
    ) {
        current = repository.configure(
                current.id(), actor, "message", "scheduled maintenance", false, current.revision()).value();
        current = repository.configure(
                current.id(), actor, "deny_login", "true", false, current.revision()).value();
        return current;
    }

    private static ServerControlRepository.ControlRecord configureRequiredFields(
            ServerControlRepository repository,
            ServerControlRepository.ControlRecord current,
            UUID actor
    ) {
        for (ServerControlSchemaRegistry.FieldDefinition field
                : ServerControlSchemaRegistry.require(current.featureId()).fields()) {
            if (!field.required()) {
                continue;
            }
            var configured = repository.configure(
                    current.id(),
                    actor,
                    field.id(),
                    validValue(field),
                    false,
                    current.revision());
            assertTrue(configured.successful(), current.featureId() + " " + field.id() + " " + configured.detail());
            current = configured.value();
        }
        return current;
    }

    private static String validValue(ServerControlSchemaRegistry.FieldDefinition field) {
        return switch (field.type()) {
            case TEXT -> "test";
            case INTEGER, DURATION_SECONDS -> Long.toString(field.minimum());
            case DECIMAL -> field.minimum() + ".0";
            case BOOLEAN -> "false";
            case ENUM -> field.enumValues().stream().sorted().findFirst().orElseThrow();
            case INSTANT -> "2026-01-01T00:00:00Z";
            case UUID -> "00000000-0000-0000-0000-000000000001";
            case RESOURCE_LOCATION -> "minecraft:overworld";
            case HTTPS_URL -> "https://example.com";
            case HASH -> "a".repeat((int) field.minimum());
            case LIST -> "test";
        };
    }

    private static ServerControlExecutionService.ExecutionContext context() {
        return new ServerControlExecutionService.ExecutionContext() {
            @Override
            public Object server() {
                return new Object();
            }

            @Override
            public Object source() {
                return new Object();
            }
        };
    }
}
