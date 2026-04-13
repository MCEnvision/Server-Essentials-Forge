package com.enviouse.sef.vanish.compat;

import java.lang.reflect.Method;

import net.minecraft.server.level.ServerPlayer;

/**
 * Compatibility with the Playtime mod.
 * When a player vanishes, we simulate a disconnect so playtime stops accumulating.
 * When they unvanish, we simulate a reconnect so playtime resumes.
 * Uses reflection to avoid compile-time dependency on Playtime.
 */
public class PlaytimeCompat {
	private static boolean initialized = false;
	private static Object sessionTracker = null;
	private static Method getSessionTracker = null;
	private static Method onPlayerLeave = null;
	private static Method onPlayerJoin = null;

	private static void init() {
		if (initialized) return;
		initialized = true;
		try {
			Class<?> playtimeClass = Class.forName("com.enviouse.playtime.Playtime");
			getSessionTracker = playtimeClass.getMethod("getSessionTracker");
			Class<?> sessionTrackerClass = Class.forName("com.enviouse.playtime.service.SessionTracker");
			onPlayerLeave = sessionTrackerClass.getMethod("onPlayerLeave", net.minecraft.server.MinecraftServer.class, ServerPlayer.class);
			onPlayerJoin = sessionTrackerClass.getMethod("onPlayerJoin", net.minecraft.server.MinecraftServer.class, ServerPlayer.class);
		} catch (Exception e) {
			// Playtime mod API not available
		}
	}

	public static void onVanishChange(ServerPlayer player, boolean vanished) {
		init();
		try {
			if (getSessionTracker == null) return;
			Object tracker = getSessionTracker.invoke(null);
			if (tracker == null) return;

			if (vanished) {
				// Simulate player leaving - stops playtime tracking
				if (onPlayerLeave != null)
					onPlayerLeave.invoke(tracker, player.getServer(), player);
			} else {
				// Simulate player joining - resumes playtime tracking
				if (onPlayerJoin != null)
					onPlayerJoin.invoke(tracker, player.getServer(), player);
			}
		} catch (Exception e) {
			// Silently ignore if Playtime mod API changed or is unavailable
		}
	}
}
