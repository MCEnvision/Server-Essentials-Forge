package com.enviouse.sef.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class SEFUtilitiesTest {
    @Test
    void duplicateNicknameModeAddsAuthenticatedUsernameHover() {
        Component displayName = SEFUtilities.withUsernameHover(
                Component.literal("shared nickname"),
                "EnVy",
                true);

        HoverEvent hover = displayName.getStyle().getHoverEvent();
        assertNotNull(hover);
        assertEquals("EnVy", hover.getValue(HoverEvent.Action.SHOW_TEXT).getString());
    }

    @Test
    void disabledDuplicateNicknameModeDoesNotAddHover() {
        Component displayName = SEFUtilities.withUsernameHover(
                Component.literal("unique nickname"),
                "EnVy",
                false);

        assertNull(displayName.getStyle().getHoverEvent());
    }
}
