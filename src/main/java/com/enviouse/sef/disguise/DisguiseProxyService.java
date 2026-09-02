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
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
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
import java.util.concurrent.TimeUnit;

public final class DisguiseProxyService {
    private static final int MAXIMUM_PROXIES_PER_VIEWER = SefProtocol.MAXIMUM_DISGUISE_PROJECTIONS;
    private static final int MAXIMUM_TOTAL_PROXIES = 65_536;
    private static final int SPATIAL_BUCKET_SIZE = 32;
    private static final int SPATIAL_BUCKET_RADIUS = 4;
    private static final int MAXIMUM_SYNCHRONIZATION_CHECKS_PER_TICK = 8_192;
    private static final long MAXIMUM_SYNCHRONIZATION_NANOS = TimeUnit.MILLISECONDS.toNanos(2);
    private static final double MAXIMUM_TRACKING_DISTANCE_SQUARED = 128.0D * 128.0D;
    private static final double MAXIMUM_INTERACTION_DISTANCE_SQUARED = 3.0D * 3.0D;
    private static final Map<ObserverSubject, ProxyView> PROXIES = new HashMap<>();
    private static final Map<ObserverEntity, ObserverSubject> REAL_ENTITY_INDEX = new HashMap<>();
    private static final Map<UUID, PermissionCache> PERMISSION_CACHE = new HashMap<>();
    private static int viewerCursor;
    private static volatile TickDiagnostic lastDiagnostic = new TickDiagnostic(0, 0, 0L);

    private DisguiseProxyService() {
    }

    public static void tick(MinecraftServer server) {
        Objects.requireNonNull(server, "server");
        if (!ConfigHandler.config.enableDisguises.get()
                || !ConfigHandler.config.disguiseVanillaProxyEnabled.get()) {
            clear(server);
            return;
        }
        long started = System.nanoTime();
        List<ServerPlayer> viewers = List.copyOf(server.getPlayerList().getPlayers());
        if (viewers.isEmpty()) {
            lastDiagnostic = new TickDiagnostic(0, 0, System.nanoTime() - started);
            return;
        }
        SubjectIndex index = subjectIndex(server);
        int checks = 0;
        int synchronizedViewers = 0;
        int start = Math.floorMod(viewerCursor, viewers.size());
        for (int offset = 0; offset < viewers.size(); offset++) {
            if (checks >= MAXIMUM_SYNCHRONIZATION_CHECKS_PER_TICK
                    || System.nanoTime() - started >= MAXIMUM_SYNCHRONIZATION_NANOS) {
                break;
            }
            int viewerIndex = Math.floorMod(start + offset, viewers.size());
            ServerPlayer viewer = viewers.get(viewerIndex);
            int remaining = MAXIMUM_SYNCHRONIZATION_CHECKS_PER_TICK - checks;
            checks += synchronizeViewer(viewer, index, remaining, server.getTickCount());
            synchronizedViewers++;
            viewerCursor = Math.floorMod(viewerIndex + 1, viewers.size());
        }
        lastDiagnostic = new TickDiagnostic(
                synchronizedViewers,
                checks,
                System.nanoTime() - started);
    }

    public static boolean shouldSuppressRealSpawn(ServerPlayer viewer, int entityId) {
        synchronized (PROXIES) {
            return REAL_ENTITY_INDEX.containsKey(new ObserverEntity(viewer.getUUID(), entityId));
        }
    }

    public static TickDiagnostic diagnostic() {
        return lastDiagnostic;
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
        synchronized (PROXIES) {
            PERMISSION_CACHE.remove(playerId);
        }
    }

    public static void clear(MinecraftServer server) {
        List<ObserverSubject> keys;
        synchronized (PROXIES) {
            keys = List.copyOf(PROXIES.keySet());
        }
        keys.forEach(key -> remove(server, key, true));
        KernelServices.disguiseProxyIds().clear();
        synchronized (PROXIES) {
            REAL_ENTITY_INDEX.clear();
            PERMISSION_CACHE.clear();
            viewerCursor = 0;
        }
    }

    public static int size() {
        synchronized (PROXIES) {
            return PROXIES.size();
        }
    }

    private static int synchronizeViewer(
            ServerPlayer viewer,
            SubjectIndex index,
            int maximumChecks,
            int tick
    ) {
        if (supportsEnhancedDisguises(viewer)) {
            removeViewer(viewer, true);
            return 1;
        }
        boolean staff = staff(viewer, tick);
        Set<UUID> desired = new HashSet<>();
        int accepted = 0;
        int checks = 0;
        for (IndexedSubject indexed : index.nearby(viewer)) {
            if (accepted >= MAXIMUM_PROXIES_PER_VIEWER || checks >= maximumChecks) {
                break;
            }
            checks++;
            ServerPlayer subject = indexed.subject();
            DisguiseService.DisguiseRecord record = indexed.record();
            if (subject == viewer
                    || viewer.distanceToSqr(subject) > MAXIMUM_TRACKING_DISTANCE_SQUARED) {
                continue;
            }
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
            synchronize(viewer, subject, projection.record(), staff);
            accepted++;
        }
        if (checks < maximumChecks) {
            List<ObserverSubject> stale;
            synchronized (PROXIES) {
                stale = PROXIES.keySet().stream()
                        .filter(key -> key.observerId().equals(viewer.getUUID())
                                && !desired.contains(key.subjectId()))
                        .toList();
            }
            stale.forEach(key -> remove(viewer.server, key, true));
        }
        return Math.max(1, checks);
    }

    private static void synchronize(
            ServerPlayer viewer,
            ServerPlayer subject,
            DisguiseService.DisguiseRecord record,
            boolean staff
    ) {
        ObserverSubject key = new ObserverSubject(viewer.getUUID(), subject.getUUID());
        ProxyView current;
        synchronized (PROXIES) {
            current = PROXIES.get(key);
        }
        if (current == null
                || current.allocation().disguiseRevision() != record.revision()
                || current.realEntityId() != subject.getId()) {
            if (current != null) {
                remove(viewer.server, key, false);
            }
            spawn(viewer, subject, record, staff);
            return;
        }
        copyTransform(subject, current.proxy());
        current.tracker().sendChanges();
        sendAnimations(viewer, subject, current);
        Map<EquipmentSlot, ItemStack> equipment =
                effectiveEquipment(subject, record.equipmentPolicy(), staff);
        if (!sameEquipment(current.equipment(), equipment)) {
            sendEquipment(viewer, current.proxy().getId(), equipment);
            current.equipment(equipment);
        }
    }

    private static void spawn(
            ServerPlayer viewer,
            ServerPlayer subject,
            DisguiseService.DisguiseRecord record,
            boolean staff
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
                effectiveEquipment(subject, record.equipmentPolicy(), staff);
        ServerEntity tracker = new ServerEntity(
                subject.serverLevel(),
                proxy,
                1,
                true,
                packet -> viewer.connection.send(packet));
        ProxyView created = new ProxyView(
                allocation,
                subject.getId(),
                proxy,
                tracker,
                equipment,
                subject.swinging,
                subject.swingTime,
                subject.hurtTime);
        synchronized (PROXIES) {
            ObserverSubject key = new ObserverSubject(viewer.getUUID(), subject.getUUID());
            PROXIES.put(key, created);
            REAL_ENTITY_INDEX.put(new ObserverEntity(viewer.getUUID(), subject.getId()), key);
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
        proxy.setSprinting(subject.isSprinting());
        proxy.setShiftKeyDown(subject.isShiftKeyDown());
        proxy.setSwimming(subject.isSwimming());
        proxy.setGlowingTag(subject.isCurrentlyGlowing());
        if (proxy instanceof LivingEntity living) {
            living.setYBodyRot(subject.yBodyRot);
            living.setArrowCount(subject.getArrowCount());
            living.setStingerCount(subject.getStingerCount());
        }
        if (proxy instanceof Bat bat) {
            bat.setResting(false);
        }
    }

    private static void sendAnimations(
            ServerPlayer viewer,
            ServerPlayer subject,
            ProxyView view
    ) {
        if (subject.swinging
                && (!view.swinging() || subject.swingTime < view.swingTime())) {
            int action = subject.swingingArm == InteractionHand.OFF_HAND
                    ? ClientboundAnimatePacket.SWING_OFF_HAND
                    : ClientboundAnimatePacket.SWING_MAIN_HAND;
            viewer.connection.send(new ClientboundAnimatePacket(view.proxy(), action));
        }
        if (subject.hurtTime > view.hurtTime()) {
            viewer.connection.send(new ClientboundEntityEventPacket(view.proxy(), (byte) 2));
        }
        view.animationState(subject.swinging, subject.swingTime, subject.hurtTime);
    }

    private static Map<EquipmentSlot, ItemStack> effectiveEquipment(
            ServerPlayer subject,
            DisguiseService.EquipmentPolicy policy,
            boolean staff
    ) {
        boolean reveal = policy == DisguiseService.EquipmentPolicy.SHOW_REAL_EQUIPMENT
                || policy == DisguiseService.EquipmentPolicy.STAFF_REVEAL
                && staff;
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
            if (removed != null) {
                REAL_ENTITY_INDEX.remove(
                        new ObserverEntity(key.observerId(), removed.realEntityId()),
                        key);
            }
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
                subject,
                DisguiseService.EquipmentPolicy.SHOW_REAL_EQUIPMENT,
                true));
        viewer.connection.send(new ClientboundSetEntityMotionPacket(
                subject.getId(),
                subject.getDeltaMovement()));
    }

    private static boolean supportsEnhancedDisguises(ServerPlayer viewer) {
        return SefSessionManager.instance().session(viewer)
                .map(session -> session.supports(SefProtocol.Feature.DISGUISE_PROJECTION))
                .orElse(false);
    }

    private static SubjectIndex subjectIndex(MinecraftServer server) {
        Map<UUID, DisguiseService.DisguiseRecord> active = new HashMap<>();
        for (DisguiseService.DisguiseRecord record : KernelServices.disguises().active()) {
            active.put(record.subjectId(), record);
        }
        Map<SpatialBucket, List<IndexedSubject>> buckets = new HashMap<>();
        for (ServerPlayer subject : server.getPlayerList().getPlayers()) {
            DisguiseService.DisguiseRecord record = active.get(subject.getUUID());
            if (record == null) {
                continue;
            }
            SpatialBucket bucket = SpatialBucket.of(subject);
            buckets.computeIfAbsent(bucket, ignored -> new ArrayList<>())
                    .add(new IndexedSubject(subject, record));
        }
        buckets.replaceAll((ignored, subjects) -> List.copyOf(subjects));
        return new SubjectIndex(Map.copyOf(buckets));
    }

    private static boolean staff(ServerPlayer viewer, int tick) {
        synchronized (PROXIES) {
            PermissionCache cached = PERMISSION_CACHE.get(viewer.getUUID());
            if (cached != null && cached.expiresAfterTick() >= tick) {
                return cached.staff();
            }
        }
        boolean staff = PermissionService.has(
                viewer,
                PermissionsHandler.phasePermission("commands.disguise.inspect"));
        synchronized (PROXIES) {
            PERMISSION_CACHE.put(viewer.getUUID(), new PermissionCache(staff, tick + 20));
        }
        return staff;
    }

    private record ObserverSubject(UUID observerId, UUID subjectId) {
    }

    private record ObserverEntity(UUID observerId, int realEntityId) {
    }

    private record PermissionCache(boolean staff, int expiresAfterTick) {
    }

    private record IndexedSubject(
            ServerPlayer subject,
            DisguiseService.DisguiseRecord record
    ) {
    }

    private record SpatialBucket(String dimensionId, int x, int z) {
        private static SpatialBucket of(ServerPlayer player) {
            return new SpatialBucket(
                    player.level().dimension().location().toString(),
                    Math.floorDiv(player.getBlockX(), SPATIAL_BUCKET_SIZE),
                    Math.floorDiv(player.getBlockZ(), SPATIAL_BUCKET_SIZE));
        }
    }

    private record SubjectIndex(Map<SpatialBucket, List<IndexedSubject>> buckets) {
        private List<IndexedSubject> nearby(ServerPlayer viewer) {
            SpatialBucket origin = SpatialBucket.of(viewer);
            List<IndexedSubject> result = new ArrayList<>();
            for (int x = -SPATIAL_BUCKET_RADIUS; x <= SPATIAL_BUCKET_RADIUS; x++) {
                for (int z = -SPATIAL_BUCKET_RADIUS; z <= SPATIAL_BUCKET_RADIUS; z++) {
                    result.addAll(buckets.getOrDefault(
                            new SpatialBucket(origin.dimensionId(), origin.x() + x, origin.z() + z),
                            List.of()));
                }
            }
            return result;
        }
    }

    public record TickDiagnostic(int synchronizedViewers, int subjectChecks, long elapsedNanos) {
    }

    private static final class ProxyView {
        private final ProxyEntityIdAllocator.Allocation allocation;
        private final int realEntityId;
        private final Entity proxy;
        private final ServerEntity tracker;
        private Map<EquipmentSlot, ItemStack> equipment;
        private boolean swinging;
        private int swingTime;
        private int hurtTime;

        private ProxyView(
                ProxyEntityIdAllocator.Allocation allocation,
                int realEntityId,
                Entity proxy,
                ServerEntity tracker,
                Map<EquipmentSlot, ItemStack> equipment,
                boolean swinging,
                int swingTime,
                int hurtTime
        ) {
            this.allocation = allocation;
            this.realEntityId = realEntityId;
            this.proxy = proxy;
            this.tracker = tracker;
            this.equipment = equipment;
            this.swinging = swinging;
            this.swingTime = swingTime;
            this.hurtTime = hurtTime;
        }

        private ProxyEntityIdAllocator.Allocation allocation() {
            return allocation;
        }

        private int realEntityId() {
            return realEntityId;
        }

        private Entity proxy() {
            return proxy;
        }

        private ServerEntity tracker() {
            return tracker;
        }

        private Map<EquipmentSlot, ItemStack> equipment() {
            return equipment;
        }

        private void equipment(Map<EquipmentSlot, ItemStack> replacement) {
            equipment = replacement;
        }

        private boolean swinging() {
            return swinging;
        }

        private int swingTime() {
            return swingTime;
        }

        private int hurtTime() {
            return hurtTime;
        }

        private void animationState(boolean newSwinging, int newSwingTime, int newHurtTime) {
            swinging = newSwinging;
            swingTime = newSwingTime;
            hurtTime = newHurtTime;
        }
    }
}
