package com.enviouse.sef.motd;

import com.enviouse.sef.storage.repository.StorageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MotdManagerTest {
    @TempDir
    Path directory;

    @Test
    void malformedReloadPreservesLiveMotdAndRejectsMutation() throws Exception {
        MotdManager manager = new MotdManager(directory);
        manager.load();
        manager.setMotd("first", "second");
        Files.writeString(
                directory.resolve("motd.json"),
                "{\"domain\":\"motd\",\"schemaVersion\":1,\"data\":{\"line1\":7,\"line2\":[]}}");

        manager.load();

        assertEquals(StorageRepository.RepositoryState.RECOVERY, manager.state());
        assertEquals(new MotdManager.MotdData("first", "second"), manager.getData());
        assertThrows(IllegalStateException.class, () -> manager.setMotd("changed", ""));
    }
}
