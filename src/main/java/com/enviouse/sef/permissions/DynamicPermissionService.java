package com.enviouse.sef.permissions;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

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

    public static boolean has(UUID playerId, String permission) {
        if (playerId == null
                || permission == null
                || !permission.matches("[a-z0-9_.-]{1,128}")
                || !ModList.get().isLoaded("luckperms")) {
            return false;
        }
        try {
            return LuckPermsDynamicPermission.has(playerId, permission);
        } catch (LinkageError | RuntimeException exception) {
            return false;
        }
    }

    public static Set<String> broadGrants(
            ServerPlayer player,
            Collection<String> protectedPermissions
    ) {
        if (player == null
                || protectedPermissions == null
                || protectedPermissions.isEmpty()
                || !ModList.get().isLoaded("luckperms")) {
            return Set.of();
        }
        try {
            return LuckPermsDynamicPermission.broadGrants(player).stream()
                    .filter(grant -> protectedPermissions.stream()
                            .anyMatch(permission -> broadGrantCovers(grant, permission)))
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
        } catch (LinkageError | RuntimeException exception) {
            return Set.of();
        }
    }

    static boolean broadGrantCovers(String grant, String permission) {
        String normalizedGrant = grant == null ? "" : grant.trim().toLowerCase(Locale.ROOT);
        String normalizedPermission =
                permission == null ? "" : permission.trim().toLowerCase(Locale.ROOT);
        if (normalizedGrant.equals("*")) {
            return !normalizedPermission.isBlank();
        }
        if (!normalizedGrant.endsWith(".*") || normalizedGrant.length() < 3) {
            return false;
        }
        return normalizedPermission.startsWith(
                normalizedGrant.substring(0, normalizedGrant.length() - 1));
    }
}
