package com.enviouse.sef.kernel;

import com.enviouse.sef.audit.SecurityAuditService;
import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.gui.protocol.SefGuiRuntime;
import com.enviouse.sef.gui.protocol.SefGuiServer;
import com.enviouse.sef.gui.protocol.SefNetwork;
import com.enviouse.sef.gui.protocol.SefProtocol;
import com.enviouse.sef.gui.protocol.SefSessionManager;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.kernel.command.ShortcutRegistry;
import com.enviouse.sef.permissions.PermissionService;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

import java.util.List;
import java.util.Map;
import com.enviouse.sef.storage.ImportDiagnostics;
import com.enviouse.sef.gui.AdminPanelCommands;

public final class KernelCommands {
    private static final int PAGE_SIZE = 10;

    private KernelCommands() {
    }

    public static void attach(LiteralArgumentBuilder<CommandSourceStack> root) {
        KernelServices.initialize();
        AdminPanelCommands.attach(root);
        root.then(Commands.literal("commands")
                .requires(source -> PermissionService.has(source, PermissionsHandler.sefCommandsCatalog))
                .executes(context -> KernelCommandExecutor.execute(
                        context.getSource(),
                        "sef:core.commands",
                        Map.of("page", "1"),
                        () -> commands(context.getSource(), 1)))
                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                        .executes(context -> {
                            int page = IntegerArgumentType.getInteger(context, "page");
                            return KernelCommandExecutor.execute(
                                    context.getSource(),
                                    "sef:core.commands",
                                    Map.of("page", Integer.toString(page)),
                                    () -> commands(context.getSource(), page));
                        })));
        root.then(Commands.literal("conflicts")
                .requires(source -> PermissionService.has(source, PermissionsHandler.sefConflicts))
                .executes(context -> KernelCommandExecutor.execute(
                        context.getSource(),
                        "sef:core.conflicts",
                        Map.of(),
                        () -> conflicts(context.getSource()))));
        root.then(Commands.literal("doctor")
                .requires(source -> PermissionService.has(source, PermissionsHandler.sefDoctor))
                .executes(context -> KernelCommandExecutor.execute(
                        context.getSource(),
                        "sef:core.doctor",
                        Map.of(),
                        () -> doctor(context.getSource()))));
        root.then(Commands.literal("dashboard")
                .requires(source -> source.getPlayer() != null
                        && PermissionService.has(source, PermissionsHandler.kernelGui))
                .executes(context -> KernelCommandExecutor.execute(
                        context.getSource(),
                        "sef:gui.dashboard.open",
                        Map.of(),
                        () -> dashboard(context.getSource()))));
        root.then(Commands.literal("cooldown")
                .then(Commands.literal("keys")
                        .requires(source -> has(source, "sef.commands.cooldown.keys"))
                        .executes(context -> cooldownKeys(context.getSource(), 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> cooldownKeys(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "page")))))
                .then(Commands.literal("explain")
                        .requires(source -> has(source, "sef.commands.cooldown.explain"))
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    context.getSource().getServer().getPlayerList().getPlayers()
                                            .forEach(player -> builder.suggest(player.getGameProfile().getName()));
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("action", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            KernelServices.cooldownDurations().definitions().forEach(definition -> {
                                                builder.suggest(definition.actionId());
                                                builder.suggest(definition.permissionKey());
                                            });
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> cooldownExplain(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "player"),
                                                StringArgumentType.getString(context, "action")))))));
        root.then(Commands.literal("client")
                .then(Commands.literal("status")
                        .executes(context -> KernelCommandExecutor.execute(
                                context.getSource(),
                                "sef:gui.client.status",
                                Map.of(),
                                () -> clientStatus(context.getSource()))))
                .then(Commands.literal("reminder")
                        .then(Commands.literal("dismiss")
                                .requires(source -> source.getPlayer() != null)
                                .executes(context -> KernelCommandExecutor.execute(
                                        context.getSource(),
                                        "sef:gui.reminder.dismiss",
                                        Map.of(),
                                        () -> dismissReminder(context.getSource())))))
                .then(Commands.literal("preference")
                        .requires(source -> source.getPlayer() != null
                                && PermissionService.has(source, PermissionsHandler.guiPreferences))
                        .then(Commands.literal("mode")
                                .then(Commands.argument("value", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            builder.suggest("auto").suggest("gui").suggest("command");
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> updatePreference(
                                                context.getSource(),
                                                "sef:gui.preference.mode",
                                                StringArgumentType.getString(context, "value")))))
                        .then(Commands.literal("pause")
                                .then(Commands.argument("value", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            builder.suggest("on").suggest("off");
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> updatePreference(
                                                context.getSource(),
                                                "sef:gui.preference.pause",
                                                StringArgumentType.getString(context, "value")))))
                        .then(Commands.literal("hud")
                                .then(Commands.argument("value", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            builder.suggest("on").suggest("off");
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> updatePreference(
                                                context.getSource(),
                                                "sef:gui.preference.hud",
                                                StringArgumentType.getString(context, "value")))))
                        .then(Commands.literal("blur")
                                .then(Commands.argument("value", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            builder.suggest("off");
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> updatePreference(
                                                context.getSource(),
                                                "sef:gui.preference.blur",
                                                StringArgumentType.getString(context, "value")))))
                        .then(Commands.literal("motion")
                                .then(Commands.argument("value", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            builder.suggest("full").suggest("reduced");
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> updatePreference(
                                                context.getSource(),
                                                "sef:gui.preference.motion",
                                                StringArgumentType.getString(context, "value")))))
                        .then(Commands.literal("page_size")
                                .then(Commands.argument("value", IntegerArgumentType.integer(4, 100))
                                        .executes(context -> updatePageSize(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "value")))))));
    }

    private static int cooldownKeys(CommandSourceStack source, int requestedPage) {
        var definitions = KernelServices.cooldownDurations().definitions();
        int pages = Math.max(1, (definitions.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int page = Math.min(requestedPage, pages);
        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(definitions.size(), start + PAGE_SIZE);
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&6Cooldown keys &7page &f" + page + "&7/&f" + pages), false);
        for (int index = start; index < end; index++) {
            var definition = definitions.get(index);
            source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                    "&e" + definition.actionId()
                            + " &8| &fsef.cooldown." + definition.permissionKey() + ".<seconds>"
                            + " &8| &7default " + definition.internalDefault().toSeconds() + " seconds"), false);
        }
        return 1;
    }

    private static int cooldownExplain(CommandSourceStack source, String playerInput, String actionInput) {
        var identity = KernelServices.identities().resolve(playerInput, source.getPlayer());
        if (!identity.successful() || identity.value().playerId() == null) {
            source.sendFailure(TextFormatter.stringToFormattedText("&cThat player identity is unavailable."));
            return 0;
        }
        String action = KernelServices.cooldownDurations().definitions().stream()
                .filter(definition -> definition.actionId().equalsIgnoreCase(actionInput)
                        || definition.permissionKey().equalsIgnoreCase(actionInput))
                .map(com.enviouse.sef.permissions.PermissionCooldownResolver.Definition::actionId)
                .findFirst()
                .orElse("");
        if (action.isEmpty()) {
            source.sendFailure(TextFormatter.stringToFormattedText("&cThat cooldown action is unknown."));
            return 0;
        }
        var resolution = KernelServices.cooldownDurations().explain(identity.value().playerId(), action);
        var persisted = KernelServices.cooldowns().inspect(identity.value().playerId(), action);
        ServerPlayer target = source.getServer().getPlayerList().getPlayer(identity.value().playerId());
        boolean bypass = target != null
                && KernelServices.cooldownBypass(target.createCommandSourceStack(), action);
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&6Cooldown explanation &8| &f" + identity.value().authenticatedUsername()), false);
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&7Action: &f" + resolution.actionId()
                        + " &8| &7key: &f" + resolution.permissionKey()
                        + " &8| &7seconds: &f" + resolution.duration().toSeconds()), false);
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&7Provider: &f" + resolution.provider()
                        + " &8| &7node: &f" + (resolution.winningNode().isBlank()
                        ? "internal default"
                        : resolution.winningNode())
                        + " &8| &7fallback: &f" + resolution.fallback()
                        + " &8| &7bypass: &f" + (target == null ? "offline unknown" : bypass)), false);
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&7Persisted remaining: &f" + persisted.remainingSeconds()
                        + " seconds &8| &7resolver revision: &f" + resolution.revision()), false);
        return 1;
    }

    private static int commands(CommandSourceStack source, int requestedPage) {
        List<CommandDefinition> visible = KernelServices.catalog().entries().stream()
                .filter(definition -> definition.permissionIds().stream().allMatch(id -> has(source, id)))
                .toList();
        int pages = Math.max(1, (visible.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int page = Math.min(requestedPage, pages);
        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(visible.size(), start + PAGE_SIZE);
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&6SEF command catalog &7page &f" + page + "&7/&f" + pages), false);
        for (int index = start; index < end; index++) {
            CommandDefinition definition = visible.get(index);
            String roots = definition.convenienceRoots().isEmpty()
                    ? ""
                    : " &8| &7/" + String.join(", /", definition.convenienceRoots());
            String cost = KernelServices.commandCostDescription(definition.id());
            String costText = cost.isBlank() ? "" : " &8| &6cost &f" + cost;
            source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                    "&e/" + definition.canonicalRoute() + roots + " &8| &f"
                            + definition.id() + costText), false);
        }
        if (visible.isEmpty()) {
            source.sendSuccess(() -> TextFormatter.stringToFormattedText("&7No catalog entries are currently available."), false);
        }
        return 1;
    }

    private static int conflicts(CommandSourceStack source) {
        List<ShortcutRegistry.Diagnostic> diagnostics = KernelServices.shortcuts().diagnostics();
        long conflicts = diagnostics.stream().filter(diagnostic ->
                diagnostic.status() == ShortcutRegistry.Status.CONFLICT
                        || diagnostic.status() == ShortcutRegistry.Status.CANONICAL_ONLY
                        || diagnostic.status() == ShortcutRegistry.Status.RESTART_REQUIRED).count();
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&6SEF command roots: &e" + diagnostics.size() + " &8| &6conflicts: &e" + conflicts), false);
        for (ShortcutRegistry.Diagnostic diagnostic : diagnostics) {
            String color = switch (diagnostic.status()) {
                case ACTIVE, ACTIVE_OVERRIDE -> "&a";
                case CANONICAL_ONLY, RESTART_REQUIRED -> "&e";
                case CONFLICT -> "&c";
            };
            source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                    color + "/" + diagnostic.root() + " &8| &f" + diagnostic.actionId()
                            + " &8| &7" + diagnostic.status().name().toLowerCase(java.util.Locale.ROOT)), false);
        }
        return 1;
    }

    private static int doctor(CommandSourceStack source) {
        var catalogProblems = KernelServices.catalog().validate();
        var storage = KernelServices.storage();
        var repositories = storage.diagnostics();
        var profiles = KernelServices.profiles().diagnostic();
        var quotaProviderProblems = KernelServices.quotas().providerDiagnostics();
        var restartRequiredDrift = KernelServices.restartRequiredConfigurationDrift();
        var securityAudit = SecurityAuditService.health();
        source.sendSuccess(() -> TextFormatter.stringToFormattedText("&6SEF doctor"), false);
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&7Catalog entries: &f" + KernelServices.catalog().size()
                        + " &8| &7capabilities: &f" + KernelServices.capabilities().size()
                        + " &8| &7shortcuts: &f" + KernelServices.shortcuts().size()), false);
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&7Policies: &f" + KernelServices.commandPolicies().policies().size()
                        + " &8| &7quotas: &f" + KernelServices.quotas().definitions().size()
                        + " &8| &7policy revision: &f" + KernelServices.commandPolicies().revision()), false);
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&7Repositories: &f" + repositories.size()
                        + " &8| &7recovery mode: " + (storage.recoveryMode() ? "&cactive" : "&ainactive")), false);
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&7Player profiles: &f" + profiles.profileCount()
                        + " &8| &7state: &f"
                        + profiles.state().name().toLowerCase(java.util.Locale.ROOT)), false);
        List<ImportDiagnostics.Entry> imports = ImportDiagnostics.snapshot();
        long importFailures = imports.stream().filter(entry ->
                entry.result() == ImportDiagnostics.Result.FAILED
                        || entry.result() == ImportDiagnostics.Result.REJECTED).count();
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&7Import diagnostics: &f" + imports.size()
                        + " &8| &7failures: " + (importFailures == 0 ? "&a0" : "&c" + importFailures)), false);
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&7Quota provider failures: "
                        + (quotaProviderProblems.isEmpty() ? "&a0" : "&c" + quotaProviderProblems.size())), false);
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&7Restart required changes: "
                        + (restartRequiredDrift.isEmpty() ? "&a0" : "&e" + restartRequiredDrift.size())), false);
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&7Security audit: "
                        + (securityAudit.running() && securityAudit.writerAlive()
                        && securityAudit.failures() == 0L && securityAudit.dropped() == 0L
                        ? "&ahealthy"
                        : "&crequires attention")), false);
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&7Enhanced GUI protocol: &f" + (SefNetwork.enhancedGuiActive() ? "active" : "disabled")
                        + " &8| &7sessions: &f" + SefSessionManager.instance().activeCount()
                        + " &8| &7pending: &f" + SefSessionManager.instance().pendingCount()), false);
        for (var repository : repositories) {
            source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                    "&7" + repository.id() + " &8| &f" + repository.state().name().toLowerCase(java.util.Locale.ROOT)
                            + (repository.dirty() ? " &8| &edirty" : "")), false);
        }
        for (var problem : catalogProblems) {
            source.sendFailure(TextFormatter.stringToFormattedText(
                    "&c" + problem.ownerId() + ": " + problem.message()));
        }
        for (var problem : quotaProviderProblems) {
            source.sendFailure(TextFormatter.stringToFormattedText(
                    "&c" + problem.providerId() + ": " + problem.detail()));
        }
        for (String setting : restartRequiredDrift) {
            source.sendFailure(TextFormatter.stringToFormattedText(
                    "&eRestart required: " + setting + "."));
        }
        boolean healthy = catalogProblems.isEmpty()
                && quotaProviderProblems.isEmpty()
                && restartRequiredDrift.isEmpty()
                && profiles.state() != com.enviouse.sef.storage.repository.StorageRepository.RepositoryState.RECOVERY
                && profiles.state() != com.enviouse.sef.storage.repository.StorageRepository.RepositoryState.UNSUPPORTED
                && profiles.state() != com.enviouse.sef.storage.repository.StorageRepository.RepositoryState.ERROR
                && securityAudit.running()
                && securityAudit.writerAlive()
                && securityAudit.failures() == 0L
                && securityAudit.dropped() == 0L
                && !storage.recoveryMode();
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                healthy ? "&aNo kernel errors detected." : "&eKernel requires operator attention."), false);
        return 1;
    }

    private static int clientStatus(CommandSourceStack source) {
        var player = source.getPlayer();
        String connection = player == null
                ? "not applicable"
                : SefSessionManager.instance().session(player)
                        .map(session -> session.authorized()
                                ? "enhanced, " + Long.bitCount(session.features()) + " features"
                                : "connected, unauthorized")
                        .orElse("command fallback");
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&6SEF client protocol &7version &f" + SefProtocol.MAJOR + "." + SefProtocol.MINOR), false);
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&7Server mode: &f" + (SefNetwork.enhancedGuiActive() ? "enhanced optional" : "command only")
                        + " &8| &7connection: &f" + connection), false);
        if (player != null) {
            var preference = KernelServices.guiPreferences().preference(player.getUUID());
            source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                    "&7Preference: &f" + preference.presentationMode().name().toLowerCase(java.util.Locale.ROOT)
                            + " &8| &7pause: &f" + preference.pauseButtonVisible()
                            + " &8| &7hud: &f" + preference.hudEnabled()
                            + " &8| &7background: &fsharp"
                            + " &8| &7reduced motion: &f" + preference.reducedMotion()
                            + " &8| &7page size: &f" + preference.preferredPageSize()), false);
        }
        return 1;
    }

    private static int dashboard(CommandSourceStack source) {
        if (SefGuiServer.openDashboard(source.getPlayer())) {
            return 1;
        }
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&eThe enhanced dashboard is unavailable for this connection. "
                        + "Use &f/sef commands&e and the normal command routes instead."), false);
        return 1;
    }

    private static int dismissReminder(CommandSourceStack source) {
        var player = source.getPlayer();
        if (player == null) {
            source.sendFailure(TextFormatter.stringToFormattedText("&cOnly players can dismiss this reminder."));
            return 0;
        }
        if (!SefGuiRuntime.dismissReminder(player)) {
            source.sendFailure(TextFormatter.stringToFormattedText("&cThe reminder preference could not be saved."));
            return 0;
        }
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&aThe optional client reminder is dismissed for its current revision."), false);
        return 1;
    }

    private static int updatePreference(CommandSourceStack source, String actionId, String value) {
        return KernelCommandExecutor.execute(
                source,
                actionId,
                Map.of("value", value),
                () -> {
                    var player = source.getPlayer();
                    if (player == null) {
                        return 0;
                    }
                    try {
                        var current = KernelServices.guiPreferences().preference(player.getUUID());
                        var mode = current.presentationMode();
                        Boolean pause = null;
                        Boolean hud = null;
                        Boolean reducedMotion = null;
                        Boolean backgroundBlur = null;
                        switch (actionId) {
                            case "sef:gui.preference.mode" ->
                                    mode = com.enviouse.sef.gui.GuiPreferenceRepository.PresentationMode.valueOf(
                                            value.toUpperCase(java.util.Locale.ROOT));
                            case "sef:gui.preference.pause" -> pause = toggle(value);
                            case "sef:gui.preference.hud" -> hud = toggle(value);
                            case "sef:gui.preference.blur" -> {
                                backgroundBlur = toggle(value);
                                if (backgroundBlur) {
                                    throw new IllegalArgumentException("background blur is disabled");
                                }
                            }
                            case "sef:gui.preference.motion" -> reducedMotion = switch (value) {
                                case "full" -> false;
                                case "reduced" -> true;
                                default -> throw new IllegalArgumentException("invalid motion preference");
                            };
                            default -> throw new IllegalArgumentException("unknown preference action");
                        }
                        if (backgroundBlur == null) {
                            KernelServices.guiPreferences().updatePresentation(
                                    player.getUUID(),
                                    mode,
                                    pause,
                                    hud,
                                    reducedMotion,
                                    null);
                        } else {
                            KernelServices.guiPreferences().updateBackgroundBlur(
                                    player.getUUID(),
                                    backgroundBlur);
                        }
                        SefSessionManager.instance().refresh(player);
                        String message = backgroundBlur == null
                                ? "&aYour GUI preference was updated."
                                : "&aSEF screens will keep the world sharp.";
                        source.sendSuccess(() -> TextFormatter.stringToFormattedText(message), false);
                        return 1;
                    } catch (IllegalArgumentException exception) {
                        source.sendFailure(TextFormatter.stringToFormattedText(
                                "&cThat preference value is invalid."));
                        return 0;
                    }
                });
    }

    private static int updatePageSize(CommandSourceStack source, int pageSize) {
        return KernelCommandExecutor.execute(
                source,
                "sef:gui.preference.page_size",
                Map.of("value", Integer.toString(pageSize)),
                () -> {
                    var player = source.getPlayer();
                    if (player == null) {
                        return 0;
                    }
                    KernelServices.guiPreferences().updatePresentation(
                            player.getUUID(),
                            null,
                            null,
                            null,
                            null,
                            pageSize);
                    source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                            "&aYour GUI page size was updated."), false);
                    return 1;
                });
    }

    private static boolean toggle(String value) {
        return switch (value) {
            case "on" -> true;
            case "off" -> false;
            default -> throw new IllegalArgumentException("invalid toggle");
        };
    }

    private static boolean has(CommandSourceStack source, String permissionId) {
        PermissionNode<Boolean> node = KernelServices.permissionNode(permissionId);
        return node != null && PermissionService.has(source, node);
    }
}
