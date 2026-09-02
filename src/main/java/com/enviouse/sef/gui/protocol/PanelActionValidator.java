package com.enviouse.sef.gui.protocol;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class PanelActionValidator {
    private PanelActionValidator() {
    }

    public static Decision validate(
            UUID expectedSessionId,
            String expectedPanelId,
            long expectedPanelRevision,
            Instant expiresAt,
            String expectedControlId,
            UUID expectedEntryId,
            long expectedEntryRevision,
            SefPayloads.PanelActionRequest request,
            Instant now
    ) {
        Objects.requireNonNull(expectedSessionId, "expectedSessionId");
        Objects.requireNonNull(expectedPanelId, "expectedPanelId");
        Objects.requireNonNull(expiresAt, "expiresAt");
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(now, "now");
        if (!expectedSessionId.equals(request.sessionId())) {
            return Decision.FORGED_SESSION;
        }
        if (!expectedPanelId.equals(request.panelId())
                || expectedPanelRevision != request.panelRevision()) {
            return Decision.STALE_PANEL;
        }
        if (expiresAt.isBefore(now)) {
            return Decision.EXPIRED;
        }
        if (expectedEntryId == null || expectedControlId == null) {
            return Decision.UNKNOWN_ENTRY;
        }
        if (!expectedEntryId.equals(request.entryId())) {
            return Decision.UNKNOWN_ENTRY;
        }
        if (!expectedControlId.equals(request.controlId())) {
            return Decision.FORGED_CONTROL;
        }
        if (expectedEntryRevision != request.entryRevision()) {
            return Decision.STALE_ENTRY;
        }
        return Decision.ACCEPTED;
    }

    public enum Decision {
        ACCEPTED,
        FORGED_SESSION,
        STALE_PANEL,
        EXPIRED,
        UNKNOWN_ENTRY,
        FORGED_CONTROL,
        STALE_ENTRY
    }
}
