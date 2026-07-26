package com.enviouse.sef.disablebuilding;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import com.enviouse.sef.ServerEssentialsForge;

/**
 * Event handler that blocks building-disabled players from:
 * - Breaking blocks
 * - Placing blocks
 * - Using block interactions that could result in placement
 */
@EventBusSubscriber(modid = ServerEssentialsForge.MODID)
public class DisableBuildingEventHandler {

    /** Block breaking blocks */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!enabled()) return;
        if (event.getPlayer() instanceof ServerPlayer player) {
            if (DisableBuildingManager.isDisabled(player.getUUID())) {
                event.setCanceled(true);
                player.sendSystemMessage(TextFormatter.stringToFormattedText(
                    ConfigHandler.config.dbBlockedMsg.get()));
            }
        }
    }

    /** Block placing blocks */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!enabled()) return;
        if (event.getEntity() instanceof ServerPlayer player) {
            if (DisableBuildingManager.isDisabled(player.getUUID())) {
                event.setCanceled(true);
                player.sendSystemMessage(TextFormatter.stringToFormattedText(
                    ConfigHandler.config.dbBlockedMsg.get()));
            }
        }
    }

    /** Block left-click on blocks (mining) */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!enabled()) return;
        if (event.getEntity() instanceof ServerPlayer player) {
            if (DisableBuildingManager.isDisabled(player.getUUID())) {
                event.setCanceled(true);
            }
        }
    }
    private static boolean enabled() {
        return ConfigHandler.config.enableDisableBuilding.get()
                || ConfigHandler.config.enableModerationEssentials.get();
    }
}

