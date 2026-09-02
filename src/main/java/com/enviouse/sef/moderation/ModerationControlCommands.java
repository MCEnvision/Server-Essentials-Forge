package com.enviouse.sef.moderation;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.identity.IdentityArguments;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.util.DurationParser;
import com.enviouse.sef.vanish.VanishUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ModerationControlCommands {
    private ModerationControlCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        registerWarnings(dispatcher);
        registerMutes(dispatcher);
        registerFreezes(dispatcher);
        registerToggle(
                dispatcher,
                "invlock",
                null,
                PermissionsHandler.invLockCommand,
                ModerationRepository.ControlType.INVENTORY_LOCK,
                "sef:moderation.invlock",
                "exempt.invlock");
        registerToggle(
                dispatcher,
                "disablebuilding",
                "db",
                PermissionsHandler.disableBuildingCommand,
                ModerationRepository.ControlType.BUILD_LOCK,
                "sef:moderation.disablebuilding",
                "exempt.disablebuilding");
    }

    private static void registerWarnings(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("warn")
                .requires(source -> PermissionService.has(source, PermissionsHandler.warnCommand))
                .then(IdentityArguments.online("player")
                        .then(Commands.argument("reason", StringArgumentType.greedyString())
                                .executes(context -> warn(
                                        context.getSource(),
                                        IdentityArguments.getOnline(context, "player"),
                                        StringArgumentType.getString(context, "reason"))))));
        dispatcher.register(Commands.literal("warns")
                .requires(source -> PermissionService.has(source, PermissionsHandler.warnsSelfCommand)
                        || PermissionService.has(source, PermissionsHandler.warnCommand))
                .executes(context -> listWarnings(context.getSource(), context.getSource().getPlayer()))
                .then(IdentityArguments.online("player")
                        .requires(source -> PermissionService.has(source, PermissionsHandler.warnCommand))
                        .executes(context -> listWarnings(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "player")))));
        dispatcher.register(Commands.literal("clearwarnings")
                .requires(source -> PermissionService.has(source, PermissionsHandler.warnCommand))
                .then(IdentityArguments.online("player")
                        .executes(context -> clearWarnings(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "player")))));
    }

    private static void registerMutes(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mute")
                .requires(source -> PermissionService.has(source, PermissionsHandler.muteCommand))
                .then(IdentityArguments.online("player")
                        .executes(context -> applyTimed(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "player"),
                                ModerationRepository.ControlType.MUTE,
                                "permanent",
                                "Muted by an administrator.",
                                "sef:moderation.mute",
                                PermissionsHandler.muteCommand,
                                "exempt.mute"))
                        .then(Commands.argument("duration", StringArgumentType.word())
                                .executes(context -> applyTimed(
                                        context.getSource(),
                                        IdentityArguments.getOnline(context, "player"),
                                        ModerationRepository.ControlType.MUTE,
                                        StringArgumentType.getString(context, "duration"),
                                        "Muted by an administrator.",
                                        "sef:moderation.mute",
                                        PermissionsHandler.muteCommand,
                                        "exempt.mute"))
                                .then(Commands.argument("reason", StringArgumentType.greedyString())
                                        .executes(context -> applyTimed(
                                                context.getSource(),
                                                IdentityArguments.getOnline(context, "player"),
                                                ModerationRepository.ControlType.MUTE,
                                                StringArgumentType.getString(context, "duration"),
                                                StringArgumentType.getString(context, "reason"),
                                                "sef:moderation.mute",
                                                PermissionsHandler.muteCommand,
                                                "exempt.mute"))))));
        dispatcher.register(Commands.literal("unmute")
                .requires(source -> PermissionService.has(source, PermissionsHandler.unmuteCommand))
                .then(IdentityArguments.online("player")
                        .executes(context -> remove(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "player"),
                                ModerationRepository.ControlType.MUTE,
                                "sef:moderation.unmute",
                                PermissionsHandler.unmuteCommand,
                                "exempt.mute"))));
        dispatcher.register(Commands.literal("mutelist")
                .requires(source -> PermissionService.has(source, PermissionsHandler.muteCommand))
                .executes(context -> listControls(
                        context.getSource(),
                        ModerationRepository.ControlType.MUTE,
                        "sef:moderation.mutelist",
                        PermissionsHandler.muteCommand)));
    }

    private static void registerFreezes(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("freeze")
                .requires(source -> PermissionService.has(source, PermissionsHandler.freezeCommand))
                .then(IdentityArguments.online("player")
                        .executes(context -> applyTimed(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "player"),
                                ModerationRepository.ControlType.FREEZE,
                                "permanent",
                                "Frozen by an administrator.",
                                "sef:moderation.freeze",
                                PermissionsHandler.freezeCommand,
                                "exempt.freeze"))
                        .then(Commands.argument("duration", StringArgumentType.word())
                                .executes(context -> applyTimed(
                                        context.getSource(),
                                        IdentityArguments.getOnline(context, "player"),
                                        ModerationRepository.ControlType.FREEZE,
                                        StringArgumentType.getString(context, "duration"),
                                        "Frozen by an administrator.",
                                        "sef:moderation.freeze",
                                        PermissionsHandler.freezeCommand,
                                        "exempt.freeze"))
                                .then(Commands.argument("reason", StringArgumentType.greedyString())
                                        .executes(context -> applyTimed(
                                                context.getSource(),
                                                IdentityArguments.getOnline(context, "player"),
                                                ModerationRepository.ControlType.FREEZE,
                                                StringArgumentType.getString(context, "duration"),
                                                StringArgumentType.getString(context, "reason"),
                                                "sef:moderation.freeze",
                                                PermissionsHandler.freezeCommand,
                                                "exempt.freeze"))))));
        dispatcher.register(Commands.literal("unfreeze")
                .requires(source -> PermissionService.has(source, PermissionsHandler.unfreezeCommand))
                .then(IdentityArguments.online("player")
                        .executes(context -> remove(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "player"),
                                ModerationRepository.ControlType.FREEZE,
                                "sef:moderation.unfreeze",
                                PermissionsHandler.unfreezeCommand,
                                "exempt.freeze"))));
        dispatcher.register(Commands.literal("freezelist")
                .requires(source -> PermissionService.has(source, PermissionsHandler.freezeCommand))
                .executes(context -> listControls(
                        context.getSource(),
                        ModerationRepository.ControlType.FREEZE,
                        "sef:moderation.freezelist",
                        PermissionsHandler.freezeCommand)));
    }

    private static void registerToggle(
            CommandDispatcher<CommandSourceStack> dispatcher,
            String literal,
            String alias,
            PermissionNode<Boolean> permission,
            ModerationRepository.ControlType type,
            String action,
            String exemption
    ) {
        dispatcher.register(toggleNode(literal, permission, type, action, exemption));
        if (alias != null) {
            dispatcher.register(toggleNode(alias, permission, type, action, exemption));
        }
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> toggleNode(
            String literal,
            PermissionNode<Boolean> permission,
            ModerationRepository.ControlType type,
            String action,
            String exemption
    ) {
        return Commands.literal(literal)
                .requires(source -> PermissionService.has(source, permission))
                .then(IdentityArguments.online("player")
                        .executes(context -> toggle(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "player"),
                                null,
                                type,
                                action,
                                permission,
                                exemption))
                        .then(Commands.argument("state", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        new String[]{"on", "off", "toggle"},
                                        builder))
                                .executes(context -> toggle(
                                        context.getSource(),
                                        IdentityArguments.getOnline(context, "player"),
                                        StringArgumentType.getString(context, "state"),
                                        type,
                                        action,
                                        permission,
                                        exemption))));
    }

    private static int warn(CommandSourceStack source, ServerPlayer target, String reason) {
        if (!eligible(source, target, "exempt.warn")) {
            return unavailable(source);
        }
        String bounded = boundedReason(reason);
        if (bounded == null) {
            return fail(source, "The warning reason is required and must be at most 512 characters.");
        }
        return execute(source, "sef:moderation.warn", Map.of(
                "target", target.getUUID().toString(),
                "reason_length", Integer.toString(bounded.length())), List.of(target.getUUID()), () -> {
            KernelServices.moderation().warn(target.getUUID(), bounded, actorId(source));
            target.sendSystemMessage(TextFormatter.stringToFormattedText("&cWarning. &f" + bounded));
            success(source, "Warned " + target.getGameProfile().getName() + ".");
            return 1;
        }, PermissionsHandler.warnCommand);
    }

    private static int listWarnings(CommandSourceStack source, ServerPlayer target) {
        if (target == null) {
            return fail(source, "An explicit online player is required.");
        }
        boolean self = source.getPlayer() == target;
        if (!self && (!PermissionService.has(source, PermissionsHandler.warnCommand)
                || !eligible(source, target, "exempt.warn"))) {
            return unavailable(source);
        }
        PermissionNode<Boolean> permission = self
                ? PermissionsHandler.warnsSelfCommand
                : PermissionsHandler.warnCommand;
        return execute(source, "sef:moderation.warns", Map.of(
                "target", target.getUUID().toString()), List.of(target.getUUID()), () -> {
            List<ModerationRepository.Warning> warnings = KernelServices.moderation().warnings(target.getUUID());
            info(source, target.getGameProfile().getName() + " has " + warnings.size() + " warning records.");
            warnings.forEach(warning -> info(source, warning.createdAt() + ", " + warning.reason()));
            return Math.max(1, warnings.size());
        }, permission);
    }

    private static int clearWarnings(CommandSourceStack source, ServerPlayer target) {
        if (!eligible(source, target, "exempt.warn")) {
            return unavailable(source);
        }
        return execute(source, "sef:moderation.clearwarnings", Map.of(
                "target", target.getUUID().toString()), List.of(target.getUUID()), () -> {
            int removed = KernelServices.moderation().clearWarnings(target.getUUID());
            success(source, "Cleared " + removed + " warning records.");
            return Math.max(1, removed);
        }, PermissionsHandler.warnCommand);
    }

    private static int applyTimed(
            CommandSourceStack source,
            ServerPlayer target,
            ModerationRepository.ControlType type,
            String durationInput,
            String reason,
            String action,
            PermissionNode<Boolean> permission,
            String exemption
    ) {
        if (!eligible(source, target, exemption)) {
            return unavailable(source);
        }
        DurationParser.Result parsed = DurationParser.parse(durationInput, true);
        if (!parsed.valid() || parsed.seconds() > 315_576_000L) {
            return fail(source, "The duration is invalid.");
        }
        String bounded = boundedReason(reason);
        if (bounded == null) {
            return fail(source, "The reason is required and must be at most 512 characters.");
        }
        Instant expiresAt = parsed.permanent() ? null : Instant.now().plusSeconds(parsed.seconds());
        return execute(source, action, Map.of(
                "target", target.getUUID().toString(),
                "temporary", Boolean.toString(expiresAt != null),
                "reason_length", Integer.toString(bounded.length())), List.of(target.getUUID()), () -> {
            KernelServices.moderation().applyControl(
                    target.getUUID(),
                    type,
                    expiresAt,
                    bounded,
                    actorId(source));
            if (type == ModerationRepository.ControlType.FREEZE) {
                target.closeContainer();
            }
            target.sendSystemMessage(TextFormatter.stringToFormattedText(
                    "&c" + display(type) + " enabled. &f" + bounded));
            success(source, display(type) + " enabled for " + target.getGameProfile().getName() + ".");
            return 1;
        }, permission);
    }

    private static int remove(
            CommandSourceStack source,
            ServerPlayer target,
            ModerationRepository.ControlType type,
            String action,
            PermissionNode<Boolean> permission,
            String exemption
    ) {
        if (!eligible(source, target, exemption)) {
            return unavailable(source);
        }
        return execute(source, action, Map.of(
                "target", target.getUUID().toString()), List.of(target.getUUID()), () -> {
            if (KernelServices.moderation().removeControl(target.getUUID(), type).isEmpty()) {
                return fail(source, "That control is not active.");
            }
            target.sendSystemMessage(TextFormatter.stringToFormattedText(
                    "&a" + display(type) + " disabled."));
            success(source, display(type) + " disabled for " + target.getGameProfile().getName() + ".");
            return 1;
        }, permission);
    }

    private static int toggle(
            CommandSourceStack source,
            ServerPlayer target,
            String state,
            ModerationRepository.ControlType type,
            String action,
            PermissionNode<Boolean> permission,
            String exemption
    ) {
        if (!eligible(source, target, exemption)) {
            return unavailable(source);
        }
        boolean active = KernelServices.moderation().control(target.getUUID(), type).isPresent();
        boolean enable = state == null || state.equalsIgnoreCase("toggle")
                ? !active
                : state.equalsIgnoreCase("on") || state.equalsIgnoreCase("true");
        if (state != null
                && !state.equalsIgnoreCase("toggle")
                && !state.equalsIgnoreCase("on")
                && !state.equalsIgnoreCase("off")
                && !state.equalsIgnoreCase("true")
                && !state.equalsIgnoreCase("false")) {
            return fail(source, "Use on, off, or toggle.");
        }
        if (enable) {
            return applyTimed(
                    source,
                    target,
                    type,
                    "permanent",
                    display(type) + " applied by an administrator.",
                    action,
                    permission,
                    exemption);
        }
        return remove(source, target, type, action, permission, exemption);
    }

    private static int listControls(
            CommandSourceStack source,
            ModerationRepository.ControlType type,
            String action,
            PermissionNode<Boolean> permission
    ) {
        return execute(source, action, Map.of("type", type.name()), List.of(), () -> {
            List<ModerationRepository.Control> controls = KernelServices.moderation().controls(type).stream()
                    .filter(control -> {
                        if (source.getPlayer() == null) {
                            return true;
                        }
                        ServerPlayer target = source.getServer().getPlayerList().getPlayer(control.playerId());
                        return target != null && eligible(source, target, "exempt." + exemptionName(type));
                    })
                    .toList();
            info(source, "Active " + display(type).toLowerCase() + " controls, " + controls.size() + ".");
            controls.forEach(control -> info(source, control.playerId() + ", expires " + control.expiresAt() + "."));
            return Math.max(1, controls.size());
        }, permission);
    }

    private static boolean eligible(CommandSourceStack source, ServerPlayer target, String exemption) {
        if (source.getPlayer() != null && VanishUtil.isVanished(target, source.getPlayer())) {
            return false;
        }
        return PlayerTargetPolicy.decide(
                source,
                target,
                PermissionsHandler.phasePermission("moderation.hierarchy.bypass"),
                PermissionsHandler.phasePermission(exemption),
                PermissionsHandler.phasePermission("moderation.bypass.exempt"),
                true,
                true).allowed();
    }

    private static UUID actorId(CommandSourceStack source) {
        return source.getEntity() == null
                ? UUID.nameUUIDFromBytes(
                ("sef:source:" + source.getTextName()).getBytes(StandardCharsets.UTF_8))
                : source.getEntity().getUUID();
    }

    private static String boundedReason(String reason) {
        if (reason == null) {
            return null;
        }
        String bounded = reason.strip();
        return bounded.isBlank()
                || bounded.length() > ConfigHandler.config.moderationMaximumReasonLength.get()
                || bounded.codePoints().anyMatch(Character::isISOControl)
                ? null
                : bounded;
    }

    private static String display(ModerationRepository.ControlType type) {
        return switch (type) {
            case MUTE -> "Mute";
            case FREEZE -> "Freeze";
            case INVENTORY_LOCK -> "Inventory lock";
            case BUILD_LOCK -> "Building restriction";
        };
    }

    private static String exemptionName(ModerationRepository.ControlType type) {
        return switch (type) {
            case MUTE -> "mute";
            case FREEZE -> "freeze";
            case INVENTORY_LOCK -> "invlock";
            case BUILD_LOCK -> "disablebuilding";
        };
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
}
