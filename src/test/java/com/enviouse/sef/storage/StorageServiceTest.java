package com.enviouse.sef.storage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void migrationJournalFailureLeavesValidSourceOutsideQuarantine() throws Exception {
        Path path = temporaryDirectory.resolve("legacy.json");
        String original = "{\"entry\":1}";
        Files.writeString(path, original);
        Files.createDirectory(temporaryDirectory.resolve("migration-journal.jsonl"));

        assertTrue(StorageService.read(path, "legacy journal failure", 1).isEmpty());

        assertTrue(Files.isRegularFile(path));
        assertEquals(original, Files.readString(path));
        assertFalse(Files.exists(temporaryDirectory.resolve(".corrupt")));
        assertEquals(
                "migration preparation failed",
                StorageService.statuses().stream()
                        .filter(status -> status.path().equals(path.toAbsolutePath().normalize()))
                        .findFirst()
                        .orElseThrow()
                        .state());
    }

    @Test
    void maximumJsonDepthLoadsAndTheNextLevelIsQuarantined() throws Exception {
        Path accepted = temporaryDirectory.resolve("accepted.json");
        Files.writeString(accepted, nestedArray(StorageService.MAX_JSON_DEPTH));

        assertTrue(StorageService.read(accepted, "depth accepted", 1).isPresent());

        Path rejected = temporaryDirectory.resolve("rejected.json");
        Files.writeString(rejected, nestedArray(StorageService.MAX_JSON_DEPTH + 1));

        assertTrue(StorageService.read(rejected, "depth rejected", 1).isEmpty());
        assertFalse(Files.exists(rejected));
        assertEquals("quarantined", status(rejected));
    }

    @Test
    void deeplyNestedJsonCannotExhaustTheStack() throws Exception {
        Path path = temporaryDirectory.resolve("deep.json");
        Files.writeString(path, nestedArray(5_000));

        assertTrue(StorageService.read(path, "deep test", 1).isEmpty());
        assertEquals("quarantined", status(path));
    }

    @Test
    void malformedUtf8AndNonregularPathsAreRejectedSafely() throws Exception {
        Path malformed = temporaryDirectory.resolve("malformed.json");
        Files.write(malformed, new byte[] {(byte) 0xC3, (byte) 0x28});
        assertTrue(StorageService.read(malformed, "utf8 test", 1).isEmpty());
        assertEquals("quarantined", status(malformed));

        Path directory = temporaryDirectory.resolve("directory.json");
        Files.createDirectory(directory);
        assertTrue(StorageService.read(directory, "directory test", 1).isEmpty());
        assertEquals("rejected", status(directory));
        assertTrue(Files.isDirectory(directory));
    }

    @Test
    void symbolicLinkStorageAndExportsCannotReadExternalFiles() throws Exception {
        Path external = temporaryDirectory.resolve("external.txt");
        Files.writeString(external, "external secret");
        Path managed = temporaryDirectory.resolve("managed.json");
        Files.createSymbolicLink(managed, external);

        assertTrue(StorageService.read(managed, "link test", 1).isEmpty());
        assertEquals("rejected", status(managed));
        assertEquals("external secret", Files.readString(external));

        Files.delete(managed);
        JsonObject data = new JsonObject();
        data.addProperty("value", "safe");
        StorageService.write(managed, "export test", 1, data, null);
        StorageService.read(managed, "export test", 1);
        Files.delete(managed);
        Files.createSymbolicLink(managed, external);

        Path exportRoot = temporaryDirectory.resolve("exports");
        assertThrows(
                AtomicFileStore.UnsafeStoragePathException.class,
                () -> StorageService.exportManagedSnapshot(
                        List.of(temporaryDirectory),
                        exportRoot,
                        status -> status.domain().equals("export test")));
        assertFalse(Files.exists(exportRoot.resolve("external.txt")));
    }

    @Test
    void missingCanonicalFileRecoversThePreviousCompleteGeneration() throws Exception {
        Path path = temporaryDirectory.resolve("recover.json");
        JsonObject first = new JsonObject();
        first.addProperty("value", "first");
        StorageService.write(path, "recovery test", 1, first, null);
        AtomicFileStore.write(
                path,
                "{\"domain\":\"recovery test\",\"schemaVersion\":1,\"data\":{\"value\":\"second\"}}"
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8),
                true);
        Files.delete(path);

        JsonObject recovered = StorageService.read(path, "recovery test", 1)
                .orElseThrow()
                .data()
                .getAsJsonObject();

        assertEquals("first", recovered.get("value").getAsString());
    }

    private String status(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        return StorageService.statuses().stream()
                .filter(storeStatus -> storeStatus.path().equals(normalized))
                .findFirst()
                .orElseThrow()
                .state();
    }

    private static String nestedArray(int depth) {
        return "[".repeat(depth) + "0" + "]".repeat(depth);
    }
}
