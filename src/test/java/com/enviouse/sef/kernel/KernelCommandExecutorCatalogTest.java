package com.enviouse.sef.kernel;

import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.audit.SecurityAuditService;
import com.enviouse.sef.kernel.command.CommandDefinition;
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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
