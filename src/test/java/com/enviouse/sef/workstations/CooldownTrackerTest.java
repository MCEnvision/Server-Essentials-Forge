package com.enviouse.sef.workstations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

class CooldownTrackerTest {
    private static final long SECOND = 1_000_000_000L;

    @Test
    void disabledCooldownAlwaysAllowsUse() {
        AtomicLong time = new AtomicLong();
        CooldownTracker tracker = new CooldownTracker(time::get);
        UUID player = UUID.randomUUID();

        assertTrue(tracker.tryUse(player, "craft", 0).allowed());
        assertTrue(tracker.tryUse(player, "craft", 0).allowed());
    }

    @Test
    void activeCooldownReportsRoundedUpSeconds() {
        AtomicLong time = new AtomicLong();
        CooldownTracker tracker = new CooldownTracker(time::get);
        UUID player = UUID.randomUUID();

        assertTrue(tracker.tryUse(player, "craft", 5).allowed());
        time.set(SECOND + 1L);

        CooldownTracker.Result blocked = tracker.tryUse(player, "craft", 5);
        assertFalse(blocked.allowed());
        assertEquals(4L, blocked.remainingSeconds());
    }

    @Test
    void commandsAndPlayersHaveIndependentCooldowns() {
        CooldownTracker tracker = new CooldownTracker(() -> 0L);
        UUID firstPlayer = UUID.randomUUID();
        UUID secondPlayer = UUID.randomUUID();

        assertTrue(tracker.tryUse(firstPlayer, "craft", 10).allowed());
        assertTrue(tracker.tryUse(firstPlayer, "anvil", 10).allowed());
        assertTrue(tracker.tryUse(secondPlayer, "craft", 10).allowed());
        assertFalse(tracker.tryUse(firstPlayer, "craft", 10).allowed());
    }

    @Test
    void clearRemovesEveryCooldown() {
        CooldownTracker tracker = new CooldownTracker(() -> 0L);
        UUID player = UUID.randomUUID();

        tracker.tryUse(player, "repair", 10);
        tracker.clear();

        assertTrue(tracker.tryUse(player, "repair", 10).allowed());
    }

    @Test
    void periodicMaintenancePrunesInactivePlayers() {
        AtomicLong time = new AtomicLong();
        CooldownTracker tracker = new CooldownTracker(time::get);
        for (int index = 0; index < 63; index++) {
            tracker.tryUse(UUID.randomUUID(), "craft", 1);
        }
        assertEquals(63, tracker.trackedPlayers());

        time.set(SECOND * 2);
        tracker.tryUse(UUID.randomUUID(), "craft", 1);
        assertEquals(1, tracker.trackedPlayers());
    }
}
