package com.enviouse.sef.kernel;

import com.enviouse.sef.kernel.command.CommandDefinition;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KernelServicesCatalogTest {
    @Test
    void everyCurrentSefActionHasCatalogOwnership() {
        KernelServices.initialize();
        Set<String> requiredRoutes = Set.of(
                "sef info",
                "sef colors",
                "sef test",
                "sef reload",
                "sef commands",
                "sef conflicts",
                "sef doctor",
                "sef filter add",
                "sef filter remove",
                "sef filter list",
                "sef storage status",
                "sef storage export",
                "sef motd set",
                "sef motd reload",
                "sef motd show",
                "sef workstation craft",
                "sef workstation anvil",
                "sef workstation enchant",
                "sef workstation super_enchant",
                "sef workstation repair");

        assertTrue(KernelServices.catalog().size() >= requiredRoutes.size());
        for (String route : requiredRoutes) {
            assertTrue(KernelServices.catalog().findByRoute(route).isPresent(), route);
        }
        assertTrue(KernelServices.catalog().validate().isEmpty());
    }

    @Test
    void socialActionsUseTheirSpecificFeatureAndPermissionContracts() {
        KernelServices.initialize();
        Map<String, String> features = Map.of(
                "sef:social.message", "sef.social",
                "sef:social.message.toggle", "sef.social",
                "sef:social.reply.toggle", "sef.social",
                "sef:social.ignore", "sef.social",
                "sef:social.spy", "sef.social.spy",
                "sef:social.mail", "sef.social.mail",
                "sef:social.connection", "sef.social.connection",
                "sef:social.reminder", "sef.social.reminders",
                "sef:social.text", "sef.social.text",
                "sef:social.identity", "sef.social");

        features.forEach((actionId, featureId) -> {
            var definition = KernelServices.catalog().find(actionId).orElseThrow();
            assertEquals(featureId, definition.featureId(), actionId);
            assertFalse(definition.permissionIds().isEmpty(), actionId);
        });
    }

    @Test
    void everyCatalogFeatureHasAPublishedRuntimeGate() {
        KernelServices.initialize();
        Set<String> published = KernelServices.featureGates().snapshot().features().keySet();

        KernelServices.catalog().entries().forEach(definition ->
                assertTrue(published.contains(definition.featureId()),
                        definition.id() + " uses unpublished feature " + definition.featureId()));
    }

    @Test
    void fancyTagsAndDisguiseUseTheirEnabledModuleGates() {
        KernelServices.initialize();
        Map<String, Boolean> features = KernelServices.featureGates().snapshot().features();

        assertEquals(
                "sef.fancy_tags",
                KernelServices.catalog().find("sef:tags.doctor").orElseThrow().featureId());
        assertEquals("fancy_tags", KernelServices.moduleConfigs().moduleForFeature("sef.fancy_tags"));
        assertTrue(features.get("sef.fancy_tags"));
        assertEquals("disguise", KernelServices.moduleConfigs().moduleForFeature("sef.disguise"));
        assertTrue(features.get("sef.disguise"));
    }

    @Test
    void commandOwnedDomainAuditsDeferToTheSharedExecutor() throws IOException {
        Path projectRoot = Path.of("").toAbsolutePath();
        while (projectRoot != null
                && !Files.isRegularFile(projectRoot.resolve("settings.gradle"))
                && !Files.isRegularFile(projectRoot.resolve("settings.gradle.kts"))) {
            projectRoot = projectRoot.getParent();
        }
        assertTrue(projectRoot != null, "project source was not found");
        Path root = projectRoot.resolve("src/main/java/com/enviouse/sef");
        for (String relative : Set.of(
                "fancytags/FancyTagService.java",
                "disguise/DisguiseService.java",
                "control/ServerControlRepository.java",
                "config/modules/ModuleConfigService.java",
                "alts/CheckAltsCommand.java")) {
            String source = Files.readString(root.resolve(relative), StandardCharsets.UTF_8);
            assertTrue(source.contains("CommandAuditScope.active()"), relative);
        }
    }

    @Test
    void phaseSixAndSevenActionsAndShortcutsHaveCatalogOwnership() {
        KernelServices.initialize();
        Set<String> requiredActions = Set.of(
                "sef:commandspy.toggle",
                "sef:commandspy.audience",
                "sef:logging.status",
                "sef:logging.retention.run",
                "sef:moderation.ban",
                "sef:moderation.tempban",
                "sef:moderation.pardon",
                "sef:moderation.ban_ip",
                "sef:moderation.pardon_ip",
                "sef:moderation.kick",
                "sef:moderation.kick_ip",
                "sef:moderation.kick_all",
                "sef:moderation.warn",
                "sef:moderation.mute",
                "sef:moderation.freeze",
                "sef:moderation.invlock",
                "sef:moderation.disablebuilding",
                "sef:moderation.jail",
                "sef:inventory.clear",
                "sef:inventory.enderchest",
                "sef:inventory.disposal",
                "sef:inventory.condense",
                "sef:inventory.itemname",
                "sef:item.give.self",
                "sef:kit.claim",
                "sef:kit.create",
                "sef:kit.edit",
                "sef:utility.fly",
                "sef:utility.ptime",
                "sef:gamemode.creative",
                "sef:gamemode.set",
                "sef:workstation.grindstone",
                "sef:workstation.workbench");
        requiredActions.forEach(action ->
                assertTrue(KernelServices.catalog().find(action).isPresent(), action));

        Map<String, String> shortcuts = Map.ofEntries(
                Map.entry("ci", "sef:inventory.clear"),
                Map.entry("ec", "sef:inventory.enderchest"),
                Map.entry("i", "sef:item.give.self"),
                Map.entry("wb", "sef:workstation.workbench"),
                Map.entry("gm", "sef:gamemode.set"),
                Map.entry("gmc", "sef:gamemode.creative"),
                Map.entry("gms", "sef:gamemode.survival"),
                Map.entry("gmsp", "sef:gamemode.spectator"),
                Map.entry("gma", "sef:gamemode.adventure"));
        shortcuts.forEach((root, action) ->
                assertEquals(action, KernelServices.shortcuts().find(root).orElseThrow().actionId(), root));
    }

    @Test
    void inventoryCatalogRoutesAndSourcePoliciesMatchImplementations() {
        KernelServices.initialize();

        var clear = KernelServices.catalog().find("sef:inventory.clear").orElseThrow();
        assertEquals("clearinventory", clear.canonicalRoute());
        assertTrue(clear.sourceTypes().contains(CommandDefinition.SourceType.CONSOLE));

        var recipe = KernelServices.catalog().find("sef:inventory.recipe").orElseThrow();
        assertEquals("recipe", recipe.canonicalRoute());
        assertEquals(Set.of(CommandDefinition.SourceType.PLAYER), recipe.sourceTypes());
    }

    @Test
    void administrativeEnchantRoutesUseTheSharedExecutorAsTheirOnlyAuditBoundary() throws IOException {
        Path sourcePath = Path.of("").toAbsolutePath();
        for (int depth = 0; depth < 8 && sourcePath != null; depth++) {
            Path candidate = sourcePath.resolve(
                    "src/main/java/com/enviouse/sef/workstations/AdministrativeEnchantCommands.java");
            if (Files.isRegularFile(candidate)) {
                sourcePath = candidate;
                break;
            }
            sourcePath = sourcePath.getParent();
        }
        assertTrue(sourcePath != null && Files.isRegularFile(sourcePath), "project source was not found");
        String source = Files.readString(sourcePath, StandardCharsets.UTF_8);

        assertFalse(source.contains("AuditService.record"));
        assertFalse(source.contains("SecurityAuditService.record"));
        assertEquals(
                3,
                source.lines().filter(line -> line.contains("KernelCommandExecutor.execute(")).count());
    }
}
