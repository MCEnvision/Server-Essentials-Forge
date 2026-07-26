package com.enviouse.sef.social;

import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.message.MessageService;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.utils.SEFUtilities;
import com.enviouse.sef.vanish.VanishUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.time.Instant;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class ObservationService implements PrivateMessageObservationAdapter {
    private static final Set<String> PLACEHOLDERS = Set.of("from", "to", "message", "route", "timestamp");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneOffset.UTC);

    private final SocialRepository repository;
    private final MessageService messages;
    private final ObservationLimiter limiter = new ObservationLimiter(4096, Duration.ofMinutes(5));
    private final Map<UUID, Deque<Component>> recent = new LinkedHashMap<>();
    private String compiledSource = "";
    private MessageService.Template compiledTemplate;

    public ObservationService(SocialRepository repository, MessageService messages) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.messages = Objects.requireNonNull(messages, "messages");
    }

    public void publishPrivateMessage(
            MinecraftServer server,
            String route,
            ServerPlayer sender,
            ServerPlayer recipient,
            Component content
    ) {
        publish(UUID.randomUUID(), server, route, sender, recipient, content);
    }

    @Override
    public void publish(
            UUID eventId,
            MinecraftServer server,
            String route,
            ServerPlayer sender,
            ServerPlayer recipient,
            Component content
    ) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(recipient, "recipient");
        String routeId = normalizeRoute(route);
        if (!server.isSameThread()) {
            UUID senderId = sender.getUUID();
            UUID recipientId = recipient.getUUID();
            Component snapshot = content == null ? Component.empty() : content.copy();
            server.execute(() -> {
                ServerPlayer currentSender = server.getPlayerList().getPlayer(senderId);
                ServerPlayer currentRecipient = server.getPlayerList().getPlayer(recipientId);
                if (currentSender != null && currentRecipient != null) {
                    publish(eventId, server, routeId, currentSender, currentRecipient, snapshot);
                }
            });
            return;
        }
        Instant now = Instant.now();
        if (!limiter.acceptEvent(eventId, now)) {
            return;
        }
        for (ServerPlayer observer : server.getPlayerList().getPlayers()) {
            if (observer == sender || observer == recipient) {
                continue;
            }
            SocialRepository.SocialPreferences preference = repository.preferences(observer.getUUID());
            if (!active(observer, preference, sender, recipient, routeId)) {
                continue;
            }
            if (!limiter.acceptObserver(
                    observer.getUUID(),
                    now,
                    ConfigHandler.config.socialSpyEventsPerSecond.get())) {
                continue;
            }
            boolean mayViewContent = preference.spyContent()
                    && PermissionService.has(observer, PermissionsHandler.socialSpyViewContent);
            Component rendered = render(
                    sender,
                    recipient,
                    mayViewContent ? sanitize(content) : Component.literal("[content hidden]"),
                    routeId);
            observer.sendSystemMessage(rendered);
            remember(observer.getUUID(), rendered);
        }
    }

    public synchronized List<Component> recent(UUID observerId, int requested) {
        int maximum = Math.max(0, ConfigHandler.config.socialSpyRecentLimit.get());
        int count = Math.max(0, Math.min(requested, maximum));
        Deque<Component> events = recent.get(observerId);
        if (events == null || count == 0) {
            return List.of();
        }
        List<Component> snapshot = new ArrayList<>(events);
        int from = Math.max(0, snapshot.size() - count);
        return List.copyOf(snapshot.subList(from, snapshot.size()));
    }

    public synchronized void clear(UUID observerId) {
        recent.remove(observerId);
        limiter.clearObserver(observerId);
    }

    public synchronized void clearAll() {
        recent.clear();
        limiter.clear();
    }

    private boolean active(
            ServerPlayer observer,
            SocialRepository.SocialPreferences preference,
            ServerPlayer sender,
            ServerPlayer recipient,
            String route
    ) {
        if (!ConfigHandler.config.enableSocialEssentials.get()
                || !ConfigHandler.config.enableSocialSpy.get()
                || !preference.socialSpyRequested()
                || !PermissionService.has(observer, PermissionsHandler.socialSpyCommand)
                || !PermissionService.has(observer, PermissionsHandler.socialSpyViewMetadata)) {
            return false;
        }
        if ((!preference.spyRoutes().isEmpty() && !preference.spyRoutes().contains(route))
                || hidden(observer, sender) || hidden(observer, recipient)) {
            return false;
        }
        boolean exempt = PermissionService.has(sender, PermissionsHandler.socialSpyExempt)
                || PermissionService.has(recipient, PermissionsHandler.socialSpyExempt);
        if (exempt && !PermissionService.has(observer, PermissionsHandler.socialSpyViewExempt)) {
            return false;
        }
        if (preference.spyAudience() == SocialRepository.SpyAudience.EVERYONE) {
            return PermissionService.has(observer, PermissionsHandler.socialSpyEveryone);
        }
        if (!PermissionService.has(observer, PermissionsHandler.socialSpyPlayer)) {
            return false;
        }
        Set<UUID> selected = preference.spySelectedPlayers();
        return switch (preference.spyMatch()) {
            case SENDER -> selected.contains(sender.getUUID());
            case RECIPIENT -> selected.contains(recipient.getUUID());
            case EITHER -> selected.contains(sender.getUUID()) || selected.contains(recipient.getUUID());
        };
    }

    private static boolean hidden(ServerPlayer observer, ServerPlayer subject) {
        return VanishUtil.isVanished(subject, observer)
                && !PermissionService.has(observer, PermissionsHandler.socialSpyViewVanished);
    }

    private Component render(
            ServerPlayer sender,
            ServerPlayer recipient,
            Component content,
            String route
    ) {
        MessageService.Template template = template();
        ActionResult<Component> rendered = messages.render(template, Map.of(
                "from", SEFUtilities.getFormattedPlayerName(sender.getGameProfile()),
                "to", SEFUtilities.getFormattedPlayerName(recipient.getGameProfile()),
                "message", content,
                "route", Component.literal(route),
                "timestamp", Component.literal(TIME.format(Instant.now()))));
        return rendered.successful()
                ? rendered.value()
                : Component.literal("[" + sender.getGameProfile().getName() + "] -> ["
                        + recipient.getGameProfile().getName() + "]: " + content.getString());
    }

    private synchronized MessageService.Template template() {
        String source = ConfigHandler.config.socialSpyFormat.get();
        if (compiledTemplate != null && source.equals(compiledSource)) {
            return compiledTemplate;
        }
        ActionResult<MessageService.Template> compiled = messages.compile(source, PLACEHOLDERS);
        if (compiled.successful()) {
            compiledSource = source;
            compiledTemplate = compiled.value();
        } else if (compiledTemplate == null) {
            compiledSource = "[{from}] -> [{to}]: {message}";
            compiledTemplate = messages.compile(compiledSource, PLACEHOLDERS).value();
        }
        return compiledTemplate;
    }

    private synchronized void remember(UUID observerId, Component event) {
        int maximum = Math.max(0, ConfigHandler.config.socialSpyRecentLimit.get());
        if (maximum == 0) {
            return;
        }
        Deque<Component> events = recent.computeIfAbsent(observerId, ignored -> new ArrayDeque<>());
        events.addLast(event.copy());
        while (events.size() > maximum) {
            events.removeFirst();
        }
    }

    private static Component sanitize(Component content) {
        String text = content == null ? "" : content.getString();
        int maximum = Math.max(1, ConfigHandler.config.privateMessageMaximumLength.get());
        if (text.length() > maximum) {
            text = text.substring(0, maximum);
        }
        return Component.literal(text.replace('\n', ' ').replace('\r', ' '));
    }

    private static String normalizeRoute(String value) {
        String route = Objects.requireNonNull(value, "route").trim().toLowerCase(java.util.Locale.ROOT);
        if (!route.matches("[a-z0-9][a-z0-9_.-]{0,63}")) {
            throw new IllegalArgumentException("invalid observation route");
        }
        return route;
    }
}
