package com.enviouse.sef.control;

import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.enviouse.sef.kernel.policy.QuotaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessLeaseRepositoryTest {
    private static final String CRAFT = "sef.commands.craft";
    private static final UUID ISSUER = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SUBJECT = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @TempDir
    Path temporaryDirectory;

    @Test
    void leaseLifecycleIsAuthoritativeAndRevisionChecked() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        AccessLeaseRepository repository = repository(clock);
        var profile = publish(repository, AccessLeaseRepository.Scope.global());
        var created = repository.create(
                profile.id(),
                SUBJECT,
                ISSUER,
                Duration.ofHours(1),
                "event support",
                "internal",
                Long.toString(profile.revision()));

        assertTrue(created.successful());
        assertEquals(
                AccessLeaseRepository.LeaseDecision.GRANTED,
                repository.decide(SUBJECT, CRAFT, AccessLeaseRepository.ScopeContext.offline()));

        var suspended = repository.transition(
                created.value().id(),
                ISSUER,
                AccessLeaseRepository.LeaseState.SUSPENDED,
                "pause",
                created.value().revision());
        assertTrue(suspended.successful());
        assertEquals(
                AccessLeaseRepository.LeaseDecision.ABSTAIN,
                repository.decide(SUBJECT, CRAFT, AccessLeaseRepository.ScopeContext.offline()));

        var staleResume = repository.transition(
                created.value().id(),
                ISSUER,
                AccessLeaseRepository.LeaseState.ACTIVE,
                "",
                created.value().revision());
        assertFalse(staleResume.successful());
        assertEquals(ActionResult.ReasonCode.CONFLICT, staleResume.reason());

        var resumed = repository.transition(
                suspended.value().id(),
                ISSUER,
                AccessLeaseRepository.LeaseState.ACTIVE,
                "",
                suspended.value().revision());
        assertTrue(resumed.successful());
        assertEquals(
                AccessLeaseRepository.LeaseDecision.GRANTED,
                repository.decide(SUBJECT, CRAFT, AccessLeaseRepository.ScopeContext.offline()));

        clock.advance(Duration.ofHours(2));
        assertEquals(
                AccessLeaseRepository.LeaseDecision.ABSTAIN,
                repository.decide(SUBJECT, CRAFT, AccessLeaseRepository.ScopeContext.offline()));
        assertEquals(
                AccessLeaseRepository.LeaseState.EXPIRED,
                repository.lease(created.value().id()).orElseThrow().state());
    }

    @Test
    void immutableLeaseSnapshotSurvivesProfileRevision() {
        AccessLeaseRepository repository = repository(Clock.systemUTC());
        var original = publish(repository, AccessLeaseRepository.Scope.global());
        var lease = repository.create(
                original.id(),
                SUBJECT,
                ISSUER,
                Duration.ofHours(1),
                "support",
                "internal",
                Long.toString(original.revision())).value();
        var replacement = repository.publishProfile(
                original.id(),
                ISSUER,
                Set.of("sef.commands.anvil"),
                Map.of(),
                Duration.ofHours(8),
                false,
                false,
                AccessLeaseRepository.Scope.global());

        assertTrue(replacement.successful());
        assertEquals(Set.of(CRAFT), repository.lease(lease.id()).orElseThrow().permissions());
        var renewal = repository.renew(
                lease.id(),
                ISSUER,
                Duration.ofHours(1),
                "renew",
                lease.revision());
        assertFalse(renewal.successful());
        assertEquals(ActionResult.ReasonCode.CONFLICT, renewal.reason());
    }

    @Test
    void scopedLeaseRequiresMatchingContext() {
        AccessLeaseRepository repository = repository(Clock.systemUTC());
        var profile = publish(
                repository,
                new AccessLeaseRepository.Scope(AccessLeaseRepository.ScopeKind.DIMENSION, "minecraft:the_nether"));
        repository.create(
                profile.id(),
                SUBJECT,
                ISSUER,
                Duration.ofHours(1),
                "nether event",
                "internal",
                Long.toString(profile.revision()));

        assertEquals(
                AccessLeaseRepository.LeaseDecision.ABSTAIN,
                repository.decide(
                        SUBJECT,
                        CRAFT,
                        new AccessLeaseRepository.ScopeContext(
                                "minecraft:overworld",
                                "minecraft:overworld",
                                "",
                                Set.of())));
        assertEquals(
                AccessLeaseRepository.LeaseDecision.GRANTED,
                repository.decide(
                        SUBJECT,
                        CRAFT,
                        new AccessLeaseRepository.ScopeContext(
                                "minecraft:the_nether",
                                "minecraft:the_nether",
                                "",
                                Set.of())));
    }

    @Test
    void quotaProviderUsesActiveLeaseSnapshotAndHardCeiling() {
        AccessLeaseRepository repository = repository(Clock.systemUTC());
        var profile = repository.publishProfile(
                "builder",
                ISSUER,
                Set.of(CRAFT),
                Map.of("sef:homes", 25L),
                Duration.ofHours(8),
                false,
                false,
                AccessLeaseRepository.Scope.global()).value();
        var lease = repository.create(
                profile.id(),
                SUBJECT,
                ISSUER,
                Duration.ofHours(1),
                "builder",
                "internal",
                Long.toString(profile.revision())).value();
        AccessLeaseQuotaProvider provider = new AccessLeaseQuotaProvider(repository);
        QuotaService.Definition definition = new QuotaService.Definition(
                "sef:homes",
                QuotaService.QuotaKind.COUNT,
                1L,
                10L,
                false,
                Map.of());
        QuotaService.Context context = new QuotaService.Context(
                "sef:homes",
                SUBJECT,
                "global",
                "minecraft:overworld",
                "minecraft:overworld",
                "sef:home.set",
                Set.of(),
                Map.of(),
                Map.of(),
                0L);

        assertEquals(10L, provider.resolve(definition, context).value());
        repository.transition(
                lease.id(),
                ISSUER,
                AccessLeaseRepository.LeaseState.REVOKED,
                "complete",
                lease.revision());
        assertEquals(null, provider.resolve(definition, context));
    }

    @Test
    void duplicateSelfAndDangerousGrantsAreRejected() {
        AccessLeaseRepository repository = repository(Clock.systemUTC());
        var forbidden = repository.publishProfile(
                "unsafe",
                ISSUER,
                Set.of("sef.commands.run.server"),
                Map.of(),
                Duration.ofHours(1),
                false,
                false,
                AccessLeaseRepository.Scope.global());
        assertFalse(forbidden.successful());

        var profile = publish(repository, AccessLeaseRepository.Scope.global());
        var self = repository.create(
                profile.id(),
                ISSUER,
                ISSUER,
                Duration.ofHours(1),
                "self",
                "internal",
                Long.toString(profile.revision()));
        assertFalse(self.successful());
        assertEquals(ActionResult.ReasonCode.POLICY_DENIED, self.reason());

        var first = repository.create(
                profile.id(),
                SUBJECT,
                ISSUER,
                Duration.ofHours(1),
                "first",
                "internal",
                Long.toString(profile.revision()));
        var duplicate = repository.create(
                profile.id(),
                SUBJECT,
                ISSUER,
                Duration.ofMinutes(30),
                "duplicate",
                "internal",
                Long.toString(profile.revision()));
        assertTrue(first.successful());
        assertFalse(duplicate.successful());
        assertEquals(ActionResult.ReasonCode.CONFLICT, duplicate.reason());
    }

    @Test
    void persistenceRoundTripRetainsProfileLeaseAndHistory() throws Exception {
        AccessLeaseRepository repository = repository(Clock.systemUTC());
        repository.load(temporaryDirectory);
        var profile = publish(repository, AccessLeaseRepository.Scope.global());
        var lease = repository.create(
                profile.id(),
                SUBJECT,
                ISSUER,
                Duration.ofHours(1),
                "persist",
                "internal",
                Long.toString(profile.revision()));
        repository.flush();

        AccessLeaseRepository restored = repository(Clock.systemUTC());
        assertEquals(
                StorageRepository.RepositoryState.READY,
                restored.load(temporaryDirectory).state());
        assertEquals(profile, restored.profile(profile.id()).orElseThrow());
        assertEquals(lease.value(), restored.lease(lease.value().id()).orElseThrow());
        assertFalse(restored.history(SUBJECT).isEmpty());
    }

    @Test
    void failedCommitRollsBackInMemoryMutation() throws Exception {
        Path blockingParent = temporaryDirectory.resolve("not-a-directory");
        Files.writeString(blockingParent, "blocked");
        AccessLeaseRepository repository = repository(Clock.systemUTC());
        repository.load(blockingParent);

        var result = repository.commit(() -> repository.publishProfile(
                "support",
                ISSUER,
                Set.of(CRAFT),
                Map.of(),
                Duration.ofHours(8),
                false,
                false,
                AccessLeaseRepository.Scope.global()));

        assertFalse(result.successful());
        assertEquals(ActionResult.ReasonCode.STORAGE_ERROR, result.reason());
        assertTrue(repository.profiles().isEmpty());
    }

    private static AccessLeaseRepository repository(Clock clock) {
        return new AccessLeaseRepository(
                permission -> Set.of(CRAFT, "sef.commands.anvil", "sef.commands.run.server").contains(permission),
                clock);
    }

    private static AccessLeaseRepository.Profile publish(
            AccessLeaseRepository repository,
            AccessLeaseRepository.Scope scope
    ) {
        return repository.publishProfile(
                "support",
                ISSUER,
                Set.of(CRAFT),
                Map.of("homes", 2L),
                Duration.ofHours(8),
                false,
                false,
                scope).value();
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
