package com.enviouse.sef.social;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.identity.IdentityArguments;
import com.enviouse.sef.identity.IdentityService;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.permissions.QuotaPermissionResolver;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MailCommands {
    private MailCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (!ConfigHandler.config.enableSocialEssentials.get() || !ConfigHandler.config.enableMail.get()) {
            return;
        }
        dispatcher.register(Commands.literal("mail")
                .requires(source -> PermissionService.has(source, PermissionsHandler.mailCommand))
                .executes(context -> list(context.getSource(), 1))
                .then(Commands.literal("read")
                        .executes(context -> readAll(context.getSource())))
                .then(Commands.literal("send")
                        .requires(source -> PermissionService.has(source, PermissionsHandler.mailSendCommand))
                        .then(IdentityArguments.known("player")
                                .then(Commands.argument("message", StringArgumentType.greedyString())
                                        .executes(context -> send(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "player"),
                                                StringArgumentType.getString(context, "message"))))))
                .then(Commands.literal("clear")
                        .executes(context -> clear(context.getSource())))
                .then(Commands.literal("delete")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(context -> mutate(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "id"),
                                        SocialRepository.MailMutation.DELETE))))
                .then(Commands.literal("archive")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(context -> mutate(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "id"),
                                        SocialRepository.MailMutation.ARCHIVE))))
                .then(Commands.literal("list")
                        .executes(context -> list(context.getSource(), 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> list(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "page"))))));
    }

    public static void notifyUnread(ServerPlayer player) {
        long unread = KernelServices.social().unreadMail(player.getUUID());
        if (unread > 0) {
            player.sendSystemMessage(TextFormatter.stringToFormattedText(
                    "&eYou have &6" + unread + " &eunread mail message" + (unread == 1 ? "." : "s.")));
        }
    }

    private static int send(CommandSourceStack source, String identity, String body) {
        ServerPlayer sender = source.getPlayer();
        if (sender == null) {
            fail(source, "Only players can send mail.");
            return 0;
        }
        if (body.isBlank()
                || body.length() > ConfigHandler.config.mailMaximumLength.get()
                || body.codePoints().anyMatch(Character::isISOControl)) {
            fail(source, "Mail body is invalid or too long.");
            return 0;
        }
        ActionResult<IdentityService.Identity> resolved = KernelServices.identities().resolve(identity, sender);
        if (!resolved.successful() || resolved.value().playerId() == null) {
            fail(source, resolved.reason() == ActionResult.ReasonCode.AMBIGUOUS
                    ? "That identity is ambiguous."
                    : "That player is unavailable.");
            return 0;
        }
        UUID recipient = resolved.value().playerId();
        if (recipient.equals(sender.getUUID())) {
            fail(source, "You cannot mail yourself.");
            return 0;
        }
        if (KernelServices.social().ignores(recipient, sender.getUUID())) {
            fail(source, "That player is unavailable.");
            return 0;
        }
        long usage = KernelServices.social().mail(recipient, true).size();
        long quota = KernelServices.quotas().resolve(new com.enviouse.sef.kernel.policy.QuotaService.Context(
                "sef:mail", recipient, "server", "server", "server", "sef:social.mail",
                QuotaPermissionResolver.granted(recipient), Map.of(), Map.of(), usage)).effectiveValue();
        return KernelCommandExecutor.execute(
                source,
                "sef:social.mail",
                Map.of("operation", "send"),
                List.of(recipient),
                false,
                () -> {
                    ActionResult<SocialRepository.MailRecord> result = KernelServices.social().sendMail(
                            sender.getUUID(),
                            recipient,
                            body,
                            Instant.now().plus(ConfigHandler.config.mailRetentionDays.get(), ChronoUnit.DAYS),
                            quota);
                    if (!result.successful()) {
                        fail(source, result.detail());
                        return 0;
                    }
                    ServerPlayer online = source.getServer().getPlayerList().getPlayer(recipient);
                    if (online != null) {
                        notifyUnread(online);
                    }
                    success(source, "Mail sent.");
                    return 1;
                },
                PermissionsHandler.mailSendCommand);
    }

    private static int list(CommandSourceStack source, int page) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        return KernelCommandExecutor.execute(source, "sef:social.mail", Map.of(
                "operation", "list",
                "page", Integer.toString(page)), () -> {
            List<SocialRepository.MailRecord> mail = KernelServices.social().mail(player.getUUID(), false);
            int pageSize = 10;
            int start = (page - 1) * pageSize;
            if (start >= mail.size()) {
                info(source, "No mail on that page.");
                return 1;
            }
            int end = Math.min(mail.size(), start + pageSize);
            info(source, "Mail page " + page + ".");
            for (SocialRepository.MailRecord record : mail.subList(start, end)) {
                String sender = KernelServices.profiles().find(record.senderId())
                        .map(profile -> profile.nickname() == null || profile.nickname().isBlank()
                                ? profile.authenticatedUsername()
                                : profile.nickname())
                        .orElse(record.senderId().toString());
                source.sendSuccess(() -> Component.literal(
                        record.id() + " from " + sender + (record.read() ? "" : " unread")
                                + ". " + record.body()), false);
            }
            return end - start;
        });
    }

    private static int readAll(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        return KernelCommandExecutor.execute(source, "sef:social.mail", Map.of("operation", "read"), () -> {
            int count = 0;
            for (SocialRepository.MailRecord record : KernelServices.social().mail(player.getUUID(), false)) {
                if (!record.read()) {
                    KernelServices.social().updateMail(
                            player.getUUID(), record.id(), SocialRepository.MailMutation.READ);
                    count++;
                }
            }
            success(source, "Marked " + count + " mail messages as read.");
            return Math.max(1, count);
        });
    }

    private static int mutate(CommandSourceStack source, String id, SocialRepository.MailMutation mutation) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        UUID mailId;
        try {
            mailId = UUID.fromString(id);
        } catch (IllegalArgumentException exception) {
            fail(source, "Invalid mail id.");
            return 0;
        }
        return KernelCommandExecutor.execute(source, "sef:social.mail", Map.of(
                "operation", mutation.name().toLowerCase(java.util.Locale.ROOT),
                "mail_id", mailId.toString()), () -> {
            ActionResult<SocialRepository.MailRecord> result =
                    KernelServices.social().updateMail(player.getUUID(), mailId, mutation);
            if (!result.successful()) {
                fail(source, result.detail());
                return 0;
            }
            success(source, mutation.name().toLowerCase(java.util.Locale.ROOT) + " completed.");
            return 1;
        });
    }

    private static int clear(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        return KernelCommandExecutor.execute(source, "sef:social.mail", Map.of("operation", "clear"), () -> {
            int removed = KernelServices.social().clearMail(player.getUUID());
            success(source, "Cleared " + removed + " mail messages.");
            return Math.max(1, removed);
        });
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
