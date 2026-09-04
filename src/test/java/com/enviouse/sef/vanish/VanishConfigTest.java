package com.enviouse.sef.vanish;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class VanishConfigTest {

	@Test
	void unloadedServerValuesUseDeclaredDefaults() {
		assertNotNull(VanishConfig.CONFIG.hidePlayersFromWorld);
		assertEquals(Boolean.TRUE, VanishConfig.get(VanishConfig.CONFIG.hidePlayersFromWorld));
		assertEquals("%s vanished", VanishConfig.get(VanishConfig.CONFIG.onVanishMessage));
	}
}
