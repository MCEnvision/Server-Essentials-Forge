package com.enviouse.sef.control;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.storage.InstantJsonAdapter;
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
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public final class ApprovalRepository implements StorageRepository {
    public static final int SCHEMA_VERSION = 1;
    public static final int MAXIMUM_REQUESTS = 20_000;
    public static final int MAXIMUM_HISTORY = 40_000;
    public static final long MAXIMUM_DURATION_SECONDS = 604_800L;
    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeAdapter(Instant.class, new InstantJsonAdapter())
            .create();

    private final Clock clock;
    private final Map<UUID, ApprovalRequest> requests = new LinkedHashMap<>();
    private final List<HistoryEntry> history = new ArrayList<>();
    private StorageService.Document document;
    private Path path;
    private RepositoryState state = RepositoryState.NEW;
    private long revision = 1L;
    private long flushedRevision = 1L;

    public ApprovalRepository() {
        this(Clock.systemUTC());
    }

    ApprovalRepository(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public String id() {
        return "sef:approvals";
    }

    @Override
    public String domain() {
        return "approvals";
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public synchronized Path path() {
        return path;
    }

    public synchronized ActionResult<ApprovalRequest> create(
            UUID requesterId,
            String actionId,
            String payloadHash,
            String preview,
            Duration duration,
            boolean separationRequired,
            String reason
    ) {
        writable();
        expire();
        Objects.requireNonNull(requesterId, "requesterId");
        Objects.requireNonNull(duration, "duration");
        if (requests.size() >= MAXIMUM_REQUESTS) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "approval request capacity is full");
        }
        if (duration.isZero()
                || duration.isNegative()
                || duration.getSeconds() > MAXIMUM_DURATION_SECONDS) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "approval duration is outside bounds");
        }
        String normalizedAction;
        String normalizedHash;
        String normalizedPreview;
        String normalizedReason;
        try {
            normalizedAction = bounded(actionId, 192, false);
            normalizedHash = hash(payloadHash);
            normalizedPreview = bounded(preview, 4096, false);
            normalizedReason = bounded(reason, 1024, false);
        } catch (IllegalArgumentException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, exception.getMessage());
        }
        boolean duplicate = requests.values().stream().anyMatch(request ->
                request.requesterId().equals(requesterId)
                        && request.actionId().equals(normalizedAction)
                        && request.payloadHash().equals(normalizedHash)
                        && request.state().pending()
                        && request.expiresAt().isAfter(now()));
        if (duplicate) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "matching approval request already exists");
        }
        Instant createdAt = now();
        ApprovalRequest request = new ApprovalRequest(
                UUID.randomUUID(),
                requesterId,
                normalizedAction,
                normalizedHash,
                normalizedPreview,
                normalizedReason,
                separationRequired,
                ApprovalState.PENDING,
                List.of(),
                null,
                createdAt,
                createdAt.plus(duration),
                createdAt,
                1L);
        requests.put(request.id(), request);
        history(request, requesterId, "created", ApprovalState.PENDING, normalizedReason);
        changed();
        return ActionResult.success(request);
    }

    public synchronized ActionResult<ApprovalRequest> approve(
            UUID requestId,
            long expectedRevision,
            UUID approverId,
            String note
    ) {
        writable();
        expire();
        ApprovalRequest current = requests.get(Objects.requireNonNull(requestId, "requestId"));
        if (current == null || !current.state().pending()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "pending approval request not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "approval request revision changed");
        }
        UUID approver = Objects.requireNonNull(approverId, "approverId");
        if (current.separationRequired() && current.requesterId().equals(approver)) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "requester cannot approve this request");
        }
        if (current.approvals().stream().anyMatch(approval -> approval.approverId().equals(approver))) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "approver already approved this request");
        }
        Instant now = now();
        List<Approval> approvals = new ArrayList<>(current.approvals());
        approvals.add(new Approval(approver, bounded(note, 1024, true), now));
        ApprovalRequest updated = current.update(
                ApprovalState.APPROVED,
                approvals,
                null,
                now);
        requests.put(updated.id(), updated);
        history(updated, approver, "approved", ApprovalState.APPROVED, note);
        changed();
        return ActionResult.success(updated);
    }

    public synchronized ActionResult<ApprovalRequest> consume(
            UUID requestId,
            UUID executorId,
            String actionId,
            String payloadHash
    ) {
        writable();
        expire();
        ApprovalRequest current = requests.get(Objects.requireNonNull(requestId, "requestId"));
        if (current == null || current.state() != ApprovalState.APPROVED) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "approved request not found");
        }
        UUID executor = Objects.requireNonNull(executorId, "executorId");
        if (!current.requesterId().equals(executor)) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "only the requester can execute this approval");
        }
        if (!current.actionId().equals(bounded(actionId, 192, false))
                || !current.payloadHash().equals(hash(payloadHash))) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "approved preview does not match this action");
        }
        Instant now = now();
        ApprovalRequest updated = current.update(
                ApprovalState.EXECUTED,
                current.approvals(),
                executor,
                now);
        requests.put(updated.id(), updated);
        history(updated, executor, "executed", ApprovalState.EXECUTED, "");
        changed();
        return ActionResult.success(updated);
    }

    public synchronized ActionResult<ApprovalRequest> restoreApproved(
            UUID requestId,
            UUID actorId,
            String reason
    ) {
        writable();
        ApprovalRequest current = requests.get(Objects.requireNonNull(requestId, "requestId"));
        if (current == null
                || current.state() != ApprovalState.EXECUTED
                || !Objects.equals(current.executedBy(), actorId)
                || !current.expiresAt().isAfter(now())) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "executed approval cannot be restored");
        }
        ApprovalRequest updated = current.update(
                ApprovalState.APPROVED,
                current.approvals(),
                null,
                now());
        requests.put(updated.id(), updated);
        history(updated, actorId, "execution_compensated", ApprovalState.APPROVED, reason);
        changed();
        return ActionResult.success(updated);
    }

    public synchronized ActionResult<ApprovalRequest> revoke(
            UUID requestId,
            long expectedRevision,
            UUID actorId,
            String reason
    ) {
        writable();
        ApprovalRequest current = requests.get(Objects.requireNonNull(requestId, "requestId"));
        if (current == null || current.state().terminal()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "active approval request not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "approval request revision changed");
        }
        ApprovalRequest updated = current.update(
                ApprovalState.REVOKED,
                current.approvals(),
                null,
                now());
        requests.put(updated.id(), updated);
        history(updated, actorId, "revoked", ApprovalState.REVOKED, reason);
        changed();
        return ActionResult.success(updated);
    }

    public synchronized Optional<ApprovalRequest> find(UUID requestId) {
        expire();
        return Optional.ofNullable(requests.get(requestId));
    }

    public synchronized List<ApprovalRequest> requests(
            UUID requesterId,
            ApprovalState requestedState
    ) {
        expire();
        return requests.values().stream()
                .filter(request -> requesterId == null || request.requesterId().equals(requesterId))
                .filter(request -> requestedState == null || request.state() == requestedState)
                .sorted(Comparator.comparing(ApprovalRequest::createdAt).reversed())
                .toList();
    }

    public synchronized List<HistoryEntry> history(UUID requestId) {
        return history.stream()
                .filter(entry -> requestId == null || entry.requestId().equals(requestId))
                .sorted(Comparator.comparing(HistoryEntry::at).reversed())
                .toList();
    }

    public synchronized <T> ActionResult<T> commit(Supplier<ActionResult<T>> mutation) {
        Objects.requireNonNull(mutation, "mutation");
        Checkpoint checkpoint = checkpoint();
        ActionResult<T> result;
        try {
            result = Objects.requireNonNull(mutation.get(), "mutation result");
        } catch (RuntimeException exception) {
            restore(checkpoint);
            throw exception;
        }
        if (!result.successful()) {
            restore(checkpoint);
            return result;
        }
        try {
            flush();
            return result;
        } catch (IOException | RuntimeException exception) {
            restore(checkpoint);
            ServerEssentialsForge.LOGGER.error("Approval persistence failed", exception);
            return ActionResult.failure(
                    ActionResult.ReasonCode.STORAGE_ERROR,
                    "approval storage could not be committed");
        }
    }

    @Override
    public synchronized LoadResult load(Path managedRoot) {
        path = Objects.requireNonNull(managedRoot, "managedRoot")
                .resolve("approvals.json")
                .toAbsolutePath()
                .normalize();
        requests.clear();
        history.clear();
        boolean existed = Files.exists(path);
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(null);
        if (document == null) {
            state = existed ? RepositoryState.RECOVERY : RepositoryState.MISSING;
            revision = 1L;
            flushedRevision = 1L;
            return new LoadResult(state, existed ? "approval storage unavailable" : "new approval repository");
        }
        try {
            Snapshot snapshot = GSON.fromJson(document.data(), Snapshot.class);
            if (snapshot == null
                    || snapshot.revision() < 1L
                    || snapshot.requests().size() > MAXIMUM_REQUESTS
                    || snapshot.history().size() > MAXIMUM_HISTORY) {
                throw new IllegalStateException("approval snapshot is outside bounds");
            }
            for (ApprovalRequest request : snapshot.requests()) {
                validate(request);
                if (requests.putIfAbsent(request.id(), request) != null) {
                    throw new IllegalStateException("duplicate approval request");
                }
            }
            for (HistoryEntry entry : snapshot.history()) {
                validate(entry);
                history.add(entry);
            }
            revision = snapshot.revision();
            flushedRevision = document.migrated() ? Math.max(0L, revision - 1L) : revision;
            state = RepositoryState.READY;
            expire();
            return new LoadResult(state, "loaded approval records");
        } catch (RuntimeException exception) {
            requests.clear();
            history.clear();
            state = RepositoryState.RECOVERY;
            return new LoadResult(state, exception.getClass().getSimpleName());
        }
    }

    @Override
    public synchronized void flush() throws IOException {
        if (path == null || !dirty()) {
            return;
        }
        writable();
        expire();
        Snapshot snapshot = new Snapshot(
                revision,
                new ArrayList<>(requests.values()),
                new ArrayList<>(history));
        StorageService.write(
                path,
                domain(),
                SCHEMA_VERSION,
                GSON.toJsonTree(snapshot),
                document,
                Set.of("/requests", "/history"));
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(document);
        flushedRevision = revision;
        state = RepositoryState.READY;
    }

    @Override
    public synchronized boolean dirty() {
        return revision > flushedRevision;
    }

    @Override
    public synchronized RepositoryState state() {
        return state;
    }

    public static String payloadHash(Map<String, String> values) {
        Objects.requireNonNull(values, "values");
        if (values.size() > 64) {
            throw new IllegalArgumentException("approval payload has too many fields");
        }
        String canonical = values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> bounded(entry.getKey(), 128, false)
                        + "\u0000"
                        + bounded(entry.getValue(), 4096, true))
                .collect(java.util.stream.Collectors.joining("\u0001"));
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("sha256 is unavailable", exception);
        }
    }

    private void expire() {
        Instant now = now();
        for (ApprovalRequest request : List.copyOf(requests.values())) {
            if (request.state().pending() && !request.expiresAt().isAfter(now)) {
                ApprovalRequest expired = request.update(
                        ApprovalState.EXPIRED,
                        request.approvals(),
                        null,
                        now);
                requests.put(expired.id(), expired);
                history(expired, new UUID(0L, 0L), "expired", ApprovalState.EXPIRED, "");
                changed();
            }
        }
    }

    private void history(
            ApprovalRequest request,
            UUID actorId,
            String operation,
            ApprovalState state,
            String note
    ) {
        history.add(new HistoryEntry(
                UUID.randomUUID(),
                request.id(),
                Objects.requireNonNull(actorId, "actorId"),
                bounded(operation, 64, false),
                state,
                bounded(note, 1024, true),
                now(),
                request.revision()));
        while (history.size() > MAXIMUM_HISTORY) {
            history.removeFirst();
        }
    }

    private void changed() {
        revision = Math.addExact(revision, 1L);
    }

    private Instant now() {
        return Instant.now(clock);
    }

    private Checkpoint checkpoint() {
        return new Checkpoint(
                new LinkedHashMap<>(requests),
                new ArrayList<>(history),
                revision,
                flushedRevision,
                document,
                state);
    }

    private void restore(Checkpoint checkpoint) {
        requests.clear();
        requests.putAll(checkpoint.requests());
        history.clear();
        history.addAll(checkpoint.history());
        revision = checkpoint.revision();
        flushedRevision = checkpoint.flushedRevision();
        document = checkpoint.document();
        state = checkpoint.state();
    }

    private void writable() {
        if (state == RepositoryState.RECOVERY
                || state == RepositoryState.UNSUPPORTED
                || state == RepositoryState.ERROR
                || state == RepositoryState.CLOSED) {
            throw new IllegalStateException("approval storage is unavailable");
        }
    }

    private static void validate(ApprovalRequest request) {
        Objects.requireNonNull(request, "request");
        bounded(request.actionId(), 192, false);
        hash(request.payloadHash());
        bounded(request.preview(), 4096, false);
        bounded(request.reason(), 1024, false);
        if (request.revision() < 1L
                || !request.expiresAt().isAfter(request.createdAt())
                || request.updatedAt().isBefore(request.createdAt())
                || request.approvals().size() > 16) {
            throw new IllegalArgumentException("approval request is invalid");
        }
        request.approvals().forEach(approval -> {
            Objects.requireNonNull(approval.approverId(), "approverId");
            bounded(approval.note(), 1024, true);
            Objects.requireNonNull(approval.at(), "approvalAt");
        });
    }

    private static void validate(HistoryEntry entry) {
        Objects.requireNonNull(entry, "history");
        bounded(entry.operation(), 64, false);
        bounded(entry.note(), 1024, true);
        if (entry.requestRevision() < 1L) {
            throw new IllegalArgumentException("approval history is invalid");
        }
    }

    private static String hash(String value) {
        String result = Objects.requireNonNullElse(value, "").strip().toLowerCase(java.util.Locale.ROOT);
        if (!result.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("approval payload hash is invalid");
        }
        return result;
    }

    private static String bounded(String value, int maximum, boolean blankAllowed) {
        String result = Objects.requireNonNullElse(value, "").strip();
        if ((!blankAllowed && result.isBlank())
                || result.length() > maximum
                || result.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("approval text is outside bounds");
        }
        return result;
    }

    public enum ApprovalState {
        PENDING,
        APPROVED,
        EXECUTED,
        REVOKED,
        EXPIRED;

        public boolean pending() {
            return this == PENDING || this == APPROVED;
        }

        public boolean terminal() {
            return this == EXECUTED || this == REVOKED || this == EXPIRED;
        }
    }

    public record Approval(UUID approverId, String note, Instant at) {
        public Approval {
            Objects.requireNonNull(approverId, "approverId");
            note = bounded(note, 1024, true);
            Objects.requireNonNull(at, "at");
        }
    }

    public record ApprovalRequest(
            UUID id,
            UUID requesterId,
            String actionId,
            String payloadHash,
            String preview,
            String reason,
            boolean separationRequired,
            ApprovalState state,
            List<Approval> approvals,
            UUID executedBy,
            Instant createdAt,
            Instant expiresAt,
            Instant updatedAt,
            long revision
    ) {
        public ApprovalRequest {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(requesterId, "requesterId");
            actionId = bounded(actionId, 192, false);
            payloadHash = hash(payloadHash);
            preview = bounded(preview, 4096, false);
            reason = bounded(reason, 1024, false);
            Objects.requireNonNull(state, "state");
            approvals = List.copyOf(Objects.requireNonNullElse(approvals, List.of()));
            Objects.requireNonNull(createdAt, "createdAt");
            Objects.requireNonNull(expiresAt, "expiresAt");
            Objects.requireNonNull(updatedAt, "updatedAt");
            if (revision < 1L
                    || !expiresAt.isAfter(createdAt)
                    || updatedAt.isBefore(createdAt)
                    || approvals.size() > 16) {
                throw new IllegalArgumentException("approval request is invalid");
            }
        }

        private ApprovalRequest update(
                ApprovalState nextState,
                List<Approval> nextApprovals,
                UUID nextExecutor,
                Instant now
        ) {
            return new ApprovalRequest(
                    id,
                    requesterId,
                    actionId,
                    payloadHash,
                    preview,
                    reason,
                    separationRequired,
                    nextState,
                    nextApprovals,
                    nextExecutor,
                    createdAt,
                    expiresAt,
                    now,
                    Math.addExact(revision, 1L));
        }
    }

    public record HistoryEntry(
            UUID id,
            UUID requestId,
            UUID actorId,
            String operation,
            ApprovalState resultingState,
            String note,
            Instant at,
            long requestRevision
    ) {
        public HistoryEntry {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(requestId, "requestId");
            Objects.requireNonNull(actorId, "actorId");
            operation = bounded(operation, 64, false);
            Objects.requireNonNull(resultingState, "resultingState");
            note = bounded(note, 1024, true);
            Objects.requireNonNull(at, "at");
            if (requestRevision < 1L) {
                throw new IllegalArgumentException("approval history revision is invalid");
            }
        }
    }

    private record Snapshot(
            long revision,
            List<ApprovalRequest> requests,
            List<HistoryEntry> history
    ) {
        private Snapshot {
            requests = List.copyOf(Objects.requireNonNullElse(requests, List.of()));
            history = List.copyOf(Objects.requireNonNullElse(history, List.of()));
        }
    }

    private record Checkpoint(
            Map<UUID, ApprovalRequest> requests,
            List<HistoryEntry> history,
            long revision,
            long flushedRevision,
            StorageService.Document document,
            RepositoryState state
    ) {
    }
}
