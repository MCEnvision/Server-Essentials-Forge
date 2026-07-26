package com.enviouse.sef.social;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.permissions.PermissionService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ReminderCommands {
    private ReminderCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (!ConfigHandler.config.enableSocialEssentials.get()
                || !ConfigHandler.config.enableReminders.get()) {
            return;
        }
        dispatcher.register(Commands.literal("reminders")
                .requires(source -> PermissionService.has(source, PermissionsHandler.remindersCommand))
                .executes(context -> list(context.getSource(), 1))
                .then(Commands.literal("list")
                        .executes(context -> list(context.getSource(), 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> list(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "page")))))
                .then(Commands.literal("dismiss")
                        .requires(source -> PermissionService.has(source, PermissionsHandler.reminderDismiss))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(context -> dismissal(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "id"),
                                        true))))
                .then(Commands.literal("restore")
                        .requires(source -> PermissionService.has(source, PermissionsHandler.reminderDismiss))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(context -> dismissal(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "id"),
                                        false)))));

        dispatcher.register(Commands.literal("reminder")
                .requires(source -> PermissionService.has(source, PermissionsHandler.reminderManage)
                        || PermissionService.has(source, PermissionsHandler.reminderSend))
                .then(Commands.literal("create")
                        .requires(source -> PermissionService.has(source, PermissionsHandler.reminderManage))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("message", StringArgumentType.greedyString())
                                        .executes(context -> create(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "id"),
                                                StringArgumentType.getString(context, "message"))))))
                .then(Commands.literal("delete")
                        .requires(source -> PermissionService.has(source, PermissionsHandler.reminderManage))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(context -> delete(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "id")))))
                .then(Commands.literal("message")
                        .requires(source -> PermissionService.has(source, PermissionsHandler.reminderManage))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("message", StringArgumentType.greedyString())
                                        .executes(context -> message(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "id"),
                                                StringArgumentType.getString(context, "message"))))))
                .then(Commands.literal("enable")
                        .requires(source -> PermissionService.has(source, PermissionsHandler.reminderManage))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(context -> enable(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "id"),
                                        true))))
                .then(Commands.literal("disable")
                        .requires(source -> PermissionService.has(source, PermissionsHandler.reminderManage))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(context -> enable(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "id"),
                                        false))))
                .then(Commands.literal("repeat")
                        .requires(source -> PermissionService.has(source, PermissionsHandler.reminderManage))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("seconds", LongArgumentType.longArg(0, 31_536_000L))
                                        .executes(context -> repeat(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "id"),
                                                LongArgumentType.getLong(context, "seconds"))))))
                .then(Commands.literal("count")
                        .requires(source -> PermissionService.has(source, PermissionsHandler.reminderManage))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("maximum", IntegerArgumentType.integer(1, 1000))
                                        .executes(context -> count(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "id"),
                                                IntegerArgumentType.getInteger(context, "maximum"))))))
                .then(Commands.literal("audience")
                        .requires(source -> PermissionService.has(source, PermissionsHandler.reminderManage))
                        .then(audience("all", SocialRepository.ReminderAudience.ALL))
                        .then(audience("first_join", SocialRepository.ReminderAudience.FIRST_JOIN))
                        .then(audience("command_fallback", SocialRepository.ReminderAudience.COMMAND_FALLBACK))
                        .then(audience("unread_mail", SocialRepository.ReminderAudience.UNREAD_MAIL)))
                .then(Commands.literal("send")
                        .requires(source -> PermissionService.has(source, PermissionsHandler.reminderSend))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("players", EntityArgument.players())
                                        .executes(context -> send(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "id"),
                                                List.copyOf(EntityArgument.getPlayers(context, "players"))))))));

        dispatcher.register(Commands.literal("welcome")
                .then(Commands.literal("preview")
                        .requires(source -> PermissionService.has(source, PermissionsHandler.welcomePreview))
                        .executes(context -> welcome(context.getSource(), null)))
                .then(Commands.literal("send")
                        .requires(source -> PermissionService.has(source, PermissionsHandler.welcomeSend))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> welcome(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "player"))))));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> audience(
            String literal,
            SocialRepository.ReminderAudience audience
    ) {
        return Commands.literal(literal)
                .then(Commands.argument("id", StringArgumentType.word())
                        .executes(context -> audience(
                                context.getSource(),
                                StringArgumentType.getString(context, "id"),
                                audience)));
    }

    private static int create(CommandSourceStack source, String id, String message) {
        UUID actor = actor(source);
        return manage(source, "create", id, () -> {
            if (KernelServices.social().reminder(id).isPresent()) {
                fail(source, "Reminder already exists.");
                return 0;
            }
            var compiled = KernelServices.messages().compile(
                    message,
                    java.util.Set.of("player", "username", "unread_mail"));
            if (!compiled.successful()) {
                fail(source, compiled.detail());
                return 0;
            }
            long usage = KernelServices.social().reminderCount();
            long quota = KernelServices.quotas().resolve(
                    new com.enviouse.sef.kernel.policy.QuotaService.Context(
                            "sef:definitions",
                            actor,
                            "server",
                            "server",
                            "server",
                            "sef:social.reminder",
                            java.util.Set.of(),
                            Map.of(),
                            Map.of(),
                            usage)).effectiveValue();
            if (usage >= quota) {
                fail(source, "Reminder definition quota reached.");
                return 0;
            }
            KernelServices.social().putReminder(new SocialRepository.ReminderDefinition(
                    id,
                    true,
                    message,
                    SocialRepository.ReminderAudience.ALL,
                    0,
                    1,
                    true,
                    1,
                    actor,
                    Instant.now()));
            success(source, "Reminder created.");
            return 1;
        });
    }

    private static int message(CommandSourceStack source, String id, String message) {
        var compiled = KernelServices.messages().compile(
                message,
                java.util.Set.of("player", "username", "unread_mail"));
        if (!compiled.successful()) {
            fail(source, compiled.detail());
            return 0;
        }
        return update(source, id, "message", definition -> new SocialRepository.ReminderDefinition(
                definition.id(),
                definition.enabled(),
                message,
                definition.audience(),
                definition.repeatSeconds(),
                definition.maximumDeliveries(),
                definition.allowDismissal(),
                definition.acknowledgementRevision() + 1,
                actor(source),
                Instant.now()));
    }

    private static int delete(CommandSourceStack source, String id) {
        return manage(source, "delete", id, () -> {
            if (!KernelServices.social().deleteReminder(id)) {
                fail(source, "Reminder not found.");
                return 0;
            }
            success(source, "Reminder deleted.");
            return 1;
        });
    }

    private static int enable(CommandSourceStack source, String id, boolean enabled) {
        return update(source, id, "enable", definition -> definition.withEnabled(enabled, actor(source)));
    }

    private static int repeat(CommandSourceStack source, String id, long seconds) {
        return update(source, id, "repeat", definition -> copy(
                definition,
                definition.audience(),
                seconds,
                definition.maximumDeliveries()));
    }

    private static int count(CommandSourceStack source, String id, int maximum) {
        return update(source, id, "count", definition -> copy(
                definition,
                definition.audience(),
                definition.repeatSeconds(),
                maximum));
    }

    private static int audience(
            CommandSourceStack source,
            String id,
            SocialRepository.ReminderAudience audience
    ) {
        return update(source, id, "audience", definition -> copy(
                definition,
                audience,
                definition.repeatSeconds(),
                definition.maximumDeliveries()));
    }

    private static int update(
            CommandSourceStack source,
            String id,
            String operation,
            java.util.function.UnaryOperator<SocialRepository.ReminderDefinition> update
    ) {
        return manage(source, operation, id, () -> {
            SocialRepository.ReminderDefinition current =
                    KernelServices.social().reminder(id).orElse(null);
            if (current == null) {
                fail(source, "Reminder not found.");
                return 0;
            }
            KernelServices.social().putReminder(update.apply(current));
            success(source, "Reminder updated.");
            return 1;
        });
    }

    private static int send(
            CommandSourceStack source,
            String id,
            List<ServerPlayer> targets
    ) {
        SocialRepository.ReminderDefinition definition = findReminder(source, id);
        if (definition == null) {
            return 0;
        }
        int limit = ConfigHandler.config.kernelMaximumTargets.get();
        if (targets.size() > limit) {
            fail(source, "Too many reminder targets.");
            return 0;
        }
        return KernelCommandExecutor.execute(
                source,
                "sef:social.reminder",
                Map.of("operation", "send", "reminder", definition.id()),
                targets.stream().map(ServerPlayer::getUUID).toList(),
                false,
                () -> {
                    int delivered = 0;
                    for (ServerPlayer target : targets) {
                        if (ReminderService.deliver(target, definition, true)) {
                            delivered++;
                        }
                    }
                    success(source, "Reminder sent to " + delivered + " player or players.");
                    return Math.max(1, delivered);
                },
                PermissionsHandler.reminderSend);
    }

    private static int welcome(CommandSourceStack source, ServerPlayer target) {
        ServerPlayer recipient = target == null ? source.getPlayer() : target;
        if (recipient == null) {
            fail(source, "A player target is required.");
            return 0;
        }
        var permission = target == null ? PermissionsHandler.welcomePreview : PermissionsHandler.welcomeSend;
        return KernelCommandExecutor.execute(
                source,
                "sef:social.reminder",
                Map.of("operation", target == null ? "welcome_preview" : "welcome_send"),
                target == null ? List.of() : List.of(target.getUUID()),
                false,
                () -> {
                    int delivered = 0;
                    for (SocialRepository.ReminderDefinition definition : KernelServices.social().reminders()) {
                        if (definition.audience() == SocialRepository.ReminderAudience.FIRST_JOIN
                                && ReminderService.deliver(recipient, definition, true)) {
                            delivered++;
                        }
                    }
                    if (delivered == 0) {
                        fail(source, "No welcome reminders are configured.");
                        return 0;
                    }
                    return delivered;
                },
                permission);
    }

    private static int dismissal(CommandSourceStack source, String id, boolean dismissed) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            fail(source, "This command can only be used by a player.");
            return 0;
        }
        SocialRepository.ReminderDefinition definition = findReminder(source, id);
        if (definition == null) {
            return 0;
        }
        if (!definition.allowDismissal()) {
            fail(source, "That reminder cannot be changed.");
            return 0;
        }
        return KernelCommandExecutor.execute(source, "sef:social.reminder", Map.of(
                "operation", dismissed ? "dismiss" : "restore",
                "reminder", definition.id()), () -> {
            SocialRepository.ReminderState current =
                    KernelServices.social().reminderState(player.getUUID(), definition.id());
            KernelServices.social().updateReminderState(new SocialRepository.ReminderState(
                    current.key(),
                    current.lastDeliveredAt(),
                    current.deliveryCount(),
                    dismissed,
                    definition.acknowledgementRevision()));
            success(source, dismissed ? "Reminder dismissed." : "Reminder restored.");
            return 1;
        }, PermissionsHandler.reminderDismiss);
    }

    private static int list(CommandSourceStack source, int page) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            fail(source, "This command can only be used by a player.");
            return 0;
        }
        return KernelCommandExecutor.execute(source, "sef:social.reminder", Map.of("operation", "list"), () -> {
            var reminders = KernelServices.social().reminders().stream()
                    .filter(SocialRepository.ReminderDefinition::enabled)
                    .filter(SocialRepository.ReminderDefinition::allowDismissal)
                    .filter(definition -> definition.audience() != SocialRepository.ReminderAudience.UNREAD_MAIL
                            || KernelServices.social().unreadMail(player.getUUID()) > 0)
                    .toList();
            if (reminders.isEmpty()) {
                source.sendSuccess(() -> TextFormatter.stringToFormattedText("&7No reminders are configured."), false);
                return 1;
            }
            int pageSize = 10;
            int start = (page - 1) * pageSize;
            if (start >= reminders.size()) {
                fail(source, "No reminders on that page.");
                return 0;
            }
            int end = Math.min(reminders.size(), start + pageSize);
            reminders.subList(start, end).forEach(definition -> source.sendSuccess(
                    () -> net.minecraft.network.chat.Component.literal(
                            definition.id() + ". "
                                    + definition.audience().name().toLowerCase(java.util.Locale.ROOT)
                                    + ". " + (definition.enabled() ? "enabled" : "disabled")
                                    + ". repeat " + definition.repeatSeconds()
                                    + ". maximum " + definition.maximumDeliveries()),
                    false));
            return end - start;
        });
    }

    private static int manage(
            CommandSourceStack source,
            String operation,
            String id,
            java.util.function.IntSupplier action
    ) {
        return KernelCommandExecutor.execute(source, "sef:social.reminder", Map.of(
                "operation", operation,
                "reminder", id), () -> {
            try {
                return action.getAsInt();
            } catch (IllegalArgumentException | IllegalStateException exception) {
                fail(source, exception.getMessage());
                return 0;
            }
        }, PermissionsHandler.reminderManage);
    }

    private static SocialRepository.ReminderDefinition findReminder(CommandSourceStack source, String id) {
        try {
            SocialRepository.ReminderDefinition definition =
                    KernelServices.social().reminder(id).orElse(null);
            if (definition == null) {
                fail(source, "Reminder not found.");
            }
            return definition;
        } catch (IllegalArgumentException exception) {
            fail(source, "Reminder id is invalid.");
            return null;
        }
    }

    private static SocialRepository.ReminderDefinition copy(
            SocialRepository.ReminderDefinition definition,
            SocialRepository.ReminderAudience audience,
            long repeat,
            int maximum
    ) {
        return new SocialRepository.ReminderDefinition(
                definition.id(),
                definition.enabled(),
                definition.message(),
                audience,
                repeat,
                maximum,
                definition.allowDismissal(),
                definition.acknowledgementRevision() + 1,
                definition.createdBy(),
                Instant.now());
    }

    private static UUID actor(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player != null) {
            return player.getUUID();
        }
        return UUID.nameUUIDFromBytes(
                ("sef:reminder:" + source.getTextName()).getBytes(StandardCharsets.UTF_8));
    }

    private static void success(CommandSourceStack source, String value) {
        source.sendSuccess(() -> TextFormatter.stringToFormattedText("&a" + value), false);
    }

    private static void fail(CommandSourceStack source, String value) {
        source.sendFailure(TextFormatter.stringToFormattedText("&c" + value));
    }
}
