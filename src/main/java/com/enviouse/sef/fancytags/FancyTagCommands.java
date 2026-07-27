package com.enviouse.sef.fancytags;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.audit.SecurityAuditService;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.gui.protocol.SefPayloads;
import com.enviouse.sef.gui.protocol.SefProtocol;
import com.enviouse.sef.gui.protocol.SefSessionManager;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.util.DurationParser;
import com.enviouse.sef.vanish.VanishUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.IntSupplier;

public final class FancyTagCommands {
    private FancyTagCommands() {
    }

    public static void attach(LiteralArgumentBuilder<CommandSourceStack> sefRoot) {
        sefRoot.then(root("tags"));
    }

    public static void registerDirect(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (!ConfigHandler.config.enableFancyTags.get()) {
            return;
        }
        dispatcher.register(root("fancytags"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> root(String literal) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(literal)
                .requires(source -> has(source, "commands.tags"))
                .executes(context -> run(context.getSource(), "status",
                        () -> openManagerOrStatus(context.getSource(), "manager")));
        root.then(Commands.literal("local")
                .requires(source -> has(source, "tags.local.overlay"))
                .executes(context -> run(context.getSource(), "local",
                        () -> openManagerOrStatus(context.getSource(), "local"))));
        root.then(Commands.literal("status")
                .requires(source -> has(source, "commands.tags.status"))
                .executes(context -> run(context.getSource(), "status",
                        () -> status(context.getSource()))));
        root.then(Commands.literal("list")
                .requires(source -> has(source, "commands.tags.list"))
                .executes(context -> run(context.getSource(), "list",
                        () -> list(context.getSource()))));
        root.then(Commands.literal("view")
                .requires(source -> has(source, "commands.tags.view"))
                .then(Commands.argument("tag", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            KernelServices.fancyTags().tags().forEach(tag -> builder.suggest(tag.resourceKey()));
                            return builder.buildFuture();
                        })
                        .executes(context -> run(context.getSource(), "view",
                                () -> view(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "tag"))))));
        root.then(Commands.literal("create")
                .requires(source -> has(source, "commands.tags.create"))
                .then(Commands.argument("resource_key", StringArgumentType.word())
                        .executes(context -> run(context.getSource(), "create",
                                () -> create(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "resource_key"),
                                        StringArgumentType.getString(context, "resource_key"))))
                        .then(Commands.argument("display_name", StringArgumentType.greedyString())
                                .executes(context -> run(context.getSource(), "create",
                                        () -> create(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "resource_key"),
                                                StringArgumentType.getString(context, "display_name")))))));
        root.then(Commands.literal("duplicate")
                .requires(source -> has(source, "commands.tags.duplicate"))
                .then(Commands.argument("source_tag", StringArgumentType.word())
                        .then(Commands.argument("resource_key", StringArgumentType.word())
                                .executes(context -> run(context.getSource(), "duplicate",
                                        () -> duplicate(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "source_tag"),
                                                StringArgumentType.getString(context, "resource_key"),
                                                StringArgumentType.getString(context, "resource_key"))))
                                .then(Commands.argument("display_name", StringArgumentType.greedyString())
                                        .executes(context -> run(context.getSource(), "duplicate",
                                                () -> duplicate(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "source_tag"),
                                                        StringArgumentType.getString(context, "resource_key"),
                                                        StringArgumentType.getString(context, "display_name"))))))));
        root.then(editNode());
        root.then(Commands.literal("validate")
                .requires(source -> has(source, "commands.tags.validate"))
                .then(Commands.argument("tag", StringArgumentType.word())
                        .executes(context -> run(context.getSource(), "validate",
                                () -> validate(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "tag"))))));
        root.then(statusNode("publish", FancyTagService.TagStatus.PUBLISHED));
        root.then(statusNode("hide", FancyTagService.TagStatus.HIDDEN));
        root.then(statusNode("archive", FancyTagService.TagStatus.ARCHIVED));
        root.then(statusNode("restore", FancyTagService.TagStatus.DRAFT));
        root.then(statusNode("delete", FancyTagService.TagStatus.PENDING_DELETE));
        root.then(Commands.literal("purge")
                .requires(source -> has(source, "commands.tags.delete.finalize"))
                .then(Commands.argument("tag", StringArgumentType.word())
                        .executes(context -> run(context.getSource(), "delete.finalize",
                                () -> purge(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "tag"))))));
        root.then(revisionNode());
        root.then(exportNode());
        root.then(assignNode());
        root.then(unassignNode());
        root.then(assignmentsNode());
        root.then(reportNode());
        root.then(moderationNode());
        root.then(categoryNode());
        root.then(paletteNode());
        root.then(templateNode());
        root.then(importNode());
        root.then(leaseNode());
        root.then(integrityNode());
        root.then(cacheNode());
        root.then(transferNode());
        root.then(auditNode());
        root.then(backupNode());
        root.then(garbageCollectionNode());
        root.then(Commands.literal("doctor")
                .requires(source -> has(source, "commands.tags.doctor"))
                .executes(context -> run(context.getSource(), "doctor",
                        () -> doctor(context.getSource()))));
        root.then(Commands.literal("reload")
                .requires(source -> has(source, "commands.tags.reload"))
                .executes(context -> run(context.getSource(), "reload",
                        () -> reload(context.getSource()))));
        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> editNode() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("edit")
                .requires(source -> has(source, "commands.tags.edit"));
        root.then(Commands.argument("tag", StringArgumentType.word())
                .then(Commands.literal("name")
                        .then(Commands.argument("value", StringArgumentType.greedyString())
                                .executes(context -> run(context.getSource(), "edit",
                                        () -> edit(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "tag"),
                                                "name",
                                                StringArgumentType.getString(context, "value"))))))
                .then(Commands.literal("description")
                        .then(Commands.argument("value", StringArgumentType.greedyString())
                                .executes(context -> run(context.getSource(), "edit",
                                        () -> edit(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "tag"),
                                                "description",
                                                StringArgumentType.getString(context, "value"))))))
                .then(Commands.literal("alternative")
                        .then(Commands.argument("value", StringArgumentType.greedyString())
                                .executes(context -> run(context.getSource(), "edit",
                                        () -> edit(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "tag"),
                                                "alternative",
                                                StringArgumentType.getString(context, "value"))))))
                .then(Commands.literal("permission")
                        .then(Commands.argument("value", StringArgumentType.word())
                                .executes(context -> run(context.getSource(), "edit",
                                        () -> edit(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "tag"),
                                                "permission",
                                                StringArgumentType.getString(context, "value"))))))
                .then(Commands.literal("category")
                        .then(Commands.argument("value", StringArgumentType.word())
                                .executes(context -> run(context.getSource(), "edit",
                                        () -> edit(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "tag"),
                                                "category",
                                                StringArgumentType.getString(context, "value"))))))
                .then(Commands.literal("contexts")
                        .then(Commands.argument("csv", StringArgumentType.word())
                                .executes(context -> run(context.getSource(), "edit",
                                        () -> edit(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "tag"),
                                                "contexts",
                                                StringArgumentType.getString(context, "csv")))))));
        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> statusNode(
            String literal,
            FancyTagService.TagStatus status
    ) {
        return Commands.literal(literal)
                .requires(source -> has(source, "commands.tags." + literal))
                .then(Commands.argument("tag", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            KernelServices.fancyTags().tags().forEach(tag -> builder.suggest(tag.resourceKey()));
                            return builder.buildFuture();
                        })
                        .executes(context -> run(context.getSource(), literal,
                                () -> changeStatus(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "tag"),
                                        status))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> revisionNode() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("revision");
        root.then(Commands.literal("list")
                .requires(source -> has(source, "commands.tags.revision.list"))
                .then(Commands.argument("tag", StringArgumentType.word())
                        .executes(context -> run(context.getSource(), "revision.list",
                                () -> listRevisions(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "tag"),
                                        1)))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> run(context.getSource(), "revision.list",
                                        () -> listRevisions(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "tag"),
                                                IntegerArgumentType.getInteger(context, "page")))))));
        root.then(Commands.literal("view")
                .requires(source -> has(source, "commands.tags.revision.view"))
                .then(Commands.argument("tag", StringArgumentType.word())
                        .then(Commands.argument("revision", LongArgumentType.longArg(1L))
                                .executes(context -> run(context.getSource(), "revision.view",
                                        () -> viewRevision(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "tag"),
                                                LongArgumentType.getLong(context, "revision")))))));
        root.then(Commands.literal("restore")
                .requires(source -> has(source, "commands.tags.revision.restore"))
                .then(Commands.argument("tag", StringArgumentType.word())
                        .then(Commands.argument("revision", LongArgumentType.longArg(1L))
                                .executes(context -> run(context.getSource(), "revision.restore",
                                        () -> restoreRevision(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "tag"),
                                                LongArgumentType.getLong(context, "revision")))))));
        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> exportNode() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("export");
        for (String format : List.of("png", "project", "manifest")) {
            root.then(Commands.literal(format)
                    .requires(source -> has(source, "commands.tags.export." + format))
                    .then(Commands.argument("tag", StringArgumentType.word())
                            .executes(context -> run(context.getSource(), "export." + format,
                                    () -> export(
                                            context.getSource(),
                                            StringArgumentType.getString(context, "tag"),
                                            0L,
                                            format)))
                            .then(Commands.argument("revision", LongArgumentType.longArg(1L))
                                    .executes(context -> run(context.getSource(), "export." + format,
                                            () -> export(
                                                    context.getSource(),
                                                    StringArgumentType.getString(context, "tag"),
                                                    LongArgumentType.getLong(context, "revision"),
                                                    format))))));
        }
        var tag = Commands.argument("tag", StringArgumentType.word());
        for (String format : List.of("png", "project", "manifest")) {
            tag.then(Commands.literal(format)
                    .requires(source -> has(source, "commands.tags.export." + format))
                    .executes(context -> run(context.getSource(), "export." + format,
                            () -> export(
                                    context.getSource(),
                                    StringArgumentType.getString(context, "tag"),
                                    0L,
                                    format)))
                    .then(Commands.argument("revision", LongArgumentType.longArg(1L))
                            .executes(context -> run(context.getSource(), "export." + format,
                                    () -> export(
                                            context.getSource(),
                                            StringArgumentType.getString(context, "tag"),
                                            LongArgumentType.getLong(context, "revision"),
                                            format)))));
        }
        root.then(tag);
        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> assignNode() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("assign");
        var playerDuration = Commands.argument("duration", StringArgumentType.word())
                .executes(context -> run(
                        context.getSource(),
                        "assign.player",
                        () -> assignDuration(
                                context.getSource(),
                                FancyTagService.TargetType.PLAYER,
                                targetId(context, "player"),
                                StringArgumentType.getString(context, "tag"),
                                StringArgumentType.getString(context, "slot"),
                                IntegerArgumentType.getInteger(context, "priority"),
                                StringArgumentType.getString(context, "duration"))));
        var playerPriority = Commands.argument("priority", IntegerArgumentType.integer(-10_000, 10_000))
                .executes(context -> run(context.getSource(), "assign.player",
                        () -> assign(
                                context.getSource(),
                                FancyTagService.TargetType.PLAYER,
                                targetId(context, "player"),
                                StringArgumentType.getString(context, "tag"),
                                StringArgumentType.getString(context, "slot"),
                                IntegerArgumentType.getInteger(context, "priority"),
                                null)))
                .then(playerDuration);
        var playerSlot = Commands.argument("slot", StringArgumentType.word())
                .executes(context -> run(context.getSource(), "assign.player",
                        () -> assign(
                                context.getSource(),
                                FancyTagService.TargetType.PLAYER,
                                targetId(context, "player"),
                                StringArgumentType.getString(context, "tag"),
                                StringArgumentType.getString(context, "slot"),
                                0,
                                null)))
                .then(playerPriority);
        root.then(Commands.literal("player")
                .requires(source -> has(source, "commands.tags.assign.player"))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("tag", StringArgumentType.word())
                                .then(playerSlot))));
        root.then(textTargetAssignment("group", FancyTagService.TargetType.GROUP));
        root.then(textTargetAssignment("team", FancyTagService.TargetType.TEAM));
        var defaultDuration = Commands.argument("duration", StringArgumentType.word())
                .executes(context -> run(
                        context.getSource(),
                        "assign.default",
                        () -> assignDuration(
                                context.getSource(),
                                FancyTagService.TargetType.DEFAULT,
                                "default",
                                StringArgumentType.getString(context, "tag"),
                                StringArgumentType.getString(context, "slot"),
                                IntegerArgumentType.getInteger(context, "priority"),
                                StringArgumentType.getString(context, "duration"))));
        var defaultPriority = Commands.argument("priority", IntegerArgumentType.integer(-10_000, 10_000))
                .executes(context -> run(context.getSource(), "assign.default",
                        () -> assign(
                                context.getSource(),
                                FancyTagService.TargetType.DEFAULT,
                                "default",
                                StringArgumentType.getString(context, "tag"),
                                StringArgumentType.getString(context, "slot"),
                                IntegerArgumentType.getInteger(context, "priority"),
                                null)))
                .then(defaultDuration);
        var defaultSlot = Commands.argument("slot", StringArgumentType.word())
                .executes(context -> run(context.getSource(), "assign.default",
                        () -> assign(
                                context.getSource(),
                                FancyTagService.TargetType.DEFAULT,
                                "default",
                                StringArgumentType.getString(context, "tag"),
                                StringArgumentType.getString(context, "slot"),
                                0,
                                null)))
                .then(defaultPriority);
        root.then(Commands.literal("default")
                .requires(source -> has(source, "commands.tags.assign.default"))
                .then(Commands.argument("tag", StringArgumentType.word())
                        .then(defaultSlot)));
        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> textTargetAssignment(
            String literal,
            FancyTagService.TargetType type
    ) {
        var duration = Commands.argument("duration", StringArgumentType.word())
                .executes(context -> run(
                        context.getSource(),
                        "assign." + literal,
                        () -> assignDuration(
                                context.getSource(),
                                type,
                                StringArgumentType.getString(context, literal),
                                StringArgumentType.getString(context, "tag"),
                                StringArgumentType.getString(context, "slot"),
                                IntegerArgumentType.getInteger(context, "priority"),
                                StringArgumentType.getString(context, "duration"))));
        var priority = Commands.argument("priority", IntegerArgumentType.integer(-10_000, 10_000))
                .executes(context -> run(
                        context.getSource(),
                        "assign." + literal,
                        () -> assign(
                                context.getSource(),
                                type,
                                StringArgumentType.getString(context, literal),
                                StringArgumentType.getString(context, "tag"),
                                StringArgumentType.getString(context, "slot"),
                                IntegerArgumentType.getInteger(context, "priority"),
                                null)))
                .then(duration);
        var slot = Commands.argument("slot", StringArgumentType.word())
                .executes(context -> run(context.getSource(), "assign." + literal,
                        () -> assign(
                                context.getSource(),
                                type,
                                StringArgumentType.getString(context, literal),
                                StringArgumentType.getString(context, "tag"),
                                StringArgumentType.getString(context, "slot"),
                                0,
                                null)))
                .then(priority);
        return Commands.literal(literal)
                .requires(source -> has(source, "commands.tags.assign." + literal))
                .then(Commands.argument(literal, StringArgumentType.word())
                        .then(Commands.argument("tag", StringArgumentType.word())
                                .then(slot)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> unassignNode() {
        return Commands.literal("unassign")
                .requires(source -> has(source, "commands.tags.unassign"))
                .then(Commands.argument("assignment_id", StringArgumentType.word())
                        .executes(context -> run(context.getSource(), "unassign",
                                () -> unassign(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "assignment_id")))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> assignmentsNode() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("assignments");
        root.then(Commands.literal("player")
                .requires(source -> has(source, "commands.tags.assignments.player"))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> run(context.getSource(), "assignments.player",
                                () -> assignments(
                                        context.getSource(),
                                        FancyTagService.TargetType.PLAYER,
                                        targetId(context, "player"),
                                        1)))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> run(context.getSource(), "assignments.player",
                                        () -> assignments(
                                                context.getSource(),
                                                FancyTagService.TargetType.PLAYER,
                                                targetId(context, "player"),
                                                IntegerArgumentType.getInteger(context, "page")))))));
        root.then(Commands.literal("tag")
                .requires(source -> has(source, "commands.tags.assignments.tag"))
                .then(Commands.argument("tag", StringArgumentType.word())
                        .executes(context -> run(context.getSource(), "assignments.tag",
                                () -> assignmentsForTag(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "tag"),
                                        1)))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> run(context.getSource(), "assignments.tag",
                                        () -> assignmentsForTag(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "tag"),
                                                IntegerArgumentType.getInteger(context, "page")))))));
        root.then(Commands.literal("group")
                .requires(source -> has(source, "commands.tags.assignments.group"))
                .then(Commands.argument("group", StringArgumentType.word())
                        .executes(context -> run(context.getSource(), "assignments.group",
                                () -> assignments(
                                        context.getSource(),
                                        FancyTagService.TargetType.GROUP,
                                        StringArgumentType.getString(context, "group"),
                                        1)))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> run(context.getSource(), "assignments.group",
                                        () -> assignments(
                                                context.getSource(),
                                                FancyTagService.TargetType.GROUP,
                                                StringArgumentType.getString(context, "group"),
                                                IntegerArgumentType.getInteger(context, "page")))))));
        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> categoryNode() {
        return Commands.literal("category")
                .then(Commands.literal("list")
                        .requires(source -> has(source, "commands.tags.category.list"))
                        .executes(context -> run(context.getSource(), "category.list",
                                () -> listCategories(context.getSource()))))
                .then(Commands.literal("create")
                        .requires(source -> has(source, "commands.tags.category.create"))
                        .then(Commands.argument("resource_key", StringArgumentType.word())
                                .executes(context -> run(context.getSource(), "category.create",
                                        () -> createCategory(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "resource_key"))))))
                .then(Commands.literal("edit")
                        .requires(source -> has(source, "commands.tags.category.edit"))
                        .then(Commands.argument("category", StringArgumentType.word())
                                .then(Commands.argument("display_name", StringArgumentType.greedyString())
                                        .executes(context -> run(context.getSource(), "category.edit",
                                                () -> editCategory(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "category"),
                                                        StringArgumentType.getString(context, "display_name")))))))
                .then(Commands.literal("delete")
                        .requires(source -> has(source, "commands.tags.category.delete"))
                        .then(Commands.argument("category", StringArgumentType.word())
                                .executes(context -> run(context.getSource(), "category.delete",
                                        () -> deleteCategory(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "category"))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> paletteNode() {
        return Commands.literal("palette")
                .then(Commands.literal("list")
                        .requires(source -> has(source, "commands.tags.palette.list"))
                        .executes(context -> run(context.getSource(), "palette.list",
                                () -> listPalettes(context.getSource()))))
                .then(Commands.literal("create")
                        .requires(source -> has(source, "commands.tags.palette.create"))
                        .then(Commands.argument("resource_key", StringArgumentType.word())
                                .executes(context -> run(context.getSource(), "palette.create",
                                        () -> createPalette(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "resource_key"),
                                                "")))
                                .then(Commands.argument("colors", StringArgumentType.greedyString())
                                        .executes(context -> run(context.getSource(), "palette.create",
                                                () -> createPalette(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "resource_key"),
                                                        StringArgumentType.getString(context, "colors")))))))
                .then(Commands.literal("edit")
                        .requires(source -> has(source, "commands.tags.palette.edit"))
                        .then(Commands.argument("palette", StringArgumentType.word())
                                .then(Commands.argument("colors", StringArgumentType.greedyString())
                                        .executes(context -> run(context.getSource(), "palette.edit",
                                                () -> editPalette(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "palette"),
                                                        StringArgumentType.getString(context, "colors")))))))
                .then(Commands.literal("delete")
                        .requires(source -> has(source, "commands.tags.palette.delete"))
                        .then(Commands.argument("palette", StringArgumentType.word())
                                .executes(context -> run(context.getSource(), "palette.delete",
                                        () -> deletePalette(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "palette"))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> templateNode() {
        return Commands.literal("template")
                .then(Commands.literal("list")
                        .requires(source -> has(source, "commands.tags.template.list"))
                        .executes(context -> run(context.getSource(), "template.list",
                                () -> listTemplates(context.getSource()))))
                .then(Commands.literal("create")
                        .requires(source -> has(source, "commands.tags.template.create"))
                        .then(Commands.argument("resource_key", StringArgumentType.word())
                                .then(Commands.argument("width", IntegerArgumentType.integer(1, 256))
                                        .then(Commands.argument("height", IntegerArgumentType.integer(1, 64))
                                                .executes(context -> run(context.getSource(), "template.create",
                                                        () -> createTemplate(
                                                                context.getSource(),
                                                                StringArgumentType.getString(context, "resource_key"),
                                                                IntegerArgumentType.getInteger(context, "width"),
                                                                IntegerArgumentType.getInteger(context, "height"),
                                                                "#00000000")))
                                                .then(Commands.argument("fill", StringArgumentType.word())
                                                        .executes(context -> run(context.getSource(), "template.create",
                                                                () -> createTemplate(
                                                                        context.getSource(),
                                                                        StringArgumentType.getString(context, "resource_key"),
                                                                        IntegerArgumentType.getInteger(context, "width"),
                                                                        IntegerArgumentType.getInteger(context, "height"),
                                                                        StringArgumentType.getString(context, "fill")))))))))
                .then(Commands.literal("edit")
                        .requires(source -> has(source, "commands.tags.template.edit"))
                        .then(Commands.argument("template", StringArgumentType.word())
                                .then(Commands.argument("width", IntegerArgumentType.integer(1, 256))
                                        .then(Commands.argument("height", IntegerArgumentType.integer(1, 64))
                                                .then(Commands.argument("fill", StringArgumentType.word())
                                                        .executes(context -> run(context.getSource(), "template.edit",
                                                                () -> editTemplate(
                                                                        context.getSource(),
                                                                        StringArgumentType.getString(context, "template"),
                                                                        IntegerArgumentType.getInteger(context, "width"),
                                                                        IntegerArgumentType.getInteger(context, "height"),
                                                                        StringArgumentType.getString(context, "fill")))))))))
                .then(Commands.literal("delete")
                        .requires(source -> has(source, "commands.tags.template.delete"))
                        .then(Commands.argument("template", StringArgumentType.word())
                                .executes(context -> run(context.getSource(), "template.delete",
                                        () -> deleteTemplate(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "template"))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> reportNode() {
        return Commands.literal("report")
                .requires(source -> has(source, "commands.tags.report"))
                .then(Commands.argument("tag", StringArgumentType.word())
                        .then(Commands.argument("reason", StringArgumentType.greedyString())
                                .executes(context -> run(context.getSource(), "report",
                                        () -> report(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "tag"),
                                                StringArgumentType.getString(context, "reason"))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> moderationNode() {
        return Commands.literal("moderation")
                .then(Commands.literal("queue")
                        .requires(source -> has(source, "commands.tags.moderation.queue"))
                        .executes(context -> run(context.getSource(), "moderation.queue",
                                () -> moderationQueue(context.getSource(), 1)))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> run(context.getSource(), "moderation.queue",
                                        () -> moderationQueue(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "page"))))))
                .then(Commands.literal("suspend")
                        .requires(source -> has(source, "commands.tags.moderation.suspend"))
                        .then(Commands.argument("tag", StringArgumentType.word())
                                .then(Commands.argument("reason", StringArgumentType.greedyString())
                                        .executes(context -> run(context.getSource(), "moderation.suspend",
                                                () -> moderationSuspend(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "tag"),
                                                        StringArgumentType.getString(context, "reason")))))))
                .then(Commands.literal("clear")
                        .requires(source -> has(source, "commands.tags.moderation.clear"))
                        .then(Commands.argument("tag", StringArgumentType.word())
                                .executes(context -> run(context.getSource(), "moderation.clear",
                                        () -> moderationClear(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "tag"))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> importNode() {
        return Commands.literal("import")
                .then(Commands.literal("list")
                        .requires(source -> has(source, "commands.tags.import.inspect"))
                        .executes(context -> run(context.getSource(), "import.list",
                                () -> listImports(context.getSource(), 1)))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> run(context.getSource(), "import.list",
                                        () -> listImports(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "page"))))))
                .then(Commands.literal("scan")
                        .requires(source -> has(source, "commands.tags.import.scan"))
                        .executes(context -> run(context.getSource(), "import.scan",
                                () -> scanImports(context.getSource()))))
                .then(Commands.literal("inspect")
                        .requires(source -> has(source, "commands.tags.import.inspect"))
                        .then(Commands.argument("candidate_id", StringArgumentType.word())
                                .executes(context -> run(context.getSource(), "import.inspect",
                                        () -> inspectImport(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "candidate_id"))))))
                .then(Commands.literal("approve")
                        .requires(source -> has(source, "commands.tags.import.approve"))
                        .then(Commands.argument("candidate_id", StringArgumentType.word())
                                .then(Commands.argument("tag", StringArgumentType.word())
                                        .executes(context -> run(context.getSource(), "import.approve",
                                                () -> approveImport(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "candidate_id"),
                                                        StringArgumentType.getString(context, "tag")))))))
                .then(Commands.literal("reject")
                        .requires(source -> has(source, "commands.tags.import.reject"))
                        .then(Commands.argument("candidate_id", StringArgumentType.word())
                                .executes(context -> run(context.getSource(), "import.reject",
                                        () -> rejectImport(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "candidate_id"))))))
                .then(Commands.literal("url")
                        .requires(source -> has(source, "commands.tags.import.url"))
                        .then(Commands.argument("url", StringArgumentType.greedyString())
                                .executes(context -> run(context.getSource(), "import.url",
                                        () -> fail(
                                                context.getSource(),
                                                "remote url imports are disabled by security policy")))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> leaseNode() {
        return Commands.literal("lease")
                .then(Commands.literal("acquire")
                        .requires(source -> has(source, "commands.tags.lease.acquire"))
                        .then(Commands.argument("tag", StringArgumentType.word())
                                .executes(context -> run(context.getSource(), "lease.acquire",
                                        () -> acquireLease(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "tag"))))))
                .then(Commands.literal("renew")
                        .requires(source -> has(source, "commands.tags.lease.renew"))
                        .then(Commands.argument("lease_id", StringArgumentType.word())
                                .executes(context -> run(context.getSource(), "lease.renew",
                                        () -> renewLease(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "lease_id"))))))
                .then(Commands.literal("status")
                        .requires(source -> has(source, "commands.tags.lease.view"))
                        .executes(context -> run(context.getSource(), "lease.view",
                                () -> leaseStatus(context.getSource(), null)))
                        .then(Commands.argument("tag", StringArgumentType.word())
                                .executes(context -> run(context.getSource(), "lease.view",
                                        () -> leaseStatus(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "tag"))))))
                .then(Commands.literal("release")
                        .requires(source -> has(source, "commands.tags.lease.override"))
                        .then(Commands.argument("lease_or_tag", StringArgumentType.word())
                                .executes(context -> run(context.getSource(), "lease.override",
                                        () -> releaseLease(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "lease_or_tag"))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> integrityNode() {
        return Commands.literal("integrity")
                .then(Commands.literal("check")
                        .requires(source -> has(source, "commands.tags.integrity.check"))
                        .executes(context -> run(context.getSource(), "integrity.check",
                                () -> integrity(context.getSource(), "all")))
                        .then(Commands.argument("tag_or_all", StringArgumentType.word())
                                .executes(context -> run(context.getSource(), "integrity.check",
                                        () -> integrity(
                                                context.getSource(),
                                                StringArgumentType.getString(
                                                        context,
                                                        "tag_or_all"))))))
                .then(Commands.literal("repair")
                        .requires(source -> has(source, "commands.tags.integrity.repair"))
                        .then(Commands.argument("repair_id", StringArgumentType.word())
                                .executes(context -> run(context.getSource(), "integrity.repair",
                                        () -> integrityRepair(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "repair_id"))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> cacheNode() {
        return Commands.literal("cache")
                .then(Commands.literal("status")
                        .requires(source -> has(source, "commands.tags.cache.status"))
                        .executes(context -> run(context.getSource(), "cache.status",
                                () -> cacheStatus(context.getSource(), null)))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> run(context.getSource(), "cache.status",
                                        () -> cacheStatus(
                                                context.getSource(),
                                                UUID.fromString(targetId(context, "player")))))))
                .then(Commands.literal("invalidate")
                        .requires(source -> has(source, "commands.tags.cache.invalidate"))
                        .then(Commands.argument("tag_or_hash", StringArgumentType.word())
                                .executes(context -> run(context.getSource(), "cache.invalidate",
                                        () -> cacheInvalidate(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "tag_or_hash"))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> transferNode() {
        return Commands.literal("transfer")
                .then(Commands.literal("status")
                        .requires(source -> has(source, "commands.tags.transfer.status"))
                        .executes(context -> run(context.getSource(), "transfer.status",
                                () -> transferStatus(context.getSource(), null)))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> run(context.getSource(), "transfer.status",
                                        () -> transferStatus(
                                                context.getSource(),
                                                UUID.fromString(targetId(context, "player")))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> auditNode() {
        return Commands.literal("audit")
                .requires(source -> has(source, "commands.tags.audit"))
                .executes(context -> run(context.getSource(), "audit",
                        () -> audit(context.getSource(), "", 1)))
                .then(Commands.argument("filter", StringArgumentType.word())
                        .executes(context -> run(context.getSource(), "audit",
                                () -> audit(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "filter"),
                                        1)))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> run(context.getSource(), "audit",
                                        () -> audit(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "filter"),
                                                IntegerArgumentType.getInteger(context, "page"))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> backupNode() {
        return Commands.literal("backup")
                .then(Commands.literal("preview")
                        .requires(source -> has(source, "commands.tags.backup.preview"))
                        .executes(context -> run(context.getSource(), "backup.preview",
                                () -> backupPreview(context.getSource()))))
                .then(Commands.literal("create")
                        .requires(source -> has(source, "commands.tags.backup.create"))
                        .executes(context -> run(context.getSource(), "backup.create",
                                () -> backupCreate(context.getSource()))))
                .then(Commands.literal("list")
                        .requires(source -> has(source, "commands.tags.backup.list"))
                        .executes(context -> run(context.getSource(), "backup.list",
                                () -> backupList(context.getSource()))))
                .then(Commands.literal("restore")
                        .requires(source -> has(source, "commands.tags.backup.restore"))
                        .then(Commands.argument("backup", StringArgumentType.word())
                                .suggests((context, builder) -> net.minecraft.commands.SharedSuggestionProvider
                                        .suggest(KernelServices.fancyTags().backups(), builder))
                                .executes(context -> run(context.getSource(), "backup.restore",
                                        () -> backupRestore(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "backup"))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> garbageCollectionNode() {
        return Commands.literal("gc")
                .then(Commands.literal("preview")
                        .requires(source -> has(source, "commands.tags.gc.preview"))
                        .executes(context -> run(context.getSource(), "gc.preview",
                                () -> garbageCollect(context.getSource(), false))))
                .then(Commands.literal("run")
                        .requires(source -> has(source, "commands.tags.gc.run"))
                        .executes(context -> run(context.getSource(), "gc.run",
                                () -> garbageCollect(context.getSource(), true))));
    }

    private static int status(CommandSourceStack source) {
        FancyTagService service = KernelServices.fancyTags();
        info(source, "fancy tags is " + (service.settings().enabled() ? "enabled" : "disabled")
                + ", repository " + service.state().name().toLowerCase(Locale.ROOT)
                + ", registry revision " + service.registryRevision()
                + ", tags " + service.tags().size()
                + ", assignments " + service.assignments().size());
        return 1;
    }

    private static int openManagerOrStatus(CommandSourceStack source, String section) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return status(source);
        }
        SefSessionManager.SessionView session =
                SefSessionManager.instance().session(player).orElse(null);
        SefProtocol.Feature required = section.equals("local")
                ? SefProtocol.Feature.FANCY_TAGS_LOCAL_OVERLAY
                : SefProtocol.Feature.FANCY_TAGS_MANAGER;
        if (session == null
                || !session.supports(required)
                || !player.connection.hasChannel(SefPayloads.OpenFancyTagsStudio.TYPE)
                || !section.equals("local") && !has(source, "tags.manage.open")) {
            return status(source);
        }
        PacketDistributor.sendToPlayer(player, new SefPayloads.OpenFancyTagsStudio(
                session.sessionId(),
                section));
        success(source, "opening the Fancy Tags " + section + " screen");
        return 1;
    }

    private static int list(CommandSourceStack source) {
        List<FancyTagService.TagRecord> tags = KernelServices.fancyTags().tags();
        if (tags.isEmpty()) {
            info(source, "no Fancy Tags definitions exist");
            return 1;
        }
        for (FancyTagService.TagRecord tag : tags.subList(0, Math.min(tags.size(), 100))) {
            info(source, tag.resourceKey() + ", " + tag.displayName() + ", "
                    + tag.status().name().toLowerCase(Locale.ROOT)
                    + ", revision " + tag.recordRevision());
        }
        return tags.size();
    }

    private static int view(CommandSourceStack source, String reference) {
        FancyTagService.TagRecord tag = KernelServices.fancyTags().find(reference).orElse(null);
        if (tag == null) {
            return fail(source, "tag not found");
        }
        info(source, tag.resourceKey() + ", " + tag.displayName());
        info(source, "status " + tag.status().name().toLowerCase(Locale.ROOT)
                + ", record revision " + tag.recordRevision()
                + ", artwork revision " + tag.currentRevision());
        info(source, "alternative text " + tag.alternativeText());
        return 1;
    }

    private static int create(CommandSourceStack source, String key, String displayName) {
        return result(
                source,
                KernelServices.fancyTags().createDraft(key, displayName, actorId(source)),
                "tag draft created");
    }

    private static int duplicate(
            CommandSourceStack source,
            String sourceTag,
            String resourceKey,
            String displayName
    ) {
        return result(
                source,
                KernelServices.fancyTags().duplicate(sourceTag, resourceKey, displayName, actorId(source)),
                "tag draft duplicated");
    }

    private static int validate(CommandSourceStack source, String reference) {
        FancyTagService.TagRecord tag = KernelServices.fancyTags().find(reference).orElse(null);
        if (tag == null) {
            return fail(source, "tag not found");
        }
        if (tag.currentRevision() < 1L) {
            return fail(source, "tag has no artwork revision");
        }
        FancyTagObjectStore.IntegrityReport report = KernelServices.fancyTags().integrity();
        boolean invalid = tag.revisions().stream().anyMatch(revision ->
                report.missing().contains(revision.contentHash())
                        || report.corrupt().contains(revision.contentHash()));
        if (invalid) {
            return fail(source, "tag artwork failed integrity validation");
        }
        success(source, "tag definition and artwork are valid");
        return 1;
    }

    private static int edit(CommandSourceStack source, String reference, String field, String value) {
        FancyTagService.TagRecord current = KernelServices.fancyTags().find(reference).orElse(null);
        if (current == null) {
            return fail(source, "tag not found");
        }
        String displayName = null;
        String description = null;
        String category = null;
        String visibilityPermission = null;
        String alternativeText = null;
        java.util.Set<FancyTagService.RenderContext> contexts = null;
        try {
            switch (field) {
                case "name" -> displayName = value;
                case "description" -> description = value;
                case "category" -> category = value.equalsIgnoreCase("none") ? "" : value;
                case "permission" -> visibilityPermission = value.equalsIgnoreCase("none") ? "" : value;
                case "alternative" -> alternativeText = value;
                case "contexts" -> {
                    contexts = EnumSet.noneOf(FancyTagService.RenderContext.class);
                    for (String token : value.split(",")) {
                        contexts.add(FancyTagService.RenderContext.valueOf(
                                token.trim().toUpperCase(Locale.ROOT)));
                    }
                }
                default -> {
                    return fail(source, "unknown tag field");
                }
            }
        } catch (IllegalArgumentException exception) {
            return fail(source, "tag render contexts are invalid");
        }
        return result(
                source,
                KernelServices.fancyTags().updateMetadata(
                        reference,
                        displayName,
                        description,
                        category,
                        visibilityPermission,
                        contexts,
                        alternativeText,
                        actorId(source),
                        current.recordRevision()),
                "tag metadata updated");
    }

    private static int purge(CommandSourceStack source, String reference) {
        FancyTagService.TagRecord current = KernelServices.fancyTags().find(reference).orElse(null);
        if (current == null) {
            return fail(source, "tag not found");
        }
        return result(
                source,
                KernelServices.fancyTags().deletePending(
                        reference,
                        actorId(source),
                        current.recordRevision()),
                "tag deleted");
    }

    private static int changeStatus(
            CommandSourceStack source,
            String reference,
            FancyTagService.TagStatus status
    ) {
        FancyTagService.TagRecord current = KernelServices.fancyTags().find(reference).orElse(null);
        if (current == null) {
            return fail(source, "tag not found");
        }
        return result(
                source,
                KernelServices.fancyTags().changeStatus(
                        reference,
                        status,
                        actorId(source),
                        current.recordRevision()),
                "tag status changed");
    }

    private static int listRevisions(CommandSourceStack source, String reference, int page) {
        FancyTagService.TagRecord tag = KernelServices.fancyTags().find(reference).orElse(null);
        if (tag == null) {
            return fail(source, "tag not found");
        }
        List<FancyTagService.ArtworkRevision> values = page(
                source,
                tag.revisions(),
                page,
                "tag revisions");
        if (values == null) {
            return 0;
        }
        for (FancyTagService.ArtworkRevision revision : values) {
            info(source, "revision " + revision.revision()
                    + ", " + revision.width() + " by " + revision.height()
                    + ", " + revision.encodedBytes() + " bytes");
        }
        return Math.max(1, values.size());
    }

    private static int viewRevision(CommandSourceStack source, String reference, long revision) {
        FancyTagService.TagRecord tag = KernelServices.fancyTags().find(reference).orElse(null);
        FancyTagService.ArtworkRevision artwork = tag == null ? null : tag.revisions().stream()
                .filter(value -> value.revision() == revision)
                .findFirst()
                .orElse(null);
        if (artwork == null) {
            return fail(source, "tag artwork revision not found");
        }
        info(source, "revision " + artwork.revision()
                + ", " + artwork.width() + " by " + artwork.height()
                + ", " + artwork.encodedBytes() + " encoded bytes");
        return 1;
    }

    private static int restoreRevision(CommandSourceStack source, String reference, long revision) {
        FancyTagService.TagRecord current = KernelServices.fancyTags().find(reference).orElse(null);
        if (current == null) {
            return fail(source, "tag not found");
        }
        ActionResult<FancyTagService.ArtworkRevision> operation = KernelServices.fancyTags().restoreRevision(
                reference,
                revision,
                actorId(source),
                current.recordRevision());
        if (operation.successful()) {
            com.enviouse.sef.gui.protocol.SefGuiServer.refreshFancyTags(source.getServer());
        }
        return result(source, operation, "tag artwork revision restored");
    }

    private static int export(CommandSourceStack source, String reference, long revision) {
        return export(source, reference, revision, "png");
    }

    private static int export(
            CommandSourceStack source,
            String reference,
            long revision,
            String format
    ) {
        String requestedName = reference.toLowerCase(Locale.ROOT).replace(':', '_');
        ActionResult<java.nio.file.Path> operation = switch (format) {
            case "png" -> KernelServices.fancyTags().exportArtwork(
                    reference, revision, requestedName, actorId(source));
            case "project" -> KernelServices.fancyTags().exportProject(
                    reference, revision, requestedName, actorId(source));
            case "manifest" -> KernelServices.fancyTags().exportManifest(
                    reference, revision, requestedName, actorId(source));
            default -> ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "unknown export format");
        };
        if (!operation.successful()) {
            return fail(source, operation.detail());
        }
        success(source, "tag " + format + " exported as " + operation.value().getFileName());
        return 1;
    }

    private static int assign(
            CommandSourceStack source,
            FancyTagService.TargetType type,
            String targetId,
            String tag,
            String slot,
            int priority,
            Instant expiresAt
    ) {
        ActionResult<Void> authorization = authorizeAssignment(source, type, targetId, slot);
        if (!authorization.successful()) {
            return fail(source, authorization.detail());
        }
        final FancyTagService.TagSlot parsed;
        try {
            parsed = FancyTagService.TagSlot.valueOf(slot.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return fail(source, "unknown tag slot");
        }
        return result(
                source,
                KernelServices.fancyTags().assign(
                        tag,
                        type,
                        targetId,
                        parsed,
                        priority,
                        expiresAt,
                        actorId(source)),
                "tag assignment created");
    }

    private static ActionResult<Void> authorizeAssignment(
            CommandSourceStack source,
            FancyTagService.TargetType type,
            String targetId,
            String slot
    ) {
        String normalizedTarget = targetId.toLowerCase(Locale.ROOT);
        if (type == FancyTagService.TargetType.PLAYER) {
            UUID targetUuid;
            try {
                targetUuid = UUID.fromString(normalizedTarget);
            } catch (IllegalArgumentException exception) {
                return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "invalid player target");
            }
            ServerPlayer target = source.getServer().getPlayerList().getPlayer(targetUuid);
            if (target == null) {
                return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "player target is not online");
            }
            var decision = PlayerTargetPolicy.decide(
                    source,
                    target,
                    PermissionsHandler.phasePermission("tags.assign.hierarchy.override"),
                    PermissionsHandler.phasePermission("tags.assign.exempt"),
                    PermissionsHandler.phasePermission("tags.assign.exemption.override"),
                    false,
                    true);
            if (!decision.allowed()) {
                return ActionResult.failure(decision.reason(), "player target is protected");
            }
            if (source.getPlayer() != null
                    && source.getPlayer() != target
                    && VanishUtil.isVanished(target, source.getPlayer())
                    && !has(source, "tags.assign.vanished")) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.TARGET_DENIED,
                        "vanished player target is unavailable");
            }
        } else if (type == FancyTagService.TargetType.GROUP
                && !FancyTagGroupResolver.health().healthy()) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.PROVIDER_ERROR,
                    "tag group provider is unavailable");
        } else if (type == FancyTagService.TargetType.TEAM
                && source.getServer().getScoreboard().getPlayerTeam(normalizedTarget) == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "scoreboard team not found");
        }
        boolean occupied = KernelServices.fancyTags().assignments().stream()
                .anyMatch(value -> value.enabled()
                        && value.targetType() == type
                        && value.targetId().equals(normalizedTarget)
                        && value.slot().name().equalsIgnoreCase(slot));
        if (occupied && !has(source, "tags.assign.multiple")) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.POLICY_DENIED,
                    "tag assignment slot already has an active tag");
        }
        return ActionResult.success(null);
    }

    private static int assignDuration(
            CommandSourceStack source,
            FancyTagService.TargetType type,
            String targetId,
            String tag,
            String slot,
            int priority,
            String duration
    ) {
        DurationParser.Result parsed = DurationParser.parse(duration, false);
        if (!parsed.valid() || parsed.seconds() > 31_536_000L) {
            return fail(source, "tag assignment duration is invalid or exceeds one year");
        }
        Instant expiresAt;
        try {
            expiresAt = Instant.now().plusSeconds(parsed.seconds());
        } catch (RuntimeException exception) {
            return fail(source, "tag assignment duration is outside the supported range");
        }
        return assign(source, type, targetId, tag, slot, priority, expiresAt);
    }

    private static int unassign(CommandSourceStack source, String assignmentId) {
        try {
            return result(
                    source,
                    KernelServices.fancyTags().unassign(UUID.fromString(assignmentId), actorId(source)),
                    "tag assignment removed");
        } catch (IllegalArgumentException exception) {
            return fail(source, "invalid assignment id");
        }
    }

    private static int assignments(
            CommandSourceStack source,
            FancyTagService.TargetType type,
            String targetId,
            int page
    ) {
        List<FancyTagService.AssignmentRecord> all = KernelServices.fancyTags().assignments().stream()
                .filter(value -> value.targetType() == type
                        && value.targetId().equals(targetId.toLowerCase(Locale.ROOT)))
                .toList();
        List<FancyTagService.AssignmentRecord> values =
                page(source, all, page, "tag assignments");
        if (values == null) {
            return 0;
        }
        values.forEach(value -> info(source,
                value.id() + ", " + value.slot().name().toLowerCase(Locale.ROOT)
                        + ", priority " + value.priority()));
        return Math.max(1, values.size());
    }

    private static int assignmentsForTag(CommandSourceStack source, String reference, int page) {
        FancyTagService.TagRecord tag = KernelServices.fancyTags().find(reference).orElse(null);
        if (tag == null) {
            return fail(source, "tag not found");
        }
        List<FancyTagService.AssignmentRecord> all = KernelServices.fancyTags().assignments().stream()
                .filter(value -> value.tagId().equals(tag.id()))
                .toList();
        List<FancyTagService.AssignmentRecord> values =
                page(source, all, page, "tag assignments");
        if (values == null) {
            return 0;
        }
        values.forEach(value -> info(source,
                value.id() + ", " + value.targetType().name().toLowerCase(Locale.ROOT)
                        + ", " + value.targetId()));
        return Math.max(1, values.size());
    }

    private static int listCategories(CommandSourceStack source) {
        List<FancyTagService.CategoryRecord> categories = KernelServices.fancyTags().categories();
        categories.forEach(value -> info(source, value.resourceKey() + ", " + value.displayName()));
        return Math.max(1, categories.size());
    }

    private static int createCategory(CommandSourceStack source, String key) {
        return result(
                source,
                KernelServices.fancyTags().createCategory(
                        key,
                        key,
                        "minecraft:name_tag",
                        actorId(source)),
                "tag category created");
    }

    private static int editCategory(CommandSourceStack source, String reference, String displayName) {
        FancyTagService.CategoryRecord current =
                KernelServices.fancyTags().findCategory(reference).orElse(null);
        if (current == null) {
            return fail(source, "tag category not found");
        }
        return result(
                source,
                KernelServices.fancyTags().updateCategory(
                        reference,
                        displayName,
                        null,
                        null,
                        -1,
                        null,
                        actorId(source),
                        current.revision()),
                "tag category updated");
    }

    private static int deleteCategory(CommandSourceStack source, String reference) {
        FancyTagService.CategoryRecord current =
                KernelServices.fancyTags().findCategory(reference).orElse(null);
        if (current == null) {
            return fail(source, "tag category not found");
        }
        return result(
                source,
                KernelServices.fancyTags().deleteCategory(
                        reference,
                        actorId(source),
                        current.revision()),
                "tag category deleted");
    }

    private static int listPalettes(CommandSourceStack source) {
        List<FancyTagService.PaletteRecord> palettes = KernelServices.fancyTags().palettes();
        palettes.forEach(value -> info(source,
                value.resourceKey() + ", " + value.displayName()
                        + ", colors " + value.colors().size()
                        + ", revision " + value.revision()));
        return Math.max(1, palettes.size());
    }

    private static int createPalette(CommandSourceStack source, String key, String colors) {
        try {
            return result(
                    source,
                    KernelServices.fancyTags().createPalette(
                            key,
                            key,
                            parseColors(colors),
                            "",
                            actorId(source)),
                    "tag palette created");
        } catch (IllegalArgumentException exception) {
            return fail(source, exception.getMessage());
        }
    }

    private static int editPalette(CommandSourceStack source, String reference, String colors) {
        FancyTagService.PaletteRecord current =
                KernelServices.fancyTags().findPalette(reference).orElse(null);
        if (current == null) {
            return fail(source, "tag palette not found");
        }
        try {
            return result(
                    source,
                    KernelServices.fancyTags().updatePalette(
                            reference,
                            null,
                            parseColors(colors),
                            null,
                            actorId(source),
                            current.revision()),
                    "tag palette updated");
        } catch (IllegalArgumentException exception) {
            return fail(source, exception.getMessage());
        }
    }

    private static int deletePalette(CommandSourceStack source, String reference) {
        FancyTagService.PaletteRecord current =
                KernelServices.fancyTags().findPalette(reference).orElse(null);
        if (current == null) {
            return fail(source, "tag palette not found");
        }
        return result(
                source,
                KernelServices.fancyTags().deletePalette(
                        reference,
                        actorId(source),
                        current.revision()),
                "tag palette deleted");
    }

    private static int listTemplates(CommandSourceStack source) {
        List<FancyTagService.TemplateRecord> templates = KernelServices.fancyTags().templates();
        templates.forEach(value -> info(source,
                value.resourceKey() + ", " + value.width() + " by " + value.height()
                        + ", revision " + value.revision()));
        return Math.max(1, templates.size());
    }

    private static int createTemplate(
            CommandSourceStack source,
            String key,
            int width,
            int height,
            String fill
    ) {
        try {
            return result(
                    source,
                    KernelServices.fancyTags().createTemplate(
                            key,
                            key,
                            width,
                            height,
                            fill,
                            "",
                            actorId(source)),
                    "tag template created");
        } catch (IllegalArgumentException exception) {
            return fail(source, exception.getMessage());
        }
    }

    private static int editTemplate(
            CommandSourceStack source,
            String reference,
            int width,
            int height,
            String fill
    ) {
        FancyTagService.TemplateRecord current =
                KernelServices.fancyTags().findTemplate(reference).orElse(null);
        if (current == null) {
            return fail(source, "tag template not found");
        }
        try {
            return result(
                    source,
                    KernelServices.fancyTags().updateTemplate(
                            reference,
                            null,
                            width,
                            height,
                            fill,
                            null,
                            actorId(source),
                            current.revision()),
                    "tag template updated");
        } catch (IllegalArgumentException exception) {
            return fail(source, exception.getMessage());
        }
    }

    private static int deleteTemplate(CommandSourceStack source, String reference) {
        FancyTagService.TemplateRecord current =
                KernelServices.fancyTags().findTemplate(reference).orElse(null);
        if (current == null) {
            return fail(source, "tag template not found");
        }
        return result(
                source,
                KernelServices.fancyTags().deleteTemplate(
                        reference,
                        actorId(source),
                        current.revision()),
                "tag template deleted");
    }

    private static int report(CommandSourceStack source, String reference, String reason) {
        UUID actor = actorId(source);
        if (actor.equals(new UUID(0L, 0L))) {
            return fail(source, "tag reports require a player");
        }
        return result(
                source,
                KernelServices.fancyTags().report(reference, actor, reason),
                "tag report submitted");
    }

    private static int moderationQueue(CommandSourceStack source, int page) {
        List<FancyTagService.ReportRecord> all = KernelServices.fancyTags().reports().stream()
                .filter(report -> report.status() == FancyTagService.ReportStatus.OPEN)
                .toList();
        List<FancyTagService.ReportRecord> reports =
                page(source, all, page, "tag moderation reports");
        if (reports == null) {
            return 0;
        }
        for (FancyTagService.ReportRecord report : reports) {
            FancyTagService.TagRecord tag =
                    KernelServices.fancyTags().find(report.tagId().toString()).orElse(null);
            info(source, report.id() + ", "
                    + (tag == null ? "missing" : tag.resourceKey())
                    + ", reporter " + report.reporterId()
                    + ", " + report.reason());
        }
        return Math.max(1, reports.size());
    }

    private static <T> List<T> page(
            CommandSourceStack source,
            List<T> values,
            int requestedPage,
            String label
    ) {
        int pageSize = 10;
        int pages = Math.max(1, (values.size() + pageSize - 1) / pageSize);
        if (requestedPage < 1 || requestedPage > pages) {
            fail(source, label + " page is outside the available range");
            return null;
        }
        info(source, label + ", page " + requestedPage + " of " + pages);
        int start = Math.min(values.size(), (requestedPage - 1) * pageSize);
        int end = Math.min(values.size(), start + pageSize);
        return values.subList(start, end);
    }

    private static int moderationSuspend(CommandSourceStack source, String reference, String reason) {
        FancyTagService.TagRecord current = KernelServices.fancyTags().find(reference).orElse(null);
        if (current == null) {
            return fail(source, "tag not found");
        }
        ActionResult<FancyTagService.TagRecord> operation = KernelServices.fancyTags().changeStatus(
                reference,
                FancyTagService.TagStatus.SUSPENDED,
                actorId(source),
                current.recordRevision());
        if (operation.successful()) {
            com.enviouse.sef.gui.protocol.SefGuiServer.refreshFancyTags(source.getServer());
            success(source, "tag suspended, " + reason);
            return 1;
        }
        return fail(source, operation.detail());
    }

    private static int moderationClear(CommandSourceStack source, String reference) {
        FancyTagService.TagRecord current = KernelServices.fancyTags().find(reference).orElse(null);
        if (current == null) {
            return fail(source, "tag not found");
        }
        ActionResult<FancyTagService.TagRecord> operation = KernelServices.fancyTags().clearModeration(
                reference,
                actorId(source),
                "cleared by moderator",
                current.recordRevision());
        if (operation.successful()) {
            com.enviouse.sef.gui.protocol.SefGuiServer.refreshFancyTags(source.getServer());
        }
        return result(source, operation, "tag moderation cleared");
    }

    private static List<String> parseColors(String colors) {
        if (colors == null || colors.isBlank()) {
            return List.of();
        }
        List<String> values = java.util.Arrays.stream(colors.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
        if (values.isEmpty() || values.size() > 64) {
            throw new IllegalArgumentException("tag palette needs one to sixty four colors");
        }
        return values;
    }

    private static int scanImports(CommandSourceStack source) {
        if (!ConfigHandler.config.fancyTagsServerInboxEnabled.get()) {
            return fail(source, "Fancy Tags server inbox is disabled");
        }
        List<FancyTagObjectStore.ImportCandidate> candidates =
                KernelServices.fancyTags().scanImportInbox();
        info(source, "stable import candidates " + candidates.size());
        candidates.forEach(candidate -> info(
                source,
                candidate.candidateId() + ", " + candidate.fileName() + ", "
                        + candidate.encodedBytes() + " bytes"));
        return Math.max(1, candidates.size());
    }

    private static int listImports(CommandSourceStack source, int page) {
        List<FancyTagObjectStore.ImportCandidate> candidates =
                KernelServices.fancyTags().importCandidates();
        int pageSize = 10;
        int pages = Math.max(1, (candidates.size() + pageSize - 1) / pageSize);
        if (page > pages) {
            return fail(source, "tag import page is outside the available range");
        }
        info(source, "tag import candidates, page " + page + " of " + pages);
        candidates.stream()
                .skip((long) (page - 1) * pageSize)
                .limit(pageSize)
                .forEach(candidate -> info(source,
                        candidate.candidateId() + ", " + candidate.fileName() + ", "
                                + candidate.encodedBytes() + " bytes"));
        return 1;
    }

    private static int inspectImport(CommandSourceStack source, String candidateId) {
        FancyTagObjectStore.ImportCandidate candidate =
                KernelServices.fancyTags().importCandidate(candidateId).orElse(null);
        if (candidate == null) {
            return fail(source, "import candidate not found");
        }
        info(source, candidate.candidateId() + ", " + candidate.fileName()
                + ", " + candidate.encodedBytes() + " bytes"
                + ", modified " + candidate.modifiedAt());
        return 1;
    }

    private static int approveImport(CommandSourceStack source, String candidateId, String tagReference) {
        FancyTagService service = KernelServices.fancyTags();
        UUID actor = actorId(source);
        FancyTagService.TagRecord tag = service.find(tagReference).orElse(null);
        boolean created = false;
        if (tag == null) {
            ActionResult<FancyTagService.TagRecord> draft =
                    service.createDraft(tagReference, tagReference, actor);
            if (!draft.successful()) {
                return fail(source, draft.detail());
            }
            tag = draft.value();
            created = true;
        }
        ActionResult<FancyTagService.ArtworkRevision> imported = service.approveImport(
                candidateId,
                tagReference,
                actor,
                tag.recordRevision());
        if (!imported.successful() && created) {
            FancyTagService.TagRecord current = service.find(tagReference).orElse(null);
            if (current != null && current.revisions().isEmpty()) {
                ActionResult<FancyTagService.TagRecord> pending = service.changeStatus(
                        tagReference,
                        FancyTagService.TagStatus.PENDING_DELETE,
                        actor,
                        current.recordRevision());
                if (pending.successful()) {
                    service.deletePending(tagReference, actor, pending.value().recordRevision());
                }
            }
        }
        return result(source, imported, "tag artwork imported into draft " + tagReference);
    }

    private static int rejectImport(CommandSourceStack source, String candidateId) {
        return result(
                source,
                KernelServices.fancyTags().rejectImport(candidateId, actorId(source)),
                "tag import candidate rejected");
    }

    private static int leaseStatus(CommandSourceStack source, String reference) {
        UUID tagId = reference == null
                ? null
                : KernelServices.fancyTags().find(reference)
                .map(FancyTagService.TagRecord::id)
                .orElse(null);
        if (reference != null && tagId == null) {
            return fail(source, "tag not found");
        }
        List<FancyTagService.EditLease> leases = KernelServices.fancyTags().leases().stream()
                .filter(value -> tagId == null || value.tagId().equals(tagId))
                .toList();
        leases.forEach(value -> info(source, value.leaseId() + ", tag " + value.tagId()
                + ", holder " + value.holder() + ", expires " + value.expiresAt()));
        return Math.max(1, leases.size());
    }

    private static int acquireLease(CommandSourceStack source, String reference) {
        UUID actor = actorId(source);
        if (actor.equals(new UUID(0L, 0L))) {
            return fail(source, "tag edit leases require a player");
        }
        FancyTagService.TagRecord tag = KernelServices.fancyTags().find(reference).orElse(null);
        if (tag == null) {
            return fail(source, "tag not found");
        }
        ActionResult<FancyTagService.EditLease> operation = KernelServices.fancyTags().acquireLease(
                reference,
                actor,
                tag.recordRevision(),
                false);
        if (!operation.successful()) {
            return fail(source, operation.detail());
        }
        success(source, "tag edit lease acquired, " + operation.value().leaseId());
        return 1;
    }

    private static int renewLease(CommandSourceStack source, String leaseId) {
        try {
            return result(
                    source,
                    KernelServices.fancyTags().renewLease(UUID.fromString(leaseId), actorId(source)),
                    "tag edit lease renewed");
        } catch (IllegalArgumentException exception) {
            return fail(source, "invalid tag edit lease id");
        }
    }

    private static int releaseLease(CommandSourceStack source, String leaseReference) {
        try {
            UUID leaseId;
            try {
                leaseId = UUID.fromString(leaseReference);
            } catch (IllegalArgumentException exception) {
                UUID tagId = KernelServices.fancyTags().find(leaseReference)
                        .map(FancyTagService.TagRecord::id)
                        .orElse(null);
                if (tagId == null) {
                    return fail(source, "tag edit lease not found");
                }
                leaseId = KernelServices.fancyTags().leases().stream()
                        .filter(value -> value.tagId().equals(tagId))
                        .map(FancyTagService.EditLease::leaseId)
                        .findFirst()
                        .orElse(null);
                if (leaseId == null) {
                    return fail(source, "tag edit lease not found");
                }
            }
            return result(
                    source,
                    KernelServices.fancyTags().releaseLease(
                            leaseId,
                            actorId(source),
                            true),
                    "tag edit lease released");
        } catch (IllegalArgumentException exception) {
            return fail(source, "invalid lease id");
        }
    }

    private static int integrity(CommandSourceStack source, String reference) {
        FancyTagObjectStore.IntegrityReport report = KernelServices.fancyTags().integrity();
        Set<String> selected;
        if (reference.equalsIgnoreCase("all")) {
            selected = new java.util.LinkedHashSet<>();
            selected.addAll(report.missing());
            selected.addAll(report.corrupt());
        } else {
            FancyTagService.TagRecord tag = KernelServices.fancyTags().find(reference).orElse(null);
            if (tag == null) {
                return fail(source, "tag not found");
            }
            selected = tag.revisions().stream()
                    .map(FancyTagService.ArtworkRevision::contentHash)
                    .filter(hash -> report.missing().contains(hash) || report.corrupt().contains(hash))
                    .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
        }
        long missing = selected.stream().filter(report.missing()::contains).count();
        long corrupt = selected.stream().filter(report.corrupt()::contains).count();
        info(source, "missing " + missing
                + ", corrupt " + corrupt
                + ", orphaned " + report.orphaned().size()
                + ", stored bytes " + report.storedBytes());
        selected.stream().filter(report.missing()::contains).limit(20)
                .forEach(hash -> info(source, "repair id missing_" + hash));
        selected.stream().filter(report.corrupt()::contains).limit(20)
                .forEach(hash -> info(source, "repair id corrupt_" + hash));
        return selected.isEmpty() ? 1 : 0;
    }

    private static int integrityRepair(CommandSourceStack source, String repairId) {
        FancyTagObjectStore.IntegrityReport report = KernelServices.fancyTags().integrity();
        boolean staged = report.missing().stream().anyMatch(hash -> repairId.equals("missing_" + hash))
                || report.corrupt().stream().anyMatch(hash -> repairId.equals("corrupt_" + hash));
        if (!staged) {
            return fail(source, "tag integrity repair id is not staged by the current check");
        }
        return fail(source, "tag object cannot be reconstructed safely, restore a verified backup first");
    }

    private static int cacheStatus(CommandSourceStack source, UUID playerId) {
        String scope = playerId == null ? "all compatible sessions" : "player " + playerId;
        info(source, "server object store bytes " + KernelServices.fancyTags().integrity().storedBytes()
                + ", " + scope + ", client caches are private and independently bounded");
        return 1;
    }

    private static int cacheInvalidate(CommandSourceStack source, String reference) {
        FancyTagService.TagRecord tag = KernelServices.fancyTags().find(reference).orElse(null);
        String hash = tag == null
                ? reference.toLowerCase(Locale.ROOT)
                : KernelServices.fancyTags().currentArtwork(reference)
                .map(FancyTagService.ArtworkRevision::contentHash)
                .orElse("");
        if (!hash.matches("[0-9a-f]{64}")) {
            return fail(source, "tag or content hash not found");
        }
        com.enviouse.sef.gui.protocol.SefGuiServer.invalidateTag(hash);
        success(source, "tag cache invalidation sent to compatible sessions");
        return 1;
    }

    private static int transferStatus(CommandSourceStack source, UUID playerId) {
        List<FancyTagTransferService.UploadView> uploads =
                KernelServices.fancyTags().transfers().active(Instant.now()).stream()
                        .filter(upload -> playerId == null || upload.ownerId().equals(playerId))
                        .toList();
        info(source, "tag transfers are bounded to "
                + com.enviouse.sef.gui.protocol.SefProtocol.MAXIMUM_TAG_BYTES
                + " encoded bytes, active " + uploads.size());
        uploads.forEach(upload -> info(source,
                upload.uploadId() + ", " + upload.receivedBytes() + "/" + upload.totalBytes()
                        + ", next chunk " + upload.nextChunkIndex()));
        return Math.max(1, uploads.size());
    }

    private static int audit(CommandSourceStack source, String filter, int page) {
        String normalized = filter == null ? "" : filter.toLowerCase(Locale.ROOT);
        List<SecurityAuditService.AuditEvent> events = SecurityAuditService.recent(
                event -> (event.origin().equals("fancy_tags")
                        || event.actionId().startsWith("sef:tags."))
                        && (normalized.isBlank()
                        || event.actionId().contains(normalized)
                        || event.actorUsername().contains(normalized)
                        || event.result().contains(normalized)
                        || event.reasonCode().contains(normalized)),
                128);
        int pageSize = 10;
        int pages = Math.max(1, (events.size() + pageSize - 1) / pageSize);
        if (page > pages) {
            return fail(source, "tag audit page is outside the available range");
        }
        info(source, "tag audit events, page " + page + " of " + pages);
        events.stream()
                .skip((long) (page - 1) * pageSize)
                .limit(pageSize)
                .forEach(event -> info(source,
                        event.timestamp() + ", " + event.actionId() + ", "
                                + event.actorUsername() + ", " + event.result() + ", "
                                + event.reasonCode()));
        return 1;
    }

    private static int backupPreview(CommandSourceStack source) {
        FancyTagService service = KernelServices.fancyTags();
        info(source, "backup would include " + service.tags().size()
                + " tags, " + service.assignments().size()
                + " assignments, and referenced canonical objects");
        return 1;
    }

    private static int backupCreate(CommandSourceStack source) {
        ActionResult<java.nio.file.Path> result =
                KernelServices.fancyTags().createBackup(actorId(source));
        if (!result.successful()) {
            return fail(source, result.detail());
        }
        success(source, "Fancy Tags backup created as " + result.value().getFileName());
        return 1;
    }

    private static int backupList(CommandSourceStack source) {
        List<String> backups = KernelServices.fancyTags().backups();
        if (backups.isEmpty()) {
            info(source, "no Fancy Tags backups are available");
            return 1;
        }
        backups.forEach(backup -> info(source, backup));
        return backups.size();
    }

    private static int backupRestore(CommandSourceStack source, String backup) {
        ActionResult<Void> result = KernelServices.fancyTags().restoreBackup(
                backup,
                actorId(source));
        if (!result.successful()) {
            return fail(source, result.detail());
        }
        com.enviouse.sef.gui.protocol.SefGuiServer.refreshFancyTags(source.getServer());
        success(source, "Fancy Tags backup restored");
        return 1;
    }

    private static int garbageCollect(CommandSourceStack source, boolean execute) {
        FancyTagObjectStore.GarbageCollectionResult result =
                KernelServices.fancyTags().garbageCollect(execute);
        info(source, "garbage collection candidates " + result.candidates()
                + ", bytes " + result.candidateBytes()
                + ", deleted " + result.deleted());
        return 1;
    }

    private static int doctor(CommandSourceStack source) {
        FancyTagService service = KernelServices.fancyTags();
        FancyTagObjectStore.IntegrityReport report = service.integrity();
        info(source, "repository " + service.state().name().toLowerCase(Locale.ROOT)
                + ", enhanced rendering " + ConfigHandler.config.fancyTagsEnhancedRendering.get()
                + ", server inbox " + ConfigHandler.config.fancyTagsServerInboxEnabled.get());
        info(source, "missing " + report.missing().size()
                + ", corrupt " + report.corrupt().size()
                + ", orphaned " + report.orphaned().size());
        return report.missing().isEmpty() && report.corrupt().isEmpty() ? 1 : 0;
    }

    private static int reload(CommandSourceStack source) {
        KernelServices.reloadConfiguration();
        success(source, "Fancy Tags policy snapshot reloaded");
        return 1;
    }

    private static int run(CommandSourceStack source, String operation, IntSupplier action) {
        String actionId = "sef:tags." + operation;
        return KernelCommandExecutor.execute(
                source,
                actionId,
                Map.of("operation", operation),
                action,
                PermissionsHandler.phasePermission("commands.tags." + operation));
    }

    private static boolean has(CommandSourceStack source, String permission) {
        var node = PermissionsHandler.phasePermission(permission);
        return node != null && PermissionService.has(source, node);
    }

    private static UUID actorId(CommandSourceStack source) {
        return source.getEntity() == null ? new UUID(0L, 0L) : source.getEntity().getUUID();
    }

    private static String targetId(
            com.mojang.brigadier.context.CommandContext<CommandSourceStack> context,
            String name
    ) {
        try {
            return EntityArgument.getPlayer(context, name).getUUID().toString();
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) {
            return new UUID(0L, 0L).toString();
        }
    }

    private static int result(CommandSourceStack source, ActionResult<?> result, String successMessage) {
        if (!result.successful()) {
            return fail(source, result.detail().isBlank()
                    ? result.reason().name().toLowerCase(Locale.ROOT)
                    : result.detail());
        }
        success(source, successMessage);
        return 1;
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
