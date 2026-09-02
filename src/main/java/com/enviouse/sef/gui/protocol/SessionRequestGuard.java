package com.enviouse.sef.gui.protocol;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.UUID;

public final class SessionRequestGuard {
    private final Clock clock;
    private final UUID sessionId;
    private final Deque<Instant> accepted = new ArrayDeque<>();
    private long features;
    private boolean authorized;
    private long lastSequence;

    public SessionRequestGuard(
            Clock clock,
            UUID sessionId,
            long features,
            boolean authorized
    ) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
        this.features = features;
        this.authorized = authorized;
    }

    public synchronized SefSessionManager.RequestDecision accept(
            UUID suppliedSessionId,
            long sequence,
            SefProtocol.Feature feature,
            int requestsPerSecond
    ) {
        Objects.requireNonNull(suppliedSessionId, "suppliedSessionId");
        Objects.requireNonNull(feature, "feature");
        if (!sessionId.equals(suppliedSessionId)) {
            return SefSessionManager.RequestDecision.NO_SESSION;
        }
        if (!authorized || !feature.present(features)) {
            return SefSessionManager.RequestDecision.NOT_AUTHORIZED;
        }
        if (sequence <= lastSequence) {
            return SefSessionManager.RequestDecision.REPLAY;
        }
        if (requestsPerSecond < 1 || requestsPerSecond > 1000) {
            throw new IllegalArgumentException("Request rate limit is outside bounds");
        }
        Instant now = clock.instant();
        Instant cutoff = now.minusSeconds(1L);
        while (!accepted.isEmpty() && !accepted.peekFirst().isAfter(cutoff)) {
            accepted.removeFirst();
        }
        if (accepted.size() >= requestsPerSecond) {
            return SefSessionManager.RequestDecision.RATE_LIMITED;
        }
        accepted.addLast(now);
        lastSequence = sequence;
        return SefSessionManager.RequestDecision.ACCEPTED;
    }

    public synchronized void update(long replacementFeatures, boolean replacementAuthorized) {
        features = replacementFeatures;
        authorized = replacementAuthorized;
    }

    public synchronized long lastSequence() {
        return lastSequence;
    }
}
