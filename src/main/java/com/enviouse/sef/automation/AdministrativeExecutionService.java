package com.enviouse.sef.automation;

import com.enviouse.sef.commands.CommandRootPolicy;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.kernel.command.CommandWrapperService;
import com.enviouse.sef.permissions.DelegatedPermissionScope;
import com.enviouse.sef.permissions.EphemeralExecutionGrant;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public final class AdministrativeExecutionService {
    private static final Set<String> UNSUPPRESSIBLE_ROOTS = Set.of(
            "msg", "tell", "w", "tellraw", "title", "playsound", "stopsound", "kick");
    private static final String DELEGATION_HARD_DENIED_ROOTS = String.join(",", Set.of(
            "op", "deop", "stop", "reload", "whitelist", "ban", "ban-ip", "pardon",
            "pardon-ip", "kick", "lp", "luckperms", "permission", "permissions", "sudo",
            "run", "silent", "execute", "function", "schedule", "data", "debug", "save-all",
            "save-off", "save-on", "publish"));

    private volatile Settings settings;
    private final AtomicLong commandTreeRevision = new AtomicLong(1L);

    public AdministrativeExecutionService(Settings settings) {
        this.settings = Objects.requireNonNull(settings, "settings");
    }

    public void configure(Settings replacement) {
        settings = Objects.requireNonNull(replacement, "replacement");
        commandTreeRevision.updateAndGet(current -> current == Long.MAX_VALUE ? 1L : current + 1L);
    }

    public Settings settings() {
        return settings;
    }

    public long commandTreeRevision() {
        return commandTreeRevision.get();
    }

    public void markCommandTreePublished() {
        commandTreeRevision.updateAndGet(current -> current == Long.MAX_VALUE ? 1L : current + 1L);
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
        return previewInternal(initiator, executionSource, command, context, origin, false);
    }

    private ActionResult<Preview> previewInternal(
            CommandSourceStack initiator,
            CommandSourceStack executionSource,
            String command,
            Context context,
            CommandWrapperService.Origin origin,
            boolean delegated
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
        CommandRootPolicy.Decision rootPolicy = rootPolicy(context, normalized.normalizedCommand(), delegated);
        if (!rootPolicy.allowed()) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, rootPolicy.reason());
        }
        DelegationProfile profile = delegated
                ? settings.profileForRoot(normalized.normalizedRoot()).orElse(null)
                : null;
        if (delegated && profile == null) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.POLICY_DENIED,
                    "command root has no published delegation profile");
        }
        if (delegated) {
            String profileDenial = validateDelegatedProfileCommand(
                    profile,
                    executionSource.getPlayer(),
                    normalized.normalizedCommand());
            if (!profileDenial.isEmpty()) {
                return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, profileDenial);
            }
        }
        ParseResults<CommandSourceStack> parse;
        try {
            parse = delegated
                    ? DelegatedPermissionScope.preview(
                            executionSource.getPlayer().getUUID(),
                            normalized.normalizedRoot(),
                            profile.canonicalActionId(),
                            profile.temporarySefPermissionIds(),
                            () -> initiator.getServer()
                                    .getCommands()
                                    .getDispatcher()
                                    .parse(normalized.normalizedCommand(), executionSource))
                    : initiator.getServer()
                            .getCommands()
                            .getDispatcher()
                            .parse(normalized.normalizedCommand(), executionSource);
        } catch (RuntimeException exception) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.PROVIDER_ERROR,
                    "delegated command parse failed");
        }
        if (!parse.getExceptions().isEmpty() || parse.getReader().canRead()) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "nested command does not parse completely");
        }
        String indirection = unsupportedIndirection(parse);
        if (delegated && !indirection.isEmpty()) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, indirection);
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
            return ActionResult.success(initiator.getServer().getCommands().getDispatcher().execute(
                    preview.value().normalizedCommand(),
                    target.createCommandSourceStack()));
        } catch (Exception exception) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.PROVIDER_ERROR,
                    exception.getClass().getSimpleName());
        }
    }

    public ActionResult<DelegatedPreview> previewDelegated(
            CommandSourceStack initiator,
            ServerPlayer target,
            String command
    ) {
        Objects.requireNonNull(target, "target");
        if (!settings.delegationEnabled()) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.FEATURE_DISABLED,
                    "delegated sudo is disabled");
        }
        ActionResult<CommandWrapperService.Preflight> normalizedResult = CommandWrapperService.preflight(
                new CommandWrapperService.Request(
                        UUID.randomUUID(),
                        initiator.getPlayer() == null ? null : initiator.getPlayer().getUUID(),
                        target.getUUID(),
                        CommandDefinition.SourceType.SUDO,
                        command,
                        CommandWrapperService.OutputMode.NORMAL,
                        CommandWrapperService.Origin.DIRECT,
                        0L,
                        0L,
                        List.of(target.getUUID()),
                        Map.of("context", "targeted_actor")),
                settings.maximumCommandLength());
        if (!normalizedResult.successful()) {
            return ActionResult.failure(normalizedResult.reason(), normalizedResult.detail());
        }
        CommandWrapperService.Preflight normalized = normalizedResult.value();
        DelegationProfile profile = settings.profileForRoot(normalized.normalizedRoot()).orElse(null);
        if (profile == null) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.POLICY_DENIED,
                    "command root has no published delegation profile");
        }
        ActionResult<Preview> preview = previewInternal(
                initiator,
                target.createCommandSourceStack().withPermission(profile.maximumTemporaryVanillaPermissionLevel()),
                normalized.normalizedCommand(),
                Context.TARGETED_ACTOR,
                CommandWrapperService.Origin.DIRECT,
                true);
        return preview.successful()
                ? ActionResult.success(new DelegatedPreview(
                        preview.value(),
                        profile,
                        commandTreeRevision()))
                : ActionResult.failure(preview.reason(), preview.detail());
    }

    public CompletableFuture<Suggestions> suggest(
            CommandSourceStack initiator,
            ServerPlayer target,
            String command,
            boolean delegated
    ) {
        Objects.requireNonNull(initiator, "initiator");
        Objects.requireNonNull(target, "target");
        String input = Objects.requireNonNullElse(command, "");
        Settings policy = settings;
        if (input.length() > policy.maximumCommandLength()
                || input.codePoints().anyMatch(codePoint ->
                Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint))
                || delegated && !policy.delegationEnabled()) {
            return CompletableFuture.completedFuture(Suggestions.empty().join());
        }

        String root = suggestionRoot(input);
        boolean rootComplete = input.stripLeading().contains(" ");
        DelegationProfile profile = delegated && rootComplete
                ? policy.profileForRoot(root).orElse(null)
                : null;
        if (rootComplete) {
            CommandRootPolicy.Decision decision = rootPolicy(
                    Context.TARGETED_ACTOR,
                    root,
                    delegated);
            if (!decision.allowed() || delegated && profile == null) {
                return CompletableFuture.completedFuture(Suggestions.empty().join());
            }
        }

        int temporaryLevel = delegated
                ? profile == null
                ? policy.delegationMaximumTemporaryVanillaPermissionLevel()
                : profile.maximumTemporaryVanillaPermissionLevel()
                : 0;
        CommandSourceStack targetSource = delegated
                ? target.createCommandSourceStack().withPermission(temporaryLevel)
                : target.createCommandSourceStack();
        java.util.function.Supplier<CompletableFuture<Suggestions>> operation = () -> {
            ParseResults<CommandSourceStack> parse =
                    initiator.getServer().getCommands().getDispatcher().parse(input, targetSource);
            return initiator.getServer().getCommands().getDispatcher().getCompletionSuggestions(parse);
        };
        CompletableFuture<Suggestions> suggestions;
        try {
            suggestions = delegated && profile != null
                    ? DelegatedPermissionScope.preview(
                            target.getUUID(),
                            root,
                            profile.canonicalActionId(),
                            profile.temporarySefPermissionIds(),
                            operation)
                    : operation.get();
        } catch (RuntimeException exception) {
            return CompletableFuture.completedFuture(Suggestions.empty().join());
        }
        return suggestions.thenApply(result -> rootComplete
                ? result
                : filterRootSuggestions(result, delegated, targetSource, initiator));
    }

    public ActionResult<Integer> sudoRun(
            CommandSourceStack initiator,
            ServerPlayer target,
            String command,
            EphemeralExecutionGrant grant
    ) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(grant, "grant");
        DelegationProfile profile = settings.profileForRoot(grant.normalizedRoot()).orElse(null);
        if (!settings.delegationEnabled()
                || profile == null
                || !profile.id().equals(grant.profileId())
                || profile.revision() != grant.profileRevision()
                || profile.maximumTemporaryVanillaPermissionLevel()
                != grant.maximumTemporaryVanillaPermissionLevel()
                || !profile.temporarySefPermissionIds().equals(grant.temporarySefPermissionIds())
                || !profile.approvedAdapterCapabilities().equals(grant.approvedAdapterCapabilities())
                || commandTreeRevision() != grant.commandTreeRevision()
                || !grant.issuerId().equals(actorId(initiator))
                || !target.getUUID().equals(grant.targetId())
                || !grant.matchesCommand(command)) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.CONFLICT,
                    "delegated execution facts changed");
        }
        try {
            int result = DelegatedPermissionScope.execute(
                    grant,
                    command,
                    profile.canonicalActionId(),
                    () -> {
                        try {
                            return initiator.getServer().getCommands().getDispatcher().execute(
                                    command,
                                    target.createCommandSourceStack().withPermission(
                                            grant.maximumTemporaryVanillaPermissionLevel()));
                        } catch (Exception exception) {
                            throw new DelegatedExecutionException(exception);
                        }
                    });
            return ActionResult.success(result);
        } catch (Exception exception) {
            Throwable cause = exception instanceof DelegatedExecutionException && exception.getCause() != null
                    ? exception.getCause()
                    : exception;
            return ActionResult.failure(ActionResult.ReasonCode.PROVIDER_ERROR, cause.getClass().getSimpleName());
        }
    }

    private CommandRootPolicy.Decision rootPolicy(Context context, String command) {
        return rootPolicy(context, command, false);
    }

    private CommandRootPolicy.Decision rootPolicy(Context context, String command, boolean delegated) {
        if (delegated) {
            return CommandRootPolicy.evaluate(
                    command,
                    settings.delegationAllowedRoots(),
                    settings.sudoDeniedRoots()
                            + "," + settings.delegationDeniedRoots()
                            + "," + DELEGATION_HARD_DENIED_ROOTS,
                    settings.maximumCommandLength());
        }
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

    private Suggestions filterRootSuggestions(
            Suggestions suggestions,
            boolean delegated,
            CommandSourceStack targetSource,
            CommandSourceStack initiator
    ) {
        List<Suggestion> filtered = suggestions.getList().stream()
                .filter(suggestion -> {
                    String root = suggestionRoot(suggestion.getText());
                    var node = initiator.getServer().getCommands().getDispatcher().getRoot().getChild(root);
                    return node != null
                            && node.canUse(targetSource)
                            && rootPolicy(Context.TARGETED_ACTOR, root, delegated).allowed()
                            && (!delegated || settings.profileForRoot(root).isPresent());
                })
                .toList();
        return new Suggestions(suggestions.getRange(), filtered);
    }

    private static String suggestionRoot(String input) {
        String normalized = input == null ? "" : input.stripLeading().toLowerCase(Locale.ROOT);
        int space = normalized.indexOf(' ');
        String root = space < 0 ? normalized : normalized.substring(0, space);
        if (root.startsWith("/")) {
            root = root.substring(1);
        }
        int namespace = root.indexOf(':');
        return namespace < 0 ? root : root.substring(namespace + 1);
    }

    private String unsupportedIndirection(ParseResults<CommandSourceStack> parse) {
        CommandContextBuilder<CommandSourceStack> context = parse.getContext();
        while (context != null) {
            for (var parsedNode : context.getNodes()) {
                var node = parsedNode.getNode();
                if (node.isFork() && !settings.delegationAllowForks()) {
                    return "delegated command forks are not allowed";
                }
                if (node.getRedirect() != null && !settings.delegationAllowRedirects()) {
                    return "delegated command redirects are not allowed";
                }
            }
            context = context.getChild();
        }
        return "";
    }

    private static String validateDelegatedProfileCommand(
            DelegationProfile profile,
            ServerPlayer target,
            String command
    ) {
        if (!profile.id().equals("effect")) {
            return "delegation profile has no bounded command analyzer";
        }
        String[] arguments = command.split("\\s+");
        if (arguments.length < 3) {
            return "effect delegation requires an explicit target";
        }
        String operation = arguments[1].toLowerCase(Locale.ROOT);
        int targetIndex = switch (operation) {
            case "give" -> 2;
            case "clear" -> 2;
            default -> -1;
        };
        if (targetIndex < 0 || targetIndex >= arguments.length) {
            return "effect delegation operation is not supported";
        }
        String targetArgument = arguments[targetIndex];
        if (!targetArgument.equals("@s")
                && !targetArgument.equalsIgnoreCase(target.getGameProfile().getName())
                && !targetArgument.equalsIgnoreCase(target.getUUID().toString())) {
            return "effect delegation may target only the effective actor";
        }
        return "";
    }

    private static UUID actorId(CommandSourceStack source) {
        return source.getEntity() == null
                ? UUID.nameUUIDFromBytes(
                        ("sef:source:" + source.getTextName()).getBytes(StandardCharsets.UTF_8))
                : source.getEntity().getUUID();
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
            int maximumCommandLength,
            boolean delegationEnabled,
            boolean delegationCompatibilityBooleanSyntax,
            boolean delegationRequireTargetConsent,
            boolean delegationAllowSelf,
            int delegationMaximumTemporaryVanillaPermissionLevel,
            int delegationGrantLifetimeSeconds,
            boolean delegationConfirmationRequired,
            boolean delegationNotifyTarget,
            boolean delegationAllowUnknownExternalPermissionChecks,
            boolean delegationAllowRedirects,
            boolean delegationAllowForks,
            boolean delegationAllowAsync,
            String delegationAllowedRoots,
            String delegationDeniedRoots,
            Map<String, DelegationProfile> delegationProfiles
    ) {
        public Settings {
            sudoAllowedRoots = boundedPolicy(sudoAllowedRoots);
            sudoDeniedRoots = boundedPolicy(sudoDeniedRoots);
            serverAllowedRoots = boundedPolicy(serverAllowedRoots);
            serverDeniedRoots = boundedPolicy(serverDeniedRoots);
            actorAllowedRoots = boundedPolicy(actorAllowedRoots);
            actorDeniedRoots = boundedPolicy(actorDeniedRoots);
            delegationAllowedRoots = boundedPolicy(delegationAllowedRoots);
            delegationDeniedRoots = boundedPolicy(delegationDeniedRoots);
            if (maximumCommandLength < 1 || maximumCommandLength > 8192) {
                throw new IllegalArgumentException("Administrative command length is outside bounds");
            }
            if (delegationMaximumTemporaryVanillaPermissionLevel < 0
                    || delegationMaximumTemporaryVanillaPermissionLevel
                    > EphemeralExecutionGrant.hardMaximumVanillaPermissionLevel()
                    || delegationGrantLifetimeSeconds < 1
                    || delegationGrantLifetimeSeconds > 60) {
                throw new IllegalArgumentException("Delegated sudo settings are outside bounds");
            }
            delegationProfiles = Map.copyOf(Objects.requireNonNull(
                    delegationProfiles,
                    "delegationProfiles"));
            if (delegationProfiles.values().stream().anyMatch(profile ->
                    profile.maximumTemporaryVanillaPermissionLevel()
                            > delegationMaximumTemporaryVanillaPermissionLevel)) {
                throw new IllegalArgumentException(
                        "Delegation profile exceeds the configured vanilla permission level");
            }
        }

        public static Settings defaults() {
            DelegationProfile effect = new DelegationProfile(
                    "effect",
                    1L,
                    Set.of("effect"),
                    "minecraft:effect",
                    2,
                    Set.of(),
                    Set.of());
            return new Settings(
                    "msg,tell,w,r,me",
                    "op,deop,stop,reload,run,silent,sudo",
                    "",
                    "run,silent,sudo",
                    "",
                    "run,silent,sudo",
                    1024,
                    false,
                    true,
                    false,
                    false,
                    2,
                    15,
                    true,
                    true,
                    false,
                    false,
                    false,
                    false,
                    "effect",
                    "op,deop,stop,reload,sudo,run,silent,execute,function,schedule",
                    Map.of(effect.id(), effect));
        }

        public Optional<DelegationProfile> profileForRoot(String root) {
            String normalized = Objects.requireNonNull(root, "root").trim().toLowerCase(Locale.ROOT);
            return delegationProfiles.values().stream()
                    .filter(profile -> profile.allowedRoots().contains(normalized))
                    .findFirst();
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

    public record DelegationProfile(
            String id,
            long revision,
            Set<String> allowedRoots,
            String canonicalActionId,
            int maximumTemporaryVanillaPermissionLevel,
            Set<String> temporarySefPermissionIds,
            Set<String> approvedAdapterCapabilities
    ) {
        public DelegationProfile {
            id = boundedIdentifier(id);
            if (revision < 1L) {
                throw new IllegalArgumentException("Delegation profile revision is outside bounds");
            }
            allowedRoots = boundedIdentifiers(allowedRoots);
            canonicalActionId = boundedIdentifier(canonicalActionId);
            if (maximumTemporaryVanillaPermissionLevel < 0
                    || maximumTemporaryVanillaPermissionLevel
                    > EphemeralExecutionGrant.hardMaximumVanillaPermissionLevel()) {
                throw new IllegalArgumentException("Delegation profile permission level is outside bounds");
            }
            temporarySefPermissionIds = boundedIdentifiers(temporarySefPermissionIds);
            approvedAdapterCapabilities = boundedIdentifiers(approvedAdapterCapabilities);
        }

        private static Set<String> boundedIdentifiers(Set<String> values) {
            Objects.requireNonNull(values, "values");
            if (values.size() > 128) {
                throw new IllegalArgumentException("Delegation profile values exceed bounds");
            }
            return values.stream()
                    .map(DelegationProfile::boundedIdentifier)
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
        }

        private static String boundedIdentifier(String value) {
            String normalized = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
            if (!normalized.matches("[a-z0-9][a-z0-9_.:-]{0,127}")) {
                throw new IllegalArgumentException("Delegation profile identifier is invalid");
            }
            return normalized;
        }
    }

    public record DelegatedPreview(
            Preview preview,
            DelegationProfile profile,
            long commandTreeRevision
    ) {
        public DelegatedPreview {
            Objects.requireNonNull(preview, "preview");
            Objects.requireNonNull(profile, "profile");
            if (commandTreeRevision < 1L) {
                throw new IllegalArgumentException("Command tree revision is invalid");
            }
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

    private static final class DelegatedExecutionException extends RuntimeException {
        private DelegatedExecutionException(Throwable cause) {
            super(cause);
        }
    }
}
