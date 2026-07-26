package com.enviouse.sef.commands;

import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.storage.StorageCommands;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class BfcCommandDispatcherTest {
    @Test
    void rootPermissionDoesNotExposeOrExecuteAdministrativeChildren() throws Exception {
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(source.getEntity()).thenReturn(player);

        try (MockedStatic<PermissionAPI> permissions = permissionApi()) {
            permissions.when(() -> PermissionAPI.getPermission(
                            player,
                            PermissionsHandler.sefCommand))
                    .thenReturn(true);

            CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();
            List<String> deniedChildren = List.of(
                    "info",
                    "colors",
                    "test",
                    "reload",
                    "filter",
                    "storage");

            assertTrue(dispatcher.getRoot().getChild("sef").canUse(source));
            for (String child : deniedChildren) {
                assertFalse(
                        dispatcher.getRoot().getChild("sef").getChild(child).canUse(source),
                        child);
                assertThrows(
                        CommandSyntaxException.class,
                        () -> dispatcher.execute("sef " + child, source),
                        child);
            }

        }
    }

    @Test
    void childPermissionsProjectOnlyTheirAuthorizedBranches() {
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(source.getEntity()).thenReturn(player);

        try (MockedStatic<PermissionAPI> permissions = permissionApi()) {
            permissions.when(() -> PermissionAPI.getPermission(
                            player,
                            PermissionsHandler.sefCommand))
                    .thenReturn(true);
            permissions.when(() -> PermissionAPI.getPermission(
                            player,
                            PermissionsHandler.sefCommandInfoSubCommand))
                    .thenReturn(true);
            permissions.when(() -> PermissionAPI.getPermission(
                            player,
                            PermissionsHandler.storageStatus))
                    .thenReturn(true);

            CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();
            var root = dispatcher.getRoot().getChild("sef");

            assertTrue(root.getChild("info").canUse(source));
            assertFalse(root.getChild("reload").canUse(source));
            assertTrue(root.getChild("storage").canUse(source));
            assertTrue(root.getChild("storage").getChild("status").canUse(source));
            assertFalse(root.getChild("storage").getChild("export").canUse(source));
        }
    }

    private static CommandDispatcher<CommandSourceStack> dispatcher() {
        LiteralArgumentBuilder<CommandSourceStack> root = BfcCommands.coreRoot();
        BfcCommands.registerFilterCommands(root);
        StorageCommands.attach(root);
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        dispatcher.register(root);
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
