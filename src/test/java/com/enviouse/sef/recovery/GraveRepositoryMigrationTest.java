package com.enviouse.sef.recovery;

import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GraveRepositoryMigrationTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void schemaOneWithoutClaimJournalMigratesToSchemaTwo() throws Exception {
        Path path = temporaryDirectory.resolve("graves.json");
        JsonObject legacy = new JsonObject();
        legacy.addProperty("revision", 1L);
        legacy.add("graves", new JsonArray());
        StorageService.write(path, "graves", 1, legacy, null, Set.of());

        GraveRepository repository = new GraveRepository();
        StorageRepository.LoadResult result = repository.load(temporaryDirectory);

        assertEquals(StorageRepository.RepositoryState.READY, result.state(), result.detail());
        assertTrue(repository.dirty());
        repository.flush();
        assertEquals(
                GraveRepository.SCHEMA_VERSION,
                StorageService.read(path, "graves", GraveRepository.SCHEMA_VERSION)
                        .orElseThrow()
                        .schemaVersion());
    }
}
