package com.enviouse.sef.moderation;

import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.vanish.VanishUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

/** Shared target admission for legacy moderation command routes. */
public final class LegacyTargetPolicy {
    private LegacyTargetPolicy() {
    }

    public static boolean mayTarget(
            CommandSourceStack source,
            ServerPlayer target,
            String exemptionPermission,
            boolean rejectSelf
    ) {
        if (source == null || target == null) {
            return false;
        }
        if (source.getPlayer() != null && VanishUtil.isVanished(target, source.getPlayer())) {
            return false;
        }
        return PlayerTargetPolicy.decide(
                source,
                target,
                PermissionsHandler.phasePermission("moderation.hierarchy.bypass"),
                PermissionsHandler.phasePermission(exemptionPermission),
                PermissionsHandler.phasePermission("moderation.bypass.exempt"),
                rejectSelf,
                true).allowed();
    }
}
