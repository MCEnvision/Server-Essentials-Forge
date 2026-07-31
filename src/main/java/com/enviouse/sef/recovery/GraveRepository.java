package com.enviouse.sef.recovery;

import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class GraveRepository implements StorageRepository {
    public static final int SCHEMA_VERSION = 2;
    public static final int HARD_MAXIMUM_GRAVES = 10_000;
    public static final int HARD_MAXIMUM_CLAIM_TRANSACTIONS = 10_000;
    public static final int HARD_MAXIMUM_DOCUMENT_CHARACTERS = 12 * 1024 * 1024;
    private static final String CLAIM_MARKER_KEY = "sef_grave_claim_transaction";
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
    private static final System.Logger LOGGER = System.getLogger(GraveRepository.class.getName());

    private final Map<UUID, GraveRecord> graves = new LinkedHashMap<>();
    private final Map<UUID, ClaimTransaction> claimTransactions = new LinkedHashMap<>();
    private StorageService.Document document;
    private Path path;
    private RepositoryState state = RepositoryState.NEW;
    private long revision;
    private long flushedRevision;

    @Override
    public String id() {
        return "sef:graves";
    }

    @Override
    public String domain() {
        return "graves";
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
                .resolve("graves.json")
                .toAbsolutePath()
                .normalize();
        graves.clear();
        claimTransactions.clear();
        revision = 0L;
        flushedRevision = 0L;
        boolean existed = Files.exists(path, java.nio.file.LinkOption.NOFOLLOW_LINKS);
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(null);
        if (document == null) {
            state = existed ? RepositoryState.RECOVERY : RepositoryState.MISSING;
            return new LoadResult(state, existed ? "grave storage unavailable" : "new grave repository");
        }
        try {
            GraveFile file = GSON.fromJson(document.data(), GraveFile.class);
            if (file == null
                    || file.revision() < 1L
                    || file.graves().size() > HARD_MAXIMUM_GRAVES
                    || file.claimTransactions().size() > HARD_MAXIMUM_CLAIM_TRANSACTIONS
                    || document.data().toString().length() > HARD_MAXIMUM_DOCUMENT_CHARACTERS) {
                throw new IllegalStateException("grave storage is outside bounds");
            }
            for (GraveRecord grave : file.graves()) {
                validate(grave);
                if (graves.putIfAbsent(grave.id(), grave) != null) {
                    throw new IllegalStateException("duplicate grave record");
                }
            }
            for (ClaimTransaction transaction : file.claimTransactions()) {
                validate(transaction);
                if (claimTransactions.putIfAbsent(transaction.id(), transaction) != null) {
                    throw new IllegalStateException("duplicate grave claim transaction");
                }
            }
            revision = file.revision();
            flushedRevision = document.migrated() ? Math.max(0L, revision - 1L) : revision;
            state = RepositoryState.READY;
            return new LoadResult(state, "loaded " + graves.size() + " grave records");
        } catch (RuntimeException exception) {
            graves.clear();
            claimTransactions.clear();
            state = RepositoryState.RECOVERY;
            Throwable cause = exception.getCause() == null ? exception : exception.getCause();
            return new LoadResult(
                    state,
                    exception.getClass().getSimpleName() + ", "
                            + Objects.requireNonNullElse(cause.getMessage(), "unknown grave storage error"));
        }
    }

    public synchronized ActionResult<GraveRecord> captureAndFlush(
            ServerPlayer owner,
            Collection<ItemEntity> drops,
            long retentionSeconds,
            String container,
            boolean protectOwner,
            boolean keepExperience
    ) {
        writable();
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(drops, "drops");
        pruneHistory(Instant.now());
        if (drops.isEmpty()) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "death produced no item drops");
        }
        if (graves.size() >= HARD_MAXIMUM_GRAVES) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "grave capacity is full");
        }
        try {
            List<ItemStack> stacks = drops.stream()
                    .map(ItemEntity::getItem)
                    .filter(stack -> !stack.isEmpty())
                    .map(ItemStack::copy)
                    .toList();
            if (stacks.isEmpty()) {
                return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "death produced no item drops");
            }
            Instant now = Instant.now();
            PhysicalPlacement placement = placePhysicalContainer(owner, normalizeContainer(container));
            GraveRecord grave = new GraveRecord(
                    UUID.randomUUID(),
                    owner.getUUID(),
                    owner.level().dimension().location().toString(),
                    placement.position().getX(),
                    placement.position().getY(),
                    placement.position().getZ(),
                    ItemStackSnapshotCodec.captureStacks(stacks, owner.registryAccess()),
                    Math.clamp(retentionSeconds, 60L, 2_592_000L),
                    placement.container(),
                    protectOwner,
                    keepExperience,
                    0,
                    now,
                    now.plusSeconds(Math.clamp(retentionSeconds, 60L, 2_592_000L)),
                    false,
                    false,
                    1L);
            validate(grave);
            long previousRevision = revision;
            graves.put(grave.id(), grave);
            if (encodedCharacters() > HARD_MAXIMUM_DOCUMENT_CHARACTERS) {
                graves.remove(grave.id());
                placement.rollback(owner.serverLevel());
                return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "grave storage size limit reached");
            }
            revision = Math.addExact(revision, 1L);
            try {
                flush();
                return ActionResult.success(grave);
            } catch (IOException exception) {
                graves.remove(grave.id());
                revision = previousRevision;
                placement.rollback(owner.serverLevel());
                return ActionResult.failure(ActionResult.ReasonCode.STORAGE_ERROR, "grave could not be persisted");
            }
        } catch (IllegalArgumentException | ArithmeticException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "grave item data is outside bounds");
        }
    }

    public synchronized ActionResult<GraveRecord> storeExperienceAndFlush(
            UUID graveId,
            int experience
    ) {
        writable();
        GraveRecord current = graves.get(graveId);
        if (current == null || current.claimed() || !current.keepExperience()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "grave is unavailable");
        }
        GraveRecord updated = current.withExperience(Math.max(0, experience));
        graves.put(graveId, updated);
        revision = Math.addExact(revision, 1L);
        try {
            flush();
            return ActionResult.success(updated);
        } catch (IOException exception) {
            graves.put(graveId, current);
            revision = Math.addExact(revision, 1L);
            return ActionResult.failure(ActionResult.ReasonCode.STORAGE_ERROR, "grave experience could not be persisted");
        }
    }

    public synchronized ActionResult<GraveRecord> unlockAndFlush(
            UUID graveId,
            long expectedRevision
    ) {
        writable();
        GraveRecord current = active(graveId).orElse(null);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "grave is unavailable");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "grave revision changed");
        }
        GraveRecord updated = current.withUnlocked(true);
        graves.put(graveId, updated);
        revision = Math.addExact(revision, 1L);
        try {
            flush();
            return ActionResult.success(updated);
        } catch (IOException exception) {
            graves.put(graveId, current);
            revision = Math.addExact(revision, 1L);
            return ActionResult.failure(ActionResult.ReasonCode.STORAGE_ERROR, "grave unlock could not be persisted");
        }
    }

    public synchronized ActionResult<GraveRecord> claimAndFlush(
            ServerPlayer claimant,
            UUID graveId,
            long expectedRevision,
            boolean ownerOverride
    ) {
        writable();
        Objects.requireNonNull(claimant, "claimant");
        GraveRecord current = active(graveId).orElse(null);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "grave is unavailable");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "grave revision changed");
        }
        if (current.protectOwner()
                && !current.unlocked()
                && !current.ownerId().equals(claimant.getUUID())
                && !ownerOverride) {
            return ActionResult.failure(ActionResult.ReasonCode.PERMISSION_DENIED, "grave is owner protected");
        }
        if (claimTransactions.values().stream().anyMatch(transaction ->
                transaction.state() == ClaimState.PREPARED
                        && (transaction.graveId().equals(graveId)
                        || transaction.claimantId().equals(claimant.getUUID())))) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "grave claim recovery is pending");
        }
        try {
            List<ItemStack> items = ItemStackSnapshotCodec.decodeStacks(current.items(), claimant.registryAccess());
            if (!canFit(claimant, items)) {
                return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "inventory has insufficient space");
            }
            List<ItemStack> before = inventorySnapshot(claimant.getInventory());
            Instant now = Instant.now();
            ClaimTransaction prepared = new ClaimTransaction(
                    UUID.randomUUID(),
                    graveId,
                    claimant.getUUID(),
                    expectedRevision,
                    ClaimState.PREPARED,
                    now,
                    now,
                    1L);
            long beforePrepareRevision = revision;
            claimTransactions.put(prepared.id(), prepared);
            revision = Math.addExact(revision, 1L);
            try {
                flush();
            } catch (IOException exception) {
                claimTransactions.remove(prepared.id());
                revision = beforePrepareRevision;
                return ActionResult.failure(
                        ActionResult.ReasonCode.STORAGE_ERROR,
                        "grave claim could not be prepared");
            }
            for (ItemStack item : items) {
                ItemStack remaining = item.copy();
                if (!claimant.getInventory().add(remaining) || !remaining.isEmpty()) {
                    restoreInventory(claimant.getInventory(), before);
                    abortAndFlush(prepared);
                    return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "inventory changed during claim");
                }
            }
            if (current.experience() > 0) {
                claimant.giveExperiencePoints(current.experience());
            }
            claimant.getPersistentData().putString(CLAIM_MARKER_KEY, prepared.id().toString());
            claimant.getInventory().setChanged();
            claimant.containerMenu.broadcastChanges();
            claimant.server.getPlayerList().saveAll();

            GraveRecord claimed = current.withClaimed(true);
            ClaimTransaction committed = prepared.withState(ClaimState.COMMITTED, Instant.now());
            graves.put(graveId, claimed);
            claimTransactions.put(prepared.id(), committed);
            revision = Math.addExact(revision, 1L);
            try {
                flush();
            } catch (IOException exception) {
                LOGGER.log(
                        System.Logger.Level.WARNING,
                        "Grave claim {0} is awaiting journal recovery after final persistence failed",
                        prepared.id());
                return ActionResult.success(claimed);
            }
            clearMarkerAndSave(claimant);
            removePhysicalContainer(claimant.server, claimed);
            return ActionResult.success(claimed);
        } catch (IllegalArgumentException | ArithmeticException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, "grave item data is unavailable");
        }
    }

    public synchronized ActionResult<ClaimRecovery> reconcilePlayer(ServerPlayer player) {
        writable();
        Objects.requireNonNull(player, "player");
        CompoundTag persistentData = player.getPersistentData();
        String marker = persistentData.getString(CLAIM_MARKER_KEY);
        if (marker.isBlank()) {
            int aborted = 0;
            for (ClaimTransaction transaction : List.copyOf(claimTransactions.values())) {
                if (transaction.claimantId().equals(player.getUUID())
                        && transaction.state() == ClaimState.PREPARED) {
                    claimTransactions.put(
                            transaction.id(),
                            transaction.withState(ClaimState.ABORTED, Instant.now()));
                    revision = Math.addExact(revision, 1L);
                    aborted++;
                }
            }
            if (aborted > 0) {
                try {
                    flush();
                } catch (IOException exception) {
                    return ActionResult.failure(
                            ActionResult.ReasonCode.STORAGE_ERROR,
                            "unfinished grave claims could not be released");
                }
            }
            return ActionResult.success(new ClaimRecovery(aborted, 0));
        }

        UUID transactionId;
        try {
            transactionId = UUID.fromString(marker);
        } catch (IllegalArgumentException exception) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.INVALID_DEFINITION,
                    "grave claim recovery marker is invalid");
        }
        ClaimTransaction transaction = claimTransactions.get(transactionId);
        if (transaction == null || !transaction.claimantId().equals(player.getUUID())) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.INVALID_DEFINITION,
                    "grave claim recovery journal is unavailable");
        }
        GraveRecord grave = graves.get(transaction.graveId());
        if (grave == null) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.INVALID_DEFINITION,
                    "grave claim recovery record is unavailable");
        }
        if (transaction.state() == ClaimState.ABORTED && !grave.claimed()) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.CONFLICT,
                    "grave claim recovery conflicts with an aborted transaction");
        }
        if (!grave.claimed()) {
            graves.put(grave.id(), grave.withClaimed(true));
            revision = Math.addExact(revision, 1L);
        }
        if (transaction.state() != ClaimState.COMMITTED) {
            claimTransactions.put(
                    transaction.id(),
                    transaction.withState(ClaimState.COMMITTED, Instant.now()));
            revision = Math.addExact(revision, 1L);
        }
        try {
            flush();
        } catch (IOException exception) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.STORAGE_ERROR,
                    "grave claim recovery could not be committed");
        }
        clearMarkerAndSave(player);
        return ActionResult.success(new ClaimRecovery(0, 1));
    }

    public synchronized Optional<GraveRecord> find(UUID graveId) {
        pruneHistory(Instant.now());
        return Optional.ofNullable(graves.get(graveId));
    }

    public synchronized Optional<GraveRecord> physicalAt(ServerLevel level, BlockPos position) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(position, "position");
        String dimensionId = level.dimension().location().toString();
        return graves.values().stream()
                .filter(grave -> !"virtual".equals(grave.container()))
                .filter(grave -> grave.dimensionId().equals(dimensionId))
                .filter(grave -> BlockPos.containing(grave.x(), grave.y(), grave.z()).equals(position))
                .findFirst();
    }

    public synchronized int cleanupExpiredContainers(MinecraftServer server, int maximumChecks) {
        Objects.requireNonNull(server, "server");
        Instant now = Instant.now();
        int checked = 0;
        int removed = 0;
        for (GraveRecord grave : graves.values()) {
            if (checked >= Math.clamp(maximumChecks, 1, 512)) {
                break;
            }
            if ("virtual".equals(grave.container()) || !grave.claimed() && grave.expiresAt().isAfter(now)) {
                continue;
            }
            checked++;
            if (removePhysicalContainer(server, grave)) {
                removed++;
            }
        }
        return removed;
    }

    public synchronized Optional<GraveRecord> active(UUID graveId) {
        pruneHistory(Instant.now());
        GraveRecord grave = graves.get(graveId);
        return grave == null || grave.claimed() || !grave.expiresAt().isAfter(Instant.now())
                ? Optional.empty()
                : Optional.of(grave);
    }

    public synchronized Optional<GraveRecord> latestActive(UUID ownerId) {
        pruneHistory(Instant.now());
        return graves.values().stream()
                .filter(grave -> grave.ownerId().equals(ownerId))
                .filter(grave -> !grave.claimed() && grave.expiresAt().isAfter(Instant.now()))
                .max(Comparator.comparing(GraveRecord::createdAt));
    }

    public synchronized List<GraveRecord> graves(UUID ownerId, boolean includeHistory) {
        Instant now = Instant.now();
        pruneHistory(now);
        return graves.values().stream()
                .filter(grave -> grave.ownerId().equals(ownerId))
                .filter(grave -> includeHistory || !grave.claimed() && grave.expiresAt().isAfter(now))
                .sorted(Comparator.comparing(GraveRecord::createdAt).reversed())
                .toList();
    }

    @Override
    public void flush() throws IOException {
        final GraveFile file;
        final StorageService.Document previous;
        final Path destination;
        final long snapshotRevision;
        synchronized (this) {
            pruneHistory(Instant.now());
            if (path == null || !dirty()) {
                return;
            }
            writable();
            file = new GraveFile(
                    Math.max(1L, revision),
                    graves.values().stream()
                            .sorted(Comparator.comparing(GraveRecord::createdAt))
                            .toList(),
                    claimTransactions.values().stream()
                            .sorted(Comparator.comparing(ClaimTransaction::createdAt))
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

    private void writable() {
        if (state == RepositoryState.RECOVERY
                || state == RepositoryState.UNSUPPORTED
                || state == RepositoryState.ERROR
                || state == RepositoryState.CLOSED) {
            throw new IllegalStateException("grave storage is unavailable");
        }
    }

    private void pruneHistory(Instant now) {
        int beforeGraves = graves.size();
        int beforeTransactions = claimTransactions.size();
        graves.values().removeIf(grave ->
                grave.expiresAt().plusSeconds(2_592_000L).isBefore(now));
        claimTransactions.values().removeIf(transaction ->
                transaction.state() != ClaimState.PREPARED
                        && transaction.updatedAt().plusSeconds(2_592_000L).isBefore(now));
        if (beforeGraves != graves.size() || beforeTransactions != claimTransactions.size()) {
            revision = Math.addExact(revision, 1L);
        }
    }

    private int encodedCharacters() {
        int total = 0;
        for (GraveRecord grave : graves.values()) {
            total = Math.addExact(total, GSON.toJson(grave).length());
        }
        for (ClaimTransaction transaction : claimTransactions.values()) {
            total = Math.addExact(total, GSON.toJson(transaction).length());
        }
        return total;
    }

    private void abortAndFlush(ClaimTransaction transaction) {
        claimTransactions.put(
                transaction.id(),
                transaction.withState(ClaimState.ABORTED, Instant.now()));
        revision = Math.addExact(revision, 1L);
        try {
            flush();
        } catch (IOException exception) {
            LOGGER.log(
                    System.Logger.Level.WARNING,
                    "Grave claim {0} could not persist its abort state",
                    transaction.id());
        }
    }

    private static void clearMarkerAndSave(ServerPlayer player) {
        player.getPersistentData().remove(CLAIM_MARKER_KEY);
        player.server.getPlayerList().saveAll();
    }

    private static PhysicalPlacement placePhysicalContainer(ServerPlayer owner, String requestedContainer) {
        BlockPos fallback = owner.blockPosition();
        if ("virtual".equals(requestedContainer)) {
            return PhysicalPlacement.virtual(fallback);
        }
        ServerLevel level = owner.serverLevel();
        Block block = "barrel".equals(requestedContainer) ? Blocks.BARREL : Blocks.CHEST;
        for (int vertical = 0; vertical <= 2; vertical++) {
            for (int radius = 0; radius <= 2; radius++) {
                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (Math.max(Math.abs(x), Math.abs(z)) != radius) {
                            continue;
                        }
                        BlockPos position = fallback.offset(x, vertical, z);
                        if (!level.hasChunkAt(position)) {
                            continue;
                        }
                        BlockState previous = level.getBlockState(position);
                        BlockPos support = position.below();
                        if (!previous.canBeReplaced()
                                || !level.getBlockState(support).isFaceSturdy(level, support, Direction.UP)) {
                            continue;
                        }
                        if (level.setBlock(position, block.defaultBlockState(), Block.UPDATE_ALL)) {
                            return new PhysicalPlacement(
                                    requestedContainer,
                                    position.immutable(),
                                    previous,
                                    true);
                        }
                    }
                }
            }
        }
        return PhysicalPlacement.virtual(fallback);
    }

    private static boolean removePhysicalContainer(MinecraftServer server, GraveRecord grave) {
        if ("virtual".equals(grave.container())) {
            return false;
        }
        ResourceLocation location = ResourceLocation.tryParse(grave.dimensionId());
        if (location == null) {
            return false;
        }
        ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, location));
        if (level == null) {
            return false;
        }
        BlockPos position = BlockPos.containing(grave.x(), grave.y(), grave.z());
        Block expected = "barrel".equals(grave.container()) ? Blocks.BARREL : Blocks.CHEST;
        return level.hasChunkAt(position)
                && level.getBlockState(position).is(expected)
                && level.removeBlock(position, false);
    }

    private static boolean canFit(ServerPlayer player, List<ItemStack> items) {
        Inventory simulated = new Inventory(player);
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            simulated.setItem(slot, player.getInventory().getItem(slot).copy());
        }
        for (ItemStack item : items) {
            ItemStack candidate = item.copy();
            if (!simulated.add(candidate) || !candidate.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static List<ItemStack> inventorySnapshot(Inventory inventory) {
        List<ItemStack> result = new ArrayList<>(inventory.getContainerSize());
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            result.add(inventory.getItem(slot).copy());
        }
        return result;
    }

    private static void restoreInventory(Inventory inventory, List<ItemStack> snapshot) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            inventory.setItem(slot, snapshot.get(slot).copy());
        }
        inventory.setChanged();
    }

    private static void validate(GraveRecord grave) {
        Objects.requireNonNull(grave, "grave");
        if (!Double.isFinite(grave.x())
                || !Double.isFinite(grave.y())
                || !Double.isFinite(grave.z())
                || grave.items().isEmpty()
                || grave.items().size() > ItemStackSnapshotCodec.HARD_MAXIMUM_SLOTS
                || grave.retentionSeconds() < 60L
                || grave.retentionSeconds() > 2_592_000L
                || grave.experience() < 0
                || grave.revision() < 1L
                || !grave.expiresAt().isAfter(grave.createdAt())) {
            throw new IllegalArgumentException("grave record is invalid");
        }
        bounded(grave.dimensionId(), 256);
        normalizeContainer(grave.container());
        if (GSON.toJson(grave.items()).length() > ItemStackSnapshotCodec.HARD_MAXIMUM_ENCODED_CHARACTERS) {
            throw new IllegalArgumentException("grave record is too large");
        }
    }

    private static void validate(ClaimTransaction transaction) {
        Objects.requireNonNull(transaction, "transaction");
        Objects.requireNonNull(transaction.id(), "id");
        Objects.requireNonNull(transaction.graveId(), "graveId");
        Objects.requireNonNull(transaction.claimantId(), "claimantId");
        Objects.requireNonNull(transaction.state(), "state");
        Objects.requireNonNull(transaction.createdAt(), "createdAt");
        Objects.requireNonNull(transaction.updatedAt(), "updatedAt");
        if (transaction.expectedGraveRevision() < 1L
                || transaction.revision() < 1L
                || transaction.updatedAt().isBefore(transaction.createdAt())) {
            throw new IllegalArgumentException("grave claim transaction is invalid");
        }
    }

    private static String normalizeContainer(String value) {
        String result = Objects.requireNonNullElse(value, "virtual").strip().toLowerCase(java.util.Locale.ROOT);
        if (!Set.of("chest", "barrel", "virtual").contains(result)) {
            throw new IllegalArgumentException("grave container is invalid");
        }
        return result;
    }

    private static String bounded(String value, int maximum) {
        String result = Objects.requireNonNullElse(value, "").strip();
        if (result.isBlank()
                || result.length() > maximum
                || result.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("grave text is outside bounds");
        }
        return result;
    }

    public record GraveRecord(
            UUID id,
            UUID ownerId,
            String dimensionId,
            double x,
            double y,
            double z,
            List<ItemStackSnapshotCodec.SlotStack> items,
            long retentionSeconds,
            String container,
            boolean protectOwner,
            boolean keepExperience,
            int experience,
            Instant createdAt,
            Instant expiresAt,
            boolean unlocked,
            boolean claimed,
            long revision
    ) {
        public GraveRecord {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(ownerId, "ownerId");
            dimensionId = bounded(dimensionId, 256);
            items = List.copyOf(Objects.requireNonNull(items, "items"));
            container = normalizeContainer(container);
            Objects.requireNonNull(createdAt, "createdAt");
            Objects.requireNonNull(expiresAt, "expiresAt");
        }

        private GraveRecord withExperience(int value) {
            return new GraveRecord(
                    id, ownerId, dimensionId, x, y, z, items, retentionSeconds, container,
                    protectOwner, keepExperience, value, createdAt, expiresAt, unlocked, claimed,
                    Math.addExact(revision, 1L));
        }

        private GraveRecord withUnlocked(boolean value) {
            return new GraveRecord(
                    id, ownerId, dimensionId, x, y, z, items, retentionSeconds, container,
                    protectOwner, keepExperience, experience, createdAt, expiresAt, value, claimed,
                    Math.addExact(revision, 1L));
        }

        private GraveRecord withClaimed(boolean value) {
            return new GraveRecord(
                    id, ownerId, dimensionId, x, y, z, items, retentionSeconds, container,
                    protectOwner, keepExperience, experience, createdAt, expiresAt, unlocked, value,
                    Math.addExact(revision, 1L));
        }
    }

    public enum ClaimState {
        PREPARED,
        COMMITTED,
        ABORTED
    }

    public record ClaimTransaction(
            UUID id,
            UUID graveId,
            UUID claimantId,
            long expectedGraveRevision,
            ClaimState state,
            Instant createdAt,
            Instant updatedAt,
            long revision
    ) {
        public ClaimTransaction {
            validateFields(id, graveId, claimantId, state, createdAt, updatedAt, expectedGraveRevision, revision);
        }

        private ClaimTransaction withState(ClaimState value, Instant now) {
            return new ClaimTransaction(
                    id,
                    graveId,
                    claimantId,
                    expectedGraveRevision,
                    value,
                    createdAt,
                    now,
                    Math.addExact(revision, 1L));
        }

        private static void validateFields(
                UUID id,
                UUID graveId,
                UUID claimantId,
                ClaimState state,
                Instant createdAt,
                Instant updatedAt,
                long expectedGraveRevision,
                long revision
        ) {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(graveId, "graveId");
            Objects.requireNonNull(claimantId, "claimantId");
            Objects.requireNonNull(state, "state");
            Objects.requireNonNull(createdAt, "createdAt");
            Objects.requireNonNull(updatedAt, "updatedAt");
            if (expectedGraveRevision < 1L || revision < 1L || updatedAt.isBefore(createdAt)) {
                throw new IllegalArgumentException("grave claim transaction is invalid");
            }
        }
    }

    public record ClaimRecovery(int abortedTransactions, int completedTransactions) {
        public ClaimRecovery {
            if (abortedTransactions < 0 || completedTransactions < 0) {
                throw new IllegalArgumentException("grave claim recovery counts are invalid");
            }
        }
    }

    private record PhysicalPlacement(
            String container,
            BlockPos position,
            BlockState previousState,
            boolean placed
    ) {
        private PhysicalPlacement {
            container = normalizeContainer(container);
            Objects.requireNonNull(position, "position");
            Objects.requireNonNull(previousState, "previousState");
        }

        private static PhysicalPlacement virtual(BlockPos position) {
            return new PhysicalPlacement("virtual", position.immutable(), Blocks.AIR.defaultBlockState(), false);
        }

        private void rollback(ServerLevel level) {
            if (placed
                    && level.hasChunkAt(position)
                    && (level.getBlockState(position).is(Blocks.CHEST)
                    || level.getBlockState(position).is(Blocks.BARREL))) {
                level.setBlock(position, previousState, Block.UPDATE_ALL);
            }
        }
    }

    private record GraveFile(
            long revision,
            List<GraveRecord> graves,
            List<ClaimTransaction> claimTransactions
    ) {
        private GraveFile {
            graves = List.copyOf(graves == null ? List.of() : graves);
            claimTransactions = List.copyOf(claimTransactions == null ? List.of() : claimTransactions);
        }
    }
}
