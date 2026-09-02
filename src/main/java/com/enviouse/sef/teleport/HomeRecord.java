package com.enviouse.sef.teleport;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public record HomeRecord(
        UUID id,
        UUID ownerId,
        String normalizedName,
        String displayName,
        SavedLocation location,
        Instant createdAt,
        Instant updatedAt,
        String icon,
        String description,
        Visibility visibility,
        String permission,
        long safetyRevision,
        long revision,
        Instant deletedAt
) {
    private static final Pattern VALID_NAME = Pattern.compile("[a-z0-9_.]{1,32}");

    public HomeRecord {
        id = Objects.requireNonNull(id, "id");
        ownerId = Objects.requireNonNull(ownerId, "ownerId");
        normalizedName = normalizeName(normalizedName);
        displayName = validateDisplayName(displayName);
        if (!normalizeName(displayName).equals(normalizedName)) {
            throw new IllegalArgumentException("Home names do not match");
        }
        location = Objects.requireNonNull(location, "location");
        createdAt = Objects.requireNonNull(createdAt, "createdAt");
        updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
        icon = bounded(icon, 128);
        description = bounded(description, 512);
        visibility = Objects.requireNonNull(visibility, "visibility");
        permission = bounded(permission, 192);
        if (safetyRevision < 0 || revision < 1) {
            throw new IllegalArgumentException("Home revision is outside bounds");
        }
    }

    public boolean active() {
        return deletedAt == null;
    }

    public HomeRecord relocated(SavedLocation replacement, Instant now) {
        return new HomeRecord(
                id,
                ownerId,
                normalizedName,
                displayName,
                replacement,
                createdAt,
                now,
                icon,
                description,
                visibility,
                permission,
                safetyRevision + 1,
                revision + 1,
                null);
    }

    public HomeRecord renamed(String replacement, Instant now) {
        String normalized = normalizeName(replacement);
        return new HomeRecord(
                id,
                ownerId,
                normalized,
                replacement,
                location,
                createdAt,
                now,
                icon,
                description,
                visibility,
                permission,
                safetyRevision,
                revision + 1,
                deletedAt);
    }

    public HomeRecord deleted(Instant now) {
        return new HomeRecord(
                id,
                ownerId,
                normalizedName,
                displayName,
                location,
                createdAt,
                now,
                icon,
                description,
                visibility,
                permission,
                safetyRevision,
                revision + 1,
                now);
    }

    public HomeRecord restored(Instant now) {
        return new HomeRecord(
                id,
                ownerId,
                normalizedName,
                displayName,
                location,
                createdAt,
                now,
                icon,
                description,
                visibility,
                permission,
                safetyRevision,
                revision + 1,
                null);
    }

    public static String normalizeName(String value) {
        String normalized = Objects.requireNonNull(value, "name").trim().toLowerCase(Locale.ROOT);
        if (!VALID_NAME.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Names may contain letters, numbers, underscores, and periods");
        }
        return normalized;
    }

    private static String validateDisplayName(String value) {
        String display = Objects.requireNonNull(value, "displayName").trim();
        normalizeName(display);
        return display;
    }

    private static String bounded(String value, int maximum) {
        String result = value == null ? "" : value.trim();
        if (result.length() > maximum) {
            throw new IllegalArgumentException("Text exceeds its maximum length");
        }
        return result;
    }

    public enum Visibility {
        PRIVATE,
        SHARED
    }
}
