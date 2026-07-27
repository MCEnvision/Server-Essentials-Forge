package com.enviouse.sef.disguise;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class ProxyEntityIdAllocator {
    private static final int MINIMUM_ID = 1_500_000_000;
    private static final int MAXIMUM_ID = 2_000_000_000;

    private final Map<ObserverSubject, Allocation> allocations = new HashMap<>();
    private final Map<ObserverProxy, Allocation> reverse = new HashMap<>();
    private int nextId = MINIMUM_ID;

    public synchronized Allocation allocate(UUID observerId, UUID subjectId, long disguiseRevision) {
        ObserverSubject key = new ObserverSubject(
                Objects.requireNonNull(observerId, "observerId"),
                Objects.requireNonNull(subjectId, "subjectId"));
        Allocation current = allocations.get(key);
        if (current != null && current.disguiseRevision() == disguiseRevision) {
            return current;
        }
        if (current != null) {
            reverse.remove(new ObserverProxy(observerId, current.proxyEntityId()));
        }
        int candidate = findFree(observerId);
        Allocation replacement = new Allocation(observerId, subjectId, candidate, disguiseRevision);
        allocations.put(key, replacement);
        reverse.put(new ObserverProxy(observerId, candidate), replacement);
        return replacement;
    }

    public synchronized Optional<Allocation> resolve(UUID observerId, int proxyEntityId) {
        return Optional.ofNullable(reverse.get(new ObserverProxy(observerId, proxyEntityId)));
    }

    public synchronized void release(UUID observerId, UUID subjectId) {
        Allocation removed = allocations.remove(new ObserverSubject(observerId, subjectId));
        if (removed != null) {
            reverse.remove(new ObserverProxy(observerId, removed.proxyEntityId()));
        }
    }

    public synchronized void releaseObserver(UUID observerId) {
        allocations.entrySet().removeIf(entry -> {
            if (!entry.getKey().observerId().equals(observerId)) {
                return false;
            }
            reverse.remove(new ObserverProxy(observerId, entry.getValue().proxyEntityId()));
            return true;
        });
    }

    public synchronized void releaseSubject(UUID subjectId) {
        allocations.entrySet().removeIf(entry -> {
            if (!entry.getKey().subjectId().equals(subjectId)) {
                return false;
            }
            reverse.remove(new ObserverProxy(entry.getKey().observerId(), entry.getValue().proxyEntityId()));
            return true;
        });
    }

    public synchronized void clear() {
        allocations.clear();
        reverse.clear();
        nextId = MINIMUM_ID;
    }

    public synchronized int size() {
        return allocations.size();
    }

    private int findFree(UUID observerId) {
        for (int attempts = 0; attempts <= MAXIMUM_ID - MINIMUM_ID; attempts++) {
            int candidate = nextId++;
            if (nextId > MAXIMUM_ID) {
                nextId = MINIMUM_ID;
            }
            if (!reverse.containsKey(new ObserverProxy(observerId, candidate))) {
                return candidate;
            }
        }
        throw new IllegalStateException("disguise proxy entity id space is exhausted");
    }

    public record Allocation(
            UUID observerId,
            UUID subjectId,
            int proxyEntityId,
            long disguiseRevision
    ) {
        public Allocation {
            Objects.requireNonNull(observerId, "observerId");
            Objects.requireNonNull(subjectId, "subjectId");
            if (proxyEntityId < MINIMUM_ID || proxyEntityId > MAXIMUM_ID || disguiseRevision < 1L) {
                throw new IllegalArgumentException("invalid disguise proxy allocation");
            }
        }
    }

    private record ObserverSubject(UUID observerId, UUID subjectId) {
    }

    private record ObserverProxy(UUID observerId, int proxyEntityId) {
    }
}
