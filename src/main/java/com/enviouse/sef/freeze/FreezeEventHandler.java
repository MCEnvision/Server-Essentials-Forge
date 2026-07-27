package com.enviouse.sef.freeze;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import com.enviouse.sef.ServerEssentialsForge;

import java.util.Locale;
import java.util.Set;

/**
 * Event handler that blocks frozen players from:
 * - Using commands (except chat is allowed via ServerChatEvent which is NOT cancelled here)
 * - Breaking blocks
 * - Placing blocks
 * - Attacking entities
 * - Right-clicking / interacting
 *
 * Movement and looking are handled by FreezeManager.tick() which teleports them back.
 */
@EventBusSubscriber(modid = ServerEssentialsForge.MODID)
public class FreezeEventHandler {
    private static final Set<String> ALLOWED_CHAT_COMMANDS = Set.of(
            "msg", "r", "tell", "w", "reply", "whisper",
            "helpop", "ac", "adminchat", "staffchat", "pchat", "teammsg", "tm");

    /**
     * Block commands from frozen players.
     * We allow certain "safe" commands like chat-related ones since the requirement
     * is that frozen players can chat to respond to the admin.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCommand(CommandEvent event) {
        if (!enabled()) return;

        ServerPlayer player = event.getParseResults().getContext().getSource().getPlayer();
        if (player == null || !FreezeManager.isFrozen(player.getUUID())) {
            return;
        }
        if (allowedWhileFrozen(
                event.getParseResults().getReader().getString(),
                ConfigHandler.config.freezeAllowChat.get())) {
            return;
        }
        event.setCanceled(true);
        player.sendSystemMessage(TextFormatter.stringToFormattedText(
                ConfigHandler.config.freezeCommandBlockedMsg.get()));
    }

    /**
     * Block breaking blocks.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!enabled()) return;

        if (event.getPlayer() instanceof ServerPlayer player) {
            if (FreezeManager.isFrozen(player.getUUID())) {
                event.setCanceled(true);
                player.sendSystemMessage(TextFormatter.stringToFormattedText(
                    ConfigHandler.config.freezeActionBlockedMsg.get()));
            }
        }
    }

    /**
     * Block placing blocks.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!enabled()) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            if (FreezeManager.isFrozen(player.getUUID())) {
                event.setCanceled(true);
                player.sendSystemMessage(TextFormatter.stringToFormattedText(
                    ConfigHandler.config.freezeActionBlockedMsg.get()));
            }
        }
    }

    /**
     * Block attacking entities.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!enabled()) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            if (FreezeManager.isFrozen(player.getUUID())) {
                event.setCanceled(true);
                player.sendSystemMessage(TextFormatter.stringToFormattedText(
                    ConfigHandler.config.freezeActionBlockedMsg.get()));
            }
        }
    }

    /**
     * Block right-click interactions.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!enabled()) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            if (FreezeManager.isFrozen(player.getUUID())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!enabled()) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            if (FreezeManager.isFrozen(player.getUUID())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!enabled()) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            if (FreezeManager.isFrozen(player.getUUID())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!enabled()) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            if (FreezeManager.isFrozen(player.getUUID())) {
                event.setCanceled(true);
            }
        }
    }

    static boolean allowedWhileFrozen(String input, boolean allowChat) {
        if (!allowChat || input == null) {
            return false;
        }
        String candidate = input.strip();
        while (candidate.startsWith("/")) {
            candidate = candidate.substring(1).stripLeading();
        }
        int separator = 0;
        while (separator < candidate.length()
                && !Character.isWhitespace(candidate.charAt(separator))) {
            separator++;
        }
        String root = candidate.substring(0, separator).toLowerCase(Locale.ROOT);
        int namespace = root.indexOf(':');
        if (namespace >= 0) {
            root = root.substring(namespace + 1);
        }
        return ALLOWED_CHAT_COMMANDS.contains(root);
    }

    private static boolean enabled() {
        return ConfigHandler.config.enableFreezeSystem.get()
                || ConfigHandler.config.enableModerationEssentials.get();
    }
}
