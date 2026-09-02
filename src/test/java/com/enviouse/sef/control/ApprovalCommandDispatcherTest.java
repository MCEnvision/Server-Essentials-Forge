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

final class ApprovalCommandDispatcherTest {
    @Test
    void everyApprovalRouteIsRegistered() {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        ApprovalCommands.register(dispatcher);
        var root = dispatcher.getRoot().getChild("approval");

        assertNotNull(root);
        for (String child : List.of("request", "approve", "revoke", "inspect", "list", "history")) {
            assertNotNull(root.getChild(child), child);
        }
        assertNotNull(root.getChild("request").getChild("accessgrant"));
        assertNotNull(root.getChild("request").getChild("generic"));
        assertNotNull(dispatcher.getRoot().getChild("approvals"));
    }

    @Test
    void permissionsProjectOnlyAuthorizedBranches() {
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(source.getEntity()).thenReturn(player);
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        ApprovalCommands.register(dispatcher);

        try (MockedStatic<PermissionAPI> permissions = permissionApi()) {
            permissions.when(() -> PermissionAPI.getPermission(
                            player,
                            PermissionsHandler.phasePermission("commands.approval.approve")))
                    .thenReturn(true);

            var root = dispatcher.getRoot().getChild("approval");
            assertTrue(root.canUse(source));
            assertTrue(root.getChild("approve").canUse(source));
            assertFalse(root.getChild("request").canUse(source));
            assertFalse(root.getChild("revoke").canUse(source));
            assertFalse(root.getChild("history").canUse(source));
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
