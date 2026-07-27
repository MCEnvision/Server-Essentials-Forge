package com.enviouse.sef.escrow;

import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.recovery.ItemStackSnapshotCodec;
import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
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

public final class EscrowRepository implements StorageRepository {
    public static final int SCHEMA_VERSION = 1;
    public static final int HARD_MAXIMUM_RECORDS = 25_000;
    public static final int HARD_MAXIMUM_DOCUMENT_CHARACTERS = 32 * 1024 * 1024;
    private static final long HISTORY_RETENTION_SECONDS = 2_592_000L;
    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeAdapter(
                    Instant.class,
                    (JsonSerializer<Instant>) (value, type, context) ->
                            new JsonPrimitive(value.toString()))
            .registerTypeAdapter(
                    Instant.class,
                    (JsonDeserializer<Instant>) (value, type, context) ->
                            Instant.parse(value.getAsString()))
            .create();

    private final Map<UUID, EscrowRecord> records = new LinkedHashMap<>();
    private StorageService.Document document;
    private Path path;
    private RepositoryState state = RepositoryState.NEW;
    private long revision;
    private long flushedRevision;

    @Override
    public String id() {
        return "sef:escrow";
    }

    @Override
    public String domain() {
        return "escrow";
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
                .resolve("escrow.json")
                .toAbsolutePath()
                .normalize();
        records.clear();
        revision = 0L;
        flushedRevision = 0L;
        boolean existed = Files.exists(path);
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(null);
        if (document == null) {
            state = existed ? RepositoryState.RECOVERY : RepositoryState.MISSING;
            return new LoadResult(state, existed ? "escrow storage unavailable" : "new escrow repository");
        }
        try {
            EscrowFile file = GSON.fromJson(document.data(), EscrowFile.class);
            if (file == null
                    || file.revision() < 1L
                    || file.records().size() > HARD_MAXIMUM_RECORDS
                    || document.data().toString().length() > HARD_MAXIMUM_DOCUMENT_CHARACTERS) {
                throw new IllegalStateException("escrow storage is outside bounds");
            }
            for (EscrowRecord record : file.records()) {
                validate(record);
                if (records.putIfAbsent(record.id(), record) != null) {
                    throw new IllegalStateException("duplicate escrow record");
                }
            }
            revision = file.revision();
            flushedRevision = document.migrated() ? Math.max(0L, revision - 1L) : revision;
            state = RepositoryState.READY;
            pruneHistory(Instant.now());
            return new LoadResult(state, "loaded " + records.size() + " escrow records");
        } catch (RuntimeException exception) {
            records.clear();
            state = RepositoryState.RECOVERY;
            Throwable cause = exception.getCause() == null ? exception : exception.getCause();
            return new LoadResult(
                    state,
                    exception.getClass().getSimpleName() + ", "
                            + Objects.requireNonNullElse(cause.getMessage(), "unknown escrow storage error"));
        }
    }

    public synchronized ActionResult<EscrowRecord> createAndFlush(EscrowRecord record) {
        writable();
        validate(record);
        EscrowRecord existing = records.get(record.id());
        if (existing != null) {
            return sameDefinition(existing, record)
                    ? ActionResult.success(existing)
                    : ActionResult.failure(
                            ActionResult.ReasonCode.CONFLICT,
                            "escrow id was reused for another transaction");
        }
        pruneHistory(Instant.now());
        if (records.size() >= HARD_MAXIMUM_RECORDS) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "escrow record limit reached");
        }
        records.put(record.id(), record);
        revision = Math.addExact(revision, 1L);
        if (encodedCharacters() > HARD_MAXIMUM_DOCUMENT_CHARACTERS) {
            records.remove(record.id());
            revision = Math.addExact(revision, 1L);
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "escrow storage size limit reached");
        }
        try {
            flush();
            return ActionResult.success(record);
        } catch (IOException exception) {
            records.remove(record.id());
            revision = Math.addExact(revision, 1L);
            return ActionResult.failure(ActionResult.ReasonCode.STORAGE_ERROR, "escrow intent could not be persisted");
        }
    }

    public synchronized ActionResult<EscrowRecord> replaceAndFlush(
            EscrowRecord replacement,
            long expectedRevision,
            Set<EscrowState> expectedStates
    ) {
        writable();
        validate(replacement);
        EscrowRecord current = records.get(replacement.id());
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "escrow record not found");
        }
        if (current.revision() != expectedRevision || !expectedStates.contains(current.state())) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "escrow state or revision changed");
        }
        if (!sameImmutableValue(current, replacement)
                || replacement.revision() != Math.addExact(current.revision(), 1L)) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "escrow transition is invalid");
        }
        records.put(replacement.id(), replacement);
        revision = Math.addExact(revision, 1L);
        if (encodedCharacters() > HARD_MAXIMUM_DOCUMENT_CHARACTERS) {
            records.put(current.id(), current);
            revision = Math.addExact(revision, 1L);
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "escrow storage size limit reached");
        }
        try {
            flush();
            return ActionResult.success(replacement);
        } catch (IOException exception) {
            records.put(current.id(), current);
            revision = Math.addExact(revision, 1L);
            return ActionResult.failure(ActionResult.ReasonCode.STORAGE_ERROR, "escrow transition could not be persisted");
        }
    }

    public synchronized Optional<EscrowRecord> find(UUID recordId) {
        pruneHistory(Instant.now());
        return Optional.ofNullable(records.get(Objects.requireNonNull(recordId, "recordId")));
    }

    public synchronized List<EscrowRecord> recordsFor(UUID playerId) {
        pruneHistory(Instant.now());
        return records.values().stream()
                .filter(record -> record.ownerId().equals(playerId)
                        || record.beneficiaryId().equals(playerId)
                        || Objects.equals(record.settlementActorId(), playerId)
                        || Objects.equals(record.highestBidderId(), playerId)
                        || Objects.equals(record.pendingBidderId(), playerId))
                .sorted(Comparator.comparing(EscrowRecord::createdAt).reversed())
                .toList();
    }

    public synchronized List<EscrowRecord> recoveryRecords(UUID playerId) {
        return recordsFor(playerId).stream()
                .filter(record -> record.state().recoverable())
                .toList();
    }

    public synchronized Optional<EscrowRecord> findBySource(
            EscrowDomain domain,
            String source
    ) {
        pruneHistory(Instant.now());
        return records.values().stream()
                .filter(record -> record.domain() == Objects.requireNonNull(domain, "domain"))
                .filter(record -> record.source().equals(Objects.requireNonNull(source, "source")))
                .findFirst();
    }

    public synchronized int size() {
        pruneHistory(Instant.now());
        return records.size();
    }

    @Override
    public void flush() throws IOException {
        final EscrowFile file;
        final StorageService.Document previous;
        final Path destination;
        final long snapshotRevision;
        synchronized (this) {
            pruneHistory(Instant.now());
            if (path == null || !dirty()) {
                return;
            }
            writable();
            file = new EscrowFile(
                    Math.max(1L, revision),
                    records.values().stream()
                            .sorted(Comparator.comparing(EscrowRecord::createdAt))
                            .toList());
            previous = document;
            destination = path;
            snapshotRevision = revision;
        }
        StorageService.write(
                destination,
                domain(),
                SCHEMA_VERSION,
                GSON.toJsonTree(file),
                previous,
                Set.of());
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

    public static String itemDigest(List<ItemStackSnapshotCodec.SlotStack> items) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(
                    GSON.toJson(List.copyOf(items)).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("sha256 is unavailable", exception);
        }
    }

    private void writable() {
        if (state == RepositoryState.RECOVERY
                || state == RepositoryState.UNSUPPORTED
                || state == RepositoryState.ERROR
                || state == RepositoryState.CLOSED) {
            throw new IllegalStateException("escrow storage is unavailable");
        }
    }

    private void pruneHistory(Instant now) {
        int before = records.size();
        records.values().removeIf(record ->
                !record.state().holdsValue()
                        && record.updatedAt().plusSeconds(HISTORY_RETENTION_SECONDS).isBefore(now));
        if (records.size() != before) {
            revision = Math.addExact(revision, 1L);
        }
    }

    private int encodedCharacters() {
        int total = 0;
        for (EscrowRecord record : records.values()) {
            total = Math.addExact(total, GSON.toJson(record).length());
        }
        return total;
    }

    private static boolean sameDefinition(EscrowRecord current, EscrowRecord candidate) {
        return sameImmutableValue(current, candidate);
    }

    private static boolean sameImmutableValue(EscrowRecord current, EscrowRecord candidate) {
        return current.id().equals(candidate.id())
                && current.domain() == candidate.domain()
                && current.ownerId().equals(candidate.ownerId())
                && current.beneficiaryId().equals(candidate.beneficiaryId())
                && current.items().equals(candidate.items())
                && current.itemDigest().equals(candidate.itemDigest())
                && current.reservedCurrency() == candidate.reservedCurrency()
                && current.price() == candidate.price()
                && current.currency().equals(candidate.currency())
                && current.providerId().equals(candidate.providerId())
                && current.saleType().equals(candidate.saleType())
                && current.source().equals(candidate.source())
                && current.createdAt().equals(candidate.createdAt())
                && current.expiresAt().equals(candidate.expiresAt());
    }

    private static void validate(EscrowRecord record) {
        Objects.requireNonNull(record, "record");
        if (record.items().size() > ItemStackSnapshotCodec.HARD_MAXIMUM_SLOTS
                || GSON.toJson(record.items()).length()
                > ItemStackSnapshotCodec.HARD_MAXIMUM_ENCODED_CHARACTERS
                || !record.itemDigest().equals(itemDigest(record.items()))
                || record.reservedCurrency() < 0L
                || record.price() < 0L
                || record.highestBid() < 0L
                || record.pendingBid() < 0L
                || record.revision() < 1L
                || record.updatedAt().isBefore(record.createdAt())
                || !record.expiresAt().isAfter(record.createdAt())
                || record.items().isEmpty() && record.reservedCurrency() == 0L && record.price() == 0L) {
            throw new IllegalArgumentException("escrow record is invalid");
        }
        bounded(record.currency(), 32, true);
        bounded(record.providerId(), 64, true);
        bounded(record.source(), 128, false);
        bounded(record.saleType(), 16, true);
        bounded(record.detail(), 512, true);
        if (record.domain() != EscrowDomain.AUCTION
                && (record.price() != 0L
                || record.highestBid() != 0L
                || record.highestBidderId() != null
                || record.pendingBid() != 0L
                || record.pendingBidderId() != null
                || record.pendingBidOperationId() != null)) {
            throw new IllegalArgumentException("non auction escrow contains market state");
        }
        if (record.domain() == EscrowDomain.AUCTION
                && !Set.of("buy_now", "bid").contains(record.saleType())) {
            throw new IllegalArgumentException("auction sale type is invalid");
        }
        if (record.pendingBid() > 0L
                != (record.pendingBidderId() != null && record.pendingBidOperationId() != null)) {
            throw new IllegalArgumentException("pending bid state is incomplete");
        }
        if (record.highestBid() > 0L != (record.highestBidderId() != null)) {
            throw new IllegalArgumentException("highest bid state is incomplete");
        }
    }

    private static String bounded(String value, int maximum, boolean allowEmpty) {
        String normalized = Objects.requireNonNullElse(value, "").strip();
        if ((!allowEmpty && normalized.isBlank())
                || normalized.length() > maximum
                || normalized.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("escrow text is outside bounds");
        }
        return normalized;
    }

    public enum EscrowDomain {
        PARCEL,
        LOST_FOUND,
        TRADE,
        AUCTION;

        public static EscrowDomain fromFeature(String featureId) {
            return switch (Objects.requireNonNullElse(featureId, "").strip().toLowerCase(Locale.ROOT)) {
                case "parcels" -> PARCEL;
                case "lost_found" -> LOST_FOUND;
                case "trades" -> TRADE;
                case "auctions" -> AUCTION;
                default -> throw new IllegalArgumentException("feature does not use escrow");
            };
        }
    }

    public enum EscrowState {
        PREPARING,
        HELD,
        BIDDING,
        SETTLING,
        RETURNING,
        SETTLED,
        RETURNED,
        FROZEN,
        RECOVERY_REQUIRED;

        public boolean holdsValue() {
            return this != SETTLED && this != RETURNED;
        }

        public boolean recoverable() {
            return this == PREPARING
                    || this == BIDDING
                    || this == SETTLING
                    || this == RETURNING
                    || this == RECOVERY_REQUIRED;
        }
    }

    public record EscrowRecord(
            UUID id,
            EscrowDomain domain,
            UUID ownerId,
            UUID beneficiaryId,
            List<ItemStackSnapshotCodec.SlotStack> items,
            String itemDigest,
            long reservedCurrency,
            long price,
            String currency,
            String providerId,
            String saleType,
            EscrowState state,
            UUID settlementActorId,
            long highestBid,
            UUID highestBidderId,
            long pendingBid,
            UUID pendingBidderId,
            UUID pendingBidOperationId,
            String source,
            Instant createdAt,
            Instant expiresAt,
            Instant updatedAt,
            long revision,
            String detail
    ) {
        public EscrowRecord {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(domain, "domain");
            Objects.requireNonNull(ownerId, "ownerId");
            Objects.requireNonNull(beneficiaryId, "beneficiaryId");
            items = List.copyOf(Objects.requireNonNull(items, "items"));
            itemDigest = bounded(itemDigest, 64, false);
            currency = bounded(currency, 32, true);
            providerId = bounded(providerId, 64, true);
            saleType = bounded(saleType, 16, true);
            Objects.requireNonNull(state, "state");
            source = bounded(source, 128, false);
            Objects.requireNonNull(createdAt, "createdAt");
            Objects.requireNonNull(expiresAt, "expiresAt");
            Objects.requireNonNull(updatedAt, "updatedAt");
            detail = bounded(detail, 512, true);
        }

        public EscrowRecord transition(
                EscrowState next,
                UUID actorId,
                String transitionDetail,
                Instant now
        ) {
            return new EscrowRecord(
                    id,
                    domain,
                    ownerId,
                    beneficiaryId,
                    items,
                    itemDigest,
                    reservedCurrency,
                    price,
                    currency,
                    providerId,
                    saleType,
                    next,
                    actorId,
                    highestBid,
                    highestBidderId,
                    pendingBid,
                    pendingBidderId,
                    pendingBidOperationId,
                    source,
                    createdAt,
                    expiresAt,
                    now,
                    Math.addExact(revision, 1L),
                    transitionDetail);
        }

        public EscrowRecord beginBid(UUID bidderId, long amount, UUID operationId, Instant now) {
            return new EscrowRecord(
                    id,
                    domain,
                    ownerId,
                    beneficiaryId,
                    items,
                    itemDigest,
                    reservedCurrency,
                    price,
                    currency,
                    providerId,
                    saleType,
                    EscrowState.BIDDING,
                    settlementActorId,
                    highestBid,
                    highestBidderId,
                    amount,
                    bidderId,
                    operationId,
                    source,
                    createdAt,
                    expiresAt,
                    now,
                    Math.addExact(revision, 1L),
                    "bid reservation pending");
        }

        public EscrowRecord completeBid(Instant now) {
            return new EscrowRecord(
                    id,
                    domain,
                    ownerId,
                    beneficiaryId,
                    items,
                    itemDigest,
                    reservedCurrency,
                    price,
                    currency,
                    providerId,
                    saleType,
                    EscrowState.HELD,
                    settlementActorId,
                    pendingBid,
                    pendingBidderId,
                    0L,
                    null,
                    null,
                    source,
                    createdAt,
                    expiresAt,
                    now,
                    Math.addExact(revision, 1L),
                    "highest bid reserved");
        }

        public EscrowRecord clearPendingBid(EscrowState next, String transitionDetail, Instant now) {
            return new EscrowRecord(
                    id,
                    domain,
                    ownerId,
                    beneficiaryId,
                    items,
                    itemDigest,
                    reservedCurrency,
                    price,
                    currency,
                    providerId,
                    saleType,
                    next,
                    settlementActorId,
                    highestBid,
                    highestBidderId,
                    0L,
                    null,
                    null,
                    source,
                    createdAt,
                    expiresAt,
                    now,
                    Math.addExact(revision, 1L),
                    transitionDetail);
        }
    }

    private record EscrowFile(long revision, List<EscrowRecord> records) {
        private EscrowFile {
            records = List.copyOf(records == null ? List.of() : records);
        }
    }
}
