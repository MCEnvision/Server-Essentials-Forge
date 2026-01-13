package com.jeremiahbl.bfcrmod.events;

import com.jeremiahbl.bfcrmod.announcements.AnnouncementCommands;
import com.jeremiahbl.bfcrmod.announcements.AnnouncementManager;
import com.jeremiahbl.bfcrmod.chat.ChatReplyHandler;
import com.jeremiahbl.bfcrmod.commands.BfcCommands;
import com.jeremiahbl.bfcrmod.commands.MsgCommands;
import com.jeremiahbl.bfcrmod.config.ConfigHandler;
import com.jeremiahbl.bfcrmod.filter.FilterManager;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class CommandRegistrationHandler {
    private static final AnnouncementManager ANNOUNCEMENT_MANAGER = new AnnouncementManager();
    private static final FilterManager FILTER_MANAGER = new FilterManager();

    public static AnnouncementManager getAnnouncementManager() { return ANNOUNCEMENT_MANAGER; }
    public static FilterManager getFilterManager() { return FILTER_MANAGER; }

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
    }
}
