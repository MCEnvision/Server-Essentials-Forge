package com.enviouse.sef.gui.client;

import com.enviouse.sef.ServerEssentialsForge;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(
        modid = ServerEssentialsForge.MODID,
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.MOD)
public final class SefClientModEvents {
    static final KeyMapping OPEN_DASHBOARD = new KeyMapping(
            "key.sef.open_dashboard",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_GRAVE_ACCENT,
            "key.categories.sef");
    static final KeyMapping DISGUISE_PRIMARY = new KeyMapping(
            "key.sef.disguise_primary",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "key.categories.sef");
    static final KeyMapping DISGUISE_SECONDARY = new KeyMapping(
            "key.sef.disguise_secondary",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "key.categories.sef");
    static final KeyMapping DISGUISE_UTILITY = new KeyMapping(
            "key.sef.disguise_utility",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "key.categories.sef");
    static final KeyMapping OPEN_FANCY_TAGS_STUDIO = new KeyMapping(
            "key.sef.open_fancy_tags_studio",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "key.categories.sef");

    private SefClientModEvents() {
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_DASHBOARD);
        event.register(DISGUISE_PRIMARY);
        event.register(DISGUISE_SECONDARY);
        event.register(DISGUISE_UTILITY);
        event.register(OPEN_FANCY_TAGS_STUDIO);
    }
}
