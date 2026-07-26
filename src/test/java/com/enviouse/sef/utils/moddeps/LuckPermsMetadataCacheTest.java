package com.enviouse.sef.utils.moddeps;

import com.enviouse.sef.events.PlayerEventHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LuckPermsMetadataCacheTest {
    @Test
    void rejectsInvalidMaximumSizes() {
        assertThrows(IllegalArgumentException.class, () -> new LuckPermsMetadataCache(0));
        assertThrows(IllegalArgumentException.class, () -> new LuckPermsMetadataCache(65_537));
    }

    @Test
    void valuesAreDefensivelyCopiedAndExpire() {
        LuckPermsMetadataCache cache = new LuckPermsMetadataCache(2);
        UUID player = UUID.randomUUID();
        String[] source = {"prefix", "suffix"};

        cache.put(player, source, 2_000L, 1_000L);
        source[0] = "changed";
        String[] loaded = cache.get(player, 1_500L);
        loaded[1] = "changed";

        assertArrayEquals(new String[]{"prefix", "suffix"}, cache.get(player, 1_500L));
        assertNull(cache.get(player, 2_000L));
        assertEquals(0, cache.size());
    }

    @Test
    void uniquePlayersCannotGrowCachePastItsLimit() {
        LuckPermsMetadataCache cache = new LuckPermsMetadataCache(3);

        for (int index = 0; index < 100; index++) {
            cache.put(
                    new UUID(0L, index),
                    new String[]{"prefix" + index, ""},
                    10_000L + index,
                    1_000L);
        }

        assertEquals(3, cache.size());
    }

    @Test
    void invalidateRemovesLogoutEntry() {
        LuckPermsMetadataCache cache = new LuckPermsMetadataCache(2);
        UUID player = UUID.randomUUID();
        cache.put(player, new String[]{"prefix", "suffix"}, 2_000L, 1_000L);

        cache.invalidate(player);

        assertNull(cache.get(player, 1_500L));
        assertEquals(0, cache.size());
    }

    @Test
    void playerLogoutInvalidatesProviderCache() {
        UUID playerId = UUID.randomUUID();
        LuckPermsMetadataCache cache = LuckPermsProvider.metadataCache();
        cache.clear();
        cache.put(playerId, new String[]{"prefix", "suffix"}, 2_000L, 1_000L);
        ServerPlayer player = mock(ServerPlayer.class);
        PlayerEvent.PlayerLoggedOutEvent event = mock(PlayerEvent.PlayerLoggedOutEvent.class);
        when(player.getUUID()).thenReturn(playerId);
        when(event.getEntity()).thenReturn(player);

        new PlayerEventHandler().onPlayerLogout(event);

        assertNull(cache.get(playerId, 1_500L));
        assertEquals(0, cache.size());
    }
}
