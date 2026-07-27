package com.enviouse.sef.docs;

import com.enviouse.sef.config.modules.ModuleConfigRegistry;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.kernel.command.PanelContracts;
import com.enviouse.sef.permissions.PermissionManifest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ProjectDocumentationGenerator {
    private static final Gson GSON = new Gson();

    private ProjectDocumentationGenerator() {
    }

    public static void writeAll(Path repositoryRoot) throws Exception {
        ModuleConfigRegistry configurations = new ModuleConfigRegistry();
        Files.writeString(
                repositoryRoot.resolve("docs/CONFIGURATION_REFERENCE.md"),
                configurations.generatedReference(),
                StandardCharsets.UTF_8);
        Files.writeString(
                repositoryRoot.resolve("docs/COMMAND_REFERENCE.md"),
                commandReference(repositoryRoot),
                StandardCharsets.UTF_8);
        Files.writeString(
                repositoryRoot.resolve("docs/PERMISSION_REFERENCE.md"),
                permissionReference(),
                StandardCharsets.UTF_8);
        Path fixture = repositoryRoot.resolve("src/test/resources/fixtures/config/sef/modules");
        Files.createDirectories(fixture);
        Files.writeString(
                fixture.resolve("index.toml"),
                configurations.defaultIndex(),
                StandardCharsets.UTF_8);
        for (ModuleConfigRegistry.ModuleDefinition module : configurations.definitions()) {
            Files.writeString(
                    fixture.resolve(module.fileName()),
                    configurations.defaultFile(module),
                    StandardCharsets.UTF_8);
        }
    }

    public static String commandReference(Path repositoryRoot) throws Exception {
        KernelServices.initialize();
        Map<String, String> language = language(repositoryRoot);
        StringBuilder output = new StringBuilder();
        List<CommandDefinition> commands = KernelServices.catalog().entries();

        output.append("# Command Reference\n\n");
        output.append("This file is generated from the sealed command, shortcut, and GUI descriptor registries. ");
        output.append("Change registry metadata and run `./gradlew generateProjectReferences` instead of editing this file.\n\n");
        output.append("Catalog entries: ").append(commands.size()).append(". ");
        output.append("Shortcut entries: ").append(KernelServices.shortcuts().size()).append(". ");
        output.append("GUI descriptors: ").append(KernelServices.descriptors().size()).append(".\n\n");

        output.append("## Commands\n\n");
        for (CommandDefinition command : commands) {
            output.append("### `").append(command.id()).append("`\n\n");
            field(output, "Description", localized(
                    language,
                    command.descriptionKey(),
                    "Executes the " + code("/" + command.canonicalRoute()) + " action through the shared policy pipeline"));
            field(output, "Usage", localized(
                    language,
                    command.usageKey(),
                    code("/" + command.canonicalRoute())));
            field(output, "Canonical route", code(command.canonicalRoute()));
            field(output, "Example", code("/" + command.canonicalRoute()));
            field(output, "Convenience roots", codes(command.convenienceRoots()));
            field(output, "Category", code(command.helpCategory()));
            field(output, "Feature gate", code(command.featureId()));
            field(output, "Permissions", codes(command.permissionIds()));
            field(output, "Access class", code(command.accessClass().name().toLowerCase()));
            field(output, "Sources", codes(command.sourceTypes().stream()
                    .map(value -> value.name().toLowerCase())
                    .collect(Collectors.toSet())));
            field(output, "Target behavior", code(command.targetBehavior().name().toLowerCase()));
            field(output, "Cooldown policy", code(command.cooldownId()));
            field(output, "Confirmation", command.confirmationRequired() ? "required" : "not required");
            field(output, "Audit class", code(command.auditClass().name().toLowerCase()));
            field(output, "GUI descriptor", optionalCode(command.guiDescriptorId(), "not applicable"));
            field(output, "HUD contract", command.hudDescriptorId().isBlank()
                    ? command.hudNotApplicableReason()
                    : code(command.hudDescriptorId()));
            field(output, "Quota contract", command.quotaId().isBlank()
                    ? command.quotaNotApplicableReason()
                    : code(command.quotaId()));
            field(output, "Conflict policy", code(command.conflictPolicy().name().toLowerCase()));
            field(output, "Player facing", Boolean.toString(command.playerFacing()));
            field(output, "Shared pipeline", command.pipelineEnforced() ? "required" : "not enforced");
            output.append('\n');
        }

        output.append("## Shortcuts\n\n");
        output.append("| Root | Action | Adapter | Additional permission | Collision policy | Structural revision |\n");
        output.append("| --- | --- | --- | --- | --- | ---: |\n");
        KernelServices.shortcuts().entries().forEach(shortcut -> output
                .append("| ").append(code(shortcut.root()))
                .append(" | ").append(code(shortcut.actionId()))
                .append(" | ").append(code(shortcut.adapter().name().toLowerCase()))
                .append(" | ").append(optionalCode(shortcut.additionalPermissionId(), "none"))
                .append(" | ").append(code(shortcut.collisionMode().name().toLowerCase()))
                .append(" | ").append(shortcut.structuralRevision())
                .append(" |\n"));
        output.append('\n');

        output.append("## GUI descriptors\n\n");
        KernelServices.descriptors().panels().values().stream()
                .sorted(Comparator.comparing(PanelContracts.PanelDescriptor::id))
                .forEach(panel -> {
                    output.append("### `").append(panel.id()).append("`\n\n");
                    field(output, "Title", localized(
                            language,
                            panel.titleKey(),
                            humanize(panel.id())));
                    field(output, "Permission", code(panel.permissionId()));
                    field(output, "Fallback route", code(panel.fallback().route()));
                    field(output, "Fallback usage", localized(
                            language,
                            panel.fallback().usageKey(),
                            code("/" + panel.fallback().route())));
                    output.append("| Control | Action | Permission | Icon | Target policy | Execution context | Audience | Maximum targets |\n");
                    output.append("| --- | --- | --- | --- | --- | --- | --- | ---: |\n");
                    panel.controls().stream()
                            .sorted(Comparator.comparing(PanelContracts.ControlDescriptor::id))
                            .forEach(control -> output
                                    .append("| ").append(code(control.id()))
                                    .append(" | ").append(code(control.actionId()))
                                    .append(" | ").append(code(control.permissionId()))
                                    .append(" | ").append(code(control.iconId()))
                                    .append(" | ").append(code(control.targetPolicy().name().toLowerCase()))
                                    .append(" | ").append(code(control.executionContext().name().toLowerCase()))
                                    .append(" | ").append(code(control.audience().kind().name().toLowerCase()))
                                    .append(" | ").append(control.audience().maximumTargets())
                                    .append(" |\n"));
                    output.append('\n');
                });

        output.append("## Command only GUI descriptors\n\n");
        KernelServices.descriptors().commandOnlyDescriptors().stream()
                .sorted()
                .forEach(descriptor -> output.append("* ").append(code(descriptor)).append('\n'));
        return output.toString().stripTrailing() + "\n";
    }

    public static String permissionReference() {
        KernelServices.initialize();
        List<PermissionManifest.Definition> permissions = PermissionManifest.definitions().stream()
                .sorted(Comparator.comparing(PermissionManifest.Definition::id))
                .toList();
        Map<String, List<String>> commandUsage = KernelServices.catalog().entries().stream()
                .flatMap(command -> command.permissionIds().stream()
                        .map(permission -> Map.entry(permission, command.id())))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        StringBuilder output = new StringBuilder();
        output.append("# Permission Reference\n\n");
        output.append("This file is generated from the permission manifest and sealed command registry. ");
        output.append("Change permission definitions and run `./gradlew generateProjectReferences` instead of editing this file.\n\n");
        output.append("Permission entries: ").append(permissions.size()).append(". ");
        output.append("Defaults are fail closed unless a row explicitly states `true`.\n\n");
        output.append("| Permission | Default | Name | Description | Catalog commands |\n");
        output.append("| --- | --- | --- | --- | --- |\n");
        for (PermissionManifest.Definition permission : permissions) {
            output.append("| ").append(code(permission.id()))
                    .append(" | ").append(code(Boolean.toString(permission.defaultValue())))
                    .append(" | ").append(table(permission.name()))
                    .append(" | ").append(table(permission.description()))
                    .append(" | ").append(codes(commandUsage.getOrDefault(permission.id(), List.of())))
                    .append(" |\n");
        }
        return output.toString().stripTrailing() + "\n";
    }

    private static Map<String, String> language(Path repositoryRoot) throws Exception {
        Path path = repositoryRoot.resolve("src/main/resources/assets/sef/lang/en_us.json");
        String json = Files.readString(path, StandardCharsets.UTF_8);
        return GSON.fromJson(json, new TypeToken<Map<String, String>>() {
        }.getType());
    }

    private static String localized(Map<String, String> language, String key, String fallback) {
        return language.containsKey(key) ? table(language.get(key)) : fallback;
    }

    private static String humanize(String value) {
        String normalized = value.substring(value.indexOf(':') + 1).replace('_', ' ').replace('.', ' ');
        if (normalized.isBlank()) {
            return value;
        }
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private static void field(StringBuilder output, String name, String value) {
        output.append("* ").append(name).append(": ").append(value).append(".\n");
    }

    private static String optionalCode(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : code(value);
    }

    private static String codes(Iterable<String> values) {
        String joined = java.util.stream.StreamSupport.stream(values.spliterator(), false)
                .sorted()
                .map(ProjectDocumentationGenerator::code)
                .collect(Collectors.joining(", "));
        return joined.isBlank() ? "none" : joined;
    }

    private static String code(String value) {
        return "`" + value.replace("`", "") + "`";
    }

    private static String table(String value) {
        return value.replace("|", "\\|").replace("\n", " ").trim();
    }
}
