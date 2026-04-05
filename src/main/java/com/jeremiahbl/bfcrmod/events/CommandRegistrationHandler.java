package com.jeremiahbl.bfcrmod.events;

import com.jeremiahbl.bfcrmod.alts.AltTracker;
import com.jeremiahbl.bfcrmod.alts.CheckAltsCommand;
import com.jeremiahbl.bfcrmod.announcements.AnnouncementCommands;
import com.jeremiahbl.bfcrmod.announcements.AnnouncementManager;
import com.jeremiahbl.bfcrmod.banned.BannedItemsCommands;
import com.jeremiahbl.bfcrmod.banned.BannedItemsManager;
import com.jeremiahbl.bfcrmod.chat.AdminChatHandler;
import com.jeremiahbl.bfcrmod.chat.ChatReplyHandler;
import com.jeremiahbl.bfcrmod.chat.OpBulletinHandler;
import com.jeremiahbl.bfcrmod.clearchat.ClearChatCommand;
import com.jeremiahbl.bfcrmod.commands.BfcCommands;
import com.jeremiahbl.bfcrmod.commands.MsgCommands;
import com.jeremiahbl.bfcrmod.config.ConfigHandler;
import com.jeremiahbl.bfcrmod.disablebuilding.DisableBuildingCommand;
import com.jeremiahbl.bfcrmod.filter.FilterManager;
import com.jeremiahbl.bfcrmod.freeze.FreezeCommand;
import com.jeremiahbl.bfcrmod.invlock.InvLockCommand;
import com.jeremiahbl.bfcrmod.invsee.InvSeeCommand;
import com.jeremiahbl.bfcrmod.motd.MotdCommands;
import com.jeremiahbl.bfcrmod.motd.MotdManager;
import com.jeremiahbl.bfcrmod.sudo.SudoCommand;
import com.jeremiahbl.bfcrmod.warn.WarnCommand;
import com.jeremiahbl.bfcrmod.warn.WarnManager;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class CommandRegistrationHandler {
    private static final AnnouncementManager ANNOUNCEMENT_MANAGER = new AnnouncementManager();
    private static final FilterManager FILTER_MANAGER = new FilterManager();
    private static final BannedItemsManager BANNED_ITEMS_MANAGER = new BannedItemsManager();
    private static final AltTracker ALT_TRACKER = new AltTracker();
    private static final WarnManager WARN_MANAGER = new WarnManager();
    private static MotdManager MOTD_MANAGER = null;

    public static AnnouncementManager getAnnouncementManager() { return ANNOUNCEMENT_MANAGER; }
    public static FilterManager getFilterManager() { return FILTER_MANAGER; }
    public static BannedItemsManager getBannedItemsManager() { return BANNED_ITEMS_MANAGER; }
    public static MotdManager getMotdManager() { return MOTD_MANAGER; }
    public static AltTracker getAltTracker() { return ALT_TRACKER; }
    public static WarnManager getWarnManager() { return WARN_MANAGER; }

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

        // Register /freeze and /unfreeze commands if enabled
        if(ConfigHandler.config.enableFreezeSystem.get()) {
            FreezeCommand.register(e.getDispatcher());
        }

        // Register /cc and /clearchat commands if enabled
        if(ConfigHandler.config.enableClearChat.get()) {
            ClearChatCommand.register(e.getDispatcher());
        }

        // Register /sudo command if enabled
        if(ConfigHandler.config.enableSudo.get()) {
            SudoCommand.register(e.getDispatcher());
        }

        // Register /invlock command if enabled
        if(ConfigHandler.config.enableInvLock.get()) {
            InvLockCommand.register(e.getDispatcher());
        }

        // Register /disablebuilding and /db commands if enabled
        if(ConfigHandler.config.enableDisableBuilding.get()) {
            DisableBuildingCommand.register(e.getDispatcher());
        }

        // Register /checkalts command if enabled
        if(ConfigHandler.config.enableCheckAlts.get()) {
            CheckAltsCommand.register(e.getDispatcher());
        }

        // Register /warn and /warns commands if enabled
        if(ConfigHandler.config.enableWarnSystem.get()) {
            WarnCommand.register(e.getDispatcher());
        }
    }

    /**
     * Register InvSee at LOW priority so it runs AFTER FTB Essentials'
     * command registration, allowing us to override their /invsee node.
     */
    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW)
    public void registerInvSeeCommand(RegisterCommandsEvent e) {
        // Register custom /invsee command if enabled
        if(ConfigHandler.config.enableInvSee.get()) {
            InvSeeCommand.register(e.getDispatcher());
        }
    }
}
