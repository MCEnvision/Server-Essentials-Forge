package com.enviouse.sef.permissions;

import com.enviouse.sef.config.PermissionsHandler;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public final class QuotaPermissionResolver {
    private QuotaPermissionResolver() {
    }

    public static Set<String> granted(ServerPlayer player) {
        return granted(player.getUUID());
    }

    public static Set<String> granted(UUID playerId) {
        Set<String> granted = new LinkedHashSet<>();
        PermissionsHandler.quotaTierNodes.forEach((id, node) -> {
            if (PermissionService.has(playerId, node)) {
                granted.add(id);
            }
        });
        return Set.copyOf(granted);
    }
}
