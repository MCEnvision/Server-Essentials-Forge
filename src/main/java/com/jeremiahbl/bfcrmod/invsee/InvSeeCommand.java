package com.jeremiahbl.bfcrmod.invsee;

import com.jeremiahbl.bfcrmod.BetterForgeChat;
import com.jeremiahbl.bfcrmod.TextFormatter;
import com.jeremiahbl.bfcrmod.config.ConfigHandler;
import com.jeremiahbl.bfcrmod.config.PermissionsHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Custom /invsee command that shows a player's inventory, armor, offhand,
 * and Curios slots in a double-chest GUI with pagination support.
 *
 * If FTB Essentials is detected and invSeeDisableFtbInvsee is true,
 * this command overrides FTB's /invsee by removing their command node first.
 */
public class InvSeeCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (!ConfigHandler.config.enableInvSee.get()) return;

        // If FTB Essentials is loaded and override is enabled, remove FTB's /invsee node
        if (ConfigHandler.config.invSeeDisableFtbInvsee.get() && ModList.get().isLoaded("ftbessentials")) {
            removeCommandNode(dispatcher, "invsee");
            BetterForgeChat.LOGGER.info("[BFCRR] Overriding FTB Essentials /invsee with BFCRR version (Curios support)");
        }

        dispatcher.register(Commands.literal("invsee")
            .requires(src -> {
                try {
                    return PermissionsHandler.playerHasPermission(
                        src.getPlayerOrException().getUUID(), PermissionsHandler.invSeeCommand);
                } catch (Exception e) { return src.hasPermission(2); }
            })
            .then(Commands.argument("player", EntityArgument.player())
                .executes(ctx -> {
                    ServerPlayer viewer = ctx.getSource().getPlayerOrException();
                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                    return openInvSee(viewer, target);
                })));
    }

    /**
     * Removes a command node from the dispatcher's root using reflection.
     * This is needed to fully replace FTB's /invsee registration.
     */
    @SuppressWarnings("unchecked")
    private static void removeCommandNode(CommandDispatcher<CommandSourceStack> dispatcher, String name) {
        try {
            RootCommandNode<CommandSourceStack> root = dispatcher.getRoot();
            // Remove from 'children' map
            Field childrenField = CommandNode.class.getDeclaredField("children");
            childrenField.setAccessible(true);
            Map<String, CommandNode<CommandSourceStack>> children =
                (Map<String, CommandNode<CommandSourceStack>>) childrenField.get(root);
            children.remove(name);

            // Remove from 'literals' map
            Field literalsField = CommandNode.class.getDeclaredField("literals");
            literalsField.setAccessible(true);
            Map<String, ?> literals = (Map<String, ?>) literalsField.get(root);
            literals.remove(name);
        } catch (Exception e) {
            BetterForgeChat.LOGGER.warn("[BFCRR] Could not remove existing /invsee command node: {}", e.getMessage());
        }
    }

    private static int openInvSee(ServerPlayer viewer, ServerPlayer target) {
        // Build the layout
        List<ItemStack> allItems = InvSeeLayout.buildAllItems(target);
        List<ItemStack[]> pages = InvSeeLayout.paginate(allItems);

        String titleStr = ConfigHandler.config.invSeeTitle.get()
            .replace("$player", target.getGameProfile().getName());
        Component title = TextFormatter.stringToFormattedText(titleStr);

        viewer.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player p) {
                return new InvSeeContainer(id, playerInventory, pages);
            }
        });

        return 1;
    }
}


