package com.enviouse.sef.gui.client;

import com.enviouse.sef.gui.protocol.ClientProtocolState;
import com.enviouse.sef.gui.protocol.SefProtocol;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

public final class FancyTagLocalOverlay {
    private static FancyTagProject project;
    private static DynamicTexture texture;
    private static ResourceLocation location;
    private static long renderedRevision;

    private FancyTagLocalOverlay() {
    }

    public static void set(FancyTagProject replacement) {
        project = replacement;
        renderedRevision = 0L;
    }

    public static void clear(Minecraft minecraft) {
        project = null;
        release(minecraft);
    }

    public static void release(Minecraft minecraft) {
        if (location != null) {
            minecraft.getTextureManager().release(location);
        }
        texture = null;
        location = null;
        renderedRevision = 0L;
    }

    public static int render(GuiGraphics graphics, Minecraft minecraft, int y) {
        if (project == null
                || minecraft.player == null
                || minecraft.getConnection() != null
                && !ClientProtocolState.negotiated(SefProtocol.Feature.FANCY_TAGS_LOCAL_OVERLAY)) {
            return y;
        }
        if (!prepare(minecraft)) {
            return y;
        }
        int maximumWidth = 96;
        int maximumHeight = 32;
        float scale = Math.min(
                1.0F,
                Math.min(
                        maximumWidth / (float) project.width(),
                        maximumHeight / (float) project.height()));
        int width = Math.max(1, Math.round(project.width() * scale));
        int height = Math.max(1, Math.round(project.height() * scale));
        graphics.fill(4, y, 12 + width, y + height + 15, 0xb0000000);
        graphics.drawString(minecraft.font, "local tag preview", 8, y + 3, 0xffffaa00, true);
        graphics.blit(
                location,
                8,
                y + 13,
                0.0F,
                0.0F,
                width,
                height,
                project.width(),
                project.height());
        return y + height + 19;
    }

    private static boolean prepare(Minecraft minecraft) {
        if (location != null && renderedRevision == project.revision()) {
            return true;
        }
        try {
            NativeImage image = project.flatten();
            if (location == null) {
                texture = new DynamicTexture(image);
                location = minecraft.getTextureManager().register("sef_local_tag_preview", texture);
            } else {
                texture.setPixels(image);
                texture.upload();
            }
            renderedRevision = project.revision();
            return true;
        } catch (RuntimeException exception) {
            release(minecraft);
            return false;
        }
    }
}
