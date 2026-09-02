package com.enviouse.sef.recovery;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.permissions.PermissionService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class GraveCommands {
    private GraveCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("grave")
                .requires(source -> source.getPlayer() != null
                        && (has(source, "commands.control.graves.view")
                        || has(source, "commands.control.graves.claim")
                        || has(source, "commands.control.graves.manage")))
                .executes(context -> latest(context.getSource()))
                .then(Commands.literal("locate")
                        .requires(source -> has(source, "commands.control.graves.view")
                                || has(source, "commands.control.graves.manage"))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(context -> locate(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "id")))))
                .then(Commands.literal("claim")
                        .requires(source -> has(source, "commands.control.graves.claim")
                                || has(source, "commands.control.graves.manage"))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("revision", LongArgumentType.longArg(1L))
                                        .executes(context -> claim(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "id"),
                                                LongArgumentType.getLong(context, "revision"))))))
                .then(Commands.literal("unlock")
                        .requires(source -> has(source, "commands.control.graves.manage"))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("revision", LongArgumentType.longArg(1L))
                                        .executes(context -> unlock(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "id"),
                                                LongArgumentType.getLong(context, "revision")))))));
        dispatcher.register(Commands.literal("graves")
                .requires(source -> source.getPlayer() != null
                        && (has(source, "commands.control.graves.view")
                        || has(source, "commands.control.graves.create")
                        || has(source, "commands.control.graves.manage")))
                .executes(context -> list(context.getSource(), context.getSource().getPlayer(), false))
                .then(Commands.argument("player", EntityArgument.player())
                        .requires(source -> has(source, "commands.control.graves.manage"))
                        .executes(context -> list(
                                context.getSource(),
                                EntityArgument.getPlayer(context, "player"),
                                true))));
    }

    private static int latest(CommandSourceStack source) {
        GraveRepository.GraveRecord grave = KernelServices.graves()
                .latestActive(source.getPlayer().getUUID())
                .orElse(null);
        if (grave == null) {
            return fail(source, "You have no active graves.");
        }
        describe(source, grave);
        return 1;
    }

    private static int list(CommandSourceStack source, ServerPlayer owner, boolean others) {
        if (owner == null
                || others && (!has(source, "commands.control.graves.manage") || !eligible(source, owner))) {
            return fail(source, "That player is unavailable.");
        }
        List<GraveRepository.GraveRecord> graves = KernelServices.graves().graves(owner.getUUID(), others);
        info(source, owner.getGameProfile().getName() + " has " + graves.size()
                + (others ? " retained" : " active") + " graves.");
        graves.stream().limit(20).forEach(grave -> info(
                source,
                grave.id() + ", revision " + grave.revision() + ", "
                        + grave.dimensionId() + ", " + block(grave.x()) + " "
                        + block(grave.y()) + " " + block(grave.z()) + ", "
                        + (grave.claimed() ? "claimed" : grave.expiresAt().isAfter(java.time.Instant.now())
                        ? "active"
                        : "expired") + "."));
        return 1;
    }

    private static int locate(CommandSourceStack source, String input) {
        GraveRepository.GraveRecord grave = find(source, input);
        if (grave == null) {
            return 0;
        }
        return KernelCommandExecutor.execute(
                source,
                "sef:control.graves.locate",
                Map.of("grave", grave.id().toString()),
                List.of(grave.ownerId()),
                false,
                () -> {
                    describe(source, grave);
                    return 1;
                });
    }

    private static int claim(CommandSourceStack source, String input, long expectedRevision) {
        GraveRepository.GraveRecord grave = find(source, input);
        ServerPlayer claimant = source.getPlayer();
        if (grave == null || claimant == null) {
            return 0;
        }
        boolean owner = grave.ownerId().equals(claimant.getUUID());
        if (!owner && !has(source, "commands.control.graves.manage")) {
            return fail(source, "That grave is unavailable.");
        }
        return KernelCommandExecutor.execute(
                source,
                owner ? "sef:control.graves.claim" : "sef:control.graves.manage",
                Map.of(
                        "grave", grave.id().toString(),
                        "revision", Long.toString(expectedRevision),
                        "owner", Boolean.toString(owner)),
                List.of(grave.ownerId()),
                false,
                () -> {
                    ActionResult<GraveRepository.GraveRecord> result =
                            KernelServices.graves().claimAndFlush(
                                    claimant,
                                    grave.id(),
                                    expectedRevision,
                                    !owner);
                    if (!result.successful()) {
                        return fail(source, result.detail());
                    }
                    KernelServices.graves().cleanupExpiredContainers(source.getServer(), 512);
                    success(source, "Grave claimed.");
                    return 1;
                });
    }

    private static int unlock(CommandSourceStack source, String input, long expectedRevision) {
        GraveRepository.GraveRecord grave = find(source, input);
        if (grave == null) {
            return 0;
        }
        return KernelCommandExecutor.execute(
                source,
                "sef:control.graves.unlock",
                Map.of(
                        "grave", grave.id().toString(),
                        "revision", Long.toString(expectedRevision)),
                List.of(grave.ownerId()),
                false,
                () -> {
                    ActionResult<GraveRepository.GraveRecord> result =
                            KernelServices.graves().unlockAndFlush(grave.id(), expectedRevision);
                    if (!result.successful()) {
                        return fail(source, result.detail());
                    }
                    success(source, "Grave owner protection was removed.");
                    return 1;
                });
    }

    private static GraveRepository.GraveRecord find(CommandSourceStack source, String input) {
        UUID graveId;
        try {
            graveId = UUID.fromString(input);
        } catch (IllegalArgumentException exception) {
            fail(source, "Grave id is invalid.");
            return null;
        }
        GraveRepository.GraveRecord grave = KernelServices.graves().active(graveId).orElse(null);
        ServerPlayer viewer = source.getPlayer();
        if (grave == null || viewer == null) {
            fail(source, "Grave is unavailable.");
            return null;
        }
        if (grave.ownerId().equals(viewer.getUUID())) {
            return grave;
        }
        ServerPlayer owner = source.getServer().getPlayerList().getPlayer(grave.ownerId());
        if (!has(source, "commands.control.graves.manage")
                || owner == null
                || !eligible(source, owner)) {
            fail(source, "Grave is unavailable.");
            return null;
        }
        return grave;
    }

    private static void describe(CommandSourceStack source, GraveRepository.GraveRecord grave) {
        info(source, "Grave " + grave.id() + ", revision " + grave.revision() + ".");
        info(source, grave.dimensionId() + ", " + block(grave.x()) + " "
                + block(grave.y()) + " " + block(grave.z()) + ", "
                + grave.items().size() + " stacks, " + grave.experience()
                + " experience, expires " + grave.expiresAt() + ".");
    }

    private static boolean eligible(CommandSourceStack source, ServerPlayer target) {
        return PlayerTargetPolicy.decide(
                source,
                target,
                PermissionsHandler.phasePermission("commands.control.graves.hierarchy.override"),
                PermissionsHandler.phasePermission("commands.control.graves.exempt"),
                PermissionsHandler.phasePermission("commands.control.graves.exemption.override"),
                false,
                true).allowed();
    }

    private static boolean has(CommandSourceStack source, String permission) {
        var node = PermissionsHandler.phasePermission(permission);
        return node != null && PermissionService.has(source, node);
    }

    private static int block(double coordinate) {
        return net.minecraft.util.Mth.floor(coordinate);
    }

    private static int fail(CommandSourceStack source, String message) {
        source.sendFailure(TextFormatter.stringToFormattedText("&c" + message));
        return 0;
    }

    private static void info(CommandSourceStack source, String message) {
        source.sendSuccess(() -> TextFormatter.stringToFormattedText("&7" + message), false);
    }

    private static void success(CommandSourceStack source, String message) {
        source.sendSuccess(() -> TextFormatter.stringToFormattedText("&a" + message), false);
    }
}
