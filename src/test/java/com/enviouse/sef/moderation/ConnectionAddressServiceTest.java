package com.enviouse.sef.moderation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectionAddressServiceTest {
    @Test
    void proxyFailSafeRejectsAnySharedDirectAddress() {
        assertTrue(ConnectionAddressService.sharedActionSafe(1, 1, 10, true));
        assertFalse(ConnectionAddressService.sharedActionSafe(2, 2, 10, true));
        assertFalse(ConnectionAddressService.sharedActionSafe(1, 2, 10, true));
    }

    @Test
    void configuredSharedActionsStillHonorHardCap() {
        assertTrue(ConnectionAddressService.sharedActionSafe(4, 4, 10, false));
        assertFalse(ConnectionAddressService.sharedActionSafe(11, 11, 10, false));
        assertFalse(ConnectionAddressService.sharedActionSafe(-1, 1, 10, false));
    }
}
