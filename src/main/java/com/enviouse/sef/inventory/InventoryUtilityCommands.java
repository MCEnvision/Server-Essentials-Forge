package com.enviouse.sef.inventory;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.identity.IdentityArguments;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.vanish.VanishUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class InventoryUtilityCommands {
    private static final List<Conversion> CONVERSIONS = List.of(
            new Conversion(Items.IRON_NUGGET, Items.IRON_INGOT, 9),
            new Conversion(Items.GOLD_NUGGET, Items.GOLD_INGOT, 9),
            new Conversion(Items.IRON_INGOT, Items.IRON_BLOCK, 9),
            new Conversion(Items.GOLD_INGOT, Items.GOLD_BLOCK, 9),
            new Conversion(Items.COPPER_INGOT, Items.COPPER_BLOCK, 9),
            new Conversion(Items.DIAMOND, Items.DIAMOND_BLOCK, 9),
            new Conversion(Items.EMERALD, Items.EMERALD_BLOCK, 9),
            new Conversion(Items.COAL, Items.COAL_BLOCK, 9),
            new Conversion(Items.REDSTONE, Items.REDSTONE_BLOCK, 9),
            new Conversion(Items.LAPIS_LAZULI, Items.LAPIS_BLOCK, 9),
            new Conversion(Items.QUARTZ, Items.QUARTZ_BLOCK, 4),
            new Conversion(Items.WHEAT, Items.HAY_BLOCK, 9));

    private InventoryUtilityCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (!ConfigHandler.config.enableInventoryUtilities.get()) {
            return;
        }
        dispatcher.register(clearNode("clearinventory"));
        dispatcher.register(enderNode("enderchest"));
        if (KernelServices.shortcuts().isActive("ci")) {
            dispatcher.register(clearNode("ci"));
        }
        if (KernelServices.shortcuts().isActive("ec")) {
            dispatcher.register(enderNode("ec"));
        }
        dispatcher.register(Commands.literal("disposal")
                .requires(source -> source.getPlayer() != null && has(source, "commands.disposal"))
                .executes(context -> disposal(context.getSource())));
        dispatcher.register(Commands.literal("more")
                .requires(source -> source.getPlayer() != null && has(source, "commands.more"))
                .executes(context -> more(context.getSource())));
        dispatcher.register(Commands.literal("condense")
                .requires(source -> source.getPlayer() != null && has(source, "commands.condense"))
                .executes(context -> condense(context.getSource())));
        dispatcher.register(Commands.literal("hat")
                .requires(source -> source.getPlayer() != null && has(source, "commands.hat"))
                .executes(context -> hat(context.getSource())));
        dispatcher.register(Commands.literal("itemname")
                .requires(source -> source.getPlayer() != null && has(source, "commands.itemname"))
                .then(Commands.literal("clear")
                        .executes(context -> itemName(context.getSource(), "")))
                .then(Commands.argument("name", StringArgumentType.greedyString())
                        .executes(context -> itemName(
                                context.getSource(),
                                StringArgumentType.getString(context, "name")))));
        dispatcher.register(Commands.literal("itemlore")
                .requires(source -> source.getPlayer() != null && has(source, "commands.itemlore"))
                .then(Commands.literal("clear")
                        .executes(context -> itemLore(context.getSource(), "", true)))
                .then(Commands.literal("add")
                        .then(Commands.argument("line", StringArgumentType.greedyString())
                                .executes(context -> itemLore(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "line"),
                                        false)))));
        dispatcher.register(Commands.literal("itemdb")
                .requires(source -> source.getPlayer() != null && has(source, "commands.itemdb"))
                .executes(context -> itemDb(context.getSource())));
        dispatcher.register(Commands.literal("book")
                .requires(source -> source.getPlayer() != null && has(source, "commands.book"))
                .executes(context -> inspectBook(context.getSource()))
                .then(Commands.literal("inspect")
                        .executes(context -> inspectBook(context.getSource())))
                .then(Commands.literal("clear")
                        .executes(context -> clearBook(context.getSource())))
                .then(Commands.literal("addpage")
                        .then(Commands.argument("text", StringArgumentType.greedyString())
                                .executes(context -> addBookPage(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "text")))))
                .then(Commands.literal("title")
                        .then(Commands.argument("title", StringArgumentType.greedyString())
                                .executes(context -> setBookTitle(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "title"))))));
        dispatcher.register(Commands.literal("recipe")
                .requires(source -> source.getPlayer() != null && has(source, "commands.recipe"))
                .executes(context -> recipes(context.getSource())));
        if (ConfigHandler.config.enableItemShortcut.get()
                && KernelServices.shortcuts().isActive("i")) {
            dispatcher.register(itemShortcut());
        }
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> clearNode(
            String literal
    ) {
        return Commands.literal(literal)
                .requires(source -> has(source, "commands.clearinventory")
                        || has(source, "commands.clearinventory.others"))
                .executes(context -> clear(context.getSource(), context.getSource().getPlayer()))
                .then(IdentityArguments.online("player")
                        .requires(source -> has(source, "commands.clearinventory.others"))
                        .executes(context -> clear(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "player"))));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> enderNode(
            String literal
    ) {
        return Commands.literal(literal)
                .requires(source -> has(source, "commands.enderchest")
                        || has(source, "commands.enderchest.others"))
                .executes(context -> enderChest(context.getSource(), context.getSource().getPlayer()))
                .then(IdentityArguments.online("player")
                        .requires(source -> has(source, "commands.enderchest.others"))
                        .executes(context -> enderChest(
                                context.getSource(),
                                IdentityArguments.getOnline(context, "player"))));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> itemShortcut() {
        return Commands.literal("i")
                .requires(source -> source.getPlayer() != null && has(source, "commands.item.give.self"))
                .then(Commands.argument("item", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                BuiltInRegistries.ITEM.keySet().stream()
                                        .map(id -> id.getNamespace().equals("minecraft")
                                                ? id.getPath()
                                                : id.toString()),
                                builder))
                        .executes(context -> giveSelf(
                                context.getSource(),
                                StringArgumentType.getString(context, "item"),
                                1))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(
                                        1,
                                        ConfigHandler.config.itemGiveMaximumAmount.get()))
                                .executes(context -> giveSelf(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "item"),
                                        IntegerArgumentType.getInteger(context, "amount")))));
    }

    private static int clear(CommandSourceStack source, ServerPlayer target) {
        if (target == null) {
            return fail(source, "An explicit online target is required.");
        }
        boolean self = source.getPlayer() == target;
        if (!self && !eligible(source, target)) {
            return unavailable(source);
        }
        String permission = self ? "commands.clearinventory" : "commands.clearinventory.others";
        return execute(source, "sef:inventory.clear",
                Map.of("target", target.getUUID().toString(), "self", Boolean.toString(self)),
                List.of(target.getUUID()), () -> {
                    int stacks = 0;
                    for (int slot = 0; slot < target.getInventory().getContainerSize(); slot++) {
                        if (!target.getInventory().getItem(slot).isEmpty()) {
                            stacks++;
                        }
                    }
                    target.getInventory().clearContent();
                    target.getInventory().setChanged();
                    target.containerMenu.broadcastChanges();
                    success(source, "Cleared " + stacks + " inventory stacks.");
                    return Math.max(1, stacks);
                }, permission(permission));
    }

    private static int enderChest(CommandSourceStack source, ServerPlayer target) {
        ServerPlayer viewer = source.getPlayer();
        if (viewer == null || target == null) {
            return fail(source, "This command requires an online player.");
        }
        boolean self = viewer == target;
        if (!self && !eligible(source, target)) {
            return unavailable(source);
        }
        return execute(source, "sef:inventory.enderchest",
                Map.of("target", target.getUUID().toString(), "self", Boolean.toString(self)),
                List.of(target.getUUID()), () -> {
                    viewer.openMenu(new SimpleMenuProvider(
                            (containerId, inventory, ignored) ->
                                    new LiveEnderChestMenu(containerId, inventory, viewer, target),
                            Component.translatable("container.enderchest")));
                    return 1;
                }, permission(self ? "commands.enderchest" : "commands.enderchest.others"));
    }

    private static int disposal(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        return execute(source, "sef:inventory.disposal", Map.of(), List.of(player.getUUID()), () -> {
            SimpleContainer disposal = new SimpleContainer(27);
            player.openMenu(new SimpleMenuProvider(
                    (containerId, inventory, ignored) ->
                            ChestMenu.threeRows(containerId, inventory, disposal),
                    Component.literal("Disposal")));
            info(source, "Items left in this disposal inventory are destroyed when it closes.");
            return 1;
        }, permission("commands.disposal"));
    }

    private static int more(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            return fail(source, "Hold an item first.");
        }
        return execute(source, "sef:inventory.more", Map.of(
                "item", BuiltInRegistries.ITEM.getKey(held.getItem()).toString()), List.of(player.getUUID()), () -> {
                    if (held.getCount() >= held.getMaxStackSize()) {
                        return fail(source, "That stack is already full.");
                    }
                    held.setCount(held.getMaxStackSize());
                    player.getInventory().setChanged();
                    player.containerMenu.broadcastChanges();
                    success(source, "Filled the held stack.");
                    return 1;
                }, permission("commands.more"));
    }

    private static int condense(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        return execute(source, "sef:inventory.condense", Map.of(), List.of(player.getUUID()), () -> {
            int crafted = condenseInventory(player.getInventory());
            if (crafted < 0) {
                return fail(source, "Inventory changed before condensation could commit.");
            }
            if (crafted == 0) {
                return fail(source, "No supported complete compression recipe was available.");
            }
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
            success(source, "Condensed inventory into " + crafted + " output items.");
            return crafted;
        }, permission("commands.condense"));
    }

    static int condenseInventory(Inventory inventory) {
        List<ItemStack> before = snapshot(inventory);
        int crafted = 0;
        try {
            for (Conversion conversion : CONVERSIONS) {
                int available = count(inventory, conversion.input());
                int outputs = available / conversion.ratio();
                if (outputs < 1) {
                    continue;
                }
                remove(inventory, conversion.input(), outputs * conversion.ratio());
                ItemStack result = new ItemStack(conversion.output(), outputs);
                if (!inventory.add(result) || !result.isEmpty()) {
                    restore(inventory, before);
                    return -1;
                }
                crafted += outputs;
            }
        } catch (RuntimeException exception) {
            restore(inventory, before);
            throw exception;
        }
        return crafted;
    }

    private static int hat(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            return fail(source, "Hold an item first.");
        }
        return execute(source, "sef:inventory.hat", Map.of(
                "item", BuiltInRegistries.ITEM.getKey(held.getItem()).toString()), List.of(player.getUUID()), () -> {
                    ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
                    player.setItemSlot(EquipmentSlot.HEAD, held.copy());
                    player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, head.copy());
                    player.getInventory().setChanged();
                    player.containerMenu.broadcastChanges();
                    success(source, "Equipped the held item as a hat.");
                    return 1;
                }, permission("commands.hat"));
    }

    private static int itemName(CommandSourceStack source, String name) {
        ServerPlayer player = source.getPlayer();
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            return fail(source, "Hold an item first.");
        }
        if (name.length() > 128 || name.codePoints().anyMatch(Character::isISOControl)) {
            return fail(source, "Item name is outside bounds.");
        }
        return execute(source, "sef:inventory.itemname",
                Map.of("clear", Boolean.toString(name.isBlank()), "length", Integer.toString(name.length())),
                List.of(player.getUUID()), () -> {
                    if (name.isBlank()) {
                        held.remove(DataComponents.CUSTOM_NAME);
                    } else {
                        held.set(DataComponents.CUSTOM_NAME, Component.literal(name));
                    }
                    player.getInventory().setChanged();
                    player.containerMenu.broadcastChanges();
                    success(source, name.isBlank() ? "Item name cleared." : "Item name updated.");
                    return 1;
                }, permission("commands.itemname"));
    }

    private static int itemLore(CommandSourceStack source, String line, boolean clear) {
        ServerPlayer player = source.getPlayer();
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            return fail(source, "Hold an item first.");
        }
        if (line.length() > 256 || line.codePoints().anyMatch(Character::isISOControl)) {
            return fail(source, "Lore line is outside bounds.");
        }
        ItemLore current = held.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);
        if (!clear && current.lines().size() >= 16) {
            return fail(source, "Lore line limit reached.");
        }
        return execute(source, "sef:inventory.itemlore",
                Map.of("clear", Boolean.toString(clear), "length", Integer.toString(line.length())),
                List.of(player.getUUID()), () -> {
                    if (clear) {
                        held.remove(DataComponents.LORE);
                    } else {
                        held.set(DataComponents.LORE, current.withLineAdded(Component.literal(line)));
                    }
                    player.getInventory().setChanged();
                    player.containerMenu.broadcastChanges();
                    success(source, clear ? "Item lore cleared." : "Item lore line added.");
                    return 1;
                }, permission("commands.itemlore"));
    }

    private static int itemDb(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            return fail(source, "Hold an item first.");
        }
        return execute(source, "sef:inventory.itemdb", Map.of(), List.of(player.getUUID()), () -> {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(held.getItem());
            info(source, "Item " + id + ", count " + held.getCount() + ", maximum "
                    + held.getMaxStackSize() + ", components " + held.getComponents().size() + ".");
            return 1;
        }, permission("commands.itemdb"));
    }

    private static int inspectBook(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        ItemStack held = player.getMainHandItem();
        WritableBookContent writable = held.get(DataComponents.WRITABLE_BOOK_CONTENT);
        WrittenBookContent written = held.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (writable == null && written == null) {
            return fail(source, "Hold a writable or written book first.");
        }
        return execute(source, "sef:inventory.book", Map.of("operation", "inspect"),
                List.of(player.getUUID()), () -> {
                    if (writable != null) {
                        info(source, "Writable book with " + writable.pages().size() + " pages.");
                    } else {
                        info(source, "Written book titled " + written.title().raw() + " by "
                                + written.author() + ", generation " + written.generation()
                                + ", pages " + written.pages().size() + ".");
                    }
                    return 1;
                }, permission("commands.book"));
    }

    private static int clearBook(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        ItemStack held = player.getMainHandItem();
        WritableBookContent writable = held.get(DataComponents.WRITABLE_BOOK_CONTENT);
        WrittenBookContent written = held.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (writable == null && written == null) {
            return fail(source, "Hold a writable or written book first.");
        }
        return execute(source, "sef:inventory.book", Map.of("operation", "clear"),
                List.of(player.getUUID()), () -> {
                    if (writable != null) {
                        held.set(DataComponents.WRITABLE_BOOK_CONTENT, WritableBookContent.EMPTY);
                    } else {
                        held.set(DataComponents.WRITTEN_BOOK_CONTENT, written.withReplacedPages(List.of()));
                    }
                    syncHeldItem(player);
                    success(source, "Book pages cleared.");
                    return 1;
                }, permission("commands.book"));
    }

    private static int addBookPage(CommandSourceStack source, String text) {
        ServerPlayer player = source.getPlayer();
        ItemStack held = player.getMainHandItem();
        WritableBookContent writable = held.get(DataComponents.WRITABLE_BOOK_CONTENT);
        WrittenBookContent written = held.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (writable == null && written == null) {
            return fail(source, "Hold a writable or written book first.");
        }
        if (!validBookText(text, 2048)) {
            return fail(source, "Book page text is outside bounds.");
        }
        int pages = writable != null ? writable.pages().size() : written.pages().size();
        if (pages >= 100) {
            return fail(source, "Book page limit reached.");
        }
        return execute(source, "sef:inventory.book", Map.of(
                        "operation", "addpage",
                        "length", Integer.toString(text.length())),
                List.of(player.getUUID()), () -> {
                    if (writable != null) {
                        List<Filterable<String>> replacement = new ArrayList<>(writable.pages());
                        replacement.add(Filterable.passThrough(text));
                        held.set(DataComponents.WRITABLE_BOOK_CONTENT, writable.withReplacedPages(replacement));
                    } else {
                        List<Filterable<Component>> replacement = new ArrayList<>(written.pages());
                        replacement.add(Filterable.passThrough(Component.literal(text)));
                        held.set(DataComponents.WRITTEN_BOOK_CONTENT, written.withReplacedPages(replacement));
                    }
                    syncHeldItem(player);
                    success(source, "Book page added.");
                    return 1;
                }, permission("commands.book"));
    }

    private static int setBookTitle(CommandSourceStack source, String title) {
        ServerPlayer player = source.getPlayer();
        ItemStack held = player.getMainHandItem();
        WrittenBookContent written = held.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (written == null) {
            return fail(source, "Hold a written book first.");
        }
        if (!validBookText(title, 32)) {
            return fail(source, "Book title is outside bounds.");
        }
        return execute(source, "sef:inventory.book", Map.of(
                        "operation", "title",
                        "length", Integer.toString(title.length())),
                List.of(player.getUUID()), () -> {
                    held.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
                            Filterable.passThrough(title),
                            written.author(),
                            written.generation(),
                            written.pages(),
                            written.resolved()));
                    syncHeldItem(player);
                    success(source, "Book title updated.");
                    return 1;
                }, permission("commands.book"));
    }

    private static boolean validBookText(String value, int maximumLength) {
        return value != null
                && !value.isBlank()
                && value.length() <= maximumLength
                && value.codePoints().noneMatch(Character::isISOControl);
    }

    private static void syncHeldItem(ServerPlayer player) {
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }

    private static int recipes(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            return fail(source, "Hold an item first.");
        }
        return execute(source, "sef:inventory.recipe", Map.of(), List.of(player.getUUID()), () -> {
            List<ResourceLocation> recipes = source.getServer().getRecipeManager().getRecipes().stream()
                    .filter(holder -> ItemStack.isSameItemSameComponents(
                            holder.value().getResultItem(source.registryAccess()),
                            held.copyWithCount(1)))
                    .map(holder -> holder.id())
                    .sorted()
                    .limit(32)
                    .toList();
            info(source, recipes.isEmpty() ? "No loaded recipe creates the held item."
                    : "Loaded recipes. " + String.join(", ", recipes.stream()
                    .map(ResourceLocation::toString).toList()));
            return Math.max(1, recipes.size());
        }, permission("commands.recipe"));
    }

    private static int giveSelf(CommandSourceStack source, String input, int amount) {
        ServerPlayer player = source.getPlayer();
        String normalized = input.contains(":") ? input : "minecraft:" + input;
        ResourceLocation id = ResourceLocation.tryParse(normalized);
        if (id == null || input.indexOf('[') >= 0 || input.indexOf('{') >= 0) {
            return fail(source, "Use one exact item registry id without component or SNBT data.");
        }
        Optional<Item> item = BuiltInRegistries.ITEM.getOptional(id);
        if (item.isEmpty() || item.orElseThrow() == Items.AIR) {
            return fail(source, "Item registry id not found.");
        }
        ItemStack grant = new ItemStack(item.orElseThrow(), amount);
        if (!canFit(player, List.of(grant))) {
            return fail(source, "Inventory does not have enough space.");
        }
        return execute(source, "sef:item.give.self",
                Map.of("item", id.toString(), "amount", Integer.toString(amount)),
                List.of(player.getUUID()), () -> {
                    List<ItemStack> before = snapshot(player.getInventory());
                    ItemStack commit = grant.copy();
                    try {
                        if (!player.getInventory().add(commit) || !commit.isEmpty()) {
                            restore(player.getInventory(), before);
                            return fail(source, "Inventory changed before the item grant could commit.");
                        }
                    } catch (RuntimeException exception) {
                        restore(player.getInventory(), before);
                        throw exception;
                    }
                    player.getInventory().setChanged();
                    player.containerMenu.broadcastChanges();
                    success(source, "Received " + amount + " x " + id + ".");
                    return amount;
                }, permission("commands.item.give.self"));
    }

    private static boolean eligible(CommandSourceStack source, ServerPlayer target) {
        if (source.getPlayer() != null && VanishUtil.isVanished(target, source.getPlayer())) {
            return false;
        }
        return PlayerTargetPolicy.decide(
                source,
                target,
                permission("inventory.hierarchy.bypass"),
                permission("exempt.inventory"),
                permission("inventory.bypass.exempt"),
                false,
                true).allowed();
    }

    static boolean canAccessEnderChest(ServerPlayer viewer, ServerPlayer target) {
        if (!ConfigHandler.config.enableInventoryUtilities.get()
                || viewer == null
                || target == null) {
            return false;
        }
        if (viewer == target) {
            return PermissionService.has(viewer, permission("commands.enderchest"));
        }
        return PermissionService.has(viewer, permission("commands.enderchest.others"))
                && eligible(viewer.createCommandSourceStack(), target);
    }

    private static int count(Inventory inventory, Item item) {
        int total = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(item)) {
                total = Math.addExact(total, stack.getCount());
            }
        }
        return total;
    }

    private static void remove(Inventory inventory, Item item, int amount) {
        int remaining = amount;
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.is(item)) {
                continue;
            }
            int taken = Math.min(remaining, stack.getCount());
            stack.shrink(taken);
            remaining -= taken;
        }
        if (remaining != 0) {
            throw new IllegalStateException("Inventory source count changed during condensation");
        }
    }

    private static boolean canFit(ServerPlayer player, List<ItemStack> items) {
        Inventory simulated = new Inventory(player);
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            simulated.setItem(slot, player.getInventory().getItem(slot).copy());
        }
        for (ItemStack item : items) {
            ItemStack candidate = item.copy();
            if (!simulated.add(candidate) || !candidate.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static List<ItemStack> snapshot(Inventory inventory) {
        List<ItemStack> result = new ArrayList<>(inventory.getContainerSize());
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            result.add(inventory.getItem(slot).copy());
        }
        return result;
    }

    private static void restore(Inventory inventory, List<ItemStack> snapshot) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            inventory.setItem(slot, snapshot.get(slot).copy());
        }
        inventory.setChanged();
    }

    @SafeVarargs
    private static int execute(
            CommandSourceStack source,
            String action,
            Map<String, String> parameters,
            List<UUID> targets,
            java.util.function.IntSupplier operation,
            PermissionNode<Boolean>... permissions
    ) {
        return KernelCommandExecutor.execute(source, action, parameters, targets, false, operation, permissions);
    }

    private static boolean has(CommandSourceStack source, String id) {
        PermissionNode<Boolean> node = permission(id);
        return node != null && PermissionService.has(source, node);
    }

    private static PermissionNode<Boolean> permission(String id) {
        return PermissionsHandler.phasePermission(id);
    }

    private static int unavailable(CommandSourceStack source) {
        return fail(source, "That player is unavailable.");
    }

    private static int fail(CommandSourceStack source, String message) {
        source.sendFailure(TextFormatter.stringToFormattedText("&c" + message));
        return 0;
    }

    private static void success(CommandSourceStack source, String message) {
        source.sendSuccess(() -> TextFormatter.stringToFormattedText("&a" + message), false);
    }

    private static void info(CommandSourceStack source, String message) {
        source.sendSuccess(() -> TextFormatter.stringToFormattedText("&7" + message), false);
    }

    private record Conversion(Item input, Item output, int ratio) {
    }
}
