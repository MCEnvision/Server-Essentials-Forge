package com.enviouse.sef.disguise;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.gui.protocol.SefGuiServer;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.vanish.VanishUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.time.Instant;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = ServerEssentialsForge.MODID)
public final class DisguiseRuntime {
    private static long lastProjectionRevision;
    private static int synchronizedEntityTypeCount = -1;
    private static final Map<UUID, EnumMap<SoundKind, Integer>> LAST_SOUND_TICKS = new HashMap<>();

    private DisguiseRuntime() {
    }

    @SubscribeEvent
    public static void serverStarted(ServerStartedEvent event) {
        synchronizeEntityAdapters();
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 2 == 0) {
            DisguiseProxyService.tick(event.getServer());
        }
        if (event.getServer().getTickCount() % 20 != 0) {
            return;
        }
        DisguiseService service = KernelServices.disguises();
        for (DisguiseService.DisguiseRecord record : service.active()) {
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(record.subjectId());
            if (player == null || !record.traitsEnabled()) {
                continue;
            }
            DisguiseService.MobAdapter adapter = service.supportedMobs().stream()
                    .filter(value -> value.entityType().equals(record.reference()))
                    .findFirst()
                    .orElse(null);
            if (adapter != null && adapter.traits().contains(DisguiseService.Trait.FIRE_RESISTANCE)) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.FIRE_RESISTANCE,
                        40,
                        0,
                        false,
                        false,
                        false));
            }
            if (adapter != null) {
                applyTraits(player, adapter);
            }
            if (ConfigHandler.config.disguiseSoundsEnabled.get()
                    && event.getServer().getTickCount() % 100 == Math.floorMod(
                    record.subjectId().hashCode(),
                    100)) {
                playProfileSound(player, record.reference(), SoundKind.AMBIENT, 0.75F, 1.0F);
            }
        }
        long revision = service.revision();
        if (revision != lastProjectionRevision) {
            lastProjectionRevision = revision;
            SefGuiServer.sendDisguiseSnapshot(event.getServer());
        }
    }

    public static void reset() {
        lastProjectionRevision = 0L;
        synchronizedEntityTypeCount = -1;
        synchronized (LAST_SOUND_TICKS) {
            LAST_SOUND_TICKS.clear();
        }
        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            DisguiseProxyService.clear(server);
        }
    }

    static synchronized void synchronizeEntityAdapters() {
        int registrySize = BuiltInRegistries.ENTITY_TYPE.size();
        if (synchronizedEntityTypeCount == registrySize) {
            return;
        }
        DisguiseService service = KernelServices.disguises();
        for (var entry : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
            if (entry.getValue().getCategory() == MobCategory.MISC) {
                continue;
            }
            service.registerEnhancedMobAdapter(
                    entry.getKey().location().toString(),
                    entry.getValue().getDescription().getString(),
                    entry.getKey().location().getNamespace().equals("minecraft"));
        }
        synchronizedEntityTypeCount = registrySize;
    }

    public static void playBlazeShoot(ServerPlayer subject) {
        if (ConfigHandler.config.disguiseSoundsEnabled.get()) {
            if (admitSound(subject, SoundKind.ABILITY, 5)) {
                playToVisible(subject, SoundEvents.BLAZE_SHOOT, 1.0F, 1.0F);
            }
        }
    }

    public static void cacheProfile(ServerPlayer player) {
        com.mojang.authlib.properties.Property textures = player.getGameProfile()
                .getProperties()
                .get("textures")
                .stream()
                .filter(com.mojang.authlib.properties.Property::hasSignature)
                .findFirst()
                .orElse(null);
        if (textures == null) {
            return;
        }
        Instant now = Instant.now();
        KernelServices.disguises().cacheProfile(new DisguiseService.ProfileSnapshot(
                player.getUUID(),
                player.getGameProfile().getName(),
                textures.value(),
                textures.signature(),
                true,
                now,
                now.plusSeconds(3600)));
    }

    public static void logout(UUID playerId) {
        synchronized (LAST_SOUND_TICKS) {
            LAST_SOUND_TICKS.remove(playerId);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onHurt(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || event.getNewDamage() <= 0.0F
                || !ConfigHandler.config.disguiseSoundsEnabled.get()) {
            return;
        }
        KernelServices.disguises().active(player.getUUID()).ifPresent(record ->
                playProfileSound(player, record.reference(), SoundKind.HURT, 0.9F, 1.0F));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !ConfigHandler.config.disguiseSoundsEnabled.get()) {
            return;
        }
        KernelServices.disguises().active(player.getUUID()).ifPresent(record ->
                playProfileSound(player, record.reference(), SoundKind.DEATH, 1.0F, 1.0F));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        DisguiseService.DisguiseRecord record =
                KernelServices.disguises().active(player.getUUID()).orElse(null);
        if (record == null || !record.traitsEnabled()) {
            return;
        }
        KernelServices.disguises().supportedMobs().stream()
                .filter(adapter -> adapter.entityType().equals(record.reference()))
                .findFirst()
                .filter(adapter -> adapter.traits().contains(DisguiseService.Trait.REDUCED_FALL_DAMAGE))
                .ifPresent(adapter -> event.setDamageMultiplier(event.getDamageMultiplier() * 0.25F));
    }

    private static void applyTraits(
            ServerPlayer player,
            DisguiseService.MobAdapter adapter
    ) {
        if (adapter.traits().contains(DisguiseService.Trait.WATER_BREATHING)) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.WATER_BREATHING, 40, 0, false, false, false));
        }
        if (adapter.traits().contains(DisguiseService.Trait.SWIM_SPEED) && player.isInWater()) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.DOLPHINS_GRACE, 40, 0, false, false, false));
        }
        if (adapter.traits().contains(DisguiseService.Trait.CLIMBING)
                && player.horizontalCollision
                && !player.isShiftKeyDown()) {
            player.setDeltaMovement(
                    player.getDeltaMovement().x,
                    Math.max(0.2D, player.getDeltaMovement().y),
                    player.getDeltaMovement().z);
            player.hurtMarked = true;
        }
        if (adapter.traits().contains(DisguiseService.Trait.CONTROLLED_FLIGHT)) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.SLOW_FALLING, 40, 0, false, false, false));
        }
        if (adapter.traits().contains(DisguiseService.Trait.WATER_VULNERABILITY)
                && player.isInWaterRainOrBubble()
                && player.server.getTickCount() % 20 == 0) {
            player.hurt(player.damageSources().drown(), 1.0F);
        }
        if (adapter.traits().contains(DisguiseService.Trait.DAYLIGHT_SENSITIVITY)
                && player.level().isDay()
                && player.level().canSeeSky(player.blockPosition())
                && player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).isEmpty()
                && !player.isInWaterRainOrBubble()) {
            player.igniteForSeconds(2);
        }
    }

    private static void playProfileSound(
            ServerPlayer subject,
            String reference,
            SoundKind kind,
            float volume,
            float pitch
    ) {
        int minimumTicks = switch (kind) {
            case AMBIENT -> 80;
            case HURT, ATTACK, ABILITY -> 5;
            case DEATH -> 0;
        };
        if (!admitSound(subject, kind, minimumTicks)) {
            return;
        }
        String path = reference.startsWith("minecraft:")
                ? reference.substring("minecraft:".length())
                : reference;
        String eventName = switch (kind) {
            case AMBIENT -> path.equals("bee")
                    ? "entity.bee.loop"
                    : path.equals("slime")
                    ? "entity.slime.squish"
                    : path.equals("creeper")
                    ? ""
                    : "entity." + path + ".ambient";
            case HURT -> "entity." + path + ".hurt";
            case DEATH -> "entity." + path + ".death";
            case ATTACK, ABILITY -> "";
        };
        ResourceLocation id = ResourceLocation.tryParse("minecraft:" + eventName);
        if (eventName.isBlank() || id == null || !BuiltInRegistries.SOUND_EVENT.containsKey(id)) {
            return;
        }
        playToVisible(subject, BuiltInRegistries.SOUND_EVENT.get(id), volume, pitch);
    }

    private static boolean admitSound(
            ServerPlayer subject,
            SoundKind kind,
            int minimumTicks
    ) {
        int now = subject.server.getTickCount();
        synchronized (LAST_SOUND_TICKS) {
            EnumMap<SoundKind, Integer> byKind =
                    LAST_SOUND_TICKS.computeIfAbsent(subject.getUUID(), ignored ->
                            new EnumMap<>(SoundKind.class));
            Integer previous = byKind.get(kind);
            if (previous != null && now - previous < minimumTicks) {
                return false;
            }
            byKind.put(kind, now);
            return true;
        }
    }

    private static void playToVisible(
            ServerPlayer subject,
            SoundEvent sound,
            float volume,
            float pitch
    ) {
        for (ServerPlayer viewer : subject.server.getPlayerList().getPlayers()) {
            if (viewer != subject && VanishUtil.isVanished(subject, viewer)) {
                continue;
            }
            viewer.connection.send(new ClientboundSoundPacket(
                    BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),
                    SoundSource.PLAYERS,
                    subject.getX(),
                    subject.getY(),
                    subject.getZ(),
                    volume,
                    pitch,
                    subject.getRandom().nextLong()));
        }
    }

    private enum SoundKind {
        AMBIENT,
        HURT,
        DEATH,
        ATTACK,
        ABILITY
    }
}
