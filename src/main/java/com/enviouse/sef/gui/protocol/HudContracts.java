package com.enviouse.sef.gui.protocol;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class HudContracts {
    private HudContracts() {
    }

    public static Registry phaseNineDefaults() {
        Registry registry = new Registry();
        registry.register(new Descriptor(
                "vanish",
                SefPayloads.HudSurface.TILE,
                "sef.kernel.hud.use",
                FallbackSurface.ACTION_BAR,
                Ownership.SEF));
        registry.register(new Descriptor(
                "social_spy",
                SefPayloads.HudSurface.ALERT,
                "sef.socialspy.view.metadata",
                FallbackSurface.CHAT,
                Ownership.COMMAND));
        registry.register(new Descriptor(
                "command_spy",
                SefPayloads.HudSurface.ALERT,
                "sef.commandspy.view.metadata",
                FallbackSurface.CHAT,
                Ownership.COMMAND));
        registry.register(new Descriptor(
                "afk",
                SefPayloads.HudSurface.TILE,
                "sef.commands.afk",
                FallbackSurface.CHAT,
                Ownership.COMMAND));
        registry.register(new Descriptor(
                "fly",
                SefPayloads.HudSurface.TILE,
                "sef.commands.fly",
                FallbackSurface.CHAT,
                Ownership.COMMAND));
        registry.register(new Descriptor(
                "god",
                SefPayloads.HudSurface.TILE,
                "sef.commands.god",
                FallbackSurface.CHAT,
                Ownership.COMMAND));
        registry.register(new Descriptor(
                "teleport_warmup",
                SefPayloads.HudSurface.PROGRESS,
                "sef.kernel.hud.use",
                FallbackSurface.ACTION_BAR,
                Ownership.SEF));
        return registry;
    }

    public record Descriptor(
            String id,
            SefPayloads.HudSurface surface,
            String permissionId,
            FallbackSurface fallback,
            Ownership fallbackOwner
    ) {
        public Descriptor {
            id = bounded(id, 64);
            Objects.requireNonNull(surface, "surface");
            permissionId = bounded(permissionId, 128);
            Objects.requireNonNull(fallback, "fallback");
            Objects.requireNonNull(fallbackOwner, "fallbackOwner");
            if (fallback == FallbackSurface.NONE && fallbackOwner != Ownership.NONE
                    || fallback != FallbackSurface.NONE && fallbackOwner == Ownership.NONE) {
                throw new IllegalArgumentException("HUD fallback ownership is incomplete");
            }
        }
    }

    public enum FallbackSurface {
        NONE,
        ACTION_BAR,
        BOSS_BAR,
        CHAT
    }

    public enum Ownership {
        NONE,
        SEF,
        COMMAND,
        EXTERNAL
    }

    public static final class Registry {
        private final Map<String, Descriptor> descriptors = new LinkedHashMap<>();

        public synchronized void register(Descriptor descriptor) {
            Objects.requireNonNull(descriptor, "descriptor");
            if (!descriptors.containsKey(descriptor.id()) && descriptors.size() >= 128) {
                throw new IllegalStateException("HUD descriptor limit reached");
            }
            if (descriptors.putIfAbsent(descriptor.id(), descriptor) != null) {
                throw new IllegalStateException("Duplicate HUD descriptor " + descriptor.id());
            }
        }

        public synchronized Optional<Descriptor> find(String id) {
            return Optional.ofNullable(descriptors.get(bounded(id, 64)));
        }

        public synchronized Map<String, Descriptor> descriptors() {
            return Map.copyOf(descriptors);
        }
    }

    private static String bounded(String value, int maximum) {
        String normalized = Objects.requireNonNull(value, "value").trim().toLowerCase(java.util.Locale.ROOT);
        if (normalized.isBlank()
                || normalized.length() > maximum
                || normalized.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("HUD contract text is outside bounds");
        }
        return normalized;
    }
}
