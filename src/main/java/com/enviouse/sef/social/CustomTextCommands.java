package com.enviouse.sef.social;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.permissions.PermissionService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.Map;

public final class CustomTextCommands {
    private CustomTextCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (!ConfigHandler.config.enableSocialEssentials.get()
                || !ConfigHandler.config.enableCustomText.get()) {
            return;
        }
        dispatcher.register(root("customtext"));
        dispatcher.register(root("booktext"));
        dispatcher.register(Commands.literal("rules")
                .requires(source -> PermissionService.has(source, PermissionsHandler.customTextCommand))
                .executes(context -> show(context.getSource(), "rules")));
        dispatcher.register(Commands.literal("info")
                .requires(source -> PermissionService.has(source, PermissionsHandler.customTextCommand))
                .executes(context -> show(context.getSource(), "info")));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> root(String root) {
        return Commands.literal(root)
                .requires(source -> PermissionService.has(source, PermissionsHandler.customTextCommand))
                .executes(context -> list(context.getSource()))
                .then(Commands.literal("set")
                        .requires(source -> PermissionService.has(source, PermissionsHandler.customTextManage))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("content", StringArgumentType.greedyString())
                                        .executes(context -> set(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "id"),
                                                StringArgumentType.getString(context, "content"))))))
                .then(Commands.literal("clear")
                        .requires(source -> PermissionService.has(source, PermissionsHandler.customTextManage))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(context -> set(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "id"),
                                        ""))))
                .then(Commands.argument("id", StringArgumentType.word())
                        .executes(context -> show(
                                context.getSource(),
                                StringArgumentType.getString(context, "id"))));
    }

    private static int show(CommandSourceStack source, String id) {
        return KernelCommandExecutor.execute(source, "sef:social.text", Map.of(
                "operation", "show",
                "page", id), () -> {
            String page;
            try {
                page = KernelServices.social().textPage(id);
            } catch (IllegalArgumentException exception) {
                fail(source, exception.getMessage());
                return 0;
            }
            if (page == null) {
                fail(source, "Text page not found.");
                return 0;
            }
            source.sendSuccess(() -> TextFormatter.stringToFormattedText(page), false);
            return 1;
        });
    }

    private static int list(CommandSourceStack source) {
        return KernelCommandExecutor.execute(source, "sef:social.text", Map.of("operation", "list"), () -> {
            var pages = KernelServices.social().textPages().keySet().stream().sorted().toList();
            source.sendSuccess(
                    () -> TextFormatter.stringToFormattedText(pages.isEmpty()
                            ? "&7No custom text pages are configured."
                            : "&7Custom text pages. &f" + String.join(", ", pages)),
                    false);
            return Math.max(1, pages.size());
        });
    }

    private static int set(CommandSourceStack source, String id, String content) {
        return KernelCommandExecutor.execute(source, "sef:social.text", Map.of(
                "operation", content.isBlank() ? "clear" : "set",
                "page", id), () -> {
            try {
                KernelServices.social().setTextPage(id, content);
            } catch (IllegalArgumentException | IllegalStateException exception) {
                fail(source, exception.getMessage());
                return 0;
            }
            source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                    content.isBlank() ? "&aText page cleared." : "&aText page updated."), false);
            return 1;
        }, PermissionsHandler.customTextManage);
    }

    private static void fail(CommandSourceStack source, String value) {
        source.sendFailure(TextFormatter.stringToFormattedText("&c" + value));
    }
}
