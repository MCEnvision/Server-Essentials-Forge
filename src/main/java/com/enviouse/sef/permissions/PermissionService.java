package com.enviouse.sef.permissions;

import java.util.UUID;

import com.enviouse.sef.ServerEssentialsForge;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

public final class PermissionService {
    private PermissionService() {
    }

    public static boolean has(CommandSourceStack source, PermissionNode<Boolean> node) {
        return decide(source, node).granted();
    }

    public static Decision decide(CommandSourceStack source, PermissionNode<Boolean> node) {
        if (source.getEntity() instanceof ServerPlayer player) {
            return decide(player, node);
        }
        boolean console = source.getEntity() == null && source.hasPermission(4);
        return new Decision(
                console,
                console ? SubjectKind.CONSOLE : SubjectKind.UNSUPPORTED_SOURCE,
                console ? DenialReason.NONE : DenialReason.SOURCE_NOT_ALLOWED);
    }

    public static boolean has(ServerPlayer player, PermissionNode<Boolean> node) {
        return decide(player, node).granted();
    }

    public static Decision decide(ServerPlayer player, PermissionNode<Boolean> node) {
        try {
            boolean granted = PermissionAPI.getPermission(player, node);
            return new Decision(
                    granted,
                    SubjectKind.ONLINE_PLAYER,
                    granted ? DenialReason.NONE : DenialReason.PERMISSION_DENIED);
        } catch (IllegalStateException exception) {
            ServerEssentialsForge.LOGGER.trace("Permission service unavailable for online player", exception);
            return new Decision(false, SubjectKind.ONLINE_PLAYER, DenialReason.PROVIDER_UNAVAILABLE);
        }
    }

    public static boolean has(UUID playerId, PermissionNode<Boolean> node) {
        return decide(playerId, node).granted();
    }

    public static Decision decide(UUID playerId, PermissionNode<Boolean> node) {
        try {
            boolean granted = PermissionAPI.getOfflinePermission(playerId, node);
            return new Decision(
                    granted,
                    SubjectKind.OFFLINE_PLAYER,
                    granted ? DenialReason.NONE : DenialReason.PERMISSION_DENIED);
        } catch (IllegalStateException exception) {
            ServerEssentialsForge.LOGGER.trace("Permission service unavailable for offline player", exception);
            return new Decision(false, SubjectKind.OFFLINE_PLAYER, DenialReason.PROVIDER_UNAVAILABLE);
        }
    }

    public static boolean isConsole(CommandSourceStack source) {
        return source.getEntity() == null && source.hasPermission(4);
    }

    public record Decision(boolean granted, SubjectKind subjectKind, DenialReason denialReason) {
    }

    public enum SubjectKind {
        ONLINE_PLAYER,
        OFFLINE_PLAYER,
        CONSOLE,
        UNSUPPORTED_SOURCE
    }

    public enum DenialReason {
        NONE,
        PERMISSION_DENIED,
        PROVIDER_UNAVAILABLE,
        SOURCE_NOT_ALLOWED
    }
}
