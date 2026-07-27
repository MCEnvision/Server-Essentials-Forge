package com.enviouse.sef.disguise;

import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.permissions.PermissionService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

import java.time.Instant;
import java.util.Objects;

public final class DisguiseAbilityExecutor {
    private DisguiseAbilityExecutor() {
    }

    public static ActionResult<Void> activate(
            ServerPlayer player,
            DisguiseService.AbilitySlot slot
    ) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(slot, "slot");
        if (!ConfigHandler.config.enableDisguises.get()
                || !ConfigHandler.config.disguiseAbilitiesEnabled.get()) {
            return ActionResult.failure(ActionResult.ReasonCode.FEATURE_DISABLED, "disguise abilities are disabled");
        }
        var commandPermission = PermissionsHandler.phasePermission("commands.disguise.ability");
        if (commandPermission == null || !PermissionService.has(player, commandPermission)) {
            return ActionResult.failure(ActionResult.ReasonCode.PERMISSION_DENIED, "disguise ability permission is required");
        }
        ActionResult<DisguiseService.AbilityAdmission> admission =
                KernelServices.disguises().admitAbility(player.getUUID(), slot, Instant.now());
        if (!admission.successful()) {
            return ActionResult.failure(admission.reason(), admission.detail());
        }
        var abilityPermission = PermissionsHandler.phasePermission(admission.value().ability().permission());
        if (abilityPermission == null || !PermissionService.has(player, abilityPermission)) {
            return ActionResult.failure(ActionResult.ReasonCode.PERMISSION_DENIED, "disguise ability permission is required");
        }
        String actionId = "sef:disguise.ability." + admission.value().ability().id();
        var cooldownResolution = KernelServices.cooldownDurations().resolve(
                player.getUUID(),
                actionId,
                admission.value().ability().cooldown());
        var bypassPermission = PermissionsHandler.phasePermission("commands.disguise.ability.cooldown.bypass");
        boolean bypass = bypassPermission != null && PermissionService.has(player, bypassPermission);
        var cooldown = KernelServices.cooldowns().tryAcquire(
                player.getUUID(),
                actionId,
                cooldownResolution.duration(),
                bypass);
        if (!cooldown.allowed()) {
            return ActionResult.failure(
                    cooldown.reason(),
                    "disguise ability cooldown has " + cooldown.remainingSeconds() + " seconds remaining");
        }
        boolean activated = switch (admission.value().ability().id()) {
            case "blaze_fireball" -> launchBlazeFireball(player);
            case "blaze_hover" -> {
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, false, true));
                yield true;
            }
            case "blaze_fire_resistance" -> {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0, false, true));
                yield true;
            }
            default -> false;
        };
        if (!activated) {
            if (!cooldown.bypassed() && !cooldownResolution.duration().isZero()) {
                KernelServices.cooldowns().clear(player.getUUID(), actionId);
            }
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "disguise ability could not be activated");
        }
        KernelServices.disguises().commitAbility(
                admission.value(),
                bypass ? java.time.Duration.ZERO : cooldownResolution.duration());
        return ActionResult.success(null);
    }

    private static boolean launchBlazeFireball(ServerPlayer player) {
        Vec3 direction = player.getLookAngle().normalize();
        if (!Double.isFinite(direction.x)
                || !Double.isFinite(direction.y)
                || !Double.isFinite(direction.z)
                || direction.lengthSqr() < 0.5D) {
            return false;
        }
        SefBlazeFireball fireball = new SefBlazeFireball(
                player,
                direction,
                ConfigHandler.config.disguiseBlazeFireballDamage.get().floatValue(),
                ConfigHandler.config.disguiseBlazeFireSeconds.get(),
                ConfigHandler.config.disguiseBlazeAllowPvp.get(),
                ConfigHandler.config.disguiseBlazeMaximumRange.get());
        Vec3 start = player.getEyePosition().add(direction.scale(0.75D));
        fireball.setPos(start.x, start.y, start.z);
        boolean added = player.level().addFreshEntity(fireball);
        if (added) {
            DisguiseRuntime.playBlazeShoot(player);
        }
        return added;
    }
}
