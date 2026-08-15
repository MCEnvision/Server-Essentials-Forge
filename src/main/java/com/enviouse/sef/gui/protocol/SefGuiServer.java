package com.enviouse.sef.gui.protocol;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.audit.SecurityAuditService;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.control.ServerControlCatalog;
import com.enviouse.sef.control.ServerControlCommands;
import com.enviouse.sef.control.ServerControlExecutionService;
import com.enviouse.sef.control.ServerControlRepository;
import com.enviouse.sef.control.ServerControlSchemaRegistry;
import com.enviouse.sef.disguise.DisguiseService;
import com.enviouse.sef.disguise.DisguiseAbilityExecutor;
import com.enviouse.sef.fancytags.FancyTagService;
import com.enviouse.sef.fancytags.FancyTagTransferService;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.kernel.policy.ConfirmationService;
import com.enviouse.sef.gui.AdminPanelService;
import com.enviouse.sef.gui.UniversalGuiCatalog;
import com.enviouse.sef.identity.PlayerProfileRepository;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.teleport.HomeRecord;
import com.enviouse.sef.teleport.TeleportRequestService;
import com.enviouse.sef.teleport.TeleportSettings;
import com.enviouse.sef.teleport.WarpRecord;
import com.enviouse.sef.vanish.VanishUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public final class SefGuiServer {
    public static final String DASHBOARD = "dashboard";
    public static final String HOMES = "homes";
    public static final String WARPS = "warps";
    public static final String TELEPORT_REQUESTS = "teleport_requests";
    public static final String HELP = "help";
    public static final String STAFF = "staff";
    public static final String PLAYERS = "players";
    public static final String PLAYERS_ONLINE = "players_online";

    private static final int PAGE_SIZE = 12;
    private static final AtomicLong PANEL_REVISIONS = new AtomicLong();
    private static final AtomicLong TARGET_REVISIONS = new AtomicLong();
    private static final AtomicLong TAG_PROJECTION_REVISIONS = new AtomicLong();
    private static final AtomicLong DISGUISE_PROJECTION_REVISIONS = new AtomicLong();
    private static final Map<UUID, OpenPanel> PANELS = new LinkedHashMap<>();
    private static final Map<UUID, Long> PLAYER_REVISIONS = new LinkedHashMap<>();
    private static final Map<UUID, AuthorizedTagProjection> TAG_PROJECTIONS = new LinkedHashMap<>();
    private static final Map<UUID, Map<String, TagDownload>> TAG_DOWNLOADS = new LinkedHashMap<>();
    private static final Map<UUID, Map<UUID, SefPayloads.DisguiseProjection>> DISGUISE_PROJECTIONS =
            new LinkedHashMap<>();
    private static final Map<UUID, PendingControlConfirmation> CONTROL_CONFIRMATIONS =
            new LinkedHashMap<>();
    private SefGuiServer() {
    }

    public static void handleOpen(ServerPlayer player, SefPayloads.OpenPanelRequest request) {
        SefProtocol.Feature feature = feature(request.panelId());
        if (feature == null
                || SefSessionManager.instance().acceptRequest(
                player,
                request.sessionId(),
                request.sequence(),
                feature) != SefSessionManager.RequestDecision.ACCEPTED) {
            return;
        }
        open(player, request.panelId(), request.page(), request.query());
    }

    public static void handleAction(ServerPlayer player, SefPayloads.PanelActionRequest request) {
        SefProtocol.Feature feature = feature(request.panelId());
        if (feature == null
                || SefSessionManager.instance().acceptRequest(
                player,
                request.sessionId(),
                request.sequence(),
                feature) != SefSessionManager.RequestDecision.ACCEPTED) {
            return;
        }
        OpenPanel panel;
        synchronized (PANELS) {
            panel = PANELS.get(player.getUUID());
        }
        if (panel == null) {
            open(player, request.panelId(), 1, "");
            return;
        }
        AllowedAction allowed = panel.actions().get(request.entryId());
        PanelActionValidator.Decision decision = PanelActionValidator.validate(
                panel.sessionId(),
                panel.panelId(),
                panel.revision(),
                panel.expiresAt(),
                allowed == null ? null : allowed.controlId(),
                allowed == null ? null : request.entryId(),
                allowed == null ? -1L : allowed.entryRevision(),
                request,
                Instant.now());
        if (decision != PanelActionValidator.Decision.ACCEPTED
                || !allowed.stillValid().test(player)) {
            open(player, request.panelId(), panel.page(), panel.query());
            return;
        }
        if (allowed.kind() == ActionKind.OPEN_PANEL) {
            open(player, allowed.value(), 1, "");
            return;
        }
        if (allowed.kind() == ActionKind.SELECT_PLAYER) {
            openPlayerDetail(player, panel.panelId(), UUID.fromString(allowed.value()));
            return;
        }
        if (allowed.kind() == ActionKind.DETAIL) {
            openHelpDetail(player, allowed.value());
            return;
        }
        if (allowed.kind() == ActionKind.CONFIRM) {
            openConfirmation(player, panel.panelId(), allowed);
            return;
        }
        if (allowed.kind() == ActionKind.CONTROL_CREATE) {
            createControlRecord(player, allowed.value());
            return;
        }
        if (allowed.kind() == ActionKind.WORKFLOW) {
            GuiWorkflowService.open(player, allowed.value(), panel.panelId());
            return;
        }
        if (allowed.kind() == ActionKind.FANCY_TAG_STUDIO) {
            SefSessionManager.instance().session(player)
                    .filter(session -> session.supports(SefProtocol.Feature.FANCY_TAGS_STATIC))
                    .ifPresent(session -> PacketDistributor.sendToPlayer(
                            player,
                            new SefPayloads.OpenFancyTagsStudio(session.sessionId(), allowed.value())));
            return;
        }
        if (allowed.kind() == ActionKind.COMMAND) {
            player.server.getCommands().performPrefixedCommand(
                    player.createCommandSourceStack(),
                    allowed.value());
            open(player, panel.panelId(), panel.page(), panel.query());
        }
    }

    public static void handleControlMutation(
            ServerPlayer player,
            SefPayloads.ControlMutationRequest request
    ) {
        if (SefSessionManager.instance().acceptRequest(
                player,
                request.sessionId(),
                request.sequence(),
                SefProtocol.Feature.CONTROL_EDITOR) != SefSessionManager.RequestDecision.ACCEPTED) {
            return;
        }
        OpenPanel panel;
        synchronized (PANELS) {
            panel = PANELS.get(player.getUUID());
        }
        if (panel == null
                || !panel.sessionId().equals(request.sessionId())
                || !panel.panelId().equals(request.panelId())
                || panel.revision() != request.panelRevision()
                || panel.expiresAt().isBefore(Instant.now())
                || !panel.panelId().equals("control_edit:" + request.recordId())) {
            return;
        }
        ServerControlRepository.ControlRecord record =
                KernelServices.serverControls().find(request.recordId()).orElse(null);
        if (record == null
                || record.revision() != request.expectedRecordRevision()
                || !canManageControlRecord(player, record)) {
            openControlEditor(player, request.recordId(), "The record changed or access was revoked.");
            return;
        }
        String operation = request.operation().strip().toLowerCase(Locale.ROOT);
        switch (operation) {
            case "save" -> saveControlRecord(player, record, request);
            case "preview" -> previewControlRecord(player, record);
            case "transition" -> transitionControlRecord(player, record, request.argument());
            case "execute" -> executeControlRecord(player, record);
            default -> openControlEditor(player, record.id(), "The requested editor operation is unavailable.");
        }
    }

    public static boolean openDashboard(ServerPlayer player) {
        SefSessionManager.SessionView session =
                SefSessionManager.instance().session(player).orElse(null);
        if (session == null || !session.supports(SefProtocol.Feature.DASHBOARD)) {
            return false;
        }
        open(player, DASHBOARD, 1, "");
        return true;
    }

    public static void sendTagManifest(ServerPlayer player) {
        SefSessionManager.SessionView session =
                SefSessionManager.instance().session(player).orElse(null);
        if (session == null
                || !session.supports(SefProtocol.Feature.FANCY_TAGS_STATIC)
                || !ConfigHandler.config.enableFancyTags.get()
                || !ConfigHandler.config.fancyTagsEnhancedRendering.get()) {
            return;
        }
        AuthorizedTagProjection projection = authorizedTags(player);
        long projectionRevision = TAG_PROJECTION_REVISIONS.incrementAndGet();
        AuthorizedTagProjection previous = TAG_PROJECTIONS.put(player.getUUID(), projection);
        if (session.supports(SefProtocol.Feature.FANCY_TAGS_REGISTRY)
                && player.connection.hasChannel(SefPayloads.TagRegistrySnapshot.TYPE)
                && player.connection.hasChannel(SefPayloads.TagAssignmentSnapshot.TYPE)) {
            if (previous != null
                    && player.connection.hasChannel(SefPayloads.TagRegistryDelta.TYPE)
                    && player.connection.hasChannel(SefPayloads.TagAssignmentDelta.TYPE)) {
                sendTagDeltas(player, session.sessionId(), projectionRevision, previous, projection);
            } else {
                PacketDistributor.sendToPlayer(player, new SefPayloads.TagRegistrySnapshot(
                        session.sessionId(),
                        projectionRevision,
                        true,
                        projection.entries()));
                PacketDistributor.sendToPlayer(player, new SefPayloads.TagAssignmentSnapshot(
                        session.sessionId(),
                        projectionRevision,
                        true,
                        projection.assignments()));
            }
        }
        if (!projection.entries().isEmpty()
                && player.connection.hasChannel(SefPayloads.TagManifest.TYPE)) {
            SefPayloads.TagManifestEntry first = projection.entries().getFirst();
            PacketDistributor.sendToPlayer(player, new SefPayloads.TagManifest(
                    session.sessionId(),
                    projectionRevision,
                    first.tagId(),
                    first.hash(),
                    first.byteLength(),
                    first.alternateText()));
        }
    }

    private static void sendTagDeltas(
            ServerPlayer player,
            UUID sessionId,
            long revision,
            AuthorizedTagProjection previous,
            AuthorizedTagProjection current
    ) {
        Map<UUID, SefPayloads.TagManifestEntry> previousTags = previous.entries().stream()
                .collect(java.util.stream.Collectors.toMap(
                        SefPayloads.TagManifestEntry::tagId,
                        entry -> entry,
                        (first, ignored) -> first,
                        LinkedHashMap::new));
        Map<UUID, SefPayloads.TagManifestEntry> currentTags = current.entries().stream()
                .collect(java.util.stream.Collectors.toMap(
                        SefPayloads.TagManifestEntry::tagId,
                        entry -> entry,
                        (first, ignored) -> first,
                        LinkedHashMap::new));
        List<UUID> removedTags = previousTags.keySet().stream()
                .filter(id -> !currentTags.containsKey(id))
                .toList();
        List<SefPayloads.TagManifestEntry> updatedTags = currentTags.values().stream()
                .filter(entry -> !entry.equals(previousTags.get(entry.tagId())))
                .toList();

        Map<String, SefPayloads.TagAssignmentProjection> previousAssignments =
                assignmentProjectionMap(previous.assignments());
        Map<String, SefPayloads.TagAssignmentProjection> currentAssignments =
                assignmentProjectionMap(current.assignments());
        List<SefPayloads.TagAssignmentKey> removedAssignments =
                previousAssignments.values().stream()
                        .filter(assignment -> !currentAssignments.containsKey(
                                assignmentProjectionKey(assignment)))
                        .map(assignment -> new SefPayloads.TagAssignmentKey(
                                assignment.subjectId(),
                                assignment.tagId(),
                                assignment.slot()))
                        .toList();
        List<SefPayloads.TagAssignmentProjection> updatedAssignments =
                currentAssignments.values().stream()
                        .filter(assignment -> !assignment.equals(
                                previousAssignments.get(assignmentProjectionKey(assignment))))
                        .toList();
        PacketDistributor.sendToPlayer(player, new SefPayloads.TagRegistryDelta(
                sessionId,
                revision,
                removedTags,
                updatedTags));
        PacketDistributor.sendToPlayer(player, new SefPayloads.TagAssignmentDelta(
                sessionId,
                revision,
                removedAssignments,
                updatedAssignments));
    }

    private static Map<String, SefPayloads.TagAssignmentProjection> assignmentProjectionMap(
            List<SefPayloads.TagAssignmentProjection> assignments
    ) {
        Map<String, SefPayloads.TagAssignmentProjection> result = new LinkedHashMap<>();
        assignments.forEach(assignment ->
                result.put(assignmentProjectionKey(assignment), assignment));
        return result;
    }

    private static String assignmentProjectionKey(SefPayloads.TagAssignmentProjection assignment) {
        return assignment.subjectId() + ":" + assignment.tagId() + ":" + assignment.slot();
    }

    public static void handleTagContent(
            ServerPlayer player,
            SefPayloads.TagContentRequest request
    ) {
        if (SefSessionManager.instance().acceptRequest(
                player,
                request.sessionId(),
                request.sequence(),
                SefProtocol.Feature.FANCY_TAGS_STATIC)
                != SefSessionManager.RequestDecision.ACCEPTED
                || !ConfigHandler.config.enableFancyTags.get()
                || !ConfigHandler.config.fancyTagsEnhancedRendering.get()
                || !player.connection.hasChannel(SefPayloads.TagContent.TYPE)) {
            return;
        }
        AuthorizedTagProjection projection = authorizedTags(player);
        if (projection.entries().stream().noneMatch(entry -> entry.hash().equals(request.hash()))) {
            return;
        }
        try {
            byte[] content = KernelServices.fancyTags().readArtwork(request.hash());
            if (content.length <= ConfigHandler.config.fancyTagsMaximumEncodedBytes.get()) {
                PacketDistributor.sendToPlayer(player, new SefPayloads.TagContent(
                        request.sessionId(),
                        request.hash(),
                        content));
            }
        } catch (java.io.IOException ignored) {
            KernelServices.fancyTags().integrity();
        }
    }

    public static void handleTagContentChunk(
            ServerPlayer player,
            SefPayloads.TagContentChunkRequest request
    ) {
        if (SefSessionManager.instance().acceptRequest(
                player,
                request.sessionId(),
                request.sequence(),
                SefProtocol.Feature.FANCY_TAGS_STATIC)
                != SefSessionManager.RequestDecision.ACCEPTED
                || !ConfigHandler.config.enableFancyTags.get()
                || !ConfigHandler.config.fancyTagsEnhancedRendering.get()
                || !player.connection.hasChannel(SefPayloads.TagContentChunk.TYPE)) {
            return;
        }
        AuthorizedTagProjection projection = authorizedTags(player);
        if (projection.entries().stream().noneMatch(entry -> entry.hash().equals(request.hash()))) {
            return;
        }
        Instant now = Instant.now();
        Map<String, TagDownload> playerDownloads =
                TAG_DOWNLOADS.computeIfAbsent(player.getUUID(), ignored -> new LinkedHashMap<>());
        playerDownloads.values().removeIf(download -> !download.expiresAt().isAfter(now));
        TagDownload download = playerDownloads.get(request.hash());
        if (request.offset() == 0) {
            if (download == null) {
                if (playerDownloads.size() >= 2) {
                    return;
                }
                try {
                    byte[] content = KernelServices.fancyTags().readArtwork(request.hash());
                    if (content.length > ConfigHandler.config.fancyTagsMaximumEncodedBytes.get()
                            || content.length > SefProtocol.MAXIMUM_TAG_BYTES) {
                        return;
                    }
                    download = new TagDownload(
                            request.sessionId(),
                            content,
                            now.plus(Duration.ofSeconds(30)));
                    playerDownloads.put(request.hash(), download);
                } catch (java.io.IOException ignored) {
                    KernelServices.fancyTags().integrity();
                    return;
                }
            }
        } else if (download == null) {
            return;
        }
        if (!download.sessionId().equals(request.sessionId())
                || request.offset() >= download.content().length) {
            playerDownloads.remove(request.hash());
            return;
        }
        int end = Math.min(
                download.content().length,
                request.offset() + SefProtocol.MAXIMUM_TAG_DOWNLOAD_CHUNK_BYTES);
        byte[] chunk = java.util.Arrays.copyOfRange(download.content(), request.offset(), end);
        PacketDistributor.sendToPlayer(player, new SefPayloads.TagContentChunk(
                request.sessionId(),
                request.hash(),
                download.content().length,
                request.offset(),
                chunk));
        if (end == download.content().length) {
            playerDownloads.remove(request.hash());
            if (playerDownloads.isEmpty()) {
                TAG_DOWNLOADS.remove(player.getUUID());
            }
        } else {
            playerDownloads.put(request.hash(), new TagDownload(
                    download.sessionId(),
                    download.content(),
                    now.plus(Duration.ofSeconds(30))));
        }
    }

    public static void handleTagManagerQuery(
            ServerPlayer player,
            SefPayloads.TagManagerQuery request
    ) {
        if (!ConfigHandler.config.enableFancyTags.get()
                || !ConfigHandler.config.fancyTagsEnhancedRendering.get()
                || !has(player, "tags.manage.open")
                || !player.connection.hasChannel(SefPayloads.TagManagerSnapshot.TYPE)
                || SefSessionManager.instance().acceptRequest(
                player,
                request.sessionId(),
                request.sequence(),
                SefProtocol.Feature.FANCY_TAGS_MANAGER)
                != SefSessionManager.RequestDecision.ACCEPTED) {
            return;
        }
        List<SefPayloads.TagManagerEntry> candidates =
                tagManagerEntries(player, request.section());
        String query = request.query().strip().toLowerCase(Locale.ROOT);
        if (!query.isEmpty()) {
            candidates = candidates.stream()
                    .filter(entry -> entry.resourceKey().toLowerCase(Locale.ROOT).contains(query)
                            || entry.title().toLowerCase(Locale.ROOT).contains(query)
                            || entry.subtitle().toLowerCase(Locale.ROOT).contains(query)
                            || entry.status().toLowerCase(Locale.ROOT).contains(query))
                    .toList();
        }
        int pageSize = 50;
        int pages = Math.max(1, (candidates.size() + pageSize - 1) / pageSize);
        int page = Math.clamp(request.page(), 1, pages);
        int start = Math.min(candidates.size(), (page - 1) * pageSize);
        int end = Math.min(candidates.size(), start + pageSize);
        PacketDistributor.sendToPlayer(player, new SefPayloads.TagManagerSnapshot(
                request.sessionId(),
                request.sequence(),
                KernelServices.fancyTags().registryRevision(),
                request.section(),
                page,
                pages,
                candidates.subList(start, end)));
    }

    private static List<SefPayloads.TagManagerEntry> tagManagerEntries(
            ServerPlayer player,
            String section
    ) {
        FancyTagService service = KernelServices.fancyTags();
        List<SefPayloads.TagManagerEntry> result = new ArrayList<>();
        switch (section) {
            case "gallery", "detail" -> {
                if (!has(player, "commands.tags.list")) {
                    return List.of();
                }
                service.tags().stream()
                        .filter(tag -> tag.status() == FancyTagService.TagStatus.PUBLISHED)
                        .map(SefGuiServer::tagManagerEntry)
                        .forEach(result::add);
            }
            case "manager" -> {
                if (!has(player, "commands.tags.list")) {
                    return List.of();
                }
                service.tags().stream()
                        .filter(tag -> switch (tag.status()) {
                            case DRAFT -> has(player, "tags.view.draft");
                            case HIDDEN -> has(player, "tags.view.hidden");
                            case ARCHIVED, PENDING_DELETE -> has(player, "tags.view.archived");
                            default -> true;
                        })
                        .map(SefGuiServer::tagManagerEntry)
                        .forEach(result::add);
            }
            case "assign" -> {
                if (!has(player, "tags.view.assignments")) {
                    return List.of();
                }
                for (FancyTagService.AssignmentRecord assignment : service.assignments()) {
                    FancyTagService.TagRecord tag =
                            service.find(assignment.tagId().toString()).orElse(null);
                    if (tag == null) {
                        continue;
                    }
                    result.add(new SefPayloads.TagManagerEntry(
                            "assignment",
                            assignment.id(),
                            assignment.tagId(),
                            tag.resourceKey(),
                            tag.displayName(),
                            assignment.targetType().name().toLowerCase(Locale.ROOT)
                                    + ", " + assignment.targetId()
                                    + ", " + assignment.slot().name().toLowerCase(Locale.ROOT)
                                    + ", priority " + assignment.priority(),
                            assignment.enabled() ? "active" : "disabled",
                            assignment.revision()));
                }
            }
            case "history" -> {
                if (!has(player, "commands.tags.revision.list")) {
                    return List.of();
                }
                for (FancyTagService.TagRecord tag : service.tags()) {
                    for (FancyTagService.ArtworkRevision revision : tag.revisions()) {
                        result.add(new SefPayloads.TagManagerEntry(
                                "revision",
                                UUID.nameUUIDFromBytes((tag.id() + ":" + revision.revision())
                                        .getBytes(StandardCharsets.UTF_8)),
                                tag.id(),
                                tag.resourceKey(),
                                tag.displayName(),
                                revision.width() + " by " + revision.height()
                                        + ", " + revision.encodedBytes() + " bytes",
                                revision.revision() == tag.currentRevision() ? "current" : "retained",
                                revision.revision()));
                    }
                }
            }
            case "import" -> {
                if (!has(player, "commands.tags.import.inspect")) {
                    return List.of();
                }
                for (var candidate : service.importCandidates()) {
                    result.add(new SefPayloads.TagManagerEntry(
                            "import",
                            UUID.nameUUIDFromBytes(candidate.candidateId().getBytes(StandardCharsets.UTF_8)),
                            null,
                            candidate.candidateId(),
                            candidate.fileName(),
                            candidate.encodedBytes() + " bytes",
                            "candidate",
                            Math.max(1L, candidate.modifiedAt().toEpochMilli())));
                }
            }
            case "transfer" -> {
                if (!has(player, "commands.tags.transfer.status")) {
                    return List.of();
                }
                for (var upload : service.transfers().active(Instant.now())) {
                    result.add(new SefPayloads.TagManagerEntry(
                            "transfer",
                            upload.uploadId(),
                            upload.tagId(),
                            upload.uploadId().toString(),
                            "upload " + upload.uploadId(),
                            upload.receivedBytes() + " of " + upload.totalBytes() + " bytes",
                            "active",
                            Math.max(1L, upload.nextChunkIndex() + 1L)));
                }
            }
            case "integrity", "cache" -> {
                if (!has(player, section.equals("cache")
                        ? "commands.tags.cache.status"
                        : "commands.tags.integrity.check")) {
                    return List.of();
                }
                var report = service.integrity();
                result.add(new SefPayloads.TagManagerEntry(
                        section,
                        UUID.nameUUIDFromBytes(("sef:tags:" + section).getBytes(StandardCharsets.UTF_8)),
                        null,
                        section,
                        section.equals("cache") ? "Server object cache" : "Artwork integrity",
                        "missing " + report.missing().size()
                                + ", corrupt " + report.corrupt().size()
                                + ", orphaned " + report.orphaned().size()
                                + ", bytes " + report.storedBytes(),
                        report.missing().isEmpty() && report.corrupt().isEmpty() ? "healthy" : "attention",
                        service.registryRevision()));
            }
            case "audit" -> {
                if (!has(player, "commands.tags.audit") || !has(player, "tags.view.audit")) {
                    return List.of();
                }
                for (SecurityAuditService.AuditEvent event : SecurityAuditService.recent(
                        value -> value.origin().equals("fancy_tags")
                                || value.actionId().startsWith("sef:tags."),
                        100)) {
                    result.add(new SefPayloads.TagManagerEntry(
                            "audit",
                            UUID.fromString(event.eventId()),
                            auditTarget(event),
                            event.actionId(),
                            event.actionId(),
                            event.actorUsername() + ", " + event.result()
                                    + ", " + event.reasonCode(),
                            event.result(),
                            Math.max(1L, Instant.parse(event.timestamp()).toEpochMilli())));
                }
            }
            case "settings" -> {
                boolean categories = has(player, "commands.tags.category.list");
                boolean palettes = has(player, "commands.tags.palette.list");
                boolean templates = has(player, "commands.tags.template.list");
                if (!categories && !palettes && !templates) {
                    return List.of();
                }
                if (categories) {
                    for (FancyTagService.CategoryRecord category : service.categories()) {
                        result.add(new SefPayloads.TagManagerEntry(
                                "category",
                                category.id(),
                                null,
                                category.resourceKey(),
                                category.displayName(),
                                category.description(),
                                "category",
                                category.revision()));
                    }
                }
                if (palettes) {
                    for (FancyTagService.PaletteRecord palette : service.palettes()) {
                        result.add(new SefPayloads.TagManagerEntry(
                                "palette",
                                palette.id(),
                                null,
                                palette.resourceKey(),
                                palette.displayName(),
                                palette.colors().size() + " colors",
                                "palette",
                                palette.revision()));
                    }
                }
                if (templates) {
                    for (FancyTagService.TemplateRecord template : service.templates()) {
                        result.add(new SefPayloads.TagManagerEntry(
                                "template",
                                template.id(),
                                null,
                                template.resourceKey(),
                                template.displayName(),
                                template.width() + " by " + template.height(),
                                "template",
                                template.revision()));
                    }
                }
            }
            default -> {
                return List.of();
            }
        }
        result.sort(Comparator.comparing(SefPayloads.TagManagerEntry::resourceKey)
                .thenComparing(entry -> entry.id().toString()));
        return List.copyOf(result);
    }

    private static UUID auditTarget(SecurityAuditService.AuditEvent event) {
        if (event.targetUuids().isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(event.targetUuids().getFirst());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static SefPayloads.TagManagerEntry tagManagerEntry(FancyTagService.TagRecord tag) {
        return new SefPayloads.TagManagerEntry(
                "tag",
                tag.id(),
                tag.id(),
                tag.resourceKey(),
                tag.displayName(),
                tag.description(),
                tag.status().name().toLowerCase(Locale.ROOT),
                tag.recordRevision());
    }

    public static void handleTagUploadBegin(ServerPlayer player, SefPayloads.TagUploadBegin request) {
        if (!acceptTagManagerRequest(player, request.sessionId(), request.sequence())
                || !has(player, "commands.tags.import.client")
                || !has(player, "commands.tags.edit")) {
            return;
        }
        FancyTagService service = KernelServices.fancyTags();
        FancyTagService.TagRecord tag = service.find(request.tagId().toString()).orElse(null);
        FancyTagService.EditLease lease = service.leases().stream()
                .filter(value -> value.leaseId().equals(request.leaseId()))
                .findFirst()
                .orElse(null);
        ActionResult<FancyTagTransferService.UploadView> result;
        if (tag == null
                || tag.recordRevision() != request.expectedTagRevision()
                || lease == null
                || !lease.tagId().equals(tag.id())
                || !lease.holder().equals(player.getUUID())
                || lease.expectedTagRevision() != request.expectedTagRevision()) {
            result = ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "tag or edit lease is stale");
        } else {
            result = service.transfers().begin(
                    player.getUUID(),
                    tag.id(),
                    request.leaseId(),
                    request.expectedTagRevision(),
                    request.totalBytes(),
                    request.expectedHash(),
                    Instant.now());
        }
        sendTagOperationResult(
                player,
                request.sequence(),
                result,
                result.successful() ? result.value().uploadId() : null,
                0,
                request.totalBytes());
    }

    public static void handleTagUploadChunk(ServerPlayer player, SefPayloads.TagUploadChunk request) {
        if (!acceptTagManagerRequest(player, request.sessionId(), request.sequence())
                || !has(player, "commands.tags.import.client")
                || !has(player, "commands.tags.edit")) {
            return;
        }
        ActionResult<FancyTagTransferService.UploadView> result =
                KernelServices.fancyTags().transfers().acceptChunk(
                        player.getUUID(),
                        request.uploadId(),
                        request.chunkIndex(),
                        request.content(),
                        Instant.now());
        sendTagOperationResult(
                player,
                request.sequence(),
                result,
                request.uploadId(),
                result.successful() ? result.value().receivedBytes() : 0,
                result.successful() ? result.value().totalBytes() : 0);
    }

    public static void handleTagUploadFinish(ServerPlayer player, SefPayloads.TagUploadFinish request) {
        if (!acceptTagManagerRequest(player, request.sessionId(), request.sequence())
                || !has(player, "commands.tags.import.client")
                || !has(player, "commands.tags.edit")) {
            return;
        }
        FancyTagService service = KernelServices.fancyTags();
        ActionResult<FancyTagTransferService.CompletedUpload> completed =
                service.transfers().finish(player.getUUID(), request.uploadId(), Instant.now());
        ActionResult<?> result = completed.successful()
                ? service.completeUpload(completed.value(), player.getUUID())
                : completed;
        if (result.successful()) {
            refreshFancyTags(player.server);
        }
        sendTagOperationResult(
                player,
                request.sequence(),
                result,
                request.uploadId(),
                completed.successful() ? completed.value().bytes().length : 0,
                completed.successful() ? completed.value().bytes().length : 0);
    }

    public static void handleTagUploadCancel(ServerPlayer player, SefPayloads.TagUploadCancel request) {
        if (!acceptTagManagerRequest(player, request.sessionId(), request.sequence())
                || !has(player, "commands.tags.import.client")) {
            return;
        }
        ActionResult<Void> result = KernelServices.fancyTags().transfers()
                .cancel(player.getUUID(), request.uploadId(), false);
        sendTagOperationResult(player, request.sequence(), result, request.uploadId(), 0, 0);
    }

    public static void handleTagMutation(ServerPlayer player, SefPayloads.TagMutationRequest request) {
        if (!acceptTagManagerRequest(player, request.sessionId(), request.sequence())) {
            return;
        }
        FancyTagService service = KernelServices.fancyTags();
        ActionResult<?> result;
        UUID operationId = null;
        FancyTagService.TagRecord tag = service.find(request.tagReference()).orElse(null);
        switch (request.operation()) {
            case "lease_acquire" -> {
                if (!has(player, "commands.tags.lease.acquire") || tag == null) {
                    result = deniedOrMissing(tag);
                } else {
                    ActionResult<FancyTagService.EditLease> lease = service.acquireLease(
                            request.tagReference(),
                            player.getUUID(),
                            request.expectedTagRevision(),
                            false);
                    result = lease;
                    operationId = lease.successful() ? lease.value().leaseId() : null;
                }
            }
            case "lease_renew" -> {
                if (!has(player, "commands.tags.lease.renew")) {
                    result = ActionResult.failure(ActionResult.ReasonCode.PERMISSION_DENIED, "permission denied");
                } else {
                    result = parseUuid(request.argument())
                            .<ActionResult<?>>map(value -> service.renewLease(value, player.getUUID()))
                            .orElseGet(() -> ActionResult.failure(
                                    ActionResult.ReasonCode.INVALID_INPUT,
                                    "invalid tag edit lease id"));
                }
            }
            case "lease_release" -> {
                if (!has(player, "commands.tags.lease.renew")) {
                    result = ActionResult.failure(ActionResult.ReasonCode.PERMISSION_DENIED, "permission denied");
                } else {
                    result = parseUuid(request.argument())
                            .<ActionResult<?>>map(value -> service.releaseLease(value, player.getUUID(), false))
                            .orElseGet(() -> ActionResult.failure(
                                    ActionResult.ReasonCode.INVALID_INPUT,
                                    "invalid tag edit lease id"));
                }
            }
            case "publish", "hide", "archive", "restore" -> {
                if (!has(player, "commands.tags." + request.operation()) || tag == null) {
                    result = deniedOrMissing(tag);
                } else {
                    FancyTagService.TagStatus status = switch (request.operation()) {
                        case "publish" -> FancyTagService.TagStatus.PUBLISHED;
                        case "hide" -> FancyTagService.TagStatus.HIDDEN;
                        case "archive" -> FancyTagService.TagStatus.ARCHIVED;
                        default -> FancyTagService.TagStatus.DRAFT;
                    };
                    result = service.changeStatus(
                            request.tagReference(),
                            status,
                            player.getUUID(),
                            request.expectedTagRevision());
                }
            }
            case "revision_restore" -> {
                if (!has(player, "commands.tags.revision.restore") || tag == null) {
                    result = deniedOrMissing(tag);
                } else {
                    try {
                        long revision = Long.parseLong(request.argument());
                        result = service.restoreRevision(
                                request.tagReference(),
                                revision,
                                player.getUUID(),
                                request.expectedTagRevision());
                    } catch (NumberFormatException exception) {
                        result = ActionResult.failure(
                                ActionResult.ReasonCode.INVALID_INPUT,
                                "invalid artwork revision");
                    }
                }
            }
            default -> result = ActionResult.failure(
                    ActionResult.ReasonCode.INVALID_INPUT,
                    "unknown tag manager operation");
        }
        if (result.successful()) {
            refreshFancyTags(player.server);
        }
        sendTagOperationResult(player, request.sequence(), result, operationId, 0, 0);
    }

    private static boolean acceptTagManagerRequest(
            ServerPlayer player,
            UUID sessionId,
            long sequence
    ) {
        return ConfigHandler.config.enableFancyTags.get()
                && ConfigHandler.config.fancyTagsEnhancedRendering.get()
                && has(player, "tags.manage.open")
                && player.connection.hasChannel(SefPayloads.TagOperationResult.TYPE)
                && SefSessionManager.instance().acceptRequest(
                player,
                sessionId,
                sequence,
                SefProtocol.Feature.FANCY_TAGS_MANAGER)
                == SefSessionManager.RequestDecision.ACCEPTED;
    }

    private static ActionResult<?> deniedOrMissing(FancyTagService.TagRecord tag) {
        return tag == null
                ? ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag not found")
                : ActionResult.failure(ActionResult.ReasonCode.PERMISSION_DENIED, "permission denied");
    }

    private static java.util.Optional<UUID> parseUuid(String value) {
        try {
            return java.util.Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException exception) {
            return java.util.Optional.empty();
        }
    }

    private static void sendTagOperationResult(
            ServerPlayer player,
            long requestSequence,
            ActionResult<?> result,
            UUID operationId,
            int completedBytes,
            int totalBytes
    ) {
        SefSessionManager.SessionView session = SefSessionManager.instance().session(player).orElse(null);
        if (session == null || !player.connection.hasChannel(SefPayloads.TagOperationResult.TYPE)) {
            return;
        }
        PacketDistributor.sendToPlayer(player, new SefPayloads.TagOperationResult(
                session.sessionId(),
                requestSequence,
                result.successful(),
                result.reason().name().toLowerCase(Locale.ROOT),
                result.detail(),
                operationId,
                KernelServices.fancyTags().registryRevision(),
                Math.max(0, completedBytes),
                Math.max(Math.max(0, completedBytes), totalBytes)));
    }

    public static void invalidateTag(String hash) {
        MinecraftServer server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null || hash == null || !hash.matches("[0-9a-f]{64}")) {
            return;
        }
        long revision = KernelServices.fancyTags().registryRevision();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            SefSessionManager.SessionView session =
                    SefSessionManager.instance().session(player).orElse(null);
            if (session != null
                    && session.supports(SefProtocol.Feature.FANCY_TAGS_REGISTRY)
                    && player.connection.hasChannel(SefPayloads.TagCacheInvalidation.TYPE)) {
                PacketDistributor.sendToPlayer(player, new SefPayloads.TagCacheInvalidation(
                        session.sessionId(),
                        revision,
                        List.of(hash)));
                sendTagManifest(player);
            }
        }
    }

    public static void refreshFancyTags(MinecraftServer server) {
        if (server == null) {
            return;
        }
        server.getPlayerList().getPlayers().forEach(SefGuiServer::sendTagManifest);
    }

    public static void sendDisguiseSnapshot(MinecraftServer server) {
        if (server == null || !ConfigHandler.config.enableDisguises.get()) {
            return;
        }
        for (ServerPlayer viewer : server.getPlayerList().getPlayers()) {
            SefSessionManager.SessionView session =
                    SefSessionManager.instance().session(viewer).orElse(null);
            if (session == null
                    || !session.supports(SefProtocol.Feature.DISGUISE_PROJECTION)
                    || !viewer.connection.hasChannel(SefPayloads.DisguiseSnapshot.TYPE)) {
                continue;
            }
            List<SefPayloads.DisguiseProjection> projections = new ArrayList<>();
            for (ServerPlayer subject : server.getPlayerList().getPlayers()) {
                DisguiseService.Projection projection = KernelServices.disguises().projection(
                        viewer.getUUID(),
                        subject.getUUID(),
                        subject != viewer && VanishUtil.isVanished(subject, viewer),
                        true,
                        PermissionService.has(
                                viewer,
                                PermissionsHandler.phasePermission("commands.disguise.inspect"))).orElse(null);
                if (projection == null
                        || projections.size() >= SefProtocol.MAXIMUM_DISGUISE_PROJECTIONS) {
                    continue;
                }
                DisguiseService.DisguiseRecord record = projection.record();
                DisguiseService.ProfileSnapshot profile = record.profileId() == null
                        ? null
                        : KernelServices.disguises().profile(record.profileId()).orElse(null);
                projections.add(new SefPayloads.DisguiseProjection(
                        record.subjectId(),
                        record.revision(),
                        record.kind().name().toLowerCase(Locale.ROOT),
                        record.reference(),
                        record.profileId(),
                        profile == null ? "" : profile.profileName(),
                        profile == null ? "" : profile.texturesValue(),
                        profile == null ? "" : profile.texturesSignature(),
                        record.labelMode().name().toLowerCase(Locale.ROOT),
                        record.equipmentPolicy().name().toLowerCase(Locale.ROOT),
                        record.traitsEnabled(),
                        record.abilitiesEnabled()));
            }
            Map<UUID, SefPayloads.DisguiseProjection> current = new LinkedHashMap<>();
            projections.forEach(projection -> current.put(projection.subjectId(), projection));
            Map<UUID, SefPayloads.DisguiseProjection> previous =
                    DISGUISE_PROJECTIONS.put(viewer.getUUID(), Map.copyOf(current));
            long revision = DISGUISE_PROJECTION_REVISIONS.incrementAndGet();
            if (previous != null && viewer.connection.hasChannel(SefPayloads.DisguiseDelta.TYPE)) {
                List<UUID> removed = previous.keySet().stream()
                        .filter(id -> !current.containsKey(id))
                        .toList();
                List<SefPayloads.DisguiseProjection> updated = current.values().stream()
                        .filter(projection -> !projection.equals(previous.get(projection.subjectId())))
                        .toList();
                PacketDistributor.sendToPlayer(viewer, new SefPayloads.DisguiseDelta(
                        session.sessionId(),
                        revision,
                        removed,
                        updated));
            } else {
                PacketDistributor.sendToPlayer(viewer, new SefPayloads.DisguiseSnapshot(
                        session.sessionId(),
                        revision,
                        true,
                        projections));
            }
        }
    }

    public static void handleDisguiseAbility(
            ServerPlayer player,
            SefPayloads.DisguiseAbilityRequest request
    ) {
        if (SefSessionManager.instance().acceptRequest(
                player,
                request.sessionId(),
                request.sequence(),
                SefProtocol.Feature.DISGUISE_ABILITY_INPUT)
                != SefSessionManager.RequestDecision.ACCEPTED) {
            return;
        }
        final DisguiseService.AbilitySlot slot;
        try {
            slot = DisguiseService.AbilitySlot.valueOf(request.slot().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return;
        }
        ActionResult<Void> result = DisguiseAbilityExecutor.activate(player, slot);
        if (!result.successful()) {
            player.sendSystemMessage(Component.literal(result.detail()));
        }
    }

    private static AuthorizedTagProjection authorizedTags(ServerPlayer viewer) {
        FancyTagService service = KernelServices.fancyTags();
        var receivePermission = PermissionsHandler.phasePermission("tags.render.receive");
        if (!service.settings().enabled()
                || receivePermission == null
                || !PermissionService.has(viewer, receivePermission)) {
            return new AuthorizedTagProjection(service.registryRevision(), List.of(), List.of());
        }
        Map<UUID, SefPayloads.TagManifestEntry> entries = new LinkedHashMap<>();
        List<SefPayloads.TagAssignmentProjection> assignments = new ArrayList<>();
        for (ServerPlayer subject : viewer.server.getPlayerList().getPlayers()) {
            Set<String> groups = com.enviouse.sef.fancytags.FancyTagGroupResolver
                    .groups(subject.getUUID());
            String team = subject.getTeam() == null ? "" : subject.getTeam().getName();
            FancyTagService.ViewerContext context = new FancyTagService.ViewerContext(
                    viewer.getUUID(),
                    subject.getUUID(),
                    groups,
                    team,
                    subject != viewer && VanishUtil.isVanished(subject, viewer));
            Map<UUID, FancyTagService.ResolvedTag> resolved = new LinkedHashMap<>();
            for (FancyTagService.RenderContext renderContext : FancyTagService.RenderContext.values()) {
                for (FancyTagService.ResolvedTag tag : service.resolve(
                        context,
                        renderContext,
                        permission -> {
                            var node = KernelServices.permissionNode(permission);
                            return node != null && PermissionService.has(viewer, node);
                        })) {
                    resolved.putIfAbsent(tag.assignmentId(), tag);
                }
            }
            for (FancyTagService.ResolvedTag tag : resolved.values()) {
                if (entries.size() >= SefProtocol.MAXIMUM_TAG_MANIFEST_ENTRIES
                        || assignments.size() >= SefProtocol.MAXIMUM_TAG_ASSIGNMENTS) {
                    break;
                }
                FancyTagService.ArtworkRevision artwork = tag.artwork();
                entries.putIfAbsent(tag.tagId(), new SefPayloads.TagManifestEntry(
                        tag.tagId(),
                        tag.tagRevision(),
                        tag.resourceKey(),
                        tag.displayName(),
                        tag.alternativeText(),
                        artwork.contentHash(),
                        artwork.encodedBytes(),
                        artwork.width(),
                        artwork.height()));
                assignments.add(new SefPayloads.TagAssignmentProjection(
                        subject.getUUID(),
                        tag.tagId(),
                        tag.slot().name().toLowerCase(Locale.ROOT),
                        tag.priority(),
                        Math.max(tag.tagRevision(), service.registryRevision())));
            }
        }
        return new AuthorizedTagProjection(
                service.registryRevision(),
                List.copyOf(entries.values()),
                List.copyOf(assignments));
    }

    public static void logout(UUID playerId) {
        synchronized (PANELS) {
            PANELS.remove(playerId);
        }
        CONTROL_CONFIRMATIONS.remove(playerId);
        synchronized (PLAYER_REVISIONS) {
            PLAYER_REVISIONS.remove(playerId);
        }
        TAG_PROJECTIONS.remove(playerId);
        TAG_DOWNLOADS.remove(playerId);
        DISGUISE_PROJECTIONS.remove(playerId);
    }

    public static void trackPlayer(ServerPlayer player) {
        synchronized (PLAYER_REVISIONS) {
            PLAYER_REVISIONS.put(player.getUUID(), nextTargetRevision());
        }
        refreshPlayerPickers(player.server);
    }

    public static void untrackPlayer(ServerPlayer player) {
        synchronized (PLAYER_REVISIONS) {
            PLAYER_REVISIONS.remove(player.getUUID());
        }
        GuiWorkflowService.invalidate(player, "The player session ended.");
        refreshPlayerPickers(player.server);
    }

    public static void clear() {
        synchronized (PANELS) {
            PANELS.clear();
        }
        synchronized (PLAYER_REVISIONS) {
            PLAYER_REVISIONS.clear();
        }
        TAG_PROJECTIONS.clear();
        TAG_DOWNLOADS.clear();
        DISGUISE_PROJECTIONS.clear();
        CONTROL_CONFIRMATIONS.clear();
        GuiWorkflowService.clear();
    }

    public static int openPanelCount() {
        synchronized (PANELS) {
            PANELS.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(Instant.now()));
            return PANELS.size();
        }
    }

    private static void createControlRecord(ServerPlayer player, String featureId) {
        ServerControlCatalog.FeatureDefinition feature;
        try {
            feature = ServerControlCatalog.require(featureId);
        } catch (IllegalArgumentException exception) {
            player.sendSystemMessage(Component.literal("That server control feature is unavailable."));
            return;
        }
        String actionId = "sef:control." + feature.id() + ".create";
        AtomicReference<ActionResult<ServerControlRepository.ControlRecord>> created = new AtomicReference<>();
        int result = KernelCommandExecutor.execute(
                player.createCommandSourceStack(),
                actionId,
                Map.of("feature", feature.id(), "route", "gui"),
                () -> {
                    ActionResult<ServerControlRepository.ControlRecord> outcome =
                            KernelServices.serverControls().create(
                                    feature.id(),
                                    player.getUUID(),
                                    null,
                                    feature.title() + " record",
                                    "",
                                    null,
                                    Map.of("route", "gui"));
                    created.set(outcome);
                    if (!outcome.successful()) {
                        player.sendSystemMessage(Component.literal(outcome.detail()));
                        return 0;
                    }
                    return 1;
                });
        if (result > 0 && created.get() != null && created.get().successful()) {
            openControlEditor(player, created.get().value().id(), "Record created. Complete the required fields.");
        }
    }

    private static void saveControlRecord(
            ServerPlayer player,
            ServerControlRepository.ControlRecord record,
            SefPayloads.ControlMutationRequest request
    ) {
        Map<String, String> values = new LinkedHashMap<>();
        for (SefPayloads.ControlFieldValue field : request.fields()) {
            if (values.putIfAbsent(field.id(), field.value()) != null) {
                openControlEditor(player, record.id(), "Duplicate fields were rejected.");
                return;
            }
        }
        AtomicReference<ActionResult<ServerControlRepository.ControlRecord>> saved = new AtomicReference<>();
        int result = KernelCommandExecutor.execute(
                player.createCommandSourceStack(),
                "sef:control." + record.featureId() + ".manage",
                Map.of(
                        "feature", record.featureId(),
                        "record", record.id().toString(),
                        "operation", "configure_all",
                        "revision", Long.toString(record.revision()),
                        "fields", String.join(",", values.keySet())),
                () -> {
                    ActionResult<ServerControlRepository.ControlRecord> outcome =
                            KernelServices.serverControls().configureAll(
                                    record.id(),
                                    player.getUUID(),
                                    request.title(),
                                    request.details(),
                                    values,
                                    record.revision());
                    saved.set(outcome);
                    if (!outcome.successful()) {
                        player.sendSystemMessage(Component.literal(outcome.detail()));
                        return 0;
                    }
                    return 1;
                });
        CONTROL_CONFIRMATIONS.remove(player.getUUID());
        ServerControlRepository.ControlRecord current =
                KernelServices.serverControls().find(record.id()).orElse(record);
        String status = result > 0 && saved.get() != null && saved.get().successful()
                ? "Changes saved atomically at revision " + current.revision() + "."
                : saved.get() == null
                ? "The save was not completed."
                : saved.get().detail();
        openControlEditor(player, record.id(), status);
    }

    private static void previewControlRecord(
            ServerPlayer player,
            ServerControlRepository.ControlRecord record
    ) {
        ServerControlExecutionService.Preview preview =
                KernelServices.serverControlExecutions().preview(record.id(), record.revision());
        List<String> status = new ArrayList<>(preview.effects());
        if (!preview.missingFields().isEmpty()) {
            status.add("missing " + String.join(", ", preview.missingFields()));
        }
        status.add(preview.detail());
        openControlEditor(player, record.id(), String.join(". ", status));
    }

    private static void transitionControlRecord(
            ServerPlayer player,
            ServerControlRepository.ControlRecord record,
            String requestedState
    ) {
        ServerControlCatalog.FeatureDefinition feature = ServerControlCatalog.require(record.featureId());
        ServerControlRepository.RecordState state;
        try {
            state = ServerControlRepository.RecordState.valueOf(
                    requestedState.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            openControlEditor(player, record.id(), "The selected state is unavailable.");
            return;
        }
        if (!feature.states().contains(state)) {
            openControlEditor(player, record.id(), "The selected state is unavailable.");
            return;
        }
        AtomicReference<ActionResult<ServerControlRepository.ControlRecord>> transitioned =
                new AtomicReference<>();
        int result = KernelCommandExecutor.execute(
                player.createCommandSourceStack(),
                "sef:control." + record.featureId() + ".manage",
                Map.of(
                        "feature", record.featureId(),
                        "record", record.id().toString(),
                        "operation", "transition",
                        "state", state.name().toLowerCase(Locale.ROOT),
                        "revision", Long.toString(record.revision())),
                () -> {
                    ActionResult<ServerControlRepository.ControlRecord> outcome =
                            KernelServices.serverControls().transition(
                                    record.id(),
                                    player.getUUID(),
                                    state,
                                    record.revision(),
                                    "gui transition");
                    transitioned.set(outcome);
                    if (!outcome.successful()) {
                        player.sendSystemMessage(Component.literal(outcome.detail()));
                        return 0;
                    }
                    return 1;
                });
        CONTROL_CONFIRMATIONS.remove(player.getUUID());
        String status = result > 0 && transitioned.get() != null && transitioned.get().successful()
                ? "Record transitioned to " + state.name().toLowerCase(Locale.ROOT) + "."
                : transitioned.get() == null
                ? "The transition was not completed."
                : transitioned.get().detail();
        openControlEditor(player, record.id(), status);
    }

    private static void executeControlRecord(
            ServerPlayer player,
            ServerControlRepository.ControlRecord record
    ) {
        ServerControlExecutionService.Preview preview =
                KernelServices.serverControlExecutions().preview(record.id(), record.revision());
        if (!preview.ready()) {
            openControlEditor(player, record.id(), preview.detail()
                    + (preview.missingFields().isEmpty()
                    ? ""
                    : ". missing " + String.join(", ", preview.missingFields())));
            return;
        }
        boolean confirmed = !preview.confirmationRequired();
        if (preview.confirmationRequired()) {
            PendingControlConfirmation pending = currentControlConfirmation(player, record);
            ConfirmationService.Request confirmation = controlConfirmation(player, record);
            if (pending == null) {
                ActionResult<ConfirmationService.IssuedToken> issued =
                        KernelServices.confirmations().issue(confirmation, Duration.ofSeconds(60));
                if (!issued.successful()) {
                    openControlEditor(player, record.id(), "A confirmation challenge could not be issued.");
                    return;
                }
                CONTROL_CONFIRMATIONS.put(
                        player.getUUID(),
                        new PendingControlConfirmation(
                                record.id(),
                                record.revision(),
                                issued.value().token(),
                                Instant.now().plusSeconds(60)));
                openControlEditor(
                        player,
                        record.id(),
                        "Confirmation required. Review the preview, then press confirm within 60 seconds.");
                return;
            }
            ActionResult<ConfirmationService.Request> consumed =
                    KernelServices.confirmations().consume(pending.token(), confirmation);
            CONTROL_CONFIRMATIONS.remove(player.getUUID());
            if (!consumed.successful()) {
                openControlEditor(player, record.id(), "The confirmation expired or became stale.");
                return;
            }
            confirmed = true;
        }
        boolean finalConfirmed = confirmed;
        AtomicReference<ActionResult<ServerControlExecutionService.Execution>> executed =
                new AtomicReference<>();
        int result = KernelCommandExecutor.execute(
                player.createCommandSourceStack(),
                "sef:control." + record.featureId() + ".manage",
                Map.of(
                        "feature", record.featureId(),
                        "record", record.id().toString(),
                        "operation", "execute",
                        "revision", Long.toString(record.revision())),
                () -> {
                    ActionResult<ServerControlExecutionService.Execution> outcome =
                            KernelServices.serverControlExecutions().execute(
                                    record.id(),
                                    player.getUUID(),
                                    record.revision(),
                                    finalConfirmed,
                                    new ServerControlExecutionService.ExecutionContext() {
                                        @Override
                                        public Object server() {
                                            return player.server;
                                        }

                                        @Override
                                        public Object source() {
                                            return player.createCommandSourceStack();
                                        }
                                    });
                    executed.set(outcome);
                    if (!outcome.successful()) {
                        player.sendSystemMessage(Component.literal(outcome.detail()));
                        return 0;
                    }
                    return 1;
                });
        String status = result > 0 && executed.get() != null && executed.get().successful()
                ? executed.get().value().detail()
                : executed.get() == null
                ? "The execution was not completed."
                : executed.get().detail();
        openControlEditor(player, record.id(), status);
    }

    private static ConfirmationService.Request controlConfirmation(
            ServerPlayer player,
            ServerControlRepository.ControlRecord record
    ) {
        return new ConfirmationService.Request(
                player.getUUID(),
                "sef:control." + record.featureId() + ".manage",
                Map.of(
                        "record", record.id().toString(),
                        "revision", Long.toString(record.revision()),
                        "operation", "execute"),
                record.subjectId() == null ? List.of() : List.of(record.subjectId()),
                "",
                0L,
                0L,
                0L,
                KernelServices.commandPolicies().revision());
    }

    private static PendingControlConfirmation currentControlConfirmation(
            ServerPlayer player,
            ServerControlRepository.ControlRecord record
    ) {
        PendingControlConfirmation pending = CONTROL_CONFIRMATIONS.get(player.getUUID());
        if (pending == null
                || pending.expiresAt().isBefore(Instant.now())
                || !pending.recordId().equals(record.id())
                || pending.recordRevision() != record.revision()) {
            CONTROL_CONFIRMATIONS.remove(player.getUUID());
            return null;
        }
        return pending;
    }

    private static void openControlEditor(ServerPlayer player, UUID recordId, String requestedStatus) {
        SefSessionManager.SessionView session =
                SefSessionManager.instance().session(player).orElse(null);
        ServerControlRepository.ControlRecord record =
                KernelServices.serverControls().find(recordId).orElse(null);
        if (session == null
                || !session.supports(SefProtocol.Feature.CONTROL_EDITOR)
                || record == null
                || !canViewControlRecord(player, record)
                || !player.connection.hasChannel(SefPayloads.ControlEditorSnapshot.TYPE)) {
            player.sendSystemMessage(Component.literal("You cannot open that server control editor."));
            return;
        }
        ServerControlCatalog.FeatureDefinition feature =
                ServerControlCatalog.require(record.featureId());
        ServerControlSchemaRegistry.FeatureSchema schema =
                ServerControlSchemaRegistry.require(record.featureId());
        boolean manage = canManageControlRecord(player, record);
        boolean sensitiveVisible = !feature.sensitive()
                || has(player, "commands.control." + record.featureId() + ".sensitive");
        List<SefPayloads.ControlField> fields = schema.fields().stream()
                .map(field -> new SefPayloads.ControlField(
                        field.id(),
                        field.type().name().toLowerCase(Locale.ROOT),
                        field.required(),
                        field.minimum(),
                        field.maximum(),
                        sensitiveVisible
                                ? record.metadata().getOrDefault("field." + field.id(), "")
                                : "",
                        field.enumValues().stream().sorted().toList()))
                .toList();
        List<String> states = manage
                ? feature.states().stream()
                .map(state -> state.name().toLowerCase(Locale.ROOT))
                .sorted()
                .toList()
                : List.of();
        List<String> operations = new ArrayList<>();
        if (manage && schema.operations().contains(ServerControlSchemaRegistry.Operation.CONFIGURE)) {
            operations.add("configure");
        }
        if (manage && schema.operations().contains(ServerControlSchemaRegistry.Operation.PREVIEW)) {
            operations.add("preview");
        }
        if (manage && schema.operations().contains(ServerControlSchemaRegistry.Operation.EXECUTE)) {
            operations.add("execute");
        }
        long panelRevision = nextPanelRevision();
        String panelId = "control_edit:" + record.id();
        OpenPanel state = new OpenPanel(
                session.sessionId(),
                panelRevision,
                panelId,
                1,
                "",
                Instant.now().plusSeconds(ConfigHandler.config.guiPanelSessionSeconds.get()),
                Map.of());
        synchronized (PANELS) {
            PANELS.put(player.getUUID(), state);
        }
        PendingControlConfirmation pending = currentControlConfirmation(player, record);
        String status = requestedStatus == null || requestedStatus.isBlank()
                ? "Workflow " + schema.workflowId()
                + ". revision " + record.revision()
                + ". screen " + schema.screen().name().toLowerCase(Locale.ROOT) + "."
                : requestedStatus;
        PacketDistributor.sendToPlayer(player, new SefPayloads.ControlEditorSnapshot(
                session.sessionId(),
                panelRevision,
                panelId,
                record.id(),
                record.revision(),
                record.featureId(),
                record.title(),
                sensitiveVisible ? record.details() : "Sensitive details are hidden.",
                record.state().name().toLowerCase(Locale.ROOT),
                schema.screen().name().toLowerCase(Locale.ROOT),
                bounded(status, 1024),
                fields,
                states,
                operations,
                schema.confirmationRequired(),
                pending != null));
    }

    private static boolean canViewControlRecord(
            ServerPlayer player,
            ServerControlRepository.ControlRecord record
    ) {
        boolean manage = KernelCommandExecutor.canUse(
                player.createCommandSourceStack(),
                "sef:control." + record.featureId() + ".manage");
        boolean view = KernelCommandExecutor.canUse(
                player.createCommandSourceStack(),
                "sef:control." + record.featureId() + ".view");
        return manage
                || view && (record.ownerId().equals(player.getUUID())
                || player.getUUID().equals(record.subjectId()));
    }

    private static boolean canManageControlRecord(
            ServerPlayer player,
            ServerControlRepository.ControlRecord record
    ) {
        ServerControlCatalog.FeatureDefinition feature =
                ServerControlCatalog.require(record.featureId());
        if (!KernelCommandExecutor.canUse(
                player.createCommandSourceStack(),
                "sef:control." + record.featureId() + ".manage")
                || feature.sensitive()
                && !has(player, "commands.control." + record.featureId() + ".sensitive")) {
            return false;
        }
        return ServerControlCommands.mayTargetRecord(
                player.createCommandSourceStack(),
                record,
                feature);
    }

    private static void open(ServerPlayer player, String requestedPanel, int requestedPage, String query) {
        String panelId = normalizePanel(requestedPanel);
        SefSessionManager.SessionView session =
                SefSessionManager.instance().session(player).orElse(null);
        if (session == null || !allowedPanel(player, panelId, session)) {
            player.sendSystemMessage(Component.literal("You cannot open that SEF panel."));
            return;
        }
        if (panelId.startsWith("control_edit:")) {
            try {
                openControlEditor(
                        player,
                        UUID.fromString(panelId.substring("control_edit:".length())),
                        "");
            } catch (IllegalArgumentException exception) {
                player.sendSystemMessage(Component.literal("That server control record is unavailable."));
            }
            return;
        }
        SnapshotData data = build(player, panelId, query);
        int maximumEntries = Math.min(
                SefProtocol.MAXIMUM_PANEL_ENTRIES,
                ConfigHandler.config.guiMaximumPanelEntries.get());
        int preferredPageSize = KernelServices.guiPreferences()
                .preference(player.getUUID())
                .preferredPageSize();
        int pageSize = Math.max(1, Math.min(Math.min(PAGE_SIZE, preferredPageSize), maximumEntries));
        List<EntryAction> filtered = data.entries().stream()
                .filter(entry -> matches(entry.entry().title(), entry.entry().subtitle(), query))
                .toList();
        int pages = Math.max(1, (filtered.size() + pageSize - 1) / pageSize);
        int page = Math.max(1, Math.min(requestedPage, pages));
        int start = Math.min(filtered.size(), (page - 1) * pageSize);
        int end = Math.min(filtered.size(), start + pageSize);
        List<EntryAction> visible = filtered.subList(start, end);
        long revision = nextPanelRevision();
        Map<UUID, AllowedAction> actions = new LinkedHashMap<>();
        for (EntryAction entry : visible) {
            if (entry.action() != null && entry.entry().enabled()) {
                actions.put(entry.entry().entryId(), entry.action());
            }
        }
        OpenPanel state = new OpenPanel(
                session.sessionId(),
                revision,
                panelId,
                page,
                Objects.requireNonNullElse(query, ""),
                Instant.now().plusSeconds(ConfigHandler.config.guiPanelSessionSeconds.get()),
                Map.copyOf(actions));
        synchronized (PANELS) {
            PANELS.put(player.getUUID(), state);
        }
        if (player.connection.hasChannel(SefPayloads.PanelSnapshot.TYPE)) {
            PacketDistributor.sendToPlayer(player, new SefPayloads.PanelSnapshot(
                    session.sessionId(),
                    revision,
                    panelId,
                    data.view(),
                    data.title(),
                    page,
                    pages,
                    state.query(),
                    visible.stream().map(EntryAction::entry).toList(),
                    data.status()));
        }
    }

    private static SnapshotData build(ServerPlayer player, String panelId, String query) {
        return switch (panelId) {
            case DASHBOARD -> dashboard(player);
            case HOMES -> homes(player);
            case WARPS -> warps(player);
            case TELEPORT_REQUESTS -> teleportRequests(player);
            case HELP -> help(player);
            case STAFF -> staff(player);
            case PLAYERS -> players(player, false);
            case PLAYERS_ONLINE -> players(player, true);
            default -> panelId.startsWith("home:")
                    ? homeDetail(player, panelId.substring("home:".length()))
                    : panelId.startsWith("control:")
                    ? controlPanel(player, panelId.substring("control:".length()))
                    : panelId.startsWith("admin_panel:")
                    ? adminPanel(player, panelId.substring("admin_panel:".length()))
                    : catalogPanel(player, panelId);
        };
    }

    private static SnapshotData dashboard(ServerPlayer player) {
        List<EntryAction> entries = new ArrayList<>();
        addPanelLink(entries, HOMES, "Homes", "View and visit your homes", "minecraft:red_bed", player);
        addPanelLink(entries, WARPS, "Warps", "View server and player warps", "minecraft:ender_pearl", player);
        addPanelLink(
                entries,
                TELEPORT_REQUESTS,
                "Teleport requests",
                "Accept, deny, or cancel requests",
                "minecraft:compass",
                player);
        addPanelLink(entries, HELP, "Help and diagnostics", "Permission filtered commands", "minecraft:book", player);
        addPanelLink(entries, STAFF, "Staff overview", "Server and policy status", "minecraft:command_block", player);
        addPanelLink(entries, PLAYERS, "Player controls", "Vanish safe target picker", "minecraft:player_head", player);
        for (UniversalGuiCatalog.Category category : KernelServices.universalGuiCatalog().categories()) {
            addPanelLink(
                    entries,
                    category.panelId(),
                    category.title(),
                    "Permission filtered commands and controls",
                    category.iconId(),
                    player);
        }
        return new SnapshotData(
                SefPayloads.PanelView.DASHBOARD,
                "Server Essentials",
                "Every action is checked again by the server.",
                entries);
    }

    private static SnapshotData catalogPanel(ServerPlayer player, String panelId) {
        UniversalGuiCatalog.Category category =
                KernelServices.universalGuiCatalog().category(panelId).orElse(null);
        if (category == null) {
            return new SnapshotData("Server essentials", "Unknown panel.", List.of());
        }
        if (panelId.equals("category_control")) {
            return controlCatalogPanel(player, category);
        }
        List<EntryAction> entries = new ArrayList<>();
        for (UniversalGuiCatalog.ActionRoute route : KernelServices.universalGuiCatalog().actions(panelId)) {
            boolean allowed = KernelCommandExecutor.canUse(player.createCommandSourceStack(), route.actionId());
            String guiMode = KernelServices.moduleConfigs().effectiveGuiMode(
                    KernelServices.moduleConfigs().moduleForFeature(route.featureId()),
                    route.actionId());
            if (!allowed || guiMode.equals("off")) {
                continue;
            }
            String hud = route.hudDescriptorId().isBlank()
                    ? route.hudNotApplicableReason()
                    : "Active state HUD, " + route.hudDescriptorId();
            boolean workflowEnabled = !guiMode.equals("command_only")
                    && route.workflowMode() != UniversalGuiCatalog.WorkflowMode.WORLD_INTERACTION;
            ActionKind actionKind = switch (route.workflowMode()) {
                case TYPED_COMMAND, PANEL_EDITOR -> ActionKind.WORKFLOW;
                case FANCY_TAG_STUDIO -> ActionKind.FANCY_TAG_STUDIO;
                case DEDICATED_PANEL -> ActionKind.OPEN_PANEL;
                case CONTROL_EDITOR, WORLD_INTERACTION -> null;
            };
            String actionValue = switch (route.workflowMode()) {
                case TYPED_COMMAND, PANEL_EDITOR -> route.actionId();
                case FANCY_TAG_STUDIO -> fancyTagStudioSection(route.actionId());
                case DEDICATED_PANEL -> "dashboard";
                case CONTROL_EDITOR, WORLD_INTERACTION -> "";
            };
            String workflowDetail = route.workflowMode() == UniversalGuiCatalog.WorkflowMode.TYPED_COMMAND
                    ? "typed server workflow"
                    : route.workflowReason();
            SefPayloads.PanelEntry panelEntry = entry(
                    "catalog:" + route.actionId(),
                    KernelServices.configurationRevision(),
                    "workflow",
                    "/" + route.commandRoute(),
                    route.featureId() + ". " + workflowDetail + ". " + hud,
                    category.iconId(),
                    workflowEnabled && actionKind != null,
                    route.destructive());
            entries.add(new EntryAction(
                    panelEntry,
                    !workflowEnabled || actionKind == null
                            ? null
                            : new AllowedAction(
                            actionKind,
                            "workflow",
                            KernelServices.configurationRevision(),
                            actionValue,
                            current -> KernelCommandExecutor.canUse(
                                    current.createCommandSourceStack(),
                                    route.actionId())
                                    && !KernelServices.moduleConfigs().effectiveGuiMode(
                                    KernelServices.moduleConfigs().moduleForFeature(route.featureId()),
                                    route.actionId()).equals("off")
                                    && !KernelServices.moduleConfigs().effectiveGuiMode(
                                    KernelServices.moduleConfigs().moduleForFeature(route.featureId()),
                                    route.actionId()).equals("command_only"))));
        }
        if (panelId.equals("category_panels")) {
            for (AdminPanelService.PanelDefinition panel : KernelServices.adminPanels().panels()) {
                if (!has(player, panel.permissionId())) {
                    continue;
                }
                String targetPanel = "admin_panel:" + panel.id();
                SefPayloads.PanelEntry panelEntry = entry(
                        "admin_panel:" + panel.id(),
                        panel.revision(),
                        "open",
                        panel.title(),
                        panel.state().name().toLowerCase(Locale.ROOT) + ", revision " + panel.revision(),
                        "minecraft:structure_block",
                        true,
                        false);
                entries.add(new EntryAction(panelEntry, new AllowedAction(
                        ActionKind.OPEN_PANEL,
                        "open",
                        panel.revision(),
                        targetPanel,
                        current -> KernelServices.adminPanels().panel(panel.id())
                                .filter(active -> active.revision() == panel.revision())
                                .filter(active -> has(current, active.permissionId()))
                                .isPresent())));
            }
        }
        String status = entries.isEmpty()
                ? "No permitted actions are currently available. Use /" + category.fallback().route() + "."
                : "Every action preserves its canonical permission, policy, and command fallback.";
        return new SnapshotData(category.title(), status, entries);
    }

    private static String fancyTagStudioSection(String actionId) {
        String action = actionId.startsWith("sef:tags.")
                ? actionId.substring("sef:tags.".length())
                : actionId;
        if (action.startsWith("assign")) {
            return "assignments";
        }
        if (action.startsWith("revision") || action.startsWith("restore")) {
            return "revisions";
        }
        if (action.startsWith("import")) {
            return "import";
        }
        if (action.startsWith("export") || action.startsWith("transfer")) {
            return "transfer";
        }
        if (action.startsWith("cache") || action.startsWith("gc")) {
            return "cache";
        }
        if (action.startsWith("integrity")) {
            return "integrity";
        }
        if (action.startsWith("audit")) {
            return "audit";
        }
        if (action.startsWith("settings")) {
            return "settings";
        }
        return action.equals("view") ? "detail" : "manager";
    }

    private static SnapshotData controlCatalogPanel(
            ServerPlayer player,
            UniversalGuiCatalog.Category category
    ) {
        List<EntryAction> entries = new ArrayList<>();
        for (ServerControlCatalog.FeatureDefinition feature : ServerControlCatalog.FEATURES) {
            boolean visible = KernelCommandExecutor.canUse(
                    player.createCommandSourceStack(),
                    "sef:control." + feature.id() + ".view")
                    || KernelCommandExecutor.canUse(
                    player.createCommandSourceStack(),
                    "sef:control." + feature.id() + ".create")
                    || KernelCommandExecutor.canUse(
                    player.createCommandSourceStack(),
                    "sef:control." + feature.id() + ".manage");
            if (!visible) {
                continue;
            }
            String targetPanel = "control:" + feature.id();
            long records = KernelServices.serverControls().records(feature.id()).stream()
                    .filter(record -> canViewControlRecord(player, record))
                    .count();
            SefPayloads.PanelEntry panelEntry = entry(
                    "control:" + feature.id(),
                    KernelServices.serverControls().diagnostic().revision(),
                    "open",
                    feature.title(),
                    feature.category() + ", " + records + " visible records",
                    controlIcon(feature.category()),
                    true,
                    feature.dangerous());
            entries.add(new EntryAction(panelEntry, new AllowedAction(
                    ActionKind.OPEN_PANEL,
                    "open",
                    panelEntry.revision(),
                    targetPanel,
                    current -> KernelCommandExecutor.canUse(
                            current.createCommandSourceStack(),
                            "sef:control." + feature.id() + ".view")
                            || KernelCommandExecutor.canUse(
                            current.createCommandSourceStack(),
                            "sef:control." + feature.id() + ".create")
                            || KernelCommandExecutor.canUse(
                            current.createCommandSourceStack(),
                            "sef:control." + feature.id() + ".manage"))));
        }
        return new SnapshotData(
                SefPayloads.PanelView.DASHBOARD,
                category.title(),
                "Each feature opens its typed, server authoritative workflow.",
                entries);
    }

    private static SnapshotData controlPanel(ServerPlayer player, String featureId) {
        ServerControlCatalog.FeatureDefinition feature;
        try {
            feature = ServerControlCatalog.require(featureId);
        } catch (IllegalArgumentException exception) {
            return new SnapshotData("Server control", "Unknown feature.", List.of());
        }
        List<EntryAction> entries = new ArrayList<>();
        boolean create = KernelCommandExecutor.canUse(
                player.createCommandSourceStack(),
                "sef:control." + feature.id() + ".create");
        if (create) {
            SefPayloads.PanelEntry createEntry = entry(
                    "control:create:" + feature.id(),
                    KernelServices.serverControls().diagnostic().revision(),
                    "create",
                    "Create " + feature.title().toLowerCase(Locale.ROOT),
                    "Create a draft, then complete its typed fields",
                    "minecraft:writable_book",
                    true,
                    feature.dangerous());
            entries.add(new EntryAction(createEntry, new AllowedAction(
                    ActionKind.CONTROL_CREATE,
                    "create",
                    createEntry.revision(),
                    feature.id(),
                    current -> KernelCommandExecutor.canUse(
                            current.createCommandSourceStack(),
                            "sef:control." + feature.id() + ".create"))));
        }
        for (ServerControlRepository.ControlRecord record :
                KernelServices.serverControls().records(feature.id())) {
            if (!canViewControlRecord(player, record)) {
                continue;
            }
            String targetPanel = "control_edit:" + record.id();
            SefPayloads.PanelEntry recordEntry = entry(
                    "control:record:" + record.id(),
                    record.revision(),
                    "open",
                    record.title(),
                    record.state().name().toLowerCase(Locale.ROOT)
                            + ", revision " + record.revision(),
                    controlIcon(feature.category()),
                    true,
                    feature.dangerous());
            entries.add(new EntryAction(recordEntry, new AllowedAction(
                    ActionKind.OPEN_PANEL,
                    "open",
                    record.revision(),
                    targetPanel,
                    current -> KernelServices.serverControls().find(record.id())
                            .filter(active -> active.revision() == record.revision())
                            .filter(active -> canViewControlRecord(current, active))
                            .isPresent())));
        }
        ServerControlSchemaRegistry.FeatureSchema schema =
                ServerControlSchemaRegistry.require(feature.id());
        return new SnapshotData(
                switch (schema.screen()) {
                    case DASHBOARD -> SefPayloads.PanelView.DASHBOARD;
                    case PREVIEW -> SefPayloads.PanelView.DETAIL;
                    case PROGRESS -> SefPayloads.PanelView.PROGRESS;
                    default -> SefPayloads.PanelView.LIST;
                },
                feature.title(),
                "Workflow " + schema.workflowId() + ". "
                        + schema.fields().size() + " typed fields. "
                        + schema.hud().name().toLowerCase(Locale.ROOT) + " hud policy.",
                entries);
    }

    private static String controlIcon(String category) {
        return switch (category) {
            case "operations" -> "minecraft:comparator";
            case "community" -> "minecraft:writable_book";
            case "onboarding" -> "minecraft:knowledge_book";
            case "recovery" -> "minecraft:recovery_compass";
            case "governance" -> "minecraft:clock";
            case "staff" -> "minecraft:player_head";
            case "access" -> "minecraft:iron_door";
            case "world" -> "minecraft:grass_block";
            case "diagnostics" -> "minecraft:spyglass";
            case "privacy" -> "minecraft:ender_chest";
            case "market" -> "minecraft:emerald";
            case "knowledge" -> "minecraft:book";
            case "display" -> "minecraft:name_tag";
            default -> "minecraft:paper";
        };
    }

    private static SnapshotData adminPanel(ServerPlayer player, String panelId) {
        AdminPanelService.PanelDefinition panel = KernelServices.adminPanels().panel(panelId).orElse(null);
        if (panel == null || !has(player, panel.permissionId())) {
            return denied("Administrative panel");
        }
        List<EntryAction> entries = new ArrayList<>();
        for (AdminPanelService.Control control : panel.controls()) {
            AdminPanelService.Execution execution =
                    KernelServices.adminPanels().execution(panel.id(), control.id()).orElse(null);
            if (execution == null
                    || !contextAuthorized(player, execution.control())
                    || !KernelCommandExecutor.canUse(player.createCommandSourceStack(), execution.action().id())) {
                continue;
            }
            StringBuilder command = new StringBuilder(execution.action().canonicalRoute());
            execution.control().fixedArguments().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(argument -> command.append(' ').append(argument.getValue()));
            String commandRoute = command.toString();
            SefPayloads.PanelEntry panelEntry = entry(
                    "admin_control:" + panel.id() + ":" + control.id(),
                    panel.revision(),
                    control.id(),
                    "/" + execution.action().canonicalRoute(),
                    control.executionContext().name().toLowerCase(Locale.ROOT)
                            + ", " + control.audienceKind().name().toLowerCase(Locale.ROOT)
                            + ", maximum " + control.maximumTargets(),
                    "minecraft:command_block",
                    true,
                    control.destructive());
            entries.add(new EntryAction(panelEntry, new AllowedAction(
                    control.destructive() ? ActionKind.CONFIRM : ActionKind.COMMAND,
                    control.id(),
                    panel.revision(),
                    commandRoute,
                    current -> KernelServices.adminPanels().execution(panel.id(), control.id())
                            .filter(active -> active.panel().revision() == panel.revision())
                            .filter(active -> has(current, active.panel().permissionId()))
                            .filter(active -> contextAuthorized(current, active.control()))
                            .filter(active -> KernelCommandExecutor.canUse(
                                    current.createCommandSourceStack(),
                                    active.action().id()))
                            .isPresent())));
        }
        return new SnapshotData(
                panel.title(),
                "Revision " + panel.revision() + ". Typed controls are revalidated by the server.",
                entries);
    }

    private static SnapshotData homes(ServerPlayer player) {
        if (!KernelCommandExecutor.canUse(player.createCommandSourceStack(), "sef:teleport.home.list")) {
            return denied("Homes");
        }
        List<EntryAction> entries = new ArrayList<>();
        if (KernelCommandExecutor.canUse(player.createCommandSourceStack(), "sef:teleport.home.set")) {
            entries.add(new EntryAction(
                    entry(
                            "home:add",
                            1L,
                            "add",
                            "Add home",
                            "Save your current location as a new home.",
                            "minecraft:lime_bed",
                            true,
                            false),
                    new AllowedAction(
                            ActionKind.WORKFLOW,
                            "add",
                            1L,
                            "sef:teleport.home.set",
                            current -> KernelCommandExecutor.canUse(
                                    current.createCommandSourceStack(),
                                    "sef:teleport.home.set"))));
        }
        boolean canVisit =
                KernelCommandExecutor.canUse(player.createCommandSourceStack(), "sef:teleport.home.use");
        List<HomeRecord> homes = KernelServices.teleports().homes(player.getUUID());
        homes.forEach(home -> {
            SefPayloads.PanelEntry panelEntry = entry(
                    "home:" + home.id(),
                    home.revision(),
                    "open",
                    home.displayName(),
                    location(home.location().dimensionId(), home.description()),
                    home.icon().isBlank() ? "minecraft:red_bed" : home.icon(),
                    true,
                    false);
            entries.add(new EntryAction(panelEntry, new AllowedAction(
                    ActionKind.OPEN_PANEL,
                    "open",
                    home.revision(),
                    "home:" + home.id(),
                            currentPlayer -> KernelServices.teleports().homeById(home.id())
                                    .filter(HomeRecord::active)
                                    .map(current -> current.ownerId().equals(currentPlayer.getUUID())
                                            && current.revision() == home.revision()
                                            && (KernelCommandExecutor.canUse(
                                            currentPlayer.createCommandSourceStack(),
                                            "sef:teleport.home.use")
                                            || KernelCommandExecutor.canUse(
                                            currentPlayer.createCommandSourceStack(),
                                            "sef:teleport.home.delete")
                                            || KernelCommandExecutor.canUse(
                                            currentPlayer.createCommandSourceStack(),
                                            "sef:teleport.home.rename")))
                            .orElse(false))));
        });
        return new SnapshotData("Homes", homes.size() + " homes", entries);
    }

    private static SnapshotData homeDetail(ServerPlayer player, String homeId) {
        HomeRecord foundHome;
        try {
            foundHome = KernelServices.teleports().homeById(UUID.fromString(homeId)).orElse(null);
        } catch (IllegalArgumentException exception) {
            foundHome = null;
        }
        if (foundHome == null
                || !foundHome.active()
                || !foundHome.ownerId().equals(player.getUUID())) {
            return denied("Home");
        }
        HomeRecord home = foundHome;
        List<EntryAction> entries = new ArrayList<>();
        if (KernelCommandExecutor.canUse(player.createCommandSourceStack(), "sef:teleport.home.use")) {
            entries.add(new EntryAction(
                    entry(
                            "home:visit:" + home.id(),
                            home.revision(),
                            "visit",
                            "Visit",
                            location(home.location().dimensionId(), home.description()),
                            "minecraft:ender_pearl",
                            true,
                            false),
                    homeCommand(home, "visit", "home " + home.normalizedName(), "sef:teleport.home.use")));
        }
        if (KernelCommandExecutor.canUse(player.createCommandSourceStack(), "sef:teleport.home.set")) {
            entries.add(new EntryAction(
                    entry(
                            "home:update:" + home.id(),
                            home.revision(),
                            "update",
                            "Update location",
                            "Replace this home with your current position.",
                            "minecraft:compass",
                            true,
                            true),
                    new AllowedAction(
                            ActionKind.CONFIRM,
                            "update",
                            home.revision(),
                            "sethome " + home.normalizedName() + " confirm",
                            current -> homeStillValid(current, home, "sef:teleport.home.set"))));
        }
        if (KernelCommandExecutor.canUse(player.createCommandSourceStack(), "sef:teleport.home.rename")) {
            entries.add(new EntryAction(
                    entry(
                            "home:rename:" + home.id(),
                            home.revision(),
                            "rename",
                            "Rename",
                            "Open the typed rename workflow.",
                            "minecraft:name_tag",
                            true,
                            false),
                    new AllowedAction(
                            ActionKind.WORKFLOW,
                            "rename",
                            home.revision(),
                            "sef:teleport.home.rename",
                            current -> homeStillValid(current, home, "sef:teleport.home.rename"))));
        }
        if (KernelCommandExecutor.canUse(player.createCommandSourceStack(), "sef:teleport.home.delete")) {
            entries.add(new EntryAction(
                    entry(
                            "home:delete:" + home.id(),
                            home.revision(),
                            "delete",
                            "Delete",
                            "Delete this home after confirmation.",
                            "minecraft:barrier",
                            true,
                            true),
                    new AllowedAction(
                            ActionKind.CONFIRM,
                            "delete",
                            home.revision(),
                            "delhome " + home.normalizedName() + " confirm",
                            current -> homeStillValid(current, home, "sef:teleport.home.delete"))));
        }
        entries.add(panelBackEntry("home:back:" + home.id(), HOMES, "Back to homes"));
        return new SnapshotData(
                home.displayName(),
                location(home.location().dimensionId(), home.description()),
                entries);
    }

    private static AllowedAction homeCommand(
            HomeRecord home,
            String controlId,
            String command,
            String actionId
    ) {
        return new AllowedAction(
                ActionKind.COMMAND,
                controlId,
                home.revision(),
                command,
                current -> homeStillValid(current, home, actionId));
    }

    private static boolean homeStillValid(ServerPlayer player, HomeRecord expected, String actionId) {
        return KernelServices.teleports().homeById(expected.id())
                .filter(HomeRecord::active)
                .map(current -> current.ownerId().equals(player.getUUID())
                        && current.revision() == expected.revision()
                        && KernelCommandExecutor.canUse(player.createCommandSourceStack(), actionId))
                .orElse(false);
    }

    private static SnapshotData warps(ServerPlayer player) {
        List<EntryAction> entries = new ArrayList<>();
        boolean canListServerWarps =
                KernelCommandExecutor.canUse(player.createCommandSourceStack(), "sef:teleport.warp.list");
        boolean canUseServerWarp =
                KernelCommandExecutor.canUse(player.createCommandSourceStack(), "sef:teleport.warp.use");
        if (canListServerWarps) {
            boolean hidden = PermissionService.has(player, PermissionsHandler.warpHiddenView);
            String root = KernelServices.teleportSettings().ownershipMode() == TeleportSettings.OwnershipMode.COEXIST
                    ? "sefwarp"
                    : "warp";
            for (WarpRecord warp : KernelServices.teleports().serverWarps(hidden)) {
                if (warp.status() != WarpRecord.Status.ACTIVE) {
                    continue;
                }
                entries.add(warpEntry(
                        warp,
                        root + " " + warp.normalizedName(),
                        "Server warp",
                        canUseServerWarp,
                        current -> KernelServices.teleports().warpById(warp.id())
                                .filter(WarpRecord::active)
                                .map(latest -> latest.revision() == warp.revision()
                                        && latest.status() == WarpRecord.Status.ACTIVE
                                        && KernelCommandExecutor.canUse(
                                        current.createCommandSourceStack(),
                                        "sef:teleport.warp.use"))
                                .orElse(false)));
            }
        }
        boolean canListPlayerWarps =
                KernelCommandExecutor.canUse(player.createCommandSourceStack(), "sef:teleport.player_warp.list");
        boolean canUsePlayerWarp =
                KernelCommandExecutor.canUse(player.createCommandSourceStack(), "sef:teleport.player_warp.use");
        if (canListPlayerWarps) {
            boolean moderator = PermissionService.has(player, PermissionsHandler.playerWarpModerate);
            for (WarpRecord warp : KernelServices.teleports()
                    .visiblePlayerWarps(player.getUUID(), moderator)) {
                String reference = warp.ownerNameSnapshot() + ":" + warp.normalizedName();
                entries.add(warpEntry(
                        warp,
                        "pwarp " + reference,
                        warp.ownerNameSnapshot(),
                        canUsePlayerWarp,
                        current -> KernelServices.teleports().warpById(warp.id())
                                .filter(WarpRecord::active)
                                .map(latest -> latest.revision() == warp.revision()
                                        && latest.canVisit(current.getUUID(), moderator)
                                        && KernelCommandExecutor.canUse(
                                        current.createCommandSourceStack(),
                                        "sef:teleport.player_warp.use"))
                                .orElse(false)));
            }
        }
        return new SnapshotData("Warps", entries.size() + " accessible warps", List.copyOf(entries));
    }

    private static EntryAction warpEntry(
            WarpRecord warp,
            String command,
            String owner,
            boolean enabled,
            Predicate<ServerPlayer> stillValid
    ) {
        String subtitle = owner + (warp.description().isBlank() ? "" : ", " + warp.description());
        return new EntryAction(
                entry(
                        "warp:" + warp.id(),
                        warp.revision(),
                        "visit",
                        warp.displayName(),
                        subtitle,
                        warp.icon().isBlank() ? "minecraft:ender_pearl" : warp.icon(),
                        enabled,
                        false),
                enabled ? new AllowedAction(
                        ActionKind.COMMAND,
                        "visit",
                        warp.revision(),
                        command,
                        stillValid) : null);
    }

    private static SnapshotData teleportRequests(ServerPlayer player) {
        List<EntryAction> entries = new ArrayList<>();
        boolean canAccept = KernelCommandExecutor.canUse(
                player.createCommandSourceStack(),
                "sef:teleport.request.accept");
        boolean canDeny = KernelCommandExecutor.canUse(
                player.createCommandSourceStack(),
                "sef:teleport.request.deny");
        if (canAccept || canDeny) {
            for (TeleportRequestService.Request request :
                    KernelServices.teleportRequests().incoming(player.getUUID())) {
                ServerPlayer sender = player.server.getPlayerList().getPlayer(request.senderId());
                if (sender == null) {
                    continue;
                }
                if (canAccept) {
                    entries.add(requestEntry(
                            request,
                            "accept",
                            "Accept from " + sender.getGameProfile().getName(),
                            "tpaccept " + sender.getGameProfile().getName(),
                            "sef:teleport.request.accept",
                            player.getUUID(),
                            request.senderId()));
                }
                if (canDeny) {
                    entries.add(requestEntry(
                            request,
                            "deny",
                            "Deny from " + sender.getGameProfile().getName(),
                            "tpdeny " + sender.getGameProfile().getName(),
                            "sef:teleport.request.deny",
                            player.getUUID(),
                            request.senderId()));
                }
            }
        }
        if (KernelCommandExecutor.canUse(
                player.createCommandSourceStack(),
                "sef:teleport.request.cancel")) {
            for (TeleportRequestService.Request request :
                    KernelServices.teleportRequests().outgoing(player.getUUID())) {
                ServerPlayer target = player.server.getPlayerList().getPlayer(request.targetId());
                if (target == null) {
                    continue;
                }
                entries.add(requestEntry(
                        request,
                        "cancel",
                        "Cancel to " + target.getGameProfile().getName(),
                        "tpcancel " + target.getGameProfile().getName(),
                        "sef:teleport.request.cancel",
                        request.targetId(),
                        player.getUUID()));
            }
        }
        return new SnapshotData(
                "Teleport requests",
                entries.size() + " available actions",
                List.copyOf(entries));
    }

    private static EntryAction requestEntry(
            TeleportRequestService.Request request,
            String control,
            String title,
            String command,
            String actionId,
            UUID expectedTarget,
            UUID expectedSender
    ) {
        return new EntryAction(
                entry(
                        "request:" + request.id() + ":" + control,
                        request.revision(),
                        control,
                        title,
                        request.type().name().toLowerCase(Locale.ROOT),
                        "minecraft:compass",
                        true,
                        control.equals("deny") || control.equals("cancel")),
                new AllowedAction(
                        ActionKind.COMMAND,
                        control,
                        request.revision(),
                        command,
                        current -> KernelServices.teleportRequests().request(request.id())
                                .map(latest -> latest.revision() == request.revision()
                                        && latest.state() == TeleportRequestService.State.PENDING
                                        && latest.targetId().equals(expectedTarget)
                                        && latest.senderId().equals(expectedSender)
                                        && KernelCommandExecutor.canUse(
                                        current.createCommandSourceStack(),
                                        actionId))
                                .orElse(false)));
    }

    private static SnapshotData help(ServerPlayer player) {
        List<EntryAction> entries = KernelServices.catalog().entries().stream()
                .filter(definition -> definition.permissionIds().stream().allMatch(
                        id -> {
                            var node = KernelServices.permissionNode(id);
                            return node != null && PermissionService.has(player, node);
                        }))
                .sorted(Comparator.comparing(CommandDefinition::canonicalRoute))
                .map(definition -> new EntryAction(
                        entry(
                                "help:" + definition.id(),
                                KernelServices.configurationRevision(),
                                "describe",
                                "/" + definition.canonicalRoute(),
                                definition.id(),
                                "minecraft:book",
                                true,
                                false),
                        new AllowedAction(
                                ActionKind.DETAIL,
                                "describe",
                                KernelServices.configurationRevision(),
                                definition.id(),
                                current -> definition.permissionIds().stream().allMatch(
                                        permissionId -> {
                                            var node = KernelServices.permissionNode(permissionId);
                                            return node != null && PermissionService.has(current, node);
                                        }))))
                .toList();
        return new SnapshotData("Command catalog", entries.size() + " permitted actions", entries);
    }

    private static void openHelpDetail(ServerPlayer player, String actionId) {
        CommandDefinition definition = KernelServices.catalog().find(actionId).orElse(null);
        if (definition == null || definition.permissionIds().stream().anyMatch(
                permissionId -> {
                    var node = KernelServices.permissionNode(permissionId);
                    return node == null || !PermissionService.has(player, node);
                })) {
            open(player, HELP, 1, "");
            return;
        }
        List<EntryAction> entries = List.of(
                information(
                        "help:detail:route:" + definition.id(),
                        "/" + definition.canonicalRoute(),
                        definition.id(),
                        "minecraft:command_block"),
                information(
                        "help:detail:permissions:" + definition.id(),
                        "Permissions",
                        String.join(", ", definition.permissionIds()),
                        "minecraft:tripwire_hook"),
                information(
                        "help:detail:feature:" + definition.id(),
                        "Feature",
                        definition.featureId(),
                        "minecraft:comparator"),
                panelBackEntry("help:detail:back:" + definition.id(), HELP, "Back to commands"));
        publishCustom(
                player,
                HELP,
                new SnapshotData(
                        SefPayloads.PanelView.DETAIL,
                        "Command details",
                        "This page is generated from the canonical action catalog.",
                        entries));
    }

    private static SnapshotData staff(ServerPlayer player) {
        if (!PermissionService.has(player, PermissionsHandler.kernelPanel)) {
            return denied("Staff overview");
        }
        List<EntryAction> entries = List.of(
                information(
                        "staff:players",
                        "Online players",
                        Integer.toString(player.server.getPlayerCount()),
                        "minecraft:player_head"),
                information(
                        "staff:catalog",
                        "Command catalog",
                        KernelServices.catalog().size() + " actions",
                        "minecraft:command_block"),
                information(
                        "staff:storage",
                        "Storage",
                        KernelServices.storage().recoveryMode() ? "Recovery mode" : "Healthy",
                        "minecraft:chest"),
                information(
                        "staff:sessions",
                        "Enhanced clients",
                        SefSessionManager.instance().activeCount() + " active",
                        "minecraft:spyglass"));
        return new SnapshotData("Staff overview", "Read only server status", entries);
    }

    private static SnapshotData players(ServerPlayer viewer, boolean onlineOnly) {
        if (!PermissionService.has(viewer, PermissionsHandler.kernelPanel)
                || !PermissionService.has(viewer, PermissionsHandler.vanishOthersCommand)) {
            return denied("Player controls");
        }
        Map<UUID, PlayerChoice> choices = new LinkedHashMap<>();
        if (!onlineOnly) {
            for (PlayerProfileRepository.Profile profile : KernelServices.profiles().snapshot()) {
                if (profile.playerId().equals(viewer.getUUID())) {
                    continue;
                }
                String username = Objects.requireNonNullElse(
                        profile.authenticatedUsername(),
                        profile.playerId().toString());
                choices.put(profile.playerId(), new PlayerChoice(
                        profile.playerId(),
                        username,
                        profile.nickname(),
                        false,
                        false,
                        profileRevision(profile)));
            }
        }
        for (ServerPlayer target : viewer.server.getPlayerList().getPlayers()) {
            if (!visibleTarget(viewer, target)) {
                choices.remove(target.getUUID());
                continue;
            }
            PlayerProfileRepository.Profile profile =
                    KernelServices.profiles().find(target.getUUID()).orElse(null);
            choices.put(target.getUUID(), new PlayerChoice(
                    target.getUUID(),
                    target.getGameProfile().getName(),
                    profile == null ? null : profile.nickname(),
                    true,
                    VanishUtil.isVanished(target),
                    targetRevision(target.getUUID())));
        }

        List<EntryAction> entries = new ArrayList<>();
        String alternatePanel = onlineOnly ? PLAYERS : PLAYERS_ONLINE;
        entries.add(new EntryAction(
                entry(
                        "player:filter:" + alternatePanel,
                        1L,
                        "open",
                        onlineOnly ? "Show all known players" : "Show online players only",
                        onlineOnly
                                ? "Include players who have joined before."
                                : "Hide offline player profiles.",
                        onlineOnly ? "minecraft:clock" : "minecraft:ender_eye",
                        true,
                        false),
                new AllowedAction(
                        ActionKind.OPEN_PANEL,
                        "open",
                        1L,
                        alternatePanel,
                        current -> PermissionService.has(current, PermissionsHandler.kernelPanel)
                                && PermissionService.has(
                                current,
                                PermissionsHandler.vanishOthersCommand))));
        choices.values().stream()
                .sorted(Comparator.comparing(
                        choice -> choice.username().toLowerCase(Locale.ROOT)))
                .map(choice -> {
                    String status = choice.online()
                            ? choice.vanished() ? "Vanished, online" : "Online"
                            : "Offline";
                    if (choice.nickname() != null && !choice.nickname().isBlank()) {
                        status = status + ", nickname " + choice.nickname();
                    }
                    SefPayloads.PanelEntry entry = entry(
                            "player:" + choice.playerId(),
                            choice.revision(),
                            "select",
                            choice.username(),
                            status,
                            "minecraft:player_head",
                            true,
                            false);
                    return new EntryAction(entry, new AllowedAction(
                            ActionKind.SELECT_PLAYER,
                            "select",
                            choice.revision(),
                            choice.playerId().toString(),
                            current -> playerChoiceStillValid(current, choice)));
                })
                .forEach(entries::add);
        int onlineCount = (int) choices.values().stream().filter(PlayerChoice::online).count();
        return new SnapshotData(
                SefPayloads.PanelView.PICKER,
                "Player controls",
                choices.size() + " known players, " + onlineCount + " online",
                entries);
    }

    private static void openPlayerDetail(ServerPlayer viewer, String returnPanel, UUID targetId) {
        String normalizedReturn = PLAYERS_ONLINE.equals(returnPanel) ? PLAYERS_ONLINE : PLAYERS;
        ServerPlayer target = viewer.server.getPlayerList().getPlayer(targetId);
        PlayerProfileRepository.Profile profile =
                KernelServices.profiles().find(targetId).orElse(null);
        boolean online = target != null
                && targetRevision(targetId) > 0L
                && visibleTarget(viewer, target);
        if (!online && profile == null) {
            open(viewer, normalizedReturn, 1, "");
            return;
        }
        long revision = online ? targetRevision(targetId) : profileRevision(profile);
        String username = online
                ? target.getGameProfile().getName()
                : profile.authenticatedUsername();
        if (!username.matches("[A-Za-z0-9_]{1,16}")) {
            open(viewer, normalizedReturn, 1, "");
            return;
        }
        String nickname = profile == null ? null : profile.nickname();
        List<EntryAction> entries = new ArrayList<>();
        String status = online
                ? VanishUtil.isVanished(target)
                ? "Vanish level " + VanishUtil.getVanishLevel(target)
                : "Visible and online"
                : "Offline";
        if (nickname != null && !nickname.isBlank()) {
            status = status + ", nickname " + nickname;
        }
        entries.add(information(
                "player:status:" + targetId,
                username,
                status,
                "minecraft:player_head"));
        if (!online) {
            entries.add(information(
                    "player:offline:" + targetId,
                    "Offline actions",
                    "Queueable typed actions will appear here when supported.",
                    "minecraft:clock"));
            entries.add(panelBackEntry(
                    "player:back:" + targetId,
                    normalizedReturn,
                    "Back to players"));
            publishCustom(
                    viewer,
                    normalizedReturn,
                    new SnapshotData(
                            SefPayloads.PanelView.FORM,
                            "Player details",
                            "Target " + username + ", offline profile",
                            entries));
            return;
        }
        Predicate<ServerPlayer> validTarget = current -> {
            ServerPlayer latest = current.server.getPlayerList().getPlayer(targetId);
            return latest != null
                    && targetRevision(targetId) == revision
                    && visibleTarget(current, latest)
                    && PermissionService.has(current, PermissionsHandler.vanishOthersCommand);
        };
        entries.add(new EntryAction(
                entry(
                        "player:toggle:" + targetId,
                        revision,
                        "confirm",
                        VanishUtil.isVanished(target) ? "Make visible" : "Vanish",
                        "This requires confirmation and rechecks hierarchy.",
                        "minecraft:ender_eye",
                        true,
                        true),
                new AllowedAction(
                        ActionKind.CONFIRM,
                        "confirm",
                        revision,
                        "v toggle " + username,
                        validTarget)));
        entries.add(panelBackEntry(
                "player:back:" + targetId,
                normalizedReturn,
                "Back to players"));
        publishCustom(
                viewer,
                normalizedReturn,
                new SnapshotData(
                        SefPayloads.PanelView.FORM,
                        "Player details",
                        "Target " + username + ", revision " + revision,
                        entries));
    }

    private static void openConfirmation(
            ServerPlayer viewer,
            String returnPanel,
            AllowedAction pending
    ) {
        if (!pending.stillValid().test(viewer)) {
            open(viewer, returnPanel, 1, "");
            return;
        }
        List<EntryAction> entries = List.of(
                new EntryAction(
                        entry(
                                "player:confirm:" + pending.value(),
                                pending.entryRevision(),
                                "execute",
                                "Confirm player action",
                                "The server rechecks permission, hierarchy, and target revision.",
                                "minecraft:barrier",
                                true,
                                true),
                        new AllowedAction(
                                ActionKind.COMMAND,
                                "execute",
                                pending.entryRevision(),
                                pending.value(),
                                pending.stillValid())),
                panelBackEntry("confirm:cancel:" + returnPanel, returnPanel, "Cancel"));
        publishCustom(
                viewer,
                returnPanel,
                new SnapshotData(
                        SefPayloads.PanelView.CONFIRMATION,
                        "Confirm action",
                        "This action is destructive. Permission and current state are checked again.",
                        entries));
    }

    private static void openProgress(ServerPlayer viewer, String returnPanel, String status) {
        publishCustom(
                viewer,
                returnPanel,
                new SnapshotData(
                        SefPayloads.PanelView.PROGRESS,
                        "Action progress",
                        status,
                        List.of(panelBackEntry(
                                "progress:back:" + returnPanel,
                                returnPanel,
                                "Return"))));
    }

    private static EntryAction panelBackEntry(String key, String panelId, String title) {
        return new EntryAction(
                entry(key, 1L, "open", title, "", "minecraft:arrow", true, false),
                new AllowedAction(
                        ActionKind.OPEN_PANEL,
                        "open",
                        1L,
                        panelId,
                        current -> SefSessionManager.instance().session(current)
                                .map(session -> allowedPanel(current, panelId, session))
                                .orElse(false)));
    }

    private static void publishCustom(
            ServerPlayer player,
            String panelId,
            SnapshotData data
    ) {
        SefSessionManager.SessionView session =
                SefSessionManager.instance().session(player).orElse(null);
        if (session == null || !allowedPanel(player, panelId, session)) {
            return;
        }
        long revision = nextPanelRevision();
        Map<UUID, AllowedAction> actions = data.entries().stream()
                .filter(entry -> entry.action() != null && entry.entry().enabled())
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                        entry -> entry.entry().entryId(),
                        EntryAction::action));
        OpenPanel state = new OpenPanel(
                session.sessionId(),
                revision,
                panelId,
                1,
                "",
                Instant.now().plusSeconds(ConfigHandler.config.guiPanelSessionSeconds.get()),
                actions);
        synchronized (PANELS) {
            PANELS.put(player.getUUID(), state);
        }
        if (player.connection.hasChannel(SefPayloads.PanelSnapshot.TYPE)) {
            PacketDistributor.sendToPlayer(player, new SefPayloads.PanelSnapshot(
                    session.sessionId(),
                    revision,
                    panelId,
                    data.view(),
                    data.title(),
                    1,
                    1,
                    "",
                    data.entries().stream().map(EntryAction::entry).toList(),
                    data.status()));
        }
    }

    private static boolean visibleTarget(ServerPlayer viewer, ServerPlayer target) {
        return !viewer.getUUID().equals(target.getUUID())
                && targetRevision(target.getUUID()) > 0L
                && !VanishUtil.isVanished(target, viewer);
    }

    private static long targetRevision(UUID playerId) {
        return targetSessionRevision(playerId);
    }

    private static long profileRevision(PlayerProfileRepository.Profile profile) {
        return Integer.toUnsignedLong(Objects.hash(
                profile.playerId(),
                profile.authenticatedUsername(),
                profile.nickname(),
                profile.updatedAt())) + 1L;
    }

    private static boolean playerChoiceStillValid(ServerPlayer viewer, PlayerChoice choice) {
        ServerPlayer target = viewer.server.getPlayerList().getPlayer(choice.playerId());
        if (target != null) {
            return choice.online()
                    && targetRevision(choice.playerId()) == choice.revision()
                    && visibleTarget(viewer, target);
        }
        return !choice.online()
                && KernelServices.profiles().find(choice.playerId())
                .map(profile -> profileRevision(profile) == choice.revision())
                .orElse(false);
    }

    public static long targetSessionRevision(UUID playerId) {
        synchronized (PLAYER_REVISIONS) {
            return PLAYER_REVISIONS.getOrDefault(playerId, 0L);
        }
    }

    private static void refreshPlayerPickers(net.minecraft.server.MinecraftServer server) {
        List<UUID> viewers;
        synchronized (PANELS) {
            viewers = PANELS.entrySet().stream()
                    .filter(entry -> entry.getValue().panelId().equals(PLAYERS)
                            || entry.getValue().panelId().equals(PLAYERS_ONLINE))
                    .map(Map.Entry::getKey)
                    .toList();
        }
        for (UUID viewerId : viewers) {
            ServerPlayer viewer = server.getPlayerList().getPlayer(viewerId);
            if (viewer != null) {
                String panelId;
                synchronized (PANELS) {
                    OpenPanel panel = PANELS.get(viewerId);
                    panelId = panel == null ? PLAYERS : panel.panelId();
                }
                open(viewer, panelId, 1, "");
            }
        }
    }

    public static void refreshAdminPanel(
            net.minecraft.server.MinecraftServer server,
            String panelDefinitionId
    ) {
        String panelId = "admin_panel:" + panelDefinitionId;
        List<UUID> viewers;
        synchronized (PANELS) {
            viewers = PANELS.entrySet().stream()
                    .filter(entry -> entry.getValue().panelId().equals(panelId))
                    .map(Map.Entry::getKey)
                    .toList();
        }
        for (UUID viewerId : viewers) {
            ServerPlayer viewer = server.getPlayerList().getPlayer(viewerId);
            if (viewer != null) {
                open(viewer, panelId, 1, "");
            }
        }
    }

    private static EntryAction information(String id, String title, String subtitle, String icon) {
        return new EntryAction(entry(id, 1L, "status", title, subtitle, icon, false, false), null);
    }

    private static SnapshotData denied(String title) {
        return new SnapshotData(title, "No permitted actions are available.", List.of());
    }

    private static void addPanelLink(
            List<EntryAction> entries,
            String panelId,
            String title,
            String subtitle,
            String icon,
            ServerPlayer player
    ) {
        SefSessionManager.SessionView session =
                SefSessionManager.instance().session(player).orElse(null);
        if (session == null || !allowedPanel(player, panelId, session)) {
            return;
        }
        SefPayloads.PanelEntry entry =
                entry("panel:" + panelId, 1L, "open", title, subtitle, icon, true, false);
        entries.add(new EntryAction(entry, new AllowedAction(
                ActionKind.OPEN_PANEL,
                "open",
                1L,
                panelId,
                current -> SefSessionManager.instance().session(current)
                        .map(active -> allowedPanel(current, panelId, active))
                        .orElse(false))));
    }

    private static boolean allowedPanel(
            ServerPlayer player,
            String panelId,
            SefSessionManager.SessionView session
    ) {
        SefProtocol.Feature feature = feature(panelId);
        if (feature == null || !session.supports(feature)) {
            return false;
        }
        return switch (panelId) {
            case DASHBOARD -> true;
            case HOMES -> KernelCommandExecutor.canUse(
                    player.createCommandSourceStack(),
                    "sef:teleport.home.list");
            case WARPS -> KernelCommandExecutor.canUse(
                    player.createCommandSourceStack(),
                    "sef:teleport.warp.list")
                    || KernelCommandExecutor.canUse(
                    player.createCommandSourceStack(),
                    "sef:teleport.player_warp.list");
            case TELEPORT_REQUESTS -> List.of(
                            "sef:teleport.request.accept",
                            "sef:teleport.request.deny",
                            "sef:teleport.request.cancel")
                    .stream()
                    .anyMatch(action -> KernelCommandExecutor.canUse(
                            player.createCommandSourceStack(),
                            action));
            case HELP -> PermissionService.has(player, PermissionsHandler.sefCommandsCatalog);
            case STAFF -> PermissionService.has(player, PermissionsHandler.kernelPanel);
            case PLAYERS, PLAYERS_ONLINE -> PermissionService.has(
                    player,
                    PermissionsHandler.kernelPanel)
                    && PermissionService.has(player, PermissionsHandler.vanishOthersCommand);
            default -> panelId.startsWith("home:")
                    ? homePanelAllowed(player, panelId)
                    : panelId.startsWith("control_edit:")
                    ? controlRecordAllowed(player, panelId)
                    : panelId.startsWith("control:")
                    ? controlFeatureAllowed(player, panelId.substring("control:".length()))
                    : panelId.startsWith("admin_panel:")
                    ? PermissionService.has(player, PermissionsHandler.kernelPanel)
                    && KernelServices.adminPanels()
                    .panel(panelId.substring("admin_panel:".length()))
                    .filter(panel -> has(player, panel.permissionId()))
                    .isPresent()
                    : KernelServices.universalGuiCatalog().category(panelId)
                    .map(category -> KernelServices.universalGuiCatalog().actions(panelId).stream()
                            .anyMatch(action -> KernelCommandExecutor.canUse(
                                    player.createCommandSourceStack(),
                                    action.actionId())
                                    && !KernelServices.moduleConfigs().effectiveGuiMode(
                                    KernelServices.moduleConfigs().moduleForFeature(action.featureId()),
                                    action.actionId()).equals("off"))
                            || shellPanel(panelId)
                            && PermissionService.has(player, PermissionsHandler.kernelPanel)
                            || panelId.equals("category_panels")
                            && KernelServices.adminPanels().panels().stream()
                            .anyMatch(panel -> has(player, panel.permissionId())))
                    .orElse(false);
        };
    }

    private static boolean shellPanel(String panelId) {
        return panelId.equals("category_protection")
                || panelId.equals("category_integrations")
                || panelId.equals("category_aliases")
                || panelId.equals("category_tags")
                || panelId.equals("category_identity");
    }

    private static boolean homePanelAllowed(ServerPlayer player, String panelId) {
        try {
            UUID id = UUID.fromString(panelId.substring("home:".length()));
            return KernelServices.teleports().homeById(id)
                    .filter(HomeRecord::active)
                    .map(home -> home.ownerId().equals(player.getUUID()))
                    .orElse(false);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static boolean controlRecordAllowed(ServerPlayer player, String panelId) {
        try {
            UUID id = UUID.fromString(panelId.substring("control_edit:".length()));
            return KernelServices.serverControls().find(id)
                    .filter(record -> canViewControlRecord(player, record))
                    .isPresent();
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static boolean controlFeatureAllowed(ServerPlayer player, String featureId) {
        try {
            ServerControlCatalog.FeatureDefinition feature =
                    ServerControlCatalog.require(featureId);
            return KernelCommandExecutor.canUse(
                    player.createCommandSourceStack(),
                    "sef:control." + feature.id() + ".view")
                    || KernelCommandExecutor.canUse(
                    player.createCommandSourceStack(),
                    "sef:control." + feature.id() + ".create")
                    || KernelCommandExecutor.canUse(
                    player.createCommandSourceStack(),
                    "sef:control." + feature.id() + ".manage");
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static SefProtocol.Feature feature(String panelId) {
        if (panelId == null) {
            return null;
        }
        return switch (panelId.toLowerCase(Locale.ROOT)) {
            case DASHBOARD -> SefProtocol.Feature.DASHBOARD;
            case HOMES -> SefProtocol.Feature.HOMES;
            case WARPS -> SefProtocol.Feature.WARPS;
            case TELEPORT_REQUESTS -> SefProtocol.Feature.TELEPORT_REQUESTS;
            case HELP -> SefProtocol.Feature.HELP_DIAGNOSTICS;
            case STAFF, PLAYERS, PLAYERS_ONLINE -> SefProtocol.Feature.STAFF_OVERVIEW;
            default -> panelId.startsWith("home:")
                    ? SefProtocol.Feature.HOMES
                    : panelId.startsWith("control:")
                    || panelId.startsWith("control_edit:")
                    ? SefProtocol.Feature.CONTROL_EDITOR
                    : panelId.startsWith("category_") || panelId.startsWith("admin_panel:")
                    ? SefProtocol.Feature.UNIVERSAL_GUI
                    : null;
        };
    }

    private static String normalizePanel(String panelId) {
        String normalized = Objects.requireNonNullElse(panelId, "").trim().toLowerCase(Locale.ROOT);
        return feature(normalized) == null ? DASHBOARD : normalized;
    }

    private static boolean matches(String title, String subtitle, String query) {
        String normalized = Objects.requireNonNullElse(query, "").trim().toLowerCase(Locale.ROOT);
        return normalized.isBlank()
                || title.toLowerCase(Locale.ROOT).contains(normalized)
                || subtitle.toLowerCase(Locale.ROOT).contains(normalized);
    }

    private static SefPayloads.PanelEntry entry(
            String stableKey,
            long revision,
            String control,
            String title,
            String subtitle,
            String icon,
            boolean enabled,
            boolean destructive
    ) {
        return new SefPayloads.PanelEntry(
                UUID.nameUUIDFromBytes(("sef:gui:" + stableKey).getBytes(StandardCharsets.UTF_8)),
                revision,
                control,
                bounded(title, 128),
                bounded(subtitle, 256),
                normalizeIcon(icon),
                enabled,
                destructive);
    }

    private static String normalizeIcon(String icon) {
        String value = Objects.requireNonNullElse(icon, "").trim().toLowerCase(Locale.ROOT);
        if (!value.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) {
            return "minecraft:paper";
        }
        return value;
    }

    private static String location(String dimension, String description) {
        String shortDimension = dimension.contains(":")
                ? dimension.substring(dimension.indexOf(':') + 1)
                : dimension;
        return description.isBlank() ? shortDimension : shortDimension + ", " + description;
    }

    private static String bounded(String text, int maximum) {
        String value = Objects.requireNonNullElse(text, "").trim();
        value = value.codePoints()
                .filter(codePoint -> !Character.isISOControl(codePoint))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return value.length() <= maximum ? value : value.substring(0, maximum);
    }

    private static boolean has(ServerPlayer player, String permissionId) {
        var node = KernelServices.permissionNode(permissionId);
        return node != null && PermissionService.has(player, node);
    }

    private static boolean contextAuthorized(ServerPlayer player, AdminPanelService.Control control) {
        return control.contextPermissionId().isBlank() || has(player, control.contextPermissionId());
    }

    private static long nextPanelRevision() {
        return PANEL_REVISIONS.updateAndGet(current -> current == Long.MAX_VALUE ? 1L : current + 1L);
    }

    private static long nextTargetRevision() {
        return TARGET_REVISIONS.updateAndGet(current -> current == Long.MAX_VALUE ? 1L : current + 1L);
    }

    private record SnapshotData(
            SefPayloads.PanelView view,
            String title,
            String status,
            List<EntryAction> entries
    ) {
        private SnapshotData(String title, String status, List<EntryAction> entries) {
            this(SefPayloads.PanelView.LIST, title, status, entries);
        }

        private SnapshotData {
            entries = List.copyOf(entries);
        }
    }

    private record EntryAction(SefPayloads.PanelEntry entry, AllowedAction action) {
    }

    private record PlayerChoice(
            UUID playerId,
            String username,
            String nickname,
            boolean online,
            boolean vanished,
            long revision
    ) {
    }

    private record AuthorizedTagProjection(
            long revision,
            List<SefPayloads.TagManifestEntry> entries,
            List<SefPayloads.TagAssignmentProjection> assignments
    ) {
        private AuthorizedTagProjection {
            entries = List.copyOf(entries);
            assignments = List.copyOf(assignments);
        }
    }

    private record TagDownload(UUID sessionId, byte[] content, Instant expiresAt) {
        private TagDownload {
            Objects.requireNonNull(sessionId, "sessionId");
            content = Objects.requireNonNull(content, "content");
            Objects.requireNonNull(expiresAt, "expiresAt");
        }
    }

    private record AllowedAction(
            ActionKind kind,
            String controlId,
            long entryRevision,
            String value,
            Predicate<ServerPlayer> stillValid
    ) {
    }

    private record OpenPanel(
            UUID sessionId,
            long revision,
            String panelId,
            int page,
            String query,
            Instant expiresAt,
            Map<UUID, AllowedAction> actions
    ) {
    }

    private record PendingControlConfirmation(
            UUID recordId,
            long recordRevision,
            String token,
            Instant expiresAt
    ) {
    }

    private enum ActionKind {
        OPEN_PANEL,
        SELECT_PLAYER,
        DETAIL,
        CONFIRM,
        CONTROL_CREATE,
        WORKFLOW,
        FANCY_TAG_STUDIO,
        COMMAND
    }
}
