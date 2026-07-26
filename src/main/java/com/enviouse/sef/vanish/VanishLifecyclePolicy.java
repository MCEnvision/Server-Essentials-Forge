package com.enviouse.sef.vanish;

import java.util.Objects;
import java.util.function.BooleanSupplier;

public final class VanishLifecyclePolicy {
    private VanishLifecyclePolicy() {
    }

    public static boolean canFilterPackets(boolean serverConfigLoaded) {
        return serverConfigLoaded;
    }

    public static boolean shouldUseFilteredStatus(
            boolean serverConfigLoaded,
            boolean filteredStatusAvailable,
            BooleanSupplier hidePlayersFromLists
    ) {
        Objects.requireNonNull(hidePlayersFromLists, "hidePlayersFromLists");
        return serverConfigLoaded
                && filteredStatusAvailable
                && hidePlayersFromLists.getAsBoolean();
    }
}
