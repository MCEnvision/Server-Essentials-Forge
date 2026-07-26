package com.enviouse.sef.gametest;

import com.enviouse.sef.storage.repository.LocationHistoryRepository;
import com.enviouse.sef.teleport.SafeTeleportService;
import com.enviouse.sef.teleport.SavedLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("sef")
@PrefixGameTestTemplate(false)
public final class TeleportGameTests {
    private TeleportGameTests() {
    }

    @GameTest(template = "empty")
    public static void safeLoadedDestinationIsAccepted(GameTestHelper helper) {
        BlockPos feet = new BlockPos(1, 3, 1);
        helper.setBlock(feet.below(), Blocks.STONE);
        helper.setBlock(feet, Blocks.AIR);
        helper.setBlock(feet.above(), Blocks.AIR);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos absolute = helper.absolutePos(feet);
        SafeTeleportService service = new SafeTeleportService(new LocationHistoryRepository(10));
        SafeTeleportService.Validation result = service.validate(
                helper.getLevel().getServer(),
                player,
                player,
                new SavedLocation(
                        helper.getLevel().dimension().location().toString(),
                        absolute.getX() + 0.5D,
                        absolute.getY(),
                        absolute.getZ() + 0.5D,
                        0,
                        0),
                policy());

        helper.assertTrue(result.successful(), "safe destination was rejected");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hazardousSupportIsRejected(GameTestHelper helper) {
        BlockPos feet = new BlockPos(1, 3, 1);
        helper.setBlock(feet.below(), Blocks.MAGMA_BLOCK);
        helper.setBlock(feet, Blocks.AIR);
        helper.setBlock(feet.above(), Blocks.AIR);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos absolute = helper.absolutePos(feet);
        SafeTeleportService service = new SafeTeleportService(new LocationHistoryRepository(10));
        SafeTeleportService.Validation result = service.validate(
                helper.getLevel().getServer(),
                player,
                player,
                new SavedLocation(
                        helper.getLevel().dimension().location().toString(),
                        absolute.getX() + 0.5D,
                        absolute.getY(),
                        absolute.getZ() + 0.5D,
                        0,
                        0),
                policy());

        helper.assertTrue(!result.successful(), "hazardous destination was accepted");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void missingDimensionFailsWithoutGeneratingChunks(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        SafeTeleportService service = new SafeTeleportService(new LocationHistoryRepository(10));
        SafeTeleportService.Validation result = service.validate(
                helper.getLevel().getServer(),
                player,
                player,
                new SavedLocation("example:missing_dimension", 0, 64, 0, 0, 0),
                policy());

        helper.assertTrue(
                result.code() == SafeTeleportService.ResultCode.DIMENSION_MISSING,
                "missing dimension did not return its stable result code");
        helper.succeed();
    }

    private static SafeTeleportService.Policy policy() {
        return new SafeTeleportService.Policy(0, 16, 1, false, false, true, false, 0);
    }
}
