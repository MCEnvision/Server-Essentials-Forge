package com.enviouse.sef.teleport;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public record WarpRecord(
        UUID id,
        UUID ownerId,
        String ownerNameSnapshot,
        String normalizedName,
        String displayName,
        Scope scope,
        Access access,
        Status status,
        SavedLocation location,
        Instant createdAt,
        Instant updatedAt,
        Instant publishedAt,
        Instant deletedAt,
        String permission,
        String icon,
        String description,
        String category,
        boolean hidden,
        boolean listed,
        boolean featured,
        long visits,
        long safetyRevision,
        long revision,
        UUID sourceHomeId,
        Set<UUID> trustedPlayers,
        Set<UUID> blockedPlayers
) {
    private static final Pattern VALID_NAME = Pattern.compile("[a-z0-9_.]{1,32}");

    public WarpRecord {
        id = Objects.requireNonNull(id, "id");
        ownerNameSnapshot = bounded(ownerNameSnapshot, 64);
        normalizedName = normalizeName(normalizedName);
        displayName = bounded(displayName, 32);
        if (!normalizeName(displayName).equals(normalizedName)) {
            throw new IllegalArgumentException("Warp names do not match");
        }
        scope = Objects.requireNonNull(scope, "scope");
        access = Objects.requireNonNull(access, "access");
        status = Objects.requireNonNull(status, "status");
        location = Objects.requireNonNull(location, "location");
        createdAt = Objects.requireNonNull(createdAt, "createdAt");
        updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
        permission = bounded(permission, 192);
        icon = bounded(icon, 128);
        description = bounded(description, 512);
        category = bounded(category, 64);
        if (visits < 0 || safetyRevision < 0 || revision < 1) {
            throw new IllegalArgumentException("Warp counter is outside bounds");
        }
        trustedPlayers = Set.copyOf(Objects.requireNonNull(trustedPlayers, "trustedPlayers"));
        blockedPlayers = Set.copyOf(Objects.requireNonNull(blockedPlayers, "blockedPlayers"));
        if (trustedPlayers.size() > 1000 || blockedPlayers.size() > 1000) {
            throw new IllegalArgumentException("Warp access list exceeds its hard limit");
        }
        if (scope == Scope.SERVER_PUBLIC && ownerId != null) {
            throw new IllegalArgumentException("Server warps cannot have a player owner");
        }
        if (scope != Scope.SERVER_PUBLIC && ownerId == null) {
            throw new IllegalArgumentException("Player warps require an owner");
        }
    }

    public boolean active() {
        return deletedAt == null && status != Status.DELETED;
    }

    public boolean published() {
        return publishedAt != null && (access == Access.PUBLIC || access == Access.UNLISTED);
    }

    public boolean canVisit(UUID visitorId, boolean moderator) {
        if (!active() || status == Status.SUSPENDED || status == Status.DISABLED) {
            return moderator;
        }
        if (scope == Scope.SERVER_PUBLIC) {
            return true;
        }
        if (Objects.equals(ownerId, visitorId) || moderator) {
            return true;
        }
        if (blockedPlayers.contains(visitorId)) {
            return false;
        }
        return switch (access) {
            case PUBLIC, UNLISTED -> true;
            case SHARED -> trustedPlayers.contains(visitorId);
            case PRIVATE -> false;
        };
    }

    public WarpRecord renamed(String replacement, Instant now) {
        return copy(
                ownerId,
                ownerNameSnapshot,
                normalizeName(replacement),
                replacement,
                scope,
                access,
                status,
                location,
                publishedAt,
                deletedAt,
                permission,
                icon,
                description,
                category,
                hidden,
                listed,
                featured,
                visits,
                safetyRevision,
                revision + 1,
                trustedPlayers,
                blockedPlayers,
                now);
    }

    public WarpRecord relocated(SavedLocation replacement, Instant now) {
        return copy(
                ownerId,
                ownerNameSnapshot,
                normalizedName,
                displayName,
                scope,
                access,
                status,
                replacement,
                publishedAt,
                deletedAt,
                permission,
                icon,
                description,
                category,
                hidden,
                listed,
                featured,
                visits,
                safetyRevision + 1,
                revision + 1,
                trustedPlayers,
                blockedPlayers,
                now);
    }

    public WarpRecord publication(Access replacement, Instant now) {
        Instant published = replacement == Access.PUBLIC || replacement == Access.UNLISTED ? now : null;
        Status publicationStatus = status == Status.DRAFT ? Status.ACTIVE : status;
        return copy(
                ownerId,
                ownerNameSnapshot,
                normalizedName,
                displayName,
                scope,
                replacement,
                publicationStatus,
                location,
                published,
                deletedAt,
                permission,
                icon,
                description,
                category,
                hidden,
                replacement != Access.UNLISTED,
                featured,
                visits,
                safetyRevision,
                revision + 1,
                trustedPlayers,
                blockedPlayers,
                now);
    }

    public WarpRecord status(Status replacement, Instant now) {
        return copy(
                ownerId,
                ownerNameSnapshot,
                normalizedName,
                displayName,
                scope,
                access,
                replacement,
                location,
                publishedAt,
                replacement == Status.DELETED ? now : deletedAt,
                permission,
                icon,
                description,
                category,
                hidden,
                listed,
                featured,
                visits,
                safetyRevision,
                revision + 1,
                trustedPlayers,
                blockedPlayers,
                now);
    }

    public WarpRecord visited(Instant now) {
        return copy(
                ownerId,
                ownerNameSnapshot,
                normalizedName,
                displayName,
                scope,
                access,
                status,
                location,
                publishedAt,
                deletedAt,
                permission,
                icon,
                description,
                category,
                hidden,
                listed,
                featured,
                visits + 1,
                safetyRevision,
                revision + 1,
                trustedPlayers,
                blockedPlayers,
                now);
    }

    public WarpRecord accessLists(Set<UUID> trusted, Set<UUID> blocked, Instant now) {
        return copy(
                ownerId,
                ownerNameSnapshot,
                normalizedName,
                displayName,
                scope,
                access,
                status,
                location,
                publishedAt,
                deletedAt,
                permission,
                icon,
                description,
                category,
                hidden,
                listed,
                featured,
                visits,
                safetyRevision,
                revision + 1,
                trusted,
                blocked,
                now);
    }

    public WarpRecord flags(boolean replacementHidden, boolean replacementListed, boolean replacementFeatured, Instant now) {
        return copy(
                ownerId,
                ownerNameSnapshot,
                normalizedName,
                displayName,
                scope,
                access,
                status,
                location,
                publishedAt,
                deletedAt,
                permission,
                icon,
                description,
                category,
                replacementHidden,
                replacementListed,
                replacementFeatured,
                visits,
                safetyRevision,
                revision + 1,
                trustedPlayers,
                blockedPlayers,
                now);
    }

    public WarpRecord transferred(UUID replacementOwner, String ownerName, Instant now) {
        return copy(
                Objects.requireNonNull(replacementOwner, "replacementOwner"),
                ownerName,
                normalizedName,
                displayName,
                scope,
                access,
                status,
                location,
                publishedAt,
                deletedAt,
                permission,
                icon,
                description,
                category,
                hidden,
                listed,
                featured,
                visits,
                safetyRevision,
                revision + 1,
                trustedPlayers,
                blockedPlayers,
                now);
    }

    private WarpRecord copy(
            UUID replacementOwner,
            String replacementOwnerName,
            String replacementNormalizedName,
            String replacementDisplayName,
            Scope replacementScope,
            Access replacementAccess,
            Status replacementStatus,
            SavedLocation replacementLocation,
            Instant replacementPublishedAt,
            Instant replacementDeletedAt,
            String replacementPermission,
            String replacementIcon,
            String replacementDescription,
            String replacementCategory,
            boolean replacementHidden,
            boolean replacementListed,
            boolean replacementFeatured,
            long replacementVisits,
            long replacementSafetyRevision,
            long replacementRevision,
            Set<UUID> replacementTrustedPlayers,
            Set<UUID> replacementBlockedPlayers,
            Instant now
    ) {
        return new WarpRecord(
                id,
                replacementOwner,
                replacementOwnerName,
                replacementNormalizedName,
                replacementDisplayName,
                replacementScope,
                replacementAccess,
                replacementStatus,
                replacementLocation,
                createdAt,
                now,
                replacementPublishedAt,
                replacementDeletedAt,
                replacementPermission,
                replacementIcon,
                replacementDescription,
                replacementCategory,
                replacementHidden,
                replacementListed,
                replacementFeatured,
                replacementVisits,
                replacementSafetyRevision,
                replacementRevision,
                sourceHomeId,
                replacementTrustedPlayers,
                replacementBlockedPlayers);
    }

    public static String normalizeName(String value) {
        String normalized = Objects.requireNonNull(value, "name").trim().toLowerCase(Locale.ROOT);
        if (!VALID_NAME.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Names may contain letters, numbers, underscores, and periods");
        }
        return normalized;
    }

    private static String bounded(String value, int maximum) {
        String result = value == null ? "" : value.trim();
        if (result.length() > maximum) {
            throw new IllegalArgumentException("Text exceeds its maximum length");
        }
        return result;
    }

    public enum Scope {
        SERVER_PUBLIC,
        PLAYER
    }

    public enum Access {
        PUBLIC,
        UNLISTED,
        SHARED,
        PRIVATE
    }

    public enum Status {
        DRAFT,
        ACTIVE,
        DISABLED,
        SUSPENDED,
        DELETED
    }
}
