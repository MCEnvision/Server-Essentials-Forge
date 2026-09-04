package com.enviouse.sef.config.modules;

import com.enviouse.sef.config.ConfigHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleConfigServiceTest {
    @TempDir
    Path temporaryDirectory;

    private ModuleConfigService service;

    @AfterEach
    void stopWatcher() {
        if (service != null) {
            service.stop();
        }
    }

    @Test
    void startupGeneratesAndLoadsEveryOwnedModule() {
        service = new ModuleConfigService(new ModuleConfigRegistry());

        var publication = service.start(temporaryDirectory.resolve("sef"), Runnable::run);

        assertTrue(publication.successful(), publication.detail());
        assertEquals(62, service.modules().size());
        assertTrue(Files.isRegularFile(temporaryDirectory.resolve("sef/modules/index.toml")));
        assertTrue(Files.isRegularFile(temporaryDirectory.resolve("sef/modules/craft.toml")));
        assertTrue(Files.isRegularFile(temporaryDirectory.resolve("sef/modules/privacy.toml")));
    }

    @Test
    void startupMaterializesNewSettingsWithRecoveryBackup() throws Exception {
        Path root = temporaryDirectory.resolve("sef");
        service = new ModuleConfigService(new ModuleConfigRegistry());
        assertTrue(service.start(root, Runnable::run).successful());
        service.stop();

        Path sudo = root.resolve("modules/sudo.toml");
        String legacy = Files.readString(sudo, StandardCharsets.UTF_8)
                .replace("documentation_version = 3", "documentation_version = 1")
                .replaceAll("(?s)\\n\\[delegation]\\n.*", "")
                + "\n[extension]\noperator_note = \"keep\"\n";
        Files.writeString(sudo, legacy, StandardCharsets.UTF_8);
        Set<PosixFilePermission> originalPermissions = null;
        if (sudo.getFileSystem().supportedFileAttributeViews().contains("posix")) {
            originalPermissions = Set.of(PosixFilePermission.OWNER_READ);
            Files.setPosixFilePermissions(sudo, originalPermissions);
        }

        service = new ModuleConfigService(new ModuleConfigRegistry());
        var publication = service.start(root, Runnable::run);

        assertTrue(publication.successful(), publication.detail());
        assertEquals("false", service.value("sudo", "delegation.enabled"));
        String migrated = Files.readString(sudo, StandardCharsets.UTF_8);
        assertTrue(migrated.contains("documentation_version = 3"));
        assertTrue(migrated.contains("[delegation]"));
        assertTrue(migrated.contains("# Enables separately authorized one execution delegated sudo grants."));
        assertTrue(migrated.contains("operator_note = \"keep\""));
        Path backup = root.resolve("modules/history/sudo/documentation-1-to-3.bak");
        assertTrue(Files.isRegularFile(backup));
        assertEquals(legacy, Files.readString(backup, StandardCharsets.UTF_8));
        if (originalPermissions != null) {
            assertEquals(originalPermissions, Files.getPosixFilePermissions(sudo));
        }
    }

    @Test
    void startupRejectsDetectableHardLinkedModuleFile() throws Exception {
        org.junit.jupiter.api.Assumptions.assumeTrue(
                temporaryDirectory.getFileSystem().supportedFileAttributeViews().contains("unix"));
        Path root = temporaryDirectory.resolve("sef");
        service = new ModuleConfigService(new ModuleConfigRegistry());
        assertTrue(service.start(root, Runnable::run).successful());
        service.stop();
        Path craft = root.resolve("modules/craft.toml");
        Files.createLink(temporaryDirectory.resolve("craft-copy.toml"), craft);

        service = new ModuleConfigService(new ModuleConfigRegistry());
        var publication = service.start(root, Runnable::run);

        assertFalse(publication.successful());
        assertTrue(publication.diagnostics().stream()
                .anyMatch(diagnostic -> diagnostic.message().contains("multiple hard links")));
    }

    @Test
    void startupRejectsConfigurationParentSymlink() throws Exception {
        Path external = temporaryDirectory.resolve("external");
        Files.createDirectories(external);
        Path linked = temporaryDirectory.resolve("linked");
        Files.createSymbolicLink(linked, external);

        service = new ModuleConfigService(new ModuleConfigRegistry());
        var publication = service.start(linked.resolve("sef"), Runnable::run);

        assertFalse(publication.successful());
        assertTrue(publication.diagnostics().stream()
                .anyMatch(diagnostic -> diagnostic.operation().equals("startup")));
        assertFalse(Files.exists(external.resolve("sef"), java.nio.file.LinkOption.NOFOLLOW_LINKS));
    }

    @Test
    void malformedEditKeepsCompletePreviousSnapshot() throws Exception {
        service = new ModuleConfigService(new ModuleConfigRegistry());
        assertTrue(service.start(temporaryDirectory.resolve("sef"), Runnable::run).successful());
        long beforeRevision = service.revision();
        String before = service.value("craft", "limits.maximum_page_size");
        Path craft = temporaryDirectory.resolve("sef/modules/craft.toml");
        Files.writeString(craft, "[broken\n", StandardCharsets.UTF_8);

        var publication = service.reload(List.of("craft"), "test");

        assertFalse(publication.successful());
        assertEquals(beforeRevision, service.revision());
        assertEquals(before, service.value("craft", "limits.maximum_page_size"));
    }

    @Test
    void typedPublicationUsesExpectedRevisionAndSupportsRollback() {
        service = new ModuleConfigService(new ModuleConfigRegistry());
        assertTrue(service.start(temporaryDirectory.resolve("sef"), Runnable::run).successful());
        long expected = service.revision();

        var changed = service.publishSetting(
                "gui",
                "gui.mode",
                "off",
                expected,
                UUID.randomUUID());

        assertTrue(changed.successful(), changed.detail());
        assertEquals("off", service.value("gui", "gui.mode"));
        assertFalse(service.publishSetting(
                "gui",
                "gui.mode",
                "on",
                expected,
                UUID.randomUUID()).successful());
        var history = service.history("gui");
        assertFalse(history.isEmpty());

        var rolledBack = service.rollback(
                "gui",
                history.getFirst().revision(),
                service.revision(),
                UUID.randomUUID());
        assertTrue(rolledBack.successful(), rolledBack.detail());
        assertEquals("auto", service.value("gui", "gui.mode"));
    }

    @Test
    void historySurvivesRestartAndRemainsRollbackCapable() {
        Path root = temporaryDirectory.resolve("sef");
        service = new ModuleConfigService(new ModuleConfigRegistry());
        assertTrue(service.start(root, Runnable::run).successful());
        assertTrue(service.publishSetting(
                "gui",
                "gui.mode",
                "off",
                service.revision(),
                UUID.randomUUID()).successful());
        long historicalRevision = service.history("gui").getFirst().revision();
        service.stop();

        service = new ModuleConfigService(new ModuleConfigRegistry());
        assertTrue(service.start(root, Runnable::run).successful());
        assertTrue(service.history("gui").stream()
                .anyMatch(entry -> entry.revision() == historicalRevision));
        var rollback = service.rollback(
                "gui",
                historicalRevision,
                service.revision(),
                UUID.randomUUID());

        assertTrue(rollback.successful(), rollback.detail());
        assertEquals("auto", service.value("gui", "gui.mode"));
    }

    @Test
    void restartRequiredSettingRemainsPendingWithoutLosingLaterEdits() throws Exception {
        Path root = temporaryDirectory.resolve("sef");
        service = new ModuleConfigService(new ModuleConfigRegistry());
        assertTrue(service.start(root, Runnable::run).successful());

        var pending = service.publishSetting(
                "super_enchanting",
                "shortcuts.enable_set",
                "false",
                service.revision(),
                UUID.randomUUID());

        assertTrue(pending.successful(), pending.detail());
        assertTrue(pending.detail().contains("restart required"));
        assertEquals("true", service.value("super_enchanting", "shortcuts.enable_set"));
        assertEquals(
                "false",
                service.diff("super_enchanting").changes().get("shortcuts.enable_set").after());
        Path module = root.resolve("modules/super_enchanting.toml");
        assertTrue(Files.readString(module, StandardCharsets.UTF_8).contains("enable_set = false"));

        var liveEdit = service.publishSetting(
                "super_enchanting",
                "safety.maximum_level",
                "500",
                service.revision(),
                UUID.randomUUID());

        assertTrue(liveEdit.successful(), liveEdit.detail());
        assertEquals("500", service.value("super_enchanting", "safety.maximum_level"));
        assertEquals("true", service.value("super_enchanting", "shortcuts.enable_set"));
        String written = Files.readString(module, StandardCharsets.UTF_8);
        assertTrue(written.contains("maximum_level = 500"));
        assertTrue(written.contains("enable_set = false"));

        long stableRevision = service.revision();
        var reconciliation = service.reload(List.of("super_enchanting"), "reconciliation");
        assertTrue(reconciliation.successful(), reconciliation.detail());
        assertEquals(stableRevision, service.revision());
        assertTrue(reconciliation.changedModules().isEmpty());
        service.stop();

        service = new ModuleConfigService(new ModuleConfigRegistry());
        assertTrue(service.start(root, Runnable::run).successful());
        assertEquals("false", service.value("super_enchanting", "shortcuts.enable_set"));
        assertEquals("500", service.value("super_enchanting", "safety.maximum_level"));
    }

    @Test
    void parserRejectsExecutableAndComplexTomlLiterals() {
        assertFalse(ModuleConfigService.parseToml("value = \"safe\"").isEmpty());
        assertNotNull(ModuleConfigService.parseToml("[gui]\nmode = \"auto\"").get("gui.mode"));
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> ModuleConfigService.parseToml("command = [\"op @a\"]"));
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> ModuleConfigService.parseToml("value = { raw = \"command\" }"));
    }

    @Test
    void legacyMigrationStagesValidatesBacksUpAndPublishesMappedValues() throws Exception {
        Path root = temporaryDirectory.resolve("sef");
        Files.createDirectories(root);
        String legacy = """
                [ServerEssentialsForgeModConfig.modules]
                sudo = false

                [ServerEssentialsForgeModConfig.gui]
                enabled = false
                maximumPanelEntries = 20

                [ServerEssentialsForgeModConfig.virtualWorkstations]
                superEnchantingMaxLevel = 400
                craftCooldownSeconds = 12
                """;
        Files.writeString(root.resolve("common.toml"), legacy, StandardCharsets.UTF_8);
        service = new ModuleConfigService(new ModuleConfigRegistry());
        assertTrue(service.start(root, Runnable::run).successful());

        ModuleConfigService.MigrationReport report = service.legacyMigrationReport();
        assertEquals("dry run ready", report.mode());
        assertEquals(8, report.mappings().size());
        assertTrue(report.unmappedFields().stream()
                .anyMatch(value -> value.contains("cooldown") && value.contains("ignored")));

        var publication = service.migrateLegacy(
                report.revision(),
                report.sourceFingerprint(),
                UUID.randomUUID());

        assertTrue(publication.successful(), publication.detail());
        assertEquals("false", service.value("sudo", "module.enabled"));
        assertEquals("false", service.value("sudo", "runtime.enable_sudo"));
        assertEquals("off", service.value("gui", "gui.mode"));
        assertEquals("false", service.value("gui", "runtime.enable_enhanced_gui"));
        assertEquals("20", service.value("gui", "limits.maximum_page_size"));
        assertEquals("20", service.value("gui", "runtime.gui_maximum_panel_entries"));
        assertEquals("400", service.value("super_enchanting", "safety.maximum_level"));
        assertEquals("400", service.value(
                "super_enchanting",
                "runtime.super_enchanting_max_level"));
        assertEquals(legacy, Files.readString(root.resolve("common.toml"), StandardCharsets.UTF_8));
        assertEquals(
                legacy,
                Files.readString(
                        root.resolve("backups/configuration/modular-migration-1/common.toml"),
                        StandardCharsets.UTF_8));
        assertTrue(Files.isRegularFile(root.resolve("modules/migration.toml")));
        assertFalse(Files.exists(root.resolve("migration-staging")));

        var repeated = service.migrateLegacy(
                service.revision(),
                report.sourceFingerprint(),
                UUID.randomUUID());
        assertTrue(repeated.successful());
        assertTrue(repeated.changedModules().isEmpty());
    }

    @Test
    void runtimePublicationAppliesLiveDecimalSetting() {
        service = new ModuleConfigService(new ModuleConfigRegistry());
        assertTrue(service.start(temporaryDirectory.resolve("sef"), Runnable::run).successful());
        double original = ConfigHandler.config.maximumFlySpeed.get();
        try {
            var publication = service.publishSetting(
                    "player_utilities",
                    "runtime.maximum_fly_speed",
                    "1.5",
                    service.revision(),
                    UUID.randomUUID());

            assertTrue(publication.successful(), publication.detail());
            ConfigHandler.publish(service);
            assertEquals(1.5D, ConfigHandler.config.maximumFlySpeed.get());
        } finally {
            ConfigHandler.config.maximumFlySpeed.set(original);
        }
    }

    @Test
    void runtimePublicationUsesAuthoritativeFancyTagAndDisguiseModuleState() {
        service = new ModuleConfigService(new ModuleConfigRegistry());
        assertTrue(service.start(temporaryDirectory.resolve("sef"), Runnable::run).successful());
        boolean originalFancyTags = ConfigHandler.config.enableFancyTags.get();
        boolean originalDisguises = ConfigHandler.config.enableDisguises.get();
        try {
            ConfigHandler.config.enableFancyTags.set(false);
            ConfigHandler.config.enableDisguises.set(false);

            ConfigHandler.publish(service);

            assertTrue(ConfigHandler.config.enableFancyTags.get());
            assertTrue(ConfigHandler.config.enableDisguises.get());
        } finally {
            ConfigHandler.config.enableFancyTags.set(originalFancyTags);
            ConfigHandler.config.enableDisguises.set(originalDisguises);
        }
    }

    @Test
    void legacyMigrationRejectsSourceChangesAfterPreview() throws Exception {
        Path root = temporaryDirectory.resolve("sef");
        Files.createDirectories(root);
        Path common = root.resolve("common.toml");
        Files.writeString(
                common,
                "[ServerEssentialsForgeModConfig.modules]\nsudo = false\n",
                StandardCharsets.UTF_8);
        service = new ModuleConfigService(new ModuleConfigRegistry());
        assertTrue(service.start(root, Runnable::run).successful());
        ModuleConfigService.MigrationReport report = service.legacyMigrationReport();
        Files.writeString(
                common,
                "[ServerEssentialsForgeModConfig.modules]\nsudo = true\n",
                StandardCharsets.UTF_8);

        var publication = service.migrateLegacy(
                report.revision(),
                report.sourceFingerprint(),
                UUID.randomUUID());

        assertFalse(publication.successful());
        assertTrue(publication.detail().contains("changed after preview"));
        assertEquals("true", service.value("sudo", "module.enabled"));
        assertFalse(Files.exists(root.resolve("modules/migration.toml")));
    }

    @Test
    void legacyMigrationRejectsConflictingRecoveryBackupBeforePublication() throws Exception {
        Path root = temporaryDirectory.resolve("sef");
        Files.createDirectories(root);
        Files.writeString(
                root.resolve("common.toml"),
                "[ServerEssentialsForgeModConfig.modules]\nsudo = false\n",
                StandardCharsets.UTF_8);
        service = new ModuleConfigService(new ModuleConfigRegistry());
        assertTrue(service.start(root, Runnable::run).successful());
        ModuleConfigService.MigrationReport report = service.legacyMigrationReport();
        Path backupRoot = root.resolve("backups/configuration/modular-migration-1");
        Files.createDirectories(backupRoot);
        Files.writeString(backupRoot.resolve("common.toml"), "conflict", StandardCharsets.UTF_8);

        var publication = service.migrateLegacy(
                report.revision(),
                report.sourceFingerprint(),
                UUID.randomUUID());

        assertFalse(publication.successful());
        assertTrue(publication.detail().contains("backup conflicts"));
        assertEquals("true", service.value("sudo", "module.enabled"));
        assertFalse(Files.exists(root.resolve("modules/migration.toml")));
        assertFalse(Files.exists(root.resolve("migration-staging")));
    }

    @Test
    void legacyMigrationRejectsSymlinkedBackupRoot() throws Exception {
        org.junit.jupiter.api.Assumptions.assumeTrue(
                temporaryDirectory.getFileSystem().supportedFileAttributeViews().contains("posix"));
        Path root = temporaryDirectory.resolve("sef");
        Files.createDirectories(root);
        Files.writeString(
                root.resolve("common.toml"),
                "[ServerEssentialsForgeModConfig.modules]\nsudo = false\n",
                StandardCharsets.UTF_8);
        service = new ModuleConfigService(new ModuleConfigRegistry());
        assertTrue(service.start(root, Runnable::run).successful());
        ModuleConfigService.MigrationReport report = service.legacyMigrationReport();
        Path outside = temporaryDirectory.resolve("outside");
        Files.createDirectory(outside);
        Files.createSymbolicLink(root.resolve("backups"), outside);

        var publication = service.migrateLegacy(
                report.revision(),
                report.sourceFingerprint(),
                UUID.randomUUID());

        assertFalse(publication.successful());
        assertEquals("true", service.value("sudo", "module.enabled"));
        try (var files = Files.list(outside)) {
            assertTrue(files.findAny().isEmpty());
        }
        assertFalse(Files.exists(root.resolve("modules/migration.toml")));
    }
}
