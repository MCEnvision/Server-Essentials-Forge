package com.enviouse.sef.teleport;

import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.storage.repository.StorageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeleportRepositoryTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void homeNamesAreCaseInsensitiveAndReplacementDoesNotConsumeQuota() {
        TeleportRepository repository = loaded();
        UUID owner = UUID.randomUUID();

        assertTrue(repository.setHome(owner, "Base", location(1), 1, 1, false).successful());
        assertEquals(
                ActionResult.ReasonCode.CONFIRMATION_REQUIRED,
                repository.setHome(owner, "base", location(2), 1, 1, false).reason());
        assertTrue(repository.setHome(owner, "BASE", location(2), 1, 1, true).successful());
        assertEquals(1, repository.homes(owner).size());
        assertEquals(2, repository.home(owner, "base").orElseThrow().location().x());
        assertEquals(
                ActionResult.ReasonCode.QUOTA_EXCEEDED,
                repository.setHome(owner, "second", location(3), 1, 1, false).reason());
    }

    @Test
    void deletedHomesRemainRecoverableAndRespectCurrentQuota() {
        TeleportRepository repository = loaded();
        UUID owner = UUID.randomUUID();
        HomeRecord created = repository.setHome(owner, "home", location(1), 1, 1, false).value();
        assertTrue(repository.deleteHome(owner, "HOME").successful());
        assertTrue(repository.home(owner, "home").isEmpty());

        repository.setHome(owner, "other", location(2), 1, 1, false);
        assertEquals(
                ActionResult.ReasonCode.QUOTA_EXCEEDED,
                repository.restoreHome(created.id(), 1, 1).reason());
        repository.deleteHome(owner, "other");
        assertTrue(repository.restoreHome(created.id(), 1, 1).successful());
    }

    @Test
    void dimensionQuotaIsIndependentFromTotalQuota() {
        TeleportRepository repository = loaded();
        UUID owner = UUID.randomUUID();
        assertTrue(repository.setHome(owner, "one", location(1), 5, 1, false).successful());
        assertEquals(
                ActionResult.ReasonCode.QUOTA_EXCEEDED,
                repository.setHome(owner, "two", location(2), 5, 1, false).reason());
        SavedLocation nether = new SavedLocation("minecraft:the_nether", 1, 64, 1, 0, 0);
        assertTrue(repository.setHome(owner, "nether", nether, 5, 1, false).successful());
    }

    @Test
    void replacingHomeCannotMoveIntoAFullDestinationDimension() {
        TeleportRepository repository = loaded();
        UUID owner = UUID.randomUUID();
        SavedLocation overworld = location(1);
        SavedLocation nether = new SavedLocation("minecraft:the_nether", 1, 64, 1, 0, 0);

        assertTrue(repository.setHome(owner, "overworld", overworld, 5, 1, false).successful());
        assertTrue(repository.setHome(owner, "nether", nether, 5, 1, false).successful());

        ActionResult<HomeRecord> result = repository.setHome(
                owner,
                "overworld",
                new SavedLocation("minecraft:the_nether", 2, 64, 2, 0, 0),
                5,
                1,
                true);

        assertEquals(ActionResult.ReasonCode.QUOTA_EXCEEDED, result.reason());
        assertEquals(
                "minecraft:overworld",
                repository.home(owner, "overworld").orElseThrow().location().dimensionId());
    }

    @Test
    void playerWarpPublicationAccessTransferAndHomeConversionAreIndependent() {
        TeleportRepository repository = loaded();
        UUID owner = UUID.randomUUID();
        UUID visitor = UUID.randomUUID();
        UUID recipient = UUID.randomUUID();
        HomeRecord home = repository.setHome(owner, "source", location(1), 5, 5, false).value();
        WarpRecord warp = repository.createPlayerWarp(
                owner,
                "owner",
                "shop",
                home.location(),
                5,
                home.id()).value();

        assertEquals(WarpRecord.Status.DRAFT, warp.status());
        assertFalse(warp.canVisit(visitor, false));
        WarpRecord published = repository.publishWarp(warp.id(), WarpRecord.Access.PUBLIC).value();
        assertTrue(published.canVisit(visitor, false));
        repository.deleteHome(owner, "source");
        assertTrue(repository.warpById(warp.id()).orElseThrow().active());

        repository.blockWarp(warp.id(), visitor, true);
        assertFalse(repository.warpById(warp.id()).orElseThrow().canVisit(visitor, false));
        WarpRecord current = repository.warpById(warp.id()).orElseThrow();
        assertTrue(repository.recordVisit(warp.id(), current.revision()).successful());
        assertEquals(1, repository.warpById(warp.id()).orElseThrow().visits());

        WarpRecord beforeOffer = repository.warpById(warp.id()).orElseThrow();
        repository.offerTransfer(
                warp.id(),
                owner,
                recipient,
                Instant.now().plusSeconds(60));
        assertEquals(
                ActionResult.ReasonCode.QUOTA_EXCEEDED,
                repository.acceptTransfer(warp.id(), recipient, "recipient", 0).reason());
        assertTrue(repository.acceptTransfer(warp.id(), recipient, "recipient", 1).successful());
        assertEquals(recipient, repository.warpById(warp.id()).orElseThrow().ownerId());
        assertTrue(beforeOffer.revision() < repository.warpById(warp.id()).orElseThrow().revision());
    }

    @Test
    void repositoryRoundTripRetainsUnavailableDimensionsAndPreferences() throws Exception {
        TeleportRepository repository = loaded();
        UUID owner = UUID.randomUUID();
        UUID blocked = UUID.randomUUID();
        SavedLocation unavailable = new SavedLocation("example:missing_dimension", 1, 64, 2, 3, 4);
        repository.setHome(owner, "missing", unavailable, 2, 2, false);
        repository.setTpaEnabled(owner, false);
        repository.setBlocked(owner, blocked, true);
        repository.flush();

        TeleportRepository reloaded = new TeleportRepository();
        assertEquals(StorageRepository.RepositoryState.READY, reloaded.load(temporaryDirectory).state());
        assertEquals(
                "example:missing_dimension",
                reloaded.home(owner, "missing").orElseThrow().location().dimensionId());
        assertFalse(reloaded.preference(owner).tpaEnabled());
        assertTrue(reloaded.preference(owner).blockedPlayers().contains(blocked));
    }

    @Test
    void malformedCollectionsEnterRecoveryWithoutPartialData() throws Exception {
        Files.writeString(temporaryDirectory.resolve("teleports.json"), """
                {
                  "domain": "teleport essentials",
                  "schemaVersion": 1,
                  "data": {
                    "homes": [5],
                    "warps": [],
                    "spawns": [],
                    "preferences": [],
                    "offlineTeleports": [],
                    "transferOffers": [],
                    "reports": []
                  }
                }
                """);
        TeleportRepository repository = new TeleportRepository();

        assertEquals(StorageRepository.RepositoryState.RECOVERY, repository.load(temporaryDirectory).state());
        assertTrue(repository.allHomes(true).isEmpty());
        assertThrows(IllegalStateException.class, () ->
                repository.setHome(UUID.randomUUID(), "home", location(1), 1, 1, false));
    }

    private TeleportRepository loaded() {
        TeleportRepository repository = new TeleportRepository();
        repository.load(temporaryDirectory);
        return repository;
    }

    private static SavedLocation location(int value) {
        return new SavedLocation("minecraft:overworld", value, 64, value, 0, 0);
    }
}
