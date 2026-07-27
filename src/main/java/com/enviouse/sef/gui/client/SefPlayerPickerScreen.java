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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

public final class SefPlayerPickerScreen extends Screen {
    private static final int PANEL_WIDTH = 376;
    private static final int PANEL_HEIGHT = 300;
    private static final int PAGE_SIZE = 8;

    private final SefWorkflowScreen parent;
    private final String fieldId;
    private final Consumer<String> selection;
    private List<GuiWorkflowPayloads.WorkflowSuggestion> suggestions;
    private String query = "";
    private boolean onlineOnly;
    private int page;
    private String status = "Select a known player or search the server profile index.";
    private EditBox search;

    public SefPlayerPickerScreen(
            SefWorkflowScreen parent,
            String fieldId,
            List<GuiWorkflowPayloads.WorkflowSuggestion> suggestions,
            Consumer<String> selection
    ) {
        super(Component.literal("Select player"));
        this.parent = Objects.requireNonNull(parent, "parent");
        this.fieldId = Objects.requireNonNull(fieldId, "fieldId");
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
                230,
                20,
                Component.literal("Search players"));
        search.setMaxLength(64);
        search.setValue(query);
        search.setHint(Component.literal("username or nickname"));
        addRenderableWidget(search);
        addRenderableWidget(Button.builder(Component.literal("search"), ignored -> {
                    query = search.getValue().strip();
                    page = 0;
                    status = "Searching every stored player profile.";
                    parent.requestPlayerSuggestions(fieldId, query);
                })
                .bounds(left + 246, top + 30, 56, 20)
                .tooltip(Tooltip.create(Component.literal(
                        "request a fresh server authoritative player search")))
                .build());
        addRenderableWidget(Button.builder(
                        Component.literal(onlineOnly ? "online" : "all"),
                        ignored -> {
                            query = search.getValue().strip();
                            onlineOnly = !onlineOnly;
                            page = 0;
                            rebuildWidgets();
                        })
                .bounds(left + 306, top + 30, 58, 20)
                .tooltip(Tooltip.create(Component.literal(
                        onlineOnly
                                ? "show every known player"
                                : "show currently online players only")))
                .build());

        List<GuiWorkflowPayloads.WorkflowSuggestion> filtered = filtered();
        int pages = pages(filtered);
        page = Math.max(0, Math.min(page, pages - 1));
        int start = Math.min(filtered.size(), page * PAGE_SIZE);
        int end = Math.min(filtered.size(), start + PAGE_SIZE);
        for (int index = start; index < end; index++) {
            GuiWorkflowPayloads.WorkflowSuggestion suggestion = filtered.get(index);
            int row = index - start;
            String label = suggestion.label()
                    + (suggestion.online() ? "  online" : "  offline");
            addRenderableWidget(Button.builder(
                            Component.literal(fit(label, 330)),
                            ignored -> {
                                selection.accept(suggestion.value());
                                if (minecraft != null) {
                                    minecraft.setScreen(parent);
                                }
                            })
                    .bounds(left + 12, top + 58 + row * 24, 352, 20)
                    .tooltip(Tooltip.create(Component.literal(
                            suggestion.value() + ", "
                                    + (suggestion.online()
                                    ? "available now"
                                    : "offline actions may be queued when supported"))))
                    .build());
        }

        addRenderableWidget(Button.builder(Component.literal("<"), ignored -> {
                    query = search.getValue().strip();
                    page = Math.max(0, page - 1);
                    rebuildWidgets();
                })
                .bounds(left + 12, top + 254, 28, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal(">"), ignored -> {
                    query = search.getValue().strip();
                    page = Math.min(pages(filtered()) - 1, page + 1);
                    rebuildWidgets();
                })
                .bounds(left + 44, top + 254, 28, 20)
                .build());
        addRenderableWidget(Button.builder(
                        Component.literal("back"),
                        ignored -> onClose())
                .bounds(left + 284, top + 254, 80, 20)
                .build());
        setInitialFocus(search);
    }

    public void acceptSuggestions(GuiWorkflowPayloads.GuiWorkflowSuggestions response) {
        if (!response.fieldId().equals(fieldId)) {
            return;
        }
        parent.acceptSuggestions(response);
        suggestions = response.suggestions();
        page = 0;
        status = suggestions.isEmpty()
                ? "No stored player matched that search."
                : "Found " + suggestions.size() + " matching players.";
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
        graphics.drawCenteredString(
                font,
                "Select player",
                width / 2,
                top + 9,
                SefVanillaTheme.TEXT);
        super.render(graphics, mouseX, mouseY, partialTick);
        List<GuiWorkflowPayloads.WorkflowSuggestion> filtered = filtered();
        graphics.drawCenteredString(
                font,
                fit(status, PANEL_WIDTH - 24),
                width / 2,
                top + 280,
                SefVanillaTheme.MUTED_TEXT);
        graphics.drawCenteredString(
                font,
                (page + 1) + "/" + pages(filtered) + ", " + filtered.size() + " players",
                width / 2,
                top + 260,
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
            if ((!onlineOnly || suggestion.online())
                    && (normalized.isBlank()
                    || suggestion.value().toLowerCase(Locale.ROOT).contains(normalized)
                    || suggestion.label().toLowerCase(Locale.ROOT).contains(normalized))) {
                result.add(suggestion);
            }
        }
        result.sort(Comparator
                .comparing(GuiWorkflowPayloads.WorkflowSuggestion::online)
                .reversed()
                .thenComparing(
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
