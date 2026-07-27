package com.enviouse.sef.gui.protocol;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PanelActionValidatorTest {
    @Test
    void rejectsForgedExpiredAndStalePanelActions() {
        UUID session = UUID.randomUUID();
        UUID entry = UUID.randomUUID();
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        SefPayloads.PanelActionRequest valid =
                new SefPayloads.PanelActionRequest(session, 1L, "homes", 7L, "visit", entry, 3L);

        assertEquals(
                PanelActionValidator.Decision.ACCEPTED,
                validate(session, entry, valid, now.plusSeconds(1), now));
        assertEquals(
                PanelActionValidator.Decision.FORGED_SESSION,
                validate(UUID.randomUUID(), entry, valid, now.plusSeconds(1), now));
        assertEquals(
                PanelActionValidator.Decision.EXPIRED,
                validate(session, entry, valid, now.minusSeconds(1), now));
        assertEquals(
                PanelActionValidator.Decision.STALE_PANEL,
                PanelActionValidator.validate(
                        session, "homes", 8L, now.plusSeconds(1),
                        "visit", entry, 3L, valid, now));
        assertEquals(
                PanelActionValidator.Decision.FORGED_CONTROL,
                PanelActionValidator.validate(
                        session, "homes", 7L, now.plusSeconds(1),
                        "delete", entry, 3L, valid, now));
        assertEquals(
                PanelActionValidator.Decision.STALE_ENTRY,
                PanelActionValidator.validate(
                        session, "homes", 7L, now.plusSeconds(1),
                        "visit", entry, 4L, valid, now));
        assertEquals(
                PanelActionValidator.Decision.UNKNOWN_ENTRY,
                PanelActionValidator.validate(
                        session, "homes", 7L, now.plusSeconds(1),
                        null, null, -1L, valid, now));
    }

    private static PanelActionValidator.Decision validate(
            UUID session,
            UUID entry,
            SefPayloads.PanelActionRequest request,
            Instant expires,
            Instant now
    ) {
        return PanelActionValidator.validate(
                session,
                "homes",
                7L,
                expires,
                "visit",
                entry,
                3L,
                request,
                now);
    }
}
