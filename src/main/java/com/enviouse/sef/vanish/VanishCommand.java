package com.enviouse.sef.vanish;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.vanish.misc.TraceHandler;

public class VanishCommand {
	private static final Component HELP_TEXT = VanishUtil.VANISHMOD_PREFIX.copy().append(Component.literal(
				"\u00A77\u00A7nSEF Vanish\u00A7r allows you to become completely undetectable for other players. Most features can be accessed using the \u00A77/vanish\u00A7r (or \u00A77/v\u00A7r) command.\n" +
				"\u00A7nCommand Usage\u00A7r:\n" +
				"\u00A77/v get [<player>]\u00A7r: Queries the current vanished status of the given player.\n" +
				"\u00A77/v help\u00A7r: Shows this message.\n" +
				"\u00A77/v queue [<player>]\u00A7r: Adds the given player name to the vanishing queue.\n" +
				"\u00A77/v [<level>]\u00A7r (or \u00A77/vanish [<level>]\u00A7r): Vanishes or unvanishes yourself at the given level.\n" +
				"\u00A77/v toggle [<player>] [<level>]\u00A7r: Vanishes or unvanishes the given player at the specified level.\n" +
				"\u00A77/v trace\u00A7r: Enables and disables tracing.\n\n" +
				"\u00A7nVanish Levels\u00A7r:\n" +
				"Level 1 = Most hidden. Only players with \u00A77sef.vanishsee.1\u00A7r can see you. Even OPs cannot see you without the permission.\n" +
				"Level 2 = Mid. Players with \u00A77sef.vanishsee.1\u00A7r or \u00A77sef.vanishsee.2\u00A7r can see you.\n" +
				"Level 3 = Least hidden. Players with any \u00A77sef.vanishsee\u00A7r permission can see you.\n\n" +
				"\u00A7nPermissions\u00A7r:\n" +
				"\u00A77sef.vanish.1\u00A7r: Can vanish at any level (1, 2, 3).\n" +
				"\u00A77sef.vanish.2\u00A7r: Can vanish at level 2 or 3.\n" +
				"\u00A77sef.vanish.3\u00A7r: Can vanish at level 3 only."));

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(alias("v"));
		dispatcher.register(alias("vanish"));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> alias(String prefix) {
		return Commands.literal(prefix).requires(VanishCommand::hasAnyVanishPermission)
				// /v — vanish self at best level
				.executes(ctx -> vanishSelfConsoleAware(ctx, 0))
				// /v <level> — vanish self at specific level
				.then(Commands.argument("level", IntegerArgumentType.integer(1, 3))
						.executes(ctx -> vanishSelfConsoleAware(ctx, IntegerArgumentType.getInteger(ctx, "level"))))
				.then(Commands.literal("get")
						.executes(VanishCommand::getVanishedStatusSelf)
						.then(Commands.argument("player", EntityArgument.player())
								.requires(source -> PermissionService.has(source, PermissionsHandler.vanishGetOthersCommand))
								.executes(ctx -> getVanishedStatus(ctx, EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("help").executes(VanishCommand::sendHelpText))
				.then(Commands.literal("queue")
						.requires(source -> PermissionService.has(source, PermissionsHandler.vanishQueueCommand))
						.executes(VanishCommand::queueSelf)
						.then(Commands.argument("player", StringArgumentType.word())
								.requires(source -> PermissionService.has(source, PermissionsHandler.vanishOthersCommand))
								.executes(ctx -> queue(ctx, StringArgumentType.getString(ctx, "player")))))
				.then(Commands.literal("toggle")
						// /v toggle — vanish self at best level
						.executes(ctx -> vanishSelfConsoleAware(ctx, 0))
						.then(Commands.argument("player", EntityArgument.player())
								.requires(source -> PermissionService.has(source, PermissionsHandler.vanishOthersCommand))
								// /v toggle <player> — vanish target at best level
								.executes(ctx -> vanish(ctx, EntityArgument.getPlayer(ctx, "player"), 0))
								.then(Commands.argument("level", IntegerArgumentType.integer(1, 3))
										// /v toggle <player> <level>
										.executes(ctx -> vanish(ctx, EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "level"))))))
				.then(Commands.literal("trace")
						.then(Commands.literal("enable").executes(ctx -> setTraceConsoleAware(ctx, true)))
						.then(Commands.literal("disable").executes(ctx -> setTraceConsoleAware(ctx, false))));
	}

	private static int failConsoleOnly(CommandSourceStack source, String commandDesc) {
		source.sendFailure(VanishUtil.VANISHMOD_PREFIX.copy().append(commandDesc + " can only be used by players."));
		return 0;
	}

	private static int vanishSelfConsoleAware(CommandContext<CommandSourceStack> ctx, int requestedLevel) {
		try {
			return vanishSelf(ctx, requestedLevel);
		} catch (CommandSyntaxException e) {
			return failConsoleOnly(ctx.getSource(), "/vanish (self)");
		}
	}

	private static int getVanishedStatusSelf(CommandContext<CommandSourceStack> ctx) {
		ServerPlayer self;
		try {
			self = ctx.getSource().getPlayerOrException();
		} catch (CommandSyntaxException e) {
			return failConsoleOnly(ctx.getSource(), "/vanish get (without player)");
		}
		return getVanishedStatus(ctx, self);
	}

	private static int queueSelf(CommandContext<CommandSourceStack> ctx) {
		String name;
		try {
			name = ctx.getSource().getPlayerOrException().getGameProfile().getName();
		} catch (CommandSyntaxException e) {
			return failConsoleOnly(ctx.getSource(), "/vanish queue (without player)");
		}
		try {
			return queue(ctx, name);
		} catch (CommandSyntaxException e) {
			return 0;
		}
	}

	private static int setTraceConsoleAware(CommandContext<CommandSourceStack> ctx, boolean shouldTrace) {
		try {
			return setTrace(ctx, null, shouldTrace);
		} catch (CommandSyntaxException e) {
			return failConsoleOnly(ctx.getSource(), "/vanish trace");
		}
	}

	/** Check if the command source has any sef.vanish.N permission. */
	private static boolean hasAnyVanishPermission(CommandSourceStack source) {
		try {
			ServerPlayer player = source.getPlayerOrException();
			return VanishUtil.getBestVanishLevel(player) > 0
					|| PermissionService.has(player, PermissionsHandler.vanishOthersCommand)
					|| PermissionService.has(player, PermissionsHandler.vanishQueueCommand)
					|| PermissionService.has(player, PermissionsHandler.vanishGetOthersCommand);
		} catch (CommandSyntaxException e) {
			return PermissionService.isConsole(source);
		}
	}

	private static int vanishSelf(CommandContext<CommandSourceStack> ctx, int requestedLevel) throws CommandSyntaxException {
		return vanish(ctx, ctx.getSource().getPlayerOrException(), requestedLevel);
	}

		private static int getVanishedStatus(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
			if (!canTarget(ctx.getSource(), player)) {
				ctx.getSource().sendFailure(VanishUtil.VANISHMOD_PREFIX.copy().append(
						"You cannot inspect a player above your vanish hierarchy."));
				return 0;
			}
			boolean isVanished = VanishUtil.isVanished(player);
		int level = VanishUtil.getVanishLevel(player);
		MutableComponent vanishedStatus = VanishUtil.getVanishedStatusText(player, isVanished);

		if (isVanished)
			vanishedStatus.append(Component.literal(" (Level " + level + ")").withStyle(net.minecraft.ChatFormatting.GRAY));

		ctx.getSource().sendSuccess(() -> VanishUtil.VANISHMOD_PREFIX.copy().append(vanishedStatus), false);

		if (ctx.getSource().getEntity() instanceof ServerPlayer currentPlayer)
			currentPlayer.connection.send(new ClientboundSetActionBarTextPacket(vanishedStatus));

		return 1;
	}

	private static int sendHelpText(CommandContext<CommandSourceStack> ctx) {
		ctx.getSource().sendSystemMessage(HELP_TEXT);
		return 1;
	}

	private static int queue(CommandContext<CommandSourceStack> ctx, String playerName) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getServer().getPlayerList().getPlayerByName(playerName);
		if (player == null) {
			ctx.getSource().sendFailure(VanishUtil.VANISHMOD_PREFIX.copy().append(
					"Offline vanish queue changes are unavailable during stabilization."));
			return 0;
		}

		ServerPlayer executor = ctx.getSource().getEntity() instanceof ServerPlayer sourcePlayer
				? sourcePlayer
				: null;
		boolean targetsOther = executor == null || !executor.getUUID().equals(player.getUUID());
		if (!VanishPermissionPolicy.canQueueTarget(
				targetsOther,
				PermissionService.has(ctx.getSource(), PermissionsHandler.vanishQueueCommand),
				PermissionService.has(ctx.getSource(), PermissionsHandler.vanishOthersCommand))) {
			ctx.getSource().sendFailure(VanishUtil.VANISHMOD_PREFIX.copy().append(
					"You do not have permission to queue that player for vanish."));
			return 0;
		}
		if (VanishUtil.isVanished(player)) {
			ctx.getSource().sendFailure(VanishUtil.VANISHMOD_PREFIX.copy().append(String.format(
					"Could not add already vanished player %s to the vanishing queue",
					playerName)));
			return 0;
		}
		return vanish(ctx, player, 0);
	}

	/**
	 * Vanish a player. If requestedLevel is 0, use the player's best (most powerful) vanish level.
	 * Checks that the executing player has permission for the requested level.
	 */
	private static int vanish(CommandContext<CommandSourceStack> ctx, ServerPlayer player, int requestedLevel) throws CommandSyntaxException {
		boolean isVanishing = !VanishUtil.isVanished(player);
		ServerPlayer executor = ctx.getSource().getEntity() instanceof ServerPlayer sourcePlayer ? sourcePlayer : null;
			boolean targetsOther = executor == null || !executor.getUUID().equals(player.getUUID());

			if (targetsOther && !canTarget(ctx.getSource(), player)) {
				ctx.getSource().sendFailure(VanishUtil.VANISHMOD_PREFIX.copy().append(
						"You cannot change a player above your vanish hierarchy."));
				return 0;
			}

		if (targetsOther
				&& PermissionService.has(player, PermissionsHandler.vanishExempt)
				&& !PermissionService.has(ctx.getSource(), PermissionsHandler.vanishBypassExempt)) {
			ctx.getSource().sendFailure(VanishUtil.VANISHMOD_PREFIX.copy().append("That player is exempt from vanish changes."));
			return 0;
		}

		// Determine the vanish level to use
		int level;
		if (!isVanishing) {
			// Unvanishing — level doesn't matter
			level = 0;
		} else {
			int bestLevel = executor == null ? 1 : VanishUtil.getBestVanishLevel(executor);

			if (bestLevel == 0) {
				ctx.getSource().sendFailure(VanishUtil.VANISHMOD_PREFIX.copy().append("You don't have permission to vanish! Requires sef.vanish.1, sef.vanish.2, or sef.vanish.3"));
				return 0;
			}

			if (requestedLevel <= 0) {
				// No level specified — use best available
				level = bestLevel;
			} else {
				// Specific level requested — check permission
				if (executor != null && !VanishUtil.canVanishAtLevel(executor, requestedLevel)) {
					ctx.getSource().sendFailure(VanishUtil.VANISHMOD_PREFIX.copy().append("You don't have permission to vanish at level " + requestedLevel + "! Your best level is " + bestLevel));
					return 0;
				}
				level = requestedLevel;
			}
		}

		if (!isVanishing) {
			ctx.getSource().sendSuccess(() -> VanishUtil.VANISHMOD_PREFIX.copy().append(Component.translatable(VanishConfig.CONFIG.onUnvanishMessage.get(), player.getDisplayName())), true);

			if (TraceHandler.isTracing(player))
				setTrace(ctx, player, false);
		}

		VanishingHandler.toggleVanish(player, isVanishing ? level : VanishUtil.getVanishLevel(player));

		if (isVanishing)
			ctx.getSource().sendSuccess(() -> VanishUtil.VANISHMOD_PREFIX.copy().append(Component.translatable(VanishConfig.CONFIG.onVanishMessage.get(), player.getDisplayName())).append(Component.literal(" (Level " + level + ")").withStyle(net.minecraft.ChatFormatting.GRAY)), true);

			return 1;
		}

		private static boolean canTarget(CommandSourceStack source, ServerPlayer target) {
			ServerPlayer executor = source.getEntity() instanceof ServerPlayer player ? player : null;
			if (executor != null && executor.getUUID().equals(target.getUUID())) return true;
			return VanishHierarchyPolicy.canTarget(
					executor == null && PermissionService.isConsole(source),
					PermissionService.has(source, PermissionsHandler.vanishHierarchyBypass),
					executor == null ? 0 : VanishUtil.getBestVanishLevel(executor),
					VanishUtil.getBestVanishLevel(target));
		}

	private static int setTrace(CommandContext<CommandSourceStack> ctx, ServerPlayer playerOverride, boolean shouldTrace) throws CommandSyntaxException {
		CommandSourceStack source = ctx.getSource();
		ServerPlayer player = playerOverride != null ? playerOverride : source.getPlayerOrException();
		boolean isTracing = TraceHandler.isTracing(player);

		if (!VanishUtil.isVanished(player)) {
			source.sendFailure(VanishUtil.VANISHMOD_PREFIX.copy().append("You need to be vanished to configure tracing!"));
			return 0;
		}
		else if (isTracing == shouldTrace) {
			source.sendFailure(VanishUtil.VANISHMOD_PREFIX.copy().append("Tracing is already " + (isTracing ? "enabled!" : "disabled!")));
			return 0;
		}

		TraceHandler.setTracing(player, shouldTrace);

		if (shouldTrace) {
			source.sendSystemMessage(VanishUtil.VANISHMOD_PREFIX.copy().append("Tracing is now enabled."));
			TraceHandler.sendTraceStatus(player);
		}
		else
			source.sendSystemMessage(VanishUtil.VANISHMOD_PREFIX.copy().append("Tracing is now disabled."));

		return 1;
	}
}
