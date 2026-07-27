package com.enviouse.sef.gui.protocol;

import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentityProjectionComposerTest {
    @Test
    void emitsViewerSpecificDeltasWithMonotonicTargetRevisions() {
        UUID sessionId = UUID.randomUUID();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        Map<UUID, Component> initialNames = new LinkedHashMap<>();
        initialNames.put(first, Component.literal("First"));
        initialNames.put(second, Component.literal("Second"));

        IdentityProjectionComposer.Update initial = IdentityProjectionComposer.compose(
                sessionId,
                null,
                initialNames).orElseThrow();
        assertTrue(initial.payload().reset());
        assertEquals(2, initial.payload().identities().size());
        assertTrue(IdentityProjectionComposer.compose(
                sessionId,
                initial.state(),
                initialNames).isEmpty());

        IdentityProjectionComposer.Update changed = IdentityProjectionComposer.compose(
                sessionId,
                initial.state(),
                Map.of(first, Component.literal("Renamed"))).orElseThrow();
        assertFalse(changed.payload().reset());
        assertEquals(1, changed.payload().identities().size());
        assertEquals(2L, changed.payload().identities().getFirst().revision());
        assertEquals(java.util.List.of(second), changed.payload().removedPlayerIds());
    }
}
