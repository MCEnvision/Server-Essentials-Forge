package com.enviouse.sef.gui.protocol;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.gui.GuiWorkflowCompiler;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public final class OfflineActionService {
    private static final int MAXIMUM_EXECUTIONS_PER_PASS = 64;

    private OfflineActionService() {
    }

    public static void executeReady(MinecraftServer server) {
        int processed = 0;
        for (OfflineActionRepository.QueuedAction action :
                KernelServices.offlineActions().pendingReady(Instant.now())) {
            if (processed >= MAXIMUM_EXECUTIONS_PER_PASS) {
                return;
            }
            ServerPlayer target = server.getPlayerList().getPlayer(action.targetId());
            ServerPlayer actor = server.getPlayerList().getPlayer(action.actorId());
            if (target == null || actor == null) {
                continue;
            }
            processed++;
            execute(server, actor, target, action);
        }
    }

    private static void execute(
            MinecraftServer server,
            ServerPlayer actor,
            ServerPlayer target,
            OfflineActionRepository.QueuedAction action
    ) {
        CommandDefinition definition = KernelServices.catalog().find(action.actionId()).orElse(null);
        if (!GuiWorkflowService.supportsOfflineQueue(action.actionId())
                || definition == null
                || definition.targetBehavior() == CommandDefinition.TargetBehavior.NONE
                || definition.targetBehavior() == CommandDefinition.TargetBehavior.SELF
                || definition.targetBehavior() == CommandDefinition.TargetBehavior.SERVER
                || !KernelCommandExecutor.canUse(
                actor.createCommandSourceStack(),
                action.actionId())) {
            resolve(
                    action,
                    OfflineActionRepository.ActionState.REVOKED,
                    "Permission, feature state, or command ownership changed.");
            actor.sendSystemMessage(Component.literal(
                    "Queued action " + action.id() + " was revoked before execution."));
            return;
        }
        try {
            GuiWorkflowCompiler.WorkflowDefinition workflow = GuiWorkflowCompiler.compile(
                    definition,
                    server.getCommands().getDispatcher(),
                    actor.createCommandSourceStack());
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
                    value = target.getGameProfile().getName();
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
            CommandSourceStack source = actor.createCommandSourceStack();
            CommandDispatcher<CommandSourceStack> dispatcher =
                    server.getCommands().getDispatcher();
            ParseResults<CommandSourceStack> parsed = dispatcher.parse(command, source);
            if (parsed.getReader().canRead()
                    || parsed.getContext().getCommand() == null
                    || !parsed.getExceptions().isEmpty()) {
                throw new IllegalArgumentException("The queued command no longer parses");
            }
            int result = dispatcher.execute(command, source);
            OfflineActionRepository.ActionState state = result > 0
                    ? OfflineActionRepository.ActionState.COMPLETED
                    : OfflineActionRepository.ActionState.FAILED;
            String outcome = result > 0
                    ? "The canonical action completed."
                    : "The canonical action completed without changing state.";
            resolve(action, state, outcome);
            actor.sendSystemMessage(Component.literal(
                    "Queued action " + action.id() + " ran for "
                            + target.getGameProfile().getName() + "."));
        } catch (Exception exception) {
            String detail = safeMessage(exception);
            resolve(action, OfflineActionRepository.ActionState.FAILED, detail);
            actor.sendSystemMessage(Component.literal(
                    "Queued action " + action.id() + " failed, " + detail));
            ServerEssentialsForge.LOGGER.warn(
                    "[SEF] Queued offline action {} failed",
                    action.id(),
                    exception);
        }
    }

    private static void resolve(
            OfflineActionRepository.QueuedAction action,
            OfflineActionRepository.ActionState state,
            String outcome
    ) {
        KernelServices.offlineActions().resolve(
                action.id(),
                state,
                outcome,
                Instant.now());
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
}
