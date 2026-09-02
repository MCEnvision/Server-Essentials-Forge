package com.enviouse.sef.control;

import com.enviouse.sef.kernel.ActionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ApprovalRepositoryTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void separationApprovalIsImmutableAndOneUse() {
        MutableClock clock = new MutableClock(Instant.parse("2026-07-27T00:00:00Z"));
        ApprovalRepository repository = new ApprovalRepository(clock);
        UUID requester = UUID.randomUUID();
        UUID approver = UUID.randomUUID();
        String hash = ApprovalRepository.payloadHash(Map.of("target", "one", "duration", "60"));

        ActionResult<ApprovalRepository.ApprovalRequest> created = repository.create(
                requester,
                "sef:test.action",
                hash,
                "test action for one",
                Duration.ofMinutes(5),
                true,
                "test");
        assertTrue(created.successful());
        assertFalse(repository.approve(
                created.value().id(),
                created.value().revision(),
                requester,
                "").successful());

        ActionResult<ApprovalRepository.ApprovalRequest> approved = repository.approve(
                created.value().id(),
                created.value().revision(),
                approver,
                "reviewed");
        assertTrue(approved.successful());
        assertFalse(repository.consume(
                created.value().id(),
                requester,
                "sef:test.action",
                ApprovalRepository.payloadHash(Map.of("target", "two"))).successful());

        ActionResult<ApprovalRepository.ApprovalRequest> executed = repository.consume(
                created.value().id(),
                requester,
                "sef:test.action",
                hash);
        assertTrue(executed.successful());
        assertEquals(ApprovalRepository.ApprovalState.EXECUTED, executed.value().state());
        assertFalse(repository.consume(created.value().id(), requester, "sef:test.action", hash).successful());
    }

    @Test
    void expiryAndCompensationAreBounded() {
        MutableClock clock = new MutableClock(Instant.parse("2026-07-27T00:00:00Z"));
        ApprovalRepository repository = new ApprovalRepository(clock);
        UUID requester = UUID.randomUUID();
        UUID approver = UUID.randomUUID();
        String hash = ApprovalRepository.payloadHash(Map.of("operation", "grant"));

        ApprovalRepository.ApprovalRequest request = repository.create(
                requester,
                "sef:test.grant",
                hash,
                "grant preview",
                Duration.ofSeconds(30),
                true,
                "test").value();
        ApprovalRepository.ApprovalRequest approved = repository.approve(
                request.id(),
                request.revision(),
                approver,
                "").value();
        assertTrue(repository.consume(request.id(), requester, "sef:test.grant", hash).successful());
        assertTrue(repository.restoreApproved(request.id(), requester, "downstream failed").successful());

        clock.advance(Duration.ofSeconds(31));
        assertEquals(
                ApprovalRepository.ApprovalState.EXPIRED,
                repository.find(request.id()).orElseThrow().state());
        assertFalse(repository.consume(request.id(), requester, "sef:test.grant", hash).successful());
    }

    @Test
    void recordsAndHistorySurviveRestart() throws Exception {
        ApprovalRepository repository = new ApprovalRepository();
        repository.load(temporaryDirectory);
        UUID requester = UUID.randomUUID();
        UUID approver = UUID.randomUUID();
        String hash = ApprovalRepository.payloadHash(Map.of("operation", "restart"));

        ApprovalRepository.ApprovalRequest request = repository.commit(() -> repository.create(
                requester,
                "sef:test.restart",
                hash,
                "restart preview",
                Duration.ofHours(1),
                true,
                "test")).value();
        assertTrue(repository.commit(() -> repository.approve(
                request.id(),
                request.revision(),
                approver,
                "approved")).successful());

        ApprovalRepository reloaded = new ApprovalRepository();
        assertEquals(
                com.enviouse.sef.storage.repository.StorageRepository.RepositoryState.READY,
                reloaded.load(temporaryDirectory).state());
        assertEquals(
                ApprovalRepository.ApprovalState.APPROVED,
                reloaded.find(request.id()).orElseThrow().state());
        assertEquals(2, reloaded.history(request.id()).size());
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

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
