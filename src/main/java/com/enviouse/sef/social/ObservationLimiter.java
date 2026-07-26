package com.enviouse.sef.social;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

final class ObservationLimiter {
    private final int eventCapacity;
    private final Duration eventLifetime;
    private final Map<UUID, Instant> events = new LinkedHashMap<>();
    private final Map<UUID, RateWindow> observers = new LinkedHashMap<>();

    ObservationLimiter(int eventCapacity, Duration eventLifetime) {
        if (eventCapacity < 1) {
            throw new IllegalArgumentException("event capacity must be positive");
        }
        this.eventCapacity = eventCapacity;
        this.eventLifetime = Objects.requireNonNull(eventLifetime, "eventLifetime");
        if (eventLifetime.isNegative() || eventLifetime.isZero()) {
            throw new IllegalArgumentException("event lifetime must be positive");
        }
    }

    synchronized boolean acceptEvent(UUID eventId, Instant now) {
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(now, "now");
        pruneEvents(now);
        if (events.containsKey(eventId)) {
            return false;
        }
        events.put(eventId, now);
        while (events.size() > eventCapacity) {
            Iterator<UUID> iterator = events.keySet().iterator();
            iterator.next();
            iterator.remove();
        }
        return true;
    }

    synchronized boolean acceptObserver(UUID observerId, Instant now, int maximumPerSecond) {
        Objects.requireNonNull(observerId, "observerId");
        Objects.requireNonNull(now, "now");
        if (maximumPerSecond < 1) {
            return false;
        }
        long second = now.getEpochSecond();
        RateWindow current = observers.get(observerId);
        if (current == null || current.second() != second) {
            observers.put(observerId, new RateWindow(second, 1));
            return true;
        }
        if (current.count() >= maximumPerSecond) {
            return false;
        }
        observers.put(observerId, new RateWindow(second, current.count() + 1));
        return true;
    }

    synchronized void clearObserver(UUID observerId) {
        observers.remove(observerId);
    }

    synchronized void clear() {
        events.clear();
        observers.clear();
    }

    private void pruneEvents(Instant now) {
        Instant threshold = now.minus(eventLifetime);
        events.values().removeIf(createdAt -> createdAt.isBefore(threshold));
    }

    private record RateWindow(long second, int count) {
    }
}
