package com.enviouse.sef.config.modules;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.config.ConfigHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public final class ModuleConfigService {
    public static final long MAXIMUM_MODULE_BYTES = 1_048_576L;
    public static final int MAXIMUM_HISTORY_PER_MODULE = 32;
    private static final long WATCH_DEBOUNCE_MILLISECONDS = 350L;
    private static final Set<PosixFilePermission> OWNER_PERMISSIONS = EnumSet.of(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE);
    private static final int LEGACY_MIGRATION_VERSION = 1;
    private static final List<LegacyMapping> LEGACY_MAPPINGS = legacyMappings();

    private final ModuleConfigRegistry registry;
    private final Map<String, ModuleSnapshot> modules = new LinkedHashMap<>();
    private final Map<String, String> guiActionModes = new LinkedHashMap<>();
    private final Map<String, String> pendingApply = new LinkedHashMap<>();
    private final Map<String, List<ModuleHistory>> history = new LinkedHashMap<>();
    private final List<Diagnostic> diagnostics = new ArrayList<>();
    private final List<Consumer<Publication>> publicationListeners = new ArrayList<>();
    private long revision = 1L;
    private Path configRoot;
    private Path modulesRoot;
    private Path historyRoot;
    private Consumer<Runnable> publicationExecutor = Runnable::run;
    private WatchService watchService;
    private ScheduledExecutorService watcherExecutor;
    private ScheduledFuture<?> pendingModuleReload;
    private ScheduledFuture<?> pendingGuiReload;
    private ScheduledFuture<?> reconciliationTask;
    private volatile boolean running;

    public ModuleConfigService(ModuleConfigRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    public synchronized Publication start(Path sefConfigRoot, Consumer<Runnable> executor) {
        if (running) {
            publicationExecutor = Objects.requireNonNullElse(executor, Runnable::run);
            return new Publication(true, revision, List.of(), List.of(), "already started");
        }
        configRoot = validateRoot(sefConfigRoot);
        modulesRoot = configRoot.resolve("modules").normalize();
        historyRoot = modulesRoot.resolve("history").normalize();
        publicationExecutor = Objects.requireNonNullElse(executor, Runnable::run);
        try {
            initializeDirectory();
            materializeDocumentationUpgrades();
            loadHistory();
            loadGuiOverrides(false);
            Publication publication = reloadInternal(registry.definitions().stream()
                    .map(ModuleConfigRegistry.ModuleDefinition::id)
                    .toList(), "startup");
            if (!publication.successful()) {
                return publication;
            }
            startWatcher();
            running = true;
            return publication;
        } catch (IOException | RuntimeException exception) {
            diagnostic(
                    DiagnosticSeverity.ERROR,
                    "",
                    "startup",
                    safeMessage(exception));
            return new Publication(
                    false,
                    revision,
                    List.of(),
                    diagnostics(),
                    "modular configuration startup failed, " + safeMessage(exception));
        }
    }

    public synchronized void stop() {
        running = false;
        if (pendingModuleReload != null) {
            pendingModuleReload.cancel(false);
            pendingModuleReload = null;
        }
        if (pendingGuiReload != null) {
            pendingGuiReload.cancel(false);
            pendingGuiReload = null;
        }
        if (reconciliationTask != null) {
            reconciliationTask.cancel(false);
            reconciliationTask = null;
        }
        if (watcherExecutor != null) {
            watcherExecutor.shutdownNow();
            watcherExecutor = null;
        }
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException exception) {
                ServerEssentialsForge.LOGGER.warn("[SEF] Failed to close the module configuration watcher", exception);
            }
            watchService = null;
        }
    }

    public synchronized boolean running() {
        return running;
    }

    public synchronized void addPublicationListener(Consumer<Publication> listener) {
        Objects.requireNonNull(listener, "listener");
        if (publicationListeners.size() >= 32) {
            throw new IllegalStateException("module configuration listener capacity reached");
        }
        publicationListeners.add(listener);
    }

    public ModuleConfigRegistry registry() {
        return registry;
    }

    public synchronized long revision() {
        return revision;
    }

    public synchronized Optional<ModuleSnapshot> module(String moduleId) {
        return Optional.ofNullable(modules.get(normalize(moduleId)));
    }

    public synchronized List<ModuleSnapshot> modules() {
        return modules.values().stream()
                .sorted(Comparator.comparing(ModuleSnapshot::moduleId))
                .toList();
    }

    public synchronized List<ModuleHistory> history(String moduleId) {
        registry.require(moduleId);
        return List.copyOf(history.getOrDefault(normalize(moduleId), List.of()));
    }

    public synchronized List<Diagnostic> diagnostics() {
        return List.copyOf(diagnostics);
    }

    public synchronized String value(String moduleId, String settingPath) {
        ModuleSnapshot snapshot = modules.get(normalize(moduleId));
        if (snapshot == null) {
            return registry.require(moduleId).settingsByPath().get(normalizePath(settingPath)).defaultValue();
        }
        return snapshot.values().get(normalizePath(settingPath));
    }

    public synchronized boolean enabled(String moduleId) {
        return Boolean.parseBoolean(value(moduleId, "module.enabled"));
    }

    public synchronized String guiActionMode(String actionId) {
        return guiActionModes.getOrDefault(normalizeAction(actionId), "inherit");
    }

    public synchronized Map<String, String> guiActionModes() {
        return Map.copyOf(guiActionModes);
    }

    public synchronized String effectiveGuiMode(String moduleId, String actionId) {
        String actionMode = guiActionMode(actionId);
        if (!actionMode.equals("inherit")) {
            return actionMode;
        }
        String moduleMode = value(moduleId, "gui.mode");
        if (!moduleMode.equals("inherit")) {
            return moduleMode;
        }
        return value("gui", "gui.mode");
    }

    public synchronized String moduleForFeature(String featureId) {
        String normalized = Objects.requireNonNull(featureId, "featureId")
                .trim()
                .toLowerCase(Locale.ROOT);
        String candidate = normalized.substring(normalized.lastIndexOf('.') + 1).replace('-', '_');
        if (registry.contains(candidate)) {
            return candidate;
        }
        return switch (normalized) {
            case "sef.control" -> "server_control";
            case "sef.fancy_tags" -> "fancy_tags";
            case "sef.enchant.admin" -> "super_enchanting";
            case "sef.gui", "sef.gui.policy" -> "gui";
            case "sef.item.self" -> "items";
            case "sef.workstation.additional" -> "workstations";
            case "sef.run" -> "run_and_silent";
            case "sef.fake" -> "fake_actions";
            default -> "commands";
        };
    }

    public synchronized Publication publishGuiActionMode(
            String actionId,
            String mode,
            long expectedRevision,
            UUID actorId
    ) {
        String normalizedAction = normalizeAction(actionId);
        String normalizedMode = normalizeGuiOverride(mode);
        if (revision != expectedRevision) {
            return failure("configuration revision changed");
        }
        Map<String, String> replacement = new LinkedHashMap<>(guiActionModes);
        if (normalizedMode.equals("inherit")) {
            replacement.remove(normalizedAction);
        } else {
            replacement.put(normalizedAction, normalizedMode);
        }
        if (replacement.size() > 8192) {
            return failure("GUI action override capacity reached");
        }
        if (replacement.equals(guiActionModes)) {
            return new Publication(true, revision, List.of(), diagnostics(), "configuration is unchanged");
        }
        try {
            writeGuiOverrides(replacement);
        } catch (IOException exception) {
            diagnostic(DiagnosticSeverity.ERROR, "gui", "action_override", safeMessage(exception));
            return failure("GUI action override write failed");
        }
        guiActionModes.clear();
        guiActionModes.putAll(replacement);
        revision = Math.addExact(revision, 1L);
        Publication publication = new Publication(
                true,
                revision,
                List.of("gui"),
                diagnostics(),
                "GUI action policy published");
        notifyListeners(publication);
        auditPublication(actorId, "gui", "action_override", publication);
        return publication;
    }

    public synchronized Validation validate(Collection<String> moduleIds) {
        List<String> requested = requested(moduleIds);
        CandidateSet candidates = parseCandidates(requested);
        if (!candidates.errors().isEmpty()) {
            return new Validation(false, revision, requested, candidates.errors(), candidates.warnings());
        }
        List<String> graphErrors = validateGraph(candidates.snapshots());
        return new Validation(
                graphErrors.isEmpty(),
                revision,
                requested,
                graphErrors,
                candidates.warnings());
    }

    public synchronized Publication reload(Collection<String> moduleIds, String source) {
        return reloadInternal(requested(moduleIds), boundedSource(source));
    }

    public synchronized Diff diff(String moduleId) {
        String normalized = registry.require(moduleId).id();
        CandidateSet candidates = parseCandidates(List.of(normalized));
        if (!candidates.errors().isEmpty()) {
            return new Diff(normalized, revision, Map.of(), candidates.errors());
        }
        ModuleSnapshot current = modules.get(normalized);
        ModuleSnapshot candidate = candidates.snapshots().get(normalized);
        Map<String, ValueChange> changes = changes(current, candidate);
        return new Diff(normalized, revision, changes, candidates.warnings());
    }

    public synchronized Publication publishSetting(
            String moduleId,
            String settingPath,
            String rawValue,
            long expectedRevision,
            UUID actorId
    ) {
        String normalizedModule = registry.require(moduleId).id();
        if (revision != expectedRevision) {
            return failure("configuration revision changed");
        }
        ModuleConfigRegistry.SettingDefinition setting = registry.require(normalizedModule)
                .settingsByPath()
                .get(normalizePath(settingPath));
        if (setting == null) {
            return failure("configuration setting is unknown");
        }
        String value;
        try {
            value = setting.validate(Objects.requireNonNull(rawValue, "rawValue"));
        } catch (IllegalArgumentException exception) {
            return failure(exception.getMessage());
        }
        ModuleSnapshot current = modules.get(normalizedModule);
        if (current == null) {
            return failure("configuration module is unavailable");
        }
        Map<String, String> values = desiredValues(current);
        values.put(setting.path(), value);
        ModuleSnapshot candidate = new ModuleSnapshot(
                current.moduleId(),
                current.schemaVersion(),
                current.documentationVersion(),
                current.revision(),
                values,
                current.unknownValues(),
                Instant.now());
        List<String> graphErrors = validateGraph(Map.of(normalizedModule, candidate));
        if (!graphErrors.isEmpty()) {
            return new Publication(false, revision, List.of(), diagnostics(), String.join(", ", graphErrors));
        }
        try {
            writeModule(candidate);
            Publication publication = reloadInternal(List.of(normalizedModule), "typed_editor");
            auditPublication(actorId, normalizedModule, setting.path(), publication);
            return publication;
        } catch (IOException exception) {
            diagnostic(DiagnosticSeverity.ERROR, normalizedModule, setting.path(), safeMessage(exception));
            return failure("configuration write failed");
        }
    }

    public synchronized Publication rollback(
            String moduleId,
            long historyRevision,
            long expectedRevision,
            UUID actorId
    ) {
        String normalized = registry.require(moduleId).id();
        if (revision != expectedRevision) {
            return failure("configuration revision changed");
        }
        ModuleHistory selected = history.getOrDefault(normalized, List.of()).stream()
                .filter(entry -> entry.revision() == historyRevision)
                .findFirst()
                .orElse(null);
        if (selected == null) {
            return failure("configuration history revision was not found");
        }
        try {
            writeModule(selected.snapshot());
            Publication publication = reloadInternal(List.of(normalized), "rollback");
            auditPublication(actorId, normalized, "rollback", publication);
            return publication;
        } catch (IOException exception) {
            diagnostic(DiagnosticSeverity.ERROR, normalized, "rollback", safeMessage(exception));
            return failure("configuration rollback failed");
        }
    }

    public synchronized MigrationReport legacyMigrationReport() {
        if (configRoot == null) {
            return new MigrationReport(
                    revision,
                    false,
                    "",
                    List.of(),
                    List.of(),
                    List.of("module configuration service is not started"),
                    "blocked");
        }
        try {
            if (migrationMarkerPublished()) {
                return new MigrationReport(
                        revision,
                        Files.exists(legacyConfigFile(), LinkOption.NOFOLLOW_LINKS),
                        "",
                        List.of(),
                        List.of(),
                        List.of(),
                        "already published");
            }
            LegacyMigrationPlan plan = legacyMigrationPlan();
            return new MigrationReport(
                    revision,
                    plan.legacyFilePresent(),
                    plan.sourceFingerprint(),
                    plan.mappings(),
                    plan.unmappedFields(),
                    plan.errors(),
                    plan.errors().isEmpty() ? "dry run ready" : "blocked");
        } catch (IOException | IllegalArgumentException exception) {
            return new MigrationReport(
                    revision,
                    Files.exists(legacyConfigFile(), LinkOption.NOFOLLOW_LINKS),
                    "",
                    List.of(),
                    List.of(),
                    List.of(safeMessage(exception)),
                    "blocked");
        }
    }

    public synchronized Publication migrateLegacy(
            long expectedRevision,
            String expectedFingerprint,
            UUID actorId
    ) {
        if (revision != expectedRevision) {
            return failure("configuration revision changed");
        }
        if (configRoot == null || modulesRoot == null) {
            return failure("module configuration service is not started");
        }
        try {
            if (migrationMarkerPublished()) {
                return new Publication(
                        true,
                        revision,
                        List.of(),
                        diagnostics(),
                        "legacy configuration migration was already published");
            }
            LegacyMigrationPlan plan = legacyMigrationPlan();
            if (!plan.legacyFilePresent()) {
                return failure("legacy common configuration file is missing");
            }
            if (!plan.errors().isEmpty()) {
                return failure(String.join(", ", plan.errors()));
            }
            if (!MessageDigest.isEqual(
                    plan.sourceFingerprint().getBytes(StandardCharsets.UTF_8),
                    Objects.requireNonNullElse(expectedFingerprint, "").getBytes(StandardCharsets.UTF_8))) {
                return failure("legacy common configuration changed after preview");
            }

            List<String> moduleIds = registry.definitions().stream()
                    .map(ModuleConfigRegistry.ModuleDefinition::id)
                    .toList();
            CandidateSet currentCandidates = parseCandidates(moduleIds);
            if (!currentCandidates.errors().isEmpty()) {
                return failure("current module configuration is invalid");
            }
            Map<String, ModuleSnapshot> candidates = migratedCandidates(
                    currentCandidates.snapshots(),
                    plan.values());
            List<String> graphErrors = validateGraph(candidates);
            if (!graphErrors.isEmpty()) {
                return failure(String.join(", ", graphErrors));
            }

            Map<String, String> originals = new LinkedHashMap<>();
            Map<String, String> staged = new LinkedHashMap<>();
            for (String moduleId : moduleIds) {
                ModuleConfigRegistry.ModuleDefinition definition = registry.require(moduleId);
                Path file = resolveModuleFile(definition);
                validateFile(file);
                originals.put(moduleId, decode(file));
                staged.put(moduleId, renderModule(definition, candidates.get(moduleId)));
            }
            Path stagingRoot = migrationStagingRoot();
            if (Files.exists(stagingRoot, LinkOption.NOFOLLOW_LINKS)) {
                return failure("legacy migration staging directory already exists");
            }
            Files.createDirectory(stagingRoot);
            try {
                validateMigrationDirectory(stagingRoot);
                for (String moduleId : moduleIds) {
                    Path stagedFile = stagingRoot.resolve(moduleId + ".toml").normalize();
                    writeAtomic(stagedFile, staged.get(moduleId));
                    List<String> warnings = new ArrayList<>();
                    parse(registry.require(moduleId), stagedFile, warnings);
                }
                validateGraph(candidates).stream().findFirst().ifPresent(error -> {
                    throw new IllegalArgumentException(error);
                });
                retainMigrationBackups(plan.sourceContent(), originals);
                validateFile(legacyConfigFile());
                if (!decode(legacyConfigFile()).equals(plan.sourceContent())) {
                    throw new IOException("legacy common configuration changed during migration");
                }
                for (String moduleId : moduleIds) {
                    Path current = resolveModuleFile(registry.require(moduleId));
                    validateFile(current);
                    if (!decode(current).equals(originals.get(moduleId))) {
                        throw new IOException("module configuration changed during migration");
                    }
                }

                List<String> published = new ArrayList<>();
                try {
                    for (String moduleId : moduleIds) {
                        writeAtomic(
                                resolveModuleFile(registry.require(moduleId)),
                                staged.get(moduleId));
                        published.add(moduleId);
                    }
                    Publication publication = reloadInternal(moduleIds, "legacy_migration");
                    if (!publication.successful()) {
                        throw new IOException(publication.detail());
                    }
                    writeMigrationMarker(plan, publication.revision());
                    auditPublication(actorId, "migration", "publish", publication);
                    return new Publication(
                            true,
                            publication.revision(),
                            publication.changedModules(),
                            publication.diagnostics(),
                            "legacy configuration migration published, common.toml retained");
                } catch (IOException | RuntimeException exception) {
                    restoreMigrationFiles(published, originals);
                    reloadInternal(moduleIds, "legacy_migration_recovery");
                    throw exception;
                }
            } finally {
                cleanupMigrationStaging(stagingRoot, moduleIds);
            }
        } catch (IOException | IllegalArgumentException exception) {
            diagnostic(DiagnosticSeverity.ERROR, "", "legacy_migration", safeMessage(exception));
            Publication publication = failure("legacy configuration migration failed, " + safeMessage(exception));
            auditPublication(actorId, "migration", "publish", publication);
            return publication;
        }
    }

    public synchronized DocumentationResult generateDocumentation(Path destination) {
        Objects.requireNonNull(destination, "destination");
        try {
            Path normalized = destination.toAbsolutePath().normalize();
            writeAtomic(normalized, registry.generatedReference());
            return new DocumentationResult(true, normalized, "");
        } catch (IOException | RuntimeException exception) {
            return new DocumentationResult(false, null, safeMessage(exception));
        }
    }

    public synchronized DocumentationResult generateDocumentation() {
        if (configRoot == null) {
            return new DocumentationResult(false, null, "module configuration service is not started");
        }
        return generateDocumentation(configRoot.resolve("CONFIGURATION_REFERENCE.md"));
    }

    private LegacyMigrationPlan legacyMigrationPlan() throws IOException {
        Path legacyFile = legacyConfigFile();
        if (!Files.exists(legacyFile, LinkOption.NOFOLLOW_LINKS)) {
            return new LegacyMigrationPlan(false, "", "", Map.of(), List.of(), List.of(), List.of());
        }
        validateFile(legacyFile);
        String content = decode(legacyFile);
        LegacyDocument document = parseLegacyToml(content);
        Map<MigrationTarget, String> values = new LinkedHashMap<>();
        List<String> mappings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Set<String> mappedPaths = new LinkedHashSet<>();
        for (LegacyMapping mapping : LEGACY_MAPPINGS) {
            String raw = document.values().get(mapping.legacyPath());
            if (raw == null) {
                continue;
            }
            mappedPaths.add(mapping.legacyPath());
            ModuleConfigRegistry.SettingDefinition setting = registry.require(mapping.target().moduleId())
                    .settingsByPath()
                    .get(mapping.target().settingPath());
            if (setting == null) {
                errors.add(mapping.legacyPath() + " has no owned destination");
                continue;
            }
            try {
                String converted = setting.validate(mapping.convert(raw));
                String existing = values.putIfAbsent(mapping.target(), converted);
                if (existing != null && !existing.equals(converted)) {
                    errors.add(mapping.target().display() + " receives conflicting legacy values");
                    continue;
                }
                mappings.add(mapping.legacyPath() + " to " + mapping.target().display());
            } catch (IllegalArgumentException exception) {
                errors.add(mapping.legacyPath() + ", " + safeMessage(exception));
            }
        }
        List<String> unmapped = document.fields().stream()
                .filter(path -> !mappedPaths.contains(path))
                .map(path -> path.contains("cooldown")
                        ? path + ", ignored, use sef.cooldown permissions"
                        : path + ", retained in common.toml")
                .sorted()
                .toList();
        return new LegacyMigrationPlan(
                true,
                fingerprint(content),
                content,
                Map.copyOf(values),
                List.copyOf(mappings),
                unmapped,
                List.copyOf(errors));
    }

    private Map<String, ModuleSnapshot> migratedCandidates(
            Map<String, ModuleSnapshot> current,
            Map<MigrationTarget, String> migratedValues
    ) {
        Map<String, ModuleSnapshot> candidates = new LinkedHashMap<>(current);
        for (Map.Entry<MigrationTarget, String> entry : migratedValues.entrySet()) {
            MigrationTarget target = entry.getKey();
            ModuleSnapshot existing = candidates.get(target.moduleId());
            if (existing == null) {
                throw new IllegalArgumentException("migration target module is unavailable");
            }
            Map<String, String> values = new LinkedHashMap<>(existing.values());
            values.put(target.settingPath(), entry.getValue());
            candidates.put(target.moduleId(), new ModuleSnapshot(
                    existing.moduleId(),
                    existing.schemaVersion(),
                    existing.documentationVersion(),
                    existing.revision(),
                    values,
                    existing.unknownValues(),
                    Instant.now()));
        }
        return Map.copyOf(candidates);
    }

    private void retainMigrationBackups(
            String legacyContent,
            Map<String, String> moduleContents
    ) throws IOException {
        Path root = migrationBackupRoot();
        ensureOwnedDirectoryTree(configRoot, root);
        validateMigrationDirectory(root);
        retainExactBackup(root.resolve("common.toml"), legacyContent);
        for (Map.Entry<String, String> entry : moduleContents.entrySet()) {
            retainExactBackup(root.resolve(entry.getKey() + ".toml"), entry.getValue());
        }
    }

    private static void retainExactBackup(Path destination, String content) throws IOException {
        if (Files.exists(destination, LinkOption.NOFOLLOW_LINKS)) {
            validateFile(destination);
            if (!decode(destination).equals(content)) {
                throw new IOException("legacy migration recovery backup conflicts with current input");
            }
            return;
        }
        writeAtomic(destination, content);
    }

    private void restoreMigrationFiles(
            List<String> published,
            Map<String, String> originals
    ) throws IOException {
        IOException failure = null;
        for (String moduleId : published.reversed()) {
            try {
                writeAtomic(
                        resolveModuleFile(registry.require(moduleId)),
                        originals.get(moduleId));
            } catch (IOException exception) {
                if (failure == null) {
                    failure = exception;
                } else {
                    failure.addSuppressed(exception);
                }
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    private void writeMigrationMarker(LegacyMigrationPlan plan, long publishedRevision) throws IOException {
        Path marker = migrationMarkerFile();
        if (Files.exists(marker, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("legacy migration marker already exists");
        }
        String content = "schema_version = " + LEGACY_MIGRATION_VERSION + "\n"
                + "source_file = \"common.toml\"\n"
                + "source_sha256 = \"" + plan.sourceFingerprint() + "\"\n"
                + "published_revision = " + publishedRevision + "\n"
                + "mapped_fields = " + plan.values().size() + "\n"
                + "legacy_file_retained = true\n"
                + "\n[mapped]\n";
        StringBuilder markerContent = new StringBuilder(content);
        for (int index = 0; index < plan.mappings().size(); index++) {
            markerContent.append("field_")
                    .append(index + 1)
                    .append(" = \"")
                    .append(escape(plan.mappings().get(index)))
                    .append("\"\n");
        }
        writeAtomic(marker, markerContent.toString());
    }

    private boolean migrationMarkerPublished() throws IOException {
        Path marker = migrationMarkerFile();
        if (!Files.exists(marker, LinkOption.NOFOLLOW_LINKS)) {
            return false;
        }
        validateFile(marker);
        Map<String, String> values = parseToml(decode(marker));
        if (!Integer.toString(LEGACY_MIGRATION_VERSION).equals(values.get("schema_version"))
                || !"common.toml".equals(values.get("source_file"))
                || !values.getOrDefault("source_sha256", "").matches("[0-9a-f]{64}")
                || !values.getOrDefault("published_revision", "").matches("[1-9][0-9]*")
                || !"true".equals(values.get("legacy_file_retained"))) {
            throw new IOException("legacy migration marker is invalid");
        }
        return true;
    }

    private Path legacyConfigFile() {
        if (configRoot == null) {
            throw new IllegalStateException("module configuration service is not started");
        }
        return ownedFile(configRoot, "common.toml");
    }

    private Path migrationMarkerFile() {
        if (modulesRoot == null) {
            throw new IllegalStateException("module configuration service is not started");
        }
        return ownedFile(modulesRoot, "migration.toml");
    }

    private Path migrationStagingRoot() {
        if (configRoot == null) {
            throw new IllegalStateException("module configuration service is not started");
        }
        return ownedFile(configRoot, "migration-staging");
    }

    private Path migrationBackupRoot() {
        if (configRoot == null) {
            throw new IllegalStateException("module configuration service is not started");
        }
        Path backups = ownedFile(configRoot, "backups");
        Path configuration = ownedFile(backups, "configuration");
        return ownedFile(configuration, "modular-migration-" + LEGACY_MIGRATION_VERSION);
    }

    private static Path ownedFile(Path parent, String name) {
        if (name == null || !name.matches("[a-z0-9_.-]{1,96}")) {
            throw new IllegalArgumentException("owned configuration name is invalid");
        }
        Path normalizedParent = parent.toAbsolutePath().normalize();
        Path path = normalizedParent.resolve(name).normalize();
        if (!normalizedParent.equals(path.getParent()) || !name.equals(path.getFileName().toString())) {
            throw new IllegalArgumentException("owned configuration path escaped its root");
        }
        return path;
    }

    private static void validateMigrationDirectory(Path directory) throws IOException {
        if (Files.isSymbolicLink(directory)
                || !Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("legacy migration directory is invalid");
        }
    }

    private static void ensureOwnedDirectoryTree(Path root, Path destination) throws IOException {
        Path normalizedRoot = root.toAbsolutePath().normalize();
        Path normalizedDestination = destination.toAbsolutePath().normalize();
        if (!normalizedDestination.startsWith(normalizedRoot)
                || normalizedDestination.equals(normalizedRoot)) {
            throw new IOException("legacy migration directory escaped its owned root");
        }
        Path current = normalizedRoot;
        for (Path segment : normalizedRoot.relativize(normalizedDestination)) {
            current = current.resolve(segment).normalize();
            if (Files.exists(current, LinkOption.NOFOLLOW_LINKS)) {
                if (Files.isSymbolicLink(current)
                        || !Files.isDirectory(current, LinkOption.NOFOLLOW_LINKS)) {
                    throw new IOException("legacy migration directory is invalid");
                }
            } else {
                Files.createDirectory(current);
            }
        }
    }

    private static void cleanupMigrationStaging(Path root, List<String> moduleIds) throws IOException {
        if (!Files.exists(root, LinkOption.NOFOLLOW_LINKS)) {
            return;
        }
        validateMigrationDirectory(root);
        for (String moduleId : moduleIds) {
            Files.deleteIfExists(ownedFile(root, moduleId + ".toml"));
        }
        try (var remaining = Files.list(root)) {
            if (remaining.findAny().isPresent()) {
                throw new IOException("legacy migration staging contains an unexpected file");
            }
        }
        Files.delete(root);
    }

    private static LegacyDocument parseLegacyToml(String content) {
        if (content == null || content.length() > MAXIMUM_MODULE_BYTES) {
            throw new IllegalArgumentException("legacy configuration content is outside bounds");
        }
        Map<String, String> values = new LinkedHashMap<>();
        List<String> fields = new ArrayList<>();
        String section = "";
        String[] lines = content.split("\\R", -1);
        if (lines.length > 50_000) {
            throw new IllegalArgumentException("legacy configuration line count is outside bounds");
        }
        Set<String> mappedPaths = LEGACY_MAPPINGS.stream()
                .map(LegacyMapping::legacyPath)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        for (int index = 0; index < lines.length; index++) {
            String line = stripComment(lines[index]).strip();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("[") && line.endsWith("]")) {
                section = line.substring(1, line.length() - 1).strip().toLowerCase(Locale.ROOT);
                if (!section.matches("[a-z0-9_.-]{1,160}")) {
                    throw new IllegalArgumentException("invalid legacy section on line " + (index + 1));
                }
                if (section.startsWith("serveressentialsforgemodconfig.")) {
                    section = section.substring("serveressentialsforgemodconfig.".length());
                } else if (section.equals("serveressentialsforgemodconfig")) {
                    section = "";
                }
                continue;
            }
            int separator = line.indexOf('=');
            if (separator < 1) {
                throw new IllegalArgumentException("invalid legacy assignment on line " + (index + 1));
            }
            String key = line.substring(0, separator).strip().toLowerCase(Locale.ROOT);
            if (!key.matches("[a-z0-9_]{1,96}")) {
                throw new IllegalArgumentException("invalid legacy key on line " + (index + 1));
            }
            String path = section.isEmpty() ? key : section + "." + key;
            if (fields.contains(path)) {
                throw new IllegalArgumentException("duplicate legacy key on line " + (index + 1));
            }
            fields.add(path);
            if (mappedPaths.contains(path)) {
                values.put(path, parseLiteral(line.substring(separator + 1).strip(), index + 1));
            }
        }
        return new LegacyDocument(Map.copyOf(values), List.copyOf(fields));
    }

    private static String fingerprint(String content) {
        try {
            return java.util.HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(content.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("sha 256 is unavailable", exception);
        }
    }

    private Publication reloadInternal(List<String> requested, String source) {
        List<String> affected = registry.dependencyClosure(requested);
        CandidateSet candidates = parseCandidates(affected);
        if (!candidates.errors().isEmpty()) {
            candidates.errors().forEach(message ->
                    diagnostic(DiagnosticSeverity.ERROR, "", source, message));
            return new Publication(false, revision, List.of(), diagnostics(), "configuration validation failed");
        }
        List<String> graphErrors = validateGraph(candidates.snapshots());
        if (!graphErrors.isEmpty()) {
            graphErrors.forEach(message ->
                    diagnostic(DiagnosticSeverity.ERROR, "", source, message));
            return new Publication(false, revision, List.of(), diagnostics(), "configuration dependency validation failed");
        }
        Map<String, ModuleSnapshot> replacements = new LinkedHashMap<>();
        List<String> applyWarnings = new ArrayList<>();
        Set<String> pendingChangedModules = new LinkedHashSet<>();
        if (source.equals("startup")) {
            pendingApply.clear();
        }
        for (String moduleId : affected) {
            ModuleSnapshot candidate = candidates.snapshots().get(moduleId);
            if (candidate != null) {
                replacements.put(
                        moduleId,
                        source.equals("startup")
                                ? candidate
                                : liveCandidate(
                                        moduleId,
                                        modules.get(moduleId),
                                        candidate,
                                        applyWarnings,
                                        pendingChangedModules));
            }
        }
        long nextRevision = Math.addExact(revision, 1L);
        List<String> changed = new ArrayList<>();
        for (Map.Entry<String, ModuleSnapshot> entry : replacements.entrySet()) {
            ModuleSnapshot previous = modules.get(entry.getKey());
            ModuleSnapshot replacement = entry.getValue().withRevision(nextRevision);
            if (!equivalent(previous, replacement) || pendingChangedModules.contains(entry.getKey())) {
                remember(previous);
                modules.put(entry.getKey(), replacement);
                changed.add(entry.getKey());
            }
        }
        if (!changed.isEmpty()) {
            revision = nextRevision;
        }
        diagnostics.removeIf(diagnostic -> diagnostic.severity() != DiagnosticSeverity.ERROR);
        for (String warning : candidates.warnings()) {
            diagnostic(DiagnosticSeverity.WARNING, "", source, warning);
        }
        for (String warning : applyWarnings) {
            diagnostic(DiagnosticSeverity.WARNING, "", source, warning);
        }
        Publication publication = new Publication(
                true,
                revision,
                List.copyOf(changed),
                diagnostics(),
                changed.isEmpty()
                        ? applyWarnings.isEmpty()
                        ? "configuration is unchanged"
                        : "configuration validated, restart required settings remain pending"
                        : applyWarnings.isEmpty()
                        ? "configuration published"
                        : "live settings published, restart required settings remain pending");
        if (!changed.isEmpty()) {
            notifyListeners(publication);
        }
        return publication;
    }

    private ModuleSnapshot liveCandidate(
            String moduleId,
            ModuleSnapshot current,
            ModuleSnapshot candidate,
            List<String> warnings,
            Set<String> pendingChangedModules
    ) {
        if (current == null) {
            return candidate;
        }
        Map<String, String> effective = new LinkedHashMap<>(candidate.values());
        for (ModuleConfigRegistry.SettingDefinition setting : registry.require(moduleId).settings()) {
            String previous = current.values().get(setting.path());
            String requested = candidate.values().get(setting.path());
            String pendingKey = moduleId + "." + setting.path();
            if (setting.applyClass() == ModuleConfigRegistry.ApplyClass.LIVE) {
                pendingApply.remove(pendingKey);
                continue;
            }
            if (!Objects.equals(previous, requested)) {
                effective.put(setting.path(), previous);
                if (!Objects.equals(pendingApply.put(pendingKey, requested), requested)) {
                    pendingChangedModules.add(moduleId);
                }
                warnings.add(moduleId + ", " + setting.path()
                        + " remains " + setting.applyClass().id()
                        + " pending a compatible lifecycle boundary");
            } else if (pendingApply.remove(pendingKey) != null) {
                pendingChangedModules.add(moduleId);
            }
        }
        return new ModuleSnapshot(
                candidate.moduleId(),
                candidate.schemaVersion(),
                candidate.documentationVersion(),
                candidate.revision(),
                effective,
                candidate.unknownValues(),
                candidate.loadedAt());
    }

    private Map<String, String> desiredValues(ModuleSnapshot current) {
        Map<String, String> values = new LinkedHashMap<>(current.values());
        String prefix = current.moduleId() + ".";
        pendingApply.forEach((path, value) -> {
            if (path.startsWith(prefix)) {
                values.put(path.substring(prefix.length()), value);
            }
        });
        return values;
    }

    private CandidateSet parseCandidates(List<String> moduleIds) {
        Map<String, ModuleSnapshot> snapshots = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        for (String moduleId : moduleIds) {
            ModuleConfigRegistry.ModuleDefinition definition = registry.require(moduleId);
            Path file;
            try {
                file = resolveModuleFile(definition);
                ModuleSnapshot snapshot = parse(definition, file, warnings);
                snapshots.put(moduleId, snapshot);
            } catch (IOException | IllegalArgumentException exception) {
                errors.add(moduleId + ", " + safeMessage(exception));
            }
        }
        return new CandidateSet(Map.copyOf(snapshots), List.copyOf(errors), List.copyOf(warnings));
    }

    private ModuleSnapshot parse(
            ModuleConfigRegistry.ModuleDefinition definition,
            Path file,
            List<String> warnings
    ) throws IOException {
        validateFile(file);
        return parse(definition, decode(file), warnings);
    }

    private ModuleSnapshot parse(
            ModuleConfigRegistry.ModuleDefinition definition,
            String content,
            List<String> warnings
    ) {
        Map<String, String> parsed = parseToml(content);
        String schema = parsed.remove("schema_version");
        String moduleId = parsed.remove("module_id");
        String documentationVersion = parsed.remove("documentation_version");
        if (!Integer.toString(ModuleConfigRegistry.SCHEMA_VERSION).equals(schema)) {
            throw new IllegalArgumentException("unsupported schema_version");
        }
        if (!definition.id().equals(moduleId)) {
            throw new IllegalArgumentException("module_id does not match its owned file");
        }
        int documentation = parsePositive(documentationVersion, "documentation_version");
        Map<String, String> values = new LinkedHashMap<>();
        Map<String, String> unknown = new LinkedHashMap<>();
        Map<String, ModuleConfigRegistry.SettingDefinition> known = definition.settingsByPath();
        for (ModuleConfigRegistry.SettingDefinition setting : definition.settings()) {
            String raw = parsed.remove(setting.path());
            if (raw == null) {
                warnings.add(definition.id() + ", missing " + setting.path() + ", internal default retained");
                raw = setting.defaultValue();
            }
            values.put(setting.path(), setting.validate(raw));
        }
        for (Map.Entry<String, String> entry : parsed.entrySet()) {
            if (entry.getKey().length() > 128 || entry.getValue().length() > 8192) {
                throw new IllegalArgumentException("unknown configuration field is outside bounds");
            }
            unknown.put(entry.getKey(), entry.getValue());
            warnings.add(definition.id() + ", unknown field preserved, " + entry.getKey());
        }
        if (known.size() != values.size()) {
            throw new IllegalStateException("configuration setting projection is incomplete");
        }
        return new ModuleSnapshot(
                definition.id(),
                ModuleConfigRegistry.SCHEMA_VERSION,
                documentation,
                revision,
                values,
                unknown,
                Instant.now());
    }

    private List<String> validateGraph(Map<String, ModuleSnapshot> candidates) {
        Map<String, ModuleSnapshot> effective = new LinkedHashMap<>(modules);
        effective.putAll(candidates);
        List<String> errors = new ArrayList<>();
        for (ModuleConfigRegistry.ModuleDefinition definition : registry.definitions()) {
            ModuleSnapshot snapshot = effective.get(definition.id());
            if (snapshot == null || !snapshot.enabled()) {
                continue;
            }
            for (String dependency : definition.dependencies()) {
                ModuleSnapshot dependencySnapshot = effective.get(dependency);
                if (dependencySnapshot == null || !dependencySnapshot.enabled()) {
                    errors.add(definition.id() + " requires enabled module " + dependency);
                }
            }
            for (String conflict : definition.conflicts()) {
                ModuleSnapshot conflictSnapshot = effective.get(conflict);
                if (conflictSnapshot != null && conflictSnapshot.enabled()) {
                    errors.add(definition.id() + " conflicts with enabled module " + conflict);
                }
            }
        }
        return List.copyOf(errors);
    }

    private void initializeDirectory() throws IOException {
        if (Files.exists(configRoot, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(configRoot)) {
            throw new IOException("configuration root cannot be a symbolic link");
        }
        Files.createDirectories(modulesRoot);
        if (Files.isSymbolicLink(modulesRoot)) {
            throw new IOException("module configuration root cannot be a symbolic link");
        }
        Files.createDirectories(historyRoot);
        if (!historyRoot.getParent().equals(modulesRoot) || Files.isSymbolicLink(historyRoot)) {
            throw new IOException("module configuration history root is invalid");
        }
        Path index = modulesRoot.resolve("index.toml");
        if (!Files.exists(index, LinkOption.NOFOLLOW_LINKS)) {
            writeAtomic(index, registry.defaultIndex());
        }
        for (ModuleConfigRegistry.ModuleDefinition definition : registry.definitions()) {
            Path file = resolveModuleFile(definition);
            if (!Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
                writeAtomic(file, registry.defaultFile(definition));
            }
        }
    }

    private void materializeDocumentationUpgrades() throws IOException {
        for (ModuleConfigRegistry.ModuleDefinition definition : registry.definitions()) {
            Path file = resolveModuleFile(definition);
            validateFile(file);
            String original = decode(file);
            List<String> warnings = new ArrayList<>();
            ModuleSnapshot snapshot = parse(definition, original, warnings);
            if (snapshot.documentationVersion() >= definition.documentationVersion()) {
                continue;
            }
            Path directory = historyDirectory(definition.id());
            Files.createDirectories(directory);
            if (Files.isSymbolicLink(directory)) {
                throw new IOException("module configuration history directory cannot be a symbolic link");
            }
            Path backup = directory.resolve(
                    "documentation-"
                            + snapshot.documentationVersion()
                            + "-to-"
                            + definition.documentationVersion()
                            + ".bak").normalize();
            if (!backup.getParent().equals(directory)) {
                throw new IOException("module configuration backup destination is invalid");
            }
            if (!Files.exists(backup, LinkOption.NOFOLLOW_LINKS)) {
                writeAtomic(backup, original);
            } else if (Files.isSymbolicLink(backup)
                    || !Files.isRegularFile(backup, LinkOption.NOFOLLOW_LINKS)) {
                throw new IOException("module configuration backup is invalid");
            } else {
                validateFile(backup);
            }
            if (!decode(backup).equals(original)) {
                throw new IOException("module configuration recovery backup conflicts with the current file");
            }
            validateFile(file);
            if (!decode(file).equals(original)) {
                throw new IOException("module configuration changed during documentation upgrade");
            }
            ModuleSnapshot upgraded = new ModuleSnapshot(
                    snapshot.moduleId(),
                    snapshot.schemaVersion(),
                    definition.documentationVersion(),
                    snapshot.revision(),
                    snapshot.values(),
                    snapshot.unknownValues(),
                    Instant.now());
            writeAtomic(file, renderModule(definition, upgraded));
            diagnostic(
                    DiagnosticSeverity.INFO,
                    definition.id(),
                    "documentation_upgrade",
                    "materialized documentation version "
                            + definition.documentationVersion()
                            + " after retaining a recovery backup");
        }
    }

    private void loadHistory() throws IOException {
        history.clear();
        long maximumRevision = revision;
        for (ModuleConfigRegistry.ModuleDefinition definition : registry.definitions()) {
            Path directory = historyDirectory(definition.id());
            if (!Files.exists(directory, LinkOption.NOFOLLOW_LINKS)) {
                continue;
            }
            if (Files.isSymbolicLink(directory) || !Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) {
                throw new IOException("module configuration history directory is invalid");
            }
            List<Path> files;
            try (var stream = Files.list(directory)) {
                files = stream
                        .filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
                        .filter(ModuleConfigService::isHistoryFile)
                        .sorted(Comparator.comparingLong(ModuleConfigService::historyFileRevision).reversed())
                        .limit(MAXIMUM_HISTORY_PER_MODULE)
                        .toList();
            }
            List<ModuleHistory> entries = new ArrayList<>();
            for (Path file : files) {
                try {
                    long historyRevision = historyFileRevision(file);
                    List<String> warnings = new ArrayList<>();
                    ModuleSnapshot snapshot = parse(definition, file, warnings).withRevision(historyRevision);
                    Instant recordedAt = Files.getLastModifiedTime(file).toInstant();
                    entries.add(new ModuleHistory(historyRevision, recordedAt, snapshot));
                    maximumRevision = Math.max(maximumRevision, historyRevision);
                    warnings.forEach(message ->
                            diagnostic(DiagnosticSeverity.WARNING, definition.id(), "history_load", message));
                } catch (IOException | IllegalArgumentException exception) {
                    diagnostic(
                            DiagnosticSeverity.WARNING,
                            definition.id(),
                            "history_load",
                            safeMessage(exception));
                }
            }
            if (!entries.isEmpty()) {
                history.put(definition.id(), List.copyOf(entries));
            }
            pruneHistoryFiles(directory);
        }
        revision = maximumRevision;
    }

    private Path historyDirectory(String moduleId) {
        String normalized = registry.require(moduleId).id();
        Path directory = historyRoot.resolve(normalized).normalize();
        if (!directory.getParent().equals(historyRoot)
                || !directory.getFileName().toString().equals(normalized)) {
            throw new IllegalArgumentException("module configuration history path escaped its owned root");
        }
        return directory;
    }

    private static long historyFileRevision(Path file) {
        String name = file.getFileName().toString();
        if (!name.matches("[1-9][0-9]{0,18}\\.toml")) {
            throw new IllegalArgumentException("module configuration history file name is invalid");
        }
        try {
            return Long.parseLong(name.substring(0, name.length() - 5));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("module configuration history revision is invalid", exception);
        }
    }

    private static boolean isHistoryFile(Path file) {
        try {
            return historyFileRevision(file) > 0L;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static void pruneHistoryFiles(Path directory) throws IOException {
        List<Path> files;
        try (var stream = Files.list(directory)) {
            files = stream
                    .filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
                    .filter(ModuleConfigService::isHistoryFile)
                    .sorted(Comparator.comparingLong(ModuleConfigService::historyFileRevision).reversed())
                    .toList();
        }
        for (int index = MAXIMUM_HISTORY_PER_MODULE; index < files.size(); index++) {
            Files.deleteIfExists(files.get(index));
        }
    }

    private void writeModule(ModuleSnapshot snapshot) throws IOException {
        ModuleConfigRegistry.ModuleDefinition definition = registry.require(snapshot.moduleId());
        writeAtomic(resolveModuleFile(definition), renderModule(definition, snapshot));
    }

    private static String renderModule(
            ModuleConfigRegistry.ModuleDefinition definition,
            ModuleSnapshot snapshot
    ) {
        StringBuilder output = new StringBuilder();
        output.append("# ").append(definition.purpose()).append("\n");
        output.append("# Written through the typed SEF configuration service.\n");
        output.append("# This file cannot grant permissions or execute commands.\n");
        output.append("schema_version = ").append(snapshot.schemaVersion()).append("\n");
        output.append("module_id = \"").append(snapshot.moduleId()).append("\"\n");
        output.append("documentation_version = ").append(snapshot.documentationVersion()).append("\n");
        Map<String, List<String>> sections = new LinkedHashMap<>();
        for (ModuleConfigRegistry.SettingDefinition setting : definition.settings()) {
            sections.computeIfAbsent(setting.section(), ignored -> new ArrayList<>())
                    .add("# " + setting.description() + " "
                            + setting.boundsDescription() + " "
                            + "Apply class " + setting.applyClass().id() + ".\n"
                            + setting.key() + " = "
                            + toToml(setting.type(), snapshot.values().get(setting.path())));
        }
        for (Map.Entry<String, String> entry : snapshot.unknownValues().entrySet()) {
            String path = entry.getKey();
            int separator = path.indexOf('.');
            if (separator < 1 || separator == path.length() - 1) {
                continue;
            }
            sections.computeIfAbsent(path.substring(0, separator), ignored -> new ArrayList<>())
                    .add(path.substring(separator + 1)
                            + " = \"" + escape(entry.getValue()) + "\"");
        }
        for (Map.Entry<String, List<String>> section : sections.entrySet()) {
            output.append("\n[").append(section.getKey()).append("]\n");
            section.getValue().forEach(line -> output.append(line).append("\n"));
        }
        return output.toString();
    }

    private void startWatcher() throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        modulesRoot.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
        watcherExecutor = Executors.newScheduledThreadPool(2, task ->
                Thread.ofPlatform().daemon(true).name("sef-config-modules").unstarted(task));
        watcherExecutor.execute(this::watchLoop);
        reconciliationTask = watcherExecutor.scheduleWithFixedDelay(
                this::scheduleReconciliation,
                30L,
                30L,
                TimeUnit.SECONDS);
    }

    private void watchLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                WatchKey key = watchService.take();
                LinkedHashSet<String> changed = new LinkedHashSet<>();
                boolean overflow = false;
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == OVERFLOW) {
                        overflow = true;
                        continue;
                    }
                    Object context = event.context();
                    if (!(context instanceof Path path)) {
                        continue;
                    }
                    String fileName = path.getFileName().toString();
                    if (fileName.equals("gui_overrides.toml")) {
                        scheduleGuiOverrideReload();
                        continue;
                    }
                    if (fileName.matches("[a-z0-9_]+\\.toml") && !fileName.equals("index.toml")) {
                        String moduleId = fileName.substring(0, fileName.length() - 5);
                        if (registry.contains(moduleId)) {
                            changed.add(moduleId);
                        }
                    }
                }
                if (!key.reset()) {
                    diagnosticThreadSafe(
                            DiagnosticSeverity.ERROR,
                            "",
                            "watcher",
                            "configuration watcher key became invalid");
                    return;
                }
                scheduleWatcherReload(overflow
                        ? registry.definitions().stream()
                        .map(ModuleConfigRegistry.ModuleDefinition::id)
                        .toList()
                        : List.copyOf(changed));
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            } catch (RuntimeException exception) {
                diagnosticThreadSafe(
                        DiagnosticSeverity.ERROR,
                        "",
                        "watcher",
                        safeMessage(exception));
            }
        }
    }

    private synchronized void scheduleWatcherReload(List<String> changed) {
        if (changed.isEmpty() || watcherExecutor == null) {
            return;
        }
        if (pendingModuleReload != null) {
            pendingModuleReload.cancel(false);
        }
        pendingModuleReload = watcherExecutor.schedule(() ->
                        publicationExecutor.accept(() -> {
                            synchronized (ModuleConfigService.this) {
                                reloadInternal(changed, "watcher");
                            }
                        }),
                WATCH_DEBOUNCE_MILLISECONDS,
                TimeUnit.MILLISECONDS);
    }

    private void scheduleReconciliation() {
        try {
            publicationExecutor.accept(() -> {
                synchronized (ModuleConfigService.this) {
                    if (!running) {
                        return;
                    }
                    reloadInternal(
                            registry.definitions().stream()
                                    .map(ModuleConfigRegistry.ModuleDefinition::id)
                                    .toList(),
                            "reconciliation");
                }
            });
        } catch (RuntimeException exception) {
            diagnosticThreadSafe(
                    DiagnosticSeverity.ERROR,
                    "",
                    "reconciliation",
                    safeMessage(exception));
        }
    }

    private synchronized void scheduleGuiOverrideReload() {
        if (watcherExecutor == null) {
            return;
        }
        if (pendingGuiReload != null) {
            pendingGuiReload.cancel(false);
        }
        pendingGuiReload = watcherExecutor.schedule(() ->
                        publicationExecutor.accept(() -> {
                            synchronized (ModuleConfigService.this) {
                                try {
                                    loadGuiOverrides(true);
                                } catch (IOException | IllegalArgumentException exception) {
                                    diagnostic(
                                            DiagnosticSeverity.ERROR,
                                            "gui",
                                            "action_override_reload",
                                            safeMessage(exception));
                                }
                            }
                        }),
                WATCH_DEBOUNCE_MILLISECONDS,
                TimeUnit.MILLISECONDS);
    }

    private void loadGuiOverrides(boolean publish) throws IOException {
        Path file = modulesRoot.resolve("gui_overrides.toml").normalize();
        if (!file.getParent().equals(modulesRoot) || Files.isSymbolicLink(file)) {
            throw new IOException("GUI override path is invalid");
        }
        Map<String, String> loaded = new LinkedHashMap<>();
        if (Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
            validateFile(file);
            Map<String, String> parsed = parseToml(decode(file));
            if (!"1".equals(parsed.remove("schema_version"))) {
                throw new IllegalArgumentException("unsupported GUI override schema");
            }
            for (Map.Entry<String, String> entry : parsed.entrySet()) {
                if (!entry.getKey().startsWith("actions.a_")) {
                    throw new IllegalArgumentException("unknown GUI override field");
                }
                String action = decodeAction(entry.getKey().substring("actions.a_".length()));
                String mode = normalizeGuiOverride(entry.getValue());
                if (mode.equals("inherit") || loaded.putIfAbsent(action, mode) != null) {
                    throw new IllegalArgumentException("duplicate or redundant GUI action override");
                }
            }
        }
        if (loaded.size() > 8192) {
            throw new IllegalArgumentException("GUI action override capacity reached");
        }
        if (loaded.equals(guiActionModes)) {
            return;
        }
        guiActionModes.clear();
        guiActionModes.putAll(loaded);
        if (publish) {
            revision = Math.addExact(revision, 1L);
            notifyListeners(new Publication(
                    true,
                    revision,
                    List.of("gui"),
                    diagnostics(),
                    "GUI action policy published"));
        }
    }

    private void writeGuiOverrides(Map<String, String> values) throws IOException {
        StringBuilder output = new StringBuilder();
        output.append("# Typed per action enhanced GUI policy overrides.\n");
        output.append("schema_version = 1\n\n[actions]\n");
        values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> output.append("a_")
                        .append(encodeAction(entry.getKey()))
                        .append(" = \"")
                        .append(entry.getValue())
                        .append("\"\n"));
        writeAtomic(modulesRoot.resolve("gui_overrides.toml"), output.toString());
    }

    private void notifyListeners(Publication publication) {
        for (Consumer<Publication> listener : List.copyOf(publicationListeners)) {
            try {
                listener.accept(publication);
            } catch (RuntimeException exception) {
                diagnostic(
                        DiagnosticSeverity.ERROR,
                        "",
                        "publication_listener",
                        safeMessage(exception));
            }
        }
    }

    private Path resolveModuleFile(ModuleConfigRegistry.ModuleDefinition definition) {
        if (modulesRoot == null) {
            throw new IllegalStateException("module configuration service is not started");
        }
        Path path = modulesRoot.resolve(definition.fileName()).normalize();
        if (!path.getParent().equals(modulesRoot)
                || !path.getFileName().toString().equals(definition.fileName())) {
            throw new IllegalArgumentException("module configuration path escaped its owned root");
        }
        return path;
    }

    private static Path validateRoot(Path path) {
        Path normalized = Objects.requireNonNull(path, "path").toAbsolutePath().normalize();
        if (normalized.getParent() == null || normalized.getFileName() == null) {
            throw new IllegalArgumentException("configuration root is too broad");
        }
        return normalized;
    }

    private static void validateFile(Path file) throws IOException {
        if (!Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("module file is missing");
        }
        if (Files.isSymbolicLink(file) || !Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("module file is not an owned regular file");
        }
        long size = Files.size(file);
        if (size < 1 || size > MAXIMUM_MODULE_BYTES) {
            throw new IOException("module file size is outside bounds");
        }
        rejectHardLink(file);
    }

    private static void rejectHardLink(Path file) throws IOException {
        try {
            Object links = Files.getAttribute(file, "unix:nlink", LinkOption.NOFOLLOW_LINKS);
            if (links instanceof Number count && count.longValue() > 1L) {
                throw new IOException("module file cannot have multiple hard links");
            }
        } catch (UnsupportedOperationException ignored) {
        }
    }

    private static String decode(Path file) throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException exception) {
            throw new IOException("module file is not valid UTF 8", exception);
        }
    }

    static Map<String, String> parseToml(String content) {
        if (content == null || content.length() > MAXIMUM_MODULE_BYTES) {
            throw new IllegalArgumentException("module content is outside bounds");
        }
        Map<String, String> values = new LinkedHashMap<>();
        String section = "";
        String[] lines = content.split("\\R", -1);
        if (lines.length > 50_000) {
            throw new IllegalArgumentException("module line count is outside bounds");
        }
        for (int index = 0; index < lines.length; index++) {
            String line = stripComment(lines[index]).strip();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("[") && line.endsWith("]")) {
                section = line.substring(1, line.length() - 1).strip().toLowerCase(Locale.ROOT);
                if (!section.matches("[a-z0-9_]{1,64}")) {
                    throw new IllegalArgumentException("invalid section on line " + (index + 1));
                }
                continue;
            }
            int separator = line.indexOf('=');
            if (separator < 1) {
                throw new IllegalArgumentException("invalid assignment on line " + (index + 1));
            }
            String key = line.substring(0, separator).strip().toLowerCase(Locale.ROOT);
            String literal = line.substring(separator + 1).strip();
            if (!key.matches("[a-z0-9_]{1,64}")) {
                throw new IllegalArgumentException("invalid key on line " + (index + 1));
            }
            String path = section.isEmpty() ? key : section + "." + key;
            String value = parseLiteral(literal, index + 1);
            if (values.putIfAbsent(path, value) != null) {
                throw new IllegalArgumentException("duplicate key on line " + (index + 1));
            }
        }
        return values;
    }

    private static String stripComment(String line) {
        boolean quoted = false;
        boolean escaped = false;
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (character == '\\' && quoted) {
                escaped = true;
                continue;
            }
            if (character == '"') {
                quoted = !quoted;
                continue;
            }
            if (character == '#' && !quoted) {
                return line.substring(0, index);
            }
        }
        if (quoted) {
            throw new IllegalArgumentException("unterminated quoted value");
        }
        return line;
    }

    private static String parseLiteral(String literal, int line) {
        if (literal.startsWith("\"")) {
            if (literal.length() < 2 || !literal.endsWith("\"")) {
                throw new IllegalArgumentException("unterminated string on line " + line);
            }
            StringBuilder value = new StringBuilder();
            boolean escaped = false;
            for (int index = 1; index < literal.length() - 1; index++) {
                char character = literal.charAt(index);
                if (escaped) {
                    value.append(switch (character) {
                        case '"', '\\' -> character;
                        case 'n' -> '\n';
                        case 'r' -> '\r';
                        case 't' -> '\t';
                        default -> throw new IllegalArgumentException("unsupported escape on line " + line);
                    });
                    escaped = false;
                } else if (character == '\\') {
                    escaped = true;
                } else {
                    value.append(character);
                }
            }
            if (escaped) {
                throw new IllegalArgumentException("unterminated escape on line " + line);
            }
            return value.toString();
        }
        if (literal.equals("true")
                || literal.equals("false")
                || literal.matches("-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?")) {
            return literal;
        }
        throw new IllegalArgumentException("unsupported literal on line " + line);
    }

    private static void writeAtomic(Path destination, String content) throws IOException {
        Path parent = destination.toAbsolutePath().normalize().getParent();
        if (parent == null) {
            throw new IOException("configuration destination is too broad");
        }
        Files.createDirectories(parent);
        if (Files.isSymbolicLink(parent)
                || (Files.exists(destination, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(destination))) {
            throw new IOException("configuration destination cannot be a symbolic link");
        }
        if (Files.exists(destination, LinkOption.NOFOLLOW_LINKS)) {
            rejectHardLink(destination);
        }
        Set<PosixFilePermission> permissions = OWNER_PERMISSIONS;
        try {
            if (Files.exists(destination, LinkOption.NOFOLLOW_LINKS)) {
                permissions = Files.getPosixFilePermissions(destination, LinkOption.NOFOLLOW_LINKS);
            }
        } catch (UnsupportedOperationException ignored) {
        }
        Path temporary;
        try {
            temporary = Files.createTempFile(
                    parent,
                    "." + destination.getFileName(),
                    ".tmp",
                    PosixFilePermissions.asFileAttribute(OWNER_PERMISSIONS));
        } catch (UnsupportedOperationException exception) {
            temporary = Files.createTempFile(parent, "." + destination.getFileName(), ".tmp");
        }
        try {
            Files.writeString(
                    temporary,
                    content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) {
                channel.force(true);
            }
            try {
                Files.setPosixFilePermissions(temporary, permissions);
            } catch (UnsupportedOperationException ignored) {
            }
            try {
                Files.move(
                        temporary,
                        destination,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private void remember(ModuleSnapshot previous) {
        if (previous == null) {
            return;
        }
        try {
            ModuleConfigRegistry.ModuleDefinition definition = registry.require(previous.moduleId());
            Path directory = historyDirectory(previous.moduleId());
            Files.createDirectories(directory);
            if (Files.isSymbolicLink(directory)) {
                throw new IOException("module configuration history directory cannot be a symbolic link");
            }
            Path destination = directory.resolve(previous.revision() + ".toml").normalize();
            if (!destination.getParent().equals(directory)) {
                throw new IOException("module configuration history destination is invalid");
            }
            writeAtomic(destination, renderModule(definition, previous));
            pruneHistoryFiles(directory);
        } catch (IOException | RuntimeException exception) {
            diagnostic(
                    DiagnosticSeverity.ERROR,
                    previous.moduleId(),
                    "history_write",
                    safeMessage(exception));
        }
        List<ModuleHistory> entries = new ArrayList<>(history.getOrDefault(previous.moduleId(), List.of()));
        entries.add(0, new ModuleHistory(previous.revision(), Instant.now(), previous));
        if (entries.size() > MAXIMUM_HISTORY_PER_MODULE) {
            entries = new ArrayList<>(entries.subList(0, MAXIMUM_HISTORY_PER_MODULE));
        }
        history.put(previous.moduleId(), List.copyOf(entries));
    }

    private static Map<String, ValueChange> changes(ModuleSnapshot current, ModuleSnapshot candidate) {
        Map<String, ValueChange> changes = new LinkedHashMap<>();
        if (candidate == null) {
            return Map.of();
        }
        for (Map.Entry<String, String> entry : candidate.values().entrySet()) {
            String before = current == null ? "" : current.values().get(entry.getKey());
            if (!Objects.equals(before, entry.getValue())) {
                changes.put(entry.getKey(), new ValueChange(before, entry.getValue()));
            }
        }
        return Map.copyOf(changes);
    }

    private static List<LegacyMapping> legacyMappings() {
        List<LegacyMapping> mappings = new ArrayList<>();
        addToggle(mappings, "modules.msg_system", "private_messages");
        addToggle(mappings, "modules.vanish_system", "vanish");
        addToggle(mappings, "modules.mute_system", "mutes");
        addToggle(mappings, "modules.warn_system", "warnings");
        addToggle(mappings, "modules.freeze_system", "freeze");
        addToggle(mappings, "modules.disable_building", "building_control");
        addToggle(mappings, "modules.inv_lock", "inventory_lock");
        addToggle(mappings, "modules.sudo", "sudo");
        addToggle(mappings, "modules.invsee", "inventory");
        addToggle(mappings, "modules.crafting_table", "craft");
        addToggle(mappings, "modules.anvil", "anvil");
        addToggle(mappings, "modules.enchanting_table", "enchanting");
        addToggle(mappings, "modules.super_enchanting_table", "super_enchanting");
        addToggle(mappings, "modules.repair", "repair");
        addToggle(mappings, "modules.homes", "homes");
        addToggle(mappings, "modules.teleport_requests", "teleport_requests");
        addToggle(mappings, "modules.back", "back");
        addToggle(mappings, "modules.spawn", "spawn");
        addToggle(mappings, "modules.server_warps", "warps");
        addToggle(mappings, "modules.player_warps", "player_warps");
        addToggle(mappings, "modules.random_teleport", "random_teleport");
        addToggle(mappings, "modules.direct_teleport", "direct_teleport");
        addToggle(mappings, "modules.social_essentials", "social");
        addToggle(mappings, "modules.social_spy", "social_spy");
        addToggle(mappings, "modules.mail", "mail");
        addToggle(mappings, "modules.connection_messages", "connection_messages");
        addToggle(mappings, "modules.reminders", "reminders");
        addToggle(mappings, "modules.moderation_essentials", "moderation");
        addToggle(mappings, "modules.command_spy", "command_spy");
        addToggle(mappings, "modules.jails", "jails");
        addToggle(mappings, "modules.additional_workstations", "workstations");
        addToggle(mappings, "modules.kits", "kits");
        addToggle(mappings, "modules.inventory_utilities", "inventory");
        addToggle(mappings, "modules.player_utilities", "player_utilities");
        addToggle(mappings, "modules.gamemode_shortcuts", "gamemode");
        addToggle(mappings, "modules.item_shortcut", "items");
        addToggle(mappings, "modules.economy", "economy");
        addToggle(mappings, "modules.economy_signs", "economy_signs");
        mappings.add(mapping("gui.enabled", "gui", "gui.mode", LegacyConversion.GUI_MODE));
        mappings.add(mapping(
                "gui.reminderenabled",
                "gui",
                "reminder.enabled",
                LegacyConversion.DIRECT));
        mappings.add(mapping(
                "gui.panelsessionseconds",
                "gui",
                "sessions.timeout_seconds",
                LegacyConversion.DIRECT));
        mappings.add(mapping(
                "gui.maximumpanelentries",
                "gui",
                "limits.maximum_page_size",
                LegacyConversion.DIRECT));
        mappings.add(mapping(
                "virtualworkstations.superenchantingminlevel",
                "super_enchanting",
                "safety.minimum_level",
                LegacyConversion.DIRECT));
        mappings.add(mapping(
                "virtualworkstations.superenchantingmaxlevel",
                "super_enchanting",
                "safety.maximum_level",
                LegacyConversion.DIRECT));
        mappings.add(mapping(
                "virtualworkstations.superenchantingallowunsafe",
                "super_enchanting",
                "safety.allow_unsafe_levels",
                LegacyConversion.DIRECT));
        mappings.add(mapping(
                "virtualworkstations.superenchantingallowunsafe",
                "super_enchanting",
                "safety.allow_incompatible",
                LegacyConversion.DIRECT));
        mappings.add(mapping(
                "virtualworkstations.enablesuperenchantingtablealias",
                "super_enchanting",
                "shortcuts.enable_set",
                LegacyConversion.DIRECT));
        mappings.add(mapping(
                "phasesevenutilities.maximumkits",
                "kits",
                "limits.maximum_records",
                LegacyConversion.DIRECT));
        mappings.add(mapping(
                "economy.maximumaccounts",
                "economy",
                "limits.maximum_records",
                LegacyConversion.DIRECT));
        mappings.add(mapping(
                "filelogging.retentiondays",
                "logger",
                "storage.retention_days",
                LegacyConversion.DIRECT));
        mappings.add(mapping(
                "fancytags.enabled",
                "fancy_tags",
                "module.enabled",
                LegacyConversion.DIRECT));
        mappings.add(mapping(
                "fancytags.maximumtags",
                "fancy_tags",
                "limits.maximum_records",
                LegacyConversion.DIRECT));
        mappings.add(mapping(
                "disguise.enabled",
                "disguise",
                "module.enabled",
                LegacyConversion.DIRECT));
        mappings.add(mapping(
                "disguise.maximumactive",
                "disguise",
                "limits.maximum_records",
                LegacyConversion.DIRECT));
        for (var binding : ConfigHandler.runtimeBindings()) {
            mappings.add(mapping(
                    binding.value().legacyPathString(),
                    binding.moduleId(),
                    binding.settingPath(),
                    LegacyConversion.DIRECT));
        }
        return List.copyOf(mappings);
    }

    private static void addToggle(List<LegacyMapping> mappings, String legacyPath, String moduleId) {
        mappings.add(mapping(legacyPath, moduleId, "module.enabled", LegacyConversion.DIRECT));
    }

    private static LegacyMapping mapping(
            String legacyPath,
            String moduleId,
            String settingPath,
            LegacyConversion conversion
    ) {
        return new LegacyMapping(
                legacyPath,
                new MigrationTarget(moduleId, settingPath),
                conversion);
    }

    private static boolean equivalent(ModuleSnapshot first, ModuleSnapshot second) {
        return first != null
                && second != null
                && first.schemaVersion() == second.schemaVersion()
                && first.documentationVersion() == second.documentationVersion()
                && first.values().equals(second.values())
                && first.unknownValues().equals(second.unknownValues());
    }

    private List<String> requested(Collection<String> moduleIds) {
        if (moduleIds == null || moduleIds.isEmpty()) {
            return registry.definitions().stream()
                    .map(ModuleConfigRegistry.ModuleDefinition::id)
                    .toList();
        }
        LinkedHashSet<String> requested = new LinkedHashSet<>();
        for (String moduleId : moduleIds) {
            requested.add(registry.require(moduleId).id());
        }
        if (requested.size() > registry.definitions().size()) {
            throw new IllegalArgumentException("too many configuration modules requested");
        }
        return List.copyOf(requested);
    }

    private static String normalize(String value) {
        return Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizePath(String value) {
        String normalized = normalize(value);
        if (!normalized.matches("[a-z0-9_]+\\.[a-z0-9_]+")) {
            throw new IllegalArgumentException("configuration setting path is invalid");
        }
        return normalized;
    }

    private static String normalizeAction(String value) {
        String normalized = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("sef:[a-z0-9_.-]{1,120}")) {
            throw new IllegalArgumentException("GUI action id is invalid");
        }
        return normalized;
    }

    private static String normalizeGuiOverride(String value) {
        String normalized = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (!Set.of("inherit", "off", "on", "command_only", "gui_preferred").contains(normalized)) {
            throw new IllegalArgumentException("GUI action mode is invalid");
        }
        return normalized;
    }

    private static String encodeAction(String actionId) {
        return java.util.HexFormat.of().formatHex(actionId.getBytes(StandardCharsets.UTF_8));
    }

    private static String decodeAction(String encoded) {
        if (!encoded.matches("[0-9a-f]{2,256}") || (encoded.length() & 1) != 0) {
            throw new IllegalArgumentException("GUI action override key is invalid");
        }
        return normalizeAction(new String(java.util.HexFormat.of().parseHex(encoded), StandardCharsets.UTF_8));
    }

    private static String boundedSource(String value) {
        String source = Objects.requireNonNullElse(value, "command").trim().toLowerCase(Locale.ROOT);
        return source.matches("[a-z0-9_]{1,32}") ? source : "command";
    }

    private synchronized void diagnostic(
            DiagnosticSeverity severity,
            String moduleId,
            String operation,
            String message
    ) {
        if (diagnostics.size() >= 512) {
            diagnostics.removeFirst();
        }
        diagnostics.add(new Diagnostic(
                severity,
                moduleId,
                operation,
                message.length() > 512 ? message.substring(0, 512) : message,
                Instant.now()));
    }

    private void diagnosticThreadSafe(
            DiagnosticSeverity severity,
            String moduleId,
            String operation,
            String message
    ) {
        synchronized (this) {
            diagnostic(severity, moduleId, operation, message);
        }
    }

    private Publication failure(String message) {
        return new Publication(false, revision, List.of(), diagnostics(), message);
    }

    private static String safeMessage(Throwable exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank()
                ? exception.getClass().getSimpleName()
                : message.replace('\\', '/').replaceAll("(?i)(/[a-z0-9_. -]+)+", "<path>");
    }

    private static int parsePositive(String value, String field) {
        if (value == null || !value.matches("[1-9][0-9]*")) {
            throw new IllegalArgumentException(field + " must be a positive integer");
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(field + " is outside bounds");
        }
    }

    private static String toToml(ModuleConfigRegistry.ValueType type, String value) {
        return type == ModuleConfigRegistry.ValueType.BOOLEAN
                || type == ModuleConfigRegistry.ValueType.INTEGER
                || type == ModuleConfigRegistry.ValueType.DECIMAL
                ? value
                : "\"" + escape(value) + "\"";
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static void auditPublication(
            UUID actorId,
            String moduleId,
            String setting,
            Publication publication
    ) {
        com.enviouse.sef.audit.AuditService.record(com.enviouse.sef.audit.AuditService.Event.metadata(
                UUID.randomUUID(),
                Objects.requireNonNullElse(actorId, new UUID(0L, 0L)),
                actorId == null ? "console" : actorId.toString(),
                actorId == null ? "console" : "player",
                "sef:config." + moduleId + "." + setting.replace('.', '_'),
                List.of(),
                publication.successful()
                        ? com.enviouse.sef.audit.AuditService.Result.SUCCESS
                        : com.enviouse.sef.audit.AuditService.Result.REJECTED,
                publication.successful()
                        ? com.enviouse.sef.kernel.ActionResult.ReasonCode.SUCCESS
                        : com.enviouse.sef.kernel.ActionResult.ReasonCode.POLICY_DENIED,
                "module_configuration",
                com.enviouse.sef.audit.AuditService.AuditClass.ADMIN_ACTION));
    }

    private record CandidateSet(
            Map<String, ModuleSnapshot> snapshots,
            List<String> errors,
            List<String> warnings
    ) {
    }

    public record ModuleSnapshot(
            String moduleId,
            int schemaVersion,
            int documentationVersion,
            long revision,
            Map<String, String> values,
            Map<String, String> unknownValues,
            Instant loadedAt
    ) {
        public ModuleSnapshot {
            moduleId = normalize(moduleId);
            values = Map.copyOf(values);
            unknownValues = Map.copyOf(unknownValues);
            Objects.requireNonNull(loadedAt, "loadedAt");
        }

        public boolean enabled() {
            return Boolean.parseBoolean(values.getOrDefault("module.enabled", "false"));
        }

        private ModuleSnapshot withRevision(long replacementRevision) {
            return new ModuleSnapshot(
                    moduleId,
                    schemaVersion,
                    documentationVersion,
                    replacementRevision,
                    values,
                    unknownValues,
                    loadedAt);
        }
    }

    public record ModuleHistory(long revision, Instant recordedAt, ModuleSnapshot snapshot) {
    }

    public record Diagnostic(
            DiagnosticSeverity severity,
            String moduleId,
            String operation,
            String message,
            Instant occurredAt
    ) {
    }

    public enum DiagnosticSeverity {
        INFO,
        WARNING,
        ERROR
    }

    public record Publication(
            boolean successful,
            long revision,
            List<String> changedModules,
            List<Diagnostic> diagnostics,
            String detail
    ) {
    }

    public record Validation(
            boolean successful,
            long revision,
            List<String> modules,
            List<String> errors,
            List<String> warnings
    ) {
    }

    public record Diff(
            String moduleId,
            long revision,
            Map<String, ValueChange> changes,
            List<String> diagnostics
    ) {
    }

    public record ValueChange(String before, String after) {
    }

    public record MigrationReport(
            long revision,
            boolean legacyFilePresent,
            String sourceFingerprint,
            List<String> mappings,
            List<String> unmappedFields,
            List<String> errors,
            String mode
    ) {
    }

    public record DocumentationResult(boolean successful, Path path, String detail) {
    }

    private record LegacyDocument(Map<String, String> values, List<String> fields) {
    }

    private record LegacyMigrationPlan(
            boolean legacyFilePresent,
            String sourceFingerprint,
            String sourceContent,
            Map<MigrationTarget, String> values,
            List<String> mappings,
            List<String> unmappedFields,
            List<String> errors
    ) {
    }

    private record MigrationTarget(String moduleId, String settingPath) {
        private MigrationTarget {
            moduleId = normalize(moduleId);
            settingPath = normalizePath(settingPath);
        }

        private String display() {
            return moduleId + "." + settingPath;
        }
    }

    private record LegacyMapping(
            String legacyPath,
            MigrationTarget target,
            LegacyConversion conversion
    ) {
        private LegacyMapping {
            legacyPath = Objects.requireNonNull(legacyPath, "legacyPath").toLowerCase(Locale.ROOT);
            Objects.requireNonNull(target, "target");
            Objects.requireNonNull(conversion, "conversion");
        }

        private String convert(String value) {
            return conversion == LegacyConversion.GUI_MODE
                    ? Boolean.parseBoolean(value) ? "auto" : "off"
                    : value;
        }
    }

    private enum LegacyConversion {
        DIRECT,
        GUI_MODE
    }
}
