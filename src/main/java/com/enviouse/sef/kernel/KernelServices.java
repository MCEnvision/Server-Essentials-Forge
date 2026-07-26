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
import com.enviouse.sef.teleport.SafeTeleportService;
import com.enviouse.sef.teleport.TeleportRepository;
import com.enviouse.sef.teleport.TeleportRequestService;
import com.enviouse.sef.teleport.TeleportSettings;
import com.enviouse.sef.social.ObservationService;
import com.enviouse.sef.social.SocialRepository;
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
    private static final Set<CommandDefinition.SourceType> STANDARD_COMMAND_SOURCES = Set.of(
            CommandDefinition.SourceType.PLAYER,
            CommandDefinition.SourceType.CONSOLE,
            CommandDefinition.SourceType.RCON);

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
    private static TeleportRepository teleports;
    private static SafeTeleportService safeTeleports;
    private static TeleportRequestService teleportRequests;
    private static TeleportSettings teleportSettings;
    private static SocialRepository social;
    private static ObservationService observations;
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
        descriptors.registerCommandOnly("sef:teleports");
        descriptors.registerCommandOnly("sef:social");

        catalog = new CommandCatalog(capabilities, descriptors);
        registerCoreCommands();
        registerTeleportCommands();
        registerSocialCommands();
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
        social = new SocialRepository();
        observations = new ObservationService(social, messages);
        profiles = new PlayerProfileRepository();
        identities = new IdentityService(ServerLifecycleHooks::getCurrentServer, profiles);

        bundleCompiler = new BundleCompiler(
                catalog,
                ConfigHandler.config.kernelMaximumBundleSteps.get(),
                ConfigHandler.config.kernelMaximumBundleDepth.get(),
                ConfigHandler.config.kernelMaximumTargets.get(),
                ConfigHandler.config.kernelMaximumTargetSteps.get());
        AliasCompiler aliasCompiler = new AliasCompiler(
                catalog,
                capabilities,
                Set.of(),
                Map.of(),
                root -> catalog.rootOwner(root)
                        .map(owner -> new AliasCompiler.RootOwnership(
                                AliasCompiler.RootOwnerKind.CATALOG,
                                owner))
                        .orElseGet(() -> shortcuts.find(root)
                                .map(shortcut -> new AliasCompiler.RootOwnership(
                                        AliasCompiler.RootOwnerKind.SHORTCUT,
                                        shortcut.actionId()))
                                .orElseGet(() -> shortcuts.existedBeforeRegistration(root)
                                        ? new AliasCompiler.RootOwnership(
                                                AliasCompiler.RootOwnerKind.EXTERNAL,
                                                "brigadier:" + root)
                                        : null)));
        aliases = new AliasCompiler.Registry(aliasCompiler, ConfigHandler.config.kernelMaximumAliases.get());

        storage = new StorageCoordinator();
        locationHistory = new LocationHistoryRepository(ConfigHandler.config.kernelLocationHistoryEntries.get());
        cooldownRepository = new CooldownRepository(
                cooldowns,
                Duration.ofSeconds(ConfigHandler.config.kernelPersistentCooldownMinimumSeconds.get()));
        teleports = new TeleportRepository();
        safeTeleports = new SafeTeleportService(locationHistory);
        teleportRequests = new TeleportRequestService();
        teleportSettings = TeleportSettings.fromConfig();
        storage.register(locationHistory);
        storage.register(cooldownRepository);
        storage.register(teleports);
        storage.register(social);

        initialized = true;
        reloadConfiguration();
    }

    public static synchronized void reloadConfiguration() {
        ensureInitialized();
        final TeleportSettings replacementTeleportSettings;
        try {
            replacementTeleportSettings = TeleportSettings.fromConfig();
        } catch (IllegalArgumentException exception) {
            com.enviouse.sef.ServerEssentialsForge.LOGGER.error(
                    "[SEF] Teleport configuration reload was rejected. The previous snapshot remains active",
                    exception);
            return;
        }
        long revision = CONFIG_REVISION.incrementAndGet();
        Map<String, Boolean> features = Map.ofEntries(
                Map.entry("sef.core", true),
                Map.entry("sef.filter", ConfigHandler.config.enableFilterSystem.get()),
                Map.entry("sef.storage", true),
                Map.entry("sef.motd", ConfigHandler.config.enableMotdSystem.get()),
                Map.entry("sef.workstation.craft", ConfigHandler.config.enableCraftingTableCommand.get()),
                Map.entry("sef.workstation.anvil", ConfigHandler.config.enableAnvilCommand.get()),
                Map.entry("sef.workstation.enchant", ConfigHandler.config.enableEnchantingTableCommand.get()),
                Map.entry("sef.workstation.super_enchant", ConfigHandler.config.enableSuperEnchantingTableCommand.get()),
                Map.entry("sef.workstation.repair", ConfigHandler.config.enableRepairCommand.get()),
                Map.entry("sef.teleport", ConfigHandler.config.enableTeleportEssentials.get()),
                Map.entry("sef.teleport.homes", ConfigHandler.config.enableTeleportEssentials.get()
                        && ConfigHandler.config.enableHomes.get()),
                Map.entry("sef.teleport.requests", ConfigHandler.config.enableTeleportEssentials.get()
                        && ConfigHandler.config.enableTeleportRequests.get()),
                Map.entry("sef.teleport.back", ConfigHandler.config.enableTeleportEssentials.get()
                        && ConfigHandler.config.enableBack.get()),
                Map.entry("sef.teleport.spawn", ConfigHandler.config.enableTeleportEssentials.get()
                        && ConfigHandler.config.enableSpawnCommands.get()),
                Map.entry("sef.teleport.warps", ConfigHandler.config.enableTeleportEssentials.get()
                        && ConfigHandler.config.enableServerWarps.get()),
                Map.entry("sef.teleport.player_warps", ConfigHandler.config.enableTeleportEssentials.get()
                        && ConfigHandler.config.enablePlayerWarps.get()),
                Map.entry("sef.teleport.random", ConfigHandler.config.enableTeleportEssentials.get()
                        && ConfigHandler.config.enableRandomTeleport.get()),
                Map.entry("sef.teleport.direct", ConfigHandler.config.enableTeleportEssentials.get()
                        && ConfigHandler.config.enableDirectTeleport.get()),
                Map.entry("sef.social", ConfigHandler.config.enableSocialEssentials.get()),
                Map.entry("sef.social.spy", ConfigHandler.config.enableSocialEssentials.get()
                        && ConfigHandler.config.enableSocialSpy.get()),
                Map.entry("sef.social.mail", ConfigHandler.config.enableSocialEssentials.get()
                        && ConfigHandler.config.enableMail.get()),
                Map.entry("sef.social.connection", ConfigHandler.config.enableSocialEssentials.get()
                        && ConfigHandler.config.enableConnectionMessages.get()),
                Map.entry("sef.social.reminders", ConfigHandler.config.enableSocialEssentials.get()
                        && ConfigHandler.config.enableReminders.get()),
                Map.entry("sef.social.text", ConfigHandler.config.enableSocialEssentials.get()
                        && ConfigHandler.config.enableCustomText.get()));
        Map<String, Boolean> actionOverrides = replacementTeleportSettings.disabledActions().stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(action -> action, ignored -> false));
        featureGates.publish(new FeatureGateService.Snapshot(revision, features, Map.of(), actionOverrides));
        teleportSettings = replacementTeleportSettings;
        if (!ConfigHandler.config.enableTeleportEssentials.get()) {
            com.enviouse.sef.teleport.TeleportWarmupManager.cancelAll(
                    WarmupService.CancelReason.FEATURE_DISABLE);
        }

        List<CommandPolicyService.Policy> policies = new ArrayList<>();
        for (CommandDefinition definition : catalog.entries()) {
            policies.add(new CommandPolicyService.Policy(
                    definition.id(),
                    definition.featureId(),
                    definition.sourceTypes(),
                    false,
                    definition.confirmationRequired(),
                    cooldownFor(definition.id()),
                    warmupFor(definition.id()),
                    costFor(definition.id()),
                    definition.auditClass()));
        }
        commandPolicies.replaceAll(policies);
        quotas.invalidate();
    }

    public static synchronized void startStorage(Path managedRoot) {
        ensureInitialized();
        if (!storage.started()) {
            cooldowns.clearAll();
            storage.start(managedRoot);
        }
    }

    public static synchronized StorageCoordinator.FlushResult shutdown() {
        ensureInitialized();
        StorageCoordinator.FlushResult result = storage.shutdown();
        warmups.clear();
        confirmations.clear();
        teleportRequests.clear();
        observations.clearAll();
        if (result.successful()) {
            cooldowns.clearAll();
        }
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

    public static synchronized void installCostProvider(CostService provider) {
        ensureInitialized();
        costs = java.util.Objects.requireNonNull(provider, "provider");
        commandExecutions = new CommandExecutionService(
                commandPolicies,
                cooldowns,
                costs,
                warmups,
                confirmations);
    }

    public static synchronized void resetCostProvider() {
        installCostProvider(new CostService.Disabled());
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

    public static TeleportRepository teleports() {
        ensureInitialized();
        return teleports;
    }

    public static SafeTeleportService safeTeleports() {
        ensureInitialized();
        return safeTeleports;
    }

    public static TeleportRequestService teleportRequests() {
        ensureInitialized();
        return teleportRequests;
    }

    public static TeleportSettings teleportSettings() {
        ensureInitialized();
        return teleportSettings;
    }

    public static SocialRepository social() {
        ensureInitialized();
        return social;
    }

    public static ObservationService observations() {
        ensureInitialized();
        return observations;
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
                CommandDefinition.AccessClass.PLAYER, STANDARD_COMMAND_SOURCES,
                "sef.core", AuditService.AuditClass.METADATA_ONLY, "sef:core", "informational command has no persistent hud");
        register("sef:core.colors", "sef colors", Set.of("colors"), "sef.commands.sef.allowed", "sef.commands.sef.colors",
                CommandDefinition.AccessClass.PLAYER, STANDARD_COMMAND_SOURCES,
                "sef.core", AuditService.AuditClass.METADATA_ONLY, "sef:core", "informational command has no persistent hud");
        register("sef:core.test", "sef test", Set.of(), "sef.commands.sef.allowed", "sef.commands.sef.test",
                CommandDefinition.AccessClass.ADMINISTRATOR, STANDARD_COMMAND_SOURCES,
                "sef.core", AuditService.AuditClass.ADMIN_ACTION, "sef:core", "debug command has no persistent hud");
        register("sef:core.reload", "sef reload", Set.of(), "sef.commands.sef.allowed", "sef.commands.sef.reload",
                CommandDefinition.AccessClass.ADMINISTRATOR, STANDARD_COMMAND_SOURCES,
                "sef.core", AuditService.AuditClass.CONFIG_DEFINITION, "sef:core", "reload result is immediate");
        register("sef:core.commands", "sef commands", Set.of(), "sef.commands.sef.allowed", "sef.commands.sef.commands",
                CommandDefinition.AccessClass.PLAYER, STANDARD_COMMAND_SOURCES,
                "sef.core", AuditService.AuditClass.METADATA_ONLY, "sef:core", "catalog view has no persistent hud");
        register("sef:core.conflicts", "sef conflicts", Set.of(), "sef.commands.sef.allowed", "sef.commands.sef.conflicts",
                CommandDefinition.AccessClass.ADMINISTRATOR, STANDARD_COMMAND_SOURCES,
                "sef.core", AuditService.AuditClass.SENSITIVE_ACCESS, "sef:core", "diagnostic view has no persistent hud");
        register("sef:core.doctor", "sef doctor", Set.of(), "sef.commands.sef.allowed", "sef.commands.sef.doctor",
                CommandDefinition.AccessClass.ADMINISTRATOR, STANDARD_COMMAND_SOURCES,
                "sef.core", AuditService.AuditClass.SENSITIVE_ACCESS, "sef:core", "diagnostic view has no persistent hud");
        register("sef:filter.add", "sef filter add", Set.of(), "sef.commands.sef.allowed", "sef.filter.manage",
                CommandDefinition.AccessClass.ADMINISTRATOR, STANDARD_COMMAND_SOURCES,
                "sef.filter", AuditService.AuditClass.CONFIG_DEFINITION, "sef:core", "filter changes are immediate");
        register("sef:filter.remove", "sef filter remove", Set.of(), "sef.commands.sef.allowed", "sef.filter.manage",
                CommandDefinition.AccessClass.ADMINISTRATOR, STANDARD_COMMAND_SOURCES,
                "sef.filter", AuditService.AuditClass.CONFIG_DEFINITION, "sef:core", "filter changes are immediate");
        register("sef:filter.list", "sef filter list", Set.of(), "sef.commands.sef.allowed", "sef.filter.manage",
                CommandDefinition.AccessClass.ADMINISTRATOR, STANDARD_COMMAND_SOURCES,
                "sef.filter", AuditService.AuditClass.SENSITIVE_ACCESS, "sef:core", "filter diagnostics have no persistent hud");
        register("sef:storage.status", "sef storage status", Set.of(), "sef.commands.sef.allowed", "sef.storage.status",
                CommandDefinition.AccessClass.ADMINISTRATOR, STANDARD_COMMAND_SOURCES,
                "sef.storage", AuditService.AuditClass.SENSITIVE_ACCESS, "sef:core", "storage diagnostics have no persistent hud");
        register("sef:storage.export", "sef storage export", Set.of(), "sef.commands.sef.allowed", "sef.storage.export",
                CommandDefinition.AccessClass.ADMINISTRATOR, STANDARD_COMMAND_SOURCES,
                "sef.storage", AuditService.AuditClass.SENSITIVE_ACCESS, "sef:core", "storage exports are reported through command output");
        register("sef:motd.set", "sef motd set", Set.of(), "sef.commands.sef.allowed", "sef.motd.manage",
                CommandDefinition.AccessClass.ADMINISTRATOR, STANDARD_COMMAND_SOURCES,
                "sef.motd", AuditService.AuditClass.CONFIG_DEFINITION, "sef:core", "motd changes are immediate");
        register("sef:motd.reload", "sef motd reload", Set.of(), "sef.commands.sef.allowed", "sef.motd.manage",
                CommandDefinition.AccessClass.ADMINISTRATOR, STANDARD_COMMAND_SOURCES,
                "sef.motd", AuditService.AuditClass.CONFIG_DEFINITION, "sef:core", "motd reload is immediate");
        register("sef:motd.show", "sef motd show", Set.of(), "sef.commands.sef.allowed", "sef.motd.manage",
                CommandDefinition.AccessClass.ADMINISTRATOR, STANDARD_COMMAND_SOURCES,
                "sef.motd", AuditService.AuditClass.SENSITIVE_ACCESS, "sef:core", "motd view has no persistent hud");

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

    private static void registerTeleportCommands() {
        registerTeleport("sef:teleport.home.use", "home", Set.of("home"), "sef.commands.home",
                CommandDefinition.AccessClass.PLAYER, "sef.teleport.homes", CommandDefinition.TargetBehavior.SELF);
        registerTeleport("sef:teleport.home.set", "sethome", Set.of("sethome"), "sef.commands.sethome",
                CommandDefinition.AccessClass.PLAYER, "sef.teleport.homes", CommandDefinition.TargetBehavior.SELF);
        registerTeleport("sef:teleport.home.delete", "delhome", Set.of("delhome"), "sef.commands.delhome",
                CommandDefinition.AccessClass.PLAYER, "sef.teleport.homes", CommandDefinition.TargetBehavior.SELF);
        registerTeleport("sef:teleport.home.rename", "renamehome", Set.of("renamehome"), "sef.commands.renamehome",
                CommandDefinition.AccessClass.PLAYER, "sef.teleport.homes", CommandDefinition.TargetBehavior.SELF);
        registerTeleport("sef:teleport.home.list", "homes", Set.of("homes"), "sef.commands.homes",
                CommandDefinition.AccessClass.PLAYER, "sef.teleport.homes", CommandDefinition.TargetBehavior.OPTIONAL_PLAYER);
        registerTeleport("sef:teleport.home.admin", "homeadmin", Set.of("homeadmin"), "sef.commands.homeadmin",
                CommandDefinition.AccessClass.ADMINISTRATOR, "sef.teleport.homes", CommandDefinition.TargetBehavior.REQUIRED_PLAYER);

        registerTeleport("sef:teleport.request.to", "tpa", Set.of("tpa"), "sef.commands.tpa",
                CommandDefinition.AccessClass.PLAYER, "sef.teleport.requests", CommandDefinition.TargetBehavior.REQUIRED_PLAYER);
        registerTeleport("sef:teleport.request.here", "tpahere", Set.of("tpahere"), "sef.commands.tpahere",
                CommandDefinition.AccessClass.PLAYER, "sef.teleport.requests", CommandDefinition.TargetBehavior.REQUIRED_PLAYER);
        registerTeleport("sef:teleport.request.accept", "tpaccept", Set.of("tpaccept"), "sef.commands.tpaccept",
                CommandDefinition.AccessClass.PLAYER, "sef.teleport.requests", CommandDefinition.TargetBehavior.OPTIONAL_PLAYER);
        registerTeleport("sef:teleport.request.deny", "tpdeny", Set.of("tpdeny"), "sef.commands.tpdeny",
                CommandDefinition.AccessClass.PLAYER, "sef.teleport.requests", CommandDefinition.TargetBehavior.OPTIONAL_PLAYER);
        registerTeleport("sef:teleport.request.cancel", "tpcancel", Set.of("tpcancel"), "sef.commands.tpcancel",
                CommandDefinition.AccessClass.PLAYER, "sef.teleport.requests", CommandDefinition.TargetBehavior.OPTIONAL_PLAYER);
        registerTeleport("sef:teleport.request.list", "tprequests", Set.of("tprequests"), "sef.commands.tprequests",
                CommandDefinition.AccessClass.PLAYER, "sef.teleport.requests", CommandDefinition.TargetBehavior.SELF);
        registerTeleport("sef:teleport.request.toggle", "tptoggle", Set.of("tptoggle"), "sef.commands.tptoggle",
                CommandDefinition.AccessClass.PLAYER, "sef.teleport.requests", CommandDefinition.TargetBehavior.SELF);
        registerTeleport("sef:teleport.request.block", "tpblock", Set.of("tpblock", "tpunblock"), "sef.commands.tpblock",
                CommandDefinition.AccessClass.PLAYER, "sef.teleport.requests", CommandDefinition.TargetBehavior.REQUIRED_PLAYER);
        registerTeleport("sef:teleport.request.auto", "tpautoaccept", Set.of("tpautoaccept"), "sef.commands.tpautoaccept",
                CommandDefinition.AccessClass.TRUSTED_PLAYER, "sef.teleport.requests", CommandDefinition.TargetBehavior.SELF);

        registerTeleport("sef:teleport.back", "back", Set.of("back"), "sef.commands.back",
                CommandDefinition.AccessClass.PLAYER, "sef.teleport.back", CommandDefinition.TargetBehavior.SELF);
        registerTeleport("sef:teleport.spawn", "spawn", Set.of("spawn"), "sef.commands.spawn",
                CommandDefinition.AccessClass.PLAYER, "sef.teleport.spawn", CommandDefinition.TargetBehavior.SELF);
        registerTeleport("sef:teleport.spawn.set", "setspawn", Set.of("setspawn"), "sef.commands.setspawn",
                CommandDefinition.AccessClass.ADMINISTRATOR, "sef.teleport.spawn", CommandDefinition.TargetBehavior.SELF);
        registerTeleport("sef:teleport.spawn.info", "spawninfo", Set.of("spawninfo"), "sef.commands.spawninfo",
                CommandDefinition.AccessClass.ADMINISTRATOR, "sef.teleport.spawn", CommandDefinition.TargetBehavior.NONE);

        registerTeleport("sef:teleport.warp.use", "warp", Set.of("warp"), "sef.commands.warp",
                CommandDefinition.AccessClass.PLAYER, "sef.teleport.warps", CommandDefinition.TargetBehavior.SELF);
        registerTeleport("sef:teleport.warp.list", "warps", Set.of("warps"), "sef.commands.warps",
                CommandDefinition.AccessClass.PLAYER, "sef.teleport.warps", CommandDefinition.TargetBehavior.NONE);
        registerTeleport("sef:teleport.warp.set", "setwarp", Set.of("setwarp"), "sef.commands.setwarp",
                CommandDefinition.AccessClass.ADMINISTRATOR, "sef.teleport.warps", CommandDefinition.TargetBehavior.SELF);
        registerTeleport("sef:teleport.warp.delete", "delwarp", Set.of("delwarp"), "sef.commands.delwarp",
                CommandDefinition.AccessClass.ADMINISTRATOR, "sef.teleport.warps", CommandDefinition.TargetBehavior.NONE);
        registerTeleport("sef:teleport.warp.rename", "renamewarp", Set.of("renamewarp"), "sef.commands.renamewarp",
                CommandDefinition.AccessClass.ADMINISTRATOR, "sef.teleport.warps", CommandDefinition.TargetBehavior.NONE);
        registerTeleport("sef:teleport.warp.manage", "warpinfo", Set.of("warpinfo"), "sef.commands.warpinfo",
                CommandDefinition.AccessClass.ADMINISTRATOR, "sef.teleport.warps", CommandDefinition.TargetBehavior.NONE);

        registerTeleport("sef:teleport.player_warp.use", "pwarp", Set.of("pwarp", "playerwarp", "pw"), "sef.commands.pwarp",
                CommandDefinition.AccessClass.PLAYER, "sef.teleport.player_warps", CommandDefinition.TargetBehavior.SELF);
        registerTeleport("sef:teleport.player_warp.list", "pwarps", Set.of("pwarps", "playerwarps", "pws"), "sef.commands.pwarps",
                CommandDefinition.AccessClass.PLAYER, "sef.teleport.player_warps", CommandDefinition.TargetBehavior.OPTIONAL_PLAYER);
        registerTeleport("sef:teleport.player_warp.create", "setpwarp", Set.of("setpwarp"), "sef.commands.setpwarp",
                CommandDefinition.AccessClass.PLAYER, "sef.teleport.player_warps", CommandDefinition.TargetBehavior.SELF);
        registerTeleport("sef:teleport.player_warp.delete", "delpwarp", Set.of("delpwarp"), "sef.commands.delpwarp",
                CommandDefinition.AccessClass.PLAYER, "sef.teleport.player_warps", CommandDefinition.TargetBehavior.SELF);
        registerTeleport("sef:teleport.player_warp.rename", "renamepwarp", Set.of("renamepwarp"), "sef.commands.renamepwarp",
                CommandDefinition.AccessClass.PLAYER, "sef.teleport.player_warps", CommandDefinition.TargetBehavior.SELF);
        registerTeleport("sef:teleport.player_warp.manage", "pwarp manage", Set.of(), "sef.playerwarps.edit",
                CommandDefinition.AccessClass.PLAYER, "sef.teleport.player_warps", CommandDefinition.TargetBehavior.SELF);
        registerTeleport("sef:teleport.player_warp.moderate", "pwarp moderate", Set.of(), "sef.playerwarps.moderate",
                CommandDefinition.AccessClass.ADMINISTRATOR, "sef.teleport.player_warps", CommandDefinition.TargetBehavior.REQUIRED_PLAYER);

        registerTeleport("sef:teleport.random", "rtp", Set.of("rtp", "tpr"), "sef.commands.rtp",
                CommandDefinition.AccessClass.PLAYER, "sef.teleport.random", CommandDefinition.TargetBehavior.SELF);
        registerTeleport("sef:teleport.random.set", "settpr", Set.of("settpr"), "sef.commands.settpr",
                CommandDefinition.AccessClass.ADMINISTRATOR, "sef.teleport.random", CommandDefinition.TargetBehavior.SELF);

        registerTeleport("sef:teleport.direct", "tp", Set.of("tp"), "sef.commands.tp",
                CommandDefinition.AccessClass.ADMINISTRATOR, "sef.teleport.direct", CommandDefinition.TargetBehavior.REQUIRED_PLAYER);
        registerTeleport("sef:teleport.direct.here", "tphere", Set.of("tphere"), "sef.commands.tphere",
                CommandDefinition.AccessClass.ADMINISTRATOR, "sef.teleport.direct", CommandDefinition.TargetBehavior.REQUIRED_PLAYER);
        registerTeleport("sef:teleport.direct.override", "tpo", Set.of("tpo", "tpohere"), "sef.commands.tpo",
                CommandDefinition.AccessClass.ADMINISTRATOR, "sef.teleport.direct", CommandDefinition.TargetBehavior.REQUIRED_PLAYER);
        registerTeleport("sef:teleport.direct.offline", "tpoffline", Set.of("tpoffline"), "sef.commands.tpoffline",
                CommandDefinition.AccessClass.ADMINISTRATOR, "sef.teleport.direct", CommandDefinition.TargetBehavior.REQUIRED_PLAYER);
        registerTeleport("sef:teleport.direct.position", "tppos", Set.of("tppos"), "sef.commands.tppos",
                CommandDefinition.AccessClass.ADMINISTRATOR, "sef.teleport.direct", CommandDefinition.TargetBehavior.SELF);
        registerTeleport("sef:teleport.direct.all", "tpall", Set.of("tpall"), "sef.commands.tpall",
                CommandDefinition.AccessClass.ADMINISTRATOR, "sef.teleport.direct", CommandDefinition.TargetBehavior.BOUNDED_PLAYERS);
        registerTeleport("sef:teleport.request.all", "tpaall", Set.of("tpaall"), "sef.commands.tpaall",
                CommandDefinition.AccessClass.ADMINISTRATOR, "sef.teleport.requests", CommandDefinition.TargetBehavior.BOUNDED_PLAYERS);
    }

    private static void registerSocialCommands() {
        registerSocial("sef:social.message", "msg", Set.of("msg", "tell", "w", "r", "reply", "whisper", "pchat"),
                "sef.commands.msg", CommandDefinition.AccessClass.PLAYER,
                CommandDefinition.TargetBehavior.REQUIRED_PLAYER);
        registerSocial("sef:social.message.toggle", "msgtoggle", Set.of("msgtoggle"),
                "sef.commands.msgtoggle", CommandDefinition.AccessClass.PLAYER,
                CommandDefinition.TargetBehavior.SELF);
        registerSocial("sef:social.reply.toggle", "rtoggle", Set.of("rtoggle"),
                "sef.commands.rtoggle", CommandDefinition.AccessClass.PLAYER,
                CommandDefinition.TargetBehavior.SELF);
        registerSocial("sef:social.ignore", "ignore", Set.of("ignore", "ignorelist"),
                "sef.commands.ignore", CommandDefinition.AccessClass.PLAYER,
                CommandDefinition.TargetBehavior.OPTIONAL_PLAYER);
        registerSocial("sef:social.spy", "socialspy", Set.of("socialspy"),
                "sef.commands.socialspy", CommandDefinition.AccessClass.ADMINISTRATOR,
                CommandDefinition.TargetBehavior.OPTIONAL_PLAYER);
        registerSocial("sef:social.mail", "mail", Set.of("mail"),
                "sef.commands.mail", CommandDefinition.AccessClass.PLAYER,
                CommandDefinition.TargetBehavior.OPTIONAL_PLAYER);
        registerSocial("sef:social.connection", "connectionmessage", Set.of("joinmessage", "leavemessage"),
                "sef.commands.connectionmessage.inspect", CommandDefinition.AccessClass.ADMINISTRATOR,
                CommandDefinition.TargetBehavior.REQUIRED_PLAYER);
        registerSocial("sef:social.reminder", "reminder", Set.of("reminder", "reminders", "welcome"),
                "sef.commands.reminders", CommandDefinition.AccessClass.PLAYER,
                CommandDefinition.TargetBehavior.OPTIONAL_PLAYER);
        registerSocial("sef:social.text", "customtext", Set.of("customtext", "booktext", "rules", "info"),
                "sef.commands.customtext", CommandDefinition.AccessClass.PLAYER,
                CommandDefinition.TargetBehavior.NONE);
        registerSocial("sef:social.identity", "sef identity", Set.of(),
                "sef.commands.sef.identity.coverage", CommandDefinition.AccessClass.ADMINISTRATOR,
                CommandDefinition.TargetBehavior.NONE);
    }

    private static void registerSocial(
            String id,
            String route,
            Set<String> roots,
            String permission,
            CommandDefinition.AccessClass access,
            CommandDefinition.TargetBehavior targetBehavior
    ) {
        catalog.register(new CommandDefinition(
                id,
                route,
                roots,
                "command." + id.replace(':', '.') + ".description",
                "command." + id.replace(':', '.') + ".usage",
                "social",
                socialFeature(id),
                Set.of(permission),
                access,
                STANDARD_COMMAND_SOURCES,
                targetBehavior,
                id,
                false,
                access == CommandDefinition.AccessClass.ADMINISTRATOR
                        ? AuditService.AuditClass.SENSITIVE_ACCESS
                        : AuditService.AuditClass.METADATA_ONLY,
                "sef:social",
                "",
                "social state is shown through immediate command feedback",
                "",
                "social collections are bounded by repository and quota policy",
                CommandDefinition.ConflictPolicy.PREFER_SEF,
                true,
                true));
    }

    private static String socialFeature(String actionId) {
        return switch (actionId) {
            case "sef:social.spy" -> "sef.social.spy";
            case "sef:social.mail" -> "sef.social.mail";
            case "sef:social.connection" -> "sef.social.connection";
            case "sef:social.reminder" -> "sef.social.reminders";
            case "sef:social.text" -> "sef.social.text";
            default -> "sef.social";
        };
    }

    private static void registerTeleport(
            String id,
            String route,
            Set<String> roots,
            String permission,
            CommandDefinition.AccessClass access,
            String feature,
            CommandDefinition.TargetBehavior targetBehavior
    ) {
        catalog.register(new CommandDefinition(
                id,
                route,
                roots,
                "command." + id.replace(':', '.').replace('/', '.') + ".description",
                "command." + id.replace(':', '.').replace('/', '.') + ".usage",
                "teleports",
                feature,
                Set.of(permission),
                access,
                STANDARD_COMMAND_SOURCES,
                targetBehavior,
                id,
                false,
                access == CommandDefinition.AccessClass.ADMINISTRATOR
                        ? AuditService.AuditClass.ADMIN_ACTION
                        : AuditService.AuditClass.METADATA_ONLY,
                "sef:teleports",
                "",
                "teleport state is shown through immediate command feedback",
                "",
                "teleport collections and target fan out have bounded configuration limits",
                CommandDefinition.ConflictPolicy.PREFER_SEF,
                true,
                true));
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
                ConfigHandler.config.defaultHomeLimit.get(),
                1000,
                true,
                Map.of("sef.homes.3", 3L, "sef.homes.5", 5L, "sef.homes.10", 10L)));
        quotas.register(new QuotaService.Definition(
                "sef:homes_per_dimension",
                QuotaService.QuotaKind.COUNT,
                ConfigHandler.config.defaultHomePerDimensionLimit.get(),
                1000,
                true,
                Map.of()));
        quotas.register(new QuotaService.Definition(
                "sef:player_warps",
                QuotaService.QuotaKind.COUNT,
                ConfigHandler.config.defaultPlayerWarpLimit.get(),
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
        if (actionId.startsWith("sef:teleport.") && !actionId.endsWith(".set")
                && !actionId.contains(".admin.")) {
            return teleportSettings.cooldown();
        }
        return switch (actionId) {
            case "sef:workstation.craft" -> Duration.ofSeconds(ConfigHandler.config.craftingTableCooldownSeconds.get());
            case "sef:workstation.anvil" -> Duration.ofSeconds(ConfigHandler.config.anvilCooldownSeconds.get());
            case "sef:workstation.enchant" -> Duration.ofSeconds(ConfigHandler.config.enchantingTableCooldownSeconds.get());
            case "sef:workstation.super_enchant" -> Duration.ofSeconds(ConfigHandler.config.superEnchantingTableCooldownSeconds.get());
            case "sef:workstation.repair" -> Duration.ofSeconds(ConfigHandler.config.repairCooldownSeconds.get());
            default -> Duration.ZERO;
        };
    }

    private static Duration warmupFor(String actionId) {
        if (actionId.startsWith("sef:teleport.")
                && (actionId.endsWith(".use")
                || actionId.equals("sef:teleport.back")
                || actionId.equals("sef:teleport.spawn")
                || actionId.equals("sef:teleport.random"))) {
            return teleportSettings.warmup();
        }
        return Duration.ZERO;
    }

    private static BigDecimal costFor(String actionId) {
        return actionId.startsWith("sef:teleport.")
                && (actionId.endsWith(".use")
                || actionId.equals("sef:teleport.back")
                || actionId.equals("sef:teleport.spawn")
                || actionId.equals("sef:teleport.random"))
                ? teleportSettings.cost()
                : BigDecimal.ZERO;
    }

    private static void ensureInitialized() {
        if (!initialized) {
            initialize();
        }
    }
}
