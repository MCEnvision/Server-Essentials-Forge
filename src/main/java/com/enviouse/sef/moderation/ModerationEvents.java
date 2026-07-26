package com.enviouse.sef.moderation;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.teleport.SafeTeleportService;
import com.enviouse.sef.teleport.SavedLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;

@EventBusSubscriber(modid = ServerEssentialsForge.MODID)
public final class ModerationEvents {
    private static final Set<String> JAIL_COMMAND_ALLOWLIST = Set.of(
            "msg", "tell", "w", "whisper", "r", "reply", "helpop", "rules", "info", "jails");

    private ModerationEvents() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!enabled() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        player.getServer().execute(() -> enforceSentence(player, true));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCommand(CommandEvent event) {
        if (!enabled()) {
            return;
        }
        ServerPlayer player = event.getParseResults().getContext().getSource().getPlayer();
        if (player == null || KernelServices.moderation().sentence(player.getUUID()).isEmpty()) {
            return;
        }
        String input = event.getParseResults().getReader().getString().strip();
        if (input.startsWith("/")) {
            input = input.substring(1);
        }
        int separator = input.indexOf(' ');
        String root = (separator < 0 ? input : input.substring(0, separator)).toLowerCase(Locale.ROOT);
        int namespace = root.indexOf(':');
        if (namespace >= 0) {
            root = root.substring(namespace + 1);
        }
        if (!JAIL_COMMAND_ALLOWLIST.contains(root)) {
            event.setCanceled(true);
            player.sendSystemMessage(TextFormatter.stringToFormattedText(
                    "&cThat command is unavailable while jailed."));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player && jailed(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && jailed(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && jailed(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player && jailed(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemInteract(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof ServerPlayer player && jailed(player)) {
            event.setCanceled(true);
        }
    }

    public static void tick(MinecraftServer server) {
        if (!enabled() || server.getTickCount() % 20 != 0) {
            return;
        }
        Instant now = Instant.now();
        for (ModerationRepository.Sentence expired
                : KernelServices.moderation().takeExpiredSentences(now)) {
            releaseExpired(server, expired);
        }
        KernelServices.moderation().purgeExpired(now);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            enforceSentence(player, false);
        }
    }

    private static void releaseExpired(MinecraftServer server, ModerationRepository.Sentence sentence) {
        ServerPlayer player = server.getPlayerList().getPlayer(sentence.playerId());
        if (player == null || sentence.releaseLocation() == null) {
            return;
        }
        KernelServices.safeTeleports().teleport(
                server,
                null,
                player,
                sentence.releaseLocation(),
                "jail sentence expiry",
                new SafeTeleportService.Policy(4, 256, 16, false, false, true, false, 20),
                () -> KernelServices.moderation().sentence(player.getUUID()).isEmpty());
    }

    private static void enforceSentence(ServerPlayer player, boolean force) {
        var sentence = KernelServices.moderation().sentence(player.getUUID());
        if (sentence.isEmpty()) {
            return;
        }
        var jail = KernelServices.moderation().jail(sentence.orElseThrow().jailName());
        if (jail.isEmpty()) {
            return;
        }
        SavedLocation destination = jail.orElseThrow().location();
        boolean wrongDimension = !player.serverLevel().dimension().location().toString()
                .equals(destination.dimensionId());
        double distance = wrongDimension
                ? Double.POSITIVE_INFINITY
                : player.distanceToSqr(destination.x(), destination.y(), destination.z());
        if (!force && distance <= 256.0D) {
            return;
        }
        KernelServices.safeTeleports().teleport(
                player.getServer(),
                null,
                player,
                destination,
                "jail enforcement",
                new SafeTeleportService.Policy(4, 256, 16, false, false, true, false, 20),
                () -> KernelServices.moderation().sentence(player.getUUID()).isPresent());
    }

    private static boolean jailed(ServerPlayer player) {
        return enabled() && KernelServices.moderation().sentence(player.getUUID()).isPresent();
    }

    private static boolean enabled() {
        return ConfigHandler.config.enableModerationEssentials.get()
                && ConfigHandler.config.enableJails.get();
    }
}
