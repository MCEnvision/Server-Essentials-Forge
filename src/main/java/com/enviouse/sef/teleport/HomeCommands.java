package com.enviouse.sef.teleport;

import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.identity.IdentityArguments;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.QuotaService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;

public final class HomeCommands {
    private HomeCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (!ConfigHandler.config.enableTeleportEssentials.get() || !ConfigHandler.config.enableHomes.get()) {
            return;
        }
        if (KernelServices.teleportSettings().ownershipMode() == TeleportSettings.OwnershipMode.EXTERNAL) {
            return;
        }
        boolean coexist = KernelServices.teleportSettings().ownershipMode()
                == TeleportSettings.OwnershipMode.COEXIST;
        dispatcher.register(setHomeNode(coexist ? "sefsethome" : "sethome"));
        dispatcher.register(homeNode(coexist ? "sefhome" : "home"));
        dispatcher.register(homesNode(coexist ? "sefhomes" : "homes"));
        dispatcher.register(deleteHomeNode(coexist ? "sefdelhome" : "delhome"));
        dispatcher.register(renameHomeNode(coexist ? "sefrenamehome" : "renamehome"));
        dispatcher.register(homeAdminNode(coexist ? "sefhomeadmin" : "homeadmin"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> setHomeNode(String literal) {
        return Commands.literal(literal)
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.setHomeCommand,
                        "sef:teleport.home.set"))
                .executes(context -> setHome(
                        context.getSource(),
                        TeleportCommandSupport.player(context.getSource(), "sef:teleport.home.set"),
                        KernelServices.teleportSettings().defaultHomeName(),
                        false,
                        "sef:teleport.home.set",
                        PermissionsHandler.setHomeCommand))
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(context -> setHome(
                                context.getSource(),
                                TeleportCommandSupport.player(context.getSource(), "sef:teleport.home.set"),
                                StringArgumentType.getString(context, "name"),
                                false,
                                "sef:teleport.home.set",
                                PermissionsHandler.setHomeCommand))
                        .then(Commands.literal("confirm")
                                .executes(context -> setHome(
                                        context.getSource(),
                                        TeleportCommandSupport.player(context.getSource(), "sef:teleport.home.set"),
                                        StringArgumentType.getString(context, "name"),
                                        true,
                                        "sef:teleport.home.set",
                                        PermissionsHandler.setHomeCommand))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> homeNode(String literal) {
        return Commands.literal(literal)
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.homeCommand,
                        "sef:teleport.home.use"))
                .executes(context -> home(
                        context.getSource(),
                        KernelServices.teleportSettings().defaultHomeName()))
                .then(Commands.argument("name", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                ownHomeNames(context.getSource()),
                                builder))
                        .executes(context -> home(
                                context.getSource(),
                                StringArgumentType.getString(context, "name"))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> homesNode(String literal) {
        return Commands.literal(literal)
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.homesCommand,
                        "sef:teleport.home.list"))
                .executes(context -> listHomes(
                        context.getSource(),
                        TeleportCommandSupport.player(
                                context.getSource(),
                                "sef:teleport.home.list")))
                .then(IdentityArguments.online("player")
                        .requires(source -> TeleportCommandSupport.has(source, PermissionsHandler.homesOthersCommand))
                        .executes(context -> {
                            ServerPlayer actor = TeleportCommandSupport.player(context.getSource());
                            ServerPlayer target = IdentityArguments.getOnline(context, "player");
                            return actor != null && TeleportCommandSupport.mayTarget(
                                    context.getSource(), actor, target, true)
                                    ? listHomes(context.getSource(), target)
                                    : 0;
                        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> deleteHomeNode(String literal) {
        return Commands.literal(literal)
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.deleteHomeCommand,
                        "sef:teleport.home.delete"))
                .then(Commands.argument("name", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                ownHomeNames(context.getSource()),
                                builder))
                        .executes(context -> {
                            String name = StringArgumentType.getString(context, "name");
                            TeleportCommandSupport.info(
                                    context.getSource(),
                                    "Run /" + commandRoot("delhome") + " " + name
                                            + " confirm to delete that home.");
                            return 0;
                        })
                        .then(Commands.literal("confirm")
                                .executes(context -> deleteHome(
                                        context.getSource(),
                                        TeleportCommandSupport.player(context.getSource()),
                                        StringArgumentType.getString(context, "name"),
                                        "sef:teleport.home.delete",
                                        PermissionsHandler.deleteHomeCommand))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> renameHomeNode(String literal) {
        return Commands.literal(literal)
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.renameHomeCommand,
                        "sef:teleport.home.rename"))
                .then(Commands.argument("current", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                ownHomeNames(context.getSource()),
                                builder))
                        .then(Commands.argument("replacement", StringArgumentType.word())
                                .executes(context -> renameHome(
                                        context.getSource(),
                                        TeleportCommandSupport.player(context.getSource()),
                                        StringArgumentType.getString(context, "current"),
                                        StringArgumentType.getString(context, "replacement"),
                                        "sef:teleport.home.rename",
                                        PermissionsHandler.renameHomeCommand))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> homeAdminNode(String literal) {
        return Commands.literal(literal)
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.homeAdminCommand,
                        "sef:teleport.home.admin"))
                .then(Commands.literal("list")
                        .requires(source -> TeleportCommandSupport.has(source, PermissionsHandler.homeAdminList))
                        .then(IdentityArguments.online("player")
                                .executes(context -> {
                                    ServerPlayer target = IdentityArguments.getOnline(context, "player");
                                    return mayAdminTarget(context.getSource(), target)
                                            ? listHomes(context.getSource(), target)
                                            : 0;
                                })))
                .then(Commands.literal("teleport")
                        .requires(source -> TeleportCommandSupport.has(source, PermissionsHandler.homeAdminTeleport))
                        .then(IdentityArguments.online("player")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                KernelServices.teleports().homes(
                                                                IdentityArguments.getOnline(context, "player").getUUID())
                                                        .stream()
                                                        .map(HomeRecord::displayName),
                                                builder))
                                        .executes(context -> adminTeleport(
                                                context.getSource(),
                                                IdentityArguments.getOnline(context, "player"),
                                                StringArgumentType.getString(context, "name"))))))
                .then(Commands.literal("set")
                        .requires(source -> TeleportCommandSupport.has(source, PermissionsHandler.homeAdminSet))
                        .then(IdentityArguments.online("player")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(context -> setHome(
                                                context.getSource(),
                                                adminTarget(context.getSource(), IdentityArguments.getOnline(context, "player")),
                                                StringArgumentType.getString(context, "name"),
                                                true,
                                                "sef:teleport.home.admin",
                                                PermissionsHandler.homeAdminSet)))))
                .then(Commands.literal("delete")
                        .requires(source -> TeleportCommandSupport.has(source, PermissionsHandler.homeAdminDelete))
                        .then(IdentityArguments.online("player")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(context -> deleteHome(
                                                context.getSource(),
                                                adminTarget(context.getSource(), IdentityArguments.getOnline(context, "player")),
                                                StringArgumentType.getString(context, "name"),
                                                "sef:teleport.home.admin",
                                                PermissionsHandler.homeAdminDelete)))))
                .then(Commands.literal("rename")
                        .requires(source -> TeleportCommandSupport.has(source, PermissionsHandler.homeAdminRename))
                        .then(IdentityArguments.online("player")
                                .then(Commands.argument("current", StringArgumentType.word())
                                        .then(Commands.argument("replacement", StringArgumentType.word())
                                                .executes(context -> renameHome(
                                                        context.getSource(),
                                                        adminTarget(context.getSource(), IdentityArguments.getOnline(context, "player")),
                                                        StringArgumentType.getString(context, "current"),
                                                        StringArgumentType.getString(context, "replacement"),
                                                        "sef:teleport.home.admin",
                                                        PermissionsHandler.homeAdminRename))))))
                .then(Commands.literal("restore")
                        .requires(source -> TeleportCommandSupport.has(source, PermissionsHandler.homeAdminRestore))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(context -> restoreHome(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "id")))))
                .then(Commands.literal("limit")
                        .requires(source -> TeleportCommandSupport.has(source, PermissionsHandler.homeAdminLimit))
                        .then(IdentityArguments.online("player")
                                .executes(context -> {
                                    ServerPlayer target = IdentityArguments.getOnline(context, "player");
                                    return mayAdminTarget(context.getSource(), target)
                                            ? showLimit(context.getSource(), target)
                                            : 0;
                                })))
                .then(Commands.literal("export")
                        .requires(source -> TeleportCommandSupport.has(source, PermissionsHandler.homeAdminExport))
                        .executes(context -> {
                            int count = KernelServices.teleports().allHomes(true).size();
                            TeleportCommandSupport.info(
                                    context.getSource(),
                                    "The versioned teleport repository contains " + count + " home records.");
                            return count;
                        }));
    }

    private static int setHome(
            CommandSourceStack source,
            ServerPlayer owner,
            String name,
            boolean overwrite,
            String actionId,
            net.neoforged.neoforge.server.permission.nodes.PermissionNode<Boolean> permission
    ) {
        if (owner == null) {
            return 0;
        }
        return KernelCommandExecutor.execute(
                source,
                actionId,
                java.util.Map.of(
                        "operation", "set",
                        "name", name,
                        "overwrite", Boolean.toString(overwrite)),
                List.of(owner.getUUID()),
                false,
                () -> setHomeInternal(source, owner, name, overwrite),
                permission);
    }

    private static int setHomeInternal(
            CommandSourceStack source,
            ServerPlayer owner,
            String name,
            boolean overwrite
    ) {
        TeleportRepository repository = KernelServices.teleports();
        boolean replacement = repository.home(owner.getUUID(), name).isPresent();
        SavedLocation location = SavedLocation.from(owner);
        long currentUsage = repository.homes(owner.getUUID()).size();
        QuotaService.Context quotaContext = TeleportCommandSupport.quotaContext(
                owner,
                "sef:homes",
                "sef:teleport.home.set",
                currentUsage);
        long limit = KernelServices.quotas().resolve(quotaContext).effectiveValue();
        long currentDimensionUsage = repository.homes(owner.getUUID()).stream()
                .filter(home -> home.location().dimensionId().equals(location.dimensionId()))
                .count();
        QuotaService.Context dimensionQuotaContext = TeleportCommandSupport.quotaContext(
                owner,
                "sef:homes_per_dimension",
                "sef:teleport.home.set",
                currentDimensionUsage);
        long dimensionLimit = KernelServices.quotas().resolve(dimensionQuotaContext).effectiveValue();
        ActionResult<QuotaService.Reservation> reserved = replacement
                ? ActionResult.success(null)
                : KernelServices.quotas().reserve(quotaContext, 1);
        if (!reserved.successful()) {
            TeleportCommandSupport.fail(source, "Your home limit is " + limit + ".");
            return 0;
        }
        try (QuotaService.Reservation reservation = reserved.value()) {
            ActionResult<QuotaService.Reservation> dimensionReserved = replacement
                    ? ActionResult.success(null)
                    : KernelServices.quotas().reserve(dimensionQuotaContext, 1);
            if (!dimensionReserved.successful()) {
                TeleportCommandSupport.fail(
                        source,
                        "Your home limit in " + location.dimensionId() + " is " + dimensionLimit + ".");
                return 0;
            }
            try (QuotaService.Reservation dimensionReservation = dimensionReserved.value()) {
            ActionResult<HomeRecord> result;
            try {
                result = repository.setHome(
                        owner.getUUID(),
                        name,
                        location,
                        limit,
                        dimensionLimit,
                        overwrite);
            } catch (IllegalArgumentException exception) {
                TeleportCommandSupport.fail(source, exception.getMessage());
                return 0;
            }
            if (!result.successful()) {
                if (result.reason() == ActionResult.ReasonCode.CONFIRMATION_REQUIRED) {
                    TeleportCommandSupport.info(
                            source,
                            "That home exists. Run /" + commandRoot("sethome") + " "
                                    + name + " confirm to replace it.");
                } else if (result.reason() == ActionResult.ReasonCode.QUOTA_EXCEEDED) {
                    TeleportCommandSupport.fail(source, "Your home limit is " + limit + ".");
                } else {
                    TeleportCommandSupport.fail(source, result.detail());
                }
                return 0;
            }
            if (reservation != null) {
                reservation.commit();
            }
            if (dimensionReservation != null) {
                dimensionReservation.commit();
            }
            TeleportCommandSupport.success(
                    source,
                    (replacement ? "Updated home " : "Created home ") + result.value().displayName() + ".");
            return 1;
            }
        }
    }

    private static int home(CommandSourceStack source, String name) {
        ServerPlayer player = TeleportCommandSupport.player(source, "sef:teleport.home.use");
        if (player == null) {
            return 0;
        }
        ActionResult<HomeRecord> result;
        try {
            result = KernelServices.teleports().home(player.getUUID(), name)
                    .map(ActionResult::success)
                    .orElseGet(() -> ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "home not found"));
        } catch (IllegalArgumentException exception) {
            TeleportCommandSupport.fail(source, exception.getMessage());
            return 0;
        }
        if (!result.successful()) {
            List<HomeRecord> homes = KernelServices.teleports().homes(player.getUUID());
            if (homes.isEmpty()) {
                TeleportCommandSupport.fail(source, "You do not have any homes.");
            } else {
                TeleportCommandSupport.fail(source, "Home not found. Your homes are " + joinNames(homes) + ".");
            }
            return 0;
        }
        HomeRecord home = result.value();
        return TeleportCommandSupport.teleport(
                source,
                player,
                player,
                home.location(),
                "sef:teleport.home.use",
                "home",
                PermissionsHandler.homeCommand,
                KernelServices.teleportSettings().userPolicy(),
                () -> KernelServices.teleports().homeById(home.id())
                        .filter(HomeRecord::active)
                        .map(current -> current.revision() == home.revision())
                        .orElse(false));
    }

    private static int adminTeleport(CommandSourceStack source, ServerPlayer owner, String name) {
        ServerPlayer actor = TeleportCommandSupport.player(source);
        if (actor == null || !TeleportCommandSupport.mayTarget(source, actor, owner, false)) {
            return 0;
        }
        HomeRecord home;
        try {
            home = KernelServices.teleports().home(owner.getUUID(), name).orElse(null);
        } catch (IllegalArgumentException exception) {
            TeleportCommandSupport.fail(source, exception.getMessage());
            return 0;
        }
        if (home == null) {
            TeleportCommandSupport.fail(source, "Home not found.");
            return 0;
        }
        return TeleportCommandSupport.teleport(
                source,
                actor,
                actor,
                home.location(),
                "sef:teleport.home.admin",
                "home_admin",
                PermissionsHandler.homeAdminTeleport,
                KernelServices.teleportSettings().userPolicy(),
                () -> KernelServices.teleports().homeById(home.id())
                        .filter(HomeRecord::active)
                        .map(current -> current.revision() == home.revision())
                        .orElse(false));
    }

    private static int listHomes(CommandSourceStack source, ServerPlayer owner) {
        if (owner == null) {
            return 0;
        }
        List<HomeRecord> homes = KernelServices.teleports().homes(owner.getUUID());
        if (homes.isEmpty()) {
            TeleportCommandSupport.info(source, owner.getGameProfile().getName() + " has no homes.");
            return 0;
        }
        boolean coordinates = TeleportCommandSupport.has(source, PermissionsHandler.homesCoordinates);
        TeleportCommandSupport.info(source, "Homes for " + owner.getGameProfile().getName() + ".");
        homes.forEach(home -> {
            String line = home.displayName();
            if (coordinates) {
                line += " at " + home.location().dimensionId()
                        + " " + Math.floor(home.location().x())
                        + " " + Math.floor(home.location().y())
                        + " " + Math.floor(home.location().z());
            }
            TeleportCommandSupport.info(source, line);
        });
        return homes.size();
    }

    private static int deleteHome(
            CommandSourceStack source,
            ServerPlayer owner,
            String name,
            String actionId,
            net.neoforged.neoforge.server.permission.nodes.PermissionNode<Boolean> permission
    ) {
        if (owner == null) {
            return 0;
        }
        return KernelCommandExecutor.execute(
                source,
                actionId,
                java.util.Map.of("operation", "delete", "name", name),
                List.of(owner.getUUID()),
                false,
                () -> deleteHomeInternal(source, owner, name),
                permission);
    }

    private static int deleteHomeInternal(CommandSourceStack source, ServerPlayer owner, String name) {
        ActionResult<HomeRecord> result;
        try {
            result = KernelServices.teleports().deleteHome(owner.getUUID(), name);
        } catch (IllegalArgumentException exception) {
            TeleportCommandSupport.fail(source, exception.getMessage());
            return 0;
        }
        if (!result.successful()) {
            TeleportCommandSupport.fail(source, "Home not found.");
            return 0;
        }
        TeleportCommandSupport.success(
                source,
                "Deleted home " + result.value().displayName() + ". Recovery id " + result.value().id() + ".");
        return 1;
    }

    private static int renameHome(
            CommandSourceStack source,
            ServerPlayer owner,
            String current,
            String replacement,
            String actionId,
            net.neoforged.neoforge.server.permission.nodes.PermissionNode<Boolean> permission
    ) {
        if (owner == null) {
            return 0;
        }
        return KernelCommandExecutor.execute(
                source,
                actionId,
                java.util.Map.of(
                        "operation", "rename",
                        "current", current,
                        "replacement", replacement),
                List.of(owner.getUUID()),
                false,
                () -> renameHomeInternal(source, owner, current, replacement),
                permission);
    }

    private static int renameHomeInternal(
            CommandSourceStack source,
            ServerPlayer owner,
            String current,
            String replacement
    ) {
        ActionResult<HomeRecord> result;
        try {
            result = KernelServices.teleports().renameHome(owner.getUUID(), current, replacement);
        } catch (IllegalArgumentException exception) {
            TeleportCommandSupport.fail(source, exception.getMessage());
            return 0;
        }
        if (!result.successful()) {
            TeleportCommandSupport.fail(source, result.detail());
            return 0;
        }
        TeleportCommandSupport.success(source, "Renamed home to " + result.value().displayName() + ".");
        return 1;
    }

    private static int restoreHome(CommandSourceStack source, String id) {
        return KernelCommandExecutor.execute(
                source,
                "sef:teleport.home.admin",
                java.util.Map.of("operation", "restore", "home_id", id),
                () -> restoreHomeInternal(source, id),
                PermissionsHandler.homeAdminRestore);
    }

    private static int restoreHomeInternal(CommandSourceStack source, String id) {
        UUID homeId;
        try {
            homeId = UUID.fromString(id);
        } catch (IllegalArgumentException exception) {
            TeleportCommandSupport.fail(source, "The recovery id is not a valid UUID.");
            return 0;
        }
        HomeRecord deleted = KernelServices.teleports().homeById(homeId).orElse(null);
        if (deleted == null || deleted.active()) {
            TeleportCommandSupport.fail(source, "Deleted home not found.");
            return 0;
        }
        long limit = TeleportCommandSupport.offlineQuota(
                deleted.ownerId(),
                "sef:homes",
                "sef:teleport.home.admin",
                KernelServices.teleports().homes(deleted.ownerId()).size(),
                deleted.location().dimensionId());
        long dimensionUsage = KernelServices.teleports().homes(deleted.ownerId()).stream()
                .filter(home -> home.location().dimensionId().equals(deleted.location().dimensionId()))
                .count();
        long dimensionLimit = TeleportCommandSupport.offlineQuota(
                deleted.ownerId(),
                "sef:homes_per_dimension",
                "sef:teleport.home.admin",
                dimensionUsage,
                deleted.location().dimensionId());
        ActionResult<HomeRecord> result =
                KernelServices.teleports().restoreHome(homeId, limit, dimensionLimit);
        if (!result.successful()) {
            TeleportCommandSupport.fail(source, result.detail());
            return 0;
        }
        TeleportCommandSupport.success(source, "Restored home " + result.value().displayName() + ".");
        return 1;
    }

    private static int showLimit(CommandSourceStack source, ServerPlayer player) {
        long usage = KernelServices.teleports().homes(player.getUUID()).size();
        long limit = TeleportCommandSupport.quota(
                player,
                "sef:homes",
                "sef:teleport.home.list",
                usage);
        TeleportCommandSupport.info(
                source,
                player.getGameProfile().getName() + " uses " + usage + " of " + limit + " homes.");
        return 1;
    }

    private static ServerPlayer adminTarget(CommandSourceStack source, ServerPlayer target) {
        return mayAdminTarget(source, target) ? target : null;
    }

    private static boolean mayAdminTarget(CommandSourceStack source, ServerPlayer target) {
        ServerPlayer actor = source.getPlayer();
        return actor == null || actor == target || TeleportCommandSupport.mayTarget(source, actor, target, false);
    }

    private static java.util.stream.Stream<String> ownHomeNames(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        return player == null
                ? java.util.stream.Stream.empty()
                : KernelServices.teleports().homes(player.getUUID()).stream().map(HomeRecord::displayName);
    }

    private static String joinNames(List<HomeRecord> homes) {
        return homes.stream().map(HomeRecord::displayName).collect(java.util.stream.Collectors.joining(", "));
    }

    private static String commandRoot(String canonical) {
        return KernelServices.teleportSettings().ownershipMode() == TeleportSettings.OwnershipMode.COEXIST
                ? "sef" + canonical
                : canonical;
    }
}
