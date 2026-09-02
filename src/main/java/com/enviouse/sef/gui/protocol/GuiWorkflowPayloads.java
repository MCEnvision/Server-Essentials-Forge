package com.enviouse.sef.gui.protocol;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class GuiWorkflowPayloads {
    public static final int MAXIMUM_VARIANTS = 64;
    public static final int MAXIMUM_FIELDS = 24;
    public static final int MAXIMUM_SUGGESTIONS = 1_000;
    public static final int MAXIMUM_FIELD_VALUE = 32_768;
    public static final int MAXIMUM_BATCH_TARGETS = 1_000;
    public static final String PLAYER_SELECTION_ALL_ONLINE = "__sef_all_online__";
    public static final String PLAYER_SELECTION_ALL_KNOWN = "__sef_all_known__";

    private GuiWorkflowPayloads() {
    }

    private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> type(String path) {
        return new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("sef", path));
    }

    public record GuiWorkflowOpen(
            UUID sessionId,
            long sequence,
            String actionId
    ) implements CustomPacketPayload {
        public static final Type<GuiWorkflowOpen> TYPE = GuiWorkflowPayloads.type("play/gui_workflow_open");
        public static final StreamCodec<FriendlyByteBuf, GuiWorkflowOpen> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeVarLong(value.sequence());
                    PayloadCodecSupport.writeString(buffer, value.actionId(), 128);
                },
                buffer -> new GuiWorkflowOpen(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        PayloadCodecSupport.readString(buffer, 128)));

        public GuiWorkflowOpen {
            request(sessionId, sequence);
            text(actionId, 128);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record GuiWorkflowSnapshot(
            UUID sessionId,
            UUID workflowId,
            long revision,
            String actionId,
            String panelId,
            String title,
            String status,
            String routePreview,
            List<WorkflowVariant> variants,
            String selectedVariantId,
            boolean destructive,
            boolean previewValid,
            boolean confirmationPending,
            String confirmationToken
    ) implements CustomPacketPayload {
        public static final Type<GuiWorkflowSnapshot> TYPE =
                GuiWorkflowPayloads.type("play/gui_workflow_snapshot");
        public static final StreamCodec<FriendlyByteBuf, GuiWorkflowSnapshot> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeUUID(value.workflowId());
                    buffer.writeVarLong(value.revision());
                    PayloadCodecSupport.writeString(buffer, value.actionId(), 128);
                    PayloadCodecSupport.writeString(buffer, value.panelId(), 64);
                    PayloadCodecSupport.writeString(buffer, value.title(), 128);
                    PayloadCodecSupport.writeString(buffer, value.status(), 1024);
                    PayloadCodecSupport.writeString(buffer, value.routePreview(), 4096);
                    PayloadCodecSupport.writeList(
                            buffer,
                            value.variants(),
                            MAXIMUM_VARIANTS,
                            WorkflowVariant::encode);
                    PayloadCodecSupport.writeString(buffer, value.selectedVariantId(), 64);
                    buffer.writeBoolean(value.destructive());
                    buffer.writeBoolean(value.previewValid());
                    buffer.writeBoolean(value.confirmationPending());
                    PayloadCodecSupport.writeString(buffer, value.confirmationToken(), 64);
                },
                buffer -> new GuiWorkflowSnapshot(
                        buffer.readUUID(),
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        PayloadCodecSupport.readString(buffer, 128),
                        PayloadCodecSupport.readString(buffer, 64),
                        PayloadCodecSupport.readString(buffer, 128),
                        PayloadCodecSupport.readString(buffer, 1024),
                        PayloadCodecSupport.readString(buffer, 4096),
                        PayloadCodecSupport.readList(buffer, MAXIMUM_VARIANTS, WorkflowVariant::decode),
                        PayloadCodecSupport.readString(buffer, 64),
                        buffer.readBoolean(),
                        buffer.readBoolean(),
                        buffer.readBoolean(),
                        PayloadCodecSupport.readString(buffer, 64)));

        public GuiWorkflowSnapshot {
            Objects.requireNonNull(sessionId, "sessionId");
            Objects.requireNonNull(workflowId, "workflowId");
            text(actionId, 128);
            text(panelId, 64);
            text(title, 128);
            text(status, 1024);
            text(routePreview, 4096);
            text(selectedVariantId, 64);
            text(confirmationToken, 64);
            variants = boundedList(variants, MAXIMUM_VARIANTS, "workflow variants");
            if (revision < 1L || variants.isEmpty()
                    || variants.stream().noneMatch(variant -> variant.id().equals(selectedVariantId))) {
                throw new IllegalArgumentException("Workflow snapshot is invalid");
            }
            if (confirmationPending != !confirmationToken.isBlank()) {
                throw new IllegalArgumentException("Workflow confirmation state is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record GuiWorkflowFieldUpdate(
            UUID sessionId,
            long sequence,
            UUID workflowId,
            long expectedRevision,
            String variantId,
            String fieldId,
            String value
    ) implements CustomPacketPayload {
        public static final Type<GuiWorkflowFieldUpdate> TYPE =
                GuiWorkflowPayloads.type("play/gui_workflow_field_update");
        public static final StreamCodec<FriendlyByteBuf, GuiWorkflowFieldUpdate> CODEC = StreamCodec.of(
                (buffer, request) -> {
                    requestHeader(
                            buffer,
                            request.sessionId(),
                            request.sequence(),
                            request.workflowId(),
                            request.expectedRevision());
                    PayloadCodecSupport.writeString(buffer, request.variantId(), 64);
                    PayloadCodecSupport.writeString(buffer, request.fieldId(), 64);
                    PayloadCodecSupport.writeString(buffer, request.value(), 4096);
                },
                buffer -> new GuiWorkflowFieldUpdate(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        PayloadCodecSupport.readString(buffer, 64),
                        PayloadCodecSupport.readString(buffer, 64),
                        PayloadCodecSupport.readString(buffer, 4096)));

        public GuiWorkflowFieldUpdate {
            workflowRequest(sessionId, sequence, workflowId, expectedRevision);
            text(variantId, 64);
            text(fieldId, 64);
            text(value, 4096);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record GuiWorkflowSuggestionsRequest(
            UUID sessionId,
            long sequence,
            UUID workflowId,
            long expectedRevision,
            String variantId,
            String fieldId,
            String value,
            UUID requestId
    ) implements CustomPacketPayload {
        public static final Type<GuiWorkflowSuggestionsRequest> TYPE =
                GuiWorkflowPayloads.type("play/gui_workflow_suggestions_request");
        public static final StreamCodec<FriendlyByteBuf, GuiWorkflowSuggestionsRequest> CODEC = StreamCodec.of(
                (buffer, request) -> {
                    requestHeader(
                            buffer,
                            request.sessionId(),
                            request.sequence(),
                            request.workflowId(),
                            request.expectedRevision());
                    PayloadCodecSupport.writeString(buffer, request.variantId(), 64);
                    PayloadCodecSupport.writeString(buffer, request.fieldId(), 64);
                    PayloadCodecSupport.writeString(buffer, request.value(), 4096);
                    buffer.writeUUID(request.requestId());
                },
                buffer -> new GuiWorkflowSuggestionsRequest(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        PayloadCodecSupport.readString(buffer, 64),
                        PayloadCodecSupport.readString(buffer, 64),
                        PayloadCodecSupport.readString(buffer, 4096),
                        buffer.readUUID()));

        public GuiWorkflowSuggestionsRequest {
            workflowRequest(sessionId, sequence, workflowId, expectedRevision);
            text(variantId, 64);
            text(fieldId, 64);
            text(value, 4096);
            Objects.requireNonNull(requestId, "requestId");
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record GuiWorkflowSuggestions(
            UUID sessionId,
            UUID workflowId,
            long revision,
            UUID requestId,
            String fieldId,
            List<WorkflowSuggestion> suggestions
    ) implements CustomPacketPayload {
        public static final Type<GuiWorkflowSuggestions> TYPE =
                GuiWorkflowPayloads.type("play/gui_workflow_suggestions");
        public static final StreamCodec<FriendlyByteBuf, GuiWorkflowSuggestions> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeUUID(value.workflowId());
                    buffer.writeVarLong(value.revision());
                    buffer.writeUUID(value.requestId());
                    PayloadCodecSupport.writeString(buffer, value.fieldId(), 64);
                    PayloadCodecSupport.writeList(
                            buffer,
                            value.suggestions(),
                            MAXIMUM_SUGGESTIONS,
                            WorkflowSuggestion::encode);
                },
                buffer -> new GuiWorkflowSuggestions(
                        buffer.readUUID(),
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        buffer.readUUID(),
                        PayloadCodecSupport.readString(buffer, 64),
                        PayloadCodecSupport.readList(
                                buffer,
                                MAXIMUM_SUGGESTIONS,
                                WorkflowSuggestion::decode)));

        public GuiWorkflowSuggestions {
            Objects.requireNonNull(sessionId, "sessionId");
            Objects.requireNonNull(workflowId, "workflowId");
            Objects.requireNonNull(requestId, "requestId");
            text(fieldId, 64);
            suggestions = boundedList(suggestions, MAXIMUM_SUGGESTIONS, "workflow suggestions");
            if (revision < 1L) {
                throw new IllegalArgumentException("Workflow suggestion revision is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record WorkflowSuggestion(
            String value,
            String label,
            boolean online
    ) {
        public WorkflowSuggestion {
            text(value, 128);
            text(label, 128);
        }

        private static void encode(FriendlyByteBuf buffer, WorkflowSuggestion value) {
            PayloadCodecSupport.writeString(buffer, value.value(), 128);
            PayloadCodecSupport.writeString(buffer, value.label(), 128);
            buffer.writeBoolean(value.online());
        }

        private static WorkflowSuggestion decode(FriendlyByteBuf buffer) {
            return new WorkflowSuggestion(
                    PayloadCodecSupport.readString(buffer, 128),
                    PayloadCodecSupport.readString(buffer, 128),
                    buffer.readBoolean());
        }
    }

    public record GuiWorkflowPreview(
            UUID sessionId,
            long sequence,
            UUID workflowId,
            long expectedRevision,
            String variantId,
            List<WorkflowFieldValue> fields
    ) implements CustomPacketPayload, RequestWithFields {
        public static final Type<GuiWorkflowPreview> TYPE =
                GuiWorkflowPayloads.type("play/gui_workflow_preview");
        public static final StreamCodec<FriendlyByteBuf, GuiWorkflowPreview> CODEC = requestWithFieldsCodec(
                GuiWorkflowPreview::new);

        public GuiWorkflowPreview {
            workflowRequest(sessionId, sequence, workflowId, expectedRevision);
            text(variantId, 64);
            fields = boundedList(fields, MAXIMUM_FIELDS, "workflow fields");
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record GuiWorkflowSubmit(
            UUID sessionId,
            long sequence,
            UUID workflowId,
            long expectedRevision,
            String variantId,
            List<WorkflowFieldValue> fields
    ) implements CustomPacketPayload, RequestWithFields {
        public static final Type<GuiWorkflowSubmit> TYPE =
                GuiWorkflowPayloads.type("play/gui_workflow_submit");
        public static final StreamCodec<FriendlyByteBuf, GuiWorkflowSubmit> CODEC = requestWithFieldsCodec(
                GuiWorkflowSubmit::new);

        public GuiWorkflowSubmit {
            workflowRequest(sessionId, sequence, workflowId, expectedRevision);
            text(variantId, 64);
            fields = boundedList(fields, MAXIMUM_FIELDS, "workflow fields");
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record GuiWorkflowConfirmation(
            UUID sessionId,
            long sequence,
            UUID workflowId,
            long expectedRevision,
            String confirmationToken
    ) implements CustomPacketPayload {
        public static final Type<GuiWorkflowConfirmation> TYPE =
                GuiWorkflowPayloads.type("play/gui_workflow_confirmation");
        public static final StreamCodec<FriendlyByteBuf, GuiWorkflowConfirmation> CODEC = StreamCodec.of(
                (buffer, request) -> {
                    requestHeader(
                            buffer,
                            request.sessionId(),
                            request.sequence(),
                            request.workflowId(),
                            request.expectedRevision());
                    PayloadCodecSupport.writeString(buffer, request.confirmationToken(), 64);
                },
                buffer -> new GuiWorkflowConfirmation(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        PayloadCodecSupport.readString(buffer, 64)));

        public GuiWorkflowConfirmation {
            workflowRequest(sessionId, sequence, workflowId, expectedRevision);
            text(confirmationToken, 64);
            if (confirmationToken.isBlank()) {
                throw new IllegalArgumentException("Workflow confirmation token is empty");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record GuiWorkflowResult(
            UUID sessionId,
            UUID workflowId,
            long revision,
            boolean successful,
            boolean closed,
            String status,
            String returnPanel
    ) implements CustomPacketPayload {
        public static final Type<GuiWorkflowResult> TYPE =
                GuiWorkflowPayloads.type("play/gui_workflow_result");
        public static final StreamCodec<FriendlyByteBuf, GuiWorkflowResult> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeUUID(value.workflowId());
                    buffer.writeVarLong(value.revision());
                    buffer.writeBoolean(value.successful());
                    buffer.writeBoolean(value.closed());
                    PayloadCodecSupport.writeString(buffer, value.status(), 1024);
                    PayloadCodecSupport.writeString(buffer, value.returnPanel(), 64);
                },
                buffer -> new GuiWorkflowResult(
                        buffer.readUUID(),
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        buffer.readBoolean(),
                        buffer.readBoolean(),
                        PayloadCodecSupport.readString(buffer, 1024),
                        PayloadCodecSupport.readString(buffer, 64)));

        public GuiWorkflowResult {
            Objects.requireNonNull(sessionId, "sessionId");
            Objects.requireNonNull(workflowId, "workflowId");
            text(status, 1024);
            text(returnPanel, 64);
            if (revision < 1L) {
                throw new IllegalArgumentException("Workflow result revision is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record GuiWorkflowProgress(
            UUID sessionId,
            UUID workflowId,
            long revision,
            int percent,
            String stage
    ) implements CustomPacketPayload {
        public static final Type<GuiWorkflowProgress> TYPE =
                GuiWorkflowPayloads.type("play/gui_workflow_progress");
        public static final StreamCodec<FriendlyByteBuf, GuiWorkflowProgress> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeUUID(value.workflowId());
                    buffer.writeVarLong(value.revision());
                    buffer.writeVarInt(value.percent());
                    PayloadCodecSupport.writeString(buffer, value.stage(), 256);
                },
                buffer -> new GuiWorkflowProgress(
                        buffer.readUUID(),
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        buffer.readVarInt(),
                        PayloadCodecSupport.readString(buffer, 256)));

        public GuiWorkflowProgress {
            Objects.requireNonNull(sessionId, "sessionId");
            Objects.requireNonNull(workflowId, "workflowId");
            text(stage, 256);
            if (revision < 1L || percent < 0 || percent > 100) {
                throw new IllegalArgumentException("Workflow progress is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record GuiWorkflowInvalidate(
            UUID sessionId,
            UUID workflowId,
            long revision,
            String reason
    ) implements CustomPacketPayload {
        public static final Type<GuiWorkflowInvalidate> TYPE =
                GuiWorkflowPayloads.type("play/gui_workflow_invalidate");
        public static final StreamCodec<FriendlyByteBuf, GuiWorkflowInvalidate> CODEC = StreamCodec.of(
                (buffer, value) -> {
                    buffer.writeUUID(value.sessionId());
                    buffer.writeUUID(value.workflowId());
                    buffer.writeVarLong(value.revision());
                    PayloadCodecSupport.writeString(buffer, value.reason(), 1024);
                },
                buffer -> new GuiWorkflowInvalidate(
                        buffer.readUUID(),
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        PayloadCodecSupport.readString(buffer, 1024)));

        public GuiWorkflowInvalidate {
            Objects.requireNonNull(sessionId, "sessionId");
            Objects.requireNonNull(workflowId, "workflowId");
            text(reason, 1024);
            if (revision < 1L) {
                throw new IllegalArgumentException("Workflow invalidation revision is invalid");
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record GuiWorkflowClose(
            UUID sessionId,
            long sequence,
            UUID workflowId,
            long expectedRevision
    ) implements CustomPacketPayload {
        public static final Type<GuiWorkflowClose> TYPE =
                GuiWorkflowPayloads.type("play/gui_workflow_close");
        public static final StreamCodec<FriendlyByteBuf, GuiWorkflowClose> CODEC = StreamCodec.of(
                (buffer, request) -> requestHeader(
                        buffer,
                        request.sessionId(),
                        request.sequence(),
                        request.workflowId(),
                        request.expectedRevision()),
                buffer -> new GuiWorkflowClose(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        buffer.readUUID(),
                        buffer.readVarLong()));

        public GuiWorkflowClose {
            workflowRequest(sessionId, sequence, workflowId, expectedRevision);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record WorkflowVariant(
            String id,
            String label,
            List<WorkflowField> fields
    ) {
        public WorkflowVariant {
            text(id, 64);
            text(label, 256);
            fields = boundedList(fields, MAXIMUM_FIELDS, "workflow variant fields");
        }

        private static void encode(FriendlyByteBuf buffer, WorkflowVariant value) {
            PayloadCodecSupport.writeString(buffer, value.id(), 64);
            PayloadCodecSupport.writeString(buffer, value.label(), 256);
            PayloadCodecSupport.writeList(buffer, value.fields(), MAXIMUM_FIELDS, WorkflowField::encode);
        }

        private static WorkflowVariant decode(FriendlyByteBuf buffer) {
            return new WorkflowVariant(
                    PayloadCodecSupport.readString(buffer, 64),
                    PayloadCodecSupport.readString(buffer, 256),
                    PayloadCodecSupport.readList(buffer, MAXIMUM_FIELDS, WorkflowField::decode));
        }
    }

    public record WorkflowField(
            String id,
            String label,
            String type,
            String renderMode,
            boolean required,
            double minimum,
            double maximum,
            int maximumLength,
            String value,
            List<String> choices,
            String suggestionKind
    ) {
        public WorkflowField {
            text(id, 64);
            text(label, 128);
            text(type, 32);
            text(renderMode, 32);
            text(value, MAXIMUM_FIELD_VALUE);
            text(suggestionKind, 32);
            choices = boundedList(choices, MAXIMUM_SUGGESTIONS, "workflow field choices");
            choices.forEach(choice -> text(choice, 128));
            if (!Double.isFinite(minimum) || !Double.isFinite(maximum)
                    || minimum > maximum || maximumLength < 1 || maximumLength > MAXIMUM_FIELD_VALUE
                    || value.length() > maximumLength) {
                throw new IllegalArgumentException("Workflow field is invalid");
            }
        }

        private static void encode(FriendlyByteBuf buffer, WorkflowField value) {
            PayloadCodecSupport.writeString(buffer, value.id(), 64);
            PayloadCodecSupport.writeString(buffer, value.label(), 128);
            PayloadCodecSupport.writeString(buffer, value.type(), 32);
            PayloadCodecSupport.writeString(buffer, value.renderMode(), 32);
            buffer.writeBoolean(value.required());
            buffer.writeDouble(value.minimum());
            buffer.writeDouble(value.maximum());
            buffer.writeVarInt(value.maximumLength());
            PayloadCodecSupport.writeString(buffer, value.value(), MAXIMUM_FIELD_VALUE);
            PayloadCodecSupport.writeList(
                    buffer,
                    value.choices(),
                    MAXIMUM_SUGGESTIONS,
                    (target, entry) -> PayloadCodecSupport.writeString(target, entry, 128));
            PayloadCodecSupport.writeString(buffer, value.suggestionKind(), 32);
        }

        private static WorkflowField decode(FriendlyByteBuf buffer) {
            return new WorkflowField(
                    PayloadCodecSupport.readString(buffer, 64),
                    PayloadCodecSupport.readString(buffer, 128),
                    PayloadCodecSupport.readString(buffer, 32),
                    PayloadCodecSupport.readString(buffer, 32),
                    buffer.readBoolean(),
                    buffer.readDouble(),
                    buffer.readDouble(),
                    buffer.readVarInt(),
                    PayloadCodecSupport.readString(buffer, MAXIMUM_FIELD_VALUE),
                    PayloadCodecSupport.readList(
                            buffer,
                            MAXIMUM_SUGGESTIONS,
                            target -> PayloadCodecSupport.readString(target, 128)),
                    PayloadCodecSupport.readString(buffer, 32));
        }
    }

    public record WorkflowFieldValue(String id, String value) {
        public WorkflowFieldValue {
            text(id, 64);
            text(value, MAXIMUM_FIELD_VALUE);
        }

        private static void encode(FriendlyByteBuf buffer, WorkflowFieldValue value) {
            PayloadCodecSupport.writeString(buffer, value.id(), 64);
            PayloadCodecSupport.writeString(buffer, value.value(), MAXIMUM_FIELD_VALUE);
        }

        private static WorkflowFieldValue decode(FriendlyByteBuf buffer) {
            return new WorkflowFieldValue(
                    PayloadCodecSupport.readString(buffer, 64),
                    PayloadCodecSupport.readString(buffer, MAXIMUM_FIELD_VALUE));
        }
    }

    private static <T extends CustomPacketPayload> StreamCodec<FriendlyByteBuf, T> requestWithFieldsCodec(
            RequestWithFieldsFactory<T> factory
    ) {
        return StreamCodec.of(
                (buffer, payload) -> {
                    RequestWithFields request = (RequestWithFields) payload;
                    requestHeader(
                            buffer,
                            request.sessionId(),
                            request.sequence(),
                            request.workflowId(),
                            request.expectedRevision());
                    PayloadCodecSupport.writeString(buffer, request.variantId(), 64);
                    PayloadCodecSupport.writeList(
                            buffer,
                            request.fields(),
                            MAXIMUM_FIELDS,
                            WorkflowFieldValue::encode);
                },
                buffer -> factory.create(
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        buffer.readUUID(),
                        buffer.readVarLong(),
                        PayloadCodecSupport.readString(buffer, 64),
                        PayloadCodecSupport.readList(buffer, MAXIMUM_FIELDS, WorkflowFieldValue::decode)));
    }

    private static void requestHeader(
            FriendlyByteBuf buffer,
            UUID sessionId,
            long sequence,
            UUID workflowId,
            long expectedRevision
    ) {
        buffer.writeUUID(sessionId);
        buffer.writeVarLong(sequence);
        buffer.writeUUID(workflowId);
        buffer.writeVarLong(expectedRevision);
    }

    private static void request(UUID sessionId, long sequence) {
        Objects.requireNonNull(sessionId, "sessionId");
        if (sequence < 1L) {
            throw new IllegalArgumentException("Workflow request sequence is invalid");
        }
    }

    private static void workflowRequest(
            UUID sessionId,
            long sequence,
            UUID workflowId,
            long expectedRevision
    ) {
        request(sessionId, sequence);
        Objects.requireNonNull(workflowId, "workflowId");
        if (expectedRevision < 1L) {
            throw new IllegalArgumentException("Workflow request revision is invalid");
        }
    }

    private static void text(String value, int maximum) {
        PayloadCodecSupport.validateText(Objects.requireNonNull(value, "value"), maximum);
    }

    private static <T> List<T> boundedList(List<T> values, int maximum, String name) {
        List<T> copy = List.copyOf(Objects.requireNonNull(values, name));
        if (copy.size() > maximum) {
            throw new IllegalArgumentException(name + " exceed hard bounds");
        }
        return copy;
    }

    private interface RequestWithFields {
        UUID sessionId();

        long sequence();

        UUID workflowId();

        long expectedRevision();

        String variantId();

        List<WorkflowFieldValue> fields();
    }

    private interface RequestWithFieldsFactory<T> {
        T create(
                UUID sessionId,
                long sequence,
                UUID workflowId,
                long expectedRevision,
                String variantId,
                List<WorkflowFieldValue> fields
        );
    }
}
