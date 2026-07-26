package com.enviouse.sef.teleport;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.WarmupService;
import com.enviouse.sef.storage.repository.LocationHistoryRepository;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.time.Instant;

@EventBusSubscriber(modid = ServerEssentialsForge.MODID)
public final class TeleportLifecycleEvents {
    private TeleportLifecycleEvents() {
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        SavedLocation location = SavedLocation.from(player);
        KernelServices.locationHistory().record(player.getUUID(), new LocationHistoryRepository.LocationRecord(
                location.dimensionId(),
                location.x(),
                location.y(),
                location.z(),
                location.yaw(),
                location.pitch(),
                Instant.now(),
                "death"));
        TeleportWarmupManager.cancel(player, WarmupService.CancelReason.DEATH);
    }

    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && KernelServices.teleportSettings().cancelOnDamage()) {
            TeleportWarmupManager.cancel(player, WarmupService.CancelReason.DAMAGE);
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TeleportWarmupManager.cancel(player, WarmupService.CancelReason.DIMENSION_CHANGE);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        KernelServices.teleports().spawn("death").ifPresent(spawn ->
                teleportLifecycle(player, spawn.location(), "death_spawn", spawn.revision()));
    }

    public static void handleLogin(ServerPlayer player, boolean firstJoin) {
        TeleportRepository repository = KernelServices.teleports();
        TeleportRepository.PendingOfflineTeleport pending =
                repository.pendingOfflineTeleport(player.getUUID()).orElse(null);
        if (pending != null) {
            SafeTeleportService.TeleportResult result = KernelServices.safeTeleports().teleport(
                    player.getServer(),
                    player,
                    player,
                    pending.location(),
                    pending.reason(),
                    lifecyclePolicy(),
                    () -> repository.pendingOfflineTeleport(player.getUUID())
                            .map(current -> current.revision() == pending.revision())
                            .orElse(false));
            if (result.successful()) {
                repository.clearOfflineTeleport(player.getUUID(), pending.revision());
            } else {
                ServerEssentialsForge.LOGGER.warn(
                        "[SEF] Deferred teleport for {} was retained because validation returned {}",
                        player.getGameProfile().getName(),
                        result.code());
            }
            return;
        }
        if (firstJoin) {
            repository.spawn("first_join").ifPresent(spawn ->
                    teleportLifecycle(player, spawn.location(), "first_join_spawn", spawn.revision()));
        }
    }

    public static void handleLogout(ServerPlayer player) {
        TeleportWarmupManager.cancel(player, WarmupService.CancelReason.LOGOUT);
        KernelServices.teleportRequests().invalidatePlayer(
                player.getUUID(),
                TeleportRequestService.State.INVALIDATED);
    }

    private static void teleportLifecycle(
            ServerPlayer player,
            SavedLocation location,
            String reason,
            long revision
    ) {
        KernelServices.safeTeleports().teleport(
                player.getServer(),
                player,
                player,
                location,
                reason,
                lifecyclePolicy(),
                () -> KernelServices.teleports().spawns().stream()
                        .anyMatch(spawn -> spawn.revision() == revision
                                && spawn.location().equals(location)));
    }

    private static SafeTeleportService.Policy lifecyclePolicy() {
        SafeTeleportService.Policy base = KernelServices.teleportSettings().userPolicy();
        return new SafeTeleportService.Policy(
                base.searchRadius(),
                base.maximumChecks(),
                base.maximumChunks(),
                base.allowHazards(),
                base.allowNetherRoof(),
                true,
                false,
                base.invulnerabilityTicks());
    }
}
