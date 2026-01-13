package com.jeremiahbl.bfcrmod.events;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import com.jeremiahbl.bfcrmod.BetterForgeChat;
import com.jeremiahbl.bfcrmod.MarkdownFormatter;
import com.jeremiahbl.bfcrmod.TextFormatter;
import com.jeremiahbl.bfcrmod.chat.ChatMessageManager;
import com.jeremiahbl.bfcrmod.chat.ChatReplyHandler;
import com.jeremiahbl.bfcrmod.config.ConfigHandler;
import com.jeremiahbl.bfcrmod.config.IReloadable;
import com.jeremiahbl.bfcrmod.config.PermissionsHandler;
import com.jeremiahbl.bfcrmod.utils.BetterForgeChatUtilities;
import com.mojang.authlib.GameProfile;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ChatEventHandler implements IReloadable {
	private SimpleDateFormat timestampFormat = null;
	private boolean markdownEnabled = false;
	private static String chatMessageFormat = "";
	private static String chatMessageColor = "";
	private boolean loaded = false;
	
        @Override
	public void reloadConfigOptions() {
		loaded = false;
		timestampFormat = ConfigHandler.config.enableTimestamp.get() ? new SimpleDateFormat(ConfigHandler.config.timestampFormat.get()) : null;
		markdownEnabled = ConfigHandler.config.enableMarkdown.get();
		chatMessageFormat = ConfigHandler.config.chatMessageFormat.get();
		chatMessageColor = ConfigHandler.config.chatMessageColor.get();
		loaded = true;
	}

	public static String getChatMessageColor() {
		BetterForgeChat.LOGGER.debug("Chat color from config: {}",chatMessageColor);
		String color = chatMessageColor;
		return color;
	}

	public Style getHoverClickEventStyle(Component old) {
		if(old instanceof TranslatableContents tcmp) {
            Object[] args = tcmp.getArgs();
			for(Object arg : args) {
				if(arg instanceof MutableComponent tc) {
                    if(tc.getStyle() != null && tc.getStyle().getClickEvent() != null)
						return ((MutableComponent) arg).getStyle();
				}
			}
		}
		return null;
	}
	
	@SubscribeEvent
    public void onServerChat(ServerChatEvent e) {
		if(!loaded) return; // Just do nothing until everything's ready to go!
    	ServerPlayer player = e.getPlayer();
        GameProfile profile = player.getGameProfile();
    	UUID uuid = profile.getId();
		if(e == null || player == null) return;
        String msg = e.getMessage().getString();
		if(msg == null || (msg).isEmpty()) return;

		// Apply word filters if enabled
		if(ConfigHandler.config.enableFilterSystem.get() && CommandRegistrationHandler.getFilterManager() != null) {
			msg = CommandRegistrationHandler.getFilterManager().applyFilters(msg);
		}

		String tstamp = timestampFormat == null ? "" : timestampFormat.format(new Date());
		String name = BetterForgeChatUtilities.getRawPreferredPlayerName(profile);
		String fmat = chatMessageFormat.replace("$time", tstamp).replace("$name", name);
		MutableComponent beforeMsg = TextFormatter.stringToFormattedText(fmat.substring(0, fmat.indexOf("$msg")));
		MutableComponent afterMsg = TextFormatter.stringToFormattedText(fmat.substring(fmat.indexOf("$msg") + 4));
		boolean enableColor = PermissionsHandler.playerHasPermission(uuid, PermissionsHandler.coloredChatNode);
		boolean enableStyle = PermissionsHandler.playerHasPermission(uuid, PermissionsHandler.styledChatNode);
		
		// Create an error message if the player isn't allowed to use styles/colors
		String emsg = "";
		if(!enableColor && TextFormatter.messageContainsColorsOrStyles(msg, true))
			emsg = "You are not permitted to use colors";
		if(!enableStyle && TextFormatter.messageContainsColorsOrStyles(msg, false))
			emsg += !emsg.isEmpty() ? " or styles" : "You are not permitted to use styles";
		if(!emsg.isEmpty()) {
			MutableComponent ecmp = Component.literal(emsg + "!");
			ecmp.withStyle(ChatFormatting.BOLD);
			ecmp.withStyle(ChatFormatting.RED);
			player.sendSystemMessage(ecmp);
		}
		// Convert markdown to normal essentials formatting
		if(markdownEnabled && enableStyle && PermissionsHandler.playerHasPermission(uuid, PermissionsHandler.markdownChatNode))
			msg = MarkdownFormatter.markdownStringToFormattedString(msg);

		// Start generating the main TextComponent
		MutableComponent msgComp = TextFormatter.stringToFormattedText(msg, enableColor, enableStyle, uuid);

		// Append the hover and click event crap
		Style sty = getHoverClickEventStyle(e.getMessage());
		MutableComponent ecmp = Component.empty();
		if(sty != null && sty.getHoverEvent() != null)
			ecmp.setStyle(sty);
		e.setCanceled(true);
		
		MutableComponent newMessage = beforeMsg.append(msgComp.append(afterMsg));
		
		// Record the message and get its ID for the reply system
		final String finalMsg = msg;
		final String formattedName = name; // name already includes prefix/suffix from getRawPreferredPlayerName
		final String rawName = profile.getName(); // raw username without prefix/suffix
		player.server.execute(() -> {
			// Record message for /ans reply system if enabled
			long messageId = -1;
			if(ConfigHandler.config.enableChatReplies.get()) {
				messageId = ChatMessageManager.recordMessage(uuid, rawName, formattedName, finalMsg);
			}

			// Make message clickable if chat replies are enabled
			MutableComponent clickableMessage;
			if(ConfigHandler.config.enableChatReplies.get() && messageId > 0) {
				final long msgId = messageId;
				clickableMessage = newMessage.withStyle(style -> style
					.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ans " + msgId + " "))
					.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						TextFormatter.stringToFormattedText("&eClick to reply"))));
			} else {
				clickableMessage = newMessage;
			}

			BetterForgeChat.LOGGER.info("[CHAT] "+clickableMessage.getString());
			ServerMessageEvent.broadcastMessage(player.level(), clickableMessage);
		});
		
    }
}