package com.enviouse.sef.chat;

import com.enviouse.sef.ServerEssentialsForge;
import net.minecraft.server.MinecraftServer;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/**
 * Issues short lived reply capabilities for chat messages.
 *
 * <p>Every capability is bound to the exact recipient that received the
 * original message. Tokens are opaque and reveal no global sequence,
 * sender identity, message content, or online state.</p>
 */
public final class ChatMessageManager {
    static final int MAXIMUM_TOKENS_PER_RECIPIENT = 64;
    static final long TOKEN_TTL_MILLIS = Duration.ofMinutes(5L).toMillis();
    private static final int TOKEN_BYTES = 18;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Map<String, ChatRecord> TOKENS = new ConcurrentHashMap<>();
    private static final Map<UUID, LinkedHashSet<String>> RECIPIENT_TOKENS =
            new ConcurrentHashMap<>();
    private static LongSupplier timeSource = System::currentTimeMillis;

    private ChatMessageManager() {
    }

    public record ChatRecord(
            String token,
            UUID senderUuid,
            UUID recipientUuid,
            String rawName,
            String formattedName,
            String message,
            long issuedAtEpochMillis,
            long expiresAtEpochMillis
    ) {
        public ChatRecord {
            Objects.requireNonNull(token, "token");
            Objects.requireNonNull(senderUuid, "senderUuid");
            Objects.requireNonNull(recipientUuid, "recipientUuid");
            rawName = bounded(rawName, "rawName", 64);
            formattedName = bounded(formattedName, "formattedName", 512);
            message = bounded(message, "message", 1_024);
            if (issuedAtEpochMillis < 0L
                    || expiresAtEpochMillis <= issuedAtEpochMillis
                    || expiresAtEpochMillis - issuedAtEpochMillis > TOKEN_TTL_MILLIS) {
                throw new IllegalArgumentException("Reply token lifetime is outside bounds");
            }
        }
    }

    public static synchronized void init(MinecraftServer server) {
        TOKENS.clear();
        RECIPIENT_TOKENS.clear();
        timeSource = System::currentTimeMillis;
        ServerEssentialsForge.LOGGER.info("[SEF] Chat reply capability manager initialized");
    }

    public static synchronized String issueToken(
            UUID senderUuid,
            UUID recipientUuid,
            String rawName,
            String formattedName,
            String message
    ) {
        Objects.requireNonNull(senderUuid, "senderUuid");
        Objects.requireNonNull(recipientUuid, "recipientUuid");
        long now = timeSource.getAsLong();
        prune(now);
        LinkedHashSet<String> recipientTokens =
                RECIPIENT_TOKENS.computeIfAbsent(recipientUuid, ignored -> new LinkedHashSet<>());
        while (recipientTokens.size() >= MAXIMUM_TOKENS_PER_RECIPIENT) {
            Iterator<String> iterator = recipientTokens.iterator();
            if (!iterator.hasNext()) {
                break;
            }
            TOKENS.remove(iterator.next());
            iterator.remove();
        }

        String token;
        do {
            byte[] bytes = new byte[TOKEN_BYTES];
            RANDOM.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } while (TOKENS.containsKey(token));

        ChatRecord record = new ChatRecord(
                token,
                senderUuid,
                recipientUuid,
                rawName,
                formattedName,
                message,
                now,
                Math.addExact(now, TOKEN_TTL_MILLIS));
        TOKENS.put(token, record);
        recipientTokens.add(token);
        return token;
    }

    public static synchronized ChatRecord resolve(String token, UUID recipientUuid) {
        long now = timeSource.getAsLong();
        prune(now);
        if (token == null || token.length() != 24 || recipientUuid == null) {
            return null;
        }
        ChatRecord record = TOKENS.get(token);
        if (record == null
                || !record.recipientUuid().equals(recipientUuid)
                || now >= record.expiresAtEpochMillis()) {
            return null;
        }
        return record;
    }

    public static synchronized boolean consume(String token, UUID recipientUuid) {
        ChatRecord record = resolve(token, recipientUuid);
        if (record == null) {
            return false;
        }
        remove(record);
        return true;
    }

    public static synchronized void handleLogout(UUID uuid) {
        LinkedHashSet<String> tokens = RECIPIENT_TOKENS.remove(uuid);
        if (tokens != null) {
            tokens.forEach(TOKENS::remove);
        }
    }

    static synchronized int tokenCount(UUID recipientUuid) {
        prune(timeSource.getAsLong());
        LinkedHashSet<String> tokens = RECIPIENT_TOKENS.get(recipientUuid);
        return tokens == null ? 0 : tokens.size();
    }

    static synchronized void resetForTests(LongSupplier source) {
        TOKENS.clear();
        RECIPIENT_TOKENS.clear();
        timeSource = Objects.requireNonNull(source, "source");
    }

    private static void prune(long now) {
        new ArrayList<>(TOKENS.values()).stream()
                .filter(record -> now >= record.expiresAtEpochMillis())
                .forEach(ChatMessageManager::remove);
    }

    private static void remove(ChatRecord record) {
        TOKENS.remove(record.token());
        LinkedHashSet<String> recipientTokens = RECIPIENT_TOKENS.get(record.recipientUuid());
        if (recipientTokens != null) {
            recipientTokens.remove(record.token());
            if (recipientTokens.isEmpty()) {
                RECIPIENT_TOKENS.remove(record.recipientUuid());
            }
        }
    }

    private static String bounded(String value, String field, int maximumLength) {
        String safe = Objects.requireNonNull(value, field);
        if (safe.length() > maximumLength
                || safe.chars().anyMatch(character -> Character.isISOControl(character)
                && character != '\n'
                && character != '\t')) {
            throw new IllegalArgumentException("Reply token " + field + " is outside bounds");
        }
        return safe;
    }
}
