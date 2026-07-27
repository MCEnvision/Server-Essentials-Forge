package com.enviouse.sef.config.modules;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.gui.GuiPreferenceRepository;
import com.enviouse.sef.gui.protocol.SefGuiServer;
import com.enviouse.sef.gui.protocol.SefSessionManager;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.ConfirmationService;
import com.enviouse.sef.permissions.PermissionService;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class ModuleConfigCommands {
    private static final int PAGE_SIZE = 8;

    private ModuleConfigCommands() {
    }

    public static void attach(LiteralArgumentBuilder<CommandSourceStack> sefRoot) {
        sefRoot.then(configRoot());
        sefRoot.then(guisRoot());
        sefRoot.then(guiPreferenceRoot());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> configRoot() {
        return Commands.literal("config")
                .requires(source -> hasAny(
                        source,
                        "commands.config.modules",
                        "commands.config.status",
                        "commands.config.inspect",
                        "commands.config.diff",
                        "commands.config.validate",
                        "commands.config.reload",
                        "commands.config.history",
                        "commands.config.rollback",
                        "commands.config.explain",
                        "commands.config.edit",
                        "commands.config.migrate",
                        "commands.config.documentation"))
                .executes(context -> status(context.getSource()))
                .then(Commands.literal("modules")
                        .requires(source -> has(source, "commands.config.modules"))
                        .executes(context -> modules(context.getSource(), 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> modules(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "page")))))
                .then(Commands.literal("status")
                        .requires(source -> has(source, "commands.config.status"))
                        .executes(context -> status(context.getSource())))
                .then(Commands.literal("inspect")
                        .requires(source -> has(source, "commands.config.inspect"))
                        .then(moduleArgument("module")
                                .executes(context -> inspect(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "module"),
                                        1))
                                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                        .executes(context -> inspect(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "module"),
                                                IntegerArgumentType.getInteger(context, "page"))))))
                .then(Commands.literal("diff")
                        .requires(source -> has(source, "commands.config.diff"))
                        .then(moduleArgument("module")
                                .executes(context -> diff(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "module")))))
                .then(Commands.literal("validate")
                        .requires(source -> has(source, "commands.config.validate"))
                        .executes(context -> validate(context.getSource(), null))
                        .then(moduleArgument("module")
                                .executes(context -> validate(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "module")))))
                .then(Commands.literal("reload")
                        .requires(source -> has(source, "commands.config.reload"))
                        .executes(context -> reload(context.getSource(), null))
                        .then(moduleArgument("module")
                                .executes(context -> reload(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "module")))))
                .then(Commands.literal("history")
                        .requires(source -> has(source, "commands.config.history"))
                        .then(moduleArgument("module")
                                .executes(context -> history(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "module")))))
                .then(Commands.literal("rollback")
                        .requires(source -> has(source, "commands.config.rollback"))
                        .then(moduleArgument("module")
                                .then(Commands.argument("revision", LongArgumentType.longArg(1L))
                                        .executes(context -> rollback(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "module"),
                                                LongArgumentType.getLong(context, "revision"))))))
                .then(Commands.literal("explain")
                        .requires(source -> has(source, "commands.config.explain"))
                        .then(moduleArgument("module")
                                .then(settingArgument("setting", "module")
                                        .executes(context -> explain(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "module"),
                                                StringArgumentType.getString(context, "setting"))))))
                .then(Commands.literal("set")
                        .requires(source -> has(source, "commands.config.edit"))
                        .then(moduleArgument("module")
                                .then(settingArgument("setting", "module")
                                        .then(Commands.argument("expected_revision", LongArgumentType.longArg(1L))
                                                .then(Commands.argument("value", StringArgumentType.greedyString())
                                                        .executes(context -> set(
                                                                context.getSource(),
                                                                StringArgumentType.getString(context, "module"),
                                                                StringArgumentType.getString(context, "setting"),
                                                                LongArgumentType.getLong(context, "expected_revision"),
                                                                StringArgumentType.getString(context, "value"))))))))
                .then(Commands.literal("migrate")
                        .requires(source -> has(source, "commands.config.migrate"))
                        .then(Commands.literal("dryrun")
                                .executes(context -> migration(context.getSource(), 1))
                                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                        .executes(context -> migration(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "page")))))
                        .then(Commands.literal("apply")
                                .then(Commands.argument("expected_revision", LongArgumentType.longArg(1L))
                                        .executes(context -> migrationApply(
                                                context.getSource(),
                                                LongArgumentType.getLong(context, "expected_revision"),
                                                null))
                                        .then(Commands.literal("confirm")
                                                .then(Commands.argument("token", StringArgumentType.word())
                                                        .executes(context -> migrationApply(
                                                                context.getSource(),
                                                                LongArgumentType.getLong(
                                                                        context,
                                                                        "expected_revision"),
                                                                StringArgumentType.getString(
                                                                        context,
                                                                        "token"))))))))
                .then(Commands.literal("documentation")
                        .requires(source -> has(source, "commands.config.documentation"))
                        .then(Commands.literal("generate")
                                .executes(context -> documentation(context.getSource()))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> guisRoot() {
        return Commands.literal("guis")
                .requires(source -> hasAny(
                        source,
                        "commands.guis.status",
                        "commands.guis.enable",
                        "commands.guis.disable",
                        "commands.guis.auto",
                        "commands.guis.module",
                        "commands.guis.action",
                        "commands.guis.sessions",
                        "commands.guis.close",
                        "commands.guis.reload",
                        "commands.guis.doctor",
                        "commands.guis.explain",
                        "commands.guis.coverage"))
                .executes(context -> guiStatus(context.getSource()))
                .then(Commands.literal("status")
                        .requires(source -> has(source, "commands.guis.status"))
                        .executes(context -> guiStatus(context.getSource())))
                .then(Commands.literal("on")
                        .requires(source -> has(source, "commands.guis.enable"))
                        .executes(context -> guiMode(context.getSource(), "on")))
                .then(Commands.literal("off")
                        .requires(source -> has(source, "commands.guis.disable"))
                        .executes(context -> guiMode(context.getSource(), "off")))
                .then(Commands.literal("auto")
                        .requires(source -> has(source, "commands.guis.auto"))
                        .executes(context -> guiMode(context.getSource(), "auto")))
                .then(Commands.literal("module")
                        .requires(source -> has(source, "commands.guis.module"))
                        .then(moduleArgument("module")
                                .then(Commands.argument("mode", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            for (String mode : List.of(
                                                    "inherit", "on", "off", "command_only", "gui_preferred")) {
                                                builder.suggest(mode);
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> guiModule(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "module"),
                                                StringArgumentType.getString(context, "mode"))))))
                .then(Commands.literal("action")
                        .requires(source -> has(source, "commands.guis.action"))
                        .then(Commands.argument("action", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    KernelServices.catalog().entries().forEach(definition ->
                                            builder.suggest(definition.id()));
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("mode", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            for (String mode : List.of(
                                                    "inherit", "on", "off", "command_only", "gui_preferred")) {
                                                builder.suggest(mode);
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> guiAction(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "action"),
                                                StringArgumentType.getString(context, "mode"))))))
                .then(Commands.literal("sessions")
                        .requires(source -> has(source, "commands.guis.sessions"))
                        .executes(context -> guiSessions(context.getSource())))
                .then(Commands.literal("close")
                        .requires(source -> has(source, "commands.guis.close"))
                        .then(Commands.literal("all")
                                .executes(context -> closeAll(context.getSource())))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> close(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("reload")
                        .requires(source -> has(source, "commands.guis.reload"))
                        .executes(context -> reload(context.getSource(), "gui")))
                .then(Commands.literal("doctor")
                        .requires(source -> has(source, "commands.guis.doctor"))
                        .executes(context -> guiDoctor(context.getSource())))
                .then(Commands.literal("explain")
                        .requires(source -> has(source, "commands.guis.explain"))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("action", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            KernelServices.catalog().entries().forEach(definition ->
                                                    builder.suggest(definition.id()));
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> guiExplain(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "player"),
                                                StringArgumentType.getString(context, "action"))))))
                .then(Commands.literal("coverage")
                        .requires(source -> has(source, "commands.guis.coverage"))
                        .executes(context -> guiCoverage(context.getSource(), 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> guiCoverage(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "page")))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> guiPreferenceRoot() {
        return Commands.literal("gui")
                .requires(source -> source.getPlayer() != null
                        && has(source, "commands.gui.preference"))
                .executes(context -> openGui(context.getSource()))
                .then(Commands.literal("on")
                        .executes(context -> preference(context.getSource(), "gui")))
                .then(Commands.literal("off")
                        .executes(context -> preference(context.getSource(), "command")))
                .then(Commands.literal("auto")
                        .executes(context -> preference(context.getSource(), "auto")))
                .then(Commands.literal("reset")
                        .executes(context -> preference(context.getSource(), "auto")))
                .then(Commands.literal("status")
                        .executes(context -> preferenceStatus(context.getSource())));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> moduleArgument(
            String name
    ) {
        return Commands.argument(name, StringArgumentType.word())
                .suggests((context, builder) -> {
                    KernelServices.moduleConfigs().registry().definitions().forEach(module ->
                            builder.suggest(module.id()));
                    return builder.buildFuture();
                });
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> settingArgument(
            String name,
            String moduleArgument
    ) {
        return Commands.argument(name, StringArgumentType.word())
                .suggests((context, builder) -> {
                    try {
                        KernelServices.moduleConfigs().registry()
                                .require(StringArgumentType.getString(context, moduleArgument))
                                .settings()
                                .forEach(setting -> builder.suggest(setting.path()));
                    } catch (IllegalArgumentException ignored) {
                    }
                    return builder.buildFuture();
                });
    }

    private static int modules(CommandSourceStack source, int requestedPage) {
        return page(
                source,
                "configuration modules",
                KernelServices.moduleConfigs().modules(),
                requestedPage,
                module -> "&e" + module.moduleId()
                        + " &8| &f" + (module.enabled() ? "enabled" : "disabled")
                        + " &8| &7r" + module.revision());
    }

    private static int status(CommandSourceStack source) {
        ModuleConfigService service = KernelServices.moduleConfigs();
        long errors = service.diagnostics().stream()
                .filter(diagnostic -> diagnostic.severity() == ModuleConfigService.DiagnosticSeverity.ERROR)
                .count();
        info(source, "modular configuration");
        info(source, "running " + service.running()
                + ", revision " + service.revision()
                + ", modules " + service.modules().size()
                + ", errors " + errors);
        return errors == 0 ? 1 : 0;
    }

    private static int inspect(CommandSourceStack source, String moduleId, int requestedPage) {
        try {
            var definition = KernelServices.moduleConfigs().registry().require(moduleId);
            var snapshot = KernelServices.moduleConfigs().module(moduleId).orElse(null);
            if (snapshot == null) {
                return fail(source, "configuration module is unavailable");
            }
            return page(
                    source,
                    definition.id() + " settings",
                    definition.settings(),
                    requestedPage,
                    setting -> "&e" + setting.path()
                            + " &8| &f" + visibleValue(source, setting, snapshot.values().get(setting.path()))
                            + " &8| &7" + setting.applyClass().id());
        } catch (IllegalArgumentException exception) {
            return fail(source, exception.getMessage());
        }
    }

    private static int diff(CommandSourceStack source, String moduleId) {
        try {
            var diff = KernelServices.moduleConfigs().diff(moduleId);
            info(source, diff.moduleId() + " pending changes, revision " + diff.revision());
            if (diff.changes().isEmpty()) {
                info(source, "no pending changes");
            }
            diff.changes().forEach((path, change) ->
                    info(source, path + ", " + change.before() + " to " + change.after()));
            diff.diagnostics().forEach(message -> info(source, message));
            return diff.diagnostics().isEmpty() ? 1 : 0;
        } catch (IllegalArgumentException exception) {
            return fail(source, exception.getMessage());
        }
    }

    private static int validate(CommandSourceStack source, String moduleId) {
        try {
            var validation = KernelServices.moduleConfigs().validate(
                    moduleId == null ? List.of() : List.of(moduleId));
            info(source, "validation revision " + validation.revision()
                    + ", modules " + validation.modules().size());
            validation.warnings().forEach(message -> info(source, message));
            validation.errors().forEach(message -> fail(source, message));
            return validation.successful() ? 1 : 0;
        } catch (IllegalArgumentException exception) {
            return fail(source, exception.getMessage());
        }
    }

    private static int reload(CommandSourceStack source, String moduleId) {
        try {
            var publication = KernelServices.moduleConfigs().reload(
                    moduleId == null ? List.of() : List.of(moduleId),
                    "command");
            return publication(source, publication);
        } catch (IllegalArgumentException exception) {
            return fail(source, exception.getMessage());
        }
    }

    private static int history(CommandSourceStack source, String moduleId) {
        try {
            var entries = KernelServices.moduleConfigs().history(moduleId);
            info(source, moduleId + " configuration history");
            if (entries.isEmpty()) {
                info(source, "no retained revisions");
            }
            entries.stream().limit(32).forEach(entry ->
                    info(source, "revision " + entry.revision() + ", " + entry.recordedAt()));
            return 1;
        } catch (IllegalArgumentException exception) {
            return fail(source, exception.getMessage());
        }
    }

    private static int rollback(CommandSourceStack source, String moduleId, long historyRevision) {
        try {
            return publication(
                    source,
                    KernelServices.moduleConfigs().rollback(
                            moduleId,
                            historyRevision,
                            KernelServices.moduleConfigs().revision(),
                            actorId(source)));
        } catch (IllegalArgumentException exception) {
            return fail(source, exception.getMessage());
        }
    }

    private static int explain(CommandSourceStack source, String moduleId, String settingPath) {
        try {
            var service = KernelServices.moduleConfigs();
            var setting = service.registry().require(moduleId).settingsByPath().get(settingPath);
            if (setting == null) {
                return fail(source, "configuration setting is unknown");
            }
            info(source, moduleId + ", " + setting.path());
            info(source, "value " + visibleValue(source, setting, service.value(moduleId, setting.path())));
            info(source, "type " + setting.type().id()
                    + ", bounds " + setting.boundsDescription()
                    + ", apply " + setting.applyClass().id());
            info(source, setting.description());
            return 1;
        } catch (IllegalArgumentException exception) {
            return fail(source, exception.getMessage());
        }
    }

    private static int set(
            CommandSourceStack source,
            String moduleId,
            String settingPath,
            long expectedRevision,
            String value
    ) {
        try {
            return publication(
                    source,
                    KernelServices.moduleConfigs().publishSetting(
                            moduleId,
                            settingPath,
                            value,
                            expectedRevision,
                            actorId(source)));
        } catch (IllegalArgumentException exception) {
            return fail(source, exception.getMessage());
        }
    }

    private static int migration(CommandSourceStack source, int requestedPage) {
        var report = KernelServices.moduleConfigs().legacyMigrationReport();
        info(source, "legacy migration " + report.mode() + ", revision " + report.revision());
        info(source, "legacy common file present " + report.legacyFilePresent());
        if (!report.sourceFingerprint().isBlank()) {
            info(source, "source fingerprint " + report.sourceFingerprint());
        }
        List<String> details = new java.util.ArrayList<>();
        report.mappings().forEach(mapping -> details.add("map " + mapping));
        report.unmappedFields().forEach(field -> details.add("retain " + field));
        report.errors().forEach(error -> details.add("error " + error));
        return page(source, "legacy migration details", details, requestedPage, value -> value);
    }

    private static int migrationApply(
            CommandSourceStack source,
            long expectedRevision,
            String token
    ) {
        ModuleConfigService service = KernelServices.moduleConfigs();
        ModuleConfigService.MigrationReport report = service.legacyMigrationReport();
        if (report.revision() != expectedRevision) {
            return fail(source, "configuration revision changed");
        }
        if (!report.legacyFilePresent()) {
            return fail(source, "legacy common configuration file is missing");
        }
        if (!report.errors().isEmpty() || !report.mode().equals("dry run ready")) {
            return fail(source, "legacy migration dry run is blocked");
        }
        ConfirmationService.Request request = new ConfirmationService.Request(
                source.getPlayer() == null ? new UUID(0L, 0L) : source.getPlayer().getUUID(),
                "sef:config.migrate",
                Map.of(
                        "revision", Long.toString(expectedRevision),
                        "fingerprint", report.sourceFingerprint()),
                List.of(),
                "",
                0L,
                0L,
                0L,
                KernelServices.commandPolicies().revision());
        if (token == null) {
            ActionResult<ConfirmationService.IssuedToken> issued =
                    KernelServices.confirmations().issue(request, Duration.ofSeconds(60));
            if (!issued.successful()) {
                return fail(source, "legacy migration confirmation could not be issued");
            }
            info(source, "migration preview maps " + report.mappings().size()
                    + " fields and retains " + report.unmappedFields().size() + " legacy fields");
            info(source, "common.toml will be backed up and retained");
            info(source, "confirm with /sef config migrate apply "
                    + expectedRevision + " confirm " + issued.value().token());
            return 1;
        }
        ActionResult<ConfirmationService.Request> consumed =
                KernelServices.confirmations().consume(token, request);
        if (!consumed.successful()) {
            return fail(source, "legacy migration confirmation is invalid, stale, expired, or used");
        }
        return publication(source, service.migrateLegacy(
                expectedRevision,
                report.sourceFingerprint(),
                actorId(source)));
    }

    private static int documentation(CommandSourceStack source) {
        var result = KernelServices.moduleConfigs().generateDocumentation();
        if (!result.successful()) {
            return fail(source, result.detail());
        }
        success(source, "configuration reference generated");
        return 1;
    }

    private static int guiStatus(CommandSourceStack source) {
        var service = KernelServices.moduleConfigs();
        info(source, "enhanced GUI policy " + service.value("gui", "gui.mode"));
        info(source, "sessions " + SefSessionManager.instance().activeCount()
                + ", open panels " + SefGuiServer.openPanelCount()
                + ", revision " + service.revision());
        return 1;
    }

    private static int guiMode(CommandSourceStack source, String mode) {
        int result = publication(
                source,
                KernelServices.moduleConfigs().publishSetting(
                        "gui",
                        "gui.mode",
                        mode,
                        KernelServices.moduleConfigs().revision(),
                        actorId(source)));
        if (result == 1 && mode.equals("off")) {
            SefGuiServer.clear();
            SefSessionManager.instance().clear();
        }
        return result;
    }

    private static int guiModule(CommandSourceStack source, String moduleId, String mode) {
        try {
            if (moduleId.equalsIgnoreCase("gui")) {
                return fail(source, "use the global GUI mode command for the gui module");
            }
            return publication(
                    source,
                    KernelServices.moduleConfigs().publishSetting(
                            moduleId,
                            "gui.mode",
                            mode,
                            KernelServices.moduleConfigs().revision(),
                            actorId(source)));
        } catch (IllegalArgumentException exception) {
            return fail(source, exception.getMessage());
        }
    }

    private static int guiAction(CommandSourceStack source, String actionId, String mode) {
        if (KernelServices.catalog().find(actionId).isEmpty()) {
            return fail(source, "GUI action is unknown");
        }
        try {
            return publication(
                    source,
                    KernelServices.moduleConfigs().publishGuiActionMode(
                            actionId,
                            mode,
                            KernelServices.moduleConfigs().revision(),
                            actorId(source)));
        } catch (IllegalArgumentException exception) {
            return fail(source, exception.getMessage());
        }
    }

    private static int guiSessions(CommandSourceStack source) {
        info(source, "negotiated sessions " + SefSessionManager.instance().activeCount()
                + ", pending " + SefSessionManager.instance().pendingCount()
                + ", open panels " + SefGuiServer.openPanelCount());
        return 1;
    }

    private static int close(CommandSourceStack source, ServerPlayer target) {
        SefSessionManager.instance().logout(target.getUUID());
        SefGuiServer.logout(target.getUUID());
        success(source, "enhanced session closed for " + target.getGameProfile().getName());
        return 1;
    }

    private static int closeAll(CommandSourceStack source) {
        int count = SefSessionManager.instance().activeCount();
        SefGuiServer.clear();
        SefSessionManager.instance().clear();
        success(source, "closed " + count + " enhanced sessions");
        return 1;
    }

    private static int guiDoctor(CommandSourceStack source) {
        var service = KernelServices.moduleConfigs();
        long errors = service.diagnostics().stream()
                .filter(diagnostic -> diagnostic.severity() == ModuleConfigService.DiagnosticSeverity.ERROR)
                .count();
        info(source, "GUI mode " + service.value("gui", "gui.mode")
                + ", catalog actions " + KernelServices.universalGuiCatalog().actionCount()
                + ", diagnostics " + errors);
        service.diagnostics().stream().limit(16).forEach(diagnostic ->
                info(source, diagnostic.severity().name().toLowerCase(Locale.ROOT)
                        + ", " + diagnostic.operation() + ", " + diagnostic.message()));
        return errors == 0 ? 1 : 0;
    }

    private static int guiExplain(
            CommandSourceStack source,
            ServerPlayer player,
            String actionId
    ) {
        var definition = KernelServices.catalog().find(actionId).orElse(null);
        if (definition == null) {
            return fail(source, "GUI action is unknown");
        }
        String module = KernelServices.moduleConfigs().moduleForFeature(definition.featureId());
        String globalMode = KernelServices.moduleConfigs().value("gui", "gui.mode");
        String moduleMode = KernelServices.moduleConfigs().value(module, "gui.mode");
        String actionMode = KernelServices.moduleConfigs().guiActionMode(definition.id());
        String effective = KernelServices.moduleConfigs().effectiveGuiMode(module, definition.id());
        var preference = KernelServices.guiPreferences().preference(player.getUUID());
        var session = SefSessionManager.instance().session(player);
        boolean permission = definition.permissionIds().stream().allMatch(permissionId -> {
            PermissionNode<Boolean> node = KernelServices.permissionNode(permissionId);
            return node != null && PermissionService.has(player, node);
        });
        info(source, "GUI explanation for " + player.getGameProfile().getName());
        info(source, "action " + definition.id() + ", module " + module);
        info(source, "global " + globalMode + ", module " + moduleMode
                + ", action " + actionMode + ", effective " + effective);
        info(source, "client " + (session.isPresent() ? "compatible" : "command fallback")
                + ", permission " + permission
                + ", preference " + preference.presentationMode().name().toLowerCase(Locale.ROOT));
        info(source, "fallback /" + definition.canonicalRoute()
                + ", revision " + KernelServices.moduleConfigs().revision());
        return 1;
    }

    private static int guiCoverage(CommandSourceStack source, int requestedPage) {
        return page(
                source,
                "GUI workflow coverage",
                KernelServices.catalog().entries().stream()
                        .filter(definition -> definition.playerFacing())
                        .toList(),
                requestedPage,
                definition -> "&e" + definition.id()
                        + " &8| &f" + definition.guiDescriptorId()
                        + " &8| &7/" + definition.canonicalRoute());
    }

    private static int openGui(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return fail(source, "a player is required");
        }
        if (KernelServices.moduleConfigs().value("gui", "gui.mode").equals("off")) {
            return fail(source, "enhanced GUIs are disabled, use commands");
        }
        return SefGuiServer.openDashboard(player) ? 1 : fail(source, "the enhanced GUI is unavailable, use commands");
    }

    private static int preference(CommandSourceStack source, String mode) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return fail(source, "a player is required");
        }
        GuiPreferenceRepository.PresentationMode presentation = switch (mode) {
            case "gui" -> GuiPreferenceRepository.PresentationMode.GUI;
            case "command" -> GuiPreferenceRepository.PresentationMode.COMMAND;
            default -> GuiPreferenceRepository.PresentationMode.AUTO;
        };
        KernelServices.guiPreferences().updatePresentation(
                player.getUUID(),
                presentation,
                null,
                null,
                null,
                null);
        SefSessionManager.instance().refresh(player);
        success(source, "GUI preference set to " + mode);
        return 1;
    }

    private static int preferenceStatus(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return fail(source, "a player is required");
        }
        var preference = KernelServices.guiPreferences().preference(player.getUUID());
        info(source, "GUI preference " + preference.presentationMode().name().toLowerCase(Locale.ROOT)
                + ", HUD " + preference.hudEnabled()
                + ", pause button " + preference.pauseButtonVisible());
        return 1;
    }

    private static int publication(CommandSourceStack source, ModuleConfigService.Publication publication) {
        if (!publication.successful()) {
            return fail(source, publication.detail());
        }
        success(source, publication.detail() + ", revision " + publication.revision());
        if (!publication.changedModules().isEmpty()) {
            info(source, "changed modules " + String.join(", ", publication.changedModules()));
        }
        return 1;
    }

    private static String visibleValue(
            CommandSourceStack source,
            ModuleConfigRegistry.SettingDefinition setting,
            String value
    ) {
        return setting.sensitivity() == ModuleConfigRegistry.Sensitivity.PUBLIC
                || has(source, "commands.config.sensitive")
                ? value
                : "<redacted>";
    }

    private static <T> int page(
            CommandSourceStack source,
            String title,
            List<T> values,
            int requestedPage,
            java.util.function.Function<T, String> formatter
    ) {
        int pages = Math.max(1, (values.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int page = Math.min(requestedPage, pages);
        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(values.size(), start + PAGE_SIZE);
        info(source, title + ", page " + page + " of " + pages);
        for (int index = start; index < end; index++) {
            info(source, formatter.apply(values.get(index)));
        }
        if (values.isEmpty()) {
            info(source, "no entries");
        }
        return 1;
    }

    private static boolean hasAny(CommandSourceStack source, String... permissions) {
        for (String permission : permissions) {
            if (has(source, permission)) {
                return true;
            }
        }
        return false;
    }

    private static boolean has(CommandSourceStack source, String permission) {
        PermissionNode<Boolean> node = PermissionsHandler.phasePermission(permission);
        return node != null && PermissionService.has(source, node);
    }

    private static UUID actorId(CommandSourceStack source) {
        return source.getPlayer() == null ? null : source.getPlayer().getUUID();
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
