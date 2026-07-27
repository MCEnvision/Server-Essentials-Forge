package com.enviouse.sef.gui.protocol;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GuiWorkflowCommandHooksTest {
    @Test
    void replacesVanillaRequirementWithoutRemovingVanillaGiveChildren() {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        dispatcher.register(Commands.<CommandSourceStack>literal("give")
                .requires(source -> false)
                .then(Commands.<CommandSourceStack>literal("external")
                        .executes(context -> 1)));
        var original = dispatcher.getRoot()
                .getChild("give")
                .getChild("external")
                .getCommand();
        CommandSourceStack console = mock(CommandSourceStack.class);
        when(console.hasPermission(2)).thenReturn(true);

        GuiWorkflowCommandHooks.register(dispatcher);

        assertNotNull(dispatcher.getRoot().getChild("give").getCommand());
        assertNotNull(dispatcher.getRoot().getChild("give").getChild("external"));
        assertNotSame(
                original,
                dispatcher.getRoot().getChild("give").getChild("external").getCommand());
        assertTrue(dispatcher.getRoot().getChild("give").canUse(console));
    }
}
