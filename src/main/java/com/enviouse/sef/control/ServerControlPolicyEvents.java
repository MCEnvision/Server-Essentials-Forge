package com.enviouse.sef.control;

import com.enviouse.sef.ServerEssentialsForge;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = ServerEssentialsForge.MODID)
public final class ServerControlPolicyEvents {
    private ServerControlPolicyEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCommand(CommandEvent event) {
        MinecraftServerControlRuntime.allowCommand(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player
                && !MinecraftServerControlRuntime.allowMovementAction(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && !MinecraftServerControlRuntime.allowMovementAction(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && !MinecraftServerControlRuntime.allowMovementAction(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player
                && !MinecraftServerControlRuntime.allowMovementAction(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemInteract(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof ServerPlayer player
                && !MinecraftServerControlRuntime.allowMovementAction(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity() instanceof ServerPlayer player
                && !MinecraftServerControlRuntime.allowMovementAction(player)) {
            event.setCanceled(true);
        }
    }
}
