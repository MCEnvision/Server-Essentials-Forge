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
                node.getNodeName(),
                "minecraft:command_source",
                DefaultUse.NOT_USED,
                Evaluation.NOT_EVALUATED,
                Evaluation.NOT_EVALUATED,
                console ? SubjectKind.CONSOLE : SubjectKind.UNSUPPORTED_SOURCE,
                console ? DenialReason.NONE : DenialReason.SOURCE_NOT_ALLOWED);
    }

    public static boolean has(ServerPlayer player, PermissionNode<Boolean> node) {
        return decide(player, node).granted();
    }

    public static Decision decide(ServerPlayer player, PermissionNode<Boolean> node) {
        try {
            boolean granted = PermissionAPI.getPermission(player, node);
            String provider = provider();
            return new Decision(
                    granted,
                    node.getNodeName(),
                    provider,
                    defaultUse(provider),
                    Evaluation.NOT_EVALUATED,
                    Evaluation.NOT_EVALUATED,
                    SubjectKind.ONLINE_PLAYER,
                    granted ? DenialReason.NONE : DenialReason.PERMISSION_DENIED);
        } catch (RuntimeException exception) {
            ServerEssentialsForge.LOGGER.trace("Permission service unavailable for online player", exception);
            return unavailable(node, SubjectKind.ONLINE_PLAYER);
        }
    }

    public static boolean has(UUID playerId, PermissionNode<Boolean> node) {
        return decide(playerId, node).granted();
    }

    public static Decision decide(UUID playerId, PermissionNode<Boolean> node) {
        try {
            boolean granted = PermissionAPI.getOfflinePermission(playerId, node);
            String provider = provider();
            return new Decision(
                    granted,
                    node.getNodeName(),
                    provider,
                    defaultUse(provider),
                    Evaluation.NOT_EVALUATED,
                    Evaluation.NOT_EVALUATED,
                    SubjectKind.OFFLINE_PLAYER,
                    granted ? DenialReason.NONE : DenialReason.PERMISSION_DENIED);
        } catch (RuntimeException exception) {
            ServerEssentialsForge.LOGGER.trace("Permission service unavailable for offline player", exception);
            return unavailable(node, SubjectKind.OFFLINE_PLAYER);
        }
    }

    public static boolean isConsole(CommandSourceStack source) {
        return source.getEntity() == null && source.hasPermission(4);
    }

    private static Decision unavailable(PermissionNode<Boolean> node, SubjectKind subjectKind) {
        return new Decision(
                false,
                node.getNodeName(),
                "unavailable",
                DefaultUse.UNKNOWN,
                Evaluation.NOT_EVALUATED,
                Evaluation.NOT_EVALUATED,
                subjectKind,
                DenialReason.PROVIDER_UNAVAILABLE);
    }

    private static String provider() {
        var provider = PermissionAPI.getActivePermissionHandler();
        return provider == null ? "unavailable" : provider.toString();
    }

    private static DefaultUse defaultUse(String provider) {
        return "neoforge:default_handler".equals(provider)
                ? DefaultUse.USED
                : DefaultUse.UNKNOWN;
    }

    public record Decision(
            boolean granted,
            String permissionId,
            String provider,
            DefaultUse defaultUse,
            Evaluation hierarchyResult,
            Evaluation exemptionResult,
            SubjectKind subjectKind,
            DenialReason denialReason
    ) {
    }

    public enum Evaluation {
        ALLOWED,
        DENIED,
        NOT_EVALUATED
    }

    public enum DefaultUse {
        USED,
        NOT_USED,
        UNKNOWN
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
