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
import com.enviouse.sef.kernel.KernelCommandExecutor;
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
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.audit.SecurityAuditService;

public final class VirtualWorkstationCommands {
    private VirtualWorkstationCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (ConfigHandler.config.enableCraftingTableCommand.get()) {
            if (KernelServices.shortcuts().isActive("craft")) {
                registerMenuCommand(dispatcher, "craft", PermissionsHandler.craftingTableCommand,
                        PermissionsHandler.craftingTableCooldownBypass,
                        "sef:workstation.craft",
                        VirtualWorkstationCommands::openCraftingTable);
            }
            if (ConfigHandler.config.enableCraftAlias.get()
                    && KernelServices.shortcuts().isActive("c")) {
                registerMenuCommand(dispatcher, "c", PermissionsHandler.craftingTableCommand,
                        PermissionsHandler.craftingTableCooldownBypass,
                        "sef:workstation.craft",
                        VirtualWorkstationCommands::openCraftingTable);
            }
        }

        if (ConfigHandler.config.enableAnvilCommand.get()) {
            if (KernelServices.shortcuts().isActive("anvil")) {
                registerMenuCommand(dispatcher, "anvil", PermissionsHandler.anvilCommand,
                        PermissionsHandler.anvilCooldownBypass,
                        "sef:workstation.anvil",
                        VirtualWorkstationCommands::openAnvil);
            }
            if (ConfigHandler.config.enableAnvilAlias.get()
                    && KernelServices.shortcuts().isActive("av")) {
                registerMenuCommand(dispatcher, "av", PermissionsHandler.anvilCommand,
                        PermissionsHandler.anvilCooldownBypass,
                        "sef:workstation.anvil",
                        VirtualWorkstationCommands::openAnvil);
            }
        }

        if (ConfigHandler.config.enableEnchantingTableCommand.get()) {
            if (KernelServices.shortcuts().isActive("enchantingtable")) {
                registerMenuCommand(dispatcher, "enchantingtable", PermissionsHandler.enchantingTableCommand,
                        PermissionsHandler.enchantingTableCooldownBypass,
                        "sef:workstation.enchant",
                        VirtualWorkstationCommands::openEnchantingTable);
            }
            if (ConfigHandler.config.enableEnchantingTableAlias.get()
                    && KernelServices.shortcuts().isActive("et")) {
                registerMenuCommand(dispatcher, "et", PermissionsHandler.enchantingTableCommand,
                        PermissionsHandler.enchantingTableCooldownBypass,
                        "sef:workstation.enchant",
                        VirtualWorkstationCommands::openEnchantingTable);
            }
        }

        if (ConfigHandler.config.enableSuperEnchantingTableCommand.get()) {
            registerSuperEnchantingCommand(dispatcher, "superenchantingtable");
            if (ConfigHandler.config.enableSuperEnchantingTableAlias.get()
                    && KernelServices.shortcuts().isActive("set")) {
                registerSuperEnchantingCommand(dispatcher, "set");
            }
        }

        if (ConfigHandler.config.enableRepairCommand.get()
                && KernelServices.shortcuts().isActive("repair")) {
            dispatcher.register(repairNode("repair"));
        }
        if (ConfigHandler.config.enableAdditionalWorkstations.get()) {
            registerAdditionalWorkstations(dispatcher);
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
        if (ConfigHandler.config.enableAdditionalWorkstations.get()) {
            attachAdditionalWorkstations(workstations);
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
                .requires(source -> source.getEntity() instanceof ServerPlayer
                        && hasPermission(source, permission))
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
                .requires(source -> source.getEntity() instanceof ServerPlayer
                        && hasPermission(source, PermissionsHandler.superEnchantingTableCommand))
                .executes(context -> {
                    ServerPlayer player = getPlayer(context.getSource());
                    if (player == null) {
                        return 0;
                    }
                    return executeKernelAction(
                            context.getSource(),
                            player,
                            PermissionsHandler.superEnchantingTableCommand,
                            PermissionsHandler.superEnchantingTableCooldownBypass,
                            "sef:workstation.super_enchant",
                            ignored -> {
                                if (!SuperEnchantingMenu.canOpen(player)) {
                                    player.sendSystemMessage(TextFormatter.stringToFormattedText(
                                            "&cHold an item in your main hand first."));
                                    return 0;
                                }
                                return openSuperEnchantingTable(player);
                            });
                });
    }

    private static int repair(CommandSourceStack source) {
        ServerPlayer player = getPlayer(source);
        if (player == null) {
            return 0;
        }

        return executeKernelAction(
                source,
                player,
                PermissionsHandler.repairCommand,
                PermissionsHandler.repairCooldownBypass,
                "sef:workstation.repair",
                ignored -> {
                    ItemStack held = player.getMainHandItem();
                    if (held.isEmpty()) {
                        player.sendSystemMessage(TextFormatter.stringToFormattedText(
                                ConfigHandler.config.repairNotHeldMessage.get()));
                        return 0;
                    }
                    if (!held.isDamageableItem() || held.getDamageValue() <= 0) {
                        player.sendSystemMessage(TextFormatter.stringToFormattedText(
                                ConfigHandler.config.repairNotNeededMessage.get()));
                        return 0;
                    }
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
                .requires(source -> source.getEntity() instanceof ServerPlayer
                        && hasPermission(source, PermissionsHandler.repairCommand))
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

    private static void registerAdditionalWorkstations(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (ConfigHandler.config.enableCartographyTableCommand.get()) {
            registerAdditional(dispatcher, "cartographytable", "cartographytable",
                    VirtualWorkstationCommands::openCartographyTable);
        }
        if (ConfigHandler.config.enableGrindstoneCommand.get()) {
            registerAdditional(dispatcher, "grindstone", "grindstone",
                    VirtualWorkstationCommands::openGrindstone);
        }
        if (ConfigHandler.config.enableLoomCommand.get()) {
            registerAdditional(dispatcher, "loom", "loom", VirtualWorkstationCommands::openLoom);
        }
        if (ConfigHandler.config.enableSmithingTableCommand.get()) {
            registerAdditional(dispatcher, "smithingtable", "smithingtable",
                    VirtualWorkstationCommands::openSmithingTable);
        }
        if (ConfigHandler.config.enableStonecutterCommand.get()) {
            registerAdditional(dispatcher, "stonecutter", "stonecutter",
                    VirtualWorkstationCommands::openStonecutter);
        }
        if (ConfigHandler.config.enableCraftingTableCommand.get()) {
            registerAdditional(dispatcher, "workbench", "workbench",
                    VirtualWorkstationCommands::openCraftingTable);
            registerAdditional(dispatcher, "wb", "workbench",
                    VirtualWorkstationCommands::openCraftingTable);
        }
    }

    private static void attachAdditionalWorkstations(
            LiteralArgumentBuilder<CommandSourceStack> workstations
    ) {
        if (ConfigHandler.config.enableCartographyTableCommand.get()) {
            workstations.then(additionalNode("cartographytable", "cartographytable",
                    VirtualWorkstationCommands::openCartographyTable));
        }
        if (ConfigHandler.config.enableGrindstoneCommand.get()) {
            workstations.then(additionalNode("grindstone", "grindstone",
                    VirtualWorkstationCommands::openGrindstone));
        }
        if (ConfigHandler.config.enableLoomCommand.get()) {
            workstations.then(additionalNode("loom", "loom", VirtualWorkstationCommands::openLoom));
        }
        if (ConfigHandler.config.enableSmithingTableCommand.get()) {
            workstations.then(additionalNode("smithingtable", "smithingtable",
                    VirtualWorkstationCommands::openSmithingTable));
        }
        if (ConfigHandler.config.enableStonecutterCommand.get()) {
            workstations.then(additionalNode("stonecutter", "stonecutter",
                    VirtualWorkstationCommands::openStonecutter));
        }
        if (ConfigHandler.config.enableCraftingTableCommand.get()) {
            workstations.then(additionalNode("workbench", "workbench",
                    VirtualWorkstationCommands::openCraftingTable));
        }
    }

    private static void registerAdditional(
            CommandDispatcher<CommandSourceStack> dispatcher,
            String literal,
            String permission,
            ToIntFunction<ServerPlayer> action
    ) {
        if (KernelServices.shortcuts().isActive(literal)) {
            dispatcher.register(additionalNode(literal, permission, action));
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> additionalNode(
            String literal,
            String permission,
            ToIntFunction<ServerPlayer> action
    ) {
        PermissionNode<Boolean> use = PermissionsHandler.phasePermission("commands." + permission);
        PermissionNode<Boolean> bypass =
                PermissionsHandler.phasePermission("commands.workstation.cooldown.bypass");
        return menuNode(literal, use, bypass, "sef:workstation." + permission, action);
    }

    private static int openCartographyTable(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new CartographyTableMenu(containerId, inventory),
                Component.translatable("container.cartography_table")));
        return 1;
    }

    private static int openGrindstone(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new GrindstoneMenu(containerId, inventory),
                Component.translatable("container.grindstone_title")));
        return 1;
    }

    private static int openLoom(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new LoomMenu(containerId, inventory),
                Component.translatable("container.loom")));
        return 1;
    }

    private static int openSmithingTable(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new SmithingMenu(containerId, inventory),
                Component.translatable("container.upgrade")));
        return 1;
    }

    private static int openStonecutter(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new StonecutterMenu(containerId, inventory),
                Component.translatable("container.stonecutter")));
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
        if (!KernelCommandExecutor.authorizeControl(source, actionId)) {
            return 0;
        }
        String dimension = player.serverLevel().dimension().location().toString();
        PermissionService.Decision permissionDecision = PermissionService.decide(player, permission);
        final String quotedCost;
        try {
            quotedCost = KernelServices.quoteCommandCost(actionId, Map.of(), List.of()).toPlainString();
        } catch (IllegalArgumentException exception) {
            source.sendFailure(TextFormatter.stringToFormattedText(
                    "&cThe configured command cost could not be calculated."));
            return 0;
        }
        ActionResult<CommandExecutionService.Lease> started = KernelServices.commandExecutions().begin(
                new CommandExecutionService.Request(
                        SecurityAuditService.currentSessionId(),
                        player.getUUID(),
                        player.getGameProfile().getName(),
                        actionId,
                        CommandDefinition.SourceType.PLAYER,
                        dimension,
                        dimension,
                        permissionDecision.granted(),
                        hasPlayerPermission(player, bypassPermission),
                        KernelServices.costBypass(source),
                        false,
                        "",
                        null,
                        null,
                        Set.of(),
                        Map.of(),
                        List.of(),
                        1L,
                        Map.of(
                                "permission_provider", permissionDecision.provider(),
                                "permission_default_use", permissionDecision.defaultUse().name(),
                                "quoted_cost", quotedCost),
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
