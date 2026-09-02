package com.enviouse.sef.commandlog;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.moderation.ConnectionAddressService;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = ServerEssentialsForge.MODID)
public final class ConnectionObservationEvents {
    private ConnectionObservationEvents() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        capture(event.getEntity(), FileLogSink.ConnectionType.JOIN);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        capture(event.getEntity(), FileLogSink.ConnectionType.LEAVE);
    }

    private static void capture(net.minecraft.world.entity.player.Player entity, FileLogSink.ConnectionType type) {
        if (!(entity instanceof ServerPlayer player) || !KernelServices.fileLogs().connectionStreamEnabled()) {
            return;
        }
        FileLogSink.Health health = KernelServices.fileLogs().health();
        if (health.sessionId() == null) {
            return;
        }
        Optional<ConnectionAddressService.Address> address =
                KernelServices.connectionAddresses().forPlayer(player);
        KernelServices.fileLogs().submitConnection(new FileLogSink.ConnectionRecord(
                1,
                UUID.randomUUID(),
                health.sessionId(),
                Instant.now(),
                type,
                player.getUUID(),
                player.getGameProfile().getName(),
                address.map(ConnectionAddressService.Address::fingerprint).orElse("unavailable"),
                address.map(ConnectionAddressService.Address::redacted).orElse("unavailable")));
    }
}
