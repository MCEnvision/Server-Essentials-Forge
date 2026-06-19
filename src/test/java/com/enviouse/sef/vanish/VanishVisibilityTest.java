package com.enviouse.sef.vanish;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Pins the SEF vanish visibility invariant in BOTH directions, without booting Minecraft, by
 * exercising {@link VanishVisibility#canSee(int, int)} — the decision that drives which observers
 * receive the add/remove player-info + entity packets in {@code VanishingHandler.sendPacketsOnVanish}.
 *
 * <p>A wrong decision here is the classic silent vanish regression (a hidden player is seen, or a
 * visible player is missing) that no compile check catches and a quick playtest misses.
 *
 * <p>Legend — target: 1 = most hidden, 3 = least hidden, 0 = not vanished;
 * observer best {@code sef.vanishsee} level: 1 = most powerful (sees all), 3 = weakest, 0 = no permission.
 */
class VanishVisibilityTest {

    @Nested
    @DisplayName("'fails to hide' guard — a vanished player is hidden from observers without sufficient permission")
    class HidesFromTheWrongObservers {
        @Test
        void noPermissionSeesNoVanishedPlayer() {
            assertFalse(VanishVisibility.canSee(0, 1));
            assertFalse(VanishVisibility.canSee(0, 2));
            assertFalse(VanishVisibility.canSee(0, 3));
        }

        @Test
        void weakerPermissionCannotSeeAMoreHiddenTarget() {
            assertFalse(VanishVisibility.canSee(3, 1)); // vanishsee.3 cannot see a level-1 target
            assertFalse(VanishVisibility.canSee(3, 2)); // vanishsee.3 cannot see a level-2 target
            assertFalse(VanishVisibility.canSee(2, 1)); // vanishsee.2 cannot see a level-1 target
        }
    }

    @Nested
    @DisplayName("a vanished player IS visible to observers with sufficient permission")
    class ShowsToTheRightObservers {
        @Test
        void level1OnlyVisibleToVanishsee1() {
            assertTrue(VanishVisibility.canSee(1, 1));
            assertFalse(VanishVisibility.canSee(2, 1));
            assertFalse(VanishVisibility.canSee(3, 1));
        }

        @Test
        void level2VisibleToVanishsee1And2() {
            assertTrue(VanishVisibility.canSee(1, 2));
            assertTrue(VanishVisibility.canSee(2, 2));
            assertFalse(VanishVisibility.canSee(3, 2));
        }

        @Test
        void level3VisibleToAnyVanishsee() {
            assertTrue(VanishVisibility.canSee(1, 3));
            assertTrue(VanishVisibility.canSee(2, 3));
            assertTrue(VanishVisibility.canSee(3, 3));
        }
    }

    @Nested
    @DisplayName("'stuck partially visible' guard — unvanish fully restores visibility to EVERYONE")
    class FullRestorationOnUnvanish {
        @Test
        void unvanishedPlayerIsVisibleToEveryObserverIncludingNoPermission() {
            for (int observer = 0; observer <= 3; observer++) {
                assertTrue(VanishVisibility.canSee(observer, 0),
                        "an unvanished player must be visible to observer best-see-level " + observer);
            }
        }

        @Test
        void observerBlockedWhileVanishedCanSeeAfterUnvanish() {
            // While vanished at the most-hidden level, a no-permission observer cannot see them...
            assertFalse(VanishVisibility.canSee(0, 1));
            // ...and after unvanish (level 0) that SAME observer can — full round-trip restoration.
            assertTrue(VanishVisibility.canSee(0, 0));
        }
    }

    @Test
    @DisplayName("full visibility matrix (observerBestSeeLevel × targetVanishLevel)")
    void fullVisibilityMatrix() {
        for (int observer = 0; observer <= 3; observer++) {
            for (int target = 0; target <= 3; target++) {
                boolean expected = (target == 0)                  // not vanished -> visible to everyone
                        || (observer >= 1 && observer <= target); // vanishsee.N sees levels N..3
                assertEquals(expected, VanishVisibility.canSee(observer, target),
                        "canSee(observerBestSeeLevel=" + observer + ", targetVanishLevel=" + target + ")");
            }
        }
    }
}
