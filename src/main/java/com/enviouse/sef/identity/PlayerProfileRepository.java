package com.enviouse.sef.identity;

import com.enviouse.sef.config.PlayerData;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class PlayerProfileRepository {
    private File playerDirectory;

    public synchronized void load(File playerDirectory) {
        this.playerDirectory = Objects.requireNonNull(playerDirectory, "playerDirectory");
        PlayerData.loadFromDir(this.playerDirectory);
    }

    public synchronized void flush(File playerDirectory) {
        this.playerDirectory = Objects.requireNonNull(playerDirectory, "playerDirectory");
        PlayerData.saveToDir(this.playerDirectory);
    }

    public synchronized void flush() {
        if (playerDirectory != null) {
            PlayerData.saveToDir(playerDirectory);
        }
    }

    public boolean remember(UUID playerId, String authenticatedUsername) {
        return PlayerData.rememberProfile(playerId, authenticatedUsername);
    }

    public boolean setNickname(UUID playerId, String nickname) {
        return PlayerData.setNickname(playerId, nickname);
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

    public record Profile(
            UUID playerId,
            String authenticatedUsername,
            String nickname,
            String updatedAt
    ) {
    }
}
