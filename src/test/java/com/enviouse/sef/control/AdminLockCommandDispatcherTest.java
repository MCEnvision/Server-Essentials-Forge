package com.enviouse.sef.control;

import com.enviouse.sef.config.PermissionsHandler;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class AdminLockCommandDispatcherTest {
    @Test
    void everyPlannedRouteIsRegistered() {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        AdminLockCommands.register(dispatcher);
        var root = dispatcher.getRoot().getChild("adminlock");

        assertNotNull(root);
        for (String child : List.of(
                "status",
                "lock",
                "unlock",
                "challenge",
                "session",
                "require",
                "release",
                "invalidate",
                "breakglass",
                "history")) {
            assertNotNull(root.getChild(child), child);
        }
        assertNotNull(root.getChild("session").getChild("open"));
        assertNotNull(root.getChild("session").getChild("close"));
        assertNotNull(root.getChild("breakglass").getChild("status"));
        assertNotNull(root.getChild("breakglass").getChild("open"));
        assertNotNull(root.getChild("breakglass").getChild("close"));
    }

    @Test
    void selfLockPermissionDoesNotExposeOwnerRecoveryActions() {
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(source.getEntity()).thenReturn(player);
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        AdminLockCommands.register(dispatcher);

        try (MockedStatic<PermissionAPI> permissions = permissionApi()) {
            permissions.when(() -> PermissionAPI.getPermission(
                            player,
                            PermissionsHandler.phasePermission("commands.adminlock.lock")))
                    .thenReturn(true);

            var root = dispatcher.getRoot().getChild("adminlock");
            assertTrue(root.canUse(source));
            assertTrue(root.getChild("lock").canUse(source));
            assertFalse(root.getChild("release").canUse(source));
            assertFalse(root.getChild("invalidate").canUse(source));
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
