package com.enviouse.sef.permissions;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public final class EphemeralExecutionGrant {
    private static final Pattern IDENTIFIER = Pattern.compile("[a-z0-9][a-z0-9_.:-]{0,127}");
    private static final int HARD_MAXIMUM_VANILLA_PERMISSION_LEVEL = 2;

    private final UUID grantId;
    private final UUID issuerId;
    private final UUID targetId;
    private final long targetSessionRevision;
    private final String normalizedRoot;
    private final String canonicalActionId;
    private final String commandDigest;
    private final long commandTreeRevision;
    private final String profileId;
    private final long profileRevision;
    private final int maximumTemporaryVanillaPermissionLevel;
    private final Set<String> temporarySefPermissionIds;
    private final Set<String> approvedAdapterCapabilities;
    private final long sudoPolicyRevision;
    private final long permissionProviderRevision;
    private final long featureRevision;
    private final long configurationRevision;
    private final Instant createdAt;
    private final Instant expiresAt;
    private final UUID confirmationId;
    private final UUID auditCorrelationId;
    private final AtomicBoolean used = new AtomicBoolean();

    public EphemeralExecutionGrant(
            UUID grantId,
            UUID issuerId,
            UUID targetId,
            long targetSessionRevision,
            String normalizedRoot,
            String canonicalActionId,
            String commandDigest,
            long commandTreeRevision,
            String profileId,
            long profileRevision,
            int maximumTemporaryVanillaPermissionLevel,
            Set<String> temporarySefPermissionIds,
            Set<String> approvedAdapterCapabilities,
            long sudoPolicyRevision,
            long permissionProviderRevision,
            long featureRevision,
            long configurationRevision,
            Instant createdAt,
            Instant expiresAt,
            UUID confirmationId,
            UUID auditCorrelationId
    ) {
        this.grantId = Objects.requireNonNull(grantId, "grantId");
        this.issuerId = Objects.requireNonNull(issuerId, "issuerId");
        this.targetId = Objects.requireNonNull(targetId, "targetId");
        this.targetSessionRevision = positive(targetSessionRevision, "targetSessionRevision");
        this.normalizedRoot = identifier(normalizedRoot, "normalizedRoot");
        this.canonicalActionId = identifier(canonicalActionId, "canonicalActionId");
        this.commandDigest = digest(commandDigest);
        this.commandTreeRevision = positive(commandTreeRevision, "commandTreeRevision");
        this.profileId = identifier(profileId, "profileId");
        this.profileRevision = positive(profileRevision, "profileRevision");
        if (maximumTemporaryVanillaPermissionLevel < 0
                || maximumTemporaryVanillaPermissionLevel > HARD_MAXIMUM_VANILLA_PERMISSION_LEVEL) {
            throw new IllegalArgumentException("maximumTemporaryVanillaPermissionLevel is outside bounds");
        }
        this.maximumTemporaryVanillaPermissionLevel = maximumTemporaryVanillaPermissionLevel;
        this.temporarySefPermissionIds = identifiers(temporarySefPermissionIds, "temporarySefPermissionIds");
        this.approvedAdapterCapabilities = identifiers(
                approvedAdapterCapabilities,
                "approvedAdapterCapabilities");
        this.sudoPolicyRevision = positive(sudoPolicyRevision, "sudoPolicyRevision");
        this.permissionProviderRevision = positive(permissionProviderRevision, "permissionProviderRevision");
        this.featureRevision = positive(featureRevision, "featureRevision");
        this.configurationRevision = positive(configurationRevision, "configurationRevision");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt");
        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("grant lifetime is invalid");
        }
        this.confirmationId = Objects.requireNonNull(confirmationId, "confirmationId");
        this.auditCorrelationId = Objects.requireNonNull(auditCorrelationId, "auditCorrelationId");
    }

    public boolean consume(Instant now) {
        Objects.requireNonNull(now, "now");
        return validAt(now) && used.compareAndSet(false, true);
    }

    public boolean validAt(Instant now) {
        Objects.requireNonNull(now, "now");
        return !now.isBefore(createdAt) && now.isBefore(expiresAt);
    }

    public boolean permitsSefPermission(String permissionId) {
        return temporarySefPermissionIds.contains(identifier(permissionId, "permissionId"));
    }

    public boolean matchesCommand(String command) {
        return commandDigest.equals(DelegatedPermissionScope.fingerprint(command));
    }

    public UUID grantId() {
        return grantId;
    }

    public UUID issuerId() {
        return issuerId;
    }

    public UUID targetId() {
        return targetId;
    }

    public long targetSessionRevision() {
        return targetSessionRevision;
    }

    public String normalizedRoot() {
        return normalizedRoot;
    }

    public String canonicalActionId() {
        return canonicalActionId;
    }

    public String commandDigest() {
        return commandDigest;
    }

    public long commandTreeRevision() {
        return commandTreeRevision;
    }

    public String profileId() {
        return profileId;
    }

    public long profileRevision() {
        return profileRevision;
    }

    public int maximumTemporaryVanillaPermissionLevel() {
        return maximumTemporaryVanillaPermissionLevel;
    }

    public Set<String> temporarySefPermissionIds() {
        return temporarySefPermissionIds;
    }

    public Set<String> approvedAdapterCapabilities() {
        return approvedAdapterCapabilities;
    }

    public long sudoPolicyRevision() {
        return sudoPolicyRevision;
    }

    public long permissionProviderRevision() {
        return permissionProviderRevision;
    }

    public long featureRevision() {
        return featureRevision;
    }

    public long configurationRevision() {
        return configurationRevision;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public int maximumInvocations() {
        return 1;
    }

    public boolean used() {
        return used.get();
    }

    public UUID confirmationId() {
        return confirmationId;
    }

    public UUID auditCorrelationId() {
        return auditCorrelationId;
    }

    public static int hardMaximumVanillaPermissionLevel() {
        return HARD_MAXIMUM_VANILLA_PERMISSION_LEVEL;
    }

    private static Set<String> identifiers(Set<String> values, String name) {
        Objects.requireNonNull(values, name);
        if (values.size() > 128) {
            throw new IllegalArgumentException(name + " exceeds bounds");
        }
        return values.stream()
                .map(value -> identifier(value, name))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private static String identifier(String value, String name) {
        String normalized = Objects.requireNonNull(value, name).trim().toLowerCase(Locale.ROOT);
        if (!IDENTIFIER.matcher(normalized).matches()) {
            throw new IllegalArgumentException(name + " is invalid");
        }
        return normalized;
    }

    private static String digest(String value) {
        String normalized = Objects.requireNonNull(value, "commandDigest").trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("commandDigest is invalid");
        }
        return normalized;
    }

    private static long positive(long value, String name) {
        if (value < 1L) {
            throw new IllegalArgumentException(name + " is outside bounds");
        }
        return value;
    }
}
