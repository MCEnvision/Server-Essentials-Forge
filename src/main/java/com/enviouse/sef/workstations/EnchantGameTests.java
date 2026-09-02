package com.enviouse.sef.workstations;

import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("sef")
@PrefixGameTestTemplate(false)
public final class EnchantGameTests {
    private EnchantGameTests() {
    }

    @GameTest(template = "empty")
    public static void arbitraryItemRetainsExtremeEnchantmentComponent(GameTestHelper helper) {
        var sharpness = helper.getLevel()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.SHARPNESS);
        ItemStack dirt = new ItemStack(Items.DIRT, 64);

        AdministrativeEnchantCommands.setEnchantmentComponent(dirt, sharpness, 1000);

        helper.assertValueEqual(
                dirt.getEnchantments().getLevel(sharpness),
                1000,
                "unsafe level did not survive the item component");
        helper.assertValueEqual(dirt.getCount(), 64, "unsafe enchanting changed the item count");
        AdministrativeEnchantCommands.clearEnchantments(dirt);
        helper.assertValueEqual(
                dirt.getEnchantments().size(),
                0,
                "clear did not remove the arbitrary item enchantment");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void implementationCeilingRejectsOverflowingMutation(GameTestHelper helper) {
        var sharpness = helper.getLevel()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.SHARPNESS);
        ItemStack item = new ItemStack(Items.DIAMOND_SWORD);
        try {
            AdministrativeEnchantCommands.setEnchantmentComponent(
                    item,
                    sharpness,
                    AdministrativeEnchantCommands.IMPLEMENTATION_MAXIMUM_LEVEL + 1);
            helper.fail("overflowing enchantment level was accepted");
        } catch (IllegalArgumentException expected) {
            helper.succeed();
        }
    }

    @GameTest(template = "empty")
    public static void administrativeCommandTreeReplacesVanillaEnchant(GameTestHelper helper) {
        var dispatcher = helper.getLevel().getServer().getCommands().getDispatcher();
        var enchant = dispatcher.getRoot().getChild("enchant");
        var sef = dispatcher.getRoot().getChild("sef");

        helper.assertTrue(enchant != null, "administrative enchant root is missing");
        helper.assertTrue(enchant.getChild("targets") != null, "target enchant route is missing");
        helper.assertTrue(enchant.getChild("self") != null, "self enchant route is missing");
        helper.assertTrue(enchant.getChild("remove") != null, "enchantment removal route is missing");
        helper.assertTrue(enchant.getChild("clear") != null, "enchantment clear route is missing");
        helper.assertTrue(
                sef != null && sef.getChild("enchant") != null,
                "canonical sef enchant route is missing");
        helper.succeed();
    }
}
