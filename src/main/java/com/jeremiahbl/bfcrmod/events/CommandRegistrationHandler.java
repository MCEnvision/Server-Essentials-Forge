package com.jeremiahbl.bfcrmod.events;

import com.jeremiahbl.bfcrmod.announcements.AnnouncementCommands;
import com.jeremiahbl.bfcrmod.announcements.AnnouncementManager;
import com.jeremiahbl.bfcrmod.banned.BannedItemsCommands;
import com.jeremiahbl.bfcrmod.banned.BannedItemsManager;
import com.jeremiahbl.bfcrmod.chat.AdminChatHandler;
import com.jeremiahbl.bfcrmod.chat.ChatReplyHandler;
import com.jeremiahbl.bfcrmod.chat.OpBulletinHandler;
import com.jeremiahbl.bfcrmod.commands.BfcCommands;
import com.jeremiahbl.bfcrmod.commands.MsgCommands;
import com.jeremiahbl.bfcrmod.config.ConfigHandler;
import com.jeremiahbl.bfcrmod.filter.FilterManager;
import com.jeremiahbl.bfcrmod.motd.MotdCommands;
import com.jeremiahbl.bfcrmod.motd.MotdManager;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class CommandRegistrationHandler {
    private static final AnnouncementManager ANNOUNCEMENT_MANAGER = new AnnouncementManager();
    private static final FilterManager FILTER_MANAGER = new FilterManager();
    private static final BannedItemsManager BANNED_ITEMS_MANAGER = new BannedItemsManager();
    private static MotdManager MOTD_MANAGER = null;

    public static AnnouncementManager getAnnouncementManager() { return ANNOUNCEMENT_MANAGER; }
    public static FilterManager getFilterManager() { return FILTER_MANAGER; }
    public static BannedItemsManager getBannedItemsManager() { return BANNED_ITEMS_MANAGER; }
    public static MotdManager getMotdManager() { return MOTD_MANAGER; }

    public static void initMotdManager(java.nio.file.Path configDir) {
        MOTD_MANAGER = new MotdManager(configDir);
        MOTD_MANAGER.load();
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent e) {
        // Always register base commands
        BfcCommands.register(e.getDispatcher());

        // Register filter commands if enabled
        if(ConfigHandler.config.enableFilterSystem.get()) {
            BfcCommands.initFilterManager(FILTER_MANAGER);
        }

        // Register messaging commands if enabled
        if(ConfigHandler.config.enableMessagingSystem.get()) {
            MsgCommands.register(e.getDispatcher());
        }

        // Register announcement commands if enabled
        if(ConfigHandler.config.enableAnnouncements.get()) {
            AnnouncementCommands.register(e.getDispatcher(), ANNOUNCEMENT_MANAGER);
        }

        // Register /ans chat reply command if enabled
        if(ConfigHandler.config.enableChatReplies.get()) {
            ChatReplyHandler.register(e.getDispatcher());
        }

        // Register /helpop and /chat admin commands
        AdminChatHandler.register(e.getDispatcher());

        // Register /opbulletin command
        OpBulletinHandler.register(e.getDispatcher());

        // Register /banned commands if enabled
        if(ConfigHandler.config.enableBannedItems.get()) {
            BannedItemsCommands.setManager(BANNED_ITEMS_MANAGER);
            BannedItemsCommands.register(e.getDispatcher());
        }

        // Register MOTD commands - always register, manager will be set later
        if(ConfigHandler.config.enableMotdSystem.get()) {
            MotdCommands.register(e.getDispatcher());
        }
    }
}
