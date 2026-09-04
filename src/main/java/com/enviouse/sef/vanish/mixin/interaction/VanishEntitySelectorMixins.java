package com.enviouse.sef.vanish.mixin.interaction;

import java.util.List;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

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
 *
 * Uses @WrapOperation instead of @Redirect so multiple mods (e.g. Canary, Lithium)
 * can wrap the same method call without conflicting.
 */
public class VanishEntitySelectorMixins {

	// Prevent minecart collision with vanished players
	@Mixin(AbstractMinecart.class)
	public abstract static class AbstractMinecartMixin {
			@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"))
			private List<Entity> vanishmod$preventMinecartCollision(Level level, Entity entity, AABB aabb, Operation<List<Entity>> original) {
				List<Entity> entities = original.call(level, entity, aabb);
				if (!VanishConfig.get(VanishConfig.CONFIG.preventEntityCollisions))
					return entities;
				return entities.stream()
						.filter(VanishUtil.NO_SPECTATORS_AND_NO_VANISH)
						.collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
			}
	}

	// Prevent bees from angering at vanished players
	@Mixin(BeehiveBlock.class)
	public abstract static class BeehiveBlockMixin {
			@WrapOperation(method = "angerNearbyBees", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"))
			private List<LivingEntity> vanishmod$preventBeeAnger(Level level, Class<LivingEntity> entityClass, AABB aabb, Operation<List<LivingEntity>> original) {
				List<LivingEntity> entities = original.call(level, entityClass, aabb);
				if (!VanishConfig.get(VanishConfig.CONFIG.hidePlayersFromWorld))
					return entities;
				return entities.stream()
						.filter(VanishUtil.NO_SPECTATORS_AND_NO_VANISH)
						.collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
			}
	}
}
