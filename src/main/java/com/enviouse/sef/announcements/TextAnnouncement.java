package com.enviouse.sef.announcements;

public record TextAnnouncement(
        String id,
        String message,
        long intervalSeconds,
        boolean toggleable,
        String target,
        boolean enabled,
        long offsetSeconds
) implements ScheduledAnnouncement {
    public TextAnnouncement with(long interval, boolean toggle, String newTarget, String newMessage) {
        return new TextAnnouncement(
                id,
                newMessage,
                interval,
                toggle,
                newTarget,
                enabled,
                offsetSeconds);
    }
}
