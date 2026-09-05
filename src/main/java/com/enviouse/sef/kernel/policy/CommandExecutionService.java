package com.enviouse.sef.kernel.policy;

import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.audit.SecurityAuditService;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.command.CommandDefinition;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class CommandExecutionService {
    private final CommandPolicyService policies;
    private final CooldownService cooldowns;
    private final CostService costs;
    private final WarmupService warmups;
    private final ConfirmationService confirmations;
    private final CooldownDurationResolver cooldownDurations;

    public CommandExecutionService(
            CommandPolicyService policies,
            CooldownService cooldowns,
            CostService costs,
            WarmupService warmups,
            ConfirmationService confirmations
    ) {
        this(
                policies,
                cooldowns,
                costs,
                warmups,
                confirmations,
                (playerId, actionId, internalDefault) -> new CooldownDurationResolver.Resolution(
                        actionId,
                        actionId,
                        internalDefault,
                        "policy_default",
                        "",
                        true,
                        1L));
    }

    public CommandExecutionService(
            CommandPolicyService policies,
            CooldownService cooldowns,
            CostService costs,
            WarmupService warmups,
            ConfirmationService confirmations,
            CooldownDurationResolver cooldownDurations
    ) {
        this.policies = Objects.requireNonNull(policies, "policies");
        this.cooldowns = Objects.requireNonNull(cooldowns, "cooldowns");
        this.costs = Objects.requireNonNull(costs, "costs");
        this.warmups = Objects.requireNonNull(warmups, "warmups");
        this.confirmations = Objects.requireNonNull(confirmations, "confirmations");
        this.cooldownDurations = Objects.requireNonNull(cooldownDurations, "cooldownDurations");
    }

    public ActionResult<Lease> begin(Request request) {
        Objects.requireNonNull(request, "request");
        long startedNanos = System.nanoTime();
        CommandPolicyService.Decision policy = policies.decide(new CommandPolicyService.Context(
                request.actionId(),
                request.sourceType(),
                request.worldId(),
                request.dimensionId()));
        boolean mandatoryAuditExpected = AuditService.accepting(policy.auditClass());
        if (!policy.allowed()) {
            if (mandatoryAuditExpected && !audit(request, AuditService.Result.REJECTED, policy.reason(), policy.auditClass(),
                    elapsedMillis(startedNanos))) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.STORAGE_ERROR,
                        "command audit could not be persisted safely");
            }
            return ActionResult.failure(policy.reason(), policy.explanation());
        }
        if (!request.permissionGranted()) {
            if (mandatoryAuditExpected && !audit(request, AuditService.Result.REJECTED, ActionResult.ReasonCode.PERMISSION_DENIED,
                    policy.auditClass(), elapsedMillis(startedNanos))) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.STORAGE_ERROR,
                        "command audit could not be persisted safely");
            }
            return ActionResult.failure(ActionResult.ReasonCode.PERMISSION_DENIED, "permission denied");
        }
        final java.math.BigDecimal effectiveCost;
        try {
            effectiveCost = request.costBypass()
                    ? java.math.BigDecimal.ZERO
                    : quotedCost(request, policy.cost());
        } catch (IllegalArgumentException exception) {
            if (mandatoryAuditExpected && !audit(request, AuditService.Result.REJECTED, ActionResult.ReasonCode.INVALID_INPUT,
                    policy.auditClass(), elapsedMillis(startedNanos))) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.STORAGE_ERROR,
                        "command audit could not be persisted safely");
            }
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, exception.getMessage());
        }

        CooldownDurationResolver.Resolution resolvedCooldown =
                cooldownDurations.resolve(request.actorId(), request.actionId(), policy.cooldown());
        Duration cooldownDuration = resolvedCooldown.duration();
        Map<String, String> cooldownContext =
                cooldownAuditContext(resolvedCooldown, request.cooldownBypass());
        if (!request.cooldownBypass() && !cooldownDuration.isZero()) {
            CooldownService.Decision current = cooldowns.inspect(request.actorId(), request.actionId());
            if (!current.allowed()) {
                if (mandatoryAuditExpected && !audit(request, AuditService.Result.REJECTED, current.reason(), policy.auditClass(),
                        elapsedMillis(startedNanos), cooldownContext)) {
                    return ActionResult.failure(
                            ActionResult.ReasonCode.STORAGE_ERROR,
                            "command audit could not be persisted safely");
                }
                return ActionResult.failure(
                        current.reason(),
                        Long.toString(current.remainingSeconds()));
            }
        }
        if (request.warmupBypass()) {
            warmups.clear(request.actorId());
        } else if (!policy.warmup().isZero()) {
            if (request.warmupPosition() == null) {
                audit(request, AuditService.Result.REJECTED, ActionResult.ReasonCode.INVALID_INPUT,
                        policy.auditClass(), elapsedMillis(startedNanos), cooldownContext);
                return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "warmup position is required");
            }
            UUID targetId = request.targetIds().isEmpty() ? null : request.targetIds().getFirst();
            warmups.inspect(request.actorId()).ifPresent(existing -> {
                if (!existing.actionId().equals(request.actionId())
                        || !Objects.equals(existing.targetId(), targetId)) {
                    warmups.clear(request.actorId());
                }
            });
            ActionResult<WarmupService.Warmup> warmup = warmups.check(
                    request.actorId(),
                    request.warmupPosition());
            if (warmup.reason() == ActionResult.ReasonCode.NOT_FOUND) {
                warmups.start(
                        request.actorId(),
                        request.actionId(),
                        request.warmupPosition(),
                        targetId,
                        policy.warmup(),
                        request.warmupCancellationPolicy());
                warmup = ActionResult.failure(ActionResult.ReasonCode.WARMUP_ACTIVE, "warmup started");
            }
            if (!warmup.successful()) {
                if (mandatoryAuditExpected && !audit(request, AuditService.Result.REJECTED, warmup.reason(), policy.auditClass(),
                        elapsedMillis(startedNanos), cooldownContext)) {
                    return ActionResult.failure(
                            ActionResult.ReasonCode.STORAGE_ERROR,
                            "command audit could not be persisted safely");
                }
                return ActionResult.failure(warmup.reason(), warmup.detail());
            }
        }

        CooldownService.Decision cooldown = cooldowns.tryAcquire(
                request.actorId(),
                request.actionId(),
                cooldownDuration,
                request.cooldownBypass());
        if (!cooldown.allowed()) {
            if (mandatoryAuditExpected && !audit(request, AuditService.Result.REJECTED, cooldown.reason(), policy.auditClass(),
                    elapsedMillis(startedNanos), cooldownContext)) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.STORAGE_ERROR,
                        "command audit could not be persisted safely");
            }
            return ActionResult.failure(
                    cooldown.reason(),
                    Long.toString(cooldown.remainingSeconds()));
        }
        boolean cooldownAcquired = !cooldown.bypassed() && !cooldownDuration.isZero();

        ActionResult<CostService.Reservation> cost = costs.reserve(
                request.actorId(),
                request.actionId(),
                effectiveCost);
        if (!cost.successful()) {
            clearCooldown(request, cooldownAcquired);
            if (mandatoryAuditExpected && !audit(request, AuditService.Result.REJECTED, cost.reason(), policy.auditClass(),
                    elapsedMillis(startedNanos), cooldownContext)) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.STORAGE_ERROR,
                        "command audit could not be persisted safely");
            }
            return ActionResult.failure(cost.reason(), cost.detail());
        }

        if (policy.confirmationRequired()) {
            if (request.confirmationToken().isBlank() || request.confirmationBinding() == null) {
                cost.value().refund();
                clearCooldown(request, cooldownAcquired);
                if (mandatoryAuditExpected && !audit(request, AuditService.Result.REJECTED, ActionResult.ReasonCode.CONFIRMATION_REQUIRED,
                        policy.auditClass(), elapsedMillis(startedNanos),
                        merge(cooldownContext, cost.value().auditContext()))) {
                    return ActionResult.failure(
                            ActionResult.ReasonCode.STORAGE_ERROR,
                            "command audit could not be persisted safely");
                }
                return ActionResult.failure(ActionResult.ReasonCode.CONFIRMATION_REQUIRED, "confirmation required");
            }
            ConfirmationService.Request binding = request.confirmationBinding();
            if (!binding.actorId().equals(request.actorId())
                    || !binding.actionId().equals(request.actionId())
                    || !binding.normalizedParameters().equals(request.normalizedParameters())
                    || !binding.targetIds().equals(request.targetIds())
                    || binding.policyRevision() != policy.revision()) {
                cost.value().refund();
                clearCooldown(request, cooldownAcquired);
                if (mandatoryAuditExpected && !audit(request, AuditService.Result.REJECTED, ActionResult.ReasonCode.CONFIRMATION_INVALID,
                        policy.auditClass(), elapsedMillis(startedNanos),
                        merge(cooldownContext, cost.value().auditContext()))) {
                    return ActionResult.failure(
                            ActionResult.ReasonCode.STORAGE_ERROR,
                            "command audit could not be persisted safely");
                }
                return ActionResult.failure(ActionResult.ReasonCode.CONFIRMATION_INVALID, "confirmation binding mismatch");
            }
            ActionResult<ConfirmationService.Request> confirmation =
                    confirmations.consume(request.confirmationToken(), binding);
            if (!confirmation.successful()) {
                cost.value().refund();
                clearCooldown(request, cooldownAcquired);
                if (mandatoryAuditExpected && !audit(request, AuditService.Result.REJECTED, confirmation.reason(), policy.auditClass(),
                        elapsedMillis(startedNanos),
                        merge(cooldownContext, cost.value().auditContext()))) {
                    return ActionResult.failure(
                            ActionResult.ReasonCode.STORAGE_ERROR,
                            "command audit could not be persisted safely");
                }
                return ActionResult.failure(confirmation.reason(), confirmation.detail());
            }
        }

        return ActionResult.success(new Lease(
                request,
                policy,
                !cooldownAcquired,
                cost.value(),
                cooldownContext,
                startedNanos,
                policy.auditClass() != AuditService.AuditClass.NONE
                        && SecurityAuditService.health().running()));
    }

    private void clearCooldown(Request request, boolean cooldownAcquired) {
        if (cooldownAcquired) {
            cooldowns.clear(request.actorId(), request.actionId());
        }
    }

    private static java.math.BigDecimal quotedCost(
            Request request,
            java.math.BigDecimal policyCost
    ) {
        String quoted = request.providerContext().get("quoted_cost");
        if (quoted == null) {
            return policyCost;
        }
        try {
            java.math.BigDecimal amount = new java.math.BigDecimal(quoted);
            if (amount.signum() < 0) {
                throw new IllegalArgumentException("Quoted cost cannot be negative");
            }
            return amount;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Quoted cost is invalid", exception);
        }
    }

    private static Map<String, String> cooldownAuditContext(
            CooldownDurationResolver.Resolution resolution,
            boolean bypass
    ) {
        return Map.of(
                "cooldown_key", resolution.permissionKey(),
                "cooldown_seconds", Long.toString(resolution.duration().getSeconds()),
                "cooldown_provider", resolution.provider(),
                "cooldown_winning_node", resolution.winningNode(),
                "cooldown_fallback", Boolean.toString(resolution.fallback()),
                "cooldown_bypass", Boolean.toString(bypass),
                "cooldown_resolver_revision", Long.toString(resolution.revision()));
    }

    private static Map<String, String> merge(
            Map<String, String> first,
            Map<String, String> second
    ) {
        Map<String, String> result = new java.util.LinkedHashMap<>(first);
        result.putAll(second);
        return Map.copyOf(result);
    }

    private boolean audit(
            Request request,
            AuditService.Result result,
            ActionResult.ReasonCode reason,
            AuditService.AuditClass auditClass,
            long durationMillis
    ) {
        return audit(request, result, reason, auditClass, durationMillis, Map.of());
    }

    private boolean audit(
            Request request,
            AuditService.Result result,
            ActionResult.ReasonCode reason,
            AuditService.AuditClass auditClass,
            long durationMillis,
            Map<String, String> executionContext
    ) {
        Map<String, String> providerContext = new java.util.LinkedHashMap<>(request.providerContext());
        providerContext.putAll(executionContext);
        return AuditService.record(new AuditService.Event(
                1,
                UUID.randomUUID(),
                java.time.Instant.now(),
                request.sessionId(),
                request.actorId(),
                request.actorName(),
                request.sourceType().name(),
                request.actionId(),
                request.targetIds(),
                request.normalizedParameters(),
                result,
                reason,
                durationMillis,
                request.origin(),
                null,
                null,
                request.definitionRevision(),
                policies.revision(),
                providerContext,
                AuditService.RedactionClass.METADATA,
                List.of(),
                null,
                "",
                auditClass));
    }

    public final class Lease implements AutoCloseable {
        private final Request request;
        private final CommandPolicyService.Decision policy;
        private final boolean cooldownNotAcquired;
        private final CostService.Reservation cost;
        private final Map<String, String> cooldownContext;
        private final long startedNanos;
        private final boolean auditExpected;
        private boolean completed;

        private Lease(
                Request request,
                CommandPolicyService.Decision policy,
                boolean cooldownNotAcquired,
                CostService.Reservation cost,
                Map<String, String> cooldownContext,
                long startedNanos,
                boolean auditExpected
        ) {
            this.request = request;
            this.policy = policy;
            this.cooldownNotAcquired = cooldownNotAcquired;
            this.cost = cost;
            this.cooldownContext = Map.copyOf(cooldownContext);
            this.startedNanos = startedNanos;
            this.auditExpected = auditExpected;
        }

        public synchronized ActionResult<Void> complete(
                boolean successful,
                ActionResult.ReasonCode failureReason
        ) {
            if (completed) {
                return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "execution lease is complete");
            }
            completed = true;
            if (successful) {
                ActionResult<Void> committed;
                try {
                    committed = cost.commit();
                } catch (RuntimeException exception) {
                    committed = ActionResult.failure(
                            ActionResult.ReasonCode.COST_UNAVAILABLE,
                            "cost commit could not be completed safely");
                }
                if (!committed.successful()) {
                    if (!cooldownNotAcquired
                            && committed.reason() != ActionResult.ReasonCode.COST_UNAVAILABLE) {
                        cooldowns.clear(request.actorId(), request.actionId());
                    }
                    audit(request,
                            committed.reason() == ActionResult.ReasonCode.COST_UNAVAILABLE
                                    ? AuditService.Result.OUTCOME_UNKNOWN
                                    : AuditService.Result.FAILED,
                            committed.reason(),
                            policy.auditClass(),
                            elapsedMillis(startedNanos), merge(cooldownContext, cost.auditContext()));
                    return committed;
                }
                boolean audited = audit(
                        request,
                        AuditService.Result.SUCCESS,
                        ActionResult.ReasonCode.SUCCESS,
                        policy.auditClass(),
                        elapsedMillis(startedNanos),
                        merge(cooldownContext, cost.auditContext()));
                if (!audited
                        && auditExpected) {
                    return ActionResult.failure(
                            ActionResult.ReasonCode.STORAGE_ERROR,
                            "command audit could not be persisted safely");
                }
                return ActionResult.success(null);
            }

            ActionResult<Void> refunded;
            try {
                refunded = cost.refund();
            } catch (RuntimeException exception) {
                refunded = ActionResult.failure(
                        ActionResult.ReasonCode.COST_UNAVAILABLE,
                        "cost refund could not be completed safely");
            }
            if (!refunded.successful()) {
                audit(request,
                        AuditService.Result.OUTCOME_UNKNOWN,
                        refunded.reason(),
                        policy.auditClass(),
                        elapsedMillis(startedNanos),
                        merge(cooldownContext, cost.auditContext()));
                return refunded;
            }
            if (!cooldownNotAcquired) {
                cooldowns.clear(request.actorId(), request.actionId());
            }
            ActionResult.ReasonCode reason = failureReason == null
                    ? ActionResult.ReasonCode.PROVIDER_ERROR
                    : failureReason;
            audit(request, AuditService.Result.FAILED, reason, policy.auditClass(),
                    elapsedMillis(startedNanos), merge(cooldownContext, cost.auditContext()));
            return ActionResult.failure(reason, "action failed");
        }

        @Override
        public synchronized void close() {
            if (!completed) {
                complete(false, ActionResult.ReasonCode.PROVIDER_ERROR);
            }
        }
    }

    public record Request(
            UUID sessionId,
            UUID actorId,
            String actorName,
            String actionId,
            CommandDefinition.SourceType sourceType,
            String worldId,
            String dimensionId,
            boolean permissionGranted,
            boolean cooldownBypass,
            boolean costBypass,
            boolean warmupBypass,
            String confirmationToken,
            ConfirmationService.Request confirmationBinding,
            WarmupService.Position warmupPosition,
            Set<WarmupService.CancelReason> warmupCancellationPolicy,
            Map<String, String> normalizedParameters,
            List<UUID> targetIds,
            long definitionRevision,
            Map<String, String> providerContext,
            String origin
    ) {
        public Request(
                UUID sessionId,
                UUID actorId,
                String actorName,
                String actionId,
                CommandDefinition.SourceType sourceType,
                String worldId,
                String dimensionId,
                boolean permissionGranted,
                boolean cooldownBypass,
                boolean warmupBypass,
                String confirmationToken,
                ConfirmationService.Request confirmationBinding,
                WarmupService.Position warmupPosition,
                Set<WarmupService.CancelReason> warmupCancellationPolicy,
                Map<String, String> normalizedParameters,
                List<UUID> targetIds,
                long definitionRevision,
                Map<String, String> providerContext,
                String origin
        ) {
            this(
                    sessionId,
                    actorId,
                    actorName,
                    actionId,
                    sourceType,
                    worldId,
                    dimensionId,
                    permissionGranted,
                    cooldownBypass,
                    false,
                    warmupBypass,
                    confirmationToken,
                    confirmationBinding,
                    warmupPosition,
                    warmupCancellationPolicy,
                    normalizedParameters,
                    targetIds,
                    definitionRevision,
                    providerContext,
                    origin);
        }

        public Request {
            Objects.requireNonNull(sessionId, "sessionId");
            Objects.requireNonNull(actorId, "actorId");
            actorName = bounded(actorName, 64);
            actionId = normalize(actionId);
            Objects.requireNonNull(sourceType, "sourceType");
            worldId = normalize(worldId);
            dimensionId = normalize(dimensionId);
            confirmationToken = confirmationToken == null ? "" : bounded(confirmationToken, 128);
            warmupCancellationPolicy = Set.copyOf(Objects.requireNonNull(
                    warmupCancellationPolicy,
                    "warmupCancellationPolicy"));
            Objects.requireNonNull(normalizedParameters, "normalizedParameters");
            if (normalizedParameters.size() > 32) {
                throw new IllegalArgumentException("Execution request has too many parameters");
            }
            normalizedParameters = normalizedParameters.entrySet().stream()
                    .collect(java.util.stream.Collectors.toUnmodifiableMap(
                            entry -> normalize(entry.getKey()),
                            entry -> bounded(Objects.requireNonNull(entry.getValue(), "parameter value"), 256)));
            Objects.requireNonNull(targetIds, "targetIds");
            if (targetIds.size() > 100 || targetIds.stream().distinct().count() != targetIds.size()) {
                throw new IllegalArgumentException("Execution request has invalid targets");
            }
            targetIds = targetIds.stream()
                    .sorted(java.util.Comparator.comparing(UUID::toString))
                    .toList();
            if (definitionRevision < 0L) {
                throw new IllegalArgumentException("Execution definition revision cannot be negative");
            }
            Objects.requireNonNull(providerContext, "providerContext");
            if (providerContext.size() > 32) {
                throw new IllegalArgumentException("Execution request has too much provider context");
            }
            providerContext = providerContext.entrySet().stream()
                    .collect(java.util.stream.Collectors.toUnmodifiableMap(
                            entry -> normalize(entry.getKey()),
                            entry -> bounded(Objects.requireNonNull(entry.getValue(), "provider value"), 256)));
            origin = normalize(origin);
        }
    }

    private static String normalize(String value) {
        return Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
    }

    private static String bounded(String value, int maximumLength) {
        String normalized = Objects.requireNonNullElse(value, "").trim();
        return normalized.length() <= maximumLength
                ? normalized
                : normalized.substring(0, maximumLength);
    }

    private static long elapsedMillis(long startedNanos) {
        return Math.max(0L, java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedNanos));
    }
}
