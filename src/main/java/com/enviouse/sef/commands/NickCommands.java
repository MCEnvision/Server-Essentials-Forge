package com.enviouse.sef.commands;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.config.PlayerData;
import com.enviouse.sef.identity.IdentityArguments;
import com.enviouse.sef.identity.IdentityService;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.utils.IntegratedNicknameProvider;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class NickCommands {
	private static final java.util.concurrent.atomic.AtomicBoolean PERMISSION_MIGRATION_WARNING =
			new java.util.concurrent.atomic.AtomicBoolean();
	private static boolean cfgWhoIsEnabled = false;
	private static boolean cfgNickEnabled = false;
	private static int minNicknameLength = -1;
	private static int maxNicknameLength = -1;
	
	public static boolean nicknameIntegrationEnabled = false;
	
		public static void reloadConfig() {
		minNicknameLength = ConfigHandler.config.minimumNicknameLength.get();
		maxNicknameLength = ConfigHandler.config.maximumNicknameLength.get();
		if(minNicknameLength > maxNicknameLength) {
			int oldMin = minNicknameLength;
			minNicknameLength = maxNicknameLength;
			maxNicknameLength = oldMin;
			ServerEssentialsForge.LOGGER.warn("Minimum nickname length was greater then maximum, swapped values");
			ServerEssentialsForge.LOGGER.warn(minNicknameLength + " < nickname.length() < " + maxNicknameLength);
		}
		cfgWhoIsEnabled = ConfigHandler.config.enableWhoisCommand.get();
			cfgNickEnabled = ConfigHandler.config.enableChatNicknameCommand.get();
				if(PERMISSION_MIGRATION_WARNING.compareAndSet(false, true)) {
					ServerEssentialsForge.LOGGER.warn(
							"[SEF] Nickname changes for other players now default to denied. Existing explicit permission provider grants remain unchanged");
				}
		}
		public static void register(CommandDispatcher<CommandSourceStack> disp) {
			boolean externalNicknameOwner = net.neoforged.fml.ModList.get().isLoaded("ftbessentials")
					&& ConfigHandler.config.enableFtbEssentials.get();
			if(!externalNicknameOwner) {
			/* /nick */
			disp.register(Commands.literal("nick").requires((c) -> {
			return (nicknameIntegrationEnabled || cfgNickEnabled) &&
				(BfcCommands.checkPermission(c, PermissionsHandler.nickCommand));
		}).executes((ctx) -> nickCommand(ctx, true, false)));
		/* /nick <nickname> */
		disp.register(Commands.literal("nick").requires((c) -> {
			return (nicknameIntegrationEnabled || cfgNickEnabled) &&
				(BfcCommands.checkPermission(c, PermissionsHandler.nickCommand));
		}).then(Commands.argument("nickname", StringArgumentType.greedyString())
			.executes((ctx) -> nickCommand(ctx, false, false))));
		/* /nickfor <username> <nickname> */
			disp.register(Commands.literal("nickfor").requires((c) -> {
				return (nicknameIntegrationEnabled || cfgNickEnabled) &&
					(BfcCommands.checkPermission(c, PermissionsHandler.nickOthersCommand)); })
				.then(Commands.argument("username", StringArgumentType.string())
				.suggests(IdentityArguments.suggestions(true))
				.then(Commands.argument("nickname", StringArgumentType.greedyString())
				.executes((ctx) -> nickCommand(ctx, false, true)))));
		/* /nickfor <username> */
		disp.register(Commands.literal("nickfor").requires((c) -> {
				return (nicknameIntegrationEnabled || cfgNickEnabled) &&
					(BfcCommands.checkPermission(c, PermissionsHandler.nickOthersCommand)); })
					.then(Commands.argument("username", StringArgumentType.string())
					.suggests(IdentityArguments.suggestions(true))
					.executes((ctx) -> nickCommand(ctx, true, true))));
			}
			/* /whois <nickname> */
		disp.register(Commands.literal("whois").requires((c) -> {
				return (nicknameIntegrationEnabled || cfgWhoIsEnabled) && 
					(BfcCommands.checkPermission(c, PermissionsHandler.whoisCommand));
			}).then(Commands.argument("displayname", StringArgumentType.string())
				.suggests(IdentityArguments.suggestions(false))
				.executes(NickCommands::whoisCommand)));
	}

		private static GameProfile lookupGameProfile(String user) {
			MinecraftServer serv = ServerLifecycleHooks.getCurrentServer();
			if(serv != null) {
				String normalizedUser = NicknamePolicy.normalizeIdentity(user);
				List<ServerPlayer> players = serv.getPlayerList().getPlayers();
					Map<UUID, GameProfile> usernameMatches = new LinkedHashMap<>();
					Map<UUID, GameProfile> nicknameMatches = new LinkedHashMap<>();
					for(ServerPlayer player : players) {
						GameProfile profile = player.getGameProfile();
						String nickname = getNickname(profile);
						if(normalizedUser.equals(NicknamePolicy.normalizeIdentity(profile.getName()))) {
							usernameMatches.put(profile.getId(), profile);
						} else if(normalizedUser.equals(NicknamePolicy.normalizeIdentity(
								NicknamePolicy.stripFormatting(nickname)))) {
							nicknameMatches.put(profile.getId(), profile);
						}
					}
				boolean includeKnownNicknames = ownsIntegratedNicknameData();
				for(PlayerData.ProfileSnapshot profile : PlayerData.profiles()) {
					boolean usernameMatch = normalizedUser.equals(
							NicknamePolicy.normalizeIdentity(profile.authenticatedUsername()));
						boolean nicknameMatch = includeKnownNicknames && normalizedUser.equals(
								NicknamePolicy.normalizeIdentity(
										NicknamePolicy.stripFormatting(profile.nickname())));
						if(usernameMatch) {
							String username = profile.authenticatedUsername() == null
									? user
									: profile.authenticatedUsername();
							usernameMatches.putIfAbsent(
									profile.playerId(),
									new GameProfile(profile.playerId(), username));
						} else if(nicknameMatch) {
							String username = profile.authenticatedUsername() == null
									? user
									: profile.authenticatedUsername();
							nicknameMatches.putIfAbsent(
									profile.playerId(),
									new GameProfile(profile.playerId(), username));
						}
					}
					if(usernameMatches.size() == 1) {
						return usernameMatches.values().iterator().next();
					}
					if(!usernameMatches.isEmpty()) {
						return null;
					}
					return nicknameMatches.size() == 1 ? nicknameMatches.values().iterator().next() : null;
			}
			return null;
		}
		private static int whoisCommand(CommandContext<CommandSourceStack> ctx) {
			String user = StringArgumentType.getString(ctx, "displayname");
			ActionResult<IdentityService.Identity> identity =
					KernelServices.identities().resolve(user, ctx.getSource().getPlayer());
			if(identity.successful() && identity.value().playerId() != null) {
				ctx.getSource().sendSuccess(()->TextFormatter.stringToFormattedText(
						"&eFound a name matching " + user + ": \""
								+ identity.value().authenticatedUsername() + "\"\n&eUUID: "
								+ identity.value().playerId() + "&r"), false);
			return 1;
		} else {
			ctx.getSource().sendFailure(TextFormatter.stringToFormattedText("&cUnknown username/nickname!&r"));
			return 0;
		}
	}
		private static int assignNickname(CommandContext<CommandSourceStack> ctx, UUID uuid, String nick) {
			if(!ownsIntegratedNicknameData()) {
				ctx.getSource().sendFailure(TextFormatter.stringToFormattedText(
						"&cNicknames are owned by the configured external provider. Use that provider's nickname command.&r"));
				return 0;
			}
			if(nick == null) {
			if(!com.enviouse.sef.kernel.KernelServices.profiles().setNickname(uuid, null)) {
				ctx.getSource().sendFailure(TextFormatter.stringToFormattedText(
						"&cNickname data is unavailable. No changes were applied.&r"));
				return 0;
			}
					refreshProjectedIdentity(uuid);
					ctx.getSource().sendSuccess(()->TextFormatter.stringToFormattedText("&eNickname reset!&r"), false);
			return 1;
		} else {
			if(NicknamePolicy.containsColorFormatting(nick)
					&& !PermissionService.has(ctx.getSource(), PermissionsHandler.nickColorsAllowed)) {
				ctx.getSource().sendFailure(TextFormatter.stringToFormattedText("&cYou do not have permission to use nickname colors.&r"));
				return 0;
			}
			if(NicknamePolicy.containsStyleFormatting(nick)
					&& !PermissionService.has(ctx.getSource(), PermissionsHandler.nickStylesAllowed)) {
				ctx.getSource().sendFailure(TextFormatter.stringToFormattedText("&cYou do not have permission to use nickname styles.&r"));
				return 0;
			}

			String visibleNickname = NicknamePolicy.stripFormatting(nick);
			NicknamePolicy.Validation validation =
					NicknamePolicy.validate(visibleNickname, minNicknameLength, maxNicknameLength);
			if(!validation.valid()) {
				ctx.getSource().sendFailure(TextFormatter.stringToFormattedText("&c" + validation.error() + ".&r"));
				return 0;
			}
				if(!ConfigHandler.config.nicknameAllowDuplicateWithUsernameHover.get()
						&& hasIdentityCollision(uuid, validation.normalized())) {
				ctx.getSource().sendFailure(TextFormatter.stringToFormattedText("&cThat nickname conflicts with another online player's name or nickname.&r"));
				return 0;
			}

			if(!com.enviouse.sef.kernel.KernelServices.profiles().setNickname(uuid, nick)) {
				ctx.getSource().sendFailure(TextFormatter.stringToFormattedText(
						"&cNickname data is unavailable. No changes were applied.&r"));
				return 0;
			}
					refreshProjectedIdentity(uuid);
					ctx.getSource().sendSuccess(()->TextFormatter.stringToFormattedText("&eNickname set to \"" + nick + "&r&e\"!&r"), false);
			return 1;
		}
	}

		private static boolean hasIdentityCollision(UUID targetId, String normalizedNickname) {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if(server == null) return false;
			if(ConfigHandler.config.nicknameUniqueOnline.get()) {
				for(ServerPlayer player : server.getPlayerList().getPlayers()) {
					if(player.getUUID().equals(targetId)) continue;
					GameProfile profile = player.getGameProfile();
					if(normalizedNickname.equals(NicknamePolicy.normalizeIdentity(profile.getName()))) return true;
					String existingNickname = getNickname(profile);
					if(existingNickname != null && normalizedNickname.equals(NicknamePolicy.normalizeIdentity(
							NicknamePolicy.stripFormatting(existingNickname)))) return true;
				}
			}
			return ConfigHandler.config.nicknameUniqueKnownProfiles.get()
					&& PlayerData.hasIdentityCollision(targetId, normalizedNickname, ownsIntegratedNicknameData());
		}

		private static void refreshProjectedIdentity(UUID playerId) {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if(server == null) return;
			ServerPlayer player = server.getPlayerList().getPlayer(playerId);
			if(player != null) {
				player.refreshTabListName();
			}
		}

		private static boolean ownsIntegratedNicknameData() {
			return ServerEssentialsForge.instance == null
					|| ServerEssentialsForge.instance.nicknameProvider == null
					|| ServerEssentialsForge.instance.nicknameProvider instanceof IntegratedNicknameProvider;
		}

	private static String getNickname(GameProfile profile) {
		if(ServerEssentialsForge.instance == null || ServerEssentialsForge.instance.nicknameProvider == null) {
			return PlayerData.getNickname(profile.getId());
		}
		return ServerEssentialsForge.instance.nicknameProvider.getPlayerNickname(profile);
	}
	private static int nickCommand(CommandContext<CommandSourceStack> ctx, boolean reset, boolean other) {
		ServerPlayer player = null;
		try {
			player = ctx.getSource().getPlayerOrException();
		} catch (CommandSyntaxException ignored) { }
		String nick = reset ? null : StringArgumentType.getString(ctx, "nickname");
		String user = other ? StringArgumentType.getString(ctx, "username") : null;
		/* /nick OR /nick <nickname> */
		if(player != null && user == null) 
			return assignNickname(ctx, player.getUUID(), nick);
			/* /nickfor <user> OR /nickfor <user> <nickname> */
			if(user != null) {
				ActionResult<IdentityService.Identity> identity =
						KernelServices.identities().resolve(user, ctx.getSource().getPlayer());
				if(identity.successful() && identity.value().playerId() != null) {
					return assignNickname(ctx, identity.value().playerId(), nick);
			} else {
				ctx.getSource().sendFailure(TextFormatter.stringToFormattedText("&cUnknown player: \"" + user + "\"!&r"));
				return 0;
			}
		}
		// /nick from console — no self and no target given
		ctx.getSource().sendFailure(TextFormatter.stringToFormattedText(
				"&c/nick can only be used by players. Use &f/nickfor <player> [nickname]&c for other players.&r"));
		return 0;
	}
}
