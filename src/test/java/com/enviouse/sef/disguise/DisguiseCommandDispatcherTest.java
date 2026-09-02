package com.enviouse.sef.disguise;

import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    void rootIsRegisteredBeforeRuntimeConfigurationPublishes() {
        ConfigHandler.config.enableDisguises.set(false);

        CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();

        assertNotNull(dispatcher.getRoot().getChild("disguise"));
        assertNotNull(dispatcher.getRoot().getChild("disguise").getCommand());
        assertNotNull(dispatcher.getRoot().getChild("undisguise"));
        assertNotNull(dispatcher.getRoot().getChild("dability"));
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

    @Test
    void entityTypesStayUnderNamedBranchesAndAcceptNamespaces() {
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
                            PermissionsHandler.phasePermission("commands.disguise.mob")))
                    .thenReturn(true);
            CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();
            var root = dispatcher.getRoot().getChild("disguise");

            assertNull(root.getChild("entity_type"));
            ArgumentCommandNode<CommandSourceStack, ?> mobType = assertInstanceOf(
                    ArgumentCommandNode.class,
                    root.getChild("mob").getChild("entity_type"));
            assertInstanceOf(ResourceLocationArgument.class, mobType.getType());
            assertNotNull(mobType.getCustomSuggestions());
            ArgumentCommandNode<CommandSourceStack, ?> setType = assertInstanceOf(
                    ArgumentCommandNode.class,
                    root.getChild("set").getChild("player").getChild("entity_type"));
            assertInstanceOf(ResourceLocationArgument.class, setType.getType());
            assertNotNull(setType.getCustomSuggestions());

            ParseResults<CommandSourceStack> parsed =
                    dispatcher.parse("disguise mob minecraft:bat", source);
            assertFalse(parsed.getReader().canRead());
            assertNotNull(parsed.getContext().getCommand());
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
