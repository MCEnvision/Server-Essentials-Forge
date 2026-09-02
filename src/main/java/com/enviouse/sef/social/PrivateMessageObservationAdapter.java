package com.enviouse.sef.social;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public interface PrivateMessageObservationAdapter {
    void publish(
            UUID eventId,
            MinecraftServer server,
            String route,
            ServerPlayer sender,
            ServerPlayer recipient,
            Component content
    );
}
