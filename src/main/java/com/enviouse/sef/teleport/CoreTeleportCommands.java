package com.enviouse.sef.teleport;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.identity.IdentityArguments;
import com.enviouse.sef.identity.IdentityService;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.storage.repository.LocationHistoryRepository;
import com.enviouse.sef.vanish.VanishUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public final class CoreTeleportCommands {
    private CoreTeleportCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (!ConfigHandler.config.enableTeleportEssentials.get()) {
            return;
        }
        if (ConfigHandler.config.enableBack.get()) {
            registerBack(dispatcher);
        }
        if (ConfigHandler.config.enableSpawnCommands.get()) {
            registerSpawn(dispatcher);
        }
        if (ConfigHandler.config.enableRandomTeleport.get()) {
            registerRandom(dispatcher);
        }
        if (ConfigHandler.config.enableDirectTeleport.get()) {
            registerDirect(dispatcher);
        }
    }

    private static void registerBack(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("back")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.backCommand,
                        "sef:teleport.back"))
                .executes(context -> back(context.getSource(), false))
                .then(Commands.literal("death")
                        .requires(source -> TeleportCommandSupport.has(source, PermissionsHandler.backDeathCommand))
                        .executes(context -> back(context.getSource(), true))));
    }

    private static void registerSpawn(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("spawn")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.spawnCommand,
                        "sef:teleport.spawn"))
                .executes(context -> spawn(context.getSource())));
        dispatcher.register(Commands.literal("setspawn")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.setSpawnCommand,
                        "sef:teleport.spawn.set"))
                .executes(context -> setSpawn(context.getSource(), "default"))
                .then(Commands.literal("default")
                        .executes(context -> setSpawn(context.getSource(), "default")))
                .then(Commands.literal("first_join")
                        .executes(context -> setSpawn(context.getSource(), "first_join")))
                .then(Commands.literal("death")
                        .executes(context -> setSpawn(context.getSource(), "death")))
                .then(Commands.literal("group")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> setSpawn(
                                        context.getSource(),
                                        "group:" + StringArgumentType.getString(context, "name")))))
                .then(Commands.literal("dimension")
                        .executes(context -> {
                            ServerPlayer player = TeleportCommandSupport.player(
                                    context.getSource(), "sef:teleport.spawn.set");
                            return player == null
                                    ? 0
                                    : setSpawn(
                                            context.getSource(),
                                            "dimension:" + player.serverLevel().dimension().location());
                        })));
        dispatcher.register(Commands.literal("spawninfo")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.spawnInfoCommand,
                        "sef:teleport.spawn.info"))
                .executes(context -> spawnInfo(context.getSource())));
    }

    private static void registerRandom(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("rtp")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.randomTeleportCommand,
                        "sef:teleport.random"))
                .executes(context -> randomTeleport(context.getSource())));
        dispatcher.register(Commands.literal("tpr")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.randomTeleportCommand,
                        "sef:teleport.random"))
                .executes(context -> randomTeleport(context.getSource())));
        dispatcher.register(Commands.literal("settpr")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.setRandomTeleportCommand,
                        "sef:teleport.random.set"))
                .executes(context -> {
                    ServerPlayer player = TeleportCommandSupport.player(
                            context.getSource(), "sef:teleport.random.set");
                    if (player == null) {
                        return 0;
                    }
                    return setSpawn(
                            context.getSource(),
                            "rtp:center:" + player.serverLevel().dimension().location());
                }));
    }

    private static void registerDirect(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (ConfigHandler.config.ownVanillaTeleportRoot.get()) {
            dispatcher.register(Commands.literal("tp")
                    .requires(source -> TeleportCommandSupport.has(
                            source,
                            PermissionsHandler.directTeleportCommand,
                            "sef:teleport.direct"))
                    .then(IdentityArguments.online("target")
                            .executes(context -> directToPlayer(
                                    context.getSource(),
                                    TeleportCommandSupport.player(context.getSource()),
                                    IdentityArguments.getOnline(context, "target"),
                                    false))
                            .then(IdentityArguments.online("destination")
                                    .executes(context -> directToPlayer(
                                            context.getSource(),
                                            IdentityArguments.getOnline(context, "target"),
                                            IdentityArguments.getOnline(context, "destination"),
                                            false)))));
        }
        dispatcher.register(Commands.literal("tphere")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.teleportHereCommand,
                        "sef:teleport.direct.here"))
                .then(IdentityArguments.online("player")
                        .executes(context -> teleportHere(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "player"),
                                false))));
        dispatcher.register(Commands.literal("tpo")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.teleportOverrideCommand,
                        "sef:teleport.direct.override"))
                .then(IdentityArguments.online("player")
                        .executes(context -> directToPlayer(
                                context.getSource(),
                                TeleportCommandSupport.player(context.getSource()),
                                IdentityArguments.getOnline(context, "player"),
                                true))));
        dispatcher.register(Commands.literal("tpohere")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.teleportOverrideHereCommand,
                        "sef:teleport.direct.override"))
                .then(IdentityArguments.online("player")
                        .executes(context -> teleportHere(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "player"),
                                true))));
        dispatcher.register(Commands.literal("tppos")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.teleportPositionCommand,
                        "sef:teleport.direct.position"))
                .then(Commands.argument("position", Vec3Argument.vec3())
                        .executes(context -> teleportPosition(
                                context.getSource(),
                                Vec3Argument.getVec3(context, "position")))));
        dispatcher.register(Commands.literal("tpall")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.teleportAllCommand,
                        "sef:teleport.direct.all"))
                .executes(context -> teleportAll(context.getSource())));
        dispatcher.register(Commands.literal("tpoffline")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.teleportOfflineCommand,
                        "sef:teleport.direct.offline"))
                .then(IdentityArguments.known("player")
                        .then(Commands.argument("position", Vec3Argument.vec3())
                                .executes(context -> queueOffline(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "player"),
                                        Vec3Argument.getVec3(context, "position"))))));
    }

    private static int back(CommandSourceStack source, boolean deathOnly) {
        ServerPlayer player = TeleportCommandSupport.player(source, "sef:teleport.back");
        if (player == null) {
            return 0;
        }
        List<LocationHistoryRepository.LocationRecord> history =
                new ArrayList<>(KernelServices.locationHistory().history(player.getUUID()));
        Collections.reverse(history);
        for (LocationHistoryRepository.LocationRecord record : history) {
            if (deathOnly && !record.reason().equalsIgnoreCase("death")) {
                continue;
            }
            SavedLocation candidate = new SavedLocation(
                    record.dimensionId(),
                    record.x(),
                    record.y(),
                    record.z(),
                    record.yaw(),
                    record.pitch());
            if (KernelServices.safeTeleports().validate(
                    source.getServer(),
                    player,
                    player,
                    candidate,
                    KernelServices.teleportSettings().userPolicy()).successful()) {
                return TeleportCommandSupport.teleport(
                        source,
                        player,
                        player,
                        candidate,
                        "sef:teleport.back",
                        deathOnly ? "back_death" : "back",
                        deathOnly ? PermissionsHandler.backDeathCommand : PermissionsHandler.backCommand,
                        KernelServices.teleportSettings().userPolicy(),
                        SafeTeleportService.DestinationGuard.ALWAYS);
            }
        }
        TeleportCommandSupport.fail(
                source,
                deathOnly ? "No valid death location is available." : "No valid back location is available.");
        return 0;
    }

    private static int spawn(CommandSourceStack source) {
        ServerPlayer player = TeleportCommandSupport.player(source, "sef:teleport.spawn");
        if (player == null) {
            return 0;
        }
        String dimensionKey = "dimension:" + player.serverLevel().dimension().location();
        String primaryGroup = ServerEssentialsForge.instance == null
                || ServerEssentialsForge.instance.metadataProvider == null
                ? ""
                : ServerEssentialsForge.instance.metadataProvider.getPrimaryGroup(player.getGameProfile());
        TeleportRepository.SpawnRecord record = (primaryGroup.isBlank()
                ? java.util.Optional.<TeleportRepository.SpawnRecord>empty()
                : KernelServices.teleports().spawn("group:" + primaryGroup))
                .or(() -> KernelServices.teleports().spawn(dimensionKey))
                .or(() -> KernelServices.teleports().spawn("default"))
                .orElse(null);
        SavedLocation location;
        long revision;
        String key;
        if (record == null) {
            ServerLevel level = source.getServer().overworld();
            BlockPos position = level.getSharedSpawnPos();
            location = new SavedLocation(
                    level.dimension().location().toString(),
                    position.getX() + 0.5D,
                    position.getY(),
                    position.getZ() + 0.5D,
                    level.getSharedSpawnAngle(),
                    0);
            revision = 0;
            key = "";
        } else {
            location = record.location();
            revision = record.revision();
            key = record.key();
        }
        String finalKey = key;
        long finalRevision = revision;
        return TeleportCommandSupport.teleport(
                source,
                player,
                player,
                location,
                "sef:teleport.spawn",
                "spawn",
                PermissionsHandler.spawnCommand,
                KernelServices.teleportSettings().userPolicy(),
                record == null
                        ? SafeTeleportService.DestinationGuard.ALWAYS
                        : () -> KernelServices.teleports().spawn(finalKey)
                                .map(current -> current.revision() == finalRevision)
                                .orElse(false));
    }

    private static int setSpawn(CommandSourceStack source, String key) {
        String actionId = key.startsWith("rtp:center:")
                ? "sef:teleport.random.set"
                : "sef:teleport.spawn.set";
        ServerPlayer player = TeleportCommandSupport.player(source, actionId);
        if (player == null) {
            return 0;
        }
        boolean randomCenter = key.startsWith("rtp:center:");
        return KernelCommandExecutor.execute(
                source,
                randomCenter ? "sef:teleport.random.set" : "sef:teleport.spawn.set",
                java.util.Map.of("operation", "set", "layer", key),
                () -> setSpawnInternal(source, player, key),
                randomCenter
                        ? PermissionsHandler.setRandomTeleportCommand
                        : PermissionsHandler.setSpawnCommand);
    }

    private static int setSpawnInternal(CommandSourceStack source, ServerPlayer player, String key) {
        long revision = KernelServices.teleports().spawn(key).map(TeleportRepository.SpawnRecord::revision).orElse(0L) + 1L;
        KernelServices.teleports().setSpawn(new TeleportRepository.SpawnRecord(
                key,
                SavedLocation.from(player),
                "",
                player.getUUID(),
                Instant.now(),
                revision));
        TeleportCommandSupport.success(source, "Set spawn layer " + key + ".");
        return 1;
    }

    private static int spawnInfo(CommandSourceStack source) {
        return KernelCommandExecutor.execute(
                source,
                "sef:teleport.spawn.info",
                java.util.Map.of("operation", "list"),
                () -> spawnInfoInternal(source),
                PermissionsHandler.spawnInfoCommand);
    }

    private static int spawnInfoInternal(CommandSourceStack source) {
        List<TeleportRepository.SpawnRecord> spawns = KernelServices.teleports().spawns();
        if (spawns.isEmpty()) {
            TeleportCommandSupport.info(source, "No custom spawn layers are configured.");
            return 0;
        }
        spawns.forEach(spawn -> TeleportCommandSupport.info(
                source,
                spawn.key() + " at " + spawn.location().dimensionId()
                        + " " + Math.floor(spawn.location().x())
                        + " " + Math.floor(spawn.location().y())
                        + " " + Math.floor(spawn.location().z())
                        + " revision " + spawn.revision()));
        return spawns.size();
    }

    private static int randomTeleport(CommandSourceStack source) {
        ServerPlayer player = TeleportCommandSupport.player(source, "sef:teleport.random");
        if (player == null) {
            return 0;
        }
        TeleportSettings settings = KernelServices.teleportSettings();
        ServerLevel level = player.serverLevel();
        String dimension = level.dimension().location().toString();
        if (!settings.randomAllowedDimensions().contains(dimension)) {
            TeleportCommandSupport.fail(source, "Random teleport is disabled in this dimension.");
            return 0;
        }
        SavedLocation center = KernelServices.teleports().spawn("rtp:center:" + dimension)
                .map(TeleportRepository.SpawnRecord::location)
                .orElseGet(() -> new SavedLocation(
                        dimension,
                        level.getSharedSpawnPos().getX() + 0.5D,
                        level.getSharedSpawnPos().getY(),
                        level.getSharedSpawnPos().getZ() + 0.5D,
                        player.getYRot(),
                        player.getXRot()));
        ThreadLocalRandom random = ThreadLocalRandom.current();
        SavedLocation destination = null;
        for (int attempt = 0; attempt < settings.randomMaximumAttempts(); attempt++) {
            double angle = random.nextDouble(Math.PI * 2.0D);
            double minimumSquared = (double) settings.randomMinimumRadius() * settings.randomMinimumRadius();
            double maximumSquared = (double) settings.randomMaximumRadius() * settings.randomMaximumRadius();
            double distance = Math.sqrt(random.nextDouble(minimumSquared, maximumSquared + 1.0D));
            int x = (int) Math.floor(center.x() + Math.cos(angle) * distance);
            int z = (int) Math.floor(center.z() + Math.sin(angle) * distance);
            BlockPos column = new BlockPos(x, level.getMinBuildHeight(), z);
            if (!level.hasChunkAt(column)) {
                continue;
            }
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            SavedLocation candidate = new SavedLocation(
                    dimension,
                    x + 0.5D,
                    y,
                    z + 0.5D,
                    player.getYRot(),
                    player.getXRot());
            if (KernelServices.safeTeleports().validate(
                    source.getServer(),
                    player,
                    player,
                    candidate,
                    settings.userPolicy(),
                    true).successful()) {
                destination = candidate;
                break;
            }
        }
        if (destination == null) {
            TeleportCommandSupport.fail(
                    source,
                    "No safe destination was found within the bounded loaded chunk search.");
            return 0;
        }
        return TeleportCommandSupport.teleport(
                source,
                player,
                player,
                destination,
                "sef:teleport.random",
                "random_teleport",
                PermissionsHandler.randomTeleportCommand,
                settings.userPolicy(),
                SafeTeleportService.DestinationGuard.ALWAYS,
                true);
    }

    private static int directToPlayer(
            CommandSourceStack source,
            ServerPlayer moving,
            ServerPlayer destination,
            boolean override
    ) {
        ServerPlayer actor = TeleportCommandSupport.player(
                source,
                override ? "sef:teleport.direct.override" : "sef:teleport.direct");
        if (actor == null || moving == null || destination == null) {
            return 0;
        }
        if (moving != actor && !TeleportCommandSupport.mayTarget(source, actor, moving, false)) {
            return 0;
        }
        return TeleportCommandSupport.teleport(
                source,
                actor,
                moving,
                SavedLocation.from(destination),
                override ? "sef:teleport.direct.override" : "sef:teleport.direct",
                override ? "teleport_override" : "teleport_direct",
                override ? PermissionsHandler.teleportOverrideCommand : PermissionsHandler.directTeleportCommand,
                administrativePolicy(actor, override),
                () -> source.getServer().getPlayerList().getPlayer(destination.getUUID()) != null);
    }

    private static int teleportHere(
            CommandSourceStack source,
            ServerPlayer moving,
            boolean override
    ) {
        ServerPlayer actor = TeleportCommandSupport.player(
                source,
                override ? "sef:teleport.direct.override" : "sef:teleport.direct.here");
        if (actor == null || !TeleportCommandSupport.mayTarget(source, actor, moving, false)) {
            return 0;
        }
        return TeleportCommandSupport.teleport(
                source,
                actor,
                moving,
                SavedLocation.from(actor),
                override ? "sef:teleport.direct.override" : "sef:teleport.direct.here",
                override ? "teleport_override_here" : "teleport_here",
                override ? PermissionsHandler.teleportOverrideHereCommand : PermissionsHandler.teleportHereCommand,
                administrativePolicy(actor, override),
                () -> source.getServer().getPlayerList().getPlayer(actor.getUUID()) != null);
    }

    private static int teleportPosition(CommandSourceStack source, Vec3 position) {
        ServerPlayer actor = TeleportCommandSupport.player(source, "sef:teleport.direct.position");
        if (actor == null) {
            return 0;
        }
        SavedLocation destination = new SavedLocation(
                actor.serverLevel().dimension().location().toString(),
                position.x,
                position.y,
                position.z,
                actor.getYRot(),
                actor.getXRot());
        return TeleportCommandSupport.teleport(
                source,
                actor,
                actor,
                destination,
                "sef:teleport.direct.position",
                "teleport_position",
                PermissionsHandler.teleportPositionCommand,
                administrativePolicy(actor, false),
                SafeTeleportService.DestinationGuard.ALWAYS);
    }

    private static int teleportAll(CommandSourceStack source) {
        ServerPlayer actor = TeleportCommandSupport.player(source, "sef:teleport.direct.all");
        if (actor == null) {
            return 0;
        }
        SavedLocation destination = SavedLocation.from(actor);
        int teleported = 0;
        for (ServerPlayer target : source.getServer().getPlayerList().getPlayers()) {
            if (teleported >= 100
                    || target == actor
                    || VanishUtil.isVanished(target, actor)
                    || !TeleportCommandSupport.mayTarget(source, actor, target, false)) {
                continue;
            }
            teleported += TeleportCommandSupport.teleport(
                    source,
                    actor,
                    target,
                    destination,
                    "sef:teleport.direct.all",
                    "teleport_all",
                    PermissionsHandler.teleportAllCommand,
                    administrativePolicy(actor, false),
                    SafeTeleportService.DestinationGuard.ALWAYS);
        }
        TeleportCommandSupport.info(source, "Teleported " + teleported + " players.");
        return teleported;
    }

    private static int queueOffline(CommandSourceStack source, String identity, Vec3 position) {
        ServerPlayer actor = TeleportCommandSupport.player(source, "sef:teleport.direct.offline");
        if (actor == null) {
            return 0;
        }
        ActionResult<IdentityService.Identity> resolved = KernelServices.identities().resolve(identity, actor);
        if (!resolved.successful() || resolved.value().playerId() == null) {
            TeleportCommandSupport.fail(source, resolved.detail());
            return 0;
        }
        return KernelCommandExecutor.execute(
                source,
                "sef:teleport.direct.offline",
                java.util.Map.of("operation", "queue"),
                java.util.List.of(resolved.value().playerId()),
                false,
                () -> queueOfflineInternal(source, actor, resolved.value(), position),
                PermissionsHandler.teleportOfflineCommand);
    }

    private static int queueOfflineInternal(
            CommandSourceStack source,
            ServerPlayer actor,
            IdentityService.Identity identity,
            Vec3 position
    ) {
        SavedLocation location = new SavedLocation(
                actor.serverLevel().dimension().location().toString(),
                position.x,
                position.y,
                position.z,
                actor.getYRot(),
                actor.getXRot());
        KernelServices.teleports().queueOfflineTeleport(new TeleportRepository.PendingOfflineTeleport(
                identity.playerId(),
                location,
                actor.getUUID(),
                "offline_admin",
                Instant.now(),
                1));
        TeleportCommandSupport.success(
                source,
                "Queued an offline teleport for " + identity.authenticatedUsername() + ".");
        return 1;
    }

    private static SafeTeleportService.Policy administrativePolicy(ServerPlayer actor, boolean override) {
        SafeTeleportService.Policy base = KernelServices.teleportSettings().userPolicy();
        boolean bypass = override && TeleportCommandSupport.has(actor, PermissionsHandler.teleportSafetyBypass);
        return new SafeTeleportService.Policy(
                base.searchRadius(),
                base.maximumChecks(),
                base.maximumChunks(),
                bypass || base.allowHazards(),
                bypass || base.allowNetherRoof(),
                true,
                true,
                base.invulnerabilityTicks());
    }
}
