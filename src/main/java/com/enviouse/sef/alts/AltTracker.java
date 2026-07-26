package com.enviouse.sef.alts;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.storage.AtomicFileStore;
import com.enviouse.sef.storage.StorageService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Opt in, retention bounded alternate account correlation.
 */
public class AltTracker {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type DATA_TYPE = new TypeToken<Map<String, List<AltEntry>>>() {}.getType();
    private static final int MAX_ADDRESSES = 100_000;
    private static final int MAX_PROFILES_PER_ADDRESS = 32;

    private final Map<String, List<AltEntry>> addressMap = new LinkedHashMap<>();
    private final Map<UUID, String> uuidToAddress = new HashMap<>();
    private Path filePath;
    private Path saltPath;
    private StorageService.Document document;
    private byte[] salt;
    private ExecutorService writer;
    private long generation;
    private long persistedGeneration;
    private boolean writeScheduled;

    public static class AltEntry {
        public String uuid;
        public String name;
        public String lastSeen;

        public AltEntry() {
        }

        public AltEntry(String uuid, String name, String lastSeen) {
            this.uuid = uuid;
            this.name = name;
            this.lastSeen = lastSeen;
        }
    }

    public void load(MinecraftServer server) {
        Path directory = server.getServerDirectory().resolve("serverconfig").resolve("sef");
        load(directory);
    }

    void load(Path directory) {
        stopWriter();
        synchronized (this) {
            filePath = directory.resolve("alt_data.json");
            saltPath = directory.resolve("alt_tracking.salt");
            salt = null;
            generation = 0L;
            persistedGeneration = 0L;
            writeScheduled = false;
            addressMap.clear();
            uuidToAddress.clear();
            startWriter();
            document = StorageService.read(filePath, "alternate account correlations", 1).orElse(null);
            if (document == null) return;
            try {
                boolean convertedRawAddress = false;
                Map<String, List<AltEntry>> loaded = GSON.fromJson(document.data(), DATA_TYPE);
                if (loaded != null) {
                    boolean hash = ConfigHandler.config.altTrackingHashAddresses.get();
                    for (Map.Entry<String, List<AltEntry>> stored : loaded.entrySet()) {
                        if (addressMap.size() >= MAX_ADDRESSES) break;
                        boolean convert = hash && !AltAddressPrivacy.isHashed(stored.getKey());
                        String key = convert ? hashAddress(stored.getKey()) : stored.getKey();
                        convertedRawAddress |= convert;
                        List<AltEntry> destination = addressMap.computeIfAbsent(key, ignored -> new ArrayList<>());
                        if (stored.getValue() != null) {
                            for (AltEntry entry : stored.getValue()) {
                                if (destination.size() >= MAX_PROFILES_PER_ADDRESS) break;
                                if (valid(entry)) destination.add(entry);
                            }
                        }
                    }
                }
                if (ConfigHandler.config.altTrackingHashAddresses.get()
                        && addressMap.keySet().stream().anyMatch(AltAddressPrivacy::isHashed)) {
                    loadExistingSalt();
                }
                boolean changed = purgeExpired();
                rebuildReverseIndex();
                ServerEssentialsForge.LOGGER.info(
                        "[SEF] Loaded alternate account correlations for {} address key(s)",
                        addressMap.size());
                if (document.migrated() || changed || convertedRawAddress) {
                    save();
                }
            } catch (Exception exception) {
                addressMap.clear();
                uuidToAddress.clear();
                ServerEssentialsForge.LOGGER.error("[SEF] Failed to load alternate account data", exception);
            }
        }
    }

    public synchronized void save() {
        markDirty();
    }

    public void recordLogin(ServerPlayer player) {
        if (!ConfigHandler.config.altTrackingCollectAddresses.get()) return;
        recordLogin(
                player.getUUID(),
                player.getGameProfile().getName(),
                player.getIpAddress(),
                ConfigHandler.config.altTrackingHashAddresses.get());
    }

    synchronized void recordLogin(UUID playerId, String name, String address, boolean hashAddress) {
        if (AltAddressPrivacy.isLocal(address)) return;

        String key;
        try {
            key = hashAddress
                    ? hashAddress(address)
                    : address;
        } catch (RuntimeException exception) {
            ServerEssentialsForge.LOGGER.error(
                    "[SEF] Could not record alternate account correlation because privacy storage is unavailable",
                    exception);
            return;
        }
        if (!addressMap.containsKey(key) && addressMap.size() >= MAX_ADDRESSES) {
            ServerEssentialsForge.LOGGER.warn("[SEF] Alternate account address limit reached");
            return;
        }

        String uuid = playerId.toString();
        String now = Instant.now().toString();
        List<AltEntry> entries = addressMap.computeIfAbsent(key, ignored -> new ArrayList<>());
        AltEntry existing = entries.stream()
                .filter(entry -> uuid.equals(entry.uuid))
                .findFirst()
                .orElse(null);
        if (existing != null) {
            existing.name = name;
            existing.lastSeen = now;
        } else if (entries.size() < MAX_PROFILES_PER_ADDRESS) {
            entries.add(new AltEntry(uuid, name, now));
        }
        uuidToAddress.put(playerId, key);
        purgeExpired();
        markDirty();
    }

    public synchronized List<AltEntry> getAltsForPlayer(UUID playerUUID) {
        String address = uuidToAddress.get(playerUUID);
        if (address == null) return Collections.emptyList();
        return List.copyOf(addressMap.getOrDefault(address, Collections.emptyList()));
    }

    public synchronized String getAddressDisplay(UUID playerUUID, boolean mayViewRawAddress) {
        String address = uuidToAddress.get(playerUUID);
        if (address == null) return "unknown";
        if (mayViewRawAddress && !AltAddressPrivacy.isHashed(address)) return address;
        return AltAddressPrivacy.redact(address);
    }

    public synchronized int purgeAll() {
        int removed = addressMap.values().stream().mapToInt(List::size).sum();
        addressMap.clear();
        uuidToAddress.clear();
        save();
        return removed;
    }

    public synchronized int purgeExpiredRecords() {
        int before = addressMap.values().stream().mapToInt(List::size).sum();
        if (purgeExpired()) save();
        int after = addressMap.values().stream().mapToInt(List::size).sum();
        return before - after;
    }

    public synchronized JsonObject buildExport(boolean includeRawAddresses) {
        JsonArray groups = new JsonArray();
        for (Map.Entry<String, List<AltEntry>> group : addressMap.entrySet()) {
            JsonObject object = new JsonObject();
            String address = includeRawAddresses && !AltAddressPrivacy.isHashed(group.getKey())
                    ? group.getKey()
                    : AltAddressPrivacy.redact(group.getKey());
            object.addProperty("address", address);
            object.add("profiles", GSON.toJsonTree(group.getValue()));
            groups.add(object);
        }
        JsonObject data = new JsonObject();
        data.addProperty("exportedAt", Instant.now().toString());
        data.add("groups", groups);
        return data;
    }

    public void flush() {
        ExecutorService current;
        synchronized (this) {
            if (writer == null || writer.isShutdown()) {
                return;
            }
            if (persistedGeneration < generation && !writeScheduled) {
                scheduleWrite();
            }
            current = writer;
        }
        try {
            Future<?> barrier = current.submit(() -> {
            });
            barrier.get(5L, TimeUnit.SECONDS);
        } catch (RejectedExecutionException exception) {
            ServerEssentialsForge.LOGGER.error("[SEF] Alternate account writer rejected a flush", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException exception) {
            ServerEssentialsForge.LOGGER.error("[SEF] Alternate account writer flush failed", exception);
        }
    }

    public void shutdown() {
        flush();
        stopWriter();
    }

    public static Path writeExport(Path directory, JsonObject data) throws IOException {
        Path target = directory.resolve("alt_data_export_" + System.currentTimeMillis() + ".json");
        StorageService.write(target, "alternate account export", 1, data, null);
        return target;
    }

    private boolean purgeExpired() {
        Instant cutoff = Instant.now().minus(
                Math.max(1, ConfigHandler.config.altTrackingRetentionDays.get()),
                ChronoUnit.DAYS);
        boolean changed = false;
        Iterator<Map.Entry<String, List<AltEntry>>> groups = addressMap.entrySet().iterator();
        while (groups.hasNext()) {
            List<AltEntry> entries = groups.next().getValue();
            changed |= entries.removeIf(entry -> lastSeen(entry).isBefore(cutoff));
            if (entries.isEmpty()) {
                groups.remove();
                changed = true;
            }
        }
        if (changed) rebuildReverseIndex();
        return changed;
    }

    private void rebuildReverseIndex() {
        uuidToAddress.clear();
        for (Map.Entry<String, List<AltEntry>> group : addressMap.entrySet()) {
            for (AltEntry entry : group.getValue()) {
                try {
                    uuidToAddress.put(UUID.fromString(entry.uuid), group.getKey());
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    private String hashAddress(String address) {
        try {
            return AltAddressPrivacy.hash(address, loadOrCreateSalt());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not initialize alternate account privacy salt", exception);
        }
    }

    private byte[] loadOrCreateSalt() throws IOException {
        if (salt != null) return salt;
        if (Files.exists(saltPath)) {
            return loadExistingSalt();
        }
        byte[] generated = new byte[32];
        new SecureRandom().nextBytes(generated);
        AtomicFileStore.write(saltPath, generated);
        salt = generated;
        return salt;
    }

    private byte[] loadExistingSalt() throws IOException {
        if (!Files.isRegularFile(saltPath)) {
            throw new IOException("Alternate account privacy salt is missing");
        }
        byte[] stored = Files.readAllBytes(saltPath);
        if (stored.length != 32) {
            throw new IOException("Alternate account privacy salt is invalid");
        }
        salt = stored;
        return salt;
    }

    private synchronized void markDirty() {
        generation++;
        scheduleWrite();
    }

    private void scheduleWrite() {
        if (writeScheduled || writer == null || writer.isShutdown()) {
            return;
        }
        writeScheduled = true;
        try {
            writer.execute(this::writeLoop);
        } catch (RejectedExecutionException exception) {
            writeScheduled = false;
            ServerEssentialsForge.LOGGER.error("[SEF] Alternate account writer rejected a save", exception);
        }
    }

    private void writeLoop() {
        while (true) {
            Snapshot snapshot;
            synchronized (this) {
                if (persistedGeneration >= generation) {
                    writeScheduled = false;
                    return;
                }
                snapshot = snapshot();
            }
            try {
                StorageService.write(
                        snapshot.filePath(),
                        "alternate account correlations",
                        1,
                        GSON.toJsonTree(snapshot.addresses(), DATA_TYPE),
                        snapshot.document(),
                        Set.of(""));
            } catch (IOException exception) {
                synchronized (this) {
                    writeScheduled = false;
                }
                ServerEssentialsForge.LOGGER.error("[SEF] Failed to save alternate account data", exception);
                return;
            }
            synchronized (this) {
                persistedGeneration = Math.max(persistedGeneration, snapshot.generation());
            }
        }
    }

    private Snapshot snapshot() {
        Map<String, List<AltEntry>> copy = new LinkedHashMap<>();
        addressMap.forEach((address, entries) -> copy.put(
                address,
                entries.stream()
                        .map(entry -> new AltEntry(entry.uuid, entry.name, entry.lastSeen))
                        .toList()));
        return new Snapshot(filePath, document, generation, Map.copyOf(copy));
    }

    private void startWriter() {
        writer = Executors.newSingleThreadExecutor(
                runnable -> Thread.ofPlatform()
                        .daemon(true)
                        .name("sef-alt-storage")
                        .unstarted(runnable));
    }

    private void stopWriter() {
        ExecutorService current;
        synchronized (this) {
            current = writer;
            writer = null;
            writeScheduled = false;
        }
        if (current == null) {
            return;
        }
        current.shutdown();
        try {
            if (!current.awaitTermination(5L, TimeUnit.SECONDS)) {
                current.shutdownNow();
                ServerEssentialsForge.LOGGER.error("[SEF] Alternate account writer did not stop cleanly");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            current.shutdownNow();
        }
    }

    synchronized int addressCount() {
        return addressMap.size();
    }

    private record Snapshot(
            Path filePath,
            StorageService.Document document,
            long generation,
            Map<String, List<AltEntry>> addresses
    ) {
    }

    private static boolean valid(AltEntry entry) {
        if (entry == null || entry.uuid == null || entry.name == null || entry.lastSeen == null) return false;
        try {
            UUID.fromString(entry.uuid);
            Instant.parse(entry.lastSeen);
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private static Instant lastSeen(AltEntry entry) {
        try {
            return Instant.parse(entry.lastSeen);
        } catch (RuntimeException exception) {
            return Instant.EPOCH;
        }
    }
}
