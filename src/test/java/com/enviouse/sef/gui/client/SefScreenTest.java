package com.enviouse.sef.gui.client;

import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SefScreenTest {
    @Test
    void everyEnhancedScreenSuppressesTheVanillaBackgroundPass() {
        assertAll(
                () -> assertTrue(SefScreen.class.isAssignableFrom(FancyTagStudioScreen.class)),
                () -> assertTrue(SefScreen.class.isAssignableFrom(SefControlEditorScreen.class)),
                () -> assertTrue(SefScreen.class.isAssignableFrom(SefItemPickerScreen.class)),
                () -> assertTrue(SefScreen.class.isAssignableFrom(SefPanelScreen.class)),
                () -> assertTrue(SefScreen.class.isAssignableFrom(SefPlayerPickerScreen.class)),
                () -> assertTrue(SefScreen.class.isAssignableFrom(SefSuggestionPickerScreen.class)),
                () -> assertTrue(SefScreen.class.isAssignableFrom(SefWorkflowScreen.class)));
    }

    @Test
    void suppressedBackgroundPassDoesNotTouchTheRenderer() {
        SefScreen screen = new SefScreen(Component.empty()) {
        };

        assertDoesNotThrow(() -> screen.renderBackground(null, 0, 0, 0.0F));
    }
}
