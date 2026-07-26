package com.enviouse.sef.kernel.policy;

import com.enviouse.sef.kernel.ActionResult;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class FeatureGateService {
    private final AtomicReference<Snapshot> snapshot =
            new AtomicReference<>(new Snapshot(1L, Map.of(), Map.of(), Map.of()));

    public Decision decide(String featureId, Context context) {
        String id = normalize(featureId);
        Objects.requireNonNull(context, "context");
        Snapshot current = snapshot.get();
        Boolean action = current.actionOverrides().get(context.actionId());
        if (action != null) {
            return new Decision(action, id, current.revision(), action ? "action override enabled" : "action override disabled");
        }
        Boolean dimension = current.dimensionOverrides().get(context.dimensionId() + "|" + id);
        if (dimension != null) {
            return new Decision(dimension, id, current.revision(), dimension ? "dimension override enabled" : "dimension override disabled");
        }
        boolean enabled = current.features().getOrDefault(id, false);
        return new Decision(enabled, id, current.revision(), enabled ? "feature enabled" : "feature disabled");
    }

    public ActionResult<Void> require(String featureId, Context context) {
        Decision decision = decide(featureId, context);
        return decision.enabled()
                ? ActionResult.success(null)
                : ActionResult.failure(ActionResult.ReasonCode.FEATURE_DISABLED, decision.explanation());
    }

    public void publish(Snapshot replacement) {
        Objects.requireNonNull(replacement, "replacement");
        Snapshot current = snapshot.get();
        if (replacement.revision() <= current.revision()) {
            throw new IllegalArgumentException("Feature snapshot revision must increase");
        }
        snapshot.set(replacement);
    }

    public Snapshot snapshot() {
        return snapshot.get();
    }

    public record Snapshot(
            long revision,
            Map<String, Boolean> features,
            Map<String, Boolean> dimensionOverrides,
            Map<String, Boolean> actionOverrides
    ) {
        public Snapshot {
            if (revision < 1) {
                throw new IllegalArgumentException("Feature revision must be positive");
            }
            features = normalizeMap(features);
            dimensionOverrides = normalizeMap(dimensionOverrides);
            actionOverrides = normalizeMap(actionOverrides);
        }
    }

    public record Context(String worldId, String dimensionId, String actionId) {
        public Context {
            worldId = normalize(worldId);
            dimensionId = normalize(dimensionId);
            actionId = normalize(actionId);
        }

        public static Context server(String actionId) {
            return new Context("server", "server", actionId);
        }
    }

    public record Decision(boolean enabled, String featureId, long revision, String explanation) {
    }

    private static Map<String, Boolean> normalizeMap(Map<String, Boolean> values) {
        Objects.requireNonNull(values, "values");
        if (values.size() > 8192) {
            throw new IllegalArgumentException("Feature map exceeds hard limit");
        }
        Map<String, Boolean> normalized = new LinkedHashMap<>();
        values.forEach((key, value) -> {
            if (normalized.putIfAbsent(normalize(key), Objects.requireNonNull(value, "value")) != null) {
                throw new IllegalArgumentException("Duplicate normalized feature key");
            }
        });
        return Map.copyOf(normalized);
    }

    private static String normalize(String value) {
        String normalized = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank() || normalized.length() > 256) {
            throw new IllegalArgumentException("Feature identifier is outside bounds");
        }
        return normalized;
    }
}
