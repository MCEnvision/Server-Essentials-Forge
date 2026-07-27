package com.enviouse.sef.control;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MinecraftServerControlRuntimeTest {
    @Test
    void reservedSlotsReduceOnlyOrdinaryAdmissionCapacity() {
        assertEquals(18, MinecraftServerControlRuntime.ordinaryAdmissionMaximum(20, 2));
        assertEquals(20, MinecraftServerControlRuntime.ordinaryAdmissionMaximum(20, -1));
        assertEquals(0, MinecraftServerControlRuntime.ordinaryAdmissionMaximum(20, 30));
        assertEquals(0, MinecraftServerControlRuntime.ordinaryAdmissionMaximum(0, 5));
    }
}
