package com.enviouse.sef.automation;

import com.enviouse.sef.gui.protocol.SefGuiServer;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.FeatureGateService;
import com.enviouse.sef.permissions.DelegatedPermissionScope;
import com.enviouse.sef.permissions.EphemeralExecutionGrant;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@GameTestHolder("sef")
@PrefixGameTestTemplate(false)
public final class SudoGameTests {
    private SudoGameTests() {
    }

    @GameTest(template = "empty")
    public static void effectRequiresRealPermissionOrOneExactDelegatedGrant(GameTestHelper helper) {
        var target = helper.makeMockServerPlayerInLevel();
        var server = helper.getLevel().getServer();
        var initiator = server.createCommandSourceStack();
        String command = "effect give " + target.getUUID()
                + " minecraft:jump_boost 2 255 true";
        AdministrativeExecutionService.DelegationProfile effect =
                new AdministrativeExecutionService.DelegationProfile(
                        "effect",
                        1L,
                        Set.of("effect"),
                        "minecraft:effect",
                        2,
                        Set.of(),
                        Set.of());
        AdministrativeExecutionService service = new AdministrativeExecutionService(
                new AdministrativeExecutionService.Settings(
                        "effect",
                        "",
                        "",
                        "",
                        "",
                        "",
                        512,
                        true,
                        true,
                        false,
                        false,
                        2,
                        15,
                        true,
                        true,
                        false,
                        false,
                        false,
                        false,
                        "effect",
                        "",
                        Map.of(effect.id(), effect)));

        helper.assertTrue(!target.hasPermissions(2), "target unexpectedly started with elevated permission");
        helper.assertTrue(
                !service.sudoRun(initiator, target, command).successful(),
                "respect mode bypassed the target permission");

        var preview = service.previewDelegated(initiator, target, command);
        helper.assertTrue(preview.successful(), "effect delegation preview was rejected");
        helper.assertTrue(
                !service.previewDelegated(
                        initiator,
                        target,
                        "effect give @a minecraft:speed 2 0 true").successful(),
                "delegated effect escaped the effective actor");
        Instant createdAt = Instant.now();
        EphemeralExecutionGrant grant = new EphemeralExecutionGrant(
                UUID.randomUUID(),
                UUID.nameUUIDFromBytes(
                        ("sef:source:" + initiator.getTextName()).getBytes(StandardCharsets.UTF_8)),
                target.getUUID(),
                1L,
                preview.value().preview().root(),
                preview.value().profile().canonicalActionId(),
                DelegatedPermissionScope.fingerprint(command),
                preview.value().commandTreeRevision(),
                preview.value().profile().id(),
                preview.value().profile().revision(),
                preview.value().profile().maximumTemporaryVanillaPermissionLevel(),
                preview.value().profile().temporarySefPermissionIds(),
                preview.value().profile().approvedAdapterCapabilities(),
                1L,
                1L,
                1L,
                1L,
                createdAt,
                createdAt.plusSeconds(15),
                UUID.randomUUID(),
                preview.value().preview().correlationId());

        helper.assertTrue(
                service.sudoRun(initiator, target, command, grant).successful(),
                "exact delegated effect command failed");
        helper.assertTrue(!target.getActiveEffects().isEmpty(), "delegated effect side effect is missing");
        helper.assertTrue(grant.used(), "delegated grant was not consumed");
        helper.assertTrue(!DelegatedPermissionScope.active(), "delegated scope leaked after dispatch");
        helper.assertTrue(!target.hasPermissions(2), "target retained temporary vanilla permission");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hardDeniedIndirectionHasNoDelegationProfile(GameTestHelper helper) {
        var target = helper.makeMockServerPlayerInLevel();
        var server = helper.getLevel().getServer();
        AdministrativeExecutionService service =
                new AdministrativeExecutionService(AdministrativeExecutionService.Settings.defaults());
        service.configure(new AdministrativeExecutionService.Settings(
                "execute",
                "",
                "",
                "",
                "",
                "",
                512,
                true,
                true,
                false,
                false,
                2,
                15,
                true,
                true,
                false,
                false,
                false,
                false,
                "execute",
                "",
                Map.of("effect", new AdministrativeExecutionService.DelegationProfile(
                        "effect",
                        1L,
                        Set.of("execute"),
                        "minecraft:execute",
                        2,
                        Set.of(),
                        Set.of()))));

        var preview = service.previewDelegated(
                server.createCommandSourceStack(),
                target,
                "execute as @s run effect clear @s");
        helper.assertTrue(!preview.successful(), "hard denied execute root was admitted");
        helper.assertTrue(!DelegatedPermissionScope.active(), "delegated preview scope leaked");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void delegatedSuggestionsUseTargetContextAndProfile(GameTestHelper helper) {
        var target = helper.makeMockServerPlayerInLevel();
        var initiator = helper.getLevel().getServer().createCommandSourceStack();
        AdministrativeExecutionService.DelegationProfile effect =
                new AdministrativeExecutionService.DelegationProfile(
                        "effect",
                        1L,
                        Set.of("effect"),
                        "minecraft:effect",
                        2,
                        Set.of(),
                        Set.of());
        AdministrativeExecutionService service = new AdministrativeExecutionService(
                new AdministrativeExecutionService.Settings(
                        "effect",
                        "",
                        "",
                        "",
                        "",
                        "",
                        512,
                        true,
                        true,
                        false,
                        false,
                        2,
                        15,
                        true,
                        true,
                        false,
                        false,
                        false,
                        false,
                        "effect",
                        "",
                        Map.of(effect.id(), effect)));

        var strictRoots = service.suggest(initiator, target, "eff", false).join();
        helper.assertTrue(
                strictRoots.getList().stream().noneMatch(value -> value.getText().equals("effect")),
                "respect suggestions exposed a command denied to the target");

        var delegatedRoots = service.suggest(initiator, target, "eff", true).join();
        helper.assertTrue(
                delegatedRoots.getList().stream().anyMatch(value -> value.getText().equals("effect")),
                "delegated suggestions did not expose the admitted profile root");

        var deniedRoots = service.suggest(initiator, target, "gam", true).join();
        helper.assertTrue(
                deniedRoots.getList().isEmpty(),
                "delegated suggestions exposed a root without a delegation profile");

        var effectSuggestions = service.suggest(
                initiator,
                target,
                "effect give " + target.getUUID() + " minecraft:jum",
                true).join();
        helper.assertTrue(
                effectSuggestions.getList().stream()
                        .anyMatch(value -> value.getText().contains("jump_boost")),
                "delegated argument suggestions did not use the target dispatcher context");
        helper.assertTrue(!DelegatedPermissionScope.active(), "delegated suggestion scope leaked");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void delegatedCommandRouteExecutesWithoutPersistentPrivilege(GameTestHelper helper) {
        var target = helper.makeMockServerPlayerInLevel();
        var server = helper.getLevel().getServer();
        var administrativeExecution = KernelServices.administrativeExecution();
        var originalSettings = administrativeExecution.settings();
        var featureGates = KernelServices.featureGates();
        var originalFeatures = featureGates.snapshot();
        Map<String, Boolean> enabledFeatures = new LinkedHashMap<>(originalFeatures.features());
        enabledFeatures.put("sef.sudo", true);
        try {
            featureGates.publish(new FeatureGateService.Snapshot(
                    originalFeatures.revision() + 1L,
                    enabledFeatures,
                    originalFeatures.dimensionOverrides(),
                    originalFeatures.actionOverrides()));
            administrativeExecution.configure(new AdministrativeExecutionService.Settings(
                    originalSettings.sudoAllowedRoots(),
                    originalSettings.sudoDeniedRoots(),
                    originalSettings.serverAllowedRoots(),
                    originalSettings.serverDeniedRoots(),
                    originalSettings.actorAllowedRoots(),
                    originalSettings.actorDeniedRoots(),
                    originalSettings.maximumCommandLength(),
                    true,
                    originalSettings.delegationCompatibilityBooleanSyntax(),
                    false,
                    false,
                    2,
                    15,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    "effect",
                    originalSettings.delegationDeniedRoots(),
                    originalSettings.delegationProfiles()));
            SefGuiServer.trackPlayer(target);
            String nested = "effect give " + target.getUUID()
                    + " minecraft:jump_boost 2 255 true";

            int result = server.getCommands().getDispatcher().execute(
                    "sudo run " + target.getUUID() + " delegate " + nested,
                    server.createCommandSourceStack());

            helper.assertTrue(result > 0, "delegated sudo command route did not report success");
            helper.assertTrue(!target.getActiveEffects().isEmpty(), "delegated command route had no effect");
            helper.assertTrue(!target.hasPermissions(2), "target retained delegated vanilla permission");
            helper.assertTrue(!DelegatedPermissionScope.active(), "delegated command route leaked scope");
            helper.succeed();
        } catch (Exception exception) {
            helper.fail("delegated sudo command route failed, " + exception.getClass().getSimpleName());
        } finally {
            SefGuiServer.untrackPlayer(target);
            administrativeExecution.configure(originalSettings);
            featureGates.publish(new FeatureGateService.Snapshot(
                    originalFeatures.revision() + 2L,
                    originalFeatures.features(),
                    originalFeatures.dimensionOverrides(),
                    originalFeatures.actionOverrides()));
        }
    }
}
