package com.enviouse.sef.fancytags;

import com.enviouse.sef.ServerEssentialsForge;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class FancyTagGroupResolver {
    private static final int MAXIMUM_GROUPS = 128;
    private static Provider provider;
    private static Health health = new Health("none", false, "provider unavailable", Instant.EPOCH);

    private FancyTagGroupResolver() {
    }

    public static synchronized void install(Provider replacement) {
        provider = Objects.requireNonNull(replacement, "replacement");
        health = new Health(provider.id(), true, "ready", Instant.now());
    }

    public static synchronized void clear() {
        provider = null;
        health = new Health("none", false, "provider unavailable", Instant.now());
    }

    public static synchronized Set<String> groups(UUID playerId) {
        if (provider == null) {
            return Set.of();
        }
        try {
            Set<String> resolved = provider.groups(playerId);
            if (resolved == null || resolved.size() > MAXIMUM_GROUPS) {
                throw new IllegalStateException("Fancy Tags group result is outside bounds");
            }
            Set<String> normalized = new LinkedHashSet<>();
            for (String group : resolved) {
                String value = Objects.requireNonNull(group, "group")
                        .trim()
                        .toLowerCase(Locale.ROOT);
                if (!value.matches("[a-z0-9][a-z0-9_.-]{0,63}")) {
                    throw new IllegalStateException("Fancy Tags group identifier is invalid");
                }
                normalized.add(value);
            }
            health = new Health(provider.id(), true, "ready", Instant.now());
            return Set.copyOf(normalized);
        } catch (RuntimeException | LinkageError exception) {
            health = new Health(provider.id(), false, exception.getClass().getSimpleName(), Instant.now());
            ServerEssentialsForge.LOGGER.warn(
                    "Fancy Tags group provider failed closed for {}",
                    playerId,
                    exception);
            return Set.of();
        }
    }

    public static synchronized Health health() {
        return health;
    }

    public interface Provider {
        String id();

        Set<String> groups(UUID playerId);
    }

    public record Health(String providerId, boolean healthy, String detail, Instant checkedAt) {
    }
}
