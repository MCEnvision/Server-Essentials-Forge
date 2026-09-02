package com.enviouse.sef.automation;

import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.permissions.DelegatedPermissionScope;
import com.enviouse.sef.permissions.EphemeralExecutionGrant;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SudoDelegationAuditTest {
    @Test
    void lifecycleEventKeepsIssuerTargetGrantAndCorrelationDistinct() {
        UUID issuer = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        UUID correlation = UUID.randomUUID();
        EphemeralExecutionGrant grant = grant(issuer, target, correlation);

        AuditService.Event event = SudoDelegationAudit.event(
                SudoDelegationAudit.Stage.DISPATCH,
                issuer,
                "issuer",
                "player",
                target,
                correlation,
                grant,
                "effect",
                "effect",
                AuditService.Result.OUTCOME_UNKNOWN,
                ActionResult.ReasonCode.SUCCESS,
                "grant created");

        assertEquals(issuer, event.actorId());
        assertEquals(target, event.targetIds().getFirst());
        assertNotEquals(event.actorId(), event.targetIds().getFirst());
        assertEquals(correlation, event.stepCorrelationId());
        assertEquals("sef:sudo.delegation.dispatch", event.actionId());
        assertEquals(grant.grantId().toString(), event.normalizedParameters().get("grant_id"));
        assertEquals(grant.commandDigest(), event.normalizedParameters().get("command_digest"));
        assertEquals("1", event.normalizedParameters().get("maximum_invocations"));
        assertEquals(AuditService.AuditClass.DELEGATED_EXECUTION, event.auditClass());
        assertEquals(AuditService.RedactionClass.SECRET_ARGUMENTS, event.redactionClass());
        assertTrue(event.appliedRedactionRuleIds().contains("sudo_command_digest"));
    }

    @Test
    void cleanupEventReportsConsumedGrantWithoutActiveScope() {
        UUID issuer = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        UUID correlation = UUID.randomUUID();
        EphemeralExecutionGrant grant = grant(issuer, target, correlation);
        assertTrue(grant.consume(Instant.now()));

        AuditService.Event event = SudoDelegationAudit.event(
                SudoDelegationAudit.Stage.CLEANUP,
                issuer,
                "issuer",
                "player",
                target,
                correlation,
                grant,
                "effect",
                "effect",
                AuditService.Result.SUCCESS,
                ActionResult.ReasonCode.SUCCESS,
                "temporary scope removed");

        assertEquals("true", event.normalizedParameters().get("used"));
        assertEquals("false", event.normalizedParameters().get("scope_active"));
        assertTrue(!DelegatedPermissionScope.active());
    }

    private static EphemeralExecutionGrant grant(UUID issuer, UUID target, UUID correlation) {
        Instant created = Instant.now().minusSeconds(1);
        String command = "effect give target minecraft:jump_boost 2 255 true";
        return new EphemeralExecutionGrant(
                UUID.randomUUID(),
                issuer,
                target,
                1L,
                "effect",
                "minecraft:effect",
                DelegatedPermissionScope.fingerprint(command),
                1L,
                "effect",
                1L,
                2,
                Set.of(),
                Set.of(),
                1L,
                1L,
                1L,
                1L,
                created,
                created.plusSeconds(15),
                UUID.randomUUID(),
                correlation);
    }
}
