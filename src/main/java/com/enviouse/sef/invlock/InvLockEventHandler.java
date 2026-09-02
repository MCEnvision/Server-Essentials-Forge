package com.enviouse.sef.invlock;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Event handler that blocks locked players from:
 * - Opening containers
 * - Picking up items
 * - Right-clicking items/blocks (to prevent item usage)
 *
 * GAME-bus handlers (default for @EventBusSubscriber); all events are game-bus.
 */
@EventBusSubscriber(modid = ServerEssentialsForge.MODID)
public class InvLockEventHandler {

    /** Block opening containers (PlayerContainerEvent.Open is not cancelable; close it instead). */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (InvLockManager.isEnforced(player.getUUID())) {
                // Close the container immediately
                player.closeContainer();
                player.sendSystemMessage(TextFormatter.stringToFormattedText(
                    ConfigHandler.config.invLockBlockedMsg.get()));
            }
        }
    }

    /**
     * Block item pickup. NeoForge replaced the cancelable Forge EntityItemPickupEvent with
     * ItemEntityPickupEvent.Pre, which is NOT cancelable — deny by setting canPickup to FALSE.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            if (InvLockManager.isEnforced(player.getUUID())) {
                event.setCanPickup(TriState.FALSE);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemToss(ItemTossEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player
                && InvLockManager.isEnforced(player.getUUID())) {
            event.setCanceled(true);
        }
    }

    /** Block right-click interactions to prevent item usage. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (InvLockManager.isEnforced(player.getUUID())) {
                event.setCanceled(true);
            }
        }
    }

    /** Block right-click blocks to prevent container opening via interact. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (InvLockManager.isEnforced(player.getUUID())) {
                event.setCanceled(true);
            }
        }
    }
}
