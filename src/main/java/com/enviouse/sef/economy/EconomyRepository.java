package com.enviouse.sef.economy;

import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class EconomyRepository implements StorageRepository, EconomyProvider {
    private static final int SCHEMA_VERSION = 1;
    private static final int MAXIMUM_METADATA_ENTRIES = 16;
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private final Settings settings;
    private final Map<UUID, Account> accounts = new LinkedHashMap<>();
    private final Map<UUID, AccountPreferences> preferences = new LinkedHashMap<>();
    private final List<Transaction> ledger = new ArrayList<>();
    private final Map<String, Transaction> idempotency = new LinkedHashMap<>();
    private final Map<UUID, PendingCost> pendingCosts = new LinkedHashMap<>();
    private final Map<String, Long> worth = new LinkedHashMap<>();
    private final List<ImportRecord> imports = new ArrayList<>();

    private RepositoryState state = RepositoryState.NEW;
    private Path path;
    private StorageService.Document document;
    private long revision;
    private long flushedRevision;
    private long balanceRevision;
    private long cachedBalanceRevision = -1L;
    private BalanceSnapshot cachedBalanceSnapshot = new BalanceSnapshot(0L, 0L, List.of());

    public EconomyRepository(Settings settings) {
        this.settings = Objects.requireNonNull(settings, "settings");
    }

    public Settings settings() {
        return settings;
    }

    @Override
    public String id() {
        return "sef:economy";
    }

    @Override
    public String domain() {
        return "economy";
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public synchronized Path path() {
        return path;
    }

    @Override
    public synchronized LoadResult load(Path managedRoot) {
        path = Objects.requireNonNull(managedRoot, "managedRoot")
                .resolve("economy.json")
                .toAbsolutePath()
                .normalize();
        clearState();
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(null);
        if (document == null) {
            state = Files.exists(path, java.nio.file.LinkOption.NOFOLLOW_LINKS)
                    ? RepositoryState.RECOVERY
                    : RepositoryState.MISSING;
            return new LoadResult(state, state == RepositoryState.MISSING ? "new repository" : "storage unavailable");
        }
        try {
            Snapshot snapshot = GSON.fromJson(document.data(), Snapshot.class);
            if (snapshot == null) {
                throw new IllegalStateException("Economy snapshot is missing");
            }
            loadSnapshot(snapshot);
            state = RepositoryState.READY;
            if (document.migrated()) {
                revision++;
            }
            int recovered = recoverPendingCosts();
            return new LoadResult(
                    state,
                    "loaded " + accounts.size() + " accounts and " + ledger.size()
                            + " ledger entries, recovered " + recovered + " pending costs");
        } catch (RuntimeException exception) {
            clearState();
            state = RepositoryState.RECOVERY;
            return new LoadResult(state, exception.getClass().getSimpleName());
        }
    }

    @Override
    public String currency() {
        return settings.currency();
    }

    @Override
    public int minorUnits() {
        return settings.minorUnits();
    }

    @Override
    public long minimumBalance() {
        return settings.minimumBalance();
    }

    @Override
    public long maximumBalance() {
        return settings.maximumBalance();
    }

    @Override
    public synchronized Optional<Account> account(UUID playerId) {
        return Optional.ofNullable(accounts.get(Objects.requireNonNull(playerId, "playerId")));
    }

    @Override
    public synchronized ActionResult<Account> createAccount(MutationRequest request) {
        try {
            validate(request);
            String fingerprint = fingerprint(
                    TransactionType.CREATE,
                    request.actorId(),
                    null,
                    request.accountId(),
                    request.amount(),
                    request.reason(),
                    request.currency(),
                    request.metadata());
            ActionResult<Transaction> reused = reused(request.idempotencyKey(), fingerprint);
            if (reused != null) {
                return reused.successful()
                        ? ActionResult.success(accounts.get(request.accountId()))
                        : ActionResult.failure(reused.reason(), reused.detail());
            }
            Account existing = accounts.get(request.accountId());
            if (existing != null) {
                return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "account already exists");
            }
            long openingBalance = request.amount();
            validateBalance(openingBalance);
            Account created = new Account(
                    request.accountId(),
                    openingBalance,
                    false,
                    1L,
                    System.currentTimeMillis());
            putAccount(created);
            append(transaction(
                    request.idempotencyKey(),
                    fingerprint,
                    TransactionType.CREATE,
                    request.actorId(),
                    null,
                    request.accountId(),
                    request.reason(),
                    openingBalance,
                    0L,
                    openingBalance,
                    request.metadata()));
            changed(true);
            return ActionResult.success(created);
        } catch (IllegalArgumentException | IllegalStateException | ArithmeticException exception) {
            return failure(exception);
        }
    }

    public synchronized ActionResult<Account> getOrCreate(UUID playerId, UUID actorId, String idempotencyKey) {
        Account existing = accounts.get(Objects.requireNonNull(playerId, "playerId"));
        if (existing != null) {
            return ActionResult.success(existing);
        }
        return createAccount(new MutationRequest(
                idempotencyKey,
                Objects.requireNonNull(actorId, "actorId"),
                playerId,
                "account creation",
                currency(),
                settings.defaultBalance(),
                Map.of(),
                false));
    }

    @Override
    public synchronized ActionResult<Boolean> has(UUID playerId, long amount) {
        if (amount < 0L) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "amount cannot be negative");
        }
        Account account = accounts.get(Objects.requireNonNull(playerId, "playerId"));
        if (account == null) {
            return ActionResult.success(settings.defaultBalance() >= amount);
        }
        if (account.frozen()) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "account is frozen");
        }
        return ActionResult.success(account.balance() >= amount);
    }

    @Override
    public synchronized ActionResult<Transaction> deposit(MutationRequest request) {
        return mutate(request, TransactionType.DEPOSIT, request.amount(), false);
    }

    @Override
    public synchronized ActionResult<Transaction> withdraw(MutationRequest request) {
        long delta;
        try {
            delta = Math.negateExact(request.amount());
        } catch (ArithmeticException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "amount overflows");
        }
        return mutate(request, TransactionType.WITHDRAW, delta, false);
    }

    @Override
    public synchronized ActionResult<Transaction> setBalance(MutationRequest request) {
        try {
            validate(request);
            if (!request.administrator()) {
                return ActionResult.failure(ActionResult.ReasonCode.PERMISSION_DENIED, "administrative adjustment required");
            }
            validateBalance(request.amount());
            Account current = accountOrCreate(request.accountId(), request.actorId());
            String fingerprint = fingerprint(
                    request.amount() == settings.defaultBalance() ? TransactionType.RESET : TransactionType.SET,
                    request.actorId(),
                    current.playerId(),
                    null,
                    request.amount(),
                    request.reason(),
                    request.currency(),
                    request.metadata());
            ActionResult<Transaction> reused = reused(request.idempotencyKey(), fingerprint);
            if (reused != null) {
                return reused;
            }
            TransactionType type = request.amount() == settings.defaultBalance()
                    ? TransactionType.RESET
                    : TransactionType.SET;
            Account updated = update(current, request.amount(), current.frozen());
            putAccount(updated);
            Transaction transaction = transaction(
                    request.idempotencyKey(),
                    fingerprint,
                    type,
                    request.actorId(),
                    current.playerId(),
                    null,
                    request.reason(),
                    Math.subtractExact(request.amount(), current.balance()),
                    updated.balance(),
                    0L,
                    request.metadata());
            append(transaction);
            changed(true);
            return ActionResult.success(transaction);
        } catch (IllegalArgumentException | IllegalStateException | ArithmeticException exception) {
            return failure(exception);
        }
    }

    @Override
    public synchronized ActionResult<Transaction> transfer(TransferRequest request) {
        try {
            validate(request);
            if (request.sourceId().equals(request.targetId())) {
                return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "source and target must differ");
            }
            if (request.amount() <= 0L) {
                return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "amount must be positive");
            }
            String fingerprint = fingerprint(
                    TransactionType.TRANSFER,
                    request.actorId(),
                    request.sourceId(),
                    request.targetId(),
                    request.amount(),
                    request.reason(),
                    request.currency(),
                    request.metadata());
            ActionResult<Transaction> reused = reused(request.idempotencyKey(), fingerprint);
            if (reused != null) {
                return reused;
            }
            boolean sourceMissing = !accounts.containsKey(request.sourceId());
            boolean targetMissing = !accounts.containsKey(request.targetId());
            int newAccounts = (sourceMissing ? 1 : 0) + (targetMissing ? 1 : 0);
            if (accounts.size() + newAccounts > settings.maximumAccounts()) {
                return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "account limit reached");
            }
            Account source = candidateAccount(request.sourceId());
            Account target = candidateAccount(request.targetId());
            if (!request.administrator() && (source.frozen() || target.frozen())) {
                return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "an account is frozen");
            }
            long sourceBalance = Math.subtractExact(source.balance(), request.amount());
            long targetBalance = Math.addExact(target.balance(), request.amount());
            validateBalance(sourceBalance);
            validateBalance(targetBalance);
            if (sourceMissing) {
                persistAutomaticAccount(source, request.actorId());
            }
            if (targetMissing) {
                persistAutomaticAccount(target, request.actorId());
            }
            Account updatedSource = update(source, sourceBalance, source.frozen());
            Account updatedTarget = update(target, targetBalance, target.frozen());
            putAccount(updatedSource);
            putAccount(updatedTarget);
            Transaction transaction = transaction(
                    request.idempotencyKey(),
                    fingerprint,
                    TransactionType.TRANSFER,
                    request.actorId(),
                    source.playerId(),
                    target.playerId(),
                    request.reason(),
                    request.amount(),
                    sourceBalance,
                    targetBalance,
                    request.metadata());
            append(transaction);
            changed(true);
            return ActionResult.success(transaction);
        } catch (IllegalArgumentException | IllegalStateException | ArithmeticException exception) {
            return failure(exception);
        }
    }

    @Override
    public synchronized ActionResult<Transaction> freezeAccount(FreezeRequest request) {
        try {
            validate(request);
            Account current = accountOrCreate(request.accountId(), request.actorId());
            TransactionType type = request.frozen() ? TransactionType.FREEZE : TransactionType.UNFREEZE;
            String fingerprint = fingerprint(
                    type,
                    request.actorId(),
                    current.playerId(),
                    null,
                    0L,
                    request.reason(),
                    request.currency(),
                    request.metadata());
            ActionResult<Transaction> reused = reused(request.idempotencyKey(), fingerprint);
            if (reused != null) {
                return reused;
            }
            Account updated = update(current, current.balance(), request.frozen());
            putAccount(updated);
            Transaction transaction = transaction(
                    request.idempotencyKey(),
                    fingerprint,
                    type,
                    request.actorId(),
                    current.playerId(),
                    null,
                    request.reason(),
                    0L,
                    current.balance(),
                    0L,
                    request.metadata());
            append(transaction);
            changed(false);
            return ActionResult.success(transaction);
        } catch (IllegalArgumentException | IllegalStateException | ArithmeticException exception) {
            return failure(exception);
        }
    }

    @Override
    public synchronized List<Transaction> listTransactions(UUID playerId, int offset, int limit) {
        Objects.requireNonNull(playerId, "playerId");
        if (offset < 0 || limit < 1 || limit > 100) {
            throw new IllegalArgumentException("Transaction page is outside bounds");
        }
        return ledger.reversed().stream()
                .filter(transaction -> playerId.equals(transaction.sourceId())
                        || playerId.equals(transaction.targetId()))
                .skip(offset)
                .limit(limit)
                .toList();
    }

    @Override
    public synchronized BalanceSnapshot createSnapshot(int limit) {
        if (limit < 1 || limit > settings.maximumAccounts()) {
            throw new IllegalArgumentException("Balance snapshot limit is outside bounds");
        }
        if (cachedBalanceRevision != balanceRevision) {
            List<BalanceEntry> entries = accounts.values().stream()
                    .map(account -> new BalanceEntry(account.playerId(), account.balance()))
                    .sorted(Comparator.comparingLong(BalanceEntry::balance)
                            .reversed()
                            .thenComparing(entry -> entry.playerId().toString()))
                    .toList();
            cachedBalanceSnapshot = new BalanceSnapshot(
                    balanceRevision,
                    System.currentTimeMillis(),
                    entries);
            cachedBalanceRevision = balanceRevision;
        }
        if (limit >= cachedBalanceSnapshot.entries().size()) {
            return cachedBalanceSnapshot;
        }
        return new BalanceSnapshot(
                cachedBalanceSnapshot.revision(),
                cachedBalanceSnapshot.createdAtEpochMillis(),
                cachedBalanceSnapshot.entries().subList(0, limit));
    }

    public synchronized AccountPreferences preferences(UUID playerId) {
        return preferences.getOrDefault(
                Objects.requireNonNull(playerId, "playerId"),
                new AccountPreferences(playerId, true, true, 1L));
    }

    public synchronized AccountPreferences setPaymentsEnabled(UUID playerId, boolean enabled) {
        writable();
        AccountPreferences current = preferences(playerId);
        AccountPreferences updated = new AccountPreferences(
                playerId,
                enabled,
                current.confirmLargePayments(),
                current.revision() + 1);
        preferences.put(playerId, updated);
        changed(false);
        return updated;
    }

    public synchronized AccountPreferences setConfirmLargePayments(UUID playerId, boolean enabled) {
        writable();
        AccountPreferences current = preferences(playerId);
        AccountPreferences updated = new AccountPreferences(
                playerId,
                current.paymentsEnabled(),
                enabled,
                current.revision() + 1);
        preferences.put(playerId, updated);
        changed(false);
        return updated;
    }

    public synchronized OptionalLong worth(String itemId) {
        Long value = worth.get(normalizeItemId(itemId));
        return value == null ? OptionalLong.empty() : OptionalLong.of(value);
    }

    public synchronized void setWorth(String itemId, long amount) {
        writable();
        if (amount < 0L || amount > settings.maximumTransaction()) {
            throw new IllegalArgumentException("Worth is outside configured bounds");
        }
        String normalized = normalizeItemId(itemId);
        if (amount == 0L) {
            worth.remove(normalized);
        } else {
            if (!worth.containsKey(normalized) && worth.size() >= settings.maximumWorthEntries()) {
                throw new IllegalStateException("Worth entry limit reached");
            }
            worth.put(normalized, amount);
        }
        changed(false);
    }

    public synchronized CostHold reserveCost(UUID playerId, String actionId, long amount) {
        return reserveCost(UUID.randomUUID(), playerId, actionId, amount);
    }

    public synchronized CostHold reserveCost(
            UUID reservationId,
            UUID playerId,
            String actionId,
            long amount
    ) {
        writable();
        Objects.requireNonNull(reservationId, "reservationId");
        PendingCost existing = pendingCosts.get(reservationId);
        if (existing != null) {
            if (!existing.playerId().equals(playerId)
                    || existing.amount() != amount
                    || !existing.actionId().equals(bounded(actionId, 128))) {
                throw new IllegalStateException("Cost reservation identifier was reused");
            }
            return new CostHold(
                    existing.reservationId(),
                    existing.playerId(),
                    existing.amount(),
                    existing.actionId(),
                    existing.reserveTransactionId());
        }
        if (amount <= 0L) {
            throw new IllegalArgumentException("Cost amount must be positive");
        }
        if (pendingCosts.size() >= settings.maximumPendingCosts()) {
            throw new IllegalStateException("Pending cost reservation limit reached");
        }
        String key = "cost.reserve." + reservationId;
        ActionResult<Transaction> withdrawn = mutate(new MutationRequest(
                key,
                playerId,
                playerId,
                "command cost for " + bounded(actionId, 128),
                currency(),
                amount,
                Map.of("action", bounded(actionId, 128), "reservation", reservationId.toString()),
                false), TransactionType.COST_RESERVE, Math.negateExact(amount), false);
        if (!withdrawn.successful()) {
            throw new IllegalStateException(withdrawn.detail());
        }
        pendingCosts.put(reservationId, new PendingCost(
                reservationId,
                playerId,
                amount,
                bounded(actionId, 128),
                withdrawn.value().transactionId(),
                System.currentTimeMillis()));
        changed(false);
        return new CostHold(
                reservationId,
                playerId,
                amount,
                actionId,
                withdrawn.value().transactionId());
    }

    public synchronized ActionResult<Void> commitCost(UUID reservationId) {
        writable();
        PendingCost removed = pendingCosts.remove(Objects.requireNonNull(reservationId, "reservationId"));
        if (removed == null) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "cost reservation is closed");
        }
        changed(false);
        return ActionResult.success(null);
    }

    public synchronized void restorePendingCost(CostHold hold) {
        writable();
        Objects.requireNonNull(hold, "hold");
        if (pendingCosts.containsKey(hold.reservationId())) {
            return;
        }
        if (pendingCosts.size() >= settings.maximumPendingCosts()) {
            throw new IllegalStateException("Pending cost reservation limit reached");
        }
        pendingCosts.put(
                hold.reservationId(),
                new PendingCost(
                        hold.reservationId(),
                        hold.playerId(),
                        hold.amount(),
                        bounded(hold.actionId(), 128),
                        hold.transactionId(),
                        System.currentTimeMillis()));
        changed(false);
    }

    public synchronized ActionResult<Void> refundCost(UUID reservationId) {
        writable();
        PendingCost pending = pendingCosts.get(Objects.requireNonNull(reservationId, "reservationId"));
        if (pending == null) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "cost reservation is closed");
        }
        ActionResult<Transaction> refunded = mutate(new MutationRequest(
                "cost.refund." + pending.reservationId(),
                pending.playerId(),
                pending.playerId(),
                "command cost refund for " + pending.actionId(),
                currency(),
                pending.amount(),
                Map.of(
                        "action", pending.actionId(),
                        "reservation", pending.reservationId().toString(),
                        "reserve_transaction", pending.reserveTransactionId().toString()),
                true), TransactionType.COST_REFUND, pending.amount(), true);
        if (!refunded.successful()) {
            return ActionResult.failure(refunded.reason(), refunded.detail());
        }
        pendingCosts.remove(reservationId);
        changed(false);
        return ActionResult.success(null);
    }

    public synchronized ImportRecord importAccounts(
            String sourceId,
            List<EconomyProviderRegistry.ImportAccount> importedAccounts,
            UUID actorId,
            String idempotencyKey
    ) {
        writable();
        String source = bounded(sourceId, 128).toLowerCase(Locale.ROOT);
        if (!imports.isEmpty() || !accounts.isEmpty()) {
            throw new IllegalStateException("Economy import requires an empty native account store");
        }
        List<EconomyProviderRegistry.ImportAccount> sourceAccounts =
                List.copyOf(Objects.requireNonNull(importedAccounts, "importedAccounts"));
        if (sourceAccounts.size() > settings.maximumAccounts()) {
            throw new IllegalArgumentException("Import account count exceeds the configured limit");
        }
        Map<UUID, Long> distinct = new LinkedHashMap<>();
        long total = 0L;
        for (EconomyProviderRegistry.ImportAccount account : sourceAccounts) {
            Objects.requireNonNull(account, "import account");
            if (distinct.putIfAbsent(Objects.requireNonNull(account.playerId(), "playerId"), account.balance()) != null) {
                throw new IllegalArgumentException("Import contains duplicate accounts");
            }
            validateBalance(account.balance());
            total = Math.addExact(total, account.balance());
        }
        long now = System.currentTimeMillis();
        for (Map.Entry<UUID, Long> entry : distinct.entrySet()) {
            Account account = new Account(entry.getKey(), entry.getValue(), false, 1L, now);
            putAccount(account);
            String key = bounded(idempotencyKey, 128) + "." + entry.getKey();
            String fingerprint = fingerprint(
                    TransactionType.IMPORT,
                    actorId,
                    null,
                    entry.getKey(),
                    entry.getValue(),
                    "import from " + source,
                    currency(),
                    Map.of("source", source));
            append(transaction(
                    key,
                    fingerprint,
                    TransactionType.IMPORT,
                    actorId,
                    null,
                    entry.getKey(),
                    "import from " + source,
                    entry.getValue(),
                    0L,
                    entry.getValue(),
                    Map.of("source", source)));
        }
        ImportRecord record = new ImportRecord(
                source,
                now,
                distinct.size(),
                total,
                fingerprintText(source + ":" + distinct.size() + ":" + total + ":" + now));
        imports.add(record);
        changed(true);
        return record;
    }

    public synchronized List<ImportRecord> imports() {
        return List.copyOf(imports);
    }

    public synchronized void prepareImportBackup() {
        writable();
        if (path == null) {
            throw new IllegalStateException("Economy repository path is unavailable");
        }
        if (!Files.exists(path)) {
            changed(false);
        }
    }

    public synchronized void rollbackImport(ImportRecord record) {
        writable();
        Objects.requireNonNull(record, "record");
        if (imports.isEmpty() || !imports.getLast().equals(record)) {
            throw new IllegalStateException("Economy import rollback does not match the latest import");
        }
        imports.removeLast();
        accounts.clear();
        ledger.removeIf(transaction ->
                transaction.type() == TransactionType.IMPORT
                        && record.sourceId().equals(transaction.metadata().get("source")));
        changed(true);
    }

    @Override
    public void flush() throws IOException {
        final Snapshot snapshot;
        final Path destination;
        final StorageService.Document previous;
        final long snapshotRevision;
        synchronized (this) {
            if (path == null || !dirty()) {
                return;
            }
            writable();
            snapshot = new Snapshot(
                    List.copyOf(accounts.values()),
                    List.copyOf(preferences.values()),
                    List.copyOf(ledger),
                    List.copyOf(pendingCosts.values()),
                    Map.copyOf(worth),
                    List.copyOf(imports));
            destination = path;
            previous = document;
            snapshotRevision = revision;
        }
        StorageService.write(
                destination,
                domain(),
                SCHEMA_VERSION,
                GSON.toJsonTree(snapshot),
                previous,
                Set.of("/accounts", "/preferences", "/ledger", "/pendingCosts", "/worth", "/imports"));
        synchronized (this) {
            document = StorageService.read(destination, domain(), SCHEMA_VERSION).orElse(previous);
            flushedRevision = Math.max(flushedRevision, snapshotRevision);
            state = RepositoryState.READY;
        }
    }

    @Override
    public synchronized boolean dirty() {
        return revision > flushedRevision;
    }

    @Override
    public synchronized RepositoryState state() {
        return state;
    }

    private ActionResult<Transaction> mutate(
            MutationRequest request,
            TransactionType type,
            long delta,
            boolean bypassFrozen
    ) {
        try {
            validate(request);
            if (request.amount() <= 0L) {
                return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "amount must be positive");
            }
            String fingerprint = fingerprint(
                    type,
                    request.actorId(),
                    request.accountId(),
                    null,
                    request.amount(),
                    request.reason(),
                    request.currency(),
                    request.metadata());
            ActionResult<Transaction> reused = reused(request.idempotencyKey(), fingerprint);
            if (reused != null) {
                return reused;
            }
            boolean missing = !accounts.containsKey(request.accountId());
            Account current = candidateAccount(request.accountId());
            if (current.frozen() && !request.administrator() && !bypassFrozen) {
                return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "account is frozen");
            }
            long nextBalance = Math.addExact(current.balance(), delta);
            validateBalance(nextBalance);
            if (missing) {
                persistAutomaticAccount(current, request.actorId());
            }
            Account updated = update(current, nextBalance, current.frozen());
            putAccount(updated);
            Transaction transaction = transaction(
                    request.idempotencyKey(),
                    fingerprint,
                    type,
                    request.actorId(),
                    current.playerId(),
                    null,
                    request.reason(),
                    request.amount(),
                    nextBalance,
                    0L,
                    request.metadata());
            append(transaction);
            changed(true);
            return ActionResult.success(transaction);
        } catch (IllegalArgumentException | IllegalStateException | ArithmeticException exception) {
            return failure(exception);
        }
    }

    private Account accountOrCreate(UUID accountId, UUID actorId) {
        Account existing = accounts.get(accountId);
        if (existing != null) {
            return existing;
        }
        Account created = candidateAccount(accountId);
        persistAutomaticAccount(created, actorId);
        return created;
    }

    private Account candidateAccount(UUID accountId) {
        Account existing = accounts.get(Objects.requireNonNull(accountId, "accountId"));
        if (existing != null) {
            return existing;
        }
        if (accounts.size() >= settings.maximumAccounts()) {
            throw new IllegalStateException("Account limit reached");
        }
        return new Account(
                accountId,
                settings.defaultBalance(),
                false,
                1L,
                System.currentTimeMillis());
    }

    private void persistAutomaticAccount(Account created, UUID actorId) {
        putAccount(created);
        String key = "automatic.create." + created.playerId();
        String fingerprint = fingerprint(
                TransactionType.CREATE,
                actorId,
                null,
                created.playerId(),
                settings.defaultBalance(),
                "automatic account creation",
                currency(),
                Map.of());
        if (!idempotency.containsKey(key)) {
            append(transaction(
                    key,
                    fingerprint,
                    TransactionType.CREATE,
                    actorId,
                    null,
                    created.playerId(),
                    "automatic account creation",
                    settings.defaultBalance(),
                    0L,
                    settings.defaultBalance(),
                    Map.of()));
        }
    }

    private void append(Transaction transaction) {
        if (ledger.size() >= settings.maximumLedgerEntries()) {
            Transaction removed = ledger.removeFirst();
            idempotency.remove(removed.idempotencyKey(), removed);
        }
        ledger.add(transaction);
        idempotency.put(transaction.idempotencyKey(), transaction);
    }

    private ActionResult<Transaction> reused(String idempotencyKey, String fingerprint) {
        Transaction existing = idempotency.get(normalizeKey(idempotencyKey));
        if (existing == null) {
            return null;
        }
        return existing.requestFingerprint().equals(fingerprint)
                ? ActionResult.success(existing)
                : ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "idempotency key was reused for another request");
    }

    private Transaction transaction(
            String idempotencyKey,
            String fingerprint,
            TransactionType type,
            UUID actorId,
            UUID sourceId,
            UUID targetId,
            String reason,
            long amount,
            long sourceBalance,
            long targetBalance,
            Map<String, String> metadata
    ) {
        return new Transaction(
                UUID.randomUUID(),
                normalizeKey(idempotencyKey),
                fingerprint,
                type,
                Objects.requireNonNull(actorId, "actorId"),
                sourceId,
                targetId,
                bounded(reason, 256),
                currency(),
                amount,
                sourceBalance,
                targetBalance,
                normalizeMetadata(metadata),
                System.currentTimeMillis());
    }

    private void validate(MutationRequest request) {
        writable();
        Objects.requireNonNull(request, "request");
        normalizeKey(request.idempotencyKey());
        Objects.requireNonNull(request.actorId(), "actorId");
        Objects.requireNonNull(request.accountId(), "accountId");
        validateCurrency(request.currency());
        bounded(request.reason(), 256);
        normalizeMetadata(request.metadata());
        if (request.amount() < 0L || request.amount() > settings.maximumTransaction()) {
            throw new IllegalArgumentException("Transaction amount is outside configured bounds");
        }
    }

    private void validate(TransferRequest request) {
        writable();
        Objects.requireNonNull(request, "request");
        normalizeKey(request.idempotencyKey());
        Objects.requireNonNull(request.actorId(), "actorId");
        Objects.requireNonNull(request.sourceId(), "sourceId");
        Objects.requireNonNull(request.targetId(), "targetId");
        validateCurrency(request.currency());
        bounded(request.reason(), 256);
        normalizeMetadata(request.metadata());
        if (request.amount() <= 0L || request.amount() > settings.maximumTransaction()) {
            throw new IllegalArgumentException("Transfer amount is outside configured bounds");
        }
    }

    private void validate(FreezeRequest request) {
        writable();
        Objects.requireNonNull(request, "request");
        normalizeKey(request.idempotencyKey());
        Objects.requireNonNull(request.actorId(), "actorId");
        Objects.requireNonNull(request.accountId(), "accountId");
        validateCurrency(request.currency());
        bounded(request.reason(), 256);
        normalizeMetadata(request.metadata());
    }

    private void validateCurrency(String value) {
        if (!currency().equalsIgnoreCase(Objects.requireNonNull(value, "currency").strip())) {
            throw new IllegalArgumentException("Currency does not match the active provider");
        }
    }

    private void validateBalance(long value) {
        if (value < settings.minimumBalance() || value > settings.maximumBalance()) {
            throw new IllegalArgumentException("Balance is outside configured bounds");
        }
    }

    private void putAccount(Account account) {
        if (!accounts.containsKey(account.playerId()) && accounts.size() >= settings.maximumAccounts()) {
            throw new IllegalStateException("Account limit reached");
        }
        accounts.put(account.playerId(), account);
    }

    private Account update(Account account, long balance, boolean frozen) {
        return new Account(
                account.playerId(),
                balance,
                frozen,
                Math.addExact(account.revision(), 1L),
                System.currentTimeMillis());
    }

    private void changed(boolean balancesChanged) {
        revision = Math.addExact(revision, 1L);
        if (balancesChanged) {
            balanceRevision = Math.addExact(balanceRevision, 1L);
        }
    }

    private int recoverPendingCosts() {
        if (pendingCosts.isEmpty()) {
            return 0;
        }
        List<UUID> reservations = List.copyOf(pendingCosts.keySet());
        int recovered = 0;
        for (UUID reservation : reservations) {
            ActionResult<Void> result = refundCost(reservation);
            if (!result.successful()) {
                throw new IllegalStateException("Pending cost recovery failed");
            }
            recovered++;
        }
        return recovered;
    }

    private void loadSnapshot(Snapshot snapshot) {
        if (snapshot.accounts().size() > settings.maximumAccounts()
                || snapshot.preferences().size() > settings.maximumAccounts()
                || snapshot.ledger().size() > settings.maximumLedgerEntries()
                || snapshot.pendingCosts().size() > settings.maximumPendingCosts()
                || snapshot.worth().size() > settings.maximumWorthEntries()
                || snapshot.imports().size() > 16) {
            throw new IllegalStateException("Economy snapshot collections exceed configured bounds");
        }
        for (Account account : snapshot.accounts()) {
            validateLoaded(account);
            if (accounts.putIfAbsent(account.playerId(), account) != null) {
                throw new IllegalStateException("Duplicate economy account");
            }
        }
        for (AccountPreferences preference : snapshot.preferences()) {
            validateLoaded(preference);
            if (preferences.putIfAbsent(preference.playerId(), preference) != null) {
                throw new IllegalStateException("Duplicate economy preferences");
            }
        }
        for (Transaction transaction : snapshot.ledger()) {
            validateLoaded(transaction);
            if (idempotency.putIfAbsent(transaction.idempotencyKey(), transaction) != null) {
                throw new IllegalStateException("Duplicate economy idempotency key");
            }
            ledger.add(transaction);
        }
        for (PendingCost pending : snapshot.pendingCosts()) {
            validateLoaded(pending);
            if (pendingCosts.putIfAbsent(pending.reservationId(), pending) != null) {
                throw new IllegalStateException("Duplicate pending economy cost");
            }
        }
        for (Map.Entry<String, Long> entry : snapshot.worth().entrySet()) {
            String id = normalizeItemId(entry.getKey());
            long value = Objects.requireNonNull(entry.getValue(), "worth");
            if (value <= 0L || value > settings.maximumTransaction() || worth.putIfAbsent(id, value) != null) {
                throw new IllegalStateException("Invalid economy worth entry");
            }
        }
        for (ImportRecord record : snapshot.imports()) {
            validateLoaded(record);
            imports.add(record);
        }
        balanceRevision++;
    }

    private void validateLoaded(Account account) {
        Objects.requireNonNull(account, "account");
        Objects.requireNonNull(account.playerId(), "playerId");
        validateBalance(account.balance());
        if (account.revision() < 1L || account.updatedAtEpochMillis() < 0L) {
            throw new IllegalStateException("Economy account is invalid");
        }
    }

    private static void validateLoaded(AccountPreferences preference) {
        if (preference == null || preference.playerId() == null || preference.revision() < 1L) {
            throw new IllegalStateException("Economy preferences are invalid");
        }
    }

    private void validateLoaded(Transaction transaction) {
        Objects.requireNonNull(transaction, "transaction");
        Objects.requireNonNull(transaction.transactionId(), "transactionId");
        normalizeKey(transaction.idempotencyKey());
        if (!fingerprintText(transaction.requestFingerprint()).equals(transaction.requestFingerprint())
                || transaction.type() == null
                || transaction.actorId() == null
                || !currency().equals(transaction.currency())
                || !validLoadedTransactionAmount(transaction)
                || transaction.createdAtEpochMillis() < 0L) {
            throw new IllegalStateException("Economy transaction is invalid");
        }
        bounded(transaction.reason(), 256);
        normalizeMetadata(transaction.metadata());
    }

    private static void validateLoaded(PendingCost pending) {
        if (pending == null
                || pending.reservationId() == null
                || pending.playerId() == null
                || pending.reserveTransactionId() == null
                || pending.amount() <= 0L
                || pending.createdAtEpochMillis() < 0L) {
            throw new IllegalStateException("Pending economy cost is invalid");
        }
        bounded(pending.actionId(), 128);
    }

    private static void validateLoaded(ImportRecord record) {
        if (record == null
                || record.importedAtEpochMillis() < 0L
                || record.accounts() < 0
                || record.reportHash() == null
                || !fingerprintText(record.reportHash()).equals(record.reportHash())) {
            throw new IllegalStateException("Economy import record is invalid");
        }
        bounded(record.sourceId(), 128);
    }

    private void clearState() {
        accounts.clear();
        preferences.clear();
        ledger.clear();
        idempotency.clear();
        pendingCosts.clear();
        worth.clear();
        imports.clear();
        revision = 0L;
        flushedRevision = 0L;
        balanceRevision = 0L;
        cachedBalanceRevision = -1L;
        cachedBalanceSnapshot = new BalanceSnapshot(0L, 0L, List.of());
    }

    private void writable() {
        if (state == RepositoryState.RECOVERY
                || state == RepositoryState.UNSUPPORTED
                || state == RepositoryState.ERROR
                || state == RepositoryState.CLOSED) {
            throw new IllegalStateException("Economy repository is not writable in " + state + " state");
        }
    }

    private static <T> ActionResult<T> failure(RuntimeException exception) {
        ActionResult.ReasonCode reason = exception instanceof IllegalStateException
                ? ActionResult.ReasonCode.COST_UNAVAILABLE
                : ActionResult.ReasonCode.INVALID_INPUT;
        String detail = exception.getMessage();
        return ActionResult.failure(reason, detail == null || detail.isBlank()
                ? exception.getClass().getSimpleName()
                : bounded(detail, 256));
    }

    private boolean validLoadedTransactionAmount(Transaction transaction) {
        long amount = transaction.amount();
        if (transaction.type() == TransactionType.SET || transaction.type() == TransactionType.RESET) {
            return amount != Long.MIN_VALUE && Math.abs(amount) <= settings.maximumTransaction();
        }
        return amount >= 0L && amount <= settings.maximumTransaction();
    }

    private static String normalizeKey(String value) {
        String normalized = bounded(value, 128);
        if (!normalized.matches("[a-zA-Z0-9_.:-]{1,128}")) {
            throw new IllegalArgumentException("Idempotency key is invalid");
        }
        return normalized;
    }

    private static String normalizeItemId(String value) {
        String normalized = bounded(value, 128).toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) {
            throw new IllegalArgumentException("Item id is invalid");
        }
        return normalized;
    }

    private static Map<String, String> normalizeMetadata(Map<String, String> input) {
        Map<String, String> source = input == null ? Map.of() : input;
        if (source.size() > MAXIMUM_METADATA_ENTRIES) {
            throw new IllegalArgumentException("Transaction metadata has too many entries");
        }
        Map<String, String> result = new LinkedHashMap<>();
        source.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String key = bounded(entry.getKey(), 64).toLowerCase(Locale.ROOT);
                    if (!key.matches("[a-z0-9_.:-]{1,64}")) {
                        throw new IllegalArgumentException("Transaction metadata key is invalid");
                    }
                    result.put(key, bounded(entry.getValue(), 256));
                });
        return Map.copyOf(result);
    }

    private static String bounded(String value, int maximum) {
        String bounded = Objects.requireNonNull(value, "value").strip();
        if (bounded.isBlank()
                || bounded.length() > maximum
                || bounded.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Economy text is invalid");
        }
        return bounded;
    }

    private static String fingerprint(
            TransactionType type,
            UUID actorId,
            UUID sourceId,
            UUID targetId,
            long amount,
            String reason,
            String currency,
            Map<String, String> metadata
    ) {
        StringBuilder value = new StringBuilder()
                .append(type).append('|')
                .append(actorId).append('|')
                .append(sourceId).append('|')
                .append(targetId).append('|')
                .append(amount).append('|')
                .append(bounded(reason, 256)).append('|')
                .append(bounded(currency, 32).toLowerCase(Locale.ROOT));
        normalizeMetadata(metadata).forEach((key, entry) ->
                value.append('|').append(key).append('=').append(entry));
        return fingerprintText(value.toString());
    }

    private static String fingerprintText(String value) {
        if (value != null && value.matches("[0-9a-f]{64}")) {
            return value;
        }
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(Objects.requireNonNull(value, "value").getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public record Settings(
            String currency,
            int minorUnits,
            long defaultBalance,
            long minimumBalance,
            long maximumBalance,
            long maximumTransaction,
            int maximumAccounts,
            int maximumLedgerEntries,
            int maximumPendingCosts,
            int maximumWorthEntries
    ) {
        public Settings {
            currency = Objects.requireNonNull(currency, "currency").strip().toLowerCase(Locale.ROOT);
            if (!currency.matches("[a-z0-9_.-]{1,32}")
                    || minorUnits < 0
                    || minorUnits > 8
                    || minimumBalance > defaultBalance
                    || defaultBalance > maximumBalance
                    || maximumTransaction < 1L
                    || maximumTransaction > maximumBalance
                    || maximumAccounts < 1
                    || maximumAccounts > 1_000_000
                    || maximumLedgerEntries < 100
                    || maximumLedgerEntries > 1_000_000
                    || maximumPendingCosts < 1
                    || maximumPendingCosts > 100_000
                    || maximumWorthEntries < 1
                    || maximumWorthEntries > 100_000) {
                throw new IllegalArgumentException("Economy settings are outside hard bounds");
            }
        }
    }

    public record AccountPreferences(
            UUID playerId,
            boolean paymentsEnabled,
            boolean confirmLargePayments,
            long revision
    ) {
    }

    public record CostHold(
            UUID reservationId,
            UUID playerId,
            long amount,
            String actionId,
            UUID transactionId
    ) {
    }

    public record PendingCost(
            UUID reservationId,
            UUID playerId,
            long amount,
            String actionId,
            UUID reserveTransactionId,
            long createdAtEpochMillis
    ) {
    }

    public record ImportRecord(
            String sourceId,
            long importedAtEpochMillis,
            int accounts,
            long totalMinorUnits,
            String reportHash
    ) {
    }

    public record Snapshot(
            List<Account> accounts,
            List<AccountPreferences> preferences,
            List<Transaction> ledger,
            List<PendingCost> pendingCosts,
            Map<String, Long> worth,
            List<ImportRecord> imports
    ) {
        public Snapshot {
            accounts = List.copyOf(accounts == null ? List.of() : accounts);
            preferences = List.copyOf(preferences == null ? List.of() : preferences);
            ledger = List.copyOf(ledger == null ? List.of() : ledger);
            pendingCosts = List.copyOf(pendingCosts == null ? List.of() : pendingCosts);
            worth = Map.copyOf(worth == null ? Map.of() : worth);
            imports = List.copyOf(imports == null ? List.of() : imports);
        }
    }

    public static final class OptionalLong {
        private static final OptionalLong EMPTY = new OptionalLong(false, 0L);
        private final boolean present;
        private final long value;

        private OptionalLong(boolean present, long value) {
            this.present = present;
            this.value = value;
        }

        public static OptionalLong empty() {
            return EMPTY;
        }

        public static OptionalLong of(long value) {
            return new OptionalLong(true, value);
        }

        public boolean isPresent() {
            return present;
        }

        public long orElse(long fallback) {
            return present ? value : fallback;
        }

        public long orElseThrow() {
            if (!present) {
                throw new IllegalStateException("No worth is defined");
            }
            return value;
        }
    }
}
