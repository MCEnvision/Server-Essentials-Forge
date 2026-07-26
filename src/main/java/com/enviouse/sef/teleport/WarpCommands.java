package com.enviouse.sef.teleport;

import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.identity.IdentityService;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.QuotaService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class WarpCommands {
    private WarpCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (!ConfigHandler.config.enableTeleportEssentials.get()) {
            return;
        }
        if (ConfigHandler.config.enableServerWarps.get()
                && KernelServices.teleportSettings().ownershipMode() != TeleportSettings.OwnershipMode.EXTERNAL) {
            registerServerWarps(
                    dispatcher,
                    KernelServices.teleportSettings().ownershipMode() == TeleportSettings.OwnershipMode.COEXIST);
        }
        if (ConfigHandler.config.enablePlayerWarps.get()) {
            registerPlayerWarps(dispatcher);
        }
    }

    private static void registerServerWarps(
            CommandDispatcher<CommandSourceStack> dispatcher,
            boolean coexist
    ) {
        dispatcher.register(serverWarpNode(coexist ? "sefwarp" : "warp"));
        dispatcher.register(Commands.literal(coexist ? "sefwarps" : "warps")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.warpsCommand,
                        "sef:teleport.warp.list"))
                .executes(context -> listServerWarps(context.getSource())));
        dispatcher.register(Commands.literal(coexist ? "sefsetwarp" : "setwarp")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.setWarpCommand,
                        "sef:teleport.warp.set"))
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(context -> setServerWarp(
                                context.getSource(),
                                StringArgumentType.getString(context, "name"),
                                false))
                        .then(Commands.literal("confirm")
                                .executes(context -> setServerWarp(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "name"),
                                        true)))));
        dispatcher.register(Commands.literal(coexist ? "sefdelwarp" : "delwarp")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.deleteWarpCommand,
                        "sef:teleport.warp.delete"))
                .then(Commands.argument("name", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                KernelServices.teleports().serverWarps(true).stream().map(WarpRecord::displayName),
                                builder))
                        .executes(context -> {
                            String name = StringArgumentType.getString(context, "name");
                            TeleportCommandSupport.info(
                                    context.getSource(),
                                    "Run /" + commandRoot("delwarp") + " " + name
                                            + " confirm to delete that warp.");
                            return 0;
                        })
                        .then(Commands.literal("confirm")
                                .executes(context -> deleteServerWarp(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "name"))))));
        dispatcher.register(Commands.literal(coexist ? "sefrenamewarp" : "renamewarp")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.renameWarpCommand,
                        "sef:teleport.warp.rename"))
                .then(Commands.argument("current", StringArgumentType.word())
                        .then(Commands.argument("replacement", StringArgumentType.word())
                                .executes(context -> renameServerWarp(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "current"),
                                        StringArgumentType.getString(context, "replacement"))))));
        dispatcher.register(Commands.literal(coexist ? "sefwarpinfo" : "warpinfo")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.warpInfoCommand,
                        "sef:teleport.warp.manage"))
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(context -> serverWarpInfo(
                                context.getSource(),
                                StringArgumentType.getString(context, "name")))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> serverWarpNode(String literal) {
        LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal(literal)
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.warpCommand,
                        "sef:teleport.warp.use"))
                .executes(context -> listServerWarps(context.getSource()));
        node.then(Commands.literal("edit")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.warpManageCommand,
                        "sef:teleport.warp.manage"))
                .then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.literal("relocate")
                                .executes(context -> relocateServerWarp(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "name"))))));
        node.then(flagNode("feature", false, true, true));
        node.then(flagNode("unfeature", false, true, false));
        node.then(statusNode("enable", WarpRecord.Status.ACTIVE));
        node.then(statusNode("disable", WarpRecord.Status.DISABLED));
        node.then(Commands.literal("restore")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.warpManageCommand,
                        "sef:teleport.warp.manage"))
                .then(Commands.argument("id", StringArgumentType.word())
                        .executes(context -> restoreWarp(
                                context.getSource(),
                                StringArgumentType.getString(context, "id"),
                                true))));
        node.then(Commands.argument("name", StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                        KernelServices.teleports().serverWarps(
                                        TeleportCommandSupport.has(
                                                context.getSource(),
                                                PermissionsHandler.warpHiddenView))
                                .stream()
                                .map(WarpRecord::displayName),
                        builder))
                .executes(context -> visitServerWarp(
                        context.getSource(),
                        StringArgumentType.getString(context, "name"))));
        return node;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> flagNode(
            String literal,
            boolean hidden,
            boolean listed,
            boolean featured
    ) {
        return Commands.literal(literal)
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.warpManageCommand,
                        "sef:teleport.warp.manage"))
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(context -> {
                            WarpRecord warp = serverWarp(
                                    context.getSource(),
                                    StringArgumentType.getString(context, "name"));
                            if (warp == null) {
                                return 0;
                            }
                            ActionResult<WarpRecord> result = KernelServices.teleports().setWarpFlags(
                                    warp.id(),
                                    hidden,
                                    listed,
                                    featured);
                            if (!result.successful()) {
                                TeleportCommandSupport.fail(context.getSource(), result.detail());
                                return 0;
                            }
                            TeleportCommandSupport.success(
                                    context.getSource(),
                                    "Updated warp " + result.value().displayName() + ".");
                            return 1;
                        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> statusNode(
            String literal,
            WarpRecord.Status status
    ) {
        return Commands.literal(literal)
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.warpManageCommand,
                        "sef:teleport.warp.manage"))
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(context -> {
                            WarpRecord warp = serverWarp(
                                    context.getSource(),
                                    StringArgumentType.getString(context, "name"));
                            if (warp == null) {
                                return 0;
                            }
                            ActionResult<WarpRecord> result =
                                    KernelServices.teleports().setWarpStatus(warp.id(), status);
                            if (!result.successful()) {
                                TeleportCommandSupport.fail(context.getSource(), result.detail());
                                return 0;
                            }
                            TeleportCommandSupport.success(
                                    context.getSource(),
                                    "Set warp " + warp.displayName() + " to " + status.name().toLowerCase(Locale.ROOT) + ".");
                            return 1;
                        }));
    }

    private static int visitServerWarp(CommandSourceStack source, String name) {
        ServerPlayer player = TeleportCommandSupport.player(source);
        if (player == null) {
            return 0;
        }
        WarpRecord warp = serverWarp(source, name);
        if (warp == null) {
            return 0;
        }
        if (warp.status() != WarpRecord.Status.ACTIVE) {
            TeleportCommandSupport.fail(source, "That warp is disabled.");
            return 0;
        }
        long revision = warp.revision();
        int moved = TeleportCommandSupport.teleport(
                source,
                player,
                player,
                warp.location(),
                "sef:teleport.warp.use",
                "server_warp",
                PermissionsHandler.warpCommand,
                KernelServices.teleportSettings().userPolicy(),
                () -> KernelServices.teleports().warpById(warp.id())
                        .filter(WarpRecord::active)
                        .map(current -> current.revision() == revision
                                && current.status() == WarpRecord.Status.ACTIVE)
                        .orElse(false));
        if (moved > 0) {
            KernelServices.teleports().recordVisit(warp.id(), revision);
        }
        return moved;
    }

    private static int listServerWarps(CommandSourceStack source) {
        boolean hidden = TeleportCommandSupport.has(source, PermissionsHandler.warpHiddenView);
        List<WarpRecord> warps = KernelServices.teleports().serverWarps(hidden).stream()
                .filter(warp -> warp.status() == WarpRecord.Status.ACTIVE)
                .toList();
        if (warps.isEmpty()) {
            TeleportCommandSupport.info(source, "No visible server warps are available.");
            return 0;
        }
        TeleportCommandSupport.info(
                source,
                "Server warps. " + warps.stream()
                        .map(WarpRecord::displayName)
                        .collect(java.util.stream.Collectors.joining(", ")));
        return warps.size();
    }

    private static int setServerWarp(CommandSourceStack source, String name, boolean overwrite) {
        ServerPlayer player = TeleportCommandSupport.player(source);
        if (player == null) {
            return 0;
        }
        ActionResult<WarpRecord> result;
        try {
            result = KernelServices.teleports().setServerWarp(name, SavedLocation.from(player), overwrite);
        } catch (IllegalArgumentException exception) {
            TeleportCommandSupport.fail(source, exception.getMessage());
            return 0;
        }
        if (!result.successful()) {
            if (result.reason() == ActionResult.ReasonCode.CONFIRMATION_REQUIRED) {
                TeleportCommandSupport.info(
                        source,
                        "That warp exists. Run /" + commandRoot("setwarp") + " "
                                + name + " confirm to relocate it.");
            } else {
                TeleportCommandSupport.fail(source, result.detail());
            }
            return 0;
        }
        TeleportCommandSupport.success(source, "Set server warp " + result.value().displayName() + ".");
        return 1;
    }

    private static int deleteServerWarp(CommandSourceStack source, String name) {
        WarpRecord warp = serverWarp(source, name);
        if (warp == null) {
            return 0;
        }
        ActionResult<WarpRecord> result = KernelServices.teleports().deleteWarp(warp.id());
        if (!result.successful()) {
            TeleportCommandSupport.fail(source, result.detail());
            return 0;
        }
        TeleportCommandSupport.success(
                source,
                "Deleted warp " + warp.displayName() + ". Recovery id " + warp.id() + ".");
        return 1;
    }

    private static int renameServerWarp(CommandSourceStack source, String current, String replacement) {
        WarpRecord warp = serverWarp(source, current);
        if (warp == null) {
            return 0;
        }
        ActionResult<WarpRecord> result;
        try {
            result = KernelServices.teleports().renameWarp(warp.id(), replacement);
        } catch (IllegalArgumentException exception) {
            TeleportCommandSupport.fail(source, exception.getMessage());
            return 0;
        }
        if (!result.successful()) {
            TeleportCommandSupport.fail(source, result.detail());
            return 0;
        }
        TeleportCommandSupport.success(source, "Renamed server warp to " + result.value().displayName() + ".");
        return 1;
    }

    private static int relocateServerWarp(CommandSourceStack source, String name) {
        ServerPlayer player = TeleportCommandSupport.player(source);
        WarpRecord warp = serverWarp(source, name);
        if (player == null || warp == null) {
            return 0;
        }
        ActionResult<WarpRecord> result =
                KernelServices.teleports().relocateWarp(warp.id(), SavedLocation.from(player));
        if (!result.successful()) {
            TeleportCommandSupport.fail(source, result.detail());
            return 0;
        }
        TeleportCommandSupport.success(source, "Relocated server warp " + warp.displayName() + ".");
        return 1;
    }

    private static int serverWarpInfo(CommandSourceStack source, String name) {
        WarpRecord warp = serverWarp(source, name);
        if (warp == null) {
            return 0;
        }
        TeleportCommandSupport.info(
                source,
                warp.displayName() + ", id " + warp.id()
                        + ", status " + warp.status().name().toLowerCase(Locale.ROOT)
                        + ", visits " + warp.visits()
                        + ", location " + warp.location().dimensionId()
                        + " " + Math.floor(warp.location().x())
                        + " " + Math.floor(warp.location().y())
                        + " " + Math.floor(warp.location().z())
                        + ", revision " + warp.revision() + ".");
        return 1;
    }

    private static WarpRecord serverWarp(CommandSourceStack source, String name) {
        try {
            WarpRecord warp = KernelServices.teleports().serverWarp(name).orElse(null);
            if (warp == null) {
                TeleportCommandSupport.fail(source, "Server warp not found.");
            }
            return warp;
        } catch (IllegalArgumentException exception) {
            TeleportCommandSupport.fail(source, exception.getMessage());
            return null;
        }
    }

    private static void registerPlayerWarps(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(playerWarpNode("pwarp", true));
        dispatcher.register(playerWarpNode("playerwarp", false));
        dispatcher.register(playerWarpNode("pw", false));
        dispatcher.register(playerWarpListNode("pwarps"));
        dispatcher.register(playerWarpListNode("playerwarps"));
        dispatcher.register(playerWarpListNode("pws"));
        dispatcher.register(Commands.literal("setpwarp")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.setPlayerWarpCommand,
                        "sef:teleport.player_warp.create"))
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(context -> createPlayerWarp(
                                context.getSource(),
                                StringArgumentType.getString(context, "name"),
                                null))));
        dispatcher.register(Commands.literal("delpwarp")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.deletePlayerWarpCommand,
                        "sef:teleport.player_warp.delete"))
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(context -> {
                            String name = StringArgumentType.getString(context, "name");
                            TeleportCommandSupport.info(
                                    context.getSource(),
                                    "Run /delpwarp " + name + " confirm to delete that player warp.");
                            return 0;
                        })
                        .then(Commands.literal("confirm")
                                .executes(context -> deleteOwnedWarp(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "name"))))));
        dispatcher.register(Commands.literal("renamepwarp")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.renamePlayerWarpCommand,
                        "sef:teleport.player_warp.rename"))
                .then(Commands.argument("current", StringArgumentType.word())
                        .then(Commands.argument("replacement", StringArgumentType.word())
                                .executes(context -> renameOwnedWarp(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "current"),
                                        StringArgumentType.getString(context, "replacement"))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> playerWarpNode(String literal, boolean management) {
        LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal(literal)
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.playerWarpCommand,
                        "sef:teleport.player_warp.use"))
                .executes(context -> listPlayerWarps(context.getSource(), null));
        if (management) {
            node.then(Commands.literal("info")
                    .requires(source -> TeleportCommandSupport.actionEnabled(
                            source,
                            "sef:teleport.player_warp.manage"))
                    .then(Commands.argument("reference", StringArgumentType.word())
                            .executes(context -> playerWarpInfo(
                                    context.getSource(),
                                    StringArgumentType.getString(context, "reference")))));
            node.then(Commands.literal("edit")
                    .requires(source -> TeleportCommandSupport.has(
                            source,
                            PermissionsHandler.playerWarpEdit,
                            "sef:teleport.player_warp.manage"))
                    .then(Commands.argument("name", StringArgumentType.word())
                            .then(Commands.literal("relocate")
                                    .executes(context -> relocateOwnedWarp(
                                            context.getSource(),
                                            StringArgumentType.getString(context, "name"))))));
            node.then(publicationNode());
            node.then(Commands.literal("unpublish")
                    .requires(source -> TeleportCommandSupport.has(
                            source,
                            PermissionsHandler.playerWarpPublish,
                            "sef:teleport.player_warp.manage"))
                    .then(Commands.argument("name", StringArgumentType.word())
                            .executes(context -> publishOwnedWarp(
                                    context.getSource(),
                                    StringArgumentType.getString(context, "name"),
                                    WarpRecord.Access.PRIVATE))));
            node.then(accessNode("trust", true));
            node.then(accessNode("untrust", false));
            node.then(blockAccessNode("block", true));
            node.then(blockAccessNode("unblock", false));
            node.then(transferNode());
            node.then(favoriteNode("favorite", true));
            node.then(favoriteNode("unfavorite", false));
            node.then(Commands.literal("report")
                    .requires(source -> TeleportCommandSupport.has(
                            source,
                            PermissionsHandler.playerWarpReport,
                            "sef:teleport.player_warp.manage"))
                    .then(Commands.argument("reference", StringArgumentType.word())
                            .then(Commands.argument("reason", StringArgumentType.greedyString())
                                    .executes(context -> reportPlayerWarp(
                                            context.getSource(),
                                            StringArgumentType.getString(context, "reference"),
                                            StringArgumentType.getString(context, "reason"))))));
            node.then(Commands.literal("visits")
                    .requires(source -> TeleportCommandSupport.actionEnabled(
                            source,
                            "sef:teleport.player_warp.manage"))
                    .then(Commands.argument("reference", StringArgumentType.word())
                            .executes(context -> playerWarpVisits(
                                    context.getSource(),
                                    StringArgumentType.getString(context, "reference")))));
            node.then(Commands.literal("fromhome")
                    .requires(source -> TeleportCommandSupport.has(
                            source,
                            PermissionsHandler.setPlayerWarpCommand,
                            "sef:teleport.player_warp.create"))
                    .then(Commands.argument("home", StringArgumentType.word())
                            .then(Commands.argument("warp", StringArgumentType.word())
                                    .executes(context -> createFromHome(
                                            context.getSource(),
                                            StringArgumentType.getString(context, "home"),
                                            StringArgumentType.getString(context, "warp"))))));
            node.then(moderationNode());
        }
        node.then(Commands.argument("reference", StringArgumentType.word())
                .executes(context -> visitPlayerWarp(
                        context.getSource(),
                        StringArgumentType.getString(context, "reference"))));
        return node;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> playerWarpListNode(String literal) {
        return Commands.literal(literal)
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.playerWarpsCommand,
                        "sef:teleport.player_warp.list"))
                .executes(context -> listPlayerWarps(context.getSource(), null))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> listPlayerWarps(
                                context.getSource(),
                                EntityArgument.getPlayer(context, "player"))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> publicationNode() {
        return Commands.literal("publish")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.playerWarpPublish,
                        "sef:teleport.player_warp.manage"))
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(context -> publishOwnedWarp(
                                context.getSource(),
                                StringArgumentType.getString(context, "name"),
                                WarpRecord.Access.PUBLIC))
                        .then(Commands.literal("public")
                                .executes(context -> publishOwnedWarp(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "name"),
                                        WarpRecord.Access.PUBLIC)))
                        .then(Commands.literal("unlisted")
                                .executes(context -> publishOwnedWarp(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "name"),
                                        WarpRecord.Access.UNLISTED)))
                        .then(Commands.literal("shared")
                                .executes(context -> publishOwnedWarp(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "name"),
                                        WarpRecord.Access.SHARED)))
                        .then(Commands.literal("private")
                                .executes(context -> publishOwnedWarp(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "name"),
                                        WarpRecord.Access.PRIVATE))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> accessNode(String literal, boolean trusted) {
        return Commands.literal(literal)
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.playerWarpAccess,
                        "sef:teleport.player_warp.manage"))
                .then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> updateOwnedAccess(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "name"),
                                        EntityArgument.getPlayer(context, "player"),
                                        trusted,
                                        false))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> blockAccessNode(String literal, boolean blocked) {
        return Commands.literal(literal)
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.playerWarpAccess,
                        "sef:teleport.player_warp.manage"))
                .then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> updateOwnedAccess(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "name"),
                                        EntityArgument.getPlayer(context, "player"),
                                        blocked,
                                        true))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> transferNode() {
        return Commands.literal("transfer")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.playerWarpTransfer,
                        "sef:teleport.player_warp.manage"))
                .then(Commands.literal("accept")
                        .then(Commands.argument("warp_id", StringArgumentType.word())
                                .executes(context -> acceptTransfer(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "warp_id")))))
                .then(Commands.literal("list")
                        .executes(context -> listTransfers(context.getSource())))
                .then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> offerTransfer(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "name"),
                                        EntityArgument.getPlayer(context, "player")))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> favoriteNode(String literal, boolean favorite) {
        return Commands.literal(literal)
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.playerWarpFavorite,
                        "sef:teleport.player_warp.manage"))
                .then(Commands.argument("reference", StringArgumentType.word())
                        .executes(context -> favorite(
                                context.getSource(),
                                StringArgumentType.getString(context, "reference"),
                                favorite)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> moderationNode() {
        return Commands.literal("moderate")
                .requires(source -> TeleportCommandSupport.has(
                        source,
                        PermissionsHandler.playerWarpModerate,
                        "sef:teleport.player_warp.moderate"))
                .then(Commands.literal("reports")
                        .executes(context -> listReports(context.getSource()))
                        .then(Commands.literal("resolve")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(context -> setReportStatus(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "id"),
                                                TeleportRepository.ReportStatus.RESOLVED))))
                        .then(Commands.literal("dismiss")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(context -> setReportStatus(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "id"),
                                                TeleportRepository.ReportStatus.DISMISSED)))))
                .then(Commands.literal("restore")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(context -> restoreWarp(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "id"),
                                        false))))
                .then(Commands.argument("reference", StringArgumentType.word())
                        .then(Commands.literal("inspect")
                                .executes(context -> playerWarpInfo(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "reference"))))
                        .then(Commands.literal("suspend")
                                .executes(context -> moderateStatus(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "reference"),
                                        WarpRecord.Status.SUSPENDED)))
                        .then(Commands.literal("disable")
                                .executes(context -> moderateStatus(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "reference"),
                                        WarpRecord.Status.DISABLED)))
                        .then(Commands.literal("enable")
                                .executes(context -> moderateStatus(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "reference"),
                                        WarpRecord.Status.ACTIVE)))
                        .then(Commands.literal("relocate")
                                .executes(context -> moderateRelocate(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "reference"))))
                        .then(Commands.literal("delete")
                                .then(Commands.literal("confirm")
                                        .executes(context -> moderateDelete(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "reference"))))));
    }

    private static int visitPlayerWarp(CommandSourceStack source, String reference) {
        ServerPlayer player = TeleportCommandSupport.player(source);
        if (player == null) {
            return 0;
        }
        WarpRecord warp = resolvePlayerWarp(source, reference, false);
        if (warp == null) {
            return 0;
        }
        boolean moderator = TeleportCommandSupport.has(source, PermissionsHandler.playerWarpModerate);
        if (!warp.canVisit(player.getUUID(), moderator)) {
            TeleportCommandSupport.fail(source, "You cannot visit that player warp.");
            return 0;
        }
        long revision = warp.revision();
        int moved = TeleportCommandSupport.teleport(
                source,
                player,
                player,
                warp.location(),
                "sef:teleport.player_warp.use",
                "player_warp",
                PermissionsHandler.playerWarpCommand,
                KernelServices.teleportSettings().userPolicy(),
                () -> KernelServices.teleports().warpById(warp.id())
                        .filter(WarpRecord::active)
                        .map(current -> current.revision() == revision
                                && current.canVisit(player.getUUID(), moderator))
                        .orElse(false));
        if (moved > 0) {
            KernelServices.teleports().recordVisit(warp.id(), revision);
        }
        return moved;
    }

    private static int listPlayerWarps(CommandSourceStack source, ServerPlayer owner) {
        ServerPlayer viewer = TeleportCommandSupport.player(source);
        if (viewer == null) {
            return 0;
        }
        boolean moderator = TeleportCommandSupport.has(source, PermissionsHandler.playerWarpModerate);
        List<WarpRecord> warps = owner == null
                ? KernelServices.teleports().visiblePlayerWarps(viewer.getUUID(), moderator)
                : KernelServices.teleports().playerWarps(owner.getUUID(), false).stream()
                        .filter(warp -> warp.canVisit(viewer.getUUID(), moderator))
                        .toList();
        if (warps.isEmpty()) {
            TeleportCommandSupport.info(source, "No accessible player warps are available.");
            return 0;
        }
        warps.forEach(warp -> TeleportCommandSupport.info(
                source,
                warp.ownerNameSnapshot() + ":" + warp.displayName()
                        + ", " + warp.access().name().toLowerCase(Locale.ROOT)
                        + ", " + warp.status().name().toLowerCase(Locale.ROOT)));
        return warps.size();
    }

    private static int createPlayerWarp(CommandSourceStack source, String name, HomeRecord sourceHome) {
        ServerPlayer player = TeleportCommandSupport.player(source);
        if (player == null) {
            return 0;
        }
        TeleportRepository repository = KernelServices.teleports();
        long usage = repository.playerWarps(player.getUUID(), false).size();
        QuotaService.Context quotaContext = TeleportCommandSupport.quotaContext(
                player,
                "sef:player_warps",
                "sef:teleport.player_warp.create",
                usage);
        long limit = KernelServices.quotas().resolve(quotaContext).effectiveValue();
        ActionResult<QuotaService.Reservation> reservationResult =
                KernelServices.quotas().reserve(quotaContext, 1);
        if (!reservationResult.successful()) {
            TeleportCommandSupport.fail(source, "Your player warp limit is " + limit + ".");
            return 0;
        }
        try (QuotaService.Reservation reservation = reservationResult.value()) {
            ActionResult<WarpRecord> result;
            try {
                result = repository.createPlayerWarp(
                        player.getUUID(),
                        player.getGameProfile().getName(),
                        name,
                        sourceHome == null ? SavedLocation.from(player) : sourceHome.location(),
                        limit,
                        sourceHome == null ? null : sourceHome.id());
            } catch (IllegalArgumentException exception) {
                TeleportCommandSupport.fail(source, exception.getMessage());
                return 0;
            }
            if (!result.successful()) {
                TeleportCommandSupport.fail(source, result.detail());
                return 0;
            }
            reservation.commit();
            TeleportCommandSupport.success(
                    source,
                    "Created private draft player warp " + result.value().displayName() + ".");
            return 1;
        }
    }

    private static int createFromHome(CommandSourceStack source, String homeName, String warpName) {
        ServerPlayer player = TeleportCommandSupport.player(source);
        if (player == null) {
            return 0;
        }
        HomeRecord home;
        try {
            home = KernelServices.teleports().home(player.getUUID(), homeName).orElse(null);
        } catch (IllegalArgumentException exception) {
            TeleportCommandSupport.fail(source, exception.getMessage());
            return 0;
        }
        if (home == null) {
            TeleportCommandSupport.fail(source, "Home not found.");
            return 0;
        }
        return createPlayerWarp(source, warpName, home);
    }

    private static int deleteOwnedWarp(CommandSourceStack source, String name) {
        WarpRecord warp = ownedWarp(source, name);
        if (warp == null) {
            return 0;
        }
        ActionResult<WarpRecord> result = KernelServices.teleports().deleteWarp(warp.id());
        if (!result.successful()) {
            TeleportCommandSupport.fail(source, result.detail());
            return 0;
        }
        TeleportCommandSupport.success(
                source,
                "Deleted player warp " + warp.displayName() + ". Recovery id " + warp.id() + ".");
        return 1;
    }

    private static int renameOwnedWarp(CommandSourceStack source, String current, String replacement) {
        WarpRecord warp = ownedWarp(source, current);
        if (warp == null) {
            return 0;
        }
        ActionResult<WarpRecord> result;
        try {
            result = KernelServices.teleports().renameWarp(warp.id(), replacement);
        } catch (IllegalArgumentException exception) {
            TeleportCommandSupport.fail(source, exception.getMessage());
            return 0;
        }
        if (!result.successful()) {
            TeleportCommandSupport.fail(source, result.detail());
            return 0;
        }
        TeleportCommandSupport.success(source, "Renamed player warp to " + result.value().displayName() + ".");
        return 1;
    }

    private static int relocateOwnedWarp(CommandSourceStack source, String name) {
        ServerPlayer player = TeleportCommandSupport.player(source);
        WarpRecord warp = ownedWarp(source, name);
        if (player == null || warp == null) {
            return 0;
        }
        ActionResult<WarpRecord> result =
                KernelServices.teleports().relocateWarp(warp.id(), SavedLocation.from(player));
        if (!result.successful()) {
            TeleportCommandSupport.fail(source, result.detail());
            return 0;
        }
        TeleportCommandSupport.success(source, "Relocated player warp " + warp.displayName() + ".");
        return 1;
    }

    private static int publishOwnedWarp(CommandSourceStack source, String name, WarpRecord.Access access) {
        WarpRecord warp = ownedWarp(source, name);
        if (warp == null) {
            return 0;
        }
        ActionResult<WarpRecord> result = KernelServices.teleports().publishWarp(warp.id(), access);
        if (!result.successful()) {
            TeleportCommandSupport.fail(source, result.detail());
            return 0;
        }
        TeleportCommandSupport.success(
                source,
                "Set player warp access to " + access.name().toLowerCase(Locale.ROOT) + ".");
        return 1;
    }

    private static int updateOwnedAccess(
            CommandSourceStack source,
            String name,
            ServerPlayer target,
            boolean value,
            boolean block
    ) {
        WarpRecord warp = ownedWarp(source, name);
        if (warp == null) {
            return 0;
        }
        if (warp.ownerId().equals(target.getUUID())) {
            TeleportCommandSupport.fail(source, "A warp owner cannot be placed on its own access list.");
            return 0;
        }
        ActionResult<WarpRecord> result = block
                ? KernelServices.teleports().blockWarp(warp.id(), target.getUUID(), value)
                : KernelServices.teleports().trustWarp(warp.id(), target.getUUID(), value);
        if (!result.successful()) {
            TeleportCommandSupport.fail(source, result.detail());
            return 0;
        }
        TeleportCommandSupport.success(source, "Updated the player warp access list.");
        return 1;
    }

    private static int offerTransfer(CommandSourceStack source, String name, ServerPlayer target) {
        ServerPlayer owner = TeleportCommandSupport.player(source);
        WarpRecord warp = ownedWarp(source, name);
        if (owner == null || warp == null) {
            return 0;
        }
        ActionResult<TeleportRepository.TransferOffer> result = KernelServices.teleports().offerTransfer(
                warp.id(),
                owner.getUUID(),
                target.getUUID(),
                Instant.now().plus(KernelServices.teleportSettings().transferLifetime()));
        if (!result.successful()) {
            TeleportCommandSupport.fail(source, result.detail());
            return 0;
        }
        target.sendSystemMessage(com.enviouse.sef.TextFormatter.stringToFormattedText(
                "&e" + owner.getGameProfile().getName() + " offered player warp "
                        + warp.displayName() + ". &7Use /pwarp transfer accept " + warp.id() + "."));
        TeleportCommandSupport.success(source, "Created the two party transfer offer.");
        return 1;
    }

    private static int acceptTransfer(CommandSourceStack source, String warpId) {
        ServerPlayer recipient = TeleportCommandSupport.player(source);
        if (recipient == null) {
            return 0;
        }
        UUID id;
        try {
            id = UUID.fromString(warpId);
        } catch (IllegalArgumentException exception) {
            TeleportCommandSupport.fail(source, "Warp id is not a valid UUID.");
            return 0;
        }
        long usage = KernelServices.teleports().playerWarps(recipient.getUUID(), false).size();
        QuotaService.Context quotaContext = TeleportCommandSupport.quotaContext(
                recipient,
                "sef:player_warps",
                "sef:teleport.player_warp.manage",
                usage);
        long limit = KernelServices.quotas().resolve(quotaContext).effectiveValue();
        ActionResult<QuotaService.Reservation> reserved = KernelServices.quotas().reserve(quotaContext, 1);
        if (!reserved.successful()) {
            TeleportCommandSupport.fail(source, "Your player warp limit is " + limit + ".");
            return 0;
        }
        try (QuotaService.Reservation reservation = reserved.value()) {
            ActionResult<WarpRecord> result = KernelServices.teleports().acceptTransfer(
                    id,
                    recipient.getUUID(),
                    recipient.getGameProfile().getName(),
                    limit);
            if (!result.successful()) {
                TeleportCommandSupport.fail(source, result.detail());
                return 0;
            }
            reservation.commit();
            TeleportCommandSupport.success(source, "Accepted player warp " + result.value().displayName() + ".");
            return 1;
        }
    }

    private static int listTransfers(CommandSourceStack source) {
        ServerPlayer player = TeleportCommandSupport.player(source);
        if (player == null) {
            return 0;
        }
        List<TeleportRepository.TransferOffer> offers =
                KernelServices.teleports().transferOffersFor(player.getUUID());
        if (offers.isEmpty()) {
            TeleportCommandSupport.info(source, "You have no pending player warp transfer offers.");
            return 0;
        }
        offers.forEach(offer -> TeleportCommandSupport.info(
                source,
                "Warp id " + offer.warpId() + ", expires " + offer.expiresAt() + "."));
        return offers.size();
    }

    private static int favorite(CommandSourceStack source, String reference, boolean favorite) {
        ServerPlayer player = TeleportCommandSupport.player(source);
        WarpRecord warp = resolvePlayerWarp(source, reference, false);
        if (player == null || warp == null) {
            return 0;
        }
        if (!warp.canVisit(player.getUUID(), false)) {
            TeleportCommandSupport.fail(source, "You cannot favorite an inaccessible player warp.");
            return 0;
        }
        KernelServices.teleports().setFavorite(player.getUUID(), warp.id(), favorite);
        TeleportCommandSupport.success(
                source,
                (favorite ? "Favorited " : "Unfavorited ") + warp.ownerNameSnapshot() + ":" + warp.displayName() + ".");
        return 1;
    }

    private static int reportPlayerWarp(CommandSourceStack source, String reference, String reason) {
        ServerPlayer reporter = TeleportCommandSupport.player(source);
        WarpRecord warp = resolvePlayerWarp(source, reference, false);
        if (reporter == null || warp == null) {
            return 0;
        }
        ActionResult<TeleportRepository.WarpReport> result =
                KernelServices.teleports().reportWarp(warp.id(), reporter.getUUID(), reason);
        if (!result.successful()) {
            TeleportCommandSupport.fail(source, result.detail());
            return 0;
        }
        TeleportCommandSupport.success(source, "Submitted warp report " + result.value().id() + ".");
        return 1;
    }

    private static int playerWarpVisits(CommandSourceStack source, String reference) {
        WarpRecord warp = resolvePlayerWarp(source, reference, false);
        if (warp == null) {
            return 0;
        }
        TeleportCommandSupport.info(source, warp.ownerNameSnapshot() + ":" + warp.displayName()
                + " has " + warp.visits() + " successful visits.");
        return 1;
    }

    private static int playerWarpInfo(CommandSourceStack source, String reference) {
        WarpRecord warp = resolvePlayerWarp(
                source,
                reference,
                TeleportCommandSupport.has(source, PermissionsHandler.playerWarpModerate));
        if (warp == null) {
            return 0;
        }
        ServerPlayer viewer = TeleportCommandSupport.player(source);
        boolean sensitive = viewer != null
                && (Objects.equals(viewer.getUUID(), warp.ownerId())
                || TeleportCommandSupport.has(source, PermissionsHandler.playerWarpModerate));
        String line = warp.ownerNameSnapshot() + ":" + warp.displayName()
                + ", id " + warp.id()
                + ", access " + warp.access().name().toLowerCase(Locale.ROOT)
                + ", status " + warp.status().name().toLowerCase(Locale.ROOT)
                + ", visits " + warp.visits()
                + ", revision " + warp.revision();
        if (sensitive) {
            line += ", location " + warp.location().dimensionId()
                    + " " + Math.floor(warp.location().x())
                    + " " + Math.floor(warp.location().y())
                    + " " + Math.floor(warp.location().z());
        }
        TeleportCommandSupport.info(source, line + ".");
        return 1;
    }

    private static int moderateStatus(CommandSourceStack source, String reference, WarpRecord.Status status) {
        WarpRecord warp = resolvePlayerWarp(source, reference, true);
        if (warp == null) {
            return 0;
        }
        ActionResult<WarpRecord> result = KernelServices.teleports().setWarpStatus(warp.id(), status);
        if (!result.successful()) {
            TeleportCommandSupport.fail(source, result.detail());
            return 0;
        }
        TeleportCommandSupport.success(source, "Updated player warp moderation status.");
        return 1;
    }

    private static int moderateRelocate(CommandSourceStack source, String reference) {
        ServerPlayer moderator = TeleportCommandSupport.player(source);
        WarpRecord warp = resolvePlayerWarp(source, reference, true);
        if (moderator == null || warp == null) {
            return 0;
        }
        ActionResult<WarpRecord> result =
                KernelServices.teleports().relocateWarp(warp.id(), SavedLocation.from(moderator));
        if (!result.successful()) {
            TeleportCommandSupport.fail(source, result.detail());
            return 0;
        }
        TeleportCommandSupport.success(source, "Relocated the player warp.");
        return 1;
    }

    private static int moderateDelete(CommandSourceStack source, String reference) {
        WarpRecord warp = resolvePlayerWarp(source, reference, true);
        if (warp == null) {
            return 0;
        }
        ActionResult<WarpRecord> result = KernelServices.teleports().deleteWarp(warp.id());
        if (!result.successful()) {
            TeleportCommandSupport.fail(source, result.detail());
            return 0;
        }
        TeleportCommandSupport.success(source, "Deleted the player warp. Recovery id " + warp.id() + ".");
        return 1;
    }

    private static int restoreWarp(CommandSourceStack source, String rawId, boolean serverWarp) {
        UUID id;
        try {
            id = UUID.fromString(rawId);
        } catch (IllegalArgumentException exception) {
            TeleportCommandSupport.fail(source, "Warp recovery id is not a valid UUID.");
            return 0;
        }
        WarpRecord deleted = KernelServices.teleports().warpById(id).orElse(null);
        if (deleted == null
                || deleted.status() != WarpRecord.Status.DELETED
                || (serverWarp && deleted.scope() != WarpRecord.Scope.SERVER_PUBLIC)
                || (!serverWarp && deleted.scope() != WarpRecord.Scope.PLAYER)) {
            TeleportCommandSupport.fail(source, "Matching deleted warp not found.");
            return 0;
        }
        long limit = serverWarp
                ? 1000
                : TeleportCommandSupport.offlineQuota(
                        deleted.ownerId(),
                        "sef:player_warps",
                        "sef:teleport.player_warp.moderate",
                        KernelServices.teleports().playerWarps(deleted.ownerId(), false).size(),
                        deleted.location().dimensionId());
        ActionResult<WarpRecord> result = KernelServices.teleports().restoreWarp(id, limit);
        if (!result.successful()) {
            TeleportCommandSupport.fail(source, result.detail());
            return 0;
        }
        TeleportCommandSupport.success(source, "Restored warp " + result.value().displayName() + ".");
        return 1;
    }

    private static int listReports(CommandSourceStack source) {
        List<TeleportRepository.WarpReport> reports =
                KernelServices.teleports().reports(TeleportRepository.ReportStatus.OPEN);
        if (reports.isEmpty()) {
            TeleportCommandSupport.info(source, "There are no open player warp reports.");
            return 0;
        }
        reports.forEach(report -> TeleportCommandSupport.info(
                source,
                report.id() + ", warp " + report.warpId()
                        + ", revision " + report.warpRevision()
                        + ", reporter " + report.reporterId()
                        + ", " + report.reason()));
        return reports.size();
    }

    private static int setReportStatus(
            CommandSourceStack source,
            String rawId,
            TeleportRepository.ReportStatus status
    ) {
        UUID id;
        try {
            id = UUID.fromString(rawId);
        } catch (IllegalArgumentException exception) {
            TeleportCommandSupport.fail(source, "Report id is not a valid UUID.");
            return 0;
        }
        ActionResult<TeleportRepository.WarpReport> result =
                KernelServices.teleports().setReportStatus(id, status);
        if (!result.successful()) {
            TeleportCommandSupport.fail(source, result.detail());
            return 0;
        }
        TeleportCommandSupport.success(
                source,
                "Set report status to " + status.name().toLowerCase(Locale.ROOT) + ".");
        return 1;
    }

    private static WarpRecord ownedWarp(CommandSourceStack source, String name) {
        ServerPlayer owner = TeleportCommandSupport.player(source);
        if (owner == null) {
            return null;
        }
        try {
            WarpRecord warp = KernelServices.teleports().playerWarp(owner.getUUID(), name).orElse(null);
            if (warp == null) {
                TeleportCommandSupport.fail(source, "Owned player warp not found.");
            }
            return warp;
        } catch (IllegalArgumentException exception) {
            TeleportCommandSupport.fail(source, exception.getMessage());
            return null;
        }
    }

    private static WarpRecord resolvePlayerWarp(
            CommandSourceStack source,
            String reference,
            boolean moderator
    ) {
        ServerPlayer viewer = TeleportCommandSupport.player(source);
        if (viewer == null) {
            return null;
        }
        String raw = reference.trim();
        int separator = raw.indexOf(':');
        if (separator > 0 && separator < raw.length() - 1) {
            String ownerInput = raw.substring(0, separator);
            String name = raw.substring(separator + 1);
            ActionResult<IdentityService.Identity> identity =
                    KernelServices.identities().resolve(ownerInput, viewer);
            if (!identity.successful() || identity.value().playerId() == null) {
                TeleportCommandSupport.fail(source, identity.detail());
                return null;
            }
            try {
                WarpRecord warp = KernelServices.teleports()
                        .playerWarp(identity.value().playerId(), name)
                        .orElse(null);
                if (warp == null || (!moderator && !warp.canVisit(viewer.getUUID(), false))) {
                    TeleportCommandSupport.fail(source, "Accessible player warp not found.");
                    return null;
                }
                return warp;
            } catch (IllegalArgumentException exception) {
                TeleportCommandSupport.fail(source, exception.getMessage());
                return null;
            }
        }

        String normalized;
        try {
            normalized = WarpRecord.normalizeName(raw);
        } catch (IllegalArgumentException exception) {
            TeleportCommandSupport.fail(source, exception.getMessage());
            return null;
        }
        WarpRecord own = KernelServices.teleports().playerWarp(viewer.getUUID(), normalized).orElse(null);
        if (own != null) {
            return own;
        }
        Set<UUID> favorites = KernelServices.teleports().preference(viewer.getUUID()).favoriteWarpIds();
        List<WarpRecord> visible = moderator
                ? KernelServices.teleports().allWarps(false).stream()
                        .filter(warp -> warp.scope() == WarpRecord.Scope.PLAYER)
                        .toList()
                : KernelServices.teleports().visiblePlayerWarps(viewer.getUUID(), false);
        List<WarpRecord> favoriteMatches = visible.stream()
                .filter(warp -> favorites.contains(warp.id()))
                .filter(warp -> warp.normalizedName().equals(normalized))
                .toList();
        if (favoriteMatches.size() == 1) {
            return favoriteMatches.getFirst();
        }
        if (favoriteMatches.size() > 1) {
            TeleportCommandSupport.fail(source, "That favorite name is ambiguous. Use owner:name.");
            return null;
        }
        List<WarpRecord> matches = visible.stream()
                .filter(warp -> warp.normalizedName().equals(normalized))
                .toList();
        if (matches.isEmpty()) {
            TeleportCommandSupport.fail(source, "Accessible player warp not found.");
            return null;
        }
        if (matches.size() > 1) {
            TeleportCommandSupport.fail(source, "That player warp name is ambiguous. Use owner:name.");
            return null;
        }
        return matches.getFirst();
    }

    private static String commandRoot(String canonical) {
        return KernelServices.teleportSettings().ownershipMode() == TeleportSettings.OwnershipMode.COEXIST
                ? "sef" + canonical
                : canonical;
    }
}
