package com.enviouse.sef.identity;

import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelServices;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Predicate;

public final class IdentityArguments {
    private static final SimpleCommandExceptionType PLAYER_UNAVAILABLE =
            new SimpleCommandExceptionType(Component.literal("That player is unavailable."));

    private IdentityArguments() {
    }

    public static RequiredArgumentBuilder<CommandSourceStack, String> online(String name) {
        return Commands.argument(name, StringArgumentType.string())
                .suggests(suggestions(true));
    }

    public static RequiredArgumentBuilder<CommandSourceStack, String> online(
            String name,
            Predicate<CommandSourceStack> includeHidden
    ) {
        return Commands.argument(name, StringArgumentType.string())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                        KernelServices.identities().suggestions(
                                        context.getSource().getPlayer(),
                                        true,
                                        includeHidden.test(context.getSource()))
                                .stream()
                                .map(StringArgumentType::escapeIfRequired),
                        builder));
    }

    public static RequiredArgumentBuilder<CommandSourceStack, String> known(String name) {
        return Commands.argument(name, StringArgumentType.string())
                .suggests(suggestions(false));
    }

    public static SuggestionProvider<CommandSourceStack> suggestions(boolean onlineOnly) {
        return (context, builder) -> SharedSuggestionProvider.suggest(
                KernelServices.identities().suggestions(context.getSource().getPlayer(), onlineOnly)
                        .stream()
                        .map(StringArgumentType::escapeIfRequired),
                builder);
    }

    public static ServerPlayer getOnline(CommandContext<CommandSourceStack> context, String name)
            throws CommandSyntaxException {
        return getOnline(context, name, false);
    }

    public static ServerPlayer getOnline(
            CommandContext<CommandSourceStack> context,
            String name,
            boolean includeHidden
    ) throws CommandSyntaxException {
        String input = StringArgumentType.getString(context, name);
        ActionResult<IdentityService.Identity> identity = KernelServices.identities().resolve(
                input,
                context.getSource().getPlayer(),
                includeHidden);
        if (!identity.successful() || identity.value().playerId() == null) {
            throw PLAYER_UNAVAILABLE.create();
        }
        ServerPlayer target = context.getSource().getServer().getPlayerList().getPlayer(identity.value().playerId());
        if (target == null) {
            throw PLAYER_UNAVAILABLE.create();
        }
        return target;
    }
}
