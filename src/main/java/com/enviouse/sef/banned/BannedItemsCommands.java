package com.enviouse.sef.banned;

import com.enviouse.sef.TextFormatter;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * Commands: /banned add, /banned remove, /banned list.
 */
public class BannedItemsCommands {
    private static BannedItemsManager manager;

    public static void setManager(BannedItemsManager mgr) {
        manager = mgr;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("banned")
            .requires(src -> src.hasPermission(2))
            // /banned add <item_id>
            .then(Commands.literal("add")
                .then(Commands.argument("item", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        String item = StringArgumentType.getString(ctx, "item");
                        manager.addItem(item);
                        ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText(
                            "&aBanned item added: &e" + item), false);
                        return 1;
                    })))
            // /banned remove <item_id>
            .then(Commands.literal("remove")
                .then(Commands.argument("item", StringArgumentType.greedyString())
                    .suggests((ctx, builder) -> {
                        manager.getBannedItems().forEach(builder::suggest);
                        return builder.buildFuture();
                    })
                    .executes(ctx -> {
                        String item = StringArgumentType.getString(ctx, "item");
                        if (manager.removeItem(item)) {
                            ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText(
                                "&aRemoved banned item: &e" + item), false);
                            return 1;
                        }
                        ctx.getSource().sendFailure(TextFormatter.stringToFormattedText("&cItem not in banned list: " + item));
                        return 0;
                    })))
            // /banned list
            .then(Commands.literal("list")
                .executes(ctx -> {
                    var items = manager.getBannedItems();
                    if (items.isEmpty()) {
                        ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText("&7No banned items configured"), false);
                        return 1;
                    }
                    ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText("&6━━━━━━━━ Banned Items ━━━━━━━━"), false);
                    for (String item : items) {
                        ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText("  &7• &f" + item), false);
                    }
                    return 1;
                })));
    }
}

