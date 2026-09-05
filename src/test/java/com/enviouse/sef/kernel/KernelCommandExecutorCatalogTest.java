package com.enviouse.sef.kernel;

import com.enviouse.sef.audit.SecurityAuditService;
import com.enviouse.sef.kernel.command.CommandDefinition;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;
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
    void everyCatalogActionRefusesExecutionBeforeInvokingTheActionWhenPermissionProviderDenies() {
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
        AtomicInteger invocations = new AtomicInteger();
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
