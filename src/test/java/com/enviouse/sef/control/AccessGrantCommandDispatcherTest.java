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

class AccessGrantCommandDispatcherTest {
    @Test
    void everyWorkflowRouteIsRegistered() {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        AccessGrantCommands.register(dispatcher);
        var root = dispatcher.getRoot().getChild("accessgrant");

        assertNotNull(root);
        for (String child : List.of(
                "profiles",
                "profile",
                "preview",
                "create",
                "renew",
                "suspend",
                "resume",
                "revoke",
                "list",
                "inspect",
                "expiring",
                "reconcile",
                "history")) {
            assertNotNull(root.getChild(child), child);
        }
        assertNotNull(root.getChild("profile").getChild("inspect"));
        assertNotNull(root.getChild("profile").getChild("publish"));
        assertNotNull(root.getChild("profile").getChild("retire"));
    }

    @Test
    void actionPermissionsProjectOnlyAuthorizedBranches() {
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(source.getEntity()).thenReturn(player);
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        AccessGrantCommands.register(dispatcher);

        try (MockedStatic<PermissionAPI> permissions = permissionApi()) {
            permissions.when(() -> PermissionAPI.getPermission(
                            player,
                            PermissionsHandler.phasePermission("commands.accessgrant.list")))
                    .thenReturn(true);

            var root = dispatcher.getRoot().getChild("accessgrant");
            assertTrue(root.canUse(source));
            assertTrue(root.getChild("list").canUse(source));
            assertFalse(root.getChild("create").canUse(source));
            assertFalse(root.getChild("reconcile").canUse(source));
            assertFalse(root.getChild("profiles").canUse(source));
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
