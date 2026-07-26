package com.enviouse.sef.social;

import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.message.MessageService;
import com.enviouse.sef.utils.SEFUtilities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

public final class ConnectionMessageService {
    private static final Set<String> PLACEHOLDERS =
            Set.of("player", "username", "uuid", "world");
    private static final Map<Component, ServerPlayer> SUBJECTS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private ConnectionMessageService() {
    }

    public static Component render(ServerPlayer player, boolean joining, Component vanillaMessage) {
        if (!ConfigHandler.config.enableSocialEssentials.get()
                || !ConfigHandler.config.enableConnectionMessages.get()) {
            return vanillaMessage;
        }
        SocialRepository.ConnectionTemplates templates =
                KernelServices.social().connectionTemplates(player.getUUID());
        String source = joining ? templates.joinTemplate() : templates.leaveTemplate();
        if (source.isBlank()) {
            source = joining
                    ? ConfigHandler.config.defaultJoinMessage.get()
                    : ConfigHandler.config.defaultLeaveMessage.get();
        }
        if (source == null || source.isBlank()) {
            return vanillaMessage;
        }
        ActionResult<MessageService.Template> compiled =
                KernelServices.messages().compile(source, PLACEHOLDERS);
        if (!compiled.successful()) {
            return vanillaMessage;
        }
        ActionResult<Component> rendered = KernelServices.messages().render(compiled.value(), Map.of(
                "player", SEFUtilities.getFormattedPlayerName(player.getGameProfile()),
                "username", Component.literal(player.getGameProfile().getName()),
                "uuid", Component.literal(player.getUUID().toString()),
                "world", Component.literal(player.serverLevel().dimension().location().toString())));
        if (!rendered.successful()) {
            return vanillaMessage;
        }
        Component result = rendered.value();
        SUBJECTS.put(result, player);
        return result;
    }

    public static Optional<ServerPlayer> subject(Component message) {
        return Optional.ofNullable(SUBJECTS.get(message));
    }
}
