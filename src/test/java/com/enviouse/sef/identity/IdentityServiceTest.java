package com.enviouse.sef.identity;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.utils.INicknameProvider;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IdentityServiceTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void usernameAndNicknameCollisionFailsClosed() {
        PlayerProfileRepository profiles = new PlayerProfileRepository();
        profiles.load(temporaryDirectory.toFile());
        UUID alice = UUID.randomUUID();
        UUID bob = UUID.randomUUID();
        assertTrue(profiles.remember(alice, "Alice"));
        assertTrue(profiles.remember(bob, "Bob"));
        assertTrue(profiles.setNickname(bob, "Alice"));
        IdentityService identities = new IdentityService(() -> null, profiles);

        ActionResult<IdentityService.Identity> result = identities.resolve("alice", null);

        assertEquals(ActionResult.ReasonCode.AMBIGUOUS, result.reason());
        assertTrue(profiles.shutdown());
    }

    @Test
    void activeNicknameProviderDrivesOnlineResolutionAndSuggestions() {
        PlayerProfileRepository profiles = new PlayerProfileRepository();
        profiles.load(temporaryDirectory.resolve("external").toFile());
        UUID playerId = UUID.randomUUID();
        GameProfile profile = new GameProfile(playerId, "Alice");
        ServerPlayer player = mock(ServerPlayer.class);
        when(player.getUUID()).thenReturn(playerId);
        when(player.getGameProfile()).thenReturn(profile);
        PlayerList playerList = mock(PlayerList.class);
        when(playerList.getPlayers()).thenReturn(List.of(player));
        MinecraftServer server = mock(MinecraftServer.class);
        when(server.getPlayerList()).thenReturn(playerList);
        INicknameProvider nicknames = mock(INicknameProvider.class);
        when(nicknames.getPlayerNickname(profile)).thenReturn("Builder");

        ServerEssentialsForge previous = ServerEssentialsForge.instance;
        ServerEssentialsForge instance = mock(ServerEssentialsForge.class);
        instance.nicknameProvider = nicknames;
        ServerEssentialsForge.instance = instance;
        try {
            IdentityService identities = new IdentityService(() -> server, profiles);

            ActionResult<IdentityService.Identity> result = identities.resolve("builder", null);

            assertTrue(result.successful());
            assertEquals(playerId, result.value().playerId());
            assertTrue(identities.suggestions(null, true).contains("Builder"));
        } finally {
            ServerEssentialsForge.instance = previous;
            assertTrue(profiles.shutdown());
        }
    }
}
