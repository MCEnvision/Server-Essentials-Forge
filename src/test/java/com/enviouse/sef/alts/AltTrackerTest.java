package com.enviouse.sef.alts;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AltTrackerTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void loginMutationFlushesThroughBackgroundWriter() throws Exception {
        AltTracker tracker = new AltTracker();
        tracker.load(temporaryDirectory);
        try {
            tracker.recordLogin(UUID.randomUUID(), "player", "8.8.8.8", false);
            tracker.flush();
        } finally {
            tracker.shutdown();
        }

        Path dataFile = temporaryDirectory.resolve("alt_data.json");
        assertTrue(Files.isRegularFile(dataFile));
        JsonObject envelope = JsonParser.parseString(Files.readString(dataFile)).getAsJsonObject();
        assertEquals("alternate account correlations", envelope.get("domain").getAsString());
        assertTrue(envelope.getAsJsonObject("data").has("8.8.8.8"));
    }

    @Test
    void corruptSaltIsNeverSilentlyReplaced() throws Exception {
        byte[] corrupt = new byte[]{1, 2, 3};
        Path salt = temporaryDirectory.resolve("alt_tracking.salt");
        Files.write(salt, corrupt);
        AltTracker tracker = new AltTracker();
        tracker.load(temporaryDirectory);
        try {
            tracker.recordLogin(UUID.randomUUID(), "player", "8.8.4.4", true);
            tracker.flush();
            assertEquals(0, tracker.addressCount());
        } finally {
            tracker.shutdown();
        }

        assertArrayEquals(corrupt, Files.readAllBytes(salt));
    }

    @Test
    void malformedReloadPreservesLiveSnapshotAndRejectsCollection() throws Exception {
        AltTracker tracker = new AltTracker();
        tracker.load(temporaryDirectory);
        try {
            tracker.recordLogin(UUID.randomUUID(), "player", "1.1.1.1", false);
            tracker.flush();
            Files.writeString(
                    temporaryDirectory.resolve("alt_data.json"),
                    """
                            {"domain":"alternate account correlations","schemaVersion":1,
                            "data":{"1.1.1.1":[{"uuid":"bad","name":"attacker","lastSeen":"2026-01-01T00:00:00Z"}]}}
                            """);

            tracker.load(temporaryDirectory);
            tracker.recordLogin(UUID.randomUUID(), "other", "2.2.2.2", false);

            assertEquals(
                    com.enviouse.sef.storage.repository.StorageRepository.RepositoryState.RECOVERY,
                    tracker.state());
            assertEquals(1, tracker.addressCount());
        } finally {
            tracker.shutdown();
        }
    }
}
