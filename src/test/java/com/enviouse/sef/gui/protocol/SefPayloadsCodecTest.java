package com.enviouse.sef.gui.protocol;

import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SefPayloadsCodecTest {
    private static final UUID SESSION = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ENTRY = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final String HASH = "a".repeat(64);

    @Test
    void roundTripsEveryConfigurationAndPlayPayload() {
        assertRoundTrip(
                SefPayloads.ServerHello.CODEC,
                new SefPayloads.ServerHello(SESSION, 42L, 1, 0, 255L));
        assertRoundTrip(
                SefPayloads.ClientHello.CODEC,
                new SefPayloads.ClientHello(SESSION, 42L, 1, 0, 127L, true));
        assertRoundTrip(
                SefPayloads.SessionState.CODEC,
                new SefPayloads.SessionState(SESSION, 1L, 1, 0, 127L, true));
        assertRoundTrip(
                SefPayloads.OpenPanelRequest.CODEC,
                new SefPayloads.OpenPanelRequest(SESSION, 1L, "homes", 1, "base"));
        SefPayloads.PanelEntry entry = new SefPayloads.PanelEntry(
                ENTRY,
                3L,
                "visit",
                "Base",
                "overworld",
                "minecraft:red_bed",
                true,
                false);
        assertRoundTrip(
                SefPayloads.PanelSnapshot.CODEC,
                new SefPayloads.PanelSnapshot(
                        SESSION,
                        2L,
                        "homes",
                        SefPayloads.PanelView.LIST,
                        "Homes",
                        1,
                        1,
                        "",
                        List.of(entry),
                        "one home"));
        assertRoundTrip(
                SefPayloads.PanelActionRequest.CODEC,
                new SefPayloads.PanelActionRequest(
                        SESSION,
                        2L,
                        "homes",
                        2L,
                        "visit",
                        ENTRY,
                        3L));
        SefPayloads.ControlField controlField = new SefPayloads.ControlField(
                "mode",
                "enum",
                true,
                0L,
                0L,
                "open",
                List.of("locked", "open"));
        assertRoundTrip(
                SefPayloads.ControlEditorSnapshot.CODEC,
                new SefPayloads.ControlEditorSnapshot(
                        SESSION,
                        3L,
                        "control_edit:" + ENTRY,
                        ENTRY,
                        4L,
                        "chat_control",
                        "Chat control",
                        "test",
                        "open",
                        "dashboard",
                        "ready",
                        List.of(controlField),
                        List.of("active", "paused"),
                        List.of("configure", "preview", "execute"),
                        true,
                        false));
        assertRoundTrip(
                SefPayloads.ControlMutationRequest.CODEC,
                new SefPayloads.ControlMutationRequest(
                        SESSION,
                        3L,
                        "control_edit:" + ENTRY,
                        3L,
                        ENTRY,
                        4L,
                        "save",
                        "Chat control",
                        "test",
                        "",
                        List.of(new SefPayloads.ControlFieldValue("mode", "open"))));
        assertRoundTrip(
                SefPayloads.HudDelta.CODEC,
                new SefPayloads.HudDelta(
                        SESSION,
                        1L,
                        true,
                        List.of(new SefPayloads.HudTile(
                                "vanish",
                                "Vanish enabled",
                                SefPayloads.Severity.NOTICE))));
        assertRoundTripRegistry(
                SefPayloads.IdentityProjection.CODEC,
                new SefPayloads.IdentityProjection(
                        SESSION,
                        1L,
                        true,
                        List.of(new SefPayloads.ProjectedIdentity(
                                ENTRY,
                                1L,
                                Component.literal("Projected"))),
                        List.of()));
        assertRoundTrip(
                SefPayloads.TagManifest.CODEC,
                new SefPayloads.TagManifest(SESSION, 1L, ENTRY, HASH, 3, "tag"));
        assertRoundTrip(
                SefPayloads.TagContentRequest.CODEC,
                new SefPayloads.TagContentRequest(SESSION, 3L, HASH));

        byte[] bytes = new byte[]{1, 2, 3};
        SefPayloads.TagContent decoded = roundTrip(
                SefPayloads.TagContent.CODEC,
                new SefPayloads.TagContent(SESSION, HASH, bytes));
        assertEquals(SESSION, decoded.sessionId());
        assertEquals(HASH, decoded.hash());
        assertArrayEquals(bytes, decoded.content());
        assertRoundTrip(
                SefPayloads.TagContentChunkRequest.CODEC,
                new SefPayloads.TagContentChunkRequest(SESSION, 4L, HASH, 0));
        SefPayloads.TagContentChunk downloaded = roundTrip(
                SefPayloads.TagContentChunk.CODEC,
                new SefPayloads.TagContentChunk(SESSION, HASH, 3, 0, bytes));
        assertArrayEquals(bytes, downloaded.content());
        assertEquals(3, downloaded.totalBytes());
        assertEquals(0, downloaded.offset());
        assertEquals(true, downloaded.complete());
        UUID lease = UUID.randomUUID();
        assertRoundTrip(
                SefPayloads.TagUploadBegin.CODEC,
                new SefPayloads.TagUploadBegin(SESSION, 4L, ENTRY, lease, 2L, 3, HASH));
        SefPayloads.TagUploadChunk chunk = roundTrip(
                SefPayloads.TagUploadChunk.CODEC,
                new SefPayloads.TagUploadChunk(SESSION, 5L, ENTRY, 0, bytes));
        assertArrayEquals(bytes, chunk.content());
        assertRoundTrip(
                SefPayloads.TagUploadFinish.CODEC,
                new SefPayloads.TagUploadFinish(SESSION, 6L, ENTRY));
        assertRoundTrip(
                SefPayloads.TagUploadCancel.CODEC,
                new SefPayloads.TagUploadCancel(SESSION, 7L, ENTRY));
        assertRoundTrip(
                SefPayloads.TagMutationRequest.CODEC,
                new SefPayloads.TagMutationRequest(
                        SESSION,
                        8L,
                        "lease_acquire",
                        "founder",
                        2L,
                        ""));
        assertRoundTrip(
                SefPayloads.TagOperationResult.CODEC,
                new SefPayloads.TagOperationResult(
                        SESSION,
                        8L,
                        true,
                        "success",
                        "",
                        ENTRY,
                        3L,
                        3,
                        3));
        SefPayloads.TagManifestEntry manifestEntry = new SefPayloads.TagManifestEntry(
                ENTRY,
                4L,
                "sef:founder",
                "Founder",
                "founder tag",
                HASH,
                3,
                3,
                1);
        assertRoundTrip(
                SefPayloads.TagRegistryDelta.CODEC,
                new SefPayloads.TagRegistryDelta(
                        SESSION,
                        9L,
                        List.of(UUID.randomUUID()),
                        List.of(manifestEntry)));
        SefPayloads.TagAssignmentProjection assignment =
                new SefPayloads.TagAssignmentProjection(
                        UUID.randomUUID(),
                        ENTRY,
                        "nameplate",
                        100,
                        5L);
        assertRoundTrip(
                SefPayloads.TagAssignmentDelta.CODEC,
                new SefPayloads.TagAssignmentDelta(
                        SESSION,
                        10L,
                        List.of(new SefPayloads.TagAssignmentKey(
                                UUID.randomUUID(),
                                ENTRY,
                                "chat")),
                        List.of(assignment)));
        assertRoundTrip(
                SefPayloads.TagManagerQuery.CODEC,
                new SefPayloads.TagManagerQuery(
                        SESSION,
                        11L,
                        "manager",
                        2,
                        "founder"));
        SefPayloads.TagManagerEntry managerEntry = new SefPayloads.TagManagerEntry(
                "tag",
                ENTRY,
                ENTRY,
                "sef:founder",
                "Founder",
                "founder tag",
                "published",
                4L);
        assertRoundTrip(
                SefPayloads.TagManagerSnapshot.CODEC,
                new SefPayloads.TagManagerSnapshot(
                        SESSION,
                        11L,
                        12L,
                        "manager",
                        1,
                        1,
                        List.of(managerEntry)));
        assertRoundTrip(
                SefPayloads.OpenFancyTagsStudio.CODEC,
                new SefPayloads.OpenFancyTagsStudio(SESSION, "manager"));
        SefPayloads.DisguiseProjection disguise = new SefPayloads.DisguiseProjection(
                UUID.randomUUID(),
                2L,
                "mob",
                "minecraft:blaze",
                null,
                "",
                "",
                "",
                "disguise_type",
                "hide_equipment",
                true,
                true);
        assertRoundTrip(
                SefPayloads.DisguiseDelta.CODEC,
                new SefPayloads.DisguiseDelta(
                        SESSION,
                        13L,
                        List.of(UUID.randomUUID()),
                        List.of(disguise)));
    }

    @Test
    void rejectsControlTextAndOversizedCollections() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SefPayloads.HudTile(
                        "bad\nid",
                        "text",
                        SefPayloads.Severity.INFO));
        List<SefPayloads.PanelEntry> entries = java.util.stream.IntStream
                .range(0, SefProtocol.MAXIMUM_PANEL_ENTRIES + 1)
                .mapToObj(index -> new SefPayloads.PanelEntry(
                        UUID.randomUUID(),
                        1L,
                        "noop",
                        "entry",
                        "",
                        "minecraft:paper",
                        false,
                        false))
                .toList();
        assertThrows(
                IllegalArgumentException.class,
                () -> new SefPayloads.PanelSnapshot(
                        SESSION,
                        1L,
                        "help",
                        SefPayloads.PanelView.LIST,
                        "Help",
                        1,
                        1,
                        "",
                        entries,
                        ""));
        SefPayloads.IdentityProjection oversizedComponent = new SefPayloads.IdentityProjection(
                SESSION,
                1L,
                true,
                List.of(new SefPayloads.ProjectedIdentity(
                        ENTRY,
                        1L,
                        Component.literal("visible").withStyle(
                                style -> style.withInsertion("x".repeat(
                                        SefProtocol.MAXIMUM_IDENTITY_COMPONENT_BYTES + 1))))),
                List.of());
        assertThrows(
                IllegalArgumentException.class,
                () -> encodeRegistry(SefPayloads.IdentityProjection.CODEC, oversizedComponent));
    }

    @Test
    void tagContentDefensivelyCopiesBytes() {
        byte[] source = new byte[]{1, 2, 3};
        SefPayloads.TagContent payload = new SefPayloads.TagContent(SESSION, HASH, source);
        source[0] = 9;
        byte[] first = payload.content();
        first[1] = 9;
        assertArrayEquals(new byte[]{1, 2, 3}, payload.content());
        SefPayloads.TagContentChunk chunk =
                new SefPayloads.TagContentChunk(SESSION, HASH, 3, 0, source);
        source[0] = 7;
        byte[] chunkCopy = chunk.content();
        chunkCopy[1] = 7;
        assertArrayEquals(new byte[]{9, 2, 3}, chunk.content());
    }

    private static <T> void assertRoundTrip(StreamCodec<FriendlyByteBuf, T> codec, T value) {
        assertEquals(value, roundTrip(codec, value));
    }

    private static <T> T roundTrip(StreamCodec<FriendlyByteBuf, T> codec, T value) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            codec.encode(buffer, value);
            return codec.decode(buffer);
        } finally {
            buffer.release();
        }
    }

    private static <T> void assertRoundTripRegistry(
            StreamCodec<RegistryFriendlyByteBuf, T> codec,
            T value
    ) {
        assertEquals(value, encodeRegistry(codec, value));
    }

    private static <T> T encodeRegistry(
            StreamCodec<RegistryFriendlyByteBuf, T> codec,
            T value
    ) {
        RegistryFriendlyByteBuf buffer =
                new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            codec.encode(buffer, value);
            return codec.decode(buffer);
        } finally {
            buffer.release();
        }
    }
}
