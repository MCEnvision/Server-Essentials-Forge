package com.enviouse.sef.vanish.compat;

import java.lang.reflect.Method;
import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Compatibility with the Playtime mod.
 * When a player vanishes, we pause their session so playtime stops accumulating.
 * When they unvanish, we resume tracking.
 * Uses reflection to avoid compile-time dependency on Playtime.
 */
public class PlaytimeCompat {
	private static final Logger LOGGER = LogManager.getLogger("SEF/Playtime");
	private static boolean initialized = false;
	private static Method getSessionTracker = null;
	private static Method pauseSessionMethod = null;
	private static Method resumeSessionMethod = null;

	private static void init() {
		if (initialized) return;
		initialized = true;
		try {
			Class<?> playtimeClass = Class.forName("com.enviouse.playtime.Playtime");
			getSessionTracker = playtimeClass.getMethod("getSessionTracker");
			Class<?> sessionTrackerClass = Class.forName("com.enviouse.playtime.service.SessionTracker");
			pauseSessionMethod = sessionTrackerClass.getMethod("pauseSession", UUID.class);
			resumeSessionMethod = sessionTrackerClass.getMethod("resumeSession", UUID.class);
			LOGGER.info("Playtime pause/resume API loaded successfully");
		} catch (Exception e) {
			LOGGER.warn("Playtime mod API not available for vanish integration: {}", e.getMessage());
		}
	}

	public static void onVanishChange(ServerPlayer player, boolean vanished) {
		init();
		try {
			if (getSessionTracker == null) return;
			Object tracker = getSessionTracker.invoke(null);
			if (tracker == null) return;

			if (vanished) {
				// Pause session — stops playtime accumulation without heavy join/leave lifecycle
				if (pauseSessionMethod != null) {
					pauseSessionMethod.invoke(tracker, player.getUUID());
					LOGGER.debug("Paused playtime tracking for vanished player {}", player.getGameProfile().getName());
				}
			} else {
				// Resume session — resumes playtime tracking
				if (resumeSessionMethod != null) {
					resumeSessionMethod.invoke(tracker, player.getUUID());
					LOGGER.debug("Resumed playtime tracking for unvanished player {}", player.getGameProfile().getName());
				}
			}
		} catch (Exception e) {
			LOGGER.warn("Failed to update Playtime tracking on vanish change for {}: {}", player.getGameProfile().getName(), e.getMessage());
		}
	}
}
