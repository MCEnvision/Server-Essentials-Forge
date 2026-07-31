package com.enviouse.sef.chat;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.utils.SEFUtilities;
import com.enviouse.sef.vanish.VanishUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Handles the /ans (reply to a chat message) command.
 */
public class ChatReplyHandler {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ans")
            .requires(src -> PermissionService.has(src, PermissionsHandler.ansCommand))
            .then(Commands.argument("token", StringArgumentType.word())
                .then(Commands.argument("reply", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        String token = StringArgumentType.getString(ctx, "token");
                        String reply = StringArgumentType.getString(ctx, "reply");
                        return handleReply(ctx.getSource(), token, reply);
                    }))));
    }

    private static int handleReply(CommandSourceStack source, String token, String reply) {
        if (!PermissionService.has(source, PermissionsHandler.ansCommand)) {
            source.sendFailure(TextFormatter.stringToFormattedText(ConfigHandler.config.noPermissionMsg.get()));
            return 0;
        }
        ServerPlayer replier;
        try {
            replier = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(TextFormatter.stringToFormattedText("&cOnly players can reply to messages."));
            return 0;
        }

        if (reply == null || reply.isBlank() || reply.length() > 1_024) {
            unavailable(source);
            return 0;
        }

        ChatMessageManager.ChatRecord original =
                ChatMessageManager.resolve(token, replier.getUUID());
        if (original == null) {
            unavailable(source);
            return 0;
        }
        ServerPlayer originalSender = source.getServer().getPlayerList().getPlayer(original.senderUuid());
        if (originalSender == null
                || !VanishUtil.playerAllowedToSeeOther(
                        replier,
                        originalSender,
                        VanishUtil.isVanished(replier),
                        true)) {
            unavailable(source);
            return 0;
        }

        String summary = original.message();
        int maxLen = ConfigHandler.config.replySummaryLength.get();
        if (maxLen > 0 && summary.length() > maxLen) {
            summary = summary.substring(0, maxLen) + "...";
        }

        String replierFormattedName = SEFUtilities.getRawPreferredPlayerName(replier.getGameProfile());

        String headerFormat = ConfigHandler.config.replyHeaderFormat.get()
            .replace("$replier", replierFormattedName)
            .replace("$original_sender", original.formattedName())
            .replace("$summary", summary);
        MutableComponent header = TextFormatter.stringToFormattedText(headerFormat);

        String bodyFormat = ConfigHandler.config.replyBodyFormat.get()
            .replace("$replier", replierFormattedName)
            .replace("$message", reply);
        MutableComponent replyMsg = TextFormatter.stringToFormattedText(bodyFormat);
        MutableComponent fullReply = header.append("\n").append(replyMsg);

        if (!ChatMessageManager.consume(token, replier.getUUID())) {
            unavailable(source);
            return 0;
        }
        originalSender.sendSystemMessage(fullReply);
        if (ConfigHandler.config.enableReplySound.get()) {
            originalSender.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.MASTER, 1.0f, 1.0f);
        }

        replier.sendSystemMessage(fullReply);

        ServerEssentialsForge.LOGGER.info(
            "[REPLY] {} replied to {} with {} characters",
            replier.getGameProfile().getName(), original.rawName(), reply.length());
        return 1;
    }

    private static void unavailable(CommandSourceStack source) {
        source.sendFailure(TextFormatter.stringToFormattedText(ConfigHandler.config.messageNotFoundMsg.get()));
    }
}
