package com.enviouse.sef.commands;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.ConfigurationEventHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.filter.FilterManager;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.gui.protocol.SefGuiServer;
import com.enviouse.sef.gui.protocol.SefProtocol;
import com.enviouse.sef.gui.protocol.SefSessionManager;
import com.enviouse.sef.commandlog.CommandSpyCommands;
import com.enviouse.sef.commandlog.LoggingCommands;
import com.enviouse.sef.kernel.KernelCommands;
import com.enviouse.sef.motd.MotdCommands;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.storage.StorageCommands;
import com.enviouse.sef.automation.AutomationCommands;
import com.enviouse.sef.workstations.VirtualWorkstationCommands;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

import java.util.Map;

public class BfcCommands {
	private static FilterManager filterManager;

	protected static boolean checkPermission(CommandSourceStack c, PermissionNode<Boolean> node) {
		return PermissionService.has(c, node);
	}

	public static void register(CommandDispatcher<CommandSourceStack> disp) {
		LiteralArgumentBuilder<CommandSourceStack> sefRoot = coreRoot();

		if(ConfigHandler.config.enableFilterSystem.get()) {
			registerFilterCommands(sefRoot);
		}
			KernelCommands.attach(sefRoot);
			AutomationCommands.attach(sefRoot);
			CommandSpyCommands.attachCanonical(sefRoot);
			LoggingCommands.attachCanonical(sefRoot);
		VirtualWorkstationCommands.attachCanonical(sefRoot);
		StorageCommands.attach(sefRoot);
		if(ConfigHandler.config.enableMotdSystem.get()) {
			MotdCommands.attach(sefRoot);
		}
		disp.register(sefRoot);

		if(ConfigHandler.config.enableColorsCommand.get()
				&& com.enviouse.sef.kernel.KernelServices.shortcuts().isActive("colors")) {
			disp.register(Commands.literal("colors")
				.requires(c -> KernelCommandExecutor.canUse(
						c,
						"sef:core.colors",
						PermissionsHandler.colorsCommand))
				.executes(ctx -> KernelCommandExecutor.execute(
						ctx.getSource(),
						"sef:core.colors",
						Map.of("route", "colors"),
						() -> colorCommand(ctx),
						PermissionsHandler.colorsCommand)));
		}

		NickCommands.register(disp);
	}

	static LiteralArgumentBuilder<CommandSourceStack> coreRoot() {
		return Commands.literal("sef")
			.requires(c -> checkPermission(c, PermissionsHandler.sefCommand))
				.executes(ctx -> {
					var player = ctx.getSource().getPlayer();
					if(player != null
							&& checkPermission(ctx.getSource(), PermissionsHandler.kernelGui)
							&& SefSessionManager.instance().session(player)
									.map(session -> session.supports(SefProtocol.Feature.DASHBOARD))
									.orElse(false)) {
						return KernelCommandExecutor.execute(
								ctx.getSource(),
								"sef:gui.dashboard.open",
								Map.of(),
								() -> SefGuiServer.openDashboard(player) ? 1 : 0);
					}
					return KernelCommandExecutor.execute(
							ctx.getSource(),
							"sef:core.info",
							Map.of(),
							() -> infoCommand(ctx));
				})
			.then(Commands.literal("info")
					.requires(c -> checkPermission(c, PermissionsHandler.sefCommandInfoSubCommand))
					.executes(ctx -> KernelCommandExecutor.execute(
							ctx.getSource(),
							"sef:core.info",
							Map.of(),
							() -> infoCommand(ctx))))
			.then(Commands.literal("colors")
					.requires(c -> checkPermission(c, PermissionsHandler.sefCommandColorsSubCommand))
					.executes(ctx -> KernelCommandExecutor.execute(
							ctx.getSource(),
							"sef:core.colors",
							Map.of("route", "sef"),
							() -> colorCommand(ctx))))
			.then(Commands.literal("test")
					.requires(c -> checkPermission(c, PermissionsHandler.sefCommandTestSubCommand))
					.executes(ctx -> KernelCommandExecutor.execute(
							ctx.getSource(),
							"sef:core.test",
							Map.of(),
							() -> testCommand(ctx))))
			.then(Commands.literal("reload")
					.requires(c -> checkPermission(c, PermissionsHandler.sefCommandReloadSubCommand))
					.executes(ctx -> KernelCommandExecutor.execute(
							ctx.getSource(),
							"sef:core.reload",
							Map.of(),
							() -> reloadCommand(ctx))));
	}
	
	public static void initFilterManager(FilterManager manager) {
		filterManager = manager;
	}


	static void registerFilterCommands(LiteralArgumentBuilder<CommandSourceStack> sefRoot) {
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

		sefRoot.then(Commands.literal("filter")
				.requires(src -> checkPermission(src, PermissionsHandler.filterManage))
				// /sef filter add <id> <caseSensitive yes/no> <wordToFilter> [replacement]
				.then(Commands.literal("add")
					.then(Commands.argument("id", StringArgumentType.word())
						.then(Commands.argument("caseSensitive", StringArgumentType.word()).suggests(caseSensitiveSuggest)
							.then(Commands.argument("wordToFilter", StringArgumentType.string())
								// With replacement
								.then(Commands.argument("replacement", StringArgumentType.greedyString())
									.executes(ctx -> KernelCommandExecutor.execute(
											ctx.getSource(),
											"sef:filter.add",
											filterAddParameters(ctx, false),
											() -> filterAdd(ctx, false))))
								// Without replacement (just remove the word)
								.executes(ctx -> KernelCommandExecutor.execute(
										ctx.getSource(),
										"sef:filter.add",
										filterAddParameters(ctx, true),
										() -> filterAdd(ctx, true)))))))
				// /sef filter remove <id>
				.then(Commands.literal("remove")
					.then(Commands.argument("id", StringArgumentType.word())
						.suggests(filterIdSuggest)
						.executes(ctx -> KernelCommandExecutor.execute(
								ctx.getSource(),
								"sef:filter.remove",
								Map.of("id", StringArgumentType.getString(ctx, "id")),
								() -> filterRemove(ctx)))))
				// /sef filter list
				.then(Commands.literal("list")
					.executes(ctx -> KernelCommandExecutor.execute(
							ctx.getSource(),
							"sef:filter.list",
							Map.of("page", "1"),
							() -> filterList(ctx, 1)))
					.then(Commands.argument("page", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1))
						.executes(ctx -> {
							int page = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "page");
							return KernelCommandExecutor.execute(
									ctx.getSource(),
									"sef:filter.list",
									Map.of("page", Integer.toString(page)),
									() -> filterList(ctx, page));
						}))));
	}

	private static Map<String, String> filterAddParameters(
			CommandContext<CommandSourceStack> ctx,
			boolean noReplacement
	) {
		String caseSensitive = StringArgumentType.getString(ctx, "caseSensitive");
		return Map.of(
				"id", StringArgumentType.getString(ctx, "id"),
				"case_sensitive", Boolean.toString(
						caseSensitive.equalsIgnoreCase("yes") || caseSensitive.equalsIgnoreCase("true")),
				"replacement_present", Boolean.toString(!noReplacement));
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

	private static final int FILTER_ITEMS_PER_PAGE = 5; // 5 items * 3 lines = 15 + header/footer = ~18 lines

	private static int filterList(CommandContext<CommandSourceStack> ctx, int page) {
		if(filterManager == null) {
			ctx.getSource().sendFailure(TextFormatter.stringToFormattedText("&cFilter system not initialized"));
			return 0;
		}

		var filters = filterManager.list();
		if(filters.isEmpty()) {
			ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText("&6━━━━━━━━ Word Filters ━━━━━━━━"), false);
			ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText("&7No filters configured"), false);
			ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText("&6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"), false);
			return 1;
		}

		java.util.List<java.util.Map.Entry<String, com.enviouse.sef.filter.FilterDataStore.FilterRecord>> filterList = new java.util.ArrayList<>(filters.entrySet());
		int totalPages = (int) Math.ceil((double) filterList.size() / FILTER_ITEMS_PER_PAGE);
		if(page > totalPages) page = totalPages;
		if(page < 1) page = 1;

		int startIdx = (page - 1) * FILTER_ITEMS_PER_PAGE;
		int endIdx = Math.min(startIdx + FILTER_ITEMS_PER_PAGE, filterList.size());

		ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText("&6━━━━━━━━ Word Filters ━━━━━━━━"), false);

		for(int i = startIdx; i < endIdx; i++) {
			var entry = filterList.get(i);
			String id = entry.getKey();
			var rec = entry.getValue();
			String caseSensitive = rec.caseSensitive() ? "&a[case-sensitive]" : "&e[case-insensitive]";
			String replacement = rec.replacement().isEmpty() ? "&c(removed)" : "&f" + rec.replacement();
			ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText("&e" + id + " " + caseSensitive), false);
			ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText("  &7Word: &f" + rec.wordToFilter()), false);
			ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText("  &7Replacement: " + replacement), false);
		}

		// Footer with navigation arrows
		final int currentPage = page;
		final int finalTotalPages = totalPages;

		net.minecraft.network.chat.MutableComponent footer = TextFormatter.stringToFormattedText("&6━━━━");

		// Left arrow (previous page)
		if(currentPage > 1) {
			net.minecraft.network.chat.MutableComponent leftArrow = TextFormatter.stringToFormattedText("&e&l[◄]");
			leftArrow = leftArrow.withStyle(style -> style
				.withClickEvent(new net.minecraft.network.chat.ClickEvent(net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND, "/sef filter list " + (currentPage - 1)))
				.withHoverEvent(new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
					TextFormatter.stringToFormattedText("&7Previous page"))));
			footer = footer.append(leftArrow);
		} else {
			footer = footer.append(TextFormatter.stringToFormattedText("&8[◄]"));
		}

		footer = footer.append(TextFormatter.stringToFormattedText("&6━━&f" + currentPage + "/" + finalTotalPages + "&6━━"));

		// Right arrow (next page)
		if(currentPage < finalTotalPages) {
			net.minecraft.network.chat.MutableComponent rightArrow = TextFormatter.stringToFormattedText("&e&l[►]");
			rightArrow = rightArrow.withStyle(style -> style
				.withClickEvent(new net.minecraft.network.chat.ClickEvent(net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND, "/sef filter list " + (currentPage + 1)))
				.withHoverEvent(new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
					TextFormatter.stringToFormattedText("&7Next page"))));
			footer = footer.append(rightArrow);
		} else {
			footer = footer.append(TextFormatter.stringToFormattedText("&8[►]"));
		}

		footer = footer.append(TextFormatter.stringToFormattedText("&6━━━━"));

		final net.minecraft.network.chat.MutableComponent finalFooter = footer;
		ctx.getSource().sendSuccess(() -> finalFooter, false);

		return 1;
	}

	private static int infoCommand(CommandContext<CommandSourceStack> ctx) {
		boolean hasMetaProv = ServerEssentialsForge.instance.metadataProvider != null;
		boolean hasNickProv = ServerEssentialsForge.instance.nicknameProvider != null;
		String metaProvName = hasMetaProv ? " (via " + ServerEssentialsForge.instance.metadataProvider.getProviderName() + ")" : "";
		String nickProvName = hasNickProv ? " (via " + ServerEssentialsForge.instance.nicknameProvider.getProviderName() + ")" : "";
		ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText(
				ServerEssentialsForge.CHAT_ID_STR + "\n&eMod ID: &d" + ServerEssentialsForge.MODID
						+ "    &r&eMod version: &d" + ServerEssentialsForge.VERSION + " (NeoForge)&r\n\n"
						+ (hasMetaProv ? "&a&lWITH" : "&c&lWITHOUT") + "&r&e metadata integration" + metaProvName + "&r\n"
						+ (hasNickProv ? "&a&lWITH" : "&c&lWITHOUT") + "&r&e nickname integration" + nickProvName + "&r\n"), false);
		return 1;
	}

	private static int testCommand(CommandContext<CommandSourceStack> ctx) {
		ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText(
				ServerEssentialsForge.CHAT_ID_STR
						+ "&eColors & Styling internal debug test&r\n"
						+ "Normal &lBold&r &nUnderline&r &oItalic&r &mStrikthrough&r &kObfuscated&r &rReset\n"
						+ "Normal &lBold &nUnderline &oItalic &mStrikthrough &kObfuscated &rReset"), false);
		return 1;
	}

	private static int reloadCommand(CommandContext<CommandSourceStack> ctx) {
		ConfigurationEventHandler.reloadConfigOptions();
		ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText("&aServerEssentialsForge config reloaded."), false);
		return 1;
	}
	public static int colorCommand(CommandContext<CommandSourceStack> ctx) {
		ctx.getSource().sendSuccess(() ->
				TextFormatter.stringToFormattedText(
				ServerEssentialsForge.CHAT_ID_STR + TextFormatter.colorString()), false);
		return 1;
	}
}
