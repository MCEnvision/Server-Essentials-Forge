package com.enviouse.sef.fancytags;

import com.enviouse.sef.kernel.ActionResult;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class FancyTagTransferService {
    public static final int HARD_MAXIMUM_CONCURRENT_UPLOADS = 64;
    public static final int HARD_MAXIMUM_UPLOADS_PER_PLAYER = 2;
    public static final int HARD_MAXIMUM_CHUNK_BYTES = 32_768;
    public static final Duration HARD_MAXIMUM_TIMEOUT = Duration.ofMinutes(2);

    private final int maximumConcurrentUploads;
    private final int maximumUploadsPerPlayer;
    private final int maximumChunkBytes;
    private final int maximumUploadBytes;
    private final Duration timeout;
    private final Map<UUID, Upload> uploads = new LinkedHashMap<>();

    public FancyTagTransferService(
            int maximumConcurrentUploads,
            int maximumUploadsPerPlayer,
            int maximumChunkBytes,
            int maximumUploadBytes,
            Duration timeout
    ) {
        if (maximumConcurrentUploads < 1 || maximumConcurrentUploads > HARD_MAXIMUM_CONCURRENT_UPLOADS
                || maximumUploadsPerPlayer < 1 || maximumUploadsPerPlayer > HARD_MAXIMUM_UPLOADS_PER_PLAYER
                || maximumChunkBytes < 1 || maximumChunkBytes > HARD_MAXIMUM_CHUNK_BYTES
                || maximumUploadBytes < 1
                || maximumUploadBytes > FancyTagObjectStore.HARD_MAXIMUM_ENCODED_BYTES
                || timeout == null || timeout.isZero() || timeout.isNegative()
                || timeout.compareTo(HARD_MAXIMUM_TIMEOUT) > 0) {
            throw new IllegalArgumentException("Fancy Tags transfer settings are outside hard bounds");
        }
        this.maximumConcurrentUploads = maximumConcurrentUploads;
        this.maximumUploadsPerPlayer = maximumUploadsPerPlayer;
        this.maximumChunkBytes = maximumChunkBytes;
        this.maximumUploadBytes = maximumUploadBytes;
        this.timeout = timeout;
    }

    public synchronized ActionResult<UploadView> begin(
            UUID ownerId,
            UUID tagId,
            UUID leaseId,
            long expectedTagRevision,
            int totalBytes,
            String expectedHash,
            Instant now
    ) {
        Objects.requireNonNull(ownerId, "ownerId");
        Objects.requireNonNull(tagId, "tagId");
        Objects.requireNonNull(leaseId, "leaseId");
        Objects.requireNonNull(now, "now");
        prune(now);
        if (expectedTagRevision < 1L
                || totalBytes < 1
                || totalBytes > maximumUploadBytes
                || expectedHash == null
                || !expectedHash.matches("[0-9a-f]{64}")) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "upload declaration is invalid");
        }
        long owned = uploads.values().stream().filter(value -> value.ownerId.equals(ownerId)).count();
        if (uploads.size() >= maximumConcurrentUploads || owned >= maximumUploadsPerPlayer) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "upload capacity is exhausted");
        }
        UUID uploadId = UUID.randomUUID();
        Upload upload = new Upload(
                uploadId,
                ownerId,
                tagId,
                leaseId,
                expectedTagRevision,
                totalBytes,
                expectedHash,
                now,
                now.plus(timeout),
                new ByteArrayOutputStream(Math.min(totalBytes, maximumChunkBytes)),
                0);
        uploads.put(uploadId, upload);
        return ActionResult.success(view(upload));
    }

    public synchronized ActionResult<UploadView> acceptChunk(
            UUID ownerId,
            UUID uploadId,
            int chunkIndex,
            byte[] bytes,
            Instant now
    ) {
        Objects.requireNonNull(ownerId, "ownerId");
        Objects.requireNonNull(uploadId, "uploadId");
        Objects.requireNonNull(now, "now");
        prune(now);
        Upload upload = uploads.get(uploadId);
        if (upload == null || !upload.ownerId.equals(ownerId)) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "upload session is unavailable");
        }
        if (chunkIndex != upload.nextChunkIndex) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "upload chunk is stale or out of order");
        }
        if (bytes == null || bytes.length < 1 || bytes.length > maximumChunkBytes
                || upload.buffer.size() + bytes.length > upload.totalBytes) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "upload chunk is outside bounds");
        }
        upload.buffer.writeBytes(bytes);
        upload.nextChunkIndex = Math.addExact(upload.nextChunkIndex, 1);
        upload.expiresAt = now.plus(timeout);
        return ActionResult.success(view(upload));
    }

    public synchronized ActionResult<CompletedUpload> finish(
            UUID ownerId,
            UUID uploadId,
            Instant now
    ) {
        Objects.requireNonNull(ownerId, "ownerId");
        Objects.requireNonNull(uploadId, "uploadId");
        Objects.requireNonNull(now, "now");
        prune(now);
        Upload upload = uploads.get(uploadId);
        if (upload == null || !upload.ownerId.equals(ownerId)) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "upload session is unavailable");
        }
        byte[] bytes = upload.buffer.toByteArray();
        if (bytes.length != upload.totalBytes) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "upload is incomplete");
        }
        String actual = sha256(bytes);
        if (!actual.equals(upload.expectedHash)) {
            uploads.remove(uploadId, upload);
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "upload hash does not match");
        }
        if (!uploads.remove(uploadId, upload)) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "upload session changed");
        }
        return ActionResult.success(new CompletedUpload(
                upload.uploadId,
                upload.ownerId,
                upload.tagId,
                upload.leaseId,
                upload.expectedTagRevision,
                actual,
                bytes));
    }

    public synchronized ActionResult<Void> cancel(UUID actorId, UUID uploadId, boolean override) {
        Upload upload = uploads.get(Objects.requireNonNull(uploadId, "uploadId"));
        if (upload == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "upload session is unavailable");
        }
        if (!upload.ownerId.equals(actorId) && !override) {
            return ActionResult.failure(ActionResult.ReasonCode.PERMISSION_DENIED, "upload session belongs to another player");
        }
        uploads.remove(uploadId);
        return ActionResult.success(null);
    }

    public synchronized void logout(UUID ownerId) {
        uploads.values().removeIf(value -> value.ownerId.equals(ownerId));
    }

    public synchronized List<UploadView> active(Instant now) {
        prune(now);
        return uploads.values().stream().map(FancyTagTransferService::view).toList();
    }

    public synchronized void clear() {
        uploads.clear();
    }

    private void prune(Instant now) {
        uploads.values().removeIf(value -> !value.expiresAt.isAfter(now));
    }

    private static UploadView view(Upload upload) {
        return new UploadView(
                upload.uploadId,
                upload.ownerId,
                upload.tagId,
                upload.leaseId,
                upload.expectedTagRevision,
                upload.totalBytes,
                upload.buffer.size(),
                upload.nextChunkIndex,
                upload.createdAt,
                upload.expiresAt);
    }

    private static String sha256(byte[] bytes) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256").digest(bytes);
            return java.util.HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public record UploadView(
            UUID uploadId,
            UUID ownerId,
            UUID tagId,
            UUID leaseId,
            long expectedTagRevision,
            int totalBytes,
            int receivedBytes,
            int nextChunkIndex,
            Instant createdAt,
            Instant expiresAt
    ) {
    }

    public record CompletedUpload(
            UUID uploadId,
            UUID ownerId,
            UUID tagId,
            UUID leaseId,
            long expectedTagRevision,
            String hash,
            byte[] bytes
    ) {
        public CompletedUpload {
            bytes = bytes.clone();
        }

        @Override
        public byte[] bytes() {
            return bytes.clone();
        }
    }

    private static final class Upload {
        private final UUID uploadId;
        private final UUID ownerId;
        private final UUID tagId;
        private final UUID leaseId;
        private final long expectedTagRevision;
        private final int totalBytes;
        private final String expectedHash;
        private final Instant createdAt;
        private Instant expiresAt;
        private final ByteArrayOutputStream buffer;
        private int nextChunkIndex;

        private Upload(
                UUID uploadId,
                UUID ownerId,
                UUID tagId,
                UUID leaseId,
                long expectedTagRevision,
                int totalBytes,
                String expectedHash,
                Instant createdAt,
                Instant expiresAt,
                ByteArrayOutputStream buffer,
                int nextChunkIndex
        ) {
            this.uploadId = uploadId;
            this.ownerId = ownerId;
            this.tagId = tagId;
            this.leaseId = leaseId;
            this.expectedTagRevision = expectedTagRevision;
            this.totalBytes = totalBytes;
            this.expectedHash = expectedHash;
            this.createdAt = createdAt;
            this.expiresAt = expiresAt;
            this.buffer = buffer;
            this.nextChunkIndex = nextChunkIndex;
        }
    }
}
