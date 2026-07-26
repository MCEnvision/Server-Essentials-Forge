package com.enviouse.sef.identity;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.config.PlayerData;
import com.enviouse.sef.storage.CoalescedPersistenceWorker;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class PlayerProfileRepository {
    private static final Duration FLUSH_TIMEOUT = Duration.ofSeconds(5);

    private File playerDirectory;
    private CoalescedPersistenceWorker writer;

    public void load(File playerDirectory) {
        File requested = Objects.requireNonNull(playerDirectory, "playerDirectory");
        File previous;
        synchronized (this) {
            previous = this.playerDirectory;
        }
        if (previous != null && sameDirectory(previous, requested)) {
            PlayerData.loadFromDir(requested);
            return;
        }
        if (previous != null && !sameDirectory(previous, requested)) {
            shutdown();
        }
        synchronized (this) {
            this.playerDirectory = requested;
            writer = new CoalescedPersistenceWorker(
                    "sef-player-profiles",
                    exception -> ServerEssentialsForge.LOGGER.error(
                            "[SEF] Failed to save integrated player identities",
                            exception));
        }
        PlayerData.loadFromDir(requested);
    }

    public boolean flush(File playerDirectory) {
        File requested = Objects.requireNonNull(playerDirectory, "playerDirectory");
        synchronized (this) {
            if (this.playerDirectory == null || !sameDirectory(this.playerDirectory, requested)) {
                load(requested);
            }
        }
        return flush();
    }

    public boolean flush() {
        CoalescedPersistenceWorker active;
        synchronized (this) {
            if (playerDirectory == null) {
                return true;
            }
            if (!queueSnapshot()) {
                return false;
            }
            active = writer;
        }
        return active != null && active.flush(FLUSH_TIMEOUT);
    }

    public boolean remember(UUID playerId, String authenticatedUsername) {
        return rememberDeferred(playerId, authenticatedUsername);
    }

    public synchronized boolean rememberDeferred(UUID playerId, String authenticatedUsername) {
        if (writer == null) {
            return false;
        }
        String previous = PlayerData.getUsername(playerId);
        if (!PlayerData.rememberProfileInMemory(playerId, authenticatedUsername)) {
            return false;
        }
        if (Objects.equals(previous, authenticatedUsername)) {
            return true;
        }
        return queueSnapshot();
    }

    public synchronized void requestFlush() {
        queueSnapshot();
    }

    public boolean shutdown() {
        CoalescedPersistenceWorker active;
        boolean queued;
        synchronized (this) {
            if (playerDirectory == null) {
                return true;
            }
            queued = queueSnapshot();
            active = writer;
            writer = null;
            playerDirectory = null;
        }
        boolean stopped = active != null && active.shutdown(FLUSH_TIMEOUT);
        boolean saved = queued && stopped;
        PlayerData.unload();
        return saved;
    }

    public synchronized boolean setNickname(UUID playerId, String nickname) {
        if (writer == null) {
            return false;
        }
        String previous = PlayerData.getNickname(playerId);
        if (!PlayerData.setNicknameInMemory(playerId, nickname)) {
            return false;
        }
        if (Objects.equals(previous, nickname)) {
            return true;
        }
        return queueSnapshot();
    }

    public Optional<UUID> resolve(String identity, boolean includeNicknames) {
        return PlayerData.findIdentity(identity, includeNicknames);
    }

    public Optional<Profile> find(UUID playerId) {
        return PlayerData.profile(playerId)
                .map(snapshot -> new Profile(
                        snapshot.playerId(),
                        snapshot.authenticatedUsername(),
                        snapshot.nickname(),
                        snapshot.updatedAt()));
    }

    public List<Profile> snapshot() {
        return PlayerData.profiles().stream()
                .map(snapshot -> new Profile(
                        snapshot.playerId(),
                        snapshot.authenticatedUsername(),
                        snapshot.nickname(),
                        snapshot.updatedAt()))
                .toList();
    }

    public PlayerData.ProfileDiagnostic diagnostic() {
        return PlayerData.diagnostic();
    }

    private boolean queueSnapshot() {
        CoalescedPersistenceWorker active = writer;
        if (active == null) {
            return false;
        }
        return active.submit(() -> {
            Optional<PlayerData.PersistenceSnapshot> snapshot = PlayerData.persistenceSnapshot();
            if (snapshot.isEmpty() || !PlayerData.persistSnapshot(snapshot.orElseThrow())) {
                throw new IOException("Integrated player identity snapshot failed");
            }
        });
    }

    private static boolean sameDirectory(File first, File second) {
        return first.toPath().toAbsolutePath().normalize()
                .equals(second.toPath().toAbsolutePath().normalize());
    }

    public record Profile(
            UUID playerId,
            String authenticatedUsername,
            String nickname,
            String updatedAt
    ) {
    }
}
