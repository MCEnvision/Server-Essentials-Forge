package com.enviouse.sef.economy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class EconomyProviderRegistry {
    private static final int MAXIMUM_PROVIDERS = 32;
    private static final List<Registration> PROVIDERS = new ArrayList<>();

    private EconomyProviderRegistry() {
    }

    public static synchronized boolean register(
            String id,
            int priority,
            EconomyProvider provider,
            ImportAdapter importer
    ) {
        final String normalized;
        try {
            normalized = normalize(id);
        } catch (IllegalArgumentException exception) {
            return false;
        }
        if (priority < -10_000 || priority > 10_000
                || provider == null
                || PROVIDERS.size() >= MAXIMUM_PROVIDERS
                || PROVIDERS.stream().anyMatch(existing -> existing.id().equals(normalized))) {
            return false;
        }
        PROVIDERS.add(new Registration(normalized, priority, provider, importer));
        PROVIDERS.sort(Comparator.comparingInt(Registration::priority)
                .reversed()
                .thenComparing(Registration::id));
        return true;
    }

    public static synchronized boolean unregister(String id) {
        String normalized;
        try {
            normalized = normalize(id);
        } catch (IllegalArgumentException exception) {
            return false;
        }
        return PROVIDERS.removeIf(provider -> provider.id().equals(normalized));
    }

    public static synchronized Optional<Registration> select(String configuredId) {
        if (configuredId != null && !configuredId.isBlank()) {
            String normalized;
            try {
                normalized = normalize(configuredId);
            } catch (IllegalArgumentException exception) {
                return Optional.empty();
            }
            return PROVIDERS.stream().filter(provider -> provider.id().equals(normalized)).findFirst();
        }
        return PROVIDERS.stream().findFirst();
    }

    public static synchronized List<String> providerIds() {
        return PROVIDERS.stream().map(Registration::id).toList();
    }

    private static String normalize(String value) {
        String normalized = Objects.requireNonNull(value, "value").strip().toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9_.:-]{1,128}")) {
            throw new IllegalArgumentException("Economy provider id is invalid");
        }
        return normalized;
    }

    public record Registration(
            String id,
            int priority,
            EconomyProvider provider,
            ImportAdapter importer
    ) {
    }

    public interface ImportAdapter {
        ImportPreview preview();

        List<ImportAccount> exportAccounts();
    }

    public record ImportAccount(UUID playerId, long balance) {
    }

    public record ImportPreview(int accounts, long totalMinorUnits, String detail) {
        public ImportPreview {
            detail = detail == null ? "" : detail;
        }
    }
}
