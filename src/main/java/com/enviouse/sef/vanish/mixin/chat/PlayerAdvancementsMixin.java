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

	// Hide advancement messages from vanished players.
	// NeoForge 1.21.1: PlayerAdvancements.award moved the announce broadcast into a lambda
	// (advancement.display().ifPresent(displayInfo -> { ... broadcastSystemMessage(...) })), so the call now
	// lives in the synthetic instance method lambda$award$2; targeting method="award" scans 0 points and throws
	// a Critical injection failure. Target the lambda directly (verified against neoforge-21.1.233);
	// this.player stays accessible because the lambda is a non-static instance method.
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

