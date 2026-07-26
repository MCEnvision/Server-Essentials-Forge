package com.enviouse.sef.workstations;

import java.util.Comparator;
import java.util.List;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;

final class SuperEnchantingMenu extends ChestMenu {
    private static final int GUI_SIZE = 54;
    private static final int PAGE_SIZE = 45;
    private static final int PREVIOUS_SLOT = 45;
    private static final int HELP_SLOT = 47;
    private static final int TARGET_SLOT = 49;
    private static final int NEXT_SLOT = 53;

    private final SimpleContainer display;
    private final ServerPlayer player;
    private final int targetSlot;
    private List<Holder.Reference<Enchantment>> enchantments = List.of();
    private int page;

    SuperEnchantingMenu(int containerId, Inventory inventory, ServerPlayer player) {
        this(containerId, inventory, player, new SimpleContainer(GUI_SIZE));
    }

    private SuperEnchantingMenu(int containerId, Inventory inventory, ServerPlayer player, SimpleContainer display) {
        super(MenuType.GENERIC_9x6, containerId, inventory, display, 6);
        this.display = display;
        this.player = player;
        this.targetSlot = inventory.selected;
        refresh();
    }

    static boolean canOpen(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        return !held.isEmpty()
                && EnchantmentHelper.canStoreEnchantments(held)
                && (!held.is(Items.BOOK) || held.getCount() == 1);
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player clicker) {
        if (slotId < 0 || slotId >= GUI_SIZE || clicker != player) {
            return;
        }
        if (slotId == PREVIOUS_SLOT && page > 0) {
            page--;
            refresh();
            return;
        }
        if (slotId == NEXT_SLOT && page + 1 < pageCount()) {
            page++;
            refresh();
            return;
        }
        if (slotId >= PAGE_SIZE) {
            return;
        }

        int enchantmentIndex = page * PAGE_SIZE + slotId;
        if (enchantmentIndex >= enchantments.size()) {
            return;
        }
        if (clickType != ClickType.PICKUP && clickType != ClickType.QUICK_MOVE) {
            return;
        }

        Holder<Enchantment> enchantment = enchantments.get(enchantmentIndex);
        if (clickType == ClickType.QUICK_MOVE) {
            setLevel(enchantment, ConfigHandler.config.superEnchantingMaxLevel.get());
        } else if (button == 1) {
            changeLevel(enchantment, -1);
        } else if (button == 0) {
            changeLevel(enchantment, 1);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotId) {
        return ItemStack.EMPTY;
    }

    private void changeLevel(Holder<Enchantment> enchantment, int amount) {
        ItemStack target = targetItem();
        int current = enchantmentLevel(target, enchantment);
        setLevel(enchantment, current + amount);
    }

    private void setLevel(Holder<Enchantment> enchantment, int requestedLevel) {
        ItemStack target = targetItem();
        if (target.isEmpty() || !EnchantmentHelper.canStoreEnchantments(target)) {
            player.closeContainer();
            player.sendSystemMessage(TextFormatter.stringToFormattedText("&cThe selected item is no longer available."));
            return;
        }

        int currentLevel = enchantmentLevel(target, enchantment);
        int newLevel = Math.clamp(requestedLevel, 0, ConfigHandler.config.superEnchantingMaxLevel.get());
        if (newLevel > currentLevel && !canApply(target, enchantment)) {
            player.sendSystemMessage(TextFormatter.stringToFormattedText("&cThat enchantment is not compatible with this item."));
            return;
        }
        if (newLevel == currentLevel) {
            return;
        }

        if (target.is(Items.BOOK)) {
            ItemStack enchantedBook = target.getItem().applyEnchantments(
                    target, List.of(new EnchantmentInstance(enchantment, newLevel)));
            player.getInventory().setItem(targetSlot, enchantedBook);
        } else {
            EnchantmentHelper.updateEnchantments(target, mutable -> mutable.set(enchantment, newLevel));
        }
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        player.displayClientMessage(
                Enchantment.getFullname(enchantment, Math.max(1, newLevel)).copy()
                        .append(newLevel == 0 ? Component.literal(" removed") : Component.literal(" applied"))
                        .withStyle(newLevel == 0 ? ChatFormatting.RED : ChatFormatting.GREEN),
                true);
        refresh();
    }

    private boolean canApply(ItemStack target, Holder<Enchantment> candidate) {
        if (ConfigHandler.config.superEnchantingAllowUnsafe.get()) {
            return true;
        }
        if (!target.supportsEnchantment(candidate)) {
            return false;
        }

        ItemEnchantments current = EnchantmentHelper.getEnchantmentsForCrafting(target);
        for (Holder<Enchantment> existing : current.keySet()) {
            if (!existing.equals(candidate) && !Enchantment.areCompatible(existing, candidate)) {
                return false;
            }
        }
        return true;
    }

    private void refresh() {
        ItemStack target = targetItem();
        if (target.isEmpty()) {
            return;
        }

        Registry<Enchantment> registry = player.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        enchantments = registry.holders()
                .filter(holder -> shouldShow(target, holder))
                .sorted(Comparator.comparing(holder -> holder.key().location().toString()))
                .toList();
        page = Math.min(page, Math.max(0, pageCount() - 1));

        display.clearContent();
        int first = page * PAGE_SIZE;
        int last = Math.min(first + PAGE_SIZE, enchantments.size());
        for (int index = first; index < last; index++) {
            display.setItem(index - first, enchantmentButton(target, enchantments.get(index)));
        }
        if (page > 0) {
            display.setItem(PREVIOUS_SLOT, namedItem(Items.ARROW, "previous page"));
        }
        if (page + 1 < pageCount()) {
            display.setItem(NEXT_SLOT, namedItem(Items.ARROW, "next page"));
        }
        display.setItem(HELP_SLOT, helpItem());
        display.setItem(TARGET_SLOT, target.copy());
        broadcastChanges();
    }

    private boolean shouldShow(ItemStack target, Holder<Enchantment> enchantment) {
        if (enchantmentLevel(target, enchantment) > 0) {
            return true;
        }
        return ConfigHandler.config.superEnchantingAllowUnsafe.get() || canApply(target, enchantment);
    }

    private ItemStack enchantmentButton(ItemStack target, Holder<Enchantment> enchantment) {
        int currentLevel = enchantmentLevel(target, enchantment);
        ItemStack button = new ItemStack(Items.ENCHANTED_BOOK);
        button.set(DataComponents.CUSTOM_NAME, Enchantment.getFullname(enchantment, Math.max(1, currentLevel)));
        button.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("current level, " + currentLevel).withStyle(ChatFormatting.GRAY),
                Component.literal("left click adds one level").withStyle(ChatFormatting.GREEN),
                Component.literal("right click removes one level").withStyle(ChatFormatting.RED),
                Component.literal("shift click sets the configured maximum").withStyle(ChatFormatting.YELLOW))));
        return button;
    }

    private ItemStack helpItem() {
        ItemStack help = namedItem(Items.PAPER, "super enchanting table help");
        help.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("page " + (page + 1) + " of " + pageCount()).withStyle(ChatFormatting.GRAY),
                Component.literal("maximum level, " + ConfigHandler.config.superEnchantingMaxLevel.get()).withStyle(ChatFormatting.GRAY),
                Component.literal("changes apply to the held item immediately").withStyle(ChatFormatting.YELLOW))));
        return help;
    }

    private static ItemStack namedItem(net.minecraft.world.item.Item item, String name) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return stack;
    }

    private ItemStack targetItem() {
        return player.getInventory().getItem(targetSlot);
    }

    private static int enchantmentLevel(ItemStack target, Holder<Enchantment> enchantment) {
        return EnchantmentHelper.getEnchantmentsForCrafting(target).getLevel(enchantment);
    }

    private int pageCount() {
        return Math.max(1, (enchantments.size() + PAGE_SIZE - 1) / PAGE_SIZE);
    }
}
