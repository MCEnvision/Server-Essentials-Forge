package com.enviouse.sef.moderation;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.teleport.SafeTeleportService;
import com.enviouse.sef.teleport.SavedLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;

@EventBusSubscriber(modid = ServerEssentialsForge.MODID)
public final class ModerationEvents {
    private static final SafeTeleportService.Policy JAIL_POLICY =
            new SafeTeleportService.Policy(4, 256, 16, false, false, true, true, 40);
    private static final Duration RETRY_DELAY = Duration.ofSeconds(30);
    private static final Set<String> JAIL_COMMAND_ALLOWLIST = Set.of(
            "msg", "tell", "w", "whisper", "r", "reply",
            "helpop", "ac", "adminchat", "staffchat", "pchat", "teammsg", "tm",
            "rules", "info", "jails");

    private ModerationEvents() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        scheduleEnforcement(event);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        scheduleEnforcement(event);
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        scheduleEnforcement(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCommand(CommandEvent event) {
        if (!enabled()) {
            return;
        }
        ServerPlayer player = event.getParseResults().getContext().getSource().getPlayer();
        if (player == null || KernelServices.moderation().sentence(player.getUUID()).isEmpty()) {
            return;
        }
        if (!allowedWhileJailed(event.getParseResults().getReader().getString())) {
            event.setCanceled(true);
            player.sendSystemMessage(TextFormatter.stringToFormattedText(
                    "&cThat command is unavailable while jailed."));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player && jailed(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && jailed(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && jailed(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player && jailed(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemInteract(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof ServerPlayer player && jailed(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity() instanceof ServerPlayer player && jailed(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getEntity() instanceof ServerPlayer player && jailed(player)) {
            event.setCanceled(true);
        }
    }

    public static void tick(MinecraftServer server) {
        if (!enabled() || server.getTickCount() % 20 != 0) {
            return;
        }
        Instant now = Instant.now();
        ModerationRepository repository = KernelServices.moderation();
        repository.markExpiredReleasePending(now);
        repository.purgeReleased(now.minus(Duration.ofDays(1)));
        if (repository.dirty() && !persist("jail expiry transition")) {
            return;
        }
        repository.purgeExpired(now);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            reconcile(player, false);
        }
    }

    static TransitionResult completePreparedJail(
            MinecraftServer server,
            ServerPlayer actor,
            ServerPlayer player,
            ModerationRepository.Sentence sentence,
            boolean rollbackKnownFailure
    ) {
        ModerationRepository repository = KernelServices.moderation();
        ModerationRepository.Jail jail = repository.jail(sentence.jailName()).orElse(null);
        if (jail == null) {
            repository.outcomeUnknown(
                    sentence.playerId(),
                    sentence.operationId(),
                    ModerationRepository.TransitionAction.JAIL,
                    "jail destination is missing");
            persist("missing jail destination");
            return new TransitionResult(false, "Jail destination is missing.");
        }
        try {
            SafeTeleportService.TeleportResult teleported = KernelServices.safeTeleports().teleport(
                    server,
                    actor,
                    player,
                    jail.location(),
                    "jail",
                    JAIL_POLICY,
                    () -> matching(
                            sentence.playerId(),
                            sentence.operationId(),
                            ModerationRepository.TransitionAction.JAIL));
            if (!teleported.successful()) {
                if (rollbackKnownFailure) {
                    repository.rollbackJail(
                            sentence.playerId(),
                            sentence.operationId(),
                            teleported.detail());
                } else {
                    repository.outcomeUnknown(
                            sentence.playerId(),
                            sentence.operationId(),
                            ModerationRepository.TransitionAction.JAIL,
                            teleported.detail());
                }
                persist("failed jail teleport");
                return new TransitionResult(false, teleported.detail());
            }
            if (repository.activateJail(sentence.playerId(), sentence.operationId()).isEmpty()
                    || !persist("completed jail transition")) {
                repository.outcomeUnknown(
                        sentence.playerId(),
                        sentence.operationId(),
                        ModerationRepository.TransitionAction.JAIL,
                        "jail teleport completed but final storage commit failed");
                persist("unknown jail outcome");
                return new TransitionResult(false, "The teleport completed but its durable state is uncertain.");
            }
            return new TransitionResult(true, "");
        } catch (RuntimeException exception) {
            repository.outcomeUnknown(
                    sentence.playerId(),
                    sentence.operationId(),
                    ModerationRepository.TransitionAction.JAIL,
                    exception.getClass().getSimpleName());
            persist("unknown jail outcome");
            ServerEssentialsForge.LOGGER.error(
                    "[SEF] Jail teleport outcome is unknown for player {}",
                    sentence.playerId(),
                    exception);
            return new TransitionResult(false, "The jail teleport outcome is uncertain.");
        }
    }

    static TransitionResult release(
            MinecraftServer server,
            ServerPlayer actor,
            ServerPlayer player,
            ModerationRepository.Sentence sentence,
            String reason
    ) {
        ModerationRepository repository = KernelServices.moderation();
        ModerationRepository.Sentence releasing = repository.beginRelease(
                sentence.playerId(),
                sentence.operationId()).orElse(null);
        if (releasing == null) {
            return new TransitionResult(false, "The jail sentence changed.");
        }
        if (!persist("prepared jail release")) {
            repository.releasePending(
                    sentence.playerId(),
                    sentence.operationId(),
                    "release intent could not be persisted");
            return new TransitionResult(false, "The release intent could not be stored.");
        }
        SavedLocation destination = releaseDestination(server, releasing);
        try {
            SafeTeleportService.TeleportResult teleported = KernelServices.safeTeleports().teleport(
                    server,
                    actor,
                    player,
                    destination,
                    reason,
                    JAIL_POLICY,
                    () -> matching(
                            sentence.playerId(),
                            sentence.operationId(),
                            ModerationRepository.TransitionAction.RELEASE));
            if (!teleported.successful()) {
                repository.releasePending(
                        sentence.playerId(),
                        sentence.operationId(),
                        teleported.detail());
                persist("failed jail release");
                return new TransitionResult(false, teleported.detail());
            }
            if (repository.completeRelease(sentence.playerId(), sentence.operationId()).isEmpty()
                    || !persist("completed jail release")) {
                repository.outcomeUnknown(
                        sentence.playerId(),
                        sentence.operationId(),
                        ModerationRepository.TransitionAction.RELEASE,
                        "release teleport completed but final storage commit failed");
                persist("unknown jail release outcome");
                return new TransitionResult(false, "The teleport completed but its durable state is uncertain.");
            }
            return new TransitionResult(true, "");
        } catch (RuntimeException exception) {
            repository.outcomeUnknown(
                    sentence.playerId(),
                    sentence.operationId(),
                    ModerationRepository.TransitionAction.RELEASE,
                    exception.getClass().getSimpleName());
            persist("unknown jail release outcome");
            ServerEssentialsForge.LOGGER.error(
                    "[SEF] Jail release outcome is unknown for player {}",
                    sentence.playerId(),
                    exception);
            return new TransitionResult(false, "The jail release outcome is uncertain.");
        }
    }

    private static void reconcile(ServerPlayer player, boolean force) {
        ModerationRepository repository = KernelServices.moderation();
        ModerationRepository.Sentence sentence = repository.transition(player.getUUID()).orElse(null);
        if (sentence == null || sentence.state() == ModerationRepository.SentenceState.RELEASED) {
            return;
        }
        if (sentence.state() == ModerationRepository.SentenceState.ACTIVE
                && sentence.expired(Instant.now())) {
            sentence = repository.prepareRelease(player.getUUID()).orElse(sentence);
            if (!persist("login jail expiry transition")) {
                return;
            }
        }
        if (!force
                && !sentence.lastFailure().isBlank()
                && Instant.now().isBefore(sentence.updatedAt().plus(RETRY_DELAY))) {
            return;
        }
        switch (sentence.state()) {
            case JAILING -> completePreparedJail(
                    player.getServer(), null, player, sentence, false);
            case ACTIVE -> enforceSentence(player, force, sentence);
            case RELEASE_PENDING, RELEASING -> release(
                    player.getServer(), null, player, sentence, "jail sentence release");
            case OUTCOME_UNKNOWN -> {
                if (sentence.pendingAction() == ModerationRepository.TransitionAction.JAIL) {
                    completePreparedJail(player.getServer(), null, player, sentence, false);
                } else if (sentence.pendingAction() == ModerationRepository.TransitionAction.RELEASE) {
                    release(player.getServer(), null, player, sentence, "jail transition reconciliation");
                }
            }
            case RELEASED -> {
            }
        }
    }

    private static void enforceSentence(
            ServerPlayer player,
            boolean force,
            ModerationRepository.Sentence sentence
    ) {
        var jail = KernelServices.moderation().jail(sentence.jailName());
        if (jail.isEmpty()) {
            return;
        }
        SavedLocation destination = jail.orElseThrow().location();
        boolean wrongDimension = !player.serverLevel().dimension().location().toString()
                .equals(destination.dimensionId());
        double distance = wrongDimension
                ? Double.POSITIVE_INFINITY
                : player.distanceToSqr(destination.x(), destination.y(), destination.z());
        if (!force && distance <= 256.0D) {
            return;
        }
        KernelServices.safeTeleports().teleport(
                player.getServer(),
                null,
                player,
                destination,
                "jail enforcement",
                JAIL_POLICY,
                () -> KernelServices.moderation().transition(player.getUUID())
                        .map(current -> current.operationId().equals(sentence.operationId())
                                && current.state() == ModerationRepository.SentenceState.ACTIVE)
                        .orElse(false));
    }

    private static boolean jailed(ServerPlayer player) {
        return enabled() && KernelServices.moderation().sentence(player.getUUID()).isPresent();
    }

    static boolean allowedWhileJailed(String input) {
        if (input == null) {
            return false;
        }
        String candidate = input.strip();
        while (candidate.startsWith("/")) {
            candidate = candidate.substring(1).stripLeading();
        }
        int separator = 0;
        while (separator < candidate.length()
                && !Character.isWhitespace(candidate.charAt(separator))) {
            separator++;
        }
        String root = candidate.substring(0, separator).toLowerCase(Locale.ROOT);
        int namespace = root.indexOf(':');
        if (namespace >= 0) {
            root = root.substring(namespace + 1);
        }
        return JAIL_COMMAND_ALLOWLIST.contains(root);
    }

    private static void scheduleEnforcement(PlayerEvent event) {
        if (!enabled() || !(event.getEntity() instanceof ServerPlayer player)
                || player.getServer() == null) {
            return;
        }
        player.getServer().execute(() -> {
            if (player.isAlive()
                    && !player.hasDisconnected()
                    && player.getServer().getPlayerList().getPlayer(player.getUUID()) == player) {
                reconcile(player, true);
            }
        });
    }

    private static SavedLocation releaseDestination(
            MinecraftServer server,
            ModerationRepository.Sentence sentence
    ) {
        if (sentence.releaseLocation() != null) {
            return sentence.releaseLocation();
        }
        ServerLevel level = server.overworld();
        var position = level.getSharedSpawnPos();
        return new SavedLocation(
                level.dimension().location().toString(),
                position.getX() + 0.5D,
                position.getY(),
                position.getZ() + 0.5D,
                level.getSharedSpawnAngle(),
                0.0F);
    }

    private static boolean matching(
            java.util.UUID playerId,
            java.util.UUID operationId,
            ModerationRepository.TransitionAction action
    ) {
        return KernelServices.moderation().transition(playerId)
                .map(current -> current.operationId().equals(operationId)
                        && current.pendingAction() == action)
                .orElse(false);
    }

    static boolean persist(String operation) {
        try {
            KernelServices.moderation().flush();
            return true;
        } catch (IOException | RuntimeException exception) {
            ServerEssentialsForge.LOGGER.error(
                    "[SEF] Moderation storage failed during {}",
                    operation,
                    exception);
            return false;
        }
    }

    private static boolean enabled() {
        return ConfigHandler.config.enableModerationEssentials.get()
                && ConfigHandler.config.enableJails.get();
    }

    record TransitionResult(boolean successful, String detail) {
    }
}
