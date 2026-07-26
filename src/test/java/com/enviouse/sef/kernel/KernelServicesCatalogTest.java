package com.enviouse.sef.kernel;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KernelServicesCatalogTest {
    @Test
    void everyCurrentSefActionHasCatalogOwnership() {
        KernelServices.initialize();
        Set<String> requiredRoutes = Set.of(
                "sef info",
                "sef colors",
                "sef test",
                "sef reload",
                "sef commands",
                "sef conflicts",
                "sef doctor",
                "sef filter add",
                "sef filter remove",
                "sef filter list",
                "sef storage status",
                "sef storage export",
                "sef motd set",
                "sef motd reload",
                "sef motd show",
                "sef workstation craft",
                "sef workstation anvil",
                "sef workstation enchant",
                "sef workstation super_enchant",
                "sef workstation repair");

        assertTrue(KernelServices.catalog().size() >= requiredRoutes.size());
        for (String route : requiredRoutes) {
            assertTrue(KernelServices.catalog().findByRoute(route).isPresent(), route);
        }
        assertTrue(KernelServices.catalog().validate().isEmpty());
    }

    @Test
    void socialActionsUseTheirSpecificFeatureAndPermissionContracts() {
        KernelServices.initialize();
        Map<String, String> features = Map.of(
                "sef:social.message", "sef.social",
                "sef:social.message.toggle", "sef.social",
                "sef:social.reply.toggle", "sef.social",
                "sef:social.ignore", "sef.social",
                "sef:social.spy", "sef.social.spy",
                "sef:social.mail", "sef.social.mail",
                "sef:social.connection", "sef.social.connection",
                "sef:social.reminder", "sef.social.reminders",
                "sef:social.text", "sef.social.text",
                "sef:social.identity", "sef.social");

        features.forEach((actionId, featureId) -> {
            var definition = KernelServices.catalog().find(actionId).orElseThrow();
            assertEquals(featureId, definition.featureId(), actionId);
            assertFalse(definition.permissionIds().isEmpty(), actionId);
        });
    }
}
