package com.enviouse.sef.control;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.gui.protocol.SefSessionManager;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.permissions.PermissionService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
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

public final class AccessGrantCommands {
    private static final UUID CONSOLE_ID = new UUID(0L, 0L);
    private static final int PAGE_SIZE = 8;

    private AccessGrantCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("accessgrant")
                .requires(source -> hasAny(
                        source,
                        "commands.accessgrant.profiles",
                        "commands.accessgrant.profile.inspect",
                        "commands.accessgrant.profile.publish",
                        "commands.accessgrant.profile.retire",
                        "commands.accessgrant.preview",
                        "commands.accessgrant.create",
                        "commands.accessgrant.renew",
                        "commands.accessgrant.suspend",
                        "commands.accessgrant.resume",
                        "commands.accessgrant.revoke",
                        "commands.accessgrant.inspect.self",
                        "commands.accessgrant.inspect.others",
                        "commands.accessgrant.list",
                        "commands.accessgrant.expiring",
                        "commands.accessgrant.reconcile",
                        "commands.accessgrant.history"))
                .then(Commands.literal("profiles")
                        .requires(source -> has(source, "commands.accessgrant.profiles"))
                        .executes(context -> profiles(context.getSource(), 1))
                        .then(Commands.argument("page", LongArgumentType.longArg(1L, 1_000_000L))
                                .executes(context -> profiles(
                                        context.getSource(),
                                        Math.toIntExact(LongArgumentType.getLong(context, "page"))))))
                .then(Commands.literal("profile")
                        .then(Commands.literal("inspect")
                                .requires(source -> has(source, "commands.accessgrant.profile.inspect"))
                                .then(Commands.argument("profile", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                KernelServices.accessLeases().profiles().stream()
                                                        .map(AccessLeaseRepository.Profile::id),
                                                builder))
                                        .executes(context -> inspectProfile(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "profile")))))
                        .then(Commands.literal("publish")
                                .requires(source -> has(source, "commands.accessgrant.profile.publish"))
                                .then(Commands.argument("profile", StringArgumentType.word())
                                        .then(Commands.argument("maximumDuration", StringArgumentType.word())
                                                .then(Commands.argument("protected", BoolArgumentType.bool())
                                                        .then(Commands.argument("separationRequired", BoolArgumentType.bool())
                                                                .then(Commands.argument(
                                                                                "permissions",
                                                                                StringArgumentType.greedyString())
                                                                        .executes(context -> publishProfile(
                                                                                context.getSource(),
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "profile"),
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "maximumDuration"),
                                                                                BoolArgumentType.getBool(
                                                                                        context,
                                                                                        "protected"),
                                                                                BoolArgumentType.getBool(
                                                                                        context,
                                                                                        "separationRequired"),
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "permissions")))))))))
                        .then(Commands.literal("retire")
                                .requires(source -> has(source, "commands.accessgrant.profile.retire"))
                                .then(Commands.argument("profile", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                KernelServices.accessLeases().profiles().stream()
                                                        .map(AccessLeaseRepository.Profile::id),
                                                builder))
                                        .then(Commands.argument("revision", LongArgumentType.longArg(1L))
                                                .executes(context -> retireProfile(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "profile"),
                                                        LongArgumentType.getLong(context, "revision")))))))
                .then(Commands.literal("preview")
                        .requires(source -> has(source, "commands.accessgrant.preview"))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("profile", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                KernelServices.accessLeases().profiles().stream()
                                                        .filter(AccessLeaseRepository.Profile::active)
                                                        .map(AccessLeaseRepository.Profile::id),
                                                builder))
                                        .then(Commands.argument("duration", StringArgumentType.word())
                                                .executes(context -> preview(
                                                        context.getSource(),
                                                        EntityArgument.getPlayer(context, "player"),
                                                        StringArgumentType.getString(context, "profile"),
                                                        StringArgumentType.getString(context, "duration")))))))
                .then(Commands.literal("create")
                        .requires(source -> has(source, "commands.accessgrant.create"))
                        .then(Commands.literal("approved")
                                .then(Commands.argument("approval", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                KernelServices.approvals().requests(
                                                                actorId(context.getSource()),
                                                                ApprovalRepository.ApprovalState.APPROVED)
                                                        .stream()
                                                        .filter(request -> request.actionId().equals(
                                                                "sef:accessgrant.create"))
                                                        .map(request -> request.id().toString()),
                                                builder))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .then(Commands.argument("profile", StringArgumentType.word())
                                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                                KernelServices.accessLeases().profiles().stream()
                                                                        .filter(AccessLeaseRepository.Profile::active)
                                                                        .map(AccessLeaseRepository.Profile::id),
                                                                builder))
                                                        .then(Commands.argument(
                                                                        "duration",
                                                                        StringArgumentType.word())
                                                                .then(Commands.argument(
                                                                                "reason",
                                                                                StringArgumentType.greedyString())
                                                                        .executes(context -> create(
                                                                                context.getSource(),
                                                                                EntityArgument.getPlayer(
                                                                                        context,
                                                                                        "player"),
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "profile"),
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "duration"),
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "reason"),
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "approval")))))))))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("profile", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                KernelServices.accessLeases().profiles().stream()
                                                        .filter(AccessLeaseRepository.Profile::active)
                                                        .map(AccessLeaseRepository.Profile::id),
                                                builder))
                                        .then(Commands.argument("duration", StringArgumentType.word())
                                                .then(Commands.argument("reason", StringArgumentType.greedyString())
                                                        .executes(context -> create(
                                                                context.getSource(),
                                                                EntityArgument.getPlayer(context, "player"),
                                                                StringArgumentType.getString(context, "profile"),
                                                                StringArgumentType.getString(context, "duration"),
                                                                StringArgumentType.getString(context, "reason"),
                                                                null)))))))
                .then(Commands.literal("renew")
                        .requires(source -> has(source, "commands.accessgrant.renew"))
                        .then(Commands.argument("lease", StringArgumentType.word())
                                .suggests(AccessGrantCommands::suggestLeaseIds)
                                .then(Commands.argument("duration", StringArgumentType.word())
                                        .then(Commands.argument("reason", StringArgumentType.greedyString())
                                                .executes(context -> renew(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "lease"),
                                                        StringArgumentType.getString(context, "duration"),
                                                        StringArgumentType.getString(context, "reason")))))))
                .then(Commands.literal("suspend")
                        .requires(source -> has(source, "commands.accessgrant.suspend"))
                        .then(Commands.argument("lease", StringArgumentType.word())
                                .suggests(AccessGrantCommands::suggestLeaseIds)
                                .then(Commands.argument("reason", StringArgumentType.greedyString())
                                        .executes(context -> transition(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "lease"),
                                                AccessLeaseRepository.LeaseState.SUSPENDED,
                                                StringArgumentType.getString(context, "reason"))))))
                .then(Commands.literal("resume")
                        .requires(source -> has(source, "commands.accessgrant.resume"))
                        .then(Commands.argument("lease", StringArgumentType.word())
                                .suggests(AccessGrantCommands::suggestLeaseIds)
                                .executes(context -> transition(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "lease"),
                                        AccessLeaseRepository.LeaseState.ACTIVE,
                                        ""))))
                .then(Commands.literal("revoke")
                        .requires(source -> has(source, "commands.accessgrant.revoke"))
                        .then(Commands.argument("lease", StringArgumentType.word())
                                .suggests(AccessGrantCommands::suggestLeaseIds)
                                .then(Commands.argument("reason", StringArgumentType.greedyString())
                                        .executes(context -> transition(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "lease"),
                                                AccessLeaseRepository.LeaseState.REVOKED,
                                                StringArgumentType.getString(context, "reason"))))))
                .then(listNode())
                .then(Commands.literal("inspect")
                        .then(Commands.argument("lease", StringArgumentType.word())
                                .suggests(AccessGrantCommands::suggestLeaseIds)
                                .executes(context -> inspect(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "lease")))))
                .then(Commands.literal("expiring")
                        .requires(source -> has(source, "commands.accessgrant.expiring"))
                        .executes(context -> expiring(context.getSource(), Duration.ofHours(24)))
                        .then(Commands.argument("duration", StringArgumentType.word())
                                .executes(context -> expiring(
                                        context.getSource(),
                                        parseDuration(StringArgumentType.getString(context, "duration"))))))
                .then(Commands.literal("reconcile")
                        .requires(source -> has(source, "commands.accessgrant.reconcile"))
                        .executes(context -> reconcile(context.getSource())))
                .then(Commands.literal("history")
                        .requires(source -> has(source, "commands.accessgrant.history"))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> history(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "player"),
                                        1))
                                .then(Commands.argument("page", LongArgumentType.longArg(1L, 1_000_000L))
                                        .executes(context -> history(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "player"),
                                                Math.toIntExact(LongArgumentType.getLong(context, "page"))))))));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> listNode() {
        return Commands.literal("list")
                .requires(source -> has(source, "commands.accessgrant.list"))
                .executes(context -> list(context.getSource(), null, null, null, 1))
                .then(Commands.literal("player")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> list(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "player").getUUID(),
                                        null,
                                        null,
                                        1))))
                .then(Commands.literal("profile")
                        .then(Commands.argument("profile", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        KernelServices.accessLeases().profiles().stream()
                                                .map(AccessLeaseRepository.Profile::id),
                                        builder))
                                .executes(context -> list(
                                        context.getSource(),
                                        null,
                                        StringArgumentType.getString(context, "profile"),
                                        null,
                                        1))))
                .then(Commands.literal("state")
                        .then(Commands.argument("state", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        Arrays.stream(AccessLeaseRepository.LeaseState.values())
                                                .map(value -> value.name().toLowerCase(Locale.ROOT)),
                                        builder))
                                .executes(context -> list(
                                        context.getSource(),
                                        null,
                                        null,
                                        parseState(StringArgumentType.getString(context, "state")),
                                        1))));
    }

    private static int profiles(CommandSourceStack source, int page) {
        return page(
                source,
                "access lease profiles",
                KernelServices.accessLeases().profiles(),
                page,
                profile -> "&e" + profile.id()
                        + " &8| &7r" + profile.revision()
                        + " &8| &f" + profile.permissions().size() + " permissions"
                        + " &8| &7" + (profile.active() ? "active" : "retired")
                        + (profile.protectedProfile() ? " &8| &cprotected" : ""));
    }

    private static int inspectProfile(CommandSourceStack source, String profileId) {
        var profile = KernelServices.accessLeases().profile(profileId).orElse(null);
        if (profile == null) {
            return fail(source, "access lease profile not found");
        }
        info(source, "&e" + profile.id() + " &8| &7revision &f" + profile.revision());
        info(source, "&7maximum &f" + profile.maximumDurationSeconds() + " seconds"
                + " &8| &7scope &f" + profile.scope().kind().name().toLowerCase(Locale.ROOT));
        info(source, "&7protected &f" + profile.protectedProfile()
                + " &8| &7separation &f" + profile.separationRequired()
                + " &8| &7active &f" + profile.active());
        profile.permissions().stream().sorted().forEach(permission -> info(source, "&8• &f" + permission));
        profile.quotas().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> info(source, "&8• &f" + entry.getKey() + " &7" + entry.getValue()));
        return 1;
    }

    private static int publishProfile(
            CommandSourceStack source,
            String profileId,
            String maximumDuration,
            boolean protectedProfile,
            boolean separationRequired,
            String permissionList
    ) {
        Duration duration;
        Set<String> permissions;
        try {
            duration = parseDuration(maximumDuration);
            permissions = Arrays.stream(permissionList.split("[,\\s]+"))
                    .map(String::strip)
                    .filter(value -> !value.isBlank())
                    .collect(Collectors.toUnmodifiableSet());
        } catch (IllegalArgumentException exception) {
            return fail(source, exception.getMessage());
        }
        if (protectedProfile && !has(source, "commands.accessgrant.protected")) {
            return fail(source, "protected access lease profile permission is required");
        }
        if (!mayDelegate(source, permissions)) {
            return 0;
        }
        return execute(
                source,
                "sef:accessgrant.profile.publish",
                "commands.accessgrant.profile.publish",
                Map.of("profile", profileId, "permission_count", Integer.toString(permissions.size())),
                List.of(),
                () -> {
                    var result = KernelServices.accessLeases().commit(() ->
                            KernelServices.accessLeases().publishProfile(
                                    profileId,
                                    actorId(source),
                                    permissions,
                                    Map.of(),
                                    duration,
                                    protectedProfile,
                                    separationRequired,
                                    AccessLeaseRepository.Scope.global()));
                    if (!result.successful()) {
                        return fail(source, result.detail());
                    }
                    success(source, "access lease profile published, revision " + result.value().revision());
                    return 1;
                });
    }

    private static int retireProfile(CommandSourceStack source, String profileId, long revision) {
        return execute(
                source,
                "sef:accessgrant.profile.retire",
                "commands.accessgrant.profile.retire",
                Map.of("profile", profileId, "revision", Long.toString(revision)),
                List.of(),
                () -> {
                    var result = KernelServices.accessLeases().commit(() ->
                            KernelServices.accessLeases().retireProfile(profileId, actorId(source), revision));
                    if (!result.successful()) {
                        return fail(source, result.detail());
                    }
                    success(source, "access lease profile retired");
                    return 1;
                });
    }

    private static int preview(
            CommandSourceStack source,
            ServerPlayer target,
            String profileId,
            String durationText
    ) {
        if (!mayTarget(source, target)) {
            return 0;
        }
        Duration duration;
        try {
            duration = parseDuration(durationText);
        } catch (IllegalArgumentException exception) {
            return fail(source, exception.getMessage());
        }
        var profile = KernelServices.accessLeases().profile(profileId).orElse(null);
        if (profile == null || !profile.active()) {
            return fail(source, "active access lease profile not found");
        }
        if (duration.getSeconds() > profile.maximumDurationSeconds()) {
            return fail(source, "requested lease duration exceeds profile maximum");
        }
        if (profile.protectedProfile() && !has(source, "commands.accessgrant.protected")) {
            return fail(source, "protected access lease profile permission is required");
        }
        info(source, "&eaccess lease preview");
        info(source, "&7player &f" + target.getGameProfile().getName()
                + " &8| &7profile &f" + profile.id() + " r" + profile.revision());
        info(source, "&7expires &f" + Instant.now().plus(duration));
        profile.permissions().stream().sorted().forEach(permission -> info(source, "&a+ &f" + permission));
        return 1;
    }

    private static int create(
            CommandSourceStack source,
            ServerPlayer target,
            String profileId,
            String durationText,
            String reason,
            String approvalInput
    ) {
        if (!mayTarget(source, target)) {
            return 0;
        }
        Duration duration;
        try {
            duration = parseDuration(durationText);
        } catch (IllegalArgumentException exception) {
            return fail(source, exception.getMessage());
        }
        var profile = KernelServices.accessLeases().profile(profileId).orElse(null);
        if (profile == null || !profile.active()) {
            return fail(source, "active access lease profile not found");
        }
        if (profile.protectedProfile() && !has(source, "commands.accessgrant.protected")) {
            return fail(source, "protected access lease profile permission is required");
        }
        UUID approvalId = approvalInput == null ? null : parseUuid(approvalInput);
        if (approvalInput != null && approvalId == null) {
            return fail(source, "approval request id is invalid");
        }
        if (profile.separationRequired() && approvalId == null) {
            return fail(source, "this profile requires a completed two person approval");
        }
        if (!profile.separationRequired() && approvalId != null) {
            return fail(source, "this profile does not require two person approval");
        }
        if (!mayDelegate(source, profile.permissions())) {
            return 0;
        }
        String approvalHash = ApprovalRepository.payloadHash(Map.of(
                "subject", target.getUUID().toString(),
                "profile", profile.id(),
                "profile_revision", Long.toString(profile.revision()),
                "duration_seconds", Long.toString(duration.toSeconds())));
        return execute(
                source,
                "sef:accessgrant.create",
                "commands.accessgrant.create",
                Map.of(
                        "profile", profile.id(),
                        "duration_seconds", Long.toString(duration.toSeconds()),
                        "subject", target.getUUID().toString(),
                        "approval", approvalId == null ? "none" : approvalId.toString()),
                List.of(target.getUUID()),
                () -> {
                    if (approvalId != null) {
                        var approval = KernelServices.approvals().commit(() ->
                                KernelServices.approvals().consume(
                                        approvalId,
                                        actorId(source),
                                        "sef:accessgrant.create",
                                        approvalHash));
                        if (!approval.successful()) {
                            return fail(source, approval.detail());
                        }
                    }
                    var result = KernelServices.accessLeases().commit(() ->
                            KernelServices.accessLeases().create(
                                    profile.id(),
                                    target.getUUID(),
                                    actorId(source),
                                    duration,
                                    reason,
                                    "internal",
                                    Long.toString(profile.revision())));
                    if (!result.successful()) {
                        if (approvalId != null) {
                            var restored = KernelServices.approvals().commit(() ->
                                    KernelServices.approvals().restoreApproved(
                                            approvalId,
                                            actorId(source),
                                            "access lease creation failed"));
                            if (!restored.successful()) {
                                return fail(source, result.detail()
                                        + ", approval compensation failed, "
                                        + restored.detail());
                            }
                        }
                        return fail(source, result.detail());
                    }
                    refresh(target, "an access lease was granted until " + result.value().expiresAt());
                    success(source, "access lease created, " + result.value().id());
                    return 1;
                });
    }

    private static int renew(
            CommandSourceStack source,
            String leaseId,
            String durationText,
            String reason
    ) {
        UUID id;
        Duration duration;
        try {
            id = UUID.fromString(leaseId);
            duration = parseDuration(durationText);
        } catch (IllegalArgumentException exception) {
            return fail(source, "access lease id or duration is invalid");
        }
        var current = KernelServices.accessLeases().lease(id).orElse(null);
        if (current == null) {
            return fail(source, "access lease not found");
        }
        ServerPlayer target = source.getServer().getPlayerList().getPlayer(current.subjectId());
        if (target != null && !mayTarget(source, target)) {
            return 0;
        }
        return execute(
                source,
                "sef:accessgrant.renew",
                "commands.accessgrant.renew",
                Map.of("lease", id.toString(), "duration_seconds", Long.toString(duration.toSeconds())),
                List.of(current.subjectId()),
                () -> {
                    ActionResult<AccessLeaseRepository.Lease> result =
                            KernelServices.accessLeases().commit(() -> KernelServices.accessLeases().renew(
                                    id,
                                    actorId(source),
                                    duration,
                                    reason,
                                    current.revision()));
                    return finishMutation(source, result, "access lease renewed");
                });
    }

    private static int transition(
            CommandSourceStack source,
            String leaseId,
            AccessLeaseRepository.LeaseState state,
            String reason
    ) {
        UUID id;
        try {
            id = UUID.fromString(leaseId);
        } catch (IllegalArgumentException exception) {
            return fail(source, "access lease id is invalid");
        }
        var current = KernelServices.accessLeases().lease(id).orElse(null);
        if (current == null) {
            return fail(source, "access lease not found");
        }
        ServerPlayer target = source.getServer().getPlayerList().getPlayer(current.subjectId());
        if (target != null && !mayTarget(source, target)) {
            return 0;
        }
        String permission = switch (state) {
            case ACTIVE -> "commands.accessgrant.resume";
            case SUSPENDED -> "commands.accessgrant.suspend";
            case REVOKED -> "commands.accessgrant.revoke";
            case EXPIRED -> throw new IllegalArgumentException("expiry is automatic");
        };
        return execute(
                source,
                "sef:accessgrant." + state.name().toLowerCase(Locale.ROOT),
                permission,
                Map.of("lease", id.toString(), "state", state.name().toLowerCase(Locale.ROOT)),
                List.of(current.subjectId()),
                () -> {
                    var result = KernelServices.accessLeases().commit(() ->
                            KernelServices.accessLeases().transition(
                                    id,
                                    actorId(source),
                                    state,
                                    reason,
                                    current.revision()));
                    return finishMutation(
                            source,
                            result,
                            "access lease " + state.name().toLowerCase(Locale.ROOT));
                });
    }

    private static int finishMutation(
            CommandSourceStack source,
            ActionResult<AccessLeaseRepository.Lease> result,
            String message
    ) {
        if (!result.successful()) {
            return fail(source, result.detail());
        }
        ServerPlayer target = source.getServer().getPlayerList().getPlayer(result.value().subjectId());
        if (target != null) {
            refresh(target, message);
        }
        success(source, message);
        return 1;
    }

    private static int list(
            CommandSourceStack source,
            UUID subjectId,
            String profileId,
            AccessLeaseRepository.LeaseState state,
            int page
    ) {
        return page(
                source,
                "access leases",
                KernelServices.accessLeases().leases(subjectId, profileId, state),
                page,
                lease -> "&e" + lease.id()
                        + " &8| &f" + lease.profileId()
                        + " &8| &7" + lease.subjectId()
                        + " &8| &f" + lease.state().name().toLowerCase(Locale.ROOT)
                        + " &8| &7r" + lease.revision());
    }

    private static int inspect(CommandSourceStack source, String leaseId) {
        UUID id;
        try {
            id = UUID.fromString(leaseId);
        } catch (IllegalArgumentException exception) {
            return fail(source, "access lease id is invalid");
        }
        var lease = KernelServices.accessLeases().lease(id).orElse(null);
        if (lease == null) {
            return fail(source, "access lease not found");
        }
        ServerPlayer actor = source.getPlayer();
        boolean own = actor != null && actor.getUUID().equals(lease.subjectId());
        if (own && !has(source, "commands.accessgrant.inspect.self")
                || !own && !has(source, "commands.accessgrant.inspect.others")) {
            return fail(source, "you cannot inspect that access lease");
        }
        info(source, "&e" + lease.id() + " &8| &f" + lease.state().name().toLowerCase(Locale.ROOT));
        info(source, "&7profile &f" + lease.profileId() + " r" + lease.profileRevision()
                + " &8| &7lease revision &f" + lease.revision());
        info(source, "&7subject &f" + lease.subjectId()
                + " &8| &7issuer &f" + lease.issuerId());
        info(source, "&7starts &f" + lease.startsAt() + " &8| &7expires &f" + lease.expiresAt());
        info(source, "&7scope &f" + lease.scope().kind().name().toLowerCase(Locale.ROOT)
                + " &8| &7provider &f" + lease.provider()
                + " &8| &7cleanup pending &f" + lease.pendingProviderCleanup());
        return 1;
    }

    private static int expiring(CommandSourceStack source, Duration duration) {
        return page(
                source,
                "expiring access leases",
                KernelServices.accessLeases().expiring(duration),
                1,
                lease -> "&e" + lease.id() + " &8| &f" + lease.profileId()
                        + " &8| &7" + lease.subjectId()
                        + " &8| &f" + lease.expiresAt());
    }

    private static int reconcile(CommandSourceStack source) {
        return execute(
                source,
                "sef:accessgrant.reconcile",
                "commands.accessgrant.reconcile",
                Map.of(),
                List.of(),
                () -> {
                    final int[] pending = new int[1];
                    ActionResult<Integer> result = KernelServices.accessLeases().commit(() -> {
                        pending[0] = KernelServices.accessLeases().reconcile();
                        return ActionResult.success(pending[0]);
                    });
                    if (!result.successful()) {
                        return fail(source, result.detail());
                    }
                    success(source, "access lease reconciliation complete, pending cleanup " + pending[0]);
                    return 1;
                });
    }

    private static int history(CommandSourceStack source, ServerPlayer target, int page) {
        if (!mayTarget(source, target)) {
            return 0;
        }
        return page(
                source,
                "access lease history for " + target.getGameProfile().getName(),
                KernelServices.accessLeases().history(target.getUUID()),
                page,
                entry -> "&e" + entry.occurredAt()
                        + " &8| &f" + entry.action()
                        + " &8| &7" + entry.before().name().toLowerCase(Locale.ROOT)
                        + " to " + entry.after().name().toLowerCase(Locale.ROOT)
                        + " &8| &7r" + entry.leaseRevision());
    }

    private static boolean mayDelegate(CommandSourceStack source, Set<String> permissions) {
        ServerPlayer actor = source.getPlayer();
        if (actor == null && PermissionService.isConsole(source)) {
            return true;
        }
        if (actor == null) {
            fail(source, "only a player or dedicated server console can delegate capabilities");
            return false;
        }
        for (String permission : permissions) {
            PermissionNode<Boolean> node = PermissionsHandler.phasePermission(permission);
            if (node == null || !PermissionService.hasProviderOnly(actor, node)) {
                fail(source, "you cannot delegate " + permission);
                return false;
            }
        }
        return true;
    }

    private static boolean mayTarget(CommandSourceStack source, ServerPlayer target) {
        var decision = PlayerTargetPolicy.decide(
                source,
                target,
                permission("commands.accessgrant.hierarchy.override"),
                permission("commands.accessgrant.exempt"),
                permission("commands.accessgrant.exemption.override"),
                true,
                false);
        if (!decision.allowed()) {
            fail(source, decision.exempt()
                    ? "that player is exempt from access grants"
                    : "you cannot target that player");
            return false;
        }
        return true;
    }

    private static void refresh(ServerPlayer player, String notice) {
        KernelServices.warmups().clear(player.getUUID());
        KernelServices.quotas().invalidate();
        SefSessionManager.instance().refresh(player);
        player.server.getCommands().sendCommands(player);
        player.sendSystemMessage(TextFormatter.stringToFormattedText("&e" + notice));
    }

    private static Duration parseDuration(String value) {
        String normalized = value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("duration is required");
        }
        if (normalized.startsWith("p")) {
            Duration duration = Duration.parse(normalized.toUpperCase(Locale.ROOT));
            validateDuration(duration);
            return duration;
        }
        long multiplier = 1L;
        char suffix = normalized.charAt(normalized.length() - 1);
        if (Character.isLetter(suffix)) {
            normalized = normalized.substring(0, normalized.length() - 1);
            multiplier = switch (suffix) {
                case 's' -> 1L;
                case 'm' -> 60L;
                case 'h' -> 3_600L;
                case 'd' -> 86_400L;
                case 'w' -> 604_800L;
                default -> throw new IllegalArgumentException("duration unit must be s, m, h, d, or w");
            };
        }
        try {
            Duration duration = Duration.ofSeconds(Math.multiplyExact(Long.parseLong(normalized), multiplier));
            validateDuration(duration);
            return duration;
        } catch (ArithmeticException | NumberFormatException exception) {
            throw new IllegalArgumentException("duration is invalid");
        }
    }

    private static void validateDuration(Duration duration) {
        if (duration.isZero()
                || duration.isNegative()
                || duration.compareTo(AccessLeaseRepository.MAXIMUM_LEASE_DURATION) > 0) {
            throw new IllegalArgumentException("duration is outside the one second to thirty day limit");
        }
    }

    private static AccessLeaseRepository.LeaseState parseState(String value) {
        try {
            return AccessLeaseRepository.LeaseState.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("access lease state is invalid");
        }
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestLeaseIds(
            com.mojang.brigadier.context.CommandContext<CommandSourceStack> context,
            com.mojang.brigadier.suggestion.SuggestionsBuilder builder
    ) {
        return SharedSuggestionProvider.suggest(
                KernelServices.accessLeases().leases(null, null, null).stream()
                        .map(lease -> lease.id().toString()),
                builder);
    }

    private static <T> int page(
            CommandSourceStack source,
            String title,
            List<T> values,
            int requestedPage,
            java.util.function.Function<T, String> renderer
    ) {
        int pages = Math.max(1, (values.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int page = Math.max(1, Math.min(requestedPage, pages));
        info(source, "&6" + title + " &8| &7page &f" + page + "&7/&f" + pages
                + " &8| &7total &f" + values.size());
        int start = (page - 1) * PAGE_SIZE;
        values.stream().skip(start).limit(PAGE_SIZE).forEach(value -> info(source, renderer.apply(value)));
        return 1;
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

    private static UUID actorId(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        return player == null ? CONSOLE_ID : player.getUUID();
    }

    private static UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static boolean has(CommandSourceStack source, String permissionId) {
        PermissionNode<Boolean> node = permission(permissionId);
        return node != null && PermissionService.has(source, node);
    }

    private static boolean hasAny(CommandSourceStack source, String... permissionIds) {
        return Arrays.stream(permissionIds).anyMatch(permission -> has(source, permission));
    }

    private static PermissionNode<Boolean> permission(String permissionId) {
        return PermissionsHandler.phasePermission(permissionId);
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
