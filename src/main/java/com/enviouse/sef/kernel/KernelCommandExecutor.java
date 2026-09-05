package com.enviouse.sef.kernel;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.kernel.observation.ObservationContracts;
import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.audit.SecurityAuditService;
import com.enviouse.sef.control.MinecraftServerControlRuntime;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.kernel.policy.CommandExecutionService;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.permissions.DelegatedPermissionScope;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.rcon.RconConsoleSource;
import net.minecraft.world.level.BaseCommandBlock;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.IntSupplier;

public final class KernelCommandExecutor {
    private KernelCommandExecutor() {
    }

    @SafeVarargs
    public static boolean canUse(
            CommandSourceStack source,
            String actionId,
            PermissionNode<Boolean>... additionalPermissions
    ) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(actionId, "actionId");
        if (!DelegatedPermissionScope.actionAllowed(actionId)) {
            return false;
        }
        CommandDefinition definition = knownDefinition(actionId);
        return definition != null && permissions(source, definition, additionalPermissions).granted();
    }

    /**
     * Applies the server-control policy gate for command adapters that must
     * keep a custom lease around their domain operation.
     */
    public static boolean authorizeControl(CommandSourceStack source, String actionId) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(actionId, "actionId");
        if (!DelegatedPermissionScope.actionAllowed(actionId)) {
            return false;
        }
        CommandDefinition definition = knownDefinition(actionId);
        if (definition == null) {
            rejectUnknownAction(source);
            return false;
        }
        if (!AuditService.accepting(definition.auditClass())) {
            source.sendFailure(TextFormatter.stringToFormattedText(
                    "&cMandatory command audit is unavailable. This action is blocked."));
            return false;
        }
        ActionResult<Void> authorization = MinecraftServerControlRuntime.authorizeAction(source, definition);
        if (authorization.successful()) {
            return true;
        }
        CommandDefinition.SourceType sourceType = sourceType(source);
        AuditService.record(AuditService.Event.metadata(
                SecurityAuditService.currentSessionId(),
                actorId(source, sourceType),
                Objects.requireNonNullElse(source.getTextName(), ""),
                sourceType.name(),
                definition.id(),
                List.of(),
                AuditService.Result.REJECTED,
                authorization.reason(),
                "server_control",
                definition.auditClass()));
        source.sendFailure(TextFormatter.stringToFormattedText(
                "&c" + authorization.detail()));
        return false;
    }

    @SafeVarargs
    public static int execute(
            CommandSourceStack source,
            String actionId,
            Map<String, String> normalizedParameters,
            IntSupplier action,
            PermissionNode<Boolean>... additionalPermissions
    ) {
        return execute(
                source,
                actionId,
                normalizedParameters,
                List.of(),
                false,
                action,
                additionalPermissions);
    }

    public static int reject(
            CommandSourceStack source,
            String actionId,
            ActionResult.ReasonCode reason,
            String detail
    ) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(actionId, "actionId");
        Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(detail, "detail");
        if (!DelegatedPermissionScope.actionAllowed(actionId)) {
            source.sendFailure(TextFormatter.stringToFormattedText(
                    "&cThe delegated execution grant does not cover this action."));
            return 0;
        }
        CommandDefinition definition = knownDefinition(actionId);
        if (definition == null) {
            rejectUnknownAction(source);
            return 0;
        }
        if (!AuditService.accepting(definition.auditClass())) {
            source.sendFailure(TextFormatter.stringToFormattedText(
                    "&cMandatory command audit is unavailable. This action is blocked."));
            return 0;
        }
        CommandDefinition.SourceType sourceType = sourceType(source);
        KernelServices.commandJournal().attachOrBegin(source, actionId);
        boolean recorded = AuditService.record(AuditService.Event.metadata(
                SecurityAuditService.currentSessionId(),
                actorId(source, sourceType),
                Objects.requireNonNullElse(source.getTextName(), ""),
                sourceType.name(),
                definition.id(),
                List.of(),
                AuditService.Result.REJECTED,
                reason,
                "command",
                definition.auditClass()));
        KernelServices.commandJournal().finishCurrent(
                ObservationContracts.LifecycleStage.REJECTED,
                0,
                reason.name().toLowerCase(Locale.ROOT));
        if (!recorded) {
            source.sendFailure(TextFormatter.stringToFormattedText(
                    "&cMandatory command audit is unavailable. This action is blocked."));
            return 0;
        }
        source.sendFailure(TextFormatter.stringToFormattedText("&c" + detail));
        return 0;
    }

    @SafeVarargs
    public static int execute(
            CommandSourceStack source,
            String actionId,
            Map<String, String> normalizedParameters,
            List<UUID> targetIds,
            boolean cooldownBypass,
            IntSupplier action,
            PermissionNode<Boolean>... additionalPermissions
    ) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(actionId, "actionId");
        Objects.requireNonNull(normalizedParameters, "normalizedParameters");
        Objects.requireNonNull(targetIds, "targetIds");
        Objects.requireNonNull(action, "action");
        if (!DelegatedPermissionScope.actionAllowed(actionId)) {
            source.sendFailure(TextFormatter.stringToFormattedText(
                    "&cThe delegated execution grant does not cover this action."));
            return 0;
        }

        CommandDefinition definition = knownDefinition(actionId);
        if (definition == null) {
            rejectUnknownAction(source);
            return 0;
        }
        if (!AuditService.accepting(definition.auditClass())) {
            KernelServices.commandJournal().finishCurrent(
                    ObservationContracts.LifecycleStage.REJECTED,
                    null,
                    ActionResult.ReasonCode.STORAGE_ERROR.name().toLowerCase(Locale.ROOT));
            source.sendFailure(TextFormatter.stringToFormattedText(
                    "&cMandatory command audit is unavailable. This action is blocked."));
            return 0;
        }
        PermissionSummary permission = permissions(source, definition, additionalPermissions);
        boolean effectiveCooldownBypass = cooldownBypass || KernelServices.cooldownBypass(source, actionId);
        boolean effectiveCostBypass = KernelServices.costBypass(source);
        CommandDefinition.SourceType sourceType = sourceType(source);
        ExecutionOperationScope.Context operation =
                ExecutionOperationScope.current().orElse(null);
        UUID actorId = operation == null
                ? actorId(source, sourceType)
                : operation.actorId();
        Map<String, String> effectiveParameters = new LinkedHashMap<>(normalizedParameters);
        if (operation != null) {
            effectiveParameters.put("operation_id", operation.operationId().toString());
        }
        String dimensionId = dimensionId(source);
        KernelServices.commandJournal().attachOrBegin(source, actionId);
        ActionResult<Void> controlAuthorization =
                MinecraftServerControlRuntime.authorizeAction(source, definition);
        if (!controlAuthorization.successful()) {
            KernelServices.commandJournal().finishCurrent(
                    ObservationContracts.LifecycleStage.REJECTED,
                    null,
                    controlAuthorization.reason().name().toLowerCase(Locale.ROOT));
            AuditService.record(AuditService.Event.metadata(
                    SecurityAuditService.currentSessionId(),
                    actorId,
                    Objects.requireNonNullElse(source.getTextName(), ""),
                    sourceType.name(),
                    definition.id(),
                    targetIds,
                    AuditService.Result.REJECTED,
                    controlAuthorization.reason(),
                    "server_control",
                    definition.auditClass()));
            source.sendFailure(TextFormatter.stringToFormattedText(
                    "&c" + controlAuthorization.detail()));
            return 0;
        }

        Map<String, String> providerContext = new LinkedHashMap<>(permission.providerContext());
        providerContext.put("source_class", sourceType.name().toLowerCase(Locale.ROOT));
        providerContext.put("cost_bypass", Boolean.toString(effectiveCostBypass));
        if (operation != null) {
            providerContext.put("operation_id", operation.operationId().toString());
            providerContext.put("idempotency_key", operation.idempotencyKey());
            providerContext.put("authorization", "queue_time");
        }
        try {
            providerContext.put(
                    "quoted_cost",
                    KernelServices.quoteCommandCost(actionId, normalizedParameters, targetIds).toPlainString());
        } catch (IllegalArgumentException exception) {
            KernelServices.commandJournal().finishCurrent(
                    ObservationContracts.LifecycleStage.REJECTED,
                    null,
                    ActionResult.ReasonCode.INVALID_INPUT.name().toLowerCase(Locale.ROOT));
            boolean recorded = AuditService.record(AuditService.Event.metadata(
                    SecurityAuditService.currentSessionId(),
                    actorId,
                    Objects.requireNonNullElse(source.getTextName(), ""),
                    sourceType.name(),
                    definition.id(),
                    targetIds,
                    AuditService.Result.REJECTED,
                    ActionResult.ReasonCode.INVALID_INPUT,
                    "command",
                    definition.auditClass()));
            source.sendFailure(TextFormatter.stringToFormattedText(
                    recorded
                            ? "&cThe configured command cost could not be calculated."
                            : "&cMandatory command audit is unavailable. This action is blocked."));
            return 0;
        }
        ActionResult<CommandExecutionService.Lease> started = KernelServices.commandExecutions().begin(
                new CommandExecutionService.Request(
                        SecurityAuditService.currentSessionId(),
                        actorId,
                        Objects.requireNonNullElse(source.getTextName(), ""),
                        definition.id(),
                        sourceType,
                        dimensionId,
                        dimensionId,
                        permission.granted(),
                        effectiveCooldownBypass,
                        effectiveCostBypass,
                        false,
                        "",
                        null,
                        null,
                        Set.of(),
                        effectiveParameters,
                        targetIds,
                        1L,
                        providerContext,
                        "command"));
        if (!started.successful()) {
            KernelServices.commandJournal().finishCurrent(
                    ObservationContracts.LifecycleStage.REJECTED,
                    null,
                    started.reason().name().toLowerCase(Locale.ROOT));
            sendFailure(source, started);
            return 0;
        }

        try (CommandExecutionService.Lease lease = started.value()) {
            int result;
            try {
                result = action.getAsInt();
            } catch (RuntimeException exception) {
                lease.complete(false, ActionResult.ReasonCode.PROVIDER_ERROR);
                KernelServices.commandJournal().finishCurrent(
                        ObservationContracts.LifecycleStage.FAILED,
                        null,
                        exception.getClass().getSimpleName());
                com.enviouse.sef.ServerEssentialsForge.LOGGER.error(
                        "[SEF] Kernel action {} failed",
                        definition.id(),
                        exception);
                source.sendFailure(TextFormatter.stringToFormattedText(
                        "&cThat action could not be completed safely."));
                return 0;
            }
            ActionResult<Void> completed = lease.complete(
                    result > 0,
                    result > 0 ? null : ActionResult.ReasonCode.PROVIDER_ERROR);
            if (!completed.successful()) {
                KernelServices.commandJournal().finishCurrent(
                        ObservationContracts.LifecycleStage.FAILED,
                        result,
                        completed.reason().name().toLowerCase(Locale.ROOT));
                if (result > 0) {
                    sendFailure(source, completed);
                }
                return 0;
            }
            KernelServices.commandJournal().finishCurrent(
                    result > 0
                            ? ObservationContracts.LifecycleStage.COMPLETED
                            : ObservationContracts.LifecycleStage.FAILED,
                    result,
                    result > 0 ? "" : ActionResult.ReasonCode.PROVIDER_ERROR.name().toLowerCase(Locale.ROOT));
            return result;
        }
    }

    public static CommandDefinition.SourceType sourceType(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer) {
            return CommandDefinition.SourceType.PLAYER;
        }
        if (source.source instanceof RconConsoleSource) {
            return CommandDefinition.SourceType.RCON;
        }
        if (source.source instanceof BaseCommandBlock) {
            return CommandDefinition.SourceType.COMMAND_BLOCK;
        }
        if (source.source instanceof MinecraftServer) {
            return CommandDefinition.SourceType.CONSOLE;
        }
        return CommandDefinition.SourceType.EXTERNAL_ADAPTER;
    }

    private static CommandDefinition knownDefinition(String actionId) {
        return KernelServices.catalog().find(actionId).orElse(null);
    }

    private static void rejectUnknownAction(CommandSourceStack source) {
        source.sendFailure(TextFormatter.stringToFormattedText(
                "&cThat command action is unavailable."));
    }

    private static PermissionSummary permissions(
            CommandSourceStack source,
            CommandDefinition definition,
            PermissionNode<Boolean>[] additionalPermissions
    ) {
        LinkedHashMap<String, PermissionNode<Boolean>> nodes = new LinkedHashMap<>();
        for (String permissionId : definition.permissionIds()) {
            PermissionNode<Boolean> node = KernelServices.permissionNode(permissionId);
            if (node == null) {
                return new PermissionSummary(false, Map.of(
                        "permission_ids", String.join(",", definition.permissionIds()),
                        "permission_providers", "missing_manifest",
                        "permission_default_use", PermissionService.DefaultUse.UNKNOWN.name(),
                        "permission_denials", PermissionService.DenialReason.PROVIDER_UNAVAILABLE.name()));
            }
            nodes.put(node.getNodeName(), node);
        }
        if (additionalPermissions != null) {
            for (PermissionNode<Boolean> node : additionalPermissions) {
                if (node != null) {
                    nodes.putIfAbsent(node.getNodeName(), node);
                }
            }
        }

        boolean granted = true;
        Set<String> providers = new LinkedHashSet<>();
        Set<String> defaultUses = new LinkedHashSet<>();
        Set<String> denialReasons = new LinkedHashSet<>();
        List<String> permissionIds = new ArrayList<>(nodes.size());
        for (PermissionNode<Boolean> node : nodes.values()) {
            PermissionService.Decision decision = PermissionService.decide(source, node);
            permissionIds.add(decision.permissionId());
            providers.add(decision.provider());
            defaultUses.add(decision.defaultUse().name());
            if (!decision.granted()) {
                granted = false;
                denialReasons.add(decision.denialReason().name());
            }
        }

        return new PermissionSummary(granted, Map.of(
                "permission_ids", String.join(",", permissionIds),
                "permission_providers", String.join(",", providers),
                "permission_default_use", String.join(",", defaultUses),
                "permission_denials", denialReasons.isEmpty() ? "none" : String.join(",", denialReasons)));
    }

    private static UUID actorId(CommandSourceStack source, CommandDefinition.SourceType sourceType) {
        if (source.getEntity() instanceof ServerPlayer player && player.getUUID() != null) {
            return player.getUUID();
        }
        String identity = "sef:" + sourceType.name() + ":" + Objects.requireNonNullElse(source.getTextName(), "");
        return UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8));
    }

    private static String dimensionId(CommandSourceStack source) {
        if (source.getLevel() == null || source.getLevel().dimension() == null) {
            return "unknown";
        }
        return source.getLevel().dimension().location().toString();
    }

    private static void sendFailure(CommandSourceStack source, ActionResult<?> result) {
        String message = switch (result.reason()) {
            case FEATURE_DISABLED -> "&cThat feature is currently disabled.";
            case SOURCE_NOT_ALLOWED -> "&cThat command cannot be used from this source.";
            case PERMISSION_DENIED -> "&cYou do not have permission to use that command.";
            case COOLDOWN_ACTIVE -> "&cThat command is on cooldown for &e" + result.detail() + " &cseconds.";
            case WARMUP_ACTIVE -> "&eThat command is warming up.";
            case CONFIRMATION_REQUIRED -> "&eThat command requires confirmation.";
            default -> "&cThat action is unavailable. &7"
                    + result.reason().name().toLowerCase(Locale.ROOT);
        };
        source.sendFailure(TextFormatter.stringToFormattedText(message));
    }

    private record PermissionSummary(boolean granted, Map<String, String> providerContext) {
    }
}
