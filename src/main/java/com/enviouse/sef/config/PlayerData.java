package com.enviouse.sef.config;

import com.enviouse.sef.commands.NicknamePolicy;
import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.ImportDiagnostics;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class PlayerData {
    private static final System.Logger LOGGER = System.getLogger("sef.playerdata");
    private static final int MAXIMUM_PROFILES = 100_000;
    public static final Map<UUID, PlayerData> map = new LinkedHashMap<>();
    public static final String playerDataFileName = "sef.playerdata.json";
    public static final String legacyPlayerDataFileName = "sef.playerdata";

    public final UUID uuid;
    public String username;
    public String nickname;
    public String updatedAt;

    private static Path dataFile;
    private static Path loadedDirectory;
    private static StorageService.Document document;
    private static StorageRepository.RepositoryState state = StorageRepository.RepositoryState.NEW;
    private static String stateDetail = "not loaded";

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.updatedAt = Instant.now().toString();
    }

    public static synchronized boolean setNickname(UUID uuid, String nickname) {
        Objects.requireNonNull(uuid, "uuid");
        if (nickname != null && (nickname.length() > 1_024
                || nickname.codePoints().anyMatch(Character::isISOControl))) {
            return false;
        }
        boolean created = !map.containsKey(uuid);
        PlayerData data = getOrCreate(uuid);
        if (data == null) return false;
        String previousNickname = data.nickname;
        String previousUpdatedAt = data.updatedAt;
        data.nickname = nickname;
        data.updatedAt = Instant.now().toString();
        if (saveCurrent()) return true;
        if (created) map.remove(uuid);
        else {
            data.nickname = previousNickname;
            data.updatedAt = previousUpdatedAt;
        }
        return false;
    }

    public static synchronized boolean setNicknameInMemory(UUID uuid, String nickname) {
        Objects.requireNonNull(uuid, "uuid");
        if (nickname != null && (nickname.length() > 1_024
                || nickname.codePoints().anyMatch(Character::isISOControl))) {
            return false;
        }
        if (state == StorageRepository.RepositoryState.RECOVERY
                || state == StorageRepository.RepositoryState.UNSUPPORTED
                || state == StorageRepository.RepositoryState.ERROR) {
            return false;
        }
        PlayerData data = getOrCreate(uuid);
        if (data == null) {
            return false;
        }
        if (!Objects.equals(data.nickname, nickname)) {
            data.nickname = nickname;
            data.updatedAt = Instant.now().toString();
        }
        return true;
    }

    public static synchronized boolean rememberProfile(UUID uuid, String username) {
        if (uuid == null || username == null || username.isBlank()
                || username.length() > 64
                || username.codePoints().anyMatch(Character::isISOControl)) return false;
        boolean created = !map.containsKey(uuid);
        PlayerData data = getOrCreate(uuid);
        if (data == null) return false;
        if (!username.equals(data.username)) {
            String previousUsername = data.username;
            String previousUpdatedAt = data.updatedAt;
            data.username = username;
            data.updatedAt = Instant.now().toString();
            if (!saveCurrent()) {
                if (created) map.remove(uuid);
                else {
                    data.username = previousUsername;
                    data.updatedAt = previousUpdatedAt;
                }
                return false;
            }
        }
        return true;
    }

    public static synchronized boolean rememberProfileInMemory(UUID uuid, String username) {
        if (uuid == null || username == null || username.isBlank()
                || username.length() > 64
                || username.codePoints().anyMatch(Character::isISOControl)) {
            return false;
        }
        if (state == StorageRepository.RepositoryState.RECOVERY
                || state == StorageRepository.RepositoryState.UNSUPPORTED
                || state == StorageRepository.RepositoryState.ERROR) {
            return false;
        }
        PlayerData data = getOrCreate(uuid);
        if (data == null) {
            return false;
        }
        if (!username.equals(data.username)) {
            data.username = username;
            data.updatedAt = Instant.now().toString();
        }
        return true;
    }

    public static synchronized String getNickname(UUID id) {
        PlayerData data = map.get(id);
        return data == null ? null : data.nickname;
    }

    public static synchronized String getUsername(UUID id) {
        PlayerData data = map.get(id);
        return data == null ? null : data.username;
    }

    public static synchronized Optional<ProfileSnapshot> profile(UUID id) {
        PlayerData data = map.get(id);
        return data == null ? Optional.empty() : Optional.of(data.snapshot());
    }

    public static synchronized List<ProfileSnapshot> profiles() {
        return map.values().stream()
                .map(PlayerData::snapshot)
                .sorted(java.util.Comparator.comparing(snapshot -> snapshot.playerId().toString()))
                .toList();
    }

    public static synchronized ProfileDiagnostic diagnostic() {
        return new ProfileDiagnostic(dataFile, state, stateDetail, map.size());
    }

    public static synchronized void unload() {
        map.clear();
        dataFile = null;
        loadedDirectory = null;
        document = null;
        state = StorageRepository.RepositoryState.CLOSED;
        stateDetail = "closed";
    }

    public static synchronized UUID whoIs(String identity) {
        return findIdentity(identity, true).orElse(null);
    }

    public static synchronized Optional<UUID> findIdentity(String identity, boolean includeNicknames) {
        String normalized = NicknamePolicy.normalizeIdentity(identity);
        if (normalized.isEmpty()) return Optional.empty();
        UUID usernameMatch = uniqueMatch(normalized, false);
        if (usernameMatch != null) return Optional.of(usernameMatch);
        if (!includeNicknames) return Optional.empty();
        return Optional.ofNullable(uniqueMatch(normalized, true));
    }

    public static synchronized boolean hasIdentityCollision(
            UUID target,
            String normalizedIdentity,
            boolean includeKnownNicknames
    ) {
        for (PlayerData data : map.values()) {
            if (data.uuid.equals(target)) continue;
            if (normalizedIdentity.equals(NicknamePolicy.normalizeIdentity(data.username))) return true;
            if (includeKnownNicknames && data.nickname != null
                    && normalizedIdentity.equals(NicknamePolicy.normalizeIdentity(
                    NicknamePolicy.stripFormatting(data.nickname)))) {
                return true;
            }
        }
        return false;
    }

    public static synchronized void loadFromDir(File playerDirectory) {
        Path directory = playerDirectory.toPath().toAbsolutePath().normalize();
        if (directory.equals(loadedDirectory)) return;
        loadedDirectory = directory;
        dataFile = directory.resolve(playerDataFileName);
        map.clear();
        document = null;
        state = StorageRepository.RepositoryState.NEW;
        stateDetail = "loading";

        if (Files.exists(dataFile)) {
            loadJson();
            return;
        }

        Path legacy = directory.resolve(legacyPlayerDataFileName);
        if (!Files.exists(legacy)) {
            state = StorageRepository.RepositoryState.MISSING;
            stateDetail = "new repository";
            return;
        }
        try {
            StorageService.recordExternalMigration(legacy, "integrated player identities", 1);
            parseLegacy(Files.readString(legacy, StandardCharsets.UTF_8));
            boolean saved = saveCurrent();
            ImportDiagnostics.record(
                    "integrated player identities",
                    legacy,
                    saved ? ImportDiagnostics.Result.SUCCESS : ImportDiagnostics.Result.FAILED,
                    map.size(),
                    saved ? "legacy nickname data migrated" : "legacy nickname data could not be persisted");
            if (saved) {
                LOGGER.log(
                        System.Logger.Level.WARNING,
                        "[SEF] Migrated legacy nickname data to " + dataFile.getFileName()
                                + ". Existing explicit permission grants are unchanged");
            } else {
                state = StorageRepository.RepositoryState.ERROR;
                stateDetail = "legacy nickname data could not be persisted";
                LOGGER.log(
                        System.Logger.Level.ERROR,
                        "[SEF] Legacy nickname data was read but could not be persisted");
            }
        } catch (IOException | RuntimeException exception) {
            map.clear();
            state = StorageRepository.RepositoryState.ERROR;
            stateDetail = exception.getClass().getSimpleName();
            ImportDiagnostics.record(
                    "integrated player identities",
                    legacy,
                    ImportDiagnostics.Result.FAILED,
                    0,
                    exception.getClass().getSimpleName());
            LOGGER.log(System.Logger.Level.ERROR, "[SEF] Failed to migrate legacy nickname data", exception);
        }
    }

    public static synchronized boolean saveToDir(File playerDirectory) {
        Path directory = playerDirectory.toPath().toAbsolutePath().normalize();
        if (!directory.equals(loadedDirectory)) {
            loadFromDir(playerDirectory);
        }
        return saveCurrent();
    }

    private static void loadJson() {
        document = StorageService.read(dataFile, "integrated player identities", 1).orElse(null);
        if (document == null) {
            state = stateFromStorageStatus(dataFile);
            stateDetail = "storage unavailable";
            return;
        }
        if (!document.data().isJsonObject()) {
            state = StorageRepository.RepositoryState.RECOVERY;
            stateDetail = "player profile data is not an object";
            return;
        }
        try {
            JsonObject players = document.data().getAsJsonObject().getAsJsonObject("players");
            if (players == null) {
                throw new IllegalStateException("Integrated player profiles collection is missing");
            }
            if (players.size() > MAXIMUM_PROFILES) {
                throw new IllegalStateException("Integrated player profile limit exceeded");
            }
            for (Map.Entry<String, JsonElement> entry : players.entrySet()) {
                UUID uuid = UUID.fromString(entry.getKey());
                if (!entry.getValue().isJsonObject()) {
                    throw new IllegalStateException("Integrated player profile is not an object");
                }
                JsonObject object = entry.getValue().getAsJsonObject();
                PlayerData data = new PlayerData(uuid);
                data.username = nullableBoundedString(object, "username", 64);
                data.nickname = nullableBoundedString(object, "nickname", 1_024);
                String updated = nullableBoundedString(object, "updatedAt", 64);
                if (updated != null) {
                    Instant.parse(updated);
                    data.updatedAt = updated;
                }
                map.put(uuid, data);
            }
            if (document.migrated()) {
                boolean saved = saveCurrent();
                ImportDiagnostics.record(
                        "integrated player identities",
                        dataFile,
                        saved ? ImportDiagnostics.Result.SUCCESS : ImportDiagnostics.Result.FAILED,
                        map.size(),
                        saved ? "unversioned json migrated" : "unversioned json could not be persisted");
            }
            LOGGER.log(
                    System.Logger.Level.INFO,
                    "[SEF] Loaded " + map.size() + " integrated player identity record or records");
            state = StorageRepository.RepositoryState.READY;
            stateDetail = "loaded " + map.size() + " player profiles";
        } catch (RuntimeException exception) {
            map.clear();
            state = StorageRepository.RepositoryState.RECOVERY;
            stateDetail = exception.getClass().getSimpleName();
            ImportDiagnostics.record(
                    "integrated player identities",
                    dataFile,
                    ImportDiagnostics.Result.FAILED,
                    0,
                    exception.getClass().getSimpleName());
            LOGGER.log(
                    System.Logger.Level.ERROR,
                    "[SEF] Failed to load integrated player identity data",
                    exception);
        }
    }

    public static synchronized Optional<PersistenceSnapshot> persistenceSnapshot() {
        return Optional.ofNullable(createPersistenceSnapshot());
    }

    public static boolean persistSnapshot(PersistenceSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        try {
            StorageService.write(
                    snapshot.path,
                    "integrated player identities",
                    1,
                    snapshot.data,
                    snapshot.previousDocument,
                    Set.of("/players"));
            synchronized (PlayerData.class) {
                if (snapshot.path.equals(dataFile)) {
                    state = StorageRepository.RepositoryState.READY;
                    stateDetail = "saved " + snapshot.profileCount + " player profiles";
                }
            }
            return true;
        } catch (IOException | RuntimeException exception) {
            synchronized (PlayerData.class) {
                if (snapshot.path.equals(dataFile)) {
                    state = StorageRepository.RepositoryState.ERROR;
                    stateDetail = exception.getClass().getSimpleName();
                }
            }
            LOGGER.log(
                    System.Logger.Level.ERROR,
                    "[SEF] Failed to save integrated player identity data",
                    exception);
            return false;
        }
    }

    private static boolean saveCurrent() {
        PersistenceSnapshot snapshot = createPersistenceSnapshot();
        return snapshot != null && persistSnapshot(snapshot);
    }

    private static PersistenceSnapshot createPersistenceSnapshot() {
        if (dataFile == null) return null;
        if (state == StorageRepository.RepositoryState.RECOVERY
                || state == StorageRepository.RepositoryState.UNSUPPORTED
                || state == StorageRepository.RepositoryState.ERROR) {
            return null;
        }
        JsonObject players = new JsonObject();
        for (PlayerData data : map.values()) {
            JsonObject object = new JsonObject();
            if (data.username == null) object.add("username", com.google.gson.JsonNull.INSTANCE);
            else object.addProperty("username", data.username);
            if (data.nickname == null) object.add("nickname", com.google.gson.JsonNull.INSTANCE);
            else object.addProperty("nickname", data.nickname);
            object.addProperty("updatedAt", data.updatedAt);
            players.add(data.uuid.toString(), object);
        }
        JsonObject root = new JsonObject();
        root.add("players", players);
        return new PersistenceSnapshot(dataFile, root, document, map.size());
    }

    static ArrayList<PlayerData> parseLegacy(String input) {
        ArrayList<PlayerData> parsed = new ArrayList<>();
        for (LegacyNicknameCodec.Entry entry : LegacyNicknameCodec.parse(input)) {
            if (parsed.size() >= MAXIMUM_PROFILES) {
                throw new IllegalStateException("Legacy player profile limit exceeded");
            }
            PlayerData data = new PlayerData(entry.uuid());
            if (entry.nickname() != null && entry.nickname().length() > 1_024) {
                throw new IllegalStateException("Legacy nickname length limit exceeded");
            }
            data.nickname = entry.nickname();
            parsed.add(data);
            map.put(entry.uuid(), data);
        }
        return parsed;
    }

    public static String encodeStr(String value) {
        if (value == null) return "null";
        return com.google.gson.JsonPrimitive.class.cast(new com.google.gson.JsonPrimitive(value)).toString();
    }

    public static String decodeStr(String value) {
        return LegacyNicknameCodec.decode(value);
    }

    private static UUID uniqueMatch(String normalized, boolean nickname) {
        UUID match = null;
        for (PlayerData data : map.values()) {
            String value = nickname
                    ? NicknamePolicy.stripFormatting(data.nickname)
                    : data.username;
            if (!normalized.equals(NicknamePolicy.normalizeIdentity(value))) continue;
            if (match != null && !match.equals(data.uuid)) return null;
            match = data.uuid;
        }
        return match;
    }

    private static String nullableBoundedString(JsonObject object, String name, int maximumLength) {
        JsonElement value = object.get(name);
        if (value == null || value.isJsonNull()) return null;
        String result = value.getAsString();
        if (result.length() > maximumLength || result.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalStateException("Player profile field is outside bounds");
        }
        return result;
    }

    private static PlayerData getOrCreate(UUID uuid) {
        PlayerData existing = map.get(uuid);
        if (existing != null) return existing;
        if (map.size() >= MAXIMUM_PROFILES) return null;
        PlayerData created = new PlayerData(uuid);
        map.put(uuid, created);
        return created;
    }

    private static StorageRepository.RepositoryState stateFromStorageStatus(Path path) {
        return StorageService.statuses().stream()
                .filter(status -> status.path().equals(path))
                .findFirst()
                .map(status -> switch (status.state()) {
                    case "missing" -> StorageRepository.RepositoryState.MISSING;
                    case "unsupported" -> StorageRepository.RepositoryState.UNSUPPORTED;
                    case "quarantined", "quarantine failed" -> StorageRepository.RepositoryState.RECOVERY;
                    default -> StorageRepository.RepositoryState.ERROR;
                })
                .orElse(StorageRepository.RepositoryState.ERROR);
    }

    private ProfileSnapshot snapshot() {
        return new ProfileSnapshot(uuid, username, nickname, updatedAt);
    }

    public record ProfileSnapshot(
            UUID playerId,
            String authenticatedUsername,
            String nickname,
            String updatedAt
    ) {
    }

    public static final class PersistenceSnapshot {
        private final Path path;
        private final JsonObject data;
        private final StorageService.Document previousDocument;
        private final int profileCount;

        private PersistenceSnapshot(
                Path path,
                JsonObject data,
                StorageService.Document previousDocument,
                int profileCount
        ) {
            this.path = path;
            this.data = data;
            this.previousDocument = previousDocument;
            this.profileCount = profileCount;
        }
    }

    public record ProfileDiagnostic(
            Path path,
            StorageRepository.RepositoryState state,
            String detail,
            int profileCount
    ) {
    }
}
