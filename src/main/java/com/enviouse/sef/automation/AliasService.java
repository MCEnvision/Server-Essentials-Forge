package com.enviouse.sef.automation;

import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.command.AliasCompiler;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.storage.InstantJsonAdapter;
import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public final class AliasService implements StorageRepository {
    public static final int SCHEMA_VERSION = 1;
    private static final int MAXIMUM_HISTORY = 32;
    private static final Pattern ID = Pattern.compile("[a-z][a-z0-9_.-]*:[a-z0-9_./-]+");
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantJsonAdapter())
            .create();

    private final AliasCompiler compiler;
    private final int maximumDefinitions;
    private final Map<String, AliasCompiler.AliasDefinition> drafts = new LinkedHashMap<>();
    private final Map<String, AliasCompiler.AliasDefinition> published = new LinkedHashMap<>();
    private final Map<String, List<AliasCompiler.AliasDefinition>> history = new LinkedHashMap<>();
    private StorageService.Document document;
    private RepositoryState state = RepositoryState.NEW;
    private Path path;
    private long revision;
    private long flushedRevision;

    public AliasService(AliasCompiler compiler, int maximumDefinitions) {
        this.compiler = Objects.requireNonNull(compiler, "compiler");
        if (maximumDefinitions < 1 || maximumDefinitions > 1024) {
            throw new IllegalArgumentException("Alias definition limit is outside hard bounds");
        }
        this.maximumDefinitions = maximumDefinitions;
    }

    @Override
    public String id() {
        return "sef:aliases";
    }

    @Override
    public String domain() {
        return "aliases";
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
                .resolve("aliases.json")
                .toAbsolutePath()
                .normalize();
        drafts.clear();
        published.clear();
        history.clear();
        revision = 0L;
        flushedRevision = 0L;
        boolean existed = Files.exists(path);
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(null);
        if (document == null) {
            state = existed ? RepositoryState.RECOVERY : RepositoryState.MISSING;
            return new LoadResult(state, existed ? "storage unavailable" : "new repository");
        }
        try {
            Snapshot snapshot = GSON.fromJson(document.data(), Snapshot.class);
            if (snapshot == null
                    || snapshot.drafts().size() + snapshot.published().size() > maximumDefinitions
                    || snapshot.history().size() > maximumDefinitions) {
                throw new IllegalStateException("Alias collections are outside bounds");
            }
            for (AliasCompiler.AliasDefinition definition : snapshot.drafts()) {
                requireState(definition, AliasCompiler.DefinitionState.DRAFT);
                putUnique(drafts, definition);
            }
            for (AliasCompiler.AliasDefinition definition : snapshot.published()) {
                requireState(definition, AliasCompiler.DefinitionState.PUBLISHED);
                ActionResult<AliasCompiler.CompiledAlias> validation =
                        compiler.validatePublication(definition);
                if (!validation.successful()) {
                    throw new IllegalStateException(validation.detail());
                }
                if (published.values().stream().anyMatch(existing ->
                        existing.root().equals(definition.root())
                                && !existing.id().equals(definition.id()))) {
                    throw new IllegalStateException("Duplicate published alias root");
                }
                putUnique(published, definition);
            }
            for (HistoryRecord record : snapshot.history()) {
                String id = normalizeId(record.id());
                if (record.revisions().size() > MAXIMUM_HISTORY) {
                    throw new IllegalStateException("Alias history limit exceeded");
                }
                List<AliasCompiler.AliasDefinition> revisions = new ArrayList<>();
                for (AliasCompiler.AliasDefinition definition : record.revisions()) {
                    requireState(definition, AliasCompiler.DefinitionState.PUBLISHED);
                    if (!definition.id().equals(id)) {
                        throw new IllegalStateException("Alias history identity mismatch");
                    }
                    revisions.add(definition);
                }
                history.put(id, List.copyOf(revisions));
            }
            state = RepositoryState.READY;
            if (document.migrated()) {
                revision++;
            }
            return new LoadResult(state, "loaded " + published.size() + " published aliases");
        } catch (RuntimeException exception) {
            drafts.clear();
            published.clear();
            history.clear();
            state = RepositoryState.RECOVERY;
            return new LoadResult(state, exception.getClass().getSimpleName());
        }
    }

    public synchronized ActionResult<AliasCompiler.AliasDefinition> createDraft(
            String id,
            String root,
            AliasCompiler.AliasKind kind,
            String targetId,
            AliasCompiler.ArgumentSchema schema,
            UUID actorId
    ) {
        return createDraft(
                id,
                root,
                kind,
                targetId,
                schema,
                "",
                CommandDefinition.AccessClass.OWNER,
                com.enviouse.sef.audit.AuditService.AuditClass.ECONOMY_TRANSACTION,
                actorId);
    }

    public synchronized ActionResult<AliasCompiler.AliasDefinition> createDraft(
            String id,
            String root,
            AliasCompiler.AliasKind kind,
            String targetId,
            AliasCompiler.ArgumentSchema schema,
            String additionalPermissionId,
            CommandDefinition.AccessClass accessClass,
            com.enviouse.sef.audit.AuditService.AuditClass auditClass,
            UUID actorId
    ) {
        writable();
        String normalizedId = normalizeId(id);
        if (drafts.containsKey(normalizedId) || published.containsKey(normalizedId)) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "alias id already exists");
        }
        if (drafts.size() + published.size() >= maximumDefinitions) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "alias definition limit reached");
        }
        AliasCompiler.AliasDefinition definition;
        try {
            definition = new AliasCompiler.AliasDefinition(
                    SCHEMA_VERSION,
                    normalizedId,
                    1L,
                    true,
                    AliasCompiler.DefinitionState.DRAFT,
                    root,
                    kind,
                    targetId,
                    schema,
                    Map.of(),
                    additionalPermissionId,
                    Set.of(CommandDefinition.SourceType.PLAYER),
                    Objects.requireNonNull(accessClass, "accessClass"),
                    CommandDefinition.ConflictPolicy.FAIL,
                    Objects.requireNonNull(auditClass, "auditClass"),
                    Objects.requireNonNull(actorId, "actorId"),
                    Instant.now());
        } catch (IllegalArgumentException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, exception.getMessage());
        }
        drafts.put(normalizedId, definition);
        revision++;
        return ActionResult.success(definition);
    }

    public synchronized ActionResult<AliasCompiler.AliasDefinition> saveDraft(
            AliasCompiler.AliasDefinition requested,
            long expectedRevision,
            UUID actorId
    ) {
        writable();
        Objects.requireNonNull(requested, "requested");
        String id = normalizeId(requested.id());
        AliasCompiler.AliasDefinition current = drafts.get(id);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "alias draft not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "alias draft revision changed");
        }
        AliasCompiler.AliasDefinition replacement = copy(
                requested,
                Math.addExact(current.revision(), 1L),
                requested.enabled(),
                AliasCompiler.DefinitionState.DRAFT,
                Objects.requireNonNull(actorId, "actorId"),
                Instant.now());
        ActionResult<AliasCompiler.CompiledAlias> validation = compiler.compile(replacement);
        if (!validation.successful()) {
            return ActionResult.failure(validation.reason(), validation.detail());
        }
        drafts.put(id, replacement);
        revision++;
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<AliasCompiler.AliasDefinition> publish(
            String id,
            long expectedRevision,
            UUID actorId
    ) {
        writable();
        String normalizedId = normalizeId(id);
        AliasCompiler.AliasDefinition draft = drafts.get(normalizedId);
        if (draft == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "alias draft not found");
        }
        if (draft.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "alias draft revision changed");
        }
        AliasCompiler.AliasDefinition current = published.get(normalizedId);
        if (current != null && !current.root().equals(draft.root())) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "alias root changes require restart");
        }
        if (published.values().stream().anyMatch(existing ->
                existing.enabled()
                        && existing.root().equals(draft.root())
                        && !existing.id().equals(draft.id()))) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "alias root is already published");
        }
        long next = Math.max(draft.revision(), current == null ? 0L : current.revision()) + 1L;
        AliasCompiler.AliasDefinition publication = copy(
                draft,
                next,
                true,
                AliasCompiler.DefinitionState.PUBLISHED,
                Objects.requireNonNull(actorId, "actorId"),
                Instant.now());
        ActionResult<AliasCompiler.CompiledAlias> compiled = compiler.validatePublication(publication);
        if (!compiled.successful()) {
            return ActionResult.failure(compiled.reason(), compiled.detail());
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

    public synchronized ActionResult<AliasCompiler.CompiledAlias> validateDraft(
            String id,
            long expectedRevision
    ) {
        AliasCompiler.AliasDefinition draft = drafts.get(normalizeId(id));
        if (draft == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "alias draft not found");
        }
        if (draft.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "alias draft revision changed");
        }
        return compiler.compile(draft);
    }

    public synchronized ActionResult<AliasCompiler.AliasDefinition> setEnabled(
            String id,
            long expectedRevision,
            boolean enabled,
            UUID actorId
    ) {
        writable();
        String normalizedId = normalizeId(id);
        AliasCompiler.AliasDefinition current = published.get(normalizedId);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "published alias not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "published alias revision changed");
        }
        AliasCompiler.AliasDefinition replacement = copy(
                current,
                Math.addExact(current.revision(), 1L),
                enabled,
                AliasCompiler.DefinitionState.PUBLISHED,
                Objects.requireNonNull(actorId, "actorId"),
                Instant.now());
        if (enabled) {
            ActionResult<AliasCompiler.CompiledAlias> validation = compiler.validatePublication(replacement);
            if (!validation.successful()) {
                return ActionResult.failure(validation.reason(), validation.detail());
            }
        }
        appendHistory(replacement);
        published.put(normalizedId, replacement);
        revision++;
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<AliasCompiler.AliasDefinition> rollback(
            String id,
            long expectedRevision,
            long historicalRevision,
            UUID actorId
    ) {
        writable();
        String normalizedId = normalizeId(id);
        AliasCompiler.AliasDefinition current = published.get(normalizedId);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "published alias not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "published alias revision changed");
        }
        AliasCompiler.AliasDefinition historical = history.getOrDefault(normalizedId, List.of()).stream()
                .filter(definition -> definition.revision() == historicalRevision)
                .findFirst()
                .orElse(null);
        if (historical == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "alias history revision not found");
        }
        AliasCompiler.AliasDefinition replacement = copy(
                historical,
                Math.addExact(current.revision(), 1L),
                historical.enabled(),
                AliasCompiler.DefinitionState.PUBLISHED,
                Objects.requireNonNull(actorId, "actorId"),
                Instant.now());
        ActionResult<AliasCompiler.CompiledAlias> validation = compiler.validatePublication(replacement);
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
        AliasCompiler.AliasDefinition draft = drafts.get(normalizedId);
        if (draft != null) {
            if (draft.revision() != expectedRevision) {
                return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "alias draft revision changed");
            }
            drafts.remove(normalizedId);
            revision++;
            return ActionResult.success(null);
        }
        AliasCompiler.AliasDefinition publication = published.get(normalizedId);
        if (publication == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "alias not found");
        }
        if (publication.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "published alias revision changed");
        }
        if (publication.enabled()) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "disable the alias before deletion");
        }
        published.remove(normalizedId);
        history.remove(normalizedId);
        revision++;
        return ActionResult.success(null);
    }

    public synchronized Optional<AliasCompiler.AliasDefinition> find(String id) {
        return Optional.ofNullable(published.get(normalizeId(id)));
    }

    public synchronized Optional<AliasCompiler.AliasDefinition> findRoot(String root) {
        String normalizedRoot = normalizeRoot(root);
        return published.values().stream()
                .filter(AliasCompiler.AliasDefinition::enabled)
                .filter(definition -> definition.root().equals(normalizedRoot))
                .findFirst();
    }

    public synchronized List<AliasCompiler.AliasDefinition> published() {
        return published.values().stream()
                .sorted(Comparator.comparing(AliasCompiler.AliasDefinition::id))
                .toList();
    }

    public synchronized List<AliasCompiler.AliasDefinition> drafts() {
        return drafts.values().stream()
                .sorted(Comparator.comparing(AliasCompiler.AliasDefinition::id))
                .toList();
    }

    public synchronized Set<String> publishedIds() {
        return Set.copyOf(published.keySet());
    }

    public synchronized List<AliasCompiler.AliasDefinition> history(String id) {
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
                            .toList());
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
                Set.of("/drafts", "/published", "/history"));
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

    private void appendHistory(AliasCompiler.AliasDefinition definition) {
        List<AliasCompiler.AliasDefinition> revisions =
                new ArrayList<>(history.getOrDefault(definition.id(), List.of()));
        revisions.removeIf(existing -> existing.revision() == definition.revision());
        revisions.add(definition);
        revisions.sort(Comparator.comparingLong(AliasCompiler.AliasDefinition::revision));
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
            throw new IllegalStateException("Alias storage is unavailable");
        }
    }

    private static void putUnique(
            Map<String, AliasCompiler.AliasDefinition> destination,
            AliasCompiler.AliasDefinition definition
    ) {
        if (destination.putIfAbsent(definition.id(), definition) != null) {
            throw new IllegalStateException("Duplicate alias id");
        }
    }

    private static void requireState(
            AliasCompiler.AliasDefinition definition,
            AliasCompiler.DefinitionState required
    ) {
        Objects.requireNonNull(definition, "definition");
        if (definition.state() != required || !ID.matcher(definition.id()).matches()) {
            throw new IllegalStateException("Alias state is invalid");
        }
    }

    private static AliasCompiler.AliasDefinition copy(
            AliasCompiler.AliasDefinition source,
            long revision,
            boolean enabled,
            AliasCompiler.DefinitionState state,
            UUID actorId,
            Instant now
    ) {
        return new AliasCompiler.AliasDefinition(
                SCHEMA_VERSION,
                source.id(),
                revision,
                enabled,
                state,
                source.root(),
                source.kind(),
                source.targetId(),
                source.argumentSchema(),
                source.fixedArguments(),
                source.additionalPermissionId(),
                source.sourceTypes(),
                source.accessClass(),
                source.conflictMode(),
                source.auditClass(),
                actorId,
                now);
    }

    private static String normalizeId(String value) {
        String normalized = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (!ID.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid alias id");
        }
        return normalized;
    }

    private static String normalizeRoot(String value) {
        String normalized = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9_:-]{1,64}")) {
            throw new IllegalArgumentException("Invalid alias root");
        }
        return normalized;
    }

    private record Snapshot(
            List<AliasCompiler.AliasDefinition> drafts,
            List<AliasCompiler.AliasDefinition> published,
            List<HistoryRecord> history
    ) {
        private Snapshot {
            drafts = List.copyOf(drafts == null ? List.of() : drafts);
            published = List.copyOf(published == null ? List.of() : published);
            history = List.copyOf(history == null ? List.of() : history);
        }
    }

    private record HistoryRecord(String id, List<AliasCompiler.AliasDefinition> revisions) {
        private HistoryRecord {
            revisions = List.copyOf(revisions == null ? List.of() : revisions);
        }
    }
}
