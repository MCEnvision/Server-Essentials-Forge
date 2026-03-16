package com.jeremiahbl.bfcrmod.chat;

import com.jeremiahbl.bfcrmod.BetterForgeChat;
import com.jeremiahbl.bfcrmod.TextFormatter;
import com.jeremiahbl.bfcrmod.config.ConfigHandler;
import com.jeremiahbl.bfcrmod.config.PermissionsHandler;
import com.jeremiahbl.bfcrmod.events.ServerMessageEvent;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.*;

/**
 * Handles admin-chat toggling and the /ac, /chat admin commands.
 */
public class AdminChatHandler {
    private static final Set<UUID> toggledPlayers = new HashSet<>();

    public static boolean isAdminChatToggled(UUID uuid) {
        return toggledPlayers.contains(uuid);
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (!ConfigHandler.config.enableAdminChat.get()) return;

        // /ac <message>
        dispatcher.register(Commands.literal("ac")
            .requires(src -> {
                try {
                    return PermissionsHandler.playerHasPermission(
                        src.getPlayerOrException().getUUID(), PermissionsHandler.adminChatUse);
                } catch (Exception e) { return true; }
            })
            .then(Commands.argument("message", StringArgumentType.greedyString())
                .executes(ctx -> {
                    String msg = StringArgumentType.getString(ctx, "message");
                    return sendAdminMessage(ctx.getSource(), msg);
                })));

        // /chat admin (toggle)
        dispatcher.register(Commands.literal("chat")
            .then(Commands.literal("admin")
                .requires(src -> {
                    try {
                        return PermissionsHandler.playerHasPermission(
                            src.getPlayerOrException().getUUID(), PermissionsHandler.adminChatUse);
                    } catch (Exception e) { return true; }
                })
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    UUID uuid = player.getUUID();
                    if (toggledPlayers.contains(uuid)) {
                        toggledPlayers.remove(uuid);
                        player.sendSystemMessage(TextFormatter.stringToFormattedText(
                            ConfigHandler.config.adminChatDisabledMsg.get()));
                    } else {
                        toggledPlayers.add(uuid);
                        player.sendSystemMessage(TextFormatter.stringToFormattedText(
                            ConfigHandler.config.adminChatEnabledMsg.get()));
                    }
                    return 1;
                })));

        // /helpop <message>
        if (ConfigHandler.config.enableHelpOp.get()) {
            dispatcher.register(Commands.literal("helpop")
                .requires(src -> {
                    try {
                        return PermissionsHandler.playerHasPermission(
                            src.getPlayerOrException().getUUID(), PermissionsHandler.helpOpSend);
                    } catch (Exception e) { return true; }
                })
                .then(Commands.argument("message", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        String msg = StringArgumentType.getString(ctx, "message");
                        return sendHelpOp(ctx.getSource(), msg);
                    })));

            // /helpopop <player> <message> (reply)
            dispatcher.register(Commands.literal("helpopop")
                .requires(src -> {
                    try {
                        return PermissionsHandler.playerHasPermission(
                            src.getPlayerOrException().getUUID(), PermissionsHandler.helpOpReply);
                    } catch (Exception e) { return true; }
                })
                .then(Commands.argument("player", StringArgumentType.word())
                    .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String targetName = StringArgumentType.getString(ctx, "player");
                            String msg = StringArgumentType.getString(ctx, "message");
                            return sendHelpOpReply(ctx.getSource(), targetName, msg);
                        }))));
        }
    }

    private static int sendAdminMessage(CommandSourceStack source, String message) {
        String senderName;
        try {
            senderName = source.getPlayerOrException().getGameProfile().getName();
        } catch (Exception e) {
            senderName = "Console";
        }

        String format = ConfigHandler.config.adminChatFormat.get()
            .replace("$sender", senderName)
            .replace("$message", message);
        MutableComponent component = TextFormatter.stringToFormattedText(format);

        // Send to all players with adminChatSee permission
        int count = 0;
        for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
            if (PermissionsHandler.playerHasPermission(player.getUUID(), PermissionsHandler.adminChatSee)) {
                player.sendSystemMessage(component);
                if (ConfigHandler.config.enableAdminChatSound.get()) {
                    player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.MASTER, 1.0f, 1.0f);
                }
                count++;
            }
        }
        BetterForgeChat.LOGGER.info("[ADMIN CHAT] {}: {}", senderName, message);
        return count > 0 ? 1 : 0;
    }

    private static int sendHelpOp(CommandSourceStack source, String message) {
        String senderName;
        try {
            senderName = source.getPlayerOrException().getGameProfile().getName();
        } catch (Exception e) {
            senderName = "Console";
        }

        String format = ConfigHandler.config.helpOpRequestFormat.get()
            .replace("$sender", senderName)
            .replace("$message", message);
        MutableComponent component = TextFormatter.stringToFormattedText(format);

        int count = 0;
        for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
            if (PermissionsHandler.playerHasPermission(player.getUUID(), PermissionsHandler.helpOpReceive)) {
                player.sendSystemMessage(component);
                if (ConfigHandler.config.enableHelpOpSound.get()) {
                    player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.MASTER, 1.0f, 1.0f);
                }
                count++;
            }
        }

        String sentMsg = ConfigHandler.config.helpOpSentMsg.get().replace("$count", String.valueOf(count));
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(sentMsg), false);
        BetterForgeChat.LOGGER.info("[HELPOP] {}: {}", senderName, message);
        return 1;
    }

    private static int sendHelpOpReply(CommandSourceStack source, String targetName, String message) {
        ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(targetName);
        if (target == null) {
            source.sendFailure(TextFormatter.stringToFormattedText(ConfigHandler.config.playerOfflineMsg.get()));
            return 0;
        }

        String format = ConfigHandler.config.helpOpReplyFormat.get().replace("$message", message);
        target.sendSystemMessage(TextFormatter.stringToFormattedText(format));
        if (ConfigHandler.config.enableHelpOpSound.get()) {
            target.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.MASTER, 1.0f, 1.0f);
        }

        String sentMsg = ConfigHandler.config.helpOpReplySentMsg.get().replace("$player", targetName);
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(sentMsg), false);
        return 1;
    }
}

