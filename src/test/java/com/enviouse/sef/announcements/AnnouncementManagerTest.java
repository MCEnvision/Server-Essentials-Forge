package com.enviouse.sef.announcements;

import com.enviouse.sef.audit.SecurityAuditService;
import com.enviouse.sef.config.ConfigHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnnouncementManagerTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void commandLikeTextIsOnlyBroadcastAsText() {
        AnnouncementManager manager = new AnnouncementManager();
        MinecraftServer server = mock(MinecraftServer.class);
        PlayerList playerList = mock(PlayerList.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(server.getPlayerList()).thenReturn(playerList);
        when(playerList.getPlayers()).thenReturn(List.of(player));

        manager.broadcastText(server, "/op @a <selector>", "@a", "notice", false);

        verify(player).sendSystemMessage(argThat(component ->
                "/op @a <selector>".equals(component.getString())));
        verify(server, never()).getCommands();
    }

    @Test
    void commandDefinitionIsRejectedWhenItsRootPolicyIsDenied() {
        AnnouncementManager manager = new AnnouncementManager();
        CommandAnnouncement announcement = announcement("op EnVy");

        assertFalse(manager.add(announcement));
        assertTrue(manager.getCommandAnnouncements().isEmpty());
    }

    @Test
    void disabledCommandFeatureAuditsDenialWithoutDispatching() throws Exception {
        boolean previousEnabled = ConfigHandler.config.enableCommandAnnouncements.get();
        MinecraftServer server = mock(MinecraftServer.class);
        AnnouncementManager manager = new AnnouncementManager();
        SecurityAuditService.start(temporaryDirectory, 7, 1);
        try {
            ConfigHandler.config.enableCommandAnnouncements.set(false);
            manager.fireCommand(server, announcement("say hello"));
        } finally {
            ConfigHandler.config.enableCommandAnnouncements.set(previousEnabled);
            SecurityAuditService.shutdown();
        }

        verify(server, never()).getCommands();
        JsonObject audit = onlyAuditEvent();
        assertEquals("execute command", audit.get("actionId").getAsString());
        assertEquals("denied", audit.get("result").getAsString());
        assertEquals("feature disabled", audit.get("reasonCode").getAsString());
        assertEquals("notice", audit.getAsJsonObject("normalizedParameters").get("target").getAsString());
    }

    @Test
    void synchronousCommandCallbackProducesOneSuccessfulAuditEvent() throws Exception {
        boolean previousEnabled = ConfigHandler.config.enableCommandAnnouncements.get();
        String previousAllowed = ConfigHandler.config.commandAnnouncementAllowedCommands.get();
        String previousDenied = ConfigHandler.config.commandAnnouncementDeniedCommands.get();
        MinecraftServer server = mock(MinecraftServer.class);
        CommandSourceStack initialSource = mock(CommandSourceStack.class);
        CommandSourceStack callbackSource = mock(CommandSourceStack.class);
        Commands commands = mock(Commands.class);
        AtomicReference<CommandResultCallback> callback = new AtomicReference<>();
        when(server.createCommandSourceStack()).thenReturn(initialSource);
        when(initialSource.withCallback(any())).thenAnswer(invocation -> {
            callback.set(invocation.getArgument(0));
            return callbackSource;
        });
        when(server.getCommands()).thenReturn(commands);
        doAnswer(invocation -> {
            callback.get().onResult(true, 1);
            return null;
        }).when(commands).performPrefixedCommand(eq(callbackSource), eq("say hello"));

        AnnouncementManager manager = new AnnouncementManager();
        SecurityAuditService.start(temporaryDirectory, 7, 1);
        try {
            ConfigHandler.config.enableCommandAnnouncements.set(true);
            ConfigHandler.config.commandAnnouncementAllowedCommands.set("say");
            ConfigHandler.config.commandAnnouncementDeniedCommands.set("");
            manager.fireCommand(server, announcement("say hello"));
        } finally {
            ConfigHandler.config.enableCommandAnnouncements.set(previousEnabled);
            ConfigHandler.config.commandAnnouncementAllowedCommands.set(previousAllowed);
            ConfigHandler.config.commandAnnouncementDeniedCommands.set(previousDenied);
            SecurityAuditService.shutdown();
        }

        JsonObject audit = onlyAuditEvent();
        assertEquals("success", audit.get("result").getAsString());
        assertEquals("result 1", audit.get("reasonCode").getAsString());
        assertEquals("say", audit.get("origin").getAsString());
    }

    @Test
    void malformedReloadPreservesLiveAnnouncementsAndRejectsMutation() throws Exception {
        AnnouncementManager manager = new AnnouncementManager();
        TextAnnouncement original = new TextAnnouncement(
                "original",
                "notice",
                60L,
                true,
                "@a",
                true,
                0L);
        assertTrue(manager.add(original));
        Files.writeString(
                temporaryDirectory.resolve("announcements.json"),
                """
                        {"domain":"announcements","schemaVersion":2,
                        "data":{"text":[{"id":"bad","message":"notice","intervalSeconds":0,
                        "toggleable":true,"target":"@a","enabled":true,"offsetSeconds":0}],
                        "commands":[]}}
                        """);

        manager.load(temporaryDirectory);

        assertEquals(
                com.enviouse.sef.storage.repository.StorageRepository.RepositoryState.RECOVERY,
                manager.announcementState());
        assertEquals(List.of(original), manager.getTextAnnouncements());
        assertThrows(IllegalStateException.class, () -> manager.add(new TextAnnouncement(
                "blocked",
                "notice",
                60L,
                true,
                "@a",
                true,
                0L)));
    }

    private JsonObject onlyAuditEvent() throws Exception {
        Path auditFile = temporaryDirectory.resolve("audit").resolve("security-audit.jsonl");
        List<String> events = Files.readAllLines(auditFile);
        assertEquals(1, events.size());
        return JsonParser.parseString(events.getFirst()).getAsJsonObject();
    }

    private static CommandAnnouncement announcement(String command) {
        return new CommandAnnouncement(
                "notice",
                command,
                60L,
                true,
                0L,
                CommandSourcePolicy.SERVER,
                "console",
                "2026-07-26T12:00:00Z");
    }
}
