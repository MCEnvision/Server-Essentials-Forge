package com.enviouse.sef.vanish.mixin.workstations;

import com.enviouse.sef.workstations.AdministrativeEnchantCommands;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ItemEnchantments.Mutable.class)
public abstract class ItemEnchantmentsMutableMixin {
    @ModifyConstant(method = {"set", "upgrade"}, constant = @Constant(intValue = 255))
    private int sef$expandMutableLevelCeiling(int original) {
        return AdministrativeEnchantCommands.IMPLEMENTATION_MAXIMUM_LEVEL;
    }
}
