package com.enviouse.sef.utils;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.mojang.authlib.GameProfile;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.stream.Collectors;

public class SEFUtilities {
	private static String playerNameFormat = "";
	private static String metaJoinSeparator = " ";

	public static void reloadConfig() {
		playerNameFormat = ConfigHandler.config.playerNameFormat.get();
		metaJoinSeparator = ConfigHandler.config.metaJoinSeparator.get();
	}
	
	public static String getRawPreferredPlayerName(GameProfile player) {
		return getRawPreferredPlayerName(player, true, true);
	}
	public static String getRawPreferredPlayerName(GameProfile player, boolean enableNickname, boolean enableMetadata) {
		String name = ServerEssentialsForge.instance.nicknameProvider != null && enableNickname ? ServerEssentialsForge.instance.nicknameProvider.getPlayerChatName(player) : player.getName();
		if(name == null) name = player.getName(); /* No nickname (or null-nickname) provided */
		String pfx = "", sfx = "";
		if(enableMetadata && ServerEssentialsForge.instance.metadataProvider != null) {
			String[] dat = ServerEssentialsForge.instance.metadataProvider.getPlayerPrefixAndSuffix(player);
			if(dat != null) {
				// multiple prefixes/suffixes may come comma-joined
				pfx = joinMeta(dat[0]);
				sfx = joinMeta(dat[1]);
			}
		}
		String fmat = playerNameFormat;
		if(fmat == null) {
			ServerEssentialsForge.LOGGER.warn("Could not get playerNameFormat from configuration file, please post issue on GitHub!");
			return player.getName();
		} else return fmat.replace("$prefix", pfx).replace("$name", name).replace("$suffix", sfx);
	}
		public static MutableComponent getFormattedPlayerName(GameProfile player) {
			return withUsernameHover(
					player,
					TextFormatter.stringToFormattedText(getRawPreferredPlayerName(player)),
					true);
		}
		public static MutableComponent getFormattedPlayerName(GameProfile player, boolean enableNickname, boolean enableMetadata) {
			return withUsernameHover(
					player,
					TextFormatter.stringToFormattedText(
							getRawPreferredPlayerName(player, enableNickname, enableMetadata)),
					enableNickname);
		}
		static MutableComponent withUsernameHover(
				GameProfile player,
				MutableComponent displayName,
				boolean nicknameEnabled
		) {
			if(!nicknameEnabled
					|| !ConfigHandler.config.nicknameAllowDuplicateWithUsernameHover.get()
					|| ServerEssentialsForge.instance == null
					|| ServerEssentialsForge.instance.nicknameProvider == null
					|| ServerEssentialsForge.instance.nicknameProvider.getPlayerNickname(player) == null) {
				return displayName;
			}
			return withUsernameHover(displayName, player.getName(), true);
		}
		static MutableComponent withUsernameHover(
				MutableComponent displayName,
				String authenticatedUsername,
				boolean enabled
		) {
			if(!enabled) {
				return displayName;
			}
			return displayName.withStyle(style -> style.withHoverEvent(new HoverEvent(
					HoverEvent.Action.SHOW_TEXT,
					Component.literal(authenticatedUsername))));
		}
	private static String joinMeta(String raw) {
        if(raw == null || raw.isEmpty()) return "";
        // If provider returned multiple entries separated by \n, split and join
        if(raw.contains("\n")) {
            List<String> parts = raw.lines().filter(l -> !l.isEmpty()).collect(Collectors.toList());
            return String.join(metaJoinSeparator, parts);
        }
        return raw;
    }
}
