package com.enviouse.sef.gui.protocol;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SefProtocolTest {
    @Test
    void inventoryAndBlurFeaturesHaveIndependentNegotiatedBits() {
        long inventory = SefProtocol.Feature.mask(EnumSet.of(SefProtocol.Feature.INVENTORY_VIEW));
        assertTrue(SefProtocol.Feature.INVENTORY_VIEW.present(inventory));
        assertFalse(SefProtocol.Feature.BACKGROUND_BLUR.present(inventory));

        long blur = SefProtocol.Feature.mask(EnumSet.of(SefProtocol.Feature.BACKGROUND_BLUR));
        assertTrue(SefProtocol.Feature.BACKGROUND_BLUR.present(blur));
        assertFalse(SefProtocol.Feature.INVENTORY_VIEW.present(blur));
    }

    @Test
    void serverAdvertisesInventoryAndBlurFeatures() {
        assertTrue(SefProtocol.Feature.INVENTORY_VIEW.present(SefProtocol.SERVER_FEATURES));
        assertTrue(SefProtocol.Feature.BACKGROUND_BLUR.present(SefProtocol.SERVER_FEATURES));
    }
}
