package com.enviouse.sef.chat;

import com.enviouse.sef.storage.repository.StorageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpBulletinHandlerTest {
    @TempDir
    Path directory;

    @Test
    void malformedReloadPreservesLiveBulletins() throws Exception {
        Path file = directory.resolve("bulletin.json");
        Files.writeString(
                file,
                "{\"domain\":\"operator bulletins\",\"schemaVersion\":1,\"data\":[\"notice\"]}");
        OpBulletinHandler.init(directory);
        Files.writeString(
                file,
                "{\"domain\":\"operator bulletins\",\"schemaVersion\":1,\"data\":[7]}");

        OpBulletinHandler.init(directory);

        assertEquals(StorageRepository.RepositoryState.RECOVERY, OpBulletinHandler.state());
        assertEquals(List.of("notice"), OpBulletinHandler.bulletins());
    }
}
