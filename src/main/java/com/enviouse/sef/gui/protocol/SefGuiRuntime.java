package com.enviouse.sef.gui.protocol;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.control.MinecraftServerControlRuntime;
import com.enviouse.sef.gui.GuiPreferenceRepository;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.player.PlayerStateService;
import com.enviouse.sef.utils.SEFUtilities;
import com.enviouse.sef.vanish.VanishUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class SefGuiRuntime {
    private static final Map<UUID, Instant> LOGIN_TIMES = new LinkedHashMap<>();
    private static final Map<UUID, HudDeltaComposer.State> HUD_STATES = new LinkedHashMap<>();
    private static final Map<UUID, IdentityProjectionComposer.State> IDENTITY_STATES = new LinkedHashMap<>();
    private static final HudContracts.Registry HUD_CONTRACTS = HudContracts.phaseNineDefaults();

    private SefGuiRuntime() {
    }

    public static synchronized void login(ServerPlayer player) {
        LOGIN_TIMES.put(player.getUUID(), Instant.now());
        HUD_STATES.remove(player.getUUID());
        IDENTITY_STATES.remove(player.getUUID());
    }

    public static synchronized void logout(UUID playerId) {
        LOGIN_TIMES.remove(playerId);
        HUD_STATES.remove(playerId);
        IDENTITY_STATES.remove(playerId);
    }

    public static synchronized void clear() {
        LOGIN_TIMES.clear();
        HUD_STATES.clear();
        IDENTITY_STATES.clear();
    }

    public static void departing(ServerPlayer player) {
        logout(player.getUUID());
        refreshIdentityProjections(player.server, player.getUUID());
    }

    public static void tick(MinecraftServer server) {
        if (!SefNetwork.enhancedGuiActive()) {
            server.getPlayerList().getPlayers().forEach(SefGuiRuntime::sendWarmupFallback);
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            SefSessionManager.instance().refresh(player);
            if (!sendHud(player)) {
                sendWarmupFallback(player);
            }
            sendReminder(player);
        }
    }

    public static boolean dismissReminder(ServerPlayer player) {
        try {
            KernelServices.guiPreferences().dismissReminder(
                    player.getUUID(),
                    ConfigHandler.config.guiReminderRevision.get());
            return true;
        } catch (IllegalStateException exception) {
            ServerEssentialsForge.LOGGER.warn(
                    "[SEF] GUI reminder preference could not be saved for {}",
                    player.getGameProfile().getName());
            return false;
        }
    }

    private static boolean sendHud(ServerPlayer player) {
        SefSessionManager.SessionView session =
                SefSessionManager.instance().session(player).orElse(null);
        if (session == null
                || !session.supports(SefProtocol.Feature.HUD)
                || !PermissionService.has(player, PermissionsHandler.kernelHud)
                || !player.connection.hasChannel(SefPayloads.HudDelta.TYPE)) {
            synchronized (SefGuiRuntime.class) {
                HUD_STATES.remove(player.getUUID());
            }
            return false;
        }
        List<SefPayloads.HudTile> tiles = new ArrayList<>();
        if (VanishUtil.isVanished(player)) {
            tiles.add(tile(
                    "vanish",
                    "Vanish level " + VanishUtil.getVanishLevel(player),
                    SefPayloads.Severity.NOTICE,
                    0));
        }
        if (ConfigHandler.config.enableSocialEssentials.get()
                && ConfigHandler.config.enableSocialSpy.get()
                && KernelServices.social().preferences(player.getUUID()).socialSpyRequested()
                && PermissionService.has(player, PermissionsHandler.socialSpyCommand)
                && PermissionService.has(player, PermissionsHandler.socialSpyViewMetadata)) {
            tiles.add(tile(
                    "social_spy",
                    "Social spy enabled",
                    SefPayloads.Severity.WARNING,
                    0));
        }
        var commandSpyPermission = PermissionsHandler.phasePermission("commands.commandspy");
        var commandSpyMetadata = PermissionsHandler.phasePermission("commandspy.view.metadata");
        if (ConfigHandler.config.enableCommandSpy.get()
                && KernelServices.commandSpies().profile(player.getUUID()).enabled()
                && commandSpyPermission != null
                && commandSpyMetadata != null
                && PermissionService.has(player, commandSpyPermission)
                && PermissionService.has(player, commandSpyMetadata)) {
            tiles.add(tile(
                    "command_spy",
                    "Command spy enabled",
                    SefPayloads.Severity.WARNING,
                    0));
        }
        UUID playerId = player.getUUID();
        if (PlayerStateService.afk(playerId)) {
            tiles.add(tile("afk", "AFK", SefPayloads.Severity.INFO, 0));
        }
        if (PlayerStateService.fly(playerId)) {
            tiles.add(tile("fly", "Flight enabled", SefPayloads.Severity.INFO, 0));
        }
        if (PlayerStateService.god(playerId)) {
            tiles.add(tile("god", "God mode enabled", SefPayloads.Severity.NOTICE, 0));
        }
        var warmup = KernelServices.warmups().inspect(playerId).orElse(null);
        if (warmup != null) {
            long duration = Math.max(1L, warmup.expiresAtEpochMillis() - warmup.startedAtEpochMillis());
            int progress = (int) Math.max(0L, Math.min(
                    100L,
                    (System.currentTimeMillis() - warmup.startedAtEpochMillis()) * 100L / duration));
            tiles.add(tile(
                    "teleport_warmup",
                    "Teleporting " + progress + "%",
                    SefPayloads.Severity.NOTICE,
                    progress));
        }
        for (MinecraftServerControlRuntime.ControlHudStatus status :
                MinecraftServerControlRuntime.hudStatuses(player)) {
            if (tiles.size() >= SefProtocol.MAXIMUM_HUD_TILES) {
                break;
            }
            tiles.add(tile(
                    status.id(),
                    status.text(),
                    status.severity(),
                    status.progress()));
        }
        if (tiles.size() > SefProtocol.MAXIMUM_HUD_TILES) {
            tiles = new ArrayList<>(tiles.subList(0, SefProtocol.MAXIMUM_HUD_TILES));
        }
        HudDeltaComposer.State previous;
        synchronized (SefGuiRuntime.class) {
            previous = HUD_STATES.get(playerId);
        }
        var update = HudDeltaComposer.compose(session.sessionId(), previous, tiles);
        if (update.isEmpty()) {
            return true;
        }
        synchronized (SefGuiRuntime.class) {
            HUD_STATES.put(playerId, update.orElseThrow().state());
        }
        PacketDistributor.sendToPlayer(player, update.orElseThrow().delta());
        return true;
    }

    private static SefPayloads.HudTile tile(
            String id,
            String text,
            SefPayloads.Severity severity,
            int progressPercent
    ) {
        HudContracts.Descriptor descriptor = HUD_CONTRACTS.find(id).orElseThrow();
        return new SefPayloads.HudTile(
                id,
                text,
                severity,
                descriptor.surface(),
                descriptor.surface() == SefPayloads.HudSurface.PROGRESS ? progressPercent : 0);
    }

    private static void sendWarmupFallback(ServerPlayer player) {
        var warmup = KernelServices.warmups().inspect(player.getUUID()).orElse(null);
        HudContracts.Descriptor descriptor = HUD_CONTRACTS.find("teleport_warmup").orElseThrow();
        if (warmup == null
                || descriptor.fallback() != HudContracts.FallbackSurface.ACTION_BAR
                || descriptor.fallbackOwner() != HudContracts.Ownership.SEF) {
            return;
        }
        long remainingMillis = Math.max(0L, warmup.expiresAtEpochMillis() - System.currentTimeMillis());
        long remainingTenths = (remainingMillis + 99L) / 100L;
        player.connection.send(new ClientboundSetActionBarTextPacket(Component.literal(
                "Teleporting in " + remainingTenths / 10L + "." + remainingTenths % 10L + "s")));
    }

    private static void sendIdentityProjection(
            ServerPlayer viewer,
            Map<UUID, Component> displayNames
    ) {
        SefSessionManager.SessionView session =
                SefSessionManager.instance().session(viewer).orElse(null);
        if (session == null
                || !session.supports(SefProtocol.Feature.IDENTITY_PROJECTION)
                || !viewer.connection.hasChannel(SefPayloads.IdentityProjection.TYPE)) {
            synchronized (SefGuiRuntime.class) {
                IDENTITY_STATES.remove(viewer.getUUID());
            }
            return;
        }
        Map<UUID, Component> visible = new LinkedHashMap<>();
        for (Map.Entry<UUID, Component> entry : displayNames.entrySet()) {
            ServerPlayer target = viewer.server.getPlayerList().getPlayer(entry.getKey());
            if (target != null
                    && (target == viewer || !VanishUtil.isVanished(target, viewer))) {
                visible.put(entry.getKey(), entry.getValue());
            }
        }
        IdentityProjectionComposer.State previous;
        synchronized (SefGuiRuntime.class) {
            previous = IDENTITY_STATES.get(viewer.getUUID());
        }
        var update = IdentityProjectionComposer.compose(session.sessionId(), previous, visible);
        if (update.isEmpty()) {
            return;
        }
        synchronized (SefGuiRuntime.class) {
            IDENTITY_STATES.put(viewer.getUUID(), update.orElseThrow().state());
        }
        PacketDistributor.sendToPlayer(viewer, update.orElseThrow().payload());
    }

    public static void refreshIdentityProjections(MinecraftServer server) {
        refreshIdentityProjections(server, null);
    }

    private static void refreshIdentityProjections(MinecraftServer server, UUID excludedPlayerId) {
        if (!SefNetwork.enhancedGuiActive()) {
            return;
        }
        Map<UUID, Component> identities = server.getPlayerList().getPlayers().stream()
                .filter(player -> !player.getUUID().equals(excludedPlayerId))
                .sorted(java.util.Comparator.comparing(ServerPlayer::getUUID))
                .limit(SefProtocol.MAXIMUM_IDENTITY_PROJECTIONS)
                .collect(java.util.stream.Collectors.toMap(
                        ServerPlayer::getUUID,
                        player -> SEFUtilities.getFormattedPlayerName(player.getGameProfile()),
                        (left, right) -> left,
                        LinkedHashMap::new));
        for (ServerPlayer viewer : server.getPlayerList().getPlayers()) {
            if (!viewer.getUUID().equals(excludedPlayerId)) {
                sendIdentityProjection(viewer, identities);
            }
        }
    }

    private static void sendReminder(ServerPlayer player) {
        if (!ConfigHandler.config.guiReminderEnabled.get()
                || SefSessionManager.instance().session(player).isPresent()
                || !reminderAudience(player)) {
            return;
        }
        Instant loginAt;
        synchronized (SefGuiRuntime.class) {
            loginAt = LOGIN_TIMES.get(player.getUUID());
        }
        Instant now = Instant.now();
        if (loginAt == null
                || now.isBefore(loginAt.plusSeconds(
                ConfigHandler.config.guiReminderDelaySeconds.get()))) {
            return;
        }
        int reminderRevision = ConfigHandler.config.guiReminderRevision.get();
        GuiPreferenceRepository.Preference preference =
                KernelServices.guiPreferences().preference(player.getUUID());
        if (preference.dismissedReminderRevision() >= reminderRevision) {
            return;
        }
        int frequencyHours = ConfigHandler.config.guiReminderFrequencyHours.get();
        if (preference.lastReminderRevision() >= reminderRevision) {
            if (frequencyHours == 0) {
                return;
            }
            Instant last = Instant.ofEpochMilli(preference.lastReminderAtEpochMillis());
            if (Duration.between(last, now).compareTo(Duration.ofHours(frequencyHours)) < 0) {
                return;
            }
        }
        try {
            KernelServices.guiPreferences().recordReminder(player.getUUID(), reminderRevision, now);
        } catch (IllegalStateException exception) {
            ServerEssentialsForge.LOGGER.warn(
                    "[SEF] GUI reminder delivery was skipped because preference storage is unavailable");
            return;
        }
        player.sendSystemMessage(TextFormatter.stringToFormattedText(
                ConfigHandler.config.optionalClientReminder.get()));
    }

    private static boolean reminderAudience(ServerPlayer player) {
        boolean staff = PermissionService.has(player, PermissionsHandler.kernelPanel)
                || PermissionService.has(player, PermissionsHandler.sefDoctor);
        return switch (ConfigHandler.config.guiReminderAudience.get()
                .trim()
                .toLowerCase(Locale.ROOT)) {
            case "staff" -> staff;
            case "players" -> !staff;
            default -> true;
        };
    }
}
