package com.jeremiahbl.bfcrmod.filter;

import com.jeremiahbl.bfcrmod.BetterForgeChat;
import com.jeremiahbl.bfcrmod.config.ConfigHandler;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;

public class FilterManager {
    private final FilterDataStore store = new FilterDataStore();

    public void load(MinecraftServer server) {
        Path dir = server.getServerDirectory().toPath().resolve("serverconfig").resolve("bfcrr");
        store.setPath(dir.resolve("filters.json"));
        store.load();
    }

    public Map<String, FilterDataStore.FilterRecord> list() {
        return store.getFilters();
    }

    public void addFilter(String id, String wordToFilter, String replacement, boolean caseSensitive) {
        store.getFilters().put(id, new FilterDataStore.FilterRecord(wordToFilter, replacement, caseSensitive));
        store.save();
        BetterForgeChat.LOGGER.info("[BFCRR] Added word filter: {}", id);
    }

    public boolean removeFilter(String id) {
        if (store.getFilters().remove(id) != null) {
            store.save();
            BetterForgeChat.LOGGER.info("[BFCRR] Removed word filter: {}", id);
            return true;
        }
        return false;
    }

    public String applyFilters(String message) {
        if (!ConfigHandler.config.enableFilterSystem.get()) return message;
        for (FilterDataStore.FilterRecord rec : store.getFilters().values()) {
            if (rec.caseSensitive()) {
                message = message.replace(rec.wordToFilter(), rec.replacement());
            } else {
                message = message.replaceAll(
                    "(?i)" + Pattern.quote(rec.wordToFilter()),
                    rec.replacement()
                );
            }
        }
        return message;
    }
}

