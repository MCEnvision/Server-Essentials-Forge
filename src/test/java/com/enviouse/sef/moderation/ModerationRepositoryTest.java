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
        ModerationRepository.Sentence prepared =
                first.prepareSentence(player, "spawn_jail", expiry, "test sentence", actor, release);
        first.activateJail(player, prepared.operationId());
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
    void expiredSentencesRemainDurableUntilReleaseCompletes() throws Exception {
        UUID actor = UUID.randomUUID();
        UUID player = UUID.randomUUID();
        SavedLocation location = new SavedLocation("minecraft:overworld", 1, 64, 1, 0, 0);
        ModerationRepository repository = new ModerationRepository();
        repository.load(temporaryDirectory);
        repository.setJail("jail", location, actor);
        Instant expiry = Instant.now().plusSeconds(1);
        ModerationRepository.Sentence prepared = repository.prepareSentence(
                player,
                "jail",
                expiry,
                "expired",
                actor,
                location);
        repository.activateJail(player, prepared.operationId());

        var expired = repository.markExpiredReleasePending(expiry.plusSeconds(1));

        assertEquals(1, expired.size());
        assertEquals(location, expired.getFirst().releaseLocation());
        assertEquals(
                ModerationRepository.SentenceState.RELEASE_PENDING,
                repository.sentence(player).orElseThrow().state());
        assertTrue(repository.takeExpiredSentences(Instant.now()).isEmpty());

        repository.flush();
        ModerationRepository restarted = new ModerationRepository();
        restarted.load(temporaryDirectory);
        ModerationRepository.Sentence pending = restarted.sentence(player).orElseThrow();
        assertEquals(ModerationRepository.SentenceState.RELEASE_PENDING, pending.state());
        restarted.beginRelease(player, pending.operationId());
        restarted.releasePending(player, pending.operationId(), "target is offline");
        restarted.flush();

        ModerationRepository offlineRestart = new ModerationRepository();
        offlineRestart.load(temporaryDirectory);
        ModerationRepository.Sentence retry = offlineRestart.sentence(player).orElseThrow();
        assertEquals(ModerationRepository.SentenceState.RELEASE_PENDING, retry.state());
        assertEquals("target is offline", retry.lastFailure());
        offlineRestart.beginRelease(player, retry.operationId());
        offlineRestart.completeRelease(player, retry.operationId());
        assertTrue(offlineRestart.sentence(player).isEmpty());
        assertEquals(
                ModerationRepository.SentenceState.RELEASED,
                offlineRestart.transition(player).orElseThrow().state());
        assertTrue(offlineRestart.completeRelease(player, retry.operationId()).isEmpty());
    }

    @Test
    void preparedJailSurvivesRestartAndKnownFailureCanRollback() throws Exception {
        UUID actor = UUID.randomUUID();
        UUID player = UUID.randomUUID();
        SavedLocation location = new SavedLocation("minecraft:overworld", 1, 64, 1, 0, 0);
        ModerationRepository first = new ModerationRepository();
        first.load(temporaryDirectory);
        first.setJail("jail", location, actor);
        ModerationRepository.Sentence prepared = first.prepareSentence(
                player,
                "jail",
                null,
                "restart",
                actor,
                location);
        first.flush();

        ModerationRepository restarted = new ModerationRepository();
        restarted.load(temporaryDirectory);
        ModerationRepository.Sentence recovered = restarted.sentence(player).orElseThrow();
        assertEquals(prepared.operationId(), recovered.operationId());
        assertEquals(ModerationRepository.SentenceState.JAILING, recovered.state());

        restarted.outcomeUnknown(
                player,
                recovered.operationId(),
                ModerationRepository.TransitionAction.JAIL,
                "provider failed");
        restarted.flush();
        ModerationRepository unknownRestart = new ModerationRepository();
        unknownRestart.load(temporaryDirectory);
        assertEquals(
                ModerationRepository.SentenceState.OUTCOME_UNKNOWN,
                unknownRestart.sentence(player).orElseThrow().state());
        assertTrue(unknownRestart.rollbackJail(
                player,
                recovered.operationId(),
                "known failure").isPresent());
        assertTrue(unknownRestart.sentence(player).isEmpty());
    }

    @Test
    void missingReleaseLocationIsRetainedForSafeFallback() {
        UUID actor = UUID.randomUUID();
        UUID player = UUID.randomUUID();
        SavedLocation jail = new SavedLocation("minecraft:overworld", 1, 64, 1, 0, 0);
        ModerationRepository repository = new ModerationRepository();
        repository.load(temporaryDirectory);
        repository.setJail("jail", jail, actor);
        ModerationRepository.Sentence prepared = repository.prepareSentence(
                player,
                "jail",
                null,
                "missing release",
                actor,
                null);
        repository.activateJail(player, prepared.operationId());

        ModerationRepository.Sentence pending = repository.prepareRelease(player).orElseThrow();

        assertEquals(ModerationRepository.SentenceState.RELEASE_PENDING, pending.state());
        assertEquals(null, pending.releaseLocation());
        assertTrue(repository.sentence(player).isPresent());
    }
}
