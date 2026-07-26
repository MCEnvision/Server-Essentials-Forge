package com.enviouse.sef.invsee;

import com.enviouse.sef.config.PermissionsHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InvSeeContainerAuthorizationTest {
    @Test
    void modifyRevocationDowngradesOpenMenuBeforeMutation() throws Exception {
        try (Fixture fixture = fixture()) {
            fixture.targetInventory().setItem(0, new ItemStack(Items.DIAMOND));
            ItemStack original = fixture.targetInventory().getItem(0);

            fixture.container().clicked(18, 0, ClickType.PICKUP, fixture.viewer());

            assertSame(original, fixture.targetInventory().getItem(0));
            assertTrue(readOnly(fixture.container()));
        }
    }

    @Test
    void viewRevocationClosesOpenMenuBeforeInteraction() throws Exception {
        try (Fixture fixture = fixture()) {
            fixture.canView().set(false);

            fixture.container().clicked(18, 0, ClickType.PICKUP, fixture.viewer());

            verify(fixture.viewer()).closeContainer();
        }
    }

    private static Fixture fixture() {
        ServerPlayer viewer = mock(ServerPlayer.class);
        ServerPlayer target = mock(ServerPlayer.class);
        CommandSourceStack source = mock(CommandSourceStack.class);
        Inventory viewerInventory = new Inventory(viewer);
        Inventory targetInventory = new Inventory(target);
        AtomicBoolean canView = new AtomicBoolean(true);

        when(viewer.createCommandSourceStack()).thenReturn(source);
        when(source.getEntity()).thenReturn(viewer);
        when(source.getPlayer()).thenReturn(viewer);
        when(viewer.isAlive()).thenReturn(true);
        when(target.isAlive()).thenReturn(true);
        when(target.getInventory()).thenReturn(targetInventory);

        MockedStatic<PermissionAPI> permissions = mockStatic(PermissionAPI.class, invocation -> {
            if ("getPermission".equals(invocation.getMethod().getName())) {
                return false;
            }
            return Answers.RETURNS_DEFAULTS.answer(invocation);
        });
        permissions.when(() -> PermissionAPI.getPermission(viewer, PermissionsHandler.invSeeView))
                .thenAnswer(ignored -> canView.get());
        permissions.when(() -> PermissionAPI.getPermission(viewer, PermissionsHandler.invSeeModify))
                .thenReturn(false);

        try {
            InvSeeContainer container = new InvSeeContainer(1, viewerInventory, target, 0, true, false);
            return new Fixture(container, viewer, targetInventory, canView, permissions);
        } catch (RuntimeException | Error failure) {
            permissions.close();
            throw failure;
        }
    }

    private static boolean readOnly(InvSeeContainer container) throws Exception {
        Field field = InvSeeContainer.class.getDeclaredField("readOnly");
        field.setAccessible(true);
        return field.getBoolean(container);
    }

    private record Fixture(
            InvSeeContainer container,
            ServerPlayer viewer,
            Inventory targetInventory,
            AtomicBoolean canView,
            MockedStatic<PermissionAPI> permissions
    ) implements AutoCloseable {
        @Override
        public void close() {
            permissions.close();
        }
    }
}
