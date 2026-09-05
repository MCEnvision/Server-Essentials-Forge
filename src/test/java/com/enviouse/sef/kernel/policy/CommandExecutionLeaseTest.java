package com.enviouse.sef.kernel.policy;

import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.audit.SecurityAuditService;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.command.CommandDefinition;
import org.mockito.MockedStatic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

class CommandExecutionLeaseTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void successfulCompletionCommitsOnceAndRejectsReplay() {
        Fixture fixture = fixture(new RecordingReservation(ActionResult.success(null), ActionResult.success(null)));
        CommandExecutionService.Lease lease = fixture.begin();

        assertTrue(lease.complete(true, null).successful());
        assertEquals(1, fixture.reservation.commitCalls);
        assertEquals(0, fixture.reservation.refundCalls);
        assertFalse(fixture.cooldowns.inspect(fixture.actor, "sef:test").allowed());

        ActionResult<Void> replay = lease.complete(true, null);
        assertEquals(ActionResult.ReasonCode.CONFLICT, replay.reason());
        assertEquals(1, fixture.reservation.commitCalls);
    }

    @Test
    void successfulCompletionFailsWhenMandatoryAuditWriterHasFailed() throws Exception {
        SecurityAuditService.start(temporaryDirectory, 7, 1);
        try {
            Fixture fixture = fixture(
                    AuditService.AuditClass.ADMIN_ACTION,
                    new RecordingReservation(ActionResult.success(null), ActionResult.success(null)));
            CommandExecutionService.Lease lease = fixture.begin();
            Path activeFile = temporaryDirectory.resolve("audit").resolve("security-audit.jsonl");
            Files.createDirectories(activeFile);
            assertTrue(SecurityAuditService.record(SecurityAuditService.AuditEvent.create(
                    "test", "writer_failure", "tester", "", "test", "attempted", "test")));
            await(() -> SecurityAuditService.health().failures() > 0L
                    && !SecurityAuditService.health().writerAlive());

            ActionResult<Void> result = lease.complete(true, null);

            assertFalse(result.successful());
            assertEquals(ActionResult.ReasonCode.STORAGE_ERROR, result.reason());
            assertEquals(1, fixture.reservation.commitCalls);
        } finally {
            SecurityAuditService.shutdown();
        }
    }

    @Test
    void failedCompletionFailsClosedWhenMandatoryAuditWriterHasFailed() throws Exception {
        SecurityAuditService.start(temporaryDirectory, 7, 1);
        try (MockedStatic<AuditService> audit = mockStatic(AuditService.class)) {
            audit.when(() -> AuditService.accepting(AuditService.AuditClass.ADMIN_ACTION)).thenReturn(true);
            audit.when(() -> AuditService.record(any(AuditService.Event.class))).thenReturn(false);
            Fixture fixture = fixture(
                    AuditService.AuditClass.ADMIN_ACTION,
                    new RecordingReservation(ActionResult.success(null), ActionResult.success(null)));
            CommandExecutionService.Lease lease = fixture.begin();

            ActionResult<Void> result = lease.complete(false, ActionResult.ReasonCode.INVALID_INPUT);

            assertFalse(result.successful());
            assertEquals(ActionResult.ReasonCode.STORAGE_ERROR, result.reason());
            assertEquals(1, fixture.reservation.refundCalls);
            assertTrue(fixture.cooldowns.inspect(fixture.actor, "sef:test").allowed());
        } finally {
            SecurityAuditService.shutdown();
        }
    }

    @Test
    void rejectedExecutionFailsClosedWhenMandatoryAuditWriterHasFailed() {
        try (MockedStatic<AuditService> audit = mockStatic(AuditService.class)) {
            audit.when(() -> AuditService.accepting(AuditService.AuditClass.ADMIN_ACTION)).thenReturn(true);
            audit.when(() -> AuditService.record(any(AuditService.Event.class))).thenReturn(false);
            RecordingReservation reservation = new RecordingReservation(
                    ActionResult.success(null), ActionResult.success(null));
            Fixture fixture = fixture(AuditService.AuditClass.ADMIN_ACTION, reservation);

            ActionResult<CommandExecutionService.Lease> result =
                    fixture.executions.begin(request(fixture.actor, false));

            assertFalse(result.successful());
            assertEquals(ActionResult.ReasonCode.STORAGE_ERROR, result.reason());
            assertEquals(0, reservation.commitCalls);
            assertEquals(0, reservation.refundCalls);
        }
    }

    @Test
    void warmupInputRejectionFailsClosedWhenMandatoryAuditWriterHasFailed() {
        try (MockedStatic<AuditService> audit = mockStatic(AuditService.class)) {
            audit.when(() -> AuditService.accepting(AuditService.AuditClass.ADMIN_ACTION)).thenReturn(true);
            audit.when(() -> AuditService.record(any(AuditService.Event.class))).thenReturn(false);
            RecordingReservation reservation = new RecordingReservation(
                    ActionResult.success(null), ActionResult.success(null));
            Fixture fixture = fixture(
                    AuditService.AuditClass.ADMIN_ACTION,
                    reservation,
                    Duration.ofSeconds(1));

            ActionResult<CommandExecutionService.Lease> result =
                    fixture.executions.begin(request(fixture.actor, true, null));

            assertFalse(result.successful());
            assertEquals(ActionResult.ReasonCode.STORAGE_ERROR, result.reason());
            assertEquals(0, reservation.commitCalls);
            assertEquals(0, reservation.refundCalls);
        }
    }

    @Test
    void failedCompletionRefundsAndClearsCooldown() {
        Fixture fixture = fixture(new RecordingReservation(ActionResult.success(null), ActionResult.success(null)));
        CommandExecutionService.Lease lease = fixture.begin();

        ActionResult<Void> result = lease.complete(false, ActionResult.ReasonCode.INVALID_INPUT);

        assertFalse(result.successful());
        assertEquals(ActionResult.ReasonCode.INVALID_INPUT, result.reason());
        assertEquals(1, fixture.reservation.refundCalls);
        assertTrue(fixture.cooldowns.inspect(fixture.actor, "sef:test").allowed());
    }

    @Test
    void failedCompletionReportsUnknownOutcomeAndKeepsCooldownWhenRefundFails() {
        Fixture fixture = fixture(new RecordingReservation(
                ActionResult.success(null),
                ActionResult.failure(ActionResult.ReasonCode.COST_UNAVAILABLE, "refund unavailable")));
        CommandExecutionService.Lease lease = fixture.begin();

        ActionResult<Void> result = lease.complete(false, ActionResult.ReasonCode.PROVIDER_ERROR);

        assertFalse(result.successful());
        assertEquals(ActionResult.ReasonCode.COST_UNAVAILABLE, result.reason());
        assertEquals(1, fixture.reservation.refundCalls);
        assertFalse(fixture.cooldowns.inspect(fixture.actor, "sef:test").allowed());
        assertEquals(ActionResult.ReasonCode.CONFLICT, lease.complete(false, null).reason());
    }

    @Test
    void commitFailureReportsUnknownOutcomeAndKeepsCooldown() {
        Fixture fixture = fixture(new RecordingReservation(
                ActionResult.failure(ActionResult.ReasonCode.COST_UNAVAILABLE, "commit unavailable"),
                ActionResult.success(null)));
        CommandExecutionService.Lease lease = fixture.begin();

        ActionResult<Void> result = lease.complete(true, null);

        assertFalse(result.successful());
        assertEquals(ActionResult.ReasonCode.COST_UNAVAILABLE, result.reason());
        assertEquals(1, fixture.reservation.commitCalls);
        assertFalse(fixture.cooldowns.inspect(fixture.actor, "sef:test").allowed());
    }

    @Test
    void closingAnAbandonedLeaseRefundsExactlyOnce() {
        Fixture fixture = fixture(new RecordingReservation(ActionResult.success(null), ActionResult.success(null)));
        CommandExecutionService.Lease lease = fixture.begin();

        lease.close();
        lease.close();

        assertEquals(1, fixture.reservation.refundCalls);
        assertTrue(fixture.cooldowns.inspect(fixture.actor, "sef:test").allowed());
    }

    @Test
    void concurrentCompletionHasOneWinnerAndOneEffect() throws Exception {
        Fixture fixture = fixture(new RecordingReservation(ActionResult.success(null), ActionResult.success(null)));
        CommandExecutionService.Lease lease = fixture.begin();
        CountDownLatch ready = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<ActionResult<Void>> first = executor.submit(() -> {
                ready.await();
                return lease.complete(true, null);
            });
            Future<ActionResult<Void>> second = executor.submit(() -> {
                ready.await();
                return lease.complete(true, null);
            });
            ready.countDown();

            ActionResult<Void> firstResult = first.get();
            ActionResult<Void> secondResult = second.get();
            assertTrue(firstResult.successful() ^ secondResult.successful());
            assertEquals(ActionResult.ReasonCode.CONFLICT,
                    firstResult.successful() ? secondResult.reason() : firstResult.reason());
            assertEquals(1, fixture.reservation.commitCalls);
        } finally {
            executor.shutdownNow();
        }
    }

    private static Fixture fixture(RecordingReservation reservation) {
        return fixture(AuditService.AuditClass.NONE, reservation);
    }

    private static Fixture fixture(AuditService.AuditClass auditClass, RecordingReservation reservation) {
        return fixture(auditClass, reservation, Duration.ZERO);
    }

    private static Fixture fixture(
            AuditService.AuditClass auditClass,
            RecordingReservation reservation,
            Duration warmup
    ) {
        FeatureGateService gates = new FeatureGateService();
        gates.publish(new FeatureGateService.Snapshot(2L, Map.of("sef.test", true), Map.of(), Map.of()));
        CommandPolicyService policies = new CommandPolicyService(gates);
        policies.register(new CommandPolicyService.Policy(
                "sef:test",
                "sef.test",
                Set.of(CommandDefinition.SourceType.PLAYER),
                false,
                false,
                Duration.ofMinutes(5),
                warmup,
                BigDecimal.ONE,
                auditClass));
        CooldownService cooldowns = new CooldownService();
        RecordingCostService costs = new RecordingCostService(reservation);
        CommandExecutionService executions = new CommandExecutionService(
                policies,
                cooldowns,
                costs,
                new WarmupService(),
                new ConfirmationService());
        return new Fixture(executions, cooldowns, UUID.randomUUID(), reservation);
    }

    private record Fixture(
            CommandExecutionService executions,
            CooldownService cooldowns,
            UUID actor,
            RecordingReservation reservation
    ) {
        private CommandExecutionService.Lease begin() {
            ActionResult<CommandExecutionService.Lease> result = executions.begin(request(actor));
            assertTrue(result.successful(), result.detail());
            return result.value();
        }
    }

    private static CommandExecutionService.Request request(UUID actor) {
        return request(actor, true);
    }

    private static CommandExecutionService.Request request(UUID actor, boolean permissionGranted) {
        return request(actor, permissionGranted, null);
    }

    private static CommandExecutionService.Request request(
            UUID actor,
            boolean permissionGranted,
            WarmupService.Position warmupPosition
    ) {
        return new CommandExecutionService.Request(
                UUID.randomUUID(),
                actor,
                "player",
                "sef:test",
                CommandDefinition.SourceType.PLAYER,
                "world",
                "dimension",
                permissionGranted,
                false,
                false,
                "",
                null,
                warmupPosition,
                Set.of(),
                Map.of(),
                List.of(),
                1L,
                Map.of(),
                "command");
    }

    private static void await(java.util.function.BooleanSupplier condition) {
        long deadline = System.nanoTime() + Duration.ofSeconds(2).toNanos();
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
        assertTrue(condition.getAsBoolean());
    }

    private static final class RecordingCostService implements CostService {
        private final CostService.Reservation reservation;

        private RecordingCostService(CostService.Reservation reservation) {
            this.reservation = reservation;
        }

        @Override
        public String providerId() {
            return "test";
        }

        @Override
        public ActionResult<Reservation> reserve(UUID actorId, String actionId, BigDecimal amount) {
            return ActionResult.success(reservation);
        }
    }

    private static final class RecordingReservation implements CostService.Reservation {
        private final ActionResult<Void> commitResult;
        private final ActionResult<Void> refundResult;
        private int commitCalls;
        private int refundCalls;

        private RecordingReservation(ActionResult<Void> commitResult, ActionResult<Void> refundResult) {
            this.commitResult = commitResult;
            this.refundResult = refundResult;
        }

        @Override
        public BigDecimal amount() {
            return BigDecimal.ONE;
        }

        @Override
        public synchronized ActionResult<Void> commit() {
            commitCalls++;
            return commitResult;
        }

        @Override
        public synchronized ActionResult<Void> refund() {
            refundCalls++;
            return refundResult;
        }

        @Override
        public Map<String, String> auditContext() {
            return Map.of("cost_amount", "1");
        }

        @Override
        public void close() {
        }
    }
}
