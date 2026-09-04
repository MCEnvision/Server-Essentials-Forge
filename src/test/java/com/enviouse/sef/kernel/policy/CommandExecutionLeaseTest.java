package com.enviouse.sef.kernel.policy;

import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.audit.SecurityAuditService;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.command.CommandDefinition;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
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

class CommandExecutionLeaseTest {
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
    void successfulCompletionFailsWhenMandatoryAuditIsRejected() {
        SecurityAuditService.shutdown();
        Fixture fixture = fixture(
                AuditService.AuditClass.ADMIN_ACTION,
                new RecordingReservation(ActionResult.success(null), ActionResult.success(null)));
        CommandExecutionService.Lease lease = fixture.begin();

        ActionResult<Void> result = lease.complete(true, null);

        assertFalse(result.successful());
        assertEquals(ActionResult.ReasonCode.STORAGE_ERROR, result.reason());
        assertEquals(1, fixture.reservation.commitCalls);
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
                Duration.ZERO,
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
        return new CommandExecutionService.Request(
                UUID.randomUUID(),
                actor,
                "player",
                "sef:test",
                CommandDefinition.SourceType.PLAYER,
                "world",
                "dimension",
                true,
                false,
                false,
                "",
                null,
                null,
                Set.of(),
                Map.of(),
                List.of(),
                1L,
                Map.of(),
                "command");
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
