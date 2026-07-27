package com.enviouse.sef.control;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.vanish.VanishUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.text.Normalizer;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MentionService {
    private static final int MAXIMUM_DELIVERY_KEYS = 4096;
    private static final int MAXIMUM_MENTIONS_PER_MESSAGE = 8;
    private static final Pattern MENTION = Pattern.compile(
            "(?<![A-Za-z0-9_])@([A-Za-z0-9_]{1,32})",
            Pattern.CASE_INSENSITIVE);
    private static final Map<DeliveryKey, Long> LAST_DELIVERY = new LinkedHashMap<>();

    private MentionService() {
    }

    public static void deliver(ServerPlayer sender, String message) {
        if (sender == null || message == null || message.isBlank()) {
            return;
        }
        Set<String> requested = parse(message);
        if (requested.isEmpty()) {
            return;
        }
        int delivered = 0;
        for (ServerPlayer target : sender.server.getPlayerList().getPlayers()) {
            if (delivered >= MAXIMUM_MENTIONS_PER_MESSAGE
                    || target.getUUID().equals(sender.getUUID())
                    || !matches(target, requested)
                    || !allowed(sender, target)) {
                continue;
            }
            long cooldown = activeCooldownSeconds();
            DeliveryKey key = new DeliveryKey(sender.getUUID(), target.getUUID());
            long now = Instant.now().getEpochSecond();
            synchronized (MentionService.class) {
                long previous = LAST_DELIVERY.getOrDefault(key, 0L);
                if (previous + cooldown > now) {
                    continue;
                }
                LAST_DELIVERY.put(key, now);
                while (LAST_DELIVERY.size() > MAXIMUM_DELIVERY_KEYS) {
                    LAST_DELIVERY.remove(LAST_DELIVERY.keySet().iterator().next());
                }
            }
            target.displayClientMessage(Component.literal(
                    sender.getGameProfile().getName() + " mentioned you"), true);
            if (preference(target.getUUID(), "mention_sound", true)) {
                target.playNotifySound(
                        SoundEvents.EXPERIENCE_ORB_PICKUP,
                        SoundSource.PLAYERS,
                        0.5F,
                        1.25F);
            }
            delivered++;
        }
    }

    public static synchronized void logout(UUID playerId) {
        LAST_DELIVERY.keySet().removeIf(key ->
                key.senderId().equals(playerId) || key.targetId().equals(playerId));
    }

    public static synchronized void clear() {
        LAST_DELIVERY.clear();
    }

    static Set<String> parse(String message) {
        Set<String> result = new HashSet<>();
        Matcher matcher = MENTION.matcher(message);
        while (matcher.find() && result.size() < MAXIMUM_MENTIONS_PER_MESSAGE) {
            result.add(normalize(matcher.group(1)));
        }
        return Set.copyOf(result);
    }

    private static boolean matches(ServerPlayer target, Set<String> requested) {
        if (requested.contains(normalize(target.getGameProfile().getName()))) {
            return true;
        }
        return KernelServices.profiles().find(target.getUUID())
                .map(profile -> visibleIdentity(profile.nickname()))
                .filter(value -> !value.isBlank())
                .map(MentionService::normalize)
                .map(requested::contains)
                .orElse(false);
    }

    private static boolean allowed(ServerPlayer sender, ServerPlayer target) {
        String mode = KernelServices.communityState().find(
                        "mention_mode",
                        target.getUUID(),
                        "preference")
                .map(CommunityStateRepository.Entry::value)
                .orElse("all");
        if (mode.equals("off")
                || CommunityCommands.interactionBlocked(
                target.getUUID(),
                sender.getUUID(),
                "mentions")) {
            return false;
        }
        if (mode.equals("friends") && KernelServices.communityState().find(
                "friend",
                target.getUUID(),
                sender.getUUID().toString()).isEmpty()) {
            return false;
        }
        if (mode.equals("staff") && !KernelCommandExecutor.canUse(
                sender.createCommandSourceStack(),
                "sef:control.mentions.manage")) {
            return false;
        }
        boolean senderVanished = VanishUtil.isVanished(sender);
        boolean targetVanished = VanishUtil.isVanished(target);
        return !targetVanished || VanishUtil.playerAllowedToSeeOther(
                sender,
                target,
                senderVanished,
                true);
    }

    private static long activeCooldownSeconds() {
        return KernelServices.serverControls().records("mentions").stream()
                .filter(record -> record.state() == ServerControlRepository.RecordState.ACTIVE)
                .filter(record -> record.expiresAt() == null || record.expiresAt().isAfter(Instant.now()))
                .map(record -> record.metadata().getOrDefault("field.cooldown_seconds", "5"))
                .mapToLong(value -> {
                    try {
                        return Math.clamp(Long.parseLong(value), 0L, 3600L);
                    } catch (NumberFormatException exception) {
                        return 5L;
                    }
                })
                .min()
                .orElse(5L);
    }

    private static boolean preference(UUID playerId, String type, boolean fallback) {
        return KernelServices.communityState().find(type, playerId, "preference")
                .map(CommunityStateRepository.Entry::value)
                .map(Boolean::parseBoolean)
                .orElse(fallback);
    }

    private static String visibleIdentity(String value) {
        return TextFormatter.stringToFormattedText(value == null ? "" : value).getString();
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .strip()
                .toLowerCase(Locale.ROOT);
    }

    private record DeliveryKey(UUID senderId, UUID targetId) {
    }
}
