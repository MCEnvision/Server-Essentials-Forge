package com.enviouse.sef.gui.protocol;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.config.ConfigHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class SefNetwork {
    private static volatile boolean enhancedGuiActive;

    private SefNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(SefProtocol.CHANNEL_VERSION)
                .versioned(SefProtocol.CHANNEL_VERSION)
                .optional();
        registrar.configurationToClient(
                SefPayloads.ServerHello.TYPE,
                SefPayloads.ServerHello.CODEC,
                (payload, context) -> context.reply(ClientProtocolState.answer(payload)));
        registrar.configurationToServer(
                SefPayloads.ClientHello.TYPE,
                SefPayloads.ClientHello.CODEC,
                (payload, context) -> {
                    try {
                        SefSessionManager.instance().acknowledge(context.connection(), payload);
                    } finally {
                        context.finishCurrentTask(SefConfigurationTask.TYPE);
                    }
                });
        registrar.playToClient(
                SefPayloads.SessionState.TYPE,
                SefPayloads.SessionState.CODEC,
                (payload, context) -> ClientProtocolState.accept(payload));
        registrar.playToServer(
                SefPayloads.OpenPanelRequest.TYPE,
                SefPayloads.OpenPanelRequest.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        SefGuiServer.handleOpen(player, payload);
                    }
                }));
        registrar.playToClient(
                SefPayloads.PanelSnapshot.TYPE,
                SefPayloads.PanelSnapshot.CODEC,
                (payload, context) -> ClientProtocolState.accept(payload));
        registrar.playToServer(
                SefPayloads.PanelActionRequest.TYPE,
                SefPayloads.PanelActionRequest.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        SefGuiServer.handleAction(player, payload);
                    }
                }));
        registrar.playToClient(
                SefPayloads.HudDelta.TYPE,
                SefPayloads.HudDelta.CODEC,
                (payload, context) -> ClientProtocolState.accept(payload));
        registrar.playToClient(
                SefPayloads.IdentityProjection.TYPE,
                SefPayloads.IdentityProjection.CODEC,
                (payload, context) -> ClientProtocolState.accept(payload));
        registrar.playToClient(
                SefPayloads.TagManifest.TYPE,
                SefPayloads.TagManifest.CODEC,
                (payload, context) -> ClientProtocolState.accept(payload));
        registrar.playToServer(
                SefPayloads.TagContentRequest.TYPE,
                SefPayloads.TagContentRequest.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        SefGuiServer.handleTagContent(player, payload);
                    }
                }));
        registrar.playToClient(
                SefPayloads.TagContent.TYPE,
                SefPayloads.TagContent.CODEC,
                (payload, context) -> ClientProtocolState.accept(payload));
    }

    public static void registerConfigurationTask(RegisterConfigurationTasksEvent event) {
        if (!enhancedGuiActive
                || !event.getListener().hasChannel(SefPayloads.ServerHello.TYPE)
                || !event.getListener().hasChannel(SefPayloads.ClientHello.TYPE)) {
            return;
        }
        try {
            SefPayloads.ServerHello hello =
                    SefSessionManager.instance().begin(event.getListener().getConnection());
            event.register(new SefConfigurationTask(hello));
        } catch (IllegalStateException exception) {
            ServerEssentialsForge.LOGGER.warn(
                    "[SEF] Enhanced client negotiation was skipped because capacity is exhausted");
        }
    }

    public static void activateConfiguredState() {
        enhancedGuiActive = ConfigHandler.config.enableEnhancedGui.get();
    }

    public static boolean enhancedGuiActive() {
        return enhancedGuiActive;
    }

    public static boolean configurationDrift() {
        return enhancedGuiActive != ConfigHandler.config.enableEnhancedGui.get();
    }
}
