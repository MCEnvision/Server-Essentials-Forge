package com.enviouse.sef.gui.client;

import com.enviouse.sef.gui.protocol.GuiWorkflowPayloads;
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
import java.util.UUID;

public final class SefWorkflowScreen extends Screen {
    private static final int PANEL_WIDTH = 408;
    private static final int PANEL_HEIGHT = 292;
    private static final int FIELDS_PER_PAGE = 6;

    private final Screen parent;
    private final GuiWorkflowPayloads.GuiWorkflowSnapshot snapshot;
    private final Map<String, Map<String, String>> drafts = new LinkedHashMap<>();
    private final Map<String, List<GuiWorkflowPayloads.WorkflowSuggestion>> suggestions =
            new LinkedHashMap<>();
    private final List<FieldWidget> visibleFields = new ArrayList<>();
    private int selectedVariant;
    private int fieldPage;
    private String liveStatus;
    private int progress = -1;
    private boolean closed;

    public SefWorkflowScreen(Screen parent, GuiWorkflowPayloads.GuiWorkflowSnapshot snapshot) {
        super(Component.literal(snapshot.title()));
        this.parent = parent instanceof SefWorkflowScreen workflow ? workflow.parent : parent;
        this.snapshot = snapshot;
        this.selectedVariant = Math.max(0, variantIndex(snapshot.selectedVariantId()));
        this.liveStatus = snapshot.status();
        for (GuiWorkflowPayloads.WorkflowVariant variant : snapshot.variants()) {
            Map<String, String> values = new LinkedHashMap<>();
            variant.fields().forEach(field -> values.put(field.id(), field.value()));
            drafts.put(variant.id(), values);
        }
    }

    @Override
    protected void init() {
        visibleFields.clear();
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        GuiWorkflowPayloads.WorkflowVariant variant = selectedVariant();

        Button variantButton = Button.builder(
                        Component.literal("mode, " + fit(variantLabel(variant), 210)),
                        ignored -> {
                            capture();
                            selectedVariant = (selectedVariant + 1) % snapshot.variants().size();
                            fieldPage = 0;
                            liveStatus = "Variant changed. Complete every required field.";
                            rebuildWidgets();
                        })
                .bounds(left + 112, top + 27, 284, 20)
                .tooltip(Tooltip.create(Component.literal(
                        snapshot.variants().size() + " server compiled command variants")))
                .build();
        variantButton.active = snapshot.variants().size() > 1 && !snapshot.confirmationPending();
        addRenderableWidget(variantButton);

        int start = fieldPage * FIELDS_PER_PAGE;
        int end = Math.min(variant.fields().size(), start + FIELDS_PER_PAGE);
        for (int index = start; index < end; index++) {
            GuiWorkflowPayloads.WorkflowField field = variant.fields().get(index);
            int y = top + 58 + (index - start) * 29;
            String value = values().getOrDefault(field.id(), "");
            if (field.type().equals("boolean") || !field.choices().isEmpty()) {
                List<String> choices = field.choices().isEmpty()
                        ? List.of("false", "true")
                        : field.choices();
                Button choice = Button.builder(
                                Component.literal(value.isBlank() ? "select" : fit(value, 170)),
                                ignored -> {
                                    int current = choices.indexOf(values().getOrDefault(field.id(), ""));
                                    String replacement = choices.get(Math.floorMod(current + 1, choices.size()));
                                    values().put(field.id(), replacement);
                                    ignored.setMessage(Component.literal(fit(replacement, 170)));
                                    liveStatus = "Field values changed. Preview again before execution.";
                                })
                        .bounds(left + 152, y, 244, 20)
                        .tooltip(Tooltip.create(fieldTooltip(field)))
                        .build();
                choice.active = !snapshot.confirmationPending();
                addRenderableWidget(choice);
                visibleFields.add(new FieldWidget(field, null));
                continue;
            }
            if (field.type().equals("player") || field.type().equals("players")) {
                Button selectPlayers = Button.builder(
                                Component.literal(playerSelectionLabel(value)),
                                ignored -> openPlayerPicker(field))
                        .bounds(left + 152, y, 244, 20)
                        .tooltip(Tooltip.create(Component.literal(
                                field.type().equals("players")
                                        ? "select players, all online, or every known profile"
                                        : "select one server known player")))
                        .build();
                selectPlayers.active = !snapshot.confirmationPending();
                addRenderableWidget(selectPlayers);
                visibleFields.add(new FieldWidget(field, null));
                continue;
            }
            if (field.type().equals("item")) {
                Button selectItem = Button.builder(
                                Component.literal(value.isBlank()
                                        ? "select item"
                                        : fit(value, 220)),
                                ignored -> openItemPicker(field))
                        .bounds(left + 152, y, 244, 20)
                        .tooltip(Tooltip.create(Component.literal(
                                "browse searchable creative style item tabs")))
                        .build();
                selectItem.active = !snapshot.confirmationPending();
                addRenderableWidget(selectItem);
                visibleFields.add(new FieldWidget(field, null));
                continue;
            }
            if (field.type().equals("integer")) {
                addIntegerControls(field, value, left + 152, y);
                visibleFields.add(new FieldWidget(field, null));
                continue;
            }

            EditBox input = new EditBox(
                    font,
                    left + 152,
                    y,
                    194,
                    20,
                    Component.literal(field.label()));
            input.setMaxLength(field.maximumLength());
            input.setValue(value);
            input.setHint(Component.literal("required " + field.type().replace('_', ' ')));
            input.setTooltip(Tooltip.create(fieldTooltip(field)));
            input.setEditable(!snapshot.confirmationPending());
            addRenderableWidget(input);
            visibleFields.add(new FieldWidget(field, input));

            Button suggest = Button.builder(
                            Component.literal(suggestions.containsKey(field.id()) ? "pick" : "find"),
                            ignored -> chooseOrRequest(field, input))
                    .bounds(left + 350, y, 46, 20)
                    .tooltip(Tooltip.create(Component.literal(
                            "request server authoritative suggestions for this typed field")))
                    .build();
            suggest.active = !snapshot.confirmationPending()
                    && !field.suggestionKind().isBlank();
            addRenderableWidget(suggest);
        }

        int pages = fieldPages(variant);
        Button previous = Button.builder(Component.literal("<"), ignored -> {
                    capture();
                    fieldPage = Math.max(0, fieldPage - 1);
                    rebuildWidgets();
                })
                .bounds(left + 12, top + 250, 24, 18)
                .build();
        previous.active = fieldPage > 0 && !snapshot.confirmationPending();
        addRenderableWidget(previous);
        Button next = Button.builder(Component.literal(">"), ignored -> {
                    capture();
                    fieldPage = Math.min(pages - 1, fieldPage + 1);
                    rebuildWidgets();
                })
                .bounds(left + 40, top + 250, 24, 18)
                .build();
        next.active = fieldPage + 1 < pages && !snapshot.confirmationPending();
        addRenderableWidget(next);

        Button preview = Button.builder(
                        Component.literal("preview"),
                        ignored -> {
                            capture();
                            SefClientTransport.previewWorkflow(
                                    snapshot,
                                    variant.id(),
                                    fieldValues(variant));
                        })
                .bounds(left + 140, top + 250, 64, 18)
                .build();
        preview.active = !snapshot.confirmationPending();
        addRenderableWidget(preview);

        Button execute = Button.builder(
                        Component.literal(snapshot.confirmationPending() ? "confirm and run" : "run"),
                        ignored -> {
                            capture();
                            if (snapshot.confirmationPending()) {
                                SefClientTransport.confirmWorkflow(snapshot);
                            } else {
                                SefClientTransport.submitWorkflow(
                                        snapshot,
                                        variant.id(),
                                        fieldValues(variant));
                            }
                        })
                .bounds(left + 208, top + 250, 104, 18)
                .tooltip(Tooltip.create(Component.literal(snapshot.confirmationPending()
                        ? "execute the exact preview after the second confirmation"
                        : "validate every field and execute through the canonical command")))
                .build();
        addRenderableWidget(execute);

        addRenderableWidget(Button.builder(
                        Component.literal("back"),
                        ignored -> onClose())
                .bounds(left + 316, top + 250, 80, 18)
                .build());
        visibleFields.stream()
                .map(FieldWidget::input)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .ifPresent(this::setInitialFocus);
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
                snapshot.confirmationPending()
                        ? SefPayloads.PanelView.CONFIRMATION
                        : SefPayloads.PanelView.FORM);
        graphics.drawCenteredString(
                font,
                fit(snapshot.title(), PANEL_WIDTH - 24),
                width / 2,
                top + 9,
                SefVanillaTheme.TEXT);
        graphics.drawString(font, "workflow mode", left + 12, top + 33, SefVanillaTheme.MUTED_TEXT);
        for (int index = 0; index < visibleFields.size(); index++) {
            GuiWorkflowPayloads.WorkflowField field = visibleFields.get(index).field();
            graphics.drawString(
                    font,
                    fit(field.label() + " *", 132),
                    left + 12,
                    top + 65 + index * 29,
                    SefVanillaTheme.TEXT);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        String preview = snapshot.routePreview().isBlank()
                ? "route preview pending"
                : "/" + snapshot.routePreview();
        graphics.drawString(
                font,
                fit(preview, PANEL_WIDTH - 24),
                left + 12,
                top + 223,
                snapshot.confirmationPending() ? 0xffffaa00 : SefVanillaTheme.MUTED_TEXT);
        graphics.drawCenteredString(
                font,
                fit(liveStatus, PANEL_WIDTH - 24),
                width / 2,
                top + 236,
                snapshot.confirmationPending() ? 0xffffaa00 : SefVanillaTheme.MUTED_TEXT);
        graphics.drawString(
                font,
                "fields " + (fieldPage + 1) + " of " + fieldPages(selectedVariant()),
                left + 12,
                top + 273,
                SefVanillaTheme.MUTED_TEXT);
        if (progress >= 0) {
            int right = left + 12 + (PANEL_WIDTH - 24) * progress / 100;
            graphics.fill(left + 12, top + 281, right, top + 285, SefVanillaTheme.PROGRESS);
        }
    }

    public void acceptSuggestions(GuiWorkflowPayloads.GuiWorkflowSuggestions response) {
        if (!response.workflowId().equals(snapshot.workflowId())
                || response.revision() != snapshot.revision()) {
            return;
        }
        suggestions.put(response.fieldId(), response.suggestions());
        liveStatus = response.suggestions().isEmpty()
                ? "The server found no matching suggestions."
                : "The server returned " + response.suggestions().size() + " suggestions. Select pick.";
        if (minecraft != null && minecraft.screen == this) {
            rebuildWidgets();
        }
    }

    public void acceptProgress(GuiWorkflowPayloads.GuiWorkflowProgress update) {
        if (!update.workflowId().equals(snapshot.workflowId())) {
            return;
        }
        progress = update.percent();
        liveStatus = update.stage();
    }

    public void acceptResult(GuiWorkflowPayloads.GuiWorkflowResult result) {
        if (!result.workflowId().equals(snapshot.workflowId())) {
            return;
        }
        liveStatus = result.status();
        progress = result.successful() ? 100 : -1;
        if (result.closed()) {
            closed = true;
            if (minecraft != null) {
                minecraft.setScreen(parent);
            }
        }
    }

    public void acceptInvalidation(GuiWorkflowPayloads.GuiWorkflowInvalidate invalidation) {
        if (!invalidation.workflowId().equals(snapshot.workflowId())) {
            return;
        }
        liveStatus = invalidation.reason();
        closed = true;
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public void onClose() {
        if (!closed) {
            closed = true;
            SefClientTransport.closeWorkflow(snapshot);
        }
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
        return Component.literal(snapshot.title() + ". " + liveStatus);
    }

    private void chooseOrRequest(
            GuiWorkflowPayloads.WorkflowField field,
            EditBox input
    ) {
        capture();
        if (minecraft == null) {
            return;
        }
        minecraft.setScreen(new SefSuggestionPickerScreen(
                this,
                field.id(),
                field.label(),
                suggestions.getOrDefault(field.id(), List.of()),
                replacement -> {
                    input.setValue(replacement);
                    values().put(field.id(), replacement);
                    liveStatus = "Selected a server value. Preview again before execution.";
                }));
    }

    private void openPlayerPicker(GuiWorkflowPayloads.WorkflowField field) {
        capture();
        if (minecraft == null) {
            return;
        }
        minecraft.setScreen(new SefPlayerPickerScreen(
                this,
                field.id(),
                field.type().equals("players")
                        && snapshot.actionId().equals("sef:item.give.others"),
                values().getOrDefault(field.id(), ""),
                suggestions.getOrDefault(field.id(), List.of()),
                replacement -> {
                    values().put(field.id(), replacement);
                    liveStatus = replacement.equals(GuiWorkflowPayloads.PLAYER_SELECTION_ALL_ONLINE)
                            ? "Selected every visible online player."
                            : replacement.equals(GuiWorkflowPayloads.PLAYER_SELECTION_ALL_KNOWN)
                            ? "Selected every visible known player. Offline actions will queue."
                            : "Selected player targets. Preview again before execution.";
                }));
    }

    private void openItemPicker(GuiWorkflowPayloads.WorkflowField field) {
        capture();
        if (minecraft == null) {
            return;
        }
        minecraft.setScreen(new SefItemPickerScreen(this, replacement -> {
            values().put(field.id(), replacement);
            liveStatus = "Selected " + replacement + ". Preview again before execution.";
        }));
    }

    private void addIntegerControls(
            GuiWorkflowPayloads.WorkflowField field,
            String value,
            int x,
            int y
    ) {
        addIntegerStep(field, x, y, 34, "-10", -10L);
        addIntegerStep(field, x + 36, y, 34, "-1", -1L);
        Button display = Button.builder(
                        Component.literal(value.isBlank() ? "select amount" : fit(value, 88)),
                        ignored -> {
                        })
                .bounds(x + 72, y, 100, 20)
                .tooltip(Tooltip.create(fieldTooltip(field)))
                .build();
        display.active = false;
        addRenderableWidget(display);
        addIntegerStep(field, x + 174, y, 34, "+1", 1L);
        addIntegerStep(field, x + 210, y, 34, "+10", 10L);
    }

    private void addIntegerStep(
            GuiWorkflowPayloads.WorkflowField field,
            int x,
            int y,
            int width,
            String label,
            long delta
    ) {
        Button step = Button.builder(Component.literal(label), ignored -> {
                    long minimum = field.minimum() <= Long.MIN_VALUE
                            ? Long.MIN_VALUE
                            : (long) Math.ceil(field.minimum());
                    long maximum = field.maximum() >= Long.MAX_VALUE
                            ? Long.MAX_VALUE
                            : (long) Math.floor(field.maximum());
                    long current;
                    try {
                        current = Long.parseLong(values().getOrDefault(field.id(), ""));
                    } catch (NumberFormatException exception) {
                        current = Math.clamp(1L, minimum, maximum);
                    }
                    long replacement;
                    try {
                        replacement = Math.addExact(current, delta);
                    } catch (ArithmeticException exception) {
                        replacement = delta < 0L ? minimum : maximum;
                    }
                    values().put(
                            field.id(),
                            Long.toString(Math.clamp(replacement, minimum, maximum)));
                    liveStatus = "Amount changed. Preview again before execution.";
                    rebuildWidgets();
                })
                .bounds(x, y, width, 20)
                .tooltip(Tooltip.create(fieldTooltip(field)))
                .build();
        step.active = !snapshot.confirmationPending();
        addRenderableWidget(step);
    }

    void requestPlayerSuggestions(String fieldId, String value) {
        requestSuggestions(fieldId, value);
    }

    void requestSuggestions(String fieldId, String value) {
        SefClientTransport.requestWorkflowSuggestions(
                snapshot,
                selectedVariant().id(),
                fieldId,
                value,
                UUID.randomUUID());
    }

    private void capture() {
        for (FieldWidget field : visibleFields) {
            if (field.input() != null) {
                values().put(field.field().id(), field.input().getValue());
            }
        }
    }

    private List<GuiWorkflowPayloads.WorkflowFieldValue> fieldValues(
            GuiWorkflowPayloads.WorkflowVariant variant
    ) {
        return variant.fields().stream()
                .map(field -> new GuiWorkflowPayloads.WorkflowFieldValue(
                        field.id(),
                        values().getOrDefault(field.id(), "")))
                .toList();
    }

    private Map<String, String> values() {
        return drafts.get(selectedVariant().id());
    }

    private GuiWorkflowPayloads.WorkflowVariant selectedVariant() {
        return snapshot.variants().get(Math.min(selectedVariant, snapshot.variants().size() - 1));
    }

    private int variantIndex(String id) {
        for (int index = 0; index < snapshot.variants().size(); index++) {
            if (snapshot.variants().get(index).id().equals(id)) {
                return index;
            }
        }
        return 0;
    }

    private int fieldPages(GuiWorkflowPayloads.WorkflowVariant variant) {
        return Math.max(1, (variant.fields().size() + FIELDS_PER_PAGE - 1) / FIELDS_PER_PAGE);
    }

    private Component fieldTooltip(GuiWorkflowPayloads.WorkflowField field) {
        String range = switch (field.type()) {
            case "integer", "decimal" -> ", range " + field.minimum() + " to " + field.maximum();
            default -> "";
        };
        String source = field.suggestionKind().isBlank()
                ? ""
                : ", suggestions " + field.suggestionKind();
        return Component.literal(field.type().replace('_', ' ') + range + source);
    }

    private String playerSelectionLabel(String value) {
        if (value.isBlank()) {
            return "select player targets";
        }
        if (value.equals(GuiWorkflowPayloads.PLAYER_SELECTION_ALL_ONLINE)) {
            return "all visible online players";
        }
        if (value.equals(GuiWorkflowPayloads.PLAYER_SELECTION_ALL_KNOWN)) {
            return "everyone, online and offline";
        }
        int count = value.split(",", -1).length;
        return count == 1 ? fit(value, 220) : count + " selected players";
    }

    private String variantLabel(GuiWorkflowPayloads.WorkflowVariant variant) {
        if (snapshot.actionId().equals("sef:item.give.others")) {
            return variant.fields().stream().anyMatch(field -> field.type().equals("integer"))
                    ? "custom amount"
                    : "one item";
        }
        return variant.label();
    }

    private String fit(String value, int maximumPixels) {
        if (font.width(value) <= maximumPixels) {
            return value;
        }
        int ellipsisWidth = font.width("…");
        return font.plainSubstrByWidth(value, Math.max(0, maximumPixels - ellipsisWidth)) + "…";
    }

    private record FieldWidget(
            GuiWorkflowPayloads.WorkflowField field,
            EditBox input
    ) {
    }
}
