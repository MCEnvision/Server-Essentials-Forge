package com.enviouse.sef.config.modules;

import com.enviouse.sef.config.PermissionsHandler;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
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

class ModuleConfigCommandDispatcherTest {
    @Test
    void configurationMutationsAreHiddenWithoutTheirSpecificPermission() {
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(source.getEntity()).thenReturn(player);
        CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();

        try (MockedStatic<PermissionAPI> permissions = permissionApi()) {
            permissions.when(() -> PermissionAPI.getPermission(
                    player, PermissionsHandler.phasePermission("commands.config.status"))).thenReturn(true);

            var config = dispatcher.getRoot().getChild("sef").getChild("config");
            assertTrue(config.canUse(source));
            assertTrue(config.getChild("status").canUse(source));
            assertFalse(config.getChild("reload").canUse(source));
            assertFalse(config.getChild("rollback").canUse(source));
            assertFalse(config.getChild("set").canUse(source));
        }
    }

    @Test
    void typedSettingRouteParsesExpectedRevisionAndGreedyValue() {
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(source.getEntity()).thenReturn(player);
        CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();

        try (MockedStatic<PermissionAPI> permissions = permissionApi()) {
            permissions.when(() -> PermissionAPI.getPermission(
                    player, PermissionsHandler.phasePermission("commands.config.edit"))).thenReturn(true);
            var parsed = dispatcher.parse(
                    "sef config set messages format.prefix 4 a bounded prefix",
                    source);

            assertTrue(parsed.getExceptions().isEmpty());
            assertFalse(parsed.getReader().canRead());
        }
    }

    @Test
    void guiPolicyAndPersonalRoutesAreSeparate() {
        CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();

        assertNotNull(dispatcher.getRoot().getChild("sef").getChild("guis"));
        assertNotNull(dispatcher.getRoot().getChild("sef").getChild("gui"));
        assertNotNull(dispatcher.getRoot().getChild("sef").getChild("guis").getChild("module"));
        assertNotNull(dispatcher.getRoot().getChild("sef").getChild("gui").getChild("reset"));
    }

    private static CommandDispatcher<CommandSourceStack> dispatcher() {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        var root = Commands.<CommandSourceStack>literal("sef");
        ModuleConfigCommands.attach(root);
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
