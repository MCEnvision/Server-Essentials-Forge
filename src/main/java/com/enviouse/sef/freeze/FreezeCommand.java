package com.enviouse.sef.freeze;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.identity.IdentityArguments;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.moderation.LegacyTargetPolicy;
import com.enviouse.sef.permissions.PermissionService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;

/**
 * Registers /freeze and /unfreeze commands.
 *
 * Usage:
 *   /freeze <player> <duration|infinite> <reason>
 *   /unfreeze <player>
 *
 * Duration formats: 30s, 5m, 1h, infinite
 * All messages are config-driven.
 */
public class FreezeCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (!ConfigHandler.config.enableFreezeSystem.get()) return;

        // /freeze <player> <duration> <reason>
        dispatcher.register(Commands.literal("freeze")
            .requires(src -> PermissionService.has(src, PermissionsHandler.freezeCommand))
            .then(IdentityArguments.online("player")
                .then(Commands.argument("duration", StringArgumentType.word())
                    .then(Commands.argument("reason", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerPlayer target = IdentityArguments.getOnline(ctx, "player");
                            String duration = StringArgumentType.getString(ctx, "duration");
                            String reason = StringArgumentType.getString(ctx, "reason");
                            return executeFreeze(ctx.getSource(), target, duration, reason);
                        })))));

        // /unfreeze <player>
        dispatcher.register(Commands.literal("unfreeze")
            .requires(src -> PermissionService.has(src, PermissionsHandler.unfreezeCommand))
            .then(IdentityArguments.online("player")
                .executes(ctx -> {
                    ServerPlayer target = IdentityArguments.getOnline(ctx, "player");
                    return executeUnfreeze(ctx.getSource(), target);
                })));
    }

    private static int executeFreeze(CommandSourceStack source, ServerPlayer target, String durationStr, String reason) {
        return KernelCommandExecutor.execute(
                source,
                "sef:moderation.freeze",
                Map.of("duration", durationStr, "reason_length", Integer.toString(reason.length())),
                List.of(target.getUUID()),
                false,
                () -> {
                    if (!mayTarget(source, target, true)) {
                        source.sendFailure(TextFormatter.stringToFormattedText("&cThat player cannot be targeted by this command."));
                        return 0;
                    }
                    String adminName;
                    try {
                        adminName = source.getPlayerOrException().getGameProfile().getName();
                    } catch (Exception e) {
                        adminName = "Console";
                    }
                    if (FreezeManager.isFrozen(target.getUUID())) {
                        source.sendFailure(TextFormatter.stringToFormattedText(
                            "&c" + target.getGameProfile().getName() + " is already frozen."));
                        return 0;
                    }
                    long durationTicks = FreezeManager.parseDuration(durationStr);
                    if (durationTicks == com.enviouse.sef.util.DurationParser.INVALID_VALUE) {
                        source.sendFailure(TextFormatter.stringToFormattedText(
                                "&cInvalid duration. Use values such as &e30s&c, &e1h30m&c, &e7d&c, or &epermanent&c."));
                        return 0;
                    }
                    FreezeManager.freezePlayer(target, adminName, reason, durationTicks, source.getServer());
                    return 1;
                },
                PermissionsHandler.freezeCommand);
    }

    private static int executeUnfreeze(CommandSourceStack source, ServerPlayer target) {
        return KernelCommandExecutor.execute(
                source,
                "sef:moderation.unfreeze",
                Map.of(),
                List.of(target.getUUID()),
                false,
                () -> {
                    if (!mayTarget(source, target, true)) {
                        source.sendFailure(TextFormatter.stringToFormattedText("&cThat player cannot be targeted by this command."));
                        return 0;
                    }
                    String adminName;
                    try {
                        adminName = source.getPlayerOrException().getGameProfile().getName();
                    } catch (Exception e) {
                        adminName = "Console";
                    }
                    if (!FreezeManager.isFrozen(target.getUUID())) {
                        source.sendFailure(TextFormatter.stringToFormattedText(
                            "&c" + target.getGameProfile().getName() + " is not frozen."));
                        return 0;
                    }
                    FreezeManager.unfreezePlayer(target.getUUID(), adminName, source.getServer());
                    return 1;
                },
                PermissionsHandler.unfreezeCommand);
    }

    private static boolean mayTarget(CommandSourceStack source, ServerPlayer target, boolean rejectSelf) {
        return LegacyTargetPolicy.mayTarget(source, target, "exempt.freeze", rejectSelf);
    }
}
