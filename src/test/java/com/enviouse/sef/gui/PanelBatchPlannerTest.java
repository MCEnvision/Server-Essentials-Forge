package com.enviouse.sef.gui;

import com.enviouse.sef.kernel.command.PanelContracts;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PanelBatchPlannerTest {
    @Test
    void sameTickRequiresSmallAllowlistedFrozenCohortAndExactPermissions() {
        PanelBatchPlanner planner = new PanelBatchPlanner(4, 16, 2, Set.of("sef:test.safe"));
        List<UUID> small = IntStream.range(0, 4).mapToObj(ignored -> UUID.randomUUID()).toList();

        PanelBatchPlanner.Decision sameTick = planner.plan(
                "sef:test.safe",
                PanelContracts.ExecutionContext.NATIVE_BULK,
                small,
                true,
                true);
        assertEquals(PanelBatchPlanner.Mode.SAME_TICK, sameTick.mode());
        assertEquals(small, sameTick.frozenTargets());

        PanelBatchPlanner.Decision paced = planner.plan(
                "sef:test.other",
                PanelContracts.ExecutionContext.NATIVE_BULK,
                small,
                true,
                true);
        assertEquals(PanelBatchPlanner.Mode.PACED, paced.mode());
        assertEquals(2, paced.estimatedTicks());

        assertFalse(planner.plan(
                "sef:test.safe",
                PanelContracts.ExecutionContext.NATIVE_BULK,
                small,
                false,
                true).accepted());
        assertFalse(planner.plan(
                "sef:test.safe",
                PanelContracts.ExecutionContext.NATIVE_BULK,
                small,
                true,
                false).accepted());
        assertTrue(sameTick.accepted());
    }

    @Test
    void oversizedCohortIsRejectedWithoutPartialAdmission() {
        PanelBatchPlanner planner = new PanelBatchPlanner(2, 4, 1, Set.of("sef:test.safe"));
        List<UUID> targets = IntStream.range(0, 5).mapToObj(ignored -> UUID.randomUUID()).toList();
        PanelBatchPlanner.Decision decision = planner.plan(
                "sef:test.safe",
                PanelContracts.ExecutionContext.NATIVE_BULK,
                targets,
                true,
                true);

        assertFalse(decision.accepted());
        assertTrue(decision.frozenTargets().isEmpty());
        assertEquals(PanelBatchPlanner.Mode.DENIED, decision.mode());
    }
}
