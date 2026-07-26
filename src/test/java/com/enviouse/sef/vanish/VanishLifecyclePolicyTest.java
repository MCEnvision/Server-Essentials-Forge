package com.enviouse.sef.vanish;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VanishLifecyclePolicyTest {
    @Test
    void unloadedServerConfigSkipsPacketFiltering() {
        assertFalse(VanishLifecyclePolicy.canFilterPackets(false));
        assertTrue(VanishLifecyclePolicy.canFilterPackets(true));
    }

    @Test
    void shutdownStatusPingDoesNotReadUnloadedConfig() {
        AtomicBoolean accessed = new AtomicBoolean();

        boolean filtered = VanishLifecyclePolicy.shouldUseFilteredStatus(
                false,
                true,
                () -> {
                    accessed.set(true);
                    throw new IllegalStateException("unloaded config");
                });

        assertFalse(filtered);
        assertFalse(accessed.get());
    }

    @Test
    void readyStatusRequiresEnabledPolicyAndAvailableProjection() {
        assertFalse(VanishLifecyclePolicy.shouldUseFilteredStatus(true, false, () -> true));
        assertFalse(VanishLifecyclePolicy.shouldUseFilteredStatus(true, true, () -> false));
        assertTrue(VanishLifecyclePolicy.shouldUseFilteredStatus(true, true, () -> true));
    }
}
