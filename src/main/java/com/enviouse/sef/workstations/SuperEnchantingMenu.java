package com.enviouse.sef.workstations;

import java.util.Comparator;
import java.util.List;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.audit.SecurityAuditService;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.CommandAuditScope;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.permissions.PermissionService;

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

import java.util.Map;
import java.util.UUID;

final class SuperEnchantingMenu extends ChestMenu {
    private static final int GUI_SIZE = 54;
    private static final int PAGE_SIZE = 45;
    private static final int PREVIOUS_SLOT = 45;
    private static final int DECREASE_LARGE_SLOT = 46;
    private static final int DECREASE_SLOT = 47;
    private static final int STATUS_SLOT = 48;
    private static final int TARGET_SLOT = 49;
    private static final int INCREASE_SLOT = 50;
    private static final int INCREASE_LARGE_SLOT = 51;
    private static final int CONFIRM_SLOT = 52;
    private static final int NEXT_SLOT = 53;

    private final SimpleContainer display;
    private final ServerPlayer player;
    private final int targetSlot;
    private final long configurationRevision;
    private ItemStack expectedTarget;
    private List<Holder.Reference<Enchantment>> enchantments = List.of();
    private int page;
    private Holder.Reference<Enchantment> selected;
    private int requestedLevel;

    SuperEnchantingMenu(int containerId, Inventory inventory, ServerPlayer player) {
        this(containerId, inventory, player, new SimpleContainer(GUI_SIZE));
    }

    private SuperEnchantingMenu(int containerId, Inventory inventory, ServerPlayer player, SimpleContainer display) {
        super(MenuType.GENERIC_9x6, containerId, inventory, display, 6);
        this.display = display;
        this.player = player;
        this.targetSlot = inventory.selected;
        this.configurationRevision = KernelServices.configurationRevision();
        this.expectedTarget = inventory.getItem(targetSlot).copy();
        refresh();
    }

    static boolean canOpen(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        return ConfigHandler.config.enableSuperEnchantingTableCommand.get()
                && configuredLevelsValid()
                && !held.isEmpty()
                && PermissionService.has(player, PermissionsHandler.superEnchantingTableCommand);
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player clicker) {
        if (!authorizationValid()) {
            player.closeContainer();
            player.sendSystemMessage(TextFormatter.stringToFormattedText(
                    "&cThe selected item or your permission changed."));
            return;
        }
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
            handleControl(slotId);
            return;
        }

        int enchantmentIndex = page * PAGE_SIZE + slotId;
        if (enchantmentIndex >= enchantments.size()) {
            return;
        }
        if (clickType != ClickType.PICKUP && clickType != ClickType.QUICK_MOVE) {
            return;
        }

        Holder.Reference<Enchantment> enchantment = enchantments.get(enchantmentIndex);
        if (clickType == ClickType.QUICK_MOVE) {
            select(enchantment, configuredMaximum());
        } else if (button == 1) {
            select(enchantment, 0);
        } else if (button == 0) {
            int current = enchantmentLevel(targetItem(), enchantment);
            select(enchantment, current == 0 ? configuredMinimum() : current);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotId) {
        return ItemStack.EMPTY;
    }

    private void handleControl(int slotId) {
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
        if (selected == null) {
            return;
        }
        switch (slotId) {
            case DECREASE_LARGE_SLOT -> adjustLevel(-10);
            case DECREASE_SLOT -> adjustLevel(-1);
            case INCREASE_SLOT -> adjustLevel(1);
            case INCREASE_LARGE_SLOT -> adjustLevel(10);
            case CONFIRM_SLOT -> mutateSelected();
            default -> {
            }
        }
    }

    private void select(Holder.Reference<Enchantment> enchantment, int level) {
        selected = enchantment;
        requestedLevel = normalizeLevel(level, configuredMinimum(), configuredMaximum());
        refresh();
    }

    private void adjustLevel(int amount) {
        long candidate = (long) requestedLevel + amount;
        if (candidate <= 0) {
            requestedLevel = 0;
        } else {
            requestedLevel = normalizeLevel(
                    (int) Math.min(candidate, Integer.MAX_VALUE),
                    configuredMinimum(),
                    configuredMaximum());
        }
        refresh();
    }

    private void mutateSelected() {
        Holder.Reference<Enchantment> enchantment = resolveSelected();
        if (enchantment == null) {
            player.closeContainer();
            player.sendSystemMessage(TextFormatter.stringToFormattedText(
                    "&cThe selected enchantment is no longer available."));
            return;
        }
        ItemStack target = targetItem();
        if (target.isEmpty()) {
            player.closeContainer();
            player.sendSystemMessage(TextFormatter.stringToFormattedText("&cThe selected item is no longer available."));
            return;
        }

        int currentLevel = enchantmentLevel(target, enchantment);
        int newLevel = normalizeLevel(
                requestedLevel,
                configuredMinimum(),
                configuredMaximum());
        if (newLevel > enchantment.value().getMaxLevel()
                && (!AdministrativeEnchantCommands.unsafeLevelsEnabled()
                || !unsafePermission("commands.enchant.unsafe_level"))) {
            player.sendSystemMessage(TextFormatter.stringToFormattedText(
                    "&cMissing permission: sef.commands.enchant.unsafe_level."));
            return;
        }
        if (newLevel > currentLevel && !canApply(target, enchantment)) {
            player.sendSystemMessage(TextFormatter.stringToFormattedText(
                    "&cThe required arbitrary item or incompatibility permission is missing."));
            return;
        }
        if (newLevel == currentLevel) {
            return;
        }
        if (newLevel == 0 && !unsafePermission("commands.enchant.remove")) {
            player.sendSystemMessage(TextFormatter.stringToFormattedText(
                    "&cMissing permission: sef.commands.enchant.remove."));
            return;
        }

        String enchantmentId = enchantment.key().location().toString();
        KernelCommandExecutor.execute(
                player.createCommandSourceStack(),
                "sef:workstation.super_enchant.mutate",
                Map.of(
                        "enchantment", enchantmentId,
                        "level", Integer.toString(newLevel),
                        "amount", "1"),
                List.of(player.getUUID()),
                false,
                () -> applyMutation(enchantment, currentLevel, newLevel));
    }

    private int applyMutation(
            Holder.Reference<Enchantment> enchantment,
            int expectedLevel,
            int newLevel
    ) {
        if (!authorizationValid()) {
            player.closeContainer();
            return 0;
        }
        Holder.Reference<Enchantment> currentEnchantment = resolveSelected();
        ItemStack current = targetItem();
        if (currentEnchantment == null
                || !currentEnchantment.key().equals(enchantment.key())
                || enchantmentLevel(current, currentEnchantment) != expectedLevel
                || newLevel < 0
                || newLevel > configuredMaximum()) {
            player.closeContainer();
            player.sendSystemMessage(TextFormatter.stringToFormattedText(
                    "&cThe selected item, enchantment, or level changed."));
            return 0;
        }
        if (newLevel > enchantment.value().getMaxLevel()
                && (!AdministrativeEnchantCommands.unsafeLevelsEnabled()
                || !unsafePermission("commands.enchant.unsafe_level"))) {
            return 0;
        }
        if (newLevel > expectedLevel && !canApply(current, enchantment)) {
            return 0;
        }
        if (newLevel == 0 && !unsafePermission("commands.enchant.remove")) {
            return 0;
        }

        ItemStack rollback = current.copy();
        try {
            AdministrativeEnchantCommands.setEnchantment(
                    player,
                    targetSlot,
                    current,
                    enchantment,
                    newLevel);
            player.getInventory().setChanged();
            expectedTarget = player.getInventory().getItem(targetSlot).copy();
            player.containerMenu.broadcastChanges();
        } catch (RuntimeException exception) {
            player.getInventory().setItem(targetSlot, rollback);
            player.getInventory().setChanged();
            return 0;
        }
        player.displayClientMessage(
                Enchantment.getFullname(enchantment, Math.max(1, newLevel)).copy()
                        .append(newLevel == 0 ? Component.literal(" removed") : Component.literal(" applied"))
                        .withStyle(newLevel == 0 ? ChatFormatting.RED : ChatFormatting.GREEN),
                true);
        AuditService.record(mutationAuditEvent(
                SecurityAuditService.currentSessionId(),
                player.getUUID(),
                player.getGameProfile().getName(),
                player.getUUID(),
                enchantment.key().location().toString(),
                expectedLevel,
                newLevel,
                targetSlot,
                CommandAuditScope.currentCorrelationId().orElse(null)));
        requestedLevel = newLevel;
        refresh();
        return 1;
    }

    static AuditService.Event mutationAuditEvent(
            UUID sessionId,
            UUID actorId,
            String actorName,
            UUID targetId,
            String enchantmentId,
            int previousLevel,
            int newLevel,
            int targetSlot,
            UUID parentCorrelation
    ) {
        return AuditService.Event.interaction(
                sessionId,
                actorId,
                actorName,
                "player",
                "sef:workstation.super_enchant.mutation",
                List.of(targetId),
                Map.of(
                        "enchantment", enchantmentId,
                        "previous_level", Integer.toString(previousLevel),
                        "new_level", Integer.toString(newLevel),
                        "target_slot", Integer.toString(targetSlot)),
                AuditService.Result.SUCCESS,
                ActionResult.ReasonCode.SUCCESS,
                "menu",
                parentCorrelation,
                AuditService.RedactionClass.METADATA,
                AuditService.AuditClass.ADMIN_ACTION);
    }

    private boolean canApply(ItemStack target, Holder<Enchantment> candidate) {
        if (!target.supportsEnchantment(candidate)) {
            if (!AdministrativeEnchantCommands.arbitraryItemsEnabled()
                    || !unsafePermission("commands.enchant.any_item")) {
                return false;
            }
        }

        for (Holder<Enchantment> existing : AdministrativeEnchantCommands.enchantments(target).keySet()) {
            if (!existing.equals(candidate) && !Enchantment.areCompatible(existing, candidate)) {
                return AdministrativeEnchantCommands.incompatibleEnchantmentsEnabled()
                        && unsafePermission("commands.enchant.incompatible");
            }
        }
        return true;
    }

    private boolean unsafePermission(String permission) {
        return PermissionService.has(player, PermissionsHandler.phasePermission(permission));
    }

    private void refresh() {
        ItemStack target = targetItem();
        if (target.isEmpty()) {
            return;
        }

        Registry<Enchantment> registry = player.registryAccess().registry(Registries.ENCHANTMENT).orElse(null);
        if (registry == null) {
            display.clearContent();
            player.closeContainer();
            player.sendSystemMessage(TextFormatter.stringToFormattedText(
                    "&cThe enchantment registry is unavailable."));
            return;
        }
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
        display.setItem(PREVIOUS_SLOT, namedItem(
                page > 0 ? Items.ARROW : Items.GRAY_STAINED_GLASS_PANE,
                page > 0 ? "previous page" : "first page"));
        display.setItem(DECREASE_LARGE_SLOT, namedItem(Items.REDSTONE, "decrease level by ten"));
        display.setItem(DECREASE_SLOT, namedItem(Items.REDSTONE_TORCH, "decrease level by one"));
        display.setItem(STATUS_SLOT, statusItem());
        display.setItem(TARGET_SLOT, target.copy());
        display.setItem(INCREASE_SLOT, namedItem(Items.EXPERIENCE_BOTTLE, "increase level by one"));
        display.setItem(INCREASE_LARGE_SLOT, namedItem(Items.LAPIS_LAZULI, "increase level by ten"));
        display.setItem(CONFIRM_SLOT, confirmItem());
        display.setItem(NEXT_SLOT, namedItem(
                page + 1 < pageCount() ? Items.ARROW : Items.GRAY_STAINED_GLASS_PANE,
                page + 1 < pageCount() ? "next page" : "last page"));
        broadcastChanges();
    }

    private boolean shouldShow(ItemStack target, Holder<Enchantment> enchantment) {
        if (enchantmentLevel(target, enchantment) > 0) {
            return true;
        }
        return canApply(target, enchantment);
    }

    private ItemStack enchantmentButton(ItemStack target, Holder<Enchantment> enchantment) {
        int currentLevel = enchantmentLevel(target, enchantment);
        ItemStack button = new ItemStack(Items.ENCHANTED_BOOK);
        button.set(DataComponents.CUSTOM_NAME, Enchantment.getFullname(enchantment, Math.max(1, currentLevel)));
        button.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("current level, " + currentLevel).withStyle(ChatFormatting.GRAY),
                Component.literal("left click selects this enchantment").withStyle(ChatFormatting.GREEN),
                Component.literal("right click prepares removal").withStyle(ChatFormatting.RED),
                Component.literal("shift click selects the configured maximum").withStyle(ChatFormatting.YELLOW))));
        return button;
    }

    private ItemStack statusItem() {
        ItemStack status = namedItem(
                selected == null ? Items.PAPER : Items.WRITABLE_BOOK,
                selected == null ? "select an enchantment" : "pending enchantment change");
        String selectedName = selected == null
                ? "none"
                : selected.key().location().toString();
        boolean unsafe = selected != null && requestedLevel > selected.value().getMaxLevel();
        status.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("selected, " + selectedName).withStyle(ChatFormatting.GRAY),
                Component.literal("requested level, " + requestedLevel).withStyle(ChatFormatting.GRAY),
                Component.literal(unsafe ? "unsafe level permission required" : "within vanilla level range")
                        .withStyle(unsafe ? ChatFormatting.RED : ChatFormatting.GREEN),
                Component.literal("page " + (page + 1) + " of " + pageCount()).withStyle(ChatFormatting.GRAY),
                Component.literal("minimum level, " + configuredMinimum()).withStyle(ChatFormatting.GRAY),
                Component.literal("maximum level, " + configuredMaximum()).withStyle(ChatFormatting.GRAY))));
        return status;
    }

    private ItemStack confirmItem() {
        ItemStack confirm = namedItem(
                selected == null ? Items.GRAY_DYE : Items.LIME_DYE,
                selected == null ? "nothing selected" : "apply selected level");
        confirm.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("all checks run again before mutation").withStyle(ChatFormatting.GRAY),
                Component.literal("cooldown and cost apply on success").withStyle(ChatFormatting.YELLOW))));
        return confirm;
    }

    private static ItemStack namedItem(net.minecraft.world.item.Item item, String name) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return stack;
    }

    private ItemStack targetItem() {
        return player.getInventory().getItem(targetSlot);
    }

    private boolean validTarget() {
        ItemStack target = targetItem();
        return !target.isEmpty()
                && ItemStack.matches(target, expectedTarget);
    }

    @Override
    public boolean stillValid(Player player) {
        return player == this.player
                && !this.player.hasDisconnected()
                && authorizationValid();
    }

    private boolean authorizationValid() {
        return configurationRevision == KernelServices.configurationRevision()
                && ConfigHandler.config.enableSuperEnchantingTableCommand.get()
                && configuredLevelsValid()
                && validTarget()
                && KernelCommandExecutor.canUse(
                this.player.createCommandSourceStack(),
                "sef:workstation.super_enchant.mutate");
    }

    private static int enchantmentLevel(ItemStack target, Holder<Enchantment> enchantment) {
        return AdministrativeEnchantCommands.enchantments(target).getLevel(enchantment);
    }

    static int normalizeLevel(int requested, int minimum, int maximum) {
        if (minimum < 1
                || maximum < minimum
                || maximum > AdministrativeEnchantCommands.IMPLEMENTATION_MAXIMUM_LEVEL) {
            throw new IllegalArgumentException("Super enchanting level bounds are invalid");
        }
        return requested <= 0 ? 0 : Math.clamp(requested, minimum, maximum);
    }

    private static boolean configuredLevelsValid() {
        int minimum = configuredMinimum();
        int maximum = configuredMaximum();
        return minimum >= 1
                && maximum >= minimum
                && maximum <= AdministrativeEnchantCommands.IMPLEMENTATION_MAXIMUM_LEVEL;
    }

    private static int configuredMinimum() {
        return AdministrativeEnchantCommands.configuredMinimum();
    }

    private static int configuredMaximum() {
        return AdministrativeEnchantCommands.configuredMaximum();
    }

    private Holder.Reference<Enchantment> resolveSelected() {
        if (selected == null) {
            return null;
        }
        Registry<Enchantment> registry =
                player.registryAccess().registry(Registries.ENCHANTMENT).orElse(null);
        return registry == null ? null : registry.getHolder(selected.key()).orElse(null);
    }

    private int pageCount() {
        return Math.max(1, (enchantments.size() + PAGE_SIZE - 1) / PAGE_SIZE);
    }
}
