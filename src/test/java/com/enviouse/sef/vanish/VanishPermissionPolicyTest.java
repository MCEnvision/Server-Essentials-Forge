package com.enviouse.sef.vanish;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class VanishPermissionPolicyTest {
    @Test
    void permissionRemovalForcesUnvanish() {
        assertEquals(0, VanishPermissionPolicy.reconcileLevel(true, 1, 0));
        assertEquals(0, VanishPermissionPolicy.reconcileLevel(true, 3, 0));
    }

    @Test
    void weakerPermissionLowersStoredVanishLevel() {
        assertEquals(2, VanishPermissionPolicy.reconcileLevel(true, 1, 2));
        assertEquals(3, VanishPermissionPolicy.reconcileLevel(true, 2, 3));
    }

    @Test
    void strongerPermissionDoesNotSilentlyIncreaseConcealment() {
        assertEquals(3, VanishPermissionPolicy.reconcileLevel(true, 3, 1));
        assertEquals(2, VanishPermissionPolicy.reconcileLevel(true, 2, 1));
    }

    @Test
    void invalidStoredLevelUsesCurrentBestPermission() {
        assertEquals(1, VanishPermissionPolicy.reconcileLevel(true, 0, 1));
        assertEquals(2, VanishPermissionPolicy.reconcileLevel(true, 9, 2));
    }

    @Test
    void persistedUnvanishedStateAlwaysWins() {
        assertEquals(0, VanishPermissionPolicy.reconcileLevel(false, 1, 1));
    }

    @Test
    void queueRequiresBasePermissionAndOthersPermissionForAnotherPlayer() {
        assertTrue(VanishPermissionPolicy.canQueueTarget(false, true, false));
        assertTrue(VanishPermissionPolicy.canQueueTarget(true, true, true));
        assertFalse(VanishPermissionPolicy.canQueueTarget(false, false, true));
        assertFalse(VanishPermissionPolicy.canQueueTarget(true, true, false));
    }
}
