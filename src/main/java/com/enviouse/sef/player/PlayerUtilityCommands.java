package com.enviouse.sef.player;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.identity.IdentityArguments;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.teleport.SafeTeleportService;
import com.enviouse.sef.teleport.SavedLocation;
import com.enviouse.sef.vanish.VanishUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class PlayerUtilityCommands {
    private PlayerUtilityCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (!ConfigHandler.config.enablePlayerUtilities.get()) {
            return;
        }
        dispatcher.register(Commands.literal("afk")
                .requires(source -> source.getPlayer() != null && has(source, "commands.afk"))
                .executes(context -> afk(context.getSource())));
        dispatcher.register(targetNode("feed", PlayerUtilityCommands::feed));
        dispatcher.register(targetNode("heal", PlayerUtilityCommands::heal));
        dispatcher.register(targetNode("fly", PlayerUtilityCommands::fly));
        dispatcher.register(targetNode("god", PlayerUtilityCommands::god));
        dispatcher.register(targetNode("rest", PlayerUtilityCommands::rest));
        dispatcher.register(speedNode());
        dispatcher.register(expNode());
        dispatcher.register(personalTimeNode());
        dispatcher.register(personalWeatherNode());
        if (ConfigHandler.config.enableSuicideCommand.get()) {
            dispatcher.register(Commands.literal("suicide")
                    .requires(source -> source.getPlayer() != null && has(source, "commands.suicide"))
                    .executes(context -> suicide(context.getSource())));
        }
        dispatcher.register(Commands.literal("near")
                .requires(source -> source.getPlayer() != null && has(source, "commands.near"))
                .executes(context -> near(context.getSource(), 100))
                .then(Commands.argument("radius", IntegerArgumentType.integer(1, 512))
                        .executes(context -> near(
                                context.getSource(),
                                IntegerArgumentType.getInteger(context, "radius")))));
        dispatcher.register(getPosNode());
        dispatcher.register(Commands.literal("compass")
                .requires(source -> source.getPlayer() != null && has(source, "commands.compass"))
                .executes(context -> compass(context.getSource())));
        dispatcher.register(Commands.literal("depth")
                .requires(source -> source.getPlayer() != null && has(source, "commands.depth"))
                .executes(context -> depth(context.getSource())));
        dispatcher.register(Commands.literal("top")
                .requires(source -> source.getPlayer() != null && has(source, "commands.top"))
                .executes(context -> top(context.getSource())));
        dispatcher.register(Commands.literal("bottom")
                .requires(source -> source.getPlayer() != null && has(source, "commands.bottom"))
                .executes(context -> bottom(context.getSource())));
        dispatcher.register(Commands.literal("jump")
                .requires(source -> source.getPlayer() != null && has(source, "commands.jump"))
                .executes(context -> jump(context.getSource())));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> targetNode(
            String literal,
            TargetAction action
    ) {
        return Commands.literal(literal)
                .requires(source -> has(source, "commands." + literal)
                        || has(source, "commands." + literal + ".others"))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    return player == null
                            ? fail(context.getSource(), "An explicit online target is required.")
                            : action.run(context.getSource(), player, false);
                })
                .then(IdentityArguments.online("player")
                        .requires(source -> has(source, "commands." + literal + ".others"))
                        .executes(context -> action.run(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "player"),
                                true)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> speedNode() {
        return Commands.literal("speed")
                .requires(source -> has(source, "commands.speed")
                        || has(source, "commands.speed.others"))
                .then(Commands.literal("reset")
                        .executes(context -> speed(context.getSource(), 1.0D, null, false))
                        .then(IdentityArguments.online("player")
                                .requires(source -> has(source, "commands.speed.others"))
                                .executes(context -> speed(
                                        context.getSource(),
                                        1.0D,
                                        IdentityArguments.getOnline(context, "player"),
                                        true))))
                .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.1D, 10.0D))
                        .executes(context -> speed(
                                context.getSource(),
                                DoubleArgumentType.getDouble(context, "value"),
                                null,
                                false))
                        .then(IdentityArguments.online("player")
                                .requires(source -> has(source, "commands.speed.others"))
                                .executes(context -> speed(
                                        context.getSource(),
                                        DoubleArgumentType.getDouble(context, "value"),
                                        IdentityArguments.getOnline(context, "player"),
                                        true))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> expNode() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("exp")
                .requires(source -> has(source, "commands.exp")
                        || has(source, "commands.exp.others"))
                .executes(context -> expStatus(context.getSource()));
        for (String operation : List.of("set", "add", "take")) {
            root.then(Commands.literal(operation)
                    .then(Commands.argument("levels", IntegerArgumentType.integer(0, 1000000))
                            .executes(context -> exp(
                                    context.getSource(),
                                    context.getSource().getPlayer(),
                                    operation,
                                    IntegerArgumentType.getInteger(context, "levels"),
                                    false))
                            .then(IdentityArguments.online("player")
                                    .requires(source -> has(source, "commands.exp.others"))
                                    .executes(context -> exp(
                                            context.getSource(),
                                            IdentityArguments.getOnline(context, "player"),
                                            operation,
                                            IntegerArgumentType.getInteger(context, "levels"),
                                            true)))));
        }
        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> personalTimeNode() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("ptime")
                .requires(source -> has(source, "commands.ptime")
                        || has(source, "commands.ptime.others"));
        for (String value : List.of("day", "night", "reset")) {
            root.then(Commands.literal(value)
                    .executes(context -> personalTime(
                            context.getSource(),
                            context.getSource().getPlayer(),
                            value.equals("reset") ? null : value.equals("day") ? 1000L : 13000L,
                            false))
                    .then(IdentityArguments.online("player")
                            .requires(source -> has(source, "commands.ptime.others"))
                            .executes(context -> personalTime(
                                    context.getSource(),
                                    IdentityArguments.getOnline(context, "player"),
                                    value.equals("reset") ? null : value.equals("day") ? 1000L : 13000L,
                                    true))));
        }
        root.then(Commands.argument("ticks", LongArgumentType.longArg(0L, Long.MAX_VALUE))
                .executes(context -> personalTime(
                        context.getSource(),
                        context.getSource().getPlayer(),
                        LongArgumentType.getLong(context, "ticks"),
                        false))
                .then(IdentityArguments.online("player")
                        .requires(source -> has(source, "commands.ptime.others"))
                        .executes(context -> personalTime(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "player"),
                                LongArgumentType.getLong(context, "ticks"),
                                true))));
        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> personalWeatherNode() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("pweather")
                .requires(source -> has(source, "commands.pweather")
                        || has(source, "commands.pweather.others"));
        for (String value : List.of("clear", "rain", "thunder", "reset")) {
            root.then(Commands.literal(value)
                    .executes(context -> personalWeather(
                            context.getSource(),
                            context.getSource().getPlayer(),
                            value,
                            false))
                    .then(IdentityArguments.online("player")
                            .requires(source -> has(source, "commands.pweather.others"))
                            .executes(context -> personalWeather(
                                    context.getSource(),
                                    IdentityArguments.getOnline(context, "player"),
                                    value,
                                    true))));
        }
        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> getPosNode() {
        return Commands.literal("getpos")
                .requires(source -> source.getPlayer() != null && has(source, "commands.getpos")
                        || has(source, "commands.getpos.others"))
                .executes(context -> getPos(context.getSource(), context.getSource().getPlayer()))
                .then(IdentityArguments.online("player")
                        .requires(source -> has(source, "commands.getpos.others"))
                        .executes(context -> getPos(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "player"))));
    }

    private static int afk(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        return execute(source, "sef:utility.afk", Map.of(), List.of(player.getUUID()), () -> {
            boolean afk = PlayerStateService.toggleAfk(player.getUUID());
            success(source, "AFK state " + (afk ? "enabled." : "disabled."));
            return 1;
        }, permission("commands.afk"));
    }

    private static int feed(CommandSourceStack source, ServerPlayer target, boolean other) {
        return targetMutation(source, target, "feed", other, () -> {
            target.getFoodData().setFoodLevel(20);
            target.getFoodData().setSaturation(0.0F);
        });
    }

    private static int heal(CommandSourceStack source, ServerPlayer target, boolean other) {
        return targetMutation(source, target, "heal", other, () -> {
            target.setHealth(target.getMaxHealth());
            target.getFoodData().setFoodLevel(20);
            target.getFoodData().setSaturation(20.0F);
            target.removeAllEffects();
            target.clearFire();
        });
    }

    private static int fly(CommandSourceStack source, ServerPlayer target, boolean other) {
        return targetMutation(source, target, "fly", other,
                () -> PlayerStateService.setFly(
                        target,
                        !PlayerStateService.fly(target.getUUID()),
                        source,
                        other));
    }

    private static int god(CommandSourceStack source, ServerPlayer target, boolean other) {
        return targetMutation(source, target, "god", other,
                () -> PlayerStateService.setGod(
                        target.getUUID(),
                        !PlayerStateService.god(target.getUUID()),
                        source,
                        other));
    }

    private static int rest(CommandSourceStack source, ServerPlayer target, boolean other) {
        return targetMutation(source, target, "rest", other,
                () -> target.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)));
    }

    private static int targetMutation(
            CommandSourceStack source,
            ServerPlayer target,
            String action,
            boolean other,
            Runnable mutation
    ) {
        if (other && !eligible(source, target)) {
            return unavailable(source);
        }
        String permission = "commands." + action + (other ? ".others" : "");
        return execute(source, "sef:utility." + action,
                Map.of("target", target.getUUID().toString(), "other", Boolean.toString(other)),
                List.of(target.getUUID()), () -> {
                    mutation.run();
                    success(source, action + " updated for " + target.getGameProfile().getName() + ".");
                    return 1;
                }, permission(permission));
    }

    private static int speed(
            CommandSourceStack source,
            double value,
            ServerPlayer explicit,
            boolean other
    ) {
        ServerPlayer target = explicit == null ? source.getPlayer() : explicit;
        if (target == null) {
            return fail(source, "An explicit online target is required.");
        }
        if (other && !eligible(source, target)) {
            return unavailable(source);
        }
        boolean flying = target.getAbilities().flying;
        double maximum = flying
                ? ConfigHandler.config.maximumFlySpeed.get()
                : ConfigHandler.config.maximumWalkSpeed.get();
        if (value > maximum) {
            return fail(source, "Speed exceeds the configured " + (flying ? "flight" : "walk") + " limit.");
        }
        String permission = "commands.speed" + (other ? ".others" : "");
        return execute(source, "sef:utility.speed", Map.of(
                "target", target.getUUID().toString(),
                "value", Double.toString(value),
                "flight", Boolean.toString(flying)), List.of(target.getUUID()), () -> {
            if (flying) {
                target.getAbilities().setFlyingSpeed((float) (0.05D * value));
            } else {
                target.getAbilities().setWalkingSpeed((float) (0.1D * value));
            }
            target.onUpdateAbilities();
            success(source, "Speed updated for " + target.getGameProfile().getName() + ".");
            return 1;
        }, permission(permission));
    }

    private static int expStatus(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return fail(source, "An explicit online target is required.");
        }
        return execute(source, "sef:utility.exp", Map.of("operation", "status"),
                List.of(player.getUUID()), () -> {
                    info(source, "Experience level " + player.experienceLevel
                            + ", total points " + player.totalExperience + ".");
                    return 1;
                }, permission("commands.exp"));
    }

    private static int exp(
            CommandSourceStack source,
            ServerPlayer target,
            String operation,
            int levels,
            boolean other
    ) {
        if (target == null || other && !eligible(source, target)) {
            return unavailable(source);
        }
        String permission = "commands.exp" + (other ? ".others" : "");
        return execute(source, "sef:utility.exp", Map.of(
                "operation", operation,
                "levels", Integer.toString(levels),
                "target", target.getUUID().toString()), List.of(target.getUUID()), () -> {
            switch (operation) {
                case "set" -> {
                    target.setExperienceLevels(levels);
                    target.setExperiencePoints(0);
                }
                case "add" -> target.giveExperienceLevels(levels);
                case "take" -> target.setExperienceLevels(Math.max(0, target.experienceLevel - levels));
                default -> {
                    return 0;
                }
            }
            success(source, "Experience updated for " + target.getGameProfile().getName() + ".");
            return 1;
        }, permission(permission));
    }

    private static int personalTime(
            CommandSourceStack source,
            ServerPlayer target,
            Long dayTime,
            boolean other
    ) {
        if (target == null || other && !eligible(source, target)) {
            return unavailable(source);
        }
        String permission = "commands.ptime" + (other ? ".others" : "");
        return execute(source, "sef:utility.ptime",
                Map.of("target", target.getUUID().toString(), "value", String.valueOf(dayTime)),
                List.of(target.getUUID()), () -> {
                    PlayerStateService.setPersonalTime(target, dayTime, source, other);
                    success(source, "Personal time updated.");
                    return 1;
                }, permission(permission));
    }

    private static int personalWeather(
            CommandSourceStack source,
            ServerPlayer target,
            String weather,
            boolean other
    ) {
        if (target == null || other && !eligible(source, target)) {
            return unavailable(source);
        }
        PlayerStateService.PersonalWeather parsed = switch (weather.toLowerCase(Locale.ROOT)) {
            case "clear" -> PlayerStateService.PersonalWeather.CLEAR;
            case "rain" -> PlayerStateService.PersonalWeather.RAIN;
            case "thunder" -> PlayerStateService.PersonalWeather.THUNDER;
            default -> PlayerStateService.PersonalWeather.RESET;
        };
        String permission = "commands.pweather" + (other ? ".others" : "");
        return execute(source, "sef:utility.pweather",
                Map.of("target", target.getUUID().toString(), "weather", weather),
                List.of(target.getUUID()), () -> {
                    PlayerStateService.setPersonalWeather(target, parsed, source, other);
                    success(source, "Personal weather updated.");
                    return 1;
                }, permission(permission));
    }

    private static int suicide(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        return execute(source, "sef:utility.suicide", Map.of(), List.of(player.getUUID()), () -> {
            player.kill();
            return 1;
        }, permission("commands.suicide"));
    }

    private static int near(CommandSourceStack source, int radius) {
        ServerPlayer player = source.getPlayer();
        return execute(source, "sef:utility.near", Map.of("radius", Integer.toString(radius)),
                List.of(player.getUUID()), () -> {
                    double maximum = (double) radius * radius;
                    List<String> names = player.serverLevel().players().stream()
                            .filter(candidate -> candidate != player)
                            .filter(candidate -> !VanishUtil.isVanished(candidate, player))
                            .filter(candidate -> candidate.distanceToSqr(player) <= maximum)
                            .limit(100)
                            .map(candidate -> candidate.getGameProfile().getName())
                            .sorted()
                            .toList();
                    info(source, names.isEmpty() ? "No visible players are nearby."
                            : "Nearby players. " + String.join(", ", names));
                    return Math.max(1, names.size());
                }, permission("commands.near"));
    }

    private static int getPos(CommandSourceStack source, ServerPlayer target) {
        if (target == null) {
            return unavailable(source);
        }
        boolean other = source.getPlayer() != target;
        if (other && !eligible(source, target)) {
            return unavailable(source);
        }
        String permission = other ? "commands.getpos.others" : "commands.getpos";
        return execute(source, "sef:utility.getpos",
                Map.of("target", target.getUUID().toString()), List.of(target.getUUID()), () -> {
                    info(source, target.getGameProfile().getName() + ", "
                            + target.serverLevel().dimension().location() + ", "
                            + target.getBlockX() + ", " + target.getBlockY() + ", " + target.getBlockZ()
                            + ", yaw " + Math.round(target.getYRot()) + ", pitch "
                            + Math.round(target.getXRot()) + ".");
                    return 1;
                }, permission(permission));
    }

    private static int compass(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        return execute(source, "sef:utility.compass", Map.of(), List.of(player.getUUID()), () -> {
            float yaw = Math.floorMod(Math.round(player.getYRot()), 360);
            String direction = direction(yaw);
            info(source, "Facing " + direction + ", yaw " + yaw + " degrees.");
            return 1;
        }, permission("commands.compass"));
    }

    private static int depth(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        return execute(source, "sef:utility.depth", Map.of(), List.of(player.getUUID()), () -> {
            int sea = player.serverLevel().getSeaLevel();
            info(source, "Y " + player.getBlockY() + ", sea level " + sea + ", relative depth "
                    + (player.getBlockY() - sea) + ".");
            return 1;
        }, permission("commands.depth"));
    }

    private static int top(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        BlockPos position = player.blockPosition();
        int y = player.serverLevel().getHeight(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                position.getX(),
                position.getZ());
        return teleport(source, "top", new BlockPos(position.getX(), y, position.getZ()));
    }

    private static int bottom(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        BlockPos origin = player.blockPosition();
        BlockPos destination = null;
        for (int y = player.serverLevel().getMinBuildHeight() + 1; y < origin.getY(); y++) {
            BlockPos feet = new BlockPos(origin.getX(), y, origin.getZ());
            if (player.serverLevel().getBlockState(feet).getCollisionShape(
                    player.serverLevel(), feet).isEmpty()
                    && player.serverLevel().getBlockState(feet.above()).getCollisionShape(
                    player.serverLevel(), feet.above()).isEmpty()
                    && player.serverLevel().getBlockState(feet.below()).isFaceSturdy(
                    player.serverLevel(), feet.below(), net.minecraft.core.Direction.UP)) {
                destination = feet;
                break;
            }
        }
        if (destination == null) {
            return fail(source, "No safe loaded lower destination was found.");
        }
        return teleport(source, "bottom", destination);
    }

    private static int jump(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        HitResult hit = player.pick(128.0D, 0.0F, false);
        if (!(hit instanceof BlockHitResult block) || hit.getType() != HitResult.Type.BLOCK) {
            return fail(source, "No block is targeted within range.");
        }
        return teleport(source, "jump", block.getBlockPos().relative(block.getDirection()));
    }

    private static int teleport(CommandSourceStack source, String action, BlockPos destination) {
        ServerPlayer player = source.getPlayer();
        SavedLocation location = new SavedLocation(
                player.serverLevel().dimension().location().toString(),
                destination.getX() + 0.5D,
                destination.getY(),
                destination.getZ() + 0.5D,
                player.getYRot(),
                player.getXRot());
        return execute(source, "sef:utility." + action,
                Map.of("x", Integer.toString(destination.getX()),
                        "y", Integer.toString(destination.getY()),
                        "z", Integer.toString(destination.getZ())),
                List.of(player.getUUID()), () -> {
                    SafeTeleportService.TeleportResult result = KernelServices.safeTeleports().teleport(
                            source.getServer(),
                            player,
                            player,
                            location,
                            action,
                            new SafeTeleportService.Policy(4, 256, 16, false, false, false, true, 20),
                            () -> true);
                    if (!result.successful()) {
                        return fail(source, "Destination rejected. " + result.detail());
                    }
                    success(source, "Teleported to a safe " + action + " destination.");
                    return 1;
                }, permission("commands." + action));
    }

    private static boolean eligible(CommandSourceStack source, ServerPlayer target) {
        if (source.getPlayer() != null && VanishUtil.isVanished(target, source.getPlayer())) {
            return false;
        }
        return PlayerTargetPolicy.decide(
                source,
                target,
                permission("utilities.hierarchy.bypass"),
                permission("exempt.utility"),
                permission("utilities.bypass.exempt"),
                false,
                true).allowed();
    }

    private static String direction(float yaw) {
        String[] directions = {"south", "southwest", "west", "northwest",
                "north", "northeast", "east", "southeast"};
        int index = Math.floorMod(Math.round(yaw / 45.0F), directions.length);
        return directions[index];
    }

    @SafeVarargs
    private static int execute(
            CommandSourceStack source,
            String action,
            Map<String, String> parameters,
            List<UUID> targets,
            java.util.function.IntSupplier operation,
            PermissionNode<Boolean>... permissions
    ) {
        return KernelCommandExecutor.execute(source, action, parameters, targets, false, operation, permissions);
    }

    private static boolean has(CommandSourceStack source, String id) {
        PermissionNode<Boolean> node = permission(id);
        return node != null && PermissionService.has(source, node);
    }

    private static PermissionNode<Boolean> permission(String id) {
        return PermissionsHandler.phasePermission(id);
    }

    private static int unavailable(CommandSourceStack source) {
        return fail(source, "That player is unavailable.");
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

    @FunctionalInterface
    private interface TargetAction {
        int run(CommandSourceStack source, ServerPlayer target, boolean other);
    }
}
