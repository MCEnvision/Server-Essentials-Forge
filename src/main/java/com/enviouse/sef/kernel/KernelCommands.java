package com.enviouse.sef.kernel;

import com.enviouse.sef.audit.SecurityAuditService;
import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.kernel.command.ShortcutRegistry;
import com.enviouse.sef.permissions.PermissionService;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

import java.util.List;
import java.util.Map;
import com.enviouse.sef.storage.ImportDiagnostics;

public final class KernelCommands {
    private static final int PAGE_SIZE = 10;

    private KernelCommands() {
    }

    public static void attach(LiteralArgumentBuilder<CommandSourceStack> root) {
        KernelServices.initialize();
        root.then(Commands.literal("commands")
                .requires(source -> PermissionService.has(source, PermissionsHandler.sefCommandsCatalog))
                .executes(context -> KernelCommandExecutor.execute(
                        context.getSource(),
                        "sef:core.commands",
                        Map.of("page", "1"),
                        () -> commands(context.getSource(), 1)))
                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                        .executes(context -> {
                            int page = IntegerArgumentType.getInteger(context, "page");
                            return KernelCommandExecutor.execute(
                                    context.getSource(),
                                    "sef:core.commands",
                                    Map.of("page", Integer.toString(page)),
                                    () -> commands(context.getSource(), page));
                        })));
        root.then(Commands.literal("conflicts")
                .requires(source -> PermissionService.has(source, PermissionsHandler.sefConflicts))
                .executes(context -> KernelCommandExecutor.execute(
                        context.getSource(),
                        "sef:core.conflicts",
                        Map.of(),
                        () -> conflicts(context.getSource()))));
        root.then(Commands.literal("doctor")
                .requires(source -> PermissionService.has(source, PermissionsHandler.sefDoctor))
                .executes(context -> KernelCommandExecutor.execute(
                        context.getSource(),
                        "sef:core.doctor",
                        Map.of(),
                        () -> doctor(context.getSource()))));
    }

    private static int commands(CommandSourceStack source, int requestedPage) {
        List<CommandDefinition> visible = KernelServices.catalog().entries().stream()
                .filter(definition -> definition.permissionIds().stream().allMatch(id -> has(source, id)))
                .toList();
        int pages = Math.max(1, (visible.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int page = Math.min(requestedPage, pages);
        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(visible.size(), start + PAGE_SIZE);
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&6SEF command catalog &7page &f" + page + "&7/&f" + pages), false);
        for (int index = start; index < end; index++) {
            CommandDefinition definition = visible.get(index);
            String roots = definition.convenienceRoots().isEmpty()
                    ? ""
                    : " &8| &7/" + String.join(", /", definition.convenienceRoots());
            source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                    "&e/" + definition.canonicalRoute() + roots + " &8| &f" + definition.id()), false);
        }
        if (visible.isEmpty()) {
            source.sendSuccess(() -> TextFormatter.stringToFormattedText("&7No catalog entries are currently available."), false);
        }
        return 1;
    }

    private static int conflicts(CommandSourceStack source) {
        List<ShortcutRegistry.Diagnostic> diagnostics = KernelServices.shortcuts().diagnostics();
        long conflicts = diagnostics.stream().filter(diagnostic ->
                diagnostic.status() == ShortcutRegistry.Status.CONFLICT
                        || diagnostic.status() == ShortcutRegistry.Status.CANONICAL_ONLY
                        || diagnostic.status() == ShortcutRegistry.Status.RESTART_REQUIRED).count();
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&6SEF command roots: &e" + diagnostics.size() + " &8| &6conflicts: &e" + conflicts), false);
        for (ShortcutRegistry.Diagnostic diagnostic : diagnostics) {
            String color = switch (diagnostic.status()) {
                case ACTIVE, ACTIVE_OVERRIDE -> "&a";
                case CANONICAL_ONLY, RESTART_REQUIRED -> "&e";
                case CONFLICT -> "&c";
            };
            source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                    color + "/" + diagnostic.root() + " &8| &f" + diagnostic.actionId()
                            + " &8| &7" + diagnostic.status().name().toLowerCase(java.util.Locale.ROOT)), false);
        }
        return 1;
    }

    private static int doctor(CommandSourceStack source) {
        var catalogProblems = KernelServices.catalog().validate();
        var storage = KernelServices.storage();
        var repositories = storage.diagnostics();
        var profiles = KernelServices.profiles().diagnostic();
        var quotaProviderProblems = KernelServices.quotas().providerDiagnostics();
        var securityAudit = SecurityAuditService.health();
        source.sendSuccess(() -> TextFormatter.stringToFormattedText("&6SEF doctor"), false);
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&7Catalog entries: &f" + KernelServices.catalog().size()
                        + " &8| &7capabilities: &f" + KernelServices.capabilities().size()
                        + " &8| &7shortcuts: &f" + KernelServices.shortcuts().size()), false);
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&7Policies: &f" + KernelServices.commandPolicies().policies().size()
                        + " &8| &7quotas: &f" + KernelServices.quotas().definitions().size()
                        + " &8| &7policy revision: &f" + KernelServices.commandPolicies().revision()), false);
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&7Repositories: &f" + repositories.size()
                        + " &8| &7recovery mode: " + (storage.recoveryMode() ? "&cactive" : "&ainactive")), false);
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&7Player profiles: &f" + profiles.profileCount()
                        + " &8| &7state: &f"
                        + profiles.state().name().toLowerCase(java.util.Locale.ROOT)), false);
        List<ImportDiagnostics.Entry> imports = ImportDiagnostics.snapshot();
        long importFailures = imports.stream().filter(entry ->
                entry.result() == ImportDiagnostics.Result.FAILED
                        || entry.result() == ImportDiagnostics.Result.REJECTED).count();
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&7Import diagnostics: &f" + imports.size()
                        + " &8| &7failures: " + (importFailures == 0 ? "&a0" : "&c" + importFailures)), false);
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&7Quota provider failures: "
                        + (quotaProviderProblems.isEmpty() ? "&a0" : "&c" + quotaProviderProblems.size())), false);
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                "&7Security audit: "
                        + (securityAudit.running() && securityAudit.writerAlive()
                        && securityAudit.failures() == 0L && securityAudit.dropped() == 0L
                        ? "&ahealthy"
                        : "&crequires attention")), false);
        for (var repository : repositories) {
            source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                    "&7" + repository.id() + " &8| &f" + repository.state().name().toLowerCase(java.util.Locale.ROOT)
                            + (repository.dirty() ? " &8| &edirty" : "")), false);
        }
        for (var problem : catalogProblems) {
            source.sendFailure(TextFormatter.stringToFormattedText(
                    "&c" + problem.ownerId() + ": " + problem.message()));
        }
        for (var problem : quotaProviderProblems) {
            source.sendFailure(TextFormatter.stringToFormattedText(
                    "&c" + problem.providerId() + ": " + problem.detail()));
        }
        boolean healthy = catalogProblems.isEmpty()
                && quotaProviderProblems.isEmpty()
                && profiles.state() != com.enviouse.sef.storage.repository.StorageRepository.RepositoryState.RECOVERY
                && profiles.state() != com.enviouse.sef.storage.repository.StorageRepository.RepositoryState.UNSUPPORTED
                && profiles.state() != com.enviouse.sef.storage.repository.StorageRepository.RepositoryState.ERROR
                && securityAudit.running()
                && securityAudit.writerAlive()
                && securityAudit.failures() == 0L
                && securityAudit.dropped() == 0L
                && !storage.recoveryMode();
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(
                healthy ? "&aNo kernel errors detected." : "&eKernel requires operator attention."), false);
        return 1;
    }

    private static boolean has(CommandSourceStack source, String permissionId) {
        PermissionNode<Boolean> node = KernelServices.permissionNode(permissionId);
        return node != null && PermissionService.has(source, node);
    }
}
