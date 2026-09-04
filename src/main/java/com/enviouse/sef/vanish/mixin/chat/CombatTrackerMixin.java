package com.enviouse.sef.vanish.mixin.chat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.CombatTracker;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import com.enviouse.sef.vanish.VanishConfig;
import com.enviouse.sef.vanish.VanishUtil;

// NeoForge 1.21.1 port note: in 1.20.1, CombatTracker.getDeathMessage built the message inline by calling
// getFallMessage(...) and DamageSource.getLocalizedDeathMessage(...), and SEF @Redirected those two calls.
// In 1.21.1 that logic was extracted into IDeathMessageProvider, so getDeathMessage no longer contains either
// invoke; the old @Redirects scanned 0 targets and threw a Critical injection failure every time CombatTracker
// was class-loaded (observed on entity-deserializer threads at world load). We now filter the final return
// value of getDeathMessage, which is robust to the refactor and covers both the fall-death and normal-death
// paths. The empty-entries generic "death.attack.generic" message has a single arg and is left untouched.
@Mixin(CombatTracker.class)
public class CombatTrackerMixin {

	// Change the death message of an unvanished victim to the generic one (or strip the killer's name) when the
	// killer is a vanished player, per the hideSystemMessages / hidePlayerNameInSystemMessages config options.
	@Inject(method = "getDeathMessage", at = @At("RETURN"), cancellable = true)
	private void vanishmod$filterDeathMessage(CallbackInfoReturnable<Component> cir) {
		cir.setReturnValue(vanishmod$filter(cir.getReturnValue()));
	}

	@Unique
	private Component vanishmod$filter(Component deathMessage) {
		if ((VanishConfig.get(VanishConfig.CONFIG.hideSystemMessages) || VanishConfig.get(VanishConfig.CONFIG.hidePlayerNameInSystemMessages)) && deathMessage != null && deathMessage.getContents() instanceof TranslatableContents content && content.getArgs().length > 1 && content.getArgs()[1] instanceof Component playerName) {
			for (ServerPlayer killer : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
				if (killer.getDisplayName().getString().equals(playerName.getString()) && VanishUtil.isVanished(killer)) {
					if (VanishConfig.get(VanishConfig.CONFIG.hideSystemMessages))
						deathMessage = Component.translatable("death.attack.generic", content.getArgs()[0]);
					else if (VanishConfig.get(VanishConfig.CONFIG.hidePlayerNameInSystemMessages))
						content.getArgs()[1] = Component.literal(VanishConfig.get(VanishConfig.CONFIG.vanishedPlayerNameReplacement));
				}

			}
		}

		return deathMessage;
	}
}
