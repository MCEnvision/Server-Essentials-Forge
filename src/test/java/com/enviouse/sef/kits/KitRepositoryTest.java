package com.enviouse.sef.kits;

import com.enviouse.sef.storage.repository.StorageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KitRepositoryTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void kitPolicyAndUsesPersistWithInstantFields() throws Exception {
        UUID actor = UUID.randomUUID();
        UUID player = UUID.randomUUID();
        KitRepository first = new KitRepository(8, 8, 4);
        first.load(temporaryDirectory);
        KitRepository.Kit kit = first.put(
                "starter",
                List.of("{id:\"minecraft:stone\",count:1}"),
                Duration.ofMinutes(5),
                false,
                actor);
        kit = first.updatePolicy("starter", 120L, true, "sef.kit.vip", "vip starter");
        first.recordUse(player, kit, Instant.now());

        first.flush();

        KitRepository second = new KitRepository(8, 8, 4);
        assertEquals(StorageRepository.RepositoryState.READY, second.load(temporaryDirectory).state());
        KitRepository.Kit loaded = second.kit("starter").orElseThrow();
        assertEquals("sef.kit.vip", loaded.permission());
        assertEquals("vip starter", loaded.displayName());
        assertEquals(120L, loaded.cooldownSeconds());
        assertFalse(second.availability(player, loaded, Instant.now()).available());
        assertFalse(second.dirty());
    }

    @Test
    void perPlayerUseRecordsHaveAHardBound() {
        UUID actor = UUID.randomUUID();
        UUID player = UUID.randomUUID();
        KitRepository repository = new KitRepository(4, 4, 2);
        repository.load(temporaryDirectory);
        KitRepository.Kit first = repository.put("first", List.of("one"), Duration.ZERO, false, actor);
        KitRepository.Kit second = repository.put("second", List.of("two"), Duration.ZERO, false, actor);
        KitRepository.Kit third = repository.put("third", List.of("three"), Duration.ZERO, false, actor);
        repository.recordUse(player, first, Instant.now());
        repository.recordUse(player, second, Instant.now());

        assertThrows(IllegalStateException.class, () ->
                repository.recordUse(player, third, Instant.now()));
    }

    @Test
    void invalidDynamicPermissionIsRejected() {
        UUID actor = UUID.randomUUID();
        KitRepository repository = new KitRepository(4, 4);
        repository.load(temporaryDirectory);
        repository.put("starter", List.of("one"), Duration.ZERO, false, actor);

        assertThrows(IllegalArgumentException.class, () ->
                repository.updatePolicy("starter", null, null, "bad permission value", null));
    }

    @Test
    void persistedPerPlayerUseLimitIsEnforcedOnLoad() throws Exception {
        UUID actor = UUID.randomUUID();
        UUID player = UUID.randomUUID();
        KitRepository writer = new KitRepository(4, 4, 3);
        writer.load(temporaryDirectory);
        for (String id : List.of("first", "second", "third")) {
            KitRepository.Kit kit = writer.put(id, List.of(id), Duration.ZERO, false, actor);
            writer.recordUse(player, kit, Instant.now());
        }
        writer.flush();

        KitRepository reader = new KitRepository(4, 4, 2);

        assertEquals(StorageRepository.RepositoryState.RECOVERY, reader.load(temporaryDirectory).state());
    }

    @Test
    void oneTimeAndCooldownPoliciesAreEnforcedAtTheRepositoryBoundary() {
        UUID actor = UUID.randomUUID();
        UUID player = UUID.randomUUID();
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        KitRepository repository = new KitRepository(4, 4);
        repository.load(temporaryDirectory);
        KitRepository.Kit oneTime =
                repository.put("once", List.of("one"), Duration.ZERO, true, actor);
        KitRepository.Kit cooldown =
                repository.put("daily", List.of("two"), Duration.ofHours(1), false, actor);

        repository.recordUse(player, oneTime, now);
        repository.recordUse(player, cooldown, now);

        assertThrows(IllegalStateException.class, () ->
                repository.recordUse(player, oneTime, now.plusSeconds(1)));
        assertThrows(IllegalStateException.class, () ->
                repository.recordUse(player, cooldown, now.plusSeconds(3599)));
        repository.recordUse(player, cooldown, now.plusSeconds(3600));
    }

    @Test
    void staleAndDeletedKitDefinitionsCannotCreateUseRecords() {
        UUID actor = UUID.randomUUID();
        UUID player = UUID.randomUUID();
        KitRepository repository = new KitRepository(4, 4);
        repository.load(temporaryDirectory);
        KitRepository.Kit stale =
                repository.put("starter", List.of("one"), Duration.ZERO, false, actor);

        repository.updatePolicy("starter", 60L, null, null, null);

        assertThrows(IllegalStateException.class, () ->
                repository.recordUse(player, stale, Instant.now()));
        KitRepository.Kit current = repository.kit("starter").orElseThrow();
        repository.delete("starter");
        assertThrows(IllegalStateException.class, () ->
                repository.recordUse(player, current, Instant.now()));
        assertEquals(0, repository.validateAll().useRecords());
    }
}
