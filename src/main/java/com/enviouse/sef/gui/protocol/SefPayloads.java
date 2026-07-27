package com.enviouse.sef.gui.protocol;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class SefPayloads {
    private SefPayloads() {
    }

    private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> type(String path) {
        return new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("sef", path));
    }

    public record ServerHello(
            UUID negotiationId,
            long nonce,
            int protocolMajor,
            int protocolMinor,
            long features
    ) implements CustomPacketPayload {
        public static final Type<ServerHello> TYPE = SefPayloads.type("configuration/server_hello");
        public static final StreamCodec<FriendlyByteBuf, ServerHello> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.negotiationId());
                    buffer.writeLong(value.nonce());
                    buffer.writeVarInt(value.protocolMajor());
                    buffer.writeVarInt(value.protocolMinor());
                    buffer.writeLong(value.features());
                },
                buffer -> new ServerHello(
                        buffer.readUUID(),
                        buffer.readLong(),
                        buffer.readVarInt(),
                        buffer.readVarInt(),
                        buffer.readLong()));

        public ServerHello {
            Objects.requireNonNull(negotiationId, "negotiationId");
            if (protocolMajor < 0 || protocolMajor > 1000
                    || protocolMinor < 0 || protocolMinor > 1000) {
                throw new IllegalArgumentException("Protocol version is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ClientHello(
            UUID negotiationId,
            long nonce,
            int protocolMajor,
            int protocolMinor,
            long features,
            boolean compatible
    ) implements CustomPacketPayload {
        public static final Type<ClientHello> TYPE = SefPayloads.type("configuration/client_hello");
        public static final StreamCodec<FriendlyByteBuf, ClientHello> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.negotiationId());
                    buffer.writeLong(value.nonce());
                    buffer.writeVarInt(value.protocolMajor());
                    buffer.writeVarInt(value.protocolMinor());
                    buffer.writeLong(value.features());
                    buffer.writeBoolean(value.compatible());
                },
                buffer -> new ClientHello(
                        buffer.readUUID(),
                        buffer.readLong(),
                        buffer.readVarInt(),
                        buffer.readVarInt(),
                        buffer.readLong(),
                        buffer.readBoolean()));

        public ClientHello {
            Objects.requireNonNull(negotiationId, "negotiationId");
            if (protocolMajor < 0 || protocolMajor > 1000
                    || protocolMinor < 0 || protocolMinor > 1000) {
                throw new IllegalArgumentException("Protocol version is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SessionState(
            UUID sessionId,
            long revision,
            int protocolMajor,
            int protocolMinor,
            long features,
            boolean enhancedAuthorized
    ) implements CustomPacketPayload {
        public static final Type<SessionState> TYPE = SefPayloads.type("play/session_state");
        public static final StreamCodec<FriendlyByteBuf, SessionState> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.revision());
                    buffer.writeVarInt(value.protocolMajor());
                    buffer.writeVarInt(value.protocolMinor());
                    buffer.writeLong(value.features());
                    buffer.writeBoolean(value.enhancedAuthorized());
                },
                buffer -> new SessionState(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        buffer.readVarInt(),
                        buffer.readVarInt(),
                        buffer.readLong(),
                        buffer.readBoolean()));

        public SessionState {
            Objects.requireNonNull(sessionId, "sessionId");
            if (revision < 1L || protocolMajor < 0 || protocolMinor < 0) {
                throw new IllegalArgumentException("Session state is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record OpenPanelRequest(
            UUID sessionId,
            long sequence,
            String panelId,
            int page,
            String query
    ) implements CustomPacketPayload {
        public static final Type<OpenPanelRequest> TYPE = SefPayloads.type("play/open_panel");
        public static final StreamCodec<FriendlyByteBuf, OpenPanelRequest> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.sequence());
                    PayloadCodecSupport.writeString(buffer, value.panelId(), 64);
                    buffer.writeVarInt(value.page());
                    PayloadCodecSupport.writeString(buffer, value.query(), 64);
                },
                buffer -> new OpenPanelRequest(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        PayloadCodecSupport.readString(buffer, 64),
                        buffer.readVarInt(),
                        PayloadCodecSupport.readString(buffer, 64)));

        public OpenPanelRequest {
            Objects.requireNonNull(sessionId, "sessionId");
            PayloadCodecSupport.validateText(panelId, 64);
            PayloadCodecSupport.validateText(query, 64);
            if (sequence < 1L || page < 1 || page > 10_000) {
                throw new IllegalArgumentException("Panel request is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record PanelSnapshot(
            UUID sessionId,
            long panelRevision,
            String panelId,
            PanelView view,
            String title,
            int page,
            int pages,
            String query,
            List<PanelEntry> entries,
            String status
    ) implements CustomPacketPayload {
        public static final Type<PanelSnapshot> TYPE = SefPayloads.type("play/panel_snapshot");
        public static final StreamCodec<FriendlyByteBuf, PanelSnapshot> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.panelRevision());
                    PayloadCodecSupport.writeString(buffer, value.panelId(), 64);
                    buffer.writeEnum(value.view());
                    PayloadCodecSupport.writeString(buffer, value.title(), 128);
                    buffer.writeVarInt(value.page());
                    buffer.writeVarInt(value.pages());
                    PayloadCodecSupport.writeString(buffer, value.query(), 64);
                    PayloadCodecSupport.writeList(
                            buffer,
                            value.entries(),
                            SefProtocol.MAXIMUM_PANEL_ENTRIES,
                            PanelEntry::encode);
                    PayloadCodecSupport.writeString(buffer, value.status(), 256);
                },
                buffer -> new PanelSnapshot(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        PayloadCodecSupport.readString(buffer, 64),
                        buffer.readEnum(PanelView.class),
                        PayloadCodecSupport.readString(buffer, 128),
                        buffer.readVarInt(),
                        buffer.readVarInt(),
                        PayloadCodecSupport.readString(buffer, 64),
                        PayloadCodecSupport.readList(
                                buffer,
                                SefProtocol.MAXIMUM_PANEL_ENTRIES,
                                PanelEntry::decode),
                        PayloadCodecSupport.readString(buffer, 256)));

        public PanelSnapshot {
            Objects.requireNonNull(sessionId, "sessionId");
            Objects.requireNonNull(view, "view");
            PayloadCodecSupport.validateText(panelId, 64);
            PayloadCodecSupport.validateText(title, 128);
            PayloadCodecSupport.validateText(query, 64);
            PayloadCodecSupport.validateText(status, 256);
            entries = List.copyOf(Objects.requireNonNull(entries, "entries"));
            if (panelRevision < 1L || page < 1 || pages < 1 || page > pages
                    || entries.size() > SefProtocol.MAXIMUM_PANEL_ENTRIES) {
                throw new IllegalArgumentException("Panel snapshot is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public enum PanelView {
        DASHBOARD,
        CATEGORY,
        LIST,
        DETAIL,
        FORM,
        PICKER,
        CONFIRMATION,
        PROGRESS
    }

    public record PanelEntry(
            UUID entryId,
            long revision,
            String controlId,
            String title,
            String subtitle,
            String icon,
            boolean enabled,
            boolean destructive
    ) {
        public PanelEntry {
            Objects.requireNonNull(entryId, "entryId");
            PayloadCodecSupport.validateText(controlId, 64);
            PayloadCodecSupport.validateText(title, 128);
            PayloadCodecSupport.validateText(subtitle, 256);
            PayloadCodecSupport.validateText(icon, 128);
            if (revision < 0L) {
                throw new IllegalArgumentException("Panel entry revision is invalid");
            }
        }

        private static void encode(FriendlyByteBuf buffer, PanelEntry value) {
            buffer.writeUUID(value.entryId());
            buffer.writeVarLong(value.revision());
            PayloadCodecSupport.writeString(buffer, value.controlId(), 64);
            PayloadCodecSupport.writeString(buffer, value.title(), 128);
            PayloadCodecSupport.writeString(buffer, value.subtitle(), 256);
            PayloadCodecSupport.writeString(buffer, value.icon(), 128);
            buffer.writeBoolean(value.enabled());
            buffer.writeBoolean(value.destructive());
        }

        private static PanelEntry decode(FriendlyByteBuf buffer) {
            return new PanelEntry(
                    buffer.readUUID(),
                    buffer.readVarLong(),
                    PayloadCodecSupport.readString(buffer, 64),
                    PayloadCodecSupport.readString(buffer, 128),
                    PayloadCodecSupport.readString(buffer, 256),
                    PayloadCodecSupport.readString(buffer, 128),
                    buffer.readBoolean(),
                    buffer.readBoolean());
        }
    }

    public record PanelActionRequest(
            UUID sessionId,
            long sequence,
            String panelId,
            long panelRevision,
            String controlId,
            UUID entryId,
            long entryRevision
    ) implements CustomPacketPayload {
        public static final Type<PanelActionRequest> TYPE = SefPayloads.type("play/panel_action");
        public static final StreamCodec<FriendlyByteBuf, PanelActionRequest> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.sequence());
                    PayloadCodecSupport.writeString(buffer, value.panelId(), 64);
                    buffer.writeVarLong(value.panelRevision());
                    PayloadCodecSupport.writeString(buffer, value.controlId(), 64);
                    buffer.writeUUID(value.entryId());
                    buffer.writeVarLong(value.entryRevision());
                },
                buffer -> new PanelActionRequest(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        PayloadCodecSupport.readString(buffer, 64),
                        buffer.readVarLong(),
                        PayloadCodecSupport.readString(buffer, 64),
                        buffer.readUUID(),
                        buffer.readVarLong()));

        public PanelActionRequest {
            Objects.requireNonNull(sessionId, "sessionId");
            Objects.requireNonNull(entryId, "entryId");
            PayloadCodecSupport.validateText(panelId, 64);
            PayloadCodecSupport.validateText(controlId, 64);
            if (sequence < 1L || panelRevision < 1L || entryRevision < 0L) {
                throw new IllegalArgumentException("Panel action request is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record HudDelta(
            UUID sessionId,
            long revision,
            boolean reset,
            List<HudTile> tiles,
            List<String> removedIds
    ) implements CustomPacketPayload {
        public static final Type<HudDelta> TYPE = SefPayloads.type("play/hud_delta");
        public static final StreamCodec<FriendlyByteBuf, HudDelta> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.revision());
                    buffer.writeBoolean(value.reset());
                    PayloadCodecSupport.writeList(
                            buffer,
                            value.tiles(),
                            SefProtocol.MAXIMUM_HUD_TILES,
                            HudTile::encode);
                    PayloadCodecSupport.writeList(
                            buffer,
                            value.removedIds(),
                            SefProtocol.MAXIMUM_HUD_TILES,
                            (target, id) -> PayloadCodecSupport.writeString(target, id, 64));
                },
                buffer -> new HudDelta(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        buffer.readBoolean(),
                        PayloadCodecSupport.readList(
                                buffer,
                                SefProtocol.MAXIMUM_HUD_TILES,
                                HudTile::decode),
                        PayloadCodecSupport.readList(
                                buffer,
                                SefProtocol.MAXIMUM_HUD_TILES,
                                target -> PayloadCodecSupport.readString(target, 64))));

        public HudDelta {
            Objects.requireNonNull(sessionId, "sessionId");
            tiles = List.copyOf(Objects.requireNonNull(tiles, "tiles"));
            removedIds = List.copyOf(Objects.requireNonNull(removedIds, "removedIds"));
            removedIds.forEach(id -> PayloadCodecSupport.validateText(id, 64));
            java.util.Set<String> upsertIds = tiles.stream()
                    .map(HudTile::id)
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
            if (revision < 1L
                    || tiles.size() > SefProtocol.MAXIMUM_HUD_TILES
                    || removedIds.size() > SefProtocol.MAXIMUM_HUD_TILES
                    || upsertIds.size() != tiles.size()
                    || new java.util.HashSet<>(removedIds).size() != removedIds.size()
                    || removedIds.stream().anyMatch(upsertIds::contains)) {
                throw new IllegalArgumentException("HUD delta is invalid");
            }
        }

        public HudDelta(UUID sessionId, long revision, boolean reset, List<HudTile> tiles) {
            this(sessionId, revision, reset, tiles, List.of());
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record HudTile(String id, String text, Severity severity, HudSurface surface, int progressPercent) {
        public HudTile {
            PayloadCodecSupport.validateText(id, 64);
            PayloadCodecSupport.validateText(text, 128);
            Objects.requireNonNull(severity, "severity");
            Objects.requireNonNull(surface, "surface");
            if (progressPercent < 0 || progressPercent > 100
                    || surface != HudSurface.PROGRESS && progressPercent != 0) {
                throw new IllegalArgumentException("HUD tile progress is invalid");
            }
        }

        public HudTile(String id, String text, Severity severity) {
            this(id, text, severity, HudSurface.TILE, 0);
        }

        private static void encode(FriendlyByteBuf buffer, HudTile value) {
            PayloadCodecSupport.writeString(buffer, value.id(), 64);
            PayloadCodecSupport.writeString(buffer, value.text(), 128);
            buffer.writeEnum(value.severity());
            buffer.writeEnum(value.surface());
            buffer.writeByte(value.progressPercent());
        }

        private static HudTile decode(FriendlyByteBuf buffer) {
            return new HudTile(
                    PayloadCodecSupport.readString(buffer, 64),
                    PayloadCodecSupport.readString(buffer, 128),
                    buffer.readEnum(Severity.class),
                    buffer.readEnum(HudSurface.class),
                    buffer.readUnsignedByte());
        }
    }

    public enum HudSurface {
        TILE,
        ALERT,
        PROGRESS
    }

    public enum Severity {
        INFO,
        NOTICE,
        WARNING,
        CRITICAL
    }

    public record IdentityProjection(
            UUID sessionId,
            long revision,
            boolean reset,
            List<ProjectedIdentity> identities,
            List<UUID> removedPlayerIds
    ) implements CustomPacketPayload {
        public static final Type<IdentityProjection> TYPE = SefPayloads.type("play/identity_projection");
        public static final StreamCodec<RegistryFriendlyByteBuf, IdentityProjection> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.revision());
                    buffer.writeBoolean(value.reset());
                    PayloadCodecSupport.writeList(
                            buffer,
                            value.identities(),
                            SefProtocol.MAXIMUM_IDENTITY_PROJECTIONS,
                            ProjectedIdentity::encode);
                    PayloadCodecSupport.writeList(
                            buffer,
                            value.removedPlayerIds(),
                            SefProtocol.MAXIMUM_IDENTITY_PROJECTIONS,
                            (target, playerId) -> target.writeUUID(playerId));
                },
                buffer -> new IdentityProjection(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        buffer.readBoolean(),
                        PayloadCodecSupport.readList(
                                buffer,
                                SefProtocol.MAXIMUM_IDENTITY_PROJECTIONS,
                                ProjectedIdentity::decode),
                        PayloadCodecSupport.readList(
                                buffer,
                                SefProtocol.MAXIMUM_IDENTITY_PROJECTIONS,
                                target -> target.readUUID())));

        public IdentityProjection {
            Objects.requireNonNull(sessionId, "sessionId");
            identities = List.copyOf(Objects.requireNonNull(identities, "identities"));
            removedPlayerIds = List.copyOf(Objects.requireNonNull(removedPlayerIds, "removedPlayerIds"));
            java.util.Set<UUID> projected = identities.stream()
                    .map(ProjectedIdentity::playerId)
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
            if (revision < 1L
                    || identities.size() > SefProtocol.MAXIMUM_IDENTITY_PROJECTIONS
                    || removedPlayerIds.size() > SefProtocol.MAXIMUM_IDENTITY_PROJECTIONS
                    || projected.size() != identities.size()
                    || new java.util.HashSet<>(removedPlayerIds).size() != removedPlayerIds.size()
                    || removedPlayerIds.stream().anyMatch(projected::contains)) {
                throw new IllegalArgumentException("Identity projection is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ProjectedIdentity(UUID playerId, long revision, Component displayName) {
        public ProjectedIdentity {
            Objects.requireNonNull(playerId, "playerId");
            Objects.requireNonNull(displayName, "displayName");
            PayloadCodecSupport.validateText(displayName.getString(), 256);
            if (revision < 1L) {
                throw new IllegalArgumentException("Projected identity revision is invalid");
            }
        }

        private static void encode(RegistryFriendlyByteBuf buffer, ProjectedIdentity value) {
            buffer.writeUUID(value.playerId());
            buffer.writeVarLong(value.revision());
            RegistryFriendlyByteBuf componentBuffer =
                    new RegistryFriendlyByteBuf(Unpooled.buffer(), buffer.registryAccess());
            try {
                ComponentSerialization.TRUSTED_STREAM_CODEC.encode(componentBuffer, value.displayName());
                byte[] bytes = new byte[componentBuffer.readableBytes()];
                componentBuffer.readBytes(bytes);
                PayloadCodecSupport.writeBytes(
                        buffer,
                        bytes,
                        SefProtocol.MAXIMUM_IDENTITY_COMPONENT_BYTES);
            } finally {
                componentBuffer.release();
            }
        }

        private static ProjectedIdentity decode(RegistryFriendlyByteBuf buffer) {
            UUID playerId = buffer.readUUID();
            long revision = buffer.readVarLong();
            byte[] bytes = PayloadCodecSupport.readBytes(
                    buffer,
                    SefProtocol.MAXIMUM_IDENTITY_COMPONENT_BYTES);
            RegistryFriendlyByteBuf componentBuffer =
                    new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(bytes), buffer.registryAccess());
            try {
                Component displayName = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(componentBuffer);
                if (componentBuffer.isReadable()) {
                    throw new IllegalArgumentException("Projected identity component has trailing data");
                }
                return new ProjectedIdentity(playerId, revision, displayName);
            } finally {
                componentBuffer.release();
            }
        }
    }

    public record TagManifest(
            UUID sessionId,
            long revision,
            UUID tagId,
            String hash,
            int byteLength,
            String alternateText
    ) implements CustomPacketPayload {
        public static final Type<TagManifest> TYPE = SefPayloads.type("play/tag_manifest");
        public static final StreamCodec<FriendlyByteBuf, TagManifest> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.revision());
                    buffer.writeUUID(value.tagId());
                    PayloadCodecSupport.writeString(buffer, value.hash(), 64);
                    buffer.writeVarInt(value.byteLength());
                    PayloadCodecSupport.writeString(buffer, value.alternateText(), 128);
                },
                buffer -> new TagManifest(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        buffer.readUUID(),
                        PayloadCodecSupport.readString(buffer, 64),
                        buffer.readVarInt(),
                        PayloadCodecSupport.readString(buffer, 128)));

        public TagManifest {
            Objects.requireNonNull(sessionId, "sessionId");
            Objects.requireNonNull(tagId, "tagId");
            Objects.requireNonNull(hash, "hash");
            PayloadCodecSupport.validateText(alternateText, 128);
            if (revision < 1L || !hash.matches("[0-9a-f]{64}")
                    || byteLength < 1 || byteLength > SefProtocol.MAXIMUM_TAG_BYTES) {
                throw new IllegalArgumentException("Tag manifest is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record TagContentRequest(
            UUID sessionId,
            long sequence,
            String hash
    ) implements CustomPacketPayload {
        public static final Type<TagContentRequest> TYPE = SefPayloads.type("play/tag_content_request");
        public static final StreamCodec<FriendlyByteBuf, TagContentRequest> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.sequence());
                    PayloadCodecSupport.writeString(buffer, value.hash(), 64);
                },
                buffer -> new TagContentRequest(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        PayloadCodecSupport.readString(buffer, 64)));

        public TagContentRequest {
            Objects.requireNonNull(sessionId, "sessionId");
            Objects.requireNonNull(hash, "hash");
            if (sequence < 1L || !hash.matches("[0-9a-f]{64}")) {
                throw new IllegalArgumentException("Tag content request is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record TagContent(
            UUID sessionId,
            String hash,
            byte[] content
    ) implements CustomPacketPayload {
        public static final Type<TagContent> TYPE = SefPayloads.type("play/tag_content");
        public static final StreamCodec<FriendlyByteBuf, TagContent> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    PayloadCodecSupport.writeString(buffer, value.hash(), 64);
                    PayloadCodecSupport.writeBytes(buffer, value.content(), SefProtocol.MAXIMUM_TAG_BYTES);
                },
                buffer -> new TagContent(
                        buffer.readUUID(),
                        PayloadCodecSupport.readString(buffer, 64),
                        PayloadCodecSupport.readBytes(buffer, SefProtocol.MAXIMUM_TAG_BYTES)));

        public TagContent {
            Objects.requireNonNull(sessionId, "sessionId");
            Objects.requireNonNull(hash, "hash");
            content = Objects.requireNonNull(content, "content").clone();
            if (!hash.matches("[0-9a-f]{64}")
                    || content.length < 1
                    || content.length > SefProtocol.MAXIMUM_TAG_BYTES) {
                throw new IllegalArgumentException("Tag content is invalid");
            }
        }

        @Override
        public byte[] content() {
            return content.clone();
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
