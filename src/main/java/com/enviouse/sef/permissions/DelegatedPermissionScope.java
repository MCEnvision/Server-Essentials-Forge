package com.enviouse.sef.permissions;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public final class DelegatedPermissionScope {
    private static final ThreadLocal<Authority> ACTIVE = new ThreadLocal<>();

    private DelegatedPermissionScope() {
    }

    public static <T> T preview(
            UUID subjectId,
            String normalizedRoot,
            String canonicalActionId,
            Set<String> temporarySefPermissionIds,
            Supplier<T> operation
    ) {
        Objects.requireNonNull(subjectId, "subjectId");
        Objects.requireNonNull(operation, "operation");
        if (ACTIVE.get() != null) {
            throw new IllegalStateException("nested delegated permission scope is not allowed");
        }
        Authority authority = new Authority(
                null,
                subjectId,
                Objects.requireNonNull(normalizedRoot, "normalizedRoot"),
                Objects.requireNonNull(canonicalActionId, "canonicalActionId"),
                Set.copyOf(Objects.requireNonNull(temporarySefPermissionIds, "temporarySefPermissionIds")),
                Thread.currentThread().threadId(),
                true);
        ACTIVE.set(authority);
        try {
            return operation.get();
        } finally {
            ACTIVE.remove();
        }
    }

    public static <T> T execute(
            EphemeralExecutionGrant grant,
            String command,
            String canonicalActionId,
            Supplier<T> operation
    ) {
        Objects.requireNonNull(grant, "grant");
        Objects.requireNonNull(operation, "operation");
        if (ACTIVE.get() != null) {
            throw new IllegalStateException("nested delegated permission scope is not allowed");
        }
        if (!grant.matchesCommand(command)
                || !grant.canonicalActionId().equals(canonicalActionId)
                || !grant.consume(Instant.now())) {
            throw new IllegalStateException("delegated permission grant is invalid, expired, or used");
        }
        Authority authority = new Authority(
                grant,
                grant.targetId(),
                grant.normalizedRoot(),
                grant.canonicalActionId(),
                grant.temporarySefPermissionIds(),
                Thread.currentThread().threadId(),
                false);
        ACTIVE.set(authority);
        try {
            return operation.get();
        } finally {
            ACTIVE.remove();
        }
    }

    public static boolean allows(UUID subjectId, String permissionId) {
        Authority authority = ACTIVE.get();
        return authority != null
                && authority.subjectId().equals(subjectId)
                && authority.threadId() == Thread.currentThread().threadId()
                && authority.temporarySefPermissionIds().contains(
                        Objects.requireNonNull(permissionId, "permissionId").trim().toLowerCase(java.util.Locale.ROOT))
                && (authority.preview()
                || authority.grant().used() && authority.grant().validAt(Instant.now()));
    }

    public static boolean actionAllowed(String canonicalActionId) {
        Authority authority = ACTIVE.get();
        return authority == null
                || authority.threadId() != Thread.currentThread().threadId()
                || authority.canonicalActionId().equals(
                        Objects.requireNonNull(canonicalActionId, "canonicalActionId")
                                .trim()
                                .toLowerCase(java.util.Locale.ROOT));
    }

    public static boolean active() {
        return ACTIVE.get() != null;
    }

    public static String fingerprint(String command) {
        String normalized = Objects.requireNonNull(command, "command").strip();
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(normalized.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public static UUID activeGrantId() {
        Authority authority = ACTIVE.get();
        return authority == null || authority.grant() == null ? null : authority.grant().grantId();
    }

    private record Authority(
            EphemeralExecutionGrant grant,
            UUID subjectId,
            String normalizedRoot,
            String canonicalActionId,
            Set<String> temporarySefPermissionIds,
            long threadId,
            boolean preview
    ) {
    }
}
