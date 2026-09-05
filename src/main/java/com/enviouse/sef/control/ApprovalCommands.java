package com.enviouse.sef.control;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.util.DurationParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class ApprovalCommands {
    private static final UUID CONSOLE_ID = new UUID(0L, 0L);
    private static final int PAGE_SIZE = 8;

    private ApprovalCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal("approval")
                .requires(source -> hasAny(
                        source,
                        "commands.approval.request",
                        "commands.approval.approve",
                        "commands.approval.revoke",
                        "commands.approval.inspect",
                        "commands.approval.list",
                        "commands.approval.history"))
                .executes(context -> list(context.getSource(), 1))
                .then(Commands.literal("request")
                        .requires(source -> has(source, "commands.approval.request"))
                        .then(Commands.literal("accessgrant")
                                .requires(source -> has(source, "commands.accessgrant.create"))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("profile", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                        KernelServices.accessLeases().profiles().stream()
                                                                .filter(AccessLeaseRepository.Profile::active)
                                                                .map(AccessLeaseRepository.Profile::id),
                                                        builder))
                                                .then(Commands.argument("leaseDuration", StringArgumentType.word())
                                                        .then(Commands.argument(
                                                                        "approvalDuration",
                                                                        StringArgumentType.word())
                                                                .then(Commands.argument(
                                                                                "reason",
                                                                                StringArgumentType.greedyString())
                                                                        .executes(context ->
                                                                                requestAccessGrant(
                                                                                        context.getSource(),
                                                                                        EntityArgument.getPlayer(
                                                                                                context,
                                                                                                "player"),
                                                                                        StringArgumentType.getString(
                                                                                                context,
                                                                                                "profile"),
                                                                                        StringArgumentType.getString(
                                                                                                context,
                                                                                                "leaseDuration"),
                                                                                        StringArgumentType.getString(
                                                                                                context,
                                                                                                "approvalDuration"),
                                                                                        StringArgumentType.getString(
                                                                                                context,
                                                                                                "reason")))))))))
                        .then(Commands.literal("generic")
                                .then(Commands.argument("action", StringArgumentType.word())
                                        .then(Commands.argument("payloadHash", StringArgumentType.word())
                                                .then(Commands.argument("duration", StringArgumentType.word())
                                                        .then(Commands.argument(
                                                                        "preview",
                                                                        StringArgumentType.greedyString())
                                                                .executes(context -> requestGeneric(
                                                                        context.getSource(),
                                                                        StringArgumentType.getString(
                                                                                context,
                                                                                "action"),
                                                                        StringArgumentType.getString(
                                                                                context,
                                                                                "payloadHash"),
                                                                        StringArgumentType.getString(
                                                                                context,
                                                                                "duration"),
                                                                        StringArgumentType.getString(
                                                                                context,
                                                                                "preview")))))))))
                .then(Commands.literal("approve")
                        .requires(source -> has(source, "commands.approval.approve"))
                        .then(Commands.argument("request", StringArgumentType.word())
                                .suggests(ApprovalCommands::suggestPending)
                                .then(Commands.argument("revision", LongArgumentType.longArg(1L))
                                        .executes(context -> approve(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "request"),
                                                LongArgumentType.getLong(context, "revision"),
                                                ""))
                                        .then(Commands.argument("note", StringArgumentType.greedyString())
                                                .executes(context -> approve(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "request"),
                                                        LongArgumentType.getLong(context, "revision"),
                                                        StringArgumentType.getString(context, "note")))))))
                .then(Commands.literal("revoke")
                        .requires(source -> has(source, "commands.approval.revoke"))
                        .then(Commands.argument("request", StringArgumentType.word())
                                .suggests(ApprovalCommands::suggestPending)
                                .then(Commands.argument("revision", LongArgumentType.longArg(1L))
                                        .then(Commands.argument("reason", StringArgumentType.greedyString())
                                                .executes(context -> revoke(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "request"),
                                                        LongArgumentType.getLong(context, "revision"),
                                                        StringArgumentType.getString(context, "reason")))))))
                .then(Commands.literal("inspect")
                        .requires(source -> has(source, "commands.approval.inspect"))
                        .then(Commands.argument("request", StringArgumentType.word())
                                .suggests(ApprovalCommands::suggestAll)
                                .executes(context -> inspect(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "request")))))
                .then(Commands.literal("list")
                        .requires(source -> has(source, "commands.approval.list"))
                        .executes(context -> list(context.getSource(), 1))
                        .then(Commands.argument("page", LongArgumentType.longArg(1L, 1_000_000L))
                                .executes(context -> list(
                                        context.getSource(),
                                        Math.toIntExact(LongArgumentType.getLong(context, "page"))))))
                .then(Commands.literal("history")
                        .requires(source -> has(source, "commands.approval.history"))
                        .then(Commands.argument("request", StringArgumentType.word())
                                .suggests(ApprovalCommands::suggestAll)
                                .executes(context -> history(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "request"),
                                        1))
                                .then(Commands.argument("page", LongArgumentType.longArg(1L, 1_000_000L))
                                        .executes(context -> history(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "request"),
                                                Math.toIntExact(LongArgumentType.getLong(context, "page")))))));
        dispatcher.register(root);
        dispatcher.register(Commands.literal("approvals")
                .requires(source -> has(source, "commands.approval.list"))
                .executes(context -> list(context.getSource(), 1)));
    }

    private static int requestAccessGrant(
            CommandSourceStack source,
            ServerPlayer target,
            String profileId,
            String leaseDurationText,
            String approvalDurationText,
            String reason
    ) {
        AccessLeaseRepository.Profile profile = KernelServices.accessLeases()
                .profile(profileId)
                .orElse(null);
        if (profile == null || !profile.active()) {
            return fail(source, "active access lease profile not found");
        }
        if (!profile.separationRequired()) {
            return fail(source, "this profile does not require two person approval");
        }
        if (profile.protectedProfile() && !has(source, "commands.accessgrant.protected")) {
            return fail(source, "protected access lease profile permission is required");
        }
        if (!mayTarget(source, target) || !mayDelegate(source, profile)) {
            return 0;
        }
        Duration leaseDuration = duration(leaseDurationText);
        Duration approvalDuration = duration(approvalDurationText);
        if (leaseDuration == null || approvalDuration == null) {
            return fail(source, "duration is invalid");
        }
        Map<String, String> payload = accessGrantPayload(target, profile, leaseDuration);
        String payloadHash = ApprovalRepository.payloadHash(payload);
        String preview = "grant profile " + profile.id() + " revision " + profile.revision()
                + " to " + target.getGameProfile().getName()
                + " for " + leaseDuration.getSeconds() + " seconds";
        return execute(source, "sef:approval.request", Map.of(
                "action", "sef:accessgrant.create",
                "payload_hash", payloadHash,
                "subject", target.getUUID().toString()), List.of(target.getUUID()), () -> {
            ActionResult<ApprovalRepository.ApprovalRequest> result =
                    KernelServices.approvals().commit(() -> KernelServices.approvals().create(
                            actorId(source),
                            "sef:accessgrant.create",
                            payloadHash,
                            preview,
                            approvalDuration,
                            true,
                            reason));
            if (!result.successful()) {
                return fail(source, result.detail());
            }
            success(source, "approval request created, " + result.value().id()
                    + ", revision " + result.value().revision());
            return 1;
        });
    }

    private static int requestGeneric(
            CommandSourceStack source,
            String actionId,
            String payloadHash,
            String durationText,
            String preview
    ) {
        Duration duration = duration(durationText);
        if (duration == null) {
            return fail(source, "duration is invalid");
        }
        return execute(source, "sef:approval.request", Map.of(
                "action", actionId,
                "payload_hash", payloadHash), List.of(), () -> {
            ActionResult<ApprovalRepository.ApprovalRequest> result =
                    KernelServices.approvals().commit(() -> KernelServices.approvals().create(
                            actorId(source),
                            actionId,
                            payloadHash,
                            preview,
                            duration,
                            true,
                            preview));
            if (!result.successful()) {
                return fail(source, result.detail());
            }
            success(source, "approval request created, " + result.value().id()
                    + ", revision " + result.value().revision());
            return 1;
        });
    }

    private static int approve(
            CommandSourceStack source,
            String requestInput,
            long expectedRevision,
            String note
    ) {
        UUID requestId = uuid(requestInput);
        if (requestId == null) {
            return fail(source, "approval request id is invalid");
        }
        return execute(source, "sef:approval.approve", Map.of(
                "request", requestId.toString(),
                "revision", Long.toString(expectedRevision)), List.of(), () -> {
            ActionResult<ApprovalRepository.ApprovalRequest> result =
                    KernelServices.approvals().commit(() -> KernelServices.approvals().approve(
                            requestId,
                            expectedRevision,
                            actorId(source),
                            note));
            if (!result.successful()) {
                return fail(source, result.detail());
            }
            success(source, "approval request approved, revision " + result.value().revision());
            return 1;
        });
    }

    private static int revoke(
            CommandSourceStack source,
            String requestInput,
            long expectedRevision,
            String reason
    ) {
        UUID requestId = uuid(requestInput);
        if (requestId == null) {
            return fail(source, "approval request id is invalid");
        }
        ApprovalRepository.ApprovalRequest request = KernelServices.approvals().find(requestId).orElse(null);
        if (request == null
                || !request.requesterId().equals(actorId(source))
                && !has(source, "commands.approval.revoke.others")) {
            return fail(source, "approval request not found");
        }
        return execute(source, "sef:approval.revoke", Map.of(
                "request", requestId.toString(),
                "revision", Long.toString(expectedRevision)), List.of(), () -> {
            ActionResult<ApprovalRepository.ApprovalRequest> result =
                    KernelServices.approvals().commit(() -> KernelServices.approvals().revoke(
                            requestId,
                            expectedRevision,
                            actorId(source),
                            reason));
            if (!result.successful()) {
                return fail(source, result.detail());
            }
            success(source, "approval request revoked");
            return 1;
        });
    }

    private static int inspect(CommandSourceStack source, String requestInput) {
        UUID requestId = uuid(requestInput);
        ApprovalRepository.ApprovalRequest request =
                requestId == null ? null : KernelServices.approvals().find(requestId).orElse(null);
        if (request == null) {
            return fail(source, "approval request not found");
        }
        info(source, "&e" + request.id() + " &8| &7revision &f" + request.revision());
        info(source, "&7action &f" + request.actionId()
                + " &8| &7state &f" + request.state().name().toLowerCase(Locale.ROOT));
        info(source, "&7requester &f" + request.requesterId()
                + " &8| &7expires &f" + request.expiresAt());
        info(source, "&7preview &f" + request.preview());
        info(source, "&7payload hash &f" + request.payloadHash());
        request.approvals().forEach(approval ->
                info(source, "&aapproved by &f" + approval.approverId() + " &7at " + approval.at()));
        return 1;
    }

    private static int list(CommandSourceStack source, int requestedPage) {
        return execute(source, "sef:approval.list", Map.of("page", Integer.toString(requestedPage)), List.of(), () -> {
            List<ApprovalRepository.ApprovalRequest> values =
                    KernelServices.approvals().requests(null, null);
            return page(source, "approval requests", values, requestedPage, request ->
                    "&e" + request.id()
                            + " &8| &f" + request.actionId()
                            + " &8| &7" + request.state().name().toLowerCase(Locale.ROOT)
                            + " &8| &7r" + request.revision());
        });
    }

    private static int history(CommandSourceStack source, String requestInput, int requestedPage) {
        UUID requestId = uuid(requestInput);
        if (requestId == null) {
            return fail(source, "approval request id is invalid");
        }
        return page(
                source,
                "approval history",
                KernelServices.approvals().history(requestId),
                requestedPage,
                entry -> "&e" + entry.at()
                        + " &8| &f" + entry.operation()
                        + " &8| &7" + entry.resultingState().name().toLowerCase(Locale.ROOT)
                        + " &8| &7" + entry.actorId());
    }

    public static Map<String, String> accessGrantPayload(
            ServerPlayer target,
            AccessLeaseRepository.Profile profile,
            Duration duration
    ) {
        return Map.of(
                "subject", target.getUUID().toString(),
                "profile", profile.id(),
                "profile_revision", Long.toString(profile.revision()),
                "duration_seconds", Long.toString(duration.getSeconds()));
    }

    private static boolean mayTarget(CommandSourceStack source, ServerPlayer target) {
        var decision = PlayerTargetPolicy.decide(
                source,
                target,
                PermissionsHandler.phasePermission("commands.accessgrant.hierarchy.override"),
                PermissionsHandler.phasePermission("commands.accessgrant.exempt"),
                PermissionsHandler.phasePermission("commands.accessgrant.exemption.override"),
                true,
                false);
        if (!decision.allowed()) {
            fail(source, "access grant target is unavailable");
        }
        return decision.allowed();
    }

    private static boolean mayDelegate(
            CommandSourceStack source,
            AccessLeaseRepository.Profile profile
    ) {
        ServerPlayer actor = source.getPlayer();
        if (actor == null && PermissionService.isConsole(source)) {
            return true;
        }
        if (actor == null) {
            fail(source, "only a player or dedicated server console can request delegated capabilities");
            return false;
        }
        for (String permission : profile.permissions()) {
            var node = PermissionsHandler.phasePermission(permission);
            if (node == null || !PermissionService.hasProviderOnly(actor, node)) {
                fail(source, "you cannot delegate permission " + permission);
                return false;
            }
        }
        return true;
    }

    private static Duration duration(String input) {
        DurationParser.Result parsed = DurationParser.parse(input, false);
        if (!parsed.valid()
                || parsed.seconds() < 1L
                || parsed.seconds() > ApprovalRepository.MAXIMUM_DURATION_SECONDS) {
            return null;
        }
        return Duration.ofSeconds(parsed.seconds());
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions>
    suggestPending(
            com.mojang.brigadier.context.CommandContext<CommandSourceStack> context,
            com.mojang.brigadier.suggestion.SuggestionsBuilder builder
    ) {
        return SharedSuggestionProvider.suggest(
                KernelServices.approvals().requests(null, null).stream()
                        .filter(request -> request.state().pending())
                        .map(request -> request.id().toString()),
                builder);
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions>
    suggestAll(
            com.mojang.brigadier.context.CommandContext<CommandSourceStack> context,
            com.mojang.brigadier.suggestion.SuggestionsBuilder builder
    ) {
        return SharedSuggestionProvider.suggest(
                KernelServices.approvals().requests(null, null).stream()
                        .map(request -> request.id().toString()),
                builder);
    }

    private static <T> int page(
            CommandSourceStack source,
            String title,
            List<T> values,
            int requestedPage,
            java.util.function.Function<T, String> formatter
    ) {
        int pages = Math.max(1, (values.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int page = Math.clamp(requestedPage, 1, pages);
        info(source, "&e" + title + " &7page &f" + page + "&7 of &f" + pages);
        values.stream()
                .skip((long) (page - 1) * PAGE_SIZE)
                .limit(PAGE_SIZE)
                .forEach(value -> info(source, formatter.apply(value)));
        return 1;
    }

    private static int execute(
            CommandSourceStack source,
            String actionId,
            Map<String, String> parameters,
            List<UUID> targets,
            java.util.function.IntSupplier operation
    ) {
        return KernelCommandExecutor.execute(source, actionId, parameters, targets, false, operation);
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

    private static boolean has(CommandSourceStack source, String permission) {
        var node = PermissionsHandler.phasePermission(permission);
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
