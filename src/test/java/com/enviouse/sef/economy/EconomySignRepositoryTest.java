package com.enviouse.sef.economy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EconomySignRepositoryTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void persistsCreatorSideDefinitionAndRevision() throws Exception {
        EconomySignRepository repository = new EconomySignRepository(16);
        assertEquals(
                com.enviouse.sef.storage.repository.StorageRepository.RepositoryState.MISSING,
                repository.load(temporaryDirectory).state());
        EconomySignRepository.SignKey key =
                new EconomySignRepository.SignKey("minecraft:overworld", 1, 64, 2, true);
        UUID creator = UUID.randomUUID();
        EconomySignParser.Definition initial = definition("[free]", "minecraft:bread", "2", "");

        EconomySignRepository.SignRecord created = repository.put(key, creator, initial);
        assertEquals(1L, created.revision());
        repository.flush();

        EconomySignRepository loaded = new EconomySignRepository(16);
        assertEquals(
                com.enviouse.sef.storage.repository.StorageRepository.RepositoryState.READY,
                loaded.load(temporaryDirectory).state());
        EconomySignRepository.SignRecord record = loaded.find(key).orElseThrow();
        assertEquals(creator, record.creatorId());
        assertEquals(EconomySignParser.SignType.FREE, record.type());
        assertEquals(List.of("minecraft:bread", "2"), record.arguments());

        EconomySignRepository.SignRecord edited =
                loaded.put(key, creator, definition("[free]", "minecraft:bread", "3", ""));
        assertEquals(2L, edited.revision());
        assertEquals(1, loaded.removeAt("minecraft:overworld", 1, 64, 2));
        assertFalse(loaded.find(key).isPresent());
    }

    @Test
    void enforcesCapacityWithoutOverwritingExistingKey() {
        EconomySignRepository repository = new EconomySignRepository(1);
        repository.load(temporaryDirectory);
        UUID creator = UUID.randomUUID();
        EconomySignRepository.SignKey first =
                new EconomySignRepository.SignKey("minecraft:overworld", 1, 64, 2, true);
        repository.put(first, creator, definition("[balance]", "", "", ""));

        boolean rejected = false;
        try {
            repository.put(
                    new EconomySignRepository.SignKey("minecraft:overworld", 2, 64, 2, true),
                    creator,
                    definition("[balance]", "", "", ""));
        } catch (IllegalStateException exception) {
            rejected = true;
        }
        assertTrue(rejected);
        assertEquals(1, repository.size());
        assertEquals(2L, repository.put(first, creator, definition("[disposal]", "", "", "")).revision());
    }

    private static EconomySignParser.Definition definition(
            String first,
            String second,
            String third,
            String fourth
    ) {
        return EconomySignParser.parse(List.of(first, second, third, fourth), 64)
                .definition();
    }
}
