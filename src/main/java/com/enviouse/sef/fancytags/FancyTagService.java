package com.enviouse.sef.fancytags;

import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.fancytags.api.FancyTagEvents;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.CommandAuditScope;
import com.enviouse.sef.storage.InstantJsonAdapter;
import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class FancyTagService implements StorageRepository {
    public static final int SCHEMA_VERSION = 1;
    public static final int HARD_MAXIMUM_TAGS = 4_096;
    public static final int HARD_MAXIMUM_CATEGORIES = 512;
    public static final int HARD_MAXIMUM_ASSIGNMENTS = 65_536;
    public static final int HARD_MAXIMUM_REVISIONS_PER_TAG = 100;
    public static final int HARD_MAXIMUM_LEASES = 1_024;
    public static final int HARD_MAXIMUM_PALETTES = 512;
    public static final int HARD_MAXIMUM_TEMPLATES = 512;
    public static final int HARD_MAXIMUM_REPORTS = 8_192;

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantJsonAdapter())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    private static final String KEY_PATTERN = "[a-z0-9][a-z0-9_.-]{0,63}";

    private final Settings settings;
    private final FancyTagObjectStore objectStore;
    private final FancyTagTransferService transfers;
    private final Map<UUID, TagRecord> tags = new LinkedHashMap<>();
    private final Map<String, UUID> tagsByKey = new LinkedHashMap<>();
    private final Map<UUID, CategoryRecord> categories = new LinkedHashMap<>();
    private final Map<String, UUID> categoriesByKey = new LinkedHashMap<>();
    private final Map<UUID, PaletteRecord> palettes = new LinkedHashMap<>();
    private final Map<String, UUID> palettesByKey = new LinkedHashMap<>();
    private final Map<UUID, TemplateRecord> templates = new LinkedHashMap<>();
    private final Map<String, UUID> templatesByKey = new LinkedHashMap<>();
    private final Map<UUID, ReportRecord> reports = new LinkedHashMap<>();
    private final Map<UUID, AssignmentRecord> assignments = new LinkedHashMap<>();
    private final Map<UUID, EditLease> leases = new LinkedHashMap<>();
    private final Map<String, FancyTagObjectStore.ImportCandidate> importCandidates = new LinkedHashMap<>();
    private StorageService.Document document;
    private Path path;
    private RepositoryState state = RepositoryState.NEW;
    private long registryRevision;
    private long flushedRevision;

    public FancyTagService(Settings settings) {
        this.settings = Objects.requireNonNull(settings, "settings");
        this.objectStore = new FancyTagObjectStore(settings.objectLimits());
        this.transfers = new FancyTagTransferService(
                32,
                2,
                FancyTagTransferService.HARD_MAXIMUM_CHUNK_BYTES,
                settings.objectLimits().maximumEncodedBytes(),
                Duration.ofSeconds(60));
    }

    @Override
    public String id() {
        return "sef:fancy_tags";
    }

    @Override
    public String domain() {
        return "fancy_tags";
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
                .resolve("fancy-tags.json")
                .toAbsolutePath()
                .normalize();
        tags.clear();
        tagsByKey.clear();
        categories.clear();
        categoriesByKey.clear();
        palettes.clear();
        palettesByKey.clear();
        templates.clear();
        templatesByKey.clear();
        reports.clear();
        assignments.clear();
        leases.clear();
        importCandidates.clear();
        registryRevision = 0L;
        flushedRevision = 0L;
        boolean existed = java.nio.file.Files.exists(
                path,
                java.nio.file.LinkOption.NOFOLLOW_LINKS);
        try {
            objectStore.initialize(managedRoot);
        } catch (IOException | RuntimeException exception) {
            state = RepositoryState.ERROR;
            return new LoadResult(state, "object store initialization failed");
        }
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(null);
        if (document == null) {
            state = existed ? RepositoryState.RECOVERY : RepositoryState.MISSING;
            return new LoadResult(state, existed ? "storage unavailable" : "new repository");
        }
        try {
            Snapshot snapshot = GSON.fromJson(document.data(), Snapshot.class);
            if (snapshot == null
                    || snapshot.tags().size() > settings.maximumTags()
                    || snapshot.categories().size() > settings.maximumCategories()
                    || snapshot.assignments().size() > settings.maximumAssignments()) {
                throw new IllegalStateException("Fancy Tags collections are outside bounds");
            }
            for (CategoryRecord category : snapshot.categories()) {
                validateCategory(category);
                if (categories.putIfAbsent(category.id(), category) != null
                        || categoriesByKey.putIfAbsent(category.resourceKey(), category.id()) != null) {
                    throw new IllegalStateException("duplicate Fancy Tags category");
                }
                registryRevision = Math.max(registryRevision, category.revision());
            }
            for (PaletteRecord palette : snapshot.palettes()) {
                validatePalette(palette);
                if (palettes.putIfAbsent(palette.id(), palette) != null
                        || palettesByKey.putIfAbsent(palette.resourceKey(), palette.id()) != null) {
                    throw new IllegalStateException("duplicate Fancy Tags palette");
                }
                registryRevision = Math.max(registryRevision, palette.revision());
            }
            for (TemplateRecord template : snapshot.templates()) {
                validateTemplate(template);
                if (templates.putIfAbsent(template.id(), template) != null
                        || templatesByKey.putIfAbsent(template.resourceKey(), template.id()) != null) {
                    throw new IllegalStateException("duplicate Fancy Tags template");
                }
                registryRevision = Math.max(registryRevision, template.revision());
            }
            for (TagRecord tag : snapshot.tags()) {
                validateTag(tag);
                if (tags.putIfAbsent(tag.id(), tag) != null
                        || tagsByKey.putIfAbsent(tag.resourceKey(), tag.id()) != null) {
                    throw new IllegalStateException("duplicate Fancy Tags tag");
                }
                registryRevision = Math.max(registryRevision, tag.recordRevision());
            }
            for (AssignmentRecord assignment : snapshot.assignments()) {
                validateAssignment(assignment);
                if (!tags.containsKey(assignment.tagId())
                        || assignments.putIfAbsent(assignment.id(), assignment) != null) {
                    throw new IllegalStateException("invalid Fancy Tags assignment");
                }
                registryRevision = Math.max(registryRevision, assignment.revision());
            }
            for (ReportRecord report : snapshot.reports()) {
                validateReport(report);
                if (!tags.containsKey(report.tagId())
                        || reports.putIfAbsent(report.id(), report) != null) {
                    throw new IllegalStateException("invalid Fancy Tags report");
                }
                registryRevision = Math.max(registryRevision, report.revision());
            }
            state = RepositoryState.READY;
            if (document.migrated()) {
                registryRevision++;
            }
            return new LoadResult(
                    state,
                    "loaded " + tags.size() + " tags and " + assignments.size() + " assignments");
        } catch (RuntimeException exception) {
            tags.clear();
            tagsByKey.clear();
            categories.clear();
            categoriesByKey.clear();
            palettes.clear();
            palettesByKey.clear();
            templates.clear();
            templatesByKey.clear();
            reports.clear();
            assignments.clear();
            state = RepositoryState.RECOVERY;
            return new LoadResult(state, exception.getClass().getSimpleName());
        }
    }

    public synchronized ActionResult<TagRecord> createDraft(
            String resourceKey,
            String displayName,
            UUID actorId
    ) {
        writable();
        String key = normalizeKey(resourceKey);
        if (tagsByKey.containsKey(key)) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "tag resource key already exists");
        }
        if (tags.size() >= settings.maximumTags()) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "tag limit reached");
        }
        Instant now = Instant.now();
        UUID id = UUID.nameUUIDFromBytes(("sef:tag:" + key).getBytes(StandardCharsets.UTF_8));
        if (tags.containsKey(id)) {
            id = UUID.randomUUID();
        }
        TagRecord tag = new TagRecord(
                id,
                key,
                bounded(displayName == null || displayName.isBlank() ? key : displayName, 64),
                "",
                null,
                TagStatus.DRAFT,
                actorId,
                now,
                actorId,
                now,
                0L,
                "",
                EnumSet.allOf(RenderContext.class),
                bounded(displayName == null || displayName.isBlank() ? key : displayName, 64),
                1L,
                List.of());
        tags.put(id, tag);
        tagsByKey.put(key, id);
        changed(tag.recordRevision());
        audit(actorId, "sef:tags.create", tag.id(), AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        event(FancyTagEvents.Type.CREATED, actorId, tag.id(), null, tag.recordRevision());
        return ActionResult.success(tag);
    }

    public synchronized ActionResult<CategoryRecord> createCategory(
            String resourceKey,
            String displayName,
            String icon,
            UUID actorId
    ) {
        writable();
        String key = normalizeKey(resourceKey);
        if (categoriesByKey.containsKey(key)) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "tag category key already exists");
        }
        if (categories.size() >= settings.maximumCategories()) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "tag category limit reached");
        }
        CategoryRecord category = new CategoryRecord(
                UUID.randomUUID(),
                key,
                bounded(displayName == null || displayName.isBlank() ? key : displayName, 64),
                "",
                normalizeResourceLocation(icon == null || icon.isBlank() ? "minecraft:name_tag" : icon),
                categories.size(),
                "",
                actorId,
                Instant.now(),
                1L);
        categories.put(category.id(), category);
        categoriesByKey.put(category.resourceKey(), category.id());
        changed(category.revision());
        audit(actorId, "sef:tags.category.create", category.id(), AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        return ActionResult.success(category);
    }

    public synchronized ActionResult<CategoryRecord> updateCategory(
            String reference,
            String displayName,
            String description,
            String icon,
            int sortOrder,
            String visibilityPermission,
            UUID actorId,
            long expectedRevision
    ) {
        writable();
        CategoryRecord current = findCategory(reference).orElse(null);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag category not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "tag category revision changed");
        }
        CategoryRecord replacement = new CategoryRecord(
                current.id(),
                current.resourceKey(),
                displayName == null ? current.displayName() : displayName,
                description == null ? current.description() : description,
                icon == null ? current.icon() : icon,
                sortOrder < 0 ? current.sortOrder() : sortOrder,
                visibilityPermission == null ? current.visibilityPermission() : visibilityPermission,
                actorId,
                Instant.now(),
                Math.addExact(current.revision(), 1L));
        categories.put(current.id(), replacement);
        changed(replacement.revision());
        audit(actorId, "sef:tags.category.edit", current.id(),
                AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<Void> deleteCategory(
            String reference,
            UUID actorId,
            long expectedRevision
    ) {
        writable();
        CategoryRecord current = findCategory(reference).orElse(null);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag category not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "tag category revision changed");
        }
        if (tags.values().stream().anyMatch(tag -> current.id().equals(tag.categoryId()))) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "tag category is still in use");
        }
        categories.remove(current.id());
        categoriesByKey.remove(current.resourceKey());
        changed(Math.addExact(current.revision(), 1L));
        audit(actorId, "sef:tags.category.delete", current.id(),
                AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        return ActionResult.success(null);
    }

    public synchronized ActionResult<PaletteRecord> createPalette(
            String resourceKey,
            String displayName,
            List<String> colors,
            String visibilityPermission,
            UUID actorId
    ) {
        writable();
        String key = normalizeKey(resourceKey);
        if (palettesByKey.containsKey(key)) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "tag palette key already exists");
        }
        if (palettes.size() >= HARD_MAXIMUM_PALETTES) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "tag palette limit reached");
        }
        PaletteRecord palette = new PaletteRecord(
                UUID.randomUUID(),
                key,
                displayName == null || displayName.isBlank() ? key : displayName,
                colors == null || colors.isEmpty() ? defaultPaletteColors() : colors,
                visibilityPermission,
                actorId,
                Instant.now(),
                1L);
        palettes.put(palette.id(), palette);
        palettesByKey.put(palette.resourceKey(), palette.id());
        changed(palette.revision());
        audit(actorId, "sef:tags.palette.create", palette.id(),
                AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        return ActionResult.success(palette);
    }

    public synchronized ActionResult<PaletteRecord> updatePalette(
            String reference,
            String displayName,
            List<String> colors,
            String visibilityPermission,
            UUID actorId,
            long expectedRevision
    ) {
        writable();
        PaletteRecord current = findPalette(reference).orElse(null);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag palette not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "tag palette revision changed");
        }
        PaletteRecord replacement = new PaletteRecord(
                current.id(),
                current.resourceKey(),
                displayName == null ? current.displayName() : displayName,
                colors == null ? current.colors() : colors,
                visibilityPermission == null ? current.visibilityPermission() : visibilityPermission,
                actorId,
                Instant.now(),
                Math.addExact(current.revision(), 1L));
        palettes.put(current.id(), replacement);
        changed(replacement.revision());
        audit(actorId, "sef:tags.palette.edit", current.id(),
                AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<Void> deletePalette(
            String reference,
            UUID actorId,
            long expectedRevision
    ) {
        writable();
        PaletteRecord current = findPalette(reference).orElse(null);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag palette not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "tag palette revision changed");
        }
        palettes.remove(current.id());
        palettesByKey.remove(current.resourceKey());
        changed(Math.addExact(current.revision(), 1L));
        audit(actorId, "sef:tags.palette.delete", current.id(),
                AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        return ActionResult.success(null);
    }

    public synchronized ActionResult<TemplateRecord> createTemplate(
            String resourceKey,
            String displayName,
            int width,
            int height,
            String fillColor,
            String visibilityPermission,
            UUID actorId
    ) {
        writable();
        String key = normalizeKey(resourceKey);
        if (templatesByKey.containsKey(key)) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "tag template key already exists");
        }
        if (templates.size() >= HARD_MAXIMUM_TEMPLATES) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "tag template limit reached");
        }
        TemplateRecord template = new TemplateRecord(
                UUID.randomUUID(),
                key,
                displayName == null || displayName.isBlank() ? key : displayName,
                width,
                height,
                normalizeColor(fillColor == null || fillColor.isBlank() ? "#00000000" : fillColor),
                visibilityPermission,
                actorId,
                Instant.now(),
                1L);
        templates.put(template.id(), template);
        templatesByKey.put(template.resourceKey(), template.id());
        changed(template.revision());
        audit(actorId, "sef:tags.template.create", template.id(),
                AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        return ActionResult.success(template);
    }

    public synchronized ActionResult<TemplateRecord> updateTemplate(
            String reference,
            String displayName,
            int width,
            int height,
            String fillColor,
            String visibilityPermission,
            UUID actorId,
            long expectedRevision
    ) {
        writable();
        TemplateRecord current = findTemplate(reference).orElse(null);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag template not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "tag template revision changed");
        }
        TemplateRecord replacement = new TemplateRecord(
                current.id(),
                current.resourceKey(),
                displayName == null ? current.displayName() : displayName,
                width < 1 ? current.width() : width,
                height < 1 ? current.height() : height,
                fillColor == null ? current.fillColor() : fillColor,
                visibilityPermission == null ? current.visibilityPermission() : visibilityPermission,
                actorId,
                Instant.now(),
                Math.addExact(current.revision(), 1L));
        templates.put(current.id(), replacement);
        changed(replacement.revision());
        audit(actorId, "sef:tags.template.edit", current.id(),
                AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<Void> deleteTemplate(
            String reference,
            UUID actorId,
            long expectedRevision
    ) {
        writable();
        TemplateRecord current = findTemplate(reference).orElse(null);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag template not found");
        }
        if (current.revision() != expectedRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "tag template revision changed");
        }
        templates.remove(current.id());
        templatesByKey.remove(current.resourceKey());
        changed(Math.addExact(current.revision(), 1L));
        audit(actorId, "sef:tags.template.delete", current.id(),
                AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        return ActionResult.success(null);
    }

    public synchronized ActionResult<ReportRecord> report(
            String tagReference,
            UUID reporterId,
            String reason
    ) {
        writable();
        TagRecord tag = find(tagReference).orElse(null);
        if (tag == null || tag.status() == TagStatus.DRAFT || tag.status() == TagStatus.ARCHIVED) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "visible tag not found");
        }
        String boundedReason = bounded(reason, 256);
        if (boundedReason.length() < 3) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "tag report reason is too short");
        }
        long openByReporter = reports.values().stream()
                .filter(report -> report.reporterId().equals(reporterId) && report.status() == ReportStatus.OPEN)
                .count();
        if (openByReporter >= 10 || reports.size() >= HARD_MAXIMUM_REPORTS) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "tag report limit reached");
        }
        boolean duplicate = reports.values().stream().anyMatch(report ->
                report.reporterId().equals(reporterId)
                        && report.tagId().equals(tag.id())
                        && report.status() == ReportStatus.OPEN);
        if (duplicate) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "tag is already reported");
        }
        ReportRecord report = new ReportRecord(
                UUID.randomUUID(),
                tag.id(),
                reporterId,
                boundedReason,
                ReportStatus.OPEN,
                Instant.now(),
                null,
                null,
                "",
                1L);
        reports.put(report.id(), report);
        changed(report.revision());
        audit(reporterId, "sef:tags.report", tag.id(),
                AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        return ActionResult.success(report);
    }

    public synchronized ActionResult<ReportRecord> clearReport(
            UUID reportId,
            UUID actorId,
            String resolution,
            long expectedRevision
    ) {
        writable();
        ReportRecord current = reports.get(reportId);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag report not found");
        }
        if (current.revision() != expectedRevision || current.status() != ReportStatus.OPEN) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "tag report revision changed");
        }
        ReportRecord replacement = new ReportRecord(
                current.id(),
                current.tagId(),
                current.reporterId(),
                current.reason(),
                ReportStatus.CLEARED,
                current.createdAt(),
                actorId,
                Instant.now(),
                resolution,
                Math.addExact(current.revision(), 1L));
        reports.put(current.id(), replacement);
        changed(replacement.revision());
        audit(actorId, "sef:tags.moderation.clear", current.tagId(),
                AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<TagRecord> clearModeration(
            String tagReference,
            UUID actorId,
            String resolution,
            long expectedTagRevision
    ) {
        writable();
        TagRecord current = find(tagReference).orElse(null);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag not found");
        }
        if (current.recordRevision() != expectedTagRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "tag revision changed");
        }
        long next = Math.addExact(current.recordRevision(), 1L);
        TagRecord replacement = copyTag(
                current,
                current.status() == TagStatus.SUSPENDED ? TagStatus.PUBLISHED : current.status(),
                actorId,
                Instant.now(),
                current.currentRevision(),
                next,
                current.revisions());
        tags.put(current.id(), replacement);
        Instant now = Instant.now();
        for (Map.Entry<UUID, ReportRecord> entry : new ArrayList<>(reports.entrySet())) {
            ReportRecord report = entry.getValue();
            if (report.tagId().equals(current.id()) && report.status() == ReportStatus.OPEN) {
                reports.put(entry.getKey(), new ReportRecord(
                        report.id(),
                        report.tagId(),
                        report.reporterId(),
                        report.reason(),
                        ReportStatus.CLEARED,
                        report.createdAt(),
                        actorId,
                        now,
                        resolution,
                        Math.addExact(report.revision(), 1L)));
            }
        }
        changed(next);
        audit(actorId, "sef:tags.moderation.clear", current.id(),
                AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        event(FancyTagEvents.Type.STATUS_CHANGED, actorId, current.id(), null, next);
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<TagRecord> updateMetadata(
            String tagReference,
            String displayName,
            String description,
            String categoryReference,
            String visibilityPermission,
            Set<RenderContext> renderContexts,
            String alternativeText,
            UUID actorId,
            long expectedRecordRevision
    ) {
        writable();
        TagRecord current = find(tagReference).orElse(null);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag not found");
        }
        if (current.recordRevision() != expectedRecordRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "tag revision changed");
        }
        UUID categoryId = null;
        if (categoryReference != null && !categoryReference.isBlank()) {
            String normalized = categoryReference.trim().toLowerCase(Locale.ROOT);
            try {
                categoryId = UUID.fromString(normalized);
            } catch (IllegalArgumentException ignored) {
                categoryId = categoriesByKey.get(normalized);
            }
            if (categoryId == null || !categories.containsKey(categoryId)) {
                return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag category not found");
            }
        }
        Set<RenderContext> contexts = renderContexts == null
                ? current.renderContexts()
                : Set.copyOf(renderContexts);
        if (contexts.isEmpty()) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "tag needs a render context");
        }
        long next = Math.addExact(current.recordRevision(), 1L);
        TagRecord replacement = new TagRecord(
                current.id(),
                current.resourceKey(),
                displayName == null ? current.displayName() : displayName,
                description == null ? current.description() : description,
                categoryId,
                current.status(),
                current.createdBy(),
                current.createdAt(),
                actorId,
                Instant.now(),
                current.currentRevision(),
                visibilityPermission == null ? current.visibilityPermission() : visibilityPermission,
                contexts,
                alternativeText == null ? current.alternativeText() : alternativeText,
                next,
                current.revisions());
        tags.put(current.id(), replacement);
        changed(next);
        audit(actorId, "sef:tags.edit", current.id(), AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        event(FancyTagEvents.Type.UPDATED, actorId, current.id(), null, next);
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<TagRecord> duplicate(
            String sourceReference,
            String newResourceKey,
            String newDisplayName,
            UUID actorId
    ) {
        writable();
        TagRecord source = find(sourceReference).orElse(null);
        if (source == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "source tag not found");
        }
        ActionResult<TagRecord> created = createDraft(newResourceKey, newDisplayName, actorId);
        if (!created.successful()) {
            return created;
        }
        TagRecord draft = created.value();
        List<ArtworkRevision> copied = new ArrayList<>();
        long parent = 0L;
        for (ArtworkRevision revision : source.revisions()) {
            long nextRevision = copied.size() + 1L;
            copied.add(new ArtworkRevision(
                    nextRevision,
                    revision.contentHash(),
                    revision.canonicalFormat(),
                    revision.width(),
                    revision.height(),
                    revision.pixels(),
                    revision.encodedBytes(),
                    revision.decodedBytes(),
                    actorId,
                    Instant.now(),
                    parent));
            parent = nextRevision;
        }
        long next = Math.addExact(draft.recordRevision(), 1L);
        TagRecord replacement = new TagRecord(
                draft.id(),
                draft.resourceKey(),
                draft.displayName(),
                source.description(),
                source.categoryId(),
                TagStatus.DRAFT,
                draft.createdBy(),
                draft.createdAt(),
                actorId,
                Instant.now(),
                copied.isEmpty() ? 0L : copied.getLast().revision(),
                source.visibilityPermission(),
                source.renderContexts(),
                source.alternativeText(),
                next,
                copied);
        tags.put(draft.id(), replacement);
        changed(next);
        audit(actorId, "sef:tags.duplicate", draft.id(), AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        event(FancyTagEvents.Type.CREATED, actorId, draft.id(), null, next);
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<ArtworkRevision> importArtwork(
            String tagReference,
            byte[] encoded,
            UUID actorId,
            long expectedRecordRevision
    ) {
        writable();
        TagRecord current = find(tagReference).orElse(null);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag not found");
        }
        if (current.recordRevision() != expectedRecordRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "tag revision changed");
        }
        if (current.status() != TagStatus.DRAFT && current.status() != TagStatus.HIDDEN) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "tag must be a draft or hidden");
        }
        if (current.revisions().size() >= settings.maximumRevisionsPerTag()) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "tag revision limit reached");
        }
        ActionResult<FancyTagObjectStore.StoredObject> stored = objectStore.canonicalizeAndStore(encoded);
        if (!stored.successful()) {
            audit(actorId, "sef:tags.import", current.id(), AuditService.Result.REJECTED, stored.reason());
            return ActionResult.failure(stored.reason(), stored.detail());
        }
        FancyTagObjectStore.StoredObject object = stored.value();
        long nextArtworkRevision = current.revisions().isEmpty()
                ? 1L
                : Math.addExact(current.revisions().getLast().revision(), 1L);
        ArtworkRevision artwork = new ArtworkRevision(
                nextArtworkRevision,
                object.hash(),
                object.format(),
                object.width(),
                object.height(),
                object.pixels(),
                object.encodedBytes(),
                Math.multiplyExact(object.pixels(), 4),
                actorId,
                Instant.now(),
                current.revisions().isEmpty() ? 0L : current.revisions().getLast().revision());
        List<ArtworkRevision> revisions = new ArrayList<>(current.revisions());
        revisions.add(artwork);
        long next = Math.addExact(current.recordRevision(), 1L);
        TagRecord replacement = copyTag(
                current,
                TagStatus.DRAFT,
                actorId,
                Instant.now(),
                artwork.revision(),
                next,
                revisions);
        tags.put(current.id(), replacement);
        changed(next);
        audit(actorId, "sef:tags.import", current.id(), AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        event(FancyTagEvents.Type.ARTWORK_IMPORTED, actorId, current.id(), null, next);
        return ActionResult.success(artwork);
    }

    public synchronized ActionResult<TagRecord> changeStatus(
            String tagReference,
            TagStatus requested,
            UUID actorId,
            long expectedRecordRevision
    ) {
        writable();
        TagRecord current = find(tagReference).orElse(null);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag not found");
        }
        if (current.recordRevision() != expectedRecordRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "tag revision changed");
        }
        if (!allowedTransition(current.status(), requested)) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "tag status transition is not allowed");
        }
        if (requested == TagStatus.PUBLISHED && current.currentRevision() < 1L) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, "tag has no validated artwork");
        }
        if (requested == TagStatus.PENDING_DELETE
                && assignments.values().stream().anyMatch(value -> value.tagId().equals(current.id()) && value.enabled())) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "tag still has active assignments");
        }
        long next = Math.addExact(current.recordRevision(), 1L);
        TagRecord replacement = copyTag(
                current,
                requested,
                actorId,
                Instant.now(),
                current.currentRevision(),
                next,
                current.revisions());
        tags.put(current.id(), replacement);
        changed(next);
        audit(actorId, "sef:tags.status." + requested.name().toLowerCase(Locale.ROOT),
                current.id(), AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        event(FancyTagEvents.Type.STATUS_CHANGED, actorId, current.id(), null, next);
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<ArtworkRevision> restoreRevision(
            String tagReference,
            long artworkRevision,
            UUID actorId,
            long expectedRecordRevision
    ) {
        writable();
        TagRecord current = find(tagReference).orElse(null);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag not found");
        }
        if (current.recordRevision() != expectedRecordRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "tag revision changed");
        }
        ArtworkRevision source = artwork(current, artworkRevision).orElse(null);
        if (source == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "artwork revision not found");
        }
        if (current.revisions().size() >= settings.maximumRevisionsPerTag()) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "tag revision limit reached");
        }
        long nextArtwork = current.revisions().isEmpty()
                ? 1L
                : Math.addExact(current.revisions().getLast().revision(), 1L);
        ArtworkRevision restored = new ArtworkRevision(
                nextArtwork,
                source.contentHash(),
                source.canonicalFormat(),
                source.width(),
                source.height(),
                source.pixels(),
                source.encodedBytes(),
                source.decodedBytes(),
                actorId,
                Instant.now(),
                current.currentRevision());
        List<ArtworkRevision> revisions = new ArrayList<>(current.revisions());
        revisions.add(restored);
        long nextRecord = Math.addExact(current.recordRevision(), 1L);
        TagRecord replacement = copyTag(
                current,
                TagStatus.DRAFT,
                actorId,
                Instant.now(),
                restored.revision(),
                nextRecord,
                revisions);
        tags.put(current.id(), replacement);
        changed(nextRecord);
        audit(actorId, "sef:tags.revision.restore", current.id(),
                AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        event(FancyTagEvents.Type.ARTWORK_IMPORTED, actorId, current.id(), null, nextRecord);
        return ActionResult.success(restored);
    }

    public synchronized ActionResult<Path> exportArtwork(
            String tagReference,
            long artworkRevision,
            String requestedName,
            UUID actorId
    ) {
        TagRecord tag = find(tagReference).orElse(null);
        if (tag == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag not found");
        }
        ArtworkRevision artwork = artworkRevision < 1L
                ? artwork(tag, tag.currentRevision()).orElse(null)
                : artwork(tag, artworkRevision).orElse(null);
        if (artwork == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag artwork revision not found");
        }
        ActionResult<Path> result = objectStore.exportObject(artwork.contentHash(), requestedName);
        audit(actorId, "sef:tags.export", tag.id(),
                result.successful() ? AuditService.Result.SUCCESS : AuditService.Result.FAILED,
                result.reason());
        return result;
    }

    public synchronized ActionResult<Path> exportManifest(
            String tagReference,
            long artworkRevision,
            String requestedName,
            UUID actorId
    ) {
        TagRecord tag = find(tagReference).orElse(null);
        if (tag == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag not found");
        }
        ArtworkRevision artwork = artworkRevision < 1L
                ? artwork(tag, tag.currentRevision()).orElse(null)
                : artwork(tag, artworkRevision).orElse(null);
        if (artwork == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag artwork revision not found");
        }
        ExportManifest manifest = new ExportManifest(
                SCHEMA_VERSION,
                tag.id(),
                tag.resourceKey(),
                tag.displayName(),
                tag.description(),
                tag.categoryId(),
                tag.status(),
                tag.visibilityPermission(),
                tag.renderContexts(),
                tag.alternativeText(),
                artwork.revision(),
                artwork.contentHash(),
                artwork.canonicalFormat(),
                artwork.width(),
                artwork.height(),
                artwork.pixels(),
                artwork.encodedBytes());
        ActionResult<Path> result = objectStore.exportArtifact(
                GSON.toJson(manifest).getBytes(StandardCharsets.UTF_8),
                requestedName,
                "json");
        audit(actorId, "sef:tags.export.manifest", tag.id(),
                result.successful() ? AuditService.Result.SUCCESS : AuditService.Result.FAILED,
                result.reason());
        return result;
    }

    public synchronized ActionResult<Path> exportProject(
            String tagReference,
            long artworkRevision,
            String requestedName,
            UUID actorId
    ) {
        TagRecord tag = find(tagReference).orElse(null);
        if (tag == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag not found");
        }
        ArtworkRevision artwork = artworkRevision < 1L
                ? artwork(tag, tag.currentRevision()).orElse(null)
                : artwork(tag, artworkRevision).orElse(null);
        if (artwork == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag artwork revision not found");
        }
        try {
            byte[] image = objectStore.read(artwork.contentHash());
            FancyTagProjectArchive.ProjectManifest manifest =
                    new FancyTagProjectArchive.ProjectManifest(
                            FancyTagProjectArchive.SCHEMA_VERSION,
                            artwork.width(),
                            artwork.height(),
                            settings.maximumRevisionsPerTag(),
                            List.of(new FancyTagProjectArchive.Layer(
                                    "base",
                                    "layers/base.png",
                                    true,
                                    255)),
                            List.of());
            byte[] archive = createProjectArchive(image, manifest);
            ActionResult<FancyTagProjectArchive.ArchiveFacts> validation =
                    FancyTagProjectArchive.validate(archive);
            if (!validation.successful()) {
                audit(actorId, "sef:tags.export.project", tag.id(),
                        AuditService.Result.FAILED, validation.reason());
                return ActionResult.failure(validation.reason(), validation.detail());
            }
            ActionResult<Path> result = objectStore.exportArtifact(
                    archive,
                    requestedName,
                    "seftagproject");
            audit(actorId, "sef:tags.export.project", tag.id(),
                    result.successful() ? AuditService.Result.SUCCESS : AuditService.Result.FAILED,
                    result.reason());
            return result;
        } catch (IOException exception) {
            audit(actorId, "sef:tags.export.project", tag.id(),
                    AuditService.Result.FAILED, ActionResult.ReasonCode.STORAGE_ERROR);
            return ActionResult.failure(ActionResult.ReasonCode.STORAGE_ERROR, "tag project export failed");
        }
    }

    private static byte[] createProjectArchive(
            byte[] image,
            FancyTagProjectArchive.ProjectManifest manifest
    ) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream output = new ZipOutputStream(bytes, StandardCharsets.UTF_8)) {
            writeArchiveEntry(output, "manifest.json",
                    GSON.toJson(manifest).getBytes(StandardCharsets.UTF_8));
            writeArchiveEntry(output, "flattened-preview.png", image);
            writeArchiveEntry(output, "layers/base.png", image);
            writeArchiveEntry(output, "palettes.json", "[]".getBytes(StandardCharsets.UTF_8));
            writeArchiveEntry(output, "editor.json", "{}".getBytes(StandardCharsets.UTF_8));
        }
        return bytes.toByteArray();
    }

    private static void writeArchiveEntry(ZipOutputStream output, String name, byte[] bytes)
            throws IOException {
        output.putNextEntry(new ZipEntry(name));
        output.write(bytes);
        output.closeEntry();
    }

    public synchronized ActionResult<Void> deletePending(String tagReference, UUID actorId, long expectedRecordRevision) {
        writable();
        TagRecord current = find(tagReference).orElse(null);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag not found");
        }
        if (current.recordRevision() != expectedRecordRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "tag revision changed");
        }
        if (current.status() != TagStatus.PENDING_DELETE) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "tag is not pending deletion");
        }
        if (assignments.values().stream().anyMatch(value -> value.tagId().equals(current.id()))) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "tag still has assignments");
        }
        tags.remove(current.id());
        tagsByKey.remove(current.resourceKey());
        leases.remove(current.id());
        changed(Math.addExact(current.recordRevision(), 1L));
        audit(actorId, "sef:tags.delete", current.id(), AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        event(FancyTagEvents.Type.DELETED, actorId, current.id(), null, registryRevision());
        return ActionResult.success(null);
    }

    public synchronized ActionResult<AssignmentRecord> assign(
            String tagReference,
            TargetType targetType,
            String targetId,
            TagSlot slot,
            int priority,
            Instant expiresAt,
            UUID actorId
    ) {
        writable();
        TagRecord tag = find(tagReference).orElse(null);
        if (tag == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag not found");
        }
        if (tag.status() != TagStatus.PUBLISHED && tag.status() != TagStatus.HIDDEN) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "only published or hidden tags may be assigned");
        }
        if (assignments.size() >= settings.maximumAssignments()) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "tag assignment limit reached");
        }
        String normalizedTarget = normalizeTarget(targetType, targetId);
        long perTarget = assignments.values().stream()
                .filter(value -> value.targetType() == targetType
                        && value.targetId().equals(normalizedTarget)
                        && value.enabled())
                .count();
        if (perTarget >= settings.maximumAssignmentsPerTarget()) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "target tag assignment limit reached");
        }
        AssignmentRecord assignment = new AssignmentRecord(
                UUID.randomUUID(),
                tag.id(),
                tag.currentRevision(),
                targetType,
                normalizedTarget,
                Objects.requireNonNull(slot, "slot"),
                Math.clamp(priority, -10_000, 10_000),
                Instant.now(),
                expiresAt,
                true,
                actorId,
                Instant.now(),
                1L,
                "");
        assignments.put(assignment.id(), assignment);
        changed(assignment.revision());
        audit(actorId, "sef:tags.assign", tag.id(), AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        event(FancyTagEvents.Type.ASSIGNED, actorId, tag.id(), assignment.id(), assignment.revision());
        return ActionResult.success(assignment);
    }

    public synchronized ActionResult<Void> unassign(UUID assignmentId, UUID actorId) {
        writable();
        AssignmentRecord removed = assignments.remove(Objects.requireNonNull(assignmentId, "assignmentId"));
        if (removed == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag assignment not found");
        }
        changed(Math.addExact(removed.revision(), 1L));
        audit(actorId, "sef:tags.unassign", removed.tagId(), AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        event(FancyTagEvents.Type.UNASSIGNED, actorId, removed.tagId(), removed.id(), registryRevision());
        return ActionResult.success(null);
    }

    public synchronized List<ResolvedTag> resolve(
            ViewerContext context,
            RenderContext renderContext,
            Predicate<String> permissionCheck
    ) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(renderContext, "renderContext");
        Objects.requireNonNull(permissionCheck, "permissionCheck");
        if (!settings.enabled() || context.subjectVanishedFromViewer()) {
            return List.of();
        }
        Instant now = Instant.now();
        List<ResolvedTag> candidates = new ArrayList<>();
        for (AssignmentRecord assignment : assignments.values()) {
            if (!assignment.enabled()
                    || assignment.startsAt().isAfter(now)
                    || assignment.expiresAt() != null && !assignment.expiresAt().isAfter(now)
                    || !targetMatches(assignment, context)) {
                continue;
            }
            TagRecord tag = tags.get(assignment.tagId());
            if (tag == null
                    || tag.status() != TagStatus.PUBLISHED && tag.status() != TagStatus.HIDDEN
                    || !tag.renderContexts().contains(renderContext)
                    || !tag.visibilityPermission().isBlank()
                    && !permissionCheck.test(tag.visibilityPermission())) {
                continue;
            }
            ArtworkRevision artwork = artwork(tag, assignment.requiredArtworkRevision()).orElse(null);
            if (artwork == null) {
                continue;
            }
            candidates.add(new ResolvedTag(
                    tag.id(),
                    tag.recordRevision(),
                    tag.resourceKey(),
                    tag.displayName(),
                    tag.alternativeText(),
                    assignment.id(),
                    assignment.slot(),
                    assignment.priority(),
                    assignment.targetType(),
                    artwork));
        }
        candidates.sort(Comparator
                .comparingInt(ResolvedTag::priority).reversed()
                .thenComparingInt(value -> targetRank(value.targetType()))
                .thenComparing(value -> value.assignmentId().toString()));
        Map<TagSlot, Integer> counts = new LinkedHashMap<>();
        List<ResolvedTag> result = new ArrayList<>();
        int totalWidth = 0;
        for (ResolvedTag candidate : candidates) {
            int count = counts.getOrDefault(candidate.slot(), 0);
            if (count >= settings.maximumTagsPerSlot()
                    || result.size() >= settings.maximumResolvedTags()
                    || totalWidth + candidate.artwork().width() > settings.maximumTotalRenderWidth()) {
                continue;
            }
            counts.put(candidate.slot(), count + 1);
            totalWidth += candidate.artwork().width();
            result.add(candidate);
        }
        return List.copyOf(result);
    }

    public synchronized ActionResult<EditLease> acquireLease(
            String tagReference,
            UUID holder,
            long expectedTagRevision,
            boolean override
    ) {
        writable();
        pruneRuntimeState(Instant.now());
        TagRecord tag = find(tagReference).orElse(null);
        if (tag == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag not found");
        }
        if (tag.recordRevision() != expectedTagRevision) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "tag revision changed");
        }
        EditLease current = leases.get(tag.id());
        if (current != null && !current.holder().equals(holder) && !override) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "tag is leased by another editor");
        }
        if (current == null && leases.size() >= settings.maximumLeases()) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "tag edit lease limit reached");
        }
        Instant now = Instant.now();
        EditLease lease = new EditLease(
                UUID.randomUUID(),
                tag.id(),
                holder,
                expectedTagRevision,
                now,
                now.plus(settings.leaseDuration()),
                now.plus(settings.minimumLeaseRenewal()),
                current == null ? 1L : Math.addExact(current.revision(), 1L));
        leases.put(tag.id(), lease);
        audit(holder, override ? "sef:tags.lease.override" : "sef:tags.lease.acquire",
                tag.id(), AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        return ActionResult.success(lease);
    }

    public synchronized ActionResult<ArtworkRevision> completeUpload(
            FancyTagTransferService.CompletedUpload upload,
            UUID actorId
    ) {
        writable();
        Objects.requireNonNull(upload, "upload");
        if (!upload.ownerId().equals(actorId)) {
            return ActionResult.failure(ActionResult.ReasonCode.PERMISSION_DENIED, "upload owner changed");
        }
        EditLease lease = leases.get(upload.tagId());
        if (lease == null
                || !lease.leaseId().equals(upload.leaseId())
                || !lease.holder().equals(upload.ownerId())
                || lease.expectedTagRevision() != upload.expectedTagRevision()
                || !lease.expiresAt().isAfter(Instant.now())) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "tag edit lease is stale");
        }
        TagRecord tag = tags.get(upload.tagId());
        if (tag == null || tag.recordRevision() != upload.expectedTagRevision()) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "tag revision changed");
        }
        ActionResult<ArtworkRevision> result =
                importArtwork(tag.id().toString(), upload.bytes(), actorId, upload.expectedTagRevision());
        if (result.successful()) {
            leases.remove(tag.id());
        }
        return result;
    }

    public synchronized ActionResult<EditLease> renewLease(UUID leaseId, UUID holder) {
        writable();
        Instant now = Instant.now();
        pruneRuntimeState(now);
        EditLease current = leases.values().stream()
                .filter(value -> value.leaseId().equals(leaseId) && value.holder().equals(holder))
                .findFirst()
                .orElse(null);
        if (current == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag edit lease not found");
        }
        if (current.renewalDeadline().isAfter(now)) {
            return ActionResult.failure(ActionResult.ReasonCode.COOLDOWN_ACTIVE, "tag edit lease renewal is too early");
        }
        EditLease replacement = new EditLease(
                current.leaseId(),
                current.tagId(),
                current.holder(),
                current.expectedTagRevision(),
                current.acquiredAt(),
                now.plus(settings.leaseDuration()),
                now.plus(settings.minimumLeaseRenewal()),
                Math.addExact(current.revision(), 1L));
        leases.put(current.tagId(), replacement);
        return ActionResult.success(replacement);
    }

    public synchronized ActionResult<Void> releaseLease(UUID leaseId, UUID holder, boolean override) {
        writable();
        EditLease lease = leases.values().stream()
                .filter(value -> value.leaseId().equals(leaseId))
                .findFirst()
                .orElse(null);
        if (lease == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag edit lease not found");
        }
        if (!lease.holder().equals(holder) && !override) {
            return ActionResult.failure(ActionResult.ReasonCode.PERMISSION_DENIED, "tag edit lease belongs to another editor");
        }
        leases.remove(lease.tagId());
        audit(holder, override ? "sef:tags.lease.override_release" : "sef:tags.lease.release",
                lease.tagId(), AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        return ActionResult.success(null);
    }

    public synchronized List<FancyTagObjectStore.ImportCandidate> scanImportInbox() {
        writable();
        List<FancyTagObjectStore.ImportCandidate> scanned =
                objectStore.scanImports(Instant.now(), settings.importSettleInterval());
        importCandidates.clear();
        for (FancyTagObjectStore.ImportCandidate candidate : scanned) {
            importCandidates.put(candidate.candidateId(), candidate);
        }
        return List.copyOf(importCandidates.values());
    }

    public synchronized Optional<FancyTagObjectStore.ImportCandidate> importCandidate(String candidateId) {
        return Optional.ofNullable(importCandidates.get(candidateId));
    }

    public synchronized List<FancyTagObjectStore.ImportCandidate> importCandidates() {
        return List.copyOf(importCandidates.values());
    }

    public synchronized ActionResult<ArtworkRevision> approveImport(
            String candidateId,
            String tagReference,
            UUID actorId,
            long expectedRecordRevision
    ) {
        writable();
        FancyTagObjectStore.ImportCandidate candidate = importCandidates.get(candidateId);
        if (candidate == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "import candidate not found");
        }
        ActionResult<byte[]> bytes = objectStore.readImportCandidate(
                candidate,
                Instant.now(),
                settings.importSettleInterval());
        if (!bytes.successful()) {
            return ActionResult.failure(bytes.reason(), bytes.detail());
        }
        ActionResult<ArtworkRevision> result =
                importArtwork(tagReference, bytes.value(), actorId, expectedRecordRevision);
        if (result.successful()) {
            importCandidates.remove(candidateId);
        }
        return result;
    }

    public synchronized ActionResult<Void> rejectImport(String candidateId, UUID actorId) {
        writable();
        FancyTagObjectStore.ImportCandidate candidate = importCandidates.get(candidateId);
        if (candidate == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "import candidate not found");
        }
        ActionResult<Void> result = objectStore.rejectImportCandidate(candidate);
        if (result.successful()) {
            importCandidates.remove(candidateId);
            audit(actorId, "sef:tags.import.reject", null, AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
        }
        return result;
    }

    public synchronized FancyTagObjectStore.IntegrityReport integrity() {
        Set<String> references = referencedHashes();
        FancyTagObjectStore.IntegrityReport report = objectStore.inspect(references);
        if (!report.missing().isEmpty() || !report.corrupt().isEmpty()) {
            for (Map.Entry<UUID, TagRecord> entry : new ArrayList<>(tags.entrySet())) {
                boolean invalid = entry.getValue().revisions().stream()
                        .map(ArtworkRevision::contentHash)
                        .anyMatch(hash -> report.missing().contains(hash) || report.corrupt().contains(hash));
                if (invalid && entry.getValue().status() != TagStatus.CORRUPT) {
                    TagRecord current = entry.getValue();
                    long next = Math.addExact(current.recordRevision(), 1L);
                    tags.put(entry.getKey(), copyTag(
                            current,
                            TagStatus.CORRUPT,
                            current.modifiedBy(),
                            Instant.now(),
                            current.currentRevision(),
                            next,
                            current.revisions()));
                    changed(next);
                }
            }
        }
        return report;
    }

    public synchronized FancyTagObjectStore.GarbageCollectionResult garbageCollect(boolean execute) {
        writable();
        return objectStore.collect(referencedHashes(), execute);
    }

    public synchronized ActionResult<Path> createBackupManifest(UUID actorId) {
        writable();
        try {
            Snapshot snapshot = snapshot();
            Path backup = objectStore.createBackupManifest(GSON.toJson(snapshot));
            audit(actorId, "sef:tags.backup", null, AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
            return ActionResult.success(backup);
        } catch (IOException exception) {
            audit(actorId, "sef:tags.backup", null, AuditService.Result.FAILED, ActionResult.ReasonCode.STORAGE_ERROR);
            return ActionResult.failure(ActionResult.ReasonCode.STORAGE_ERROR, "tag backup manifest creation failed");
        }
    }

    public synchronized ActionResult<Path> createBackup(UUID actorId) {
        writable();
        BackupEnvelope envelope = new BackupEnvelope(SCHEMA_VERSION, Instant.now(), snapshot());
        ActionResult<Path> result = objectStore.createBackup(
                GSON.toJson(envelope),
                referencedHashes());
        audit(
                actorId,
                "sef:tags.backup",
                null,
                result.successful() ? AuditService.Result.SUCCESS : AuditService.Result.FAILED,
                result.reason());
        return result;
    }

    public synchronized List<String> backups() {
        return objectStore.backups();
    }

    public synchronized ActionResult<Void> restoreBackup(String backupName, UUID actorId) {
        writable();
        ActionResult<FancyTagObjectStore.StagedRestore> stagedResult =
                objectStore.stageRestore(backupName);
        if (!stagedResult.successful()) {
            audit(actorId, "sef:tags.restore", null, AuditService.Result.FAILED, stagedResult.reason());
            return ActionResult.failure(stagedResult.reason(), stagedResult.detail());
        }
        FancyTagObjectStore.StagedRestore staged = stagedResult.value();
        try {
            BackupEnvelope envelope = GSON.fromJson(staged.snapshotJson(), BackupEnvelope.class);
            if (envelope == null
                    || envelope.schemaVersion() != SCHEMA_VERSION
                    || envelope.snapshot() == null) {
                return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, "tag backup schema is unsupported");
            }
            PreparedSnapshot prepared = prepareSnapshot(envelope.snapshot());
            if (!prepared.referencedHashes().equals(staged.hashes())) {
                return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, "tag backup object set does not match its manifest");
            }
            ActionResult<Void> committed = objectStore.commitRestore(staged);
            if (!committed.successful()) {
                return committed;
            }
            Map<UUID, TagRecord> previousTags = new LinkedHashMap<>(tags);
            Map<String, UUID> previousTagsByKey = new LinkedHashMap<>(tagsByKey);
            Map<UUID, CategoryRecord> previousCategories = new LinkedHashMap<>(categories);
            Map<String, UUID> previousCategoriesByKey = new LinkedHashMap<>(categoriesByKey);
            Map<UUID, PaletteRecord> previousPalettes = new LinkedHashMap<>(palettes);
            Map<String, UUID> previousPalettesByKey = new LinkedHashMap<>(palettesByKey);
            Map<UUID, TemplateRecord> previousTemplates = new LinkedHashMap<>(templates);
            Map<String, UUID> previousTemplatesByKey = new LinkedHashMap<>(templatesByKey);
            Map<UUID, AssignmentRecord> previousAssignments = new LinkedHashMap<>(assignments);
            Map<UUID, ReportRecord> previousReports = new LinkedHashMap<>(reports);
            long previousRegistryRevision = registryRevision;
            long previousFlushedRevision = flushedRevision;
            tags.clear();
            tags.putAll(prepared.tags());
            tagsByKey.clear();
            tagsByKey.putAll(prepared.tagsByKey());
            categories.clear();
            categories.putAll(prepared.categories());
            categoriesByKey.clear();
            categoriesByKey.putAll(prepared.categoriesByKey());
            palettes.clear();
            palettes.putAll(prepared.palettes());
            palettesByKey.clear();
            palettesByKey.putAll(prepared.palettesByKey());
            templates.clear();
            templates.putAll(prepared.templates());
            templatesByKey.clear();
            templatesByKey.putAll(prepared.templatesByKey());
            assignments.clear();
            assignments.putAll(prepared.assignments());
            reports.clear();
            reports.putAll(prepared.reports());
            leases.clear();
            transfers.clear();
            registryRevision = Math.max(
                    Math.addExact(previousRegistryRevision, 1L),
                    Math.addExact(prepared.maximumRevision(), 1L));
            try {
                flush();
            } catch (IOException exception) {
                tags.clear();
                tags.putAll(previousTags);
                tagsByKey.clear();
                tagsByKey.putAll(previousTagsByKey);
                categories.clear();
                categories.putAll(previousCategories);
                categoriesByKey.clear();
                categoriesByKey.putAll(previousCategoriesByKey);
                palettes.clear();
                palettes.putAll(previousPalettes);
                palettesByKey.clear();
                palettesByKey.putAll(previousPalettesByKey);
                templates.clear();
                templates.putAll(previousTemplates);
                templatesByKey.clear();
                templatesByKey.putAll(previousTemplatesByKey);
                assignments.clear();
                assignments.putAll(previousAssignments);
                reports.clear();
                reports.putAll(previousReports);
                registryRevision = previousRegistryRevision;
                flushedRevision = previousFlushedRevision;
                audit(actorId, "sef:tags.restore", null, AuditService.Result.FAILED, ActionResult.ReasonCode.STORAGE_ERROR);
                return ActionResult.failure(ActionResult.ReasonCode.STORAGE_ERROR, "tag restore could not publish its registry");
            }
            audit(actorId, "sef:tags.restore", null, AuditService.Result.SUCCESS, ActionResult.ReasonCode.SUCCESS);
            event(FancyTagEvents.Type.CACHE_INVALIDATED, actorId, null, null, registryRevision);
            return ActionResult.success(null);
        } catch (RuntimeException exception) {
            audit(actorId, "sef:tags.restore", null, AuditService.Result.FAILED, ActionResult.ReasonCode.INVALID_DEFINITION);
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, "tag backup registry validation failed");
        } finally {
            objectStore.discardRestore(staged);
        }
    }

    public synchronized Optional<TagRecord> find(String reference) {
        if (reference == null || reference.isBlank()) {
            return Optional.empty();
        }
        String normalized = reference.trim().toLowerCase(Locale.ROOT);
        try {
            TagRecord byId = tags.get(UUID.fromString(normalized));
            if (byId != null) {
                return Optional.of(byId);
            }
        } catch (IllegalArgumentException ignored) {
        }
        UUID id = tagsByKey.get(normalized);
        return Optional.ofNullable(id == null ? null : tags.get(id));
    }

    public synchronized Optional<CategoryRecord> findCategory(String reference) {
        return findRecord(reference, categories, categoriesByKey);
    }

    public synchronized Optional<PaletteRecord> findPalette(String reference) {
        return findRecord(reference, palettes, palettesByKey);
    }

    public synchronized Optional<TemplateRecord> findTemplate(String reference) {
        return findRecord(reference, templates, templatesByKey);
    }

    public synchronized Optional<ArtworkRevision> currentArtwork(String tagReference) {
        return find(tagReference).flatMap(tag -> artwork(tag, tag.currentRevision()));
    }

    public synchronized byte[] readArtwork(String hash) throws IOException {
        return objectStore.read(hash);
    }

    public synchronized List<TagRecord> tags() {
        return tags.values().stream()
                .sorted(Comparator.comparing(TagRecord::resourceKey))
                .toList();
    }

    public synchronized List<CategoryRecord> categories() {
        return categories.values().stream()
                .sorted(Comparator.comparingInt(CategoryRecord::sortOrder)
                        .thenComparing(CategoryRecord::resourceKey))
                .toList();
    }

    public synchronized List<PaletteRecord> palettes() {
        return palettes.values().stream()
                .sorted(Comparator.comparing(PaletteRecord::resourceKey))
                .toList();
    }

    public synchronized List<TemplateRecord> templates() {
        return templates.values().stream()
                .sorted(Comparator.comparing(TemplateRecord::resourceKey))
                .toList();
    }

    public synchronized List<ReportRecord> reports() {
        return reports.values().stream()
                .sorted(Comparator.comparing(ReportRecord::createdAt).reversed())
                .toList();
    }

    public synchronized List<AssignmentRecord> assignments() {
        pruneRuntimeState(Instant.now());
        return List.copyOf(assignments.values());
    }

    public synchronized List<EditLease> leases() {
        pruneRuntimeState(Instant.now());
        return List.copyOf(leases.values());
    }

    public synchronized long registryRevision() {
        return Math.max(1L, registryRevision);
    }

    public Settings settings() {
        return settings;
    }

    public FancyTagObjectStore objectStore() {
        return objectStore;
    }

    public FancyTagTransferService transfers() {
        return transfers;
    }

    @Override
    public synchronized void flush() throws IOException {
        if (!dirty()) {
            return;
        }
        StorageService.write(path, domain(), SCHEMA_VERSION, GSON.toJsonTree(snapshot()), document);
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(null);
        flushedRevision = registryRevision;
        if (state == RepositoryState.MISSING || state == RepositoryState.NEW) {
            state = RepositoryState.READY;
        }
    }

    @Override
    public synchronized boolean dirty() {
        return registryRevision != flushedRevision;
    }

    @Override
    public synchronized RepositoryState state() {
        return state;
    }

    private Snapshot snapshot() {
        return new Snapshot(
                tags(),
                categories(),
                palettes(),
                templates(),
                List.copyOf(assignments.values()),
                reports());
    }

    private PreparedSnapshot prepareSnapshot(Snapshot snapshot) {
        if (snapshot.tags().size() > settings.maximumTags()
                || snapshot.categories().size() > settings.maximumCategories()
                || snapshot.palettes().size() > HARD_MAXIMUM_PALETTES
                || snapshot.templates().size() > HARD_MAXIMUM_TEMPLATES
                || snapshot.reports().size() > HARD_MAXIMUM_REPORTS
                || snapshot.assignments().size() > settings.maximumAssignments()) {
            throw new IllegalArgumentException("Fancy Tags backup collections are outside bounds");
        }
        Map<UUID, CategoryRecord> preparedCategories = new LinkedHashMap<>();
        Map<String, UUID> preparedCategoriesByKey = new LinkedHashMap<>();
        Map<UUID, PaletteRecord> preparedPalettes = new LinkedHashMap<>();
        Map<String, UUID> preparedPalettesByKey = new LinkedHashMap<>();
        Map<UUID, TemplateRecord> preparedTemplates = new LinkedHashMap<>();
        Map<String, UUID> preparedTemplatesByKey = new LinkedHashMap<>();
        Map<UUID, ReportRecord> preparedReports = new LinkedHashMap<>();
        Map<UUID, TagRecord> preparedTags = new LinkedHashMap<>();
        Map<String, UUID> preparedTagsByKey = new LinkedHashMap<>();
        Map<UUID, AssignmentRecord> preparedAssignments = new LinkedHashMap<>();
        Set<String> references = new LinkedHashSet<>();
        long maximumRevision = 1L;
        for (CategoryRecord category : snapshot.categories()) {
            validateCategory(category);
            if (preparedCategories.putIfAbsent(category.id(), category) != null
                    || preparedCategoriesByKey.putIfAbsent(category.resourceKey(), category.id()) != null) {
                throw new IllegalArgumentException("duplicate Fancy Tags backup category");
            }
            maximumRevision = Math.max(maximumRevision, category.revision());
        }
        for (PaletteRecord palette : snapshot.palettes()) {
            validatePalette(palette);
            if (preparedPalettes.putIfAbsent(palette.id(), palette) != null
                    || preparedPalettesByKey.putIfAbsent(palette.resourceKey(), palette.id()) != null) {
                throw new IllegalArgumentException("duplicate Fancy Tags backup palette");
            }
            maximumRevision = Math.max(maximumRevision, palette.revision());
        }
        for (TemplateRecord template : snapshot.templates()) {
            validateTemplate(template);
            if (preparedTemplates.putIfAbsent(template.id(), template) != null
                    || preparedTemplatesByKey.putIfAbsent(template.resourceKey(), template.id()) != null) {
                throw new IllegalArgumentException("duplicate Fancy Tags backup template");
            }
            maximumRevision = Math.max(maximumRevision, template.revision());
        }
        for (TagRecord tag : snapshot.tags()) {
            validateTag(tag);
            if (tag.categoryId() != null && !preparedCategories.containsKey(tag.categoryId())) {
                throw new IllegalArgumentException("tag backup references a missing category");
            }
            if (preparedTags.putIfAbsent(tag.id(), tag) != null
                    || preparedTagsByKey.putIfAbsent(tag.resourceKey(), tag.id()) != null) {
                throw new IllegalArgumentException("duplicate Fancy Tags backup tag");
            }
            tag.revisions().stream().map(ArtworkRevision::contentHash).forEach(references::add);
            maximumRevision = Math.max(maximumRevision, tag.recordRevision());
        }
        Map<String, Integer> assignmentCounts = new LinkedHashMap<>();
        for (AssignmentRecord assignment : snapshot.assignments()) {
            validateAssignment(assignment);
            TagRecord tag = preparedTags.get(assignment.tagId());
            if (tag == null
                    || tag.revisions().stream().noneMatch(revision ->
                    revision.revision() == assignment.requiredArtworkRevision())
                    || preparedAssignments.putIfAbsent(assignment.id(), assignment) != null) {
                throw new IllegalArgumentException("invalid Fancy Tags backup assignment");
            }
            String target = assignment.targetType().name() + "|" + assignment.targetId();
            int count = assignmentCounts.merge(target, 1, Integer::sum);
            if (count > settings.maximumAssignmentsPerTarget()) {
                throw new IllegalArgumentException("tag backup target assignment quota exceeded");
            }
            maximumRevision = Math.max(maximumRevision, assignment.revision());
        }
        for (ReportRecord report : snapshot.reports()) {
            validateReport(report);
            if (!preparedTags.containsKey(report.tagId())
                    || preparedReports.putIfAbsent(report.id(), report) != null) {
                throw new IllegalArgumentException("invalid Fancy Tags backup report");
            }
            maximumRevision = Math.max(maximumRevision, report.revision());
        }
        return new PreparedSnapshot(
                Map.copyOf(preparedTags),
                Map.copyOf(preparedTagsByKey),
                Map.copyOf(preparedCategories),
                Map.copyOf(preparedCategoriesByKey),
                Map.copyOf(preparedPalettes),
                Map.copyOf(preparedPalettesByKey),
                Map.copyOf(preparedTemplates),
                Map.copyOf(preparedTemplatesByKey),
                Map.copyOf(preparedAssignments),
                Map.copyOf(preparedReports),
                Set.copyOf(references),
                maximumRevision);
    }

    private Set<String> referencedHashes() {
        Set<String> result = new LinkedHashSet<>();
        for (TagRecord tag : tags.values()) {
            for (ArtworkRevision revision : tag.revisions()) {
                result.add(revision.contentHash());
            }
        }
        return Set.copyOf(result);
    }

    private Optional<ArtworkRevision> artwork(TagRecord tag, long revision) {
        return tag.revisions().stream()
                .filter(value -> value.revision() == revision)
                .findFirst();
    }

    private boolean targetMatches(AssignmentRecord assignment, ViewerContext context) {
        return switch (assignment.targetType()) {
            case PLAYER -> assignment.targetId().equals(context.subjectId().toString());
            case GROUP -> context.permissionGroups().contains(assignment.targetId());
            case TEAM -> assignment.targetId().equals(context.scoreboardTeam());
            case DEFAULT -> true;
        };
    }

    private static int targetRank(TargetType type) {
        return switch (type) {
            case PLAYER -> 0;
            case GROUP -> 1;
            case TEAM -> 2;
            case DEFAULT -> 3;
        };
    }

    private static String normalizeTarget(TargetType type, String targetId) {
        if (type == TargetType.DEFAULT) {
            return "default";
        }
        String result = Objects.requireNonNull(targetId, "targetId").trim().toLowerCase(Locale.ROOT);
        if (result.isBlank() || result.length() > 128 || result.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("invalid tag assignment target");
        }
        if (type == TargetType.PLAYER) {
            return UUID.fromString(result).toString();
        }
        return result;
    }

    private static <T> Optional<T> findRecord(
            String reference,
            Map<UUID, T> records,
            Map<String, UUID> keys
    ) {
        if (reference == null || reference.isBlank()) {
            return Optional.empty();
        }
        String normalized = reference.trim().toLowerCase(Locale.ROOT);
        try {
            T byId = records.get(UUID.fromString(normalized));
            if (byId != null) {
                return Optional.of(byId);
            }
        } catch (IllegalArgumentException ignored) {
        }
        UUID id = keys.get(normalized);
        return Optional.ofNullable(id == null ? null : records.get(id));
    }

    private static String normalizeKey(String value) {
        String result = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (!result.matches(KEY_PATTERN)) {
            throw new IllegalArgumentException("resource key is invalid");
        }
        return result;
    }

    private static String normalizeResourceLocation(String value) {
        String result = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (!result.matches("[a-z0-9_.-]+:[a-z0-9/._-]+") || result.length() > 128) {
            throw new IllegalArgumentException("resource location is invalid");
        }
        return result;
    }

    private static String bounded(String value, int maximum) {
        String result = Objects.requireNonNullElse(value, "").codePoints()
                .filter(codePoint -> !Character.isISOControl(codePoint))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString()
                .trim();
        if (result.length() > maximum) {
            throw new IllegalArgumentException("Fancy Tags text exceeds its bound");
        }
        return result;
    }

    private static String normalizeColor(String value) {
        String result = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (result.matches("#[0-9a-f]{6}")) {
            return result + "ff";
        }
        if (!result.matches("#[0-9a-f]{8}")) {
            throw new IllegalArgumentException("tag color must be rgba hexadecimal");
        }
        return result;
    }

    private static List<String> defaultPaletteColors() {
        return List.of(
                "#000000ff",
                "#ffffffff",
                "#ff5555ff",
                "#55ff55ff",
                "#5555ffff",
                "#ffff55ff",
                "#ff55ffff",
                "#55ffffff");
    }

    private void writable() {
        if (state == RepositoryState.RECOVERY
                || state == RepositoryState.UNSUPPORTED
                || state == RepositoryState.ERROR
                || state == RepositoryState.CLOSED) {
            throw new IllegalStateException("Fancy Tags repository is not writable");
        }
    }

    private void changed(long recordRevision) {
        registryRevision = Math.max(Math.addExact(registryRevision, 1L), recordRevision);
    }

    private void pruneRuntimeState(Instant now) {
        leases.values().removeIf(value -> !value.expiresAt().isAfter(now));
    }

    private static boolean allowedTransition(TagStatus from, TagStatus to) {
        if (from == to) {
            return true;
        }
        return switch (from) {
            case DRAFT -> to == TagStatus.PUBLISHED || to == TagStatus.ARCHIVED
                    || to == TagStatus.PENDING_DELETE;
            case PUBLISHED -> to == TagStatus.HIDDEN || to == TagStatus.ARCHIVED
                    || to == TagStatus.SUSPENDED;
            case HIDDEN -> to == TagStatus.PUBLISHED || to == TagStatus.ARCHIVED
                    || to == TagStatus.SUSPENDED;
            case ARCHIVED -> to == TagStatus.DRAFT || to == TagStatus.PUBLISHED
                    || to == TagStatus.PENDING_DELETE;
            case SUSPENDED -> to == TagStatus.PUBLISHED || to == TagStatus.HIDDEN
                    || to == TagStatus.ARCHIVED;
            case PENDING_DELETE -> to == TagStatus.ARCHIVED;
            case CORRUPT -> to == TagStatus.ARCHIVED || to == TagStatus.DRAFT;
        };
    }

    private static TagRecord copyTag(
            TagRecord current,
            TagStatus status,
            UUID actorId,
            Instant modifiedAt,
            long currentArtworkRevision,
            long recordRevision,
            List<ArtworkRevision> revisions
    ) {
        return new TagRecord(
                current.id(),
                current.resourceKey(),
                current.displayName(),
                current.description(),
                current.categoryId(),
                status,
                current.createdBy(),
                current.createdAt(),
                actorId,
                modifiedAt,
                currentArtworkRevision,
                current.visibilityPermission(),
                current.renderContexts(),
                current.alternativeText(),
                recordRevision,
                revisions);
    }

    private static void validateTag(TagRecord tag) {
        Objects.requireNonNull(tag, "tag");
        normalizeKey(tag.resourceKey());
        if (tag.recordRevision() < 1L
                || tag.currentRevision() < 0L
                || tag.revisions().size() > HARD_MAXIMUM_REVISIONS_PER_TAG
                || tag.currentRevision() > 0L
                && tag.revisions().stream().noneMatch(value -> value.revision() == tag.currentRevision())) {
            throw new IllegalArgumentException("invalid tag record");
        }
        long expected = 1L;
        for (ArtworkRevision revision : tag.revisions()) {
            if (revision.revision() != expected++) {
                throw new IllegalArgumentException("noncontiguous tag artwork history");
            }
        }
    }

    private static void validateCategory(CategoryRecord category) {
        Objects.requireNonNull(category, "category");
        normalizeKey(category.resourceKey());
        normalizeResourceLocation(category.icon());
        if (category.revision() < 1L || category.sortOrder() < 0) {
            throw new IllegalArgumentException("invalid tag category");
        }
    }

    private static void validatePalette(PaletteRecord palette) {
        Objects.requireNonNull(palette, "palette");
        normalizeKey(palette.resourceKey());
        if (palette.revision() < 1L || palette.colors().isEmpty() || palette.colors().size() > 64) {
            throw new IllegalArgumentException("invalid tag palette");
        }
        palette.colors().forEach(FancyTagService::normalizeColor);
    }

    private static void validateTemplate(TemplateRecord template) {
        Objects.requireNonNull(template, "template");
        normalizeKey(template.resourceKey());
        normalizeColor(template.fillColor());
        if (template.revision() < 1L
                || template.width() < 1
                || template.height() < 1
                || template.width() > 256
                || template.height() > 64
                || template.width() * template.height() > 16_384) {
            throw new IllegalArgumentException("invalid tag template");
        }
    }

    private static void validateReport(ReportRecord report) {
        Objects.requireNonNull(report, "report");
        if (report.revision() < 1L
                || report.reason().length() < 3
                || report.status() == ReportStatus.OPEN
                && (report.resolvedBy() != null || report.resolvedAt() != null)
                || report.status() == ReportStatus.CLEARED
                && (report.resolvedBy() == null || report.resolvedAt() == null)) {
            throw new IllegalArgumentException("invalid tag report");
        }
    }

    private static void validateAssignment(AssignmentRecord assignment) {
        Objects.requireNonNull(assignment, "assignment");
        normalizeTarget(assignment.targetType(), assignment.targetId());
        if (assignment.revision() < 1L || assignment.requiredArtworkRevision() < 1L) {
            throw new IllegalArgumentException("invalid tag assignment");
        }
    }

    private static void audit(
            UUID actorId,
            String actionId,
            UUID targetId,
            AuditService.Result result,
            ActionResult.ReasonCode reason
    ) {
        if (CommandAuditScope.active()) {
            return;
        }
        UUID safeActor = actorId == null ? new UUID(0L, 0L) : actorId;
        AuditService.record(AuditService.Event.metadata(
                UUID.randomUUID(),
                safeActor,
                actorId == null ? "console" : safeActor.toString(),
                actorId == null ? "console" : "player",
                actionId,
                targetId == null ? List.of() : List.of(targetId),
                result,
                reason,
                "fancy_tags",
                AuditService.AuditClass.ADMIN_ACTION));
    }

    private static void event(
            FancyTagEvents.Type type,
            UUID actorId,
            UUID tagId,
            UUID assignmentId,
            long revision
    ) {
        FancyTagEvents.publish(new FancyTagEvents.LifecycleEvent(
                type,
                actorId,
                tagId,
                assignmentId,
                Math.max(1L, revision),
                Instant.now()));
    }

    public enum TagStatus {
        DRAFT,
        PUBLISHED,
        HIDDEN,
        ARCHIVED,
        SUSPENDED,
        PENDING_DELETE,
        CORRUPT
    }

    public enum RenderContext {
        CHAT,
        NAMEPLATE,
        TAB,
        HUD,
        TOOLTIP,
        GUI
    }

    public enum TagSlot {
        CHAT_PREFIX,
        CHAT_SUFFIX,
        NAMEPLATE_PREFIX,
        NAMEPLATE_SUFFIX,
        TAB_PREFIX,
        TAB_SUFFIX,
        BADGE,
        HUD,
        TOOLTIP
    }

    public enum TargetType {
        PLAYER,
        GROUP,
        TEAM,
        DEFAULT
    }

    public enum ReportStatus {
        OPEN,
        CLEARED
    }

    public record Settings(
            boolean enabled,
            int maximumTags,
            int maximumCategories,
            int maximumAssignments,
            int maximumAssignmentsPerTarget,
            int maximumRevisionsPerTag,
            int maximumLeases,
            int maximumTagsPerSlot,
            int maximumResolvedTags,
            int maximumTotalRenderWidth,
            Duration leaseDuration,
            Duration minimumLeaseRenewal,
            Duration importSettleInterval,
            FancyTagObjectStore.Limits objectLimits
    ) {
        public Settings {
            Objects.requireNonNull(leaseDuration, "leaseDuration");
            Objects.requireNonNull(minimumLeaseRenewal, "minimumLeaseRenewal");
            Objects.requireNonNull(importSettleInterval, "importSettleInterval");
            Objects.requireNonNull(objectLimits, "objectLimits");
            if (maximumTags < 1 || maximumTags > HARD_MAXIMUM_TAGS
                    || maximumCategories < 1 || maximumCategories > HARD_MAXIMUM_CATEGORIES
                    || maximumAssignments < 1 || maximumAssignments > HARD_MAXIMUM_ASSIGNMENTS
                    || maximumAssignmentsPerTarget < 1 || maximumAssignmentsPerTarget > 1_024
                    || maximumRevisionsPerTag < 1
                    || maximumRevisionsPerTag > HARD_MAXIMUM_REVISIONS_PER_TAG
                    || maximumLeases < 1 || maximumLeases > HARD_MAXIMUM_LEASES
                    || maximumTagsPerSlot < 1 || maximumTagsPerSlot > 16
                    || maximumResolvedTags < 1 || maximumResolvedTags > 64
                    || maximumTotalRenderWidth < 8 || maximumTotalRenderWidth > 2_048
                    || leaseDuration.isNegative() || leaseDuration.isZero()
                    || leaseDuration.compareTo(Duration.ofMinutes(30)) > 0
                    || minimumLeaseRenewal.isNegative() || minimumLeaseRenewal.isZero()
                    || minimumLeaseRenewal.compareTo(leaseDuration) >= 0
                    || importSettleInterval.isNegative()
                    || importSettleInterval.compareTo(Duration.ofMinutes(5)) > 0) {
                throw new IllegalArgumentException("Fancy Tags settings are outside hard bounds");
            }
        }

        public static Settings defaults() {
            return new Settings(
                    false,
                    1_024,
                    128,
                    16_384,
                    32,
                    20,
                    256,
                    3,
                    16,
                    128,
                    Duration.ofSeconds(120),
                    Duration.ofSeconds(30),
                    Duration.ofSeconds(2),
                    FancyTagObjectStore.Limits.defaults());
        }
    }

    public record TagRecord(
            UUID id,
            String resourceKey,
            String displayName,
            String description,
            UUID categoryId,
            TagStatus status,
            UUID createdBy,
            Instant createdAt,
            UUID modifiedBy,
            Instant modifiedAt,
            long currentRevision,
            String visibilityPermission,
            Set<RenderContext> renderContexts,
            String alternativeText,
            long recordRevision,
            List<ArtworkRevision> revisions
    ) {
        public TagRecord {
            Objects.requireNonNull(id, "id");
            resourceKey = normalizeKey(resourceKey);
            displayName = bounded(displayName, 64);
            description = bounded(description, 512);
            Objects.requireNonNull(status, "status");
            Objects.requireNonNull(createdAt, "createdAt");
            Objects.requireNonNull(modifiedAt, "modifiedAt");
            visibilityPermission = bounded(visibilityPermission, 128).toLowerCase(Locale.ROOT);
            renderContexts = Set.copyOf(renderContexts);
            alternativeText = bounded(alternativeText, 128);
            revisions = List.copyOf(revisions);
        }
    }

    public record ArtworkRevision(
            long revision,
            String contentHash,
            String canonicalFormat,
            int width,
            int height,
            int pixels,
            int encodedBytes,
            int decodedBytes,
            UUID createdBy,
            Instant createdAt,
            long parentRevision
    ) {
        public ArtworkRevision {
            Objects.requireNonNull(contentHash, "contentHash");
            Objects.requireNonNull(canonicalFormat, "canonicalFormat");
            Objects.requireNonNull(createdAt, "createdAt");
            if (revision < 1L
                    || !contentHash.matches("[0-9a-f]{64}")
                    || !canonicalFormat.equals("png")
                    || width < 1
                    || height < 1
                    || pixels != width * height
                    || encodedBytes < 1
                    || decodedBytes != pixels * 4
                    || parentRevision < 0L
                    || parentRevision >= revision) {
                throw new IllegalArgumentException("invalid tag artwork revision");
            }
        }
    }

    private record ExportManifest(
            int schemaVersion,
            UUID id,
            String resourceKey,
            String displayName,
            String description,
            UUID categoryId,
            TagStatus status,
            String visibilityPermission,
            Set<RenderContext> renderContexts,
            String alternativeText,
            long artworkRevision,
            String contentHash,
            String canonicalFormat,
            int width,
            int height,
            int pixels,
            int encodedBytes
    ) {
    }

    public record CategoryRecord(
            UUID id,
            String resourceKey,
            String displayName,
            String description,
            String icon,
            int sortOrder,
            String visibilityPermission,
            UUID modifiedBy,
            Instant modifiedAt,
            long revision
    ) {
        public CategoryRecord {
            Objects.requireNonNull(id, "id");
            resourceKey = normalizeKey(resourceKey);
            displayName = bounded(displayName, 64);
            description = bounded(description, 512);
            icon = normalizeResourceLocation(icon);
            visibilityPermission = bounded(visibilityPermission, 128).toLowerCase(Locale.ROOT);
            Objects.requireNonNull(modifiedAt, "modifiedAt");
        }
    }

    public record PaletteRecord(
            UUID id,
            String resourceKey,
            String displayName,
            List<String> colors,
            String visibilityPermission,
            UUID modifiedBy,
            Instant modifiedAt,
            long revision
    ) {
        public PaletteRecord {
            Objects.requireNonNull(id, "id");
            resourceKey = normalizeKey(resourceKey);
            displayName = bounded(displayName, 64);
            colors = colors == null ? List.of() : colors.stream()
                    .map(FancyTagService::normalizeColor)
                    .distinct()
                    .limit(64)
                    .toList();
            visibilityPermission = bounded(visibilityPermission, 128).toLowerCase(Locale.ROOT);
            Objects.requireNonNull(modifiedAt, "modifiedAt");
            validatePalette(this);
        }
    }

    public record TemplateRecord(
            UUID id,
            String resourceKey,
            String displayName,
            int width,
            int height,
            String fillColor,
            String visibilityPermission,
            UUID modifiedBy,
            Instant modifiedAt,
            long revision
    ) {
        public TemplateRecord {
            Objects.requireNonNull(id, "id");
            resourceKey = normalizeKey(resourceKey);
            displayName = bounded(displayName, 64);
            fillColor = normalizeColor(fillColor);
            visibilityPermission = bounded(visibilityPermission, 128).toLowerCase(Locale.ROOT);
            Objects.requireNonNull(modifiedAt, "modifiedAt");
            if (revision < 1L || width < 1 || height < 1
                    || width > 256 || height > 64 || width * height > 16_384) {
                throw new IllegalArgumentException("invalid tag template");
            }
        }
    }

    public record ReportRecord(
            UUID id,
            UUID tagId,
            UUID reporterId,
            String reason,
            ReportStatus status,
            Instant createdAt,
            UUID resolvedBy,
            Instant resolvedAt,
            String resolution,
            long revision
    ) {
        public ReportRecord {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(tagId, "tagId");
            Objects.requireNonNull(reporterId, "reporterId");
            reason = bounded(reason, 256);
            Objects.requireNonNull(status, "status");
            Objects.requireNonNull(createdAt, "createdAt");
            resolution = bounded(resolution, 256);
            validateReport(this);
        }
    }

    public record AssignmentRecord(
            UUID id,
            UUID tagId,
            long requiredArtworkRevision,
            TargetType targetType,
            String targetId,
            TagSlot slot,
            int priority,
            Instant startsAt,
            Instant expiresAt,
            boolean enabled,
            UUID assignedBy,
            Instant assignedAt,
            long revision,
            String reason
    ) {
        public AssignmentRecord {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(tagId, "tagId");
            Objects.requireNonNull(targetType, "targetType");
            targetId = normalizeTarget(targetType, targetId);
            Objects.requireNonNull(slot, "slot");
            Objects.requireNonNull(startsAt, "startsAt");
            Objects.requireNonNull(assignedAt, "assignedAt");
            reason = bounded(reason, 256);
        }
    }

    public record EditLease(
            UUID leaseId,
            UUID tagId,
            UUID holder,
            long expectedTagRevision,
            Instant acquiredAt,
            Instant expiresAt,
            Instant renewalDeadline,
            long revision
    ) {
        public EditLease {
            Objects.requireNonNull(leaseId, "leaseId");
            Objects.requireNonNull(tagId, "tagId");
            Objects.requireNonNull(holder, "holder");
            Objects.requireNonNull(acquiredAt, "acquiredAt");
            Objects.requireNonNull(expiresAt, "expiresAt");
            Objects.requireNonNull(renewalDeadline, "renewalDeadline");
            if (expectedTagRevision < 1L || revision < 1L
                    || !expiresAt.isAfter(acquiredAt)
                    || !renewalDeadline.isAfter(acquiredAt)) {
                throw new IllegalArgumentException("invalid tag edit lease");
            }
        }
    }

    public record ViewerContext(
            UUID viewerId,
            UUID subjectId,
            Set<String> permissionGroups,
            String scoreboardTeam,
            boolean subjectVanishedFromViewer
    ) {
        public ViewerContext {
            Objects.requireNonNull(viewerId, "viewerId");
            Objects.requireNonNull(subjectId, "subjectId");
            permissionGroups = permissionGroups.stream()
                    .map(value -> value.toLowerCase(Locale.ROOT))
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
            scoreboardTeam = Objects.requireNonNullElse(scoreboardTeam, "").toLowerCase(Locale.ROOT);
        }
    }

    public record ResolvedTag(
            UUID tagId,
            long tagRevision,
            String resourceKey,
            String displayName,
            String alternativeText,
            UUID assignmentId,
            TagSlot slot,
            int priority,
            TargetType targetType,
            ArtworkRevision artwork
    ) {
    }

    private record Snapshot(
            List<TagRecord> tags,
            List<CategoryRecord> categories,
            List<PaletteRecord> palettes,
            List<TemplateRecord> templates,
            List<AssignmentRecord> assignments,
            List<ReportRecord> reports
    ) {
        private Snapshot {
            tags = tags == null ? List.of() : List.copyOf(tags);
            categories = categories == null ? List.of() : List.copyOf(categories);
            palettes = palettes == null ? List.of() : List.copyOf(palettes);
            templates = templates == null ? List.of() : List.copyOf(templates);
            assignments = assignments == null ? List.of() : List.copyOf(assignments);
            reports = reports == null ? List.of() : List.copyOf(reports);
        }
    }

    private record BackupEnvelope(int schemaVersion, Instant createdAt, Snapshot snapshot) {
        private BackupEnvelope {
            Objects.requireNonNull(createdAt, "createdAt");
        }
    }

    private record PreparedSnapshot(
            Map<UUID, TagRecord> tags,
            Map<String, UUID> tagsByKey,
            Map<UUID, CategoryRecord> categories,
            Map<String, UUID> categoriesByKey,
            Map<UUID, PaletteRecord> palettes,
            Map<String, UUID> palettesByKey,
            Map<UUID, TemplateRecord> templates,
            Map<String, UUID> templatesByKey,
            Map<UUID, AssignmentRecord> assignments,
            Map<UUID, ReportRecord> reports,
            Set<String> referencedHashes,
            long maximumRevision
    ) {
    }
}
