package com.jeremiahbl.bfcrmod;

import org.slf4j.Logger;

import com.jeremiahbl.bfcrmod.commands.NickCommands;
import com.jeremiahbl.bfcrmod.chat.ChatMessageManager;
import com.jeremiahbl.bfcrmod.config.ConfigHandler;
import com.jeremiahbl.bfcrmod.config.ConfigurationEventHandler;
import com.jeremiahbl.bfcrmod.config.PermissionsHandler;
import com.jeremiahbl.bfcrmod.events.ChatEventHandler;
import com.jeremiahbl.bfcrmod.events.CommandRegistrationHandler;
import com.jeremiahbl.bfcrmod.events.ExternalModLoadingEvent;
import com.jeremiahbl.bfcrmod.events.PlayerEventHandler;
import com.jeremiahbl.bfcrmod.tab.TabAnimationManager;
import com.jeremiahbl.bfcrmod.utils.BetterForgeChatUtilities;
import com.jeremiahbl.bfcrmod.utils.IMetadataProvider;
import com.jeremiahbl.bfcrmod.utils.INicknameProvider;
import com.jeremiahbl.bfcrmod.utils.loader;
import com.mojang.logging.LogUtils;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(BetterForgeChat.MODID )
public class BetterForgeChat {
	public static final String CHAT_ID_STR = 
			"&cBetter &9&lForge&r &eChat&r &d(c) Jeremiah Lowe, Disa Kandria 2022-2026&r\n";
	public static final String MODID = "bfcrmod";
	public static final String VERSION = "V4.0.0";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static BetterForgeChat instance;
	
    public IMetadataProvider metadataProvider = null;
    public INicknameProvider nicknameProvider = null;
    
    private final ChatEventHandler chatHandler = new ChatEventHandler();
    private final ExternalModLoadingEvent modLoadingEvent = new ExternalModLoadingEvent();
    private final PlayerEventHandler playerEventHandler = new PlayerEventHandler();
    private final PermissionsHandler permissionsHandler = new PermissionsHandler();
    private final CommandRegistrationHandler commandRegistrator = new CommandRegistrationHandler();
    private final ConfigurationEventHandler configurationHandler = new ConfigurationEventHandler();

    @SuppressWarnings("removal")
    public BetterForgeChat() {
        instance = this;
        ConfigurationEventHandler.registerReloadable(playerEventHandler);
        ConfigurationEventHandler.registerReloadable(chatHandler);
        ConfigurationEventHandler.registerReloadable(() -> {
            NickCommands.reloadConfig();
            BetterForgeChatUtilities.reloadConfig();
        });
        ConfigurationEventHandler.registerReloadable(() -> BetterForgeChat.LOGGER.info("Configuration options loaded!"));
        loader.register(configurationHandler);
        
        loader.MlContext(true); // Set mod to ignore whether the other side has it (we only care about the server side - but this works on the client's side too)
        loader.MLConfig("COMMON", ConfigHandler.spec);//set config type and config to register
        loader.register(permissionsHandler); //register permissions handler

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete); // Register mod loading completed event
        loader.register(this); // Register the mod itself
        loader.register(commandRegistrator); // Register commands
    }
    private void loadComplete(FMLLoadCompleteEvent e) {
        loader.register(chatHandler);
        loader.register(modLoadingEvent);
        loader.register(playerEventHandler);
    }

    // Server lifecycle hooks
    @SubscribeEvent
    public void onServerStarted(net.minecraftforge.event.server.ServerStartedEvent ev) {
        if(ConfigHandler.config.enableAnnouncements.get()) {
            CommandRegistrationHandler.getAnnouncementManager().load(ev.getServer());
        }
        if(ConfigHandler.config.enableFilterSystem.get()) {
            CommandRegistrationHandler.getFilterManager().load(ev.getServer());
        }
        if(ConfigHandler.config.enableChatReplies.get()) {
            ChatMessageManager.init(ev.getServer());
        }
    }
    @SubscribeEvent
    public void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent ev) {
        if(ev.phase == net.minecraftforge.event.TickEvent.Phase.END && ConfigHandler.config.enableAnnouncements.get())
            CommandRegistrationHandler.getAnnouncementManager().tick(ev.getServer());
    }
}
