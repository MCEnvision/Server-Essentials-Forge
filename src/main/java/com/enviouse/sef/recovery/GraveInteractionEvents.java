package com.enviouse.sef.recovery;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.kernel.KernelServices;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

import java.time.Instant;

@EventBusSubscriber(modid = ServerEssentialsForge.MODID)
public final class GraveInteractionEvents {
    private GraveInteractionEvents() {
    }

    @SubscribeEvent
    public static void onUse(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        GraveRepository.GraveRecord grave = KernelServices.graves()
                .physicalAt(player.serverLevel(), event.getPos())
                .orElse(null);
        if (grave == null) {
            return;
        }
        event.setCanceled(true);
        if (grave.claimed() || !grave.expiresAt().isAfter(Instant.now())) {
            KernelServices.graves().cleanupExpiredContainers(player.server, 512);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "This grave has expired or was already claimed."));
            return;
        }
        if (!grave.ownerId().equals(player.getUUID())) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "This grave is protected. Staff can manage it with the grave command."));
            return;
        }
        player.server.getCommands().performPrefixedCommand(
                player.createCommandSourceStack(),
                "grave claim " + grave.id() + " " + grave.revision());
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        if (KernelServices.graves().physicalAt(player.serverLevel(), event.getPos()).isPresent()) {
            event.setCanceled(true);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "Use this grave to claim it. Staff can manage it with the grave command."));
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof net.minecraft.server.level.ServerLevel level)) {
            return;
        }
        event.getAffectedBlocks().removeIf(position ->
                KernelServices.graves().physicalAt(level, position).isPresent());
    }
}
