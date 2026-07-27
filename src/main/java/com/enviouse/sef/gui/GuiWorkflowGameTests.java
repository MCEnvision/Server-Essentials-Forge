package com.enviouse.sef.gui;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.FeatureGateService;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder("sef")
@PrefixGameTestTemplate(false)
public final class GuiWorkflowGameTests {
    private GuiWorkflowGameTests() {
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void everyPlayerFacingActionCompilesToATypedWorkflow(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var dispatcher = server.getCommands().getDispatcher();
        List<String> failures = new ArrayList<>();
        int covered = 0;
        for (var definition : KernelServices.catalog().entries()) {
            if (!definition.playerFacing()) {
                continue;
            }
            UniversalGuiCatalog.ActionRoute route =
                    KernelServices.universalGuiCatalog().action(definition.id()).orElse(null);
            if (route == null) {
                failures.add(definition.id() + ", no GUI route");
                continue;
            }
            if (route.workflowMode() != UniversalGuiCatalog.WorkflowMode.TYPED_COMMAND) {
                covered++;
                continue;
            }
            boolean enabled = KernelServices.featureGates().decide(
                    definition.featureId(),
                    FeatureGateService.Context.server(definition.id())).enabled();
            if (!enabled) {
                covered++;
                continue;
            }
            try {
                var workflow = GuiWorkflowCompiler.compileStructure(definition, dispatcher);
                if (workflow.variants().isEmpty()) {
                    failures.add(definition.id() + ", no variants");
                } else {
                    covered++;
                }
            } catch (IllegalArgumentException exception) {
                failures.add(definition.id() + ", " + exception.getMessage());
            }
        }
        failures.forEach(failure ->
                ServerEssentialsForge.LOGGER.error("[SEF] Typed GUI workflow coverage, {}", failure));
        helper.assertTrue(
                failures.isEmpty(),
                "typed GUI workflow coverage failed, " + String.join("; ", failures.stream().limit(8).toList()));
        helper.assertTrue(
                covered == KernelServices.catalog().entries().stream()
                        .filter(definition -> definition.playerFacing())
                        .count(),
                "typed GUI workflow count does not match the player facing catalog");
        helper.succeed();
    }
}
