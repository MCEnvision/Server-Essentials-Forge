package com.enviouse.sef.kernel.policy;

import java.time.Duration;
import java.util.UUID;

@FunctionalInterface
public interface CooldownDurationResolver {
    Resolution resolve(UUID playerId, String actionId, Duration internalDefault);

    record Resolution(
            String actionId,
            String permissionKey,
            Duration duration,
            String provider,
            String winningNode,
            boolean fallback,
            long revision
    ) {
    }
}
