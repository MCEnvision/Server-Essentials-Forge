package com.enviouse.sef.permissions;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LuckPermsDynamicPermissionTest {
    @Test
    void broadSefWildcardGrantsRegisteredChildWhenProviderWildcardsAreDisabled() {
        assertTrue(resolve(Map.of(
                "sef.*", HierarchicalPermissionResolver.Evaluation.GRANTED),
                "sef.commands.enchant.unsafe_level"));
    }

    @Test
    void exactDenyOverridesBroadGrant() {
        assertFalse(resolve(Map.of(
                "sef.commands.enchant.unsafe_level", HierarchicalPermissionResolver.Evaluation.DENIED,
                "sef.*", HierarchicalPermissionResolver.Evaluation.GRANTED),
                "sef.commands.enchant.unsafe_level"));
    }

    @Test
    void nearestWildcardDenyOverridesBroaderGrant() {
        assertFalse(resolve(Map.of(
                "sef.commands.enchant.*", HierarchicalPermissionResolver.Evaluation.DENIED,
                "sef.*", HierarchicalPermissionResolver.Evaluation.GRANTED),
                "sef.commands.enchant.unsafe_level"));
    }

    @Test
    void nearestWildcardGrantOverridesGlobalDeny() {
        assertTrue(resolve(Map.of(
                "sef.commands.enchant.*", HierarchicalPermissionResolver.Evaluation.GRANTED,
                "*", HierarchicalPermissionResolver.Evaluation.DENIED),
                "sef.commands.enchant.unsafe_level"));
    }

    @Test
    void undefinedPermissionFailsClosed() {
        assertFalse(resolve(Map.of(), "sef.commands.enchant.unsafe_level"));
    }

    @Test
    void candidatesAreCheckedFromExactToBroadest() {
        Map<String, HierarchicalPermissionResolver.Evaluation> decisions = new LinkedHashMap<>();
        decisions.put("sef.commands.*", HierarchicalPermissionResolver.Evaluation.GRANTED);
        decisions.put("sef.*", HierarchicalPermissionResolver.Evaluation.DENIED);

        assertTrue(resolve(decisions, "sef.commands.disguise"));
    }

    private static boolean resolve(
            Map<String, HierarchicalPermissionResolver.Evaluation> decisions,
            String permission
    ) {
        return HierarchicalPermissionResolver.resolve(
                permission,
                candidate -> decisions.getOrDefault(
                        candidate,
                        HierarchicalPermissionResolver.Evaluation.UNDEFINED));
    }
}
