package com.enviouse.sef.inventory;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("sef")
@PrefixGameTestTemplate(false)
public final class InventoryGameTests {
    private InventoryGameTests() {
    }

    @GameTest(template = "empty")
    public static void condensationPreservesExactItemTotals(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Inventory inventory = player.getInventory();
        inventory.setItem(0, new ItemStack(Items.IRON_INGOT, 18));
        inventory.setItem(1, new ItemStack(Items.DIAMOND, 3));

        int crafted = InventoryUtilityCommands.condenseInventory(inventory);

        helper.assertValueEqual(crafted, 2, "unexpected condensation output count");
        helper.assertValueEqual(count(inventory, Items.IRON_INGOT), 0, "iron ingots were not consumed");
        helper.assertValueEqual(count(inventory, Items.IRON_BLOCK), 2, "iron blocks were not created");
        helper.assertValueEqual(count(inventory, Items.DIAMOND), 3, "unrelated items changed");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void incompleteCondensationRecipeDoesNotMutateInventory(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Inventory inventory = player.getInventory();
        inventory.setItem(0, new ItemStack(Items.GOLD_INGOT, 8));
        ItemStack before = inventory.getItem(0).copy();

        int crafted = InventoryUtilityCommands.condenseInventory(inventory);

        helper.assertValueEqual(crafted, 0, "an incomplete recipe produced output");
        helper.assertTrue(
                ItemStack.matches(before, inventory.getItem(0)),
                "an incomplete recipe changed the inventory");
        helper.succeed();
    }

    private static int count(Inventory inventory, Item item) {
        int count = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }
}
