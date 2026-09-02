package com.enviouse.sef.teleport;

import com.enviouse.sef.config.PermissionsHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class TeleportCommandDispatcherTest {
    @Test
    void playerHomePermissionDoesNotExposeMutationOrAdministrationRoots() {
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(source.getEntity()).thenReturn(player);
        try (MockedStatic<PermissionAPI> permissions = permissionApi()) {
            permissions.when(() -> PermissionAPI.getPermission(player, PermissionsHandler.homeCommand))
                    .thenReturn(true);
            CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();

            assertTrue(dispatcher.getRoot().getChild("home").canUse(source));
            assertFalse(dispatcher.getRoot().getChild("sethome").canUse(source));
            assertFalse(dispatcher.getRoot().getChild("homeadmin").canUse(source));
        }
    }

    @Test
    void playerWarpManagementBranchesRequireTheirSeparatePermissions() {
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(source.getEntity()).thenReturn(player);
        try (MockedStatic<PermissionAPI> permissions = permissionApi()) {
            permissions.when(() -> PermissionAPI.getPermission(player, PermissionsHandler.playerWarpCommand))
                    .thenReturn(true);
            CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();
            var root = dispatcher.getRoot().getChild("pwarp");

            assertTrue(root.canUse(source));
            assertFalse(root.getChild("publish").canUse(source));
            assertFalse(root.getChild("moderate").canUse(source));
        }
    }

    @Test
    void playerWarpInspectionBranchesDoNotBypassManagementPermission() {
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(source.getEntity()).thenReturn(player);
        try (MockedStatic<PermissionAPI> permissions = permissionApi()) {
            permissions.when(() -> PermissionAPI.getPermission(player, PermissionsHandler.playerWarpCommand))
                    .thenReturn(true);
            CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();
            var root = dispatcher.getRoot().getChild("pwarp");

            assertFalse(root.getChild("info").canUse(source));
            assertFalse(root.getChild("visits").canUse(source));

            permissions.when(() -> PermissionAPI.getPermission(player, PermissionsHandler.playerWarpEdit))
                    .thenReturn(true);
            assertTrue(root.getChild("info").canUse(source));
            assertTrue(root.getChild("visits").canUse(source));
        }
    }

    @Test
    void vanillaTeleportRootIsNotOwnedByDefault() {
        CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();
        assertNull(dispatcher.getRoot().getChild("tp"));
    }

    @Test
    void teleportRequestsAcceptQuotedNicknameTargets() {
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(source.getEntity()).thenReturn(player);
        try (MockedStatic<PermissionAPI> permissions = permissionApi()) {
            permissions.when(() -> PermissionAPI.getPermission(
                    player, PermissionsHandler.tpaCommand)).thenReturn(true);
            CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();

            ParseResults<CommandSourceStack> parsed =
                    dispatcher.parse("tpa \"staff member\"", source);

            assertTrue(parsed.getExceptions().isEmpty());
            assertFalse(parsed.getReader().canRead());
        }
    }

    @Test
    void teleportRequestAndRandomRootsRemainIndependentlyPermissionGated() {
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(source.getEntity()).thenReturn(player);
        try (MockedStatic<PermissionAPI> permissions = permissionApi()) {
            permissions.when(() -> PermissionAPI.getPermission(player, PermissionsHandler.tpaCommand))
                    .thenReturn(true);
            CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();

            assertTrue(dispatcher.getRoot().getChild("tpa").canUse(source));
            assertFalse(dispatcher.getRoot().getChild("tpahere").canUse(source));
            assertFalse(dispatcher.getRoot().getChild("tpaccept").canUse(source));
            assertFalse(dispatcher.getRoot().getChild("tpdeny").canUse(source));
            assertFalse(dispatcher.getRoot().getChild("tpcancel").canUse(source));
            assertFalse(dispatcher.getRoot().getChild("rtp").canUse(source));
        }
    }

    private static CommandDispatcher<CommandSourceStack> dispatcher() {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        HomeCommands.register(dispatcher);
        TeleportRequestCommands.register(dispatcher);
        CoreTeleportCommands.register(dispatcher);
        WarpCommands.register(dispatcher);
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
