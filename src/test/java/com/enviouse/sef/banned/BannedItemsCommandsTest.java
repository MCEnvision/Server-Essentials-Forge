package com.enviouse.sef.banned;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BannedItemsCommandsTest {
    @Test
    void parsesSupportedDurations() {
        assertEquals(-1L, BannedItemsCommands.parseDurationMs("permanent"));
        assertEquals(30_000L, BannedItemsCommands.parseDurationMs("30s"));
        assertEquals(300_000L, BannedItemsCommands.parseDurationMs("5m"));
        assertEquals(5_400_000L, BannedItemsCommands.parseDurationMs("1h30m"));
        assertEquals(216_000_000L, BannedItemsCommands.parseDurationMs("2d12h"));
        assertEquals(60_000L, BannedItemsCommands.parseDurationMs("60"));
    }

    @Test
    void rejectsMalformedDurationsInsteadOfCreatingPermanentBans() {
        assertThrows(IllegalArgumentException.class, () -> BannedItemsCommands.parseDurationMs("nonsense"));
        assertThrows(IllegalArgumentException.class, () -> BannedItemsCommands.parseDurationMs("5x"));
        assertThrows(IllegalArgumentException.class, () -> BannedItemsCommands.parseDurationMs("1h2"));
        assertThrows(IllegalArgumentException.class, () -> BannedItemsCommands.parseDurationMs("0"));
        assertThrows(IllegalArgumentException.class, () -> BannedItemsCommands.parseDurationMs(""));
        assertThrows(IllegalArgumentException.class, () -> BannedItemsCommands.parseDurationMs(null));
    }

    @Test
    void rejectsOverflowingDurations() {
        assertThrows(
                IllegalArgumentException.class,
                () -> BannedItemsCommands.parseDurationMs("999999999999999999999999d"));
    }
}
