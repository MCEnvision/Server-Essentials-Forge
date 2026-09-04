package com.enviouse.sef.docs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Versioned, test-side contract for sanitized audit evidence and inventory rows.
 * Raw command output and host state must never be passed to the writer without
 * going through {@link #sanitize(JsonObject)}.
 */
public final class AuditEvidenceContract {
    public static final int SCHEMA_VERSION = 1;
    public static final int MAX_STRING_LENGTH = 1024;
    public static final int MAX_ARRAY_ENTRIES = 64;
    public static final int MAX_OBJECT_ENTRIES = 64;
    public static final int MAX_DEPTH = 8;

    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();
    private static final Pattern ROW_ID = Pattern.compile("[a-z][a-z0-9_.-]*:[a-z0-9_.-]+");
    private static final Pattern PHASE_ID = Pattern.compile("SEFAUD-PHASE-[0-9]{3}");
    private static final Pattern HOST_PATH = Pattern.compile(
            "(?i)(?:^|[\\s=])(\\\\\\\\[^\\s]+|[a-z]:[\\\\/][^\\s]+|/(?:home|root|mnt|Users|private|var|tmp|opt)/[^\\s]+)");
    private static final Pattern EMAIL = Pattern.compile(
            "(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b");
    private static final Pattern SECRET_VALUE = Pattern.compile(
            "(?i)\\b(?:bearer|basic)\\s+[A-Z0-9._~+/=-]{8,}|(?:sk|ghp|github_pat)_[A-Z0-9_]{8,}");
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "passwd", "secret", "token", "apikey", "api_key", "credential",
            "credentials", "authorization", "cookie", "session_cookie", "private_key",
            "access_key", "refresh_token", "client_secret", "keystore", "signing_key");
    private static final Set<String> PERSONAL_KEYS = Set.of(
            "email", "username", "playername", "displayname", "realname", "phone", "ip",
            "ip_address", "address", "hostname", "actorname", "actorusername", "personal_data");
    private static final Set<String> LOG_KEYS = Set.of(
            "log", "logs", "raw_log", "stdout", "stderr", "stacktrace", "trace", "raw_output",
            "command_output", "debug_output");

    private AuditEvidenceContract() {
    }

    public static JsonObject sanitize(JsonObject source) {
        if (source == null) {
            throw new IllegalArgumentException("evidence record is required");
        }
        JsonElement sanitized = sanitizeElement(source, "", 0);
        if (!sanitized.isJsonObject()) {
            throw new IllegalArgumentException("sanitized evidence record must be an object");
        }
        return sanitized.getAsJsonObject();
    }

    public static void validateEvidenceRecord(JsonObject record) {
        if (record == null) {
            throw new IllegalArgumentException("evidence record is required");
        }
        requireVersion(record, "evidence record");
        requireString(record, "recordId");
        requireString(record, "phaseId");
        if (!PHASE_ID.matcher(record.get("phaseId").getAsString()).matches()) {
            throw new IllegalArgumentException("invalid evidence phase");
        }
        requireString(record, "taskId");
        requireString(record, "owner");
        requireString(record, "evidenceRoute");
        requireString(record, "environmentId");
        requireString(record, "expected");
        requireString(record, "actual");
        requireString(record, "sourceDigest");
        requireInstant(record, "capturedAt");
        requireObject(record, "revision");
        requireString(record.getAsJsonObject("revision"), "commit");
        requireString(record.getAsJsonObject("revision"), "tree");
        requireObject(record, "retention");
        JsonObject retention = record.getAsJsonObject("retention");
        requireString(retention, "class");
        if (!Set.of("task", "phase", "release").contains(retention.get("class").getAsString())) {
            throw new IllegalArgumentException("invalid evidence retention class");
        }
        if (!retention.has("days") || !retention.get("days").isJsonPrimitive()
                || !retention.get("days").getAsJsonPrimitive().isNumber()
                || retention.get("days").getAsInt() < 1 || retention.get("days").getAsInt() > 3650) {
            throw new IllegalArgumentException("invalid evidence retention days");
        }
        requireObject(record, "invalidation");
        JsonObject invalidation = record.getAsJsonObject("invalidation");
        requireString(invalidation, "state");
        if (!Set.of("valid", "invalidated").contains(invalidation.get("state").getAsString())) {
            throw new IllegalArgumentException("invalid evidence invalidation state");
        }
        requireStringArray(invalidation, "triggers", false);
        requireStringArray(invalidation, "invalidatedBy", false);
        if (!record.has("result") || !record.get("result").isJsonPrimitive()
                || !record.getAsJsonPrimitive("result").isString()
                || !Set.of("pass", "fail", "blocked", "not_run", "invalidated")
                .contains(record.get("result").getAsString())) {
            throw new IllegalArgumentException("invalid evidence result");
        }
        boolean invalidated = invalidation.get("state").getAsString().equals("invalidated");
        boolean hasInvalidationCause = !invalidation.getAsJsonArray("invalidatedBy").isEmpty();
        boolean resultInvalidated = record.get("result").getAsString().equals("invalidated");
        if (invalidated != resultInvalidated || (!invalidated && hasInvalidationCause)) {
            throw new IllegalArgumentException("evidence invalidation and result disagree");
        }
        requireObject(record, "payload");
    }

    public static void validateInventoryRow(JsonObject row) {
        if (row == null) {
            throw new IllegalArgumentException("inventory row is required");
        }
        requireVersion(row, "inventory row");
        requireString(row, "rowId");
        if (!ROW_ID.matcher(row.get("rowId").getAsString()).matches()) {
            throw new IllegalArgumentException("inventory row id must use a stable namespace");
        }
        requireString(row, "category");
        requireString(row, "semanticKey");
        requireString(row, "owner");
        requireString(row, "phase");
        if (!PHASE_ID.matcher(row.get("phase").getAsString()).matches()) {
            throw new IllegalArgumentException("invalid inventory phase");
        }
        requireString(row, "evidenceRoute");
        requireString(row, "evidenceClass");
        requireString(row, "disposition");
        if (!Set.of("implemented", "incomplete", "stale", "blocked", "excluded", "finding")
                .contains(row.get("disposition").getAsString())) {
            throw new IllegalArgumentException("invalid inventory disposition");
        }
        requireStringArray(row, "invalidatedBy", true);
        requireStringArray(row, "sourceLocations", false);
    }

    public static void validateInventorySet(JsonArray rows) {
        if (rows == null) {
            throw new IllegalArgumentException("inventory set is required");
        }
        Set<String> rowIds = new HashSet<>();
        Set<String> semanticKeys = new HashSet<>();
        for (JsonElement element : rows) {
            if (!element.isJsonObject()) {
                throw new IllegalArgumentException("inventory row must be an object");
            }
            JsonObject row = element.getAsJsonObject();
            validateInventoryRow(row);
            String rowId = row.get("rowId").getAsString();
            String semanticKey = row.get("category").getAsString() + "\u0000" + row.get("semanticKey").getAsString();
            if (!rowIds.add(rowId)) {
                throw new IllegalArgumentException("duplicate inventory row id " + rowId);
            }
            if (!semanticKeys.add(semanticKey)) {
                throw new IllegalArgumentException("duplicate inventory semantic key " + semanticKey);
            }
        }
    }

    public static void validateCommandInventory(JsonObject inventory) {
        if (inventory == null) {
            throw new IllegalArgumentException("command inventory is required");
        }
        requireVersion(inventory, "command inventory");
        requireString(inventory, "inventoryId");
        requireString(inventory, "phase");
        if (!PHASE_ID.matcher(inventory.get("phase").getAsString()).matches()) {
            throw new IllegalArgumentException("invalid command inventory phase");
        }
        requireString(inventory, "task");
        requireString(inventory, "source");
        requireInteger(inventory, "catalogActionCount");
        requireInteger(inventory, "routeCount");
        requireInteger(inventory, "shortcutCount");
        requireInteger(inventory, "permissionCount");
        requireInteger(inventory, "unavailableFamilyCount");
        if (!inventory.has("rows") || !inventory.get("rows").isJsonArray()) {
            throw new IllegalArgumentException("command inventory rows are required");
        }
        JsonArray rows = inventory.getAsJsonArray("rows");
        validateInventorySet(rows);

        Set<String> actions = new HashSet<>();
        Set<String> routes = new HashSet<>();
        Set<String> shortcuts = new HashSet<>();
        Set<String> permissions = new HashSet<>();
        int actionCount = 0;
        int routeCount = 0;
        int shortcutCount = 0;
        int permissionCount = 0;
        int unavailableCount = 0;
        for (JsonElement element : rows) {
            JsonObject row = element.getAsJsonObject();
            String category = row.get("category").getAsString();
            switch (category) {
                case "command" -> {
                    actionCount++;
                    if (!actions.add(row.get("semanticKey").getAsString())) {
                        throw new IllegalArgumentException("duplicate command action");
                    }
                    requireString(row, "canonicalRoute");
                    requireString(row, "featureId");
                    requireString(row, "accessClass");
                    requireString(row, "auditClass");
                    requireString(row, "targetBehavior");
                    requireBoolean(row, "pipelineEnforced");
                    requireStringArray(row, "permissionIds", true);
                    requireStringArray(row, "sourceTypes", true);
                    requireStringArray(row, "convenienceRoots", false);
                }
                case "route" -> {
                    routeCount++;
                    if (!routes.add(row.get("semanticKey").getAsString())) {
                        throw new IllegalArgumentException("duplicate command route");
                    }
                    requireString(row, "actionId");
                    if (!actions.contains(row.get("actionId").getAsString())) {
                        throw new IllegalArgumentException("route points to an unknown command");
                    }
                }
                case "shortcut" -> {
                    shortcutCount++;
                    if (!shortcuts.add(row.get("semanticKey").getAsString())) {
                        throw new IllegalArgumentException("duplicate command shortcut");
                    }
                    requireString(row, "actionId");
                    if (!actions.contains(row.get("actionId").getAsString())) {
                        throw new IllegalArgumentException("shortcut points to an unknown command");
                    }
                    requireString(row, "adapter");
                    requireString(row, "collisionMode");
                    requireNumber(row, "structuralRevision");
                    if (row.has("registrationStatus")) {
                        requireString(row, "registrationStatus");
                        if (!Set.of("active", "active_override", "disabled", "canonical_only", "conflict",
                                "restart_required").contains(row.get("registrationStatus").getAsString())) {
                            throw new IllegalArgumentException("invalid shortcut registration status");
                        }
                        requireString(row, "registrationDetail");
                        requireBoolean(row, "registered");
                    }
                }
                case "permission" -> {
                    permissionCount++;
                    if (!permissions.add(row.get("semanticKey").getAsString())) {
                        throw new IllegalArgumentException("duplicate command permission");
                    }
                    requireBoolean(row, "default");
                    requireString(row, "name");
                }
                case "unavailable" -> {
                    unavailableCount++;
                    requireBoolean(row, "runtimeHandler");
                    requireBoolean(row, "capabilityAdvertised");
                    if (row.get("runtimeHandler").getAsBoolean()
                            || row.get("capabilityAdvertised").getAsBoolean()) {
                        throw new IllegalArgumentException("unavailable family is advertised");
                    }
                }
                default -> throw new IllegalArgumentException("unknown command inventory category " + category);
            }
        }
        requireCount(inventory, "catalogActionCount", actionCount);
        requireCount(inventory, "routeCount", routeCount);
        requireCount(inventory, "shortcutCount", shortcutCount);
        requireCount(inventory, "permissionCount", permissionCount);
        requireCount(inventory, "unavailableFamilyCount", unavailableCount);
        if (inventory.has("dispatcherRootCount")) {
            requireInteger(inventory, "dispatcherRootCount");
            if (!inventory.has("dispatcherRoots") || !inventory.get("dispatcherRoots").isJsonArray()) {
                throw new IllegalArgumentException("dispatcher root rows are required");
            }
            JsonArray dispatcherRoots = inventory.getAsJsonArray("dispatcherRoots");
            if (dispatcherRoots.size() != inventory.get("dispatcherRootCount").getAsInt()) {
                throw new IllegalArgumentException("dispatcher root count does not match rows");
            }
            Set<String> rootKeys = new HashSet<>();
            for (JsonElement element : dispatcherRoots) {
                if (!element.isJsonObject()) {
                    throw new IllegalArgumentException("dispatcher root row must be an object");
                }
                JsonObject row = element.getAsJsonObject();
                validateInventoryRow(row);
                if (!"dispatcher-root".equals(row.get("category").getAsString())
                        || !rootKeys.add(row.get("semanticKey").getAsString())) {
                    throw new IllegalArgumentException("invalid dispatcher root row");
                }
                requireBoolean(row, "registered");
            }
        }
    }

    public static Path write(Path approvedExternalRoot, String fileName, JsonObject rawRecord) throws IOException {
        if (approvedExternalRoot == null || fileName == null || !fileName.matches("[A-Za-z0-9._-]+\\.json")) {
            throw new IllegalArgumentException("evidence output name is invalid");
        }
        Path root = approvedExternalRoot.toAbsolutePath().normalize();
        rejectSymlinkComponents(root);
        Files.createDirectories(root);
        rejectSymlinkComponents(root);
        JsonObject record = sanitize(rawRecord);
        validateEvidenceRecord(record);
        return writeJson(root, fileName, record);
    }

    public static Path writeInventory(
            Path approvedExternalRoot,
            String fileName,
            JsonObject metadata,
            JsonArray rows
    ) throws IOException {
        validateInventorySet(rows);
        JsonObject metadataOnly = metadata == null ? new JsonObject() : metadata.deepCopy();
        metadataOnly.remove("rows");
        metadataOnly.addProperty("schemaVersion", SCHEMA_VERSION);
        JsonObject sanitized = sanitize(metadataOnly);
        JsonArray sanitizedRows = new JsonArray();
        for (JsonElement row : rows) {
            sanitizedRows.add(sanitize(row.getAsJsonObject()));
        }
        validateInventorySet(sanitizedRows);
        sanitized.add("rows", sanitizedRows);
        Path root = approvedExternalRoot.toAbsolutePath().normalize();
        rejectSymlinkComponents(root);
        Files.createDirectories(root);
        rejectSymlinkComponents(root);
        return writeJson(root, fileName, sanitized);
    }

    public static boolean isExpired(JsonObject record, Instant now) {
        validateEvidenceRecord(record);
        if (now == null) {
            throw new IllegalArgumentException("retention clock is required");
        }
        Instant captured = parseInstant(record.get("capturedAt").getAsString());
        long days = record.getAsJsonObject("retention").get("days").getAsLong();
        return !now.isBefore(captured.plusSeconds(days * 86_400L));
    }

    private static JsonElement sanitizeElement(JsonElement element, String key, int depth) {
        if (depth > MAX_DEPTH) {
            return new JsonPrimitive("[depth-limited]");
        }
        if (element == null || element.isJsonNull()) {
            return JsonParser.parseString("null");
        }
        if (element.isJsonObject()) {
            JsonObject object = new JsonObject();
            Map<String, JsonElement> sorted = new TreeMap<>();
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                String normalizedKey = normalizeKey(entry.getKey());
                if (isSensitiveKey(normalizedKey) || isPersonalKey(normalizedKey) || isLogKey(normalizedKey)) {
                    continue;
                }
                sorted.put(normalizedKey, sanitizeElement(entry.getValue(), normalizedKey, depth + 1));
            }
            int count = 0;
            for (Map.Entry<String, JsonElement> entry : sorted.entrySet()) {
                if (count++ >= MAX_OBJECT_ENTRIES) {
                    break;
                }
                object.add(entry.getKey(), entry.getValue());
            }
            return object;
        }
        if (element.isJsonArray()) {
            JsonArray array = new JsonArray();
            int count = 0;
            for (JsonElement child : element.getAsJsonArray()) {
                if (count++ >= MAX_ARRAY_ENTRIES) {
                    break;
                }
                array.add(sanitizeElement(child, key, depth + 1));
            }
            return array;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return new JsonPrimitive(sanitizeString(element.getAsString()));
        }
        return element.deepCopy();
    }

    private static String sanitizeString(String value) {
        String sanitized = value.codePoints()
                .filter(codePoint -> !Character.isISOControl(codePoint))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString()
                .replaceAll("\\s+", " ")
                .trim();
        sanitized = HOST_PATH.matcher(sanitized).replaceAll(" [host-path]");
        sanitized = EMAIL.matcher(sanitized).replaceAll("[personal-data]");
        sanitized = SECRET_VALUE.matcher(sanitized).replaceAll("[redacted]");
        if (sanitized.length() > MAX_STRING_LENGTH) {
            sanitized = sanitized.substring(0, MAX_STRING_LENGTH) + "[truncated]";
        }
        return sanitized;
    }

    private static String normalizeKey(String key) {
        return key == null ? "" : key.replaceAll("\\s+", " ").trim();
    }

    private static boolean isSensitiveKey(String key) {
        return keyMatchesClass(key, SENSITIVE_KEYS);
    }

    private static boolean isPersonalKey(String key) {
        return keyMatchesClass(key, PERSONAL_KEYS);
    }

    private static boolean isLogKey(String key) {
        return keyMatchesClass(key, LOG_KEYS);
    }

    private static boolean keyMatchesClass(String key, Set<String> keyClass) {
        String separated = key.replaceAll("([a-z])([A-Z])", "$1_$2")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_");
        String compact = separated.replace("_", "");
        return keyClass.stream().anyMatch(candidate -> {
            String token = candidate.toLowerCase(Locale.ROOT);
            String compactToken = token.replace("_", "");
            return separated.equals(token)
                    || separated.startsWith(token + "_")
                    || separated.endsWith("_" + token)
                    || separated.contains("_" + token + "_")
                    || compact.equals(compactToken);
        });
    }

    private static void requireVersion(JsonObject object, String kind) {
        if (!object.has("schemaVersion") || !object.get("schemaVersion").isJsonPrimitive()
                || !object.getAsJsonPrimitive("schemaVersion").isNumber()
                || object.get("schemaVersion").getAsInt() != SCHEMA_VERSION) {
            throw new IllegalArgumentException("unsupported " + kind + " schema version");
        }
    }

    private static void requireBoolean(JsonObject object, String field) {
        if (!object.has(field) || !object.get(field).isJsonPrimitive()
                || !object.getAsJsonPrimitive(field).isBoolean()) {
            throw new IllegalArgumentException("missing " + field);
        }
    }

    private static void requireNumber(JsonObject object, String field) {
        if (!object.has(field) || !object.get(field).isJsonPrimitive()
                || !object.getAsJsonPrimitive(field).isNumber()) {
            throw new IllegalArgumentException("missing " + field);
        }
    }

    private static void requireInteger(JsonObject object, String field) {
        requireNumber(object, field);
        if (object.get(field).getAsInt() < 0) {
            throw new IllegalArgumentException("invalid " + field);
        }
    }

    private static void requireCount(JsonObject object, String field, int actual) {
        if (object.get(field).getAsInt() != actual) {
            throw new IllegalArgumentException(field + " does not match rows");
        }
    }

    private static void requireObject(JsonObject object, String field) {
        if (object == null || !object.has(field) || !object.get(field).isJsonObject()) {
            throw new IllegalArgumentException("missing " + field);
        }
    }

    private static void requireString(JsonObject object, String field) {
        if (!object.has(field) || !object.get(field).isJsonPrimitive()
                || !object.getAsJsonPrimitive(field).isString()
                || object.get(field).getAsString().isBlank()) {
            throw new IllegalArgumentException("missing " + field);
        }
    }

    private static void requireInstant(JsonObject object, String field) {
        requireString(object, field);
        parseInstant(object.get(field).getAsString());
    }

    private static Instant parseInstant(String value) {
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("invalid evidence timestamp", exception);
        }
    }

    private static void requireStringArray(JsonObject object, String field, boolean requireAtLeastOne) {
        if (!object.has(field) || !object.get(field).isJsonArray()) {
            throw new IllegalArgumentException("missing " + field);
        }
        JsonArray values = object.getAsJsonArray(field);
        if (requireAtLeastOne && values.isEmpty()) {
            throw new IllegalArgumentException("empty " + field);
        }
        if (values.size() > MAX_ARRAY_ENTRIES) {
            throw new IllegalArgumentException("unbounded " + field);
        }
        for (JsonElement value : values) {
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()
                    || value.getAsString().isBlank()) {
                throw new IllegalArgumentException("invalid " + field);
            }
        }
    }

    private static void rejectSymlinkComponents(Path path) {
        Path absolute = path.toAbsolutePath().normalize();
        Path current = absolute.getRoot();
        if (current == null) {
            throw new IllegalArgumentException("evidence root must be absolute");
        }
        for (Path component : absolute) {
            current = current.resolve(component);
            if (Files.isSymbolicLink(current) && !isTrustedMacSystemAlias(current)) {
                throw new IllegalArgumentException("evidence path cannot contain symlinks");
            }
        }
    }

    private static boolean isTrustedMacSystemAlias(Path path) {
        if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("mac")) {
            return false;
        }
        Path root = path.getRoot();
        if (root == null || !root.equals(path.getParent())) {
            return false;
        }
        try {
            Path resolved = path.toRealPath();
            return resolved.startsWith(root.resolve("private"))
                    && Files.isDirectory(resolved, LinkOption.NOFOLLOW_LINKS);
        } catch (IOException exception) {
            return false;
        }
    }

    private static Path writeJson(Path root, String fileName, JsonObject value) throws IOException {
        if (!fileName.matches("[A-Za-z0-9._-]+\\.json")) {
            throw new IllegalArgumentException("evidence output name is invalid");
        }
        Path target = root.resolve(fileName).normalize();
        if (!target.getParent().equals(root) || Files.isSymbolicLink(target)) {
            throw new IllegalArgumentException("evidence output must be a regular child of the approved root");
        }
        Files.writeString(
                target,
                GSON.toJson(value) + System.lineSeparator(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE);
        return target;
    }
}
