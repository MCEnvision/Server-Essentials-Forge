package com.enviouse.sef.permissions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamicPermissionServiceTest {
    @Test
    void broadWildcardDiagnosticsMatchDelegatedSudoCapabilities() {
        String delegated = "sef.commands.sudo.delegate.confirm";

        assertTrue(DynamicPermissionService.broadGrantCovers("*", delegated));
        assertTrue(DynamicPermissionService.broadGrantCovers("sef.*", delegated));
        assertTrue(DynamicPermissionService.broadGrantCovers("sef.commands.*", delegated));
        assertTrue(DynamicPermissionService.broadGrantCovers("sef.commands.sudo.*", delegated));
        assertTrue(DynamicPermissionService.broadGrantCovers("sef.commands.sudo.delegate.*", delegated));
        assertFalse(DynamicPermissionService.broadGrantCovers(
                "sef.commands.sudo.delegate.confirm",
                delegated));
        assertFalse(DynamicPermissionService.broadGrantCovers("sef.commands.teleport.*", delegated));
        assertFalse(DynamicPermissionService.broadGrantCovers("sef.commands.sudo.delegate", delegated));
    }
}
