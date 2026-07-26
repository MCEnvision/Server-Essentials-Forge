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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

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
}
