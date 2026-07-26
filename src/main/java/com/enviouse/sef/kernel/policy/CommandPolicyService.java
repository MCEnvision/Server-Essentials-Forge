package com.enviouse.sef.kernel.policy;

import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.command.CommandDefinition;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public final class CommandPolicyService {
    private final FeatureGateService featureGates;
    private final Map<String, Policy> policies = new LinkedHashMap<>();
    private final AtomicLong revision = new AtomicLong(1L);

    public CommandPolicyService(FeatureGateService featureGates) {
        this.featureGates = Objects.requireNonNull(featureGates, "featureGates");
    }

    public synchronized void register(Policy policy) {
        Objects.requireNonNull(policy, "policy");
        if (!policies.containsKey(policy.actionId()) && policies.size() >= 8192) {
            throw new IllegalStateException("Command policy limit reached");
        }
        if (policies.putIfAbsent(policy.actionId(), policy) != null) {
            throw new IllegalStateException("Duplicate command policy " + policy.actionId());
        }
        revision.incrementAndGet();
    }

    public synchronized void replaceAll(List<Policy> replacements) {
        if (Objects.requireNonNull(replacements, "replacements").size() > 8192) {
            throw new IllegalArgumentException("Command policy limit exceeded");
        }
        Map<String, Policy> validated = new LinkedHashMap<>();
        for (Policy policy : replacements) {
            if (validated.putIfAbsent(policy.actionId(), policy) != null) {
                throw new IllegalArgumentException("Duplicate command policy " + policy.actionId());
            }
        }
        policies.clear();
        policies.putAll(validated);
        revision.incrementAndGet();
    }

    public Decision decide(Context context) {
        Objects.requireNonNull(context, "context");
        Policy policy;
        synchronized (this) {
            policy = policies.get(context.actionId());
        }
        if (policy == null) {
            return Decision.denied(context.actionId(), revision.get(), ActionResult.ReasonCode.POLICY_DENIED,
                    "no policy registered");
        }
        FeatureGateService.Decision feature = featureGates.decide(
                policy.featureId(),
                new FeatureGateService.Context(context.worldId(), context.dimensionId(), context.actionId()));
        if (!feature.enabled()) {
            return Decision.denied(context.actionId(), revision.get(), ActionResult.ReasonCode.FEATURE_DISABLED,
                    feature.explanation());
        }
        if (!policy.sourceTypes().contains(context.sourceType())) {
            return Decision.denied(context.actionId(), revision.get(), ActionResult.ReasonCode.SOURCE_NOT_ALLOWED,
                    "source type is not allowed");
        }
        if (policy.hardDenied()) {
            return Decision.denied(context.actionId(), revision.get(), ActionResult.ReasonCode.POLICY_DENIED,
                    "action is hard denied");
        }
        return new Decision(
                true,
                context.actionId(),
                revision.get(),
                ActionResult.ReasonCode.SUCCESS,
                "allowed",
                policy.confirmationRequired(),
                policy.cooldown(),
                policy.warmup(),
                policy.cost(),
                policy.auditClass());
    }

    public synchronized List<Policy> policies() {
        return policies.values().stream().sorted(Comparator.comparing(Policy::actionId)).toList();
    }

    public long revision() {
        return revision.get();
    }

    public record Policy(
            String actionId,
            String featureId,
            Set<CommandDefinition.SourceType> sourceTypes,
            boolean hardDenied,
            boolean confirmationRequired,
            Duration cooldown,
            Duration warmup,
            BigDecimal cost,
            AuditService.AuditClass auditClass
    ) {
        public Policy {
            actionId = normalize(actionId);
            featureId = normalize(featureId);
            sourceTypes = Set.copyOf(Objects.requireNonNull(sourceTypes, "sourceTypes"));
            Objects.requireNonNull(cooldown, "cooldown");
            Objects.requireNonNull(warmup, "warmup");
            Objects.requireNonNull(cost, "cost");
            Objects.requireNonNull(auditClass, "auditClass");
            if (sourceTypes.isEmpty() || cooldown.isNegative() || warmup.isNegative() || cost.signum() < 0
                    || cooldown.compareTo(Duration.ofDays(365)) > 0
                    || warmup.compareTo(Duration.ofHours(1)) > 0
                    || cost.scale() > 8
                    || cost.compareTo(new BigDecimal("1000000000000000000")) > 0) {
                throw new IllegalArgumentException("Command policy is outside bounds");
            }
        }
    }

    public record Context(
            String actionId,
            CommandDefinition.SourceType sourceType,
            String worldId,
            String dimensionId
    ) {
        public Context {
            actionId = normalize(actionId);
            Objects.requireNonNull(sourceType, "sourceType");
            worldId = normalize(worldId);
            dimensionId = normalize(dimensionId);
        }
    }

    public record Decision(
            boolean allowed,
            String actionId,
            long revision,
            ActionResult.ReasonCode reason,
            String explanation,
            boolean confirmationRequired,
            Duration cooldown,
            Duration warmup,
            BigDecimal cost,
            AuditService.AuditClass auditClass
    ) {
        private static Decision denied(
                String actionId,
                long revision,
                ActionResult.ReasonCode reason,
                String explanation
        ) {
            return new Decision(
                    false,
                    actionId,
                    revision,
                    reason,
                    explanation,
                    false,
                    Duration.ZERO,
                    Duration.ZERO,
                    BigDecimal.ZERO,
                    AuditService.AuditClass.METADATA_ONLY);
        }
    }

    private static String normalize(String value) {
        String normalized = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank() || normalized.length() > 128) {
            throw new IllegalArgumentException("Command policy identifier is outside bounds");
        }
        return normalized;
    }
}
