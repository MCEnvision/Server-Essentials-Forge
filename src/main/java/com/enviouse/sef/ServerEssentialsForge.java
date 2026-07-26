package com.enviouse.sef;

import org.slf4j.Logger;

import com.enviouse.sef.audit.SecurityAuditService;
import com.enviouse.sef.chat.ChatMessageManager;
import com.enviouse.sef.chat.OpBulletinHandler;
import com.enviouse.sef.commands.NickCommands;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.ConfigurationEventHandler;
import com.enviouse.sef.events.ChatEventHandler;
import com.enviouse.sef.events.CommandRegistrationHandler;
import com.enviouse.sef.events.ExternalModLoadingEvent;
import com.enviouse.sef.events.PlayerEventHandler;
import com.enviouse.sef.freeze.FreezeManager;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.permissions.PermissionManifest;
import com.enviouse.sef.storage.StorageExportService;
import com.enviouse.sef.utils.IMetadataProvider;
import com.enviouse.sef.utils.INicknameProvider;
import com.enviouse.sef.utils.SEFUtilities;
import com.enviouse.sef.utils.loader;
import com.enviouse.sef.vanish.VanishConfig;
import com.enviouse.sef.vanish.VanishCommand;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.minecraft.world.level.storage.LevelResource;

// Server Essentials Forge (SEF) — NeoForge 1.21.1 port.
// Server-side only (neoforge.mods.toml: displayTest = "IGNORE_SERVER_VERSION").
@Mod(ServerEssentialsForge.MODID)
public class ServerEssentialsForge {
    public static final String CHAT_ID_STR =
            "&e&lServer&6Essentials&bForge&r &d(c) EnVy 2022-2026&r\n";
    public static final String MODID = "sef";
    public static final String VERSION = "V1.1";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static ServerEssentialsForge instance;

    // Vanish compat detection flags (set in onServerStarted).
    public static boolean mc2discordDetected = false;
    public static boolean playtimeDetected = false;
    public static boolean sdlinkDetected = false;

    // Integration providers — populated by ExternalModLoadingEvent; null when absent.
    public IMetadataProvider metadataProvider = null;
    public INicknameProvider nicknameProvider = null;

    // These handlers hold per-instance state (IReloadable caches, manager refs) and/or have
    // NON-static @SubscribeEvent methods, so they must be registered as INSTANCES on the game bus
    // (NeoForge.EVENT_BUS) — @EventBusSubscriber would only wire static handlers, silently dropping them.
    private final ChatEventHandler chatHandler = new ChatEventHandler();
    private final ExternalModLoadingEvent modLoadingEvent = new ExternalModLoadingEvent();
    private final PlayerEventHandler playerEventHandler = new PlayerEventHandler();
    private final CommandRegistrationHandler commandRegistrator = new CommandRegistrationHandler();

    public ServerEssentialsForge(IEventBus modEventBus, ModContainer modContainer) {
        instance = this;

        ConfigurationEventHandler.registerReloadable(playerEventHandler);
        ConfigurationEventHandler.registerReloadable(chatHandler);
        ConfigurationEventHandler.registerReloadable(() -> {
            NickCommands.reloadConfig();
            SEFUtilities.reloadConfig();
            KernelServices.reloadConfiguration();
            if (metadataProvider != null) {
                metadataProvider.invalidateCache();
            }
            var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                server.execute(() -> {
                    KernelServices.fileLogs().reload();
                    com.enviouse.sef.vanish.VanishUtil.recheckAll(server);
                    com.enviouse.sef.vanish.VanishUtil.refreshAllVisibility(server);
                });
            }
        });
        ConfigurationEventHandler.registerReloadable(() -> LOGGER.info("Configuration options loaded!"));

        loader.registerConfig(modContainer, "COMMON", ConfigHandler.spec, "sef/common.toml");
        loader.registerConfig(modContainer, "SERVER", VanishConfig.SERVER_SPEC, "sef-vanish-server.toml");

        // Instance registrations on the GAME bus (see field comment above).
        NeoForge.EVENT_BUS.register(this);              // server lifecycle (onServerStarted/Tick/Stopping)
        NeoForge.EVENT_BUS.register(chatHandler);       // ServerChatEvent — the chat formatting hook
        NeoForge.EVENT_BUS.register(playerEventHandler);// tab/name/login/logout/tick
        NeoForge.EVENT_BUS.register(modLoadingEvent);   // optional-integration detection
        NeoForge.EVENT_BUS.register(commandRegistrator);// RegisterCommandsEvent
        NeoForge.EVENT_BUS.addListener(this::registerVanishCommands);

        LOGGER.info("ServerEssentialsForge (NeoForge 1.21.1 port) initialized");
    }

    public void registerVanishCommands(RegisterCommandsEvent event) {
        if (!ConfigHandler.config.enableVanishSystem.get()) return;
        VanishCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent ev) {
        java.nio.file.Path sefDataDirectory = ev.getServer().getServerDirectory()
                .resolve("serverconfig")
                .resolve("sef");
        KernelServices.initialize();
        KernelServices.startStorage(sefDataDirectory);
        KernelServices.profiles().load(
                ev.getServer().getWorldPath(LevelResource.PLAYER_DATA_DIR).toFile());
        SecurityAuditService.start(
                sefDataDirectory,
                ConfigHandler.config.securityAuditRetentionDays.get(),
                ConfigHandler.config.securityAuditMaximumFileMiB.get());
        if (!KernelServices.fileLogs().startConfigured(ev.getServer().getServerDirectory())) {
            LOGGER.error("[SEF] Optional file logging could not be initialized");
        }
        StorageExportService.start();
        try {
            PermissionManifest.writeRuntimeManifest(sefDataDirectory.resolve("permission-manifest.json"));
        } catch (java.io.IOException exception) {
            LOGGER.error("[SEF] Failed to write the runtime permission manifest", exception);
        }
        if (ConfigHandler.config.enableChatReplies.get() && !ConfigHandler.config.enableChatFormatting.get())
            LOGGER.warn("[SEF] click_to_respond is enabled but chat_formatting is disabled — clickable replies and /ans require chat_formatting=true to function.");
        if (ConfigHandler.config.enableAnnouncements.get())
            CommandRegistrationHandler.getAnnouncementManager().load(ev.getServer());
        if (ConfigHandler.config.enableFilterSystem.get())
            CommandRegistrationHandler.getFilterManager().load(ev.getServer());
        if (ConfigHandler.config.enableChatReplies.get())
            ChatMessageManager.init(ev.getServer());
        if (ConfigHandler.config.enableOpBulletin.get())
            OpBulletinHandler.init(ev.getServer());
        if (ConfigHandler.config.enableBannedItems.get())
            CommandRegistrationHandler.getBannedItemsManager().load(ev.getServer());
        if (ConfigHandler.config.enableMotdSystem.get()) {
            // getServerDirectory() returns a Path in 1.21.1 (was File). Keep the singleplayer/LAN fallback.
            java.nio.file.Path serverDir = ev.getServer().getServerDirectory();
            java.nio.file.Path configDir = serverDir != null
                    ? serverDir.resolve("config").resolve("sef")
                    : FMLPaths.CONFIGDIR.get().resolve("sef");
            CommandRegistrationHandler.initMotdManager(configDir);
            if (ConfigHandler.config.applyMotdOnStartup.get() && ev.getServer().isDedicatedServer())
                CommandRegistrationHandler.getMotdManager().applyToServer(ev.getServer());
        }
        if (ConfigHandler.config.enableCheckAlts.get())
            CommandRegistrationHandler.getAltTracker().load(ev.getServer());
        if (ConfigHandler.config.enableWarnSystem.get()
                && !ConfigHandler.config.enableModerationEssentials.get())
            CommandRegistrationHandler.getWarnManager().load(ev.getServer());
        if (ConfigHandler.config.enableMuteSystem.get()
                && !ConfigHandler.config.enableModerationEssentials.get())
            CommandRegistrationHandler.getMuteManager().load(ev.getServer());

        if (ModList.get().isLoaded("mc2discord")) mc2discordDetected = true;
        if (ModList.get().isLoaded("playtime")) playtimeDetected = true;
        if (ModList.get().isLoaded("sdlink")) sdlinkDetected = true;
    }

    // ServerTickEvent.Post == the old TickEvent.ServerTickEvent END phase.
    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post ev) {
        if (ConfigHandler.config.enableAnnouncements.get())
            CommandRegistrationHandler.getAnnouncementManager().tick(ev.getServer());
        if (ConfigHandler.config.enableBannedItems.get())
            CommandRegistrationHandler.getBannedItemsManager().tick(ev.getServer(), ev.getServer().getTickCount());
        if (ConfigHandler.config.enableFreezeSystem.get()
                || ConfigHandler.config.enableModerationEssentials.get())
            FreezeManager.tick(ev.getServer());
        if (ConfigHandler.config.enableMuteSystem.get()
                && !ConfigHandler.config.enableModerationEssentials.get())
            CommandRegistrationHandler.getMuteManager().tick(ev.getServer());
        com.enviouse.sef.moderation.ModerationEvents.tick(ev.getServer());
        com.enviouse.sef.player.PlayerStateService.tick(ev.getServer());
        if (ConfigHandler.config.enableCountdown.get())
            com.enviouse.sef.countdown.CountdownManager.tick(ev.getServer());
        if (ConfigHandler.config.enableTeleportEssentials.get()
                && ev.getServer().getTickCount() % 20 == 0) {
            KernelServices.teleportRequests().expire();
            KernelServices.teleports().purgeExpired(java.time.Instant.now());
        }
        if (ConfigHandler.config.enableTeleportEssentials.get())
            com.enviouse.sef.teleport.TeleportWarmupManager.tick(ev.getServer());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent ev) {
        FreezeManager.clear();
        com.enviouse.sef.invlock.InvLockManager.clear();
        com.enviouse.sef.disablebuilding.DisableBuildingManager.clear();
        if (ConfigHandler.config.enableMuteSystem.get()
                && !ConfigHandler.config.enableModerationEssentials.get()) {
            CommandRegistrationHandler.getMuteManager().shutdown();
        }
        CommandRegistrationHandler.getBannedItemsManager().shutdown();
        CommandRegistrationHandler.getAltTracker().shutdown();
        com.enviouse.sef.countdown.CountdownManager.clear();
        com.enviouse.sef.player.PlayerStateService.clearAll();
        com.enviouse.sef.vanish.VanishUtil.clearRuntimeState();
        com.enviouse.sef.vanish.misc.SoundSuppressionHelper.clear();
        com.enviouse.sef.teleport.TeleportWarmupManager.cancelAll(
                com.enviouse.sef.kernel.policy.WarmupService.CancelReason.FEATURE_DISABLE);
        ExternalModLoadingEvent.stopOptionalIntegrations();
        if (!KernelServices.profiles().shutdown()) {
            LOGGER.error("[SEF] Player profile shutdown flush did not complete");
        }
        var kernelShutdown = KernelServices.shutdown();
        if (!kernelShutdown.successful()) {
            LOGGER.error(
                    "[SEF] Kernel shutdown flush failed for repositories {}",
                    kernelShutdown.failedRepositoryIds());
        }
        StorageExportService.shutdown();
        SecurityAuditService.shutdown();
    }
}
