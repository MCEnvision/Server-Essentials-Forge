package com.enviouse.sef.gui.protocol;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OfflineActionRepositoryTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void onlyExplicitlySupportedActionsCanUsePersistentOfflineExecution() {
        assertTrue(GuiWorkflowService.supportsOfflineQueue("sef:item.give.others"));
        assertFalse(GuiWorkflowService.supportsOfflineQueue("sef:social.message"));
        assertFalse(GuiWorkflowService.supportsOfflineQueue("minecraft:op"));
    }

    @Test
    void persistsTypedQueuedActionsAndResolution() throws Exception {
        OfflineActionRepository repository = new OfflineActionRepository();
        repository.load(temporaryDirectory);
        UUID actorId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        Instant now = Instant.parse("2026-07-27T18:00:00Z");
        OfflineActionRepository.QueuedAction queued = repository.enqueue(
                actorId,
                targetId,
                "sef:test.give",
                "variant_1",
                "targets",
                Map.of("targets", "OfflinePlayer", "item", "minecraft:stone"),
                now,
                Duration.ofDays(7L));

        assertEquals(1, repository.pendingReady(now).size());
        repository.flush();

        OfflineActionRepository restored = new OfflineActionRepository();
        assertEquals(
                com.enviouse.sef.storage.repository.StorageRepository.RepositoryState.READY,
                restored.load(temporaryDirectory).state());
        assertEquals(queued.id(), restored.pendingReady(now).getFirst().id());

        restored.resolve(
                queued.id(),
                OfflineActionRepository.ActionState.COMPLETED,
                "completed",
                now.plusSeconds(5L));
        assertTrue(restored.pendingReady(now.plusSeconds(5L)).isEmpty());
        assertEquals(
                OfflineActionRepository.ActionState.COMPLETED,
                restored.entries().getFirst().state());
    }

    @Test
    void expiresPendingActionsWithoutExecutingThem() {
        OfflineActionRepository repository = new OfflineActionRepository();
        repository.load(temporaryDirectory);
        Instant now = Instant.parse("2026-07-27T18:00:00Z");
        repository.enqueue(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "sef:test.give",
                "variant_1",
                "targets",
                Map.of("targets", "OfflinePlayer"),
                now,
                Duration.ofSeconds(10L));

        assertTrue(repository.pendingReady(now.plusSeconds(11L)).isEmpty());
        assertEquals(
                OfflineActionRepository.ActionState.EXPIRED,
                repository.entries().getFirst().state());
    }

    @Test
    void selectsOnlyOnlineTargetsWithinTheExecutionBudget() {
        OfflineActionRepository repository = new OfflineActionRepository();
        repository.load(temporaryDirectory);
        Instant now = Instant.parse("2026-07-27T18:00:00Z");
        UUID actorId = UUID.randomUUID();
        UUID onlineTarget = UUID.randomUUID();
        UUID offlineTarget = UUID.randomUUID();
        repository.enqueue(
                actorId,
                offlineTarget,
                "sef:test.give",
                "variant_1",
                "targets",
                Map.of("targets", "OfflinePlayer"),
                now,
                Duration.ofDays(1L));
        OfflineActionRepository.QueuedAction ready = repository.enqueue(
                actorId,
                onlineTarget,
                "sef:test.give",
                "variant_1",
                "targets",
                Map.of("targets", "OnlinePlayer"),
                now.plusMillis(1L),
                Duration.ofDays(1L));

        assertEquals(
                java.util.List.of(ready.id()),
                repository.pendingReady(
                                now.plusSeconds(1L),
                                Set.of(onlineTarget),
                                1)
                        .stream()
                        .map(OfflineActionRepository.QueuedAction::id)
                        .toList());
        assertTrue(repository.pendingReady(
                now.plusSeconds(1L),
                Set.of(),
                64).isEmpty());
    }

    @Test
    void durableClaimPreventsTheSameOperationFromBeingSelectedTwice() throws Exception {
        OfflineActionRepository repository = new OfflineActionRepository();
        repository.load(temporaryDirectory);
        Instant now = Instant.parse("2026-07-27T18:00:00Z");
        UUID targetId = UUID.randomUUID();
        OfflineActionRepository.QueuedAction action = repository.enqueue(
                UUID.randomUUID(),
                targetId,
                "sef:test.give",
                "variant_1",
                "targets",
                Map.of("targets", "OfflinePlayer"),
                now,
                Duration.ofDays(1L));

        OfflineActionRepository.QueuedAction claimed =
                repository.claimAndFlush(action.id(), now.plusSeconds(1L));

        assertEquals(OfflineActionRepository.ActionState.CLAIMED, claimed.state());
        assertEquals("sef:offline:" + action.id(), claimed.idempotencyKey());
        assertTrue(repository.pendingReady(
                now.plusSeconds(2L),
                Set.of(targetId),
                64).isEmpty());
    }

    @Test
    void restartRetriesAClaimButNeverRetriesAnExecutingOperation() throws Exception {
        Instant now = Instant.parse("2026-07-27T18:00:00Z");

        Path claimedRoot = temporaryDirectory.resolve("claimed");
        OfflineActionRepository claimedRepository = new OfflineActionRepository();
        claimedRepository.load(claimedRoot);
        OfflineActionRepository.QueuedAction claimedAction = claimedRepository.enqueue(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "sef:test.give",
                "variant_1",
                "targets",
                Map.of("targets", "OfflinePlayer"),
                now,
                Duration.ofDays(1L));
        claimedRepository.claimAndFlush(claimedAction.id(), now.plusSeconds(1L));

        OfflineActionRepository claimedReplacement = new OfflineActionRepository();
        claimedReplacement.load(claimedRoot);
        assertEquals(
                OfflineActionRepository.ActionState.PENDING,
                claimedReplacement.entries().getFirst().state());

        Path executingRoot = temporaryDirectory.resolve("executing");
        OfflineActionRepository executingRepository = new OfflineActionRepository();
        executingRepository.load(executingRoot);
        OfflineActionRepository.QueuedAction executingAction = executingRepository.enqueue(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "sef:test.give",
                "variant_1",
                "targets",
                Map.of("targets", "OfflinePlayer"),
                now,
                Duration.ofDays(1L));
        executingRepository.claimAndFlush(executingAction.id(), now.plusSeconds(1L));
        executingRepository.beginAndFlush(executingAction.id(), now.plusSeconds(2L));

        OfflineActionRepository executingReplacement = new OfflineActionRepository();
        executingReplacement.load(executingRoot);
        assertEquals(
                OfflineActionRepository.ActionState.OUTCOME_UNKNOWN,
                executingReplacement.entries().getFirst().state());
        assertTrue(executingReplacement.pendingReady(now.plusSeconds(3L)).isEmpty());
    }

    @Test
    void terminalOutcomeKeepsADurableTargetNotificationUntilAcknowledged() throws Exception {
        OfflineActionRepository repository = new OfflineActionRepository();
        repository.load(temporaryDirectory);
        Instant now = Instant.parse("2026-07-27T18:00:00Z");
        UUID targetId = UUID.randomUUID();
        OfflineActionRepository.QueuedAction action = repository.enqueue(
                UUID.randomUUID(),
                targetId,
                "sef:test.give",
                "variant_1",
                "targets",
                Map.of("targets", "OfflinePlayer"),
                now,
                Duration.ofDays(1L));
        repository.claimAndFlush(action.id(), now.plusSeconds(1L));
        repository.beginAndFlush(action.id(), now.plusSeconds(2L));
        repository.resolveAndFlush(
                action.id(),
                OfflineActionRepository.ActionState.SUCCEEDED,
                "completed",
                now.plusSeconds(3L));

        assertEquals(
                java.util.List.of(action.id()),
                repository.pendingNotifications(Set.of(targetId), 64)
                        .stream()
                        .map(OfflineActionRepository.QueuedAction::id)
                        .toList());
        repository.markNotificationDeliveredAndFlush(action.id(), now.plusSeconds(4L));
        assertTrue(repository.pendingNotifications(Set.of(targetId), 64).isEmpty());
    }
}
