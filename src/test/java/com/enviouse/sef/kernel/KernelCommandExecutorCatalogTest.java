package com.enviouse.sef.kernel;

import com.enviouse.sef.kernel.command.CommandDefinition;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Catalog policy regression coverage for the command execution boundary.
 * Route registration and parser coverage do not prove authorization. Every
 * sealed action is checked against a provider that denies every permission.
 */
class KernelCommandExecutorCatalogTest {
    @Test
    void everyCatalogActionFailsClosedWhenPermissionProviderDenies() {
        KernelServices.initialize();
        CommandSourceStack source = mock(CommandSourceStack.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(source.getEntity()).thenReturn(player);
        when(player.getUUID()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        List<String> failures = new ArrayList<>();
        try (MockedStatic<PermissionAPI> permissions = denyingPermissionApi()) {
            for (CommandDefinition definition : KernelServices.catalog().entries()) {
                if (KernelCommandExecutor.canUse(source, definition.id())) {
                    failures.add(definition.id());
                }
            }
        }

        assertTrue(
                failures.isEmpty(),
                () -> "catalog actions bypassed a denying permission provider, "
                        + String.join(", ", failures.stream().limit(12).toList()));
    }

    private static MockedStatic<PermissionAPI> denyingPermissionApi() {
        return mockStatic(PermissionAPI.class, invocation -> {
            String method = invocation.getMethod().getName();
            if (method.equals("getPermission") || method.equals("getOfflinePermission")) {
                return false;
            }
            if (method.equals("getActivePermissionHandler")) {
                return "sef:test-deny";
            }
            return Answers.RETURNS_DEFAULTS.answer(invocation);
        });
    }
}
