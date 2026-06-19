package com.enviouse.sef;

import org.slf4j.Logger;

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
import com.enviouse.sef.utils.IMetadataProvider;
import com.enviouse.sef.utils.INicknameProvider;
import com.enviouse.sef.utils.SEFUtilities;
import com.enviouse.sef.utils.loader;
import com.enviouse.sef.vanish.VanishConfig;
import com.enviouse.sef.vanish.VanishCommand;
import com.enviouse.sef.vanish.Vanishmod;
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
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

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
        NeoForge.EVENT_BUS.addListener(this::registerVanishPermissions);

        LOGGER.info("ServerEssentialsForge (NeoForge 1.21.1 port) initialized");
    }

    public void registerVanishCommands(RegisterCommandsEvent event) {
        VanishCommand.register(event.getDispatcher());
    }

    public void registerVanishPermissions(PermissionGatherEvent.Nodes event) {
        // sef.vanishsee.1..3
        for (int level = 1; level <= 3; level++) {
            PermissionNode<Boolean> node = new PermissionNode<>(MODID, "vanishsee." + level,
                    PermissionTypes.BOOLEAN, (player, uuid, context) -> false);
            Vanishmod.VANISH_SEE_NODES.put(level, node);
            event.addNodes(node);
        }
        // sef.vanish.1..3
        for (int level = 1; level <= 3; level++) {
            PermissionNode<Boolean> node = new PermissionNode<>(MODID, "vanish." + level,
                    PermissionTypes.BOOLEAN, (player, uuid, context) -> false);
            Vanishmod.VANISH_LEVEL_NODES.put(level, node);
            event.addNodes(node);
        }
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent ev) {
        if (ConfigHandler.config.enableAnnouncements.get())
            CommandRegistrationHandler.getAnnouncementManager().load(ev.getServer());
        if (ConfigHandler.config.enableFilterSystem.get())
            CommandRegistrationHandler.getFilterManager().load(ev.getServer());
        if (ConfigHandler.config.enableChatReplies.get())
            ChatMessageManager.init(ev.getServer());
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
        if (ConfigHandler.config.enableWarnSystem.get())
            CommandRegistrationHandler.getWarnManager().load(ev.getServer());
        if (ConfigHandler.config.enableMuteSystem.get())
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
        if (ConfigHandler.config.enableFreezeSystem.get())
            FreezeManager.tick(ev.getServer());
        if (ConfigHandler.config.enableMuteSystem.get())
            CommandRegistrationHandler.getMuteManager().tick(ev.getServer());
        if (ConfigHandler.config.enableCountdown.get())
            com.enviouse.sef.countdown.CountdownManager.tick(ev.getServer());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent ev) {
        FreezeManager.clear();
        com.enviouse.sef.invlock.InvLockManager.clear();
        com.enviouse.sef.disablebuilding.DisableBuildingManager.clear();
        if (ConfigHandler.config.enableMuteSystem.get())
            CommandRegistrationHandler.getMuteManager().shutdown();
        if (ConfigHandler.config.enableBannedItems.get())
            CommandRegistrationHandler.getBannedItemsManager().save();
        com.enviouse.sef.countdown.CountdownManager.clear();
        com.enviouse.sef.vanish.VanishUtil.VANISHED_PLAYERS.clear();
    }
}
