package com.enviouse.sef.escrow;

import com.enviouse.sef.storage.repository.StorageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EscrowRepositoryTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void journalPersistsValueAndRejectsStaleTransitions() {
        EscrowRepository repository = new EscrowRepository();
        assertEquals(
                StorageRepository.RepositoryState.MISSING,
                repository.load(temporaryDirectory).state());
        Instant now = Instant.now();
        EscrowRepository.EscrowRecord prepared = currencyRecord(now);

        var created = repository.createAndFlush(prepared);
        assertTrue(created.successful(), created.detail());
        EscrowRepository.EscrowRecord held = prepared.transition(
                EscrowRepository.EscrowState.HELD,
                prepared.ownerId(),
                "custody committed",
                now.plusSeconds(1L));
        var transitioned = repository.replaceAndFlush(
                held,
                prepared.revision(),
                Set.of(EscrowRepository.EscrowState.PREPARING));
        assertTrue(transitioned.successful(), transitioned.detail());
        assertFalse(repository.replaceAndFlush(
                held.transition(
                        EscrowRepository.EscrowState.SETTLED,
                        held.beneficiaryId(),
                        "settled",
                        now.plusSeconds(2L)),
                prepared.revision(),
                Set.of(EscrowRepository.EscrowState.PREPARING)).successful());

        EscrowRepository reloaded = new EscrowRepository();
        assertEquals(
                StorageRepository.RepositoryState.READY,
                reloaded.load(temporaryDirectory).state());
        EscrowRepository.EscrowRecord restored = reloaded.find(prepared.id()).orElseThrow();
        assertEquals(EscrowRepository.EscrowState.HELD, restored.state());
        assertEquals(250L, restored.reservedCurrency());
        assertEquals(prepared.itemDigest(), restored.itemDigest());
    }

    @Test
    void activeValueIsNeverPrunedAsOrdinaryHistory() {
        EscrowRepository repository = new EscrowRepository();
        repository.load(temporaryDirectory);
        Instant old = Instant.now().minusSeconds(5_184_000L);
        EscrowRepository.EscrowRecord record = currencyRecord(old);

        assertTrue(repository.createAndFlush(record).successful());

        assertEquals(1, repository.size());
        assertTrue(repository.find(record.id()).isPresent());
    }

    @Test
    void sourceLookupSupportsTypedLostAndFoundDeduplication() {
        EscrowRepository repository = new EscrowRepository();
        repository.load(temporaryDirectory);
        Instant now = Instant.now();
        String source = "grave/" + "a".repeat(96);
        EscrowRepository.EscrowRecord record = new EscrowRepository.EscrowRecord(
                UUID.randomUUID(),
                EscrowRepository.EscrowDomain.LOST_FOUND,
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of(),
                EscrowRepository.itemDigest(List.of()),
                1L,
                0L,
                "",
                "",
                "",
                EscrowRepository.EscrowState.PREPARING,
                null,
                0L,
                null,
                0L,
                null,
                null,
                source,
                now,
                now.plusSeconds(3600L),
                now,
                1L,
                "reservation prepared");

        assertTrue(repository.createAndFlush(record).successful());
        assertEquals(record.id(), repository.findBySource(
                EscrowRepository.EscrowDomain.LOST_FOUND,
                source).orElseThrow().id());
        assertTrue(repository.findBySource(EscrowRepository.EscrowDomain.PARCEL, source).isEmpty());
    }

    private static EscrowRepository.EscrowRecord currencyRecord(Instant now) {
        UUID owner = UUID.randomUUID();
        return new EscrowRepository.EscrowRecord(
                UUID.randomUUID(),
                EscrowRepository.EscrowDomain.PARCEL,
                owner,
                UUID.randomUUID(),
                List.of(),
                EscrowRepository.itemDigest(List.of()),
                250L,
                0L,
                "coins",
                "native",
                "",
                EscrowRepository.EscrowState.PREPARING,
                null,
                0L,
                null,
                0L,
                null,
                null,
                "parcels",
                now,
                now.plusSeconds(604_800L),
                now,
                1L,
                "reservation prepared");
    }
}
