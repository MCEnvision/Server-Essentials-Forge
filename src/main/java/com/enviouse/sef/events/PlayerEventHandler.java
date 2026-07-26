package com.enviouse.sef.events;

import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.IReloadable;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.tab.TabPlaceholderRenderer;
import com.enviouse.sef.tab.TabAnimationManager;
import com.enviouse.sef.utils.SEFUtilities;
import com.enviouse.sef.ServerEssentialsForge;
import com.mojang.authlib.GameProfile;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.LoadFromFile;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.NameFormat;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.SaveToFile;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.TabListNameFormat;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import com.enviouse.sef.commands.MsgCommands;
import com.enviouse.sef.chat.AdminChatHandler;
import com.enviouse.sef.chat.ChatMessageManager;
import com.enviouse.sef.vanish.compat.SDLinkHideTracker;
import net.neoforged.fml.ModList;

public class PlayerEventHandler implements IReloadable {
	private boolean enableNicknamesInTabList = false;
	private boolean enableMetadataInTabList = false;
	private static final TabAnimationManager TAB_ANIM = new TabAnimationManager();

        @Override
	public void reloadConfigOptions() {
		enableNicknamesInTabList = ConfigHandler.config.enableNicknamesInTabList.get();
		enableMetadataInTabList = ConfigHandler.config.enableMetadataInTabList.get();
	}
	
	@SubscribeEvent
	public void onTabListNameFormatEvent(TabListNameFormat e) {
		ServerEssentialsForge.LOGGER.debug("Tablist formatting");
		if(ConfigHandler.config.enableTabListIntegration.get() && e.getEntity() != null && e.getEntity() instanceof ServerPlayer) {
			ServerEssentialsForge.LOGGER.debug("Tablist formatting enabled");
			GameProfile player = e.getEntity().getGameProfile();
            ServerEssentialsForge.LOGGER.debug("Tablist formatting for: {}", player);
			e.setDisplayName(SEFUtilities.getFormattedPlayerName(player, 
				enableNicknamesInTabList && PermissionsHandler.playerHasPermission(player.getId(), PermissionsHandler.tabListNicknameNode),  
				enableMetadataInTabList  && PermissionsHandler.playerHasPermission(player.getId(), PermissionsHandler.tabListMetadataNode)));
		}
	}

	@SubscribeEvent
	public void onNameFormatEvent(NameFormat e) {
		if(e.getEntity() != null && e.getEntity() instanceof ServerPlayer)
			e.setDisplayname(SEFUtilities.getFormattedPlayerName(e.getEntity().getGameProfile()));
	}

	@SubscribeEvent
	public void onSavePlayerData(SaveToFile e) {
			KernelServices.profiles().requestFlush();
		}

	@SubscribeEvent
	public void onLoadPlayerData(LoadFromFile e){
			ServerEssentialsForge.LOGGER.debug("Loading all Player Data");
			KernelServices.profiles().load(e.getPlayerDirectory());
	}

	@SubscribeEvent
	public void onPlayerLogout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e) {
        if(e.getEntity() instanceof ServerPlayer sp) {
            // Clean up admin chat toggle state
            AdminChatHandler.handleLogout(sp.getUUID());
            // Clean up private msg toggle and /r tracking
            MsgCommands.handleLogout(sp.getUUID());
            // Clean up chat message history
            ChatMessageManager.handleLogout(sp.getUUID());
            // Clear ALL SDLink hide reasons (vanish, admin chat, private msg) to prevent
            // stale hidden-player entries that would keep the player invisible in Discord.
		            SDLinkHideTracker.clearAll(sp.getUUID());
		            com.enviouse.sef.vanish.VanishUtil.forgetPlayer(sp.getUUID());
		            com.enviouse.sef.utils.moddeps.LuckPermsProvider.invalidate(sp.getUUID());
		            KernelServices.warmups().clear(sp.getUUID());
		            KernelServices.confirmations().revokeActor(sp.getUUID());
		        }
    }

	@SubscribeEvent
	public void onPlayerLogin(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e) {
	        if(e.getEntity() instanceof ServerPlayer sp) {
		            KernelServices.profiles().rememberDeferred(
		                    sp.getUUID(),
		                    sp.getGameProfile().getName());
	            // Record login for alt tracking
            if(ConfigHandler.config.enableCheckAlts.get()) {
                com.enviouse.sef.alts.AltTracker tracker = CommandRegistrationHandler.getAltTracker();
                if(tracker != null) {
                    tracker.recordLogin(sp);
                }
            }
        }
    }

	@SubscribeEvent
	public void onServerStarted(ServerStartedEvent e) {
        // Guard LuckPermsProvider.get() — it throws IllegalStateException if LuckPerms is absent.
        // FMLEnvironment.dist is CLIENT on integrated (singleplayer / LAN) servers, so LuckPerms
        // may not be present at all. Always check isLoaded() first.
        net.luckperms.api.LuckPerms luckPerms = null;
        if (ModList.get().isLoaded("luckperms")) {
            try {
                luckPerms = net.luckperms.api.LuckPermsProvider.get();
            } catch (IllegalStateException ex) {
                ServerEssentialsForge.LOGGER.warn("[SEF] LuckPerms detected but API not ready; tab animations will run without it.", ex);
            }
        }
        TAB_ANIM.load(e.getServer(), luckPerms);
        TabPlaceholderRenderer.applyHeaderFooter(e.getServer());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onServerTick(ServerTickEvent.Post e) {
        int interval = Math.max(1, ConfigHandler.config.tabUpdateIntervalTicks.get());
        if(e.getServer().getTickCount() % interval == 0) {
            TAB_ANIM.tick(e.getServer());
            TabPlaceholderRenderer.applyHeaderFooter(e.getServer());
        }
    }
}
