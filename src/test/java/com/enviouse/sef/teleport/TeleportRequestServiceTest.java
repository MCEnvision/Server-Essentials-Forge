package com.enviouse.sef.teleport;

import com.enviouse.sef.kernel.ActionResult;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeleportRequestServiceTest {
    @Test
    void noArgumentAcceptFailsClosedWhenIncomingRequestsAreAmbiguous() {
        MutableClock clock = new MutableClock();
        TeleportRequestService service = new TeleportRequestService(clock);
        UUID target = UUID.randomUUID();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        service.create(
                TeleportRequestService.Type.TO_TARGET,
                first,
                target,
                location(),
                Duration.ofMinutes(1),
                10);
        service.create(
                TeleportRequestService.Type.TARGET_TO_SENDER,
                second,
                target,
                location(),
                Duration.ofMinutes(1),
                10);

        assertEquals(ActionResult.ReasonCode.AMBIGUOUS, service.accept(target, null).reason());
        assertTrue(service.accept(target, first).successful());
        assertEquals(1, service.incoming(target).size());
    }

    @Test
    void acceptedRequestCanCompleteAndCannotBeAcceptedTwice() {
        TeleportRequestService service = new TeleportRequestService();
        UUID sender = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        TeleportRequestService.Request request = service.create(
                TeleportRequestService.Type.TO_TARGET,
                sender,
                target,
                location(),
                Duration.ofMinutes(1),
                10).value();

        TeleportRequestService.Request accepted = service.accept(target, sender).value();
        assertEquals(TeleportRequestService.State.ACCEPTED, accepted.state());
        assertEquals(ActionResult.ReasonCode.NOT_FOUND, service.accept(target, sender).reason());
        assertTrue(service.invalidate(request.id(), TeleportRequestService.State.COMPLETED).successful());
        assertTrue(service.request(request.id()).isEmpty());
        assertEquals(TeleportRequestService.State.COMPLETED, service.history().getFirst().state());
    }

    @Test
    void acceptedRequestRemainsAuthoritativeDuringWarmup() {
        TeleportRequestService service = new TeleportRequestService();
        UUID sender = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        TeleportRequestService.Request request = service.create(
                TeleportRequestService.Type.TO_TARGET,
                sender,
                target,
                location(),
                Duration.ofMinutes(1),
                10).value();

        assertTrue(service.accept(target, sender).successful());
        TeleportRequestService.Request warming = service.markWarmup(request.id()).value();
        assertEquals(TeleportRequestService.State.WARMUP, warming.state());
        assertEquals(ActionResult.ReasonCode.CONFLICT, service.markWarmup(request.id()).reason());
        assertTrue(service.invalidate(request.id(), TeleportRequestService.State.COMPLETED).successful());
        assertEquals(TeleportRequestService.State.COMPLETED, service.history().getFirst().state());
    }

    @Test
    void expiryAndParticipantLogoutInvalidateRequestsDeterministically() {
        MutableClock clock = new MutableClock();
        TeleportRequestService service = new TeleportRequestService(clock);
        UUID sender = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        service.create(
                TeleportRequestService.Type.TO_TARGET,
                sender,
                target,
                location(),
                Duration.ofSeconds(30),
                10);
        clock.advance(Duration.ofSeconds(31));

        assertEquals(1, service.expire());
        assertEquals(TeleportRequestService.State.EXPIRED, service.history().getFirst().state());

        service.create(
                TeleportRequestService.Type.TO_TARGET,
                sender,
                target,
                location(),
                Duration.ofSeconds(30),
                10);
        assertEquals(1, service.invalidatePlayer(sender, TeleportRequestService.State.INVALIDATED));
        assertEquals(TeleportRequestService.State.INVALIDATED, service.history().getLast().state());
    }

    private static SavedLocation location() {
        return new SavedLocation("minecraft:overworld", 0, 64, 0, 0, 0);
    }

    private static final class MutableClock extends Clock {
        private Instant instant = Instant.parse("2026-01-01T00:00:00Z");

        private void advance(Duration duration) {
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
