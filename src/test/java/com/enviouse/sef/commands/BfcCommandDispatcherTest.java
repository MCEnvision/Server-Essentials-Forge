package com.enviouse.sef.commands;

import com.enviouse.sef.audit.SecurityAuditService;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.motd.MotdCommands;
import com.enviouse.sef.storage.StorageCommands;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Answers;
import org.mockito.MockedStatic;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class BfcCommandDispatcherTest {
    @TempDir
    Path temporaryDirectory;

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
                    "storage",
                    "motd");

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

    @Test
    void dispatcherRevalidatesPermissionInsideSharedExecutionPipeline() throws Exception {
        ServerPlayer player = mock(ServerPlayer.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());
        CommandSourceStack source = playerSource(player);
        AtomicBoolean actionPermission = new AtomicBoolean(true);

        try (MockedStatic<PermissionAPI> permissions = permissionApi()) {
            permissions.when(() -> PermissionAPI.getPermission(
                            player,
                            PermissionsHandler.sefCommand))
                    .thenReturn(true);
            permissions.when(() -> PermissionAPI.getPermission(
                            player,
                            PermissionsHandler.sefCommandTestSubCommand))
                    .thenAnswer(ignored -> actionPermission.get());

            CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();
            ParseResults<CommandSourceStack> parsed = dispatcher.parse("sef test", source);
            assertTrue(parsed.getExceptions().isEmpty());

            SecurityAuditService.start(temporaryDirectory, 7, 1);
            try {
                actionPermission.set(false);
                assertEquals(0, dispatcher.execute(parsed));
            } finally {
                SecurityAuditService.shutdown();
            }
        }

        Path auditFile = temporaryDirectory.resolve("audit").resolve("security-audit.jsonl");
        JsonObject event = JsonParser.parseString(Files.readAllLines(auditFile).getFirst()).getAsJsonObject();
        assertEquals("sef:core.test", event.get("actionId").getAsString());
        assertEquals("rejected", event.get("result").getAsString());
        assertEquals("permission_denied", event.get("reasonCode").getAsString());
        assertEquals("player", event.get("sourceType").getAsString());
    }

    private static CommandDispatcher<CommandSourceStack> dispatcher() {
        LiteralArgumentBuilder<CommandSourceStack> root = BfcCommands.coreRoot();
        BfcCommands.registerFilterCommands(root);
        StorageCommands.attach(root);
        MotdCommands.attach(root);
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

    private static CommandSourceStack playerSource(ServerPlayer player) {
        CommandSource output = mock(CommandSource.class);
        ServerLevel level = mock(ServerLevel.class);
        MinecraftServer server = mock(MinecraftServer.class);
        when(level.dimension()).thenReturn(net.minecraft.world.level.Level.OVERWORLD);
        return new CommandSourceStack(
                output,
                Vec3.ZERO,
                Vec2.ZERO,
                level,
                4,
                "tester",
                Component.literal("tester"),
                server,
                player);
    }
}
