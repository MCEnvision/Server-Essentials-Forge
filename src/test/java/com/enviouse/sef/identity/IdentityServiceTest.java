package com.enviouse.sef.identity;

import com.enviouse.sef.kernel.ActionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentityServiceTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void usernameAndNicknameCollisionFailsClosed() {
        PlayerProfileRepository profiles = new PlayerProfileRepository();
        profiles.load(temporaryDirectory.toFile());
        UUID alice = UUID.randomUUID();
        UUID bob = UUID.randomUUID();
        assertTrue(profiles.remember(alice, "Alice"));
        assertTrue(profiles.remember(bob, "Bob"));
        assertTrue(profiles.setNickname(bob, "Alice"));
        IdentityService identities = new IdentityService(() -> null, profiles);

        ActionResult<IdentityService.Identity> result = identities.resolve("alice", null);

        assertEquals(ActionResult.ReasonCode.AMBIGUOUS, result.reason());
        assertTrue(profiles.shutdown());
    }
}
