package com.enviouse.sef.kernel;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        assertEquals(requiredRoutes.size(), KernelServices.catalog().size());
        for (String route : requiredRoutes) {
            assertTrue(KernelServices.catalog().findByRoute(route).isPresent(), route);
        }
        assertTrue(KernelServices.catalog().validate().isEmpty());
    }
}
