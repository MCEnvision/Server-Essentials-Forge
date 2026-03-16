package com.jeremiahbl.bfcrmod.tab;

import com.jeremiahbl.bfcrmod.BetterForgeChat;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;

/**
 * Manages animated tab-list entries (e.g. rotating group colors from LuckPerms).
 * Accepts a nullable LuckPerms reference — works gracefully without it.
 */
public class TabAnimationManager {
    @Nullable
    private net.luckperms.api.LuckPerms luckPerms;

    public TabAnimationManager() {
    }

    /**
     * Called on server start. LuckPerms may be null if it's not installed.
     */
    public void load(MinecraftServer server, @Nullable net.luckperms.api.LuckPerms luckPerms) {
        this.luckPerms = luckPerms;
        BetterForgeChat.LOGGER.info("[BFCRR] Tab animation manager loaded (LuckPerms: {})",
            luckPerms != null ? "available" : "not available");
    }

    /**
     * Called every server tick. Performs any animated tab-list updates.
     */
    public void tick(MinecraftServer server) {
        // Tab animations can be extended here in the future.
        // Currently a no-op — tab name formatting is handled by PlayerEventHandler.onTabListNameFormatEvent
    }
}

