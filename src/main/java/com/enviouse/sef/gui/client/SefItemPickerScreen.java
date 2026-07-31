package com.enviouse.sef.gui.client;

import com.enviouse.sef.gui.protocol.GuiWorkflowPayloads;
import com.enviouse.sef.gui.protocol.SefPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public final class SefItemPickerScreen extends SefScreen {
    private static final int PANEL_WIDTH = 500;
    private static final int PANEL_HEIGHT = 360;
    private static final int COLUMNS = 11;
    private static final int ROWS = 5;
    private static final int PAGE_SIZE = COLUMNS * ROWS;
    private static final int TABS_PER_PAGE = 18;

    private final SefWorkflowScreen parent;
    private final Consumer<String> selection;
    private final List<ItemEntry> allItems;
    private final List<TabView> tabs;
    private final List<ItemButton> itemButtons = new ArrayList<>();
    private String query = "";
    private int selectedTab;
    private int tabPage;
    private int itemPage;
    private EditBox search;

    public SefItemPickerScreen(
            SefWorkflowScreen parent,
            Consumer<String> selection
    ) {
        super(Component.literal("Select item"));
        this.parent = Objects.requireNonNull(parent, "parent");
        this.selection = Objects.requireNonNull(selection, "selection");
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.player != null) {
            CreativeModeTabs.tryRebuildTabContents(
                    minecraft.level.enabledFeatures(),
                    minecraft.player.canUseGameMasterBlocks(),
                    minecraft.level.registryAccess());
        }
        allItems = allItems();
        tabs = tabs(allItems);
    }

    @Override
    protected void init() {
        itemButtons.clear();
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        search = new EditBox(
                font,
                left + 12,
                top + 30,
                382,
                20,
                Component.literal("Search items"));
        search.setMaxLength(128);
        search.setValue(query);
        search.setHint(Component.literal("item name or namespace"));
        addRenderableWidget(search);
        addRenderableWidget(Button.builder(Component.literal("search"), ignored -> {
                    query = search.getValue().strip();
                    itemPage = 0;
                    rebuildWidgets();
                })
                .bounds(left + 398, top + 30, 90, 20)
                .build());

        int firstTab = tabPage * TABS_PER_PAGE;
        int lastTab = Math.min(tabs.size(), firstTab + TABS_PER_PAGE);
        for (int index = firstTab; index < lastTab; index++) {
            int tabIndex = index;
            TabView tab = tabs.get(index);
            int x = left + 34 + (index - firstTab) * 24;
            ItemButton button = new ItemButton(
                    x,
                    top + 56,
                    tab.icon(),
                    ignored -> {
                        query = search.getValue().strip();
                        selectedTab = tabIndex;
                        itemPage = 0;
                        rebuildWidgets();
                    });
            button.setTooltip(Tooltip.create(tab.name()));
            button.active = selectedTab != index;
            addRenderableWidget(button);
        }

        List<ItemEntry> visible = visibleItems();
        int pages = pages(visible);
        itemPage = Math.max(0, Math.min(itemPage, pages - 1));
        int first = Math.min(visible.size(), itemPage * PAGE_SIZE);
        int last = Math.min(visible.size(), first + PAGE_SIZE);
        int gridLeft = left + (PANEL_WIDTH - COLUMNS * 24) / 2;
        for (int index = first; index < last; index++) {
            ItemEntry entry = visible.get(index);
            int offset = index - first;
            int x = gridLeft + offset % COLUMNS * 24;
            int y = top + 86 + offset / COLUMNS * 24;
            ItemButton button = new ItemButton(x, y, entry.stack(), ignored -> choose(entry));
            itemButtons.add(button);
            addRenderableWidget(button);
        }

        int footer = top + 306;
        addRenderableWidget(Button.builder(Component.literal("< tabs"), ignored -> {
                    tabPage = Math.max(0, tabPage - 1);
                    rebuildWidgets();
                })
                .bounds(left + 12, footer, 58, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("tabs >"), ignored -> {
                    tabPage = Math.min(tabPages() - 1, tabPage + 1);
                    rebuildWidgets();
                })
                .bounds(left + 74, footer, 58, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("<"), ignored -> {
                    itemPage = Math.max(0, itemPage - 1);
                    rebuildWidgets();
                })
                .bounds(left + 162, footer, 28, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal(">"), ignored -> {
                    itemPage = Math.min(pages(visibleItems()) - 1, itemPage + 1);
                    rebuildWidgets();
                })
                .bounds(left + 194, footer, 28, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("back"), ignored -> onClose())
                .bounds(left + 408, footer, 80, 20)
                .build());
        setInitialFocus(search);
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
        graphics.drawCenteredString(font, "Select item", width / 2, top + 9, SefVanillaTheme.TEXT);
        super.render(graphics, mouseX, mouseY, partialTick);
        List<ItemEntry> visible = visibleItems();
        graphics.drawCenteredString(
                font,
                tabs.get(selectedTab).name().getString()
                        + ", " + visible.size() + " items, page "
                        + (itemPage + 1) + "/" + pages(visible),
                width / 2,
                top + 286,
                SefVanillaTheme.MUTED_TEXT);
        graphics.drawCenteredString(
                font,
                "click an item to select its canonical registry id",
                width / 2,
                top + 336,
                SefVanillaTheme.MUTED_TEXT);
        for (ItemButton button : itemButtons) {
            if (button.isHovered()) {
                graphics.renderTooltip(font, button.stack, mouseX, mouseY);
                break;
            }
        }
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

    private void choose(ItemEntry entry) {
        selection.accept(entry.id().toString());
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    private List<ItemEntry> visibleItems() {
        String normalized = query.strip().toLowerCase(Locale.ROOT);
        List<ItemEntry> source = normalized.isBlank()
                ? tabs.get(selectedTab).items()
                : allItems;
        if (normalized.isBlank()) {
            return source;
        }
        return source.stream()
                .filter(entry -> entry.id().toString().contains(normalized)
                        || entry.stack().getHoverName().getString()
                        .toLowerCase(Locale.ROOT)
                        .contains(normalized))
                .toList();
    }

    private int tabPages() {
        return Math.max(1, (tabs.size() + TABS_PER_PAGE - 1) / TABS_PER_PAGE);
    }

    private static int pages(List<?> entries) {
        return Math.max(1, (entries.size() + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    private static List<ItemEntry> allItems() {
        return BuiltInRegistries.ITEM.entrySet().stream()
                .filter(entry -> entry.getValue() != Items.AIR)
                .map(entry -> new ItemEntry(
                        entry.getKey().location(),
                        new ItemStack(entry.getValue())))
                .sorted(itemOrder())
                .toList();
    }

    private static List<TabView> tabs(List<ItemEntry> allItems) {
        List<TabView> result = new ArrayList<>();
        result.add(new TabView(
                Component.literal("All items"),
                new ItemStack(Items.COMPASS),
                allItems));
        try {
            for (CreativeModeTab tab : CreativeModeTabs.tabs()) {
                if (!tab.shouldDisplay() || !tab.hasAnyItems()) {
                    continue;
                }
                Map<ResourceLocation, ItemEntry> entries = new LinkedHashMap<>();
                for (ItemStack stack : tab.getDisplayItems()) {
                    Item item = stack.getItem();
                    ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                    if (id != null && item != Items.AIR) {
                        entries.putIfAbsent(id, new ItemEntry(id, stack.copyWithCount(1)));
                    }
                }
                if (!entries.isEmpty()) {
                    result.add(new TabView(
                            tab.getDisplayName(),
                            tab.getIconItem().copyWithCount(1),
                            entries.values().stream().sorted(itemOrder()).toList()));
                }
            }
        } catch (RuntimeException exception) {
            return List.copyOf(result);
        }
        return List.copyOf(result);
    }

    private static Comparator<ItemEntry> itemOrder() {
        return Comparator
                .comparing((ItemEntry entry) ->
                        entry.stack().getHoverName().getString().toLowerCase(Locale.ROOT))
                .thenComparing(entry -> entry.id().toString());
    }

    private record ItemEntry(ResourceLocation id, ItemStack stack) {
    }

    private record TabView(Component name, ItemStack icon, List<ItemEntry> items) {
        private TabView {
            items = List.copyOf(items);
        }
    }

    private static final class ItemButton extends AbstractButton {
        private final ItemStack stack;
        private final Consumer<ItemButton> press;

        private ItemButton(
                int x,
                int y,
                ItemStack stack,
                Consumer<ItemButton> press
        ) {
            super(x, y, 22, 22, Component.empty());
            this.stack = stack;
            this.press = press;
        }

        @Override
        public void onPress() {
            press.accept(this);
        }

        @Override
        protected void renderWidget(
                GuiGraphics graphics,
                int mouseX,
                int mouseY,
                float partialTick
        ) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
            graphics.renderItem(stack, getX() + 3, getY() + 3);
        }

        @Override
        public void renderString(
                GuiGraphics graphics,
                net.minecraft.client.gui.Font font,
                int color
        ) {
        }

        @Override
        protected net.minecraft.network.chat.MutableComponent createNarrationMessage() {
            return wrapDefaultNarrationMessage(stack.getHoverName());
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }
}
