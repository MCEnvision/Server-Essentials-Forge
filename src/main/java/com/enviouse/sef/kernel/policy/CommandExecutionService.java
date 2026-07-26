package com.enviouse.sef.kernel.policy;

import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.command.CommandDefinition;

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

    public CommandExecutionService(
            CommandPolicyService policies,
            CooldownService cooldowns,
            CostService costs,
            WarmupService warmups,
            ConfirmationService confirmations
    ) {
        this.policies = Objects.requireNonNull(policies, "policies");
        this.cooldowns = Objects.requireNonNull(cooldowns, "cooldowns");
        this.costs = Objects.requireNonNull(costs, "costs");
        this.warmups = Objects.requireNonNull(warmups, "warmups");
        this.confirmations = Objects.requireNonNull(confirmations, "confirmations");
    }

    public ActionResult<Lease> begin(Request request) {
        Objects.requireNonNull(request, "request");
        if (!request.permissionGranted()) {
            audit(request, AuditService.Result.REJECTED, ActionResult.ReasonCode.PERMISSION_DENIED,
                    AuditService.AuditClass.METADATA_ONLY);
            return ActionResult.failure(ActionResult.ReasonCode.PERMISSION_DENIED, "permission denied");
        }

        CommandPolicyService.Decision policy = policies.decide(new CommandPolicyService.Context(
                request.actionId(),
                request.sourceType(),
                request.worldId(),
                request.dimensionId()));
        if (!policy.allowed()) {
            audit(request, AuditService.Result.REJECTED, policy.reason(), policy.auditClass());
            return ActionResult.failure(policy.reason(), policy.explanation());
        }
        if (!policy.warmup().isZero()) {
            if (request.warmupPosition() == null) {
                audit(request, AuditService.Result.REJECTED, ActionResult.ReasonCode.INVALID_INPUT,
                        policy.auditClass());
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
                audit(request, AuditService.Result.REJECTED, warmup.reason(), policy.auditClass());
                return ActionResult.failure(warmup.reason(), warmup.detail());
            }
        }
        if (policy.confirmationRequired()) {
            if (request.confirmationToken().isBlank() || request.confirmationBinding() == null) {
                audit(request, AuditService.Result.REJECTED, ActionResult.ReasonCode.CONFIRMATION_REQUIRED,
                        policy.auditClass());
                return ActionResult.failure(ActionResult.ReasonCode.CONFIRMATION_REQUIRED, "confirmation required");
            }
            ConfirmationService.Request binding = request.confirmationBinding();
            if (!binding.actorId().equals(request.actorId())
                    || !binding.actionId().equals(request.actionId())
                    || !binding.normalizedParameters().equals(request.normalizedParameters())
                    || !binding.targetIds().equals(request.targetIds())
                    || binding.policyRevision() != policy.revision()) {
                audit(request, AuditService.Result.REJECTED, ActionResult.ReasonCode.CONFIRMATION_INVALID,
                        policy.auditClass());
                return ActionResult.failure(ActionResult.ReasonCode.CONFIRMATION_INVALID, "confirmation binding mismatch");
            }
            ActionResult<ConfirmationService.Request> confirmation =
                    confirmations.consume(request.confirmationToken(), binding);
            if (!confirmation.successful()) {
                audit(request, AuditService.Result.REJECTED, confirmation.reason(), policy.auditClass());
                return ActionResult.failure(confirmation.reason(), confirmation.detail());
            }
        }

        ActionResult<CostService.Reservation> cost = costs.reserve(
                request.actorId(),
                request.actionId(),
                policy.cost());
        if (!cost.successful()) {
            audit(request, AuditService.Result.REJECTED, cost.reason(), policy.auditClass());
            return ActionResult.failure(cost.reason(), cost.detail());
        }

        CooldownService.Decision cooldown = cooldowns.tryAcquire(
                request.actorId(),
                request.actionId(),
                policy.cooldown(),
                request.cooldownBypass());
        if (!cooldown.allowed()) {
            cost.value().close();
            audit(request, AuditService.Result.REJECTED, cooldown.reason(), policy.auditClass());
            return ActionResult.failure(
                    cooldown.reason(),
                    Long.toString(cooldown.remainingSeconds()));
        }

        return ActionResult.success(new Lease(
                request,
                policy,
                cooldown.bypassed() || policy.cooldown().isZero(),
                cost.value()));
    }

    private void audit(
            Request request,
            AuditService.Result result,
            ActionResult.ReasonCode reason,
            AuditService.AuditClass auditClass
    ) {
        AuditService.record(AuditService.Event.metadata(
                request.sessionId(),
                request.actorId(),
                request.actorName(),
                request.sourceType().name(),
                request.actionId(),
                request.targetIds(),
                result,
                reason,
                request.origin(),
                auditClass));
    }

    public final class Lease implements AutoCloseable {
        private final Request request;
        private final CommandPolicyService.Decision policy;
        private final boolean cooldownNotAcquired;
        private final CostService.Reservation cost;
        private boolean completed;

        private Lease(
                Request request,
                CommandPolicyService.Decision policy,
                boolean cooldownNotAcquired,
                CostService.Reservation cost
        ) {
            this.request = request;
            this.policy = policy;
            this.cooldownNotAcquired = cooldownNotAcquired;
            this.cost = cost;
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
                ActionResult<Void> committed = cost.commit();
                if (!committed.successful()) {
                    if (!cooldownNotAcquired) {
                        cooldowns.clear(request.actorId(), request.actionId());
                    }
                    audit(request, AuditService.Result.FAILED, committed.reason(), policy.auditClass());
                    return committed;
                }
                audit(request, AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS, policy.auditClass());
                return ActionResult.success(null);
            }

            cost.refund();
            if (!cooldownNotAcquired) {
                cooldowns.clear(request.actorId(), request.actionId());
            }
            ActionResult.ReasonCode reason = failureReason == null
                    ? ActionResult.ReasonCode.PROVIDER_ERROR
                    : failureReason;
            audit(request, AuditService.Result.FAILED, reason, policy.auditClass());
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
            String confirmationToken,
            ConfirmationService.Request confirmationBinding,
            WarmupService.Position warmupPosition,
            Set<WarmupService.CancelReason> warmupCancellationPolicy,
            Map<String, String> normalizedParameters,
            List<UUID> targetIds,
            String origin
    ) {
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
}
