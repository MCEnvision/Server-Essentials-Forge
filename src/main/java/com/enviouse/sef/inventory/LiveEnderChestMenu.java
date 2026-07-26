package com.enviouse.sef.inventory;

import com.enviouse.sef.kernel.KernelServices;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

final class LiveEnderChestMenu extends ChestMenu {
    private final ServerPlayer viewer;
    private final ServerPlayer target;
    private final long configurationRevision;

    LiveEnderChestMenu(int containerId, Inventory inventory, ServerPlayer viewer, ServerPlayer target) {
        super(MenuType.GENERIC_9x3, containerId, inventory, target.getEnderChestInventory(), 3);
        this.viewer = viewer;
        this.target = target;
        this.configurationRevision = KernelServices.configurationRevision();
    }

    @Override
    public void clicked(int slotId, int button, net.minecraft.world.inventory.ClickType clickType, Player player) {
        if (!stillValid(player)) {
            viewer.closeContainer();
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotId) {
        if (!stillValid(player)) {
            viewer.closeContainer();
            return ItemStack.EMPTY;
        }
        return super.quickMoveStack(player, slotId);
    }

    @Override
    public void broadcastChanges() {
        if (!stillValid(viewer)) {
            viewer.closeContainer();
            return;
        }
        super.broadcastChanges();
    }

    @Override
    public boolean stillValid(Player player) {
        return player == viewer
                && viewer.isAlive()
                && !viewer.hasDisconnected()
                && target.isAlive()
                && !target.hasDisconnected()
                && configurationRevision == KernelServices.configurationRevision()
                && InventoryUtilityCommands.canAccessEnderChest(viewer, target);
    }
}
