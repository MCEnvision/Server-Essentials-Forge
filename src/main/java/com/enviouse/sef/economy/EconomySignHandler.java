package com.enviouse.sef.economy;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.permissions.PermissionService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = ServerEssentialsForge.MODID)
public final class EconomySignHandler {
    private static final int MAXIMUM_PENDING_CLAIMS = 10_000;
    private static final Map<LocationKey, PlacementClaim> PLACEMENT_CLAIMS = new ConcurrentHashMap<>();

    private EconomySignHandler() {
    }

    @SubscribeEvent
    public static void onPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!enabled() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        BlockPos position = event.getPos();
        if (!(event.getLevel().getBlockEntity(position) instanceof SignBlockEntity)) {
            return;
        }
        if (!signStorageAvailable()) {
            return;
        }
        String dimensionId = player.level().dimension().location().toString();
        KernelServices.economySigns().removeAt(
                dimensionId,
                position.getX(),
                position.getY(),
                position.getZ());
        rememberPlacement(new LocationKey(dimensionId, position), player.getUUID());
    }

    @SubscribeEvent
    public static void onBroken(BlockEvent.BreakEvent event) {
        if (!enabled() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        removeAt(level, event.getPos());
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (!enabled() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        for (BlockPos position : event.getAffectedBlocks()) {
            removeAt(level, position);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!enabled()
                || event.isCanceled()
                || event.getHand() != InteractionHand.MAIN_HAND
                || !(event.getEntity() instanceof ServerPlayer player)
                || !(event.getLevel().getBlockEntity(event.getPos()) instanceof SignBlockEntity sign)) {
            return;
        }
        boolean front = sign.isFacingFrontText(player);
        EconomySignRepository.SignKey key = key(player.serverLevel(), event.getPos(), front);
        EconomySignParser.ParseResult parsed = EconomySignParser.parse(
                lines(sign.getText(front), player),
                ConfigHandler.config.economySignMaximumQuantity.get());
        if (parsed.status() == EconomySignParser.Status.NOT_ECONOMY) {
            if (signStorageAvailable()) {
                KernelServices.economySigns().remove(key);
            }
            return;
        }
        cancel(event);
        if (!signStorageAvailable()) {
            fail(player, "Economy sign storage is unavailable.");
            return;
        }
        if (!parsed.successful()) {
            fail(player, parsed.detail());
            return;
        }
        EconomySignParser.Definition definition = parsed.definition();
        if (!enabledTypes().contains(definition.type())) {
            fail(player, "That economy sign type is disabled.");
            return;
        }
        EconomySignRepository.SignRecord record = authorizeDefinition(player, key, definition);
        if (record == null || !has(player, usePermission(definition.type()))) {
            if (record != null) {
                fail(player, "You do not have permission to use this economy sign.");
            }
            return;
        }
        execute(player, record);
    }

    private static EconomySignRepository.SignRecord authorizeDefinition(
            ServerPlayer player,
            EconomySignRepository.SignKey key,
            EconomySignParser.Definition definition
    ) {
        EconomySignRepository repository = KernelServices.economySigns();
        EconomySignRepository.SignRecord existing = repository.find(key).orElse(null);
        if (existing != null && existing.fingerprint().equals(definition.fingerprint())) {
            return existing;
        }
        if (!has(player, createPermission(definition.type()))) {
            fail(player, existing == null
                    ? "You do not have permission to create this economy sign."
                    : "This economy sign changed and requires creator authorization.");
            return null;
        }
        UUID creatorId;
        if (existing != null) {
            if (!existing.creatorId().equals(player.getUUID())
                    && !has(player, permission("economy.sign.bypass.owner"))) {
                fail(player, "Only this economy sign's creator may authorize its edited text.");
                return null;
            }
            creatorId = existing.creatorId();
        } else {
            PlacementClaim claim = placementClaim(key);
            if (claim != null && !claim.creatorId().equals(player.getUUID())) {
                fail(player, "Only the player who placed this sign may authorize it.");
                return null;
            }
            consumePlacement(key);
            creatorId = claim == null ? player.getUUID() : claim.creatorId();
        }
        java.util.concurrent.atomic.AtomicReference<EconomySignRepository.SignRecord> authorized =
                new java.util.concurrent.atomic.AtomicReference<>();
        java.util.concurrent.atomic.AtomicReference<String> failure =
                new java.util.concurrent.atomic.AtomicReference<>("");
        int result = KernelCommandExecutor.execute(
                player.createCommandSourceStack(),
                "sef:economy.sign.create." + definition.type().id(),
                Map.of(
                        "sign", key.stableId(),
                        "type", definition.type().id(),
                        "edited", Boolean.toString(existing != null)),
                List.of(),
                false,
                () -> {
                    try {
                        authorized.set(repository.put(key, creatorId, definition));
                        return 1;
                    } catch (IllegalArgumentException | IllegalStateException exception) {
                        failure.set(message(exception));
                        return 0;
                    }
                },
                createPermission(definition.type()));
        if (result <= 0) {
            if (!failure.get().isBlank()) {
                fail(player, failure.get());
            }
            return null;
        }
        return authorized.get();
    }

    private static void execute(ServerPlayer player, EconomySignRepository.SignRecord record) {
        EconomySignParser.SignType type = record.type();
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("sign", record.key().stableId());
        parameters.put("type", type.id());
        parameters.put("revision", Long.toString(record.revision()));
        KernelCommandExecutor.execute(
                player.createCommandSourceStack(),
                "sef:economy.sign." + type.id(),
                parameters,
                List.of(player.getUUID()),
                false,
                () -> executeAuthorized(player, record),
                usePermission(type));
    }

    private static int executeAuthorized(ServerPlayer player, EconomySignRepository.SignRecord record) {
        try {
            return switch (record.type()) {
                case BALANCE -> balance(player);
                case BUY -> buy(player, record.arguments());
                case SELL -> sell(player, record.arguments());
                case TRADE -> trade(player, record.arguments());
                case FREE -> free(player, record.arguments());
                case DISPOSAL -> disposal(player);
                case KIT -> commandWithCharge(player, "kit " + record.arguments().getFirst(), 0L, "kit sign");
                case HEAL -> heal(player, amount(record.arguments().getFirst()));
                case REPAIR -> repair(player, amount(record.arguments().getFirst()));
                case TIME -> time(player, record.arguments().get(0), amount(record.arguments().get(1)));
                case WEATHER -> weather(player, record.arguments().get(0), amount(record.arguments().get(1)));
                case WARP -> commandWithCharge(
                        player,
                        "warp " + record.arguments().get(0),
                        amount(record.arguments().get(1)),
                        "warp sign");
            };
        } catch (IllegalArgumentException | IllegalStateException | ArithmeticException exception) {
            fail(player, message(exception));
            return 0;
        }
    }

    private static int balance(ServerPlayer player) {
        ActionResult<EconomyProvider.Account> result =
                KernelServices.economy().getOrCreate(player.getUUID(), player.getUUID());
        if (!result.successful()) {
            fail(player, result.detail());
            return 0;
        }
        success(player, "Balance, " + KernelServices.economy().format(result.value().balance()) + ".");
        return 1;
    }

    private static int buy(ServerPlayer player, List<String> arguments) {
        Item item = item(arguments.get(0));
        int quantity = Integer.parseInt(arguments.get(1));
        long value = amount(arguments.get(2));
        validateSignValue(value);
        java.util.concurrent.atomic.AtomicReference<Charge> reserved =
                new java.util.concurrent.atomic.AtomicReference<>();
        EconomyInventoryTransaction.Result transaction = EconomyInventoryTransaction.buy(
                player.getInventory(),
                item,
                quantity,
                () -> {
                    Charge charge = charge(player, value, "economy sign purchase");
                    reserved.set(charge);
                    return charge.successful();
                },
                () -> rollbackCharge(player, reserved.get()));
        if (!transaction.successful()) {
            Charge charge = reserved.get();
            fail(player, transaction.code() == EconomyInventoryTransaction.Code.INVENTORY_FULL
                    ? "Your inventory does not have enough space."
                    : charge != null && !charge.detail().isBlank()
                    ? charge.detail()
                    : "The purchase failed. Your balance and inventory were restored.");
            return 0;
        }
        sync(player);
        success(player, "Bought " + quantity + " " + itemId(item) + " for "
                + KernelServices.economy().format(value) + ".");
        return 1;
    }

    private static int sell(ServerPlayer player, List<String> arguments) {
        Item item = item(arguments.get(0));
        int quantity = Integer.parseInt(arguments.get(1));
        long value = amount(arguments.get(2));
        validateSignValue(value);
        EconomyProvider provider = KernelServices.economy().requireProvider();
        java.util.concurrent.atomic.AtomicReference<String> providerFailure =
                new java.util.concurrent.atomic.AtomicReference<>("");
        EconomyInventoryTransaction.Result transaction = EconomyInventoryTransaction.sell(
                player.getInventory(),
                item,
                quantity,
                () -> {
                    ActionResult<EconomyProvider.Transaction> credited = provider.deposit(
                            new EconomyProvider.MutationRequest(
                                    "sign.sell." + UUID.randomUUID(),
                                    player.getUUID(),
                                    player.getUUID(),
                                    "economy sign sale",
                                    provider.currency(),
                                    value,
                                    Map.of("item", itemId(item), "quantity", Integer.toString(quantity)),
                                    false));
                    if (!credited.successful()) {
                        providerFailure.set(credited.detail());
                    }
                    return credited.successful();
                });
        if (!transaction.successful()) {
            fail(player, transaction.code() == EconomyInventoryTransaction.Code.INSUFFICIENT_ITEMS
                    ? "You do not have enough matching component safe items."
                    : providerFailure.get() + ". Your inventory was restored.");
            return 0;
        }
        sync(player);
        success(player, "Sold " + quantity + " " + itemId(item) + " for "
                + KernelServices.economy().format(value) + ".");
        return 1;
    }

    private static int trade(ServerPlayer player, List<String> arguments) {
        Item offered = item(arguments.get(0));
        int offeredQuantity = Integer.parseInt(arguments.get(1));
        Item received = item(arguments.get(2));
        int receivedQuantity = Integer.parseInt(arguments.get(3));
        EconomyInventoryTransaction.Result transaction = EconomyInventoryTransaction.trade(
                player.getInventory(),
                offered,
                offeredQuantity,
                received,
                receivedQuantity);
        if (!transaction.successful()) {
            fail(player, transaction.code() == EconomyInventoryTransaction.Code.INSUFFICIENT_ITEMS
                    ? "You do not have enough matching trade items."
                    : "The trade could not fit. Your inventory was restored.");
            return 0;
        }
        sync(player);
        success(player, "Traded " + offeredQuantity + " " + itemId(offered)
                + " for " + receivedQuantity + " " + itemId(received) + ".");
        return 1;
    }

    private static int free(ServerPlayer player, List<String> arguments) {
        Item item = item(arguments.get(0));
        int quantity = Integer.parseInt(arguments.get(1));
        EconomyInventoryTransaction.Result transaction =
                EconomyInventoryTransaction.give(player.getInventory(), item, quantity);
        if (!transaction.successful()) {
            fail(player, "Your inventory does not have enough space.");
            return 0;
        }
        sync(player);
        success(player, "Received " + quantity + " " + itemId(item) + ".");
        return 1;
    }

    private static int disposal(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, ignored) ->
                        ChestMenu.threeRows(containerId, inventory, new net.minecraft.world.SimpleContainer(27)),
                Component.translatable("container.sef.disposal")));
        return 1;
    }

    private static int heal(ServerPlayer player, long price) {
        if (player.getHealth() >= player.getMaxHealth()
                && player.getFoodData().getFoodLevel() >= 20
                && player.getRemainingFireTicks() <= 0) {
            fail(player, "You do not need healing.");
            return 0;
        }
        Charge charge = charge(player, price, "economy sign heal");
        if (!charge.successful()) {
            fail(player, charge.detail());
            return 0;
        }
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(5.0F);
        player.clearFire();
        success(player, "You were healed for " + KernelServices.economy().format(price) + ".");
        return 1;
    }

    private static int repair(ServerPlayer player, long price) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty() || !held.isDamageableItem() || !held.isDamaged()) {
            fail(player, "Hold a damaged repairable item.");
            return 0;
        }
        Charge charge = charge(player, price, "economy sign repair");
        if (!charge.successful()) {
            fail(player, charge.detail());
            return 0;
        }
        held.setDamageValue(0);
        sync(player);
        success(player, "Your held item was repaired for " + KernelServices.economy().format(price) + ".");
        return 1;
    }

    private static int time(ServerPlayer player, String setting, long price) {
        Charge charge = charge(player, price, "economy sign time");
        if (!charge.successful()) {
            fail(player, charge.detail());
            return 0;
        }
        ServerLevel level = player.serverLevel();
        long day = level.getDayTime() - Math.floorMod(level.getDayTime(), 24_000L);
        level.setDayTime(day + (setting.equals("day") ? 1_000L : 13_000L));
        success(player, "Time changed to " + setting + ".");
        return 1;
    }

    private static int weather(ServerPlayer player, String setting, long price) {
        Charge charge = charge(player, price, "economy sign weather");
        if (!charge.successful()) {
            fail(player, charge.detail());
            return 0;
        }
        switch (setting) {
            case "clear" -> player.serverLevel().setWeatherParameters(12_000, 0, false, false);
            case "rain" -> player.serverLevel().setWeatherParameters(0, 12_000, true, false);
            case "thunder" -> player.serverLevel().setWeatherParameters(0, 12_000, true, true);
            default -> {
                rollbackCharge(player, charge);
                throw new IllegalArgumentException("Economy sign weather option is invalid");
            }
        }
        success(player, "Weather changed to " + setting + ".");
        return 1;
    }

    private static int commandWithCharge(ServerPlayer player, String command, long price, String reason) {
        Charge charge = charge(player, price, reason);
        if (!charge.successful()) {
            fail(player, charge.detail());
            return 0;
        }
        final int result;
        try {
            result = player.getServer().getCommands().getDispatcher()
                    .execute(command, player.createCommandSourceStack());
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) {
            rollbackCharge(player, charge);
            fail(player, exception.getRawMessage().getString() + ". Your balance was restored.");
            return 0;
        }
        if (result <= 0) {
            rollbackCharge(player, charge);
            fail(player, "The linked command failed. Your balance was restored.");
            return 0;
        }
        return result;
    }

    private static Charge charge(ServerPlayer player, long amount, String reason) {
        validateSignValue(amount);
        if (amount == 0L) {
            return Charge.free();
        }
        EconomyProvider provider = KernelServices.economy().requireProvider();
        ActionResult<EconomyProvider.Transaction> result = provider.withdraw(
                new EconomyProvider.MutationRequest(
                        "sign.charge." + UUID.randomUUID(),
                        player.getUUID(),
                        player.getUUID(),
                        reason,
                        provider.currency(),
                        amount,
                        Map.of("route", "economy_sign"),
                        false));
        return result.successful()
                ? Charge.paid(amount, result.value().transactionId())
                : Charge.failure(result.detail());
    }

    private static void rollbackCharge(ServerPlayer player, Charge charge) {
        if (!charge.successful() || charge.amount() == 0L) {
            return;
        }
        EconomyProvider provider = KernelServices.economy().requireProvider();
        ActionResult<EconomyProvider.Transaction> refund = provider.deposit(
                new EconomyProvider.MutationRequest(
                        "sign.refund." + charge.transactionId(),
                        player.getUUID(),
                        player.getUUID(),
                        "economy sign rollback",
                        provider.currency(),
                        charge.amount(),
                        Map.of("route", "economy_sign", "charge", charge.transactionId().toString()),
                        true));
        if (!refund.successful()) {
            ServerEssentialsForge.LOGGER.error(
                    "[SEF] Economy sign compensation failed for player {} and transaction {}. {}",
                    player.getUUID(),
                    charge.transactionId(),
                    refund.detail());
            throw new IllegalStateException("Economy sign compensation failed. Contact an administrator.");
        }
    }

    private static long amount(String input) {
        long parsed = EconomyMoney.parse(
                input,
                KernelServices.economy().requireProvider().minorUnits(),
                0L,
                ConfigHandler.config.economySignMaximumValue.get(),
                false);
        validateSignValue(parsed);
        return parsed;
    }

    private static void validateSignValue(long value) {
        if (value < 0L || value > ConfigHandler.config.economySignMaximumValue.get()) {
            throw new IllegalArgumentException("Economy sign value is outside configured bounds");
        }
    }

    private static Item item(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null || !BuiltInRegistries.ITEM.containsKey(location)) {
            throw new IllegalArgumentException("Economy sign item is unavailable");
        }
        return BuiltInRegistries.ITEM.get(location);
    }

    private static String itemId(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    private static void sync(ServerPlayer player) {
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }

    private static List<String> lines(SignText text, ServerPlayer player) {
        List<String> lines = new ArrayList<>(4);
        for (int index = 0; index < 4; index++) {
            lines.add(text.getMessage(index, player.isTextFilteringEnabled()).getString());
        }
        return lines;
    }

    private static EconomySignRepository.SignKey key(ServerLevel level, BlockPos position, boolean front) {
        return new EconomySignRepository.SignKey(
                level.dimension().location().toString(),
                position.getX(),
                position.getY(),
                position.getZ(),
                front);
    }

    private static void removeAt(ServerLevel level, BlockPos position) {
        if (!signStorageAvailable()) {
            return;
        }
        String dimensionId = level.dimension().location().toString();
        KernelServices.economySigns().removeAt(
                dimensionId,
                position.getX(),
                position.getY(),
                position.getZ());
        PLACEMENT_CLAIMS.remove(new LocationKey(dimensionId, position));
    }

    private static void rememberPlacement(LocationKey key, UUID creatorId) {
        long now = System.currentTimeMillis();
        PLACEMENT_CLAIMS.entrySet().removeIf(entry -> entry.getValue().expiresAtEpochMillis() <= now);
        if (PLACEMENT_CLAIMS.size() >= MAXIMUM_PENDING_CLAIMS && !PLACEMENT_CLAIMS.containsKey(key)) {
            return;
        }
        long lifetime = ConfigHandler.config.economySignClaimSeconds.get() * 1_000L;
        PLACEMENT_CLAIMS.put(key, new PlacementClaim(creatorId, Math.addExact(now, lifetime)));
    }

    private static PlacementClaim placementClaim(EconomySignRepository.SignKey signKey) {
        LocationKey key = new LocationKey(
                signKey.dimensionId(),
                new BlockPos(signKey.x(), signKey.y(), signKey.z()));
        PlacementClaim claim = PLACEMENT_CLAIMS.get(key);
        if (claim != null && claim.expiresAtEpochMillis() <= System.currentTimeMillis()) {
            PLACEMENT_CLAIMS.remove(key, claim);
            return null;
        }
        return claim;
    }

    private static void consumePlacement(EconomySignRepository.SignKey signKey) {
        PLACEMENT_CLAIMS.remove(new LocationKey(
                signKey.dimensionId(),
                new BlockPos(signKey.x(), signKey.y(), signKey.z())));
    }

    private static Set<EconomySignParser.SignType> enabledTypes() {
        java.util.EnumSet<EconomySignParser.SignType> result =
                java.util.EnumSet.noneOf(EconomySignParser.SignType.class);
        for (String entry : ConfigHandler.config.economyEnabledSignTypes.get().split(",")) {
            EconomySignParser.SignType type =
                    EconomySignParser.SignType.parse(entry.strip().toLowerCase(Locale.ROOT));
            if (type != null) {
                result.add(type);
            }
        }
        return Set.copyOf(result);
    }

    private static PermissionNode<Boolean> createPermission(EconomySignParser.SignType type) {
        return permission("economy.sign." + type.id() + ".create");
    }

    private static PermissionNode<Boolean> usePermission(EconomySignParser.SignType type) {
        return permission("economy.sign." + type.id() + ".use");
    }

    private static PermissionNode<Boolean> permission(String id) {
        return PermissionsHandler.phasePermission(id);
    }

    private static boolean has(ServerPlayer player, PermissionNode<Boolean> node) {
        return node != null && PermissionService.has(player.createCommandSourceStack(), node);
    }

    private static boolean enabled() {
        return KernelServices.economy().settings().enabled()
                && ConfigHandler.config.enableEconomySigns.get();
    }

    private static boolean signStorageAvailable() {
        return switch (KernelServices.economySigns().state()) {
            case MISSING, READY -> true;
            case NEW, RECOVERY, UNSUPPORTED, ERROR, CLOSED -> false;
        };
    }

    private static void cancel(PlayerInteractEvent.RightClickBlock event) {
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    private static String message(RuntimeException exception) {
        return exception.getMessage() == null || exception.getMessage().isBlank()
                ? exception.getClass().getSimpleName()
                : exception.getMessage();
    }

    private static void success(Player player, String message) {
        player.sendSystemMessage(TextFormatter.stringToFormattedText("&a" + message));
    }

    private static void fail(Player player, String message) {
        player.sendSystemMessage(TextFormatter.stringToFormattedText("&c" + message));
    }

    private record LocationKey(String dimensionId, BlockPos position) {
        private LocationKey {
            Objects.requireNonNull(dimensionId, "dimensionId");
            position = position.immutable();
        }
    }

    private record PlacementClaim(UUID creatorId, long expiresAtEpochMillis) {
    }

    private record Charge(boolean successful, long amount, UUID transactionId, String detail) {
        static Charge free() {
            return new Charge(true, 0L, new UUID(0L, 0L), "");
        }

        static Charge paid(long amount, UUID transactionId) {
            return new Charge(true, amount, transactionId, "");
        }

        static Charge failure(String detail) {
            return new Charge(false, 0L, new UUID(0L, 0L), Objects.requireNonNullElse(detail, ""));
        }
    }
}
