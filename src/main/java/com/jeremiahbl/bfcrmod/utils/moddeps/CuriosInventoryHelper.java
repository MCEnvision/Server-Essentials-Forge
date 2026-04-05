package com.jeremiahbl.bfcrmod.utils.moddeps;

import com.jeremiahbl.bfcrmod.BetterForgeChat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utility to read and write Curios inventory items from a player.
 * Isolated from the main codebase so it only touches Curios classes
 * when the mod is confirmed loaded.
 */
public class CuriosInventoryHelper {

    /**
     * Represents a group of curios slots of the same type.
     * The handler is an IItemHandlerModifiable that can get/set items in those slots.
     */
    public record CuriosSlotGroup(String slotType, IItemHandlerModifiable handler, int slotCount) {}

    /**
     * Returns all curios slot groups for a player.
     * Each group has a slot type name, an IItemHandler for direct get/set, and a slot count.
     * Returns empty list if Curios is not installed or any error occurs.
     */
    public static List<CuriosSlotGroup> getCuriosSlotGroups(ServerPlayer player) {
        if (!ModList.get().isLoaded("curios")) {
            return Collections.emptyList();
        }
        try {
            return fetchCuriosSlotGroups(player);
        } catch (Throwable e) {
            BetterForgeChat.LOGGER.trace("[BFCRR] Error reading Curios slot groups", e);
            return Collections.emptyList();
        }
    }

    /**
     * Isolated method that accesses Curios API classes for slot groups.
     */
    private static List<CuriosSlotGroup> fetchCuriosSlotGroups(ServerPlayer player) {
        List<CuriosSlotGroup> result = new ArrayList<>();

        top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            Map<String, top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler> curios = handler.getCurios();
            for (Map.Entry<String, top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler> entry : curios.entrySet()) {
                String slotType = entry.getKey();
                IItemHandlerModifiable stacks = entry.getValue().getStacks();
                result.add(new CuriosSlotGroup(slotType, stacks, stacks.getSlots()));
            }
        });

        return result;
    }

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
                IItemHandlerModifiable stacks = entry.getValue().getStacks();
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
