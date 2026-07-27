package com.enviouse.sef.gui;

import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.command.CapabilityManifest;
import com.enviouse.sef.kernel.command.CommandCatalog;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.kernel.command.PanelContracts;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public final class AdminPanelService implements StorageRepository {
    public static final int SCHEMA_VERSION = 1;
    private static final int MAXIMUM_PANELS = 256;
    private static final int MAXIMUM_HISTORY_PER_PANEL = 32;
    private static final Pattern ID = Pattern.compile("[a-z][a-z0-9_.-]{0,63}");
    private static final Gson GSON = new GsonBuilder().create();

    private final CommandCatalog catalog;
    private final CapabilityManifest capabilities;
    private final Map<String, PanelDefinition> builtIns = new LinkedHashMap<>();
    private final Map<String, PanelDefinition> drafts = new LinkedHashMap<>();
    private final Map<String, PanelDefinition> published = new LinkedHashMap<>();
    private final Map<String, List<PanelDefinition>> history = new LinkedHashMap<>();
    private final List<Consumer<PublicationEvent>> listeners = new CopyOnWriteArrayList<>();
    private Path path;
    private StorageService.Document document;
    private RepositoryState state = RepositoryState.NEW;
    private long repositoryRevision;
    private long flushedRevision;

    public AdminPanelService(CommandCatalog catalog, CapabilityManifest capabilities) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.capabilities = Objects.requireNonNull(capabilities, "capabilities");
        registerBuiltIns();
    }

    @Override
    public String id() {
        return "sef:admin_panels";
    }

    @Override
    public String domain() {
        return "admin panels";
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
        path = managedRoot.resolve("admin-panels.json").toAbsolutePath().normalize();
        drafts.clear();
        published.clear();
        history.clear();
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(null);
        if (document == null) {
            state = Files.exists(path) ? RepositoryState.RECOVERY : RepositoryState.MISSING;
            return new LoadResult(state, state == RepositoryState.MISSING
                    ? "new repository"
                    : "storage unavailable");
        }
        try {
            Snapshot snapshot = GSON.fromJson(document.data(), Snapshot.class);
            if (snapshot == null || snapshot.drafts().size() > MAXIMUM_PANELS
                    || snapshot.published().size() > MAXIMUM_PANELS
                    || snapshot.history().size() > MAXIMUM_PANELS) {
                throw new IllegalStateException("Panel repository collections are outside bounds");
            }
            for (PanelDefinition panel : snapshot.drafts()) {
                validateDefinition(panel, State.DRAFT);
                putUnique(drafts, panel);
            }
            for (PanelDefinition panel : snapshot.published()) {
                validateDefinition(panel, State.PUBLISHED);
                putUnique(published, panel);
            }
            for (HistoryRecord record : snapshot.history()) {
                String panelId = normalizeId(record.panelId());
                if (record.revisions().size() > MAXIMUM_HISTORY_PER_PANEL) {
                    throw new IllegalStateException("Panel history limit exceeded");
                }
                List<PanelDefinition> revisions = new ArrayList<>();
                for (PanelDefinition panel : record.revisions()) {
                    if (!panel.id().equals(panelId)) {
                        throw new IllegalStateException("Panel history identity mismatch");
                    }
                    validateDefinition(panel, State.PUBLISHED);
                    revisions.add(panel);
                }
                history.put(panelId, List.copyOf(revisions));
            }
            state = RepositoryState.READY;
            if (document.migrated()) {
                repositoryRevision++;
            }
            return new LoadResult(
                    state,
                    "loaded " + drafts.size() + " drafts and " + published.size() + " publications");
        } catch (RuntimeException exception) {
            drafts.clear();
            published.clear();
            history.clear();
            state = RepositoryState.RECOVERY;
            return new LoadResult(state, exception.getClass().getSimpleName());
        }
    }

    public synchronized ActionResult<PanelDefinition> createDraft(String id, String title, UUID actorId) {
        writable();
        String panelId = normalizeId(id);
        if (builtIns.containsKey(panelId) || drafts.containsKey(panelId) || published.containsKey(panelId)) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "panel id already exists");
        }
        if (knownPanelCount() >= MAXIMUM_PANELS) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "panel definition limit reached");
        }
        PanelDefinition definition = new PanelDefinition(
                SCHEMA_VERSION,
                panelId,
                1L,
                State.DRAFT,
                bounded(title, 128),
                "sef.kernel.panel.use",
                1,
                List.of(),
                Objects.requireNonNull(actorId, "actorId"),
                Instant.now().toString());
        drafts.put(panelId, definition);
        repositoryRevision++;
        return ActionResult.success(definition);
    }

    public synchronized ActionResult<PanelDefinition> saveDraft(
            PanelDefinition definition,
            long expectedRevision,
            UUID actorId
    ) {
        writable();
        Objects.requireNonNull(definition, "definition");
        String panelId = normalizeId(definition.id());
        if (builtIns.containsKey(panelId)) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "built in panels are immutable");
        }
        PanelDefinition current = drafts.get(panelId);
        long currentRevision = current == null
                ? published.getOrDefault(panelId, definition).revision()
                : current.revision();
        if (current == null && !published.containsKey(panelId) && knownPanelCount() >= MAXIMUM_PANELS) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "panel definition limit reached");
        }
        if (currentRevision != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "panel draft revision changed");
        }
        PanelDefinition replacement = new PanelDefinition(
                SCHEMA_VERSION,
                panelId,
                Math.addExact(currentRevision, 1L),
                State.DRAFT,
                definition.title(),
                definition.permissionId(),
                definition.rows(),
                definition.controls(),
                Objects.requireNonNull(actorId, "actorId"),
                Instant.now().toString());
        try {
            validateDefinition(replacement, State.DRAFT);
        } catch (IllegalArgumentException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, exception.getMessage());
        }
        drafts.put(panelId, replacement);
        repositoryRevision++;
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<PanelDefinition> addControl(
            String panelId,
            long expectedRevision,
            Control control,
            UUID actorId
    ) {
        PanelDefinition current = drafts.get(normalizeId(panelId));
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "panel draft not found");
        }
        List<Control> controls = new ArrayList<>(current.controls());
        if (controls.stream().anyMatch(existing -> existing.id().equals(control.id()))) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "panel control id already exists");
        }
        controls.add(control);
        return saveDraft(current.withControls(controls), expectedRevision, actorId);
    }

    public synchronized ActionResult<PanelDefinition> removeControl(
            String panelId,
            long expectedRevision,
            String controlId,
            UUID actorId
    ) {
        PanelDefinition current = drafts.get(normalizeId(panelId));
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "panel draft not found");
        }
        String normalizedControl = normalizeId(controlId);
        List<Control> controls = current.controls().stream()
                .filter(control -> !control.id().equals(normalizedControl))
                .toList();
        if (controls.size() == current.controls().size()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "panel control not found");
        }
        return saveDraft(current.withControls(controls), expectedRevision, actorId);
    }

    public synchronized ActionResult<PanelDefinition> publish(
            String panelId,
            long expectedDraftRevision,
            UUID actorId
    ) {
        writable();
        String id = normalizeId(panelId);
        PanelDefinition draft = drafts.get(id);
        if (draft == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "panel draft not found");
        }
        if (draft.revision() != expectedDraftRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "panel draft revision changed");
        }
        try {
            validateDefinition(draft, State.DRAFT);
        } catch (IllegalArgumentException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, exception.getMessage());
        }
        long nextRevision = Math.max(
                draft.revision(),
                published.getOrDefault(id, draft).revision()) + 1L;
        PanelDefinition publication = new PanelDefinition(
                SCHEMA_VERSION,
                id,
                nextRevision,
                State.PUBLISHED,
                draft.title(),
                draft.permissionId(),
                draft.rows(),
                draft.controls(),
                Objects.requireNonNull(actorId, "actorId"),
                Instant.now().toString());
        PanelDefinition previous = published.put(id, publication);
        if (previous != null) {
            appendHistory(id, previous);
        }
        appendHistory(id, publication);
        drafts.remove(id);
        repositoryRevision++;
        notifyListeners(new PublicationEvent(id, publication.revision(), PublicationKind.PUBLISH));
        return ActionResult.success(publication);
    }

    public synchronized ActionResult<PanelDefinition> rollback(
            String panelId,
            long expectedPublishedRevision,
            long historyRevision,
            UUID actorId
    ) {
        writable();
        String id = normalizeId(panelId);
        PanelDefinition current = published.get(id);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "published panel not found");
        }
        if (current.revision() != expectedPublishedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "published panel revision changed");
        }
        PanelDefinition historical = history.getOrDefault(id, List.of()).stream()
                .filter(revision -> revision.revision() == historyRevision)
                .findFirst()
                .orElse(null);
        if (historical == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "panel history revision not found");
        }
        PanelDefinition replacement = new PanelDefinition(
                SCHEMA_VERSION,
                id,
                Math.addExact(current.revision(), 1L),
                State.PUBLISHED,
                historical.title(),
                historical.permissionId(),
                historical.rows(),
                historical.controls(),
                Objects.requireNonNull(actorId, "actorId"),
                Instant.now().toString());
        appendHistory(id, replacement);
        published.put(id, replacement);
        repositoryRevision++;
        notifyListeners(new PublicationEvent(id, replacement.revision(), PublicationKind.ROLLBACK));
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<Void> deleteDraft(String panelId, long expectedRevision) {
        writable();
        String id = normalizeId(panelId);
        PanelDefinition current = drafts.get(id);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "panel draft not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "panel draft revision changed");
        }
        drafts.remove(id);
        repositoryRevision++;
        return ActionResult.success(null);
    }

    public synchronized Optional<PanelDefinition> panel(String panelId) {
        String id = normalizeId(panelId);
        PanelDefinition builtIn = builtIns.get(id);
        return Optional.ofNullable(builtIn == null ? published.get(id) : builtIn);
    }

    public synchronized Optional<PanelDefinition> draft(String panelId) {
        return Optional.ofNullable(drafts.get(normalizeId(panelId)));
    }

    public synchronized List<PanelDefinition> panels() {
        List<PanelDefinition> panels = new ArrayList<>(builtIns.values());
        panels.addAll(published.values());
        return panels.stream().sorted(Comparator.comparing(PanelDefinition::id)).toList();
    }

    public synchronized List<PanelDefinition> drafts() {
        return drafts.values().stream().sorted(Comparator.comparing(PanelDefinition::id)).toList();
    }

    public synchronized List<PanelDefinition> history(String panelId) {
        return history.getOrDefault(normalizeId(panelId), List.of());
    }

    public synchronized Preview preview(String panelId) {
        String id = normalizeId(panelId);
        PanelDefinition panel = drafts.get(id);
        if (panel == null) {
            panel = builtIns.get(id);
        }
        if (panel == null) {
            panel = published.get(id);
        }
        if (panel == null) {
            return new Preview(id, -1L, List.of(), List.of("panel not found"));
        }
        List<String> problems = new ArrayList<>();
        try {
            validateDefinition(panel, panel.state());
        } catch (IllegalArgumentException exception) {
            problems.add(exception.getMessage());
        }
        List<PreviewControl> controls = panel.controls().stream()
                .map(control -> new PreviewControl(
                        control.id(),
                        control.actionId(),
                        control.executionContext(),
                        control.audienceKind(),
                        control.maximumTargets(),
                        catalog.find(control.actionId())
                                .map(CommandDefinition::canonicalRoute)
                                .orElse("unavailable")))
                .toList();
        return new Preview(id, panel.revision(), controls, problems);
    }

    public synchronized Optional<Execution> execution(String panelId, String controlId) {
        PanelDefinition panel = panel(panelId).orElse(null);
        if (panel == null) {
            return Optional.empty();
        }
        String id = normalizeId(controlId);
        return panel.controls().stream()
                .filter(control -> control.id().equals(id))
                .findFirst()
                .flatMap(control -> catalog.find(control.actionId())
                        .map(action -> new Execution(panel, control, action)));
    }

    public void addPublicationListener(Consumer<PublicationEvent> listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    @Override
    public void flush() throws IOException {
        final Snapshot snapshot;
        final Path destination;
        final StorageService.Document previous;
        final long snapshotRevision;
        synchronized (this) {
            if (path == null || !dirty()) {
                return;
            }
            writable();
            snapshot = new Snapshot(
                    drafts(),
                    published.values().stream().sorted(Comparator.comparing(PanelDefinition::id)).toList(),
                    history.entrySet().stream()
                            .sorted(Map.Entry.comparingByKey())
                            .map(entry -> new HistoryRecord(entry.getKey(), entry.getValue()))
                            .toList());
            destination = path;
            previous = document;
            snapshotRevision = repositoryRevision;
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
        return repositoryRevision > flushedRevision;
    }

    @Override
    public synchronized RepositoryState state() {
        return state;
    }

    private void registerBuiltIns() {
        builtIns.put("staff", builtIn(
                "staff",
                "Staff overview",
                List.of(
                        builtInControl("doctor", 0, "sef:core.doctor"),
                        builtInControl("conflicts", 1, "sef:core.conflicts"),
                        builtInControl("logging", 2, "sef:logging.status"))));
        builtIns.put("player_control", builtIn(
                "player_control",
                "Player control",
                List.of(
                        builtInControl("gamemode", 0, "sef:gamemode.set"),
                        builtInControl("inventory", 1, "sef:inventory.enderchest"),
                        builtInControl("vanish", 2, "sef:utility.vanish"))));
        builtIns.put("observation", builtIn(
                "observation",
                "Observation and logs",
                List.of(
                        builtInControl("command_spy", 0, "sef:commandspy.toggle"),
                        builtInControl("social_spy", 1, "sef:social.spy"),
                        builtInControl("logger", 2, "sef:logging.status"))));
    }

    private PanelDefinition builtIn(String id, String title, List<Control> controls) {
        List<Control> available = controls.stream()
                .filter(control -> catalog.find(control.actionId()).isPresent())
                .toList();
        return new PanelDefinition(
                SCHEMA_VERSION,
                id,
                1L,
                State.BUILT_IN,
                title,
                "sef.kernel.panel.use",
                Math.max(1, (available.size() + 8) / 9),
                available,
                new UUID(0L, 0L),
                Instant.EPOCH.toString());
    }

    private Control builtInControl(String id, int slot, String actionId) {
        CommandDefinition action = catalog.find(actionId).orElse(null);
        String permission = action == null
                ? "sef.kernel.panel.use"
                : action.permissionIds().stream().sorted().findFirst().orElse("sef.kernel.panel.use");
        PanelContracts.TargetPolicy targetPolicy = action == null
                ? PanelContracts.TargetPolicy.NONE
                : targetPolicy(action.targetBehavior());
        return new Control(
                id,
                slot,
                1,
                actionId,
                permission,
                targetPolicy,
                executionContext(targetPolicy),
                contextPermission(executionContext(targetPolicy)),
                audienceKind(targetPolicy),
                targetPolicy == PanelContracts.TargetPolicy.BOUNDED_AUDIENCE ? 64 : 1,
                Map.of(),
                action != null && action.accessClass().isPrivileged());
    }

    private void validateDefinition(PanelDefinition panel, State requiredState) {
        Objects.requireNonNull(panel, "panel");
        if (panel.schemaVersion() != SCHEMA_VERSION || panel.state() != requiredState) {
            throw new IllegalArgumentException("panel schema or state is invalid");
        }
        if (!ID.matcher(panel.id()).matches() || panel.rows() < 1 || panel.rows() > 6
                || panel.controls().size() > 54 || !capabilities.contains(panel.permissionId())) {
            throw new IllegalArgumentException("panel metadata is invalid");
        }
        boolean[] occupied = new boolean[panel.rows() * 9];
        Set<String> ids = new java.util.HashSet<>();
        for (Control control : panel.controls()) {
            if (!ids.add(control.id())) {
                throw new IllegalArgumentException("panel control id is duplicated");
            }
            CommandDefinition action = catalog.find(control.actionId()).orElse(null);
            if (action == null) {
                throw new IllegalArgumentException("panel control action is not cataloged");
            }
            if (!capabilities.contains(control.permissionId())
                    || !action.permissionIds().contains(control.permissionId())) {
                throw new IllegalArgumentException("panel control permission does not preserve the action permission");
            }
            int end = control.slot() + control.span();
            if (control.slot() < 0 || control.span() < 1 || control.span() > 9 || end > occupied.length) {
                throw new IllegalArgumentException("panel control is outside the grid");
            }
            for (int slot = control.slot(); slot < end; slot++) {
                if (occupied[slot]) {
                    throw new IllegalArgumentException("panel controls overlap");
                }
                occupied[slot] = true;
            }
            PanelContracts.AudienceKind expectedAudience = audienceKind(control.targetPolicy());
            if (control.audienceKind() != expectedAudience
                    || control.maximumTargets() < 1 || control.maximumTargets() > 512
                    || expectedAudience != PanelContracts.AudienceKind.BOUNDED_AUDIENCE
                    && control.maximumTargets() != 1) {
                throw new IllegalArgumentException("panel control audience is invalid");
            }
            if ((control.executionContext() == PanelContracts.ExecutionContext.SERVER_PROFILE
                    || control.executionContext() == PanelContracts.ExecutionContext.NATIVE_BULK
                    || control.executionContext() == PanelContracts.ExecutionContext.AS_EACH_PARTICIPANT)
                    && (!control.permissionId().equals(action.permissionIds().stream().sorted().findFirst().orElse(""))
                    || !contextPermission(control.executionContext()).equals(control.contextPermissionId())
                    || !capabilities.contains(control.contextPermissionId()))) {
                throw new IllegalArgumentException("delegated control permission is invalid");
            }
            if ((control.executionContext() == PanelContracts.ExecutionContext.ACTOR
                    || control.executionContext() == PanelContracts.ExecutionContext.TARGET_ACTOR)
                    && !control.contextPermissionId().isBlank()) {
                throw new IllegalArgumentException("ordinary control declares a delegated context permission");
            }
            if (control.fixedArguments().size() > 32
                    || control.fixedArguments().entrySet().stream()
                    .anyMatch(entry -> !safe(entry.getKey(), 64) || !safe(entry.getValue(), 256))) {
                throw new IllegalArgumentException("panel control arguments are outside bounds");
            }
        }
    }

    private int knownPanelCount() {
        Set<String> ids = new java.util.HashSet<>(drafts.keySet());
        ids.addAll(published.keySet());
        return ids.size();
    }

    private void appendHistory(String id, PanelDefinition definition) {
        List<PanelDefinition> revisions = new ArrayList<>(history.getOrDefault(id, List.of()));
        revisions.removeIf(existing -> existing.revision() == definition.revision());
        revisions.add(definition);
        revisions.sort(Comparator.comparingLong(PanelDefinition::revision));
        while (revisions.size() > MAXIMUM_HISTORY_PER_PANEL) {
            revisions.removeFirst();
        }
        history.put(id, List.copyOf(revisions));
    }

    private void notifyListeners(PublicationEvent event) {
        listeners.forEach(listener -> listener.accept(event));
    }

    private void writable() {
        if (state == RepositoryState.RECOVERY
                || state == RepositoryState.UNSUPPORTED
                || state == RepositoryState.ERROR
                || state == RepositoryState.CLOSED) {
            throw new IllegalStateException("Admin panel repository is not writable in " + state + " state");
        }
    }

    private static void putUnique(Map<String, PanelDefinition> destination, PanelDefinition panel) {
        if (destination.putIfAbsent(panel.id(), panel) != null) {
            throw new IllegalStateException("Duplicate panel id");
        }
    }

    private static PanelContracts.TargetPolicy targetPolicy(CommandDefinition.TargetBehavior behavior) {
        return switch (behavior) {
            case NONE -> PanelContracts.TargetPolicy.NONE;
            case SELF -> PanelContracts.TargetPolicy.SELF;
            case OPTIONAL_PLAYER, REQUIRED_PLAYER -> PanelContracts.TargetPolicy.SELECTED_VISIBLE_PLAYER;
            case BOUNDED_PLAYERS -> PanelContracts.TargetPolicy.BOUNDED_AUDIENCE;
            case SERVER -> PanelContracts.TargetPolicy.SERVER;
        };
    }

    private static PanelContracts.ExecutionContext executionContext(PanelContracts.TargetPolicy targetPolicy) {
        return switch (targetPolicy) {
            case NONE, SELF -> PanelContracts.ExecutionContext.ACTOR;
            case EXPLICIT_VISIBLE_PLAYER, SELECTED_VISIBLE_PLAYER -> PanelContracts.ExecutionContext.TARGET_ACTOR;
            case BOUNDED_AUDIENCE -> PanelContracts.ExecutionContext.NATIVE_BULK;
            case SERVER -> PanelContracts.ExecutionContext.SERVER_PROFILE;
        };
    }

    private static PanelContracts.AudienceKind audienceKind(PanelContracts.TargetPolicy targetPolicy) {
        return switch (targetPolicy) {
            case NONE, SELF -> PanelContracts.AudienceKind.SELF;
            case EXPLICIT_VISIBLE_PLAYER -> PanelContracts.AudienceKind.ONE_VISIBLE_PLAYER;
            case SELECTED_VISIBLE_PLAYER -> PanelContracts.AudienceKind.SELECTED_VISIBLE_PLAYERS;
            case BOUNDED_AUDIENCE -> PanelContracts.AudienceKind.BOUNDED_AUDIENCE;
            case SERVER -> PanelContracts.AudienceKind.SERVER;
        };
    }

    private static String contextPermission(PanelContracts.ExecutionContext executionContext) {
        return switch (executionContext) {
            case ACTOR, TARGET_ACTOR -> "";
            case SERVER_PROFILE -> "sef.kernel.panel.context.server";
            case NATIVE_BULK -> "sef.kernel.panel.context.bulk";
            case AS_EACH_PARTICIPANT -> "sef.kernel.panel.context.participants";
        };
    }

    private static String normalizeId(String value) {
        String normalized = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (!ID.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Panel identifier is invalid");
        }
        return normalized;
    }

    private static String bounded(String value, int maximumLength) {
        String bounded = Objects.requireNonNull(value, "value").trim();
        if (bounded.isBlank() || bounded.length() > maximumLength
                || bounded.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Panel text is outside bounds");
        }
        return bounded;
    }

    private static boolean safe(String value, int maximumLength) {
        return value != null && !value.isBlank() && value.length() <= maximumLength
                && value.codePoints().noneMatch(Character::isISOControl);
    }

    public record PanelDefinition(
            int schemaVersion,
            String id,
            long revision,
            State state,
            String title,
            String permissionId,
            int rows,
            List<Control> controls,
            UUID updatedBy,
            String updatedAt
    ) {
        public PanelDefinition {
            id = normalizeId(id);
            if (revision < 1L) {
                throw new IllegalArgumentException("Panel revision must be positive");
            }
            Objects.requireNonNull(state, "state");
            title = bounded(title, 128);
            permissionId = bounded(permissionId, 128).toLowerCase(Locale.ROOT);
            controls = List.copyOf(Objects.requireNonNull(controls, "controls"));
            Objects.requireNonNull(updatedBy, "updatedBy");
            Instant.parse(Objects.requireNonNull(updatedAt, "updatedAt"));
        }

        public PanelDefinition withControls(List<Control> replacement) {
            return new PanelDefinition(
                    schemaVersion,
                    id,
                    revision,
                    state,
                    title,
                    permissionId,
                    rows,
                    replacement,
                    updatedBy,
                    updatedAt);
        }
    }

    public record Control(
            String id,
            int slot,
            int span,
            String actionId,
            String permissionId,
            PanelContracts.TargetPolicy targetPolicy,
            PanelContracts.ExecutionContext executionContext,
            String contextPermissionId,
            PanelContracts.AudienceKind audienceKind,
            int maximumTargets,
            Map<String, String> fixedArguments,
            boolean destructive
    ) {
        public Control {
            id = normalizeId(id);
            actionId = bounded(actionId, 128).toLowerCase(Locale.ROOT);
            permissionId = bounded(permissionId, 128).toLowerCase(Locale.ROOT);
            Objects.requireNonNull(targetPolicy, "targetPolicy");
            Objects.requireNonNull(executionContext, "executionContext");
            contextPermissionId = contextPermissionId == null || contextPermissionId.isBlank()
                    ? ""
                    : bounded(contextPermissionId, 128).toLowerCase(Locale.ROOT);
            Objects.requireNonNull(audienceKind, "audienceKind");
            fixedArguments = Map.copyOf(Objects.requireNonNull(fixedArguments, "fixedArguments"));
        }
    }

    public record Preview(String panelId, long revision, List<PreviewControl> controls, List<String> problems) {
        public Preview {
            controls = List.copyOf(controls);
            problems = List.copyOf(problems);
        }
    }

    public record PreviewControl(
            String controlId,
            String actionId,
            PanelContracts.ExecutionContext executionContext,
            PanelContracts.AudienceKind audience,
            int maximumTargets,
            String commandFallback
    ) {
    }

    public record Execution(
            PanelDefinition panel,
            Control control,
            CommandDefinition action
    ) {
    }

    public record PublicationEvent(String panelId, long revision, PublicationKind kind) {
    }

    private record Snapshot(
            List<PanelDefinition> drafts,
            List<PanelDefinition> published,
            List<HistoryRecord> history
    ) {
        private Snapshot {
            drafts = List.copyOf(Objects.requireNonNull(drafts, "drafts"));
            published = List.copyOf(Objects.requireNonNull(published, "published"));
            history = List.copyOf(Objects.requireNonNull(history, "history"));
        }
    }

    private record HistoryRecord(String panelId, List<PanelDefinition> revisions) {
        private HistoryRecord {
            panelId = normalizeId(panelId);
            revisions = List.copyOf(Objects.requireNonNull(revisions, "revisions"));
        }
    }

    public enum State {
        BUILT_IN,
        DRAFT,
        PUBLISHED
    }

    public enum PublicationKind {
        PUBLISH,
        ROLLBACK
    }
}
