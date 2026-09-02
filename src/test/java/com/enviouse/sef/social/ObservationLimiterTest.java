package com.enviouse.sef.social;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObservationLimiterTest {
    @Test
    void duplicateEventIdsAreRejectedUntilTheyExpire() {
        ObservationLimiter limiter = new ObservationLimiter(4, Duration.ofSeconds(5));
        UUID event = UUID.randomUUID();
        Instant now = Instant.parse("2026-01-01T00:00:00Z");

        assertTrue(limiter.acceptEvent(event, now));
        assertFalse(limiter.acceptEvent(event, now.plusSeconds(1)));
        assertTrue(limiter.acceptEvent(event, now.plusSeconds(6)));
    }

    @Test
    void observerRateLimitResetsOnTheNextSecond() {
        ObservationLimiter limiter = new ObservationLimiter(4, Duration.ofSeconds(5));
        UUID observer = UUID.randomUUID();
        Instant now = Instant.parse("2026-01-01T00:00:00Z");

        assertTrue(limiter.acceptObserver(observer, now, 2));
        assertTrue(limiter.acceptObserver(observer, now.plusMillis(100), 2));
        assertFalse(limiter.acceptObserver(observer, now.plusMillis(200), 2));
        assertTrue(limiter.acceptObserver(observer, now.plusSeconds(1), 2));
    }

    @Test
    void eventMemoryIsBoundedWithoutRejectingNewIds() {
        ObservationLimiter limiter = new ObservationLimiter(2, Duration.ofMinutes(1));
        Instant now = Instant.parse("2026-01-01T00:00:00Z");

        assertTrue(limiter.acceptEvent(UUID.randomUUID(), now));
        assertTrue(limiter.acceptEvent(UUID.randomUUID(), now));
        assertTrue(limiter.acceptEvent(UUID.randomUUID(), now));
    }
}
