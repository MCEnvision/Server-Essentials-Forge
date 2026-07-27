package com.enviouse.sef.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ConfigHandler {
	private static final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
	public static final ConfigBuilder config = new ConfigBuilder(builder);
	public static ModConfigSpec spec = builder.build();

    // NOTE (NeoForge 1.21.1 port): the Forge-era reloadFromDisk() forced an on-demand re-read of
    // config/sef/common.toml via ModConfigSpec#setConfig(CommentedFileConfig). That method does NOT
    // exist on NeoForge's ModConfigSpec (its only acceptor is acceptConfig(ILoadedConfig), and
    // ILoadedConfig is a sealed type a mod cannot construct), so an on-demand disk re-read is no
    // longer supported. NeoForge's FML owns config loading: it watches the file and fires
    // ModConfigEvent.Reloading on external edits, and the ConfigValue getters always reflect the
    // currently-loaded config. Reload is therefore driven by ConfigurationEventHandler (which listens
    // for ModConfigEvent.Loading/Reloading) and /sef reload re-applies the loaded values via
    // ConfigurationEventHandler.reloadConfigOptions(). See PORTING_NOTES.md (behavior change).

	public static class ConfigBuilder {
		public final ModConfigSpec.ConfigValue<String> playerNameFormat;
		public final ModConfigSpec.ConfigValue<String> chatMessageFormat;
		public final ModConfigSpec.ConfigValue<String> chatMessageColor;
		public final ModConfigSpec.ConfigValue<String> timestampFormat;
		public final ModConfigSpec.ConfigValue<String> discordBotToken;

		public final ModConfigSpec.ConfigValue<Integer> maximumNicknameLength;
		public final ModConfigSpec.ConfigValue<Integer> minimumNicknameLength;
		public final ModConfigSpec.ConfigValue<String> metaJoinSeparator;
		public final ModConfigSpec.ConfigValue<Integer> maxPrefixesDisplayed;
		public final ModConfigSpec.ConfigValue<Integer> maxSuffixesDisplayed;

		// Master module toggles — defined in the [modules] section (see constructor)
		public final ModConfigSpec.ConfigValue<Boolean> enableVanishSystem;
		public final ModConfigSpec.ConfigValue<Boolean> enableChatFormatting;
		public final ModConfigSpec.ConfigValue<Boolean> enableOpBulletin;

		public final ModConfigSpec.ConfigValue<Boolean> enableTimestamp;
		public final ModConfigSpec.ConfigValue<Boolean> enableFtbEssentials;
		public final ModConfigSpec.ConfigValue<Boolean> enableLuckPerms;
		public final ModConfigSpec.ConfigValue<Boolean> enableMarkdown;
		public final ModConfigSpec.ConfigValue<Boolean> enableColorsCommand;
		public final ModConfigSpec.ConfigValue<Boolean> enableTabListIntegration;
		public final ModConfigSpec.ConfigValue<Boolean> enableMetadataInTabList;
		public final ModConfigSpec.ConfigValue<Boolean> enableNicknamesInTabList;
		public final ModConfigSpec.ConfigValue<Boolean> enableWhoisCommand;
		public final ModConfigSpec.ConfigValue<Boolean> enableChatNicknameCommand;
		public final ModConfigSpec.ConfigValue<Boolean> autoEnableChatNicknameCommand;
		public final ModConfigSpec.ConfigValue<Boolean> enableDiscordBotIntegration;
		public final ModConfigSpec.ConfigValue<Boolean> enableCustomTabHeaderFooter;
		public final ModConfigSpec.ConfigValue<String> tabHeaderFormat;
		public final ModConfigSpec.ConfigValue<String> tabFooterFormat;
		public final ModConfigSpec.ConfigValue<Boolean> enableCraftingTableCommand;
		public final ModConfigSpec.ConfigValue<Boolean> enableAnvilCommand;
		public final ModConfigSpec.ConfigValue<Boolean> enableEnchantingTableCommand;
		public final ModConfigSpec.ConfigValue<Boolean> enableSuperEnchantingTableCommand;
		public final ModConfigSpec.ConfigValue<Boolean> enableRepairCommand;
		public final ModConfigSpec.ConfigValue<Boolean> enableCraftAlias;
		public final ModConfigSpec.ConfigValue<Boolean> enableAnvilAlias;
		public final ModConfigSpec.ConfigValue<Boolean> enableEnchantingTableAlias;
		public final ModConfigSpec.ConfigValue<Boolean> enableSuperEnchantingTableAlias;
		public final ModConfigSpec.ConfigValue<Integer> craftingTableCooldownSeconds;
		public final ModConfigSpec.ConfigValue<Integer> anvilCooldownSeconds;
		public final ModConfigSpec.ConfigValue<Integer> enchantingTableCooldownSeconds;
		public final ModConfigSpec.ConfigValue<Integer> superEnchantingTableCooldownSeconds;
		public final ModConfigSpec.ConfigValue<Integer> repairCooldownSeconds;
		public final ModConfigSpec.ConfigValue<Integer> superEnchantingMaxLevel;
		public final ModConfigSpec.ConfigValue<Integer> superEnchantingMinLevel;
		public final ModConfigSpec.ConfigValue<Boolean> superEnchantingAllowUnsafe;
		public final ModConfigSpec.ConfigValue<String> workstationCooldownMessage;
		public final ModConfigSpec.ConfigValue<String> repairSuccessMessage;
		public final ModConfigSpec.ConfigValue<String> repairNotHeldMessage;
		public final ModConfigSpec.ConfigValue<String> repairNotNeededMessage;
		public final ModConfigSpec.ConfigValue<Boolean> enableAnnouncements;
		public final ModConfigSpec.ConfigValue<Integer> announcementIntervalSeconds;
		public final ModConfigSpec.ConfigValue<Boolean> announcementUseRandomOrder;
		public final ModConfigSpec.ConfigValue<Boolean> enableCommandAnnouncements;
		public final ModConfigSpec.ConfigValue<String> commandAnnouncementAllowedCommands;
		public final ModConfigSpec.ConfigValue<String> commandAnnouncementDeniedCommands;
		public final ModConfigSpec.ConfigValue<Integer> commandAnnouncementMaximumCommandLength;
		public final ModConfigSpec.ConfigValue<Boolean> commandAnnouncementAllowLeadingSlash;
		public final ModConfigSpec.ConfigValue<Boolean> commandAnnouncementAllowSelectors;
		public final ModConfigSpec.ConfigValue<Boolean> enableFilterSystem;
		public final ModConfigSpec.ConfigValue<Boolean> enableMessagingSystem;
		public final ModConfigSpec.ConfigValue<Boolean> enableChatReplies;
		public final ModConfigSpec.ConfigValue<Integer> replySummaryLength;
		public final ModConfigSpec.ConfigValue<Boolean> enableHelpOp;
		public final ModConfigSpec.ConfigValue<Boolean> enableAdminChat;
		public final ModConfigSpec.ConfigValue<Boolean> enableBannedItems;
		public final ModConfigSpec.ConfigValue<Boolean> enableBannedBlockScanning;
		public final ModConfigSpec.ConfigValue<Integer> bannedBlockScanRadius;
		public final ModConfigSpec.ConfigValue<Integer> bannedBlockScanInterval;
		public final ModConfigSpec.ConfigValue<Integer> bannedInventoryScanInterval;
		public final ModConfigSpec.ConfigValue<Integer> bannedBlockScanBudget;
		public final ModConfigSpec.ConfigValue<String> bannedItemRemovedMsg;
		public final ModConfigSpec.ConfigValue<String> bannedAnnounceFormat;
		public final ModConfigSpec.ConfigValue<Boolean> enableCountdown;
		public final ModConfigSpec.ConfigValue<String> countdownTitleFormat;
		public final ModConfigSpec.ConfigValue<String> countdownSubtitleFormat;
		public final ModConfigSpec.ConfigValue<String> countdownChatFormat;
		public final ModConfigSpec.ConfigValue<Boolean> enableMotdSystem;
		public final ModConfigSpec.ConfigValue<Boolean> applyMotdOnStartup;

		// FTB Mute Integration
		public final ModConfigSpec.ConfigValue<Boolean> enableFtbMuteIntegration;
		public final ModConfigSpec.ConfigValue<String> mutedPlayerMessage;
		public final ModConfigSpec.ConfigValue<String> mutedMessageOpFormat;
		public final ModConfigSpec.ConfigValue<Boolean> sendMutedMessageToOps;

		// Persistent Mute System
		public final ModConfigSpec.ConfigValue<Boolean> enableMuteSystem;
		public final ModConfigSpec.ConfigValue<String> muteNotifyPlayerFormat;
		public final ModConfigSpec.ConfigValue<String> muteAdminNotifyFormat;
		public final ModConfigSpec.ConfigValue<String> unmuteNotifyPlayerFormat;
		public final ModConfigSpec.ConfigValue<String> unmuteAdminNotifyFormat;
		public final ModConfigSpec.ConfigValue<String> muteConfirmFormat;
		public final ModConfigSpec.ConfigValue<String> unmuteConfirmFormat;
		public final ModConfigSpec.ConfigValue<String> muteAlreadyMutedMsg;
		public final ModConfigSpec.ConfigValue<String> muteNotMutedMsg;
		public final ModConfigSpec.ConfigValue<String> muteListHeaderFormat;
		public final ModConfigSpec.ConfigValue<String> muteListEntryFormat;
		public final ModConfigSpec.ConfigValue<String> muteListEmptyMsg;
		public final ModConfigSpec.ConfigValue<String> mutedPlayerChatMsg;
		public final ModConfigSpec.ConfigValue<String> mutedPlayerChatMsgWithRemaining;

		// InvSee System
		public final ModConfigSpec.ConfigValue<Boolean> enableInvSee;
		public final ModConfigSpec.ConfigValue<Boolean> invSeeDisableFtbInvsee;
		public final ModConfigSpec.ConfigValue<String> invSeeTitle;
		public final ModConfigSpec.ConfigValue<String> invSeeArmorLabel;
		public final ModConfigSpec.ConfigValue<String> invSeeOffhandLabel;
		public final ModConfigSpec.ConfigValue<String> invSeeCuriosLabel;
		public final ModConfigSpec.ConfigValue<String> invSeeMainInvLabel;
		public final ModConfigSpec.ConfigValue<String> invSeeNextPageLabel;
		public final ModConfigSpec.ConfigValue<String> invSeePrevPageLabel;
		public final ModConfigSpec.ConfigValue<Boolean> invSeeReadOnly;
		public final ModConfigSpec.ConfigValue<Boolean> invSeeAuditModifications;

		// Clear Chat System
		public final ModConfigSpec.ConfigValue<Boolean> enableClearChat;
		public final ModConfigSpec.ConfigValue<Integer> clearChatLineCount;
		public final ModConfigSpec.ConfigValue<String> clearChatSuccessMsg;
		public final ModConfigSpec.ConfigValue<String> clearChatAllSuccessMsg;
		public final ModConfigSpec.ConfigValue<String> clearChatSelfMsg;

		// Sudo System
		public final ModConfigSpec.ConfigValue<Boolean> enableSudo;
		public final ModConfigSpec.ConfigValue<String> sudoExecutedMsg;
		public final ModConfigSpec.ConfigValue<String> sudoNotifyMsg;
		public final ModConfigSpec.ConfigValue<String> sudoAllowedCommands;
		public final ModConfigSpec.ConfigValue<String> sudoDeniedCommands;
		public final ModConfigSpec.ConfigValue<Boolean> sudoNotifyTarget;
		public final ModConfigSpec.ConfigValue<Integer> sudoMaximumCommandLength;

		// Inventory Lock System
		public final ModConfigSpec.ConfigValue<Boolean> enableInvLock;
		public final ModConfigSpec.ConfigValue<String> invLockLockedMsg;
		public final ModConfigSpec.ConfigValue<String> invLockUnlockedMsg;
		public final ModConfigSpec.ConfigValue<String> invLockAdminLockMsg;
		public final ModConfigSpec.ConfigValue<String> invLockAdminUnlockMsg;
		public final ModConfigSpec.ConfigValue<String> invLockBlockedMsg;

		// Disable Building System
		public final ModConfigSpec.ConfigValue<Boolean> enableDisableBuilding;
		public final ModConfigSpec.ConfigValue<String> dbEnabledMsg;
		public final ModConfigSpec.ConfigValue<String> dbDisabledMsg;
		public final ModConfigSpec.ConfigValue<String> dbPlayerNotifyMsg;
		public final ModConfigSpec.ConfigValue<String> dbBlockedMsg;

		// Check Alts System
		public final ModConfigSpec.ConfigValue<Boolean> enableCheckAlts;
		public final ModConfigSpec.ConfigValue<String> checkAltsHeaderFormat;
		public final ModConfigSpec.ConfigValue<String> checkAltsEntryFormat;
		public final ModConfigSpec.ConfigValue<String> checkAltsNoAltsMsg;
		public final ModConfigSpec.ConfigValue<Boolean> altTrackingCollectAddresses;
		public final ModConfigSpec.ConfigValue<Integer> altTrackingRetentionDays;
		public final ModConfigSpec.ConfigValue<Boolean> altTrackingHashAddresses;

		public final ModConfigSpec.ConfigValue<Boolean> nicknameUniqueOnline;
		public final ModConfigSpec.ConfigValue<Boolean> nicknameUniqueKnownProfiles;
		public final ModConfigSpec.ConfigValue<Boolean> nicknameAllowDuplicateWithUsernameHover;

		public final ModConfigSpec.ConfigValue<Integer> tabUpdateIntervalTicks;
		public final ModConfigSpec.ConfigValue<Integer> securityAuditRetentionDays;
		public final ModConfigSpec.ConfigValue<Integer> securityAuditMaximumFileMiB;
		public final ModConfigSpec.ConfigValue<Integer> kernelMaximumAliases;
		public final ModConfigSpec.ConfigValue<Integer> kernelMaximumBundleSteps;
		public final ModConfigSpec.ConfigValue<Integer> kernelMaximumBundleDepth;
		public final ModConfigSpec.ConfigValue<Integer> kernelMaximumTargets;
		public final ModConfigSpec.ConfigValue<Integer> kernelMaximumTargetSteps;
		public final ModConfigSpec.ConfigValue<Integer> kernelLocationHistoryEntries;
		public final ModConfigSpec.ConfigValue<Integer> kernelPersistentCooldownMinimumSeconds;
		public final ModConfigSpec.ConfigValue<Integer> kernelRepositoryFlushSeconds;
		public final ModConfigSpec.ConfigValue<Boolean> enableTeleportEssentials;
		public final ModConfigSpec.ConfigValue<Boolean> enableHomes;
		public final ModConfigSpec.ConfigValue<Boolean> enableTeleportRequests;
		public final ModConfigSpec.ConfigValue<Boolean> enableBack;
		public final ModConfigSpec.ConfigValue<Boolean> enableSpawnCommands;
		public final ModConfigSpec.ConfigValue<Boolean> enableServerWarps;
		public final ModConfigSpec.ConfigValue<Boolean> enablePlayerWarps;
		public final ModConfigSpec.ConfigValue<Boolean> enableRandomTeleport;
		public final ModConfigSpec.ConfigValue<Boolean> enableDirectTeleport;
		public final ModConfigSpec.ConfigValue<Boolean> ownVanillaTeleportRoot;
		public final ModConfigSpec.ConfigValue<String> defaultHomeName;
		public final ModConfigSpec.ConfigValue<Integer> defaultHomeLimit;
		public final ModConfigSpec.ConfigValue<Integer> defaultPlayerWarpLimit;
		public final ModConfigSpec.ConfigValue<Integer> defaultHomePerDimensionLimit;
		public final ModConfigSpec.ConfigValue<Integer> teleportCooldownSeconds;
		public final ModConfigSpec.ConfigValue<Integer> teleportWarmupSeconds;
		public final ModConfigSpec.ConfigValue<Boolean> teleportCancelOnMovement;
		public final ModConfigSpec.ConfigValue<Boolean> teleportCancelOnDamage;
		public final ModConfigSpec.ConfigValue<Boolean> teleportAllowInCombat;
		public final ModConfigSpec.ConfigValue<Boolean> teleportAllowNetherRoof;
		public final ModConfigSpec.ConfigValue<Boolean> teleportAllowHazards;
		public final ModConfigSpec.ConfigValue<Integer> teleportSafeSearchRadius;
		public final ModConfigSpec.ConfigValue<Integer> teleportMaximumSafeChecks;
		public final ModConfigSpec.ConfigValue<Integer> teleportMaximumChunks;
		public final ModConfigSpec.ConfigValue<Integer> teleportInvulnerabilityTicks;
		public final ModConfigSpec.ConfigValue<Integer> teleportRequestExpirySeconds;
		public final ModConfigSpec.ConfigValue<Integer> teleportMaximumPendingRequests;
		public final ModConfigSpec.ConfigValue<Integer> playerWarpTransferExpirySeconds;
		public final ModConfigSpec.ConfigValue<Integer> randomTeleportMinimumRadius;
		public final ModConfigSpec.ConfigValue<Integer> randomTeleportMaximumRadius;
		public final ModConfigSpec.ConfigValue<Integer> randomTeleportMaximumAttempts;
		public final ModConfigSpec.ConfigValue<String> randomTeleportAllowedDimensions;
		public final ModConfigSpec.ConfigValue<String> teleportOwnershipMode;
		public final ModConfigSpec.ConfigValue<Double> teleportCost;
		public final ModConfigSpec.ConfigValue<String> disabledTeleportActions;
		public final ModConfigSpec.ConfigValue<Boolean> enableSocialEssentials;
		public final ModConfigSpec.ConfigValue<Boolean> enableSocialSpy;
		public final ModConfigSpec.ConfigValue<Boolean> enableMail;
		public final ModConfigSpec.ConfigValue<Boolean> enableConnectionMessages;
		public final ModConfigSpec.ConfigValue<Boolean> enableReminders;
		public final ModConfigSpec.ConfigValue<Boolean> enableCustomText;
		public final ModConfigSpec.ConfigValue<Boolean> enableModerationEssentials;
		public final ModConfigSpec.ConfigValue<Boolean> enableCommandSpy;
		public final ModConfigSpec.ConfigValue<Boolean> enableJails;
		public final ModConfigSpec.ConfigValue<Boolean> enableAdditionalWorkstations;
		public final ModConfigSpec.ConfigValue<Boolean> enableKits;
		public final ModConfigSpec.ConfigValue<Boolean> enableInventoryUtilities;
		public final ModConfigSpec.ConfigValue<Boolean> enablePlayerUtilities;
		public final ModConfigSpec.ConfigValue<Boolean> enableGamemodeShortcuts;
		public final ModConfigSpec.ConfigValue<Boolean> enableItemShortcut;
		public final ModConfigSpec.ConfigValue<Boolean> enableEconomy;
		public final ModConfigSpec.ConfigValue<Boolean> enableEconomySigns;
		public final ModConfigSpec.ConfigValue<String> socialSpyFormat;
		public final ModConfigSpec.ConfigValue<Integer> socialSpyRecentLimit;
		public final ModConfigSpec.ConfigValue<Integer> socialSpyEventsPerSecond;
		public final ModConfigSpec.ConfigValue<Integer> privateMessageMaximumLength;
		public final ModConfigSpec.ConfigValue<Integer> mailMaximumLength;
		public final ModConfigSpec.ConfigValue<Integer> mailRetentionDays;
		public final ModConfigSpec.ConfigValue<String> defaultJoinMessage;
		public final ModConfigSpec.ConfigValue<String> defaultLeaveMessage;
		public final ModConfigSpec.ConfigValue<String> optionalClientReminder;

		// Moderation, observation, and optional file logging
		public final ModConfigSpec.ConfigValue<Integer> moderationMaximumReasonLength;
		public final ModConfigSpec.ConfigValue<Integer> moderationMaximumMassTargets;
		public final ModConfigSpec.ConfigValue<String> moderationDefaultKickReason;
		public final ModConfigSpec.ConfigValue<String> moderationAddressProvider;
		public final ModConfigSpec.ConfigValue<Boolean> moderationAllowLiteralPlayerAddresses;
		public final ModConfigSpec.ConfigValue<Boolean> moderationAllowLiteralConsoleAddresses;
		public final ModConfigSpec.ConfigValue<Integer> moderationSharedAddressHardCap;
		public final ModConfigSpec.ConfigValue<Integer> moderationConfirmationSeconds;
		public final ModConfigSpec.ConfigValue<Boolean> moderationFailOnSharedProxy;
		public final ModConfigSpec.ConfigValue<Integer> commandSpyRecentLimit;
		public final ModConfigSpec.ConfigValue<Integer> commandSpySelectedLimit;
		public final ModConfigSpec.ConfigValue<Integer> commandSpyEventsPerSecond;
		public final ModConfigSpec.ConfigValue<Boolean> fileLoggingEnabled;
		public final ModConfigSpec.ConfigValue<Boolean> fileLoggingConnectionEvents;
		public final ModConfigSpec.ConfigValue<Boolean> fileLoggingTextMirror;
		public final ModConfigSpec.ConfigValue<Integer> fileLoggingQueueCapacity;
		public final ModConfigSpec.ConfigValue<Integer> fileLoggingBatchRecords;
		public final ModConfigSpec.ConfigValue<Integer> fileLoggingFlushIntervalMillis;
		public final ModConfigSpec.ConfigValue<Integer> fileLoggingMaximumRecordBytes;
		public final ModConfigSpec.ConfigValue<Integer> fileLoggingMaximumFileMiB;
		public final ModConfigSpec.ConfigValue<Integer> fileLoggingMaximumFileAgeHours;
		public final ModConfigSpec.ConfigValue<Integer> fileLoggingRetentionDays;
		public final ModConfigSpec.ConfigValue<Integer> fileLoggingMaximumArchives;
		public final ModConfigSpec.ConfigValue<Integer> fileLoggingMaximumTotalMiB;
		public final ModConfigSpec.ConfigValue<Integer> fileLoggingShutdownTimeoutSeconds;

		// Phase 7 inventory and player utilities
		public final ModConfigSpec.ConfigValue<Boolean> enableCartographyTableCommand;
		public final ModConfigSpec.ConfigValue<Boolean> enableGrindstoneCommand;
		public final ModConfigSpec.ConfigValue<Boolean> enableLoomCommand;
		public final ModConfigSpec.ConfigValue<Boolean> enableSmithingTableCommand;
		public final ModConfigSpec.ConfigValue<Boolean> enableStonecutterCommand;
		public final ModConfigSpec.ConfigValue<Integer> utilityCooldownSeconds;
		public final ModConfigSpec.ConfigValue<Integer> itemGiveMaximumAmount;
		public final ModConfigSpec.ConfigValue<Integer> maximumKits;
		public final ModConfigSpec.ConfigValue<Integer> maximumKitItems;
		public final ModConfigSpec.ConfigValue<Integer> maximumKitUsesPerPlayer;
		public final ModConfigSpec.ConfigValue<Boolean> kitDropOverflow;
		public final ModConfigSpec.ConfigValue<Boolean> kitRequirePerKitPermission;
		public final ModConfigSpec.ConfigValue<Boolean> enableSuicideCommand;
		public final ModConfigSpec.ConfigValue<Double> maximumFlySpeed;
		public final ModConfigSpec.ConfigValue<Double> maximumWalkSpeed;

		// Phase 8 economy
		public final ModConfigSpec.ConfigValue<String> economyProviderMode;
		public final ModConfigSpec.ConfigValue<String> economyExternalProvider;
		public final ModConfigSpec.ConfigValue<String> economyCurrency;
		public final ModConfigSpec.ConfigValue<String> economyCurrencySymbol;
		public final ModConfigSpec.ConfigValue<Integer> economyMinorUnits;
		public final ModConfigSpec.ConfigValue<Long> economyDefaultBalance;
		public final ModConfigSpec.ConfigValue<Long> economyMinimumBalance;
		public final ModConfigSpec.ConfigValue<Long> economyMaximumBalance;
		public final ModConfigSpec.ConfigValue<Long> economyMaximumTransaction;
		public final ModConfigSpec.ConfigValue<Integer> economyMaximumAccounts;
		public final ModConfigSpec.ConfigValue<Integer> economyMaximumLedgerEntries;
		public final ModConfigSpec.ConfigValue<Integer> economyMaximumPendingCosts;
		public final ModConfigSpec.ConfigValue<Integer> economyMaximumWorthEntries;
		public final ModConfigSpec.ConfigValue<Boolean> economyAllowOfflinePayments;
		public final ModConfigSpec.ConfigValue<Boolean> economyAllowSelfPayments;
		public final ModConfigSpec.ConfigValue<Long> economyConfirmationThreshold;
		public final ModConfigSpec.ConfigValue<Integer> economyBalanceTopPageSize;
		public final ModConfigSpec.ConfigValue<Integer> economyHistoryPageSize;
		public final ModConfigSpec.ConfigValue<Integer> economyMaximumImportAccounts;
		public final ModConfigSpec.ConfigValue<Integer> economyMaximumSigns;
		public final ModConfigSpec.ConfigValue<Integer> economySignClaimSeconds;
		public final ModConfigSpec.ConfigValue<Integer> economySignMaximumQuantity;
		public final ModConfigSpec.ConfigValue<Long> economySignMaximumValue;
		public final ModConfigSpec.ConfigValue<String> economyEnabledSignTypes;
		public final ModConfigSpec.ConfigValue<String> economyCommandCosts;

		// Warn System
		public final ModConfigSpec.ConfigValue<Boolean> enableWarnSystem;
		public final ModConfigSpec.ConfigValue<String> warnAddedMsg;
		public final ModConfigSpec.ConfigValue<String> warnRemovedMsg;
		public final ModConfigSpec.ConfigValue<String> warnListHeaderFormat;
		public final ModConfigSpec.ConfigValue<String> warnEntryFormat;
		public final ModConfigSpec.ConfigValue<String> warnExpiredTag;
		public final ModConfigSpec.ConfigValue<String> warnNoWarnsMsg;
		public final ModConfigSpec.ConfigValue<String> warnNotifyPlayerMsg;
		public final ModConfigSpec.ConfigValue<Boolean> warnPlaySound;

		// Freeze System
		public final ModConfigSpec.ConfigValue<Boolean> enableFreezeSystem;
		public final ModConfigSpec.ConfigValue<String> freezeMessageToPlayer;
		public final ModConfigSpec.ConfigValue<String> freezeReasonFormat;
		public final ModConfigSpec.ConfigValue<String> freezeReminderFormat;
		public final ModConfigSpec.ConfigValue<Integer> freezeReminderIntervalSeconds;
		public final ModConfigSpec.ConfigValue<String> freezeAdminNotifyFormat;
		public final ModConfigSpec.ConfigValue<String> unfreezeMessageToPlayer;
		public final ModConfigSpec.ConfigValue<String> unfreezeAdminNotifyFormat;
		public final ModConfigSpec.ConfigValue<String> freezeCommandBlockedMsg;
		public final ModConfigSpec.ConfigValue<String> freezeActionBlockedMsg;
		public final ModConfigSpec.ConfigValue<Boolean> freezePlaySound;
		public final ModConfigSpec.ConfigValue<Boolean> freezeAllowChat;

		// Message Format Options
		public final ModConfigSpec.ConfigValue<String> msgSentFormat;
		public final ModConfigSpec.ConfigValue<String> msgReceivedFormat;
		public final ModConfigSpec.ConfigValue<String> replyHeaderFormat;
		public final ModConfigSpec.ConfigValue<String> replyBodyFormat;
		public final ModConfigSpec.ConfigValue<String> helpOpRequestFormat;
		public final ModConfigSpec.ConfigValue<String> helpOpReplyFormat;
		public final ModConfigSpec.ConfigValue<String> adminChatFormat;
		public final ModConfigSpec.ConfigValue<String> announcementConfirmFormat;

		// Sound Options
		public final ModConfigSpec.ConfigValue<Boolean> enableMsgSound;
		public final ModConfigSpec.ConfigValue<Boolean> enableReplySound;
		public final ModConfigSpec.ConfigValue<Boolean> enableHelpOpSound;
		public final ModConfigSpec.ConfigValue<Boolean> enableAdminChatSound;

		// Toggle Messages
		public final ModConfigSpec.ConfigValue<String> adminChatEnabledMsg;
		public final ModConfigSpec.ConfigValue<String> adminChatDisabledMsg;
		public final ModConfigSpec.ConfigValue<String> helpOpSentMsg;
		public final ModConfigSpec.ConfigValue<String> helpOpReplySentMsg;
		public final ModConfigSpec.ConfigValue<String> noReplyTargetMsg;
		public final ModConfigSpec.ConfigValue<String> playerOfflineMsg;
		public final ModConfigSpec.ConfigValue<String> messageNotFoundMsg;
		public final ModConfigSpec.ConfigValue<String> noPermissionMsg;

		// Hover Text Options
		public final ModConfigSpec.ConfigValue<String> clickToReplyHover;
		public final ModConfigSpec.ConfigValue<String> clickToMessageHover;
		public final ModConfigSpec.ConfigValue<String> helpOpReplyHover;

		// Announcement Format Options
		public final ModConfigSpec.ConfigValue<String> announcementListHeaderText;
		public final ModConfigSpec.ConfigValue<String> announcementListHeaderCmd;
		public final ModConfigSpec.ConfigValue<String> toggleListHeader;
		public final ModConfigSpec.ConfigValue<String> toggleOnText;
		public final ModConfigSpec.ConfigValue<String> toggleOffText;

		public ConfigBuilder(ModConfigSpec.Builder builder) {
			builder.push("ServerEssentialsForgeModConfig");

			// ─────────────────────────────────────────────────────────────────────────────
			//  MODULES — master on/off switch for every feature & command in SEF.
			//  Set a value to false to fully disable that part of the mod: its command is not
			//  registered and its behaviour is skipped. Everything defaults to ON, except a
			//  few that need extra setup (discord_integration needs a bot token; nickname_command
			//  conflicts with FTB Essentials' /nick; custom_tab_header_footer is opt-in).
			//  Fine-grained sub-options (message formats, sounds, markdown, timestamp,
			//  integrations, scan radius, etc.) live in each feature's own section further down.
			// ─────────────────────────────────────────────────────────────────────────────
			builder.push("modules");
			// Chat & communication
			enableChatFormatting = builder.comment("  Master toggle for SEF's chat formatting (prefix/suffix/color/timestamp). When false, chat uses the vanilla format. Note: click_to_respond needs this ON to attach clickable replies.").define("chat_formatting", true);
			enableMessagingSystem = builder.comment("  Private messaging system (/msg, /r, /tell, /w)").define("msg_system", true);
			enableChatReplies = builder.comment("  Click-to-reply system (/ans, clickable chat messages, chat logging). Requires chat_formatting = true (SEF must own the chat line to make it clickable).").define("click_to_respond", true);
			enableHelpOp = builder.comment("  /helpop command for players to request operator help").define("helpop", true);
			enableAdminChat = builder.comment("  Admin chat system (/ac, /chat admin)").define("admin_chat", true);
			enableOpBulletin = builder.comment("  Operator bulletin system (/opbulletin)").define("op_bulletin", true);
			enableFilterSystem = builder.comment("  Word filter system (/sef filter ...)").define("filter_system", true);
			enableColorsCommand = builder.comment("  /colors command").define("colors_command", true);
			enableTabListIntegration = builder.comment("  Custom tab list information").define("tab_list", true);
			enableCustomTabHeaderFooter = builder.comment("  Server-side custom tab header/footer rendering (opt-in; configure formats in the tab section)").define("custom_tab_header_footer", false);
			enableWhoisCommand = builder.comment("  Integrated /whois command (ignored if autoIntegratedNicknames is on)").define("whois_command", true);
			enableChatNicknameCommand = builder.comment("  Integrated /nick command (off by default; conflicts with FTB Essentials' /nick. Ignored if autoIntegratedNicknames is on)").define("nickname_command", false);
			enableDiscordBotIntegration = builder.comment("  Discord bot integration (off by default; requires a bot token)").define("discord_integration", false);
			// Vanish
			enableVanishSystem = builder.comment("  Vanish system (/vanish, /trace + the vanish hiding mixins). Detailed vanish behaviour lives in sef-vanish-server.toml.").define("vanish_system", true);
			// Moderation
			enableMuteSystem = builder.comment("  Persistent mute system (/mute, /unmute, /mutelist)").define("mute_system", true);
			enableWarnSystem = builder.comment("  Warning system (/warn, /warns)").define("warn_system", true);
			enableFreezeSystem = builder.comment("  Freeze system (/freeze, /unfreeze)").define("freeze_system", true);
			enableCheckAlts = builder.comment("  Alt-account checker (/checkalts)").define("check_alts", true);
			enableBannedItems = builder.comment("  Banned items system (/banned ...)").define("banned_items", true);
			enableDisableBuilding = builder.comment("  Building restriction system (/disablebuilding, /db)").define("disable_building", true);
			enableInvLock = builder.comment("  Inventory lock (/invlock)").define("inv_lock", true);
			enableSudo = builder.comment("  /sudo command").define("sudo", false);
			// Utility
			enableInvSee = builder.comment("  Inventory viewer (/invsee)").define("invsee", true);
			enableClearChat = builder.comment("  Clear chat (/cc, /clearchat)").define("clear_chat", true);
			enableCountdown = builder.comment("  Countdown broadcaster (/countdown)").define("countdown", true);
			enableMotdSystem = builder.comment("  MOTD system (/sef motd ...)").define("motd_system", true);
			enableAnnouncements = builder.comment("  Scheduled announcements + announcement commands").define("announcements", true);
			enableCraftingTableCommand = builder.comment("  Virtual crafting table (/craft, /c)").define("crafting_table", true);
			enableAnvilCommand = builder.comment("  Virtual anvil (/anvil, /av)").define("anvil", true);
			enableEnchantingTableCommand = builder.comment("  Virtual enchanting table (/enchantingtable, /et)").define("enchanting_table", true);
			enableSuperEnchantingTableCommand = builder.comment("  Super enchanting table (/superenchantingtable, /set)").define("super_enchanting_table", true);
			enableRepairCommand = builder.comment("  Held item repair command (/repair)").define("repair", true);
			enableTeleportEssentials = builder.comment("  Teleport essentials platform").define("teleport_essentials", true);
			enableHomes = builder.comment("  Home commands").define("homes", true);
			enableTeleportRequests = builder.comment("  Teleport request commands").define("teleport_requests", true);
			enableBack = builder.comment("  Back command and location history").define("back", true);
			enableSpawnCommands = builder.comment("  Spawn commands").define("spawn", true);
			enableServerWarps = builder.comment("  Server warp commands").define("server_warps", true);
			enablePlayerWarps = builder.comment("  Player hosted warp commands").define("player_warps", true);
			enableRandomTeleport = builder.comment("  Random teleport commands").define("random_teleport", true);
			enableDirectTeleport = builder.comment("  Staff direct teleport commands").define("direct_teleport", true);
			enableSocialEssentials = builder.comment("  Social, identity, mail, and connection message platform").define("social_essentials", true);
			enableSocialSpy = builder.comment("  Permission controlled private message observation").define("social_spy", true);
			enableMail = builder.comment("  Offline UUID addressed mail").define("mail", true);
			enableConnectionMessages = builder.comment("  Custom real join and leave messages").define("connection_messages", true);
			enableReminders = builder.comment("  Welcome, onboarding, and reminder delivery").define("reminders", true);
			enableCustomText = builder.comment("  Rules, info, and custom text pages").define("custom_text", true);
			enableModerationEssentials = builder.comment("  Ban, kick, IP moderation, and moderation history").define("moderation_essentials", true);
			enableCommandSpy = builder.comment("  Permission controlled command observation").define("command_spy", true);
			enableJails = builder.comment("  Persistent jail definitions and sentences").define("jails", true);
			enableAdditionalWorkstations = builder.comment("  Cartography, grindstone, loom, smithing, stonecutter, and workbench commands").define("additional_workstations", true);
			enableKits = builder.comment("  Versioned item kit repository and commands").define("kits", true);
			enableInventoryUtilities = builder.comment("  Inventory, ender chest, disposal, and safe item utility commands").define("inventory_utilities", true);
			enablePlayerUtilities = builder.comment("  Player state and position utility commands").define("player_utilities", true);
			enableGamemodeShortcuts = builder.comment("  Bounded gamemode shortcut family").define("gamemode_shortcuts", true);
			enableItemShortcut = builder.comment("  Bounded self only item shortcut").define("item_shortcut", true);
			enableEconomy = builder.comment("  Native or adapter backed economy, worth, sell, and command costs").define("economy", true);
			enableEconomySigns = builder.comment("  Server authoritative vanilla economy signs").define("economy_signs", true);
			builder.pop(); // modules

			playerNameFormat = builder
					.comment("  Controls the chat message format",
							 "    $prefix is replaced by the user's prefix or nothing if the user has no prefix",
							 "    $suffix is replaced by the user's suffix or nothing if the user has no suffix",
							 "    $name is replaced by the user's name, or nickname if they have one")
					.define("playerNameFormat", "$prefix$name$suffix");
			chatMessageFormat = builder
					.comment("  Controls the chat message format",
							 "    $time is replaced by the timestamp field or nothing if disabled", 
							 "    $name is replaced by the user's name, or nickname if they have one",
							 "    colors can be uses in the formatting string. for a global message color see next section",
							 "    $msg is replaced by the username's message (if you use it more then once it WILL break this mod)")
					.define("chatMessageFormat", "$time | $name: $msg");
			chatMessageColor = builder
					.comment("  Sets the global color of the chat messages",
							"   Choose one of the following:",
							"    AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE, BLACK, GOLD, GRAY, BLUE, GREEN,",
							"    DARK_GRAY, DARK_AQUA, DARK_RED, DARK_PURPLE, DARK_GREEN, DARK_BLUE")
					.define("chatMessageColor", "WHITE");
			timestampFormat = builder
					.comment("  Timestamp format following the java SimpleDateFormat",
							 "    Read more here: https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html")
					.define("timestampFormat", "HH:mm");
			discordBotToken = builder.comment("  Discord bot token for discord integration").define("discordBotToken", "");
			metaJoinSeparator = builder.comment("  Separator inserted between multiple prefixes/suffixes when combined").define("metaJoinSeparator", " ");
			enableTimestamp = builder.comment("  Enables or disables the filling in of timestamps").define("enableTimestamp", true);
			enableFtbEssentials = builder.comment("  Enables or disables FTB essentials nickname integration").define("useFtbEssentials", true);
			enableLuckPerms = builder.comment("  Enables or disables LuckPerms integration").define("useLuckPerms", true);
			enableMarkdown = builder.comment("  Enables or disables markdown styling").define("markdownEnabled", true);
			enableMetadataInTabList = builder.comment("  Enables or disables prefixes&suffixes in the tab list").define("tabListMetadata", true);
			enableNicknamesInTabList = builder.comment("  Enables or disables nicknames in the tab list").define("tabListNicknames", true);
			tabHeaderFormat = builder.comment("  Tab header format (placeholders: {server_ip}, {online}, {max})").define("tabHeaderFormat", "");
			tabFooterFormat = builder.comment("  Tab footer format (placeholders: {server_ip}, {online}, {max})").define("tabFooterFormat", "");
			autoEnableChatNicknameCommand = builder.comment("  When true, enables the integrated nickname-related commands if FTB essentials is not present").define("autoIntegratedNicknames", true);
			maximumNicknameLength = builder.comment("  Maximum allowed nickname length (for integrated nickname commands)").defineInRange("maximumNicknameLength", 50, 1, 500);
			minimumNicknameLength = builder.comment("  Minimum allowed nickname length (for integrated nickname commands)").defineInRange("minimumNicknameLength", 1, 1, 500);
			nicknameUniqueOnline = builder.comment("  Require nicknames to be unique among online players").define("nicknameUniqueOnline", true);
			nicknameUniqueKnownProfiles = builder.comment("  Require nicknames to be unique among all profiles known to the integrated nickname store").define("nicknameUniqueKnownProfiles", true);
			nicknameAllowDuplicateWithUsernameHover = builder.comment("  Allow duplicate display names. Vanilla hover text shows the authenticated username, and identity lookup remains fail closed when a nickname is ambiguous.").define("nicknameAllowDuplicateWithUsernameHover", false);
			maxPrefixesDisplayed = builder.comment("  Maximum number of prefixes to show (weighted order)").defineInRange("maxPrefixesDisplayed", 1, 0, 50);
			maxSuffixesDisplayed = builder.comment("  Maximum number of suffixes to show (weighted order)").defineInRange("maxSuffixesDisplayed", 1, 0, 50);
			tabUpdateIntervalTicks = builder.comment("  Minimum ticks between tab header and footer refreshes").defineInRange("tabUpdateIntervalTicks", 20, 1, 1200);
			securityAuditRetentionDays = builder.comment("  Days to retain structured SEF security audit files").defineInRange("securityAuditRetentionDays", 30, 1, 3650);
			securityAuditMaximumFileMiB = builder.comment("  Maximum size of the active structured security audit file before rotation").defineInRange("securityAuditMaximumFileMiB", 16, 1, 1024);

			builder.comment("Command kernel hard limits").push("commandKernel");
			kernelMaximumAliases = builder.comment("  Maximum operator alias definitions").defineInRange("maximumAliases", 256, 1, 1024);
			kernelMaximumBundleSteps = builder.comment("  Maximum steps in one command bundle").defineInRange("maximumBundleSteps", 64, 1, 256);
			kernelMaximumBundleDepth = builder.comment("  Maximum nested command bundle depth").defineInRange("maximumBundleDepth", 4, 1, 8);
			kernelMaximumTargets = builder.comment("  Maximum targets resolved by one bundle").defineInRange("maximumTargets", 100, 1, 1000);
			kernelMaximumTargetSteps = builder.comment("  Maximum expanded target steps in one bundle").defineInRange("maximumTargetSteps", 2000, 1, 100000);
			kernelLocationHistoryEntries = builder.comment("  Maximum stored location history entries per player").defineInRange("locationHistoryEntries", 20, 1, 100);
			kernelPersistentCooldownMinimumSeconds = builder.comment("  Persist cooldowns with at least this many seconds remaining").defineInRange("persistentCooldownMinimumSeconds", 60, 0, 86400);
			kernelRepositoryFlushSeconds = builder.comment("  Maximum seconds dirty kernel repositories remain only in memory").defineInRange("repositoryFlushSeconds", 30, 1, 600);
			builder.pop();

			builder.comment("Moderation and command observation",
					"  Address operations use only the configured authoritative provider.",
					"  Raw addresses never enter normal command feedback or file logs.").push("moderation");
			moderationMaximumReasonLength = builder.comment("  Maximum stored moderation reason length").defineInRange("maximumReasonLength", 512, 1, 2048);
			moderationMaximumMassTargets = builder.comment("  Maximum sessions affected by one bounded moderation action").defineInRange("maximumMassTargets", 100, 1, 1000);
			moderationDefaultKickReason = builder.comment("  Disconnect reason used when no reason is supplied").define("defaultKickReason", "Removed by an administrator.");
			moderationAddressProvider = builder.comment("  Address provider. direct, trusted_proxy, external, or disabled. Restart required.").define("addressProvider", "direct");
			moderationAllowLiteralPlayerAddresses = builder.comment("  Permit players with the literal address permission to enter a literal address").define("allowLiteralPlayerAddresses", false);
			moderationAllowLiteralConsoleAddresses = builder.comment("  Permit console to enter a literal address").define("allowLiteralConsoleAddresses", true);
			moderationSharedAddressHardCap = builder.comment("  Maximum sessions resolved from one address").defineInRange("sharedAddressHardCap", 10, 1, 100);
			moderationConfirmationSeconds = builder.comment("  Lifetime of destructive mass action confirmation tokens").defineInRange("confirmationSeconds", 30, 10, 300);
			moderationFailOnSharedProxy = builder.comment("  Disable shared address actions when a likely unconfigured proxy is detected").define("failOnSharedProxy", true);
			commandSpyRecentLimit = builder.comment("  Maximum redacted command events retained in memory").defineInRange("commandSpyRecentLimit", 4096, 32, 65536);
			commandSpySelectedLimit = builder.comment("  Maximum selected UUID filters per observer").defineInRange("commandSpySelectedLimit", 32, 1, 256);
			commandSpyEventsPerSecond = builder.comment("  Maximum command observations delivered to one viewer per second").defineInRange("commandSpyEventsPerSecond", 100, 1, 1000);
			builder.pop();

			builder.comment("Optional structured file logging",
					"  Disabled by default. Output is fixed under logs/sef.",
					"  Queue, record, rotation, retention, and shutdown values are hard bounded.").push("fileLogging");
			fileLoggingEnabled = builder.comment("  Start the optional command log writer").define("enabled", false);
			fileLoggingConnectionEvents = builder.comment(
					"  Capture redacted player connection events when file logging is active")
					.define("connectionEvents", false);
			fileLoggingTextMirror = builder.comment("  Write a stripped human readable mirror beside JSON Lines").define("textMirror", false);
			fileLoggingQueueCapacity = builder.comment("  Bounded event queue capacity").defineInRange("queueCapacity", 8192, 128, 65536);
			fileLoggingBatchRecords = builder.comment("  Maximum records written per batch").defineInRange("batchRecords", 128, 1, 1024);
			fileLoggingFlushIntervalMillis = builder.comment("  Maximum delay before flushing a nonempty batch").defineInRange("flushIntervalMillis", 1000, 50, 60000);
			fileLoggingMaximumRecordBytes = builder.comment("  Maximum encoded bytes in one record").defineInRange("maximumRecordBytes", 16384, 1024, 1048576);
			fileLoggingMaximumFileMiB = builder.comment("  Maximum active stream size before rotation").defineInRange("maximumFileMiB", 64, 1, 1024);
			fileLoggingMaximumFileAgeHours = builder.comment("  Maximum active stream age before rotation").defineInRange("maximumFileAgeHours", 24, 1, 720);
			fileLoggingRetentionDays = builder.comment("  Maximum archive age").defineInRange("retentionDays", 30, 1, 3650);
			fileLoggingMaximumArchives = builder.comment("  Maximum retained command archives").defineInRange("maximumArchives", 100, 1, 10000);
			fileLoggingMaximumTotalMiB = builder.comment("  Maximum retained command archive bytes").defineInRange("maximumTotalMiB", 1024, 1, 1048576);
			fileLoggingShutdownTimeoutSeconds = builder.comment("  Maximum bounded shutdown drain time").defineInRange("shutdownTimeoutSeconds", 10, 1, 60);
			builder.pop();

			builder.comment("Inventory, kit, workstation, and player utilities").push("phaseSevenUtilities");
			enableCartographyTableCommand = builder.comment("  Virtual cartography table command").define("cartographyTable", true);
			enableGrindstoneCommand = builder.comment("  Virtual grindstone command").define("grindstone", true);
			enableLoomCommand = builder.comment("  Virtual loom command").define("loom", true);
			enableSmithingTableCommand = builder.comment("  Virtual smithing table command").define("smithingTable", true);
			enableStonecutterCommand = builder.comment("  Virtual stonecutter command").define("stonecutter", true);
			utilityCooldownSeconds = builder.comment("  Shared cooldown for ordinary player utility mutations").defineInRange("cooldownSeconds", 0, 0, 31536000);
			itemGiveMaximumAmount = builder.comment("  Maximum amount accepted by the self only item shortcut").defineInRange("itemGiveMaximumAmount", 64, 1, 6400);
			maximumKits = builder.comment("  Maximum stored kit definitions").defineInRange("maximumKits", 128, 1, 1024);
			maximumKitItems = builder.comment("  Maximum item stacks in one kit").defineInRange("maximumKitItems", 256, 1, 1024);
			maximumKitUsesPerPlayer = builder.comment("  Maximum retained kit use records per player").defineInRange("maximumKitUsesPerPlayer", 256, 1, 1024);
			kitDropOverflow = builder.comment("  Drop kit overflow into the world. Disabled keeps grants atomic.").define("kitDropOverflow", false);
			kitRequirePerKitPermission = builder.comment("  Require each kit permission through LuckPerms when available").define("requirePerKitPermission", false);
			enableSuicideCommand = builder.comment("  Enable the self only suicide command").define("suicide", false);
			maximumFlySpeed = builder.comment("  Maximum fly speed multiplier accepted by the speed command").defineInRange("maximumFlySpeed", 10.0D, 0.1D, 10.0D);
			maximumWalkSpeed = builder.comment("  Maximum walk speed multiplier accepted by the speed command").defineInRange("maximumWalkSpeed", 10.0D, 0.1D, 10.0D);
			builder.pop();

			builder.comment("Native economy and provider ownership",
					"  Balances use integer minor units only.",
					"  Provider mode changes require a restart.",
					"  External mode never mirrors provider balances into SEF storage.",
					"  Import once requires an explicit in game preview and confirm operation.").push("economy");
			economyProviderMode = builder.comment("  native, external, disabled, or import_once").define("providerMode", "native");
			economyExternalProvider = builder.comment("  Registered external provider id. Empty selects the highest priority adapter").define("externalProvider", "");
			economyCurrency = builder.comment("  Stable currency identifier").define("currency", "coin");
			economyCurrencySymbol = builder.comment("  Display only currency prefix").define("currencySymbol", "$");
			economyMinorUnits = builder.comment("  Decimal minor units used when parsing and formatting").defineInRange("minorUnits", 2, 0, 8);
			economyDefaultBalance = builder.comment("  Opening account balance in minor units").defineInRange("defaultBalance", 0L, -9_000_000_000_000_000L, 9_000_000_000_000_000L);
			economyMinimumBalance = builder.comment("  Minimum account balance in minor units. Use zero to disallow debt").defineInRange("minimumBalance", 0L, -9_000_000_000_000_000L, 9_000_000_000_000_000L);
			economyMaximumBalance = builder.comment("  Maximum account balance in minor units").defineInRange("maximumBalance", 1_000_000_000_000_000L, 1L, 9_000_000_000_000_000L);
			economyMaximumTransaction = builder.comment("  Maximum value of one transaction in minor units").defineInRange("maximumTransaction", 1_000_000_000_000L, 1L, 9_000_000_000_000_000L);
			economyMaximumAccounts = builder.comment("  Maximum native accounts").defineInRange("maximumAccounts", 100_000, 1, 1_000_000);
			economyMaximumLedgerEntries = builder.comment("  Maximum retained native ledger entries").defineInRange("maximumLedgerEntries", 100_000, 100, 1_000_000);
			economyMaximumPendingCosts = builder.comment("  Maximum crash recoverable pending command costs").defineInRange("maximumPendingCosts", 10_000, 1, 100_000);
			economyMaximumWorthEntries = builder.comment("  Maximum server defined item worth entries").defineInRange("maximumWorthEntries", 10_000, 1, 100_000);
			economyAllowOfflinePayments = builder.comment("  Allow payments to unambiguous known offline identities").define("allowOfflinePayments", true);
			economyAllowSelfPayments = builder.comment("  Allow a player to pay the same account").define("allowSelfPayments", false);
			economyConfirmationThreshold = builder.comment("  Payments at or above this value require confirmation when the player preference is enabled. Zero disables the threshold").defineInRange("confirmationThreshold", 100_000L, 0L, 9_000_000_000_000_000L);
			economyBalanceTopPageSize = builder.comment("  Entries shown per balance top page").defineInRange("balanceTopPageSize", 10, 1, 100);
			economyHistoryPageSize = builder.comment("  Transactions shown per history page").defineInRange("historyPageSize", 10, 1, 100);
			economyMaximumImportAccounts = builder.comment("  Maximum accounts accepted by one import once operation").defineInRange("maximumImportAccounts", 100_000, 1, 1_000_000);
			economyMaximumSigns = builder.comment("  Maximum registered economy sign sides").defineInRange("maximumSigns", 100_000, 1, 1_000_000);
			economySignClaimSeconds = builder.comment("  Seconds a placed sign remains claimable by its placer").defineInRange("signClaimSeconds", 300, 10, 3600);
			economySignMaximumQuantity = builder.comment("  Maximum items in one economy sign transaction").defineInRange("signMaximumQuantity", 2304, 1, 100_000);
			economySignMaximumValue = builder.comment("  Maximum economy sign transaction value in minor units").defineInRange("signMaximumValue", 1_000_000_000L, 1L, 9_000_000_000_000_000L);
			economyEnabledSignTypes = builder.comment("  Comma separated enabled sign types").define("enabledSignTypes", "balance,buy,sell,trade,free,disposal,kit,heal,repair,time,weather,warp");
			economyCommandCosts = builder.comment("  Comma separated action cost mappings. Components are fixed, use, target, distance, and item. Example sef:teleport.spawn=5.00,sef:teleport.spawn@distance=0.01").define("commandCosts", "");
			builder.pop();

			builder.comment("Teleport essentials",
					"  Every destination is validated on the logical server.",
					"  Unloaded chunks are never generated by teleport commands.",
					"  The vanilla teleport root remains owned by vanilla unless explicitly enabled.").push("teleportEssentials");
			ownVanillaTeleportRoot = builder.comment("  Replace the vanilla /tp root with the SEF direct teleport command").define("ownVanillaTeleportRoot", false);
			defaultHomeName = builder.comment("  Home name used when no name is supplied").define("defaultHomeName", "home");
			defaultHomeLimit = builder.comment("  Default home quota before permission or metadata tiers").defineInRange("defaultHomeLimit", 1, 0, 1000);
			defaultHomePerDimensionLimit = builder.comment("  Default home quota in one dimension").defineInRange("defaultHomePerDimensionLimit", 1000, 0, 1000);
			defaultPlayerWarpLimit = builder.comment("  Default player warp quota before permission or metadata tiers").defineInRange("defaultPlayerWarpLimit", 5, 0, 1000);
			teleportCooldownSeconds = builder.comment("  Shared cooldown for user teleport actions").defineInRange("cooldownSeconds", 0, 0, 31536000);
			teleportCost = builder.comment("  Shared economy cost for user teleport actions. A positive value fails closed until an economy provider is installed").defineInRange("cost", 0.0D, 0.0D, 1000000000.0D);
			teleportWarmupSeconds = builder.comment("  Shared warmup for user teleport actions").defineInRange("warmupSeconds", 0, 0, 3600);
			teleportCancelOnMovement = builder.comment("  Cancel an active teleport warmup when the player moves").define("cancelOnMovement", true);
			teleportCancelOnDamage = builder.comment("  Cancel an active teleport warmup when the player takes damage").define("cancelOnDamage", true);
			teleportAllowInCombat = builder.comment("  Allow normal user teleports while in combat").define("allowInCombat", false);
			teleportAllowNetherRoof = builder.comment("  Allow destinations on the Nether roof").define("allowNetherRoof", false);
			teleportAllowHazards = builder.comment("  Allow lava, fire, cactus, magma, and similar destinations").define("allowHazards", false);
			teleportSafeSearchRadius = builder.comment("  Maximum horizontal and vertical safe destination search radius").defineInRange("safeSearchRadius", 4, 0, 32);
			teleportMaximumSafeChecks = builder.comment("  Maximum block positions inspected by one teleport").defineInRange("maximumSafeChecks", 512, 1, 100000);
			teleportMaximumChunks = builder.comment("  Maximum already loaded chunks inspected by one teleport").defineInRange("maximumChunks", 9, 1, 256);
			teleportInvulnerabilityTicks = builder.comment("  Damage immunity ticks after a successful user teleport").defineInRange("invulnerabilityTicks", 20, 0, 200);
			teleportRequestExpirySeconds = builder.comment("  Lifetime of a pending teleport request").defineInRange("requestExpirySeconds", 60, 1, 3600);
			teleportMaximumPendingRequests = builder.comment("  Maximum incoming or outgoing requests per player").defineInRange("maximumPendingRequests", 10, 1, 100);
			playerWarpTransferExpirySeconds = builder.comment("  Lifetime of a two party player warp transfer offer").defineInRange("playerWarpTransferExpirySeconds", 300, 10, 3600);
			randomTeleportMinimumRadius = builder.comment("  Minimum random teleport radius from the configured center").defineInRange("randomTeleportMinimumRadius", 256, 0, 30000000);
			randomTeleportMaximumRadius = builder.comment("  Maximum random teleport radius from the configured center").defineInRange("randomTeleportMaximumRadius", 5000, 1, 30000000);
			randomTeleportMaximumAttempts = builder.comment("  Maximum random candidates inspected per request").defineInRange("randomTeleportMaximumAttempts", 32, 1, 256);
			randomTeleportAllowedDimensions = builder.comment("  Comma separated dimension identifiers allowed for random teleport").define("randomTeleportAllowedDimensions", "minecraft:overworld");
			teleportOwnershipMode = builder.comment("  Ownership mode for homes and server warps. Values are sef, external, coexist, or import_once").define("ownershipMode", "sef");
			disabledTeleportActions = builder.comment("  Comma separated canonical action ids to disable without removing their saved data").define("disabledActions", "");
			builder.pop();

			builder.comment("Social essentials",
					"  Private message content remains outside ordinary audit and persistent observer state.",
					"  Social spy revalidates permission and visibility for every delivered event.").push("socialEssentials");
			socialSpyFormat = builder.comment("  Typed social spy template. Placeholders are {from}, {to}, {message}, {route}, and {timestamp}").define("socialSpyFormat", "&8[&b{from}&8] &7-> &8[&d{to}&8]&7: &f{message}");
			socialSpyRecentLimit = builder.comment("  Maximum already authorized social spy events retained per observer session").defineInRange("socialSpyRecentLimit", 50, 0, 500);
			socialSpyEventsPerSecond = builder.comment("  Maximum social spy events delivered to one observer per second").defineInRange("socialSpyEventsPerSecond", 100, 1, 1000);
			privateMessageMaximumLength = builder.comment("  Maximum private message length").defineInRange("privateMessageMaximumLength", 2048, 1, 16384);
			mailMaximumLength = builder.comment("  Maximum mail body length").defineInRange("mailMaximumLength", 2048, 1, 16384);
			mailRetentionDays = builder.comment("  Mail expiry in days").defineInRange("mailRetentionDays", 30, 1, 3650);
			defaultJoinMessage = builder.comment("  Default real join template").define("defaultJoinMessage", "&e{player} joined the game");
			defaultLeaveMessage = builder.comment("  Default real leave template").define("defaultLeaveMessage", "&e{player} left the game");
			optionalClientReminder = builder.comment("  Command fallback reminder for players without an enhanced client").define("optionalClientReminder", "&6This server supports optional SEF enhanced menus. &fEvery feature still works through commands. &eUse /sef commands &ffor available commands.");
			builder.pop();

			builder.comment("Virtual Workstations",
					"  These commands open workstations without a placed block.",
					"  Cooldowns are tracked per player and per command.",
					"  A cooldown of 0 disables that command's cooldown.").push("virtualWorkstations");
			enableCraftAlias = builder.comment("  Enable the /c alias for /craft").define("enableCraftAlias", true);
			enableAnvilAlias = builder.comment("  Enable the /av alias for /anvil").define("enableAnvilAlias", true);
			enableEnchantingTableAlias = builder.comment("  Enable the /et alias for /enchantingtable").define("enableEnchantingTableAlias", true);
			enableSuperEnchantingTableAlias = builder.comment("  Enable the /set alias for /superenchantingtable").define("enableSuperEnchantingTableAlias", true);
			craftingTableCooldownSeconds = builder.comment("  Cooldown for /craft and /c in seconds").defineInRange("craftingTableCooldownSeconds", 0, 0, 86400);
			anvilCooldownSeconds = builder.comment("  Cooldown for /anvil and /av in seconds").defineInRange("anvilCooldownSeconds", 0, 0, 86400);
			enchantingTableCooldownSeconds = builder.comment("  Cooldown for /enchantingtable and /et in seconds").defineInRange("enchantingTableCooldownSeconds", 0, 0, 86400);
			superEnchantingTableCooldownSeconds = builder.comment("  Cooldown for /superenchantingtable and /set in seconds").defineInRange("superEnchantingTableCooldownSeconds", 0, 0, 86400);
			repairCooldownSeconds = builder.comment("  Cooldown for /repair in seconds").defineInRange("repairCooldownSeconds", 0, 0, 86400);
			superEnchantingMinLevel = builder.comment("  Lowest nonzero level the super enchanting table can apply.").defineInRange("superEnchantingMinLevel", 1, 1, 255);
			superEnchantingMaxLevel = builder.comment("  Highest level the super enchanting table can apply. Minecraft stores levels up to 255.").defineInRange("superEnchantingMaxLevel", 10, 1, 255);
			superEnchantingAllowUnsafe = builder.comment("  Show enchantments that do not normally support the held item and allow incompatible combinations").define("superEnchantingAllowUnsafe", false);
			workstationCooldownMessage = builder.comment("  Message shown during a cooldown. Placeholder: $seconds").define("cooldownMessage", "&cYou must wait &e$seconds &cseconds before using that command again.");
			repairSuccessMessage = builder.comment("  Message shown after repairing the held item. Placeholder: $item").define("repairSuccessMessage", "&aRepaired &e$item&a.");
			repairNotHeldMessage = builder.comment("  Message shown when no item is held").define("repairNotHeldMessage", "&cHold the item you want to repair in your main hand.");
			repairNotNeededMessage = builder.comment("  Message shown when the held item is not damaged").define("repairNotNeededMessage", "&eThat item does not need to be repaired.");
			builder.pop(); // virtual workstations

			announcementIntervalSeconds = builder.comment("  Interval in seconds between announcements").define("announcementIntervalSeconds", 300);
			announcementUseRandomOrder = builder.comment("  When true, announcements are chosen randomly; otherwise in order").define("announcementUseRandomOrder", false);
			enableCommandAnnouncements = builder.comment("  Allow stored command announcements to execute. Disabled by default because commands run with server authority.").define("enableCommandAnnouncements", false);
			commandAnnouncementAllowedCommands = builder.comment("  Comma separated command roots allowed for scheduled command announcements. An empty value denies every command.").define("commandAnnouncementAllowedCommands", "");
			commandAnnouncementDeniedCommands = builder.comment("  Comma separated command roots denied for command announcements even when the allowlist contains them or a wildcard.").define("commandAnnouncementDeniedCommands", "commandannouncement,sudo,execute,op,deop,stop,reload,ban,ban-ip,pardon,pardon-ip,whitelist,luckperms,lp,sef");
			commandAnnouncementMaximumCommandLength = builder.comment("  Maximum command length accepted for command announcements.").defineInRange("commandAnnouncementMaximumCommandLength", 512, 1, 8192);
			commandAnnouncementAllowLeadingSlash = builder.comment("  Allow a stored command announcement to begin with a slash").define("commandAnnouncementAllowLeadingSlash", false);
			commandAnnouncementAllowSelectors = builder.comment("  Allow entity selectors in stored command announcements").define("commandAnnouncementAllowSelectors", false);
			replySummaryLength = builder.comment("  Maximum length of message summary shown in reply headers (0 = no limit)").defineInRange("replySummaryLength", 50, 0, 500);
			enableBannedBlockScanning = builder.comment("  Enable bounded background scanning for banned blocks. Event driven placement enforcement remains active when disabled.").define("enableBannedBlockScanning", false);
			bannedBlockScanRadius = builder.comment("  Default radius around players to scan for banned blocks (smaller = less TPS impact). Override at runtime with /banned setradius").defineInRange("bannedBlockScanRadius", 6, 1, 20);
			bannedBlockScanInterval = builder.comment("  Default interval in ticks between banned-block sweeps. Override at runtime with /banned setinterval. 20 = 1s, 40 = 2s.").defineInRange("bannedBlockScanInterval", 40, 1, 24000);
			bannedInventoryScanInterval = builder.comment("  Minimum ticks between fallback banned inventory scans").defineInRange("bannedInventoryScanInterval", 20, 1, 24000);
			bannedBlockScanBudget = builder.comment("  Maximum block positions inspected per server tick by the background scanner").defineInRange("bannedBlockScanBudget", 512, 1, 65536);
			bannedItemRemovedMsg = builder.comment(
				"  Message shown to a player when a banned item is confiscated.",
				"  Placeholders: $item, $reason, $by, $remaining.",
				"  Leave empty to use the built-in default."
			).define("bannedItemRemovedMsg", "");
			bannedAnnounceFormat = builder.comment(
				"  Server-wide message format when a banned entry has announce=true and a hit occurs.",
				"  Placeholders: $player, $item, $reason.",
				"  Leave empty to use the built-in default."
			).define("bannedAnnounceFormat", "");
			countdownTitleFormat = builder.comment(
				"  Format for the title shown to players on each countdown beat.",
				"  Placeholders: $message, $time, $colored_time, $color.",
				"  Leave empty to use the built-in default ($message)."
			).define("countdownTitleFormat", "");
			countdownSubtitleFormat = builder.comment(
				"  Format for the subtitle shown to players on each countdown beat.",
				"  Placeholders: $message, $time, $colored_time, $color.",
				"  Leave empty to use the built-in default ($colored_time)."
			).define("countdownSubtitleFormat", "");
			countdownChatFormat = builder.comment(
				"  Chat-line format used when /countdown's chat_too argument is true.",
				"  Placeholders: $message, $time, $colored_time, $color.",
				"  Leave empty to use the built-in default."
			).define("countdownChatFormat", "");
			applyMotdOnStartup = builder.comment("  Automatically apply the configured MOTD when the server starts").define("applyMotdOnStartup", true);

			// FTB Mute Integration
			builder.comment("FTB Essentials Mute Integration",
					"  Requires FTB Essentials to be installed.",
					"  When a player is muted via /mute, their chat messages will be blocked by this mod's chat system.").push("ftbMuteIntegration");
			enableFtbMuteIntegration = builder.comment("  Enable checking FTB Essentials mute status to block muted players from chatting").define("enableFtbMuteIntegration", true);
			mutedPlayerMessage = builder.comment("  Message shown to the muted player when they try to chat").define("mutedPlayerMessage", "&cYou are muted and cannot send messages.");
			sendMutedMessageToOps = builder.comment("  When true, muted messages are relayed to online operators so they can see what the muted player tried to say").define("sendMutedMessageToOps", true);
			mutedMessageOpFormat = builder.comment("  Format for relaying muted messages to operators. Placeholders: $username, $message").define("mutedMessageOpFormat", "&c&lMuted Message &7From $username:&r $message");
			builder.pop(); // ftbMuteIntegration

			// Persistent Mute System
			builder.comment("Persistent Mute System",
					"  /mute and /unmute commands with persistent, tick-based mute tracking.",
					"  Mutes survive player disconnects and server restarts.",
					"  Duration counts in server ticks (stops when server is off).",
					"  Data stored in <world>/serverconfig/sef/mutes.json").push("muteSystem");
			muteNotifyPlayerFormat = builder.comment("  Message sent to the player when muted. Placeholders: $admin, $reason, $duration")
					.define("muteNotifyPlayerFormat", "&c&l⚠ YOU HAVE BEEN MUTED ⚠\\n&7Reason: &f$reason\\n&7Muted by: &e$admin\\n&7Duration: &e$duration");
			muteAdminNotifyFormat = builder.comment("  Notification sent to admins when a player is muted. Placeholders: $player, $admin, $reason, $duration")
					.define("muteAdminNotifyFormat", "&e$admin &7has muted &e$player &7for &e$duration&7. Reason: &f$reason");
			unmuteNotifyPlayerFormat = builder.comment("  Message sent to the player when unmuted. Placeholder: $admin")
					.define("unmuteNotifyPlayerFormat", "&a&lYou have been unmuted by &e$admin&a&l.");
			unmuteAdminNotifyFormat = builder.comment("  Notification sent to admins when a player is unmuted. Placeholders: $player, $admin")
					.define("unmuteAdminNotifyFormat", "&e$admin &7has unmuted &e$player&7.");
			muteConfirmFormat = builder.comment("  Confirmation message to the admin who muted. Placeholders: $player, $admin, $reason, $duration")
					.define("muteConfirmFormat", "&aMuted &e$player &afor &e$duration&a. Reason: &7$reason");
			unmuteConfirmFormat = builder.comment("  Confirmation message to the admin who unmuted. Placeholders: $player, $admin")
					.define("unmuteConfirmFormat", "&aUnmuted &e$player&a.");
			muteAlreadyMutedMsg = builder.comment("  Message when trying to mute an already muted player. Placeholder: $player")
					.define("muteAlreadyMutedMsg", "&c$player is already muted.");
			muteNotMutedMsg = builder.comment("  Message when trying to unmute a player who isn't muted. Placeholder: $player")
					.define("muteNotMutedMsg", "&c$player is not muted.");
			muteListHeaderFormat = builder.comment("  Header for /mutelist. No placeholders.")
					.define("muteListHeaderFormat", "&6━━━━ Currently Muted Players ━━━━");
			muteListEntryFormat = builder.comment("  Format for each muted player entry. Placeholders: $player, $admin, $reason, $remaining, $duration")
					.define("muteListEntryFormat", "&7- &e$player &7by &e$admin &7| Reason: &f$reason &7| Remaining: &e$remaining &7/ &e$duration");
			muteListEmptyMsg = builder.comment("  Message when no one is currently muted")
					.define("muteListEmptyMsg", "&7No players are currently muted.");
			mutedPlayerChatMsg = builder.comment("  Message shown to a muted player when they try to chat (permanent mute). No placeholders.")
					.define("mutedPlayerChatMsg", "&cYou are muted and cannot send messages.");
			mutedPlayerChatMsgWithRemaining = builder.comment("  Message shown to a muted player when they try to chat (timed mute). Placeholder: $remaining")
					.define("mutedPlayerChatMsgWithRemaining", "&cYou are muted and cannot send messages. &7Time remaining: &e$remaining");
			builder.pop(); // muteSystem

			// InvSee System
			builder.comment("InvSee System",
					"  Custom /invsee command with Curios mod support.",
					"  SEF can cooperatively add its route when another mod owns the root.",
					"  Existing Brigadier nodes are never removed through reflection.").push("invSee");
			invSeeDisableFtbInvsee = builder.comment("  Register the SEF route when another mod already owns invsee").define("invSeeDisableFtbInvsee", true);
			invSeeTitle = builder.comment("  Title of the InvSee GUI. Placeholder: $player").define("invSeeTitle", "&e$player's Inventory");
			invSeeArmorLabel = builder.comment("  Name shown on the glass pane separator for armor section").define("invSeeArmorLabel", "&9Armor");
			invSeeOffhandLabel = builder.comment("  Name shown on the glass pane separator for offhand section").define("invSeeOffhandLabel", "&6Offhand");
			invSeeCuriosLabel = builder.comment("  Name shown on the glass pane separator for curios section").define("invSeeCuriosLabel", "&dCurios");
			invSeeMainInvLabel = builder.comment("  Name shown on the glass pane separator for main inventory section").define("invSeeMainInvLabel", "&aInventory");
			invSeeNextPageLabel = builder.comment("  Name shown on the next page arrow item").define("invSeeNextPageLabel", "&eNext Page >>>");
			invSeePrevPageLabel = builder.comment("  Name shown on the previous page arrow item").define("invSeePrevPageLabel", "&e<<< Previous Page");
			invSeeReadOnly = builder.comment("  When true, players cannot move items in the InvSee GUI (view-only mode)").define("invSeeReadOnly", false);
			invSeeAuditModifications = builder.comment("  Write inventory modification metadata to the structured security audit").define("invSeeAuditModifications", true);
			builder.pop(); // invSee

			// Clear Chat System
			builder.comment("Clear Chat System",
					"  /cc and /clearchat commands to clear player chat.").push("clearChat");
			clearChatLineCount = builder.comment("  Number of blank lines to send to clear chat").defineInRange("clearChatLineCount", 100, 1, 500);
			clearChatSuccessMsg = builder.comment("  Message shown to admin when clearing a specific player's chat. Placeholder: $player").define("clearChatSuccessMsg", "&aChat cleared for $player.");
			clearChatAllSuccessMsg = builder.comment("  Message shown to admin when clearing all non-OP chats. Placeholder: $admin").define("clearChatAllSuccessMsg", "&aChat cleared for all non-OP players by $admin.");
			clearChatSelfMsg = builder.comment("  Message shown to the player whose chat was cleared").define("clearChatSelfMsg", "&7Your chat has been cleared by an operator.");
			builder.pop(); // clearChat

			// Sudo System
			builder.comment("Sudo System",
					"  /sudo command to force a player to execute a command.").push("sudo");
			sudoExecutedMsg = builder.comment("  Message shown to the admin. Placeholders: $player, $command, $admin").define("sudoExecutedMsg", "&aForced $player to execute: &7/$command");
			sudoNotifyMsg = builder.comment("  Message shown to the target player. Placeholders: $admin, $command").define("sudoNotifyMsg", "&c$admin forced you to run: &7/$command");
			sudoAllowedCommands = builder.comment("  Comma separated command roots allowed through sudo. An empty value denies every command.").define("allowedCommands", "msg,tell,w,r,me");
			sudoDeniedCommands = builder.comment("  Comma separated command roots denied even when allowedCommands contains them or a wildcard.").define("deniedCommands", "sudo,execute,op,deop,stop,reload,ban,ban-ip,pardon,pardon-ip,whitelist,luckperms,lp,sef");
			sudoNotifyTarget = builder.comment("  Notify the target when an administrator uses sudo.").define("notifyTarget", true);
			sudoMaximumCommandLength = builder.comment("  Maximum command length accepted by sudo.").defineInRange("maximumCommandLength", 512, 1, 8192);
			builder.pop(); // sudo

			// Inventory Lock System
			builder.comment("Inventory Lock System",
					"  /invlock command to lock/unlock a player's inventory.").push("invLock");
			invLockLockedMsg = builder.comment("  Message shown to the player when their inventory is locked. Placeholder: $admin").define("invLockLockedMsg", "&c$admin has locked your inventory.");
			invLockUnlockedMsg = builder.comment("  Message shown to the player when their inventory is unlocked. Placeholder: $admin").define("invLockUnlockedMsg", "&a$admin has unlocked your inventory.");
			invLockAdminLockMsg = builder.comment("  Message shown to admin when locking. Placeholder: $player").define("invLockAdminLockMsg", "&eLocked inventory for $player.");
			invLockAdminUnlockMsg = builder.comment("  Message shown to admin when unlocking. Placeholder: $player").define("invLockAdminUnlockMsg", "&eUnlocked inventory for $player.");
			invLockBlockedMsg = builder.comment("  Message shown when a locked player tries to use their inventory").define("invLockBlockedMsg", "&cYour inventory is locked.");
			builder.pop(); // invLock

			// Disable Building System
			builder.comment("Disable Building System",
					"  /disablebuilding command to toggle building restrictions for a player.").push("disableBuilding");
			dbEnabledMsg = builder.comment("  Message shown to admin when disabling building. Placeholders: $player, $admin").define("dbEnabledMsg", "&cBuilding disabled for $player by $admin.");
			dbDisabledMsg = builder.comment("  Message shown to admin when re-enabling building. Placeholders: $player, $admin").define("dbDisabledMsg", "&aBuilding re-enabled for $player by $admin.");
			dbPlayerNotifyMsg = builder.comment("  Message shown to the player. Placeholders: $status (disabled/enabled), $admin").define("dbPlayerNotifyMsg", "&cYour building privileges have been $status by $admin.");
			dbBlockedMsg = builder.comment("  Message shown when a player with building disabled tries to build").define("dbBlockedMsg", "&cYou are not allowed to build.");
			builder.pop(); // disableBuilding

			// Check Alts System
			builder.comment("Check Alts System",
					"  /checkalts command to check alternate accounts by IP.").push("checkAlts");
			checkAltsHeaderFormat = builder.comment("  Header for alts list. Placeholders: $player, $ip").define("checkAltsHeaderFormat", "&6━━━━ Alts for $player ($ip) ━━━━");
			checkAltsEntryFormat = builder.comment("  Format for each alt entry. Placeholders: $name, $uuid, $lastseen").define("checkAltsEntryFormat", "&7- &e$name &7($uuid) Last seen: $lastseen");
			checkAltsNoAltsMsg = builder.comment("  Message when no alts found. Placeholder: $player").define("checkAltsNoAltsMsg", "&7No alternate accounts found for $player.");
			altTrackingCollectAddresses = builder.comment("  Collect address derived alternate account data. This is privacy sensitive and disabled by default.").define("collectAddresses", false);
			altTrackingRetentionDays = builder.comment("  Days to retain alternate account observations before automatic purge").defineInRange("retentionDays", 30, 1, 3650);
			altTrackingHashAddresses = builder.comment("  Store a one way server local hash instead of raw addresses").define("hashAddresses", true);
			builder.pop(); // checkAlts

			// Warn System
			builder.comment("Warn System",
					"  /warn and /warns commands for player warning management.",
					"  Warns persist to JSON and support expiration durations.").push("warnSystem");
			warnAddedMsg = builder.comment("  Message to admin when warning added. Placeholders: $player, $reason, $admin, $id, $duration").define("warnAddedMsg", "&aWarning #$id added for $player: &7$reason &e(Duration: $duration)");
			warnRemovedMsg = builder.comment("  Message to admin when warning removed. Placeholders: $player, $id").define("warnRemovedMsg", "&eWarning #$id removed for $player.");
			warnListHeaderFormat = builder.comment("  Header for warnings list. Placeholder: $player").define("warnListHeaderFormat", "&6━━━━ Warnings for $player ━━━━");
			warnEntryFormat = builder.comment("  Format for each warning entry. Placeholders: $id, $reason, $admin, $date, $expired").define("warnEntryFormat", "&7#$id &f$reason &7(by $admin, $date)$expired");
			warnExpiredTag = builder.comment("  Text appended for expired warnings").define("warnExpiredTag", " &c(expired)");
			warnNoWarnsMsg = builder.comment("  Message when player has no warnings. Placeholder: $player").define("warnNoWarnsMsg", "&7$player has no warnings.");
			warnNotifyPlayerMsg = builder.comment("  Message shown to the warned player. Placeholders: $admin, $reason").define("warnNotifyPlayerMsg", "&c⚠ You have been warned by $admin: &f$reason");
			warnPlaySound = builder.comment("  Play a sound when a player is warned").define("warnPlaySound", true);
			builder.pop(); // warnSystem

			// Freeze System
			builder.comment("Freeze System",
					"  /freeze command to lock a player in place.",
					"  Frozen players cannot move, look around, jump, mine, break, place, or use commands.",
					"  They CAN still type in chat to respond to the admin.").push("freezeSystem");
			freezeMessageToPlayer = builder.comment("  Message sent to the player when they are frozen. Placeholders: $reason, $admin, $duration")
					.define("freezeMessageToPlayer", "&c&l⚠ YOU HAVE BEEN FROZEN ⚠\n&7Reason: &f$reason\n&7Frozen by: &e$admin\n&7Duration: &e$duration\n&7&oPlease respond to the admin in chat.");
			freezeReasonFormat = builder.comment("  Format of the reason displayed. Placeholder: $reason")
					.define("freezeReasonFormat", "&c&lFROZEN &7- &f$reason");
			freezeReminderFormat = builder.comment("  Periodic reminder message to frozen players. Placeholders: $reason, $admin")
					.define("freezeReminderFormat", "&c&l⚠ You are still frozen! &7Reason: &f$reason &7- Please respond in chat.");
			freezeReminderIntervalSeconds = builder.comment("  How often (seconds) to remind frozen players (0 = no reminders)")
					.defineInRange("freezeReminderIntervalSeconds", 15, 0, 3600);
			freezeAdminNotifyFormat = builder.comment("  Notification sent to admins when a player is frozen. Placeholders: $player, $admin, $reason, $duration")
					.define("freezeAdminNotifyFormat", "&e$admin &7has frozen &e$player &7for &e$duration&7. Reason: &f$reason");
			unfreezeMessageToPlayer = builder.comment("  Message sent to the player when unfrozen. Placeholder: $admin")
					.define("unfreezeMessageToPlayer", "&a&lYou have been unfrozen by &e$admin&a&l.");
			unfreezeAdminNotifyFormat = builder.comment("  Notification sent to admins when a player is unfrozen. Placeholders: $player, $admin")
					.define("unfreezeAdminNotifyFormat", "&e$admin &7has unfrozen &e$player&7.");
			freezeCommandBlockedMsg = builder.comment("  Message when a frozen player tries to use a command")
					.define("freezeCommandBlockedMsg", "&cYou are frozen and cannot use commands. Please respond in chat.");
			freezeActionBlockedMsg = builder.comment("  Message when a frozen player tries to interact/mine/etc")
					.define("freezeActionBlockedMsg", "&cYou are frozen and cannot do that.");
			freezePlaySound = builder.comment("  Play a sound when a player is frozen").define("freezePlaySound", true);
			freezeAllowChat = builder.comment("  Allow frozen players to chat (so they can respond to the admin)").define("freezeAllowChat", true);
			builder.pop(); // freezeSystem

			// Message Formats
			builder.comment("Message Format Customization",
					"  Available placeholders vary by message type:",
					"  Private Messages: $sender, $receiver, $message",
					"  Reply System: $replier, $original_sender, $summary, $message",
					"  HelpOp: $sender, $message",
					"  Admin Chat: $sender, $message").push("messageFormats");

			msgSentFormat = builder
					.comment("  Format for outgoing private messages. Placeholders: $sender, $receiver, $message")
					.define("msgSentFormat", "&d&lTo &d$receiver&7: &r&7$message");
			msgReceivedFormat = builder
					.comment("  Format for incoming private messages. Placeholders: $sender, $receiver, $message")
					.define("msgReceivedFormat", "&d&lFrom &d$sender&7: &r&7$message");
			replyHeaderFormat = builder
					.comment("  Format for reply header. Placeholders: $replier, $original_sender, $summary")
					.define("replyHeaderFormat", "    &f&l┌────&r &7Replying to $original_sender&7: &7$summary");
			replyBodyFormat = builder
					.comment("  Format for the reply body line (the replier's message). Placeholders: $replier, $message",
							 "  $replier will include the player's rank/prefix/suffix from LuckPerms if available")
					.define("replyBodyFormat", "$replier&7: &r$message");
			helpOpRequestFormat = builder
					.comment("  Format for HelpOp requests to operators. Placeholders: $sender, $message")
					.define("helpOpRequestFormat", "&l&cHelpOp &fFrom &e$sender&7:&r&7 $message");
			helpOpReplyFormat = builder
					.comment("  Format for HelpOp replies to players. Placeholders: $message")
					.define("helpOpReplyFormat", "&l&cHelpOp &4OP&f Replied&7:&r&7 $message");
			adminChatFormat = builder
					.comment("  Format for Admin Chat messages. Placeholders: $sender, $message")
					.define("adminChatFormat", "&4&lAdmin Chat &e$sender&7:&r $message");
			announcementConfirmFormat = builder
					.comment("  Format for announcement added confirmation. Placeholders: $id, $interval, $message")
					.define("announcementConfirmFormat", "&aAdded announcement: &e$id &7(every $interval)");

			builder.pop(); // messageFormats

			// Sound Options
			builder.comment("Sound Notifications").push("sounds");
			enableMsgSound = builder.comment("  Play sound when receiving a private message").define("enableMsgSound", true);
			enableReplySound = builder.comment("  Play sound when someone replies to your message").define("enableReplySound", true);
			enableHelpOpSound = builder.comment("  Play sound for HelpOp notifications").define("enableHelpOpSound", true);
			enableAdminChatSound = builder.comment("  Play sound for Admin Chat messages").define("enableAdminChatSound", false);
			builder.pop(); // sounds

			// System Messages
			builder.comment("System Messages - Customize all feedback messages").push("systemMessages");
			adminChatEnabledMsg = builder.comment("  Message when admin chat is enabled").define("adminChatEnabledMsg", "&aAdmin chat enabled. &7Your messages will only be seen by operators.");
			adminChatDisabledMsg = builder.comment("  Message when admin chat is disabled").define("adminChatDisabledMsg", "&cAdmin chat disabled. &7You are now in public chat.");
			helpOpSentMsg = builder.comment("  Message when helpop is sent. No placeholders.").define("helpOpSentMsg", "&aMessage sent to all online operators. If there is no one online make a discord ticket.");
			helpOpReplySentMsg = builder.comment("  Message when helpop reply is sent. Placeholder: $player").define("helpOpReplySentMsg", "&aReply sent to $player");
			noReplyTargetMsg = builder.comment("  Message when there's no one to reply to").define("noReplyTargetMsg", "&cNo one to reply to.");
			playerOfflineMsg = builder.comment("  Message when target player is offline").define("playerOfflineMsg", "&cThat player is offline.");
			messageNotFoundMsg = builder.comment("  Message when reply target message not found").define("messageNotFoundMsg", "&cMessage not found or too old to reply to.");
			noPermissionMsg = builder.comment("  Message when player lacks permission").define("noPermissionMsg", "&cYou don't have permission to do that.");
			builder.pop(); // systemMessages

			// Hover Text
			builder.comment("Hover Text - Customize hover tooltips").push("hoverText");
			clickToReplyHover = builder.comment("  Hover text for click to reply. Placeholder: $player").define("clickToReplyHover", "&eClick to reply");
			clickToMessageHover = builder.comment("  Hover text for click to message. Placeholder: $player").define("clickToMessageHover", "&dClick to message $player");
			helpOpReplyHover = builder.comment("  Hover text for HelpOp reply. Placeholder: $player").define("helpOpReplyHover", "&7Click to reply to $player");
			builder.pop(); // hoverText

			// Announcement Formatting
			builder.comment("Announcement System Formatting").push("announcementFormatting");
			announcementListHeaderText = builder.comment("  Header for text announcement list").define("announcementListHeaderText", "&6━━━━━━━━ Text Announcements ━━━━━━━━");
			announcementListHeaderCmd = builder.comment("  Header for command announcement list").define("announcementListHeaderCmd", "&6━━━━━━━━ Command Announcements ━━━━━━━━");
			toggleListHeader = builder.comment("  Header for toggle list").define("toggleListHeader", "&6━━━━━━ Toggleable Announcements ━━━━━━");
			toggleOnText = builder.comment("  Text shown when toggle is ON").define("toggleOnText", "&a[ON]");
			toggleOffText = builder.comment("  Text shown when toggle is OFF").define("toggleOffText", "&c[OFF]");
			builder.pop(); // announcementFormatting

			builder.pop(); // ServerEssentialsForgeModConfig
		}
	}
}
