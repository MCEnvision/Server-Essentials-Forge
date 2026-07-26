package com.enviouse.sef.kernel.command;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public final class ShortcutRegistry {
    private static final Pattern ROOT = Pattern.compile("[a-z0-9_:-]+");
    private static final Set<ArgumentAdapter> SAFE_ADAPTERS = Set.of(
            ArgumentAdapter.NONE,
            ArgumentAdapter.OPTIONAL_SELF_TARGET,
            ArgumentAdapter.FIXED_ENUM,
            ArgumentAdapter.REORDER_TYPED_ARGUMENTS);

    private final CommandCatalog catalog;
    private final CapabilityManifest capabilities;
    private final Map<String, Shortcut> shortcuts = new LinkedHashMap<>();
    private final Set<String> rootsPresentBeforeSef = new LinkedHashSet<>();
    private boolean registrationCaptured;

    public ShortcutRegistry(CommandCatalog catalog, CapabilityManifest capabilities) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.capabilities = Objects.requireNonNull(capabilities, "capabilities");
    }

    public synchronized void captureExistingRoots(Set<String> roots) {
        if (registrationCaptured) {
            return;
        }
        roots.stream().map(ShortcutRegistry::normalize).forEach(rootsPresentBeforeSef::add);
        registrationCaptured = true;
    }

    public synchronized void register(Shortcut shortcut) {
        Objects.requireNonNull(shortcut, "shortcut");
        if (!catalog.find(shortcut.actionId()).isPresent()) {
            throw new IllegalArgumentException("Unknown shortcut action " + shortcut.actionId());
        }
        if (!SAFE_ADAPTERS.contains(shortcut.adapter())) {
            throw new IllegalArgumentException("Unsafe shortcut adapter");
        }
        if (!shortcut.additionalPermissionId().isBlank()
                && !capabilities.contains(shortcut.additionalPermissionId())) {
            throw new IllegalArgumentException("Unknown shortcut permission " + shortcut.additionalPermissionId());
        }
        if (shortcuts.putIfAbsent(shortcut.root(), shortcut) != null) {
            throw new IllegalStateException("Duplicate shortcut root " + shortcut.root());
        }
    }

    public synchronized Optional<Shortcut> find(String root) {
        return Optional.ofNullable(shortcuts.get(normalize(root)));
    }

    public synchronized boolean existedBeforeRegistration(String root) {
        return rootsPresentBeforeSef.contains(normalize(root));
    }

    public synchronized String canonicalAction(String entryRoot) {
        Shortcut shortcut = shortcuts.get(normalize(entryRoot));
        return shortcut == null ? "" : shortcut.actionId();
    }

    public synchronized boolean isActive(String root) {
        String normalized = normalize(root);
        return diagnostics().stream()
                .filter(diagnostic -> diagnostic.root().equals(normalized))
                .map(diagnostic -> diagnostic.status() == Status.ACTIVE
                        || diagnostic.status() == Status.ACTIVE_OVERRIDE)
                .findFirst()
                .orElse(false);
    }

    public synchronized List<Diagnostic> diagnostics() {
        List<Diagnostic> results = new ArrayList<>();
        for (Shortcut shortcut : shortcuts.values()) {
            boolean occupied = rootsPresentBeforeSef.contains(shortcut.root());
            Status status;
            String detail;
            if (!occupied) {
                status = Status.ACTIVE;
                detail = "root available";
            } else {
                status = switch (shortcut.collisionMode()) {
                    case PREFER_SEF -> Status.ACTIVE_OVERRIDE;
                    case PREFER_EXISTING, CANONICAL_ONLY -> Status.CANONICAL_ONLY;
                    case FAIL -> Status.CONFLICT;
                    case RESTART_REQUIRED -> Status.RESTART_REQUIRED;
                };
                detail = "root existed before sef registration";
            }
            results.add(new Diagnostic(shortcut.root(), shortcut.actionId(), status, detail));
        }
        results.sort(Comparator.comparing(Diagnostic::root));
        return List.copyOf(results);
    }

    public synchronized Map<String, String> activeAliasMap() {
        Map<String, String> active = new LinkedHashMap<>();
        diagnostics().stream()
                .filter(diagnostic -> diagnostic.status() == Status.ACTIVE
                        || diagnostic.status() == Status.ACTIVE_OVERRIDE)
                .forEach(diagnostic -> active.put(diagnostic.root(), diagnostic.actionId()));
        return Map.copyOf(active);
    }

    public synchronized int size() {
        return shortcuts.size();
    }

    public record Shortcut(
            String root,
            String actionId,
            ArgumentAdapter adapter,
            String additionalPermissionId,
            CommandDefinition.ConflictPolicy collisionMode,
            long structuralRevision
    ) {
        public Shortcut {
            root = normalize(root);
            actionId = normalize(actionId);
            additionalPermissionId = additionalPermissionId == null ? "" : normalize(additionalPermissionId);
            Objects.requireNonNull(adapter, "adapter");
            Objects.requireNonNull(collisionMode, "collisionMode");
            if (!ROOT.matcher(root).matches()) {
                throw new IllegalArgumentException("Invalid shortcut root " + root);
            }
            if (structuralRevision < 1) {
                throw new IllegalArgumentException("Shortcut structural revision must be positive");
            }
        }
    }

    public enum ArgumentAdapter {
        NONE,
        OPTIONAL_SELF_TARGET,
        FIXED_ENUM,
        REORDER_TYPED_ARGUMENTS,
        RAW_STRING_SUBSTITUTION
    }

    public enum Status {
        ACTIVE,
        ACTIVE_OVERRIDE,
        CANONICAL_ONLY,
        CONFLICT,
        RESTART_REQUIRED
    }

    public record Diagnostic(String root, String actionId, Status status, String detail) {
    }

    private static String normalize(String value) {
        return Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
    }
}
