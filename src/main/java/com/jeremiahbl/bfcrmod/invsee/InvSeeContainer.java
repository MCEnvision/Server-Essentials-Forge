package com.jeremiahbl.bfcrmod.invsee;

import com.jeremiahbl.bfcrmod.config.ConfigHandler;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Custom container menu for the InvSee GUI.
 * Supports pagination with clickable arrows.
 * Can be read-only based on config.
 */
public class InvSeeContainer extends AbstractContainerMenu {
    private final List<ItemStack[]> pages;
    private int currentPage;
    private final SimpleContainer displayContainer;
    private final boolean readOnly;

    public InvSeeContainer(int containerId, Inventory playerInventory, List<ItemStack[]> pages) {
        super(MenuType.GENERIC_9x6, containerId);
        this.pages = pages;
        this.currentPage = 0;
        this.readOnly = ConfigHandler.config.invSeeReadOnly.get();
        this.displayContainer = new SimpleContainer(54);

        loadPage(0);

        // Add the 6 rows of the display container (the double chest)
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new DisplaySlot(displayContainer, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        // Add the player's own inventory slots (bottom 3 rows)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }

        // Add the player's hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
        }
    }

    private void loadPage(int page) {
        if (page < 0 || page >= pages.size()) return;
        this.currentPage = page;
        ItemStack[] pageItems = pages.get(page);
        for (int i = 0; i < 54; i++) {
            displayContainer.setItem(i, pageItems[i] != null ? pageItems[i].copy() : ItemStack.EMPTY);
        }
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        // Handle navigation clicks on display slots
        if (slotId >= 0 && slotId < 54 && pages.size() > 1) {
            if (slotId == InvSeeLayout.NAV_PREV_SLOT && currentPage > 0) {
                loadPage(currentPage - 1);
                broadcastChanges();
                return;
            }
            if (slotId == InvSeeLayout.NAV_NEXT_SLOT && currentPage < pages.size() - 1) {
                loadPage(currentPage + 1);
                broadcastChanges();
                return;
            }
        }

        // Block all interactions on display slots if read-only
        if (readOnly && slotId >= 0 && slotId < 54) {
            return;
        }

        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // Block shift-clicking from display container when read-only
        if (readOnly && index < 54) {
            return ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    /**
     * Custom slot class for the display container.
     * Prevents item placement when in read-only mode.
     */
    private class DisplaySlot extends Slot {
        public DisplaySlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !readOnly;
        }

        @Override
        public boolean mayPickup(Player player) {
            return !readOnly;
        }
    }
}



