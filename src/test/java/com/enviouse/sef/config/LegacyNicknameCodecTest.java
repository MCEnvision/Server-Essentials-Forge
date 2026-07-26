package com.enviouse.sef.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LegacyNicknameCodecTest {
    @Test
    void loadsTheLegacyPlayerDataFixtureWithoutLosingEntries() throws IOException {
        try (var stream = getClass().getResourceAsStream("/fixtures/sef.playerdata")) {
            String fixture = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            var entries = LegacyNicknameCodec.parse(fixture);

            assertEquals(2, entries.size());
            assertEquals(UUID.fromString("11111111-1111-1111-1111-111111111111"), entries.get(0).uuid());
            assertEquals("&aEnVy", entries.get(0).nickname());
            assertEquals("Nïck", entries.get(1).nickname());
        }
    }
}
