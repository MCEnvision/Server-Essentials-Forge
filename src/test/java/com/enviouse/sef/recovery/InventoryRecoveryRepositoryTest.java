package com.enviouse.sef.recovery;

import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class InventoryRecoveryRepositoryTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void emptyVersionedSnapshotLoadsReadyWithoutCreatingRecoveryState() throws Exception {
        JsonObject data = new JsonObject();
        data.addProperty("revision", 1L);
        data.add("players", new JsonArray());
        StorageService.write(
                temporaryDirectory.resolve("inventory-recovery.json"),
                "inventory_recovery",
                InventoryRecoveryRepository.SCHEMA_VERSION,
                data,
                null);

        InventoryRecoveryRepository repository = new InventoryRecoveryRepository();
        StorageRepository.LoadResult result = repository.load(temporaryDirectory);
        assertEquals(StorageRepository.RepositoryState.READY, result.state(), result.detail());
        assertEquals(0, repository.count());
        assertFalse(repository.dirty());
    }

    @Test
    void invalidRevisionEntersRecoveryInsteadOfAcceptingAnEmptySnapshot() throws Exception {
        JsonObject data = new JsonObject();
        data.addProperty("revision", 0L);
        data.add("players", new JsonArray());
        StorageService.write(
                temporaryDirectory.resolve("inventory-recovery.json"),
                "inventory_recovery",
                InventoryRecoveryRepository.SCHEMA_VERSION,
                data,
                null);

        InventoryRecoveryRepository repository = new InventoryRecoveryRepository();
        assertEquals(StorageRepository.RepositoryState.RECOVERY, repository.load(temporaryDirectory).state());
        assertEquals(0, repository.count());
    }
}
