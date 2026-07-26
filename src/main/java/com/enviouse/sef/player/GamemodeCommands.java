package com.enviouse.sef.player;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.identity.IdentityArguments;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.vanish.VanishUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class GamemodeCommands {
    private GamemodeCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (!ConfigHandler.config.enableGamemodeShortcuts.get()) {
            return;
        }
        if (KernelServices.shortcuts().isActive("gmc")) {
            dispatcher.register(fixedNode("gmc", GameType.CREATIVE));
        }
        if (KernelServices.shortcuts().isActive("gms")) {
            dispatcher.register(fixedNode("gms", GameType.SURVIVAL));
        }
        if (KernelServices.shortcuts().isActive("gmsp")) {
            dispatcher.register(fixedNode("gmsp", GameType.SPECTATOR));
        }
        if (KernelServices.shortcuts().isActive("gma")) {
            dispatcher.register(fixedNode("gma", GameType.ADVENTURE));
        }
        if (KernelServices.shortcuts().isActive("gm")) {
            dispatcher.register(Commands.literal("gm")
                .requires(source -> has(source, "commands.gamemode"))
                .then(Commands.argument("mode", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                new String[]{"creative", "survival", "spectator", "adventure",
                                        "c", "s", "sp", "a", "0", "1", "2", "3"},
                                builder))
                        .executes(context -> parsed(
                                context.getSource(),
                                StringArgumentType.getString(context, "mode"),
                                null))
                        .then(IdentityArguments.online("player")
                                .requires(source -> has(source, "commands.gamemode.others"))
                                .executes(context -> parsed(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "mode"),
                                IdentityArguments.getOnline(context, "player"))))));
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> fixedNode(String literal, GameType mode) {
        String modeName = modeName(mode);
        return Commands.literal(literal)
                .requires(source -> has(source, "commands.gamemode." + modeName))
                .executes(context -> change(context.getSource(), context.getSource().getPlayer(), mode, false))
                .then(IdentityArguments.online("player")
                        .requires(source -> has(source, "commands.gamemode." + modeName + ".others"))
                        .executes(context -> change(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "player"),
                                mode,
                                true)));
    }

    private static int parsed(CommandSourceStack source, String input, ServerPlayer explicitTarget) {
        GameType mode = parse(input);
        if (mode == null) {
            return fail(source, "Use creative, survival, spectator, adventure, c, s, sp, a, 0, 1, 2, or 3.");
        }
        boolean other = explicitTarget != null;
        ServerPlayer target = other ? explicitTarget : source.getPlayer();
        if (!has(source, "commands.gamemode." + modeName(mode))) {
            return fail(source, "You do not have permission for that game mode.");
        }
        return change(source, target, mode, other, permission("commands.gamemode"));
    }

    @SafeVarargs
    private static int change(
            CommandSourceStack source,
            ServerPlayer target,
            GameType mode,
            boolean other,
            PermissionNode<Boolean>... routePermissions
    ) {
        if (target == null) {
            return fail(source, "An explicit online target is required.");
        }
        if (other && !eligible(source, target)) {
            return fail(source, "That player is unavailable.");
        }
        String modeName = modeName(mode);
        String modePermission = "commands.gamemode." + modeName + (other ? ".others" : "");
        PermissionNode<Boolean>[] permissions = java.util.Arrays.copyOf(
                routePermissions,
                routePermissions.length + 1);
        permissions[routePermissions.length] = permission(modePermission);
        return KernelCommandExecutor.execute(
                source,
                "sef:gamemode." + modeName,
                Map.of("target", target.getUUID().toString(), "mode", modeName),
                List.of(target.getUUID()),
                false,
                () -> {
                    if (target.gameMode.getGameModeForPlayer() == mode) {
                        info(source, target.getGameProfile().getName() + " is already in " + modeName + ".");
                        return 1;
                    }
                    if (!target.setGameMode(mode)) {
                        return fail(source, "The server rejected that game mode transition.");
                    }
                    if (PlayerStateService.fly(target.getUUID())) {
                        target.getAbilities().mayfly = true;
                        target.onUpdateAbilities();
                    }
                    success(source, "Set " + target.getGameProfile().getName() + " to " + modeName + ".");
                    return 1;
                },
                permissions);
    }

    private static boolean eligible(CommandSourceStack source, ServerPlayer target) {
        if (source.getPlayer() != null && VanishUtil.isVanished(target, source.getPlayer())) {
            return false;
        }
        return PlayerTargetPolicy.decide(
                source,
                target,
                permission("utilities.hierarchy.bypass"),
                permission("exempt.gamemode"),
                permission("utilities.bypass.exempt"),
                false,
                true).allowed();
    }

    private static GameType parse(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "survival", "s", "0" -> GameType.SURVIVAL;
            case "creative", "c", "1" -> GameType.CREATIVE;
            case "adventure", "a", "2" -> GameType.ADVENTURE;
            case "spectator", "sp", "3" -> GameType.SPECTATOR;
            default -> null;
        };
    }

    private static String modeName(GameType mode) {
        return mode.getName();
    }

    private static boolean has(CommandSourceStack source, String id) {
        PermissionNode<Boolean> node = permission(id);
        return node != null && PermissionService.has(source, node);
    }

    private static PermissionNode<Boolean> permission(String id) {
        return PermissionsHandler.phasePermission(id);
    }

    private static int fail(CommandSourceStack source, String message) {
        source.sendFailure(TextFormatter.stringToFormattedText("&c" + message));
        return 0;
    }

    private static void success(CommandSourceStack source, String message) {
        source.sendSuccess(() -> TextFormatter.stringToFormattedText("&a" + message), false);
    }

    private static void info(CommandSourceStack source, String message) {
        source.sendSuccess(() -> TextFormatter.stringToFormattedText("&7" + message), false);
    }
}
