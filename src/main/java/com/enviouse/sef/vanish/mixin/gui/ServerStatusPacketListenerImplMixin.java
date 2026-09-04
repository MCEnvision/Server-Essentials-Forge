package com.enviouse.sef.vanish.mixin.gui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.network.ServerStatusPacketListenerImpl;
import com.enviouse.sef.vanish.VanishConfig;
import com.enviouse.sef.vanish.VanishLifecyclePolicy;
import com.enviouse.sef.vanish.misc.FieldHolder;

@Mixin(ServerStatusPacketListenerImpl.class)
public class ServerStatusPacketListenerImplMixin {
	//Updates the player list sent to clients on the Multiplayer screen to only display and count unvanished players
	@Redirect(method = "handleStatusRequest", at = @At(value = "NEW", target = "(Lnet/minecraft/network/protocol/status/ServerStatus;Ljava/lang/String;)Lnet/minecraft/network/protocol/status/ClientboundStatusResponsePacket;"))
	public ClientboundStatusResponsePacket vanishmod$constructSServerInfoPacket(ServerStatus status, String cachedStatus) {
		// Status pings are handled on the Netty I/O thread and can arrive outside the server-running window
		// (shutdown race / brief config reload), when this SERVER-type config is unloaded and a .get() would
		// throw "Cannot get config value before config is loaded". Fail open: return the unmodified vanilla
		// status (no player is vanished and vanishedServerStatus is stale in that window anyway).
			if (VanishLifecyclePolicy.shouldUseFilteredStatus(
					VanishConfig.SERVER_SPEC.isLoaded(),
					FieldHolder.vanishedServerStatus != null,
					() -> VanishConfig.get(VanishConfig.CONFIG.hidePlayersFromPlayerLists))) {
			status = FieldHolder.vanishedServerStatus;
			cachedStatus = null;
		}

		return new ClientboundStatusResponsePacket(status, cachedStatus);
	}
}
