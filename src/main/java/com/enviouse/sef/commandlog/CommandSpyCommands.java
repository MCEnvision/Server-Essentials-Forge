package com.enviouse.sef.commandlog;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.identity.IdentityArguments;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.kernel.observation.ObservationContracts;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.vanish.VanishUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class CommandSpyCommands {
    private CommandSpyCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (!ConfigHandler.config.enableCommandSpy.get()
                || !KernelServices.shortcuts().isActive("commandspy")) {
            return;
        }
        dispatcher.register(node("commandspy", false));
    }

    public static void attachCanonical(LiteralArgumentBuilder<CommandSourceStack> sefRoot) {
        if (ConfigHandler.config.enableCommandSpy.get()) {
            sefRoot.then(node("commandspy", true));
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> node(String literal, boolean management) {
        PermissionNode<Boolean> base = permission("commands.commandspy");
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(literal)
                .requires(source -> (management || source.getPlayer() != null)
                        && PermissionService.has(source, base))
                .executes(context -> toggle(context.getSource(), null));

        root.then(Commands.literal("on")
                .executes(context -> setEnabled(context.getSource(), null, true)));
        root.then(Commands.literal("off")
                .executes(context -> setEnabled(context.getSource(), null, false)));
        root.then(Commands.literal("toggle")
                .executes(context -> toggle(context.getSource(), null)));
        root.then(Commands.literal("status")
                .requires(source -> has(source, "commands.commandspy.status"))
                .executes(context -> status(context.getSource(), null))
                .then(IdentityArguments.online("observer")
                        .requires(source -> management && has(source, "commands.commandspy.others"))
                        .executes(context -> status(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "observer")))));
        root.then(Commands.literal("recent")
                .requires(source -> has(source, "commands.commandspy.recent"))
                .executes(context -> recent(context.getSource(), 10))
                .then(Commands.argument("count", IntegerArgumentType.integer(1, 100))
                        .executes(context -> recent(
                                context.getSource(),
                                IntegerArgumentType.getInteger(context, "count")))));
        root.then(Commands.literal("everyone")
                .requires(source -> has(source, "commands.commandspy.everyone"))
                .executes(context -> everyone(context.getSource(), true))
                .then(booleanArgument("state")
                        .executes(context -> everyone(
                                context.getSource(),
                                parseBoolean(StringArgumentType.getString(context, "state"))))));
        root.then(selectedNode());
        root.then(scopeNode());
        root.then(Commands.literal("results")
                .requires(source -> has(source, "commands.commandspy.results"))
                .then(booleanArgument("state")
                        .executes(context -> projection(
                                context.getSource(),
                                null,
                                parseBoolean(StringArgumentType.getString(context, "state"))))));
        root.then(Commands.literal("location")
                .requires(source -> has(source, "commands.commandspy.location"))
                .then(booleanArgument("state")
                        .executes(context -> projection(
                                context.getSource(),
                                parseBoolean(StringArgumentType.getString(context, "state")),
                                null))));
        root.then(filterNode());
        if (management) {
            root.then(Commands.literal("set")
                    .requires(source -> has(source, "commands.commandspy.others"))
                    .then(IdentityArguments.online("observer")
                            .then(booleanArgument("state")
                                    .executes(context -> setEnabled(
                                            context.getSource(),
                                            IdentityArguments.getOnline(context, "observer"),
                                            parseBoolean(StringArgumentType.getString(context, "state")))))));
        }
        root.then(IdentityArguments.online("player")
                .requires(source -> has(source, "commands.commandspy.player"))
                .executes(context -> selectOnly(
                        context.getSource(),
                        IdentityArguments.getOnline(context, "player"),
                        true))
                .then(booleanArgument("state")
                        .executes(context -> selectOnly(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "player"),
                                parseBoolean(StringArgumentType.getString(context, "state"))))));
        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> selectedNode() {
        return Commands.literal("selected")
                .requires(source -> has(source, "commands.commandspy.selected"))
                .then(Commands.literal("list")
                        .executes(context -> listSelected(context.getSource())))
                .then(Commands.literal("clear")
                        .executes(context -> clearSelected(context.getSource())))
                .then(Commands.literal("add")
                        .then(IdentityArguments.online("player")
                                .executes(context -> selected(
                                        context.getSource(),
                                        IdentityArguments.getOnline(context, "player"),
                                        true))))
                .then(Commands.literal("remove")
                        .then(IdentityArguments.online("player")
                                .executes(context -> selected(
                                        context.getSource(),
                                        IdentityArguments.getOnline(context, "player"),
                                        false))))
                .then(Commands.literal("match")
                        .requires(source -> has(source, "commands.commandspy.match"))
                        .then(Commands.argument("relation", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        new String[]{"initiator", "effective-actor", "either"},
                                        builder))
                                .executes(context -> relation(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "relation")))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> scopeNode() {
        return Commands.literal("scope")
                .then(Commands.literal("players")
                        .requires(source -> has(source, "commands.commandspy.scope.player"))
                        .executes(context -> sources(context.getSource(), true, false)))
                .then(Commands.literal("all-sources")
                        .requires(source -> has(source, "commands.commandspy.scope.nonplayer"))
                        .executes(context -> sources(context.getSource(), true, true)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> filterNode() {
        LiteralArgumentBuilder<CommandSourceStack> filter = Commands.literal("filter")
                .requires(source -> has(source, "commands.commandspy.filter"))
                .then(Commands.literal("list")
                        .executes(context -> filterList(context.getSource())))
                .then(Commands.literal("reset")
                        .executes(context -> filterReset(context.getSource())));
        for (String mode : new String[]{"include", "exclude"}) {
            filter.then(Commands.literal(mode)
                    .then(Commands.literal("root")
                            .then(Commands.argument("root", StringArgumentType.word())
                                    .executes(context -> filter(
                                            context.getSource(),
                                            mode,
                                            "root",
                                            StringArgumentType.getString(context, "root")))))
                    .then(Commands.literal("action")
                            .then(Commands.argument("action", StringArgumentType.word())
                                    .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                            KernelServices.catalog().entries().stream().map(entry -> entry.id()).toList(),
                                            builder))
                                    .executes(context -> filter(
                                            context.getSource(),
                                            mode,
                                            "action",
                                            StringArgumentType.getString(context, "action"))))));
        }
        filter.then(typedFilterNode(
                "source",
                "commands.commandspy.filter.source",
                java.util.Arrays.stream(CommandDefinition.SourceType.values())
                        .map(value -> value.name().toLowerCase(Locale.ROOT))
                        .toArray(String[]::new)));
        filter.then(Commands.literal("player")
                .requires(source -> has(source, "commands.commandspy.filter.player"))
                .then(playerFilterModeNode("include", true))
                .then(playerFilterModeNode("exclude", false))
                .then(Commands.literal("clear")
                        .then(IdentityArguments.online("player")
                                .executes(context -> playerFilterClear(
                                        context.getSource(),
                                        IdentityArguments.getOnline(context, "player"))))));
        filter.then(typedFilterNode(
                "result",
                "commands.commandspy.filter.result",
                java.util.Arrays.stream(ObservationContracts.LifecycleStage.values())
                        .map(value -> value.name().toLowerCase(Locale.ROOT))
                        .toArray(String[]::new)));
        filter.then(typedFilterNode("world", "commands.commandspy.filter.world", new String[0]));
        filter.then(typedFilterNode(
                "origin",
                "commands.commandspy.filter.origin",
                new String[]{"player", "console", "rcon", "command_block", "function", "scheduler",
                        "panel", "bundle", "sudo", "execution_profile", "run_server",
                        "silent_actor", "silent_server", "external_integration"}));
        return filter;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> playerFilterModeNode(
            String literal,
            boolean include
    ) {
        return Commands.literal(literal)
                .then(IdentityArguments.online("player")
                        .then(booleanArgument("state")
                                .executes(context -> playerFilter(
                                        context.getSource(),
                                        IdentityArguments.getOnline(context, "player"),
                                        include,
                                        StringArgumentType.getString(context, "state")))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> typedFilterNode(
            String kind,
            String permission,
            String[] suggestions
    ) {
        var value = Commands.argument("value", StringArgumentType.word());
        if (suggestions.length > 0) {
            value.suggests((context, builder) -> SharedSuggestionProvider.suggest(suggestions, builder));
        }
        return Commands.literal(kind)
                .requires(source -> has(source, permission))
                .then(value.then(booleanArgument("state")
                        .executes(context -> typedFilter(
                                context.getSource(),
                                kind,
                                StringArgumentType.getString(context, "value"),
                                StringArgumentType.getString(context, "state")))));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> booleanArgument(
            String name
    ) {
        return Commands.argument(name, StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                        new String[]{"on", "off"},
                        builder));
    }

    private static int toggle(CommandSourceStack source, ServerPlayer explicitObserver) {
        ServerPlayer observer = explicitObserver == null ? source.getPlayer() : explicitObserver;
        if (observer == null) {
            return KernelCommandExecutor.reject(
                    source,
                    "sef:commandspy.toggle",
                    ActionResult.ReasonCode.SOURCE_NOT_ALLOWED,
                    "An explicit online observer is required.");
        }
        CommandSpyRepository.Profile profile = KernelServices.commandSpies().profile(observer.getUUID());
        return setEnabled(source, observer, !profile.enabled());
    }

    private static int setEnabled(CommandSourceStack source, ServerPlayer explicitObserver, boolean enabled) {
        ServerPlayer observer = explicitObserver == null ? source.getPlayer() : explicitObserver;
        if (observer == null) {
            return KernelCommandExecutor.reject(
                    source,
                    "sef:commandspy.toggle",
                    ActionResult.ReasonCode.SOURCE_NOT_ALLOWED,
                    "An explicit online observer is required.");
        }
        boolean managingOther = explicitObserver != null
                && (source.getPlayer() == null
                || !source.getPlayer().getUUID().equals(observer.getUUID()));
        if (managingOther && !canManageObserver(source, observer)) {
            return fail(source, "That observer is unavailable.");
        }
        PermissionNode<Boolean>[] permissions = managingOther
                ? permissionArray("commands.commandspy.others")
                : permissionArray();
        return execute(
                source,
                "sef:commandspy.toggle",
                Map.of("enabled", Boolean.toString(enabled), "observer", observer.getUUID().toString()),
                () -> {
                    if (enabled && !PermissionService.has(
                            observer,
                            permission("commandspy.view.metadata"))) {
                        return fail(source, "The observer lacks command metadata visibility.");
                    }
                    CommandSpyRepository.Profile profile = KernelServices.commandSpies().profile(observer.getUUID());
                    if (enabled && !canActivate(observer, profile)) {
                        return fail(source, "The observer lacks permission for the saved audience or source scope.");
                    }
                    KernelServices.commandSpies().put(profile.withEnabled(enabled));
                    success(source, "Command spy " + (enabled ? "enabled" : "disabled") + " for "
                            + observer.getGameProfile().getName() + ".");
                    return 1;
                },
                permissions);
    }

    private static int status(CommandSourceStack source, ServerPlayer explicitObserver) {
        ServerPlayer observer = explicitObserver == null ? source.getPlayer() : explicitObserver;
        if (observer == null) {
            return KernelCommandExecutor.reject(
                    source,
                    "sef:commandspy.status",
                    ActionResult.ReasonCode.SOURCE_NOT_ALLOWED,
                    "An explicit online observer is required.");
        }
        boolean managingOther = explicitObserver != null
                && (source.getPlayer() == null
                || !source.getPlayer().getUUID().equals(observer.getUUID()));
        if (managingOther && !canManageObserver(source, observer)) {
            return fail(source, "That observer is unavailable.");
        }
        return execute(source, "sef:commandspy.status", Map.of("observer", observer.getUUID().toString()), () -> {
            CommandSpyRepository.Profile profile = KernelServices.commandSpies().profile(observer.getUUID());
            info(source, "Command spy for " + observer.getGameProfile().getName() + ".");
            info(source, "Requested " + profile.enabled() + ", audience "
                    + profile.audience().name().toLowerCase(Locale.ROOT) + ", selected "
                    + profile.selectedPlayerIds().size() + ".");
            info(source, "Match " + profile.actorRelation().name().toLowerCase(Locale.ROOT)
                    + ", player sources " + profile.playerSources() + ", non player sources "
                    + profile.nonPlayerSources() + ".");
            info(source, "Location " + profile.includeLocation() + ", results " + profile.includeResults()
                    + ", revision " + profile.revision() + ".");
            return 1;
        }, managingOther
                ? permissionArray("commands.commandspy.status", "commands.commandspy.others")
                : permissionArray("commands.commandspy.status"));
    }

    private static boolean canManageObserver(CommandSourceStack source, ServerPlayer observer) {
        ServerPlayer actor = source.getPlayer();
        if (actor != null && actor.getUUID().equals(observer.getUUID())) {
            return true;
        }
        if (actor != null && VanishUtil.isVanished(observer, actor)) {
            return false;
        }
        return PlayerTargetPolicy.decide(
                source,
                observer,
                permission("commandspy.hierarchy.bypass"),
                permission("commandspy.exempt"),
                permission("commandspy.view.exempt"),
                false,
                true).allowed();
    }

    private static int recent(CommandSourceStack source, int count) {
        return execute(source, "sef:commandspy.recent", Map.of("count", Integer.toString(count)), () -> {
            ServerPlayer observer = player(source, "sef:commandspy.recent");
            if (observer == null) {
                return 0;
            }
            var records = KernelServices.commandJournal().recentAuthorized(observer, count);
            info(source, "Recent redacted command events, " + records.size() + ".");
            for (var record : records) {
                info(source, "[" + record.sourceType().name().toLowerCase(Locale.ROOT) + "] "
                        + record.actorName() + ", /" + record.root() + ", "
                        + record.stage().name().toLowerCase(Locale.ROOT) + ".");
            }
            return 1;
        }, permission("commands.commandspy.recent"));
    }

    static boolean canActivate(ServerPlayer observer, CommandSpyRepository.Profile profile) {
        if (!PermissionService.has(observer, permission("commands.commandspy"))
                || !PermissionService.has(observer, permission("commandspy.view.metadata"))
                || profile.playerSources()
                && !PermissionService.has(observer, permission("commands.commandspy.scope.player"))) {
            return false;
        }
        if (profile.nonPlayerSources()
                && !PermissionService.has(observer, permission("commands.commandspy.scope.nonplayer"))) {
            return false;
        }
        if (profile.audience() == CommandSpyRepository.Audience.EVERYONE) {
            return PermissionService.has(observer, permission("commands.commandspy.everyone"));
        }
        return PermissionService.has(observer, permission("commands.commandspy.player"))
                || PermissionService.has(observer, permission("commands.commandspy.selected"));
    }

    private static int everyone(CommandSourceStack source, boolean enabled) {
        ServerPlayer observer = player(source, "sef:commandspy.audience");
        if (observer == null) {
            return 0;
        }
        return execute(source, "sef:commandspy.audience", Map.of("audience", "everyone", "enabled",
                Boolean.toString(enabled)), () -> {
            CommandSpyRepository.Profile profile = KernelServices.commandSpies().profile(observer.getUUID());
            CommandSpyRepository.Profile replacement = profile
                    .withAudience(CommandSpyRepository.Audience.EVERYONE, profile.selectedPlayerIds())
                    .withEnabled(enabled);
            if (enabled && !canActivate(observer, replacement)) {
                return fail(source, "The observer lacks permission for the saved audience or source scope.");
            }
            KernelServices.commandSpies().put(replacement);
            success(source, "Command spy everyone scope " + (enabled ? "enabled" : "disabled") + ".");
            return 1;
        }, permission("commands.commandspy.everyone"));
    }

    private static int selectOnly(CommandSourceStack source, ServerPlayer target, boolean enabled) {
        ServerPlayer observer = player(source, "sef:commandspy.audience");
        if (observer == null) {
            return 0;
        }
        if (!enabled) {
            return selected(source, target, false);
        }
        return execute(source, "sef:commandspy.audience",
                Map.of("audience", "selected", "selected_count", "1"), () -> {
                    CommandSpyRepository.Profile replacement =
                            KernelServices.commandSpies().profile(observer.getUUID())
                                    .withAudience(CommandSpyRepository.Audience.SELECTED, Set.of(target.getUUID()))
                                    .withEnabled(true);
                    if (!canActivate(observer, replacement)) {
                        return fail(source, "The observer lacks permission for the saved audience or source scope.");
                    }
                    KernelServices.commandSpies().put(replacement);
                    success(source, "Command spy now observes the selected player.");
                    return 1;
                }, permission("commands.commandspy.player"));
    }

    private static int selected(CommandSourceStack source, ServerPlayer target, boolean add) {
        ServerPlayer observer = player(source, "sef:commandspy.selected");
        if (observer == null) {
            return 0;
        }
        return execute(source, "sef:commandspy.selected",
                Map.of("operation", add ? "add" : "remove", "target", target.getUUID().toString()), () -> {
                    CommandSpyRepository.Profile profile = KernelServices.commandSpies().profile(observer.getUUID());
                    Set<UUID> selected = new HashSet<>(profile.selectedPlayerIds());
                    if (add) {
                        selected.add(target.getUUID());
                    } else {
                        selected.remove(target.getUUID());
                    }
                    boolean enabled = profile.enabled() && !selected.isEmpty();
                    KernelServices.commandSpies().put(profile
                            .withAudience(CommandSpyRepository.Audience.SELECTED, selected)
                            .withEnabled(enabled));
                    success(source, "Selected command spy player "
                            + (add ? "added" : "removed") + ". Total " + selected.size() + ".");
                    return 1;
                }, permission("commands.commandspy.selected"));
    }

    private static int listSelected(CommandSourceStack source) {
        ServerPlayer observer = player(source, "sef:commandspy.selected");
        if (observer == null) {
            return 0;
        }
        return execute(source, "sef:commandspy.selected", Map.of("operation", "list"), () -> {
            Set<UUID> selected = KernelServices.commandSpies().profile(observer.getUUID()).selectedPlayerIds();
            info(source, "Selected command spy UUIDs, " + selected.size() + ".");
            selected.stream().sorted().forEach(id -> info(source, id.toString()));
            return 1;
        }, permission("commands.commandspy.selected"));
    }

    private static int clearSelected(CommandSourceStack source) {
        ServerPlayer observer = player(source, "sef:commandspy.selected");
        if (observer == null) {
            return 0;
        }
        return execute(source, "sef:commandspy.selected", Map.of("operation", "clear"), () -> {
            CommandSpyRepository.Profile profile = KernelServices.commandSpies().profile(observer.getUUID());
            KernelServices.commandSpies().put(profile
                    .withAudience(CommandSpyRepository.Audience.SELECTED, Set.of())
                    .withEnabled(false));
            success(source, "Selected command spy players cleared. Observation disabled.");
            return 1;
        }, permission("commands.commandspy.selected"));
    }

    private static int relation(CommandSourceStack source, String input) {
        ServerPlayer observer = player(source, "sef:commandspy.selected");
        if (observer == null) {
            return 0;
        }
        CommandSpyRepository.ActorRelation relation = switch (input.toLowerCase(Locale.ROOT)) {
            case "initiator" -> CommandSpyRepository.ActorRelation.INITIATOR;
            case "effective", "effective-actor", "effective_actor" -> CommandSpyRepository.ActorRelation.EFFECTIVE;
            case "either" -> CommandSpyRepository.ActorRelation.EITHER;
            default -> null;
        };
        if (relation == null) {
            return fail(source, "Use initiator, effective-actor, or either.");
        }
        return execute(source, "sef:commandspy.selected", Map.of("relation", relation.name()), () -> {
            CommandSpyRepository.Profile profile = KernelServices.commandSpies().profile(observer.getUUID());
            KernelServices.commandSpies().put(profile.withRelation(relation));
            success(source, "Command spy actor relation set to " + input + ".");
            return 1;
        }, permission("commands.commandspy.match"));
    }

    private static int sources(CommandSourceStack source, boolean players, boolean nonPlayers) {
        ServerPlayer observer = player(source, "sef:commandspy.scope");
        if (observer == null) {
            return 0;
        }
        return execute(source, "sef:commandspy.scope", Map.of(
                "players", Boolean.toString(players),
                "non_players", Boolean.toString(nonPlayers)), () -> {
            CommandSpyRepository.Profile profile = KernelServices.commandSpies().profile(observer.getUUID());
            KernelServices.commandSpies().put(profile.withSources(players, nonPlayers));
            success(source, "Command spy source scope updated.");
            return 1;
        }, permission(nonPlayers ? "commands.commandspy.scope.nonplayer" : "commands.commandspy.scope.player"));
    }

    private static int projection(CommandSourceStack source, Boolean location, Boolean results) {
        ServerPlayer observer = player(source, "sef:commandspy.scope");
        if (observer == null) {
            return 0;
        }
        return execute(source, "sef:commandspy.scope", Map.of(
                "location", String.valueOf(location),
                "results", String.valueOf(results)), () -> {
            CommandSpyRepository.Profile profile = KernelServices.commandSpies().profile(observer.getUUID());
            KernelServices.commandSpies().put(profile.withProjection(
                    location == null ? profile.includeLocation() : location,
                    results == null ? profile.includeResults() : results));
            success(source, "Command spy projection updated.");
            return 1;
        }, permission(location != null ? "commands.commandspy.location" : "commands.commandspy.results"));
    }

    private static int filterList(CommandSourceStack source) {
        ServerPlayer observer = player(source, "sef:commandspy.filter");
        if (observer == null) {
            return 0;
        }
        return execute(source, "sef:commandspy.filter", Map.of("operation", "list"), () -> {
            CommandSpyRepository.Profile profile = KernelServices.commandSpies().profile(observer.getUUID());
            info(source, "Included roots " + profile.includedRoots() + ".");
            info(source, "Excluded roots " + profile.excludedRoots() + ".");
            info(source, "Included actions " + profile.includedActions() + ".");
            info(source, "Excluded actions " + profile.excludedActions() + ".");
            info(source, "Disabled sources " + profile.typedFilters().disabledSources() + ".");
            info(source, "Disabled results " + profile.typedFilters().disabledResults() + ".");
            info(source, "Disabled worlds " + profile.typedFilters().disabledWorlds() + ".");
            info(source, "Disabled origins " + profile.typedFilters().disabledOrigins() + ".");
            return 1;
        }, permission("commands.commandspy.filter"));
    }

    private static int filterReset(CommandSourceStack source) {
        ServerPlayer observer = player(source, "sef:commandspy.filter");
        if (observer == null) {
            return 0;
        }
        return execute(source, "sef:commandspy.filter", Map.of("operation", "reset"), () -> {
            CommandSpyRepository.Profile profile = KernelServices.commandSpies().profile(observer.getUUID());
            KernelServices.commandSpies().put(profile
                    .withFilters(Set.of(), Set.of(), Set.of(), Set.of())
                    .withTypedFilters(CommandSpyRepository.TypedFilters.defaults()));
            success(source, "Command spy filters reset.");
            return 1;
        }, permission("commands.commandspy.filter"));
    }

    private static int typedFilter(
            CommandSourceStack source,
            String kind,
            String input,
            String state
    ) {
        ServerPlayer observer = player(source, "sef:commandspy.filter");
        if (observer == null) {
            return 0;
        }
        if (!state.equalsIgnoreCase("on") && !state.equalsIgnoreCase("off")) {
            return fail(source, "Filter state must be on or off.");
        }
        String normalized = input.trim().toLowerCase(Locale.ROOT);
        if (!validTypedFilter(kind, normalized)) {
            return fail(source, "That typed filter value is invalid.");
        }
        boolean enabled = state.equalsIgnoreCase("on");
        return execute(source, "sef:commandspy.filter", Map.of(
                "kind", kind,
                "value", normalized,
                "enabled", Boolean.toString(enabled)), () -> {
            CommandSpyRepository.Profile profile = KernelServices.commandSpies().profile(observer.getUUID());
            KernelServices.commandSpies().put(profile.withTypedFilters(
                    profile.typedFilters().withValue(kind, normalized, enabled)));
            success(source, "Command spy typed filter updated.");
            return 1;
        }, permission("commands.commandspy.filter." + kind));
    }

    private static int playerFilter(
            CommandSourceStack source,
            ServerPlayer target,
            boolean include,
            String state
    ) {
        ServerPlayer observer = player(source, "sef:commandspy.filter");
        if (observer == null) {
            return 0;
        }
        if (!state.equalsIgnoreCase("on") && !state.equalsIgnoreCase("off")) {
            return fail(source, "Filter state must be on or off.");
        }
        boolean enabled = state.equalsIgnoreCase("on");
        return execute(source, "sef:commandspy.filter", Map.of(
                "kind", "player",
                "mode", include ? "include" : "exclude",
                "enabled", Boolean.toString(enabled)), () -> {
            CommandSpyRepository.Profile profile = KernelServices.commandSpies().profile(observer.getUUID());
            KernelServices.commandSpies().put(profile.withTypedFilters(
                    profile.typedFilters().withPlayer(target.getUUID().toString(), include, enabled)));
            success(source, "Command spy player filter updated.");
            return 1;
        }, permission("commands.commandspy.filter.player"));
    }

    private static int playerFilterClear(CommandSourceStack source, ServerPlayer target) {
        ServerPlayer observer = player(source, "sef:commandspy.filter");
        if (observer == null) {
            return 0;
        }
        return execute(source, "sef:commandspy.filter", Map.of(
                "kind", "player",
                "mode", "clear"), () -> {
            CommandSpyRepository.Profile profile = KernelServices.commandSpies().profile(observer.getUUID());
            KernelServices.commandSpies().put(profile.withTypedFilters(
                    profile.typedFilters().withoutPlayer(target.getUUID().toString())));
            success(source, "Command spy player filter cleared.");
            return 1;
        }, permission("commands.commandspy.filter.player"));
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

    private static int filter(CommandSourceStack source, String mode, String kind, String value) {
        ServerPlayer observer = player(source, "sef:commandspy.filter");
        if (observer == null) {
            return 0;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > 128 || normalized.isBlank()) {
            return fail(source, "Filter value is outside bounds.");
        }
        return execute(source, "sef:commandspy.filter", Map.of(
                "mode", mode, "kind", kind, "value", normalized), () -> {
            CommandSpyRepository.Profile profile = KernelServices.commandSpies().profile(observer.getUUID());
            Set<String> includeRoots = new HashSet<>(profile.includedRoots());
            Set<String> excludeRoots = new HashSet<>(profile.excludedRoots());
            Set<String> includeActions = new HashSet<>(profile.includedActions());
            Set<String> excludeActions = new HashSet<>(profile.excludedActions());
            Set<String> destination = kind.equals("root")
                    ? mode.equals("include") ? includeRoots : excludeRoots
                    : mode.equals("include") ? includeActions : excludeActions;
            destination.add(normalized);
            KernelServices.commandSpies().put(profile.withFilters(
                    includeRoots, excludeRoots, includeActions, excludeActions));
            success(source, "Command spy filter added.");
            return 1;
        }, permission("commands.commandspy.filter"));
    }

    @SafeVarargs
    private static int execute(
            CommandSourceStack source,
            String actionId,
            Map<String, String> parameters,
            java.util.function.IntSupplier action,
            PermissionNode<Boolean>... additional
    ) {
        return KernelCommandExecutor.execute(source, actionId, parameters, action, additional);
    }

    private static boolean parseBoolean(String value) {
        return value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true");
    }

    private static boolean has(CommandSourceStack source, String id) {
        PermissionNode<Boolean> node = permission(id);
        return node != null && PermissionService.has(source, node);
    }

    private static PermissionNode<Boolean> permission(String id) {
        return PermissionsHandler.phasePermission(id);
    }

    @SuppressWarnings("unchecked")
    private static PermissionNode<Boolean>[] permissionArray(String... ids) {
        PermissionNode<Boolean>[] nodes = new PermissionNode[ids.length];
        for (int index = 0; index < ids.length; index++) {
            nodes[index] = permission(ids[index]);
        }
        return nodes;
    }

    private static ServerPlayer player(CommandSourceStack source, String actionId) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            KernelCommandExecutor.reject(
                    source,
                    actionId,
                    ActionResult.ReasonCode.SOURCE_NOT_ALLOWED,
                    "This command requires a player observer.");
        }
        return player;
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
