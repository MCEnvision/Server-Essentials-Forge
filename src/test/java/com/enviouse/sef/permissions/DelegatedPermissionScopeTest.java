package com.enviouse.sef.permissions;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DelegatedPermissionScopeTest {
    @Test
    void grantIsBoundToOneSubjectPermissionThreadActionAndSynchronousScope() {
        UUID subject = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        EphemeralExecutionGrant grant = grant(
                subject,
                "minecraft:help",
                "help",
                Set.of("sef.commands.help"),
                Instant.now(),
                Instant.now().plusSeconds(15));
        AtomicBoolean otherThread = new AtomicBoolean(true);

        boolean result = DelegatedPermissionScope.execute(
                grant,
                "help",
                "minecraft:help",
                () -> {
                    assertTrue(DelegatedPermissionScope.active());
                    assertTrue(DelegatedPermissionScope.allows(subject, "sef.commands.help"));
                    assertFalse(DelegatedPermissionScope.allows(subject, "sef.commands.list"));
                    assertFalse(DelegatedPermissionScope.allows(other, "sef.commands.help"));
                    assertTrue(DelegatedPermissionScope.actionAllowed("minecraft:help"));
                    assertFalse(DelegatedPermissionScope.actionAllowed("sef:list"));
                    Thread thread = new Thread(() -> otherThread.set(
                            DelegatedPermissionScope.allows(subject, "sef.commands.help")));
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(exception);
                    }
                    return true;
                });

        assertTrue(result);
        assertTrue(grant.used());
        assertFalse(otherThread.get());
        assertFalse(DelegatedPermissionScope.active());
        assertFalse(DelegatedPermissionScope.allows(subject, "sef.commands.help"));
    }

    @Test
    void previewUsesExactPermissionsWithoutPublishingOrConsumingGrant() {
        UUID subject = UUID.randomUUID();
        assertTrue(DelegatedPermissionScope.preview(
                subject,
                "effect",
                "minecraft:effect",
                Set.of("sef.commands.effect"),
                () -> {
                    assertTrue(DelegatedPermissionScope.active());
                    assertTrue(DelegatedPermissionScope.allows(subject, "sef.commands.effect"));
                    assertFalse(DelegatedPermissionScope.allows(subject, "sef.commands.op"));
                    return true;
                }));
        assertFalse(DelegatedPermissionScope.active());
    }

    @Test
    void nestedScopeFailsAndFinallyAlwaysRemovesGrant() {
        UUID subject = UUID.randomUUID();
        EphemeralExecutionGrant outer = grant(
                subject,
                "minecraft:help",
                "help",
                Set.of(),
                Instant.now(),
                Instant.now().plusSeconds(15));
        EphemeralExecutionGrant inner = grant(
                subject,
                "minecraft:list",
                "list",
                Set.of(),
                Instant.now(),
                Instant.now().plusSeconds(15));
        assertThrows(IllegalStateException.class, () ->
                DelegatedPermissionScope.execute(outer, "help", "minecraft:help", () ->
                        DelegatedPermissionScope.execute(
                                inner,
                                "list",
                                "minecraft:list",
                                () -> true)));
        assertFalse(DelegatedPermissionScope.active());
        assertTrue(outer.used());
        assertFalse(inner.used());
    }

    @Test
    void expiredMismatchedAndReplayedGrantsFailClosed() {
        UUID subject = UUID.randomUUID();
        Instant now = Instant.now();
        EphemeralExecutionGrant expired = grant(
                subject,
                "minecraft:help",
                "help",
                Set.of(),
                now.minusSeconds(20),
                now.minusSeconds(10));
        assertThrows(IllegalStateException.class, () ->
                DelegatedPermissionScope.execute(
                        expired,
                        "help",
                        "minecraft:help",
                        () -> true));
        EphemeralExecutionGrant future = grant(
                subject,
                "minecraft:help",
                "help",
                Set.of(),
                now.plusSeconds(10),
                now.plusSeconds(20));
        assertThrows(IllegalStateException.class, () ->
                DelegatedPermissionScope.execute(
                        future,
                        "help",
                        "minecraft:help",
                        () -> true));
        assertFalse(future.used());

        EphemeralExecutionGrant grant = grant(
                subject,
                "minecraft:help",
                "help",
                Set.of(),
                now,
                now.plusSeconds(15));
        assertThrows(IllegalStateException.class, () ->
                DelegatedPermissionScope.execute(
                        grant,
                        "list",
                        "minecraft:help",
                        () -> true));
        assertFalse(grant.used());
        assertTrue(DelegatedPermissionScope.execute(
                grant,
                "help",
                "minecraft:help",
                () -> true));
        assertThrows(IllegalStateException.class, () ->
                DelegatedPermissionScope.execute(
                        grant,
                        "help",
                        "minecraft:help",
                        () -> true));
    }

    @Test
    void concurrentDispatchConsumesGrantOnce() throws InterruptedException {
        UUID subject = UUID.randomUUID();
        EphemeralExecutionGrant grant = grant(
                subject,
                "minecraft:help",
                "help",
                Set.of(),
                Instant.now(),
                Instant.now().plusSeconds(15));
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger completed = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        Runnable operation = () -> {
            ready.countDown();
            try {
                start.await();
                DelegatedPermissionScope.execute(
                        grant,
                        "help",
                        "minecraft:help",
                        completed::incrementAndGet);
            } catch (IllegalStateException exception) {
                rejected.incrementAndGet();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        };
        Thread first = new Thread(operation);
        Thread second = new Thread(operation);
        first.start();
        second.start();
        ready.await();
        start.countDown();
        first.join();
        second.join();

        assertEquals(1, completed.get());
        assertEquals(1, rejected.get());
        assertTrue(grant.used());
    }

    @Test
    void exceptionConsumesGrantAndCleansScope() {
        EphemeralExecutionGrant grant = grant(
                UUID.randomUUID(),
                "minecraft:help",
                "help",
                Set.of(),
                Instant.now(),
                Instant.now().plusSeconds(15));
        assertThrows(IllegalArgumentException.class, () ->
                DelegatedPermissionScope.execute(
                        grant,
                        "help",
                        "minecraft:help",
                        () -> {
                            throw new IllegalArgumentException("failure");
                        }));
        assertTrue(grant.used());
        assertFalse(DelegatedPermissionScope.active());
    }

    @Test
    void commandFingerprintIsStableAndCommandSpecific() {
        assertTrue(DelegatedPermissionScope.fingerprint(" help ")
                .equals(DelegatedPermissionScope.fingerprint("help")));
        assertNotEquals(
                DelegatedPermissionScope.fingerprint("help"),
                DelegatedPermissionScope.fingerprint("list"));
    }

    private static EphemeralExecutionGrant grant(
            UUID subject,
            String action,
            String command,
            Set<String> permissions,
            Instant createdAt,
            Instant expiresAt
    ) {
        return new EphemeralExecutionGrant(
                UUID.randomUUID(),
                UUID.randomUUID(),
                subject,
                1L,
                command,
                action,
                DelegatedPermissionScope.fingerprint(command),
                1L,
                "test",
                1L,
                2,
                permissions,
                Set.of(),
                1L,
                1L,
                1L,
                1L,
                createdAt,
                expiresAt,
                UUID.randomUUID(),
                UUID.randomUUID());
    }
}
