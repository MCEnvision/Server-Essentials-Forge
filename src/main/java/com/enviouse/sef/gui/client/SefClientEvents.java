package com.enviouse.sef.gui.client;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.gui.protocol.ClientProtocolState;
import com.enviouse.sef.gui.protocol.SefGuiServer;
import com.enviouse.sef.gui.protocol.SefPayloads;
import com.enviouse.sef.gui.protocol.SefProtocol;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.util.TriState;

import java.util.List;

@EventBusSubscriber(modid = ServerEssentialsForge.MODID, value = Dist.CLIENT)
public final class SefClientEvents {
    private SefClientEvents() {
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        while (SefClientModEvents.OPEN_DASHBOARD.consumeClick()) {
            if (minecraft.player != null
                    && ClientProtocolState.negotiated(SefProtocol.Feature.DASHBOARD)) {
                SefClientTransport.open(SefGuiServer.DASHBOARD, 1, "");
            }
        }
        if (minecraft.screen instanceof SefPanelScreen
                && !ClientProtocolState.negotiated(SefProtocol.Feature.DASHBOARD)) {
            minecraft.screen.onClose();
        }
        ClientProtocolState.takePanel().ifPresent(snapshot -> {
            if (minecraft.player == null) {
                return;
            }
            minecraft.setScreen(new SefPanelScreen(minecraft.screen, snapshot));
        });
        if (minecraft.player != null) {
            FancyTagClientCache.tick(minecraft);
        }
    }

    @SubscribeEvent
    public static void pauseScreen(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof PauseScreen)
                || !ClientProtocolState.negotiated(SefProtocol.Feature.PAUSE_BUTTON)) {
            return;
        }
        Placement placement = findPausePlacement(event);
        if (placement == null) {
            return;
        }
        Button button = Button.builder(
                        Component.translatable("gui.sef.pause_button"),
                        ignored -> SefClientTransport.open(SefGuiServer.DASHBOARD, 1, ""))
                .bounds(placement.x(), placement.y(), 100, 20)
                .tooltip(Tooltip.create(Component.translatable("gui.sef.pause_tooltip")))
                .build();
        event.addListener(button);
    }

    @SubscribeEvent
    public static void renderHud(RenderGuiEvent.Post event) {
        List<SefPayloads.HudTile> tiles = ClientProtocolState.hudTiles();
        GuiGraphics graphics = event.getGuiGraphics();
        Minecraft minecraft = Minecraft.getInstance();
        int right = graphics.guiWidth() - 6;
        int y = 6;
        for (SefPayloads.HudTile tile : tiles) {
            int color = switch (tile.severity()) {
                case INFO -> 0xffbdbdbd;
                case NOTICE -> 0xff55ffff;
                case WARNING -> 0xffffaa00;
                case CRITICAL -> 0xffff5555;
            };
            int width = minecraft.font.width(tile.text()) + 8;
            int height = tile.surface() == SefPayloads.HudSurface.PROGRESS ? 18 : 13;
            int background = tile.surface() == SefPayloads.HudSurface.ALERT
                    ? 0xc0402000
                    : 0xb0000000;
            graphics.fill(right - width, y, right, y + height, background);
            graphics.drawString(minecraft.font, tile.text(), right - width + 4, y + 3, color, true);
            if (tile.surface() == SefPayloads.HudSurface.PROGRESS) {
                int progressWidth = Math.max(0, width * tile.progressPercent() / 100);
                graphics.fill(right - width, y + 14, right - width + progressWidth, y + 17, color);
            }
            y += height + 2;
        }
        int tagY = y;
        if (ClientProtocolState.negotiated(SefProtocol.Feature.FANCY_TAGS_STATIC)) {
            FancyTagClientCache.texture().ifPresent(texture -> renderTag(
                    graphics,
                    texture,
                    right - 20,
                    tagY));
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        Minecraft minecraft = Minecraft.getInstance();
        FancyTagClientCache.close(minecraft);
        ClientProtocolState.reset();
    }

    @SubscribeEvent
    public static void renderNameTag(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof Player player)
                || !ClientProtocolState.negotiated(SefProtocol.Feature.IDENTITY_PROJECTION)) {
            return;
        }
        ClientProtocolState.identity(player.getUUID()).ifPresentOrElse(
                identity -> event.setContent(identity.displayName()),
                () -> event.setCanRender(TriState.FALSE));
    }

    private static void renderTag(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y
    ) {
        int sourceWidth = Math.max(1, FancyTagClientCache.textureWidth());
        int sourceHeight = Math.max(1, FancyTagClientCache.textureHeight());
        graphics.fill(x - 2, y - 2, x + 18, y + 18, 0xb0000000);
        graphics.blit(texture, x, y, 0.0F, 0.0F, 16, 16, sourceWidth, sourceHeight);
    }

    private static Placement findPausePlacement(ScreenEvent.Init.Post event) {
        int right = Math.max(5, event.getScreen().width - 105);
        int bottom = Math.max(5, event.getScreen().height - 25);
        for (int x : new int[]{right, 5}) {
            for (int y = 5; y <= bottom; y += 22) {
                int candidateY = y;
                boolean occupied = event.getListenersList().stream()
                        .filter(AbstractWidget.class::isInstance)
                        .map(AbstractWidget.class::cast)
                        .anyMatch(widget -> intersects(x, candidateY, 100, 20, widget));
                if (!occupied) {
                    return new Placement(x, candidateY);
                }
            }
        }
        return null;
    }

    private static boolean intersects(int x, int y, int width, int height, AbstractWidget widget) {
        return x < widget.getX() + widget.getWidth()
                && x + width > widget.getX()
                && y < widget.getY() + widget.getHeight()
                && y + height > widget.getY();
    }

    private record Placement(int x, int y) {
    }
}
