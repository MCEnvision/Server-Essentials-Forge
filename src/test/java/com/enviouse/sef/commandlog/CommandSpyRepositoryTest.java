package com.enviouse.sef.commandlog;

import com.enviouse.sef.storage.StorageService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandSpyRepositoryTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void typedFiltersPersistAcrossRestart() throws Exception {
        UUID observer = UUID.randomUUID();
        UUID includedPlayer = UUID.randomUUID();
        CommandSpyRepository first = new CommandSpyRepository(32);
        first.load(temporaryDirectory);
        first.put(CommandSpyRepository.Profile.defaults(observer).withTypedFilters(
                new CommandSpyRepository.TypedFilters(
                        Set.of("console"),
                        Set.of("failed"),
                        Set.of("minecraft:the_nether"),
                        Set.of("sudo"),
                        Set.of(includedPlayer.toString()),
                        Set.of())));

        first.flush();

        CommandSpyRepository second = new CommandSpyRepository(32);
        second.load(temporaryDirectory);
        CommandSpyRepository.TypedFilters restored = second.profile(observer).typedFilters();
        assertEquals(Set.of("console"), restored.disabledSources());
        assertEquals(Set.of("failed"), restored.disabledResults());
        assertEquals(Set.of("minecraft:the_nether"), restored.disabledWorlds());
        assertEquals(Set.of("sudo"), restored.disabledOrigins());
        assertEquals(Set.of(includedPlayer.toString()), restored.includedPlayers());
    }

    @Test
    void profilesWrittenBeforeTypedFiltersRemainCompatible() throws Exception {
        UUID observer = UUID.randomUUID();
        JsonObject profile = new JsonObject();
        profile.addProperty("observerId", observer.toString());
        profile.addProperty("enabled", false);
        profile.addProperty("audience", "EVERYONE");
        profile.add("selectedPlayerIds", new JsonArray());
        profile.addProperty("actorRelation", "EITHER");
        profile.addProperty("playerSources", true);
        profile.addProperty("nonPlayerSources", false);
        profile.addProperty("includeLocation", false);
        profile.addProperty("includeResults", false);
        profile.add("includedRoots", new JsonArray());
        profile.add("excludedRoots", new JsonArray());
        profile.add("includedActions", new JsonArray());
        profile.add("excludedActions", new JsonArray());
        profile.addProperty("revision", 1L);
        JsonArray profiles = new JsonArray();
        profiles.add(profile);
        JsonObject data = new JsonObject();
        data.add("profiles", profiles);
        StorageService.write(
                temporaryDirectory.resolve("command-spy.json"),
                "command spy",
                1,
                data,
                null,
                Set.of("/profiles"));

        CommandSpyRepository repository = new CommandSpyRepository(32);
        repository.load(temporaryDirectory);

        assertEquals(observer, repository.profile(observer).observerId());
        assertTrue(repository.profile(observer).typedFilters().disabledSources().isEmpty());
    }

    @Test
    void playerFiltersCanMoveBetweenIncludeExcludeAndNeutralStates() {
        String playerId = UUID.randomUUID().toString();
        CommandSpyRepository.TypedFilters filters = CommandSpyRepository.TypedFilters.defaults()
                .withPlayer(playerId, true, true);

        assertEquals(Set.of(playerId), filters.includedPlayers());
        assertTrue(filters.excludedPlayers().isEmpty());

        filters = filters.withPlayer(playerId, false, true);
        assertTrue(filters.includedPlayers().isEmpty());
        assertEquals(Set.of(playerId), filters.excludedPlayers());

        filters = filters.withPlayer(playerId, false, false);
        assertTrue(filters.includedPlayers().isEmpty());
        assertTrue(filters.excludedPlayers().isEmpty());

        filters = filters.withPlayer(playerId, true, true).withoutPlayer(playerId);
        assertTrue(filters.includedPlayers().isEmpty());
        assertTrue(filters.excludedPlayers().isEmpty());
    }
}
