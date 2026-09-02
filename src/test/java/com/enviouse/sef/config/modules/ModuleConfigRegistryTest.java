package com.enviouse.sef.config.modules;

import com.enviouse.sef.config.ConfigHandler;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleConfigRegistryTest {
    @Test
    void registryOwnsEveryPlannedModuleAndContainsNoCooldownSetting() {
        ModuleConfigRegistry registry = new ModuleConfigRegistry();

        assertEquals(62, registry.definitions().size());
        assertTrue(registry.contains("craft"));
        assertTrue(registry.contains("super_enchanting"));
        assertTrue(registry.contains("server_control"));
        assertTrue(registry.contains("privacy"));
        assertTrue(registry.contains("displays"));
        assertTrue(registry.definitions().stream()
                .flatMap(module -> module.settings().stream())
                .noneMatch(setting -> setting.path().matches("runtime\\..*_cooldown_seconds")));
    }

    @Test
    void bootstrapSpecContainsOnlyModularPlatformControls() {
        assertEquals(
                Set.of(
                        "modular_configuration_enabled",
                        "compatibility_mode",
                        "maximum_module_file_kib"),
                ConfigHandler.spec.getValues().valueMap().keySet());
    }

    @Test
    void everyNonSecretRuntimeValueHasExactlyOneModuleOwner() {
        ModuleConfigRegistry registry = new ModuleConfigRegistry();
        var bindings = ConfigHandler.runtimeBindings();

        assertEquals(370, bindings.size());
        assertEquals(
                bindings.size(),
                bindings.stream()
                        .map(binding -> binding.moduleId() + "." + binding.settingPath())
                        .collect(Collectors.toSet())
                        .size());
        assertTrue(bindings.stream().noneMatch(binding -> binding.fieldName().equals("discordBotToken")));
        assertTrue(bindings.stream().allMatch(binding ->
                registry.require(binding.moduleId()).settingsByPath().containsKey(binding.settingPath())));
    }

    @Test
    void dependencyClosureIsOrderedAndCycleFree() {
        ModuleConfigRegistry registry = new ModuleConfigRegistry();

        var closure = registry.dependencyClosure(Set.of("super_enchanting"));

        assertTrue(closure.indexOf("core") < closure.indexOf("commands"));
        assertTrue(closure.indexOf("commands") < closure.indexOf("enchanting"));
        assertTrue(closure.indexOf("enchanting") < closure.indexOf("super_enchanting"));
        assertEquals(closure.size(), Set.copyOf(closure).size());
    }

    @Test
    void typedSettingsRejectCoercionAndOverflow() {
        ModuleConfigRegistry registry = new ModuleConfigRegistry();
        var limit = registry.require("super_enchanting")
                .settingsByPath()
                .get("safety.maximum_level");
        var mode = registry.require("gui").settingsByPath().get("gui.mode");

        assertEquals("1000000", limit.validate("1000000"));
        assertThrows(IllegalArgumentException.class, () -> limit.validate("+1"));
        assertThrows(IllegalArgumentException.class, () -> limit.validate("1.0"));
        assertThrows(IllegalArgumentException.class, () -> limit.validate("1000001"));
        assertEquals("auto", mode.validate("AUTO"));
        assertThrows(IllegalArgumentException.class, () -> mode.validate("enabled"));
    }

    @Test
    void decimalAndNegativeIntegerSettingsRemainTyped() {
        ModuleConfigRegistry registry = new ModuleConfigRegistry();
        var speed = registry.require("player_utilities")
                .settingsByPath()
                .get("runtime.maximum_fly_speed");
        var minimumBalance = registry.require("economy")
                .settingsByPath()
                .get("runtime.economy_minimum_balance");

        assertEquals(ModuleConfigRegistry.ValueType.DECIMAL, speed.type());
        assertEquals("1.5", speed.validate("1.5"));
        assertThrows(IllegalArgumentException.class, () -> speed.validate("10.1"));
        assertEquals("-500", minimumBalance.validate("-500"));
        assertThrows(IllegalArgumentException.class, () -> minimumBalance.validate("-9000000000000001"));
    }

    @Test
    void generatedFilesContainIdentitySchemaAndTypedComments() {
        ModuleConfigRegistry registry = new ModuleConfigRegistry();
        String craft = registry.defaultFile(registry.require("craft"));
        String sudo = registry.defaultFile(registry.require("sudo"));

        assertTrue(craft.contains("schema_version = 1"));
        assertTrue(craft.contains("module_id = \"craft\""));
        assertTrue(craft.contains("[gui]"));
        assertTrue(craft.contains("[limits]"));
        assertFalse(craft.contains("cooldown_seconds"));
        assertTrue(sudo.contains("[delegation]"));
        assertTrue(sudo.contains("enabled = false"));
        assertTrue(sudo.contains("maximum_temporary_vanilla_permission_level = 2"));
        assertTrue(sudo.contains("allowed_roots = \"effect\""));
        assertTrue(sudo.contains("allow_redirects = false"));
        assertTrue(sudo.contains("allow_async = false"));
        assertTrue(registry.generatedReference().contains("sef.cooldown.<action>.<seconds>"));
    }

    @Test
    void trackedConfigurationReferenceMatchesRuntimeRegistry() throws Exception {
        Path reference = repositoryRoot().resolve("docs/CONFIGURATION_REFERENCE.md");
        String generated = new ModuleConfigRegistry().generatedReference();
        if (Boolean.getBoolean("sef.updateProjectReferences")) {
            Files.writeString(reference, generated, StandardCharsets.UTF_8);
        }
        String tracked = Files.readString(reference, StandardCharsets.UTF_8);

        assertEquals(generated, tracked);
    }

    @Test
    void trackedDefaultDirectoryFixtureMatchesRuntimeRegistry() throws Exception {
        ModuleConfigRegistry registry = new ModuleConfigRegistry();
        Path fixture = repositoryRoot().resolve("src/test/resources/fixtures/config/sef/modules");
        if (Boolean.getBoolean("sef.updateProjectReferences")) {
            Files.createDirectories(fixture);
            Files.writeString(
                    fixture.resolve("index.toml"),
                    registry.defaultIndex(),
                    StandardCharsets.UTF_8);
            for (ModuleConfigRegistry.ModuleDefinition module : registry.definitions()) {
                Files.writeString(
                        fixture.resolve(module.fileName()),
                        registry.defaultFile(module),
                        StandardCharsets.UTF_8);
            }
        }
        Set<String> expected = registry.definitions().stream()
                .map(ModuleConfigRegistry.ModuleDefinition::fileName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        expected.add("index.toml");
        Set<String> actual;
        try (var files = Files.list(fixture)) {
            actual = files
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toSet());
        }
        assertEquals(expected, actual);
        assertEquals(
                registry.defaultIndex(),
                Files.readString(fixture.resolve("index.toml"), StandardCharsets.UTF_8));
        for (ModuleConfigRegistry.ModuleDefinition module : registry.definitions()) {
            assertEquals(
                    registry.defaultFile(module),
                    Files.readString(fixture.resolve(module.fileName()), StandardCharsets.UTF_8),
                    module.id());
        }
    }

    private static Path repositoryRoot() {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("settings.gradle"))
                    && Files.isDirectory(current.resolve("src"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("repository root is unavailable");
    }
}
