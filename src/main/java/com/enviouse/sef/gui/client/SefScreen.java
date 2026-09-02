package com.enviouse.sef.gui.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

abstract class SefScreen extends Screen {
    protected SefScreen(Component title) {
        super(title);
    }

    @Override
    public final void renderBackground(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
    }
}
