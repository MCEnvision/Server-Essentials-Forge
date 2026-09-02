package com.enviouse.sef.player;

import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.permissions.PermissionService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerStateService {
    private static final Set<UUID> AFK = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, StateGrant> FLY = new ConcurrentHashMap<>();
    private static final Map<UUID, StateGrant> GOD = new ConcurrentHashMap<>();
    private static final Map<UUID, PersonalTime> TIMES = new ConcurrentHashMap<>();
    private static final Map<UUID, PersonalWeather> WEATHER = new ConcurrentHashMap<>();
    private static final Map<UUID, StateGrant> TIME_GRANTS = new ConcurrentHashMap<>();
    private static final Map<UUID, StateGrant> WEATHER_GRANTS = new ConcurrentHashMap<>();

    private PlayerStateService() {
    }

    public static boolean toggleAfk(UUID playerId) {
        if (AFK.remove(playerId)) {
            return false;
        }
        AFK.add(playerId);
        return true;
    }

    public static boolean afk(UUID playerId) {
        return AFK.contains(playerId);
    }

    public static void setFly(
            ServerPlayer player,
            boolean enabled,
            CommandSourceStack source,
            boolean other
    ) {
        if (enabled) {
            FLY.put(player.getUUID(), StateGrant.from(source, other));
        } else {
            FLY.remove(player.getUUID());
        }
        player.getAbilities().mayfly = enabled || player.isCreative() || player.isSpectator();
        if (!player.getAbilities().mayfly) {
            player.getAbilities().flying = false;
        }
        player.onUpdateAbilities();
    }

    public static boolean fly(UUID playerId) {
        return FLY.containsKey(playerId);
    }

    public static void setGod(UUID playerId, boolean enabled, CommandSourceStack source, boolean other) {
        if (enabled) {
            GOD.put(playerId, StateGrant.from(source, other));
        } else {
            GOD.remove(playerId);
        }
    }

    public static boolean god(UUID playerId) {
        return GOD.containsKey(playerId);
    }

    public static void setPersonalTime(
            ServerPlayer player,
            Long dayTime,
            CommandSourceStack source,
            boolean other
    ) {
        if (dayTime == null) {
            TIMES.remove(player.getUUID());
            TIME_GRANTS.remove(player.getUUID());
        } else {
            TIMES.put(player.getUUID(), new PersonalTime(Math.floorMod(dayTime, 24000L)));
            TIME_GRANTS.put(player.getUUID(), StateGrant.from(source, other));
        }
        sendPersonalTime(player);
    }

    public static void setPersonalWeather(
            ServerPlayer player,
            PersonalWeather weather,
            CommandSourceStack source,
            boolean other
    ) {
        if (weather == null || weather == PersonalWeather.RESET) {
            WEATHER.remove(player.getUUID());
            WEATHER_GRANTS.remove(player.getUUID());
        } else {
            WEATHER.put(player.getUUID(), weather);
            WEATHER_GRANTS.put(player.getUUID(), StateGrant.from(source, other));
        }
        sendPersonalWeather(player);
    }

    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % 100 != 0) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            StateGrant flyGrant = FLY.get(player.getUUID());
            if (flyGrant != null && !authorized(server, player, flyGrant, "fly")) {
                setFly(player, false, player.createCommandSourceStack(), false);
            }
            StateGrant godGrant = GOD.get(player.getUUID());
            if (godGrant != null && !authorized(server, player, godGrant, "god")) {
                GOD.remove(player.getUUID());
            }
            StateGrant timeGrant = TIME_GRANTS.get(player.getUUID());
            if (timeGrant != null && !authorized(server, player, timeGrant, "ptime")) {
                TIMES.remove(player.getUUID());
                TIME_GRANTS.remove(player.getUUID());
                sendPersonalTime(player);
            } else if (TIMES.containsKey(player.getUUID())) {
                sendPersonalTime(player);
            }
            StateGrant weatherGrant = WEATHER_GRANTS.get(player.getUUID());
            if (weatherGrant != null && !authorized(server, player, weatherGrant, "pweather")) {
                WEATHER.remove(player.getUUID());
                WEATHER_GRANTS.remove(player.getUUID());
                sendPersonalWeather(player);
            } else if (WEATHER.containsKey(player.getUUID())) {
                sendPersonalWeather(player);
            }
        }
    }

    public static void clearPlayer(ServerPlayer player) {
        UUID playerId = player.getUUID();
        AFK.remove(playerId);
        FLY.remove(playerId);
        GOD.remove(playerId);
        TIMES.remove(playerId);
        WEATHER.remove(playerId);
        TIME_GRANTS.remove(playerId);
        WEATHER_GRANTS.remove(playerId);
        player.getAbilities().mayfly = player.isCreative() || player.isSpectator();
        if (!player.getAbilities().mayfly) {
            player.getAbilities().flying = false;
        }
        player.onUpdateAbilities();
    }

    public static void clearAll() {
        AFK.clear();
        FLY.clear();
        GOD.clear();
        TIMES.clear();
        WEATHER.clear();
        TIME_GRANTS.clear();
        WEATHER_GRANTS.clear();
    }

    private static void sendPersonalTime(ServerPlayer player) {
        PersonalTime personal = TIMES.get(player.getUUID());
        long dayTime = personal == null ? player.serverLevel().getDayTime() : personal.dayTime();
        player.connection.send(new ClientboundSetTimePacket(
                player.serverLevel().getGameTime(),
                dayTime,
                personal == null && player.serverLevel().getGameRules()
                        .getBoolean(net.minecraft.world.level.GameRules.RULE_DAYLIGHT)));
    }

    private static void sendPersonalWeather(ServerPlayer player) {
        PersonalWeather weather = WEATHER.get(player.getUUID());
        if (weather == null) {
            boolean raining = player.serverLevel().isRaining();
            player.connection.send(new ClientboundGameEventPacket(
                    raining
                            ? ClientboundGameEventPacket.START_RAINING
                            : ClientboundGameEventPacket.STOP_RAINING,
                    0.0F));
            player.connection.send(new ClientboundGameEventPacket(
                    ClientboundGameEventPacket.RAIN_LEVEL_CHANGE,
                    player.serverLevel().getRainLevel(1.0F)));
            player.connection.send(new ClientboundGameEventPacket(
                    ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE,
                    player.serverLevel().getThunderLevel(1.0F)));
            return;
        }
        boolean raining = weather != PersonalWeather.CLEAR;
        player.connection.send(new ClientboundGameEventPacket(
                raining
                        ? ClientboundGameEventPacket.START_RAINING
                        : ClientboundGameEventPacket.STOP_RAINING,
                0.0F));
        player.connection.send(new ClientboundGameEventPacket(
                ClientboundGameEventPacket.RAIN_LEVEL_CHANGE,
                raining ? 1.0F : 0.0F));
        player.connection.send(new ClientboundGameEventPacket(
                ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE,
                weather == PersonalWeather.THUNDER ? 1.0F : 0.0F));
    }

    private record PersonalTime(long dayTime) {
    }

    private static boolean authorized(
            MinecraftServer server,
            ServerPlayer target,
            StateGrant grant,
            String action
    ) {
        if (!ConfigHandler.config.enablePlayerUtilities.get()) {
            return false;
        }
        CommandSourceStack source;
        if (grant.actorId() == null) {
            source = server.createCommandSourceStack();
        } else {
            ServerPlayer actor = server.getPlayerList().getPlayer(grant.actorId());
            if (actor == null) {
                return false;
            }
            source = actor.createCommandSourceStack();
        }
        var permission = PermissionsHandler.phasePermission(
                "commands." + action + (grant.other() ? ".others" : ""));
        if (permission == null || !PermissionService.has(source, permission)) {
            return false;
        }
        if (!grant.other()) {
            return source.getPlayer() == target;
        }
        return PlayerTargetPolicy.decide(
                source,
                target,
                PermissionsHandler.phasePermission("utilities.hierarchy.bypass"),
                PermissionsHandler.phasePermission("exempt.utility"),
                PermissionsHandler.phasePermission("utilities.bypass.exempt"),
                false,
                true).allowed();
    }

    private record StateGrant(UUID actorId, boolean other) {
        private static StateGrant from(CommandSourceStack source, boolean other) {
            ServerPlayer actor = source.getPlayer();
            return new StateGrant(actor == null ? null : actor.getUUID(), other);
        }
    }

    public enum PersonalWeather {
        CLEAR,
        RAIN,
        THUNDER,
        RESET
    }
}
