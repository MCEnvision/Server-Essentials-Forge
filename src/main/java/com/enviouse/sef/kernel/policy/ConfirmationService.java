package com.enviouse.sef.kernel.policy;

import com.enviouse.sef.kernel.ActionResult;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfirmationService {
    private static final Duration HARD_MAXIMUM = Duration.ofMinutes(10);
    private static final int MAXIMUM_PENDING = 4096;

    private final Clock clock;
    private final SecureRandom random;
    private final Map<String, Binding> pending = new ConcurrentHashMap<>();

    public ConfirmationService() {
        this(Clock.systemUTC(), new SecureRandom());
    }

    ConfirmationService(Clock clock, SecureRandom random) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.random = Objects.requireNonNull(random, "random");
    }

    public synchronized ActionResult<IssuedToken> issue(Request request, Duration lifetime) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(lifetime, "lifetime");
        prune();
        if (lifetime.isNegative() || lifetime.isZero() || lifetime.compareTo(HARD_MAXIMUM) > 0) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "confirmation lifetime is outside bounds");
        }
        if (pending.size() >= MAXIMUM_PENDING) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "confirmation capacity reached");
        }
        byte[] tokenBytes = new byte[18];
        random.nextBytes(tokenBytes);
        String token = HexFormat.of().formatHex(tokenBytes);
        String digest = digest(token);
        long expiresAt = clock.millis() + lifetime.toMillis();
        pending.put(digest, new Binding(request, expiresAt));
        return ActionResult.success(new IssuedToken(token, expiresAt));
    }

    public synchronized ActionResult<Request> consume(String token, Request presented) {
        Objects.requireNonNull(presented, "presented");
        if (token == null || token.length() != 36) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFIRMATION_INVALID, "confirmation token is invalid");
        }
        Binding binding = pending.remove(digest(token));
        if (binding == null) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFIRMATION_INVALID, "confirmation token is unknown or used");
        }
        if (binding.expiresAtEpochMillis() < clock.millis()) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFIRMATION_EXPIRED, "confirmation token expired");
        }
        if (!binding.request().equals(presented)) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFIRMATION_INVALID, "confirmation binding changed");
        }
        return ActionResult.success(binding.request());
    }

    public synchronized void revokeActor(UUID actorId) {
        pending.entrySet().removeIf(entry -> entry.getValue().request().actorId().equals(actorId));
    }

    public synchronized void clear() {
        pending.clear();
    }

    public synchronized int size() {
        prune();
        return pending.size();
    }

    private void prune() {
        long now = clock.millis();
        pending.entrySet().removeIf(entry -> entry.getValue().expiresAtEpochMillis() < now);
    }

    public record Request(
            UUID actorId,
            String actionId,
            Map<String, String> normalizedParameters,
            List<UUID> targetIds,
            String panelId,
            long aliasRevision,
            long bundleRevision,
            long executionProfileRevision,
            long policyRevision
    ) {
        public Request {
            Objects.requireNonNull(actorId, "actorId");
            actionId = normalize(actionId);
            Objects.requireNonNull(normalizedParameters, "normalizedParameters");
            Objects.requireNonNull(targetIds, "targetIds");
            if (normalizedParameters.size() > 32 || targetIds.size() > 100) {
                throw new IllegalArgumentException("Confirmation request exceeds bounds");
            }
            normalizedParameters = normalizedParameters.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(java.util.stream.Collectors.toUnmodifiableMap(
                            entry -> normalize(entry.getKey()),
                            entry -> bounded(entry.getValue(), 256)));
            targetIds = targetIds.stream().sorted(Comparator.comparing(UUID::toString)).toList();
            panelId = panelId == null || panelId.isBlank() ? "" : normalize(panelId);
            if (aliasRevision < 0 || bundleRevision < 0 || executionProfileRevision < 0 || policyRevision < 0) {
                throw new IllegalArgumentException("Confirmation request exceeds bounds");
            }
        }
    }

    public record IssuedToken(String token, long expiresAtEpochMillis) {
    }

    private record Binding(Request request, long expiresAtEpochMillis) {
    }

    private static String digest(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static String normalize(String value) {
        String normalized = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank() || normalized.length() > 128) {
            throw new IllegalArgumentException("Confirmation identifier exceeds bounds");
        }
        return normalized;
    }

    private static String bounded(String value, int maximumLength) {
        String normalized = Objects.requireNonNull(value, "value")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
        return normalized.length() <= maximumLength ? normalized : normalized.substring(0, maximumLength);
    }
}
