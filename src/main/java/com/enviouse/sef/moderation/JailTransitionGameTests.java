package com.enviouse.sef.moderation;

import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.teleport.SavedLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.time.Instant;
import java.util.UUID;

@GameTestHolder("sef")
@PrefixGameTestTemplate(false)
public final class JailTransitionGameTests {
    private JailTransitionGameTests() {
    }

    @GameTest(template = "empty")
    public static void jailAndReleaseCommitOnlyAfterSuccessfulTeleports(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ModerationRepository repository = KernelServices.moderation();
        String jailName = uniqueName("success", player);
        BlockPos releaseBlock = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos jailBlock = helper.absolutePos(new BlockPos(4, 2, 4));
        helper.getLevel().setBlockAndUpdate(releaseBlock.below(), Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(jailBlock.below(), Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(releaseBlock, Blocks.AIR.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(releaseBlock.above(), Blocks.AIR.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(jailBlock, Blocks.AIR.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(jailBlock.above(), Blocks.AIR.defaultBlockState());
        SavedLocation release = location(helper, releaseBlock);
        SavedLocation jail = location(helper, jailBlock);

        try {
            repository.setJail(jailName, jail, UUID.randomUUID());
            ModerationRepository.Sentence prepared = repository.prepareSentence(
                    player.getUUID(),
                    jailName,
                    Instant.now().plusSeconds(60),
                    "game test",
                    UUID.randomUUID(),
                    release);
            helper.assertTrue(ModerationEvents.persist("game test jail prepare"), "jail intent did not persist");

            ModerationEvents.TransitionResult jailed = ModerationEvents.completePreparedJail(
                    helper.getLevel().getServer(),
                    null,
                    player,
                    prepared,
                    true);

            helper.assertTrue(jailed.successful(), "jail teleport failed, " + jailed.detail());
            helper.assertTrue(
                    repository.sentence(player.getUUID()).orElseThrow().state()
                            == ModerationRepository.SentenceState.ACTIVE,
                    "jail did not become active");
            helper.assertTrue(
                    player.distanceToSqr(jailBlock.getX() + 0.5D, jailBlock.getY(), jailBlock.getZ() + 0.5D) < 1.0D,
                    "player did not arrive at the jail");

            ModerationRepository.Sentence pending =
                    repository.prepareRelease(player.getUUID()).orElseThrow();
            helper.assertTrue(
                    ModerationEvents.persist("game test release prepare"),
                    "release intent did not persist");
            ModerationEvents.TransitionResult released = ModerationEvents.release(
                    helper.getLevel().getServer(),
                    null,
                    player,
                    pending,
                    "game test release");

            helper.assertTrue(released.successful(), "release teleport failed, " + released.detail());
            helper.assertTrue(repository.sentence(player.getUUID()).isEmpty(), "released player stayed jailed");
            helper.assertTrue(
                    player.distanceToSqr(
                            releaseBlock.getX() + 0.5D,
                            releaseBlock.getY(),
                            releaseBlock.getZ() + 0.5D) < 1.0D,
                    "player did not return to the release location");
            ModerationEvents.TransitionResult duplicate = ModerationEvents.release(
                    helper.getLevel().getServer(),
                    null,
                    player,
                    pending,
                    "duplicate release");
            helper.assertTrue(!duplicate.successful(), "duplicate release executed");
        } finally {
            repository.purgeReleased(Instant.now().plusSeconds(1));
            repository.rollbackJail(
                    player.getUUID(),
                    repository.transition(player.getUUID())
                            .map(ModerationRepository.Sentence::operationId)
                            .orElse(UUID.randomUUID()),
                    "game test cleanup");
            try {
                repository.deleteJail(jailName);
            } catch (IllegalStateException ignored) {
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void failedJailTeleportRollsBackPreparedSentence(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ModerationRepository repository = KernelServices.moderation();
        String jailName = uniqueName("failure", player);
        SavedLocation missing = new SavedLocation("sef:missing_dimension", 0, 64, 0, 0, 0);

        try {
            repository.setJail(jailName, missing, UUID.randomUUID());
            ModerationRepository.Sentence prepared = repository.prepareSentence(
                    player.getUUID(),
                    jailName,
                    null,
                    "game test",
                    UUID.randomUUID(),
                    SavedLocation.from(player));
            helper.assertTrue(ModerationEvents.persist("game test failed jail prepare"), "intent did not persist");

            ModerationEvents.TransitionResult result = ModerationEvents.completePreparedJail(
                    helper.getLevel().getServer(),
                    null,
                    player,
                    prepared,
                    true);

            helper.assertTrue(!result.successful(), "invalid jail destination succeeded");
            helper.assertTrue(repository.sentence(player.getUUID()).isEmpty(), "failed jail remained active");
        } finally {
            repository.transition(player.getUUID()).ifPresent(sentence ->
                    repository.rollbackJail(
                            player.getUUID(),
                            sentence.operationId(),
                            "game test cleanup"));
            repository.deleteJail(jailName);
        }
        helper.succeed();
    }

    private static SavedLocation location(GameTestHelper helper, BlockPos position) {
        return new SavedLocation(
                helper.getLevel().dimension().location().toString(),
                position.getX() + 0.5D,
                position.getY(),
                position.getZ() + 0.5D,
                0.0F,
                0.0F);
    }

    private static String uniqueName(String prefix, ServerPlayer player) {
        return (prefix + "_" + player.getUUID().toString().replace("-", "").substring(0, 8))
                .toLowerCase(java.util.Locale.ROOT);
    }
}
