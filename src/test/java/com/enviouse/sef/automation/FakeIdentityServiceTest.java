package com.enviouse.sef.automation;

import com.enviouse.sef.identity.IdentityService;
import com.enviouse.sef.identity.PlayerProfileRepository;
import com.enviouse.sef.message.MessageService;
import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FakeIdentityServiceTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void draftPublicationAndReloadPreserveTheOwnerState() throws Exception {
        FakeIdentityService service = service();
        assertEquals(StorageRepository.RepositoryState.MISSING, service.load(temporaryDirectory).state());

        UUID actor = UUID.randomUUID();
        FakeIdentityService.FakeProfile draft = service.createDraft(
                "operator",
                "operator",
                "Operator",
                "[staff] ",
                "",
                actor).value();
        FakeIdentityService.FakeProfile published = service.publish(
                draft.id(),
                draft.revision(),
                actor).value();
        service.flush();

        FakeIdentityService restored = service();
        assertEquals(StorageRepository.RepositoryState.READY, restored.load(temporaryDirectory).state());
        assertEquals(published.revision(), restored.profiles().getFirst().revision());
        assertEquals("Operator", restored.profiles().getFirst().nickname());
        assertTrue(restored.scene("missing").isEmpty());
    }

    @Test
    void invalidPublishedProfileEntersRecoveryAndBlocksMutation() throws Exception {
        JsonObject data = new JsonObject();
        data.add("drafts", new JsonArray());
        JsonArray published = new JsonArray();
        JsonObject invalid = new JsonObject();
        invalid.addProperty("schemaVersion", 99);
        invalid.addProperty("id", "operator");
        invalid.addProperty("revision", 1L);
        invalid.addProperty("state", "PUBLISHED");
        invalid.addProperty("enabled", true);
        invalid.addProperty("username", "operator");
        invalid.addProperty("nickname", "Operator");
        invalid.addProperty("prefix", "");
        invalid.addProperty("suffix", "");
        invalid.addProperty("changedBy", UUID.randomUUID().toString());
        invalid.addProperty("changedAt", Instant.now().toString());
        published.add(invalid);
        data.add("published", published);
        data.add("history", new JsonArray());
        data.add("scenes", new JsonArray());
        data.add("schedules", new JsonArray());
        StorageService.write(
                temporaryDirectory.resolve("fake-identities.json"),
                "fake identity",
                FakeIdentityService.SCHEMA_VERSION,
                data,
                null);

        FakeIdentityService service = service();
        assertEquals(StorageRepository.RepositoryState.RECOVERY, service.load(temporaryDirectory).state());
        assertThrows(
                IllegalStateException.class,
                () -> service.createDraft("blocked", "blocked", "Blocked", "", "", UUID.randomUUID()));
    }

    private static FakeIdentityService service() {
        IdentityService identities = new IdentityService(() -> null, new PlayerProfileRepository());
        return new FakeIdentityService(identities, new MessageService());
    }
}
