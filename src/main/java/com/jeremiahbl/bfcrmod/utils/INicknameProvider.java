package com.jeremiahbl.bfcrmod.utils;

import com.jeremiahbl.bfcrmod.BetterForgeChat;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.mojang.authlib.GameProfile;

public interface INicknameProvider {
	@NonNull public String getProviderName();

	public String getPlayerNickname(@NonNull GameProfile player);
	@NonNull public default String getPlayerChatName(@NonNull GameProfile player) {
		
		String nick = getPlayerNickname(player);
		if(nick == null || nick.isEmpty()){
			BetterForgeChat.LOGGER.debug("no nickname found for: {}",player.getName());
			return player.getName();
		}else{
			BetterForgeChat.LOGGER.debug("found nickname: {} for {}",nick,player.getName());
			return nick;
		}
	}
}