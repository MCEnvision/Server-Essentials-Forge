package com.enviouse.sef.storage.repository;

import com.enviouse.sef.ServerEssentialsForge;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class StorageCoordinator {
    private final Map<String, StorageRepository> repositories = new LinkedHashMap<>();
    private Path managedRoot;
    private boolean started;
    private boolean recoveryMode;

    public synchronized void register(StorageRepository repository) {
        Objects.requireNonNull(repository, "repository");
        if (started) {
            throw new IllegalStateException("Cannot register storage after startup");
        }
        if (repositories.putIfAbsent(repository.id(), repository) != null) {
            throw new IllegalStateException("Duplicate repository " + repository.id());
        }
    }

    public synchronized List<StorageRepository.LoadResult> start(Path root) {
        if (started) {
            throw new IllegalStateException("Storage coordinator already started");
        }
        managedRoot = Objects.requireNonNull(root, "root").toAbsolutePath().normalize();
        recoveryMode = false;
        List<StorageRepository.LoadResult> results = new ArrayList<>();
        for (StorageRepository repository : repositories.values()) {
            StorageRepository.LoadResult result = repository.load(managedRoot);
            results.add(result);
            if (result.state() == StorageRepository.RepositoryState.RECOVERY
                    || result.state() == StorageRepository.RepositoryState.UNSUPPORTED
                    || result.state() == StorageRepository.RepositoryState.ERROR) {
                recoveryMode = true;
            }
        }
        started = true;
        return List.copyOf(results);
    }

    public synchronized FlushResult flush() {
        int flushed = 0;
        List<String> failed = new ArrayList<>();
        for (StorageRepository repository : repositories.values()) {
            if (!repository.dirty()) {
                continue;
            }
            try {
                repository.flush();
                flushed++;
            } catch (IOException | RuntimeException exception) {
                failed.add(repository.id());
                recoveryMode = true;
                ServerEssentialsForge.LOGGER.error(
                        "[SEF] Failed to flush repository {}",
                        repository.id(),
                        exception);
            }
        }
        return new FlushResult(flushed, List.copyOf(failed), failed.isEmpty());
    }

    public synchronized FlushResult shutdown() {
        FlushResult result = flush();
        started = false;
        return result;
    }

    public synchronized boolean recoveryMode() {
        return recoveryMode;
    }

    public synchronized boolean started() {
        return started;
    }

    public synchronized List<Diagnostic> diagnostics() {
        return repositories.values().stream()
                .map(repository -> new Diagnostic(
                        repository.id(),
                        repository.domain(),
                        repository.schemaVersion(),
                        repository.path(),
                        repository.state(),
                        repository.dirty()))
                .sorted(Comparator.comparing(Diagnostic::id))
                .toList();
    }

    public synchronized int size() {
        return repositories.size();
    }

    public synchronized Path managedRoot() {
        return managedRoot;
    }

    public record FlushResult(int flushed, List<String> failedRepositoryIds, boolean successful) {
    }

    public record Diagnostic(
            String id,
            String domain,
            int schemaVersion,
            Path path,
            StorageRepository.RepositoryState state,
            boolean dirty
    ) {
    }
}
