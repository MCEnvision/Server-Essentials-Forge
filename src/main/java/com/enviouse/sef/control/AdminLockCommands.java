package com.enviouse.sef.control;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.permissions.PermissionService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class AdminLockCommands {
    private static final UUID CONSOLE_ID = new UUID(0L, 0L);
    private static final int PAGE_SIZE = 8;

    private AdminLockCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("adminlock")
                .requires(source -> hasAny(
                        source,
                        "commands.adminlock.status.self",
                        "commands.adminlock.status.others",
                        "commands.adminlock.lock",
                        "commands.adminlock.unlock",
                        "commands.adminlock.challenge",
                        "commands.adminlock.session.open",
                        "commands.adminlock.session.close",
                        "commands.adminlock.require",
                        "commands.adminlock.release",
                        "commands.adminlock.invalidate",
                        "commands.adminlock.breakglass.status",
                        "commands.adminlock.breakglass.open",
                        "commands.adminlock.breakglass.close",
                        "commands.adminlock.breakglass.profile",
                        "commands.adminlock.history.self",
                        "commands.adminlock.history.others"))
                .then(Commands.literal("status")
                        .requires(source -> hasAny(
                                source,
                                "commands.adminlock.status.self",
                                "commands.adminlock.status.others"))
                        .executes(context -> statusSelf(context.getSource()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .requires(source -> has(source, "commands.adminlock.status.others"))
                                .executes(context -> status(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("lock")
                        .requires(source -> has(source, "commands.adminlock.lock"))
                        .executes(context -> lock(context.getSource(), ""))
                        .then(Commands.argument("reason", StringArgumentType.greedyString())
                                .executes(context -> lock(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "reason")))))
                .then(Commands.literal("unlock")
                        .requires(source -> has(source, "commands.adminlock.unlock"))
                        .executes(context -> unlock(context.getSource())))
                .then(Commands.literal("challenge")
                        .requires(source -> has(source, "commands.adminlock.challenge"))
                        .executes(context -> challenge(context.getSource())))
                .then(Commands.literal("session")
                        .then(Commands.literal("open")
                                .requires(source -> has(source, "commands.adminlock.session.open"))
                                .then(Commands.argument("duration", StringArgumentType.word())
                                        .then(Commands.argument("reason", StringArgumentType.greedyString())
                                                .executes(context -> openSession(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "duration"),
                                                        StringArgumentType.getString(context, "reason"))))))
                        .then(Commands.literal("close")
                                .requires(source -> has(source, "commands.adminlock.session.close"))
                                .executes(context -> closeSession(context.getSource()))))
                .then(Commands.literal("require")
                        .requires(source -> has(source, "commands.adminlock.require"))
                        .then(Commands.argument("accessClass", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        Arrays.stream(CommandDefinition.AccessClass.values())
                                                .filter(CommandDefinition.AccessClass::isPrivileged)
                                                .map(value -> value.name().toLowerCase(Locale.ROOT)),
                                        builder))
                                .executes(context -> require(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "accessClass"),
                                        true))
                                .then(Commands.argument("required", BoolArgumentType.bool())
                                        .executes(context -> require(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "accessClass"),
                                                BoolArgumentType.getBool(context, "required"))))))
                .then(Commands.literal("release")
                        .requires(source -> has(source, "commands.adminlock.release"))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("reason", StringArgumentType.greedyString())
                                        .executes(context -> release(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "player"),
                                                StringArgumentType.getString(context, "reason"))))))
                .then(Commands.literal("invalidate")
                        .requires(source -> has(source, "commands.adminlock.invalidate"))
                        .then(Commands.literal("all")
                                .then(Commands.argument("reason", StringArgumentType.greedyString())
                                        .executes(context -> invalidate(
                                                context.getSource(),
                                                null,
                                                StringArgumentType.getString(context, "reason")))))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("reason", StringArgumentType.greedyString())
                                        .executes(context -> invalidate(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "player"),
                                                StringArgumentType.getString(context, "reason"))))))
                .then(breakGlassNode())
                .then(Commands.literal("history")
                        .requires(source -> hasAny(
                                source,
                                "commands.adminlock.history.self",
                                "commands.adminlock.history.others"))
                        .executes(context -> historySelf(context.getSource()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .requires(source -> has(source, "commands.adminlock.history.others"))
                                .executes(context -> history(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "player"),
                                        1)))));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> breakGlassNode() {
        return Commands.literal("breakglass")
                .then(Commands.literal("status")
                        .requires(source -> has(source, "commands.adminlock.breakglass.status"))
                        .executes(context -> breakGlassStatus(context.getSource())))
                .then(Commands.literal("open")
                        .requires(source -> PermissionService.isConsole(source)
                                && has(source, "commands.adminlock.breakglass.open"))
                        .then(Commands.argument("profile", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        KernelServices.adminLockRepository().profiles().stream()
                                                .filter(AdminLockRepository.BreakGlassProfile::active)
                                                .map(AdminLockRepository.BreakGlassProfile::id),
                                        builder))
                                .then(Commands.argument("duration", StringArgumentType.word())
                                        .then(Commands.argument("incident", StringArgumentType.word())
                                                .executes(context -> openBreakGlass(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "profile"),
                                                        StringArgumentType.getString(context, "duration"),
                                                        StringArgumentType.getString(context, "incident")))))))
                .then(Commands.literal("close")
                        .requires(source -> PermissionService.isConsole(source)
                                && has(source, "commands.adminlock.breakglass.close"))
                        .then(Commands.argument("session", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        KernelServices.adminLocks().breakGlassSessions().stream()
                                                .map(session -> session.id().toString()),
                                        builder))
                                .executes(context -> closeBreakGlass(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "session")))))
                .then(Commands.literal("profile")
                        .requires(source -> PermissionService.isConsole(source)
                                && has(source, "commands.adminlock.breakglass.profile"))
                        .then(Commands.literal("publish")
                                .then(Commands.argument("profile", StringArgumentType.word())
                                        .then(Commands.argument("maximumDuration", StringArgumentType.word())
                                                .then(Commands.argument("classes", StringArgumentType.greedyString())
                                                        .executes(context -> publishBreakGlassProfile(
                                                                context.getSource(),
                                                                StringArgumentType.getString(context, "profile"),
                                                                StringArgumentType.getString(
                                                                        context,
                                                                        "maximumDuration"),
                                                                StringArgumentType.getString(
                                                                        context,
                                                                        "classes"))))))));
    }

    private static int statusSelf(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return fail(source, "specify an online player");
        }
        if (!has(source, "commands.adminlock.status.self")) {
            return fail(source, "administrative lock status permission is required");
        }
        return status(source, player);
    }

    private static int status(CommandSourceStack source, ServerPlayer player) {
        if (!player.getUUID().equals(actorId(source)) && !mayTarget(source, player)) {
            return 0;
        }
        AdminLockService.Status status = KernelServices.adminLocks().status(player.getUUID());
        info(source, "&eadministrative lock for &f" + player.getGameProfile().getName());
        info(source, "&7locked &f" + status.locked() + " &8| &7revision &f" + status.revision());
        if (!status.reason().isBlank()) {
            info(source, "&7reason &f" + status.reason());
        }
        status.session().ifPresent(session ->
                info(source, "&7session &f" + session.id() + " &8| &7expires &f" + session.expiresAt()));
        info(source, "&7required classes &f" + KernelServices.adminLockRepository().requiredClasses().stream()
                .map(value -> value.name().toLowerCase(Locale.ROOT))
                .sorted()
                .collect(Collectors.joining(", ")));
        return 1;
    }

    private static int lock(CommandSourceStack source, String reason) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return fail(source, "only players can self lock");
        }
        return execute(
                source,
                "sef:adminlock.lock",
                "commands.adminlock.lock",
                Map.of(),
                List.of(player.getUUID()),
                () -> {
                    var result = KernelServices.adminLocks().lock(
                            player.getUUID(),
                            player.getUUID(),
                            reason);
                    return finish(source, result, "administrative account locked");
                });
    }

    private static int challenge(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return fail(source, "only players can use a local challenge");
        }
        return execute(
                source,
                "sef:adminlock.challenge",
                "commands.adminlock.challenge",
                Map.of(),
                List.of(player.getUUID()),
                () -> {
                    var result = KernelServices.adminLocks().challenge(player.getUUID());
                    if (!result.successful()) {
                        return fail(source, result.detail());
                    }
                    success(source, "local confirmation is ready until " + result.value().expiresAt()
                            + ", this is not identity reauthentication");
                    return 1;
                });
    }

    private static int unlock(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return fail(source, "use release with an online player from console");
        }
        return execute(
                source,
                "sef:adminlock.unlock",
                "commands.adminlock.unlock",
                Map.of(),
                List.of(player.getUUID()),
                () -> finish(
                        source,
                        KernelServices.adminLocks().unlock(player.getUUID(), player.getUUID(), false),
                        "administrative account unlocked"));
    }

    private static int openSession(CommandSourceStack source, String durationText, String reason) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return fail(source, "only players can open a privileged session");
        }
        Duration duration;
        try {
            duration = parseDuration(durationText, AdminLockService.MAXIMUM_SESSION_DURATION);
        } catch (IllegalArgumentException exception) {
            return fail(source, exception.getMessage());
        }
        return execute(
                source,
                "sef:adminlock.session.open",
                "commands.adminlock.session.open",
                Map.of("duration_seconds", Long.toString(duration.toSeconds())),
                List.of(player.getUUID()),
                () -> finish(
                        source,
                        KernelServices.adminLocks().openSession(
                                player.getUUID(),
                                player.getUUID(),
                                duration,
                                reason),
                        "privileged session opened"));
    }

    private static int closeSession(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return fail(source, "only players can close their privileged session");
        }
        return execute(
                source,
                "sef:adminlock.session.close",
                "commands.adminlock.session.close",
                Map.of(),
                List.of(player.getUUID()),
                () -> finish(
                        source,
                        KernelServices.adminLocks().closeSession(
                                player.getUUID(),
                                player.getUUID(),
                                "self close"),
                        "privileged session closed"));
    }

    private static int require(CommandSourceStack source, String classText, boolean required) {
        CommandDefinition.AccessClass accessClass;
        try {
            accessClass = CommandDefinition.AccessClass.valueOf(classText.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return fail(source, "administrative access class is invalid");
        }
        return execute(
                source,
                "sef:adminlock.require",
                "commands.adminlock.require",
                Map.of(
                        "access_class", accessClass.name().toLowerCase(Locale.ROOT),
                        "required", Boolean.toString(required)),
                List.of(),
                () -> finish(
                        source,
                        KernelServices.adminLockRepository().commit(() ->
                                KernelServices.adminLockRepository().require(
                                        accessClass,
                                        required,
                                        actorId(source))),
                        "administrative session requirement updated"));
    }

    private static int release(CommandSourceStack source, ServerPlayer target, String reason) {
        if (!mayTarget(source, target)) {
            return 0;
        }
        return execute(
                source,
                "sef:adminlock.release",
                "commands.adminlock.release",
                Map.of("subject", target.getUUID().toString()),
                List.of(target.getUUID()),
                () -> finish(
                        source,
                        KernelServices.adminLocks().unlock(
                                target.getUUID(),
                                actorId(source),
                                true),
                        "administrative account released"));
    }

    private static int invalidate(CommandSourceStack source, ServerPlayer target, String reason) {
        if (target != null && !mayTarget(source, target)) {
            return 0;
        }
        List<UUID> targets = target == null ? List.of() : List.of(target.getUUID());
        return execute(
                source,
                "sef:adminlock.invalidate",
                "commands.adminlock.invalidate",
                Map.of("scope", target == null ? "all" : "player"),
                targets,
                () -> {
                    int count = KernelServices.adminLocks().invalidate(
                            target == null ? null : target.getUUID(),
                            actorId(source),
                            reason);
                    success(source, "invalidated " + count + " privileged sessions");
                    return 1;
                });
    }

    private static int breakGlassStatus(CommandSourceStack source) {
        List<AdminLockService.BreakGlassSession> sessions = KernelServices.adminLocks().breakGlassSessions();
        info(source, "&ebreak glass sessions &f" + sessions.size());
        sessions.stream().limit(PAGE_SIZE).forEach(session ->
                info(source, "&e" + session.id()
                        + " &8| &f" + session.profileId()
                        + " &8| &7incident &f" + session.incidentId()
                        + " &8| &7expires &f" + session.expiresAt()));
        return 1;
    }

    private static int publishBreakGlassProfile(
            CommandSourceStack source,
            String profileId,
            String maximumDurationText,
            String classList
    ) {
        Duration duration;
        Set<CommandDefinition.AccessClass> classes;
        try {
            duration = parseDuration(maximumDurationText, Duration.ofHours(1));
            classes = Arrays.stream(classList.split("[,\\s]+"))
                    .filter(value -> !value.isBlank())
                    .map(value -> CommandDefinition.AccessClass.valueOf(value.toUpperCase(Locale.ROOT)))
                    .collect(Collectors.toUnmodifiableSet());
        } catch (IllegalArgumentException exception) {
            return fail(source, "break glass duration or access classes are invalid");
        }
        return execute(
                source,
                "sef:adminlock.breakglass.profile",
                "commands.adminlock.breakglass.profile",
                Map.of("profile", profileId),
                List.of(),
                () -> finish(
                        source,
                        KernelServices.adminLockRepository().commit(() ->
                                KernelServices.adminLockRepository().publishProfile(
                                        profileId,
                                        classes,
                                        duration.toSeconds(),
                                        CONSOLE_ID)),
                        "break glass profile published"));
    }

    private static int openBreakGlass(
            CommandSourceStack source,
            String profileId,
            String durationText,
            String incidentId
    ) {
        Duration duration;
        try {
            duration = parseDuration(durationText, Duration.ofHours(1));
        } catch (IllegalArgumentException exception) {
            return fail(source, exception.getMessage());
        }
        return execute(
                source,
                "sef:adminlock.breakglass.open",
                "commands.adminlock.breakglass.open",
                Map.of("profile", profileId, "incident", incidentId),
                List.of(),
                () -> finish(
                        source,
                        KernelServices.adminLocks().openBreakGlass(profileId, duration, incidentId),
                        "break glass session opened"));
    }

    private static int closeBreakGlass(CommandSourceStack source, String sessionId) {
        UUID id;
        try {
            id = UUID.fromString(sessionId);
        } catch (IllegalArgumentException exception) {
            return fail(source, "break glass session id is invalid");
        }
        return execute(
                source,
                "sef:adminlock.breakglass.close",
                "commands.adminlock.breakglass.close",
                Map.of("session", id.toString()),
                List.of(),
                () -> finish(
                        source,
                        KernelServices.adminLocks().closeBreakGlass(id),
                        "break glass session closed"));
    }

    private static int historySelf(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return fail(source, "specify an online player");
        }
        if (!has(source, "commands.adminlock.history.self")) {
            return fail(source, "administrative lock history permission is required");
        }
        return history(source, player, 1);
    }

    private static int history(CommandSourceStack source, ServerPlayer target, int requestedPage) {
        if (!target.getUUID().equals(actorId(source)) && !mayTarget(source, target)) {
            return 0;
        }
        List<AdminLockRepository.HistoryEntry> values =
                KernelServices.adminLockRepository().history(target.getUUID());
        int pages = Math.max(1, (values.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int page = Math.max(1, Math.min(requestedPage, pages));
        info(source, "&eadministrative lock history &8| &7page &f" + page + "&7/&f" + pages);
        values.stream().skip((long) (page - 1) * PAGE_SIZE).limit(PAGE_SIZE).forEach(entry ->
                info(source, "&e" + entry.occurredAt()
                        + " &8| &f" + entry.action()
                        + " &8| &7" + entry.detail()));
        return 1;
    }

    private static boolean mayTarget(CommandSourceStack source, ServerPlayer target) {
        var decision = PlayerTargetPolicy.decide(
                source,
                target,
                permission("commands.adminlock.hierarchy.override"),
                permission("commands.adminlock.exempt"),
                permission("commands.adminlock.exemption.override"),
                false,
                false);
        if (!decision.allowed()) {
            fail(source, decision.exempt()
                    ? "that player is exempt from administrative lock targeting"
                    : "you cannot target that player");
            return false;
        }
        return true;
    }

    private static Duration parseDuration(String value, Duration maximum) {
        String normalized = value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("duration is required");
        }
        long multiplier = 1L;
        char suffix = normalized.charAt(normalized.length() - 1);
        if (Character.isLetter(suffix)) {
            normalized = normalized.substring(0, normalized.length() - 1);
            multiplier = switch (suffix) {
                case 's' -> 1L;
                case 'm' -> 60L;
                case 'h' -> 3_600L;
                default -> throw new IllegalArgumentException("duration unit must be s, m, or h");
            };
        }
        try {
            Duration duration = Duration.ofSeconds(Math.multiplyExact(Long.parseLong(normalized), multiplier));
            if (duration.isZero() || duration.isNegative() || duration.compareTo(maximum) > 0) {
                throw new IllegalArgumentException("duration is outside bounds");
            }
            return duration;
        } catch (ArithmeticException | NumberFormatException exception) {
            throw new IllegalArgumentException("duration is invalid");
        }
    }

    private static int execute(
            CommandSourceStack source,
            String actionId,
            String permissionId,
            Map<String, String> parameters,
            List<UUID> targets,
            java.util.function.IntSupplier action
    ) {
        return KernelCommandExecutor.execute(
                source,
                actionId,
                parameters,
                targets,
                false,
                action,
                permission(permissionId));
    }

    private static int finish(CommandSourceStack source, ActionResult<?> result, String message) {
        if (!result.successful()) {
            return fail(source, result.detail());
        }
        success(source, message);
        return 1;
    }

    private static UUID actorId(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        return player == null ? CONSOLE_ID : player.getUUID();
    }

    private static PermissionNode<Boolean> permission(String permissionId) {
        return PermissionsHandler.phasePermission(permissionId);
    }

    private static boolean has(CommandSourceStack source, String permissionId) {
        PermissionNode<Boolean> node = permission(permissionId);
        return node != null && PermissionService.has(source, node);
    }

    private static boolean hasAny(CommandSourceStack source, String... permissionIds) {
        return Arrays.stream(permissionIds).anyMatch(permission -> has(source, permission));
    }

    private static int fail(CommandSourceStack source, String message) {
        source.sendFailure(TextFormatter.stringToFormattedText("&c" + message));
        return 0;
    }

    private static void success(CommandSourceStack source, String message) {
        source.sendSuccess(() -> TextFormatter.stringToFormattedText("&a" + message), false);
    }

    private static void info(CommandSourceStack source, String message) {
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(message), false);
    }
}
