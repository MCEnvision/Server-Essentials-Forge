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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class StorageCoordinator {
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 5L;

    private final Map<String, StorageRepository> repositories = new LinkedHashMap<>();
    private final Object flushMonitor = new Object();
    private final int flushIntervalSeconds;
    private Path managedRoot;
    private boolean started;
    private boolean recoveryMode;
    private ExecutorService pendingShutdownExecutor;
    private ScheduledExecutorService periodicFlushExecutor;

    public StorageCoordinator() {
        this(30);
    }

    public StorageCoordinator(int flushIntervalSeconds) {
        if (flushIntervalSeconds < 1 || flushIntervalSeconds > 600) {
            throw new IllegalArgumentException("Flush interval must be between 1 and 600 seconds");
        }
        this.flushIntervalSeconds = flushIntervalSeconds;
    }

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
        if (pendingShutdownExecutor != null) {
            if (!pendingShutdownExecutor.isTerminated()) {
                throw new IllegalStateException("Previous storage shutdown is still active");
            }
            pendingShutdownExecutor = null;
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
        periodicFlushExecutor = Executors.newSingleThreadScheduledExecutor(task ->
                Thread.ofPlatform().daemon(true).name("sef-storage-flush").unstarted(task));
        periodicFlushExecutor.scheduleWithFixedDelay(
                this::periodicFlush,
                flushIntervalSeconds,
                flushIntervalSeconds,
                TimeUnit.SECONDS);
        return List.copyOf(results);
    }

    public FlushResult flush() {
        synchronized (flushMonitor) {
            return flushLocked();
        }
    }

    private FlushResult flushLocked() {
        List<StorageRepository> snapshot;
        synchronized (this) {
            snapshot = List.copyOf(repositories.values());
        }
        int flushed = 0;
        List<String> failed = new ArrayList<>();
        for (StorageRepository repository : snapshot) {
            if (!repository.dirty()) {
                continue;
            }
            try {
                repository.flush();
                flushed++;
            } catch (IOException | RuntimeException exception) {
                failed.add(repository.id());
                synchronized (this) {
                    recoveryMode = true;
                }
                ServerEssentialsForge.LOGGER.error(
                        "[SEF] Failed to flush repository {}",
                        repository.id(),
                        exception);
            }
        }
        return new FlushResult(flushed, List.copyOf(failed), failed.isEmpty());
    }

    public FlushResult shutdown() {
        stopPeriodicFlush();
        List<String> pendingRepositoryIds;
        var executor = Executors.newSingleThreadExecutor(task ->
                Thread.ofPlatform().daemon(true).name("sef-storage-shutdown").unstarted(task));
        Future<FlushResult> future;
        synchronized (this) {
            pendingRepositoryIds = repositories.values().stream()
                    .filter(StorageRepository::dirty)
                    .map(StorageRepository::id)
                    .sorted()
                    .toList();
            future = executor.submit(this::flush);
            pendingShutdownExecutor = executor;
        }
        executor.shutdown();

        FlushResult result;
        try {
            result = future.get(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            future.cancel(true);
            result = failedShutdown(pendingRepositoryIds, "interrupted", exception);
        } catch (TimeoutException exception) {
            future.cancel(true);
            result = failedShutdown(pendingRepositoryIds, "timed out", exception);
        } catch (ExecutionException exception) {
            result = failedShutdown(pendingRepositoryIds, "failed", exception.getCause());
        } finally {
            executor.shutdownNow();
            try {
                executor.awaitTermination(100L, TimeUnit.MILLISECONDS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
            synchronized (this) {
                started = false;
                if (executor.isTerminated()) {
                    pendingShutdownExecutor = null;
                }
            }
        }
        return result;
    }

    private void periodicFlush() {
        try {
            flush();
        } catch (RuntimeException exception) {
            synchronized (this) {
                recoveryMode = true;
            }
            ServerEssentialsForge.LOGGER.error("[SEF] Periodic repository flush failed", exception);
        }
    }

    private void stopPeriodicFlush() {
        ScheduledExecutorService executor;
        synchronized (this) {
            executor = periodicFlushExecutor;
            periodicFlushExecutor = null;
        }
        if (executor == null) {
            return;
        }
        executor.shutdownNow();
        try {
            executor.awaitTermination(250L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
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

    private FlushResult failedShutdown(
            List<String> pendingRepositoryIds,
            String reason,
            Throwable exception
    ) {
        synchronized (this) {
            recoveryMode = true;
        }
        ServerEssentialsForge.LOGGER.error(
                "[SEF] Storage shutdown flush {} for repositories {}",
                reason,
                pendingRepositoryIds,
                exception);
        return new FlushResult(0, pendingRepositoryIds, false);
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
