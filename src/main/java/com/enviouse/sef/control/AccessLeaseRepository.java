package com.enviouse.sef.control;

import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.storage.InstantJsonAdapter;
import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Clock;
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
import java.util.function.Supplier;
import java.util.regex.Pattern;

public final class AccessLeaseRepository implements StorageRepository {
    public static final int SCHEMA_VERSION = 1;
    public static final int MAXIMUM_PROFILES = 1_024;
    public static final int MAXIMUM_LEASES = 100_000;
    public static final int MAXIMUM_HISTORY = 200_000;
    public static final int MAXIMUM_PROFILE_PERMISSIONS = 256;
    public static final Duration MAXIMUM_LEASE_DURATION = Duration.ofDays(30);
    private static final Pattern ID = Pattern.compile("[a-z0-9][a-z0-9._]{0,63}");
    private static final Pattern QUOTA_ID = Pattern.compile("[a-z0-9][a-z0-9._:]{0,63}");
    private static final Pattern PERMISSION = Pattern.compile("sef\\.[a-z0-9._]{1,123}");
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantJsonAdapter())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    private static final Set<String> FORBIDDEN_EXACT = Set.of(
            "sef.commands.run",
            "sef.commands.silent",
            "sef.commands.permissions",
            "sef.commands.approval",
            "sef.commands.audit",
            "sef.commands.accessgrant",
            "sef.commands.adminlock");
    private static final List<String> FORBIDDEN_PREFIXES = List.of(
            "sef.commands.run.",
            "sef.commands.silent.",
            "sef.commands.permissions.",
            "sef.commands.approval.",
            "sef.commands.audit.",
            "sef.commands.accessgrant.",
            "sef.commands.adminlock.",
            "sef.permissions.",
            "sef.owner.");

    private final Predicate<String> permissionExists;
    private final Clock clock;
    private final Map<String, Profile> profiles = new LinkedHashMap<>();
    private final Map<UUID, Lease> leases = new LinkedHashMap<>();
    private final List<HistoryEntry> history = new ArrayList<>();
    private Path path;
    private StorageService.Document document;
    private RepositoryState state = RepositoryState.NEW;
    private long revision = 1L;
    private long flushedRevision;

    public AccessLeaseRepository(Predicate<String> permissionExists) {
        this(permissionExists, Clock.systemUTC());
    }

    AccessLeaseRepository(Predicate<String> permissionExists, Clock clock) {
        this.permissionExists = Objects.requireNonNull(permissionExists, "permissionExists");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public String id() {
        return "sef:access_leases";
    }

    @Override
    public String domain() {
        return "access_leases";
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public synchronized Path path() {
        return path;
    }

    public synchronized ActionResult<Profile> publishProfile(
            String profileId,
            UUID actorId,
            Set<String> permissions,
            Map<String, Long> quotas,
            Duration maximumDuration,
            boolean protectedProfile,
            boolean separationRequired,
            Scope scope
    ) {
        writable();
        String normalizedId;
        Set<String> normalizedPermissions;
        Map<String, Long> normalizedQuotas;
        try {
            normalizedId = profileId(profileId);
            normalizedPermissions = permissions(permissions);
            normalizedQuotas = quotas(quotas);
            validateDuration(maximumDuration);
            Objects.requireNonNull(scope, "scope");
            validateScope(scope);
        } catch (IllegalArgumentException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, exception.getMessage());
        }
        Profile current = profiles.get(normalizedId);
        if (current == null && profiles.size() >= MAXIMUM_PROFILES) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "access lease profile limit reached");
        }
        Instant now = now();
        Profile replacement = new Profile(
                normalizedId,
                current == null ? 1L : Math.addExact(current.revision(), 1L),
                normalizedPermissions,
                normalizedQuotas,
                maximumDuration.getSeconds(),
                protectedProfile,
                separationRequired,
                scope,
                true,
                Objects.requireNonNull(actorId, "actorId"),
                now);
        profiles.put(normalizedId, replacement);
        changed();
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<Profile> retireProfile(
            String profileId,
            UUID actorId,
            long expectedRevision
    ) {
        writable();
        Profile current;
        try {
            current = profiles.get(profileId(profileId));
        } catch (IllegalArgumentException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, exception.getMessage());
        }
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "access lease profile not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "access lease profile revision changed");
        }
        Profile replacement = new Profile(
                current.id(),
                Math.addExact(current.revision(), 1L),
                current.permissions(),
                current.quotas(),
                current.maximumDurationSeconds(),
                current.protectedProfile(),
                current.separationRequired(),
                current.scope(),
                false,
                Objects.requireNonNull(actorId, "actorId"),
                now());
        profiles.put(replacement.id(), replacement);
        changed();
        return ActionResult.success(replacement);
    }

    public synchronized Optional<Profile> profile(String profileId) {
        try {
            return Optional.ofNullable(profiles.get(profileId(profileId)));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    public synchronized List<Profile> profiles() {
        return profiles.values().stream()
                .sorted(Comparator.comparing(Profile::id))
                .toList();
    }

    public synchronized ActionResult<Lease> create(
            String profileId,
            UUID subjectId,
            UUID issuerId,
            Duration duration,
            String reason,
            String provider,
            String providerRevision
    ) {
        writable();
        expire(now());
        Profile profile;
        try {
            profile = profiles.get(profileId(profileId));
            validateDuration(duration);
        } catch (IllegalArgumentException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, exception.getMessage());
        }
        if (profile == null || !profile.active()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "active access lease profile not found");
        }
        if (duration.getSeconds() > profile.maximumDurationSeconds()) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "requested lease duration exceeds profile maximum");
        }
        UUID subject = Objects.requireNonNull(subjectId, "subjectId");
        UUID issuer = Objects.requireNonNull(issuerId, "issuerId");
        if (subject.equals(issuer)) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "self grants are not allowed");
        }
        boolean overlaps = leases.values().stream()
                .anyMatch(lease -> lease.subjectId().equals(subject)
                        && lease.profileId().equals(profile.id())
                        && lease.state().authoritative()
                        && lease.expiresAt().isAfter(now()));
        if (overlaps) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "an overlapping access lease already exists");
        }
        if (leases.size() >= MAXIMUM_LEASES) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "access lease limit reached");
        }
        Instant now = now();
        Lease lease = new Lease(
                UUID.randomUUID(),
                profile.id(),
                profile.revision(),
                profile.permissions(),
                profile.quotas(),
                subject,
                issuer,
                now,
                now.plus(duration),
                LeaseState.ACTIVE,
                bounded(reason, 512, false),
                profile.scope(),
                provider(provider),
                bounded(providerRevision, 128, true),
                false,
                1L,
                now);
        leases.put(lease.id(), lease);
        history(lease, issuer, "created", LeaseState.ACTIVE, LeaseState.ACTIVE);
        changed();
        return ActionResult.success(lease);
    }

    public synchronized ActionResult<Lease> renew(
            UUID leaseId,
            UUID actorId,
            Duration duration,
            String reason,
            long expectedRevision
    ) {
        writable();
        expire(now());
        Lease current = leases.get(Objects.requireNonNull(leaseId, "leaseId"));
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "access lease not found");
        }
        Profile profile = profiles.get(current.profileId());
        try {
            validateDuration(duration);
        } catch (IllegalArgumentException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, exception.getMessage());
        }
        if (profile == null || !profile.active() || profile.revision() != current.profileRevision()) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "bound access lease profile revision is no longer current");
        }
        if (duration.getSeconds() > profile.maximumDurationSeconds()) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "requested lease duration exceeds profile maximum");
        }
        if (current.state() == LeaseState.REVOKED || current.revision() != expectedRevision) {
            return ActionResult.failure(
                    current.revision() == expectedRevision
                            ? ActionResult.ReasonCode.POLICY_DENIED
                            : ActionResult.ReasonCode.CONFLICT,
                    current.revision() == expectedRevision
                            ? "revoked access leases cannot be renewed"
                            : "access lease revision changed");
        }
        Instant now = now();
        Lease replacement = replace(
                current,
                current.state() == LeaseState.EXPIRED ? LeaseState.ACTIVE : current.state(),
                now.plus(duration),
                bounded(reason, 512, false),
                current.pendingProviderCleanup(),
                now);
        leases.put(replacement.id(), replacement);
        history(replacement, actorId, "renewed", current.state(), replacement.state());
        changed();
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<Lease> transition(
            UUID leaseId,
            UUID actorId,
            LeaseState replacementState,
            String reason,
            long expectedRevision
    ) {
        writable();
        expire(now());
        Lease current = leases.get(Objects.requireNonNull(leaseId, "leaseId"));
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "access lease not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "access lease revision changed");
        }
        if (!allowedTransition(current.state(), replacementState)) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "access lease state transition is invalid");
        }
        Instant now = now();
        boolean pendingCleanup = replacementState == LeaseState.REVOKED
                && !current.provider().equals("internal");
        Lease replacement = replace(
                current,
                replacementState,
                current.expiresAt(),
                bounded(reason, 512, replacementState == LeaseState.ACTIVE),
                pendingCleanup,
                now);
        leases.put(replacement.id(), replacement);
        history(replacement, actorId, replacementState.name().toLowerCase(Locale.ROOT), current.state(), replacementState);
        changed();
        return ActionResult.success(replacement);
    }

    public synchronized Optional<Lease> lease(UUID leaseId) {
        expire(now());
        return Optional.ofNullable(leases.get(Objects.requireNonNull(leaseId, "leaseId")));
    }

    public synchronized List<Lease> leases(UUID subjectId, String profileId, LeaseState stateFilter) {
        expire(now());
        String normalizedProfile = profileId == null || profileId.isBlank() ? null : profileId(profileId);
        return leases.values().stream()
                .filter(lease -> subjectId == null || lease.subjectId().equals(subjectId))
                .filter(lease -> normalizedProfile == null || lease.profileId().equals(normalizedProfile))
                .filter(lease -> stateFilter == null || lease.state() == stateFilter)
                .sorted(Comparator.comparing(Lease::updatedAt).reversed())
                .toList();
    }

    public synchronized List<Lease> expiring(Duration within) {
        if (within.isNegative() || within.compareTo(Duration.ofDays(365)) > 0) {
            throw new IllegalArgumentException("access lease expiry window is outside bounds");
        }
        Instant now = now();
        expire(now);
        Instant upper = now.plus(within);
        return leases.values().stream()
                .filter(lease -> lease.state() == LeaseState.ACTIVE)
                .filter(lease -> !lease.expiresAt().isAfter(upper))
                .sorted(Comparator.comparing(Lease::expiresAt))
                .toList();
    }

    public synchronized List<HistoryEntry> history(UUID subjectId) {
        return history.stream()
                .filter(entry -> entry.subjectId().equals(Objects.requireNonNull(subjectId, "subjectId")))
                .sorted(Comparator.comparing(HistoryEntry::occurredAt).reversed())
                .toList();
    }

    public synchronized LeaseDecision decide(UUID subjectId, String permission, ScopeContext context) {
        Instant now = now();
        expire(now);
        String normalizedPermission = permission(permission, false);
        boolean granted = leases.values().stream()
                .filter(lease -> lease.subjectId().equals(subjectId))
                .filter(lease -> lease.state() == LeaseState.ACTIVE)
                .filter(lease -> lease.expiresAt().isAfter(now))
                .filter(lease -> scopeMatches(lease.scope(), context))
                .anyMatch(lease -> lease.permissions().contains(normalizedPermission));
        return granted ? LeaseDecision.GRANTED : LeaseDecision.ABSTAIN;
    }

    public synchronized Optional<Long> quota(
            UUID subjectId,
            String quotaId,
            ScopeContext context
    ) {
        Instant now = now();
        expire(now);
        String normalizedQuota = bounded(quotaId, 64, false).toLowerCase(Locale.ROOT);
        if (!QUOTA_ID.matcher(normalizedQuota).matches()) {
            throw new IllegalArgumentException("access lease quota id is invalid");
        }
        return leases.values().stream()
                .filter(lease -> lease.subjectId().equals(subjectId))
                .filter(lease -> lease.state() == LeaseState.ACTIVE)
                .filter(lease -> lease.expiresAt().isAfter(now))
                .filter(lease -> scopeMatches(lease.scope(), context))
                .map(Lease::quotas)
                .map(values -> values.get(normalizedQuota))
                .filter(Objects::nonNull)
                .max(Long::compareTo);
    }

    public synchronized int reconcile() {
        expire(now());
        int pending = 0;
        for (Lease current : List.copyOf(leases.values())) {
            if (!current.pendingProviderCleanup()) {
                continue;
            }
            pending++;
            if (current.provider().equals("internal")) {
                Lease replacement = replace(
                        current,
                        current.state(),
                        current.expiresAt(),
                        current.reason(),
                        false,
                        now());
                leases.put(replacement.id(), replacement);
                changed();
                pending--;
            }
        }
        return pending;
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
                    "Access lease persistence failed",
                    exception);
            return ActionResult.failure(
                    ActionResult.ReasonCode.STORAGE_ERROR,
                    "access lease storage could not be committed");
        }
    }

    @Override
    public synchronized LoadResult load(Path managedRoot) {
        path = Objects.requireNonNull(managedRoot, "managedRoot")
                .resolve("access-leases.json")
                .toAbsolutePath()
                .normalize();
        profiles.clear();
        leases.clear();
        history.clear();
        boolean existed = Files.exists(path, java.nio.file.LinkOption.NOFOLLOW_LINKS);
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(null);
        if (document == null) {
            state = existed ? RepositoryState.RECOVERY : RepositoryState.MISSING;
            revision = 1L;
            flushedRevision = 1L;
            return new LoadResult(state, existed ? "access lease storage unavailable" : "new access lease repository");
        }
        try {
            Snapshot snapshot = GSON.fromJson(document.data(), Snapshot.class);
            if (snapshot == null
                    || snapshot.revision() < 1L
                    || snapshot.profiles().size() > MAXIMUM_PROFILES
                    || snapshot.leases().size() > MAXIMUM_LEASES
                    || snapshot.history().size() > MAXIMUM_HISTORY) {
                throw new IllegalStateException("access lease snapshot is outside bounds");
            }
            for (Profile profile : snapshot.profiles()) {
                validate(profile);
                if (profiles.putIfAbsent(profile.id(), profile) != null) {
                    throw new IllegalStateException("duplicate access lease profile");
                }
            }
            for (Lease lease : snapshot.leases()) {
                validate(lease);
                if (leases.putIfAbsent(lease.id(), lease) != null) {
                    throw new IllegalStateException("duplicate access lease");
                }
            }
            for (HistoryEntry entry : snapshot.history()) {
                validate(entry);
                history.add(entry);
            }
            revision = snapshot.revision();
            flushedRevision = document.migrated() ? Math.max(0L, revision - 1L) : revision;
            state = RepositoryState.READY;
            expire(now());
            return new LoadResult(state, "loaded access lease data");
        } catch (RuntimeException exception) {
            profiles.clear();
            leases.clear();
            history.clear();
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
        expire(now());
        Snapshot snapshot = new Snapshot(
                revision,
                new ArrayList<>(profiles.values()),
                new ArrayList<>(leases.values()),
                new ArrayList<>(history));
        StorageService.write(path, domain(), SCHEMA_VERSION, GSON.toJsonTree(snapshot), document, Set.of("/leases"));
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

    private void expire(Instant now) {
        for (Lease current : List.copyOf(leases.values())) {
            if (current.state().authoritative() && !current.expiresAt().isAfter(now)) {
                Lease replacement = replace(
                        current,
                        LeaseState.EXPIRED,
                        current.expiresAt(),
                        current.reason(),
                        !current.provider().equals("internal"),
                        now);
                leases.put(replacement.id(), replacement);
                history(replacement, new UUID(0L, 0L), "expired", current.state(), LeaseState.EXPIRED);
                changed();
            }
        }
    }

    private Checkpoint checkpoint() {
        return new Checkpoint(
                new LinkedHashMap<>(profiles),
                new LinkedHashMap<>(leases),
                new ArrayList<>(history),
                revision,
                flushedRevision,
                state,
                document);
    }

    private void restore(Checkpoint checkpoint) {
        profiles.clear();
        profiles.putAll(checkpoint.profiles());
        leases.clear();
        leases.putAll(checkpoint.leases());
        history.clear();
        history.addAll(checkpoint.history());
        revision = checkpoint.revision();
        flushedRevision = checkpoint.flushedRevision();
        state = checkpoint.state();
        document = checkpoint.document();
    }

    private void history(
            Lease lease,
            UUID actorId,
            String action,
            LeaseState before,
            LeaseState after
    ) {
        if (history.size() >= MAXIMUM_HISTORY) {
            history.removeFirst();
        }
        history.add(new HistoryEntry(
                UUID.randomUUID(),
                lease.id(),
                lease.subjectId(),
                Objects.requireNonNull(actorId, "actorId"),
                bounded(action, 64, false),
                before,
                after,
                now(),
                lease.revision()));
    }

    private void changed() {
        revision = Math.addExact(revision, 1L);
    }

    private Instant now() {
        return clock.instant();
    }

    private void writable() {
        if (state == RepositoryState.RECOVERY
                || state == RepositoryState.UNSUPPORTED
                || state == RepositoryState.ERROR
                || state == RepositoryState.CLOSED) {
            throw new IllegalStateException("access lease storage is unavailable");
        }
    }

    private Set<String> permissions(Set<String> values) {
        if (values == null || values.isEmpty() || values.size() > MAXIMUM_PROFILE_PERMISSIONS) {
            throw new IllegalArgumentException("access lease profile permission count is outside bounds");
        }
        Set<String> result = new LinkedHashSet<>();
        for (String value : values) {
            String normalized = permission(value, true);
            if (!result.add(normalized)) {
                throw new IllegalArgumentException("access lease profile contains a duplicate permission");
            }
        }
        return Set.copyOf(result);
    }

    private String permission(String value, boolean validateManifest) {
        String normalized = Objects.requireNonNull(value, "permission").strip().toLowerCase(Locale.ROOT);
        if (!PERMISSION.matcher(normalized).matches()) {
            throw new IllegalArgumentException("access lease permission is invalid");
        }
        if (validateManifest && !permissionExists.test(normalized)) {
            throw new IllegalArgumentException("access lease permission is not registered");
        }
        if (validateManifest && forbidden(normalized)) {
            throw new IllegalArgumentException("access lease permission is not delegable");
        }
        return normalized;
    }

    private static boolean forbidden(String permission) {
        return FORBIDDEN_EXACT.contains(permission)
                || FORBIDDEN_PREFIXES.stream().anyMatch(permission::startsWith)
                || permission.contains(".bypass")
                || permission.endsWith(".hierarchy.override")
                || permission.endsWith(".exemption.override");
    }

    private static Map<String, Long> quotas(Map<String, Long> values) {
        if (values == null || values.size() > 64) {
            throw new IllegalArgumentException("access lease profile quota count is outside bounds");
        }
        Map<String, Long> result = new LinkedHashMap<>();
        for (Map.Entry<String, Long> entry : values.entrySet()) {
            String key = bounded(entry.getKey(), 64, false).toLowerCase(Locale.ROOT);
            long value = Objects.requireNonNull(entry.getValue(), "quota value");
            if (!QUOTA_ID.matcher(key).matches() || value < 0L || value > 1_000_000_000L) {
                throw new IllegalArgumentException("access lease quota is outside bounds");
            }
            result.put(key, value);
        }
        return Map.copyOf(result);
    }

    private static String profileId(String value) {
        String normalized = Objects.requireNonNull(value, "profileId").strip().toLowerCase(Locale.ROOT);
        if (!ID.matcher(normalized).matches()) {
            throw new IllegalArgumentException("access lease profile id is invalid");
        }
        return normalized;
    }

    private static String provider(String value) {
        String normalized = bounded(value, 64, false).toLowerCase(Locale.ROOT);
        if (!normalized.equals("internal")) {
            throw new IllegalArgumentException("external access lease providers are not enabled");
        }
        return normalized;
    }

    private static void validateDuration(Duration duration) {
        if (duration == null
                || duration.isZero()
                || duration.isNegative()
                || duration.compareTo(MAXIMUM_LEASE_DURATION) > 0) {
            throw new IllegalArgumentException("access lease duration is outside bounds");
        }
    }

    private void validate(Profile profile) {
        if (!profile.id().equals(profileId(profile.id()))
                || profile.revision() < 1L
                || profile.maximumDurationSeconds() < 1L
                || profile.maximumDurationSeconds() > MAXIMUM_LEASE_DURATION.toSeconds()
                || profile.permissions().size() > MAXIMUM_PROFILE_PERMISSIONS) {
            throw new IllegalArgumentException("access lease profile is invalid");
        }
        permissions(profile.permissions());
        quotas(profile.quotas());
        validateScope(profile.scope());
        Objects.requireNonNull(profile.publishedBy(), "publishedBy");
        Objects.requireNonNull(profile.publishedAt(), "publishedAt");
    }

    private void validate(Lease lease) {
        Objects.requireNonNull(lease.id(), "id");
        profileId(lease.profileId());
        if (lease.profileRevision() < 1L
                || lease.revision() < 1L
                || lease.expiresAt().isBefore(lease.startsAt())
                || lease.updatedAt().isBefore(lease.startsAt())
                || lease.permissions().size() > MAXIMUM_PROFILE_PERMISSIONS) {
            throw new IllegalArgumentException("access lease is invalid");
        }
        permissions(lease.permissions());
        quotas(lease.quotas());
        Objects.requireNonNull(lease.subjectId(), "subjectId");
        Objects.requireNonNull(lease.issuerId(), "issuerId");
        Objects.requireNonNull(lease.state(), "state");
        bounded(lease.reason(), 512, false);
        validateScope(lease.scope());
        provider(lease.provider());
        bounded(lease.providerRevision(), 128, true);
    }

    private static void validate(HistoryEntry entry) {
        Objects.requireNonNull(entry.id(), "id");
        Objects.requireNonNull(entry.leaseId(), "leaseId");
        Objects.requireNonNull(entry.subjectId(), "subjectId");
        Objects.requireNonNull(entry.actorId(), "actorId");
        bounded(entry.action(), 64, false);
        Objects.requireNonNull(entry.before(), "before");
        Objects.requireNonNull(entry.after(), "after");
        Objects.requireNonNull(entry.occurredAt(), "occurredAt");
        if (entry.leaseRevision() < 1L) {
            throw new IllegalArgumentException("access lease history is invalid");
        }
    }

    private static void validateScope(Scope scope) {
        Objects.requireNonNull(scope.kind(), "scope kind");
        String value = bounded(scope.value(), 128, scope.kind() == ScopeKind.GLOBAL);
        if (scope.kind() == ScopeKind.GLOBAL && !value.isBlank()) {
            throw new IllegalArgumentException("global access lease scope cannot have a value");
        }
        if (scope.kind() != ScopeKind.GLOBAL && value.isBlank()) {
            throw new IllegalArgumentException("scoped access lease requires a value");
        }
    }

    private static boolean scopeMatches(Scope scope, ScopeContext context) {
        if (scope.kind() == ScopeKind.GLOBAL) {
            return true;
        }
        if (context == null) {
            return false;
        }
        return switch (scope.kind()) {
            case GLOBAL -> true;
            case WORLD -> scope.value().equals(context.world());
            case DIMENSION -> scope.value().equals(context.dimension());
            case SERVER_PHASE -> scope.value().equals(context.serverPhase());
            case EVENT -> context.events().contains(scope.value());
        };
    }

    private static boolean allowedTransition(LeaseState current, LeaseState replacement) {
        return switch (current) {
            case ACTIVE -> replacement == LeaseState.SUSPENDED || replacement == LeaseState.REVOKED;
            case SUSPENDED -> replacement == LeaseState.ACTIVE || replacement == LeaseState.REVOKED;
            case EXPIRED -> replacement == LeaseState.REVOKED;
            case REVOKED -> false;
        };
    }

    private static Lease replace(
            Lease current,
            LeaseState state,
            Instant expiresAt,
            String reason,
            boolean pendingProviderCleanup,
            Instant now
    ) {
        return new Lease(
                current.id(),
                current.profileId(),
                current.profileRevision(),
                current.permissions(),
                current.quotas(),
                current.subjectId(),
                current.issuerId(),
                current.startsAt(),
                expiresAt,
                state,
                reason,
                current.scope(),
                current.provider(),
                current.providerRevision(),
                pendingProviderCleanup,
                Math.addExact(current.revision(), 1L),
                now);
    }

    private static String bounded(String value, int maximum, boolean allowBlank) {
        String normalized = Objects.requireNonNullElse(value, "").strip();
        if ((!allowBlank && normalized.isBlank())
                || normalized.length() > maximum
                || normalized.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("access lease text is outside bounds");
        }
        return normalized;
    }

    public enum LeaseDecision {
        GRANTED,
        ABSTAIN
    }

    public enum LeaseState {
        ACTIVE,
        SUSPENDED,
        EXPIRED,
        REVOKED;

        public boolean authoritative() {
            return this == ACTIVE || this == SUSPENDED;
        }
    }

    public enum ScopeKind {
        GLOBAL,
        WORLD,
        DIMENSION,
        SERVER_PHASE,
        EVENT
    }

    public record Scope(ScopeKind kind, String value) {
        public Scope {
            Objects.requireNonNull(kind, "kind");
            value = Objects.requireNonNullElse(value, "").strip().toLowerCase(Locale.ROOT);
        }

        public static Scope global() {
            return new Scope(ScopeKind.GLOBAL, "");
        }
    }

    public record ScopeContext(
            String world,
            String dimension,
            String serverPhase,
            Set<String> events
    ) {
        public ScopeContext {
            world = Objects.requireNonNullElse(world, "").strip().toLowerCase(Locale.ROOT);
            dimension = Objects.requireNonNullElse(dimension, "").strip().toLowerCase(Locale.ROOT);
            serverPhase = Objects.requireNonNullElse(serverPhase, "").strip().toLowerCase(Locale.ROOT);
            events = Set.copyOf(Objects.requireNonNullElse(events, Set.of()));
        }

        public static ScopeContext offline() {
            return new ScopeContext("", "", "", Set.of());
        }
    }

    public record Profile(
            String id,
            long revision,
            Set<String> permissions,
            Map<String, Long> quotas,
            long maximumDurationSeconds,
            boolean protectedProfile,
            boolean separationRequired,
            Scope scope,
            boolean active,
            UUID publishedBy,
            Instant publishedAt
    ) {
        public Profile {
            permissions = Set.copyOf(Objects.requireNonNullElse(permissions, Set.of()));
            quotas = Map.copyOf(Objects.requireNonNullElse(quotas, Map.of()));
        }
    }

    public record Lease(
            UUID id,
            String profileId,
            long profileRevision,
            Set<String> permissions,
            Map<String, Long> quotas,
            UUID subjectId,
            UUID issuerId,
            Instant startsAt,
            Instant expiresAt,
            LeaseState state,
            String reason,
            Scope scope,
            String provider,
            String providerRevision,
            boolean pendingProviderCleanup,
            long revision,
            Instant updatedAt
    ) {
        public Lease {
            permissions = Set.copyOf(Objects.requireNonNullElse(permissions, Set.of()));
            quotas = Map.copyOf(Objects.requireNonNullElse(quotas, Map.of()));
        }
    }

    public record HistoryEntry(
            UUID id,
            UUID leaseId,
            UUID subjectId,
            UUID actorId,
            String action,
            LeaseState before,
            LeaseState after,
            Instant occurredAt,
            long leaseRevision
    ) {
    }

    private record Snapshot(
            long revision,
            List<Profile> profiles,
            List<Lease> leases,
            List<HistoryEntry> history
    ) {
        private Snapshot {
            profiles = profiles == null ? List.of() : List.copyOf(profiles);
            leases = leases == null ? List.of() : List.copyOf(leases);
            history = history == null ? List.of() : List.copyOf(history);
        }
    }

    private record Checkpoint(
            Map<String, Profile> profiles,
            Map<UUID, Lease> leases,
            List<HistoryEntry> history,
            long revision,
            long flushedRevision,
            RepositoryState state,
            StorageService.Document document
    ) {
    }
}
