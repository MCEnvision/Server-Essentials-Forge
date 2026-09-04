package com.enviouse.sef.kernel.policy;

import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.permissions.PermissionService;
import com.mojang.authlib.GameProfile;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;

class PlayerTargetPolicyTest {
    @Test
    void consoleCanTargetOfflinePlayerWithoutPlayerPermissionContext() {
        CommandSourceStack source = mock(CommandSourceStack.class);
        GameProfile target = new GameProfile(UUID.randomUUID(), "offline-target");
        PermissionNode<Boolean> hierarchyBypass = permissionNode();
        PermissionNode<Boolean> targetExempt = permissionNode();
        PermissionNode<Boolean> exemptionBypass = permissionNode();
        TargetHierarchyService hierarchy = new TargetHierarchyService();

        try (MockedStatic<PermissionService> permissions = mockStatic(PermissionService.class);
             MockedStatic<KernelServices> kernel = mockStatic(KernelServices.class)) {
            permissions.when(() -> PermissionService.isConsole(source)).thenReturn(true);
            permissions.when(() -> PermissionService.has(target.getId(), targetExempt)).thenReturn(false);
            kernel.when(KernelServices::hierarchy).thenReturn(hierarchy);

            TargetHierarchyService.Decision decision = PlayerTargetPolicy.decideOffline(
                    source,
                    target,
                    hierarchyBypass,
                    targetExempt,
                    exemptionBypass,
                    true,
                    false);

            assertTrue(decision.allowed());
            assertTrue(decision.bypassed());
            assertEquals(Integer.MAX_VALUE, decision.actorWeight());
            assertEquals(ActionResult.ReasonCode.SUCCESS, decision.reason());
        }
    }

    @Test
    void onlineTargetPolicyHonorsExemptionAndHierarchyBypassInputs() {
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer actor = mock(ServerPlayer.class);
        ServerPlayer target = mock(ServerPlayer.class);
        UUID actorId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        when(source.getPlayer()).thenReturn(actor);
        when(actor.getUUID()).thenReturn(actorId);
        when(target.getUUID()).thenReturn(targetId);
        when(actor.hasPermissions(4)).thenReturn(false);
        when(target.hasPermissions(4)).thenReturn(false);
        when(actor.getGameProfile()).thenReturn(new GameProfile(actorId, "actor"));
        when(target.getGameProfile()).thenReturn(new GameProfile(targetId, "target"));
        PermissionNode<Boolean> hierarchyBypass = permissionNode();
        PermissionNode<Boolean> targetExempt = permissionNode();
        PermissionNode<Boolean> exemptionBypass = permissionNode();
        TargetHierarchyService hierarchy = new TargetHierarchyService();

        try (MockedStatic<PermissionService> permissions = mockStatic(PermissionService.class);
             MockedStatic<KernelServices> kernel = mockStatic(KernelServices.class)) {
            permissions.when(() -> PermissionService.isConsole(source)).thenReturn(false);
            permissions.when(() -> PermissionService.has(actor, hierarchyBypass)).thenReturn(false);
            permissions.when(() -> PermissionService.has(actor, exemptionBypass)).thenReturn(false);
            permissions.when(() -> PermissionService.has(target, targetExempt)).thenReturn(true);
            kernel.when(KernelServices::hierarchy).thenReturn(hierarchy);

            TargetHierarchyService.Decision exempt = PlayerTargetPolicy.decide(
                    source,
                    target,
                    hierarchyBypass,
                    targetExempt,
                    exemptionBypass,
                    false,
                    false);
            assertFalse(exempt.allowed());
            assertTrue(exempt.exempt());
            assertEquals(ActionResult.ReasonCode.TARGET_EXEMPT, exempt.reason());

            permissions.when(() -> PermissionService.has(target, targetExempt)).thenReturn(false);
            permissions.when(() -> PermissionService.has(actor, hierarchyBypass)).thenReturn(true);

            TargetHierarchyService.Decision bypass = PlayerTargetPolicy.decide(
                    source,
                    target,
                    hierarchyBypass,
                    targetExempt,
                    exemptionBypass,
                    false,
                    false);
            assertTrue(bypass.allowed());
            assertTrue(bypass.bypassed());
            assertEquals(ActionResult.ReasonCode.SUCCESS, bypass.reason());
        }
    }

    @SuppressWarnings("unchecked")
    private static PermissionNode<Boolean> permissionNode() {
        return (PermissionNode<Boolean>) mock(PermissionNode.class);
    }
}
