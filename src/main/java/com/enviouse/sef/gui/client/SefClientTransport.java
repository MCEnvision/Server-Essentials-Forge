package com.enviouse.sef.gui.client;

import com.enviouse.sef.gui.protocol.ClientProtocolState;
import com.enviouse.sef.gui.protocol.GuiWorkflowPayloads;
import com.enviouse.sef.gui.protocol.SefPayloads;
import net.neoforged.neoforge.network.PacketDistributor;

final class SefClientTransport {
    private SefClientTransport() {
    }

    static void open(String panelId, int page, String query) {
        ClientProtocolState.session().ifPresent(session -> {
            long sequence = ClientProtocolState.nextSequence();
            if (sequence > 0L) {
                PacketDistributor.sendToServer(new SefPayloads.OpenPanelRequest(
                        session.sessionId(),
                        sequence,
                        panelId,
                        page,
                        query));
            }
        });
    }

    static void action(SefPayloads.PanelSnapshot panel, SefPayloads.PanelEntry entry) {
        ClientProtocolState.session().ifPresent(session -> {
            long sequence = ClientProtocolState.nextSequence();
            if (sequence > 0L) {
                PacketDistributor.sendToServer(new SefPayloads.PanelActionRequest(
                        session.sessionId(),
                        sequence,
                        panel.panelId(),
                        panel.panelRevision(),
                        entry.controlId(),
                        entry.entryId(),
                        entry.revision()));
            }
        });
    }

    static void mutateControl(
            SefPayloads.ControlEditorSnapshot snapshot,
            String operation,
            String title,
            String details,
            String argument,
            java.util.List<SefPayloads.ControlFieldValue> fields
    ) {
        ClientProtocolState.session().ifPresent(session -> {
            long sequence = ClientProtocolState.nextSequence();
            if (sequence > 0L) {
                PacketDistributor.sendToServer(new SefPayloads.ControlMutationRequest(
                        session.sessionId(),
                        sequence,
                        snapshot.panelId(),
                        snapshot.panelRevision(),
                        snapshot.recordId(),
                        snapshot.recordRevision(),
                        operation,
                        title,
                        details,
                        argument,
                        fields));
            }
        });
    }

    static void openWorkflow(String actionId) {
        ClientProtocolState.session().ifPresent(session -> {
            long sequence = ClientProtocolState.nextSequence();
            if (sequence > 0L) {
                PacketDistributor.sendToServer(new GuiWorkflowPayloads.GuiWorkflowOpen(
                        session.sessionId(),
                        sequence,
                        actionId));
            }
        });
    }

    static void updateWorkflowField(
            GuiWorkflowPayloads.GuiWorkflowSnapshot snapshot,
            String variantId,
            String fieldId,
            String value
    ) {
        ClientProtocolState.session().ifPresent(session -> {
            long sequence = ClientProtocolState.nextSequence();
            if (sequence > 0L) {
                PacketDistributor.sendToServer(new GuiWorkflowPayloads.GuiWorkflowFieldUpdate(
                        session.sessionId(),
                        sequence,
                        snapshot.workflowId(),
                        snapshot.revision(),
                        variantId,
                        fieldId,
                        value));
            }
        });
    }

    static void requestWorkflowSuggestions(
            GuiWorkflowPayloads.GuiWorkflowSnapshot snapshot,
            String variantId,
            String fieldId,
            String value,
            java.util.UUID requestId
    ) {
        ClientProtocolState.session().ifPresent(session -> {
            long sequence = ClientProtocolState.nextSequence();
            if (sequence > 0L) {
                PacketDistributor.sendToServer(new GuiWorkflowPayloads.GuiWorkflowSuggestionsRequest(
                        session.sessionId(),
                        sequence,
                        snapshot.workflowId(),
                        snapshot.revision(),
                        variantId,
                        fieldId,
                        value,
                        requestId));
            }
        });
    }

    static void previewWorkflow(
            GuiWorkflowPayloads.GuiWorkflowSnapshot snapshot,
            String variantId,
            java.util.List<GuiWorkflowPayloads.WorkflowFieldValue> fields
    ) {
        ClientProtocolState.session().ifPresent(session -> {
            long sequence = ClientProtocolState.nextSequence();
            if (sequence > 0L) {
                PacketDistributor.sendToServer(new GuiWorkflowPayloads.GuiWorkflowPreview(
                        session.sessionId(),
                        sequence,
                        snapshot.workflowId(),
                        snapshot.revision(),
                        variantId,
                        fields));
            }
        });
    }

    static void submitWorkflow(
            GuiWorkflowPayloads.GuiWorkflowSnapshot snapshot,
            String variantId,
            java.util.List<GuiWorkflowPayloads.WorkflowFieldValue> fields
    ) {
        ClientProtocolState.session().ifPresent(session -> {
            long sequence = ClientProtocolState.nextSequence();
            if (sequence > 0L) {
                PacketDistributor.sendToServer(new GuiWorkflowPayloads.GuiWorkflowSubmit(
                        session.sessionId(),
                        sequence,
                        snapshot.workflowId(),
                        snapshot.revision(),
                        variantId,
                        fields));
            }
        });
    }

    static void confirmWorkflow(GuiWorkflowPayloads.GuiWorkflowSnapshot snapshot) {
        if (snapshot.confirmationToken().isBlank()) {
            return;
        }
        ClientProtocolState.session().ifPresent(session -> {
            long sequence = ClientProtocolState.nextSequence();
            if (sequence > 0L) {
                PacketDistributor.sendToServer(new GuiWorkflowPayloads.GuiWorkflowConfirmation(
                        session.sessionId(),
                        sequence,
                        snapshot.workflowId(),
                        snapshot.revision(),
                        snapshot.confirmationToken()));
            }
        });
    }

    static void closeWorkflow(GuiWorkflowPayloads.GuiWorkflowSnapshot snapshot) {
        ClientProtocolState.session().ifPresent(session -> {
            long sequence = ClientProtocolState.nextSequence();
            if (sequence > 0L) {
                PacketDistributor.sendToServer(new GuiWorkflowPayloads.GuiWorkflowClose(
                        session.sessionId(),
                        sequence,
                        snapshot.workflowId(),
                        snapshot.revision()));
            }
        });
    }

    static void requestTag(String hash) {
        requestTag(hash, 0);
    }

    static void requestTag(String hash, int offset) {
        ClientProtocolState.session().ifPresent(session -> {
            long sequence = ClientProtocolState.nextSequence();
            if (sequence > 0L) {
                PacketDistributor.sendToServer(new SefPayloads.TagContentChunkRequest(
                        session.sessionId(),
                        sequence,
                        hash,
                        offset));
            }
        });
    }

    static void beginTagUpload(
            java.util.UUID tagId,
            java.util.UUID leaseId,
            long expectedTagRevision,
            byte[] content
    ) {
        String hash = sha256(content);
        ClientProtocolState.session().ifPresent(session -> {
            long sequence = ClientProtocolState.nextSequence();
            if (sequence > 0L) {
                PacketDistributor.sendToServer(new SefPayloads.TagUploadBegin(
                        session.sessionId(),
                        sequence,
                        tagId,
                        leaseId,
                        expectedTagRevision,
                        content.length,
                        hash));
            }
        });
    }

    static void sendTagUploadChunk(java.util.UUID uploadId, int chunkIndex, byte[] content) {
        ClientProtocolState.session().ifPresent(session -> {
            long sequence = ClientProtocolState.nextSequence();
            if (sequence > 0L) {
                PacketDistributor.sendToServer(new SefPayloads.TagUploadChunk(
                        session.sessionId(),
                        sequence,
                        uploadId,
                        chunkIndex,
                        content));
            }
        });
    }

    static void finishTagUpload(java.util.UUID uploadId) {
        ClientProtocolState.session().ifPresent(session -> {
            long sequence = ClientProtocolState.nextSequence();
            if (sequence > 0L) {
                PacketDistributor.sendToServer(new SefPayloads.TagUploadFinish(
                        session.sessionId(),
                        sequence,
                        uploadId));
            }
        });
    }

    static void cancelTagUpload(java.util.UUID uploadId) {
        ClientProtocolState.session().ifPresent(session -> {
            long sequence = ClientProtocolState.nextSequence();
            if (sequence > 0L) {
                PacketDistributor.sendToServer(new SefPayloads.TagUploadCancel(
                        session.sessionId(),
                        sequence,
                        uploadId));
            }
        });
    }

    static void mutateTag(String operation, String tagReference, long expectedRevision, String argument) {
        ClientProtocolState.session().ifPresent(session -> {
            long sequence = ClientProtocolState.nextSequence();
            if (sequence > 0L) {
                PacketDistributor.sendToServer(new SefPayloads.TagMutationRequest(
                        session.sessionId(),
                        sequence,
                        operation,
                        tagReference,
                        expectedRevision,
                        argument));
            }
        });
    }

    static void queryTagManager(String section, int page, String query) {
        ClientProtocolState.session().ifPresent(session -> {
            long sequence = ClientProtocolState.nextSequence();
            if (sequence > 0L) {
                PacketDistributor.sendToServer(new SefPayloads.TagManagerQuery(
                        session.sessionId(),
                        sequence,
                        section,
                        Math.max(1, page),
                        query == null ? "" : query));
            }
        });
    }

    static void disguiseAbility(String slot) {
        ClientProtocolState.session().ifPresent(session -> {
            long sequence = ClientProtocolState.nextSequence();
            if (sequence > 0L) {
                PacketDistributor.sendToServer(new SefPayloads.DisguiseAbilityRequest(
                        session.sessionId(),
                        sequence,
                        slot));
            }
        });
    }

    private static String sha256(byte[] bytes) {
        try {
            return java.util.HexFormat.of().formatHex(
                    java.security.MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
