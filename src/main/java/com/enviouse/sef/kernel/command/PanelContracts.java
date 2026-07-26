package com.enviouse.sef.kernel.command;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class PanelContracts {
    private PanelContracts() {
    }

    public record PanelDescriptor(
            String id,
            String titleKey,
            int rows,
            String permissionId,
            List<ControlDescriptor> controls,
            CommandFallback fallback
    ) {
        public PanelDescriptor {
            id = normalize(id);
            titleKey = normalize(titleKey);
            permissionId = normalize(permissionId);
            Objects.requireNonNull(controls, "controls");
            if (controls.size() > 54) {
                throw new IllegalArgumentException("Panel control count exceeds hard limit");
            }
            controls = List.copyOf(Objects.requireNonNull(controls, "controls"));
            Objects.requireNonNull(fallback, "fallback");
            if (rows < 1 || rows > 6) {
                throw new IllegalArgumentException("Panel rows must be between one and six");
            }
            boolean[] occupied = new boolean[rows * 9];
            for (ControlDescriptor control : controls) {
                int end = control.slot() + control.span();
                if (control.slot() < 0 || end > occupied.length) {
                    throw new IllegalArgumentException("Panel control is outside the grid");
                }
                for (int slot = control.slot(); slot < end; slot++) {
                    if (occupied[slot]) {
                        throw new IllegalArgumentException("Panel controls overlap at slot " + slot);
                    }
                    occupied[slot] = true;
                }
            }
        }
    }

    public record ControlDescriptor(
            String id,
            int slot,
            int span,
            String actionId,
            String permissionId,
            TargetPolicy targetPolicy,
            boolean destructive,
            String iconId
    ) {
        public ControlDescriptor {
            id = normalize(id);
            actionId = normalize(actionId);
            permissionId = normalize(permissionId);
            iconId = normalize(iconId);
            Objects.requireNonNull(targetPolicy, "targetPolicy");
            if (span < 1 || span > 9) {
                throw new IllegalArgumentException("Control span must be between one and nine");
            }
        }
    }

    public record CommandFallback(String route, String usageKey) {
        public CommandFallback {
            route = normalize(route);
            usageKey = normalize(usageKey);
            if (route.startsWith("/")) {
                throw new IllegalArgumentException("Fallback route must not start with slash");
            }
        }
    }

    public record TargetReference(TargetKind kind, UUID playerId, long revision) {
        public TargetReference {
            Objects.requireNonNull(kind, "kind");
            if ((kind == TargetKind.EXPLICIT_PLAYER || kind == TargetKind.SELECTED_PLAYER) && playerId == null) {
                throw new IllegalArgumentException("Player target requires UUID");
            }
            if (revision < 0) {
                throw new IllegalArgumentException("Target revision cannot be negative");
            }
        }

        public static TargetReference self(UUID playerId, long revision) {
            return new TargetReference(TargetKind.SELF, Objects.requireNonNull(playerId, "playerId"), revision);
        }
    }

    public enum TargetPolicy {
        NONE,
        SELF,
        EXPLICIT_VISIBLE_PLAYER,
        SELECTED_VISIBLE_PLAYER,
        BOUNDED_AUDIENCE,
        SERVER
    }

    public enum TargetKind {
        NONE,
        SELF,
        EXPLICIT_PLAYER,
        SELECTED_PLAYER,
        SERVER
    }

    public static final class Registry {
        private final java.util.Map<String, PanelDescriptor> panels = new java.util.LinkedHashMap<>();
        private final Set<String> nonPanelDescriptors = new java.util.LinkedHashSet<>();

        public synchronized void register(PanelDescriptor descriptor) {
            if (!panels.containsKey(descriptor.id())
                    && !nonPanelDescriptors.contains(descriptor.id())
                    && panels.size() + nonPanelDescriptors.size() >= 1024) {
                throw new IllegalStateException("GUI descriptor limit reached");
            }
            if (panels.putIfAbsent(descriptor.id(), descriptor) != null
                    || nonPanelDescriptors.contains(descriptor.id())) {
                throw new IllegalStateException("Duplicate GUI descriptor " + descriptor.id());
            }
        }

        public synchronized void registerCommandOnly(String descriptorId) {
            String id = normalize(descriptorId);
            if (!panels.containsKey(id)
                    && !nonPanelDescriptors.contains(id)
                    && panels.size() + nonPanelDescriptors.size() >= 1024) {
                throw new IllegalStateException("GUI descriptor limit reached");
            }
            if (panels.containsKey(id) || !nonPanelDescriptors.add(id)) {
                throw new IllegalStateException("Duplicate GUI descriptor " + id);
            }
        }

        public synchronized boolean contains(String id) {
            String normalized = normalize(id);
            return panels.containsKey(normalized) || nonPanelDescriptors.contains(normalized);
        }

        public synchronized int size() {
            return panels.size() + nonPanelDescriptors.size();
        }
    }

    private static String normalize(String value) {
        String normalized = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank() || normalized.length() > 128) {
            throw new IllegalArgumentException("Panel identifier is outside bounds");
        }
        return normalized;
    }
}
