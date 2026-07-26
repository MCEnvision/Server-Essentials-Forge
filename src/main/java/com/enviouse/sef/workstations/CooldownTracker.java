package com.enviouse.sef.workstations;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.LongSupplier;

public final class CooldownTracker {
    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    private final LongSupplier nanoTime;
    private final Map<UUID, Map<String, Long>> deadlines = new HashMap<>();
    private int usesSincePrune;

    public CooldownTracker() {
        this(System::nanoTime);
    }

    CooldownTracker(LongSupplier nanoTime) {
        this.nanoTime = nanoTime;
    }

    public synchronized Result tryUse(UUID playerId, String command, int cooldownSeconds) {
        long now = nanoTime.getAsLong();
        if (++usesSincePrune >= 64) {
            pruneExpired(now);
            usesSincePrune = 0;
        }
        if (cooldownSeconds <= 0) {
            return Result.allow();
        }

        Map<String, Long> playerDeadlines = deadlines.computeIfAbsent(playerId, ignored -> new HashMap<>());
        long deadline = playerDeadlines.getOrDefault(command, 0L);
        if (deadline > now) {
            long remainingNanos = deadline - now;
            long remainingSeconds = Math.max(1L, (remainingNanos + NANOS_PER_SECOND - 1L) / NANOS_PER_SECOND);
            return Result.block(remainingSeconds);
        }

        playerDeadlines.put(command, now + cooldownSeconds * NANOS_PER_SECOND);
        return Result.allow();
    }

    public synchronized void clear() {
        deadlines.clear();
        usesSincePrune = 0;
    }

    synchronized int trackedPlayers() {
        return deadlines.size();
    }

    private void pruneExpired(long now) {
        deadlines.values().forEach(playerDeadlines ->
                playerDeadlines.entrySet().removeIf(entry -> entry.getValue() <= now));
        deadlines.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    public record Result(boolean allowed, long remainingSeconds) {
        private static Result allow() {
            return new Result(true, 0L);
        }

        private static Result block(long remainingSeconds) {
            return new Result(false, remainingSeconds);
        }
    }
}
