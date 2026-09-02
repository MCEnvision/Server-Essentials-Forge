package com.enviouse.sef.player;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("sef")
@PrefixGameTestTemplate(false)
public final class PlayerUtilityGameTests {
    private PlayerUtilityGameTests() {
    }

    @GameTest(template = "empty")
    public static void feedRestoresHungerWithoutSaturationOrHealing(GameTestHelper helper) {
        ServerPlayer target = helper.makeMockServerPlayerInLevel();
        target.setHealth(5.0F);
        target.getFoodData().setFoodLevel(4);
        target.getFoodData().setSaturation(4.0F);
        float healthBefore = target.getHealth();

        try {
            int result = helper.getLevel().getServer().getCommands().getDispatcher().execute(
                    "feed " + target.getUUID(),
                    helper.getLevel().getServer().createCommandSourceStack());

            helper.assertTrue(result > 0, "feed command did not report success");
            helper.assertValueEqual(target.getFoodData().getFoodLevel(), 20, "feed did not fill hunger");
            helper.assertTrue(
                    Float.compare(target.getFoodData().getSaturationLevel(), 0.0F) == 0,
                    "feed granted saturation");
            helper.assertTrue(
                    Float.compare(target.getHealth(), healthBefore) == 0,
                    "feed changed health");
            helper.succeed();
        } catch (CommandSyntaxException exception) {
            helper.fail("feed command failed through the live dispatcher");
        }
    }
}
