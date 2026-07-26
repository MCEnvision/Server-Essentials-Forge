package com.enviouse.sef.announcements;

public record TitleAnnouncement(String title, String subtitle) {
    public TitleAnnouncement {
        title = title == null ? "" : title;
        subtitle = subtitle == null ? "" : subtitle;
    }
}
