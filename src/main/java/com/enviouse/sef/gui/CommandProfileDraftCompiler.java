package com.enviouse.sef.gui;

import com.enviouse.sef.kernel.command.CommandCatalog;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.kernel.command.PanelContracts;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public final class CommandProfileDraftCompiler {
    private static final Pattern ID = Pattern.compile("[a-z][a-z0-9_.-]{0,63}");
    private final CommandCatalog catalog;
    private final int maximumArguments;

    public CommandProfileDraftCompiler(CommandCatalog catalog, int maximumArguments) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        if (maximumArguments < 1 || maximumArguments > 64) {
            throw new IllegalArgumentException("Profile argument limit is outside hard bounds");
        }
        this.maximumArguments = maximumArguments;
    }

    public Result compile(Draft draft) {
        Objects.requireNonNull(draft, "draft");
        if (!ID.matcher(draft.id()).matches()) {
            return Result.rejected("profile id is invalid");
        }
        CommandDefinition definition = catalog.find(draft.actionId()).orElse(null);
        if (definition == null) {
            return Result.rejected("action id is not cataloged");
        }
        if (draft.arguments().size() > maximumArguments) {
            return Result.rejected("profile argument count exceeds the hard limit");
        }
        for (Map.Entry<String, String> argument : draft.arguments().entrySet()) {
            if (!safe(argument.getKey(), 64) || !safe(argument.getValue(), 256)) {
                return Result.rejected("profile argument is outside bounds");
            }
        }
        if (draft.executionContext() == PanelContracts.ExecutionContext.AS_EACH_PARTICIPANT
                && definition.targetBehavior() != CommandDefinition.TargetBehavior.BOUNDED_PLAYERS) {
            return Result.rejected("participant execution requires a bounded player action");
        }
        if ((draft.executionContext() == PanelContracts.ExecutionContext.SERVER_PROFILE
                || draft.executionContext() == PanelContracts.ExecutionContext.NATIVE_BULK
                || draft.executionContext() == PanelContracts.ExecutionContext.AS_EACH_PARTICIPANT)
                && draft.requiredPermissionIds().isEmpty()) {
            return Result.rejected("delegated profile requires an additional permission");
        }
        return new Result(true, "profile is valid", new CompiledProfile(
                draft.id(),
                definition.id(),
                draft.executionContext(),
                draft.arguments(),
                draft.requiredPermissionIds(),
                definition.auditClass(),
                definition.canonicalRoute()));
    }

    public record Draft(
            String id,
            String actionId,
            PanelContracts.ExecutionContext executionContext,
            Map<String, String> arguments,
            Set<String> requiredPermissionIds
    ) {
        public Draft {
            id = normalize(id);
            actionId = normalize(actionId);
            Objects.requireNonNull(executionContext, "executionContext");
            arguments = Map.copyOf(Objects.requireNonNull(arguments, "arguments"));
            requiredPermissionIds = Set.copyOf(Objects.requireNonNull(requiredPermissionIds, "requiredPermissionIds"));
        }
    }

    public record CompiledProfile(
            String id,
            String actionId,
            PanelContracts.ExecutionContext executionContext,
            Map<String, String> arguments,
            Set<String> requiredPermissionIds,
            com.enviouse.sef.audit.AuditService.AuditClass auditClass,
            String commandFallback
    ) {
    }

    public record Result(boolean accepted, String detail, CompiledProfile profile) {
        private static Result rejected(String detail) {
            return new Result(false, detail, null);
        }
    }

    private static boolean safe(String value, int maximumLength) {
        return value != null && !value.isBlank() && value.length() <= maximumLength
                && value.codePoints().noneMatch(Character::isISOControl);
    }

    private static String normalize(String value) {
        return Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
    }
}
