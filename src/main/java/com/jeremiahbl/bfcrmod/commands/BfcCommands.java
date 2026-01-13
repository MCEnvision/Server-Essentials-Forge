package com.jeremiahbl.bfcrmod.commands;

import java.util.Arrays;

import com.jeremiahbl.bfcrmod.BetterForgeChat;
import com.jeremiahbl.bfcrmod.TextFormatter;
import com.jeremiahbl.bfcrmod.config.ConfigHandler;
import com.jeremiahbl.bfcrmod.config.ConfigurationEventHandler;
import com.jeremiahbl.bfcrmod.config.PermissionsHandler;
import com.jeremiahbl.bfcrmod.filter.FilterDataStore;
import com.jeremiahbl.bfcrmod.filter.FilterManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.server.permission.nodes.PermissionNode;

public class BfcCommands {
	private static final Iterable<String> bfcrmodSubCommands = Arrays.asList(new String[] { 
			"info", "colors", "test", "reload"
	});
	
	private static FilterManager filterManager;

	protected static boolean checkPermission(CommandSourceStack c, PermissionNode<Boolean> node) {
		try {
			return PermissionsHandler.playerHasPermission(c.getPlayerOrException().getUUID(), node);
		} catch(CommandSyntaxException e) {
			// Not a player (console or rcon)
			return true;
		}
	}
	protected static boolean checkContextPermission(CommandContext<CommandSourceStack> c, PermissionNode<Boolean> node) {
		return checkPermission(c.getSource(), node);
	}
	protected static int failNoPermission(CommandContext<CommandSourceStack> ctx) {
		ctx.getSource().sendFailure(TextFormatter.stringToFormattedText(TextFormatter.COLOR_RED + "You don't have permission to run this command" + TextFormatter.RESET_ALL_FORMAT));
		return 0;
	}
	
	public static void register(CommandDispatcher<CommandSourceStack> disp) {
		disp.register(Commands.literal("bfcr").requires((c) -> {
				return checkPermission(c, PermissionsHandler.bfcrmodCommand);
			}).then(Commands.argument("mode", StringArgumentType.greedyString())
					.suggests((context, builder) -> SharedSuggestionProvider.suggest(bfcrmodSubCommands, builder))
					.executes(ctx -> modCommand(ctx))));
		if(ConfigHandler.config.enableColorsCommand.get()) {
			disp.register(Commands.literal("colors").requires((c) -> {
					return checkPermission(c, PermissionsHandler.coloredChatNode);
				}).executes(ctx -> colorCommand(ctx)));
		}

		// Register filter commands if enabled
		if(ConfigHandler.config.enableFilterSystem.get()) {
			registerFilterCommands(disp);
		}

		NickCommands.register(disp);
	}
	
	public static void initFilterManager(FilterManager manager) {
		filterManager = manager;
	}

	public static FilterManager getFilterManager() {
		return filterManager;
	}

	private static void registerFilterCommands(CommandDispatcher<CommandSourceStack> disp) {
		SuggestionProvider<CommandSourceStack> caseSensitiveSuggest = (c, b) -> {
			b.suggest("yes");
			b.suggest("no");
			return b.buildFuture();
		};

		SuggestionProvider<CommandSourceStack> filterIdSuggest = (c, b) -> {
			if(filterManager != null) {
				filterManager.list().keySet().forEach(b::suggest);
			}
			return b.buildFuture();
		};

		disp.register(Commands.literal("bfcr")
			.requires(src -> src.hasPermission(2))
			.then(Commands.literal("filter")
				// /bfcr filter add <id> <caseSensitive yes/no> <wordToFilter> [replacement]
				.then(Commands.literal("add")
					.then(Commands.argument("id", StringArgumentType.word())
						.then(Commands.argument("caseSensitive", StringArgumentType.word()).suggests(caseSensitiveSuggest)
							.then(Commands.argument("wordToFilter", StringArgumentType.string())
								// With replacement
								.then(Commands.argument("replacement", StringArgumentType.greedyString())
									.executes(ctx -> filterAdd(ctx, false)))
								// Without replacement (just remove the word)
								.executes(ctx -> filterAdd(ctx, true))))))
				// /bfcr filter remove <id>
				.then(Commands.literal("remove")
					.then(Commands.argument("id", StringArgumentType.word())
						.suggests(filterIdSuggest)
						.executes(ctx -> filterRemove(ctx))))
				// /bfcr filter list
				.then(Commands.literal("list")
					.executes(ctx -> filterList(ctx)))));
	}

	private static int filterAdd(CommandContext<CommandSourceStack> ctx, boolean noReplacement) {
		if(filterManager == null) {
			ctx.getSource().sendFailure(TextFormatter.stringToFormattedText("&cFilter system not initialized"));
			return 0;
		}

		String id = StringArgumentType.getString(ctx, "id");
		String caseSensitiveStr = StringArgumentType.getString(ctx, "caseSensitive").toLowerCase();
		String wordToFilter = StringArgumentType.getString(ctx, "wordToFilter");
		String replacement = noReplacement ? "" : StringArgumentType.getString(ctx, "replacement");

		boolean caseSensitive = caseSensitiveStr.equals("yes") || caseSensitiveStr.equals("true");

		filterManager.addFilter(id, wordToFilter, replacement, caseSensitive);

		ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText("&aAdded filter: &e" + id), false);
		ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText("&7  Word: &f" + wordToFilter), false);
		ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText("&7  Replacement: &f" + (replacement.isEmpty() ? "(removed)" : replacement)), false);
		ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText("&7  Case sensitive: &f" + caseSensitive), false);
		return 1;
	}

	private static int filterRemove(CommandContext<CommandSourceStack> ctx) {
		if(filterManager == null) {
			ctx.getSource().sendFailure(TextFormatter.stringToFormattedText("&cFilter system not initialized"));
			return 0;
		}

		String id = StringArgumentType.getString(ctx, "id");
		boolean removed = filterManager.removeFilter(id);

		if(removed) {
			ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText("&aRemoved filter: &e" + id), false);
		} else {
			ctx.getSource().sendFailure(TextFormatter.stringToFormattedText("&cFilter not found: " + id));
		}
		return removed ? 1 : 0;
	}

	private static int filterList(CommandContext<CommandSourceStack> ctx) {
		if(filterManager == null) {
			ctx.getSource().sendFailure(TextFormatter.stringToFormattedText("&cFilter system not initialized"));
			return 0;
		}

		ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText("&6━━━━━━━━ Word Filters ━━━━━━━━"), false);

		var filters = filterManager.list();
		if(filters.isEmpty()) {
			ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText("&7No filters configured"), false);
		} else {
			filters.forEach((id, rec) -> {
				String caseSensitive = rec.caseSensitive() ? "&a[case-sensitive]" : "&e[case-insensitive]";
				String replacement = rec.replacement().isEmpty() ? "&c(removed)" : "&f" + rec.replacement();
				ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText(
					"&e" + id + " " + caseSensitive + "\n" +
					"  &7Word: &f" + rec.wordToFilter() + "\n" +
					"  &7Replacement: " + replacement
				), false);
			});
		}

		ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText("&6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"), false);
		return 1;
	}

	public static int modCommand(CommandContext<CommandSourceStack> ctx) {
		String arg = StringArgumentType.getString(ctx, "mode");
            switch (arg) {
                case "colors" -> {
                    if(checkContextPermission(ctx, PermissionsHandler.bfcrmodCommandColorsSubCommand))
                        return colorCommand(ctx);
                    else return failNoPermission(ctx);
                }
                case "info" -> {
                    if(checkContextPermission(ctx, PermissionsHandler.bfcrmodCommandInfoSubCommand)) {
                        boolean hasMetaProv = BetterForgeChat.instance.metadataProvider != null;
                        boolean hasNickProv = BetterForgeChat.instance.nicknameProvider != null;
                        String metaProvName = hasMetaProv ? BetterForgeChat.instance.metadataProvider.getProviderName() : "";
                        String nickProvName = hasNickProv ? BetterForgeChat.instance.nicknameProvider.getProviderName() : "";
                        if(hasMetaProv) metaProvName = " (via " + metaProvName + ")";
                        if(hasNickProv) nickProvName = " (via " + nickProvName + ")";
						String finalMetaProvName = metaProvName;
						String finalNickProvName = nickProvName;
						ctx.getSource().sendSuccess(() ->TextFormatter.stringToFormattedText(
                                BetterForgeChat.CHAT_ID_STR + "\n&eMod ID: &d" + BetterForgeChat.MODID + "    &r&eMod version: &d" + BetterForgeChat.VERSION + " (forge)&r\n\n"
                                        + (hasMetaProv ? "&a&lWITH" : "&c&lWITHOUT") + "&r&e metadata integration" + finalMetaProvName + "&r\n"
                                        + (hasNickProv ? "&a&lWITH" : "&c&lWITHOUT") + "&r&e nickname integration" + finalNickProvName + "&r\n"), false);
                        return 1;
                    } else return failNoPermission(ctx);
                }
                case "test" -> {
                    ctx.getSource().sendSuccess(() ->TextFormatter.stringToFormattedText(
                            BetterForgeChat.CHAT_ID_STR
                                    + "&eColors & Styling internal debug test&r\n"
                                            + "Normal &lBold&r &nUnderline&r &oItalic&r &mStrikthrough&r &kObfuscated&r &rReset\n"
                                            + "Normal &lBold &nUnderline &oItalic &mStrikthrough &kObfuscated &rReset"), false);
                    return 1;
                }
                case "reload" -> {
                    if(checkContextPermission(ctx, PermissionsHandler.bfcrmodCommandReloadSubCommand)) {
                        ConfigHandler.reloadFromDisk();
                        ConfigurationEventHandler.reloadConfigOptions();
                        ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText("&aBetterForgeChat config reloaded."), false);
                        return 1;
                    } else return failNoPermission(ctx);
                }
                default -> {
                    return 0;
                }
            }
	}
	public static int colorCommand(CommandContext<CommandSourceStack> ctx) {
		ctx.getSource().sendSuccess(() ->
				TextFormatter.stringToFormattedText(
				BetterForgeChat.CHAT_ID_STR + TextFormatter.colorString()), false);
		return 1;
	}
}
