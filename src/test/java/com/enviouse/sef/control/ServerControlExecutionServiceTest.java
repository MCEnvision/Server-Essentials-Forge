package com.enviouse.sef.control;

import com.enviouse.sef.kernel.ActionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

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
