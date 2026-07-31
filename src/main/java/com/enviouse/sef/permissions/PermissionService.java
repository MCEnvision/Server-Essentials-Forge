package com.enviouse.sef.permissions;

import java.util.UUID;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import com.enviouse.sef.ServerEssentialsForge;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

public final class PermissionService {
    private static volatile LeaseResolver leaseResolver = LeaseResolver.NONE;
    private static final AtomicLong PROVIDER_REVISION = new AtomicLong(1L);

    private PermissionService() {
    }

    public static void setLeaseResolver(LeaseResolver resolver) {
        leaseResolver = Objects.requireNonNullElse(resolver, LeaseResolver.NONE);
        advanceProviderRevision();
    }

    public static long providerRevision() {
        return PROVIDER_REVISION.get();
    }

    public static long advanceProviderRevision() {
        return PROVIDER_REVISION.updateAndGet(current -> current == Long.MAX_VALUE ? 1L : current + 1L);
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
        if (DelegatedPermissionScope.allows(player.getUUID(), node.getNodeName())) {
            return new Decision(
                    true,
                    node.getNodeName(),
                    "sef:one_execution_delegation",
                    DefaultUse.NOT_USED,
                    Evaluation.NOT_EVALUATED,
                    Evaluation.NOT_EVALUATED,
                    SubjectKind.ONLINE_PLAYER,
                    DenialReason.NONE);
        }
        try {
            if (leaseResolver.decide(player, node.getNodeName()) == LeaseEvaluation.GRANTED) {
                return new Decision(
                        true,
                        node.getNodeName(),
                        "sef:access_lease",
                        DefaultUse.NOT_USED,
                        Evaluation.NOT_EVALUATED,
                        Evaluation.NOT_EVALUATED,
                        SubjectKind.ONLINE_PLAYER,
                        DenialReason.NONE);
            }
        } catch (RuntimeException exception) {
            ServerEssentialsForge.LOGGER.error(
                    "Access lease permission evaluation failed for {}",
                    player.getUUID(),
                    exception);
        }
        boolean permissionApiGranted = false;
        RuntimeException permissionApiFailure = null;
        try {
            permissionApiGranted = PermissionAPI.getPermission(player, node);
        } catch (RuntimeException exception) {
            permissionApiFailure = exception;
            ServerEssentialsForge.LOGGER.trace(
                    "NeoForge permission service unavailable for online player",
                    exception);
        }
        DynamicPermissionService.Decision directProviderDecision = Objects.requireNonNullElse(
                DynamicPermissionService.decision(player, node.getNodeName()),
                DynamicPermissionService.Decision.UNAVAILABLE);
        if (directProviderDecision == DynamicPermissionService.Decision.DENIED) {
            return deniedByDirectProvider(node, SubjectKind.ONLINE_PLAYER);
        }
        boolean directProviderGrant =
                directProviderDecision == DynamicPermissionService.Decision.GRANTED;
        if (permissionApiFailure != null && !directProviderGrant) {
            return unavailable(node, SubjectKind.ONLINE_PLAYER);
        }
        String provider = directProviderGrant ? "luckperms:direct" : providerId();
        boolean granted = permissionApiGranted || directProviderGrant;
        return new Decision(
                granted,
                node.getNodeName(),
                provider,
                defaultUse(provider),
                Evaluation.NOT_EVALUATED,
                Evaluation.NOT_EVALUATED,
                SubjectKind.ONLINE_PLAYER,
                granted ? DenialReason.NONE : DenialReason.PERMISSION_DENIED);
    }

    public static boolean hasProviderOnly(ServerPlayer player, PermissionNode<Boolean> node) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(node, "node");
        boolean permissionApiGranted = false;
        try {
            permissionApiGranted = PermissionAPI.getPermission(player, node);
        } catch (RuntimeException exception) {
            ServerEssentialsForge.LOGGER.trace("Permission provider unavailable for online player", exception);
        }
        DynamicPermissionService.Decision directProviderDecision = Objects.requireNonNullElse(
                DynamicPermissionService.decision(player, node.getNodeName()),
                DynamicPermissionService.Decision.UNAVAILABLE);
        return switch (directProviderDecision) {
            case GRANTED -> true;
            case DENIED -> false;
            case UNDEFINED, UNAVAILABLE -> permissionApiGranted;
        };
    }

    public static boolean has(UUID playerId, PermissionNode<Boolean> node) {
        return decide(playerId, node).granted();
    }

    public static Decision decide(UUID playerId, PermissionNode<Boolean> node) {
        try {
            if (leaseResolver.decide(playerId, node.getNodeName()) == LeaseEvaluation.GRANTED) {
                return new Decision(
                        true,
                        node.getNodeName(),
                        "sef:access_lease",
                        DefaultUse.NOT_USED,
                        Evaluation.NOT_EVALUATED,
                        Evaluation.NOT_EVALUATED,
                        SubjectKind.OFFLINE_PLAYER,
                        DenialReason.NONE);
            }
        } catch (RuntimeException exception) {
            ServerEssentialsForge.LOGGER.error(
                    "Offline access lease permission evaluation failed for {}",
                    playerId,
                    exception);
        }
        boolean permissionApiGranted = false;
        RuntimeException permissionApiFailure = null;
        try {
            permissionApiGranted = PermissionAPI.getOfflinePermission(playerId, node);
        } catch (RuntimeException exception) {
            permissionApiFailure = exception;
            ServerEssentialsForge.LOGGER.trace(
                    "NeoForge permission service unavailable for offline player",
                    exception);
        }
        DynamicPermissionService.Decision directProviderDecision = Objects.requireNonNullElse(
                DynamicPermissionService.decision(playerId, node.getNodeName()),
                DynamicPermissionService.Decision.UNAVAILABLE);
        if (directProviderDecision == DynamicPermissionService.Decision.DENIED) {
            return deniedByDirectProvider(node, SubjectKind.OFFLINE_PLAYER);
        }
        boolean directProviderGrant =
                directProviderDecision == DynamicPermissionService.Decision.GRANTED;
        if (permissionApiFailure != null && !directProviderGrant) {
            return unavailable(node, SubjectKind.OFFLINE_PLAYER);
        }
        String provider = directProviderGrant ? "luckperms:direct" : providerId();
        boolean granted = permissionApiGranted || directProviderGrant;
        return new Decision(
                granted,
                node.getNodeName(),
                provider,
                defaultUse(provider),
                Evaluation.NOT_EVALUATED,
                Evaluation.NOT_EVALUATED,
                SubjectKind.OFFLINE_PLAYER,
                granted ? DenialReason.NONE : DenialReason.PERMISSION_DENIED);
    }

    public static boolean isConsole(CommandSourceStack source) {
        return source.getEntity() == null && source.hasPermission(4);
    }

    private static Decision deniedByDirectProvider(
            PermissionNode<Boolean> node,
            SubjectKind subjectKind
    ) {
        return new Decision(
                false,
                node.getNodeName(),
                "luckperms:direct",
                DefaultUse.NOT_USED,
                Evaluation.NOT_EVALUATED,
                Evaluation.NOT_EVALUATED,
                subjectKind,
                DenialReason.PERMISSION_DENIED);
    }

    public static String providerId() {
        try {
            return provider();
        } catch (RuntimeException exception) {
            return "unavailable";
        }
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

    public enum LeaseEvaluation {
        GRANTED,
        ABSTAIN
    }

    public interface LeaseResolver {
        LeaseResolver NONE = new LeaseResolver() {
            @Override
            public LeaseEvaluation decide(ServerPlayer player, String permissionId) {
                return LeaseEvaluation.ABSTAIN;
            }

            @Override
            public LeaseEvaluation decide(UUID playerId, String permissionId) {
                return LeaseEvaluation.ABSTAIN;
            }
        };

        LeaseEvaluation decide(ServerPlayer player, String permissionId);

        LeaseEvaluation decide(UUID playerId, String permissionId);
    }
}
