package com.enviouse.sef.economy;

import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.KernelServices;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class EconomyCommandDispatcherTest {
    @Test
    void aliasesAndAdministrativeBranchesRemainPermissionGated() {
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(source.getEntity()).thenReturn(player);
        when(source.getPlayer()).thenReturn(player);

        try (MockedStatic<PermissionAPI> permissions = permissionApi()) {
            CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();
            assertFalse(dispatcher.getRoot().getChild("balance").canUse(source));
            assertFalse(dispatcher.getRoot().getChild("bal").canUse(source));
            assertFalse(dispatcher.getRoot().getChild("money").canUse(source));
            assertFalse(dispatcher.getRoot().getChild("eco").canUse(source));

            permissions.when(() -> PermissionAPI.getPermission(
                            player,
                            PermissionsHandler.phasePermission("commands.balance")))
                    .thenReturn(true);
            assertTrue(dispatcher.getRoot().getChild("balance").canUse(source));
            assertTrue(dispatcher.getRoot().getChild("bal").canUse(source));
            assertTrue(dispatcher.getRoot().getChild("money").canUse(source));

            permissions.when(() -> PermissionAPI.getPermission(
                            player,
                            PermissionsHandler.phasePermission("commands.eco.reset")))
                    .thenReturn(true);
            var eco = dispatcher.getRoot().getChild("eco");
            assertTrue(eco.canUse(source));
            assertTrue(eco.getChild("reset").canUse(source));
            var playerArgument = eco.getChild("reset").getChild("player");
            assertNotNull(playerArgument);
            assertNotNull(playerArgument.getChild("confirm"));
        }
    }

    @Test
    void paymentAndAdjustmentConfirmationLiteralsAreInTheRealTree() {
        CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();

        var payPlayer = dispatcher.getRoot().getChild("pay").getChild("player");
        assertNotNull(payPlayer.getChild("amount").getChild("confirm"));

        var eco = dispatcher.getRoot().getChild("eco");
        for (String operation : Set.of("give", "take", "set")) {
            assertNotNull(eco.getChild(operation)
                    .getChild("player")
                    .getChild("amount")
                    .getChild("confirm"));
        }
    }

    private static CommandDispatcher<CommandSourceStack> dispatcher() {
        KernelServices.initialize();
        KernelServices.shortcuts().captureExistingRoots(Set.of());
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        EconomyCommands.register(dispatcher);
        return dispatcher;
    }

    private static MockedStatic<PermissionAPI> permissionApi() {
        return mockStatic(PermissionAPI.class, invocation -> {
            if ("getPermission".equals(invocation.getMethod().getName())) {
                return false;
            }
            return Answers.RETURNS_DEFAULTS.answer(invocation);
        });
    }
}
