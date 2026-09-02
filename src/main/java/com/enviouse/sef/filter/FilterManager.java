package com.enviouse.sef.filter;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.config.ConfigHandler;
import net.minecraft.server.MinecraftServer;
import com.enviouse.sef.storage.repository.StorageRepository;

import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilterManager {
    private final FilterDataStore store = new FilterDataStore();

    public void load(MinecraftServer server) {
        Path dir = server.getServerDirectory().resolve("serverconfig").resolve("sef");
        store.setPath(dir.resolve("filters.json"));
        store.load();
    }

    public Map<String, FilterDataStore.FilterRecord> list() {
        return store.getFilters();
    }

    public void addFilter(String id, String wordToFilter, String replacement, boolean caseSensitive) {
        store.put(id, new FilterDataStore.FilterRecord(wordToFilter, replacement, caseSensitive));
        ServerEssentialsForge.LOGGER.info("[SEF] Added word filter: {}", id);
    }

    public boolean removeFilter(String id) {
        if (store.remove(id)) {
            ServerEssentialsForge.LOGGER.info("[SEF] Removed word filter: {}", id);
            return true;
        }
        return false;
    }

    public String applyFilters(String message) {
        if (!ConfigHandler.config.enableFilterSystem.get()) return message;
        if (!available()) {
            throw new IllegalStateException("Filter enforcement storage is unavailable");
        }
        for (FilterDataStore.FilterRecord rec : store.getFilters().values()) {
            if (rec.caseSensitive()) {
                message = message.replace(rec.wordToFilter(), rec.replacement());
            } else {
                message = message.replaceAll(
                    "(?i)" + Pattern.quote(rec.wordToFilter()),
                    Matcher.quoteReplacement(rec.replacement())
                );
            }
        }
        return message;
    }

    public boolean available() {
        StorageRepository.RepositoryState state = store.state();
        return state == StorageRepository.RepositoryState.READY
                || state == StorageRepository.RepositoryState.MISSING;
    }

    FilterDataStore store() {
        return store;
    }
}
