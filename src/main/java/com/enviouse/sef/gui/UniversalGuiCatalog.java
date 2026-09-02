package com.enviouse.sef.gui;

import com.enviouse.sef.kernel.command.CommandCatalog;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.kernel.command.PanelContracts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class UniversalGuiCatalog {
    private static final Map<String, CategoryTemplate> CATEGORIES = Map.ofEntries(
            Map.entry("sef:gui", new CategoryTemplate("dashboard", "Server essentials", "minecraft:compass")),
            Map.entry("sef:core", new CategoryTemplate("category_core", "Server and settings", "minecraft:command_block")),
            Map.entry("sef:workstations", new CategoryTemplate("category_workstations", "Workstations", "minecraft:crafting_table")),
            Map.entry("sef:teleports", new CategoryTemplate("category_teleports", "Teleports", "minecraft:ender_pearl")),
            Map.entry("sef:social", new CategoryTemplate("category_social", "Social", "minecraft:writable_book")),
            Map.entry("sef:observation", new CategoryTemplate("category_observation", "Observation and logs", "minecraft:spyglass")),
            Map.entry("sef:moderation", new CategoryTemplate("category_moderation", "Moderation", "minecraft:iron_bars")),
            Map.entry("sef:protection", new CategoryTemplate("category_protection", "Protection", "minecraft:shield")),
            Map.entry("sef:inventory", new CategoryTemplate("category_inventory", "Inventory administration", "minecraft:chest")),
            Map.entry("sef:kits", new CategoryTemplate("category_kits", "Kits", "minecraft:bundle")),
            Map.entry("sef:utilities", new CategoryTemplate("category_utilities", "Player utilities", "minecraft:nether_star")),
            Map.entry("sef:economy", new CategoryTemplate("category_economy", "Economy", "minecraft:emerald")),
            Map.entry("sef:settings", new CategoryTemplate("category_settings", "Configuration safe settings", "minecraft:comparator")),
            Map.entry("sef:integrations", new CategoryTemplate("category_integrations", "Integrations", "minecraft:observer")),
            Map.entry("sef:panels", new CategoryTemplate("category_panels", "Administrative panels", "minecraft:structure_block")),
            Map.entry("sef:aliases", new CategoryTemplate("category_aliases", "Aliases and bundles", "minecraft:repeater")),
            Map.entry("sef:tags", new CategoryTemplate("category_tags", "Fancy tags", "minecraft:name_tag")),
            Map.entry("sef:identity", new CategoryTemplate("category_identity", "Identity and disguise", "minecraft:player_head")),
            Map.entry("sef:control", new CategoryTemplate("category_control", "Server control", "minecraft:structure_block")));

    private final Map<String, Category> categories;
    private final Map<String, ActionRoute> byAction;
    private final Map<String, List<ActionRoute>> byPanel;

    private UniversalGuiCatalog(
            Map<String, Category> categories,
            Map<String, ActionRoute> byAction,
            Map<String, List<ActionRoute>> byPanel
    ) {
        this.categories = Map.copyOf(categories);
        this.byAction = Map.copyOf(byAction);
        this.byPanel = Map.copyOf(byPanel);
    }

    public static UniversalGuiCatalog build(CommandCatalog catalog, PanelContracts.Registry descriptors) {
        Objects.requireNonNull(catalog, "catalog");
        Objects.requireNonNull(descriptors, "descriptors");
        Map<String, Category> categories = new LinkedHashMap<>();
        CATEGORIES.forEach((descriptorId, template) -> {
            PanelContracts.PanelDescriptor descriptor = descriptors.panel(descriptorId).orElse(null);
            PanelContracts.CommandFallback fallback = descriptor == null
                    ? new PanelContracts.CommandFallback("sef commands", "sef.gui.catalog.usage")
                    : descriptor.fallback();
            categories.put(template.panelId(), new Category(
                    descriptorId,
                    template.panelId(),
                    template.title(),
                    template.iconId(),
                    fallback));
        });

        Map<String, ActionRoute> byAction = new LinkedHashMap<>();
        Map<String, List<ActionRoute>> byPanel = new LinkedHashMap<>();
        for (CommandDefinition definition : catalog.entries()) {
            if (!definition.playerFacing()) {
                continue;
            }
            CategoryTemplate template = CATEGORIES.get(definition.guiDescriptorId());
            if (template == null) {
                throw new IllegalStateException(
                        "No universal GUI category maps descriptor " + definition.guiDescriptorId());
            }
            ActionRoute route = new ActionRoute(
                    definition.id(),
                    template.panelId(),
                    definition.canonicalRoute(),
                    definition.permissionIds(),
                    definition.targetBehavior(),
                    definition.accessClass().isPrivileged()
                            || definition.auditClass() != com.enviouse.sef.audit.AuditService.AuditClass.METADATA_ONLY,
                    definition.hudDescriptorId(),
                    definition.hudNotApplicableReason(),
                    definition.featureId(),
                    coverage(definition).mode(),
                    coverage(definition).reason());
            if (byAction.putIfAbsent(route.actionId(), route) != null) {
                throw new IllegalStateException("Duplicate universal GUI action " + route.actionId());
            }
            byPanel.computeIfAbsent(route.panelId(), ignored -> new ArrayList<>()).add(route);
        }
        byPanel.replaceAll((panelId, actions) -> actions.stream()
                .sorted(Comparator.comparing(ActionRoute::commandRoute))
                .toList());
        return new UniversalGuiCatalog(categories, byAction, byPanel);
    }

    public Optional<Category> category(String panelId) {
        return Optional.ofNullable(categories.get(normalize(panelId)));
    }

    public Optional<ActionRoute> action(String actionId) {
        return Optional.ofNullable(byAction.get(normalize(actionId)));
    }

    public List<ActionRoute> actions(String panelId) {
        return byPanel.getOrDefault(normalize(panelId), List.of());
    }

    public List<Category> categories() {
        return categories.values().stream()
                .filter(category -> !category.panelId().equals("dashboard"))
                .sorted(Comparator.comparing(Category::title))
                .toList();
    }

    public List<String> validate(CommandCatalog catalog) {
        List<String> problems = new ArrayList<>();
        for (CommandDefinition definition : catalog.entries()) {
            if (!definition.playerFacing()) {
                continue;
            }
            ActionRoute route = byAction.get(definition.id());
            if (route == null) {
                problems.add(definition.id() + " has no GUI route");
                continue;
            }
            if (route.commandRoute().isBlank()) {
                problems.add(definition.id() + " has no command fallback");
            }
            if (route.hudDescriptorId().isBlank() == route.hudNotApplicableReason().isBlank()) {
                problems.add(definition.id() + " has incomplete HUD coverage");
            }
            if (route.workflowMode() == WorkflowMode.TYPED_COMMAND
                    == !route.workflowReason().isBlank()) {
                problems.add(definition.id() + " has incomplete workflow coverage");
            }
        }
        return List.copyOf(problems);
    }

    public int actionCount() {
        return byAction.size();
    }

    public record Category(
            String descriptorId,
            String panelId,
            String title,
            String iconId,
            PanelContracts.CommandFallback fallback
    ) {
        public Category {
            descriptorId = normalize(descriptorId);
            panelId = normalize(panelId);
            title = bounded(title, 128);
            iconId = normalize(iconId);
            Objects.requireNonNull(fallback, "fallback");
        }
    }

    public record ActionRoute(
            String actionId,
            String panelId,
            String commandRoute,
            Set<String> permissionIds,
            CommandDefinition.TargetBehavior targetBehavior,
            boolean destructive,
            String hudDescriptorId,
            String hudNotApplicableReason,
            String featureId,
            WorkflowMode workflowMode,
            String workflowReason
    ) {
        public ActionRoute {
            actionId = normalize(actionId);
            panelId = normalize(panelId);
            commandRoute = normalize(commandRoute);
            permissionIds = Set.copyOf(Objects.requireNonNull(permissionIds, "permissionIds"));
            Objects.requireNonNull(targetBehavior, "targetBehavior");
            hudDescriptorId = optional(hudDescriptorId);
            hudNotApplicableReason = optionalBounded(hudNotApplicableReason, 256);
            featureId = normalize(featureId);
            Objects.requireNonNull(workflowMode, "workflowMode");
            workflowReason = optionalBounded(workflowReason, 256);
            if (permissionIds.isEmpty()) {
                throw new IllegalArgumentException("GUI action permission set is empty");
            }
            if (workflowMode == WorkflowMode.TYPED_COMMAND == !workflowReason.isBlank()) {
                throw new IllegalArgumentException("GUI workflow coverage is incomplete");
            }
        }
    }

    public enum WorkflowMode {
        TYPED_COMMAND,
        CONTROL_EDITOR,
        PANEL_EDITOR,
        FANCY_TAG_STUDIO,
        DEDICATED_PANEL,
        WORLD_INTERACTION
    }

    private record CategoryTemplate(String panelId, String title, String iconId) {
    }

    private static WorkflowCoverage coverage(CommandDefinition definition) {
        String actionId = definition.id();
        if (actionId.startsWith("sef:control.") && actionId.endsWith(".manage")) {
            return new WorkflowCoverage(
                    WorkflowMode.CONTROL_EDITOR,
                    "the typed server control editor owns create, configure, preview, state, and execute");
        }
        if (actionId.startsWith("sef:tags.")) {
            return new WorkflowCoverage(
                    WorkflowMode.FANCY_TAG_STUDIO,
                    "the fancy tag studio owns tag definitions, assignments, leases, previews, and publication");
        }
        if (actionId.equals("sef:gui.dashboard.open")
                || actionId.startsWith("sef:gui.preference")
                || actionId.startsWith("sef:gui.reminder")
                || actionId.startsWith("sef:guis.")
                || actionId.equals("sef:teleport.player_warp.manage")
                || actionId.equals("sef:workstation.super_enchant.mutate")) {
            return new WorkflowCoverage(
                    WorkflowMode.DEDICATED_PANEL,
                    actionId.equals("sef:teleport.player_warp.manage")
                            ? "the warps panel owns player warp inspection and management"
                            : actionId.equals("sef:workstation.super_enchant.mutate")
                            ? "the server authoritative super enchanting menu owns this mutation"
                            : "the dashboard and preferences screens own this client workflow");
        }
        if (actionId.startsWith("sef:economy.sign.")) {
            return new WorkflowCoverage(
                    WorkflowMode.WORLD_INTERACTION,
                    "the inspected in world sign is required context and owns this interaction");
        }
        return new WorkflowCoverage(WorkflowMode.TYPED_COMMAND, "");
    }

    private record WorkflowCoverage(WorkflowMode mode, String reason) {
    }

    private static String normalize(String value) {
        String normalized = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank() || normalized.length() > 128
                || normalized.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("GUI catalog text is outside bounds");
        }
        return normalized;
    }

    private static String optional(String value) {
        return value == null || value.isBlank() ? "" : normalize(value);
    }

    private static String bounded(String value, int maximumLength) {
        String bounded = Objects.requireNonNull(value, "value").trim();
        if (bounded.isBlank() || bounded.length() > maximumLength
                || bounded.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("GUI catalog text is outside bounds");
        }
        return bounded;
    }

    private static String optionalBounded(String value, int maximumLength) {
        return value == null || value.isBlank() ? "" : bounded(value, maximumLength);
    }
}
