package com.enviouse.sef.announcements;

public record CommandAnnouncement(
        String id,
        String command,
        long intervalSeconds,
        boolean enabled,
        long offsetSeconds,
        CommandSourcePolicy sourcePolicy,
        String createdBy,
        String createdAt
) implements ScheduledAnnouncement {
}
