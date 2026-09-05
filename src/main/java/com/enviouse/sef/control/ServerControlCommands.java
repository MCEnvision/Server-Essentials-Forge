package com.enviouse.sef.control;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.ConfirmationService;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.time.Duration;

public final class ServerControlCommands {
    private static final int PAGE_SIZE = 8;
    private static final UUID CONSOLE_ID = new UUID(0L, 0L);
    private static final Map<String, String> DIRECT_ROOTS = directRoots();

    private ServerControlCommands() {
    }

    public static void attach(LiteralArgumentBuilder<CommandSourceStack> sefRoot) {
        LiteralArgumentBuilder<CommandSourceStack> control = Commands.literal("control")
                .requires(source -> has(source, "commands.control"))
                .executes(context -> catalog(context.getSource(), 1))
                .then(Commands.literal("catalog")
                        .requires(source -> has(source, "commands.control.catalog"))
                        .executes(context -> catalog(context.getSource(), 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> catalog(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "page")))))
                .then(Commands.literal("status")
                        .requires(source -> has(source, "commands.control.status"))
                        .executes(context -> status(context.getSource())))
                .then(Commands.literal("recovery")
                        .requires(source -> has(source, "commands.control.status"))
                        .executes(context -> recovery(context.getSource(), 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> recovery(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "page")))))
                .then(Commands.literal("reconcile")
                        .requires(source -> has(source, "commands.control.status"))
                        .then(Commands.argument("operation", StringArgumentType.word())
                                .then(Commands.argument("outcome", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            builder.suggest("applied");
                                            builder.suggest("not_applied");
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("note", StringArgumentType.greedyString())
                                                .executes(context -> reconcile(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "operation"),
                                                        StringArgumentType.getString(context, "outcome"),
                                                        StringArgumentType.getString(context, "note")))))))
                .then(Commands.literal("view")
                        .then(Commands.argument("record", StringArgumentType.word())
                                .executes(context -> view(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "record")))))
                .then(Commands.literal("history")
                        .then(Commands.argument("record", StringArgumentType.word())
                                .executes(context -> history(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "record"),
                                        1))
                                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                        .executes(context -> history(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "record"),
                                                IntegerArgumentType.getInteger(context, "page"))))));
        for (ServerControlCatalog.FeatureDefinition feature : ServerControlCatalog.FEATURES) {
            control.then(featureRoot(feature));
        }
        sefRoot.then(control);
    }

    public static void registerDirect(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (Map.Entry<String, String> entry : DIRECT_ROOTS.entrySet()) {
            if (dispatcher.getRoot().getChild(entry.getKey()) != null) {
                continue;
            }
            ServerControlCatalog.FeatureDefinition feature = ServerControlCatalog.require(entry.getValue());
            dispatcher.register(featureRoot(feature, entry.getKey()));
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> featureRoot(
            ServerControlCatalog.FeatureDefinition feature
    ) {
        return featureRoot(feature, feature.id());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> featureRoot(
            ServerControlCatalog.FeatureDefinition feature,
            String literal
    ) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(literal)
                .requires(source -> hasAny(
                        source,
                        permission(feature, "view"),
                        permission(feature, "create"),
                        permission(feature, "manage")))
                .executes(context -> list(context.getSource(), feature, 1))
                .then(Commands.literal("list")
                        .requires(source -> has(source, permission(feature, "view"))
                                || has(source, permission(feature, "manage")))
                        .executes(context -> list(context.getSource(), feature, 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> list(
                                        context.getSource(),
                                        feature,
                                        IntegerArgumentType.getInteger(context, "page")))))
                .then(Commands.literal("fields")
                        .requires(source -> has(source, permission(feature, "view"))
                                || has(source, permission(feature, "manage")))
                        .executes(context -> fields(context.getSource(), feature, 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> fields(
                                        context.getSource(),
                                        feature,
                                        IntegerArgumentType.getInteger(context, "page")))))
                .then(Commands.literal("manage")
                        .requires(source -> has(source, permission(feature, "manage")))
                        .executes(context -> list(context.getSource(), feature, 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> list(
                                        context.getSource(),
                                        feature,
                                        IntegerArgumentType.getInteger(context, "page")))))
                .then(Commands.literal("create")
                        .requires(source -> has(source, permission(feature, "create")))
                        .then(Commands.argument("title", StringArgumentType.string())
                                .executes(context -> create(
                                        context.getSource(),
                                        feature,
                                        null,
                                        StringArgumentType.getString(context, "title"),
                                        ""))
                                .then(Commands.argument("details", StringArgumentType.greedyString())
                                        .executes(context -> create(
                                                context.getSource(),
                                                feature,
                                                null,
                                                StringArgumentType.getString(context, "title"),
                                                StringArgumentType.getString(context, "details"))))))
                .then(Commands.literal("createfor")
                        .requires(source -> has(source, permission(feature, "create"))
                                && has(source, permission(feature, "others")))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("title", StringArgumentType.string())
                                        .executes(context -> create(
                                                context.getSource(),
                                                feature,
                                                EntityArgument.getPlayer(context, "player"),
                                                StringArgumentType.getString(context, "title"),
                                                ""))
                                        .then(Commands.argument("details", StringArgumentType.greedyString())
                                                .executes(context -> create(
                                                        context.getSource(),
                                                        feature,
                                                        EntityArgument.getPlayer(context, "player"),
                                                        StringArgumentType.getString(context, "title"),
                                                        StringArgumentType.getString(context, "details")))))))
                .then(Commands.literal("configure")
                        .requires(source -> has(source, permission(feature, "manage")))
                        .then(Commands.argument("record", StringArgumentType.word())
                                .then(Commands.argument("revision", LongArgumentType.longArg(1L))
                                        .then(Commands.argument("field", StringArgumentType.word())
                                                .suggests((context, builder) -> {
                                                    ServerControlSchemaRegistry.require(feature.id()).fields()
                                                            .forEach(field -> builder.suggest(field.id()));
                                                    return builder.buildFuture();
                                                })
                                                .then(Commands.argument("value", StringArgumentType.greedyString())
                                                        .executes(context -> configure(
                                                                context.getSource(),
                                                                feature,
                                                                StringArgumentType.getString(context, "record"),
                                                                LongArgumentType.getLong(context, "revision"),
                                                                StringArgumentType.getString(context, "field"),
                                                                StringArgumentType.getString(context, "value"),
                                                                false)))))))
                .then(Commands.literal("unset")
                        .requires(source -> has(source, permission(feature, "manage")))
                        .then(Commands.argument("record", StringArgumentType.word())
                                .then(Commands.argument("revision", LongArgumentType.longArg(1L))
                                        .then(Commands.argument("field", StringArgumentType.word())
                                                .suggests((context, builder) -> {
                                                    ServerControlSchemaRegistry.require(feature.id()).fields()
                                                            .stream()
                                                            .filter(field -> !field.required())
                                                            .forEach(field -> builder.suggest(field.id()));
                                                    return builder.buildFuture();
                                                })
                                                .executes(context -> configure(
                                                        context.getSource(),
                                                        feature,
                                                        StringArgumentType.getString(context, "record"),
                                                        LongArgumentType.getLong(context, "revision"),
                                                        StringArgumentType.getString(context, "field"),
                                                        "",
                                                        true))))))
                .then(Commands.literal("preview")
                        .requires(source -> has(source, permission(feature, "manage")))
                        .then(Commands.argument("record", StringArgumentType.word())
                                .then(Commands.argument("revision", LongArgumentType.longArg(1L))
                                        .executes(context -> preview(
                                                context.getSource(),
                                                feature,
                                                StringArgumentType.getString(context, "record"),
                                                LongArgumentType.getLong(context, "revision"))))))
                .then(Commands.literal("execute")
                        .requires(source -> has(source, permission(feature, "manage")))
                        .then(Commands.argument("record", StringArgumentType.word())
                                .then(Commands.argument("revision", LongArgumentType.longArg(1L))
                                        .executes(context -> executeRecord(
                                                context.getSource(),
                                                feature,
                                                StringArgumentType.getString(context, "record"),
                                                LongArgumentType.getLong(context, "revision"),
                                                null))
                                        .then(Commands.literal("confirm")
                                                .then(Commands.argument("token", StringArgumentType.word())
                                                        .executes(context -> executeRecord(
                                                                context.getSource(),
                                                                feature,
                                                                StringArgumentType.getString(context, "record"),
                                                                LongArgumentType.getLong(context, "revision"),
                                                                StringArgumentType.getString(context, "token"))))))))
                .then(Commands.literal("state")
                        .requires(source -> has(source, permission(feature, "manage")))
                        .then(Commands.argument("record", StringArgumentType.word())
                                .then(Commands.argument("state", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            feature.states().stream()
                                                    .map(value -> value.name().toLowerCase(Locale.ROOT))
                                                    .forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("revision", LongArgumentType.longArg(1L))
                                                .executes(context -> transition(
                                                        context.getSource(),
                                                        feature,
                                                        StringArgumentType.getString(context, "record"),
                                                        StringArgumentType.getString(context, "state"),
                                                        LongArgumentType.getLong(context, "revision"),
                                                        ""))
                                                .then(Commands.argument("note", StringArgumentType.greedyString())
                                                        .executes(context -> transition(
                                                                context.getSource(),
                                                                feature,
                                                                StringArgumentType.getString(context, "record"),
                                                                StringArgumentType.getString(context, "state"),
                                                                LongArgumentType.getLong(context, "revision"),
                                                                StringArgumentType.getString(context, "note"))))))))
                .then(Commands.literal("update")
                        .requires(source -> has(source, permission(feature, "manage")))
                        .then(Commands.argument("record", StringArgumentType.word())
                                .then(Commands.argument("revision", LongArgumentType.longArg(1L))
                                        .then(Commands.argument("title", StringArgumentType.string())
                                                .executes(context -> update(
                                                        context.getSource(),
                                                        feature,
                                                        StringArgumentType.getString(context, "record"),
                                                        LongArgumentType.getLong(context, "revision"),
                                                        StringArgumentType.getString(context, "title"),
                                                        ""))
                                                .then(Commands.argument("details", StringArgumentType.greedyString())
                                                        .executes(context -> update(
                                                                context.getSource(),
                                                                feature,
                                                                StringArgumentType.getString(context, "record"),
                                                                LongArgumentType.getLong(context, "revision"),
                                                                StringArgumentType.getString(context, "title"),
                                                                StringArgumentType.getString(context, "details"))))))));
        return root;
    }

    private static int catalog(CommandSourceStack source, int requestedPage) {
        return KernelCommandExecutor.execute(
                source,
                "sef:control.catalog",
                Map.of("page", Integer.toString(requestedPage)),
                () -> {
                    List<ServerControlCatalog.FeatureDefinition> visible = ServerControlCatalog.FEATURES.stream()
                            .filter(feature -> hasAny(
                                    source,
                                    permission(feature, "view"),
                                    permission(feature, "create"),
                                    permission(feature, "manage")))
                            .toList();
                    return page(
                            source,
                            "server control catalog",
                            visible,
                            requestedPage,
                            feature -> "&e" + feature.id()
                                    + " &8| &7" + feature.title()
                                    + " &8| &f" + feature.category()
                                    + (feature.dangerous() ? " &8| &cdangerous" : ""));
                });
    }

    private static int status(CommandSourceStack source) {
        return KernelCommandExecutor.execute(
                source,
                "sef:control.status",
                Map.of(),
                () -> {
                    var diagnostic = KernelServices.serverControls().diagnostic();
                    info(source, "server control repository");
                    info(source, "records " + diagnostic.records()
                            + ", active " + diagnostic.activeRecords()
                            + ", history " + diagnostic.historyEntries());
                    info(source, "executions " + diagnostic.executions()
                            + ", incomplete " + diagnostic.incompleteExecutions());
                    info(source, "revision " + diagnostic.revision()
                            + ", state " + diagnostic.state().name().toLowerCase(Locale.ROOT)
                            + ", dirty " + diagnostic.dirty());
                    return 1;
                });
    }

    private static int recovery(CommandSourceStack source, int requestedPage) {
        List<ServerControlRepository.ExecutionOperation> visible =
                KernelServices.serverControls()
                        .executions(ServerControlRepository.ExecutionStatus.OUTCOME_UNKNOWN)
                        .stream()
                        .filter(operation -> has(
                                source,
                                permission(
                                        ServerControlCatalog.require(operation.featureId()),
                                        "manage")))
                        .toList();
        return KernelCommandExecutor.execute(
                source,
                "sef:control.status",
                Map.of(
                        "view", "recovery",
                        "page", Integer.toString(requestedPage)),
                () -> page(
                        source,
                        "server control recovery",
                        visible,
                        requestedPage,
                        operation -> "&e" + operation.id()
                                + " &8| &f" + operation.featureId()
                                + " &8| &7record " + operation.recordId()
                                + " &8| &7" + value(operation.detail())));
    }

    private static int reconcile(
            CommandSourceStack source,
            String operationInput,
            String outcome,
            String note
    ) {
        UUID operationId = uuid(operationInput);
        if (operationId == null) {
            return fail(source, "operation id is invalid");
        }
        ServerControlRepository.ExecutionOperation operation =
                KernelServices.serverControls().execution(operationId).orElse(null);
        if (operation == null
                || operation.status() != ServerControlRepository.ExecutionStatus.OUTCOME_UNKNOWN) {
            return fail(source, "execution recovery record not found");
        }
        ServerControlCatalog.FeatureDefinition feature =
                ServerControlCatalog.require(operation.featureId());
        if (!has(source, permission(feature, "manage"))) {
            return fail(source, "you cannot reconcile this feature");
        }
        boolean applied;
        if ("applied".equalsIgnoreCase(outcome)) {
            applied = true;
        } else if ("not_applied".equalsIgnoreCase(outcome)) {
            applied = false;
        } else {
            return fail(source, "outcome must be applied or not_applied");
        }
        return execute(
                source,
                "sef:control." + feature.id() + ".manage",
                Map.of(
                        "feature", feature.id(),
                        "operation", "reconcile",
                        "execution", operationId.toString(),
                        "outcome", applied ? "applied" : "not_applied"),
                () -> {
                    ActionResult<ServerControlRepository.ExecutionOperation> result =
                            KernelServices.serverControls().reconcileExecution(
                                    operationId,
                                    actorId(source),
                                    applied,
                                    note);
                    if (!result.successful()) {
                        return fail(source, detail(result));
                    }
                    try {
                        KernelServices.serverControls().flush();
                    } catch (java.io.IOException | RuntimeException exception) {
                        return fail(source, "execution reconciliation could not be durably saved");
                    }
                    success(
                            source,
                            "execution reconciled as "
                                    + result.value().status().name().toLowerCase(Locale.ROOT)
                                    + ", " + operationId);
                    return 1;
                },
                permission(feature, "manage"));
    }

    private static int list(
            CommandSourceStack source,
            ServerControlCatalog.FeatureDefinition feature,
            int requestedPage
    ) {
        if (!has(source, permission(feature, "view"))
                && !has(source, permission(feature, "manage"))) {
            return fail(source, "you cannot view this feature");
        }
        String actionId = has(source, permission(feature, "view"))
                ? "sef:control." + feature.id() + ".view"
                : "sef:control." + feature.id() + ".manage";
        return KernelCommandExecutor.execute(
                source,
                actionId,
                Map.of(
                        "feature", feature.id(),
                        "page", Integer.toString(requestedPage)),
                () -> {
                    boolean manage = has(source, permission(feature, "manage"));
                    UUID actor = actorId(source);
                    List<ServerControlRepository.ControlRecord> visible = KernelServices.serverControls()
                            .records(feature.id())
                            .stream()
                            .filter(record -> manage
                                    || record.ownerId().equals(actor)
                                    || actor.equals(record.subjectId()))
                            .toList();
                    return page(
                            source,
                            feature.title(),
                            visible,
                            requestedPage,
                            record -> "&e" + record.id()
                                    + " &8| &f" + record.title()
                                    + " &8| &7" + record.state().name().toLowerCase(Locale.ROOT)
                                    + " &8| &7r" + record.revision());
                });
    }

    private static int fields(
            CommandSourceStack source,
            ServerControlCatalog.FeatureDefinition feature,
            int requestedPage
    ) {
        String actionId = has(source, permission(feature, "view"))
                ? "sef:control." + feature.id() + ".view"
                : "sef:control." + feature.id() + ".manage";
        return KernelCommandExecutor.execute(
                source,
                actionId,
                Map.of(
                        "feature", feature.id(),
                        "page", Integer.toString(requestedPage),
                        "view", "fields"),
                () -> {
                    ServerControlSchemaRegistry.FeatureSchema schema =
                            ServerControlSchemaRegistry.require(feature.id());
                    info(source, "workflow " + schema.workflowId()
                            + ", screen " + schema.screen().name().toLowerCase(Locale.ROOT)
                            + ", hud " + schema.hud().name().toLowerCase(Locale.ROOT));
                    return page(
                            source,
                            feature.title() + " fields",
                            schema.fields(),
                            requestedPage,
                            field -> "&e" + field.id()
                                    + " &8| &f" + field.type().name().toLowerCase(Locale.ROOT)
                                    + " &8| &7" + (field.required() ? "required" : "optional"));
                });
    }

    private static int configure(
            CommandSourceStack source,
            ServerControlCatalog.FeatureDefinition feature,
            String recordInput,
            long expectedRevision,
            String field,
            String value,
            boolean remove
    ) {
        UUID recordId = uuid(recordInput);
        if (recordId == null) {
            return fail(source, "record id is invalid");
        }
        ServerControlRepository.ControlRecord current =
                KernelServices.serverControls().find(recordId).orElse(null);
        if (current == null || !current.featureId().equals(feature.id())) {
            return fail(source, "record not found");
        }
        if (!mayTargetRecord(source, current, feature)) {
            return 0;
        }
        return execute(
                source,
                "sef:control." + feature.id() + ".manage",
                Map.of(
                        "feature", feature.id(),
                        "record", recordId.toString(),
                        "field", field,
                        "operation", remove ? "unset" : "configure",
                        "revision", Long.toString(expectedRevision)),
                () -> result(
                        source,
                        KernelServices.serverControls().configure(
                                recordId,
                                actorId(source),
                                field,
                                value,
                                remove,
                                expectedRevision),
                        remove ? "server control field removed" : "server control field configured"),
                permission(feature, "manage"));
    }

    private static int preview(
            CommandSourceStack source,
            ServerControlCatalog.FeatureDefinition feature,
            String recordInput,
            long expectedRevision
    ) {
        UUID recordId = uuid(recordInput);
        if (recordId == null) {
            return fail(source, "record id is invalid");
        }
        ServerControlRepository.ControlRecord current =
                KernelServices.serverControls().find(recordId).orElse(null);
        if (current == null || !current.featureId().equals(feature.id())) {
            return fail(source, "record not found");
        }
        if (!mayTargetRecord(source, current, feature)) {
            return 0;
        }
        return execute(
                source,
                "sef:control." + feature.id() + ".manage",
                Map.of(
                        "feature", feature.id(),
                        "record", recordId.toString(),
                        "operation", "preview",
                        "revision", Long.toString(expectedRevision)),
                () -> {
                    var preview = KernelServices.serverControlExecutions().preview(recordId, expectedRevision);
                    info(source, feature.title() + " execution preview");
                    preview.effects().forEach(effect -> info(source, effect));
                    if (!preview.missingFields().isEmpty()) {
                        info(source, "missing fields " + String.join(", ", preview.missingFields()));
                    }
                    info(source, "confirmation " + preview.confirmationRequired()
                            + ", reversible " + preview.reversible()
                            + ", result " + preview.detail());
                    return preview.ready() ? 1 : 0;
                },
                permission(feature, "manage"));
    }

    private static int executeRecord(
            CommandSourceStack source,
            ServerControlCatalog.FeatureDefinition feature,
            String recordInput,
            long expectedRevision,
            String token
    ) {
        UUID recordId = uuid(recordInput);
        if (recordId == null) {
            return fail(source, "record id is invalid");
        }
        ServerControlRepository.ControlRecord current =
                KernelServices.serverControls().find(recordId).orElse(null);
        if (current == null || !current.featureId().equals(feature.id())) {
            return fail(source, "record not found");
        }
        if (!mayTargetRecord(source, current, feature)) {
            return 0;
        }
        var preview = KernelServices.serverControlExecutions().preview(recordId, expectedRevision);
        if (!preview.ready()) {
            return fail(source, preview.detail());
        }
        ConfirmationService.Request confirmation = confirmation(
                source,
                feature,
                current,
                expectedRevision);
        if (preview.confirmationRequired()) {
            if (token == null) {
                ActionResult<ConfirmationService.IssuedToken> issued = KernelServices.confirmations().issue(
                        confirmation,
                        Duration.ofSeconds(60));
                if (!issued.successful()) {
                    return fail(source, "confirmation token could not be issued");
                }
                info(source, "confirmation required, run /sef control "
                        + feature.id() + " execute " + recordId + " " + expectedRevision
                        + " confirm " + issued.value().token());
                return 1;
            }
            ActionResult<ConfirmationService.Request> consumed =
                    KernelServices.confirmations().consume(token, confirmation);
            if (!consumed.successful()) {
                return fail(source, "confirmation token is invalid, expired, used, or stale");
            }
        } else if (token != null) {
            return fail(source, "this action does not require a confirmation token");
        }
        return execute(
                source,
                "sef:control." + feature.id() + ".manage",
                Map.of(
                        "feature", feature.id(),
                        "record", recordId.toString(),
                        "operation", "execute",
                        "revision", Long.toString(expectedRevision)),
                () -> {
                    ActionResult<com.enviouse.sef.control.ServerControlExecutionService.Execution> result =
                            KernelServices.serverControlExecutions().execute(
                                    recordId,
                                    actorId(source),
                                    expectedRevision,
                                    !preview.confirmationRequired() || token != null,
                                    new com.enviouse.sef.control.ServerControlExecutionService.ExecutionContext() {
                                        @Override
                                        public Object server() {
                                            return source.getServer();
                                        }

                                        @Override
                                        public Object source() {
                                            return source;
                                        }
                                    });
                    if (!result.successful()) {
                        return fail(source, detail(result));
                    }
                    success(source, result.value().detail()
                            + ", revision " + result.value().revision());
                    return 1;
                },
                permission(feature, "manage"));
    }

    private static ConfirmationService.Request confirmation(
            CommandSourceStack source,
            ServerControlCatalog.FeatureDefinition feature,
            ServerControlRepository.ControlRecord record,
            long expectedRevision
    ) {
        return new ConfirmationService.Request(
                actorId(source),
                "sef:control." + feature.id() + ".manage",
                Map.of(
                        "record", record.id().toString(),
                        "revision", Long.toString(expectedRevision),
                        "operation", "execute"),
                record.subjectId() == null ? List.of() : List.of(record.subjectId()),
                "",
                0L,
                0L,
                0L,
                KernelServices.commandPolicies().revision());
    }

    private static int view(CommandSourceStack source, String recordInput) {
        UUID recordId = uuid(recordInput);
        if (recordId == null) {
            return fail(source, "record id is invalid");
        }
        ServerControlRepository.ControlRecord record = KernelServices.serverControls()
                .find(recordId)
                .orElse(null);
        if (record == null) {
            return fail(source, "record not found");
        }
        ServerControlCatalog.FeatureDefinition feature = ServerControlCatalog.require(record.featureId());
        boolean manage = has(source, permission(feature, "manage"));
        UUID actor = actorId(source);
        if (!has(source, permission(feature, "view")) && !manage) {
            return fail(source, "you cannot view this feature");
        }
        if (!manage && !record.ownerId().equals(actor) && !actor.equals(record.subjectId())) {
            return fail(source, "record not found");
        }
        boolean sensitive = !feature.sensitive() || has(source, permission(feature, "sensitive"));
        String action = manage
                ? "sef:control." + feature.id() + ".manage"
                : "sef:control." + feature.id() + ".view";
        String requiredPermission = manage
                ? permission(feature, "manage")
                : permission(feature, "view");
        return execute(
                source,
                action,
                Map.of(
                        "feature", feature.id(),
                        "record", record.id().toString(),
                        "operation", "view"),
                () -> {
                    info(source, feature.title() + ", " + record.id());
                    info(source, "state " + record.state().name().toLowerCase(Locale.ROOT)
                            + ", revision " + record.revision()
                            + ", title " + record.title());
                    if (sensitive) {
                        info(source, "details " + value(record.details()));
                        info(source, "owner " + record.ownerId()
                                + ", subject " + (record.subjectId() == null ? "none" : record.subjectId()));
                        info(source, "created " + record.createdAt()
                                + ", updated " + record.updatedAt()
                                + ", expires " + (record.expiresAt() == null ? "never" : record.expiresAt()));
                    } else {
                        info(source, "sensitive fields are redacted");
                    }
                    return 1;
                },
                requiredPermission);
    }

    private static int history(CommandSourceStack source, String recordInput, int requestedPage) {
        UUID recordId = uuid(recordInput);
        if (recordId == null) {
            return fail(source, "record id is invalid");
        }
        ServerControlRepository.ControlRecord record = KernelServices.serverControls()
                .find(recordId)
                .orElse(null);
        if (record == null) {
            return fail(source, "record not found");
        }
        ServerControlCatalog.FeatureDefinition feature = ServerControlCatalog.require(record.featureId());
        if (!has(source, permission(feature, "manage"))) {
            return fail(source, "you cannot view record history");
        }
        return execute(
                source,
                "sef:control." + feature.id() + ".manage",
                Map.of(
                        "feature", feature.id(),
                        "record", record.id().toString(),
                        "operation", "history",
                        "page", Integer.toString(requestedPage)),
                () -> page(
                        source,
                        feature.title() + " history",
                        KernelServices.serverControls().history(recordId),
                        requestedPage,
                        entry -> "&e" + entry.occurredAt()
                                + " &8| &f" + entry.before().name().toLowerCase(Locale.ROOT)
                                + " &7to &f" + entry.after().name().toLowerCase(Locale.ROOT)
                                + " &8| &7r" + entry.recordRevision()
                                + " &8| &7" + value(entry.note())),
                permission(feature, "manage"));
    }

    private static int create(
            CommandSourceStack source,
            ServerControlCatalog.FeatureDefinition feature,
            ServerPlayer subject,
            String title,
            String details
    ) {
        if (subject != null && !mayTarget(source, subject, feature)) {
            return 0;
        }
        UUID actor = actorId(source);
        UUID subjectId = subject == null ? null : subject.getUUID();
        return execute(
                source,
                "sef:control." + feature.id() + ".create",
                Map.of(
                        "feature", feature.id(),
                        "subject", subjectId == null ? "" : subjectId.toString(),
                        "title", title),
                () -> {
                    ActionResult<ServerControlRepository.ControlRecord> result =
                            KernelServices.serverControls().create(
                                    feature.id(),
                                    actor,
                                    subjectId,
                                    title,
                                    details,
                                    null,
                                    Map.of("route", "command"));
                    if (!result.successful()) {
                        return fail(source, detail(result));
                    }
                    success(source, feature.title() + " record created, " + result.value().id());
                    return 1;
                },
                permission(feature, "create"));
    }

    private static int transition(
            CommandSourceStack source,
            ServerControlCatalog.FeatureDefinition feature,
            String recordInput,
            String stateInput,
            long expectedRevision,
            String note
    ) {
        UUID recordId = uuid(recordInput);
        if (recordId == null) {
            return fail(source, "record id is invalid");
        }
        ServerControlRepository.RecordState state;
        try {
            state = ServerControlRepository.RecordState.valueOf(stateInput.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return fail(source, "record state is invalid");
        }
        if (!feature.states().contains(state)) {
            return fail(source, "record state is unavailable for this feature");
        }
        ServerControlRepository.ControlRecord current = KernelServices.serverControls()
                .find(recordId)
                .orElse(null);
        if (current == null || !current.featureId().equals(feature.id())) {
            return fail(source, "record not found");
        }
        if (!mayTargetRecord(source, current, feature)) {
            return 0;
        }
        return execute(
                source,
                "sef:control." + feature.id() + ".manage",
                Map.of(
                        "feature", feature.id(),
                        "record", recordId.toString(),
                        "state", state.name().toLowerCase(Locale.ROOT),
                        "revision", Long.toString(expectedRevision)),
                () -> result(
                        source,
                        KernelServices.serverControls().transition(
                                recordId,
                                actorId(source),
                                state,
                                expectedRevision,
                                note),
                        feature.title() + " record transitioned"),
                permission(feature, "manage"));
    }

    private static int update(
            CommandSourceStack source,
            ServerControlCatalog.FeatureDefinition feature,
            String recordInput,
            long expectedRevision,
            String title,
            String details
    ) {
        UUID recordId = uuid(recordInput);
        if (recordId == null) {
            return fail(source, "record id is invalid");
        }
        ServerControlRepository.ControlRecord current = KernelServices.serverControls()
                .find(recordId)
                .orElse(null);
        if (current == null || !current.featureId().equals(feature.id())) {
            return fail(source, "record not found");
        }
        if (!mayTargetRecord(source, current, feature)) {
            return 0;
        }
        return execute(
                source,
                "sef:control." + feature.id() + ".manage",
                Map.of(
                        "feature", feature.id(),
                        "record", recordId.toString(),
                        "revision", Long.toString(expectedRevision)),
                () -> result(
                        source,
                        KernelServices.serverControls().update(
                                recordId,
                                actorId(source),
                                title,
                                details,
                                expectedRevision),
                        feature.title() + " record updated"),
                permission(feature, "manage"));
    }

    public static boolean mayTargetRecord(
            CommandSourceStack source,
            ServerControlRepository.ControlRecord record,
            ServerControlCatalog.FeatureDefinition feature
    ) {
        if (record.subjectId() == null) {
            return true;
        }
        ServerPlayer target = source.getServer().getPlayerList().getPlayer(record.subjectId());
        if (target == null) {
            return true;
        }
        return mayTarget(source, target, feature);
    }

    private static boolean mayTarget(
            CommandSourceStack source,
            ServerPlayer target,
            ServerControlCatalog.FeatureDefinition feature
    ) {
        ServerPlayer actor = source.getPlayer();
        if (actor != null && actor.getUUID().equals(target.getUUID())) {
            return true;
        }
        var decision = PlayerTargetPolicy.decide(
                source,
                target,
                node(permission(feature, "hierarchy.override")),
                node(permission(feature, "exempt")),
                node(permission(feature, "exemption.override")),
                false,
                false);
        if (!decision.allowed()) {
            fail(source, decision.exempt()
                    ? "that player is exempt from this control"
                    : "you cannot target a player at or above your hierarchy");
            return false;
        }
        return true;
    }

    private static int result(CommandSourceStack source, ActionResult<?> result, String message) {
        if (!result.successful()) {
            return fail(source, detail(result));
        }
        success(source, message);
        return 1;
    }

    private static int execute(
            CommandSourceStack source,
            String action,
            Map<String, String> parameters,
            java.util.function.IntSupplier operation,
            String permission
    ) {
        PermissionNode<Boolean> node = node(permission);
        if (node == null) {
            return fail(source, "permission definition is unavailable");
        }
        return KernelCommandExecutor.execute(source, action, parameters, operation, node);
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
        if (values.isEmpty()) {
            info(source, "no authorized records");
            return 1;
        }
        for (int index = start; index < end; index++) {
            info(source, formatter.apply(values.get(index)));
        }
        return 1;
    }

    private static String permission(ServerControlCatalog.FeatureDefinition feature, String action) {
        return "commands.control." + feature.id() + "." + action;
    }

    private static PermissionNode<Boolean> node(String permission) {
        return PermissionsHandler.phasePermission(permission);
    }

    private static boolean has(CommandSourceStack source, String permission) {
        PermissionNode<Boolean> node = node(permission);
        return node != null && PermissionService.has(source, node);
    }

    private static boolean hasAny(CommandSourceStack source, String... permissions) {
        for (String permission : permissions) {
            if (has(source, permission)) {
                return true;
            }
        }
        return false;
    }

    private static UUID actorId(CommandSourceStack source) {
        return source.getPlayer() == null ? CONSOLE_ID : source.getPlayer().getUUID();
    }

    private static UUID uuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static String detail(ActionResult<?> result) {
        return result.detail().isBlank()
                ? result.reason().name().toLowerCase(Locale.ROOT)
                : result.detail();
    }

    private static String value(String value) {
        return value == null || value.isBlank() ? "none" : value;
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

    public static Map<String, String> directRoots() {
        Map<String, String> roots = new LinkedHashMap<>();
        roots.put("maintenance", "maintenance");
        roots.put("policylab", "policy_lab");
        roots.put("drift", "config_drift");
        roots.put("guardrail", "guardrails");
        roots.put("changewindow", "change_windows");
        roots.put("permissionimpact", "permission_impact");
        roots.put("featuregraph", "dependency_graph");
        roots.put("impactpreview", "player_impact");
        roots.put("quarantine", "session_quarantine");
        roots.put("adminjournal", "admin_journal");
        roots.put("governor", "resource_governor");
        roots.put("anomaly", "command_anomaly");
        roots.put("statesnapshot", "operational_snapshots");
        roots.put("incident", "incidents");
        roots.put("rollout", "rollouts");
        roots.put("reports", "reports");
        roots.put("report", "reports");
        roots.put("tickets", "tickets");
        roots.put("ticket", "tickets");
        roots.put("staffnote", "staff_notes");
        roots.put("staffnotes", "staff_notes");
        roots.put("channel", "chat_channels");
        roots.put("channels", "chat_channels");
        roots.put("mentions", "mentions");
        roots.put("friend", "friends");
        roots.put("friends", "friends");
        roots.put("trust", "friends");
        roots.put("untrust", "friends");
        roots.put("block", "interaction_blocks");
        roots.put("unblock", "interaction_blocks");
        roots.put("blocks", "interaction_blocks");
        roots.put("rules", "rules");
        roots.put("onboarding", "onboarding");
        roots.put("playtimerewards", "playtime_rewards");
        roots.put("rewards", "playtime_rewards");
        roots.put("daily", "daily_rewards");
        roots.put("weekly", "weekly_rewards");
        roots.put("sleepvote", "sleep_vote");
        roots.put("deathlocation", "death_compass");
        roots.put("deathcompass", "death_compass");
        roots.put("afkzone", "afk_zones");
        roots.put("grave", "graves");
        roots.put("graves", "graves");
        roots.put("inventoryhistory", "inventory_recovery");
        roots.put("inventoryrestore", "inventory_recovery");
        roots.put("restart", "restart_coordinator");
        roots.put("resourceworld", "resource_worlds");
        roots.put("pregen", "chunk_pregen");
        roots.put("cleanup", "cleanup");
        roots.put("performance", "performance");
        roots.put("calendar", "server_calendar");
        roots.put("waypoint", "waypoints");
        roots.put("waypoints", "waypoints");
        roots.put("portal", "portal_policy");
        roots.put("shortcut", "alias_diagnostics");
        roots.put("staffduty", "staff_duty");
        roots.put("staffshift", "staff_duty");
        roots.put("approval", "approvals");
        roots.put("appeals", "appeals");
        roots.put("appeal", "appeals");
        roots.put("discipline", "discipline");
        roots.put("automod", "automod");
        roots.put("chatcontrol", "chat_control");
        roots.put("admission", "admission");
        roots.put("queue", "queue");
        roots.put("access", "access_applications");
        roots.put("accessgrant", "capability_leases");
        roots.put("adminlock", "admin_lock");
        roots.put("applications", "access_applications");
        roots.put("invites", "invites");
        roots.put("resourcepack", "resource_packs");
        roots.put("serverpresentation", "server_presentation");
        roots.put("worldpolicy", "world_policy");
        roots.put("borderprofile", "world_border");
        roots.put("chunktickets", "chunk_tickets");
        roots.put("activityprofile", "block_activity");
        roots.put("spawnpolicy", "spawn_ecology");
        roots.put("datapacks", "datapacks");
        roots.put("modhealth", "mod_health");
        roots.put("backup", "backups");
        roots.put("mydata", "privacy");
        roots.put("privacycenter", "privacy");
        roots.put("privacyrequests", "privacy");
        roots.put("privacy", "privacy");
        roots.put("evidence", "evidence");
        roots.put("parcel", "parcels");
        roots.put("parcels", "parcels");
        roots.put("parceladmin", "parcels");
        roots.put("lostfound", "lost_found");
        roots.put("lostfoundadmin", "lost_found");
        roots.put("trade", "trades");
        roots.put("trades", "trades");
        roots.put("tradeadmin", "trades");
        roots.put("auction", "auctions");
        roots.put("auctions", "auctions");
        roots.put("auctionadmin", "auctions");
        roots.put("poll", "polls");
        roots.put("polls", "polls");
        roots.put("polladmin", "polls");
        roots.put("event", "community_events");
        roots.put("events", "community_events");
        roots.put("eventadmin", "community_events");
        roots.put("guide", "knowledge");
        roots.put("guideadmin", "knowledge");
        roots.put("knowledge", "knowledge");
        roots.put("displayprofile", "display_profiles");
        roots.put("sidebar", "display_profiles");
        roots.put("bossbars", "display_profiles");
        return Map.copyOf(roots);
    }
}
