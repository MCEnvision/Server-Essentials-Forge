package com.enviouse.sef.gui.client;

import com.enviouse.sef.gui.protocol.SefPayloads;
import net.minecraft.client.gui.GuiGraphics;

final class SefVanillaTheme {
    static final int TEXT = 0xffffffff;
    static final int MUTED_TEXT = 0xffa0a0a0;
    static final int PANEL_BORDER = 0xffc6c6c6;
    static final int PANEL_BACKGROUND = 0xff202020;
    static final int HEADER = 0xff4a4a4a;
    static final int CONFIRMATION_HEADER = 0xff7f2020;
    static final int PROGRESS = 0xff55aa55;

    private SefVanillaTheme() {
    }

    static void panel(
            GuiGraphics graphics,
            int left,
            int top,
            int width,
            int height,
            SefPayloads.PanelView view
    ) {
        graphics.fill(left, top, left + width, top + height, PANEL_BORDER);
        graphics.fill(left + 3, top + 3, left + width - 3, top + height - 3, PANEL_BACKGROUND);
        graphics.fill(
                left + 6,
                top + 6,
                left + width - 6,
                top + 22,
                view == SefPayloads.PanelView.CONFIRMATION ? CONFIRMATION_HEADER : HEADER);
    }
}
