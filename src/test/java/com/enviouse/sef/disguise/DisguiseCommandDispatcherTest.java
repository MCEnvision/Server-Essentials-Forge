package com.enviouse.sef.disguise;

import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class DisguiseCommandDispatcherTest {
    @AfterEach
    void restoreConfig() {
        ConfigHandler.config.enableDisguises.set(false);
    }

    @Test
    void rootPermissionDoesNotExposeSpecializedOrAdministrativeBranches() {
        ConfigHandler.config.enableDisguises.set(true);
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(source.getEntity()).thenReturn(player);
        try (MockedStatic<PermissionAPI> permissions = permissionApi()) {
            permissions.when(() -> PermissionAPI.getPermission(
                            player,
                            PermissionsHandler.phasePermission("commands.disguise")))
                    .thenReturn(true);
            CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();
            var root = dispatcher.getRoot().getChild("disguise");

            assertTrue(root.canUse(source));
            assertFalse(root.getChild("mob").canUse(source));
            assertFalse(root.getChild("player").canUse(source));
            assertFalse(root.getChild("presets").canUse(source));
            assertFalse(root.getChild("set").canUse(source));
            assertFalse(root.getChild("inspect").canUse(source));
            assertFalse(root.getChild("ability").canUse(source));
        }
    }

    @Test
    void offlineProfileArgumentIsTypedAndOnlyAuthorizedBranchesProject() {
        ConfigHandler.config.enableDisguises.set(true);
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(source.getEntity()).thenReturn(player);
        try (MockedStatic<PermissionAPI> permissions = permissionApi()) {
            permissions.when(() -> PermissionAPI.getPermission(
                            player,
                            PermissionsHandler.phasePermission("commands.disguise")))
                    .thenReturn(true);
            permissions.when(() -> PermissionAPI.getPermission(
                            player,
                            PermissionsHandler.phasePermission("commands.disguise.player")))
                    .thenReturn(true);
            permissions.when(() -> PermissionAPI.getPermission(
                            player,
                            PermissionsHandler.phasePermission("commands.disguise.preset.manage")))
                    .thenReturn(true);
            CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();
            var root = dispatcher.getRoot().getChild("disguise");

            assertTrue(root.getChild("player").canUse(source));
            assertTrue(root.getChild("presets").canUse(source));
            assertFalse(root.getChild("mob").canUse(source));
            ArgumentCommandNode<CommandSourceStack, ?> profile =
                    assertInstanceOf(
                            ArgumentCommandNode.class,
                            root.getChild("player").getChild("profile"));
            assertInstanceOf(StringArgumentType.class, profile.getType());
            assertNotNull(profile.getCustomSuggestions());
        }
    }

    private static CommandDispatcher<CommandSourceStack> dispatcher() {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        DisguiseCommands.register(dispatcher);
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
