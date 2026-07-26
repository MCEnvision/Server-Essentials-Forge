package com.enviouse.sef.storage;

import com.enviouse.sef.ServerEssentialsForge;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class StorageExportService {
    private static ThreadPoolExecutor executor;

    private StorageExportService() {
    }

    public static synchronized void start() {
        shutdown();
        executor = new ThreadPoolExecutor(
                1,
                1,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(8),
                runnable -> Thread.ofPlatform().daemon(true).name("sef-storage-export").unstarted(runnable),
                new ThreadPoolExecutor.AbortPolicy());
    }

    public static synchronized boolean submit(Runnable task) {
        if (executor == null || executor.isShutdown()) return false;
        try {
            executor.execute(task);
            return true;
        } catch (RejectedExecutionException exception) {
            return false;
        }
    }

    public static synchronized void shutdown() {
        ThreadPoolExecutor current = executor;
        executor = null;
        if (current == null) return;
        current.shutdown();
        try {
            if (!current.awaitTermination(5L, TimeUnit.SECONDS)) {
                int dropped = current.shutdownNow().size();
                ServerEssentialsForge.LOGGER.error(
                        "[SEF] Storage export shutdown discarded {} queued task or tasks",
                        dropped);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            current.shutdownNow();
        }
    }
}
