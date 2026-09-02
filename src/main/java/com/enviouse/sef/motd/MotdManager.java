package com.enviouse.sef.motd;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.storage.StorageLifecycle;
import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.nio.file.Path;

public class MotdManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int MAXIMUM_LINE_LENGTH = 1_024;
    private final Path configPath;
    private MotdData data = new MotdData("", "");
    private StorageService.Document document;
    private StorageRepository.RepositoryState state = StorageRepository.RepositoryState.NEW;

    public record MotdData(String line1, String line2) {
    }

    public MotdManager(Path configDir) {
        this.configPath = configDir.resolve("motd.json").toAbsolutePath().normalize();
    }

    public synchronized void load() {
        StorageService.Document candidate = StorageService.read(configPath, "motd", 1).orElse(null);
        if (candidate == null) {
            StorageRepository.RepositoryState detected = StorageLifecycle.stateFor(configPath);
            state = detected == StorageRepository.RepositoryState.MISSING
                    && data.line1().isEmpty()
                    && data.line2().isEmpty()
                    ? detected
                    : StorageRepository.RepositoryState.RECOVERY;
            return;
        }
        try {
            MotdData loaded = GSON.fromJson(candidate.data(), MotdData.class);
            if (loaded == null) {
                throw new IllegalStateException("MOTD snapshot is missing");
            }
            MotdData validated = new MotdData(
                    bounded(loaded.line1()),
                    bounded(loaded.line2()));
            data = validated;
            document = candidate;
            state = StorageRepository.RepositoryState.READY;
            ServerEssentialsForge.LOGGER.info("[SEF] MOTD loaded");
            if (candidate.migrated() && !save()) {
                state = StorageRepository.RepositoryState.ERROR;
            }
        } catch (RuntimeException exception) {
            state = StorageRepository.RepositoryState.RECOVERY;
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to load MOTD", exception);
        }
    }

    public synchronized boolean save() {
        if (!StorageLifecycle.writable(state)) {
            return false;
        }
        try {
            StorageService.write(configPath, "motd", 1, GSON.toJsonTree(data), document);
            document = StorageService.read(configPath, "motd", 1).orElse(document);
            state = StorageRepository.RepositoryState.READY;
            return true;
        } catch (IOException | RuntimeException exception) {
            state = StorageRepository.RepositoryState.ERROR;
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to save MOTD", exception);
            return false;
        }
    }

    public synchronized void setMotd(String line1, String line2) {
        if (!StorageLifecycle.writable(state)) {
            throw new IllegalStateException("MOTD storage is unavailable in " + state + " state");
        }
        MotdData previous = data;
        data = new MotdData(bounded(line1), bounded(line2));
        if (!save()) {
            data = previous;
            throw new IllegalStateException("MOTD could not be persisted");
        }
    }

    public synchronized MotdData getData() {
        return data;
    }

    public synchronized StorageRepository.RepositoryState state() {
        return state;
    }

    public synchronized void applyToServer(MinecraftServer server) {
        String motd = data.line1();
        if (!data.line2().isEmpty()) {
            motd += "\n" + data.line2();
        }
        if (motd.isEmpty()) {
            return;
        }
        motd = motd.replace("&0", "\u00A70").replace("&1", "\u00A71").replace("&2", "\u00A72")
                .replace("&3", "\u00A73").replace("&4", "\u00A74").replace("&5", "\u00A75")
                .replace("&6", "\u00A76").replace("&7", "\u00A77").replace("&8", "\u00A78")
                .replace("&9", "\u00A79").replace("&a", "\u00A7a").replace("&b", "\u00A7b")
                .replace("&c", "\u00A7c").replace("&d", "\u00A7d").replace("&e", "\u00A7e")
                .replace("&f", "\u00A7f").replace("&l", "\u00A7l").replace("&o", "\u00A7o")
                .replace("&n", "\u00A7n").replace("&m", "\u00A7m").replace("&k", "\u00A7k")
                .replace("&r", "\u00A7r");
        server.setMotd(motd);
    }

    private static String bounded(String value) {
        String normalized = value == null ? "" : value;
        if (normalized.length() > MAXIMUM_LINE_LENGTH
                || normalized.codePoints().anyMatch(character ->
                Character.isISOControl(character) && character != '\t')) {
            throw new IllegalArgumentException("MOTD line is outside bounds");
        }
        return normalized;
    }
}
