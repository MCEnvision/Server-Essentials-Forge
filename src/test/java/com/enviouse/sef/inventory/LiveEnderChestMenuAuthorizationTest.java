package com.enviouse.sef.inventory;

import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.KernelServices;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LiveEnderChestMenuAuthorizationTest {
    @Test
    void permissionRevocationClosesTheOpenMenuBeforeInteraction() {
        ServerPlayer viewer = mock(ServerPlayer.class);
        ServerPlayer target = mock(ServerPlayer.class);
        CommandSourceStack source = mock(CommandSourceStack.class);
        PlayerEnderChestContainer enderChest = mock(PlayerEnderChestContainer.class);
        AtomicBoolean permitted = new AtomicBoolean(true);

        when(viewer.getUUID()).thenReturn(UUID.randomUUID());
        when(target.getUUID()).thenReturn(UUID.randomUUID());
        when(viewer.isAlive()).thenReturn(true);
        when(target.isAlive()).thenReturn(true);
        when(viewer.createCommandSourceStack()).thenReturn(source);
        when(source.getEntity()).thenReturn(viewer);
        when(source.getPlayer()).thenReturn(viewer);
        when(target.getEnderChestInventory()).thenReturn(enderChest);
        when(enderChest.getContainerSize()).thenReturn(27);

        try (MockedStatic<PermissionAPI> permissions = mockStatic(PermissionAPI.class, invocation -> {
            if ("getPermission".equals(invocation.getMethod().getName())) {
                return false;
            }
            return Answers.RETURNS_DEFAULTS.answer(invocation);
        })) {
            permissions.when(() -> PermissionAPI.getPermission(
                            viewer,
                            PermissionsHandler.phasePermission("commands.enderchest.others")))
                    .thenAnswer(ignored -> permitted.get());
            LiveEnderChestMenu menu = new LiveEnderChestMenu(
                    1,
                    new Inventory(viewer),
                    viewer,
                    target);

            permitted.set(false);
            menu.clicked(0, 0, ClickType.PICKUP, viewer);

            verify(viewer).closeContainer();
            assertFalse(menu.stillValid(viewer));
        }
    }

    @Test
    void configurationRevisionChangeInvalidatesTheOpenMenu() {
        KernelServices.initialize();
        ServerPlayer viewer = mock(ServerPlayer.class);
        PlayerEnderChestContainer enderChest = mock(PlayerEnderChestContainer.class);
        when(viewer.getUUID()).thenReturn(UUID.randomUUID());
        when(viewer.isAlive()).thenReturn(true);
        when(viewer.getEnderChestInventory()).thenReturn(enderChest);
        when(enderChest.getContainerSize()).thenReturn(27);

        try (MockedStatic<PermissionAPI> permissions = mockStatic(PermissionAPI.class, invocation -> {
            if ("getPermission".equals(invocation.getMethod().getName())) {
                return true;
            }
            return Answers.RETURNS_DEFAULTS.answer(invocation);
        })) {
            LiveEnderChestMenu menu = new LiveEnderChestMenu(
                    1,
                    new Inventory(viewer),
                    viewer,
                    viewer);

            KernelServices.reloadConfiguration();

            assertFalse(menu.stillValid(viewer));
        }
    }
}
