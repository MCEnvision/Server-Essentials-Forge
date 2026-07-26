package com.enviouse.sef.vanish;

final class VanishPermissionPolicy {
    private VanishPermissionPolicy() {
    }

    static int reconcileLevel(boolean persistedVanish, int storedLevel, int bestAllowedLevel) {
        if (!persistedVanish || bestAllowedLevel < 1 || bestAllowedLevel > 3) {
            return 0;
        }
        if (storedLevel < 1 || storedLevel > 3) {
            return bestAllowedLevel;
        }
        return Math.max(storedLevel, bestAllowedLevel);
    }

    static boolean canQueueTarget(
            boolean targetsOther,
            boolean queuePermission,
            boolean othersPermission
    ) {
        return queuePermission && (!targetsOther || othersPermission);
    }
}
