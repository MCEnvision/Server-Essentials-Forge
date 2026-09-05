package com.enviouse.sef.alts;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.audit.SecurityAuditService;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.events.CommandRegistrationHandler;
import com.enviouse.sef.identity.IdentityArguments;
import com.enviouse.sef.kernel.CommandAuditScope;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.storage.StorageExportService;
import com.enviouse.sef.vanish.VanishUtil;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;

/**
 * Registers /checkalts command.
 *
 * Usage:
 *   /checkalts <player>  — shows all accounts that share the same IP
 *
 * All messages are config-driven.
 */
public class CheckAltsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("checkalts")
            .requires(src -> PermissionService.has(src, PermissionsHandler.checkAltsCommand))
            .then(Commands.literal("purge")
                .requires(src -> PermissionService.has(src, PermissionsHandler.checkAltsPurge))
                .then(Commands.literal("expired")
                    .executes(ctx -> purgeExpired(ctx.getSource())))
                .then(Commands.literal("confirm")
                    .executes(ctx -> purgeAll(ctx.getSource()))))
            .then(Commands.literal("export")
                .requires(src -> PermissionService.has(src, PermissionsHandler.checkAltsExport))
                .executes(ctx -> export(ctx.getSource())))
            .then(IdentityArguments.online("player")
                .executes(ctx -> {
                    ServerPlayer target = IdentityArguments.getOnline(ctx, "player");
                    return executeCheckAlts(ctx.getSource(), target);
                })));
    }

    private static int executeCheckAlts(CommandSourceStack source, ServerPlayer target) {
        return KernelCommandExecutor.execute(
                source,
                "sef:identity.alts",
                Map.of("route", "inspect"),
                List.of(target.getUUID()),
                false,
                () -> {
                    if (!eligible(source, target)) {
                        source.sendFailure(TextFormatter.stringToFormattedText("&cThat player is unavailable."));
                        audit(source, "inspect", "target denied");
                        return 0;
                    }
                    AltTracker tracker = CommandRegistrationHandler.getAltTracker();
                    if (tracker == null) {
                        source.sendFailure(TextFormatter.stringToFormattedText("&cAlt tracking is not initialized."));
                        return 0;
                    }
                    List<AltTracker.AltEntry> alts = tracker.getAltsForPlayer(target.getUUID());
                    boolean mayViewRawAddress = PermissionService.has(source, PermissionsHandler.checkAltsIpView);
                    String address = tracker.getAddressDisplay(target.getUUID(), mayViewRawAddress);
                    if (alts.isEmpty() || alts.size() <= 1) {
                        String msg = ConfigHandler.config.checkAltsNoAltsMsg.get()
                                .replace("$player", target.getGameProfile().getName());
                        source.sendSuccess(() -> TextFormatter.stringToFormattedText(msg), false);
                        return 1;
                    }
                    String header = ConfigHandler.config.checkAltsHeaderFormat.get()
                            .replace("$player", target.getGameProfile().getName())
                            .replace("$ip", address);
                    source.sendSuccess(() -> TextFormatter.stringToFormattedText(header), false);
                    for (AltTracker.AltEntry entry : alts) {
                        if (entry.uuid.equals(target.getUUID().toString())) continue;
                        String line = ConfigHandler.config.checkAltsEntryFormat.get()
                                .replace("$name", entry.name)
                                .replace("$uuid", entry.uuid)
                                .replace("$lastseen", entry.lastSeen != null ? entry.lastSeen : "unknown");
                        source.sendSuccess(() -> TextFormatter.stringToFormattedText(line), false);
                    }
                    return 1;
                });
    }

    private static boolean eligible(CommandSourceStack source, ServerPlayer target) {
        if (source.getPlayer() != null && VanishUtil.isVanished(target, source.getPlayer())) {
            return false;
        }
        return PlayerTargetPolicy.decide(
                source,
                target,
                PermissionsHandler.phasePermission("checkalts.hierarchy.bypass"),
                PermissionsHandler.phasePermission("checkalts.exempt"),
                PermissionsHandler.phasePermission("checkalts.bypass.exempt"),
                false,
                true).allowed();
    }

    private static int purgeExpired(CommandSourceStack source) {
        return KernelCommandExecutor.execute(
                source,
                "sef:identity.alts",
                Map.of("route", "purge expired"),
                () -> {
                    AltTracker tracker = CommandRegistrationHandler.getAltTracker();
                    if (tracker == null) {
                        source.sendFailure(TextFormatter.stringToFormattedText("&cAlt tracking is not initialized."));
                        return 0;
                    }
                    int removed;
                    try {
                        removed = tracker.purgeExpiredRecords();
                    } catch (IllegalStateException exception) {
                        source.sendFailure(TextFormatter.stringToFormattedText(
                                "&cExpired records could not be purged. &7" + exception.getMessage()));
                        return 0;
                    }
                    audit(source, "purge expired", Integer.toString(removed));
                    source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                            "&aPurged &e" + removed + " &aexpired alternate account record(s)."), false);
                    return 1;
                },
                PermissionsHandler.checkAltsPurge);
    }

    private static int purgeAll(CommandSourceStack source) {
        return KernelCommandExecutor.execute(
                source,
                "sef:identity.alts",
                Map.of("route", "purge all"),
                () -> {
                    AltTracker tracker = CommandRegistrationHandler.getAltTracker();
                    if (tracker == null) {
                        source.sendFailure(TextFormatter.stringToFormattedText("&cAlt tracking is not initialized."));
                        return 0;
                    }
                    int removed;
                    try {
                        removed = tracker.purgeAll();
                    } catch (IllegalStateException exception) {
                        source.sendFailure(TextFormatter.stringToFormattedText(
                                "&cAlternate account records could not be purged. &7" + exception.getMessage()));
                        return 0;
                    }
                    audit(source, "purge all", Integer.toString(removed));
                    source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                            "&aPurged &e" + removed + " &aalternate account record(s)."), false);
                    return 1;
                },
                PermissionsHandler.checkAltsPurge);
    }

    private static int export(CommandSourceStack source) {
        return KernelCommandExecutor.execute(
                source,
                "sef:identity.alts",
                Map.of("route", "export"),
                () -> {
                    AltTracker tracker = CommandRegistrationHandler.getAltTracker();
                    if (tracker == null) {
                        source.sendFailure(TextFormatter.stringToFormattedText("&cAlt tracking is not initialized."));
                        return 0;
                    }
                    boolean includeRaw = PermissionService.has(source, PermissionsHandler.checkAltsIpView);
                    java.nio.file.Path directory = source.getServer().getServerDirectory()
                            .resolve("serverconfig").resolve("sef").resolve("exports");
                    com.google.gson.JsonObject snapshot = tracker.buildExport(includeRaw);
                    String issuer = source.getTextName();
                    boolean accepted = StorageExportService.submit(() -> {
                        try {
                            java.nio.file.Path exported = AltTracker.writeExport(directory, snapshot);
                            SecurityAuditService.record(SecurityAuditService.AuditEvent.create(
                                    "privacy", "export", issuer, "alternate account records", "checkalts",
                                    "success", includeRaw ? "raw permitted" : "redacted"));
                            source.getServer().execute(() -> source.sendSuccess(
                                    () -> TextFormatter.stringToFormattedText(
                                            "&aExported alternate account data to &e" + exported.getFileName()), false));
                        } catch (java.io.IOException exception) {
                            SecurityAuditService.record(SecurityAuditService.AuditEvent.create(
                                    "privacy", "export", issuer, "alternate account records", "checkalts",
                                    "failed", exception.getClass().getSimpleName()));
                            source.getServer().execute(() -> source.sendFailure(
                                    TextFormatter.stringToFormattedText("&cAlternate account export failed.")));
                        }
                    });
                    if (!accepted) {
                        source.sendFailure(TextFormatter.stringToFormattedText("&cThe export queue is full."));
                        return 0;
                    }
                    source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                            "&aAlternate account export queued."), false);
                    return 1;
                },
                PermissionsHandler.checkAltsExport);
    }

    private static void audit(CommandSourceStack source, String action, String result) {
        if (CommandAuditScope.active()) {
            return;
        }
        SecurityAuditService.record(SecurityAuditService.AuditEvent.create(
                "privacy",
                action,
                source.getTextName(),
                "alternate account records",
                "checkalts",
                result,
                ""));
    }
}
