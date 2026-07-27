package com.enviouse.sef.gui.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;

final class SefInvSeeScreen extends ContainerScreen {
    private static final int PANEL = 0xffc6c6c6;
    private static final int PANEL_DARK = 0xff373737;
    private static final int PANEL_LIGHT = 0xffffffff;
    private static final int SLOT_DARK = 0xff373737;
    private static final int SLOT_LIGHT = 0xffffffff;
    private static final int SLOT_FILL = 0xff8b8b8b;
    private static final int HEADER = 0xff3c4350;

    private final String targetName;
    private final int page;

    SefInvSeeScreen(
            ChestMenu menu,
            Inventory playerInventory,
            Component title,
            String targetName,
            int page
    ) {
        super(menu, playerInventory, title);
        this.targetName = targetName;
        this.page = Math.max(0, page);
        this.imageHeight = 222;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;
        graphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, PANEL_DARK);
        graphics.fill(left + 1, top + 1, left + this.imageWidth - 1, top + this.imageHeight - 1, PANEL_LIGHT);
        graphics.fill(left + 2, top + 2, left + this.imageWidth - 2, top + this.imageHeight - 2, PANEL);
        graphics.fill(left + 4, top + 4, left + this.imageWidth - 4, top + 16, HEADER);
        graphics.fill(left + 6, top + 124, left + this.imageWidth - 6, top + 125, PANEL_DARK);
        for (Slot slot : this.menu.slots) {
            drawSlot(graphics, left + slot.x, top + slot.y);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String targetLabel = this.page == 0
                ? targetName + " inventory"
                : targetName + " curios, page " + this.page;
        graphics.drawString(this.font, targetLabel, this.titleLabelX, this.titleLabelY, 0xffffffff, false);
        graphics.drawString(
                this.font,
                Component.translatable("container.inventory"),
                this.inventoryLabelX,
                this.inventoryLabelY,
                0xff404040,
                false);
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, SLOT_DARK);
        graphics.fill(x, y, x + 17, y + 17, SLOT_LIGHT);
        graphics.fill(x, y, x + 16, y + 16, SLOT_FILL);
    }
}
