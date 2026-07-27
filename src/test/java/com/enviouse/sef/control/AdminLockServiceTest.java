package com.enviouse.sef.control;

import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.command.CommandDefinition;
import org.junit.jupiter.api.Test;

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

class AdminLockServiceTest {
    private static final UUID SUBJECT = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OWNER = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void lockDeniesOrdinaryActionUntilChallengeUnlock() {
        AdminLockRepository repository = new AdminLockRepository();
        AdminLockService service = new AdminLockService(repository);
        CommandDefinition action = definition(CommandDefinition.AccessClass.OWNER);

        assertTrue(service.lock(SUBJECT, SUBJECT, "stepping away").successful());
        assertFalse(service.authorize(SUBJECT, action, false).successful());
        assertTrue(service.authorize(SUBJECT, action, true).successful());

        ActionResult<AdminLockRepository.AccountLock> withoutChallenge =
                service.unlock(SUBJECT, SUBJECT, false);
        assertFalse(withoutChallenge.successful());
        assertEquals(ActionResult.ReasonCode.CONFIRMATION_REQUIRED, withoutChallenge.reason());

        assertTrue(service.challenge(SUBJECT).successful());
        assertTrue(service.unlock(SUBJECT, SUBJECT, false).successful());
        assertTrue(service.authorize(SUBJECT, action, false).successful());
    }

    @Test
    void configuredActionClassRequiresShortLivedSession() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        AdminLockRepository repository = new AdminLockRepository();
        AdminLockService service = new AdminLockService(repository, clock);
        repository.require(CommandDefinition.AccessClass.ADMINISTRATOR, true, OWNER);
        CommandDefinition action = definition(CommandDefinition.AccessClass.ADMINISTRATOR);

        assertFalse(service.authorize(SUBJECT, action, false).successful());
        var session = service.openSession(SUBJECT, SUBJECT, Duration.ofMinutes(10), "maintenance");
        assertTrue(session.successful());
        assertTrue(service.authorize(SUBJECT, action, false).successful());

        clock.advance(Duration.ofMinutes(11));
        assertFalse(service.authorize(SUBJECT, action, false).successful());
        assertEquals(0, service.activeSessionCount());
    }

    @Test
    void logoutAndRestartDiscardPrivilegedSession() {
        AdminLockRepository repository = new AdminLockRepository();
        repository.require(CommandDefinition.AccessClass.OWNER, true, OWNER);
        AdminLockService service = new AdminLockService(repository);
        assertTrue(service.openSession(SUBJECT, SUBJECT, Duration.ofMinutes(10), "owner work").successful());

        service.logout(SUBJECT);
        assertEquals(0, service.activeSessionCount());

        assertTrue(service.openSession(SUBJECT, SUBJECT, Duration.ofMinutes(10), "owner work").successful());
        AdminLockService restarted = new AdminLockService(repository);
        assertEquals(0, restarted.activeSessionCount());
    }

    @Test
    void breakGlassProfilesAreBoundedIncidentLinkedAndConsoleOwned() {
        AdminLockRepository repository = new AdminLockRepository();
        AdminLockService service = new AdminLockService(repository);
        var profile = repository.publishProfile(
                "recovery",
                Set.of(CommandDefinition.AccessClass.OWNER),
                600L,
                new UUID(0L, 0L));
        assertTrue(profile.successful());

        var tooLong = service.openBreakGlass("recovery", Duration.ofMinutes(11), "incident.1");
        assertFalse(tooLong.successful());
        var opened = service.openBreakGlass("recovery", Duration.ofMinutes(5), "incident.1");
        assertTrue(opened.successful());
        assertEquals("incident.1", opened.value().incidentId());
        assertEquals(1, service.breakGlassSessions().size());
        assertTrue(service.closeBreakGlass(opened.value().id()).successful());
        assertTrue(service.breakGlassSessions().isEmpty());
    }

    private static CommandDefinition definition(CommandDefinition.AccessClass accessClass) {
        return new CommandDefinition(
                "sef:test.action",
                "test action",
                Set.of(),
                "description",
                "usage",
                "test",
                "sef.control",
                Set.of("sef.test"),
                accessClass,
                Set.of(CommandDefinition.SourceType.PLAYER),
                CommandDefinition.TargetBehavior.NONE,
                "sef:test.action",
                false,
                com.enviouse.sef.audit.AuditService.AuditClass.ADMIN_ACTION,
                "",
                "",
                "chat",
                "",
                "not applicable",
                CommandDefinition.ConflictPolicy.CANONICAL_ONLY,
                false,
                true);
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
