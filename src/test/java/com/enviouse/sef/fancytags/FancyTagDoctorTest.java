package com.enviouse.sef.fancytags;

import com.enviouse.sef.storage.repository.StorageRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FancyTagDoctorTest {
    @Test
    void unopenedRepositoryIsReportedAsUnused() {
        assertEquals(
                "unused, file is created on first write",
                FancyTagCommands.repositoryStatus(StorageRepository.RepositoryState.MISSING));
    }

    @Test
    void failureStatesRetainTheirExactNames() {
        assertEquals(
                "recovery",
                FancyTagCommands.repositoryStatus(StorageRepository.RepositoryState.RECOVERY));
        assertEquals(
                "error",
                FancyTagCommands.repositoryStatus(StorageRepository.RepositoryState.ERROR));
    }
}
