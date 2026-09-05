package com.enviouse.sef.commandlog;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.kernel.observation.ObservationContracts;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.vanish.VanishUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.CommandEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class CommandEventJournal {
    private static final int MAXIMUM_PENDING = 1024;
    private static final ThreadLocal<UUID> CURRENT_EVENT = new ThreadLocal<>();

    private final CommandSpyRepository spies;
    private final FileLogSink fileLogSink;
    private final int maximumRecent;
    private final int eventsPerSecond;
    private final Deque<CommandRecord> recent = new ArrayDeque<>();
    private final Map<UUID, PendingCommand> pending = new LinkedHashMap<>();
    private final Map<UUID, DeliveryWindow> deliveryWindows = new HashMap<>();

    public CommandEventJournal(
            CommandSpyRepository spies,
            FileLogSink fileLogSink,
            int maximumRecent,
            int eventsPerSecond
    ) {
        this.spies = Objects.requireNonNull(spies, "spies");
        this.fileLogSink = Objects.requireNonNull(fileLogSink, "fileLogSink");
        if (maximumRecent < 32 || maximumRecent > 65536) {
            throw new IllegalArgumentException("Command journal recent limit is outside hard bounds");
        }
        if (eventsPerSecond < 1 || eventsPerSecond > 1000) {
            throw new IllegalArgumentException("Command journal delivery limit is outside hard bounds");
        }
        this.maximumRecent = maximumRecent;
        this.eventsPerSecond = eventsPerSecond;
    }

    public void onCommand(CommandEvent event) {
        Objects.requireNonNull(event, "event");
        CommandSourceStack source = event.getParseResults().getContext().getSource();
        String input = event.getParseResults().getReader().getString();
        CommandRedactionPolicy.RedactedCommand redacted = CommandRedactionPolicy.redact(input);
        UUID eventId = UUID.randomUUID();
        PendingCommand command = PendingCommand.create(eventId, source, redacted);

        synchronized (this) {
            if (pending.size() >= MAXIMUM_PENDING) {
                UUID oldest = pending.keySet().iterator().next();
                PendingCommand displaced = pending.remove(oldest);
                if (displaced != null) {
                    append(displaced.record(
                            ObservationContracts.LifecycleStage.OUTCOME_UNKNOWN,
                            null,
                            "pending_limit",
                            Duration.between(displaced.startedAt(), Instant.now()).toMillis()));
                }
            }
            pending.put(eventId, command);
        }
        CURRENT_EVENT.set(eventId);
        append(command.record(ObservationContracts.LifecycleStage.PARSED, null, "", 0L));

        if (!event.getParseResults().getExceptions().isEmpty()) {
            finish(eventId, ObservationContracts.LifecycleStage.REJECTED, null, "parse_error");
            return;
        }

        MinecraftServer server = source.getServer();
        server.execute(() -> {
            ObservationContracts.LifecycleStage stage;
            String detail;
            if (event.isCanceled()) {
                stage = ObservationContracts.LifecycleStage.CANCELLED;
                detail = "cancelled_by_event";
            } else if (event.getException() != null) {
                stage = ObservationContracts.LifecycleStage.FAILED;
                detail = event.getException().getClass().getSimpleName();
            } else {
                stage = ObservationContracts.LifecycleStage.OUTCOME_UNKNOWN;
                detail = "external_outcome_unknown";
            }
            finish(eventId, stage, null, detail);
            if (eventId.equals(CURRENT_EVENT.get())) {
                CURRENT_EVENT.remove();
            }
        });
    }

    public UUID attachOrBegin(CommandSourceStack source, String actionId) {
        UUID current = CURRENT_EVENT.get();
        PendingCommand command;
        synchronized (this) {
            command = current == null ? null : pending.get(current);
            if (command == null) {
                CommandRedactionPolicy.RedactedCommand redacted =
                        CommandRedactionPolicy.redact(actionId.replace(':', ' '));
                current = UUID.randomUUID();
                command = PendingCommand.create(current, source, redacted);
                pending.put(current, command);
                CURRENT_EVENT.set(current);
            }
            command = command.withAction(actionId);
            pending.put(current, command);
        }
        append(command.record(ObservationContracts.LifecycleStage.STARTED, null, "", 0L));
        return current;
    }

    public void finishCurrent(
            ObservationContracts.LifecycleStage stage,
            Integer resultCode,
            String detail
    ) {
        UUID current = CURRENT_EVENT.get();
        if (current != null) {
            finish(current, stage, resultCode, detail);
        }
        CURRENT_EVENT.remove();
    }

    public synchronized List<CommandRecord> recent(int maximum, boolean terminalOnly) {
        int limit = Math.clamp(maximum, 1, 256);
        List<CommandRecord> result = new ArrayList<>(limit);
        var iterator = recent.descendingIterator();
        while (iterator.hasNext() && result.size() < limit) {
            CommandRecord record = iterator.next();
            if (!terminalOnly || record.terminal()) {
                result.add(record);
            }
        }
        return List.copyOf(result);
    }

    public List<CommandRecord> recentAuthorized(ServerPlayer observer, int maximum) {
        Objects.requireNonNull(observer, "observer");
        int limit = Math.clamp(maximum, 1, 256);
        MinecraftServer server = observer.getServer();
        CommandSpyRepository.Profile profile = spies.profile(observer.getUUID());
        return recent(limit, true).stream()
                .filter(record -> eligible(observer, profile, record, server, false))
                .limit(limit)
                .toList();
    }

    public synchronized void clearRuntime() {
        pending.clear();
        deliveryWindows.clear();
        recent.clear();
        CURRENT_EVENT.remove();
    }

    public synchronized void clearDeliveryState(UUID observerId) {
        deliveryWindows.remove(observerId);
    }

    private void finish(
            UUID eventId,
            ObservationContracts.LifecycleStage stage,
            Integer resultCode,
            String detail
    ) {
        PendingCommand command;
        synchronized (this) {
            command = pending.remove(eventId);
        }
        if (command == null) {
            return;
        }
        long duration = Math.max(0L, Duration.between(command.startedAt(), Instant.now()).toMillis());
        append(command.record(stage, resultCode, detail, duration));
        if (eventId.equals(CURRENT_EVENT.get())) {
            CURRENT_EVENT.remove();
        }
    }

    private void append(CommandRecord record) {
        synchronized (this) {
            recent.addLast(record);
            while (recent.size() > maximumRecent) {
                recent.removeFirst();
            }
        }
        fileLogSink.submit(record);
        if (record.terminal()) {
            project(record);
        }
    }

    private void project(CommandRecord record) {
        MinecraftServer server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        for (ServerPlayer observer : server.getPlayerList().getPlayers()) {
            CommandSpyRepository.Profile profile = spies.profile(observer.getUUID());
            if (!eligible(observer, profile, record, server, true)) {
                continue;
            }
            if (!consumeDelivery(observer.getUUID())) {
                continue;
            }
            observer.sendSystemMessage(render(observer, profile, record));
        }
    }

    private boolean eligible(
            ServerPlayer observer,
            CommandSpyRepository.Profile profile,
            CommandRecord record,
            MinecraftServer server,
            boolean requireEnabled
    ) {
        if (requireEnabled && !profile.enabled()
                || !PermissionService.has(observer, PermissionsHandler.phasePermission("commands.commandspy"))
                || !PermissionService.has(observer, PermissionsHandler.phasePermission("commandspy.view.metadata"))) {
            return false;
        }
        boolean playerSource = record.sourceType() == CommandDefinition.SourceType.PLAYER;
        if (playerSource
                && !PermissionService.has(observer, PermissionsHandler.phasePermission("commands.commandspy.scope.player"))
                || !playerSource
                && !PermissionService.has(observer, PermissionsHandler.phasePermission("commands.commandspy.scope.nonplayer"))) {
            return false;
        }
        if (playerSource && !profile.playerSources() || !playerSource && !profile.nonPlayerSources()) {
            return false;
        }
        if (profile.audience() == CommandSpyRepository.Audience.EVERYONE
                && !PermissionService.has(observer, PermissionsHandler.phasePermission("commands.commandspy.everyone"))) {
            return false;
        }
        if (profile.audience() == CommandSpyRepository.Audience.SELECTED
                && !PermissionService.has(observer, PermissionsHandler.phasePermission("commands.commandspy.player"))
                && !PermissionService.has(observer, PermissionsHandler.phasePermission("commands.commandspy.selected"))) {
            return false;
        }
        UUID matchId = switch (profile.actorRelation()) {
            case INITIATOR -> record.initiatorId();
            case EFFECTIVE -> record.effectiveActorId();
            case EITHER -> profile.selectedPlayerIds().contains(record.initiatorId())
                    ? record.initiatorId()
                    : record.effectiveActorId();
        };
        if (profile.audience() == CommandSpyRepository.Audience.SELECTED
                && (matchId == null || !profile.selectedPlayerIds().contains(matchId))) {
            return false;
        }
        if (!profile.includedRoots().isEmpty() && !profile.includedRoots().contains(record.root())
                || profile.excludedRoots().contains(record.root())) {
            return false;
        }
        if (!profile.includedActions().isEmpty() && !profile.includedActions().contains(record.actionId())
                || profile.excludedActions().contains(record.actionId())) {
            return false;
        }
        CommandSpyRepository.TypedFilters filters = profile.typedFilters();
        if (filters.disabledSources().contains(record.sourceType().name().toLowerCase(Locale.ROOT))
                || filters.disabledResults().contains(record.stage().name().toLowerCase(Locale.ROOT))
                || filters.disabledWorlds().contains(record.dimensionId().toLowerCase(Locale.ROOT))
                || filters.disabledOrigins().contains(record.origin())) {
            return false;
        }
        Set<String> actorIds = java.util.stream.Stream.of(record.initiatorId(), record.effectiveActorId())
                .filter(Objects::nonNull)
                .map(UUID::toString)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        if (!filters.includedPlayers().isEmpty()
                && actorIds.stream().noneMatch(filters.includedPlayers()::contains)
                || actorIds.stream().anyMatch(filters.excludedPlayers()::contains)) {
            return false;
        }
        UUID subjectId = matchId == null ? record.initiatorId() : matchId;
        if (subjectId == null || subjectId.equals(observer.getUUID())) {
            return true;
        }
        ServerPlayer subject = server.getPlayerList().getPlayer(subjectId);
        if (subject == null) {
            return profile.audience() == CommandSpyRepository.Audience.EVERYONE;
        }
        if (VanishUtil.isVanished(subject, observer)) {
            return false;
        }
        return PlayerTargetPolicy.decide(
                observer.createCommandSourceStack(),
                subject,
                PermissionsHandler.phasePermission("commandspy.hierarchy.bypass"),
                PermissionsHandler.phasePermission("commandspy.exempt"),
                PermissionsHandler.phasePermission("commandspy.view.exempt"),
                false,
                true).allowed();
    }

    private net.minecraft.network.chat.Component render(
            ServerPlayer observer,
            CommandSpyRepository.Profile profile,
            CommandRecord record
    ) {
        boolean arguments = PermissionService.has(
                observer,
                PermissionsHandler.phasePermission("commandspy.view.arguments"));
        String command = arguments && record.redactionClass() == CommandRedactionPolicy.RedactionClass.PUBLIC
                ? record.commandDisplay()
                : "/" + record.root() + (record.redactionClass() == CommandRedactionPolicy.RedactionClass.PUBLIC
                ? " <arguments hidden>"
                : " <redacted>");
        StringBuilder value = new StringBuilder("&8[&b")
                .append(record.sourceType().name().toLowerCase(Locale.ROOT))
                .append("&8] ");
        if (profile.includeLocation()
                && PermissionService.has(observer, PermissionsHandler.phasePermission("commandspy.view.location"))) {
            value.append("&8[&7")
                    .append(record.dimensionId())
                    .append(' ')
                    .append(record.x()).append(' ')
                    .append(record.y()).append(' ')
                    .append(record.z())
                    .append("&8] ");
        }
        value.append("&e").append(record.actorName()).append("&7: &f").append(command);
        if (profile.includeResults()
                && PermissionService.has(observer, PermissionsHandler.phasePermission("commandspy.view.result"))) {
            value.append(" &8[&7")
                    .append(record.stage().name().toLowerCase(Locale.ROOT));
            if (record.resultCode() != null) {
                value.append(' ').append(record.resultCode());
            }
            value.append("&8]");
        }
        return TextFormatter.stringToFormattedText(value.toString());
    }

    private synchronized boolean consumeDelivery(UUID observerId) {
        long second = System.currentTimeMillis() / 1000L;
        DeliveryWindow window = deliveryWindows.get(observerId);
        if (window == null || window.second() != second) {
            deliveryWindows.put(observerId, new DeliveryWindow(second, 1));
            if (deliveryWindows.size() > 10000) {
                deliveryWindows.entrySet().removeIf(entry -> entry.getValue().second() + 60L < second);
            }
            return true;
        }
        if (window.count() >= eventsPerSecond) {
            return false;
        }
        deliveryWindows.put(observerId, new DeliveryWindow(second, window.count() + 1));
        return true;
    }

    private record DeliveryWindow(long second, int count) {
    }

    private record PendingCommand(
            UUID eventId,
            UUID parentEventId,
            UUID sessionId,
            Instant startedAt,
            UUID initiatorId,
            UUID effectiveActorId,
            String actorName,
            CommandDefinition.SourceType sourceType,
            String dimensionId,
            int x,
            int y,
            int z,
            String root,
            String actionId,
            String commandDisplay,
            CommandRedactionPolicy.RedactionClass redactionClass,
            Set<String> redactionRuleIds,
            String origin,
            boolean feedbackSuppressed
    ) {
        static PendingCommand create(
                UUID eventId,
                CommandSourceStack source,
                CommandRedactionPolicy.RedactedCommand redacted
        ) {
            ServerPlayer player = source.getPlayer();
            UUID actorId = player == null
                    ? UUID.nameUUIDFromBytes(("sef:" + source.getTextName()).getBytes(java.nio.charset.StandardCharsets.UTF_8))
                    : player.getUUID();
            var position = source.getPosition();
            return new PendingCommand(
                    eventId,
                    null,
                    com.enviouse.sef.audit.SecurityAuditService.currentSessionId(),
                    Instant.now(),
                    actorId,
                    actorId,
                    Objects.requireNonNullElse(source.getTextName(), "server"),
                    KernelCommandExecutor.sourceType(source),
                    source.getLevel() == null ? "unknown" : source.getLevel().dimension().location().toString(),
                    (int) Math.floor(position.x),
                    (int) Math.floor(position.y),
                    (int) Math.floor(position.z),
                    redacted.root(),
                    "",
                    redacted.display(),
                    redacted.redactionClass(),
                    redacted.ruleIds(),
                    origin(KernelCommandExecutor.sourceType(source)),
                    false);
        }

        PendingCommand withAction(String value) {
            return new PendingCommand(
                    eventId, parentEventId, sessionId, startedAt, initiatorId, effectiveActorId,
                    actorName, sourceType, dimensionId, x, y, z, root,
                    value == null ? "" : value.trim().toLowerCase(Locale.ROOT),
                    commandDisplay, redactionClass, redactionRuleIds, origin, feedbackSuppressed);
        }

        CommandRecord record(
                ObservationContracts.LifecycleStage stage,
                Integer resultCode,
                String detail,
                long durationMillis
        ) {
            return new CommandRecord(
                    1,
                    eventId,
                    parentEventId,
                    sessionId,
                    Instant.now(),
                    stage,
                    initiatorId,
                    effectiveActorId,
                    actorName,
                    sourceType,
                    dimensionId,
                    x,
                    y,
                    z,
                    root,
                    actionId,
                    commandDisplay,
                    redactionClass,
                    redactionRuleIds,
                    origin,
                    feedbackSuppressed,
                    resultCode,
                    durationMillis,
                    detail);
        }

        private static String origin(CommandDefinition.SourceType sourceType) {
            return switch (sourceType) {
                case CONSOLE -> "console";
                case RCON -> "rcon";
                case COMMAND_BLOCK -> "command_block";
                case FUNCTION -> "function";
                case SCHEDULED_TASK -> "scheduler";
                case PANEL -> "panel";
                case BUNDLE -> "bundle";
                case SUDO -> "sudo";
                case SERVER_PROFILE -> "execution_profile";
                case RUN_SERVER_WRAPPER -> "run_server";
                case SILENT_ACTOR_WRAPPER -> "silent_actor";
                case SILENT_SERVER_WRAPPER -> "silent_server";
                case INTEGRATION, EXTERNAL_ADAPTER -> "external_integration";
                default -> "player";
            };
        }
    }

    public record CommandRecord(
            int schemaVersion,
            UUID eventId,
            UUID parentEventId,
            UUID serverSessionId,
            Instant timestamp,
            ObservationContracts.LifecycleStage stage,
            UUID initiatorId,
            UUID effectiveActorId,
            String actorName,
            CommandDefinition.SourceType sourceType,
            String dimensionId,
            int x,
            int y,
            int z,
            String root,
            String actionId,
            String commandDisplay,
            CommandRedactionPolicy.RedactionClass redactionClass,
            Set<String> redactionRuleIds,
            String origin,
            boolean feedbackSuppressed,
            Integer resultCode,
            long durationMillis,
            String detail
    ) {
        public CommandRecord {
            if (schemaVersion != 1) {
                throw new IllegalArgumentException("Unsupported command journal schema");
            }
            Objects.requireNonNull(eventId, "eventId");
            Objects.requireNonNull(serverSessionId, "serverSessionId");
            Objects.requireNonNull(timestamp, "timestamp");
            Objects.requireNonNull(stage, "stage");
            actorName = bounded(actorName, 64);
            Objects.requireNonNull(sourceType, "sourceType");
            dimensionId = bounded(dimensionId, 128);
            root = bounded(root, 64).toLowerCase(Locale.ROOT);
            actionId = bounded(actionId, 128).toLowerCase(Locale.ROOT);
            commandDisplay = bounded(commandDisplay, 512);
            Objects.requireNonNull(redactionClass, "redactionClass");
            redactionRuleIds = Set.copyOf(redactionRuleIds);
            origin = bounded(origin, 64).toLowerCase(Locale.ROOT);
            detail = bounded(detail, 128);
            if (durationMillis < 0L) {
                throw new IllegalArgumentException("Command duration cannot be negative");
            }
        }

        public boolean terminal() {
            return stage == ObservationContracts.LifecycleStage.COMPLETED
                    || stage == ObservationContracts.LifecycleStage.FAILED
                    || stage == ObservationContracts.LifecycleStage.CANCELLED
                    || stage == ObservationContracts.LifecycleStage.REJECTED
                    || stage == ObservationContracts.LifecycleStage.OUTCOME_UNKNOWN;
        }

        private static String bounded(String value, int maximumLength) {
            if (value == null) {
                return "";
            }
            String sanitized = value.replace("\r", "\\r").replace("\n", "\\n").codePoints()
                    .filter(codePoint -> !Character.isISOControl(codePoint)
                            && Character.getType(codePoint) != Character.FORMAT)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString()
                    .trim();
            return sanitized.length() <= maximumLength ? sanitized : sanitized.substring(0, maximumLength);
        }
    }
}
