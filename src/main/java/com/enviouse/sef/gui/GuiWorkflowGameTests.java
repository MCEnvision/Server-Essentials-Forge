package com.enviouse.sef.gui;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.audit.SecurityAuditService;
import com.enviouse.sef.gui.protocol.OfflineActionRepository;
import com.enviouse.sef.gui.protocol.OfflineActionService;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.kernel.command.ShortcutRegistry;
import com.enviouse.sef.kernel.policy.FeatureGateService;
import com.mojang.brigadier.ParseResults;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.time.Duration;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@GameTestHolder("sef")
@PrefixGameTestTemplate(false)
public final class GuiWorkflowGameTests {
    private GuiWorkflowGameTests() {
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void everyCatalogActionResolvesToAnExecutableLiveRoute(GameTestHelper helper) {
        var dispatcher = helper.getLevel().getServer().getCommands().getDispatcher();
        List<String> failures = new ArrayList<>();
        for (var definition : KernelServices.catalog().entries()) {
            if (!definition.playerFacing()) {
                continue;
            }
            boolean enabled = KernelServices.featureGates().decide(
                    definition.featureId(),
                    FeatureGateService.Context.server(definition.id())).enabled();
            if (!enabled) {
                continue;
            }
            try {
                var workflow = GuiWorkflowCompiler.compileStructure(definition, dispatcher);
                if (workflow.variants().isEmpty()) {
                    failures.add(definition.id() + ", no executable variant");
                }
            } catch (IllegalArgumentException exception) {
                failures.add(definition.id() + ", " + exception.getMessage());
            }
        }
        failures.forEach(failure ->
                ServerEssentialsForge.LOGGER.error("[SEF] Command route coverage, {}", failure));
        helper.assertTrue(
                failures.isEmpty(),
                "command route coverage failed, " + String.join("; ", failures.stream().limit(8).toList()));
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void everyActiveShortcutAndSefRootHasCatalogOwnership(GameTestHelper helper) {
        var dispatcher = helper.getLevel().getServer().getCommands().getDispatcher();
        var catalog = KernelServices.catalog();
        var shortcuts = KernelServices.shortcuts();
        List<String> failures = new ArrayList<>();

        for (ShortcutRegistry.Diagnostic diagnostic : shortcuts.diagnostics()) {
            boolean active = diagnostic.status() == ShortcutRegistry.Status.ACTIVE
                    || diagnostic.status() == ShortcutRegistry.Status.ACTIVE_OVERRIDE;
            if (active && dispatcher.getRoot().getChild(diagnostic.root()) == null) {
                failures.add(diagnostic.root() + ", active shortcut is not registered");
            }
        }

        dispatcher.getRoot().getChildren().stream()
                .filter(node -> !shortcuts.existedBeforeRegistration(node.getName()))
                .filter(node -> catalog.rootOwner(node.getName()).isEmpty())
                .filter(node -> shortcuts.find(node.getName()).isEmpty())
                .forEach(node -> failures.add(node.getName() + ", new root has no catalog owner"));

        failures.forEach(failure ->
                ServerEssentialsForge.LOGGER.error("[SEF] Command ownership coverage, {}", failure));
        helper.assertTrue(
                failures.isEmpty(),
                "command ownership coverage failed, " + String.join("; ", failures.stream().limit(8).toList()));
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void everyEnabledConsoleActionCompilesForTheConsoleSource(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var dispatcher = server.getCommands().getDispatcher();
        var source = server.createCommandSourceStack();
        List<String> failures = new ArrayList<>();
        int covered = 0;
        for (var definition : KernelServices.catalog().entries()) {
            if (!definition.sourceTypes().contains(CommandDefinition.SourceType.CONSOLE)) {
                continue;
            }
            boolean enabled = KernelServices.featureGates().decide(
                    definition.featureId(),
                    FeatureGateService.Context.server(definition.id())).enabled();
            if (!enabled) {
                continue;
            }
            try {
                GuiWorkflowCompiler.compile(definition, dispatcher, source);
                covered++;
            } catch (IllegalArgumentException exception) {
                failures.add(definition.id() + ", " + exception.getMessage());
            }
        }
        failures.forEach(failure ->
                ServerEssentialsForge.LOGGER.error("[SEF] Console command coverage, {}", failure));
        helper.assertTrue(
                failures.isEmpty(),
                "console command coverage failed, " + String.join("; ", failures.stream().limit(8).toList()));
        helper.assertTrue(covered > 0, "console command coverage did not inspect any actions");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void everyReadOnlyArgumentFreeConsoleRouteExecutes(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var dispatcher = server.getCommands().getDispatcher();
        var source = server.createCommandSourceStack();
        List<String> failures = new ArrayList<>();
        Set<String> executed = new LinkedHashSet<>();

        for (var definition : KernelServices.catalog().entries()) {
            if (definition.auditClass() != AuditService.AuditClass.METADATA_ONLY
                    || !definition.sourceTypes().contains(CommandDefinition.SourceType.CONSOLE)) {
                continue;
            }
            boolean enabled = KernelServices.featureGates().decide(
                    definition.featureId(),
                    FeatureGateService.Context.server(definition.id())).enabled();
            if (!enabled) {
                continue;
            }
            GuiWorkflowCompiler.WorkflowDefinition workflow;
            try {
                workflow = GuiWorkflowCompiler.compile(definition, dispatcher, source);
            } catch (IllegalArgumentException exception) {
                continue;
            }
            for (GuiWorkflowCompiler.Variant variant : workflow.variants()) {
                if (!variant.fields().isEmpty()) {
                    continue;
                }
                String command = variant.segments().stream()
                        .map(GuiWorkflowCompiler.Segment::value)
                        .reduce((left, right) -> left + " " + right)
                        .orElse("");
                if (command.isBlank() || !executed.add(command)) {
                    continue;
                }
                try {
                    dispatcher.execute(command, source);
                } catch (Exception exception) {
                    failures.add(definition.id() + ", " + command + ", "
                            + exception.getClass().getSimpleName());
                }
            }
        }

        failures.forEach(failure ->
                ServerEssentialsForge.LOGGER.error("[SEF] Read only command execution, {}", failure));
        helper.assertTrue(
                failures.isEmpty(),
                "read only command execution failed, "
                        + String.join("; ", failures.stream().limit(8).toList()));
        helper.assertTrue(!executed.isEmpty(), "no read only console commands were executed");
        ServerEssentialsForge.LOGGER.info(
                "[SEF] Read only command execution covered {} unique routes",
                executed.size());
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void everyEnabledArgumentFreeConsoleRouteReachesTheSharedDispatcher(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var dispatcher = server.getCommands().getDispatcher();
        var source = server.createCommandSourceStack();
        List<String> failures = new ArrayList<>();
        Map<String, List<CommandDefinition>> candidates = new LinkedHashMap<>();

        for (var definition : KernelServices.catalog().entries()) {
            if (!definition.sourceTypes().contains(CommandDefinition.SourceType.CONSOLE)) {
                continue;
            }
            boolean enabled = KernelServices.featureGates().decide(
                    definition.featureId(),
                    FeatureGateService.Context.server(definition.id())).enabled();
            if (!enabled) {
                continue;
            }
            GuiWorkflowCompiler.WorkflowDefinition workflow;
            try {
                workflow = GuiWorkflowCompiler.compile(definition, dispatcher, source);
            } catch (IllegalArgumentException exception) {
                failures.add(definition.id() + ", workflow, " + exception.getMessage());
                continue;
            }
            for (GuiWorkflowCompiler.Variant variant : workflow.variants()) {
                if (!variant.fields().isEmpty()) {
                    continue;
                }
                String command = render(variant);
                if (command.isBlank()) {
                    continue;
                }
                candidates.computeIfAbsent(command, ignored -> new ArrayList<>()).add(definition);
            }
        }

        Map<String, Integer> results = new LinkedHashMap<>();
        Map<String, Integer> unjoinedByDisposition = new LinkedHashMap<>();
        List<String> unjoinedDetails = new ArrayList<>();
        List<String> expectedNonApplicableDetails = new ArrayList<>();
        JsonArray runtimeRows = new JsonArray();
        int unjoinedRows = 0;
        for (Map.Entry<String, List<CommandDefinition>> candidate : candidates.entrySet()) {
            String command = candidate.getKey();
            Set<String> actionIds = candidate.getValue().stream()
                    .filter(definition -> routeOwnedByDefinition(definition, command))
                    .map(CommandDefinition::id)
                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
            if (actionIds.isEmpty()) {
                continue;
            }
            Set<String> before = SecurityAuditService.recent(
                            event -> actionIds.contains(event.actionId()),
                            128)
                    .stream()
                    .map(SecurityAuditService.AuditEvent::eventId)
                    .collect(java.util.stream.Collectors.toSet());
            int result;
            try {
                result = dispatcher.execute(command, source);
                results.merge(result > 0 ? "positive" : "non_positive", 1, Integer::sum);
            } catch (Exception exception) {
                failures.add(String.join(", ", candidate.getValue().stream()
                        .map(CommandDefinition::id)
                        .toList()) + ", " + command + ", "
                        + exception.getClass().getSimpleName());
                continue;
            }

            Set<String> noAuditExpected = positiveRouteNeedsNoAudit(command)
                    ? actionIds
                    : Set.of();
            List<SecurityAuditService.AuditEvent> newEvents = SecurityAuditService.recent(
                            event -> actionIds.contains(event.actionId()) && !before.contains(event.eventId()),
                            128);
            for (CommandDefinition definition : candidate.getValue()) {
                if (!routeOwnedByDefinition(definition, command)) {
                    continue;
                }
                if (definition.auditClass() == AuditService.AuditClass.NONE
                        || noAuditExpected.contains(definition.id())) {
                    continue;
                }
                List<SecurityAuditService.AuditEvent> definitionEvents = newEvents.stream()
                        .filter(candidateEvent -> candidateEvent.actionId().equals(definition.id()))
                        .toList();
                var event = definitionEvents.stream()
                        .filter(candidateEvent -> candidateEvent.actionId().equals(definition.id())
                                && routeOwnedByDefinition(definition, command))
                        .findFirst()
                        .orElse(null);
                if (event == null) {
                    if (definition.targetBehavior() == CommandDefinition.TargetBehavior.REQUIRED_PLAYER) {
                        expectedNonApplicableDetails.add(
                                definition.id() + "|" + definition.canonicalRoute() + "|requires_player_argument");
                        continue;
                    }
                    unjoinedRows++;
                    String disposition = definitionEvents.isEmpty()
                            ? result <= 0 ? "non_positive_without_event" : "missing_event"
                            : "event_route_mismatch";
                    unjoinedByDisposition.merge(disposition, 1, Integer::sum);
                    unjoinedDetails.add(
                            definition.id() + "|" + definition.canonicalRoute() + "|" + disposition);
                    continue;
                }
                JsonObject runtimeRow = new JsonObject();
                runtimeRow.addProperty("actionId", definition.id());
                runtimeRow.addProperty("canonicalRoute", definition.canonicalRoute());
                runtimeRow.addProperty("commandDigest", digest(command));
                runtimeRow.addProperty("result", result > 0 ? "success" : "non_positive");
                runtimeRow.addProperty("auditEventCount", definitionEvents.size());
                runtimeRow.addProperty("sourceType", event.sourceType());
                runtimeRow.addProperty("auditResult", event.result());
                runtimeRow.addProperty("auditClass", event.auditClass());
                runtimeRow.addProperty("redactionClass", event.redactionClass());
                runtimeRows.add(runtimeRow);
                // Some adapters intentionally return the domain count, which may be
                // zero even though the shared lease completed successfully.
                boolean resultProjectionMatches = result <= 0 || "success".equals(event.result());
                if (!"console".equals(event.sourceType())
                        || event.actorUuid().isBlank()
                        || event.actorUsername().isBlank()
                        || event.serverSessionId().isBlank()
                        || event.eventId().isBlank()
                        || !resultProjectionMatches
                        || !definition.auditClass().name().toLowerCase(java.util.Locale.ROOT)
                                .equals(event.auditClass())
                        || event.normalizedParameters().values().stream()
                                .anyMatch(value -> value.contains(command))) {
                    failures.add(definition.id() + ", " + command + ", unsafe audit projection, result "
                            + result + ", audit result " + event.result() + ", reason " + event.reasonCode());
                }
            }
        }

        failures.forEach(failure ->
                ServerEssentialsForge.LOGGER.error("[SEF] Argument free console route, {}", failure));
        helper.assertTrue(
                failures.isEmpty(),
                "argument free console route execution failed, "
                        + String.join("; ", failures.stream().limit(8).toList()));
        helper.assertTrue(!candidates.isEmpty(), "no enabled argument free console routes were discovered");
        try {
            writeCatalogRuntimeEvidence(runtimeRows);
        } catch (Exception exception) {
            helper.fail("catalog runtime evidence could not be written, "
                    + exception.getClass().getSimpleName());
            return;
        }
        ServerEssentialsForge.LOGGER.info(
                "[SEF] Argument free console routes executed {}, result classes {}",
                candidates.size(),
                results);
        ServerEssentialsForge.LOGGER.info(
                "[SEF] Argument free console audit rows without an observed event {}, "
                        + "expected non applicable rows {}, dispositions {}, samples {}, expected samples {}",
                unjoinedRows,
                expectedNonApplicableDetails.size(),
                unjoinedByDisposition,
                unjoinedDetails.stream().limit(24).toList(),
                expectedNonApplicableDetails.stream().limit(24).toList());
        helper.succeed();
    }

    private static void writeCatalogRuntimeEvidence(JsonArray runtimeRows) throws Exception {
        writeCatalogRuntimeEvidence(
                "catalog-console-runtime.json",
                "everyEnabledArgumentFreeConsoleRouteReachesTheSharedDispatcher",
                runtimeRows);
    }

    private static void writeCatalogArgumentRuntimeEvidence(JsonArray runtimeRows) throws Exception {
        writeCatalogRuntimeEvidence(
                "catalog-console-argument-runtime.json",
                "everyMetadataOnlyConsoleVariantExecutesWithRepresentativeArguments",
                runtimeRows);
    }

    private static void writeCatalogRuntimeEvidence(
            String fileName,
            String source,
            JsonArray runtimeRows
    ) throws Exception {
        String evidenceRoot = System.getProperty("sef.audit.evidenceRoot", "").trim();
        if (evidenceRoot.isEmpty()) {
            return;
        }
        String candidateCommit = System.getProperty("sef.audit.candidateCommit", "").trim();
        String candidateSha256 = System.getProperty("sef.audit.candidateSha256", "").trim();
        if (!candidateCommit.matches("[0-9a-f]{40}")
                || !candidateSha256.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("candidate identity properties are required");
        }
        Path root = Path.of(evidenceRoot).toAbsolutePath().normalize();
        Files.createDirectories(root);
        Path output = root.resolve(fileName);
        if (Files.isSymbolicLink(output)) {
            throw new IllegalArgumentException("catalog runtime evidence target is a symlink, " + fileName);
        }
        JsonObject record = new JsonObject();
        record.addProperty("schemaVersion", 1);
        record.addProperty("candidateCommit", candidateCommit);
        record.addProperty("candidateSha256", candidateSha256);
        record.addProperty("source", source);
        record.addProperty("rowCount", runtimeRows.size());
        record.add("rows", runtimeRows);
        Files.writeString(
                output,
                record.toString() + System.lineSeparator(),
                StandardCharsets.UTF_8);
    }

    private static String digest(String value) {
        try {
            return java.util.HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException("sha-256 is unavailable", exception);
        }
    }

    @GameTest(template = "empty", timeoutTicks = 500)
    public static void everyEnabledPositiveArgumentFreeConsoleRouteEmitsCorrelatedAudit(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var dispatcher = server.getCommands().getDispatcher();
        var source = server.createCommandSourceStack();
        List<String> failures = new ArrayList<>();
        Set<String> executed = new LinkedHashSet<>();
        int positiveRoutes = 0;

        for (var definition : KernelServices.catalog().entries()) {
            if (definition.auditClass() == AuditService.AuditClass.NONE
                    || !definition.sourceTypes().contains(CommandDefinition.SourceType.CONSOLE)) {
                continue;
            }
            boolean enabled = KernelServices.featureGates().decide(
                    definition.featureId(),
                    FeatureGateService.Context.server(definition.id())).enabled();
            if (!enabled) {
                continue;
            }
            GuiWorkflowCompiler.WorkflowDefinition workflow;
            try {
                workflow = GuiWorkflowCompiler.compile(definition, dispatcher, source);
            } catch (IllegalArgumentException exception) {
                failures.add(definition.id() + ", workflow, " + exception.getMessage());
                continue;
            }
            for (GuiWorkflowCompiler.Variant variant : workflow.variants()) {
                if (!variant.fields().isEmpty()) {
                    continue;
                }
                String command = render(variant);
                if (command.isBlank() || !executed.add(command)) {
                    continue;
                }
                if (!routeOwnedByDefinition(definition, command)) {
                    continue;
                }
                java.time.Instant startedAt = java.time.Instant.now();
                int result;
                try {
                    result = dispatcher.execute(command, source);
                } catch (Exception exception) {
                    failures.add(definition.id() + ", " + command + ", "
                            + exception.getClass().getSimpleName());
                    continue;
                }
                if (result <= 0) {
                    continue;
                }
                positiveRoutes++;
                List<SecurityAuditService.AuditEvent> events = SecurityAuditService.recent(
                                candidate -> !java.time.Instant.parse(candidate.timestamp()).isBefore(startedAt),
                                32);
                List<SecurityAuditService.AuditEvent> routeEvents = events.stream()
                        .filter(candidate -> {
                            var auditedDefinition = KernelServices.catalog().find(candidate.actionId()).orElse(null);
                            return auditedDefinition != null
                                    && routeOwnedByDefinition(auditedDefinition, command);
                        })
                        .toList();
                if (routeEvents.isEmpty() && !positiveRouteNeedsNoAudit(command)) {
                    failures.add(definition.id() + ", " + command + ", missing audit event");
                    continue;
                }
                if (!positiveRouteNeedsNoAudit(command) && routeEvents.size() != 1) {
                    failures.add(definition.id() + ", " + command + ", expected one audit event but saw "
                            + routeEvents.size());
                }
                for (var event : routeEvents) {
                    var auditedDefinition = KernelServices.catalog().find(event.actionId()).orElse(null);
                    if (!"console".equals(event.sourceType())
                            || event.actorUuid().isBlank()
                            || event.actorUsername().isBlank()
                            || !"success".equals(event.result())
                            || auditedDefinition == null
                            || auditedDefinition.auditClass() == AuditService.AuditClass.NONE
                            || !auditedDefinition.auditClass().name().toLowerCase(java.util.Locale.ROOT)
                                    .equals(event.auditClass())
                            || event.normalizedParameters().values().stream()
                                    .anyMatch(value -> value.contains(command))) {
                        failures.add(definition.id() + ", " + command + ", unsafe audit projection, "
                                + event.actionId());
                    }
                }
            }
        }

        String configurationAction = "sef:config.reload";
        Set<String> configurationBefore = SecurityAuditService.recent(
                        event -> event.actionId().equals(configurationAction),
                        128)
                .stream()
                .map(SecurityAuditService.AuditEvent::eventId)
                .collect(java.util.stream.Collectors.toSet());
        try {
            int result = dispatcher.execute("sef config reload gui", source);
            List<SecurityAuditService.AuditEvent> events = SecurityAuditService.recent(
                            event -> event.actionId().equals(configurationAction)
                                    && !configurationBefore.contains(event.eventId()),
                            8);
            if (result <= 0) {
                failures.add("sef config reload gui, zero result");
            } else if (events.size() != 1) {
                failures.add("sef config reload gui, expected one audit event but saw " + events.size());
            } else {
                var event = events.getFirst();
                if (!"console".equals(event.sourceType()) || !"success".equals(event.result())) {
                    failures.add("sef config reload gui, unsafe shared audit projection");
                }
            }
        } catch (Exception exception) {
            failures.add("sef config reload gui, " + exception.getClass().getSimpleName());
        }

        failures.forEach(failure ->
                ServerEssentialsForge.LOGGER.error("[SEF] Positive console audit, {}", failure));
        helper.assertTrue(
                failures.isEmpty(),
                "positive console audit failed, "
                        + String.join("; ", failures.stream().limit(8).toList()));
        helper.assertTrue(positiveRoutes > 0, "no positive audited console routes were executed");
        ServerEssentialsForge.LOGGER.info(
                "[SEF] Positive argument free console audit covered {} routes", positiveRoutes);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void playerOnlyConsoleRootIsRejectedAndAudited(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var dispatcher = server.getCommands().getDispatcher();
        var source = server.createCommandSourceStack();
        String actionId = "sef:social.message";
        Set<String> before = SecurityAuditService.recent(
                        event -> event.actionId().equals(actionId),
                        128)
                .stream()
                .map(SecurityAuditService.AuditEvent::eventId)
                .collect(java.util.stream.Collectors.toSet());

        int result;
        try {
            result = dispatcher.execute("msg", source);
        } catch (Exception exception) {
            helper.fail("player only console route threw " + exception.getClass().getSimpleName());
            return;
        }

        var event = SecurityAuditService.recent(
                        candidate -> candidate.actionId().equals(actionId)
                                && !before.contains(candidate.eventId()),
                        1)
                .stream()
                .findFirst()
                .orElse(null);
        helper.assertTrue(result == 0, "player only console route returned a positive result");
        helper.assertTrue(event != null, "player only console route did not emit an audit event");
        if (event != null) {
            helper.assertTrue("rejected".equals(event.result()), "player only route was not rejected");
            helper.assertTrue("source_not_allowed".equals(event.reasonCode()),
                    "player only route used the wrong rejection reason");
            helper.assertTrue("console".equals(event.sourceType()),
                    "player only route recorded the wrong source type");
            helper.assertTrue(event.normalizedParameters().isEmpty(),
                    "player only route leaked command parameters");
            helper.assertTrue(event.targetUuids().isEmpty(),
                    "player only route recorded unexpected targets");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 300)
    public static void representativePlayerSourceRoutesEmitAttributedAudit(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var dispatcher = server.getCommands().getDispatcher();
        var target = helper.makeMockServerPlayerInLevel();
        helper.runAfterDelay(20, () -> {
            Map<String, String> routes = Map.of(
                    "getpos", "sef:utility.getpos",
                    "compass", "sef:utility.compass",
                    "depth", "sef:utility.depth");
            List<String> failures = new ArrayList<>();
            int executed = 0;

            for (Map.Entry<String, String> route : routes.entrySet()) {
                Set<String> before = SecurityAuditService.recent(
                                event -> event.actionId().equals(route.getValue()),
                                128)
                        .stream()
                        .map(SecurityAuditService.AuditEvent::eventId)
                        .collect(java.util.stream.Collectors.toSet());
                int result;
                try {
                    result = dispatcher.execute(route.getKey(), target.createCommandSourceStack());
                } catch (Exception exception) {
                    failures.add(route.getKey() + ", " + exception.getClass().getSimpleName());
                    continue;
                }
                var event = SecurityAuditService.recent(
                                candidate -> candidate.actionId().equals(route.getValue())
                                        && !before.contains(candidate.eventId()),
                                1)
                        .stream()
                        .findFirst()
                        .orElse(null);
                if (result <= 0 || event == null) {
                    failures.add(route.getKey() + ", result or audit missing");
                    continue;
                }
                if (!"player".equals(event.sourceType())
                        || !target.getUUID().toString().equals(event.actorUuid())
                        || target.getGameProfile().getName().isBlank()
                        || !target.getGameProfile().getName().equals(event.actorUsername())
                        || !"success".equals(event.result())
                        || !"metadata_only".equals(event.auditClass())
                        || !"metadata".equals(event.redactionClass())
                        || !event.targetUuids().contains(target.getUUID().toString())
                        || event.normalizedParameters().values().stream()
                                .anyMatch(value -> value.contains(route.getKey()))) {
                    failures.add(route.getKey() + ", unsafe player audit projection");
                    continue;
                }
                executed++;
            }

            failures.forEach(failure ->
                    ServerEssentialsForge.LOGGER.error("[SEF] Player source audit, {}", failure));
            helper.assertTrue(
                    failures.isEmpty(),
                    "player source audit failed, " + String.join("; ", failures));
            helper.assertTrue(executed == routes.size(), "player source audit did not cover every route");
            helper.succeed();
        });
    }

    private static boolean routeMatchesCommand(String canonicalRoute, String command) {
        String route = canonicalRoute.toLowerCase(java.util.Locale.ROOT).strip();
        String normalizedCommand = command.toLowerCase(java.util.Locale.ROOT).strip();
        return normalizedCommand.equals(route) || normalizedCommand.startsWith(route + " ");
    }

    private static boolean isCanonicalVariant(
            CommandDefinition definition,
            GuiWorkflowCompiler.Variant variant
    ) {
        String[] routeSegments = definition.canonicalRoute().split(" ");
        List<GuiWorkflowCompiler.Segment> segments = variant.segments();
        if (segments.size() < routeSegments.length) {
            return false;
        }
        for (int index = 0; index < routeSegments.length; index++) {
            GuiWorkflowCompiler.Segment segment = segments.get(index);
            if (!segment.literal() || !segment.value().equals(routeSegments[index])) {
                return false;
            }
        }
        return segments.subList(routeSegments.length, segments.size()).stream()
                .noneMatch(GuiWorkflowCompiler.Segment::literal);
    }

    private static boolean routeOwnedByDefinition(CommandDefinition definition, String command) {
        if (!routeMatchesCommand(definition.canonicalRoute(), command)) {
            return false;
        }
        int ownerLength = definition.canonicalRoute().length();
        return KernelServices.catalog().entries().stream()
                .filter(candidate -> !candidate.id().equals(definition.id()))
                .filter(candidate -> routeMatchesCommand(candidate.canonicalRoute(), command))
                .noneMatch(candidate -> candidate.canonicalRoute().length() > ownerLength);
    }

    private static boolean positiveRouteNeedsNoAudit(String command) {
        String normalized = command.toLowerCase(java.util.Locale.ROOT);
        return normalized.endsWith(" help")
                || normalized.equals("help")
                || normalized.equals("kickall")
                || normalized.equals("sef logging retention run");
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void representativeMetadataOnlyConsoleRoutesEmitBoundedAudit(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var dispatcher = server.getCommands().getDispatcher();
        var source = server.createCommandSourceStack();
        Set<String> representativeActions = Set.of(
                "sef:core.info",
                "sef:core.commands",
                "sef:gui.client.status");
        List<String> failures = new ArrayList<>();
        Set<String> executed = new LinkedHashSet<>();

        for (var definition : KernelServices.catalog().entries()) {
            if (definition.auditClass() != AuditService.AuditClass.METADATA_ONLY
                    || !representativeActions.contains(definition.id())
                    || !definition.sourceTypes().contains(CommandDefinition.SourceType.CONSOLE)) {
                continue;
            }
            boolean enabled = KernelServices.featureGates().decide(
                    definition.featureId(),
                    FeatureGateService.Context.server(definition.id())).enabled();
            if (!enabled) {
                failures.add(definition.id() + ", feature disabled");
                continue;
            }
            GuiWorkflowCompiler.WorkflowDefinition workflow;
            try {
                workflow = GuiWorkflowCompiler.compile(definition, dispatcher, source);
            } catch (IllegalArgumentException exception) {
                failures.add(definition.id() + ", workflow, " + exception.getMessage());
                continue;
            }
            var variant = workflow.variants().stream()
                    .filter(candidate -> candidate.fields().isEmpty())
                    .findFirst()
                    .orElse(null);
            if (variant == null) {
                failures.add(definition.id() + ", no argument free console variant");
                continue;
            }
            String command = render(variant);
            if (command.isBlank() || !executed.add(command)) {
                continue;
            }
            Set<String> before = SecurityAuditService.recent(
                            event -> event.actionId().equals(definition.id()),
                            128)
                    .stream()
                    .map(SecurityAuditService.AuditEvent::eventId)
                    .collect(java.util.stream.Collectors.toSet());
            try {
                int result = dispatcher.execute(command, source);
                if (result <= 0) {
                    failures.add(definition.id() + ", " + command + ", zero result");
                    continue;
                }
            } catch (Exception exception) {
                failures.add(definition.id() + ", " + command + ", "
                        + exception.getClass().getSimpleName());
                continue;
            }
            var event = SecurityAuditService.recent(
                            candidate -> candidate.actionId().equals(definition.id())
                                    && !before.contains(candidate.eventId()),
                            1)
                    .stream()
                    .findFirst()
                    .orElse(null);
            if (event == null) {
                failures.add(definition.id() + ", " + command + ", missing audit event");
                continue;
            }
            if (!"console".equals(event.sourceType())
                    || event.actorUuid().isBlank()
                    || event.actorUsername().isBlank()
                    || !"success".equals(event.result())
                    || !"metadata_only".equals(event.auditClass())
                    || !"metadata".equals(event.redactionClass())
                    || !event.targetUuids().isEmpty()
                    || event.normalizedParameters().values().stream()
                            .anyMatch(value -> value.contains(command))) {
                failures.add(definition.id() + ", " + command + ", unsafe audit projection");
            }
        }

        failures.forEach(failure ->
                ServerEssentialsForge.LOGGER.error("[SEF] Metadata only console audit, {}", failure));
        helper.assertTrue(
                failures.isEmpty(),
                "metadata only console audit failed, "
                        + String.join("; ", failures.stream().limit(8).toList()));
        helper.assertTrue(
                executed.size() == representativeActions.size(),
                "metadata only console audit did not cover every representative action");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void everyServerControlMetadataOnlyConsoleRouteEmitsBoundedAudit(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var dispatcher = server.getCommands().getDispatcher();
        var source = server.createCommandSourceStack();
        List<String> failures = new ArrayList<>();
        Set<String> executed = new LinkedHashSet<>();

        for (var definition : KernelServices.catalog().entries()) {
            if (definition.auditClass() != AuditService.AuditClass.METADATA_ONLY
                    || !definition.id().startsWith("sef:control.")
                    || !definition.sourceTypes().contains(CommandDefinition.SourceType.CONSOLE)) {
                continue;
            }
            boolean enabled = KernelServices.featureGates().decide(
                    definition.featureId(),
                    FeatureGateService.Context.server(definition.id())).enabled();
            if (!enabled) {
                continue;
            }
            GuiWorkflowCompiler.WorkflowDefinition workflow;
            try {
                workflow = GuiWorkflowCompiler.compile(definition, dispatcher, source);
            } catch (IllegalArgumentException exception) {
                continue;
            }
            for (GuiWorkflowCompiler.Variant variant : workflow.variants()) {
                if (!variant.fields().isEmpty()) {
                    continue;
                }
                String command = render(variant);
                String routeKey = definition.id() + "|" + command;
                if (command.isBlank() || !executed.add(routeKey)) {
                    continue;
                }
                if (!routeOwnedByDefinition(definition, command)) {
                    continue;
                }
                Set<String> before = SecurityAuditService.recent(
                                event -> event.actionId().equals(definition.id()),
                                128)
                        .stream()
                        .map(SecurityAuditService.AuditEvent::eventId)
                        .collect(java.util.stream.Collectors.toSet());
                int result;
                try {
                    result = dispatcher.execute(command, source);
                } catch (Exception exception) {
                    failures.add(definition.id() + ", " + command + ", "
                            + exception.getClass().getSimpleName());
                    continue;
                }
                var event = SecurityAuditService.recent(
                                candidate -> candidate.actionId().equals(definition.id())
                                        && !before.contains(candidate.eventId()),
                                1)
                        .stream()
                        .findFirst()
                        .orElse(null);
                if (event == null) {
                    failures.add(definition.id() + ", " + command + ", missing audit event");
                    continue;
                }
                boolean resultProjectionMatches = result > 0
                        ? "success".equals(event.result())
                        : !"success".equals(event.result());
                if (!"console".equals(event.sourceType())
                        || event.actorUuid().isBlank()
                        || event.actorUsername().isBlank()
                        || !resultProjectionMatches
                        || !"metadata_only".equals(event.auditClass())
                        || !"metadata".equals(event.redactionClass())
                        || !event.targetUuids().isEmpty()
                        || event.normalizedParameters().values().stream()
                                .anyMatch(value -> value.contains(command))) {
                    failures.add(definition.id() + ", " + command + ", unsafe audit projection");
                }
            }
        }

        failures.forEach(failure ->
                ServerEssentialsForge.LOGGER.error("[SEF] Metadata only console catalog audit, {}", failure));
        helper.assertTrue(
                failures.isEmpty(),
                "metadata only console catalog audit failed, "
                        + String.join("; ", failures.stream().limit(8).toList()));
        helper.assertTrue(!executed.isEmpty(), "no argument free metadata only console routes were executed");
        ServerEssentialsForge.LOGGER.info(
                "[SEF] Argument free metadata only console audit covered {} unique routes",
                executed.size());
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void representativeMetadataOnlyPlayerRoutesExecuteAndAudit(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var dispatcher = server.getCommands().getDispatcher();
        var target = helper.makeMockServerPlayerInLevel();
        var source = server.createCommandSourceStack();
        helper.runAfterDelay(20, () -> {
            Set<String> representativeActions = Set.of(
                    "sef:utility.exp",
                    "sef:utility.ptime",
                    "sef:utility.pweather");
            List<String> failures = new ArrayList<>();
            Set<String> executed = new LinkedHashSet<>();
            int attempted = 0;

            for (var definition : KernelServices.catalog().entries()) {
                if (definition.auditClass() != AuditService.AuditClass.METADATA_ONLY
                        || !representativeActions.contains(definition.id())
                        || !definition.sourceTypes().contains(CommandDefinition.SourceType.PLAYER)) {
                    continue;
                }
                boolean enabled = KernelServices.featureGates().decide(
                        definition.featureId(),
                        FeatureGateService.Context.server(definition.id())).enabled();
                if (!enabled) {
                    continue;
                }
                GuiWorkflowCompiler.WorkflowDefinition workflow;
                try {
                    workflow = GuiWorkflowCompiler.compileStructure(definition, dispatcher);
                } catch (IllegalArgumentException exception) {
                    failures.add(definition.id() + ", workflow, " + exception.getMessage());
                    continue;
                }
                for (GuiWorkflowCompiler.Variant variant : workflow.variants()) {
                    if (variant.fields().stream().noneMatch(
                            field -> field.type() == GuiWorkflowCompiler.FieldType.PLAYER)) {
                        continue;
                    }
                    String command = render(variant, target.getUUID().toString());
                    if (command.isBlank() || !executed.add(command)) {
                        continue;
                    }
                    int before = SecurityAuditService.recent(
                            event -> event.actionId().equals(definition.id()),
                            128).size();
                    try {
                        int result = dispatcher.execute(command, source);
                        if (result <= 0) {
                            failures.add(definition.id() + ", " + command + ", zero result");
                            continue;
                        }
                        attempted++;
                    } catch (Exception exception) {
                        failures.add(definition.id() + ", " + command + ", "
                                + exception.getClass().getSimpleName() + ", " + exception.getMessage());
                        continue;
                    }
                    int after = SecurityAuditService.recent(
                            event -> event.actionId().equals(definition.id()),
                            128).size();
                    if (after <= before) {
                        failures.add(definition.id() + ", " + command + ", missing audit event");
                    }
                }
            }

            failures.forEach(failure ->
                    ServerEssentialsForge.LOGGER.error("[SEF] Metadata-only command execution, {}", failure));
            helper.assertTrue(
                    failures.isEmpty(),
                    "metadata-only command execution failed, "
                            + String.join("; ", failures.stream().limit(8).toList()));
            helper.assertTrue(attempted > 0, "no representative metadata-only commands were executed");
            ServerEssentialsForge.LOGGER.info(
                    "[SEF] Representative metadata-only command execution covered {} unique routes",
                    attempted);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void everyConsoleWorkflowVariantAcceptsRepresentativeArguments(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var dispatcher = server.getCommands().getDispatcher();
        var source = server.createCommandSourceStack();
        List<String> failures = new ArrayList<>();
        Set<String> parsed = new LinkedHashSet<>();

        for (var definition : KernelServices.catalog().entries()) {
            if (!definition.sourceTypes().contains(CommandDefinition.SourceType.CONSOLE)) {
                continue;
            }
            boolean enabled = KernelServices.featureGates().decide(
                    definition.featureId(),
                    FeatureGateService.Context.server(definition.id())).enabled();
            if (!enabled) {
                continue;
            }
            GuiWorkflowCompiler.WorkflowDefinition workflow;
            try {
                workflow = GuiWorkflowCompiler.compile(definition, dispatcher, source);
            } catch (IllegalArgumentException exception) {
                continue;
            }
            for (GuiWorkflowCompiler.Variant variant : workflow.variants()) {
                String command = render(variant);
                if (!parsed.add(command)) {
                    continue;
                }
                ParseResults<CommandSourceStack> result = dispatcher.parse(command, source);
                if (!result.getExceptions().isEmpty() || result.getReader().canRead()) {
                    String fields = variant.fields().stream()
                            .map(field -> field.id() + ":" + field.type().name().toLowerCase())
                            .reduce((left, right) -> left + "|" + right)
                            .orElse("none");
                    failures.add(definition.id() + ", " + command + ", " + fields);
                }
            }
        }

        failures.forEach(failure ->
                ServerEssentialsForge.LOGGER.error("[SEF] Representative command parsing, {}", failure));
        helper.assertTrue(
                failures.isEmpty(),
                "representative command parsing failed, "
                        + String.join("; ", failures.stream().limit(8).toList()));
        helper.assertTrue(!parsed.isEmpty(), "no representative console command variants were parsed");
        ServerEssentialsForge.LOGGER.info(
                "[SEF] Representative command parsing covered {} unique variants",
                parsed.size());
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void everyMetadataOnlyConsoleVariantExecutesWithRepresentativeArguments(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var dispatcher = server.getCommands().getDispatcher();
        var source = server.createCommandSourceStack();
        var target = helper.makeMockServerPlayerInLevel();
        List<String> failures = new ArrayList<>();
        Set<String> executed = new LinkedHashSet<>();
        JsonArray runtimeRows = new JsonArray();

        for (var definition : KernelServices.catalog().entries()) {
            if (definition.auditClass() != AuditService.AuditClass.METADATA_ONLY
                    || !definition.sourceTypes().contains(CommandDefinition.SourceType.CONSOLE)) {
                continue;
            }
            boolean enabled = KernelServices.featureGates().decide(
                    definition.featureId(),
                    FeatureGateService.Context.server(definition.id())).enabled();
            if (!enabled) {
                continue;
            }
            GuiWorkflowCompiler.WorkflowDefinition workflow;
            try {
                workflow = GuiWorkflowCompiler.compile(definition, dispatcher, source);
            } catch (IllegalArgumentException exception) {
                failures.add(definition.id() + ", workflow, " + exception.getMessage());
                continue;
            }
            for (GuiWorkflowCompiler.Variant variant : workflow.variants()) {
                if (variant.fields().isEmpty() || !isCanonicalVariant(definition, variant)) {
                    continue;
                }
                String command = render(variant, "@a[limit=1]");
                if (command.isBlank() || !executed.add(command)
                        || !routeOwnedByDefinition(definition, command)) {
                    continue;
                }
                Set<String> before = SecurityAuditService.recent(
                                event -> event.actionId().equals(definition.id()),
                                128)
                        .stream()
                        .map(SecurityAuditService.AuditEvent::eventId)
                        .collect(java.util.stream.Collectors.toSet());
                int result;
                try {
                    result = dispatcher.execute(command, source);
                } catch (Exception exception) {
                    failures.add(definition.id() + ", " + command + ", "
                            + exception.getClass().getSimpleName());
                    continue;
                }
                List<SecurityAuditService.AuditEvent> events = SecurityAuditService.recent(
                                event -> event.actionId().equals(definition.id())
                                        && !before.contains(event.eventId()),
                                16);
                var event = events.stream().findFirst().orElse(null);
                if (result <= 0 || event == null || events.size() != 1) {
                    if (result > 0) {
                        failures.add(definition.id() + ", " + command
                                + ", positive result without one audit event, events " + events.size());
                    }
                    continue;
                }
                boolean redactionSafe = event != null
                        && event.normalizedParameters().values().stream()
                        .noneMatch(value -> value.contains(command)
                                        || value.contains(target.getGameProfile().getName())
                                        || value.contains("@a[limit=1]"));
                if (!"console".equals(event.sourceType())
                        || event.actorUuid().isBlank()
                        || event.actorUsername().isBlank()
                        || event.serverSessionId().isBlank()
                        || !"success".equals(event.result())
                        || !"metadata_only".equals(event.auditClass())
                        || !"metadata".equals(event.redactionClass())
                        || !redactionSafe) {
                    failures.add(definition.id() + ", " + command + ", unsafe argument audit projection, result "
                            + result + ", events " + events.size());
                    continue;
                }
                JsonObject runtimeRow = new JsonObject();
                runtimeRow.addProperty("actionId", definition.id());
                runtimeRow.addProperty("canonicalRoute", definition.canonicalRoute());
                runtimeRow.addProperty("commandDigest", digest(command));
                runtimeRow.addProperty("result", "success");
                runtimeRow.addProperty("auditEventCount", events.size());
                runtimeRow.addProperty("sourceType", event.sourceType());
                runtimeRow.addProperty("auditResult", event.result());
                runtimeRow.addProperty("auditClass", event.auditClass());
                runtimeRow.addProperty("redactionClass", event.redactionClass());
                runtimeRow.addProperty("redactionSafe", redactionSafe);
                runtimeRows.add(runtimeRow);
            }
        }

        failures.forEach(failure ->
                ServerEssentialsForge.LOGGER.error("[SEF] Metadata-only argument route, {}", failure));
        helper.assertTrue(
                failures.isEmpty(),
                "metadata-only argument execution failed, "
                        + String.join("; ", failures.stream().limit(8).toList()));
        helper.assertTrue(!executed.isEmpty(), "no metadata-only argument routes were discovered");
        helper.assertTrue(runtimeRows.size() > 0, "no metadata-only argument routes produced runtime evidence");
        try {
            writeCatalogArgumentRuntimeEvidence(runtimeRows);
        } catch (Exception exception) {
            helper.fail("catalog argument runtime evidence could not be written, "
                    + exception.getClass().getSimpleName());
            return;
        }
        ServerEssentialsForge.LOGGER.info(
                "[SEF] Metadata-only argument routes executed {}, evidence rows {}",
                executed.size(),
                runtimeRows.size());
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void everyPlayerFacingActionCompilesToATypedWorkflow(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var dispatcher = server.getCommands().getDispatcher();
        List<String> failures = new ArrayList<>();
        int covered = 0;
        for (var definition : KernelServices.catalog().entries()) {
            if (!definition.playerFacing()) {
                continue;
            }
            UniversalGuiCatalog.ActionRoute route =
                    KernelServices.universalGuiCatalog().action(definition.id()).orElse(null);
            if (route == null) {
                failures.add(definition.id() + ", no GUI route");
                continue;
            }
            if (route.workflowMode() != UniversalGuiCatalog.WorkflowMode.TYPED_COMMAND) {
                covered++;
                continue;
            }
            boolean enabled = KernelServices.featureGates().decide(
                    definition.featureId(),
                    FeatureGateService.Context.server(definition.id())).enabled();
            if (!enabled) {
                covered++;
                continue;
            }
            try {
                var workflow = GuiWorkflowCompiler.compileStructure(definition, dispatcher);
                if (workflow.variants().isEmpty()) {
                    failures.add(definition.id() + ", no variants");
                } else {
                    covered++;
                }
            } catch (IllegalArgumentException exception) {
                failures.add(definition.id() + ", " + exception.getMessage());
            }
        }
        failures.forEach(failure ->
                ServerEssentialsForge.LOGGER.error("[SEF] Typed GUI workflow coverage, {}", failure));
        helper.assertTrue(
                failures.isEmpty(),
                "typed GUI workflow coverage failed, " + String.join("; ", failures.stream().limit(8).toList()));
        helper.assertTrue(
                covered == KernelServices.catalog().entries().stream()
                        .filter(definition -> definition.playerFacing())
                        .count(),
                "typed GUI workflow count does not match the player facing catalog");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void queuedGiveExecutesWithOfflineActorAndOnlyOnce(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var target = helper.makeMockServerPlayerInLevel();
        helper.assertTrue(
                server.getPlayerList().getPlayer(target.getUUID()) != null,
                "mock target is not present in the live player list");
        var definition = KernelServices.catalog()
                .find("sef:item.give.others")
                .orElseThrow();
        var workflow = GuiWorkflowCompiler.compile(
                definition,
                server.getCommands().getDispatcher(),
                server.createCommandSourceStack());
        var variant = workflow.variants().stream()
                .filter(candidate -> candidate.fields().stream()
                        .anyMatch(field -> field.type() == GuiWorkflowCompiler.FieldType.ITEM))
                .filter(candidate -> candidate.fields().stream()
                        .anyMatch(field -> field.type() == GuiWorkflowCompiler.FieldType.PLAYER
                                || field.type() == GuiWorkflowCompiler.FieldType.PLAYERS))
                .findFirst()
                .orElseThrow();
        var targetField = variant.fields().stream()
                .filter(field -> field.type() == GuiWorkflowCompiler.FieldType.PLAYER
                        || field.type() == GuiWorkflowCompiler.FieldType.PLAYERS)
                .findFirst()
                .orElseThrow();
        Map<String, String> values = new LinkedHashMap<>();
        variant.fields().forEach(field -> values.put(field.id(), representative(field)));
        values.put(targetField.id(), target.getGameProfile().getName());
        int before = target.getInventory().countItem(Items.STONE);
        OfflineActionRepository.QueuedAction action = KernelServices.offlineActions().enqueue(
                UUID.randomUUID(),
                target.getUUID(),
                definition.id(),
                variant.id(),
                targetField.id(),
                values,
                Instant.now(),
                Duration.ofMinutes(5L));

        OfflineActionService.executeReady(server, target.getUUID());
        int afterFirstPass = target.getInventory().countItem(Items.STONE);
        OfflineActionService.executeReady(server, target.getUUID());
        int afterSecondPass = target.getInventory().countItem(Items.STONE);

        helper.assertTrue(afterFirstPass > before, "queued give did not mutate the target inventory");
        helper.assertTrue(afterSecondPass == afterFirstPass, "queued give executed more than once");
        helper.assertTrue(
                KernelServices.offlineActions().entries().stream()
                        .filter(entry -> entry.id().equals(action.id()))
                        .anyMatch(entry -> entry.state() == OfflineActionRepository.ActionState.SUCCEEDED),
                "queued give did not persist a successful terminal outcome");
        helper.succeed();
    }

    private static String render(GuiWorkflowCompiler.Variant variant) {
        var fields = variant.fields().stream()
                .collect(java.util.stream.Collectors.toMap(
                        GuiWorkflowCompiler.Field::id,
                        field -> field));
        return variant.segments().stream()
                .map(segment -> segment.literal()
                        ? segment.value()
                        : representative(fields.get(segment.value())))
                .reduce((left, right) -> left + " " + right)
                .orElse("");
    }

    private static String render(GuiWorkflowCompiler.Variant variant, String playerValue) {
        var fields = variant.fields().stream()
                .collect(java.util.stream.Collectors.toMap(
                        GuiWorkflowCompiler.Field::id,
                        field -> field));
        return variant.segments().stream()
                .map(segment -> segment.literal()
                        ? segment.value()
                        : (fields.get(segment.value()).type() == GuiWorkflowCompiler.FieldType.PLAYER
                                || fields.get(segment.value()).type() == GuiWorkflowCompiler.FieldType.PLAYERS)
                        ? playerValue
                        : representative(fields.get(segment.value())))
                .reduce((left, right) -> left + " " + right)
                .orElse("");
    }

    private static String representative(GuiWorkflowCompiler.Field field) {
        String fieldId = field.id().toLowerCase(java.util.Locale.ROOT);
        if (fieldId.contains("enchantment")) {
            return "minecraft:sharpness";
        }
        if (fieldId.equals("gamemode") || fieldId.equals("game_mode")) {
            return "creative";
        }
        if (fieldId.equals("amount") || fieldId.equals("price") || fieldId.equals("value")) {
            return "1";
        }
        if (fieldId.equals("rotation")) {
            return "~ ~";
        }
        if (fieldId.contains("anchor")) {
            return "eyes";
        }
        return switch (field.type()) {
            case BOOLEAN -> "false";
            case INTEGER -> Long.toString(Math.round(
                    Math.max(field.minimum(), Math.min(field.maximum(), 1.0D))));
            case DECIMAL -> Double.toString(
                    Math.max(field.minimum(), Math.min(field.maximum(), 1.0D)));
            case DURATION -> "1s";
            case PLAYER -> "test-mock-player";
            case PLAYERS -> "@a[limit=1]";
            case ITEM -> "minecraft:stone";
            case ENCHANTMENT -> "minecraft:sharpness";
            case DIMENSION -> "minecraft:overworld";
            case COORDINATES -> "~ ~ ~";
            case PERMISSION -> "sef.commands.test";
            case RESOURCE_LOCATION -> "minecraft:stone";
            case IDENTIFIER -> "00000000-0000-0000-0000-000000000001";
            case TEXT -> switch (field.renderMode()) {
                case QUOTED -> "\"test value\"";
                case GREEDY -> "test value";
                default -> "test";
            };
        };
    }
}
