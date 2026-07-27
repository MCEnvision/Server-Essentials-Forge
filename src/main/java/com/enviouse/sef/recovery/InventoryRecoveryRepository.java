package com.enviouse.sef.recovery;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.repository.StorageRepository;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class InventoryRecoveryRepository implements StorageRepository {
    public static final int SCHEMA_VERSION = 1;
    public static final int HARD_MAXIMUM_PLAYERS = 10_000;
    public static final int HARD_MAXIMUM_SNAPSHOTS_PER_PLAYER = 128;
    public static final int HARD_MAXIMUM_TOTAL_SNAPSHOTS = 10_000;
    public static final int HARD_MAXIMUM_DOCUMENT_CHARACTERS = 12 * 1024 * 1024;
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private final Map<UUID, Deque<InventorySnapshot>> snapshots = new LinkedHashMap<>();
    private StorageService.Document document;
    private Path path;
    private RepositoryState state = RepositoryState.NEW;
    private long revision;
    private long flushedRevision;

    @Override
    public String id() {
        return "sef:inventory_recovery";
    }

    @Override
    public String domain() {
        return "inventory_recovery";
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
                .resolve("inventory-recovery.json")
                .toAbsolutePath()
                .normalize();
        snapshots.clear();
        revision = 0L;
        flushedRevision = 0L;
        boolean existed = Files.exists(path);
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(null);
        if (document == null) {
            state = existed ? RepositoryState.RECOVERY : RepositoryState.MISSING;
            return new LoadResult(state, existed
                    ? "inventory recovery storage unavailable"
                    : "new inventory recovery repository");
        }
        try {
            SnapshotFile file = GSON.fromJson(document.data(), SnapshotFile.class);
            if (file == null
                    || file.revision() < 1L
                    || file.players().size() > HARD_MAXIMUM_PLAYERS
                    || document.data().toString().length() > HARD_MAXIMUM_DOCUMENT_CHARACTERS) {
                throw new IllegalStateException("inventory recovery snapshot is outside bounds");
            }
            Instant now = Instant.now();
            for (PlayerSnapshots player : file.players()) {
                if (player.snapshots().size() > HARD_MAXIMUM_SNAPSHOTS_PER_PLAYER
                        || snapshots.containsKey(player.playerId())) {
                    throw new IllegalStateException("inventory recovery player entry is outside bounds");
                }
                Deque<InventorySnapshot> retained = new ArrayDeque<>();
                for (InventorySnapshot snapshot : player.snapshots()) {
                    validate(snapshot);
                    if (snapshot.expiresAt().isAfter(now)) {
                        retained.addLast(snapshot);
                    }
                }
                if (!retained.isEmpty()) {
                    snapshots.put(player.playerId(), retained);
                }
                if (countWithoutPrune() > HARD_MAXIMUM_TOTAL_SNAPSHOTS) {
                    throw new IllegalStateException("inventory recovery snapshot count is outside bounds");
                }
            }
            revision = file.revision();
            flushedRevision = document.migrated() ? Math.max(0L, revision - 1L) : revision;
            state = RepositoryState.READY;
            return new LoadResult(state, "loaded " + count() + " inventory recovery snapshots");
        } catch (RuntimeException exception) {
            snapshots.clear();
            state = RepositoryState.RECOVERY;
            return new LoadResult(state, exception.getClass().getSimpleName());
        }
    }

    public synchronized ActionResult<InventorySnapshot> capture(
            ServerPlayer player,
            String reason,
            boolean includeEnderChest,
            int maximumSnapshots,
            long retentionSeconds
    ) {
        writable();
        Objects.requireNonNull(player, "player");
        int maximum = Math.clamp(maximumSnapshots, 1, HARD_MAXIMUM_SNAPSHOTS_PER_PLAYER);
        long retention = Math.clamp(retentionSeconds, 60L, 31_536_000L);
        prune(Instant.now());
        if ((!snapshots.containsKey(player.getUUID()) && snapshots.size() >= HARD_MAXIMUM_PLAYERS)
                || countWithoutPrune() >= HARD_MAXIMUM_TOTAL_SNAPSHOTS) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.QUOTA_EXCEEDED,
                    "inventory recovery capacity is full");
        }
        try {
            List<ItemStackSnapshotCodec.SlotStack> inventory =
                    ItemStackSnapshotCodec.capture(player.getInventory(), player.registryAccess());
            List<ItemStackSnapshotCodec.SlotStack> enderChest = includeEnderChest
                    ? ItemStackSnapshotCodec.capture(player.getEnderChestInventory(), player.registryAccess())
                    : List.of();
            Instant now = Instant.now();
            InventorySnapshot snapshot = new InventorySnapshot(
                    UUID.randomUUID(),
                    player.getUUID(),
                    bounded(reason, 64),
                    player.level().dimension().location().toString(),
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    inventory,
                    enderChest,
                    includeEnderChest,
                    now,
                    now.plusSeconds(retention),
                    1L);
            validate(snapshot);
            Deque<InventorySnapshot> previousHistory = snapshots.containsKey(player.getUUID())
                    ? new ArrayDeque<>(snapshots.get(player.getUUID()))
                    : null;
            Deque<InventorySnapshot> history = snapshots.computeIfAbsent(
                    player.getUUID(),
                    ignored -> new ArrayDeque<>());
            history.addLast(snapshot);
            while (history.size() > maximum) {
                history.removeFirst();
            }
            if (encodedCharacters() > HARD_MAXIMUM_DOCUMENT_CHARACTERS) {
                if (previousHistory == null) {
                    snapshots.remove(player.getUUID());
                } else {
                    snapshots.put(player.getUUID(), previousHistory);
                }
                return ActionResult.failure(
                        ActionResult.ReasonCode.QUOTA_EXCEEDED,
                        "inventory recovery storage size limit reached");
            }
            revision = Math.addExact(revision, 1L);
            return ActionResult.success(snapshot);
        } catch (IllegalArgumentException | ArithmeticException exception) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.INVALID_INPUT,
                    "inventory snapshot could not be captured");
        }
    }

    public synchronized ActionResult<InventorySnapshot> restore(
            ServerPlayer target,
            UUID snapshotId,
            long expectedRevision,
            int maximumSnapshots,
            long retentionSeconds
    ) {
        writable();
        Objects.requireNonNull(target, "target");
        InventorySnapshot snapshot = find(snapshotId).orElse(null);
        if (snapshot == null || !snapshot.playerId().equals(target.getUUID())) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "inventory snapshot not found");
        }
        if (snapshot.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "inventory snapshot revision changed");
        }
        try {
            List<ItemStackSnapshotCodec.DecodedSlot> inventory = ItemStackSnapshotCodec.decode(
                    snapshot.inventory(),
                    target.registryAccess(),
                    target.getInventory().getContainerSize());
            List<ItemStackSnapshotCodec.DecodedSlot> enderChest = snapshot.includesEnderChest()
                    ? ItemStackSnapshotCodec.decode(
                    snapshot.enderChest(),
                    target.registryAccess(),
                    target.getEnderChestInventory().getContainerSize())
                    : List.of();
            ActionResult<InventorySnapshot> backup = capture(
                    target,
                    "pre_restore",
                    snapshot.includesEnderChest(),
                    maximumSnapshots,
                    retentionSeconds);
            if (!backup.successful()) {
                return ActionResult.failure(backup.reason(), backup.detail());
            }
            ItemStackSnapshotCodec.apply(target.getInventory(), inventory);
            if (snapshot.includesEnderChest()) {
                ItemStackSnapshotCodec.apply(target.getEnderChestInventory(), enderChest);
            }
            target.containerMenu.broadcastChanges();
            return ActionResult.success(snapshot);
        } catch (IllegalArgumentException | ArithmeticException exception) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.INVALID_DEFINITION,
                    "inventory snapshot contains unavailable or invalid item data");
        }
    }

    public synchronized Optional<InventorySnapshot> find(UUID snapshotId) {
        prune(Instant.now());
        return snapshots.values().stream()
                .flatMap(Deque::stream)
                .filter(snapshot -> snapshot.id().equals(snapshotId))
                .findFirst();
    }

    public synchronized List<InventorySnapshot> snapshots(UUID playerId) {
        prune(Instant.now());
        Deque<InventorySnapshot> history = snapshots.get(playerId);
        if (history == null) {
            return List.of();
        }
        return history.stream()
                .sorted(Comparator.comparing(InventorySnapshot::createdAt).reversed())
                .toList();
    }

    public synchronized int count() {
        prune(Instant.now());
        return snapshots.values().stream().mapToInt(Deque::size).sum();
    }

    @Override
    public void flush() throws IOException {
        final SnapshotFile file;
        final StorageService.Document previous;
        final Path destination;
        final long snapshotRevision;
        synchronized (this) {
            prune(Instant.now());
            if (path == null || !dirty()) {
                return;
            }
            writable();
            List<PlayerSnapshots> players = snapshots.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(Comparator.comparing(UUID::toString)))
                    .map(entry -> new PlayerSnapshots(
                            entry.getKey(),
                            List.copyOf(entry.getValue())))
                    .toList();
            file = new SnapshotFile(Math.max(1L, revision), players);
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
                Set.of("/players"));
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

    private void prune(Instant now) {
        int before = countWithoutPrune();
        snapshots.values().forEach(history ->
                history.removeIf(snapshot -> !snapshot.expiresAt().isAfter(now)));
        snapshots.values().removeIf(Deque::isEmpty);
        if (before != countWithoutPrune()) {
            revision = Math.addExact(revision, 1L);
        }
    }

    private int countWithoutPrune() {
        return snapshots.values().stream().mapToInt(Deque::size).sum();
    }

    private int encodedCharacters() {
        int total = 0;
        for (Deque<InventorySnapshot> history : snapshots.values()) {
            for (InventorySnapshot snapshot : history) {
                total = Math.addExact(total, GSON.toJson(snapshot).length());
            }
        }
        return total;
    }

    private void writable() {
        if (state == RepositoryState.RECOVERY
                || state == RepositoryState.UNSUPPORTED
                || state == RepositoryState.ERROR
                || state == RepositoryState.CLOSED) {
            throw new IllegalStateException("inventory recovery storage is unavailable");
        }
    }

    private static void validate(InventorySnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        Objects.requireNonNull(snapshot.id(), "id");
        Objects.requireNonNull(snapshot.playerId(), "playerId");
        bounded(snapshot.reason(), 64);
        bounded(snapshot.dimensionId(), 256);
        if (!Double.isFinite(snapshot.x())
                || !Double.isFinite(snapshot.y())
                || !Double.isFinite(snapshot.z())
                || snapshot.inventory().size() > ItemStackSnapshotCodec.HARD_MAXIMUM_SLOTS
                || snapshot.enderChest().size() > ItemStackSnapshotCodec.HARD_MAXIMUM_SLOTS
                || !snapshot.includesEnderChest() && !snapshot.enderChest().isEmpty()
                || snapshot.revision() < 1L
                || !snapshot.expiresAt().isAfter(snapshot.createdAt())) {
            throw new IllegalArgumentException("inventory recovery record is invalid");
        }
        int encodedCharacters = GSON.toJson(snapshot.inventory()).length()
                + GSON.toJson(snapshot.enderChest()).length();
        if (encodedCharacters > ItemStackSnapshotCodec.HARD_MAXIMUM_ENCODED_CHARACTERS) {
            throw new IllegalArgumentException("inventory recovery record is too large");
        }
    }

    private static String bounded(String value, int maximum) {
        String result = Objects.requireNonNullElse(value, "").strip();
        if (result.isBlank()
                || result.length() > maximum
                || result.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("inventory recovery text is outside bounds");
        }
        return result;
    }

    public record InventorySnapshot(
            UUID id,
            UUID playerId,
            String reason,
            String dimensionId,
            double x,
            double y,
            double z,
            List<ItemStackSnapshotCodec.SlotStack> inventory,
            List<ItemStackSnapshotCodec.SlotStack> enderChest,
            boolean includesEnderChest,
            Instant createdAt,
            Instant expiresAt,
            long revision
    ) {
        public InventorySnapshot {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(playerId, "playerId");
            reason = bounded(reason, 64);
            dimensionId = bounded(dimensionId, 256);
            inventory = List.copyOf(Objects.requireNonNull(inventory, "inventory"));
            enderChest = List.copyOf(Objects.requireNonNull(enderChest, "enderChest"));
            Objects.requireNonNull(createdAt, "createdAt");
            Objects.requireNonNull(expiresAt, "expiresAt");
        }
    }

    private record PlayerSnapshots(UUID playerId, List<InventorySnapshot> snapshots) {
        private PlayerSnapshots {
            Objects.requireNonNull(playerId, "playerId");
            snapshots = List.copyOf(Objects.requireNonNull(snapshots, "snapshots"));
        }
    }

    private record SnapshotFile(long revision, List<PlayerSnapshots> players) {
        private SnapshotFile {
            players = List.copyOf(Objects.requireNonNull(players, "players"));
        }
    }
}
