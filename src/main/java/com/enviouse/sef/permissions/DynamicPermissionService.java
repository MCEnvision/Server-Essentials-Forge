package com.enviouse.sef.permissions;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;

public final class DynamicPermissionService {
    private DynamicPermissionService() {
    }

    public static boolean has(ServerPlayer player, String permission) {
        if (player == null
                || permission == null
                || !permission.matches("[a-z0-9_.-]{1,128}")
                || !ModList.get().isLoaded("luckperms")) {
            return false;
        }
        try {
            return LuckPermsDynamicPermission.has(player, permission);
        } catch (LinkageError | RuntimeException exception) {
            return false;
        }
    }
}
