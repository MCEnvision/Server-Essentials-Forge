package com.enviouse.sef.workstations;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;

final class VirtualCraftingMenu extends CraftingMenu {
    VirtualCraftingMenu(int containerId, Inventory inventory) {
        super(containerId, inventory, ContainerLevelAccess.create(inventory.player.level(), inventory.player.blockPosition()));
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
