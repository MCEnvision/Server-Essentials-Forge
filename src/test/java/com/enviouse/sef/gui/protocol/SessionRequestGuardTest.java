package com.enviouse.sef.gui.protocol;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SessionRequestGuardTest {
    @Test
    void rejectsForgedUnauthorizedReplayAndFloodedRequests() {
        MutableClock clock = new MutableClock();
        UUID sessionId = UUID.randomUUID();
        SessionRequestGuard guard = new SessionRequestGuard(
                clock,
                sessionId,
                SefProtocol.Feature.DASHBOARD.flag(),
                true);

        assertEquals(
                SefSessionManager.RequestDecision.NO_SESSION,
                guard.accept(UUID.randomUUID(), 1L, SefProtocol.Feature.DASHBOARD, 2));
        assertEquals(
                SefSessionManager.RequestDecision.NOT_AUTHORIZED,
                guard.accept(sessionId, 1L, SefProtocol.Feature.HOMES, 2));
        assertEquals(
                SefSessionManager.RequestDecision.ACCEPTED,
                guard.accept(sessionId, 1L, SefProtocol.Feature.DASHBOARD, 2));
        assertEquals(
                SefSessionManager.RequestDecision.REPLAY,
                guard.accept(sessionId, 1L, SefProtocol.Feature.DASHBOARD, 2));
        assertEquals(
                SefSessionManager.RequestDecision.ACCEPTED,
                guard.accept(sessionId, 2L, SefProtocol.Feature.DASHBOARD, 2));
        assertEquals(
                SefSessionManager.RequestDecision.RATE_LIMITED,
                guard.accept(sessionId, 3L, SefProtocol.Feature.DASHBOARD, 2));

        clock.advance(Duration.ofSeconds(1));
        assertEquals(
                SefSessionManager.RequestDecision.ACCEPTED,
                guard.accept(sessionId, 3L, SefProtocol.Feature.DASHBOARD, 2));

        guard.update(SefProtocol.Feature.DASHBOARD.flag(), false);
        assertEquals(
                SefSessionManager.RequestDecision.NOT_AUTHORIZED,
                guard.accept(sessionId, 4L, SefProtocol.Feature.DASHBOARD, 2));
    }

    static final class MutableClock extends Clock {
        private Instant instant = Instant.parse("2026-01-01T00:00:00Z");

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
