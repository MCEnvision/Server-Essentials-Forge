package com.enviouse.sef.gui.client;

import com.enviouse.sef.gui.protocol.ClientProtocolState;
import com.enviouse.sef.gui.protocol.SefPayloads;
import com.enviouse.sef.gui.protocol.SefProtocol;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class SefPanelScreen extends Screen {
    private static final int PANEL_WIDTH = 310;
    private static final int PANEL_HEIGHT = 218;

    private final Screen parent;
    private final SefPayloads.PanelSnapshot snapshot;
    private final List<RenderedEntry> renderedEntries = new ArrayList<>();
    private EditBox search;

    public SefPanelScreen(Screen parent, SefPayloads.PanelSnapshot snapshot) {
        super(Component.literal(snapshot.title()));
        this.parent = parent instanceof SefPanelScreen panel ? panel.parent : parent;
        this.snapshot = snapshot;
    }

    @Override
    protected void init() {
        renderedEntries.clear();
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        search = new EditBox(
                font,
                left + 12,
                top + 27,
                172,
                20,
                Component.translatable("gui.sef.search.narration"));
        search.setMaxLength(64);
        search.setValue(snapshot.query());
        search.setHint(Component.translatable("gui.sef.search"));
        addRenderableWidget(search);
        addRenderableWidget(Button.builder(
                        Component.translatable("gui.sef.search"),
                        ignored -> SefClientTransport.open(snapshot.panelId(), 1, search.getValue()))
                .bounds(left + 188, top + 27, 54, 20)
                .build());
        addRenderableWidget(Button.builder(
                        Component.translatable("gui.sef.refresh"),
                        ignored -> SefClientTransport.open(
                                snapshot.panelId(),
                                snapshot.page(),
                                search.getValue()))
                .bounds(left + 246, top + 27, 52, 20)
                .build());

        int entryTop = top + 53;
        for (int index = 0; index < snapshot.entries().size(); index++) {
            SefPayloads.PanelEntry entry = snapshot.entries().get(index);
            int column = index % 2;
            int row = index / 2;
            int x = left + 12 + column * 144;
            int y = entryTop + row * 18;
            boolean hasIcon = !icon(entry.icon()).isEmpty();
            Button button = Button.builder(
                            Component.literal((hasIcon ? "   " : "") + fit(
                                    entry.title(),
                                    hasIcon ? 110 : 128)),
                            ignored -> SefClientTransport.action(snapshot, entry))
                    .bounds(x, y, 140, 16)
                    .tooltip(Tooltip.create(Component.literal(entry.subtitle())))
                    .build();
            button.active = entry.enabled();
            addRenderableWidget(button);
            renderedEntries.add(new RenderedEntry(entry, x + 2, y));
        }

        Button previous = Button.builder(
                        Component.literal("<"),
                        ignored -> SefClientTransport.open(
                                snapshot.panelId(),
                                Math.max(1, snapshot.page() - 1),
                                search.getValue()))
                .bounds(left + 12, top + 190, 24, 18)
                .build();
        previous.active = snapshot.page() > 1;
        addRenderableWidget(previous);
        Button next = Button.builder(
                        Component.literal(">"),
                        ignored -> SefClientTransport.open(
                                snapshot.panelId(),
                                Math.min(snapshot.pages(), snapshot.page() + 1),
                                search.getValue()))
                .bounds(left + 274, top + 190, 24, 18)
                .build();
        next.active = snapshot.page() < snapshot.pages();
        addRenderableWidget(next);
        addRenderableWidget(Button.builder(
                        Component.translatable(snapshot.panelId().equals("dashboard")
                                ? "gui.sef.close"
                                : "gui.sef.dashboard"),
                        ignored -> {
                            if (snapshot.panelId().equals("dashboard")) {
                                onClose();
                            } else {
                                SefClientTransport.open("dashboard", 1, "");
                            }
                        })
                .bounds(left + 111, top + 190, 88, 18)
                .build());
        setInitialFocus(search);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        SefVanillaTheme.panel(
                graphics,
                left,
                top,
                PANEL_WIDTH,
                PANEL_HEIGHT,
                snapshot.view());
        graphics.drawCenteredString(
                font,
                fit(title.getString(), PANEL_WIDTH - 48),
                width / 2,
                top + 10,
                SefVanillaTheme.TEXT);
        if (ClientProtocolState.negotiated(SefProtocol.Feature.FANCY_TAGS_STATIC)) {
            FancyTagClientCache.texture().ifPresent(texture -> graphics.blit(
                    texture,
                    left + 8,
                    top + 5,
                    0.0F,
                    0.0F,
                    16,
                    16,
                    Math.max(1, FancyTagClientCache.textureWidth()),
                    Math.max(1, FancyTagClientCache.textureHeight())));
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        for (RenderedEntry rendered : renderedEntries) {
            ItemStack icon = icon(rendered.entry().icon());
            if (!icon.isEmpty()) {
                graphics.renderFakeItem(icon, rendered.x(), rendered.y());
            }
        }
        graphics.drawCenteredString(
                font,
                Component.translatable("gui.sef.page", snapshot.page(), snapshot.pages()),
                width / 2,
                top + 195,
                SefVanillaTheme.MUTED_TEXT);
        graphics.drawCenteredString(
                font,
                fit(snapshot.status(), PANEL_WIDTH - 24),
                width / 2,
                top + 177,
                SefVanillaTheme.MUTED_TEXT);
        if (snapshot.view() == SefPayloads.PanelView.PROGRESS) {
            graphics.fill(
                    left + 30,
                    top + 168,
                    left + PANEL_WIDTH - 30,
                    top + 173,
                    SefVanillaTheme.PROGRESS);
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

    @Override
    public Component getNarrationMessage() {
        return Component.literal(snapshot.title() + ". " + snapshot.status()
                + ". Page " + snapshot.page() + " of " + snapshot.pages() + ".");
    }

    private static ItemStack icon(String id) {
        ResourceLocation resource = ResourceLocation.tryParse(id);
        if (resource == null || !BuiltInRegistries.ITEM.containsKey(resource)) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(BuiltInRegistries.ITEM.get(resource));
    }

    private String fit(String value, int maximumPixels) {
        if (font.width(value) <= maximumPixels) {
            return value;
        }
        int ellipsisWidth = font.width("…");
        return font.plainSubstrByWidth(value, Math.max(0, maximumPixels - ellipsisWidth)) + "…";
    }

    private record RenderedEntry(SefPayloads.PanelEntry entry, int x, int y) {
    }
}
