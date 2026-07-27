package com.enviouse.sef.teleport;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.recovery.GraveRepository;
import com.enviouse.sef.kernel.policy.WarmupService;
import com.enviouse.sef.storage.repository.LocationHistoryRepository;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
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
        com.enviouse.sef.control.MinecraftServerControlRuntime.effectivePolicy("inventory_recovery")
                .ifPresent(policy -> {
                    try {
                        ActionResult<com.enviouse.sef.recovery.InventoryRecoveryRepository.InventorySnapshot> result =
                                KernelServices.inventoryRecovery().capture(
                                        player,
                                        "death",
                                        Boolean.parseBoolean(policy.metadata().getOrDefault(
                                                "field.include_ender_chest",
                                                "true")),
                                        boundedInteger(policy, "maximum_snapshots", 16, 1, 128),
                                        boundedLong(
                                                policy,
                                                "retention_seconds",
                                                604_800L,
                                                60L,
                                                31_536_000L));
                        if (!result.successful()) {
                            ServerEssentialsForge.LOGGER.warn(
                                    "[SEF] Inventory recovery capture failed for {}. {}",
                                    player.getGameProfile().getName(),
                                    result.detail());
                        }
                    } catch (RuntimeException exception) {
                        ServerEssentialsForge.LOGGER.error(
                                "[SEF] Inventory recovery storage was unavailable for {}",
                                player.getGameProfile().getName(),
                                exception);
                    }
                });
        TeleportWarmupManager.cancel(player, WarmupService.CancelReason.DEATH);
    }

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getDrops().isEmpty()) {
            return;
        }
        com.enviouse.sef.control.MinecraftServerControlRuntime.effectivePolicy("graves")
                .ifPresent(policy -> {
                    try {
                        ActionResult<GraveRepository.GraveRecord> result =
                                KernelServices.graves().captureAndFlush(
                                        player,
                                        event.getDrops(),
                                        boundedLong(policy, "retention_seconds", 86_400L, 60L, 2_592_000L),
                                        policy.metadata().getOrDefault("field.container", "virtual"),
                                        Boolean.parseBoolean(policy.metadata().getOrDefault(
                                                "field.protect_owner",
                                                "true")),
                                        Boolean.parseBoolean(policy.metadata().getOrDefault(
                                                "field.keep_experience",
                                                "true")));
                        if (result.successful()) {
                            event.setCanceled(true);
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    "Your items were secured in grave " + result.value().id() + "."));
                        } else {
                            ServerEssentialsForge.LOGGER.error(
                                    "[SEF] Grave capture failed for {}. Vanilla drops were preserved. {}",
                                    player.getGameProfile().getName(),
                                    result.detail());
                        }
                    } catch (RuntimeException exception) {
                        ServerEssentialsForge.LOGGER.error(
                                "[SEF] Grave storage failed for {}. Vanilla drops were preserved",
                                player.getGameProfile().getName(),
                                exception);
                    }
                });
    }

    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getDroppedExperience() <= 0) {
            return;
        }
        GraveRepository.GraveRecord grave = KernelServices.graves()
                .latestActive(player.getUUID())
                .orElse(null);
        if (grave == null
                || !grave.keepExperience()
                || java.time.Duration.between(grave.createdAt(), Instant.now()).abs().toSeconds() > 10L) {
            return;
        }
        try {
            ActionResult<GraveRepository.GraveRecord> result =
                    KernelServices.graves().storeExperienceAndFlush(
                            grave.id(),
                            event.getDroppedExperience());
            if (result.successful()) {
                event.setDroppedExperience(0);
                return;
            }
            ServerEssentialsForge.LOGGER.error(
                    "[SEF] Grave experience capture failed for {}. Vanilla experience was preserved. {}",
                    player.getGameProfile().getName(),
                    result.detail());
        } catch (RuntimeException exception) {
            ServerEssentialsForge.LOGGER.error(
                    "[SEF] Grave experience storage failed for {}. Vanilla experience was preserved",
                    player.getGameProfile().getName(),
                    exception);
        }
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

    private static int boundedInteger(
            com.enviouse.sef.control.ServerControlRepository.ControlRecord record,
            String field,
            int fallback,
            int minimum,
            int maximum
    ) {
        return (int) boundedLong(record, field, fallback, minimum, maximum);
    }

    private static long boundedLong(
            com.enviouse.sef.control.ServerControlRepository.ControlRecord record,
            String field,
            long fallback,
            long minimum,
            long maximum
    ) {
        try {
            return Math.clamp(
                    Long.parseLong(record.metadata().getOrDefault(
                            "field." + field,
                            Long.toString(fallback))),
                    minimum,
                    maximum);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }
}
