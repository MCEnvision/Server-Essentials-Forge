package com.enviouse.sef.social;

import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.message.MessageService;
import com.enviouse.sef.utils.SEFUtilities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ConnectionMessageService {
    private static final int MAXIMUM_SUBJECTS = 2_048;
    private static final Set<String> PLACEHOLDERS =
            Set.of("player", "username", "uuid", "world");
    private static final ReferenceQueue<Component> STALE_MESSAGES = new ReferenceQueue<>();
    private static final Map<IdentityWeakReference, WeakReference<ServerPlayer>> SUBJECTS = new HashMap<>();

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
        rememberSubject(result, player);
        return result;
    }

    public static synchronized Optional<ServerPlayer> subject(Component message) {
        prune();
        WeakReference<ServerPlayer> reference = SUBJECTS.get(new IdentityWeakReference(message, null));
        return reference == null ? Optional.empty() : Optional.ofNullable(reference.get());
    }

    static synchronized void rememberSubject(Component message, ServerPlayer player) {
        prune();
        SUBJECTS.put(
                new IdentityWeakReference(message, STALE_MESSAGES),
                new WeakReference<>(player));
        while (SUBJECTS.size() > MAXIMUM_SUBJECTS) {
            SUBJECTS.remove(SUBJECTS.keySet().iterator().next());
        }
    }

    static synchronized void clearSubjects() {
        SUBJECTS.clear();
        while (STALE_MESSAGES.poll() != null) {
        }
    }

    private static void prune() {
        IdentityWeakReference reference;
        while ((reference = (IdentityWeakReference) STALE_MESSAGES.poll()) != null) {
            SUBJECTS.remove(reference);
        }
        SUBJECTS.entrySet().removeIf(entry -> entry.getValue().get() == null);
    }

    private static final class IdentityWeakReference extends WeakReference<Component> {
        private final int identityHash;

        private IdentityWeakReference(Component referent, ReferenceQueue<Component> queue) {
            super(referent, queue);
            identityHash = System.identityHashCode(referent);
        }

        @Override
        public int hashCode() {
            return identityHash;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof IdentityWeakReference reference)) {
                return false;
            }
            Component mine = get();
            return mine != null && mine == reference.get();
        }
    }
}
