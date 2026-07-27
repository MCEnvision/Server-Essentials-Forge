package com.enviouse.sef.gui;

import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.gui.protocol.HudContracts;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UniversalGuiCatalogTest {
    @Test
    void everyPlayerFacingActionHasTypedGuiCommandAndHudCoverage() {
        KernelServices.initialize();
        long playerFacing = KernelServices.catalog().entries().stream()
                .filter(definition -> definition.playerFacing())
                .count();

        assertEquals(playerFacing, KernelServices.universalGuiCatalog().actionCount());
        assertTrue(KernelServices.universalGuiCatalog().validate(KernelServices.catalog()).isEmpty());
        assertTrue(KernelServices.descriptors().commandOnlyDescriptors().isEmpty());
        assertTrue(VanillaDescriptorLinter.lint(KernelServices.descriptors()).isEmpty());
        HudContracts.Registry hudContracts = HudContracts.phaseNineDefaults();
        KernelServices.catalog().entries().stream()
                .filter(definition -> definition.playerFacing())
                .forEach(definition -> {
                    UniversalGuiCatalog.ActionRoute route =
                            KernelServices.universalGuiCatalog().action(definition.id()).orElseThrow();
                    assertEquals(definition.canonicalRoute(), route.commandRoute(), definition.id());
                    assertEquals(definition.guiDescriptorId(),
                            KernelServices.universalGuiCatalog()
                                    .category(route.panelId())
                                    .orElseThrow()
                                    .descriptorId(),
                            definition.id());
                    assertTrue(!route.hudDescriptorId().isBlank()
                                    || !route.hudNotApplicableReason().isBlank(),
                            definition.id());
                    if (!route.hudDescriptorId().isBlank()) {
                        assertTrue(hudContracts.find(route.hudDescriptorId()).isPresent(), definition.id());
                    }
                });
    }
}
