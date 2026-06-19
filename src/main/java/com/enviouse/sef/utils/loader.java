package com.enviouse.sef.utils;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;

// NeoForge 1.21.1 port of the old Forge `loader` helper.
//  - The server-side-only marker (Forge IExtensionPoint.DisplayTest + NetworkConstants.IGNORESERVERONLY)
//    is removed; that behavior is declared in neoforge.mods.toml via displayTest = "IGNORE_SERVER_VERSION".
//  - Game-bus registration: MinecraftForge.EVENT_BUS -> NeoForge.EVENT_BUS.
//  - Config registration moves off the global ModLoadingContext onto the injected ModContainer,
//    and takes a ModConfigSpec directly (the old IConfigSpec<?> param type changed in NeoForge).
public class loader {

    public static void register(Object toRegister) {
        NeoForge.EVENT_BUS.register(toRegister);
    }

    public static void registerConfig(ModContainer modContainer, String cType, ModConfigSpec spec, String fileName) {
        modContainer.registerConfig(ModConfig.Type.valueOf(cType), spec, fileName);
    }
}
