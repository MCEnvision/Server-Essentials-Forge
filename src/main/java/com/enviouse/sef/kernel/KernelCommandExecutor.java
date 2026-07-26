package com.enviouse.sef.kernel;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.audit.SecurityAuditService;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.kernel.policy.CommandExecutionService;
import com.enviouse.sef.permissions.PermissionService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.rcon.RconConsoleSource;
import net.minecraft.world.level.BaseCommandBlock;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.IntSupplier;

public final class KernelCommandExecutor {
    private KernelCommandExecutor() {
    }

    public static boolean canUse(
            CommandSourceStack source,
            String actionId,
            PermissionNode<Boolean>... additionalPermissions
    ) {
        Objects.requireNonNull(source, "source");
        return permissions(source, definition(actionId), additionalPermissions).granted();
    }

    public static int execute(
            CommandSourceStack source,
            String actionId,
            Map<String, String> normalizedParameters,
            IntSupplier action,
            PermissionNode<Boolean>... additionalPermissions
    ) {
        return execute(
                source,
                actionId,
                normalizedParameters,
                List.of(),
                false,
                action,
                additionalPermissions);
    }

    public static int execute(
            CommandSourceStack source,
            String actionId,
            Map<String, String> normalizedParameters,
            List<UUID> targetIds,
            boolean cooldownBypass,
            IntSupplier action,
            PermissionNode<Boolean>... additionalPermissions
    ) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(normalizedParameters, "normalizedParameters");
        Objects.requireNonNull(targetIds, "targetIds");
        Objects.requireNonNull(action, "action");

        CommandDefinition definition = definition(actionId);
        PermissionSummary permission = permissions(source, definition, additionalPermissions);
        CommandDefinition.SourceType sourceType = sourceType(source);
        UUID actorId = actorId(source, sourceType);
        String dimensionId = dimensionId(source);

        Map<String, String> providerContext = new LinkedHashMap<>(permission.providerContext());
        providerContext.put("source_class", sourceType.name().toLowerCase(Locale.ROOT));
        ActionResult<CommandExecutionService.Lease> started = KernelServices.commandExecutions().begin(
                new CommandExecutionService.Request(
                        SecurityAuditService.currentSessionId(),
                        actorId,
                        Objects.requireNonNullElse(source.getTextName(), ""),
                        definition.id(),
                        sourceType,
                        dimensionId,
                        dimensionId,
                        permission.granted(),
                        cooldownBypass,
                        false,
                        "",
                        null,
                        null,
                        Set.of(),
                        normalizedParameters,
                        targetIds,
                        1L,
                        providerContext,
                        "command"));
        if (!started.successful()) {
            sendFailure(source, started);
            return 0;
        }

        try (CommandExecutionService.Lease lease = started.value()) {
            int result = action.getAsInt();
            ActionResult<Void> completed = lease.complete(
                    result > 0,
                    result > 0 ? null : ActionResult.ReasonCode.PROVIDER_ERROR);
            if (!completed.successful()) {
                if (result > 0) {
                    sendFailure(source, completed);
                }
                return 0;
            }
            return result;
        }
    }

    static CommandDefinition.SourceType sourceType(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer) {
            return CommandDefinition.SourceType.PLAYER;
        }
        if (source.source instanceof RconConsoleSource) {
            return CommandDefinition.SourceType.RCON;
        }
        if (source.source instanceof BaseCommandBlock) {
            return CommandDefinition.SourceType.COMMAND_BLOCK;
        }
        if (source.source instanceof MinecraftServer) {
            return CommandDefinition.SourceType.CONSOLE;
        }
        return CommandDefinition.SourceType.EXTERNAL_ADAPTER;
    }

    private static CommandDefinition definition(String actionId) {
        return KernelServices.catalog().find(actionId)
                .orElseThrow(() -> new IllegalStateException("Unknown kernel action " + actionId));
    }

    private static PermissionSummary permissions(
            CommandSourceStack source,
            CommandDefinition definition,
            PermissionNode<Boolean>[] additionalPermissions
    ) {
        LinkedHashMap<String, PermissionNode<Boolean>> nodes = new LinkedHashMap<>();
        for (String permissionId : definition.permissionIds()) {
            PermissionNode<Boolean> node = KernelServices.permissionNode(permissionId);
            if (node == null) {
                return new PermissionSummary(false, Map.of(
                        "permission_ids", String.join(",", definition.permissionIds()),
                        "permission_providers", "missing_manifest",
                        "permission_default_use", PermissionService.DefaultUse.UNKNOWN.name(),
                        "permission_denials", PermissionService.DenialReason.PROVIDER_UNAVAILABLE.name()));
            }
            nodes.put(node.getNodeName(), node);
        }
        if (additionalPermissions != null) {
            for (PermissionNode<Boolean> node : additionalPermissions) {
                if (node != null) {
                    nodes.putIfAbsent(node.getNodeName(), node);
                }
            }
        }

        boolean granted = true;
        Set<String> providers = new LinkedHashSet<>();
        Set<String> defaultUses = new LinkedHashSet<>();
        Set<String> denialReasons = new LinkedHashSet<>();
        List<String> permissionIds = new ArrayList<>(nodes.size());
        for (PermissionNode<Boolean> node : nodes.values()) {
            PermissionService.Decision decision = PermissionService.decide(source, node);
            permissionIds.add(decision.permissionId());
            providers.add(decision.provider());
            defaultUses.add(decision.defaultUse().name());
            if (!decision.granted()) {
                granted = false;
                denialReasons.add(decision.denialReason().name());
            }
        }

        return new PermissionSummary(granted, Map.of(
                "permission_ids", String.join(",", permissionIds),
                "permission_providers", String.join(",", providers),
                "permission_default_use", String.join(",", defaultUses),
                "permission_denials", denialReasons.isEmpty() ? "none" : String.join(",", denialReasons)));
    }

    private static UUID actorId(CommandSourceStack source, CommandDefinition.SourceType sourceType) {
        if (source.getEntity() instanceof ServerPlayer player && player.getUUID() != null) {
            return player.getUUID();
        }
        String identity = "sef:" + sourceType.name() + ":" + Objects.requireNonNullElse(source.getTextName(), "");
        return UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8));
    }

    private static String dimensionId(CommandSourceStack source) {
        if (source.getLevel() == null || source.getLevel().dimension() == null) {
            return "unknown";
        }
        return source.getLevel().dimension().location().toString();
    }

    private static void sendFailure(CommandSourceStack source, ActionResult<?> result) {
        String message = switch (result.reason()) {
            case FEATURE_DISABLED -> "&cThat feature is currently disabled.";
            case SOURCE_NOT_ALLOWED -> "&cThat command cannot be used from this source.";
            case PERMISSION_DENIED -> "&cYou do not have permission to use that command.";
            case COOLDOWN_ACTIVE -> "&cThat command is on cooldown for &e" + result.detail() + " &cseconds.";
            case WARMUP_ACTIVE -> "&eThat command is warming up.";
            case CONFIRMATION_REQUIRED -> "&eThat command requires confirmation.";
            default -> "&cThat action is unavailable. &7"
                    + result.reason().name().toLowerCase(Locale.ROOT);
        };
        source.sendFailure(TextFormatter.stringToFormattedText(message));
    }

    private record PermissionSummary(boolean granted, Map<String, String> providerContext) {
    }
}
