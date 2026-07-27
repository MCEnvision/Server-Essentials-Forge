package com.enviouse.sef.gui.protocol;

import com.enviouse.sef.control.ServerControlSchemaRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HudContractsTest {
    @Test
    void phaseNineContractsAssignOneFallbackOwner() {
        HudContracts.Registry registry = HudContracts.phaseNineDefaults();
        HudContracts.Descriptor warmup = registry.find("teleport_warmup").orElseThrow();

        assertEquals(SefPayloads.HudSurface.PROGRESS, warmup.surface());
        assertEquals(HudContracts.FallbackSurface.ACTION_BAR, warmup.fallback());
        assertEquals(HudContracts.Ownership.SEF, warmup.fallbackOwner());
        long controlDescriptors = ServerControlSchemaRegistry.schemas().stream()
                .filter(schema -> schema.hud() != ServerControlSchemaRegistry.HudPolicy.NONE)
                .count();
        assertEquals(7 + controlDescriptors, registry.descriptors().size());
        ServerControlSchemaRegistry.schemas().stream()
                .filter(schema -> schema.hud() != ServerControlSchemaRegistry.HudPolicy.NONE)
                .forEach(schema -> registry.find("control_" + schema.featureId()).orElseThrow());
    }

    @Test
    void rejectsFallbacksWithoutAnOwner() {
        assertThrows(IllegalArgumentException.class, () -> new HudContracts.Descriptor(
                "broken",
                SefPayloads.HudSurface.ALERT,
                "sef.test",
                HudContracts.FallbackSurface.ACTION_BAR,
                HudContracts.Ownership.NONE));
    }
}
