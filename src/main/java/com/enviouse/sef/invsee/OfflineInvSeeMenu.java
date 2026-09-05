package com.enviouse.sef.invsee;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.audit.SecurityAuditService;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.kernel.KernelServices;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class OfflineInvSeeMenu extends AbstractContainerMenu {
    private final ServerPlayer viewer;
    private final OfflinePlayerInventoryAdapter adapter;
    private final SimpleContainer offlineInventory;
    private final long configurationRevision;
    private final OfflineInvSeeService.Lease lease;
    private OfflinePlayerInventoryAdapter.Snapshot snapshot;
    private boolean readOnly;

    OfflineInvSeeMenu(
            int containerId,
            Inventory viewerInventory,
            OfflinePlayerInventoryAdapter adapter,
            OfflinePlayerInventoryAdapter.Snapshot snapshot,
            boolean mayModify,
            OfflineInvSeeService.Lease lease
    ) {
        super(MenuType.GENERIC_9x5, containerId);
        this.viewer = (ServerPlayer) viewerInventory.player;
        this.adapter = adapter;
        this.snapshot = snapshot;
        this.configurationRevision = KernelServices.configurationRevision();
        this.lease = lease;
        this.readOnly = !mayModify;
        this.offlineInventory = new SimpleContainer(OfflinePlayerInventoryAdapter.MENU_SLOTS);
        List<ItemStack> stacks = snapshot.stacks();
        for (int slot = 0; slot < stacks.size(); slot++) {
            offlineInventory.setItem(slot, stacks.get(slot).copy());
        }

        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 9; column++) {
                int slot = column + row * 9;
                addSlot(slot < OfflinePlayerInventoryAdapter.INVENTORY_SLOTS
                        ? new Slot(offlineInventory, slot, 8 + column * 18, 18 + row * 18)
                        : new LockedSlot(offlineInventory, slot, 8 + column * 18, 18 + row * 18));
            }
        }
        int verticalOffset = 18;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(
                        viewerInventory,
                        column + row * 9 + 9,
                        8 + column * 18,
                        103 + row * 18 + verticalOffset));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(viewerInventory, column, 8 + column * 18, 161 + verticalOffset));
        }
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!authorizationValid(false)) {
            viewer.closeContainer();
            return;
        }
        if (slotId >= OfflinePlayerInventoryAdapter.INVENTORY_SLOTS
                && slotId < OfflinePlayerInventoryAdapter.MENU_SLOTS) {
            return;
        }
        if (readOnly && (slotId < OfflinePlayerInventoryAdapter.MENU_SLOTS
                || clickType == ClickType.PICKUP_ALL)) {
            return;
        }
        if (!readOnly && !authorizationValid(true)) {
            readOnly = true;
            OfflineInvSeeService.release(snapshot.targetId(), lease);
            viewer.sendSystemMessage(TextFormatter.stringToFormattedText(
                    "&cYour offline inventory modification authority was revoked."));
            return;
        }

        List<ItemStack> targetBefore = copy(offlineInventory);
        List<ItemStack> viewerBefore = copy(viewer.getInventory());
        ItemStack carriedBefore = getCarried().copy();
        super.clicked(slotId, button, clickType, player);
        if (same(targetBefore, offlineInventory)) {
            return;
        }
        try {
            snapshot = adapter.commit(snapshot, offlineInventory);
            AuditService.record(AuditService.Event.interaction(
                    SecurityAuditService.currentSessionId(),
                    viewer.getUUID(),
                    viewer.getGameProfile().getName(),
                    "GUI",
                    "sef:inventory.view",
                    List.of(snapshot.targetId()),
                    Map.of(
                            "mode", "offline",
                            "operation", "modify",
                            "adapter_version", Integer.toString(OfflinePlayerInventoryAdapter.ADAPTER_VERSION),
                            "slot", Integer.toString(slotId),
                            "click", clickType.name(),
                            "revision", snapshot.revision()),
                    AuditService.Result.SUCCESS,
                    com.enviouse.sef.kernel.ActionResult.ReasonCode.SUCCESS,
                    "gui",
                    AuditService.RedactionClass.ITEM_METADATA,
                    AuditService.AuditClass.SENSITIVE_ACCESS));
        } catch (IOException | RuntimeException exception) {
            AuditService.record(AuditService.Event.interaction(
                    SecurityAuditService.currentSessionId(),
                    viewer.getUUID(),
                    viewer.getGameProfile().getName(),
                    "GUI",
                    "sef:inventory.view",
                    List.of(snapshot.targetId()),
                    Map.of(
                            "mode", "offline",
                            "operation", "modify",
                            "slot", Integer.toString(slotId),
                            "click", clickType.name(),
                            "failure", exception.getClass().getSimpleName()),
                    AuditService.Result.FAILED,
                    com.enviouse.sef.kernel.ActionResult.ReasonCode.STORAGE_ERROR,
                    "gui",
                    AuditService.RedactionClass.ITEM_METADATA,
                    AuditService.AuditClass.SENSITIVE_ACCESS));
            restore(offlineInventory, targetBefore);
            restore(viewer.getInventory(), viewerBefore);
            setCarried(carriedBefore);
            broadcastFullState();
            readOnly = true;
            OfflineInvSeeService.release(snapshot.targetId(), lease);
            viewer.sendSystemMessage(TextFormatter.stringToFormattedText(
                    "&cThe offline inventory changed or could not be saved. "
                            + "Your operation was rolled back and the menu is now read only."));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player == viewer && authorizationValid(false);
    }

    @Override
    public void broadcastChanges() {
        if (!authorizationValid(false)) {
            viewer.closeContainer();
            return;
        }
        super.broadcastChanges();
    }

    @Override
    public void removed(Player player) {
        OfflineInvSeeService.release(snapshot.targetId(), lease);
        super.removed(player);
    }

    private boolean authorizationValid(boolean requireModify) {
        return configurationRevision == KernelServices.configurationRevision()
                && OfflineInvSeeService.canAccess(
                viewer,
                snapshot.targetId(),
                snapshot.targetName(),
                requireModify)
                && (!requireModify || OfflineInvSeeService.leaseValid(
                snapshot.targetId(),
                lease,
                viewer.getUUID()));
    }

    private static List<ItemStack> copy(Container container) {
        List<ItemStack> result = new ArrayList<>(container.getContainerSize());
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            result.add(container.getItem(slot).copy());
        }
        return result;
    }

    private static boolean same(List<ItemStack> before, Container current) {
        if (before.size() != current.getContainerSize()) {
            return false;
        }
        for (int slot = 0; slot < before.size(); slot++) {
            if (!ItemStack.matches(before.get(slot), current.getItem(slot))) {
                return false;
            }
        }
        return true;
    }

    private static void restore(Container container, List<ItemStack> stacks) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            container.setItem(slot, stacks.get(slot).copy());
        }
        container.setChanged();
    }

    private static final class LockedSlot extends Slot {
        private LockedSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }
}
