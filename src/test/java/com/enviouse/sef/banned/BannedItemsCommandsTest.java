package com.enviouse.sef.banned;

import com.enviouse.sef.config.PermissionsHandler;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;

class BannedItemsCommandsTest {
    @AfterEach
    void clearManager() {
        BannedItemsCommands.setManager(null);
    }

    @Test
    void parsesSupportedDurations() {
        assertEquals(-1L, BannedItemsCommands.parseDurationMs("permanent"));
        assertEquals(30_000L, BannedItemsCommands.parseDurationMs("30s"));
        assertEquals(300_000L, BannedItemsCommands.parseDurationMs("5m"));
        assertEquals(5_400_000L, BannedItemsCommands.parseDurationMs("1h30m"));
        assertEquals(216_000_000L, BannedItemsCommands.parseDurationMs("2d12h"));
        assertEquals(60_000L, BannedItemsCommands.parseDurationMs("60"));
    }

    @Test
    void rejectsMalformedDurationsInsteadOfCreatingPermanentBans() {
        assertThrows(IllegalArgumentException.class, () -> BannedItemsCommands.parseDurationMs("nonsense"));
        assertThrows(IllegalArgumentException.class, () -> BannedItemsCommands.parseDurationMs("5x"));
        assertThrows(IllegalArgumentException.class, () -> BannedItemsCommands.parseDurationMs("1h2"));
        assertThrows(IllegalArgumentException.class, () -> BannedItemsCommands.parseDurationMs("0"));
        assertThrows(IllegalArgumentException.class, () -> BannedItemsCommands.parseDurationMs(""));
        assertThrows(IllegalArgumentException.class, () -> BannedItemsCommands.parseDurationMs(null));
    }

    @Test
    void rejectsOverflowingDurations() {
        assertThrows(
                IllegalArgumentException.class,
                () -> BannedItemsCommands.parseDurationMs("999999999999999999999999d"));
    }

    @Test
    void malformedDurationCannotMutateThroughCommandDispatcher() throws Exception {
        Path root = Files.createTempDirectory("sef-banned-command-test");
        try {
            BannedItemsManager manager = new BannedItemsManager();
            manager.load(root);
            BannedItemsCommands.setManager(manager);

            CommandSourceStack source = mock(CommandSourceStack.class);
            ServerPlayer player = mock(ServerPlayer.class);
            when(source.getEntity()).thenReturn(player);
            CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
            try (MockedStatic<PermissionAPI> permissions = mockStatic(PermissionAPI.class)) {
                permissions.when(() -> PermissionAPI.getPermission(
                                player,
                                PermissionsHandler.bannedCommand))
                        .thenReturn(true);
                BannedItemsCommands.register(dispatcher);

                assertEquals(0, dispatcher.execute("banned add minecraft:stone 5x", source));
            }
            assertTrue(manager.getEntries().isEmpty(), "malformed duration changed banned state");
        } finally {
            deleteTree(root);
        }
    }

    private static void deleteTree(Path root) throws IOException {
        if (root == null || !Files.exists(root)) {
            return;
        }
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }
}
