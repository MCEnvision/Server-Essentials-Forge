package com.enviouse.sef.kernel;

import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.identity.IdentityService;
import com.enviouse.sef.identity.PlayerProfileRepository;
import com.enviouse.sef.kernel.command.AliasCompiler;
import com.enviouse.sef.kernel.command.BundleCompiler;
import com.enviouse.sef.kernel.command.CapabilityManifest;
import com.enviouse.sef.kernel.command.CommandCatalog;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.kernel.command.PanelContracts;
import com.enviouse.sef.kernel.command.ShortcutRegistry;
import com.enviouse.sef.kernel.policy.CommandPolicyService;
import com.enviouse.sef.kernel.policy.CommandExecutionService;
import com.enviouse.sef.kernel.policy.ConfirmationService;
import com.enviouse.sef.kernel.policy.CooldownService;
import com.enviouse.sef.kernel.policy.CostService;
import com.enviouse.sef.kernel.policy.FeatureGateService;
import com.enviouse.sef.kernel.policy.QuotaService;
import com.enviouse.sef.kernel.policy.TargetHierarchyService;
import com.enviouse.sef.kernel.policy.WarmupService;
import com.enviouse.sef.message.MessageService;
import com.enviouse.sef.permissions.PermissionManifest;
import com.enviouse.sef.storage.repository.CooldownRepository;
import com.enviouse.sef.storage.repository.LocationHistoryRepository;
import com.enviouse.sef.storage.repository.StorageCoordinator;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public final class KernelServices {
    private static final AtomicLong CONFIG_REVISION = new AtomicLong(1L);

    private static boolean initialized;
    private static CapabilityManifest capabilities;
    private static PanelContracts.Registry descriptors;
    private static CommandCatalog catalog;
    private static ShortcutRegistry shortcuts;
    private static FeatureGateService featureGates;
    private static QuotaService quotas;
    private static TargetHierarchyService hierarchy;
    private static CommandPolicyService commandPolicies;
    private static CommandExecutionService commandExecutions;
    private static CooldownService cooldowns;
    private static WarmupService warmups;
    private static ConfirmationService confirmations;
    private static CostService costs;
    private static MessageService messages;
    private static PlayerProfileRepository profiles;
    private static IdentityService identities;
    private static StorageCoordinator storage;
    private static LocationHistoryRepository locationHistory;
    private static CooldownRepository cooldownRepository;
    private static Map<String, PermissionNode<Boolean>> permissionNodes;
    private static AliasCompiler.Registry aliases;
    private static BundleCompiler bundleCompiler;

    private KernelServices() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        PermissionsHandler.sefCommand.toString();

        capabilities = new CapabilityManifest();
        permissionNodes = new LinkedHashMap<>();
        for (PermissionManifest.Definition definition : PermissionManifest.definitions()) {
            capabilities.register(new CapabilityManifest.Capability(
                    definition.id(),
                    CapabilityManifest.inferType(definition.id()),
                    definition.defaultValue(),
                    definition.name(),
                    definition.description()));
            permissionNodes.put(definition.id(), definition.node());
        }

        descriptors = new PanelContracts.Registry();
        descriptors.registerCommandOnly("sef:core");
        descriptors.registerCommandOnly("sef:workstations");

        catalog = new CommandCatalog(capabilities, descriptors);
        registerCoreCommands();
        catalog.seal();

        shortcuts = new ShortcutRegistry(catalog, capabilities);
        registerShortcuts();

        featureGates = new FeatureGateService();
        quotas = new QuotaService();
        quotas.setProviders(List.of(new QuotaService.ContextMetadataProvider()));
        registerQuotas();
        hierarchy = new TargetHierarchyService();
        commandPolicies = new CommandPolicyService(featureGates);
        cooldowns = new CooldownService();
        registerCooldownAliases();
        warmups = new WarmupService();
        confirmations = new ConfirmationService();
        costs = new CostService.Disabled();
        commandExecutions = new CommandExecutionService(
                commandPolicies,
                cooldowns,
                costs,
                warmups,
                confirmations);
        messages = new MessageService();
        profiles = new PlayerProfileRepository();
        identities = new IdentityService(ServerLifecycleHooks::getCurrentServer, profiles);

        bundleCompiler = new BundleCompiler(
                catalog,
                ConfigHandler.config.kernelMaximumBundleSteps.get(),
                ConfigHandler.config.kernelMaximumBundleDepth.get(),
                ConfigHandler.config.kernelMaximumTargets.get(),
                ConfigHandler.config.kernelMaximumTargetSteps.get());
        AliasCompiler aliasCompiler = new AliasCompiler(catalog, capabilities, Set.of(), Map.of());
        aliases = new AliasCompiler.Registry(aliasCompiler, ConfigHandler.config.kernelMaximumAliases.get());

        storage = new StorageCoordinator();
        locationHistory = new LocationHistoryRepository(ConfigHandler.config.kernelLocationHistoryEntries.get());
        cooldownRepository = new CooldownRepository(
                cooldowns,
                Duration.ofSeconds(ConfigHandler.config.kernelPersistentCooldownMinimumSeconds.get()));
        storage.register(locationHistory);
        storage.register(cooldownRepository);

        initialized = true;
        reloadConfiguration();
    }

    public static synchronized void reloadConfiguration() {
        ensureInitialized();
        long revision = CONFIG_REVISION.incrementAndGet();
        Map<String, Boolean> features = Map.of(
                "sef.core", true,
                "sef.workstation.craft", ConfigHandler.config.enableCraftingTableCommand.get(),
                "sef.workstation.anvil", ConfigHandler.config.enableAnvilCommand.get(),
                "sef.workstation.enchant", ConfigHandler.config.enableEnchantingTableCommand.get(),
                "sef.workstation.super_enchant", ConfigHandler.config.enableSuperEnchantingTableCommand.get(),
                "sef.workstation.repair", ConfigHandler.config.enableRepairCommand.get());
        featureGates.publish(new FeatureGateService.Snapshot(revision, features, Map.of(), Map.of()));

        List<CommandPolicyService.Policy> policies = new ArrayList<>();
        for (CommandDefinition definition : catalog.entries()) {
            policies.add(new CommandPolicyService.Policy(
                    definition.id(),
                    definition.featureId(),
                    definition.sourceTypes(),
                    false,
                    definition.confirmationRequired(),
                    cooldownFor(definition.id()),
                    Duration.ZERO,
                    BigDecimal.ZERO,
                    definition.auditClass()));
        }
        commandPolicies.replaceAll(policies);
        quotas.invalidate();
    }

    public static synchronized void startStorage(Path managedRoot) {
        ensureInitialized();
        if (!storage.started()) {
            storage.start(managedRoot);
        }
    }

    public static synchronized StorageCoordinator.FlushResult shutdown() {
        ensureInitialized();
        StorageCoordinator.FlushResult result = storage.shutdown();
        warmups.clear();
        confirmations.clear();
        cooldowns.clearAll();
        return result;
    }

    public static CapabilityManifest capabilities() {
        ensureInitialized();
        return capabilities;
    }

    public static CommandCatalog catalog() {
        ensureInitialized();
        return catalog;
    }

    public static ShortcutRegistry shortcuts() {
        ensureInitialized();
        return shortcuts;
    }

    public static FeatureGateService featureGates() {
        ensureInitialized();
        return featureGates;
    }

    public static QuotaService quotas() {
        ensureInitialized();
        return quotas;
    }

    public static synchronized void installQuotaProvider(QuotaService.Provider provider) {
        ensureInitialized();
        quotas.setProviders(List.of(
                provider,
                new QuotaService.ContextMetadataProvider()));
    }

    public static synchronized void resetQuotaProviders() {
        ensureInitialized();
        quotas.setProviders(List.of(new QuotaService.ContextMetadataProvider()));
    }

    public static TargetHierarchyService hierarchy() {
        ensureInitialized();
        return hierarchy;
    }

    public static CommandPolicyService commandPolicies() {
        ensureInitialized();
        return commandPolicies;
    }

    public static CommandExecutionService commandExecutions() {
        ensureInitialized();
        return commandExecutions;
    }

    public static CooldownService cooldowns() {
        ensureInitialized();
        return cooldowns;
    }

    public static WarmupService warmups() {
        ensureInitialized();
        return warmups;
    }

    public static ConfirmationService confirmations() {
        ensureInitialized();
        return confirmations;
    }

    public static CostService costs() {
        ensureInitialized();
        return costs;
    }

    public static MessageService messages() {
        ensureInitialized();
        return messages;
    }

    public static PlayerProfileRepository profiles() {
        ensureInitialized();
        return profiles;
    }

    public static IdentityService identities() {
        ensureInitialized();
        return identities;
    }

    public static StorageCoordinator storage() {
        ensureInitialized();
        return storage;
    }

    public static LocationHistoryRepository locationHistory() {
        ensureInitialized();
        return locationHistory;
    }

    public static AliasCompiler.Registry aliases() {
        ensureInitialized();
        return aliases;
    }

    public static BundleCompiler bundleCompiler() {
        ensureInitialized();
        return bundleCompiler;
    }

    public static PermissionNode<Boolean> permissionNode(String id) {
        ensureInitialized();
        return permissionNodes.get(id);
    }

    private static void registerCoreCommands() {
        register("sef:core.info", "sef info", Set.of(), "sef.commands.sef.allowed", "sef.commands.sef.info",
                CommandDefinition.AccessClass.PLAYER, Set.of(CommandDefinition.SourceType.PLAYER, CommandDefinition.SourceType.CONSOLE),
                "sef.core", AuditService.AuditClass.METADATA_ONLY, "sef:core", "informational command has no persistent hud");
        register("sef:core.colors", "sef colors", Set.of("colors"), "sef.commands.sef.allowed", "sef.commands.sef.colors",
                CommandDefinition.AccessClass.PLAYER, Set.of(CommandDefinition.SourceType.PLAYER, CommandDefinition.SourceType.CONSOLE),
                "sef.core", AuditService.AuditClass.METADATA_ONLY, "sef:core", "informational command has no persistent hud");
        register("sef:core.test", "sef test", Set.of(), "sef.commands.sef.allowed", "sef.commands.sef.test",
                CommandDefinition.AccessClass.ADMINISTRATOR, Set.of(CommandDefinition.SourceType.PLAYER, CommandDefinition.SourceType.CONSOLE),
                "sef.core", AuditService.AuditClass.ADMIN_ACTION, "sef:core", "debug command has no persistent hud");
        register("sef:core.reload", "sef reload", Set.of(), "sef.commands.sef.allowed", "sef.commands.sef.reload",
                CommandDefinition.AccessClass.ADMINISTRATOR, Set.of(CommandDefinition.SourceType.PLAYER, CommandDefinition.SourceType.CONSOLE),
                "sef.core", AuditService.AuditClass.CONFIG_DEFINITION, "sef:core", "reload result is immediate");
        register("sef:core.commands", "sef commands", Set.of(), "sef.commands.sef.allowed", "sef.commands.sef.commands",
                CommandDefinition.AccessClass.PLAYER, Set.of(CommandDefinition.SourceType.PLAYER, CommandDefinition.SourceType.CONSOLE),
                "sef.core", AuditService.AuditClass.METADATA_ONLY, "sef:core", "catalog view has no persistent hud");
        register("sef:core.conflicts", "sef conflicts", Set.of(), "sef.commands.sef.allowed", "sef.commands.sef.conflicts",
                CommandDefinition.AccessClass.ADMINISTRATOR, Set.of(CommandDefinition.SourceType.PLAYER, CommandDefinition.SourceType.CONSOLE),
                "sef.core", AuditService.AuditClass.SENSITIVE_ACCESS, "sef:core", "diagnostic view has no persistent hud");
        register("sef:core.doctor", "sef doctor", Set.of(), "sef.commands.sef.allowed", "sef.commands.sef.doctor",
                CommandDefinition.AccessClass.ADMINISTRATOR, Set.of(CommandDefinition.SourceType.PLAYER, CommandDefinition.SourceType.CONSOLE),
                "sef.core", AuditService.AuditClass.SENSITIVE_ACCESS, "sef:core", "diagnostic view has no persistent hud");

        register("sef:workstation.craft", "sef workstation craft", Set.of("craft", "c"), "sef.commands.craft",
                CommandDefinition.AccessClass.PLAYER, "sef.workstation.craft");
        register("sef:workstation.anvil", "sef workstation anvil", Set.of("anvil", "av"), "sef.commands.anvil",
                CommandDefinition.AccessClass.PLAYER, "sef.workstation.anvil");
        register("sef:workstation.enchant", "sef workstation enchant", Set.of("enchantingtable", "et"),
                "sef.commands.enchantingtable", CommandDefinition.AccessClass.PLAYER, "sef.workstation.enchant");
        register("sef:workstation.super_enchant", "sef workstation super_enchant", Set.of("superenchantingtable", "set"),
                "sef.commands.superenchantingtable", CommandDefinition.AccessClass.TRUSTED_PLAYER, "sef.workstation.super_enchant");
        register("sef:workstation.repair", "sef workstation repair", Set.of("repair"), "sef.commands.repair",
                CommandDefinition.AccessClass.TRUSTED_PLAYER, "sef.workstation.repair");
    }

    private static void register(
            String id,
            String route,
            Set<String> roots,
            String rootPermission,
            String actionPermission,
            CommandDefinition.AccessClass access,
            Set<CommandDefinition.SourceType> sources,
            String feature,
            AuditService.AuditClass auditClass,
            String gui,
            String hudReason
    ) {
        catalog.register(new CommandDefinition(
                id,
                route,
                roots,
                "command." + id.replace(':', '.').replace('/', '.') + ".description",
                "command." + id.replace(':', '.').replace('/', '.') + ".usage",
                route.startsWith("sef workstation") ? "workstations" : "sef",
                feature,
                Set.of(rootPermission, actionPermission),
                access,
                sources,
                CommandDefinition.TargetBehavior.NONE,
                id,
                false,
                auditClass,
                gui,
                "",
                hudReason,
                "",
                "command does not create retained records or variable fan out",
                CommandDefinition.ConflictPolicy.CANONICAL_ONLY,
                true,
                true));
    }

    private static void register(
            String id,
            String route,
            Set<String> roots,
            String permission,
            CommandDefinition.AccessClass access,
            String feature
    ) {
        catalog.register(new CommandDefinition(
                id,
                route,
                roots,
                "command." + id.replace(':', '.') + ".description",
                "command." + id.replace(':', '.') + ".usage",
                "workstations",
                feature,
                Set.of(permission),
                access,
                Set.of(CommandDefinition.SourceType.PLAYER),
                CommandDefinition.TargetBehavior.SELF,
                id,
                false,
                AuditService.AuditClass.METADATA_ONLY,
                "sef:workstations",
                "",
                "virtual workstation actions are immediate",
                "",
                "virtual workstation actions do not retain records or fan out",
                CommandDefinition.ConflictPolicy.PREFER_SEF,
                true,
                true));
    }

    private static void registerShortcuts() {
        for (CommandDefinition definition : catalog.entries()) {
            for (String root : definition.convenienceRoots()) {
                if (!shortcutEnabled(root)) {
                    continue;
                }
                shortcuts.register(new ShortcutRegistry.Shortcut(
                        root,
                        definition.id(),
                        ShortcutRegistry.ArgumentAdapter.NONE,
                        root.equals("colors") ? "sef.commands.colors" : "",
                        definition.conflictPolicy(),
                        1L));
            }
        }
    }

    private static boolean shortcutEnabled(String root) {
        return switch (root) {
            case "colors" -> ConfigHandler.config.enableColorsCommand.get();
            case "craft" -> ConfigHandler.config.enableCraftingTableCommand.get();
            case "c" -> ConfigHandler.config.enableCraftingTableCommand.get()
                    && ConfigHandler.config.enableCraftAlias.get();
            case "anvil" -> ConfigHandler.config.enableAnvilCommand.get();
            case "av" -> ConfigHandler.config.enableAnvilCommand.get()
                    && ConfigHandler.config.enableAnvilAlias.get();
            case "enchantingtable" -> ConfigHandler.config.enableEnchantingTableCommand.get();
            case "et" -> ConfigHandler.config.enableEnchantingTableCommand.get()
                    && ConfigHandler.config.enableEnchantingTableAlias.get();
            case "superenchantingtable" -> ConfigHandler.config.enableSuperEnchantingTableCommand.get();
            case "set" -> ConfigHandler.config.enableSuperEnchantingTableCommand.get()
                    && ConfigHandler.config.enableSuperEnchantingTableAlias.get();
            case "repair" -> ConfigHandler.config.enableRepairCommand.get();
            default -> true;
        };
    }

    private static void registerQuotas() {
        quotas.register(new QuotaService.Definition(
                "sef:homes",
                QuotaService.QuotaKind.COUNT,
                1,
                1000,
                true,
                Map.of("sef.homes.3", 3L, "sef.homes.5", 5L, "sef.homes.10", 10L)));
        quotas.register(new QuotaService.Definition(
                "sef:player_warps",
                QuotaService.QuotaKind.COUNT,
                5,
                1000,
                true,
                Map.of("sef.playerwarps.10", 10L, "sef.playerwarps.25", 25L)));
        quotas.register(new QuotaService.Definition(
                "sef:targets",
                QuotaService.QuotaKind.TARGET_CAP,
                1,
                1000,
                false,
                Map.of("sef.targets.10", 10L, "sef.targets.100", 100L)));
        quotas.register(new QuotaService.Definition(
                "sef:mail",
                QuotaService.QuotaKind.COUNT,
                100,
                10_000,
                false,
                Map.of("sef.mail.500", 500L, "sef.mail.1000", 1000L)));
        quotas.register(new QuotaService.Definition(
                "sef:definitions",
                QuotaService.QuotaKind.DEFINITION_COUNT,
                64,
                1024,
                false,
                Map.of("sef.definitions.256", 256L, "sef.definitions.512", 512L)));
    }

    private static void registerCooldownAliases() {
        for (ShortcutRegistry.Shortcut shortcut : shortcuts.activeAliasMap().entrySet().stream()
                .map(entry -> shortcuts.find(entry.getKey()).orElseThrow())
                .toList()) {
            cooldowns.registerAlias(shortcut.root(), shortcut.actionId());
        }
    }

    private static Duration cooldownFor(String actionId) {
        return switch (actionId) {
            case "sef:workstation.craft" -> Duration.ofSeconds(ConfigHandler.config.craftingTableCooldownSeconds.get());
            case "sef:workstation.anvil" -> Duration.ofSeconds(ConfigHandler.config.anvilCooldownSeconds.get());
            case "sef:workstation.enchant" -> Duration.ofSeconds(ConfigHandler.config.enchantingTableCooldownSeconds.get());
            case "sef:workstation.super_enchant" -> Duration.ofSeconds(ConfigHandler.config.superEnchantingTableCooldownSeconds.get());
            case "sef:workstation.repair" -> Duration.ofSeconds(ConfigHandler.config.repairCooldownSeconds.get());
            default -> Duration.ZERO;
        };
    }

    private static void ensureInitialized() {
        if (!initialized) {
            initialize();
        }
    }
}
