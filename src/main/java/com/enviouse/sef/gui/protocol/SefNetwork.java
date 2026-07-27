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
                SefPayloads.ControlEditorSnapshot.TYPE,
                SefPayloads.ControlEditorSnapshot.CODEC,
                (payload, context) -> ClientProtocolState.accept(payload));
        registrar.playToServer(
                SefPayloads.ControlMutationRequest.TYPE,
                SefPayloads.ControlMutationRequest.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        SefGuiServer.handleControlMutation(player, payload);
                    }
                }));
        registrar.playToServer(
                GuiWorkflowPayloads.GuiWorkflowOpen.TYPE,
                GuiWorkflowPayloads.GuiWorkflowOpen.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        GuiWorkflowService.handleOpen(player, payload);
                    }
                }));
        registrar.playToClient(
                GuiWorkflowPayloads.GuiWorkflowSnapshot.TYPE,
                GuiWorkflowPayloads.GuiWorkflowSnapshot.CODEC,
                (payload, context) -> ClientProtocolState.accept(payload));
        registrar.playToServer(
                GuiWorkflowPayloads.GuiWorkflowFieldUpdate.TYPE,
                GuiWorkflowPayloads.GuiWorkflowFieldUpdate.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        GuiWorkflowService.handleFieldUpdate(player, payload);
                    }
                }));
        registrar.playToServer(
                GuiWorkflowPayloads.GuiWorkflowSuggestionsRequest.TYPE,
                GuiWorkflowPayloads.GuiWorkflowSuggestionsRequest.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        GuiWorkflowService.handleSuggestions(player, payload);
                    }
                }));
        registrar.playToClient(
                GuiWorkflowPayloads.GuiWorkflowSuggestions.TYPE,
                GuiWorkflowPayloads.GuiWorkflowSuggestions.CODEC,
                (payload, context) -> ClientProtocolState.accept(payload));
        registrar.playToServer(
                GuiWorkflowPayloads.GuiWorkflowPreview.TYPE,
                GuiWorkflowPayloads.GuiWorkflowPreview.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        GuiWorkflowService.handlePreview(player, payload);
                    }
                }));
        registrar.playToServer(
                GuiWorkflowPayloads.GuiWorkflowSubmit.TYPE,
                GuiWorkflowPayloads.GuiWorkflowSubmit.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        GuiWorkflowService.handleSubmit(player, payload);
                    }
                }));
        registrar.playToServer(
                GuiWorkflowPayloads.GuiWorkflowConfirmation.TYPE,
                GuiWorkflowPayloads.GuiWorkflowConfirmation.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        GuiWorkflowService.handleConfirmation(player, payload);
                    }
                }));
        registrar.playToClient(
                GuiWorkflowPayloads.GuiWorkflowProgress.TYPE,
                GuiWorkflowPayloads.GuiWorkflowProgress.CODEC,
                (payload, context) -> ClientProtocolState.accept(payload));
        registrar.playToClient(
                GuiWorkflowPayloads.GuiWorkflowResult.TYPE,
                GuiWorkflowPayloads.GuiWorkflowResult.CODEC,
                (payload, context) -> ClientProtocolState.accept(payload));
        registrar.playToClient(
                GuiWorkflowPayloads.GuiWorkflowInvalidate.TYPE,
                GuiWorkflowPayloads.GuiWorkflowInvalidate.CODEC,
                (payload, context) -> ClientProtocolState.accept(payload));
        registrar.playToServer(
                GuiWorkflowPayloads.GuiWorkflowClose.TYPE,
                GuiWorkflowPayloads.GuiWorkflowClose.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        GuiWorkflowService.handleClose(player, payload);
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
        registrar.playToServer(
                SefPayloads.TagContentChunkRequest.TYPE,
                SefPayloads.TagContentChunkRequest.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        SefGuiServer.handleTagContentChunk(player, payload);
                    }
                }));
        registrar.playToClient(
                SefPayloads.TagContentChunk.TYPE,
                SefPayloads.TagContentChunk.CODEC,
                (payload, context) -> ClientProtocolState.accept(payload));
        registrar.playToServer(
                SefPayloads.TagUploadBegin.TYPE,
                SefPayloads.TagUploadBegin.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        SefGuiServer.handleTagUploadBegin(player, payload);
                    }
                }));
        registrar.playToServer(
                SefPayloads.TagUploadChunk.TYPE,
                SefPayloads.TagUploadChunk.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        SefGuiServer.handleTagUploadChunk(player, payload);
                    }
                }));
        registrar.playToServer(
                SefPayloads.TagUploadFinish.TYPE,
                SefPayloads.TagUploadFinish.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        SefGuiServer.handleTagUploadFinish(player, payload);
                    }
                }));
        registrar.playToServer(
                SefPayloads.TagUploadCancel.TYPE,
                SefPayloads.TagUploadCancel.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        SefGuiServer.handleTagUploadCancel(player, payload);
                    }
                }));
        registrar.playToServer(
                SefPayloads.TagMutationRequest.TYPE,
                SefPayloads.TagMutationRequest.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        SefGuiServer.handleTagMutation(player, payload);
                    }
                }));
        registrar.playToClient(
                SefPayloads.TagOperationResult.TYPE,
                SefPayloads.TagOperationResult.CODEC,
                (payload, context) -> ClientProtocolState.accept(payload));
        registrar.playToClient(
                SefPayloads.TagRegistrySnapshot.TYPE,
                SefPayloads.TagRegistrySnapshot.CODEC,
                (payload, context) -> ClientProtocolState.accept(payload));
        registrar.playToClient(
                SefPayloads.TagRegistryDelta.TYPE,
                SefPayloads.TagRegistryDelta.CODEC,
                (payload, context) -> ClientProtocolState.accept(payload));
        registrar.playToClient(
                SefPayloads.TagAssignmentSnapshot.TYPE,
                SefPayloads.TagAssignmentSnapshot.CODEC,
                (payload, context) -> ClientProtocolState.accept(payload));
        registrar.playToClient(
                SefPayloads.TagAssignmentDelta.TYPE,
                SefPayloads.TagAssignmentDelta.CODEC,
                (payload, context) -> ClientProtocolState.accept(payload));
        registrar.playToServer(
                SefPayloads.TagManagerQuery.TYPE,
                SefPayloads.TagManagerQuery.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        SefGuiServer.handleTagManagerQuery(player, payload);
                    }
                }));
        registrar.playToClient(
                SefPayloads.TagManagerSnapshot.TYPE,
                SefPayloads.TagManagerSnapshot.CODEC,
                (payload, context) -> ClientProtocolState.accept(payload));
        registrar.playToClient(
                SefPayloads.OpenFancyTagsStudio.TYPE,
                SefPayloads.OpenFancyTagsStudio.CODEC,
                (payload, context) -> ClientProtocolState.accept(payload));
        registrar.playToClient(
                SefPayloads.TagCacheInvalidation.TYPE,
                SefPayloads.TagCacheInvalidation.CODEC,
                (payload, context) -> ClientProtocolState.accept(payload));
        registrar.playToClient(
                SefPayloads.DisguiseSnapshot.TYPE,
                SefPayloads.DisguiseSnapshot.CODEC,
                (payload, context) -> ClientProtocolState.accept(payload));
        registrar.playToClient(
                SefPayloads.DisguiseDelta.TYPE,
                SefPayloads.DisguiseDelta.CODEC,
                (payload, context) -> ClientProtocolState.accept(payload));
        registrar.playToServer(
                SefPayloads.DisguiseAbilityRequest.TYPE,
                SefPayloads.DisguiseAbilityRequest.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        SefGuiServer.handleDisguiseAbility(player, payload);
                    }
                }));
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

    public static void applyRuntimeMode(String mode) {
        String normalized = java.util.Objects.requireNonNull(mode, "mode")
                .trim()
                .toLowerCase(java.util.Locale.ROOT);
        if (!normalized.equals("off") && !normalized.equals("on") && !normalized.equals("auto")) {
            throw new IllegalArgumentException("enhanced GUI mode is invalid");
        }
        enhancedGuiActive = !normalized.equals("off");
    }

    public static boolean configurationDrift() {
        return enhancedGuiActive != ConfigHandler.config.enableEnhancedGui.get();
    }
}
