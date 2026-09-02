package com.enviouse.sef.freeze;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FreezeEventHandlerTest {
    @Test
    void chatAllowlistHandlesNamespacesAliasesAndWhitespace() {
        assertTrue(FreezeEventHandler.allowedWhileFrozen("/minecraft:msg Notch hello", true));
        assertTrue(FreezeEventHandler.allowedWhileFrozen("adminchat staff message", true));
        assertTrue(FreezeEventHandler.allowedWhileFrozen("teammsg\tteam message", true));
        assertFalse(FreezeEventHandler.allowedWhileFrozen("minecraft:teleport Notch", true));
        assertFalse(FreezeEventHandler.allowedWhileFrozen("msg Notch hello", false));
    }
}
