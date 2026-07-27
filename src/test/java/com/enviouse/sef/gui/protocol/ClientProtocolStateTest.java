package com.enviouse.sef.gui.protocol;

import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientProtocolStateTest {
    @AfterEach
    void reset() {
        ClientProtocolState.reset();
    }

    @Test
    void ignoresStaleAndWrongSessionUpdates() {
        UUID session = UUID.randomUUID();
        ClientProtocolState.accept(new SefPayloads.SessionState(
                session,
                1L,
                SefProtocol.MAJOR,
                SefProtocol.MINOR,
                SefProtocol.SERVER_FEATURES,
                true));
        ClientProtocolState.accept(new SefPayloads.HudDelta(
                UUID.randomUUID(),
                2L,
                true,
                List.of(new SefPayloads.HudTile("wrong", "wrong", SefPayloads.Severity.INFO))));
        assertTrue(ClientProtocolState.hudTiles().isEmpty());

        ClientProtocolState.accept(new SefPayloads.HudDelta(
                session,
                2L,
                true,
                List.of(new SefPayloads.HudTile("new", "new", SefPayloads.Severity.INFO))));
        ClientProtocolState.accept(new SefPayloads.HudDelta(
                session,
                1L,
                true,
                List.of(new SefPayloads.HudTile("old", "old", SefPayloads.Severity.INFO))));
        UUID projectedPlayer = UUID.randomUUID();
        ClientProtocolState.accept(new SefPayloads.IdentityProjection(
                session,
                1L,
                true,
                List.of(new SefPayloads.ProjectedIdentity(
                        projectedPlayer,
                        1L,
                        Component.literal("Nickname"))),
                List.of()));
        assertEquals("new", ClientProtocolState.hudTiles().getFirst().id());
        assertEquals(
                "Nickname",
                ClientProtocolState.identity(projectedPlayer).orElseThrow().displayName().getString());
        assertTrue(ClientProtocolState.negotiated(SefProtocol.Feature.DASHBOARD));

        ClientProtocolState.accept(new SefPayloads.SessionState(
                UUID.randomUUID(),
                1L,
                SefProtocol.MAJOR,
                SefProtocol.MINOR,
                0L,
                false));
        assertTrue(ClientProtocolState.hudTiles().isEmpty());
        assertTrue(ClientProtocolState.identity(projectedPlayer).isEmpty());
        assertFalse(ClientProtocolState.negotiated(SefProtocol.Feature.DASHBOARD));
    }

    @Test
    void rejectsLateFeaturePayloadsAfterAuthorizationIsRevoked() {
        UUID session = UUID.randomUUID();
        UUID projectedPlayer = UUID.randomUUID();
        ClientProtocolState.accept(new SefPayloads.SessionState(
                session,
                1L,
                SefProtocol.MAJOR,
                SefProtocol.MINOR,
                SefProtocol.SERVER_FEATURES,
                true));
        ClientProtocolState.accept(new SefPayloads.SessionState(
                session,
                2L,
                SefProtocol.MAJOR,
                SefProtocol.MINOR,
                SefProtocol.SERVER_FEATURES,
                false));

        ClientProtocolState.accept(new SefPayloads.HudDelta(
                session,
                99L,
                true,
                List.of(new SefPayloads.HudTile("late", "late", SefPayloads.Severity.WARNING))));
        ClientProtocolState.accept(new SefPayloads.IdentityProjection(
                session,
                99L,
                true,
                List.of(new SefPayloads.ProjectedIdentity(
                        projectedPlayer,
                        99L,
                        Component.literal("Late"))),
                List.of()));

        assertTrue(ClientProtocolState.hudTiles().isEmpty());
        assertTrue(ClientProtocolState.identity(projectedPlayer).isEmpty());
        assertTrue(ClientProtocolState.takePanel().isEmpty());
    }

    @Test
    void rejectsStalePerPlayerIdentityRevisions() {
        UUID session = UUID.randomUUID();
        UUID projectedPlayer = UUID.randomUUID();
        ClientProtocolState.accept(new SefPayloads.SessionState(
                session,
                1L,
                SefProtocol.MAJOR,
                SefProtocol.MINOR,
                SefProtocol.SERVER_FEATURES,
                true));
        ClientProtocolState.accept(new SefPayloads.IdentityProjection(
                session,
                1L,
                true,
                List.of(new SefPayloads.ProjectedIdentity(
                        projectedPlayer,
                        4L,
                        Component.literal("New"))),
                List.of()));
        ClientProtocolState.accept(new SefPayloads.IdentityProjection(
                session,
                2L,
                false,
                List.of(new SefPayloads.ProjectedIdentity(
                        projectedPlayer,
                        3L,
                        Component.literal("Old"))),
                List.of()));

        assertEquals(
                "New",
                ClientProtocolState.identity(projectedPlayer).orElseThrow().displayName().getString());
    }
}
