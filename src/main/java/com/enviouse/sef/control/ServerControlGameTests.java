package com.enviouse.sef.control;

import com.enviouse.sef.kernel.ActionResult;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@GameTestHolder("sef")
@PrefixGameTestTemplate(false)
public final class ServerControlGameTests {
    private ServerControlGameTests() {
    }

    @GameTest(template = "empty")
    public static void everyServerControlSchemaHasANativeHandler(GameTestHelper helper) {
        ServerControlRepository repository = repository();
        ServerControlExecutionService executions = new ServerControlExecutionService(repository);

        MinecraftServerControlRuntime.registerHandlers(executions);

        var diagnostic = executions.diagnostic();
        helper.assertTrue(
                diagnostic.registeredHandlers().size() == ServerControlSchemaRegistry.schemas().size(),
                "not every server control schema has a native handler");
        helper.assertTrue(
                diagnostic.unavailableIntegrations().isEmpty(),
                "server control handler registration is incomplete");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void worldPolicyAppliesValidatedGamerules(GameTestHelper helper) {
        boolean previous = helper.getLevel().getGameRules().getBoolean(GameRules.RULE_DAYLIGHT);
        try {
            ActionResult<ServerControlExecutionService.Execution> result = execute(
                    helper,
                    "world_policy",
                    Map.of(
                            "field.world", helper.getLevel().dimension().location().toString(),
                            "field.gamerules", "doDaylightCycle=false",
                            "field.drift_response", "restore"));

            helper.assertTrue(result.successful(), result.detail());
            helper.assertTrue(
                    !helper.getLevel().getGameRules().getBoolean(GameRules.RULE_DAYLIGHT),
                    "world policy did not change the selected gamerule");
        } finally {
            helper.getLevel().getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(previous, null);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void worldPolicyRejectsTheWholeBatchBeforeMutation(GameTestHelper helper) {
        boolean previous = helper.getLevel().getGameRules().getBoolean(GameRules.RULE_DAYLIGHT);
        ActionResult<ServerControlExecutionService.Execution> result = execute(
                helper,
                "world_policy",
                Map.of(
                        "field.world", helper.getLevel().dimension().location().toString(),
                        "field.gamerules", "doDaylightCycle=false,sefMissingRule=true",
                        "field.drift_response", "restore"));

        helper.assertTrue(!result.successful(), "invalid gamerule batch was accepted");
        helper.assertTrue(
                helper.getLevel().getGameRules().getBoolean(GameRules.RULE_DAYLIGHT) == previous,
                "invalid gamerule batch partially mutated the world");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void worldBorderAppliesBoundedCenterAndSize(GameTestHelper helper) {
        var border = helper.getLevel().getWorldBorder();
        double previousX = border.getCenterX();
        double previousZ = border.getCenterZ();
        double previousSize = border.getSize();
        try {
            ActionResult<ServerControlExecutionService.Execution> result = execute(
                    helper,
                    "world_border",
                    Map.of(
                            "field.world", helper.getLevel().dimension().location().toString(),
                            "field.center_x", "32.5",
                            "field.center_z", "-48.5",
                            "field.size", "512",
                            "field.transition_seconds", "0"));

            helper.assertTrue(result.successful(), result.detail());
            helper.assertTrue(border.getCenterX() == 32.5D, "world border center x did not change");
            helper.assertTrue(border.getCenterZ() == -48.5D, "world border center z did not change");
            helper.assertTrue(border.getSize() == 512.0D, "world border size did not change");
        } finally {
            border.setCenter(previousX, previousZ);
            border.setSize(previousSize);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void cleanupRemovesSelectedItemsWithoutRemovingPlayers(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        ItemEntity item = new ItemEntity(
                helper.getLevel(),
                player.getX(),
                player.getY(),
                player.getZ(),
                new ItemStack(Items.ROTTEN_FLESH));
        helper.getLevel().addFreshEntity(item);

        ActionResult<ServerControlExecutionService.Execution> result = execute(
                helper,
                "cleanup",
                Map.of(
                        "field.targets", "items",
                        "field.interval_seconds", "10",
                        "field.minimum_age_seconds", "0",
                        "field.worlds", helper.getLevel().dimension().location().toString()));

        helper.assertTrue(result.successful(), result.detail());
        helper.assertTrue(item.isRemoved(), "cleanup did not remove the selected item entity");
        helper.assertTrue(!player.isRemoved(), "cleanup removed a player");
        helper.succeed();
    }

    private static ActionResult<ServerControlExecutionService.Execution> execute(
            GameTestHelper helper,
            String feature,
            Map<String, String> metadata
    ) {
        ServerControlRepository repository = repository();
        ServerControlExecutionService executions = new ServerControlExecutionService(repository);
        MinecraftServerControlRuntime.registerHandlers(executions);
        UUID actor = UUID.randomUUID();
        var created = repository.create(
                feature,
                actor,
                null,
                feature,
                "game test",
                null,
                metadata);
        if (!created.successful()) {
            return ActionResult.failure(created.reason(), created.detail());
        }
        MinecraftServer server = helper.getLevel().getServer();
        return executions.execute(
                created.value().id(),
                actor,
                created.value().revision(),
                true,
                new ServerControlExecutionService.ExecutionContext() {
                    @Override
                    public Object server() {
                        return server;
                    }

                    @Override
                    public Object source() {
                        return server.createCommandSourceStack();
                    }
                });
    }

    private static ServerControlRepository repository() {
        try {
            Path path = Files.createTempDirectory("sef-control-gametest");
            ServerControlRepository repository = new ServerControlRepository();
            repository.load(path);
            return repository;
        } catch (IOException exception) {
            throw new IllegalStateException("server control game test storage is unavailable", exception);
        }
    }
}
