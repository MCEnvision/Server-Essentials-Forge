package com.enviouse.sef.storage;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.audit.SecurityAuditService;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.CommandAuditScope;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.permissions.PermissionService;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public final class StorageCommands {
    private StorageCommands() {
    }

    public static void attach(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("storage")
                .requires(source -> PermissionService.has(source, PermissionsHandler.storageStatus)
                        || PermissionService.has(source, PermissionsHandler.storageExport))
                .then(Commands.literal("status")
                        .requires(source -> PermissionService.has(source, PermissionsHandler.storageStatus))
                        .executes(context -> KernelCommandExecutor.execute(
                                context.getSource(),
                                "sef:storage.status",
                                Map.of(),
                                () -> status(context.getSource()))))
                .then(Commands.literal("export")
                        .requires(source -> PermissionService.has(source, PermissionsHandler.storageExport))
                        .executes(context -> KernelCommandExecutor.execute(
                                context.getSource(),
                                "sef:storage.export",
                                Map.of("operation", "queue_export"),
                                () -> export(context.getSource())))));
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
        java.util.UUID parentJobId = CommandAuditScope.currentCorrelationId().orElse(null);
        java.util.UUID actorId = KernelCommandExecutor.actorId(source);
        String sourceType = KernelCommandExecutor.sourceType(source).name();
        boolean accepted = StorageExportService.submit(() -> {
            try {
                Path snapshot = StorageService.exportManagedSnapshot(
                        java.util.List.of(
                                managedRoot,
                                source.getServer().getServerDirectory().resolve("config").resolve("sef")),
                        exportRoot,
                        status -> mayExportAlts || !status.path().getFileName().toString().equals("alt_data.json"));
                AuditService.record(AuditService.Event.completion(
                        SecurityAuditService.currentSessionId(),
                        actorId,
                        issuer,
                        sourceType,
                        "sef:storage.export",
                        Map.of("export_file", snapshot.getFileName().toString()),
                        AuditService.Result.SUCCESS,
                        ActionResult.ReasonCode.SUCCESS,
                        "command",
                        parentJobId,
                        AuditService.AuditClass.SENSITIVE_ACCESS));
                source.getServer().execute(() -> source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                        "&aExported managed storage to &e" + snapshot.getFileName()), false));
            } catch (IOException exception) {
                AuditService.record(AuditService.Event.completion(
                        SecurityAuditService.currentSessionId(),
                        actorId,
                        issuer,
                        sourceType,
                        "sef:storage.export",
                        Map.of("failure", exception.getClass().getSimpleName()),
                        AuditService.Result.FAILED,
                        ActionResult.ReasonCode.STORAGE_ERROR,
                        "command",
                        parentJobId,
                        AuditService.AuditClass.SENSITIVE_ACCESS));
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
