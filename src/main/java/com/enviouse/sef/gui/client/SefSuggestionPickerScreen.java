package com.enviouse.sef.gui.client;

import com.enviouse.sef.gui.protocol.GuiWorkflowPayloads;
import com.enviouse.sef.gui.protocol.SefPayloads;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

public final class SefSuggestionPickerScreen extends SefScreen {
    private static final int PANEL_WIDTH = 400;
    private static final int PANEL_HEIGHT = 320;
    private static final int PAGE_SIZE = 9;

    private final SefWorkflowScreen parent;
    private final String fieldId;
    private final String fieldLabel;
    private final Consumer<String> selection;
    private List<GuiWorkflowPayloads.WorkflowSuggestion> suggestions;
    private String query = "";
    private String status = "Select a server validated value.";
    private int page;
    private boolean initialRequestSent;
    private EditBox search;

    public SefSuggestionPickerScreen(
            SefWorkflowScreen parent,
            String fieldId,
            String fieldLabel,
            List<GuiWorkflowPayloads.WorkflowSuggestion> suggestions,
            Consumer<String> selection
    ) {
        super(Component.literal("Select " + fieldLabel));
        this.parent = Objects.requireNonNull(parent, "parent");
        this.fieldId = Objects.requireNonNull(fieldId, "fieldId");
        this.fieldLabel = Objects.requireNonNull(fieldLabel, "fieldLabel");
        this.suggestions = List.copyOf(suggestions);
        this.selection = Objects.requireNonNull(selection, "selection");
    }

    @Override
    protected void init() {
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        search = new EditBox(
                font,
                left + 12,
                top + 30,
                292,
                20,
                Component.literal("Search " + fieldLabel));
        search.setMaxLength(128);
        search.setValue(query);
        search.setHint(Component.literal("search server values"));
        addRenderableWidget(search);
        addRenderableWidget(Button.builder(Component.literal("search"), ignored -> {
                    query = search.getValue().strip();
                    page = 0;
                    status = "Requesting matching server values.";
                    parent.requestSuggestions(fieldId, query);
                })
                .bounds(left + 308, top + 30, 80, 20)
                .build());

        List<GuiWorkflowPayloads.WorkflowSuggestion> filtered = filtered();
        int pages = pages(filtered);
        page = Math.max(0, Math.min(page, pages - 1));
        int first = Math.min(filtered.size(), page * PAGE_SIZE);
        int last = Math.min(filtered.size(), first + PAGE_SIZE);
        for (int index = first; index < last; index++) {
            GuiWorkflowPayloads.WorkflowSuggestion suggestion = filtered.get(index);
            int row = index - first;
            addRenderableWidget(Button.builder(
                            Component.literal(fit(suggestion.label(), 352)),
                            ignored -> {
                                selection.accept(suggestion.value());
                                if (minecraft != null) {
                                    minecraft.setScreen(parent);
                                }
                            })
                    .bounds(left + 12, top + 58 + row * 24, 376, 20)
                    .build());
        }

        addRenderableWidget(Button.builder(Component.literal("<"), ignored -> {
                    query = search.getValue().strip();
                    page = Math.max(0, page - 1);
                    rebuildWidgets();
                })
                .bounds(left + 12, top + 280, 28, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal(">"), ignored -> {
                    query = search.getValue().strip();
                    page = Math.min(pages(filtered()) - 1, page + 1);
                    rebuildWidgets();
                })
                .bounds(left + 44, top + 280, 28, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("back"), ignored -> onClose())
                .bounds(left + 308, top + 280, 80, 20)
                .build());
        setInitialFocus(search);
        if (!initialRequestSent) {
            initialRequestSent = true;
            status = "Loading server validated values.";
            parent.requestSuggestions(fieldId, "");
        }
    }

    public void acceptSuggestions(GuiWorkflowPayloads.GuiWorkflowSuggestions response) {
        if (!response.fieldId().equals(fieldId)) {
            return;
        }
        parent.acceptSuggestions(response);
        suggestions = response.suggestions();
        page = 0;
        status = suggestions.isEmpty()
                ? "No matching server value was found."
                : "Found " + suggestions.size() + " matching values.";
        rebuildWidgets();
    }

    public void acceptInvalidation(GuiWorkflowPayloads.GuiWorkflowInvalidate invalidation) {
        parent.acceptInvalidation(invalidation);
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
                SefPayloads.PanelView.PICKER);
        graphics.drawCenteredString(font, getTitle(), width / 2, top + 9, SefVanillaTheme.TEXT);
        super.render(graphics, mouseX, mouseY, partialTick);
        List<GuiWorkflowPayloads.WorkflowSuggestion> filtered = filtered();
        graphics.drawCenteredString(
                font,
                fit(status, PANEL_WIDTH - 24),
                width / 2,
                top + 306,
                SefVanillaTheme.MUTED_TEXT);
        graphics.drawCenteredString(
                font,
                (page + 1) + "/" + pages(filtered) + ", " + filtered.size() + " values",
                width / 2,
                top + 286,
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

    private List<GuiWorkflowPayloads.WorkflowSuggestion> filtered() {
        String normalized = query.strip().toLowerCase(Locale.ROOT);
        List<GuiWorkflowPayloads.WorkflowSuggestion> result = new ArrayList<>();
        for (GuiWorkflowPayloads.WorkflowSuggestion suggestion : suggestions) {
            if (normalized.isBlank()
                    || suggestion.value().toLowerCase(Locale.ROOT).contains(normalized)
                    || suggestion.label().toLowerCase(Locale.ROOT).contains(normalized)) {
                result.add(suggestion);
            }
        }
        result.sort(Comparator.comparing(
                suggestion -> suggestion.label().toLowerCase(Locale.ROOT)));
        return result;
    }

    private static int pages(List<?> values) {
        return Math.max(1, (values.size() + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    private String fit(String value, int maximumPixels) {
        if (font.width(value) <= maximumPixels) {
            return value;
        }
        int ellipsisWidth = font.width("…");
        return font.plainSubstrByWidth(value, Math.max(0, maximumPixels - ellipsisWidth)) + "…";
    }
}
