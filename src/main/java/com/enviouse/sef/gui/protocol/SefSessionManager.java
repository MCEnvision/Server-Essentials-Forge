package com.enviouse.sef.gui.protocol;

import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.permissions.PermissionService;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class SefSessionManager {
    private static final Duration NEGOTIATION_LIFETIME = Duration.ofMinutes(2);
    private static final int MAXIMUM_PENDING = 4096;
    private static final int MAXIMUM_ACTIVE = 100_000;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final SefSessionManager INSTANCE = new SefSessionManager(Clock.systemUTC());

    private final Clock clock;
    private final Map<Connection, Pending> pending = new IdentityHashMap<>();
    private final Map<UUID, Session> sessions = new LinkedHashMap<>();

    SefSessionManager(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public static SefSessionManager instance() {
        return INSTANCE;
    }

    public synchronized SefPayloads.ServerHello begin(Connection connection) {
        Objects.requireNonNull(connection, "connection");
        prune();
        if (pending.size() >= MAXIMUM_PENDING) {
            pending.entrySet().removeIf(entry -> !entry.getKey().isConnected());
        }
        if (pending.size() >= MAXIMUM_PENDING) {
            throw new IllegalStateException("Enhanced client negotiation capacity reached");
        }
        Pending state = new Pending(
                UUID.randomUUID(),
                RANDOM.nextLong(),
                clock.instant(),
                0L,
                false);
        pending.put(connection, state);
        return new SefPayloads.ServerHello(
                state.negotiationId(),
                state.nonce(),
                SefProtocol.MAJOR,
                SefProtocol.MINOR,
                SefProtocol.SERVER_FEATURES);
    }

    public synchronized boolean acknowledge(Connection connection, SefPayloads.ClientHello hello) {
        Objects.requireNonNull(connection, "connection");
        Objects.requireNonNull(hello, "hello");
        Pending current = pending.get(connection);
        if (current == null
                || expired(current)
                || !current.negotiationId().equals(hello.negotiationId())
                || current.nonce() != hello.nonce()
                || !hello.compatible()
                || !SefProtocol.compatible(hello.protocolMajor())) {
            pending.remove(connection);
            return false;
        }
        long negotiated = hello.features() & SefProtocol.SERVER_FEATURES;
        pending.put(connection, new Pending(
                current.negotiationId(),
                current.nonce(),
                current.createdAt(),
                negotiated,
                true));
        return true;
    }

    public synchronized Optional<SessionView> bind(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        if (!SefNetwork.enhancedGuiActive()) {
            pending.remove(player.connection.getConnection());
            sessions.remove(player.getUUID());
            return Optional.empty();
        }
        Pending negotiated = pending.remove(player.connection.getConnection());
        if (negotiated == null || !negotiated.accepted() || expired(negotiated)) {
            sessions.remove(player.getUUID());
            return Optional.empty();
        }
        if (sessions.size() >= MAXIMUM_ACTIVE && !sessions.containsKey(player.getUUID())) {
            return Optional.empty();
        }
        boolean authorized = PermissionService.has(player, PermissionsHandler.kernelGui);
        long effectiveFeatures = effectiveFeatures(player, negotiated.features());
        UUID sessionId = UUID.randomUUID();
        Session created = new Session(
                sessionId,
                player.getUUID(),
                negotiated.features(),
                effectiveFeatures,
                authorized,
                1L,
                0L,
                clock.instant(),
                new SessionRequestGuard(
                        clock,
                        sessionId,
                        effectiveFeatures,
                        authorized));
        sessions.put(player.getUUID(), created);
        sendState(player, created);
        return Optional.of(created.view());
    }

    public synchronized Optional<SessionView> session(ServerPlayer player) {
        Session session = sessions.get(player.getUUID());
        return session == null ? Optional.empty() : Optional.of(session.view());
    }

    public synchronized RequestDecision acceptRequest(
            ServerPlayer player,
            UUID sessionId,
            long sequence,
            SefProtocol.Feature requiredFeature
    ) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(requiredFeature, "requiredFeature");
        Session current = sessions.get(player.getUUID());
        if (current == null) {
            return RequestDecision.NO_SESSION;
        }
        RequestDecision decision = current.requestGuard().accept(
                sessionId,
                sequence,
                requiredFeature,
                ConfigHandler.config.guiPanelRequestsPerSecond.get());
        if (decision == RequestDecision.ACCEPTED) {
            current.setLastSequence(current.requestGuard().lastSequence());
            current.setLastActivity(clock.instant());
        }
        return decision;
    }

    public synchronized void refresh(ServerPlayer player) {
        Session current = sessions.get(player.getUUID());
        if (current == null) {
            return;
        }
        boolean authorized = PermissionService.has(player, PermissionsHandler.kernelGui);
        long features = effectiveFeatures(player, current.negotiatedFeatures());
        if (authorized != current.authorized() || features != current.features()) {
            current.setAuthorized(authorized);
            current.setFeatures(features);
            current.requestGuard().update(features, authorized);
            current.setRevision(current.revision() + 1L);
            sendState(player, current);
        }
    }

    public synchronized void logout(UUID playerId) {
        sessions.remove(playerId);
    }

    public synchronized void clear() {
        pending.clear();
        sessions.clear();
    }

    public synchronized int activeCount() {
        return sessions.size();
    }

    public synchronized int pendingCount() {
        prune();
        return pending.size();
    }

    private void sendState(ServerPlayer player, Session session) {
        if (!player.connection.hasChannel(SefPayloads.SessionState.TYPE)) {
            sessions.remove(player.getUUID());
            return;
        }
        PacketDistributor.sendToPlayer(player, new SefPayloads.SessionState(
                session.sessionId(),
                session.revision(),
                SefProtocol.MAJOR,
                SefProtocol.MINOR,
                session.features(),
                session.authorized()));
    }

    private static long effectiveFeatures(ServerPlayer player, long negotiatedFeatures) {
        long result = negotiatedFeatures;
        var preferences = com.enviouse.sef.kernel.KernelServices.guiPreferences().preference(player.getUUID());
        if (preferences.presentationMode()
                == com.enviouse.sef.gui.GuiPreferenceRepository.PresentationMode.COMMAND) {
            return 0L;
        }
        if (!preferences.pauseButtonVisible()) {
            result &= ~SefProtocol.Feature.PAUSE_BUTTON.flag();
        }
        if (!preferences.hudEnabled()
                || !PermissionService.has(player, PermissionsHandler.kernelHud)) {
            result &= ~SefProtocol.Feature.HUD.flag();
        }
        if (!PermissionService.has(player, PermissionsHandler.kernelPanel)) {
            result &= ~SefProtocol.Feature.STAFF_OVERVIEW.flag();
        }
        if (!PermissionService.has(player, PermissionsHandler.kernelEditor)) {
            result &= ~SefProtocol.Feature.PANEL_EDITOR.flag();
        }
        return result;
    }

    private void prune() {
        pending.entrySet().removeIf(entry -> expired(entry.getValue()) || !entry.getKey().isConnected());
    }

    private boolean expired(Pending state) {
        return !state.createdAt().plus(NEGOTIATION_LIFETIME).isAfter(clock.instant());
    }

    private record Pending(
            UUID negotiationId,
            long nonce,
            Instant createdAt,
            long features,
            boolean accepted
    ) {
    }

    private static final class Session {
        private final UUID sessionId;
        private final UUID playerId;
        private final long negotiatedFeatures;
        private long features;
        private boolean authorized;
        private long revision;
        private long lastSequence;
        private Instant lastActivity;
        private final SessionRequestGuard requestGuard;

        private Session(
                UUID sessionId,
                UUID playerId,
                long negotiatedFeatures,
                long features,
                boolean authorized,
                long revision,
                long lastSequence,
                Instant lastActivity,
                SessionRequestGuard requestGuard
        ) {
            this.sessionId = sessionId;
            this.playerId = playerId;
            this.negotiatedFeatures = negotiatedFeatures;
            this.features = features;
            this.authorized = authorized;
            this.revision = revision;
            this.lastSequence = lastSequence;
            this.lastActivity = lastActivity;
            this.requestGuard = requestGuard;
        }

        private SessionView view() {
            return new SessionView(
                    sessionId,
                    playerId,
                    features,
                    authorized,
                    revision,
                    lastSequence,
                    lastActivity);
        }

        private UUID sessionId() {
            return sessionId;
        }

        private long features() {
            return features;
        }

        private long negotiatedFeatures() {
            return negotiatedFeatures;
        }

        private void setFeatures(long features) {
            this.features = features;
        }

        private boolean authorized() {
            return authorized;
        }

        private void setAuthorized(boolean authorized) {
            this.authorized = authorized;
        }

        private long revision() {
            return revision;
        }

        private void setRevision(long revision) {
            this.revision = revision;
        }

        private long lastSequence() {
            return lastSequence;
        }

        private void setLastSequence(long lastSequence) {
            this.lastSequence = lastSequence;
        }

        private void setLastActivity(Instant lastActivity) {
            this.lastActivity = lastActivity;
        }

        private SessionRequestGuard requestGuard() {
            return requestGuard;
        }

    }

    public record SessionView(
            UUID sessionId,
            UUID playerId,
            long features,
            boolean authorized,
            long revision,
            long lastSequence,
            Instant lastActivity
    ) {
        public boolean supports(SefProtocol.Feature feature) {
            return authorized && feature.present(features);
        }
    }

    public enum RequestDecision {
        ACCEPTED,
        NO_SESSION,
        NOT_AUTHORIZED,
        REPLAY,
        RATE_LIMITED
    }
}
