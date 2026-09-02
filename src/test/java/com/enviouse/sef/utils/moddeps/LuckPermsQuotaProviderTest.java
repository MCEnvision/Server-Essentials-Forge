package com.enviouse.sef.utils.moddeps;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LuckPermsQuotaProviderTest {
    @Test
    void genericMetadataKeysAreStableAndNamespaced() {
        assertEquals("sef.limit.homes.total", LuckPermsQuotaProvider.genericKey("sef:homes_total"));
        assertEquals("sef.limit.mail", LuckPermsQuotaProvider.genericKey("sef:mail"));
        assertEquals("sef.limit.external.value", LuckPermsQuotaProvider.genericKey("external:value"));
    }

    @Test
    void perDimensionHomesAcceptCanonicalAndMigrationMetadataNames() {
        assertEquals(
                List.of("sef.limit.homes.per.dimension", "sef.limit.homes.per.world"),
                LuckPermsQuotaProvider.metadataKeys("sef:homes_per_dimension"));
    }
}
