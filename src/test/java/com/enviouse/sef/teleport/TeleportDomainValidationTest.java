package com.enviouse.sef.teleport;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TeleportDomainValidationTest {
    @Test
    void savedLocationsRejectInvalidDimensionsAndNonFiniteCoordinates() {
        assertThrows(IllegalArgumentException.class, () ->
                new SavedLocation("not a dimension", 0, 64, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () ->
                new SavedLocation("minecraft:overworld", Double.NaN, 64, 0, 0, 0));
    }

    @Test
    void settingsRejectInvertedRandomRadiusAndUnboundedSafetyPolicy() {
        assertThrows(IllegalArgumentException.class, () -> new TeleportSettings(
                "home",
                1,
                1,
                5,
                java.math.BigDecimal.ZERO,
                Duration.ZERO,
                true,
                true,
                new SafeTeleportService.Policy(4, 512, 9, false, false, false, true, 20),
                Duration.ofSeconds(60),
                10,
                Duration.ofMinutes(5),
                500,
                100,
                32,
                Set.of("minecraft:overworld"),
                TeleportSettings.OwnershipMode.SEF,
                Set.of()));
        assertThrows(IllegalArgumentException.class, () ->
                new SafeTeleportService.Policy(33, 512, 9, false, false, false, true, 20));
    }

    @Test
    void randomTeleportRadiusIsBoundedAtTwentyThousandBlocks() {
        assertDoesNotThrow(() -> new TeleportSettings(
                "home",
                1,
                1,
                5,
                java.math.BigDecimal.ZERO,
                Duration.ZERO,
                true,
                true,
                new SafeTeleportService.Policy(4, 512, 9, false, false, false, true, 20),
                Duration.ofSeconds(60),
                10,
                Duration.ofMinutes(5),
                256,
                20_000,
                32,
                Set.of("minecraft:overworld"),
                TeleportSettings.OwnershipMode.SEF,
                Set.of()));
        assertThrows(IllegalArgumentException.class, () -> new TeleportSettings(
                "home",
                1,
                1,
                5,
                java.math.BigDecimal.ZERO,
                Duration.ZERO,
                true,
                true,
                new SafeTeleportService.Policy(4, 512, 9, false, false, false, true, 20),
                Duration.ofSeconds(60),
                10,
                Duration.ofMinutes(5),
                256,
                20_001,
                32,
                Set.of("minecraft:overworld"),
                TeleportSettings.OwnershipMode.SEF,
                Set.of()));
    }
}
