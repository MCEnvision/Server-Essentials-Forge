package com.enviouse.sef.kernel.command;

import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.CooldownService;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteEquivalenceContractTest {
    @Test
    void everyCatalogActionHasOneCanonicalRouteAndMatchingGuiProjection() {
        KernelServices.initialize();

        for (CommandDefinition definition : KernelServices.catalog().entries()) {
            assertEquals(
                    definition.id(),
                    KernelServices.catalog().findByRoute(definition.canonicalRoute()).orElseThrow().id(),
                    definition.id());
            assertTrue(
                    KernelServices.catalog().rootOwner(definition.canonicalRoot()).isPresent(),
                    definition.id());

            if (!definition.playerFacing()) {
                continue;
            }
            UniversalRoute route = universalRoute(definition.id());
            assertEquals(definition.canonicalRoute(), route.commandRoute(), definition.id());
            assertEquals(definition.permissionIds(), route.permissionIds(), definition.id());
        }
    }

    @Test
    void everyActiveShortcutTargetsItsCatalogActionAndCanonicalCooldown() {
        KernelServices.initialize();
        UUID actor = UUID.randomUUID();

        for (ShortcutRegistry.Shortcut shortcut : KernelServices.shortcuts().entries()) {
            CommandDefinition definition = KernelServices.catalog()
                    .find(shortcut.actionId())
                    .orElseThrow(() -> new AssertionError("shortcut target is not cataloged: " + shortcut.root()));
            assertTrue(definition.convenienceRoots().contains(shortcut.root()), shortcut.root());
            assertEquals(shortcut.actionId(), KernelServices.catalog().rootOwner(shortcut.root()).orElseThrow());

            if (!KernelServices.shortcuts().isActive(shortcut.root())) {
                continue;
            }
            CooldownService.Decision acquired = KernelServices.cooldowns().tryAcquire(
                    actor,
                    shortcut.root(),
                    Duration.ofMinutes(1),
                    false);
            assertTrue(acquired.allowed(), shortcut.root());
            CooldownService.Decision canonical = KernelServices.cooldowns().inspect(actor, shortcut.actionId());
            assertFalse(canonical.allowed(), shortcut.root());
            assertEquals(shortcut.actionId(), canonical.canonicalActionId(), shortcut.root());
            KernelServices.cooldowns().clear(actor, shortcut.actionId());
        }
    }

    @Test
    void publishedGuiRoutesExposeOnlyCatalogOwnedActions() {
        KernelServices.initialize();
        for (var category : KernelServices.universalGuiCatalog().categories()) {
            for (var route : KernelServices.universalGuiCatalog().actions(category.panelId())) {
                CommandDefinition definition = KernelServices.catalog().find(route.actionId()).orElse(null);
                assertNotNull(definition, route.actionId());
                assertEquals(definition.canonicalRoute(), route.commandRoute(), route.actionId());
                assertEquals(definition.permissionIds(), route.permissionIds(), route.actionId());
            }
        }
    }

    private static UniversalRoute universalRoute(String actionId) {
        var route = KernelServices.universalGuiCatalog().action(actionId).orElseThrow();
        return new UniversalRoute(route.commandRoute(), route.permissionIds());
    }

    private record UniversalRoute(String commandRoute, java.util.Set<String> permissionIds) {
    }
}
