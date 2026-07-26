package com.enviouse.sef.social;

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

class SocialCommandDispatcherTest {
    @Test
    void socialSpyRootDoesNotExposeSensitiveChildren() {
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(source.getEntity()).thenReturn(player);

        try (MockedStatic<PermissionAPI> permissions = permissionApi()) {
            permissions.when(() -> PermissionAPI.getPermission(
                    player, PermissionsHandler.socialSpyCommand)).thenReturn(true);

            CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
            SocialCommands.register(dispatcher);
            var root = dispatcher.getRoot().getChild("socialspy");

            assertTrue(root.canUse(source));
            assertFalse(root.getChild("status").canUse(source));
            assertFalse(root.getChild("recent").canUse(source));
            assertFalse(root.getChild("everyone").canUse(source));
            assertFalse(root.getChild("selected").canUse(source));
            assertFalse(root.getChild("filter").canUse(source));
            assertFalse(root.getChild("format").getChild("preview").canUse(source));
        }
    }

    @Test
    void phaseFiveCommandFamiliesRegisterWithFeatureDefaults() {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();

        SocialCommands.register(dispatcher);
        MailCommands.register(dispatcher);
        ConnectionCommands.register(dispatcher);
        ReminderCommands.register(dispatcher);
        CustomTextCommands.register(dispatcher);
        IdentityCommands.register(dispatcher);

        for (String root : new String[]{
                "msgtoggle", "rtoggle", "ignore", "ignorelist", "socialspy",
                "mail", "joinmessage", "leavemessage", "connectionmessage",
                "reminders", "reminder", "welcome", "customtext", "booktext",
                "rules", "info", "sef"}) {
            assertNotNull(dispatcher.getRoot().getChild(root), root);
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
