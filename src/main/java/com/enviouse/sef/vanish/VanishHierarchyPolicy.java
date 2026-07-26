package com.enviouse.sef.vanish;

public final class VanishHierarchyPolicy {
    private VanishHierarchyPolicy() {
    }

    public static boolean canTarget(
            boolean console,
            boolean hierarchyBypass,
            int executorBestLevel,
            int targetBestLevel
    ) {
        if (console || hierarchyBypass) return true;
        if (executorBestLevel <= 0) return false;
        return targetBestLevel <= 0 || executorBestLevel <= targetBestLevel;
    }
}
