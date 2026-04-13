package com.enviouse.sef.announcements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Manages scheduled server announcements (text & command) stored in serverconfig/sef/announcements.json.
 */
public class AnnouncementManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public record Announcement(String id, String type, String message, boolean enabled) {}

    private static final Type LIST_TYPE = new TypeToken<List<Announcement>>(){}.getType();
    private final List<Announcement> announcements = new ArrayList<>();
    private final Set<UUID> toggledOff = new HashSet<>();
    private Path filePath;
    private Path prefsPath;
    private int tickCounter = 0;
    private int currentIndex = 0;
    private final Random random = new Random();

    public void load(MinecraftServer server) {
        Path dir = server.getServerDirectory().toPath().resolve("serverconfig").resolve("sef");
        filePath = dir.resolve("announcements.json");
        prefsPath = dir.resolve("announcement_prefs.json");
        loadAnnouncements();
        loadPrefs();
    }

    private void loadAnnouncements() {
        announcements.clear();
        if (filePath == null || !Files.exists(filePath)) return;
        try (Reader reader = Files.newBufferedReader(filePath)) {
            List<Announcement> loaded = GSON.fromJson(reader, LIST_TYPE);
            if (loaded != null) announcements.addAll(loaded);
            ServerEssentialsForge.LOGGER.info("[SEF] Loaded {} announcement(s)", announcements.size());
        } catch (Exception e) {
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to load announcements", e);
        }
    }

    private void loadPrefs() {
        toggledOff.clear();
        if (prefsPath == null || !Files.exists(prefsPath)) return;
        try (Reader reader = Files.newBufferedReader(prefsPath)) {
            List<String> uuids = GSON.fromJson(reader, new TypeToken<List<String>>(){}.getType());
            if (uuids != null) uuids.forEach(s -> toggledOff.add(UUID.fromString(s)));
        } catch (Exception e) {
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to load announcement prefs", e);
        }
    }

    public void save() {
        if (filePath == null) return;
        try {
            Files.createDirectories(filePath.getParent());
            try (Writer writer = Files.newBufferedWriter(filePath)) {
                GSON.toJson(announcements, writer);
            }
        } catch (IOException e) {
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to save announcements", e);
        }
    }

    private void savePrefs() {
        if (prefsPath == null) return;
        try {
            Files.createDirectories(prefsPath.getParent());
            try (Writer writer = Files.newBufferedWriter(prefsPath)) {
                GSON.toJson(toggledOff.stream().map(UUID::toString).toList(), writer);
            }
        } catch (IOException e) {
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to save announcement prefs", e);
        }
    }

    public void tick(MinecraftServer server) {
        if (announcements.isEmpty()) return;
        int intervalTicks = ConfigHandler.config.announcementIntervalSeconds.get() * 20;
        if (intervalTicks <= 0) return;

        tickCounter++;
        if (tickCounter < intervalTicks) return;
        tickCounter = 0;

        // Find enabled announcements
        List<Announcement> enabled = announcements.stream().filter(Announcement::enabled).toList();
        if (enabled.isEmpty()) return;

        Announcement ann;
        if (ConfigHandler.config.announcementUseRandomOrder.get()) {
            ann = enabled.get(random.nextInt(enabled.size()));
        } else {
            if (currentIndex >= enabled.size()) currentIndex = 0;
            ann = enabled.get(currentIndex++);
        }

        if ("command".equalsIgnoreCase(ann.type())) {
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), ann.message());
        } else {
            MutableComponent component = TextFormatter.stringToFormattedText(ann.message());
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (!toggledOff.contains(player.getUUID())) {
                    player.sendSystemMessage(component);
                }
            }
        }
    }

    public List<Announcement> getAnnouncements() {
        return announcements;
    }

    public void addAnnouncement(String id, String type, String message) {
        announcements.add(new Announcement(id, type, message, true));
        save();
    }

    public boolean removeAnnouncement(String id) {
        boolean removed = announcements.removeIf(a -> a.id().equals(id));
        if (removed) save();
        return removed;
    }

    public boolean togglePlayer(UUID uuid) {
        if (toggledOff.contains(uuid)) {
            toggledOff.remove(uuid);
            savePrefs();
            return true; // now ON
        } else {
            toggledOff.add(uuid);
            savePrefs();
            return false; // now OFF
        }
    }

    public boolean isToggledOff(UUID uuid) {
        return toggledOff.contains(uuid);
    }
}

