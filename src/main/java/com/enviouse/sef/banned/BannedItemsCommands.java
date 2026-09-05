package com.enviouse.sef.banned;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.identity.IdentityArguments;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.vanish.VanishUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;
import java.util.regex.Pattern;

/**
 * Registers the {@code /banned} command suite.
 *
 * <pre>
 * /banned                                 — list view (everyone, hover-tooltip)
 * /banned add &lt;item&gt; [duration] [announce] [reason...]
 * /banned remove &lt;item&gt;
 * /banned update &lt;item&gt; [duration] [announce] [reason...]
 * /banned list
 * /banned clear
 * /banned reload
 * /banned setradius &lt;n&gt;
 * /banned setinterval &lt;ticks&gt;
 * /banned toggle &lt;items|blocks|drops|all&gt;
 * /banned bypass &lt;player&gt; &lt;on|off&gt;
 * /banned scan &lt;player&gt;
 * /banned excepted
 * /banned excepted remove &lt;index&gt;
 * /banned excepted clear
 * </pre>
 *
 * <p>All mutating subcommands require op (level 2). The bare {@code /banned} and
 * {@code /banned list} are available to everyone for read-only inspection.
 */
public class BannedItemsCommands {
    private static BannedItemsManager manager;
    private static final Pattern COMPOUND_DURATION = Pattern.compile("(?:\\d+[smhd])+", Pattern.CASE_INSENSITIVE);

    public static void setManager(BannedItemsManager mgr) { manager = mgr; }

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ALL_ITEMS =
        (ctx, b) -> {
            String input = b.getRemainingLowerCase();
            for (String id : manager.suggestAllItems()) {
                if (id.toLowerCase().contains(input)) b.suggest(id);
            }
            return b.buildFuture();
        };

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_BANNED =
        (ctx, b) -> {
            for (BannedEntry e : manager.getEntries().values()) {
                if (e.pattern != null) b.suggest(e.pattern);
            }
            return b.buildFuture();
        };

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_DURATION =
        (ctx, b) -> {
            for (String s : new String[]{"infinite", "permanent", "30s", "5m", "1h", "1h30m", "1d"})
                b.suggest(s);
            return b.buildFuture();
        };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("banned")
            .requires(src -> PermissionService.has(src, PermissionsHandler.bannedView)
                    || PermissionService.has(src, PermissionsHandler.bannedCommand))
            .executes(ctx -> doList(ctx.getSource())); // /banned → list view (everyone)

        // /banned list (alias of bare command)
        root.then(Commands.literal("list").executes(ctx -> doList(ctx.getSource())));

        // ── Mutations: op-only ──────────────────────────────────────────────
        // /banned add <item> [duration] [announce] [reason...]
        root.then(Commands.literal("add").requires(BannedItemsCommands::isOp)
            .then(Commands.argument("item", ResourceLocationArgument.id())
                .suggests(SUGGEST_ALL_ITEMS)
                .executes(ctx -> doAdd(ctx, "infinite", false, ""))
                .then(Commands.argument("duration", StringArgumentType.word())
                    .suggests(SUGGEST_DURATION)
                    .executes(ctx -> doAdd(ctx, dur(ctx), false, ""))
                    .then(Commands.argument("announce", BoolArgumentType.bool())
                        .executes(ctx -> doAdd(ctx, dur(ctx), BoolArgumentType.getBool(ctx, "announce"), ""))
                        .then(Commands.argument("reason", StringArgumentType.greedyString())
                            .executes(ctx -> doAdd(ctx, dur(ctx),
                                BoolArgumentType.getBool(ctx, "announce"),
                                StringArgumentType.getString(ctx, "reason"))))))));

        // /banned addhand [duration] [announce] [reason...]
        // Bans whatever item the executing player holds in their main hand.
        root.then(Commands.literal("addhand").requires(BannedItemsCommands::isOp)
            .executes(ctx -> doAddHand(ctx, "infinite", false, ""))
            .then(Commands.argument("duration", StringArgumentType.word())
                .suggests(SUGGEST_DURATION)
                .executes(ctx -> doAddHand(ctx, dur(ctx), false, ""))
                .then(Commands.argument("announce", BoolArgumentType.bool())
                    .executes(ctx -> doAddHand(ctx, dur(ctx), BoolArgumentType.getBool(ctx, "announce"), ""))
                    .then(Commands.argument("reason", StringArgumentType.greedyString())
                        .executes(ctx -> doAddHand(ctx, dur(ctx),
                            BoolArgumentType.getBool(ctx, "announce"),
                            StringArgumentType.getString(ctx, "reason")))))));

        // /banned remove <item>
        root.then(Commands.literal("remove").requires(BannedItemsCommands::isOp)
            .then(Commands.argument("item", StringArgumentType.greedyString())
                .suggests(SUGGEST_BANNED)
                .executes(ctx -> doRemove(ctx.getSource(), StringArgumentType.getString(ctx, "item")))));

        // /banned update <item> [duration] [announce] [reason...]
        root.then(Commands.literal("update").requires(BannedItemsCommands::isOp)
            .then(Commands.argument("item", ResourceLocationArgument.id())
                .suggests(SUGGEST_BANNED)
                .executes(ctx -> doUpdate(ctx, null, null, null))
                .then(Commands.argument("duration", StringArgumentType.word())
                    .suggests(SUGGEST_DURATION)
                    .executes(ctx -> doUpdate(ctx, dur(ctx), null, null))
                    .then(Commands.argument("announce", BoolArgumentType.bool())
                        .executes(ctx -> doUpdate(ctx, dur(ctx), BoolArgumentType.getBool(ctx, "announce"), null))
                        .then(Commands.argument("reason", StringArgumentType.greedyString())
                            .executes(ctx -> doUpdate(ctx, dur(ctx),
                                BoolArgumentType.getBool(ctx, "announce"),
                                StringArgumentType.getString(ctx, "reason"))))))));

        // /banned clear
        root.then(Commands.literal("clear").requires(BannedItemsCommands::isOp)
            .executes(ctx -> clear(ctx.getSource())));

        // /banned reload
        root.then(Commands.literal("reload").requires(BannedItemsCommands::isOp)
            .executes(ctx -> reload(ctx.getSource())));

        // /banned setradius <n>
        root.then(Commands.literal("setradius").requires(BannedItemsCommands::isOp)
            .then(Commands.argument("n", IntegerArgumentType.integer(1, 64))
                .executes(ctx -> setRadius(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "n")))));

        // /banned setinterval <ticks>
        root.then(Commands.literal("setinterval").requires(BannedItemsCommands::isOp)
            .then(Commands.argument("ticks", IntegerArgumentType.integer(1, 24000))
                .executes(ctx -> setInterval(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "ticks")))));

        // /banned toggle <items|blocks|drops|all>
        root.then(Commands.literal("toggle").requires(BannedItemsCommands::isOp)
            .then(Commands.literal("items").executes(ctx -> toggle(ctx.getSource(), "items")))
            .then(Commands.literal("blocks").executes(ctx -> toggle(ctx.getSource(), "blocks")))
            .then(Commands.literal("drops").executes(ctx -> toggle(ctx.getSource(), "drops")))
            .then(Commands.literal("all").executes(ctx -> toggle(ctx.getSource(), "all"))));

        // /banned bypass <player> <on|off>
        root.then(Commands.literal("bypass").requires(BannedItemsCommands::isOp)
            .then(IdentityArguments.online("player")
                .then(Commands.literal("on").executes(ctx -> bypass(ctx, true)))
                .then(Commands.literal("off").executes(ctx -> bypass(ctx, false)))));

        // /banned scan <player>
        root.then(Commands.literal("scan").requires(BannedItemsCommands::isOp)
            .then(IdentityArguments.online("player")
                .executes(BannedItemsCommands::scan)));

        // /banned excepted [remove <index> | clear]
        LiteralArgumentBuilder<CommandSourceStack> excepted = Commands.literal("excepted")
            .requires(BannedItemsCommands::isOp)
            .executes(ctx -> doListExcepted(ctx.getSource()));
        excepted.then(Commands.literal("remove")
            .then(Commands.argument("index", IntegerArgumentType.integer(0))
                .executes(ctx -> removeException(
                        ctx.getSource(), IntegerArgumentType.getInteger(ctx, "index")))));
        excepted.then(Commands.literal("clear")
            .executes(ctx -> clearExceptions(ctx.getSource())));
        root.then(excepted);

        dispatcher.register(root);
    }

    // ── Subcommand impls ────────────────────────────────────────────────────

    private static int doList(CommandSourceStack source) {
        return execute(source, "sef:banned.list", Map.of("operation", "list"), List.of(), true, () -> {
            var entries = manager.getEntries();
            if (entries.isEmpty()) {
                source.sendSuccess(() -> fmt("&7No banned items configured."), false);
                return 0;
            }
            source.sendSuccess(() -> fmt("&6━━━━━━━━ Banned Items &7(" + entries.size() + ") &6━━━━━━━━"), false);
            for (BannedEntry e : entries.values()) {
                source.sendSuccess(() -> formatEntryLine(e), false);
            }
            return entries.size();
        });
    }

    private static int doAdd(CommandContext<CommandSourceStack> ctx, String durationStr,
                             boolean announce, String reason) {
        String item = ResourceLocationArgument.getId(ctx, "item").toString();
        return execute(
                ctx.getSource(),
                "sef:banned.add",
                Map.of(
                        "item", item,
                        "duration", durationStr,
                        "announce", Boolean.toString(announce),
                        "reason_length", Integer.toString(reason == null ? 0 : reason.length())),
                List.of(),
                false,
                () -> doAddInternal(ctx, durationStr, announce, reason));
    }

    private static int doAddInternal(CommandContext<CommandSourceStack> ctx, String durationStr,
                                    boolean announce, String reason) {
        String item = ResourceLocationArgument.getId(ctx, "item").toString();
        Long durMs = parseDuration(ctx.getSource(), durationStr);
        if (durMs == null) {
            return 0;
        }
        String issuer = sourceName(ctx.getSource());
        boolean added = manager.addBan(item, reason == null ? "" : reason, durMs, issuer, announce);
        if (!added) {
            ctx.getSource().sendFailure(fmt("&cAlready banned: &e" + item));
            return 0;
        }
        BannedEntry e = manager.getEntry(item);
        ctx.getSource().sendSuccess(() -> fmt("&aBanned &e" + item
            + "&a — &7duration: &f" + (e != null ? e.getDurationString() : durationStr)
            + " &7| announce: &f" + announce
            + " &7| reason: &f" + (reason == null || reason.isEmpty() ? "—" : reason)), true);
        return 1;
    }

    private static int doAddHand(CommandContext<CommandSourceStack> ctx, String durationStr,
                                 boolean announce, String reason) {
        ServerPlayer player;
        try {
            player = ctx.getSource().getPlayerOrException();
        } catch (Exception e) {
            ctx.getSource().sendFailure(fmt("&c/banned addhand can only be used by a player."));
            return 0;
        }
        return execute(
                ctx.getSource(),
                "sef:banned.addhand",
                Map.of(
                        "duration", durationStr,
                        "announce", Boolean.toString(announce),
                        "reason_length", Integer.toString(reason == null ? 0 : reason.length())),
                List.of(player.getUUID()),
                false,
                () -> doAddHandInternal(ctx, durationStr, announce, reason));
    }

    private static int doAddHandInternal(CommandContext<CommandSourceStack> ctx, String durationStr,
                                         boolean announce, String reason) {
        ServerPlayer player;
        try {
            player = ctx.getSource().getPlayerOrException();
        } catch (Exception e) {
            ctx.getSource().sendFailure(fmt("&c/banned addhand can only be used by a player."));
            return 0;
        }
        net.minecraft.world.item.ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            ctx.getSource().sendFailure(fmt("&cYour main hand is empty — nothing to ban."));
            return 0;
        }
        net.minecraft.resources.ResourceLocation rl =
            net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (rl == null) {
            ctx.getSource().sendFailure(fmt("&cCould not resolve registry id for the held item."));
            return 0;
        }
        String item = rl.toString();
        Long durMs = parseDuration(ctx.getSource(), durationStr);
        if (durMs == null) {
            return 0;
        }
        String issuer = sourceName(ctx.getSource());
        boolean added = manager.addBan(item, reason == null ? "" : reason, durMs, issuer, announce);
        if (!added) {
            ctx.getSource().sendFailure(fmt("&cAlready banned: &e" + item));
            return 0;
        }
        BannedEntry e = manager.getEntry(item);
        ctx.getSource().sendSuccess(() -> fmt("&aBanned (from hand) &e" + item
            + "&a — &7duration: &f" + (e != null ? e.getDurationString() : durationStr)
            + " &7| announce: &f" + announce
            + " &7| reason: &f" + (reason == null || reason.isEmpty() ? "—" : reason)), true);
        return 1;
    }

    private static int doRemove(CommandSourceStack source, String item) {
        return execute(
                source,
                "sef:banned.remove",
                Map.of("item", item),
                List.of(),
                false,
                () -> doRemoveInternal(source, item));
    }

    private static int doRemoveInternal(CommandSourceStack source, String item) {
        if (manager.removeBan(item)) {
            source.sendSuccess(() -> fmt("&aRemoved banned entry: &e" + item), true);
            return 1;
        }
        source.sendFailure(fmt("&cItem not in banned list: " + item));
        return 0;
    }

    private static int doUpdate(CommandContext<CommandSourceStack> ctx,
                                String durationStr, Boolean announce, String reason) {
        String item = ResourceLocationArgument.getId(ctx, "item").toString();
        return execute(
                ctx.getSource(),
                "sef:banned.update",
                Map.of(
                        "item", item,
                        "duration_present", Boolean.toString(durationStr != null),
                        "announce_present", Boolean.toString(announce != null),
                        "reason_length", Integer.toString(reason == null ? 0 : reason.length())),
                List.of(),
                false,
                () -> doUpdateInternal(ctx, durationStr, announce, reason));
    }

    private static int doUpdateInternal(CommandContext<CommandSourceStack> ctx,
                                       String durationStr, Boolean announce, String reason) {
        String item = ResourceLocationArgument.getId(ctx, "item").toString();
        Long durMs = durationStr == null ? null : parseDuration(ctx.getSource(), durationStr);
        if (durationStr != null && durMs == null) {
            return 0;
        }
        boolean ok = manager.updateBan(item, reason, durMs, announce);
        if (!ok) {
            ctx.getSource().sendFailure(fmt("&cNot banned (cannot update): &e" + item));
            return 0;
        }
        BannedEntry e = manager.getEntry(item);
        ctx.getSource().sendSuccess(() -> fmt("&aUpdated &e" + item
            + "&a — &7duration: &f" + (e != null ? e.getDurationString() : "(unchanged)")
            + " &7| announce: &f" + (e != null ? e.announce : "(unchanged)")
            + " &7| reason: &f" + (e != null && !e.reason.isEmpty() ? e.reason : "—")), true);
        return 1;
    }

    private static int doListExcepted(CommandSourceStack source) {
        return execute(source, "sef:banned.excepted", Map.of("operation", "list"), List.of(), true, () -> {
            List<BannedExceptedBlock> list = manager.getExceptions();
            if (list.isEmpty()) {
                source.sendSuccess(() -> fmt("&7No excepted blocks recorded."), false);
                return 0;
            }
            source.sendSuccess(() -> fmt("&6━━━━ Banned-block Exceptions &7(" + list.size() + ") &6━━━━"), false);
            for (int i = 0; i < list.size(); i++) {
                BannedExceptedBlock b = list.get(i);
                int idx = i;
                source.sendSuccess(() -> formatExceptedLine(idx, b), false);
            }
            source.sendSuccess(() -> fmt("&7Use &f/banned excepted remove <index>&7 to drop one."), false);
            return list.size();
        });
    }

    private static int clear(CommandSourceStack source) {
        return execute(source, "sef:banned.clear", Map.of(), List.of(), true, () -> {
            int count = manager.clearAll();
            source.sendSuccess(() -> fmt("&aCleared &e" + count + "&a banned entries."), true);
            return count;
        });
    }

    private static int reload(CommandSourceStack source) {
        return execute(source, "sef:banned.reload", Map.of(), List.of(), false, () -> {
            manager.reload(source.getServer());
            source.sendSuccess(() -> fmt("&aBanned items reloaded from disk."), true);
            return 1;
        });
    }

    private static int setRadius(CommandSourceStack source, int radius) {
        return execute(source, "sef:banned.setradius", Map.of("radius", Integer.toString(radius)),
                List.of(), false, () -> {
                    manager.setRadiusOverride(radius);
                    source.sendSuccess(() -> fmt("&aBanned-block scan radius set to &e" + radius + "&a."), true);
                    return 1;
                });
    }

    private static int setInterval(CommandSourceStack source, int ticks) {
        return execute(source, "sef:banned.setinterval", Map.of("ticks", Integer.toString(ticks)),
                List.of(), false, () -> {
                    manager.setIntervalOverride(ticks);
                    source.sendSuccess(() -> fmt("&aBanned-block scan interval set to &e" + ticks + "&a ticks."), true);
                    return 1;
                });
    }

    private static int toggle(CommandSourceStack source, String target) {
        return execute(source, "sef:banned.toggle", Map.of("target", target), List.of(), false, () -> {
            switch (target) {
                case "items" -> {
                    manager.setItemsEnabled(!manager.isItemsEnabled());
                    source.sendSuccess(() -> fmt("&aItem scanning: " + onOff(manager.isItemsEnabled())), true);
                }
                case "blocks" -> {
                    manager.setBlocksEnabled(!manager.isBlocksEnabled());
                    source.sendSuccess(() -> fmt("&aBlock scanning: " + onOff(manager.isBlocksEnabled())), true);
                }
                case "drops" -> {
                    manager.setDropOnDestroy(!manager.isDropOnDestroy());
                    source.sendSuccess(() -> fmt("&aDrop on destroy: " + onOff(manager.isDropOnDestroy())), true);
                }
                case "all" -> {
                    boolean both = !(manager.isItemsEnabled() && manager.isBlocksEnabled());
                    manager.setItemsEnabled(both);
                    manager.setBlocksEnabled(both);
                    source.sendSuccess(() -> fmt("&aAll banned scans: " + onOff(both)), true);
                }
                default -> throw new IllegalArgumentException("unknown banned item toggle");
            }
            return 1;
        });
    }

    private static int bypass(CommandContext<CommandSourceStack> context, boolean enabled)
            throws CommandSyntaxException {
        ServerPlayer target = IdentityArguments.getOnline(context, "player");
        if (!eligible(context.getSource(), target)) {
            return unavailable(context.getSource());
        }
        return execute(
                context.getSource(),
                "sef:banned.bypass",
                Map.of("enabled", Boolean.toString(enabled)),
                List.of(target.getUUID()),
                false,
                () -> {
                    if (!eligible(context.getSource(), target)) {
                        return unavailable(context.getSource());
                    }
                    boolean changed = manager.setBypass(target.getUUID(), enabled);
                    context.getSource().sendSuccess(() -> fmt(changed
                            ? "&aBypass " + (enabled ? "&2enabled" : "&cdisabled")
                                    + " &afor &e" + target.getGameProfile().getName()
                            : "&7" + target.getGameProfile().getName()
                                    + (enabled ? " already had bypass." : " did not have bypass.")), true);
                    return 1;
                });
    }

    private static int scan(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = IdentityArguments.getOnline(context, "player");
        if (!eligible(context.getSource(), target)) {
            return unavailable(context.getSource());
        }
        return execute(
                context.getSource(),
                "sef:banned.scan",
                Map.of("target", target.getUUID().toString()),
                List.of(target.getUUID()),
                true,
                () -> {
                    if (!eligible(context.getSource(), target)) {
                        return -1;
                    }
                    int count = manager.forceScan(target);
                    context.getSource().sendSuccess(() -> fmt(
                            "&aForced scan on &e" + target.getGameProfile().getName()
                                    + "&a — removed &e" + count + "&a item(s)."), true);
                    return count;
                });
    }

    private static int removeException(CommandSourceStack source, int index) {
        return execute(source, "sef:banned.excepted.remove", Map.of("index", Integer.toString(index)),
                List.of(), false, () -> {
                    boolean removed = manager.removeExceptionAt(index);
                    source.sendSuccess(() -> fmt(removed
                            ? "&aRemoved exception #" + index
                            : "&cNo exception at index " + index), true);
                    return removed ? 1 : 0;
                });
    }

    private static int clearExceptions(CommandSourceStack source) {
        return execute(source, "sef:banned.excepted.clear", Map.of(), List.of(), true, () -> {
            int count = manager.clearExceptions();
            source.sendSuccess(() -> fmt("&aCleared &e" + count + "&a exception(s)."), true);
            return count;
        });
    }

    private static int execute(
            CommandSourceStack source,
            String actionId,
            Map<String, String> parameters,
            List<UUID> targetIds,
            boolean zeroIsSuccess,
            IntSupplier action
    ) {
        AtomicInteger result = new AtomicInteger();
        int completed = KernelCommandExecutor.execute(
                source,
                actionId,
                parameters,
                targetIds,
                false,
                () -> {
                    int value = action.getAsInt();
                    result.set(value);
                    return value == 0 && zeroIsSuccess ? 1 : value;
                });
        return completed > 0 ? result.get() : 0;
    }

    // ── Formatting ──────────────────────────────────────────────────────────

    /** A single line in /banned list with a hover tooltip. */
    private static MutableComponent formatEntryLine(BannedEntry e) {
        String label = e.pattern + (e.isPermanent() ? "" : " (" + e.getRemainingString() + ")");
        MutableComponent line = (MutableComponent) TextFormatter.stringToFormattedText("  &7• &f" + label);

        MutableComponent tooltip = Component.empty()
            .append(Component.literal("Banned item ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(e.pattern).withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("\n"))
            .append(Component.literal("By: ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(e.bannedBy == null ? "?" : e.bannedBy).withStyle(ChatFormatting.WHITE))
            .append(Component.literal("\n"))
            .append(Component.literal("Reason: ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(e.reason == null || e.reason.isEmpty() ? "—" : e.reason).withStyle(ChatFormatting.WHITE))
            .append(Component.literal("\n"))
            .append(Component.literal("Duration: ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(e.getDurationString()).withStyle(ChatFormatting.WHITE))
            .append(Component.literal("\n"))
            .append(Component.literal(e.isPermanent() ? "Status: Permanent" : "Time left: " + e.getRemainingString())
                .withStyle(e.isPermanent() ? ChatFormatting.RED : ChatFormatting.AQUA))
            .append(Component.literal("\n"))
            .append(Component.literal("Announce on hit: ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(String.valueOf(e.announce)).withStyle(ChatFormatting.WHITE));

        line.setStyle(line.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip)));
        return line;
    }

    /** A single line in /banned excepted with a click-to-tp action. */
    private static MutableComponent formatExceptedLine(int index, BannedExceptedBlock b) {
        String posStr = b.x + " " + b.y + " " + b.z;
        String tpCmd = "/execute in " + b.dimension + " run tp @s " + posStr;
        MutableComponent line = (MutableComponent) TextFormatter.stringToFormattedText(
            "  &7#" + index + " &f" + b.itemId + " &7at &e" + posStr + " &7(" + b.dimension + ")");

        MutableComponent tooltip = Component.empty()
            .append(Component.literal("Click to teleport").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("\n"))
            .append(Component.literal("Placed by: ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(b.addedBy == null ? "?" : b.addedBy).withStyle(ChatFormatting.WHITE));

        Style style = line.getStyle()
            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, tpCmd))
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip));
        line.setStyle(style);
        return line;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private static boolean isOp(CommandSourceStack src) {
        return PermissionService.has(src, PermissionsHandler.bannedCommand);
    }

    private static boolean eligible(CommandSourceStack source, ServerPlayer target) {
        if (source.getPlayer() != null && VanishUtil.isVanished(target, source.getPlayer())) {
            return false;
        }
        return PlayerTargetPolicy.decide(
                source,
                target,
                PermissionsHandler.phasePermission("banned.hierarchy.bypass"),
                PermissionsHandler.phasePermission("banned.exempt"),
                PermissionsHandler.phasePermission("banned.bypass.exempt"),
                false,
                true).allowed();
    }

    private static int unavailable(CommandSourceStack source) {
        source.sendFailure(fmt("&cThat player is unavailable."));
        return 0;
    }

    private static String sourceName(CommandSourceStack src) {
        try { return src.getPlayerOrException().getGameProfile().getName(); }
        catch (Exception e) { return "Console"; }
    }

    private static String dur(CommandContext<CommandSourceStack> ctx) {
        try { return StringArgumentType.getString(ctx, "duration"); }
        catch (IllegalArgumentException e) { return "infinite"; }
    }

    private static String onOff(boolean v) {
        return v ? "&2enabled" : "&cdisabled";
    }

    private static Long parseDuration(CommandSourceStack source, String input) {
        try {
            return parseDurationMs(input);
        } catch (IllegalArgumentException exception) {
            source.sendFailure(fmt("&cInvalid duration. Use infinite, seconds, or values such as 30s, 5m, 1h30m, or 2d."));
            return null;
        }
    }

    private static MutableComponent fmt(String s) {
        return (MutableComponent) TextFormatter.stringToFormattedText(s);
    }

    /**
     * Parses durations like {@code 30s}, {@code 5m}, {@code 1h30m}, {@code 2d12h},
     * {@code infinite}/{@code permanent}/{@code inf}/{@code forever}/{@code perm}.
     * Returns milliseconds, or {@code -1} for infinite. Plain numbers are seconds.
     * Invalid input is rejected instead of being interpreted as infinite.
     */
    public static long parseDurationMs(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Duration is required");
        }
        String s = input.trim().toLowerCase(Locale.ROOT);
        if (s.isEmpty()) {
            throw new IllegalArgumentException("Duration is required");
        }
        if (s.equals("infinite") || s.equals("inf") || s.equals("forever")
                || s.equals("perm") || s.equals("permanent")) return -1L;
        if (s.chars().allMatch(Character::isDigit)) {
            try {
                long duration = Math.multiplyExact(Long.parseLong(s), 1000L);
                if (duration <= 0L) {
                    throw new IllegalArgumentException("Duration must be greater than zero");
                }
                return duration;
            } catch (ArithmeticException | NumberFormatException exception) {
                throw new IllegalArgumentException("Duration is outside bounds", exception);
            }
        }
        if (!COMPOUND_DURATION.matcher(s).matches()) {
            throw new IllegalArgumentException("Duration format is invalid");
        }
        long total = 0L;
        long current = 0L;
        try {
            for (int i = 0; i < s.length(); i++) {
                char value = s.charAt(i);
                if (Character.isDigit(value)) {
                    current = Math.addExact(Math.multiplyExact(current, 10L), value - '0');
                    continue;
                }
                long multiplier = switch (value) {
                    case 's' -> 1_000L;
                    case 'm' -> 60_000L;
                    case 'h' -> 3_600_000L;
                    case 'd' -> 86_400_000L;
                    default -> throw new IllegalArgumentException("Duration format is invalid");
                };
                total = Math.addExact(total, Math.multiplyExact(current, multiplier));
                current = 0L;
            }
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Duration is outside bounds", exception);
        }
        if (total <= 0L) {
            throw new IllegalArgumentException("Duration must be greater than zero");
        }
        return total;
    }
}
