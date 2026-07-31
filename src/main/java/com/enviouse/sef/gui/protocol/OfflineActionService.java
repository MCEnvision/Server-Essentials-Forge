package com.enviouse.sef.gui.protocol;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.gui.GuiWorkflowCompiler;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.ExecutionOperationScope;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class OfflineActionService {
    private static final int MAXIMUM_EXECUTIONS_PER_PASS = 64;

    private OfflineActionService() {
    }

    public static void executeReady(MinecraftServer server) {
        Set<UUID> onlineTargets = server.getPlayerList().getPlayers().stream()
                .map(ServerPlayer::getUUID)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        executeReady(server, onlineTargets);
    }

    public static void executeReady(MinecraftServer server, UUID targetId) {
        executeReady(server, Set.of(targetId));
    }

    private static void executeReady(MinecraftServer server, Set<UUID> onlineTargets) {
        deliverNotifications(server, onlineTargets);
        for (OfflineActionRepository.QueuedAction action :
                KernelServices.offlineActions().pendingReady(
                        Instant.now(),
                        onlineTargets,
                        MAXIMUM_EXECUTIONS_PER_PASS)) {
            ServerPlayer target = server.getPlayerList().getPlayer(action.targetId());
            if (target == null) {
                continue;
            }
            OfflineActionRepository.QueuedAction claimed;
            try {
                claimed = KernelServices.offlineActions().claimAndFlush(action.id(), Instant.now());
            } catch (IOException | RuntimeException exception) {
                releaseFailedClaim(action, exception);
                continue;
            }
            if (server.getPlayerList().getPlayer(action.targetId()) == null) {
                try {
                    KernelServices.offlineActions().releaseClaimAndFlush(
                            claimed.id(),
                            Instant.now(),
                            "Target disconnected before execution.");
                } catch (IOException | RuntimeException exception) {
                    ServerEssentialsForge.LOGGER.error(
                            "[SEF] Could not release queued action claim {}",
                            claimed.id(),
                            exception);
                }
                continue;
            }
            execute(server, target, claimed);
        }
        deliverNotifications(server, onlineTargets);
    }

    private static void execute(
            MinecraftServer server,
            ServerPlayer target,
            OfflineActionRepository.QueuedAction action
    ) {
        CommandDefinition definition = KernelServices.catalog().find(action.actionId()).orElse(null);
        CommandSourceStack source = server.createCommandSourceStack().withSuppressedOutput();
        if (!GuiWorkflowService.supportsOfflineQueue(action.actionId())
                || definition == null
                || definition.targetBehavior() == CommandDefinition.TargetBehavior.NONE
                || definition.targetBehavior() == CommandDefinition.TargetBehavior.SELF
                || definition.targetBehavior() == CommandDefinition.TargetBehavior.SERVER
                || !KernelCommandExecutor.canUse(
                source,
                action.actionId())) {
            resolveDurably(
                    action,
                    OfflineActionRepository.ActionState.REVOKED,
                    "Permission, feature state, or command ownership changed.");
            notifyActor(
                    server,
                    action.actorId(),
                    "Queued action " + action.id() + " was revoked before execution.");
            return;
        }
        boolean executionStarted = false;
        try {
            GuiWorkflowCompiler.WorkflowDefinition workflow = GuiWorkflowCompiler.compile(
                    definition,
                    server.getCommands().getDispatcher(),
                    source);
            GuiWorkflowCompiler.Variant variant =
                    workflow.requireVariant(action.variantId());
            if (variant.fields().size() != action.values().size()) {
                throw new IllegalArgumentException("The queued field set changed");
            }
            Map<String, String> values = new LinkedHashMap<>(action.values());
            boolean targetFieldFound = false;
            for (GuiWorkflowCompiler.Field field : variant.fields()) {
                String value = values.get(field.id());
                if (value == null) {
                    throw new IllegalArgumentException("The queued field set is incomplete");
                }
                if (field.id().equals(action.targetFieldId())) {
                    if (field.type() != GuiWorkflowCompiler.FieldType.PLAYER
                            && field.type() != GuiWorkflowCompiler.FieldType.PLAYERS) {
                        throw new IllegalArgumentException("The queued target field changed");
                    }
                    value = exactTargetSelector(target.getUUID());
                    values.put(field.id(), value);
                    targetFieldFound = true;
                }
                GuiWorkflowService.validateField(field, value);
            }
            if (!targetFieldFound) {
                throw new IllegalArgumentException("The queued target field is unavailable");
            }
            String command = GuiWorkflowService.render(variant, values);
            if (!command.equals(definition.canonicalRoute())
                    && !command.startsWith(definition.canonicalRoute() + " ")) {
                throw new IllegalArgumentException("The queued route changed");
            }
            CommandDispatcher<CommandSourceStack> dispatcher =
                    server.getCommands().getDispatcher();
            ParseResults<CommandSourceStack> parsed = dispatcher.parse(command, source);
            if (parsed.getReader().canRead()
                    || parsed.getContext().getCommand() == null
                    || !parsed.getExceptions().isEmpty()) {
                throw new IllegalArgumentException("The queued command no longer parses");
            }
            action = KernelServices.offlineActions().beginAndFlush(action.id(), Instant.now());
            executionStarted = true;
            int result;
            try (ExecutionOperationScope ignored = ExecutionOperationScope.open(
                    action.id(),
                    action.idempotencyKey(),
                    action.actorId())) {
                result = dispatcher.execute(command, source);
            }
            OfflineActionRepository.ActionState state = result > 0
                    ? OfflineActionRepository.ActionState.SUCCEEDED
                    : OfflineActionRepository.ActionState.FAILED;
            String outcome = result > 0
                    ? "The canonical action completed."
                    : "The canonical action completed without changing state.";
            resolveDurably(action, state, outcome);
            notifyActor(server, action.actorId(), "Queued action " + action.id() + " ran for "
                    + target.getGameProfile().getName() + ".");
        } catch (Exception exception) {
            String detail = safeMessage(exception);
            resolveDurably(
                    action,
                    executionStarted
                            ? OfflineActionRepository.ActionState.OUTCOME_UNKNOWN
                            : OfflineActionRepository.ActionState.FAILED,
                    detail);
            notifyActor(
                    server,
                    action.actorId(),
                    "Queued action " + action.id() + " failed, " + detail);
            ServerEssentialsForge.LOGGER.warn(
                    "[SEF] Queued offline action {} failed with idempotency key {}",
                    action.id(),
                    action.idempotencyKey(),
                    exception);
        }
    }

    private static void notifyActor(MinecraftServer server, UUID actorId, String message) {
        ServerPlayer actor = server.getPlayerList().getPlayer(actorId);
        if (actor != null) {
            actor.sendSystemMessage(Component.literal(message));
        }
    }

    private static void resolveDurably(
            OfflineActionRepository.QueuedAction action,
            OfflineActionRepository.ActionState state,
            String outcome
    ) {
        try {
            KernelServices.offlineActions().resolveAndFlush(
                    action.id(),
                    state,
                    outcome,
                    Instant.now());
        } catch (IOException | RuntimeException exception) {
            ServerEssentialsForge.LOGGER.error(
                    "[SEF] Could not durably resolve queued action {} as {}",
                    action.id(),
                    state,
                    exception);
        }
    }

    private static void releaseFailedClaim(
            OfflineActionRepository.QueuedAction action,
            Exception failure
    ) {
        try {
            KernelServices.offlineActions().releaseClaimAndFlush(
                    action.id(),
                    Instant.now(),
                    "Execution claim persistence failed.");
        } catch (IOException | RuntimeException releaseFailure) {
            failure.addSuppressed(releaseFailure);
        }
        ServerEssentialsForge.LOGGER.error(
                "[SEF] Could not durably claim queued action {}",
                action.id(),
                failure);
    }

    private static void deliverNotifications(
            MinecraftServer server,
            Set<UUID> onlineTargets
    ) {
        for (OfflineActionRepository.QueuedAction action :
                KernelServices.offlineActions().pendingNotifications(
                        onlineTargets,
                        MAXIMUM_EXECUTIONS_PER_PASS)) {
            ServerPlayer target = server.getPlayerList().getPlayer(action.targetId());
            if (target == null) {
                continue;
            }
            boolean successful = action.state() == OfflineActionRepository.ActionState.SUCCEEDED
                    || action.state() == OfflineActionRepository.ActionState.COMPLETED;
            target.sendSystemMessage(Component.literal(successful
                    ? "Queued staff action " + action.id() + " completed."
                    : "Queued staff action " + action.id()
                    + " did not complete. Staff can review its recorded outcome."));
            try {
                KernelServices.offlineActions().markNotificationDeliveredAndFlush(
                        action.id(),
                        Instant.now());
            } catch (IOException | RuntimeException exception) {
                ServerEssentialsForge.LOGGER.warn(
                        "[SEF] Could not mark queued action notification {} as delivered",
                        action.id(),
                        exception);
            }
        }
    }

    private static String safeMessage(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return throwable.getClass().getSimpleName();
        }
        String sanitized = message.codePoints()
                .filter(codePoint -> !Character.isISOControl(codePoint))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return sanitized.length() <= 256 ? sanitized : sanitized.substring(0, 256);
    }

    private static String exactTargetSelector(UUID playerId) {
        long most = playerId.getMostSignificantBits();
        long least = playerId.getLeastSignificantBits();
        return "@a[nbt={UUID:[I;"
                + (int) (most >> 32) + ","
                + (int) most + ","
                + (int) (least >> 32) + ","
                + (int) least + "]},limit=1]";
    }
}
