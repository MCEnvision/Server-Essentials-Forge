package com.enviouse.sef.workstations;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.audit.SecurityAuditService;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.ConfirmationService;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.permissions.PermissionService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

public final class AdministrativeEnchantCommands {
    public static final int IMPLEMENTATION_MAXIMUM_LEVEL = 1_000_000;

    private AdministrativeEnchantCommands() {
    }

    public static void register(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext buildContext
    ) {
        Objects.requireNonNull(dispatcher, "dispatcher");
        Objects.requireNonNull(buildContext, "buildContext");
        dispatcher.register(Commands.literal("sef").then(root("enchant", buildContext)));
    }

    public static void registerVanillaRoot(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext buildContext
    ) {
        Objects.requireNonNull(dispatcher, "dispatcher");
        Objects.requireNonNull(buildContext, "buildContext");
        dispatcher.register(root("enchant", buildContext));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> root(
            String literal,
            CommandBuildContext buildContext
    ) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(literal)
                .requires(source -> ConfigHandler.config.enableAdministrativeEnchanting.get()
                        && KernelCommandExecutor.canUse(source, "sef:enchant.apply"));
        root.then(applyTargets(buildContext));
        root.then(applySelf(buildContext));
        root.then(Commands.literal("self").then(applySelfEnchantment(buildContext)));
        root.then(removeNode(buildContext));
        root.then(clearNode());
        return root;
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, ?> applyTargets(
            CommandBuildContext buildContext
    ) {
        return Commands.argument("targets", EntityArgument.players())
                .then(Commands.argument(
                                "enchantment",
                                ResourceArgument.resource(buildContext, Registries.ENCHANTMENT))
                        .executes(context -> apply(
                                context.getSource(),
                                EntityArgument.getPlayers(context, "targets"),
                                ResourceArgument.getEnchantment(context, "enchantment"),
                                1))
                        .then(Commands.argument(
                                        "level",
                                        IntegerArgumentType.integer(1, IMPLEMENTATION_MAXIMUM_LEVEL))
                                .executes(context -> apply(
                                        context.getSource(),
                                        EntityArgument.getPlayers(context, "targets"),
                                        ResourceArgument.getEnchantment(context, "enchantment"),
                                        IntegerArgumentType.getInteger(context, "level")))));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, ?> applySelf(
            CommandBuildContext buildContext
    ) {
        return Commands.argument(
                        "self_enchantment",
                        ResourceArgument.resource(buildContext, Registries.ENCHANTMENT))
                .requires(source -> source.getPlayer() != null && has(source, "commands.enchant.self"))
                .executes(context -> apply(
                        context.getSource(),
                        List.of(context.getSource().getPlayerOrException()),
                        ResourceArgument.getEnchantment(context, "self_enchantment"),
                        1))
                .then(Commands.argument(
                                "self_level",
                                IntegerArgumentType.integer(1, IMPLEMENTATION_MAXIMUM_LEVEL))
                        .executes(context -> apply(
                                context.getSource(),
                                List.of(context.getSource().getPlayerOrException()),
                                ResourceArgument.getEnchantment(context, "self_enchantment"),
                                IntegerArgumentType.getInteger(context, "self_level"))));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, ?> applySelfEnchantment(
            CommandBuildContext buildContext
    ) {
        return Commands.argument(
                        "enchantment",
                        ResourceArgument.resource(buildContext, Registries.ENCHANTMENT))
                .requires(source -> source.getPlayer() != null && has(source, "commands.enchant.self"))
                .executes(context -> apply(
                        context.getSource(),
                        List.of(context.getSource().getPlayerOrException()),
                        ResourceArgument.getEnchantment(context, "enchantment"),
                        1))
                .then(Commands.argument(
                                "level",
                                IntegerArgumentType.integer(1, IMPLEMENTATION_MAXIMUM_LEVEL))
                        .executes(context -> apply(
                                context.getSource(),
                                List.of(context.getSource().getPlayerOrException()),
                                ResourceArgument.getEnchantment(context, "enchantment"),
                                IntegerArgumentType.getInteger(context, "level"))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> removeNode(CommandBuildContext buildContext) {
        return Commands.literal("remove")
                .requires(source -> KernelCommandExecutor.canUse(source, "sef:enchant.remove"))
                .then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.argument(
                                        "enchantment",
                                        ResourceArgument.resource(buildContext, Registries.ENCHANTMENT))
                                .executes(context -> previewRemove(
                                        context.getSource(),
                                        EntityArgument.getPlayers(context, "targets"),
                                        ResourceArgument.getEnchantment(context, "enchantment")))
                                .then(Commands.literal("confirm")
                                        .then(Commands.argument("token", StringArgumentType.word())
                                                .executes(context -> remove(
                                                        context.getSource(),
                                                        EntityArgument.getPlayers(context, "targets"),
                                                        ResourceArgument.getEnchantment(context, "enchantment"),
                                                        StringArgumentType.getString(context, "token")))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> clearNode() {
        return Commands.literal("clear")
                .requires(source -> KernelCommandExecutor.canUse(source, "sef:enchant.clear"))
                .then(Commands.argument("targets", EntityArgument.players())
                        .executes(context -> previewClear(
                                context.getSource(),
                                EntityArgument.getPlayers(context, "targets")))
                        .then(Commands.literal("confirm")
                                .then(Commands.argument("token", StringArgumentType.word())
                                        .executes(context -> clear(
                                                context.getSource(),
                                                EntityArgument.getPlayers(context, "targets"),
                                                StringArgumentType.getString(context, "token"))))));
    }

    private static int apply(
            CommandSourceStack source,
            Collection<ServerPlayer> targets,
            Holder<Enchantment> enchantment,
            int level
    ) {
        ActionResult<List<Plan>> admission = plan(source, targets, enchantment, level, Operation.APPLY);
        if (!admission.successful()) {
            return fail(source, admission.detail());
        }
        List<Plan> plans = admission.value();
        Map<String, String> parameters = parameters(plans, enchantment, level, Operation.APPLY);
        return KernelCommandExecutor.execute(
                source,
                "sef:enchant.apply",
                parameters,
                targetIds(plans),
                false,
                () -> execute(source, plans, enchantment, level, Operation.APPLY));
    }

    private static int remove(
            CommandSourceStack source,
            Collection<ServerPlayer> targets,
            Holder<Enchantment> enchantment,
            String token
    ) {
        ActionResult<List<Plan>> admission = plan(source, targets, enchantment, 0, Operation.REMOVE);
        if (!admission.successful()) {
            return fail(source, admission.detail());
        }
        List<Plan> plans = admission.value();
        Map<String, String> parameters = parameters(plans, enchantment, 0, Operation.REMOVE);
        if (!consumeConfirmation(source, "sef:enchant.remove", parameters, targetIds(plans), token)) {
            return 0;
        }
        return KernelCommandExecutor.execute(
                source,
                "sef:enchant.remove",
                parameters,
                targetIds(plans),
                false,
                () -> execute(source, plans, enchantment, 0, Operation.REMOVE));
    }

    private static int clear(
            CommandSourceStack source,
            Collection<ServerPlayer> targets,
            String token
    ) {
        ActionResult<List<Plan>> admission = plan(source, targets, null, 0, Operation.CLEAR);
        if (!admission.successful()) {
            return fail(source, admission.detail());
        }
        List<Plan> plans = admission.value();
        Map<String, String> parameters = parameters(plans, null, 0, Operation.CLEAR);
        if (!consumeConfirmation(source, "sef:enchant.clear", parameters, targetIds(plans), token)) {
            return 0;
        }
        return KernelCommandExecutor.execute(
                source,
                "sef:enchant.clear",
                parameters,
                targetIds(plans),
                false,
                () -> execute(source, plans, null, 0, Operation.CLEAR));
    }

    private static ActionResult<List<Plan>> plan(
            CommandSourceStack source,
            Collection<ServerPlayer> rawTargets,
            Holder<Enchantment> enchantment,
            int level,
            Operation operation
    ) {
        List<ServerPlayer> targets = rawTargets.stream().distinct().toList();
        if (targets.isEmpty()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "no players were selected");
        }
        if (targets.size() > 1 && !has(source, "commands.enchant.bulk")) {
            return ActionResult.failure(ActionResult.ReasonCode.PERMISSION_DENIED, "bulk enchanting permission is required");
        }
        if (level < 0
                || level > configuredMaximum()
                || level > IMPLEMENTATION_MAXIMUM_LEVEL) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "enchantment level is outside the safety ceiling");
        }
        List<Plan> plans = new ArrayList<>();
        for (ServerPlayer target : targets) {
            ActionResult<Void> targetDecision = authorizeTarget(source, target);
            if (!targetDecision.successful()) {
                return ActionResult.failure(targetDecision.reason(), targetDecision.detail());
            }
            int slot = target.getInventory().selected;
            ItemStack item = target.getInventory().getItem(slot);
            if (item.isEmpty()) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.INVALID_INPUT,
                        target.getGameProfile().getName() + " is not holding an item");
            }
            int currentLevel = enchantment == null ? 0 : enchantments(item).getLevel(enchantment);
            if (operation == Operation.APPLY) {
                if (level < currentLevel) {
                    return ActionResult.failure(
                            ActionResult.ReasonCode.POLICY_DENIED,
                            "the requested level would lower an existing enchantment");
                }
                int vanillaMaximum = enchantment.value().getMaxLevel();
                if (level > vanillaMaximum
                        && (!unsafeLevelsEnabled() || !has(source, "commands.enchant.unsafe_level"))) {
                    return ActionResult.failure(
                            ActionResult.ReasonCode.PERMISSION_DENIED,
                            "unsafe enchantment level permission is required");
                }
                if (!item.supportsEnchantment(enchantment)
                        && (!arbitraryItemsEnabled() || !has(source, "commands.enchant.any_item"))) {
                    return ActionResult.failure(
                            ActionResult.ReasonCode.PERMISSION_DENIED,
                            "arbitrary item enchantment permission is required");
                }
                if (hasConflict(item, enchantment)
                        && (!incompatibleEnchantmentsEnabled()
                        || !has(source, "commands.enchant.incompatible"))) {
                    return ActionResult.failure(
                            ActionResult.ReasonCode.PERMISSION_DENIED,
                            "incompatible enchantment permission is required");
                }
            } else if (operation == Operation.REMOVE && currentLevel == 0) {
                return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "the held item does not have that enchantment");
            }
            plans.add(new Plan(target, slot, item.copy(), currentLevel));
        }
        return ActionResult.success(List.copyOf(plans));
    }

    private static ActionResult<Void> authorizeTarget(CommandSourceStack source, ServerPlayer target) {
        ServerPlayer actor = source.getPlayer();
        if (actor == target) {
            return has(source, "commands.enchant.self")
                    ? ActionResult.success(null)
                    : ActionResult.failure(
                            ActionResult.ReasonCode.PERMISSION_DENIED,
                            "self enchanting permission is required");
        }
        if (!has(source, "commands.enchant.others")) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.PERMISSION_DENIED,
                    "other player enchanting permission is required");
        }
        var decision = PlayerTargetPolicy.decide(
                source,
                target,
                PermissionsHandler.phasePermission("commands.enchant.hierarchy.override"),
                PermissionsHandler.phasePermission("commands.enchant.exempt"),
                PermissionsHandler.phasePermission("commands.enchant.exemption.override"),
                false,
                true);
        return decision.allowed()
                ? ActionResult.success(null)
                : ActionResult.failure(decision.reason(), "the selected player is protected");
    }

    private static int execute(
            CommandSourceStack source,
            List<Plan> plans,
            Holder<Enchantment> enchantment,
            int level,
            Operation operation
    ) {
        for (Plan plan : plans) {
            ItemStack current = plan.player().getInventory().getItem(plan.slot());
            if (!ItemStack.matches(current, plan.snapshot())
                    || current.getCount() != plan.snapshot().getCount()) {
                return fail(source, "a selected held item changed before the operation");
            }
        }
        List<Plan> applied = new ArrayList<>();
        try {
            for (Plan plan : plans) {
                ItemStack current = plan.player().getInventory().getItem(plan.slot());
                if (operation == Operation.CLEAR) {
                    clearEnchantments(current);
                } else {
                    setEnchantment(plan.player(), plan.slot(), current, enchantment, level);
                }
                plan.player().getInventory().setChanged();
                plan.player().containerMenu.broadcastChanges();
                applied.add(plan);
            }
        } catch (RuntimeException exception) {
            for (Plan plan : applied) {
                plan.player().getInventory().setItem(plan.slot(), plan.snapshot().copy());
                plan.player().getInventory().setChanged();
            }
            return fail(source, "the enchantment operation was rolled back");
        }
        audit(source, plans, operation);
        String label = switch (operation) {
            case APPLY -> "enchantment applied at level " + level;
            case REMOVE -> "enchantment removed";
            case CLEAR -> "all enchantments cleared";
        };
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&a" + label + " for &e" + plans.size() + " &aplayer items."), true);
        return plans.size();
    }

    static void setEnchantment(
            ServerPlayer player,
            int slot,
            ItemStack item,
            Holder<Enchantment> enchantment,
            int level
    ) {
        if (item.is(Items.BOOK) && level > 0) {
            ItemStack replacement = item.getItem().applyEnchantments(
                    item,
                    List.of(new net.minecraft.world.item.enchantment.EnchantmentInstance(enchantment, level)));
            player.getInventory().setItem(slot, replacement);
            return;
        }
        setEnchantmentComponent(item, enchantment, level);
    }

    static void setEnchantmentComponent(
            ItemStack item,
            Holder<Enchantment> enchantment,
            int level
    ) {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(enchantment, "enchantment");
        if (level < 0 || level > IMPLEMENTATION_MAXIMUM_LEVEL) {
            throw new IllegalArgumentException("enchantment level is outside bounds");
        }
        DataComponentType<ItemEnchantments> component =
                item.is(Items.ENCHANTED_BOOK) ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS;
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(
                item.getOrDefault(component, ItemEnchantments.EMPTY));
        mutable.set(enchantment, level);
        item.set(component, mutable.toImmutable());
    }

    static ItemEnchantments enchantments(ItemStack item) {
        DataComponentType<ItemEnchantments> component =
                item.is(Items.ENCHANTED_BOOK) ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS;
        return item.getOrDefault(component, ItemEnchantments.EMPTY);
    }

    static void clearEnchantments(ItemStack item) {
        item.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        item.set(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
    }

    static boolean hasConflict(ItemStack item, Holder<Enchantment> candidate) {
        for (Holder<Enchantment> current : enchantments(item).keySet()) {
            if (!current.equals(candidate) && !Enchantment.areCompatible(current, candidate)) {
                return true;
            }
        }
        return false;
    }

    private static int previewRemove(
            CommandSourceStack source,
            Collection<ServerPlayer> targets,
            Holder<Enchantment> enchantment
    ) {
        ActionResult<List<Plan>> admission = plan(source, targets, enchantment, 0, Operation.REMOVE);
        if (!admission.successful()) {
            return fail(source, admission.detail());
        }
        return issueConfirmation(
                source,
                "sef:enchant.remove",
                parameters(admission.value(), enchantment, 0, Operation.REMOVE),
                targetIds(admission.value()),
                "remove one enchantment");
    }

    private static int previewClear(
            CommandSourceStack source,
            Collection<ServerPlayer> targets
    ) {
        ActionResult<List<Plan>> admission = plan(source, targets, null, 0, Operation.CLEAR);
        if (!admission.successful()) {
            return fail(source, admission.detail());
        }
        return issueConfirmation(
                source,
                "sef:enchant.clear",
                parameters(admission.value(), null, 0, Operation.CLEAR),
                targetIds(admission.value()),
                "clear all enchantments");
    }

    private static int issueConfirmation(
            CommandSourceStack source,
            String actionId,
            Map<String, String> parameters,
            List<UUID> targets,
            String operation
    ) {
        ConfirmationService.Request request = confirmation(source, actionId, parameters, targets);
        ActionResult<ConfirmationService.IssuedToken> issued = KernelServices.confirmations().issue(
                request,
                Duration.ofSeconds(confirmationSeconds()));
        if (!issued.successful()) {
            return fail(source, "a confirmation token could not be issued");
        }
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&ePreview. " + operation + " for &f" + targets.size()
                        + " &eplayers. Append &fconfirm " + issued.value().token()
                        + "&e to execute."), false);
        return targets.size();
    }

    private static boolean consumeConfirmation(
            CommandSourceStack source,
            String actionId,
            Map<String, String> parameters,
            List<UUID> targets,
            String token
    ) {
        ActionResult<ConfirmationService.Request> consumed = KernelServices.confirmations().consume(
                token,
                confirmation(source, actionId, parameters, targets));
        if (!consumed.successful()) {
            fail(source, "the confirmation token is invalid, expired, used, or stale");
            return false;
        }
        return true;
    }

    private static ConfirmationService.Request confirmation(
            CommandSourceStack source,
            String actionId,
            Map<String, String> parameters,
            List<UUID> targets
    ) {
        return new ConfirmationService.Request(
                actorId(source),
                actionId,
                parameters,
                targets,
                "",
                0L,
                0L,
                0L,
                KernelServices.commandPolicies().revision());
    }

    private static UUID actorId(CommandSourceStack source) {
        return source.getEntity() == null
                ? UUID.nameUUIDFromBytes(
                ("sef:source:" + source.getTextName()).getBytes(StandardCharsets.UTF_8))
                : source.getEntity().getUUID();
    }

    private static Map<String, String> parameters(
            List<Plan> plans,
            Holder<Enchantment> enchantment,
            int level,
            Operation operation
    ) {
        String enchantmentId = enchantment == null
                ? ""
                : enchantment.unwrapKey().map(key -> key.location().toString()).orElse("");
        String itemBinding = plans.stream()
                .map(Plan::binding)
                .sorted()
                .collect(java.util.stream.Collectors.joining(","));
        return Map.of(
                "operation", operation.name().toLowerCase(java.util.Locale.ROOT),
                "enchantment", enchantmentId,
                "level", Integer.toString(level),
                "count", Integer.toString(plans.size()),
                "items", digest(itemBinding));
    }

    private static List<UUID> targetIds(List<Plan> plans) {
        return plans.stream().map(plan -> plan.player().getUUID()).sorted().toList();
    }

    static int configuredMinimum() {
        return moduleInteger("safety.minimum_level", ConfigHandler.config.superEnchantingMinLevel.get());
    }

    static int configuredMaximum() {
        return Math.clamp(
                moduleInteger("safety.maximum_level", ConfigHandler.config.superEnchantingMaxLevel.get()),
                1,
                IMPLEMENTATION_MAXIMUM_LEVEL);
    }

    static boolean unsafeLevelsEnabled() {
        return ConfigHandler.config.superEnchantingAllowUnsafe.get()
                && moduleBoolean("safety.allow_unsafe_levels", true);
    }

    static boolean arbitraryItemsEnabled() {
        return ConfigHandler.config.superEnchantingAllowUnsafe.get()
                && moduleBoolean("safety.allow_arbitrary_items", true);
    }

    static boolean incompatibleEnchantmentsEnabled() {
        return ConfigHandler.config.superEnchantingAllowUnsafe.get()
                && moduleBoolean("safety.allow_incompatible", true);
    }

    private static int confirmationSeconds() {
        return Math.clamp(moduleInteger("confirmation.seconds", 60), 5, 600);
    }

    private static int moduleInteger(String path, int fallback) {
        try {
            return Integer.parseInt(KernelServices.moduleConfigs().value("super_enchanting", path));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return fallback;
        }
    }

    private static boolean moduleBoolean(String path, boolean fallback) {
        try {
            return Boolean.parseBoolean(KernelServices.moduleConfigs().value("super_enchanting", path));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return fallback;
        }
    }

    private static String digest(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("sha256 is unavailable", exception);
        }
    }

    private static void audit(CommandSourceStack source, List<Plan> plans, Operation operation) {
        AuditService.record(AuditService.Event.metadata(
                SecurityAuditService.currentSessionId(),
                source.getEntity() == null ? new java.util.UUID(0L, 0L) : source.getEntity().getUUID(),
                source.getTextName(),
                source.getEntity() == null ? "console" : "player",
                "sef:enchant." + operation.name().toLowerCase(java.util.Locale.ROOT),
                plans.stream().map(plan -> plan.player().getUUID()).toList(),
                AuditService.Result.SUCCESS,
                ActionResult.ReasonCode.SUCCESS,
                "command",
                AuditService.AuditClass.ADMIN_ACTION));
    }

    private static boolean has(CommandSourceStack source, String id) {
        var node = PermissionsHandler.phasePermission(id);
        return node != null && PermissionService.has(source, node);
    }

    private static int fail(CommandSourceStack source, String detail) {
        source.sendFailure(TextFormatter.stringToFormattedText("&c" + detail));
        return 0;
    }

    private enum Operation {
        APPLY,
        REMOVE,
        CLEAR
    }

    private record Plan(ServerPlayer player, int slot, ItemStack snapshot, int currentLevel) {
        private String binding() {
            return player.getUUID() + ":" + slot + ":" + snapshot.getCount() + ":"
                    + Integer.toUnsignedString(snapshot.hashCode(), 16);
        }
    }
}
