package com.enviouse.sef.vanish.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.Holder;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import com.enviouse.sef.vanish.VanishConfig;
import com.enviouse.sef.vanish.VanishLifecyclePolicy;
import com.enviouse.sef.vanish.VanishListProjection;
import com.enviouse.sef.vanish.VanishUtil;
import com.enviouse.sef.vanish.misc.FieldHolder;
import com.enviouse.sef.vanish.misc.SoundSuppressionHelper;
import com.enviouse.sef.vanish.misc.TraceHandler;
import com.enviouse.sef.social.ConnectionMessageService;
import com.enviouse.sef.disguise.DisguiseProxyService;

// 1.20.2+ packet-listener split: send(...) moved from ServerGamePacketListenerImpl to its superclass
// ServerCommonPacketListenerImpl. The vanish per-receiver packet filtering therefore injects here; the
// receiving player is obtained by casting this listener to ServerGamePacketListenerImpl when it is one
// (config/status/login common listeners have no player and never send these game packets).
@Mixin(value = ServerCommonPacketListenerImpl.class, priority = 1200)
public class ServerCommonPacketListenerImplMixin {

	// Filter packets we don't want sent to players who cannot see vanished players (player info, sounds,
	// take-item animations). Player-info REMOVE packets are intentionally not filtered (see VanishingHandler).
	@Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
	private void vanishmod$onSendPacket(Packet<?> packet, CallbackInfo callbackInfo) {
		if (!((Object) this instanceof ServerGamePacketListenerImpl conn)) return;
		// The vanish config is a SERVER-type ModConfigSpec, which is only loaded between server-start and
		// server-stop. send() can be called outside that window by off-thread senders that keep their own
		// schedule (e.g. Simple Voice Chat keepalives via NeoForgeNetManager#sendToClient during the
		// shutdown race, or a momentary unload while FML reloads the watched .toml). Reading any value
		// then throws "Cannot get config value before config is loaded". No player is vanished outside the
		// loaded window, so skipping the filter (fail-open) is the correct behaviour.
		if (!VanishLifecyclePolicy.canFilterPackets(VanishConfig.SERVER_SPEC.isLoaded())) return;
		ServerPlayer receivingPlayer = conn.player;
		Level level = receivingPlayer.level();

		if (packet instanceof ClientboundAddEntityPacket addPacket
				&& DisguiseProxyService.shouldSuppressRealSpawn(receivingPlayer, addPacket.getId())) {
			callbackInfo.cancel();
			return;
		}

		if (packet instanceof ClientboundPlayerInfoUpdatePacket infoPacket) {
			List<ClientboundPlayerInfoUpdatePacket.Entry> filteredPacketEntries =
					VanishListProjection.visibleCopy(
							infoPacket.entries(),
							entry -> !VanishUtil.isVanished(
									receivingPlayer.server.getPlayerList().getPlayer(entry.profileId()),
									receivingPlayer));

			if (filteredPacketEntries.isEmpty())
				callbackInfo.cancel();
			else if (!filteredPacketEntries.equals(infoPacket.entries())) {
				List<ServerPlayer> visiblePlayers = filteredPacketEntries.stream()
						.map(entry -> receivingPlayer.server.getPlayerList().getPlayer(entry.profileId()))
						.filter(java.util.Objects::nonNull)
						.toList();
				callbackInfo.cancel();
				if (visiblePlayers.size() == filteredPacketEntries.size()) {
					conn.send(new ClientboundPlayerInfoUpdatePacket(infoPacket.actions(), visiblePlayers));
				}
			}
		}
		else if (packet instanceof ClientboundTakeItemEntityPacket pickupPacket && level.getEntity(pickupPacket.getPlayerId()) instanceof ServerPlayer pickUppingPlayer && VanishUtil.isVanished(pickUppingPlayer, receivingPlayer)) {
			TraceHandler.trace(pickUppingPlayer, "Pickup Animation", pickupPacket.getItemId() + "x" + pickupPacket.getAmount());
			callbackInfo.cancel();
		}
		else if (VanishConfig.get(VanishConfig.CONFIG.hidePlayersFromWorld)) {
			PlayerList playerList = receivingPlayer.server.getPlayerList();
			Holder<SoundEvent> suppressedSound = null;
			Player vanishedIndirectCause = null;

			if (packet instanceof ClientboundSoundPacket soundPacket) {
				vanishedIndirectCause = SoundSuppressionHelper.getIndirectVanishedSoundCause(SoundSuppressionHelper.getPlayerForPacket(soundPacket, playerList), level, soundPacket.getX(), soundPacket.getY(), soundPacket.getZ(), receivingPlayer);

				if (vanishedIndirectCause != null)
					suppressedSound = soundPacket.getSound();
			}
			else if (packet instanceof ClientboundSoundEntityPacket soundPacket) {
				vanishedIndirectCause = SoundSuppressionHelper.getIndirectVanishedSoundCause(SoundSuppressionHelper.getPlayerForPacket(soundPacket, playerList), level, level.getEntity(soundPacket.getId()), receivingPlayer);

				if (vanishedIndirectCause != null)
					suppressedSound = soundPacket.getSound();
			}
			else if (packet instanceof ClientboundLevelEventPacket soundPacket) {
				vanishedIndirectCause = SoundSuppressionHelper.getIndirectVanishedSoundCause(SoundSuppressionHelper.getPlayerForPacket(soundPacket, playerList), level, Vec3.atCenterOf(soundPacket.getPos()), receivingPlayer);

				if (vanishedIndirectCause != null) {
					TraceHandler.trace(vanishedIndirectCause, "Level Event", soundPacket.getType() + "/" + soundPacket.getData());
					callbackInfo.cancel();
				}
			}
			else if (packet instanceof ClientboundBlockEventPacket eventPacket) {
				vanishedIndirectCause = SoundSuppressionHelper.getIndirectVanishedSoundCause(null, level, Vec3.atCenterOf(eventPacket.getPos()), receivingPlayer);

				if (vanishedIndirectCause != null) {
					TraceHandler.trace(vanishedIndirectCause, "Block Event", eventPacket.getBlock().getName().getString() + "/" + eventPacket.getB0() + "/" + eventPacket.getB1());
					callbackInfo.cancel();
				}
			}
			else if (packet instanceof ClientboundLevelParticlesPacket particlesPacket) {
				vanishedIndirectCause = SoundSuppressionHelper.getIndirectVanishedParticleCause(null, level, particlesPacket.getX(), particlesPacket.getY(), particlesPacket.getZ(), receivingPlayer);

				if (vanishedIndirectCause != null) {
					TraceHandler.trace(vanishedIndirectCause, "Particle", particlesPacket.getParticle().getClass().getSimpleName());
					callbackInfo.cancel();
				}
			}

			if (suppressedSound != null) {
				TraceHandler.trace(vanishedIndirectCause, "Sound", suppressedSound.value().getLocation().toString());
				callbackInfo.cancel();
			}
		}
	}

	// Suppress vanilla join/leave/death/advancement/command-feedback system messages that reference vanished
	// players (and optionally strip their names from modded system messages).
	@Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V", at = @At("HEAD"), cancellable = true)
	private void vanishmod$onSendPacketWithListener(Packet<?> packet, PacketSendListener listener, CallbackInfo callbackInfo) {
		if (!((Object) this instanceof ServerGamePacketListenerImpl conn)) return;
		// See vanishmod$onSendPacket: the SERVER-type vanish config may be unloaded when an off-thread
		// sender calls send() outside the server-running window; reading a value would throw. Fail open.
		if (!VanishLifecyclePolicy.canFilterPackets(VanishConfig.SERVER_SPEC.isLoaded())) return;
		ServerPlayer player = conn.player;
		if (packet instanceof ClientboundSystemChatPacket chatPacket) {
			ServerPlayer subject = ConnectionMessageService.subject(chatPacket.content()).orElse(null);
			if (subject != null && VanishUtil.isVanished(subject, player)) {
				TraceHandler.trace(subject, "Connection Message", chatPacket.content().getString());
				callbackInfo.cancel();
				return;
			}
		}

		if (packet instanceof ClientboundSystemChatPacket chatPacket && chatPacket.content() instanceof MutableComponent component && component.getContents() instanceof TranslatableContents content) {
			List<ServerPlayer> vanishedPlayers = new ArrayList<>(ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().stream().filter(p -> VanishUtil.isVanished(p, player)).toList());
			String key = content.getKey();
			boolean joiningPlayerVanished = VanishUtil.isVanished(FieldHolder.joiningPlayer, player);

			if (joiningPlayerVanished)
				vanishedPlayers.add(FieldHolder.joiningPlayer);

			if (VanishUtil.isVanished(FieldHolder.leavingPlayer, player))
				vanishedPlayers.add(FieldHolder.leavingPlayer);

			if (VanishConfig.get(VanishConfig.CONFIG.hideSystemMessages) || VanishConfig.get(VanishConfig.CONFIG.hidePlayerNameInSystemMessages)) {
				ServerPlayer vanishedSender = null;
				Object[] args = content.getArgs();

				if (key.startsWith("multiplayer.player.joined") && joiningPlayerVanished)
					vanishedSender = FieldHolder.joiningPlayer;
				else if (key.startsWith("multiplayer.player.left") || key.startsWith("death.") || key.startsWith("chat.type.advancement.") || key.startsWith("chat.type.admin")) {
					if (args[0] instanceof Component playerName) {
						for (ServerPlayer sender : vanishedPlayers) {
							if (sender.getDisplayName().getString().equals(playerName.getString())) {
								vanishedSender = sender;
								break;
							}
						}
					}
				}

				if (vanishedSender != null) {
					if (VanishConfig.get(VanishConfig.CONFIG.hideSystemMessages)) {
						TraceHandler.trace(vanishedSender, "Announcement", component.getString());
						callbackInfo.cancel();
					}
					else if (VanishConfig.get(VanishConfig.CONFIG.hidePlayerNameInSystemMessages)) {
						Component replacement = Component.literal(VanishConfig.get(VanishConfig.CONFIG.vanishedPlayerNameReplacement));

						TraceHandler.trace(vanishedSender, Component.literal("Player Name (now \"").append(replacement).append("\")"), component.getString());
						args[0] = replacement;
					}

					return;
				}
			}

			if (VanishConfig.get(VanishConfig.CONFIG.removeModdedSystemMessageReferences) && !key.startsWith("commands.message.display.incoming") && !key.startsWith("chat.type.")) {
				for (Object arg : content.getArgs()) {
					if (arg instanceof Component componentArg) {
						String potentialPlayerName = componentArg.getString();

						for (ServerPlayer vanishedPlayer : vanishedPlayers) {
							if (vanishedPlayer.getDisplayName().getString().equals(potentialPlayerName)) {
								TraceHandler.trace(vanishedPlayer, "Mentioning Message", component.getString());
								callbackInfo.cancel();
								return;
							}
						}
					}
				}
			}
		}
	}
}
