package com.enviouse.sef.control;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinecraftServerControlRuntimeTest {
    @Test
    void reservedSlotsReduceOnlyOrdinaryAdmissionCapacity() {
        assertEquals(18, MinecraftServerControlRuntime.ordinaryAdmissionMaximum(20, 2));
        assertEquals(20, MinecraftServerControlRuntime.ordinaryAdmissionMaximum(20, -1));
        assertEquals(0, MinecraftServerControlRuntime.ordinaryAdmissionMaximum(20, 30));
        assertEquals(0, MinecraftServerControlRuntime.ordinaryAdmissionMaximum(0, 5));
    }

    @Test
    void performanceQueriesAreLimitedGloballyAndPerActor() {
        MinecraftServerControlRuntime.clear();
        java.util.UUID first = java.util.UUID.randomUUID();
        java.util.UUID second = java.util.UUID.randomUUID();
        long start = 10_000L;

        assertTrue(MinecraftServerControlRuntime.performanceAllowed(first, start));
        assertFalse(MinecraftServerControlRuntime.performanceAllowed(second, start + 999L));
        assertTrue(MinecraftServerControlRuntime.performanceAllowed(second, start + 1_000L));
        assertFalse(MinecraftServerControlRuntime.performanceAllowed(first, start + 4_999L));
        assertTrue(MinecraftServerControlRuntime.performanceAllowed(first, start + 5_000L));
    }
}
