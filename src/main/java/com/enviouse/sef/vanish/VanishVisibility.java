package com.enviouse.sef.vanish;

/**
 * Pure (Minecraft-free) encoding of the SEF vanish visibility invariant, split out from
 * {@link VanishUtil} so it can be unit-tested without booting Minecraft (loading VanishUtil
 * triggers MC static initializers). See {@code VanishVisibilityTest}.
 *
 * <p>Vanish levels: 1 = most hidden, 3 = least hidden, 0 = not vanished.
 * Permission model: {@code sef.vanishsee.N} lets an observer see targets vanished at level N or
 * higher-numbered (vanishsee.1 sees all; vanishsee.2 sees 2..3; vanishsee.3 sees only 3).
 */
public final class VanishVisibility {
    private VanishVisibility() {}

    /**
     * The core visibility decision.
     *
     * @param observerBestSeeLevel the lowest (most powerful) {@code sef.vanishsee.N} the observer
     *                             holds, or 0 if the observer has no vanishsee permission.
     * @param targetVanishLevel    the target's vanish level (1=most hidden, 3=least), or 0 if the
     *                             target is not vanished.
     * @return whether the observer is permitted to see the target.
     */
    public static boolean canSee(int observerBestSeeLevel, int targetVanishLevel) {
        if (targetVanishLevel <= 0)
            return true;  // not vanished -> visible to EVERYONE (full restoration on unvanish)
        if (observerBestSeeLevel <= 0)
            return false; // no sef.vanishsee permission -> cannot see any vanished player
        return observerBestSeeLevel <= targetVanishLevel;
    }
}
