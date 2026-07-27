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

    public record ControlEditorSnapshot(
            UUID sessionId,
            long panelRevision,
            String panelId,
            UUID recordId,
            long recordRevision,
            String featureId,
            String title,
            String details,
            String state,
            String screen,
            String status,
            List<ControlField> fields,
            List<String> states,
            List<String> operations,
            boolean confirmationRequired,
            boolean confirmationPending
    ) implements CustomPacketPayload {
        public static final Type<ControlEditorSnapshot> TYPE =
                SefPayloads.type("play/control_editor_snapshot");
        public static final StreamCodec<FriendlyByteBuf, ControlEditorSnapshot> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.panelRevision());
                    PayloadCodecSupport.writeString(buffer, value.panelId(), 64);
                    buffer.writeUUID(value.recordId());
                    buffer.writeVarLong(value.recordRevision());
                    PayloadCodecSupport.writeString(buffer, value.featureId(), 64);
                    PayloadCodecSupport.writeString(buffer, value.title(), 128);
                    PayloadCodecSupport.writeString(buffer, value.details(), 4096);
                    PayloadCodecSupport.writeString(buffer, value.state(), 32);
                    PayloadCodecSupport.writeString(buffer, value.screen(), 32);
                    PayloadCodecSupport.writeString(buffer, value.status(), 1024);
                    PayloadCodecSupport.writeList(buffer, value.fields(), 32, ControlField::encode);
                    PayloadCodecSupport.writeList(
                            buffer,
                            value.states(),
                            16,
                            (target, entry) -> PayloadCodecSupport.writeString(target, entry, 32));
                    PayloadCodecSupport.writeList(
                            buffer,
                            value.operations(),
                            16,
                            (target, entry) -> PayloadCodecSupport.writeString(target, entry, 32));
                    buffer.writeBoolean(value.confirmationRequired());
                    buffer.writeBoolean(value.confirmationPending());
                },
                buffer -> new ControlEditorSnapshot(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        PayloadCodecSupport.readString(buffer, 64),
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        PayloadCodecSupport.readString(buffer, 64),
                        PayloadCodecSupport.readString(buffer, 128),
                        PayloadCodecSupport.readString(buffer, 4096),
                        PayloadCodecSupport.readString(buffer, 32),
                        PayloadCodecSupport.readString(buffer, 32),
                        PayloadCodecSupport.readString(buffer, 1024),
                        PayloadCodecSupport.readList(buffer, 32, ControlField::decode),
                        PayloadCodecSupport.readList(
                                buffer,
                                16,
                                target -> PayloadCodecSupport.readString(target, 32)),
                        PayloadCodecSupport.readList(
                                buffer,
                                16,
                                target -> PayloadCodecSupport.readString(target, 32)),
                        buffer.readBoolean(),
                        buffer.readBoolean()));

        public ControlEditorSnapshot {
            Objects.requireNonNull(sessionId, "sessionId");
            Objects.requireNonNull(recordId, "recordId");
            PayloadCodecSupport.validateText(panelId, 64);
            PayloadCodecSupport.validateText(featureId, 64);
            PayloadCodecSupport.validateText(title, 128);
            PayloadCodecSupport.validateText(details, 4096);
            PayloadCodecSupport.validateText(state, 32);
            PayloadCodecSupport.validateText(screen, 32);
            PayloadCodecSupport.validateText(status, 1024);
            fields = List.copyOf(Objects.requireNonNull(fields, "fields"));
            states = List.copyOf(Objects.requireNonNull(states, "states"));
            operations = List.copyOf(Objects.requireNonNull(operations, "operations"));
            states.forEach(entry -> PayloadCodecSupport.validateText(entry, 32));
            operations.forEach(entry -> PayloadCodecSupport.validateText(entry, 32));
            if (panelRevision < 1L
                    || recordRevision < 1L
                    || fields.size() > 32
                    || states.size() > 16
                    || operations.size() > 16) {
                throw new IllegalArgumentException("Control editor snapshot is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ControlField(
            String id,
            String type,
            boolean required,
            long minimum,
            long maximum,
            String value,
            List<String> choices
    ) {
        public ControlField {
            PayloadCodecSupport.validateText(id, 64);
            PayloadCodecSupport.validateText(type, 32);
            PayloadCodecSupport.validateText(value, 4096);
            choices = List.copyOf(Objects.requireNonNull(choices, "choices"));
            choices.forEach(choice -> PayloadCodecSupport.validateText(choice, 128));
            if (choices.size() > 64) {
                throw new IllegalArgumentException("Control field choices are outside bounds");
            }
        }

        private static void encode(FriendlyByteBuf buffer, ControlField value) {
            PayloadCodecSupport.writeString(buffer, value.id(), 64);
            PayloadCodecSupport.writeString(buffer, value.type(), 32);
            buffer.writeBoolean(value.required());
            buffer.writeLong(value.minimum());
            buffer.writeLong(value.maximum());
            PayloadCodecSupport.writeString(buffer, value.value(), 4096);
            PayloadCodecSupport.writeList(
                    buffer,
                    value.choices(),
                    64,
                    (target, entry) -> PayloadCodecSupport.writeString(target, entry, 128));
        }

        private static ControlField decode(FriendlyByteBuf buffer) {
            return new ControlField(
                    PayloadCodecSupport.readString(buffer, 64),
                    PayloadCodecSupport.readString(buffer, 32),
                    buffer.readBoolean(),
                    buffer.readLong(),
                    buffer.readLong(),
                    PayloadCodecSupport.readString(buffer, 4096),
                    PayloadCodecSupport.readList(
                            buffer,
                            64,
                            target -> PayloadCodecSupport.readString(target, 128)));
        }
    }

    public record ControlMutationRequest(
            UUID sessionId,
            long sequence,
            String panelId,
            long panelRevision,
            UUID recordId,
            long expectedRecordRevision,
            String operation,
            String title,
            String details,
            String argument,
            List<ControlFieldValue> fields
    ) implements CustomPacketPayload {
        public static final Type<ControlMutationRequest> TYPE =
                SefPayloads.type("play/control_mutation");
        public static final StreamCodec<FriendlyByteBuf, ControlMutationRequest> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.sequence());
                    PayloadCodecSupport.writeString(buffer, value.panelId(), 64);
                    buffer.writeVarLong(value.panelRevision());
                    buffer.writeUUID(value.recordId());
                    buffer.writeVarLong(value.expectedRecordRevision());
                    PayloadCodecSupport.writeString(buffer, value.operation(), 32);
                    PayloadCodecSupport.writeString(buffer, value.title(), 128);
                    PayloadCodecSupport.writeString(buffer, value.details(), 4096);
                    PayloadCodecSupport.writeString(buffer, value.argument(), 1024);
                    PayloadCodecSupport.writeList(
                            buffer,
                            value.fields(),
                            32,
                            ControlFieldValue::encode);
                },
                buffer -> new ControlMutationRequest(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        PayloadCodecSupport.readString(buffer, 64),
                        buffer.readVarLong(),
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        PayloadCodecSupport.readString(buffer, 32),
                        PayloadCodecSupport.readString(buffer, 128),
                        PayloadCodecSupport.readString(buffer, 4096),
                        PayloadCodecSupport.readString(buffer, 1024),
                        PayloadCodecSupport.readList(buffer, 32, ControlFieldValue::decode)));

        public ControlMutationRequest {
            Objects.requireNonNull(sessionId, "sessionId");
            Objects.requireNonNull(recordId, "recordId");
            PayloadCodecSupport.validateText(panelId, 64);
            PayloadCodecSupport.validateText(operation, 32);
            PayloadCodecSupport.validateText(title, 128);
            PayloadCodecSupport.validateText(details, 4096);
            PayloadCodecSupport.validateText(argument, 1024);
            fields = List.copyOf(Objects.requireNonNull(fields, "fields"));
            if (sequence < 1L
                    || panelRevision < 1L
                    || expectedRecordRevision < 1L
                    || fields.size() > 32) {
                throw new IllegalArgumentException("Control mutation request is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ControlFieldValue(String id, String value) {
        public ControlFieldValue {
            PayloadCodecSupport.validateText(id, 64);
            PayloadCodecSupport.validateText(value, 4096);
        }

        private static void encode(FriendlyByteBuf buffer, ControlFieldValue value) {
            PayloadCodecSupport.writeString(buffer, value.id(), 64);
            PayloadCodecSupport.writeString(buffer, value.value(), 4096);
        }

        private static ControlFieldValue decode(FriendlyByteBuf buffer) {
            return new ControlFieldValue(
                    PayloadCodecSupport.readString(buffer, 64),
                    PayloadCodecSupport.readString(buffer, 4096));
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

    public record TagContentChunkRequest(
            UUID sessionId,
            long sequence,
            String hash,
            int offset
    ) implements CustomPacketPayload {
        public static final Type<TagContentChunkRequest> TYPE =
                SefPayloads.type("play/tag_content_chunk_request");
        public static final StreamCodec<FriendlyByteBuf, TagContentChunkRequest> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.sequence());
                    PayloadCodecSupport.writeString(buffer, value.hash(), 64);
                    buffer.writeVarInt(value.offset());
                },
                buffer -> new TagContentChunkRequest(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        PayloadCodecSupport.readString(buffer, 64),
                        buffer.readVarInt()));

        public TagContentChunkRequest {
            Objects.requireNonNull(sessionId, "sessionId");
            Objects.requireNonNull(hash, "hash");
            if (sequence < 1L
                    || !hash.matches("[0-9a-f]{64}")
                    || offset < 0
                    || offset >= SefProtocol.MAXIMUM_TAG_BYTES
                    || offset % SefProtocol.MAXIMUM_TAG_DOWNLOAD_CHUNK_BYTES != 0) {
                throw new IllegalArgumentException("tag content chunk request is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record TagContentChunk(
            UUID sessionId,
            String hash,
            int totalBytes,
            int offset,
            byte[] content
    ) implements CustomPacketPayload {
        public static final Type<TagContentChunk> TYPE = SefPayloads.type("play/tag_content_chunk");
        public static final StreamCodec<FriendlyByteBuf, TagContentChunk> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    PayloadCodecSupport.writeString(buffer, value.hash(), 64);
                    buffer.writeVarInt(value.totalBytes());
                    buffer.writeVarInt(value.offset());
                    PayloadCodecSupport.writeBytes(
                            buffer,
                            value.content(),
                            SefProtocol.MAXIMUM_TAG_DOWNLOAD_CHUNK_BYTES);
                },
                buffer -> new TagContentChunk(
                        buffer.readUUID(),
                        PayloadCodecSupport.readString(buffer, 64),
                        buffer.readVarInt(),
                        buffer.readVarInt(),
                        PayloadCodecSupport.readBytes(
                                buffer,
                                SefProtocol.MAXIMUM_TAG_DOWNLOAD_CHUNK_BYTES)));

        public TagContentChunk {
            Objects.requireNonNull(sessionId, "sessionId");
            Objects.requireNonNull(hash, "hash");
            content = Objects.requireNonNull(content, "content").clone();
            if (!hash.matches("[0-9a-f]{64}")
                    || totalBytes < 1
                    || totalBytes > SefProtocol.MAXIMUM_TAG_BYTES
                    || offset < 0
                    || offset >= totalBytes
                    || offset % SefProtocol.MAXIMUM_TAG_DOWNLOAD_CHUNK_BYTES != 0
                    || content.length < 1
                    || content.length > SefProtocol.MAXIMUM_TAG_DOWNLOAD_CHUNK_BYTES
                    || offset + content.length > totalBytes
                    || offset + content.length < totalBytes
                    && content.length != SefProtocol.MAXIMUM_TAG_DOWNLOAD_CHUNK_BYTES) {
                throw new IllegalArgumentException("tag content chunk is invalid");
            }
        }

        @Override
        public byte[] content() {
            return content.clone();
        }

        public boolean complete() {
            return offset + content.length == totalBytes;
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record TagUploadBegin(
            UUID sessionId,
            long sequence,
            UUID tagId,
            UUID leaseId,
            long expectedTagRevision,
            int totalBytes,
            String expectedHash
    ) implements CustomPacketPayload {
        public static final Type<TagUploadBegin> TYPE = SefPayloads.type("play/tag_upload_begin");
        public static final StreamCodec<FriendlyByteBuf, TagUploadBegin> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.sequence());
                    buffer.writeUUID(value.tagId());
                    buffer.writeUUID(value.leaseId());
                    buffer.writeVarLong(value.expectedTagRevision());
                    buffer.writeVarInt(value.totalBytes());
                    PayloadCodecSupport.writeString(buffer, value.expectedHash(), 64);
                },
                buffer -> new TagUploadBegin(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        buffer.readUUID(),
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        buffer.readVarInt(),
                        PayloadCodecSupport.readString(buffer, 64)));

        public TagUploadBegin {
            Objects.requireNonNull(sessionId, "sessionId");
            Objects.requireNonNull(tagId, "tagId");
            Objects.requireNonNull(leaseId, "leaseId");
            Objects.requireNonNull(expectedHash, "expectedHash");
            if (sequence < 1L || expectedTagRevision < 1L
                    || totalBytes < 1 || totalBytes > SefProtocol.MAXIMUM_TAG_BYTES
                    || !expectedHash.matches("[0-9a-f]{64}")) {
                throw new IllegalArgumentException("tag upload declaration is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record TagUploadChunk(
            UUID sessionId,
            long sequence,
            UUID uploadId,
            int chunkIndex,
            byte[] content
    ) implements CustomPacketPayload {
        public static final Type<TagUploadChunk> TYPE = SefPayloads.type("play/tag_upload_chunk");
        public static final StreamCodec<FriendlyByteBuf, TagUploadChunk> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.sequence());
                    buffer.writeUUID(value.uploadId());
                    buffer.writeVarInt(value.chunkIndex());
                    PayloadCodecSupport.writeBytes(
                            buffer,
                            value.content(),
                            SefProtocol.MAXIMUM_TAG_UPLOAD_CHUNK_BYTES);
                },
                buffer -> new TagUploadChunk(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        buffer.readUUID(),
                        buffer.readVarInt(),
                        PayloadCodecSupport.readBytes(
                                buffer,
                                SefProtocol.MAXIMUM_TAG_UPLOAD_CHUNK_BYTES)));

        public TagUploadChunk {
            Objects.requireNonNull(sessionId, "sessionId");
            Objects.requireNonNull(uploadId, "uploadId");
            content = Objects.requireNonNull(content, "content").clone();
            if (sequence < 1L || chunkIndex < 0
                    || content.length < 1
                    || content.length > SefProtocol.MAXIMUM_TAG_UPLOAD_CHUNK_BYTES) {
                throw new IllegalArgumentException("tag upload chunk is invalid");
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

    public record TagUploadFinish(
            UUID sessionId,
            long sequence,
            UUID uploadId
    ) implements CustomPacketPayload {
        public static final Type<TagUploadFinish> TYPE = SefPayloads.type("play/tag_upload_finish");
        public static final StreamCodec<FriendlyByteBuf, TagUploadFinish> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.sequence());
                    buffer.writeUUID(value.uploadId());
                },
                buffer -> new TagUploadFinish(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        buffer.readUUID()));

        public TagUploadFinish {
            Objects.requireNonNull(sessionId, "sessionId");
            Objects.requireNonNull(uploadId, "uploadId");
            if (sequence < 1L) {
                throw new IllegalArgumentException("tag upload finish is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record TagUploadCancel(
            UUID sessionId,
            long sequence,
            UUID uploadId
    ) implements CustomPacketPayload {
        public static final Type<TagUploadCancel> TYPE = SefPayloads.type("play/tag_upload_cancel");
        public static final StreamCodec<FriendlyByteBuf, TagUploadCancel> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.sequence());
                    buffer.writeUUID(value.uploadId());
                },
                buffer -> new TagUploadCancel(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        buffer.readUUID()));

        public TagUploadCancel {
            Objects.requireNonNull(sessionId, "sessionId");
            Objects.requireNonNull(uploadId, "uploadId");
            if (sequence < 1L) {
                throw new IllegalArgumentException("tag upload cancel is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record TagMutationRequest(
            UUID sessionId,
            long sequence,
            String operation,
            String tagReference,
            long expectedTagRevision,
            String argument
    ) implements CustomPacketPayload {
        public static final Type<TagMutationRequest> TYPE = SefPayloads.type("play/tag_mutation_request");
        public static final StreamCodec<FriendlyByteBuf, TagMutationRequest> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.sequence());
                    PayloadCodecSupport.writeString(buffer, value.operation(), 32);
                    PayloadCodecSupport.writeString(buffer, value.tagReference(), 64);
                    buffer.writeVarLong(value.expectedTagRevision());
                    PayloadCodecSupport.writeString(buffer, value.argument(), 256);
                },
                buffer -> new TagMutationRequest(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        PayloadCodecSupport.readString(buffer, 32),
                        PayloadCodecSupport.readString(buffer, 64),
                        buffer.readVarLong(),
                        PayloadCodecSupport.readString(buffer, 256)));

        public TagMutationRequest {
            Objects.requireNonNull(sessionId, "sessionId");
            PayloadCodecSupport.validateText(operation, 32);
            PayloadCodecSupport.validateText(tagReference, 64);
            PayloadCodecSupport.validateText(argument, 256);
            if (sequence < 1L || expectedTagRevision < 0L
                    || !operation.matches("[a-z_]{1,32}")) {
                throw new IllegalArgumentException("tag mutation request is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record TagOperationResult(
            UUID sessionId,
            long requestSequence,
            boolean successful,
            String reason,
            String detail,
            UUID operationId,
            long registryRevision,
            int completedBytes,
            int totalBytes
    ) implements CustomPacketPayload {
        public static final Type<TagOperationResult> TYPE = SefPayloads.type("play/tag_operation_result");
        public static final StreamCodec<FriendlyByteBuf, TagOperationResult> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.requestSequence());
                    buffer.writeBoolean(value.successful());
                    PayloadCodecSupport.writeString(buffer, value.reason(), 64);
                    PayloadCodecSupport.writeString(buffer, value.detail(), 256);
                    buffer.writeBoolean(value.operationId() != null);
                    if (value.operationId() != null) {
                        buffer.writeUUID(value.operationId());
                    }
                    buffer.writeVarLong(value.registryRevision());
                    buffer.writeVarInt(value.completedBytes());
                    buffer.writeVarInt(value.totalBytes());
                },
                buffer -> new TagOperationResult(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        buffer.readBoolean(),
                        PayloadCodecSupport.readString(buffer, 64),
                        PayloadCodecSupport.readString(buffer, 256),
                        buffer.readBoolean() ? buffer.readUUID() : null,
                        buffer.readVarLong(),
                        buffer.readVarInt(),
                        buffer.readVarInt()));

        public TagOperationResult {
            Objects.requireNonNull(sessionId, "sessionId");
            PayloadCodecSupport.validateText(reason, 64);
            PayloadCodecSupport.validateText(detail, 256);
            if (requestSequence < 1L || registryRevision < 1L
                    || completedBytes < 0 || totalBytes < 0 || completedBytes > totalBytes) {
                throw new IllegalArgumentException("tag operation result is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record TagRegistrySnapshot(
            UUID sessionId,
            long revision,
            boolean reset,
            List<TagManifestEntry> entries
    ) implements CustomPacketPayload {
        public static final Type<TagRegistrySnapshot> TYPE = SefPayloads.type("play/tag_registry_snapshot");
        public static final StreamCodec<FriendlyByteBuf, TagRegistrySnapshot> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.revision());
                    buffer.writeBoolean(value.reset());
                    PayloadCodecSupport.writeList(
                            buffer,
                            value.entries(),
                            SefProtocol.MAXIMUM_TAG_MANIFEST_ENTRIES,
                            (target, entry) -> entry.encode(target));
                },
                buffer -> new TagRegistrySnapshot(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        buffer.readBoolean(),
                        PayloadCodecSupport.readList(
                                buffer,
                                SefProtocol.MAXIMUM_TAG_MANIFEST_ENTRIES,
                                TagManifestEntry::decode)));

        public TagRegistrySnapshot {
            Objects.requireNonNull(sessionId, "sessionId");
            entries = List.copyOf(entries);
            if (revision < 1L || entries.size() > SefProtocol.MAXIMUM_TAG_MANIFEST_ENTRIES) {
                throw new IllegalArgumentException("tag registry snapshot is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record TagManifestEntry(
            UUID tagId,
            long tagRevision,
            String resourceKey,
            String displayName,
            String alternateText,
            String hash,
            int byteLength,
            int width,
            int height
    ) {
        public TagManifestEntry {
            Objects.requireNonNull(tagId, "tagId");
            PayloadCodecSupport.validateText(resourceKey, 64);
            PayloadCodecSupport.validateText(displayName, 64);
            PayloadCodecSupport.validateText(alternateText, 128);
            Objects.requireNonNull(hash, "hash");
            if (tagRevision < 1L
                    || !hash.matches("[0-9a-f]{64}")
                    || byteLength < 1
                    || byteLength > SefProtocol.MAXIMUM_TAG_BYTES
                    || width < 1
                    || width > 512
                    || height < 1
                    || height > 256
                    || (long) width * height > 65_536L) {
                throw new IllegalArgumentException("tag manifest entry is invalid");
            }
        }

        private void encode(FriendlyByteBuf buffer) {
            buffer.writeUUID(tagId);
            buffer.writeVarLong(tagRevision);
            PayloadCodecSupport.writeString(buffer, resourceKey, 64);
            PayloadCodecSupport.writeString(buffer, displayName, 64);
            PayloadCodecSupport.writeString(buffer, alternateText, 128);
            PayloadCodecSupport.writeString(buffer, hash, 64);
            buffer.writeVarInt(byteLength);
            buffer.writeVarInt(width);
            buffer.writeVarInt(height);
        }

        private static TagManifestEntry decode(FriendlyByteBuf buffer) {
            return new TagManifestEntry(
                    buffer.readUUID(),
                    buffer.readVarLong(),
                    PayloadCodecSupport.readString(buffer, 64),
                    PayloadCodecSupport.readString(buffer, 64),
                    PayloadCodecSupport.readString(buffer, 128),
                    PayloadCodecSupport.readString(buffer, 64),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readVarInt());
        }
    }

    public record TagAssignmentSnapshot(
            UUID sessionId,
            long revision,
            boolean reset,
            List<TagAssignmentProjection> assignments
    ) implements CustomPacketPayload {
        public static final Type<TagAssignmentSnapshot> TYPE = SefPayloads.type("play/tag_assignment_snapshot");
        public static final StreamCodec<FriendlyByteBuf, TagAssignmentSnapshot> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.revision());
                    buffer.writeBoolean(value.reset());
                    PayloadCodecSupport.writeList(
                            buffer,
                            value.assignments(),
                            SefProtocol.MAXIMUM_TAG_ASSIGNMENTS,
                            (target, assignment) -> assignment.encode(target));
                },
                buffer -> new TagAssignmentSnapshot(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        buffer.readBoolean(),
                        PayloadCodecSupport.readList(
                                buffer,
                                SefProtocol.MAXIMUM_TAG_ASSIGNMENTS,
                                TagAssignmentProjection::decode)));

        public TagAssignmentSnapshot {
            Objects.requireNonNull(sessionId, "sessionId");
            assignments = List.copyOf(assignments);
            if (revision < 1L || assignments.size() > SefProtocol.MAXIMUM_TAG_ASSIGNMENTS) {
                throw new IllegalArgumentException("tag assignment snapshot is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record TagAssignmentProjection(
            UUID subjectId,
            UUID tagId,
            String slot,
            int priority,
            long presentationRevision
    ) {
        public TagAssignmentProjection {
            Objects.requireNonNull(subjectId, "subjectId");
            Objects.requireNonNull(tagId, "tagId");
            PayloadCodecSupport.validateText(slot, 32);
            if (priority < -10_000 || priority > 10_000 || presentationRevision < 1L) {
                throw new IllegalArgumentException("tag assignment projection is invalid");
            }
        }

        private void encode(FriendlyByteBuf buffer) {
            buffer.writeUUID(subjectId);
            buffer.writeUUID(tagId);
            PayloadCodecSupport.writeString(buffer, slot, 32);
            buffer.writeInt(priority);
            buffer.writeVarLong(presentationRevision);
        }

        private static TagAssignmentProjection decode(FriendlyByteBuf buffer) {
            return new TagAssignmentProjection(
                    buffer.readUUID(),
                    buffer.readUUID(),
                    PayloadCodecSupport.readString(buffer, 32),
                    buffer.readInt(),
                    buffer.readVarLong());
        }
    }

    public record TagManagerQuery(
            UUID sessionId,
            long sequence,
            String section,
            int page,
            String query
    ) implements CustomPacketPayload {
        public static final Type<TagManagerQuery> TYPE = SefPayloads.type("play/tag_manager_query");
        public static final StreamCodec<FriendlyByteBuf, TagManagerQuery> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.sequence());
                    PayloadCodecSupport.writeString(buffer, value.section(), 32);
                    buffer.writeVarInt(value.page());
                    PayloadCodecSupport.writeString(buffer, value.query(), 64);
                },
                buffer -> new TagManagerQuery(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        PayloadCodecSupport.readString(buffer, 32),
                        buffer.readVarInt(),
                        PayloadCodecSupport.readString(buffer, 64)));

        public TagManagerQuery {
            Objects.requireNonNull(sessionId, "sessionId");
            PayloadCodecSupport.validateText(section, 32);
            PayloadCodecSupport.validateText(query, 64);
            if (sequence < 1L
                    || page < 1
                    || page > 10_000
                    || !section.matches("[a-z_]{1,32}")) {
                throw new IllegalArgumentException("tag manager query is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record TagManagerSnapshot(
            UUID sessionId,
            long requestSequence,
            long registryRevision,
            String section,
            int page,
            int pages,
            List<TagManagerEntry> entries
    ) implements CustomPacketPayload {
        public static final Type<TagManagerSnapshot> TYPE = SefPayloads.type("play/tag_manager_snapshot");
        public static final StreamCodec<FriendlyByteBuf, TagManagerSnapshot> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.requestSequence());
                    buffer.writeVarLong(value.registryRevision());
                    PayloadCodecSupport.writeString(buffer, value.section(), 32);
                    buffer.writeVarInt(value.page());
                    buffer.writeVarInt(value.pages());
                    PayloadCodecSupport.writeList(
                            buffer,
                            value.entries(),
                            100,
                            (target, entry) -> entry.encode(target));
                },
                buffer -> new TagManagerSnapshot(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        buffer.readVarLong(),
                        PayloadCodecSupport.readString(buffer, 32),
                        buffer.readVarInt(),
                        buffer.readVarInt(),
                        PayloadCodecSupport.readList(buffer, 100, TagManagerEntry::decode)));

        public TagManagerSnapshot {
            Objects.requireNonNull(sessionId, "sessionId");
            PayloadCodecSupport.validateText(section, 32);
            entries = List.copyOf(entries);
            if (requestSequence < 1L
                    || registryRevision < 1L
                    || page < 1
                    || pages < 1
                    || page > pages
                    || entries.size() > 100) {
                throw new IllegalArgumentException("tag manager snapshot is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record TagManagerEntry(
            String type,
            UUID id,
            UUID tagId,
            String resourceKey,
            String title,
            String subtitle,
            String status,
            long revision
    ) {
        public TagManagerEntry {
            PayloadCodecSupport.validateText(type, 32);
            Objects.requireNonNull(id, "id");
            PayloadCodecSupport.validateText(resourceKey, 64);
            PayloadCodecSupport.validateText(title, 128);
            PayloadCodecSupport.validateText(subtitle, 256);
            PayloadCodecSupport.validateText(status, 32);
            if (!type.matches("[a-z_]{1,32}") || revision < 1L) {
                throw new IllegalArgumentException("tag manager entry is invalid");
            }
        }

        private void encode(FriendlyByteBuf buffer) {
            PayloadCodecSupport.writeString(buffer, type, 32);
            buffer.writeUUID(id);
            buffer.writeBoolean(tagId != null);
            if (tagId != null) {
                buffer.writeUUID(tagId);
            }
            PayloadCodecSupport.writeString(buffer, resourceKey, 64);
            PayloadCodecSupport.writeString(buffer, title, 128);
            PayloadCodecSupport.writeString(buffer, subtitle, 256);
            PayloadCodecSupport.writeString(buffer, status, 32);
            buffer.writeVarLong(revision);
        }

        private static TagManagerEntry decode(FriendlyByteBuf buffer) {
            return new TagManagerEntry(
                    PayloadCodecSupport.readString(buffer, 32),
                    buffer.readUUID(),
                    buffer.readBoolean() ? buffer.readUUID() : null,
                    PayloadCodecSupport.readString(buffer, 64),
                    PayloadCodecSupport.readString(buffer, 128),
                    PayloadCodecSupport.readString(buffer, 256),
                    PayloadCodecSupport.readString(buffer, 32),
                    buffer.readVarLong());
        }
    }

    public record TagRegistryDelta(
            UUID sessionId,
            long revision,
            List<UUID> removedTagIds,
            List<TagManifestEntry> entries
    ) implements CustomPacketPayload {
        public static final Type<TagRegistryDelta> TYPE = SefPayloads.type("play/tag_registry_delta");
        public static final StreamCodec<FriendlyByteBuf, TagRegistryDelta> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.revision());
                    PayloadCodecSupport.writeList(
                            buffer,
                            value.removedTagIds(),
                            SefProtocol.MAXIMUM_TAG_MANIFEST_ENTRIES,
                            (target, id) -> target.writeUUID(id));
                    PayloadCodecSupport.writeList(
                            buffer,
                            value.entries(),
                            SefProtocol.MAXIMUM_TAG_MANIFEST_ENTRIES,
                            (target, entry) -> entry.encode(target));
                },
                buffer -> new TagRegistryDelta(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        PayloadCodecSupport.readList(
                                buffer,
                                SefProtocol.MAXIMUM_TAG_MANIFEST_ENTRIES,
                                target -> target.readUUID()),
                        PayloadCodecSupport.readList(
                                buffer,
                                SefProtocol.MAXIMUM_TAG_MANIFEST_ENTRIES,
                                TagManifestEntry::decode)));

        public TagRegistryDelta {
            Objects.requireNonNull(sessionId, "sessionId");
            removedTagIds = List.copyOf(removedTagIds);
            entries = List.copyOf(entries);
            if (revision < 1L
                    || removedTagIds.size() + entries.size()
                    > SefProtocol.MAXIMUM_TAG_MANIFEST_ENTRIES * 2) {
                throw new IllegalArgumentException("tag registry delta is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record TagAssignmentDelta(
            UUID sessionId,
            long revision,
            List<TagAssignmentKey> removedAssignments,
            List<TagAssignmentProjection> assignments
    ) implements CustomPacketPayload {
        public static final Type<TagAssignmentDelta> TYPE = SefPayloads.type("play/tag_assignment_delta");
        public static final StreamCodec<FriendlyByteBuf, TagAssignmentDelta> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.revision());
                    PayloadCodecSupport.writeList(
                            buffer,
                            value.removedAssignments(),
                            SefProtocol.MAXIMUM_TAG_ASSIGNMENTS,
                            (target, key) -> key.encode(target));
                    PayloadCodecSupport.writeList(
                            buffer,
                            value.assignments(),
                            SefProtocol.MAXIMUM_TAG_ASSIGNMENTS,
                            (target, assignment) -> assignment.encode(target));
                },
                buffer -> new TagAssignmentDelta(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        PayloadCodecSupport.readList(
                                buffer,
                                SefProtocol.MAXIMUM_TAG_ASSIGNMENTS,
                                TagAssignmentKey::decode),
                        PayloadCodecSupport.readList(
                                buffer,
                                SefProtocol.MAXIMUM_TAG_ASSIGNMENTS,
                                TagAssignmentProjection::decode)));

        public TagAssignmentDelta {
            Objects.requireNonNull(sessionId, "sessionId");
            removedAssignments = List.copyOf(removedAssignments);
            assignments = List.copyOf(assignments);
            if (revision < 1L
                    || removedAssignments.size() + assignments.size()
                    > SefProtocol.MAXIMUM_TAG_ASSIGNMENTS * 2) {
                throw new IllegalArgumentException("tag assignment delta is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record TagAssignmentKey(UUID subjectId, UUID tagId, String slot) {
        public TagAssignmentKey {
            Objects.requireNonNull(subjectId, "subjectId");
            Objects.requireNonNull(tagId, "tagId");
            PayloadCodecSupport.validateText(slot, 32);
        }

        private void encode(FriendlyByteBuf buffer) {
            buffer.writeUUID(subjectId);
            buffer.writeUUID(tagId);
            PayloadCodecSupport.writeString(buffer, slot, 32);
        }

        private static TagAssignmentKey decode(FriendlyByteBuf buffer) {
            return new TagAssignmentKey(
                    buffer.readUUID(),
                    buffer.readUUID(),
                    PayloadCodecSupport.readString(buffer, 32));
        }
    }

    public record OpenFancyTagsStudio(
            UUID sessionId,
            String section
    ) implements CustomPacketPayload {
        public static final Type<OpenFancyTagsStudio> TYPE =
                SefPayloads.type("play/open_fancy_tags_studio");
        public static final StreamCodec<FriendlyByteBuf, OpenFancyTagsStudio> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    PayloadCodecSupport.writeString(buffer, value.section(), 32);
                },
                buffer -> new OpenFancyTagsStudio(
                        buffer.readUUID(),
                        PayloadCodecSupport.readString(buffer, 32)));

        public OpenFancyTagsStudio {
            Objects.requireNonNull(sessionId, "sessionId");
            PayloadCodecSupport.validateText(section, 32);
            if (!section.matches("[a-z_]{1,32}")) {
                throw new IllegalArgumentException("fancy tags studio section is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record TagCacheInvalidation(
            UUID sessionId,
            long revision,
            List<String> hashes
    ) implements CustomPacketPayload {
        public static final Type<TagCacheInvalidation> TYPE = SefPayloads.type("play/tag_cache_invalidation");
        public static final StreamCodec<FriendlyByteBuf, TagCacheInvalidation> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.revision());
                    PayloadCodecSupport.writeList(
                            buffer,
                            value.hashes(),
                            64,
                            (target, hash) -> PayloadCodecSupport.writeString(target, hash, 64));
                },
                buffer -> new TagCacheInvalidation(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        PayloadCodecSupport.readList(
                                buffer,
                                64,
                                target -> PayloadCodecSupport.readString(target, 64))));

        public TagCacheInvalidation {
            Objects.requireNonNull(sessionId, "sessionId");
            hashes = List.copyOf(hashes);
            if (revision < 1L
                    || hashes.size() > 64
                    || hashes.stream().anyMatch(hash -> !hash.matches("[0-9a-f]{64}"))) {
                throw new IllegalArgumentException("tag cache invalidation is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record DisguiseAbilityRequest(
            UUID sessionId,
            long sequence,
            String slot
    ) implements CustomPacketPayload {
        public static final Type<DisguiseAbilityRequest> TYPE =
                SefPayloads.type("play/disguise_ability_request");
        public static final StreamCodec<FriendlyByteBuf, DisguiseAbilityRequest> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.sequence());
                    PayloadCodecSupport.writeString(buffer, value.slot(), 16);
                },
                buffer -> new DisguiseAbilityRequest(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        PayloadCodecSupport.readString(buffer, 16)));

        public DisguiseAbilityRequest {
            Objects.requireNonNull(sessionId, "sessionId");
            PayloadCodecSupport.validateText(slot, 16);
            if (sequence < 1L || !slot.matches("[a-z_]{1,16}")) {
                throw new IllegalArgumentException("disguise ability request is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record DisguiseSnapshot(
            UUID sessionId,
            long revision,
            boolean reset,
            List<DisguiseProjection> projections
    ) implements CustomPacketPayload {
        public static final Type<DisguiseSnapshot> TYPE = SefPayloads.type("play/disguise_snapshot");
        public static final StreamCodec<FriendlyByteBuf, DisguiseSnapshot> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.revision());
                    buffer.writeBoolean(value.reset());
                    PayloadCodecSupport.writeList(
                            buffer,
                            value.projections(),
                            SefProtocol.MAXIMUM_DISGUISE_PROJECTIONS,
                            (target, projection) -> projection.encode(target));
                },
                buffer -> new DisguiseSnapshot(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        buffer.readBoolean(),
                        PayloadCodecSupport.readList(
                                buffer,
                                SefProtocol.MAXIMUM_DISGUISE_PROJECTIONS,
                                DisguiseProjection::decode)));

        public DisguiseSnapshot {
            Objects.requireNonNull(sessionId, "sessionId");
            projections = List.copyOf(projections);
            if (revision < 1L || projections.size() > SefProtocol.MAXIMUM_DISGUISE_PROJECTIONS) {
                throw new IllegalArgumentException("disguise snapshot is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record DisguiseDelta(
            UUID sessionId,
            long revision,
            List<UUID> removedSubjectIds,
            List<DisguiseProjection> projections
    ) implements CustomPacketPayload {
        public static final Type<DisguiseDelta> TYPE = SefPayloads.type("play/disguise_delta");
        public static final StreamCodec<FriendlyByteBuf, DisguiseDelta> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.revision());
                    PayloadCodecSupport.writeList(
                            buffer,
                            value.removedSubjectIds(),
                            SefProtocol.MAXIMUM_DISGUISE_PROJECTIONS,
                            (target, id) -> target.writeUUID(id));
                    PayloadCodecSupport.writeList(
                            buffer,
                            value.projections(),
                            SefProtocol.MAXIMUM_DISGUISE_PROJECTIONS,
                            (target, projection) -> projection.encode(target));
                },
                buffer -> new DisguiseDelta(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        PayloadCodecSupport.readList(
                                buffer,
                                SefProtocol.MAXIMUM_DISGUISE_PROJECTIONS,
                                target -> target.readUUID()),
                        PayloadCodecSupport.readList(
                                buffer,
                                SefProtocol.MAXIMUM_DISGUISE_PROJECTIONS,
                                DisguiseProjection::decode)));

        public DisguiseDelta {
            Objects.requireNonNull(sessionId, "sessionId");
            removedSubjectIds = List.copyOf(removedSubjectIds);
            projections = List.copyOf(projections);
            if (revision < 1L
                    || removedSubjectIds.size() + projections.size()
                    > SefProtocol.MAXIMUM_DISGUISE_PROJECTIONS * 2) {
                throw new IllegalArgumentException("disguise delta is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record DisguiseProjection(
            UUID subjectId,
            long disguiseRevision,
            String kind,
            String reference,
            UUID profileId,
            String profileName,
            String texturesValue,
            String texturesSignature,
            String labelMode,
            String equipmentPolicy,
            boolean traitsEnabled,
            boolean abilitiesEnabled
    ) {
        public DisguiseProjection {
            Objects.requireNonNull(subjectId, "subjectId");
            PayloadCodecSupport.validateText(kind, 16);
            PayloadCodecSupport.validateText(reference, 128);
            PayloadCodecSupport.validateText(profileName, 16);
            PayloadCodecSupport.validateText(texturesValue, 4_096);
            PayloadCodecSupport.validateText(texturesSignature, 4_096);
            PayloadCodecSupport.validateText(labelMode, 32);
            PayloadCodecSupport.validateText(equipmentPolicy, 32);
            if (disguiseRevision < 1L) {
                throw new IllegalArgumentException("disguise projection is invalid");
            }
            if (profileId == null && (!profileName.isBlank()
                    || !texturesValue.isBlank()
                    || !texturesSignature.isBlank())
                    || texturesValue.isBlank() != texturesSignature.isBlank()) {
                throw new IllegalArgumentException("disguise profile projection is invalid");
            }
        }

        private void encode(FriendlyByteBuf buffer) {
            buffer.writeUUID(subjectId);
            buffer.writeVarLong(disguiseRevision);
            PayloadCodecSupport.writeString(buffer, kind, 16);
            PayloadCodecSupport.writeString(buffer, reference, 128);
            buffer.writeBoolean(profileId != null);
            if (profileId != null) {
                buffer.writeUUID(profileId);
            }
            PayloadCodecSupport.writeString(buffer, profileName, 16);
            PayloadCodecSupport.writeString(buffer, texturesValue, 4_096);
            PayloadCodecSupport.writeString(buffer, texturesSignature, 4_096);
            PayloadCodecSupport.writeString(buffer, labelMode, 32);
            PayloadCodecSupport.writeString(buffer, equipmentPolicy, 32);
            buffer.writeBoolean(traitsEnabled);
            buffer.writeBoolean(abilitiesEnabled);
        }

        private static DisguiseProjection decode(FriendlyByteBuf buffer) {
            UUID subjectId = buffer.readUUID();
            long revision = buffer.readVarLong();
            String kind = PayloadCodecSupport.readString(buffer, 16);
            String reference = PayloadCodecSupport.readString(buffer, 128);
            UUID profileId = buffer.readBoolean() ? buffer.readUUID() : null;
            return new DisguiseProjection(
                    subjectId,
                    revision,
                    kind,
                    reference,
                    profileId,
                    PayloadCodecSupport.readString(buffer, 16),
                    PayloadCodecSupport.readString(buffer, 4_096),
                    PayloadCodecSupport.readString(buffer, 4_096),
                    PayloadCodecSupport.readString(buffer, 32),
                    PayloadCodecSupport.readString(buffer, 32),
                    buffer.readBoolean(),
                    buffer.readBoolean());
        }
    }
}
