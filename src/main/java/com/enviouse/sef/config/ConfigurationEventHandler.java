package com.enviouse.sef.config;

import java.util.ArrayList;
import java.util.List;

import com.enviouse.sef.ServerEssentialsForge;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;

// MOD bus: ModConfigEvent.Loading/Reloading are fired by FML for this mod's configs.
// NeoForge owns config (re)loading and fires Reloading on external edits to the TOML;
// this handler re-applies the loaded values to runtime state (the IReloadable caches).
// bus = Bus.MOD is required for ModConfigEvent (a mod-bus event). EventBusSubscriber.Bus is
// deprecated-for-removal in NeoForge 21.1.x (forward-looking to the single-bus merge) but is the
// only way to target the mod bus on 1.21.1, hence the suppression.
@SuppressWarnings("removal")
@EventBusSubscriber(modid = ServerEssentialsForge.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ConfigurationEventHandler {
    private static final List<IReloadable> reloadables = new ArrayList<>();

    public static void registerReloadable(IReloadable rel) {
        reloadables.add(rel);
    }

    public static void reloadConfigOptions() {
        for (IReloadable reloadable : reloadables)
            if (reloadable != null)
                reloadable.reloadConfigOptions();
    }

    @SubscribeEvent
    public static void onModConfigReloadingEvent(ModConfigEvent.Reloading e) {
        reloadConfigOptions();
    }

    @SubscribeEvent
    public static void onModConfigLoadingEvent(ModConfigEvent.Loading e) {
        reloadConfigOptions();
    }
}
