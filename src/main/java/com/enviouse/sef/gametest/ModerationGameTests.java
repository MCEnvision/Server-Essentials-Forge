package com.enviouse.sef.gametest;

import com.enviouse.sef.disablebuilding.DisableBuildingEventHandler;
import com.enviouse.sef.freeze.FreezeEventHandler;
import com.enviouse.sef.freeze.FreezeManager;
import com.enviouse.sef.invlock.InvLockEventHandler;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.moderation.ModerationRepository;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder("sef")
@PrefixGameTestTemplate(false)
public final class ModerationGameTests {
    private ModerationGameTests() {
    }

    @GameTest(template = "empty")
    public static void persistentBuildAndFreezeControlsCancelBlockBreaking(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos relative = new BlockPos(1, 2, 1);
        helper.setBlock(relative, Blocks.STONE);
        BlockPos absolute = helper.absolutePos(relative);
        UUID actor = UUID.randomUUID();
        ModerationRepository repository = KernelServices.moderation();

        try {
            repository.applyControl(
                    player.getUUID(),
                    ModerationRepository.ControlType.BUILD_LOCK,
                    null,
                    "game test",
                    actor);
            BlockEvent.BreakEvent buildEvent = new BlockEvent.BreakEvent(
                    helper.getLevel(),
                    absolute,
                    helper.getLevel().getBlockState(absolute),
                    player);
            DisableBuildingEventHandler.onBlockBreak(buildEvent);
            helper.assertTrue(buildEvent.isCanceled(), "build lock did not cancel block breaking");

            repository.removeControl(player.getUUID(), ModerationRepository.ControlType.BUILD_LOCK);
            repository.applyControl(
                    player.getUUID(),
                    ModerationRepository.ControlType.FREEZE,
                    null,
                    "game test",
                    actor);
            BlockEvent.BreakEvent freezeEvent = new BlockEvent.BreakEvent(
                    helper.getLevel(),
                    absolute,
                    helper.getLevel().getBlockState(absolute),
                    player);
            FreezeEventHandler.onBlockBreak(freezeEvent);
            helper.assertTrue(freezeEvent.isCanceled(), "freeze did not cancel block breaking");
        } finally {
            repository.removeControl(player.getUUID(), ModerationRepository.ControlType.BUILD_LOCK);
            repository.removeControl(player.getUUID(), ModerationRepository.ControlType.FREEZE);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void persistentInventoryLockCancelsItemUse(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ModerationRepository repository = KernelServices.moderation();
        try {
            repository.applyControl(
                    player.getUUID(),
                    ModerationRepository.ControlType.INVENTORY_LOCK,
                    null,
                    "game test",
                    UUID.randomUUID());
            PlayerInteractEvent.RightClickItem event =
                    new PlayerInteractEvent.RightClickItem(player, InteractionHand.MAIN_HAND);

            InvLockEventHandler.onRightClickItem(event);

            helper.assertTrue(event.isCanceled(), "inventory lock did not cancel item use");
        } finally {
            repository.removeControl(player.getUUID(), ModerationRepository.ControlType.INVENTORY_LOCK);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void persistentInventoryLockCancelsItemDrops(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ModerationRepository repository = KernelServices.moderation();
        try {
            repository.applyControl(
                    player.getUUID(),
                    ModerationRepository.ControlType.INVENTORY_LOCK,
                    null,
                    "game test",
                    UUID.randomUUID());
            ItemEntity dropped = new ItemEntity(
                    helper.getLevel(),
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    new ItemStack(Items.STONE));
            ItemTossEvent event = new ItemTossEvent(dropped, player);

            InvLockEventHandler.onItemToss(event);

            helper.assertTrue(event.isCanceled(), "inventory lock did not cancel item dropping");
        } finally {
            repository.removeControl(player.getUUID(), ModerationRepository.ControlType.INVENTORY_LOCK);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void repositoryFreezeMirrorCanBeClearedWithoutDeletingTheControl(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ModerationRepository repository = KernelServices.moderation();
        try {
            repository.applyControl(
                    player.getUUID(),
                    ModerationRepository.ControlType.FREEZE,
                    null,
                    "game test",
                    UUID.randomUUID());
            FreezeManager.tick(helper.getLevel().getServer());
            helper.assertTrue(
                    FreezeManager.getFreezeData(player.getUUID()) != null,
                    "repository freeze was not mirrored");

            FreezeManager.clearRepositoryState();

            helper.assertTrue(
                    FreezeManager.getFreezeData(player.getUUID()) == null,
                    "repository freeze mirror survived disable cleanup");
            helper.assertTrue(
                    repository.control(player.getUUID(), ModerationRepository.ControlType.FREEZE).isPresent(),
                    "disable cleanup deleted persistent moderation state");
        } finally {
            FreezeManager.clearRepositoryState();
            repository.removeControl(player.getUUID(), ModerationRepository.ControlType.FREEZE);
        }
        helper.succeed();
    }
}
