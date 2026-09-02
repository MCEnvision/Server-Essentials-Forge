package com.enviouse.sef.kernel.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class CommandCatalog {
    private final CapabilityManifest capabilities;
    private final PanelContracts.Registry descriptors;
    private final Map<String, CommandDefinition> byId = new LinkedHashMap<>();
    private final Map<String, String> byRoute = new LinkedHashMap<>();
    private boolean sealed;

    public CommandCatalog(CapabilityManifest capabilities, PanelContracts.Registry descriptors) {
        this.capabilities = Objects.requireNonNull(capabilities, "capabilities");
        this.descriptors = Objects.requireNonNull(descriptors, "descriptors");
    }

    public synchronized void register(CommandDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        requireMutable();
        if (!byId.containsKey(definition.id()) && byId.size() >= 8192) {
            throw new IllegalStateException("Command catalog limit reached");
        }
        if (byId.putIfAbsent(definition.id(), definition) != null) {
            throw new IllegalStateException("Duplicate command id " + definition.id());
        }
        String routeOwner = byRoute.putIfAbsent(definition.canonicalRoute(), definition.id());
        if (routeOwner != null) {
            byId.remove(definition.id());
            throw new IllegalStateException(
                    "Duplicate canonical route " + definition.canonicalRoute() + " owned by " + routeOwner);
        }
    }

    public synchronized List<Problem> validate() {
        List<Problem> problems = new ArrayList<>();
        for (CommandDefinition definition : byId.values()) {
            for (String permissionId : definition.permissionIds()) {
                if (!capabilities.contains(permissionId)) {
                    problems.add(new Problem(
                            Severity.ERROR,
                            definition.id(),
                            "unknown permission " + permissionId));
                }
            }
            if (definition.playerFacing() && !descriptors.contains(definition.guiDescriptorId())) {
                problems.add(new Problem(
                        Severity.ERROR,
                        definition.id(),
                        "unknown gui descriptor " + definition.guiDescriptorId()));
            }
            if (!definition.pipelineEnforced()) {
                problems.add(new Problem(
                        Severity.ERROR,
                        definition.id(),
                        "shared pipeline is not enforced"));
            }
            if (definition.quotaId().isBlank() == definition.quotaNotApplicableReason().isBlank()) {
                problems.add(new Problem(
                        Severity.ERROR,
                        definition.id(),
                        "quota policy is incomplete"));
            }
            if (definition.accessClass().isPrivileged()
                    && definition.auditClass() == com.enviouse.sef.audit.AuditService.AuditClass.NONE) {
                problems.add(new Problem(
                        Severity.ERROR,
                        definition.id(),
                        "privileged command has no audit policy"));
            }
        }
        for (PanelContracts.PanelDescriptor panel : descriptors.panels().values()) {
            if (!capabilities.contains(panel.permissionId())) {
                problems.add(new Problem(
                        Severity.ERROR,
                        panel.id(),
                        "unknown panel permission " + panel.permissionId()));
            }
            for (PanelContracts.ControlDescriptor control : panel.controls()) {
                if (!byId.containsKey(control.actionId())) {
                    problems.add(new Problem(
                            Severity.ERROR,
                            panel.id(),
                            "unknown panel action " + control.actionId()));
                }
                if (!capabilities.contains(control.permissionId())) {
                    problems.add(new Problem(
                            Severity.ERROR,
                            panel.id(),
                            "unknown control permission " + control.permissionId()));
                }
                if (!capabilities.contains(control.audience().permissionId())) {
                    problems.add(new Problem(
                            Severity.ERROR,
                            panel.id(),
                            "unknown audience permission " + control.audience().permissionId()));
                }
            }
        }
        return List.copyOf(problems);
    }

    public synchronized void seal() {
        List<Problem> problems = validate();
        List<Problem> errors = problems.stream().filter(problem -> problem.severity() == Severity.ERROR).toList();
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Command catalog validation failed. " + errors);
        }
        sealed = true;
    }

    public synchronized boolean sealed() {
        return sealed;
    }

    public synchronized Optional<CommandDefinition> find(String id) {
        return Optional.ofNullable(byId.get(normalize(id)));
    }

    public synchronized Optional<CommandDefinition> findByRoute(String route) {
        String owner = byRoute.get(normalize(route));
        return owner == null ? Optional.empty() : Optional.ofNullable(byId.get(owner));
    }

    public synchronized Optional<String> rootOwner(String root) {
        String normalizedRoot = normalize(root);
        for (CommandDefinition definition : byId.values()) {
            String routeRoot = definition.canonicalRoute().split("\\s+", 2)[0];
            if (routeRoot.equals(normalizedRoot) || definition.convenienceRoots().contains(normalizedRoot)) {
                return Optional.of(definition.id());
            }
        }
        return Optional.empty();
    }

    public synchronized List<CommandDefinition> entries() {
        return byId.values().stream()
                .sorted(Comparator.comparing(CommandDefinition::id))
                .toList();
    }

    public synchronized Map<String, String> routes() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(byRoute));
    }

    public synchronized int size() {
        return byId.size();
    }

    private void requireMutable() {
        if (sealed) {
            throw new IllegalStateException("Command catalog is sealed");
        }
    }

    private static String normalize(String value) {
        return Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
    }

    public record Problem(Severity severity, String ownerId, String message) {
        public Problem {
            Objects.requireNonNull(severity, "severity");
            ownerId = normalize(ownerId);
            message = Objects.requireNonNull(message, "message").trim();
        }
    }

    public enum Severity {
        INFO,
        WARNING,
        ERROR
    }
}
