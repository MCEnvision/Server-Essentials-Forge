package com.enviouse.sef.kernel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandAuditScopeTest {
    @Test
    void scopeRestoresThePreviousCommandBoundary() {
        assertFalse(CommandAuditScope.active());
        try (CommandAuditScope outer = CommandAuditScope.open("sef:test.outer")) {
            assertTrue(CommandAuditScope.active());
            assertEquals("sef:test.outer", CommandAuditScope.currentActionId().orElseThrow());
            try (CommandAuditScope inner = CommandAuditScope.open("sef:test.inner")) {
                assertEquals("sef:test.inner", CommandAuditScope.currentActionId().orElseThrow());
            }
            assertEquals("sef:test.outer", CommandAuditScope.currentActionId().orElseThrow());
        }
        assertFalse(CommandAuditScope.active());
    }
}
