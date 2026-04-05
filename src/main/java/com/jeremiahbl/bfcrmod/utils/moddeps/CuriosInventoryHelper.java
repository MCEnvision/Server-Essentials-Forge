package com.jeremiahbl.bfcrmod.utils.moddeps;

import com.jeremiahbl.bfcrmod.BetterForgeChat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility to read Curios inventory items from a player.
 * Isolated from the main codebase so it only touches Curios classes
 * when the mod is confirmed loaded.
 */
public class CuriosInventoryHelper {

    /**
     * Returns a list of (slotTypeName, ItemStack) pairs for all equipped curios.
     * Returns an empty list if Curios is not installed or any error occurs.
     */
    public static List<Map.Entry<String, ItemStack>> getEquippedCurios(ServerPlayer player) {
        if (!ModList.get().isLoaded("curios")) {
            return new ArrayList<>();
        }
        try {
            return fetchCurios(player);
        } catch (Throwable e) {
            BetterForgeChat.LOGGER.trace("[BFCRR] Error reading Curios inventory", e);
            return new ArrayList<>();
        }
    }

    /**
     * Checks if Curios mod is available.
     */
    public static boolean isCuriosLoaded() {
        return ModList.get().isLoaded("curios");
    }

    /**
     * Isolated method that actually accesses Curios API classes.
     */
    private static List<Map.Entry<String, ItemStack>> fetchCurios(ServerPlayer player) {
        List<Map.Entry<String, ItemStack>> result = new ArrayList<>();

        top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            Map<String, top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler> curios = handler.getCurios();
            for (Map.Entry<String, top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler> entry : curios.entrySet()) {
                String slotType = entry.getKey();
                net.minecraftforge.items.IItemHandlerModifiable stacks = entry.getValue().getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        result.add(new AbstractMap.SimpleEntry<>(slotType, stack.copy()));
                    }
                }
            }
        });

        return result;
    }
}

