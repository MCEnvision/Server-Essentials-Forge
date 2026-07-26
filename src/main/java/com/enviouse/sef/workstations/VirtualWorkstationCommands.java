package com.enviouse.sef.workstations;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.ToIntFunction;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.kernel.policy.CommandExecutionService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import com.enviouse.sef.permissions.PermissionService;

public final class VirtualWorkstationCommands {
    private VirtualWorkstationCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (ConfigHandler.config.enableCraftingTableCommand.get()) {
            registerMenuCommand(dispatcher, "craft", PermissionsHandler.craftingTableCommand,
                    PermissionsHandler.craftingTableCooldownBypass,
                    "sef:workstation.craft",
                    VirtualWorkstationCommands::openCraftingTable);
            if (ConfigHandler.config.enableCraftAlias.get()) {
                registerMenuCommand(dispatcher, "c", PermissionsHandler.craftingTableCommand,
                        PermissionsHandler.craftingTableCooldownBypass,
                        "sef:workstation.craft",
                        VirtualWorkstationCommands::openCraftingTable);
            }
        }

        if (ConfigHandler.config.enableAnvilCommand.get()) {
            registerMenuCommand(dispatcher, "anvil", PermissionsHandler.anvilCommand,
                    PermissionsHandler.anvilCooldownBypass,
                    "sef:workstation.anvil",
                    VirtualWorkstationCommands::openAnvil);
            if (ConfigHandler.config.enableAnvilAlias.get()) {
                registerMenuCommand(dispatcher, "av", PermissionsHandler.anvilCommand,
                        PermissionsHandler.anvilCooldownBypass,
                        "sef:workstation.anvil",
                        VirtualWorkstationCommands::openAnvil);
            }
        }

        if (ConfigHandler.config.enableEnchantingTableCommand.get()) {
            registerMenuCommand(dispatcher, "enchantingtable", PermissionsHandler.enchantingTableCommand,
                    PermissionsHandler.enchantingTableCooldownBypass,
                    "sef:workstation.enchant",
                    VirtualWorkstationCommands::openEnchantingTable);
            if (ConfigHandler.config.enableEnchantingTableAlias.get()) {
                registerMenuCommand(dispatcher, "et", PermissionsHandler.enchantingTableCommand,
                        PermissionsHandler.enchantingTableCooldownBypass,
                        "sef:workstation.enchant",
                        VirtualWorkstationCommands::openEnchantingTable);
            }
        }

        if (ConfigHandler.config.enableSuperEnchantingTableCommand.get()) {
            registerSuperEnchantingCommand(dispatcher, "superenchantingtable");
            if (ConfigHandler.config.enableSuperEnchantingTableAlias.get()) {
                registerSuperEnchantingCommand(dispatcher, "set");
            }
        }

        if (ConfigHandler.config.enableRepairCommand.get()) {
            dispatcher.register(repairNode("repair"));
        }
    }

    public static void attachCanonical(LiteralArgumentBuilder<CommandSourceStack> sefRoot) {
        LiteralArgumentBuilder<CommandSourceStack> workstations = Commands.literal("workstation");
        if (ConfigHandler.config.enableCraftingTableCommand.get()) {
            workstations.then(menuNode(
                    "craft",
                    PermissionsHandler.craftingTableCommand,
                    PermissionsHandler.craftingTableCooldownBypass,
                    "sef:workstation.craft",
                    VirtualWorkstationCommands::openCraftingTable));
        }
        if (ConfigHandler.config.enableAnvilCommand.get()) {
            workstations.then(menuNode(
                    "anvil",
                    PermissionsHandler.anvilCommand,
                    PermissionsHandler.anvilCooldownBypass,
                    "sef:workstation.anvil",
                    VirtualWorkstationCommands::openAnvil));
        }
        if (ConfigHandler.config.enableEnchantingTableCommand.get()) {
            workstations.then(menuNode(
                    "enchant",
                    PermissionsHandler.enchantingTableCommand,
                    PermissionsHandler.enchantingTableCooldownBypass,
                    "sef:workstation.enchant",
                    VirtualWorkstationCommands::openEnchantingTable));
        }
        if (ConfigHandler.config.enableSuperEnchantingTableCommand.get()) {
            workstations.then(superEnchantingNode("super_enchant"));
        }
        if (ConfigHandler.config.enableRepairCommand.get()) {
            workstations.then(repairNode("repair"));
        }
        sefRoot.then(workstations);
    }

    public static void clearCooldowns() {
        KernelServices.cooldowns().clearAll();
    }

    private static void registerMenuCommand(
            CommandDispatcher<CommandSourceStack> dispatcher,
            String literal,
            PermissionNode<Boolean> permission,
            PermissionNode<Boolean> bypassPermission,
            String actionId,
            ToIntFunction<ServerPlayer> action) {
        dispatcher.register(menuNode(literal, permission, bypassPermission, actionId, action));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> menuNode(
            String literal,
            PermissionNode<Boolean> permission,
            PermissionNode<Boolean> bypassPermission,
            String actionId,
            ToIntFunction<ServerPlayer> action) {
        return Commands.literal(literal)
                .requires(source -> hasPermission(source, permission))
                .executes(context -> {
                    ServerPlayer player = getPlayer(context.getSource());
                    if (player == null) {
                        return 0;
                    }
                    return executeKernelAction(
                            context.getSource(),
                            player,
                            permission,
                            bypassPermission,
                            actionId,
                            action);
                });
    }

    private static void registerSuperEnchantingCommand(CommandDispatcher<CommandSourceStack> dispatcher, String literal) {
        dispatcher.register(superEnchantingNode(literal));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> superEnchantingNode(String literal) {
        return Commands.literal(literal)
                .requires(source -> hasPermission(source, PermissionsHandler.superEnchantingTableCommand))
                .executes(context -> {
                    ServerPlayer player = getPlayer(context.getSource());
                    if (player == null) {
                        return 0;
                    }
                    if (!SuperEnchantingMenu.canOpen(player)) {
                        player.sendSystemMessage(TextFormatter.stringToFormattedText(
                                "&cHold one enchantable item in your main hand first."));
                        return 0;
                    }
                    return executeKernelAction(
                            context.getSource(),
                            player,
                            PermissionsHandler.superEnchantingTableCommand,
                            PermissionsHandler.superEnchantingTableCooldownBypass,
                            "sef:workstation.super_enchant",
                            VirtualWorkstationCommands::openSuperEnchantingTable);
                });
    }

    private static int repair(CommandSourceStack source) {
        ServerPlayer player = getPlayer(source);
        if (player == null) {
            return 0;
        }

        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            player.sendSystemMessage(TextFormatter.stringToFormattedText(ConfigHandler.config.repairNotHeldMessage.get()));
            return 0;
        }
        if (!held.isDamageableItem() || held.getDamageValue() <= 0) {
            player.sendSystemMessage(TextFormatter.stringToFormattedText(ConfigHandler.config.repairNotNeededMessage.get()));
            return 0;
        }
        return executeKernelAction(
                source,
                player,
                PermissionsHandler.repairCommand,
                PermissionsHandler.repairCooldownBypass,
                "sef:workstation.repair",
                ignored -> {
                    String itemName = held.getHoverName().getString();
                    held.setDamageValue(0);
                    player.getInventory().setChanged();
                    player.containerMenu.broadcastChanges();
                    player.sendSystemMessage(TextFormatter.stringToFormattedText(
                            ConfigHandler.config.repairSuccessMessage.get().replace("$item", itemName)));
                    return 1;
                });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> repairNode(String literal) {
        return Commands.literal(literal)
                .requires(source -> hasPermission(source, PermissionsHandler.repairCommand))
                .executes(context -> repair(context.getSource()));
    }

    private static int openCraftingTable(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new VirtualCraftingMenu(containerId, inventory),
                Component.translatable("container.crafting")));
        return 1;
    }

    private static int openAnvil(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new AnvilMenu(containerId, inventory),
                Component.translatable("container.repair")));
        return 1;
    }

    private static int openEnchantingTable(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new VirtualEnchantmentMenu(containerId, inventory),
                Component.translatable("container.enchant")));
        return 1;
    }

    private static int openSuperEnchantingTable(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new SuperEnchantingMenu(containerId, inventory, player),
                Component.literal("super enchanting table")));
        return 1;
    }

    private static int executeKernelAction(
            CommandSourceStack source,
            ServerPlayer player,
            PermissionNode<Boolean> permission,
            PermissionNode<Boolean> bypassPermission,
            String actionId,
            ToIntFunction<ServerPlayer> action
    ) {
        String dimension = player.serverLevel().dimension().location().toString();
        ActionResult<CommandExecutionService.Lease> started = KernelServices.commandExecutions().begin(
                new CommandExecutionService.Request(
                        UUID.randomUUID(),
                        player.getUUID(),
                        player.getGameProfile().getName(),
                        actionId,
                        CommandDefinition.SourceType.PLAYER,
                        dimension,
                        dimension,
                        hasPlayerPermission(player, permission),
                        hasPlayerPermission(player, bypassPermission),
                        "",
                        null,
                        null,
                        Set.of(),
                        Map.of(),
                        List.of(),
                        "command"));
        if (!started.successful()) {
            if (started.reason() == ActionResult.ReasonCode.COOLDOWN_ACTIVE) {
                player.sendSystemMessage(TextFormatter.stringToFormattedText(
                        ConfigHandler.config.workstationCooldownMessage.get()
                                .replace("$seconds", started.detail())));
            } else if (started.reason() == ActionResult.ReasonCode.FEATURE_DISABLED) {
                source.sendFailure(TextFormatter.stringToFormattedText("&cThat feature is currently disabled."));
            } else if (started.reason() == ActionResult.ReasonCode.PERMISSION_DENIED) {
                source.sendFailure(TextFormatter.stringToFormattedText("&cYou do not have permission to use that command."));
            } else {
                source.sendFailure(TextFormatter.stringToFormattedText(
                        "&cThat action is unavailable. &7" + started.reason().name().toLowerCase(java.util.Locale.ROOT)));
            }
            return 0;
        }

        try (CommandExecutionService.Lease lease = started.value()) {
            int result = action.applyAsInt(player);
            ActionResult<Void> completed = lease.complete(
                    result > 0,
                    result > 0 ? null : ActionResult.ReasonCode.PROVIDER_ERROR);
            return completed.successful() ? result : 0;
        } catch (RuntimeException exception) {
            throw exception;
        }
    }

    private static boolean hasPermission(CommandSourceStack source, PermissionNode<Boolean> permission) {
        return PermissionService.has(source, permission);
    }

    private static boolean hasPlayerPermission(ServerPlayer player, PermissionNode<Boolean> permission) {
        return PermissionService.has(player, permission);
    }

    private static ServerPlayer getPlayer(CommandSourceStack source) {
        try {
            return source.getPlayerOrException();
        } catch (Exception exception) {
            source.sendFailure(TextFormatter.stringToFormattedText("&cThis command can only be used by a player."));
            return null;
        }
    }
}
