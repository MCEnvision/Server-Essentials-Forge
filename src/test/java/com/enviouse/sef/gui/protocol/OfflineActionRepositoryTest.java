package com.enviouse.sef.gui.protocol;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
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
}
