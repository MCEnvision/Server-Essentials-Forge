package com.enviouse.sef.economy;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public final class EconomyInventoryTransaction {
    private EconomyInventoryTransaction() {
    }

    public static Result buy(
            Inventory inventory,
            Item item,
            int quantity,
            BooleanSupplier withdraw,
            Runnable refund
    ) {
        validate(inventory, item, quantity);
        Objects.requireNonNull(withdraw, "withdraw");
        Objects.requireNonNull(refund, "refund");
        if (!canFit(inventory, item, quantity)) {
            return Result.failure(Code.INVENTORY_FULL);
        }
        if (!withdraw.getAsBoolean()) {
            return Result.failure(Code.PROVIDER_REJECTED);
        }
        List<ItemStack> before = snapshot(inventory);
        if (!insert(inventory, item, quantity)) {
            restore(inventory, before);
            refund.run();
            return Result.failure(Code.COMMIT_FAILED);
        }
        inventory.setChanged();
        return Result.success();
    }

    public static Result buy(
            Inventory inventory,
            Item item,
            int quantity,
            BooleanSupplier commit
    ) {
        validate(inventory, item, quantity);
        Objects.requireNonNull(commit, "commit");
        if (!canFit(inventory, item, quantity)) {
            return Result.failure(Code.INVENTORY_FULL);
        }
        List<ItemStack> before = snapshot(inventory);
        try {
            if (!insert(inventory, item, quantity)) {
                restore(inventory, before);
                return Result.failure(Code.COMMIT_FAILED);
            }
            if (!commit.getAsBoolean()) {
                restore(inventory, before);
                return Result.failure(Code.PROVIDER_REJECTED);
            }
            inventory.setChanged();
            return Result.success();
        } catch (RuntimeException exception) {
            restore(inventory, before);
            throw exception;
        }
    }

    public static Result sell(
            Inventory inventory,
            Item item,
            int quantity,
            BooleanSupplier deposit
    ) {
        validate(inventory, item, quantity);
        Objects.requireNonNull(deposit, "deposit");
        List<ItemStack> before = snapshot(inventory);
        if (!remove(inventory, item, quantity)) {
            return Result.failure(Code.INSUFFICIENT_ITEMS);
        }
        if (!deposit.getAsBoolean()) {
            restore(inventory, before);
            return Result.failure(Code.PROVIDER_REJECTED);
        }
        inventory.setChanged();
        return Result.success();
    }

    public static Result trade(
            Inventory inventory,
            Item offered,
            int offeredQuantity,
            Item received,
            int receivedQuantity
    ) {
        validate(inventory, offered, offeredQuantity);
        validate(inventory, received, receivedQuantity);
        List<ItemStack> before = snapshot(inventory);
        if (!remove(inventory, offered, offeredQuantity)) {
            return Result.failure(Code.INSUFFICIENT_ITEMS);
        }
        if (!canFit(inventory, received, receivedQuantity)
                || !insert(inventory, received, receivedQuantity)) {
            restore(inventory, before);
            return Result.failure(Code.INVENTORY_FULL);
        }
        inventory.setChanged();
        return Result.success();
    }

    public static Result give(Inventory inventory, Item item, int quantity) {
        validate(inventory, item, quantity);
        List<ItemStack> before = snapshot(inventory);
        if (!canFit(inventory, item, quantity) || !insert(inventory, item, quantity)) {
            restore(inventory, before);
            return Result.failure(Code.INVENTORY_FULL);
        }
        inventory.setChanged();
        return Result.success();
    }

    public static int countComponentSafe(Inventory inventory, Item item) {
        Objects.requireNonNull(inventory, "inventory");
        Objects.requireNonNull(item, "item");
        ItemStack reference = new ItemStack(item);
        int available = 0;
        for (ItemStack stack : inventory.items) {
            if (ItemStack.isSameItemSameComponents(stack, reference)) {
                available = Math.addExact(available, stack.getCount());
            }
        }
        return available;
    }

    private static boolean canFit(Inventory inventory, Item item, int quantity) {
        int remaining = quantity;
        ItemStack reference = new ItemStack(item);
        for (ItemStack stack : inventory.items) {
            if (remaining <= 0) {
                break;
            }
            if (stack.isEmpty()) {
                remaining -= reference.getMaxStackSize();
            } else if (ItemStack.isSameItemSameComponents(stack, reference)) {
                remaining -= Math.max(0, stack.getMaxStackSize() - stack.getCount());
            }
        }
        return remaining <= 0;
    }

    private static boolean insert(Inventory inventory, Item item, int quantity) {
        int remaining = quantity;
        int maximum = new ItemStack(item).getMaxStackSize();
        while (remaining > 0) {
            int inserted = Math.min(remaining, maximum);
            ItemStack stack = new ItemStack(item, inserted);
            if (!inventory.add(stack) || !stack.isEmpty()) {
                return false;
            }
            remaining -= inserted;
        }
        return true;
    }

    private static boolean remove(Inventory inventory, Item item, int quantity) {
        if (countComponentSafe(inventory, item) < quantity) {
            return false;
        }
        ItemStack reference = new ItemStack(item);
        int remaining = quantity;
        for (ItemStack stack : inventory.items) {
            if (remaining <= 0) {
                break;
            }
            if (!ItemStack.isSameItemSameComponents(stack, reference)) {
                continue;
            }
            int removed = Math.min(remaining, stack.getCount());
            stack.shrink(removed);
            remaining -= removed;
        }
        return remaining == 0;
    }

    private static List<ItemStack> snapshot(Inventory inventory) {
        List<ItemStack> result = new ArrayList<>(inventory.getContainerSize());
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            result.add(inventory.getItem(slot).copy());
        }
        return result;
    }

    private static void restore(Inventory inventory, List<ItemStack> snapshot) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            inventory.setItem(slot, snapshot.get(slot).copy());
        }
        inventory.setChanged();
    }

    private static void validate(Inventory inventory, Item item, int quantity) {
        Objects.requireNonNull(inventory, "inventory");
        Objects.requireNonNull(item, "item");
        if (quantity < 1 || quantity > 100_000) {
            throw new IllegalArgumentException("Inventory transaction quantity is outside hard bounds");
        }
    }

    public record Result(boolean successful, Code code) {
        static Result success() {
            return new Result(true, Code.SUCCESS);
        }

        static Result failure(Code code) {
            return new Result(false, code);
        }
    }

    public enum Code {
        SUCCESS,
        INVENTORY_FULL,
        INSUFFICIENT_ITEMS,
        PROVIDER_REJECTED,
        COMMIT_FAILED
    }
}
