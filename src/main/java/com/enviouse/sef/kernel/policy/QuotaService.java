package com.enviouse.sef.kernel.policy;

import com.enviouse.sef.kernel.ActionResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.time.Instant;

public final class QuotaService {
    private final Map<String, Definition> definitions = new ConcurrentHashMap<>();
    private final List<Provider> providers = new ArrayList<>();
    private final Map<ReservationKey, AtomicLong> reservations = new ConcurrentHashMap<>();
    private final Map<String, ProviderDiagnostic> providerDiagnostics = new ConcurrentHashMap<>();
    private final AtomicLong revision = new AtomicLong(1L);

    public synchronized void register(Definition definition) {
        Objects.requireNonNull(definition, "definition");
        if (definitions.putIfAbsent(definition.id(), definition) != null) {
            throw new IllegalStateException("Duplicate quota " + definition.id());
        }
        revision.incrementAndGet();
    }

    public synchronized void setProviders(List<Provider> replacements) {
        if (Objects.requireNonNull(replacements, "replacements").size() > 32) {
            throw new IllegalArgumentException("Too many quota providers");
        }
        providers.clear();
        for (Provider provider : replacements) {
            providers.add(Objects.requireNonNull(provider, "provider"));
        }
        providers.sort(Comparator.comparingInt(Provider::priority).reversed());
        revision.incrementAndGet();
    }

    public Decision resolve(Context context) {
        Objects.requireNonNull(context, "context");
        Definition definition = definitions.get(context.quotaId());
        if (definition == null) {
            throw new IllegalArgumentException("Unknown quota " + context.quotaId());
        }

        Candidate candidate = null;
        List<Provider> providerSnapshot;
        synchronized (this) {
            providerSnapshot = List.copyOf(providers);
        }
        for (Provider provider : providerSnapshot) {
            Candidate provided;
            String providerId = safeProviderId(provider);
            try {
                provided = provider.resolve(definition, context);
                providerDiagnostics.remove(providerId);
            } catch (RuntimeException | LinkageError exception) {
                providerDiagnostics.put(
                        providerId,
                        new ProviderDiagnostic(
                                providerId,
                                Instant.now(),
                                exception.getClass().getSimpleName()));
                continue;
            }
            if (provided != null) {
                candidate = provided;
                break;
            }
        }
        if (candidate == null) {
            candidate = resolvePermissionTier(definition, context);
        }
        if (candidate == null) {
            candidate = resolveInternalOverride(definition, context);
        }
        if (candidate == null) {
            candidate = new Candidate(
                    false,
                    definition.defaultValue(),
                    "internal_default",
                    "default",
                    ActionResult.ReasonCode.SUCCESS);
        }

        boolean unlimited = candidate.unlimited() && definition.allowUnlimited();
        long effective = unlimited
                ? definition.hardCeiling()
                : Math.min(Math.max(0L, candidate.value()), definition.hardCeiling());
        long reserved = reserved(context.subjectId(), definition.id());
        long remaining = Math.max(0L, effective - context.currentUsage() - reserved);
        return new Decision(
                definition.id(),
                context.subjectId(),
                context.contextId(),
                effective,
                unlimited,
                candidate.provider(),
                candidate.ruleId(),
                definition.hardCeiling(),
                context.currentUsage(),
                reserved,
                remaining,
                candidate.reason(),
                revision.get());
    }

    public ActionResult<Reservation> reserve(Context context, long amount) {
        if (amount <= 0) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "reservation amount must be positive");
        }
        ReservationKey key = new ReservationKey(context.subjectId(), context.quotaId());
        AtomicLong counter = reservations.computeIfAbsent(key, ignored -> new AtomicLong());
        long reservationRevision;
        synchronized (counter) {
            Decision refreshed = resolve(context);
            if (amount > refreshed.remaining()) {
                return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "quota remaining is insufficient");
            }
            counter.addAndGet(amount);
            reservationRevision = refreshed.revision();
        }
        return ActionResult.success(new Reservation(
                UUID.randomUUID(),
                key,
                amount,
                reservationRevision,
                this));
    }

    public long reserved(UUID subjectId, String quotaId) {
        AtomicLong counter = reservations.get(new ReservationKey(subjectId, normalize(quotaId)));
        return counter == null ? 0L : Math.max(0L, counter.get());
    }

    public void invalidate() {
        revision.incrementAndGet();
    }

    public long revision() {
        return revision.get();
    }

    public List<Definition> definitions() {
        return definitions.values().stream().sorted(Comparator.comparing(Definition::id)).toList();
    }

    public List<ProviderDiagnostic> providerDiagnostics() {
        return providerDiagnostics.values().stream()
                .sorted(Comparator.comparing(ProviderDiagnostic::providerId))
                .toList();
    }

    private Candidate resolvePermissionTier(Definition definition, Context context) {
        return definition.permissionTiers().entrySet().stream()
                .filter(entry -> context.permissionIds().contains(entry.getKey()))
                .max(Map.Entry.comparingByValue())
                .map(entry -> new Candidate(
                        false,
                        entry.getValue(),
                        "permission_tier",
                        entry.getKey(),
                        ActionResult.ReasonCode.SUCCESS))
                .orElse(null);
    }

    private Candidate resolveInternalOverride(Definition definition, Context context) {
        Long value = context.internalOverrides().get(definition.id());
        return value == null
                ? null
                : Candidate.finite(value, "internal_override", definition.id());
    }

    private void release(ReservationKey key, long amount) {
        AtomicLong counter = reservations.get(key);
        if (counter == null) {
            return;
        }
        synchronized (counter) {
            long remaining = Math.max(0L, counter.addAndGet(-amount));
            counter.set(remaining);
            if (remaining == 0L) {
                reservations.remove(key, counter);
            }
        }
    }

    public record Definition(
            String id,
            QuotaKind kind,
            long defaultValue,
            long hardCeiling,
            boolean allowUnlimited,
            Map<String, Long> permissionTiers
    ) {
        public Definition {
            id = normalize(id);
            Objects.requireNonNull(kind, "kind");
            permissionTiers = normalizeTiers(permissionTiers);
            if (permissionTiers.size() > 64) {
                throw new IllegalArgumentException("Too many quota permission tiers");
            }
            if (defaultValue < 0 || hardCeiling < 1 || defaultValue > hardCeiling) {
                throw new IllegalArgumentException("Quota values are outside bounds");
            }
            if (permissionTiers.values().stream().anyMatch(value -> value < 0 || value > hardCeiling)) {
                throw new IllegalArgumentException("Quota tier exceeds hard ceiling");
            }
        }
    }

    public record Context(
            String quotaId,
            UUID subjectId,
            String contextId,
            String worldId,
            String dimensionId,
            String actionId,
            Set<String> permissionIds,
            Map<String, String> metadata,
            Map<String, Long> internalOverrides,
            long currentUsage
    ) {
        public Context {
            Objects.requireNonNull(subjectId, "subjectId");
            Objects.requireNonNull(permissionIds, "permissionIds");
            if (permissionIds.size() > 256) {
                throw new IllegalArgumentException("Too many quota permission ids");
            }
            quotaId = normalize(quotaId);
            contextId = normalize(contextId);
            worldId = normalize(worldId);
            dimensionId = normalize(dimensionId);
            actionId = normalize(actionId);
            permissionIds = permissionIds.stream().map(QuotaService::normalize)
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
            metadata = normalizeStrings(metadata);
            internalOverrides = normalizeLongs(internalOverrides);
            if (currentUsage < 0) {
                throw new IllegalArgumentException("Quota usage cannot be negative");
            }
        }
    }

    public record Decision(
            String quotaId,
            UUID subjectId,
            String contextId,
            long effectiveValue,
            boolean unlimited,
            String provider,
            String ruleId,
            long hardCeiling,
            long currentUsage,
            long reservedUsage,
            long remaining,
            ActionResult.ReasonCode reason,
            long revision
    ) {
    }

    public record Candidate(
            boolean unlimited,
            long value,
            String provider,
            String ruleId,
            ActionResult.ReasonCode reason
    ) {
        public Candidate {
            provider = normalize(provider);
            ruleId = normalize(ruleId);
            Objects.requireNonNull(reason, "reason");
            if (value < 0) {
                throw new IllegalArgumentException("Quota candidate cannot be negative");
            }
        }

        public static Candidate finite(long value, String provider, String ruleId) {
            return new Candidate(false, value, provider, ruleId, ActionResult.ReasonCode.SUCCESS);
        }

        public static Candidate unlimited(String provider, String ruleId) {
            return new Candidate(true, 0L, provider, ruleId, ActionResult.ReasonCode.SUCCESS);
        }
    }

    public interface Provider {
        String id();

        int priority();

        Candidate resolve(Definition definition, Context context);
    }

    public record ProviderDiagnostic(String providerId, Instant observedAt, String detail) {
        public ProviderDiagnostic {
            providerId = normalize(providerId);
            Objects.requireNonNull(observedAt, "observedAt");
            detail = bounded(detail, 128);
        }
    }

    public static final class ContextMetadataProvider implements Provider {
        @Override
        public String id() {
            return "context_metadata";
        }

        @Override
        public int priority() {
            return 100;
        }

        @Override
        public Candidate resolve(Definition definition, Context context) {
            String raw = context.metadata().get(definition.id());
            if (raw == null) {
                return null;
            }
            if (raw.equalsIgnoreCase("unlimited")) {
                return definition.allowUnlimited() ? Candidate.unlimited(id(), definition.id()) : null;
            }
            try {
                long parsed = Long.parseLong(raw);
                return parsed < 0 ? null : Candidate.finite(parsed, id(), definition.id());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }

    public static final class Reservation implements AutoCloseable {
        private final UUID id;
        private final ReservationKey key;
        private final long amount;
        private final long quotaRevision;
        private final QuotaService owner;
        private boolean closed;
        private boolean committed;

        private Reservation(UUID id, ReservationKey key, long amount, long quotaRevision, QuotaService owner) {
            this.id = id;
            this.key = key;
            this.amount = amount;
            this.quotaRevision = quotaRevision;
            this.owner = owner;
        }

        public synchronized void commit() {
            if (closed) {
                throw new IllegalStateException("Reservation is closed");
            }
            committed = true;
            close();
        }

        @Override
        public synchronized void close() {
            if (closed) {
                return;
            }
            closed = true;
            owner.release(key, amount);
        }

        public UUID id() {
            return id;
        }

        public long amount() {
            return amount;
        }

        public long quotaRevision() {
            return quotaRevision;
        }

        public synchronized boolean committed() {
            return committed;
        }
    }

    public enum QuotaKind {
        COUNT,
        TARGET_CAP,
        STORAGE_RETENTION,
        DEFINITION_COUNT,
        RATE,
        RESOURCE_BUDGET
    }

    private record ReservationKey(UUID subjectId, String quotaId) {
    }

    private static Map<String, Long> normalizeTiers(Map<String, Long> values) {
        return normalizeLongs(values);
    }

    private static Map<String, Long> normalizeLongs(Map<String, Long> values) {
        Objects.requireNonNull(values, "values");
        if (values.size() > 64) {
            throw new IllegalArgumentException("Too many quota values");
        }
        Map<String, Long> normalized = new LinkedHashMap<>();
        values.forEach((key, value) -> {
            if (normalized.putIfAbsent(normalize(key), Objects.requireNonNull(value, "value")) != null) {
                throw new IllegalArgumentException("Duplicate normalized quota value");
            }
        });
        return Map.copyOf(normalized);
    }

    private static Map<String, String> normalizeStrings(Map<String, String> values) {
        Objects.requireNonNull(values, "values");
        if (values.size() > 64) {
            throw new IllegalArgumentException("Too many quota metadata values");
        }
        Map<String, String> normalized = new LinkedHashMap<>();
        values.forEach((key, value) -> {
            String normalizedValue = Objects.requireNonNull(value, "value").trim();
            if (normalizedValue.length() > 128) {
                throw new IllegalArgumentException("Quota metadata value is outside bounds");
            }
            if (normalized.putIfAbsent(
                    normalize(key),
                    normalizedValue) != null) {
                throw new IllegalArgumentException("Duplicate normalized quota metadata");
            }
        });
        return Map.copyOf(normalized);
    }

    private static String normalize(String value) {
        String normalized = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank() || normalized.length() > 128) {
            throw new IllegalArgumentException("Quota identifier is outside bounds");
        }
        return normalized;
    }

    private static String bounded(String value, int maximumLength) {
        return value.length() <= maximumLength ? value : value.substring(0, maximumLength);
    }

    private static String safeProviderId(Provider provider) {
        try {
            return normalize(provider.id());
        } catch (RuntimeException exception) {
            String fallback = provider.getClass().getSimpleName().trim().toLowerCase(Locale.ROOT);
            return fallback.isBlank() ? "unknown_provider" : bounded(fallback, 128);
        }
    }
}
