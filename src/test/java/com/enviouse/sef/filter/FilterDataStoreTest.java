package com.enviouse.sef.filter;

import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilterDataStoreTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void missingStorageStartsEmptyWithoutWritingAFile() {
        FilterDataStore store = new FilterDataStore();

        StorageRepository.LoadResult result = store.load(temporaryDirectory);

        assertEquals(StorageRepository.RepositoryState.MISSING, result.state());
        assertTrue(store.getFilters().isEmpty());
        assertFalse(Files.exists(temporaryDirectory.resolve("filters.json")));
    }

    @Test
    void mutationsPersistAndReload() {
        FilterDataStore store = new FilterDataStore();
        store.load(temporaryDirectory);
        store.put("spoiler", new FilterDataStore.FilterRecord("secret", "hidden", false));

        FilterDataStore reloaded = new FilterDataStore();
        assertEquals(StorageRepository.RepositoryState.READY, reloaded.load(temporaryDirectory).state());
        assertEquals("hidden", reloaded.getFilters().get("spoiler").replacement());
    }

    @Test
    void corruptReloadPreservesLastValidSnapshotAndRejectsWrites() throws Exception {
        FilterDataStore store = new FilterDataStore();
        store.load(temporaryDirectory);
        store.put("spoiler", new FilterDataStore.FilterRecord("secret", "hidden", false));
        Files.writeString(temporaryDirectory.resolve("filters.json"), "{");

        StorageRepository.LoadResult result = store.load(temporaryDirectory);

        assertEquals(StorageRepository.RepositoryState.RECOVERY, result.state());
        assertTrue(store.getFilters().containsKey("spoiler"));
        assertThrows(
                IllegalStateException.class,
                () -> store.put("other", new FilterDataStore.FilterRecord("a", "b", true)));
    }

    @Test
    void partiallyInvalidDocumentNeverPublishes() throws Exception {
        FilterDataStore store = new FilterDataStore();
        store.load(temporaryDirectory);
        store.put("known", new FilterDataStore.FilterRecord("known", "safe", true));

        JsonObject data = new JsonObject();
        JsonObject valid = new JsonObject();
        valid.addProperty("wordToFilter", "new");
        valid.addProperty("replacement", "replacement");
        valid.addProperty("caseSensitive", true);
        data.add("valid", valid);
        JsonObject invalid = new JsonObject();
        invalid.addProperty("wordToFilter", "");
        invalid.addProperty("replacement", "replacement");
        invalid.addProperty("caseSensitive", false);
        data.add("invalid", invalid);
        StorageService.write(
                temporaryDirectory.resolve("filters.json"),
                "filters",
                FilterDataStore.SCHEMA_VERSION,
                data,
                null);

        assertEquals(
                StorageRepository.RepositoryState.RECOVERY,
                store.load(temporaryDirectory).state());
        assertEquals(1, store.getFilters().size());
        assertTrue(store.getFilters().containsKey("known"));
    }

    @Test
    void unsupportedSchemaIsDistinctFromMissingStorage() throws Exception {
        Path path = temporaryDirectory.resolve("filters.json");
        JsonObject envelope = new JsonObject();
        envelope.addProperty("domain", "filters");
        envelope.addProperty("schemaVersion", 999);
        envelope.add("data", new JsonArray());
        Files.writeString(path, envelope.toString());

        FilterDataStore store = new FilterDataStore();

        assertEquals(
                StorageRepository.RepositoryState.UNSUPPORTED,
                store.load(temporaryDirectory).state());
        assertThrows(
                IllegalStateException.class,
                () -> store.put("blocked", new FilterDataStore.FilterRecord("a", "b", true)));
    }
}
