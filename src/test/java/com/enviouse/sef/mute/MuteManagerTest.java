package com.enviouse.sef.mute;

import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.repository.StorageRepository;
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

class MuteManagerTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void missingStorageDoesNotCreateAnEmptyEnforcementFile() {
        MuteManager manager = new MuteManager();

        assertEquals(
                StorageRepository.RepositoryState.MISSING,
                manager.load(temporaryDirectory).state());
        assertFalse(Files.exists(temporaryDirectory.resolve("mutes.json")));
    }

    @Test
    void corruptReloadPreservesTheLastValidMuteAndRejectsMutations() throws Exception {
        UUID playerId = UUID.randomUUID();
        writeMute(playerId, System.currentTimeMillis(), 1_200L, 2);
        MuteManager manager = new MuteManager();
        assertEquals(StorageRepository.RepositoryState.READY, manager.load(temporaryDirectory).state());
        assertTrue(manager.isMuted(playerId));
        Files.writeString(temporaryDirectory.resolve("mutes.json"), "{");

        assertEquals(
                StorageRepository.RepositoryState.RECOVERY,
                manager.load(temporaryDirectory).state());
        assertTrue(manager.isMuted(playerId));
        assertThrows(
                IllegalStateException.class,
                () -> manager.unmutePlayer(playerId, "Console", null));
    }

    @Test
    void legacyTickDurationMigratesToWallClockAndExpiresAcrossRestart() throws Exception {
        UUID playerId = UUID.randomUUID();
        writeMute(playerId, System.currentTimeMillis() - 5_000L, 20L, 1);
        MuteManager manager = new MuteManager();

        assertEquals(StorageRepository.RepositoryState.READY, manager.load(temporaryDirectory).state());
        assertFalse(manager.isMuted(playerId));
        assertTrue(manager.getAllMutes().isEmpty());
        assertEquals(
                2,
                StorageService.read(temporaryDirectory.resolve("mutes.json"), "mutes", 2)
                        .orElseThrow()
                        .schemaVersion());
    }

    @Test
    void partiallyInvalidDocumentDoesNotReplaceAValidSnapshot() throws Exception {
        UUID playerId = UUID.randomUUID();
        writeMute(playerId, System.currentTimeMillis(), 1_200L, 2);
        MuteManager manager = new MuteManager();
        manager.load(temporaryDirectory);

        JsonObject data = muteData(playerId, System.currentTimeMillis(), 1_200L, true);
        JsonObject invalid = muteEntry(UUID.randomUUID(), System.currentTimeMillis(), 1_200L, true);
        invalid.addProperty("playerUUID", "not a uuid");
        data.add(UUID.randomUUID().toString(), invalid);
        StorageService.write(
                temporaryDirectory.resolve("mutes.json"),
                "mutes",
                2,
                data,
                null,
                Set.of(""));

        assertEquals(
                StorageRepository.RepositoryState.RECOVERY,
                manager.load(temporaryDirectory).state());
        assertTrue(manager.isMuted(playerId));
        assertEquals(1, manager.getAllMutes().size());
    }

    @Test
    void unsupportedSchemaFailsClosed() throws Exception {
        Path path = temporaryDirectory.resolve("mutes.json");
        JsonObject envelope = new JsonObject();
        envelope.addProperty("domain", "mutes");
        envelope.addProperty("schemaVersion", 999);
        envelope.add("data", new JsonObject());
        Files.writeString(path, envelope.toString());
        UUID playerId = UUID.randomUUID();
        MuteManager manager = new MuteManager();

        assertEquals(
                StorageRepository.RepositoryState.UNSUPPORTED,
                manager.load(temporaryDirectory).state());
        assertTrue(manager.isMuted(playerId));
        assertThrows(
                IllegalStateException.class,
                () -> manager.unmutePlayer(playerId, "Console", null));
    }

    @Test
    void zeroAndExcessiveDurationsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> MuteManager.parseDuration("0s"));
        assertThrows(IllegalArgumentException.class, () -> MuteManager.parseDuration("10000d"));
    }

    private void writeMute(UUID playerId, long mutedAt, long durationTicks, int schemaVersion)
            throws Exception {
        StorageService.write(
                temporaryDirectory.resolve("mutes.json"),
                "mutes",
                schemaVersion,
                muteData(playerId, mutedAt, durationTicks, schemaVersion >= 2),
                null,
                Set.of(""));
    }

    private static JsonObject muteData(
            UUID playerId,
            long mutedAt,
            long durationTicks,
            boolean includeExpiry
    ) {
        JsonObject data = new JsonObject();
        data.add(
                playerId.toString(),
                muteEntry(playerId, mutedAt, durationTicks, includeExpiry));
        return data;
    }

    private static JsonObject muteEntry(
            UUID playerId,
            long mutedAt,
            long durationTicks,
            boolean includeExpiry
    ) {
        JsonObject entry = new JsonObject();
        entry.addProperty("playerUUID", playerId.toString());
        entry.addProperty("playerName", "Player");
        entry.addProperty("adminName", "Console");
        entry.addProperty("reason", "test");
        entry.addProperty("remainingTicks", durationTicks);
        entry.addProperty("originalDurationTicks", durationTicks);
        entry.addProperty("mutedAtMillis", mutedAt);
        if (includeExpiry) {
            entry.addProperty("expiresAtEpochMillis", mutedAt + durationTicks * 50L);
        }
        return entry;
    }
}
