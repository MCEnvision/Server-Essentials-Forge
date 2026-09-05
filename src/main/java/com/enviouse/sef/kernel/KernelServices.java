package com.enviouse.sef.kernel;

import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.automation.AdministrativeExecutionService;
import com.enviouse.sef.automation.AliasService;
import com.enviouse.sef.automation.BundleService;
import com.enviouse.sef.automation.CommandProfileService;
import com.enviouse.sef.automation.FakeIdentityService;
import com.enviouse.sef.automation.SudoPolicyRepository;
import com.enviouse.sef.commandlog.CommandEventJournal;
import com.enviouse.sef.commandlog.CommandSpyRepository;
import com.enviouse.sef.commandlog.FileLogSink;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.config.modules.ModuleConfigRegistry;
import com.enviouse.sef.config.modules.ModuleConfigService;
import com.enviouse.sef.control.CommunityStateRepository;
import com.enviouse.sef.control.AccessLeaseRepository;
import com.enviouse.sef.control.AccessLeaseQuotaProvider;
import com.enviouse.sef.control.AdminLockRepository;
import com.enviouse.sef.control.AdminLockService;
import com.enviouse.sef.control.ApprovalRepository;
import com.enviouse.sef.control.ServerControlCatalog;
import com.enviouse.sef.control.ServerControlCommands;
import com.enviouse.sef.control.ServerControlExecutionService;
import com.enviouse.sef.control.MinecraftServerControlRuntime;
import com.enviouse.sef.control.ServerControlRepository;
import com.enviouse.sef.economy.CommandCostSchedule;
import com.enviouse.sef.economy.EconomyCostService;
import com.enviouse.sef.economy.EconomyRepository;
import com.enviouse.sef.economy.EconomyService;
import com.enviouse.sef.economy.EconomySignRepository;
import com.enviouse.sef.escrow.EscrowRepository;
import com.enviouse.sef.escrow.EscrowService;
import com.enviouse.sef.gui.GuiPreferenceRepository;
import com.enviouse.sef.gui.AdminPanelService;
import com.enviouse.sef.gui.UniversalGuiCatalog;
import com.enviouse.sef.gui.protocol.SefNetwork;
import com.enviouse.sef.gui.protocol.SefGuiServer;
import com.enviouse.sef.gui.protocol.OfflineActionRepository;
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
import com.enviouse.sef.fancytags.FancyTagObjectStore;
import com.enviouse.sef.fancytags.FancyTagService;
import com.enviouse.sef.disguise.DisguiseService;
import com.enviouse.sef.disguise.ProxyEntityIdAllocator;
import com.enviouse.sef.message.MessageService;
import com.enviouse.sef.moderation.ConnectionAddressService;
import com.enviouse.sef.moderation.ModerationRepository;
import com.enviouse.sef.permissions.PermissionManifest;
import com.enviouse.sef.permissions.PermissionCooldownResolver;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.recovery.InventoryRecoveryRepository;
import com.enviouse.sef.recovery.GraveRepository;
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

    private static boolean manifestPrepared;
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
    private static PermissionCooldownResolver cooldownDurations;
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
    private static UniversalGuiCatalog universalGuiCatalog;
    private static AdminPanelService adminPanels;
    private static SocialRepository social;
    private static ObservationService observations;
    private static CommandSpyRepository commandSpies;
    private static FileLogSink fileLogs;
    private static CommandEventJournal commandJournal;
    private static ModerationRepository moderation;
    private static ConnectionAddressService connectionAddresses;
    private static KitRepository kits;
    private static Map<String, PermissionNode<Boolean>> permissionNodes;
    private static BundleCompiler bundleCompiler;
    private static BundleService bundles;
    private static AliasService aliases;
    private static CommandProfileService commandProfiles;
    private static FakeIdentityService fakeIdentities;
    private static SudoPolicyRepository sudoPolicies;
    private static AdministrativeExecutionService administrativeExecution;
    private static FancyTagService fancyTags;
    private static DisguiseService disguises;
    private static ProxyEntityIdAllocator disguiseProxyIds;
    private static ServerControlRepository serverControls;
    private static AccessLeaseRepository accessLeases;
    private static AdminLockRepository adminLockRepository;
    private static AdminLockService adminLocks;
    private static ApprovalRepository approvals;
    private static ServerControlExecutionService serverControlExecutions;
    private static CommunityStateRepository communityState;
    private static InventoryRecoveryRepository inventoryRecovery;
    private static GraveRepository graves;
    private static EscrowRepository escrowRepository;
    private static EscrowService escrow;
    private static ModuleConfigService moduleConfigs;
    private static OfflineActionRepository offlineActions;
    private static Path preloadedAutomationRoot;

    private KernelServices() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        prepareManifest();
        ensureModuleConfigurationService();

        universalGuiCatalog = UniversalGuiCatalog.build(catalog, descriptors);
        if (!universalGuiCatalog.validate(catalog).isEmpty()) {
            throw new IllegalStateException(
                    "Universal GUI catalog validation failed. " + universalGuiCatalog.validate(catalog));
        }
        adminPanels = new AdminPanelService(catalog, capabilities);
        adminPanels.addPublicationListener(event -> {
            var server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                server.execute(() -> SefGuiServer.refreshAdminPanel(server, event.panelId()));
            }
        });

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
                confirmations,
                cooldownDurations);
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
        bundles = new BundleService(bundleCompiler);
        AliasCompiler aliasCompiler = new AliasCompiler(
                catalog,
                capabilities,
                bundles::publishedIds,
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
        aliases = new AliasService(aliasCompiler, ConfigHandler.config.kernelMaximumAliases.get());
        commandProfiles = new CommandProfileService();
        commandProfiles.setReferenceCheck(profileId ->
                bundles.publications().stream()
                        .flatMap(definition -> definition.steps().stream())
                        .anyMatch(step -> (step.kind() == BundleCompiler.StepKind.EXTERNAL_ACTOR_COMMAND
                                || step.kind() == BundleCompiler.StepKind.SERVER_COMMAND_PROFILE)
                                && step.targetId().equals(profileId))
                        || aliases.published().stream().anyMatch(alias ->
                        (alias.kind() == AliasCompiler.AliasKind.EXTERNAL_ACTOR_COMMAND
                                || alias.kind() == AliasCompiler.AliasKind.SERVER_COMMAND_PROFILE)
                                && alias.targetId().equals(profileId)));
        fakeIdentities = new FakeIdentityService(identities, messages);
        sudoPolicies = new SudoPolicyRepository();
        administrativeExecution =
                new AdministrativeExecutionService(AdministrativeExecutionService.Settings.defaults());
        fancyTags = new FancyTagService(fancyTagSettings());
        disguises = new DisguiseService(disguiseSettings());
        disguiseProxyIds = new ProxyEntityIdAllocator();
        serverControls = new ServerControlRepository();
        accessLeases = new AccessLeaseRepository(permissionNodes::containsKey);
        quotas.setProviders(List.of(
                new AccessLeaseQuotaProvider(accessLeases),
                new QuotaService.ContextMetadataProvider()));
        PermissionService.setLeaseResolver(new PermissionService.LeaseResolver() {
            @Override
            public PermissionService.LeaseEvaluation decide(
                    net.minecraft.server.level.ServerPlayer player,
                    String permissionId
            ) {
                var dimension = player.level().dimension().location().toString();
                var context = new AccessLeaseRepository.ScopeContext(
                        dimension,
                        dimension,
                        "",
                        Set.of());
                return accessLeases.decide(player.getUUID(), permissionId, context)
                        == AccessLeaseRepository.LeaseDecision.GRANTED
                        ? PermissionService.LeaseEvaluation.GRANTED
                        : PermissionService.LeaseEvaluation.ABSTAIN;
            }

            @Override
            public PermissionService.LeaseEvaluation decide(
                    java.util.UUID playerId,
                    String permissionId
            ) {
                return accessLeases.decide(
                                playerId,
                                permissionId,
                                AccessLeaseRepository.ScopeContext.offline())
                        == AccessLeaseRepository.LeaseDecision.GRANTED
                        ? PermissionService.LeaseEvaluation.GRANTED
                        : PermissionService.LeaseEvaluation.ABSTAIN;
            }
        });
        adminLockRepository = new AdminLockRepository();
        adminLocks = new AdminLockService(adminLockRepository);
        approvals = new ApprovalRepository();
        communityState = new CommunityStateRepository();
        inventoryRecovery = new InventoryRecoveryRepository();
        graves = new GraveRepository();
        escrowRepository = new EscrowRepository();
        escrow = new EscrowService(escrowRepository);
        serverControlExecutions = new ServerControlExecutionService(serverControls);
        MinecraftServerControlRuntime.registerHandlers(serverControlExecutions);

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
        offlineActions = new OfflineActionRepository();
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
        storage.register(offlineActions);
        storage.register(adminPanels);
        storage.register(bundles);
        storage.register(aliases);
        storage.register(commandProfiles);
        storage.register(fakeIdentities);
        storage.register(sudoPolicies);
        storage.register(fancyTags);
        storage.register(disguises);
        storage.register(serverControls);
        storage.register(accessLeases);
        storage.register(adminLockRepository);
        storage.register(approvals);
        storage.register(communityState);
        storage.register(inventoryRecovery);
        storage.register(graves);
        storage.register(escrowRepository);

        initialized = true;
        reloadConfiguration();
    }

    private static void ensureModuleConfigurationService() {
        if (moduleConfigs != null) {
            return;
        }
        moduleConfigs = new ModuleConfigService(new ModuleConfigRegistry());
        moduleConfigs.addPublicationListener(publication -> {
            ConfigHandler.publish(moduleConfigs);
            if (!initialized) {
                return;
            }
            SefNetwork.applyRuntimeMode(moduleConfigs.value("gui", "gui.mode"));
            reloadConfiguration();
            if (fileLogs != null) {
                fileLogs.reload();
            }
            var server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                server.execute(() -> {
                    if (!SefNetwork.enhancedGuiActive()) {
                        SefGuiServer.clear();
                        com.enviouse.sef.gui.protocol.SefSessionManager.instance().clear();
                    } else {
                        server.getPlayerList().getPlayers().forEach(
                                com.enviouse.sef.gui.protocol.SefSessionManager.instance()::refresh);
                    }
                    server.getPlayerList().getPlayers().forEach(
                            server.getCommands()::sendCommands);
                });
            }
        });
    }

    public static synchronized void prepareManifest() {
        if (manifestPrepared) {
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
        registerGuiDescriptors();

        catalog = new CommandCatalog(capabilities, descriptors);
        registerCoreCommands();
        registerTeleportCommands();
        registerSocialCommands();
        registerObservationCommands();
        registerModerationCommands();
        registerPhaseSevenCommands();
        registerEconomyCommands();
        registerPanelCommands();
        registerPhaseElevenCommands();
        registerPhaseTwelveCommands();
        registerPhaseThirteenCommands();
        registerLegacyCommands();
        cooldownDurations = new PermissionCooldownResolver(catalog.entries());
        for (PermissionManifest.Definition definition : PermissionManifest.definitions()) {
            if (!capabilities.contains(definition.id())) {
                capabilities.register(new CapabilityManifest.Capability(
                        definition.id(),
                        CapabilityManifest.inferType(definition.id()),
                        definition.defaultValue(),
                        definition.name(),
                        definition.description()));
                permissionNodes.put(definition.id(), definition.node());
            }
        }
        catalog.seal();
        manifestPrepared = true;
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
                Map.entry("sef.workstation.craft", moduleEnabled("craft")
                        && ConfigHandler.config.enableCraftingTableCommand.get()),
                Map.entry("sef.workstation.anvil", moduleEnabled("anvil")
                        && ConfigHandler.config.enableAnvilCommand.get()),
                Map.entry("sef.workstation.enchant", moduleEnabled("enchanting")
                        && ConfigHandler.config.enableEnchantingTableCommand.get()),
                Map.entry("sef.workstation.super_enchant", moduleEnabled("super_enchanting")
                        && ConfigHandler.config.enableSuperEnchantingTableCommand.get()),
                Map.entry("sef.workstation.repair", moduleEnabled("repair")
                        && ConfigHandler.config.enableRepairCommand.get()),
                Map.entry("sef.teleport", moduleEnabled("homes")
                        && ConfigHandler.config.enableTeleportEssentials.get()),
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
                Map.entry("sef.social", moduleEnabled("social")
                        && ConfigHandler.config.enableSocialEssentials.get()),
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
                Map.entry("sef.logging", moduleEnabled("logger")),
                Map.entry("sef.moderation", moduleEnabled("moderation")
                        && ConfigHandler.config.enableModerationEssentials.get()),
                Map.entry("sef.moderation.jails", ConfigHandler.config.enableModerationEssentials.get()
                        && ConfigHandler.config.enableJails.get()),
                Map.entry("sef.inventory", moduleEnabled("inventory")
                        && ConfigHandler.config.enableInventoryUtilities.get()),
                Map.entry("sef.kits", moduleEnabled("kits") && ConfigHandler.config.enableKits.get()),
                Map.entry("sef.utilities", moduleEnabled("player_utilities")
                        && ConfigHandler.config.enablePlayerUtilities.get()),
                Map.entry("sef.utilities.suicide", moduleEnabled("player_utilities")
                        && ConfigHandler.config.enablePlayerUtilities.get()
                        && ConfigHandler.config.enableSuicideCommand.get()),
                Map.entry("sef.gamemode", moduleEnabled("gamemode")
                        && ConfigHandler.config.enableGamemodeShortcuts.get()),
                Map.entry("sef.item.self", moduleEnabled("items")
                        && ConfigHandler.config.enableItemShortcut.get()),
                Map.entry("sef.item.give", moduleEnabled("items")),
                Map.entry("sef.workstation.additional", moduleEnabled("workstations")
                        && ConfigHandler.config.enableAdditionalWorkstations.get()),
                Map.entry("sef.economy", moduleEnabled("economy") && economy.settings().enabled()),
                Map.entry("sef.economy.signs", economy.settings().enabled()
                        && ConfigHandler.config.enableEconomySigns.get()),
                Map.entry("sef.automation", moduleEnabled("aliases") || moduleEnabled("bundles")),
                Map.entry("sef.fake", moduleEnabled("fake_actions")),
                Map.entry("sef.sudo", moduleEnabled("sudo") && ConfigHandler.config.enableSudo.get()),
                Map.entry("sef.run", moduleEnabled("run_and_silent")),
                Map.entry("sef.fancy_tags", moduleEnabled("fancy_tags")),
                Map.entry("sef.disguise", moduleEnabled("disguise")),
                Map.entry("sef.enchant.admin", moduleEnabled("super_enchanting")
                        && ConfigHandler.config.enableAdministrativeEnchanting.get()),
                Map.entry("sef.control", moduleEnabled("server_control")),
                Map.entry("sef.config", moduleEnabled("core")),
                Map.entry("sef.gui.policy", true),
                Map.entry("sef.gui", SefNetwork.enhancedGuiActive()));
        Map<String, Boolean> actionOverrides = replacementTeleportSettings.disabledActions().stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(action -> action, ignored -> false));
        featureGates.publish(new FeatureGateService.Snapshot(revision, features, Map.of(), actionOverrides));
        commandCosts = replacementCommandCosts;
        teleportSettings = replacementTeleportSettings;
        int delegatedPermissionLevel = Integer.parseInt(moduleConfigs.value(
                "sudo",
                "delegation.maximum_temporary_vanilla_permission_level"));
        AdministrativeExecutionService.DelegationProfile effectDelegationProfile =
                new AdministrativeExecutionService.DelegationProfile(
                        "effect",
                        1L,
                        Set.of("effect"),
                        "minecraft:effect",
                        delegatedPermissionLevel,
                        Set.of(),
                        Set.of());
        administrativeExecution.configure(new AdministrativeExecutionService.Settings(
                ConfigHandler.config.sudoAllowedCommands.get(),
                ConfigHandler.config.sudoDeniedCommands.get(),
                ConfigHandler.config.runAllowedCommands.get(),
                ConfigHandler.config.runDeniedCommands.get(),
                ConfigHandler.config.silentActorAllowedCommands.get(),
                ConfigHandler.config.silentActorDeniedCommands.get(),
                ConfigHandler.config.sudoMaximumCommandLength.get(),
                Boolean.parseBoolean(moduleConfigs.value("sudo", "delegation.enabled")),
                Boolean.parseBoolean(moduleConfigs.value(
                        "sudo",
                        "delegation.compatibility_boolean_syntax")),
                Boolean.parseBoolean(moduleConfigs.value(
                        "sudo",
                        "delegation.require_target_consent")),
                Boolean.parseBoolean(moduleConfigs.value(
                        "sudo",
                        "delegation.allow_self_delegation")),
                delegatedPermissionLevel,
                Integer.parseInt(moduleConfigs.value("sudo", "delegation.grant_lifetime_seconds")),
                Boolean.parseBoolean(moduleConfigs.value(
                        "sudo",
                        "delegation.confirmation_required")),
                Boolean.parseBoolean(moduleConfigs.value("sudo", "delegation.notify_target")),
                Boolean.parseBoolean(moduleConfigs.value(
                        "sudo",
                        "delegation.allow_unknown_external_permission_checks")),
                Boolean.parseBoolean(moduleConfigs.value("sudo", "delegation.allow_redirects")),
                Boolean.parseBoolean(moduleConfigs.value("sudo", "delegation.allow_forks")),
                Boolean.parseBoolean(moduleConfigs.value("sudo", "delegation.allow_async")),
                moduleConfigs.value("sudo", "delegation.allowed_roots"),
                moduleConfigs.value("sudo", "delegation.denied_roots"),
                Map.of(effectDelegationProfile.id(), effectDelegationProfile)));
        fakeIdentities.configure(new FakeIdentityService.Formats(
                ConfigHandler.config.fakeChatFormat.get(),
                ConfigHandler.config.fakeJoinFormat.get(),
                ConfigHandler.config.fakeLeaveFormat.get(),
                ConfigHandler.config.fakeMaximumMessageLength.get()));
        disguises.configure(disguiseSettings());
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
        cooldownDurations.invalidate();
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

    public static synchronized void preloadAutomationDefinitions(Path managedRoot) {
        ensureInitialized();
        Path normalized = managedRoot.toAbsolutePath().normalize();
        if (storage.started() || normalized.equals(preloadedAutomationRoot)) {
            return;
        }
        bundles.load(normalized);
        aliases.load(normalized);
        commandProfiles.load(normalized);
        fakeIdentities.load(normalized);
        sudoPolicies.load(normalized);
        preloadedAutomationRoot = normalized;
    }

    public static synchronized StorageCoordinator.FlushResult shutdown() {
        ensureInitialized();
        moduleConfigs.stop();
        fileLogs.shutdown();
        StorageCoordinator.FlushResult result = storage.shutdown();
        preloadedAutomationRoot = null;
        warmups.clear();
        confirmations.clear();
        teleportRequests.clear();
        observations.clearAll();
        commandJournal.clearRuntime();
        disguises.clearAll(DisguiseService.ClearReason.SHUTDOWN);
        disguiseProxyIds.clear();
        com.enviouse.sef.disguise.DisguiseRuntime.reset();
        com.enviouse.sef.control.MentionService.clear();
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

    public static PanelContracts.Registry descriptors() {
        ensureInitialized();
        return descriptors;
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

    public static PermissionCooldownResolver cooldownDurations() {
        ensureInitialized();
        return cooldownDurations;
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
                confirmations,
                cooldownDurations);
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

    public static OfflineActionRepository offlineActions() {
        ensureInitialized();
        return offlineActions;
    }

    public static UniversalGuiCatalog universalGuiCatalog() {
        ensureInitialized();
        return universalGuiCatalog;
    }

    public static AdminPanelService adminPanels() {
        ensureInitialized();
        return adminPanels;
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

    public static AliasService aliases() {
        ensureInitialized();
        return aliases;
    }

    public static BundleCompiler bundleCompiler() {
        ensureInitialized();
        return bundleCompiler;
    }

    public static BundleService bundles() {
        ensureInitialized();
        return bundles;
    }

    public static CommandProfileService commandProfiles() {
        ensureInitialized();
        return commandProfiles;
    }

    public static FakeIdentityService fakeIdentities() {
        ensureInitialized();
        return fakeIdentities;
    }

    public static SudoPolicyRepository sudoPolicies() {
        ensureInitialized();
        return sudoPolicies;
    }

    public static AdministrativeExecutionService administrativeExecution() {
        ensureInitialized();
        return administrativeExecution;
    }

    public static FancyTagService fancyTags() {
        ensureInitialized();
        return fancyTags;
    }

    public static DisguiseService disguises() {
        ensureInitialized();
        return disguises;
    }

    public static ProxyEntityIdAllocator disguiseProxyIds() {
        ensureInitialized();
        return disguiseProxyIds;
    }

    public static ServerControlRepository serverControls() {
        ensureInitialized();
        return serverControls;
    }

    public static AccessLeaseRepository accessLeases() {
        ensureInitialized();
        return accessLeases;
    }

    public static AdminLockRepository adminLockRepository() {
        ensureInitialized();
        return adminLockRepository;
    }

    public static AdminLockService adminLocks() {
        ensureInitialized();
        return adminLocks;
    }

    public static ApprovalRepository approvals() {
        ensureInitialized();
        return approvals;
    }

    public static ServerControlExecutionService serverControlExecutions() {
        ensureInitialized();
        return serverControlExecutions;
    }

    public static CommunityStateRepository communityState() {
        ensureInitialized();
        return communityState;
    }

    public static InventoryRecoveryRepository inventoryRecovery() {
        ensureInitialized();
        return inventoryRecovery;
    }

    public static GraveRepository graves() {
        ensureInitialized();
        return graves;
    }

    public static EscrowService escrow() {
        ensureInitialized();
        return escrow;
    }

    public static ModuleConfigService moduleConfigs() {
        ensureInitialized();
        return moduleConfigs;
    }

    public static synchronized ModuleConfigService.Publication preloadModuleConfiguration(Path configRoot) {
        ensureModuleConfigurationService();
        return moduleConfigs.start(configRoot, Runnable::run);
    }

    public static synchronized ModuleConfigService.Publication startModuleConfiguration(
            Path configRoot,
            java.util.function.Consumer<Runnable> publicationExecutor
    ) {
        ensureModuleConfigurationService();
        return moduleConfigs.start(configRoot, publicationExecutor);
    }

    public static PermissionNode<Boolean> permissionNode(String id) {
        ensureInitialized();
        return permissionNodes.get(id);
    }

    private static void registerGuiDescriptors() {
        registerCategoryDescriptor("sef:core", "sef.gui.category.core", "sef commands");
        registerCategoryDescriptor("sef:workstations", "sef.gui.category.workstations", "sef commands");
        registerCategoryDescriptor("sef:teleports", "sef.gui.category.teleports", "homes");
        registerCategoryDescriptor("sef:social", "sef.gui.category.social", "msg");
        registerCategoryDescriptor("sef:observation", "sef.gui.category.observation", "sef commandspy status");
        registerCategoryDescriptor("sef:moderation", "sef.gui.category.moderation", "warns");
        registerCategoryDescriptor("sef:protection", "sef.gui.category.protection", "sef commands");
        registerCategoryDescriptor("sef:inventory", "sef.gui.category.inventory", "itemdb");
        registerCategoryDescriptor("sef:kits", "sef.gui.category.kits", "kits");
        registerCategoryDescriptor("sef:utilities", "sef.gui.category.utilities", "getpos");
        registerCategoryDescriptor("sef:economy", "sef.gui.category.economy", "balance");
        registerCategoryDescriptor("sef:settings", "sef.gui.category.settings", "sef doctor");
        registerCategoryDescriptor("sef:integrations", "sef.gui.category.integrations", "sef doctor");
        registerCategoryDescriptor("sef:panels", "sef.gui.category.panels", "sef panel list");
        registerCategoryDescriptor("sef:aliases", "sef.gui.category.aliases", "sef commands");
        registerCategoryDescriptor("sef:tags", "sef.gui.category.tags", "sef commands");
        registerCategoryDescriptor("sef:identity", "sef.gui.category.identity", "nick");
        registerCategoryDescriptor("sef:control", "sef.gui.category.control", "sef control catalog");
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

    private static void registerCategoryDescriptor(String id, String titleKey, String fallback) {
        descriptors.register(new PanelContracts.PanelDescriptor(
                id,
                titleKey,
                6,
                "sef.kernel.gui.use",
                List.of(),
                new PanelContracts.CommandFallback(fallback, titleKey + ".usage")));
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
        for (String preference : List.of("mode", "pause", "hud", "blur", "motion", "page_size")) {
            registerDomainCommand(
                    "sef:gui.preference." + preference,
                    "sef client preference " + preference,
                    Set.of(),
                    "sef.commands.sef.client.preferences",
                    CommandDefinition.AccessClass.PLAYER,
                    Set.of(CommandDefinition.SourceType.PLAYER),
                    CommandDefinition.TargetBehavior.SELF,
                    "sef.core",
                    AuditService.AuditClass.METADATA_ONLY,
                    "sef:settings",
                    CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        }
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
        registerDomainCommand(
                "sef:teleport.player_warp.manage",
                "pwarp info",
                Set.of(),
                "sef.playerwarps.edit",
                CommandDefinition.AccessClass.PLAYER,
                Set.of(CommandDefinition.SourceType.PLAYER),
                CommandDefinition.TargetBehavior.SELF,
                "sef.teleport.player_warps",
                AuditService.AuditClass.METADATA_ONLY,
                "sef:teleports",
                CommandDefinition.ConflictPolicy.PREFER_SEF);
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
                "sef:commandspy.toggle", "sef commandspy toggle", Set.of("commandspy"),
                "sef.commands.commandspy", CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                "sef.observation.command", AuditService.AuditClass.COMMAND_OBSERVATION,
                "sef:observation", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand(
                "sef:commandspy.status", "sef commandspy status", Set.of(),
                "sef.commands.commandspy.status", CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                "sef.observation.command", AuditService.AuditClass.SENSITIVE_ACCESS,
                "sef:observation", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand(
                "sef:commandspy.recent", "sef commandspy recent", Set.of(),
                "sef.commands.commandspy.recent", CommandDefinition.AccessClass.ADMINISTRATOR,
                Set.of(CommandDefinition.SourceType.PLAYER), CommandDefinition.TargetBehavior.SELF,
                "sef.observation.command", AuditService.AuditClass.COMMAND_OBSERVATION,
                "sef:observation", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand(
                "sef:commandspy.audience", "sef commandspy everyone", Set.of(),
                "sef.commands.commandspy", CommandDefinition.AccessClass.ADMINISTRATOR,
                Set.of(CommandDefinition.SourceType.PLAYER), CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                "sef.observation.command", AuditService.AuditClass.COMMAND_OBSERVATION,
                "sef:observation", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand(
                "sef:commandspy.selected", "sef commandspy selected", Set.of(),
                "sef.commands.commandspy.selected", CommandDefinition.AccessClass.ADMINISTRATOR,
                Set.of(CommandDefinition.SourceType.PLAYER), CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                "sef.observation.command", AuditService.AuditClass.COMMAND_OBSERVATION,
                "sef:observation", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand(
                "sef:commandspy.scope", "sef commandspy scope", Set.of(),
                "sef.commands.commandspy", CommandDefinition.AccessClass.ADMINISTRATOR,
                Set.of(CommandDefinition.SourceType.PLAYER), CommandDefinition.TargetBehavior.SELF,
                "sef.observation.command", AuditService.AuditClass.COMMAND_OBSERVATION,
                "sef:observation", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand(
                "sef:commandspy.filter", "sef commandspy filter", Set.of(),
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
                "sef:logging.stream.configure", "sef logging stream enable", Set.of(),
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
                "sef:logging.filter.capture", "sef logging filter mode capture", Set.of(),
                "sef.commands.logging.filter.capture", CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.SERVER,
                "sef.logging", AuditService.AuditClass.FILE_LOG_CONTROL,
                "sef:observation", CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:logging.filter.view", "sef logging filter mode view", Set.of(),
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
        registerDomainCommand("sef:moderation.ban_ip", "ban-ip", Set.of("ban-ip", "banip"),
                "sef.commands.banip", CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.BOUNDED_PLAYERS,
                "sef.moderation", AuditService.AuditClass.NETWORK_ADDRESS_ACTION,
                "sef:moderation", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand("sef:moderation.tempban_ip", "tempban-ip", Set.of("tempban-ip", "tempbanip"),
                "sef.commands.tempbanip", CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.BOUNDED_PLAYERS,
                "sef.moderation", AuditService.AuditClass.NETWORK_ADDRESS_ACTION,
                "sef:moderation", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand("sef:moderation.pardon_ip", "pardon-ip",
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
        registerDomainCommand("sef:moderation.kick_ip", "kick-ip", Set.of("kick-ip", "kickip"),
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
                    action.equals("setjail")
                            ? Set.of(CommandDefinition.SourceType.PLAYER)
                            : STANDARD_COMMAND_SOURCES,
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
                Set.of("book", "condense", "disposal", "hat", "itemdb", "itemlore", "itemname", "more")
                        .contains(action)
                        ? Set.of(CommandDefinition.SourceType.PLAYER)
                        : STANDARD_COMMAND_SOURCES,
                CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                "sef.inventory", AuditService.AuditClass.ADMIN_ACTION,
                "sef:inventory", CommandDefinition.ConflictPolicy.PREFER_SEF));
        registerDomainCommand("sef:item.give.self", "i", Set.of("i"),
                "sef.commands.item.give.self", CommandDefinition.AccessClass.PLAYER,
                Set.of(CommandDefinition.SourceType.PLAYER), CommandDefinition.TargetBehavior.SELF,
                "sef.item.self", AuditService.AuditClass.ADMIN_ACTION,
                "sef:inventory", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand("sef:item.give.others", "give", Set.of("give"),
                "sef.commands.item.give.others", CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES, CommandDefinition.TargetBehavior.REQUIRED_PLAYER,
                "sef.item.give", AuditService.AuditClass.ADMIN_ACTION,
                "sef:inventory", CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand("sef:inventory.view", "invsee", Set.of("invsee"),
                "sef.commands.invsee.view", CommandDefinition.AccessClass.ADMINISTRATOR,
                Set.of(CommandDefinition.SourceType.PLAYER), CommandDefinition.TargetBehavior.REQUIRED_PLAYER,
                "sef.inventory", AuditService.AuditClass.METADATA_ONLY,
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
            String route = switch (action) {
                case "claim" -> "kit";
                case "list" -> "kits";
                case "show" -> "showkit";
                case "create" -> "createkit";
                case "delete" -> "delkit";
                case "reset" -> "kitreset";
                default -> "kit " + action;
            };
            registerDomainCommand("sef:kit." + action, route, roots,
                    permission, CommandDefinition.AccessClass.PLAYER,
                    Set.of("create", "list", "show").contains(action)
                            ? Set.of(CommandDefinition.SourceType.PLAYER)
                            : STANDARD_COMMAND_SOURCES,
                    CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                    "sef.kits", AuditService.AuditClass.ADMIN_ACTION,
                    "sef:kits", CommandDefinition.ConflictPolicy.PREFER_SEF);
        }
        for (String utility : List.of(
                "afk", "feed", "heal", "fly", "god", "speed", "exp", "ptime", "pweather",
                "rest", "suicide", "near", "getpos", "compass", "depth", "top", "bottom", "jump")) {
            registerDomainCommand("sef:utility." + utility, utility, Set.of(utility),
                    "sef.commands." + utility, CommandDefinition.AccessClass.PLAYER,
                    Set.of("afk", "bottom", "compass", "depth", "jump", "near", "top")
                            .contains(utility)
                            ? Set.of(CommandDefinition.SourceType.PLAYER)
                            : STANDARD_COMMAND_SOURCES,
                    CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                    utility.equals("suicide") ? "sef.utilities.suicide" : "sef.utilities",
                    AuditService.AuditClass.METADATA_ONLY,
                    "sef:utilities", CommandDefinition.ConflictPolicy.PREFER_SEF);
        }
        for (String mode : List.of("creative", "survival", "spectator", "adventure")) {
            Set<String> roots = switch (mode) {
                case "creative" -> Set.of("gmc");
                case "survival" -> Set.of("gms");
                case "spectator" -> Set.of("gmsp");
                default -> Set.of("gma");
            };
            registerDomainCommand("sef:gamemode." + mode, roots.iterator().next(), roots,
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
            registerDomainAction(
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
            registerDomainAction(
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

    private static void registerPanelCommands() {
        registerDomainCommand(
                "sef:panel.list",
                "sef panel list",
                Set.of(),
                "sef.commands.panel.list",
                CommandDefinition.AccessClass.STAFF,
                STANDARD_COMMAND_SOURCES,
                CommandDefinition.TargetBehavior.NONE,
                "sef.core",
                AuditService.AuditClass.SENSITIVE_ACCESS,
                "sef:panels",
                CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:panel.inspect",
                "sef panel inspect",
                Set.of(),
                "sef.commands.panel.inspect",
                CommandDefinition.AccessClass.STAFF,
                STANDARD_COMMAND_SOURCES,
                CommandDefinition.TargetBehavior.NONE,
                "sef.core",
                AuditService.AuditClass.SENSITIVE_ACCESS,
                "sef:panels",
                CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:panel.preview",
                "sef panel preview",
                Set.of(),
                "sef.commands.panel.preview",
                CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES,
                CommandDefinition.TargetBehavior.NONE,
                "sef.core",
                AuditService.AuditClass.WORKFLOW_EXECUTION,
                "sef:panels",
                CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:panel.run",
                "sef panel run",
                Set.of(),
                "sef.commands.panel.run",
                CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES,
                CommandDefinition.TargetBehavior.NONE,
                "sef.core",
                AuditService.AuditClass.WORKFLOW_EXECUTION,
                "sef:panels",
                CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        for (String action : List.of("create", "control_add", "control_remove", "delete")) {
            String route = action.startsWith("control_")
                    ? "sef panel draft control " + action.substring("control_".length())
                    : "sef panel draft " + action;
            registerDomainCommand(
                    "sef:panel.draft." + action,
                    route,
                    Set.of(),
                    "sef.commands.panel.draft",
                    CommandDefinition.AccessClass.ADMINISTRATOR,
                    STANDARD_COMMAND_SOURCES,
                    CommandDefinition.TargetBehavior.NONE,
                    "sef.core",
                    AuditService.AuditClass.CONFIG_DEFINITION,
                    "sef:panels",
                    CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        }
        registerDomainCommand(
                "sef:panel.publish",
                "sef panel publish",
                Set.of(),
                "sef.commands.panel.publish",
                CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES,
                CommandDefinition.TargetBehavior.NONE,
                "sef.core",
                AuditService.AuditClass.CONFIG_DEFINITION,
                "sef:panels",
                CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:panel.rollback",
                "sef panel rollback",
                Set.of(),
                "sef.commands.panel.rollback",
                CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES,
                CommandDefinition.TargetBehavior.NONE,
                "sef.core",
                AuditService.AuditClass.CONFIG_DEFINITION,
                "sef:panels",
                CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
    }

    private static void registerPhaseElevenCommands() {
        for (String action : List.of(
                "list", "inspect", "create", "validate", "publish",
                "disable", "rollback", "delete", "run", "help")) {
            registerDomainCommand(
                    "sef:alias." + action,
                    "sef alias " + action,
                    Set.of(),
                    "sef.commands.alias." + action,
                    action.equals("run")
                            ? CommandDefinition.AccessClass.TRUSTED_PLAYER
                            : CommandDefinition.AccessClass.ADMINISTRATOR,
                    STANDARD_COMMAND_SOURCES,
                    CommandDefinition.TargetBehavior.NONE,
                    "sef.automation",
                    action.equals("run")
                            ? AuditService.AuditClass.WORKFLOW_EXECUTION
                            : AuditService.AuditClass.CONFIG_DEFINITION,
                    "sef:aliases",
                    CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        }
        for (String action : List.of(
                "list", "inspect", "create", "edit", "preview", "publish",
                "run", "cancel", "recover", "disable", "rollback", "delete")) {
            registerDomainCommand(
                    "sef:bundle." + action,
                    "sef bundle " + action,
                    Set.of(),
                    "sef.commands.bundle." + action,
                    action.equals("run") || action.equals("cancel")
                            ? CommandDefinition.AccessClass.STAFF
                            : CommandDefinition.AccessClass.ADMINISTRATOR,
                    STANDARD_COMMAND_SOURCES,
                    action.equals("run")
                            ? CommandDefinition.TargetBehavior.BOUNDED_PLAYERS
                            : CommandDefinition.TargetBehavior.NONE,
                    "sef.automation",
                    action.equals("run") || action.equals("cancel") || action.equals("recover")
                            ? AuditService.AuditClass.WORKFLOW_EXECUTION
                            : AuditService.AuditClass.CONFIG_DEFINITION,
                    "sef:aliases",
                    CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        }
        for (String action : List.of(
                "list", "inspect", "create", "validate", "test", "publish",
                "reference", "enable", "execute", "rollback", "delete")) {
            registerDomainCommand(
                    "sef:profile." + action,
                    "sef profile " + action,
                    Set.of(),
                    "sef.commands.profile." + action,
                    CommandDefinition.AccessClass.OWNER,
                    STANDARD_COMMAND_SOURCES,
                    action.equals("execute")
                            ? CommandDefinition.TargetBehavior.BOUNDED_PLAYERS
                            : CommandDefinition.TargetBehavior.NONE,
                    "sef.automation",
                    action.equals("execute")
                            ? AuditService.AuditClass.WORKFLOW_EXECUTION
                            : AuditService.AuditClass.CONFIG_DEFINITION,
                    "sef:aliases",
                    CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        }
        registerDomainCommand(
                "sef:fake.join",
                "fakejoin",
                Set.of("fakejoin"),
                "sef.commands.fakejoin",
                CommandDefinition.AccessClass.STAFF,
                STANDARD_COMMAND_SOURCES,
                CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                "sef.fake",
                AuditService.AuditClass.ADMIN_ACTION,
                "sef:identity",
                CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand(
                "sef:fake.leave",
                "fakeleave",
                Set.of("fakeleave"),
                "sef.commands.fakeleave",
                CommandDefinition.AccessClass.STAFF,
                STANDARD_COMMAND_SOURCES,
                CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                "sef.fake",
                AuditService.AuditClass.ADMIN_ACTION,
                "sef:identity",
                CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand(
                "sef:fake.message",
                "fakemessage",
                Set.of("fakemessage"),
                "sef.commands.fakemessage",
                CommandDefinition.AccessClass.STAFF,
                STANDARD_COMMAND_SOURCES,
                CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                "sef.fake",
                AuditService.AuditClass.ADMIN_ACTION,
                "sef:identity",
                CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand(
                "sef:fake.rank_message",
                "fakerankmessage",
                Set.of("fakerankmessage"),
                "sef.commands.fakerankmessage",
                CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES,
                CommandDefinition.TargetBehavior.NONE,
                "sef.fake",
                AuditService.AuditClass.ADMIN_ACTION,
                "sef:identity",
                CommandDefinition.ConflictPolicy.PREFER_SEF);
        for (String action : List.of("profile", "scene", "schedule")) {
            registerDomainCommand(
                    "sef:fake." + action,
                    "sef fake " + action,
                    Set.of(),
                    "sef.commands.fake." + action,
                    CommandDefinition.AccessClass.ADMINISTRATOR,
                    STANDARD_COMMAND_SOURCES,
                    CommandDefinition.TargetBehavior.NONE,
                    "sef.fake",
                    AuditService.AuditClass.CONFIG_DEFINITION,
                    "sef:identity",
                    CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        }
        registerDomainCommand(
                "sef:sudo.run",
                "sudo run",
                Set.of("sudo"),
                "sef.commands.sudo.run",
                CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES,
                CommandDefinition.TargetBehavior.REQUIRED_PLAYER,
                "sef.sudo",
                AuditService.AuditClass.ADMIN_ACTION,
                "sef:identity",
                CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand(
                "sef:sudo.chat",
                "sudo chat",
                Set.of(),
                "sef.commands.sudo.chat",
                CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES,
                CommandDefinition.TargetBehavior.REQUIRED_PLAYER,
                "sef.sudo",
                AuditService.AuditClass.ADMIN_ACTION,
                "sef:identity",
                CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        for (String action : List.of("dryrun", "consent", "lock", "policy")) {
            registerDomainCommand(
                    "sef:sudo." + action,
                    "sudo " + action,
                    Set.of(),
                    "sef.commands.sudo." + action,
                    action.equals("consent")
                            ? CommandDefinition.AccessClass.PLAYER
                            : CommandDefinition.AccessClass.ADMINISTRATOR,
                    STANDARD_COMMAND_SOURCES,
                    action.equals("lock")
                            ? CommandDefinition.TargetBehavior.REQUIRED_PLAYER
                            : action.equals("policy")
                            ? CommandDefinition.TargetBehavior.OPTIONAL_PLAYER
                            : CommandDefinition.TargetBehavior.NONE,
                    "sef.sudo",
                    AuditService.AuditClass.ADMIN_ACTION,
                    "sef:identity",
                    CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        }
        registerDomainCommand(
                "sef:run.server",
                "run",
                Set.of("run"),
                "sef.commands.run",
                CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES,
                CommandDefinition.TargetBehavior.SERVER,
                "sef.run",
                AuditService.AuditClass.ADMIN_ACTION,
                "sef:core",
                CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand(
                "sef:silent.actor",
                "silent",
                Set.of("silent"),
                "sef.commands.silent.actor",
                CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES,
                CommandDefinition.TargetBehavior.NONE,
                "sef.run",
                AuditService.AuditClass.ADMIN_ACTION,
                "sef:core",
                CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand(
                "sef:silent.server",
                "silent server",
                Set.of(),
                "sef.commands.silent.server",
                CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES,
                CommandDefinition.TargetBehavior.SERVER,
                "sef.run",
                AuditService.AuditClass.ADMIN_ACTION,
                "sef:core",
                CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
    }

    private static void registerPhaseTwelveCommands() {
        Map<String, CommandDefinition.AccessClass> safeTagActions = Map.of(
                "status", CommandDefinition.AccessClass.PLAYER,
                "list", CommandDefinition.AccessClass.PLAYER,
                "view", CommandDefinition.AccessClass.PLAYER);
        for (String action : List.of(
                "status", "list", "view", "create", "duplicate", "edit", "validate",
                "publish", "hide", "archive", "restore", "delete",
                "revision.list", "revision.view", "revision.restore",
                "assign.player", "assign.group", "assign.team", "assign.default",
                "unassign", "assignments.player", "assignments.tag", "assignments.group",
                "report", "moderation.queue", "moderation.suspend", "moderation.clear",
                "category.list", "category.create", "category.edit", "category.delete",
                "palette.list", "palette.create", "palette.edit", "palette.delete",
                "template.list", "template.create", "template.edit", "template.delete",
                "import.scan", "import.inspect", "import.approve", "import.reject", "import.url",
                "export.png", "export.project", "export.manifest",
                "lease.view", "lease.override", "integrity.check", "integrity.repair",
                "cache.status", "cache.invalidate", "transfer.status", "audit",
                "backup.preview", "backup.create", "gc.preview", "gc.run", "reload", "doctor")) {
            registerDomainCommand(
                    "sef:tags." + action,
                    "sef tags " + switch (action) {
                        case "lease.view" -> "lease status";
                        case "lease.override" -> "lease release";
                        default -> action.replace('.', ' ');
                    },
                    action.equals("status") ? Set.of("fancytags") : Set.of(),
                    "sef.commands.tags." + action,
                    safeTagActions.getOrDefault(
                            action,
                            action.startsWith("moderation.")
                                    || action.startsWith("integrity.")
                                    || action.startsWith("backup.")
                                    || action.startsWith("gc.")
                                    || action.equals("delete")
                                    || action.equals("reload")
                                    ? CommandDefinition.AccessClass.OWNER
                                    : CommandDefinition.AccessClass.ADMINISTRATOR),
                    STANDARD_COMMAND_SOURCES,
                    action.startsWith("assign.") || action.startsWith("assignments.")
                            ? CommandDefinition.TargetBehavior.OPTIONAL_PLAYER
                            : CommandDefinition.TargetBehavior.NONE,
                    "sef.fancy_tags",
                    action.equals("delete") || action.equals("gc.run")
                            ? AuditService.AuditClass.DESTRUCTIVE
                            : action.equals("status") || action.equals("list") || action.equals("view")
                            ? AuditService.AuditClass.METADATA_ONLY
                            : AuditService.AuditClass.ADMIN_ACTION,
                    "sef:tags",
                    CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        }
        for (String action : List.of(
                "set.mob", "set.player", "set.preset", "clear", "status", "list",
                "preview", "ability", "options", "inspect", "conflicts", "preset.manage")) {
            String permission = switch (action) {
                case "set.mob" -> "sef.commands.disguise.mob";
                case "set.player" -> "sef.commands.disguise.player";
                case "set.preset" -> "sef.commands.disguise.preset";
                default -> "sef.commands.disguise." + action;
            };
            registerDomainCommand(
                    "sef:disguise." + action,
                    switch (action) {
                        case "set.mob" -> "disguise mob";
                        case "set.player" -> "disguise player";
                        case "set.preset" -> "disguise preset";
                        case "preset.manage" -> "disguise presets";
                        default -> "disguise " + action.replace('.', ' ');
                    },
                    switch (action) {
                        case "set.mob" -> Set.of("disguise");
                        case "clear" -> Set.of("undisguise");
                        case "ability" -> Set.of("dability");
                        default -> Set.of();
                    },
                    permission,
                    action.equals("status") || action.equals("list")
                            ? CommandDefinition.AccessClass.PLAYER
                            : action.equals("set.mob")
                            || action.equals("set.player")
                            || action.equals("set.preset")
                            || action.equals("clear")
                            || action.equals("ability")
                            ? CommandDefinition.AccessClass.TRUSTED_PLAYER
                            : CommandDefinition.AccessClass.STAFF,
                    STANDARD_COMMAND_SOURCES,
                    action.equals("inspect")
                            ? CommandDefinition.TargetBehavior.OPTIONAL_PLAYER
                            : CommandDefinition.TargetBehavior.SELF,
                    "sef.disguise",
                    action.equals("status") || action.equals("list")
                            ? AuditService.AuditClass.METADATA_ONLY
                            : AuditService.AuditClass.ADMIN_ACTION,
                    "sef:identity",
                    CommandDefinition.ConflictPolicy.PREFER_SEF);
        }
    }

    private static void registerPhaseThirteenCommands() {
        for (String action : List.of(
                "profile.publish",
                "profile.retire",
                "create",
                "renew",
                "suspend",
                "resume",
                "revoke",
                "reconcile")) {
            registerDomainCommand(
                    "sef:accessgrant." + action,
                    "accessgrant " + action.replace('.', ' '),
                    Set.of(),
                    "sef.commands.accessgrant." + action,
                    CommandDefinition.AccessClass.OWNER,
                    STANDARD_COMMAND_SOURCES,
                    action.equals("create")
                            ? CommandDefinition.TargetBehavior.REQUIRED_PLAYER
                            : CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                    "sef.control",
                    AuditService.AuditClass.ADMIN_ACTION,
                    "sef:control",
                    CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        }
        for (String action : List.of(
                "request",
                "approve",
                "revoke")) {
            registerDomainCommand(
                    "sef:approval." + action,
                    "approval " + action,
                    Set.of(),
                    "sef.commands.approval." + action,
                    CommandDefinition.AccessClass.OWNER,
                    STANDARD_COMMAND_SOURCES,
                    CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                    "sef.control",
                    AuditService.AuditClass.ADMIN_ACTION,
                    "sef:control",
                    CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        }
        registerDomainCommand(
                "sef:approval.list",
                "approval",
                Set.of("approvals"),
                "sef.commands.approval.list",
                CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES,
                CommandDefinition.TargetBehavior.NONE,
                "sef.control",
                AuditService.AuditClass.SENSITIVE_ACCESS,
                "sef:control",
                CommandDefinition.ConflictPolicy.PREFER_SEF);
        for (String action : List.of(
                "lock",
                "unlock",
                "challenge",
                "session.open",
                "session.close",
                "require",
                "release",
                "invalidate",
                "breakglass.open",
                "breakglass.close",
                "breakglass.profile")) {
            boolean selfAction = Set.of(
                    "lock", "unlock", "challenge", "session.open", "session.close").contains(action);
            registerDomainCommand(
                    "sef:adminlock." + action,
                    "adminlock " + action.replace('.', ' '),
                    Set.of(),
                    "sef.commands.adminlock." + action,
                    selfAction
                            ? CommandDefinition.AccessClass.STAFF
                            : CommandDefinition.AccessClass.OWNER,
                    STANDARD_COMMAND_SOURCES,
                    selfAction
                            ? CommandDefinition.TargetBehavior.SELF
                            : CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                    "sef.control",
                    AuditService.AuditClass.ADMIN_ACTION,
                    "sef:control",
                    CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        }
        registerDomainAction(
                "sef:workstation.super_enchant.mutate",
                "sef workstation super_enchant mutate",
                Set.of(),
                "sef.commands.superenchantingtable",
                CommandDefinition.AccessClass.TRUSTED_PLAYER,
                Set.of(CommandDefinition.SourceType.PLAYER),
                CommandDefinition.TargetBehavior.SELF,
                "sef.workstation.super_enchant",
                AuditService.AuditClass.ADMIN_ACTION,
                "sef:workstations",
                CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:enchant.apply",
                "sef enchant",
                Set.of("enchant"),
                "sef.commands.enchant",
                CommandDefinition.AccessClass.ADMINISTRATOR,
                STANDARD_COMMAND_SOURCES,
                CommandDefinition.TargetBehavior.REQUIRED_PLAYER,
                "sef.enchant.admin",
                AuditService.AuditClass.ADMIN_ACTION,
                "sef:workstations",
                CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand(
                "sef:enchant.remove",
                "sef enchant remove",
                Set.of(),
                "sef.commands.enchant.remove",
                CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES,
                CommandDefinition.TargetBehavior.REQUIRED_PLAYER,
                "sef.enchant.admin",
                AuditService.AuditClass.DESTRUCTIVE,
                "sef:workstations",
                CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:enchant.clear",
                "sef enchant clear",
                Set.of(),
                "sef.commands.enchant.clear",
                CommandDefinition.AccessClass.OWNER,
                STANDARD_COMMAND_SOURCES,
                CommandDefinition.TargetBehavior.REQUIRED_PLAYER,
                "sef.enchant.admin",
                AuditService.AuditClass.DESTRUCTIVE,
                "sef:workstations",
                CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:control.catalog",
                "sef control catalog",
                Set.of(),
                "sef.commands.control.catalog",
                CommandDefinition.AccessClass.STAFF,
                STANDARD_COMMAND_SOURCES,
                CommandDefinition.TargetBehavior.NONE,
                "sef.control",
                AuditService.AuditClass.METADATA_ONLY,
                "sef:control",
                CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        registerDomainCommand(
                "sef:control.status",
                "sef control status",
                Set.of(),
                "sef.commands.control.status",
                CommandDefinition.AccessClass.STAFF,
                STANDARD_COMMAND_SOURCES,
                CommandDefinition.TargetBehavior.NONE,
                "sef.control",
                AuditService.AuditClass.METADATA_ONLY,
                "sef:control",
                CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        for (ServerControlCatalog.FeatureDefinition feature : ServerControlCatalog.FEATURES) {
            Set<String> featureRoots = ServerControlCommands.directRoots().entrySet().stream()
                    .filter(entry -> entry.getValue().equals(feature.id()))
                    .map(Map.Entry::getKey)
                    .filter(root -> !root.equals("inventoryrestore"))
                    .filter(root -> catalog.rootOwner(root).isEmpty())
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
            CommandDefinition.AccessClass viewAccess = feature.sensitive()
                    ? CommandDefinition.AccessClass.STAFF
                    : feature.playerCreate()
                    ? CommandDefinition.AccessClass.PLAYER
                    : CommandDefinition.AccessClass.TRUSTED_PLAYER;
            registerDomainCommand(
                    "sef:control." + feature.id() + ".view",
                    "sef control " + feature.id(),
                    featureRoots,
                    "sef.commands.control." + feature.id() + ".view",
                    viewAccess,
                    STANDARD_COMMAND_SOURCES,
                    CommandDefinition.TargetBehavior.NONE,
                    "sef.control",
                    AuditService.AuditClass.METADATA_ONLY,
                    "sef:control",
                    CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
            registerDomainCommand(
                    "sef:control." + feature.id() + ".create",
                    "sef control " + feature.id() + " create",
                    Set.of(),
                    "sef.commands.control." + feature.id() + ".create",
                    feature.playerCreate()
                            ? CommandDefinition.AccessClass.PLAYER
                            : CommandDefinition.AccessClass.ADMINISTRATOR,
                    STANDARD_COMMAND_SOURCES,
                    CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                    "sef.control",
                    feature.dangerous()
                            ? AuditService.AuditClass.ADMIN_ACTION
                            : AuditService.AuditClass.METADATA_ONLY,
                    "sef:control",
                    CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
            registerDomainCommand(
                    "sef:control." + feature.id() + ".manage",
                    "sef control " + feature.id() + " manage",
                    feature.id().equals("inventory_recovery")
                            ? Set.of("inventoryrestore")
                            : Set.of(),
                    "sef.commands.control." + feature.id() + ".manage",
                    feature.dangerous()
                            ? CommandDefinition.AccessClass.OWNER
                            : CommandDefinition.AccessClass.ADMINISTRATOR,
                    STANDARD_COMMAND_SOURCES,
                    CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                    "sef.control",
                    feature.dangerous()
                            ? AuditService.AuditClass.DESTRUCTIVE
                            : AuditService.AuditClass.ADMIN_ACTION,
                    "sef:control",
                    CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        }
        Map<String, String> playerWorkflows = Map.ofEntries(
                Map.entry("rules.accept", "rules accept"),
                Map.entry("playtime_rewards.claim", "rewards claim"),
                Map.entry("daily_rewards.claim", "daily claim"),
                Map.entry("weekly_rewards.claim", "weekly claim"),
                Map.entry("reports.submit", "report"),
                Map.entry("tickets.submit", "ticket"),
                Map.entry("friends.request", "friend add"),
                Map.entry("friends.accept", "friend accept"),
                Map.entry("friends.remove", "friend remove"),
                Map.entry("interaction_blocks.set", "blocks add"),
                Map.entry("waypoints.set", "waypoint set"),
                Map.entry("waypoints.remove", "waypoint remove"),
                Map.entry("waypoints.go", "waypoint go"),
                Map.entry("polls.vote", "poll vote"),
                Map.entry("community_events.join", "events join"),
                Map.entry("community_events.leave", "events leave"),
                Map.entry("knowledge.bookmark", "knowledge bookmark"),
                Map.entry("invites.redeem", "invite redeem"),
                Map.entry("mentions.set", "mentions mode"),
                Map.entry("onboarding.complete", "onboarding step"),
                Map.entry("onboarding.dismiss", "onboarding dismiss"),
                Map.entry("sleep_vote.vote", "sleepvote yes"),
                Map.entry("death_compass.clear", "deathlocation clear"),
                Map.entry("graves.claim", "grave claim"),
                Map.entry("graves.locate", "grave locate"),
                Map.entry("graves.unlock", "grave unlock"),
                Map.entry("appeals.submit", "appeal"),
                Map.entry("access_applications.submit", "accessapply"),
                Map.entry("privacy.request", "privacy request"),
                Map.entry("server_calendar.subscribe", "calendar subscribe"));
        playerWorkflows.forEach((workflow, route) -> {
            int separator = workflow.lastIndexOf('.');
            String feature = workflow.substring(0, separator);
            String action = workflow.substring(separator + 1);
            registerDomainCommand(
                "sef:control." + workflow,
                route,
                Set.of(),
                "sef.commands.control." + feature + ".create",
                CommandDefinition.AccessClass.PLAYER,
                Set.of(CommandDefinition.SourceType.PLAYER),
                CommandDefinition.TargetBehavior.SELF,
                "sef.control",
                AuditService.AuditClass.WORKFLOW_EXECUTION,
                "sef:control",
                CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        });
        for (String action : List.of(
                "modules", "status", "inspect", "diff", "validate", "reload", "history",
                "rollback", "explain", "edit", "migrate", "documentation")) {
            registerDomainCommand(
                    "sef:config." + action,
                    "sef config " + (action.equals("edit") ? "set" : action),
                    Set.of(),
                    "sef.commands.config." + action,
                    action.equals("modules") || action.equals("status")
                            ? CommandDefinition.AccessClass.STAFF
                            : CommandDefinition.AccessClass.OWNER,
                    STANDARD_COMMAND_SOURCES,
                    CommandDefinition.TargetBehavior.NONE,
                    "sef.config",
                    action.equals("rollback")
                            ? AuditService.AuditClass.DESTRUCTIVE
                            : action.equals("modules") || action.equals("status")
                            || action.equals("inspect") || action.equals("diff")
                            || action.equals("explain")
                            ? AuditService.AuditClass.METADATA_ONLY
                            : AuditService.AuditClass.ADMIN_ACTION,
                    "sef:settings",
                    CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        }
        for (String action : List.of(
                "status", "enable", "disable", "auto", "module", "action", "sessions",
                "close", "reload", "doctor", "explain", "coverage")) {
            registerDomainCommand(
                    "sef:guis." + action,
                    "sef guis " + switch (action) {
                        case "enable" -> "on";
                        case "disable" -> "off";
                        default -> action;
                    },
                    Set.of(),
                    "sef.commands.guis." + action,
                    action.equals("status") || action.equals("coverage")
                            ? CommandDefinition.AccessClass.STAFF
                            : CommandDefinition.AccessClass.OWNER,
                    STANDARD_COMMAND_SOURCES,
                    action.equals("close")
                            ? CommandDefinition.TargetBehavior.OPTIONAL_PLAYER
                            : CommandDefinition.TargetBehavior.NONE,
                    "sef.gui.policy",
                    action.equals("status") || action.equals("coverage")
                            ? AuditService.AuditClass.METADATA_ONLY
                            : AuditService.AuditClass.ADMIN_ACTION,
                    "sef:settings",
                    CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
        }
        registerDomainCommand(
                "sef:gui.preference",
                "sef gui",
                Set.of(),
                "sef.commands.gui.preference",
                CommandDefinition.AccessClass.PLAYER,
                Set.of(CommandDefinition.SourceType.PLAYER),
                CommandDefinition.TargetBehavior.SELF,
                "sef.gui.policy",
                AuditService.AuditClass.METADATA_ONLY,
                "sef:settings",
                CommandDefinition.ConflictPolicy.CANONICAL_ONLY);
    }

    private static void registerLegacyCommands() {
        registerDomainCommand(
                "sef:chat.admin",
                "ac",
                Set.of("ac", "chat"),
                "sef.adminchat.use",
                CommandDefinition.AccessClass.STAFF,
                Set.of(CommandDefinition.SourceType.PLAYER),
                CommandDefinition.TargetBehavior.NONE,
                "sef.core",
                AuditService.AuditClass.METADATA_ONLY,
                "sef:social",
                CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand(
                "sef:chat.helpop",
                "helpop",
                Set.of("helpop"),
                "sef.helpop.send",
                CommandDefinition.AccessClass.PLAYER,
                Set.of(CommandDefinition.SourceType.PLAYER),
                CommandDefinition.TargetBehavior.NONE,
                "sef.core",
                AuditService.AuditClass.METADATA_ONLY,
                "sef:social",
                CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand(
                "sef:chat.helpop.reply",
                "helpopop",
                Set.of("helpopop"),
                "sef.helpop.reply",
                CommandDefinition.AccessClass.STAFF,
                Set.of(CommandDefinition.SourceType.PLAYER),
                CommandDefinition.TargetBehavior.REQUIRED_PLAYER,
                "sef.core",
                AuditService.AuditClass.METADATA_ONLY,
                "sef:social",
                CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand(
                "sef:identity.nick",
                "nick",
                Set.of("nick"),
                "sef.commands.nick",
                CommandDefinition.AccessClass.PLAYER,
                Set.of(CommandDefinition.SourceType.PLAYER),
                CommandDefinition.TargetBehavior.SELF,
                "sef.core",
                AuditService.AuditClass.METADATA_ONLY,
                "sef:identity",
                CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand(
                "sef:identity.nick.others",
                "nickfor",
                Set.of("nickfor"),
                "sef.commands.nick.others",
                CommandDefinition.AccessClass.STAFF,
                Set.of(CommandDefinition.SourceType.PLAYER),
                CommandDefinition.TargetBehavior.REQUIRED_PLAYER,
                "sef.core",
                AuditService.AuditClass.ADMIN_ACTION,
                "sef:identity",
                CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand(
                "sef:identity.whois",
                "whois",
                Set.of("whois"),
                "sef.commands.whois",
                CommandDefinition.AccessClass.PLAYER,
                Set.of(CommandDefinition.SourceType.PLAYER),
                CommandDefinition.TargetBehavior.REQUIRED_PLAYER,
                "sef.core",
                AuditService.AuditClass.METADATA_ONLY,
                "sef:identity",
                CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand(
                "sef:vanish.manage",
                "vanish",
                Set.of("vanish", "v"),
                "sef.vanish.1",
                CommandDefinition.AccessClass.TRUSTED_PLAYER,
                STANDARD_COMMAND_SOURCES,
                CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                "sef.core",
                AuditService.AuditClass.ADMIN_ACTION,
                "sef:identity",
                CommandDefinition.ConflictPolicy.PREFER_SEF);
        registerDomainCommand(
                "sef:teleport.request.blocked",
                "tpblocked",
                Set.of("tpblocked"),
                "sef.commands.tpblock",
                CommandDefinition.AccessClass.PLAYER,
                Set.of(CommandDefinition.SourceType.PLAYER),
                CommandDefinition.TargetBehavior.SELF,
                "sef.teleport.requests",
                AuditService.AuditClass.METADATA_ONLY,
                "sef:teleports",
                CommandDefinition.ConflictPolicy.PREFER_SEF);

        if (ConfigHandler.config.enableAnnouncements.get()) {
            registerDomainCommand(
                    "sef:announcement.text",
                    "textannouncement",
                    Set.of("textannouncement"),
                    "sef.announcements.manage",
                    CommandDefinition.AccessClass.ADMINISTRATOR,
                    STANDARD_COMMAND_SOURCES,
                    CommandDefinition.TargetBehavior.SERVER,
                    "sef.core",
                    AuditService.AuditClass.CONFIG_DEFINITION,
                    "sef:core",
                    CommandDefinition.ConflictPolicy.PREFER_SEF);
            registerDomainCommand(
                    "sef:announcement.command",
                    "commandannouncement",
                    Set.of("commandannouncement"),
                    "sef.announcements.command.manage",
                    CommandDefinition.AccessClass.ADMINISTRATOR,
                    STANDARD_COMMAND_SOURCES,
                    CommandDefinition.TargetBehavior.SERVER,
                    "sef.core",
                    AuditService.AuditClass.CONFIG_DEFINITION,
                    "sef:core",
                    CommandDefinition.ConflictPolicy.PREFER_SEF);
            registerDomainCommand(
                    "sef:announcement.title",
                    "titleannouncement",
                    Set.of("titleannouncement"),
                    "sef.announcements.title",
                    CommandDefinition.AccessClass.ADMINISTRATOR,
                    STANDARD_COMMAND_SOURCES,
                    CommandDefinition.TargetBehavior.BOUNDED_PLAYERS,
                    "sef.core",
                    AuditService.AuditClass.ADMIN_ACTION,
                    "sef:core",
                    CommandDefinition.ConflictPolicy.PREFER_SEF);
            registerDomainCommand(
                    "sef:announcement.toggle",
                    "toggle",
                    Set.of("toggle"),
                    "sef.announcements.toggle",
                    CommandDefinition.AccessClass.PLAYER,
                    Set.of(CommandDefinition.SourceType.PLAYER),
                    CommandDefinition.TargetBehavior.SELF,
                    "sef.core",
                    AuditService.AuditClass.METADATA_ONLY,
                    "sef:core",
                    CommandDefinition.ConflictPolicy.PREFER_SEF);
        }
        if (ConfigHandler.config.enableChatReplies.get()) {
            registerDomainCommand(
                    "sef:chat.reply",
                    "ans",
                    Set.of("ans"),
                    "sef.commands.ans",
                    CommandDefinition.AccessClass.PLAYER,
                    Set.of(CommandDefinition.SourceType.PLAYER),
                    CommandDefinition.TargetBehavior.REQUIRED_PLAYER,
                    "sef.core",
                    AuditService.AuditClass.METADATA_ONLY,
                    "sef:social",
                    CommandDefinition.ConflictPolicy.PREFER_SEF);
        }
        if (ConfigHandler.config.enableOpBulletin.get()) {
            registerDomainCommand(
                    "sef:announcement.bulletin",
                    "opbulletin",
                    Set.of("opbulletin"),
                    "sef.opbulletin.manage",
                    CommandDefinition.AccessClass.ADMINISTRATOR,
                    STANDARD_COMMAND_SOURCES,
                    CommandDefinition.TargetBehavior.SERVER,
                    "sef.core",
                    AuditService.AuditClass.CONFIG_DEFINITION,
                    "sef:core",
                    CommandDefinition.ConflictPolicy.PREFER_SEF);
        }
        if (ConfigHandler.config.enableBannedItems.get()) {
            registerDomainCommand(
                    "sef:banned.list",
                    "banned",
                    Set.of("banned"),
                    "sef.banned.view",
                    CommandDefinition.AccessClass.PLAYER,
                    STANDARD_COMMAND_SOURCES,
                    CommandDefinition.TargetBehavior.NONE,
                    "sef.core",
                    AuditService.AuditClass.SENSITIVE_ACCESS,
                    "sef:moderation",
                    CommandDefinition.ConflictPolicy.PREFER_SEF);
        }
        if (ConfigHandler.config.enableClearChat.get()) {
            registerDomainCommand(
                    "sef:chat.clear",
                    "clearchat",
                    Set.of("clearchat", "cc"),
                    "sef.commands.clearchat",
                    CommandDefinition.AccessClass.ADMINISTRATOR,
                    STANDARD_COMMAND_SOURCES,
                    CommandDefinition.TargetBehavior.SERVER,
                    "sef.core",
                    AuditService.AuditClass.ADMIN_ACTION,
                    "sef:moderation",
                    CommandDefinition.ConflictPolicy.PREFER_SEF);
        }
        if (ConfigHandler.config.enableCheckAlts.get()) {
            registerDomainCommand(
                    "sef:identity.alts",
                    "checkalts",
                    Set.of("checkalts"),
                    "sef.commands.checkalts",
                    CommandDefinition.AccessClass.ADMINISTRATOR,
                    STANDARD_COMMAND_SOURCES,
                    CommandDefinition.TargetBehavior.OPTIONAL_PLAYER,
                    "sef.core",
                    AuditService.AuditClass.SENSITIVE_ACCESS,
                    "sef:identity",
                    CommandDefinition.ConflictPolicy.PREFER_SEF);
        }
        if (ConfigHandler.config.enableCountdown.get()) {
            registerDomainCommand(
                    "sef:announcement.countdown",
                    "countdown",
                    Set.of("countdown"),
                    "sef.commands.countdown",
                    CommandDefinition.AccessClass.ADMINISTRATOR,
                    STANDARD_COMMAND_SOURCES,
                    CommandDefinition.TargetBehavior.SERVER,
                    "sef.core",
                    AuditService.AuditClass.ADMIN_ACTION,
                    "sef:core",
                    CommandDefinition.ConflictPolicy.PREFER_SEF);
        }
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
        registerDomainAction(
                id,
                route,
                roots,
                permission,
                access,
                sources,
                targetBehavior,
                feature,
                auditClass,
                descriptor,
                conflictPolicy,
                true);
    }

    private static void registerDomainAction(
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
        registerDomainAction(
                id,
                route,
                roots,
                permission,
                access,
                sources,
                targetBehavior,
                feature,
                auditClass,
                descriptor,
                conflictPolicy,
                false);
    }

    private static void registerDomainAction(
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
            CommandDefinition.ConflictPolicy conflictPolicy,
            boolean playerFacing
    ) {
        String hudDescriptor = hudDescriptor(id);
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
                hudDescriptor,
                hudDescriptor.isBlank() ? "state is shown through immediate command feedback" : "",
                "",
                "domain collections and projections have finite hard bounds",
                conflictPolicy,
                playerFacing,
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
        String hudDescriptor = hudDescriptor(id);
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
                hudDescriptor,
                hudDescriptor.isBlank() ? "social state is shown through immediate command feedback" : "",
                "",
                "social collections are bounded by repository and quota policy",
                CommandDefinition.ConflictPolicy.PREFER_SEF,
                true,
                true));
    }

    private static String hudDescriptor(String actionId) {
        return switch (actionId) {
            case "sef:social.spy" -> "social_spy";
            case "sef:commandspy.toggle", "sef:commandspy.audience" -> "command_spy";
            case "sef:utility.afk" -> "afk";
            case "sef:utility.fly" -> "fly";
            case "sef:utility.god" -> "god";
            default -> "";
        };
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
                Map.ofEntries(
                        Map.entry("sef.homes.3", 3L),
                        Map.entry("sef.homes.5", 5L),
                        Map.entry("sef.homes.10", 10L),
                        Map.entry("sef.homes.25", 25L),
                        Map.entry("sef.homes.50", 50L),
                        Map.entry("sef.homes.100", 100L),
                        Map.entry("sef.homes.250", 250L),
                        Map.entry("sef.homes.500", 500L),
                        Map.entry("sef.homes.1000", 1000L))));
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
                Map.ofEntries(
                        Map.entry("sef.playerwarps.10", 10L),
                        Map.entry("sef.playerwarps.25", 25L),
                        Map.entry("sef.playerwarps.50", 50L),
                        Map.entry("sef.playerwarps.100", 100L),
                        Map.entry("sef.playerwarps.250", 250L),
                        Map.entry("sef.playerwarps.500", 500L),
                        Map.entry("sef.playerwarps.1000", 1000L))));
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
        return PermissionCooldownResolver.internalDefault(actionId);
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

    private static boolean moduleEnabled(String moduleId) {
        return moduleConfigs == null
                || moduleConfigs.module(moduleId)
                .map(com.enviouse.sef.config.modules.ModuleConfigService.ModuleSnapshot::enabled)
                .orElse(true);
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

    private static FancyTagService.Settings fancyTagSettings() {
        return new FancyTagService.Settings(
                ConfigHandler.config.enableFancyTags.get(),
                ConfigHandler.config.fancyTagsMaximumTags.get(),
                ConfigHandler.config.fancyTagsMaximumCategories.get(),
                ConfigHandler.config.fancyTagsMaximumAssignments.get(),
                ConfigHandler.config.fancyTagsMaximumAssignmentsPerTarget.get(),
                ConfigHandler.config.fancyTagsMaximumRevisionsPerTag.get(),
                256,
                3,
                16,
                128,
                Duration.ofSeconds(120),
                Duration.ofSeconds(30),
                Duration.ofSeconds(ConfigHandler.config.fancyTagsImportSettleSeconds.get()),
                new FancyTagObjectStore.Limits(
                        ConfigHandler.config.fancyTagsMaximumWidth.get(),
                        ConfigHandler.config.fancyTagsMaximumHeight.get(),
                        ConfigHandler.config.fancyTagsMaximumPixels.get(),
                        ConfigHandler.config.fancyTagsMaximumEncodedBytes.get(),
                        ConfigHandler.config.fancyTagsMaximumDecodedBytes.get(),
                        ConfigHandler.config.fancyTagsMaximumStoreBytes.get(),
                        ConfigHandler.config.fancyTagsMaximumImportCandidates.get()));
    }

    private static DisguiseService.Settings disguiseSettings() {
        return new DisguiseService.Settings(
                ConfigHandler.config.enableDisguises.get(),
                ConfigHandler.config.disguiseTraitsEnabled.get(),
                ConfigHandler.config.disguiseAbilitiesEnabled.get(),
                ConfigHandler.config.disguiseClearOnLogout.get(),
                ConfigHandler.config.disguiseClearOnDeath.get(),
                ConfigHandler.config.disguiseMaximumActive.get(),
                PermissionCooldownResolver.internalDefault("sef:disguise.ability.blaze_fireball"),
                ConfigHandler.config.disguiseBlazeFireballDamage.get(),
                ConfigHandler.config.disguiseBlazeFireSeconds.get());
    }

    private static void ensureInitialized() {
        if (!initialized) {
            initialize();
        }
    }
}
