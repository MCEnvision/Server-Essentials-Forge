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
