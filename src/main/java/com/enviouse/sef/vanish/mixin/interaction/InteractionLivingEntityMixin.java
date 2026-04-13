package com.enviouse.sef.vanish.mixin.interaction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.entity.LivingEntity;
import com.enviouse.sef.vanish.VanishConfig;
import com.enviouse.sef.vanish.VanishUtil;

@Mixin(LivingEntity.class)
public abstract class InteractionLivingEntityMixin {

	// Prevent vanished players from being pushable
	@Redirect(method = "isPushable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSpectator()Z"))
	public boolean vanishmod$preventPushing(LivingEntity entity) {
		return entity.isSpectator() || (VanishConfig.CONFIG.preventEntityCollisions.get() && VanishUtil.isVanished(entity));
	}
}

