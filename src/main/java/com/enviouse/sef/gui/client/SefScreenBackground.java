package com.enviouse.sef.gui.client;

import com.enviouse.sef.gui.protocol.ClientProtocolState;
import com.enviouse.sef.gui.protocol.SefProtocol;
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
        if (ClientProtocolState.negotiated(SefProtocol.Feature.BACKGROUND_BLUR)) {
            screen.renderBackground(graphics, mouseX, mouseY, partialTick);
        } else {
            screen.renderTransparentBackground(graphics);
        }
    }
}
