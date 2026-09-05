package com.enviouse.sef.teleport;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.identity.IdentityArguments;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.control.CommunityCommands;
import com.enviouse.sef.vanish.VanishUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;

public final class TeleportRequestCommands {
    private TeleportRequestCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (!ConfigHandler.config.enableTeleportEssentials.get()
                || !ConfigHandler.config.enableTeleportRequests.get()) {
            return;
        }
        dispatcher.register(requestNode("tpa", TeleportRequestService.Type.TO_TARGET));
        dispatcher.register(requestNode("tpahere", TeleportRequestService.Type.TARGET_TO_SENDER));
        dispatcher.register(acceptNode());
        dispatcher.register(denyNode());
        dispatcher.register(cancelNode());
        dispatcher.register(listNode());
        dispatcher.register(toggleNode());
        dispatcher.register(blockNode("tpblock", true));
        dispatcher.register(blockNode("tpunblock", false));
        dispatcher.register(blockedNode());
        dispatcher.register(autoAcceptNode());
        dispatcher.register(requestAllNode());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> requestNode(
            String literal,
            TeleportRequestService.Type type
    ) {
        return Commands.literal(literal)
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        type == TeleportRequestService.Type.TO_TARGET
                                ? PermissionsHandler.tpaCommand
                                : PermissionsHandler.tpaHereCommand,
                        type == TeleportRequestService.Type.TO_TARGET
                                ? "sef:teleport.request.to"
                                : "sef:teleport.request.here"))
                .then(IdentityArguments.online("player")
                        .executes(context -> create(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "player"),
                                type,
                                type == TeleportRequestService.Type.TO_TARGET
                                        ? "sef:teleport.request.to"
                                        : "sef:teleport.request.here",
                                type == TeleportRequestService.Type.TO_TARGET
                                        ? PermissionsHandler.tpaCommand
                                        : PermissionsHandler.tpaHereCommand)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> acceptNode() {
        return Commands.literal("tpaccept")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.tpAcceptCommand,
                        "sef:teleport.request.accept"))
                .executes(context -> accept(context.getSource(), null))
                .then(IdentityArguments.online("player")
                        .executes(context -> accept(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "player").getUUID())));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> denyNode() {
        return Commands.literal("tpdeny")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.tpDenyCommand,
                        "sef:teleport.request.deny"))
                .executes(context -> deny(context.getSource(), null))
                .then(IdentityArguments.online("player")
                        .executes(context -> deny(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "player").getUUID())));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> cancelNode() {
        return Commands.literal("tpcancel")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.tpCancelCommand,
                        "sef:teleport.request.cancel"))
                .executes(context -> cancel(context.getSource(), null))
                .then(IdentityArguments.online("player")
                        .executes(context -> cancel(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "player").getUUID())));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> listNode() {
        return Commands.literal("tprequests")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.tpRequestsCommand,
                        "sef:teleport.request.list"))
                .executes(context -> list(context.getSource()));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> toggleNode() {
        return Commands.literal("tptoggle")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.tpToggleCommand,
                        "sef:teleport.request.toggle"))
                .executes(context -> {
                    ServerPlayer player = TeleportCommandSupport.player(
                            context.getSource(),
                            "sef:teleport.request.toggle");
                    if (player == null) {
                        return 0;
                    }
                    return KernelCommandExecutor.execute(
                            context.getSource(),
                            "sef:teleport.request.toggle",
                            java.util.Map.of("operation", "toggle"),
                            () -> {
                                boolean enabled = !KernelServices.teleports()
                                        .preference(player.getUUID()).tpaEnabled();
                                KernelServices.teleports().setTpaEnabled(player.getUUID(), enabled);
                                TeleportCommandSupport.success(
                                        context.getSource(),
                                        "Incoming teleport requests are now "
                                                + (enabled ? "enabled." : "disabled."));
                                return 1;
                            },
                            PermissionsHandler.tpToggleCommand);
                });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> blockNode(String literal, boolean blocked) {
        return Commands.literal(literal)
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.tpBlockCommand,
                        "sef:teleport.request.block"))
                .then(IdentityArguments.online("player")
                        .executes(context -> {
                            ServerPlayer player = TeleportCommandSupport.player(context.getSource());
                            ServerPlayer target = IdentityArguments.getOnline(context, "player");
                            if (player == null || player.getUUID().equals(target.getUUID())) {
                                TeleportCommandSupport.fail(context.getSource(), "You cannot block yourself.");
                                return 0;
                            }
                            return KernelCommandExecutor.execute(
                                    context.getSource(),
                                    "sef:teleport.request.block",
                                    java.util.Map.of(
                                            "operation", blocked ? "block" : "unblock",
                                            "target", target.getUUID().toString()),
                                    java.util.List.of(target.getUUID()),
                                    false,
                                    () -> {
                                        KernelServices.teleports().setBlocked(
                                                player.getUUID(),
                                                target.getUUID(),
                                                blocked);
                                        TeleportCommandSupport.success(
                                                context.getSource(),
                                                (blocked ? "Blocked " : "Unblocked ")
                                                        + target.getGameProfile().getName()
                                                        + " for teleport requests.");
                                        return 1;
                                    },
                                    PermissionsHandler.tpBlockCommand);
                        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> blockedNode() {
        return Commands.literal("tpblocked")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.tpBlockCommand,
                        "sef:teleport.request.block"))
                .executes(context -> {
                    ServerPlayer player = TeleportCommandSupport.player(
                            context.getSource(),
                            "sef:teleport.request.block");
                    if (player == null) {
                        return 0;
                    }
                    var blocked = KernelServices.teleports().preference(player.getUUID()).blockedPlayers();
                    if (blocked.isEmpty()) {
                        TeleportCommandSupport.info(context.getSource(), "Your teleport block list is empty.");
                        return 0;
                    }
                    TeleportCommandSupport.info(context.getSource(), "Blocked player UUIDs.");
                    blocked.stream().sorted().forEach(id ->
                            TeleportCommandSupport.info(context.getSource(), id.toString()));
                    return blocked.size();
                });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> autoAcceptNode() {
        return Commands.literal("tpautoaccept")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.tpAutoAcceptCommand,
                        "sef:teleport.request.auto"))
                .executes(context -> {
                    ServerPlayer player = TeleportCommandSupport.player(
                            context.getSource(),
                            "sef:teleport.request.auto");
                    if (player == null) {
                        return 0;
                    }
                    return KernelCommandExecutor.execute(
                            context.getSource(),
                            "sef:teleport.request.auto",
                            java.util.Map.of("operation", "toggle"),
                            () -> {
                                boolean enabled = !KernelServices.teleports()
                                        .preference(player.getUUID()).autoAccept();
                                KernelServices.teleports().setAutoAccept(player.getUUID(), enabled);
                                TeleportCommandSupport.success(
                                        context.getSource(),
                                        "Teleport request auto accept is now "
                                                + (enabled ? "enabled." : "disabled."));
                                return 1;
                            },
                            PermissionsHandler.tpAutoAcceptCommand);
                });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> requestAllNode() {
        return Commands.literal("tpaall")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.teleportAskAllCommand,
                        "sef:teleport.request.all"))
                .executes(context -> {
                    ServerPlayer actor = TeleportCommandSupport.player(context.getSource());
                    if (actor == null) {
                        return 0;
                    }
                    List<ServerPlayer> targets = context.getSource().getServer().getPlayerList().getPlayers().stream()
                            .filter(target -> target != actor)
                            .filter(target -> !VanishUtil.isVanished(target, actor))
                            .limit(100)
                            .toList();
                    return KernelCommandExecutor.execute(
                            context.getSource(),
                            "sef:teleport.request.all",
                            java.util.Map.of(
                                    "operation", "create_all",
                                    "target_count", Integer.toString(targets.size())),
                            targets.stream().map(ServerPlayer::getUUID).toList(),
                            false,
                            () -> {
                                int created = 0;
                                for (ServerPlayer target : targets) {
                                    if (createInternal(
                                            context.getSource(),
                                            actor,
                                            target,
                                            TeleportRequestService.Type.TO_TARGET) > 0) {
                                        created++;
                                    }
                                }
                                TeleportCommandSupport.info(
                                        context.getSource(),
                                        "Created " + created + " teleport requests.");
                                return Math.max(1, created);
                            },
                            PermissionsHandler.teleportAskAllCommand);
                });
    }

    private static int create(
            CommandSourceStack source,
            ServerPlayer target,
            TeleportRequestService.Type type,
            String actionId,
            net.neoforged.neoforge.server.permission.nodes.PermissionNode<Boolean> permission
    ) {
        ServerPlayer sender = TeleportCommandSupport.player(source, actionId);
        if (sender == null) {
            return 0;
        }
        if (VanishUtil.isVanished(target, sender)) {
            TeleportCommandSupport.fail(source, "That player is unavailable.");
            return 0;
        }
        return KernelCommandExecutor.execute(
                source,
                actionId,
                java.util.Map.of(
                        "operation", "create",
                        "type", type.name().toLowerCase(java.util.Locale.ROOT)),
                java.util.List.of(target.getUUID()),
                false,
                () -> createInternal(source, sender, target, type),
                permission);
    }

    private static int createInternal(
            CommandSourceStack source,
            ServerPlayer sender,
            ServerPlayer target,
            TeleportRequestService.Type type
    ) {
        TeleportRepository.TeleportPreference preference =
                KernelServices.teleports().preference(target.getUUID());
        if (!preference.tpaEnabled()) {
            TeleportCommandSupport.fail(source, "That player has disabled teleport requests.");
            return 0;
        }
        if (preference.blockedPlayers().contains(sender.getUUID())) {
            TeleportCommandSupport.fail(source, "That player is not accepting your teleport requests.");
            return 0;
        }
        if (CommunityCommands.interactionBlocked(
                target.getUUID(),
                sender.getUUID(),
                "teleports")) {
            TeleportCommandSupport.fail(source, "That player is not accepting your teleport requests.");
            return 0;
        }
        ActionResult<TeleportRequestService.Request> result = KernelServices.teleportRequests().create(
                type,
                sender.getUUID(),
                target.getUUID(),
                SavedLocation.from(sender),
                KernelServices.teleportSettings().requestLifetime(),
                KernelServices.teleportSettings().maximumPendingRequests());
        if (!result.successful()) {
            TeleportCommandSupport.fail(source, result.detail());
            return 0;
        }
        TeleportRequestService.Request request = result.value();
        String verb = type == TeleportRequestService.Type.TO_TARGET
                ? "teleport to you"
                : "have you teleport to them";
        target.sendSystemMessage(TextFormatter.stringToFormattedText(
                "&e" + sender.getGameProfile().getName() + " requested to " + verb
                        + ". &7Use /tpaccept " + sender.getGameProfile().getName()
                        + " or /tpdeny " + sender.getGameProfile().getName() + "."));
        TeleportCommandSupport.success(source, "Sent a teleport request to " + target.getGameProfile().getName() + ".");
        if (preference.autoAccept()) {
            ActionResult<TeleportRequestService.Request> accepted =
                    KernelServices.teleportRequests().accept(target.getUUID(), sender.getUUID());
            if (accepted.successful()) {
                return complete(source, accepted.value(), target);
            }
        }
        return 1;
    }

    private static int accept(CommandSourceStack source, UUID senderId) {
        ServerPlayer target = TeleportCommandSupport.player(source, "sef:teleport.request.accept");
        if (target == null) {
            return 0;
        }
        return acceptInternal(source, target, senderId);
    }

    private static int acceptInternal(CommandSourceStack source, ServerPlayer target, UUID senderId) {
        ActionResult<TeleportRequestService.Request> result =
                KernelServices.teleportRequests().accept(target.getUUID(), senderId);
        if (!result.successful()) {
            TeleportCommandSupport.fail(source, result.detail());
            return 0;
        }
        return complete(source, result.value(), target);
    }

    private static int complete(
            CommandSourceStack source,
            TeleportRequestService.Request request,
            ServerPlayer acceptingTarget
    ) {
        ServerPlayer sender = source.getServer().getPlayerList().getPlayer(request.senderId());
        ServerPlayer target = source.getServer().getPlayerList().getPlayer(request.targetId());
        if (sender == null || target == null || acceptingTarget != target) {
            KernelServices.teleportRequests().invalidate(request.id(), TeleportRequestService.State.INVALIDATED);
            TeleportCommandSupport.fail(source, "A request participant is no longer online.");
            return 0;
        }
        TeleportRepository.TeleportPreference preference =
                KernelServices.teleports().preference(target.getUUID());
        if (!preference.tpaEnabled()
                || preference.blockedPlayers().contains(sender.getUUID())
                || CommunityCommands.interactionBlocked(
                        target.getUUID(),
                        sender.getUUID(),
                        "teleports")) {
            KernelServices.teleportRequests().invalidate(request.id(), TeleportRequestService.State.REJECTED);
            TeleportCommandSupport.fail(source, "The request is no longer allowed.");
            return 0;
        }

        ServerPlayer moving = request.type() == TeleportRequestService.Type.TO_TARGET ? sender : target;
        ServerPlayer destinationPlayer = request.type() == TeleportRequestService.Type.TO_TARGET ? target : sender;
        SavedLocation destination = SavedLocation.from(destinationPlayer);
        var movingPermission = request.type() == TeleportRequestService.Type.TO_TARGET
                ? PermissionsHandler.tpaCommand
                : PermissionsHandler.tpAcceptCommand;
        int result = TeleportCommandSupport.teleport(
                source,
                moving,
                moving,
                destination,
                "sef:teleport.request.accept",
                "teleport_request",
                movingPermission,
                KernelServices.teleportSettings().userPolicy(),
                () -> KernelServices.teleportRequests().request(request.id())
                        .map(current -> current.senderId().equals(request.senderId())
                                && current.targetId().equals(request.targetId())
                                && current.type() == request.type()
                                && (current.state() == TeleportRequestService.State.ACCEPTED
                                || current.state() == TeleportRequestService.State.WARMUP))
                        .orElse(false)
                        && TeleportCommandSupport.has(target, PermissionsHandler.tpAcceptCommand)
                        && source.getServer().getPlayerList().getPlayer(destinationPlayer.getUUID()) != null,
                asynchronousResult -> finishRequest(
                        source.getServer(),
                        request,
                        asynchronousResult));
        if (result == TeleportCommandSupport.ASYNC_PENDING) {
            ActionResult<TeleportRequestService.Request> warming =
                    KernelServices.teleportRequests().markWarmup(request.id());
            if (!warming.successful()) {
                KernelServices.warmups().clear(moving.getUUID());
                KernelServices.teleportRequests().invalidate(
                        request.id(),
                        TeleportRequestService.State.FAILED);
                TeleportCommandSupport.fail(source, "The teleport request warmup could not be retained.");
            }
            return 0;
        }
        return finishRequest(source.getServer(), request, result);
    }

    private static int finishRequest(
            net.minecraft.server.MinecraftServer server,
            TeleportRequestService.Request request,
            int result
    ) {
        KernelServices.teleportRequests().invalidate(
                request.id(),
                result > 0 ? TeleportRequestService.State.COMPLETED : TeleportRequestService.State.FAILED);
        if (result > 0) {
            ServerPlayer sender = server.getPlayerList().getPlayer(request.senderId());
            ServerPlayer target = server.getPlayerList().getPlayer(request.targetId());
            if (sender != null) {
                sender.sendSystemMessage(TextFormatter.stringToFormattedText("&aTeleport request completed."));
            }
            if (target != null) {
                target.sendSystemMessage(TextFormatter.stringToFormattedText("&aTeleport request completed."));
            }
        }
        return result;
    }

    private static int deny(CommandSourceStack source, UUID senderId) {
        ServerPlayer target = TeleportCommandSupport.player(source, "sef:teleport.request.deny");
        if (target == null) {
            return 0;
        }
        return KernelCommandExecutor.execute(
                source,
                "sef:teleport.request.deny",
                java.util.Map.of("operation", "deny"),
                senderId == null ? java.util.List.of() : java.util.List.of(senderId),
                false,
                () -> denyInternal(source, target, senderId),
                PermissionsHandler.tpDenyCommand);
    }

    private static int denyInternal(CommandSourceStack source, ServerPlayer target, UUID senderId) {
        ActionResult<TeleportRequestService.Request> result =
                KernelServices.teleportRequests().deny(target.getUUID(), senderId);
        if (!result.successful()) {
            TeleportCommandSupport.fail(source, result.detail());
            return 0;
        }
        notifySender(source, result.value(), "Your teleport request was denied.");
        TeleportCommandSupport.success(source, "Denied the teleport request.");
        return 1;
    }

    private static int cancel(CommandSourceStack source, UUID targetId) {
        ServerPlayer sender = TeleportCommandSupport.player(source, "sef:teleport.request.cancel");
        if (sender == null) {
            return 0;
        }
        return KernelCommandExecutor.execute(
                source,
                "sef:teleport.request.cancel",
                java.util.Map.of("operation", "cancel"),
                targetId == null ? java.util.List.of() : java.util.List.of(targetId),
                false,
                () -> cancelInternal(source, sender, targetId),
                PermissionsHandler.tpCancelCommand);
    }

    private static int cancelInternal(CommandSourceStack source, ServerPlayer sender, UUID targetId) {
        ActionResult<TeleportRequestService.Request> result =
                KernelServices.teleportRequests().cancel(sender.getUUID(), targetId);
        if (!result.successful()) {
            TeleportCommandSupport.fail(source, result.detail());
            return 0;
        }
        ServerPlayer target = source.getServer().getPlayerList().getPlayer(result.value().targetId());
        if (target != null) {
            target.sendSystemMessage(TextFormatter.stringToFormattedText("&eA teleport request was cancelled."));
        }
        TeleportCommandSupport.success(source, "Cancelled the teleport request.");
        return 1;
    }

    private static int list(CommandSourceStack source) {
        ServerPlayer player = TeleportCommandSupport.player(source, "sef:teleport.request.list");
        if (player == null) {
            return 0;
        }
        return KernelCommandExecutor.execute(
                source,
                "sef:teleport.request.list",
                java.util.Map.of("operation", "list"),
                () -> listInternal(source, player),
                PermissionsHandler.tpRequestsCommand);
    }

    private static int listInternal(CommandSourceStack source, ServerPlayer player) {
        List<TeleportRequestService.Request> incoming =
                KernelServices.teleportRequests().incoming(player.getUUID());
        List<TeleportRequestService.Request> outgoing =
                KernelServices.teleportRequests().outgoing(player.getUUID());
        if (incoming.isEmpty() && outgoing.isEmpty()) {
            TeleportCommandSupport.info(source, "You have no pending teleport requests.");
            return 1;
        }
        TeleportCommandSupport.info(source, "Incoming requests.");
        incoming.forEach(request -> TeleportCommandSupport.info(
                source,
                playerName(source, request.senderId()) + " " + request.type().name().toLowerCase(java.util.Locale.ROOT)));
        TeleportCommandSupport.info(source, "Outgoing requests.");
        outgoing.forEach(request -> TeleportCommandSupport.info(
                source,
                playerName(source, request.targetId()) + " " + request.type().name().toLowerCase(java.util.Locale.ROOT)));
        return incoming.size() + outgoing.size();
    }

    private static String playerName(CommandSourceStack source, UUID playerId) {
        ServerPlayer player = source.getServer().getPlayerList().getPlayer(playerId);
        return player == null ? playerId.toString() : player.getGameProfile().getName();
    }

    private static void notifySender(
            CommandSourceStack source,
            TeleportRequestService.Request request,
            String message
    ) {
        ServerPlayer sender = source.getServer().getPlayerList().getPlayer(request.senderId());
        if (sender != null) {
            sender.sendSystemMessage(TextFormatter.stringToFormattedText("&e" + message));
        }
    }
}
