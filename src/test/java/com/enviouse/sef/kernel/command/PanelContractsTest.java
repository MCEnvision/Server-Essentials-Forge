package com.enviouse.sef.kernel.command;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PanelContractsTest {
    @Test
    void registryExposesImmutableTypedPanelDescriptors() {
        PanelContracts.Registry registry = new PanelContracts.Registry();
        PanelContracts.ControlDescriptor control = new PanelContracts.ControlDescriptor(
                "accept",
                0,
                1,
                "sef:teleport.request.accept",
                "sef.commands.tpaccept",
                PanelContracts.TargetPolicy.SELECTED_VISIBLE_PLAYER,
                false,
                "minecraft:lime_dye");
        PanelContracts.PanelDescriptor panel = new PanelContracts.PanelDescriptor(
                "sef:gui.requests",
                "sef.gui.requests",
                1,
                "sef.commands.tprequests",
                List.of(control),
                new PanelContracts.CommandFallback("tprequests", "sef.gui.requests.usage"));

        registry.register(panel);

        assertEquals(panel, registry.panel(panel.id()).orElseThrow());
        assertEquals(panel, registry.panels().get(panel.id()));
        assertTrue(registry.commandOnlyDescriptors().isEmpty());
        assertEquals(PanelContracts.ExecutionContext.TARGET_ACTOR, control.executionContext());
        assertEquals(PanelContracts.AudienceKind.SELECTED_VISIBLE_PLAYERS, control.audience().kind());
        assertEquals(1, control.audience().maximumTargets());
        assertThrows(UnsupportedOperationException.class, () -> registry.panels().clear());
    }

    @Test
    void delegatedExecutionCannotUseAWeakerAudiencePermission() {
        assertThrows(IllegalArgumentException.class, () -> new PanelContracts.ControlDescriptor(
                "broadcast",
                0,
                1,
                "sef:bundle.broadcast",
                "sef.commands.bundle",
                PanelContracts.TargetPolicy.BOUNDED_AUDIENCE,
                true,
                "minecraft:command_block",
                PanelContracts.ExecutionContext.AS_EACH_PARTICIPANT,
                new PanelContracts.AudienceDescriptor(
                        PanelContracts.AudienceKind.BOUNDED_AUDIENCE,
                        32,
                        "sef.commands.bundle.weak")));
    }
}
