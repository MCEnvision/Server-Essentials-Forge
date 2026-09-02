package com.enviouse.sef.kits;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.identity.IdentityArguments;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.permissions.DynamicPermissionService;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.util.DurationParser;
import com.enviouse.sef.vanish.VanishUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class KitCommands {
    private KitCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (!ConfigHandler.config.enableKits.get()) {
            return;
        }
        dispatcher.register(Commands.literal("kit")
                .requires(source -> has(source, "commands.kit")
                        || has(source, "commands.kit.validate")
                        || has(source, "commands.kit.export")
                        || has(source, "commands.kit.edit"))
                .executes(context -> listForClaim(context.getSource()))
                .then(Commands.literal("validate")
                        .requires(source -> has(source, "commands.kit.validate"))
                        .executes(context -> validate(context.getSource(), null))
                        .then(kitArgument("kit")
                                .executes(context -> validate(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "kit")))))
                .then(Commands.literal("export")
                        .requires(source -> has(source, "commands.kit.export"))
                        .then(kitArgument("kit")
                                .executes(context -> export(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "kit")))))
                .then(Commands.literal("edit")
                        .requires(source -> has(source, "commands.kit.edit"))
                        .then(kitArgument("kit")
                                .then(Commands.literal("cooldown")
                                        .then(Commands.argument("duration", StringArgumentType.word())
                                                .executes(context -> editCooldown(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "kit"),
                                                        StringArgumentType.getString(context, "duration")))))
                                .then(Commands.literal("onetime")
                                        .then(Commands.argument("state", BoolArgumentType.bool())
                                                .executes(context -> editOneTime(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "kit"),
                                                        BoolArgumentType.getBool(context, "state")))))
                                .then(Commands.literal("permission")
                                        .then(Commands.argument("permission", StringArgumentType.word())
                                                .executes(context -> editPermission(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "kit"),
                                                        StringArgumentType.getString(context, "permission")))))
                                .then(Commands.literal("name")
                                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                                .executes(context -> editName(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "kit"),
                                                        StringArgumentType.getString(context, "name")))))))
                .then(kitArgument("kit")
                        .requires(source -> source.getPlayer() != null && has(source, "commands.kit"))
                        .executes(context -> claim(
                                context.getSource(),
                                StringArgumentType.getString(context, "kit")))));
        dispatcher.register(Commands.literal("kits")
                .requires(source -> source.getPlayer() != null && has(source, "commands.kits"))
                .executes(context -> list(context.getSource())));
        dispatcher.register(Commands.literal("showkit")
                .requires(source -> source.getPlayer() != null && has(source, "commands.showkit"))
                .then(kitArgument("kit")
                        .executes(context -> show(
                                context.getSource(),
                                StringArgumentType.getString(context, "kit")))));
        dispatcher.register(Commands.literal("createkit")
                .requires(source -> source.getPlayer() != null && has(source, "commands.createkit"))
                .then(Commands.argument("kit", StringArgumentType.word())
                        .executes(context -> create(
                                context.getSource(),
                                StringArgumentType.getString(context, "kit"),
                                "0"))
                        .then(Commands.argument("cooldown", StringArgumentType.word())
                                .executes(context -> create(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "kit"),
                                        StringArgumentType.getString(context, "cooldown"))))));
        dispatcher.register(Commands.literal("delkit")
                .requires(source -> has(source, "commands.delkit"))
                .then(kitArgument("kit")
                        .executes(context -> delete(
                                context.getSource(),
                                StringArgumentType.getString(context, "kit")))));
        dispatcher.register(Commands.literal("kitreset")
                .requires(source -> has(source, "commands.kitreset"))
                .then(IdentityArguments.known("player")
                        .then(kitArgument("kit")
                                .executes(context -> reset(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "player"),
                                        StringArgumentType.getString(context, "kit"))))));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> kitArgument(
            String name
    ) {
        return Commands.argument(name, StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                        KernelServices.kits().kits().stream().map(KitRepository.Kit::id),
                        builder));
    }

    private static int list(CommandSourceStack source) {
        return execute(source, "sef:kit.list", Map.of(), List.of(), () -> {
            ServerPlayer player = source.getPlayer();
            if (player == null) {
                return fail(source, "This command requires a player.");
            }
            List<KitRepository.Kit> kits = KernelServices.kits().kits().stream()
                    .filter(kit -> accessible(player, kit))
                    .toList();
            info(source, kits.isEmpty() ? "No kits are configured."
                    : "Available kits. " + String.join(", ", kits.stream()
                    .map(KitRepository.Kit::id).toList()));
            return Math.max(1, kits.size());
        }, permission("commands.kits"));
    }

    private static int listForClaim(CommandSourceStack source) {
        if (source.getPlayer() == null || !has(source, "commands.kit")) {
            return fail(source, "You do not have permission to use kits.");
        }
        return execute(source, "sef:kit.claim", Map.of("operation", "list"), List.of(), () -> {
            ServerPlayer player = source.getPlayer();
            List<KitRepository.Kit> kits = KernelServices.kits().kits().stream()
                    .filter(kit -> accessible(player, kit))
                    .toList();
            info(source, kits.isEmpty() ? "No kits are configured."
                    : "Available kits. " + String.join(", ", kits.stream()
                    .map(KitRepository.Kit::id).toList()));
            return Math.max(1, kits.size());
        }, permission("commands.kit"));
    }

    private static int show(CommandSourceStack source, String id) {
        Optional<KitRepository.Kit> found = find(source, id);
        if (found.isEmpty()) {
            return 0;
        }
        if (!accessible(source.getPlayer(), found.orElseThrow())) {
            return fail(source, "You do not have permission for that kit.");
        }
        return execute(source, "sef:kit.show", Map.of("kit", found.orElseThrow().id()), List.of(), () -> {
            KitRepository.Kit kit = found.orElseThrow();
            List<ItemStack> items = decode(source, kit);
            info(source, "Kit " + kit.id() + ", stacks " + items.size() + ", cooldown "
                    + kit.cooldownSeconds() + " seconds, one time " + kit.oneTime() + ".");
            for (ItemStack stack : items) {
                info(source, stack.getCount() + " x " + stack.getHoverName().getString());
            }
            return 1;
        }, permission("commands.showkit"));
    }

    private static int create(CommandSourceStack source, String id, String cooldownInput) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return fail(source, "This command requires a player.");
        }
        Duration cooldown;
        if (cooldownInput.equals("0")) {
            cooldown = Duration.ZERO;
        } else {
            DurationParser.Result parsed = DurationParser.parse(cooldownInput, false);
            if (!parsed.valid() || parsed.seconds() > 315_576_000L) {
                return fail(source, "Cooldown is invalid.");
            }
            cooldown = Duration.ofSeconds(parsed.seconds());
        }
        List<ItemStack> stacks = inventoryStacks(player);
        if (stacks.isEmpty()) {
            return fail(source, "Inventory has no items to store.");
        }
        if (stacks.size() > ConfigHandler.config.maximumKitItems.get()) {
            return fail(source, "Inventory exceeds the configured kit stack limit.");
        }
        List<String> encoded;
        try {
            encoded = stacks.stream()
                    .map(stack -> stack.save(player.registryAccess()).toString())
                    .toList();
        } catch (RuntimeException exception) {
            return fail(source, "Inventory contains an item that could not be encoded.");
        }
        if (encoded.stream().anyMatch(item -> item.length() > 65536)) {
            return fail(source, "Inventory contains an item with oversized component data.");
        }
        return execute(source, "sef:kit.create", Map.of(
                "kit", id,
                "stacks", Integer.toString(encoded.size()),
                "cooldown_seconds", Long.toString(cooldown.toSeconds())), List.of(), () -> {
            try {
                KernelServices.kits().put(id, encoded, cooldown, false, player.getUUID());
            } catch (IllegalArgumentException | IllegalStateException exception) {
                return fail(source, exception.getMessage());
            }
            success(source, "Kit " + id + " saved from " + encoded.size() + " stacks.");
            return 1;
        }, permission("commands.createkit"));
    }

    private static int claim(CommandSourceStack source, String id) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return fail(source, "This command requires a player.");
        }
        Optional<KitRepository.Kit> found = find(source, id);
        if (found.isEmpty()) {
            return 0;
        }
        KitRepository.Kit kit = found.orElseThrow();
        if (!accessible(player, kit)) {
            return fail(source, "You do not have permission for that kit.");
        }
        KitRepository.Availability availability =
                KernelServices.kits().availability(player.getUUID(), kit, Instant.now());
        if (!availability.available()) {
            return fail(source, availability.alreadyClaimed()
                    ? "That one time kit was already claimed."
                    : "That kit is on cooldown until " + availability.nextUseAt() + ".");
        }
        List<ItemStack> items = decode(source, kit);
        if (items.size() != kit.items().size()) {
            return fail(source, "The kit contains missing or invalid item registry data.");
        }
        boolean dropOverflow = ConfigHandler.config.kitDropOverflow.get();
        if (!dropOverflow && !canFit(player, items)) {
            return fail(source, "Inventory does not have enough space for this kit.");
        }
        int result = execute(source, "sef:kit.claim", Map.of(
                "kit", kit.id(),
                "stacks", Integer.toString(items.size())), List.of(player.getUUID()), () -> {
            List<ItemStack> before = snapshot(player.getInventory());
            List<ItemEntity> dropped = new ArrayList<>();
            try {
                for (ItemStack item : items) {
                    ItemStack grant = item.copy();
                    if (!player.getInventory().add(grant) || !grant.isEmpty()) {
                        if (!dropOverflow || grant.isEmpty()) {
                            rollback(player, before, dropped);
                            return fail(source, "Inventory changed before the kit could be committed.");
                        }
                        ItemEntity entity = player.drop(grant.copy(), false);
                        if (entity == null) {
                            rollback(player, before, dropped);
                            return fail(source, "Kit overflow could not be placed safely.");
                        }
                        dropped.add(entity);
                    }
                }
                player.getInventory().setChanged();
                player.containerMenu.broadcastChanges();
                KernelServices.kits().recordUse(player.getUUID(), kit, Instant.now());
                return 1;
            } catch (RuntimeException exception) {
                try {
                    rollback(player, before, dropped);
                } catch (RuntimeException rollbackFailure) {
                    exception.addSuppressed(rollbackFailure);
                }
                throw exception;
            }
        }, permission("commands.kit"));
        if (result > 0) {
            success(source, "Claimed kit " + kit.id() + ".");
        }
        return result;
    }

    private static int delete(CommandSourceStack source, String id) {
        return execute(source, "sef:kit.delete", Map.of("kit", id), List.of(), () -> {
            try {
                if (!KernelServices.kits().delete(id)) {
                    return fail(source, "Kit not found.");
                }
            } catch (IllegalArgumentException | IllegalStateException exception) {
                return fail(source, exception.getMessage());
            }
            success(source, "Kit " + id + " deleted.");
            return 1;
        }, permission("commands.delkit"));
    }

    private static int editCooldown(CommandSourceStack source, String id, String durationInput) {
        long seconds;
        if (durationInput.equals("0")) {
            seconds = 0L;
        } else {
            DurationParser.Result parsed = DurationParser.parse(durationInput, false);
            if (!parsed.valid() || parsed.seconds() > 315_576_000L) {
                return fail(source, "Cooldown is invalid.");
            }
            seconds = parsed.seconds();
        }
        return edit(source, id, seconds, null, null, null, "cooldown");
    }

    private static int editOneTime(CommandSourceStack source, String id, boolean state) {
        return edit(source, id, null, state, null, null, "one time");
    }

    private static int editPermission(CommandSourceStack source, String id, String permissionId) {
        return edit(source, id, null, null, permissionId, null, "permission");
    }

    private static int editName(CommandSourceStack source, String id, String displayName) {
        return edit(source, id, null, null, null, displayName, "display name");
    }

    private static int edit(
            CommandSourceStack source,
            String id,
            Long cooldown,
            Boolean oneTime,
            String permissionId,
            String displayName,
            String field
    ) {
        return execute(source, "sef:kit.edit", Map.of("kit", id, "field", field), List.of(), () -> {
            try {
                KitRepository.Kit updated = KernelServices.kits().updatePolicy(
                        id,
                        cooldown,
                        oneTime,
                        permissionId,
                        displayName);
                success(source, "Kit " + updated.id() + " " + field + " updated.");
                return 1;
            } catch (IllegalArgumentException | IllegalStateException exception) {
                return fail(source, exception.getMessage());
            }
        }, permission("commands.kit.edit"));
    }

    private static int reset(CommandSourceStack source, String playerInput, String kitId) {
        ActionResult<com.enviouse.sef.identity.IdentityService.Identity> identity =
                KernelServices.identities().resolve(playerInput, source.getPlayer(), PermissionService.isConsole(source));
        if (!identity.successful() || identity.value().playerId() == null) {
            return fail(source, "That player is unavailable.");
        }
        UUID target = identity.value().playerId();
        ServerPlayer online = source.getServer().getPlayerList().getPlayer(target);
        if ((online != null && !eligible(source, online))
                || (online == null && source.getPlayer() != null
                && !has(source, "kits.hierarchy.bypass"))) {
            return fail(source, "That player is unavailable.");
        }
        return execute(source, "sef:kit.reset", Map.of("kit", kitId, "target", target.toString()),
                List.of(target), () -> {
                    try {
                        if (!KernelServices.kits().reset(target, kitId)) {
                            return fail(source, "No kit use record changed.");
                        }
                    } catch (IllegalArgumentException | IllegalStateException exception) {
                        return fail(source, exception.getMessage());
                    }
                    success(source, "Kit use record reset.");
                    return 1;
                }, permission("commands.kitreset"));
    }

    private static boolean eligible(CommandSourceStack source, ServerPlayer target) {
        if (source.getPlayer() != null && VanishUtil.isVanished(target, source.getPlayer())) {
            return false;
        }
        return PlayerTargetPolicy.decide(
                source,
                target,
                permission("kits.hierarchy.bypass"),
                permission("kits.exempt"),
                permission("kits.bypass.exempt"),
                false,
                true).allowed();
    }

    private static int validate(CommandSourceStack source, String id) {
        return execute(source, "sef:kit.validate", Map.of("kit", id == null ? "all" : id), List.of(), () -> {
            if (id != null) {
                Optional<KitRepository.Kit> found = find(source, id);
                if (found.isEmpty()) {
                    return 0;
                }
                List<ItemStack> decoded = decode(source, found.orElseThrow());
                boolean valid = decoded.size() == found.orElseThrow().items().size();
                info(source, "Kit " + id + " validation " + (valid ? "passed." : "failed."));
                return valid ? 1 : 0;
            }
            KitRepository.Validation validation = KernelServices.kits().validateAll();
            info(source, "Kits " + validation.kits() + ", invalid definitions "
                    + validation.invalidKits() + ", use records " + validation.useRecords() + ".");
            return validation.invalidKits() == 0 ? 1 : 0;
        }, permission("commands.kit.validate"));
    }

    private static int export(CommandSourceStack source, String id) {
        Optional<KitRepository.Kit> found = find(source, id);
        if (found.isEmpty()) {
            return 0;
        }
        return execute(source, "sef:kit.export", Map.of("kit", id, "operation", "export"), List.of(), () -> {
            KitRepository.Kit kit = found.orElseThrow();
            info(source, "Kit export metadata. id " + kit.id() + ", revision " + kit.revision()
                    + ", stacks " + kit.items().size() + ", cooldown " + kit.cooldownSeconds() + ".");
            return 1;
        }, permission("commands.kit.export"));
    }

    private static Optional<KitRepository.Kit> find(CommandSourceStack source, String id) {
        try {
            Optional<KitRepository.Kit> found = KernelServices.kits().kit(id);
            if (found.isEmpty()) {
                fail(source, "Kit not found.");
            }
            return found;
        } catch (IllegalArgumentException exception) {
            fail(source, exception.getMessage());
            return Optional.empty();
        }
    }

    private static boolean accessible(ServerPlayer player, KitRepository.Kit kit) {
        return !ConfigHandler.config.kitRequirePerKitPermission.get()
                || DynamicPermissionService.has(player, kit.permission());
    }

    private static List<ItemStack> decode(CommandSourceStack source, KitRepository.Kit kit) {
        List<ItemStack> items = new ArrayList<>();
        for (String encoded : kit.items()) {
            try {
                ItemStack stack = ItemStack.parseOptional(
                        source.registryAccess(),
                        TagParser.parseTag(encoded));
                if (stack.isEmpty() || stack.getCount() < 1 || stack.getCount() > stack.getMaxStackSize()) {
                    return List.of();
                }
                items.add(stack);
            } catch (RuntimeException | com.mojang.brigadier.exceptions.CommandSyntaxException exception) {
                return List.of();
            }
        }
        return List.copyOf(items);
    }

    private static List<ItemStack> inventoryStacks(ServerPlayer player) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (!item.isEmpty()) {
                stacks.add(item.copy());
            }
        }
        return List.copyOf(stacks);
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

    private static void discard(List<ItemEntity> dropped) {
        dropped.forEach(ItemEntity::discard);
    }

    private static void rollback(
            ServerPlayer player,
            List<ItemStack> snapshot,
            List<ItemEntity> dropped
    ) {
        discard(dropped);
        restore(player.getInventory(), snapshot);
        player.containerMenu.sendAllDataToRemote();
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
}
