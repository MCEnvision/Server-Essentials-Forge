package com.enviouse.sef.economy;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.identity.IdentityArguments;
import com.enviouse.sef.identity.IdentityService;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.vanish.VanishUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class EconomyCommands {
    private static final UUID CONSOLE_ACTOR =
            UUID.nameUUIDFromBytes("sef:console".getBytes(StandardCharsets.UTF_8));
    private static final int MAXIMUM_CONFIRMATIONS = 10_000;
    private static final long CONFIRMATION_LIFETIME_MILLIS = 30_000L;
    private static final Map<UUID, PendingPayment> PENDING_PAYMENTS = new ConcurrentHashMap<>();
    private static final Map<UUID, PendingAdjustment> PENDING_ADJUSTMENTS = new ConcurrentHashMap<>();

    private EconomyCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (!ConfigHandler.config.enableEconomy.get()) {
            return;
        }
        registerBalance(dispatcher, "balance");
        registerBalance(dispatcher, "bal");
        registerBalance(dispatcher, "money");
        if (active("pay")) {
            dispatcher.register(Commands.literal("pay")
                    .requires(source -> has(source, "commands.pay"))
                    .then(IdentityArguments.known("player")
                            .then(Commands.argument("amount", StringArgumentType.word())
                                    .executes(context -> pay(
                                            context.getSource(),
                                            StringArgumentType.getString(context, "player"),
                                            StringArgumentType.getString(context, "amount"),
                                            false))
                                    .then(Commands.literal("confirm")
                                            .executes(context -> pay(
                                                    context.getSource(),
                                                    StringArgumentType.getString(context, "player"),
                                                    StringArgumentType.getString(context, "amount"),
                                                    true))))));
        }
        if (active("paytoggle")) {
            dispatcher.register(Commands.literal("paytoggle")
                    .requires(source -> has(source, "commands.paytoggle"))
                    .executes(context -> payToggle(context.getSource())));
        }
        if (active("payconfirmtoggle")) {
            dispatcher.register(Commands.literal("payconfirmtoggle")
                    .requires(source -> has(source, "commands.payconfirmtoggle"))
                    .executes(context -> payConfirmToggle(context.getSource())));
        }
        registerBalanceTop(dispatcher, "balancetop");
        registerBalanceTop(dispatcher, "baltop");
        if (active("worth")) {
            dispatcher.register(worthNode());
        }
        if (active("sell")) {
            dispatcher.register(sellNode());
        }
        dispatcher.register(ecoNode());
        if (active("setworth")) {
            dispatcher.register(Commands.literal("setworth")
                    .requires(source -> has(source, "commands.setworth"))
                    .then(Commands.argument("item", ResourceLocationArgument.id())
                            .then(Commands.argument("amount", StringArgumentType.word())
                                    .executes(context -> setWorth(
                                            context.getSource(),
                                            ResourceLocationArgument.getId(context, "item").toString(),
                                            StringArgumentType.getString(context, "amount"))))));
        }
    }

    private static void registerBalance(CommandDispatcher<CommandSourceStack> dispatcher, String literal) {
        if (!active(literal)) {
            return;
        }
        dispatcher.register(Commands.literal(literal)
                .requires(source -> has(source, "commands.balance"))
                .executes(context -> balanceSelf(context.getSource()))
                .then(IdentityArguments.known("player")
                        .requires(source -> has(source, "commands.balance.others"))
                        .executes(context -> balanceOther(
                                context.getSource(),
                                StringArgumentType.getString(context, "player")))));
    }

    private static void registerBalanceTop(CommandDispatcher<CommandSourceStack> dispatcher, String literal) {
        if (!active(literal)) {
            return;
        }
        dispatcher.register(Commands.literal(literal)
                .requires(source -> has(source, "commands.balancetop"))
                .executes(context -> balanceTop(context.getSource(), 1))
                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                        .executes(context -> balanceTop(
                                context.getSource(),
                                IntegerArgumentType.getInteger(context, "page")))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> worthNode() {
        return Commands.literal("worth")
                .requires(source -> has(source, "commands.worth"))
                .executes(context -> worthHand(context.getSource()))
                .then(Commands.literal("hand")
                        .executes(context -> worthHand(context.getSource())))
                .then(Commands.literal("inventory")
                        .executes(context -> worthInventory(context.getSource())))
                .then(Commands.literal("item")
                        .then(Commands.argument("item", StringArgumentType.word())
                                .executes(context -> worthItem(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "item")))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> sellNode() {
        return Commands.literal("sell")
                .requires(source -> has(source, "commands.sell"))
                .then(Commands.literal("hand")
                        .executes(context -> sellHand(context.getSource(), -1))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(context -> sellHand(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "amount")))))
                .then(Commands.literal("inventory")
                        .executes(context -> sellInventory(context.getSource())))
                .then(Commands.literal("item")
                        .then(Commands.argument("item", StringArgumentType.word())
                                .executes(context -> sellItem(context.getSource(),
                                        StringArgumentType.getString(context, "item"), -1))
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> sellItem(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "item"),
                                                IntegerArgumentType.getInteger(context, "amount"))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> ecoNode() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("eco")
                .requires(EconomyCommands::hasAnyEcoPermission);
        root.then(adjustmentNode("give"));
        root.then(adjustmentNode("take"));
        root.then(adjustmentNode("set"));
        root.then(Commands.literal("reset")
                .requires(source -> has(source, "commands.eco.reset"))
                .then(IdentityArguments.known("player")
                        .executes(context -> reset(
                                context.getSource(),
                                StringArgumentType.getString(context, "player"),
                                false))
                        .then(Commands.literal("confirm")
                                .executes(context -> reset(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "player"),
                                        true)))));
        root.then(Commands.literal("freeze")
                .requires(source -> has(source, "commands.eco.freeze"))
                .then(IdentityArguments.known("player")
                        .executes(context -> freeze(
                                context.getSource(),
                                StringArgumentType.getString(context, "player"),
                                true))));
        root.then(Commands.literal("unfreeze")
                .requires(source -> has(source, "commands.eco.unfreeze"))
                .then(IdentityArguments.known("player")
                        .executes(context -> freeze(
                                context.getSource(),
                                StringArgumentType.getString(context, "player"),
                                false))));
        root.then(Commands.literal("history")
                .requires(source -> has(source, "commands.eco.history"))
                .then(IdentityArguments.known("player")
                        .executes(context -> history(
                                context.getSource(),
                                StringArgumentType.getString(context, "player"),
                                1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> history(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "player"),
                                        IntegerArgumentType.getInteger(context, "page"))))));
        root.then(Commands.literal("import")
                .requires(source -> has(source, "commands.eco.import"))
                .executes(context -> importStatus(context.getSource()))
                .then(Commands.literal("status")
                        .executes(context -> importStatus(context.getSource())))
                .then(Commands.literal("preview")
                        .executes(context -> importPreview(context.getSource())))
                .then(Commands.literal("execute")
                        .then(Commands.literal("confirm")
                                .executes(context -> importExecute(context.getSource())))));
        root.then(Commands.literal("sign")
                .requires(source -> has(source, "economy.sign.manage"))
                .executes(context -> signList(context.getSource(), 1))
                .then(Commands.literal("list")
                        .executes(context -> signList(context.getSource(), 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> signList(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "page")))))
                .then(Commands.literal("info")
                        .then(Commands.argument("sign", StringArgumentType.word())
                                .executes(context -> signInfo(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "sign")))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("sign", StringArgumentType.word())
                                .then(Commands.literal("confirm")
                                        .executes(context -> signRemove(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "sign"))))))
                .then(Commands.literal("adopt")
                        .then(Commands.argument("sign", StringArgumentType.word())
                                .then(IdentityArguments.known("player")
                                        .executes(context -> signAdopt(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "sign"),
                                                StringArgumentType.getString(context, "player")))))));
        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> adjustmentNode(String operation) {
        return Commands.literal(operation)
                .requires(source -> has(source, "commands.eco." + operation))
                .then(IdentityArguments.known("player")
                        .then(Commands.argument("amount", StringArgumentType.word())
                                .executes(context -> adjustment(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "player"),
                                        StringArgumentType.getString(context, "amount"),
                                        operation,
                                        false))
                                .then(Commands.literal("confirm")
                                        .executes(context -> adjustment(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "player"),
                                                StringArgumentType.getString(context, "amount"),
                                                operation,
                                                true)))));
    }

    private static int balanceSelf(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return fail(source, "An explicit player is required from this command source.");
        }
        return balance(source, player.getUUID(), player.getUUID(), "sef:economy.balance", false);
    }

    private static int balanceOther(CommandSourceStack source, String input) {
        ResolvedTarget target = resolve(source, input);
        if (target == null
                || (target.onlinePlayer() != null
                && source.getPlayer() != null
                && VanishUtil.isVanished(target.onlinePlayer(), source.getPlayer()))) {
            return unavailable(source);
        }
        return balance(source, target.playerId(), actorId(source), "sef:economy.balance", true);
    }

    private static int balance(
            CommandSourceStack source,
            UUID playerId,
            UUID actorId,
            String action,
            boolean other
    ) {
        return execute(source, action, Map.of("other", Boolean.toString(other)), List.of(playerId), () -> {
            EconomyProvider provider = KernelServices.economy().requireProvider();
            ActionResult<EconomyProvider.Account> result;
            if (other) {
                EconomyProvider.Account account = provider.account(playerId).orElse(null);
                if (account == null && provider == KernelServices.economyRepository()) {
                    account = new EconomyProvider.Account(
                            playerId,
                            KernelServices.economyRepository().settings().defaultBalance(),
                            false,
                            0L,
                            0L);
                }
                result = account == null
                        ? ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "That account does not exist")
                        : ActionResult.success(account);
            } else {
                result = KernelServices.economy().getOrCreate(playerId, actorId);
            }
            if (!result.successful()) {
                return fail(source, result.detail());
            }
            info(source, displayName(source, playerId) + " has "
                    + KernelServices.economy().format(result.value().balance())
                    + (result.value().frozen() ? " and the account is frozen." : "."));
            return 1;
        }, other ? permission("commands.balance.others") : permission("commands.balance"));
    }

    private static int pay(CommandSourceStack source, String input, String rawAmount, boolean confirmed) {
        ServerPlayer sender = source.getPlayer();
        if (sender == null) {
            return fail(source, "Only players can pay another account.");
        }
        ResolvedTarget target = resolve(source, input);
        if (target == null) {
            return unavailable(source);
        }
        if (target.onlinePlayer() != null
                && VanishUtil.isVanished(target.onlinePlayer(), sender)) {
            return unavailable(source);
        }
        if (target.playerId().equals(sender.getUUID())
                && !KernelServices.economy().settings().allowSelfPayments()) {
            return fail(source, "You cannot pay your own account.");
        }
        if (!target.online()
                && (!KernelServices.economy().settings().allowOfflinePayments()
                || !has(source, "economy.pay.offline"))) {
            return unavailable(source);
        }
        if (!KernelServices.economyRepository().preferences(target.playerId()).paymentsEnabled()
                && !has(source, "economy.pay.bypass.toggle")) {
            return unavailable(source);
        }
        if (KernelServices.social().ignores(target.playerId(), sender.getUUID())
                && !has(source, "economy.pay.bypass.ignore")) {
            return unavailable(source);
        }
        final long amount;
        try {
            amount = KernelServices.economy().parsePositive(rawAmount);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return fail(source, exception.getMessage());
        }
        if (confirmed && !consumePayment(sender.getUUID(), target.playerId(), amount)) {
            return fail(source, "That payment confirmation is missing, expired, or changed.");
        }
        if (!confirmed && requiresPaymentConfirmation(sender.getUUID(), amount)) {
            try {
                rememberPayment(sender.getUUID(), target.playerId(), amount);
            } catch (IllegalStateException exception) {
                return fail(source, exception.getMessage());
            }
            info(source, "Confirm this payment with /pay "
                    + StringArgumentType.escapeIfRequired(input)
                    + " " + rawAmount + " confirm within 30 seconds.");
            return 1;
        }
        EconomyProvider provider;
        try {
            provider = KernelServices.economy().requireProvider();
        } catch (IllegalStateException exception) {
            return fail(source, exception.getMessage());
        }
        return execute(
                source,
                "sef:economy.pay",
                Map.of("amount_minor", Long.toString(amount)),
                List.of(target.playerId()),
                () -> {
                    if (!paymentAllowed(source, sender, target)) {
                        return unavailable(source);
                    }
                    ActionResult<EconomyProvider.Transaction> result = provider.transfer(
                            new EconomyProvider.TransferRequest(
                                    "pay." + UUID.randomUUID(),
                                    sender.getUUID(),
                                    sender.getUUID(),
                                    target.playerId(),
                                    "player payment",
                                    provider.currency(),
                                    amount,
                                    Map.of("route", "pay"),
                                    false));
                    if (!result.successful()) {
                        return fail(source, result.detail());
                    }
                    success(source, "Paid " + displayName(source, target.playerId()) + " "
                            + KernelServices.economy().format(amount) + ".");
                    ServerPlayer recipient = source.getServer().getPlayerList().getPlayer(target.playerId());
                    if (recipient != null && !recipient.getUUID().equals(sender.getUUID())) {
                        recipient.sendSystemMessage(TextFormatter.stringToFormattedText(
                                "&aYou received &e" + KernelServices.economy().format(amount)
                                        + " &afrom &e" + sender.getGameProfile().getName() + "&a."));
                    }
                    return 1;
                },
                permission("commands.pay"));
    }

    private static int payToggle(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return fail(source, "Only players have payment preferences.");
        }
        return execute(source, "sef:economy.pay.toggle", Map.of(), List.of(player.getUUID()), () -> {
            EconomyRepository.AccountPreferences current =
                    KernelServices.economyRepository().preferences(player.getUUID());
            EconomyRepository.AccountPreferences updated =
                    KernelServices.economyRepository().setPaymentsEnabled(
                            player.getUUID(),
                            !current.paymentsEnabled());
            success(source, "Incoming payments "
                    + (updated.paymentsEnabled() ? "enabled." : "disabled."));
            return 1;
        }, permission("commands.paytoggle"));
    }

    private static int payConfirmToggle(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return fail(source, "Only players have payment preferences.");
        }
        return execute(source, "sef:economy.pay.confirm", Map.of(), List.of(player.getUUID()), () -> {
            EconomyRepository.AccountPreferences current =
                    KernelServices.economyRepository().preferences(player.getUUID());
            EconomyRepository.AccountPreferences updated =
                    KernelServices.economyRepository().setConfirmLargePayments(
                            player.getUUID(),
                            !current.confirmLargePayments());
            success(source, "Large payment confirmation "
                    + (updated.confirmLargePayments() ? "enabled." : "disabled."));
            return 1;
        }, permission("commands.payconfirmtoggle"));
    }

    private static int balanceTop(CommandSourceStack source, int page) {
        return execute(source, "sef:economy.top", Map.of("page", Integer.toString(page)), List.of(), () -> {
            EconomyProvider provider = KernelServices.economy().requireProvider();
            int pageSize = KernelServices.economy().settings().balanceTopPageSize();
            long requestedEnd = (long) page * pageSize;
            if (requestedEnd > ConfigHandler.config.economyMaximumAccounts.get()) {
                return fail(source, "That balance page is outside configured bounds.");
            }
            EconomyProvider.BalanceSnapshot snapshot = provider.createSnapshot((int) requestedEnd);
            int start = (page - 1) * pageSize;
            if (start >= snapshot.entries().size()) {
                info(source, "No balances exist on that page.");
                return 1;
            }
            int end = Math.min(snapshot.entries().size(), start + pageSize);
            info(source, "Balance top page " + page + ".");
            for (int index = start; index < end; index++) {
                EconomyProvider.BalanceEntry entry = snapshot.entries().get(index);
                int rank = index + 1;
                source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                        "&7" + rank + ". &e" + displayName(source, entry.playerId())
                                + " &7" + KernelServices.economy().format(entry.balance())), false);
            }
            return end - start;
        }, permission("commands.balancetop"));
    }

    private static int worthHand(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return fail(source, "Only players have a held item.");
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) return fail(source, "Hold an item to inspect its worth.");
        return worthStack(source, stack);
    }

    private static int worthInventory(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return fail(source, "Only players have an inventory.");
        return execute(source, "sef:economy.worth", Map.of("scope", "inventory"), List.of(player.getUUID()), () -> {
            long total = 0L;
            int sellable = 0;
            for (ItemStack stack : player.getInventory().items) {
                if (stack.isEmpty()) continue;
                if (!ItemStack.isSameItemSameComponents(stack, new ItemStack(stack.getItem()))) continue;
                long each = worth(stack.getItem());
                if (each <= 0L) continue;
                total = Math.addExact(total, EconomyMoney.multiply(
                        each,
                        stack.getCount(),
                        KernelServices.economy().settings().maximumTransaction()));
                sellable += stack.getCount();
            }
            info(source, "Inventory worth is " + KernelServices.economy().format(total)
                    + " across " + sellable + " sellable items.");
            return 1;
        }, permission("commands.worth"));
    }

    private static int worthItem(CommandSourceStack source, String itemId) {
        Item item = item(itemId);
        if (item == null) return fail(source, "That item id is unavailable.");
        return execute(source, "sef:economy.worth", Map.of("item", itemId(item)), List.of(), () -> {
            long amount = worth(item);
            info(source, itemId(item) + " is worth "
                    + KernelServices.economy().format(amount) + " each.");
            return 1;
        }, permission("commands.worth"));
    }

    private static int worthStack(CommandSourceStack source, ItemStack stack) {
        if (!ItemStack.isSameItemSameComponents(stack, new ItemStack(stack.getItem()))) {
            return fail(source, "Items with nondefault components do not have a sell value.");
        }
        return execute(source, "sef:economy.worth", Map.of("item", itemId(stack.getItem())), List.of(), () -> {
            long each = worth(stack.getItem());
            long total = EconomyMoney.multiply(
                    each,
                    stack.getCount(),
                    KernelServices.economy().settings().maximumTransaction());
            info(source, stack.getCount() + " " + itemId(stack.getItem()) + " is worth "
                    + KernelServices.economy().format(total) + ".");
            return 1;
        }, permission("commands.worth"));
    }

    private static int sellHand(CommandSourceStack source, int requested) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return fail(source, "Only players have a held item.");
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) return fail(source, "Hold an item to sell it.");
        if (!ItemStack.isSameItemSameComponents(held, new ItemStack(held.getItem()))) {
            return fail(source, "Items with nondefault components cannot be sold.");
        }
        int amount = requested < 0 ? held.getCount() : requested;
        return sell(source, player, held.getItem(), amount, true);
    }

    private static int sellItem(CommandSourceStack source, String itemId, int requested) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return fail(source, "Only players have an inventory.");
        Item item = item(itemId);
        if (item == null) return fail(source, "That item id is unavailable.");
        int available = count(player.getInventory(), item);
        int amount = requested < 0 ? available : requested;
        return sell(source, player, item, amount, false);
    }

    private static int sellInventory(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return fail(source, "Only players have an inventory.");
        Inventory inventory = player.getInventory();
        List<ItemStack> before = snapshot(inventory);
        Map<Item, Integer> quantities = new LinkedHashMap<>();
        long total = 0L;
        int totalItems = 0;
        try {
            for (ItemStack stack : inventory.items) {
                if (stack.isEmpty()) continue;
                if (!ItemStack.isSameItemSameComponents(stack, new ItemStack(stack.getItem()))) continue;
                long each = worth(stack.getItem());
                if (each <= 0L) continue;
                quantities.merge(stack.getItem(), stack.getCount(), Math::addExact);
                total = Math.addExact(total, EconomyMoney.multiply(
                        each,
                        stack.getCount(),
                        KernelServices.economy().settings().maximumTransaction()));
                totalItems = Math.addExact(totalItems, stack.getCount());
            }
        } catch (ArithmeticException | IllegalArgumentException exception) {
            return fail(source, exception.getMessage());
        }
        if (totalItems < 1 || total <= 0L) {
            return fail(source, "Your inventory contains no sellable items.");
        }
        int count = totalItems;
        long value = total;
        return execute(source, "sef:economy.sell",
                Map.of("scope", "inventory", "items", Integer.toString(count), "amount_minor", Long.toString(value)),
                List.of(player.getUUID()), () -> {
                    for (Map.Entry<Item, Integer> entry : quantities.entrySet()) {
                        if (!remove(inventory, entry.getKey(), entry.getValue(), false)) {
                            restore(inventory, before);
                            return fail(source, "Inventory changed before the sale could commit.");
                        }
                    }
                    if (!creditSale(player, value, count, "inventory")) {
                        restore(inventory, before);
                        return fail(source, "The economy provider rejected the sale. Your inventory was restored.");
                    }
                    inventory.setChanged();
                    player.containerMenu.broadcastChanges();
                    success(source, "Sold " + count + " items for "
                            + KernelServices.economy().format(value) + ".");
                    return 1;
                }, permission("commands.sell"));
    }

    private static int sell(
            CommandSourceStack source,
            ServerPlayer player,
            Item item,
            int amount,
            boolean mainHandOnly
    ) {
        if (amount < 1 || amount > ConfigHandler.config.economySignMaximumQuantity.get()) {
            return fail(source, "Sale quantity is outside configured bounds.");
        }
        long each = worth(item);
        if (each <= 0L) {
            return fail(source, "That item has no configured worth.");
        }
        final long value;
        try {
            value = EconomyMoney.multiply(
                    each,
                    amount,
                    KernelServices.economy().settings().maximumTransaction());
        } catch (IllegalArgumentException exception) {
            return fail(source, exception.getMessage());
        }
        Inventory inventory = player.getInventory();
        List<ItemStack> before = snapshot(inventory);
        return execute(source, "sef:economy.sell",
                Map.of("item", itemId(item), "items", Integer.toString(amount), "amount_minor", Long.toString(value)),
                List.of(player.getUUID()), () -> {
                    if (!remove(inventory, item, amount, mainHandOnly)) {
                        return fail(source, "You do not have that many matching items.");
                    }
                    if (!creditSale(player, value, amount, itemId(item))) {
                        restore(inventory, before);
                        return fail(source, "The economy provider rejected the sale. Your inventory was restored.");
                    }
                    inventory.setChanged();
                    player.containerMenu.broadcastChanges();
                    success(source, "Sold " + amount + " " + itemId(item) + " for "
                            + KernelServices.economy().format(value) + ".");
                    return 1;
                }, permission("commands.sell"));
    }

    private static boolean creditSale(ServerPlayer player, long value, int count, String scope) {
        EconomyProvider provider = KernelServices.economy().requireProvider();
        ActionResult<EconomyProvider.Transaction> result = provider.deposit(
                new EconomyProvider.MutationRequest(
                        "sell." + UUID.randomUUID(),
                        player.getUUID(),
                        player.getUUID(),
                        "item sale",
                        provider.currency(),
                        value,
                        Map.of("scope", scope, "items", Integer.toString(count)),
                        false));
        return result.successful();
    }

    private static int adjustment(
            CommandSourceStack source,
            String input,
            String rawAmount,
            String operation,
            boolean confirmed
    ) {
        ResolvedTarget target = resolve(source, input);
        if (target == null || !eligible(source, target)) return unavailable(source);
        final long amount;
        try {
            amount = operation.equals("set")
                    ? KernelServices.economy().parseBalance(rawAmount)
                    : KernelServices.economy().parsePositive(rawAmount);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return fail(source, exception.getMessage());
        }
        UUID actor = actorId(source);
        if (requiresAdjustmentConfirmation(amount)) {
            if (!confirmed) {
                try {
                    rememberAdjustment(actor, operation, target.playerId(), amount);
                } catch (IllegalStateException exception) {
                    return fail(source, exception.getMessage());
                }
                info(source, "Confirm this adjustment with /eco " + operation + " "
                        + StringArgumentType.escapeIfRequired(input) + " " + rawAmount
                        + " confirm within 30 seconds.");
                return 1;
            }
            if (!consumeAdjustment(actor, operation, target.playerId(), amount)) {
                return fail(source, "That economy adjustment confirmation is missing, expired, or changed.");
            }
        }
        EconomyProvider provider = KernelServices.economy().requireProvider();
        return execute(source, "sef:economy.admin." + operation,
                Map.of("amount_minor", Long.toString(amount)), List.of(target.playerId()), () -> {
                    if (!eligible(source, target)) {
                        return unavailable(source);
                    }
                    EconomyProvider.MutationRequest request = new EconomyProvider.MutationRequest(
                            "eco." + operation + "." + UUID.randomUUID(),
                            actorId(source),
                            target.playerId(),
                            "administrative " + operation,
                            provider.currency(),
                            amount,
                            Map.of("route", "eco." + operation),
                            true);
                    ActionResult<EconomyProvider.Transaction> result = switch (operation) {
                        case "give" -> provider.deposit(request);
                        case "take" -> provider.withdraw(request);
                        default -> provider.setBalance(request);
                    };
                    if (!result.successful()) return fail(source, result.detail());
                    success(source, operation + " completed for " + displayName(source, target.playerId())
                            + ". New balance " + KernelServices.economy().format(
                            provider.account(target.playerId()).orElseThrow().balance()) + ".");
                    return 1;
                }, permission("commands.eco." + operation));
    }

    private static int reset(CommandSourceStack source, String input, boolean confirmed) {
        ResolvedTarget target = resolve(source, input);
        if (target == null || !eligible(source, target)) return unavailable(source);
        EconomyProvider provider = KernelServices.economy().requireProvider();
        long reset = provider == KernelServices.economyRepository()
                ? KernelServices.economyRepository().settings().defaultBalance()
                : 0L;
        UUID actor = actorId(source);
        if (!confirmed) {
            try {
                rememberAdjustment(actor, "reset", target.playerId(), reset);
            } catch (IllegalStateException exception) {
                return fail(source, exception.getMessage());
            }
            info(source, "Confirm this reset with /eco reset "
                    + StringArgumentType.escapeIfRequired(input)
                    + " confirm within 30 seconds.");
            return 1;
        }
        if (!consumeAdjustment(actor, "reset", target.playerId(), reset)) {
            return fail(source, "That economy reset confirmation is missing, expired, or changed.");
        }
        return execute(source, "sef:economy.admin.reset", Map.of(), List.of(target.playerId()), () -> {
            if (!eligible(source, target)) {
                return unavailable(source);
            }
            ActionResult<EconomyProvider.Transaction> result = provider.setBalance(
                    new EconomyProvider.MutationRequest(
                            "eco.reset." + UUID.randomUUID(),
                            actorId(source),
                            target.playerId(),
                            "administrative reset",
                            provider.currency(),
                            reset,
                            Map.of("route", "eco.reset"),
                            true));
            if (!result.successful()) return fail(source, result.detail());
            success(source, "Reset " + displayName(source, target.playerId()) + " to "
                    + KernelServices.economy().format(reset) + ".");
            return 1;
        }, permission("commands.eco.reset"));
    }

    private static int freeze(CommandSourceStack source, String input, boolean frozen) {
        ResolvedTarget target = resolve(source, input);
        if (target == null || !eligible(source, target)) return unavailable(source);
        EconomyProvider provider = KernelServices.economy().requireProvider();
        String operation = frozen ? "freeze" : "unfreeze";
        return execute(source, "sef:economy.admin." + operation, Map.of(), List.of(target.playerId()), () -> {
            if (!eligible(source, target)) {
                return unavailable(source);
            }
            ActionResult<EconomyProvider.Transaction> result = provider.freezeAccount(
                    new EconomyProvider.FreezeRequest(
                            "eco." + operation + "." + UUID.randomUUID(),
                            actorId(source),
                            target.playerId(),
                            "administrative " + operation,
                            provider.currency(),
                            frozen,
                            Map.of("route", "eco." + operation)));
            if (!result.successful()) return fail(source, result.detail());
            success(source, (frozen ? "Froze " : "Unfroze ")
                    + displayName(source, target.playerId()) + ".");
            return 1;
        }, permission("commands.eco." + operation));
    }

    private static int history(CommandSourceStack source, String input, int page) {
        ResolvedTarget target = resolve(source, input);
        if (target == null || !eligible(source, target)) return unavailable(source);
        return execute(source, "sef:economy.admin.history", Map.of("page", Integer.toString(page)),
                List.of(target.playerId()), () -> {
                    if (!eligible(source, target)) {
                        return unavailable(source);
                    }
                    EconomyProvider provider = KernelServices.economy().requireProvider();
                    int pageSize = KernelServices.economy().settings().historyPageSize();
                    long offset = (long) (page - 1) * pageSize;
                    if (offset > Integer.MAX_VALUE) return fail(source, "That history page is outside bounds.");
                    List<EconomyProvider.Transaction> transactions =
                            provider.listTransactions(target.playerId(), (int) offset, pageSize);
                    if (transactions.isEmpty()) {
                        info(source, "No transactions exist on that page.");
                        return 1;
                    }
                    info(source, "Economy history for " + displayName(source, target.playerId())
                            + ", page " + page + ".");
                    for (EconomyProvider.Transaction transaction : transactions) {
                        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                                "&7" + transaction.transactionId() + " &e"
                                        + transaction.type().name().toLowerCase(Locale.ROOT)
                                        + " &f" + KernelServices.economy().format(transaction.amount())
                                        + " &7" + transaction.reason()), false);
                    }
                    return transactions.size();
                }, permission("commands.eco.history"));
    }

    private static int importStatus(CommandSourceStack source) {
        return execute(source, "sef:economy.admin.import", Map.of("operation", "status"), List.of(), () -> {
            if (KernelServices.economy().importPending()) {
                info(source, "An import once operation is pending. Run /eco import preview.");
            } else if (KernelServices.economyRepository().imports().isEmpty()) {
                info(source, "No economy import is configured or completed.");
            } else {
                EconomyRepository.ImportRecord record =
                        KernelServices.economyRepository().imports().getLast();
                info(source, "Imported " + record.accounts() + " accounts from " + record.sourceId()
                        + ". Report " + record.reportHash() + ".");
            }
            return 1;
        }, permission("commands.eco.import"));
    }

    private static int importPreview(CommandSourceStack source) {
        return execute(source, "sef:economy.admin.import", Map.of("operation", "preview"), List.of(), () -> {
            try {
                EconomyProviderRegistry.ImportPreview preview = KernelServices.economy().importPreview();
                info(source, "Import preview. " + preview.accounts() + " accounts, "
                        + preview.totalMinorUnits() + " total minor units. " + preview.detail()
                        + " Run /eco import execute confirm to commit.");
                return 1;
            } catch (IllegalArgumentException | IllegalStateException exception) {
                return fail(source, exception.getMessage());
            }
        }, permission("commands.eco.import"));
    }

    private static int importExecute(CommandSourceStack source) {
        return execute(source, "sef:economy.admin.import", Map.of("operation", "execute"), List.of(), () -> {
            try {
                EconomyRepository.ImportRecord record =
                        KernelServices.economy().executeImport(actorId(source), "confirm");
                success(source, "Imported " + record.accounts() + " accounts. Report "
                        + record.reportHash() + ".");
                return 1;
            } catch (IllegalArgumentException | IllegalStateException exception) {
                return fail(source, exception.getMessage());
            }
        }, permission("commands.eco.import"));
    }

    private static int signList(CommandSourceStack source, int page) {
        int pageSize = Math.min(50, KernelServices.economy().settings().historyPageSize());
        long offset = (long) (page - 1) * pageSize;
        if (offset > Integer.MAX_VALUE) {
            return fail(source, "That economy sign page is outside bounds.");
        }
        return execute(
                source,
                "sef:economy.sign.manage",
                Map.of("operation", "list", "page", Integer.toString(page)),
                List.of(),
                () -> {
                    List<EconomySignRepository.SignRecord> entries =
                            KernelServices.economySigns().entries();
                    if (offset >= entries.size()) {
                        info(source, "No economy signs exist on that page.");
                        return 1;
                    }
                    int end = Math.min(entries.size(), (int) offset + pageSize);
                    info(source, "Economy signs, page " + page + ", total " + entries.size() + ".");
                    for (EconomySignRepository.SignRecord record :
                            entries.subList((int) offset, end)) {
                        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                                "&7" + record.key().stableId()
                                        + " &e" + record.type().id()
                                        + " &f" + record.creatorId()
                                        + " &7revision " + record.revision()), false);
                    }
                    return end - (int) offset;
                },
                permission("economy.sign.manage"));
    }

    private static int signInfo(CommandSourceStack source, String stableId) {
        return execute(
                source,
                "sef:economy.sign.manage",
                Map.of("operation", "info", "sign", stableId),
                List.of(),
                () -> {
                    EconomySignRepository.SignRecord record =
                            KernelServices.economySigns().find(stableId).orElse(null);
                    if (record == null) {
                        return fail(source, "That economy sign was not found.");
                    }
                    info(source, "Economy sign " + record.key().stableId() + ".");
                    info(source, "Type " + record.type().id()
                            + ", creator " + record.creatorId()
                            + ", revision " + record.revision() + ".");
                    info(source, "Arguments " + String.join(", ", record.arguments()) + ".");
                    return 1;
                },
                permission("economy.sign.manage"));
    }

    private static int signRemove(CommandSourceStack source, String stableId) {
        return execute(
                source,
                "sef:economy.sign.manage",
                Map.of("operation", "remove", "sign", stableId),
                List.of(),
                () -> {
                    EconomySignRepository.SignRecord record =
                            KernelServices.economySigns().find(stableId).orElse(null);
                    if (record == null || !KernelServices.economySigns().remove(record.key())) {
                        return fail(source, "That economy sign was not found.");
                    }
                    success(source, "Removed economy sign " + stableId + ".");
                    return 1;
                },
                permission("economy.sign.manage"));
    }

    private static int signAdopt(
            CommandSourceStack source,
            String stableId,
            String playerInput
    ) {
        ResolvedTarget target = resolve(source, playerInput);
        if (target == null || !eligible(source, target)) {
            return unavailable(source);
        }
        return execute(
                source,
                "sef:economy.sign.manage",
                Map.of("operation", "adopt", "sign", stableId),
                List.of(target.playerId()),
                () -> {
                    if (!eligible(source, target)) {
                        return unavailable(source);
                    }
                    try {
                        KernelServices.economySigns().adopt(stableId, target.playerId());
                        success(source, "Assigned economy sign " + stableId + " to "
                                + displayName(source, target.playerId()) + ".");
                        return 1;
                    } catch (IllegalArgumentException | IllegalStateException exception) {
                        return fail(source, exception.getMessage());
                    }
                },
                permission("economy.sign.manage"));
    }

    private static int setWorth(CommandSourceStack source, String rawItem, String rawAmount) {
        Item item = item(rawItem);
        if (item == null) return fail(source, "That item id is unavailable.");
        final long amount;
        try {
            amount = EconomyMoney.parse(
                    rawAmount,
                    KernelServices.economyRepository().minorUnits(),
                    0L,
                    KernelServices.economy().settings().maximumTransaction(),
                    false);
        } catch (IllegalArgumentException exception) {
            return fail(source, exception.getMessage());
        }
        return execute(source, "sef:economy.worth.set",
                Map.of("item", itemId(item), "amount_minor", Long.toString(amount)), List.of(), () -> {
                    KernelServices.economyRepository().setWorth(itemId(item), amount);
                    success(source, amount == 0L
                            ? "Removed worth for " + itemId(item) + "."
                            : "Set worth for " + itemId(item) + " to "
                            + KernelServices.economy().format(amount) + ".");
                    return 1;
                }, permission("commands.setworth"));
    }

    private static boolean requiresPaymentConfirmation(UUID actor, long amount) {
        long threshold = KernelServices.economy().settings().confirmationThreshold();
        return threshold > 0L
                && amount >= threshold
                && KernelServices.economyRepository().preferences(actor).confirmLargePayments();
    }

    private static void rememberPayment(UUID actor, UUID target, long amount) {
        long now = System.currentTimeMillis();
        PENDING_PAYMENTS.entrySet().removeIf(entry -> entry.getValue().expiresAtEpochMillis() <= now);
        if (PENDING_PAYMENTS.size() >= MAXIMUM_CONFIRMATIONS && !PENDING_PAYMENTS.containsKey(actor)) {
            throw new IllegalStateException("Payment confirmation capacity is full");
        }
        PENDING_PAYMENTS.put(actor, new PendingPayment(target, amount, now + CONFIRMATION_LIFETIME_MILLIS));
    }

    private static boolean consumePayment(UUID actor, UUID target, long amount) {
        PendingPayment pending = PENDING_PAYMENTS.remove(actor);
        return pending != null
                && pending.expiresAtEpochMillis() > System.currentTimeMillis()
                && pending.targetId().equals(target)
                && pending.amount() == amount;
    }

    private static boolean requiresAdjustmentConfirmation(long amount) {
        long threshold = KernelServices.economy().settings().confirmationThreshold();
        return threshold > 0L
                && amount != Long.MIN_VALUE
                && Math.abs(amount) >= threshold;
    }

    private static void rememberAdjustment(UUID actor, String operation, UUID target, long amount) {
        long now = System.currentTimeMillis();
        PENDING_ADJUSTMENTS.entrySet().removeIf(
                entry -> entry.getValue().expiresAtEpochMillis() <= now);
        if (PENDING_ADJUSTMENTS.size() >= MAXIMUM_CONFIRMATIONS
                && !PENDING_ADJUSTMENTS.containsKey(actor)) {
            throw new IllegalStateException("Economy adjustment confirmation capacity is full");
        }
        PENDING_ADJUSTMENTS.put(
                actor,
                new PendingAdjustment(
                        operation,
                        target,
                        amount,
                        now + CONFIRMATION_LIFETIME_MILLIS));
    }

    private static boolean consumeAdjustment(UUID actor, String operation, UUID target, long amount) {
        PendingAdjustment pending = PENDING_ADJUSTMENTS.remove(actor);
        return pending != null
                && pending.expiresAtEpochMillis() > System.currentTimeMillis()
                && pending.operation().equals(operation)
                && pending.targetId().equals(target)
                && pending.amount() == amount;
    }

    private static ResolvedTarget resolve(CommandSourceStack source, String input) {
        ActionResult<IdentityService.Identity> result =
                KernelServices.identities().resolve(input, source.getPlayer());
        if (!result.successful() || result.value().playerId() == null) {
            return null;
        }
        ServerPlayer online = source.getServer().getPlayerList().getPlayer(result.value().playerId());
        return new ResolvedTarget(
                result.value().playerId(),
                result.value().authenticatedUsername(),
                online);
    }

    private static boolean eligible(CommandSourceStack source, ResolvedTarget target) {
        if (target.onlinePlayer() != null) {
            if (source.getPlayer() != null && VanishUtil.isVanished(target.onlinePlayer(), source.getPlayer())) {
                return false;
            }
            return PlayerTargetPolicy.decide(
                    source,
                    target.onlinePlayer(),
                    permission("economy.hierarchy.bypass"),
                    permission("economy.exempt"),
                    permission("economy.bypass.exempt"),
                    false,
                    true).allowed();
        }
        boolean exempt = PermissionsHandler.playerHasPermission(
                target.playerId(),
                permission("economy.exempt"));
        boolean exemptionAllowed = !exempt
                || PermissionService.isConsole(source)
                || has(source, "economy.bypass.exempt");
        boolean hierarchyAllowed = PermissionService.isConsole(source)
                || has(source, "economy.hierarchy.bypass");
        return exemptionAllowed && hierarchyAllowed;
    }

    private static boolean paymentAllowed(
            CommandSourceStack source,
            ServerPlayer sender,
            ResolvedTarget target
    ) {
        if (target.onlinePlayer() != null
                && VanishUtil.isVanished(target.onlinePlayer(), sender)) {
            return false;
        }
        return (KernelServices.economyRepository().preferences(target.playerId()).paymentsEnabled()
                || has(source, "economy.pay.bypass.toggle"))
                && (!KernelServices.social().ignores(target.playerId(), sender.getUUID())
                || has(source, "economy.pay.bypass.ignore"));
    }

    private static Item item(String input) {
        ResourceLocation id = ResourceLocation.tryParse(Objects.requireNonNullElse(input, ""));
        return id == null || !BuiltInRegistries.ITEM.containsKey(id)
                ? null
                : BuiltInRegistries.ITEM.get(id);
    }

    private static String itemId(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    private static long worth(Item item) {
        return KernelServices.economyRepository().worth(itemId(item)).orElse(0L);
    }

    private static int count(Inventory inventory, Item item) {
        int count = 0;
        ItemStack reference = new ItemStack(item);
        for (ItemStack stack : inventory.items) {
            if (ItemStack.isSameItemSameComponents(stack, reference)) {
                count = Math.addExact(count, stack.getCount());
            }
        }
        return count;
    }

    private static boolean remove(Inventory inventory, Item item, int amount, boolean mainHandOnly) {
        if (amount < 1) return false;
        ItemStack reference = new ItemStack(item);
        if (mainHandOnly) {
            ItemStack held = inventory.getSelected();
            if (!ItemStack.isSameItemSameComponents(held, reference) || held.getCount() < amount) {
                return false;
            }
            held.shrink(amount);
            return true;
        }
        if (count(inventory, item) < amount) {
            return false;
        }
        int remaining = amount;
        for (int slot = 0; slot < inventory.items.size() && remaining > 0; slot++) {
            ItemStack stack = inventory.items.get(slot);
            if (!ItemStack.isSameItemSameComponents(stack, reference)) continue;
            int removed = Math.min(remaining, stack.getCount());
            stack.shrink(removed);
            remaining -= removed;
        }
        return remaining == 0;
    }

    private static List<ItemStack> snapshot(Inventory inventory) {
        List<ItemStack> result = new ArrayList<>(inventory.getContainerSize());
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            result.add(inventory.getItem(slot).copy());
        }
        return result;
    }

    private static void restore(Inventory inventory, List<ItemStack> snapshot) {
        for (int slot = 0; slot < snapshot.size(); slot++) {
            inventory.setItem(slot, snapshot.get(slot).copy());
        }
        inventory.setChanged();
    }

    private static String displayName(CommandSourceStack source, UUID playerId) {
        ServerPlayer online = source.getServer().getPlayerList().getPlayer(playerId);
        if (online != null) {
            return online.getDisplayName().getString();
        }
        return KernelServices.profiles().find(playerId)
                .map(profile -> profile.nickname() == null || profile.nickname().isBlank()
                        ? profile.authenticatedUsername()
                        : profile.nickname())
                .orElse(playerId.toString());
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

    private static boolean active(String root) {
        return KernelServices.shortcuts().isActive(root);
    }

    private static boolean hasAnyEcoPermission(CommandSourceStack source) {
        return List.of("give", "take", "set", "reset", "freeze", "unfreeze", "history", "import")
                .stream()
                .anyMatch(action -> has(source, "commands.eco." + action))
                || has(source, "economy.sign.manage");
    }

    private static boolean has(CommandSourceStack source, String id) {
        PermissionNode<Boolean> node = permission(id);
        return node != null && PermissionService.has(source, node);
    }

    private static PermissionNode<Boolean> permission(String id) {
        return PermissionsHandler.phasePermission(id);
    }

    private static UUID actorId(CommandSourceStack source) {
        return source.getPlayer() == null ? CONSOLE_ACTOR : source.getPlayer().getUUID();
    }

    private static int unavailable(CommandSourceStack source) {
        return fail(source, "That player is unavailable.");
    }

    private static int fail(CommandSourceStack source, String message) {
        source.sendFailure(TextFormatter.stringToFormattedText(
                "&c" + Objects.requireNonNullElse(message, "The economy action failed.")));
        return 0;
    }

    private static void success(CommandSourceStack source, String message) {
        source.sendSuccess(() -> TextFormatter.stringToFormattedText("&a" + message), false);
    }

    private static void info(CommandSourceStack source, String message) {
        source.sendSuccess(() -> TextFormatter.stringToFormattedText("&7" + message), false);
    }

    private record PendingPayment(UUID targetId, long amount, long expiresAtEpochMillis) {
    }

    private record PendingAdjustment(
            String operation,
            UUID targetId,
            long amount,
            long expiresAtEpochMillis
    ) {
    }

    private record ResolvedTarget(UUID playerId, String username, ServerPlayer onlinePlayer) {
        boolean online() {
            return onlinePlayer != null;
        }
    }
}
