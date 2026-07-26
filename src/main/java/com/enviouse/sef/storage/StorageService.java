package com.enviouse.sef.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.concurrent.ConcurrentHashMap;

public final class StorageService {
    private static final System.Logger LOGGER = System.getLogger("sef.storage");
    public record Document(
            String domain,
            int schemaVersion,
            JsonElement data,
            JsonObject rootExtras,
            JsonElement originalData,
            boolean migrated
    ) {
    }

    public record StoreStatus(
            Path path,
            String domain,
            int schemaVersion,
            long sizeBytes,
            Instant lastLoaded,
            String state
    ) {
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final int MAX_DOCUMENT_BYTES = 16 * 1024 * 1024;
    private static final Clock CLOCK = Clock.systemUTC();
    private static final Map<Path, StoreStatus> STATUS = new ConcurrentHashMap<>();

    private StorageService() {
    }

    public static Optional<Document> read(Path path, String domain, int currentVersion) {
        Path normalized = path.toAbsolutePath().normalize();
        if (!Files.exists(normalized)) {
            STATUS.put(normalized, new StoreStatus(
                    normalized, domain, currentVersion, 0L, Instant.now(CLOCK), "missing"));
            return Optional.empty();
        }

        try {
            long size = Files.size(normalized);
            if (size > MAX_DOCUMENT_BYTES) {
                quarantine(normalized, domain, "document exceeds size limit");
                return Optional.empty();
            }

            JsonElement root = JsonParser.parseString(Files.readString(normalized, StandardCharsets.UTF_8));
            if (root == null || root.isJsonNull()) {
                quarantine(normalized, domain, "document is empty");
                return Optional.empty();
            }

            if (!isEnvelope(root)) {
                Path backup = AtomicFileStore.backup(
                        normalized,
                        normalized.getParent().resolve(".backups"),
                        "v0.bak",
                        CLOCK);
                appendMigrationJournal(normalized, domain, 0, currentVersion, backup);
                STATUS.put(normalized, new StoreStatus(
                        normalized, domain, 0, size, Instant.now(CLOCK), "legacy"));
                return Optional.of(new Document(
                        domain,
                        0,
                        root,
                        new JsonObject(),
                        root.deepCopy(),
                        true));
            }

            JsonObject object = root.getAsJsonObject();
            String storedDomain = object.get("domain").getAsString();
            int version = object.get("schemaVersion").getAsInt();
            if (!domain.equals(storedDomain)) {
                quarantine(normalized, domain, "domain mismatch");
                return Optional.empty();
            }
            if (version > currentVersion || version < 1) {
                STATUS.put(normalized, new StoreStatus(
                        normalized, domain, version, size, Instant.now(CLOCK), "unsupported"));
                LOGGER.log(
                        System.Logger.Level.ERROR,
                        "[SEF] Refusing unsupported " + domain + " storage version " + version
                                + " in " + normalized.getFileName());
                return Optional.empty();
            }

            JsonObject extras = object.deepCopy();
            extras.remove("domain");
            extras.remove("schemaVersion");
            extras.remove("data");
            JsonElement data = object.get("data");
            boolean migrated = version < currentVersion;
            if (migrated) {
                Path backup = AtomicFileStore.backup(
                        normalized,
                        normalized.getParent().resolve(".backups"),
                        "v" + version + ".bak",
                        CLOCK);
                appendMigrationJournal(normalized, domain, version, currentVersion, backup);
            }
            STATUS.put(normalized, new StoreStatus(
                    normalized,
                    domain,
                    version,
                    size,
                    Instant.now(CLOCK),
                    migrated ? "migration pending" : "ready"));
            return Optional.of(new Document(
                    domain,
                    version,
                    data,
                    extras,
                    data.deepCopy(),
                    migrated));
        } catch (Exception exception) {
            quarantine(normalized, domain, exception.getClass().getSimpleName());
            return Optional.empty();
        }
    }

    public static void write(
            Path path,
            String domain,
            int schemaVersion,
            JsonElement data,
            Document previous
    ) throws IOException {
        write(path, domain, schemaVersion, data, previous, Set.of());
    }

    public static void write(
            Path path,
            String domain,
            int schemaVersion,
            JsonElement data,
            Document previous,
            Set<String> dynamicObjectPaths
    ) throws IOException {
        Path normalized = path.toAbsolutePath().normalize();
        JsonObject root = previous == null ? new JsonObject() : previous.rootExtras().deepCopy();
        root.addProperty("domain", domain);
        root.addProperty("schemaVersion", schemaVersion);
        root.add("data", previous == null
                ? data
                : mergeUnknownFields(previous.originalData(), data, "", dynamicObjectPaths));
        byte[] content = GSON.toJson(root).getBytes(StandardCharsets.UTF_8);
        AtomicFileStore.write(normalized, content);
        STATUS.put(normalized, new StoreStatus(
                normalized,
                domain,
                schemaVersion,
                content.length,
                Instant.now(CLOCK),
                "ready"));
    }

    public static List<StoreStatus> statuses() {
        return STATUS.values().stream()
                .sorted(Comparator.comparing(status -> status.path().toString()))
                .toList();
    }

    public static JsonObject buildPermissionManifest(Map<String, Boolean> entries) {
        JsonObject data = new JsonObject();
        for (Map.Entry<String, Boolean> entry : entries.entrySet()) {
            data.addProperty(entry.getKey(), entry.getValue());
        }
        return data;
    }

    public static Path exportManagedSnapshot(
            Path managedRoot,
            Path exportRoot,
            Predicate<StoreStatus> include
    ) throws IOException {
        return exportManagedSnapshot(List.of(managedRoot), exportRoot, include);
    }

    public static Path exportManagedSnapshot(
            List<Path> managedRoots,
            Path exportRoot,
            Predicate<StoreStatus> include
    ) throws IOException {
        List<Path> normalizedRoots = managedRoots.stream()
                .map(path -> path.toAbsolutePath().normalize())
                .toList();
        Path snapshot = exportRoot.resolve("storage_" + System.currentTimeMillis()).toAbsolutePath().normalize();
        if (!snapshot.startsWith(exportRoot.toAbsolutePath().normalize())) {
            throw new IOException("Storage export escaped its configured directory");
        }
        Files.createDirectories(snapshot);
        for (StoreStatus status : statuses()) {
            Path source = status.path().toAbsolutePath().normalize();
            boolean managed = normalizedRoots.stream().anyMatch(root -> source.getParent().equals(root));
            if (!managed
                    || !Files.isRegularFile(source)
                    || !include.test(status)) {
                continue;
            }
            long size = Files.size(source);
            if (size > MAX_DOCUMENT_BYTES) {
                throw new IOException("Managed document exceeds export size limit");
            }
            Path target = snapshot.resolve(source.getFileName());
            if (Files.exists(target)) {
                String prefix = status.domain().replaceAll("[^a-zA-Z0-9._]", "_");
                target = snapshot.resolve(prefix + "_" + source.getFileName());
            }
            AtomicFileStore.write(target, Files.readAllBytes(source));
        }
        return snapshot;
    }

    public static Path recordExternalMigration(Path source, String domain, int targetVersion) throws IOException {
        Path normalized = source.toAbsolutePath().normalize();
        Path backup = AtomicFileStore.backup(
                normalized,
                normalized.getParent().resolve(".backups"),
                "v0.bak",
                CLOCK);
        appendMigrationJournal(normalized, domain, 0, targetVersion, backup);
        return backup;
    }

    private static boolean isEnvelope(JsonElement root) {
        if (!root.isJsonObject()) {
            return false;
        }
        JsonObject object = root.getAsJsonObject();
        return object.has("domain")
                && object.get("domain").isJsonPrimitive()
                && object.has("schemaVersion")
                && object.get("schemaVersion").isJsonPrimitive()
                && object.has("data");
    }

    private static JsonElement mergeUnknownFields(
            JsonElement original,
            JsonElement current,
            String path,
            Set<String> dynamicObjectPaths
    ) {
        if (original == null || current == null) {
            return current;
        }
        if (original.isJsonArray() && current.isJsonArray()) {
            JsonArray oldArray = original.getAsJsonArray();
            JsonArray newArray = current.getAsJsonArray();
            JsonArray merged = new JsonArray();
            for (int index = 0; index < newArray.size(); index++) {
                JsonElement newValue = newArray.get(index);
                JsonElement oldValue = findMatchingArrayElement(oldArray, newValue, index);
                merged.add(mergeUnknownFields(
                        oldValue,
                        newValue,
                        path + "/" + index,
                        dynamicObjectPaths));
            }
            return merged;
        }
        if (!original.isJsonObject() || !current.isJsonObject()) {
            return current;
        }
        JsonObject merged = dynamicObjectPaths.contains(path)
                ? new JsonObject()
                : original.getAsJsonObject().deepCopy();
        for (Map.Entry<String, JsonElement> entry : current.getAsJsonObject().entrySet()) {
            JsonElement oldValue = original.getAsJsonObject().get(entry.getKey());
            merged.add(entry.getKey(), mergeUnknownFields(
                    oldValue,
                    entry.getValue(),
                    path + "/" + escapePathSegment(entry.getKey()),
                    dynamicObjectPaths));
        }
        return merged;
    }

    private static JsonElement findMatchingArrayElement(JsonArray original, JsonElement current, int index) {
        if (current.isJsonObject()) {
            for (String identityField : List.of("id", "uuid", "playerUUID")) {
                JsonElement id = current.getAsJsonObject().get(identityField);
                if (id != null && id.isJsonPrimitive()) {
                    for (JsonElement candidate : original) {
                        if (!candidate.isJsonObject()) continue;
                        JsonElement candidateId = candidate.getAsJsonObject().get(identityField);
                        if (id.equals(candidateId)) return candidate;
                    }
                    return null;
                }
            }
            return null;
        }
        return index < original.size() ? original.get(index) : null;
    }

    private static String escapePathSegment(String value) {
        return value.replace("~", "~0").replace("/", "~1");
    }

    private static void appendMigrationJournal(
            Path source,
            String domain,
            int fromVersion,
            int toVersion,
            Path backup
    ) throws IOException {
        JsonObject entry = new JsonObject();
        entry.addProperty("timestamp", Instant.now(CLOCK).toString());
        entry.addProperty("domain", domain);
        entry.addProperty("fromVersion", fromVersion);
        entry.addProperty("toVersion", toVersion);
        entry.addProperty("source", source.getFileName().toString());
        entry.addProperty("backup", backup.getFileName().toString());
        Path journal = source.getParent().resolve("migration-journal.jsonl");
        Files.writeString(
                journal,
                GSON.toJson(entry) + System.lineSeparator(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }

    private static void quarantine(Path path, String domain, String reason) {
        try {
            Path quarantined = AtomicFileStore.quarantine(
                    path,
                    path.getParent().resolve(".corrupt"),
                    CLOCK);
            STATUS.put(path, new StoreStatus(
                    path,
                    domain,
                    0,
                    Files.size(quarantined),
                    Instant.now(CLOCK),
                    "quarantined"));
            LOGGER.log(
                    System.Logger.Level.ERROR,
                    "[SEF] Quarantined unreadable " + domain + " storage "
                            + quarantined.getFileName() + " because " + reason);
        } catch (IOException quarantineFailure) {
            STATUS.put(path, new StoreStatus(
                    path,
                    domain,
                    0,
                    0L,
                    Instant.now(CLOCK),
                    "quarantine failed"));
            LOGGER.log(
                    System.Logger.Level.ERROR,
                    "[SEF] Failed to quarantine unreadable " + domain + " storage " + path.getFileName(),
                    quarantineFailure);
        }
    }
}
