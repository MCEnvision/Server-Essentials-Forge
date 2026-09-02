package com.enviouse.sef.banned;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.enviouse.sef.utils.moddeps.CuriosInventoryHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Banned-item manager.
 *
 * <ul>
 *   <li>Stores ban entries (with reason / duration / banner / announce flag), bypass
 *       UUIDs, excepted blocks, and runtime tuning state in
 *       {@code <world>/serverconfig/sef/banned_items.json}.</li>
 *   <li>Inventory + cursor + open container + Curios are scanned every tick for
 *       any online player.</li>
 *   <li>Block sweeps run on a configurable interval/radius around each player.</li>
 *   <li>Players in creative or with {@code sef.banned.bypass} or in the manual bypass
 *       list are exempt.</li>
 *   <li>Auto-purges expired ban entries.</li>
 * </ul>
 */
public class BannedItemsManager implements StorageRepository {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String DOMAIN = "banned items";
    private static final int SCHEMA_VERSION = 2;
    private static final int MAXIMUM_ENTRIES = 100_000;
    private static final int MAXIMUM_BYPASSES = 100_000;
    private static final int MAXIMUM_EXCEPTIONS = 100_000;
    private static final long MAXIMUM_DURATION_MILLIS = Duration.ofDays(3_650).toMillis();
    private static final int MAXIMUM_SCAN_RADIUS = 256;
    private static final int MAXIMUM_SCAN_INTERVAL = 1_200_000;

    private final Map<String, BannedEntry> entries = new ConcurrentHashMap<>();
    private final java.util.Set<UUID> bypassed = ConcurrentHashMap.newKeySet();
    private final List<BannedExceptedBlock> excepted = new CopyOnWriteArrayList<>();

    private boolean enabledItems = true;
    private boolean enabledBlocks = true;
    private boolean dropOnDestroy = false;
    private int radiusOverride = -1;       // -1 = use config
    private int intervalOverride = -1;     // -1 = use config

    private Path filePath;
    private Path loadedPath;
    private StorageService.Document document;
    private RepositoryState state = RepositoryState.NEW;
    private long revision;
    private long flushedRevision;
    private int lastScanTick = 0;
    private final Map<UUID, BlockScanCursor> blockScanCursors = new ConcurrentHashMap<>();

    // ── Persistence ─────────────────────────────────────────────────────────

    public void load(MinecraftServer server) {
        LoadResult result = load(server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                .resolve("serverconfig")
                .resolve("sef"));
        if (available()) {
            ServerEssentialsForge.LOGGER.info(
                    "[SEF] Loaded {} banned entries, {} bypasses, and {} exceptions",
                    entries.size(),
                    bypassed.size(),
                    excepted.size());
        } else {
            ServerEssentialsForge.LOGGER.error(
                    "[SEF] Banned item storage is unavailable in state {}. Item actions are blocked until recovery",
                    result.state());
        }
    }

    @Override
    public synchronized LoadResult load(Path managedRoot) {
        Path destination = Objects.requireNonNull(managedRoot, "managedRoot")
                .resolve("banned_items.json")
                .toAbsolutePath()
                .normalize();
        Path previousPath = loadedPath;
        RepositoryState previousState = state;
        filePath = destination;
        boolean existed = Files.exists(destination, java.nio.file.LinkOption.NOFOLLOW_LINKS);
        StorageService.Document candidate =
                StorageService.read(destination, DOMAIN, SCHEMA_VERSION).orElse(null);
        if (candidate == null) {
            RepositoryState loadedState = stateFromStorageStatus(destination);
            if (loadedState == RepositoryState.MISSING) {
                if (destination.equals(previousPath)
                        && (previousState == RepositoryState.READY || previousState == RepositoryState.MISSING)) {
                    state = RepositoryState.RECOVERY;
                    return new LoadResult(state, "banned item storage disappeared after initialization");
                }
                publish(emptySnapshot());
                loadedPath = destination;
                document = null;
                revision = 0L;
                flushedRevision = 0L;
                state = RepositoryState.MISSING;
                return new LoadResult(state, "new repository");
            }
            if (!destination.equals(previousPath)) {
                publish(emptySnapshot());
                document = null;
                revision = 0L;
                flushedRevision = 0L;
            }
            state = loadedState;
            return new LoadResult(state, existed ? "storage unavailable" : "storage missing");
        }

        try {
            ParseResult parsed = parse(candidate.data());
            publish(parsed.snapshot());
            loadedPath = destination;
            document = candidate;
            state = RepositoryState.READY;
            revision = Math.addExact(revision, 1L);
            flushedRevision = candidate.migrated() || parsed.normalized()
                    ? revision - 1L
                    : revision;
            if (dirty()) {
                flush();
            }
            return new LoadResult(state, "loaded banned item storage");
        } catch (IOException | RuntimeException exception) {
            state = RepositoryState.RECOVERY;
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to load banned items", exception);
            return new LoadResult(state, exception.getClass().getSimpleName());
        }
    }

    public synchronized void save() {
        try {
            flush();
        } catch (IOException exception) {
            state = RepositoryState.ERROR;
            throw new IllegalStateException("Failed to save banned item storage", exception);
        }
    }

    @Override
    public synchronized void flush() throws IOException {
        if (filePath == null || !dirty()) {
            return;
        }
        writable();
        long snapshotRevision = revision;
        JsonObject root = serialize(capture());
        StorageService.write(
                filePath,
                DOMAIN,
                SCHEMA_VERSION,
                root,
                document,
                Set.of("/entries"));
        document = StorageService.read(filePath, DOMAIN, SCHEMA_VERSION).orElse(document);
        flushedRevision = Math.max(flushedRevision, snapshotRevision);
        state = RepositoryState.READY;
    }

    public void reload(MinecraftServer server) {
        load(server);
    }

    public synchronized boolean shutdown() {
        try {
            flush();
            state = RepositoryState.CLOSED;
            return true;
        } catch (IOException | RuntimeException exception) {
            state = RepositoryState.ERROR;
            ServerEssentialsForge.LOGGER.error(
                    "[SEF] Banned item shutdown flush did not complete",
                    exception);
            return false;
        }
    }

    // ── Entry CRUD ──────────────────────────────────────────────────────────

    public synchronized Map<String, BannedEntry> getEntries() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(entries));
    }

    public synchronized java.util.Set<String> getPatterns() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(entries.keySet()));
    }

    public synchronized BannedEntry getEntry(String pattern) {
        return pattern == null ? null : entries.get(pattern.toLowerCase(Locale.ROOT));
    }

    public synchronized boolean addBan(
            String pattern,
            String reason,
            long durationMs,
            String bannedBy,
            boolean announce
    ) {
        writableForMutation();
        String normalizedPattern = validatePattern(pattern);
        String key = normalizedPattern.toLowerCase(Locale.ROOT);
        if (entries.containsKey(key)) {
            return false;
        }
        validateText(reason == null ? "" : reason, "reason", 1_024, true);
        validateText(bannedBy == null ? "Console" : bannedBy, "banned by", 128, false);
        validateDuration(durationMs);
        mutate(() -> entries.put(
                key,
                new BannedEntry(normalizedPattern, reason, bannedBy, durationMs, announce)));
        return true;
    }

    public synchronized boolean removeBan(String pattern) {
        writableForMutation();
        if (pattern == null) {
            return false;
        }
        String key = pattern.toLowerCase(Locale.ROOT);
        if (!entries.containsKey(key)) {
            return false;
        }
        mutate(() -> entries.remove(key));
        return true;
    }

    public synchronized boolean updateBan(
            String pattern,
            String reason,
            Long durationMs,
            Boolean announce
    ) {
        writableForMutation();
        if (pattern == null) {
            return false;
        }
        String key = pattern.toLowerCase(Locale.ROOT);
        BannedEntry entry = entries.get(key);
        if (entry == null) {
            return false;
        }
        if (reason != null) {
            validateText(reason, "reason", 1_024, true);
        }
        if (durationMs != null) {
            validateDuration(durationMs);
        }
        mutate(() -> {
            BannedEntry updated = copy(entry);
            if (reason != null) {
                updated.reason = reason;
            }
            if (durationMs != null) {
                updated.durationMs = durationMs;
                updated.bannedAtMillis = System.currentTimeMillis();
            }
            if (announce != null) {
                updated.announce = announce;
            }
            entries.put(key, updated);
        });
        return true;
    }

    public synchronized int clearAll() {
        writableForMutation();
        int count = entries.size();
        if (count > 0) {
            mutate(entries::clear);
        }
        return count;
    }

    private synchronized int purgeExpired() {
        List<String> expired = entries.entrySet().stream()
                .filter(entry -> entry.getValue().isExpired())
                .map(Map.Entry::getKey)
                .toList();
        if (!expired.isEmpty()) {
            mutate(() -> expired.forEach(entries::remove));
        }
        return expired.size();
    }

    // ── Matching ────────────────────────────────────────────────────────────

    public synchronized BannedEntry matchItem(ItemStack stack) {
        requireAvailable();
        if (stack == null || stack.isEmpty()) return null;
        Item item = stack.getItem();
        ResourceLocation rl = BuiltInRegistries.ITEM.getKey(item);
        if (rl == null) return null;
        for (BannedEntry e : entries.values()) {
            if (e.isExpired()) continue;
            if (e.matchesItem(rl, stack)) return e;
        }
        return null;
    }

    public synchronized BannedEntry matchBlock(BlockState state) {
        requireAvailable();
        if (state == null || state.isAir()) return null;
        Block block = state.getBlock();
        ResourceLocation rl = BuiltInRegistries.BLOCK.getKey(block);
        if (rl == null) return null;
        for (BannedEntry e : entries.values()) {
            if (e.isExpired()) continue;
            if (e.matchesBlock(rl, state)) return e;
            // also catch tag/wildcard bans expressed as item ids by checking the block's item form
            Item itemForm = block.asItem();
            if (itemForm != null) {
                ResourceLocation irl = BuiltInRegistries.ITEM.getKey(itemForm);
                if (irl != null && e.matchesItem(irl, new ItemStack(itemForm))) return e;
            }
        }
        return null;
    }

    // ── Bypass ──────────────────────────────────────────────────────────────

    public synchronized boolean isBypassed(ServerPlayer player) {
        if (!available()) return false;
        if (player == null) return false;
        if (player.gameMode.getGameModeForPlayer() == GameType.CREATIVE) return true;
        if (bypassed.contains(player.getUUID())) return true;
        try {
            // permission-based bypass; falls through to false on any error
            net.neoforged.neoforge.server.permission.nodes.PermissionNode<Boolean> node =
                    com.enviouse.sef.config.PermissionsHandler.bannedBypassNode;
            if (node != null && com.enviouse.sef.permissions.PermissionService.has(player, node)) {
                return true;
            }
        } catch (RuntimeException exception) {
            ServerEssentialsForge.LOGGER.warn(
                    "[SEF] Failed to evaluate the banned item bypass permission for {}",
                    player.getUUID(),
                    exception);
        }
        return false;
    }

    public synchronized boolean setBypass(UUID uuid, boolean on) {
        writableForMutation();
        Objects.requireNonNull(uuid, "uuid");
        boolean changed = on ? !bypassed.contains(uuid) : bypassed.contains(uuid);
        if (changed) {
            mutate(() -> {
                if (on) {
                    bypassed.add(uuid);
                } else {
                    bypassed.remove(uuid);
                }
            });
        }
        return changed;
    }

    public synchronized boolean isManualBypass(UUID uuid) { return bypassed.contains(uuid); }

    public synchronized java.util.Set<UUID> getBypassed() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(bypassed));
    }

    // ── Exceptions ──────────────────────────────────────────────────────────

    public synchronized List<BannedExceptedBlock> getExceptions() {
        return Collections.unmodifiableList(new ArrayList<>(excepted));
    }

    public synchronized void addException(ServerLevel level, BlockPos pos, String itemId, String addedBy) {
        writableForMutation();
        if (level == null || pos == null) return;
        String dim = level.dimension().location().toString();
        if (isExcepted(dim, pos.getX(), pos.getY(), pos.getZ())) return;
        BannedExceptedBlock block =
                new BannedExceptedBlock(dim, pos.getX(), pos.getY(), pos.getZ(), itemId, addedBy);
        validate(block, System.currentTimeMillis());
        mutate(() -> excepted.add(block));
    }

    public boolean isExcepted(ServerLevel level, BlockPos pos) {
        return isExcepted(level.dimension().location().toString(),
                pos.getX(), pos.getY(), pos.getZ());
    }

    public synchronized boolean isExcepted(String dim, int x, int y, int z) {
        for (BannedExceptedBlock b : excepted) {
            if (b.matches(dim, x, y, z)) return true;
        }
        return false;
    }

    public synchronized boolean removeExceptionAt(int index) {
        writableForMutation();
        if (index < 0 || index >= excepted.size()) return false;
        mutate(() -> excepted.remove(index));
        return true;
    }

    public synchronized int clearExceptions() {
        writableForMutation();
        int count = excepted.size();
        if (count > 0) {
            mutate(excepted::clear);
        }
        return count;
    }

    // ── Toggles ─────────────────────────────────────────────────────────────

    public synchronized boolean isItemsEnabled() { return enabledItems; }
    public synchronized boolean isBlocksEnabled() { return enabledBlocks; }
    public synchronized boolean isDropOnDestroy() { return dropOnDestroy; }

    public synchronized void setItemsEnabled(boolean value) {
        writableForMutation();
        if (enabledItems != value) {
            mutate(() -> enabledItems = value);
        }
    }

    public synchronized void setBlocksEnabled(boolean value) {
        writableForMutation();
        if (enabledBlocks != value) {
            mutate(() -> enabledBlocks = value);
        }
    }

    public synchronized void setDropOnDestroy(boolean value) {
        writableForMutation();
        if (dropOnDestroy != value) {
            mutate(() -> dropOnDestroy = value);
        }
    }

    public int getEffectiveRadius() {
        if (radiusOverride > 0) return radiusOverride;
        return Math.min(MAXIMUM_SCAN_RADIUS, Math.max(1, ConfigHandler.config.bannedBlockScanRadius.get()));
    }

    public int getEffectiveInterval() {
        if (intervalOverride > 0) return intervalOverride;
        return Math.min(
                MAXIMUM_SCAN_INTERVAL,
                Math.max(1, ConfigHandler.config.bannedBlockScanInterval.get()));
    }

    public synchronized void setRadiusOverride(int value) {
        writableForMutation();
        if (value < 1 || value > MAXIMUM_SCAN_RADIUS) {
            throw new IllegalArgumentException("Banned block scan radius is outside bounds");
        }
        if (radiusOverride != value) {
            mutate(() -> radiusOverride = value);
        }
    }

    public synchronized void setIntervalOverride(int value) {
        writableForMutation();
        if (value < 1 || value > MAXIMUM_SCAN_INTERVAL) {
            throw new IllegalArgumentException("Banned block scan interval is outside bounds");
        }
        if (intervalOverride != value) {
            mutate(() -> intervalOverride = value);
        }
    }

    // ── Tick ────────────────────────────────────────────────────────────────

    public void tick(MinecraftServer server, int tickCount) {
        if (!available()) return;
        if (entries.isEmpty()) return;

        // Drop expired entries every ~5s so /banned list stays accurate.
        if (tickCount % 100 == 0) purgeExpired();

        int inventoryInterval = Math.max(1, ConfigHandler.config.bannedInventoryScanInterval.get());
        if (enabledItems && tickCount % inventoryInterval == 0) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (isBypassed(player)) continue;
                scanInventory(player);
            }
        }

        if (enabledBlocks && ConfigHandler.config.enableBannedBlockScanning.get()) {
            int interval = getEffectiveInterval();
            if (tickCount - lastScanTick >= interval) {
                lastScanTick = tickCount;
                int radius = getEffectiveRadius();
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (!isBypassed(player)) {
                        blockScanCursors.put(player.getUUID(), new BlockScanCursor(
                                player.serverLevel().dimension().location().toString(),
                                player.blockPosition(),
                                radius));
                    }
                }
            }
            processBlockScanBudget(server);
        } else {
            blockScanCursors.clear();
        }
    }

    private void processBlockScanBudget(MinecraftServer server) {
        int remaining = Math.max(1, ConfigHandler.config.bannedBlockScanBudget.get());
        java.util.Set<UUID> online = new java.util.HashSet<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            online.add(player.getUUID());
            BlockScanCursor cursor = blockScanCursors.get(player.getUUID());
            if (cursor == null || remaining <= 0) continue;
            if (!cursor.dimension.equals(player.serverLevel().dimension().location().toString())) {
                blockScanCursors.remove(player.getUUID());
                continue;
            }
            remaining -= processBlockScan(player, cursor, remaining);
            if (cursor.complete()) blockScanCursors.remove(player.getUUID());
        }
        blockScanCursors.keySet().removeIf(uuid -> !online.contains(uuid));
    }

    private int processBlockScan(ServerPlayer player, BlockScanCursor cursor, int budget) {
        ServerLevel level = player.serverLevel();
        int processed = 0;
        int side = cursor.radius * 2 + 1;
        int plane = side * side;
        while (processed < budget && !cursor.complete()) {
            int index = cursor.index++;
            int x = index / plane - cursor.radius;
            int remainder = index % plane;
            int y = remainder / side - cursor.radius;
            int z = remainder % side - cursor.radius;
            BlockPos pos = cursor.center.offset(x, y, z);
            processed++;
            if (!level.hasChunkAt(pos) || isExcepted(level, pos)) continue;
            BlockState state = level.getBlockState(pos);
            BannedEntry hit = matchBlock(state);
            if (hit != null) {
                ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                level.destroyBlock(pos, dropOnDestroy);
                ServerEssentialsForge.LOGGER.info(
                        "[SEF] Destroyed banned block {} at {} near {}",
                        id, pos, player.getGameProfile().getName());
            }
        }
        return processed;
    }

    // ── Scans ───────────────────────────────────────────────────────────────

    /** Force a full inventory + container + curios scan for one player. */
    public int forceScan(ServerPlayer player) {
        int total = scanInventory(player);
        return total;
    }

    /**
     * Scans the player's main inventory (hotbar + main + armor + offhand), the
     * cursor stack, the currently open container, and Curios slots.
     */
    public int scanInventory(ServerPlayer player) {
        int removed = 0;
        // Main inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            BannedEntry hit = matchItem(stack);
            if (hit != null) {
                player.getInventory().setItem(i, ItemStack.EMPTY);
                notifyConfiscation(player, stack, hit);
                removed++;
            }
        }
        // Cursor (held while a container is open)
        AbstractContainerMenu menu = player.containerMenu;
        if (menu != null) {
            ItemStack carried = menu.getCarried();
            BannedEntry hit = matchItem(carried);
            if (hit != null) {
                menu.setCarried(ItemStack.EMPTY);
                menu.broadcastChanges();
                notifyConfiscation(player, carried, hit);
                removed++;
            }
            // Currently-open container slots (chests etc.)
            for (Slot slot : menu.slots) {
                ItemStack s = slot.getItem();
                BannedEntry h = matchItem(s);
                if (h != null) {
                    slot.set(ItemStack.EMPTY);
                    notifyConfiscation(player, s, h);
                    removed++;
                }
            }
            if (removed > 0) menu.broadcastChanges();
        }
        // Curios
        if (CuriosInventoryHelper.isCuriosLoaded()) {
            int curiosRemoved = CuriosInventoryHelper.clearMatching(player, stack -> {
                BannedEntry e = matchItem(stack);
                if (e != null) {
                    notifyConfiscation(player, stack, e);
                    return true;
                }
                return false;
            });
            removed += curiosRemoved;
        }
        if (removed > 0) {
            player.inventoryMenu.broadcastChanges();
        }
        return removed;
    }

    /** Cubic radius sweep around the player. Excepted blocks are skipped. */
    public int scanBlocksAround(ServerPlayer player, int radius) {
        requireAvailable();
        if (radius < 1 || radius > MAXIMUM_SCAN_RADIUS) {
            throw new IllegalArgumentException("Banned block scan radius is outside bounds");
        }
        ServerLevel level = player.serverLevel();
        BlockPos center = player.blockPosition();
        int destroyed = 0;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (!level.hasChunkAt(pos) || isExcepted(level, pos)) continue;
                    BlockState state = level.getBlockState(pos);
                    BannedEntry hit = matchBlock(state);
                    if (hit != null) {
                        ResourceLocation rl = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                        level.destroyBlock(pos, dropOnDestroy);
                        ServerEssentialsForge.LOGGER.info(
                                "[SEF] Destroyed banned block {} at {} near {}",
                                rl, pos, player.getGameProfile().getName());
                        destroyed++;
                    }
                }
            }
        }
        return destroyed;
    }

    private static final class BlockScanCursor {
        private final String dimension;
        private final BlockPos center;
        private final int radius;
        private final int total;
        private int index;

        private BlockScanCursor(String dimension, BlockPos center, int radius) {
            this.dimension = dimension;
            this.center = center.immutable();
            this.radius = Math.max(1, radius);
            int side = this.radius * 2 + 1;
            this.total = side * side * side;
        }

        private boolean complete() {
            return index >= total;
        }
    }

    // ── Notification ────────────────────────────────────────────────────────

    private void notifyConfiscation(ServerPlayer player, ItemStack stack, BannedEntry hit) {
        ResourceLocation rl = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String idStr = rl != null ? rl.toString() : "unknown";

        String configFmt = ConfigHandler.config.bannedItemRemovedMsg.get();
        String fmt = (configFmt == null || configFmt.isEmpty())
                ? "&cBanned item removed: &e$item&7 (&fReason: $reason&7)"
                : configFmt;
        String text = fmt.replace("$item", idStr)
                .replace("$reason", hit.reason == null || hit.reason.isEmpty() ? "no reason given" : hit.reason)
                .replace("$by", hit.bannedBy == null ? "" : hit.bannedBy)
                .replace("$remaining", hit.getRemainingString());
        Component msg = TextFormatter.stringToFormattedText(text);
        player.sendSystemMessage(msg);

        ServerEssentialsForge.LOGGER.info("[SEF] Confiscated banned item {} from {}",
                idStr, player.getGameProfile().getName());

        if (hit.announce && player.getServer() != null) {
            String anFmt = ConfigHandler.config.bannedAnnounceFormat.get();
            if (anFmt == null || anFmt.isEmpty()) {
                anFmt = "&7[&cBanned&7] &e$player &7had banned item &e$item &7removed.";
            }
            String anText = anFmt
                    .replace("$player", player.getGameProfile().getName())
                    .replace("$item", idStr)
                    .replace("$reason", hit.reason == null ? "" : hit.reason);
            Component aMsg = TextFormatter.stringToFormattedText(anText);
            for (ServerPlayer p : player.getServer().getPlayerList().getPlayers()) {
                p.sendSystemMessage(aMsg);
            }
        }
    }

    public synchronized boolean available() {
        return state == RepositoryState.READY || state == RepositoryState.MISSING;
    }

    @Override
    public String id() {
        return "sef:banned_items";
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
        return filePath;
    }

    @Override
    public synchronized boolean dirty() {
        return revision > flushedRevision;
    }

    @Override
    public synchronized RepositoryState state() {
        return state;
    }

    private synchronized void mutate(Runnable mutation) {
        Snapshot previous = capture();
        mutation.run();
        revision = Math.addExact(revision, 1L);
        try {
            save();
        } catch (RuntimeException exception) {
            publish(previous);
            revision = Math.addExact(revision, 1L);
            throw exception;
        }
    }

    private synchronized Snapshot capture() {
        Map<String, BannedEntry> entrySnapshot = new LinkedHashMap<>();
        entries.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> entrySnapshot.put(entry.getKey(), copy(entry.getValue())));
        Set<UUID> bypassSnapshot = new LinkedHashSet<>(bypassed.stream().sorted().toList());
        List<BannedExceptedBlock> exceptionSnapshot = excepted.stream()
                .map(BannedItemsManager::copy)
                .toList();
        return new Snapshot(
                entrySnapshot,
                bypassSnapshot,
                exceptionSnapshot,
                enabledItems,
                enabledBlocks,
                dropOnDestroy,
                radiusOverride,
                intervalOverride);
    }

    private synchronized void publish(Snapshot snapshot) {
        entries.clear();
        snapshot.entries().forEach((key, value) -> entries.put(key, copy(value)));
        bypassed.clear();
        bypassed.addAll(snapshot.bypassed());
        excepted.clear();
        snapshot.excepted().stream().map(BannedItemsManager::copy).forEach(excepted::add);
        enabledItems = snapshot.enabledItems();
        enabledBlocks = snapshot.enabledBlocks();
        dropOnDestroy = snapshot.dropOnDestroy();
        radiusOverride = snapshot.radiusOverride();
        intervalOverride = snapshot.intervalOverride();
    }

    private static Snapshot emptySnapshot() {
        return new Snapshot(Map.of(), Set.of(), List.of(), true, true, false, -1, -1);
    }

    private static JsonObject serialize(Snapshot snapshot) {
        JsonObject root = new JsonObject();
        JsonObject serializedEntries = new JsonObject();
        snapshot.entries().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> serializedEntries.add(entry.getKey(), GSON.toJsonTree(entry.getValue())));
        root.add("entries", serializedEntries);

        JsonArray serializedBypasses = new JsonArray();
        snapshot.bypassed().stream()
                .sorted()
                .forEach(uuid -> serializedBypasses.add(uuid.toString()));
        root.add("bypassed", serializedBypasses);

        JsonArray serializedExceptions = new JsonArray();
        snapshot.excepted().stream()
                .sorted(Comparator
                        .comparing((BannedExceptedBlock block) -> block.dimension)
                        .thenComparingInt(block -> block.x)
                        .thenComparingInt(block -> block.y)
                        .thenComparingInt(block -> block.z))
                .forEach(block -> serializedExceptions.add(GSON.toJsonTree(block)));
        root.add("excepted", serializedExceptions);

        JsonObject settings = new JsonObject();
        settings.addProperty("enabledItems", snapshot.enabledItems());
        settings.addProperty("enabledBlocks", snapshot.enabledBlocks());
        settings.addProperty("dropOnDestroy", snapshot.dropOnDestroy());
        settings.addProperty("radiusOverride", snapshot.radiusOverride());
        settings.addProperty("intervalOverride", snapshot.intervalOverride());
        root.add("settings", settings);
        return root;
    }

    private static ParseResult parse(JsonElement root) {
        if (root == null || root.isJsonNull()) {
            throw new IllegalArgumentException("Banned item data is empty");
        }
        long now = System.currentTimeMillis();
        if (root.isJsonArray()) {
            if (root.getAsJsonArray().size() > MAXIMUM_ENTRIES) {
                throw new IllegalArgumentException("Banned item entry limit exceeded");
            }
            Map<String, BannedEntry> legacyEntries = new LinkedHashMap<>();
            for (JsonElement element : root.getAsJsonArray()) {
                if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                    throw new IllegalArgumentException("Legacy banned item entry is invalid");
                }
                String pattern = validatePattern(element.getAsString());
                String key = pattern.toLowerCase(Locale.ROOT);
                if (legacyEntries.putIfAbsent(
                        key,
                        new BannedEntry(pattern, "", "Console", -1L, false)) != null) {
                    throw new IllegalArgumentException("Duplicate banned item pattern");
                }
            }
            return new ParseResult(
                    new Snapshot(legacyEntries, Set.of(), List.of(), true, true, false, -1, -1),
                    true);
        }
        if (!root.isJsonObject()) {
            throw new IllegalArgumentException("Banned item data is not an object");
        }

        JsonObject object = root.getAsJsonObject();
        JsonObject serializedEntries = requireObject(object, "entries");
        JsonArray serializedBypasses = requireArray(object, "bypassed");
        JsonArray serializedExceptions = requireArray(object, "excepted");
        JsonObject settings = requireObject(object, "settings");
        if (serializedEntries.size() > MAXIMUM_ENTRIES
                || serializedBypasses.size() > MAXIMUM_BYPASSES
                || serializedExceptions.size() > MAXIMUM_EXCEPTIONS) {
            throw new IllegalArgumentException("Banned item collection limit exceeded");
        }

        boolean normalized = false;
        Map<String, BannedEntry> parsedEntries = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> serialized : serializedEntries.entrySet()) {
            BannedEntry entry = GSON.fromJson(serialized.getValue(), BannedEntry.class);
            EntryValidation validation = validate(entry, now);
            String key = validation.entry().pattern.toLowerCase(Locale.ROOT);
            if (!serialized.getKey().equals(key)) {
                throw new IllegalArgumentException("Banned item key does not match its pattern");
            }
            normalized |= validation.normalized();
            if (validation.entry().isExpired()) {
                normalized = true;
                continue;
            }
            if (parsedEntries.putIfAbsent(key, validation.entry()) != null) {
                throw new IllegalArgumentException("Duplicate banned item pattern");
            }
        }

        Set<UUID> parsedBypasses = new LinkedHashSet<>();
        for (JsonElement serialized : serializedBypasses) {
            if (!serialized.isJsonPrimitive() || !serialized.getAsJsonPrimitive().isString()) {
                throw new IllegalArgumentException("Banned item bypass is invalid");
            }
            if (!parsedBypasses.add(UUID.fromString(serialized.getAsString()))) {
                throw new IllegalArgumentException("Duplicate banned item bypass");
            }
        }

        List<BannedExceptedBlock> parsedExceptions = new ArrayList<>();
        for (JsonElement serialized : serializedExceptions) {
            BannedExceptedBlock block = GSON.fromJson(serialized, BannedExceptedBlock.class);
            BannedExceptedBlock validated = validate(block, now);
            normalized |= validated != block;
            parsedExceptions.add(validated);
        }

        boolean parsedItemsEnabled = requireBoolean(settings, "enabledItems");
        boolean parsedBlocksEnabled = requireBoolean(settings, "enabledBlocks");
        boolean parsedDropOnDestroy = requireBoolean(settings, "dropOnDestroy");
        int parsedRadius = requireInt(settings, "radiusOverride");
        int parsedInterval = requireInt(settings, "intervalOverride");
        validateOverride(parsedRadius, MAXIMUM_SCAN_RADIUS, "radius");
        validateOverride(parsedInterval, MAXIMUM_SCAN_INTERVAL, "interval");

        return new ParseResult(
                new Snapshot(
                        parsedEntries,
                        parsedBypasses,
                        parsedExceptions,
                        parsedItemsEnabled,
                        parsedBlocksEnabled,
                        parsedDropOnDestroy,
                        parsedRadius,
                        parsedInterval),
                normalized);
    }

    private static EntryValidation validate(BannedEntry source, long now) {
        if (source == null) {
            throw new IllegalArgumentException("Banned item entry is null");
        }
        String pattern = validatePattern(source.pattern);
        validateText(source.reason, "reason", 1_024, true);
        validateText(source.bannedBy, "banned by", 128, false);
        boolean normalized = false;
        long duration = source.durationMs;
        if (duration == 0L) {
            duration = -1L;
            normalized = true;
        }
        validateDuration(duration);
        long created = source.bannedAtMillis;
        if (created == 0L) {
            created = now;
            normalized = true;
        }
        if (created < 0L || created > now + Duration.ofDays(1).toMillis()) {
            throw new IllegalArgumentException("Banned item timestamp is outside bounds");
        }
        if (duration > 0L) {
            Math.addExact(created, duration);
        }
        if (!normalized && pattern.equals(source.pattern)) {
            return new EntryValidation(source, false);
        }
        BannedEntry entry = copy(source);
        entry.pattern = pattern;
        entry.durationMs = duration;
        entry.bannedAtMillis = created;
        return new EntryValidation(entry, true);
    }

    private static BannedExceptedBlock validate(BannedExceptedBlock source, long now) {
        if (source == null || ResourceLocation.tryParse(source.dimension) == null) {
            throw new IllegalArgumentException("Banned block exception dimension is invalid");
        }
        if (Math.abs((long) source.x) > 30_000_000L
                || source.y < -4_096
                || source.y > 4_096
                || Math.abs((long) source.z) > 30_000_000L) {
            throw new IllegalArgumentException("Banned block exception position is outside bounds");
        }
        if (source.itemId != null
                && !source.itemId.isBlank()
                && ResourceLocation.tryParse(source.itemId) == null) {
            throw new IllegalArgumentException("Banned block exception item is invalid");
        }
        validateText(source.addedBy, "exception owner", 128, true);
        if (source.addedAtMillis < 0L || source.addedAtMillis > now + Duration.ofDays(1).toMillis()) {
            throw new IllegalArgumentException("Banned block exception timestamp is outside bounds");
        }
        if (source.addedAtMillis != 0L) {
            return source;
        }
        BannedExceptedBlock normalized = copy(source);
        normalized.addedAtMillis = now;
        return normalized;
    }

    private static String validatePattern(String source) {
        String pattern = Objects.requireNonNull(source, "pattern").strip();
        if (pattern.isEmpty() || pattern.length() > 256) {
            throw new IllegalArgumentException("Banned item pattern is outside bounds");
        }
        if (pattern.startsWith("#")) {
            if (ResourceLocation.tryParse(pattern.substring(1)) == null) {
                throw new IllegalArgumentException("Banned item tag is invalid");
            }
            return pattern;
        }
        if (pattern.endsWith(":*")) {
            String namespace = pattern.substring(0, pattern.length() - 2);
            if (ResourceLocation.tryParse(namespace + ":placeholder") == null) {
                throw new IllegalArgumentException("Banned item namespace is invalid");
            }
            return pattern;
        }
        if (ResourceLocation.tryParse(pattern) == null) {
            throw new IllegalArgumentException("Banned item identifier is invalid");
        }
        return pattern;
    }

    private static void validateDuration(long durationMillis) {
        if (durationMillis == -1L) {
            return;
        }
        if (durationMillis <= 0L || durationMillis > MAXIMUM_DURATION_MILLIS) {
            throw new IllegalArgumentException("Banned item duration is outside bounds");
        }
    }

    private static void validateOverride(int value, int maximum, String field) {
        if (value != -1 && (value < 1 || value > maximum)) {
            throw new IllegalArgumentException("Banned item " + field + " override is outside bounds");
        }
    }

    private static void validateText(
            String value,
            String field,
            int maximumLength,
            boolean allowBlank
    ) {
        String safe = Objects.requireNonNull(value, field);
        if ((!allowBlank && safe.isBlank()) || safe.length() > maximumLength) {
            throw new IllegalArgumentException("Banned item " + field + " is outside bounds");
        }
        if (safe.chars().anyMatch(character -> Character.isISOControl(character)
                && character != '\n'
                && character != '\t')) {
            throw new IllegalArgumentException("Banned item " + field + " contains control characters");
        }
    }

    private static JsonObject requireObject(JsonObject parent, String field) {
        JsonElement element = parent.get(field);
        if (element == null || !element.isJsonObject()) {
            throw new IllegalArgumentException("Banned item " + field + " is not an object");
        }
        return element.getAsJsonObject();
    }

    private static JsonArray requireArray(JsonObject parent, String field) {
        JsonElement element = parent.get(field);
        if (element == null || !element.isJsonArray()) {
            throw new IllegalArgumentException("Banned item " + field + " is not an array");
        }
        return element.getAsJsonArray();
    }

    private static boolean requireBoolean(JsonObject parent, String field) {
        JsonElement element = parent.get(field);
        if (element == null
                || !element.isJsonPrimitive()
                || !element.getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException("Banned item " + field + " is not a boolean");
        }
        return element.getAsBoolean();
    }

    private static int requireInt(JsonObject parent, String field) {
        JsonElement element = parent.get(field);
        if (element == null
                || !element.isJsonPrimitive()
                || !element.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException("Banned item " + field + " is not an integer");
        }
        int result = element.getAsInt();
        if (element.getAsDouble() != result) {
            throw new IllegalArgumentException("Banned item " + field + " is not an integer");
        }
        return result;
    }

    private static BannedEntry copy(BannedEntry source) {
        BannedEntry copy = new BannedEntry();
        copy.pattern = source.pattern;
        copy.reason = source.reason;
        copy.bannedBy = source.bannedBy;
        copy.bannedAtMillis = source.bannedAtMillis;
        copy.durationMs = source.durationMs;
        copy.announce = source.announce;
        return copy;
    }

    private static BannedExceptedBlock copy(BannedExceptedBlock source) {
        BannedExceptedBlock copy = new BannedExceptedBlock();
        copy.dimension = source.dimension;
        copy.x = source.x;
        copy.y = source.y;
        copy.z = source.z;
        copy.itemId = source.itemId;
        copy.addedBy = source.addedBy;
        copy.addedAtMillis = source.addedAtMillis;
        return copy;
    }

    private synchronized void writableForMutation() {
        requireAvailable();
    }

    private synchronized void requireAvailable() {
        if (!available()) {
            throw new IllegalStateException("Banned item storage is unavailable in " + state + " state");
        }
    }

    private synchronized void writable() throws IOException {
        if (state == RepositoryState.RECOVERY
                || state == RepositoryState.UNSUPPORTED
                || state == RepositoryState.ERROR
                || state == RepositoryState.CLOSED) {
            throw new IOException("Banned item repository is not writable in " + state + " state");
        }
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
                .orElse(RepositoryState.ERROR);
    }

    private record Snapshot(
            Map<String, BannedEntry> entries,
            Set<UUID> bypassed,
            List<BannedExceptedBlock> excepted,
            boolean enabledItems,
            boolean enabledBlocks,
            boolean dropOnDestroy,
            int radiusOverride,
            int intervalOverride
    ) {
    }

    private record ParseResult(Snapshot snapshot, boolean normalized) {
    }

    private record EntryValidation(BannedEntry entry, boolean normalized) {
    }

    // ── Suggestion helpers (used by Brigadier) ──────────────────────────────

    public List<String> suggestAllItems() {
        List<String> out = new ArrayList<>(BuiltInRegistries.ITEM.keySet().size());
        for (ResourceLocation rl : BuiltInRegistries.ITEM.keySet()) out.add(rl.toString());
        return out;
    }
}
