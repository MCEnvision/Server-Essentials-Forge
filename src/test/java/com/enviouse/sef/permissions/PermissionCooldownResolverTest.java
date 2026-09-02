package com.enviouse.sef.permissions;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionCooldownResolverTest {
    private static final UUID PLAYER = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final PermissionCooldownResolver.Definition CRAFT =
            new PermissionCooldownResolver.Definition(
                    "sef:workstation.craft",
                    "craft",
                    Duration.ofSeconds(10));

    @Test
    void directAssignmentWinsOverInheritedDurations() {
        PermissionCooldownResolver resolver = resolver(prefix ->
                new PermissionCooldownResolver.GrantSnapshot(
                        "luckperms",
                        true,
                        true,
                        Set.of(prefix + "100"),
                        Set.of(prefix + "5", prefix + "30")));

        var result = resolver.explain(PLAYER, "sef:workstation.craft");

        assertEquals(Duration.ofSeconds(100), result.duration());
        assertEquals("luckperms_direct", result.provider());
        assertFalse(result.fallback());
    }

    @Test
    void lowestInheritedGrantWinsDeterministically() {
        PermissionCooldownResolver resolver = resolver(prefix ->
                new PermissionCooldownResolver.GrantSnapshot(
                        "luckperms",
                        true,
                        true,
                        Set.of(),
                        Set.of(prefix + "600", prefix + "30", prefix + "60")));

        var result = resolver.explain(PLAYER, "sef:workstation.craft");

        assertEquals(Duration.ofSeconds(30), result.duration());
        assertEquals("sef.cooldown.craft.30", result.winningNode());
    }

    @Test
    void malformedAndDeniedStyleValuesCannotBecomeGrants() {
        PermissionCooldownResolver resolver = resolver(prefix ->
                new PermissionCooldownResolver.GrantSnapshot(
                        "luckperms",
                        true,
                        true,
                        Set.of(),
                        Set.of(
                                prefix + "-1",
                                prefix + "+1",
                                prefix + "1.0",
                                prefix + "1e2",
                                prefix + " 1",
                                prefix + "31536001",
                                prefix + "*")));

        var result = resolver.explain(PLAYER, "sef:workstation.craft");

        assertEquals(Duration.ofSeconds(10), result.duration());
        assertTrue(result.fallback());
    }

    @Test
    void providerOutageUsesFiniteNonzeroDefault() {
        PermissionCooldownResolver resolver = resolver(prefix -> {
            throw new IllegalStateException("provider unavailable");
        });

        var result = resolver.explain(PLAYER, "sef:workstation.craft");

        assertEquals(Duration.ofSeconds(10), result.duration());
        assertEquals("provider_outage", result.provider());
        assertTrue(result.fallback());
    }

    @Test
    void cacheIsBoundedByRevisionAndInvalidation() {
        AtomicInteger calls = new AtomicInteger();
        PermissionCooldownResolver resolver = resolver(prefix -> {
            calls.incrementAndGet();
            return new PermissionCooldownResolver.GrantSnapshot(
                    "test",
                    true,
                    true,
                    Set.of(),
                    Set.of(prefix + "5"));
        });

        resolver.explain(PLAYER, "sef:workstation.craft");
        resolver.explain(PLAYER, "sef:workstation.craft");
        assertEquals(1, calls.get());
        resolver.invalidate();
        resolver.explain(PLAYER, "sef:workstation.craft");
        assertEquals(2, calls.get());
    }

    @Test
    void numericParserAcceptsOnlyBoundedBaseTenWholeSeconds() {
        assertEquals(0L, PermissionCooldownResolver.parseSeconds("0"));
        assertEquals(31536000L, PermissionCooldownResolver.parseSeconds("31536000"));
        assertNull(PermissionCooldownResolver.parseSeconds(""));
        assertNull(PermissionCooldownResolver.parseSeconds("-1"));
        assertNull(PermissionCooldownResolver.parseSeconds("+1"));
        assertNull(PermissionCooldownResolver.parseSeconds("1.0"));
        assertNull(PermissionCooldownResolver.parseSeconds("1e2"));
        assertNull(PermissionCooldownResolver.parseSeconds("31536001"));
        assertNull(PermissionCooldownResolver.parseSeconds("999999999999999999999"));
    }

    @Test
    void teleportRequestAndRandomActionsHavePermissionControlledDefaults() {
        assertEquals(Duration.ofSeconds(5),
                PermissionCooldownResolver.internalDefault("sef:teleport.request.to"));
        assertEquals(Duration.ofSeconds(5),
                PermissionCooldownResolver.internalDefault("sef:teleport.request.accept"));
        assertEquals(Duration.ofSeconds(5),
                PermissionCooldownResolver.internalDefault("sef:teleport.random"));
    }

    private static PermissionCooldownResolver resolver(SnapshotFactory factory) {
        return new PermissionCooldownResolver(
                List.of(CRAFT),
                (player, prefix) -> factory.create(prefix),
                false);
    }

    @FunctionalInterface
    private interface SnapshotFactory {
        PermissionCooldownResolver.GrantSnapshot create(String prefix);
    }
}
