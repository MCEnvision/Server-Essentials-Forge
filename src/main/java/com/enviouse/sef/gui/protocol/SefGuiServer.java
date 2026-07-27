package com.enviouse.sef.gui.protocol;

import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.teleport.HomeRecord;
import com.enviouse.sef.teleport.TeleportRequestService;
import com.enviouse.sef.teleport.TeleportSettings;
import com.enviouse.sef.teleport.WarpRecord;
import com.enviouse.sef.vanish.VanishUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

public final class SefGuiServer {
    public static final String DASHBOARD = "dashboard";
    public static final String HOMES = "homes";
    public static final String WARPS = "warps";
    public static final String TELEPORT_REQUESTS = "teleport_requests";
    public static final String HELP = "help";
    public static final String STAFF = "staff";
    public static final String PLAYERS = "players";

    private static final int PAGE_SIZE = 12;
    private static final AtomicLong PANEL_REVISIONS = new AtomicLong();
    private static final AtomicLong TARGET_REVISIONS = new AtomicLong();
    private static final Map<UUID, OpenPanel> PANELS = new LinkedHashMap<>();
    private static final Map<UUID, Long> PLAYER_REVISIONS = new LinkedHashMap<>();
    private static final byte[] PROTOTYPE_TAG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAQAAAC1+jfqAAAAHUlEQVR42mNkYGD4z0ABYBw1"
                    + "YNSAUQNGDRg1gAIAIwkCAUEW5RQAAAAASUVORK5CYII=");
    private static final String PROTOTYPE_HASH = sha256(PROTOTYPE_TAG);
    private static final UUID PROTOTYPE_TAG_ID =
            UUID.nameUUIDFromBytes("sef:fancy_tags:prototype".getBytes(StandardCharsets.UTF_8));

    private SefGuiServer() {
    }

    public static void handleOpen(ServerPlayer player, SefPayloads.OpenPanelRequest request) {
        SefProtocol.Feature feature = feature(request.panelId());
        if (feature == null
                || SefSessionManager.instance().acceptRequest(
                player,
                request.sessionId(),
                request.sequence(),
                feature) != SefSessionManager.RequestDecision.ACCEPTED) {
            return;
        }
        open(player, request.panelId(), request.page(), request.query());
    }

    public static void handleAction(ServerPlayer player, SefPayloads.PanelActionRequest request) {
        SefProtocol.Feature feature = feature(request.panelId());
        if (feature == null
                || SefSessionManager.instance().acceptRequest(
                player,
                request.sessionId(),
                request.sequence(),
                feature) != SefSessionManager.RequestDecision.ACCEPTED) {
            return;
        }
        OpenPanel panel;
        synchronized (PANELS) {
            panel = PANELS.get(player.getUUID());
        }
        if (panel == null) {
            open(player, request.panelId(), 1, "");
            return;
        }
        AllowedAction allowed = panel.actions().get(request.entryId());
        PanelActionValidator.Decision decision = PanelActionValidator.validate(
                panel.sessionId(),
                panel.panelId(),
                panel.revision(),
                panel.expiresAt(),
                allowed == null ? null : allowed.controlId(),
                allowed == null ? null : request.entryId(),
                allowed == null ? -1L : allowed.entryRevision(),
                request,
                Instant.now());
        if (decision != PanelActionValidator.Decision.ACCEPTED
                || !allowed.stillValid().test(player)) {
            open(player, request.panelId(), panel.page(), panel.query());
            return;
        }
        if (allowed.kind() == ActionKind.OPEN_PANEL) {
            open(player, allowed.value(), 1, "");
            return;
        }
        if (allowed.kind() == ActionKind.SELECT_PLAYER) {
            openPlayerDetail(player, UUID.fromString(allowed.value()));
            return;
        }
        if (allowed.kind() == ActionKind.DETAIL) {
            openHelpDetail(player, allowed.value());
            return;
        }
        if (allowed.kind() == ActionKind.CONFIRM) {
            openConfirmation(player, allowed);
            return;
        }
        if (allowed.kind() == ActionKind.COMMAND) {
            player.server.getCommands().performPrefixedCommand(
                    player.createCommandSourceStack(),
                    allowed.value());
            openProgress(player, "The server processed the selected player action.");
        }
    }

    public static boolean openDashboard(ServerPlayer player) {
        SefSessionManager.SessionView session =
                SefSessionManager.instance().session(player).orElse(null);
        if (session == null || !session.supports(SefProtocol.Feature.DASHBOARD)) {
            return false;
        }
        open(player, DASHBOARD, 1, "");
        return true;
    }

    public static void sendTagManifest(ServerPlayer player) {
        SefSessionManager.SessionView session =
                SefSessionManager.instance().session(player).orElse(null);
        if (session == null
                || !session.supports(SefProtocol.Feature.FANCY_TAGS_STATIC)
                || !ConfigHandler.config.fancyTagsPrototypeEnabled.get()
                || PROTOTYPE_TAG.length > ConfigHandler.config.fancyTagsPrototypeMaximumBytes.get()
                || !player.connection.hasChannel(SefPayloads.TagManifest.TYPE)) {
            return;
        }
        PacketDistributor.sendToPlayer(player, new SefPayloads.TagManifest(
                session.sessionId(),
                1L,
                PROTOTYPE_TAG_ID,
                PROTOTYPE_HASH,
                PROTOTYPE_TAG.length,
                "SEF"));
    }

    public static void handleTagContent(
            ServerPlayer player,
            SefPayloads.TagContentRequest request
    ) {
        if (SefSessionManager.instance().acceptRequest(
                player,
                request.sessionId(),
                request.sequence(),
                SefProtocol.Feature.FANCY_TAGS_STATIC)
                != SefSessionManager.RequestDecision.ACCEPTED
                || !ConfigHandler.config.fancyTagsPrototypeEnabled.get()
                || !PROTOTYPE_HASH.equals(request.hash())
                || PROTOTYPE_TAG.length > ConfigHandler.config.fancyTagsPrototypeMaximumBytes.get()
                || !player.connection.hasChannel(SefPayloads.TagContent.TYPE)) {
            return;
        }
        PacketDistributor.sendToPlayer(player, new SefPayloads.TagContent(
                request.sessionId(),
                PROTOTYPE_HASH,
                PROTOTYPE_TAG));
    }

    public static void logout(UUID playerId) {
        synchronized (PANELS) {
            PANELS.remove(playerId);
        }
        synchronized (PLAYER_REVISIONS) {
            PLAYER_REVISIONS.remove(playerId);
        }
    }

    public static void trackPlayer(ServerPlayer player) {
        synchronized (PLAYER_REVISIONS) {
            PLAYER_REVISIONS.put(player.getUUID(), nextTargetRevision());
        }
        refreshPlayerPickers(player.server);
    }

    public static void untrackPlayer(ServerPlayer player) {
        synchronized (PLAYER_REVISIONS) {
            PLAYER_REVISIONS.remove(player.getUUID());
        }
        refreshPlayerPickers(player.server);
    }

    public static void clear() {
        synchronized (PANELS) {
            PANELS.clear();
        }
        synchronized (PLAYER_REVISIONS) {
            PLAYER_REVISIONS.clear();
        }
    }

    public static int openPanelCount() {
        synchronized (PANELS) {
            PANELS.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(Instant.now()));
            return PANELS.size();
        }
    }

    private static void open(ServerPlayer player, String requestedPanel, int requestedPage, String query) {
        String panelId = normalizePanel(requestedPanel);
        SefSessionManager.SessionView session =
                SefSessionManager.instance().session(player).orElse(null);
        if (session == null || !allowedPanel(player, panelId, session)) {
            player.sendSystemMessage(Component.literal("You cannot open that SEF panel."));
            return;
        }
        SnapshotData data = build(player, panelId, query);
        int maximumEntries = Math.min(
                SefProtocol.MAXIMUM_PANEL_ENTRIES,
                ConfigHandler.config.guiMaximumPanelEntries.get());
        int pageSize = Math.max(1, Math.min(PAGE_SIZE, maximumEntries));
        List<EntryAction> filtered = data.entries().stream()
                .filter(entry -> matches(entry.entry().title(), entry.entry().subtitle(), query))
                .toList();
        int pages = Math.max(1, (filtered.size() + pageSize - 1) / pageSize);
        int page = Math.max(1, Math.min(requestedPage, pages));
        int start = Math.min(filtered.size(), (page - 1) * pageSize);
        int end = Math.min(filtered.size(), start + pageSize);
        List<EntryAction> visible = filtered.subList(start, end);
        long revision = nextPanelRevision();
        Map<UUID, AllowedAction> actions = new LinkedHashMap<>();
        for (EntryAction entry : visible) {
            if (entry.action() != null && entry.entry().enabled()) {
                actions.put(entry.entry().entryId(), entry.action());
            }
        }
        OpenPanel state = new OpenPanel(
                session.sessionId(),
                revision,
                panelId,
                page,
                Objects.requireNonNullElse(query, ""),
                Instant.now().plusSeconds(ConfigHandler.config.guiPanelSessionSeconds.get()),
                Map.copyOf(actions));
        synchronized (PANELS) {
            PANELS.put(player.getUUID(), state);
        }
        if (player.connection.hasChannel(SefPayloads.PanelSnapshot.TYPE)) {
            PacketDistributor.sendToPlayer(player, new SefPayloads.PanelSnapshot(
                    session.sessionId(),
                    revision,
                    panelId,
                    data.view(),
                    data.title(),
                    page,
                    pages,
                    state.query(),
                    visible.stream().map(EntryAction::entry).toList(),
                    data.status()));
        }
    }

    private static SnapshotData build(ServerPlayer player, String panelId, String query) {
        return switch (panelId) {
            case DASHBOARD -> dashboard(player);
            case HOMES -> homes(player);
            case WARPS -> warps(player);
            case TELEPORT_REQUESTS -> teleportRequests(player);
            case HELP -> help(player);
            case STAFF -> staff(player);
            case PLAYERS -> players(player);
            default -> new SnapshotData("SEF", "Unknown panel", List.of());
        };
    }

    private static SnapshotData dashboard(ServerPlayer player) {
        List<EntryAction> entries = new ArrayList<>();
        addPanelLink(entries, HOMES, "Homes", "View and visit your homes", "minecraft:red_bed", player);
        addPanelLink(entries, WARPS, "Warps", "View server and player warps", "minecraft:ender_pearl", player);
        addPanelLink(
                entries,
                TELEPORT_REQUESTS,
                "Teleport requests",
                "Accept, deny, or cancel requests",
                "minecraft:compass",
                player);
        addPanelLink(entries, HELP, "Help and diagnostics", "Permission filtered commands", "minecraft:book", player);
        addPanelLink(entries, STAFF, "Staff overview", "Server and policy status", "minecraft:command_block", player);
        addPanelLink(entries, PLAYERS, "Player controls", "Vanish safe target picker", "minecraft:player_head", player);
        return new SnapshotData(
                SefPayloads.PanelView.DASHBOARD,
                "Server Essentials",
                "Every action is checked again by the server.",
                entries);
    }

    private static SnapshotData homes(ServerPlayer player) {
        if (!KernelCommandExecutor.canUse(player.createCommandSourceStack(), "sef:teleport.home.list")) {
            return denied("Homes");
        }
        boolean canVisit =
                KernelCommandExecutor.canUse(player.createCommandSourceStack(), "sef:teleport.home.use");
        List<EntryAction> entries = KernelServices.teleports().homes(player.getUUID()).stream()
                .map(home -> {
                    SefPayloads.PanelEntry entry = entry(
                            "home:" + home.id(),
                            home.revision(),
                            "visit",
                            home.displayName(),
                            location(home.location().dimensionId(), home.description()),
                            home.icon().isBlank() ? "minecraft:red_bed" : home.icon(),
                            canVisit,
                            false);
                    return new EntryAction(entry, canVisit ? new AllowedAction(
                            ActionKind.COMMAND,
                            "visit",
                            home.revision(),
                            "home " + home.normalizedName(),
                            currentPlayer -> KernelServices.teleports().homeById(home.id())
                                    .filter(HomeRecord::active)
                                    .map(current -> current.ownerId().equals(currentPlayer.getUUID())
                                            && current.revision() == home.revision()
                                            && KernelCommandExecutor.canUse(
                                            currentPlayer.createCommandSourceStack(),
                                            "sef:teleport.home.use"))
                                    .orElse(false)) : null);
                })
                .toList();
        return new SnapshotData("Homes", entries.size() + " homes", entries);
    }

    private static SnapshotData warps(ServerPlayer player) {
        List<EntryAction> entries = new ArrayList<>();
        boolean canListServerWarps =
                KernelCommandExecutor.canUse(player.createCommandSourceStack(), "sef:teleport.warp.list");
        boolean canUseServerWarp =
                KernelCommandExecutor.canUse(player.createCommandSourceStack(), "sef:teleport.warp.use");
        if (canListServerWarps) {
            boolean hidden = PermissionService.has(player, PermissionsHandler.warpHiddenView);
            String root = KernelServices.teleportSettings().ownershipMode() == TeleportSettings.OwnershipMode.COEXIST
                    ? "sefwarp"
                    : "warp";
            for (WarpRecord warp : KernelServices.teleports().serverWarps(hidden)) {
                if (warp.status() != WarpRecord.Status.ACTIVE) {
                    continue;
                }
                entries.add(warpEntry(
                        warp,
                        root + " " + warp.normalizedName(),
                        "Server warp",
                        canUseServerWarp,
                        current -> KernelServices.teleports().warpById(warp.id())
                                .filter(WarpRecord::active)
                                .map(latest -> latest.revision() == warp.revision()
                                        && latest.status() == WarpRecord.Status.ACTIVE
                                        && KernelCommandExecutor.canUse(
                                        current.createCommandSourceStack(),
                                        "sef:teleport.warp.use"))
                                .orElse(false)));
            }
        }
        boolean canListPlayerWarps =
                KernelCommandExecutor.canUse(player.createCommandSourceStack(), "sef:teleport.player_warp.list");
        boolean canUsePlayerWarp =
                KernelCommandExecutor.canUse(player.createCommandSourceStack(), "sef:teleport.player_warp.use");
        if (canListPlayerWarps) {
            boolean moderator = PermissionService.has(player, PermissionsHandler.playerWarpModerate);
            for (WarpRecord warp : KernelServices.teleports()
                    .visiblePlayerWarps(player.getUUID(), moderator)) {
                String reference = warp.ownerNameSnapshot() + ":" + warp.normalizedName();
                entries.add(warpEntry(
                        warp,
                        "pwarp " + reference,
                        warp.ownerNameSnapshot(),
                        canUsePlayerWarp,
                        current -> KernelServices.teleports().warpById(warp.id())
                                .filter(WarpRecord::active)
                                .map(latest -> latest.revision() == warp.revision()
                                        && latest.canVisit(current.getUUID(), moderator)
                                        && KernelCommandExecutor.canUse(
                                        current.createCommandSourceStack(),
                                        "sef:teleport.player_warp.use"))
                                .orElse(false)));
            }
        }
        return new SnapshotData("Warps", entries.size() + " accessible warps", List.copyOf(entries));
    }

    private static EntryAction warpEntry(
            WarpRecord warp,
            String command,
            String owner,
            boolean enabled,
            Predicate<ServerPlayer> stillValid
    ) {
        String subtitle = owner + (warp.description().isBlank() ? "" : ", " + warp.description());
        return new EntryAction(
                entry(
                        "warp:" + warp.id(),
                        warp.revision(),
                        "visit",
                        warp.displayName(),
                        subtitle,
                        warp.icon().isBlank() ? "minecraft:ender_pearl" : warp.icon(),
                        enabled,
                        false),
                enabled ? new AllowedAction(
                        ActionKind.COMMAND,
                        "visit",
                        warp.revision(),
                        command,
                        stillValid) : null);
    }

    private static SnapshotData teleportRequests(ServerPlayer player) {
        List<EntryAction> entries = new ArrayList<>();
        if (KernelCommandExecutor.canUse(
                player.createCommandSourceStack(),
                "sef:teleport.request.accept")) {
            for (TeleportRequestService.Request request :
                    KernelServices.teleportRequests().incoming(player.getUUID())) {
                ServerPlayer sender = player.server.getPlayerList().getPlayer(request.senderId());
                if (sender == null) {
                    continue;
                }
                entries.add(requestEntry(
                        request,
                        "accept",
                        "Accept from " + sender.getGameProfile().getName(),
                        "tpaccept " + sender.getGameProfile().getName(),
                        player.getUUID(),
                        request.senderId()));
                if (KernelCommandExecutor.canUse(
                        player.createCommandSourceStack(),
                        "sef:teleport.request.deny")) {
                    entries.add(requestEntry(
                            request,
                            "deny",
                            "Deny from " + sender.getGameProfile().getName(),
                            "tpdeny " + sender.getGameProfile().getName(),
                            player.getUUID(),
                            request.senderId()));
                }
            }
        }
        if (KernelCommandExecutor.canUse(
                player.createCommandSourceStack(),
                "sef:teleport.request.cancel")) {
            for (TeleportRequestService.Request request :
                    KernelServices.teleportRequests().outgoing(player.getUUID())) {
                ServerPlayer target = player.server.getPlayerList().getPlayer(request.targetId());
                if (target == null) {
                    continue;
                }
                entries.add(requestEntry(
                        request,
                        "cancel",
                        "Cancel to " + target.getGameProfile().getName(),
                        "tpcancel " + target.getGameProfile().getName(),
                        request.targetId(),
                        player.getUUID()));
            }
        }
        return new SnapshotData(
                "Teleport requests",
                entries.size() + " available actions",
                List.copyOf(entries));
    }

    private static EntryAction requestEntry(
            TeleportRequestService.Request request,
            String control,
            String title,
            String command,
            UUID expectedTarget,
            UUID expectedSender
    ) {
        return new EntryAction(
                entry(
                        "request:" + request.id() + ":" + control,
                        request.revision(),
                        control,
                        title,
                        request.type().name().toLowerCase(Locale.ROOT),
                        "minecraft:compass",
                        true,
                        control.equals("deny") || control.equals("cancel")),
                new AllowedAction(
                        ActionKind.COMMAND,
                        control,
                        request.revision(),
                        command,
                        player -> KernelServices.teleportRequests().request(request.id())
                                .map(latest -> latest.revision() == request.revision()
                                        && latest.state() == TeleportRequestService.State.PENDING
                                        && latest.targetId().equals(expectedTarget)
                                        && latest.senderId().equals(expectedSender))
                                .orElse(false)));
    }

    private static SnapshotData help(ServerPlayer player) {
        List<EntryAction> entries = KernelServices.catalog().entries().stream()
                .filter(definition -> definition.permissionIds().stream().allMatch(
                        id -> {
                            var node = KernelServices.permissionNode(id);
                            return node != null && PermissionService.has(player, node);
                        }))
                .sorted(Comparator.comparing(CommandDefinition::canonicalRoute))
                .map(definition -> new EntryAction(
                        entry(
                                "help:" + definition.id(),
                                KernelServices.configurationRevision(),
                                "describe",
                                "/" + definition.canonicalRoute(),
                                definition.id(),
                                "minecraft:book",
                                true,
                                false),
                        new AllowedAction(
                                ActionKind.DETAIL,
                                "describe",
                                KernelServices.configurationRevision(),
                                definition.id(),
                                current -> definition.permissionIds().stream().allMatch(
                                        permissionId -> {
                                            var node = KernelServices.permissionNode(permissionId);
                                            return node != null && PermissionService.has(current, node);
                                        }))))
                .toList();
        return new SnapshotData("Command catalog", entries.size() + " permitted actions", entries);
    }

    private static void openHelpDetail(ServerPlayer player, String actionId) {
        CommandDefinition definition = KernelServices.catalog().find(actionId).orElse(null);
        if (definition == null || definition.permissionIds().stream().anyMatch(
                permissionId -> {
                    var node = KernelServices.permissionNode(permissionId);
                    return node == null || !PermissionService.has(player, node);
                })) {
            open(player, HELP, 1, "");
            return;
        }
        List<EntryAction> entries = List.of(
                information(
                        "help:detail:route:" + definition.id(),
                        "/" + definition.canonicalRoute(),
                        definition.id(),
                        "minecraft:command_block"),
                information(
                        "help:detail:permissions:" + definition.id(),
                        "Permissions",
                        String.join(", ", definition.permissionIds()),
                        "minecraft:tripwire_hook"),
                information(
                        "help:detail:feature:" + definition.id(),
                        "Feature",
                        definition.featureId(),
                        "minecraft:comparator"),
                panelBackEntry("help:detail:back:" + definition.id(), HELP, "Back to commands"));
        publishCustom(
                player,
                HELP,
                new SnapshotData(
                        SefPayloads.PanelView.DETAIL,
                        "Command details",
                        "This page is generated from the canonical action catalog.",
                        entries));
    }

    private static SnapshotData staff(ServerPlayer player) {
        if (!PermissionService.has(player, PermissionsHandler.kernelPanel)) {
            return denied("Staff overview");
        }
        List<EntryAction> entries = List.of(
                information(
                        "staff:players",
                        "Online players",
                        Integer.toString(player.server.getPlayerCount()),
                        "minecraft:player_head"),
                information(
                        "staff:catalog",
                        "Command catalog",
                        KernelServices.catalog().size() + " actions",
                        "minecraft:command_block"),
                information(
                        "staff:storage",
                        "Storage",
                        KernelServices.storage().recoveryMode() ? "Recovery mode" : "Healthy",
                        "minecraft:chest"),
                information(
                        "staff:sessions",
                        "Enhanced clients",
                        SefSessionManager.instance().activeCount() + " active",
                        "minecraft:spyglass"));
        return new SnapshotData("Staff overview", "Read only server status", entries);
    }

    private static SnapshotData players(ServerPlayer viewer) {
        if (!PermissionService.has(viewer, PermissionsHandler.kernelPanel)
                || !PermissionService.has(viewer, PermissionsHandler.vanishOthersCommand)) {
            return denied("Player controls");
        }
        List<EntryAction> entries = viewer.server.getPlayerList().getPlayers().stream()
                .filter(target -> visibleTarget(viewer, target))
                .sorted(Comparator.comparing(
                        target -> target.getGameProfile().getName().toLowerCase(Locale.ROOT)))
                .map(target -> {
                    long targetRevision = targetRevision(target.getUUID());
                    SefPayloads.PanelEntry entry = entry(
                            "player:" + target.getUUID(),
                            targetRevision,
                            "select",
                            target.getGameProfile().getName(),
                            VanishUtil.isVanished(target) ? "Vanished" : "Online",
                            "minecraft:player_head",
                            true,
                            false);
                    return new EntryAction(entry, new AllowedAction(
                            ActionKind.SELECT_PLAYER,
                            "select",
                            targetRevision,
                            target.getUUID().toString(),
                            current -> {
                                ServerPlayer latest = current.server.getPlayerList().getPlayer(target.getUUID());
                                return latest != null
                                        && targetRevision(latest.getUUID()) == targetRevision
                                        && visibleTarget(current, latest);
                            }));
                })
                .toList();
        return new SnapshotData(
                SefPayloads.PanelView.PICKER,
                "Player controls",
                entries.size() + " visible targets",
                entries);
    }

    private static void openPlayerDetail(ServerPlayer viewer, UUID targetId) {
        ServerPlayer target = viewer.server.getPlayerList().getPlayer(targetId);
        long revision = targetRevision(targetId);
        if (target == null || revision < 1L || !visibleTarget(viewer, target)) {
            open(viewer, PLAYERS, 1, "");
            return;
        }
        String username = target.getGameProfile().getName();
        if (!username.matches("[A-Za-z0-9_]{1,16}")) {
            open(viewer, PLAYERS, 1, "");
            return;
        }
        Predicate<ServerPlayer> validTarget = current -> {
            ServerPlayer latest = current.server.getPlayerList().getPlayer(targetId);
            return latest != null
                    && targetRevision(targetId) == revision
                    && visibleTarget(current, latest)
                    && PermissionService.has(current, PermissionsHandler.vanishOthersCommand);
        };
        List<EntryAction> entries = List.of(
                information(
                        "player:status:" + targetId,
                        username,
                        VanishUtil.isVanished(target)
                                ? "Vanish level " + VanishUtil.getVanishLevel(target)
                                : "Visible",
                        "minecraft:player_head"),
                new EntryAction(
                        entry(
                                "player:toggle:" + targetId,
                                revision,
                                "confirm",
                                VanishUtil.isVanished(target) ? "Make visible" : "Vanish",
                                "This requires confirmation and rechecks hierarchy.",
                                "minecraft:ender_eye",
                                true,
                                true),
                        new AllowedAction(
                                ActionKind.CONFIRM,
                                "confirm",
                                revision,
                                "v toggle " + username,
                                validTarget)),
                panelBackEntry("player:back:" + targetId, PLAYERS, "Back to players"));
        publishCustom(
                viewer,
                PLAYERS,
                new SnapshotData(
                        SefPayloads.PanelView.FORM,
                        "Player details",
                        "Target " + username + ", revision " + revision,
                        entries));
    }

    private static void openConfirmation(ServerPlayer viewer, AllowedAction pending) {
        if (!pending.stillValid().test(viewer)) {
            open(viewer, PLAYERS, 1, "");
            return;
        }
        List<EntryAction> entries = List.of(
                new EntryAction(
                        entry(
                                "player:confirm:" + pending.value(),
                                pending.entryRevision(),
                                "execute",
                                "Confirm player action",
                                "The server rechecks permission, hierarchy, and target revision.",
                                "minecraft:barrier",
                                true,
                                true),
                        new AllowedAction(
                                ActionKind.COMMAND,
                                "execute",
                                pending.entryRevision(),
                                pending.value(),
                                pending.stillValid())),
                panelBackEntry("player:confirm:cancel", PLAYERS, "Cancel"));
        publishCustom(
                viewer,
                PLAYERS,
                new SnapshotData(
                        SefPayloads.PanelView.CONFIRMATION,
                        "Confirm action",
                        "This action changes another player's private vanish state.",
                        entries));
    }

    private static void openProgress(ServerPlayer viewer, String status) {
        publishCustom(
                viewer,
                PLAYERS,
                new SnapshotData(
                        SefPayloads.PanelView.PROGRESS,
                        "Action progress",
                        status,
                        List.of(panelBackEntry(
                                "player:progress:back",
                                PLAYERS,
                                "Return to players"))));
    }

    private static EntryAction panelBackEntry(String key, String panelId, String title) {
        return new EntryAction(
                entry(key, 1L, "open", title, "", "minecraft:arrow", true, false),
                new AllowedAction(
                        ActionKind.OPEN_PANEL,
                        "open",
                        1L,
                        panelId,
                        current -> SefSessionManager.instance().session(current)
                                .map(session -> allowedPanel(current, panelId, session))
                                .orElse(false)));
    }

    private static void publishCustom(
            ServerPlayer player,
            String panelId,
            SnapshotData data
    ) {
        SefSessionManager.SessionView session =
                SefSessionManager.instance().session(player).orElse(null);
        if (session == null || !allowedPanel(player, panelId, session)) {
            return;
        }
        long revision = nextPanelRevision();
        Map<UUID, AllowedAction> actions = data.entries().stream()
                .filter(entry -> entry.action() != null && entry.entry().enabled())
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                        entry -> entry.entry().entryId(),
                        EntryAction::action));
        OpenPanel state = new OpenPanel(
                session.sessionId(),
                revision,
                panelId,
                1,
                "",
                Instant.now().plusSeconds(ConfigHandler.config.guiPanelSessionSeconds.get()),
                actions);
        synchronized (PANELS) {
            PANELS.put(player.getUUID(), state);
        }
        if (player.connection.hasChannel(SefPayloads.PanelSnapshot.TYPE)) {
            PacketDistributor.sendToPlayer(player, new SefPayloads.PanelSnapshot(
                    session.sessionId(),
                    revision,
                    panelId,
                    data.view(),
                    data.title(),
                    1,
                    1,
                    "",
                    data.entries().stream().map(EntryAction::entry).toList(),
                    data.status()));
        }
    }

    private static boolean visibleTarget(ServerPlayer viewer, ServerPlayer target) {
        return !viewer.getUUID().equals(target.getUUID())
                && targetRevision(target.getUUID()) > 0L
                && !VanishUtil.isVanished(target, viewer);
    }

    private static long targetRevision(UUID playerId) {
        synchronized (PLAYER_REVISIONS) {
            return PLAYER_REVISIONS.getOrDefault(playerId, 0L);
        }
    }

    private static void refreshPlayerPickers(net.minecraft.server.MinecraftServer server) {
        List<UUID> viewers;
        synchronized (PANELS) {
            viewers = PANELS.entrySet().stream()
                    .filter(entry -> entry.getValue().panelId().equals(PLAYERS))
                    .map(Map.Entry::getKey)
                    .toList();
        }
        for (UUID viewerId : viewers) {
            ServerPlayer viewer = server.getPlayerList().getPlayer(viewerId);
            if (viewer != null) {
                open(viewer, PLAYERS, 1, "");
            }
        }
    }

    private static EntryAction information(String id, String title, String subtitle, String icon) {
        return new EntryAction(entry(id, 1L, "status", title, subtitle, icon, false, false), null);
    }

    private static SnapshotData denied(String title) {
        return new SnapshotData(title, "No permitted actions are available.", List.of());
    }

    private static void addPanelLink(
            List<EntryAction> entries,
            String panelId,
            String title,
            String subtitle,
            String icon,
            ServerPlayer player
    ) {
        SefSessionManager.SessionView session =
                SefSessionManager.instance().session(player).orElse(null);
        if (session == null || !allowedPanel(player, panelId, session)) {
            return;
        }
        SefPayloads.PanelEntry entry =
                entry("panel:" + panelId, 1L, "open", title, subtitle, icon, true, false);
        entries.add(new EntryAction(entry, new AllowedAction(
                ActionKind.OPEN_PANEL,
                "open",
                1L,
                panelId,
                current -> SefSessionManager.instance().session(current)
                        .map(active -> allowedPanel(current, panelId, active))
                        .orElse(false))));
    }

    private static boolean allowedPanel(
            ServerPlayer player,
            String panelId,
            SefSessionManager.SessionView session
    ) {
        SefProtocol.Feature feature = feature(panelId);
        if (feature == null || !session.supports(feature)) {
            return false;
        }
        return switch (panelId) {
            case DASHBOARD -> true;
            case HOMES -> KernelCommandExecutor.canUse(
                    player.createCommandSourceStack(),
                    "sef:teleport.home.list");
            case WARPS -> KernelCommandExecutor.canUse(
                    player.createCommandSourceStack(),
                    "sef:teleport.warp.list")
                    || KernelCommandExecutor.canUse(
                    player.createCommandSourceStack(),
                    "sef:teleport.player_warp.list");
            case TELEPORT_REQUESTS -> List.of(
                            "sef:teleport.request.accept",
                            "sef:teleport.request.deny",
                            "sef:teleport.request.cancel")
                    .stream()
                    .anyMatch(action -> KernelCommandExecutor.canUse(
                            player.createCommandSourceStack(),
                            action));
            case HELP -> PermissionService.has(player, PermissionsHandler.sefCommandsCatalog);
            case STAFF -> PermissionService.has(player, PermissionsHandler.kernelPanel);
            case PLAYERS -> PermissionService.has(player, PermissionsHandler.kernelPanel)
                    && PermissionService.has(player, PermissionsHandler.vanishOthersCommand);
            default -> false;
        };
    }

    private static SefProtocol.Feature feature(String panelId) {
        if (panelId == null) {
            return null;
        }
        return switch (panelId.toLowerCase(Locale.ROOT)) {
            case DASHBOARD -> SefProtocol.Feature.DASHBOARD;
            case HOMES -> SefProtocol.Feature.HOMES;
            case WARPS -> SefProtocol.Feature.WARPS;
            case TELEPORT_REQUESTS -> SefProtocol.Feature.TELEPORT_REQUESTS;
            case HELP -> SefProtocol.Feature.HELP_DIAGNOSTICS;
            case STAFF, PLAYERS -> SefProtocol.Feature.STAFF_OVERVIEW;
            default -> null;
        };
    }

    private static String normalizePanel(String panelId) {
        String normalized = Objects.requireNonNullElse(panelId, "").trim().toLowerCase(Locale.ROOT);
        return feature(normalized) == null ? DASHBOARD : normalized;
    }

    private static boolean matches(String title, String subtitle, String query) {
        String normalized = Objects.requireNonNullElse(query, "").trim().toLowerCase(Locale.ROOT);
        return normalized.isBlank()
                || title.toLowerCase(Locale.ROOT).contains(normalized)
                || subtitle.toLowerCase(Locale.ROOT).contains(normalized);
    }

    private static SefPayloads.PanelEntry entry(
            String stableKey,
            long revision,
            String control,
            String title,
            String subtitle,
            String icon,
            boolean enabled,
            boolean destructive
    ) {
        return new SefPayloads.PanelEntry(
                UUID.nameUUIDFromBytes(("sef:gui:" + stableKey).getBytes(StandardCharsets.UTF_8)),
                revision,
                control,
                bounded(title, 128),
                bounded(subtitle, 256),
                normalizeIcon(icon),
                enabled,
                destructive);
    }

    private static String normalizeIcon(String icon) {
        String value = Objects.requireNonNullElse(icon, "").trim().toLowerCase(Locale.ROOT);
        if (!value.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) {
            return "minecraft:paper";
        }
        return value;
    }

    private static String location(String dimension, String description) {
        String shortDimension = dimension.contains(":")
                ? dimension.substring(dimension.indexOf(':') + 1)
                : dimension;
        return description.isBlank() ? shortDimension : shortDimension + ", " + description;
    }

    private static String bounded(String text, int maximum) {
        String value = Objects.requireNonNullElse(text, "").trim();
        value = value.codePoints()
                .filter(codePoint -> !Character.isISOControl(codePoint))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return value.length() <= maximum ? value : value.substring(0, maximum);
    }

    private static long nextPanelRevision() {
        return PANEL_REVISIONS.updateAndGet(current -> current == Long.MAX_VALUE ? 1L : current + 1L);
    }

    private static long nextTargetRevision() {
        return TARGET_REVISIONS.updateAndGet(current -> current == Long.MAX_VALUE ? 1L : current + 1L);
    }

    private static String sha256(byte[] bytes) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private record SnapshotData(
            SefPayloads.PanelView view,
            String title,
            String status,
            List<EntryAction> entries
    ) {
        private SnapshotData(String title, String status, List<EntryAction> entries) {
            this(SefPayloads.PanelView.LIST, title, status, entries);
        }

        private SnapshotData {
            entries = List.copyOf(entries);
        }
    }

    private record EntryAction(SefPayloads.PanelEntry entry, AllowedAction action) {
    }

    private record AllowedAction(
            ActionKind kind,
            String controlId,
            long entryRevision,
            String value,
            Predicate<ServerPlayer> stillValid
    ) {
    }

    private record OpenPanel(
            UUID sessionId,
            long revision,
            String panelId,
            int page,
            String query,
            Instant expiresAt,
            Map<UUID, AllowedAction> actions
    ) {
    }

    private enum ActionKind {
        OPEN_PANEL,
        SELECT_PLAYER,
        DETAIL,
        CONFIRM,
        COMMAND
    }
}
