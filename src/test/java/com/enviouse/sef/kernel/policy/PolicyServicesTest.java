package com.enviouse.sef.kernel.policy;

import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.kernel.command.CommandDefinition;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolicyServicesTest {
    @Test
    void featureGateUsesImmutableRevisionedPrecedence() {
        FeatureGateService gates = new FeatureGateService();
        gates.publish(new FeatureGateService.Snapshot(
                2,
                Map.of("sef.home", true),
                Map.of("minecraft:the_nether|sef.home", false),
                Map.of("sef:home.force", true)));

        assertTrue(gates.decide(
                "sef.home",
                new FeatureGateService.Context("world", "minecraft:overworld", "sef:home")).enabled());
        assertFalse(gates.decide(
                "sef.home",
                new FeatureGateService.Context("world", "minecraft:the_nether", "sef:home")).enabled());
        assertTrue(gates.decide(
                "sef.home",
                new FeatureGateService.Context("world", "minecraft:the_nether", "sef:home.force")).enabled());
    }

    @Test
    void quotasResolveDeterministicallyWithAndWithoutMetadataProvider() {
        QuotaService quotas = quotaService();
        UUID subject = UUID.randomUUID();
        List<String> ids = List.of(
                "sef:homes",
                "sef:player_warps",
                "sef:targets",
                "sef:mail",
                "sef:definitions");

        for (String id : ids) {
            QuotaService.Decision absent = quotas.resolve(context(id, subject, 0));
            assertEquals(defaultFor(id), absent.effectiveValue());
            assertEquals("internal_default", absent.provider());
        }

        quotas.setProviders(List.of(new QuotaService.Provider() {
            @Override
            public String id() {
                return "luckperms_metadata";
            }

            @Override
            public int priority() {
                return 200;
            }

            @Override
            public QuotaService.Candidate resolve(
                    QuotaService.Definition definition,
                    QuotaService.Context context
            ) {
                return QuotaService.Candidate.finite(7, id(), "sef.limit.test");
            }
        }));

        for (String id : ids) {
            QuotaService.Decision present = quotas.resolve(context(id, subject, 0));
            assertEquals(7, present.effectiveValue());
            assertEquals("luckperms_metadata", present.provider());
        }
    }

    @Test
    void quotaHardCeilingsAndExplicitUnlimitedRemainBounded() {
        QuotaService quotas = new QuotaService();
        quotas.register(new QuotaService.Definition(
                "sef:test",
                QuotaService.QuotaKind.COUNT,
                2,
                10,
                true,
                Map.of()));
        UUID subject = UUID.randomUUID();
        quotas.setProviders(List.of(new QuotaService.ContextMetadataProvider()));

        QuotaService.Context oversized = new QuotaService.Context(
                "sef:test",
                subject,
                "server",
                "world",
                "dimension",
                "sef:test",
                Set.of(),
                Map.of("sef:test", "999999"),
                Map.of(),
                0);
        assertEquals(10, quotas.resolve(oversized).effectiveValue());

        QuotaService.Context unlimited = new QuotaService.Context(
                "sef:test",
                subject,
                "server",
                "world",
                "dimension",
                "sef:test",
                Set.of(),
                Map.of("sef:test", "unlimited"),
                Map.of(),
                0);
        QuotaService.Decision decision = quotas.resolve(unlimited);
        assertTrue(decision.unlimited());
        assertEquals(10, decision.effectiveValue());
    }

    @Test
    void finitePermissionTierPrecedesInternalOverride() {
        QuotaService quotas = new QuotaService();
        quotas.register(new QuotaService.Definition(
                "sef:test",
                QuotaService.QuotaKind.COUNT,
                1,
                100,
                false,
                Map.of("sef.limits.test.10", 10L)));
        QuotaService.Context context = new QuotaService.Context(
                "sef:test",
                UUID.randomUUID(),
                "server",
                "world",
                "dimension",
                "sef:test",
                Set.of("sef.limits.test.10"),
                Map.of(),
                Map.of("sef:test", 3L),
                0);

        QuotaService.Decision decision = quotas.resolve(context);

        assertEquals(10, decision.effectiveValue());
        assertEquals("permission_tier", decision.provider());
    }

    @Test
    void quotaProviderFailureFallsBackFiniteAndProducesDiagnostics() {
        QuotaService quotas = new QuotaService();
        quotas.register(new QuotaService.Definition(
                "sef:test",
                QuotaService.QuotaKind.COUNT,
                2,
                10,
                false,
                Map.of()));
        quotas.setProviders(List.of(new QuotaService.Provider() {
            @Override
            public String id() {
                return "broken";
            }

            @Override
            public int priority() {
                return 100;
            }

            @Override
            public QuotaService.Candidate resolve(
                    QuotaService.Definition definition,
                    QuotaService.Context context
            ) {
                throw new IllegalStateException("provider unavailable");
            }
        }));

        QuotaService.Decision decision = quotas.resolve(context("sef:test", UUID.randomUUID(), 0));

        assertEquals(2, decision.effectiveValue());
        assertEquals("internal_default", decision.provider());
        assertEquals("broken", quotas.providerDiagnostics().getFirst().providerId());
        assertEquals("IllegalStateException", quotas.providerDiagnostics().getFirst().detail());
    }

    @Test
    void concurrentReservationsCannotSpendTheSameFinalSlot() throws Exception {
        QuotaService quotas = new QuotaService();
        quotas.register(new QuotaService.Definition(
                "sef:test",
                QuotaService.QuotaKind.COUNT,
                1,
                1,
                false,
                Map.of()));
        UUID subject = UUID.randomUUID();
        QuotaService.Context context = new QuotaService.Context(
                "sef:test",
                subject,
                "server",
                "world",
                "dimension",
                "sef:test",
                Set.of(),
                Map.of(),
                Map.of(),
                0);
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            List<Future<ActionResult<QuotaService.Reservation>>> futures = new ArrayList<>();
            for (int index = 0; index < 2; index++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    return quotas.reserve(context, 1);
                }));
            }
            start.countDown();
            List<ActionResult<QuotaService.Reservation>> results = futures.stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception exception) {
                            throw new RuntimeException(exception);
                        }
                    })
                    .toList();
            assertEquals(1, results.stream().filter(ActionResult::successful).count());
            results.stream()
                    .filter(ActionResult::successful)
                    .map(ActionResult::value)
                    .forEach(QuotaService.Reservation::close);
        }
    }

    @Test
    void shortcutsShareCanonicalCooldownState() {
        CooldownService cooldowns = new CooldownService();
        cooldowns.registerAlias("c", "sef:workstation.craft");
        UUID player = UUID.randomUUID();

        assertTrue(cooldowns.tryAcquire(
                player,
                "c",
                Duration.ofMinutes(5),
                false).allowed());
        CooldownService.Decision canonical = cooldowns.inspect(player, "sef:workstation.craft");
        assertFalse(canonical.allowed());
        assertEquals("sef:workstation.craft", canonical.canonicalActionId());
    }

    @Test
    void cooldownAliasesRejectCycles() {
        CooldownService cooldowns = new CooldownService();
        cooldowns.registerAlias("a", "b");
        assertThrows(IllegalArgumentException.class, () -> cooldowns.registerAlias("b", "a"));
    }

    @Test
    void warmupAcceptsEmptyCancellationPolicyAndTracksMovementPolicy() {
        WarmupService warmups = new WarmupService();
        UUID actor = UUID.randomUUID();
        WarmupService.Position start = new WarmupService.Position(
                "minecraft:overworld",
                0,
                64,
                0,
                0,
                0);

        assertTrue(warmups.start(
                actor,
                "sef:home",
                start,
                null,
                Duration.ofSeconds(30),
                Set.of()).successful());
        assertEquals(ActionResult.ReasonCode.WARMUP_ACTIVE, warmups.check(actor, start).reason());

        warmups.clear();
        assertTrue(warmups.start(
                actor,
                "sef:home",
                start,
                null,
                Duration.ofSeconds(30),
                Set.of(WarmupService.CancelReason.MOVEMENT)).successful());
        WarmupService.Position moved = new WarmupService.Position(
                "minecraft:overworld",
                1,
                64,
                0,
                0,
                0);
        assertEquals(ActionResult.ReasonCode.WARMUP_CANCELLED, warmups.check(actor, moved).reason());
    }

    @Test
    void hierarchyChecksExemptionBeforeBypassAndUsesWeights() {
        TargetHierarchyService hierarchy = new TargetHierarchyService();
        UUID actor = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        TargetHierarchyService.Decision exempt = hierarchy.decide(new TargetHierarchyService.Context(
                actor,
                target,
                false,
                true,
                true,
                false,
                false,
                false,
                500,
                100,
                "",
                ""));
        assertFalse(exempt.allowed());
        assertEquals(ActionResult.ReasonCode.TARGET_EXEMPT, exempt.reason());

        TargetHierarchyService.Decision weighted = hierarchy.decide(new TargetHierarchyService.Context(
                actor,
                target,
                false,
                false,
                false,
                false,
                false,
                false,
                300,
                200,
                "",
                ""));
        assertTrue(weighted.allowed());
    }

    @Test
    void confirmationTokensAreBoundOneUseAndCostProviderFailsClosed() {
        ConfirmationService confirmations = new ConfirmationService();
        ConfirmationService.Request request = new ConfirmationService.Request(
                UUID.randomUUID(),
                "sef:dangerous",
                Map.of("mode", "test"),
                List.of(UUID.randomUUID()),
                "",
                0,
                0,
                0,
                2);
        ActionResult<ConfirmationService.IssuedToken> issued =
                confirmations.issue(request, Duration.ofSeconds(30));
        assertTrue(issued.successful());
        assertTrue(confirmations.consume(issued.value().token(), request).successful());
        assertEquals(
                ActionResult.ReasonCode.CONFIRMATION_INVALID,
                confirmations.consume(issued.value().token(), request).reason());

        CostService costs = new CostService.Disabled();
        assertTrue(costs.reserve(UUID.randomUUID(), "sef:free", BigDecimal.ZERO).successful());
        assertEquals(
                ActionResult.ReasonCode.COST_UNAVAILABLE,
                costs.reserve(UUID.randomUUID(), "sef:paid", BigDecimal.ONE).reason());
    }

    @Test
    void sharedExecutionPipelineRevalidatesPermissionAndRollsBackFailedCooldown() {
        FeatureGateService gates = new FeatureGateService();
        gates.publish(new FeatureGateService.Snapshot(
                2,
                Map.of("sef.test", true),
                Map.of(),
                Map.of()));
        CommandPolicyService policies = new CommandPolicyService(gates);
        policies.register(new CommandPolicyService.Policy(
                "sef:test",
                "sef.test",
                Set.of(CommandDefinition.SourceType.PLAYER),
                false,
                false,
                Duration.ofMinutes(5),
                Duration.ZERO,
                BigDecimal.ZERO,
                AuditService.AuditClass.METADATA_ONLY));
        CooldownService cooldowns = new CooldownService();
        CommandExecutionService executions =
                new CommandExecutionService(
                        policies,
                        cooldowns,
                        new CostService.Disabled(),
                        new WarmupService(),
                        new ConfirmationService());
        UUID actor = UUID.randomUUID();

        CommandExecutionService.Request denied = request(actor, false);
        assertEquals(ActionResult.ReasonCode.PERMISSION_DENIED, executions.begin(denied).reason());

        ActionResult<CommandExecutionService.Lease> first = executions.begin(request(actor, true));
        assertTrue(first.successful());
        assertEquals(ActionResult.ReasonCode.COOLDOWN_ACTIVE, executions.begin(request(actor, true)).reason());
        assertFalse(first.value().complete(false, ActionResult.ReasonCode.PROVIDER_ERROR).successful());

        ActionResult<CommandExecutionService.Lease> retried = executions.begin(request(actor, true));
        assertTrue(retried.successful());
        assertTrue(retried.value().complete(true, null).successful());
        assertEquals(ActionResult.ReasonCode.COOLDOWN_ACTIVE, executions.begin(request(actor, true)).reason());
    }

    @Test
    void sharedExecutionPipelineConsumesBoundConfirmationToken() {
        FeatureGateService gates = new FeatureGateService();
        gates.publish(new FeatureGateService.Snapshot(2, Map.of("sef.test", true), Map.of(), Map.of()));
        CommandPolicyService policies = new CommandPolicyService(gates);
        policies.register(new CommandPolicyService.Policy(
                "sef:test",
                "sef.test",
                Set.of(CommandDefinition.SourceType.PLAYER),
                false,
                true,
                Duration.ZERO,
                Duration.ZERO,
                BigDecimal.ZERO,
                AuditService.AuditClass.ADMIN_ACTION));
        ConfirmationService confirmations = new ConfirmationService();
        CommandExecutionService executions = new CommandExecutionService(
                policies,
                new CooldownService(),
                new CostService.Disabled(),
                new WarmupService(),
                confirmations);
        UUID actor = UUID.randomUUID();
        assertEquals(
                ActionResult.ReasonCode.CONFIRMATION_REQUIRED,
                executions.begin(request(actor, true)).reason());

        ConfirmationService.Request binding = new ConfirmationService.Request(
                actor,
                "sef:test",
                Map.of(),
                List.of(),
                "",
                0,
                0,
                0,
                policies.revision());
        ConfirmationService.IssuedToken issued =
                confirmations.issue(binding, Duration.ofSeconds(30)).value();
        CommandExecutionService.Request confirmed = new CommandExecutionService.Request(
                UUID.randomUUID(),
                actor,
                "player",
                "sef:test",
                CommandDefinition.SourceType.PLAYER,
                "world",
                "dimension",
                true,
                false,
                issued.token(),
                binding,
                null,
                Set.of(),
                Map.of(),
                List.of(),
                1L,
                Map.of(),
                "command");

        ActionResult<CommandExecutionService.Lease> started = executions.begin(confirmed);
        assertTrue(started.successful());
        assertTrue(started.value().complete(true, null).successful());
        assertEquals(ActionResult.ReasonCode.CONFIRMATION_INVALID, executions.begin(confirmed).reason());
    }

    @Test
    void warmupChecksCooldownWithoutAcquiringItBeforeWarmupCompletes() {
        FeatureGateService gates = new FeatureGateService();
        gates.publish(new FeatureGateService.Snapshot(2, Map.of("sef.test", true), Map.of(), Map.of()));
        CommandPolicyService policies = new CommandPolicyService(gates);
        policies.register(new CommandPolicyService.Policy(
                "sef:test",
                "sef.test",
                Set.of(CommandDefinition.SourceType.PLAYER),
                false,
                false,
                Duration.ofMinutes(5),
                Duration.ofSeconds(1),
                BigDecimal.ZERO,
                AuditService.AuditClass.METADATA_ONLY));
        MutableClock clock = new MutableClock();
        CooldownService cooldowns = new CooldownService(clock);
        CommandExecutionService executions = new CommandExecutionService(
                policies,
                cooldowns,
                new CostService.Disabled(),
                new WarmupService(clock),
                new ConfirmationService());
        UUID actor = UUID.randomUUID();
        CommandExecutionService.Request request = request(
                actor,
                true,
                new WarmupService.Position("minecraft:overworld", 0, 64, 0, 0, 0));

        assertEquals(ActionResult.ReasonCode.WARMUP_ACTIVE, executions.begin(request).reason());
        assertTrue(cooldowns.inspect(actor, "sef:test").allowed());

        clock.advance(Duration.ofSeconds(2));
        ActionResult<CommandExecutionService.Lease> started = executions.begin(request);
        assertTrue(started.successful());
        assertFalse(cooldowns.inspect(actor, "sef:test").allowed());
        assertTrue(started.value().complete(true, null).successful());
    }

    @Test
    void rejectedConfirmationRefundsTheCooldownReservation() {
        FeatureGateService gates = new FeatureGateService();
        gates.publish(new FeatureGateService.Snapshot(2, Map.of("sef.test", true), Map.of(), Map.of()));
        CommandPolicyService policies = new CommandPolicyService(gates);
        policies.register(new CommandPolicyService.Policy(
                "sef:test",
                "sef.test",
                Set.of(CommandDefinition.SourceType.PLAYER),
                false,
                true,
                Duration.ofMinutes(5),
                Duration.ZERO,
                BigDecimal.ZERO,
                AuditService.AuditClass.ADMIN_ACTION));
        CooldownService cooldowns = new CooldownService();
        CommandExecutionService executions = new CommandExecutionService(
                policies,
                cooldowns,
                new CostService.Disabled(),
                new WarmupService(),
                new ConfirmationService());
        UUID actor = UUID.randomUUID();

        assertEquals(ActionResult.ReasonCode.CONFIRMATION_REQUIRED, executions.begin(request(actor, true)).reason());
        assertTrue(cooldowns.inspect(actor, "sef:test").allowed());
        assertEquals(ActionResult.ReasonCode.CONFIRMATION_REQUIRED, executions.begin(request(actor, true)).reason());
    }

    private static QuotaService quotaService() {
        QuotaService quotas = new QuotaService();
        quotas.register(new QuotaService.Definition("sef:homes", QuotaService.QuotaKind.COUNT, 1, 1000, true, Map.of()));
        quotas.register(new QuotaService.Definition("sef:player_warps", QuotaService.QuotaKind.COUNT, 5, 1000, true, Map.of()));
        quotas.register(new QuotaService.Definition("sef:targets", QuotaService.QuotaKind.TARGET_CAP, 1, 1000, false, Map.of()));
        quotas.register(new QuotaService.Definition("sef:mail", QuotaService.QuotaKind.COUNT, 100, 10000, false, Map.of()));
        quotas.register(new QuotaService.Definition("sef:definitions", QuotaService.QuotaKind.DEFINITION_COUNT, 64, 1024, false, Map.of()));
        quotas.setProviders(List.of(new QuotaService.ContextMetadataProvider()));
        return quotas;
    }

    private static QuotaService.Context context(String quotaId, UUID subject, long usage) {
        return new QuotaService.Context(
                quotaId,
                subject,
                "server",
                "world",
                "dimension",
                "sef:test",
                Set.of(),
                Map.of(),
                Map.of(),
                usage);
    }

    private static long defaultFor(String quotaId) {
        return switch (quotaId) {
            case "sef:homes", "sef:targets" -> 1;
            case "sef:player_warps" -> 5;
            case "sef:mail" -> 100;
            case "sef:definitions" -> 64;
            default -> throw new IllegalArgumentException(quotaId);
        };
    }

    private static CommandExecutionService.Request request(UUID actor, boolean permissionGranted) {
        return request(actor, permissionGranted, null);
    }

    private static CommandExecutionService.Request request(
            UUID actor,
            boolean permissionGranted,
            WarmupService.Position position
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
                "",
                null,
                position,
                Set.of(),
                Map.of(),
                List.of(),
                1L,
                Map.of(),
                "command");
    }

    private static final class MutableClock extends Clock {
        private long epochMillis = 1_000L;

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(epochMillis);
        }

        @Override
        public long millis() {
            return epochMillis;
        }

        private void advance(Duration duration) {
            epochMillis += duration.toMillis();
        }
    }
}
