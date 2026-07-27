package com.enviouse.sef.moderation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModerationEventsTest {
    @Test
    void jailAllowlistHandlesNamespacesAliasesAndWhitespace() {
        assertTrue(ModerationEvents.allowedWhileJailed("/minecraft:msg Notch hello"));
        assertTrue(ModerationEvents.allowedWhileJailed("adminchat staff message"));
        assertTrue(ModerationEvents.allowedWhileJailed("rules"));
        assertFalse(ModerationEvents.allowedWhileJailed("minecraft:teleport Notch"));
        assertFalse(ModerationEvents.allowedWhileJailed("execute run msg Notch hello"));
    }
}
