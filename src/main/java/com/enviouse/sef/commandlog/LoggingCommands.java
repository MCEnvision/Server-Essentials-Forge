package com.enviouse.sef.commandlog;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.identity.IdentityArguments;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.kernel.observation.ObservationContracts;
import com.enviouse.sef.kernel.policy.ConfirmationService;
import com.enviouse.sef.permissions.PermissionService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class LoggingCommands {
    private static final List<PermissionNode<Boolean>> LOGGING_ACTION_PERMISSIONS =
            PermissionsHandler.phaseSixSevenNodes.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith("sef.commands.logging."))
                    .map(Map.Entry::getValue)
                    .toList();

    private LoggingCommands() {
    }

    public static void attachCanonical(LiteralArgumentBuilder<CommandSourceStack> sefRoot) {
        sefRoot.then(node("logging", false));
    }

    public static void registerAlias(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (KernelServices.shortcuts().isActive("loggerspy")) {
            dispatcher.register(node("loggerspy", true));
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> node(String literal, boolean alias) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(literal)
                .requires(source -> (!alias || has(source, "commands.loggerspy"))
                        && hasAnyLoggingAction(source));
        root.then(Commands.literal("status")
                .requires(source -> has(source, "commands.logging.status"))
                .executes(context -> status(context.getSource())));
        root.then(Commands.literal("enable")
                .requires(source -> has(source, "commands.logging.enable"))
                .executes(context -> enable(context.getSource())));
        root.then(Commands.literal("disable")
                .requires(source -> has(source, "commands.logging.disable"))
                .executes(context -> disable(context.getSource())));
        root.then(Commands.literal("reload")
                .requires(source -> has(source, "commands.logging.enable"))
                .executes(context -> reload(context.getSource())));
        root.then(Commands.literal("rotate")
                .requires(source -> has(source, "commands.logging.rotate"))
                .executes(context -> rotate(context.getSource()))
                .then(Commands.argument("stream", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                new String[]{"commands"},
                                builder))
                        .executes(context -> rotate(context.getSource()))));
        root.then(Commands.literal("flush")
                .requires(source -> has(source, "commands.logging.flush"))
                .executes(context -> flush(context.getSource())));
        root.then(Commands.literal("stats")
                .requires(source -> has(source, "commands.logging.stats"))
                .executes(context -> stats(context.getSource())));
        root.then(Commands.literal("doctor")
                .requires(source -> has(source, "commands.logging.doctor"))
                .executes(context -> doctor(context.getSource())));
        root.then(liveNode());
        root.then(recentNode());
        root.then(streamNode());
        root.then(filterNode());
        root.then(sessionNode());
        root.then(formatNode());
        root.then(Commands.literal("tail")
                .requires(source -> has(source, "commands.logging.tail"))
                .then(Commands.argument("stream", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                new String[]{"commands"},
                                builder))
                        .executes(context -> tail(context.getSource(), 10))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 100))
                                .executes(context -> tail(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "count"))))));
        root.then(Commands.literal("search")
                .requires(source -> has(source, "commands.logging.search"))
                .then(Commands.literal("commands")
                        .executes(context -> search(context.getSource(), "", 20))
                        .then(Commands.argument("root", StringArgumentType.word())
                                .executes(context -> search(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "root"),
                                        20))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 100))
                                        .executes(context -> search(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "root"),
                                                IntegerArgumentType.getInteger(context, "count")))))));
        root.then(Commands.literal("export")
                .requires(source -> has(source, "commands.logging.export"))
                .then(Commands.literal("commands")
                        .executes(context -> export(context.getSource(), 100))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 4096))
                                .executes(context -> export(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "count"))))));
        root.then(retentionNode());
        root.then(Commands.literal("repair")
                .then(Commands.literal("acknowledge")
                        .requires(source -> has(source, "commands.logging.repair"))
                        .executes(context -> repair(context.getSource()))));
        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> liveNode() {
        return Commands.literal("live")
                .requires(source -> has(source, "commands.logging.live"))
                .executes(context -> live(context.getSource(), null))
                .then(booleanArgument("state")
                        .executes(context -> live(
                                context.getSource(),
                                parseBoolean(StringArgumentType.getString(context, "state")))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> recentNode() {
        return Commands.literal("recent")
                .requires(source -> has(source, "commands.logging.recent"))
                .then(Commands.literal("commands")
                        .executes(context -> tail(context.getSource(), 10))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 100))
                                .executes(context -> tail(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "count")))))
                .then(Commands.literal("connections")
                        .executes(context -> recentConnections(context.getSource(), 10))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 100))
                                .executes(context -> recentConnections(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "count")))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> streamNode() {
        return Commands.literal("stream")
                .then(Commands.literal("list")
                        .requires(source -> has(source, "commands.logging.stream.list"))
                        .executes(context -> streams(context.getSource())))
                .then(Commands.literal("enable")
                        .requires(source -> has(source, "commands.logging.stream.configure"))
                        .then(Commands.argument("stream", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        new String[]{"commands", "connection_events"},
                                        builder))
                                .executes(context -> stream(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "stream"),
                                        true))))
                .then(Commands.literal("disable")
                        .requires(source -> has(source, "commands.logging.stream.configure"))
                        .then(Commands.argument("stream", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        new String[]{"commands", "connection_events"},
                                        builder))
                                .executes(context -> stream(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "stream"),
                                        false))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> filterNode() {
        LiteralArgumentBuilder<CommandSourceStack> filter = Commands.literal("filter")
                .then(Commands.literal("list")
                        .requires(source -> has(source, "commands.logging.filter.list"))
                        .executes(context -> filterList(context.getSource())));
        for (String scope : new String[]{"capture", "view"}) {
            filter.then(Commands.literal("reset")
                    .then(Commands.literal(scope)
                            .requires(source -> has(
                                    source,
                                    scope.equals("capture")
                                            ? "commands.logging.filter.capture"
                                            : "commands.logging.filter.view"))
                            .executes(context -> filterReset(context.getSource(), scope))));
            filter.then(Commands.literal("mode")
                    .then(Commands.literal(scope)
                            .requires(source -> has(
                                    source,
                                    scope.equals("capture")
                                            ? "commands.logging.filter.capture"
                                            : "commands.logging.filter.view"))
                            .then(Commands.argument("mode", StringArgumentType.word())
                                    .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                            new String[]{"all", "include"},
                                            builder))
                                    .executes(context -> filterMode(
                                            context.getSource(),
                                            scope,
                                            StringArgumentType.getString(context, "mode"))))));
            for (String kind : new String[]{"root", "action"}) {
                filter.then(Commands.literal(kind)
                        .requires(source -> has(
                                source,
                                kind.equals("root")
                                        ? "commands.logging.filter.root"
                                        : "commands.logging.filter.action"))
                        .then(Commands.literal(scope)
                                .then(Commands.argument("mode", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                new String[]{"include", "exclude"},
                                                builder))
                                        .then(Commands.argument("value", StringArgumentType.word())
                                                .executes(context -> filterValue(
                                                        context.getSource(),
                                                        scope,
                                                        StringArgumentType.getString(context, "mode"),
                                                        kind,
                                                        StringArgumentType.getString(context, "value")))))));
            }
            filter.then(Commands.literal("player")
                    .requires(source -> has(source, "commands.logging.filter.player"))
                    .then(Commands.literal(scope)
                            .then(Commands.argument("mode", StringArgumentType.word())
                                    .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                            new String[]{"include", "exclude"},
                                            builder))
                                    .then(IdentityArguments.online("player")
                                            .executes(context -> filterPlayer(
                                                    context.getSource(),
                                                    scope,
                                                    StringArgumentType.getString(context, "mode"),
                                                    IdentityArguments.getOnline(context, "player")))))));
            for (String kind : new String[]{"source", "result", "world", "origin"}) {
                var value = Commands.argument("value", StringArgumentType.word());
                String[] suggestions = typedSuggestions(kind);
                if (suggestions.length > 0) {
                    value.suggests((context, builder) -> SharedSuggestionProvider.suggest(suggestions, builder));
                }
                filter.then(Commands.literal(kind)
                        .requires(source -> has(source, "commands.logging.filter." + kind))
                        .then(Commands.literal(scope)
                                .then(value.then(booleanArgument("state")
                                        .executes(context -> filterTyped(
                                                context.getSource(),
                                                scope,
                                                kind,
                                                StringArgumentType.getString(context, "value"),
                                                StringArgumentType.getString(context, "state")))))));
            }
        }
        return filter;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> sessionNode() {
        return Commands.literal("session")
                .then(Commands.literal("current")
                        .requires(source -> has(source, "commands.logging.session.current"))
                        .executes(context -> currentSession(context.getSource())))
                .then(Commands.literal("list")
                        .requires(source -> has(source, "commands.logging.session.list"))
                        .executes(context -> sessions(context.getSource())));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> formatNode() {
        return Commands.literal("format")
                .then(Commands.literal("show")
                        .requires(source -> has(source, "commands.logging.format.show"))
                        .then(Commands.argument("stream", StringArgumentType.word())
                                .executes(context -> formatShow(context.getSource()))))
                .then(Commands.literal("validate")
                        .requires(source -> has(source, "commands.logging.format.validate"))
                        .then(Commands.argument("stream", StringArgumentType.word())
                                .then(Commands.argument("template", StringArgumentType.greedyString())
                                        .executes(context -> formatValidate(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "template"))))))
                .then(Commands.literal("set")
                        .requires(source -> has(source, "commands.logging.format.set"))
                        .then(Commands.argument("stream", StringArgumentType.word())
                                .then(Commands.argument("template", StringArgumentType.greedyString())
                                        .executes(context -> formatSet(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "template"))))))
                .then(Commands.literal("reset")
                        .requires(source -> has(source, "commands.logging.format.reset"))
                        .then(Commands.argument("stream", StringArgumentType.word())
                                .executes(context -> formatReset(context.getSource()))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> retentionNode() {
        return Commands.literal("retention")
                .then(Commands.literal("preview")
                        .requires(source -> has(source, "commands.logging.retention.preview"))
                        .executes(context -> retentionPreview(context.getSource())))
                .then(Commands.literal("run")
                        .requires(source -> has(source, "commands.logging.retention.run"))
                        .executes(context -> retentionRun(context.getSource(), null))
                        .then(Commands.literal("confirm")
                                .then(Commands.argument("token", StringArgumentType.word())
                                        .executes(context -> retentionRun(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "token"))))));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> booleanArgument(
            String name
    ) {
        return Commands.argument(name, StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                        new String[]{"on", "off", "toggle", "status"},
                        builder));
    }

    private static int status(CommandSourceStack source) {
        return execute(source, "sef:logging.status", Map.of(), () -> {
            FileLogSink.Health health = KernelServices.fileLogs().health();
            info(source, "Optional file logging state "
                    + health.state().name().toLowerCase(Locale.ROOT) + ".");
            info(source, "Queue " + health.queueDepth() + " of " + health.queueCapacity()
                    + ", accepted " + health.accepted() + ", written " + health.written()
                    + ", dropped " + health.dropped() + ".");
            if (!health.detail().isBlank()) {
                info(source, "Health detail, " + health.detail() + ".");
            }
            return 1;
        }, permission("commands.logging.status"));
    }

    private static int enable(CommandSourceStack source) {
        return execute(source, "sef:logging.enable", Map.of("enabled", "true"), () -> {
            if (!KernelServices.fileLogs().enable()) {
                return fail(source, "Optional file logging could not start. Check server diagnostics.");
            }
            success(source, "Optional file logging enabled for this runtime.");
            return 1;
        }, permission("commands.logging.enable"));
    }

    private static int disable(CommandSourceStack source) {
        return execute(source, "sef:logging.disable", Map.of("enabled", "false"), () -> {
            KernelServices.fileLogs().disable();
            success(source, "Optional file logging disabled and flushed.");
            return 1;
        }, permission("commands.logging.disable"));
    }

    private static int reload(CommandSourceStack source) {
        return execute(source, "sef:logging.reload", Map.of("operation", "reload"), () -> {
            if (!KernelServices.fileLogs().reload()) {
                return fail(source, "Optional file logging reload failed. The previous safe state remains.");
            }
            success(source, "Optional file logging configuration reloaded.");
            return 1;
        }, permission("commands.logging.enable"));
    }

    private static int rotate(CommandSourceStack source) {
        return execute(source, "sef:logging.rotate", Map.of("stream", "commands"), () -> {
            KernelServices.fileLogs().requestRotate();
            success(source, "Command log rotation requested.");
            return 1;
        }, permission("commands.logging.rotate"));
    }

    private static int flush(CommandSourceStack source) {
        return execute(source, "sef:logging.flush", Map.of(), () -> {
            KernelServices.fileLogs().requestFlush();
            success(source, "Optional log flush requested.");
            return 1;
        }, permission("commands.logging.flush"));
    }

    private static int stats(CommandSourceStack source) {
        return execute(source, "sef:logging.stats", Map.of(), () -> {
            FileLogSink.Health health = KernelServices.fileLogs().health();
            info(source, "Queue " + health.queueDepth() + " of " + health.queueCapacity()
                    + ", accepted " + health.accepted() + ", written " + health.written()
                    + ", dropped " + health.dropped() + ", failures " + health.failures() + ".");
            return 1;
        }, permission("commands.logging.stats"));
    }

    private static int doctor(CommandSourceStack source) {
        return execute(source, "sef:logging.doctor", Map.of(), () -> {
            FileLogSink.Health health = KernelServices.fileLogs().health();
            boolean healthy = health.state() == FileLogSink.State.HEALTHY
                    || health.state() == FileLogSink.State.DISABLED;
            info(source, healthy ? "No optional logger errors detected."
                    : "Optional logger requires operator attention.");
            info(source, "Failures " + health.failures() + ", rotations " + health.rotations() + ".");
            return healthy ? 1 : 0;
        }, permission("commands.logging.doctor"));
    }

    private static int live(CommandSourceStack source, Boolean requested) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return fail(source, "Live observation requires a player.");
        }
        return execute(source, "sef:logging.live", Map.of("requested", String.valueOf(requested)), () -> {
            CommandSpyRepository.Profile profile = KernelServices.commandSpies().profile(player.getUUID());
            if (requested == null) {
                info(source, "Logger live mapping is " + profile.enabled() + ".");
                return 1;
            }
            if (requested && !CommandSpyCommands.canActivate(player, profile)) {
                return fail(source, "The saved command spy audience or source scope is not authorized.");
            }
            KernelServices.commandSpies().put(profile.withEnabled(requested));
            success(source, "Logger live mapping " + (requested ? "enabled" : "disabled") + ".");
            return 1;
        }, permission("commands.logging.live"));
    }

    private static int streams(CommandSourceStack source) {
        return execute(source, "sef:logging.stream.list", Map.of("operation", "list"), () -> {
            info(source, "commands, " + KernelServices.fileLogs().health().accepting() + ".");
            info(source, "connection_events, " + KernelServices.fileLogs().connectionStreamEnabled() + ".");
            return 1;
        }, permission("commands.logging.stream.list"));
    }

    private static int stream(CommandSourceStack source, String stream, boolean enabled) {
        String normalized = stream.toLowerCase(Locale.ROOT);
        return execute(source, "sef:logging.stream.configure", Map.of(
                "stream", normalized,
                "enabled", Boolean.toString(enabled)), () -> {
            if (normalized.equals("commands")) {
                if (enabled && !KernelServices.fileLogs().enable()) {
                    return fail(source, "The command stream could not start.");
                }
                if (!enabled) {
                    KernelServices.fileLogs().disable();
                }
                success(source, "Command stream " + (enabled ? "enabled" : "disabled") + ".");
                return 1;
            }
            if (normalized.equals("connection_events")) {
                if (enabled && !KernelServices.fileLogs().health().accepting()
                        && !KernelServices.fileLogs().enable()) {
                    return fail(source, "The connection event stream could not start.");
                }
                if (!KernelServices.fileLogs().setConnectionStreamEnabled(enabled)) {
                    return fail(source, "The connection event stream could not be changed.");
                }
                success(source, "Connection event stream " + (enabled ? "enabled" : "disabled") + ".");
                return 1;
            }
            return fail(source, "That stream is not active in this configuration.");
        }, permission("commands.logging.stream.configure"));
    }

    private static int recentConnections(CommandSourceStack source, int count) {
        return execute(source, "sef:logging.recent", Map.of(
                "stream", "connection_events",
                "count", Integer.toString(count)), () -> {
            var records = KernelServices.fileLogs().recentConnections(count);
            info(source, "Recent durable redacted connection records, " + records.size() + ".");
            for (var record : records) {
                info(source, record.timestamp() + ", " + record.playerName() + ", "
                        + record.type().name().toLowerCase(Locale.ROOT) + ", "
                        + record.redactedAddress() + ".");
            }
            return 1;
        }, permission("commands.logging.recent"));
    }

    private static int tail(CommandSourceStack source, int count) {
        return execute(source, "sef:logging.recent", Map.of("count", Integer.toString(count)), () -> {
            var records = KernelServices.fileLogs().recent(count);
            info(source, "Recent durable redacted command records, " + records.size() + ".");
            for (var record : records) {
                info(source, record.timestamp() + ", " + record.actorName() + ", /"
                        + record.root() + ", " + record.stage().name().toLowerCase(Locale.ROOT) + ".");
            }
            return 1;
        }, permission("commands.logging.recent"));
    }

    private static int search(CommandSourceStack source, String root, int count) {
        String normalized = root.toLowerCase(Locale.ROOT);
        return execute(source, "sef:logging.search", Map.of("root", normalized, "count",
                Integer.toString(count)), () -> {
            var records = KernelServices.fileLogs().recent(256).stream()
                    .filter(record -> normalized.isBlank() || record.root().equals(normalized))
                    .limit(count)
                    .toList();
            info(source, "Bounded redacted search results, " + records.size() + ".");
            for (var record : records) {
                info(source, record.eventId() + ", " + record.actorName() + ", /"
                        + record.root() + ", " + record.stage().name().toLowerCase(Locale.ROOT) + ".");
            }
            return 1;
        }, permission("commands.logging.search"));
    }

    private static int export(CommandSourceStack source, int count) {
        return execute(source, "sef:logging.export", Map.of("count", Integer.toString(count)), () -> {
            try {
                KernelServices.fileLogs().exportRecent(count);
                success(source, "Bounded redacted command export created in the owned export area.");
                return 1;
            } catch (IOException exception) {
                return fail(source, "The bounded export could not be created.");
            }
        }, permission("commands.logging.export"));
    }

    private static int currentSession(CommandSourceStack source) {
        return execute(source, "sef:logging.session.current", Map.of("view", "current"), () -> {
            FileLogSink.Health health = KernelServices.fileLogs().health();
            info(source, "Current session " + health.sessionId() + ", since " + health.activeSince() + ".");
            return 1;
        }, permission("commands.logging.session.current"));
    }

    private static int sessions(CommandSourceStack source) {
        return execute(source, "sef:logging.session.list", Map.of("view", "list"), () -> {
            FileLogSink.RetentionPreview preview = KernelServices.fileLogs().retentionPreview();
            info(source, "Current session " + KernelServices.fileLogs().health().sessionId() + ".");
            info(source, "Owned command archives " + preview.archives() + ".");
            return 1;
        }, permission("commands.logging.session.list"));
    }

    private static int filterList(CommandSourceStack source) {
        return execute(source, "sef:logging.filter.list", Map.of("operation", "list"), () -> {
            FileLogSink.CaptureFilter filter = KernelServices.fileLogs().captureFilter();
            info(source, "Capture mode " + filter.mode().name().toLowerCase(Locale.ROOT) + ".");
            info(source, "Capture include roots " + filter.includedRoots() + ".");
            info(source, "Capture exclude roots " + filter.excludedRoots() + ".");
            info(source, "Capture include actions " + filter.includedActions() + ".");
            info(source, "Capture exclude actions " + filter.excludedActions() + ".");
            for (FileLogSink.FilterKind kind : new FileLogSink.FilterKind[]{
                    FileLogSink.FilterKind.SOURCE,
                    FileLogSink.FilterKind.PLAYER,
                    FileLogSink.FilterKind.RESULT,
                    FileLogSink.FilterKind.WORLD,
                    FileLogSink.FilterKind.ORIGIN}) {
                info(source, "Capture include " + kind.name().toLowerCase(Locale.ROOT) + " "
                        + filter.included(kind) + ".");
                info(source, "Capture exclude " + kind.name().toLowerCase(Locale.ROOT) + " "
                        + filter.excluded(kind) + ".");
            }
            ServerPlayer player = source.getPlayer();
            if (player != null) {
                CommandSpyRepository.Profile view = KernelServices.commandSpies().profile(player.getUUID());
                info(source, "View include roots " + view.includedRoots() + ".");
                info(source, "View exclude roots " + view.excludedRoots() + ".");
                info(source, "View include actions " + view.includedActions() + ".");
                info(source, "View exclude actions " + view.excludedActions() + ".");
                info(source, "View disabled sources " + view.typedFilters().disabledSources() + ".");
                info(source, "View disabled results " + view.typedFilters().disabledResults() + ".");
                info(source, "View disabled worlds " + view.typedFilters().disabledWorlds() + ".");
                info(source, "View disabled origins " + view.typedFilters().disabledOrigins() + ".");
                info(source, "View include players " + view.typedFilters().includedPlayers() + ".");
                info(source, "View exclude players " + view.typedFilters().excludedPlayers() + ".");
            }
            return 1;
        }, permission("commands.logging.filter.list"));
    }

    private static int filterReset(CommandSourceStack source, String scope) {
        return execute(source, scope.equals("capture")
                ? "sef:logging.filter.capture"
                : "sef:logging.filter.view", Map.of("operation", "reset", "scope", scope), () -> {
            if (scope.equals("capture")) {
                KernelServices.fileLogs().resetCaptureFilter();
            } else {
                ServerPlayer player = source.getPlayer();
                if (player == null) {
                    return fail(source, "Personal view filters require a player.");
                }
                CommandSpyRepository.Profile profile = KernelServices.commandSpies().profile(player.getUUID());
                KernelServices.commandSpies().put(profile
                        .withFilters(Set.of(), Set.of(), Set.of(), Set.of())
                        .withTypedFilters(CommandSpyRepository.TypedFilters.defaults()));
            }
            success(source, "Logging " + scope + " filters reset.");
            return 1;
        }, permission(scope.equals("capture")
                ? "commands.logging.filter.capture"
                : "commands.logging.filter.view"));
    }

    private static int filterMode(CommandSourceStack source, String scope, String mode) {
        return execute(source, scope.equals("capture")
                ? "sef:logging.filter.capture"
                : "sef:logging.filter.view", Map.of("operation", "mode", "scope", scope, "mode", mode), () -> {
            if (!scope.equals("capture")) {
                info(source, "View filters use explicit include sets and exclusions.");
                return 1;
            }
            FileLogSink.FilterMode parsed = mode.equalsIgnoreCase("include")
                    ? FileLogSink.FilterMode.INCLUDE
                    : FileLogSink.FilterMode.ALL;
            if (!KernelServices.fileLogs().setCaptureMode(parsed)) {
                return fail(source, "Include mode requires at least one include filter.");
            }
            success(source, "Capture filter mode updated.");
            return 1;
        }, permission(scope.equals("capture")
                ? "commands.logging.filter.capture"
                : "commands.logging.filter.view"));
    }

    private static int filterValue(
            CommandSourceStack source,
            String scope,
            String mode,
            String kind,
            String value
    ) {
        boolean include = mode.equalsIgnoreCase("include");
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return execute(source, kind.equals("root")
                ? "sef:logging.filter.root"
                : "sef:logging.filter.action", Map.of(
                "scope", scope, "mode", mode, "kind", kind, "value", normalized), () -> {
            if (scope.equals("capture")) {
                if (!KernelServices.fileLogs().addCaptureFilter(
                        include,
                        kind.equals("root") ? FileLogSink.FilterKind.ROOT : FileLogSink.FilterKind.ACTION,
                        normalized)) {
                    return fail(source, "Capture filter value is invalid or exceeds limits.");
                }
            } else {
                ServerPlayer player = source.getPlayer();
                if (player == null) {
                    return fail(source, "Personal view filters require a player.");
                }
                CommandSpyRepository.Profile profile = KernelServices.commandSpies().profile(player.getUUID());
                Set<String> includeRoots = new HashSet<>(profile.includedRoots());
                Set<String> excludeRoots = new HashSet<>(profile.excludedRoots());
                Set<String> includeActions = new HashSet<>(profile.includedActions());
                Set<String> excludeActions = new HashSet<>(profile.excludedActions());
                Set<String> destination = kind.equals("root")
                        ? include ? includeRoots : excludeRoots
                        : include ? includeActions : excludeActions;
                destination.add(normalized);
                KernelServices.commandSpies().put(profile.withFilters(
                        includeRoots, excludeRoots, includeActions, excludeActions));
            }
            success(source, "Logging filter added.");
            return 1;
        }, permission(kind.equals("root")
                ? "commands.logging.filter.root"
                : "commands.logging.filter.action"));
    }

    private static int filterPlayer(
            CommandSourceStack source,
            String scope,
            String mode,
            ServerPlayer target
    ) {
        if (!mode.equalsIgnoreCase("include") && !mode.equalsIgnoreCase("exclude")) {
            return fail(source, "Player filter mode must be include or exclude.");
        }
        boolean include = mode.equalsIgnoreCase("include");
        String playerId = target.getUUID().toString();
        return execute(source, "sef:logging.filter.player", Map.of(
                "scope", scope,
                "mode", mode.toLowerCase(Locale.ROOT),
                "player", playerId), () -> {
            if (scope.equals("capture")) {
                if (!KernelServices.fileLogs().addCaptureFilter(
                        include,
                        FileLogSink.FilterKind.PLAYER,
                        playerId)) {
                    return fail(source, "Capture player filter is invalid or exceeds limits.");
                }
            } else {
                ServerPlayer observer = source.getPlayer();
                if (observer == null) {
                    return fail(source, "Personal view filters require a player.");
                }
                CommandSpyRepository.Profile profile =
                        KernelServices.commandSpies().profile(observer.getUUID());
                KernelServices.commandSpies().put(profile.withTypedFilters(
                        profile.typedFilters().withPlayer(playerId, include, true)));
            }
            success(source, "Logging player filter added.");
            return 1;
        }, permission("commands.logging.filter.player"));
    }

    private static int filterTyped(
            CommandSourceStack source,
            String scope,
            String kind,
            String input,
            String state
    ) {
        if (!state.equalsIgnoreCase("on") && !state.equalsIgnoreCase("off")) {
            return fail(source, "Typed filter state must be on or off.");
        }
        String normalized = input.trim().toLowerCase(Locale.ROOT);
        if (!validTypedFilter(kind, normalized)) {
            return fail(source, "That typed filter value is invalid.");
        }
        boolean enabled = state.equalsIgnoreCase("on");
        return execute(source, "sef:logging.filter." + kind, Map.of(
                "scope", scope,
                "value", normalized,
                "enabled", Boolean.toString(enabled)), () -> {
            if (scope.equals("capture")) {
                FileLogSink.FilterKind captureKind =
                        FileLogSink.FilterKind.valueOf(kind.toUpperCase(Locale.ROOT));
                if (!KernelServices.fileLogs().setCaptureFilterEnabled(
                        captureKind,
                        normalized,
                        enabled)) {
                    return fail(source, "Capture typed filter is invalid or exceeds limits.");
                }
            } else {
                ServerPlayer observer = source.getPlayer();
                if (observer == null) {
                    return fail(source, "Personal view filters require a player.");
                }
                CommandSpyRepository.Profile profile =
                        KernelServices.commandSpies().profile(observer.getUUID());
                KernelServices.commandSpies().put(profile.withTypedFilters(
                        profile.typedFilters().withValue(kind, normalized, enabled)));
            }
            success(source, "Logging typed filter updated.");
            return 1;
        }, permission("commands.logging.filter." + kind));
    }

    private static String[] typedSuggestions(String kind) {
        return switch (kind) {
            case "source" -> java.util.Arrays.stream(CommandDefinition.SourceType.values())
                    .map(value -> value.name().toLowerCase(Locale.ROOT))
                    .toArray(String[]::new);
            case "result" -> java.util.Arrays.stream(ObservationContracts.LifecycleStage.values())
                    .map(value -> value.name().toLowerCase(Locale.ROOT))
                    .toArray(String[]::new);
            case "origin" -> new String[]{"player", "console", "rcon", "command_block", "function",
                    "scheduler", "panel", "bundle", "sudo", "execution_profile", "run_server",
                    "silent_actor", "silent_server", "external_integration"};
            default -> new String[0];
        };
    }

    private static boolean validTypedFilter(String kind, String value) {
        if (value.isBlank() || value.length() > 128) {
            return false;
        }
        return switch (kind) {
            case "source" -> java.util.Arrays.stream(CommandDefinition.SourceType.values())
                    .anyMatch(candidate -> candidate.name().equalsIgnoreCase(value));
            case "result" -> java.util.Arrays.stream(ObservationContracts.LifecycleStage.values())
                    .anyMatch(candidate -> candidate.name().equalsIgnoreCase(value));
            case "world" -> ResourceLocation.tryParse(value) != null;
            case "origin" -> value.matches("[a-z0-9_.:-]+");
            default -> false;
        };
    }

    private static int formatShow(CommandSourceStack source) {
        return execute(source, "sef:logging.format.show", Map.of("operation", "show"), () -> {
            info(source, "Command text mirror format, " + KernelServices.fileLogs().commandTextFormat());
            return 1;
        }, permission("commands.logging.format.show"));
    }

    private static int formatValidate(CommandSourceStack source, String template) {
        return execute(source, "sef:logging.format.validate", Map.of(
                "operation", "validate",
                "length", Integer.toString(template.length())), () -> {
            boolean valid = FileLogSink.validTextFormat(template);
            if (!valid) {
                return fail(source, "Text mirror template is invalid.");
            }
            success(source, "Text mirror template is valid.");
            return 1;
        }, permission("commands.logging.format.validate"));
    }

    private static int formatSet(CommandSourceStack source, String template) {
        return execute(source, "sef:logging.format.set", Map.of(
                "operation", "set",
                "length", Integer.toString(template.length())), () -> {
            if (!KernelServices.fileLogs().setCommandTextFormat(template)) {
                return fail(source, "Text mirror template is invalid.");
            }
            success(source, "Text mirror template updated for this runtime.");
            return 1;
        }, permission("commands.logging.format.set"));
    }

    private static int formatReset(CommandSourceStack source) {
        return execute(source, "sef:logging.format.reset", Map.of("operation", "reset"), () -> {
            KernelServices.fileLogs().resetCommandTextFormat();
            success(source, "Text mirror template reset.");
            return 1;
        }, permission("commands.logging.format.reset"));
    }

    private static int retentionPreview(CommandSourceStack source) {
        return execute(source, "sef:logging.retention.preview", Map.of("operation", "preview"), () -> {
            FileLogSink.RetentionPreview preview = KernelServices.fileLogs().retentionPreview();
            info(source, "Owned archives " + preview.archives() + ", bytes " + preview.bytes() + ".");
            info(source, "Oldest " + preview.oldest() + ", newest " + preview.newest() + ".");
            return 1;
        }, permission("commands.logging.retention.preview"));
    }

    private static int retentionRun(CommandSourceStack source, String token) {
        FileLogSink.RetentionPreview preview = KernelServices.fileLogs().retentionPreview();
        Map<String, String> parameters = Map.of(
                "operation", "run",
                "archives", Integer.toString(preview.archives()),
                "bytes", Long.toString(preview.bytes()),
                "oldest", String.valueOf(preview.oldest()),
                "newest", String.valueOf(preview.newest()));
        ConfirmationService.Request request = new ConfirmationService.Request(
                actorId(source),
                "sef:logging.retention.run",
                parameters,
                List.of(),
                "",
                0L,
                0L,
                0L,
                KernelServices.commandPolicies().revision());
        if (token == null) {
            ActionResult<ConfirmationService.IssuedToken> issued =
                    KernelServices.confirmations().issue(request, Duration.ofSeconds(30));
            if (!issued.successful()) {
                return fail(source, "A confirmation token could not be issued.");
            }
            source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                    "&eConfirmation required. Use /sef logging retention run confirm "
                            + issued.value().token() + "."), false);
            return 1;
        }
        ActionResult<ConfirmationService.Request> consumed =
                KernelServices.confirmations().consume(token, request);
        if (!consumed.successful()) {
            return fail(source, "The confirmation token is invalid, expired, used, or no longer matches.");
        }
        return execute(source, "sef:logging.retention.run", parameters, () -> {
            int deleted = KernelServices.fileLogs().runRetention(preview);
            if (deleted < 0) {
                return fail(source, "The archive set changed. Preview and confirm retention again.");
            }
            success(source, "Retention removed " + deleted + " eligible owned archives.");
            return 1;
        }, permission("commands.logging.retention.run"));
    }

    private static UUID actorId(CommandSourceStack source) {
        if (source.getEntity() != null) {
            return source.getEntity().getUUID();
        }
        return UUID.nameUUIDFromBytes(
                ("sef:source:" + source.getTextName()).getBytes(StandardCharsets.UTF_8));
    }

    private static int repair(CommandSourceStack source) {
        return execute(source, "sef:logging.repair", Map.of("operation", "acknowledge"), () -> {
            boolean removed = KernelServices.fileLogs().acknowledgeRepair();
            if (!removed) {
                return fail(source, "No incomplete session marker was acknowledged.");
            }
            success(source, "Incomplete logger state acknowledged.");
            return 1;
        }, permission("commands.logging.repair"));
    }

    @SafeVarargs
    private static int execute(
            CommandSourceStack source,
            String actionId,
            Map<String, String> parameters,
            java.util.function.IntSupplier action,
            PermissionNode<Boolean>... permissions
    ) {
        return KernelCommandExecutor.execute(source, actionId, parameters, action, permissions);
    }

    private static Boolean parseBoolean(String value) {
        if (value.equalsIgnoreCase("toggle") || value.equalsIgnoreCase("status")) {
            return null;
        }
        return value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true");
    }

    private static boolean has(CommandSourceStack source, String id) {
        PermissionNode<Boolean> node = permission(id);
        return node != null && PermissionService.has(source, node);
    }

    private static boolean hasAnyLoggingAction(CommandSourceStack source) {
        return LOGGING_ACTION_PERMISSIONS.stream().anyMatch(node -> PermissionService.has(source, node));
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
