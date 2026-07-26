package com.enviouse.sef.kernel.policy;

import com.enviouse.sef.kernel.ActionResult;

import java.time.Clock;
import java.time.Duration;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class WarmupService {
    private static final Duration HARD_MAXIMUM = Duration.ofHours(1);

    private final Clock clock;
    private final Map<UUID, Warmup> warmups = new ConcurrentHashMap<>();

    public WarmupService() {
        this(Clock.systemUTC());
    }

    WarmupService(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public ActionResult<Warmup> start(
            UUID actorId,
            String actionId,
            Position initialPosition,
            UUID targetId,
            Duration duration,
            Set<CancelReason> cancellationPolicy
    ) {
        Objects.requireNonNull(actorId, "actorId");
        Objects.requireNonNull(initialPosition, "initialPosition");
        Objects.requireNonNull(duration, "duration");
        if (duration.isNegative() || duration.isZero() || duration.compareTo(HARD_MAXIMUM) > 0) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "warmup duration is outside bounds");
        }
        Set<CancelReason> requestedPolicy = Objects.requireNonNull(cancellationPolicy, "cancellationPolicy");
        Set<CancelReason> policy = requestedPolicy.isEmpty()
                ? EnumSet.noneOf(CancelReason.class)
                : EnumSet.copyOf(requestedPolicy);
        long start = clock.millis();
        Warmup created = new Warmup(
                UUID.randomUUID(),
                actorId,
                normalize(actionId),
                initialPosition,
                targetId,
                start,
                start + duration.toMillis(),
                policy);
        Warmup existing = warmups.putIfAbsent(actorId, created);
        return existing == null
                ? ActionResult.success(created)
                : ActionResult.failure(ActionResult.ReasonCode.WARMUP_ACTIVE, "actor already has a warmup");
    }

    public ActionResult<Warmup> check(UUID actorId, Position currentPosition) {
        Warmup warmup = warmups.get(actorId);
        if (warmup == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "warmup not found");
        }
        if (clock.millis() >= warmup.expiresAtEpochMillis()) {
            warmups.remove(actorId, warmup);
            return ActionResult.success(warmup);
        }
        if (warmup.cancellationPolicy().contains(CancelReason.MOVEMENT)
                && warmup.initialPosition().distanceSquared(currentPosition) > 0.01D) {
            warmups.remove(actorId, warmup);
            return ActionResult.failure(ActionResult.ReasonCode.WARMUP_CANCELLED, "movement");
        }
        if (warmup.cancellationPolicy().contains(CancelReason.ROTATION)
                && warmup.initialPosition().rotationDifference(currentPosition) > 1.0F) {
            warmups.remove(actorId, warmup);
            return ActionResult.failure(ActionResult.ReasonCode.WARMUP_CANCELLED, "rotation");
        }
        return ActionResult.failure(ActionResult.ReasonCode.WARMUP_ACTIVE, "warmup has not completed");
    }

    public boolean cancel(UUID actorId, CancelReason reason) {
        Warmup warmup = warmups.get(actorId);
        return warmup != null
                && warmup.cancellationPolicy().contains(reason)
                && warmups.remove(actorId, warmup);
    }

    public Optional<Warmup> inspect(UUID actorId) {
        return Optional.ofNullable(warmups.get(actorId));
    }

    public boolean clear(UUID actorId) {
        return warmups.remove(actorId) != null;
    }

    public void clear() {
        warmups.clear();
    }

    public int size() {
        return warmups.size();
    }

    public record Warmup(
            UUID id,
            UUID actorId,
            String actionId,
            Position initialPosition,
            UUID targetId,
            long startedAtEpochMillis,
            long expiresAtEpochMillis,
            Set<CancelReason> cancellationPolicy
    ) {
        public Warmup {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(actorId, "actorId");
            actionId = normalize(actionId);
            Objects.requireNonNull(initialPosition, "initialPosition");
            cancellationPolicy = Set.copyOf(Objects.requireNonNull(cancellationPolicy, "cancellationPolicy"));
            if (expiresAtEpochMillis <= startedAtEpochMillis) {
                throw new IllegalArgumentException("Warmup expiry must follow start");
            }
        }
    }

    public record Position(
            String dimensionId,
            double x,
            double y,
            double z,
            float yaw,
            float pitch
    ) {
        public Position {
            dimensionId = normalize(dimensionId);
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                    || !Float.isFinite(yaw) || !Float.isFinite(pitch)) {
                throw new IllegalArgumentException("Warmup position must be finite");
            }
        }

        double distanceSquared(Position other) {
            if (!dimensionId.equals(other.dimensionId)) {
                return Double.POSITIVE_INFINITY;
            }
            double dx = x - other.x;
            double dy = y - other.y;
            double dz = z - other.z;
            return dx * dx + dy * dy + dz * dz;
        }

        float rotationDifference(Position other) {
            return Math.max(Math.abs(yaw - other.yaw), Math.abs(pitch - other.pitch));
        }
    }

    public enum CancelReason {
        MOVEMENT,
        ROTATION,
        DAMAGE,
        COMBAT_TAG,
        DEATH,
        LOGOUT,
        DIMENSION_CHANGE,
        PERMISSION_LOSS,
        FEATURE_DISABLE
    }

    private static String normalize(String value) {
        return Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
    }
}
