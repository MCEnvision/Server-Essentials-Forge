package com.enviouse.sef.vanish;

import com.enviouse.sef.config.PermissionsHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class VanishCommandDispatcherTest {
    @Test
    void queuePermissionWithoutOthersCannotParseOnlineTargetPath() {
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer issuer = mock(ServerPlayer.class);
        when(source.getEntity()).thenReturn(issuer);
        try {
            when(source.getPlayerOrException()).thenReturn(issuer);
        } catch (CommandSyntaxException exception) {
            throw new AssertionError(exception);
        }

        try (MockedStatic<PermissionAPI> permissions = permissionApi()) {
            permissions.when(() -> PermissionAPI.getPermission(
                            issuer,
                            PermissionsHandler.vanishQueueCommand))
                    .thenReturn(true);
            permissions.when(() -> PermissionAPI.getPermission(
                            issuer,
                            PermissionsHandler.vanishOthersCommand))
                    .thenReturn(false);

            CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
            VanishCommand.register(dispatcher);

            assertTrue(dispatcher.getRoot().getChild("v").canUse(source));
            assertTrue(dispatcher.getRoot().getChild("v").getChild("queue").canUse(source));
            assertFalse(dispatcher.getRoot()
                    .getChild("v")
                    .getChild("queue")
                    .getChild("player")
                    .canUse(source));
            assertThrows(
                    CommandSyntaxException.class,
                    () -> dispatcher.execute("v queue target", source));
        }
    }

    @Test
    void queueTargetPathRequiresBothPermissionsForEveryAlias() {
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer issuer = mock(ServerPlayer.class);
        when(source.getEntity()).thenReturn(issuer);
        try {
            when(source.getPlayerOrException()).thenReturn(issuer);
        } catch (CommandSyntaxException exception) {
            throw new AssertionError(exception);
        }

        try (MockedStatic<PermissionAPI> permissions = permissionApi()) {
            permissions.when(() -> PermissionAPI.getPermission(
                            issuer,
                            PermissionsHandler.vanishQueueCommand))
                    .thenReturn(true);
            permissions.when(() -> PermissionAPI.getPermission(
                            issuer,
                            PermissionsHandler.vanishOthersCommand))
                    .thenReturn(true);

            CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
            VanishCommand.register(dispatcher);

            assertTrue(dispatcher.getRoot()
                    .getChild("v")
                    .getChild("queue")
                    .getChild("player")
                    .canUse(source));
            assertTrue(dispatcher.getRoot()
                    .getChild("vanish")
                    .getChild("queue")
                    .getChild("player")
                    .canUse(source));
        }
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
