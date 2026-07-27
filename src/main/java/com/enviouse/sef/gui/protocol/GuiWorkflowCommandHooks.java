package com.enviouse.sef.gui.protocol;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class GuiWorkflowCommandHooks {
    private static final String GIVE_ACTION = "sef:item.give.others";

    private GuiWorkflowCommandHooks() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        List<CommandNode<CommandSourceStack>> vanillaChildren = detachGiveRoot(dispatcher);
        wrapGiveCommands(vanillaChildren);
        var root = Commands.<CommandSourceStack>literal("give")
                .requires(source -> source.getPlayer() == null
                        ? source.hasPermission(2)
                        : KernelCommandExecutor.canUse(source, GIVE_ACTION))
                .executes(context -> {
                    if (GuiWorkflowService.openBare(context.getSource(), GIVE_ACTION)) {
                        return 1;
                    }
                    context.getSource().sendFailure(TextFormatter.stringToFormattedText(
                            "&cUsage: /give <player> <item> [amount]"));
                    return 0;
                });
        vanillaChildren.forEach(root::then);
        dispatcher.register(root);
    }

    @SuppressWarnings("unchecked")
    private static List<CommandNode<CommandSourceStack>> detachGiveRoot(
            CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        RootCommandNode<CommandSourceStack> root = dispatcher.getRoot();
        CommandNode<CommandSourceStack> existing = root.getChild("give");
        if (existing == null) {
            return List.of();
        }
        List<CommandNode<CommandSourceStack>> children = List.copyOf(existing.getChildren());
        try {
            for (String fieldName : List.of("children", "literals", "arguments")) {
                Field field = CommandNode.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                ((Map<String, CommandNode<CommandSourceStack>>) field.get(root)).remove("give");
            }
        } catch (ReflectiveOperationException | RuntimeException exception) {
            ServerEssentialsForge.LOGGER.error(
                    "[SEF] Could not replace the vanilla give command root",
                    exception);
            throw new IllegalStateException(
                    "Could not install the permission aware give command root",
                    exception);
        }
        return children;
    }

    private static void wrapGiveCommands(List<CommandNode<CommandSourceStack>> roots) {
        try {
            Field commandField = CommandNode.class.getDeclaredField("command");
            commandField.setAccessible(true);
            for (CommandNode<CommandSourceStack> root : roots) {
                wrapGiveCommand(root, commandField);
            }
        } catch (ReflectiveOperationException | RuntimeException exception) {
            ServerEssentialsForge.LOGGER.error(
                    "[SEF] Could not install the vanilla give policy wrapper",
                    exception);
            throw new IllegalStateException(
                    "Could not install the vanilla give policy wrapper",
                    exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static void wrapGiveCommand(
            CommandNode<CommandSourceStack> node,
            Field commandField
    ) throws IllegalAccessException {
        Command<CommandSourceStack> command =
                (Command<CommandSourceStack>) commandField.get(node);
        if (command != null && !(command instanceof GiveKernelCommand)) {
            commandField.set(node, new GiveKernelCommand(command));
        }
        for (CommandNode<CommandSourceStack> child : node.getChildren()) {
            wrapGiveCommand(child, commandField);
        }
    }

    private static int count(CommandContext<CommandSourceStack> context) {
        try {
            return IntegerArgumentType.getInteger(context, "count");
        } catch (IllegalArgumentException exception) {
            return 1;
        }
    }

    private static int runVanilla(
            CommandContext<CommandSourceStack> context,
            Command<CommandSourceStack> command
    ) {
        try {
            return command.run(context);
        } catch (CommandSyntaxException exception) {
            context.getSource().sendFailure(TextFormatter.stringToFormattedText(
                    "&c" + exception.getMessage()));
            return 0;
        }
    }

    private record GiveKernelCommand(
            Command<CommandSourceStack> delegate
    ) implements Command<CommandSourceStack> {
        @Override
        public int run(CommandContext<CommandSourceStack> context)
                throws CommandSyntaxException {
            CommandSourceStack source = context.getSource();
            if (source.getPlayer() == null) {
                return delegate.run(context);
            }
            Collection<ServerPlayer> targets =
                    EntityArgument.getPlayers(context, "targets");
            ItemInput item = ItemArgument.getItem(context, "item");
            int count = count(context);
            List<UUID> targetIds = targets.stream()
                    .map(ServerPlayer::getUUID)
                    .toList();
            Map<String, String> parameters = Map.of(
                    "item", BuiltInRegistries.ITEM.getKey(item.getItem()).toString(),
                    "count", Integer.toString(count),
                    "targets", Integer.toString(targetIds.size()));
            return KernelCommandExecutor.execute(
                    source,
                    GIVE_ACTION,
                    parameters,
                    targetIds,
                    false,
                    () -> runVanilla(context, delegate));
        }
    }
}
