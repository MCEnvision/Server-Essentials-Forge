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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public final class SefPlayerPickerScreen extends Screen {
    private static final int PANEL_WIDTH = 420;
    private static final int PANEL_HEIGHT = 330;
    private static final int PAGE_SIZE = 8;

    private final SefWorkflowScreen parent;
    private final String fieldId;
    private final boolean multiple;
    private final Consumer<String> selection;
    private final Set<String> selected = new LinkedHashSet<>();
    private List<GuiWorkflowPayloads.WorkflowSuggestion> suggestions;
    private String query = "";
    private FilterMode filter = FilterMode.ALL;
    private int page;
    private String status;
    private boolean initialRequestSent;
    private EditBox search;

    public SefPlayerPickerScreen(
            SefWorkflowScreen parent,
            String fieldId,
            boolean multiple,
            String initialValue,
            List<GuiWorkflowPayloads.WorkflowSuggestion> suggestions,
            Consumer<String> selection
    ) {
        super(Component.literal(multiple ? "Select players" : "Select player"));
        this.parent = Objects.requireNonNull(parent, "parent");
        this.fieldId = Objects.requireNonNull(fieldId, "fieldId");
        this.multiple = multiple;
        this.suggestions = List.copyOf(suggestions);
        this.selection = Objects.requireNonNull(selection, "selection");
        if (initialValue != null
                && !initialValue.isBlank()
                && !initialValue.equals(GuiWorkflowPayloads.PLAYER_SELECTION_ALL_ONLINE)
                && !initialValue.equals(GuiWorkflowPayloads.PLAYER_SELECTION_ALL_KNOWN)) {
            for (String value : initialValue.split(",", -1)) {
                if (!value.isBlank()) {
                    selected.add(value);
                }
            }
        }
        status = multiple
                ? "Select individual players or use a bulk target."
                : "Select one known player.";
    }

    @Override
    protected void init() {
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        search = new EditBox(
                font,
                left + 12,
                top + 30,
                242,
                20,
                Component.literal("Search players"));
        search.setMaxLength(64);
        search.setValue(query);
        search.setHint(Component.literal("username or nickname"));
        addRenderableWidget(search);
        addRenderableWidget(Button.builder(Component.literal("search"), ignored -> search())
                .bounds(left + 258, top + 30, 62, 20)
                .tooltip(Tooltip.create(Component.literal(
                        "search the server authoritative profile index")))
                .build());
        addRenderableWidget(Button.builder(
                        Component.literal(filter.label),
                        ignored -> {
                            query = search.getValue().strip();
                            filter = filter.next();
                            page = 0;
                            rebuildWidgets();
                        })
                .bounds(left + 324, top + 30, 84, 20)
                .tooltip(Tooltip.create(Component.literal(filter.tooltip)))
                .build());

        int listTop = top + 58;
        if (multiple) {
            addRenderableWidget(Button.builder(Component.literal("all online"), ignored ->
                            finish(GuiWorkflowPayloads.PLAYER_SELECTION_ALL_ONLINE))
                    .bounds(left + 12, top + 56, 104, 20)
                    .tooltip(Tooltip.create(Component.literal(
                            "target every currently visible online player")))
                    .build());
            addRenderableWidget(Button.builder(Component.literal("everyone"), ignored ->
                            finish(GuiWorkflowPayloads.PLAYER_SELECTION_ALL_KNOWN))
                    .bounds(left + 120, top + 56, 104, 20)
                    .tooltip(Tooltip.create(Component.literal(
                            "target every visible stored profile and queue offline players")))
                    .build());
            addRenderableWidget(Button.builder(Component.literal("clear"), ignored -> {
                        selected.clear();
                        status = "Selection cleared.";
                        rebuildWidgets();
                    })
                    .bounds(left + 228, top + 56, 72, 20)
                    .build());
            listTop = top + 82;
        }

        List<GuiWorkflowPayloads.WorkflowSuggestion> filtered = filtered();
        int pages = pages(filtered);
        page = Math.max(0, Math.min(page, pages - 1));
        int start = Math.min(filtered.size(), page * PAGE_SIZE);
        int end = Math.min(filtered.size(), start + PAGE_SIZE);
        for (int index = start; index < end; index++) {
            GuiWorkflowPayloads.WorkflowSuggestion suggestion = filtered.get(index);
            int row = index - start;
            boolean checked = selected.stream()
                    .anyMatch(value -> value.equalsIgnoreCase(suggestion.value()));
            String label = (multiple ? checked ? "[x] " : "[ ] " : "")
                    + suggestion.label()
                    + (suggestion.online() ? "  online" : "  offline");
            addRenderableWidget(Button.builder(
                            Component.literal(fit(label, 368)),
                            ignored -> choose(suggestion))
                    .bounds(left + 12, listTop + row * 24, 396, 20)
                    .tooltip(Tooltip.create(Component.literal(
                            suggestion.value() + ", "
                                    + (suggestion.online()
                                    ? "available now"
                                    : "queued until login when this action supports it"))))
                    .build());
        }

        int footer = top + 282;
        addRenderableWidget(Button.builder(Component.literal("<"), ignored -> {
                    query = search.getValue().strip();
                    page = Math.max(0, page - 1);
                    rebuildWidgets();
                })
                .bounds(left + 12, footer, 28, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal(">"), ignored -> {
                    query = search.getValue().strip();
                    page = Math.min(pages(filtered()) - 1, page + 1);
                    rebuildWidgets();
                })
                .bounds(left + 44, footer, 28, 20)
                .build());
        if (multiple) {
            Button done = Button.builder(Component.literal("use selected"), ignored -> {
                        if (!selected.isEmpty()) {
                            finish(String.join(",", selected));
                        }
                    })
                    .bounds(left + 226, footer, 98, 20)
                    .tooltip(Tooltip.create(Component.literal(
                            "use the checked players as one server validated batch")))
                    .build();
            done.active = !selected.isEmpty();
            addRenderableWidget(done);
        }
        addRenderableWidget(Button.builder(Component.literal("back"), ignored -> onClose())
                .bounds(left + 328, footer, 80, 20)
                .build());
        setInitialFocus(search);

        if (!initialRequestSent) {
            initialRequestSent = true;
            status = "Loading every visible stored player profile.";
            parent.requestPlayerSuggestions(fieldId, "");
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
        graphics.drawCenteredString(font, getTitle(), width / 2, top + 9, SefVanillaTheme.TEXT);
        super.render(graphics, mouseX, mouseY, partialTick);
        List<GuiWorkflowPayloads.WorkflowSuggestion> filtered = filtered();
        graphics.drawCenteredString(
                font,
                fit(status, PANEL_WIDTH - 24),
                width / 2,
                top + 308,
                SefVanillaTheme.MUTED_TEXT);
        String count = (page + 1) + "/" + pages(filtered) + ", " + filtered.size() + " players";
        if (multiple) {
            count += ", " + selected.size() + " selected";
        }
        graphics.drawCenteredString(
                font,
                count,
                width / 2,
                top + 288,
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

    private void search() {
        query = search.getValue().strip();
        page = 0;
        status = "Searching every visible stored player profile.";
        parent.requestPlayerSuggestions(fieldId, query);
    }

    private void choose(GuiWorkflowPayloads.WorkflowSuggestion suggestion) {
        if (!multiple) {
            finish(suggestion.value());
            return;
        }
        String existing = selected.stream()
                .filter(value -> value.equalsIgnoreCase(suggestion.value()))
                .findFirst()
                .orElse(null);
        if (existing == null) {
            if (selected.size() >= GuiWorkflowPayloads.MAXIMUM_BATCH_TARGETS) {
                status = "The batch selection limit is "
                        + GuiWorkflowPayloads.MAXIMUM_BATCH_TARGETS + " players.";
                return;
            }
            selected.add(suggestion.value());
        } else {
            selected.remove(existing);
        }
        status = selected.size() + " players selected.";
        rebuildWidgets();
    }

    private void finish(String value) {
        selection.accept(value);
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    private List<GuiWorkflowPayloads.WorkflowSuggestion> filtered() {
        String normalized = query.strip().toLowerCase(Locale.ROOT);
        List<GuiWorkflowPayloads.WorkflowSuggestion> result = new ArrayList<>();
        for (GuiWorkflowPayloads.WorkflowSuggestion suggestion : suggestions) {
            if (filter.accepts(suggestion)
                    && (normalized.isBlank()
                    || suggestion.value().toLowerCase(Locale.ROOT).contains(normalized)
                    || suggestion.label().toLowerCase(Locale.ROOT).contains(normalized))) {
                result.add(suggestion);
            }
        }
        result.sort(Comparator
                .comparing(GuiWorkflowPayloads.WorkflowSuggestion::online)
                .reversed()
                .thenComparing(suggestion -> suggestion.label().toLowerCase(Locale.ROOT)));
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

    private enum FilterMode {
        ALL("all players", "show online and offline players"),
        ONLINE("online", "show currently online players only"),
        OFFLINE("offline", "show stored offline profiles only");

        private final String label;
        private final String tooltip;

        FilterMode(String label, String tooltip) {
            this.label = label;
            this.tooltip = tooltip;
        }

        private FilterMode next() {
            return values()[(ordinal() + 1) % values().length];
        }

        private boolean accepts(GuiWorkflowPayloads.WorkflowSuggestion suggestion) {
            return this == ALL
                    || this == ONLINE && suggestion.online()
                    || this == OFFLINE && !suggestion.online();
        }
    }
}
