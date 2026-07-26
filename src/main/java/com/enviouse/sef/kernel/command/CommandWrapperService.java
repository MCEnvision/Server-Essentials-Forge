package com.enviouse.sef.kernel.command;

import com.enviouse.sef.kernel.ActionResult;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class CommandWrapperService {
    private static final Set<Origin> FORBIDDEN_ORIGINS = Set.of(
            Origin.ALIAS,
            Origin.PANEL,
            Origin.BUNDLE,
            Origin.SCHEDULE,
            Origin.PROFILE,
            Origin.EXTERNAL_ADAPTER,
            Origin.SUDO,
            Origin.RUN_WRAPPER,
            Origin.SILENT_WRAPPER);

    private CommandWrapperService() {
    }

    public static ActionResult<Preflight> preflight(Request request, int maximumCommandLength) {
        Objects.requireNonNull(request, "request");
        if (maximumCommandLength < 1 || maximumCommandLength > 8192) {
            throw new IllegalArgumentException("Wrapper command length limit is invalid");
        }
        if (request.command().isBlank() || request.command().length() > maximumCommandLength
                || request.command().indexOf('\n') >= 0 || request.command().indexOf('\r') >= 0) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "nested command is invalid");
        }
        if (FORBIDDEN_ORIGINS.contains(request.origin())) {
            return ActionResult.failure(ActionResult.ReasonCode.RECURSION_DENIED, "wrapper origin is forbidden");
        }
        String normalized = request.command().startsWith("/")
                ? request.command().substring(1).trim()
                : request.command().trim();
        int separator = normalized.indexOf(' ');
        String root = (separator < 0 ? normalized : normalized.substring(0, separator)).toLowerCase(Locale.ROOT);
        if (root.equals("run") || root.equals("silent") || root.equals("sudo")) {
            return ActionResult.failure(ActionResult.ReasonCode.RECURSION_DENIED, "wrapper recursion is forbidden");
        }
        return ActionResult.success(new Preflight(
                request.correlationId(),
                request.initiatorId(),
                request.effectiveActorId(),
                request.sourceType(),
                normalized,
                root,
                request.outputMode(),
                request.origin(),
                request.policyRevision(),
                request.commandTreeRevision(),
                request.targets(),
                request.parameters()));
    }

    public record Request(
            UUID correlationId,
            UUID initiatorId,
            UUID effectiveActorId,
            CommandDefinition.SourceType sourceType,
            String command,
            OutputMode outputMode,
            Origin origin,
            long policyRevision,
            long commandTreeRevision,
            List<UUID> targets,
            Map<String, String> parameters
    ) {
        public Request {
            Objects.requireNonNull(correlationId, "correlationId");
            Objects.requireNonNull(sourceType, "sourceType");
            command = Objects.requireNonNull(command, "command").trim();
            Objects.requireNonNull(outputMode, "outputMode");
            Objects.requireNonNull(origin, "origin");
            Objects.requireNonNull(targets, "targets");
            Objects.requireNonNull(parameters, "parameters");
            if (targets.size() > 100 || parameters.size() > 32) {
                throw new IllegalArgumentException("Wrapper request exceeds bounds");
            }
            targets = List.copyOf(Objects.requireNonNull(targets, "targets"));
            parameters = parameters.entrySet().stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
                    entry -> bounded(entry.getKey(), 64),
                    entry -> bounded(entry.getValue(), 256)));
            if (policyRevision < 0 || commandTreeRevision < 0) {
                throw new IllegalArgumentException("Wrapper request exceeds bounds");
            }
        }
    }

    public record Preflight(
            UUID correlationId,
            UUID initiatorId,
            UUID effectiveActorId,
            CommandDefinition.SourceType sourceType,
            String normalizedCommand,
            String normalizedRoot,
            OutputMode outputMode,
            Origin origin,
            long policyRevision,
            long commandTreeRevision,
            List<UUID> targets,
            Map<String, String> parameters
    ) {
    }

    public enum OutputMode {
        NORMAL,
        SUPPRESS_SUCCESS,
        SUPPRESS_ALL_NONMANDATORY
    }

    public enum Origin {
        DIRECT,
        GUI,
        ALIAS,
        PANEL,
        BUNDLE,
        SCHEDULE,
        PROFILE,
        EXTERNAL_ADAPTER,
        SUDO,
        RUN_WRAPPER,
        SILENT_WRAPPER
    }

    private static String bounded(String value, int maximumLength) {
        String normalized = Objects.requireNonNull(value, "value").trim();
        return normalized.length() <= maximumLength
                ? normalized
                : normalized.substring(0, maximumLength);
    }
}
