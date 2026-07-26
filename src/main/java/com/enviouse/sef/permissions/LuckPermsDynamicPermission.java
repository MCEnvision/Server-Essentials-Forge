package com.enviouse.sef.permissions;

import net.luckperms.api.model.user.User;
import net.minecraft.server.level.ServerPlayer;

final class LuckPermsDynamicPermission {
    private LuckPermsDynamicPermission() {
    }

    static boolean has(ServerPlayer player, String permission) {
        User user = net.luckperms.api.LuckPermsProvider.get()
                .getUserManager()
                .getUser(player.getUUID());
        return user != null
                && user.getCachedData()
                .getPermissionData()
                .checkPermission(permission)
                .asBoolean();
    }
}
