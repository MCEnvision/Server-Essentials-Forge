package com.enviouse.sef.kernel;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandAuditScopeTest {
    @Test
    void nestedScopesRestoreActionAndCorrelation() {
        UUID outerCorrelation = UUID.randomUUID();
        UUID innerCorrelation = UUID.randomUUID();

        assertFalse(CommandAuditScope.active());
        try (CommandAuditScope outer = CommandAuditScope.open("sef:outer", outerCorrelation)) {
            assertTrue(CommandAuditScope.active());
            assertEquals("sef:outer", CommandAuditScope.currentActionId().orElseThrow());
            assertEquals(outerCorrelation, CommandAuditScope.currentCorrelationId().orElseThrow());

            try (CommandAuditScope inner = CommandAuditScope.open("sef:inner", innerCorrelation)) {
                assertEquals("sef:inner", CommandAuditScope.currentActionId().orElseThrow());
                assertEquals(innerCorrelation, CommandAuditScope.currentCorrelationId().orElseThrow());
            }

            assertEquals("sef:outer", CommandAuditScope.currentActionId().orElseThrow());
            assertEquals(outerCorrelation, CommandAuditScope.currentCorrelationId().orElseThrow());
        }
        assertFalse(CommandAuditScope.active());
        assertTrue(CommandAuditScope.currentCorrelationId().isEmpty());
    }

    @Test
    void scopeFactoryIsNotPublicToUntrustedIntegrations() throws Exception {
        assertFalse(Modifier.isPublic(
                CommandAuditScope.class.getDeclaredMethod("open", String.class).getModifiers()));
        assertFalse(Modifier.isPublic(
                CommandAuditScope.class.getDeclaredMethod("open", String.class, UUID.class).getModifiers()));
    }
}
