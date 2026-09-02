package com.enviouse.sef.motd;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.events.CommandRegistrationHandler;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.permissions.PermissionService;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.Map;

/**
 * Commands: /sef motd set, /sef motd reload, /sef motd show.
 */
public final class MotdCommands {
    private MotdCommands() {
    }

    public static void attach(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("motd")
                .requires(src -> PermissionService.has(src, PermissionsHandler.motdManage))
                // /sef motd set <line1> [| line2]
                .then(Commands.literal("set")
                    .then(Commands.argument("motd", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String motd = StringArgumentType.getString(ctx, "motd");
                            String[] lines = motd.split("\\|", 2);
                            return KernelCommandExecutor.execute(
                                    ctx.getSource(),
                                    "sef:motd.set",
                                    Map.of(
                                            "line_count", lines.length > 1 ? "2" : "1",
                                            "character_count", Integer.toString(motd.length())),
                                    () -> set(ctx, lines));
                        })))
                // /sef motd reload
                .then(Commands.literal("reload")
                    .executes(ctx -> KernelCommandExecutor.execute(
                            ctx.getSource(),
                            "sef:motd.reload",
                            Map.of(),
                            () -> reload(ctx))))
                // /sef motd show
                .then(Commands.literal("show")
                    .executes(ctx -> KernelCommandExecutor.execute(
                            ctx.getSource(),
                            "sef:motd.show",
                            Map.of(),
                            () -> show(ctx)))));
    }

    private static int set(CommandContext<CommandSourceStack> context, String[] lines) {
        MotdManager manager = manager(context.getSource());
        if (manager == null) {
            return 0;
        }
        String line1 = lines[0].trim();
        String line2 = lines.length > 1 ? lines[1].trim() : "";
        try {
            manager.setMotd(line1, line2);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            context.getSource().sendFailure(TextFormatter.stringToFormattedText(
                    "&cMOTD could not be updated. &7" + exception.getMessage()));
            return 0;
        }
        manager.applyToServer(context.getSource().getServer());
        context.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&aMOTD updated and applied"), false);
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        MotdManager manager = manager(context.getSource());
        if (manager == null) {
            return 0;
        }
        manager.load();
        if (manager.state() == com.enviouse.sef.storage.repository.StorageRepository.RepositoryState.RECOVERY
                || manager.state() == com.enviouse.sef.storage.repository.StorageRepository.RepositoryState.ERROR) {
            context.getSource().sendFailure(TextFormatter.stringToFormattedText(
                    "&cMOTD reload failed. The previous MOTD remains active."));
            return 0;
        }
        manager.applyToServer(context.getSource().getServer());
        context.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&aMOTD reloaded and applied"), false);
        return 1;
    }

    private static int show(CommandContext<CommandSourceStack> context) {
        MotdManager manager = manager(context.getSource());
        if (manager == null) {
            return 0;
        }
        var data = manager.getData();
        context.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&6Current MOTD:"), false);
        context.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&7Line 1: &f" + data.line1()), false);
        context.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&7Line 2: &f" + data.line2()), false);
        return 1;
    }

    private static MotdManager manager(CommandSourceStack source) {
        MotdManager manager = CommandRegistrationHandler.getMotdManager();
        if (manager == null) {
            source.sendFailure(TextFormatter.stringToFormattedText("&cMOTD system not initialized"));
        }
        return manager;
    }
}
