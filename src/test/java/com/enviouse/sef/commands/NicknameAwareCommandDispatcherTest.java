package com.enviouse.sef.commands;

import com.enviouse.sef.alts.CheckAltsCommand;
import com.enviouse.sef.banned.BannedItemsCommands;
import com.enviouse.sef.banned.BannedItemsManager;
import com.enviouse.sef.clearchat.ClearChatCommand;
import com.enviouse.sef.disablebuilding.DisableBuildingCommand;
import com.enviouse.sef.freeze.FreezeCommand;
import com.enviouse.sef.invlock.InvLockCommand;
import com.enviouse.sef.invsee.InvSeeCommand;
import com.enviouse.sef.mute.MuteCommand;
import com.enviouse.sef.warn.WarnCommand;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class NicknameAwareCommandDispatcherTest {
    @Test
    void enabledAdministrativePlayerTargetsUseIdentityArguments() {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        ClearChatCommand.register(dispatcher);
        CheckAltsCommand.register(dispatcher);
        InvLockCommand.register(dispatcher);
        DisableBuildingCommand.register(dispatcher);
        FreezeCommand.register(dispatcher);
        MuteCommand.register(dispatcher);
        InvSeeCommand.register(dispatcher);
        WarnCommand.register(dispatcher);
        BannedItemsCommands.setManager(mock(BannedItemsManager.class));
        BannedItemsCommands.register(dispatcher);

        assertIdentityArgument(dispatcher, "cc", "player");
        assertIdentityArgument(dispatcher, "clearchat", "player");
        assertIdentityArgument(dispatcher, "checkalts", "player");
        assertIdentityArgument(dispatcher, "invlock", "player");
        assertIdentityArgument(dispatcher, "disablebuilding", "player");
        assertIdentityArgument(dispatcher, "db", "player");
        assertIdentityArgument(dispatcher, "freeze", "player");
        assertIdentityArgument(dispatcher, "unfreeze", "player");
        assertIdentityArgument(dispatcher, "mute", "player");
        assertIdentityArgument(dispatcher, "unmute", "player");
        assertIdentityArgument(dispatcher, "invsee", "player");
        assertIdentityArgument(dispatcher, "warn", "player");
        assertIdentityArgument(dispatcher, "banned", "bypass", "player");
        assertIdentityArgument(dispatcher, "banned", "scan", "player");
    }

    private static void assertIdentityArgument(
            CommandDispatcher<CommandSourceStack> dispatcher,
            String... path
    ) {
        CommandNode<CommandSourceStack> node = dispatcher.getRoot();
        for (String segment : path) {
            node = node.getChild(segment);
            assertNotNull(node, String.join(" ", path));
        }
        ArgumentCommandNode<CommandSourceStack, ?> argument =
                assertInstanceOf(ArgumentCommandNode.class, node);
        assertInstanceOf(StringArgumentType.class, argument.getType());
        assertNotNull(argument.getCustomSuggestions());
    }
}
