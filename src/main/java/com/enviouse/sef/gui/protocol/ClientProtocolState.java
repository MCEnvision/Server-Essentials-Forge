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
    private static final Map<String, SefPayloads.HudTile> HUD_TILES = new LinkedHashMap<>();
    private static long hudRevision;
    private static final Map<UUID, SefPayloads.ProjectedIdentity> IDENTITIES = new LinkedHashMap<>();
    private static long identityRevision;
    private static SefPayloads.TagManifest manifest;
    private static long tagRevision;
    private static SefPayloads.TagContent pendingTagContent;

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
            HUD_TILES.clear();
            hudRevision = 0L;
            IDENTITIES.clear();
            identityRevision = 0L;
            manifest = null;
            tagRevision = 0L;
            pendingTagContent = null;
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
                || !SefProtocol.Feature.FANCY_TAGS_STATIC.present(replacement.features())) {
            manifest = null;
            pendingTagContent = null;
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
                || manifest == null
                || !manifest.hash().equals(content.hash())
                || manifest.byteLength() != content.content().length) {
            return;
        }
        pendingTagContent = content;
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

    public static synchronized void reset() {
        session = null;
        outboundSequence = 0L;
        pendingPanel = null;
        panelRevision = 0L;
        HUD_TILES.clear();
        hudRevision = 0L;
        IDENTITIES.clear();
        identityRevision = 0L;
        manifest = null;
        tagRevision = 0L;
        pendingTagContent = null;
    }

    private static boolean matches(UUID sessionId) {
        return session != null && session.sessionId().equals(sessionId);
    }

    private static boolean supports(SefProtocol.Feature feature) {
        return session != null
                && session.enhancedAuthorized()
                && feature.present(session.features());
    }

    private static SefProtocol.Feature panelFeature(String panelId) {
        return switch (panelId) {
            case "dashboard" -> SefProtocol.Feature.DASHBOARD;
            case "homes" -> SefProtocol.Feature.HOMES;
            case "warps" -> SefProtocol.Feature.WARPS;
            case "teleport_requests" -> SefProtocol.Feature.TELEPORT_REQUESTS;
            case "help" -> SefProtocol.Feature.HELP_DIAGNOSTICS;
            case "staff", "players" -> SefProtocol.Feature.STAFF_OVERVIEW;
            default -> panelId.startsWith("category_") || panelId.startsWith("admin_panel:")
                    ? SefProtocol.Feature.UNIVERSAL_GUI
                    : null;
        };
    }
}
