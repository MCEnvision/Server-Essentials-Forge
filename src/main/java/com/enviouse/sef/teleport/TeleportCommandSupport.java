package com.enviouse.sef.teleport;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.audit.SecurityAuditService;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.kernel.policy.CommandExecutionService;
import com.enviouse.sef.kernel.policy.FeatureGateService;
import com.enviouse.sef.kernel.policy.QuotaService;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.kernel.policy.TargetHierarchyService;
import com.enviouse.sef.kernel.policy.WarmupService;
import com.enviouse.sef.permissions.PermissionService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.IntConsumer;

final class TeleportCommandSupport {
    static final int ASYNC_PENDING = -1;

    private TeleportCommandSupport() {
    }

    static boolean has(CommandSourceStack source, PermissionNode<Boolean> permission) {
        return PermissionService.has(source, permission);
    }

    static boolean has(
            CommandSourceStack source,
            PermissionNode<Boolean> permission,
            String actionId
    ) {
        return PermissionService.has(source, permission) && actionEnabled(source, actionId);
    }

    static boolean has(ServerPlayer player, PermissionNode<Boolean> permission) {
        return PermissionService.has(player, permission);
    }

    static ServerPlayer player(CommandSourceStack source) {
        try {
            return source.getPlayerOrException();
        } catch (Exception exception) {
            fail(source, "This command can only be used by a player.");
            return null;
        }
    }

    static boolean actionEnabled(CommandSourceStack source, String actionId) {
        return KernelServices.catalog().find(actionId)
                .map(definition -> {
                    String dimension = source.getLevel() == null
                            ? "server"
                            : source.getLevel().dimension().location().toString();
                    return KernelServices.featureGates().decide(
                            definition.featureId(),
                            new FeatureGateService.Context(dimension, dimension, actionId)).enabled();
                })
                .orElse(false);
    }

    static int teleport(
            CommandSourceStack source,
            ServerPlayer actor,
            ServerPlayer target,
            SavedLocation destination,
            String actionId,
            String reason,
            PermissionNode<Boolean> actionPermission,
            SafeTeleportService.Policy policy,
            SafeTeleportService.DestinationGuard destinationGuard
    ) {
        return teleport(
                source,
                actor,
                target,
                destination,
                actionId,
                reason,
                actionPermission,
                policy,
                destinationGuard,
                ignored -> {
                },
                false);
    }

    static int teleport(
            CommandSourceStack source,
            ServerPlayer actor,
            ServerPlayer target,
            SavedLocation destination,
            String actionId,
            String reason,
            PermissionNode<Boolean> actionPermission,
            SafeTeleportService.Policy policy,
            SafeTeleportService.DestinationGuard destinationGuard,
            boolean surfaceOnly
    ) {
        return teleport(
                source,
                actor,
                target,
                destination,
                actionId,
                reason,
                actionPermission,
                policy,
                destinationGuard,
                ignored -> {
                },
                surfaceOnly);
    }

    static int teleport(
            CommandSourceStack source,
            ServerPlayer actor,
            ServerPlayer target,
            SavedLocation destination,
            String actionId,
            String reason,
            PermissionNode<Boolean> actionPermission,
            SafeTeleportService.Policy policy,
            SafeTeleportService.DestinationGuard destinationGuard,
            IntConsumer asynchronousCompletion
    ) {
        return teleport(
                source,
                actor,
                target,
                destination,
                actionId,
                reason,
                actionPermission,
                policy,
                destinationGuard,
                asynchronousCompletion,
                false);
    }

    static int teleport(
            CommandSourceStack source,
            ServerPlayer actor,
            ServerPlayer target,
            SavedLocation destination,
            String actionId,
            String reason,
            PermissionNode<Boolean> actionPermission,
            SafeTeleportService.Policy policy,
            SafeTeleportService.DestinationGuard destinationGuard,
            IntConsumer asynchronousCompletion,
            boolean surfaceOnly
    ) {
        if (!KernelCommandExecutor.authorizeControl(source, actionId)) {
            return 0;
        }
        PermissionService.Decision permission = PermissionService.decide(actor, actionPermission);
        String dimension = actor.serverLevel().dimension().location().toString();
        Set<WarmupService.CancelReason> cancellation = new LinkedHashSet<>();
        TeleportSettings settings = KernelServices.teleportSettings();
        if (settings.cancelOnMovement()) {
            cancellation.add(WarmupService.CancelReason.MOVEMENT);
        }
        if (settings.cancelOnDamage()) {
            cancellation.add(WarmupService.CancelReason.DAMAGE);
        }
        List<UUID> targetIds = actor == target ? List.of() : List.of(target.getUUID());
        long distance = destination.dimensionId().equals(dimension)
                ? (long) Math.ceil(Math.sqrt(
                        square(destination.x() - actor.getX())
                                + square(destination.y() - actor.getY())
                                + square(destination.z() - actor.getZ())))
                : 0L;
        Map<String, String> parameters = Map.of(
                "reason", reason,
                "distance", Long.toString(distance));
        final String quotedCost;
        try {
            quotedCost = KernelServices.quoteCommandCost(actionId, parameters, targetIds).toPlainString();
        } catch (IllegalArgumentException exception) {
            fail(source, "The configured teleport cost could not be calculated.");
            return 0;
        }
        ActionResult<CommandExecutionService.Lease> started = KernelServices.commandExecutions().begin(
                new CommandExecutionService.Request(
                        SecurityAuditService.currentSessionId(),
                        actor.getUUID(),
                        actor.getGameProfile().getName(),
                        actionId,
                        CommandDefinition.SourceType.PLAYER,
                        dimension,
                        dimension,
                        permission.granted(),
                        has(actor, PermissionsHandler.teleportCooldownBypass),
                        KernelServices.costBypass(source),
                        has(actor, PermissionsHandler.teleportWarmupBypass),
                        "",
                        null,
                        new WarmupService.Position(
                                dimension,
                                actor.getX(),
                                actor.getY(),
                                actor.getZ(),
                                actor.getYRot(),
                                actor.getXRot()),
                        cancellation,
                        parameters,
                        targetIds,
                        1,
                        Map.of(
                                "permission_provider", permission.provider(),
                                "permission_default_use", permission.defaultUse().name(),
                                "quoted_cost", quotedCost),
                        "command"));
        if (!started.successful()) {
            if (started.reason() == ActionResult.ReasonCode.WARMUP_ACTIVE
                    && "warmup started".equals(started.detail())) {
                TeleportWarmupManager.schedule(actor, () -> {
                    int result = teleport(
                            source,
                            actor,
                            target,
                            destination,
                            actionId,
                            reason,
                            actionPermission,
                            policy,
                            destinationGuard,
                            asynchronousCompletion,
                            surfaceOnly);
                    if (result != ASYNC_PENDING) {
                        asynchronousCompletion.accept(result);
                    }
                    return result;
                });
                executionFailure(source, started);
                return ASYNC_PENDING;
            }
            executionFailure(source, started);
            return 0;
        }

        try (CommandExecutionService.Lease lease = started.value()) {
            SafeTeleportService.DestinationGuard guard = () ->
                    PermissionService.has(actor, actionPermission) && destinationGuard.stillValid();
            SafeTeleportService.TeleportResult result = KernelServices.safeTeleports().teleport(
                    source.getServer(),
                    actor,
                    target,
                    destination,
                    reason,
                    policy,
                    guard,
                    surfaceOnly);
            if (!result.successful()) {
                lease.complete(false, reason(result.code()));
                fail(source, describe(result));
                return 0;
            }
            ActionResult<Void> completed = lease.complete(true, null);
            if (!completed.successful()) {
                fail(source, "The teleport could not commit its cooldown or cost policy.");
                return 0;
            }
            success(source, "Teleported safely.");
            return 1;
        }
    }

    private static double square(double value) {
        return value * value;
    }

    static long quota(ServerPlayer player, String quotaId, String actionId, long currentUsage) {
        return KernelServices.quotas().resolve(quotaContext(player, quotaId, actionId, currentUsage)).effectiveValue();
    }

    static QuotaService.Context quotaContext(
            ServerPlayer player,
            String quotaId,
            String actionId,
            long currentUsage
    ) {
        Set<String> tiers = new LinkedHashSet<>();
        PermissionsHandler.quotaTierNodes.forEach((id, node) -> {
            if (has(player, node)) {
                tiers.add(id);
            }
        });
        String dimension = player.serverLevel().dimension().location().toString();
        return new QuotaService.Context(
                quotaId,
                player.getUUID(),
                "server",
                dimension,
                dimension,
                actionId,
                tiers,
                Map.of(),
                Map.of(),
                currentUsage);
    }

    static long offlineQuota(
            UUID subjectId,
            String quotaId,
            String actionId,
            long currentUsage,
            String dimensionId
    ) {
        Set<String> tiers = new LinkedHashSet<>();
        PermissionsHandler.quotaTierNodes.forEach((id, node) -> {
            if (PermissionService.has(subjectId, node)) {
                tiers.add(id);
            }
        });
        return KernelServices.quotas().resolve(new QuotaService.Context(
                quotaId,
                subjectId,
                "server",
                dimensionId,
                dimensionId,
                actionId,
                tiers,
                Map.of(),
                Map.of(),
                currentUsage)).effectiveValue();
    }

    static boolean mayTarget(
            CommandSourceStack source,
            ServerPlayer actor,
            ServerPlayer target,
            boolean allowEqual
    ) {
        TargetHierarchyService.Decision decision = PlayerTargetPolicy.decide(
                source,
                target,
                PermissionsHandler.teleportHierarchyBypass,
                PermissionsHandler.teleportExempt,
                PermissionsHandler.teleportBypassExempt,
                true,
                allowEqual);
        if (!decision.allowed()) {
            fail(source, decision.exempt()
                    ? "That player is exempt from teleport targeting."
                    : "You cannot target that player because of hierarchy.");
        }
        return decision.allowed();
    }

    static void success(CommandSourceStack source, String message) {
        source.sendSuccess(() -> TextFormatter.stringToFormattedText("&a" + message), false);
    }

    static void info(CommandSourceStack source, String message) {
        source.sendSuccess(() -> TextFormatter.stringToFormattedText("&7" + message), false);
    }

    static void fail(CommandSourceStack source, String message) {
        source.sendFailure(TextFormatter.stringToFormattedText("&c" + message));
    }

    static void executionFailure(CommandSourceStack source, ActionResult<?> result) {
        switch (result.reason()) {
            case COOLDOWN_ACTIVE -> fail(source, "Wait " + result.detail() + " seconds before using that teleport again.");
            case WARMUP_ACTIVE -> info(source, "Teleport warmup started. Stay still until it completes.");
            case WARMUP_CANCELLED -> fail(source, "Teleport warmup cancelled because " + result.detail() + ".");
            case FEATURE_DISABLED -> fail(source, "That teleport feature is disabled.");
            case PERMISSION_DENIED -> fail(source, "You no longer have permission for that teleport.");
            case COST_UNAVAILABLE -> fail(source, "The configured teleport cost provider is unavailable.");
            default -> fail(source, "Teleport policy rejected the action. " + result.reason().name().toLowerCase(java.util.Locale.ROOT));
        }
    }

    private static String describe(SafeTeleportService.TeleportResult result) {
        return switch (result.code()) {
            case DIMENSION_MISSING -> "The saved dimension is not available.";
            case OUTSIDE_BORDER -> "The destination is outside the world border.";
            case CHUNK_BUDGET_EXCEEDED, CHUNK_TIMEOUT -> "The destination exceeded the loaded chunk safety budget.";
            case NO_SAFE_SPACE -> "No safe loaded position was found near the destination.";
            case HAZARD -> "The destination is hazardous.";
            case CLAIM_DENIED -> "A claim provider denied the destination.";
            case COMBAT_DENIED -> "Teleporting while in combat is disabled.";
            case MOVEMENT_CANCELLED -> "The teleport was cancelled by movement.";
            case PERMISSION_LOST -> "Permission was lost before the teleport completed.";
            case TARGET_OFFLINE -> "The teleport target went offline.";
            case STATE_CHANGED -> "The saved destination changed before the teleport completed.";
            case PROVIDER_ERROR -> "Minecraft rejected the teleport.";
            case SUCCESS -> "Teleported safely.";
        };
    }

    private static ActionResult.ReasonCode reason(SafeTeleportService.ResultCode code) {
        return switch (code) {
            case DIMENSION_MISSING, OUTSIDE_BORDER, CHUNK_BUDGET_EXCEEDED, CHUNK_TIMEOUT,
                    NO_SAFE_SPACE, HAZARD, CLAIM_DENIED, COMBAT_DENIED ->
                    ActionResult.ReasonCode.POLICY_DENIED;
            case MOVEMENT_CANCELLED -> ActionResult.ReasonCode.WARMUP_CANCELLED;
            case PERMISSION_LOST -> ActionResult.ReasonCode.PERMISSION_DENIED;
            case TARGET_OFFLINE -> ActionResult.ReasonCode.NOT_FOUND;
            case STATE_CHANGED -> ActionResult.ReasonCode.CONFLICT;
            case PROVIDER_ERROR, SUCCESS -> ActionResult.ReasonCode.PROVIDER_ERROR;
        };
    }
}
