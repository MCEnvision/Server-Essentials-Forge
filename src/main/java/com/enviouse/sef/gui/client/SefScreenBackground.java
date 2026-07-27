package com.enviouse.sef.gui.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

final class SefScreenBackground {
    private SefScreenBackground() {
    }

    static void render(
            Screen screen,
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        screen.renderTransparentBackground(graphics);
    }
}
