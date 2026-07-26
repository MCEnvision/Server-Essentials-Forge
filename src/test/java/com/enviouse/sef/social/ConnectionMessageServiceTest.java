package com.enviouse.sef.social;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ConnectionMessageServiceTest {
    @AfterEach
    void clearSubjects() {
        ConnectionMessageService.clearSubjects();
    }

    @Test
    void equalComponentsRemainCorrelatedByObjectIdentity() {
        Component firstMessage = Component.literal("same");
        Component secondMessage = Component.literal("same");
        ServerPlayer firstPlayer = mock(ServerPlayer.class);
        ServerPlayer secondPlayer = mock(ServerPlayer.class);

        ConnectionMessageService.rememberSubject(firstMessage, firstPlayer);
        ConnectionMessageService.rememberSubject(secondMessage, secondPlayer);

        assertSame(firstPlayer, ConnectionMessageService.subject(firstMessage).orElseThrow());
        assertSame(secondPlayer, ConnectionMessageService.subject(secondMessage).orElseThrow());
        assertTrue(ConnectionMessageService.subject(Component.literal("same")).isEmpty());
    }
}
