package com.enviouse.sef.announcements;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * Commands for managing announcements: /announce add/remove/list/toggle.
 */
public class AnnouncementCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, AnnouncementManager manager) {
        dispatcher.register(Commands.literal("announce")
            .requires(src -> src.hasPermission(2))
            // /announce add <id> <type: text|command> <message>
            .then(Commands.literal("add")
                .then(Commands.argument("id", StringArgumentType.word())
                    .then(Commands.argument("type", StringArgumentType.word())
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String id = StringArgumentType.getString(ctx, "id");
                                String type = StringArgumentType.getString(ctx, "type");
                                String message = StringArgumentType.getString(ctx, "message");
                                manager.addAnnouncement(id, type, message);
                                int interval = ConfigHandler.config.announcementIntervalSeconds.get();
                                String confirmFormat = ConfigHandler.config.announcementConfirmFormat.get()
                                    .replace("$id", id)
                                    .replace("$interval", interval + "s")
                                    .replace("$message", message);
                                ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText(confirmFormat), false);
                                return 1;
                            })))))
            // /announce remove <id>
            .then(Commands.literal("remove")
                .then(Commands.argument("id", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        manager.getAnnouncements().forEach(a -> builder.suggest(a.id()));
                        return builder.buildFuture();
                    })
                    .executes(ctx -> {
                        String id = StringArgumentType.getString(ctx, "id");
                        if (manager.removeAnnouncement(id)) {
                            ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText("&aRemoved announcement: &e" + id), false);
                            return 1;
                        }
                        ctx.getSource().sendFailure(TextFormatter.stringToFormattedText("&cAnnouncement not found: " + id));
                        return 0;
                    })))
            // /announce list
            .then(Commands.literal("list")
                .executes(ctx -> {
                    var anns = manager.getAnnouncements();
                    if (anns.isEmpty()) {
                        ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText("&7No announcements configured"), false);
                        return 1;
                    }
                    // Text announcements
                    ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText(
                        ConfigHandler.config.announcementListHeaderText.get()), false);
                    for (var ann : anns) {
                        if ("text".equalsIgnoreCase(ann.type())) {
                            String status = ann.enabled() ? "&a[ON]" : "&c[OFF]";
                            ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText(
                                "  &e" + ann.id() + " " + status + " &7" + ann.message()), false);
                        }
                    }
                    // Command announcements
                    ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText(
                        ConfigHandler.config.announcementListHeaderCmd.get()), false);
                    for (var ann : anns) {
                        if ("command".equalsIgnoreCase(ann.type())) {
                            String status = ann.enabled() ? "&a[ON]" : "&c[OFF]";
                            ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText(
                                "  &e" + ann.id() + " " + status + " &7/" + ann.message()), false);
                        }
                    }
                    return 1;
                })));

        // /announce toggle (player self-toggle)
        dispatcher.register(Commands.literal("announce")
            .then(Commands.literal("toggle")
                .requires(src -> {
                    try {
                        return PermissionsHandler.playerHasPermission(
                            src.getPlayerOrException().getUUID(), PermissionsHandler.announcementToggle);
                    } catch (Exception e) { return false; }
                })
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    boolean nowOn = manager.togglePlayer(player.getUUID());
                    String text = nowOn
                        ? ConfigHandler.config.toggleOnText.get() + " &7Announcements enabled"
                        : ConfigHandler.config.toggleOffText.get() + " &7Announcements disabled";
                    ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText(text), false);
                    return 1;
                })));
    }
}

