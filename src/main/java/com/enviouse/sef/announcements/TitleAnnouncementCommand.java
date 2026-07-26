package com.enviouse.sef.announcements;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.permissions.PermissionService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

/**
 * /titleannouncement &lt;targets&gt; &lt;title&gt; [&lt;subtitle&gt;]
 * Title supports quoted strings. Subtitle is optional and greedy.
 */
public class TitleAnnouncementCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("titleannouncement")
            .requires(TitleAnnouncementCommand::hasPerm)
            .then(Commands.argument("targets", EntityArgument.players())
                .then(Commands.argument("title", StringArgumentType.string())
                    .executes(ctx -> doSend(ctx, false))
                    .then(Commands.argument("subtitle", StringArgumentType.greedyString())
                        .executes(ctx -> doSend(ctx, true))))));
    }

    private static boolean hasPerm(CommandSourceStack src) {
        return PermissionService.has(src, PermissionsHandler.titleAnnouncementUse);
    }

    private static int doSend(CommandContext<CommandSourceStack> ctx, boolean withSubtitle) {
        Collection<ServerPlayer> targets;
        try {
            targets = EntityArgument.getPlayers(ctx, "targets");
        } catch (Exception e) {
            ctx.getSource().sendFailure(TextFormatter.stringToFormattedText("&cInvalid targets."));
            return 0;
        }
        String titleStr = StringArgumentType.getString(ctx, "title");
        String subtitleText = "";
        if (withSubtitle) {
            subtitleText = StringArgumentType.getString(ctx, "subtitle");
        }
        TitleAnnouncement announcement = new TitleAnnouncement(titleStr, subtitleText);
        Component title = TextFormatter.stringToFormattedText(announcement.title());
        Component subtitle = announcement.subtitle().isEmpty()
                ? null
                : TextFormatter.stringToFormattedText(announcement.subtitle());
        for (ServerPlayer p : targets) {
            if (subtitle != null) {
                p.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
            }
            p.connection.send(new ClientboundSetTitleTextPacket(title));
        }
        ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText(
            "&aSent title to &e" + targets.size() + "&a player(s)."), false);
        return 1;
    }
}
