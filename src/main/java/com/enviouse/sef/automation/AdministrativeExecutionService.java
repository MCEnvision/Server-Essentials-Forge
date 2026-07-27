package com.enviouse.sef.automation;

import com.enviouse.sef.commands.CommandRootPolicy;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.kernel.command.CommandWrapperService;
import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class AdministrativeExecutionService {
    private static final Set<String> UNSUPPRESSIBLE_ROOTS = Set.of(
            "msg", "tell", "w", "tellraw", "title", "playsound", "stopsound", "kick");

    private volatile Settings settings;

    public AdministrativeExecutionService(Settings settings) {
        this.settings = Objects.requireNonNull(settings, "settings");
    }

    public void configure(Settings replacement) {
        settings = Objects.requireNonNull(replacement, "replacement");
    }

    public Settings settings() {
        return settings;
    }

    public ActionResult<Preview> preview(
            CommandSourceStack initiator,
            String command,
            Context context,
            CommandWrapperService.Origin origin
    ) {
        return preview(initiator, sourceForPreview(initiator, context), command, context, origin);
    }

    public ActionResult<Preview> preview(
            CommandSourceStack initiator,
            CommandSourceStack executionSource,
            String command,
            Context context,
            CommandWrapperService.Origin origin
    ) {
        Objects.requireNonNull(initiator, "initiator");
        Objects.requireNonNull(executionSource, "executionSource");
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(origin, "origin");
        CommandDefinition.SourceType sourceType = switch (context) {
            case ACTOR -> CommandDefinition.SourceType.PLAYER;
            case TARGETED_ACTOR -> CommandDefinition.SourceType.SUDO;
            case SERVER -> CommandDefinition.SourceType.RUN_SERVER_WRAPPER;
            case SILENT_ACTOR -> CommandDefinition.SourceType.SILENT_ACTOR_WRAPPER;
            case SILENT_SERVER -> CommandDefinition.SourceType.SILENT_SERVER_WRAPPER;
        };
        CommandWrapperService.OutputMode outputMode = switch (context) {
            case SILENT_ACTOR, SILENT_SERVER ->
                    CommandWrapperService.OutputMode.SUPPRESS_ALL_NONMANDATORY;
            default -> CommandWrapperService.OutputMode.NORMAL;
        };
        UUID initiatorId = initiator.getPlayer() == null ? null : initiator.getPlayer().getUUID();
        CommandWrapperService.Request request = new CommandWrapperService.Request(
                UUID.randomUUID(),
                initiatorId,
                initiatorId,
                sourceType,
                command,
                outputMode,
                origin,
                0L,
                0L,
                List.of(),
                Map.of("context", context.name().toLowerCase(Locale.ROOT)));
        ActionResult<CommandWrapperService.Preflight> preflight =
                CommandWrapperService.preflight(request, settings.maximumCommandLength());
        if (!preflight.successful()) {
            return ActionResult.failure(preflight.reason(), preflight.detail());
        }
        CommandWrapperService.Preflight normalized = preflight.value();
        CommandRootPolicy.Decision rootPolicy = rootPolicy(context, normalized.normalizedCommand());
        if (!rootPolicy.allowed()) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, rootPolicy.reason());
        }
        ParseResults<CommandSourceStack> parse = initiator.getServer()
                .getCommands()
                .getDispatcher()
                .parse(normalized.normalizedCommand(), executionSource);
        if (!parse.getExceptions().isEmpty() || parse.getReader().canRead()) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "nested command does not parse completely");
        }
        SilenceCapability silence = UNSUPPRESSIBLE_ROOTS.contains(normalized.normalizedRoot())
                ? SilenceCapability.SOURCE_FEEDBACK_ONLY
                : SilenceCapability.SUPPORTED;
        return ActionResult.success(new Preview(
                normalized.correlationId(),
                context,
                normalized.normalizedCommand(),
                normalized.normalizedRoot(),
                silence,
                context == Context.SERVER || context == Context.SILENT_SERVER,
                context == Context.SILENT_ACTOR || context == Context.SILENT_SERVER,
                "command journal, security audit, file logging, command spy, and independent mod output remain active"));
    }

    public ActionResult<Integer> runServer(CommandSourceStack initiator, String command) {
        ActionResult<Preview> preview = preview(
                initiator,
                command,
                Context.SERVER,
                CommandWrapperService.Origin.DIRECT);
        if (!preview.successful()) {
            return ActionResult.failure(preview.reason(), preview.detail());
        }
        MinecraftServer server = initiator.getServer();
        try {
            int result = server.getCommands().getDispatcher().execute(
                    preview.value().normalizedCommand(),
                    server.createCommandSourceStack());
            return ActionResult.success(result);
        } catch (Exception exception) {
            return ActionResult.failure(ActionResult.ReasonCode.PROVIDER_ERROR, exception.getClass().getSimpleName());
        }
    }

    public ActionResult<Integer> silentActor(
            CommandSourceStack initiator,
            String command,
            boolean acceptUnsuppressible
    ) {
        ActionResult<Preview> preview = preview(
                initiator,
                command,
                Context.SILENT_ACTOR,
                CommandWrapperService.Origin.DIRECT);
        if (!preview.successful()) {
            return ActionResult.failure(preview.reason(), preview.detail());
        }
        if (preview.value().silenceCapability() != SilenceCapability.SUPPORTED && !acceptUnsuppressible) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.POLICY_DENIED,
                    "command may create independent output. explicit unsuppressible permission is required");
        }
        try {
            int result = initiator.getServer().getCommands().getDispatcher().execute(
                    preview.value().normalizedCommand(),
                    initiator.withSuppressedOutput());
            return ActionResult.success(result);
        } catch (Exception exception) {
            return ActionResult.failure(ActionResult.ReasonCode.PROVIDER_ERROR, exception.getClass().getSimpleName());
        }
    }

    public ActionResult<Integer> silentServer(
            CommandSourceStack initiator,
            String command,
            boolean acceptUnsuppressible
    ) {
        ActionResult<Preview> preview = preview(
                initiator,
                command,
                Context.SILENT_SERVER,
                CommandWrapperService.Origin.DIRECT);
        if (!preview.successful()) {
            return ActionResult.failure(preview.reason(), preview.detail());
        }
        if (preview.value().silenceCapability() != SilenceCapability.SUPPORTED && !acceptUnsuppressible) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.POLICY_DENIED,
                    "command may create independent output. explicit unsuppressible permission is required");
        }
        try {
            MinecraftServer server = initiator.getServer();
            int result = server.getCommands().getDispatcher().execute(
                    preview.value().normalizedCommand(),
                    server.createCommandSourceStack().withSuppressedOutput());
            return ActionResult.success(result);
        } catch (Exception exception) {
            return ActionResult.failure(ActionResult.ReasonCode.PROVIDER_ERROR, exception.getClass().getSimpleName());
        }
    }

    public ActionResult<Integer> sudoRun(
            CommandSourceStack initiator,
            ServerPlayer target,
            String command
    ) {
        Objects.requireNonNull(target, "target");
        ActionResult<Preview> preview = preview(
                initiator,
                target.createCommandSourceStack(),
                command,
                Context.TARGETED_ACTOR,
                CommandWrapperService.Origin.DIRECT);
        if (!preview.successful()) {
            return ActionResult.failure(preview.reason(), preview.detail());
        }
        try {
            int result = initiator.getServer().getCommands().getDispatcher().execute(
                    preview.value().normalizedCommand(),
                    target.createCommandSourceStack());
            return ActionResult.success(result);
        } catch (Exception exception) {
            return ActionResult.failure(ActionResult.ReasonCode.PROVIDER_ERROR, exception.getClass().getSimpleName());
        }
    }

    private CommandRootPolicy.Decision rootPolicy(Context context, String command) {
        return switch (context) {
            case TARGETED_ACTOR -> CommandRootPolicy.evaluate(
                    command,
                    settings.sudoAllowedRoots(),
                    settings.sudoDeniedRoots(),
                    settings.maximumCommandLength());
            case SERVER, SILENT_SERVER -> CommandRootPolicy.evaluate(
                    command,
                    settings.serverAllowedRoots(),
                    settings.serverDeniedRoots(),
                    settings.maximumCommandLength());
            case ACTOR, SILENT_ACTOR -> CommandRootPolicy.evaluate(
                    command,
                    settings.actorAllowedRoots(),
                    settings.actorDeniedRoots(),
                    settings.maximumCommandLength());
        };
    }

    private static CommandSourceStack sourceForPreview(CommandSourceStack initiator, Context context) {
        return switch (context) {
            case SERVER -> initiator.getServer().createCommandSourceStack();
            case SILENT_SERVER -> initiator.getServer().createCommandSourceStack().withSuppressedOutput();
            case SILENT_ACTOR -> initiator.withSuppressedOutput();
            case ACTOR, TARGETED_ACTOR -> initiator;
        };
    }

    public record Settings(
            String sudoAllowedRoots,
            String sudoDeniedRoots,
            String serverAllowedRoots,
            String serverDeniedRoots,
            String actorAllowedRoots,
            String actorDeniedRoots,
            int maximumCommandLength
    ) {
        public Settings {
            sudoAllowedRoots = boundedPolicy(sudoAllowedRoots);
            sudoDeniedRoots = boundedPolicy(sudoDeniedRoots);
            serverAllowedRoots = boundedPolicy(serverAllowedRoots);
            serverDeniedRoots = boundedPolicy(serverDeniedRoots);
            actorAllowedRoots = boundedPolicy(actorAllowedRoots);
            actorDeniedRoots = boundedPolicy(actorDeniedRoots);
            if (maximumCommandLength < 1 || maximumCommandLength > 8192) {
                throw new IllegalArgumentException("Administrative command length is outside bounds");
            }
        }

        public static Settings defaults() {
            return new Settings(
                    "msg,tell,w,r,me",
                    "op,deop,stop,reload,run,silent,sudo",
                    "",
                    "run,silent,sudo",
                    "",
                    "run,silent,sudo",
                    1024);
        }

        private static String boundedPolicy(String value) {
            String bounded = Objects.requireNonNullElse(value, "").trim().toLowerCase(Locale.ROOT);
            if (bounded.length() > 4096
                    || bounded.codePoints().anyMatch(Character::isISOControl)) {
                throw new IllegalArgumentException("Administrative root policy is outside bounds");
            }
            return bounded;
        }
    }

    public record Preview(
            UUID correlationId,
            Context context,
            String normalizedCommand,
            String root,
            SilenceCapability silenceCapability,
            boolean serverAuthority,
            boolean suppressesSourceFeedback,
            String mandatoryObservation
    ) {
    }

    public enum Context {
        ACTOR,
        TARGETED_ACTOR,
        SERVER,
        SILENT_ACTOR,
        SILENT_SERVER
    }

    public enum SilenceCapability {
        SUPPORTED,
        SOURCE_FEEDBACK_ONLY
    }
}
