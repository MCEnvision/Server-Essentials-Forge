package com.enviouse.sef.announcements;

public sealed interface ScheduledAnnouncement permits TextAnnouncement, CommandAnnouncement {
    String id();

    long intervalSeconds();

    boolean enabled();

    long offsetSeconds();
}
