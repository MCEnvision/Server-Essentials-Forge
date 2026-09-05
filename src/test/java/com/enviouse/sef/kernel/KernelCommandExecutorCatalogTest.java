package com.enviouse.sef.kernel;

import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.audit.SecurityAuditService;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.control.MinecraftServerControlRuntime;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.permissions.DelegatedPermissionScope;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Answers;
import org.mockito.MockedStatic;

import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Catalog policy regression coverage for the command execution boundary.
 * Route registration and parser coverage do not prove authorization. Every
 * sealed action is checked against a provider that denies every permission.
 */
class KernelCommandExecutorCatalogTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void everyCatalogActionFailsClosedWhenPermissionProviderDenies() {
        KernelServices.initialize();
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(source.getEntity()).thenReturn(player);
        when(player.getUUID()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        ServerLevel level = mock(ServerLevel.class);
        when(level.dimension()).thenReturn(net.minecraft.world.level.Level.OVERWORLD);
        when(player.level()).thenReturn(level);

        List<String> failures = new ArrayList<>();
        try (MockedStatic<PermissionAPI> permissions = denyingPermissionApi()) {
            for (CommandDefinition definition : KernelServices.catalog().entries()) {
                if (KernelCommandExecutor.canUse(source, definition.id())) {
                    failures.add(definition.id());
                }
            }
        }

        assertTrue(
                failures.isEmpty(),
                () -> "catalog actions bypassed a denying permission provider, "
                + String.join(", ", failures.stream().limit(12).toList()));
    }

    @Test
    void everyCatalogActionResolvesPermissionGrantThroughTheSharedManifest() {
        KernelServices.initialize();
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(source.getEntity()).thenReturn(player);
        when(player.getUUID()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000004"));

        List<String> failures = new ArrayList<>();
        try (MockedStatic<PermissionAPI> permissions = grantingPermissionApi()) {
            for (CommandDefinition definition : KernelServices.catalog().entries()) {
                if (!KernelCommandExecutor.canUse(source, definition.id())) {
                    failures.add(definition.id());
                }
            }
        }

        assertTrue(
                failures.isEmpty(),
                () -> "catalog actions did not resolve a granted manifest permission, "
                        + String.join(", ", failures.stream().limit(12).toList()));
    }

    @Test
    void unknownDynamicActionIdsFailClosedWithoutInvokingCallbacks() {
        KernelServices.initialize();
        ServerLevel level = mock(ServerLevel.class);
        when(level.dimension()).thenReturn(net.minecraft.world.level.Level.OVERWORLD);
        CommandSource output = mock(CommandSource.class);
        when(output.acceptsFailure()).thenReturn(true);
        List<String> feedback = new ArrayList<>();
        doAnswer(invocation -> {
            feedback.add(invocation.getArgument(0, Component.class).getString());
            return null;
        }).when(output).sendSystemMessage(org.mockito.ArgumentMatchers.any());
        CommandSourceStack source = new CommandSourceStack(
                output,
                Vec3.ZERO,
                Vec2.ZERO,
                level,
                4,
                "tester",
                Component.literal("tester"),
                mock(MinecraftServer.class),
                null);
        AtomicInteger invocations = new AtomicInteger();

        assertFalse(KernelCommandExecutor.canUse(source, "sef:unknown.dynamic.action"));
        assertFalse(KernelCommandExecutor.authorizeControl(source, "sef:unknown.dynamic.action"));
        assertEquals(
                0,
                KernelCommandExecutor.execute(
                        source,
                        "sef:unknown.dynamic.action",
                        Map.of(),
                        invocations::incrementAndGet));

        assertEquals(0, invocations.get());
        assertEquals(2, feedback.size());
        assertTrue(feedback.stream().allMatch(message ->
                message.equals("That command action is unavailable.")));
    }

    @Test
    void everyCatalogActionRejectsWithBoundedFeedbackAndCorrelatedAudit() throws Exception {
        KernelServices.initialize();
        ServerLevel level = mock(ServerLevel.class);
        when(level.dimension()).thenReturn(net.minecraft.world.level.Level.OVERWORLD);
        ServerPlayer player = mock(ServerPlayer.class);
        UUID actorId = UUID.fromString("00000000-0000-0000-0000-000000000003");
        when(player.getUUID()).thenReturn(actorId);
        when(player.level()).thenReturn(level);
        CommandSource output = mock(CommandSource.class);
        when(output.acceptsFailure()).thenReturn(true);
        List<String> feedback = new ArrayList<>();
        doAnswer(invocation -> {
            feedback.add(invocation.getArgument(0, Component.class).getString());
            return null;
        }).when(output).sendSystemMessage(org.mockito.ArgumentMatchers.any());
        CommandSourceStack source = new CommandSourceStack(
                output,
                Vec3.ZERO,
                Vec2.ZERO,
                level,
                4,
                "tester",
                Component.literal("tester"),
                mock(MinecraftServer.class),
                player);

        SecurityAuditService.start(temporaryDirectory, 7, 1);
        List<CommandDefinition> definitions = KernelServices.catalog().entries();
        try {
            for (CommandDefinition definition : definitions) {
                assertEquals(
                        0,
                        KernelCommandExecutor.reject(
                                source,
                                definition.id(),
                                ActionResult.ReasonCode.INVALID_INPUT,
                                "synthetic bounded rejection"),
                        definition.id());
            }
        } finally {
            SecurityAuditService.shutdown();
        }

        Path auditFile = temporaryDirectory.resolve("audit").resolve("security-audit.jsonl");
        List<JsonObject> events = Files.readAllLines(auditFile, StandardCharsets.UTF_8).stream()
                .filter(line -> !line.isBlank())
                .map(JsonParser::parseString)
                .map(element -> element.getAsJsonObject())
                .toList();
        long auditedDefinitions = definitions.stream()
                .filter(definition -> definition.auditClass() != AuditService.AuditClass.NONE)
                .count();
        assertEquals(definitions.size(), feedback.size());
        assertEquals(auditedDefinitions, events.size());
        Set<String> observedActions = new LinkedHashSet<>();
        for (JsonObject event : events) {
            CommandDefinition definition = KernelServices.catalog()
                    .find(event.get("actionId").getAsString())
                    .orElseThrow(() -> new AssertionError(
                            "audit event references an unknown catalog action "
                                    + event.get("actionId").getAsString()));
            assertEquals("rejected", event.get("result").getAsString());
            assertEquals("invalid_input", event.get("reasonCode").getAsString());
            assertEquals(actorId.toString(), event.get("actorUuid").getAsString());
            assertEquals("tester", event.get("actorUsername").getAsString());
            assertEquals("player", event.get("sourceType").getAsString());
            assertEquals("command", event.get("origin").getAsString());
            assertEquals(
                    definition.auditClass().name().toLowerCase(java.util.Locale.ROOT),
                    event.get("auditClass").getAsString());
            assertEquals("metadata", event.get("redactionClass").getAsString());
            assertFalse(event.get("eventId").getAsString().isBlank());
            assertFalse(event.get("serverSessionId").getAsString().isBlank());
            assertTrue(event.get("definitionRevision").getAsLong() >= 0L);
            assertTrue(event.get("policyRevision").getAsLong() >= 0L);
            assertTrue(event.getAsJsonObject("normalizedParameters").entrySet().isEmpty());
            assertTrue(event.getAsJsonArray("targetUuids").isEmpty());
            assertTrue(event.getAsJsonObject("providerContext").entrySet().isEmpty());
            assertFalse(event.toString().contains("synthetic bounded rejection"));
            observedActions.add(event.get("actionId").getAsString());
        }
        assertEquals(
                definitions.stream()
                        .filter(definition -> definition.auditClass() != AuditService.AuditClass.NONE)
                        .map(CommandDefinition::id)
                        .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)),
                observedActions);
        assertTrue(feedback.stream().allMatch(message -> message.length() <= 256));
    }

    @Test
    void representativeDistinctRejectionReasonsRemainCorrelatedAndRedacted() throws Exception {
        KernelServices.initialize();
        ServerLevel level = mock(ServerLevel.class);
        when(level.dimension()).thenReturn(net.minecraft.world.level.Level.OVERWORLD);
        ServerPlayer player = mock(ServerPlayer.class);
        UUID actorId = UUID.fromString("00000000-0000-0000-0000-000000000016");
        when(player.getUUID()).thenReturn(actorId);
        when(player.level()).thenReturn(level);
        CommandSource output = mock(CommandSource.class);
        when(output.acceptsFailure()).thenReturn(true);
        List<String> feedback = new ArrayList<>();
        doAnswer(invocation -> {
            feedback.add(invocation.getArgument(0, Component.class).getString());
            return null;
        }).when(output).sendSystemMessage(org.mockito.ArgumentMatchers.any());
        CommandSourceStack source = new CommandSourceStack(
                output,
                Vec3.ZERO,
                Vec2.ZERO,
                level,
                4,
                "tester",
                Component.literal("tester"),
                mock(MinecraftServer.class),
                player);
        List<ActionResult.ReasonCode> reasons = List.of(
                ActionResult.ReasonCode.SOURCE_NOT_ALLOWED,
                ActionResult.ReasonCode.PERMISSION_DENIED,
                ActionResult.ReasonCode.INVALID_INPUT,
                ActionResult.ReasonCode.CONFLICT,
                ActionResult.ReasonCode.STORAGE_ERROR,
                ActionResult.ReasonCode.PROVIDER_ERROR);
        Set<String> auditClasses = new LinkedHashSet<>();

        SecurityAuditService.start(temporaryDirectory, 7, 1);
        try {
            for (CommandDefinition definition : KernelServices.catalog().entries()) {
                if (definition.auditClass() == AuditService.AuditClass.NONE
                        || !auditClasses.add(definition.auditClass().name())) {
                    continue;
                }
                for (ActionResult.ReasonCode reason : reasons) {
                    assertEquals(
                            0,
                            KernelCommandExecutor.reject(
                                    source,
                                    definition.id(),
                                    reason,
                                    "synthetic bounded rejection"),
                            definition.id() + " " + reason);
                }
            }
        } finally {
            SecurityAuditService.shutdown();
        }

        Path auditFile = temporaryDirectory.resolve("audit").resolve("security-audit.jsonl");
        List<JsonObject> events = Files.readAllLines(auditFile, StandardCharsets.UTF_8).stream()
                .filter(line -> !line.isBlank())
                .map(JsonParser::parseString)
                .map(element -> element.getAsJsonObject())
                .toList();
        assertEquals(auditClasses.size() * reasons.size(), events.size());
        assertEquals(events.size(), feedback.size());
        assertTrue(feedback.stream().allMatch(message -> message.length() <= 256));
        for (JsonObject event : events) {
            assertEquals("rejected", event.get("result").getAsString());
            assertEquals(actorId.toString(), event.get("actorUuid").getAsString());
            assertEquals("tester", event.get("actorUsername").getAsString());
            assertEquals("player", event.get("sourceType").getAsString());
            assertEquals("command", event.get("origin").getAsString());
            assertEquals("metadata", event.get("redactionClass").getAsString());
            assertTrue(event.getAsJsonObject("normalizedParameters").entrySet().isEmpty());
            assertTrue(event.getAsJsonArray("targetUuids").isEmpty());
            assertFalse(event.toString().contains("synthetic bounded rejection"));
        }
    }

    @Test
    void customLeaseAdaptersFailClosedWhenServerControlPolicyDenies() {
        KernelServices.initialize();
        SecurityAuditService.start(temporaryDirectory, 7, 1);
        ServerPlayer player = mock(ServerPlayer.class);
        when(player.getUUID()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        ServerLevel level = mock(ServerLevel.class);
        when(level.dimension()).thenReturn(net.minecraft.world.level.Level.OVERWORLD);
        when(player.level()).thenReturn(level);
        CommandSource output = mock(CommandSource.class);
        when(output.acceptsFailure()).thenReturn(true);
        List<String> feedback = new ArrayList<>();
        doAnswer(invocation -> {
            feedback.add(invocation.getArgument(0, net.minecraft.network.chat.Component.class).getString());
            return null;
        }).when(output).sendSystemMessage(org.mockito.ArgumentMatchers.any());
        CommandSourceStack source = new CommandSourceStack(
                output,
                Vec3.ZERO,
                Vec2.ZERO,
                level,
                4,
                "tester",
                Component.literal("tester"),
                mock(MinecraftServer.class),
                player);

        try (MockedStatic<MinecraftServerControlRuntime> control = mockStatic(MinecraftServerControlRuntime.class)) {
            control.when(() -> MinecraftServerControlRuntime.authorizeAction(
                            org.mockito.ArgumentMatchers.eq(source),
                            org.mockito.ArgumentMatchers.any(CommandDefinition.class)))
                    .thenReturn(ActionResult.failure(
                            ActionResult.ReasonCode.POLICY_DENIED,
                            "control policy denied"));
            assertFalse(KernelCommandExecutor.authorizeControl(
                    source,
                    KernelServices.catalog().entries().getFirst().id()));
        } finally {
            SecurityAuditService.shutdown();
        }

        assertEquals(1, feedback.size());
        assertTrue(feedback.getFirst().contains("control policy denied"));
    }

    @Test
    void everyCatalogActionRefusesExecutionBeforeInvokingTheActionWhenPermissionProviderDenies() throws Exception {
        KernelServices.initialize();
        SecurityAuditService.start(temporaryDirectory, 7, 1);
        ServerPlayer player = mock(ServerPlayer.class);
        when(player.getUUID()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        ServerLevel level = mock(ServerLevel.class);
        when(level.dimension()).thenReturn(net.minecraft.world.level.Level.OVERWORLD);
        when(player.level()).thenReturn(level);
        CommandSource output = mock(CommandSource.class);
        MinecraftServer server = mock(MinecraftServer.class);
        CommandSourceStack source = new CommandSourceStack(
                output,
                Vec3.ZERO,
                Vec2.ZERO,
                level,
                4,
                "tester",
                Component.literal("tester"),
                server,
                player);

        List<String> failures = new ArrayList<>();
        List<String> feedback = new ArrayList<>();
        AtomicInteger invocations = new AtomicInteger();
        when(output.acceptsFailure()).thenReturn(true);
        doAnswer(invocation -> {
            feedback.add(invocation.getArgument(0, net.minecraft.network.chat.Component.class).getString());
            return null;
        }).when(output).sendSystemMessage(org.mockito.ArgumentMatchers.any());
        Set<String> auditedActions = KernelServices.catalog().entries().stream()
                .filter(definition -> definition.auditClass() != AuditService.AuditClass.NONE)
                .map(CommandDefinition::id)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        try (MockedStatic<PermissionAPI> permissions = denyingPermissionApi()) {
            for (CommandDefinition definition : KernelServices.catalog().entries()) {
                int result = KernelCommandExecutor.execute(
                        source,
                        definition.id(),
                        java.util.Map.of(),
                        invocations::incrementAndGet);
                if (result != 0) {
                    failures.add(definition.id() + " returned " + result);
                }
            }
        } finally {
            SecurityAuditService.shutdown();
        }

        assertTrue(
                failures.isEmpty(),
                () -> "catalog actions executed despite a denying permission provider, "
                        + String.join(", ", failures.stream().limit(12).toList()));
        assertTrue(
                invocations.get() == 0,
                () -> "permission denied catalog actions invoked their callbacks " + invocations.get() + " times");

        Path auditFile = temporaryDirectory.resolve("audit").resolve("security-audit.jsonl");
        List<JsonObject> auditEvents = Files.readAllLines(auditFile, StandardCharsets.UTF_8).stream()
                .filter(line -> !line.isBlank())
                .map(JsonParser::parseString)
                .map(element -> element.getAsJsonObject())
                .toList();
        assertEquals(
                KernelServices.catalog().entries().size(),
                feedback.size(),
                "every denied catalog action must return bounded permission feedback");
        assertEquals(
                auditedActions.size(),
                auditEvents.size(),
                "every audited catalog action must emit one denial event");
        Set<String> observedActions = new LinkedHashSet<>();
        for (JsonObject event : auditEvents) {
            assertEquals("rejected", event.get("result").getAsString());
            assertTrue(!event.get("reasonCode").getAsString().isBlank());
            assertEquals("tester", event.get("actorUsername").getAsString());
            assertEquals("player", event.get("sourceType").getAsString());
            assertTrue(event.getAsJsonObject("normalizedParameters").entrySet().isEmpty());
            observedActions.add(event.get("actionId").getAsString());
        }
        assertEquals(auditedActions, observedActions);
        assertTrue(
                feedback.stream().allMatch(message -> message.length() <= 256),
                "denied feedback must remain bounded");
    }

    @Test
    void successfulExecutionPersistsBoundedMetadataActorAndTargetCorrelation() throws Exception {
        KernelServices.initialize();
        UUID actorId = UUID.fromString("00000000-0000-0000-0000-000000000011");
        UUID targetId = UUID.fromString("00000000-0000-0000-0000-000000000012");
        ServerPlayer player = mock(ServerPlayer.class);
        when(player.getUUID()).thenReturn(actorId);
        ServerLevel level = mock(ServerLevel.class);
        when(level.dimension()).thenReturn(net.minecraft.world.level.Level.OVERWORLD);
        when(player.level()).thenReturn(level);
        CommandSource output = mock(CommandSource.class);
        when(output.acceptsFailure()).thenReturn(true);
        CommandSourceStack source = new CommandSourceStack(
                output,
                Vec3.ZERO,
                Vec2.ZERO,
                level,
                4,
                "tester",
                Component.literal("tester"),
                mock(MinecraftServer.class),
                player);

        SecurityAuditService.start(temporaryDirectory, 7, 1);
        try (MockedStatic<PermissionAPI> permissions = grantingPermissionApi();
                MockedStatic<MinecraftServerControlRuntime> control = mockStatic(MinecraftServerControlRuntime.class)) {
            control.when(() -> MinecraftServerControlRuntime.authorizeAction(
                            org.mockito.ArgumentMatchers.eq(source),
                            org.mockito.ArgumentMatchers.any(CommandDefinition.class)))
                    .thenReturn(ActionResult.success(null));
            assertEquals(
                    1,
                    KernelCommandExecutor.execute(
                            source,
                            "sef:core.test",
                            Map.of("mode", "safe", "message_length", "12"),
                            List.of(targetId),
                            false,
                            () -> 1));
        } finally {
            SecurityAuditService.shutdown();
        }

        Path auditFile = temporaryDirectory.resolve("audit").resolve("security-audit.jsonl");
        List<JsonObject> events = Files.readAllLines(auditFile, StandardCharsets.UTF_8).stream()
                .filter(line -> !line.isBlank())
                .map(JsonParser::parseString)
                .map(element -> element.getAsJsonObject())
                .toList();
        assertEquals(1, events.size());
        JsonObject event = events.getFirst();
        assertEquals("sef:core.test", event.get("actionId").getAsString());
        assertEquals("success", event.get("result").getAsString());
        assertEquals("success", event.get("reasonCode").getAsString());
        assertEquals(actorId.toString(), event.get("actorUuid").getAsString());
        assertEquals("tester", event.get("actorUsername").getAsString());
        assertEquals(targetId.toString(), event.getAsJsonArray("targetUuids").get(0).getAsString());
        assertEquals("safe", event.getAsJsonObject("normalizedParameters").get("mode").getAsString());
        assertEquals("12", event.getAsJsonObject("normalizedParameters").get("message_length").getAsString());
        assertEquals("player", event.get("sourceType").getAsString());
        assertEquals("player", event.getAsJsonObject("providerContext").get("source_class").getAsString());
    }

    @Test
    void everyCatalogActionCompletesThroughSharedExecutorAndAuditsSuccess() throws Exception {
        KernelServices.initialize();
        UUID actorId = UUID.fromString("00000000-0000-0000-0000-000000000019");
        ServerPlayer player = mock(ServerPlayer.class);
        when(player.getUUID()).thenReturn(actorId);
        ServerLevel level = mock(ServerLevel.class);
        when(level.dimension()).thenReturn(net.minecraft.world.level.Level.OVERWORLD);
        when(player.level()).thenReturn(level);
        CommandSource output = mock(CommandSource.class);
        when(output.acceptsFailure()).thenReturn(true);
        CommandSourceStack source = new CommandSourceStack(
                output,
                Vec3.ZERO,
                Vec2.ZERO,
                level,
                4,
                "tester",
                Component.literal("tester"),
                mock(MinecraftServer.class),
                player);

        List<CommandDefinition> definitions = KernelServices.catalog().entries();
        AtomicInteger callbacks = new AtomicInteger();
        Map<String, Integer> results = new java.util.LinkedHashMap<>();
        SecurityAuditService.start(temporaryDirectory, 7, 1);
        try (MockedStatic<PermissionAPI> permissions = grantingPermissionApi();
                MockedStatic<MinecraftServerControlRuntime> control = mockStatic(MinecraftServerControlRuntime.class)) {
            control.when(() -> MinecraftServerControlRuntime.authorizeAction(
                            org.mockito.ArgumentMatchers.eq(source),
                            org.mockito.ArgumentMatchers.any(CommandDefinition.class)))
                    .thenReturn(ActionResult.success(null));
            for (CommandDefinition definition : definitions) {
                int result = KernelCommandExecutor.execute(
                        source,
                        definition.id(),
                        Map.of(),
                        () -> {
                            callbacks.incrementAndGet();
                            return 1;
                        });
                assertTrue(result >= 0, definition.id());
                results.put(definition.id(), result);
            }
        } finally {
            SecurityAuditService.shutdown();
        }

        Path auditFile = temporaryDirectory.resolve("audit").resolve("security-audit.jsonl");
        List<JsonObject> events = Files.readAllLines(auditFile, StandardCharsets.UTF_8).stream()
                .filter(line -> !line.isBlank())
                .map(JsonParser::parseString)
                .map(element -> element.getAsJsonObject())
                .toList();
        long auditedDefinitions = definitions.stream()
                .filter(definition -> definition.auditClass() != AuditService.AuditClass.NONE)
                .count();
        long successfulResults = results.values().stream().filter(result -> result > 0).count();
        assertEquals(successfulResults, callbacks.get());
        assertEquals(auditedDefinitions, events.size());
        Set<String> observedActions = new LinkedHashSet<>();
        for (JsonObject event : events) {
            String actionId = event.get("actionId").getAsString();
            String result = event.get("result").getAsString();
            if (results.get(actionId) > 0) {
                assertEquals("success", result);
                assertEquals("success", event.get("reasonCode").getAsString());
            } else {
                assertTrue(Set.of("rejected", "failed", "outcome_unknown").contains(result));
            }
            assertEquals(actorId.toString(), event.get("actorUuid").getAsString());
            assertEquals("tester", event.get("actorUsername").getAsString());
            assertEquals("player", event.get("sourceType").getAsString());
            assertEquals("command", event.get("origin").getAsString());
            assertTrue(event.getAsJsonObject("normalizedParameters").entrySet().isEmpty());
            assertTrue(event.getAsJsonArray("targetUuids").isEmpty());
            assertFalse(event.toString().contains("tester-secret"));
            observedActions.add(actionId);
        }
        assertEquals(
                definitions.stream()
                        .filter(definition -> definition.auditClass() != AuditService.AuditClass.NONE)
                        .map(CommandDefinition::id)
                        .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)),
                observedActions);
    }

    @Test
    void everyCatalogActionAuditsCallbackFailureWithoutInvokingASecondCallback() throws Exception {
        KernelServices.initialize();
        UUID actorId = UUID.fromString("00000000-0000-0000-0000-000000000020");
        ServerPlayer player = mock(ServerPlayer.class);
        when(player.getUUID()).thenReturn(actorId);
        ServerLevel level = mock(ServerLevel.class);
        when(level.dimension()).thenReturn(net.minecraft.world.level.Level.OVERWORLD);
        when(player.level()).thenReturn(level);
        CommandSource output = mock(CommandSource.class);
        when(output.acceptsFailure()).thenReturn(true);
        CommandSourceStack source = new CommandSourceStack(
                output,
                Vec3.ZERO,
                Vec2.ZERO,
                level,
                4,
                "tester",
                Component.literal("tester"),
                mock(MinecraftServer.class),
                player);

        List<CommandDefinition> definitions = KernelServices.catalog().entries();
        Set<String> callbackActions = new LinkedHashSet<>();
        SecurityAuditService.start(temporaryDirectory, 7, 1);
        try (MockedStatic<PermissionAPI> permissions = grantingPermissionApi();
                MockedStatic<MinecraftServerControlRuntime> control = mockStatic(MinecraftServerControlRuntime.class)) {
            control.when(() -> MinecraftServerControlRuntime.authorizeAction(
                            org.mockito.ArgumentMatchers.eq(source),
                            org.mockito.ArgumentMatchers.any(CommandDefinition.class)))
                    .thenReturn(ActionResult.success(null));
            for (CommandDefinition definition : definitions) {
                int result = KernelCommandExecutor.execute(
                        source,
                        definition.id(),
                        Map.of(),
                        () -> {
                            callbackActions.add(definition.id());
                            return 0;
                });
                assertEquals(0, result, definition.id());
            }
        } finally {
            SecurityAuditService.shutdown();
        }

        Path auditFile = temporaryDirectory.resolve("audit").resolve("security-audit.jsonl");
        List<JsonObject> events = Files.readAllLines(auditFile, StandardCharsets.UTF_8).stream()
                .filter(line -> !line.isBlank())
                .map(JsonParser::parseString)
                .map(element -> element.getAsJsonObject())
                .toList();
        long auditedDefinitions = definitions.stream()
                .filter(definition -> definition.auditClass() != AuditService.AuditClass.NONE)
                .count();
        assertEquals(auditedDefinitions, events.size());
        Set<String> observedActions = new LinkedHashSet<>();
        for (JsonObject event : events) {
            String actionId = event.get("actionId").getAsString();
            if (callbackActions.contains(actionId)) {
                assertEquals("failed", event.get("result").getAsString());
                assertEquals("provider_error", event.get("reasonCode").getAsString());
            } else {
                assertEquals("rejected", event.get("result").getAsString());
            }
            assertEquals(actorId.toString(), event.get("actorUuid").getAsString());
            assertEquals("tester", event.get("actorUsername").getAsString());
            assertEquals("player", event.get("sourceType").getAsString());
            assertEquals("command", event.get("origin").getAsString());
            assertTrue(event.getAsJsonObject("normalizedParameters").entrySet().isEmpty());
            assertTrue(event.getAsJsonArray("targetUuids").isEmpty());
            assertFalse(event.toString().contains("callback-secret"));
            observedActions.add(actionId);
        }
        assertEquals(
                definitions.stream()
                        .filter(definition -> definition.auditClass() != AuditService.AuditClass.NONE)
                        .map(CommandDefinition::id)
                        .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)),
                observedActions);
    }

    @Test
    void failedExecutionAuditsProviderFailureAndDoesNotLeakCallbackDetails() throws Exception {
        KernelServices.initialize();
        UUID actorId = UUID.fromString("00000000-0000-0000-0000-000000000013");
        ServerPlayer player = mock(ServerPlayer.class);
        when(player.getUUID()).thenReturn(actorId);
        ServerLevel level = mock(ServerLevel.class);
        when(level.dimension()).thenReturn(net.minecraft.world.level.Level.OVERWORLD);
        when(player.level()).thenReturn(level);
        CommandSource output = mock(CommandSource.class);
        when(output.acceptsFailure()).thenReturn(true);
        CommandSourceStack source = new CommandSourceStack(
                output,
                Vec3.ZERO,
                Vec2.ZERO,
                level,
                4,
                "tester",
                Component.literal("tester"),
                mock(MinecraftServer.class),
                player);

        SecurityAuditService.start(temporaryDirectory, 7, 1);
        try (MockedStatic<PermissionAPI> permissions = grantingPermissionApi();
                MockedStatic<MinecraftServerControlRuntime> control = mockStatic(MinecraftServerControlRuntime.class)) {
            control.when(() -> MinecraftServerControlRuntime.authorizeAction(
                            org.mockito.ArgumentMatchers.eq(source),
                            org.mockito.ArgumentMatchers.any(CommandDefinition.class)))
                    .thenReturn(ActionResult.success(null));
            assertEquals(
                    0,
                    KernelCommandExecutor.execute(
                            source,
                            "sef:core.test",
                            Map.of("mode", "safe"),
                            () -> 0));
        } finally {
            SecurityAuditService.shutdown();
        }

        Path auditFile = temporaryDirectory.resolve("audit").resolve("security-audit.jsonl");
        JsonObject event = JsonParser.parseString(Files.readAllLines(auditFile, StandardCharsets.UTF_8).getFirst())
                .getAsJsonObject();
        assertEquals("sef:core.test", event.get("actionId").getAsString());
        assertEquals("failed", event.get("result").getAsString());
        assertEquals("provider_error", event.get("reasonCode").getAsString());
        assertEquals("safe", event.getAsJsonObject("normalizedParameters").get("mode").getAsString());
        assertFalse(event.toString().contains("callback"));
        assertFalse(event.toString().contains("stack"));
    }

    @Test
    void unavailableMandatoryAuditBlocksBeforeInvokingTheAction() throws Exception {
        KernelServices.initialize();
        UUID actorId = UUID.fromString("00000000-0000-0000-0000-000000000014");
        ServerPlayer player = mock(ServerPlayer.class);
        when(player.getUUID()).thenReturn(actorId);
        ServerLevel level = mock(ServerLevel.class);
        when(level.dimension()).thenReturn(net.minecraft.world.level.Level.OVERWORLD);
        when(player.level()).thenReturn(level);
        CommandSource output = mock(CommandSource.class);
        when(output.acceptsFailure()).thenReturn(true);
        List<String> feedback = new ArrayList<>();
        doAnswer(invocation -> {
            feedback.add(invocation.getArgument(0, Component.class).getString());
            return null;
        }).when(output).sendSystemMessage(org.mockito.ArgumentMatchers.any());
        CommandSourceStack source = new CommandSourceStack(
                output,
                Vec3.ZERO,
                Vec2.ZERO,
                level,
                4,
                "tester",
                Component.literal("tester"),
                mock(MinecraftServer.class),
                player);
        AtomicInteger invocations = new AtomicInteger();

        SecurityAuditService.shutdown();
        try (MockedStatic<PermissionAPI> permissions = grantingPermissionApi();
                MockedStatic<MinecraftServerControlRuntime> control = mockStatic(MinecraftServerControlRuntime.class)) {
            control.when(() -> MinecraftServerControlRuntime.authorizeAction(
                            org.mockito.ArgumentMatchers.eq(source),
                            org.mockito.ArgumentMatchers.any(CommandDefinition.class)))
                    .thenReturn(ActionResult.success(null));
            assertEquals(
                    0,
                    KernelCommandExecutor.execute(
                            source,
                            "sef:core.test",
                            Map.of("mode", "safe"),
                            invocations::incrementAndGet));
        } finally {
            SecurityAuditService.shutdown();
        }

        assertEquals(0, invocations.get());
        assertEquals(1, feedback.size());
        assertTrue(feedback.getFirst().contains("audit"));
        assertFalse(AuditService.accepting(AuditService.AuditClass.ADMIN_ACTION));
    }

    @Test
    void callbackExceptionProducesSafeFailureAuditAndFeedback() throws Exception {
        KernelServices.initialize();
        UUID actorId = UUID.fromString("00000000-0000-0000-0000-000000000015");
        ServerPlayer player = mock(ServerPlayer.class);
        when(player.getUUID()).thenReturn(actorId);
        ServerLevel level = mock(ServerLevel.class);
        when(level.dimension()).thenReturn(net.minecraft.world.level.Level.OVERWORLD);
        when(player.level()).thenReturn(level);
        CommandSource output = mock(CommandSource.class);
        when(output.acceptsFailure()).thenReturn(true);
        List<String> feedback = new ArrayList<>();
        doAnswer(invocation -> {
            feedback.add(invocation.getArgument(0, Component.class).getString());
            return null;
        }).when(output).sendSystemMessage(org.mockito.ArgumentMatchers.any());
        CommandSourceStack source = new CommandSourceStack(
                output,
                Vec3.ZERO,
                Vec2.ZERO,
                level,
                4,
                "tester",
                Component.literal("tester"),
                mock(MinecraftServer.class),
                player);

        SecurityAuditService.start(temporaryDirectory, 7, 1);
        try (MockedStatic<PermissionAPI> permissions = grantingPermissionApi();
                MockedStatic<MinecraftServerControlRuntime> control = mockStatic(MinecraftServerControlRuntime.class)) {
            control.when(() -> MinecraftServerControlRuntime.authorizeAction(
                            org.mockito.ArgumentMatchers.eq(source),
                            org.mockito.ArgumentMatchers.any(CommandDefinition.class)))
                    .thenReturn(ActionResult.success(null));
            assertEquals(
                    0,
                    KernelCommandExecutor.execute(
                            source,
                            "sef:core.test",
                            Map.of("mode", "safe"),
                            () -> {
                                throw new IllegalStateException("secret callback detail");
                            }));
        } finally {
            SecurityAuditService.shutdown();
        }

        Path auditFile = temporaryDirectory.resolve("audit").resolve("security-audit.jsonl");
        JsonObject event = JsonParser.parseString(Files.readAllLines(auditFile, StandardCharsets.UTF_8).getFirst())
                .getAsJsonObject();
        assertEquals("failed", event.get("result").getAsString());
        assertEquals("provider_error", event.get("reasonCode").getAsString());
        assertFalse(event.toString().contains("secret callback detail"));
        assertEquals(1, feedback.size());
        assertEquals("That action could not be completed safely.", feedback.getFirst());
    }

    @Test
    void invalidConfiguredCostAuditsRejectedCommandWithoutLeakingCostDetail() throws Exception {
        KernelServices.initialize();
        String previousCosts = ConfigHandler.config.economyCommandCosts.get();
        ConfigHandler.config.economyCommandCosts.set("sef:core.test@item=1.00");
        KernelServices.reloadConfiguration();

        UUID actorId = UUID.fromString("00000000-0000-0000-0000-000000000017");
        ServerPlayer player = mock(ServerPlayer.class);
        when(player.getUUID()).thenReturn(actorId);
        ServerLevel level = mock(ServerLevel.class);
        when(level.dimension()).thenReturn(net.minecraft.world.level.Level.OVERWORLD);
        when(player.level()).thenReturn(level);
        CommandSource output = mock(CommandSource.class);
        when(output.acceptsFailure()).thenReturn(true);
        List<String> feedback = new ArrayList<>();
        doAnswer(invocation -> {
            feedback.add(invocation.getArgument(0, Component.class).getString());
            return null;
        }).when(output).sendSystemMessage(org.mockito.ArgumentMatchers.any());
        CommandSourceStack source = new CommandSourceStack(
                output,
                Vec3.ZERO,
                Vec2.ZERO,
                level,
                4,
                "tester",
                Component.literal("tester"),
                mock(MinecraftServer.class),
                player);

        SecurityAuditService.start(temporaryDirectory, 7, 1);
        try (MockedStatic<PermissionAPI> permissions = grantingPermissionApi();
                MockedStatic<MinecraftServerControlRuntime> control = mockStatic(MinecraftServerControlRuntime.class)) {
            control.when(() -> MinecraftServerControlRuntime.authorizeAction(
                            org.mockito.ArgumentMatchers.eq(source),
                            org.mockito.ArgumentMatchers.any(CommandDefinition.class)))
                    .thenReturn(ActionResult.success(null));
            assertEquals(
                    0,
                    KernelCommandExecutor.execute(
                            source,
                            "sef:core.test",
                            Map.of("count", "not-a-number"),
                            () -> 1));
        } finally {
            SecurityAuditService.shutdown();
            ConfigHandler.config.economyCommandCosts.set(previousCosts);
            KernelServices.reloadConfiguration();
        }

        Path auditFile = temporaryDirectory.resolve("audit").resolve("security-audit.jsonl");
        List<JsonObject> events = Files.readAllLines(auditFile, StandardCharsets.UTF_8).stream()
                .filter(line -> !line.isBlank())
                .map(JsonParser::parseString)
                .map(element -> element.getAsJsonObject())
                .toList();
        assertEquals(1, events.size());
        JsonObject event = events.getFirst();
        assertEquals("sef:core.test", event.get("actionId").getAsString());
        assertEquals("rejected", event.get("result").getAsString());
        assertEquals("invalid_input", event.get("reasonCode").getAsString());
        assertEquals(actorId.toString(), event.get("actorUuid").getAsString());
        assertEquals("player", event.get("sourceType").getAsString());
        assertTrue(event.getAsJsonObject("normalizedParameters").entrySet().isEmpty());
        assertFalse(event.toString().contains("not-a-number"));
        assertEquals(1, feedback.size());
        assertEquals("The configured command cost could not be calculated.", feedback.getFirst());
    }

    @Test
    void delegatedScopeMismatchAuditsRejectedActionWithoutInvokingCallback() throws Exception {
        KernelServices.initialize();
        UUID actorId = UUID.fromString("00000000-0000-0000-0000-000000000018");
        ServerPlayer player = mock(ServerPlayer.class);
        when(player.getUUID()).thenReturn(actorId);
        ServerLevel level = mock(ServerLevel.class);
        when(level.dimension()).thenReturn(net.minecraft.world.level.Level.OVERWORLD);
        when(player.level()).thenReturn(level);
        CommandSource output = mock(CommandSource.class);
        when(output.acceptsFailure()).thenReturn(true);
        List<String> feedback = new ArrayList<>();
        doAnswer(invocation -> {
            feedback.add(invocation.getArgument(0, Component.class).getString());
            return null;
        }).when(output).sendSystemMessage(org.mockito.ArgumentMatchers.any());
        CommandSourceStack source = new CommandSourceStack(
                output,
                Vec3.ZERO,
                Vec2.ZERO,
                level,
                4,
                "tester",
                Component.literal("tester"),
                mock(MinecraftServer.class),
                player);
        AtomicInteger invocations = new AtomicInteger();

        SecurityAuditService.start(temporaryDirectory, 7, 1);
        try {
            DelegatedPermissionScope.preview(
                    actorId,
                    "core",
                    "sef:other.action",
                    Set.of(),
                    () -> KernelCommandExecutor.execute(
                            source,
                            "sef:core.test",
                            Map.of("secret", "delegated-secret"),
                            invocations::incrementAndGet));
        } finally {
            SecurityAuditService.shutdown();
        }

        Path auditFile = temporaryDirectory.resolve("audit").resolve("security-audit.jsonl");
        List<JsonObject> events = Files.readAllLines(auditFile, StandardCharsets.UTF_8).stream()
                .filter(line -> !line.isBlank())
                .map(JsonParser::parseString)
                .map(element -> element.getAsJsonObject())
                .toList();
        assertEquals(0, invocations.get());
        assertEquals(1, feedback.size());
        assertEquals("The delegated execution grant does not cover this action.", feedback.getFirst());
        assertEquals(1, events.size());
        JsonObject event = events.getFirst();
        assertEquals("sef:core.test", event.get("actionId").getAsString());
        assertEquals("rejected", event.get("result").getAsString());
        assertEquals("policy_denied", event.get("reasonCode").getAsString());
        assertEquals(actorId.toString(), event.get("actorUuid").getAsString());
        assertEquals("player", event.get("sourceType").getAsString());
        assertTrue(event.getAsJsonObject("normalizedParameters").entrySet().isEmpty());
        assertFalse(event.toString().contains("delegated-secret"));
    }

    private static MockedStatic<PermissionAPI> denyingPermissionApi() {
        return mockStatic(PermissionAPI.class, invocation -> {
            String method = invocation.getMethod().getName();
            if (method.equals("getPermission") || method.equals("getOfflinePermission")) {
                return false;
            }
            if (method.equals("getActivePermissionHandler")) {
                return "sef:test-deny";
            }
            return Answers.RETURNS_DEFAULTS.answer(invocation);
        });
    }

    private static MockedStatic<PermissionAPI> grantingPermissionApi() {
        return mockStatic(PermissionAPI.class, invocation -> {
            String method = invocation.getMethod().getName();
            if (method.equals("getPermission") || method.equals("getOfflinePermission")) {
                return true;
            }
            if (method.equals("getActivePermissionHandler")) {
                return "sef:test-grant";
            }
            return Answers.RETURNS_DEFAULTS.answer(invocation);
        });
    }
}
