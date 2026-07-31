package com.enviouse.sef.moderation;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.identity.IdentityArguments;
import com.enviouse.sef.identity.IdentityService;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.ConfirmationService;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.teleport.SafeTeleportService;
import com.enviouse.sef.teleport.SavedLocation;
import com.enviouse.sef.util.DurationParser;
import com.enviouse.sef.vanish.VanishUtil;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.IpBanListEntry;
import net.minecraft.server.players.UserBanListEntry;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ModerationCommands {
    private ModerationCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (!ConfigHandler.config.enableModerationEssentials.get()) {
            return;
        }
        dispatcher.register(playerBanNode("ban", false));
        dispatcher.register(playerBanNode("tempban", true));
        dispatcher.register(pardonNode("pardon"));
        dispatcher.register(pardonNode("unban"));
        dispatcher.register(addressBanNode("ban-ip", false));
        dispatcher.register(addressBanNode("banip", false));
        dispatcher.register(addressBanNode("tempban-ip", true));
        dispatcher.register(addressBanNode("tempbanip", true));
        dispatcher.register(addressPardonNode("pardon-ip"));
        dispatcher.register(addressPardonNode("unban-ip"));
        dispatcher.register(addressPardonNode("unbanip"));
        dispatcher.register(kickNode());
        dispatcher.register(kickAddressNode("kick-ip"));
        dispatcher.register(kickAddressNode("kickip"));
        dispatcher.register(kickMeNode());
        dispatcher.register(kickAllNode());
        ModerationControlCommands.register(dispatcher);
        if (ConfigHandler.config.enableJails.get()) {
            registerJails(dispatcher);
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> playerBanNode(String literal, boolean temporary) {
        String permission = temporary ? "commands.tempban" : "commands.ban";
        String action = temporary ? "sef:moderation.tempban" : "sef:moderation.ban";
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(literal)
                .requires(source -> has(source, permission));
        if (temporary) {
            root.then(IdentityArguments.known("player")
                    .then(Commands.argument("duration", StringArgumentType.word())
                            .executes(context -> ban(
                                    context.getSource(),
                                    StringArgumentType.getString(context, "player"),
                                    StringArgumentType.getString(context, "duration"),
                                    "",
                                    action,
                                    permission))
                            .then(Commands.argument("reason", StringArgumentType.greedyString())
                                    .executes(context -> ban(
                                            context.getSource(),
                                            StringArgumentType.getString(context, "player"),
                                            StringArgumentType.getString(context, "duration"),
                                            StringArgumentType.getString(context, "reason"),
                                            action,
                                            permission)))));
        } else {
            root.then(IdentityArguments.known("player")
                    .executes(context -> ban(
                            context.getSource(),
                            StringArgumentType.getString(context, "player"),
                            "permanent",
                            "",
                            action,
                            permission))
                    .then(Commands.argument("reason", StringArgumentType.greedyString())
                            .executes(context -> ban(
                                    context.getSource(),
                                    StringArgumentType.getString(context, "player"),
                                    "permanent",
                                    StringArgumentType.getString(context, "reason"),
                                    action,
                                    permission))));
        }
        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> pardonNode(String literal) {
        return Commands.literal(literal)
                .requires(source -> has(source, "commands.pardon"))
                .then(IdentityArguments.known("player")
                        .executes(context -> pardon(
                                context.getSource(),
                                StringArgumentType.getString(context, "player"))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> addressBanNode(String literal, boolean temporary) {
        String permission = temporary ? "commands.tempbanip" : "commands.banip";
        String action = temporary ? "sef:moderation.tempban_ip" : "sef:moderation.ban_ip";
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(literal)
                .requires(source -> has(source, permission));
        if (temporary) {
            root.then(Commands.argument("address_or_player", StringArgumentType.string())
                    .suggests(IdentityArguments.suggestions(true))
                    .then(Commands.argument("duration", StringArgumentType.word())
                            .executes(context -> addressBan(
                                    context.getSource(),
                                    StringArgumentType.getString(context, "address_or_player"),
                                    StringArgumentType.getString(context, "duration"),
                                    "",
                                    action,
                                    permission))
                            .then(Commands.argument("reason", StringArgumentType.greedyString())
                                    .executes(context -> addressBan(
                                            context.getSource(),
                                            StringArgumentType.getString(context, "address_or_player"),
                                            StringArgumentType.getString(context, "duration"),
                                            StringArgumentType.getString(context, "reason"),
                                            action,
                                            permission)))));
        } else {
            root.then(Commands.argument("address_or_player", StringArgumentType.string())
                    .suggests(IdentityArguments.suggestions(true))
                    .executes(context -> addressBan(
                            context.getSource(),
                            StringArgumentType.getString(context, "address_or_player"),
                            "permanent",
                            "",
                            action,
                            permission))
                    .then(Commands.argument("reason", StringArgumentType.greedyString())
                            .executes(context -> addressBan(
                                    context.getSource(),
                                    StringArgumentType.getString(context, "address_or_player"),
                                    "permanent",
                                    StringArgumentType.getString(context, "reason"),
                                    action,
                                    permission))));
        }
        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> addressPardonNode(String literal) {
        return Commands.literal(literal)
                .requires(source -> has(source, "commands.pardonip"))
                .then(Commands.argument("address", StringArgumentType.word())
                        .executes(context -> addressPardon(
                                context.getSource(),
                                StringArgumentType.getString(context, "address"))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> kickNode() {
        return Commands.literal("kick")
                .requires(source -> has(source, "commands.kick"))
                .then(IdentityArguments.online("player")
                        .executes(context -> kick(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "player"),
                                ""))
                        .then(Commands.argument("reason", StringArgumentType.greedyString())
                                .executes(context -> kick(
                                        context.getSource(),
                                        IdentityArguments.getOnline(context, "player"),
                                        StringArgumentType.getString(context, "reason")))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> kickAddressNode(String literal) {
        return Commands.literal(literal)
                .requires(source -> has(source, "commands.kickip"))
                .then(Commands.literal("confirm")
                        .then(Commands.argument("token", StringArgumentType.word())
                                .then(Commands.argument("address_or_player", StringArgumentType.string())
                                        .suggests(IdentityArguments.suggestions(true))
                                        .executes(context -> kickAddress(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "address_or_player"),
                                                "",
                                                StringArgumentType.getString(context, "token")))
                                        .then(Commands.argument("reason", StringArgumentType.greedyString())
                                                .executes(context -> kickAddress(
                                                        context.getSource(),
                                                        StringArgumentType.getString(
                                                                context,
                                                                "address_or_player"),
                                                        StringArgumentType.getString(context, "reason"),
                                                        StringArgumentType.getString(context, "token")))))))
                .then(Commands.argument("address_or_player", StringArgumentType.string())
                        .suggests(IdentityArguments.suggestions(true))
                        .executes(context -> kickAddress(
                                context.getSource(),
                                StringArgumentType.getString(context, "address_or_player"),
                                "",
                                null))
                        .then(Commands.argument("reason", StringArgumentType.greedyString())
                                .executes(context -> kickAddress(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "address_or_player"),
                                        StringArgumentType.getString(context, "reason"),
                                        null))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> kickMeNode() {
        return Commands.literal("kickme")
                .requires(source -> source.getPlayer() != null && has(source, "commands.kickme"))
                .executes(context -> kickSelf(context.getSource(), ""))
                .then(Commands.argument("reason", StringArgumentType.greedyString())
                        .executes(context -> kickSelf(
                                context.getSource(),
                                StringArgumentType.getString(context, "reason"))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> kickAllNode() {
        return Commands.literal("kickall")
                .requires(source -> has(source, "commands.kickall"))
                .executes(context -> kickAll(context.getSource(), "", null))
                .then(Commands.literal("confirm")
                        .then(Commands.argument("token", StringArgumentType.word())
                                .executes(context -> kickAll(
                                        context.getSource(),
                                        "",
                                        StringArgumentType.getString(context, "token")))
                                .then(Commands.argument("reason", StringArgumentType.greedyString())
                                        .executes(context -> kickAll(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "reason"),
                                                StringArgumentType.getString(context, "token"))))))
                .then(Commands.argument("reason", StringArgumentType.greedyString())
                        .executes(context -> kickAll(
                                context.getSource(),
                                StringArgumentType.getString(context, "reason"),
                                null)));
    }

    private static void registerJails(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("setjail")
                .requires(source -> source.getPlayer() != null && has(source, "commands.setjail"))
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(context -> setJail(
                                context.getSource(),
                                StringArgumentType.getString(context, "name")))));
        dispatcher.register(Commands.literal("deljail")
                .requires(source -> has(source, "commands.deljail"))
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(context -> deleteJail(
                                context.getSource(),
                                StringArgumentType.getString(context, "name")))));
        dispatcher.register(Commands.literal("jails")
                .requires(source -> has(source, "commands.jails"))
                .executes(context -> listJails(context.getSource())));
        dispatcher.register(Commands.literal("jail")
                .requires(source -> has(source, "commands.jail"))
                .then(IdentityArguments.online("player")
                        .then(Commands.argument("jail", StringArgumentType.word())
                                .executes(context -> jail(
                                        context.getSource(),
                                        IdentityArguments.getOnline(context, "player"),
                                        StringArgumentType.getString(context, "jail"),
                                        "permanent",
                                        ""))
                                .then(Commands.argument("duration", StringArgumentType.word())
                                        .executes(context -> jail(
                                                context.getSource(),
                                                IdentityArguments.getOnline(context, "player"),
                                                StringArgumentType.getString(context, "jail"),
                                                StringArgumentType.getString(context, "duration"),
                                                ""))
                                        .then(Commands.argument("reason", StringArgumentType.greedyString())
                                                .executes(context -> jail(
                                                        context.getSource(),
                                                        IdentityArguments.getOnline(context, "player"),
                                                        StringArgumentType.getString(context, "jail"),
                                                        StringArgumentType.getString(context, "duration"),
                                                        StringArgumentType.getString(context, "reason"))))))));
        dispatcher.register(Commands.literal("unjail")
                .requires(source -> has(source, "commands.unjail"))
                .then(IdentityArguments.online("player")
                        .executes(context -> unjail(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "player")))));
        dispatcher.register(Commands.literal("jailedplayers")
                .requires(source -> has(source, "commands.jailedplayers"))
                .executes(context -> jailedPlayers(context.getSource())));
    }

    private static int ban(
            CommandSourceStack source,
            String playerInput,
            String durationInput,
            String reasonInput,
            String action,
            String permission
    ) {
        ResolvedIdentity identity = resolveKnown(source, playerInput);
        if (identity == null || !eligibleIdentity(source, identity, "exempt.ban")) {
            return unavailable(source);
        }
        Expiry expiry = expiry(durationInput, true);
        if (expiry == null) {
            return fail(source, "Duration is invalid.");
        }
        String reason = reason(reasonInput, "Banned by an administrator.");
        if (reason == null) {
            return 0;
        }
        return execute(source, action, Map.of(
                "target", identity.playerId().toString(),
                "temporary", Boolean.toString(expiry.date() != null),
                "reason_length", Integer.toString(reason.length())), List.of(identity.playerId()), () -> {
            GameProfile profile = identity.profile();
            var bans = source.getServer().getPlayerList().getBans();
            if (bans.isBanned(profile)) {
                return fail(source, "That profile is already banned.");
            }
            bans.add(new UserBanListEntry(profile, new Date(), source.getTextName(), expiry.date(), reason));
            ServerPlayer online = source.getServer().getPlayerList().getPlayer(identity.playerId());
            if (online != null) {
                online.connection.disconnect(Component.literal(reason));
            }
            success(source, "Banned " + identity.username() + ".");
            return 1;
        }, permission(permission));
    }

    private static int pardon(CommandSourceStack source, String playerInput) {
        ResolvedIdentity identity = resolveKnown(source, playerInput);
        if (identity == null || !eligibleIdentity(source, identity, "exempt.ban")) {
            return unavailable(source);
        }
        return execute(source, "sef:moderation.pardon",
                Map.of("target", identity.playerId().toString()), List.of(identity.playerId()), () -> {
                    var bans = source.getServer().getPlayerList().getBans();
                    if (!bans.isBanned(identity.profile())) {
                        return fail(source, "That profile is not banned.");
                    }
                    bans.remove(identity.profile());
                    success(source, "Pardoned " + identity.username() + ".");
                    return 1;
                }, permission("commands.pardon"));
    }

    private static int addressBan(
            CommandSourceStack source,
            String input,
            String durationInput,
            String reasonInput,
            String action,
            String permission
    ) {
        AddressTarget target = resolveAddress(source, input, "commands.banip.literal");
        if (target == null || !eligibleSessions(source, target.sessions(), "exempt.ipban")) {
            return unavailable(source);
        }
        if (!KernelServices.connectionAddresses().safeForSharedAction(source.getServer(), target.address())) {
            return fail(source, "The authoritative address provider rejected this shared address action.");
        }
        Expiry expiry = expiry(durationInput, true);
        if (expiry == null) {
            return fail(source, "Duration is invalid.");
        }
        String reason = reason(reasonInput, "Address banned by an administrator.");
        if (reason == null) {
            return 0;
        }
        return execute(source, action, Map.of(
                "address", target.address().fingerprint(),
                "target_count", Integer.toString(target.sessions().size()),
                "temporary", Boolean.toString(expiry.date() != null)), target.playerIds(), () -> {
            var bans = source.getServer().getPlayerList().getIpBans();
            if (bans.isBanned(target.address().normalized())) {
                return fail(source, "That address record is already banned.");
            }
            bans.add(new IpBanListEntry(
                    target.address().normalized(),
                    new Date(),
                    source.getTextName(),
                    expiry.date(),
                    reason));
            for (ConnectionAddressService.Session session : target.sessions()) {
                session.player().connection.disconnect(Component.literal(reason));
            }
            success(source, "Banned " + target.address().redacted() + ".");
            return 1;
        }, permission(permission));
    }

    private static int addressPardon(CommandSourceStack source, String input) {
        if (!literalAllowed(source, "commands.pardonip.literal")) {
            return fail(source, "Literal address input is disabled for this source.");
        }
        Optional<ConnectionAddressService.Address> parsed = KernelServices.connectionAddresses().literal(input);
        if (parsed.isEmpty()) {
            return fail(source, "Use an exact literal IPv4 or IPv6 address.");
        }
        ConnectionAddressService.Address address = parsed.orElseThrow();
        return execute(source, "sef:moderation.pardon_ip",
                Map.of("address", address.fingerprint()), List.of(), () -> {
                    var bans = source.getServer().getPlayerList().getIpBans();
                    if (!bans.isBanned(address.normalized())) {
                        return fail(source, "That address record is not banned.");
                    }
                    bans.remove(address.normalized());
                    success(source, "Pardoned " + address.redacted() + ".");
                    return 1;
                }, permission("commands.pardonip"), permission("commands.pardonip.literal"));
    }

    private static int kick(CommandSourceStack source, ServerPlayer target, String reasonInput) {
        if (!eligible(source, target, "exempt.kick")) {
            return unavailable(source);
        }
        String reason = reason(reasonInput, ConfigHandler.config.moderationDefaultKickReason.get());
        if (reason == null) {
            return 0;
        }
        UUID sessionId = target.getUUID();
        return execute(source, "sef:moderation.kick",
                Map.of("target", sessionId.toString(), "reason_length", Integer.toString(reason.length())),
                List.of(sessionId), () -> {
                    if (target.hasDisconnected() || source.getServer().getPlayerList().getPlayer(sessionId) != target) {
                        return fail(source, "The target session changed.");
                    }
                    target.connection.disconnect(Component.literal(reason));
                    success(source, "Kicked " + target.getGameProfile().getName() + ".");
                    return 1;
                }, permission("commands.kick"));
    }

    private static int kickAddress(
            CommandSourceStack source,
            String input,
            String reasonInput,
            String confirmationToken
    ) {
        AddressTarget target = resolveAddress(source, input, "commands.kickip.literal");
        if (target == null || target.sessions().isEmpty()
                || !eligibleSessions(source, target.sessions(), "exempt.kickip")) {
            return unavailable(source);
        }
        if (!KernelServices.connectionAddresses().safeForSharedAction(source.getServer(), target.address())) {
            return fail(source, "The authoritative address provider rejected this shared address action.");
        }
        String reason = reason(reasonInput, ConfigHandler.config.moderationDefaultKickReason.get());
        if (reason == null) {
            return 0;
        }
        ConfirmationService.Request confirmation = confirmation(
                source,
                "sef:moderation.kick_ip",
                Map.of(
                        "address", target.address().fingerprint(),
                        "reason", digest(reason)),
                target.playerIds());
        int confirmationResult = requireConfirmation(
                source,
                confirmation,
                confirmationToken,
                "Use /kick-ip confirm <token> <address or player> [reason].");
        if (confirmationResult >= 0) {
            return confirmationResult;
        }
        return execute(source, "sef:moderation.kick_ip", Map.of(
                "address", target.address().fingerprint(),
                "target_count", Integer.toString(target.sessions().size())), target.playerIds(), () -> {
            int disconnected = 0;
            for (ConnectionAddressService.Session session : target.sessions()) {
                if (!session.player().hasDisconnected()) {
                    session.player().connection.disconnect(Component.literal(reason));
                    disconnected++;
                }
            }
            success(source, "Disconnected " + disconnected + " sessions sharing "
                    + target.address().redacted() + ".");
            return Math.max(1, disconnected);
        }, permission("commands.kickip"));
    }

    private static int kickSelf(CommandSourceStack source, String reasonInput) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return unavailable(source);
        }
        String reason = reason(reasonInput, "Disconnected by request.");
        if (reason == null) {
            return 0;
        }
        return execute(source, "sef:moderation.kick_self", Map.of(), List.of(player.getUUID()), () -> {
            player.connection.disconnect(Component.literal(reason));
            return 1;
        }, permission("commands.kickme"));
    }

    private static int kickAll(CommandSourceStack source, String reasonInput, String confirmationToken) {
        ServerPlayer actor = source.getPlayer();
        int cap = Math.min(
                ConfigHandler.config.moderationMaximumMassTargets.get(),
                ConfigHandler.config.moderationSharedAddressHardCap.get() * 10);
        List<ServerPlayer> targets = source.getServer().getPlayerList().getPlayers().stream()
                .filter(target -> actor == null || target != actor)
                .filter(target -> eligible(source, target, "exempt.kick"))
                .limit(cap + 1L)
                .toList();
        if (targets.size() > cap) {
            return fail(source, "The eligible target count exceeds the configured hard cap.");
        }
        String reason = reason(reasonInput, ConfigHandler.config.moderationDefaultKickReason.get());
        if (reason == null) {
            return 0;
        }
        List<UUID> targetIds = targets.stream().map(ServerPlayer::getUUID).toList();
        ConfirmationService.Request confirmation = confirmation(
                source,
                "sef:moderation.kick_all",
                Map.of(
                        "reason", digest(reason),
                        "target_count", Integer.toString(targets.size())),
                targetIds);
        int confirmationResult = requireConfirmation(
                source,
                confirmation,
                confirmationToken,
                "Use /kickall confirm <token> [reason].");
        if (confirmationResult >= 0) {
            return confirmationResult;
        }
        return execute(source, "sef:moderation.kick_all",
                Map.of("target_count", Integer.toString(targets.size())),
                targetIds, () -> {
                    for (ServerPlayer target : targets) {
                        target.connection.disconnect(Component.literal(reason));
                    }
                    success(source, "Disconnected " + targets.size() + " eligible players.");
                    return Math.max(1, targets.size());
                }, permission("commands.kickall"));
    }

    private static int setJail(CommandSourceStack source, String name) {
        ServerPlayer actor = source.getPlayer();
        return execute(source, "sef:moderation.setjail", Map.of("jail", name), List.of(), () -> {
            try {
                KernelServices.moderation().setJail(name, SavedLocation.from(actor), actor.getUUID());
            } catch (IllegalArgumentException | IllegalStateException exception) {
                return fail(source, exception.getMessage());
            }
            success(source, "Jail " + name + " saved.");
            return 1;
        }, permission("commands.setjail"));
    }

    private static int deleteJail(CommandSourceStack source, String name) {
        return execute(source, "sef:moderation.deljail", Map.of("jail", name), List.of(), () -> {
            try {
                if (!KernelServices.moderation().deleteJail(name)) {
                    return fail(source, "Jail not found.");
                }
            } catch (IllegalArgumentException | IllegalStateException exception) {
                return fail(source, exception.getMessage());
            }
            success(source, "Jail " + name + " deleted.");
            return 1;
        }, permission("commands.deljail"));
    }

    private static int listJails(CommandSourceStack source) {
        return execute(source, "sef:moderation.jails", Map.of(), List.of(), () -> {
            List<ModerationRepository.Jail> jails = KernelServices.moderation().jails();
            info(source, jails.isEmpty() ? "No jails are configured."
                    : "Jails. " + String.join(", ", jails.stream()
                    .map(ModerationRepository.Jail::displayName).toList()));
            return Math.max(1, jails.size());
        }, permission("commands.jails"));
    }

    private static int jail(
            CommandSourceStack source,
            ServerPlayer target,
            String jailName,
            String durationInput,
            String reasonInput
    ) {
        if (!eligible(source, target, "exempt.jail")) {
            return unavailable(source);
        }
        Optional<ModerationRepository.Jail> jail = KernelServices.moderation().jail(jailName);
        if (jail.isEmpty()) {
            return fail(source, "Jail not found.");
        }
        Expiry expiry = expiry(durationInput, true);
        if (expiry == null) {
            return fail(source, "Duration is invalid.");
        }
        String reason = reason(reasonInput, "Jailed by an administrator.");
        if (reason == null) {
            return 0;
        }
        return execute(source, "sef:moderation.jail",
                Map.of("jail", jail.orElseThrow().normalizedName(), "target", target.getUUID().toString()),
                List.of(target.getUUID()), () -> {
                    SavedLocation release = SavedLocation.from(target);
                    ModerationRepository.Sentence prepared = KernelServices.moderation().prepareSentence(
                            target.getUUID(),
                            jailName,
                            expiry.instant(),
                            reason,
                            source.getPlayer() == null ? null : source.getPlayer().getUUID(),
                            release);
                    if (!ModerationEvents.persist("prepared jail transition")) {
                        KernelServices.moderation().rollbackJail(
                                target.getUUID(),
                                prepared.operationId(),
                                "jail intent could not be persisted");
                        return fail(source, "The jail intent could not be stored.");
                    }
                    ModerationEvents.TransitionResult result = ModerationEvents.completePreparedJail(
                            source.getServer(),
                            source.getPlayer(),
                            target,
                            prepared,
                            true);
                    if (!result.successful()) {
                        return fail(source, "Jail destination rejected. " + result.detail());
                    }
                    target.sendSystemMessage(Component.literal(reason));
                    success(source, "Jailed " + target.getGameProfile().getName() + ".");
                    return 1;
                }, permission("commands.jail"));
    }

    private static int unjail(CommandSourceStack source, ServerPlayer target) {
        if (!eligible(source, target, "exempt.jail")) {
            return unavailable(source);
        }
        Optional<ModerationRepository.Sentence> sentence = KernelServices.moderation().sentence(target.getUUID());
        if (sentence.isEmpty()) {
            return fail(source, "That player is not jailed.");
        }
        return execute(source, "sef:moderation.unjail",
                Map.of("target", target.getUUID().toString()), List.of(target.getUUID()), () -> {
                    ModerationRepository.Sentence pending = KernelServices.moderation()
                            .prepareRelease(target.getUUID()).orElse(null);
                    if (pending == null) {
                        return fail(source, "The jail sentence changed.");
                    }
                    if (!ModerationEvents.persist("prepared jail release")) {
                        return fail(source, "The release intent could not be stored.");
                    }
                    ModerationEvents.TransitionResult result = ModerationEvents.release(
                            source.getServer(),
                            source.getPlayer(),
                            target,
                            pending,
                            "jail release");
                    if (!result.successful()) {
                        return fail(source, "The player remains jailed. " + result.detail());
                    }
                    success(source, "Released " + target.getGameProfile().getName() + ".");
                    return 1;
                }, permission("commands.unjail"));
    }

    private static int jailedPlayers(CommandSourceStack source) {
        return execute(source, "sef:moderation.jailedplayers", Map.of(), List.of(), () -> {
            List<ModerationRepository.Sentence> sentences = KernelServices.moderation().sentences();
            info(source, "Active jail sentences. " + sentences.size() + ".");
            for (ModerationRepository.Sentence sentence : sentences) {
                String name = KernelServices.profiles().find(sentence.playerId())
                        .map(profile -> profile.authenticatedUsername())
                        .orElse(sentence.playerId().toString());
                info(source, name + ", " + sentence.jailName() + ", "
                        + (sentence.expiresAt() == null ? "permanent" : sentence.expiresAt()) + ".");
            }
            return Math.max(1, sentences.size());
        }, permission("commands.jailedplayers"));
    }

    private static ResolvedIdentity resolveKnown(CommandSourceStack source, String input) {
        ActionResult<IdentityService.Identity> result = KernelServices.identities().resolve(
                input,
                source.getPlayer(),
                PermissionService.isConsole(source));
        if (!result.successful() || result.value().playerId() == null
                || result.value().authenticatedUsername().isBlank()) {
            return null;
        }
        return new ResolvedIdentity(
                result.value().playerId(),
                result.value().authenticatedUsername(),
                new GameProfile(result.value().playerId(), result.value().authenticatedUsername()));
    }

    private static boolean eligibleIdentity(CommandSourceStack source, ResolvedIdentity identity, String exemption) {
        ServerPlayer online = source.getServer().getPlayerList().getPlayer(identity.playerId());
        if (online != null) {
            return eligible(source, online, exemption);
        }
        PermissionNode<Boolean> exempt = permission(exemption);
        PermissionNode<Boolean> bypass = permission("moderation.bypass.exempt");
        return !PermissionsHandler.playerHasPermission(identity.playerId(), exempt)
                || source.getPlayer() != null && PermissionService.has(source, bypass)
                || PermissionService.isConsole(source);
    }

    private static boolean eligible(CommandSourceStack source, ServerPlayer target, String exemption) {
        ServerPlayer actor = source.getPlayer();
        if (actor != null && VanishUtil.isVanished(target, actor)) {
            return false;
        }
        return PlayerTargetPolicy.decide(
                source,
                target,
                permission("moderation.hierarchy.bypass"),
                permission(exemption),
                permission("moderation.bypass.exempt"),
                true,
                true).allowed();
    }

    private static boolean eligibleSessions(
            CommandSourceStack source,
            List<ConnectionAddressService.Session> sessions,
            String exemption
    ) {
        return sessions.stream().allMatch(session -> eligible(source, session.player(), exemption));
    }

    private static ConfirmationService.Request confirmation(
            CommandSourceStack source,
            String action,
            Map<String, String> parameters,
            List<UUID> targets
    ) {
        UUID actorId = source.getEntity() == null
                ? UUID.nameUUIDFromBytes(
                ("sef:source:" + source.getTextName()).getBytes(StandardCharsets.UTF_8))
                : source.getEntity().getUUID();
        return new ConfirmationService.Request(
                actorId,
                action,
                parameters,
                targets,
                "",
                0L,
                0L,
                0L,
                KernelServices.commandPolicies().revision());
    }

    private static int requireConfirmation(
            CommandSourceStack source,
            ConfirmationService.Request request,
            String token,
            String usage
    ) {
        if (token == null) {
            ActionResult<ConfirmationService.IssuedToken> issued = KernelServices.confirmations().issue(
                    request,
                    Duration.ofSeconds(ConfigHandler.config.moderationConfirmationSeconds.get()));
            if (!issued.successful()) {
                return fail(source, "A confirmation token could not be issued.");
            }
            source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                    "&eConfirmation required. " + usage.replace("<token>", issued.value().token())), false);
            return 1;
        }
        ActionResult<ConfirmationService.Request> consumed =
                KernelServices.confirmations().consume(token, request);
        if (!consumed.successful()) {
            return fail(source, "The confirmation token is invalid, expired, used, or no longer matches.");
        }
        return -1;
    }

    private static String digest(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static AddressTarget resolveAddress(CommandSourceStack source, String input, String literalPermission) {
        ActionResult<IdentityService.Identity> identity = KernelServices.identities().resolve(
                input,
                source.getPlayer(),
                PermissionService.isConsole(source));
        ConnectionAddressService.Address address;
        if (identity.successful() && identity.value().online() && identity.value().playerId() != null) {
            ServerPlayer player = source.getServer().getPlayerList().getPlayer(identity.value().playerId());
            if (player == null || !eligible(source, player, "exempt.ipban")) {
                return null;
            }
            Optional<ConnectionAddressService.Address> resolved =
                    KernelServices.connectionAddresses().forPlayer(player);
            if (resolved.isEmpty()) {
                return null;
            }
            address = resolved.orElseThrow();
        } else {
            if (!literalAllowed(source, literalPermission)) {
                return null;
            }
            Optional<ConnectionAddressService.Address> parsed =
                    KernelServices.connectionAddresses().literal(input);
            if (parsed.isEmpty()) {
                return null;
            }
            address = parsed.orElseThrow();
        }
        List<ConnectionAddressService.Session> sessions =
                KernelServices.connectionAddresses().sessions(source.getServer(), address);
        if (sessions.size() > ConfigHandler.config.moderationSharedAddressHardCap.get()) {
            return null;
        }
        return new AddressTarget(address, sessions);
    }

    private static boolean literalAllowed(CommandSourceStack source, String permission) {
        if (!has(source, permission)) {
            return false;
        }
        return PermissionService.isConsole(source)
                ? ConfigHandler.config.moderationAllowLiteralConsoleAddresses.get()
                : ConfigHandler.config.moderationAllowLiteralPlayerAddresses.get();
    }

    private static Expiry expiry(String input, boolean allowPermanent) {
        DurationParser.Result parsed = DurationParser.parse(input, allowPermanent);
        if (!parsed.valid() || parsed.seconds() > 315_576_000L) {
            return null;
        }
        if (parsed.permanent()) {
            return new Expiry(null, null);
        }
        Instant instant;
        try {
            instant = Instant.now().plusSeconds(parsed.seconds());
        } catch (RuntimeException exception) {
            return null;
        }
        return new Expiry(Date.from(instant), instant);
    }

    private static String reason(String input, String fallback) {
        String reason = input == null || input.isBlank() ? fallback : input.strip();
        if (reason == null || reason.isBlank()
                || reason.length() > ConfigHandler.config.moderationMaximumReasonLength.get()
                || reason.codePoints().anyMatch(Character::isISOControl)) {
            return null;
        }
        return reason;
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

    private static boolean has(CommandSourceStack source, String permission) {
        PermissionNode<Boolean> node = permission(permission);
        return node != null && PermissionService.has(source, node);
    }

    private static PermissionNode<Boolean> permission(String permission) {
        return PermissionsHandler.phasePermission(permission);
    }

    private static int unavailable(CommandSourceStack source) {
        return fail(source, "That player or address is unavailable.");
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

    private record ResolvedIdentity(UUID playerId, String username, GameProfile profile) {
    }

    private record Expiry(Date date, Instant instant) {
    }

    private record AddressTarget(
            ConnectionAddressService.Address address,
            List<ConnectionAddressService.Session> sessions
    ) {
        private AddressTarget {
            sessions = List.copyOf(sessions);
        }

        List<UUID> playerIds() {
            return sessions.stream().map(session -> session.player().getUUID()).toList();
        }
    }
}
