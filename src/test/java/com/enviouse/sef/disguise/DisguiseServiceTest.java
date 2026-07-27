package com.enviouse.sef.disguise;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DisguiseServiceTest {
    @TempDir
    Path directory;

    @Test
    void admitsCuratedAbilitiesAndCommitsCooldownAfterSuccess() {
        DisguiseService service = new DisguiseService(settings(true, true, true));
        service.load(directory);
        UUID player = UUID.randomUUID();
        var disguise = service.setMob(player, "blaze", player, true, true, null);
        assertTrue(disguise.successful());
        assertEquals("minecraft:blaze", disguise.value().reference());

        Instant now = Instant.now();
        var admission = service.admitAbility(player, DisguiseService.AbilitySlot.PRIMARY, now);
        assertTrue(admission.successful());
        assertEquals("blaze_fireball", admission.value().ability().id());
        service.commitAbility(admission.value());
        assertFalse(service.admitAbility(
                player,
                DisguiseService.AbilitySlot.PRIMARY,
                now.plusSeconds(1)).successful());
    }

    @Test
    void mobWithoutAbilitiesReportsUnavailableSlotInsteadOfGlobalDisable() {
        DisguiseService service = new DisguiseService(settings(true, true, true));
        service.load(directory);
        UUID player = UUID.randomUUID();
        assertTrue(service.setMob(
                player,
                "minecraft:enderman",
                player,
                false,
                false,
                null).successful());

        var result = service.admitAbility(
                player,
                DisguiseService.AbilitySlot.PRIMARY,
                Instant.now());

        assertFalse(result.successful());
        assertEquals("this disguise has no primary ability", result.detail());
    }

    @Test
    void projectionHonorsVanishAndVanillaSelfViewLimits() {
        DisguiseService service = new DisguiseService(settings(true, false, false));
        service.load(directory);
        UUID subject = UUID.randomUUID();
        UUID viewer = UUID.randomUUID();
        assertTrue(service.setMob(subject, "minecraft:cow", subject, false, false, null).successful());

        assertTrue(service.projection(viewer, subject, false, true).isPresent());
        assertEquals(
                DisguiseService.ProjectionMode.ENHANCED,
                service.projection(viewer, subject, false, true).orElseThrow().mode());
        assertEquals(
                DisguiseService.ProjectionMode.VANILLA_PROXY,
                service.projection(viewer, subject, false, false).orElseThrow().mode());
        assertEquals(
                DisguiseService.ProjectionMode.TEXT_FALLBACK,
                service.projection(subject, subject, false, false).orElseThrow().mode());
        assertTrue(service.projection(viewer, subject, true, true).isEmpty());
    }

    @Test
    void staffOnlyProjectionFailsClosedForOrdinaryViewers() {
        DisguiseService service = new DisguiseService(settings(true, false, false));
        service.load(directory);
        UUID subject = UUID.randomUUID();
        UUID viewer = UUID.randomUUID();
        DisguiseService.DisguisePreset preset = new DisguiseService.DisguisePreset(
                "staff_cow",
                "Staff cow",
                DisguiseService.DisguiseKind.MOB,
                "minecraft:cow",
                null,
                DisguiseService.DisplayLabelMode.DISGUISE_TYPE,
                DisguiseService.EquipmentPolicy.HIDE_EQUIPMENT,
                DisguiseService.HitboxPolicy.PLAYER,
                DisguiseService.ViewerPolicy.STAFF_ONLY,
                false,
                false,
                DisguiseService.PersistencePolicy.defaults(),
                true,
                1L,
                subject,
                Instant.now());
        assertTrue(service.savePreset(preset, 0L, subject).successful());
        assertTrue(service.setPreset(subject, "staff_cow", subject, null).successful());

        assertTrue(service.projection(viewer, subject, false, true, false).isEmpty());
        assertTrue(service.projection(viewer, subject, false, true, true).isPresent());
    }

    @Test
    void persistencePolicyAndProxyIdsAreRevisionScoped() throws Exception {
        DisguiseService service = new DisguiseService(settings(true, false, false));
        service.load(directory);
        UUID subject = UUID.randomUUID();
        service.setMob(subject, "minecraft:pig", subject, false, false, null);
        service.flush();

        DisguiseService reloaded = new DisguiseService(settings(true, false, false));
        reloaded.load(directory);
        assertTrue(reloaded.active(subject).isEmpty());

        ProxyEntityIdAllocator allocator = new ProxyEntityIdAllocator();
        UUID observer = UUID.randomUUID();
        var first = allocator.allocate(observer, subject, 1L);
        assertEquals(first, allocator.resolve(observer, first.proxyEntityId()).orElseThrow());
        var second = allocator.allocate(observer, subject, 2L);
        assertFalse(first.proxyEntityId() == second.proxyEntityId());
        assertTrue(allocator.resolve(observer, first.proxyEntityId()).isEmpty());
        allocator.releaseSubject(subject);
        assertEquals(0, allocator.size());
    }

    @Test
    void trustedProfileCacheExpiresAndUntrustedProfilesAreRejected() {
        DisguiseService service = new DisguiseService(settings(true, false, false));
        service.load(directory);
        UUID subject = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        DisguiseService.ProfileSnapshot untrusted = new DisguiseService.ProfileSnapshot(
                profileId,
                "profile",
                "",
                "",
                false,
                Instant.now(),
                Instant.now().plusSeconds(60));
        assertFalse(service.setPlayer(subject, untrusted, subject, null).successful());

        Instant now = Instant.now();
        DisguiseService.ProfileSnapshot trusted = new DisguiseService.ProfileSnapshot(
                profileId,
                "Trusted",
                "texture",
                "signature",
                true,
                now,
                now.plusSeconds(60));
        assertTrue(service.setPlayer(subject, trusted, subject, null).successful());
        assertEquals(profileId, service.profile("trusted").orElseThrow().profileId());

        DisguiseService.ProfileSnapshot expired = new DisguiseService.ProfileSnapshot(
                UUID.randomUUID(),
                "Expired",
                "texture",
                "signature",
                true,
                now.minusSeconds(120),
                now.minusSeconds(60));
        service.cacheProfile(expired);
        assertTrue(service.profile("expired").isEmpty());
    }

    @Test
    void builtInTraitProfilesAreCuratedAndRejectUnsupportedTraitRequests() {
        DisguiseService service = new DisguiseService(settings(true, true, false));
        service.load(directory);
        var adapters = service.supportedMobs();
        assertTrue(adapters.stream()
                .filter(adapter -> adapter.entityType().equals("minecraft:dolphin"))
                .findFirst()
                .orElseThrow()
                .traits()
                .containsAll(java.util.Set.of(
                        DisguiseService.Trait.WATER_BREATHING,
                        DisguiseService.Trait.SWIM_SPEED)));
        assertTrue(adapters.stream()
                .filter(adapter -> adapter.entityType().equals("minecraft:spider"))
                .findFirst()
                .orElseThrow()
                .traits()
                .contains(DisguiseService.Trait.CLIMBING));
        assertFalse(service.setMob(
                UUID.randomUUID(),
                "minecraft:cow",
                UUID.randomUUID(),
                true,
                false,
                null).successful());
    }

    @Test
    void registeredModdedLivingEntityCanUseNamespacedDisguiseId() {
        DisguiseService service = new DisguiseService(settings(true, false, false));
        service.load(directory);

        assertTrue(service.registerEnhancedMobAdapter("example:yak", "Yak", false));
        var result = service.setMob(
                UUID.randomUUID(),
                "example:yak",
                UUID.randomUUID(),
                false,
                false,
                null);

        assertTrue(result.successful());
        assertEquals("example:yak", result.value().reference());
        assertFalse(service.supportedMobs().stream()
                .filter(adapter -> adapter.entityType().equals("example:yak"))
                .findFirst()
                .orElseThrow()
                .vanillaProxySupported());
    }

    @Test
    void optionRevisionsAndPersistencePoliciesSurviveConfiguredLifecycle() throws Exception {
        DisguiseService service = new DisguiseService(settings(
                true, true, false, false, false));
        service.load(directory);
        UUID subject = UUID.randomUUID();
        var created = service.setMob(
                subject,
                "minecraft:chicken",
                subject,
                true,
                false,
                null);
        assertTrue(created.successful());
        var updated = service.updateOptions(
                subject,
                DisguiseService.DisplayLabelMode.DISGUISE_TYPE,
                DisguiseService.EquipmentPolicy.HIDE_EQUIPMENT,
                null,
                new DisguiseService.PersistencePolicy(true, true, true),
                subject,
                created.value().revision());
        assertTrue(updated.successful());
        assertTrue(updated.value().revision() > created.value().revision());
        assertFalse(service.updateOptions(
                subject,
                null,
                DisguiseService.EquipmentPolicy.SHOW_REAL_EQUIPMENT,
                null,
                null,
                subject,
                created.value().revision()).successful());

        service.onDeath(subject);
        service.onLogout(subject);
        assertTrue(service.active(subject).isPresent());
        service.flush();

        DisguiseService reloaded = new DisguiseService(settings(
                true, true, false, false, false));
        reloaded.load(directory);
        assertEquals(
                DisguiseService.EquipmentPolicy.HIDE_EQUIPMENT,
                reloaded.active(subject).orElseThrow().equipmentPolicy());
    }

    @Test
    void presetLookupDeleteAndActiveReferenceConflictsAreRevisionSafe() {
        DisguiseService service = new DisguiseService(settings(true, false, false));
        service.load(directory);
        UUID actor = UUID.randomUUID();
        DisguiseService.DisguisePreset requested = new DisguiseService.DisguisePreset(
                "cow",
                "Cow",
                DisguiseService.DisguiseKind.MOB,
                "minecraft:cow",
                null,
                DisguiseService.DisplayLabelMode.DISGUISE_TYPE,
                DisguiseService.EquipmentPolicy.PRESET_COSMETIC,
                DisguiseService.HitboxPolicy.PLAYER,
                DisguiseService.ViewerPolicy.EVERYONE,
                false,
                false,
                DisguiseService.PersistencePolicy.defaults(),
                true,
                1L,
                actor,
                Instant.now());
        var saved = service.savePreset(requested, 0L, actor);
        assertTrue(saved.successful());
        assertNotNull(service.preset("COW").orElse(null));
        UUID subject = UUID.randomUUID();
        assertTrue(service.setPreset(subject, "cow", actor, null).successful());
        assertFalse(service.deletePreset("cow", actor, saved.value().revision()).successful());
        assertTrue(service.clear(subject, actor, DisguiseService.ClearReason.COMMAND).successful());
        assertTrue(service.deletePreset("cow", actor, saved.value().revision()).successful());
        assertTrue(service.preset("cow").isEmpty());
    }

    private static DisguiseService.Settings settings(
            boolean enabled,
            boolean traits,
            boolean abilities
    ) {
        return settings(enabled, traits, abilities, true, true);
    }

    private static DisguiseService.Settings settings(
            boolean enabled,
            boolean traits,
            boolean abilities,
            boolean clearOnLogout,
            boolean clearOnDeath
    ) {
        return new DisguiseService.Settings(
                enabled,
                traits,
                abilities,
                clearOnLogout,
                clearOnDeath,
                64,
                Duration.ofSeconds(10),
                5.0D,
                5);
    }
}
