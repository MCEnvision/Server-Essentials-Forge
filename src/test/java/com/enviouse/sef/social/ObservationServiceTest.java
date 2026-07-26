package com.enviouse.sef.social;

import com.enviouse.sef.audit.AuditService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObservationServiceTest {
    @Test
    void privateContentDeliveryRecordsObserverAndRedaction() {
        UUID observerId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();

        AuditService.Event event = ObservationService.deliveryAudit(
                observerId,
                senderId,
                "sender",
                recipientId,
                "sef_msg",
                true);

        assertEquals(observerId, event.observerId());
        assertEquals(senderId, event.actorId());
        assertEquals(recipientId, event.targetIds().getFirst());
        assertEquals("content", event.normalizedParameters().get("scope"));
        assertEquals(AuditService.RedactionClass.PRIVATE_CONTENT, event.redactionClass());
        assertEquals(AuditService.AuditClass.PRIVATE_MESSAGE_OBSERVATION, event.auditClass());
        assertTrue(event.appliedRedactionRuleIds().contains("private_message_content"));
    }

    @Test
    void metadataDeliveryDoesNotClaimPrivateContent() {
        AuditService.Event event = ObservationService.deliveryAudit(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "sender",
                UUID.randomUUID(),
                "sef_reply",
                false);

        assertEquals("metadata", event.normalizedParameters().get("scope"));
        assertEquals(AuditService.RedactionClass.METADATA, event.redactionClass());
        assertTrue(event.appliedRedactionRuleIds().isEmpty());
    }
}
