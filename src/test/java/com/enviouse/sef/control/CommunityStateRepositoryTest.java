package com.enviouse.sef.control;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommunityStateRepositoryTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void entriesPersistAndUpsertWithOptimisticRemoval() throws Exception {
        CommunityStateRepository repository = new CommunityStateRepository();
        repository.load(temporaryDirectory);
        UUID owner = UUID.randomUUID();
        UUID subject = UUID.randomUUID();

        var first = repository.put("friend", owner, subject, subject.toString(), "friend", null);
        var second = repository.put("friend", owner, subject, subject.toString(), "trusted", null);

        assertEquals(first.id(), second.id());
        assertEquals(first.revision() + 1L, second.revision());
        assertFalse(repository.compareAndRemove("friend", owner, subject.toString(), first.revision()));
        repository.flush();

        CommunityStateRepository loaded = new CommunityStateRepository();
        loaded.load(temporaryDirectory);
        assertEquals("trusted", loaded.find("friend", owner, subject.toString()).orElseThrow().value());
        assertTrue(loaded.compareAndRemove("friend", owner, subject.toString(), second.revision()));
    }

    @Test
    void expiredEntriesArePrunedAndCapacityKeysRemainDistinct() {
        CommunityStateRepository repository = new CommunityStateRepository();
        repository.load(temporaryDirectory);
        UUID owner = UUID.randomUUID();

        repository.put("poll_ballot", owner, null, "poll-one", "yes", Instant.now().minusSeconds(1L));
        repository.put("poll_ballot", owner, null, "poll-two", "no", null);

        assertTrue(repository.find("poll_ballot", owner, "poll-one").isEmpty());
        assertEquals(1, repository.entries("poll_ballot", owner).size());
        assertEquals(1L, repository.count("poll_ballot", "poll-two"));
    }

    @Test
    void atomicWritesCommitEveryEntryWithOneRepositoryRevision() {
        CommunityStateRepository repository = new CommunityStateRepository();
        repository.load(temporaryDirectory);
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        long before = repository.revision();

        repository.putAtomically(List.of(
                new CommunityStateRepository.Write(
                        "friend", first, second, second.toString(), "trusted", null),
                new CommunityStateRepository.Write(
                        "friend", second, first, first.toString(), "trusted", null)));

        assertEquals(before + 1L, repository.revision());
        assertEquals(
                "trusted",
                repository.find("friend", first, second.toString()).orElseThrow().value());
        assertEquals(
                "trusted",
                repository.find("friend", second, first.toString()).orElseThrow().value());
    }

    @Test
    void atomicWritesRejectWholeBatchBeforeMutation() {
        CommunityStateRepository repository = new CommunityStateRepository();
        repository.load(temporaryDirectory);
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        long before = repository.revision();

        assertThrows(IllegalArgumentException.class, () -> repository.putAtomically(List.of(
                new CommunityStateRepository.Write(
                        "friend", first, second, second.toString(), "trusted", null),
                new CommunityStateRepository.Write(
                        "friend", second, first, first.toString(), "x".repeat(4097), null))));

        assertEquals(before, repository.revision());
        assertTrue(repository.find("friend", first, second.toString()).isEmpty());
        assertTrue(repository.find("friend", second, first.toString()).isEmpty());
    }

    @Test
    void conditionalMutationRejectsWholeBatchWhenRevisionChanges() {
        CommunityStateRepository repository = new CommunityStateRepository();
        repository.load(temporaryDirectory);
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        var request = repository.put(
                "friend_request", second, first, first.toString(), "pending", null);
        long before = repository.revision();

        CommunityStateRepository.MutationResult result = repository.mutateAtomically(List.of(
                new CommunityStateRepository.CompareAndRemove(
                        "friend_request", second, first.toString(), request.revision() + 1L),
                new CommunityStateRepository.Write(
                        "friend", first, second, second.toString(), "friend", null),
                new CommunityStateRepository.Write(
                        "friend", second, first, first.toString(), "friend", null)));

        assertFalse(result.successful());
        assertEquals(before, repository.revision());
        assertTrue(repository.find("friend_request", second, first.toString()).isPresent());
        assertTrue(repository.find("friend", first, second.toString()).isEmpty());
        assertTrue(repository.find("friend", second, first.toString()).isEmpty());
    }
}
