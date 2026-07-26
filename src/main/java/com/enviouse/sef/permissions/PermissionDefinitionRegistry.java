package com.enviouse.sef.permissions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class PermissionDefinitionRegistry<T> {
    public record Definition<T>(
            String id,
            boolean defaultValue,
            String name,
            String description,
            T value
    ) {
    }

    private final Map<String, Definition<T>> definitions = new LinkedHashMap<>();

    public synchronized T register(
            String id,
            boolean defaultValue,
            String name,
            String description,
            Supplier<T> valueFactory
    ) {
        if (definitions.containsKey(id)) {
            throw new IllegalStateException("Duplicate permission node " + id);
        }
        T value = valueFactory.get();
        definitions.put(id, new Definition<>(id, defaultValue, name, description, value));
        return value;
    }

    public synchronized List<Definition<T>> definitions() {
        return Collections.unmodifiableList(new ArrayList<>(definitions.values()));
    }

    public synchronized Map<String, Boolean> defaults() {
        Map<String, Boolean> defaults = new LinkedHashMap<>();
        definitions.forEach((id, definition) -> defaults.put(id, definition.defaultValue()));
        return Collections.unmodifiableMap(defaults);
    }
}
