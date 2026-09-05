package com.enviouse.sef.gui;

import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.kernel.command.PanelContracts;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AdminPanelCommands {
    private static final int PAGE_SIZE = 10;

    private AdminPanelCommands() {
    }

    public static void attach(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("panel")
                .then(Commands.literal("list")
                        .requires(source -> KernelCommandExecutor.canUse(source, "sef:panel.list"))
                        .executes(context -> execute(
                                context.getSource(),
                                "sef:panel.list",
                                Map.of("page", "1"),
                                () -> list(context.getSource(), 1)))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    int page = IntegerArgumentType.getInteger(context, "page");
                                    return execute(
                                            context.getSource(),
                                            "sef:panel.list",
                                            Map.of("page", Integer.toString(page)),
                                            () -> list(context.getSource(), page));
                                })))
                .then(Commands.literal("inspect")
                        .requires(source -> KernelCommandExecutor.canUse(source, "sef:panel.inspect"))
                        .then(Commands.argument("panel", StringArgumentType.word())
                                .suggests((context, builder) -> suggestPanels(builder))
                                .executes(context -> {
                                    String panel = StringArgumentType.getString(context, "panel");
                                    return execute(
                                            context.getSource(),
                                            "sef:panel.inspect",
                                            Map.of("panel", panel),
                                            () -> inspect(context.getSource(), panel));
                                })))
                .then(Commands.literal("preview")
                        .requires(source -> KernelCommandExecutor.canUse(source, "sef:panel.preview"))
                        .then(Commands.argument("panel", StringArgumentType.word())
                                .suggests((context, builder) -> suggestPanelsAndDrafts(builder))
                                .executes(context -> {
                                    String panel = StringArgumentType.getString(context, "panel");
                                    return execute(
                                            context.getSource(),
                                            "sef:panel.preview",
                                            Map.of("panel", panel),
                                            () -> preview(context.getSource(), panel));
                                })))
                .then(Commands.literal("run")
                        .requires(source -> KernelCommandExecutor.canUse(source, "sef:panel.run"))
                        .then(Commands.argument("panel", StringArgumentType.word())
                                .suggests((context, builder) -> suggestPanels(builder))
                                .then(Commands.argument("control", StringArgumentType.word())
                                        .executes(context -> {
                                            String panel = StringArgumentType.getString(context, "panel");
                                            String control = StringArgumentType.getString(context, "control");
                                            return execute(
                                                    context.getSource(),
                                                    "sef:panel.run",
                                                    Map.of("panel", panel, "control", control),
                                                    () -> run(context.getSource(), panel, control));
                                        }))))
                .then(Commands.literal("draft")
                        .then(Commands.literal("create")
                                .requires(source -> KernelCommandExecutor.canUse(source, "sef:panel.draft.create"))
                                .then(Commands.argument("panel", StringArgumentType.word())
                                        .then(Commands.argument("title", StringArgumentType.greedyString())
                                                .executes(context -> {
                                                    String panel = StringArgumentType.getString(context, "panel");
                                                    String title = StringArgumentType.getString(context, "title");
                                                    return execute(
                                                            context.getSource(),
                                                            "sef:panel.draft.create",
                                                            Map.of("panel", panel),
                                                            () -> create(
                                                                    context.getSource(),
                                                                    panel,
                                                                    title));
                                                }))))
                        .then(Commands.literal("control")
                                .then(Commands.literal("add")
                                        .requires(source -> KernelCommandExecutor.canUse(
                                                source,
                                                "sef:panel.draft.control_add"))
                                        .then(Commands.argument("panel", StringArgumentType.word())
                                                .suggests((context, builder) -> suggestDrafts(builder))
                                                .then(Commands.argument("revision", LongArgumentType.longArg(1))
                                                        .then(Commands.argument("control", StringArgumentType.word())
                                                                .then(Commands.argument(
                                                                                "slot",
                                                                                IntegerArgumentType.integer(0, 53))
                                                                        .then(Commands.argument(
                                                                                        "action",
                                                                                        StringArgumentType.word())
                                                                                .suggests((context, builder) ->
                                                                                        suggestActions(builder))
                                                                                .executes(context -> {
                                                                                    String panel =
                                                                                            StringArgumentType.getString(
                                                                                                    context,
                                                                                                    "panel");
                                                                                    long revision =
                                                                                            LongArgumentType.getLong(
                                                                                                    context,
                                                                                                    "revision");
                                                                                    String control =
                                                                                            StringArgumentType.getString(
                                                                                                    context,
                                                                                                    "control");
                                                                                    int slot =
                                                                                            IntegerArgumentType.getInteger(
                                                                                                    context,
                                                                                                    "slot");
                                                                                    String action =
                                                                                            StringArgumentType.getString(
                                                                                                    context,
                                                                                                    "action");
                                                                                    return execute(
                                                                                            context.getSource(),
                                                                                            "sef:panel.draft.control_add",
                                                                                            Map.of(
                                                                                                    "panel",
                                                                                                    panel,
                                                                                                    "control",
                                                                                                    control,
                                                                                                    "action",
                                                                                                    action),
                                                                                            () -> addControl(
                                                                                                    context.getSource(),
                                                                                                    panel,
                                                                                                    revision,
                                                                                                    control,
                                                                                                    slot,
                                                                                                    action));
                                                                                })))))))
                                .then(Commands.literal("remove")
                                        .requires(source -> KernelCommandExecutor.canUse(
                                                source,
                                                "sef:panel.draft.control_remove"))
                                        .then(Commands.argument("panel", StringArgumentType.word())
                                                .suggests((context, builder) -> suggestDrafts(builder))
                                                .then(Commands.argument("revision", LongArgumentType.longArg(1))
                                                        .then(Commands.argument("control", StringArgumentType.word())
                                                                .executes(context -> {
                                                                    String panel = StringArgumentType.getString(
                                                                            context,
                                                                            "panel");
                                                                    long revision = LongArgumentType.getLong(
                                                                            context,
                                                                            "revision");
                                                                    String control = StringArgumentType.getString(
                                                                            context,
                                                                            "control");
                                                                    return execute(
                                                                            context.getSource(),
                                                                            "sef:panel.draft.control_remove",
                                                                            Map.of(
                                                                                    "panel",
                                                                                    panel,
                                                                                    "control",
                                                                                    control),
                                                                            () -> removeControl(
                                                                                    context.getSource(),
                                                                                    panel,
                                                                                    revision,
                                                                                    control));
                                                                }))))))
                        .then(Commands.literal("delete")
                                .requires(source -> KernelCommandExecutor.canUse(source, "sef:panel.draft.delete"))
                                .then(Commands.argument("panel", StringArgumentType.word())
                                        .suggests((context, builder) -> suggestDrafts(builder))
                                        .then(Commands.argument("revision", LongArgumentType.longArg(1))
                                                .executes(context -> {
                                                    String panel = StringArgumentType.getString(context, "panel");
                                                    long revision = LongArgumentType.getLong(context, "revision");
                                                    return execute(
                                                            context.getSource(),
                                                            "sef:panel.draft.delete",
                                                            Map.of("panel", panel),
                                                            () -> deleteDraft(
                                                                    context.getSource(),
                                                                    panel,
                                                                    revision));
                                                })))))
                .then(Commands.literal("publish")
                        .requires(source -> KernelCommandExecutor.canUse(source, "sef:panel.publish"))
                        .then(Commands.argument("panel", StringArgumentType.word())
                                .suggests((context, builder) -> suggestDrafts(builder))
                                .then(Commands.argument("revision", LongArgumentType.longArg(1))
                                        .executes(context -> {
                                            String panel = StringArgumentType.getString(context, "panel");
                                            long revision = LongArgumentType.getLong(context, "revision");
                                            return execute(
                                                    context.getSource(),
                                                    "sef:panel.publish",
                                                    Map.of("panel", panel),
                                                    () -> publish(
                                                            context.getSource(),
                                                            panel,
                                                            revision));
                                        }))))
                .then(Commands.literal("rollback")
                        .requires(source -> KernelCommandExecutor.canUse(source, "sef:panel.rollback"))
                        .then(Commands.argument("panel", StringArgumentType.word())
                                .suggests((context, builder) -> suggestPanels(builder))
                                .then(Commands.argument("revision", LongArgumentType.longArg(1))
                                        .then(Commands.argument(
                                                        "history_revision",
                                                        LongArgumentType.longArg(1))
                                                .executes(context -> {
                                                    String panel = StringArgumentType.getString(context, "panel");
                                                    long revision = LongArgumentType.getLong(context, "revision");
                                                    long historyRevision = LongArgumentType.getLong(
                                                            context,
                                                            "history_revision");
                                                    return execute(
                                                            context.getSource(),
                                                            "sef:panel.rollback",
                                                            Map.of("panel", panel),
                                                            () -> rollback(
                                                                    context.getSource(),
                                                                    panel,
                                                                    revision,
                                                                    historyRevision));
                                                }))))));
    }

    private static int list(CommandSourceStack source, int requestedPage) {
        List<AdminPanelService.PanelDefinition> panels = KernelServices.adminPanels().panels().stream()
                .filter(panel -> has(source, panel.permissionId()))
                .toList();
        int pages = Math.max(1, (panels.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int page = Math.min(requestedPage, pages);
        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(panels.size(), start + PAGE_SIZE);
        source.sendSuccess(() -> Component.literal(
                "SEF panels, page " + page + " of " + pages + "."), false);
        for (int index = start; index < end; index++) {
            AdminPanelService.PanelDefinition panel = panels.get(index);
            source.sendSuccess(() -> Component.literal(
                    panel.id() + ", revision " + panel.revision() + ", "
                            + panel.controls().size() + " controls, " + panel.state().name().toLowerCase()), false);
        }
        return 1;
    }

    private static int inspect(CommandSourceStack source, String panelId) {
        AdminPanelService.PanelDefinition panel = KernelServices.adminPanels().panel(panelId).orElse(null);
        if (panel == null || !has(source, panel.permissionId())) {
            source.sendFailure(Component.literal("That panel is unavailable."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal(
                panel.title() + ", " + panel.id() + ", revision " + panel.revision() + "."), false);
        for (AdminPanelService.Control control : panel.controls()) {
            source.sendSuccess(() -> Component.literal(
                    control.id() + ", " + control.actionId() + ", "
                            + control.executionContext().name().toLowerCase() + ", "
                            + control.audienceKind().name().toLowerCase() + ", maximum "
                            + control.maximumTargets() + "."), false);
        }
        return 1;
    }

    private static int preview(CommandSourceStack source, String panelId) {
        AdminPanelService.Preview preview = KernelServices.adminPanels().preview(panelId);
        if (preview.revision() < 1L) {
            source.sendFailure(Component.literal("That panel is unavailable."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal(
                "Panel preview, " + preview.panelId() + ", revision " + preview.revision() + "."), false);
        for (AdminPanelService.PreviewControl control : preview.controls()) {
            source.sendSuccess(() -> Component.literal(
                    control.controlId() + ", " + control.actionId() + ", fallback /"
                            + control.commandFallback() + ", " + control.executionContext().name().toLowerCase()
                            + ", maximum " + control.maximumTargets() + "."), false);
        }
        preview.problems().forEach(problem ->
                source.sendFailure(Component.literal("Validation, " + problem + ".")));
        return preview.problems().isEmpty() ? 1 : 0;
    }

    private static int run(CommandSourceStack source, String panelId, String controlId) {
        AdminPanelService.Execution execution =
                KernelServices.adminPanels().execution(panelId, controlId).orElse(null);
        if (execution == null
                || !has(source, execution.panel().permissionId())
                || !execution.control().contextPermissionId().isBlank()
                && !has(source, execution.control().contextPermissionId())
                || !KernelCommandExecutor.canUse(source, execution.action().id())) {
            source.sendFailure(Component.literal("That panel control is unavailable."));
            return 0;
        }
        if (execution.control().executionContext() != PanelContracts.ExecutionContext.ACTOR
                && execution.control().executionContext() != PanelContracts.ExecutionContext.TARGET_ACTOR) {
            source.sendFailure(Component.literal(
                    "This control requires its typed server or bulk execution route."));
            return 0;
        }
        StringBuilder command = new StringBuilder(execution.action().canonicalRoute());
        execution.control().fixedArguments().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> command.append(' ').append(entry.getValue()));
        try {
            return source.getServer().getCommands().getDispatcher().execute(command.toString(), source);
        } catch (CommandSyntaxException exception) {
            source.sendFailure(Component.literal("That panel control could not be completed safely."));
            return 0;
        }
    }

    private static int create(CommandSourceStack source, String panelId, String title) {
        ActionResult<AdminPanelService.PanelDefinition> result =
                KernelServices.adminPanels().createDraft(panelId, title, actorId(source));
        return report(source, result, "Created panel draft ");
    }

    private static int addControl(
            CommandSourceStack source,
            String panelId,
            long revision,
            String controlId,
            int slot,
            String actionId
    ) {
        CommandDefinition action = KernelServices.catalog().find(actionId).orElse(null);
        if (action == null) {
            source.sendFailure(Component.literal("That action is not cataloged."));
            return 0;
        }
        String permission = action.permissionIds().stream().sorted().findFirst().orElseThrow();
        PanelContracts.TargetPolicy targetPolicy = targetPolicy(action.targetBehavior());
        AdminPanelService.Control control = new AdminPanelService.Control(
                controlId,
                slot,
                1,
                action.id(),
                permission,
                targetPolicy,
                executionContext(targetPolicy),
                contextPermission(executionContext(targetPolicy)),
                audienceKind(targetPolicy),
                targetPolicy == PanelContracts.TargetPolicy.BOUNDED_AUDIENCE ? 64 : 1,
                Map.of(),
                action.accessClass().isPrivileged());
        ActionResult<AdminPanelService.PanelDefinition> result =
                KernelServices.adminPanels().addControl(
                        panelId,
                        revision,
                        control,
                        actorId(source));
        return report(source, result, "Updated panel draft ");
    }

    private static int removeControl(
            CommandSourceStack source,
            String panelId,
            long revision,
            String controlId
    ) {
        return report(
                source,
                KernelServices.adminPanels().removeControl(
                        panelId,
                        revision,
                        controlId,
                        actorId(source)),
                "Updated panel draft ");
    }

    private static int deleteDraft(CommandSourceStack source, String panelId, long revision) {
        ActionResult<Void> result = KernelServices.adminPanels().deleteDraft(panelId, revision);
        if (!result.successful()) {
            source.sendFailure(Component.literal(result.detail()));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Deleted panel draft " + panelId + "."), false);
        return 1;
    }

    private static int publish(CommandSourceStack source, String panelId, long revision) {
        return report(
                source,
                KernelServices.adminPanels().publish(panelId, revision, actorId(source)),
                "Published panel ");
    }

    private static int rollback(
            CommandSourceStack source,
            String panelId,
            long revision,
            long historyRevision
    ) {
        return report(
                source,
                KernelServices.adminPanels().rollback(
                        panelId,
                        revision,
                        historyRevision,
                        actorId(source)),
                "Rolled back panel ");
    }

    private static int execute(
            CommandSourceStack source,
            String actionId,
            Map<String, String> parameters,
            java.util.function.IntSupplier action
    ) {
        return KernelCommandExecutor.execute(source, actionId, parameters, action);
    }

    private static int report(
            CommandSourceStack source,
            ActionResult<AdminPanelService.PanelDefinition> result,
            String prefix
    ) {
        if (!result.successful()) {
            source.sendFailure(Component.literal(result.detail()));
            return 0;
        }
        source.sendSuccess(() -> Component.literal(
                prefix + result.value().id() + ", revision " + result.value().revision() + "."), false);
        return 1;
    }

    private static boolean has(CommandSourceStack source, String permissionId) {
        var node = KernelServices.permissionNode(permissionId);
        return node != null && com.enviouse.sef.permissions.PermissionService.has(source, node);
    }

    private static UUID actorId(CommandSourceStack source) {
        return source.getEntity() instanceof ServerPlayer player ? player.getUUID() : new UUID(0L, 0L);
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestPanels(
            com.mojang.brigadier.suggestion.SuggestionsBuilder builder
    ) {
        KernelServices.adminPanels().panels().forEach(panel -> builder.suggest(panel.id()));
        return builder.buildFuture();
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestDrafts(
            com.mojang.brigadier.suggestion.SuggestionsBuilder builder
    ) {
        KernelServices.adminPanels().drafts().forEach(panel -> builder.suggest(panel.id()));
        return builder.buildFuture();
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions>
    suggestPanelsAndDrafts(
            com.mojang.brigadier.suggestion.SuggestionsBuilder builder
    ) {
        KernelServices.adminPanels().panels().forEach(panel -> builder.suggest(panel.id()));
        KernelServices.adminPanels().drafts().forEach(panel -> builder.suggest(panel.id()));
        return builder.buildFuture();
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestActions(
            com.mojang.brigadier.suggestion.SuggestionsBuilder builder
    ) {
        KernelServices.catalog().entries().stream()
                .sorted(Comparator.comparing(CommandDefinition::id))
                .forEach(action -> builder.suggest(action.id()));
        return builder.buildFuture();
    }

    private static PanelContracts.TargetPolicy targetPolicy(CommandDefinition.TargetBehavior behavior) {
        return switch (behavior) {
            case NONE -> PanelContracts.TargetPolicy.NONE;
            case SELF -> PanelContracts.TargetPolicy.SELF;
            case OPTIONAL_PLAYER, REQUIRED_PLAYER -> PanelContracts.TargetPolicy.SELECTED_VISIBLE_PLAYER;
            case BOUNDED_PLAYERS -> PanelContracts.TargetPolicy.BOUNDED_AUDIENCE;
            case SERVER -> PanelContracts.TargetPolicy.SERVER;
        };
    }

    private static PanelContracts.ExecutionContext executionContext(PanelContracts.TargetPolicy targetPolicy) {
        return switch (targetPolicy) {
            case NONE, SELF -> PanelContracts.ExecutionContext.ACTOR;
            case EXPLICIT_VISIBLE_PLAYER, SELECTED_VISIBLE_PLAYER -> PanelContracts.ExecutionContext.TARGET_ACTOR;
            case BOUNDED_AUDIENCE -> PanelContracts.ExecutionContext.NATIVE_BULK;
            case SERVER -> PanelContracts.ExecutionContext.SERVER_PROFILE;
        };
    }

    private static PanelContracts.AudienceKind audienceKind(PanelContracts.TargetPolicy targetPolicy) {
        return switch (targetPolicy) {
            case NONE, SELF -> PanelContracts.AudienceKind.SELF;
            case EXPLICIT_VISIBLE_PLAYER -> PanelContracts.AudienceKind.ONE_VISIBLE_PLAYER;
            case SELECTED_VISIBLE_PLAYER -> PanelContracts.AudienceKind.SELECTED_VISIBLE_PLAYERS;
            case BOUNDED_AUDIENCE -> PanelContracts.AudienceKind.BOUNDED_AUDIENCE;
            case SERVER -> PanelContracts.AudienceKind.SERVER;
        };
    }

    private static String contextPermission(PanelContracts.ExecutionContext executionContext) {
        return switch (executionContext) {
            case ACTOR, TARGET_ACTOR -> "";
            case SERVER_PROFILE -> "sef.kernel.panel.context.server";
            case NATIVE_BULK -> "sef.kernel.panel.context.bulk";
            case AS_EACH_PARTICIPANT -> "sef.kernel.panel.context.participants";
        };
    }
}
