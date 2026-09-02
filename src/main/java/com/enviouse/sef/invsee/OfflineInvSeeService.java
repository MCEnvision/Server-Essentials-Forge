package com.enviouse.sef.invsee;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.identity.IdentityService;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.permissions.PermissionService;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class OfflineInvSeeService {
    private static final Map<UUID, Lease> EDIT_LEASES = new java.util.concurrent.ConcurrentHashMap<>();
    private static ExecutorService executor;

    private OfflineInvSeeService() {
    }

    public static int open(ServerPlayer viewer, IdentityService.Identity identity) {
        Objects.requireNonNull(viewer, "viewer");
        Objects.requireNonNull(identity, "identity");
        UUID targetId = identity.playerId();
        String targetName = identity.authenticatedUsername();
        if (targetId == null || targetName == null || !canAccess(viewer, targetId, targetName, false)) {
            return fail(viewer, "That offline inventory is unavailable.");
        }
        MinecraftServer server = viewer.server;
        OfflinePlayerInventoryAdapter adapter;
        try {
            adapter = adapter(server);
        } catch (RuntimeException exception) {
            return fail(viewer, "The offline inventory provider is unavailable.");
        }
        long requestedConfigurationRevision = KernelServices.configurationRevision();
        viewer.sendSystemMessage(TextFormatter.stringToFormattedText(
                "&7Loading the revisioned offline inventory snapshot."));
        CompletableFuture.supplyAsync(() -> {
            try {
                return adapter.load(targetId, targetName);
            } catch (Exception exception) {
                throw new OfflineLoadException(exception);
            }
        }, executor()).whenComplete((snapshot, failure) -> server.execute(() -> {
            if (failure != null) {
                fail(viewer, "The offline inventory could not be loaded safely.");
                return;
            }
            if (requestedConfigurationRevision != KernelServices.configurationRevision()
                    || !canAccess(viewer, targetId, targetName, false)) {
                fail(viewer, "The offline inventory request became stale.");
                return;
            }
            boolean mayModify = snapshot.mutable()
                    && !ConfigHandler.config.invSeeReadOnly.get()
                    && PermissionService.has(viewer, PermissionsHandler.invSeeModify);
            Lease lease = mayModify ? acquire(targetId, viewer.getUUID()) : null;
            if (mayModify && lease == null) {
                mayModify = false;
                viewer.sendSystemMessage(TextFormatter.stringToFormattedText(
                        "&eAnother administrator owns the editable offline inventory lease. "
                                + "This snapshot is read only."));
            } else if (!snapshot.mutable()) {
                viewer.sendSystemMessage(TextFormatter.stringToFormattedText(
                        "&eUnsupported item data was preserved. This snapshot is read only."));
            }
            boolean editable = mayModify;
            Lease activeLease = lease;
            Component title = TextFormatter.stringToFormattedText(
                    ConfigHandler.config.invSeeTitle.get().replace("$player", targetName)
                            + " &7(Offline)");
            viewer.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return title;
                }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                    return new OfflineInvSeeMenu(
                            id,
                            inventory,
                            adapter,
                            snapshot,
                            editable,
                            activeLease);
                }
            });
        }));
        return 1;
    }

    static boolean canAccess(
            ServerPlayer viewer,
            UUID targetId,
            String targetName,
            boolean requireModify
    ) {
        if (!ConfigHandler.config.enableInvSee.get()
                || !ConfigHandler.config.invSeeOfflineEnabled.get()
                || viewer == null
                || !viewer.isAlive()
                || viewer.hasDisconnected()
                || viewer.server.getPlayerList().getPlayer(targetId) != null
                || !InvSeeCommand.canView(viewer.createCommandSourceStack())
                || !PermissionService.has(viewer, PermissionsHandler.invSeeOffline)
                || requireModify && (ConfigHandler.config.invSeeReadOnly.get()
                || !PermissionService.has(viewer, PermissionsHandler.invSeeModify))) {
            return false;
        }
        return PlayerTargetPolicy.decideOffline(
                viewer.createCommandSourceStack(),
                new GameProfile(targetId, targetName),
                PermissionsHandler.phasePermission("inventory.hierarchy.bypass"),
                PermissionsHandler.phasePermission("exempt.inventory"),
                PermissionsHandler.phasePermission("inventory.bypass.exempt"),
                true,
                true).allowed();
    }

    static boolean leaseValid(UUID targetId, Lease lease, UUID viewerId) {
        return lease != null
                && lease.viewerId().equals(viewerId)
                && lease.equals(EDIT_LEASES.get(targetId));
    }

    static void release(UUID targetId, Lease lease) {
        if (lease != null) {
            EDIT_LEASES.remove(targetId, lease);
        }
    }

    public static synchronized void shutdown() {
        EDIT_LEASES.clear();
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    private static OfflinePlayerInventoryAdapter adapter(MinecraftServer server) {
        Path playerData = server.getWorldPath(LevelResource.PLAYER_DATA_DIR);
        Path backups = server.getServerDirectory()
                .resolve("serverconfig")
                .resolve("sef")
                .resolve("backups")
                .resolve("offline_inventory");
        int maximumBytes = Math.multiplyExact(
                ConfigHandler.config.invSeeOfflineMaximumFileKiB.get(),
                1024);
        return new OfflinePlayerInventoryAdapter(
                playerData,
                backups,
                server.registryAccess(),
                server.getFixerUpper(),
                maximumBytes,
                ConfigHandler.config.invSeeOfflineMaximumBackups.get());
    }

    private static Lease acquire(UUID targetId, UUID viewerId) {
        Lease lease = new Lease(UUID.randomUUID(), viewerId);
        return EDIT_LEASES.putIfAbsent(targetId, lease) == null ? lease : null;
    }

    private static synchronized ExecutorService executor() {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor(task -> {
                Thread thread = new Thread(task, "sef-offline-inventory");
                thread.setDaemon(true);
                return thread;
            });
        }
        return executor;
    }

    private static int fail(ServerPlayer viewer, String message) {
        viewer.sendSystemMessage(TextFormatter.stringToFormattedText("&c" + message));
        return 0;
    }

    record Lease(UUID id, UUID viewerId) {
        Lease {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(viewerId, "viewerId");
        }
    }

    private static final class OfflineLoadException extends RuntimeException {
        private OfflineLoadException(Throwable cause) {
            super(cause);
        }
    }
}
