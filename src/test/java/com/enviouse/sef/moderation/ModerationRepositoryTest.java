package com.enviouse.sef.moderation;

import com.enviouse.sef.storage.repository.StorageRepository;
import com.enviouse.sef.teleport.SavedLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModerationRepositoryTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void moderationStatePersistsWithInstantFields() throws Exception {
        UUID actor = UUID.randomUUID();
        UUID player = UUID.randomUUID();
        SavedLocation jail = new SavedLocation("minecraft:overworld", 10, 64, 10, 0, 0);
        SavedLocation release = new SavedLocation("minecraft:overworld", 20, 70, 20, 0, 0);
        Instant expiry = Instant.now().plusSeconds(600);
        ModerationRepository first = new ModerationRepository();
        first.load(temporaryDirectory);
        first.setJail("spawn_jail", jail, actor);
        first.sentence(player, "spawn_jail", expiry, "test sentence", actor, release);
        first.applyControl(player, ModerationRepository.ControlType.MUTE, expiry, "test mute", actor);
        first.warn(player, "test warning", actor);

        first.flush();

        ModerationRepository second = new ModerationRepository();
        assertEquals(StorageRepository.RepositoryState.READY, second.load(temporaryDirectory).state());
        assertEquals(release, second.sentence(player).orElseThrow().releaseLocation());
        assertTrue(second.control(player, ModerationRepository.ControlType.MUTE).isPresent());
        assertEquals(1, second.warnings(player).size());
        assertFalse(second.dirty());
    }

    @Test
    void expiredSentencesAreReturnedBeforeRemoval() {
        UUID actor = UUID.randomUUID();
        UUID player = UUID.randomUUID();
        SavedLocation location = new SavedLocation("minecraft:overworld", 1, 64, 1, 0, 0);
        ModerationRepository repository = new ModerationRepository();
        repository.load(temporaryDirectory);
        repository.setJail("jail", location, actor);
        Instant expiry = Instant.now().plusSeconds(1);
        repository.sentence(
                player,
                "jail",
                expiry,
                "expired",
                actor,
                location);

        var expired = repository.takeExpiredSentences(expiry.plusSeconds(1));

        assertEquals(1, expired.size());
        assertEquals(location, expired.getFirst().releaseLocation());
        assertTrue(repository.sentence(player).isEmpty());
        assertTrue(repository.takeExpiredSentences(Instant.now()).isEmpty());
    }
}
