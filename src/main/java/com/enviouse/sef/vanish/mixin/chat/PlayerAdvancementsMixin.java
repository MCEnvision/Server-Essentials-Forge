package com.enviouse.sef.vanish.mixin.chat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.network.chat.Component;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import com.enviouse.sef.vanish.VanishConfig;
import com.enviouse.sef.vanish.VanishUtil;

@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementsMixin {

	@Shadow
	private ServerPlayer player;

		// NeoForge moves advancement broadcasts into the synthetic award lambda.
		// Targeting award finds no injection point and causes a critical mixin failure.
		// lambda$award$2 is verified against NeoForge 21.1.235.
	@Redirect(method = "lambda$award$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
	public void vanishmod$hideAdvancementMessage(PlayerList playerList, Component message, boolean overlay) {
		if (VanishConfig.CONFIG.hideSystemMessages.get() && VanishUtil.isVanished(this.player)) {
			// Only send to players who can see the vanished player
			for (ServerPlayer observer : playerList.getPlayers()) {
				if (VanishUtil.playerAllowedToSeeOther(observer, this.player, VanishUtil.isVanished(observer), true)) {
					observer.sendSystemMessage(message);
				}
			}
		} else {
			playerList.broadcastSystemMessage(message, overlay);
		}
	}
}
