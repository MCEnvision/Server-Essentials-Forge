package com.enviouse.sef.chat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatMessageManagerTest {
    private final AtomicLong now = new AtomicLong(1_000_000L);

    @AfterEach
    void restoreClock() {
        ChatMessageManager.resetForTests(System::currentTimeMillis);
    }

    @Test
    void opaqueTokenIsBoundToTheExactRecipient() {
        ChatMessageManager.resetForTests(now::get);
        UUID sender = UUID.randomUUID();
        UUID recipient = UUID.randomUUID();
        UUID other = UUID.randomUUID();

        String token = ChatMessageManager.issueToken(
                sender,
                recipient,
                "Sender",
                "Sender",
                "private context");

        assertEquals(24, token.length());
        assertTrue(token.matches("[A-Za-z0-9_-]{24}"));
        assertEquals(sender, ChatMessageManager.resolve(token, recipient).senderUuid());
        assertNull(ChatMessageManager.resolve(token, other));
    }

    @Test
    void tokenExpiresWithAUniformUnavailableResult() {
        ChatMessageManager.resetForTests(now::get);
        UUID recipient = UUID.randomUUID();
        String token = ChatMessageManager.issueToken(
                UUID.randomUUID(),
                recipient,
                "Sender",
                "Sender",
                "message");

        now.addAndGet(ChatMessageManager.TOKEN_TTL_MILLIS);

        assertNull(ChatMessageManager.resolve(token, recipient));
        assertEquals(0, ChatMessageManager.tokenCount(recipient));
    }

    @Test
    void successfulConsumptionIsSingleUse() {
        ChatMessageManager.resetForTests(now::get);
        UUID recipient = UUID.randomUUID();
        String token = ChatMessageManager.issueToken(
                UUID.randomUUID(),
                recipient,
                "Sender",
                "Sender",
                "message");

        assertTrue(ChatMessageManager.consume(token, recipient));
        assertFalse(ChatMessageManager.consume(token, recipient));
        assertNull(ChatMessageManager.resolve(token, recipient));
    }

    @Test
    void strictPerRecipientCapacityEvictsTheOldestToken() {
        ChatMessageManager.resetForTests(now::get);
        UUID recipient = UUID.randomUUID();
        String oldest = ChatMessageManager.issueToken(
                UUID.randomUUID(),
                recipient,
                "Sender",
                "Sender",
                "oldest");
        for (int index = 1; index <= ChatMessageManager.MAXIMUM_TOKENS_PER_RECIPIENT; index++) {
            now.incrementAndGet();
            ChatMessageManager.issueToken(
                    UUID.randomUUID(),
                    recipient,
                    "Sender",
                    "Sender",
                    "message " + index);
        }

        assertNull(ChatMessageManager.resolve(oldest, recipient));
        assertEquals(
                ChatMessageManager.MAXIMUM_TOKENS_PER_RECIPIENT,
                ChatMessageManager.tokenCount(recipient));
    }

    @Test
    void logoutRevokesOnlyTheRecipientsCapabilities() {
        ChatMessageManager.resetForTests(now::get);
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        String firstToken = ChatMessageManager.issueToken(
                UUID.randomUUID(), first, "Sender", "Sender", "one");
        String secondToken = ChatMessageManager.issueToken(
                UUID.randomUUID(), second, "Sender", "Sender", "two");

        ChatMessageManager.handleLogout(first);

        assertNull(ChatMessageManager.resolve(firstToken, first));
        assertEquals(second, ChatMessageManager.resolve(secondToken, second).recipientUuid());
    }
}
