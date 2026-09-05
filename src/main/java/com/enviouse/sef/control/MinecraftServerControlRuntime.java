package com.enviouse.sef.control;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.gui.protocol.SefPayloads;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.player.PlayerStateService;
import com.enviouse.sef.vanish.VanishUtil;
import com.mojang.authlib.GameProfile;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.entity.player.PlayerNegotiationEvent;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public final class MinecraftServerControlRuntime {
    private static final int MAXIMUM_CACHED_ACTIVE_RECORDS = 4096;
    private static final int MAXIMUM_REMOVALS_PER_PASS = 10_000;
    private static final Pattern SAFE_GAMERULE = Pattern.compile("[A-Za-z0-9_]{1,128}");
    private static final Pattern SAFE_GAMERULE_VALUE = Pattern.compile("[A-Za-z0-9._+-]{1,128}");
    private static final Map<UUID, Instant> LAST_CHAT = new LinkedHashMap<>();
    private static final Map<UUID, Long> LAST_MAINTENANCE_NOTICE = new LinkedHashMap<>();
    private static final Map<UUID, Long> LAST_CLEANUP = new LinkedHashMap<>();
    private static final Map<UUID, Deque<Long>> COMMAND_WINDOWS = new LinkedHashMap<>();
    private static final Map<UUID, QuarantineAnchor> QUARANTINE_ANCHORS = new LinkedHashMap<>();
    private static final Map<UUID, Set<Long>> RESTART_WARNINGS = new LinkedHashMap<>();
    private static final Map<UUID, Long> LAST_GUARDRAIL_WARNING = new LinkedHashMap<>();
    private static final Map<UUID, Long> LAST_PERFORMANCE_QUERY = new LinkedHashMap<>();
    private static final Deque<Long> JOIN_WINDOW = new ArrayDeque<>();
    private static final Deque<AdmissionQueueEntry> ADMISSION_QUEUE = new ArrayDeque<>();
    private static final Map<UUID, Instant> RELEASED_ADMISSIONS = new LinkedHashMap<>();
    private static GuardrailSnapshot guardrailSnapshot;
    private static PerformanceSnapshot performanceSnapshot;
    private static long lastGlobalPerformanceQuery;
    private static long cachedRevision = -1L;
    private static Map<String, List<ServerControlRepository.ControlRecord>> active = Map.of();
    private MinecraftServerControlRuntime() {
    }

    public static List<String> unavailableRuntimeFeatures() {
        return ServerControlRuntimeAvailability.features();
    }

    public static void registerHandlers(ServerControlExecutionService executions) {
        for (String feature : List.of(
                "maintenance",
                "guardrails",
                "change_windows",
                "resource_governor",
                "chat_channels",
                "mentions",
                "interaction_blocks",
                "session_quarantine",
                "rules",
                "onboarding",
                "playtime_rewards",
                "daily_rewards",
                "weekly_rewards",
                "afk_zones",
                "death_compass",
                "graves",
                "inventory_recovery",
                "restart_coordinator",
                "resource_worlds",
                "admin_journal",
                "command_anomaly",
                "rollouts",
                "server_calendar",
                "waypoints",
                "portal_policy",
                "staff_duty",
                "approvals",
                "capability_leases",
                "admin_lock",
                "automod",
                "chat_control",
                "admission",
                "invites",
                "server_presentation",
                "spawn_ecology",
                "polls",
                "community_events",
                "knowledge",
                "display_profiles",
                "display_ownership")) {
            if (ServerControlRuntimeAvailability.unavailable(feature)) {
                executions.registerUnavailable(
                        feature,
                        feature + " runtime behavior is unavailable, the record cannot be activated");
            } else {
                executions.register(feature, MinecraftServerControlRuntime::activatePolicy);
            }
        }
        for (String feature : List.of(
                "reports",
                "tickets",
                "staff_notes",
                "player_warp_review",
                "incidents",
                "appeals",
                "discipline",
                "access_applications",
                "privacy")) {
            if (ServerControlRuntimeAvailability.unavailable(feature)) {
                executions.registerUnavailable(
                        feature,
                        feature + " runtime behavior is unavailable, the record cannot be resolved");
            } else {
                executions.register(feature, MinecraftServerControlRuntime::resolveReview);
            }
        }
        executions.register("policy_lab", MinecraftServerControlRuntime::policyLab);
        executions.register("config_drift", MinecraftServerControlRuntime::configDrift);
        executions.register("permission_impact", MinecraftServerControlRuntime::permissionImpact);
        executions.register("dependency_graph", MinecraftServerControlRuntime::dependencyGraph);
        executions.register("player_impact", MinecraftServerControlRuntime::playerImpact);
        executions.register("operational_snapshots", MinecraftServerControlRuntime::operationalSnapshot);
        executions.register("alias_diagnostics", MinecraftServerControlRuntime::aliasDiagnostics);
        executions.register("friends", MinecraftServerControlRuntime::relationship);
        executions.register("parcels", MinecraftServerControlRuntime::escrow);
        executions.register("lost_found", MinecraftServerControlRuntime::escrow);
        executions.register("trades", MinecraftServerControlRuntime::escrow);
        executions.register("auctions", MinecraftServerControlRuntime::escrow);
        executions.register("evidence", MinecraftServerControlRuntime::evidence);
        executions.register("chunk_pregen", MinecraftServerControlRuntime::externalProvider);
        executions.register("backups", MinecraftServerControlRuntime::externalProvider);
        executions.register("sleep_vote", MinecraftServerControlRuntime::sleepVote);
        executions.register("world_policy", MinecraftServerControlRuntime::worldPolicy);
        executions.register("world_border", MinecraftServerControlRuntime::worldBorder);
        executions.register("resource_packs", MinecraftServerControlRuntime::resourcePack);
        executions.register("cleanup", MinecraftServerControlRuntime::cleanupAdmission);
        executions.register("mod_health", MinecraftServerControlRuntime::modHealth);
        executions.register("performance", MinecraftServerControlRuntime::performance);
        executions.register("queue", MinecraftServerControlRuntime::queue);
        executions.register("datapacks", MinecraftServerControlRuntime::datapacks);
        executions.register("chunk_tickets", MinecraftServerControlRuntime::chunkTickets);
        executions.register("block_activity", MinecraftServerControlRuntime::blockActivity);
    }

    public static boolean allowCommand(CommandEvent event) {
        ServerPlayer player = event.getParseResults().getContext().getSource().getPlayer();
        if (player == null) {
            return true;
        }
        ServerControlRepository.ControlRecord quarantine = quarantine(player, "commands").orElse(null);
        if (quarantine != null && !exempt(player, "session_quarantine")) {
            event.setCanceled(true);
            player.sendSystemMessage(TextFormatter.stringToFormattedText(
                    "&cCommands are unavailable during this session quarantine."));
            return false;
        }
        ServerControlRepository.ControlRecord rules = pendingRules(player).orElse(null);
        String root = commandRoot(event.getParseResults().getReader().getString());
        if (rules != null
                && Set.of("commands", "lobby").contains(field(rules, "restriction", "none"))
                && !Set.of("rules", "help", "list", "msg", "tell", "w", "whisper").contains(root)) {
            event.setCanceled(true);
            player.sendSystemMessage(TextFormatter.stringToFormattedText(
                    "&cAccept the current server rules with /rules accept before using that command."));
            return false;
        }
        ServerControlRepository.ControlRecord policy = latestEffective("command_anomaly").orElse(null);
        if (policy == null || exempt(player, "command_anomaly")) {
            return true;
        }
        long now = Instant.now().getEpochSecond();
        long window = number(policy, "window_seconds", 10L);
        int maximum = (int) number(policy, "commands_per_window", 20L);
        int count;
        synchronized (MinecraftServerControlRuntime.class) {
            Deque<Long> samples = COMMAND_WINDOWS.computeIfAbsent(player.getUUID(), ignored -> new ArrayDeque<>());
            while (!samples.isEmpty() && samples.peekFirst() <= now - window) {
                samples.removeFirst();
            }
            samples.addLast(now);
            while (samples.size() > 100_000) {
                samples.removeFirst();
            }
            count = samples.size();
        }
        if (count <= maximum) {
            return true;
        }
        String response = field(policy, "response", "observe");
        if (response.equals("alert") || response.equals("throttle") || response.equals("quarantine")) {
            alertStaff(player.server, "command anomaly for " + player.getGameProfile().getName()
                    + ", " + count + " commands in " + window + " seconds");
        }
        if (response.equals("quarantine")) {
            createAutomaticQuarantine(player, "command rate anomaly");
        }
        if (response.equals("throttle") || response.equals("quarantine")) {
            event.setCanceled(true);
            player.sendSystemMessage(TextFormatter.stringToFormattedText(
                    "&cYour command rate exceeded the server policy."));
            return false;
        }
        return true;
    }

    public static boolean allowMovementAction(ServerPlayer player) {
        if (quarantine(player, "movement").isEmpty()
                && quarantine(player, "all").isEmpty()
                || exempt(player, "session_quarantine")) {
            return true;
        }
        player.sendSystemMessage(TextFormatter.stringToFormattedText(
                "&cInteractions are unavailable during this session quarantine."));
        return false;
    }

    public static boolean allowChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        if (player == null) {
            return true;
        }
        if (quarantine(player, "chat").isPresent() && !exempt(player, "session_quarantine")) {
            event.setCanceled(true);
            player.sendSystemMessage(TextFormatter.stringToFormattedText(
                    "&cChat is unavailable during this session quarantine."));
            return false;
        }
        ServerControlRepository.ControlRecord rules = pendingRules(player).orElse(null);
        if (rules != null && Set.of("chat", "lobby").contains(field(rules, "restriction", "none"))) {
            event.setCanceled(true);
            player.sendSystemMessage(TextFormatter.stringToFormattedText(
                    "&cAccept the current server rules with /rules accept before chatting."));
            return false;
        }
        ServerControlRepository.ControlRecord chatPolicy = latestEffective("chat_control").orElse(null);
        if (chatPolicy != null && !exempt(player, "chat_control")) {
            String mode = field(chatPolicy, "mode", "open");
            String message = field(chatPolicy, "message", "Chat is temporarily restricted.");
            boolean staff = has(player, "commands.control.chat_control.manage");
            if (mode.equals("locked")
                    || mode.equals("read_only")
                    || mode.equals("staff_only") && !staff) {
                event.setCanceled(true);
                player.sendSystemMessage(TextFormatter.stringToFormattedText("&c" + message));
                return false;
            }
            if (mode.equals("slow")) {
                long seconds = number(chatPolicy, "slow_seconds", 5L);
                Instant now = Instant.now();
                Instant previous;
                synchronized (MinecraftServerControlRuntime.class) {
                    previous = LAST_CHAT.get(player.getUUID());
                    if (previous == null || !previous.plusSeconds(seconds).isAfter(now)) {
                        boundedPut(LAST_CHAT, player.getUUID(), now, 4096);
                    }
                }
                if (previous != null && previous.plusSeconds(seconds).isAfter(now)) {
                    long remaining = Math.max(1L, Duration.between(now, previous.plusSeconds(seconds)).toSeconds());
                    event.setCanceled(true);
                    player.sendSystemMessage(TextFormatter.stringToFormattedText(
                            "&cChat slow mode is active. Wait &e" + remaining + "&c seconds."));
                    return false;
                }
            }
        }

        String message = event.getMessage().getString();
        String normalized = message.toLowerCase(Locale.ROOT);
        for (ServerControlRepository.ControlRecord rule : active("automod")) {
            String matcher = field(rule, "matcher", "literal");
            String pattern = field(rule, "pattern", "").toLowerCase(Locale.ROOT);
            boolean matched = !pattern.isBlank() && switch (matcher) {
                case "literal" -> normalized.contains(pattern);
                case "glob" -> safeGlob(normalized, pattern);
                case "regex_adapter" -> false;
                default -> false;
            };
            if (!matched) {
                continue;
            }
            String response = field(rule, "response", "flag");
            createAutomodReport(player, message, rule);
            if (response.equals("block")) {
                event.setCanceled(true);
                player.sendSystemMessage(TextFormatter.stringToFormattedText(
                        "&cThat message was blocked by server chat policy."));
                return false;
            }
            if (response.equals("mute_proposal") || response.equals("quarantine_proposal")) {
                createAutomodProposal(player, response, rule);
            }
        }
        return true;
    }

    public static void negotiate(PlayerNegotiationEvent event) {
        MinecraftServer server =
                net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        ServerControlRepository.ControlRecord admission = latestEffective("admission").orElse(null);
        int maximum = admission == null
                ? server.getMaxPlayers()
                : (int) number(admission, "maximum_players", server.getMaxPlayers());
        int ordinaryMaximum = ordinaryAdmissionMaximum(admission, maximum);
        if (maximum <= 0
                || exempt(event.getProfile().getId(), "admission")
                || exempt(event.getProfile().getId(), "queue")
                || ordinaryPlayerCount(server) + releasedAdmissionCount() < ordinaryMaximum) {
            return;
        }

        ServerControlRepository.ControlRecord queuePolicy = latestEffective("queue").orElse(null);
        String denial = admission == null
                ? "The server is currently full."
                : field(
                        admission,
                        "denial_message",
                        "The server is currently at its admission limit.");
        if (queuePolicy == null
                || !field(queuePolicy, "mode", "deny_retry").equals("native_wait")) {
            event.getConnection().disconnect(Component.literal(denial));
            return;
        }

        int maximumEntries = (int) number(queuePolicy, "maximum_entries", 100L);
        int maximumWaitSeconds = (int) number(queuePolicy, "maximum_wait_seconds", 900L);
        String status = field(
                queuePolicy,
                "status_message",
                "The server is full. Your connection is waiting in the admission queue.");
        CompletableFuture<Void> gate = new CompletableFuture<>();
        AdmissionQueueEntry queued = new AdmissionQueueEntry(
                event.getProfile(),
                event.getConnection(),
                gate,
                Instant.now(),
                Instant.now().plusSeconds(maximumWaitSeconds),
                status);
        synchronized (MinecraftServerControlRuntime.class) {
            ADMISSION_QUEUE.removeIf(entry -> {
                if (!entry.profile().getId().equals(event.getProfile().getId())) {
                    return false;
                }
                entry.connection().disconnect(Component.literal(
                        "A newer connection replaced this queued login."));
                entry.gate().complete(null);
                return true;
            });
            if (ADMISSION_QUEUE.size() >= maximumEntries) {
                event.getConnection().disconnect(Component.literal(
                        denial + " The admission queue is full."));
                return;
            }
            ADMISSION_QUEUE.addLast(queued);
            ServerEssentialsForge.LOGGER.info(
                    "[SEF] Queued login for {} at admission position {}",
                    event.getProfile().getId(),
                    ADMISSION_QUEUE.size());
        }
        event.enqueueWork(gate);
    }

    public static boolean allowsFullServerNegotiation(UUID playerId) {
        if (playerId == null) {
            return false;
        }
        if (exempt(playerId, "admission") || exempt(playerId, "queue")) {
            return true;
        }
        ServerControlRepository.ControlRecord queuePolicy =
                latestEffective("queue").orElse(null);
        return queuePolicy != null
                && field(queuePolicy, "mode", "deny_retry").equals("native_wait");
    }

    public static void login(ServerPlayer player) {
        synchronized (MinecraftServerControlRuntime.class) {
            RELEASED_ADMISSIONS.remove(player.getUUID());
            ADMISSION_QUEUE.removeIf(entry -> entry.profile().getId().equals(player.getUUID()));
        }
        LAST_CHAT.remove(player.getUUID());
        ServerControlRepository.ControlRecord maintenance = latestEffective("maintenance").orElse(null);
        if (maintenance != null
                && Boolean.parseBoolean(field(maintenance, "deny_login", "true"))
                && !exempt(player, "maintenance")) {
            player.connection.disconnect(Component.literal(field(
                    maintenance,
                    "message",
                    "The server is in maintenance mode.")));
            return;
        }
        ServerControlRepository.ControlRecord admission = latestEffective("admission").orElse(null);
        if (admission != null) {
            boolean reserved = exempt(player, "admission");
            int maximum = (int) number(admission, "maximum_players", 0L);
            int ordinaryMaximum = ordinaryAdmissionMaximum(admission, maximum);
            long now = Instant.now().getEpochSecond();
            int joinsPerMinute = (int) number(admission, "joins_per_minute", 100_000L);
            boolean joinRateExceeded;
            synchronized (MinecraftServerControlRuntime.class) {
                while (!JOIN_WINDOW.isEmpty() && JOIN_WINDOW.peekFirst() <= now - 60L) {
                    JOIN_WINDOW.removeFirst();
                }
                joinRateExceeded = JOIN_WINDOW.size() >= joinsPerMinute;
                if (!joinRateExceeded || reserved) {
                    JOIN_WINDOW.addLast(now);
                }
            }
            if (!reserved && (joinRateExceeded
                    || ordinaryMaximum > 0
                    && ordinaryPlayerCount(player.server) > ordinaryMaximum)) {
                player.connection.disconnect(Component.literal(field(
                        admission,
                        "denial_message",
                        "The server is currently at its admission limit.")));
                return;
            }
        }
        if (pendingRules(player).isPresent()) {
            player.sendSystemMessage(TextFormatter.stringToFormattedText(
                    "&eThe server rules require acceptance. Read them with &f/rules&e."));
        }
    }

    public static synchronized void logout(UUID playerId) {
        LAST_CHAT.remove(playerId);
        LAST_MAINTENANCE_NOTICE.remove(playerId);
        LAST_GUARDRAIL_WARNING.remove(playerId);
        COMMAND_WINDOWS.remove(playerId);
        QUARANTINE_ANCHORS.remove(playerId);
    }

    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % 20 != 0) {
            return;
        }
        refreshPerformanceSnapshot(server);
        maintenanceTick(server);
        cleanupTick(server);
        quarantineTick(server);
        restartTick(server);
        sleepVoteTick(server);
        admissionQueueTick(server);
    }

    public static synchronized void clear() {
        LAST_CHAT.clear();
        LAST_MAINTENANCE_NOTICE.clear();
        LAST_CLEANUP.clear();
        COMMAND_WINDOWS.clear();
        QUARANTINE_ANCHORS.clear();
        RESTART_WARNINGS.clear();
        LAST_PERFORMANCE_QUERY.clear();
        JOIN_WINDOW.clear();
        ADMISSION_QUEUE.forEach(entry -> {
            entry.connection().disconnect(Component.literal(
                    "The admission queue was stopped by a server configuration change."));
            entry.gate().complete(null);
        });
        ADMISSION_QUEUE.clear();
        RELEASED_ADMISSIONS.clear();
        performanceSnapshot = null;
        lastGlobalPerformanceQuery = 0L;
        cachedRevision = -1L;
        active = Map.of();
    }

    public static synchronized int admissionQueueSize() {
        return ADMISSION_QUEUE.size();
    }

    private static void admissionQueueTick(MinecraftServer server) {
        ServerControlRepository.ControlRecord admission = latestEffective("admission").orElse(null);
        ServerControlRepository.ControlRecord queuePolicy = latestEffective("queue").orElse(null);
        int maximum = admission == null
                ? server.getMaxPlayers()
                : (int) number(admission, "maximum_players", server.getMaxPlayers());
        int ordinaryMaximum = ordinaryAdmissionMaximum(admission, maximum);
        boolean activeQueue = maximum > 0
                && queuePolicy != null
                && field(queuePolicy, "mode", "deny_retry").equals("native_wait");
        Instant now = Instant.now();
        synchronized (MinecraftServerControlRuntime.class) {
            RELEASED_ADMISSIONS.entrySet().removeIf(entry ->
                    server.getPlayerList().getPlayer(entry.getKey()) != null
                            || !entry.getValue().isAfter(now));
            ADMISSION_QUEUE.removeIf(entry -> {
                if (!entry.connection().isConnected()) {
                    entry.gate().complete(null);
                    return true;
                }
                if (!activeQueue) {
                    entry.connection().disconnect(Component.literal(
                            entry.status() + " The admission queue is no longer active."));
                    entry.gate().complete(null);
                    return true;
                }
                if (!entry.expiresAt().isAfter(now)) {
                    int position = queuePosition(entry.profile().getId());
                    entry.connection().disconnect(Component.literal(
                            entry.status() + " The wait expired at position "
                                    + position + ". Please reconnect."));
                    entry.gate().complete(null);
                    return true;
                }
                return false;
            });
            int available = Math.max(
                    0,
                    ordinaryMaximum
                            - ordinaryPlayerCount(server)
                            - RELEASED_ADMISSIONS.size());
            while (available > 0 && !ADMISSION_QUEUE.isEmpty()) {
                AdmissionQueueEntry next = ADMISSION_QUEUE.removeFirst();
                if (!next.connection().isConnected()) {
                    next.gate().complete(null);
                    continue;
                }
                RELEASED_ADMISSIONS.put(
                        next.profile().getId(),
                        now.plusSeconds(30L));
                next.gate().complete(null);
                available--;
            }
        }
    }

    private static int queuePosition(UUID playerId) {
        int position = 1;
        for (AdmissionQueueEntry entry : ADMISSION_QUEUE) {
            if (entry.profile().getId().equals(playerId)) {
                return position;
            }
            position++;
        }
        return 0;
    }

    private static synchronized int releasedAdmissionCount() {
        return RELEASED_ADMISSIONS.size();
    }

    private static int ordinaryAdmissionMaximum(
            ServerControlRepository.ControlRecord admission,
            int maximum
    ) {
        if (maximum <= 0 || admission == null) {
            return maximum;
        }
        return ordinaryAdmissionMaximum(
                maximum,
                (int) number(admission, "reserved_slots", 0L));
    }

    static int ordinaryAdmissionMaximum(int maximum, int reserved) {
        if (maximum <= 0) {
            return maximum;
        }
        return maximum - Math.min(maximum, Math.max(0, reserved));
    }

    private static int ordinaryPlayerCount(MinecraftServer server) {
        return (int) server.getPlayerList().getPlayers().stream()
                .filter(player -> !exempt(player, "admission") && !exempt(player, "queue"))
                .count();
    }

    public static Optional<ServerControlRepository.ControlRecord> effectivePolicy(String featureId) {
        return latestEffective(featureId);
    }

    public static ActionResult<Void> authorizeAction(
            net.minecraft.commands.CommandSourceStack source,
            CommandDefinition definition
    ) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(definition, "definition");
        ServerPlayer player = source.getPlayer();
        if (player != null) {
            ActionResult<Void> lockDecision = KernelServices.adminLocks().authorize(
                    player.getUUID(),
                    definition,
                    recoveryAction(definition.id()));
            if (!lockDecision.successful()) {
                return lockDecision;
            }
            ServerControlRepository.ControlRecord lock = latestEffective("admin_lock").orElse(null);
            if (lock != null
                    && (lock.subjectId() == null || lock.subjectId().equals(player.getUUID()))
                    && field(lock, "mode", "unlocked").equals("locked")
                    && !recoveryAction(definition.id())) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.POLICY_DENIED,
                        "privileged administration is locked for this account");
            }
        }
        Instant now = Instant.now();
        for (ServerControlRepository.ControlRecord window : active("change_windows")) {
            Instant opens = instant(window, "opens_at").orElse(null);
            long duration = number(window, "duration_seconds", 0L);
            if (opens == null || duration < 1L || !matchesActionList(window, definition.id())) {
                continue;
            }
            Instant closes;
            try {
                closes = opens.plusSeconds(duration);
            } catch (RuntimeException exception) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.INVALID_DEFINITION,
                        "change window time is invalid");
            }
            if (now.isBefore(opens) || !now.isBefore(closes)) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.POLICY_DENIED,
                        "that action is outside its approved change window");
            }
        }
        if (definition.accessClass().ordinal() >= CommandDefinition.AccessClass.ADMINISTRATOR.ordinal()) {
            for (ServerControlRepository.ControlRecord guardrail : active("guardrails")) {
                String response = field(guardrail, "response", "warn");
                double signal = guardrailSignal(
                        source.getServer(),
                        field(guardrail, "metric", "players"),
                        number(guardrail, "window_seconds", 60L));
                double threshold;
                try {
                    threshold = Double.parseDouble(field(guardrail, "threshold", "0"));
                } catch (NumberFormatException exception) {
                    return ActionResult.failure(
                            ActionResult.ReasonCode.INVALID_DEFINITION,
                            "guardrail threshold is invalid");
                }
                if (Double.isFinite(signal) && signal >= threshold) {
                    if (response.equals("warn")) {
                        if (player != null && shouldWarnGuardrail(player.getUUID())) {
                            player.sendSystemMessage(TextFormatter.stringToFormattedText(
                                    "&eAn operational guardrail threshold is active."));
                        }
                        continue;
                    }
                    return ActionResult.failure(
                            ActionResult.ReasonCode.POLICY_DENIED,
                            response.equals("rollback")
                                    ? "an operational guardrail requires rollback before that action"
                                    : response.equals("pause")
                                    ? "an operational guardrail paused administrative actions"
                                    : "an operational guardrail denied that administrative action");
                }
            }
        }
        return ActionResult.success(null);
    }

    private static boolean recoveryAction(String actionId) {
        return actionId.startsWith("sef:control.admin_lock")
                || actionId.startsWith("sef:config.status")
                || actionId.startsWith("sef:control.status")
                || actionId.equals("sef:core.help");
    }

    private static boolean matchesActionList(
            ServerControlRepository.ControlRecord record,
            String actionId
    ) {
        return List.of(field(record, "allowed_actions", "").split("[,|\\n]", -1)).stream()
                .map(String::strip)
                .filter(value -> !value.isBlank())
                .anyMatch(value -> value.endsWith("*")
                        ? actionId.startsWith(value.substring(0, value.length() - 1))
                        : actionId.equals(value));
    }

    private static double guardrailSignal(MinecraftServer server, String metric, long windowSeconds) {
        if (metric.equals("commands")) {
            long cutoff = System.currentTimeMillis() - Math.max(1L, windowSeconds) * 1000L;
            synchronized (MinecraftServerControlRuntime.class) {
                long count = 0L;
                for (Deque<Long> window : COMMAND_WINDOWS.values()) {
                    while (!window.isEmpty() && window.peekFirst() < cutoff) {
                        window.removeFirst();
                    }
                    count = Math.min(1_000_001L, count + window.size());
                }
                return count;
            }
        }
        GuardrailSnapshot snapshot = guardrailSnapshot(server);
        return switch (metric) {
            case "players" -> snapshot.players();
            case "memory" -> snapshot.usedMemory();
            case "entities" -> snapshot.entities();
            case "tick_time" -> snapshot.averageTickMillis();
            default -> Double.NaN;
        };
    }

    private static synchronized GuardrailSnapshot guardrailSnapshot(MinecraftServer server) {
        long now = System.currentTimeMillis();
        if (guardrailSnapshot != null && now - guardrailSnapshot.capturedAtMillis() < 1_000L) {
            return guardrailSnapshot;
        }
        long entities = 0L;
        outer:
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity ignored : level.getAllEntities()) {
                entities++;
                if (entities >= 1_000_001L) {
                    break outer;
                }
            }
        }
        guardrailSnapshot = new GuardrailSnapshot(
                now,
                server.getPlayerCount(),
                Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(),
                entities,
                server.getAverageTickTimeNanos() / 1_000_000.0D);
        return guardrailSnapshot;
    }

    private static synchronized boolean shouldWarnGuardrail(UUID playerId) {
        long now = System.currentTimeMillis();
        long previous = LAST_GUARDRAIL_WARNING.getOrDefault(playerId, 0L);
        if (now - previous < 30_000L) {
            return false;
        }
        boundedPut(LAST_GUARDRAIL_WARNING, playerId, now, 4096);
        return true;
    }

    public static List<ControlHudStatus> hudStatuses(ServerPlayer player) {
        List<ControlHudStatus> result = new ArrayList<>();
        for (ServerControlSchemaRegistry.FeatureSchema schema :
                ServerControlSchemaRegistry.schemas()) {
            if (schema.hud() == ServerControlSchemaRegistry.HudPolicy.NONE) {
                continue;
            }
            var permission = PermissionsHandler.phasePermission(
                    "commands.control." + schema.featureId() + ".hud");
            if (permission == null || !PermissionService.has(player, permission)) {
                continue;
            }
            for (ServerControlRepository.ControlRecord record : active(schema.featureId())) {
                boolean relevant = schema.hud() == ServerControlSchemaRegistry.HudPolicy.REQUIRED
                        || record.subjectId() == null
                        || record.subjectId().equals(player.getUUID())
                        || record.ownerId().equals(player.getUUID());
                if (!relevant) {
                    continue;
                }
                int progress = hudProgress(record);
                SefPayloads.Severity severity = ServerControlCatalog.require(record.featureId()).dangerous()
                        ? SefPayloads.Severity.WARNING
                        : record.subjectId() != null && record.subjectId().equals(player.getUUID())
                        ? SefPayloads.Severity.NOTICE
                        : SefPayloads.Severity.INFO;
                result.add(new ControlHudStatus(
                        "control_" + record.featureId(),
                        ServerControlCatalog.require(record.featureId()).title() + " active",
                        severity,
                        progress,
                        record.updatedAt()));
                break;
            }
        }
        var leaseHudPermission = PermissionsHandler.phasePermission("commands.control.capability_leases.hud");
        var leaseInspectPermission = PermissionsHandler.phasePermission("commands.accessgrant.inspect.self");
        if ((leaseHudPermission != null && PermissionService.has(player, leaseHudPermission))
                || leaseInspectPermission != null && PermissionService.has(player, leaseInspectPermission)) {
            KernelServices.accessLeases()
                    .leases(player.getUUID(), null, AccessLeaseRepository.LeaseState.ACTIVE)
                    .stream()
                    .min(Comparator.comparing(AccessLeaseRepository.Lease::expiresAt))
                    .ifPresent(lease -> {
                        long remaining = Math.max(0L, Duration.between(Instant.now(), lease.expiresAt()).toSeconds());
                        long total = Math.max(1L, Duration.between(lease.startsAt(), lease.expiresAt()).toSeconds());
                        result.add(new ControlHudStatus(
                                "access_lease",
                                "Access lease expires in " + remaining + " seconds",
                                remaining <= 300L ? SefPayloads.Severity.WARNING : SefPayloads.Severity.NOTICE,
                                (int) Math.clamp(remaining * 100L / total, 0L, 100L),
                                lease.updatedAt()));
                    });
        }
        var lockStatusPermission = PermissionsHandler.phasePermission("commands.adminlock.status.self");
        if (lockStatusPermission != null && PermissionService.has(player, lockStatusPermission)) {
            AdminLockService.Status status = KernelServices.adminLocks().status(player.getUUID());
            if (status.locked()) {
                Instant updated = KernelServices.adminLockRepository().lock(player.getUUID())
                        .map(AdminLockRepository.AccountLock::updatedAt)
                        .orElse(Instant.now());
                result.add(new ControlHudStatus(
                        "admin_lock",
                        "Administrative actions locked",
                        SefPayloads.Severity.WARNING,
                        100,
                        updated));
            } else {
                status.session().ifPresent(session -> {
                    long remaining = Math.max(
                            0L,
                            Duration.between(Instant.now(), session.expiresAt()).toSeconds());
                    long total = Math.max(
                            1L,
                            Duration.between(session.openedAt(), session.expiresAt()).toSeconds());
                    result.add(new ControlHudStatus(
                            "admin_session",
                            "Privileged session expires in " + remaining + " seconds",
                            remaining <= 60L ? SefPayloads.Severity.WARNING : SefPayloads.Severity.NOTICE,
                            (int) Math.clamp(remaining * 100L / total, 0L, 100L),
                            session.openedAt()));
                });
            }
        }
        return result.stream()
                .sorted(Comparator.comparing(ControlHudStatus::severity).reversed()
                        .thenComparing(
                                ControlHudStatus::updatedAt,
                                Comparator.reverseOrder())
                        .thenComparing(ControlHudStatus::id))
                .toList();
    }

    private static int hudProgress(ServerControlRepository.ControlRecord record) {
        Instant destination = instant(record, "restart_at")
                .or(() -> instant(record, "reset_at"))
                .or(() -> instant(record, "starts_at"))
                .orElse(null);
        if (destination == null) {
            return 0;
        }
        long seconds = Math.max(0L, Duration.between(Instant.now(), destination).toSeconds());
        long window = Math.max(1L, number(
                record,
                "duration_seconds",
                number(record, "stage_seconds", Math.max(1L, seconds))));
        return (int) Math.clamp(100L - seconds * 100L / window, 0L, 100L);
    }

    private static void maintenanceTick(MinecraftServer server) {
        ServerControlRepository.ControlRecord maintenance = latestEffective("maintenance").orElse(null);
        if (maintenance == null) {
            return;
        }
        long interval = Math.max(5L, number(maintenance, "reminder_seconds", 60L));
        long now = Instant.now().getEpochSecond();
        String message = field(maintenance, "message", "The server is in maintenance mode.");
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (exempt(player, "maintenance")) {
                continue;
            }
            long previous;
            synchronized (MinecraftServerControlRuntime.class) {
                previous = LAST_MAINTENANCE_NOTICE.getOrDefault(player.getUUID(), 0L);
                if (now - previous >= interval) {
                    boundedPut(LAST_MAINTENANCE_NOTICE, player.getUUID(), now, 4096);
                }
            }
            if (now - previous >= interval) {
                player.sendSystemMessage(TextFormatter.stringToFormattedText("&e" + message));
            }
        }
    }

    private static void sleepVoteTick(MinecraftServer server) {
        ServerControlRepository.ControlRecord policy = latestEffective("sleep_vote").orElse(null);
        if (policy == null) {
            return;
        }
        ServerLevel level = server.overworld();
        if (!level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            return;
        }
        if (!level.isNight()) {
            clearSleepVotes(policy.id());
            return;
        }
        SleepVoteSnapshot snapshot = sleepVoteSnapshot(server, policy);
        if (snapshot.eligible() == 0) {
            return;
        }
        if (snapshot.yes() < snapshot.required()) {
            return;
        }
        long accelerationSeconds = Math.max(1L, number(policy, "acceleration_seconds", 1L));
        long dayTime = level.getDayTime();
        long untilNextDay = 24_000L - Math.floorMod(dayTime, 24_000L);
        long advance = Math.max(1L, (12_000L + accelerationSeconds - 1L) / accelerationSeconds);
        long applied = Math.min(untilNextDay, advance);
        level.setDayTime(dayTime > Long.MAX_VALUE - applied ? applied : dayTime + applied);
        if (applied >= untilNextDay) {
            if (Boolean.parseBoolean(field(policy, "clear_weather", "true"))) {
                level.setWeatherParameters(0, 0, false, false);
            }
            clearSleepVotes(policy.id());
            broadcast(server, "&aThe sleep vote passed. Morning has arrived.");
        }
    }

    public static SleepVoteSnapshot sleepVoteSnapshot(
            MinecraftServer server,
            ServerControlRepository.ControlRecord policy
    ) {
        ServerLevel level = server.overworld();
        boolean ignoreAfk = Boolean.parseBoolean(field(policy, "ignore_afk", "true"));
        List<ServerPlayer> eligible = server.getPlayerList().getPlayers().stream()
                .filter(player -> player.level() == level)
                .filter(player -> !player.isSpectator())
                .filter(player -> !VanishUtil.isVanished(player))
                .filter(player -> !ignoreAfk || !PlayerStateService.afk(player.getUUID()))
                .filter(player -> KernelServices.moderation().sentence(player.getUUID()).isEmpty())
                .filter(player -> quarantine(player, "all").isEmpty())
                .filter(player -> quarantine(player, "movement").isEmpty())
                .toList();
        long yes = eligible.stream().filter(player -> KernelServices.communityState().find(
                        "sleep_vote",
                        player.getUUID(),
                        policy.id().toString())
                .map(entry -> Boolean.parseBoolean(entry.value()))
                .orElse(false)).count();
        long required = Math.max(1L, (eligible.size()
                * number(policy, "required_percent", 100L) + 99L) / 100L);
        return new SleepVoteSnapshot(eligible.size(), yes, required);
    }

    private static void clearSleepVotes(UUID policyId) {
        KernelServices.communityState().entries("sleep_vote").stream()
                .filter(entry -> entry.key().equals(policyId.toString()))
                .limit(10_000)
                .forEach(entry -> KernelServices.communityState().remove(
                        "sleep_vote",
                        entry.ownerId(),
                        entry.key()));
    }

    private static void quarantineTick(MinecraftServer server) {
        Set<UUID> constrained = new HashSet<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (quarantine(player, "movement").isEmpty() || exempt(player, "session_quarantine")) {
                continue;
            }
            constrained.add(player.getUUID());
            QuarantineAnchor anchor;
            synchronized (MinecraftServerControlRuntime.class) {
                anchor = QUARANTINE_ANCHORS.computeIfAbsent(
                        player.getUUID(),
                        ignored -> new QuarantineAnchor(
                                player.level().dimension().location().toString(),
                                player.getX(),
                                player.getY(),
                                player.getZ(),
                                player.getYRot(),
                                player.getXRot()));
            }
            if (anchor.world().equals(player.level().dimension().location().toString())
                    && player.distanceToSqr(anchor.x(), anchor.y(), anchor.z()) > 0.01D) {
                player.teleportTo(anchor.x(), anchor.y(), anchor.z());
                player.setYRot(anchor.yaw());
                player.setXRot(anchor.pitch());
                player.setDeltaMovement(0.0D, 0.0D, 0.0D);
            }
        }
        synchronized (MinecraftServerControlRuntime.class) {
            QUARANTINE_ANCHORS.keySet().removeIf(playerId -> !constrained.contains(playerId));
        }
    }

    private static void restartTick(MinecraftServer server) {
        long now = Instant.now().getEpochSecond();
        for (ServerControlRepository.ControlRecord restart : active("restart_coordinator")) {
            Instant restartAt = instant(restart, "restart_at").orElse(null);
            if (restartAt == null) {
                continue;
            }
            long remaining = restartAt.getEpochSecond() - now;
            Set<Long> warnings = warningSeconds(restart);
            synchronized (MinecraftServerControlRuntime.class) {
                Set<Long> sent = RESTART_WARNINGS.computeIfAbsent(restart.id(), ignored -> new HashSet<>());
                for (long warning : warnings) {
                    if (remaining <= warning && remaining > 0L && sent.add(warning)) {
                        broadcast(server, field(restart, "message", "Server restart in {seconds} seconds.")
                                .replace("{seconds}", Long.toString(remaining)));
                    }
                }
            }
            if (remaining > 0L) {
                continue;
            }
            if (Boolean.parseBoolean(field(restart, "save_worlds", "true"))) {
                server.saveEverything(true, true, true);
            }
            KernelServices.serverControls().transition(
                    restart.id(),
                    new UUID(0L, 0L),
                    ServerControlRepository.RecordState.RESOLVED,
                    restart.revision(),
                    "scheduled restart started");
            broadcast(server, field(restart, "message", "Server restarting now.")
                    .replace("{seconds}", "0"));
            server.halt(false);
            return;
        }
    }

    private static void cleanupTick(MinecraftServer server) {
        long now = Instant.now().getEpochSecond();
        for (ServerControlRepository.ControlRecord cleanup : active("cleanup")) {
            long interval = number(cleanup, "interval_seconds", 300L);
            long previous;
            synchronized (MinecraftServerControlRuntime.class) {
                previous = LAST_CLEANUP.getOrDefault(cleanup.id(), 0L);
                if (now - previous >= interval) {
                    boundedPut(LAST_CLEANUP, cleanup.id(), now, 4096);
                }
            }
            if (now - previous >= interval) {
                performCleanup(server, cleanup);
            }
        }
    }

    private static ActionResult<String> worldBorder(
            ServerControlRepository.ControlRecord record,
            ServerControlExecutionService.ExecutionContext context
    ) {
        MinecraftServer server = server(context);
        if (server == null) {
            return ActionResult.failure(ActionResult.ReasonCode.SOURCE_NOT_ALLOWED, "server source is unavailable");
        }
        ServerLevel level = level(server, field(record, "world", ""));
        if (level == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "world is unavailable");
        }
        double centerX = decimal(record, "center_x", 0.0D);
        double centerZ = decimal(record, "center_z", 0.0D);
        double size = decimal(record, "size", 1.0D);
        long seconds = number(record, "transition_seconds", 0L);
        var border = level.getWorldBorder();
        border.setCenter(centerX, centerZ);
        if (seconds == 0L) {
            border.setSize(size);
        } else {
            border.lerpSizeBetween(border.getSize(), size, Math.multiplyExact(seconds, 1000L));
        }
        return ActionResult.success("world border profile applied");
    }

    private static ActionResult<String> activatePolicy(
            ServerControlRepository.ControlRecord record,
            ServerControlExecutionService.ExecutionContext context
    ) {
        if (server(context) == null) {
            return ActionResult.failure(ActionResult.ReasonCode.SOURCE_NOT_ALLOWED, "server source is unavailable");
        }
        if (record.featureId().equals("automod")
                && field(record, "matcher", "literal").equals("regex_adapter")) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.PROVIDER_ERROR,
                    "the configured safe regular expression adapter is unavailable");
        }
        return ActionResult.success(record.featureId() + " policy activated");
    }

    private static ActionResult<String> resolveReview(
            ServerControlRepository.ControlRecord record,
            ServerControlExecutionService.ExecutionContext context
    ) {
        if (context.source() == null) {
            return ActionResult.failure(ActionResult.ReasonCode.SOURCE_NOT_ALLOWED, "review source is unavailable");
        }
        return ActionResult.success(record.featureId() + " record resolved");
    }

    private static ActionResult<String> relationship(
            ServerControlRepository.ControlRecord record,
            ServerControlExecutionService.ExecutionContext context
    ) {
        UUID owner = record.ownerId();
        UUID target;
        try {
            target = UUID.fromString(field(record, "player", ""));
        } catch (IllegalArgumentException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "relationship player is invalid");
        }
        if (owner.equals(target)) {
            return ActionResult.failure(ActionResult.ReasonCode.TARGET_DENIED, "relationship target is self");
        }
        String relationship = field(record, "relationship", "friend");
        Instant expires = instant(record, "expires_at").orElse(null);
        KernelServices.communityState().putAtomically(List.of(
                new CommunityStateRepository.Write(
                        "friend",
                        owner,
                        target,
                        target.toString(),
                        relationship,
                        expires),
                new CommunityStateRepository.Write(
                        "friend",
                        target,
                        owner,
                        owner.toString(),
                        relationship,
                        expires)));
        return ActionResult.success(relationship + " relationship published");
    }

    private static ActionResult<String> escrow(
            ServerControlRepository.ControlRecord record,
            ServerControlExecutionService.ExecutionContext context
    ) {
        return KernelServices.escrow().execute(record, context);
    }

    private static ActionResult<String> evidence(
            ServerControlRepository.ControlRecord record,
            ServerControlExecutionService.ExecutionContext context
    ) {
        if (!Boolean.parseBoolean(field(record, "sealed", "true"))) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "evidence must be sealed");
        }
        KernelServices.communityState().put(
                "sealed_evidence",
                record.ownerId(),
                record.subjectId(),
                record.id().toString(),
                field(record, "case", "") + "\n"
                        + field(record, "source_type", "") + "\n"
                        + field(record, "source_reference", ""),
                record.expiresAt());
        return ActionResult.success("sealed evidence reference committed");
    }

    private static ActionResult<String> externalProvider(
            ServerControlRepository.ControlRecord record,
            ServerControlExecutionService.ExecutionContext context
    ) {
        String provider = field(record, "provider", "");
        if (provider.isBlank() || !net.neoforged.fml.ModList.get().isLoaded(provider)) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.PROVIDER_ERROR,
                    "configured provider is not installed");
        }
        return ActionResult.failure(
                ActionResult.ReasonCode.PROVIDER_ERROR,
                "configured provider has no published sef adapter");
    }

    private static ActionResult<String> policyLab(
            ServerControlRepository.ControlRecord record,
            ServerControlExecutionService.ExecutionContext context
    ) {
        String candidate = field(record, "candidate", "");
        String scope = field(record, "scope", "server");
        boolean catalogAction = scope.equals("command") && KernelServices.catalog().find(candidate).isPresent();
        return ActionResult.success("policy simulation for " + scope
                + ", candidate " + candidate
                + ", catalog action " + catalogAction
                + ", no state changed");
    }

    private static ActionResult<String> configDrift(
            ServerControlRepository.ControlRecord record,
            ServerControlExecutionService.ExecutionContext context
    ) {
        List<String> drift = KernelServices.restartRequiredConfigurationDrift();
        if (Boolean.parseBoolean(field(record, "auto_repair", "false")) && !drift.isEmpty()) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.POLICY_DENIED,
                    "automatic repair is unavailable for restart required configuration");
        }
        return ActionResult.success(drift.isEmpty()
                ? "no restart required configuration drift"
                : "configuration drift, " + String.join(", ", drift));
    }

    private static ActionResult<String> permissionImpact(
            ServerControlRepository.ControlRecord record,
            ServerControlExecutionService.ExecutionContext context
    ) {
        String permission = field(record, "permission", "");
        var capability = KernelServices.capabilities().get(permission);
        return capability == null
                ? ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "permission is not in the capability manifest")
                : ActionResult.success("permission " + permission
                + ", default " + capability.defaultAllowed()
                + ", operation " + field(record, "operation", "remove")
                + ", no provider state changed");
    }

    private static ActionResult<String> dependencyGraph(
            ServerControlRepository.ControlRecord record,
            ServerControlExecutionService.ExecutionContext context
    ) {
        String feature = field(record, "feature", "");
        boolean command = KernelServices.catalog().find(feature).isPresent();
        boolean control = ServerControlCatalog.BY_ID.containsKey(feature);
        boolean module = KernelServices.moduleConfigs().modules().stream()
                .anyMatch(snapshot -> snapshot.moduleId().equals(feature));
        return ActionResult.success("dependency lookup " + feature
                + ", command " + command
                + ", control " + control
                + ", module " + module);
    }

    private static ActionResult<String> playerImpact(
            ServerControlRepository.ControlRecord record,
            ServerControlExecutionService.ExecutionContext context
    ) {
        String targets = field(record, "targets", "");
        int count = targets.isBlank() ? 0 : targets.split(",", -1).length;
        return ActionResult.success("player impact preview for " + field(record, "action", "")
                + ", declared targets " + count
                + ", no state changed");
    }

    private static ActionResult<String> operationalSnapshot(
            ServerControlRepository.ControlRecord record,
            ServerControlExecutionService.ExecutionContext context
    ) {
        MinecraftServer server = server(context);
        if (server == null) {
            return ActionResult.failure(ActionResult.ReasonCode.SOURCE_NOT_ALLOWED, "server source is unavailable");
        }
        var control = KernelServices.serverControls().diagnostic();
        return ActionResult.success("snapshot " + field(record, "label", "")
                + ", players " + server.getPlayerList().getPlayerCount()
                + ", control records " + control.records()
                + ", active " + control.activeRecords()
                + ", repository revision " + control.revision());
    }

    private static ActionResult<String> aliasDiagnostics(
            ServerControlRepository.ControlRecord record,
            ServerControlExecutionService.ExecutionContext context
    ) {
        return ActionResult.success("aliases " + KernelServices.aliases().published().size()
                + ", bundles " + KernelServices.bundles().publications().size()
                + ", panels " + KernelServices.adminPanels().panels().size()
                + ", scope " + field(record, "scope", "all"));
    }

    private static ActionResult<String> sleepVote(
            ServerControlRepository.ControlRecord record,
            ServerControlExecutionService.ExecutionContext context
    ) {
        MinecraftServer server = server(context);
        if (server == null) {
            return ActionResult.failure(ActionResult.ReasonCode.SOURCE_NOT_ALLOWED, "server source is unavailable");
        }
        long configuredPercentage = number(record, "required_percent", 100L);
        if (configuredPercentage < 1L || configuredPercentage > 100L) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.INVALID_INPUT,
                    "sleep vote threshold must be between 1 and 100 percent");
        }
        int percentage = (int) configuredPercentage;
        String command = "gamerule playersSleepingPercentage " + percentage;
        var source = server.createCommandSourceStack().withSuppressedOutput();
        var parsed = server.getCommands().getDispatcher().parse(command, source);
        if (parsed.getReader().canRead()
                || parsed.getContext().getCommand() == null
                || !parsed.getExceptions().isEmpty()) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.INVALID_INPUT,
                    "sleep vote threshold command is unavailable");
        }
        try {
            if (server.getCommands().getDispatcher().execute(parsed) < 0) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.PROVIDER_ERROR,
                        "sleep vote threshold could not be applied");
            }
        } catch (Exception exception) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.PROVIDER_ERROR,
                    "sleep vote threshold could not be applied");
        }
        return ActionResult.success("sleep vote threshold set to " + percentage + " percent");
    }

    private static ActionResult<String> worldPolicy(
            ServerControlRepository.ControlRecord record,
            ServerControlExecutionService.ExecutionContext context
    ) {
        MinecraftServer server = server(context);
        if (server == null) {
            return ActionResult.failure(ActionResult.ReasonCode.SOURCE_NOT_ALLOWED, "server source is unavailable");
        }
        ServerLevel level = level(server, field(record, "world", ""));
        if (level == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "world is unavailable");
        }
        String definitions = field(record, "gamerules", "");
        List<String> commands = new ArrayList<>();
        for (String definition : definitions.split("[,\\n]", -1)) {
            if (definition.isBlank()) {
                continue;
            }
            String[] pair = definition.strip().split("=", 2);
            if (pair.length != 2
                    || !SAFE_GAMERULE.matcher(pair[0].strip()).matches()
                    || !SAFE_GAMERULE_VALUE.matcher(pair[1].strip()).matches()) {
                return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "gamerule definition is invalid");
            }
            commands.add("gamerule " + pair[0].strip() + " " + pair[1].strip());
        }
        var source = server.createCommandSourceStack().withLevel(level).withSuppressedOutput();
        List<com.mojang.brigadier.ParseResults<CommandSourceStack>> parsedCommands = new ArrayList<>();
        for (String command : commands) {
            var parsed = server.getCommands().getDispatcher().parse(command, source);
            if (parsed.getReader().canRead()
                    || parsed.getContext().getCommand() == null
                    || !parsed.getExceptions().isEmpty()) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.INVALID_INPUT,
                        "gamerule definition is unavailable");
            }
            parsedCommands.add(parsed);
        }
        for (var parsed : parsedCommands) {
            try {
                if (server.getCommands().getDispatcher().execute(parsed) < 0) {
                    return ActionResult.failure(
                            ActionResult.ReasonCode.PROVIDER_ERROR,
                            "gamerule definition could not be applied");
                }
            } catch (Exception exception) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.PROVIDER_ERROR,
                        "gamerule definition could not be applied");
            }
        }
        return ActionResult.success("world policy applied " + commands.size() + " gamerules");
    }

    private static ActionResult<String> resourcePack(
            ServerControlRepository.ControlRecord record,
            ServerControlExecutionService.ExecutionContext context
    ) {
        MinecraftServer server = server(context);
        if (server == null) {
            return ActionResult.failure(ActionResult.ReasonCode.SOURCE_NOT_ALLOWED, "server source is unavailable");
        }
        String url = field(record, "url", "");
        String hash = field(record, "sha1", "");
        boolean required = Boolean.parseBoolean(field(record, "required", "false"));
        String prompt = field(record, "prompt", "");
        ClientboundResourcePackPushPacket packet = new ClientboundResourcePackPushPacket(
                UUID.nameUUIDFromBytes(("sef:resource_pack:" + record.id()).getBytes(StandardCharsets.UTF_8)),
                url,
                hash,
                required,
                prompt.isBlank() ? Optional.empty() : Optional.of(Component.literal(prompt)));
        server.getPlayerList().getPlayers().forEach(player -> player.connection.send(packet));
        return ActionResult.success("resource pack profile sent to "
                + server.getPlayerList().getPlayerCount() + " players");
    }

    private static ActionResult<String> cleanupAdmission(
            ServerControlRepository.ControlRecord record,
            ServerControlExecutionService.ExecutionContext context
    ) {
        MinecraftServer server = server(context);
        if (server == null) {
            return ActionResult.failure(ActionResult.ReasonCode.SOURCE_NOT_ALLOWED, "server source is unavailable");
        }
        int removed = performCleanup(server, record);
        return ActionResult.success("cleanup admitted and removed " + removed + " entities");
    }

    private static ActionResult<String> modHealth(
            ServerControlRepository.ControlRecord record,
            ServerControlExecutionService.ExecutionContext context
    ) {
        int mods = net.neoforged.fml.ModList.get().size();
        var integration = KernelServices.serverControlExecutions().diagnostic();
        return ActionResult.success("mods " + mods
                + ", handlers " + integration.registeredHandlers().size()
                + ", unavailable integrations " + integration.unavailableIntegrations().size());
    }

    private static ActionResult<String> performance(
            ServerControlRepository.ControlRecord record,
            ServerControlExecutionService.ExecutionContext context
    ) {
        MinecraftServer server = server(context);
        if (server == null) {
            return ActionResult.failure(ActionResult.ReasonCode.SOURCE_NOT_ALLOWED, "server source is unavailable");
        }
        UUID actorId = performanceActor(context);
        long now = System.currentTimeMillis();
        if (!performanceAllowed(actorId, now)) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.COOLDOWN_ACTIVE,
                    "performance snapshot requests are rate limited");
        }
        PerformanceSnapshot snapshot;
        synchronized (MinecraftServerControlRuntime.class) {
            snapshot = performanceSnapshot;
        }
        if (snapshot == null) {
            refreshPerformanceSnapshot(server);
            synchronized (MinecraftServerControlRuntime.class) {
                snapshot = performanceSnapshot;
            }
        }
        long entities = snapshot == null ? 0L : snapshot.entities();
        long chunks = snapshot == null ? 0L : snapshot.loadedChunks();
        long age = snapshot == null
                ? 0L
                : Math.max(0L, now - snapshot.createdAtEpochMillis());
        return ActionResult.success("average tick " + String.format(
                Locale.ROOT,
                "%.2f",
                server.getAverageTickTimeNanos() / 1_000_000.0D)
                + " ms, entities " + entities
                + ", loaded chunks " + chunks
                + ", snapshot age " + age + " ms"
                + (snapshot != null && snapshot.incompleteLevels() > 0
                ? ", entity counters unavailable for " + snapshot.incompleteLevels() + " levels"
                : ""));
    }

    static void refreshPerformanceSnapshot(MinecraftServer server) {
        long entities = 0L;
        long chunks = 0L;
        int levels = 0;
        int incompleteLevels = 0;
        for (ServerLevel level : server.getAllLevels()) {
            long exactEntityCount = level.getEntities().getAll()
                    .spliterator()
                    .getExactSizeIfKnown();
            if (exactEntityCount >= 0L) {
                entities += exactEntityCount;
            } else {
                incompleteLevels++;
            }
            chunks += level.getChunkSource().getLoadedChunksCount();
            levels++;
        }
        synchronized (MinecraftServerControlRuntime.class) {
            performanceSnapshot = new PerformanceSnapshot(
                    entities,
                    chunks,
                    levels,
                    incompleteLevels,
                    System.currentTimeMillis());
        }
    }

    static synchronized boolean performanceAllowed(UUID actorId, long nowEpochMillis) {
        Objects.requireNonNull(actorId, "actorId");
        if (nowEpochMillis < lastGlobalPerformanceQuery + 1_000L
                || nowEpochMillis < LAST_PERFORMANCE_QUERY.getOrDefault(actorId, 0L) + 5_000L) {
            return false;
        }
        lastGlobalPerformanceQuery = nowEpochMillis;
        boundedPut(LAST_PERFORMANCE_QUERY, actorId, nowEpochMillis, 4_096);
        return true;
    }

    private static UUID performanceActor(ServerControlExecutionService.ExecutionContext context) {
        if (context.source() instanceof CommandSourceStack source) {
            if (source.getEntity() != null) {
                return source.getEntity().getUUID();
            }
            return UUID.nameUUIDFromBytes(
                    ("sef:performance:" + source.getTextName()).getBytes(StandardCharsets.UTF_8));
        }
        Object source = context.source();
        return UUID.nameUUIDFromBytes(
                ("sef:performance:" + (source == null ? "unknown" : source.getClass().getName()))
                        .getBytes(StandardCharsets.UTF_8));
    }

    private static ActionResult<String> queue(
            ServerControlRepository.ControlRecord record,
            ServerControlExecutionService.ExecutionContext context
    ) {
        String mode = field(record, "mode", "deny_retry");
        if (mode.equals("proxy_adapter")) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.PROVIDER_ERROR,
                    "trusted proxy queue adapter is unavailable");
        }
        return ActionResult.success(
                "queue policy activated in " + mode + " mode, waiting "
                        + admissionQueueSize());
    }

    private static ActionResult<String> datapacks(
            ServerControlRepository.ControlRecord record,
            ServerControlExecutionService.ExecutionContext context
    ) {
        MinecraftServer server = server(context);
        if (server == null) {
            return ActionResult.failure(ActionResult.ReasonCode.SOURCE_NOT_ALLOWED, "server source is unavailable");
        }
        String operation = field(record, "operation", "scan");
        if (!operation.equals("scan") && !operation.equals("validate")) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.PROVIDER_ERROR,
                    "data pack staging provider is required for mutation");
        }
        boolean available = server.getPackRepository().getAvailablePacks().stream()
                .anyMatch(pack -> pack.getId().equals(field(record, "pack", "")));
        return available
                ? ActionResult.success("data pack is available and passed bounded discovery")
                : ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "data pack is unavailable");
    }

    private static ActionResult<String> chunkTickets(
            ServerControlRepository.ControlRecord record,
            ServerControlExecutionService.ExecutionContext context
    ) {
        MinecraftServer server = server(context);
        if (server == null) {
            return ActionResult.failure(ActionResult.ReasonCode.SOURCE_NOT_ALLOWED, "server source is unavailable");
        }
        long chunks = 0L;
        for (ServerLevel level : server.getAllLevels()) {
            chunks += level.getChunkSource().getLoadedChunksCount();
        }
        return ActionResult.success("loaded chunks " + chunks
                + ", release remains limited to sef owned tickets");
    }

    private static ActionResult<String> blockActivity(
            ServerControlRepository.ControlRecord record,
            ServerControlExecutionService.ExecutionContext context
    ) {
        return ActionResult.success("bounded block activity sampling session admitted");
    }

    private static int performCleanup(
            MinecraftServer server,
            ServerControlRepository.ControlRecord record
    ) {
        String targets = field(record, "targets", "items");
        int minimumTicks = Math.toIntExact(Math.min(
                Integer.MAX_VALUE,
                Math.multiplyExact(number(record, "minimum_age_seconds", 0L), 20L)));
        List<String> worlds = List.of(field(record, "worlds", "").split(",", -1));
        int removed = 0;
        for (ServerLevel level : server.getAllLevels()) {
            if (!worlds.getFirst().isBlank()
                    && !worlds.contains(level.dimension().location().toString())) {
                continue;
            }
            List<Entity> snapshot = new ArrayList<>();
            level.getEntities().getAll().forEach(snapshot::add);
            for (Entity entity : snapshot) {
                if (removed >= MAXIMUM_REMOVALS_PER_PASS) {
                    return removed;
                }
                if (entity instanceof ServerPlayer || entity.tickCount < minimumTicks) {
                    continue;
                }
                boolean selected = targets.equals("all")
                        || targets.equals("items") && entity instanceof ItemEntity
                        || targets.equals("experience") && entity instanceof ExperienceOrb
                        || targets.equals("mobs") && entity instanceof Mob
                        || targets.equals("projectiles") && entity instanceof Projectile;
                if (selected) {
                    entity.discard();
                    removed++;
                }
            }
        }
        return removed;
    }

    private static void createAutomodReport(
            ServerPlayer player,
            String message,
            ServerControlRepository.ControlRecord rule
    ) {
        if (KernelServices.serverControls().records("reports").size() >= 10_000) {
            return;
        }
        KernelServices.serverControls().create(
                "reports",
                new UUID(0L, 0L),
                player.getUUID(),
                "automod review",
                "",
                Instant.now().plus(Duration.ofDays(30)),
                Map.of(
                        "route", "automod",
                        "rule", rule.id().toString(),
                        "field.category", "chat",
                        "field.description", boundedMessage(message),
                        "field.priority", number(rule, "severity", 1L) >= 75L ? "high" : "normal"));
    }

    private static void createAutomodProposal(
            ServerPlayer player,
            String response,
            ServerControlRepository.ControlRecord rule
    ) {
        if (KernelServices.serverControls().records("discipline").size() >= 10_000) {
            return;
        }
        KernelServices.serverControls().create(
                "discipline",
                new UUID(0L, 0L),
                player.getUUID(),
                response.replace('_', ' '),
                "automod generated proposal",
                Instant.now().plus(Duration.ofDays(30)),
                Map.of(
                        "route", "automod",
                        "rule", rule.id().toString(),
                        "field.subject", player.getUUID().toString(),
                        "field.policy", response,
                        "field.points", Long.toString(number(rule, "severity", 1L))));
    }

    private static void createAutomaticQuarantine(ServerPlayer player, String reason) {
        if (quarantine(player, "commands").isPresent()
                || KernelServices.serverControls().records("session_quarantine").size() >= 10_000) {
            return;
        }
        long duration = 600L;
        ActionResult<ServerControlRepository.ControlRecord> created = KernelServices.serverControls().create(
                "session_quarantine",
                new UUID(0L, 0L),
                player.getUUID(),
                "automatic session quarantine",
                reason,
                Instant.now().plusSeconds(duration),
                Map.of(
                        "origin", "command_anomaly",
                        "field.subject", player.getUUID().toString(),
                        "field.reason", reason,
                        "field.duration_seconds", Long.toString(duration),
                        "field.scope", "commands"));
        if (created.successful()) {
            KernelServices.serverControls().transition(
                    created.value().id(),
                    new UUID(0L, 0L),
                    ServerControlRepository.RecordState.ACTIVE,
                    created.value().revision(),
                    "automatic command anomaly quarantine");
        }
    }

    private static Optional<ServerControlRepository.ControlRecord> quarantine(
            ServerPlayer player,
            String scope
    ) {
        return active("session_quarantine").stream()
                .filter(MinecraftServerControlRuntime::effective)
                .filter(record -> player.getUUID().equals(record.subjectId())
                        || player.getUUID().toString().equals(field(record, "subject", "")))
                .filter(record -> field(record, "scope", "all").equals("all")
                        || field(record, "scope", "all").equals(scope))
                .max(java.util.Comparator.comparing(ServerControlRepository.ControlRecord::updatedAt));
    }

    private static Optional<ServerControlRepository.ControlRecord> pendingRules(ServerPlayer player) {
        return latestEffective("rules")
                .filter(rules -> !CommunityCommands.acceptedRules(player, rules));
    }

    private static void alertStaff(MinecraftServer server, String message) {
        for (ServerPlayer observer : server.getPlayerList().getPlayers()) {
            if (has(observer, "commands.control.command_anomaly.manage")) {
                observer.sendSystemMessage(TextFormatter.stringToFormattedText(
                        "&8[&csef anomaly&8] &e" + message));
            }
        }
    }

    private static void broadcast(MinecraftServer server, String message) {
        Component component = TextFormatter.stringToFormattedText("&e" + boundedMessage(message));
        server.getPlayerList().getPlayers().forEach(player -> player.sendSystemMessage(component));
    }

    private static synchronized List<ServerControlRepository.ControlRecord> active(String featureId) {
        long revision = KernelServices.serverControls().diagnostic().revision();
        if (revision != cachedRevision) {
            Map<String, List<ServerControlRepository.ControlRecord>> replacement = new LinkedHashMap<>();
            int total = 0;
            for (ServerControlCatalog.FeatureDefinition feature : ServerControlCatalog.FEATURES) {
                List<ServerControlRepository.ControlRecord> records =
                        KernelServices.serverControls().records(feature.id()).stream()
                                .filter(record -> record.state() == ServerControlRepository.RecordState.ACTIVE)
                                .limit(Math.max(0, MAXIMUM_CACHED_ACTIVE_RECORDS - total))
                                .toList();
                total += records.size();
                if (!records.isEmpty()) {
                    replacement.put(feature.id(), records);
                }
                if (total >= MAXIMUM_CACHED_ACTIVE_RECORDS) {
                    break;
                }
            }
            active = Map.copyOf(replacement);
            cachedRevision = revision;
        }
        return active.getOrDefault(featureId, List.of());
    }

    private static Optional<ServerControlRepository.ControlRecord> latest(String featureId) {
        return active(featureId).stream()
                .max(java.util.Comparator.comparing(ServerControlRepository.ControlRecord::updatedAt));
    }

    private static Optional<ServerControlRepository.ControlRecord> latestEffective(String featureId) {
        return active(featureId).stream()
                .filter(MinecraftServerControlRuntime::effective)
                .max(java.util.Comparator.comparing(ServerControlRepository.ControlRecord::updatedAt));
    }

    private static boolean effective(ServerControlRepository.ControlRecord record) {
        Instant now = Instant.now();
        if (record.expiresAt() != null && !record.expiresAt().isAfter(now)) {
            return false;
        }
        Instant startsAt = instant(record, "starts_at").orElse(null);
        if (startsAt != null && startsAt.isAfter(now)) {
            return false;
        }
        long duration = number(record, "duration_seconds", 0L);
        if (startsAt != null && duration > 0L && !startsAt.plusSeconds(duration).isAfter(now)) {
            return false;
        }
        Instant restoresAt = instant(record, "restores_at").orElse(null);
        return restoresAt == null || restoresAt.isAfter(now);
    }

    private static boolean exempt(ServerPlayer player, String featureId) {
        var node = PermissionsHandler.phasePermission("commands.control." + featureId + ".exempt");
        return node != null && PermissionService.has(player, node);
    }

    private static boolean exempt(UUID playerId, String featureId) {
        var node = PermissionsHandler.phasePermission("commands.control." + featureId + ".exempt");
        return node != null && PermissionService.has(playerId, node);
    }

    private static boolean has(ServerPlayer player, String permission) {
        var node = PermissionsHandler.phasePermission(permission);
        return node != null && PermissionService.has(player, node);
    }

    private static MinecraftServer server(ServerControlExecutionService.ExecutionContext context) {
        return context.server() instanceof MinecraftServer server ? server : null;
    }

    private record AdmissionQueueEntry(
            GameProfile profile,
            Connection connection,
            CompletableFuture<Void> gate,
            Instant queuedAt,
            Instant expiresAt,
            String status
    ) {
    }

    public record ControlHudStatus(
            String id,
            String text,
            SefPayloads.Severity severity,
            int progress,
            Instant updatedAt
    ) {
    }

    private record GuardrailSnapshot(
            long capturedAtMillis,
            long players,
            long usedMemory,
            long entities,
            double averageTickMillis
    ) {
    }

    record PerformanceSnapshot(
            long entities,
            long loadedChunks,
            int levels,
            int incompleteLevels,
            long createdAtEpochMillis
    ) {
    }

    public record SleepVoteSnapshot(int eligible, long yes, long required) {
    }

    private static ServerLevel level(MinecraftServer server, String worldId) {
        ResourceLocation location = ResourceLocation.tryParse(worldId);
        return location == null
                ? null
                : server.getLevel(ResourceKey.create(Registries.DIMENSION, location));
    }

    private static String field(
            ServerControlRepository.ControlRecord record,
            String field,
            String fallback
    ) {
        return record.metadata().getOrDefault("field." + field, fallback);
    }

    private static long number(
            ServerControlRepository.ControlRecord record,
            String field,
            long fallback
    ) {
        try {
            return Long.parseLong(field(record, field, Long.toString(fallback)));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static double decimal(
            ServerControlRepository.ControlRecord record,
            String field,
            double fallback
    ) {
        try {
            return new BigDecimal(field(record, field, Double.toString(fallback))).doubleValue();
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static Optional<Instant> instant(
            ServerControlRepository.ControlRecord record,
            String field
    ) {
        String value = field(record, field, "");
        if (value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Instant.parse(value));
        } catch (DateTimeParseException exception) {
            return Optional.empty();
        }
    }

    private static Set<Long> warningSeconds(ServerControlRepository.ControlRecord record) {
        Set<Long> result = new HashSet<>();
        for (String value : field(record, "warnings_seconds", "300,60,30,10,5,4,3,2,1").split(",", -1)) {
            try {
                long seconds = Long.parseLong(value.strip());
                if (seconds > 0L && seconds <= 2_592_000L) {
                    result.add(seconds);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return result;
    }

    private static boolean safeGlob(String value, String pattern) {
        if (pattern.length() > 128) {
            return false;
        }
        String[] segments = pattern.split("\\*", -1);
        int cursor = 0;
        for (String segment : segments) {
            if (segment.isEmpty()) {
                continue;
            }
            int found = value.indexOf(segment, cursor);
            if (found < 0) {
                return false;
            }
            cursor = found + segment.length();
        }
        return true;
    }

    private static String commandRoot(String input) {
        String value = Optional.ofNullable(input).orElse("").strip();
        while (value.startsWith("/")) {
            value = value.substring(1).stripLeading();
        }
        int separator = 0;
        while (separator < value.length() && !Character.isWhitespace(value.charAt(separator))) {
            separator++;
        }
        String root = value.substring(0, separator).toLowerCase(Locale.ROOT);
        int namespace = root.indexOf(':');
        return namespace < 0 ? root : root.substring(namespace + 1);
    }

    private static String boundedMessage(String message) {
        String normalized = message == null ? "" : message.replaceAll("[\\p{Cc}\\p{Cf}]", "");
        return normalized.length() <= 1024 ? normalized : normalized.substring(0, 1024);
    }

    private static <K, V> void boundedPut(Map<K, V> map, K key, V value, int maximum) {
        if (!map.containsKey(key) && map.size() >= maximum) {
            K first = map.keySet().iterator().next();
            map.remove(first);
        }
        map.put(key, value);
    }

    private record QuarantineAnchor(
            String world,
            double x,
            double y,
            double z,
            float yaw,
            float pitch
    ) {
    }
}
