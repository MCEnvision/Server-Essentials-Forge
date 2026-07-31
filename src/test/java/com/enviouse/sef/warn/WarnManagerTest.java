package com.enviouse.sef.warn;

import com.enviouse.sef.storage.repository.StorageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WarnManagerTest {
    @TempDir
    Path directory;

    @Test
    void malformedReloadPreservesLiveWarningsAndRejectsMutation() throws Exception {
        WarnManager manager = new WarnManager();
        UUID player = UUID.randomUUID();
        manager.load(directory);
        manager.addWarn(
                player,
                "first warning",
                "Console",
                new UUID(0L, 0L).toString(),
                -1L);
        Files.writeString(
                directory.resolve("warns.json"),
                """
                        {"domain":"warnings","schemaVersion":1,"data":{"not-a-uuid":[]}}
                        """);

        manager.load(directory);

        assertEquals(StorageRepository.RepositoryState.RECOVERY, manager.state());
        assertEquals(1, manager.getWarns(player).size());
        assertThrows(IllegalStateException.class, () -> manager.addWarn(
                player,
                "second warning",
                "Console",
                new UUID(0L, 0L).toString(),
                -1L));
    }
}
