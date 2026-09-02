package com.enviouse.sef.gui.protocol;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HudDeltaComposerTest {
    @Test
    void emitsResetThenOnlyChangedAndRemovedTiles() {
        UUID sessionId = UUID.randomUUID();
        SefPayloads.HudTile vanish = new SefPayloads.HudTile(
                "vanish",
                "Vanish level 1",
                SefPayloads.Severity.NOTICE);
        SefPayloads.HudTile spy = new SefPayloads.HudTile(
                "command_spy",
                "Command spy enabled",
                SefPayloads.Severity.WARNING,
                SefPayloads.HudSurface.ALERT,
                0);

        HudDeltaComposer.Update initial = HudDeltaComposer.compose(
                sessionId,
                null,
                List.of(vanish, spy)).orElseThrow();
        assertTrue(initial.delta().reset());
        assertEquals(List.of(vanish, spy), initial.delta().tiles());
        assertTrue(initial.delta().removedIds().isEmpty());
        assertTrue(HudDeltaComposer.compose(
                sessionId,
                initial.state(),
                List.of(vanish, spy)).isEmpty());

        SefPayloads.HudTile changed = new SefPayloads.HudTile(
                "vanish",
                "Vanish level 2",
                SefPayloads.Severity.NOTICE);
        HudDeltaComposer.Update delta = HudDeltaComposer.compose(
                sessionId,
                initial.state(),
                List.of(changed)).orElseThrow();
        assertFalse(delta.delta().reset());
        assertEquals(List.of(changed), delta.delta().tiles());
        assertEquals(List.of("command_spy"), delta.delta().removedIds());
    }

    @Test
    void rejectsDuplicateTilesAndOverlappingDeltaOperations() {
        SefPayloads.HudTile tile = new SefPayloads.HudTile(
                "afk",
                "AFK",
                SefPayloads.Severity.INFO);
        assertThrows(IllegalArgumentException.class, () -> HudDeltaComposer.compose(
                UUID.randomUUID(),
                null,
                List.of(tile, tile)));
        assertThrows(IllegalArgumentException.class, () -> new SefPayloads.HudDelta(
                UUID.randomUUID(),
                1,
                false,
                List.of(tile),
                List.of(tile.id())));
    }
}
