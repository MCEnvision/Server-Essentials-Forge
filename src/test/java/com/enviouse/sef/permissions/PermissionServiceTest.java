package com.enviouse.sef.permissions;

import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.ConfirmationService;
import com.enviouse.sef.kernel.policy.WarmupService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class PermissionServiceTest {
    @Test
    void providerRefreshRevokesPendingActorAuthority() {
        UUID actorId = UUID.randomUUID();
        KernelServices.warmups().start(
                actorId,
                "sef:test",
                new WarmupService.Position("minecraft:overworld", 0, 64, 0, 0, 0),
                null,
                Duration.ofSeconds(30),
                Set.of());
        KernelServices.confirmations().issue(
                new ConfirmationService.Request(
                        actorId,
                        "sef:test",
                        Map.of(),
                        List.of(),
                        "",
                        0L,
                        0L,
                        0L,
                        1L),
                Duration.ofSeconds(30));

        PermissionRefreshBridge.invalidateKernelActor(actorId);

        assertTrue(KernelServices.warmups().inspect(actorId).isEmpty());
        assertEquals(0, KernelServices.confirmations().size());
    }

    @Test
    void decisionReportsPermissionProviderAndUnevaluatedTargetPolicy() {
        ServerPlayer player = mock(ServerPlayer.class);
        ResourceLocation provider = ResourceLocation.fromNamespaceAndPath("luckperms", "provider");
        try (MockedStatic<PermissionAPI> permissions = mockStatic(PermissionAPI.class)) {
            permissions.when(() -> PermissionAPI.getPermission(player, PermissionsHandler.nickCommand))
                    .thenReturn(true);
            permissions.when(PermissionAPI::getActivePermissionHandler).thenReturn(provider);

            PermissionService.Decision decision =
                    PermissionService.decide(player, PermissionsHandler.nickCommand);

            assertTrue(decision.granted());
            assertEquals("sef.commands.nick", decision.permissionId());
            assertEquals(provider.toString(), decision.provider());
            assertEquals(PermissionService.DefaultUse.UNKNOWN, decision.defaultUse());
            assertEquals(PermissionService.Evaluation.NOT_EVALUATED, decision.hierarchyResult());
            assertEquals(PermissionService.Evaluation.NOT_EVALUATED, decision.exemptionResult());
            assertEquals(PermissionService.DenialReason.NONE, decision.denialReason());
        }
    }

    @Test
    void directLuckPermsGrantSurvivesTransientNeoForgeBridgeFailure() {
        ServerPlayer player = mock(ServerPlayer.class);
        UUID playerId = UUID.randomUUID();
        when(player.getUUID()).thenReturn(playerId);
        try (MockedStatic<PermissionAPI> permissions = mockStatic(PermissionAPI.class);
             MockedStatic<DynamicPermissionService> dynamic = mockStatic(DynamicPermissionService.class)) {
            permissions.when(() -> PermissionAPI.getPermission(player, PermissionsHandler.nickCommand))
                    .thenThrow(new IllegalStateException("capability is not initialized"));
            dynamic.when(() -> DynamicPermissionService.decision(
                            player,
                            PermissionsHandler.nickCommand.getNodeName()))
                    .thenReturn(DynamicPermissionService.Decision.GRANTED);

            PermissionService.Decision decision =
                    PermissionService.decide(player, PermissionsHandler.nickCommand);

            assertTrue(decision.granted());
            assertEquals("luckperms:direct", decision.provider());
            assertEquals(PermissionService.DenialReason.NONE, decision.denialReason());
        }
    }

    @Test
    void providerFailureWithoutDirectGrantFailsClosed() {
        ServerPlayer player = mock(ServerPlayer.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());
        try (MockedStatic<PermissionAPI> permissions = mockStatic(PermissionAPI.class);
             MockedStatic<DynamicPermissionService> dynamic = mockStatic(DynamicPermissionService.class)) {
            permissions.when(() -> PermissionAPI.getPermission(player, PermissionsHandler.nickCommand))
                    .thenThrow(new IllegalStateException("capability is not initialized"));
            dynamic.when(() -> DynamicPermissionService.decision(
                            player,
                            PermissionsHandler.nickCommand.getNodeName()))
                    .thenReturn(DynamicPermissionService.Decision.UNAVAILABLE);

            PermissionService.Decision decision =
                    PermissionService.decide(player, PermissionsHandler.nickCommand);

            assertFalse(decision.granted());
            assertEquals(PermissionService.DenialReason.PROVIDER_UNAVAILABLE, decision.denialReason());
        }
    }

    @Test
    void explicitDirectLuckPermsDenyOverridesNeoForgeGrant() {
        ServerPlayer player = mock(ServerPlayer.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());
        try (MockedStatic<PermissionAPI> permissions = mockStatic(PermissionAPI.class);
             MockedStatic<DynamicPermissionService> dynamic = mockStatic(DynamicPermissionService.class)) {
            permissions.when(() -> PermissionAPI.getPermission(player, PermissionsHandler.nickCommand))
                    .thenReturn(true);
            dynamic.when(() -> DynamicPermissionService.decision(
                            player,
                            PermissionsHandler.nickCommand.getNodeName()))
                    .thenReturn(DynamicPermissionService.Decision.DENIED);

            PermissionService.Decision decision =
                    PermissionService.decide(player, PermissionsHandler.nickCommand);

            assertFalse(decision.granted());
            assertEquals("luckperms:direct", decision.provider());
            assertEquals(PermissionService.DenialReason.PERMISSION_DENIED, decision.denialReason());
        }
    }

    @Test
    void providerOnlyCheckUsesDirectLuckPermsGrantWhenNeoForgeBridgeFails() {
        ServerPlayer player = mock(ServerPlayer.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());
        try (MockedStatic<PermissionAPI> permissions = mockStatic(PermissionAPI.class);
             MockedStatic<DynamicPermissionService> dynamic = mockStatic(DynamicPermissionService.class)) {
            permissions.when(() -> PermissionAPI.getPermission(player, PermissionsHandler.nickCommand))
                    .thenThrow(new IllegalStateException("capability is not initialized"));
            dynamic.when(() -> DynamicPermissionService.decision(
                            player,
                            PermissionsHandler.nickCommand.getNodeName()))
                    .thenReturn(DynamicPermissionService.Decision.GRANTED);

            assertTrue(PermissionService.hasProviderOnly(player, PermissionsHandler.nickCommand));
        }
    }

    @Test
    void providerOnlyCheckHonorsDirectLuckPermsDenyOverNeoForgeGrant() {
        ServerPlayer player = mock(ServerPlayer.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());
        try (MockedStatic<PermissionAPI> permissions = mockStatic(PermissionAPI.class);
             MockedStatic<DynamicPermissionService> dynamic = mockStatic(DynamicPermissionService.class)) {
            permissions.when(() -> PermissionAPI.getPermission(player, PermissionsHandler.nickCommand))
                    .thenReturn(true);
            dynamic.when(() -> DynamicPermissionService.decision(
                            player,
                            PermissionsHandler.nickCommand.getNodeName()))
                    .thenReturn(DynamicPermissionService.Decision.DENIED);

            assertFalse(PermissionService.hasProviderOnly(player, PermissionsHandler.nickCommand));
        }
    }

    @Test
    void providerOnlyCheckFallsBackToNeoForgeWhenDirectProviderIsUnavailable() {
        ServerPlayer player = mock(ServerPlayer.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());
        try (MockedStatic<PermissionAPI> permissions = mockStatic(PermissionAPI.class);
             MockedStatic<DynamicPermissionService> dynamic = mockStatic(DynamicPermissionService.class)) {
            permissions.when(() -> PermissionAPI.getPermission(player, PermissionsHandler.nickCommand))
                    .thenReturn(true);
            dynamic.when(() -> DynamicPermissionService.decision(
                            player,
                            PermissionsHandler.nickCommand.getNodeName()))
                    .thenReturn(DynamicPermissionService.Decision.UNAVAILABLE);

            assertTrue(PermissionService.hasProviderOnly(player, PermissionsHandler.nickCommand));
        }
    }

    @Test
    void providerOnlyCheckFailsClosedWhenBothProvidersAreUnavailable() {
        ServerPlayer player = mock(ServerPlayer.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());
        try (MockedStatic<PermissionAPI> permissions = mockStatic(PermissionAPI.class);
             MockedStatic<DynamicPermissionService> dynamic = mockStatic(DynamicPermissionService.class)) {
            permissions.when(() -> PermissionAPI.getPermission(player, PermissionsHandler.nickCommand))
                    .thenThrow(new IllegalStateException("capability is not initialized"));
            dynamic.when(() -> DynamicPermissionService.decision(
                            player,
                            PermissionsHandler.nickCommand.getNodeName()))
                    .thenReturn(DynamicPermissionService.Decision.UNAVAILABLE);

            assertFalse(PermissionService.hasProviderOnly(player, PermissionsHandler.nickCommand));
        }
    }

    @Test
    void quotaPermissionResolverReturnsGrantedOfflineTiers() {
        UUID playerId = UUID.randomUUID();
        try (MockedStatic<PermissionAPI> permissions = mockStatic(PermissionAPI.class)) {
            permissions.when(() -> PermissionAPI.getOfflinePermission(
                            playerId,
                            PermissionsHandler.quotaTierNodes.get("sef.mail.500")))
                    .thenReturn(true);
            permissions.when(() -> PermissionAPI.getOfflinePermission(
                            playerId,
                            PermissionsHandler.quotaTierNodes.get("sef.definitions.256")))
                    .thenReturn(true);

            Set<String> granted = QuotaPermissionResolver.granted(playerId);

            assertTrue(granted.contains("sef.mail.500"));
            assertTrue(granted.contains("sef.definitions.256"));
            assertFalse(granted.contains("sef.mail.1000"));
        }
    }
}
