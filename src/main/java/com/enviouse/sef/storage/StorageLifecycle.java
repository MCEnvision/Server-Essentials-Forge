package com.enviouse.sef.storage;

import com.enviouse.sef.storage.repository.StorageRepository;

import java.nio.file.Path;
import java.util.Objects;

public final class StorageLifecycle {
    private StorageLifecycle() {
    }

    public static StorageRepository.RepositoryState stateFor(Path path) {
        Path normalized = Objects.requireNonNull(path, "path").toAbsolutePath().normalize();
        return StorageService.statuses().stream()
                .filter(status -> status.path().equals(normalized))
                .findFirst()
                .map(status -> switch (status.state()) {
                    case "missing" -> StorageRepository.RepositoryState.MISSING;
                    case "unsupported" -> StorageRepository.RepositoryState.UNSUPPORTED;
                    case "quarantined", "quarantine failed", "rejected" ->
                            StorageRepository.RepositoryState.RECOVERY;
                    default -> StorageRepository.RepositoryState.ERROR;
                })
                .orElse(StorageRepository.RepositoryState.ERROR);
    }

    public static boolean writable(StorageRepository.RepositoryState state) {
        return state == StorageRepository.RepositoryState.MISSING
                || state == StorageRepository.RepositoryState.READY;
    }
}
