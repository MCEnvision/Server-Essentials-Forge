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
        filePath = directory.resolve("alt_data.json");
        saltPath = directory.resolve("alt_tracking.salt");
        addressMap.clear();
        uuidToAddress.clear();
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

    public void save() {
        if (filePath == null) return;
        try {
            StorageService.write(
                    filePath,
                    "alternate account correlations",
                    1,
                    GSON.toJsonTree(addressMap, DATA_TYPE),
                    document,
                    Set.of(""));
        } catch (IOException exception) {
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to save alternate account data", exception);
        }
    }

    public void recordLogin(ServerPlayer player) {
        if (!ConfigHandler.config.altTrackingCollectAddresses.get()) return;
        String address = player.getIpAddress();
        if (AltAddressPrivacy.isLocal(address)) return;

        String key;
        try {
            key = ConfigHandler.config.altTrackingHashAddresses.get()
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

        String uuid = player.getUUID().toString();
        String name = player.getGameProfile().getName();
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
        uuidToAddress.put(player.getUUID(), key);
        purgeExpired();
        save();
    }

    public List<AltEntry> getAltsForPlayer(UUID playerUUID) {
        String address = uuidToAddress.get(playerUUID);
        if (address == null) return Collections.emptyList();
        return List.copyOf(addressMap.getOrDefault(address, Collections.emptyList()));
    }

    public String getAddressDisplay(UUID playerUUID, boolean mayViewRawAddress) {
        String address = uuidToAddress.get(playerUUID);
        if (address == null) return "unknown";
        if (mayViewRawAddress && !AltAddressPrivacy.isHashed(address)) return address;
        return AltAddressPrivacy.redact(address);
    }

    public int purgeAll() {
        int removed = addressMap.values().stream().mapToInt(List::size).sum();
        addressMap.clear();
        uuidToAddress.clear();
        save();
        return removed;
    }

    public int purgeExpiredRecords() {
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
            byte[] stored = Files.readAllBytes(saltPath);
            if (stored.length >= 32) {
                salt = stored;
                return salt;
            }
        }
        byte[] generated = new byte[32];
        new SecureRandom().nextBytes(generated);
        AtomicFileStore.write(saltPath, generated);
        salt = generated;
        return salt;
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
