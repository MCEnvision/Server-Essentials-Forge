package com.enviouse.sef.vanish;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VanishHierarchyPolicyTest {
    @Test
    void consoleAndExplicitBypassIgnoreHierarchy() {
        assertTrue(VanishHierarchyPolicy.canTarget(true, false, 0, 1));
        assertTrue(VanishHierarchyPolicy.canTarget(false, true, 3, 1));
    }

    @Test
    void playersCanOnlyTargetEqualOrWeakerLevels() {
        assertTrue(VanishHierarchyPolicy.canTarget(false, false, 1, 1));
        assertTrue(VanishHierarchyPolicy.canTarget(false, false, 1, 3));
        assertTrue(VanishHierarchyPolicy.canTarget(false, false, 3, 0));
        assertFalse(VanishHierarchyPolicy.canTarget(false, false, 3, 1));
        assertFalse(VanishHierarchyPolicy.canTarget(false, false, 0, 3));
    }
}
