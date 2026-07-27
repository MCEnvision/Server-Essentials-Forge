package com.enviouse.sef.permissions;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.utils.moddeps.LuckPermsProvider;
import com.enviouse.sef.vanish.VanishUtil;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.gui.protocol.SefGuiRuntime;
import com.enviouse.sef.gui.protocol.SefSessionManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.UUID;

public final class PermissionRefreshBridge {
    private static EventSubscription<UserDataRecalculateEvent> subscription;

    private PermissionRefreshBridge() {
    }

    public static synchronized void start(LuckPerms luckPerms) {
        stop();
        subscription = luckPerms.getEventBus().subscribe(
                UserDataRecalculateEvent.class,
                PermissionRefreshBridge::onUserDataRecalculated);
    }

    public static synchronized void stop() {
        if (subscription != null) {
            subscription.close();
            subscription = null;
        }
        LuckPermsProvider.invalidateAll();
    }

    private static void onUserDataRecalculated(UserDataRecalculateEvent event) {
        UUID playerId = event.getUser().getUniqueId();
        LuckPermsProvider.invalidate(playerId);
        invalidateKernelActor(playerId);
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        server.execute(() -> {
            var player = server.getPlayerList().getPlayer(playerId);
            if (player != null) {
                VanishUtil.recheckVanished(player);
                player.refreshTabListName();
                server.getCommands().sendCommands(player);
                SefSessionManager.instance().refresh(player);
                SefGuiRuntime.refreshIdentityProjections(server);
            }
        });
    }

    static void invalidateKernelActor(UUID playerId) {
        KernelServices.quotas().invalidate();
        KernelServices.warmups().clear(playerId);
        KernelServices.confirmations().revokeActor(playerId);
    }
}
