package com.enviouse.sef.kernel.command;

import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.kernel.ActionResult;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public final class BundleCompiler {
    private static final Pattern ID = Pattern.compile("[a-z][a-z0-9_.-]*:[a-z0-9_./-]+");

    private final CommandCatalog catalog;
    private final int maximumSteps;
    private final int maximumDepth;
    private final int maximumTargets;
    private final int maximumTargetSteps;

    public BundleCompiler(
            CommandCatalog catalog,
            int maximumSteps,
            int maximumDepth,
            int maximumTargets,
            int maximumTargetSteps
    ) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        if (maximumSteps < 1 || maximumSteps > 256
                || maximumDepth < 1 || maximumDepth > 8
                || maximumTargets < 1 || maximumTargets > 1000
                || maximumTargetSteps < 1 || maximumTargetSteps > 100_000) {
            throw new IllegalArgumentException("Bundle limits are outside hard bounds");
        }
        this.maximumSteps = maximumSteps;
        this.maximumDepth = maximumDepth;
        this.maximumTargets = maximumTargets;
        this.maximumTargetSteps = maximumTargetSteps;
    }

    public ActionResult<Map<String, CompiledBundle>> compileAll(Map<String, BundleDefinition> definitions) {
        Objects.requireNonNull(definitions, "definitions");
        if (definitions.size() > 1024) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "bundle definition limit reached");
        }
        Map<String, BundleDefinition> normalized = new LinkedHashMap<>();
        for (BundleDefinition definition : definitions.values()) {
            if (normalized.putIfAbsent(definition.id(), definition) != null) {
                return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, "duplicate bundle id");
            }
        }

        for (BundleDefinition definition : normalized.values()) {
            ActionResult<Void> local = validateLocal(definition, normalized.keySet());
            if (!local.successful()) {
                return ActionResult.failure(local.reason(), definition.id() + ". " + local.detail());
            }
        }

        ActionResult<Void> graph = validateGraph(normalized);
        if (!graph.successful()) {
            return ActionResult.failure(graph.reason(), graph.detail());
        }

        Map<String, CompiledBundle> compiled = new LinkedHashMap<>();
        Map<String, Long> expansionMemo = new LinkedHashMap<>();
        for (Map.Entry<String, BundleDefinition> entry : normalized.entrySet()) {
            long expansion = maximumExpansion(entry.getValue(), normalized, expansionMemo);
            if (expansion > maximumTargetSteps) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.QUOTA_EXCEEDED,
                        entry.getKey() + ". target step expansion exceeds limit");
            }
            compiled.put(entry.getKey(), new CompiledBundle(entry.getValue(), Instant.now(), expansion));
        }
        return ActionResult.success(Map.copyOf(compiled));
    }

    private ActionResult<Void> validateLocal(BundleDefinition definition, Set<String> bundleIds) {
        if (!ID.matcher(definition.id()).matches()) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, "invalid bundle id");
        }
        if (definition.steps().isEmpty() || definition.steps().size() > maximumSteps) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, "invalid step count");
        }
        if (definition.maximumTargets() < 1 || definition.maximumTargets() > maximumTargets) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, "target cap exceeds limit");
        }
        long expansion = maximumExpansion(definition);
        if (expansion > maximumTargetSteps) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "target step expansion exceeds limit");
        }

        Set<String> stepIds = new HashSet<>();
        for (BundleStep step : definition.steps()) {
            if (!stepIds.add(step.id())) {
                return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, "duplicate step id");
            }
            if (step.kind() == StepKind.SEF_ACTION && catalog.find(step.targetId()).isEmpty()) {
                return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "unknown action " + step.targetId());
            }
            if (step.kind() == StepKind.BUNDLE && !bundleIds.contains(step.targetId())) {
                return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "unknown bundle " + step.targetId());
            }
            if (step.kind() == StepKind.DELAY
                    && (step.delay().isNegative() || step.delay().isZero() || step.delay().compareTo(Duration.ofHours(1)) > 0)) {
                return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, "delay is outside bounds");
            }
            if ((step.kind() == StepKind.EXTERNAL_ACTOR_COMMAND
                    || step.kind() == StepKind.SERVER_COMMAND_PROFILE)
                    && step.targetId().isBlank()) {
                return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, "external step requires profile id");
            }
            if (step.kind() == StepKind.RAW_COMMAND) {
                return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "raw command steps are forbidden");
            }
        }
        return ActionResult.success(null);
    }

    private ActionResult<Void> validateGraph(Map<String, BundleDefinition> definitions) {
        Set<String> complete = new HashSet<>();
        Deque<String> path = new ArrayDeque<>();
        for (String id : definitions.keySet()) {
            ActionResult<Void> result = visit(id, definitions, complete, path, 1);
            if (!result.successful()) {
                return result;
            }
        }
        return ActionResult.success(null);
    }

    private ActionResult<Void> visit(
            String id,
            Map<String, BundleDefinition> definitions,
            Set<String> complete,
            Deque<String> path,
            int depth
    ) {
        if (complete.contains(id)) {
            return ActionResult.success(null);
        }
        if (path.contains(id)) {
            return ActionResult.failure(ActionResult.ReasonCode.RECURSION_DENIED, "bundle cycle includes " + id);
        }
        if (depth > maximumDepth) {
            return ActionResult.failure(ActionResult.ReasonCode.RECURSION_DENIED, "bundle depth exceeds limit");
        }
        path.addLast(id);
        for (BundleStep step : definitions.get(id).steps()) {
            if (step.kind() != StepKind.BUNDLE) {
                continue;
            }
            ActionResult<Void> nested = visit(step.targetId(), definitions, complete, path, depth + 1);
            if (!nested.successful()) {
                return nested;
            }
        }
        path.removeLast();
        complete.add(id);
        return ActionResult.success(null);
    }

    private long maximumExpansion(BundleDefinition definition) {
        return (long) definition.steps().size() * definition.maximumTargets();
    }

    private long maximumExpansion(
            BundleDefinition definition,
            Map<String, BundleDefinition> definitions,
            Map<String, Long> memo
    ) {
        Long cached = memo.get(definition.id());
        if (cached != null) {
            return cached;
        }
        long steps = 0L;
        for (BundleStep step : definition.steps()) {
            long contribution = step.kind() == StepKind.BUNDLE
                    ? maximumExpansion(definitions.get(step.targetId()), definitions, memo)
                    : 1L;
            steps = saturatedAdd(steps, contribution);
        }
        long expansion = saturatedMultiply(steps, definition.maximumTargets());
        memo.put(definition.id(), expansion);
        return expansion;
    }

    private long saturatedAdd(long left, long right) {
        if (left > maximumTargetSteps || right > maximumTargetSteps
                || left > Long.MAX_VALUE - right) {
            return (long) maximumTargetSteps + 1L;
        }
        return left + right;
    }

    private long saturatedMultiply(long left, long right) {
        if (left == 0L || right == 0L) {
            return 0L;
        }
        if (left > maximumTargetSteps || right > maximumTargetSteps
                || left > Long.MAX_VALUE / right) {
            return (long) maximumTargetSteps + 1L;
        }
        long result = left * right;
        return result > maximumTargetSteps ? (long) maximumTargetSteps + 1L : result;
    }

    public record BundleDefinition(
            int schemaVersion,
            String id,
            long revision,
            DefinitionState state,
            boolean enabled,
            String additionalPermissionId,
            Set<CommandDefinition.SourceType> sourceTypes,
            AuthorizationMode authorizationMode,
            ExecutionMode executionMode,
            int maximumTargets,
            int actionsPerTick,
            Duration maximumDuration,
            boolean confirmationRequired,
            AuditService.AuditClass auditClass,
            List<BundleStep> steps
    ) {
        public BundleDefinition {
            if (schemaVersion != 1) {
                throw new IllegalArgumentException("Unsupported bundle schema");
            }
            id = normalize(id);
            additionalPermissionId = additionalPermissionId == null ? "" : normalize(additionalPermissionId);
            Objects.requireNonNull(state, "state");
            sourceTypes = Set.copyOf(Objects.requireNonNull(sourceTypes, "sourceTypes"));
            Objects.requireNonNull(authorizationMode, "authorizationMode");
            Objects.requireNonNull(executionMode, "executionMode");
            Objects.requireNonNull(maximumDuration, "maximumDuration");
            Objects.requireNonNull(auditClass, "auditClass");
            steps = List.copyOf(Objects.requireNonNull(steps, "steps"));
            if (revision < 1 || sourceTypes.isEmpty() || maximumTargets < 1
                    || actionsPerTick < 1 || actionsPerTick > 20
                    || maximumDuration.isNegative() || maximumDuration.isZero()
                    || maximumDuration.compareTo(Duration.ofHours(1)) > 0) {
                throw new IllegalArgumentException("Bundle policy is outside bounds");
            }
        }
    }

    public record BundleStep(
            String id,
            StepKind kind,
            String targetId,
            TargetBinding targetBinding,
            FailureBehavior failureBehavior,
            Duration delay,
            Map<String, String> typedBindings
    ) {
        public BundleStep {
            id = normalize(id);
            Objects.requireNonNull(kind, "kind");
            targetId = targetId == null ? "" : normalize(targetId);
            Objects.requireNonNull(targetBinding, "targetBinding");
            Objects.requireNonNull(failureBehavior, "failureBehavior");
            delay = delay == null ? Duration.ZERO : delay;
            typedBindings = boundedBindings(typedBindings);
            if (id.isBlank()) {
                throw new IllegalArgumentException("Bundle step id is empty");
            }
        }
    }

    public record CompiledBundle(BundleDefinition definition, Instant compiledAt, long maximumTargetSteps) {
    }

    public record BundleJob(
            UUID jobId,
            String bundleId,
            long bundleRevision,
            UUID issuerId,
            UUID correlationId,
            JobState state,
            int nextStep,
            int completedActions,
            int failedActions,
            Instant createdAt,
            Instant deadline
    ) {
        public BundleJob {
            Objects.requireNonNull(jobId, "jobId");
            bundleId = normalize(bundleId);
            Objects.requireNonNull(correlationId, "correlationId");
            Objects.requireNonNull(state, "state");
            Objects.requireNonNull(createdAt, "createdAt");
            Objects.requireNonNull(deadline, "deadline");
            if (bundleRevision < 1 || nextStep < 0 || completedActions < 0 || failedActions < 0
                    || !deadline.isAfter(createdAt)) {
                throw new IllegalArgumentException("Invalid bundle job state");
            }
        }
    }

    public interface ExecutionProfile {
        String id();

        long revision();

        boolean permits(String actionId, int targetCount);

        boolean delegated();
    }

    public interface PolicyHook {
        ActionResult<Void> beforeStep(BundleJob job, BundleStep step, UUID targetId);

        default void afterStep(BundleJob job, BundleStep step, UUID targetId, ActionResult<?> result) {
        }
    }

    public enum DefinitionState {
        DRAFT,
        PUBLISHED
    }

    public enum StepKind {
        SEF_ACTION,
        BUNDLE,
        EXTERNAL_ACTOR_COMMAND,
        SERVER_COMMAND_PROFILE,
        DELAY,
        CONDITION,
        NOTICE,
        CHECKPOINT,
        RAW_COMMAND
    }

    public enum AuthorizationMode {
        STRICT_ACTOR,
        DELEGATED_ACTION_PROFILE,
        EXTERNAL_ACTOR,
        SERVER_PROFILE
    }

    public enum ExecutionMode {
        STOP_ON_FAILURE,
        CONTINUE_ON_FAILURE,
        COMPENSATE_ON_FAILURE,
        ATOMIC_DOMAIN
    }

    public enum TargetBinding {
        ACTOR,
        SELECTED_PLAYER,
        EXPLICIT_VISIBLE_IDENTITY,
        PREVIOUS_STEP_RESULT,
        APPROVED_AUDIENCE,
        FIXED_IDENTITY,
        NO_PLAYER
    }

    public enum FailureBehavior {
        STOP,
        CONTINUE,
        COMPENSATE
    }

    public enum JobState {
        PREVIEWED,
        AWAITING_CONFIRMATION,
        QUEUED,
        RUNNING,
        PAUSED,
        COMPLETED,
        COMPLETED_WITH_FAILURES,
        CANCELLED,
        EXPIRED,
        FAILED,
        RECOVERING
    }

    private static Map<String, String> boundedBindings(Map<String, String> bindings) {
        Objects.requireNonNull(bindings, "bindings");
        if (bindings.size() > 32) {
            throw new IllegalArgumentException("Too many bundle bindings");
        }
        Map<String, String> bounded = new LinkedHashMap<>();
        bindings.forEach((key, value) -> {
            String normalizedKey = normalize(key);
            String normalizedValue = Objects.requireNonNull(value, "value").trim();
            if (normalizedValue.length() > 256) {
                throw new IllegalArgumentException("Bundle binding exceeds size limit");
            }
            bounded.put(normalizedKey, normalizedValue);
        });
        return Map.copyOf(bounded);
    }

    private static String normalize(String value) {
        return Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
    }
}
