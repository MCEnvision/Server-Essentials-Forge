package com.enviouse.sef.teleport;

import com.enviouse.sef.config.ConfigHandler;
import net.minecraft.resources.ResourceLocation;

import java.time.Duration;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public record TeleportSettings(
        String defaultHomeName,
        int defaultHomeLimit,
        int defaultHomePerDimensionLimit,
        int defaultPlayerWarpLimit,
        BigDecimal cost,
        Duration warmup,
        boolean cancelOnMovement,
        boolean cancelOnDamage,
        SafeTeleportService.Policy userPolicy,
        Duration requestLifetime,
        int maximumPendingRequests,
        Duration transferLifetime,
        int randomMinimumRadius,
        int randomMaximumRadius,
        int randomMaximumAttempts,
        Set<String> randomAllowedDimensions,
        OwnershipMode ownershipMode,
        Set<String> disabledActions
) {
    public TeleportSettings {
        defaultHomeName = HomeRecord.normalizeName(defaultHomeName);
        cost = java.util.Objects.requireNonNull(cost, "cost");
        warmup = requireDuration(warmup, Duration.ofHours(1), "warmup");
        requestLifetime = requirePositiveDuration(requestLifetime, Duration.ofHours(1), "request lifetime");
        transferLifetime = requirePositiveDuration(transferLifetime, Duration.ofHours(1), "transfer lifetime");
        userPolicy = java.util.Objects.requireNonNull(userPolicy, "userPolicy");
        randomAllowedDimensions = Set.copyOf(randomAllowedDimensions);
        ownershipMode = java.util.Objects.requireNonNull(ownershipMode, "ownershipMode");
        disabledActions = Set.copyOf(disabledActions);
        if (cost.signum() < 0 || cost.compareTo(new BigDecimal("1000000000")) > 0
                || defaultHomeLimit < 0 || defaultHomeLimit > 1000
                || defaultHomePerDimensionLimit < 0 || defaultHomePerDimensionLimit > 1000
                || defaultPlayerWarpLimit < 0 || defaultPlayerWarpLimit > 1000
                || maximumPendingRequests < 1 || maximumPendingRequests > 100
                || randomMinimumRadius < 0
                || randomMaximumRadius < 1
                || randomMinimumRadius > randomMaximumRadius
                || randomMaximumRadius > 30_000_000
                || randomMaximumAttempts < 1
                || randomMaximumAttempts > 256
                || randomAllowedDimensions.isEmpty()
                || disabledActions.size() > 256
                || disabledActions.stream().anyMatch(action ->
                        !action.startsWith("sef:teleport.") || action.length() > 256)) {
            throw new IllegalArgumentException("Teleport settings are outside hard bounds");
        }
        if (randomAllowedDimensions.stream().anyMatch(value -> ResourceLocation.tryParse(value) == null)) {
            throw new IllegalArgumentException("Random teleport dimensions contain an invalid identifier");
        }
    }

    public static TeleportSettings fromConfig() {
        ConfigHandler.ConfigBuilder config = ConfigHandler.config;
        Set<String> dimensions = Arrays.stream(config.randomTeleportAllowedDimensions.get().split(","))
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
        Set<String> disabledActions = Arrays.stream(config.disabledTeleportActions.get().split(","))
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
        return new TeleportSettings(
                config.defaultHomeName.get(),
                config.defaultHomeLimit.get(),
                config.defaultHomePerDimensionLimit.get(),
                config.defaultPlayerWarpLimit.get(),
                BigDecimal.valueOf(config.teleportCost.get()),
                Duration.ofSeconds(config.teleportWarmupSeconds.get()),
                config.teleportCancelOnMovement.get(),
                config.teleportCancelOnDamage.get(),
                new SafeTeleportService.Policy(
                        config.teleportSafeSearchRadius.get(),
                        config.teleportMaximumSafeChecks.get(),
                        config.teleportMaximumChunks.get(),
                        config.teleportAllowHazards.get(),
                        config.teleportAllowNetherRoof.get(),
                        config.teleportAllowInCombat.get(),
                        true,
                        config.teleportInvulnerabilityTicks.get()),
                Duration.ofSeconds(config.teleportRequestExpirySeconds.get()),
                config.teleportMaximumPendingRequests.get(),
                Duration.ofSeconds(config.playerWarpTransferExpirySeconds.get()),
                config.randomTeleportMinimumRadius.get(),
                config.randomTeleportMaximumRadius.get(),
                config.randomTeleportMaximumAttempts.get(),
                dimensions,
                OwnershipMode.parse(config.teleportOwnershipMode.get()),
                disabledActions);
    }

    private static Duration requireDuration(Duration value, Duration maximum, String name) {
        if (value == null || value.isNegative() || value.compareTo(maximum) > 0) {
            throw new IllegalArgumentException(name + " is outside hard bounds");
        }
        return value;
    }

    private static Duration requirePositiveDuration(Duration value, Duration maximum, String name) {
        requireDuration(value, maximum, name);
        if (value.isZero()) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }

    public enum OwnershipMode {
        SEF,
        EXTERNAL,
        COEXIST,
        IMPORT_ONCE;

        public static OwnershipMode parse(String value) {
            try {
                return valueOf(java.util.Objects.requireNonNull(value, "value")
                        .trim()
                        .toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Teleport ownership mode is invalid", exception);
            }
        }
    }
}
