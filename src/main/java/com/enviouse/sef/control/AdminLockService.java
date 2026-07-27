package com.enviouse.sef.control;

import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.command.CommandDefinition;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class AdminLockService {
    public static final Duration CHALLENGE_DURATION = Duration.ofMinutes(2);
    public static final Duration MAXIMUM_SESSION_DURATION = Duration.ofHours(1);
    private static final int MAXIMUM_SESSIONS = 100_000;
    private static final UUID CONSOLE_ID = new UUID(0L, 0L);

    private final AdminLockRepository repository;
    private final Clock clock;
    private final Map<UUID, Instant> challenges = new LinkedHashMap<>();
    private final Map<UUID, PrivilegedSession> playerSessions = new LinkedHashMap<>();
    private final Map<UUID, BreakGlassSession> breakGlassSessions = new LinkedHashMap<>();

    public AdminLockService(AdminLockRepository repository) {
        this(repository, Clock.systemUTC());
    }

    AdminLockService(AdminLockRepository repository, Clock clock) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public synchronized Status status(UUID subjectId) {
        prune();
        var lock = repository.lock(subjectId).orElse(null);
        return new Status(
                lock != null && lock.locked(),
                lock == null ? "" : lock.reason(),
                lock == null ? 0L : lock.revision(),
                Optional.ofNullable(playerSessions.get(subjectId)));
    }

    public synchronized ActionResult<AdminLockRepository.AccountLock> lock(
            UUID subjectId,
            UUID actorId,
            String reason
    ) {
        ActionResult<AdminLockRepository.AccountLock> result =
                repository.commit(() -> repository.lock(subjectId, actorId, reason));
        if (result.successful()) {
            playerSessions.remove(subjectId);
            challenges.remove(subjectId);
        }
        return result;
    }

    public synchronized ActionResult<Challenge> challenge(UUID subjectId) {
        Objects.requireNonNull(subjectId, "subjectId");
        if (!status(subjectId).locked()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "administrative account is not locked");
        }
        Instant expiresAt = now().plus(CHALLENGE_DURATION);
        challenges.put(subjectId, expiresAt);
        return ActionResult.success(new Challenge(subjectId, expiresAt));
    }

    public synchronized ActionResult<AdminLockRepository.AccountLock> unlock(
            UUID subjectId,
            UUID actorId,
            boolean consoleRecovery
    ) {
        prune();
        if (!consoleRecovery) {
            Instant challenge = challenges.remove(subjectId);
            if (challenge == null || !challenge.isAfter(now())) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.CONFIRMATION_REQUIRED,
                        "run the administrative lock challenge first");
            }
        }
        ActionResult<AdminLockRepository.AccountLock> result = repository.commit(() ->
                repository.unlock(subjectId, actorId, consoleRecovery ? "console recovery" : "local challenge"));
        if (result.successful()) {
            playerSessions.remove(subjectId);
        }
        return result;
    }

    public synchronized ActionResult<PrivilegedSession> openSession(
            UUID subjectId,
            UUID actorId,
            Duration duration,
            String reason
    ) {
        prune();
        if (status(subjectId).locked()) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.POLICY_DENIED,
                    "administrative account is locked");
        }
        if (duration == null
                || duration.isZero()
                || duration.isNegative()
                || duration.compareTo(MAXIMUM_SESSION_DURATION) > 0) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.INVALID_INPUT,
                    "privileged session duration is outside bounds");
        }
        if (!playerSessions.containsKey(subjectId) && playerSessions.size() >= MAXIMUM_SESSIONS) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "privileged session limit reached");
        }
        Set<CommandDefinition.AccessClass> classes = repository.requiredClasses();
        if (classes.isEmpty()) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.POLICY_DENIED,
                    "no action class requires a privileged session");
        }
        PrivilegedSession session = new PrivilegedSession(
                UUID.randomUUID(),
                subjectId,
                actorId,
                classes,
                now(),
                now().plus(duration),
                bounded(reason, 512, false));
        var persisted = repository.commit(() -> repository.event(
                subjectId,
                actorId,
                "session_opened",
                session.id().toString()));
        if (!persisted.successful()) {
            return ActionResult.failure(persisted.reason(), persisted.detail());
        }
        playerSessions.put(subjectId, session);
        return ActionResult.success(session);
    }

    public synchronized ActionResult<PrivilegedSession> closeSession(UUID subjectId, UUID actorId, String reason) {
        prune();
        PrivilegedSession removed = playerSessions.remove(Objects.requireNonNull(subjectId, "subjectId"));
        if (removed == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "privileged session not found");
        }
        var persisted = repository.commit(() -> repository.event(
                subjectId,
                actorId,
                "session_closed",
                bounded(reason, 512, true)));
        if (!persisted.successful()) {
            playerSessions.put(subjectId, removed);
            return ActionResult.failure(persisted.reason(), persisted.detail());
        }
        return ActionResult.success(removed);
    }

    public synchronized int invalidate(UUID subjectId, UUID actorId, String reason) {
        prune();
        List<PrivilegedSession> removed = new ArrayList<>();
        if (subjectId == null) {
            removed.addAll(playerSessions.values());
            playerSessions.clear();
        } else {
            PrivilegedSession session = playerSessions.remove(subjectId);
            if (session != null) {
                removed.add(session);
            }
        }
        for (PrivilegedSession session : removed) {
            var result = repository.commit(() -> repository.event(
                    session.subjectId(),
                    actorId,
                    "session_invalidated",
                    bounded(reason, 512, false)));
            if (!result.successful()) {
                playerSessions.put(session.subjectId(), session);
                return 0;
            }
        }
        return removed.size();
    }

    public synchronized ActionResult<BreakGlassSession> openBreakGlass(
            String profileId,
            Duration duration,
            String incidentId
    ) {
        prune();
        AdminLockRepository.BreakGlassProfile profile = repository.profile(profileId).orElse(null);
        if (profile == null || !profile.active()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "active break glass profile not found");
        }
        if (duration == null
                || duration.isZero()
                || duration.isNegative()
                || duration.getSeconds() > profile.maximumSeconds()) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.POLICY_DENIED,
                    "break glass duration exceeds the profile maximum");
        }
        if (breakGlassSessions.size() >= MAXIMUM_SESSIONS) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "break glass session limit reached");
        }
        BreakGlassSession session = new BreakGlassSession(
                UUID.randomUUID(),
                profile.id(),
                profile.revision(),
                profile.accessClasses(),
                bounded(incidentId, 128, false),
                now(),
                now().plus(duration));
        var persisted = repository.commit(() -> repository.event(
                CONSOLE_ID,
                CONSOLE_ID,
                "break_glass_opened",
                session.id() + " " + session.incidentId()));
        if (!persisted.successful()) {
            return ActionResult.failure(persisted.reason(), persisted.detail());
        }
        breakGlassSessions.put(session.id(), session);
        return ActionResult.success(session);
    }

    public synchronized ActionResult<BreakGlassSession> closeBreakGlass(UUID sessionId) {
        prune();
        BreakGlassSession removed = breakGlassSessions.remove(Objects.requireNonNull(sessionId, "sessionId"));
        if (removed == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "break glass session not found");
        }
        var persisted = repository.commit(() -> repository.event(
                CONSOLE_ID,
                CONSOLE_ID,
                "break_glass_closed",
                removed.id().toString()));
        if (!persisted.successful()) {
            breakGlassSessions.put(removed.id(), removed);
            return ActionResult.failure(persisted.reason(), persisted.detail());
        }
        return ActionResult.success(removed);
    }

    public synchronized List<BreakGlassSession> breakGlassSessions() {
        prune();
        return breakGlassSessions.values().stream()
                .sorted(Comparator.comparing(BreakGlassSession::expiresAt))
                .toList();
    }

    public synchronized ActionResult<Void> authorize(
            UUID subjectId,
            CommandDefinition definition,
            boolean recoveryAction
    ) {
        prune();
        Status status = status(subjectId);
        if (status.locked() && !recoveryAction) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.POLICY_DENIED,
                    "privileged administration is locked for this account");
        }
        if (!recoveryAction && repository.requiredClasses().contains(definition.accessClass())) {
            PrivilegedSession session = playerSessions.get(subjectId);
            if (session == null || !session.accessClasses().contains(definition.accessClass())) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.POLICY_DENIED,
                        "that action requires an active privileged session");
            }
        }
        return ActionResult.success(null);
    }

    public synchronized void logout(UUID subjectId) {
        challenges.remove(subjectId);
        playerSessions.remove(subjectId);
    }

    public synchronized int activeSessionCount() {
        prune();
        return playerSessions.size();
    }

    private void prune() {
        Instant now = now();
        challenges.entrySet().removeIf(entry -> !entry.getValue().isAfter(now));
        List<PrivilegedSession> expired = playerSessions.values().stream()
                .filter(session -> !session.expiresAt().isAfter(now))
                .toList();
        expired.forEach(session -> playerSessions.remove(session.subjectId()));
        breakGlassSessions.values().removeIf(session -> !session.expiresAt().isAfter(now));
    }

    private Instant now() {
        return clock.instant();
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

    public record Status(
            boolean locked,
            String reason,
            long revision,
            Optional<PrivilegedSession> session
    ) {
    }

    public record Challenge(UUID subjectId, Instant expiresAt) {
    }

    public record PrivilegedSession(
            UUID id,
            UUID subjectId,
            UUID openedBy,
            Set<CommandDefinition.AccessClass> accessClasses,
            Instant openedAt,
            Instant expiresAt,
            String reason
    ) {
        public PrivilegedSession {
            accessClasses = Set.copyOf(accessClasses);
        }
    }

    public record BreakGlassSession(
            UUID id,
            String profileId,
            long profileRevision,
            Set<CommandDefinition.AccessClass> accessClasses,
            String incidentId,
            Instant openedAt,
            Instant expiresAt
    ) {
        public BreakGlassSession {
            accessClasses = Set.copyOf(accessClasses);
        }
    }
}
