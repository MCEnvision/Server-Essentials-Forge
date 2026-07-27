package com.enviouse.sef.economy;

import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.storage.repository.StorageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EconomyRepositoryTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void idempotentMutationDoesNotApplyTwiceAndMismatchedReuseFails() {
        EconomyRepository repository = repository();
        repository.load(temporaryDirectory);
        UUID player = UUID.randomUUID();
        UUID actor = UUID.randomUUID();

        EconomyProvider.MutationRequest request = request("deposit.one", actor, player, 250L);
        ActionResult<EconomyProvider.Transaction> first = repository.deposit(request);
        ActionResult<EconomyProvider.Transaction> repeated = repository.deposit(request);
        ActionResult<EconomyProvider.Transaction> mismatch =
                repository.deposit(request("deposit.one", actor, player, 251L));

        assertTrue(first.successful());
        assertTrue(repeated.successful());
        assertEquals(first.value().transactionId(), repeated.value().transactionId());
        assertFalse(mismatch.successful());
        assertEquals(ActionResult.ReasonCode.CONFLICT, mismatch.reason());
        assertEquals(1_250L, repository.account(player).orElseThrow().balance());
    }

    @Test
    void failedTransferDoesNotCreateAccountsOrMoveMoney() {
        EconomyRepository repository = repository();
        repository.load(temporaryDirectory);
        UUID source = UUID.randomUUID();
        UUID target = UUID.randomUUID();

        ActionResult<EconomyProvider.Transaction> result = repository.transfer(
                new EconomyProvider.TransferRequest(
                        "transfer.too.large",
                        source,
                        source,
                        target,
                        "test",
                        "coin",
                        2_000L,
                        Map.of(),
                        false));

        assertFalse(result.successful());
        assertTrue(repository.account(source).isEmpty());
        assertTrue(repository.account(target).isEmpty());
    }

    @Test
    void pendingCommandCostIsRefundedAfterReload() throws Exception {
        EconomyRepository repository = repository();
        repository.load(temporaryDirectory);
        UUID player = UUID.randomUUID();
        EconomyRepository.CostHold hold = repository.reserveCost(player, "sef:test", 125L);
        assertEquals(875L, repository.account(player).orElseThrow().balance());
        assertTrue(repository.dirty());
        repository.flush();

        EconomyRepository restored = repository();
        StorageRepository.LoadResult load = restored.load(temporaryDirectory);

        assertEquals(StorageRepository.RepositoryState.READY, load.state());
        assertEquals(1_000L, restored.account(player).orElseThrow().balance());
        assertFalse(restored.commitCost(hold.reservationId()).successful());
        assertTrue(restored.dirty());
    }

    @Test
    void balanceTopSnapshotIsCachedUntilABalanceChanges() {
        EconomyRepository repository = repository();
        repository.load(temporaryDirectory);
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        repository.deposit(request("deposit.first", first, first, 100L));
        repository.deposit(request("deposit.second", second, second, 200L));

        EconomyProvider.BalanceSnapshot before = repository.createSnapshot(100);
        EconomyProvider.BalanceSnapshot cached = repository.createSnapshot(100);

        assertSame(before, cached);
        assertEquals(second, before.entries().getFirst().playerId());

        repository.deposit(request("deposit.third", first, first, 200L));
        EconomyProvider.BalanceSnapshot changed = repository.createSnapshot(100);
        assertTrue(changed.revision() > before.revision());
        assertEquals(first, changed.entries().getFirst().playerId());
    }

    @Test
    void freezeBlocksPlayerMutationButAllowsAdministrativeAdjustment() {
        EconomyRepository repository = repository();
        repository.load(temporaryDirectory);
        UUID player = UUID.randomUUID();
        UUID admin = UUID.randomUUID();
        assertTrue(repository.freezeAccount(new EconomyProvider.FreezeRequest(
                "freeze.one",
                admin,
                player,
                "test",
                "coin",
                true,
                Map.of())).successful());

        assertFalse(repository.withdraw(request("withdraw.frozen", player, player, 1L)).successful());
        EconomyProvider.MutationRequest administrative = new EconomyProvider.MutationRequest(
                "deposit.admin",
                admin,
                player,
                "test",
                "coin",
                10L,
                Map.of(),
                true);
        assertTrue(repository.deposit(administrative).successful());
        assertEquals(1_010L, repository.account(player).orElseThrow().balance());
    }

    @Test
    void signedSetAdjustmentSurvivesPersistenceReload() throws Exception {
        EconomyRepository repository = repository();
        repository.load(temporaryDirectory);
        UUID player = UUID.randomUUID();
        UUID admin = UUID.randomUUID();
        assertTrue(repository.setBalance(new EconomyProvider.MutationRequest(
                "set.lower",
                admin,
                player,
                "test",
                "coin",
                500L,
                Map.of(),
                true)).successful());
        repository.flush();

        EconomyRepository loaded = repository();
        assertEquals(
                com.enviouse.sef.storage.repository.StorageRepository.RepositoryState.READY,
                loaded.load(temporaryDirectory).state());
        assertEquals(500L, loaded.account(player).orElseThrow().balance());
        assertEquals(
                -500L,
                loaded.listTransactions(player, 0, 10).getFirst().amount());
    }

    private static EconomyProvider.MutationRequest request(
            String idempotencyKey,
            UUID actor,
            UUID player,
            long amount
    ) {
        return new EconomyProvider.MutationRequest(
                idempotencyKey,
                actor,
                player,
                "test",
                "coin",
                amount,
                Map.of(),
                false);
    }

    private static EconomyRepository repository() {
        return new EconomyRepository(new EconomyRepository.Settings(
                "coin",
                2,
                1_000L,
                0L,
                1_000_000L,
                100_000L,
                1_000,
                10_000,
                1_000,
                1_000));
    }
}
