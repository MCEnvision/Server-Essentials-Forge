package com.enviouse.sef.vanish;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VanishListProjectionTest {
    @Test
    void eachObserverGetsAnIndependentProjectionWithoutMutatingInput() {
        List<String> sharedEntries = new ArrayList<>(List.of("owner", "staff", "player"));

        List<String> ordinaryView = VanishListProjection.visibleCopy(
                sharedEntries,
                entry -> !Set.of("owner", "staff").contains(entry));
        List<String> staffView = VanishListProjection.visibleCopy(
                sharedEntries,
                entry -> !"owner".equals(entry));

        assertEquals(List.of("player"), ordinaryView);
        assertEquals(List.of("staff", "player"), staffView);
        assertEquals(List.of("owner", "staff", "player"), sharedEntries);
        assertNotSame(sharedEntries, ordinaryView);
        assertNotSame(ordinaryView, staffView);
        assertThrows(UnsupportedOperationException.class, () -> ordinaryView.add("owner"));
    }
}
