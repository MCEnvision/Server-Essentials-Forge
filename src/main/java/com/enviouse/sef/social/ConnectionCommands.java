package com.enviouse.sef.social;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.identity.IdentityArguments;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.kernel.policy.TargetHierarchyService;
import com.enviouse.sef.permissions.PermissionService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.FakePlayer;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ConnectionCommands {
    private static final Set<String> PLACEHOLDERS = Set.of("player", "username", "uuid", "world");

    private ConnectionCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (!ConfigHandler.config.enableSocialEssentials.get()
                || !ConfigHandler.config.enableConnectionMessages.get()) {
            return;
        }
        registerTemplate(dispatcher, true);
        registerTemplate(dispatcher, false);
        dispatcher.register(Commands.literal("connectionmessage")
                .requires(source -> PermissionService.has(source, PermissionsHandler.connectionMessageInspect))
                .then(IdentityArguments.online("player")
                        .executes(context -> inspect(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "player")))));
    }

    private static void registerTemplate(CommandDispatcher<CommandSourceStack> dispatcher, boolean joining) {
        String root = joining ? "joinmessage" : "leavemessage";
        var setPermission = joining ? PermissionsHandler.joinMessageSet : PermissionsHandler.leaveMessageSet;
        var clearPermission = joining ? PermissionsHandler.joinMessageClear : PermissionsHandler.leaveMessageClear;
        var previewPermission = joining
                ? PermissionsHandler.joinMessagePreview
                : PermissionsHandler.leaveMessagePreview;
        dispatcher.register(Commands.literal(root)
                .then(Commands.literal("set")
                        .requires(source -> PermissionService.has(source, setPermission))
                        .then(IdentityArguments.online("player")
                                .then(Commands.argument("message", StringArgumentType.greedyString())
                                        .executes(context -> set(
                                                context.getSource(),
                                                IdentityArguments.getOnline(context, "player"),
                                                joining,
                                                StringArgumentType.getString(context, "message"),
                                                setPermission)))))
                .then(Commands.literal("clear")
                        .requires(source -> PermissionService.has(source, clearPermission))
                        .then(IdentityArguments.online("player")
                                .executes(context -> set(
                                        context.getSource(),
                                        IdentityArguments.getOnline(context, "player"),
                                        joining,
                                        "",
                                        clearPermission))))
                .then(Commands.literal("preview")
                        .requires(source -> PermissionService.has(source, previewPermission))
                        .then(IdentityArguments.online("player")
                                .executes(context -> preview(
                                        context.getSource(),
                                        IdentityArguments.getOnline(context, "player"),
                                        joining,
                                        previewPermission)))));
    }

    private static int set(
            CommandSourceStack source,
            ServerPlayer target,
            boolean joining,
            String template,
            net.neoforged.neoforge.server.permission.nodes.PermissionNode<Boolean> permission
    ) {
        if (!mayTarget(source, target)) {
            return 0;
        }
        if (!template.isBlank()) {
            ActionResult<com.enviouse.sef.message.MessageService.Template> compiled =
                    KernelServices.messages().compile(template, PLACEHOLDERS);
            if (!compiled.successful()) {
                fail(source, compiled.detail());
                return 0;
            }
        }
        return KernelCommandExecutor.execute(
                source,
                "sef:social.connection",
                Map.of("operation", template.isBlank() ? "clear" : "set",
                        "kind", joining ? "join" : "leave"),
                List.of(target.getUUID()),
                false,
                () -> {
                    KernelServices.social().setConnectionTemplate(target.getUUID(), joining, template);
                    success(source, (joining ? "Join" : "Leave") + " message updated for "
                            + target.getGameProfile().getName() + ".");
                    return 1;
                },
                permission);
    }

    private static int preview(
            CommandSourceStack source,
            ServerPlayer target,
            boolean joining,
            net.neoforged.neoforge.server.permission.nodes.PermissionNode<Boolean> permission
    ) {
        if (!mayTarget(source, target)) {
            return 0;
        }
        return KernelCommandExecutor.execute(
                source,
                "sef:social.connection",
                Map.of("operation", "preview", "kind", joining ? "join" : "leave"),
                List.of(target.getUUID()),
                false,
                () -> {
                    Component vanilla = Component.translatable(
                            joining ? "multiplayer.player.joined" : "multiplayer.player.left",
                            target.getDisplayName());
                    source.sendSuccess(() -> ConnectionMessageService.render(target, joining, vanilla), false);
                    return 1;
                },
                permission);
    }

    private static int inspect(CommandSourceStack source, ServerPlayer target) {
        if (!mayTarget(source, target)) {
            return 0;
        }
        return KernelCommandExecutor.execute(
                source,
                "sef:social.connection",
                Map.of("operation", "inspect"),
                List.of(target.getUUID()),
                false,
                () -> {
                    SocialRepository.ConnectionTemplates templates =
                            KernelServices.social().connectionTemplates(target.getUUID());
                    source.sendSuccess(() -> Component.literal("join. "
                            + value(templates.joinTemplate())
                            + " leave. " + value(templates.leaveTemplate())
                            + " revision. " + templates.revision()), false);
                    return 1;
                },
                PermissionsHandler.connectionMessageInspect);
    }

    private static boolean mayTarget(CommandSourceStack source, ServerPlayer target) {
        ServerPlayer actor = source.getPlayer();
        if (actor instanceof FakePlayer) {
            fail(source, "Fake players cannot manage connection messages.");
            return false;
        }
        TargetHierarchyService.Decision decision = PlayerTargetPolicy.decide(
                source,
                target,
                PermissionsHandler.connectionMessageHierarchyBypass,
                PermissionsHandler.connectionMessageExempt,
                PermissionsHandler.connectionMessageBypassExempt,
                false,
                true);
        if (!decision.allowed()) {
            fail(source, "You cannot manage that player because of hierarchy.");
        }
        return decision.allowed();
    }

    private static String value(String value) {
        return value == null || value.isBlank() ? "default" : value;
    }

    private static void success(CommandSourceStack source, String value) {
        source.sendSuccess(() -> TextFormatter.stringToFormattedText("&a" + value), false);
    }

    private static void fail(CommandSourceStack source, String value) {
        source.sendFailure(TextFormatter.stringToFormattedText("&c" + value));
    }
}
