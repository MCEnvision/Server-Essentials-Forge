package com.enviouse.sef.teleport;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.WarmupService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntSupplier;

public final class TeleportWarmupManager {
    private static final Map<UUID, Pending> PENDING = new ConcurrentHashMap<>();

    private TeleportWarmupManager() {
    }

    public static void schedule(ServerPlayer player, IntSupplier completion) {
        KernelServices.warmups().inspect(player.getUUID()).ifPresent(warmup ->
                PENDING.compute(player.getUUID(), (ignored, existing) ->
                        existing != null && existing.warmupId().equals(warmup.id())
                                ? existing
                                : new Pending(warmup.id(), completion)));
    }

    public static void tick(MinecraftServer server) {
        for (Map.Entry<UUID, Pending> entry : Map.copyOf(PENDING).entrySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                PENDING.remove(entry.getKey(), entry.getValue());
                KernelServices.warmups().clear(entry.getKey());
                continue;
            }
            WarmupService.Warmup warmup = KernelServices.warmups().inspect(entry.getKey()).orElse(null);
            if (warmup == null || !warmup.id().equals(entry.getValue().warmupId())) {
                PENDING.remove(entry.getKey(), entry.getValue());
                continue;
            }
            long now = System.currentTimeMillis();
            if (now >= warmup.expiresAtEpochMillis()) {
                if (PENDING.remove(entry.getKey(), entry.getValue())) {
                    entry.getValue().completion().getAsInt();
                }
                continue;
            }
            if (now + 100L >= warmup.expiresAtEpochMillis()) {
                continue;
            }
            ActionResult<WarmupService.Warmup> checked = KernelServices.warmups().check(
                    player.getUUID(),
                    new WarmupService.Position(
                            player.serverLevel().dimension().location().toString(),
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            player.getYRot(),
                            player.getXRot()));
            if (checked.successful()) {
                KernelServices.warmups().start(
                        warmup.actorId(),
                        warmup.actionId(),
                        warmup.initialPosition(),
                        warmup.targetId(),
                        java.time.Duration.ofMillis(1),
                        warmup.cancellationPolicy());
                KernelServices.warmups().inspect(entry.getKey()).ifPresent(replacement ->
                        PENDING.replace(
                                entry.getKey(),
                                entry.getValue(),
                                new Pending(replacement.id(), entry.getValue().completion())));
                continue;
            }
            if (checked.reason() == ActionResult.ReasonCode.WARMUP_CANCELLED
                    && PENDING.remove(entry.getKey(), entry.getValue())) {
                player.sendSystemMessage(TextFormatter.stringToFormattedText(
                        "&cTeleport warmup cancelled because " + checked.detail() + "."));
            }
        }
    }

    public static boolean cancel(ServerPlayer player, WarmupService.CancelReason reason) {
        boolean cancelled = KernelServices.warmups().cancel(player.getUUID(), reason);
        Pending removed = PENDING.remove(player.getUUID());
        if (cancelled && removed != null && reason != WarmupService.CancelReason.LOGOUT) {
            player.sendSystemMessage(TextFormatter.stringToFormattedText(
                    "&cTeleport warmup cancelled because " + reason.name().toLowerCase(java.util.Locale.ROOT) + "."));
        }
        return cancelled;
    }

    public static void cancelAll(WarmupService.CancelReason reason) {
        PENDING.keySet().forEach(playerId -> KernelServices.warmups().cancel(playerId, reason));
        PENDING.clear();
    }

    public static int size() {
        return PENDING.size();
    }

    private record Pending(UUID warmupId, IntSupplier completion) {
    }
}
