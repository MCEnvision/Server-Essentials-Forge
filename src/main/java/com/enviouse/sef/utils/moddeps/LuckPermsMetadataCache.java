package com.enviouse.sef.utils.moddeps;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

final class LuckPermsMetadataCache {
    private final int maximumEntries;
    private final Map<UUID, Entry> entries = new HashMap<>();

    LuckPermsMetadataCache(int maximumEntries) {
        if (maximumEntries < 1 || maximumEntries > 65_536) {
            throw new IllegalArgumentException("Metadata cache limit is outside bounds");
        }
        this.maximumEntries = maximumEntries;
    }

    synchronized String[] get(UUID playerId, long now) {
        Objects.requireNonNull(playerId, "playerId");
        Entry entry = entries.get(playerId);
        if (entry == null) {
            return null;
        }
        if (entry.expiresAt() <= now) {
            entries.remove(playerId, entry);
            return null;
        }
        return entry.value().clone();
    }

    synchronized void put(UUID playerId, String[] value, long expiresAt, long now) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(value, "value");
        if (expiresAt <= now) {
            entries.remove(playerId);
            return;
        }
        entries.entrySet().removeIf(entry -> entry.getValue().expiresAt() <= now);
        while (!entries.containsKey(playerId) && entries.size() >= maximumEntries) {
            UUID oldest = entries.entrySet().stream()
                    .min(Map.Entry.comparingByValue(
                            java.util.Comparator.comparingLong(Entry::expiresAt)))
                    .map(Map.Entry::getKey)
                    .orElse(null);
            if (oldest == null) {
                break;
            }
            entries.remove(oldest);
        }
        entries.put(playerId, new Entry(value.clone(), expiresAt));
    }

    synchronized void invalidate(UUID playerId) {
        if (playerId != null) {
            entries.remove(playerId);
        }
    }

    synchronized void clear() {
        entries.clear();
    }

    synchronized int size() {
        return entries.size();
    }

    private record Entry(String[] value, long expiresAt) {
    }
}
