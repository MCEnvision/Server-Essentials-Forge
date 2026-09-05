package com.enviouse.sef.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
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
    static final int MAX_DOCUMENT_BYTES = 16 * 1024 * 1024;
    static final int MAX_JSON_DEPTH = 128;
    static final int MAX_JSON_CONTAINER_ENTRIES = 100_000;
    static final int MAX_JSON_TOTAL_VALUES = 1_000_000;
    static final int MAX_JSON_STRING_LENGTH = 1_048_576;
    static final int MAX_JSON_NAME_LENGTH = 1_024;
    private static final Clock CLOCK = Clock.systemUTC();
    private static final Map<Path, StoreStatus> STATUS = new ConcurrentHashMap<>();

    private StorageService() {
    }

    public static Optional<Document> read(Path path, String domain, int currentVersion) {
        return read(path, domain, currentVersion, true);
    }

    private static Optional<Document> read(
            Path path,
            String domain,
            int currentVersion,
            boolean allowPreviousRecovery
    ) {
        Path normalized = path.toAbsolutePath().normalize();
        if (!Files.exists(normalized, LinkOption.NOFOLLOW_LINKS)) {
            if (allowPreviousRecovery) {
                try {
                    if (AtomicFileStore.restorePrevious(normalized, MAX_DOCUMENT_BYTES)) {
                        LOGGER.log(
                                System.Logger.Level.WARNING,
                                "[SEF] Restored the previous durable generation for "
                                        + normalized.getFileName());
                        return read(normalized, domain, currentVersion, false);
                    }
                } catch (IOException exception) {
                    rejected(normalized, domain, currentVersion, "previous generation recovery failed", exception);
                    return Optional.empty();
                }
            }
            STATUS.put(normalized, new StoreStatus(
                    normalized, domain, currentVersion, 0L, Instant.now(CLOCK), "missing"));
            return Optional.empty();
        }

        try {
            byte[] encoded = AtomicFileStore.readBounded(normalized, MAX_DOCUMENT_BYTES);
            String json = decodeUtf8(encoded);
            validateJsonStructure(json);
            JsonElement root = JsonParser.parseString(json);
            if (root == null || root.isJsonNull()) {
                throw new JsonStructureException("Managed document is empty");
            }

            if (!isEnvelope(root)) {
                if (!prepareMigration(
                        normalized,
                        domain,
                        0,
                        currentVersion,
                        "v0.bak",
                        encoded.length)) {
                    return Optional.empty();
                }
                STATUS.put(normalized, new StoreStatus(
                        normalized, domain, 0, encoded.length, Instant.now(CLOCK), "legacy"));
                return Optional.of(new Document(
                        domain,
                        0,
                        root,
                        new JsonObject(),
                        boundedCopy(root),
                        true));
            }

            JsonObject object = root.getAsJsonObject();
            String storedDomain = object.get("domain").getAsString();
            int version = object.get("schemaVersion").getAsInt();
            if (object.get("schemaVersion").getAsDouble() != version) {
                throw new JsonStructureException("Managed document schema version is not an integer");
            }
            if (!domain.equals(storedDomain)) {
                throw new JsonStructureException("Managed document domain does not match");
            }
            if (version > currentVersion || version < 1) {
                STATUS.put(normalized, new StoreStatus(
                        normalized, domain, version, encoded.length, Instant.now(CLOCK), "unsupported"));
                LOGGER.log(
                        System.Logger.Level.ERROR,
                        "[SEF] Refusing unsupported " + domain + " storage version " + version
                                + " in " + normalized.getFileName());
                return Optional.empty();
            }

            JsonObject extras = boundedCopy(object).getAsJsonObject();
            extras.remove("domain");
            extras.remove("schemaVersion");
            extras.remove("data");
            JsonElement data = object.get("data");
            boolean migrated = version < currentVersion;
            if (migrated && !prepareMigration(
                        normalized,
                        domain,
                        version,
                        currentVersion,
                        "v" + version + ".bak",
                        encoded.length)) {
                return Optional.empty();
            }
            STATUS.put(normalized, new StoreStatus(
                    normalized,
                    domain,
                    version,
                    encoded.length,
                    Instant.now(CLOCK),
                    migrated ? "migration pending" : "ready"));
            return Optional.of(new Document(
                    domain,
                    version,
                    data,
                    extras,
                    boundedCopy(data),
                    migrated));
        } catch (AtomicFileStore.UnsafeStoragePathException exception) {
            rejected(normalized, domain, currentVersion, "unsafe managed path", exception);
            return Optional.empty();
        } catch (IOException | RuntimeException exception) {
            boolean quarantined = quarantine(normalized, domain, exception.getClass().getSimpleName());
            if (quarantined && allowPreviousRecovery) {
                try {
                    if (AtomicFileStore.restorePrevious(normalized, MAX_DOCUMENT_BYTES)) {
                        LOGGER.log(
                                System.Logger.Level.WARNING,
                                "[SEF] Recovered " + domain + " from its previous durable generation");
                        return read(normalized, domain, currentVersion, false);
                    }
                } catch (IOException recoveryFailure) {
                    rejected(
                            normalized,
                            domain,
                            currentVersion,
                            "previous generation recovery failed",
                            recoveryFailure);
                }
            }
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
        if (schemaVersion < 1) {
            throw new IOException("Managed storage schema version is invalid");
        }
        JsonObject root = previous == null
                ? new JsonObject()
                : boundedCopy(previous.rootExtras()).getAsJsonObject();
        root.addProperty("domain", domain);
        root.addProperty("schemaVersion", schemaVersion);
        root.add("data", previous == null
                ? boundedCopy(data)
                : mergeUnknownFields(previous.originalData(), data, "", dynamicObjectPaths));
        validateJsonElement(root);
        byte[] content = GSON.toJson(root).getBytes(StandardCharsets.UTF_8);
        if (content.length > MAX_DOCUMENT_BYTES) {
            throw new AtomicFileStore.DocumentLimitException(
                    "Managed document exceeds its byte limit");
        }
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
        Path normalizedExportRoot = exportRoot.toAbsolutePath().normalize();
        AtomicFileStore.createSafeDirectories(normalizedExportRoot);
        Path snapshot = normalizedExportRoot.resolve("storage_" + System.currentTimeMillis()).normalize();
        if (!snapshot.startsWith(normalizedExportRoot)) {
            throw new IOException("Storage export escaped its configured directory");
        }
        AtomicFileStore.createSafeDirectories(snapshot);
        for (StoreStatus status : statuses()) {
            Path source = status.path().toAbsolutePath().normalize();
            boolean managed = normalizedRoots.stream()
                    .anyMatch(root -> source.getParent() != null && source.getParent().equals(root));
            if (!managed
                    || !include.test(status)) {
                continue;
            }
            if (!Files.exists(source, LinkOption.NOFOLLOW_LINKS)) {
                continue;
            }
            byte[] content = AtomicFileStore.readBounded(source, MAX_DOCUMENT_BYTES);
            Path target = snapshot.resolve(source.getFileName());
            if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                String prefix = status.domain().replaceAll("[^a-zA-Z0-9._]", "_");
                target = snapshot.resolve(prefix + "_" + source.getFileName());
            }
            if (!target.normalize().startsWith(snapshot)) {
                throw new IOException("Storage export target escaped its snapshot directory");
            }
            AtomicFileStore.write(target, content);
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
                && object.getAsJsonPrimitive("domain").isString()
                && object.has("schemaVersion")
                && object.get("schemaVersion").isJsonPrimitive()
                && object.getAsJsonPrimitive("schemaVersion").isNumber()
                && object.has("data");
    }

    private static JsonElement mergeUnknownFields(
            JsonElement original,
            JsonElement current,
            String path,
            Set<String> dynamicObjectPaths
    ) {
        JsonElement[] result = new JsonElement[1];
        Deque<MergeTask> tasks = new ArrayDeque<>();
        tasks.push(new MergeTask(original, current, path, value -> result[0] = value, 0));
        int processed = 0;
        while (!tasks.isEmpty()) {
            MergeTask task = tasks.pop();
            processed++;
            if (processed > MAX_JSON_TOTAL_VALUES || task.depth() > MAX_JSON_DEPTH) {
                throw new IllegalArgumentException("Managed JSON merge exceeds structural limits");
            }
            JsonElement oldValue = task.original();
            JsonElement newValue = task.current();
            if (oldValue == null || newValue == null) {
                task.sink().accept(boundedCopy(newValue));
                continue;
            }
            if (oldValue.isJsonArray() && newValue.isJsonArray()) {
                JsonArray oldArray = oldValue.getAsJsonArray();
                JsonArray newArray = newValue.getAsJsonArray();
                JsonArray merged = new JsonArray();
                for (int index = 0; index < newArray.size(); index++) {
                    merged.add(com.google.gson.JsonNull.INSTANCE);
                }
                task.sink().accept(merged);
                for (int index = newArray.size() - 1; index >= 0; index--) {
                    int targetIndex = index;
                    JsonElement child = newArray.get(index);
                    tasks.push(new MergeTask(
                            findMatchingArrayElement(oldArray, child, index),
                            child,
                            task.path() + "/" + index,
                            value -> merged.set(targetIndex, value),
                            task.depth() + 1));
                }
                continue;
            }
            if (oldValue.isJsonObject() && newValue.isJsonObject()) {
                JsonObject merged = dynamicObjectPaths.contains(task.path())
                        ? new JsonObject()
                        : boundedCopy(oldValue).getAsJsonObject();
                task.sink().accept(merged);
                List<Map.Entry<String, JsonElement>> entries =
                        new ArrayList<>(newValue.getAsJsonObject().entrySet());
                Collections.reverse(entries);
                for (Map.Entry<String, JsonElement> entry : entries) {
                    String key = entry.getKey();
                    tasks.push(new MergeTask(
                            oldValue.getAsJsonObject().get(key),
                            entry.getValue(),
                            task.path() + "/" + escapePathSegment(key),
                            value -> merged.add(key, value),
                            task.depth() + 1));
                }
                continue;
            }
            task.sink().accept(boundedCopy(newValue));
        }
        return result[0];
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

    private static String decodeUtf8(byte[] encoded) throws CharacterCodingException {
        return StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(encoded))
                .toString();
    }

    private static void validateJsonStructure(String json) throws IOException {
        try (JsonReader reader = new JsonReader(new StringReader(json))) {
            reader.setLenient(false);
            Deque<ContainerCounter> containers = new ArrayDeque<>();
            boolean rootSeen = false;
            int totalValues = 0;
            while (true) {
                JsonToken token = reader.peek();
                if (token == JsonToken.END_DOCUMENT) {
                    if (!rootSeen || !containers.isEmpty()) {
                        throw new JsonStructureException("Managed JSON is incomplete");
                    }
                    return;
                }
                switch (token) {
                    case BEGIN_ARRAY -> {
                        rootSeen = beforeJsonValue(containers, rootSeen);
                        totalValues = checkedValueCount(totalValues);
                        reader.beginArray();
                        containers.push(new ContainerCounter(true));
                        checkedDepth(containers.size());
                    }
                    case BEGIN_OBJECT -> {
                        rootSeen = beforeJsonValue(containers, rootSeen);
                        totalValues = checkedValueCount(totalValues);
                        reader.beginObject();
                        containers.push(new ContainerCounter(false));
                        checkedDepth(containers.size());
                    }
                    case END_ARRAY -> {
                        requireContainer(containers, true);
                        reader.endArray();
                        containers.pop();
                    }
                    case END_OBJECT -> {
                        requireContainer(containers, false);
                        reader.endObject();
                        containers.pop();
                    }
                    case NAME -> {
                        requireContainer(containers, false);
                        String name = reader.nextName();
                        if (name.length() > MAX_JSON_NAME_LENGTH) {
                            throw new JsonStructureException("Managed JSON name exceeds its limit");
                        }
                        containers.peek().increment();
                    }
                    case STRING -> {
                        rootSeen = beforeJsonValue(containers, rootSeen);
                        totalValues = checkedValueCount(totalValues);
                        if (reader.nextString().length() > MAX_JSON_STRING_LENGTH) {
                            throw new JsonStructureException("Managed JSON string exceeds its limit");
                        }
                    }
                    case NUMBER -> {
                        rootSeen = beforeJsonValue(containers, rootSeen);
                        totalValues = checkedValueCount(totalValues);
                        if (reader.nextString().length() > 128) {
                            throw new JsonStructureException("Managed JSON number exceeds its limit");
                        }
                    }
                    case BOOLEAN -> {
                        rootSeen = beforeJsonValue(containers, rootSeen);
                        totalValues = checkedValueCount(totalValues);
                        reader.nextBoolean();
                    }
                    case NULL -> {
                        rootSeen = beforeJsonValue(containers, rootSeen);
                        totalValues = checkedValueCount(totalValues);
                        reader.nextNull();
                    }
                    default -> throw new JsonStructureException("Managed JSON token is invalid");
                }
            }
        } catch (IllegalStateException exception) {
            throw new JsonStructureException("Managed JSON is malformed", exception);
        }
    }

    private static boolean beforeJsonValue(
            Deque<ContainerCounter> containers,
            boolean rootSeen
    ) throws JsonStructureException {
        if (containers.isEmpty()) {
            if (rootSeen) {
                throw new JsonStructureException("Managed JSON has multiple root values");
            }
            return true;
        }
        if (containers.peek().array()) {
            containers.peek().increment();
        }
        return rootSeen;
    }

    private static int checkedValueCount(int current) throws JsonStructureException {
        int next = Math.addExact(current, 1);
        if (next > MAX_JSON_TOTAL_VALUES) {
            throw new JsonStructureException("Managed JSON value count exceeds its limit");
        }
        return next;
    }

    private static void checkedDepth(int depth) throws JsonStructureException {
        if (depth > MAX_JSON_DEPTH) {
            throw new JsonStructureException("Managed JSON nesting exceeds its limit");
        }
    }

    private static void requireContainer(
            Deque<ContainerCounter> containers,
            boolean array
    ) throws JsonStructureException {
        if (containers.isEmpty() || containers.peek().array() != array) {
            throw new JsonStructureException("Managed JSON container is malformed");
        }
    }

    private static void validateJsonElement(JsonElement root) throws IOException {
        Deque<ElementDepth> elements = new ArrayDeque<>();
        elements.push(new ElementDepth(root, 0));
        int totalValues = 0;
        while (!elements.isEmpty()) {
            ElementDepth item = elements.pop();
            checkedDepth(item.depth());
            totalValues = checkedValueCount(totalValues);
            JsonElement element = item.element();
            if (element == null || element.isJsonNull()) {
                continue;
            }
            if (element.isJsonPrimitive()) {
                if (element.getAsJsonPrimitive().isString()
                        && element.getAsString().length() > MAX_JSON_STRING_LENGTH) {
                    throw new JsonStructureException("Managed JSON string exceeds its limit");
                }
                continue;
            }
            if (element.isJsonArray()) {
                JsonArray array = element.getAsJsonArray();
                if (array.size() > MAX_JSON_CONTAINER_ENTRIES) {
                    throw new JsonStructureException("Managed JSON array exceeds its limit");
                }
                for (int index = array.size() - 1; index >= 0; index--) {
                    elements.push(new ElementDepth(array.get(index), item.depth() + 1));
                }
                continue;
            }
            JsonObject object = element.getAsJsonObject();
            if (object.size() > MAX_JSON_CONTAINER_ENTRIES) {
                throw new JsonStructureException("Managed JSON object exceeds its limit");
            }
            List<Map.Entry<String, JsonElement>> entries =
                    new ArrayList<>(object.entrySet());
            Collections.reverse(entries);
            for (Map.Entry<String, JsonElement> entry : entries) {
                if (entry.getKey().length() > MAX_JSON_NAME_LENGTH) {
                    throw new JsonStructureException("Managed JSON name exceeds its limit");
                }
                elements.push(new ElementDepth(entry.getValue(), item.depth() + 1));
            }
        }
    }

    private static JsonElement boundedCopy(JsonElement source) {
        if (source == null) {
            return com.google.gson.JsonNull.INSTANCE;
        }
        JsonElement[] result = new JsonElement[1];
        Deque<CopyTask> tasks = new ArrayDeque<>();
        tasks.push(new CopyTask(source, value -> result[0] = value, 0));
        int processed = 0;
        while (!tasks.isEmpty()) {
            CopyTask task = tasks.pop();
            processed++;
            if (processed > MAX_JSON_TOTAL_VALUES || task.depth() > MAX_JSON_DEPTH) {
                throw new IllegalArgumentException("Managed JSON copy exceeds structural limits");
            }
            JsonElement element = task.source();
            if (element == null || element.isJsonNull() || element.isJsonPrimitive()) {
                task.sink().accept(element == null
                        ? com.google.gson.JsonNull.INSTANCE
                        : element);
                continue;
            }
            if (element.isJsonArray()) {
                JsonArray sourceArray = element.getAsJsonArray();
                JsonArray targetArray = new JsonArray();
                for (int index = 0; index < sourceArray.size(); index++) {
                    targetArray.add(com.google.gson.JsonNull.INSTANCE);
                }
                task.sink().accept(targetArray);
                for (int index = sourceArray.size() - 1; index >= 0; index--) {
                    int targetIndex = index;
                    tasks.push(new CopyTask(
                            sourceArray.get(index),
                            value -> targetArray.set(targetIndex, value),
                            task.depth() + 1));
                }
                continue;
            }
            JsonObject targetObject = new JsonObject();
            task.sink().accept(targetObject);
            List<Map.Entry<String, JsonElement>> entries =
                    new ArrayList<>(element.getAsJsonObject().entrySet());
            Collections.reverse(entries);
            for (Map.Entry<String, JsonElement> entry : entries) {
                String key = entry.getKey();
                tasks.push(new CopyTask(
                        entry.getValue(),
                        value -> targetObject.add(key, value),
                        task.depth() + 1));
            }
        }
        return result[0];
    }

    private static boolean prepareMigration(
            Path source,
            String domain,
            int fromVersion,
            int toVersion,
            String backupSuffix,
            long size
    ) {
        try {
            Path backup = AtomicFileStore.backup(
                    source,
                    source.getParent().resolve(".backups"),
                    backupSuffix,
                    CLOCK);
            appendMigrationJournal(source, domain, fromVersion, toVersion, backup);
            return true;
        } catch (IOException | RuntimeException exception) {
            STATUS.put(source, new StoreStatus(
                    source,
                    domain,
                    fromVersion,
                    size,
                    Instant.now(CLOCK),
                    "migration preparation failed"));
            LOGGER.log(
                    System.Logger.Level.ERROR,
                    "[SEF] Failed to prepare " + domain + " migration for " + source.getFileName()
                            + ". The original file was left unchanged",
                    exception);
            return false;
        }
    }

    private static synchronized void appendMigrationJournal(
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
        byte[] encoded = (GSON.toJson(entry) + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
        try (FileChannel channel = FileChannel.open(
                journal,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.APPEND)) {
            ByteBuffer buffer = ByteBuffer.wrap(encoded);
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
            channel.force(true);
        }
    }

    private static boolean quarantine(Path path, String domain, String reason) {
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
            return true;
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
            return false;
        }
    }

    private static void rejected(
            Path path,
            String domain,
            int schemaVersion,
            String reason,
            Exception exception
    ) {
        STATUS.put(path, new StoreStatus(
                path,
                domain,
                schemaVersion,
                0L,
                Instant.now(CLOCK),
                "rejected"));
        LOGGER.log(
                System.Logger.Level.ERROR,
                "[SEF] Rejected " + domain + " storage " + path.getFileName() + " because " + reason,
                exception);
    }

    private static final class ContainerCounter {
        private final boolean array;
        private int count;

        private ContainerCounter(boolean array) {
            this.array = array;
        }

        private boolean array() {
            return array;
        }

        private void increment() throws JsonStructureException {
            count = Math.addExact(count, 1);
            if (count > MAX_JSON_CONTAINER_ENTRIES) {
                throw new JsonStructureException(
                        "Managed JSON container entry count exceeds its limit");
            }
        }
    }

    private record ElementDepth(JsonElement element, int depth) {
    }

    private record CopyTask(
            JsonElement source,
            Consumer<JsonElement> sink,
            int depth
    ) {
    }

    private record MergeTask(
            JsonElement original,
            JsonElement current,
            String path,
            Consumer<JsonElement> sink,
            int depth
    ) {
    }

    private static final class JsonStructureException extends IOException {
        private JsonStructureException(String message) {
            super(message);
        }

        private JsonStructureException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
