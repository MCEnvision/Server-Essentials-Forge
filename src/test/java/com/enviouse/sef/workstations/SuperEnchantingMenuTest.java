package com.enviouse.sef.workstations;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SuperEnchantingMenuTest {
    @Test
    void configuredLevelBoundsPreserveRemovalAndClampNonzeroLevels() {
        assertEquals(0, SuperEnchantingMenu.normalizeLevel(0, 5, 20));
        assertEquals(5, SuperEnchantingMenu.normalizeLevel(1, 5, 20));
        assertEquals(10, SuperEnchantingMenu.normalizeLevel(10, 5, 20));
        assertEquals(20, SuperEnchantingMenu.normalizeLevel(100, 5, 20));
    }

    @Test
    void invalidConfiguredLevelRangeIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                SuperEnchantingMenu.normalizeLevel(1, 20, 5));
        assertThrows(IllegalArgumentException.class, () ->
                SuperEnchantingMenu.normalizeLevel(1, 0, 5));
        assertThrows(IllegalArgumentException.class, () ->
                SuperEnchantingMenu.normalizeLevel(
                        1,
                        1,
                        AdministrativeEnchantCommands.IMPLEMENTATION_MAXIMUM_LEVEL + 1));
        assertEquals(
                1000,
                SuperEnchantingMenu.normalizeLevel(1000, 1, 1000));
    }
}
