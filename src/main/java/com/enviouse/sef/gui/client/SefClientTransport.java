package com.enviouse.sef.gui.client;

import com.enviouse.sef.gui.protocol.ClientProtocolState;
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

    static void requestTag(String hash) {
        ClientProtocolState.session().ifPresent(session -> {
            long sequence = ClientProtocolState.nextSequence();
            if (sequence > 0L) {
                PacketDistributor.sendToServer(new SefPayloads.TagContentRequest(
                        session.sessionId(),
                        sequence,
                        hash));
            }
        });
    }
}
