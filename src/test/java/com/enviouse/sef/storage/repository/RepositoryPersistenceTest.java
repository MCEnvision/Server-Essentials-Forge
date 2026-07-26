package com.enviouse.sef.storage.repository;

import com.enviouse.sef.kernel.policy.CooldownService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoryPersistenceTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void shutdownFlushesLocationHistoryAndCoordinatorCanRestart() {
        LocationHistoryRepository repository = new LocationHistoryRepository(10);
        StorageCoordinator coordinator = new StorageCoordinator();
        coordinator.register(repository);
        coordinator.start(temporaryDirectory);
        UUID player = UUID.randomUUID();
        repository.record(player, location(1));

        StorageCoordinator.FlushResult shutdown = coordinator.shutdown();

        assertTrue(shutdown.successful());
        assertTrue(Files.isRegularFile(temporaryDirectory.resolve("location-history.json")));
        assertFalse(repository.dirty());

        coordinator.start(temporaryDirectory);
        assertEquals(1, repository.history(player).size());
        assertTrue(coordinator.shutdown().successful());
    }

    @Test
    void corruptRepositoryIsQuarantinedAndActivatesRecoveryMode() throws Exception {
        Path path = temporaryDirectory.resolve("location-history.json");
        Files.writeString(path, "{broken");
        LocationHistoryRepository repository = new LocationHistoryRepository(10);
        StorageCoordinator coordinator = new StorageCoordinator();
        coordinator.register(repository);

        List<StorageRepository.LoadResult> results = coordinator.start(temporaryDirectory);

        assertEquals(StorageRepository.RepositoryState.RECOVERY, results.getFirst().state());
        assertTrue(coordinator.recoveryMode());
        assertFalse(Files.exists(path));
        assertThrows(IllegalStateException.class, () ->
                repository.record(UUID.randomUUID(), location(1)));
        try (var quarantined = Files.list(temporaryDirectory.resolve(".corrupt"))) {
            assertEquals(1, quarantined.count());
        }
    }

    @Test
    void malformedLocationRecordActivatesRecoveryInsteadOfDroppingData() throws Exception {
        Path path = temporaryDirectory.resolve("location-history.json");
        UUID player = UUID.randomUUID();
        Files.writeString(path, """
                {
                  "domain": "location history",
                  "schemaVersion": 1,
                  "data": {
                    "players": {
                      "%s": [5]
                    }
                  }
                }
                """.formatted(player));
        LocationHistoryRepository repository = new LocationHistoryRepository(10);

        StorageRepository.LoadResult result = repository.load(temporaryDirectory);

        assertEquals(StorageRepository.RepositoryState.RECOVERY, result.state());
        assertTrue(repository.history(player).isEmpty());
        assertThrows(IllegalStateException.class, () -> repository.record(player, location(1)));
    }

    @Test
    void cooldownsPersistAcrossRepositoryInstances() throws Exception {
        UUID player = UUID.randomUUID();
        CooldownService firstService = new CooldownService();
        CooldownRepository first = new CooldownRepository(firstService, Duration.ZERO);
        first.load(temporaryDirectory);
        assertTrue(firstService.tryAcquire(
                player,
                "sef:test",
                Duration.ofMinutes(10),
                false).allowed());
        assertTrue(first.dirty());
        first.flush();
        assertFalse(first.dirty());

        CooldownService secondService = new CooldownService();
        CooldownRepository second = new CooldownRepository(secondService, Duration.ZERO);
        assertEquals(StorageRepository.RepositoryState.READY, second.load(temporaryDirectory).state());
        assertFalse(secondService.inspect(player, "sef:test").allowed());
    }

    @Test
    void malformedCooldownEntryActivatesRecoveryInsteadOfDroppingData() throws Exception {
        Path path = temporaryDirectory.resolve("cooldowns.json");
        Files.writeString(path, """
                {
                  "domain": "command cooldowns",
                  "schemaVersion": 1,
                  "data": {
                    "entries": [5]
                  }
                }
                """);
        CooldownService service = new CooldownService();
        CooldownRepository repository = new CooldownRepository(service, Duration.ZERO);

        StorageRepository.LoadResult result = repository.load(temporaryDirectory);

        assertEquals(StorageRepository.RepositoryState.RECOVERY, result.state());
        assertTrue(service.tryAcquire(
                UUID.randomUUID(),
                "sef:test",
                Duration.ofMinutes(1),
                false).allowed());
        assertThrows(java.io.IOException.class, repository::flush);
    }

    @Test
    void missingRequiredRepositoryCollectionsActivateRecovery() throws Exception {
        Files.writeString(temporaryDirectory.resolve("location-history.json"), """
                {
                  "domain": "location history",
                  "schemaVersion": 1,
                  "data": {}
                }
                """);
        Files.writeString(temporaryDirectory.resolve("cooldowns.json"), """
                {
                  "domain": "command cooldowns",
                  "schemaVersion": 1,
                  "data": {}
                }
                """);

        LocationHistoryRepository locations = new LocationHistoryRepository(10);
        CooldownRepository cooldowns = new CooldownRepository(new CooldownService(), Duration.ZERO);

        assertEquals(StorageRepository.RepositoryState.RECOVERY, locations.load(temporaryDirectory).state());
        assertEquals(StorageRepository.RepositoryState.RECOVERY, cooldowns.load(temporaryDirectory).state());
    }

    @Test
    void concurrentSnapshotsDoNotLoseDirtyStateOrCorruptData() throws Exception {
        LocationHistoryRepository repository = new LocationHistoryRepository(100);
        StorageCoordinator coordinator = new StorageCoordinator();
        coordinator.register(repository);
        coordinator.start(temporaryDirectory);
        UUID player = UUID.randomUUID();
        CountDownLatch start = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(5)) {
            List<Future<?>> writers = new ArrayList<>();
            for (int writer = 0; writer < 4; writer++) {
                int offset = writer * 100;
                writers.add(executor.submit(() -> {
                    start.await();
                    for (int index = 0; index < 100; index++) {
                        repository.record(player, location(offset + index));
                    }
                    return null;
                }));
            }
            Future<?> flusher = executor.submit(() -> {
                start.await();
                for (int index = 0; index < 25; index++) {
                    coordinator.flush();
                }
                return null;
            });
            start.countDown();
            for (Future<?> writer : writers) {
                writer.get();
            }
            flusher.get();
        }
        assertTrue(coordinator.flush().successful());
        assertFalse(repository.dirty());

        LocationHistoryRepository reloaded = new LocationHistoryRepository(100);
        assertEquals(StorageRepository.RepositoryState.READY, reloaded.load(temporaryDirectory).state());
        assertEquals(100, reloaded.history(player).size());
    }

    private static LocationHistoryRepository.LocationRecord location(int value) {
        return new LocationHistoryRepository.LocationRecord(
                "minecraft:overworld",
                value,
                64,
                value,
                0,
                0,
                Instant.ofEpochSecond(value + 1L),
                "test");
    }
}
