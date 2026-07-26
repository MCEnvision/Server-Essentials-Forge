package com.enviouse.sef.storage;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.audit.SecurityAuditService;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.permissions.PermissionService;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.io.IOException;
import java.nio.file.Path;

public final class StorageCommands {
    private StorageCommands() {
    }

    public static void attach(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("storage")
                .then(Commands.literal("status")
                        .requires(source -> PermissionService.has(source, PermissionsHandler.storageStatus))
                        .executes(context -> status(context.getSource())))
                .then(Commands.literal("export")
                        .requires(source -> PermissionService.has(source, PermissionsHandler.storageExport))
                        .executes(context -> export(context.getSource()))));
    }

    private static int status(CommandSourceStack source) {
        var statuses = StorageService.statuses();
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&6Managed SEF storage: &e" + statuses.size() + " &6document(s)"), false);
        for (StorageService.StoreStatus status : statuses) {
            String line = "&7" + status.path().getFileName()
                    + " &8| &f" + status.domain()
                    + " &8| &e" + status.state()
                    + " &8| &f" + status.sizeBytes() + " bytes";
            source.sendSuccess(() -> TextFormatter.stringToFormattedText(line), false);
        }
        return 1;
    }

    private static int export(CommandSourceStack source) {
        Path managedRoot = source.getServer().getServerDirectory()
                .resolve("serverconfig").resolve("sef");
        Path exportRoot = managedRoot.resolve("exports");
        boolean mayExportAlts = PermissionService.has(source, PermissionsHandler.checkAltsExport)
                && PermissionService.has(source, PermissionsHandler.checkAltsIpView);
        String issuer = source.getTextName();
        boolean accepted = StorageExportService.submit(() -> {
            try {
                Path snapshot = StorageService.exportManagedSnapshot(
                        java.util.List.of(
                                managedRoot,
                                source.getServer().getServerDirectory().resolve("config").resolve("sef")),
                        exportRoot,
                        status -> mayExportAlts || !status.path().getFileName().toString().equals("alt_data.json"));
                SecurityAuditService.record(SecurityAuditService.AuditEvent.create(
                        "storage",
                        "export",
                        issuer,
                        snapshot.getFileName().toString(),
                        "sef storage",
                        "success",
                        mayExportAlts ? "alternate account data included" : "alternate account data excluded"));
                source.getServer().execute(() -> source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                        "&aExported managed storage to &e" + snapshot.getFileName()), false));
            } catch (IOException exception) {
                SecurityAuditService.record(SecurityAuditService.AuditEvent.create(
                        "storage",
                        "export",
                        issuer,
                        "",
                        "sef storage",
                        "failed",
                        exception.getClass().getSimpleName()));
                source.getServer().execute(() -> source.sendFailure(
                        TextFormatter.stringToFormattedText("&cManaged storage export failed.")));
            }
        });
        if (!accepted) {
            source.sendFailure(TextFormatter.stringToFormattedText("&cThe storage export queue is full."));
            return 0;
        }
        source.sendSuccess(() -> TextFormatter.stringToFormattedText("&aStorage export queued."), false);
        return 1;
    }
}
