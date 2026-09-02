package com.enviouse.sef.events;

import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.IReloadable;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.gui.protocol.SefGuiRuntime;
import com.enviouse.sef.gui.protocol.SefGuiServer;
import com.enviouse.sef.gui.protocol.SefSessionManager;
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
import net.neoforged.neoforge.event.entity.player.PlayerNegotiationEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import com.enviouse.sef.commands.MsgCommands;
import com.enviouse.sef.chat.AdminChatHandler;
import com.enviouse.sef.chat.ChatMessageManager;
import com.enviouse.sef.vanish.compat.SDLinkHideTracker;
import com.enviouse.sef.teleport.TeleportLifecycleEvents;
import com.enviouse.sef.social.MailCommands;
import com.enviouse.sef.social.ReminderService;
import com.enviouse.sef.automation.AutomationRuntime;
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
				com.enviouse.sef.control.MinecraftServerControlRuntime.logout(sp.getUUID());
				KernelServices.adminLocks().logout(sp.getUUID());
				com.enviouse.sef.control.MentionService.logout(sp.getUUID());
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
		            KernelServices.cooldownDurations().invalidate(sp.getUUID());
		            KernelServices.warmups().clear(sp.getUUID());
		            KernelServices.confirmations().revokeActor(sp.getUUID());
		            KernelServices.observations().clear(sp.getUUID());
		            SefGuiRuntime.departing(sp);
		            SefGuiServer.logout(sp.getUUID());
		            SefGuiServer.untrackPlayer(sp);
		            SefSessionManager.instance().logout(sp.getUUID());
			            KernelServices.disguises().onLogout(sp.getUUID());
	            KernelServices.fancyTags().transfers().logout(sp.getUUID());
		            com.enviouse.sef.disguise.DisguiseProxyService.logout(sp.server, sp.getUUID());
		            com.enviouse.sef.disguise.DisguiseRuntime.logout(sp.getUUID());
	            KernelServices.disguiseProxyIds().releaseObserver(sp.getUUID());
		            KernelServices.disguiseProxyIds().releaseSubject(sp.getUUID());
		            TeleportLifecycleEvents.handleLogout(sp);
		        }
    }

	@SubscribeEvent
	public void onPlayerNegotiation(PlayerNegotiationEvent event) {
		com.enviouse.sef.control.MinecraftServerControlRuntime.negotiate(event);
	}

	@SubscribeEvent
	public void onPlayerLogin(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e) {
			if(e.getEntity() instanceof ServerPlayer sp) {
					com.enviouse.sef.control.MinecraftServerControlRuntime.login(sp);
			            var graveRecovery = KernelServices.graves().reconcilePlayer(sp);
		            if (!graveRecovery.successful()) {
		                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
		                        "A grave claim recovery requires staff review. "
		                                + graveRecovery.detail()));
		                ServerEssentialsForge.LOGGER.error(
		                        "Grave claim recovery failed for player {}. {}",
		                        sp.getUUID(),
			                        graveRecovery.detail());
			            }
			            var escrowRecovery = KernelServices.escrow().reconcilePlayer(sp);
			            if (!escrowRecovery.successful()) {
			                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
			                        "An escrow recovery requires staff review. "
			                                + escrowRecovery.detail()));
			                ServerEssentialsForge.LOGGER.error(
			                        "Escrow recovery failed for player {}. {}",
			                        sp.getUUID(),
			                        escrowRecovery.detail());
			            }
			            boolean firstJoin = KernelServices.profiles().find(sp.getUUID()).isEmpty();
		            KernelServices.profiles().rememberDeferred(
		                    sp.getUUID(),
		                    sp.getGameProfile().getName());
		            SefGuiServer.trackPlayer(sp);
		            com.enviouse.sef.gui.protocol.OfflineActionService.executeReady(
		                    sp.server,
		                    sp.getUUID());
		            com.enviouse.sef.disguise.DisguiseRuntime.cacheProfile(sp);
		            SefSessionManager.instance().bind(sp).ifPresent(session -> {
		                SefGuiServer.sendTagManifest(sp);
		            });
		            SefGuiRuntime.login(sp);
		            SefGuiRuntime.refreshIdentityProjections(sp.server);
		            SefGuiServer.sendDisguiseSnapshot(sp.server);
		            TeleportLifecycleEvents.handleLogin(sp, firstJoin);
		            if (ConfigHandler.config.enableSocialEssentials.get()
		                    && ConfigHandler.config.enableMail.get()) {
		                MailCommands.notifyUnread(sp);
		            }
		            ReminderService.deliverLogin(sp, firstJoin);
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
        AutomationRuntime.tick(e.getServer());
        int interval = Math.max(1, ConfigHandler.config.tabUpdateIntervalTicks.get());
        if (e.getServer().getTickCount() % 20 == 0) {
            ReminderService.deliverScheduled(e.getServer().getPlayerList().getPlayers());
            com.enviouse.sef.gui.protocol.OfflineActionService.executeReady(e.getServer());
        }
        if (e.getServer().getTickCount() % 1200 == 0) {
            KernelServices.graves().cleanupExpiredContainers(e.getServer(), 512);
        }
        if(e.getServer().getTickCount() % interval == 0) {
            TAB_ANIM.tick(e.getServer());
            TabPlaceholderRenderer.applyHeaderFooter(e.getServer());
        }
    }
}
