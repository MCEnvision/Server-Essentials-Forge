package com.enviouse.sef.fancytags;

import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.mojang.brigadier.CommandDispatcher;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class FancyTagCommandDispatcherTest {
    @AfterEach
    void restoreConfig() {
        ConfigHandler.config.enableFancyTags.set(false);
    }

    @Test
    void rootPermissionDoesNotExposeAdministrativeBranches() {
        ConfigHandler.config.enableFancyTags.set(true);
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(source.getEntity()).thenReturn(player);
        try (MockedStatic<PermissionAPI> permissions = permissionApi()) {
            allow(permissions, player, "commands.tags");
            CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();
            var root = dispatcher.getRoot().getChild("fancytags");

            assertTrue(root.canUse(source));
            assertFalse(root.getChild("create").canUse(source));
            assertFalse(root.getChild("assign").getChild("player").canUse(source));
            assertFalse(root.getChild("moderation").getChild("queue").canUse(source));
            assertFalse(root.getChild("backup").getChild("create").canUse(source));
            assertFalse(root.getChild("integrity").getChild("repair").canUse(source));
        }
    }

    @Test
    void specializedPermissionsExposeOnlyTheirTypedRoutes() {
        ConfigHandler.config.enableFancyTags.set(true);
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(source.getEntity()).thenReturn(player);
        try (MockedStatic<PermissionAPI> permissions = permissionApi()) {
            allow(permissions, player, "commands.tags");
            allow(permissions, player, "commands.tags.assign.player");
            allow(permissions, player, "commands.tags.import.approve");
            allow(permissions, player, "commands.tags.export.project");
            CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();
            var root = dispatcher.getRoot().getChild("fancytags");

            assertTrue(root.getChild("assign").getChild("player").canUse(source));
            assertFalse(root.getChild("assign").getChild("group").canUse(source));
            assertTrue(root.getChild("import").getChild("approve").canUse(source));
            assertFalse(root.getChild("import").getChild("url").canUse(source));
            assertTrue(root.getChild("export").getChild("project").canUse(source));
            assertFalse(root.getChild("export").getChild("png").canUse(source));
            assertInstanceOf(
                    ArgumentCommandNode.class,
                    root.getChild("assign").getChild("player").getChild("player"));
        }
    }

    private static CommandDispatcher<CommandSourceStack> dispatcher() {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        FancyTagCommands.registerDirect(dispatcher);
        return dispatcher;
    }

    private static void allow(
            MockedStatic<PermissionAPI> permissions,
            ServerPlayer player,
            String permission
    ) {
        permissions.when(() -> PermissionAPI.getPermission(
                        player,
                        PermissionsHandler.phasePermission(permission)))
                .thenReturn(true);
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
