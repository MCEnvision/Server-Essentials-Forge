package com.enviouse.sef.kernel.policy;

import com.enviouse.sef.kernel.ActionResult;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public final class TargetHierarchyService {
    private final AtomicReference<Snapshot> snapshot = new AtomicReference<>(Snapshot.defaults());

    public Decision decide(Context context) {
        Objects.requireNonNull(context, "context");
        Snapshot current = snapshot.get();
        if (context.targetExempt() && !context.exemptionBypass()) {
            return new Decision(false, 0, 0, false, true, ActionResult.ReasonCode.TARGET_EXEMPT, current.revision());
        }
        if (context.console() && current.consoleMayTarget()) {
            return new Decision(true, Integer.MAX_VALUE, resolveTarget(context, current), true, false,
                    ActionResult.ReasonCode.SUCCESS, current.revision());
        }
        if (context.hierarchyBypass()) {
            return new Decision(true, resolveActor(context, current), resolveTarget(context, current), true, false,
                    ActionResult.ReasonCode.SUCCESS, current.revision());
        }
        if (context.actorId() != null && context.actorId().equals(context.targetId()) && context.rejectSelf()) {
            return new Decision(false, resolveActor(context, current), resolveTarget(context, current), false, false,
                    ActionResult.ReasonCode.TARGET_DENIED, current.revision());
        }
        int actor = resolveActor(context, current);
        int target = resolveTarget(context, current);
        boolean allowed = actor > target || context.allowEqual();
        if (context.allowEqual() && actor < target) {
            allowed = false;
        }
        return new Decision(
                allowed,
                actor,
                target,
                false,
                false,
                allowed ? ActionResult.ReasonCode.SUCCESS : ActionResult.ReasonCode.HIERARCHY_DENIED,
                current.revision());
    }

    public void publish(Snapshot replacement) {
        Objects.requireNonNull(replacement, "replacement");
        if (replacement.revision() <= snapshot.get().revision()) {
            throw new IllegalArgumentException("Hierarchy revision must increase");
        }
        snapshot.set(replacement);
    }

    public Snapshot snapshot() {
        return snapshot.get();
    }

    private int resolveActor(Context context, Snapshot current) {
        if (context.actorProviderWeight() != null) {
            return context.actorProviderWeight();
        }
        return current.fallbackWeights().getOrDefault(normalize(context.actorTier()), 0);
    }

    private int resolveTarget(Context context, Snapshot current) {
        if (context.targetProviderWeight() != null) {
            return context.targetProviderWeight();
        }
        return current.fallbackWeights().getOrDefault(normalize(context.targetTier()), 0);
    }

    public record Context(
            UUID actorId,
            UUID targetId,
            boolean console,
            boolean hierarchyBypass,
            boolean targetExempt,
            boolean exemptionBypass,
            boolean rejectSelf,
            boolean allowEqual,
            Integer actorProviderWeight,
            Integer targetProviderWeight,
            String actorTier,
            String targetTier
    ) {
        public Context {
            actorTier = normalize(actorTier);
            targetTier = normalize(targetTier);
            if ((actorProviderWeight != null && (actorProviderWeight < 0 || actorProviderWeight > 1_000_000))
                    || (targetProviderWeight != null
                    && (targetProviderWeight < 0 || targetProviderWeight > 1_000_000))) {
                throw new IllegalArgumentException("Hierarchy provider weight is outside bounds");
            }
        }
    }

    public record Decision(
            boolean allowed,
            int actorWeight,
            int targetWeight,
            boolean bypassed,
            boolean exempt,
            ActionResult.ReasonCode reason,
            long revision
    ) {
    }

    public record Snapshot(long revision, boolean consoleMayTarget, Map<String, Integer> fallbackWeights) {
        public Snapshot {
            if (revision < 1) {
                throw new IllegalArgumentException("Hierarchy revision must be positive");
            }
            Objects.requireNonNull(fallbackWeights, "fallbackWeights");
            if (fallbackWeights.size() > 64
                    || fallbackWeights.values().stream().anyMatch(
                    value -> value == null || value < 0 || value > 1_000_000)) {
                throw new IllegalArgumentException("Hierarchy fallback weights are outside bounds");
            }
            fallbackWeights = fallbackWeights.entrySet().stream()
                    .collect(java.util.stream.Collectors.toUnmodifiableMap(
                            entry -> normalize(entry.getKey()),
                            Map.Entry::getValue));
        }

        public static Snapshot defaults() {
            return new Snapshot(1L, true, Map.of(
                    "owner", 500,
                    "administrator", 400,
                    "moderator", 300,
                    "helper", 200,
                    "player", 100));
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
