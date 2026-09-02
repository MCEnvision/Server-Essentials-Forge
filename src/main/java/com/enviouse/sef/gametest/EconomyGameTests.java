package com.enviouse.sef.gametest;

import com.enviouse.sef.economy.EconomyInventoryTransaction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("sef")
@PrefixGameTestTemplate(false)
public final class EconomyGameTests {
    private EconomyGameTests() {
    }

    @GameTest(template = "empty")
    public static void providerFailureRollsBackInventoryAndBalanceSideEffects(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Inventory inventory = player.getInventory();
        inventory.setItem(0, new ItemStack(Items.STONE, 10));

        EconomyInventoryTransaction.Result failedSell =
                EconomyInventoryTransaction.sell(inventory, Items.STONE, 4, () -> false);
        helper.assertTrue(!failedSell.successful(), "failed provider accepted a sign sale");
        helper.assertTrue(
                EconomyInventoryTransaction.countComponentSafe(inventory, Items.STONE) == 10,
                "failed sign sale changed inventory");

        EconomyInventoryTransaction.Result failedBuy =
                EconomyInventoryTransaction.buy(
                        inventory,
                        Items.DIAMOND,
                        2,
                        () -> false,
                        () -> helper.fail("uncommitted charge requested a refund"));
        helper.assertTrue(!failedBuy.successful(), "failed provider accepted a sign purchase");
        helper.assertTrue(
                EconomyInventoryTransaction.countComponentSafe(inventory, Items.DIAMOND) == 0,
                "failed sign purchase inserted items");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void inventoryFullTradeRestoresEverySlot(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            inventory.setItem(slot, new ItemStack(Items.STONE, 64));
        }
        int before = EconomyInventoryTransaction.countComponentSafe(inventory, Items.STONE);

        EconomyInventoryTransaction.Result trade = EconomyInventoryTransaction.trade(
                inventory,
                Items.STONE,
                1,
                Items.DIAMOND,
                1);

        helper.assertTrue(!trade.successful(), "inventory full trade unexpectedly succeeded");
        helper.assertTrue(
                EconomyInventoryTransaction.countComponentSafe(inventory, Items.STONE) == before,
                "inventory full trade did not restore offered items");
        helper.assertTrue(
                EconomyInventoryTransaction.countComponentSafe(inventory, Items.DIAMOND) == 0,
                "inventory full trade inserted requested items");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void failedOrExceptionalPurchaseCommitRestoresInventory(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Inventory inventory = player.getInventory();

        EconomyInventoryTransaction.Result rejected = EconomyInventoryTransaction.buy(
                inventory,
                Items.DIAMOND,
                2,
                () -> false);
        helper.assertTrue(!rejected.successful(), "rejected reservation committed a purchase");
        helper.assertTrue(
                EconomyInventoryTransaction.countComponentSafe(inventory, Items.DIAMOND) == 0,
                "rejected reservation retained purchased items");

        try {
            EconomyInventoryTransaction.buy(
                    inventory,
                    Items.DIAMOND,
                    2,
                    () -> {
                        throw new IllegalStateException("injected commit failure");
                    });
            helper.fail("exceptional reservation commit did not throw");
        } catch (IllegalStateException expected) {
            helper.assertTrue(
                    EconomyInventoryTransaction.countComponentSafe(inventory, Items.DIAMOND) == 0,
                    "exceptional reservation commit retained purchased items");
        }
        helper.succeed();
    }
}
