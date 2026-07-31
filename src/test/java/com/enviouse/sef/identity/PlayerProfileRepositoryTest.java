package com.enviouse.sef.identity;

import com.enviouse.sef.config.PlayerData;
import com.enviouse.sef.storage.ImportDiagnostics;
import com.enviouse.sef.storage.repository.StorageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerProfileRepositoryTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void legacyNicknameDataImportsIntoVersionedPlayerProfileStorage() throws Exception {
        UUID player = UUID.randomUUID();
        Path legacy = temporaryDirectory.resolve(PlayerData.legacyPlayerDataFileName);
        Files.writeString(legacy, """
                [PlayerDataEntry]
                UUID: "%s"
                Nickname: "Captain"
                """.formatted(player));
        ImportDiagnostics.clear();
        PlayerProfileRepository repository = new PlayerProfileRepository();

        repository.load(temporaryDirectory.toFile());

        PlayerProfileRepository.Profile profile = repository.find(player).orElseThrow();
        assertEquals("Captain", profile.nickname());
        assertTrue(Files.isRegularFile(temporaryDirectory.resolve(PlayerData.playerDataFileName)));
        assertEquals(ImportDiagnostics.Result.SUCCESS, ImportDiagnostics.snapshot().getLast().result());
        try (var backups = Files.list(temporaryDirectory.resolve(".backups"))) {
            assertEquals(1, backups.count());
        }
    }

    @Test
    void quarantinedProfileDataEntersRecoveryAndCannotBeOverwritten() throws Exception {
        Path profileFile = temporaryDirectory.resolve(PlayerData.playerDataFileName);
        Files.writeString(profileFile, "{broken");
        PlayerProfileRepository repository = new PlayerProfileRepository();

        repository.load(temporaryDirectory.toFile());

        assertEquals(StorageRepository.RepositoryState.RECOVERY, repository.diagnostic().state());
        assertFalse(repository.setNickname(UUID.randomUUID(), "unsafe write"));
        assertFalse(Files.exists(profileFile));
        try (var quarantined = Files.list(temporaryDirectory.resolve(".corrupt"))) {
            assertEquals(1, quarantined.count());
        }
    }

    @Test
    void malformedProfileEntryEntersRecoveryAndCannotBeOverwritten() throws Exception {
        Path profileFile = temporaryDirectory.resolve(PlayerData.playerDataFileName);
        UUID player = UUID.randomUUID();
        String malformed = """
                {
                  "domain": "integrated player identities",
                  "schemaVersion": 1,
                  "data": {
                    "players": {
                      "%s": []
                    }
                  }
                }
                """.formatted(player);
        Files.writeString(profileFile, malformed);
        PlayerProfileRepository repository = new PlayerProfileRepository();

        repository.load(temporaryDirectory.toFile());

        assertEquals(StorageRepository.RepositoryState.RECOVERY, repository.diagnostic().state());
        assertFalse(repository.setNickname(player, "unsafe write"));
        assertEquals(malformed, Files.readString(profileFile));
    }

    @Test
    void malformedProfileCollectionDoesNotPublishValidatedPrefix() throws Exception {
        Path profileFile = temporaryDirectory.resolve(PlayerData.playerDataFileName);
        UUID validPlayer = UUID.randomUUID();
        UUID malformedPlayer = UUID.randomUUID();
        Files.writeString(profileFile, """
                {
                  "domain": "integrated player identities",
                  "schemaVersion": 1,
                  "data": {
                    "players": {
                      "%s": {
                        "username": "EnVy",
                        "nickname": "Captain",
                        "updatedAt": "2026-07-28T00:00:00Z"
                      },
                      "%s": []
                    }
                  }
                }
                """.formatted(validPlayer, malformedPlayer));
        PlayerProfileRepository repository = new PlayerProfileRepository();

        repository.load(temporaryDirectory.toFile());

        assertEquals(StorageRepository.RepositoryState.RECOVERY, repository.diagnostic().state());
        assertTrue(repository.find(validPlayer).isEmpty());
        assertTrue(repository.find(malformedPlayer).isEmpty());
    }

    @Test
    void missingProfileCollectionEntersRecovery() throws Exception {
        Path profileFile = temporaryDirectory.resolve(PlayerData.playerDataFileName);
        Files.writeString(profileFile, """
                {
                  "domain": "integrated player identities",
                  "schemaVersion": 1,
                  "data": {}
                }
                """);
        PlayerProfileRepository repository = new PlayerProfileRepository();

        repository.load(temporaryDirectory.toFile());

        assertEquals(StorageRepository.RepositoryState.RECOVERY, repository.diagnostic().state());
    }

    @Test
    void deferredProfileUpdatesFlushDuringBoundedShutdown() {
        UUID player = UUID.randomUUID();
        PlayerProfileRepository repository = new PlayerProfileRepository();
        repository.load(temporaryDirectory.toFile());

        assertTrue(repository.rememberDeferred(player, "EnVy"));
        assertTrue(repository.shutdown());

        PlayerProfileRepository reloaded = new PlayerProfileRepository();
        reloaded.load(temporaryDirectory.toFile());
        assertEquals("EnVy", reloaded.find(player).orElseThrow().authenticatedUsername());
        assertTrue(reloaded.shutdown());
    }

    @Test
    void deferredNicknameUpdatesFlushDuringBoundedShutdown() {
        UUID player = UUID.randomUUID();
        PlayerProfileRepository repository = new PlayerProfileRepository();
        repository.load(temporaryDirectory.toFile());

        assertTrue(repository.rememberDeferred(player, "EnVy"));
        assertTrue(repository.setNickname(player, "Captain"));
        assertTrue(repository.shutdown());

        PlayerProfileRepository reloaded = new PlayerProfileRepository();
        reloaded.load(temporaryDirectory.toFile());
        assertEquals("Captain", reloaded.find(player).orElseThrow().nickname());
        assertTrue(reloaded.shutdown());
    }
}
