package com.enviouse.sef.automation;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.identity.IdentityArguments;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.command.AliasCompiler;
import com.enviouse.sef.kernel.command.BundleCompiler;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.kernel.command.CommandWrapperService;
import com.enviouse.sef.kernel.policy.ConfirmationService;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.permissions.DelegatedPermissionScope;
import com.enviouse.sef.permissions.DynamicPermissionService;
import com.enviouse.sef.permissions.EphemeralExecutionGrant;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.gui.protocol.SefGuiServer;
import com.enviouse.sef.vanish.VanishUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class AutomationCommands {
    private static final int PAGE_SIZE = 10;

    private AutomationCommands() {
    }

    public static void attach(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(aliasRoot());
        root.then(bundleRoot());
        root.then(profileRoot());
        root.then(fakeRoot());
    }

    public static void registerDirect(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(fakeJoinRoot("fakejoin", FakeIdentityService.EventType.JOIN));
        dispatcher.register(fakeJoinRoot("fakeleave", FakeIdentityService.EventType.LEAVE));
        dispatcher.register(fakeMessageRoot());
        dispatcher.register(fakeRankMessageRoot());
        dispatcher.register(sudoRoot());
        dispatcher.register(runRoot());
        dispatcher.register(silentRoot());
        KernelServices.administrativeExecution().markCommandTreePublished();
    }

    public static void registerPublishedAliases(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (AliasCompiler.AliasDefinition alias : KernelServices.aliases().published()) {
            if (!alias.enabled() || dispatcher.getRoot().getChild(alias.root()) != null) {
                continue;
            }
            if (alias.kind() == AliasCompiler.AliasKind.ACTION) {
                CommandDefinition target = KernelServices.catalog().find(alias.targetId()).orElse(null);
                if (target == null) {
                    continue;
                }
                CommandNode<CommandSourceStack> targetRoot =
                        dispatcher.getRoot().getChild(target.canonicalRoot());
                if (targetRoot == null) {
                    continue;
                }
                dispatcher.register(Commands.literal(alias.root())
                        .requires(source -> aliasAvailable(source, alias))
                        .redirect(targetRoot));
                continue;
            }
            if (alias.kind() == AliasCompiler.AliasKind.BUNDLE) {
                dispatcher.register(Commands.literal(alias.root())
                        .requires(source -> aliasAvailable(source, alias))
                        .executes(context -> runBundleAlias(context.getSource(), alias)));
            }
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> aliasRoot() {
        return Commands.literal("alias")
                .then(Commands.literal("list")
                        .requires(source -> can(source, "sef:alias.list"))
                        .executes(context -> execute(
                                context.getSource(), "sef:alias.list", Map.of(),
                                () -> listAliases(context.getSource()))))
                .then(Commands.literal("inspect")
                        .requires(source -> can(source, "sef:alias.inspect"))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests((context, builder) -> suggestAliasIds(builder, true))
                                .executes(context -> execute(
                                        context.getSource(),
                                        "sef:alias.inspect",
                                        Map.of("id", StringArgumentType.getString(context, "id")),
                                        () -> inspectAlias(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "id"))))))
                .then(Commands.literal("create")
                        .requires(source -> can(source, "sef:alias.create"))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("root", StringArgumentType.word())
                                        .then(Commands.argument("kind", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                        new String[]{"action", "bundle"}, builder))
                                                .then(Commands.argument("target", StringArgumentType.word())
                                                        .then(Commands.argument(
                                                                        "schema",
                                                                        StringArgumentType.word())
                                                                .suggests((context, builder) ->
                                                                        SharedSuggestionProvider.suggest(
                                                                                java.util.Arrays.stream(
                                                                                                AliasCompiler.ArgumentSchema.values())
                                                                                        .map(value -> value.name()
                                                                                                .toLowerCase(Locale.ROOT)),
                                                                                builder))
                                                                .executes(context -> createAlias(
                                                                        context.getSource(),
                                                                        StringArgumentType.getString(context, "id"),
                                                                        StringArgumentType.getString(context, "root"),
                                                                        StringArgumentType.getString(context, "kind"),
                                                                        StringArgumentType.getString(context, "target"),
                                                                        StringArgumentType.getString(context, "schema"),
                                                                        ""))
                                                                .then(Commands.argument(
                                                                                "permission",
                                                                                StringArgumentType.word())
                                                                        .executes(context -> createAlias(
                                                                                context.getSource(),
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "id"),
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "root"),
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "kind"),
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "target"),
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "schema"),
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "permission"))))))))))
                .then(revisionAction(
                        "validate", "sef:alias.validate",
                        (source, id, revision) -> result(
                                source,
                                KernelServices.aliases().validateDraft(id, revision),
                                "Alias draft is valid.")))
                .then(revisionAction(
                        "publish", "sef:alias.publish",
                        (source, id, revision) -> result(
                                source,
                                KernelServices.aliases().publish(id, revision, actorId(source)),
                                "Alias published. restart or reload commands to activate a new root.")))
                .then(revisionAction(
                        "disable", "sef:alias.disable",
                        (source, id, revision) -> result(
                                source,
                                KernelServices.aliases().setEnabled(
                                        id, revision, false, actorId(source)),
                                "Alias disabled.")))
                .then(Commands.literal("rollback")
                        .requires(source -> can(source, "sef:alias.rollback"))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests((context, builder) -> suggestAliasIds(builder, false))
                                .then(Commands.argument("revision", LongArgumentType.longArg(1))
                                        .then(Commands.argument(
                                                        "historical_revision",
                                                        LongArgumentType.longArg(1))
                                                .executes(context -> execute(
                                                        context.getSource(),
                                                        "sef:alias.rollback",
                                                        Map.of("id", StringArgumentType.getString(
                                                                context,
                                                                "id")),
                                                        () -> result(
                                                                context.getSource(),
                                                                KernelServices.aliases().rollback(
                                                                        StringArgumentType.getString(context, "id"),
                                                                        LongArgumentType.getLong(context, "revision"),
                                                                        LongArgumentType.getLong(
                                                                                context,
                                                                                "historical_revision"),
                                                                        actorId(context.getSource())),
                                                                "Alias rolled back.")))))))
                .then(revisionAction(
                        "delete", "sef:alias.delete",
                        (source, id, revision) -> result(
                                source,
                                KernelServices.aliases().delete(id, revision),
                                "Alias deleted.")))
                .then(Commands.literal("run")
                        .requires(source -> can(source, "sef:alias.run"))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests((context, builder) -> suggestAliasIds(builder, false))
                                .executes(context -> execute(
                                        context.getSource(),
                                        "sef:alias.run",
                                        Map.of("id", StringArgumentType.getString(context, "id")),
                                        () -> runAliasById(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "id"))))))
                .then(Commands.literal("help")
                        .requires(source -> can(source, "sef:alias.help"))
                        .executes(context -> execute(
                                context.getSource(), "sef:alias.help", Map.of(),
                                () -> aliasHelp(context.getSource()))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> bundleRoot() {
        return Commands.literal("bundle")
                .then(Commands.literal("list")
                        .requires(source -> can(source, "sef:bundle.list"))
                        .executes(context -> execute(
                                context.getSource(), "sef:bundle.list", Map.of(),
                                () -> listBundles(context.getSource()))))
                .then(Commands.literal("inspect")
                        .requires(source -> can(source, "sef:bundle.inspect"))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests((context, builder) -> suggestBundleIds(builder, true))
                                .executes(context -> execute(
                                        context.getSource(),
                                        "sef:bundle.inspect",
                                        Map.of("id", StringArgumentType.getString(context, "id")),
                                        () -> inspectBundle(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "id"))))))
                .then(Commands.literal("create")
                        .requires(source -> can(source, "sef:bundle.create"))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(context -> execute(
                                        context.getSource(),
                                        "sef:bundle.create",
                                        Map.of("id", StringArgumentType.getString(context, "id")),
                                        () -> result(
                                                context.getSource(),
                                                KernelServices.bundles().createDraft(
                                                        StringArgumentType.getString(context, "id"),
                                                        actorId(context.getSource())),
                                                "Bundle draft created.")))))
                .then(bundleEditRoot())
                .then(Commands.literal("preview")
                        .requires(source -> can(source, "sef:bundle.preview"))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests((context, builder) -> suggestBundleIds(builder, true))
                                .executes(context -> previewBundle(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "id"),
                                        1))
                                .then(Commands.argument(
                                                "targets",
                                                IntegerArgumentType.integer(1, 1000))
                                        .executes(context -> previewBundle(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "id"),
                                                IntegerArgumentType.getInteger(context, "targets"))))))
                .then(bundleRevisionAction(
                        "publish", "sef:bundle.publish",
                        (source, id, revision) -> result(
                                source,
                                KernelServices.bundles().publish(id, revision),
                                "Bundle published.")))
                .then(bundleRunRoot())
                .then(Commands.literal("cancel")
                        .requires(source -> can(source, "sef:bundle.cancel"))
                        .then(Commands.argument("job", UuidArgument.uuid())
                                .executes(context -> execute(
                                        context.getSource(),
                                        "sef:bundle.cancel",
                                        Map.of("job", UuidArgument.getUuid(context, "job").toString()),
                                        () -> result(
                                                context.getSource(),
                                                KernelServices.bundles().cancel(
                                                        UuidArgument.getUuid(context, "job"),
                                                        actorId(context.getSource()),
                                                        PermissionService.isConsole(context.getSource())),
                                                "Bundle job cancelled.")))))
                .then(Commands.literal("recover")
                        .requires(source -> can(source, "sef:bundle.recover"))
                        .then(Commands.argument("job", UuidArgument.uuid())
                                .executes(context -> execute(
                                        context.getSource(),
                                        "sef:bundle.recover",
                                        Map.of("job", UuidArgument.getUuid(context, "job").toString()),
                                        () -> result(
                                                context.getSource(),
                                                KernelServices.bundles().recover(
                                                        UuidArgument.getUuid(context, "job")),
                                                "Bundle job recovered.")))))
                .then(bundleRevisionAction(
                        "disable", "sef:bundle.disable",
                        (source, id, revision) -> result(
                                source,
                                KernelServices.bundles().setEnabled(id, revision, false),
                                "Bundle disabled.")))
                .then(Commands.literal("rollback")
                        .requires(source -> can(source, "sef:bundle.rollback"))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests((context, builder) -> suggestBundleIds(builder, false))
                                .then(Commands.argument("revision", LongArgumentType.longArg(1))
                                        .then(Commands.argument(
                                                        "historical_revision",
                                                        LongArgumentType.longArg(1))
                                                .executes(context -> execute(
                                                        context.getSource(),
                                                        "sef:bundle.rollback",
                                                        Map.of("id", StringArgumentType.getString(
                                                                context,
                                                                "id")),
                                                        () -> result(
                                                                context.getSource(),
                                                                KernelServices.bundles().rollback(
                                                                        StringArgumentType.getString(context, "id"),
                                                                        LongArgumentType.getLong(context, "revision"),
                                                                        LongArgumentType.getLong(
                                                                                context,
                                                                                "historical_revision")),
                                                                "Bundle rolled back.")))))))
                .then(bundleRevisionAction(
                        "delete", "sef:bundle.delete",
                        (source, id, revision) -> result(
                                source,
                                KernelServices.bundles().delete(id, revision),
                                "Bundle deleted.")));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> bundleEditRoot() {
        return Commands.literal("edit")
                .requires(source -> can(source, "sef:bundle.edit"))
                .then(Commands.literal("remove")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("revision", LongArgumentType.longArg(1))
                                        .then(Commands.argument("step", StringArgumentType.word())
                                                .executes(context -> execute(
                                                        context.getSource(),
                                                        "sef:bundle.edit",
                                                        Map.of(
                                                                "id",
                                                                StringArgumentType.getString(context, "id"),
                                                                "operation",
                                                                "remove_step"),
                                                        () -> result(
                                                                context.getSource(),
                                                                KernelServices.bundles().removeStep(
                                                                        StringArgumentType.getString(context, "id"),
                                                                        LongArgumentType.getLong(
                                                                                context,
                                                                                "revision"),
                                                                        StringArgumentType.getString(
                                                                                context,
                                                                                "step")),
                                                                "Bundle step removed.")))))))
                .then(Commands.literal("add")
                        .then(bundleAddAction())
                        .then(bundleAddBundle())
                        .then(bundleAddNotice())
                        .then(bundleAddDelay())
                        .then(bundleAddProfile()));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> bundleAddAction() {
        return Commands.literal("action")
                .then(Commands.argument("id", StringArgumentType.word())
                        .then(Commands.argument("revision", LongArgumentType.longArg(1))
                                .then(Commands.argument("step", StringArgumentType.word())
                                        .then(Commands.argument("action", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                        KernelServices.catalog().entries().stream()
                                                                .map(CommandDefinition::id),
                                                        builder))
                                                .executes(context -> addBundleStep(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "id"),
                                                        LongArgumentType.getLong(context, "revision"),
                                                        StringArgumentType.getString(context, "step"),
                                                        BundleCompiler.StepKind.SEF_ACTION,
                                                        StringArgumentType.getString(context, "action"),
                                                        Duration.ZERO,
                                                        Map.of()))
                                                .then(Commands.argument(
                                                                "arguments",
                                                                StringArgumentType.greedyString())
                                                        .executes(context -> addBundleStep(
                                                                context.getSource(),
                                                                StringArgumentType.getString(context, "id"),
                                                                LongArgumentType.getLong(context, "revision"),
                                                                StringArgumentType.getString(context, "step"),
                                                                BundleCompiler.StepKind.SEF_ACTION,
                                                                StringArgumentType.getString(context, "action"),
                                                                Duration.ZERO,
                                                                Map.of(
                                                                        "arguments",
                                                                        boundedDefinitionText(
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "arguments"),
                                                                                512)))))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> bundleAddBundle() {
        return Commands.literal("bundle")
                .then(Commands.argument("id", StringArgumentType.word())
                        .then(Commands.argument("revision", LongArgumentType.longArg(1))
                                .then(Commands.argument("step", StringArgumentType.word())
                                        .then(Commands.argument("target", StringArgumentType.word())
                                                .suggests((context, builder) -> suggestBundleIds(builder, false))
                                                .executes(context -> addBundleStep(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "id"),
                                                        LongArgumentType.getLong(context, "revision"),
                                                        StringArgumentType.getString(context, "step"),
                                                        BundleCompiler.StepKind.BUNDLE,
                                                        StringArgumentType.getString(context, "target"),
                                                        Duration.ZERO,
                                                        Map.of()))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> bundleAddNotice() {
        return Commands.literal("notice")
                .then(Commands.argument("id", StringArgumentType.word())
                        .then(Commands.argument("revision", LongArgumentType.longArg(1))
                                .then(Commands.argument("step", StringArgumentType.word())
                                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                                .executes(context -> addBundleStep(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "id"),
                                                        LongArgumentType.getLong(context, "revision"),
                                                        StringArgumentType.getString(context, "step"),
                                                        BundleCompiler.StepKind.NOTICE,
                                                        "notice",
                                                        Duration.ZERO,
                                                        Map.of(
                                                                "message",
                                                                boundedDefinitionText(
                                                                        StringArgumentType.getString(
                                                                                context,
                                                                                "message"),
                                                                        512))))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> bundleAddDelay() {
        return Commands.literal("delay")
                .then(Commands.argument("id", StringArgumentType.word())
                        .then(Commands.argument("revision", LongArgumentType.longArg(1))
                                .then(Commands.argument("step", StringArgumentType.word())
                                        .then(Commands.argument(
                                                        "ticks",
                                                        IntegerArgumentType.integer(1, 72_000))
                                                .executes(context -> addBundleStep(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "id"),
                                                        LongArgumentType.getLong(context, "revision"),
                                                        StringArgumentType.getString(context, "step"),
                                                        BundleCompiler.StepKind.DELAY,
                                                        "delay",
                                                        Duration.ofMillis(
                                                                IntegerArgumentType.getInteger(
                                                                        context,
                                                                        "ticks") * 50L),
                                                        Map.of()))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> bundleAddProfile() {
        return Commands.literal("profile")
                .then(Commands.argument("id", StringArgumentType.word())
                        .then(Commands.argument("revision", LongArgumentType.longArg(1))
                                .then(Commands.argument("step", StringArgumentType.word())
                                        .then(Commands.argument("profile", StringArgumentType.word())
                                                .suggests((context, builder) -> suggestProfileIds(builder, false))
                                                .executes(context -> addBundleStep(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "id"),
                                                        LongArgumentType.getLong(context, "revision"),
                                                        StringArgumentType.getString(context, "step"),
                                                        BundleCompiler.StepKind.EXTERNAL_ACTOR_COMMAND,
                                                        StringArgumentType.getString(context, "profile"),
                                                        Duration.ZERO,
                                                        Map.of()))
                                                .then(Commands.argument(
                                                                "values",
                                                                StringArgumentType.greedyString())
                                                        .executes(context -> addBundleStep(
                                                                context.getSource(),
                                                                StringArgumentType.getString(context, "id"),
                                                                LongArgumentType.getLong(context, "revision"),
                                                                StringArgumentType.getString(context, "step"),
                                                                BundleCompiler.StepKind.EXTERNAL_ACTOR_COMMAND,
                                                                StringArgumentType.getString(context, "profile"),
                                                                Duration.ZERO,
                                                                parseBindings(StringArgumentType.getString(
                                                                        context,
                                                                        "values")))))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> bundleRunRoot() {
        return Commands.literal("run")
                .requires(source -> can(source, "sef:bundle.run"))
                .then(Commands.literal("confirm")
                        .then(Commands.argument("token", StringArgumentType.word())
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .then(Commands.argument("revision", LongArgumentType.longArg(1))
                                                .executes(context -> confirmBundle(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "token"),
                                                        StringArgumentType.getString(context, "id"),
                                                        LongArgumentType.getLong(context, "revision"),
                                                        selfTargets(context.getSource())))
                                                .then(Commands.argument("players", EntityArgument.players())
                                                        .executes(context -> confirmBundle(
                                                                context.getSource(),
                                                                StringArgumentType.getString(context, "token"),
                                                                StringArgumentType.getString(context, "id"),
                                                                LongArgumentType.getLong(context, "revision"),
                                                                EntityArgument.getPlayers(
                                                                        context,
                                                                        "players"))))))))
                .then(Commands.argument("id", StringArgumentType.word())
                        .suggests((context, builder) -> suggestBundleIds(builder, false))
                        .then(Commands.argument("revision", LongArgumentType.longArg(1))
                                .executes(context -> requestBundle(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "id"),
                                        LongArgumentType.getLong(context, "revision"),
                                        selfTargets(context.getSource())))
                                .then(Commands.argument("players", EntityArgument.players())
                                        .executes(context -> requestBundle(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "id"),
                                                LongArgumentType.getLong(context, "revision"),
                                                EntityArgument.getPlayers(context, "players"))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> profileRoot() {
        return Commands.literal("profile")
                .then(Commands.literal("list")
                        .requires(source -> can(source, "sef:profile.list"))
                        .executes(context -> execute(
                                context.getSource(), "sef:profile.list", Map.of(),
                                () -> listProfiles(context.getSource()))))
                .then(Commands.literal("inspect")
                        .requires(source -> can(source, "sef:profile.inspect"))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests((context, builder) -> suggestProfileIds(builder, true))
                                .executes(context -> execute(
                                        context.getSource(),
                                        "sef:profile.inspect",
                                        Map.of("id", StringArgumentType.getString(context, "id")),
                                        () -> inspectProfile(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "id"))))))
                .then(Commands.literal("create")
                        .requires(source -> can(source, "sef:profile.create"))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("context", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                java.util.Arrays.stream(CommandProfileService.Context.values())
                                                        .map(value -> value.name().toLowerCase(Locale.ROOT)),
                                                builder))
                                        .then(Commands.argument(
                                                        "maximum_targets",
                                                        IntegerArgumentType.integer(1, 1000))
                                                .then(Commands.argument(
                                                                "arguments",
                                                                StringArgumentType.word())
                                                        .then(Commands.argument(
                                                                        "template",
                                                                        StringArgumentType.greedyString())
                                                                .executes(context -> createProfile(
                                                                        context.getSource(),
                                                                        StringArgumentType.getString(context, "id"),
                                                                        StringArgumentType.getString(
                                                                                context,
                                                                                "context"),
                                                                        IntegerArgumentType.getInteger(
                                                                                context,
                                                                                "maximum_targets"),
                                                                        StringArgumentType.getString(
                                                                                context,
                                                                                "arguments"),
                                                                        StringArgumentType.getString(
                                                                                context,
                                                                                "template")))))))))
                .then(profileRevisionAction(
                        "validate", "sef:profile.validate",
                        (source, id, revision) -> validateProfile(source, id, revision)))
                .then(Commands.literal("test")
                        .requires(source -> can(source, "sef:profile.test"))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("revision", LongArgumentType.longArg(1))
                                        .then(Commands.argument("values", StringArgumentType.greedyString())
                                                .executes(context -> testProfile(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "id"),
                                                        LongArgumentType.getLong(context, "revision"),
                                                        StringArgumentType.getString(
                                                                context,
                                                                "values")))))))
                .then(profileRevisionAction(
                        "publish", "sef:profile.publish",
                        (source, id, revision) -> result(
                                source,
                                KernelServices.commandProfiles().publish(
                                        id, revision, actorId(source)),
                                "Command profile published.")))
                .then(Commands.literal("reference")
                        .requires(source -> can(source, "sef:profile.reference"))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests((context, builder) -> suggestProfileIds(builder, false))
                                .executes(context -> execute(
                                        context.getSource(),
                                        "sef:profile.reference",
                                        Map.of("id", StringArgumentType.getString(context, "id")),
                                        () -> profileReferences(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "id"))))))
                .then(Commands.literal("enable")
                        .requires(source -> can(source, "sef:profile.enable"))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("revision", LongArgumentType.longArg(1))
                                        .then(Commands.argument("state", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                        new String[]{"on", "off"}, builder))
                                                .executes(context -> enableProfile(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "id"),
                                                        LongArgumentType.getLong(context, "revision"),
                                                        StringArgumentType.getString(
                                                                context,
                                                                "state")))))))
                .then(Commands.literal("execute")
                        .requires(source -> can(source, "sef:profile.execute"))
                        .then(Commands.literal("targeted")
                                .requires(source -> has(source, "commands.profile.targeted"))
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((context, builder) -> suggestProfileIds(builder, false))
                                        .then(Commands.argument("revision", LongArgumentType.longArg(1))
                                                .then(IdentityArguments.online("player")
                                                        .executes(context -> executeProfile(
                                                                context.getSource(),
                                                                StringArgumentType.getString(context, "id"),
                                                                LongArgumentType.getLong(context, "revision"),
                                                                IdentityArguments.getOnline(context, "player"),
                                                                Map.of()))
                                                        .then(Commands.argument(
                                                                        "values",
                                                                        StringArgumentType.greedyString())
                                                                .executes(context -> executeProfile(
                                                                        context.getSource(),
                                                                        StringArgumentType.getString(
                                                                                context,
                                                                                "id"),
                                                                        LongArgumentType.getLong(
                                                                                context,
                                                                                "revision"),
                                                                        IdentityArguments.getOnline(
                                                                                context,
                                                                                "player"),
                                                                        parseBindings(
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "values")))))))))
                        .then(Commands.literal("confirm")
                                .then(Commands.literal("server")
                                        .requires(source -> has(source, "commands.profile.server"))
                                        .then(Commands.argument("token", StringArgumentType.word())
                                                .then(Commands.argument("id", StringArgumentType.word())
                                                        .then(Commands.argument(
                                                                        "revision",
                                                                        LongArgumentType.longArg(1))
                                                                .executes(context -> confirmProfile(
                                                                        context.getSource(),
                                                                        StringArgumentType.getString(
                                                                                context,
                                                                                "token"),
                                                                        StringArgumentType.getString(
                                                                                context,
                                                                                "id"),
                                                                        LongArgumentType.getLong(
                                                                                context,
                                                                                "revision"),
                                                                        null,
                                                                        Map.of()))
                                                                .then(Commands.argument(
                                                                                "values",
                                                                                StringArgumentType.greedyString())
                                                                        .executes(context -> confirmProfile(
                                                                                context.getSource(),
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "token"),
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "id"),
                                                                                LongArgumentType.getLong(
                                                                                        context,
                                                                                        "revision"),
                                                                                null,
                                                                                parseBindings(
                                                                                        StringArgumentType.getString(
                                                                                                context,
                                                                                                "values")))))))))
                                .then(Commands.literal("targeted")
                                        .requires(source -> has(source, "commands.profile.targeted"))
                                        .then(Commands.argument("token", StringArgumentType.word())
                                                .then(Commands.argument("id", StringArgumentType.word())
                                                        .then(Commands.argument(
                                                                        "revision",
                                                                        LongArgumentType.longArg(1))
                                                                .then(IdentityArguments.online("player")
                                                                        .executes(context -> confirmProfile(
                                                                                context.getSource(),
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "token"),
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "id"),
                                                                                LongArgumentType.getLong(
                                                                                        context,
                                                                                        "revision"),
                                                                                IdentityArguments.getOnline(
                                                                                        context,
                                                                                        "player"),
                                                                                Map.of()))
                                                                        .then(Commands.argument(
                                                                                        "values",
                                                                                        StringArgumentType.greedyString())
                                                                                .executes(context -> confirmProfile(
                                                                                        context.getSource(),
                                                                                        StringArgumentType.getString(
                                                                                                context,
                                                                                                "token"),
                                                                                        StringArgumentType.getString(
                                                                                                context,
                                                                                                "id"),
                                                                                        LongArgumentType.getLong(
                                                                                                context,
                                                                                                "revision"),
                                                                                        IdentityArguments.getOnline(
                                                                                                context,
                                                                                                "player"),
                                                                                        parseBindings(
                                                                                                StringArgumentType.getString(
                                                                                                        context,
                                                                                                        "values")))))))))))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests((context, builder) -> suggestProfileIds(builder, false))
                                .then(Commands.argument("revision", LongArgumentType.longArg(1))
                                        .executes(context -> executeProfile(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "id"),
                                                LongArgumentType.getLong(context, "revision"),
                                                null,
                                                Map.of()))
                                        .then(Commands.argument("values", StringArgumentType.greedyString())
                                                .executes(context -> executeProfile(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "id"),
                                                        LongArgumentType.getLong(context, "revision"),
                                                        null,
                                                        parseBindings(StringArgumentType.getString(
                                                                context,
                                                                "values"))))))))
                .then(Commands.literal("rollback")
                        .requires(source -> can(source, "sef:profile.rollback"))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("revision", LongArgumentType.longArg(1))
                                        .then(Commands.argument(
                                                        "historical_revision",
                                                        LongArgumentType.longArg(1))
                                                .executes(context -> execute(
                                                        context.getSource(),
                                                        "sef:profile.rollback",
                                                        Map.of("id", StringArgumentType.getString(
                                                                context,
                                                                "id")),
                                                        () -> result(
                                                                context.getSource(),
                                                                KernelServices.commandProfiles().rollback(
                                                                        StringArgumentType.getString(context, "id"),
                                                                        LongArgumentType.getLong(context, "revision"),
                                                                        LongArgumentType.getLong(
                                                                                context,
                                                                                "historical_revision"),
                                                                        actorId(context.getSource())),
                                                                "Command profile rolled back.")))))))
                .then(profileRevisionAction(
                        "delete", "sef:profile.delete",
                        (source, id, revision) -> result(
                                source,
                                KernelServices.commandProfiles().delete(id, revision),
                                "Command profile deleted.")));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> fakeRoot() {
        return Commands.literal("fake")
                .then(Commands.literal("profile")
                        .requires(source -> can(source, "sef:fake.profile"))
                        .then(Commands.literal("list")
                                .executes(context -> execute(
                                        context.getSource(),
                                        "sef:fake.profile",
                                        Map.of("operation", "list"),
                                        () -> listFakeProfiles(context.getSource()))))
                        .then(Commands.literal("create")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .then(Commands.argument("username", StringArgumentType.word())
                                                .then(Commands.argument(
                                                                "nickname",
                                                                StringArgumentType.string())
                                                        .then(Commands.argument(
                                                                        "prefix",
                                                                        StringArgumentType.string())
                                                                .then(Commands.argument(
                                                                                "suffix",
                                                                                StringArgumentType.string())
                                                                        .executes(context -> fakeProfileCreate(
                                                                                context.getSource(),
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "id"),
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "username"),
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "nickname"),
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "prefix"),
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "suffix")))))))))
                        .then(Commands.literal("publish")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .then(Commands.argument("revision", LongArgumentType.longArg(1))
                                                .executes(context -> execute(
                                                        context.getSource(),
                                                        "sef:fake.profile",
                                                        Map.of("operation", "publish"),
                                                        () -> result(
                                                                context.getSource(),
                                                                KernelServices.fakeIdentities().publish(
                                                                        StringArgumentType.getString(context, "id"),
                                                                        LongArgumentType.getLong(context, "revision"),
                                                                        actorId(context.getSource())),
                                                                "Fake profile published."))))))
                        .then(Commands.literal("rollback")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .then(Commands.argument("revision", LongArgumentType.longArg(1))
                                                .then(Commands.argument(
                                                                "historical_revision",
                                                                LongArgumentType.longArg(1))
                                                        .executes(context -> execute(
                                                                context.getSource(),
                                                                "sef:fake.profile",
                                                                Map.of("operation", "rollback"),
                                                                () -> result(
                                                                        context.getSource(),
                                                                        KernelServices.fakeIdentities().rollback(
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "id"),
                                                                                LongArgumentType.getLong(
                                                                                        context,
                                                                                        "revision"),
                                                                                LongArgumentType.getLong(
                                                                                        context,
                                                                                        "historical_revision"),
                                                                                actorId(context.getSource())),
                                                                        "Fake profile rolled back."))))))))
                .then(Commands.literal("scene")
                        .requires(source -> can(source, "sef:fake.scene"))
                        .then(Commands.literal("list")
                                .executes(context -> execute(
                                        context.getSource(),
                                        "sef:fake.scene",
                                        Map.of("operation", "list"),
                                        () -> listFakeScenes(context.getSource()))))
                        .then(Commands.literal("save")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .then(Commands.argument("revision", LongArgumentType.longArg(0))
                                                .then(Commands.argument("audience", StringArgumentType.word())
                                                        .suggests((context, builder) ->
                                                                SharedSuggestionProvider.suggest(
                                                                        java.util.Arrays.stream(
                                                                                        FakeIdentityService.Audience.values())
                                                                                .map(value -> value.name()
                                                                                        .toLowerCase(Locale.ROOT)),
                                                                        builder))
                                                        .then(Commands.argument(
                                                                        "type",
                                                                        StringArgumentType.word())
                                                                .suggests((context, builder) ->
                                                                        SharedSuggestionProvider.suggest(
                                                                                java.util.Arrays.stream(
                                                                                                FakeIdentityService.EventType.values())
                                                                                        .map(value -> value.name()
                                                                                                .toLowerCase(Locale.ROOT)),
                                                                                builder))
                                                                .then(Commands.argument(
                                                                                "identity",
                                                                                StringArgumentType.word())
                                                                        .then(Commands.argument(
                                                                                        "message",
                                                                                        StringArgumentType.greedyString())
                                                                                .executes(context ->
                                                                                        saveFakeScene(
                                                                                                context.getSource(),
                                                                                                StringArgumentType.getString(
                                                                                                        context,
                                                                                                        "id"),
                                                                                                LongArgumentType.getLong(
                                                                                                        context,
                                                                                                        "revision"),
                                                                                                StringArgumentType.getString(
                                                                                                        context,
                                                                                                        "audience"),
                                                                                                StringArgumentType.getString(
                                                                                                        context,
                                                                                                        "type"),
                                                                                                StringArgumentType.getString(
                                                                                                        context,
                                                                                                        "identity"),
                                                                                                StringArgumentType.getString(
                                                                                                        context,
                                                                                                        "message"))))))))))
                        .then(Commands.literal("preview")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(context -> previewFakeScene(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "id"))))))
                .then(Commands.literal("schedule")
                        .requires(source -> can(source, "sef:fake.schedule"))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("revision", LongArgumentType.longArg(1))
                                        .then(Commands.argument(
                                                        "delay_seconds",
                                                        IntegerArgumentType.integer(0, 31_536_000))
                                                .executes(context -> scheduleFakeScene(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "id"),
                                                        LongArgumentType.getLong(context, "revision"),
                                                        IntegerArgumentType.getInteger(
                                                                context,
                                                                "delay_seconds")))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> fakeJoinRoot(
            String literal,
            FakeIdentityService.EventType type
    ) {
        String action = type == FakeIdentityService.EventType.JOIN
                ? "sef:fake.join"
                : "sef:fake.leave";
        return Commands.literal(literal)
                .requires(source -> can(source, action))
                .then(Commands.argument("identity", StringArgumentType.word())
                        .executes(context -> fakeConnection(
                                context.getSource(),
                                StringArgumentType.getString(context, "identity"),
                                type)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> fakeMessageRoot() {
        return Commands.literal("fakemessage")
                .requires(source -> can(source, "sef:fake.message"))
                .then(Commands.argument("identity", StringArgumentType.word())
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(context -> fakeMessage(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "identity"),
                                        StringArgumentType.getString(context, "message")))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> fakeRankMessageRoot() {
        return Commands.literal("fakerankmessage")
                .requires(source -> can(source, "sef:fake.rank_message"))
                .then(Commands.argument("prefix", StringArgumentType.string())
                        .then(Commands.argument("suffix", StringArgumentType.string())
                                .then(Commands.argument("username", StringArgumentType.word())
                                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                                .executes(context -> fakeRankMessage(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "prefix"),
                                                        StringArgumentType.getString(context, "suffix"),
                                                        StringArgumentType.getString(context, "username"),
                                                        StringArgumentType.getString(
                                                                context,
                                                                "message")))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> sudoRoot() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("sudo")
                .requires(source -> canAny(
                        source,
                        "sef:sudo.run",
                        "sef:sudo.chat",
                        "sef:sudo.dryrun",
                        "sef:sudo.consent",
                        "sef:sudo.lock",
                        "sef:sudo.policy"));
        root.then(Commands.literal("run")
                .requires(source -> can(source, "sef:sudo.run"))
                .then(sudoPlayerArgument("player")
                        .then(sudoRunMode("respect", false, false))
                        .then(sudoRunMode("delegate", true, false))
                        .then(Commands.argument("command", StringArgumentType.greedyString())
                                .suggests((context, builder) ->
                                        suggestSudoCommand(context, builder, false))
                                .executes(context -> requestSudo(
                                        context.getSource(),
                                        IdentityArguments.getOnline(context, "player"),
                                        StringArgumentType.getString(context, "command"),
                                        false,
                                        false)))));
        root.then(Commands.literal("confirm")
                .requires(source -> can(source, "sef:sudo.run"))
                .then(Commands.argument("token", StringArgumentType.word())
                        .then(sudoPlayerArgument("player")
                                .then(sudoConfirmMode("respect", false))
                                .then(sudoConfirmMode("delegate", true))
                                .then(sudoConfirmMode("false", false))
                                .then(sudoConfirmMode("true", true))
                                .then(Commands.argument("command", StringArgumentType.greedyString())
                                        .suggests((context, builder) ->
                                                suggestSudoCommand(context, builder, false))
                                        .executes(context -> confirmSudo(
                                                context.getSource(),
                                                        StringArgumentType.getString(context, "token"),
                                                        IdentityArguments.getOnline(context, "player"),
                                                        "respect",
                                                        StringArgumentType.getString(context, "command")))))));
        root.then(Commands.literal("dryrun")
                .requires(source -> can(source, "sef:sudo.dryrun"))
                .then(sudoPlayerArgument("player")
                        .then(Commands.argument("command", StringArgumentType.greedyString())
                                .suggests((context, builder) ->
                                        suggestSudoCommand(context, builder, false))
                                .executes(context -> sudoDryRun(
                                        context.getSource(),
                                        IdentityArguments.getOnline(context, "player"),
                                        StringArgumentType.getString(context, "command"),
                                        false)))));
        root.then(Commands.literal("preview")
                .requires(source -> can(source, "sef:sudo.dryrun"))
                .then(sudoPlayerArgument("player")
                        .then(sudoPreviewMode("respect", false))
                        .then(sudoPreviewMode("delegate", true))
                        .then(Commands.argument("command", StringArgumentType.greedyString())
                                .suggests((context, builder) ->
                                        suggestSudoCommand(context, builder, false))
                                .executes(context -> sudoDryRun(
                                        context.getSource(),
                                        IdentityArguments.getOnline(context, "player"),
                                        StringArgumentType.getString(context, "command"),
                                        false)))));
        root.then(Commands.literal("chat")
                .requires(source -> can(source, "sef:sudo.chat"))
                .then(sudoPlayerArgument("player")
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(context -> sudoChat(
                                        context.getSource(),
                                        IdentityArguments.getOnline(context, "player"),
                                        StringArgumentType.getString(context, "message"))))));
        root.then(Commands.literal("consent")
                .requires(source -> can(source, "sef:sudo.consent"))
                .then(Commands.argument("state", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                new String[]{"on", "off"}, builder))
                        .executes(context -> sudoConsent(
                                context.getSource(),
                                parseState(StringArgumentType.getString(context, "state"))))));
        root.then(Commands.literal("lock")
                .requires(source -> can(source, "sef:sudo.lock"))
                .then(sudoPlayerArgument("player")
                        .then(Commands.argument("state", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        new String[]{"on", "off"}, builder))
                                .executes(context -> sudoLock(
                                        context.getSource(),
                                        IdentityArguments.getOnline(context, "player"),
                                        parseState(StringArgumentType.getString(context, "state")),
                                        ""))
                                .then(Commands.argument("reason", StringArgumentType.greedyString())
                                        .executes(context -> sudoLock(
                                                context.getSource(),
                                                IdentityArguments.getOnline(context, "player"),
                                                parseState(StringArgumentType.getString(
                                                        context,
                                                        "state")),
                                                StringArgumentType.getString(
                                                        context,
                                                        "reason")))))));
        root.then(Commands.literal("policy")
                .requires(source -> can(source, "sef:sudo.policy"))
                .executes(context -> sudoPolicy(context.getSource(), null))
                .then(sudoPlayerArgument("player")
                        .executes(context -> sudoPolicy(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "player")))));
        root.then(sudoPlayerArgument("player")
                .requires(source -> can(source, "sef:sudo.run"))
                .then(sudoRunMode("false", false, true))
                .then(sudoRunMode("true", true, true))
                .then(Commands.argument("command", StringArgumentType.greedyString())
                        .suggests((context, builder) ->
                                suggestSudoCommand(context, builder, false))
                        .executes(context -> requestSudo(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "player"),
                                StringArgumentType.getString(context, "command"),
                                false,
                                true))));
        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> sudoRunMode(
            String mode,
            boolean delegated,
            boolean compatibilitySyntax
    ) {
        return Commands.literal(mode)
                .then(Commands.argument("command", StringArgumentType.greedyString())
                        .suggests((context, builder) ->
                                suggestSudoCommand(context, builder, delegated))
                        .executes(context -> requestSudo(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "player"),
                                StringArgumentType.getString(context, "command"),
                                delegated,
                                compatibilitySyntax)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> sudoConfirmMode(
            String mode,
            boolean delegated
    ) {
        return Commands.literal(mode)
                .then(Commands.argument("command", StringArgumentType.greedyString())
                        .suggests((context, builder) ->
                                suggestSudoCommand(context, builder, delegated))
                        .executes(context -> confirmSudo(
                                context.getSource(),
                                StringArgumentType.getString(context, "token"),
                                IdentityArguments.getOnline(context, "player"),
                                mode,
                                StringArgumentType.getString(context, "command"))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> sudoPreviewMode(
            String mode,
            boolean delegated
    ) {
        return Commands.literal(mode)
                .then(Commands.argument("command", StringArgumentType.greedyString())
                        .suggests((context, builder) ->
                                suggestSudoCommand(context, builder, delegated))
                        .executes(context -> sudoDryRun(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "player"),
                                StringArgumentType.getString(context, "command"),
                                delegated)));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> sudoPlayerArgument(String name) {
        return Commands.argument(name, StringArgumentType.string())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                        context.getSource().getServer().getPlayerList().getPlayers().stream()
                                .filter(target -> eligibleTarget(
                                        context.getSource(),
                                        target,
                                        "sudo.exempt",
                                        "sudo.bypass.exempt",
                                        "sudo.hierarchy.bypass"))
                                .map(target -> StringArgumentType.escapeIfRequired(
                                        target.getGameProfile().getName())),
                        builder));
    }

    private static CompletableFuture<Suggestions> suggestSudoCommand(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder,
            boolean delegated
    ) {
        ServerPlayer target;
        try {
            target = IdentityArguments.getOnline(context, "player");
        } catch (Exception exception) {
            return builder.buildFuture();
        }
        if (!eligibleTarget(
                context.getSource(),
                target,
                "sudo.exempt",
                "sudo.bypass.exempt",
                "sudo.hierarchy.bypass")
                || delegated && !eligibleTarget(
                context.getSource(),
                target,
                "sudo.delegate.exempt",
                "sudo.bypass.exempt",
                "sudo.hierarchy.bypass")) {
            return builder.buildFuture();
        }
        int offset = builder.getStart();
        return KernelServices.administrativeExecution()
                .suggest(context.getSource(), target, builder.getRemaining(), delegated)
                .thenApply(suggestions -> {
                    SuggestionsBuilder shifted =
                            builder.createOffset(offset + suggestions.getRange().getStart());
                    suggestions.getList().forEach(suggestion ->
                            shifted.suggest(suggestion.getText(), suggestion.getTooltip()));
                    return shifted.build();
                })
                .exceptionally(exception -> builder.build());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> runRoot() {
        return Commands.literal("run")
                .requires(source -> can(source, "sef:run.server"))
                .then(Commands.literal("confirm")
                        .then(Commands.argument("token", StringArgumentType.word())
                                .then(Commands.argument("command", StringArgumentType.greedyString())
                                        .executes(context -> confirmRun(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "token"),
                                                StringArgumentType.getString(context, "command"))))))
                .then(Commands.argument("command", StringArgumentType.greedyString())
                        .executes(context -> requestRun(
                                context.getSource(),
                                StringArgumentType.getString(context, "command"))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> silentRoot() {
        return Commands.literal("silent")
                .requires(source -> can(source, "sef:silent.actor")
                        || can(source, "sef:silent.server"))
                .then(Commands.literal("server")
                        .requires(source -> can(source, "sef:silent.server"))
                        .then(Commands.literal("confirm")
                                .then(Commands.argument("token", StringArgumentType.word())
                                        .then(Commands.argument(
                                                        "command",
                                                        StringArgumentType.greedyString())
                                                .executes(context -> confirmSilentServer(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "token"),
                                                        StringArgumentType.getString(
                                                                context,
                                                                "command"))))))
                        .then(Commands.argument("command", StringArgumentType.greedyString())
                                .executes(context -> requestSilentServer(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "command")))))
                .then(Commands.argument("command", StringArgumentType.greedyString())
                        .requires(source -> can(source, "sef:silent.actor"))
                        .executes(context -> silentActor(
                                context.getSource(),
                                StringArgumentType.getString(context, "command"))));
    }

    private static int createAlias(
            CommandSourceStack source,
            String id,
            String root,
            String kindInput,
            String target,
            String schemaInput,
            String additionalPermission
    ) {
        AliasCompiler.AliasKind kind;
        AliasCompiler.ArgumentSchema schema;
        try {
            kind = AliasCompiler.AliasKind.valueOf(kindInput.toUpperCase(Locale.ROOT));
            schema = AliasCompiler.ArgumentSchema.valueOf(schemaInput.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return fail(source, "Alias kind or schema is invalid.");
        }
        if (kind != AliasCompiler.AliasKind.ACTION && kind != AliasCompiler.AliasKind.BUNDLE) {
            return fail(source, "Only action and bundle aliases can be created directly.");
        }
        CommandDefinition.AccessClass access = CommandDefinition.AccessClass.OWNER;
        AuditService.AuditClass auditClass = AuditService.AuditClass.ECONOMY_TRANSACTION;
        if (kind == AliasCompiler.AliasKind.ACTION) {
            CommandDefinition definition = KernelServices.catalog().find(target).orElse(null);
            if (definition == null) {
                return fail(source, "The target action was not found.");
            }
            access = definition.accessClass();
            auditClass = definition.auditClass();
        }
        String permission = additionalPermission == null ? "" : additionalPermission.toLowerCase(Locale.ROOT);
        CommandDefinition.AccessClass finalAccess = access;
        AuditService.AuditClass finalAuditClass = auditClass;
        return execute(source, "sef:alias.create", Map.of(
                "id", id,
                "root", root,
                "kind", kind.name().toLowerCase(Locale.ROOT),
                "target", target), () -> result(
                source,
                KernelServices.aliases().createDraft(
                        id,
                        root,
                        kind,
                        target,
                        schema,
                        permission,
                        finalAccess,
                        finalAuditClass,
                        actorId(source)),
                "Alias draft created."));
    }

    private static int listAliases(CommandSourceStack source) {
        List<AliasCompiler.AliasDefinition> all = new ArrayList<>(KernelServices.aliases().published());
        all.addAll(KernelServices.aliases().drafts());
        info(source, "Aliases. " + all.size() + " definition or definitions.");
        all.stream().limit(PAGE_SIZE).forEach(alias -> info(
                source,
                alias.id() + ", /" + alias.root() + ", " + alias.kind().name().toLowerCase(Locale.ROOT)
                        + ", revision " + alias.revision() + ", "
                        + alias.state().name().toLowerCase(Locale.ROOT)
                        + (alias.enabled() ? ", enabled." : ", disabled.")));
        return Math.max(1, all.size());
    }

    private static int inspectAlias(CommandSourceStack source, String id) {
        AliasCompiler.AliasDefinition alias = KernelServices.aliases().drafts().stream()
                .filter(candidate -> candidate.id().equals(id.toLowerCase(Locale.ROOT)))
                .findFirst()
                .orElseGet(() -> KernelServices.aliases().find(id).orElse(null));
        if (alias == null) {
            return fail(source, "Alias not found.");
        }
        info(source, alias.id() + ", revision " + alias.revision() + ", root /" + alias.root() + ".");
        info(source, "Target " + alias.targetId() + ", schema "
                + alias.argumentSchema().name().toLowerCase(Locale.ROOT) + ", access "
                + alias.accessClass().name().toLowerCase(Locale.ROOT) + ".");
        info(source, alias.additionalPermissionId().isBlank()
                ? "No additional permission."
                : "Additional permission " + alias.additionalPermissionId() + ".");
        return 1;
    }

    private static int aliasHelp(CommandSourceStack source) {
        info(source, "/sef alias create <id> <root> <action or bundle> <target> <schema> [permission]");
        info(source, "/sef alias validate, publish, disable, rollback, delete, inspect, list, run");
        info(source, "New alias roots activate after a command reload or restart.");
        return 1;
    }

    private static int runAliasById(CommandSourceStack source, String id) {
        AliasCompiler.AliasDefinition alias = KernelServices.aliases().find(id).orElse(null);
        if (alias == null || !alias.enabled() || !aliasAvailable(source, alias)) {
            return fail(source, "Alias is unavailable.");
        }
        if (alias.kind() == AliasCompiler.AliasKind.BUNDLE) {
            return runBundleAlias(source, alias);
        }
        if (alias.argumentSchema() != AliasCompiler.ArgumentSchema.NONE) {
            return fail(source, "This typed alias must be used through its published command root.");
        }
        CommandDefinition target = KernelServices.catalog().find(alias.targetId()).orElse(null);
        if (target == null) {
            return fail(source, "Alias target is unavailable.");
        }
        try {
            return source.getServer().getCommands().getDispatcher().execute(
                    target.canonicalRoute(),
                    source);
        } catch (Exception exception) {
            return fail(source, "Alias target failed to execute.");
        }
    }

    private static int runBundleAlias(CommandSourceStack source, AliasCompiler.AliasDefinition alias) {
        BundleCompiler.BundleDefinition bundle = KernelServices.bundles().find(alias.targetId()).orElse(null);
        if (bundle == null || !bundle.enabled()) {
            return fail(source, "Alias bundle is unavailable.");
        }
        Collection<ServerPlayer> targets = selfTargets(source);
        if (targets.isEmpty()) {
            return fail(source, "Bundle aliases require a player source.");
        }
        return requestBundle(source, bundle.id(), bundle.revision(), targets);
    }

    private static int listBundles(CommandSourceStack source) {
        List<BundleCompiler.BundleDefinition> all = new ArrayList<>(KernelServices.bundles().publications());
        all.addAll(KernelServices.bundles().drafts());
        info(source, "Bundles. " + all.size() + " definition or definitions.");
        all.stream().limit(PAGE_SIZE).forEach(bundle -> info(
                source,
                bundle.id() + ", revision " + bundle.revision() + ", "
                        + bundle.state().name().toLowerCase(Locale.ROOT) + ", "
                        + bundle.steps().size() + " step or steps, "
                        + (bundle.enabled() ? "enabled." : "disabled.")));
        return Math.max(1, all.size());
    }

    private static int inspectBundle(CommandSourceStack source, String id) {
        BundleCompiler.BundleDefinition bundle = KernelServices.bundles().drafts().stream()
                .filter(candidate -> candidate.id().equals(id.toLowerCase(Locale.ROOT)))
                .findFirst()
                .orElseGet(() -> KernelServices.bundles().find(id).orElse(null));
        if (bundle == null) {
            return fail(source, "Bundle not found.");
        }
        info(source, bundle.id() + ", revision " + bundle.revision() + ", "
                + bundle.authorizationMode().name().toLowerCase(Locale.ROOT) + ".");
        for (BundleCompiler.BundleStep step : bundle.steps()) {
            info(source, step.id() + ", " + step.kind().name().toLowerCase(Locale.ROOT)
                    + ", target " + step.targetId() + ".");
        }
        return 1;
    }

    private static int previewBundle(CommandSourceStack source, String id, int targetCount) {
        return execute(source, "sef:bundle.preview", Map.of(
                "id", id,
                "target_count", Integer.toString(targetCount)), () -> {
            BundleService.Preview preview = KernelServices.bundles().preview(id, targetCount);
            info(source, "Bundle preview. " + preview.bundleId() + ", revision "
                    + preview.revision() + ", expansion " + preview.maximumTargetSteps() + ".");
            preview.problems().forEach(problem -> info(source, "Problem. " + problem));
            return preview.valid() ? 1 : 0;
        });
    }

    private static int addBundleStep(
            CommandSourceStack source,
            String id,
            long revision,
            String stepId,
            BundleCompiler.StepKind kind,
            String targetId,
            Duration delay,
            Map<String, String> bindings
    ) {
        if (kind == BundleCompiler.StepKind.SEF_ACTION && wrapperAction(targetId)) {
            return fail(source, "Wrapper, alias, bundle, profile, and sudo actions cannot be bundle steps.");
        }
        Map<String, String> effectiveBindings = bindings;
        if (kind == BundleCompiler.StepKind.EXTERNAL_ACTOR_COMMAND
                || kind == BundleCompiler.StepKind.SERVER_COMMAND_PROFILE) {
            CommandProfileService.CommandProfile profile =
                    KernelServices.commandProfiles().find(targetId).orElse(null);
            if (profile == null || !profile.enabled()) {
                return fail(source, "An enabled command profile is required.");
            }
            LinkedHashMap<String, String> captured = new LinkedHashMap<>(bindings);
            captured.put("profile_revision", Long.toString(profile.revision()));
            effectiveBindings = Map.copyOf(captured);
        }
        BundleCompiler.BundleStep step;
        try {
            step = new BundleCompiler.BundleStep(
                    stepId,
                    kind,
                    targetId,
                    BundleCompiler.TargetBinding.ACTOR,
                    BundleCompiler.FailureBehavior.STOP,
                    delay,
                    effectiveBindings);
        } catch (IllegalArgumentException exception) {
            return fail(source, "Bundle step is invalid.");
        }
        return execute(source, "sef:bundle.edit", Map.of(
                "id", id,
                "operation", "add_step",
                "kind", kind.name().toLowerCase(Locale.ROOT)), () -> result(
                source,
                KernelServices.bundles().addStep(id, revision, step),
                "Bundle step added."));
    }

    private static int requestBundle(
            CommandSourceStack source,
            String id,
            long revision,
            Collection<ServerPlayer> targets
    ) {
        if (source.getPlayer() == null || targets.isEmpty()) {
            return fail(source, "Bundle execution requires a player issuer and at least one target.");
        }
        BundleService.Preview preview = KernelServices.bundles().preview(id, targets.size());
        if (!preview.valid() || preview.revision() != revision) {
            return fail(source, "Bundle preview failed or revision changed.");
        }
        if (!eligibleTargets(source, targets, "sudo.exempt", "sudo.bypass.exempt", "sudo.hierarchy.bypass")) {
            return fail(source, "One or more bundle targets are unavailable.");
        }
        ConfirmationService.Request request = confirmation(
                source,
                "sef:bundle.run",
                Map.of(
                        "id", id.toLowerCase(Locale.ROOT),
                        "revision", Long.toString(revision),
                        "target_count", Integer.toString(targets.size())),
                targets.stream().map(ServerPlayer::getUUID).toList());
        ActionResult<ConfirmationService.IssuedToken> issued =
                KernelServices.confirmations().issue(request, Duration.ofSeconds(60));
        if (!issued.successful()) {
            return fail(source, "A bundle confirmation token could not be issued.");
        }
        success(source, "Confirmation required. /sef bundle run confirm "
                + issued.value().token() + " " + id + " " + revision
                + (targets.size() == 1 && targets.iterator().next() == source.getPlayer()
                ? ""
                : " <same player selector>"));
        return 1;
    }

    private static int confirmBundle(
            CommandSourceStack source,
            String token,
            String id,
            long revision,
            Collection<ServerPlayer> targets
    ) {
        ConfirmationService.Request request = confirmation(
                source,
                "sef:bundle.run",
                Map.of(
                        "id", id.toLowerCase(Locale.ROOT),
                        "revision", Long.toString(revision),
                        "target_count", Integer.toString(targets.size())),
                targets.stream().map(ServerPlayer::getUUID).toList());
        ActionResult<ConfirmationService.Request> confirmation =
                KernelServices.confirmations().consume(token, request);
        if (!confirmation.successful()) {
            return fail(source, "Bundle confirmation is invalid, expired, used, or changed.");
        }
        if (!eligibleTargets(source, targets, "sudo.exempt", "sudo.bypass.exempt", "sudo.hierarchy.bypass")) {
            return fail(source, "One or more bundle targets became unavailable.");
        }
        return execute(source, "sef:bundle.run", Map.of(
                "id", id,
                "revision", Long.toString(revision),
                "target_count", Integer.toString(targets.size())), targets.stream()
                .map(ServerPlayer::getUUID).toList(), () -> {
            ActionResult<BundleService.RuntimeJob> queued = KernelServices.bundles().enqueue(
                    id,
                    revision,
                    actorId(source),
                    targets.stream().map(ServerPlayer::getUUID).toList(),
                    Instant.now());
            if (!queued.successful()) {
                return fail(source, "Bundle queue rejected. " + queued.detail());
            }
            success(source, "Bundle queued as " + queued.value().jobId() + ".");
            return 1;
        });
    }

    private static int listProfiles(CommandSourceStack source) {
        List<CommandProfileService.CommandProfile> all =
                new ArrayList<>(KernelServices.commandProfiles().publications());
        all.addAll(KernelServices.commandProfiles().drafts());
        info(source, "Command profiles. " + all.size() + " definition or definitions.");
        all.stream().limit(PAGE_SIZE).forEach(profile -> info(
                source,
                profile.id() + ", revision " + profile.revision() + ", "
                        + profile.context().name().toLowerCase(Locale.ROOT) + ", "
                        + profile.state().name().toLowerCase(Locale.ROOT) + ", "
                        + (profile.enabled() ? "enabled." : "disabled.")));
        return Math.max(1, all.size());
    }

    private static int inspectProfile(CommandSourceStack source, String id) {
        CommandProfileService.CommandProfile profile = KernelServices.commandProfiles().drafts().stream()
                .filter(candidate -> candidate.id().equals(id.toLowerCase(Locale.ROOT)))
                .findFirst()
                .orElseGet(() -> KernelServices.commandProfiles().find(id).orElse(null));
        if (profile == null) {
            return fail(source, "Command profile not found.");
        }
        info(source, profile.id() + ", revision " + profile.revision() + ", context "
                + profile.context().name().toLowerCase(Locale.ROOT) + ".");
        info(source, "Root " + profile.root() + ", maximum targets " + profile.maximumTargets()
                + ", consent " + profile.consentRequired() + ".");
        return 1;
    }

    private static int createProfile(
            CommandSourceStack source,
            String id,
            String contextInput,
            int maximumTargets,
            String arguments,
            String template
    ) {
        CommandProfileService.Context profileContext;
        try {
            profileContext = CommandProfileService.Context.valueOf(contextInput.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return fail(source, "Command profile context is invalid.");
        }
        if (profileContext == CommandProfileService.Context.SERVER
                && !has(source, "commands.profile.server")) {
            return fail(source, "Server command profile permission is required.");
        }
        if (profileContext == CommandProfileService.Context.TARGETED_ACTOR
                && !has(source, "commands.profile.targeted")) {
            return fail(source, "Targeted actor profile permission is required.");
        }
        Set<String> allowedArguments = csv(arguments);
        return execute(source, "sef:profile.create", Map.of(
                "id", id,
                "context", profileContext.name().toLowerCase(Locale.ROOT),
                "maximum_targets", Integer.toString(maximumTargets),
                "argument_count", Integer.toString(allowedArguments.size())), () -> result(
                source,
                KernelServices.commandProfiles().createDraft(
                        id,
                        profileContext,
                        boundedDefinitionText(template, 1024),
                        allowedArguments,
                        maximumTargets,
                        actorId(source)),
                "Command profile draft created."));
    }

    private static int validateProfile(CommandSourceStack source, String id, long revision) {
        CommandProfileService.Validation validation =
                KernelServices.commandProfiles().validateDraft(id, revision);
        info(source, "Command profile validation. root " + validation.root() + ", placeholders "
                + String.join(",", validation.placeholders()) + ".");
        validation.problems().forEach(problem -> info(source, "Problem. " + problem));
        return validation.valid() ? 1 : 0;
    }

    private static int testProfile(
            CommandSourceStack source,
            String id,
            long revision,
            String values
    ) {
        ActionResult<CommandProfileService.RenderedCommand> rendered =
                KernelServices.commandProfiles().test(id, revision, parseBindings(values), 1);
        if (!rendered.successful()) {
            return fail(source, "Profile test failed. " + rendered.detail());
        }
        info(source, "Profile test accepted root " + rendered.value().root()
                + ", command length " + rendered.value().command().length() + ".");
        return 1;
    }

    private static int profileReferences(CommandSourceStack source, String id) {
        String normalized = id.toLowerCase(Locale.ROOT);
        List<String> references = new ArrayList<>();
        KernelServices.aliases().published().stream()
                .filter(alias -> (alias.kind() == AliasCompiler.AliasKind.EXTERNAL_ACTOR_COMMAND
                        || alias.kind() == AliasCompiler.AliasKind.SERVER_COMMAND_PROFILE)
                        && alias.targetId().equals(normalized))
                .map(alias -> "alias " + alias.id() + ", revision " + alias.revision())
                .forEach(references::add);
        KernelServices.bundles().publications().forEach(bundle ->
                bundle.steps().stream()
                        .filter(step -> (step.kind() == BundleCompiler.StepKind.EXTERNAL_ACTOR_COMMAND
                                || step.kind() == BundleCompiler.StepKind.SERVER_COMMAND_PROFILE)
                                && step.targetId().equals(normalized))
                        .map(step -> "bundle " + bundle.id() + ", revision " + bundle.revision()
                                + ", step " + step.id())
                        .forEach(references::add));
        info(source, "Command profile references. " + references.size() + ".");
        references.stream().limit(PAGE_SIZE).forEach(reference -> info(source, reference + "."));
        return Math.max(1, references.size());
    }

    private static int enableProfile(
            CommandSourceStack source,
            String id,
            long revision,
            String state
    ) {
        CommandProfileService.CommandProfile profile =
                KernelServices.commandProfiles().find(id).orElse(null);
        if (profile == null) {
            return fail(source, "Command profile not found.");
        }
        if (profile.context() == CommandProfileService.Context.SERVER
                && !has(source, "commands.profile.server")) {
            return fail(source, "Server command profile permission is required.");
        }
        if (profile.context() == CommandProfileService.Context.TARGETED_ACTOR
                && !has(source, "commands.profile.targeted")) {
            return fail(source, "Targeted actor profile permission is required.");
        }
        return execute(source, "sef:profile.enable", Map.of(
                "id", id,
                "state", Boolean.toString(parseState(state))), () -> result(
                source,
                KernelServices.commandProfiles().setEnabled(
                        id, revision, parseState(state), actorId(source)),
                "Command profile state changed."));
    }

    private static int executeProfile(
            CommandSourceStack source,
            String id,
            long revision,
            ServerPlayer target,
            Map<String, String> values
    ) {
        return executeProfile(source, null, id, revision, target, values);
    }

    private static int confirmProfile(
            CommandSourceStack source,
            String token,
            String id,
            long revision,
            ServerPlayer target,
            Map<String, String> values
    ) {
        return executeProfile(source, token, id, revision, target, values);
    }

    private static int executeProfile(
            CommandSourceStack source,
            String confirmationToken,
            String id,
            long revision,
            ServerPlayer target,
            Map<String, String> values
    ) {
        CommandProfileService.CommandProfile profile =
                KernelServices.commandProfiles().find(id).orElse(null);
        if (profile == null || !profile.enabled()) {
            return fail(source, "Enabled command profile not found.");
        }
        if (profile.context() == CommandProfileService.Context.SERVER
                && !has(source, "commands.profile.server")) {
            return fail(source, "Server command profile permission is required.");
        }
        if (profile.context() == CommandProfileService.Context.TARGETED_ACTOR
                && !has(source, "commands.profile.targeted")) {
            return fail(source, "Targeted actor profile permission is required.");
        }
        if (profile.context() == CommandProfileService.Context.TARGETED_ACTOR && target == null) {
            return fail(source, "Targeted actor profiles require an online target.");
        }
        if (profile.context() != CommandProfileService.Context.TARGETED_ACTOR && target != null) {
            return fail(source, "This command profile does not accept a target.");
        }
        if (target != null && !eligibleTarget(
                source,
                target,
                "sudo.exempt",
                "sudo.bypass.exempt",
                "sudo.hierarchy.bypass")) {
            return fail(source, "That player is unavailable.");
        }
        if (target != null) {
            SudoPolicyRepository.Decision policy = KernelServices.sudoPolicies().decide(
                    target.getUUID(),
                    has(source, "commands.sudo.bypass.consent"),
                    has(source, "commands.sudo.bypass.lock"));
            if (!policy.allowed()) {
                return fail(source, policy.detail() + ".");
            }
        }
        Map<String, String> boundValues = new LinkedHashMap<>(values);
        if (target != null) {
            boundValues.put("target", target.getGameProfile().getName());
        }
        ActionResult<CommandProfileService.RenderedCommand> rendered =
                KernelServices.commandProfiles().renderPublished(
                        id,
                        revision,
                        profile.context(),
                        boundValues,
                        1);
        if (!rendered.successful()) {
            return fail(source, "Command profile rendering failed. " + rendered.detail());
        }
        List<UUID> targets = target == null ? List.of() : List.of(target.getUUID());
        Map<String, String> metadata = Map.of(
                "id", profile.id(),
                "revision", Long.toString(revision),
                "context", profile.context().name().toLowerCase(Locale.ROOT),
                "root", rendered.value().root(),
                "argument_count", Integer.toString(boundValues.size()),
                "binding_digest", digestBindings(boundValues));
        boolean confirmationRequired =
                profile.context() != CommandProfileService.Context.ACTOR;
        if (confirmationRequired) {
            ConfirmationService.Request request = confirmation(
                    source,
                    "sef:profile.execute",
                    metadata,
                    targets);
            if (confirmationToken == null) {
                ActionResult<ConfirmationService.IssuedToken> issued =
                        KernelServices.confirmations().issue(request, Duration.ofSeconds(60));
                if (!issued.successful()) {
                    return fail(source, "A command profile confirmation token could not be issued.");
                }
                String context = profile.context() == CommandProfileService.Context.SERVER
                        ? "server"
                        : "targeted";
                String targetName = target == null
                        ? ""
                        : " " + target.getGameProfile().getName();
                success(source, "Confirmation required. /sef profile execute confirm "
                        + context + " " + issued.value().token() + " " + profile.id()
                        + " " + revision + targetName + ". repeat the same values.");
                return 1;
            }
            ActionResult<ConfirmationService.Request> consumed =
                    KernelServices.confirmations().consume(confirmationToken, request);
            if (!consumed.successful()) {
                return fail(source, "Command profile confirmation is invalid, expired, used, or changed.");
            }
        } else if (confirmationToken != null) {
            return fail(source, "Actor command profiles do not accept a confirmation token.");
        }
        CommandSourceStack executionSource = profile.context() == CommandProfileService.Context.SERVER
                ? source.getServer().createCommandSourceStack()
                : source;
        return execute(source, "sef:profile.execute", metadata, targets, () -> dispatch(
                source,
                executionSource,
                rendered.value().command(),
                "Command profile"));
    }

    private static String digestBindings(Map<String, String> values) {
        String canonical = values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "\u0000" + entry.getValue())
                .collect(java.util.stream.Collectors.joining("\u0001"));
        return digest(canonical);
    }

    private static int listFakeProfiles(CommandSourceStack source) {
        List<FakeIdentityService.FakeProfile> profiles = KernelServices.fakeIdentities().profiles();
        info(source, "Fake profiles. " + profiles.size() + ".");
        profiles.stream().limit(PAGE_SIZE).forEach(profile -> info(
                source,
                profile.id() + ", " + profile.username() + ", revision " + profile.revision()
                        + ", " + (profile.enabled() ? "enabled." : "disabled.")));
        return Math.max(1, profiles.size());
    }

    private static int fakeProfileCreate(
            CommandSourceStack source,
            String id,
            String username,
            String nickname,
            String prefix,
            String suffix
    ) {
        return execute(source, "sef:fake.profile", Map.of(
                "operation", "create",
                "id", id), () -> result(
                source,
                KernelServices.fakeIdentities().createDraft(
                        id, username, nickname, prefix, suffix, actorId(source)),
                "Fake profile draft created."));
    }

    private static int listFakeScenes(CommandSourceStack source) {
        List<FakeIdentityService.Scene> scenes = KernelServices.fakeIdentities().scenes();
        info(source, "Fake scenes. " + scenes.size() + ".");
        scenes.stream().limit(PAGE_SIZE).forEach(scene -> info(
                source,
                scene.id() + ", revision " + scene.revision() + ", "
                        + scene.events().size() + " event or events, "
                        + (scene.enabled() ? "enabled." : "disabled.")));
        return Math.max(1, scenes.size());
    }

    private static int saveFakeScene(
            CommandSourceStack source,
            String id,
            long revision,
            String audienceInput,
            String typeInput,
            String identity,
            String message
    ) {
        FakeIdentityService.Audience audience;
        FakeIdentityService.EventType type;
        try {
            audience = FakeIdentityService.Audience.valueOf(audienceInput.toUpperCase(Locale.ROOT));
            type = FakeIdentityService.EventType.valueOf(typeInput.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return fail(source, "Fake scene audience or event type is invalid.");
        }
        FakeIdentityService.Scene scene;
        try {
            scene = new FakeIdentityService.Scene(
                    id,
                    Math.max(1L, revision),
                    List.of(new FakeIdentityService.SceneEvent(
                            0L,
                            type,
                            identity,
                            type == FakeIdentityService.EventType.MESSAGE
                                    ? boundedDefinitionText(message, 2048)
                                    : "")),
                    audience,
                    true);
        } catch (IllegalArgumentException exception) {
            return fail(source, "Fake scene is invalid.");
        }
        return execute(source, "sef:fake.scene", Map.of(
                "operation", "save",
                "id", id,
                "event_count", "1"), () -> result(
                source,
                KernelServices.fakeIdentities().saveScene(scene, revision),
                "Fake scene saved."));
    }

    private static int previewFakeScene(CommandSourceStack source, String id) {
        FakeIdentityService.Scene scene = KernelServices.fakeIdentities().scene(id).orElse(null);
        if (scene == null) {
            return fail(source, "Fake scene not found.");
        }
        info(source, "Fake scene preview. " + scene.id() + ", revision " + scene.revision()
                + ", audience " + scene.audience().name().toLowerCase(Locale.ROOT) + ".");
        for (FakeIdentityService.SceneEvent event : scene.events()) {
            info(source, event.offsetTicks() + " ticks, "
                    + event.type().name().toLowerCase(Locale.ROOT) + ", identity "
                    + event.identity() + ", message length " + event.message().length() + ".");
        }
        return 1;
    }

    private static int scheduleFakeScene(
            CommandSourceStack source,
            String id,
            long revision,
            int delaySeconds
    ) {
        return execute(source, "sef:fake.schedule", Map.of(
                "id", id,
                "delay_seconds", Integer.toString(delaySeconds)), () -> result(
                source,
                KernelServices.fakeIdentities().schedule(
                        id,
                        revision,
                        Instant.now().plusSeconds(delaySeconds),
                        actorId(source)),
                "Fake scene scheduled."));
    }

    private static int fakeConnection(
            CommandSourceStack source,
            String input,
            FakeIdentityService.EventType type
    ) {
        ActionResult<FakeIdentityService.ResolvedIdentity> identity =
                KernelServices.fakeIdentities().resolve(input, source.getPlayer());
        if (!identity.successful()) {
            return fail(source, "Fake identity is invalid.");
        }
        ActionResult<Component> rendered = type == FakeIdentityService.EventType.JOIN
                ? KernelServices.fakeIdentities().renderJoin(identity.value())
                : KernelServices.fakeIdentities().renderLeave(identity.value());
        if (!rendered.successful()) {
            return fail(source, "Fake connection message template is invalid.");
        }
        String action = type == FakeIdentityService.EventType.JOIN
                ? "sef:fake.join"
                : "sef:fake.leave";
        return execute(source, action, Map.of(
                "identity_provenance", identity.value().provenance(),
                "identity_known", Boolean.toString(identity.value().playerId() != null)), () -> {
            int delivered = KernelServices.fakeIdentities().broadcast(
                    source.getServer(),
                    identity.value(),
                    rendered.value(),
                    FakeIdentityService.Audience.SERVER,
                    source.getPlayer());
            success(source, "Unsigned fake connection message delivered to " + delivered + " player or players.");
            return Math.max(1, delivered);
        });
    }

    private static int fakeMessage(CommandSourceStack source, String input, String message) {
        ActionResult<FakeIdentityService.ResolvedIdentity> identity =
                KernelServices.fakeIdentities().resolve(input, source.getPlayer());
        if (!identity.successful()) {
            return fail(source, "Fake identity is invalid.");
        }
        ActionResult<Component> rendered =
                KernelServices.fakeIdentities().renderChat(identity.value(), message);
        if (!rendered.successful()) {
            return fail(source, "Fake message is invalid.");
        }
        return execute(source, "sef:fake.message", Map.of(
                "identity_provenance", identity.value().provenance(),
                "message_length", Integer.toString(message.length())), () -> {
            int delivered = KernelServices.fakeIdentities().broadcast(
                    source.getServer(),
                    identity.value(),
                    rendered.value(),
                    FakeIdentityService.Audience.SERVER,
                    source.getPlayer());
            success(source, "Unsigned fake system message delivered to " + delivered + " player or players.");
            return Math.max(1, delivered);
        });
    }

    private static int fakeRankMessage(
            CommandSourceStack source,
            String prefix,
            String suffix,
            String username,
            String message
    ) {
        FakeIdentityService.ResolvedIdentity identity;
        try {
            identity = KernelServices.fakeIdentities().rank(prefix, suffix, username);
        } catch (IllegalArgumentException exception) {
            return fail(source, "Fake rank identity is invalid.");
        }
        ActionResult<Component> rendered = KernelServices.fakeIdentities().renderChat(identity, message);
        if (!rendered.successful()) {
            return fail(source, "Fake rank message is invalid.");
        }
        return execute(source, "sef:fake.rank_message", Map.of(
                "message_length", Integer.toString(message.length()),
                "prefix_length", Integer.toString(prefix.length()),
                "suffix_length", Integer.toString(suffix.length())), () -> {
            int delivered = KernelServices.fakeIdentities().broadcast(
                    source.getServer(),
                    identity,
                    rendered.value(),
                    FakeIdentityService.Audience.SERVER,
                    source.getPlayer());
            success(source, "Unsigned fake rank system message delivered.");
            return Math.max(1, delivered);
        });
    }

    private static int requestSudo(
            CommandSourceStack source,
            ServerPlayer target,
            String command,
            boolean delegated,
            boolean compatibilitySyntax
    ) {
        SudoPreflight preflight = sudoPreflight(
                source,
                target,
                command,
                delegated,
                compatibilitySyntax);
        if (!preflight.allowed()) {
            if (delegated) {
                SudoDelegationAudit.record(
                        SudoDelegationAudit.Stage.ADMISSION,
                        source,
                        target,
                        UUID.randomUUID(),
                        null,
                        "",
                        "",
                        AuditService.Result.REJECTED,
                        ActionResult.ReasonCode.POLICY_DENIED,
                        preflight.detail());
            }
            return fail(source, preflight.detail());
        }
        if (delegated) {
            SudoDelegationAudit.record(
                    SudoDelegationAudit.Stage.ADMISSION,
                    source,
                    target,
                    preflight.preview().correlationId(),
                    null,
                    preflight.preview().root(),
                    preflight.delegationProfile().id(),
                    AuditService.Result.SUCCESS,
                    ActionResult.ReasonCode.SUCCESS,
                    "admission facts accepted");
        }
        if (delegated && !KernelServices.administrativeExecution()
                .settings()
                .delegationConfirmationRequired()) {
            if (!has(source, "commands.sudo.delegate.confirm")) {
                SudoDelegationAudit.record(
                        SudoDelegationAudit.Stage.CONFIRMATION,
                        source,
                        target,
                        preflight.preview().correlationId(),
                        null,
                        preflight.preview().root(),
                        preflight.delegationProfile().id(),
                        AuditService.Result.REJECTED,
                        ActionResult.ReasonCode.PERMISSION_DENIED,
                        "delegated dispatch permission denied");
                return fail(source, "Delegated sudo dispatch is not allowed.");
            }
            SudoDelegationAudit.record(
                    SudoDelegationAudit.Stage.CONFIRMATION,
                    source,
                    target,
                    preflight.preview().correlationId(),
                    null,
                    preflight.preview().root(),
                    preflight.delegationProfile().id(),
                    AuditService.Result.SUCCESS,
                    ActionResult.ReasonCode.SUCCESS,
                    "confirmation disabled by policy");
            return dispatchSudo(
                    source,
                    target,
                    true,
                    preflight,
                    UUID.randomUUID());
        }
        ConfirmationService.Request request = confirmation(
                source,
                "sef:sudo.run",
                sudoCommandMetadata(preflight, delegated),
                List.of(target.getUUID()));
        ActionResult<ConfirmationService.IssuedToken> issued =
                KernelServices.confirmations().issue(request, Duration.ofSeconds(60));
        if (!issued.successful()) {
            if (delegated) {
                SudoDelegationAudit.record(
                        SudoDelegationAudit.Stage.CONFIRMATION,
                        source,
                        target,
                        preflight.preview().correlationId(),
                        null,
                        preflight.preview().root(),
                        preflight.delegationProfile().id(),
                        AuditService.Result.FAILED,
                        issued.reason(),
                        issued.detail());
            }
            return fail(source, "A sudo confirmation token could not be issued.");
        }
        if (delegated) {
            SudoDelegationAudit.record(
                    SudoDelegationAudit.Stage.CONFIRMATION,
                    source,
                    target,
                    preflight.preview().correlationId(),
                    null,
                    preflight.preview().root(),
                    preflight.delegationProfile().id(),
                    AuditService.Result.SUCCESS,
                    ActionResult.ReasonCode.SUCCESS,
                    "confirmation issued");
        }
        success(source, "Confirmation required. /sudo confirm " + issued.value().token()
                + " " + target.getGameProfile().getName()
                + " " + (delegated ? "delegate" : "respect")
                + " " + preflight.preview().normalizedCommand());
        return 1;
    }

    private static int confirmSudo(
            CommandSourceStack source,
            String token,
            ServerPlayer target,
            String permissionMode,
            String command
    ) {
        boolean delegated;
        try {
            delegated = parseDelegatedMode(permissionMode);
        } catch (IllegalArgumentException exception) {
            return fail(source, exception.getMessage());
        }
        boolean compatibilitySyntax = permissionMode.equalsIgnoreCase("true")
                || permissionMode.equalsIgnoreCase("false");
        if (delegated && !has(source, "commands.sudo.delegate.confirm")) {
            return fail(source, "Delegated sudo confirmation is not allowed.");
        }
        SudoPreflight preflight = sudoPreflight(
                source,
                target,
                command,
                delegated,
                compatibilitySyntax);
        if (!preflight.allowed()) {
            if (delegated) {
                SudoDelegationAudit.record(
                        SudoDelegationAudit.Stage.ADMISSION,
                        source,
                        target,
                        UUID.randomUUID(),
                        null,
                        "",
                        "",
                        AuditService.Result.REJECTED,
                        ActionResult.ReasonCode.POLICY_DENIED,
                        preflight.detail());
            }
            return fail(source, preflight.detail());
        }
        ConfirmationService.Request request = confirmation(
                source,
                "sef:sudo.run",
                sudoCommandMetadata(preflight, delegated),
                List.of(target.getUUID()));
        ActionResult<ConfirmationService.Request> consumed =
                KernelServices.confirmations().consume(token, request);
        if (!consumed.successful()) {
            if (delegated) {
                SudoDelegationAudit.record(
                        SudoDelegationAudit.Stage.CONFIRMATION,
                        source,
                        target,
                        preflight.preview().correlationId(),
                        null,
                        preflight.preview().root(),
                        preflight.delegationProfile().id(),
                        AuditService.Result.REJECTED,
                        consumed.reason(),
                        consumed.detail());
            }
            return fail(source, "Sudo confirmation is invalid, expired, used, or changed.");
        }
        if (delegated) {
            SudoDelegationAudit.record(
                    SudoDelegationAudit.Stage.CONFIRMATION,
                    source,
                    target,
                    preflight.preview().correlationId(),
                    null,
                    preflight.preview().root(),
                    preflight.delegationProfile().id(),
                    AuditService.Result.SUCCESS,
                    ActionResult.ReasonCode.SUCCESS,
                    "confirmation consumed");
        }
        return dispatchSudo(
                source,
                target,
                delegated,
                preflight,
                UUID.nameUUIDFromBytes(
                        ("sef:sudo:confirmation:" + digest(token))
                                .getBytes(StandardCharsets.UTF_8)));
    }

    private static int dispatchSudo(
            CommandSourceStack source,
            ServerPlayer target,
            boolean delegated,
            SudoPreflight preflight,
            UUID confirmationId
    ) {
        EphemeralExecutionGrant grant = null;
        if (delegated) {
            if (!preflight.revisionsCurrent(target)) {
                return fail(source, "Delegated sudo admission changed before dispatch.");
            }
            Instant createdAt = Instant.now();
            AdministrativeExecutionService.DelegationProfile profile =
                    preflight.delegationProfile();
            grant = new EphemeralExecutionGrant(
                    UUID.randomUUID(),
                    actorId(source),
                    target.getUUID(),
                    preflight.targetSessionRevision(),
                    preflight.preview().root(),
                    profile.canonicalActionId(),
                    digest(preflight.preview().normalizedCommand()),
                    preflight.commandTreeRevision(),
                    profile.id(),
                    profile.revision(),
                    profile.maximumTemporaryVanillaPermissionLevel(),
                    profile.temporarySefPermissionIds(),
                    profile.approvedAdapterCapabilities(),
                    preflight.sudoPolicyRevision(),
                    preflight.permissionProviderRevision(),
                    preflight.featureRevision(),
                    preflight.configurationRevision(),
                    createdAt,
                    createdAt.plusSeconds(KernelServices.administrativeExecution()
                            .settings()
                            .delegationGrantLifetimeSeconds()),
                    confirmationId,
                    preflight.preview().correlationId());
        }
        EphemeralExecutionGrant executionGrant = grant;
        boolean[] bodyEntered = {false};
        if (executionGrant != null) {
            SudoDelegationAudit.record(
                    SudoDelegationAudit.Stage.DISPATCH,
                    source,
                    target,
                    preflight.preview().correlationId(),
                    executionGrant,
                    preflight.preview().root(),
                    preflight.delegationProfile().id(),
                    AuditService.Result.OUTCOME_UNKNOWN,
                    ActionResult.ReasonCode.SUCCESS,
                    "grant created for dispatcher invocation");
        }
        try {
            int outcome = execute(source, "sef:sudo.run", sudoExecutionMetadata(
                    preflight,
                    executionGrant),
                    List.of(target.getUUID()), () -> {
                        bodyEntered[0] = true;
                        ActionResult<Integer> executed =
                                delegated
                                        ? KernelServices.administrativeExecution().sudoRun(
                                                source,
                                                target,
                                                preflight.preview().normalizedCommand(),
                                                executionGrant)
                                        : KernelServices.administrativeExecution().sudoRun(
                                                source,
                                                target,
                                                preflight.preview().normalizedCommand());
                        if (executionGrant != null) {
                            SudoDelegationAudit.record(
                                    SudoDelegationAudit.Stage.RESULT,
                                    source,
                                    target,
                                    preflight.preview().correlationId(),
                                    executionGrant,
                                    preflight.preview().root(),
                                    preflight.delegationProfile().id(),
                                    executed.successful()
                                            ? AuditService.Result.SUCCESS
                                            : AuditService.Result.FAILED,
                                    executed.reason(),
                                    executed.detail());
                        }
                        if (!executed.successful()) {
                            return fail(source, "Sudo execution failed. " + executed.detail());
                        }
                        if (ConfigHandler.config.sudoNotifyTarget.get()
                                && (!delegated || KernelServices.administrativeExecution()
                                .settings()
                                .delegationNotifyTarget())) {
                            target.sendSystemMessage(TextFormatter.stringToFormattedText(
                                    ConfigHandler.config.sudoNotifyMsg.get()
                                            .replace("$admin", source.getTextName())
                                            .replace("$command", preflight.preview().normalizedCommand())));
                        }
                        success(source, "Sudo command executed as " + target.getGameProfile().getName()
                                + (delegated ? " with one execution delegated permission." : "."));
                        return Math.max(1, executed.value());
                    });
            if (executionGrant != null && !bodyEntered[0]) {
                SudoDelegationAudit.record(
                        SudoDelegationAudit.Stage.RESULT,
                        source,
                        target,
                        preflight.preview().correlationId(),
                        executionGrant,
                        preflight.preview().root(),
                        preflight.delegationProfile().id(),
                        AuditService.Result.REJECTED,
                        ActionResult.ReasonCode.POLICY_DENIED,
                        "shared execution policy rejected dispatch");
            }
            return outcome;
        } finally {
            if (executionGrant != null) {
                SudoDelegationAudit.record(
                        SudoDelegationAudit.Stage.CLEANUP,
                        source,
                        target,
                        preflight.preview().correlationId(),
                        executionGrant,
                        preflight.preview().root(),
                        preflight.delegationProfile().id(),
                        AuditService.Result.SUCCESS,
                        ActionResult.ReasonCode.SUCCESS,
                        DelegatedPermissionScope.active()
                                ? "temporary scope remained active"
                                : "temporary scope removed");
            }
        }
    }

    private static int sudoDryRun(
            CommandSourceStack source,
            ServerPlayer target,
            String command,
            boolean delegated
    ) {
        SudoPreflight preflight = sudoPreflight(source, target, command, delegated, false);
        return execute(source, "sef:sudo.dryrun", Map.of(
                "target", target.getUUID().toString(),
                "allowed", Boolean.toString(preflight.allowed()),
                "permission_mode", delegated ? "delegated_once" : "target",
                "command_length", Integer.toString(command.length())), List.of(target.getUUID()), () -> {
            if (!preflight.allowed()) {
                info(source, "Sudo dry run denied. " + preflight.detail());
                return 0;
            }
            info(source, "Sudo dry run accepted root " + preflight.preview().root()
                    + ", command length " + preflight.preview().normalizedCommand().length()
                    + (delegated
                    ? ", profile " + preflight.delegationProfile().id()
                    + ", temporary vanilla level "
                    + preflight.delegationProfile().maximumTemporaryVanillaPermissionLevel()
                    : "")
                    + ".");
            return 1;
        });
    }

    private static int sudoChat(CommandSourceStack source, ServerPlayer target, String message) {
        if (!eligibleTarget(
                source,
                target,
                "sudo.exempt",
                "sudo.bypass.exempt",
                "sudo.hierarchy.bypass")) {
            return fail(source, "That player is unavailable.");
        }
        SudoPolicyRepository.Decision policy = KernelServices.sudoPolicies().decide(
                target.getUUID(),
                has(source, "commands.sudo.bypass.consent"),
                has(source, "commands.sudo.bypass.lock"));
        if (!policy.allowed()) {
            return fail(source, policy.detail() + ".");
        }
        ActionResult<FakeIdentityService.ResolvedIdentity> identity =
                KernelServices.fakeIdentities().resolve(
                        target.getGameProfile().getName(),
                        source.getPlayer());
        if (!identity.successful()) {
            return fail(source, "Target identity could not be rendered.");
        }
        ActionResult<Component> rendered =
                KernelServices.fakeIdentities().renderChat(identity.value(), message);
        if (!rendered.successful()) {
            return fail(source, "Sudo chat message is invalid.");
        }
        return execute(source, "sef:sudo.chat", Map.of(
                "message_length", Integer.toString(message.length()),
                "transport", "unsigned_system"), List.of(target.getUUID()), () -> {
            int delivered = KernelServices.fakeIdentities().broadcast(
                    source.getServer(),
                    identity.value(),
                    rendered.value(),
                    FakeIdentityService.Audience.SERVER,
                    source.getPlayer());
            success(source, "Unsigned sudo chat presentation delivered.");
            return Math.max(1, delivered);
        });
    }

    private static int sudoConsent(CommandSourceStack source, boolean consent) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return fail(source, "Sudo consent requires a player source.");
        }
        SudoPolicyRepository.Policy current = KernelServices.sudoPolicies().policy(player.getUUID());
        return execute(source, "sef:sudo.consent", Map.of(
                "state", Boolean.toString(consent)), () -> result(
                source,
                KernelServices.sudoPolicies().setConsent(
                        player.getUUID(),
                        consent,
                        current.revision()),
                "Sudo consent changed."));
    }

    private static int sudoLock(
            CommandSourceStack source,
            ServerPlayer target,
            boolean locked,
            String reason
    ) {
        if (!eligibleTarget(
                source,
                target,
                "sudo.exempt",
                "sudo.bypass.exempt",
                "sudo.hierarchy.bypass")) {
            return fail(source, "That player is unavailable.");
        }
        SudoPolicyRepository.Policy current = KernelServices.sudoPolicies().policy(target.getUUID());
        return execute(source, "sef:sudo.lock", Map.of(
                "state", Boolean.toString(locked),
                "reason_length", Integer.toString(reason.length())), List.of(target.getUUID()), () -> result(
                source,
                KernelServices.sudoPolicies().setLock(
                        target.getUUID(),
                        locked,
                        reason,
                        actorId(source),
                        current.revision()),
                "Sudo lock changed."));
    }

    private static int sudoPolicy(CommandSourceStack source, ServerPlayer target) {
        AdministrativeExecutionService.Settings settings =
                KernelServices.administrativeExecution().settings();
        List<UUID> targets = target == null ? List.of() : List.of(target.getUUID());
        return execute(source, "sef:sudo.policy", Map.of(
                "scope", target == null ? "server" : "target"),
                targets, () -> {
            info(source, "Delegated sudo " + (settings.delegationEnabled() ? "enabled" : "disabled")
                    + ", allowed roots " + settings.delegationAllowedRoots()
                    + ", denied roots " + settings.delegationDeniedRoots()
                    + ", maximum vanilla level "
                    + settings.delegationMaximumTemporaryVanillaPermissionLevel()
                    + ", lifetime " + settings.delegationGrantLifetimeSeconds() + " seconds.");
            settings.delegationProfiles().values().stream()
                    .sorted(java.util.Comparator.comparing(
                            AdministrativeExecutionService.DelegationProfile::id))
                    .forEach(profile -> info(source, "Delegation profile " + profile.id()
                            + ", revision " + profile.revision()
                            + ", roots " + String.join(",", profile.allowedRoots()) + "."));
            Set<String> broadDelegationGrants = DynamicPermissionService.broadGrants(
                    source.getPlayer(),
                    Set.of(
                            "sef.commands.sudo.ignore_permissions",
                            "sef.commands.sudo.delegate",
                            "sef.commands.sudo.delegate.preview",
                            "sef.commands.sudo.delegate.confirm",
                            "sef.commands.sudo.delegate.root.effect",
                            "sef.commands.sudo.delegate.profile.effect"));
            if (!broadDelegationGrants.isEmpty()) {
                info(source, "Warning. broad permission grants include delegated sudo. "
                        + String.join(",", broadDelegationGrants) + ".");
            }
            if (target != null) {
                SudoPolicyRepository.Policy policy =
                        KernelServices.sudoPolicies().policy(target.getUUID());
                info(source, "Target " + target.getGameProfile().getName()
                        + ", consent " + policy.consent()
                        + ", locked " + policy.locked()
                        + ", session revision "
                        + SefGuiServer.targetSessionRevision(target.getUUID())
                        + ", policy revision " + policy.revision() + ".");
            }
            return 1;
        });
    }

    private static SudoPreflight sudoPreflight(
            CommandSourceStack source,
            ServerPlayer target,
            String command,
            boolean delegated,
            boolean compatibilitySyntax
    ) {
        AdministrativeExecutionService.Settings settings =
                KernelServices.administrativeExecution().settings();
        if (compatibilitySyntax && !settings.delegationCompatibilityBooleanSyntax()) {
            return SudoPreflight.denied("The sudo compatibility boolean syntax is disabled.");
        }
        if (delegated && !settings.delegationEnabled()) {
            return SudoPreflight.denied("Delegated sudo is disabled.");
        }
        if (delegated
                && (!has(source, "commands.sudo.ignore_permissions")
                || !has(source, "commands.sudo.delegate")
                || !has(source, "commands.sudo.delegate.preview"))) {
            return SudoPreflight.denied("One execution delegated permission is not allowed.");
        }
        if (delegated
                && source.getPlayer() != null
                && source.getPlayer().getUUID().equals(target.getUUID())
                && (!settings.delegationAllowSelf()
                || !has(source, "commands.sudo.delegate.self"))) {
            return SudoPreflight.denied("Delegated sudo cannot target the issuer.");
        }
        if (!eligibleTarget(
                source,
                target,
                "sudo.exempt",
                "sudo.bypass.exempt",
                "sudo.hierarchy.bypass")) {
            return SudoPreflight.denied("That player is unavailable.");
        }
        if (delegated && !eligibleTarget(
                source,
                target,
                "sudo.delegate.exempt",
                "sudo.bypass.exempt",
                "sudo.hierarchy.bypass")) {
            return SudoPreflight.denied("That player is exempt from delegated sudo.");
        }
        SudoPolicyRepository.Decision targetPolicy = KernelServices.sudoPolicies().decide(
                target.getUUID(),
                has(source, "commands.sudo.bypass.consent")
                        || delegated && !settings.delegationRequireTargetConsent(),
                has(source, "commands.sudo.bypass.lock"));
        if (!targetPolicy.allowed()) {
            return SudoPreflight.denied(targetPolicy.detail() + ".");
        }
        if (!delegated) {
            ActionResult<AdministrativeExecutionService.Preview> preview =
                    KernelServices.administrativeExecution().preview(
                            source,
                            target.createCommandSourceStack(),
                            command,
                            AdministrativeExecutionService.Context.TARGETED_ACTOR,
                            CommandWrapperService.Origin.DIRECT);
            return preview.successful()
                    ? SudoPreflight.respect(preview.value())
                    : SudoPreflight.denied("Sudo denied. " + preview.detail() + ".");
        }
        long targetSessionRevision = SefGuiServer.targetSessionRevision(target.getUUID());
        if (targetSessionRevision < 1L) {
            return SudoPreflight.denied("The target session is unavailable.");
        }
        ActionResult<AdministrativeExecutionService.DelegatedPreview> preview =
                KernelServices.administrativeExecution().previewDelegated(
                        source,
                        target,
                        command);
        if (!preview.successful()) {
            return SudoPreflight.denied("Delegated sudo denied. " + preview.detail() + ".");
        }
        AdministrativeExecutionService.DelegatedPreview delegatedPreview = preview.value();
        if (!has(source, "commands.sudo.delegate.root." + delegatedPreview.preview().root())) {
            return SudoPreflight.denied("Delegated sudo root permission is denied.");
        }
        if (!has(source, "commands.sudo.delegate.profile." + delegatedPreview.profile().id())) {
            return SudoPreflight.denied("Delegated sudo profile permission is denied.");
        }
        long configurationRevision = KernelServices.configurationRevision();
        return SudoPreflight.delegated(
                delegatedPreview.preview(),
                delegatedPreview.profile(),
                delegatedPreview.commandTreeRevision(),
                targetSessionRevision,
                targetPolicy.policy().revision(),
                PermissionService.providerRevision(),
                configurationRevision,
                configurationRevision);
    }

    private static int requestRun(CommandSourceStack source, String command) {
        ActionResult<AdministrativeExecutionService.Preview> preview =
                KernelServices.administrativeExecution().preview(
                        source,
                        command,
                        AdministrativeExecutionService.Context.SERVER,
                        CommandWrapperService.Origin.DIRECT);
        if (!preview.successful()) {
            return fail(source, "Run denied. " + preview.detail());
        }
        if (!runRootAllowed(source, preview.value().root())) {
            return fail(source, "Run root permission is denied.");
        }
        ConfirmationService.Request request = confirmation(
                source,
                "sef:run.server",
                commandMetadata(preview.value()),
                List.of());
        ActionResult<ConfirmationService.IssuedToken> issued =
                KernelServices.confirmations().issue(request, Duration.ofSeconds(60));
        if (!issued.successful()) {
            return fail(source, "A run confirmation token could not be issued.");
        }
        success(source, "Confirmation required. /run confirm " + issued.value().token()
                + " " + preview.value().normalizedCommand());
        return 1;
    }

    private static int confirmRun(CommandSourceStack source, String token, String command) {
        ActionResult<AdministrativeExecutionService.Preview> preview =
                KernelServices.administrativeExecution().preview(
                        source,
                        command,
                        AdministrativeExecutionService.Context.SERVER,
                        CommandWrapperService.Origin.DIRECT);
        if (!preview.successful() || !runRootAllowed(source, preview.successful()
                ? preview.value().root()
                : "")) {
            return fail(source, "Run preflight changed or was denied.");
        }
        ConfirmationService.Request request = confirmation(
                source,
                "sef:run.server",
                commandMetadata(preview.value()),
                List.of());
        ActionResult<ConfirmationService.Request> consumed =
                KernelServices.confirmations().consume(token, request);
        if (!consumed.successful()) {
            return fail(source, "Run confirmation is invalid, expired, used, or changed.");
        }
        return execute(source, "sef:run.server", commandMetadata(preview.value()), () -> {
            ActionResult<Integer> result =
                    KernelServices.administrativeExecution().runServer(
                            source,
                            preview.value().normalizedCommand());
            if (!result.successful()) {
                return fail(source, "Run execution failed. " + result.detail());
            }
            success(source, "Server source command completed with result " + result.value() + ".");
            return Math.max(1, result.value());
        });
    }

    private static int silentActor(CommandSourceStack source, String command) {
        ActionResult<AdministrativeExecutionService.Preview> preview =
                KernelServices.administrativeExecution().preview(
                        source,
                        command,
                        AdministrativeExecutionService.Context.SILENT_ACTOR,
                        CommandWrapperService.Origin.DIRECT);
        if (!preview.successful()) {
            return fail(source, "Silent command denied. " + preview.detail());
        }
        boolean acceptUnsuppressible = has(source, "commands.silent.unsuppressible");
        if (preview.value().silenceCapability()
                != AdministrativeExecutionService.SilenceCapability.SUPPORTED
                && !acceptUnsuppressible) {
            return fail(source, "This command can create independent output.");
        }
        return execute(source, "sef:silent.actor", commandMetadata(preview.value()), () -> {
            ActionResult<Integer> result = KernelServices.administrativeExecution().silentActor(
                    source,
                    preview.value().normalizedCommand(),
                    acceptUnsuppressible);
            if (!result.successful()) {
                return fail(source, "Silent command failed. " + result.detail());
            }
            success(source, "Source feedback was suppressed. mandatory observation remains active.");
            return Math.max(1, result.value());
        });
    }

    private static int requestSilentServer(CommandSourceStack source, String command) {
        ActionResult<AdministrativeExecutionService.Preview> preview =
                KernelServices.administrativeExecution().preview(
                        source,
                        command,
                        AdministrativeExecutionService.Context.SILENT_SERVER,
                        CommandWrapperService.Origin.DIRECT);
        if (!preview.successful()) {
            return fail(source, "Silent server command denied. " + preview.detail());
        }
        boolean acceptUnsuppressible = has(source, "commands.silent.unsuppressible");
        if (preview.value().silenceCapability()
                != AdministrativeExecutionService.SilenceCapability.SUPPORTED
                && !acceptUnsuppressible) {
            return fail(source, "This command can create independent output.");
        }
        ConfirmationService.Request request = confirmation(
                source,
                "sef:silent.server",
                commandMetadata(preview.value()),
                List.of());
        ActionResult<ConfirmationService.IssuedToken> issued =
                KernelServices.confirmations().issue(request, Duration.ofSeconds(60));
        if (!issued.successful()) {
            return fail(source, "A silent server confirmation token could not be issued.");
        }
        success(source, "Confirmation required. /silent server confirm "
                + issued.value().token() + " " + preview.value().normalizedCommand());
        return 1;
    }

    private static int confirmSilentServer(
            CommandSourceStack source,
            String token,
            String command
    ) {
        ActionResult<AdministrativeExecutionService.Preview> preview =
                KernelServices.administrativeExecution().preview(
                        source,
                        command,
                        AdministrativeExecutionService.Context.SILENT_SERVER,
                        CommandWrapperService.Origin.DIRECT);
        if (!preview.successful()) {
            return fail(source, "Silent server preflight changed or was denied.");
        }
        boolean acceptUnsuppressible = has(source, "commands.silent.unsuppressible");
        if (preview.value().silenceCapability()
                != AdministrativeExecutionService.SilenceCapability.SUPPORTED
                && !acceptUnsuppressible) {
            return fail(source, "This command can create independent output.");
        }
        ConfirmationService.Request request = confirmation(
                source,
                "sef:silent.server",
                commandMetadata(preview.value()),
                List.of());
        ActionResult<ConfirmationService.Request> consumed =
                KernelServices.confirmations().consume(token, request);
        if (!consumed.successful()) {
            return fail(source, "Silent server confirmation is invalid, expired, used, or changed.");
        }
        return execute(source, "sef:silent.server", commandMetadata(preview.value()), () -> {
            ActionResult<Integer> result = KernelServices.administrativeExecution().silentServer(
                    source,
                    preview.value().normalizedCommand(),
                    acceptUnsuppressible);
            if (!result.successful()) {
                return fail(source, "Silent server command failed. " + result.detail());
            }
            success(source, "Server source feedback was suppressed. mandatory observation remains active.");
            return Math.max(1, result.value());
        });
    }

    private static boolean runRootAllowed(CommandSourceStack source, String root) {
        if (PermissionService.isConsole(source)) {
            return true;
        }
        ServerPlayer player = source.getPlayer();
        return has(source, "commands.run.root.any")
                || player != null && DynamicPermissionService.has(
                        player,
                        "sef.commands.run.root." + root.toLowerCase(Locale.ROOT));
    }

    private static boolean aliasAvailable(
            CommandSourceStack source,
            AliasCompiler.AliasDefinition registered
    ) {
        AliasCompiler.AliasDefinition current =
                KernelServices.aliases().findRoot(registered.root()).orElse(null);
        if (current == null
                || current.revision() != registered.revision()
                || !current.id().equals(registered.id())) {
            return false;
        }
        if (!current.additionalPermissionId().isBlank()
                && !hasQualified(source, current.additionalPermissionId())) {
            return false;
        }
        if (current.kind() == AliasCompiler.AliasKind.ACTION) {
            return can(source, current.targetId());
        }
        return can(source, "sef:alias.run") && can(source, "sef:bundle.run");
    }

    private static boolean eligibleTargets(
            CommandSourceStack source,
            Collection<ServerPlayer> targets,
            String exemption,
            String exemptionBypass,
            String hierarchyBypass
    ) {
        return targets.stream().allMatch(target -> eligibleTarget(
                source,
                target,
                exemption,
                exemptionBypass,
                hierarchyBypass));
    }

    private static boolean eligibleTarget(
            CommandSourceStack source,
            ServerPlayer target,
            String exemption,
            String exemptionBypass,
            String hierarchyBypass
    ) {
        ServerPlayer actor = source.getPlayer();
        if (actor != null && VanishUtil.isVanished(target, actor)) {
            return false;
        }
        return PlayerTargetPolicy.decide(
                source,
                target,
                permission(hierarchyBypass),
                permission(exemption),
                permission(exemptionBypass),
                true,
                true).allowed();
    }

    private static ConfirmationService.Request confirmation(
            CommandSourceStack source,
            String action,
            Map<String, String> parameters,
            List<UUID> targets
    ) {
        return new ConfirmationService.Request(
                actorId(source),
                action,
                parameters,
                targets,
                "",
                0L,
                0L,
                0L,
                KernelServices.commandPolicies().revision());
    }

    private static Map<String, String> commandMetadata(
            AdministrativeExecutionService.Preview preview
    ) {
        return Map.of(
                "root", preview.root(),
                "command_length", Integer.toString(preview.normalizedCommand().length()),
                "command_digest", digest(preview.normalizedCommand()),
                "context", preview.context().name().toLowerCase(Locale.ROOT));
    }

    private static Map<String, String> sudoCommandMetadata(SudoPreflight preflight, boolean delegated) {
        Map<String, String> metadata = new LinkedHashMap<>(commandMetadata(preflight.preview()));
        metadata.put("permission_mode", delegated ? "delegated_once" : "target");
        if (delegated) {
            AdministrativeExecutionService.DelegationProfile profile =
                    preflight.delegationProfile();
            metadata.put("canonical_action", profile.canonicalActionId());
            metadata.put("profile", profile.id());
            metadata.put("profile_revision", Long.toString(profile.revision()));
            metadata.put("command_tree_revision", Long.toString(preflight.commandTreeRevision()));
            metadata.put("target_session_revision", Long.toString(preflight.targetSessionRevision()));
            metadata.put("sudo_policy_revision", Long.toString(preflight.sudoPolicyRevision()));
            metadata.put("permission_provider_revision", Long.toString(
                    preflight.permissionProviderRevision()));
            metadata.put("feature_revision", Long.toString(preflight.featureRevision()));
            metadata.put("configuration_revision", Long.toString(
                    preflight.configurationRevision()));
            metadata.put("temporary_vanilla_level", Integer.toString(
                    profile.maximumTemporaryVanillaPermissionLevel()));
            metadata.put("temporary_sef_permissions", profile.temporarySefPermissionIds().stream()
                    .sorted()
                    .collect(java.util.stream.Collectors.joining(",")));
            metadata.put("adapter_capabilities", profile.approvedAdapterCapabilities().stream()
                    .sorted()
                    .collect(java.util.stream.Collectors.joining(",")));
        }
        return Map.copyOf(metadata);
    }

    private static Map<String, String> sudoExecutionMetadata(
            SudoPreflight preflight,
            EphemeralExecutionGrant grant
    ) {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("root", preflight.preview().root());
        metadata.put("permission_mode", grant == null ? "target" : "delegated_once");
        metadata.put(
                "command_length",
                Integer.toString(preflight.preview().normalizedCommand().length()));
        metadata.put("command_digest", digest(preflight.preview().normalizedCommand()));
        metadata.put("audit_correlation", preflight.preview().correlationId().toString());
        if (grant != null) {
            metadata.put("grant_id", grant.grantId().toString());
            metadata.put("profile", grant.profileId());
            metadata.put("profile_revision", Long.toString(grant.profileRevision()));
            metadata.put(
                    "temporary_vanilla_level",
                    Integer.toString(grant.maximumTemporaryVanillaPermissionLevel()));
            metadata.put("temporary_sef_permissions", grant.temporarySefPermissionIds().stream()
                    .sorted()
                    .collect(java.util.stream.Collectors.joining(",")));
            metadata.put("adapter_capabilities", grant.approvedAdapterCapabilities().stream()
                    .sorted()
                    .collect(java.util.stream.Collectors.joining(",")));
        }
        return Map.copyOf(metadata);
    }

    private static int dispatch(
            CommandSourceStack reporter,
            CommandSourceStack executionSource,
            String command,
            String label
    ) {
        try {
            int value = reporter.getServer().getCommands().getDispatcher().execute(
                    command,
                    executionSource);
            success(reporter, label + " completed with result " + value + ".");
            return Math.max(1, value);
        } catch (Exception exception) {
            return fail(reporter, label + " failed.");
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> revisionAction(
            String literal,
            String action,
            RevisionOperation operation
    ) {
        return Commands.literal(literal)
                .requires(source -> can(source, action))
                .then(Commands.argument("id", StringArgumentType.word())
                        .suggests((context, builder) -> suggestAliasIds(builder, true))
                        .then(Commands.argument("revision", LongArgumentType.longArg(1))
                                .executes(context -> execute(
                                        context.getSource(),
                                        action,
                                        Map.of("id", StringArgumentType.getString(context, "id")),
                                        () -> operation.apply(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "id"),
                                                LongArgumentType.getLong(context, "revision"))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> bundleRevisionAction(
            String literal,
            String action,
            RevisionOperation operation
    ) {
        return Commands.literal(literal)
                .requires(source -> can(source, action))
                .then(Commands.argument("id", StringArgumentType.word())
                        .suggests((context, builder) -> suggestBundleIds(builder, true))
                        .then(Commands.argument("revision", LongArgumentType.longArg(1))
                                .executes(context -> execute(
                                        context.getSource(),
                                        action,
                                        Map.of("id", StringArgumentType.getString(context, "id")),
                                        () -> operation.apply(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "id"),
                                                LongArgumentType.getLong(context, "revision"))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> profileRevisionAction(
            String literal,
            String action,
            RevisionOperation operation
    ) {
        return Commands.literal(literal)
                .requires(source -> can(source, action))
                .then(Commands.argument("id", StringArgumentType.word())
                        .suggests((context, builder) -> suggestProfileIds(builder, true))
                        .then(Commands.argument("revision", LongArgumentType.longArg(1))
                                .executes(context -> execute(
                                        context.getSource(),
                                        action,
                                        Map.of("id", StringArgumentType.getString(context, "id")),
                                        () -> operation.apply(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "id"),
                                                LongArgumentType.getLong(context, "revision"))))));
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions>
    suggestAliasIds(com.mojang.brigadier.suggestion.SuggestionsBuilder builder, boolean includeDrafts) {
        List<String> ids = new ArrayList<>(KernelServices.aliases().published().stream()
                .map(AliasCompiler.AliasDefinition::id)
                .toList());
        if (includeDrafts) {
            ids.addAll(KernelServices.aliases().drafts().stream()
                    .map(AliasCompiler.AliasDefinition::id)
                    .toList());
        }
        return SharedSuggestionProvider.suggest(ids, builder);
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions>
    suggestBundleIds(com.mojang.brigadier.suggestion.SuggestionsBuilder builder, boolean includeDrafts) {
        List<String> ids = new ArrayList<>(KernelServices.bundles().publications().stream()
                .map(BundleCompiler.BundleDefinition::id)
                .toList());
        if (includeDrafts) {
            ids.addAll(KernelServices.bundles().drafts().stream()
                    .map(BundleCompiler.BundleDefinition::id)
                    .toList());
        }
        return SharedSuggestionProvider.suggest(ids, builder);
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions>
    suggestProfileIds(com.mojang.brigadier.suggestion.SuggestionsBuilder builder, boolean includeDrafts) {
        List<String> ids = new ArrayList<>(KernelServices.commandProfiles().publications().stream()
                .map(CommandProfileService.CommandProfile::id)
                .toList());
        if (includeDrafts) {
            ids.addAll(KernelServices.commandProfiles().drafts().stream()
                    .map(CommandProfileService.CommandProfile::id)
                    .toList());
        }
        return SharedSuggestionProvider.suggest(ids, builder);
    }

    private static Set<String> csv(String value) {
        String normalized = Objects.requireNonNullElse(value, "").trim();
        if (normalized.isBlank() || normalized.equals("-")) {
            return Set.of();
        }
        Set<String> result = java.util.Arrays.stream(normalized.split(","))
                .map(String::trim)
                .map(part -> part.toLowerCase(Locale.ROOT))
                .filter(part -> part.matches("[a-z][a-z0-9_.-]{0,63}"))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        if (result.size() > 32) {
            throw new IllegalArgumentException("Too many command profile arguments");
        }
        return result;
    }

    private static Map<String, String> parseBindings(String input) {
        String normalized = Objects.requireNonNullElse(input, "").trim();
        if (normalized.isBlank() || normalized.equals("-")) {
            return Map.of();
        }
        Map<String, String> bindings = new LinkedHashMap<>();
        for (String entry : normalized.split(",")) {
            int separator = entry.indexOf('=');
            if (separator < 1) {
                throw new IllegalArgumentException("Bindings must use key=value pairs");
            }
            String key = entry.substring(0, separator).trim().toLowerCase(Locale.ROOT);
            String value = entry.substring(separator + 1).trim();
            if (!key.matches("[a-z][a-z0-9_.-]{0,63}")
                    || value.length() > 256
                    || value.codePoints().anyMatch(Character::isISOControl)
                    || bindings.putIfAbsent(key, value) != null) {
                throw new IllegalArgumentException("Binding is invalid");
            }
            if (bindings.size() > 32) {
                throw new IllegalArgumentException("Too many bindings");
            }
        }
        return Map.copyOf(bindings);
    }

    private static String boundedDefinitionText(String value, int maximum) {
        String bounded = Objects.requireNonNull(value, "value").trim();
        if (bounded.isBlank()
                || bounded.length() > maximum
                || bounded.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Definition text is outside bounds");
        }
        return bounded;
    }

    private static boolean wrapperAction(String action) {
        String normalized = Objects.requireNonNullElse(action, "").toLowerCase(Locale.ROOT);
        return normalized.startsWith("sef:alias.")
                || normalized.startsWith("sef:bundle.")
                || normalized.startsWith("sef:profile.")
                || normalized.startsWith("sef:sudo.")
                || normalized.startsWith("sef:run.")
                || normalized.startsWith("sef:silent.");
    }

    private static boolean parseState(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "on", "true", "enable", "enabled" -> true;
            case "off", "false", "disable", "disabled" -> false;
            default -> throw new IllegalArgumentException("State must be on or off");
        };
    }

    private static boolean parseDelegatedMode(String value) {
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "respect", "false" -> false;
            case "delegate", "true" -> true;
            default -> throw new IllegalArgumentException(
                    "Permission mode must be respect, delegate, false, or true.");
        };
    }

    private static Collection<ServerPlayer> selfTargets(CommandSourceStack source) {
        return source.getPlayer() == null ? List.of() : List.of(source.getPlayer());
    }

    private static UUID actorId(CommandSourceStack source) {
        return source.getEntity() == null
                ? UUID.nameUUIDFromBytes(
                ("sef:source:" + source.getTextName()).getBytes(StandardCharsets.UTF_8))
                : source.getEntity().getUUID();
    }

    private static boolean can(CommandSourceStack source, String action) {
        return KernelCommandExecutor.canUse(source, action);
    }

    private static boolean canAny(CommandSourceStack source, String... actions) {
        for (String action : actions) {
            if (can(source, action)) {
                return true;
            }
        }
        return false;
    }

    private static boolean has(CommandSourceStack source, String permission) {
        PermissionNode<Boolean> node = permission(permission);
        return node != null && PermissionService.has(source, node);
    }

    private static boolean hasQualified(CommandSourceStack source, String permission) {
        PermissionNode<Boolean> node = KernelServices.permissionNode(permission);
        if (node != null) {
            return PermissionService.has(source, node);
        }
        return source.getPlayer() != null
                && DynamicPermissionService.has(source.getPlayer(), permission);
    }

    private static PermissionNode<Boolean> permission(String id) {
        return PermissionsHandler.phasePermission(id);
    }

    private static int execute(
            CommandSourceStack source,
            String action,
            Map<String, String> parameters,
            java.util.function.IntSupplier operation
    ) {
        return KernelCommandExecutor.execute(source, action, parameters, operation);
    }

    private static int execute(
            CommandSourceStack source,
            String action,
            Map<String, String> parameters,
            List<UUID> targets,
            java.util.function.IntSupplier operation
    ) {
        return KernelCommandExecutor.execute(source, action, parameters, targets, false, operation);
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

    private static String digest(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    @FunctionalInterface
    private interface RevisionOperation {
        int apply(CommandSourceStack source, String id, long revision);
    }

    private record SudoPreflight(
            boolean allowed,
            String detail,
            AdministrativeExecutionService.Preview preview,
            AdministrativeExecutionService.DelegationProfile delegationProfile,
            long commandTreeRevision,
            long targetSessionRevision,
            long sudoPolicyRevision,
            long permissionProviderRevision,
            long featureRevision,
            long configurationRevision
    ) {
        private static SudoPreflight denied(String detail) {
            return new SudoPreflight(false, detail, null, null, 0L, 0L, 0L, 0L, 0L, 0L);
        }

        private static SudoPreflight respect(AdministrativeExecutionService.Preview preview) {
            return new SudoPreflight(true, "", preview, null, 0L, 0L, 0L, 0L, 0L, 0L);
        }

        private static SudoPreflight delegated(
                AdministrativeExecutionService.Preview preview,
                AdministrativeExecutionService.DelegationProfile profile,
                long commandTreeRevision,
                long targetSessionRevision,
                long sudoPolicyRevision,
                long permissionProviderRevision,
                long featureRevision,
                long configurationRevision
        ) {
            return new SudoPreflight(
                    true,
                    "",
                    preview,
                    profile,
                    commandTreeRevision,
                    targetSessionRevision,
                    sudoPolicyRevision,
                    permissionProviderRevision,
                    featureRevision,
                    configurationRevision);
        }

        private boolean revisionsCurrent(ServerPlayer target) {
            AdministrativeExecutionService.Settings settings =
                    KernelServices.administrativeExecution().settings();
            AdministrativeExecutionService.DelegationProfile currentProfile =
                    settings.profileForRoot(preview.root()).orElse(null);
            return target.server.getPlayerList().getPlayer(target.getUUID()) == target
                    && SefGuiServer.targetSessionRevision(target.getUUID()) == targetSessionRevision
                    && KernelServices.sudoPolicies().policy(target.getUUID()).revision()
                    == sudoPolicyRevision
                    && PermissionService.providerRevision() == permissionProviderRevision
                    && KernelServices.configurationRevision() == featureRevision
                    && KernelServices.configurationRevision() == configurationRevision
                    && KernelServices.administrativeExecution().commandTreeRevision()
                    == commandTreeRevision
                    && currentProfile != null
                    && currentProfile.id().equals(delegationProfile.id())
                    && currentProfile.revision() == delegationProfile.revision();
        }
    }
}
