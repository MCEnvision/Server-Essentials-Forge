package com.enviouse.sef.kernel.policy;

import com.enviouse.sef.kernel.ActionResult;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class CooldownService {
    private static final Duration HARD_MAXIMUM = Duration.ofDays(365);
    private static final long MAXIMUM_CLOCK_ROLLBACK_MILLIS = Duration.ofMinutes(5).toMillis();

    private final Clock clock;
    private final Map<Key, Long> expiryEpochMillis = new ConcurrentHashMap<>();
    private final Map<String, String> canonicalActions = new ConcurrentHashMap<>();
    private final List<Runnable> changeListeners = new CopyOnWriteArrayList<>();
    private volatile long lastObservedEpochMillis;
    private int operationsSincePrune;

    public CooldownService() {
        this(Clock.systemUTC());
    }

    CooldownService(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.lastObservedEpochMillis = clock.millis();
    }

    public synchronized void registerAlias(String entryActionId, String canonicalActionId) {
        String entry = normalize(entryActionId);
        String canonical = normalize(canonicalActionId);
        if (entry.equals(canonical)) {
            return;
        }
        if (!canonicalActions.containsKey(entry) && canonicalActions.size() >= 8192) {
            throw new IllegalStateException("Cooldown alias limit reached");
        }
        String cursor = canonical;
        for (int depth = 0; depth < 8; depth++) {
            if (cursor.equals(entry)) {
                throw new IllegalArgumentException("Cooldown alias cycle detected");
            }
            String next = canonicalActions.get(cursor);
            if (next == null) {
                break;
            }
            cursor = next;
            if (depth == 7) {
                throw new IllegalArgumentException("Cooldown alias chain exceeds limit");
            }
        }
        String existing = canonicalActions.putIfAbsent(entry, canonical);
        if (existing != null && !existing.equals(canonical)) {
            throw new IllegalStateException("Cooldown alias has multiple canonical actions");
        }
    }

    public synchronized Decision tryAcquire(UUID playerId, String actionId, Duration duration, boolean bypass) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(duration, "duration");
        String canonical = canonical(actionId);
        if (duration.isNegative() || duration.compareTo(HARD_MAXIMUM) > 0) {
            return new Decision(false, canonical, 0L, ActionResult.ReasonCode.INVALID_INPUT, false);
        }
        long now = safeNow();
        if (++operationsSincePrune >= 64) {
            pruneExpired(now);
            operationsSincePrune = 0;
        }
        if (bypass || duration.isZero()) {
            return new Decision(true, canonical, 0L, ActionResult.ReasonCode.SUCCESS, bypass);
        }

        Key key = new Key(playerId, canonical);
        long existing = expiryEpochMillis.getOrDefault(key, 0L);
        if (existing > now) {
            return new Decision(
                    false,
                    canonical,
                    Math.max(1L, (existing - now + 999L) / 1000L),
                    ActionResult.ReasonCode.COOLDOWN_ACTIVE,
                    false);
        }
        long expiry;
        try {
            expiry = Math.addExact(now, duration.toMillis());
        } catch (ArithmeticException exception) {
            return new Decision(false, canonical, 0L, ActionResult.ReasonCode.INVALID_INPUT, false);
        }
        expiryEpochMillis.put(key, expiry);
        notifyChanged();
        return new Decision(true, canonical, 0L, ActionResult.ReasonCode.SUCCESS, false);
    }

    public synchronized Decision inspect(UUID playerId, String actionId) {
        long now = safeNow();
        String canonical = canonical(actionId);
        long expiry = expiryEpochMillis.getOrDefault(new Key(playerId, canonical), 0L);
        if (expiry <= now) {
            return new Decision(true, canonical, 0L, ActionResult.ReasonCode.SUCCESS, false);
        }
        return new Decision(
                false,
                canonical,
                Math.max(1L, (expiry - now + 999L) / 1000L),
                ActionResult.ReasonCode.COOLDOWN_ACTIVE,
                false);
    }

    public synchronized void clear(UUID playerId, String actionId) {
        if (expiryEpochMillis.remove(new Key(playerId, canonical(actionId))) != null) {
            notifyChanged();
        }
    }

    public synchronized void clearAll() {
        if (!expiryEpochMillis.isEmpty()) {
            expiryEpochMillis.clear();
            notifyChanged();
        }
        operationsSincePrune = 0;
    }

    public synchronized List<Entry> snapshotPersistent(Duration minimumRemaining) {
        Objects.requireNonNull(minimumRemaining, "minimumRemaining");
        long now = safeNow();
        long minimum = Math.max(0L, minimumRemaining.toMillis());
        return expiryEpochMillis.entrySet().stream()
                .filter(entry -> entry.getValue() - now >= minimum)
                .map(entry -> new Entry(entry.getKey().playerId(), entry.getKey().actionId(), entry.getValue()))
                .sorted(Comparator.comparing(Entry::playerId).thenComparing(Entry::actionId))
                .toList();
    }

    public synchronized void restore(List<Entry> entries) {
        Objects.requireNonNull(entries, "entries");
        long now = safeNow();
        for (Entry entry : entries) {
            if (entry.expiryEpochMillis() <= now) {
                continue;
            }
            if (entry.expiryEpochMillis() - now > HARD_MAXIMUM.toMillis()) {
                continue;
            }
            expiryEpochMillis.merge(
                    new Key(entry.playerId(), canonical(entry.actionId())),
                    entry.expiryEpochMillis(),
                    Math::max);
        }
    }

    public void onChange(Runnable listener) {
        changeListeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public synchronized int size() {
        pruneExpired(safeNow());
        return expiryEpochMillis.size();
    }

    private long safeNow() {
        long observed = clock.millis();
        long previous = lastObservedEpochMillis;
        if (observed + MAXIMUM_CLOCK_ROLLBACK_MILLIS < previous) {
            observed = previous - MAXIMUM_CLOCK_ROLLBACK_MILLIS;
        }
        lastObservedEpochMillis = Math.max(observed, previous);
        return observed;
    }

    private void pruneExpired(long now) {
        if (expiryEpochMillis.entrySet().removeIf(entry -> entry.getValue() <= now)) {
            notifyChanged();
        }
    }

    private String canonical(String actionId) {
        String current = normalize(actionId);
        for (int depth = 0; depth < 8; depth++) {
            String next = canonicalActions.get(current);
            if (next == null) {
                return current;
            }
            current = next;
        }
        throw new IllegalStateException("Cooldown alias chain exceeds limit");
    }

    private void notifyChanged() {
        changeListeners.forEach(Runnable::run);
    }

    public record Decision(
            boolean allowed,
            String canonicalActionId,
            long remainingSeconds,
            ActionResult.ReasonCode reason,
            boolean bypassed
    ) {
    }

    public record Entry(UUID playerId, String actionId, long expiryEpochMillis) {
        public Entry {
            Objects.requireNonNull(playerId, "playerId");
            actionId = normalize(actionId);
            if (expiryEpochMillis < 0) {
                throw new IllegalArgumentException("Cooldown expiry cannot be negative");
            }
        }
    }

    private record Key(UUID playerId, String actionId) {
    }

    private static String normalize(String value) {
        String normalized = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank() || normalized.length() > 128) {
            throw new IllegalArgumentException("Cooldown action id is outside bounds");
        }
        return normalized;
    }
}
