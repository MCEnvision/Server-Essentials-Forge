package com.enviouse.sef.utils.moddeps;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.enviouse.sef.utils.INicknameProvider;
import com.mojang.authlib.GameProfile;

import dev.ftb.mods.ftbessentials.util.FTBEPlayerData;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class FTBNicknameProvider implements INicknameProvider {
	@Override public String getPlayerNickname(GameProfile player) {
		// FTB Essentials 2101.1.x removed FTBEPlayerData.getOrCreate(GameProfile); the surviving
		// overloads are getOrCreate(Player) and getOrCreate(MinecraftServer, UUID). The provider
		// interface only gives us a GameProfile, so resolve via (server, uuid). An empty Optional
		// (player not in FTB's cache) yields no nickname — identical to the old behavior.
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server == null) return null;
		FTBEPlayerData data = FTBEPlayerData.getOrCreate(server, player.getId()).orElse(null);
		if (data != null && !data.getNick().isEmpty()) {
			return data.getNick();
		}
		return null;
	}
	@Override public @NonNull String getProviderName() {
		return "FTB Essentials";
	}
}
