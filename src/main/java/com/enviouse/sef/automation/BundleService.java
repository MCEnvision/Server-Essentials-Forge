package com.enviouse.sef.automation;

import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.command.BundleCompiler;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.storage.InstantJsonAdapter;
import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public final class BundleService implements StorageRepository {
    public static final int SCHEMA_VERSION = 1;
    private static final int MAXIMUM_DEFINITIONS = 256;
    private static final int MAXIMUM_HISTORY = 32;
    private static final int MAXIMUM_JOBS = 512;
    private static final Pattern ID = Pattern.compile("[a-z][a-z0-9_.-]*:[a-z0-9_./-]+");
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantJsonAdapter())
            .registerTypeAdapter(Duration.class, new DurationJsonAdapter())
            .create();

    private final BundleCompiler compiler;
    private final Map<String, BundleCompiler.BundleDefinition> drafts = new LinkedHashMap<>();
    private final Map<String, BundleCompiler.BundleDefinition> published = new LinkedHashMap<>();
    private final Map<String, List<BundleCompiler.BundleDefinition>> history = new LinkedHashMap<>();
    private final Map<UUID, RuntimeJob> jobs = new LinkedHashMap<>();
    private StorageService.Document document;
    private RepositoryState state = RepositoryState.NEW;
    private Path path;
    private long revision;
    private long flushedRevision;

    public BundleService(BundleCompiler compiler) {
        this.compiler = Objects.requireNonNull(compiler, "compiler");
    }

    @Override
    public String id() {
        return "sef:bundles";
    }

    @Override
    public String domain() {
        return "bundles";
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public synchronized Path path() {
        return path;
    }

    @Override
    public synchronized LoadResult load(Path managedRoot) {
        path = Objects.requireNonNull(managedRoot, "managedRoot")
                .resolve("bundles.json")
                .toAbsolutePath()
                .normalize();
        drafts.clear();
        published.clear();
        history.clear();
        jobs.clear();
        revision = 0L;
        flushedRevision = 0L;
        boolean existed = Files.exists(path);
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(null);
        if (document == null) {
            state = existed ? RepositoryState.RECOVERY : RepositoryState.MISSING;
            if (!existed) {
                seedBuiltinTemplates();
            }
            return new LoadResult(state, existed ? "storage unavailable" : "new repository");
        }
        try {
            Snapshot snapshot = GSON.fromJson(document.data(), Snapshot.class);
            if (snapshot == null
                    || snapshot.drafts().size() + snapshot.published().size() > MAXIMUM_DEFINITIONS
                    || snapshot.history().size() > MAXIMUM_DEFINITIONS
                    || snapshot.jobs().size() > MAXIMUM_JOBS) {
                throw new IllegalStateException("Bundle collections are outside bounds");
            }
            for (BundleCompiler.BundleDefinition definition : snapshot.drafts()) {
                requireState(definition, BundleCompiler.DefinitionState.DRAFT);
                putUnique(drafts, definition);
            }
            for (BundleCompiler.BundleDefinition definition : snapshot.published()) {
                requireState(definition, BundleCompiler.DefinitionState.PUBLISHED);
                putUnique(published, definition);
            }
            ActionResult<Map<String, BundleCompiler.CompiledBundle>> compiled =
                    compiler.compileAll(enabledPublications(published));
            if (!compiled.successful()) {
                throw new IllegalStateException(compiled.detail());
            }
            for (HistoryRecord record : snapshot.history()) {
                String id = normalizeId(record.id());
                if (record.revisions().size() > MAXIMUM_HISTORY) {
                    throw new IllegalStateException("Bundle history limit exceeded");
                }
                for (BundleCompiler.BundleDefinition definition : record.revisions()) {
                    requireState(definition, BundleCompiler.DefinitionState.PUBLISHED);
                    if (!definition.id().equals(id)) {
                        throw new IllegalStateException("Bundle history identity mismatch");
                    }
                }
                history.put(id, record.revisions());
            }
            for (RuntimeJob job : snapshot.jobs()) {
                RuntimeJob recovered = job.recover();
                if (jobs.putIfAbsent(recovered.jobId(), recovered) != null) {
                    throw new IllegalStateException("Duplicate bundle job");
                }
            }
            state = RepositoryState.READY;
            if (document.migrated()) {
                revision++;
            }
            return new LoadResult(state, "loaded " + published.size() + " bundles and " + jobs.size() + " jobs");
        } catch (RuntimeException exception) {
            drafts.clear();
            published.clear();
            history.clear();
            jobs.clear();
            state = RepositoryState.RECOVERY;
            return new LoadResult(state, exception.getClass().getSimpleName());
        }
    }

    public synchronized ActionResult<BundleCompiler.BundleDefinition> createDraft(String id, UUID actorId) {
        writable();
        String normalizedId = normalizeId(id);
        if (drafts.containsKey(normalizedId) || published.containsKey(normalizedId)) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "bundle id already exists");
        }
        if (drafts.size() + published.size() >= MAXIMUM_DEFINITIONS) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "bundle definition limit reached");
        }
        BundleCompiler.BundleDefinition definition = new BundleCompiler.BundleDefinition(
                SCHEMA_VERSION,
                normalizedId,
                1L,
                BundleCompiler.DefinitionState.DRAFT,
                true,
                "",
                Set.of(CommandDefinition.SourceType.PLAYER),
                BundleCompiler.AuthorizationMode.STRICT_ACTOR,
                BundleCompiler.ExecutionMode.STOP_ON_FAILURE,
                1,
                1,
                Duration.ofMinutes(5),
                true,
                AuditService.AuditClass.WORKFLOW_EXECUTION,
                List.of(new BundleCompiler.BundleStep(
                        "notice",
                        BundleCompiler.StepKind.NOTICE,
                        "bundle_preview",
                        BundleCompiler.TargetBinding.ACTOR,
                        BundleCompiler.FailureBehavior.STOP,
                        Duration.ZERO,
                        Map.of("message", "bundle draft"))));
        drafts.put(normalizedId, definition);
        revision++;
        return ActionResult.success(definition);
    }

    public synchronized ActionResult<BundleCompiler.BundleDefinition> saveDraft(
            BundleCompiler.BundleDefinition requested,
            long expectedRevision
    ) {
        writable();
        Objects.requireNonNull(requested, "requested");
        String id = normalizeId(requested.id());
        BundleCompiler.BundleDefinition current = drafts.get(id);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "bundle draft not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "bundle draft revision changed");
        }
        BundleCompiler.BundleDefinition replacement = copy(
                requested,
                Math.addExact(current.revision(), 1L),
                requested.enabled(),
                BundleCompiler.DefinitionState.DRAFT);
        Map<String, BundleCompiler.BundleDefinition> candidates = enabledPublications(published);
        candidates.put(id, copy(
                replacement,
                replacement.revision(),
                true,
                BundleCompiler.DefinitionState.PUBLISHED));
        ActionResult<Map<String, BundleCompiler.CompiledBundle>> validation = compiler.compileAll(candidates);
        if (!validation.successful()) {
            return ActionResult.failure(validation.reason(), validation.detail());
        }
        drafts.put(id, replacement);
        revision++;
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<BundleCompiler.BundleDefinition> addStep(
            String id,
            long expectedRevision,
            BundleCompiler.BundleStep step
    ) {
        BundleCompiler.BundleDefinition current = drafts.get(normalizeId(id));
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "bundle draft not found");
        }
        if (current.steps().stream().anyMatch(existing -> existing.id().equals(step.id()))) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "bundle step id already exists");
        }
        List<BundleCompiler.BundleStep> steps = new ArrayList<>(current.steps());
        if (steps.size() == 1 && steps.getFirst().kind() == BundleCompiler.StepKind.NOTICE
                && steps.getFirst().targetId().equals("bundle_preview")) {
            steps.clear();
        }
        steps.add(Objects.requireNonNull(step, "step"));
        return saveDraft(withSteps(current, steps), expectedRevision);
    }

    public synchronized ActionResult<BundleCompiler.BundleDefinition> removeStep(
            String id,
            long expectedRevision,
            String stepId
    ) {
        BundleCompiler.BundleDefinition current = drafts.get(normalizeId(id));
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "bundle draft not found");
        }
        String normalizedStep = normalizePart(stepId);
        List<BundleCompiler.BundleStep> steps = current.steps().stream()
                .filter(step -> !step.id().equals(normalizedStep))
                .toList();
        if (steps.size() == current.steps().size()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "bundle step not found");
        }
        if (steps.isEmpty()) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, "bundle must keep one step");
        }
        return saveDraft(withSteps(current, steps), expectedRevision);
    }

    public synchronized ActionResult<BundleCompiler.BundleDefinition> publish(
            String id,
            long expectedRevision
    ) {
        writable();
        String normalizedId = normalizeId(id);
        BundleCompiler.BundleDefinition draft = drafts.get(normalizedId);
        if (draft == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "bundle draft not found");
        }
        if (draft.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "bundle draft revision changed");
        }
        BundleCompiler.BundleDefinition current = published.get(normalizedId);
        long next = Math.max(draft.revision(), current == null ? 0L : current.revision()) + 1L;
        BundleCompiler.BundleDefinition publication =
                copy(draft, next, true, BundleCompiler.DefinitionState.PUBLISHED);
        Map<String, BundleCompiler.BundleDefinition> candidates = enabledPublications(published);
        candidates.put(normalizedId, publication);
        ActionResult<Map<String, BundleCompiler.CompiledBundle>> validation = compiler.compileAll(candidates);
        if (!validation.successful()) {
            return ActionResult.failure(validation.reason(), validation.detail());
        }
        if (current != null) {
            appendHistory(current);
        }
        appendHistory(publication);
        published.put(normalizedId, publication);
        drafts.remove(normalizedId);
        revision++;
        return ActionResult.success(publication);
    }

    public synchronized ActionResult<BundleCompiler.BundleDefinition> setEnabled(
            String id,
            long expectedRevision,
            boolean enabled
    ) {
        writable();
        String normalizedId = normalizeId(id);
        BundleCompiler.BundleDefinition current = published.get(normalizedId);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "published bundle not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "published bundle revision changed");
        }
        BundleCompiler.BundleDefinition replacement = copy(
                current,
                Math.addExact(current.revision(), 1L),
                enabled,
                BundleCompiler.DefinitionState.PUBLISHED);
        Map<String, BundleCompiler.BundleDefinition> candidates = enabledPublications(published);
        if (enabled) {
            candidates.put(normalizedId, replacement);
        } else {
            candidates.remove(normalizedId);
        }
        ActionResult<Map<String, BundleCompiler.CompiledBundle>> validation = compiler.compileAll(candidates);
        if (!validation.successful()) {
            return ActionResult.failure(validation.reason(), validation.detail());
        }
        appendHistory(replacement);
        published.put(normalizedId, replacement);
        revision++;
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<BundleCompiler.BundleDefinition> rollback(
            String id,
            long expectedRevision,
            long historicalRevision
    ) {
        writable();
        String normalizedId = normalizeId(id);
        BundleCompiler.BundleDefinition current = published.get(normalizedId);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "published bundle not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "published bundle revision changed");
        }
        BundleCompiler.BundleDefinition historical = history.getOrDefault(normalizedId, List.of()).stream()
                .filter(definition -> definition.revision() == historicalRevision)
                .findFirst()
                .orElse(null);
        if (historical == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "bundle history revision not found");
        }
        BundleCompiler.BundleDefinition replacement = copy(
                historical,
                Math.addExact(current.revision(), 1L),
                historical.enabled(),
                BundleCompiler.DefinitionState.PUBLISHED);
        Map<String, BundleCompiler.BundleDefinition> candidates = enabledPublications(published);
        if (replacement.enabled()) {
            candidates.put(normalizedId, replacement);
        } else {
            candidates.remove(normalizedId);
        }
        ActionResult<Map<String, BundleCompiler.CompiledBundle>> validation = compiler.compileAll(candidates);
        if (!validation.successful()) {
            return ActionResult.failure(validation.reason(), validation.detail());
        }
        appendHistory(replacement);
        published.put(normalizedId, replacement);
        revision++;
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<Void> delete(String id, long expectedRevision) {
        writable();
        String normalizedId = normalizeId(id);
        BundleCompiler.BundleDefinition draft = drafts.get(normalizedId);
        if (draft != null) {
            if (draft.revision() != expectedRevision) {
                return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "bundle draft revision changed");
            }
            drafts.remove(normalizedId);
            revision++;
            return ActionResult.success(null);
        }
        BundleCompiler.BundleDefinition current = published.get(normalizedId);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "bundle not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "published bundle revision changed");
        }
        if (current.enabled()) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "disable the bundle before deletion");
        }
        boolean referenced = published.values().stream()
                .filter(BundleCompiler.BundleDefinition::enabled)
                .flatMap(definition -> definition.steps().stream())
                .anyMatch(step -> step.kind() == BundleCompiler.StepKind.BUNDLE
                        && step.targetId().equals(normalizedId));
        if (referenced) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "bundle is referenced");
        }
        published.remove(normalizedId);
        history.remove(normalizedId);
        revision++;
        return ActionResult.success(null);
    }

    public synchronized Preview preview(String id, int targetCount) {
        String normalizedId = normalizeId(id);
        BundleCompiler.BundleDefinition definition =
                Optional.ofNullable(drafts.get(normalizedId)).orElse(published.get(normalizedId));
        if (definition == null) {
            return new Preview(normalizedId, -1L, false, 0L, List.of(), List.of("bundle not found"));
        }
        Map<String, BundleCompiler.BundleDefinition> candidates = enabledPublications(published);
        candidates.put(normalizedId, copy(
                definition,
                definition.revision(),
                true,
                BundleCompiler.DefinitionState.PUBLISHED));
        ActionResult<Map<String, BundleCompiler.CompiledBundle>> result = compiler.compileAll(candidates);
        if (!result.successful()) {
            return new Preview(
                    normalizedId,
                    definition.revision(),
                    false,
                    0L,
                    definition.steps(),
                    List.of(result.detail()));
        }
        long expansion = result.value().get(normalizedId).maximumTargetSteps();
        if (targetCount < 1 || targetCount > definition.maximumTargets()) {
            return new Preview(
                    normalizedId,
                    definition.revision(),
                    false,
                    expansion,
                    definition.steps(),
                    List.of("target count exceeds bundle policy"));
        }
        return new Preview(
                normalizedId,
                definition.revision(),
                true,
                expansion,
                definition.steps(),
                List.of());
    }

    public synchronized ActionResult<RuntimeJob> enqueue(
            String id,
            long expectedRevision,
            UUID issuerId,
            List<UUID> targets,
            Instant now
    ) {
        writable();
        if (jobs.values().stream().filter(job -> !job.terminal()).count() >= MAXIMUM_JOBS) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "bundle job queue is full");
        }
        BundleCompiler.BundleDefinition definition = published.get(normalizeId(id));
        if (definition == null || !definition.enabled()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "enabled bundle not found");
        }
        if (definition.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "bundle revision changed");
        }
        LinkedHashSet<UUID> frozen = new LinkedHashSet<>(Objects.requireNonNull(targets, "targets"));
        if (frozen.contains(null) || frozen.isEmpty() || frozen.size() > definition.maximumTargets()) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "bundle target cohort is invalid");
        }
        List<BundleCompiler.BundleStep> plan;
        try {
            plan = expandedSteps(definition.id(), new ArrayDeque<>(), 0);
        } catch (IllegalStateException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.RECURSION_DENIED, exception.getMessage());
        }
        Instant created = Objects.requireNonNull(now, "now");
        RuntimeJob job = new RuntimeJob(
                UUID.randomUUID(),
                definition.id(),
                definition.revision(),
                Objects.requireNonNull(issuerId, "issuerId"),
                UUID.randomUUID(),
                BundleCompiler.JobState.QUEUED,
                0,
                0,
                0,
                created,
                created.plus(definition.maximumDuration()),
                created,
                List.copyOf(frozen),
                plan,
                List.of(),
                "");
        jobs.put(job.jobId(), job);
        revision++;
        return ActionResult.success(job);
    }

    public synchronized TickResult tick(
            Instant now,
            Revalidator revalidator,
            StepExecutor executor
    ) {
        Objects.requireNonNull(now, "now");
        Objects.requireNonNull(revalidator, "revalidator");
        Objects.requireNonNull(executor, "executor");
        int actions = 0;
        int completed = 0;
        int failed = 0;
        for (RuntimeJob original : new ArrayList<>(jobs.values())) {
            if (original.terminal() || original.state() == BundleCompiler.JobState.RECOVERING) {
                continue;
            }
            BundleCompiler.BundleDefinition definition = published.get(original.bundleId());
            if (definition == null || !definition.enabled() || definition.revision() != original.bundleRevision()) {
                replaceJob(original.fail("bundle definition changed", now));
                failed++;
                continue;
            }
            if (!now.isBefore(original.deadline())) {
                replaceJob(original.expire(now));
                failed++;
                continue;
            }
            if (now.isBefore(original.nextRunAt())) {
                continue;
            }
            RuntimeJob job = original.withState(BundleCompiler.JobState.RUNNING);
            int perTick = definition.actionsPerTick();
            int jobActions = 0;
            while (jobActions < perTick && job.nextStep() < job.plan().size()) {
                BundleCompiler.BundleStep step = job.plan().get(job.nextStep());
                if (step.kind() == BundleCompiler.StepKind.DELAY) {
                    job = job.advance(now.plus(step.delay()), null, true);
                    break;
                }
                List<UUID> stepTargets = targets(step, job);
                ActionResult<Void> validation = revalidator.validate(job, definition, step, stepTargets);
                if (!validation.successful()) {
                    job = handleFailure(job, definition, step, validation, executor, now);
                    failed++;
                    break;
                }
                boolean stepSuccessful = true;
                RuntimeJob beforeTargets = job;
                List<UUID> pendingTargets = stepTargets.stream()
                        .filter(target -> beforeTargets.executed().stream().noneMatch(executed ->
                                executed.step().equals(step) && executed.targetId().equals(target)))
                        .toList();
                for (UUID target : pendingTargets) {
                    ActionResult<Void> result = executor.execute(job, definition, step, target);
                    actions++;
                    jobActions++;
                    if (!result.successful()) {
                        job = job.recordFailure(step, target, result.detail());
                        stepSuccessful = false;
                        if (step.failureBehavior() == BundleCompiler.FailureBehavior.STOP
                                || definition.executionMode() == BundleCompiler.ExecutionMode.STOP_ON_FAILURE
                                || definition.executionMode()
                                == BundleCompiler.ExecutionMode.COMPENSATE_ON_FAILURE) {
                            job = handleFailure(job, definition, step, result, executor, now);
                            failed++;
                            break;
                        }
                    } else {
                        job = job.recordSuccess(step, target);
                    }
                    if (jobActions >= perTick) {
                        break;
                    }
                }
                if (job.terminal()) {
                    break;
                }
                RuntimeJob afterTargets = job;
                boolean allTargetsCompleted = stepTargets.stream().allMatch(target ->
                        afterTargets.executed().stream().anyMatch(executed ->
                                executed.step().equals(step) && executed.targetId().equals(target)));
                if ((stepSuccessful && allTargetsCompleted)
                        || step.failureBehavior() == BundleCompiler.FailureBehavior.CONTINUE) {
                    job = job.advance(now, null, true);
                }
            }
            if (!job.terminal() && job.nextStep() >= job.plan().size()) {
                job = job.complete(now);
                completed++;
            }
            replaceJob(job);
        }
        return new TickResult(actions, completed, failed);
    }

    private void seedBuiltinTemplates() {
        for (String template : List.of(
                "sef:staff_mode",
                "sef:moderation_handoff",
                "sef:maintenance",
                "sef:incident",
                "sef:recovery")) {
            BundleCompiler.BundleDefinition definition = new BundleCompiler.BundleDefinition(
                    SCHEMA_VERSION,
                    template,
                    1L,
                    BundleCompiler.DefinitionState.PUBLISHED,
                    false,
                    "",
                    Set.of(CommandDefinition.SourceType.PLAYER),
                    BundleCompiler.AuthorizationMode.STRICT_ACTOR,
                    BundleCompiler.ExecutionMode.STOP_ON_FAILURE,
                    1,
                    1,
                    Duration.ofMinutes(5),
                    true,
                    AuditService.AuditClass.WORKFLOW_EXECUTION,
                    List.of(new BundleCompiler.BundleStep(
                            "notice",
                            BundleCompiler.StepKind.NOTICE,
                            "template_disabled",
                            BundleCompiler.TargetBinding.ACTOR,
                            BundleCompiler.FailureBehavior.STOP,
                            Duration.ZERO,
                            Map.of("message", template.substring("sef:".length()).replace('_', ' ') + " template"))));
            published.put(template, definition);
            appendHistory(definition);
        }
        revision++;
    }

    public synchronized ActionResult<RuntimeJob> cancel(UUID jobId, UUID requesterId, boolean override) {
        writable();
        RuntimeJob current = jobs.get(Objects.requireNonNull(jobId, "jobId"));
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "bundle job not found");
        }
        if (!override && !current.issuerId().equals(requesterId)) {
            return ActionResult.failure(ActionResult.ReasonCode.PERMISSION_DENIED, "bundle job belongs to another issuer");
        }
        if (current.terminal()) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "bundle job is already terminal");
        }
        RuntimeJob replacement = current.withTerminal(BundleCompiler.JobState.CANCELLED, "cancelled");
        replaceJob(replacement);
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<RuntimeJob> recover(UUID jobId) {
        writable();
        RuntimeJob current = jobs.get(Objects.requireNonNull(jobId, "jobId"));
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "bundle job not found");
        }
        if (current.state() != BundleCompiler.JobState.RECOVERING) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "bundle job does not require recovery");
        }
        BundleCompiler.BundleDefinition definition = published.get(current.bundleId());
        if (definition == null || !definition.enabled() || definition.revision() != current.bundleRevision()) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "bundle definition cannot resume this job");
        }
        RuntimeJob replacement = current.withState(BundleCompiler.JobState.QUEUED);
        replaceJob(replacement);
        return ActionResult.success(replacement);
    }

    public synchronized Optional<BundleCompiler.BundleDefinition> find(String id) {
        return Optional.ofNullable(published.get(normalizeId(id)));
    }

    public synchronized Set<String> publishedIds() {
        return Set.copyOf(published.keySet());
    }

    public synchronized List<BundleCompiler.BundleDefinition> publications() {
        return published.values().stream()
                .sorted(Comparator.comparing(BundleCompiler.BundleDefinition::id))
                .toList();
    }

    public synchronized List<BundleCompiler.BundleDefinition> drafts() {
        return drafts.values().stream()
                .sorted(Comparator.comparing(BundleCompiler.BundleDefinition::id))
                .toList();
    }

    public synchronized List<RuntimeJob> jobs() {
        return jobs.values().stream()
                .sorted(Comparator.comparing(RuntimeJob::createdAt))
                .toList();
    }

    public synchronized List<BundleCompiler.BundleDefinition> history(String id) {
        return history.getOrDefault(normalizeId(id), List.of());
    }

    @Override
    public void flush() throws IOException {
        Snapshot snapshot;
        Path destination;
        StorageService.Document previous;
        long snapshotRevision;
        synchronized (this) {
            if (path == null || !dirty()) {
                return;
            }
            writable();
            snapshot = new Snapshot(
                    new ArrayList<>(drafts.values()),
                    new ArrayList<>(published.values()),
                    history.entrySet().stream()
                            .map(entry -> new HistoryRecord(entry.getKey(), entry.getValue()))
                            .toList(),
                    jobs.values().stream().filter(job -> !job.prunable()).toList());
            destination = path;
            previous = document;
            snapshotRevision = revision;
        }
        StorageService.write(
                destination,
                domain(),
                SCHEMA_VERSION,
                GSON.toJsonTree(snapshot),
                previous,
                Set.of("/drafts", "/published", "/history", "/jobs"));
        synchronized (this) {
            document = StorageService.read(destination, domain(), SCHEMA_VERSION).orElse(previous);
            flushedRevision = Math.max(flushedRevision, snapshotRevision);
            state = RepositoryState.READY;
        }
    }

    @Override
    public synchronized boolean dirty() {
        return revision > flushedRevision;
    }

    @Override
    public synchronized RepositoryState state() {
        return state;
    }

    private RuntimeJob handleFailure(
            RuntimeJob job,
            BundleCompiler.BundleDefinition definition,
            BundleCompiler.BundleStep failedStep,
            ActionResult<?> failure,
            StepExecutor executor,
            Instant now
    ) {
        if (definition.executionMode() == BundleCompiler.ExecutionMode.COMPENSATE_ON_FAILURE
                || failedStep.failureBehavior() == BundleCompiler.FailureBehavior.COMPENSATE) {
            List<ExecutedStep> executed = new ArrayList<>(job.executed());
            java.util.Collections.reverse(executed);
            for (ExecutedStep completed : executed) {
                ActionResult<Void> compensation = executor.compensate(
                        job,
                        definition,
                        completed.step(),
                        completed.targetId());
                if (!compensation.successful()) {
                    return job.withTerminal(
                            BundleCompiler.JobState.FAILED,
                            "compensation failed. " + compensation.detail());
                }
            }
        }
        return job.withTerminal(BundleCompiler.JobState.FAILED, failure.detail());
    }

    private List<BundleCompiler.BundleStep> expandedSteps(String id, Deque<String> path, int depth) {
        if (depth > 8 || path.contains(id)) {
            throw new IllegalStateException("bundle expansion is recursive");
        }
        BundleCompiler.BundleDefinition definition = published.get(id);
        if (definition == null || !definition.enabled()) {
            throw new IllegalStateException("nested bundle is unavailable");
        }
        path.addLast(id);
        List<BundleCompiler.BundleStep> expanded = new ArrayList<>();
        for (BundleCompiler.BundleStep step : definition.steps()) {
            if (step.kind() == BundleCompiler.StepKind.BUNDLE) {
                expanded.addAll(expandedSteps(step.targetId(), path, depth + 1));
            } else {
                expanded.add(step);
            }
            if (expanded.size() > 256) {
                throw new IllegalStateException("bundle expansion exceeds the step limit");
            }
        }
        path.removeLast();
        return List.copyOf(expanded);
    }

    private static List<UUID> targets(BundleCompiler.BundleStep step, RuntimeJob job) {
        return switch (step.targetBinding()) {
            case ACTOR -> List.of(job.issuerId());
            case SELECTED_PLAYER, EXPLICIT_VISIBLE_IDENTITY, APPROVED_AUDIENCE, FIXED_IDENTITY ->
                    job.targets();
            case PREVIOUS_STEP_RESULT ->
                    job.executed().isEmpty()
                            ? List.of()
                            : List.of(job.executed().getLast().targetId());
            case NO_PLAYER -> List.of(job.issuerId());
        };
    }

    private void replaceJob(RuntimeJob job) {
        jobs.put(job.jobId(), job);
        revision++;
    }

    private void appendHistory(BundleCompiler.BundleDefinition definition) {
        List<BundleCompiler.BundleDefinition> revisions =
                new ArrayList<>(history.getOrDefault(definition.id(), List.of()));
        revisions.removeIf(existing -> existing.revision() == definition.revision());
        revisions.add(definition);
        revisions.sort(Comparator.comparingLong(BundleCompiler.BundleDefinition::revision));
        while (revisions.size() > MAXIMUM_HISTORY) {
            revisions.removeFirst();
        }
        history.put(definition.id(), List.copyOf(revisions));
    }

    private void writable() {
        if (state == RepositoryState.RECOVERY
                || state == RepositoryState.UNSUPPORTED
                || state == RepositoryState.ERROR
                || state == RepositoryState.CLOSED) {
            throw new IllegalStateException("Bundle storage is unavailable");
        }
    }

    private static Map<String, BundleCompiler.BundleDefinition> enabledPublications(
            Map<String, BundleCompiler.BundleDefinition> source
    ) {
        Map<String, BundleCompiler.BundleDefinition> enabled = new LinkedHashMap<>();
        source.forEach((id, definition) -> {
            if (definition.enabled()) {
                enabled.put(id, definition);
            }
        });
        return enabled;
    }

    private static void putUnique(
            Map<String, BundleCompiler.BundleDefinition> destination,
            BundleCompiler.BundleDefinition definition
    ) {
        if (destination.putIfAbsent(definition.id(), definition) != null) {
            throw new IllegalStateException("Duplicate bundle id");
        }
    }

    private static void requireState(
            BundleCompiler.BundleDefinition definition,
            BundleCompiler.DefinitionState expected
    ) {
        Objects.requireNonNull(definition, "definition");
        if (definition.state() != expected || !ID.matcher(definition.id()).matches()) {
            throw new IllegalStateException("Bundle state is invalid");
        }
    }

    private static BundleCompiler.BundleDefinition copy(
            BundleCompiler.BundleDefinition source,
            long revision,
            boolean enabled,
            BundleCompiler.DefinitionState state
    ) {
        return new BundleCompiler.BundleDefinition(
                SCHEMA_VERSION,
                source.id(),
                revision,
                state,
                enabled,
                source.additionalPermissionId(),
                source.sourceTypes(),
                source.authorizationMode(),
                source.executionMode(),
                source.maximumTargets(),
                source.actionsPerTick(),
                source.maximumDuration(),
                source.confirmationRequired(),
                source.auditClass(),
                source.steps());
    }

    private static BundleCompiler.BundleDefinition withSteps(
            BundleCompiler.BundleDefinition source,
            List<BundleCompiler.BundleStep> steps
    ) {
        return new BundleCompiler.BundleDefinition(
                SCHEMA_VERSION,
                source.id(),
                source.revision(),
                source.state(),
                source.enabled(),
                source.additionalPermissionId(),
                source.sourceTypes(),
                source.authorizationMode(),
                source.executionMode(),
                source.maximumTargets(),
                source.actionsPerTick(),
                source.maximumDuration(),
                source.confirmationRequired(),
                source.auditClass(),
                steps);
    }

    private static String normalizeId(String value) {
        String normalized = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (!ID.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid bundle id");
        }
        return normalized;
    }

    private static String normalizePart(String value) {
        String normalized = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9_.-]{1,64}")) {
            throw new IllegalArgumentException("Invalid bundle part");
        }
        return normalized;
    }

    public record Preview(
            String bundleId,
            long revision,
            boolean valid,
            long maximumTargetSteps,
            List<BundleCompiler.BundleStep> steps,
            List<String> problems
    ) {
        public Preview {
            steps = List.copyOf(steps);
            problems = List.copyOf(problems);
        }
    }

    public record RuntimeJob(
            UUID jobId,
            String bundleId,
            long bundleRevision,
            UUID issuerId,
            UUID correlationId,
            BundleCompiler.JobState state,
            int nextStep,
            int completedActions,
            int failedActions,
            Instant createdAt,
            Instant deadline,
            Instant nextRunAt,
            List<UUID> targets,
            List<BundleCompiler.BundleStep> plan,
            List<ExecutedStep> executed,
            String detail
    ) {
        public RuntimeJob {
            Objects.requireNonNull(jobId, "jobId");
            bundleId = normalizeId(bundleId);
            Objects.requireNonNull(issuerId, "issuerId");
            Objects.requireNonNull(correlationId, "correlationId");
            Objects.requireNonNull(state, "state");
            Objects.requireNonNull(createdAt, "createdAt");
            Objects.requireNonNull(deadline, "deadline");
            Objects.requireNonNull(nextRunAt, "nextRunAt");
            targets = List.copyOf(targets);
            plan = List.copyOf(plan);
            executed = List.copyOf(executed);
            detail = Objects.requireNonNullElse(detail, "");
            if (bundleRevision < 1L || nextStep < 0 || nextStep > plan.size()
                    || completedActions < 0 || failedActions < 0
                    || targets.isEmpty() || targets.size() > 1000
                    || plan.isEmpty() || plan.size() > 256
                    || !deadline.isAfter(createdAt)) {
                throw new IllegalArgumentException("Bundle runtime job is invalid");
            }
        }

        RuntimeJob recover() {
            if (state == BundleCompiler.JobState.RUNNING
                    || state == BundleCompiler.JobState.QUEUED
                    || state == BundleCompiler.JobState.PAUSED) {
                return withState(BundleCompiler.JobState.RECOVERING);
            }
            return this;
        }

        RuntimeJob withState(BundleCompiler.JobState replacement) {
            return new RuntimeJob(jobId, bundleId, bundleRevision, issuerId, correlationId,
                    replacement, nextStep, completedActions, failedActions, createdAt, deadline,
                    nextRunAt, targets, plan, executed, detail);
        }

        RuntimeJob advance(Instant nextRun, String replacementDetail, boolean advance) {
            return new RuntimeJob(jobId, bundleId, bundleRevision, issuerId, correlationId,
                    BundleCompiler.JobState.RUNNING, nextStep + (advance ? 1 : 0),
                    completedActions, failedActions, createdAt, deadline, nextRun,
                    targets, plan, executed, replacementDetail == null ? detail : replacementDetail);
        }

        RuntimeJob recordSuccess(BundleCompiler.BundleStep step, UUID targetId) {
            List<ExecutedStep> replacement = new ArrayList<>(executed);
            replacement.add(new ExecutedStep(step, targetId));
            return new RuntimeJob(jobId, bundleId, bundleRevision, issuerId, correlationId,
                    state, nextStep, completedActions + 1, failedActions, createdAt, deadline,
                    nextRunAt, targets, plan, replacement, detail);
        }

        RuntimeJob recordFailure(BundleCompiler.BundleStep step, UUID targetId, String failure) {
            return new RuntimeJob(jobId, bundleId, bundleRevision, issuerId, correlationId,
                    state, nextStep, completedActions, failedActions + 1, createdAt, deadline,
                    nextRunAt, targets, plan, executed,
                    step.id() + " for " + targetId + ". " + Objects.requireNonNullElse(failure, "failed"));
        }

        RuntimeJob complete(Instant now) {
            return withTerminal(
                    failedActions == 0
                            ? BundleCompiler.JobState.COMPLETED
                            : BundleCompiler.JobState.COMPLETED_WITH_FAILURES,
                    "completed at " + now);
        }

        RuntimeJob expire(Instant now) {
            return withTerminal(BundleCompiler.JobState.EXPIRED, "expired at " + now);
        }

        RuntimeJob fail(String failure, Instant now) {
            return withTerminal(BundleCompiler.JobState.FAILED, failure + " at " + now);
        }

        RuntimeJob withTerminal(BundleCompiler.JobState terminalState, String terminalDetail) {
            return new RuntimeJob(jobId, bundleId, bundleRevision, issuerId, correlationId,
                    terminalState, nextStep, completedActions, failedActions, createdAt, deadline,
                    nextRunAt, targets, plan, executed, terminalDetail);
        }

        boolean terminal() {
            return switch (state) {
                case COMPLETED, COMPLETED_WITH_FAILURES, CANCELLED, EXPIRED, FAILED -> true;
                default -> false;
            };
        }

        boolean prunable() {
            return terminal() && deadline.isBefore(Instant.now().minus(Duration.ofHours(24)));
        }
    }

    public record ExecutedStep(BundleCompiler.BundleStep step, UUID targetId) {
        public ExecutedStep {
            Objects.requireNonNull(step, "step");
            Objects.requireNonNull(targetId, "targetId");
        }
    }

    @FunctionalInterface
    public interface Revalidator {
        ActionResult<Void> validate(
                RuntimeJob job,
                BundleCompiler.BundleDefinition definition,
                BundleCompiler.BundleStep step,
                List<UUID> targets);
    }

    @FunctionalInterface
    public interface StepExecutor {
        ActionResult<Void> execute(
                RuntimeJob job,
                BundleCompiler.BundleDefinition definition,
                BundleCompiler.BundleStep step,
                UUID targetId);

        default ActionResult<Void> compensate(
                RuntimeJob job,
                BundleCompiler.BundleDefinition definition,
                BundleCompiler.BundleStep step,
                UUID targetId
        ) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "step has no compensation adapter");
        }
    }

    public record TickResult(int actions, int completedJobs, int failedJobs) {
    }

    private record Snapshot(
            List<BundleCompiler.BundleDefinition> drafts,
            List<BundleCompiler.BundleDefinition> published,
            List<HistoryRecord> history,
            List<RuntimeJob> jobs
    ) {
        private Snapshot {
            drafts = List.copyOf(drafts == null ? List.of() : drafts);
            published = List.copyOf(published == null ? List.of() : published);
            history = List.copyOf(history == null ? List.of() : history);
            jobs = List.copyOf(jobs == null ? List.of() : jobs);
        }
    }

    private record HistoryRecord(String id, List<BundleCompiler.BundleDefinition> revisions) {
        private HistoryRecord {
            revisions = List.copyOf(revisions == null ? List.of() : revisions);
        }
    }

    private static final class DurationJsonAdapter
            implements com.google.gson.JsonSerializer<Duration>, com.google.gson.JsonDeserializer<Duration> {
        @Override
        public com.google.gson.JsonElement serialize(
                Duration source,
                java.lang.reflect.Type type,
                com.google.gson.JsonSerializationContext context
        ) {
            return new com.google.gson.JsonPrimitive(source.toString());
        }

        @Override
        public Duration deserialize(
                com.google.gson.JsonElement source,
                java.lang.reflect.Type type,
                com.google.gson.JsonDeserializationContext context
        ) {
            try {
                return Duration.parse(source.getAsString());
            } catch (RuntimeException exception) {
                throw new com.google.gson.JsonParseException("Invalid duration", exception);
            }
        }
    }
}
