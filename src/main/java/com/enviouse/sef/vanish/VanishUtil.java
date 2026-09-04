package com.enviouse.sef.vanish;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.permissions.PermissionService;

public class VanishUtil {
	/** Maps vanished player UUID to their vanish level (1=highest/most hidden, 3=lowest). */
	public static final Map<UUID, Integer> VANISHED_PLAYERS = new HashMap<>();
	private static final Map<UUID, Integer> OBSERVER_LEVELS = new HashMap<>();
	public static final MutableComponent VANISHMOD_PREFIX = Component.literal("").append(Component.literal("[").withStyle(ChatFormatting.WHITE)).append(Component.literal("SEF-Vanish").withStyle(s -> s.applyFormat(ChatFormatting.GRAY))).append(Component.literal("] ").withStyle(ChatFormatting.WHITE));

	/** Predicate that excludes spectators AND vanished players. */
	public static final Predicate<Entity> NO_SPECTATORS_AND_NO_VANISH = EntitySelector.NO_SPECTATORS.and(entity -> !isVanished(entity));
	/** Predicate that excludes spectators, non-collidable, AND vanished players. */
	public static final Predicate<Entity> CAN_BE_COLLIDED_WITH_AND_NO_VANISH = NO_SPECTATORS_AND_NO_VANISH.and(Entity::canBeCollidedWith);

	public static boolean isVanished(Entity player) {
		return isVanished(player, null);
	}

	public static boolean isVanished(Player player) {
		return isVanished(player, null);
	}

	public static boolean isVanished(Entity potentialPlayer, Entity forPlayer) {
		if (potentialPlayer instanceof Player player)
			return isVanished(player, forPlayer);

		return false;
	}

	public static boolean isVanished(Player player, Entity forPlayer) {
		if (VANISHED_PLAYERS.isEmpty())
			return false;

		// Master kill switch: if the vanish system is disabled, nobody is ever considered vanished
		// (so every vanish mixin no-ops). Checked after the empty-set fast path to keep it cheap.
		if (!ConfigHandler.config.enableVanishSystem.get())
			return false;

		if (player != null && !player.level().isClientSide) {
			boolean isVanished = VANISHED_PLAYERS.containsKey(player.getUUID());

			if (forPlayer != null)
				return !playerAllowedToSeeOther(forPlayer, player, isVanished(forPlayer), isVanished);

			return isVanished;
		}

		return false;
	}

	/** Get the vanish level of a player (1=highest/most hidden, 2, 3=lowest). Returns 0 if not vanished. */
	public static int getVanishLevel(Entity entity) {
		if (!ConfigHandler.config.enableVanishSystem.get())
			return 0;
		if (entity instanceof Player player && !player.level().isClientSide)
			return VANISHED_PLAYERS.getOrDefault(player.getUUID(), 0);
		return 0;
	}

	/**
	 * Determines if the subject (observer) is allowed to see the otherPlayer (actor).
	 * Uses the 3-level permission system — NO OP fallback, purely permission-based:
	 * - sef.vanishsee.1 can see ALL vanished (level 1, 2, 3)
	 * - sef.vanishsee.2 can see level 2 and 3
	 * - sef.vanishsee.3 can see only level 3
	 */
	public static boolean playerAllowedToSeeOther(Entity subject, Entity otherPlayer, boolean isSubjectVanished, boolean isOtherVanished) {
		if (subject.equals(otherPlayer))
			return true;

		if (!isOtherVanished)
			return true;

		// Check permission-based visibility
		if (canSeeVanishedAtLevel(subject, getVanishLevel(otherPlayer)))
			return true;

		// Check team visibility
		if (checkTeamVisibility(subject, otherPlayer))
			return true;

		return false;
	}

	/**
	 * Checks if the observer entity can see vanished players at the given level.
	 * Purely permission-based via Forge PermissionAPI (compatible with LuckPerms).
	 * NO OP fallback — even operators cannot see vanished players without explicit permission.
	 * To see a player vanished at level N, the observer needs sef.vanishsee.N (or a lower number like .1).
	 */
	public static boolean canSeeVanishedAtLevel(Entity entity, int vanishLevel) {
		if (vanishLevel <= 0)
			return true; // Not vanished, always visible
		if (entity instanceof ServerPlayer player)
			return observerCanSeeAtLevel(observerBestSeeLevel(player), vanishLevel);
		return false;
	}

	/**
	 * Pure visibility decision, extracted from {@link #canSeeVanishedAtLevel} with NO Minecraft or
	 * PermissionAPI calls so it can be unit-tested (see VanishVisibilityTest). Encodes the invariant:
	 * a vanished player is hidden from observers without sufficient permission, and FULLY restored to
	 * visible-for-everyone once unvanished (targetVanishLevel == 0).
	 *
	 * @param observerBestSeeLevel the lowest (most powerful) {@code sef.vanishsee.N} the observer holds,
	 *                             or 0 if none. (vanishsee.1 is most powerful, sees all levels.)
	 * @param targetVanishLevel    the target's vanish level: 1 = most hidden, 3 = least hidden,
	 *                             0 = not vanished.
	 * @return whether the observer is permitted to see the target.
	 */
	public static boolean observerCanSeeAtLevel(int observerBestSeeLevel, int targetVanishLevel) {
		// Delegates to the MC-free VanishVisibility so the decision can be unit-tested.
		return VanishVisibility.canSee(observerBestSeeLevel, targetVanishLevel);
	}

	/** The lowest (most powerful) {@code sef.vanishsee.N} the observer holds via PermissionAPI, or 0 if none. */
	private static int observerBestSeeLevel(ServerPlayer player) {
		for (int n = 1; n <= 3; n++) {
			PermissionNode<Boolean> node = Vanishmod.VANISH_SEE_NODES.get(n);
			if (node != null && PermissionService.has(player, node))
				return n;
		}
		return 0;
	}

	/** Checks if the observer can see ANY vanished player (has at least sef.vanishsee.3). */
	public static boolean canSeeAnyVanished(Entity entity) {
		return canSeeVanishedAtLevel(entity, 3);
	}

	/**
	 * Checks if the player has permission to vanish at the given level.
	 * Having sef.vanish.1 allows vanishing at 1, 2, or 3.
	 * Having sef.vanish.2 allows vanishing at 2 or 3.
	 * Having sef.vanish.3 allows vanishing at 3 only.
	 */
	public static boolean canVanishAtLevel(ServerPlayer player, int level) {
		if (level < 1 || level > 3) return false;
		for (int checkLevel = 1; checkLevel <= level; checkLevel++) {
				PermissionNode<Boolean> node = Vanishmod.VANISH_LEVEL_NODES.get(checkLevel);
				if (node != null) {
					if (PermissionService.has(player, node))
						return true;
				}
		}
		return false;
	}

	/**
	 * Returns the best (most powerful / lowest number) vanish level this player can use.
	 * Returns 1 if they have sef.vanish.1, 2 if sef.vanish.2, 3 if sef.vanish.3, or 0 if none.
	 */
	public static int getBestVanishLevel(ServerPlayer player) {
		for (int level = 1; level <= 3; level++) {
				PermissionNode<Boolean> node = Vanishmod.VANISH_LEVEL_NODES.get(level);
				if (node != null) {
					if (PermissionService.has(player, node))
						return level;
				}
		}
		return 0;
	}

	public static boolean checkTeamVisibility(Entity player, Entity otherPlayer) {
		if (!VanishConfig.get(VanishConfig.CONFIG.seeVanishedTeamPlayers))
			return false;

		Team team = player.getTeam();

		return team != null && team.canSeeFriendlyInvisibles() && team.getPlayers().contains(otherPlayer.getScoreboardName());
	}

	public static void recheckVanished(ServerPlayer player) {
		int observerLevel = observerBestSeeLevel(player);
		Integer previousObserverLevel = OBSERVER_LEVELS.put(player.getUUID(), observerLevel);
		boolean isMarkedVanished = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean("Vanished");
		if (!ConfigHandler.config.enableVanishSystem.get()) {
			boolean wasRuntimeVanished = VANISHED_PLAYERS.containsKey(player.getUUID());
			if (wasRuntimeVanished || isMarkedVanished) {
				VanishingHandler.updateVanishedStatus(player, false, 0);
				if (wasRuntimeVanished) {
					VanishingHandler.sendPacketsOnVanish(player, player.serverLevel(), false);
				}
			}
			return;
		}

		int storedLevel = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getInt("VanishLevel");
		int desiredLevel = VanishPermissionPolicy.reconcileLevel(
				isMarkedVanished, storedLevel, getBestVanishLevel(player));
		if (isMarkedVanished && desiredLevel == 0 && getVanishLevel(player) == 0) {
			VanishingHandler.updateVanishedStatus(player, false, 0);
		} else {
			VanishingHandler.reconcilePermissionLevel(player, desiredLevel);
		}
		if (previousObserverLevel != null && previousObserverLevel != observerLevel) {
			refreshAllVisibility(player.server);
		}
	}

	public static void recheckAll(net.minecraft.server.MinecraftServer server) {
		if (server == null) return;
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			recheckVanished(player);
		}
	}

	public static void refreshAllVisibility(net.minecraft.server.MinecraftServer server) {
		if (server == null || !ConfigHandler.config.enableVanishSystem.get()) return;
		for (ServerPlayer target : List.copyOf(server.getPlayerList().getPlayers())) {
			if (isVanished(target)) {
				VanishingHandler.sendPacketsOnVanish(target, target.serverLevel(), true);
			}
		}
	}

	public static void clearRuntimeState() {
		VANISHED_PLAYERS.clear();
		OBSERVER_LEVELS.clear();
	}

	public static void forgetPlayer(UUID playerId) {
		VANISHED_PLAYERS.remove(playerId);
		OBSERVER_LEVELS.remove(playerId);
	}

	public static List<? extends Entity> removeVanishedFromEntityList(List<? extends Entity> rawList, Entity forPlayer) {
		if (VANISHED_PLAYERS.isEmpty())
			return rawList;

		return rawList.stream().filter(entity -> !(entity instanceof Player player) || !isVanished(player, forPlayer)).collect(Collectors.toList());
	}

	public static <T extends Player> List<T> removeVanishedFromPlayerList(List<T> rawList, Entity forPlayer) {
		if (VANISHED_PLAYERS.isEmpty())
			return rawList;

		return rawList.stream().filter(player -> !isVanished(player, forPlayer)).collect(Collectors.toList());
	}

	public static MutableComponent getVanishedStatusText(ServerPlayer player, boolean isVanished) {
		return Component.translatable(isVanished ? VanishConfig.get(VanishConfig.CONFIG.onVanishQuery) : VanishConfig.get(VanishConfig.CONFIG.onUnvanishQuery), player.getDisplayName());
	}

	public static ResourceKey<ChatType> getChatTypeRegistryKey(ChatType.Bound chatType, Player player) {
		return player.level().registryAccess().registryOrThrow(Registries.CHAT_TYPE).getResourceKey(chatType.chatType().value()).orElse(ChatType.CHAT);
	}
}
