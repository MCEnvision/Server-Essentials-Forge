package com.enviouse.sef.commands;

import com.enviouse.sef.commandlog.CommandSpyCommands;
import com.enviouse.sef.commandlog.LoggingCommands;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.inventory.InventoryUtilityCommands;
import com.enviouse.sef.invsee.InvSeeCommand;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kits.KitCommands;
import com.enviouse.sef.moderation.ModerationCommands;
import com.enviouse.sef.player.GamemodeCommands;
import com.enviouse.sef.player.PlayerUtilityCommands;
import com.enviouse.sef.workstations.VirtualWorkstationCommands;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class PhaseSixSevenCommandDispatcherTest {
    @Test
    void phaseRootsAreRegisteredAndPermissionGated() {
        AtomicBoolean granted = new AtomicBoolean(false);
        ServerPlayer player = mock(ServerPlayer.class);
        CommandSourceStack source = mock(CommandSourceStack.class);
        when(source.getEntity()).thenReturn(player);
        when(source.getPlayer()).thenReturn(player);

        try (MockedStatic<PermissionAPI> permissions = permissionApi(granted)) {
            CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();
            List<String> roots = List.of(
                    "ban", "tempban", "pardon", "unban", "ban-ip", "banip", "tempban-ip",
                    "tempbanip", "pardon-ip", "unban-ip", "unbanip", "kick", "kick-ip", "kickip",
                    "kickme", "kickall", "warn", "warns", "clearwarnings", "mute", "unmute",
                    "mutelist", "freeze", "unfreeze", "freezelist", "invlock", "disablebuilding",
                    "db", "setjail", "deljail", "jails", "jail", "unjail", "jailedplayers",
                    "commandspy", "loggerspy",
                    "kit", "kits", "showkit", "createkit", "delkit", "kitreset",
                    "clearinventory", "ci", "enderchest", "ec", "disposal", "more", "condense",
                    "hat", "itemname", "itemlore", "itemdb", "book", "recipe", "i",
                    "afk", "feed", "heal", "fly", "god", "rest", "speed", "exp", "ptime",
                    "pweather", "near", "getpos", "compass", "depth", "top", "bottom",
                    "jump", "gm", "gmc", "gms", "gmsp", "gma", "invsee",
                    "craft", "c", "anvil", "av", "enchantingtable", "et",
                    "superenchantingtable", "set", "repair", "cartographytable", "grindstone",
                    "loom", "smithingtable", "stonecutter", "workbench", "wb");

            for (String root : roots) {
                assertNotNull(dispatcher.getRoot().getChild(root), root);
                assertFalse(dispatcher.getRoot().getChild(root).canUse(source), root);
            }

            granted.set(true);
            for (String root : roots) {
                assertTrue(dispatcher.getRoot().getChild(root).canUse(source), root);
            }
        }
    }

    @Test
    void representativePhaseGrammarParsesThroughBrigadier() {
        AtomicBoolean granted = new AtomicBoolean(true);
        ServerPlayer player = mock(ServerPlayer.class);
        CommandSourceStack source = mock(CommandSourceStack.class);
        when(source.getEntity()).thenReturn(player);
        when(source.getPlayer()).thenReturn(player);

        try (MockedStatic<PermissionAPI> permissions = permissionApi(granted)) {
            CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();
            List<String> commands = List.of(
                    "ban Notch griefing",
                    "tempban Notch 1h testing",
                    "kick-ip Notch",
                    "warn Notch testing",
                    "mute Notch 1h testing",
                    "commandspy filter source player off",
                    "commandspy filter player include Notch on",
                    "sef logging filter source view player off",
                    "sef logging retention run confirm 0123456789abcdef0123456789abcdef0123",
                    "kit starter",
                    "createkit starter 1h",
                    "i cobblestone 64",
                    "gm creative Notch",
                    "gmc Notch",
                    "enderchest Notch",
                    "itemname custom name",
                    "ptime day Notch",
                    "craft",
                    "grindstone");

            for (String command : commands) {
                ParseResults<CommandSourceStack> parsed = dispatcher.parse(command, source);
                assertTrue(parsed.getExceptions().isEmpty(), command + " " + parsed.getExceptions());
                assertFalse(parsed.getReader().canRead(), command);
            }
        }
    }

    @Test
    void selfItemShortcutRejectsAmountsAboveTheConfiguredMaximum() {
        AtomicBoolean granted = new AtomicBoolean(true);
        ServerPlayer player = mock(ServerPlayer.class);
        CommandSourceStack source = mock(CommandSourceStack.class);
        when(source.getEntity()).thenReturn(player);
        when(source.getPlayer()).thenReturn(player);

        try (MockedStatic<PermissionAPI> permissions = permissionApi(granted)) {
            CommandDispatcher<CommandSourceStack> dispatcher = dispatcher();
            int invalidAmount = ConfigHandler.config.itemGiveMaximumAmount.get() + 1;
            ParseResults<CommandSourceStack> parsed =
                    dispatcher.parse("i minecraft:cobblestone " + invalidAmount, source);

            assertTrue(parsed.getReader().canRead() || !parsed.getExceptions().isEmpty());
        }
    }

    private static CommandDispatcher<CommandSourceStack> dispatcher() {
        KernelServices.initialize();
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        var sef = Commands.literal("sef");
        CommandSpyCommands.attachCanonical(sef);
        LoggingCommands.attachCanonical(sef);
        dispatcher.register(sef);
        CommandSpyCommands.register(dispatcher);
        LoggingCommands.registerAlias(dispatcher);
        ModerationCommands.register(dispatcher);
        KitCommands.register(dispatcher);
        InventoryUtilityCommands.register(dispatcher);
        PlayerUtilityCommands.register(dispatcher);
        GamemodeCommands.register(dispatcher);
        InvSeeCommand.register(dispatcher);
        VirtualWorkstationCommands.register(dispatcher);
        return dispatcher;
    }

    private static MockedStatic<PermissionAPI> permissionApi(AtomicBoolean granted) {
        return mockStatic(PermissionAPI.class, invocation -> {
            if ("getPermission".equals(invocation.getMethod().getName())) {
                return granted.get();
            }
            return Answers.RETURNS_DEFAULTS.answer(invocation);
        });
    }
}
