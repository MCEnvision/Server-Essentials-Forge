package com.enviouse.sef.workstations;

import com.enviouse.sef.audit.AuditService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

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

    @Test
    void mutationAuditCarriesBoundedParametersAndCorrelation() {
        UUID sessionId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID parentCorrelation = UUID.randomUUID();
        AuditService.Event event = SuperEnchantingMenu.mutationAuditEvent(
                sessionId,
                actorId,
                "operator",
                actorId,
                "minecraft:sharpness",
                0,
                5,
                2,
                parentCorrelation);

        assertEquals(sessionId, event.sessionId());
        assertEquals(actorId, event.actorId());
        assertEquals(List.of(actorId), event.targetIds());
        assertEquals("sef:workstation.super_enchant.mutation", event.actionId());
        assertEquals(parentCorrelation, event.parentJobId());
        assertEquals("minecraft:sharpness", event.normalizedParameters().get("enchantment"));
        assertEquals("0", event.normalizedParameters().get("previous_level"));
        assertEquals("5", event.normalizedParameters().get("new_level"));
        assertEquals("2", event.normalizedParameters().get("target_slot"));
        assertEquals(AuditService.AuditClass.ADMIN_ACTION, event.auditClass());
    }
}
