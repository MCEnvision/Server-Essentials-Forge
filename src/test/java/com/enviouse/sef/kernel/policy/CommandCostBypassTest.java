package com.enviouse.sef.kernel.policy;

import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.command.CommandDefinition;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandCostBypassTest {
    @Test
    void bypassReservesZeroWithoutChangingTheConfiguredPolicyCost() {
        FeatureGateService gates = new FeatureGateService();
        gates.publish(new FeatureGateService.Snapshot(
                2L,
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
                Duration.ZERO,
                Duration.ZERO,
                new BigDecimal("5.00"),
                AuditService.AuditClass.METADATA_ONLY));
        RecordingCostService costs = new RecordingCostService();
        CommandExecutionService executions = new CommandExecutionService(
                policies,
                new CooldownService(),
                costs,
                new WarmupService(),
                new ConfirmationService());

        ActionResult<CommandExecutionService.Lease> bypassed =
                executions.begin(request(true));
        assertTrue(bypassed.successful());
        assertEquals(BigDecimal.ZERO, costs.lastAmount);
        assertTrue(bypassed.value().complete(true, null).successful());

        ActionResult<CommandExecutionService.Lease> charged =
                executions.begin(request(false));
        assertTrue(charged.successful());
        assertEquals(new BigDecimal("5.00"), costs.lastAmount);
        assertTrue(charged.value().complete(true, null).successful());
    }

    private static CommandExecutionService.Request request(boolean costBypass) {
        return new CommandExecutionService.Request(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "player",
                "sef:test",
                CommandDefinition.SourceType.PLAYER,
                "world",
                "dimension",
                true,
                false,
                costBypass,
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
        private BigDecimal lastAmount;

        @Override
        public String providerId() {
            return "test";
        }

        @Override
        public ActionResult<Reservation> reserve(UUID actorId, String actionId, BigDecimal amount) {
            lastAmount = amount;
            return ActionResult.success(new FreeReservation());
        }
    }
}
