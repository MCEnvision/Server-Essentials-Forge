package com.enviouse.sef.gui.protocol;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ClientProtocolState {
    private static SefPayloads.SessionState session;
    private static long outboundSequence;
    private static SefPayloads.PanelSnapshot pendingPanel;
    private static long panelRevision;
    private static SefPayloads.ControlEditorSnapshot pendingControlEditor;
    private static long controlEditorRevision;
    private static GuiWorkflowPayloads.GuiWorkflowSnapshot pendingWorkflow;
    private static long workflowRevision;
    private static GuiWorkflowPayloads.GuiWorkflowSuggestions pendingWorkflowSuggestions;
    private static GuiWorkflowPayloads.GuiWorkflowProgress pendingWorkflowProgress;
    private static GuiWorkflowPayloads.GuiWorkflowResult pendingWorkflowResult;
    private static GuiWorkflowPayloads.GuiWorkflowInvalidate pendingWorkflowInvalidation;
    private static final Map<String, SefPayloads.HudTile> HUD_TILES = new LinkedHashMap<>();
    private static long hudRevision;
    private static final Map<UUID, SefPayloads.ProjectedIdentity> IDENTITIES = new LinkedHashMap<>();
    private static long identityRevision;
    private static SefPayloads.TagManifest manifest;
    private static long tagRevision;
    private static SefPayloads.TagContent pendingTagContent;
    private static SefPayloads.TagContentChunk pendingTagContentChunk;
    private static SefPayloads.TagOperationResult pendingTagOperation;
    private static SefPayloads.TagManagerSnapshot pendingTagManagerSnapshot;
    private static String pendingFancyTagsStudioSection;
    private static final Map<UUID, SefPayloads.TagManifestEntry> TAG_MANIFESTS = new LinkedHashMap<>();
    private static final Map<String, SefPayloads.TagAssignmentProjection> TAG_ASSIGNMENTS = new LinkedHashMap<>();
    private static final Map<UUID, SefPayloads.DisguiseProjection> DISGUISES = new LinkedHashMap<>();
    private static final java.util.Set<String> INVALIDATED_TAG_HASHES = new java.util.LinkedHashSet<>();
    private static long tagRegistryRevision;
    private static long tagAssignmentRevision;
    private static long disguiseRevision;

    private ClientProtocolState() {
    }

    public static synchronized SefPayloads.ClientHello answer(SefPayloads.ServerHello hello) {
        reset();
        boolean compatible = SefProtocol.compatible(hello.protocolMajor());
        long features = compatible ? hello.features() & SefProtocol.SERVER_FEATURES : 0L;
        return new SefPayloads.ClientHello(
                hello.negotiationId(),
                hello.nonce(),
                SefProtocol.MAJOR,
                SefProtocol.MINOR,
                features,
                compatible);
    }

    public static synchronized void accept(SefPayloads.SessionState replacement) {
        if (!SefProtocol.compatible(replacement.protocolMajor())) {
            reset();
            return;
        }
        if (session != null
                && session.sessionId().equals(replacement.sessionId())
                && replacement.revision() <= session.revision()) {
            return;
        }
        if (session == null || !session.sessionId().equals(replacement.sessionId())) {
            outboundSequence = 0L;
            pendingPanel = null;
            panelRevision = 0L;
            pendingControlEditor = null;
            controlEditorRevision = 0L;
            pendingWorkflow = null;
            workflowRevision = 0L;
            pendingWorkflowSuggestions = null;
            pendingWorkflowProgress = null;
            pendingWorkflowResult = null;
            pendingWorkflowInvalidation = null;
            HUD_TILES.clear();
            hudRevision = 0L;
            IDENTITIES.clear();
            identityRevision = 0L;
            manifest = null;
            tagRevision = 0L;
            pendingTagContent = null;
            pendingTagContentChunk = null;
            pendingTagOperation = null;
            pendingTagManagerSnapshot = null;
            pendingFancyTagsStudioSection = null;
            TAG_MANIFESTS.clear();
            TAG_ASSIGNMENTS.clear();
            DISGUISES.clear();
            INVALIDATED_TAG_HASHES.clear();
            tagRegistryRevision = 0L;
            tagAssignmentRevision = 0L;
            disguiseRevision = 0L;
        }
        session = replacement;
        if (!replacement.enhancedAuthorized()
                || !SefProtocol.Feature.HUD.present(replacement.features())) {
            HUD_TILES.clear();
        }
        if (!replacement.enhancedAuthorized()
                || !SefProtocol.Feature.IDENTITY_PROJECTION.present(replacement.features())) {
            IDENTITIES.clear();
        }
        if (!replacement.enhancedAuthorized()
                || !SefProtocol.Feature.DASHBOARD.present(replacement.features())) {
            pendingPanel = null;
        }
        if (!replacement.enhancedAuthorized()
                || !SefProtocol.Feature.CONTROL_EDITOR.present(replacement.features())) {
            pendingControlEditor = null;
        }
        if (!replacement.enhancedAuthorized()
                || !SefProtocol.Feature.GUI_WORKFLOW.present(replacement.features())) {
            clearWorkflow();
        }
        if (!replacement.enhancedAuthorized()
                || !SefProtocol.Feature.FANCY_TAGS_STATIC.present(replacement.features())) {
            manifest = null;
            pendingTagContent = null;
            pendingTagContentChunk = null;
            pendingTagOperation = null;
            pendingTagManagerSnapshot = null;
            pendingFancyTagsStudioSection = null;
            TAG_MANIFESTS.clear();
            TAG_ASSIGNMENTS.clear();
            INVALIDATED_TAG_HASHES.clear();
        }
        if (!replacement.enhancedAuthorized()
                || !SefProtocol.Feature.DISGUISE_PROJECTION.present(replacement.features())) {
            DISGUISES.clear();
        }
    }

    public static synchronized void accept(SefPayloads.PanelSnapshot snapshot) {
        SefProtocol.Feature feature = panelFeature(snapshot.panelId());
        if (feature == null
                || !supports(feature)
                || !matches(snapshot.sessionId())
                || snapshot.panelRevision() <= panelRevision) {
            return;
        }
        panelRevision = snapshot.panelRevision();
        pendingPanel = snapshot;
    }

    public static synchronized void accept(SefPayloads.ControlEditorSnapshot snapshot) {
        if (!supports(SefProtocol.Feature.CONTROL_EDITOR)
                || !matches(snapshot.sessionId())
                || snapshot.panelRevision() <= controlEditorRevision) {
            return;
        }
        controlEditorRevision = snapshot.panelRevision();
        pendingControlEditor = snapshot;
    }

    public static synchronized void accept(GuiWorkflowPayloads.GuiWorkflowSnapshot snapshot) {
        if (!supports(SefProtocol.Feature.GUI_WORKFLOW)
                || !matches(snapshot.sessionId())
                || snapshot.revision() <= workflowRevision) {
            return;
        }
        workflowRevision = snapshot.revision();
        pendingWorkflow = snapshot;
        pendingWorkflowInvalidation = null;
    }

    public static synchronized void accept(GuiWorkflowPayloads.GuiWorkflowSuggestions suggestions) {
        if (!supports(SefProtocol.Feature.GUI_WORKFLOW)
                || !matches(suggestions.sessionId())
                || suggestions.revision() != workflowRevision) {
            return;
        }
        pendingWorkflowSuggestions = suggestions;
    }

    public static synchronized void accept(GuiWorkflowPayloads.GuiWorkflowProgress progress) {
        if (!supports(SefProtocol.Feature.GUI_WORKFLOW)
                || !matches(progress.sessionId())
                || progress.revision() < workflowRevision) {
            return;
        }
        workflowRevision = progress.revision();
        pendingWorkflowProgress = progress;
    }

    public static synchronized void accept(GuiWorkflowPayloads.GuiWorkflowResult result) {
        if (!supports(SefProtocol.Feature.GUI_WORKFLOW)
                || !matches(result.sessionId())
                || result.revision() < workflowRevision) {
            return;
        }
        workflowRevision = result.revision();
        pendingWorkflowResult = result;
    }

    public static synchronized void accept(GuiWorkflowPayloads.GuiWorkflowInvalidate invalidation) {
        if (!supports(SefProtocol.Feature.GUI_WORKFLOW)
                || !matches(invalidation.sessionId())
                || invalidation.revision() < workflowRevision) {
            return;
        }
        workflowRevision = invalidation.revision();
        pendingWorkflow = null;
        pendingWorkflowSuggestions = null;
        pendingWorkflowProgress = null;
        pendingWorkflowResult = null;
        pendingWorkflowInvalidation = invalidation;
    }

    public static synchronized void accept(SefPayloads.HudDelta delta) {
        if (!supports(SefProtocol.Feature.HUD)
                || !matches(delta.sessionId())
                || delta.revision() <= hudRevision) {
            return;
        }
        hudRevision = delta.revision();
        if (delta.reset()) {
            HUD_TILES.clear();
        }
        delta.removedIds().forEach(HUD_TILES::remove);
        for (SefPayloads.HudTile tile : delta.tiles()) {
            HUD_TILES.put(tile.id(), tile);
        }
    }

    public static synchronized void accept(SefPayloads.TagManifest replacement) {
        if (!supports(SefProtocol.Feature.FANCY_TAGS_STATIC)
                || !matches(replacement.sessionId())
                || replacement.revision() <= tagRevision) {
            return;
        }
        tagRevision = replacement.revision();
        manifest = replacement;
        pendingTagContent = null;
    }

    public static synchronized void accept(SefPayloads.IdentityProjection projection) {
        if (!supports(SefProtocol.Feature.IDENTITY_PROJECTION)
                || !matches(projection.sessionId())
                || projection.revision() <= identityRevision) {
            return;
        }
        identityRevision = projection.revision();
        if (projection.reset()) {
            IDENTITIES.clear();
        }
        projection.removedPlayerIds().forEach(IDENTITIES::remove);
        for (SefPayloads.ProjectedIdentity identity : projection.identities()) {
            SefPayloads.ProjectedIdentity current = IDENTITIES.get(identity.playerId());
            if (current == null || identity.revision() > current.revision()) {
                IDENTITIES.put(identity.playerId(), identity);
            }
        }
    }

    public static synchronized void accept(SefPayloads.TagContent content) {
        if (!supports(SefProtocol.Feature.FANCY_TAGS_STATIC)
                || !matches(content.sessionId())
                || TAG_MANIFESTS.values().stream().noneMatch(entry ->
                entry.hash().equals(content.hash()) && entry.byteLength() == content.content().length)
                && (manifest == null
                || !manifest.hash().equals(content.hash())
                || manifest.byteLength() != content.content().length)) {
            return;
        }
        pendingTagContent = content;
    }

    public static synchronized void accept(SefPayloads.TagContentChunk chunk) {
        if (!supports(SefProtocol.Feature.FANCY_TAGS_STATIC)
                || !matches(chunk.sessionId())
                || TAG_MANIFESTS.values().stream().noneMatch(entry ->
                entry.hash().equals(chunk.hash()) && entry.byteLength() == chunk.totalBytes())
                && (manifest == null
                || !manifest.hash().equals(chunk.hash())
                || manifest.byteLength() != chunk.totalBytes())) {
            return;
        }
        pendingTagContentChunk = chunk;
    }

    public static synchronized void accept(SefPayloads.TagOperationResult result) {
        if (!supports(SefProtocol.Feature.FANCY_TAGS_MANAGER)
                || !matches(result.sessionId())) {
            return;
        }
        pendingTagOperation = result;
    }

    public static synchronized void accept(SefPayloads.TagRegistrySnapshot snapshot) {
        if (!supports(SefProtocol.Feature.FANCY_TAGS_REGISTRY)
                || !matches(snapshot.sessionId())
                || snapshot.revision() <= tagRegistryRevision) {
            return;
        }
        tagRegistryRevision = snapshot.revision();
        if (snapshot.reset()) {
            TAG_MANIFESTS.clear();
        }
        for (SefPayloads.TagManifestEntry entry : snapshot.entries()) {
            SefPayloads.TagManifestEntry current = TAG_MANIFESTS.get(entry.tagId());
            if (current == null || entry.tagRevision() > current.tagRevision()) {
                TAG_MANIFESTS.put(entry.tagId(), entry);
            }
        }
        TAG_MANIFESTS.values().removeIf(entry -> INVALIDATED_TAG_HASHES.contains(entry.hash()));
    }

    public static synchronized void accept(SefPayloads.TagRegistryDelta delta) {
        if (!supports(SefProtocol.Feature.FANCY_TAGS_REGISTRY)
                || !matches(delta.sessionId())
                || delta.revision() <= tagRegistryRevision) {
            return;
        }
        tagRegistryRevision = delta.revision();
        delta.removedTagIds().forEach(TAG_MANIFESTS::remove);
        for (SefPayloads.TagManifestEntry entry : delta.entries()) {
            SefPayloads.TagManifestEntry current = TAG_MANIFESTS.get(entry.tagId());
            if (current == null || entry.tagRevision() > current.tagRevision()) {
                TAG_MANIFESTS.put(entry.tagId(), entry);
            }
        }
        TAG_MANIFESTS.values().removeIf(entry -> INVALIDATED_TAG_HASHES.contains(entry.hash()));
        TAG_ASSIGNMENTS.values().removeIf(assignment -> !TAG_MANIFESTS.containsKey(assignment.tagId()));
    }

    public static synchronized void accept(SefPayloads.TagAssignmentSnapshot snapshot) {
        if (!supports(SefProtocol.Feature.FANCY_TAGS_REGISTRY)
                || !matches(snapshot.sessionId())
                || snapshot.revision() <= tagAssignmentRevision) {
            return;
        }
        tagAssignmentRevision = snapshot.revision();
        if (snapshot.reset()) {
            TAG_ASSIGNMENTS.clear();
        }
        for (SefPayloads.TagAssignmentProjection assignment : snapshot.assignments()) {
            String key = tagAssignmentKey(
                    assignment.subjectId(),
                    assignment.tagId(),
                    assignment.slot());
            SefPayloads.TagAssignmentProjection current = TAG_ASSIGNMENTS.get(key);
            if (current == null || assignment.presentationRevision() > current.presentationRevision()) {
                TAG_ASSIGNMENTS.put(key, assignment);
            }
        }
    }

    public static synchronized void accept(SefPayloads.TagAssignmentDelta delta) {
        if (!supports(SefProtocol.Feature.FANCY_TAGS_REGISTRY)
                || !matches(delta.sessionId())
                || delta.revision() <= tagAssignmentRevision) {
            return;
        }
        tagAssignmentRevision = delta.revision();
        for (SefPayloads.TagAssignmentKey key : delta.removedAssignments()) {
            TAG_ASSIGNMENTS.remove(tagAssignmentKey(key.subjectId(), key.tagId(), key.slot()));
        }
        for (SefPayloads.TagAssignmentProjection assignment : delta.assignments()) {
            String key = tagAssignmentKey(
                    assignment.subjectId(),
                    assignment.tagId(),
                    assignment.slot());
            SefPayloads.TagAssignmentProjection current = TAG_ASSIGNMENTS.get(key);
            if (current == null || assignment.presentationRevision() > current.presentationRevision()) {
                TAG_ASSIGNMENTS.put(key, assignment);
            }
        }
    }

    public static synchronized void accept(SefPayloads.TagManagerSnapshot snapshot) {
        if (!supports(SefProtocol.Feature.FANCY_TAGS_MANAGER)
                || !matches(snapshot.sessionId())) {
            return;
        }
        pendingTagManagerSnapshot = snapshot;
    }

    public static synchronized void accept(SefPayloads.OpenFancyTagsStudio request) {
        if (!supports(SefProtocol.Feature.FANCY_TAGS_STATIC)
                || !matches(request.sessionId())) {
            return;
        }
        pendingFancyTagsStudioSection = request.section();
    }

    public static synchronized void accept(SefPayloads.TagCacheInvalidation invalidation) {
        if (!supports(SefProtocol.Feature.FANCY_TAGS_REGISTRY)
                || !matches(invalidation.sessionId())
                || invalidation.revision() < tagRegistryRevision) {
            return;
        }
        INVALIDATED_TAG_HASHES.addAll(invalidation.hashes());
        TAG_MANIFESTS.values().removeIf(entry -> INVALIDATED_TAG_HASHES.contains(entry.hash()));
        TAG_ASSIGNMENTS.values().removeIf(assignment -> !TAG_MANIFESTS.containsKey(assignment.tagId()));
    }

    public static synchronized void accept(SefPayloads.DisguiseSnapshot snapshot) {
        if (!supports(SefProtocol.Feature.DISGUISE_PROJECTION)
                || !matches(snapshot.sessionId())
                || snapshot.revision() <= disguiseRevision) {
            return;
        }
        disguiseRevision = snapshot.revision();
        if (snapshot.reset()) {
            DISGUISES.clear();
        }
        for (SefPayloads.DisguiseProjection projection : snapshot.projections()) {
            SefPayloads.DisguiseProjection current = DISGUISES.get(projection.subjectId());
            if (current == null || projection.disguiseRevision() > current.disguiseRevision()) {
                DISGUISES.put(projection.subjectId(), projection);
            }
        }
    }

    public static synchronized void accept(SefPayloads.DisguiseDelta delta) {
        if (!supports(SefProtocol.Feature.DISGUISE_PROJECTION)
                || !matches(delta.sessionId())
                || delta.revision() <= disguiseRevision) {
            return;
        }
        disguiseRevision = delta.revision();
        delta.removedSubjectIds().forEach(DISGUISES::remove);
        for (SefPayloads.DisguiseProjection projection : delta.projections()) {
            SefPayloads.DisguiseProjection current = DISGUISES.get(projection.subjectId());
            if (current == null || projection.disguiseRevision() > current.disguiseRevision()) {
                DISGUISES.put(projection.subjectId(), projection);
            }
        }
    }

    public static synchronized Optional<SefPayloads.SessionState> session() {
        return Optional.ofNullable(session);
    }

    public static synchronized boolean negotiated(SefProtocol.Feature feature) {
        return session != null
                && session.enhancedAuthorized()
                && feature.present(session.features());
    }

    public static synchronized long nextSequence() {
        if (session == null || outboundSequence == Long.MAX_VALUE) {
            return -1L;
        }
        return ++outboundSequence;
    }

    public static synchronized Optional<SefPayloads.PanelSnapshot> takePanel() {
        SefPayloads.PanelSnapshot result = pendingPanel;
        pendingPanel = null;
        return Optional.ofNullable(result);
    }

    public static synchronized Optional<SefPayloads.ControlEditorSnapshot> takeControlEditor() {
        SefPayloads.ControlEditorSnapshot result = pendingControlEditor;
        pendingControlEditor = null;
        return Optional.ofNullable(result);
    }

    public static synchronized Optional<GuiWorkflowPayloads.GuiWorkflowSnapshot> takeWorkflow() {
        GuiWorkflowPayloads.GuiWorkflowSnapshot result = pendingWorkflow;
        pendingWorkflow = null;
        return Optional.ofNullable(result);
    }

    public static synchronized Optional<GuiWorkflowPayloads.GuiWorkflowSuggestions> takeWorkflowSuggestions() {
        GuiWorkflowPayloads.GuiWorkflowSuggestions result = pendingWorkflowSuggestions;
        pendingWorkflowSuggestions = null;
        return Optional.ofNullable(result);
    }

    public static synchronized Optional<GuiWorkflowPayloads.GuiWorkflowProgress> takeWorkflowProgress() {
        GuiWorkflowPayloads.GuiWorkflowProgress result = pendingWorkflowProgress;
        pendingWorkflowProgress = null;
        return Optional.ofNullable(result);
    }

    public static synchronized Optional<GuiWorkflowPayloads.GuiWorkflowResult> takeWorkflowResult() {
        GuiWorkflowPayloads.GuiWorkflowResult result = pendingWorkflowResult;
        pendingWorkflowResult = null;
        return Optional.ofNullable(result);
    }

    public static synchronized Optional<GuiWorkflowPayloads.GuiWorkflowInvalidate> takeWorkflowInvalidation() {
        GuiWorkflowPayloads.GuiWorkflowInvalidate result = pendingWorkflowInvalidation;
        pendingWorkflowInvalidation = null;
        return Optional.ofNullable(result);
    }

    public static synchronized List<SefPayloads.HudTile> hudTiles() {
        return List.copyOf(HUD_TILES.values());
    }

    public static synchronized Optional<SefPayloads.ProjectedIdentity> identity(UUID playerId) {
        return Optional.ofNullable(IDENTITIES.get(playerId));
    }

    public static synchronized Optional<SefPayloads.TagManifest> manifest() {
        return Optional.ofNullable(manifest);
    }

    public static synchronized Optional<SefPayloads.TagContent> takeTagContent() {
        SefPayloads.TagContent result = pendingTagContent;
        pendingTagContent = null;
        return Optional.ofNullable(result);
    }

    public static synchronized Optional<SefPayloads.TagContentChunk> takeTagContentChunk() {
        SefPayloads.TagContentChunk result = pendingTagContentChunk;
        pendingTagContentChunk = null;
        return Optional.ofNullable(result);
    }

    public static synchronized Optional<SefPayloads.TagOperationResult> takeTagOperationResult() {
        SefPayloads.TagOperationResult result = pendingTagOperation;
        pendingTagOperation = null;
        return Optional.ofNullable(result);
    }

    public static synchronized Optional<SefPayloads.TagManagerSnapshot> takeTagManagerSnapshot() {
        SefPayloads.TagManagerSnapshot result = pendingTagManagerSnapshot;
        pendingTagManagerSnapshot = null;
        return Optional.ofNullable(result);
    }

    public static synchronized Optional<String> takeFancyTagsStudioSection() {
        String result = pendingFancyTagsStudioSection;
        pendingFancyTagsStudioSection = null;
        return Optional.ofNullable(result);
    }

    public static synchronized List<SefPayloads.TagManifestEntry> tagManifests() {
        return List.copyOf(TAG_MANIFESTS.values());
    }

    public static synchronized List<SefPayloads.TagAssignmentProjection> tagAssignments() {
        return List.copyOf(TAG_ASSIGNMENTS.values());
    }

    public static synchronized Optional<SefPayloads.DisguiseProjection> disguise(UUID subjectId) {
        return Optional.ofNullable(DISGUISES.get(subjectId));
    }

    public static synchronized java.util.Set<String> takeTagInvalidations() {
        java.util.Set<String> result = java.util.Set.copyOf(INVALIDATED_TAG_HASHES);
        INVALIDATED_TAG_HASHES.clear();
        return result;
    }

    public static synchronized void reset() {
        session = null;
        outboundSequence = 0L;
        pendingPanel = null;
        panelRevision = 0L;
        pendingControlEditor = null;
        controlEditorRevision = 0L;
        clearWorkflow();
        HUD_TILES.clear();
        hudRevision = 0L;
        IDENTITIES.clear();
        identityRevision = 0L;
        manifest = null;
        tagRevision = 0L;
        pendingTagContent = null;
        pendingTagContentChunk = null;
        pendingTagOperation = null;
        pendingTagManagerSnapshot = null;
        pendingFancyTagsStudioSection = null;
        TAG_MANIFESTS.clear();
        TAG_ASSIGNMENTS.clear();
        DISGUISES.clear();
        INVALIDATED_TAG_HASHES.clear();
        tagRegistryRevision = 0L;
        tagAssignmentRevision = 0L;
        disguiseRevision = 0L;
    }

    private static void clearWorkflow() {
        pendingWorkflow = null;
        workflowRevision = 0L;
        pendingWorkflowSuggestions = null;
        pendingWorkflowProgress = null;
        pendingWorkflowResult = null;
        pendingWorkflowInvalidation = null;
    }

    private static boolean matches(UUID sessionId) {
        return session != null && session.sessionId().equals(sessionId);
    }

    private static boolean supports(SefProtocol.Feature feature) {
        return session != null
                && session.enhancedAuthorized()
                && feature.present(session.features());
    }

    private static String tagAssignmentKey(UUID subjectId, UUID tagId, String slot) {
        return subjectId + ":" + tagId + ":" + slot;
    }

    private static SefProtocol.Feature panelFeature(String panelId) {
        return switch (panelId) {
            case "dashboard" -> SefProtocol.Feature.DASHBOARD;
            case "homes" -> SefProtocol.Feature.HOMES;
            case "warps" -> SefProtocol.Feature.WARPS;
            case "teleport_requests" -> SefProtocol.Feature.TELEPORT_REQUESTS;
            case "help" -> SefProtocol.Feature.HELP_DIAGNOSTICS;
            case "staff", "players" -> SefProtocol.Feature.STAFF_OVERVIEW;
            default -> panelId.startsWith("control:")
                    || panelId.startsWith("control_edit:")
                    ? SefProtocol.Feature.CONTROL_EDITOR
                    : panelId.startsWith("category_") || panelId.startsWith("admin_panel:")
                    ? SefProtocol.Feature.UNIVERSAL_GUI
                    : null;
        };
    }
}
