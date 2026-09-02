package com.enviouse.sef.gui.client;

import com.enviouse.sef.gui.protocol.SefPayloads;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SefControlEditorScreen extends SefScreen {
    private static final int PANEL_WIDTH = 368;
    private static final int PANEL_HEIGHT = 270;
    private static final int FIELDS_PER_PAGE = 5;

    private final Screen parent;
    private final SefPayloads.ControlEditorSnapshot snapshot;
    private final Map<String, String> draft = new LinkedHashMap<>();
    private final List<FieldWidget> visibleFields = new ArrayList<>();
    private String draftTitle;
    private String draftDetails;
    private int fieldPage;
    private int selectedState;
    private EditBox titleInput;
    private EditBox detailsInput;

    public SefControlEditorScreen(Screen parent, SefPayloads.ControlEditorSnapshot snapshot) {
        super(Component.literal(snapshot.featureId().replace('_', ' ')));
        this.parent = parent instanceof SefControlEditorScreen editor ? editor.parent : parent;
        this.snapshot = snapshot;
        this.draftTitle = snapshot.title();
        this.draftDetails = snapshot.details();
        snapshot.fields().forEach(field -> draft.put(field.id(), field.value()));
        this.selectedState = Math.max(0, snapshot.states().indexOf(snapshot.state()));
    }

    @Override
    protected void init() {
        visibleFields.clear();
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;

        titleInput = input(left + 82, top + 28, 270, 18, 128, draftTitle, "title");
        detailsInput = input(left + 82, top + 50, 270, 18, 4096, draftDetails, "details");

        int start = fieldPage * FIELDS_PER_PAGE;
        int end = Math.min(snapshot.fields().size(), start + FIELDS_PER_PAGE);
        for (int index = start; index < end; index++) {
            SefPayloads.ControlField field = snapshot.fields().get(index);
            int y = top + 78 + (index - start) * 27;
            if (field.type().equals("boolean") || field.type().equals("enum")) {
                List<String> choices = field.type().equals("boolean")
                        ? List.of("false", "true")
                        : field.choices();
                String current = draft.getOrDefault(field.id(), "");
                Button button = Button.builder(
                                Component.literal(choiceLabel(current, field.required())),
                                ignored -> {
                                    int choice = choices.indexOf(draft.getOrDefault(field.id(), ""));
                                    int next = Math.floorMod(choice + 1, choices.size() + (field.required() ? 0 : 1));
                                    String value = !field.required() && next == choices.size()
                                            ? ""
                                            : choices.get(next);
                                    draft.put(field.id(), value);
                                    ignored.setMessage(Component.literal(choiceLabel(value, field.required())));
                                })
                        .bounds(left + 150, y, 202, 20)
                        .tooltip(Tooltip.create(fieldTooltip(field)))
                        .build();
                addRenderableWidget(button);
                visibleFields.add(new FieldWidget(field, null));
            } else {
                int maximumLength = field.type().equals("text")
                        ? (int) Math.min(4096L, Math.max(1L, field.maximum()))
                        : 4096;
                EditBox input = input(
                        left + 150,
                        y,
                        202,
                        20,
                        maximumLength,
                        draft.getOrDefault(field.id(), ""),
                        field.id());
                input.setHint(Component.literal(field.required() ? "required" : "optional"));
                input.setTooltip(Tooltip.create(fieldTooltip(field)));
                visibleFields.add(new FieldWidget(field, input));
            }
        }

        int pages = fieldPages();
        Button previous = Button.builder(Component.literal("<"), ignored -> {
                    capture();
                    fieldPage = Math.max(0, fieldPage - 1);
                    rebuildWidgets();
                })
                .bounds(left + 12, top + 224, 24, 18)
                .build();
        previous.active = fieldPage > 0;
        addRenderableWidget(previous);
        Button next = Button.builder(Component.literal(">"), ignored -> {
                    capture();
                    fieldPage = Math.min(pages - 1, fieldPage + 1);
                    rebuildWidgets();
                })
                .bounds(left + 40, top + 224, 24, 18)
                .build();
        next.active = fieldPage + 1 < pages;
        addRenderableWidget(next);

        Button state = Button.builder(
                        Component.literal("state, " + selectedState()),
                        ignored -> {
                            if (!snapshot.states().isEmpty()) {
                                selectedState = (selectedState + 1) % snapshot.states().size();
                                ignored.setMessage(Component.literal("state, " + selectedState()));
                            }
                        })
                .bounds(left + 70, top + 224, 92, 18)
                .build();
        state.active = snapshot.operations().contains("configure") && !snapshot.states().isEmpty();
        addRenderableWidget(state);

        Button applyState = Button.builder(
                        Component.literal("apply"),
                        ignored -> mutate("transition", selectedState(), List.of()))
                .bounds(left + 166, top + 224, 42, 18)
                .build();
        applyState.active = snapshot.operations().contains("configure") && !snapshot.states().isEmpty();
        addRenderableWidget(applyState);

        Button preview = Button.builder(
                        Component.literal("preview"),
                        ignored -> mutate("preview", "", List.of()))
                .bounds(left + 212, top + 224, 64, 18)
                .build();
        preview.active = snapshot.operations().contains("preview");
        addRenderableWidget(preview);

        Button execute = Button.builder(
                        Component.literal(snapshot.confirmationPending() ? "confirm" : "execute"),
                        ignored -> mutate("execute", "", List.of()))
                .bounds(left + 280, top + 224, 72, 18)
                .tooltip(Tooltip.create(Component.literal(snapshot.confirmationRequired()
                        ? "this action requires a second confirmation click"
                        : "execute this server authoritative workflow")))
                .build();
        execute.active = snapshot.operations().contains("execute");
        addRenderableWidget(execute);

        Button save = Button.builder(
                        Component.literal("save changes"),
                        ignored -> {
                            capture();
                            mutate(
                                    "save",
                                    "",
                                    snapshot.fields().stream()
                                            .map(field -> new SefPayloads.ControlFieldValue(
                                                    field.id(),
                                                    draft.getOrDefault(field.id(), "")))
                                            .toList());
                        })
                .bounds(left + 90, top + 247, 100, 18)
                .build();
        save.active = snapshot.operations().contains("configure");
        addRenderableWidget(save);
        addRenderableWidget(Button.builder(
                        Component.literal("back"),
                        ignored -> SefClientTransport.open("control:" + snapshot.featureId(), 1, ""))
                .bounds(left + 194, top + 247, 76, 18)
                .build());
        setInitialFocus(titleInput);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        SefScreenBackground.render(this, graphics, mouseX, mouseY, partialTick);
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        SefVanillaTheme.panel(
                graphics,
                left,
                top,
                PANEL_WIDTH,
                PANEL_HEIGHT,
                SefPayloads.PanelView.FORM);
        graphics.drawCenteredString(
                font,
                title.getString() + ", " + snapshot.state(),
                width / 2,
                top + 9,
                SefVanillaTheme.TEXT);
        graphics.drawString(font, "title", left + 12, top + 34, SefVanillaTheme.MUTED_TEXT);
        graphics.drawString(font, "details", left + 12, top + 56, SefVanillaTheme.MUTED_TEXT);
        for (int index = 0; index < visibleFields.size(); index++) {
            SefPayloads.ControlField field = visibleFields.get(index).field();
            String label = field.id().replace('_', ' ') + (field.required() ? " *" : "");
            graphics.drawString(
                    font,
                    fit(label, 130),
                    left + 12,
                    top + 84 + index * 27,
                    field.required() ? SefVanillaTheme.TEXT : SefVanillaTheme.MUTED_TEXT);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(
                font,
                fit(snapshot.status(), PANEL_WIDTH - 24),
                width / 2,
                top + 210,
                snapshot.confirmationPending() ? 0xffffaa00 : SefVanillaTheme.MUTED_TEXT);
        graphics.drawString(
                font,
                "fields " + (fieldPage + 1) + " of " + fieldPages(),
                left + 12,
                top + 249,
                SefVanillaTheme.MUTED_TEXT);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public Component getNarrationMessage() {
        return Component.literal(title.getString() + ". " + snapshot.status());
    }

    private EditBox input(
            int x,
            int y,
            int width,
            int height,
            int maximumLength,
            String value,
            String label
    ) {
        EditBox input = new EditBox(font, x, y, width, height, Component.literal(label));
        input.setMaxLength(maximumLength);
        input.setValue(value);
        addRenderableWidget(input);
        return input;
    }

    private void capture() {
        draftTitle = titleInput.getValue();
        draftDetails = detailsInput.getValue();
        for (FieldWidget field : visibleFields) {
            if (field.input() != null) {
                draft.put(field.field().id(), field.input().getValue());
            }
        }
    }

    private void mutate(
            String operation,
            String argument,
            List<SefPayloads.ControlFieldValue> fields
    ) {
        capture();
        SefClientTransport.mutateControl(
                snapshot,
                operation,
                draftTitle,
                draftDetails,
                argument,
                fields);
    }

    private int fieldPages() {
        return Math.max(1, (snapshot.fields().size() + FIELDS_PER_PAGE - 1) / FIELDS_PER_PAGE);
    }

    private String selectedState() {
        return snapshot.states().isEmpty()
                ? snapshot.state()
                : snapshot.states().get(Math.min(selectedState, snapshot.states().size() - 1));
    }

    private String fit(String value, int maximumPixels) {
        if (font.width(value) <= maximumPixels) {
            return value;
        }
        int ellipsisWidth = font.width("…");
        return font.plainSubstrByWidth(value, Math.max(0, maximumPixels - ellipsisWidth)) + "…";
    }

    private static String choiceLabel(String value, boolean required) {
        return value.isBlank() && !required ? "unset" : value.toLowerCase(Locale.ROOT);
    }

    private static Component fieldTooltip(SefPayloads.ControlField field) {
        String range = switch (field.type()) {
            case "integer", "decimal", "duration_seconds" ->
                    ", range " + field.minimum() + " to " + field.maximum();
            default -> "";
        };
        String choices = field.choices().isEmpty()
                ? ""
                : ", choices " + String.join(", ", field.choices());
        return Component.literal(field.type() + range + choices);
    }

    private record FieldWidget(SefPayloads.ControlField field, EditBox input) {
    }
}
