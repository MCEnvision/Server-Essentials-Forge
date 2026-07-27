package com.enviouse.sef.recovery;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.control.MinecraftServerControlRuntime;
import com.enviouse.sef.control.ServerControlRepository;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.ConfirmationService;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.permissions.PermissionService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class RecoveryCommands {
    private RecoveryCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("inventoryhistory")
                .requires(source -> has(source, "commands.control.inventory_recovery.view"))
                .then(Commands.literal("inspect")
                        .then(Commands.argument("snapshot", StringArgumentType.word())
                                .executes(context -> inspect(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "snapshot")))))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> list(
                                context.getSource(),
                                EntityArgument.getPlayer(context, "player")))));
        dispatcher.register(Commands.literal("inventoryrestore")
                .requires(source -> has(source, "commands.control.inventory_recovery.manage"))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("snapshot", StringArgumentType.word())
                                .then(Commands.argument("revision", LongArgumentType.longArg(1L))
                                        .executes(context -> restore(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "player"),
                                                StringArgumentType.getString(context, "snapshot"),
                                                LongArgumentType.getLong(context, "revision"),
                                                null))
                                        .then(Commands.literal("confirm")
                                                .then(Commands.argument("token", StringArgumentType.word())
                                                        .executes(context -> restore(
                                                                context.getSource(),
                                                                EntityArgument.getPlayer(context, "player"),
                                                                StringArgumentType.getString(context, "snapshot"),
                                                                LongArgumentType.getLong(context, "revision"),
                                                                StringArgumentType.getString(context, "token")))))))));
    }

    private static int list(CommandSourceStack source, ServerPlayer target) {
        if (!eligible(source, target)) {
            return fail(source, "That player is unavailable.");
        }
        List<InventoryRecoveryRepository.InventorySnapshot> snapshots =
                KernelServices.inventoryRecovery().snapshots(target.getUUID());
        info(source, target.getGameProfile().getName() + " has "
                + snapshots.size() + " retained inventory snapshots.");
        snapshots.stream().limit(20).forEach(snapshot -> info(
                source,
                snapshot.id() + ", revision " + snapshot.revision() + ", "
                        + snapshot.reason() + ", " + snapshot.createdAt() + "."));
        return 1;
    }

    private static int inspect(CommandSourceStack source, String snapshotInput) {
        UUID snapshotId = uuid(snapshotInput);
        InventoryRecoveryRepository.InventorySnapshot snapshot = snapshotId == null
                ? null
                : KernelServices.inventoryRecovery().find(snapshotId).orElse(null);
        ServerPlayer target = snapshot == null
                ? null
                : source.getServer().getPlayerList().getPlayer(snapshot.playerId());
        if (snapshot == null || target == null || !eligible(source, target)) {
            return fail(source, "Inventory snapshot is unavailable.");
        }
        info(source, "Inventory snapshot " + snapshot.id() + ", revision "
                + snapshot.revision() + ".");
        info(source, "Owner " + target.getGameProfile().getName() + ", reason "
                + snapshot.reason() + ", created " + snapshot.createdAt()
                + ", expires " + snapshot.expiresAt() + ".");
        info(source, "Inventory stacks " + snapshot.inventory().size()
                + ", ender chest stacks " + snapshot.enderChest().size() + ".");
        return 1;
    }

    private static int restore(
            CommandSourceStack source,
            ServerPlayer target,
            String snapshotInput,
            long expectedRevision,
            String token
    ) {
        UUID snapshotId = uuid(snapshotInput);
        InventoryRecoveryRepository.InventorySnapshot snapshot = snapshotId == null
                ? null
                : KernelServices.inventoryRecovery().find(snapshotId).orElse(null);
        ServerControlRepository.ControlRecord policy =
                MinecraftServerControlRuntime.effectivePolicy("inventory_recovery").orElse(null);
        if (snapshot == null
                || policy == null
                || !snapshot.playerId().equals(target.getUUID())
                || !eligible(source, target)) {
            return fail(source, "Inventory snapshot is unavailable.");
        }
        ConfirmationService.Request request = new ConfirmationService.Request(
                actorId(source),
                "sef:control.inventory_recovery.manage",
                Map.of(
                        "snapshot", snapshot.id().toString(),
                        "revision", Long.toString(expectedRevision)),
                List.of(target.getUUID()),
                "",
                0L,
                0L,
                0L,
                KernelServices.commandPolicies().revision());
        if (token == null) {
            ActionResult<ConfirmationService.IssuedToken> issued =
                    KernelServices.confirmations().issue(request, Duration.ofSeconds(60));
            if (!issued.successful()) {
                return fail(source, "Inventory restore confirmation could not be issued.");
            }
            info(source, "Restore preview, " + snapshot.inventory().size()
                    + " inventory stacks and " + snapshot.enderChest().size()
                    + " ender chest stacks.");
            info(source, "Confirm with /inventoryrestore "
                    + target.getGameProfile().getName() + " " + snapshot.id() + " "
                    + expectedRevision + " confirm " + issued.value().token() + ".");
            return 1;
        }
        ActionResult<ConfirmationService.Request> consumed =
                KernelServices.confirmations().consume(token, request);
        if (!consumed.successful()) {
            return fail(source, "Inventory restore confirmation is invalid, stale, expired, or used.");
        }
        int maximum = integer(policy, "maximum_snapshots", 16, 1, 128);
        long retention = number(policy, "retention_seconds", 604_800L, 60L, 31_536_000L);
        return KernelCommandExecutor.execute(
                source,
                "sef:control.inventory_recovery.manage",
                Map.of(
                        "target", target.getUUID().toString(),
                        "snapshot", snapshot.id().toString(),
                        "revision", Long.toString(expectedRevision)),
                () -> {
                    ActionResult<InventoryRecoveryRepository.InventorySnapshot> result =
                            KernelServices.inventoryRecovery().restore(
                                    target,
                                    snapshot.id(),
                                    expectedRevision,
                                    maximum,
                                    retention);
                    if (!result.successful()) {
                        return fail(source, result.detail());
                    }
                    success(source, "Inventory restored. A pre-restore backup was retained.");
                    return 1;
                });
    }

    private static boolean eligible(CommandSourceStack source, ServerPlayer target) {
        return PlayerTargetPolicy.decide(
                source,
                target,
                PermissionsHandler.phasePermission(
                        "commands.control.inventory_recovery.hierarchy.override"),
                PermissionsHandler.phasePermission(
                        "commands.control.inventory_recovery.exempt"),
                PermissionsHandler.phasePermission(
                        "commands.control.inventory_recovery.exemption.override"),
                false,
                true).allowed();
    }

    private static boolean has(CommandSourceStack source, String permission) {
        var node = PermissionsHandler.phasePermission(permission);
        return node != null && PermissionService.has(source, node);
    }

    private static UUID uuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static UUID actorId(CommandSourceStack source) {
        return source.getPlayer() == null ? new UUID(0L, 0L) : source.getPlayer().getUUID();
    }

    private static int integer(
            ServerControlRepository.ControlRecord record,
            String field,
            int fallback,
            int minimum,
            int maximum
    ) {
        return (int) number(record, field, fallback, minimum, maximum);
    }

    private static long number(
            ServerControlRepository.ControlRecord record,
            String field,
            long fallback,
            long minimum,
            long maximum
    ) {
        try {
            return Math.clamp(
                    Long.parseLong(record.metadata().getOrDefault(
                            "field." + field,
                            Long.toString(fallback))),
                    minimum,
                    maximum);
        } catch (NumberFormatException exception) {
            return fallback;
        }
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
