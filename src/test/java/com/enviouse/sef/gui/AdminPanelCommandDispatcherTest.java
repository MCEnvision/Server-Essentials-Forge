package com.enviouse.sef.gui;

import com.enviouse.sef.commands.BfcCommands;
import com.enviouse.sef.config.PermissionsHandler;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class AdminPanelCommandDispatcherTest {
    @Test
    void panelPermissionsDoNotImplyDraftPublicationOrRollback() {
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(source.getEntity()).thenReturn(player);
        when(source.getPlayer()).thenReturn(player);

        try (MockedStatic<PermissionAPI> permissions = permissionApi()) {
            permissions.when(() -> PermissionAPI.getPermission(player, PermissionsHandler.sefCommand))
                    .thenReturn(true);
            permissions.when(() -> PermissionAPI.getPermission(player, PermissionsHandler.panelList))
                    .thenReturn(true);
            permissions.when(() -> PermissionAPI.getPermission(player, PermissionsHandler.panelRun))
                    .thenReturn(true);

            CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();
            var panel = dispatcher.getRoot().getChild("sef").getChild("panel");

            assertTrue(panel.getChild("list").canUse(source));
            assertTrue(panel.getChild("run").canUse(source));
            assertFalse(panel.getChild("inspect").canUse(source));
            assertFalse(panel.getChild("preview").canUse(source));
            assertFalse(panel.getChild("draft").getChild("create").canUse(source));
            assertFalse(panel.getChild("publish").canUse(source));
            assertFalse(panel.getChild("rollback").canUse(source));
        }
    }

    @Test
    void guiPreferencePermissionDoesNotExposeAdministrativePanels() {
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(source.getEntity()).thenReturn(player);
        when(source.getPlayer()).thenReturn(player);

        try (MockedStatic<PermissionAPI> permissions = permissionApi()) {
            permissions.when(() -> PermissionAPI.getPermission(player, PermissionsHandler.sefCommand))
                    .thenReturn(true);
            permissions.when(() -> PermissionAPI.getPermission(player, PermissionsHandler.guiPreferences))
                    .thenReturn(true);

            CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();
            var sef = dispatcher.getRoot().getChild("sef");

            assertTrue(sef.getChild("client").getChild("preference").canUse(source));
            assertNotNull(sef.getChild("client").getChild("preference").getChild("blur"));
            assertFalse(sef.getChild("panel").getChild("list").canUse(source));
            assertFalse(sef.getChild("panel").getChild("publish").canUse(source));
        }
    }

    @Test
    void coreAndFancyTagRootsExistBeforeRuntimeConfigurationPublishes() {
        CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();
        var sef = dispatcher.getRoot().getChild("sef");

        assertNotNull(sef);
        assertNotNull(sef.getCommand());
        assertNotNull(sef.getChild("tags"));
        assertNotNull(sef.getChild("tags").getChild("doctor"));
    }

    private static CommandDispatcher<CommandSourceStack> dispatcher() {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        BfcCommands.register(dispatcher);
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
