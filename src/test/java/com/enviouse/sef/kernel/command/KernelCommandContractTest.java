package com.enviouse.sef.kernel.command;

import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.kernel.ActionResult;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KernelCommandContractTest {
    @Test
    void completeCatalogSealsAndRejectsIncompleteCapabilityReferences() {
        CapabilityManifest capabilities = capabilities("sef.test.use");
        PanelContracts.Registry panels = panels();
        CommandCatalog catalog = new CommandCatalog(capabilities, panels);
        catalog.register(definition(
                "sef:test",
                "sef test",
                Set.of("test"),
                "sef.test.use",
                CommandDefinition.AccessClass.PLAYER,
                AuditService.AuditClass.METADATA_ONLY));

        catalog.seal();

        assertTrue(catalog.sealed());
        assertTrue(catalog.validate().isEmpty());
        assertThrows(IllegalStateException.class, () -> catalog.register(definition(
                "sef:late",
                "sef late",
                Set.of(),
                "sef.test.use",
                CommandDefinition.AccessClass.PLAYER,
                AuditService.AuditClass.METADATA_ONLY)));

        CommandCatalog invalid = new CommandCatalog(capabilities, panels);
        invalid.register(definition(
                "sef:unknown_permission",
                "sef unknown",
                Set.of(),
                "sef.missing",
                CommandDefinition.AccessClass.PLAYER,
                AuditService.AuditClass.METADATA_ONLY));
        assertFalse(invalid.validate().isEmpty());
        assertThrows(IllegalStateException.class, invalid::seal);
    }

    @Test
    void capabilityInferenceCoversUniversalControlClasses() {
        assertEquals(CapabilityManifest.CapabilityType.GUI, CapabilityManifest.inferType("sef.kernel.gui.open"));
        assertEquals(CapabilityManifest.CapabilityType.HUD, CapabilityManifest.inferType("sef.kernel.hud.view"));
        assertEquals(CapabilityManifest.CapabilityType.PANEL, CapabilityManifest.inferType("sef.kernel.panel.open"));
        assertEquals(CapabilityManifest.CapabilityType.TARGET, CapabilityManifest.inferType("sef.kernel.target.others"));
        assertEquals(CapabilityManifest.CapabilityType.AUDIENCE, CapabilityManifest.inferType("sef.kernel.audience.resolve"));
        assertEquals(CapabilityManifest.CapabilityType.EDITOR, CapabilityManifest.inferType("sef.kernel.editor.use"));
        assertEquals(CapabilityManifest.CapabilityType.ALIAS, CapabilityManifest.inferType("sef.kernel.alias.manage"));
        assertEquals(CapabilityManifest.CapabilityType.BUNDLE, CapabilityManifest.inferType("sef.kernel.bundle.manage"));
        assertEquals(CapabilityManifest.CapabilityType.PROFILE, CapabilityManifest.inferType("sef.kernel.profile.manage"));
        assertEquals(CapabilityManifest.CapabilityType.BYPASS, CapabilityManifest.inferType("sef.kernel.bypass.cooldown"));
        assertEquals(CapabilityManifest.CapabilityType.SENSITIVE_DATA, CapabilityManifest.inferType("sef.kernel.sensitive.view"));
        assertThrows(IllegalArgumentException.class, () -> CapabilityManifest.inferType("x".repeat(129)));
        assertThrows(IllegalArgumentException.class, () -> new CapabilityManifest.Capability(
                "sef.test",
                CapabilityManifest.CapabilityType.COMMAND,
                false,
                "x".repeat(129),
                "description"));
    }

    @Test
    void shortcutCollisionModesAreDeterministic() {
        CapabilityManifest capabilities = capabilities("sef.test.use");
        CommandCatalog catalog = catalog(capabilities);
        ShortcutRegistry shortcuts = new ShortcutRegistry(catalog, capabilities);
        shortcuts.register(new ShortcutRegistry.Shortcut(
                "active",
                "sef:test",
                ShortcutRegistry.ArgumentAdapter.NONE,
                "",
                CommandDefinition.ConflictPolicy.PREFER_SEF,
                1));
        shortcuts.register(new ShortcutRegistry.Shortcut(
                "canonical",
                "sef:test",
                ShortcutRegistry.ArgumentAdapter.NONE,
                "",
                CommandDefinition.ConflictPolicy.CANONICAL_ONLY,
                1));
        shortcuts.captureExistingRoots(Set.of("active", "canonical"));

        Map<String, ShortcutRegistry.Status> statuses = shortcuts.diagnostics().stream()
                .collect(java.util.stream.Collectors.toMap(
                        ShortcutRegistry.Diagnostic::root,
                        ShortcutRegistry.Diagnostic::status));
        assertEquals(ShortcutRegistry.Status.ACTIVE_OVERRIDE, statuses.get("active"));
        assertEquals(ShortcutRegistry.Status.CANONICAL_ONLY, statuses.get("canonical"));
        assertTrue(shortcuts.isActive("active"));
        assertFalse(shortcuts.isActive("canonical"));
        assertEquals(Map.of("active", "sef:test"), shortcuts.activeAliasMap());
    }

    @Test
    void aliasCompilerRejectsUnknownRecursiveAmbiguousAndWeakeningDefinitions() {
        CapabilityManifest capabilities = capabilities("sef.test.use");
        CommandCatalog catalog = catalog(capabilities);
        AliasCompiler compiler = new AliasCompiler(catalog, capabilities, Set.of(), Map.of());
        AliasCompiler.Registry registry = new AliasCompiler.Registry(compiler, 8);

        AliasCompiler.AliasDefinition unknown = alias(
                "custom:unknown",
                "unknown",
                "sef:missing",
                CommandDefinition.AccessClass.PLAYER,
                AuditService.AuditClass.METADATA_ONLY);
        assertEquals(ActionResult.ReasonCode.NOT_FOUND, compiler.compile(unknown).reason());

        AliasCompiler.AliasDefinition recursive = alias(
                "sef:test",
                "recursive",
                "sef:test",
                CommandDefinition.AccessClass.PLAYER,
                AuditService.AuditClass.METADATA_ONLY);
        assertEquals(ActionResult.ReasonCode.RECURSION_DENIED, compiler.compile(recursive).reason());

        CommandCatalog privilegedCatalog = new CommandCatalog(capabilities, panels());
        privilegedCatalog.register(definition(
                "sef:staff",
                "sef staff",
                Set.of(),
                "sef.test.use",
                CommandDefinition.AccessClass.STAFF,
                AuditService.AuditClass.ADMIN_ACTION));
        privilegedCatalog.seal();
        AliasCompiler privilegedCompiler = new AliasCompiler(privilegedCatalog, capabilities, Set.of(), Map.of());
        AliasCompiler.AliasDefinition weak = alias(
                "custom:weak",
                "weak",
                "sef:staff",
                CommandDefinition.AccessClass.PLAYER,
                AuditService.AuditClass.METADATA_ONLY);
        assertEquals(ActionResult.ReasonCode.POLICY_DENIED, privilegedCompiler.compile(weak).reason());

        AliasCompiler.AliasDefinition first = alias(
                "custom:first",
                "shared",
                "sef:test",
                CommandDefinition.AccessClass.PLAYER,
                AuditService.AuditClass.METADATA_ONLY);
        AliasCompiler.AliasDefinition second = alias(
                "custom:second",
                "shared",
                "sef:test",
                CommandDefinition.AccessClass.PLAYER,
                AuditService.AuditClass.METADATA_ONLY);
        assertTrue(registry.saveDraft(first).successful());
        assertTrue(registry.publish(first.id(), first.revision()).successful());
        assertTrue(registry.saveDraft(second).successful());
        assertEquals(ActionResult.ReasonCode.CONFLICT, registry.publish(second.id(), second.revision()).reason());
    }

    @Test
    void aliasPublicationHonorsCatalogAndExternalRootOwnership() {
        CapabilityManifest capabilities = capabilities("sef.test.use");
        CommandCatalog catalog = catalog(capabilities);
        AliasCompiler.Registry catalogRegistry = new AliasCompiler.Registry(
                new AliasCompiler(catalog, capabilities, Set.of(), Map.of()),
                8);
        AliasCompiler.AliasDefinition catalogCollision = alias(
                "custom:catalog",
                "sef",
                "sef:test",
                CommandDefinition.AccessClass.PLAYER,
                AuditService.AuditClass.METADATA_ONLY);
        assertTrue(catalogRegistry.saveDraft(catalogCollision).successful());
        assertEquals(
                ActionResult.ReasonCode.CONFLICT,
                catalogRegistry.publish(catalogCollision.id(), catalogCollision.revision()).reason());

        AliasCompiler externalCompiler = new AliasCompiler(
                catalog,
                capabilities,
                Set.of(),
                Map.of(),
                root -> new AliasCompiler.RootOwnership(
                        AliasCompiler.RootOwnerKind.EXTERNAL,
                        "example:" + root));
        AliasCompiler.Registry preferredRegistry = new AliasCompiler.Registry(externalCompiler, 8);
        AliasCompiler.AliasDefinition preferred = alias(
                "custom:preferred",
                "external",
                "sef:test",
                CommandDefinition.AccessClass.PLAYER,
                AuditService.AuditClass.METADATA_ONLY);
        assertTrue(preferredRegistry.saveDraft(preferred).successful());
        assertTrue(preferredRegistry.publish(preferred.id(), preferred.revision()).successful());

        AliasCompiler.Registry canonicalRegistry = new AliasCompiler.Registry(externalCompiler, 8);
        AliasCompiler.AliasDefinition canonicalOnly = withConflictMode(
                alias(
                        "custom:canonical",
                        "external",
                        "sef:test",
                        CommandDefinition.AccessClass.PLAYER,
                        AuditService.AuditClass.METADATA_ONLY),
                CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        assertTrue(canonicalRegistry.saveDraft(canonicalOnly).successful());
        assertEquals(
                ActionResult.ReasonCode.CONFLICT,
                canonicalRegistry.publish(canonicalOnly.id(), canonicalOnly.revision()).reason());
    }

    @Test
    void publishedAliasAndItsDraftConsumeOneDefinitionSlot() {
        CapabilityManifest capabilities = capabilities("sef.test.use");
        CommandCatalog catalog = catalog(capabilities);
        AliasCompiler.Registry registry = new AliasCompiler.Registry(
                new AliasCompiler(catalog, capabilities, Set.of(), Map.of()),
                1);
        AliasCompiler.AliasDefinition first = alias(
                "custom:first",
                "first",
                "sef:test",
                CommandDefinition.AccessClass.PLAYER,
                AuditService.AuditClass.METADATA_ONLY);
        assertTrue(registry.saveDraft(first).successful());
        assertTrue(registry.publish(first.id(), first.revision()).successful());

        AliasCompiler.AliasDefinition revised = new AliasCompiler.AliasDefinition(
                first.schemaVersion(),
                first.id(),
                2,
                first.enabled(),
                AliasCompiler.DefinitionState.DRAFT,
                first.root(),
                first.kind(),
                first.targetId(),
                first.argumentSchema(),
                first.fixedArguments(),
                first.additionalPermissionId(),
                first.sourceTypes(),
                first.accessClass(),
                first.conflictMode(),
                first.auditClass(),
                first.createdBy(),
                first.createdAt());
        assertTrue(registry.saveDraft(revised).successful());
        assertTrue(registry.publish(revised.id(), revised.revision()).successful());

        AliasCompiler.AliasDefinition second = alias(
                "custom:second",
                "second",
                "sef:test",
                CommandDefinition.AccessClass.PLAYER,
                AuditService.AuditClass.METADATA_ONLY);
        assertEquals(ActionResult.ReasonCode.QUOTA_EXCEEDED, registry.saveDraft(second).reason());
    }

    @Test
    void bundleCompilerRejectsCyclesRawCommandsAndUnboundedExpansion() {
        CapabilityManifest capabilities = capabilities("sef.test.use");
        CommandCatalog catalog = catalog(capabilities);
        BundleCompiler compiler = new BundleCompiler(catalog, 8, 3, 10, 20);

        BundleCompiler.BundleDefinition first = bundle(
                "custom:first",
                List.of(step("nested", BundleCompiler.StepKind.BUNDLE, "custom:second")));
        BundleCompiler.BundleDefinition second = bundle(
                "custom:second",
                List.of(step("nested", BundleCompiler.StepKind.BUNDLE, "custom:first")));
        assertEquals(
                ActionResult.ReasonCode.RECURSION_DENIED,
                compiler.compileAll(Map.of(first.id(), first, second.id(), second)).reason());

        BundleCompiler.BundleDefinition raw = bundle(
                "custom:raw",
                List.of(step("raw", BundleCompiler.StepKind.RAW_COMMAND, "op @a")));
        assertEquals(ActionResult.ReasonCode.POLICY_DENIED, compiler.compileAll(Map.of(raw.id(), raw)).reason());

        BundleCompiler.BundleDefinition expanded = new BundleCompiler.BundleDefinition(
                1,
                "custom:expanded",
                1,
                BundleCompiler.DefinitionState.DRAFT,
                true,
                "",
                Set.of(CommandDefinition.SourceType.PLAYER),
                BundleCompiler.AuthorizationMode.STRICT_ACTOR,
                BundleCompiler.ExecutionMode.STOP_ON_FAILURE,
                10,
                1,
                Duration.ofMinutes(1),
                true,
                AuditService.AuditClass.WORKFLOW_EXECUTION,
                List.of(
                        step("one", BundleCompiler.StepKind.SEF_ACTION, "sef:test"),
                        step("two", BundleCompiler.StepKind.SEF_ACTION, "sef:test"),
                        step("three", BundleCompiler.StepKind.SEF_ACTION, "sef:test")));
        assertEquals(ActionResult.ReasonCode.QUOTA_EXCEEDED,
                compiler.compileAll(Map.of(expanded.id(), expanded)).reason());

        BundleCompiler.BundleDefinition nestedLeaf = new BundleCompiler.BundleDefinition(
                1,
                "custom:leaf",
                1,
                BundleCompiler.DefinitionState.DRAFT,
                true,
                "",
                Set.of(CommandDefinition.SourceType.PLAYER),
                BundleCompiler.AuthorizationMode.STRICT_ACTOR,
                BundleCompiler.ExecutionMode.STOP_ON_FAILURE,
                5,
                1,
                Duration.ofMinutes(1),
                true,
                AuditService.AuditClass.WORKFLOW_EXECUTION,
                List.of(
                        step("one", BundleCompiler.StepKind.SEF_ACTION, "sef:test"),
                        step("two", BundleCompiler.StepKind.SEF_ACTION, "sef:test"),
                        step("three", BundleCompiler.StepKind.SEF_ACTION, "sef:test")));
        BundleCompiler.BundleDefinition nestedRoot = new BundleCompiler.BundleDefinition(
                1,
                "custom:root",
                1,
                BundleCompiler.DefinitionState.DRAFT,
                true,
                "",
                Set.of(CommandDefinition.SourceType.PLAYER),
                BundleCompiler.AuthorizationMode.STRICT_ACTOR,
                BundleCompiler.ExecutionMode.STOP_ON_FAILURE,
                2,
                1,
                Duration.ofMinutes(1),
                true,
                AuditService.AuditClass.WORKFLOW_EXECUTION,
                List.of(step("nested", BundleCompiler.StepKind.BUNDLE, "custom:leaf")));
        assertEquals(
                ActionResult.ReasonCode.QUOTA_EXCEEDED,
                compiler.compileAll(Map.of(
                        nestedLeaf.id(), nestedLeaf,
                        nestedRoot.id(), nestedRoot)).reason());
    }

    @Test
    void commandWrapperRejectsNestedOriginsAndWrapperRoots() {
        UUID correlation = UUID.randomUUID();
        CommandWrapperService.Request direct = new CommandWrapperService.Request(
                correlation,
                UUID.randomUUID(),
                null,
                CommandDefinition.SourceType.PLAYER,
                "/time set day",
                CommandWrapperService.OutputMode.NORMAL,
                CommandWrapperService.Origin.DIRECT,
                3,
                4,
                List.of(),
                Map.of());
        ActionResult<CommandWrapperService.Preflight> accepted =
                CommandWrapperService.preflight(direct, 128);
        assertTrue(accepted.successful());
        assertEquals("time", accepted.value().normalizedRoot());

        CommandWrapperService.Request nested = new CommandWrapperService.Request(
                correlation,
                UUID.randomUUID(),
                null,
                CommandDefinition.SourceType.BUNDLE,
                "time set day",
                CommandWrapperService.OutputMode.NORMAL,
                CommandWrapperService.Origin.BUNDLE,
                3,
                4,
                List.of(),
                Map.of());
        assertEquals(ActionResult.ReasonCode.RECURSION_DENIED,
                CommandWrapperService.preflight(nested, 128).reason());

        CommandWrapperService.Request recursive = new CommandWrapperService.Request(
                correlation,
                UUID.randomUUID(),
                null,
                CommandDefinition.SourceType.PLAYER,
                "silent stop",
                CommandWrapperService.OutputMode.NORMAL,
                CommandWrapperService.Origin.DIRECT,
                3,
                4,
                List.of(),
                Map.of());
        assertEquals(ActionResult.ReasonCode.RECURSION_DENIED,
                CommandWrapperService.preflight(recursive, 128).reason());
    }

    private static CapabilityManifest capabilities(String permission) {
        CapabilityManifest capabilities = new CapabilityManifest();
        capabilities.register(new CapabilityManifest.Capability(
                permission,
                CapabilityManifest.CapabilityType.COMMAND,
                false,
                "test",
                "test permission"));
        return capabilities;
    }

    private static PanelContracts.Registry panels() {
        PanelContracts.Registry panels = new PanelContracts.Registry();
        panels.registerCommandOnly("sef:test");
        return panels;
    }

    private static CommandCatalog catalog(CapabilityManifest capabilities) {
        CommandCatalog catalog = new CommandCatalog(capabilities, panels());
        catalog.register(definition(
                "sef:test",
                "sef test",
                Set.of("test"),
                "sef.test.use",
                CommandDefinition.AccessClass.PLAYER,
                AuditService.AuditClass.METADATA_ONLY));
        catalog.seal();
        return catalog;
    }

    private static CommandDefinition definition(
            String id,
            String route,
            Set<String> roots,
            String permission,
            CommandDefinition.AccessClass access,
            AuditService.AuditClass auditClass
    ) {
        return new CommandDefinition(
                id,
                route,
                roots,
                "command.test.description",
                "command.test.usage",
                "test",
                "sef.test",
                Set.of(permission),
                access,
                Set.of(CommandDefinition.SourceType.PLAYER),
                CommandDefinition.TargetBehavior.NONE,
                id,
                false,
                auditClass,
                "sef:test",
                "",
                "hud is not applicable",
                "",
                "quota is not applicable",
                CommandDefinition.ConflictPolicy.PREFER_SEF,
                true,
                true);
    }

    private static AliasCompiler.AliasDefinition alias(
            String id,
            String root,
            String target,
            CommandDefinition.AccessClass access,
            AuditService.AuditClass auditClass
    ) {
        return new AliasCompiler.AliasDefinition(
                1,
                id,
                1,
                true,
                AliasCompiler.DefinitionState.DRAFT,
                root,
                AliasCompiler.AliasKind.ACTION,
                target,
                AliasCompiler.ArgumentSchema.NONE,
                Map.of(),
                "",
                Set.of(CommandDefinition.SourceType.PLAYER),
                access,
                CommandDefinition.ConflictPolicy.PREFER_SEF,
                auditClass,
                UUID.randomUUID(),
                Instant.EPOCH);
    }

    private static AliasCompiler.AliasDefinition withConflictMode(
            AliasCompiler.AliasDefinition definition,
            CommandDefinition.ConflictPolicy conflictMode
    ) {
        return new AliasCompiler.AliasDefinition(
                definition.schemaVersion(),
                definition.id(),
                definition.revision(),
                definition.enabled(),
                definition.state(),
                definition.root(),
                definition.kind(),
                definition.targetId(),
                definition.argumentSchema(),
                definition.fixedArguments(),
                definition.additionalPermissionId(),
                definition.sourceTypes(),
                definition.accessClass(),
                conflictMode,
                definition.auditClass(),
                definition.createdBy(),
                definition.createdAt());
    }

    private static BundleCompiler.BundleDefinition bundle(
            String id,
            List<BundleCompiler.BundleStep> steps
    ) {
        return new BundleCompiler.BundleDefinition(
                1,
                id,
                1,
                BundleCompiler.DefinitionState.DRAFT,
                true,
                "",
                Set.of(CommandDefinition.SourceType.PLAYER),
                BundleCompiler.AuthorizationMode.STRICT_ACTOR,
                BundleCompiler.ExecutionMode.STOP_ON_FAILURE,
                1,
                1,
                Duration.ofMinutes(1),
                true,
                AuditService.AuditClass.WORKFLOW_EXECUTION,
                steps);
    }

    private static BundleCompiler.BundleStep step(
            String id,
            BundleCompiler.StepKind kind,
            String target
    ) {
        return new BundleCompiler.BundleStep(
                id,
                kind,
                target,
                BundleCompiler.TargetBinding.ACTOR,
                BundleCompiler.FailureBehavior.STOP,
                Duration.ZERO,
                Map.of());
    }
}
