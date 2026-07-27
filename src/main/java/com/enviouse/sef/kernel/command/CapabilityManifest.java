package com.enviouse.sef.kernel.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class CapabilityManifest {
    private static final int MAXIMUM_CAPABILITIES = 32_768;
    private static final int MAXIMUM_IDENTIFIER_LENGTH = 128;
    private final Map<String, Capability> capabilities = new LinkedHashMap<>();

    public synchronized void register(Capability capability) {
        Objects.requireNonNull(capability, "capability");
        if (!capabilities.containsKey(capability.id())
                && capabilities.size() >= MAXIMUM_CAPABILITIES) {
            throw new IllegalStateException("Capability manifest limit reached");
        }
        if (capabilities.putIfAbsent(capability.id(), capability) != null) {
            throw new IllegalStateException("Duplicate capability " + capability.id());
        }
    }

    public synchronized boolean contains(String id) {
        return capabilities.containsKey(normalize(id));
    }

    public synchronized Capability get(String id) {
        return capabilities.get(normalize(id));
    }

    public synchronized List<Capability> entries() {
        return Collections.unmodifiableList(new ArrayList<>(capabilities.values()));
    }

    public synchronized Map<String, Boolean> defaults() {
        Map<String, Boolean> values = new LinkedHashMap<>();
        capabilities.forEach((id, capability) -> values.put(id, capability.defaultAllowed()));
        return Collections.unmodifiableMap(values);
    }

    public synchronized int size() {
        return capabilities.size();
    }

    public static CapabilityType inferType(String permissionId) {
        String id = normalize(permissionId);
        if (id.contains(".sensitive") || id.contains(".ip.") || id.contains(".raw.")) {
            return CapabilityType.SENSITIVE_DATA;
        }
        if (id.contains(".bypass")) {
            return CapabilityType.BYPASS;
        }
        if (id.contains(".alias")) {
            return CapabilityType.ALIAS;
        }
        if (id.contains(".bundle")) {
            return CapabilityType.BUNDLE;
        }
        if (id.contains(".profile")) {
            return CapabilityType.PROFILE;
        }
        if (id.contains(".panel")) {
            return CapabilityType.PANEL;
        }
        if (id.contains(".hud")) {
            return CapabilityType.HUD;
        }
        if (id.contains(".gui")) {
            return CapabilityType.GUI;
        }
        if (id.contains(".editor") || id.contains(".edit") || id.contains(".manage")) {
            return CapabilityType.EDITOR;
        }
        if (id.contains(".target") || id.contains(".others")) {
            return CapabilityType.TARGET;
        }
        if (id.contains(".audience")) {
            return CapabilityType.AUDIENCE;
        }
        if (id.contains(".discover") || id.contains(".list") || id.contains(".status")) {
            return CapabilityType.DISCOVERY;
        }
        return CapabilityType.COMMAND;
    }

    private static String normalize(String value) {
        String normalized = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank() || normalized.length() > MAXIMUM_IDENTIFIER_LENGTH) {
            throw new IllegalArgumentException("Capability identifier is outside bounds");
        }
        return normalized;
    }

    public record Capability(
            String id,
            CapabilityType type,
            boolean defaultAllowed,
            String name,
            String description
    ) {
        public Capability {
            id = normalize(id);
            Objects.requireNonNull(type, "type");
            name = Objects.requireNonNull(name, "name").trim();
            description = Objects.requireNonNull(description, "description").trim();
            if (name.isBlank() || description.isBlank()
                    || name.length() > 128
                    || description.length() > 512) {
                throw new IllegalArgumentException("Capability metadata is incomplete");
            }
        }
    }

    public enum CapabilityType {
        COMMAND,
        DISCOVERY,
        GUI,
        HUD,
        PANEL,
        TARGET,
        AUDIENCE,
        EDITOR,
        ALIAS,
        BUNDLE,
        PROFILE,
        BYPASS,
        SENSITIVE_DATA
    }
}
