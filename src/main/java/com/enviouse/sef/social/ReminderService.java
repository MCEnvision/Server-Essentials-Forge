package com.enviouse.sef.social;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.message.MessageService;
import com.enviouse.sef.utils.SEFUtilities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ReminderService {
    private static final Set<String> PLACEHOLDERS = Set.of("player", "username", "unread_mail");

    private ReminderService() {
    }

    public static int deliverLogin(ServerPlayer player, boolean firstJoin) {
        if (!ConfigHandler.config.enableSocialEssentials.get()
                || !ConfigHandler.config.enableReminders.get()) {
            return 0;
        }
        String optionalClient = ConfigHandler.config.optionalClientReminder.get();
        if (optionalClient != null && !optionalClient.isBlank()) {
            player.sendSystemMessage(TextFormatter.stringToFormattedText(optionalClient));
        }
        int delivered = 0;
        for (SocialRepository.ReminderDefinition definition : KernelServices.social().reminders()) {
            if (applies(definition, firstJoin, player) && deliver(player, definition, false)) {
                delivered++;
            }
        }
        return delivered;
    }

    public static int deliverScheduled(ServerPlayer player) {
        return deliverScheduled(List.of(player));
    }

    public static int deliverScheduled(Collection<ServerPlayer> players) {
        if (!ConfigHandler.config.enableSocialEssentials.get()
                || !ConfigHandler.config.enableReminders.get()) {
            return 0;
        }
        int delivered = 0;
        List<SocialRepository.ReminderDefinition> definitions = KernelServices.social().reminders();
        for (ServerPlayer player : players) {
            delivered += deliverScheduled(player, definitions);
        }
        return delivered;
    }

    private static int deliverScheduled(
            ServerPlayer player,
            List<SocialRepository.ReminderDefinition> definitions
    ) {
        int delivered = 0;
        for (SocialRepository.ReminderDefinition definition : definitions) {
            boolean applies = definition.audience() == SocialRepository.ReminderAudience.ALL
                    || definition.audience() == SocialRepository.ReminderAudience.COMMAND_FALLBACK
                    || definition.audience() == SocialRepository.ReminderAudience.UNREAD_MAIL
                    && KernelServices.social().unreadMail(player.getUUID()) > 0;
            if (applies && deliver(player, definition, false)) {
                delivered++;
            }
        }
        return delivered;
    }

    public static boolean deliver(ServerPlayer player, SocialRepository.ReminderDefinition definition, boolean forced) {
        if (!forced && !definition.enabled()) {
            return false;
        }
        SocialRepository.ReminderState state =
                KernelServices.social().reminderState(player.getUUID(), definition.id());
        if (!forced) {
            boolean dismissed = state.dismissed()
                    && state.acknowledgedRevision() >= definition.acknowledgementRevision();
            if (dismissed || state.deliveryCount() >= definition.maximumDeliveries()) {
                return false;
            }
            if (state.lastDeliveredAt() != null) {
                if (definition.repeatSeconds() == 0
                        || Instant.now().isBefore(state.lastDeliveredAt().plusSeconds(definition.repeatSeconds()))) {
                    return false;
                }
            }
        }
        Component message = render(player, definition);
        if (message == null) {
            return false;
        }
        player.sendSystemMessage(message);
        if (!forced) {
            KernelServices.social().updateReminderState(
                    state.delivered(Instant.now(), definition.acknowledgementRevision()));
        }
        return true;
    }

    public static Component render(ServerPlayer player, SocialRepository.ReminderDefinition definition) {
        ActionResult<MessageService.Template> compiled =
                KernelServices.messages().compile(definition.message(), PLACEHOLDERS);
        if (!compiled.successful()) {
            return null;
        }
        ActionResult<Component> rendered = KernelServices.messages().render(compiled.value(), Map.of(
                "player", SEFUtilities.getFormattedPlayerName(player.getGameProfile()),
                "username", Component.literal(player.getGameProfile().getName()),
                "unread_mail", Component.literal(Long.toString(
                        KernelServices.social().unreadMail(player.getUUID())))));
        return rendered.successful() ? rendered.value() : null;
    }

    private static boolean applies(
            SocialRepository.ReminderDefinition definition,
            boolean firstJoin,
            ServerPlayer player
    ) {
        if (!definition.enabled()) {
            return false;
        }
        return switch (definition.audience()) {
            case ALL, COMMAND_FALLBACK -> true;
            case FIRST_JOIN -> firstJoin;
            case UNREAD_MAIL -> KernelServices.social().unreadMail(player.getUUID()) > 0;
        };
    }
}
