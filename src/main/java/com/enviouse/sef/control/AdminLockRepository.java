package com.enviouse.sef.control;

import com.enviouse.sef.kernel.ActionResult;
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
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public final class AdminLockRepository implements StorageRepository {
    public static final int SCHEMA_VERSION = 1;
    public static final int MAXIMUM_LOCKS = 100_000;
    public static final int MAXIMUM_PROFILES = 128;
    public static final int MAXIMUM_HISTORY = 200_000;
    private static final Pattern ID = Pattern.compile("[a-z0-9][a-z0-9._]{0,63}");
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantJsonAdapter())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private final Map<UUID, AccountLock> locks = new LinkedHashMap<>();
    private final Map<String, BreakGlassProfile> profiles = new LinkedHashMap<>();
    private final List<HistoryEntry> history = new ArrayList<>();
    private Set<CommandDefinition.AccessClass> requiredClasses = Set.of();
    private Path path;
    private StorageService.Document document;
    private RepositoryState state = RepositoryState.NEW;
    private long revision = 1L;
    private long flushedRevision;

    @Override
    public String id() {
        return "sef:admin_lock";
    }

    @Override
    public String domain() {
        return "admin_lock";
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public synchronized Path path() {
        return path;
    }

    public synchronized ActionResult<AccountLock> lock(UUID subjectId, UUID actorId, String reason) {
        writable();
        UUID subject = Objects.requireNonNull(subjectId, "subjectId");
        UUID actor = Objects.requireNonNull(actorId, "actorId");
        AccountLock current = locks.get(subject);
        if (current != null && current.locked()) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "administrative account is already locked");
        }
        if (current == null && locks.size() >= MAXIMUM_LOCKS) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "administrative lock limit reached");
        }
        Instant now = Instant.now();
        AccountLock replacement = new AccountLock(
                subject,
                true,
                bounded(reason, 512, true),
                actor,
                now,
                current == null ? 1L : Math.addExact(current.revision(), 1L));
        locks.put(subject, replacement);
        history(subject, actor, "locked", replacement.reason(), replacement.revision());
        changed();
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<AccountLock> unlock(UUID subjectId, UUID actorId, String reason) {
        writable();
        UUID subject = Objects.requireNonNull(subjectId, "subjectId");
        AccountLock current = locks.get(subject);
        if (current == null || !current.locked()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "administrative account is not locked");
        }
        Instant now = Instant.now();
        AccountLock replacement = new AccountLock(
                subject,
                false,
                bounded(reason, 512, true),
                Objects.requireNonNull(actorId, "actorId"),
                now,
                Math.addExact(current.revision(), 1L));
        locks.put(subject, replacement);
        history(subject, actorId, "unlocked", replacement.reason(), replacement.revision());
        changed();
        return ActionResult.success(replacement);
    }

    public synchronized Optional<AccountLock> lock(UUID subjectId) {
        return Optional.ofNullable(locks.get(Objects.requireNonNull(subjectId, "subjectId")));
    }

    public synchronized List<AccountLock> locks() {
        return locks.values().stream()
                .sorted(Comparator.comparing(AccountLock::updatedAt).reversed())
                .toList();
    }

    public synchronized ActionResult<Set<CommandDefinition.AccessClass>> require(
            CommandDefinition.AccessClass accessClass,
            boolean required,
            UUID actorId
    ) {
        writable();
        Objects.requireNonNull(accessClass, "accessClass");
        if (accessClass == CommandDefinition.AccessClass.PLAYER
                || accessClass == CommandDefinition.AccessClass.TRUSTED_PLAYER) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.POLICY_DENIED,
                    "player access classes cannot require privileged sessions");
        }
        EnumSet<CommandDefinition.AccessClass> replacement = requiredClasses.isEmpty()
                ? EnumSet.noneOf(CommandDefinition.AccessClass.class)
                : EnumSet.copyOf(requiredClasses);
        if (required) {
            replacement.add(accessClass);
        } else {
            replacement.remove(accessClass);
        }
        requiredClasses = Set.copyOf(replacement);
        history(new UUID(0L, 0L), actorId, required ? "require" : "release_requirement",
                accessClass.name().toLowerCase(Locale.ROOT), revision);
        changed();
        return ActionResult.success(requiredClasses);
    }

    public synchronized Set<CommandDefinition.AccessClass> requiredClasses() {
        return requiredClasses;
    }

    public synchronized ActionResult<BreakGlassProfile> publishProfile(
            String profileId,
            Set<CommandDefinition.AccessClass> accessClasses,
            long maximumSeconds,
            UUID actorId
    ) {
        writable();
        String normalized;
        try {
            normalized = profileId(profileId);
        } catch (IllegalArgumentException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, exception.getMessage());
        }
        if (accessClasses == null
                || accessClasses.isEmpty()
                || accessClasses.contains(CommandDefinition.AccessClass.PLAYER)
                || accessClasses.contains(CommandDefinition.AccessClass.TRUSTED_PLAYER)
                || maximumSeconds < 1L
                || maximumSeconds > 3_600L) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.INVALID_INPUT,
                    "break glass profile is outside bounds");
        }
        BreakGlassProfile current = profiles.get(normalized);
        if (current == null && profiles.size() >= MAXIMUM_PROFILES) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "break glass profile limit reached");
        }
        BreakGlassProfile replacement = new BreakGlassProfile(
                normalized,
                Set.copyOf(accessClasses),
                maximumSeconds,
                current == null ? 1L : Math.addExact(current.revision(), 1L),
                true,
                Objects.requireNonNull(actorId, "actorId"),
                Instant.now());
        profiles.put(normalized, replacement);
        history(new UUID(0L, 0L), actorId, "profile_published", normalized, replacement.revision());
        changed();
        return ActionResult.success(replacement);
    }

    public synchronized Optional<BreakGlassProfile> profile(String profileId) {
        try {
            return Optional.ofNullable(profiles.get(profileId(profileId)));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    public synchronized List<BreakGlassProfile> profiles() {
        return profiles.values().stream().sorted(Comparator.comparing(BreakGlassProfile::id)).toList();
    }

    public synchronized List<HistoryEntry> history(UUID subjectId) {
        return history.stream()
                .filter(entry -> entry.subjectId().equals(Objects.requireNonNull(subjectId, "subjectId")))
                .sorted(Comparator.comparing(HistoryEntry::occurredAt).reversed())
                .toList();
    }

    public synchronized ActionResult<HistoryEntry> event(
            UUID subjectId,
            UUID actorId,
            String action,
            String detail
    ) {
        writable();
        history(
                Objects.requireNonNull(subjectId, "subjectId"),
                Objects.requireNonNull(actorId, "actorId"),
                action,
                detail,
                locks.getOrDefault(
                        subjectId,
                        new AccountLock(subjectId, false, "", actorId, Instant.now(), 1L)).revision());
        changed();
        return ActionResult.success(history.getLast());
    }

    public synchronized <T> ActionResult<T> commit(Supplier<ActionResult<T>> mutation) {
        Objects.requireNonNull(mutation, "mutation");
        Checkpoint checkpoint = checkpoint();
        ActionResult<T> result;
        try {
            result = Objects.requireNonNull(mutation.get(), "mutation result");
        } catch (RuntimeException exception) {
            restore(checkpoint);
            throw exception;
        }
        if (!result.successful()) {
            restore(checkpoint);
            return result;
        }
        try {
            flush();
            return result;
        } catch (IOException | RuntimeException exception) {
            restore(checkpoint);
            com.enviouse.sef.ServerEssentialsForge.LOGGER.error(
                    "Administrative lock persistence failed",
                    exception);
            return ActionResult.failure(
                    ActionResult.ReasonCode.STORAGE_ERROR,
                    "administrative lock storage could not be committed");
        }
    }

    @Override
    public synchronized LoadResult load(Path managedRoot) {
        path = Objects.requireNonNull(managedRoot, "managedRoot")
                .resolve("admin-lock.json")
                .toAbsolutePath()
                .normalize();
        clear();
        boolean existed = Files.exists(path, java.nio.file.LinkOption.NOFOLLOW_LINKS);
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(null);
        if (document == null) {
            state = existed ? RepositoryState.RECOVERY : RepositoryState.MISSING;
            return new LoadResult(state, existed ? "administrative lock storage unavailable" : "new repository");
        }
        try {
            Snapshot snapshot = GSON.fromJson(document.data(), Snapshot.class);
            if (snapshot == null
                    || snapshot.revision() < 1L
                    || snapshot.locks().size() > MAXIMUM_LOCKS
                    || snapshot.profiles().size() > MAXIMUM_PROFILES
                    || snapshot.history().size() > MAXIMUM_HISTORY) {
                throw new IllegalStateException("administrative lock snapshot is outside bounds");
            }
            for (AccountLock lock : snapshot.locks()) {
                validate(lock);
                if (locks.putIfAbsent(lock.subjectId(), lock) != null) {
                    throw new IllegalStateException("duplicate administrative account lock");
                }
            }
            for (BreakGlassProfile profile : snapshot.profiles()) {
                validate(profile);
                if (profiles.putIfAbsent(profile.id(), profile) != null) {
                    throw new IllegalStateException("duplicate break glass profile");
                }
            }
            requiredClasses = validateRequired(snapshot.requiredClasses());
            for (HistoryEntry entry : snapshot.history()) {
                validate(entry);
                history.add(entry);
            }
            revision = snapshot.revision();
            flushedRevision = document.migrated() ? Math.max(0L, revision - 1L) : revision;
            state = RepositoryState.READY;
            return new LoadResult(state, "loaded administrative lock data");
        } catch (RuntimeException exception) {
            clear();
            state = RepositoryState.RECOVERY;
            return new LoadResult(state, exception.getClass().getSimpleName());
        }
    }

    @Override
    public synchronized void flush() throws IOException {
        if (path == null || !dirty()) {
            return;
        }
        writable();
        Snapshot snapshot = new Snapshot(
                revision,
                new ArrayList<>(locks.values()),
                new ArrayList<>(profiles.values()),
                requiredClasses,
                new ArrayList<>(history));
        StorageService.write(path, domain(), SCHEMA_VERSION, GSON.toJsonTree(snapshot), document, Set.of());
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(document);
        flushedRevision = revision;
        state = RepositoryState.READY;
    }

    @Override
    public synchronized boolean dirty() {
        return revision > flushedRevision;
    }

    @Override
    public synchronized RepositoryState state() {
        return state;
    }

    private void clear() {
        locks.clear();
        profiles.clear();
        history.clear();
        requiredClasses = Set.of();
        revision = 1L;
        flushedRevision = 1L;
    }

    private void changed() {
        revision = Math.addExact(revision, 1L);
    }

    private void writable() {
        if (state == RepositoryState.RECOVERY
                || state == RepositoryState.UNSUPPORTED
                || state == RepositoryState.ERROR
                || state == RepositoryState.CLOSED) {
            throw new IllegalStateException("administrative lock storage is unavailable");
        }
    }

    private void history(
            UUID subjectId,
            UUID actorId,
            String action,
            String detail,
            long subjectRevision
    ) {
        if (history.size() >= MAXIMUM_HISTORY) {
            history.removeFirst();
        }
        history.add(new HistoryEntry(
                UUID.randomUUID(),
                Objects.requireNonNull(subjectId, "subjectId"),
                Objects.requireNonNull(actorId, "actorId"),
                bounded(action, 64, false),
                bounded(detail, 512, true),
                Instant.now(),
                Math.max(1L, subjectRevision)));
    }

    private Checkpoint checkpoint() {
        return new Checkpoint(
                new LinkedHashMap<>(locks),
                new LinkedHashMap<>(profiles),
                new ArrayList<>(history),
                requiredClasses,
                revision,
                flushedRevision,
                state,
                document);
    }

    private void restore(Checkpoint checkpoint) {
        locks.clear();
        locks.putAll(checkpoint.locks());
        profiles.clear();
        profiles.putAll(checkpoint.profiles());
        history.clear();
        history.addAll(checkpoint.history());
        requiredClasses = checkpoint.requiredClasses();
        revision = checkpoint.revision();
        flushedRevision = checkpoint.flushedRevision();
        state = checkpoint.state();
        document = checkpoint.document();
    }

    private static void validate(AccountLock lock) {
        Objects.requireNonNull(lock.subjectId(), "subjectId");
        bounded(lock.reason(), 512, true);
        Objects.requireNonNull(lock.changedBy(), "changedBy");
        Objects.requireNonNull(lock.updatedAt(), "updatedAt");
        if (lock.revision() < 1L) {
            throw new IllegalArgumentException("administrative account lock is invalid");
        }
    }

    private static void validate(BreakGlassProfile profile) {
        profileId(profile.id());
        if (profile.accessClasses().isEmpty()
                || profile.accessClasses().contains(CommandDefinition.AccessClass.PLAYER)
                || profile.accessClasses().contains(CommandDefinition.AccessClass.TRUSTED_PLAYER)
                || profile.maximumSeconds() < 1L
                || profile.maximumSeconds() > 3_600L
                || profile.revision() < 1L) {
            throw new IllegalArgumentException("break glass profile is invalid");
        }
        Objects.requireNonNull(profile.publishedBy(), "publishedBy");
        Objects.requireNonNull(profile.publishedAt(), "publishedAt");
    }

    private static void validate(HistoryEntry entry) {
        Objects.requireNonNull(entry.id(), "id");
        Objects.requireNonNull(entry.subjectId(), "subjectId");
        Objects.requireNonNull(entry.actorId(), "actorId");
        bounded(entry.action(), 64, false);
        bounded(entry.detail(), 512, true);
        Objects.requireNonNull(entry.occurredAt(), "occurredAt");
        if (entry.subjectRevision() < 1L) {
            throw new IllegalArgumentException("administrative lock history is invalid");
        }
    }

    private static Set<CommandDefinition.AccessClass> validateRequired(
            Set<CommandDefinition.AccessClass> values
    ) {
        Set<CommandDefinition.AccessClass> safe = Set.copyOf(Objects.requireNonNullElse(values, Set.of()));
        if (safe.contains(CommandDefinition.AccessClass.PLAYER)
                || safe.contains(CommandDefinition.AccessClass.TRUSTED_PLAYER)) {
            throw new IllegalArgumentException("administrative lock requirements are invalid");
        }
        return safe;
    }

    private static String profileId(String value) {
        String normalized = Objects.requireNonNull(value, "profileId").strip().toLowerCase(Locale.ROOT);
        if (!ID.matcher(normalized).matches()) {
            throw new IllegalArgumentException("break glass profile id is invalid");
        }
        return normalized;
    }

    private static String bounded(String value, int maximum, boolean allowBlank) {
        String normalized = Objects.requireNonNullElse(value, "").strip();
        if ((!allowBlank && normalized.isBlank())
                || normalized.length() > maximum
                || normalized.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("administrative lock text is outside bounds");
        }
        return normalized;
    }

    public record AccountLock(
            UUID subjectId,
            boolean locked,
            String reason,
            UUID changedBy,
            Instant updatedAt,
            long revision
    ) {
    }

    public record BreakGlassProfile(
            String id,
            Set<CommandDefinition.AccessClass> accessClasses,
            long maximumSeconds,
            long revision,
            boolean active,
            UUID publishedBy,
            Instant publishedAt
    ) {
        public BreakGlassProfile {
            accessClasses = Set.copyOf(Objects.requireNonNullElse(accessClasses, Set.of()));
        }
    }

    public record HistoryEntry(
            UUID id,
            UUID subjectId,
            UUID actorId,
            String action,
            String detail,
            Instant occurredAt,
            long subjectRevision
    ) {
    }

    private record Snapshot(
            long revision,
            List<AccountLock> locks,
            List<BreakGlassProfile> profiles,
            Set<CommandDefinition.AccessClass> requiredClasses,
            List<HistoryEntry> history
    ) {
        private Snapshot {
            locks = locks == null ? List.of() : List.copyOf(locks);
            profiles = profiles == null ? List.of() : List.copyOf(profiles);
            requiredClasses = Set.copyOf(Objects.requireNonNullElse(requiredClasses, Set.of()));
            history = history == null ? List.of() : List.copyOf(history);
        }
    }

    private record Checkpoint(
            Map<UUID, AccountLock> locks,
            Map<String, BreakGlassProfile> profiles,
            List<HistoryEntry> history,
            Set<CommandDefinition.AccessClass> requiredClasses,
            long revision,
            long flushedRevision,
            RepositoryState state,
            StorageService.Document document
    ) {
    }
}
