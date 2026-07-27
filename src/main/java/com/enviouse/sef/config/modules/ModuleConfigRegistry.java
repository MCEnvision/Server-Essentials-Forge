package com.enviouse.sef.config.modules;

import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.RuntimeConfigBindings;
import com.enviouse.sef.config.RuntimeConfigValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ModuleConfigRegistry {
    public static final int SCHEMA_VERSION = 1;
    private static final List<String> MODULE_IDS = List.of(
            "core", "commands", "messages", "permissions", "gui", "hud",
            "craft", "anvil", "enchanting", "super_enchanting", "repair", "workstations",
            "homes", "teleport_requests", "spawn", "back", "warps", "player_warps",
            "random_teleport", "direct_teleport",
            "social", "private_messages", "social_spy", "mail", "nicknames",
            "connection_messages", "reminders",
            "moderation", "bans", "kicks", "mutes", "warnings", "jails", "freeze",
            "inventory_lock", "building_control", "vanish", "command_spy", "logger", "audit",
            "inventory", "kits", "player_utilities", "gamemode", "items", "economy",
            "economy_signs", "admin_panels", "aliases", "bundles", "fake_actions", "sudo",
            "run_and_silent", "fancy_tags", "disguise", "integrations", "server_control",
            "performance", "backups", "privacy", "community", "displays");

    private final Map<String, ModuleDefinition> definitions;

    public ModuleConfigRegistry() {
        Map<String, ModuleDefinition> built = new LinkedHashMap<>();
        for (String moduleId : MODULE_IDS) {
            ModuleDefinition definition = definition(moduleId);
            if (built.putIfAbsent(definition.id(), definition) != null) {
                throw new IllegalStateException("duplicate module configuration definition");
            }
        }
        definitions = Map.copyOf(built);
        validateGraph();
    }

    public List<ModuleDefinition> definitions() {
        return definitions.values().stream()
                .sorted(Comparator.comparing(ModuleDefinition::id))
                .toList();
    }

    public ModuleDefinition require(String moduleId) {
        ModuleDefinition definition = definitions.get(normalize(moduleId));
        if (definition == null) {
            throw new IllegalArgumentException("unknown configuration module");
        }
        return definition;
    }

    public boolean contains(String moduleId) {
        return definitions.containsKey(normalize(moduleId));
    }

    public List<String> dependencyClosure(Collection<String> requested) {
        LinkedHashSet<String> closure = new LinkedHashSet<>();
        for (String id : requested) {
            expand(require(id), closure, new LinkedHashSet<>());
        }
        return List.copyOf(closure);
    }

    public String generatedReference() {
        StringBuilder document = new StringBuilder();
        document.append("# SEF modular configuration reference\n\n");
        document.append("This reference is generated from the runtime schema registry. ");
        document.append("Cooldown durations are controlled only by `sef.cooldown.<action>.<seconds>` permissions.\n\n");
        for (ModuleDefinition module : definitions()) {
            document.append("## ").append(module.id()).append("\n\n");
            document.append(module.purpose()).append("\n\n");
            document.append("File: `config/sef/modules/").append(module.fileName()).append("`. ");
            document.append("Schema version: `").append(SCHEMA_VERSION).append("`. ");
            document.append("Documentation version: `").append(module.documentationVersion()).append("`. ");
            document.append("Apply class: `").append(module.applyClass().id()).append("`. ");
            document.append("Dependencies: ");
            document.append(module.dependencies().isEmpty()
                    ? "none"
                    : "`" + String.join("`, `", module.dependencies().stream().sorted().toList()) + "`");
            document.append(". Conflicts: ");
            document.append(module.conflicts().isEmpty()
                    ? "none"
                    : "`" + String.join("`, `", module.conflicts().stream().sorted().toList()) + "`");
            document.append(".\n\n");
            document.append("Inspect permission: `sef.commands.config.inspect`. ");
            document.append("Edit permission: `sef.commands.config.edit`. ");
            document.append("Migration path: inspect the retained legacy `common.toml` with ");
            document.append("`/sef config migrate dryrun`, then request a revision-bound confirmation with ");
            document.append("`/sef config migrate apply <expected_revision>`. ");
            document.append("Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.\n\n");
            document.append("| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |\n");
            document.append("| --- | --- | --- | --- | --- | --- | --- | --- | --- |\n");
            for (SettingDefinition setting : module.settings()) {
                document.append("| `").append(setting.path()).append("` | ")
                        .append(setting.description()).append(" | ")
                        .append(setting.type().id()).append(" | `")
                        .append(setting.defaultValue()).append("` | ")
                        .append(setting.boundsDescription()).append(" | ")
                        .append(unit(setting)).append(" | ")
                        .append(setting.applyClass().id()).append(" | ")
                        .append(setting.sensitivity().id()).append(" | `")
                        .append(setting.path()).append(" = ")
                        .append(setting.tomlDefault().replace("`", "")).append("` |\n");
            }
            document.append("\n");
        }
        return document.toString().stripTrailing() + "\n";
    }

    private static String unit(SettingDefinition setting) {
        String path = setting.path();
        if (path.endsWith("_milliseconds")) {
            return "milliseconds";
        }
        if (path.endsWith("_seconds")) {
            return "seconds";
        }
        if (path.endsWith("_minutes")) {
            return "minutes";
        }
        if (path.endsWith("_hours")) {
            return "hours";
        }
        if (path.endsWith("_days")) {
            return "days";
        }
        if (path.endsWith("_bytes")) {
            return "bytes";
        }
        if (path.endsWith("_level")) {
            return "level";
        }
        if (path.contains("per_minute")) {
            return "actions per minute";
        }
        return switch (setting.type()) {
            case BOOLEAN -> "boolean flag";
            case INTEGER -> "count";
            case DECIMAL -> "decimal value";
            case STRING -> "text";
            case ENUM -> "named option";
        };
    }

    public String defaultFile(ModuleDefinition module) {
        StringBuilder output = new StringBuilder();
        output.append("# ").append(module.purpose()).append("\n");
        output.append("# Generated schema version ").append(SCHEMA_VERSION).append(".\n");
        output.append("# This file cannot grant permissions or execute commands.\n");
        output.append("schema_version = ").append(SCHEMA_VERSION).append("\n");
        output.append("module_id = \"").append(module.id()).append("\"\n");
        output.append("documentation_version = ").append(module.documentationVersion()).append("\n");
        String section = "";
        for (SettingDefinition setting : module.settings()) {
            String nextSection = setting.section();
            if (!nextSection.equals(section)) {
                section = nextSection;
                output.append("\n[").append(section).append("]\n");
            }
            output.append("# ").append(setting.description()).append(" ")
                    .append(setting.boundsDescription()).append(" ")
                    .append("Apply class ").append(setting.applyClass().id()).append(".\n");
            output.append(setting.key()).append(" = ").append(setting.tomlDefault()).append("\n");
        }
        return output.toString();
    }

    public String defaultIndex() {
        StringBuilder output = new StringBuilder();
        output.append("# SEF modular configuration index.\n");
        output.append("schema_version = ").append(SCHEMA_VERSION).append("\n");
        output.append("last_successful_migration_revision = 0\n");
        output.append("recognized_optional_files = [\"gui_overrides.toml\"]\n");
        output.append("load_order = [");
        for (int index = 0; index < MODULE_IDS.size(); index++) {
            if (index > 0) {
                output.append(", ");
            }
            output.append('"').append(MODULE_IDS.get(index)).append('"');
        }
        output.append("]\n");
        output.append("enabled_modules = [");
        for (int index = 0; index < MODULE_IDS.size(); index++) {
            if (index > 0) {
                output.append(", ");
            }
            output.append('"').append(MODULE_IDS.get(index)).append('"');
        }
        output.append("]\n");
        return output.toString();
    }

    private ModuleDefinition definition(String moduleId) {
        List<SettingDefinition> settings = new ArrayList<>();
        settings.add(bool("module.enabled", true, "Enables the module after dependency validation.", ApplyClass.LIVE));
        settings.add(enumeration(
                "module.disabled_behavior",
                "command_unavailable",
                Set.of("command_unavailable", "read_only", "provider_fallback"),
                "Defines truthful behavior while this module is disabled.",
                ApplyClass.LIVE));
        settings.add(enumeration(
                "gui.mode",
                moduleId.equals("gui") ? "auto" : "inherit",
                moduleId.equals("gui")
                        ? Set.of("off", "on", "auto")
                        : Set.of("inherit", "off", "on", "command_only", "gui_preferred"),
                "Selects enhanced GUI presentation without changing command access.",
                ApplyClass.LIVE));
        settings.add(bool(
                "gui.bare_command_opens",
                !Set.of("core", "permissions", "audit", "integrations").contains(moduleId),
                "Allows eligible bare command routes to prefer a dedicated workflow.",
                ApplyClass.LIVE));
        settings.add(string(
                "gui.default_page",
                "overview",
                1,
                64,
                "[a-z0-9_]+",
                "Selects the stable default workflow page.",
                ApplyClass.LIVE,
                Sensitivity.PUBLIC));
        settings.add(integer(
                "limits.maximum_records",
                defaultRecordLimit(moduleId),
                1,
                hardRecordLimit(moduleId),
                "Bounds persisted records owned by this module.",
                ApplyClass.LIVE));
        settings.add(integer(
                "limits.maximum_page_size",
                45,
                4,
                100,
                "Bounds command and GUI result pages.",
                ApplyClass.LIVE));
        settings.add(integer(
                "rate.maximum_actions_per_minute",
                defaultActionRate(moduleId),
                1,
                10_000,
                "Bounds admission attempts independently of permission cooldown duration.",
                ApplyClass.LIVE));
        settings.add(integer(
                "storage.retention_days",
                defaultRetention(moduleId),
                0,
                3650,
                "Defines bounded data retention where the module owns persistent records.",
                ApplyClass.LIVE));
        settings.add(enumeration(
                "failure.mode",
                dangerous(moduleId) ? "fail_closed" : "previous_known_good",
                Set.of("fail_closed", "previous_known_good", "read_only"),
                "Defines provider and validation failure behavior.",
                ApplyClass.LIVE));
        settings.add(enumeration(
                "audit.class",
                dangerous(moduleId) ? "admin_action" : "metadata_only",
                Set.of("none", "metadata_only", "admin_action", "destructive"),
                "Defines the minimum audit class for module actions.",
                ApplyClass.LIVE));
        settings.add(enumeration(
                "audit.redaction",
                sensitive(moduleId) ? "sensitive" : "standard",
                Set.of("standard", "sensitive", "security_critical"),
                "Defines the module redaction class.",
                ApplyClass.LIVE));
        addSpecialized(moduleId, settings);
        addRuntimeBindings(moduleId, settings);
        return new ModuleDefinition(
                moduleId,
                moduleId + ".toml",
                purpose(moduleId),
                documentationVersion(moduleId),
                defaultApplyClass(moduleId),
                dependencies(moduleId),
                conflicts(moduleId),
                List.copyOf(settings));
    }

    private static int documentationVersion(String moduleId) {
        return moduleId.equals("sudo") ? 3 : 2;
    }

    private static void addRuntimeBindings(String moduleId, List<SettingDefinition> settings) {
        for (RuntimeConfigBindings.Binding binding : ConfigHandler.runtimeBindings()) {
            if (!binding.moduleId().equals(moduleId)) {
                continue;
            }
            RuntimeConfigValue<?> value = binding.value();
            String description = value.description().isBlank()
                    ? "Owns the runtime setting " + binding.fieldName() + "."
                    : value.description();
            double minimum = value.minimum() == null
                    ? value.kind() == RuntimeConfigValue.Kind.STRING ? 0.0D : -Double.MAX_VALUE
                    : value.minimum().doubleValue();
            double maximum = value.maximum() == null
                    ? value.kind() == RuntimeConfigValue.Kind.STRING ? 8192.0D : Double.MAX_VALUE
                    : value.maximum().doubleValue();
            ValueType type = switch (value.kind()) {
                case BOOLEAN -> ValueType.BOOLEAN;
                case INTEGER -> ValueType.INTEGER;
                case DECIMAL -> ValueType.DECIMAL;
                case STRING -> ValueType.STRING;
            };
            settings.add(new SettingDefinition(
                    binding.settingPath(),
                    type,
                    value.defaultValueString(),
                    minimum,
                    maximum,
                    Set.of(),
                    "",
                    description,
                    binding.applyClass(),
                    binding.sensitivity()));
        }
    }

    private static void addSpecialized(String moduleId, List<SettingDefinition> settings) {
        switch (moduleId) {
            case "gui" -> {
                settings.add(integer(
                        "sessions.maximum_open_per_player",
                        1,
                        1,
                        4,
                        "Bounds privileged enhanced workflow sessions.",
                        ApplyClass.LIVE));
                settings.add(integer(
                        "sessions.timeout_seconds",
                        120,
                        10,
                        1800,
                        "Expires inactive enhanced workflow sessions.",
                        ApplyClass.LIVE));
                settings.add(integer(
                        "sessions.maximum_draft_bytes",
                        262_144,
                        4096,
                        1_048_576,
                        "Bounds server held typed draft state per player.",
                        ApplyClass.LIVE));
                settings.add(bool(
                        "reminder.enabled",
                        true,
                        "Enables the optional client enhancement reminder.",
                        ApplyClass.LIVE));
                settings.add(integer(
                        "reminder.minimum_interval_hours",
                        24,
                        1,
                        8760,
                        "Bounds repeated enhancement reminders.",
                        ApplyClass.LIVE));
            }
            case "super_enchanting" -> {
                settings.add(integer(
                        "safety.minimum_level",
                        1,
                        1,
                        1_000_000,
                        "Defines the smallest nonzero level selected by the virtual workstation.",
                        ApplyClass.LIVE));
                settings.add(integer(
                        "safety.maximum_level",
                        1000,
                        1,
                        1_000_000,
                        "Defines the administrative enchantment hard ceiling.",
                        ApplyClass.LIVE));
                settings.add(bool(
                        "safety.allow_unsafe_levels",
                        true,
                        "Allows separately permitted levels above the enchantment vanilla maximum.",
                        ApplyClass.LIVE));
                settings.add(bool(
                        "safety.allow_arbitrary_items",
                        true,
                        "Allows separately permitted arbitrary item enchanting.",
                        ApplyClass.LIVE));
                settings.add(bool(
                        "safety.allow_incompatible",
                        true,
                        "Allows separately permitted incompatible combinations.",
                        ApplyClass.LIVE));
                settings.add(bool(
                        "shortcuts.enable_set",
                        true,
                        "Requests the collision aware set shortcut.",
                        ApplyClass.RESTART_REQUIRED));
                settings.add(integer(
                        "confirmation.seconds",
                        60,
                        5,
                        600,
                        "Defines the lifetime of a destructive enchantment confirmation token.",
                        ApplyClass.LIVE));
            }
            case "sudo" -> {
                settings.add(bool(
                        "delegation.enabled",
                        false,
                        "Enables separately authorized one execution delegated sudo grants.",
                        ApplyClass.LIVE));
                settings.add(bool(
                        "delegation.compatibility_boolean_syntax",
                        true,
                        "Allows the compatibility boolean delegated mode syntax.",
                        ApplyClass.LIVE));
                settings.add(bool(
                        "delegation.require_target_consent",
                        false,
                        "Requires target consent specifically for delegated sudo.",
                        ApplyClass.LIVE));
                settings.add(bool(
                        "delegation.allow_self_delegation",
                        false,
                        "Allows separately permitted delegated execution against the issuer.",
                        ApplyClass.LIVE));
                settings.add(integer(
                        "delegation.maximum_temporary_vanilla_permission_level",
                        2,
                        0,
                        2,
                        "Bounds temporary vanilla command requirements for one dispatch.",
                        ApplyClass.LIVE));
                settings.add(integer(
                        "delegation.grant_lifetime_seconds",
                        15,
                        1,
                        60,
                        "Bounds an admitted grant lifetime before dispatch.",
                        ApplyClass.LIVE));
                settings.add(bool(
                        "delegation.confirmation_required",
                        true,
                        "Requires exact single use confirmation before grant publication.",
                        ApplyClass.LIVE));
                settings.add(bool(
                        "delegation.notify_target",
                        true,
                        "Notifies the effective target after delegated execution.",
                        ApplyClass.LIVE));
                settings.add(bool(
                        "delegation.allow_unknown_external_permission_checks",
                        false,
                        "Allows unknown provider checks that cannot prove scoped authority.",
                        ApplyClass.LIVE));
                settings.add(bool(
                        "delegation.allow_redirects",
                        false,
                        "Allows only statically admitted command redirects.",
                        ApplyClass.LIVE));
                settings.add(bool(
                        "delegation.allow_forks",
                        false,
                        "Allows only bounded and previewed command forks.",
                        ApplyClass.LIVE));
                settings.add(bool(
                        "delegation.allow_async",
                        false,
                        "Allows profiles that retain execution for asynchronous work.",
                        ApplyClass.LIVE));
                settings.add(string(
                        "delegation.allowed_roots",
                        "effect",
                        0,
                        4096,
                        "",
                        "Lists roots eligible for a published delegation profile.",
                        ApplyClass.LIVE,
                        Sensitivity.PUBLIC));
                settings.add(string(
                        "delegation.denied_roots",
                        "op,deop,stop,reload,sudo,run,silent,execute,function,schedule",
                        0,
                        4096,
                        "",
                        "Adds delegated roots denied beyond the code hard deny set.",
                        ApplyClass.LIVE,
                        Sensitivity.PUBLIC));
            }
            case "backups" -> {
                settings.add(bool(
                        "verification.required",
                        true,
                        "Requires a verified checkpoint before a backup reports success.",
                        ApplyClass.LIVE));
                settings.add(integer(
                        "verification.maximum_minutes",
                        30,
                        1,
                        240,
                        "Bounds provider checkpoint verification.",
                        ApplyClass.LIVE));
            }
            case "privacy" -> {
                settings.add(integer(
                        "export.maximum_bytes",
                        8_388_608,
                        1024,
                        67_108_864,
                        "Bounds one privacy export.",
                        ApplyClass.LIVE));
                settings.add(integer(
                        "requests.maximum_open_per_player",
                        3,
                        1,
                        20,
                        "Bounds simultaneous privacy requests.",
                        ApplyClass.LIVE));
            }
            case "performance" -> {
                settings.add(integer(
                        "sampling.maximum_milliseconds_per_minute",
                        1000,
                        10,
                        10_000,
                        "Bounds active profiler overhead.",
                        ApplyClass.LIVE));
                settings.add(integer(
                        "sampling.maximum_chunks",
                        4096,
                        16,
                        100_000,
                        "Bounds one diagnostic sampling scope.",
                        ApplyClass.LIVE));
            }
            case "messages" -> settings.add(string(
                    "format.prefix",
                    "&8[&6SEF&8]&r ",
                    0,
                    128,
                    "",
                    "Defines the bounded default message prefix.",
                    ApplyClass.LIVE,
                    Sensitivity.PUBLIC));
            case "integrations" -> settings.add(integer(
                    "providers.timeout_milliseconds",
                    3000,
                    100,
                    30_000,
                    "Bounds optional provider calls.",
                    ApplyClass.LIVE));
            default -> {
            }
        }
    }

    private void validateGraph() {
        for (ModuleDefinition definition : definitions.values()) {
            for (String dependency : definition.dependencies()) {
                if (!definitions.containsKey(dependency)) {
                    throw new IllegalStateException("unknown configuration module dependency");
                }
            }
            for (String conflict : definition.conflicts()) {
                if (!definitions.containsKey(conflict)) {
                    throw new IllegalStateException("unknown configuration module conflict");
                }
            }
            expand(definition, new LinkedHashSet<>(), new LinkedHashSet<>());
        }
    }

    private void expand(
            ModuleDefinition definition,
            LinkedHashSet<String> result,
            LinkedHashSet<String> visiting
    ) {
        if (!visiting.add(definition.id())) {
            throw new IllegalStateException("configuration module dependency cycle");
        }
        for (String dependency : definition.dependencies()) {
            ModuleDefinition dependencyDefinition = require(dependency);
            if (!result.contains(dependencyDefinition.id())) {
                expand(dependencyDefinition, result, visiting);
            }
        }
        visiting.remove(definition.id());
        result.add(definition.id());
    }

    private static Set<String> dependencies(String moduleId) {
        if (moduleId.equals("core")) {
            return Set.of();
        }
        LinkedHashSet<String> dependencies = new LinkedHashSet<>();
        dependencies.add("core");
        if (!Set.of("commands", "messages", "permissions", "gui", "hud", "audit", "integrations").contains(moduleId)) {
            dependencies.add("commands");
            dependencies.add("permissions");
            dependencies.add("messages");
        }
        if (Set.of(
                "craft", "anvil", "enchanting", "super_enchanting", "repair", "workstations",
                "homes", "teleport_requests", "spawn", "back", "warps", "player_warps",
                "random_teleport", "direct_teleport", "private_messages", "mail", "nicknames",
                "moderation", "bans", "kicks", "mutes", "warnings", "jails", "freeze",
                "inventory_lock", "building_control", "vanish", "command_spy", "inventory",
                "kits", "player_utilities", "gamemode", "items", "economy", "economy_signs",
                "admin_panels", "fancy_tags", "disguise", "server_control", "community",
                "displays").contains(moduleId)) {
            dependencies.add("gui");
        }
        if (Set.of(
                "moderation", "bans", "kicks", "mutes", "warnings", "jails", "freeze",
                "inventory_lock", "building_control", "vanish", "command_spy", "logger",
                "admin_panels", "aliases", "bundles", "fake_actions", "sudo", "run_and_silent",
                "fancy_tags", "disguise", "server_control", "performance", "backups",
                "privacy", "community", "displays").contains(moduleId)) {
            dependencies.add("audit");
        }
        if (moduleId.equals("super_enchanting")) {
            dependencies.add("enchanting");
        }
        if (moduleId.equals("economy_signs")) {
            dependencies.add("economy");
        }
        return Set.copyOf(dependencies);
    }

    private static Set<String> conflicts(String moduleId) {
        return Set.of();
    }

    private static int defaultRecordLimit(String moduleId) {
        return sensitive(moduleId) || dangerous(moduleId) ? 1000 : 10_000;
    }

    private static int hardRecordLimit(String moduleId) {
        return sensitive(moduleId) || dangerous(moduleId) ? 100_000 : 1_000_000;
    }

    private static int defaultActionRate(String moduleId) {
        return dangerous(moduleId) ? 30 : 300;
    }

    private static int defaultRetention(String moduleId) {
        return sensitive(moduleId) ? 90 : 30;
    }

    private static boolean sensitive(String moduleId) {
        return Set.of(
                "moderation", "bans", "kicks", "mutes", "warnings", "jails", "freeze",
                "command_spy", "logger", "audit", "sudo", "run_and_silent", "server_control",
                "backups", "privacy").contains(moduleId);
    }

    private static boolean dangerous(String moduleId) {
        return Set.of(
                "bans", "kicks", "jails", "freeze", "inventory_lock", "building_control",
                "admin_panels", "aliases", "bundles", "fake_actions", "sudo", "run_and_silent",
                "disguise", "server_control", "backups").contains(moduleId);
    }

    private static ApplyClass defaultApplyClass(String moduleId) {
        return Set.of("core", "integrations").contains(moduleId)
                ? ApplyClass.RESTART_REQUIRED
                : ApplyClass.LIVE;
    }

    private static String purpose(String moduleId) {
        return "Controls " + moduleId.replace('_', ' ') + " behavior, presentation, bounds, storage, and diagnostics.";
    }

    private static SettingDefinition bool(
            String path,
            boolean defaultValue,
            String description,
            ApplyClass applyClass
    ) {
        return new SettingDefinition(
                path,
                ValueType.BOOLEAN,
                Boolean.toString(defaultValue),
                0,
                0,
                Set.of(),
                "",
                description,
                applyClass,
                Sensitivity.PUBLIC);
    }

    private static SettingDefinition integer(
            String path,
            long defaultValue,
            long minimum,
            long maximum,
            String description,
            ApplyClass applyClass
    ) {
        return new SettingDefinition(
                path,
                ValueType.INTEGER,
                Long.toString(defaultValue),
                minimum,
                maximum,
                Set.of(),
                "",
                description,
                applyClass,
                Sensitivity.PUBLIC);
    }

    private static SettingDefinition string(
            String path,
            String defaultValue,
            long minimum,
            long maximum,
            String pattern,
            String description,
            ApplyClass applyClass,
            Sensitivity sensitivity
    ) {
        return new SettingDefinition(
                path,
                ValueType.STRING,
                defaultValue,
                minimum,
                maximum,
                Set.of(),
                pattern,
                description,
                applyClass,
                sensitivity);
    }

    private static SettingDefinition enumeration(
            String path,
            String defaultValue,
            Set<String> values,
            String description,
            ApplyClass applyClass
    ) {
        return new SettingDefinition(
                path,
                ValueType.ENUM,
                defaultValue,
                0,
                0,
                values,
                "",
                description,
                applyClass,
                Sensitivity.PUBLIC);
    }

    private static String normalize(String value) {
        String normalized = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9][a-z0-9_]{0,63}")) {
            throw new IllegalArgumentException("configuration module id is invalid");
        }
        return normalized;
    }

    public record ModuleDefinition(
            String id,
            String fileName,
            String purpose,
            int documentationVersion,
            ApplyClass applyClass,
            Set<String> dependencies,
            Set<String> conflicts,
            List<SettingDefinition> settings
    ) {
        public ModuleDefinition {
            id = normalize(id);
            fileName = Objects.requireNonNull(fileName, "fileName");
            purpose = Objects.requireNonNull(purpose, "purpose").strip();
            dependencies = Set.copyOf(dependencies);
            conflicts = Set.copyOf(conflicts);
            settings = List.copyOf(settings);
            if (!fileName.equals(id + ".toml") || documentationVersion < 1 || settings.isEmpty()) {
                throw new IllegalArgumentException("configuration module definition is invalid");
            }
            Set<String> paths = new LinkedHashSet<>();
            for (SettingDefinition setting : settings) {
                if (!paths.add(setting.path())) {
                    throw new IllegalArgumentException("duplicate module setting");
                }
            }
        }

        public Map<String, SettingDefinition> settingsByPath() {
            Map<String, SettingDefinition> result = new LinkedHashMap<>();
            for (SettingDefinition setting : settings) {
                result.put(setting.path(), setting);
            }
            return Map.copyOf(result);
        }
    }

    public record SettingDefinition(
            String path,
            ValueType type,
            String defaultValue,
            double minimum,
            double maximum,
            Set<String> enumValues,
            String pattern,
            String description,
            ApplyClass applyClass,
            Sensitivity sensitivity
    ) {
        public SettingDefinition {
            path = Objects.requireNonNull(path, "path").trim().toLowerCase(Locale.ROOT);
            type = Objects.requireNonNull(type, "type");
            defaultValue = Objects.requireNonNull(defaultValue, "defaultValue");
            enumValues = enumValues.stream()
                    .map(value -> value.toLowerCase(Locale.ROOT))
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
            pattern = Objects.requireNonNullElse(pattern, "");
            description = Objects.requireNonNull(description, "description").strip();
            applyClass = Objects.requireNonNull(applyClass, "applyClass");
            sensitivity = Objects.requireNonNull(sensitivity, "sensitivity");
            if (!path.matches("[a-z0-9_]+\\.[a-z0-9_]+")
                    || ((type == ValueType.INTEGER || type == ValueType.DECIMAL)
                    && (!Double.isFinite(minimum) || !Double.isFinite(maximum) || minimum > maximum))
                    || (type == ValueType.ENUM && !enumValues.contains(defaultValue))) {
                throw new IllegalArgumentException("configuration setting definition is invalid");
            }
        }

        public String section() {
            return path.substring(0, path.indexOf('.'));
        }

        public String key() {
            return path.substring(path.indexOf('.') + 1);
        }

        public String validate(String rawValue) {
            String value = Objects.requireNonNull(rawValue, "rawValue");
            boolean containsInvalidControl = value.codePoints().anyMatch(codePoint ->
                    Character.isISOControl(codePoint)
                            && !(type == ValueType.STRING
                            && (codePoint == '\n' || codePoint == '\r' || codePoint == '\t')));
            if (value.length() > 8192 || containsInvalidControl) {
                throw new IllegalArgumentException(path + " contains invalid characters");
            }
            return switch (type) {
                case BOOLEAN -> {
                    String normalized = value.toLowerCase(Locale.ROOT);
                    if (!normalized.equals("true") && !normalized.equals("false")) {
                        throw new IllegalArgumentException(path + " must be true or false");
                    }
                    yield normalized;
                }
                case INTEGER -> {
                    if (!value.matches("-?(0|[1-9][0-9]*)")) {
                        throw new IllegalArgumentException(path + " must be a base ten whole number");
                    }
                    long parsed;
                    try {
                        parsed = Long.parseLong(value);
                    } catch (NumberFormatException exception) {
                        throw new IllegalArgumentException(path + " is outside bounds");
                    }
                    if (parsed < minimum || parsed > maximum) {
                        throw new IllegalArgumentException(path + " is outside bounds");
                    }
                    yield Long.toString(parsed);
                }
                case DECIMAL -> {
                    double parsed;
                    try {
                        parsed = Double.parseDouble(value);
                    } catch (NumberFormatException exception) {
                        throw new IllegalArgumentException(path + " must be a decimal number");
                    }
                    if (!Double.isFinite(parsed) || parsed < minimum || parsed > maximum) {
                        throw new IllegalArgumentException(path + " is outside bounds");
                    }
                    yield Double.toString(parsed);
                }
                case STRING -> {
                    int length = value.codePointCount(0, value.length());
                    if (length < minimum || length > maximum
                            || (!pattern.isBlank() && !value.matches(pattern))) {
                        throw new IllegalArgumentException(path + " is outside bounds");
                    }
                    yield value;
                }
                case ENUM -> {
                    String normalized = value.toLowerCase(Locale.ROOT);
                    if (!enumValues.contains(normalized)) {
                        throw new IllegalArgumentException(path + " is not an allowed value");
                    }
                    yield normalized;
                }
            };
        }

        public String boundsDescription() {
            return switch (type) {
                case BOOLEAN -> "`true` or `false`";
                case INTEGER -> (long) minimum + " through " + (long) maximum;
                case DECIMAL -> minimum + " through " + maximum;
                case STRING -> (long) minimum + " through " + (long) maximum + " characters"
                        + (pattern.isBlank() ? "" : ", pattern `" + pattern + "`");
                case ENUM -> "`" + String.join("`, `", enumValues.stream().sorted().toList()) + "`";
            };
        }

        public String tomlDefault() {
            return type == ValueType.BOOLEAN || type == ValueType.INTEGER || type == ValueType.DECIMAL
                    ? defaultValue
                    : "\"" + defaultValue
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t") + "\"";
        }
    }

    public enum ValueType {
        BOOLEAN("boolean"),
        INTEGER("integer"),
        DECIMAL("decimal"),
        STRING("string"),
        ENUM("enum");

        private final String id;

        ValueType(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }

    public enum ApplyClass {
        LIVE("live"),
        NEXT_SESSION("next_session"),
        NEXT_WORLD_LOAD("next_world_load"),
        RESTART_REQUIRED("restart_required");

        private final String id;

        ApplyClass(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }

    public enum Sensitivity {
        PUBLIC("public"),
        SENSITIVE("sensitive"),
        SECRET("secret");

        private final String id;

        Sensitivity(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }
}
