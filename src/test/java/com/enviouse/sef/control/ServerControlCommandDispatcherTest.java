package com.enviouse.sef.control;

import com.enviouse.sef.config.PermissionsHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class ServerControlCommandDispatcherTest {
    @Test
    void deniedFeatureChildrenRemainHidden() {
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(source.getEntity()).thenReturn(player);
        var root = Commands.<CommandSourceStack>literal("sef");
        ServerControlCommands.attach(root);
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        dispatcher.register(root);

        try (MockedStatic<PermissionAPI> permissions = permissionApi()) {
            permissions.when(() -> PermissionAPI.getPermission(
                    player, PermissionsHandler.phasePermission("commands.control"))).thenReturn(true);
            var control = dispatcher.getRoot().getChild("sef").getChild("control");
            assertTrue(control.canUse(source));
            assertFalse(control.getChild("backups").canUse(source));
            assertFalse(control.getChild("reports").canUse(source));
        }
    }

    @Test
    void allowedFeatureParsesTypedCreateAndRevisionRoutes() {
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(source.getEntity()).thenReturn(player);
        var root = Commands.<CommandSourceStack>literal("sef");
        ServerControlCommands.attach(root);
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        dispatcher.register(root);

        try (MockedStatic<PermissionAPI> permissions = permissionApi()) {
            permissions.when(() -> PermissionAPI.getPermission(
                    player, PermissionsHandler.phasePermission("commands.control"))).thenReturn(true);
            permissions.when(() -> PermissionAPI.getPermission(
                    player, PermissionsHandler.phasePermission("commands.control.reports.create"))).thenReturn(true);
            permissions.when(() -> PermissionAPI.getPermission(
                    player, PermissionsHandler.phasePermission("commands.control.reports.manage"))).thenReturn(true);

            ParseResults<CommandSourceStack> create =
                    dispatcher.parse("sef control reports create \"chat report\" details", source);
            ParseResults<CommandSourceStack> state =
                    dispatcher.parse("sef control reports state 00000000-0000-0000-0000-000000000001 active 4 note", source);

            assertTrue(create.getExceptions().isEmpty());
            assertFalse(create.getReader().canRead());
            assertTrue(state.getExceptions().isEmpty());
            assertFalse(state.getReader().canRead());
        }
    }

    @Test
    void directRootsAreCollisionAware() {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        dispatcher.register(Commands.literal("rules"));

        ServerControlCommands.registerDirect(dispatcher);

        assertNotNull(dispatcher.getRoot().getChild("rules"));
        assertNull(dispatcher.getRoot().getChild("rules").getChild("create"));
        assertNotNull(dispatcher.getRoot().getChild("reports"));
        assertNotNull(dispatcher.getRoot().getChild("reports").getChild("create"));
    }

    @Test
    void communityRoutesOwnPlayerWorkflowRootsBeforeGenericControls() {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        CommunityCommands.register(dispatcher);
        ServerControlCommands.registerDirect(dispatcher);

        assertNotNull(dispatcher.getRoot().getChild("rules").getChild("accept"));
        assertNotNull(dispatcher.getRoot().getChild("daily").getChild("claim"));
        assertNotNull(dispatcher.getRoot().getChild("friend").getChild("accept"));
        assertNotNull(dispatcher.getRoot().getChild("waypoint").getChild("go"));
        assertNotNull(dispatcher.getRoot().getChild("poll").getChild("vote"));
        assertNotNull(dispatcher.getRoot().getChild("invite").getChild("redeem"));
        assertNotNull(dispatcher.getRoot().getChild("mentions").getChild("mode"));
        assertNotNull(dispatcher.getRoot().getChild("onboarding").getChild("step"));
        assertNotNull(dispatcher.getRoot().getChild("sleepvote").getChild("yes"));
        assertNotNull(dispatcher.getRoot().getChild("deathlocation").getChild("clear"));
        assertNotNull(dispatcher.getRoot().getChild("appeal"));
        assertNotNull(dispatcher.getRoot().getChild("accessapply"));
        assertNotNull(dispatcher.getRoot().getChild("privacy").getChild("request"));
        assertNotNull(dispatcher.getRoot().getChild("calendar").getChild("subscribe"));
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
