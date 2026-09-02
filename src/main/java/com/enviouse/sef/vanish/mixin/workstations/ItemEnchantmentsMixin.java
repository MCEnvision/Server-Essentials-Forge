package com.enviouse.sef.vanish.mixin.workstations;

import com.enviouse.sef.workstations.AdministrativeEnchantCommands;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ItemEnchantments.class)
public abstract class ItemEnchantmentsMixin {
    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 255))
    private int sef$expandStoredLevelCeiling(int original) {
        return AdministrativeEnchantCommands.IMPLEMENTATION_MAXIMUM_LEVEL;
    }

    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 255))
    private static int sef$expandSerializedLevelCeiling(int original) {
        return AdministrativeEnchantCommands.IMPLEMENTATION_MAXIMUM_LEVEL;
    }
}
