package com.enviouse.sef.automation;

import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.command.CommandWrapperService;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CommandProfileService implements StorageRepository {
    public static final int SCHEMA_VERSION = 1;
    private static final int MAXIMUM_PROFILES = 256;
    private static final int MAXIMUM_HISTORY = 32;
    private static final int MAXIMUM_COMMAND_LENGTH = 2048;
    private static final Pattern ID = Pattern.compile("[a-z][a-z0-9_.-]*:[a-z0-9_./-]+");
    private static final Pattern ROOT = Pattern.compile("[a-z0-9_:-]{1,64}");
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([a-z][a-z0-9_.-]{0,63})}");
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantJsonAdapter())
            .create();

    private final Map<String, CommandProfile> drafts = new LinkedHashMap<>();
    private final Map<String, CommandProfile> published = new LinkedHashMap<>();
    private final Map<String, List<CommandProfile>> history = new LinkedHashMap<>();
    private Predicate<String> referenceCheck = ignored -> false;
    private StorageService.Document document;
    private RepositoryState state = RepositoryState.NEW;
    private Path path;
    private long revision;
    private long flushedRevision;

    @Override
    public String id() {
        return "sef:command_profiles";
    }

    @Override
    public String domain() {
        return "command profiles";
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public synchronized Path path() {
        return path;
    }

    public synchronized void setReferenceCheck(Predicate<String> referenceCheck) {
        this.referenceCheck = Objects.requireNonNull(referenceCheck, "referenceCheck");
    }

    @Override
    public synchronized LoadResult load(Path managedRoot) {
        path = Objects.requireNonNull(managedRoot, "managedRoot")
                .resolve("command-profiles.json")
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
                    || snapshot.drafts().size() + snapshot.published().size() > MAXIMUM_PROFILES
                    || snapshot.history().size() > MAXIMUM_PROFILES) {
                throw new IllegalStateException("Command profile collections are outside bounds");
            }
            for (CommandProfile profile : snapshot.drafts()) {
                validate(profile, State.DRAFT);
                putUnique(drafts, profile);
            }
            for (CommandProfile profile : snapshot.published()) {
                validate(profile, State.PUBLISHED);
                putUnique(published, profile);
            }
            for (HistoryRecord record : snapshot.history()) {
                String id = normalizeId(record.id());
                if (record.revisions().size() > MAXIMUM_HISTORY) {
                    throw new IllegalStateException("Command profile history limit exceeded");
                }
                for (CommandProfile profile : record.revisions()) {
                    validate(profile, State.PUBLISHED);
                    if (!profile.id().equals(id)) {
                        throw new IllegalStateException("Command profile history identity mismatch");
                    }
                }
                history.put(id, record.revisions());
            }
            state = RepositoryState.READY;
            if (document.migrated()) {
                revision++;
            }
            return new LoadResult(state, "loaded " + published.size() + " command profiles");
        } catch (RuntimeException exception) {
            drafts.clear();
            published.clear();
            history.clear();
            state = RepositoryState.RECOVERY;
            return new LoadResult(state, exception.getClass().getSimpleName());
        }
    }

    public synchronized ActionResult<CommandProfile> createDraft(
            String id,
            Context context,
            String commandTemplate,
            Set<String> arguments,
            int maximumTargets,
            UUID actorId
    ) {
        writable();
        String normalizedId = normalizeId(id);
        if (drafts.containsKey(normalizedId) || published.containsKey(normalizedId)) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "command profile id already exists");
        }
        if (drafts.size() + published.size() >= MAXIMUM_PROFILES) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "command profile limit reached");
        }
        CommandProfile profile;
        try {
            String template = normalizeTemplate(commandTemplate);
            profile = new CommandProfile(
                    SCHEMA_VERSION,
                    normalizedId,
                    1L,
                    State.DRAFT,
                    context == Context.ACTOR,
                    Objects.requireNonNull(context, "context"),
                    root(template),
                    template,
                    arguments,
                    maximumTargets,
                    context == Context.TARGETED_ACTOR,
                    context == Context.TARGETED_ACTOR,
                    Objects.requireNonNull(actorId, "actorId"),
                    Instant.now());
            validate(profile, State.DRAFT);
        } catch (IllegalArgumentException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, exception.getMessage());
        }
        drafts.put(normalizedId, profile);
        revision++;
        return ActionResult.success(profile);
    }

    public synchronized ActionResult<CommandProfile> saveDraft(
            CommandProfile requested,
            long expectedRevision,
            UUID actorId
    ) {
        writable();
        Objects.requireNonNull(requested, "requested");
        String id = normalizeId(requested.id());
        CommandProfile current = drafts.get(id);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "command profile draft not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "command profile draft revision changed");
        }
        CommandProfile replacement = copy(
                requested,
                Math.addExact(current.revision(), 1L),
                State.DRAFT,
                requested.enabled(),
                Objects.requireNonNull(actorId, "actorId"),
                Instant.now());
        try {
            validate(replacement, State.DRAFT);
        } catch (IllegalArgumentException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, exception.getMessage());
        }
        drafts.put(id, replacement);
        revision++;
        return ActionResult.success(replacement);
    }

    public synchronized Validation validateDraft(String id, long expectedRevision) {
        CommandProfile draft = drafts.get(normalizeId(id));
        if (draft == null) {
            return new Validation(false, List.of("command profile draft not found"), "", Set.of());
        }
        if (draft.revision() != expectedRevision) {
            return new Validation(false, List.of("command profile draft revision changed"), "", Set.of());
        }
        try {
            validate(draft, State.DRAFT);
            return new Validation(true, List.of(), draft.root(), placeholders(draft.commandTemplate()));
        } catch (IllegalArgumentException exception) {
            return new Validation(false, List.of(exception.getMessage()), draft.root(), Set.of());
        }
    }

    public synchronized ActionResult<RenderedCommand> test(
            String id,
            long expectedRevision,
            Map<String, String> arguments,
            int targetCount
    ) {
        CommandProfile draft = drafts.get(normalizeId(id));
        if (draft == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "command profile draft not found");
        }
        if (draft.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "command profile draft revision changed");
        }
        return render(draft, arguments, targetCount);
    }

    public synchronized ActionResult<CommandProfile> publish(
            String id,
            long expectedRevision,
            UUID actorId
    ) {
        writable();
        String normalizedId = normalizeId(id);
        CommandProfile draft = drafts.get(normalizedId);
        if (draft == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "command profile draft not found");
        }
        if (draft.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "command profile draft revision changed");
        }
        CommandProfile current = published.get(normalizedId);
        long next = Math.max(draft.revision(), current == null ? 0L : current.revision()) + 1L;
        boolean enabled = draft.context() == Context.ACTOR && draft.enabled();
        CommandProfile publication = copy(
                draft,
                next,
                State.PUBLISHED,
                enabled,
                Objects.requireNonNull(actorId, "actorId"),
                Instant.now());
        try {
            validate(publication, State.PUBLISHED);
        } catch (IllegalArgumentException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, exception.getMessage());
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

    public synchronized ActionResult<CommandProfile> setEnabled(
            String id,
            long expectedRevision,
            boolean enabled,
            UUID actorId
    ) {
        writable();
        String normalizedId = normalizeId(id);
        CommandProfile current = published.get(normalizedId);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "published command profile not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "command profile revision changed");
        }
        CommandProfile replacement = copy(
                current,
                Math.addExact(current.revision(), 1L),
                State.PUBLISHED,
                enabled,
                Objects.requireNonNull(actorId, "actorId"),
                Instant.now());
        appendHistory(replacement);
        published.put(normalizedId, replacement);
        revision++;
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<CommandProfile> rollback(
            String id,
            long expectedRevision,
            long historicalRevision,
            UUID actorId
    ) {
        writable();
        String normalizedId = normalizeId(id);
        CommandProfile current = published.get(normalizedId);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "published command profile not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "command profile revision changed");
        }
        CommandProfile historical = history.getOrDefault(normalizedId, List.of()).stream()
                .filter(profile -> profile.revision() == historicalRevision)
                .findFirst()
                .orElse(null);
        if (historical == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "command profile history revision not found");
        }
        CommandProfile replacement = copy(
                historical,
                Math.addExact(current.revision(), 1L),
                State.PUBLISHED,
                historical.enabled(),
                Objects.requireNonNull(actorId, "actorId"),
                Instant.now());
        appendHistory(replacement);
        published.put(normalizedId, replacement);
        revision++;
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<Void> delete(String id, long expectedRevision) {
        writable();
        String normalizedId = normalizeId(id);
        CommandProfile draft = drafts.get(normalizedId);
        if (draft != null) {
            if (draft.revision() != expectedRevision) {
                return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "command profile draft revision changed");
            }
            drafts.remove(normalizedId);
            revision++;
            return ActionResult.success(null);
        }
        CommandProfile current = published.get(normalizedId);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "command profile not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "command profile revision changed");
        }
        if (current.enabled()) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "disable the command profile before deletion");
        }
        if (referenceCheck.test(normalizedId)) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "command profile is referenced");
        }
        published.remove(normalizedId);
        history.remove(normalizedId);
        revision++;
        return ActionResult.success(null);
    }

    public synchronized ActionResult<RenderedCommand> renderPublished(
            String id,
            long expectedRevision,
            Context requiredContext,
            Map<String, String> arguments,
            int targetCount
    ) {
        CommandProfile profile = published.get(normalizeId(id));
        if (profile == null || !profile.enabled()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "enabled command profile not found");
        }
        if (profile.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "command profile revision changed");
        }
        if (profile.context() != requiredContext) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "command profile context does not match");
        }
        return render(profile, arguments, targetCount);
    }

    public synchronized Optional<CommandProfile> find(String id) {
        return Optional.ofNullable(published.get(normalizeId(id)));
    }

    public synchronized List<CommandProfile> publications() {
        return published.values().stream().sorted(Comparator.comparing(CommandProfile::id)).toList();
    }

    public synchronized List<CommandProfile> drafts() {
        return drafts.values().stream().sorted(Comparator.comparing(CommandProfile::id)).toList();
    }

    public synchronized List<CommandProfile> history(String id) {
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

    private ActionResult<RenderedCommand> render(
            CommandProfile profile,
            Map<String, String> values,
            int targetCount
    ) {
        if (targetCount < 1 || targetCount > profile.maximumTargets()) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "command profile target count is outside policy");
        }
        Map<String, String> normalized = boundedArguments(values);
        Set<String> required = placeholders(profile.commandTemplate());
        if (!normalized.keySet().equals(required) || !profile.allowedArguments().containsAll(required)) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "command profile arguments do not match");
        }
        String command = profile.commandTemplate();
        for (String key : required) {
            command = command.replace("{" + key + "}", quote(normalized.get(key)));
        }
        CommandWrapperService.Request request = new CommandWrapperService.Request(
                UUID.randomUUID(),
                null,
                null,
                switch (profile.context()) {
                    case ACTOR, TARGETED_ACTOR -> com.enviouse.sef.kernel.command.CommandDefinition.SourceType.PLAYER;
                    case SERVER -> com.enviouse.sef.kernel.command.CommandDefinition.SourceType.SERVER_PROFILE;
                },
                command,
                CommandWrapperService.OutputMode.NORMAL,
                CommandWrapperService.Origin.DIRECT,
                profile.revision(),
                0L,
                List.of(),
                normalized);
        ActionResult<CommandWrapperService.Preflight> preflight =
                CommandWrapperService.preflight(request, MAXIMUM_COMMAND_LENGTH);
        if (!preflight.successful() || !preflight.value().normalizedRoot().equals(profile.root())) {
            return ActionResult.failure(
                    preflight.successful() ? ActionResult.ReasonCode.POLICY_DENIED : preflight.reason(),
                    preflight.successful() ? "command profile root changed" : preflight.detail());
        }
        return ActionResult.success(new RenderedCommand(
                profile.id(),
                profile.revision(),
                profile.context(),
                preflight.value().normalizedCommand(),
                profile.root(),
                targetCount,
                profile.consentRequired(),
                profile.strictParticipant()));
    }

    private static void validate(CommandProfile profile, State requiredState) {
        Objects.requireNonNull(profile, "profile");
        if (profile.schemaVersion() != SCHEMA_VERSION
                || profile.state() != requiredState
                || !ID.matcher(profile.id()).matches()
                || !ROOT.matcher(profile.root()).matches()
                || profile.revision() < 1L
                || profile.maximumTargets() < 1
                || profile.maximumTargets() > 100
                || profile.commandTemplate().isBlank()
                || profile.commandTemplate().length() > MAXIMUM_COMMAND_LENGTH
                || profile.commandTemplate().indexOf('\n') >= 0
                || profile.commandTemplate().indexOf('\r') >= 0
                || !root(profile.commandTemplate()).equals(profile.root())
                || profile.allowedArguments().size() > 32
                || !profile.allowedArguments().containsAll(placeholders(profile.commandTemplate()))) {
            throw new IllegalArgumentException("Command profile is invalid");
        }
        String root = profile.root();
        if (root.equals("run") || root.equals("silent") || root.equals("sudo")
                || root.equals("alias") || root.equals("bundle") || root.equals("profile")) {
            throw new IllegalArgumentException("Command profile wrapper recursion is forbidden");
        }
        if (profile.context() == Context.SERVER && profile.enabled() && requiredState == State.DRAFT) {
            throw new IllegalArgumentException("Server profiles must publish disabled");
        }
        if (profile.context() == Context.TARGETED_ACTOR
                && !placeholders(profile.commandTemplate()).contains("target")) {
            throw new IllegalArgumentException("Targeted actor profiles require the target placeholder");
        }
    }

    private void appendHistory(CommandProfile profile) {
        List<CommandProfile> revisions = new ArrayList<>(history.getOrDefault(profile.id(), List.of()));
        revisions.removeIf(existing -> existing.revision() == profile.revision());
        revisions.add(profile);
        revisions.sort(Comparator.comparingLong(CommandProfile::revision));
        while (revisions.size() > MAXIMUM_HISTORY) {
            revisions.removeFirst();
        }
        history.put(profile.id(), List.copyOf(revisions));
    }

    private void writable() {
        if (state == RepositoryState.RECOVERY
                || state == RepositoryState.UNSUPPORTED
                || state == RepositoryState.ERROR
                || state == RepositoryState.CLOSED) {
            throw new IllegalStateException("Command profile storage is unavailable");
        }
    }

    private static void putUnique(Map<String, CommandProfile> destination, CommandProfile profile) {
        if (destination.putIfAbsent(profile.id(), profile) != null) {
            throw new IllegalStateException("Duplicate command profile id");
        }
    }

    private static CommandProfile copy(
            CommandProfile source,
            long revision,
            State state,
            boolean enabled,
            UUID actorId,
            Instant now
    ) {
        return new CommandProfile(
                SCHEMA_VERSION,
                source.id(),
                revision,
                state,
                enabled,
                source.context(),
                source.root(),
                source.commandTemplate(),
                source.allowedArguments(),
                source.maximumTargets(),
                source.consentRequired(),
                source.strictParticipant(),
                actorId,
                now);
    }

    private static Map<String, String> boundedArguments(Map<String, String> source) {
        Objects.requireNonNull(source, "source");
        if (source.size() > 32) {
            throw new IllegalArgumentException("Too many command profile arguments");
        }
        Map<String, String> result = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            String normalizedKey = normalizePart(key);
            String bounded = Objects.requireNonNull(value, "value").trim();
            if (bounded.length() > 256
                    || bounded.codePoints().anyMatch(character ->
                    Character.isISOControl(character) && character != '\t')) {
                throw new IllegalArgumentException("Command profile argument is outside bounds");
            }
            result.put(normalizedKey, bounded);
        });
        return Map.copyOf(result);
    }

    private static Set<String> placeholders(String template) {
        Matcher matcher = PLACEHOLDER.matcher(template);
        Set<String> result = new LinkedHashSet<>();
        while (matcher.find()) {
            result.add(normalizePart(matcher.group(1)));
        }
        return Set.copyOf(result);
    }

    private static String quote(String value) {
        if (value.matches("[a-zA-Z0-9_:.+/@-]+")) {
            return value;
        }
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String normalizeTemplate(String command) {
        String normalized = Objects.requireNonNull(command, "command").trim();
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1).trim();
        }
        if (normalized.isBlank() || normalized.length() > MAXIMUM_COMMAND_LENGTH
                || normalized.indexOf('\n') >= 0 || normalized.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("Command profile template is invalid");
        }
        return normalized;
    }

    private static String root(String command) {
        String normalized = normalizeTemplate(command);
        int separator = normalized.indexOf(' ');
        return (separator < 0 ? normalized : normalized.substring(0, separator))
                .toLowerCase(Locale.ROOT);
    }

    private static String normalizeId(String value) {
        String normalized = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (!ID.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid command profile id");
        }
        return normalized;
    }

    private static String normalizePart(String value) {
        String normalized = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z][a-z0-9_.-]{0,63}")) {
            throw new IllegalArgumentException("Invalid command profile field");
        }
        return normalized;
    }

    public record CommandProfile(
            int schemaVersion,
            String id,
            long revision,
            State state,
            boolean enabled,
            Context context,
            String root,
            String commandTemplate,
            Set<String> allowedArguments,
            int maximumTargets,
            boolean consentRequired,
            boolean strictParticipant,
            UUID changedBy,
            Instant changedAt
    ) {
        public CommandProfile {
            id = normalizeId(id);
            Objects.requireNonNull(state, "state");
            Objects.requireNonNull(context, "context");
            root = Objects.requireNonNull(root, "root").trim().toLowerCase(Locale.ROOT);
            commandTemplate = normalizeTemplate(commandTemplate);
            allowedArguments = Set.copyOf(allowedArguments == null ? Set.of() : allowedArguments.stream()
                    .map(CommandProfileService::normalizePart)
                    .toList());
            Objects.requireNonNull(changedBy, "changedBy");
            Objects.requireNonNull(changedAt, "changedAt");
        }
    }

    public record RenderedCommand(
            String profileId,
            long profileRevision,
            Context context,
            String command,
            String root,
            int targetCount,
            boolean consentRequired,
            boolean strictParticipant
    ) {
    }

    public record Validation(boolean valid, List<String> problems, String root, Set<String> placeholders) {
        public Validation {
            problems = List.copyOf(problems);
            placeholders = Set.copyOf(placeholders);
        }
    }

    public enum State {
        DRAFT,
        PUBLISHED
    }

    public enum Context {
        ACTOR,
        TARGETED_ACTOR,
        SERVER
    }

    private record Snapshot(
            List<CommandProfile> drafts,
            List<CommandProfile> published,
            List<HistoryRecord> history
    ) {
        private Snapshot {
            drafts = List.copyOf(drafts == null ? List.of() : drafts);
            published = List.copyOf(published == null ? List.of() : published);
            history = List.copyOf(history == null ? List.of() : history);
        }
    }

    private record HistoryRecord(String id, List<CommandProfile> revisions) {
        private HistoryRecord {
            revisions = List.copyOf(revisions == null ? List.of() : revisions);
        }
    }
}
