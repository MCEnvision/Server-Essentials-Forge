package com.jeremiahbl.bfcrmod.events;

import com.jeremiahbl.bfcrmod.BetterForgeChat;
import com.jeremiahbl.bfcrmod.commands.NickCommands;
import com.jeremiahbl.bfcrmod.config.ConfigHandler;
import com.jeremiahbl.bfcrmod.utils.IntegratedNicknameProvider;
import com.jeremiahbl.bfcrmod.utils.moddeps.FTBNicknameProvider;
import com.jeremiahbl.bfcrmod.utils.moddeps.LuckPermsProvider;

import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ExternalModLoadingEvent {
	@SubscribeEvent public void onServerStarted(ServerStartedEvent e) {
		loadLuckPerms();
		loadFtbEssentials();
		loadIntegratedNicknameProvider();
	}

	private void loadIntegratedNicknameProvider() {
		if (BetterForgeChat.instance.nicknameProvider == null &&
				ConfigHandler.config.autoEnableChatNicknameCommand.get()) {
			BetterForgeChat.instance.nicknameProvider = new IntegratedNicknameProvider();
			NickCommands.nicknameIntegrationEnabled = true;
			BetterForgeChat.LOGGER.info("Integrated nickname management enabled successfully!");
		}
	}

	private void loadLuckPerms() {
			BetterForgeChat.LOGGER.info("Detected loaded status of luckperms: " + ModList.get().isLoaded("luckperms"));
			if (ModList.get().isLoaded("luckperms")) {
				if (!ConfigHandler.config.enableLuckPerms.get()) {
					BetterForgeChat.LOGGER.info("LuckPerms API was skipped by configuration file!");
					return;
				}
				BetterForgeChat.LOGGER.info("Attempting to integrate LuckPerms API!");
				try {
					BetterForgeChat.instance.metadataProvider = new LuckPermsProvider();
					BetterForgeChat.LOGGER.info("LuckPerms API found and integrated successfully!");
				} catch (Exception e) { // Could have a NoClassDefFoundError here!
					BetterForgeChat.instance.metadataProvider = null;
					BetterForgeChat.LOGGER.warn("Something went wrong — LuckPerms was reported loaded but its API threw; prefix/suffix will not be shown.\nIf you see this warning please submit an issue report.");
				}
			} else {
				BetterForgeChat.instance.metadataProvider = null;
				BetterForgeChat.LOGGER.info("LuckPerms was not found; prefix/suffix metadata will not be shown.");
			}
	}

	private void loadFtbEssentials() {
		BetterForgeChat.LOGGER.info("Detected loaded status of FTB Essentials: " + ModList.get().isLoaded("ftbessentials"));
		if (ModList.get().isLoaded("ftbessentials")) {
			if (!ConfigHandler.config.enableFtbEssentials.get()) {
				BetterForgeChat.LOGGER.info("FTB Essentials integration was skipped by configuration file!");
				return;
			}
			BetterForgeChat.LOGGER.info("Attempting to integrate FTB Essentials!");
			try {
				BetterForgeChat.instance.nicknameProvider = new FTBNicknameProvider();
				BetterForgeChat.LOGGER.info("FTB Essentials API found and integrated successfully!");
			} catch (Error e2) { // Could have a NoClassDefFoundError here!
				BetterForgeChat.instance.nicknameProvider = null;
				BetterForgeChat.LOGGER.warn("Something went wrong — FTB Essentials was reported loaded but threw; nicknames will not be shown.\nIf you see this warning please submit an issue report.");
			}
		} else {
			BetterForgeChat.instance.nicknameProvider = null;
			BetterForgeChat.LOGGER.info("FTB Essentials was not found; integrated nickname provider will be used (if enabled in config).");
		}
	}
}
