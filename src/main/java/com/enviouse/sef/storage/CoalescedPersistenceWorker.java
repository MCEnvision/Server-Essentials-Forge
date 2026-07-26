package com.enviouse.sef.storage;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class CoalescedPersistenceWorker {
    private static final System.Logger LOGGER = System.getLogger("sef.persistence");

    @FunctionalInterface
    public interface WriteOperation {
        void run() throws Exception;
    }

    private final ExecutorService executor;
    private final Consumer<Exception> failureHandler;
    private final Object monitor = new Object();

    private WriteOperation latestWrite;
    private long submittedRevision;
    private long completedRevision;
    private long successfulRevision;
    private long failedRevision;
    private boolean drainScheduled;
    private boolean accepting = true;

    public CoalescedPersistenceWorker(String threadName, Consumer<Exception> failureHandler) {
        Objects.requireNonNull(threadName, "threadName");
        this.failureHandler = Objects.requireNonNull(failureHandler, "failureHandler");
        this.executor = Executors.newSingleThreadExecutor(task -> {
            Thread thread = new Thread(task, threadName);
            thread.setDaemon(true);
            return thread;
        });
    }

    public boolean submit(WriteOperation operation) {
        Objects.requireNonNull(operation, "operation");
        synchronized (monitor) {
            if (!accepting) {
                return false;
            }
            latestWrite = operation;
            submittedRevision++;
            if (!drainScheduled) {
                drainScheduled = true;
                executor.execute(this::drain);
            }
            return true;
        }
    }

    public boolean flush(Duration timeout) {
        Objects.requireNonNull(timeout, "timeout");
        long targetRevision;
        synchronized (monitor) {
            targetRevision = submittedRevision;
        }
        return awaitRevision(targetRevision, timeout);
    }

    public boolean shutdown(Duration timeout) {
        Objects.requireNonNull(timeout, "timeout");
        long deadline = deadline(timeout);
        long targetRevision;
        synchronized (monitor) {
            accepting = false;
            targetRevision = submittedRevision;
        }

        boolean flushed = awaitRevision(targetRevision, remaining(deadline));
        executor.shutdown();
        boolean terminated = awaitTermination(remaining(deadline));
        if (!terminated) {
            executor.shutdownNow();
        }
        return flushed && terminated;
    }

    private void drain() {
        while (true) {
            WriteOperation operation;
            long targetRevision;
            synchronized (monitor) {
                if (completedRevision >= submittedRevision) {
                    drainScheduled = false;
                    monitor.notifyAll();
                    return;
                }
                operation = latestWrite;
                targetRevision = submittedRevision;
            }

            try {
                operation.run();
                synchronized (monitor) {
                    successfulRevision = Math.max(successfulRevision, targetRevision);
                }
            } catch (Exception exception) {
                synchronized (monitor) {
                    failedRevision = Math.max(failedRevision, targetRevision);
                }
                try {
                    failureHandler.accept(exception);
                } catch (RuntimeException callbackException) {
                    LOGGER.log(
                            System.Logger.Level.ERROR,
                            "Persistence failure handler failed",
                            callbackException);
                }
            } finally {
                synchronized (monitor) {
                    completedRevision = Math.max(completedRevision, targetRevision);
                    monitor.notifyAll();
                }
            }
        }
    }

    private boolean awaitRevision(long targetRevision, Duration timeout) {
        long deadline = deadline(timeout);
        synchronized (monitor) {
            while (completedRevision < targetRevision) {
                long remainingNanos = deadline - System.nanoTime();
                if (remainingNanos <= 0L) {
                    return false;
                }
                try {
                    TimeUnit.NANOSECONDS.timedWait(monitor, remainingNanos);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            return failedRevision <= successfulRevision;
        }
    }

    private boolean awaitTermination(Duration timeout) {
        try {
            return executor.awaitTermination(Math.max(0L, timeout.toNanos()), TimeUnit.NANOSECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static long deadline(Duration timeout) {
        long timeoutNanos = Math.max(0L, timeout.toNanos());
        long now = System.nanoTime();
        long deadline = now + timeoutNanos;
        return deadline < now ? Long.MAX_VALUE : deadline;
    }

    private static Duration remaining(long deadline) {
        return Duration.ofNanos(Math.max(0L, deadline - System.nanoTime()));
    }
}
