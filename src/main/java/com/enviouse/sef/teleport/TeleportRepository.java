package com.enviouse.sef.teleport;

import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class TeleportRepository implements StorageRepository {
    private static final String DOMAIN = "teleport essentials";
    private static final int SCHEMA_VERSION = 1;
    private static final int MAXIMUM_HOMES = 1_000_000;
    private static final int MAXIMUM_WARPS = 1_000_000;
    private static final int MAXIMUM_PREFERENCES = 1_000_000;
    private static final int MAXIMUM_REPORTS = 100_000;

    private final Map<UUID, HomeRecord> homesById = new LinkedHashMap<>();
    private final Map<OwnerName, UUID> homeNames = new HashMap<>();
    private final Map<UUID, WarpRecord> warpsById = new LinkedHashMap<>();
    private final Map<String, UUID> serverWarpNames = new HashMap<>();
    private final Map<OwnerName, UUID> playerWarpNames = new HashMap<>();
    private final Map<String, SpawnRecord> spawns = new LinkedHashMap<>();
    private final Map<UUID, TeleportPreference> preferences = new LinkedHashMap<>();
    private final Map<UUID, PendingOfflineTeleport> offlineTeleports = new LinkedHashMap<>();
    private final Map<UUID, TransferOffer> transferOffers = new LinkedHashMap<>();
    private final Map<UUID, WarpReport> reports = new LinkedHashMap<>();
    private final Set<String> completedImports = new LinkedHashSet<>();

    private Path path;
    private StorageService.Document document;
    private RepositoryState state = RepositoryState.MISSING;
    private long revision;
    private long flushedRevision;

    @Override
    public String id() {
        return "sef:teleports";
    }

    @Override
    public String domain() {
        return DOMAIN;
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
        path = managedRoot.resolve("teleports.json").toAbsolutePath().normalize();
        clearCollections();
        document = StorageService.read(path, DOMAIN, SCHEMA_VERSION).orElse(null);
        if (document == null) {
            state = stateFromStorageStatus(path);
            return new LoadResult(state, state == RepositoryState.MISSING ? "new repository" : "storage unavailable");
        }
        if (!document.data().isJsonObject()) {
            state = RepositoryState.RECOVERY;
            return new LoadResult(state, "teleport data is not an object");
        }
        try {
            JsonObject data = document.data().getAsJsonObject();
            loadHomes(requiredArray(data, "homes"));
            loadWarps(requiredArray(data, "warps"));
            loadSpawns(requiredArray(data, "spawns"));
            loadPreferences(requiredArray(data, "preferences"));
            loadOfflineTeleports(requiredArray(data, "offlineTeleports"));
            loadTransferOffers(requiredArray(data, "transferOffers"));
            loadReports(requiredArray(data, "reports"));
            loadCompletedImports(requiredArray(data, "completedImports"));
            state = RepositoryState.READY;
            if (document.migrated()) {
                revision++;
            }
            return new LoadResult(
                    state,
                    "loaded " + homesById.size() + " homes and " + warpsById.size() + " warps");
        } catch (RuntimeException exception) {
            clearCollections();
            state = RepositoryState.RECOVERY;
            return new LoadResult(state, exception.getClass().getSimpleName());
        }
    }

    public synchronized Optional<HomeRecord> home(UUID ownerId, String name) {
        UUID id = homeNames.get(new OwnerName(ownerId, HomeRecord.normalizeName(name)));
        HomeRecord record = id == null ? null : homesById.get(id);
        return record != null && record.active() ? Optional.of(record) : Optional.empty();
    }

    public synchronized Optional<HomeRecord> homeById(UUID id) {
        return Optional.ofNullable(homesById.get(id));
    }

    public synchronized List<HomeRecord> homes(UUID ownerId) {
        return homesById.values().stream()
                .filter(HomeRecord::active)
                .filter(record -> record.ownerId().equals(ownerId))
                .sorted(Comparator.comparing(HomeRecord::normalizedName))
                .toList();
    }

    public synchronized List<HomeRecord> allHomes(boolean includeDeleted) {
        return homesById.values().stream()
                .filter(record -> includeDeleted || record.active())
                .sorted(Comparator.comparing(HomeRecord::ownerId)
                        .thenComparing(HomeRecord::normalizedName))
                .toList();
    }

    public synchronized ActionResult<HomeRecord> setHome(
            UUID ownerId,
            String displayName,
            SavedLocation location,
            long maximumHomes,
            long maximumHomesInDimension,
            boolean overwrite
    ) {
        ensureWritable();
        String normalized = HomeRecord.normalizeName(displayName);
        OwnerName key = new OwnerName(ownerId, normalized);
        UUID existingId = homeNames.get(key);
        HomeRecord existing = existingId == null ? null : homesById.get(existingId);
        Instant now = Instant.now();
        if (existing != null && existing.active()) {
            if (!overwrite) {
                return ActionResult.failure(ActionResult.ReasonCode.CONFIRMATION_REQUIRED, existing.id().toString());
            }
            if (!existing.location().dimensionId().equals(location.dimensionId())) {
                long inDestinationDimension = homes(ownerId).stream()
                        .filter(home -> home.location().dimensionId().equals(location.dimensionId()))
                        .count();
                if (maximumHomesInDimension < 0 || inDestinationDimension >= maximumHomesInDimension) {
                    return ActionResult.failure(
                            ActionResult.ReasonCode.QUOTA_EXCEEDED,
                            "dimension:" + maximumHomesInDimension);
                }
            }
            HomeRecord replacement = existing.relocated(location, now);
            homesById.put(replacement.id(), replacement);
            changed();
            return ActionResult.success(replacement);
        }
        long active = homes(ownerId).size();
        if (maximumHomes < 0 || active >= maximumHomes) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, Long.toString(maximumHomes));
        }
        long inDimension = homes(ownerId).stream()
                .filter(home -> home.location().dimensionId().equals(location.dimensionId()))
                .count();
        if (maximumHomesInDimension < 0 || inDimension >= maximumHomesInDimension) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.QUOTA_EXCEEDED,
                    "dimension:" + maximumHomesInDimension);
        }
        HomeRecord created = new HomeRecord(
                UUID.randomUUID(),
                ownerId,
                normalized,
                displayName,
                location,
                now,
                now,
                "",
                "",
                HomeRecord.Visibility.PRIVATE,
                "",
                1,
                1,
                null);
        homesById.put(created.id(), created);
        homeNames.put(key, created.id());
        changed();
        return ActionResult.success(created);
    }

    public synchronized ActionResult<HomeRecord> deleteHome(UUID ownerId, String name) {
        ensureWritable();
        Optional<HomeRecord> found = home(ownerId, name);
        if (found.isEmpty()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "home not found");
        }
        HomeRecord deleted = found.get().deleted(Instant.now());
        homesById.put(deleted.id(), deleted);
        homeNames.remove(new OwnerName(ownerId, deleted.normalizedName()));
        changed();
        return ActionResult.success(deleted);
    }

    public synchronized ActionResult<HomeRecord> restoreHome(
            UUID id,
            long maximumHomes,
            long maximumHomesInDimension
    ) {
        ensureWritable();
        HomeRecord record = homesById.get(id);
        if (record == null || record.active()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "deleted home not found");
        }
        OwnerName key = new OwnerName(record.ownerId(), record.normalizedName());
        if (homeNames.containsKey(key)) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "home name is already in use");
        }
        if (homes(record.ownerId()).size() >= maximumHomes) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, Long.toString(maximumHomes));
        }
        long inDimension = homes(record.ownerId()).stream()
                .filter(home -> home.location().dimensionId().equals(record.location().dimensionId()))
                .count();
        if (inDimension >= maximumHomesInDimension) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.QUOTA_EXCEEDED,
                    "dimension:" + maximumHomesInDimension);
        }
        HomeRecord restored = record.restored(Instant.now());
        homesById.put(id, restored);
        homeNames.put(key, id);
        changed();
        return ActionResult.success(restored);
    }

    public synchronized ActionResult<HomeRecord> renameHome(UUID ownerId, String currentName, String newName) {
        ensureWritable();
        Optional<HomeRecord> found = home(ownerId, currentName);
        if (found.isEmpty()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "home not found");
        }
        String normalized = HomeRecord.normalizeName(newName);
        OwnerName replacementKey = new OwnerName(ownerId, normalized);
        if (homeNames.containsKey(replacementKey)) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "home name is already in use");
        }
        HomeRecord replacement = found.get().renamed(newName, Instant.now());
        homeNames.remove(new OwnerName(ownerId, found.get().normalizedName()));
        homeNames.put(replacementKey, replacement.id());
        homesById.put(replacement.id(), replacement);
        changed();
        return ActionResult.success(replacement);
    }

    public synchronized Optional<WarpRecord> serverWarp(String name) {
        UUID id = serverWarpNames.get(WarpRecord.normalizeName(name));
        WarpRecord record = id == null ? null : warpsById.get(id);
        return record != null && record.active() ? Optional.of(record) : Optional.empty();
    }

    public synchronized Optional<WarpRecord> playerWarp(UUID ownerId, String name) {
        UUID id = playerWarpNames.get(new OwnerName(ownerId, WarpRecord.normalizeName(name)));
        WarpRecord record = id == null ? null : warpsById.get(id);
        return record != null && record.active() ? Optional.of(record) : Optional.empty();
    }

    public synchronized Optional<WarpRecord> warpById(UUID id) {
        return Optional.ofNullable(warpsById.get(id));
    }

    public synchronized List<WarpRecord> serverWarps(boolean includeHidden) {
        return warpsById.values().stream()
                .filter(WarpRecord::active)
                .filter(record -> record.scope() == WarpRecord.Scope.SERVER_PUBLIC)
                .filter(record -> includeHidden || !record.hidden())
                .sorted(Comparator.comparing(WarpRecord::normalizedName))
                .toList();
    }

    public synchronized List<WarpRecord> playerWarps(UUID ownerId, boolean includeDeleted) {
        return warpsById.values().stream()
                .filter(record -> record.scope() == WarpRecord.Scope.PLAYER)
                .filter(record -> record.ownerId().equals(ownerId))
                .filter(record -> includeDeleted || record.active())
                .sorted(Comparator.comparing(WarpRecord::normalizedName))
                .toList();
    }

    public synchronized List<WarpRecord> visiblePlayerWarps(UUID visitorId, boolean moderator) {
        return warpsById.values().stream()
                .filter(record -> record.scope() == WarpRecord.Scope.PLAYER)
                .filter(record -> record.canVisit(visitorId, moderator))
                .filter(record -> !record.hidden() || moderator || Objects.equals(record.ownerId(), visitorId))
                .filter(record -> record.listed() || moderator || Objects.equals(record.ownerId(), visitorId))
                .sorted(Comparator.comparing(WarpRecord::ownerNameSnapshot)
                        .thenComparing(WarpRecord::normalizedName))
                .toList();
    }

    public synchronized List<WarpRecord> allWarps(boolean includeDeleted) {
        return warpsById.values().stream()
                .filter(record -> includeDeleted || record.active())
                .sorted(Comparator.comparing(WarpRecord::scope)
                        .thenComparing(record -> record.ownerId() == null ? new UUID(0, 0) : record.ownerId())
                        .thenComparing(WarpRecord::normalizedName))
                .toList();
    }

    public synchronized ActionResult<WarpRecord> setServerWarp(
            String displayName,
            SavedLocation location,
            boolean overwrite
    ) {
        ensureWritable();
        String normalized = WarpRecord.normalizeName(displayName);
        Optional<WarpRecord> existing = serverWarp(normalized);
        if (existing.isPresent()) {
            if (!overwrite) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.CONFIRMATION_REQUIRED,
                        existing.get().id().toString());
            }
            WarpRecord replacement = existing.get().relocated(location, Instant.now());
            warpsById.put(replacement.id(), replacement);
            changed();
            return ActionResult.success(replacement);
        }
        Instant now = Instant.now();
        WarpRecord created = new WarpRecord(
                UUID.randomUUID(),
                null,
                "",
                normalized,
                displayName,
                WarpRecord.Scope.SERVER_PUBLIC,
                WarpRecord.Access.PUBLIC,
                WarpRecord.Status.ACTIVE,
                location,
                now,
                now,
                now,
                null,
                "",
                "",
                "",
                "",
                false,
                true,
                false,
                0,
                1,
                1,
                null,
                Set.of(),
                Set.of());
        warpsById.put(created.id(), created);
        serverWarpNames.put(normalized, created.id());
        changed();
        return ActionResult.success(created);
    }

    public synchronized ActionResult<WarpRecord> createPlayerWarp(
            UUID ownerId,
            String ownerName,
            String displayName,
            SavedLocation location,
            long maximumWarps,
            UUID sourceHomeId
    ) {
        ensureWritable();
        String normalized = WarpRecord.normalizeName(displayName);
        OwnerName key = new OwnerName(ownerId, normalized);
        if (playerWarpNames.containsKey(key)) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "player warp name is already in use");
        }
        long active = playerWarps(ownerId, false).size();
        if (maximumWarps < 0 || active >= maximumWarps) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, Long.toString(maximumWarps));
        }
        Instant now = Instant.now();
        WarpRecord created = new WarpRecord(
                UUID.randomUUID(),
                ownerId,
                ownerName,
                normalized,
                displayName,
                WarpRecord.Scope.PLAYER,
                WarpRecord.Access.PRIVATE,
                WarpRecord.Status.DRAFT,
                location,
                now,
                now,
                null,
                null,
                "",
                "",
                "",
                "",
                false,
                false,
                false,
                0,
                1,
                1,
                sourceHomeId,
                Set.of(),
                Set.of());
        warpsById.put(created.id(), created);
        playerWarpNames.put(key, created.id());
        changed();
        return ActionResult.success(created);
    }

    public synchronized ActionResult<WarpRecord> deleteWarp(UUID id) {
        ensureWritable();
        WarpRecord record = warpsById.get(id);
        if (record == null || !record.active()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "warp not found");
        }
        WarpRecord deleted = record.status(WarpRecord.Status.DELETED, Instant.now());
        warpsById.put(id, deleted);
        removeWarpName(record);
        transferOffers.remove(id);
        changed();
        return ActionResult.success(deleted);
    }

    public synchronized ActionResult<WarpRecord> restoreWarp(UUID id, long maximumOwnedWarps) {
        ensureWritable();
        WarpRecord record = warpsById.get(id);
        if (record == null || record.status() != WarpRecord.Status.DELETED) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "deleted warp not found");
        }
        if (record.scope() == WarpRecord.Scope.SERVER_PUBLIC) {
            if (serverWarpNames.containsKey(record.normalizedName())) {
                return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "warp name is already in use");
            }
        } else {
            OwnerName key = new OwnerName(record.ownerId(), record.normalizedName());
            if (playerWarpNames.containsKey(key)) {
                return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "warp name is already in use");
            }
            if (playerWarps(record.ownerId(), false).size() >= maximumOwnedWarps) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.QUOTA_EXCEEDED,
                        Long.toString(maximumOwnedWarps));
            }
        }
        WarpRecord restored = record.status(
                record.scope() == WarpRecord.Scope.SERVER_PUBLIC
                        ? WarpRecord.Status.ACTIVE
                        : WarpRecord.Status.DRAFT,
                Instant.now());
        restored = new WarpRecord(
                restored.id(),
                restored.ownerId(),
                restored.ownerNameSnapshot(),
                restored.normalizedName(),
                restored.displayName(),
                restored.scope(),
                restored.access(),
                restored.status(),
                restored.location(),
                restored.createdAt(),
                restored.updatedAt(),
                restored.publishedAt(),
                null,
                restored.permission(),
                restored.icon(),
                restored.description(),
                restored.category(),
                restored.hidden(),
                restored.listed(),
                restored.featured(),
                restored.visits(),
                restored.safetyRevision(),
                restored.revision(),
                restored.sourceHomeId(),
                restored.trustedPlayers(),
                restored.blockedPlayers());
        warpsById.put(id, restored);
        indexWarpName(restored);
        changed();
        return ActionResult.success(restored);
    }

    public synchronized ActionResult<WarpRecord> renameWarp(UUID id, String newName) {
        ensureWritable();
        WarpRecord record = warpsById.get(id);
        if (record == null || !record.active()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "warp not found");
        }
        String normalized = WarpRecord.normalizeName(newName);
        boolean conflict = record.scope() == WarpRecord.Scope.SERVER_PUBLIC
                ? serverWarpNames.containsKey(normalized)
                : playerWarpNames.containsKey(new OwnerName(record.ownerId(), normalized));
        if (conflict) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "warp name is already in use");
        }
        removeWarpName(record);
        WarpRecord replacement = record.renamed(newName, Instant.now());
        warpsById.put(id, replacement);
        indexWarpName(replacement);
        changed();
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<WarpRecord> relocateWarp(UUID id, SavedLocation location) {
        ensureWritable();
        WarpRecord record = warpsById.get(id);
        if (record == null || !record.active()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "warp not found");
        }
        WarpRecord replacement = record.relocated(location, Instant.now());
        warpsById.put(id, replacement);
        changed();
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<WarpRecord> setWarpStatus(UUID id, WarpRecord.Status status) {
        ensureWritable();
        WarpRecord record = warpsById.get(id);
        if (record == null || !record.active()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "warp not found");
        }
        WarpRecord replacement = record.status(status, Instant.now());
        warpsById.put(id, replacement);
        if (status == WarpRecord.Status.DELETED) {
            removeWarpName(record);
        }
        changed();
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<WarpRecord> setWarpFlags(
            UUID id,
            boolean hidden,
            boolean listed,
            boolean featured
    ) {
        ensureWritable();
        WarpRecord record = warpsById.get(id);
        if (record == null || !record.active()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "warp not found");
        }
        WarpRecord replacement = record.flags(hidden, listed, featured, Instant.now());
        warpsById.put(id, replacement);
        changed();
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<WarpRecord> publishWarp(UUID id, WarpRecord.Access access) {
        ensureWritable();
        WarpRecord record = warpsById.get(id);
        if (record == null || !record.active() || record.scope() != WarpRecord.Scope.PLAYER) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "player warp not found");
        }
        WarpRecord replacement = record.publication(access, Instant.now());
        warpsById.put(id, replacement);
        changed();
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<WarpRecord> trustWarp(UUID id, UUID playerId, boolean trusted) {
        ensureWritable();
        WarpRecord record = warpsById.get(id);
        if (record == null || !record.active() || record.scope() != WarpRecord.Scope.PLAYER) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "player warp not found");
        }
        Set<UUID> trust = new HashSet<>(record.trustedPlayers());
        Set<UUID> block = new HashSet<>(record.blockedPlayers());
        if (trusted) {
            if (trust.size() >= 1000) {
                return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "trusted player limit");
            }
            block.remove(playerId);
            trust.add(playerId);
        } else {
            trust.remove(playerId);
        }
        WarpRecord replacement = record.accessLists(trust, block, Instant.now());
        warpsById.put(id, replacement);
        changed();
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<WarpRecord> blockWarp(UUID id, UUID playerId, boolean blocked) {
        ensureWritable();
        WarpRecord record = warpsById.get(id);
        if (record == null || !record.active() || record.scope() != WarpRecord.Scope.PLAYER) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "player warp not found");
        }
        Set<UUID> trust = new HashSet<>(record.trustedPlayers());
        Set<UUID> block = new HashSet<>(record.blockedPlayers());
        if (blocked) {
            if (block.size() >= 1000) {
                return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "blocked player limit");
            }
            trust.remove(playerId);
            block.add(playerId);
        } else {
            block.remove(playerId);
        }
        WarpRecord replacement = record.accessLists(trust, block, Instant.now());
        warpsById.put(id, replacement);
        changed();
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<WarpRecord> recordVisit(UUID id, long expectedRevision) {
        ensureWritable();
        WarpRecord record = warpsById.get(id);
        if (record == null || !record.active()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "warp not found");
        }
        if (record.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "warp changed during teleport");
        }
        WarpRecord replacement = record.visited(Instant.now());
        warpsById.put(id, replacement);
        changed();
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<TransferOffer> offerTransfer(
            UUID warpId,
            UUID currentOwner,
            UUID proposedOwner,
            Instant expiresAt
    ) {
        ensureWritable();
        WarpRecord record = warpsById.get(warpId);
        if (record == null || !record.active() || !Objects.equals(record.ownerId(), currentOwner)) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "owned player warp not found");
        }
        if (currentOwner.equals(proposedOwner) || !expiresAt.isAfter(Instant.now())) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "invalid transfer target or expiry");
        }
        TransferOffer offer = new TransferOffer(
                UUID.randomUUID(),
                warpId,
                currentOwner,
                proposedOwner,
                record.revision(),
                Instant.now(),
                expiresAt);
        transferOffers.put(warpId, offer);
        changed();
        return ActionResult.success(offer);
    }

    public synchronized ActionResult<WarpRecord> acceptTransfer(
            UUID warpId,
            UUID proposedOwner,
            String ownerName,
            long maximumOwnedWarps
    ) {
        ensureWritable();
        TransferOffer offer = transferOffers.get(warpId);
        WarpRecord record = warpsById.get(warpId);
        if (offer == null || record == null || !record.active()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "transfer offer not found");
        }
        if (!offer.proposedOwnerId().equals(proposedOwner)
                || offer.expiresAt().isBefore(Instant.now())
                || offer.warpRevision() != record.revision()) {
            transferOffers.remove(warpId);
            changed();
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "transfer offer is stale");
        }
        OwnerName newKey = new OwnerName(proposedOwner, record.normalizedName());
        if (playerWarpNames.containsKey(newKey)) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "recipient already has that warp name");
        }
        if (playerWarps(proposedOwner, false).size() >= maximumOwnedWarps) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, Long.toString(maximumOwnedWarps));
        }
        removeWarpName(record);
        WarpRecord replacement = record.transferred(proposedOwner, ownerName, Instant.now());
        warpsById.put(warpId, replacement);
        indexWarpName(replacement);
        transferOffers.remove(warpId);
        changed();
        return ActionResult.success(replacement);
    }

    public synchronized List<TransferOffer> transferOffersFor(UUID proposedOwnerId) {
        purgeExpiredReadOnly(Instant.now());
        return transferOffers.values().stream()
                .filter(offer -> offer.proposedOwnerId().equals(proposedOwnerId))
                .sorted(Comparator.comparing(TransferOffer::createdAt))
                .toList();
    }

    public synchronized ActionResult<WarpReport> reportWarp(
            UUID warpId,
            UUID reporterId,
            String reason
    ) {
        ensureWritable();
        WarpRecord record = warpsById.get(warpId);
        if (record == null || !record.active()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "warp not found");
        }
        if (reports.size() >= MAXIMUM_REPORTS) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "report limit reached");
        }
        String boundedReason = Objects.requireNonNull(reason, "reason").trim();
        if (boundedReason.isEmpty() || boundedReason.length() > 512) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "report reason is outside bounds");
        }
        WarpReport report = new WarpReport(
                UUID.randomUUID(),
                warpId,
                record.revision(),
                reporterId,
                boundedReason,
                Instant.now(),
                ReportStatus.OPEN);
        reports.put(report.id(), report);
        changed();
        return ActionResult.success(report);
    }

    public synchronized List<WarpReport> reports(ReportStatus status) {
        return reports.values().stream()
                .filter(report -> status == null || report.status() == status)
                .sorted(Comparator.comparing(WarpReport::createdAt))
                .toList();
    }

    public synchronized ActionResult<WarpReport> setReportStatus(UUID reportId, ReportStatus status) {
        ensureWritable();
        WarpReport report = reports.get(reportId);
        if (report == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "warp report not found");
        }
        WarpReport replacement = new WarpReport(
                report.id(),
                report.warpId(),
                report.warpRevision(),
                report.reporterId(),
                report.reason(),
                report.createdAt(),
                status);
        reports.put(reportId, replacement);
        changed();
        return ActionResult.success(replacement);
    }

    public synchronized TeleportPreference preference(UUID playerId) {
        return preferences.getOrDefault(playerId, TeleportPreference.defaults(playerId));
    }

    public synchronized TeleportPreference setTpaEnabled(UUID playerId, boolean enabled) {
        ensureWritable();
        TeleportPreference replacement = preference(playerId).withTpaEnabled(enabled);
        preferences.put(playerId, replacement);
        changed();
        return replacement;
    }

    public synchronized TeleportPreference setAutoAccept(UUID playerId, boolean enabled) {
        ensureWritable();
        TeleportPreference replacement = preference(playerId).withAutoAccept(enabled);
        preferences.put(playerId, replacement);
        changed();
        return replacement;
    }

    public synchronized TeleportPreference setBlocked(UUID playerId, UUID blockedId, boolean blocked) {
        ensureWritable();
        TeleportPreference current = preference(playerId);
        Set<UUID> blockedPlayers = new HashSet<>(current.blockedPlayers());
        if (blocked) {
            if (blockedPlayers.size() >= 1000) {
                throw new IllegalStateException("Teleport block list limit reached");
            }
            blockedPlayers.add(blockedId);
        } else {
            blockedPlayers.remove(blockedId);
        }
        TeleportPreference replacement = current.withBlockedPlayers(blockedPlayers);
        preferences.put(playerId, replacement);
        changed();
        return replacement;
    }

    public synchronized TeleportPreference setFavorite(UUID playerId, UUID warpId, boolean favorite) {
        ensureWritable();
        TeleportPreference current = preference(playerId);
        Set<UUID> favorites = new HashSet<>(current.favoriteWarpIds());
        if (favorite) {
            if (favorites.size() >= 1000) {
                throw new IllegalStateException("Favorite warp limit reached");
            }
            favorites.add(warpId);
        } else {
            favorites.remove(warpId);
        }
        TeleportPreference replacement = current.withFavoriteWarpIds(favorites);
        preferences.put(playerId, replacement);
        changed();
        return replacement;
    }

    public synchronized void setSpawn(SpawnRecord record) {
        ensureWritable();
        spawns.put(record.key(), record);
        changed();
    }

    public synchronized Optional<SpawnRecord> spawn(String key) {
        return Optional.ofNullable(spawns.get(SpawnRecord.normalizeKey(key)));
    }

    public synchronized List<SpawnRecord> spawns() {
        return spawns.values().stream().sorted(Comparator.comparing(SpawnRecord::key)).toList();
    }

    public synchronized void queueOfflineTeleport(PendingOfflineTeleport pending) {
        ensureWritable();
        offlineTeleports.put(pending.playerId(), pending);
        changed();
    }

    public synchronized Optional<PendingOfflineTeleport> consumeOfflineTeleport(UUID playerId) {
        ensureWritable();
        PendingOfflineTeleport removed = offlineTeleports.remove(playerId);
        if (removed != null) {
            changed();
        }
        return Optional.ofNullable(removed);
    }

    public synchronized Optional<PendingOfflineTeleport> pendingOfflineTeleport(UUID playerId) {
        return Optional.ofNullable(offlineTeleports.get(playerId));
    }

    public synchronized boolean clearOfflineTeleport(UUID playerId, long expectedRevision) {
        ensureWritable();
        PendingOfflineTeleport current = offlineTeleports.get(playerId);
        if (current == null || current.revision() != expectedRevision) {
            return false;
        }
        offlineTeleports.remove(playerId);
        changed();
        return true;
    }

    public synchronized int purgeExpired(Instant now) {
        ensureWritable();
        int before = transferOffers.size();
        transferOffers.values().removeIf(offer -> !offer.expiresAt().isAfter(now));
        int removed = before - transferOffers.size();
        if (removed > 0) {
            changed();
        }
        return removed;
    }

    public synchronized boolean completedImport(String importId) {
        return completedImports.contains(normalizeImportId(importId));
    }

    public synchronized boolean markImportComplete(String importId) {
        ensureWritable();
        boolean added = completedImports.add(normalizeImportId(importId));
        if (added) {
            changed();
        }
        return added;
    }

    private void purgeExpiredReadOnly(Instant now) {
        int before = transferOffers.size();
        transferOffers.values().removeIf(offer -> !offer.expiresAt().isAfter(now));
        if (before != transferOffers.size() && writableState()) {
            changed();
        }
    }

    @Override
    public void flush() throws IOException {
        final Snapshot snapshot;
        final long snapshotRevision;
        final StorageService.Document previous;
        final Path destination;
        synchronized (this) {
            if (path == null || !dirty()) {
                return;
            }
            if (!writableState()) {
                throw new IOException("Teleport repository is not writable in " + state + " state");
            }
            snapshot = snapshot();
            snapshotRevision = revision;
            previous = document;
            destination = path;
        }
        JsonObject data = encode(snapshot);
        StorageService.write(
                destination,
                DOMAIN,
                SCHEMA_VERSION,
                data,
                previous,
                Set.of(
                        "/homes",
                        "/warps",
                        "/spawns",
                        "/preferences",
                        "/offlineTeleports",
                        "/transferOffers",
                        "/reports",
                        "/completedImports"));
        synchronized (this) {
            document = StorageService.read(destination, DOMAIN, SCHEMA_VERSION).orElse(previous);
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

    public synchronized Snapshot snapshot() {
        return new Snapshot(
                List.copyOf(homesById.values()),
                List.copyOf(warpsById.values()),
                List.copyOf(spawns.values()),
                List.copyOf(preferences.values()),
                List.copyOf(offlineTeleports.values()),
                List.copyOf(transferOffers.values()),
                List.copyOf(reports.values()),
                Set.copyOf(completedImports));
    }

    private void loadHomes(JsonArray array) {
        if (array.size() > MAXIMUM_HOMES) {
            throw new IllegalStateException("Home collection limit exceeded");
        }
        for (JsonElement element : array) {
            HomeRecord record = decodeHome(requiredObject(element, "home"));
            if (homesById.putIfAbsent(record.id(), record) != null) {
                throw new IllegalStateException("Duplicate home identifier");
            }
            if (record.active()) {
                OwnerName key = new OwnerName(record.ownerId(), record.normalizedName());
                if (homeNames.putIfAbsent(key, record.id()) != null) {
                    throw new IllegalStateException("Duplicate active home name");
                }
            }
        }
    }

    private void loadWarps(JsonArray array) {
        if (array.size() > MAXIMUM_WARPS) {
            throw new IllegalStateException("Warp collection limit exceeded");
        }
        for (JsonElement element : array) {
            WarpRecord record = decodeWarp(requiredObject(element, "warp"));
            if (warpsById.putIfAbsent(record.id(), record) != null) {
                throw new IllegalStateException("Duplicate warp identifier");
            }
            if (record.active()) {
                indexWarpName(record);
            }
        }
    }

    private void loadSpawns(JsonArray array) {
        if (array.size() > 10_000) {
            throw new IllegalStateException("Spawn collection limit exceeded");
        }
        for (JsonElement element : array) {
            SpawnRecord record = decodeSpawn(requiredObject(element, "spawn"));
            if (spawns.putIfAbsent(record.key(), record) != null) {
                throw new IllegalStateException("Duplicate spawn key");
            }
        }
    }

    private void loadPreferences(JsonArray array) {
        if (array.size() > MAXIMUM_PREFERENCES) {
            throw new IllegalStateException("Preference collection limit exceeded");
        }
        for (JsonElement element : array) {
            TeleportPreference preference = decodePreference(requiredObject(element, "preference"));
            if (preferences.putIfAbsent(preference.playerId(), preference) != null) {
                throw new IllegalStateException("Duplicate teleport preference");
            }
        }
    }

    private void loadOfflineTeleports(JsonArray array) {
        if (array.size() > MAXIMUM_PREFERENCES) {
            throw new IllegalStateException("Offline teleport collection limit exceeded");
        }
        for (JsonElement element : array) {
            JsonObject object = requiredObject(element, "offline teleport");
            PendingOfflineTeleport pending = new PendingOfflineTeleport(
                    requiredUuid(object, "playerId"),
                    SavedLocation.decode(requiredObject(object.get("location"), "location")),
                    requiredUuid(object, "actorId"),
                    requiredString(object, "reason"),
                    requiredInstant(object, "createdAt"),
                    requiredLong(object, "revision"));
            if (offlineTeleports.putIfAbsent(pending.playerId(), pending) != null) {
                throw new IllegalStateException("Duplicate offline teleport");
            }
        }
    }

    private void loadTransferOffers(JsonArray array) {
        if (array.size() > MAXIMUM_WARPS) {
            throw new IllegalStateException("Transfer offer collection limit exceeded");
        }
        for (JsonElement element : array) {
            JsonObject object = requiredObject(element, "transfer offer");
            TransferOffer offer = new TransferOffer(
                    requiredUuid(object, "id"),
                    requiredUuid(object, "warpId"),
                    requiredUuid(object, "currentOwnerId"),
                    requiredUuid(object, "proposedOwnerId"),
                    requiredLong(object, "warpRevision"),
                    requiredInstant(object, "createdAt"),
                    requiredInstant(object, "expiresAt"));
            if (transferOffers.putIfAbsent(offer.warpId(), offer) != null) {
                throw new IllegalStateException("Duplicate transfer offer");
            }
        }
    }

    private void loadReports(JsonArray array) {
        if (array.size() > MAXIMUM_REPORTS) {
            throw new IllegalStateException("Report collection limit exceeded");
        }
        for (JsonElement element : array) {
            JsonObject object = requiredObject(element, "report");
            WarpReport report = new WarpReport(
                    requiredUuid(object, "id"),
                    requiredUuid(object, "warpId"),
                    requiredLong(object, "warpRevision"),
                    requiredUuid(object, "reporterId"),
                    requiredString(object, "reason"),
                    requiredInstant(object, "createdAt"),
                    ReportStatus.valueOf(requiredString(object, "status")));
            if (reports.putIfAbsent(report.id(), report) != null) {
                throw new IllegalStateException("Duplicate report");
            }
        }
    }

    private void loadCompletedImports(JsonArray array) {
        if (array.size() > 128) {
            throw new IllegalStateException("Completed import collection limit exceeded");
        }
        for (JsonElement element : array) {
            if (!element.isJsonPrimitive() || !completedImports.add(normalizeImportId(element.getAsString()))) {
                throw new IllegalStateException("Invalid completed import identifier");
            }
        }
    }

    private static JsonObject encode(Snapshot snapshot) {
        JsonObject data = new JsonObject();
        JsonArray homes = new JsonArray();
        snapshot.homes().stream().sorted(Comparator.comparing(HomeRecord::id)).forEach(record -> homes.add(encodeHome(record)));
        data.add("homes", homes);

        JsonArray warps = new JsonArray();
        snapshot.warps().stream().sorted(Comparator.comparing(WarpRecord::id)).forEach(record -> warps.add(encodeWarp(record)));
        data.add("warps", warps);

        JsonArray spawns = new JsonArray();
        snapshot.spawns().stream().sorted(Comparator.comparing(SpawnRecord::key)).forEach(record -> spawns.add(encodeSpawn(record)));
        data.add("spawns", spawns);

        JsonArray preferences = new JsonArray();
        snapshot.preferences().stream().sorted(Comparator.comparing(TeleportPreference::playerId))
                .forEach(record -> preferences.add(encodePreference(record)));
        data.add("preferences", preferences);

        JsonArray offline = new JsonArray();
        snapshot.offlineTeleports().stream().sorted(Comparator.comparing(PendingOfflineTeleport::playerId))
                .forEach(record -> {
                    JsonObject object = new JsonObject();
                    object.addProperty("playerId", record.playerId().toString());
                    object.add("location", record.location().encode());
                    object.addProperty("actorId", record.actorId().toString());
                    object.addProperty("reason", record.reason());
                    object.addProperty("createdAt", record.createdAt().toString());
                    object.addProperty("revision", record.revision());
                    offline.add(object);
                });
        data.add("offlineTeleports", offline);

        JsonArray transfers = new JsonArray();
        snapshot.transferOffers().stream().sorted(Comparator.comparing(TransferOffer::warpId))
                .forEach(offer -> {
                    JsonObject object = new JsonObject();
                    object.addProperty("id", offer.id().toString());
                    object.addProperty("warpId", offer.warpId().toString());
                    object.addProperty("currentOwnerId", offer.currentOwnerId().toString());
                    object.addProperty("proposedOwnerId", offer.proposedOwnerId().toString());
                    object.addProperty("warpRevision", offer.warpRevision());
                    object.addProperty("createdAt", offer.createdAt().toString());
                    object.addProperty("expiresAt", offer.expiresAt().toString());
                    transfers.add(object);
                });
        data.add("transferOffers", transfers);

        JsonArray reports = new JsonArray();
        snapshot.reports().stream().sorted(Comparator.comparing(WarpReport::id))
                .forEach(report -> {
                    JsonObject object = new JsonObject();
                    object.addProperty("id", report.id().toString());
                    object.addProperty("warpId", report.warpId().toString());
                    object.addProperty("warpRevision", report.warpRevision());
                    object.addProperty("reporterId", report.reporterId().toString());
                    object.addProperty("reason", report.reason());
                    object.addProperty("createdAt", report.createdAt().toString());
                    object.addProperty("status", report.status().name());
                    reports.add(object);
                });
        data.add("reports", reports);
        JsonArray completedImports = new JsonArray();
        snapshot.completedImports().stream().sorted().forEach(completedImports::add);
        data.add("completedImports", completedImports);
        return data;
    }

    private static JsonObject encodeHome(HomeRecord record) {
        JsonObject object = new JsonObject();
        object.addProperty("id", record.id().toString());
        object.addProperty("ownerId", record.ownerId().toString());
        object.addProperty("normalizedName", record.normalizedName());
        object.addProperty("displayName", record.displayName());
        object.add("location", record.location().encode());
        object.addProperty("createdAt", record.createdAt().toString());
        object.addProperty("updatedAt", record.updatedAt().toString());
        object.addProperty("icon", record.icon());
        object.addProperty("description", record.description());
        object.addProperty("visibility", record.visibility().name());
        object.addProperty("permission", record.permission());
        object.addProperty("safetyRevision", record.safetyRevision());
        object.addProperty("revision", record.revision());
        if (record.deletedAt() != null) {
            object.addProperty("deletedAt", record.deletedAt().toString());
        }
        return object;
    }

    private static HomeRecord decodeHome(JsonObject object) {
        return new HomeRecord(
                requiredUuid(object, "id"),
                requiredUuid(object, "ownerId"),
                requiredString(object, "normalizedName"),
                requiredString(object, "displayName"),
                SavedLocation.decode(requiredObject(object.get("location"), "location")),
                requiredInstant(object, "createdAt"),
                requiredInstant(object, "updatedAt"),
                requiredString(object, "icon"),
                requiredString(object, "description"),
                HomeRecord.Visibility.valueOf(requiredString(object, "visibility")),
                requiredString(object, "permission"),
                requiredLong(object, "safetyRevision"),
                requiredLong(object, "revision"),
                optionalInstant(object, "deletedAt"));
    }

    private static JsonObject encodeWarp(WarpRecord record) {
        JsonObject object = new JsonObject();
        object.addProperty("id", record.id().toString());
        if (record.ownerId() != null) {
            object.addProperty("ownerId", record.ownerId().toString());
        }
        object.addProperty("ownerNameSnapshot", record.ownerNameSnapshot());
        object.addProperty("normalizedName", record.normalizedName());
        object.addProperty("displayName", record.displayName());
        object.addProperty("scope", record.scope().name());
        object.addProperty("access", record.access().name());
        object.addProperty("status", record.status().name());
        object.add("location", record.location().encode());
        object.addProperty("createdAt", record.createdAt().toString());
        object.addProperty("updatedAt", record.updatedAt().toString());
        if (record.publishedAt() != null) {
            object.addProperty("publishedAt", record.publishedAt().toString());
        }
        if (record.deletedAt() != null) {
            object.addProperty("deletedAt", record.deletedAt().toString());
        }
        object.addProperty("permission", record.permission());
        object.addProperty("icon", record.icon());
        object.addProperty("description", record.description());
        object.addProperty("category", record.category());
        object.addProperty("hidden", record.hidden());
        object.addProperty("listed", record.listed());
        object.addProperty("featured", record.featured());
        object.addProperty("visits", record.visits());
        object.addProperty("safetyRevision", record.safetyRevision());
        object.addProperty("revision", record.revision());
        if (record.sourceHomeId() != null) {
            object.addProperty("sourceHomeId", record.sourceHomeId().toString());
        }
        object.add("trustedPlayers", encodeUuidSet(record.trustedPlayers()));
        object.add("blockedPlayers", encodeUuidSet(record.blockedPlayers()));
        return object;
    }

    private static WarpRecord decodeWarp(JsonObject object) {
        return new WarpRecord(
                requiredUuid(object, "id"),
                optionalUuid(object, "ownerId"),
                requiredString(object, "ownerNameSnapshot"),
                requiredString(object, "normalizedName"),
                requiredString(object, "displayName"),
                WarpRecord.Scope.valueOf(requiredString(object, "scope")),
                WarpRecord.Access.valueOf(requiredString(object, "access")),
                WarpRecord.Status.valueOf(requiredString(object, "status")),
                SavedLocation.decode(requiredObject(object.get("location"), "location")),
                requiredInstant(object, "createdAt"),
                requiredInstant(object, "updatedAt"),
                optionalInstant(object, "publishedAt"),
                optionalInstant(object, "deletedAt"),
                requiredString(object, "permission"),
                requiredString(object, "icon"),
                requiredString(object, "description"),
                requiredString(object, "category"),
                requiredBoolean(object, "hidden"),
                requiredBoolean(object, "listed"),
                requiredBoolean(object, "featured"),
                requiredLong(object, "visits"),
                requiredLong(object, "safetyRevision"),
                requiredLong(object, "revision"),
                optionalUuid(object, "sourceHomeId"),
                decodeUuidSet(requiredArray(object, "trustedPlayers")),
                decodeUuidSet(requiredArray(object, "blockedPlayers")));
    }

    private static JsonObject encodeSpawn(SpawnRecord record) {
        JsonObject object = new JsonObject();
        object.addProperty("key", record.key());
        object.add("location", record.location().encode());
        object.addProperty("permission", record.permission());
        object.addProperty("updatedBy", record.updatedBy().toString());
        object.addProperty("updatedAt", record.updatedAt().toString());
        object.addProperty("revision", record.revision());
        return object;
    }

    private static SpawnRecord decodeSpawn(JsonObject object) {
        return new SpawnRecord(
                requiredString(object, "key"),
                SavedLocation.decode(requiredObject(object.get("location"), "location")),
                requiredString(object, "permission"),
                requiredUuid(object, "updatedBy"),
                requiredInstant(object, "updatedAt"),
                requiredLong(object, "revision"));
    }

    private static JsonObject encodePreference(TeleportPreference record) {
        JsonObject object = new JsonObject();
        object.addProperty("playerId", record.playerId().toString());
        object.addProperty("tpaEnabled", record.tpaEnabled());
        object.addProperty("autoAccept", record.autoAccept());
        object.add("blockedPlayers", encodeUuidSet(record.blockedPlayers()));
        object.add("favoriteWarpIds", encodeUuidSet(record.favoriteWarpIds()));
        object.addProperty("revision", record.revision());
        return object;
    }

    private static TeleportPreference decodePreference(JsonObject object) {
        return new TeleportPreference(
                requiredUuid(object, "playerId"),
                requiredBoolean(object, "tpaEnabled"),
                requiredBoolean(object, "autoAccept"),
                decodeUuidSet(requiredArray(object, "blockedPlayers")),
                decodeUuidSet(requiredArray(object, "favoriteWarpIds")),
                requiredLong(object, "revision"));
    }

    private static JsonArray encodeUuidSet(Set<UUID> values) {
        JsonArray array = new JsonArray();
        values.stream().sorted().forEach(value -> array.add(value.toString()));
        return array;
    }

    private static Set<UUID> decodeUuidSet(JsonArray array) {
        if (array.size() > 1000) {
            throw new IllegalStateException("UUID collection limit exceeded");
        }
        Set<UUID> result = new LinkedHashSet<>();
        for (JsonElement element : array) {
            if (!element.isJsonPrimitive()) {
                throw new IllegalStateException("UUID collection value is not a string");
            }
            if (!result.add(UUID.fromString(element.getAsString()))) {
                throw new IllegalStateException("Duplicate UUID collection value");
            }
        }
        return Set.copyOf(result);
    }

    private void indexWarpName(WarpRecord record) {
        UUID conflict;
        if (record.scope() == WarpRecord.Scope.SERVER_PUBLIC) {
            conflict = serverWarpNames.putIfAbsent(record.normalizedName(), record.id());
        } else {
            conflict = playerWarpNames.putIfAbsent(
                    new OwnerName(record.ownerId(), record.normalizedName()),
                    record.id());
        }
        if (conflict != null) {
            throw new IllegalStateException("Duplicate active warp name");
        }
    }

    private void removeWarpName(WarpRecord record) {
        if (record.scope() == WarpRecord.Scope.SERVER_PUBLIC) {
            serverWarpNames.remove(record.normalizedName(), record.id());
        } else {
            playerWarpNames.remove(new OwnerName(record.ownerId(), record.normalizedName()), record.id());
        }
    }

    private void clearCollections() {
        homesById.clear();
        homeNames.clear();
        warpsById.clear();
        serverWarpNames.clear();
        playerWarpNames.clear();
        spawns.clear();
        preferences.clear();
        offlineTeleports.clear();
        transferOffers.clear();
        reports.clear();
        completedImports.clear();
    }

    private void ensureWritable() {
        if (!writableState()) {
            throw new IllegalStateException("Teleport repository is not writable in " + state + " state");
        }
    }

    private boolean writableState() {
        return state != RepositoryState.RECOVERY
                && state != RepositoryState.UNSUPPORTED
                && state != RepositoryState.ERROR;
    }

    private void changed() {
        revision++;
    }

    private static String normalizeImportId(String value) {
        String normalized = Objects.requireNonNull(value, "importId").trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9_:/.]{1,128}")) {
            throw new IllegalArgumentException("Invalid import identifier");
        }
        return normalized;
    }

    private static JsonArray requiredArray(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonArray()) {
            throw new IllegalStateException("Required array is missing, " + key);
        }
        return object.getAsJsonArray(key);
    }

    private static JsonObject requiredObject(JsonElement element, String name) {
        if (element == null || !element.isJsonObject()) {
            throw new IllegalStateException("Required object is missing, " + name);
        }
        return element.getAsJsonObject();
    }

    private static String requiredString(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonPrimitive()) {
            throw new IllegalStateException("Required string is missing, " + key);
        }
        return object.get(key).getAsString();
    }

    private static long requiredLong(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonPrimitive()) {
            throw new IllegalStateException("Required number is missing, " + key);
        }
        return object.get(key).getAsLong();
    }

    private static boolean requiredBoolean(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonPrimitive()) {
            throw new IllegalStateException("Required boolean is missing, " + key);
        }
        return object.get(key).getAsBoolean();
    }

    private static UUID requiredUuid(JsonObject object, String key) {
        return UUID.fromString(requiredString(object, key));
    }

    private static UUID optionalUuid(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull()
                ? UUID.fromString(object.get(key).getAsString())
                : null;
    }

    private static Instant requiredInstant(JsonObject object, String key) {
        return Instant.parse(requiredString(object, key));
    }

    private static Instant optionalInstant(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull()
                ? Instant.parse(object.get(key).getAsString())
                : null;
    }

    private static RepositoryState stateFromStorageStatus(Path path) {
        return StorageService.statuses().stream()
                .filter(status -> status.path().equals(path))
                .findFirst()
                .map(status -> switch (status.state()) {
                    case "missing" -> RepositoryState.MISSING;
                    case "unsupported" -> RepositoryState.UNSUPPORTED;
                    case "quarantined", "quarantine failed", "rejected" -> RepositoryState.RECOVERY;
                    default -> RepositoryState.ERROR;
                })
                .orElse(RepositoryState.MISSING);
    }

    private record OwnerName(UUID ownerId, String normalizedName) {
        private OwnerName {
            Objects.requireNonNull(ownerId, "ownerId");
            Objects.requireNonNull(normalizedName, "normalizedName");
        }
    }

    public record Snapshot(
            List<HomeRecord> homes,
            List<WarpRecord> warps,
            List<SpawnRecord> spawns,
            List<TeleportPreference> preferences,
            List<PendingOfflineTeleport> offlineTeleports,
            List<TransferOffer> transferOffers,
            List<WarpReport> reports,
            Set<String> completedImports
    ) {
        public Snapshot {
            homes = List.copyOf(homes);
            warps = List.copyOf(warps);
            spawns = List.copyOf(spawns);
            preferences = List.copyOf(preferences);
            offlineTeleports = List.copyOf(offlineTeleports);
            transferOffers = List.copyOf(transferOffers);
            reports = List.copyOf(reports);
            completedImports = Set.copyOf(completedImports);
        }
    }

    public record SpawnRecord(
            String key,
            SavedLocation location,
            String permission,
            UUID updatedBy,
            Instant updatedAt,
            long revision
    ) {
        public SpawnRecord {
            key = normalizeKey(key);
            location = Objects.requireNonNull(location, "location");
            permission = permission == null ? "" : permission.trim();
            if (permission.length() > 192) {
                throw new IllegalArgumentException("Spawn permission is too long");
            }
            updatedBy = Objects.requireNonNull(updatedBy, "updatedBy");
            updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
            if (revision < 1) {
                throw new IllegalArgumentException("Spawn revision is outside bounds");
            }
        }

        public static String normalizeKey(String value) {
            String normalized = Objects.requireNonNull(value, "key").trim().toLowerCase(Locale.ROOT);
            if (!normalized.matches("[a-z0-9_:/.]{1,128}")) {
                throw new IllegalArgumentException("Invalid spawn key");
            }
            return normalized;
        }
    }

    public record TeleportPreference(
            UUID playerId,
            boolean tpaEnabled,
            boolean autoAccept,
            Set<UUID> blockedPlayers,
            Set<UUID> favoriteWarpIds,
            long revision
    ) {
        public TeleportPreference {
            playerId = Objects.requireNonNull(playerId, "playerId");
            blockedPlayers = Set.copyOf(Objects.requireNonNull(blockedPlayers, "blockedPlayers"));
            favoriteWarpIds = Set.copyOf(Objects.requireNonNull(favoriteWarpIds, "favoriteWarpIds"));
            if (blockedPlayers.size() > 1000 || favoriteWarpIds.size() > 1000 || revision < 1) {
                throw new IllegalArgumentException("Teleport preference is outside bounds");
            }
        }

        public static TeleportPreference defaults(UUID playerId) {
            return new TeleportPreference(playerId, true, false, Set.of(), Set.of(), 1);
        }

        public TeleportPreference withTpaEnabled(boolean replacement) {
            return new TeleportPreference(
                    playerId,
                    replacement,
                    autoAccept,
                    blockedPlayers,
                    favoriteWarpIds,
                    revision + 1);
        }

        public TeleportPreference withAutoAccept(boolean replacement) {
            return new TeleportPreference(
                    playerId,
                    tpaEnabled,
                    replacement,
                    blockedPlayers,
                    favoriteWarpIds,
                    revision + 1);
        }

        public TeleportPreference withBlockedPlayers(Set<UUID> replacement) {
            return new TeleportPreference(
                    playerId,
                    tpaEnabled,
                    autoAccept,
                    replacement,
                    favoriteWarpIds,
                    revision + 1);
        }

        public TeleportPreference withFavoriteWarpIds(Set<UUID> replacement) {
            return new TeleportPreference(
                    playerId,
                    tpaEnabled,
                    autoAccept,
                    blockedPlayers,
                    replacement,
                    revision + 1);
        }
    }

    public record PendingOfflineTeleport(
            UUID playerId,
            SavedLocation location,
            UUID actorId,
            String reason,
            Instant createdAt,
            long revision
    ) {
        public PendingOfflineTeleport {
            playerId = Objects.requireNonNull(playerId, "playerId");
            location = Objects.requireNonNull(location, "location");
            actorId = Objects.requireNonNull(actorId, "actorId");
            reason = Objects.requireNonNull(reason, "reason").trim();
            createdAt = Objects.requireNonNull(createdAt, "createdAt");
            if (reason.length() > 128 || revision < 1) {
                throw new IllegalArgumentException("Offline teleport is outside bounds");
            }
        }
    }

    public record TransferOffer(
            UUID id,
            UUID warpId,
            UUID currentOwnerId,
            UUID proposedOwnerId,
            long warpRevision,
            Instant createdAt,
            Instant expiresAt
    ) {
        public TransferOffer {
            id = Objects.requireNonNull(id, "id");
            warpId = Objects.requireNonNull(warpId, "warpId");
            currentOwnerId = Objects.requireNonNull(currentOwnerId, "currentOwnerId");
            proposedOwnerId = Objects.requireNonNull(proposedOwnerId, "proposedOwnerId");
            createdAt = Objects.requireNonNull(createdAt, "createdAt");
            expiresAt = Objects.requireNonNull(expiresAt, "expiresAt");
            if (warpRevision < 1 || !expiresAt.isAfter(createdAt)) {
                throw new IllegalArgumentException("Transfer offer is outside bounds");
            }
        }
    }

    public record WarpReport(
            UUID id,
            UUID warpId,
            long warpRevision,
            UUID reporterId,
            String reason,
            Instant createdAt,
            ReportStatus status
    ) {
        public WarpReport {
            id = Objects.requireNonNull(id, "id");
            warpId = Objects.requireNonNull(warpId, "warpId");
            reporterId = Objects.requireNonNull(reporterId, "reporterId");
            reason = Objects.requireNonNull(reason, "reason").trim();
            createdAt = Objects.requireNonNull(createdAt, "createdAt");
            status = Objects.requireNonNull(status, "status");
            if (warpRevision < 1 || reason.isEmpty() || reason.length() > 512) {
                throw new IllegalArgumentException("Warp report is outside bounds");
            }
        }
    }

    public enum ReportStatus {
        OPEN,
        RESOLVED,
        DISMISSED
    }
}
