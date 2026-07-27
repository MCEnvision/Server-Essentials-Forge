package com.enviouse.sef.fancytags.api;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class FancyTagTargetRegistry {
    private static final int MAXIMUM_PROVIDERS = 64;
    private static final Map<String, TargetProvider> PROVIDERS = new LinkedHashMap<>();

    private FancyTagTargetRegistry() {
    }

    public static synchronized void register(String id, TargetProvider provider) {
        String normalized = normalize(id);
        Objects.requireNonNull(provider, "provider");
        if (!PROVIDERS.containsKey(normalized) && PROVIDERS.size() >= MAXIMUM_PROVIDERS) {
            throw new IllegalStateException("Fancy Tags target provider capacity is exhausted");
        }
        if (PROVIDERS.putIfAbsent(normalized, provider) != null) {
            throw new IllegalArgumentException("Fancy Tags target provider already exists");
        }
    }

    public static synchronized void unregister(String id) {
        PROVIDERS.remove(normalize(id));
    }

    public static synchronized Set<String> ids() {
        return Set.copyOf(PROVIDERS.keySet());
    }

    public static synchronized boolean matches(
            String providerId,
            UUID viewerId,
            UUID subjectId,
            String target
    ) {
        TargetProvider provider = PROVIDERS.get(normalize(providerId));
        if (provider == null) {
            return false;
        }
        String bounded = Objects.requireNonNullElse(target, "").trim().toLowerCase(Locale.ROOT);
        if (bounded.isBlank() || bounded.length() > 128
                || bounded.codePoints().anyMatch(Character::isISOControl)) {
            return false;
        }
        return provider.matches(
                Objects.requireNonNull(viewerId, "viewerId"),
                Objects.requireNonNull(subjectId, "subjectId"),
                bounded);
    }

    private static String normalize(String value) {
        String result = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (!result.matches("[a-z0-9_.-]+:[a-z0-9/._-]+") || result.length() > 128) {
            throw new IllegalArgumentException("Fancy Tags target provider id is invalid");
        }
        return result;
    }

    @FunctionalInterface
    public interface TargetProvider {
        boolean matches(UUID viewerId, UUID subjectId, String target);
    }
}
