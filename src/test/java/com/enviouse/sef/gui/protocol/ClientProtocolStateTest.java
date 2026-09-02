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

    @Test
    void deltasPreservePerEntryRevisionsAndApplyRemovals() {
        UUID session = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        ClientProtocolState.accept(new SefPayloads.SessionState(
                session,
                1L,
                SefProtocol.MAJOR,
                SefProtocol.MINOR,
                SefProtocol.SERVER_FEATURES,
                true));
        SefPayloads.TagManifestEntry currentTag = tag(tagId, 5L, "Current");
        SefPayloads.TagAssignmentProjection currentAssignment =
                new SefPayloads.TagAssignmentProjection(
                        subjectId,
                        tagId,
                        "chat",
                        10,
                        5L);
        SefPayloads.DisguiseProjection currentDisguise =
                disguise(subjectId, 5L, "minecraft:blaze");
        ClientProtocolState.accept(new SefPayloads.TagRegistrySnapshot(
                session, 1L, true, List.of(currentTag)));
        ClientProtocolState.accept(new SefPayloads.TagAssignmentSnapshot(
                session, 1L, true, List.of(currentAssignment)));
        ClientProtocolState.accept(new SefPayloads.DisguiseSnapshot(
                session, 1L, true, List.of(currentDisguise)));

        ClientProtocolState.accept(new SefPayloads.TagRegistryDelta(
                session,
                2L,
                List.of(),
                List.of(tag(tagId, 4L, "Old"))));
        ClientProtocolState.accept(new SefPayloads.TagAssignmentDelta(
                session,
                2L,
                List.of(),
                List.of(new SefPayloads.TagAssignmentProjection(
                        subjectId,
                        tagId,
                        "chat",
                        1,
                        4L))));
        ClientProtocolState.accept(new SefPayloads.DisguiseDelta(
                session,
                2L,
                List.of(),
                List.of(disguise(subjectId, 4L, "minecraft:cow"))));

        assertEquals("Current", ClientProtocolState.tagManifests().getFirst().displayName());
        assertEquals(10, ClientProtocolState.tagAssignments().getFirst().priority());
        assertEquals(
                "minecraft:blaze",
                ClientProtocolState.disguise(subjectId).orElseThrow().reference());

        ClientProtocolState.accept(new SefPayloads.TagRegistryDelta(
                session, 3L, List.of(tagId), List.of()));
        ClientProtocolState.accept(new SefPayloads.DisguiseDelta(
                session, 3L, List.of(subjectId), List.of()));

        assertTrue(ClientProtocolState.tagManifests().isEmpty());
        assertTrue(ClientProtocolState.tagAssignments().isEmpty());
        assertTrue(ClientProtocolState.disguise(subjectId).isEmpty());
    }

    @Test
    void acceptsOnlyAuthorizedTagChunksAndStudioRequests() {
        UUID session = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        ClientProtocolState.accept(new SefPayloads.SessionState(
                session,
                1L,
                SefProtocol.MAJOR,
                SefProtocol.MINOR,
                SefProtocol.SERVER_FEATURES,
                true));
        ClientProtocolState.accept(new SefPayloads.TagRegistrySnapshot(
                session,
                1L,
                true,
                List.of(tag(tagId, 1L, "Tag"))));

        ClientProtocolState.accept(new SefPayloads.TagContentChunk(
                UUID.randomUUID(),
                "a".repeat(64),
                3,
                0,
                new byte[]{1, 2, 3}));
        assertTrue(ClientProtocolState.takeTagContentChunk().isEmpty());

        ClientProtocolState.accept(new SefPayloads.TagContentChunk(
                session,
                "a".repeat(64),
                3,
                0,
                new byte[]{1, 2, 3}));
        assertEquals(3, ClientProtocolState.takeTagContentChunk().orElseThrow().totalBytes());

        ClientProtocolState.accept(new SefPayloads.OpenFancyTagsStudio(session, "manager"));
        assertEquals("manager", ClientProtocolState.takeFancyTagsStudioSection().orElseThrow());
    }

    private static SefPayloads.TagManifestEntry tag(
            UUID id,
            long revision,
            String displayName
    ) {
        return new SefPayloads.TagManifestEntry(
                id,
                revision,
                "sef:test",
                displayName,
                "test tag",
                "a".repeat(64),
                3,
                3,
                1);
    }

    private static SefPayloads.DisguiseProjection disguise(
            UUID subjectId,
            long revision,
            String reference
    ) {
        return new SefPayloads.DisguiseProjection(
                subjectId,
                revision,
                "mob",
                reference,
                null,
                "",
                "",
                "",
                "disguise_type",
                "hide_equipment",
                true,
                true);
    }
}
