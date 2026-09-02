package com.enviouse.sef.automation;

import com.enviouse.sef.audit.SecurityAuditService;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.command.BundleCompiler;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.kernel.policy.FeatureGateService;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.permissions.DynamicPermissionService;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.vanish.VanishUtil;
import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class AutomationRuntime {
    private static final int MAXIMUM_FAKE_EVENTS_PER_TICK = 8;

    private AutomationRuntime() {
    }

    public static void tick(MinecraftServer server) {
        Objects.requireNonNull(server, "server");
        tickBundles(server);
        tickFakeScenes(server);
    }

    private static void tickBundles(MinecraftServer server) {
        KernelServices.bundles().tick(
                Instant.now(),
                (job, definition, step, targets) -> revalidate(server, job, definition, step, targets),
                new RuntimeStepExecutor(server));
    }

    private static ActionResult<Void> revalidate(
            MinecraftServer server,
            BundleService.RuntimeJob job,
            BundleCompiler.BundleDefinition definition,
            BundleCompiler.BundleStep step,
            List<UUID> targets
    ) {
        ServerPlayer issuer = server.getPlayerList().getPlayer(job.issuerId());
        if (issuer == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "bundle issuer is offline");
        }
        if (definition.authorizationMode() != BundleCompiler.AuthorizationMode.STRICT_ACTOR) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.POLICY_DENIED,
                    "delegated bundle profiles are disabled until separately published");
        }
        if (!definition.additionalPermissionId().isBlank()
                && !hasQualified(issuer, definition.additionalPermissionId())) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.PERMISSION_DENIED,
                    "bundle additional permission was lost");
        }
        if (targets.isEmpty() || targets.size() > definition.maximumTargets()) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "bundle target cap changed");
        }
        for (UUID targetId : targets) {
            ServerPlayer target = server.getPlayerList().getPlayer(targetId);
            if (target == null) {
                return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "bundle target is offline");
            }
            if (!target.getUUID().equals(issuer.getUUID()) && !eligible(issuer, target)) {
                return ActionResult.failure(ActionResult.ReasonCode.TARGET_DENIED, "bundle target policy changed");
            }
        }
        if (step.kind() == BundleCompiler.StepKind.SEF_ACTION) {
            CommandDefinition action = KernelServices.catalog().find(step.targetId()).orElse(null);
            if (action == null || wrapperAction(action.id())) {
                return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "bundle action is unavailable");
            }
            FeatureGateService.Decision feature = KernelServices.featureGates().decide(
                    action.featureId(),
                    FeatureGateService.Context.server(action.id()));
            if (!feature.enabled() || !KernelCommandExecutor.canUse(issuer.createCommandSourceStack(), action.id())) {
                return ActionResult.failure(
                        feature.enabled()
                                ? ActionResult.ReasonCode.PERMISSION_DENIED
                                : ActionResult.ReasonCode.FEATURE_DISABLED,
                        feature.enabled() ? "bundle action permission was lost" : feature.explanation());
            }
        }
        if (step.kind() == BundleCompiler.StepKind.EXTERNAL_ACTOR_COMMAND
                || step.kind() == BundleCompiler.StepKind.SERVER_COMMAND_PROFILE) {
            CommandProfileService.CommandProfile profile =
                    KernelServices.commandProfiles().find(step.targetId()).orElse(null);
            long capturedRevision = parseRevision(step.typedBindings().get("profile_revision"));
            CommandProfileService.Context required =
                    step.kind() == BundleCompiler.StepKind.SERVER_COMMAND_PROFILE
                            ? CommandProfileService.Context.SERVER
                            : CommandProfileService.Context.ACTOR;
            if (profile == null
                    || !profile.enabled()
                    || profile.context() != required
                    || profile.revision() != capturedRevision) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.CONFLICT,
                        "bundle command profile changed");
            }
        }
        return ActionResult.success(null);
    }

    private static void tickFakeScenes(MinecraftServer server) {
        for (FakeIdentityService.DueSceneEvent due :
                KernelServices.fakeIdentities().pollDueEvents(
                        Instant.now(),
                        MAXIMUM_FAKE_EVENTS_PER_TICK)) {
            ServerPlayer issuer = server.getPlayerList().getPlayer(due.actorId());
            if (issuer == null
                    || !KernelCommandExecutor.canUse(
                    issuer.createCommandSourceStack(),
                    "sef:fake.schedule")
                    || !KernelCommandExecutor.canUse(
                    issuer.createCommandSourceStack(),
                    "sef:fake.scene")) {
                auditScene(due, "failed", "issuer or permission unavailable");
                continue;
            }
            ActionResult<FakeIdentityService.ResolvedIdentity> identity =
                    KernelServices.fakeIdentities().resolve(
                            due.event().identity(),
                            issuer);
            if (!identity.successful()) {
                auditScene(due, "failed", "identity unavailable");
                continue;
            }
            ActionResult<Component> rendered = switch (due.event().type()) {
                case JOIN -> KernelServices.fakeIdentities().renderJoin(identity.value());
                case LEAVE -> KernelServices.fakeIdentities().renderLeave(identity.value());
                case MESSAGE -> KernelServices.fakeIdentities().renderChat(
                        identity.value(),
                        due.event().message());
            };
            if (!rendered.successful()) {
                auditScene(due, "failed", rendered.detail());
                continue;
            }
            int delivered = KernelServices.fakeIdentities().broadcast(
                    server,
                    identity.value(),
                    rendered.value(),
                    due.scene().audience(),
                    issuer);
            auditScene(due, "success", "delivered " + delivered);
        }
    }

    private static void auditScene(
            FakeIdentityService.DueSceneEvent due,
            String result,
            String detail
    ) {
        SecurityAuditService.record(SecurityAuditService.AuditEvent.create(
                "fake_scene",
                due.event().type().name().toLowerCase(Locale.ROOT),
                due.actorId().toString(),
                due.scheduleId().toString(),
                due.scene().id(),
                result,
                detail));
    }

    private static boolean eligible(ServerPlayer issuer, ServerPlayer target) {
        if (VanishUtil.isVanished(target, issuer)) {
            return false;
        }
        CommandSourceStack source = issuer.createCommandSourceStack();
        return PlayerTargetPolicy.decide(
                source,
                target,
                permission("sudo.hierarchy.bypass"),
                permission("sudo.exempt"),
                permission("sudo.bypass.exempt"),
                true,
                true).allowed();
    }

    private static boolean hasQualified(ServerPlayer player, String id) {
        PermissionNode<Boolean> staticNode = KernelServices.permissionNode(id);
        return staticNode == null
                ? DynamicPermissionService.has(player, id)
                : PermissionService.has(player, staticNode);
    }

    private static PermissionNode<Boolean> permission(String id) {
        return PermissionsHandler.phasePermission(id);
    }

    static boolean wrapperAction(String action) {
        return action.startsWith("sef:alias.")
                || action.startsWith("sef:bundle.")
                || action.startsWith("sef:profile.")
                || action.startsWith("sef:panel.")
                || action.equals("sef:fake.profile")
                || action.equals("sef:fake.scene")
                || action.equals("sef:fake.schedule")
                || action.startsWith("sef:sudo.")
                || action.startsWith("sef:run.")
                || action.startsWith("sef:silent.");
    }

    private static long parseRevision(String value) {
        try {
            return Long.parseLong(Objects.requireNonNullElse(value, "0"));
        } catch (NumberFormatException exception) {
            return 0L;
        }
    }

    private static String commandRoute(CommandDefinition action, Map<String, String> bindings) {
        String base = java.util.Arrays.stream(action.canonicalRoute().split(" "))
                .takeWhile(part -> !part.startsWith("<") && !part.startsWith("["))
                .collect(java.util.stream.Collectors.joining(" "));
        String arguments = Objects.requireNonNullElse(bindings.get("arguments"), "").trim();
        return arguments.isBlank() ? base : base + " " + arguments;
    }

    private static Map<String, String> profileArguments(Map<String, String> bindings) {
        Map<String, String> values = new LinkedHashMap<>(bindings);
        values.remove("profile_revision");
        return Map.copyOf(values);
    }

    private static final class RuntimeStepExecutor implements BundleService.StepExecutor {
        private final MinecraftServer server;

        private RuntimeStepExecutor(MinecraftServer server) {
            this.server = server;
        }

        @Override
        public ActionResult<Void> execute(
                BundleService.RuntimeJob job,
                BundleCompiler.BundleDefinition definition,
                BundleCompiler.BundleStep step,
                UUID targetId
        ) {
            ServerPlayer issuer = server.getPlayerList().getPlayer(job.issuerId());
            ServerPlayer target = server.getPlayerList().getPlayer(targetId);
            if (issuer == null || target == null) {
                return failure(job, step, targetId, ActionResult.ReasonCode.NOT_FOUND, "actor or target left");
            }
            ActionResult<Void> result = switch (step.kind()) {
                case SEF_ACTION -> executeAction(issuer, step);
                case EXTERNAL_ACTOR_COMMAND -> executeProfile(
                        issuer,
                        issuer.createCommandSourceStack(),
                        step,
                        CommandProfileService.Context.ACTOR);
                case SERVER_COMMAND_PROFILE -> executeProfile(
                        issuer,
                        server.createCommandSourceStack(),
                        step,
                        CommandProfileService.Context.SERVER);
                case NOTICE -> {
                    String message = Objects.requireNonNullElse(
                            step.typedBindings().get("message"),
                            step.targetId());
                    target.sendSystemMessage(Component.literal(message));
                    yield ActionResult.success(null);
                }
                case CONDITION, CHECKPOINT -> ActionResult.success(null);
                case DELAY, BUNDLE -> ActionResult.success(null);
                case RAW_COMMAND -> ActionResult.failure(
                        ActionResult.ReasonCode.POLICY_DENIED,
                        "raw command bundle steps are forbidden");
            };
            audit(job, step, targetId, result);
            return result;
        }

        private ActionResult<Void> executeAction(
                ServerPlayer issuer,
                BundleCompiler.BundleStep step
        ) {
            CommandDefinition action = KernelServices.catalog().find(step.targetId()).orElse(null);
            if (action == null) {
                return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "action disappeared");
            }
            return executeParsed(
                    issuer.createCommandSourceStack(),
                    commandRoute(action, step.typedBindings()));
        }

        private ActionResult<Void> executeProfile(
                ServerPlayer issuer,
                CommandSourceStack executionSource,
                BundleCompiler.BundleStep step,
                CommandProfileService.Context context
        ) {
            long revision = parseRevision(step.typedBindings().get("profile_revision"));
            ActionResult<CommandProfileService.RenderedCommand> rendered =
                    KernelServices.commandProfiles().renderPublished(
                            step.targetId(),
                            revision,
                            context,
                            profileArguments(step.typedBindings()),
                            1);
            if (!rendered.successful()) {
                return ActionResult.failure(rendered.reason(), rendered.detail());
            }
            if (context == CommandProfileService.Context.SERVER
                    && !PermissionService.has(
                    issuer,
                    permission("commands.profile.server"))) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.PERMISSION_DENIED,
                        "server profile permission was lost");
            }
            return executeParsed(executionSource, rendered.value().command());
        }

        private ActionResult<Void> executeParsed(
                CommandSourceStack source,
                String command
        ) {
            ParseResults<CommandSourceStack> parse =
                    server.getCommands().getDispatcher().parse(command, source);
            if (!parse.getExceptions().isEmpty() || parse.getReader().canRead()) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.INVALID_INPUT,
                        "bundle nested command no longer parses");
            }
            try {
                int result = server.getCommands().getDispatcher().execute(parse);
                return result > 0
                        ? ActionResult.success(null)
                        : ActionResult.failure(ActionResult.ReasonCode.PROVIDER_ERROR, "nested command returned zero");
            } catch (Exception exception) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.PROVIDER_ERROR,
                        exception.getClass().getSimpleName());
            }
        }

        private ActionResult<Void> failure(
                BundleService.RuntimeJob job,
                BundleCompiler.BundleStep step,
                UUID targetId,
                ActionResult.ReasonCode reason,
                String detail
        ) {
            ActionResult<Void> result = ActionResult.failure(reason, detail);
            audit(job, step, targetId, result);
            return result;
        }

        private void audit(
                BundleService.RuntimeJob job,
                BundleCompiler.BundleStep step,
                UUID targetId,
                ActionResult<?> result
        ) {
            SecurityAuditService.record(SecurityAuditService.AuditEvent.create(
                    "bundle",
                    step.kind().name().toLowerCase(Locale.ROOT),
                    job.issuerId().toString(),
                    targetId.toString(),
                    job.correlationId().toString(),
                    result.successful() ? "success" : "failed",
                    result.successful() ? step.id() : result.detail()));
        }
    }
}
