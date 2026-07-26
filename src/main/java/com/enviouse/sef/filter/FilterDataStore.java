package com.enviouse.sef.filter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.storage.StorageService;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class FilterDataStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<Map<String, FilterRecord>>(){}.getType();

    public record FilterRecord(String wordToFilter, String replacement, boolean caseSensitive) {}

    private final Map<String, FilterRecord> filters = new LinkedHashMap<>();
    private Path filePath;
    private StorageService.Document document;

    public void setPath(Path path) {
        this.filePath = path;
    }

    public Map<String, FilterRecord> getFilters() {
        return filters;
    }

    public void load() {
        filters.clear();
        if (filePath == null) return;
        document = StorageService.read(filePath, "filters", 1).orElse(null);
        if (document == null) return;
        try {
            Map<String, FilterRecord> loaded = GSON.fromJson(document.data(), MAP_TYPE);
            if (loaded != null) filters.putAll(loaded);
            ServerEssentialsForge.LOGGER.info("[SEF] Loaded {} word filter(s)", filters.size());
            if (document.migrated()) save();
        } catch (Exception e) {
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to load filters", e);
        }
    }

    public void save() {
        if (filePath == null) return;
        try {
            StorageService.write(filePath, "filters", 1, GSON.toJsonTree(filters), document, Set.of(""));
        } catch (IOException e) {
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to save filters", e);
        }
    }
}
