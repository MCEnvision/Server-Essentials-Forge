package com.enviouse.sef.storage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageServiceTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void dynamicMapsKeepRecordExtensionsWithoutRestoringDeletedRecords() throws Exception {
        Path path = temporaryDirectory.resolve("records.json");
        JsonObject first = new JsonObject();
        JsonObject retained = new JsonObject();
        retained.addProperty("known", "before");
        retained.addProperty("future", "preserved");
        first.add("retained", retained);
        first.add("deleted", new JsonObject());
        StorageService.write(path, "test records", 1, first, null, Set.of(""));

        StorageService.Document loaded = StorageService.read(path, "test records", 1).orElseThrow();
        JsonObject second = new JsonObject();
        JsonObject changed = new JsonObject();
        changed.addProperty("known", "after");
        second.add("retained", changed);
        StorageService.write(path, "test records", 1, second, loaded, Set.of(""));

        JsonObject result = StorageService.read(path, "test records", 1)
                .orElseThrow()
                .data()
                .getAsJsonObject();
        assertFalse(result.has("deleted"));
        assertEquals("after", result.getAsJsonObject("retained").get("known").getAsString());
        assertEquals("preserved", result.getAsJsonObject("retained").get("future").getAsString());
    }

    @Test
    void fixedObjectsKeepUnknownFields() throws Exception {
        Path path = temporaryDirectory.resolve("settings.json");
        JsonObject first = new JsonObject();
        first.addProperty("known", 1);
        first.addProperty("future", true);
        StorageService.write(path, "test settings", 1, first, null);

        StorageService.Document loaded = StorageService.read(path, "test settings", 1).orElseThrow();
        JsonObject second = new JsonObject();
        second.addProperty("known", 2);
        StorageService.write(path, "test settings", 1, second, loaded);

        JsonObject result = StorageService.read(path, "test settings", 1)
                .orElseThrow()
                .data()
                .getAsJsonObject();
        assertEquals(2, result.get("known").getAsInt());
        assertTrue(result.get("future").getAsBoolean());
    }

    @Test
    void legacyDocumentsAreBackedUpAndJournaledBeforeMigration() throws Exception {
        Path path = temporaryDirectory.resolve("legacy.json");
        Files.writeString(path, "{\"entry\":1}");

        StorageService.Document document = StorageService.read(path, "legacy test", 1).orElseThrow();

        assertTrue(document.migrated());
        assertEquals(0, document.schemaVersion());
        try (var backups = Files.list(temporaryDirectory.resolve(".backups"))) {
            assertEquals(1, backups.count());
        }
        String journal = Files.readString(temporaryDirectory.resolve("migration-journal.jsonl"));
        JsonObject entry = JsonParser.parseString(journal).getAsJsonObject();
        assertEquals("legacy test", entry.get("domain").getAsString());
        assertEquals(0, entry.get("fromVersion").getAsInt());
        assertEquals(1, entry.get("toVersion").getAsInt());
    }
}
