package com.enviouse.sef.disguise;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.gui.protocol.SefProtocol;
import com.enviouse.sef.gui.protocol.SefSessionManager;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.vanish.VanishUtil;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class DisguiseProxyService {
    private static final int MAXIMUM_PROXIES_PER_VIEWER = SefProtocol.MAXIMUM_DISGUISE_PROJECTIONS;
    private static final int MAXIMUM_TOTAL_PROXIES = 65_536;
    private static final double MAXIMUM_TRACKING_DISTANCE_SQUARED = 128.0D * 128.0D;
    private static final double MAXIMUM_INTERACTION_DISTANCE_SQUARED = 3.0D * 3.0D;
    private static final Map<ObserverSubject, ProxyView> PROXIES = new HashMap<>();

    private DisguiseProxyService() {
    }

    public static void tick(MinecraftServer server) {
        Objects.requireNonNull(server, "server");
        if (!ConfigHandler.config.enableDisguises.get()
                || !ConfigHandler.config.disguiseVanillaProxyEnabled.get()) {
            clear(server);
            return;
        }
        Set<UUID> online = server.getPlayerList().getPlayers().stream()
                .map(ServerPlayer::getUUID)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        List<ObserverSubject> abandoned;
        synchronized (PROXIES) {
            abandoned = PROXIES.keySet().stream()
                    .filter(key -> !online.contains(key.observerId()) || !online.contains(key.subjectId()))
                    .toList();
        }
        abandoned.forEach(key -> remove(server, key, false));
        for (ServerPlayer viewer : server.getPlayerList().getPlayers()) {
            synchronizeViewer(viewer);
        }
    }

    public static boolean shouldSuppressRealSpawn(ServerPlayer viewer, int entityId) {
        synchronized (PROXIES) {
            return PROXIES.values().stream().anyMatch(view ->
                    view.allocation().observerId().equals(viewer.getUUID())
                            && view.realEntityId() == entityId);
        }
    }

    public static boolean handleInteraction(
            ServerPlayer observer,
            ServerboundInteractPacket packet,
            int proxyEntityId
    ) {
        ProxyEntityIdAllocator.Allocation allocation = KernelServices.disguiseProxyIds()
                .resolve(observer.getUUID(), proxyEntityId)
                .orElse(null);
        if (allocation == null) {
            return false;
        }
        ProxyView view;
        synchronized (PROXIES) {
            view = PROXIES.get(new ObserverSubject(observer.getUUID(), allocation.subjectId()));
        }
        ServerPlayer subject = observer.server.getPlayerList().getPlayer(allocation.subjectId());
        DisguiseService.DisguiseRecord record = KernelServices.disguises()
                .active(allocation.subjectId())
                .orElse(null);
        if (view == null
                || subject == null
                || subject.level() != observer.level()
                || record == null
                || record.revision() != allocation.disguiseRevision()
                || VanishUtil.isVanished(subject, observer)
                || observer.distanceToSqr(subject) > MAXIMUM_INTERACTION_DISTANCE_SQUARED
                || !observer.hasLineOfSight(subject)) {
            remove(observer.server, new ObserverSubject(observer.getUUID(), allocation.subjectId()), false);
            return true;
        }
        packet.dispatch(new ServerboundInteractPacket.Handler() {
            @Override
            public void onInteraction(InteractionHand hand) {
                observer.interactOn(subject, hand);
            }

            @Override
            public void onInteraction(InteractionHand hand, Vec3 location) {
                subject.interactAt(observer, location, hand);
            }

            @Override
            public void onAttack() {
                if (observer.canHarmPlayer(subject)) {
                    observer.attack(subject);
                }
            }
        });
        return true;
    }

    public static void logout(MinecraftServer server, UUID playerId) {
        List<ObserverSubject> affected;
        synchronized (PROXIES) {
            affected = PROXIES.keySet().stream()
                    .filter(key -> key.observerId().equals(playerId) || key.subjectId().equals(playerId))
                    .toList();
        }
        affected.forEach(key -> remove(server, key, false));
    }

    public static void clear(MinecraftServer server) {
        List<ObserverSubject> keys;
        synchronized (PROXIES) {
            keys = List.copyOf(PROXIES.keySet());
        }
        keys.forEach(key -> remove(server, key, true));
        KernelServices.disguiseProxyIds().clear();
    }

    public static int size() {
        synchronized (PROXIES) {
            return PROXIES.size();
        }
    }

    private static void synchronizeViewer(ServerPlayer viewer) {
        if (supportsEnhancedDisguises(viewer)) {
            removeViewer(viewer, true);
            return;
        }
        Set<UUID> desired = new HashSet<>();
        int accepted = 0;
        for (DisguiseService.DisguiseRecord record : KernelServices.disguises().active()) {
            if (accepted >= MAXIMUM_PROXIES_PER_VIEWER) {
                break;
            }
            ServerPlayer subject = viewer.server.getPlayerList().getPlayer(record.subjectId());
            if (subject == null
                    || subject == viewer
                    || subject.level() != viewer.level()
                    || viewer.distanceToSqr(subject) > MAXIMUM_TRACKING_DISTANCE_SQUARED) {
                continue;
            }
            boolean staff = PermissionService.has(
                    viewer,
                    PermissionsHandler.phasePermission("commands.disguise.inspect"));
            DisguiseService.Projection projection = KernelServices.disguises().projection(
                    viewer.getUUID(),
                    subject.getUUID(),
                    VanishUtil.isVanished(subject, viewer),
                    false,
                    staff).orElse(null);
            if (projection == null || projection.mode() != DisguiseService.ProjectionMode.VANILLA_PROXY) {
                continue;
            }
            desired.add(subject.getUUID());
            synchronize(viewer, subject, projection.record());
            accepted++;
        }
        List<ObserverSubject> stale;
        synchronized (PROXIES) {
            stale = PROXIES.keySet().stream()
                    .filter(key -> key.observerId().equals(viewer.getUUID())
                            && !desired.contains(key.subjectId()))
                    .toList();
        }
        stale.forEach(key -> remove(viewer.server, key, true));
    }

    private static void synchronize(
            ServerPlayer viewer,
            ServerPlayer subject,
            DisguiseService.DisguiseRecord record
    ) {
        ObserverSubject key = new ObserverSubject(viewer.getUUID(), subject.getUUID());
        ProxyView current;
        synchronized (PROXIES) {
            current = PROXIES.get(key);
        }
        if (current == null || current.allocation().disguiseRevision() != record.revision()) {
            if (current != null) {
                remove(viewer.server, key, false);
            }
            spawn(viewer, subject, record);
            return;
        }
        copyTransform(subject, current.proxy());
        viewer.connection.send(new ClientboundTeleportEntityPacket(current.proxy()));
        viewer.connection.send(new ClientboundRotateHeadPacket(
                current.proxy(),
                (byte) Math.floor(current.proxy().getYHeadRot() * 256.0F / 360.0F)));
        viewer.connection.send(new ClientboundSetEntityMotionPacket(
                current.proxy().getId(),
                current.proxy().getDeltaMovement()));
        Map<EquipmentSlot, ItemStack> equipment =
                effectiveEquipment(viewer, subject, record.equipmentPolicy());
        if (!sameEquipment(current.equipment(), equipment)) {
            sendEquipment(viewer, current.proxy().getId(), equipment);
            synchronized (PROXIES) {
                if (PROXIES.get(key) == current) {
                    PROXIES.put(key, new ProxyView(
                            current.allocation(),
                            current.realEntityId(),
                            current.proxy(),
                            equipment));
                }
            }
        }
    }

    private static void spawn(
            ServerPlayer viewer,
            ServerPlayer subject,
            DisguiseService.DisguiseRecord record
    ) {
        synchronized (PROXIES) {
            if (PROXIES.size() >= MAXIMUM_TOTAL_PROXIES) {
                return;
            }
        }
        Entity proxy = net.minecraft.world.entity.EntityType.byString(record.reference())
                .map(type -> type.create(subject.level()))
                .orElse(null);
        if (!(proxy instanceof LivingEntity)) {
            return;
        }
        ProxyEntityIdAllocator.Allocation allocation = KernelServices.disguiseProxyIds()
                .allocate(viewer.getUUID(), subject.getUUID(), record.revision());
        proxy.setId(allocation.proxyEntityId());
        proxy.setUUID(UUID.nameUUIDFromBytes((
                "sef:disguise:"
                        + viewer.getUUID()
                        + ":"
                        + subject.getUUID()
                        + ":"
                        + record.revision()).getBytes(StandardCharsets.UTF_8)));
        copyTransform(subject, proxy);
        Map<EquipmentSlot, ItemStack> equipment =
                effectiveEquipment(viewer, subject, record.equipmentPolicy());
        ProxyView created = new ProxyView(allocation, subject.getId(), proxy, equipment);
        synchronized (PROXIES) {
            PROXIES.put(new ObserverSubject(viewer.getUUID(), subject.getUUID()), created);
        }
        viewer.connection.send(new ClientboundRemoveEntitiesPacket(subject.getId()));
        viewer.connection.send(new ClientboundAddEntityPacket(proxy, 0, proxy.blockPosition()));
        List<net.minecraft.network.syncher.SynchedEntityData.DataValue<?>> metadata =
                proxy.getEntityData().getNonDefaultValues();
        if (metadata != null && !metadata.isEmpty()) {
            viewer.connection.send(new ClientboundSetEntityDataPacket(proxy.getId(), metadata));
        }
        sendEquipment(viewer, proxy.getId(), equipment);
        viewer.connection.send(new ClientboundSetEntityMotionPacket(proxy.getId(), proxy.getDeltaMovement()));
    }

    private static void copyTransform(ServerPlayer subject, Entity proxy) {
        proxy.setPos(subject.position());
        proxy.setYRot(subject.getYRot());
        proxy.setXRot(subject.getXRot());
        proxy.setYHeadRot(subject.getYHeadRot());
        proxy.setPose(subject.getPose());
        proxy.setOnGround(subject.onGround());
        proxy.setDeltaMovement(subject.getDeltaMovement());
    }

    private static Map<EquipmentSlot, ItemStack> effectiveEquipment(
            ServerPlayer viewer,
            ServerPlayer subject,
            DisguiseService.EquipmentPolicy policy
    ) {
        boolean reveal = policy == DisguiseService.EquipmentPolicy.SHOW_REAL_EQUIPMENT
                || policy == DisguiseService.EquipmentPolicy.STAFF_REVEAL
                && PermissionService.has(
                        viewer,
                        PermissionsHandler.phasePermission("commands.disguise.inspect"));
        Map<EquipmentSlot, ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            boolean held = slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND;
            boolean visible = reveal
                    || policy == DisguiseService.EquipmentPolicy.HELD_ITEM_ONLY && held;
            equipment.put(slot, visible ? subject.getItemBySlot(slot).copy() : ItemStack.EMPTY);
        }
        return Map.copyOf(equipment);
    }

    private static boolean sameEquipment(
            Map<EquipmentSlot, ItemStack> first,
            Map<EquipmentSlot, ItemStack> second
    ) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!ItemStack.matches(
                    first.getOrDefault(slot, ItemStack.EMPTY),
                    second.getOrDefault(slot, ItemStack.EMPTY))) {
                return false;
            }
        }
        return true;
    }

    private static void sendEquipment(
            ServerPlayer viewer,
            int entityId,
            Map<EquipmentSlot, ItemStack> equipment
    ) {
        List<Pair<EquipmentSlot, ItemStack>> packetEquipment = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            packetEquipment.add(Pair.of(
                    slot,
                    equipment.getOrDefault(slot, ItemStack.EMPTY).copy()));
        }
        viewer.connection.send(new ClientboundSetEquipmentPacket(entityId, packetEquipment));
    }

    private static void removeViewer(ServerPlayer viewer, boolean restoreReal) {
        List<ObserverSubject> keys;
        synchronized (PROXIES) {
            keys = PROXIES.keySet().stream()
                    .filter(key -> key.observerId().equals(viewer.getUUID()))
                    .toList();
        }
        keys.forEach(key -> remove(viewer.server, key, restoreReal));
    }

    private static void remove(MinecraftServer server, ObserverSubject key, boolean restoreReal) {
        ProxyView removed;
        synchronized (PROXIES) {
            removed = PROXIES.remove(key);
        }
        KernelServices.disguiseProxyIds().release(key.observerId(), key.subjectId());
        if (removed == null) {
            return;
        }
        ServerPlayer viewer = server.getPlayerList().getPlayer(key.observerId());
        if (viewer == null) {
            return;
        }
        viewer.connection.send(new ClientboundRemoveEntitiesPacket(removed.proxy().getId()));
        if (!restoreReal) {
            return;
        }
        ServerPlayer subject = server.getPlayerList().getPlayer(key.subjectId());
        if (subject == null
                || subject.level() != viewer.level()
                || VanishUtil.isVanished(subject, viewer)
                || viewer.distanceToSqr(subject) > MAXIMUM_TRACKING_DISTANCE_SQUARED) {
            return;
        }
        viewer.connection.send(new ClientboundAddEntityPacket(subject, 0, subject.blockPosition()));
        List<net.minecraft.network.syncher.SynchedEntityData.DataValue<?>> metadata =
                subject.getEntityData().getNonDefaultValues();
        if (metadata != null && !metadata.isEmpty()) {
            viewer.connection.send(new ClientboundSetEntityDataPacket(subject.getId(), metadata));
        }
        sendEquipment(viewer, subject.getId(), effectiveEquipment(
                viewer,
                subject,
                DisguiseService.EquipmentPolicy.SHOW_REAL_EQUIPMENT));
        viewer.connection.send(new ClientboundSetEntityMotionPacket(
                subject.getId(),
                subject.getDeltaMovement()));
    }

    private static boolean supportsEnhancedDisguises(ServerPlayer viewer) {
        return SefSessionManager.instance().session(viewer)
                .map(session -> session.supports(SefProtocol.Feature.DISGUISE_PROJECTION))
                .orElse(false);
    }

    private record ObserverSubject(UUID observerId, UUID subjectId) {
    }

    private record ProxyView(
            ProxyEntityIdAllocator.Allocation allocation,
            int realEntityId,
            Entity proxy,
            Map<EquipmentSlot, ItemStack> equipment
    ) {
    }
}
