package com.enviouse.sef.vanish.mixin.interaction;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.phys.AABB;
import com.enviouse.sef.vanish.VanishConfig;
import com.enviouse.sef.vanish.VanishUtil;

/**
 * Collection of mixins that filter vanished players from various entity selection calls.
 * Each inner class is a separate @Mixin target.
 */
public class VanishEntitySelectorMixins {

	// Prevent minecart collision with vanished players
	@Mixin(AbstractMinecart.class)
	public abstract static class AbstractMinecartMixin {
		@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"))
		private List<Entity> vanishmod$preventMinecartCollision(Level level, Entity entity, AABB aabb) {
			if (VanishConfig.CONFIG.preventEntityCollisions.get())
				return level.getEntities(entity, aabb, VanishUtil.NO_SPECTATORS_AND_NO_VANISH);
			return level.getEntities(entity, aabb);
		}
	}

	// Prevent bees from angering at vanished players
	@Mixin(BeehiveBlock.class)
	public abstract static class BeehiveBlockMixin {
		@Redirect(method = "angerNearbyBees", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"))
		private List<LivingEntity> vanishmod$preventBeeAnger(Level level, Class<LivingEntity> entityClass, AABB aabb) {
			if (VanishConfig.CONFIG.hidePlayersFromWorld.get())
				return level.getEntitiesOfClass(entityClass, aabb, VanishUtil.NO_SPECTATORS_AND_NO_VANISH);
			return level.getEntitiesOfClass(entityClass, aabb);
		}
	}
}

