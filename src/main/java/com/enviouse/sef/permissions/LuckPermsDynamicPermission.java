package com.enviouse.sef.permissions;

import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.util.Tristate;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

final class LuckPermsDynamicPermission {
    private LuckPermsDynamicPermission() {
    }

    static boolean has(ServerPlayer player, String permission) {
        return decide(player.getUUID(), permission) == HierarchicalPermissionResolver.Evaluation.GRANTED;
    }

    static boolean has(UUID playerId, String permission) {
        return decide(playerId, permission) == HierarchicalPermissionResolver.Evaluation.GRANTED;
    }

    static HierarchicalPermissionResolver.Evaluation decide(ServerPlayer player, String permission) {
        return decide(player.getUUID(), permission);
    }

    static HierarchicalPermissionResolver.Evaluation decide(UUID playerId, String permission) {
        User user = net.luckperms.api.LuckPermsProvider.get()
                .getUserManager()
                .getUser(playerId);
        return user == null
                ? HierarchicalPermissionResolver.Evaluation.UNDEFINED
                : HierarchicalPermissionResolver.resolveDecision(
                permission,
                candidate -> evaluation(user.getCachedData()
                        .getPermissionData()
                        .checkPermission(candidate)));
    }

    private static HierarchicalPermissionResolver.Evaluation evaluation(Tristate value) {
        return switch (Objects.requireNonNullElse(value, Tristate.UNDEFINED)) {
            case TRUE -> HierarchicalPermissionResolver.Evaluation.GRANTED;
            case FALSE -> HierarchicalPermissionResolver.Evaluation.DENIED;
            case UNDEFINED -> HierarchicalPermissionResolver.Evaluation.UNDEFINED;
        };
    }

    static PermissionCooldownResolver.GrantSnapshot cooldownSnapshot(UUID playerId, String prefix) {
        User user = net.luckperms.api.LuckPermsProvider.get()
                .getUserManager()
                .getUser(playerId);
        if (user == null) {
            return new PermissionCooldownResolver.GrantSnapshot(
                    "luckperms",
                    false,
                    true,
                    Set.of(),
                    Set.of());
        }
        Set<String> direct = new LinkedHashSet<>();
        user.getNodes(NodeType.PERMISSION).stream()
                .filter(PermissionNode::getValue)
                .filter(node -> !node.hasExpired())
                .filter(node -> user.getQueryOptions().satisfies(node.getContexts()))
                .map(PermissionNode::getPermission)
                .filter(permission -> permission.startsWith(prefix))
                .forEach(direct::add);
        Set<String> inherited = new LinkedHashSet<>();
        user.getCachedData().getPermissionData().getPermissionMap().forEach((permission, granted) -> {
            if (Boolean.TRUE.equals(granted) && permission.startsWith(prefix)) {
                inherited.add(permission);
            }
        });
        return new PermissionCooldownResolver.GrantSnapshot(
                "luckperms",
                true,
                true,
                direct,
                inherited);
    }

    static Set<String> broadGrants(ServerPlayer player) {
        User user = net.luckperms.api.LuckPermsProvider.get()
                .getUserManager()
                .getUser(player.getUUID());
        if (user == null) {
            return Set.of();
        }
        Set<String> grants = new LinkedHashSet<>();
        user.getNodes(NodeType.PERMISSION).stream()
                .filter(PermissionNode::getValue)
                .filter(node -> !node.hasExpired())
                .filter(node -> user.getQueryOptions().satisfies(node.getContexts()))
                .map(PermissionNode::getPermission)
                .filter(LuckPermsDynamicPermission::wildcard)
                .forEach(grants::add);
        user.getCachedData().getPermissionData().getPermissionMap().forEach((permission, granted) -> {
            if (Boolean.TRUE.equals(granted) && wildcard(permission)) {
                grants.add(permission);
            }
        });
        return Set.copyOf(grants);
    }

    private static boolean wildcard(String permission) {
        return permission.equals("*") || permission.endsWith(".*");
    }
}
