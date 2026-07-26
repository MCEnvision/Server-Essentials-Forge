package com.enviouse.sef.social;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.message.MessageService;
import com.enviouse.sef.permissions.PermissionService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class SocialCommands {
    private SocialCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (!ConfigHandler.config.enableSocialEssentials.get()) {
            return;
        }
        registerPreferences(dispatcher);
        if (ConfigHandler.config.enableSocialSpy.get()) {
            registerSocialSpy(dispatcher);
        }
    }

    private static void registerPreferences(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("msgtoggle")
                .requires(source -> PermissionService.has(source, PermissionsHandler.messageToggleCommand))
                .executes(context -> toggleMessages(context.getSource())));
        dispatcher.register(Commands.literal("rtoggle")
                .requires(source -> PermissionService.has(source, PermissionsHandler.replyToggleCommand))
                .executes(context -> toggleReplies(context.getSource())));
        dispatcher.register(Commands.literal("ignore")
                .requires(source -> PermissionService.has(source, PermissionsHandler.ignoreCommand))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> ignore(
                                context.getSource(),
                                EntityArgument.getPlayer(context, "player")))));
        dispatcher.register(Commands.literal("ignorelist")
                .requires(source -> PermissionService.has(source, PermissionsHandler.ignoreListCommand))
                .executes(context -> ignoreList(context.getSource())));
    }

    private static void registerSocialSpy(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("socialspy")
                .requires(source -> PermissionService.has(source, PermissionsHandler.socialSpyCommand))
                .executes(context -> toggleSpy(context.getSource()))
                .then(Commands.literal("on").executes(context -> setSpy(context.getSource(), true)))
                .then(Commands.literal("off").executes(context -> setSpy(context.getSource(), false)))
                .then(Commands.literal("toggle").executes(context -> toggleSpy(context.getSource())))
                .then(Commands.literal("status")
                        .requires(source -> PermissionService.has(source, PermissionsHandler.socialSpyStatus))
                        .executes(context -> status(context.getSource())))
                .then(Commands.literal("recent")
                        .requires(source -> PermissionService.has(source, PermissionsHandler.socialSpyRecent))
                        .executes(context -> recent(context.getSource(), 10))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 500))
                                .executes(context -> recent(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "count")))))
                .then(Commands.literal("everyone")
                        .requires(source -> PermissionService.has(source, PermissionsHandler.socialSpyEveryone))
                        .executes(context -> audience(context.getSource(), SocialRepository.SpyAudience.EVERYONE, true))
                        .then(Commands.literal("on")
                                .executes(context -> audience(
                                        context.getSource(), SocialRepository.SpyAudience.EVERYONE, true)))
                        .then(Commands.literal("off")
                                .executes(context -> setSpy(context.getSource(), false))))
                .then(Commands.literal("selected")
                        .requires(source -> PermissionService.has(source, PermissionsHandler.socialSpySelected))
                        .then(Commands.literal("list").executes(context -> selectedList(context.getSource())))
                        .then(Commands.literal("clear").executes(context -> selectedClear(context.getSource())))
                        .then(Commands.literal("add")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> selected(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "player"),
                                                true,
                                                false))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> selected(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "player"),
                                                false,
                                                false))))
                        .then(Commands.literal("match")
                                .then(Commands.literal("sender")
                                        .executes(context -> match(
                                                context.getSource(), SocialRepository.SpyMatch.SENDER)))
                                .then(Commands.literal("recipient")
                                        .executes(context -> match(
                                                context.getSource(), SocialRepository.SpyMatch.RECIPIENT)))
                                .then(Commands.literal("either")
                                        .executes(context -> match(
                                                context.getSource(), SocialRepository.SpyMatch.EITHER)))))
                .then(Commands.literal("scope")
                        .then(Commands.literal("metadata")
                                .requires(source -> PermissionService.has(
                                        source, PermissionsHandler.socialSpyScopeMetadata))
                                .executes(context -> content(context.getSource(), false)))
                        .then(Commands.literal("content")
                                .requires(source -> PermissionService.has(
                                        source, PermissionsHandler.socialSpyScopeContent))
                                .executes(context -> content(context.getSource(), true))))
                .then(Commands.literal("filter")
                        .requires(source -> PermissionService.has(source, PermissionsHandler.socialSpyFilter))
                        .then(Commands.literal("reset").executes(context -> routesReset(context.getSource())))
                        .then(Commands.literal("route")
                                .then(Commands.literal("add")
                                        .then(Commands.argument("route", StringArgumentType.word())
                                                .executes(context -> route(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "route"),
                                                        true))))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("route", StringArgumentType.word())
                                                .executes(context -> route(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "route"),
                                                        false))))))
                .then(Commands.literal("format")
                        .then(Commands.literal("preview")
                                .requires(source -> PermissionService.has(
                                        source, PermissionsHandler.socialSpyFormatPreview))
                                .executes(context -> formatPreview(context.getSource()))))
                .then(Commands.argument("player", EntityArgument.player())
                        .requires(source -> PermissionService.has(source, PermissionsHandler.socialSpyPlayer))
                        .executes(context -> selected(
                                context.getSource(),
                                EntityArgument.getPlayer(context, "player"),
                                true,
                                true))
                        .then(Commands.literal("on")
                                .executes(context -> selected(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "player"),
                                        true,
                                        true)))
                        .then(Commands.literal("off")
                                .executes(context -> selected(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "player"),
                                        false,
                                        true))));
        dispatcher.register(root);
    }

    private static int toggleMessages(CommandSourceStack source) {
        ServerPlayer player = player(source);
        if (player == null) return 0;
        return KernelCommandExecutor.execute(source, "sef:social.message.toggle", Map.of(), () -> {
            SocialRepository.SocialPreferences current = KernelServices.social().preferences(player.getUUID());
            KernelServices.social().updatePreferences(current.withMessagesEnabled(!current.messagesEnabled()));
            success(source, "Private messages " + (!current.messagesEnabled() ? "enabled." : "disabled."));
            return 1;
        });
    }

    private static int toggleReplies(CommandSourceStack source) {
        ServerPlayer player = player(source);
        if (player == null) return 0;
        return KernelCommandExecutor.execute(source, "sef:social.reply.toggle", Map.of(), () -> {
            SocialRepository.SocialPreferences current = KernelServices.social().preferences(player.getUUID());
            KernelServices.social().updatePreferences(current.withRepliesEnabled(!current.repliesEnabled()));
            success(source, "Private message replies " + (!current.repliesEnabled() ? "enabled." : "disabled."));
            return 1;
        });
    }

    private static int ignore(CommandSourceStack source, ServerPlayer target) {
        ServerPlayer player = player(source);
        if (player == null || player == target) return 0;
        return KernelCommandExecutor.execute(
                source,
                "sef:social.ignore",
                Map.of("operation", "toggle"),
                java.util.List.of(target.getUUID()),
                false,
                () -> {
                    boolean ignored = KernelServices.social().ignores(player.getUUID(), target.getUUID());
                    KernelServices.social().setIgnored(player.getUUID(), target.getUUID(), !ignored);
                    success(source, (ignored ? "Unignored " : "Ignored ")
                            + target.getGameProfile().getName() + ".");
                    return 1;
                });
    }

    private static int ignoreList(CommandSourceStack source) {
        ServerPlayer player = player(source);
        if (player == null) return 0;
        return KernelCommandExecutor.execute(source, "sef:social.ignore", Map.of("operation", "list"), () -> {
            Set<UUID> ignored = KernelServices.social().preferences(player.getUUID()).ignoredPlayers();
            info(source, ignored.isEmpty() ? "Your ignore list is empty." : "Ignored player UUIDs. " + ignored);
            return Math.max(1, ignored.size());
        }, PermissionsHandler.ignoreListCommand);
    }

    private static int toggleSpy(CommandSourceStack source) {
        ServerPlayer player = player(source);
        if (player == null) return 0;
        return setSpy(source, !KernelServices.social().preferences(player.getUUID()).socialSpyRequested());
    }

    private static int setSpy(CommandSourceStack source, boolean enabled) {
        ServerPlayer player = player(source);
        if (player == null) return 0;
        return KernelCommandExecutor.execute(source, "sef:social.spy", Map.of(
                "operation", enabled ? "enable" : "disable"), () -> {
            SocialRepository.SocialPreferences current = KernelServices.social().preferences(player.getUUID());
            if (enabled && !PermissionService.has(player, PermissionsHandler.socialSpyViewMetadata)) {
                fail(source, "Social spy metadata permission is required.");
                return 0;
            }
            KernelServices.social().updatePreferences(current.withSpy(
                    enabled, current.spyAudience(), current.spyMatch(), current.spyContent(),
                    current.spySelectedPlayers(), current.spyRoutes()));
            if (!enabled) {
                KernelServices.observations().clear(player.getUUID());
            }
            success(source, "Social spy requested state " + (enabled ? "enabled." : "disabled."));
            return 1;
        });
    }

    private static int audience(
            CommandSourceStack source,
            SocialRepository.SpyAudience audience,
            boolean enable
    ) {
        ServerPlayer player = player(source);
        if (player == null) return 0;
        return KernelCommandExecutor.execute(source, "sef:social.spy", Map.of(
                "operation", "audience",
                "audience", audience.name().toLowerCase(java.util.Locale.ROOT)), () -> {
            SocialRepository.SocialPreferences current = KernelServices.social().preferences(player.getUUID());
            KernelServices.social().updatePreferences(current.withSpy(
                    enable, audience, current.spyMatch(), current.spyContent(),
                    current.spySelectedPlayers(), current.spyRoutes()));
            return statusOutput(source);
        }, PermissionsHandler.socialSpyEveryone);
    }

    private static int selected(
            CommandSourceStack source,
            ServerPlayer target,
            boolean add,
            boolean replace
    ) {
        ServerPlayer player = player(source);
        if (player == null) return 0;
        return KernelCommandExecutor.execute(
                source,
                "sef:social.spy",
                Map.of("operation", add ? "select" : "deselect"),
                java.util.List.of(target.getUUID()),
                false,
                () -> {
                    SocialRepository.SocialPreferences current = KernelServices.social().preferences(player.getUUID());
                    Set<UUID> selected = replace
                            ? new LinkedHashSet<>()
                            : new LinkedHashSet<>(current.spySelectedPlayers());
                    if (add) selected.add(target.getUUID());
                    else selected.remove(target.getUUID());
                    if (selected.size() > 32) {
                        fail(source, "Social spy selected-player limit reached.");
                        return 0;
                    }
                    KernelServices.social().updatePreferences(current.withSpy(
                            !selected.isEmpty(), SocialRepository.SpyAudience.SELECTED, current.spyMatch(),
                            current.spyContent(), selected, current.spyRoutes()));
                    return statusOutput(source);
                },
                replace ? PermissionsHandler.socialSpyPlayer : PermissionsHandler.socialSpySelected);
    }

    private static int selectedClear(CommandSourceStack source) {
        ServerPlayer player = player(source);
        if (player == null) return 0;
        return KernelCommandExecutor.execute(source, "sef:social.spy", Map.of("operation", "selected_clear"), () -> {
            SocialRepository.SocialPreferences current = KernelServices.social().preferences(player.getUUID());
            KernelServices.social().updatePreferences(current.withSpy(
                    false, SocialRepository.SpyAudience.SELECTED, current.spyMatch(),
                    current.spyContent(), Set.of(), current.spyRoutes()));
            return statusOutput(source);
        }, PermissionsHandler.socialSpySelected);
    }

    private static int selectedList(CommandSourceStack source) {
        ServerPlayer player = player(source);
        if (player == null) return 0;
        return KernelCommandExecutor.execute(source, "sef:social.spy", Map.of("operation", "selected_list"), () -> {
            Set<UUID> selected = KernelServices.social().preferences(player.getUUID()).spySelectedPlayers();
            info(source, selected.isEmpty()
                    ? "No selected social spy players."
                    : "Selected player UUIDs. " + selected);
            return Math.max(1, selected.size());
        }, PermissionsHandler.socialSpySelected);
    }

    private static int match(CommandSourceStack source, SocialRepository.SpyMatch match) {
        ServerPlayer player = player(source);
        if (player == null) return 0;
        return KernelCommandExecutor.execute(source, "sef:social.spy", Map.of(
                "operation", "match",
                "match", match.name().toLowerCase(java.util.Locale.ROOT)), () -> {
            SocialRepository.SocialPreferences current = KernelServices.social().preferences(player.getUUID());
            KernelServices.social().updatePreferences(current.withSpy(
                    current.socialSpyRequested(), current.spyAudience(), match, current.spyContent(),
                    current.spySelectedPlayers(), current.spyRoutes()));
            return statusOutput(source);
        }, PermissionsHandler.socialSpySelected);
    }

    private static int content(CommandSourceStack source, boolean content) {
        ServerPlayer player = player(source);
        if (player == null) return 0;
        if (content && !PermissionService.has(player, PermissionsHandler.socialSpyViewContent)) {
            fail(source, "Social spy content permission is required.");
            return 0;
        }
        return KernelCommandExecutor.execute(source, "sef:social.spy", Map.of(
                "operation", "scope",
                "scope", content ? "content" : "metadata"), () -> {
            SocialRepository.SocialPreferences current = KernelServices.social().preferences(player.getUUID());
            KernelServices.social().updatePreferences(current.withSpy(
                    current.socialSpyRequested(), current.spyAudience(), current.spyMatch(), content,
                    current.spySelectedPlayers(), current.spyRoutes()));
            return statusOutput(source);
        }, content ? PermissionsHandler.socialSpyScopeContent : PermissionsHandler.socialSpyScopeMetadata);
    }

    private static int route(CommandSourceStack source, String route, boolean add) {
        ServerPlayer player = player(source);
        if (player == null) return 0;
        return KernelCommandExecutor.execute(source, "sef:social.spy", Map.of(
                "operation", add ? "route_add" : "route_remove",
                "route", route), () -> {
            SocialRepository.SocialPreferences current = KernelServices.social().preferences(player.getUUID());
            Set<String> routes = new LinkedHashSet<>(current.spyRoutes());
            if (add) routes.add(route);
            else routes.remove(route.toLowerCase(java.util.Locale.ROOT));
            try {
                KernelServices.social().updatePreferences(current.withSpy(
                        current.socialSpyRequested(), current.spyAudience(), current.spyMatch(),
                        current.spyContent(), current.spySelectedPlayers(), routes));
            } catch (IllegalArgumentException exception) {
                fail(source, exception.getMessage());
                return 0;
            }
            return statusOutput(source);
        }, PermissionsHandler.socialSpyFilter);
    }

    private static int routesReset(CommandSourceStack source) {
        ServerPlayer player = player(source);
        if (player == null) return 0;
        return KernelCommandExecutor.execute(source, "sef:social.spy", Map.of("operation", "filter_reset"), () -> {
            SocialRepository.SocialPreferences current = KernelServices.social().preferences(player.getUUID());
            KernelServices.social().updatePreferences(current.withSpy(
                    current.socialSpyRequested(), current.spyAudience(), current.spyMatch(),
                    current.spyContent(), current.spySelectedPlayers(), Set.of()));
            return statusOutput(source);
        }, PermissionsHandler.socialSpyFilter);
    }

    private static int status(CommandSourceStack source) {
        ServerPlayer player = player(source);
        if (player == null) return 0;
        return KernelCommandExecutor.execute(source, "sef:social.spy", Map.of("operation", "status"),
                () -> statusOutput(source), PermissionsHandler.socialSpyStatus);
    }

    private static int statusOutput(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SocialRepository.SocialPreferences current = KernelServices.social().preferences(player.getUUID());
        boolean active = current.socialSpyRequested()
                && PermissionService.has(player, PermissionsHandler.socialSpyCommand)
                && PermissionService.has(player, PermissionsHandler.socialSpyViewMetadata)
                && (current.spyAudience() == SocialRepository.SpyAudience.EVERYONE
                    ? PermissionService.has(player, PermissionsHandler.socialSpyEveryone)
                    : PermissionService.has(player, PermissionsHandler.socialSpyPlayer));
        info(source, "Social spy requested " + current.socialSpyRequested()
                + ", active " + active
                + ", audience " + current.spyAudience().name().toLowerCase(java.util.Locale.ROOT)
                + ", match " + current.spyMatch().name().toLowerCase(java.util.Locale.ROOT)
                + ", content " + current.spyContent()
                + ", selected " + current.spySelectedPlayers().size()
                + ", routes " + current.spyRoutes().size() + ".");
        return active ? 1 : 0;
    }

    private static int recent(CommandSourceStack source, int count) {
        ServerPlayer player = player(source);
        if (player == null) return 0;
        return KernelCommandExecutor.execute(source, "sef:social.spy", Map.of(
                "operation", "recent",
                "count", Integer.toString(count)), () -> {
            var events = KernelServices.observations().recent(player.getUUID(), count);
            events.forEach(player::sendSystemMessage);
            return Math.max(1, events.size());
        }, PermissionsHandler.socialSpyRecent);
    }

    private static int formatPreview(CommandSourceStack source) {
        return KernelCommandExecutor.execute(source, "sef:social.spy", Map.of(
                "operation", "format_preview"), () -> {
            ActionResult<MessageService.Template> compiled = KernelServices.messages().compile(
                    ConfigHandler.config.socialSpyFormat.get(),
                    Set.of("from", "to", "message", "route", "timestamp"));
            if (!compiled.successful()) {
                fail(source, compiled.detail());
                return 0;
            }
            ActionResult<Component> rendered = KernelServices.messages().render(compiled.value(), Map.of(
                    "from", Component.literal("Notch"),
                    "to", Component.literal("Herobrine"),
                    "message", Component.literal("Nothing much"),
                    "route", Component.literal("sef_msg"),
                    "timestamp", Component.literal("12:00:00")));
            if (!rendered.successful()) {
                fail(source, rendered.detail());
                return 0;
            }
            source.sendSuccess(rendered::value, false);
            return 1;
        }, PermissionsHandler.socialSpyFormatPreview);
    }

    private static ServerPlayer player(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            fail(source, "This command can only be used by a player.");
        }
        return player;
    }

    private static void success(CommandSourceStack source, String value) {
        source.sendSuccess(() -> TextFormatter.stringToFormattedText("&a" + value), false);
    }

    private static void info(CommandSourceStack source, String value) {
        source.sendSuccess(() -> TextFormatter.stringToFormattedText("&7" + value), false);
    }

    private static void fail(CommandSourceStack source, String value) {
        source.sendFailure(TextFormatter.stringToFormattedText("&c" + value));
    }
}
