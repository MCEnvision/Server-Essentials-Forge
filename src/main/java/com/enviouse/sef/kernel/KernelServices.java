package com.enviouse.sef.kernel;

import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.commandlog.CommandEventJournal;
import com.enviouse.sef.commandlog.CommandSpyRepository;
import com.enviouse.sef.commandlog.FileLogSink;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.economy.CommandCostSchedule;
import com.enviouse.sef.economy.EconomyCostService;
import com.enviouse.sef.economy.EconomyRepository;
import com.enviouse.sef.economy.EconomyService;
import com.enviouse.sef.economy.EconomySignRepository;
import com.enviouse.sef.gui.GuiPreferenceRepository;
import com.enviouse.sef.gui.protocol.SefNetwork;
import com.enviouse.sef.identity.IdentityService;
import com.enviouse.sef.identity.PlayerProfileRepository;
import com.enviouse.sef.freeze.FreezeManager;
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
import com.enviouse.sef.kits.KitRepository;
import com.enviouse.sef.message.MessageService;
import com.enviouse.sef.moderation.ConnectionAddressService;
import com.enviouse.sef.moderation.ModerationRepository;
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
    private static EconomyRepository economyRepository;
    private static EconomySignRepository economySigns;
    private static EconomyService economy;
    private static CommandCostSchedule commandCosts = CommandCostSchedule.empty();
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
    private static GuiPreferenceRepository guiPreferences;
    private static SocialRepository social;
    private static ObservationService observations;
    private static CommandSpyRepository commandSpies;
    private static FileLogSink fileLogs;
    private static CommandEventJournal commandJournal;
    private static ModerationRepository moderation;
    private static ConnectionAddressService connectionAddresses;
    private static KitRepository kits;
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
        descriptors.registerCommandOnly("sef:observation");
        descriptors.registerCommandOnly("sef:moderation");
        descriptors.registerCommandOnly("sef:inventory");
        descriptors.registerCommandOnly("sef:kits");
        descriptors.registerCommandOnly("sef:utilities");
        descriptors.registerCommandOnly("sef:economy");
        registerGuiDescriptors();

        catalog = new CommandCatalog(capabilities, descriptors);
        registerCoreCommands();
        registerTeleportCommands();
        registerSocialCommands();
        registerObservationCommands();
        registerModerationCommands();
        registerPhaseSevenCommands();
        registerEconomyCommands();
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
        economyRepository = new EconomyRepository(economyRepositorySettings());
        economySigns = new EconomySignRepository(ConfigHandler.config.economyMaximumSigns.get());
        economy = new EconomyService(economyRepository, economyServiceSettings());
        costs = economy.settings().enabled()
                ? new EconomyCostService(economy)
                : new CostService.Disabled();
        commandExecutions = new CommandExecutionService(
                commandPolicies,
                cooldowns,
                costs,
                warmups,
                confirmations);
        messages = new MessageService();
        social = new SocialRepository();
        observations = new ObservationService(social, messages);
        commandSpies = new CommandSpyRepository(ConfigHandler.config.commandSpySelectedLimit.get());
        fileLogs = new FileLogSink();
        commandJournal = new CommandEventJournal(
                commandSpies,
                fileLogs,
                ConfigHandler.config.commandSpyRecentLimit.get(),
                ConfigHandler.config.commandSpyEventsPerSecond.get());
        moderation = new ModerationRepository();
        connectionAddresses = new ConnectionAddressService();
        kits = new KitRepository(
                ConfigHandler.config.maximumKits.get(),
                ConfigHandler.config.maximumKitItems.get(),
                ConfigHandler.config.maximumKitUsesPerPlayer.get());
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

        storage = new StorageCoordinator(ConfigHandler.config.kernelRepositoryFlushSeconds.get());
        locationHistory = new LocationHistoryRepository(ConfigHandler.config.kernelLocationHistoryEntries.get());
        cooldownRepository = new CooldownRepository(
                cooldowns,
                Duration.ofSeconds(ConfigHandler.config.kernelPersistentCooldownMinimumSeconds.get()));
        teleports = new TeleportRepository();
        safeTeleports = new SafeTeleportService(locationHistory);
        teleportRequests = new TeleportRequestService();
        teleportSettings = TeleportSettings.fromConfig();
        guiPreferences = new GuiPreferenceRepository();
        storage.register(locationHistory);
        storage.register(cooldownRepository);
        storage.register(teleports);
        storage.register(social);
        storage.register(commandSpies);
        storage.register(moderation);
        storage.register(kits);
        storage.register(economyRepository);
        storage.register(economySigns);
        storage.register(guiPreferences);

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
        final CommandCostSchedule replacementCommandCosts;
        try {
            replacementCommandCosts = CommandCostSchedule.parse(
                    ConfigHandler.config.economyCommandCosts.get(),
                    economy.provider()
                            .map(com.enviouse.sef.economy.EconomyProvider::minorUnits)
                            .orElse(economyRepository.minorUnits()),
                    economy.settings().maximumTransaction());
        } catch (IllegalArgumentException exception) {
            com.enviouse.sef.ServerEssentialsForge.LOGGER.error(
                    "[SEF] Economy command cost reload was rejected. The previous snapshot remains active",
                    exception);
            return;
        }
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
                        && ConfigHandler.config.enableCustomText.get()),
                Map.entry("sef.observation.command", ConfigHandler.config.enableCommandSpy.get()),
                Map.entry("sef.logging", true),
                Map.entry("sef.moderation", ConfigHandler.config.enableModerationEssentials.get()),
                Map.entry("sef.moderation.jails", ConfigHandler.config.enableModerationEssentials.get()
                        && ConfigHandler.config.enableJails.get()),
                Map.entry("sef.inventory", ConfigHandler.config.enableInventoryUtilities.get()),
                Map.entry("sef.kits", ConfigHandler.config.enableKits.get()),
                Map.entry("sef.utilities", ConfigHandler.config.enablePlayerUtilities.get()),
                Map.entry("sef.gamemode", ConfigHandler.config.enableGamemodeShortcuts.get()),
                Map.entry("sef.item.self", ConfigHandler.config.enableItemShortcut.get()),
                Map.entry("sef.workstation.additional", ConfigHandler.config.enableAdditionalWorkstations.get()),
                Map.entry("sef.economy", economy.settings().enabled()),
                Map.entry("sef.economy.signs", economy.settings().enabled()
                        && ConfigHandler.config.enableEconomySigns.get()),
                Map.entry("sef.gui", SefNetwork.enhancedGuiActive()));
        Map<String, Boolean> actionOverrides = replacementTeleportSettings.disabledActions().stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(action -> action, ignored -> false));
        featureGates.publish(new FeatureGateService.Snapshot(revision, features, Map.of(), actionOverrides));
        commandCosts = replacementCommandCosts;
        teleportSettings = replacementTeleportSettings;
        ConnectionAddressService.ProviderMode configuredAddressMode =
                ConnectionAddressService.ProviderMode.parse(
                        ConfigHandler.config.moderationAddressProvider.get());
        if (connectionAddresses.mode() != configuredAddressMode) {
            connectionAddresses = new ConnectionAddressService(configuredAddressMode);
        }
        if (!ConfigHandler.config.enableModerationEssentials.get()) {
            FreezeManager.clearRepositoryState();
        }
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

    public static long configurationRevision() {
        return CONFIG_REVISION.get();
    }

    public static boolean cooldownBypass(net.minecraft.commands.CommandSourceStack source, String actionId) {
        String permission = actionId.startsWith("sef:inventory.") || actionId.startsWith("sef:item.")
                ? "sef.inventory.cooldown.bypass"
                : actionId.startsWith("sef:utility.")
                ? "sef.utilities.cooldown.bypass"
                : actionId.startsWith("sef:gamemode.")
                ? "sef.gamemode.cooldown.bypass"
                : actionId.startsWith("sef:kit.")
                ? "sef.kits.cooldown.bypass"
                : actionId.startsWith("sef:workstation.")
                ? "sef.commands.workstation.cooldown.bypass"
                : "";
        PermissionNode<Boolean> node = permission.isEmpty() ? null : permissionNode(permission);
        return node != null && com.enviouse.sef.permissions.PermissionService.has(source, node);
    }

    public static boolean costBypass(net.minecraft.commands.CommandSourceStack source) {
        PermissionNode<Boolean> node = permissionNode("sef.economy.cost.bypass");
        return node != null && com.enviouse.sef.permissions.PermissionService.has(source, node);
    }

    public static BigDecimal quoteCommandCost(
            String actionId,
            Map<String, String> normalizedParameters,
            List<java.util.UUID> targetIds
    ) {
        ensureInitialized();
        return commandCosts.configured(actionId)
                ? commandCosts.quote(actionId, normalizedParameters, targetIds)
                : costFor(actionId);
    }

    public static String commandCostDescription(String actionId) {
        ensureInitialized();
        if (commandCosts.configured(actionId)) {
            return commandCosts.describe(actionId, economy.settings().symbol());
        }
        BigDecimal cost = costFor(actionId);
        return cost.signum() == 0
                ? ""
                : "fixed " + economy.settings().symbol() + cost.toPlainString();
    }

    public static List<String> restartRequiredConfigurationDrift() {
        ensureInitialized();
        List<String> drift = new ArrayList<>();
        try {
            if (!economy.settings().equals(economyServiceSettings())) {
                drift.add("economy provider or transaction settings");
            }
            if (!economyRepository.settings().equals(economyRepositorySettings())) {
                drift.add("native economy account or storage bounds");
            }
            if (SefNetwork.configurationDrift()) {
                drift.add("enhanced gui protocol");
            }
        } catch (IllegalArgumentException | IllegalStateException exception) {
            drift.add("invalid pending economy configuration");
        }
        return List.copyOf(drift);
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
        fileLogs.shutdown();
        StorageCoordinator.FlushResult result = storage.shutdown();
        warmups.clear();
        confirmations.clear();
        teleportRequests.clear();
        observations.clearAll();
        commandJournal.clearRuntime();
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

    public static EconomyRepository economyRepository() {
        ensureInitialized();
        return economyRepository;
    }

    public static EconomyService economy() {
        ensureInitialized();
        return economy;
    }

    public static EconomySignRepository economySigns() {
        ensureInitialized();
        return economySigns;
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

    public static ModerationRepository moderation() {
        ensureInitialized();
        return moderation;
    }

    public static ConnectionAddressService connectionAddresses() {
        ensureInitialized();
        return connectionAddresses;
    }

    public static KitRepository kits() {
        ensureInitialized();
        return kits;
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

    public static GuiPreferenceRepository guiPreferences() {
        ensureInitialized();
        return guiPreferences;
    }

    public static SocialRepository social() {
        ensureInitialized();
        return social;
    }

    public static ObservationService observations() {
        ensureInitialized();
        return observations;
    }

    public static CommandSpyRepository commandSpies() {
        ensureInitialized();
        return commandSpies;
    }

    public static FileLogSink fileLogs() {
        ensureInitialized();
        return fileLogs;
    }

    public static CommandEventJournal commandJournal() {
        ensureInitialized();
        return commandJournal;
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

    private static void registerGuiDescriptors() {
        descriptors.register(new PanelContracts.PanelDescriptor(
                "sef:gui",
                "sef.gui.dashboard",
                3,
                "sef.kernel.gui.use",
                List.of(
                        panelControl(
                                "homes",
                                0,
                                "sef:teleport.home.list",
                                "sef.commands.homes",
                                PanelContracts.TargetPolicy.SELF,
                                "minecraft:red_bed"),
                        panelControl(
                                "warps",
                                1,
                                "sef:teleport.warp.list",
                                "sef.commands.warps",
                                PanelContracts.TargetPolicy.NONE,
                                "minecraft:ender_pearl"),
                        panelControl(
                                "requests",
                                2,
                                "sef:teleport.request.list",
                                "sef.commands.tprequests",
                                PanelContracts.TargetPolicy.SELF,
                                "minecraft:paper"),
                        panelControl(
                                "help",
                                3,
                                "sef:core.commands",
                                "sef.commands.sef.commands",
                                PanelContracts.TargetPolicy.SELF,
                                "minecraft:knowledge_book")),
                new PanelContracts.CommandFallback("sef dashboard", "sef.gui.dashboard.usage")));
        descriptors.register(new PanelContracts.PanelDescriptor(
                "sef:gui.homes",
                "sef.gui.homes",
                6,
                "sef.commands.homes",
                List.of(panelControl(
                        "home",
                        0,
                        "sef:teleport.home.use",
                        "sef.commands.home",
                        PanelContracts.TargetPolicy.SELF,
                        "minecraft:red_bed")),
                new PanelContracts.CommandFallback("homes", "sef.gui.homes.usage")));
        descriptors.register(new PanelContracts.PanelDescriptor(
                "sef:gui.warps",
                "sef.gui.warps",
                6,
                "sef.commands.warps",
                List.of(
                        panelControl(
                                "warp",
                                0,
                                "sef:teleport.warp.use",
                                "sef.commands.warp",
                                PanelContracts.TargetPolicy.SELF,
                                "minecraft:ender_pearl"),
                        panelControl(
                                "player_warp",
                                1,
                                "sef:teleport.player_warp.use",
                                "sef.commands.pwarp",
                                PanelContracts.TargetPolicy.SELF,
                                "minecraft:lodestone")),
                new PanelContracts.CommandFallback("warps", "sef.gui.warps.usage")));
        descriptors.register(new PanelContracts.PanelDescriptor(
                "sef:gui.teleport_requests",
                "sef.gui.teleport_requests",
                6,
                "sef.commands.tprequests",
                List.of(
                        panelControl(
                                "accept",
                                0,
                                "sef:teleport.request.accept",
                                "sef.commands.tpaccept",
                                PanelContracts.TargetPolicy.SELECTED_VISIBLE_PLAYER,
                                "minecraft:lime_dye"),
                        panelControl(
                                "deny",
                                1,
                                "sef:teleport.request.deny",
                                "sef.commands.tpdeny",
                                PanelContracts.TargetPolicy.SELECTED_VISIBLE_PLAYER,
                                "minecraft:red_dye"),
                        panelControl(
                                "cancel",
                                2,
                                "sef:teleport.request.cancel",
                                "sef.commands.tpcancel",
                                PanelContracts.TargetPolicy.SELECTED_VISIBLE_PLAYER,
                                "minecraft:barrier")),
                new PanelContracts.CommandFallback("tprequests", "sef.gui.teleport_requests.usage")));
        descriptors.register(new PanelContracts.PanelDescriptor(
                "sef:gui.help",
                "sef.gui.help",
                6,
                "sef.commands.sef.commands",
                List.of(),
                new PanelContracts.CommandFallback("sef commands", "sef.gui.help.usage")));
        descriptors.register(new PanelContracts.PanelDescriptor(
                "sef:gui.staff",
                "sef.gui.staff",
                6,
                "sef.kernel.panel.use",
                List.of(),
                new PanelContracts.CommandFallback("sef doctor", "sef.gui.staff.usage")));
        descriptors.register(new PanelContracts.PanelDescriptor(
                "sef:gui.players",
                "sef.gui.players",
                6,
                "sef.kernel.panel.use",
                List.of(),
                new PanelContracts.CommandFallback("list", "sef.gui.players.usage")));
    }

    private static PanelContracts.ControlDescriptor panelControl(
            String id,
            int slot,
            String actionId,
            String permissionId,
            PanelContracts.TargetPolicy targetPolicy,
            String iconId
    ) {
        return new PanelContracts.ControlDescriptor(
                id,
                slot,
                1,
                actionId,
                permissionId,
                targetPolicy,
                false,
                iconId);
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
        register("sef:gui.dashboard.open", "sef dashboard", Set.of(), "sef.commands.sef.allowed", "sef.kernel.gui.use",
                CommandDefinition.AccessClass.PLAYER, Set.of(CommandDefinition.SourceType.PLAYER),
                "sef.gui", AuditService.AuditClass.METADATA_ONLY, "sef:gui", "dashboard state is sent through the client protocol");
        register("sef:gui.client.status", "sef client status", Set.of(), "sef.commands.sef.allowed", "sef.commands.sef.info",
                CommandDefinition.AccessClass.PLAYER, STANDARD_COMMAND_SOURCES,
                "sef.core", AuditService.AuditClass.METADATA_ONLY, "sef:gui", "client status is immediate");
        register("sef:gui.reminder.dismiss", "sef client reminder dismiss", Set.of(), "sef.commands.sef.allowed", "sef.commands.sef.info",
                CommandDefinition.AccessClass.PLAYER, Set.of(CommandDefinition.SourceType.PLAYER),
                "sef.core", AuditService.AuditClass.METADATA_ONLY, "sef:gui", "reminder preference is persisted");
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

    private static void registerObservationCommands() {
        registerDomainCommand(
                "sef:commandspy.toggle", "commandspy toggle", Set.of("commandspy"),
                "sef.commands.commandspy", CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                "sef.observation.command", AuditService.AuditClass.COMMAND_OBSERVATION,
                "sef:observation", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand(
                "sef:commandspy.status", "commandspy status", Set.of(),
                "sef.commands.commandspy.status", CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                "sef.observation.command", AuditService.AuditClass.SENSITIVE_ACCESS,
                "sef:observation", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand(
                "sef:commandspy.recent", "commandspy recent", Set.of(),
                "sef.commands.commandspy.recent", CommandDefinition.AccessClass.ADMINISTRATOR,
                Set.of(CommandDefinition.SourceType.PLAYER), CommandDefinition.TargetBehavior.SELF,
                "sef.observation.command", AuditService.AuditClass.COMMAND_OBSERVATION,
                "sef:observation", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand(
                "sef:commandspy.audience", "commandspy audience", Set.of(),
                "sef.commands.commandspy", CommandDefinition.AccessClass.ADMINISTRATOR,
                Set.of(CommandDefinition.SourceType.PLAYER), CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                "sef.observation.command", AuditService.AuditClass.COMMAND_OBSERVATION,
                "sef:observation", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand(
                "sef:commandspy.selected", "commandspy selected", Set.of(),
                "sef.commands.commandspy.selected", CommandDefinition.AccessClass.ADMINISTRATOR,
                Set.of(CommandDefinition.SourceType.PLAYER), CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                "sef.observation.command", AuditService.AuditClass.COMMAND_OBSERVATION,
                "sef:observation", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand(
                "sef:commandspy.scope", "commandspy scope", Set.of(),
                "sef.commands.commandspy", CommandDefinition.AccessClass.ADMINISTRATOR,
                Set.of(CommandDefinition.SourceType.PLAYER), CommandDefinition.TargetBehavior.SELF,
                "sef.observation.command", AuditService.AuditClass.COMMAND_OBSERVATION,
                "sef:observation", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand(
                "sef:commandspy.filter", "commandspy filter", Set.of(),
                "sef.commands.commandspy.filter", CommandDefinition.AccessClass.ADMINISTRATOR,
                Set.of(CommandDefinition.SourceType.PLAYER), CommandDefinition.TargetBehavior.SELF,
                "sef.observation.command", AuditService.AuditClass.COMMAND_OBSERVATION,
                "sef:observation", CommandDefinition.ConflictPolicy.PREFER_SEF);

        registerDomainCommand(
                "sef:logging.status", "sef logging status", Set.of("loggerspy"),
                "sef.commands.logging.status", CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.logging", AuditService.AuditClass.FILE_LOG_CONTROL,
                "sef:observation", CommandDefinition.ConflictPolicy.PREFER_EXISTING);
        registerDomainCommand(
                "sef:logging.enable", "sef logging enable", Set.of(),
                "sef.commands.logging.enable", CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.logging", AuditService.AuditClass.FILE_LOG_CONTROL,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:logging.disable", "sef logging disable", Set.of(),
                "sef.commands.logging.disable", CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.logging", AuditService.AuditClass.FILE_LOG_CONTROL,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:logging.reload", "sef logging reload", Set.of(),
                "sef.commands.logging.enable", CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.logging", AuditService.AuditClass.FILE_LOG_CONTROL,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:logging.rotate", "sef logging rotate", Set.of(),
                "sef.commands.logging.rotate", CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.logging", AuditService.AuditClass.FILE_LOG_CONTROL,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:logging.flush", "sef logging flush", Set.of(),
                "sef.commands.logging.flush", CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.logging", AuditService.AuditClass.FILE_LOG_CONTROL,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:logging.stats", "sef logging stats", Set.of(),
                "sef.commands.logging.stats", CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.logging", AuditService.AuditClass.SENSITIVE_ACCESS,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:logging.doctor", "sef logging doctor", Set.of(),
                "sef.commands.logging.doctor", CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.logging", AuditService.AuditClass.SENSITIVE_ACCESS,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:logging.live", "sef logging live", Set.of(),
                "sef.commands.logging.live", CommandDefinition.AccessClass.ADMINISTRATOR,
                Set.of(CommandDefinition.SourceType.PLAYER), CommandDefinition.TargetBehavior.SELF,
                "sef.logging", AuditService.AuditClass.COMMAND_OBSERVATION,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:logging.recent", "sef logging recent", Set.of(),
                "sef.commands.logging.recent", CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.logging", AuditService.AuditClass.SENSITIVE_ACCESS,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:logging.stream.list", "sef logging stream list", Set.of(),
                "sef.commands.logging.stream.list", CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.logging", AuditService.AuditClass.FILE_LOG_CONTROL,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:logging.stream.configure", "sef logging stream configure", Set.of(),
                "sef.commands.logging.stream.configure", CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.logging", AuditService.AuditClass.FILE_LOG_CONTROL,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:logging.filter.list", "sef logging filter list", Set.of(),
                "sef.commands.logging.filter.list", CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.logging", AuditService.AuditClass.FILE_LOG_CONTROL,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:logging.filter.capture", "sef logging filter capture", Set.of(),
                "sef.commands.logging.filter.capture", CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.logging", AuditService.AuditClass.FILE_LOG_CONTROL,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:logging.filter.view", "sef logging filter view", Set.of(),
                "sef.commands.logging.filter.view", CommandDefinition.AccessClass.ADMINISTRATOR,
                Set.of(CommandDefinition.SourceType.PLAYER), CommandDefinition.TargetBehavior.SELF,
                "sef.logging", AuditService.AuditClass.COMMAND_OBSERVATION,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:logging.filter.root", "sef logging filter root", Set.of(),
                "sef.commands.logging.filter.root", CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.logging", AuditService.AuditClass.FILE_LOG_CONTROL,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:logging.filter.action", "sef logging filter action", Set.of(),
                "sef.commands.logging.filter.action", CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.logging", AuditService.AuditClass.FILE_LOG_CONTROL,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:logging.session.current", "sef logging session current", Set.of(),
                "sef.commands.logging.session.current", CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.logging", AuditService.AuditClass.SENSITIVE_ACCESS,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:logging.session.list", "sef logging session list", Set.of(),
                "sef.commands.logging.session.list", CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.logging", AuditService.AuditClass.SENSITIVE_ACCESS,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:logging.format.show", "sef logging format show", Set.of(),
                "sef.commands.logging.format.show", CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.logging", AuditService.AuditClass.FILE_LOG_CONTROL,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:logging.format.validate", "sef logging format validate", Set.of(),
                "sef.commands.logging.format.validate", CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.logging", AuditService.AuditClass.FILE_LOG_CONTROL,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:logging.format.set", "sef logging format set", Set.of(),
                "sef.commands.logging.format.set", CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.logging", AuditService.AuditClass.FILE_LOG_CONTROL,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:logging.format.reset", "sef logging format reset", Set.of(),
                "sef.commands.logging.format.reset", CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.logging", AuditService.AuditClass.FILE_LOG_CONTROL,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:logging.search", "sef logging search", Set.of(),
                "sef.commands.logging.search", CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.logging", AuditService.AuditClass.SENSITIVE_ACCESS,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:logging.export", "sef logging export", Set.of(),
                "sef.commands.logging.export", CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.logging", AuditService.AuditClass.FILE_LOG_CONTROL,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:logging.retention.preview", "sef logging retention preview", Set.of(),
                "sef.commands.logging.retention.preview", CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.logging", AuditService.AuditClass.FILE_LOG_CONTROL,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:logging.retention.run", "sef logging retention run", Set.of(),
                "sef.commands.logging.retention.run", CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.logging", AuditService.AuditClass.FILE_LOG_CONTROL,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:logging.repair", "sef logging repair", Set.of(),
                "sef.commands.logging.repair", CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.logging", AuditService.AuditClass.FILE_LOG_CONTROL,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
    }

    private static void registerModerationCommands() {
        registerDomainCommand("sef:moderation.ban", "ban", Set.of("ban"),
                "sef.commands.ban", CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.REQUIRED_PLAYER,
                "sef.moderation", AuditService.AuditClass.ADMIN_ACTION,
                "sef:moderation", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand("sef:moderation.tempban", "tempban", Set.of("tempban"),
                "sef.commands.tempban", CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.REQUIRED_PLAYER,
                "sef.moderation", AuditService.AuditClass.ADMIN_ACTION,
                "sef:moderation", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand("sef:moderation.pardon", "pardon", Set.of("pardon", "unban"),
                "sef.commands.pardon", CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.REQUIRED_PLAYER,
                "sef.moderation", AuditService.AuditClass.ADMIN_ACTION,
                "sef:moderation", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand("sef:moderation.ban_ip", "ban ip", Set.of("ban-ip", "banip"),
                "sef.commands.banip", CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.BOUNDED_PLAYERS,
                "sef.moderation", AuditService.AuditClass.NETWORK_ADDRESS_ACTION,
                "sef:moderation", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand("sef:moderation.tempban_ip", "tempban ip", Set.of("tempban-ip", "tempbanip"),
                "sef.commands.tempbanip", CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.BOUNDED_PLAYERS,
                "sef.moderation", AuditService.AuditClass.NETWORK_ADDRESS_ACTION,
                "sef:moderation", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand("sef:moderation.pardon_ip", "pardon ip",
                Set.of("pardon-ip", "unban-ip", "unbanip"),
                "sef.commands.pardonip", CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.moderation", AuditService.AuditClass.NETWORK_ADDRESS_ACTION,
                "sef:moderation", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand("sef:moderation.kick", "kick", Set.of("kick"),
                "sef.commands.kick", CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.REQUIRED_PLAYER,
                "sef.moderation", AuditService.AuditClass.ADMIN_ACTION,
                "sef:moderation", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand("sef:moderation.kick_ip", "kick ip", Set.of("kick-ip", "kickip"),
                "sef.commands.kickip", CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.BOUNDED_PLAYERS,
                "sef.moderation", AuditService.AuditClass.NETWORK_ADDRESS_ACTION,
                "sef:moderation", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand("sef:moderation.kick_self", "kickme", Set.of("kickme"),
                "sef.commands.kickme", CommandDefinition.AccessClass.PLAYER,
                Set.of(CommandDefinition.SourceType.PLAYER), CommandDefinition.TargetBehavior.SELF,
                "sef.moderation", AuditService.AuditClass.ADMIN_ACTION,
                "sef:moderation", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand("sef:moderation.kick_all", "kickall", Set.of("kickall"),
                "sef.commands.kickall", CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.BOUNDED_PLAYERS,
                "sef.moderation", AuditService.AuditClass.ADMIN_ACTION,
                "sef:moderation", CommandDefinition.ConflictPolicy.PREFER_SEF);
        Map<String, String> controlPermissions = Map.ofEntries(
                Map.entry("warn", "sef.commands.warn"),
                Map.entry("warns", "sef.commands.warns"),
                Map.entry("clearwarnings", "sef.commands.warn"),
                Map.entry("mute", "sef.commands.mute"),
                Map.entry("unmute", "sef.commands.unmute"),
                Map.entry("mutelist", "sef.commands.mute"),
                Map.entry("freeze", "sef.commands.freeze"),
                Map.entry("unfreeze", "sef.commands.unfreeze"),
                Map.entry("freezelist", "sef.commands.freeze"),
                Map.entry("invlock", "sef.commands.invlock"),
                Map.entry("disablebuilding", "sef.commands.disablebuilding"));
        controlPermissions.forEach((action, permission) -> {
            Set<String> roots = action.equals("disablebuilding")
                    ? Set.of("disablebuilding", "db")
                    : Set.of(action);
            registerDomainCommand(
                    "sef:moderation." + action,
                    action,
                    roots,
                    permission,
                    action.equals("warns")
                            ? CommandDefinition.AccessClass.PLAYER
                            : CommandDefinition.AccessClass.ADMINISTRATOR,
                    STANDARD_COMMAND_SOURCES,
                    action.endsWith("list")
                            ? CommandDefinition.TargetBehavior.SERVER
                            : CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                    "sef.moderation",
                    action.equals("warns") || action.endsWith("list")
                            ? AuditService.AuditClass.SENSITIVE_ACCESS
                            : AuditService.AuditClass.ADMIN_ACTION,
                    "sef:moderation",
                    CommandDefinition.ConflictPolicy.PREFER_SEF);
        });
        for (String action : List.of("setjail", "deljail", "jails", "jail", "unjail", "jailedplayers")) {
            registerDomainCommand("sef:moderation." + action, action, Set.of(action),
                    "sef.commands." + action, CommandDefinition.AccessClass.ADMINISTRATOR,
                    STANDARD_COMMAND_SOURCES,
                    action.equals("jail") || action.equals("unjail")
                            ? CommandDefinition.TargetBehavior.REQUIRED_PLAYER
                            : CommandDefinition.TargetBehavior.SERVER,
                    "sef.moderation.jails", AuditService.AuditClass.ADMIN_ACTION,
                    "sef:moderation", CommandDefinition.ConflictPolicy.PREFER_SEF);
        }
    }

    private static void registerPhaseSevenCommands() {
        for (String workstation : List.of(
                "cartographytable", "grindstone", "loom", "smithingtable", "stonecutter", "workbench")) {
            Set<String> roots = workstation.equals("workbench")
                    ? Set.of("workbench", "wb")
                    : Set.of(workstation);
            registerDomainCommand("sef:workstation." + workstation, workstation, roots,
                    "sef.commands." + workstation, CommandDefinition.AccessClass.PLAYER,
                    Set.of(CommandDefinition.SourceType.PLAYER), CommandDefinition.TargetBehavior.SELF,
                    "sef.workstation.additional", AuditService.AuditClass.METADATA_ONLY,
                    "sef:workstations", CommandDefinition.ConflictPolicy.PREFER_SEF);
        }
        Map<String, String> inventoryPermissions = Map.ofEntries(
                Map.entry("clear", "sef.commands.clearinventory"),
                Map.entry("enderchest", "sef.commands.enderchest"),
                Map.entry("disposal", "sef.commands.disposal"),
                Map.entry("more", "sef.commands.more"),
                Map.entry("condense", "sef.commands.condense"),
                Map.entry("hat", "sef.commands.hat"),
                Map.entry("itemname", "sef.commands.itemname"),
                Map.entry("itemlore", "sef.commands.itemlore"),
                Map.entry("book", "sef.commands.book"),
                Map.entry("recipe", "sef.commands.recipe"),
                Map.entry("itemdb", "sef.commands.itemdb"));
        Map<String, Set<String>> inventoryRoots = Map.ofEntries(
                Map.entry("clear", Set.of("clearinventory", "ci")),
                Map.entry("enderchest", Set.of("enderchest", "ec")),
                Map.entry("disposal", Set.of("disposal")),
                Map.entry("more", Set.of("more")),
                Map.entry("condense", Set.of("condense")),
                Map.entry("hat", Set.of("hat")),
                Map.entry("itemname", Set.of("itemname")),
                Map.entry("itemlore", Set.of("itemlore")),
                Map.entry("book", Set.of("book")),
                Map.entry("recipe", Set.of("recipe")),
                Map.entry("itemdb", Set.of("itemdb")));
        inventoryPermissions.forEach((action, permission) -> registerDomainCommand(
                "sef:inventory." + action, action, inventoryRoots.get(action),
                permission, CommandDefinition.AccessClass.PLAYER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                "sef.inventory", AuditService.AuditClass.ADMIN_ACTION,
                "sef:inventory", CommandDefinition.ConflictPolicy.PREFER_SEF));
        registerDomainCommand("sef:item.give.self", "item give self", Set.of("i"),
                "sef.commands.item.give.self", CommandDefinition.AccessClass.PLAYER,
                Set.of(CommandDefinition.SourceType.PLAYER), CommandDefinition.TargetBehavior.SELF,
                "sef.item.self", AuditService.AuditClass.ADMIN_ACTION,
                "sef:inventory", CommandDefinition.ConflictPolicy.PREFER_SEF);
        for (String action : List.of(
                "claim", "list", "show", "create", "delete", "reset", "edit", "validate", "export")) {
            String permission = switch (action) {
                case "claim" -> "sef.commands.kit";
                case "list" -> "sef.commands.kits";
                case "show" -> "sef.commands.showkit";
                case "create" -> "sef.commands.createkit";
                case "delete" -> "sef.commands.delkit";
                case "reset" -> "sef.commands.kitreset";
                case "edit" -> "sef.commands.kit.edit";
                case "export" -> "sef.commands.kit.export";
                default -> "sef.commands.kit.validate";
            };
            Set<String> roots = switch (action) {
                case "claim" -> Set.of("kit");
                case "list" -> Set.of("kits");
                case "show" -> Set.of("showkit");
                case "create" -> Set.of("createkit");
                case "delete" -> Set.of("delkit");
                case "reset" -> Set.of("kitreset");
                default -> Set.of();
            };
            registerDomainCommand("sef:kit." + action, "kit " + action, roots,
                    permission, CommandDefinition.AccessClass.PLAYER,
                    STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                    "sef.kits", AuditService.AuditClass.ADMIN_ACTION,
                    "sef:kits", CommandDefinition.ConflictPolicy.PREFER_SEF);
        }
        for (String utility : List.of(
                "afk", "feed", "heal", "fly", "god", "speed", "exp", "ptime", "pweather",
                "rest", "suicide", "near", "getpos", "compass", "depth", "top", "bottom", "jump")) {
            registerDomainCommand("sef:utility." + utility, utility, Set.of(utility),
                    "sef.commands." + utility, CommandDefinition.AccessClass.PLAYER,
                    STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                    "sef.utilities", AuditService.AuditClass.METADATA_ONLY,
                    "sef:utilities", CommandDefinition.ConflictPolicy.PREFER_SEF);
        }
        for (String mode : List.of("creative", "survival", "spectator", "adventure")) {
            Set<String> roots = switch (mode) {
                case "creative" -> Set.of("gmc");
                case "survival" -> Set.of("gms");
                case "spectator" -> Set.of("gmsp");
                default -> Set.of("gma");
            };
            registerDomainCommand("sef:gamemode." + mode, "gamemode " + mode, roots,
                    "sef.commands.gamemode." + mode, CommandDefinition.AccessClass.ADMINISTRATOR,
                    STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                    "sef.gamemode", AuditService.AuditClass.ADMIN_ACTION,
                    "sef:utilities", CommandDefinition.ConflictPolicy.PREFER_SEF);
        }
        registerDomainCommand("sef:gamemode.set", "gamemode", Set.of("gm"),
                "sef.commands.gamemode", CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                "sef.gamemode", AuditService.AuditClass.ADMIN_ACTION,
                "sef:utilities", CommandDefinition.ConflictPolicy.PREFER_SEF);
    }

    private static void registerEconomyCommands() {
        registerDomainCommand("sef:economy.balance", "balance", Set.of("balance", "bal", "money"),
                "sef.commands.balance", CommandDefinition.AccessClass.PLAYER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                "sef.economy", AuditService.AuditClass.METADATA_ONLY,
                "sef:economy", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand("sef:economy.pay", "pay", Set.of("pay"),
                "sef.commands.pay", CommandDefinition.AccessClass.PLAYER,
                Set.of(CommandDefinition.SourceType.PLAYER), CommandDefinition.TargetBehavior.REQUIRED_PLAYER,
                "sef.economy", AuditService.AuditClass.METADATA_ONLY,
                "sef:economy", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand("sef:economy.pay.toggle", "paytoggle", Set.of("paytoggle"),
                "sef.commands.paytoggle", CommandDefinition.AccessClass.PLAYER,
                Set.of(CommandDefinition.SourceType.PLAYER), CommandDefinition.TargetBehavior.SELF,
                "sef.economy", AuditService.AuditClass.METADATA_ONLY,
                "sef:economy", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand("sef:economy.pay.confirm", "payconfirmtoggle", Set.of("payconfirmtoggle"),
                "sef.commands.payconfirmtoggle", CommandDefinition.AccessClass.PLAYER,
                Set.of(CommandDefinition.SourceType.PLAYER), CommandDefinition.TargetBehavior.SELF,
                "sef.economy", AuditService.AuditClass.METADATA_ONLY,
                "sef:economy", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand("sef:economy.top", "balancetop", Set.of("balancetop", "baltop"),
                "sef.commands.balancetop", CommandDefinition.AccessClass.PLAYER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.NONE,
                "sef.economy", AuditService.AuditClass.METADATA_ONLY,
                "sef:economy", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand("sef:economy.worth", "worth", Set.of("worth"),
                "sef.commands.worth", CommandDefinition.AccessClass.PLAYER,
                Set.of(CommandDefinition.SourceType.PLAYER), CommandDefinition.TargetBehavior.SELF,
                "sef.economy", AuditService.AuditClass.METADATA_ONLY,
                "sef:economy", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand("sef:economy.sell", "sell", Set.of("sell"),
                "sef.commands.sell", CommandDefinition.AccessClass.PLAYER,
                Set.of(CommandDefinition.SourceType.PLAYER), CommandDefinition.TargetBehavior.SELF,
                "sef.economy", AuditService.AuditClass.METADATA_ONLY,
                "sef:economy", CommandDefinition.ConflictPolicy.PREFER_SEF);

        for (String action : List.of("give", "take", "set", "reset", "freeze", "unfreeze", "history", "import")) {
            registerDomainCommand("sef:economy.admin." + action, "eco " + action, Set.of(),
                    "sef.commands.eco." + action, CommandDefinition.AccessClass.ADMINISTRATOR,
                    STANDARD_COMMAND_SOURCES,
                    action.equals("import")
                            ? CommandDefinition.TargetBehavior.SERVER
                            : CommandDefinition.TargetBehavior.REQUIRED_PLAYER,
                    "sef.economy", AuditService.AuditClass.ADMIN_ACTION,
                    "sef:economy", CommandDefinition.ConflictPolicy.PREFER_SEF);
        }
        registerDomainCommand("sef:economy.worth.set", "setworth", Set.of("setworth"),
                "sef.commands.setworth", CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.NONE,
                "sef.economy", AuditService.AuditClass.ADMIN_ACTION,
                "sef:economy", CommandDefinition.ConflictPolicy.PREFER_SEF);
        for (String type : List.of(
                "balance", "buy", "sell", "trade", "free", "disposal",
                "kit", "heal", "repair", "time", "weather", "warp")) {
            registerDomainCommand(
                    "sef:economy.sign." + type,
                    "sign " + type,
                    Set.of(),
                    "sef.economy.sign." + type + ".use",
                    CommandDefinition.AccessClass.PLAYER,
                    Set.of(CommandDefinition.SourceType.PLAYER),
                    CommandDefinition.TargetBehavior.SELF,
                    "sef.economy.signs",
                    AuditService.AuditClass.METADATA_ONLY,
                    "sef:economy",
                    CommandDefinition.ConflictPolicy.PREFER_SEF);
            registerDomainCommand(
                    "sef:economy.sign.create." + type,
                    "sign create " + type,
                    Set.of(),
                    "sef.economy.sign." + type + ".create",
                    CommandDefinition.AccessClass.ADMINISTRATOR,
                    Set.of(CommandDefinition.SourceType.PLAYER),
                    CommandDefinition.TargetBehavior.SERVER,
                    "sef.economy.signs",
                    AuditService.AuditClass.ADMIN_ACTION,
                    "sef:economy",
                    CommandDefinition.ConflictPolicy.PREFER_SEF);
        }
        registerDomainCommand(
                "sef:economy.sign.manage",
                "eco sign",
                Set.of(),
                "sef.economy.sign.manage",
                CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES,
                CommandDefinition.TargetBehavior.SERVER,
                "sef.economy.signs",
                AuditService.AuditClass.ADMIN_ACTION,
                "sef:economy",
                CommandDefinition.ConflictPolicy.PREFER_SEF);
    }

    private static void registerDomainCommand(
            String id,
            String route,
            Set<String> roots,
            String permission,
            CommandDefinition.AccessClass access,
            Set<CommandDefinition.SourceType> sources,
            CommandDefinition.TargetBehavior targetBehavior,
            String feature,
            AuditService.AuditClass auditClass,
            String descriptor,
            CommandDefinition.ConflictPolicy conflictPolicy
    ) {
        catalog.register(new CommandDefinition(
                id,
                route,
                roots,
                "command." + id.replace(':', '.') + ".description",
                "command." + id.replace(':', '.') + ".usage",
                feature.substring(feature.indexOf('.') + 1),
                feature,
                Set.of(permission),
                access,
                sources,
                targetBehavior,
                id,
                false,
                auditClass,
                descriptor,
                "",
                "state is shown through immediate command feedback",
                "",
                "domain collections and projections have finite hard bounds",
                conflictPolicy,
                true,
                true));
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
        if (actionId.startsWith("sef:inventory.")
                || actionId.startsWith("sef:item.")
                || actionId.startsWith("sef:utility.")
                || actionId.startsWith("sef:gamemode.")
                || actionId.startsWith("sef:kit.")
                || actionId.startsWith("sef:workstation.")
                && !Set.of(
                        "sef:workstation.craft",
                        "sef:workstation.anvil",
                        "sef:workstation.enchant",
                        "sef:workstation.super_enchant",
                        "sef:workstation.repair").contains(actionId)) {
            return Duration.ofSeconds(ConfigHandler.config.utilityCooldownSeconds.get());
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
        BigDecimal configured = commandCosts.cost(actionId);
        if (configured.signum() > 0) {
            return configured;
        }
        return actionId.startsWith("sef:teleport.")
                && (actionId.endsWith(".use")
                || actionId.equals("sef:teleport.back")
                || actionId.equals("sef:teleport.spawn")
                || actionId.equals("sef:teleport.random"))
                ? teleportSettings.cost()
                : BigDecimal.ZERO;
    }

    private static EconomyRepository.Settings economyRepositorySettings() {
        return new EconomyRepository.Settings(
                ConfigHandler.config.economyCurrency.get(),
                ConfigHandler.config.economyMinorUnits.get(),
                ConfigHandler.config.economyDefaultBalance.get(),
                ConfigHandler.config.economyMinimumBalance.get(),
                ConfigHandler.config.economyMaximumBalance.get(),
                ConfigHandler.config.economyMaximumTransaction.get(),
                ConfigHandler.config.economyMaximumAccounts.get(),
                ConfigHandler.config.economyMaximumLedgerEntries.get(),
                ConfigHandler.config.economyMaximumPendingCosts.get(),
                ConfigHandler.config.economyMaximumWorthEntries.get());
    }

    private static EconomyService.Settings economyServiceSettings() {
        return new EconomyService.Settings(
                ConfigHandler.config.enableEconomy.get(),
                EconomyService.Mode.parse(ConfigHandler.config.economyProviderMode.get()),
                ConfigHandler.config.economyExternalProvider.get(),
                ConfigHandler.config.economyCurrencySymbol.get(),
                ConfigHandler.config.economyAllowOfflinePayments.get(),
                ConfigHandler.config.economyAllowSelfPayments.get(),
                ConfigHandler.config.economyConfirmationThreshold.get(),
                ConfigHandler.config.economyMaximumTransaction.get(),
                ConfigHandler.config.economyBalanceTopPageSize.get(),
                ConfigHandler.config.economyHistoryPageSize.get(),
                ConfigHandler.config.economyMaximumImportAccounts.get());
    }

    private static void ensureInitialized() {
        if (!initialized) {
            initialize();
        }
    }
}
