package com.enviouse.sef.gui.protocol;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GuiWorkflowBatchSelectionTest {
    @Test
    void resolvesExplicitSelectionsCaseInsensitivelyAndDeduplicates() {
        UUID alpha = UUID.randomUUID();
        UUID beta = UUID.randomUUID();
        Map<String, UUID> visible = new LinkedHashMap<>();
        visible.put("alpha", alpha);
        visible.put("beta", beta);

        assertEquals(
                List.of(beta, alpha),
                GuiWorkflowService.parseBatchSelection(
                        "Beta,alpha,BETA",
                        visible,
                        Set.of(alpha)));
    }

    @Test
    void resolvesOnlineAndKnownBulkSelectionsFromFrozenVisibility() {
        UUID alpha = UUID.randomUUID();
        UUID beta = UUID.randomUUID();
        UUID gamma = UUID.randomUUID();
        Map<String, UUID> visible = new LinkedHashMap<>();
        visible.put("alpha", alpha);
        visible.put("beta", beta);
        visible.put("gamma", gamma);
        Set<UUID> online = new LinkedHashSet<>(List.of(gamma, alpha));

        assertEquals(
                List.of(alpha, gamma),
                GuiWorkflowService.parseBatchSelection(
                        GuiWorkflowPayloads.PLAYER_SELECTION_ALL_ONLINE,
                        visible,
                        online));
        assertEquals(
                List.of(alpha, beta, gamma),
                GuiWorkflowService.parseBatchSelection(
                        GuiWorkflowPayloads.PLAYER_SELECTION_ALL_KNOWN,
                        visible,
                        online));
    }

    @Test
    void rejectsUnknownEmptyAndOversizedSelections() {
        UUID alpha = UUID.randomUUID();
        assertThrows(
                IllegalArgumentException.class,
                () -> GuiWorkflowService.parseBatchSelection(
                        "hidden",
                        Map.of("alpha", alpha),
                        Set.of()));
        assertThrows(
                IllegalArgumentException.class,
                () -> GuiWorkflowService.parseBatchSelection(
                        GuiWorkflowPayloads.PLAYER_SELECTION_ALL_ONLINE,
                        Map.of("alpha", alpha),
                        Set.of()));

        Map<String, UUID> oversized = new LinkedHashMap<>();
        for (int index = 0; index <= GuiWorkflowPayloads.MAXIMUM_BATCH_TARGETS; index++) {
            oversized.put("player" + index, UUID.randomUUID());
        }
        assertThrows(
                IllegalArgumentException.class,
                () -> GuiWorkflowService.parseBatchSelection(
                        GuiWorkflowPayloads.PLAYER_SELECTION_ALL_KNOWN,
                        oversized,
                        Set.of()));
    }
}
