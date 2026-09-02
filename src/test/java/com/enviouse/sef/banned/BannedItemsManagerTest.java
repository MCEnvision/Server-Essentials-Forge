package com.enviouse.sef.banned;

import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BannedItemsManagerTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void missingStorageDoesNotCreateAnEmptyEnforcementFile() {
        BannedItemsManager manager = new BannedItemsManager();

        assertEquals(
                StorageRepository.RepositoryState.MISSING,
                manager.load(temporaryDirectory).state());
        assertFalse(Files.exists(temporaryDirectory.resolve("banned_items.json")));
    }

    @Test
    void bansPersistAndReload() {
        BannedItemsManager manager = new BannedItemsManager();
        manager.load(temporaryDirectory);
        assertTrue(manager.addBan("minecraft:bedrock", "protected", -1L, "Console", false));

        BannedItemsManager reloaded = new BannedItemsManager();
        assertEquals(StorageRepository.RepositoryState.READY, reloaded.load(temporaryDirectory).state());
        assertTrue(reloaded.getPatterns().contains("minecraft:bedrock"));
    }

    @Test
    void corruptReloadPreservesLastValidSnapshotAndRejectsMutations() throws Exception {
        BannedItemsManager manager = new BannedItemsManager();
        manager.load(temporaryDirectory);
        manager.addBan("minecraft:bedrock", "protected", -1L, "Console", false);
        Files.writeString(temporaryDirectory.resolve("banned_items.json"), "{");

        assertEquals(
                StorageRepository.RepositoryState.RECOVERY,
                manager.load(temporaryDirectory).state());
        assertTrue(manager.getPatterns().contains("minecraft:bedrock"));
        assertThrows(
                IllegalStateException.class,
                () -> manager.addBan("minecraft:barrier", "", -1L, "Console", false));
    }

    @Test
    void invalidBypassDoesNotPublishAnyPartOfTheDocument() throws Exception {
        BannedItemsManager manager = new BannedItemsManager();
        manager.load(temporaryDirectory);
        manager.addBan("minecraft:bedrock", "protected", -1L, "Console", false);

        JsonObject data = validData("minecraft:barrier");
        data.getAsJsonArray("bypassed").add("not a uuid");
        StorageService.write(
                temporaryDirectory.resolve("banned_items.json"),
                "banned items",
                2,
                data,
                null,
                Set.of("/entries"));

        assertEquals(
                StorageRepository.RepositoryState.RECOVERY,
                manager.load(temporaryDirectory).state());
        assertEquals(Set.of("minecraft:bedrock"), manager.getPatterns());
    }

    @Test
    void legacyArrayMigratesAfterFullValidation() throws Exception {
        Path path = temporaryDirectory.resolve("banned_items.json");
        Files.writeString(path, "[\"minecraft:bedrock\"]");
        BannedItemsManager manager = new BannedItemsManager();

        assertEquals(StorageRepository.RepositoryState.READY, manager.load(temporaryDirectory).state());
        assertTrue(manager.getPatterns().contains("minecraft:bedrock"));
        assertEquals(
                2,
                StorageService.read(path, "banned items", 2).orElseThrow().schemaVersion());
    }

    @Test
    void unsupportedSchemaFailsClosedAndRejectsWrites() throws Exception {
        Path path = temporaryDirectory.resolve("banned_items.json");
        JsonObject envelope = new JsonObject();
        envelope.addProperty("domain", "banned items");
        envelope.addProperty("schemaVersion", 999);
        envelope.add("data", new JsonObject());
        Files.writeString(path, envelope.toString());
        BannedItemsManager manager = new BannedItemsManager();

        assertEquals(
                StorageRepository.RepositoryState.UNSUPPORTED,
                manager.load(temporaryDirectory).state());
        assertFalse(manager.available());
        assertThrows(
                IllegalStateException.class,
                () -> manager.addBan("minecraft:barrier", "", -1L, "Console", false));
    }

    @Test
    void invalidPatternsAndDurationsAreRejectedBeforeMutation() {
        BannedItemsManager manager = new BannedItemsManager();
        manager.load(temporaryDirectory);

        assertThrows(
                IllegalArgumentException.class,
                () -> manager.addBan("not valid", "", -1L, "Console", false));
        assertThrows(
                IllegalArgumentException.class,
                () -> manager.addBan("minecraft:barrier", "", 0L, "Console", false));
        assertTrue(manager.getEntries().isEmpty());
    }

    private static JsonObject validData(String pattern) {
        JsonObject data = new JsonObject();
        JsonObject entries = new JsonObject();
        JsonObject entry = new JsonObject();
        entry.addProperty("pattern", pattern);
        entry.addProperty("reason", "test");
        entry.addProperty("bannedBy", "Console");
        entry.addProperty("bannedAtMillis", System.currentTimeMillis());
        entry.addProperty("durationMs", -1L);
        entry.addProperty("announce", false);
        entries.add(pattern, entry);
        data.add("entries", entries);
        data.add("bypassed", new JsonArray());
        data.add("excepted", new JsonArray());
        JsonObject settings = new JsonObject();
        settings.addProperty("enabledItems", true);
        settings.addProperty("enabledBlocks", true);
        settings.addProperty("dropOnDestroy", false);
        settings.addProperty("radiusOverride", -1);
        settings.addProperty("intervalOverride", -1);
        data.add("settings", settings);
        return data;
    }
}
