package com.enviouse.sef.economy;

import com.enviouse.sef.kernel.ActionResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface EconomyProvider {
    String id();

    String currency();

    int minorUnits();

    long minimumBalance();

    long maximumBalance();

    Optional<Account> account(UUID playerId);

    ActionResult<Account> createAccount(MutationRequest request);

    ActionResult<Boolean> has(UUID playerId, long amount);

    ActionResult<Transaction> deposit(MutationRequest request);

    ActionResult<Transaction> withdraw(MutationRequest request);

    ActionResult<Transaction> transfer(TransferRequest request);

    ActionResult<Transaction> setBalance(MutationRequest request);

    ActionResult<Transaction> freezeAccount(FreezeRequest request);

    List<Transaction> listTransactions(UUID playerId, int offset, int limit);

    BalanceSnapshot createSnapshot(int limit);

    interface CostReservationProvider {
        ActionResult<EconomyCostReservation> reserveCost(CostRequest request);
    }

    interface EconomyCostReservation {
        UUID reservationId();

        UUID transactionId();

        long amount();

        ActionResult<Void> commit();

        ActionResult<Void> refund();
    }

    record CostRequest(
            String idempotencyKey,
            UUID actorId,
            String actionId,
            String currency,
            long amount,
            Map<String, String> metadata
    ) {
        public CostRequest {
            metadata = Map.copyOf(metadata == null ? Map.of() : metadata);
        }
    }

    record Account(
            UUID playerId,
            long balance,
            boolean frozen,
            long revision,
            long updatedAtEpochMillis
    ) {
    }

    record MutationRequest(
            String idempotencyKey,
            UUID actorId,
            UUID accountId,
            String reason,
            String currency,
            long amount,
            Map<String, String> metadata,
            boolean administrator
    ) {
        public MutationRequest {
            metadata = Map.copyOf(metadata == null ? Map.of() : metadata);
        }
    }

    record TransferRequest(
            String idempotencyKey,
            UUID actorId,
            UUID sourceId,
            UUID targetId,
            String reason,
            String currency,
            long amount,
            Map<String, String> metadata,
            boolean administrator
    ) {
        public TransferRequest {
            metadata = Map.copyOf(metadata == null ? Map.of() : metadata);
        }
    }

    record FreezeRequest(
            String idempotencyKey,
            UUID actorId,
            UUID accountId,
            String reason,
            String currency,
            boolean frozen,
            Map<String, String> metadata
    ) {
        public FreezeRequest {
            metadata = Map.copyOf(metadata == null ? Map.of() : metadata);
        }
    }

    record Transaction(
            UUID transactionId,
            String idempotencyKey,
            String requestFingerprint,
            TransactionType type,
            UUID actorId,
            UUID sourceId,
            UUID targetId,
            String reason,
            String currency,
            long amount,
            long sourceBalance,
            long targetBalance,
            Map<String, String> metadata,
            long createdAtEpochMillis
    ) {
        public Transaction {
            metadata = Map.copyOf(metadata == null ? Map.of() : metadata);
        }
    }

    record BalanceEntry(UUID playerId, long balance) {
    }

    record BalanceSnapshot(long revision, long createdAtEpochMillis, List<BalanceEntry> entries) {
        public BalanceSnapshot {
            entries = List.copyOf(entries == null ? List.of() : entries);
        }
    }

    enum TransactionType {
        CREATE,
        DEPOSIT,
        WITHDRAW,
        TRANSFER,
        SET,
        RESET,
        FREEZE,
        UNFREEZE,
        COST_RESERVE,
        COST_REFUND,
        IMPORT
    }
}
