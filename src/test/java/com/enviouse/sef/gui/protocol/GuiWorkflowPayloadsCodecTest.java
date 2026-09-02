package com.enviouse.sef.gui.protocol;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GuiWorkflowPayloadsCodecTest {
    private static final UUID SESSION = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID WORKFLOW = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID REQUEST = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Test
    void roundTripsTheCompleteWorkflowProtocol() {
        GuiWorkflowPayloads.WorkflowField field = new GuiWorkflowPayloads.WorkflowField(
                "player",
                "player",
                "player",
                "raw",
                true,
                -Double.MAX_VALUE,
                Double.MAX_VALUE,
                256,
                "EnVy",
                List.of(),
                "players");
        GuiWorkflowPayloads.WorkflowVariant variant = new GuiWorkflowPayloads.WorkflowVariant(
                "variant_1",
                "{player}",
                List.of(field));
        GuiWorkflowPayloads.WorkflowFieldValue value =
                new GuiWorkflowPayloads.WorkflowFieldValue("player", "EnVy");

        assertRoundTrip(
                GuiWorkflowPayloads.GuiWorkflowOpen.CODEC,
                new GuiWorkflowPayloads.GuiWorkflowOpen(SESSION, 1L, "sef:test"));
        assertRoundTrip(
                GuiWorkflowPayloads.GuiWorkflowSnapshot.CODEC,
                new GuiWorkflowPayloads.GuiWorkflowSnapshot(
                        SESSION,
                        WORKFLOW,
                        1L,
                        "sef:test",
                        "category_core",
                        "Test",
                        "ready",
                        "test EnVy",
                        List.of(variant),
                        "variant_1",
                        true,
                        true,
                        true,
                        REQUEST.toString()));
        assertRoundTrip(
                GuiWorkflowPayloads.GuiWorkflowFieldUpdate.CODEC,
                new GuiWorkflowPayloads.GuiWorkflowFieldUpdate(
                        SESSION,
                        2L,
                        WORKFLOW,
                        1L,
                        "variant_1",
                        "player",
                        "EnVy"));
        assertRoundTrip(
                GuiWorkflowPayloads.GuiWorkflowSuggestionsRequest.CODEC,
                new GuiWorkflowPayloads.GuiWorkflowSuggestionsRequest(
                        SESSION,
                        3L,
                        WORKFLOW,
                        1L,
                        "variant_1",
                        "player",
                        "En",
                        REQUEST));
        assertRoundTrip(
                GuiWorkflowPayloads.GuiWorkflowSuggestions.CODEC,
                new GuiWorkflowPayloads.GuiWorkflowSuggestions(
                        SESSION,
                        WORKFLOW,
                        1L,
                        REQUEST,
                        "player",
                        List.of(new GuiWorkflowPayloads.WorkflowSuggestion(
                                "EnVy",
                                "EnVy",
                                true))));
        assertRoundTrip(
                GuiWorkflowPayloads.GuiWorkflowPreview.CODEC,
                new GuiWorkflowPayloads.GuiWorkflowPreview(
                        SESSION,
                        4L,
                        WORKFLOW,
                        1L,
                        "variant_1",
                        List.of(value)));
        assertRoundTrip(
                GuiWorkflowPayloads.GuiWorkflowSubmit.CODEC,
                new GuiWorkflowPayloads.GuiWorkflowSubmit(
                        SESSION,
                        5L,
                        WORKFLOW,
                        1L,
                        "variant_1",
                        List.of(value)));
        assertRoundTrip(
                GuiWorkflowPayloads.GuiWorkflowConfirmation.CODEC,
                new GuiWorkflowPayloads.GuiWorkflowConfirmation(
                        SESSION,
                        6L,
                        WORKFLOW,
                        1L,
                        REQUEST.toString()));
        assertRoundTrip(
                GuiWorkflowPayloads.GuiWorkflowProgress.CODEC,
                new GuiWorkflowPayloads.GuiWorkflowProgress(
                        SESSION,
                        WORKFLOW,
                        1L,
                        50,
                        "executing"));
        assertRoundTrip(
                GuiWorkflowPayloads.GuiWorkflowResult.CODEC,
                new GuiWorkflowPayloads.GuiWorkflowResult(
                        SESSION,
                        WORKFLOW,
                        1L,
                        true,
                        true,
                        "complete",
                        "category_core"));
        assertRoundTrip(
                GuiWorkflowPayloads.GuiWorkflowInvalidate.CODEC,
                new GuiWorkflowPayloads.GuiWorkflowInvalidate(
                        SESSION,
                        WORKFLOW,
                        2L,
                        "permission changed"));
        assertRoundTrip(
                GuiWorkflowPayloads.GuiWorkflowClose.CODEC,
                new GuiWorkflowPayloads.GuiWorkflowClose(
                        SESSION,
                        7L,
                        WORKFLOW,
                        2L));
    }

    @Test
    void rejectsMismatchedConfirmationStateAndOversizedLists() {
        GuiWorkflowPayloads.WorkflowVariant variant =
                new GuiWorkflowPayloads.WorkflowVariant("variant_1", "default", List.of());
        assertThrows(
                IllegalArgumentException.class,
                () -> new GuiWorkflowPayloads.GuiWorkflowSnapshot(
                        SESSION,
                        WORKFLOW,
                        1L,
                        "sef:test",
                        "category_core",
                        "Test",
                        "",
                        "",
                        List.of(variant),
                        "variant_1",
                        false,
                        false,
                        true,
                        ""));
        assertThrows(
                IllegalArgumentException.class,
                () -> new GuiWorkflowPayloads.GuiWorkflowSuggestions(
                        SESSION,
                        WORKFLOW,
                        1L,
                        REQUEST,
                        "player",
                        java.util.Collections.nCopies(
                                GuiWorkflowPayloads.MAXIMUM_SUGGESTIONS + 1,
                                new GuiWorkflowPayloads.WorkflowSuggestion(
                                        "value",
                                        "value",
                                        false))));
    }

    @Test
    void boundedBatchTargetFieldRoundTripsAtItsHardLimit() {
        String value = "a".repeat(GuiWorkflowPayloads.MAXIMUM_FIELD_VALUE);
        assertRoundTrip(
                GuiWorkflowPayloads.GuiWorkflowPreview.CODEC,
                new GuiWorkflowPayloads.GuiWorkflowPreview(
                        SESSION,
                        1L,
                        WORKFLOW,
                        1L,
                        "variant_1",
                        List.of(new GuiWorkflowPayloads.WorkflowFieldValue("targets", value))));
        assertThrows(
                IllegalArgumentException.class,
                () -> new GuiWorkflowPayloads.WorkflowFieldValue(
                        "targets",
                        value + "a"));
    }

    private static <T> void assertRoundTrip(StreamCodec<FriendlyByteBuf, T> codec, T value) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            codec.encode(buffer, value);
            assertEquals(value, codec.decode(buffer));
        } finally {
            buffer.release();
        }
    }
}
