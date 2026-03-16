package com.jeremiahbl.bfcrmod.events;

import com.jeremiahbl.bfcrmod.config.ConfigHandler;
import com.jeremiahbl.bfcrmod.config.IReloadable;
import com.jeremiahbl.bfcrmod.config.PermissionsHandler;
import com.jeremiahbl.bfcrmod.config.PlayerData;
import com.jeremiahbl.bfcrmod.tab.TabPlaceholderRenderer;
import com.jeremiahbl.bfcrmod.tab.TabAnimationManager;
import com.jeremiahbl.bfcrmod.utils.BetterForgeChatUtilities;
import com.jeremiahbl.bfcrmod.BetterForgeChat;
import com.mojang.authlib.GameProfile;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent.LoadFromFile;
import net.minecraftforge.event.entity.player.PlayerEvent.NameFormat;
import net.minecraftforge.event.entity.player.PlayerEvent.SaveToFile;
import net.minecraftforge.event.entity.player.PlayerEvent.TabListNameFormat;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.TickEvent;

import com.jeremiahbl.bfcrmod.commands.MsgCommands;
import com.jeremiahbl.bfcrmod.chat.ChatMessageManager;
import net.minecraftforge.fml.ModList;

@EventBusSubscriber
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
		BetterForgeChat.LOGGER.debug("Tablist formatting");
		if(ConfigHandler.config.enableTabListIntegration.get() && e.getEntity() != null && e.getEntity() instanceof ServerPlayer) {
			BetterForgeChat.LOGGER.debug("Tablist formatting enabled");
			GameProfile player = e.getEntity().getGameProfile();
            BetterForgeChat.LOGGER.debug("Tablist formatting for: {}", player);
			e.setDisplayName(BetterForgeChatUtilities.getFormattedPlayerName(player, 
				enableNicknamesInTabList && PermissionsHandler.playerHasPermission(player.getId(), PermissionsHandler.tabListNicknameNode),  
				enableMetadataInTabList  && PermissionsHandler.playerHasPermission(player.getId(), PermissionsHandler.tabListMetadataNode)));
		}
	}

	@SubscribeEvent
	public void onNameFormatEvent(NameFormat e) {
		if(e.getEntity() != null && e.getEntity() instanceof ServerPlayer)
			e.setDisplayname(BetterForgeChatUtilities.getFormattedPlayerName(e.getEntity().getGameProfile()));
	}

	@SubscribeEvent
	public void onSavePlayerData(SaveToFile e) {
		BetterForgeChat.LOGGER.debug("saving all Player Data");
		PlayerData.saveToDir(e.getPlayerDirectory());
	}

	@SubscribeEvent
	public void onLoadPlayerData(LoadFromFile e){
		BetterForgeChat.LOGGER.debug("Loading all Player Data");
		PlayerData.loadFromDir(e.getPlayerDirectory());
	}

	@SubscribeEvent
	public void onPlayerLogout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e) {
        if(e.getEntity() instanceof ServerPlayer sp) {
            MsgCommands.handleLogout(sp.getUUID());
            ChatMessageManager.handleLogout(sp.getUUID());
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
                BetterForgeChat.LOGGER.warn("[BFCRR] LuckPerms detected but API not ready; tab animations will run without it.", ex);
            }
        }
        TAB_ANIM.load(e.getServer(), luckPerms);
        TabPlaceholderRenderer.applyHeaderFooter(e.getServer());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onServerTick(TickEvent.ServerTickEvent e) {
        if(e.phase != TickEvent.Phase.END) return;
        TAB_ANIM.tick(e.getServer());
        TabPlaceholderRenderer.applyHeaderFooter(e.getServer());
    }
}
