package com.enviouse.sef.gui;

import com.enviouse.sef.storage.repository.StorageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuiPreferenceRepositoryTest {
    @TempDir
    Path directory;

    @Test
    void persistsReminderAndDismissalRevisions() throws Exception {
        UUID playerId = UUID.randomUUID();
        GuiPreferenceRepository repository = new GuiPreferenceRepository();
        repository.load(directory);
        repository.recordReminder(playerId, 2, Instant.parse("2026-01-01T00:00:00Z"));
        repository.dismissReminder(playerId, 2);
        repository.updatePresentation(
                playerId,
                GuiPreferenceRepository.PresentationMode.COMMAND,
                false,
                false,
                true,
                24);
        repository.flush();

        GuiPreferenceRepository reloaded = new GuiPreferenceRepository();
        assertEquals(StorageRepository.RepositoryState.READY, reloaded.load(directory).state());
        GuiPreferenceRepository.Preference preference = reloaded.preference(playerId);
        assertEquals(2, preference.lastReminderRevision());
        assertEquals(2, preference.dismissedReminderRevision());
        assertTrue(preference.lastReminderAtEpochMillis() > 0L);
        assertEquals(GuiPreferenceRepository.PresentationMode.COMMAND, preference.presentationMode());
        assertEquals(false, preference.pauseButtonVisible());
        assertEquals(false, preference.hudEnabled());
        assertTrue(preference.reducedMotion());
        assertEquals(24, preference.preferredPageSize());
    }

    @Test
    void entersRecoveryForMalformedStorage() throws Exception {
        Files.writeString(directory.resolve("gui-preferences.json"), "not json");
        GuiPreferenceRepository repository = new GuiPreferenceRepository();
        assertEquals(StorageRepository.RepositoryState.RECOVERY, repository.load(directory).state());
    }
}
