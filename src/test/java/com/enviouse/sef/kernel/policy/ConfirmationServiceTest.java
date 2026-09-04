package com.enviouse.sef.kernel.policy;

import com.enviouse.sef.kernel.ActionResult;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfirmationServiceTest {
    @Test
    void requestNormalizesBindingFieldsAndEscapesLineBreaks() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        ConfirmationService.Request request = new ConfirmationService.Request(
                UUID.randomUUID(),
                "  SEF:Test.Action  ",
                Map.of(" Zeta ", "line\nfeed\rvalue", "alpha", "value"),
                List.of(second, first),
                " Panel.Main ",
                4L,
                3L,
                2L,
                1L);

        assertEquals("sef:test.action", request.actionId());
        assertEquals(Set.of("alpha", "zeta"), request.normalizedParameters().keySet());
        assertEquals("line\\nfeed\\rvalue", request.normalizedParameters().get("zeta"));
        assertEquals(
                List.of(first, second).stream().sorted(Comparator.comparing(UUID::toString)).toList(),
                request.targetIds());
        assertEquals("panel.main", request.panelId());
    }

    @Test
    void consumeRejectsChangedBindingAndCannotBeReplayed() {
        UUID actor = UUID.randomUUID();
        ConfirmationService service = new ConfirmationService();
        ConfirmationService.Request request = request(actor, "sef:test", List.of());
        ActionResult<ConfirmationService.IssuedToken> issued = service.issue(request, Duration.ofMinutes(1));
        assertTrue(issued.successful());

        ConfirmationService.Request changed = request(actor, "sef:other", List.of());
        ActionResult<ConfirmationService.Request> mismatch = service.consume(issued.value().token(), changed);
        assertFalse(mismatch.successful());
        assertEquals(ActionResult.ReasonCode.CONFIRMATION_INVALID, mismatch.reason());

        ActionResult<ConfirmationService.Request> replay = service.consume(issued.value().token(), request);
        assertFalse(replay.successful());
        assertEquals(ActionResult.ReasonCode.CONFIRMATION_INVALID, replay.reason());
    }

    @Test
    void issueRejectsInvalidLifetimeAndActorRevocationClearsPendingTokens() {
        UUID actor = UUID.randomUUID();
        ConfirmationService service = new ConfirmationService();
        ConfirmationService.Request request = request(actor, "sef:test", List.of());
        assertFalse(service.issue(request, Duration.ZERO).successful());
        assertFalse(service.issue(request, Duration.ofMinutes(11)).successful());
        assertTrue(service.issue(request, Duration.ofMinutes(1)).successful());
        assertEquals(1, service.size());

        service.revokeActor(actor);

        assertEquals(0, service.size());
    }

    private static ConfirmationService.Request request(UUID actor, String action, List<UUID> targets) {
        return new ConfirmationService.Request(
                actor,
                action,
                Map.of("value", "safe"),
                targets,
                "",
                0L,
                0L,
                0L,
                1L);
    }
}
