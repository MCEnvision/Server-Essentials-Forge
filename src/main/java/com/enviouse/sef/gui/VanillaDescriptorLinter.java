package com.enviouse.sef.gui;

import com.enviouse.sef.kernel.command.PanelContracts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public final class VanillaDescriptorLinter {
    private VanillaDescriptorLinter() {
    }

    public static List<Problem> lint(PanelContracts.PanelDescriptor panel) {
        Objects.requireNonNull(panel, "panel");
        List<Problem> problems = new ArrayList<>();
        if (!panel.titleKey().contains(".")) {
            problems.add(new Problem(panel.id(), "title must use a translation key"));
        }
        if (panel.fallback().route().isBlank() || panel.fallback().usageKey().isBlank()) {
            problems.add(new Problem(panel.id(), "command fallback is incomplete"));
        }
        Set<String> controlIds = new HashSet<>();
        for (PanelContracts.ControlDescriptor control : panel.controls()) {
            if (!controlIds.add(control.id())) {
                problems.add(new Problem(panel.id(), "duplicate control id " + control.id()));
            }
            if (!control.iconId().contains(":")) {
                problems.add(new Problem(panel.id(), "control icon is not namespaced"));
            }
            if (control.destructive() && control.executionContext() == PanelContracts.ExecutionContext.ACTOR
                    && control.targetPolicy() == PanelContracts.TargetPolicy.NONE) {
                problems.add(new Problem(panel.id(), "destructive actor control has no explicit target"));
            }
        }
        return List.copyOf(problems);
    }

    public static List<Problem> lint(PanelContracts.Registry registry) {
        List<Problem> problems = new ArrayList<>();
        registry.panels().values().stream()
                .sorted(java.util.Comparator.comparing(PanelContracts.PanelDescriptor::id))
                .forEach(panel -> problems.addAll(lint(panel)));
        registry.commandOnlyDescriptors().stream()
                .sorted()
                .forEach(id -> problems.add(new Problem(id, "command only descriptor has no enhanced screen")));
        return List.copyOf(problems);
    }

    public record Problem(String ownerId, String message) {
        public Problem {
            ownerId = bounded(ownerId, 128).toLowerCase(Locale.ROOT);
            message = bounded(message, 256);
        }
    }

    private static String bounded(String value, int maximumLength) {
        String bounded = Objects.requireNonNull(value, "value").trim();
        if (bounded.isBlank() || bounded.length() > maximumLength
                || bounded.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Descriptor lint text is outside bounds");
        }
        return bounded;
    }
}
