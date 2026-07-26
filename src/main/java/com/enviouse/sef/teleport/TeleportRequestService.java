package com.enviouse.sef.teleport;

import com.enviouse.sef.kernel.ActionResult;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class TeleportRequestService {
    private static final int MAXIMUM_HISTORY = 512;

    private final Clock clock;
    private final Map<UUID, Request> pending = new LinkedHashMap<>();
    private final Deque<Request> history = new ArrayDeque<>();

    public TeleportRequestService() {
        this(Clock.systemUTC());
    }

    TeleportRequestService(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public synchronized ActionResult<Request> create(
            Type type,
            UUID senderId,
            UUID targetId,
            SavedLocation senderOrigin,
            Duration lifetime,
            int maximumPendingPerPlayer
    ) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(senderId, "senderId");
        Objects.requireNonNull(targetId, "targetId");
        Objects.requireNonNull(senderOrigin, "senderOrigin");
        Objects.requireNonNull(lifetime, "lifetime");
        expire();
        if (senderId.equals(targetId)) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "cannot request yourself");
        }
        if (lifetime.isZero() || lifetime.isNegative() || lifetime.compareTo(Duration.ofHours(1)) > 0) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "request lifetime is outside bounds");
        }
        if (maximumPendingPerPlayer < 1 || maximumPendingPerPlayer > 100) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "request limit is outside bounds");
        }
        long senderPending = pending.values().stream()
                .filter(request -> request.senderId().equals(senderId))
                .count();
        long targetPending = pending.values().stream()
                .filter(request -> request.targetId().equals(targetId))
                .count();
        if (senderPending >= maximumPendingPerPlayer || targetPending >= maximumPendingPerPlayer) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "pending request limit reached");
        }
        boolean duplicate = pending.values().stream().anyMatch(request ->
                request.senderId().equals(senderId)
                        && request.targetId().equals(targetId)
                        && request.type() == type);
        if (duplicate) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "matching request is already pending");
        }
        Instant now = clock.instant();
        Request created = new Request(
                UUID.randomUUID(),
                type,
                senderId,
                targetId,
                now,
                now.plus(lifetime),
                senderOrigin,
                State.PENDING,
                1);
        pending.put(created.id(), created);
        return ActionResult.success(created);
    }

    public synchronized ActionResult<Request> accept(UUID targetId, UUID senderId) {
        return transitionIncoming(targetId, senderId, State.ACCEPTED);
    }

    public synchronized ActionResult<Request> deny(UUID targetId, UUID senderId) {
        return transitionIncoming(targetId, senderId, State.DENIED);
    }

    public synchronized ActionResult<Request> cancel(UUID senderId, UUID targetId) {
        expire();
        List<Request> matches = pending.values().stream()
                .filter(request -> request.senderId().equals(senderId))
                .filter(request -> targetId == null || request.targetId().equals(targetId))
                .filter(request -> request.state() == State.PENDING)
                .toList();
        if (matches.isEmpty()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "outgoing request not found");
        }
        if (matches.size() > 1 && targetId == null) {
            return ActionResult.failure(ActionResult.ReasonCode.AMBIGUOUS, "more than one outgoing request is pending");
        }
        return transition(matches.getFirst(), State.CANCELLED);
    }

    public synchronized ActionResult<Request> invalidate(UUID requestId, State state) {
        if (state != State.INVALIDATED && state != State.FAILED && state != State.COMPLETED) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "invalid terminal state");
        }
        Request request = pending.get(requestId);
        if (request == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "request not found");
        }
        return transition(request, state);
    }

    public synchronized ActionResult<Request> markWarmup(UUID requestId) {
        Request request = pending.get(requestId);
        if (request == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "request not found");
        }
        if (request.state() != State.ACCEPTED) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "request is not accepted");
        }
        return transition(request, State.WARMUP);
    }

    public synchronized List<Request> incoming(UUID targetId) {
        expire();
        return pending.values().stream()
                .filter(request -> request.targetId().equals(targetId))
                .filter(request -> request.state() == State.PENDING)
                .sorted(Comparator.comparing(Request::createdAt))
                .toList();
    }

    public synchronized List<Request> outgoing(UUID senderId) {
        expire();
        return pending.values().stream()
                .filter(request -> request.senderId().equals(senderId))
                .filter(request -> request.state() == State.PENDING)
                .sorted(Comparator.comparing(Request::createdAt))
                .toList();
    }

    public synchronized Optional<Request> request(UUID id) {
        expire();
        return Optional.ofNullable(pending.get(id));
    }

    public synchronized List<Request> history() {
        return List.copyOf(history);
    }

    public synchronized int expire() {
        Instant now = clock.instant();
        List<Request> expired = new ArrayList<>();
        pending.values().stream()
                .filter(request -> !request.expiresAt().isAfter(now))
                .forEach(expired::add);
        expired.forEach(request -> transition(request, State.EXPIRED));
        return expired.size();
    }

    public synchronized void clear() {
        pending.clear();
        history.clear();
    }

    public synchronized int invalidatePlayer(UUID playerId, State state) {
        if (state != State.INVALIDATED && state != State.CANCELLED && state != State.FAILED) {
            throw new IllegalArgumentException("Invalid player cleanup state");
        }
        List<Request> matches = pending.values().stream()
                .filter(request -> request.senderId().equals(playerId) || request.targetId().equals(playerId))
                .toList();
        matches.forEach(request -> transition(request, state));
        return matches.size();
    }

    private ActionResult<Request> transitionIncoming(UUID targetId, UUID senderId, State state) {
        expire();
        List<Request> matches = pending.values().stream()
                .filter(request -> request.targetId().equals(targetId))
                .filter(request -> senderId == null || request.senderId().equals(senderId))
                .filter(request -> request.state() == State.PENDING)
                .toList();
        if (matches.isEmpty()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "incoming request not found");
        }
        if (matches.size() > 1 && senderId == null) {
            return ActionResult.failure(ActionResult.ReasonCode.AMBIGUOUS, "more than one incoming request is pending");
        }
        return transition(matches.getFirst(), state);
    }

    private ActionResult<Request> transition(Request current, State replacement) {
        if (current.state() != State.PENDING
                && current.state() != State.ACCEPTED
                && current.state() != State.WARMUP) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "request is no longer pending");
        }
        Request transitioned = current.withState(replacement);
        if (replacement == State.ACCEPTED || replacement == State.WARMUP) {
            pending.put(current.id(), transitioned);
        } else {
            pending.remove(current.id());
            history.addLast(transitioned);
            while (history.size() > MAXIMUM_HISTORY) {
                history.removeFirst();
            }
        }
        return ActionResult.success(transitioned);
    }

    public record Request(
            UUID id,
            Type type,
            UUID senderId,
            UUID targetId,
            Instant createdAt,
            Instant expiresAt,
            SavedLocation senderOrigin,
            State state,
            long revision
    ) {
        public Request {
            id = Objects.requireNonNull(id, "id");
            type = Objects.requireNonNull(type, "type");
            senderId = Objects.requireNonNull(senderId, "senderId");
            targetId = Objects.requireNonNull(targetId, "targetId");
            createdAt = Objects.requireNonNull(createdAt, "createdAt");
            expiresAt = Objects.requireNonNull(expiresAt, "expiresAt");
            senderOrigin = Objects.requireNonNull(senderOrigin, "senderOrigin");
            state = Objects.requireNonNull(state, "state");
            if (!expiresAt.isAfter(createdAt) || revision < 1) {
                throw new IllegalArgumentException("Teleport request is outside bounds");
            }
        }

        public Request withState(State replacement) {
            return new Request(
                    id,
                    type,
                    senderId,
                    targetId,
                    createdAt,
                    expiresAt,
                    senderOrigin,
                    replacement,
                    revision + 1);
        }
    }

    public enum Type {
        TO_TARGET,
        TARGET_TO_SENDER
    }

    public enum State {
        CREATED,
        PENDING,
        ACCEPTED,
        WARMUP,
        COMPLETED,
        DENIED,
        CANCELLED,
        EXPIRED,
        INVALIDATED,
        FAILED,
        REJECTED
    }
}
