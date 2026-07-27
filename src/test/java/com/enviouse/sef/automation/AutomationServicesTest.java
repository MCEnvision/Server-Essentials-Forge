package com.enviouse.sef.automation;

import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.command.AliasCompiler;
import com.enviouse.sef.kernel.command.BundleCompiler;
import com.enviouse.sef.kernel.command.CapabilityManifest;
import com.enviouse.sef.kernel.command.CommandCatalog;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.kernel.command.PanelContracts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutomationServicesTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void aliasLifecyclePersistsAndRejectsRootCollisions() throws Exception {
        AliasService service = aliasService();
        service.load(temporaryDirectory);
        UUID actor = UUID.randomUUID();

        ActionResult<AliasCompiler.AliasDefinition> created = service.createDraft(
                "custom:day",
                "day",
                AliasCompiler.AliasKind.ACTION,
                "sef:test",
                AliasCompiler.ArgumentSchema.NONE,
                "",
                CommandDefinition.AccessClass.PLAYER,
                AuditService.AuditClass.METADATA_ONLY,
                actor);
        assertTrue(created.successful());
        assertTrue(service.validateDraft(created.value().id(), created.value().revision()).successful());

        ActionResult<AliasCompiler.AliasDefinition> published =
                service.publish(created.value().id(), created.value().revision(), actor);
        assertTrue(published.successful());
        assertEquals("day", service.findRoot("day").orElseThrow().root());

        ActionResult<AliasCompiler.AliasDefinition> collision = service.createDraft(
                "custom:collision",
                "day",
                AliasCompiler.AliasKind.ACTION,
                "sef:test",
                AliasCompiler.ArgumentSchema.NONE,
                "",
                CommandDefinition.AccessClass.PLAYER,
                AuditService.AuditClass.METADATA_ONLY,
                actor);
        assertTrue(collision.successful());
        assertEquals(
                ActionResult.ReasonCode.CONFLICT,
                service.publish(collision.value().id(), collision.value().revision(), actor).reason());

        service.flush();
        AliasService restored = aliasService();
        assertEquals(
                com.enviouse.sef.storage.repository.StorageRepository.RepositoryState.READY,
                restored.load(temporaryDirectory).state());
        assertEquals(published.value().revision(), restored.find("custom:day").orElseThrow().revision());
    }

    @Test
    void commandProfilesEnforceContextsBindingsAndReferences() throws Exception {
        CommandProfileService profiles = new CommandProfileService();
        profiles.load(temporaryDirectory);
        UUID actor = UUID.randomUUID();

        ActionResult<CommandProfileService.CommandProfile> invalidTargeted = profiles.createDraft(
                "custom:invalid_target",
                CommandProfileService.Context.TARGETED_ACTOR,
                "tellraw @s \"hello\"",
                Set.of(),
                1,
                actor);
        assertFalse(invalidTargeted.successful());

        ActionResult<CommandProfileService.CommandProfile> actorDraft = profiles.createDraft(
                "custom:actor",
                CommandProfileService.Context.ACTOR,
                "time set {value}",
                Set.of("value"),
                1,
                actor);
        assertTrue(actorDraft.successful());
        ActionResult<CommandProfileService.CommandProfile> actorPublication =
                profiles.publish(actorDraft.value().id(), actorDraft.value().revision(), actor);
        assertTrue(actorPublication.successful());
        assertTrue(actorPublication.value().enabled());
        ActionResult<CommandProfileService.RenderedCommand> rendered = profiles.renderPublished(
                actorPublication.value().id(),
                actorPublication.value().revision(),
                CommandProfileService.Context.ACTOR,
                Map.of("value", "day"),
                1);
        assertTrue(rendered.successful());
        assertEquals("time set day", rendered.value().command());

        ActionResult<CommandProfileService.CommandProfile> serverDraft = profiles.createDraft(
                "custom:server",
                CommandProfileService.Context.SERVER,
                "weather clear",
                Set.of(),
                1,
                actor);
        assertTrue(serverDraft.successful());
        ActionResult<CommandProfileService.CommandProfile> serverPublication =
                profiles.publish(serverDraft.value().id(), serverDraft.value().revision(), actor);
        assertTrue(serverPublication.successful());
        assertFalse(serverPublication.value().enabled());

        profiles.setReferenceCheck(id -> id.equals(actorPublication.value().id()));
        assertEquals(
                ActionResult.ReasonCode.POLICY_DENIED,
                profiles.delete(actorPublication.value().id(), actorPublication.value().revision()).reason());

        profiles.flush();
        CommandProfileService restored = new CommandProfileService();
        assertEquals(
                com.enviouse.sef.storage.repository.StorageRepository.RepositoryState.READY,
                restored.load(temporaryDirectory).state());
        assertEquals(
                serverPublication.value().revision(),
                restored.find(serverPublication.value().id()).orElseThrow().revision());
    }

    @Test
    void bundleQueueResumesPartialTargetStepsWithoutRepeatingSuccesses() {
        BundleService bundles = bundleService();
        bundles.load(temporaryDirectory);
        UUID issuer = UUID.randomUUID();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        BundleCompiler.BundleDefinition draft =
                bundles.createDraft("custom:cohort", issuer).value();
        BundleCompiler.BundleStep step = new BundleCompiler.BundleStep(
                "apply",
                BundleCompiler.StepKind.SEF_ACTION,
                "sef:test",
                BundleCompiler.TargetBinding.SELECTED_PLAYER,
                BundleCompiler.FailureBehavior.STOP,
                Duration.ZERO,
                Map.of());
        BundleCompiler.BundleDefinition withStep =
                bundles.addStep(draft.id(), draft.revision(), step).value();
        BundleCompiler.BundleDefinition expanded = new BundleCompiler.BundleDefinition(
                withStep.schemaVersion(),
                withStep.id(),
                withStep.revision(),
                withStep.state(),
                withStep.enabled(),
                withStep.additionalPermissionId(),
                withStep.sourceTypes(),
                withStep.authorizationMode(),
                withStep.executionMode(),
                2,
                1,
                withStep.maximumDuration(),
                withStep.confirmationRequired(),
                withStep.auditClass(),
                withStep.steps());
        BundleCompiler.BundleDefinition saved =
                bundles.saveDraft(expanded, withStep.revision()).value();
        BundleCompiler.BundleDefinition publication =
                bundles.publish(saved.id(), saved.revision()).value();
        BundleService.RuntimeJob job = bundles.enqueue(
                publication.id(),
                publication.revision(),
                issuer,
                List.of(first, second),
                Instant.EPOCH).value();

        List<UUID> executed = new ArrayList<>();
        BundleService.Revalidator revalidator =
                (runtime, definition, current, targets) -> ActionResult.success(null);
        BundleService.StepExecutor executor = new BundleService.StepExecutor() {
            @Override
            public ActionResult<Void> execute(
                    BundleService.RuntimeJob runtime,
                    BundleCompiler.BundleDefinition definition,
                    BundleCompiler.BundleStep current,
                    UUID target
            ) {
                executed.add(target);
                return ActionResult.success(null);
            }

            @Override
            public ActionResult<Void> compensate(
                    BundleService.RuntimeJob runtime,
                    BundleCompiler.BundleDefinition definition,
                    BundleCompiler.BundleStep current,
                    UUID target
            ) {
                return ActionResult.success(null);
            }
        };

        assertEquals(1, bundles.tick(Instant.EPOCH.plusSeconds(1), revalidator, executor).actions());
        assertEquals(1, bundles.tick(Instant.EPOCH.plusSeconds(2), revalidator, executor).actions());
        assertEquals(List.of(first, second), executed);
        BundleService.RuntimeJob complete = bundles.jobs().stream()
                .filter(candidate -> candidate.jobId().equals(job.jobId()))
                .findFirst()
                .orElseThrow();
        assertEquals(BundleCompiler.JobState.COMPLETED, complete.state());
    }

    @Test
    void sudoConsentAndLocksAreRevisionedAndPersistent() throws Exception {
        SudoPolicyRepository policies = new SudoPolicyRepository();
        policies.load(temporaryDirectory);
        UUID player = UUID.randomUUID();
        UUID staff = UUID.randomUUID();

        assertFalse(policies.decide(player, false, false).allowed());
        SudoPolicyRepository.Policy consent =
                policies.setConsent(player, true, 0).value();
        assertTrue(policies.decide(player, false, false).allowed());
        SudoPolicyRepository.Policy locked =
                policies.setLock(player, true, "investigation", staff, consent.revision()).value();
        assertFalse(policies.decide(player, false, false).allowed());
        assertTrue(policies.decide(player, false, true).allowed());
        assertEquals(
                ActionResult.ReasonCode.CONFLICT,
                policies.setConsent(player, false, consent.revision()).reason());

        policies.flush();
        SudoPolicyRepository restored = new SudoPolicyRepository();
        restored.load(temporaryDirectory);
        assertEquals(locked.revision(), restored.policy(player).revision());
        assertTrue(restored.policy(player).locked());
    }

    @Test
    void bundlesRejectEveryAdministrativeIndirectionClass() {
        assertTrue(AutomationRuntime.wrapperAction("sef:alias.run"));
        assertTrue(AutomationRuntime.wrapperAction("sef:bundle.run"));
        assertTrue(AutomationRuntime.wrapperAction("sef:profile.execute"));
        assertTrue(AutomationRuntime.wrapperAction("sef:panel.run"));
        assertTrue(AutomationRuntime.wrapperAction("sef:fake.schedule"));
        assertTrue(AutomationRuntime.wrapperAction("sef:sudo.run"));
        assertTrue(AutomationRuntime.wrapperAction("sef:run.server"));
        assertTrue(AutomationRuntime.wrapperAction("sef:silent.server"));
        assertFalse(AutomationRuntime.wrapperAction("sef:test"));
    }

    private static AliasService aliasService() {
        CapabilityManifest capabilities = capabilities();
        CommandCatalog catalog = catalog(capabilities);
        return new AliasService(
                new AliasCompiler(catalog, capabilities, Set.of(), Map.of()),
                16);
    }

    private static BundleService bundleService() {
        return new BundleService(new BundleCompiler(catalog(capabilities()), 16, 4, 16, 256));
    }

    private static CapabilityManifest capabilities() {
        CapabilityManifest capabilities = new CapabilityManifest();
        capabilities.register(new CapabilityManifest.Capability(
                "sef.test.use",
                CapabilityManifest.CapabilityType.COMMAND,
                true,
                "test",
                "test permission"));
        return capabilities;
    }

    private static CommandCatalog catalog(CapabilityManifest capabilities) {
        PanelContracts.Registry panels = new PanelContracts.Registry();
        panels.registerCommandOnly("sef:test");
        CommandCatalog catalog = new CommandCatalog(capabilities, panels);
        catalog.register(new CommandDefinition(
                "sef:test",
                "sef test",
                Set.of("test"),
                "command.test.description",
                "command.test.usage",
                "test",
                "sef.test",
                Set.of("sef.test.use"),
                CommandDefinition.AccessClass.PLAYER,
                Set.of(CommandDefinition.SourceType.PLAYER),
                CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                "sef:test",
                false,
                AuditService.AuditClass.METADATA_ONLY,
                "sef:test",
                "",
                "hud is not applicable",
                "",
                "quota is not applicable",
                CommandDefinition.ConflictPolicy.PREFER_SEF,
                true,
                true));
        catalog.seal();
        return catalog;
    }
}
