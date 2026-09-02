package com.enviouse.sef.social;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.utils.IntegratedNicknameProvider;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public final class IdentityCommands {
    private IdentityCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sef")
                .then(Commands.literal("identity")
                        .then(Commands.literal("coverage")
                                .requires(source -> PermissionService.has(
                                        source, PermissionsHandler.identityCoverage))
                                .executes(context -> coverage(context.getSource())))
                        .then(Commands.literal("refresh")
                                .requires(source -> PermissionService.has(
                                        source, PermissionsHandler.identityRefresh))
                                .executes(context -> refresh(context.getSource())))));
    }

    private static int coverage(CommandSourceStack source) {
        return KernelCommandExecutor.execute(source, "sef:social.identity", Map.of(
                "operation", "coverage"), () -> {
            Object provider = ServerEssentialsForge.instance == null
                    ? null
                    : ServerEssentialsForge.instance.nicknameProvider;
            String ownership = provider == null || provider instanceof IntegratedNicknameProvider
                    ? "integrated"
                    : "external";
            source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                    "&7nickname owner. &f" + ownership
                            + "&7. projected surfaces. &fchat, tab, connection messages, death and advancement display components, sef identity resolution, command feedback"
                            + "&7. authenticated surfaces. &fbrigadier player arguments, signed chat identity"
                            + "&7. enhanced nametags. &fphase 9 client contract"), false);
            return 1;
        }, PermissionsHandler.identityCoverage);
    }

    private static int refresh(CommandSourceStack source) {
        return KernelCommandExecutor.execute(source, "sef:social.identity", Map.of(
                "operation", "refresh"), () -> {
            int refreshed = 0;
            for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
                player.refreshTabListName();
                refreshed++;
            }
            int count = refreshed;
            source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                    "&aRefreshed projected identity state for &e" + count + " &aplayer or players."), false);
            return Math.max(1, refreshed);
        }, PermissionsHandler.identityRefresh);
    }
}
