package com.enviouse.sef.gui.protocol;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.gui.GuiWorkflowCompiler;
import com.enviouse.sef.gui.UniversalGuiCatalog;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.vanish.VanishUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestion;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public final class GuiWorkflowService {
    private static final Duration SESSION_LIFETIME = Duration.ofMinutes(10);
    private static final int MAXIMUM_SESSIONS = 100_000;
    private static final java.util.Set<String> OFFLINE_QUEUE_ACTIONS =
            java.util.Set.of("sef:item.give.others");
    private static final AtomicLong REVISIONS = new AtomicLong();
    private static final Map<UUID, WorkflowSession> SESSIONS = new LinkedHashMap<>();

    private GuiWorkflowService() {
    }

    public static void open(ServerPlayer player, String actionId, String returnPanel) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(actionId, "actionId");
        Objects.requireNonNull(returnPanel, "returnPanel");
        SefSessionManager.SessionView transport =
                SefSessionManager.instance().session(player).orElse(null);
        if (transport == null || !transport.supports(SefProtocol.Feature.GUI_WORKFLOW)) {
            return;
        }
        openAuthorized(player, transport, actionId, returnPanel);
    }

    public static boolean openBare(CommandSourceStack source, String actionId) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(actionId, "actionId");
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return false;
        }
        SefSessionManager.SessionView transport =
                SefSessionManager.instance().session(player).orElse(null);
        if (transport == null || !transport.supports(SefProtocol.Feature.GUI_WORKFLOW)) {
            return false;
        }
        UniversalGuiCatalog.ActionRoute route =
                KernelServices.universalGuiCatalog().action(actionId).orElse(null);
        CommandDefinition definition = KernelServices.catalog().find(actionId).orElse(null);
        if (route == null
                || definition == null
                || route.workflowMode() != UniversalGuiCatalog.WorkflowMode.TYPED_COMMAND
                || !KernelCommandExecutor.canUse(source, actionId)) {
            return false;
        }
        String moduleId = KernelServices.moduleConfigs().moduleForFeature(route.featureId());
        String guiMode = KernelServices.moduleConfigs().effectiveGuiMode(moduleId, actionId);
        if (guiMode.equals("off")
                || guiMode.equals("command_only")
                || !Boolean.parseBoolean(KernelServices.moduleConfigs().value(
                        moduleId,
                        "gui.bare_command_opens"))) {
            return false;
        }
        openAuthorized(player, transport, actionId, "dashboard");
        return true;
    }

    public static void handleOpen(ServerPlayer player, GuiWorkflowPayloads.GuiWorkflowOpen request) {
        if (!accepted(player, request.sessionId(), request.sequence())) {
            return;
        }
        SefSessionManager.SessionView transport =
                SefSessionManager.instance().session(player).orElse(null);
        if (transport != null) {
            openAuthorized(player, transport, request.actionId(), "dashboard");
        }
    }

    public static void handleFieldUpdate(
            ServerPlayer player,
            GuiWorkflowPayloads.GuiWorkflowFieldUpdate request
    ) {
        if (!accepted(player, request.sessionId(), request.sequence())) {
            return;
        }
        WorkflowSession workflow = current(player, request.workflowId(), request.expectedRevision());
        if (workflow == null) {
            return;
        }
        try {
            GuiWorkflowCompiler.Variant variant = workflow.definition.requireVariant(request.variantId());
            GuiWorkflowCompiler.Field field = requireField(variant, request.fieldId());
            validateField(field, request.value());
            workflow.select(variant.id());
            workflow.values(variant.id()).put(field.id(), request.value());
            workflow.invalidatePreview("Field values changed. Preview the action again.");
            publish(player, workflow);
        } catch (IllegalArgumentException exception) {
            workflow.fail(exception.getMessage());
            publish(player, workflow);
        }
    }

    public static void handleSuggestions(
            ServerPlayer player,
            GuiWorkflowPayloads.GuiWorkflowSuggestionsRequest request
    ) {
        if (!accepted(player, request.sessionId(), request.sequence())) {
            return;
        }
        WorkflowSession workflow = current(player, request.workflowId(), request.expectedRevision());
        if (workflow == null) {
            return;
        }
        final GuiWorkflowCompiler.Variant variant;
        final GuiWorkflowCompiler.Field field;
        final String partial;
        try {
            variant = workflow.definition.requireVariant(request.variantId());
            field = requireField(variant, request.fieldId());
            partial = suggestionCommand(workflow, variant, field.id(), request.value());
        } catch (IllegalArgumentException exception) {
            sendSuggestions(player, workflow, request.requestId(), request.fieldId(), List.of());
            return;
        }

        if (!field.choices().isEmpty()) {
            List<String> filtered = field.choices().stream()
                    .filter(choice -> choice.startsWith(request.value().toLowerCase(Locale.ROOT)))
                    .toList();
            sendSuggestions(player, workflow, request.requestId(), field.id(), filtered);
            return;
        }
        if (field.type() == GuiWorkflowCompiler.FieldType.PLAYER
                || field.type() == GuiWorkflowCompiler.FieldType.PLAYERS) {
            sendSuggestionEntries(
                    player,
                    workflow,
                    request.requestId(),
                    field.id(),
                    playerSuggestions(player, request.value()));
            return;
        }

        CommandDispatcher<CommandSourceStack> dispatcher =
                player.server.getCommands().getDispatcher();
        ParseResults<CommandSourceStack> parse =
                dispatcher.parse(partial, player.createCommandSourceStack());
        dispatcher.getCompletionSuggestions(parse, partial.length()).whenComplete((suggestions, failure) ->
                player.server.execute(() -> {
                    WorkflowSession latest = current(player, workflow.id, workflow.revision);
                    if (latest == null) {
                        return;
                    }
                    if (failure != null) {
                        ServerEssentialsForge.LOGGER.debug(
                                "[SEF] GUI workflow suggestions failed for {}",
                                workflow.definition.actionId(),
                                failure);
                        sendSuggestions(player, workflow, request.requestId(), field.id(), List.of());
                        return;
                    }
                    List<String> values = suggestions.getList().stream()
                            .map(Suggestion::getText)
                            .filter(value -> value.length() <= 128)
                            .distinct()
                            .limit(GuiWorkflowPayloads.MAXIMUM_SUGGESTIONS)
                            .toList();
                    sendSuggestions(player, workflow, request.requestId(), field.id(), values);
                }));
    }

    public static void handlePreview(
            ServerPlayer player,
            GuiWorkflowPayloads.GuiWorkflowPreview request
    ) {
        if (!accepted(player, request.sessionId(), request.sequence())) {
            return;
        }
        WorkflowSession workflow = current(player, request.workflowId(), request.expectedRevision());
        if (workflow == null) {
            return;
        }
        prepare(player, workflow, request.variantId(), request.fields());
        publish(player, workflow);
    }

    public static void handleSubmit(
            ServerPlayer player,
            GuiWorkflowPayloads.GuiWorkflowSubmit request
    ) {
        if (!accepted(player, request.sessionId(), request.sequence())) {
            return;
        }
        WorkflowSession workflow = current(player, request.workflowId(), request.expectedRevision());
        if (workflow == null) {
            return;
        }
        if (!prepare(player, workflow, request.variantId(), request.fields())) {
            publish(player, workflow);
            return;
        }
        if (workflow.definition.requiresConfirmation()) {
            workflow.status = "Review the exact route preview, then confirm this action.";
            publish(player, workflow);
            return;
        }
        execute(player, workflow);
    }

    public static void handleConfirmation(
            ServerPlayer player,
            GuiWorkflowPayloads.GuiWorkflowConfirmation request
    ) {
        if (!accepted(player, request.sessionId(), request.sequence())) {
            return;
        }
        WorkflowSession workflow = current(player, request.workflowId(), request.expectedRevision());
        if (workflow == null) {
            return;
        }
        if (workflow.confirmationToken == null
                || !constantTimeEquals(workflow.confirmationToken.toString(), request.confirmationToken())
                || workflow.previewCommand.isBlank()) {
            workflow.fail("The confirmation expired or no longer matches this preview.");
            publish(player, workflow);
            return;
        }
        execute(player, workflow);
    }

    public static void handleClose(ServerPlayer player, GuiWorkflowPayloads.GuiWorkflowClose request) {
        if (!accepted(player, request.sessionId(), request.sequence())) {
            return;
        }
        synchronized (SESSIONS) {
            WorkflowSession current = SESSIONS.get(player.getUUID());
            if (current != null
                    && current.id.equals(request.workflowId())
                    && current.revision == request.expectedRevision()) {
                SESSIONS.remove(player.getUUID());
            }
        }
    }

    public static void invalidate(ServerPlayer player, String reason) {
        WorkflowSession removed;
        synchronized (SESSIONS) {
            removed = SESSIONS.remove(player.getUUID());
        }
        if (removed == null) {
            return;
        }
        SefSessionManager.instance().session(player).ifPresent(transport ->
                PacketDistributor.sendToPlayer(player, new GuiWorkflowPayloads.GuiWorkflowInvalidate(
                        transport.sessionId(),
                        removed.id,
                        removed.nextRevision(),
                        bounded(reason, 1024))));
    }

    public static void clear() {
        synchronized (SESSIONS) {
            SESSIONS.clear();
        }
    }

    public static int openSessionCount() {
        synchronized (SESSIONS) {
            expire();
            return SESSIONS.size();
        }
    }

    private static void openAuthorized(
            ServerPlayer player,
            SefSessionManager.SessionView transport,
            String actionId,
            String returnPanel
    ) {
        UniversalGuiCatalog.ActionRoute route =
                KernelServices.universalGuiCatalog().action(actionId).orElse(null);
        CommandDefinition definition = KernelServices.catalog().find(actionId).orElse(null);
        if (route == null || definition == null
                || route.workflowMode() != UniversalGuiCatalog.WorkflowMode.TYPED_COMMAND
                || !KernelCommandExecutor.canUse(player.createCommandSourceStack(), actionId)) {
            invalidateUnknown(player, transport, "This workflow is unavailable or access was revoked.");
            return;
        }
        String moduleId = KernelServices.moduleConfigs().moduleForFeature(route.featureId());
        String guiMode = KernelServices.moduleConfigs().effectiveGuiMode(moduleId, actionId);
        if (guiMode.equals("off") || guiMode.equals("command_only")) {
            invalidateUnknown(
                    player,
                    transport,
                    guiMode.equals("command_only")
                            ? "This action is explicitly configured for command use only."
                            : "This action is disabled.");
            return;
        }

        final GuiWorkflowCompiler.WorkflowDefinition workflowDefinition;
        try {
            workflowDefinition = GuiWorkflowCompiler.compile(
                    definition,
                    player.server.getCommands().getDispatcher(),
                    player.createCommandSourceStack());
        } catch (IllegalArgumentException exception) {
            ServerEssentialsForge.LOGGER.error(
                    "[SEF] GUI workflow compilation failed for {}",
                    actionId,
                    exception);
            invalidateUnknown(player, transport, "This action has no valid typed workflow.");
            return;
        }

        WorkflowSession workflow = new WorkflowSession(
                UUID.randomUUID(),
                transport.sessionId(),
                workflowDefinition,
                bounded(returnPanel, 64),
                nextGlobalRevision(),
                Instant.now().plus(SESSION_LIFETIME));
        synchronized (SESSIONS) {
            expire();
            if (!SESSIONS.containsKey(player.getUUID()) && SESSIONS.size() >= MAXIMUM_SESSIONS) {
                invalidateUnknown(player, transport, "The workflow service is at capacity.");
                return;
            }
            SESSIONS.put(player.getUUID(), workflow);
        }
        publish(player, workflow);
    }

    private static boolean prepare(
            ServerPlayer player,
            WorkflowSession workflow,
            String variantId,
            List<GuiWorkflowPayloads.WorkflowFieldValue> submitted
    ) {
        try {
            GuiWorkflowCompiler.Variant variant = workflow.definition.requireVariant(variantId);
            Map<String, String> values = validatedValues(variant, submitted);
            String command = render(variant, values);
            DeferredTarget deferred = deferredTarget(
                    player,
                    workflow.definition,
                    variant,
                    values);
            String validationCommand = command;
            if (deferred != null) {
                Map<String, String> validationValues = new LinkedHashMap<>(values);
                validationValues.put(
                        deferred.fieldId(),
                        player.getGameProfile().getName());
                validationCommand = render(variant, validationValues);
            }
            validateCommand(player, workflow, validationCommand);
            workflow.selectedVariant = variant.id();
            workflow.values.put(variant.id(), new LinkedHashMap<>(values));
            workflow.previewCommand = command;
            workflow.previewDisplay = redact(variant, values);
            workflow.deferredTargetId = deferred == null ? null : deferred.targetId();
            workflow.deferredTargetFieldId = deferred == null ? "" : deferred.fieldId();
            workflow.confirmationToken = workflow.definition.requiresConfirmation()
                    ? UUID.randomUUID()
                    : null;
            workflow.status = deferred != null
                    ? workflow.definition.requiresConfirmation()
                    ? "Target is offline. Confirm to queue this typed action."
                    : "Target is offline. Run to queue this typed action."
                    : workflow.definition.requiresConfirmation()
                    ? "Preview validated. A second confirmation is required."
                    : "Preview validated. The action is ready.";
            workflow.bump();
            return true;
        } catch (IllegalArgumentException exception) {
            workflow.fail(exception.getMessage());
            return false;
        }
    }

    private static void execute(ServerPlayer player, WorkflowSession workflow) {
        if (!stillAuthorized(player, workflow)) {
            workflow.fail("Permission, module state, or route availability changed.");
            publish(player, workflow);
            return;
        }
        if (workflow.deferredTargetId != null
                && player.server.getPlayerList().getPlayer(workflow.deferredTargetId) == null) {
            queueOffline(player, workflow);
            return;
        }
        if (workflow.deferredTargetId != null) {
            ServerPlayer target =
                    player.server.getPlayerList().getPlayer(workflow.deferredTargetId);
            GuiWorkflowCompiler.Variant variant =
                    workflow.definition.requireVariant(workflow.selectedVariant);
            Map<String, String> currentValues =
                    new LinkedHashMap<>(workflow.values(workflow.selectedVariant));
            currentValues.put(
                    workflow.deferredTargetFieldId,
                    target.getGameProfile().getName());
            workflow.previewCommand = render(variant, currentValues);
        }
        try {
            validateCommand(player, workflow, workflow.previewCommand);
        } catch (IllegalArgumentException exception) {
            workflow.fail(exception.getMessage());
            publish(player, workflow);
            return;
        }
        workflow.confirmationToken = null;
        workflow.bump();
        PacketDistributor.sendToPlayer(player, new GuiWorkflowPayloads.GuiWorkflowProgress(
                workflow.transportSessionId,
                workflow.id,
                workflow.revision,
                25,
                "The server is rechecking policy and executing the canonical action."));
        int result;
        try {
            result = player.server.getCommands().getDispatcher().execute(
                    workflow.previewCommand,
                    player.createCommandSourceStack());
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException | RuntimeException exception) {
            workflow.fail("The command rejected the workflow, " + safeMessage(exception));
            PacketDistributor.sendToPlayer(player, new GuiWorkflowPayloads.GuiWorkflowResult(
                    workflow.transportSessionId,
                    workflow.id,
                    workflow.revision,
                    false,
                    false,
                    workflow.status,
                    workflow.returnPanel));
            publish(player, workflow);
            return;
        }
        boolean successful = result > 0;
        String status = successful
                ? "The canonical action completed."
                : "The canonical action completed without changing state.";
        PacketDistributor.sendToPlayer(player, new GuiWorkflowPayloads.GuiWorkflowProgress(
                workflow.transportSessionId,
                workflow.id,
                workflow.revision,
                100,
                status));
        PacketDistributor.sendToPlayer(player, new GuiWorkflowPayloads.GuiWorkflowResult(
                workflow.transportSessionId,
                workflow.id,
                workflow.revision,
                successful,
                true,
                status,
                workflow.returnPanel));
        synchronized (SESSIONS) {
            WorkflowSession current = SESSIONS.get(player.getUUID());
            if (current == workflow) {
                SESSIONS.remove(player.getUUID());
            }
        }
    }

    private static void queueOffline(ServerPlayer player, WorkflowSession workflow) {
        try {
            OfflineActionRepository.QueuedAction queued = KernelServices.offlineActions().enqueue(
                    player.getUUID(),
                    workflow.deferredTargetId,
                    workflow.definition.actionId(),
                    workflow.selectedVariant,
                    workflow.deferredTargetFieldId,
                    workflow.values(workflow.selectedVariant),
                    Instant.now(),
                    Duration.ofDays(7L));
            workflow.confirmationToken = null;
            workflow.bump();
            String status = "Queued action " + queued.id()
                    + ". It will recheck access when the player is online.";
            PacketDistributor.sendToPlayer(player, new GuiWorkflowPayloads.GuiWorkflowProgress(
                    workflow.transportSessionId,
                    workflow.id,
                    workflow.revision,
                    100,
                    status));
            PacketDistributor.sendToPlayer(player, new GuiWorkflowPayloads.GuiWorkflowResult(
                    workflow.transportSessionId,
                    workflow.id,
                    workflow.revision,
                    true,
                    true,
                    status,
                    workflow.returnPanel));
            synchronized (SESSIONS) {
                if (SESSIONS.get(player.getUUID()) == workflow) {
                    SESSIONS.remove(player.getUUID());
                }
            }
        } catch (IllegalArgumentException | IllegalStateException exception) {
            workflow.fail("The offline action could not be queued, " + safeMessage(exception));
            publish(player, workflow);
        }
    }

    private static void validateCommand(
            ServerPlayer player,
            WorkflowSession workflow,
            String command
    ) {
        if (command.isBlank() || command.length() > 8192
                || !command.equals(command.strip())
                || command.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("The rendered command is outside hard bounds.");
        }
        if (!command.equals(workflow.definition.canonicalRoute())
                && !command.startsWith(workflow.definition.canonicalRoute() + " ")) {
            throw new IllegalArgumentException("The workflow escaped its canonical route.");
        }
        CommandDispatcher<CommandSourceStack> dispatcher =
                player.server.getCommands().getDispatcher();
        ParseResults<CommandSourceStack> parsed =
                dispatcher.parse(command, player.createCommandSourceStack());
        if (parsed.getReader().canRead()
                || parsed.getContext().getCommand() == null
                || !parsed.getExceptions().isEmpty()) {
            throw new IllegalArgumentException("The typed values do not form a valid command.");
        }
    }

    private static Map<String, String> validatedValues(
            GuiWorkflowCompiler.Variant variant,
            List<GuiWorkflowPayloads.WorkflowFieldValue> submitted
    ) {
        if (submitted.size() != variant.fields().size()) {
            throw new IllegalArgumentException("Every field in the selected workflow variant is required.");
        }
        Map<String, GuiWorkflowCompiler.Field> expected = new LinkedHashMap<>();
        variant.fields().forEach(field -> expected.put(field.id(), field));
        Map<String, String> values = new LinkedHashMap<>();
        for (GuiWorkflowPayloads.WorkflowFieldValue value : submitted) {
            GuiWorkflowCompiler.Field field = expected.get(value.id());
            if (field == null || values.putIfAbsent(field.id(), value.value()) != null) {
                throw new IllegalArgumentException("The workflow contains an unknown or duplicate field.");
            }
            validateField(field, value.value());
        }
        if (!values.keySet().equals(expected.keySet())) {
            throw new IllegalArgumentException("The workflow field set is incomplete.");
        }
        return values;
    }

    static void validateField(GuiWorkflowCompiler.Field field, String value) {
        Objects.requireNonNull(value, "value");
        if (value.isBlank() || value.length() > field.maximumLength()
                || value.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(field.label() + " is required and must remain within bounds.");
        }
        switch (field.type()) {
            case BOOLEAN -> {
                if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                    throw new IllegalArgumentException(field.label() + " must be true or false.");
                }
            }
            case INTEGER -> {
                try {
                    long number = Long.parseLong(value);
                    if (number < field.minimum() || number > field.maximum()) {
                        throw new IllegalArgumentException(field.label() + " is outside its allowed range.");
                    }
                } catch (NumberFormatException exception) {
                    throw new IllegalArgumentException(field.label() + " must be an integer.");
                }
            }
            case DECIMAL -> {
                try {
                    double number = Double.parseDouble(value);
                    if (!Double.isFinite(number)
                            || number < field.minimum()
                            || number > field.maximum()) {
                        throw new IllegalArgumentException(field.label() + " is outside its allowed range.");
                    }
                } catch (NumberFormatException exception) {
                    throw new IllegalArgumentException(field.label() + " must be a decimal number.");
                }
            }
            default -> {
            }
        }
        if (field.renderMode() == GuiWorkflowCompiler.RenderMode.WORD
                && value.codePoints().anyMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException(field.label() + " must be one word.");
        }
    }

    static String render(
            GuiWorkflowCompiler.Variant variant,
            Map<String, String> values
    ) {
        StringBuilder command = new StringBuilder();
        Map<String, GuiWorkflowCompiler.Field> fields = new LinkedHashMap<>();
        variant.fields().forEach(field -> fields.put(field.id(), field));
        for (GuiWorkflowCompiler.Segment segment : variant.segments()) {
            if (!command.isEmpty()) {
                command.append(' ');
            }
            if (segment.literal()) {
                command.append(segment.value());
                continue;
            }
            GuiWorkflowCompiler.Field field = fields.get(segment.value());
            String value = values.get(segment.value());
            command.append(switch (field.renderMode()) {
                case QUOTED -> StringArgumentType.escapeIfRequired(value);
                case RAW, WORD, GREEDY -> value;
            });
        }
        return command.toString();
    }

    private static DeferredTarget deferredTarget(
            ServerPlayer player,
            GuiWorkflowCompiler.WorkflowDefinition workflow,
            GuiWorkflowCompiler.Variant variant,
            Map<String, String> values
    ) {
        if (!supportsOfflineQueue(workflow.actionId())) {
            return null;
        }
        CommandDefinition definition =
                KernelServices.catalog().find(workflow.actionId()).orElse(null);
        if (definition == null) {
            return null;
        }
        if (definition.targetBehavior() == CommandDefinition.TargetBehavior.NONE
                || definition.targetBehavior() == CommandDefinition.TargetBehavior.SELF
                || definition.targetBehavior() == CommandDefinition.TargetBehavior.SERVER) {
            return null;
        }
        List<GuiWorkflowCompiler.Field> playerFields = variant.fields().stream()
                .filter(field -> field.type() == GuiWorkflowCompiler.FieldType.PLAYER
                        || field.type() == GuiWorkflowCompiler.FieldType.PLAYERS)
                .toList();
        if (playerFields.size() != 1) {
            return null;
        }
        GuiWorkflowCompiler.Field field = playerFields.getFirst();
        UUID targetId = KernelServices.profiles()
                .resolve(values.get(field.id()), true)
                .orElse(null);
        if (targetId == null
                || player.server.getPlayerList().getPlayer(targetId) != null) {
            return null;
        }
        return new DeferredTarget(targetId, field.id());
    }

    static boolean supportsOfflineQueue(String actionId) {
        return OFFLINE_QUEUE_ACTIONS.contains(actionId);
    }

    private static String redact(
            GuiWorkflowCompiler.Variant variant,
            Map<String, String> values
    ) {
        Map<String, String> display = new LinkedHashMap<>();
        for (GuiWorkflowCompiler.Field field : variant.fields()) {
            String normalized = field.id().toLowerCase(Locale.ROOT);
            display.put(field.id(), normalized.contains("password")
                    || normalized.contains("secret")
                    || normalized.contains("token")
                    || normalized.contains("credential")
                    ? "<redacted>"
                    : values.get(field.id()));
        }
        return render(variant, display);
    }

    private static String suggestionCommand(
            WorkflowSession workflow,
            GuiWorkflowCompiler.Variant variant,
            String fieldId,
            String partialValue
    ) {
        Map<String, GuiWorkflowCompiler.Field> fields = new LinkedHashMap<>();
        variant.fields().forEach(field -> fields.put(field.id(), field));
        Map<String, String> values = workflow.values(variant.id());
        StringBuilder command = new StringBuilder();
        boolean found = false;
        for (GuiWorkflowCompiler.Segment segment : variant.segments()) {
            if (!command.isEmpty()) {
                command.append(' ');
            }
            if (segment.literal()) {
                command.append(segment.value());
            } else if (segment.value().equals(fieldId)) {
                command.append(partialValue);
                found = true;
                break;
            } else {
                String value = values.get(segment.value());
                if (value == null || value.isBlank()) {
                    throw new IllegalArgumentException("Complete earlier fields before requesting suggestions.");
                }
                GuiWorkflowCompiler.Field field = fields.get(segment.value());
                command.append(field.renderMode() == GuiWorkflowCompiler.RenderMode.QUOTED
                        ? StringArgumentType.escapeIfRequired(value)
                        : value);
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Unknown workflow field");
        }
        return command.toString();
    }

    private static void publish(ServerPlayer player, WorkflowSession workflow) {
        if (!stillAuthorized(player, workflow)) {
            invalidate(player, "Permission or module state changed while this workflow was open.");
            return;
        }
        List<GuiWorkflowPayloads.WorkflowVariant> variants = workflow.definition.variants().stream()
                .map(variant -> new GuiWorkflowPayloads.WorkflowVariant(
                        variant.id(),
                        variant.label(),
                        variant.fields().stream()
                                .map(field -> new GuiWorkflowPayloads.WorkflowField(
                                        field.id(),
                                        field.label(),
                                        field.type().name().toLowerCase(Locale.ROOT),
                                        field.renderMode().name().toLowerCase(Locale.ROOT),
                                        true,
                                        finiteMinimum(field.minimum()),
                                        finiteMaximum(field.maximum()),
                                        field.maximumLength(),
                                        workflow.values(variant.id()).getOrDefault(field.id(), ""),
                                        field.choices(),
                                        field.suggestionKind()))
                                .toList()))
                .toList();
        PacketDistributor.sendToPlayer(player, new GuiWorkflowPayloads.GuiWorkflowSnapshot(
                workflow.transportSessionId,
                workflow.id,
                workflow.revision,
                workflow.definition.actionId(),
                workflow.returnPanel,
                title(workflow.definition.actionId()),
                workflow.status,
                workflow.previewDisplay,
                variants,
                workflow.selectedVariant,
                workflow.definition.destructive(),
                !workflow.previewCommand.isBlank(),
                workflow.confirmationToken != null,
                workflow.confirmationToken == null ? "" : workflow.confirmationToken.toString()));
    }

    private static void sendSuggestions(
            ServerPlayer player,
            WorkflowSession workflow,
            UUID requestId,
            String fieldId,
            List<String> suggestions
    ) {
        PacketDistributor.sendToPlayer(player, new GuiWorkflowPayloads.GuiWorkflowSuggestions(
                workflow.transportSessionId,
                workflow.id,
                workflow.revision,
                requestId,
                fieldId,
                suggestions.stream()
                        .sorted()
                        .map(value -> new GuiWorkflowPayloads.WorkflowSuggestion(
                                value,
                                value,
                                true))
                        .limit(GuiWorkflowPayloads.MAXIMUM_SUGGESTIONS)
                        .toList()));
    }

    private static void sendSuggestionEntries(
            ServerPlayer player,
            WorkflowSession workflow,
            UUID requestId,
            String fieldId,
            List<GuiWorkflowPayloads.WorkflowSuggestion> suggestions
    ) {
        PacketDistributor.sendToPlayer(player, new GuiWorkflowPayloads.GuiWorkflowSuggestions(
                workflow.transportSessionId,
                workflow.id,
                workflow.revision,
                requestId,
                fieldId,
                suggestions.stream()
                        .sorted(Comparator
                                .comparing(GuiWorkflowPayloads.WorkflowSuggestion::online)
                                .reversed()
                                .thenComparing(
                                        suggestion -> suggestion.label().toLowerCase(Locale.ROOT)))
                        .limit(GuiWorkflowPayloads.MAXIMUM_SUGGESTIONS)
                        .toList()));
    }

    private static List<GuiWorkflowPayloads.WorkflowSuggestion> playerSuggestions(
            ServerPlayer viewer,
            String query
    ) {
        String normalized = Objects.requireNonNullElse(query, "").trim().toLowerCase(Locale.ROOT);
        Map<UUID, GuiWorkflowPayloads.WorkflowSuggestion> choices = new LinkedHashMap<>();
        KernelServices.profiles().snapshot().forEach(profile -> {
            String username = Objects.requireNonNullElse(profile.authenticatedUsername(), "");
            String nickname = Objects.requireNonNullElse(profile.nickname(), "");
            if (!username.isBlank()
                    && (normalized.isBlank()
                    || username.toLowerCase(Locale.ROOT).contains(normalized)
                    || nickname.toLowerCase(Locale.ROOT).contains(normalized))) {
                String label = nickname.isBlank() ? username : username + ", " + nickname;
                choices.put(profile.playerId(), new GuiWorkflowPayloads.WorkflowSuggestion(
                        username,
                        label,
                        false));
            }
        });
        viewer.server.getPlayerList().getPlayers().forEach(target -> {
            if (VanishUtil.isVanished(target, viewer)) {
                choices.remove(target.getUUID());
                return;
            }
            String username = target.getGameProfile().getName();
            String nickname = KernelServices.profiles()
                    .find(target.getUUID())
                    .map(profile -> Objects.requireNonNullElse(profile.nickname(), ""))
                    .orElse("");
            if (normalized.isBlank()
                    || username.toLowerCase(Locale.ROOT).contains(normalized)
                    || nickname.toLowerCase(Locale.ROOT).contains(normalized)) {
                String label = nickname.isBlank() ? username : username + ", " + nickname;
                choices.put(target.getUUID(), new GuiWorkflowPayloads.WorkflowSuggestion(
                        username,
                        label,
                        true));
            }
        });
        return List.copyOf(choices.values());
    }

    private static WorkflowSession current(
            ServerPlayer player,
            UUID workflowId,
            long expectedRevision
    ) {
        WorkflowSession workflow;
        synchronized (SESSIONS) {
            expire();
            workflow = SESSIONS.get(player.getUUID());
        }
        if (workflow == null || !workflow.id.equals(workflowId)) {
            invalidateUnknown(
                    player,
                    SefSessionManager.instance().session(player).orElse(null),
                    "This workflow expired.");
            return null;
        }
        if (workflow.revision != expectedRevision) {
            workflow.fail("The workflow changed. Review the refreshed values.");
            publish(player, workflow);
            return null;
        }
        if (!stillAuthorized(player, workflow)) {
            invalidate(player, "Permission or module state changed while this workflow was open.");
            return null;
        }
        return workflow;
    }

    private static boolean stillAuthorized(ServerPlayer player, WorkflowSession workflow) {
        UniversalGuiCatalog.ActionRoute route =
                KernelServices.universalGuiCatalog().action(workflow.definition.actionId()).orElse(null);
        if (route == null || !KernelCommandExecutor.canUse(
                player.createCommandSourceStack(),
                workflow.definition.actionId())) {
            return false;
        }
        String mode = KernelServices.moduleConfigs().effectiveGuiMode(
                KernelServices.moduleConfigs().moduleForFeature(route.featureId()),
                route.actionId());
        return !mode.equals("off") && !mode.equals("command_only");
    }

    private static boolean accepted(ServerPlayer player, UUID sessionId, long sequence) {
        return SefSessionManager.instance().acceptRequest(
                player,
                sessionId,
                sequence,
                SefProtocol.Feature.GUI_WORKFLOW) == SefSessionManager.RequestDecision.ACCEPTED;
    }

    private static GuiWorkflowCompiler.Field requireField(
            GuiWorkflowCompiler.Variant variant,
            String fieldId
    ) {
        return variant.fields().stream()
                .filter(field -> field.id().equals(fieldId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown workflow field"));
    }

    private static void invalidateUnknown(
            ServerPlayer player,
            SefSessionManager.SessionView transport,
            String reason
    ) {
        if (transport == null || !transport.supports(SefProtocol.Feature.GUI_WORKFLOW)) {
            return;
        }
        PacketDistributor.sendToPlayer(player, new GuiWorkflowPayloads.GuiWorkflowInvalidate(
                transport.sessionId(),
                UUID.randomUUID(),
                nextGlobalRevision(),
                bounded(reason, 1024)));
    }

    private static void expire() {
        Instant now = Instant.now();
        SESSIONS.entrySet().removeIf(entry -> !entry.getValue().expiresAt.isAfter(now));
    }

    private static long nextGlobalRevision() {
        return REVISIONS.updateAndGet(current -> current == Long.MAX_VALUE ? 1L : current + 1L);
    }

    private static String title(String actionId) {
        String normalized = actionId.startsWith("sef:") ? actionId.substring(4) : actionId;
        String title = normalized.replace('.', ' ').replace('_', ' ').replace('/', ' ');
        if (title.isBlank()) {
            return "Server action";
        }
        return Character.toUpperCase(title.charAt(0)) + title.substring(1);
    }

    private static double finiteMinimum(double value) {
        return Double.isFinite(value) ? value : -Double.MAX_VALUE;
    }

    private static double finiteMaximum(double value) {
        return Double.isFinite(value) ? value : Double.MAX_VALUE;
    }

    private static String bounded(String value, int maximum) {
        String normalized = Objects.requireNonNullElse(value, "").trim();
        if (normalized.length() > maximum) {
            normalized = normalized.substring(0, maximum);
        }
        return normalized.codePoints().anyMatch(Character::isISOControl)
                ? "Workflow state changed."
                : normalized;
    }

    private static String safeMessage(Exception exception) {
        String message = Objects.requireNonNullElse(exception.getMessage(), exception.getClass().getSimpleName());
        return bounded(message, 256);
    }

    private static boolean constantTimeEquals(String left, String right) {
        byte[] leftBytes = left.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] rightBytes = right.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return java.security.MessageDigest.isEqual(leftBytes, rightBytes);
    }

    private static final class WorkflowSession {
        private final UUID id;
        private final UUID transportSessionId;
        private final GuiWorkflowCompiler.WorkflowDefinition definition;
        private final String returnPanel;
        private final Map<String, Map<String, String>> values = new LinkedHashMap<>();
        private final Instant expiresAt;
        private long revision;
        private String selectedVariant;
        private String status = "Complete the typed fields, then preview the canonical action.";
        private String previewCommand = "";
        private String previewDisplay = "";
        private UUID confirmationToken;
        private UUID deferredTargetId;
        private String deferredTargetFieldId = "";

        private WorkflowSession(
                UUID id,
                UUID transportSessionId,
                GuiWorkflowCompiler.WorkflowDefinition definition,
                String returnPanel,
                long revision,
                Instant expiresAt
        ) {
            this.id = id;
            this.transportSessionId = transportSessionId;
            this.definition = definition;
            this.returnPanel = returnPanel;
            this.revision = revision;
            this.expiresAt = expiresAt;
            this.selectedVariant = definition.variants().getFirst().id();
            definition.variants().forEach(variant ->
                    values.put(variant.id(), new LinkedHashMap<>()));
        }

        private Map<String, String> values(String variantId) {
            Map<String, String> result = values.get(variantId);
            if (result == null) {
                throw new IllegalArgumentException("Unknown workflow variant");
            }
            return result;
        }

        private void select(String variantId) {
            definition.requireVariant(variantId);
            selectedVariant = variantId;
        }

        private void invalidatePreview(String replacementStatus) {
            previewCommand = "";
            previewDisplay = "";
            confirmationToken = null;
            deferredTargetId = null;
            deferredTargetFieldId = "";
            status = replacementStatus;
            bump();
        }

        private void fail(String replacementStatus) {
            invalidatePreview(bounded(replacementStatus, 1024));
        }

        private void bump() {
            revision = nextGlobalRevision();
        }

        private long nextRevision() {
            bump();
            return revision;
        }
    }

    private record DeferredTarget(UUID targetId, String fieldId) {
    }
}
