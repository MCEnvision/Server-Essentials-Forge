package com.enviouse.sef.sudo;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.commands.CommandRootPolicy;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.permissions.PermissionService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

/**
 * Registers /sudo command.
 *
 * Usage:
 *   /sudo <player> <command>
 *
 * Forces the target player to execute a command.
 * All messages are config-driven.
 */
public class SudoCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sudo")
            .requires(src -> PermissionService.has(src, PermissionsHandler.sudoCommand))
            .then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("command", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                        String command = StringArgumentType.getString(ctx, "command");
                        return executeSudo(ctx.getSource(), target, command);
                    }))));
    }

    private static int executeSudo(CommandSourceStack source, ServerPlayer target, String command) {
        if (PermissionService.has(target, PermissionsHandler.sudoExempt)
                && !PermissionService.has(source, PermissionsHandler.sudoBypassExempt)) {
            source.sendFailure(TextFormatter.stringToFormattedText("&cThat player is exempt from sudo."));
            return 0;
        }

        CommandRootPolicy.Decision decision = CommandRootPolicy.evaluate(
                command,
                ConfigHandler.config.sudoAllowedCommands.get(),
                ConfigHandler.config.sudoDeniedCommands.get(),
                ConfigHandler.config.sudoMaximumCommandLength.get());
        if (!decision.allowed()) {
            source.sendFailure(TextFormatter.stringToFormattedText(
                    "&cSudo denied. &7" + decision.reason() + "."));
            ServerEssentialsForge.LOGGER.warn("[SUDO] denied source {} target {} reason {}",
                    source.getTextName(), target.getGameProfile().getName(), decision.reason());
            return 0;
        }

        String adminName;
        try {
            adminName = source.getPlayerOrException().getGameProfile().getName();
        } catch (Exception e) {
            adminName = "Console";
        }

        CommandSourceStack targetSource = target.createCommandSourceStack();
        source.getServer().getCommands().performPrefixedCommand(targetSource, decision.command());

        String adminMsg = ConfigHandler.config.sudoExecutedMsg.get()
                .replace("$player", target.getGameProfile().getName())
                .replace("$command", decision.command())
                .replace("$admin", adminName);
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(adminMsg), false);

        if (ConfigHandler.config.sudoNotifyTarget.get()) {
            String targetMsg = ConfigHandler.config.sudoNotifyMsg.get()
                    .replace("$admin", adminName)
                    .replace("$command", decision.command());
            target.sendSystemMessage(TextFormatter.stringToFormattedText(targetMsg));
        }

        ServerEssentialsForge.LOGGER.info("[SUDO] {} forced {} to execute root {} with {} characters",
                adminName, target.getGameProfile().getName(), decision.root(), decision.command().length());
        return 1;
    }
}
