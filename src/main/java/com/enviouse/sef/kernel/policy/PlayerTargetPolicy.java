package com.enviouse.sef.kernel.policy;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.utils.IMetadataProvider;
import com.mojang.authlib.GameProfile;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

public final class PlayerTargetPolicy {
    private PlayerTargetPolicy() {
    }

    public static TargetHierarchyService.Decision decide(
            CommandSourceStack source,
            ServerPlayer target,
            PermissionNode<Boolean> hierarchyBypass,
            PermissionNode<Boolean> targetExempt,
            PermissionNode<Boolean> exemptionBypass,
            boolean rejectSelf,
            boolean allowEqual
    ) {
        ServerPlayer actor = source.getPlayer();
        boolean console = PermissionService.isConsole(source);
        return KernelServices.hierarchy().decide(new TargetHierarchyService.Context(
                actor == null ? null : actor.getUUID(),
                target.getUUID(),
                console,
                actor != null && PermissionService.has(actor, hierarchyBypass),
                PermissionService.has(target, targetExempt),
                console || actor != null && PermissionService.has(actor, exemptionBypass),
                rejectSelf,
                allowEqual,
                hierarchyWeight(actor),
                hierarchyWeight(target),
                hierarchyTier(actor),
                hierarchyTier(target)));
    }

    public static TargetHierarchyService.Decision decideOffline(
            CommandSourceStack source,
            GameProfile target,
            PermissionNode<Boolean> hierarchyBypass,
            PermissionNode<Boolean> targetExempt,
            PermissionNode<Boolean> exemptionBypass,
            boolean rejectSelf,
            boolean allowEqual
    ) {
        ServerPlayer actor = source.getPlayer();
        boolean console = PermissionService.isConsole(source);
        return KernelServices.hierarchy().decide(new TargetHierarchyService.Context(
                actor == null ? null : actor.getUUID(),
                target.getId(),
                console,
                actor != null && PermissionService.has(actor, hierarchyBypass),
                PermissionService.has(target.getId(), targetExempt),
                console || actor != null && PermissionService.has(actor, exemptionBypass),
                rejectSelf,
                allowEqual,
                hierarchyWeight(actor),
                hierarchyWeight(target),
                hierarchyTier(actor),
                hierarchyTier(target)));
    }

    private static Integer hierarchyWeight(ServerPlayer player) {
        if (player == null) {
            return null;
        }
        if (player.hasPermissions(4)) {
            return 1_000_000;
        }
        IMetadataProvider provider = metadataProvider();
        return provider == null ? null : provider.getHierarchyWeight(player.getGameProfile());
    }

    private static String hierarchyTier(ServerPlayer player) {
        if (player == null) {
            return "";
        }
        if (player.hasPermissions(4)) {
            return "administrator";
        }
        IMetadataProvider provider = metadataProvider();
        if (provider == null) {
            return "player";
        }
        String group = provider.getPrimaryGroup(player.getGameProfile());
        return group == null || group.isBlank() ? "player" : group;
    }

    private static Integer hierarchyWeight(GameProfile profile) {
        IMetadataProvider provider = metadataProvider();
        return provider == null ? null : provider.getHierarchyWeight(profile);
    }

    private static String hierarchyTier(GameProfile profile) {
        IMetadataProvider provider = metadataProvider();
        if (provider == null) {
            return "player";
        }
        String group = provider.getPrimaryGroup(profile);
        return group == null || group.isBlank() ? "player" : group;
    }

    private static IMetadataProvider metadataProvider() {
        return ServerEssentialsForge.instance == null
                ? null
                : ServerEssentialsForge.instance.metadataProvider;
    }
}
