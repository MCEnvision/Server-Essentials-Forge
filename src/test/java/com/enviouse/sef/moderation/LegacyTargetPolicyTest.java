package com.enviouse.sef.moderation;

import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.TargetHierarchyService;
import com.enviouse.sef.permissions.PermissionService;
import com.mojang.authlib.GameProfile;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class LegacyTargetPolicyTest {
    @Test
    void deniesExemptTargetBeforeLegacyMutation() {
        Fixture fixture = new Fixture();
        try (MockedStatic<PermissionService> permissions = mockStatic(PermissionService.class);
             MockedStatic<KernelServices> kernel = mockStatic(KernelServices.class)) {
            configure(permissions, kernel, fixture);
            permissions.when(() -> PermissionService.has(fixture.target, fixture.exemption)).thenReturn(true);

            assertFalse(LegacyTargetPolicy.mayTarget(
                    fixture.source, fixture.target, "exempt.freeze", true));
        }
    }

    @Test
    void hierarchyBypassAllowsLegacyTargetAfterExplicitGrant() {
        Fixture fixture = new Fixture();
        when(fixture.target.hasPermissions(4)).thenReturn(true);
        try (MockedStatic<PermissionService> permissions = mockStatic(PermissionService.class);
             MockedStatic<KernelServices> kernel = mockStatic(KernelServices.class)) {
            configure(permissions, kernel, fixture);
            permissions.when(() -> PermissionService.has(fixture.actor, fixture.hierarchyBypass)).thenReturn(true);

            assertTrue(LegacyTargetPolicy.mayTarget(
                    fixture.source, fixture.target, "exempt.freeze", true));
        }
    }

    @Test
    void nullInputsFailClosed() {
        assertFalse(LegacyTargetPolicy.mayTarget(null, null, "exempt.freeze", true));
    }

    private static void configure(
            MockedStatic<PermissionService> permissions,
            MockedStatic<KernelServices> kernel,
            Fixture fixture
    ) {
        permissions.when(() -> PermissionService.isConsole(fixture.source)).thenReturn(false);
        permissions.when(() -> PermissionService.has(fixture.actor, fixture.hierarchyBypass)).thenReturn(false);
        permissions.when(() -> PermissionService.has(fixture.actor, fixture.exemptionBypass)).thenReturn(false);
        permissions.when(() -> PermissionService.has(fixture.target, fixture.exemption)).thenReturn(false);
        kernel.when(KernelServices::hierarchy).thenReturn(new TargetHierarchyService());
    }

    @SuppressWarnings("unchecked")
    private static final class Fixture {
        private final CommandSourceStack source = mock(CommandSourceStack.class);
        private final ServerPlayer actor = mock(ServerPlayer.class);
        private final ServerPlayer target = mock(ServerPlayer.class);
        private final UUID actorId = UUID.randomUUID();
        private final UUID targetId = UUID.randomUUID();
        private final PermissionNode<Boolean> hierarchyBypass =
                PermissionsHandler.phasePermission("moderation.hierarchy.bypass");
        private final PermissionNode<Boolean> exemptionBypass =
                PermissionsHandler.phasePermission("moderation.bypass.exempt");
        private final PermissionNode<Boolean> exemption =
                PermissionsHandler.phasePermission("exempt.freeze");

        private Fixture() {
            when(source.getPlayer()).thenReturn(actor);
            when(actor.getUUID()).thenReturn(actorId);
            when(target.getUUID()).thenReturn(targetId);
            when(actor.hasPermissions(4)).thenReturn(false);
            when(target.hasPermissions(4)).thenReturn(false);
            when(actor.getGameProfile()).thenReturn(new GameProfile(actorId, "actor"));
            when(target.getGameProfile()).thenReturn(new GameProfile(targetId, "target"));
        }
    }
}
