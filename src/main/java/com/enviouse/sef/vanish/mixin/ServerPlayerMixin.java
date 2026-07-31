package com.enviouse.sef.vanish.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ChatType.Bound;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.chat.ChatMessageManager;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.fancytags.FancyTagFallbackRenderer;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.utils.SEFUtilities;
import com.enviouse.sef.vanish.VanishConfig;
import com.enviouse.sef.vanish.VanishUtil;
import com.enviouse.sef.vanish.misc.TraceHandler;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
	@Shadow
	@Final
	public MinecraftServer server;

	//player entity needs a constructor, so here we go
	public ServerPlayerMixin(Level world, BlockPos pos, float angle, GameProfile gameProfile) {
		super(world, pos, angle, gameProfile);
	}

	//1. Suppresses chat and /teammsg messages from vanished to unvanished players
	//2. Changes other chat messages to system messages so unvanished clients don't disconnect when receiving these messages (due to the sender's UUID not being present there)
	//3. Conceals the vanished sender of a /say, /me or /msg message by replacing its name with "vanished"
	@Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
	public void vanishmod$onSendChatMessage(OutgoingChatMessage message, boolean filter, Bound chatType, CallbackInfo callback) {
		if (message instanceof OutgoingChatMessage.Player playerChatMessage) {
			ServerPlayer receiver = (ServerPlayer) (Object) this;
			ServerPlayer sender = server.getPlayerList().getPlayer(playerChatMessage.message().sender());
			if (sender == null) {
				return;
			}
			ResourceKey<ChatType> chatTypeKey = VanishUtil.getChatTypeRegistryKey(chatType, receiver);
			boolean ordinaryChat = ChatType.CHAT.equals(chatTypeKey);
			boolean teamChat = ChatType.TEAM_MSG_COMMAND_INCOMING.equals(chatTypeKey);
			boolean hiddenFromReceiver = VanishUtil.isVanished(sender, receiver);

			if (hiddenFromReceiver) {
				if (VanishConfig.CONFIG.hideChatMessages.get() && (ordinaryChat || teamChat)) {
					TraceHandler.trace(sender, "Chat Message", message.content().getString());
					callback.cancel();
					return;
				}

				if (VanishConfig.CONFIG.hidePlayerNameInChat.get()) {
					Component replacement =
							Component.literal(VanishConfig.CONFIG.vanishedPlayerNameReplacement.get());
					TraceHandler.trace(
							sender,
							Component.literal("Chat Message Sender (now \"")
									.append(replacement)
									.append("\")"),
							message.content().getString());
					chatType = ChatType.bind(chatTypeKey, level().registryAccess(), replacement);
				}
				new OutgoingChatMessage.Disguised(playerChatMessage.content())
						.sendToPlayer(receiver, filter, chatType);
				callback.cancel();
				return;
			}

			if (!ordinaryChat) {
				return;
			}
			Component decorated = FancyTagFallbackRenderer.decorateChat(
					playerChatMessage.content().copy(),
					sender,
					receiver);
			if (ConfigHandler.config.enableChatReplies.get()
					&& PermissionService.has(receiver, PermissionsHandler.ansCommand)) {
				try {
					String token = ChatMessageManager.issueToken(
							sender.getUUID(),
							receiver.getUUID(),
							sender.getGameProfile().getName(),
							SEFUtilities.getRawPreferredPlayerName(sender.getGameProfile()),
							playerChatMessage.content().getString());
					decorated = decorated.copy().withStyle(style -> style
							.withClickEvent(new ClickEvent(
									ClickEvent.Action.SUGGEST_COMMAND,
									"/ans " + token + " "))
							.withHoverEvent(new HoverEvent(
									HoverEvent.Action.SHOW_TEXT,
									TextFormatter.stringToFormattedText("&eClick to reply"))));
				} catch (RuntimeException exception) {
					ServerEssentialsForge.LOGGER.error(
							"[SEF] Failed to issue a chat reply capability for {}",
							receiver.getUUID(),
							exception);
				}
			}
			PlayerChatMessage decoratedMessage =
					playerChatMessage.message().withUnsignedContent(decorated);
			OutgoingChatMessage.create(decoratedMessage).sendToPlayer(receiver, filter, chatType);
			callback.cancel();
		}
	}

	//Hacky mixin that should improve mod compat: mods should always respect spectator mode when targeting players, and this mixin lets isSpectator also check if the player is vanished (and thus should also not be targeted); but don't interfere with Vanilla's isSpectator() calls, else weird glitches can happen
	@Inject(method = "isSpectator", at = @At("HEAD"), cancellable = true)
	public void vanishmod$onIsSpectator(CallbackInfoReturnable<Boolean> callback) {
		if (VanishConfig.CONFIG.fixPlayerDetectionModCompatibility.get() && VanishUtil.isVanished(this)) {
			String callerClassName = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(2).findFirst().map(f -> f.getDeclaringClass().getPackageName()).orElse("")); //0 is this mixin, 1 is isSpectator(), 2 is the caller of isSpectator()

			if (!callerClassName.isEmpty() && !callerClassName.startsWith("net.minecraft.")) //if a mod calls this on a vanished player, then it is a spectator and should not be targeted
				callback.setReturnValue(true);
		}
	}
}
