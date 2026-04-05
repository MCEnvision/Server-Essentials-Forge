package com.jeremiahbl.bfcrmod.invsee;

import com.jeremiahbl.bfcrmod.TextFormatter;
import com.jeremiahbl.bfcrmod.config.ConfigHandler;
import com.jeremiahbl.bfcrmod.utils.moddeps.CuriosInventoryHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds the list of display items for the InvSee GUI, organized into pages.
 * Each page is 54 slots (double chest). Sections are separated by named glass panes.
 *
 * Layout per page:
 *  - Armor section (4 items + labeled separator)
 *  - Offhand section (1 item + labeled separator)
 *  - Main Inventory (36 items + labeled separator)
 *  - Curios section (variable, labeled separator)
 *  - Glass-pane padding for unused slots
 *  - Navigation arrows in slots 45 (prev) and 53 (next) if multi-page
 */
public class InvSeeLayout {
    public static final int PAGE_SIZE = 54; // double chest
    public static final int NAV_PREV_SLOT = 45;
    public static final int NAV_NEXT_SLOT = 53;

    /**
     * Builds all items in order, with glass-pane separators.
     * Returns a flat list that will be paginated.
     */
    public static List<ItemStack> buildAllItems(ServerPlayer target) {
        List<ItemStack> items = new ArrayList<>();

        // --- Armor Section ---
        items.add(makeSeparator(ConfigHandler.config.invSeeArmorLabel.get()));
        // Helmet, Chestplate, Leggings, Boots (slots 39, 38, 37, 36 in player inventory)
        for (int i = 39; i >= 36; i--) {
            items.add(target.getInventory().getItem(i).copy());
        }

        // --- Offhand Section ---
        items.add(makeSeparator(ConfigHandler.config.invSeeOffhandLabel.get()));
        items.add(target.getInventory().getItem(40).copy()); // offhand

        // --- Main Inventory Section ---
        items.add(makeSeparator(ConfigHandler.config.invSeeMainInvLabel.get()));
        // Hotbar (0-8), then main inv (9-35)
        for (int i = 0; i < 36; i++) {
            items.add(target.getInventory().getItem(i).copy());
        }

        // --- Curios Section (if available) ---
        List<Map.Entry<String, ItemStack>> curios = CuriosInventoryHelper.getEquippedCurios(target);
        if (!curios.isEmpty()) {
            items.add(makeSeparator(ConfigHandler.config.invSeeCuriosLabel.get()));
            for (Map.Entry<String, ItemStack> entry : curios) {
                ItemStack curiosItem = entry.getValue().copy();
                // Add the curios slot type as lore info
                String slotType = entry.getKey();
                curiosItem.getOrCreateTag();
                // We'll add slot-type info via the display name approach in the container
                items.add(curiosItem);
            }
        }

        return items;
    }

    /**
     * Paginates the flat item list into pages of PAGE_SIZE.
     * Reserves slot 45 for prev-page arrow and slot 53 for next-page arrow when multiple pages exist.
     */
    public static List<ItemStack[]> paginate(List<ItemStack> allItems) {
        List<ItemStack[]> pages = new ArrayList<>();

        if (allItems.size() <= PAGE_SIZE) {
            // Single page, no navigation needed
            ItemStack[] page = new ItemStack[PAGE_SIZE];
            for (int i = 0; i < PAGE_SIZE; i++) {
                if (i < allItems.size()) {
                    page[i] = allItems.get(i);
                } else {
                    page[i] = ItemStack.EMPTY;
                }
            }
            pages.add(page);
            return pages;
        }

        // Multi-page: each page uses 52 content slots (54 - 2 nav slots)
        // Nav slots: 45 = prev, 53 = next
        int contentSlotsPerPage = PAGE_SIZE - 2;
        int totalPages = (int) Math.ceil((double) allItems.size() / contentSlotsPerPage);

        for (int p = 0; p < totalPages; p++) {
            ItemStack[] page = new ItemStack[PAGE_SIZE];

            // Fill content slots (0-44, 46-52)
            int contentIndex = 0;
            for (int slot = 0; slot < PAGE_SIZE; slot++) {
                if (slot == NAV_PREV_SLOT || slot == NAV_NEXT_SLOT) {
                    continue;
                }
                int itemIndex = p * contentSlotsPerPage + contentIndex;
                if (itemIndex < allItems.size()) {
                    page[slot] = allItems.get(itemIndex);
                } else {
                    page[slot] = ItemStack.EMPTY;
                }
                contentIndex++;
            }

            // Navigation arrows
            if (p > 0) {
                page[NAV_PREV_SLOT] = makeNavArrow(ConfigHandler.config.invSeePrevPageLabel.get());
            } else {
                page[NAV_PREV_SLOT] = makeFiller();
            }

            if (p < totalPages - 1) {
                page[NAV_NEXT_SLOT] = makeNavArrow(ConfigHandler.config.invSeeNextPageLabel.get());
            } else {
                page[NAV_NEXT_SLOT] = makeFiller();
            }

            pages.add(page);
        }

        return pages;
    }

    /**
     * Creates a glass pane separator with a colored name.
     */
    public static ItemStack makeSeparator(String name) {
        ItemStack pane = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
        pane.setHoverName(TextFormatter.stringToFormattedText(name));
        return pane;
    }

    /**
     * Creates a navigation arrow item.
     */
    public static ItemStack makeNavArrow(String name) {
        ItemStack arrow = new ItemStack(Items.ARROW);
        arrow.setHoverName(TextFormatter.stringToFormattedText(name));
        return arrow;
    }

    /**
     * Creates a filler glass pane for empty navigation slots.
     */
    public static ItemStack makeFiller() {
        ItemStack pane = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
        pane.setHoverName(net.minecraft.network.chat.Component.literal(" "));
        return pane;
    }
}

