package com.enviouse.sef.banned;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Manages banned items: scans inventories + placed blocks and confiscates/breaks them.
 */
public class BannedItemsManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type SET_TYPE = new TypeToken<Set<String>>(){}.getType();
    private final Set<String> bannedItems = new LinkedHashSet<>();
    private Path filePath;
    private int lastScanTick = 0;

    public void load(MinecraftServer server) {
        Path dir = server.getServerDirectory().toPath().resolve("serverconfig").resolve("sef");
        filePath = dir.resolve("banned_items.json");
        bannedItems.clear();
        if (!Files.exists(filePath)) return;
        try (Reader reader = Files.newBufferedReader(filePath)) {
            Set<String> loaded = GSON.fromJson(reader, SET_TYPE);
            if (loaded != null) bannedItems.addAll(loaded);
            ServerEssentialsForge.LOGGER.info("[SEF] Loaded {} banned item(s)", bannedItems.size());
        } catch (Exception e) {
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to load banned items", e);
        }
    }

    public void save() {
        if (filePath == null) return;
        try {
            Files.createDirectories(filePath.getParent());
            try (Writer writer = Files.newBufferedWriter(filePath)) {
                GSON.toJson(bannedItems, writer);
            }
        } catch (IOException e) {
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to save banned items", e);
        }
    }

    public Set<String> getBannedItems() {
        return bannedItems;
    }

    public void addItem(String itemId) {
        bannedItems.add(itemId);
        save();
    }

    public boolean removeItem(String itemId) {
        boolean removed = bannedItems.remove(itemId);
        if (removed) save();
        return removed;
    }

    public boolean isBanned(String itemId) {
        return bannedItems.contains(itemId);
    }

    public void tick(MinecraftServer server, int tickCount) {
        if (bannedItems.isEmpty()) return;

        // Scan inventories every tick for banned items
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            scanInventory(player);
        }

        // Scan placed blocks at a configurable interval
        if (!ConfigHandler.config.enableBannedBlockScanning.get()) return;
        int interval = ConfigHandler.config.bannedBlockScanInterval.get();
        if (tickCount - lastScanTick < interval) return;
        lastScanTick = tickCount;

        int radius = ConfigHandler.config.bannedBlockScanRadius.get();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            scanBlocks(player, radius);
        }
    }

    private void scanInventory(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (rl != null && bannedItems.contains(rl.toString())) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                    player.sendSystemMessage(TextFormatter.stringToFormattedText(
                        "&cBanned item removed from your inventory: &e" + rl));
                    ServerEssentialsForge.LOGGER.info("[SEF] Removed banned item {} from {}'s inventory",
                        rl, player.getGameProfile().getName());
                }
            }
        }
    }

    private void scanBlocks(ServerPlayer player, int radius) {
        ServerLevel level = player.serverLevel();
        BlockPos center = player.blockPosition();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    ResourceLocation rl = ForgeRegistries.BLOCKS.getKey(state.getBlock());
                    if (rl != null && bannedItems.contains(rl.toString())) {
                        level.destroyBlock(pos, false);
                        ServerEssentialsForge.LOGGER.info("[SEF] Destroyed banned block {} at {} near {}",
                            rl, pos, player.getGameProfile().getName());
                    }
                }
            }
        }
    }
}

