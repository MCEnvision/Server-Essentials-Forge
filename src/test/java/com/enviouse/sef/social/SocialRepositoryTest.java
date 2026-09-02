package com.enviouse.sef.social;

import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.storage.repository.StorageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SocialRepositoryTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void socialStateRoundTripsWithoutLosingTypedRecords() throws Exception {
        SocialRepository repository = loaded();
        UUID player = UUID.randomUUID();
        UUID ignored = UUID.randomUUID();
        UUID sender = UUID.randomUUID();
        repository.updatePreferences(repository.preferences(player).withMessagesEnabled(false));
        repository.setIgnored(player, ignored, true);
        repository.setConnectionTemplate(player, true, "&a{player} arrived");
        repository.setTextPage("rules", "&6Be kind.");
        repository.putReminder(new SocialRepository.ReminderDefinition(
                "welcome",
                true,
                "&eWelcome {player}.",
                SocialRepository.ReminderAudience.FIRST_JOIN,
                0,
                1,
                true,
                1,
                sender,
                Instant.now()));
        ActionResult<SocialRepository.MailRecord> mail = repository.sendMail(
                sender,
                player,
                "private body",
                Instant.now().plus(30, ChronoUnit.DAYS),
                10);
        assertTrue(mail.successful());
        repository.flush();

        SocialRepository reloaded = new SocialRepository();
        assertEquals(StorageRepository.RepositoryState.READY, reloaded.load(temporaryDirectory).state());
        assertFalse(reloaded.preferences(player).messagesEnabled());
        assertTrue(reloaded.ignores(player, ignored));
        assertEquals("&a{player} arrived", reloaded.connectionTemplates(player).joinTemplate());
        assertEquals("&6Be kind.", reloaded.textPage("rules"));
        assertEquals("private body", reloaded.mail(player, false).getFirst().body());
        assertTrue(reloaded.reminder("WELCOME").isPresent());
    }

    @Test
    void collectionsRejectUnsafeAndUnboundedInput() {
        SocialRepository repository = loaded();
        UUID player = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> repository.setTextPage("../escape", "no"));
        assertThrows(IllegalArgumentException.class, () -> repository.setTextPage("rules", "x".repeat(16_385)));
        assertThrows(IllegalArgumentException.class, () -> repository.setConnectionTemplate(
                player,
                true,
                "x".repeat(513)));
        assertThrows(IllegalArgumentException.class, () -> repository.updatePreferences(
                repository.preferences(player).withSpy(
                        true,
                        SocialRepository.SpyAudience.SELECTED,
                        SocialRepository.SpyMatch.EITHER,
                        true,
                        Set.of(),
                        Set.of("../route"))));
        assertThrows(IllegalArgumentException.class, () -> repository.sendMail(
                UUID.randomUUID(),
                player,
                "x".repeat(16_385),
                Instant.now().plus(1, ChronoUnit.DAYS),
                10));
    }

    @Test
    void mailboxQuotaOwnershipArchiveAndClearAreIsolated() {
        SocialRepository repository = loaded();
        UUID sender = UUID.randomUUID();
        UUID recipient = UUID.randomUUID();
        UUID otherRecipient = UUID.randomUUID();
        Instant expiry = Instant.now().plus(30, ChronoUnit.DAYS);

        SocialRepository.MailRecord owned = repository.sendMail(
                sender, recipient, "owned", expiry, 1).value();
        SocialRepository.MailRecord other = repository.sendMail(
                sender, otherRecipient, "other", expiry, 1).value();

        assertFalse(repository.sendMail(sender, recipient, "over quota", expiry, 1).successful());
        assertFalse(repository.updateMail(
                otherRecipient, owned.id(), SocialRepository.MailMutation.DELETE).successful());
        assertTrue(repository.updateMail(
                recipient, owned.id(), SocialRepository.MailMutation.ARCHIVE).successful());
        assertTrue(repository.mail(recipient, false).isEmpty());
        assertEquals(1, repository.mail(recipient, true).size());
        assertEquals(1, repository.clearMail(recipient));
        assertEquals("other", repository.mail(otherRecipient, false).getFirst().body());
        assertEquals(other.id(), repository.mail(otherRecipient, false).getFirst().id());
    }

    @Test
    void reminderDismissalTracksAcknowledgementRevision() {
        SocialRepository repository = loaded();
        UUID player = UUID.randomUUID();
        SocialRepository.ReminderDefinition reminder = new SocialRepository.ReminderDefinition(
                "onboarding",
                true,
                "Read the rules",
                SocialRepository.ReminderAudience.ALL,
                60,
                3,
                true,
                4,
                UUID.randomUUID(),
                Instant.now());
        repository.putReminder(reminder);

        SocialRepository.ReminderState state = repository.reminderState(player, reminder.id())
                .delivered(Instant.now(), reminder.acknowledgementRevision())
                .withDismissed(true);
        repository.updateReminderState(state);

        assertTrue(repository.reminderState(player, reminder.id()).dismissed());
        assertEquals(4, repository.reminderState(player, reminder.id()).acknowledgedRevision());
        assertEquals(1, repository.reminderState(player, reminder.id()).deliveryCount());
    }

    @Test
    void malformedSocialDocumentEntersRecoveryWithoutPartialState() throws Exception {
        Files.writeString(temporaryDirectory.resolve("social.json"), """
                {
                  "domain": "social identity mail and reminders",
                  "schemaVersion": 1,
                  "data": {
                    "preferences": [5],
                    "mail": [],
                    "connectionTemplates": [],
                    "reminders": [],
                    "reminderStates": [],
                    "textPages": {}
                  }
                }
                """);
        SocialRepository repository = new SocialRepository();

        assertEquals(StorageRepository.RepositoryState.RECOVERY, repository.load(temporaryDirectory).state());
        assertTrue(repository.snapshot().preferences().isEmpty());
        assertThrows(IllegalStateException.class, () ->
                repository.setIgnored(UUID.randomUUID(), UUID.randomUUID(), true));
    }

    private SocialRepository loaded() {
        SocialRepository repository = new SocialRepository();
        repository.load(temporaryDirectory);
        return repository;
    }
}
