package com.enviouse.sef.vanish.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import com.enviouse.sef.invlock.InvLockManager;
import com.enviouse.sef.social.ConnectionMessageService;
import com.enviouse.sef.vanish.VanishUtil;
import com.enviouse.sef.vanish.misc.FieldHolder;
import com.enviouse.sef.vanish.misc.TraceHandler;
import com.enviouse.sef.disguise.DisguiseProxyService;
import com.enviouse.sef.vanish.mixin.accessor.ServerboundInteractPacketAccessor;

// NOTE (1.20.2+ split): the outgoing-packet send(...) overloads moved from ServerGamePacketListenerImpl
// to its superclass ServerCommonPacketListenerImpl, so the vanish send-filtering injectors live in
// ServerCommonPacketListenerImplMixin. This mixin keeps only the game-listener-specific injectors.
@Mixin(value = ServerGamePacketListenerImpl.class, priority = 1200) // run after other mixins; onFinishDisconnect must be last
public class ServerGamePacketListenerImplMixin {
	@Shadow
	public ServerPlayer player;

	@ModifyArg(
			method = "removePlayerFromWorld",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"),
			index = 0)
	private Component sef$customLeaveMessage(Component original) {
		return ConnectionMessageService.render(player, false, original);
	}

	// Stores the player that is about to leave the server and get removed from the regular player list.
	// 1.20.2+: onDisconnect takes DisconnectionDetails (was Component).
	@Inject(method = "onDisconnect", at = @At("HEAD"))
	private void vanishmod$onStartDisconnect(DisconnectionDetails reason, CallbackInfo callbackInfo) {
		FieldHolder.leavingPlayer = player;
	}

	// Removes the stored player after it has fully left the server.
	@Inject(method = "onDisconnect", at = @At("TAIL"))
	private void vanishmod$onFinishDisconnect(DisconnectionDetails reason, CallbackInfo callbackInfo) {
		FieldHolder.leavingPlayer = null;
		TraceHandler.setTracing(player, false);
	}

	// Track active entity during packet handling for sound/event suppression.
	@Inject(method = "handlePlayerAction", at = @At("HEAD"), cancellable = true)
	public void vanishmod$beforeHandlePlayerAction(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
		if (InvLockManager.isEnforced(player.getUUID())
				&& switch (packet.getAction()) {
					case DROP_ITEM, DROP_ALL_ITEMS, SWAP_ITEM_WITH_OFFHAND -> true;
					default -> false;
				}) {
			sef$resynchronizeLockedInventory();
			ci.cancel();
			return;
		}
		VanishUtil.ACTIVE_ENTITY.set(player);
	}

	@Inject(
			method = {
					"handlePickItem",
					"handleContainerSlotStateChanged",
					"handleContainerClick",
					"handlePlaceRecipe",
					"handleContainerButtonClick",
					"handleSetCreativeModeSlot"
			},
			at = @At("HEAD"),
			cancellable = true)
	private void sef$blockLockedInventoryMutation(CallbackInfo ci) {
		if (!InvLockManager.isEnforced(player.getUUID())) {
			return;
		}
		sef$resynchronizeLockedInventory();
		ci.cancel();
	}

	private void sef$resynchronizeLockedInventory() {
		player.containerMenu.sendAllDataToRemote();
		if (player.containerMenu != player.inventoryMenu) {
			player.closeContainer();
		}
	}

	@Inject(method = {"handlePlayerAction", "handleUseItemOn", "handleUseItem", "handleInteract"}, at = @At("RETURN"))
	public void vanishmod$afterPacket(CallbackInfo ci) {
		VanishUtil.ACTIVE_ENTITY.remove();
	}

	@Inject(method = "handleUseItemOn", at = @At("HEAD"))
	public void vanishmod$beforeHandleUseItemOn(CallbackInfo ci) {
		VanishUtil.ACTIVE_ENTITY.set(player);
	}

	@Inject(method = "handleUseItem", at = @At("HEAD"))
	public void vanishmod$beforeHandleUseItem(CallbackInfo ci) {
		VanishUtil.ACTIVE_ENTITY.set(player);
	}

	@Inject(method = "handleInteract", at = @At("HEAD"))
	public void vanishmod$beforeHandleInteract(ServerboundInteractPacket packet, CallbackInfo ci) {
		VanishUtil.ACTIVE_ENTITY.set(player);
	}

	@Inject(method = "handleInteract", at = @At("HEAD"), cancellable = true)
	private void sef$remapDisguiseProxy(ServerboundInteractPacket packet, CallbackInfo ci) {
		int entityId = ((ServerboundInteractPacketAccessor) packet).sef$getEntityId();
		if (DisguiseProxyService.handleInteraction(player, packet, entityId)) {
			VanishUtil.ACTIVE_ENTITY.remove();
			ci.cancel();
		}
	}

	@Inject(method = "tick", at = @At("HEAD"))
	public void vanishmod$beforeTick(CallbackInfo ci) {
		VanishUtil.ACTIVE_ENTITY.set(player);
	}

	@Inject(method = "tick", at = @At("RETURN"))
	public void vanishmod$afterTick(CallbackInfo ci) {
		VanishUtil.ACTIVE_ENTITY.remove();
	}
}
