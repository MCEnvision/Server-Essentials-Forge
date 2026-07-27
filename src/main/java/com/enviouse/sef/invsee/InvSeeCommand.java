package com.enviouse.sef.invsee;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.identity.IdentityArguments;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.vanish.VanishUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
/**
 * Custom /invsee command that shows a player's inventory, armor, offhand,
 * and Curios slots in a double-chest GUI with glass pane separators.
 * All slots are editable - changes sync directly to the target player's inventory.
 */
public class InvSeeCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (!ConfigHandler.config.enableInvSee.get()) return;
        if (dispatcher.getRoot().getChild("invsee") != null
                && !ConfigHandler.config.invSeeDisableFtbInvsee.get()) {
            return;
        }

        dispatcher.register(Commands.literal("invsee")
            .requires(InvSeeCommand::canView)
            .then(IdentityArguments.known("player")
                .executes(ctx -> {
                    ServerPlayer viewer;
                    try {
                        viewer = ctx.getSource().getPlayerOrException();
                    } catch (Exception e) {
                        ctx.getSource().sendFailure(TextFormatter.stringToFormattedText(
                            "&c/invsee can only be used by players — it opens a GUI."));
                        return 0;
                    }
                    var identity = KernelServices.identities().resolve(
                            StringArgumentType.getString(ctx, "player"),
                            viewer);
                    if (!identity.successful() || identity.value().playerId() == null) {
                        ctx.getSource().sendFailure(TextFormatter.stringToFormattedText(
                                "&cThat player is unavailable."));
                        return 0;
                    }
                    ServerPlayer target = ctx.getSource().getServer()
                            .getPlayerList()
                            .getPlayer(identity.value().playerId());
                    return target == null
                            ? OfflineInvSeeService.open(viewer, identity.value())
                            : openInvSee(viewer, target, 0);
                })));
    }

    /**
     * Opens the InvSee GUI for the viewer showing the target's inventory.
     * @param viewer The admin viewing the inventory
     * @param target The player whose inventory is being viewed/edited
     * @param page 0 = main inventory, 1+ = curios pages
     */
    public static int openInvSee(ServerPlayer viewer, ServerPlayer target, int page) {
        if (!canAccess(viewer, target)) {
            viewer.sendSystemMessage(TextFormatter.stringToFormattedText(
                    "&cThat inventory is no longer available."));
            viewer.closeContainer();
            return 0;
        }
        boolean mayViewCurios = PermissionService.has(viewer, PermissionsHandler.invSeeCurios);
        if (page > 0 && !mayViewCurios) {
            viewer.sendSystemMessage(TextFormatter.stringToFormattedText(
                    "&cYou do not have permission to view Curios inventories."));
            return 0;
        }
        boolean mayModify = PermissionService.has(viewer, PermissionsHandler.invSeeModify);
        String titleStr = ConfigHandler.config.invSeeTitle.get()
            .replace("$player", target.getGameProfile().getName());
        if (page > 0) {
            titleStr += " &7(Curios)";
        }
        Component title = TextFormatter.stringToFormattedText(titleStr);

        viewer.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player p) {
                return new InvSeeContainer(id, playerInventory, target, page, mayModify, mayViewCurios);
            }
        });

        return 1;
    }

    static boolean canView(CommandSourceStack source) {
        return PermissionService.has(source, PermissionsHandler.invSeeView)
                || PermissionService.has(source, PermissionsHandler.invSeeCommand);
    }

    static boolean canAccess(ServerPlayer viewer, ServerPlayer target) {
        if (!ConfigHandler.config.enableInvSee.get()
                || viewer == null
                || target == null
                || !viewer.isAlive()
                || viewer.hasDisconnected()
                || !target.isAlive()
                || target.hasDisconnected()
                || !canView(viewer.createCommandSourceStack())
                || VanishUtil.isVanished(target, viewer)) {
            return false;
        }
        return PlayerTargetPolicy.decide(
                viewer.createCommandSourceStack(),
                target,
                PermissionsHandler.phasePermission("inventory.hierarchy.bypass"),
                PermissionsHandler.phasePermission("exempt.inventory"),
                PermissionsHandler.phasePermission("inventory.bypass.exempt"),
                false,
                true).allowed();
    }
}
