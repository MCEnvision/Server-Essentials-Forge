package com.jeremiahbl.bfcrmod.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigHandler {
	private static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
	public static final ConfigBuilder config = new ConfigBuilder(builder);
	public static ForgeConfigSpec spec = builder.build();

    public static void reloadFromDisk() {
        // Re-read the common config from disk and apply to the spec
        java.nio.file.Path path = FMLPaths.CONFIGDIR.get().resolve("bfcrr").resolve("common.toml");
        try {
            java.nio.file.Files.createDirectories(path.getParent());
        } catch (java.io.IOException ignored) {}
        CommentedFileConfig cfg = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();
        cfg.load();
        spec.setConfig(cfg);
    }

	public static class ConfigBuilder {
		public final ForgeConfigSpec.ConfigValue<String> playerNameFormat;
		public final ForgeConfigSpec.ConfigValue<String> chatMessageFormat;
		public final ForgeConfigSpec.ConfigValue<String> chatMessageColor;
		public final ForgeConfigSpec.ConfigValue<String> timestampFormat;
		public final ForgeConfigSpec.ConfigValue<String> discordBotToken;

		public final ForgeConfigSpec.ConfigValue<Integer> maximumNicknameLength;
		public final ForgeConfigSpec.ConfigValue<Integer> minimumNicknameLength;
		public final ForgeConfigSpec.ConfigValue<String> metaJoinSeparator;
		public final ForgeConfigSpec.ConfigValue<Integer> maxPrefixesDisplayed;
		public final ForgeConfigSpec.ConfigValue<Integer> maxSuffixesDisplayed;

		public final ForgeConfigSpec.ConfigValue<Boolean> enableTimestamp;
		public final ForgeConfigSpec.ConfigValue<Boolean> enableFtbEssentials;
		public final ForgeConfigSpec.ConfigValue<Boolean> enableLuckPerms;
		public final ForgeConfigSpec.ConfigValue<Boolean> enableMarkdown;
		public final ForgeConfigSpec.ConfigValue<Boolean> enableColorsCommand;
		public final ForgeConfigSpec.ConfigValue<Boolean> enableTabListIntegration;
		public final ForgeConfigSpec.ConfigValue<Boolean> enableMetadataInTabList;
		public final ForgeConfigSpec.ConfigValue<Boolean> enableNicknamesInTabList;
		public final ForgeConfigSpec.ConfigValue<Boolean> enableWhoisCommand;
		public final ForgeConfigSpec.ConfigValue<Boolean> enableChatNicknameCommand;
		public final ForgeConfigSpec.ConfigValue<Boolean> autoEnableChatNicknameCommand;
		public final ForgeConfigSpec.ConfigValue<Boolean> enableDiscordBotIntegration;
		public final ForgeConfigSpec.ConfigValue<Boolean> enableCustomTabHeaderFooter;
		public final ForgeConfigSpec.ConfigValue<String> tabHeaderFormat;
		public final ForgeConfigSpec.ConfigValue<String> tabFooterFormat;
		public final ForgeConfigSpec.ConfigValue<Boolean> enableAnnouncements;
		public final ForgeConfigSpec.ConfigValue<Integer> announcementIntervalSeconds;
		public final ForgeConfigSpec.ConfigValue<Boolean> announcementUseRandomOrder;
		public final ForgeConfigSpec.ConfigValue<Boolean> enableFilterSystem;
		public final ForgeConfigSpec.ConfigValue<Boolean> enableMessagingSystem;
		public final ForgeConfigSpec.ConfigValue<Boolean> enableChatReplies;
		public final ForgeConfigSpec.ConfigValue<Integer> replySummaryLength;
		public final ForgeConfigSpec.ConfigValue<Boolean> enableHelpOp;
		public final ForgeConfigSpec.ConfigValue<Boolean> enableAdminChat;
		public final ForgeConfigSpec.ConfigValue<Boolean> enableBannedItems;
		public final ForgeConfigSpec.ConfigValue<Boolean> enableBannedBlockScanning;
		public final ForgeConfigSpec.ConfigValue<Integer> bannedBlockScanRadius;
		public final ForgeConfigSpec.ConfigValue<Integer> bannedBlockScanInterval;
		public final ForgeConfigSpec.ConfigValue<Boolean> enableMotdSystem;
		public final ForgeConfigSpec.ConfigValue<Boolean> applyMotdOnStartup;

		// Message Format Options
		public final ForgeConfigSpec.ConfigValue<String> msgSentFormat;
		public final ForgeConfigSpec.ConfigValue<String> msgReceivedFormat;
		public final ForgeConfigSpec.ConfigValue<String> replyHeaderFormat;
		public final ForgeConfigSpec.ConfigValue<String> helpOpRequestFormat;
		public final ForgeConfigSpec.ConfigValue<String> helpOpReplyFormat;
		public final ForgeConfigSpec.ConfigValue<String> adminChatFormat;
		public final ForgeConfigSpec.ConfigValue<String> announcementConfirmFormat;

		// Sound Options
		public final ForgeConfigSpec.ConfigValue<Boolean> enableMsgSound;
		public final ForgeConfigSpec.ConfigValue<Boolean> enableReplySound;
		public final ForgeConfigSpec.ConfigValue<Boolean> enableHelpOpSound;
		public final ForgeConfigSpec.ConfigValue<Boolean> enableAdminChatSound;

		// Toggle Messages
		public final ForgeConfigSpec.ConfigValue<String> adminChatEnabledMsg;
		public final ForgeConfigSpec.ConfigValue<String> adminChatDisabledMsg;
		public final ForgeConfigSpec.ConfigValue<String> helpOpSentMsg;
		public final ForgeConfigSpec.ConfigValue<String> helpOpReplySentMsg;
		public final ForgeConfigSpec.ConfigValue<String> noReplyTargetMsg;
		public final ForgeConfigSpec.ConfigValue<String> playerOfflineMsg;
		public final ForgeConfigSpec.ConfigValue<String> messageNotFoundMsg;
		public final ForgeConfigSpec.ConfigValue<String> noPermissionMsg;

		// Hover Text Options
		public final ForgeConfigSpec.ConfigValue<String> clickToReplyHover;
		public final ForgeConfigSpec.ConfigValue<String> clickToMessageHover;
		public final ForgeConfigSpec.ConfigValue<String> helpOpReplyHover;

		// Announcement Format Options
		public final ForgeConfigSpec.ConfigValue<String> announcementListHeaderText;
		public final ForgeConfigSpec.ConfigValue<String> announcementListHeaderCmd;
		public final ForgeConfigSpec.ConfigValue<String> toggleListHeader;
		public final ForgeConfigSpec.ConfigValue<String> toggleOnText;
		public final ForgeConfigSpec.ConfigValue<String> toggleOffText;

		public ConfigBuilder(ForgeConfigSpec.Builder builder) {
			builder.push("BetterForgeChatModConfig");
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
			enableTabListIntegration = builder.comment("  Enables or disables custom tab list information").define("tabList", true);
			enableMetadataInTabList = builder.comment("  Enables or disables prefixes&suffixes in the tab list").define("tabListMetadata", true);
			enableNicknamesInTabList = builder.comment("  Enables or disables nicknames in the tab list").define("tabListNicknames", true);
			enableColorsCommand = builder.comment("  Enables or disables the /colors command").define("enableColorsCommand", true);
			enableWhoisCommand = builder.comment(
					"  Enables or disables the integrated whois command"
				  + "    (If autoIntegratedNicknames is true, this setting is ignored) ").define("enableWhoisCommand", true);
			enableChatNicknameCommand = builder.comment(
					  "  Enables or disables the integrated nickname command"
					+ "   (If autoIntegratedNicknames is true, this setting is ignored) ").define("enableIntegratedNicknames", false);
			enableDiscordBotIntegration = builder.comment("  Enables or disables discord integration").define("enableDiscordIntegraation", false);
			enableCustomTabHeaderFooter = builder.comment("  Enables custom tab header/footer rendering server-side").define("enableCustomTabHeaderFooter", false);
			tabHeaderFormat = builder.comment("  Tab header format (placeholders: {server_ip}, {online}, {max})").define("tabHeaderFormat", "");
			tabFooterFormat = builder.comment("  Tab footer format (placeholders: {server_ip}, {online}, {max})").define("tabFooterFormat", "");
			autoEnableChatNicknameCommand = builder.comment("  When true, enables the integrated nickname-related commands if FTB essentials is not present").define("autoIntegratedNicknames", true);
			maximumNicknameLength = builder.comment("  Maximum allowed nickname length (for integrated nickname commands)").defineInRange("maximumNicknameLength", 50, 1, 500);
			minimumNicknameLength = builder.comment("  Minimum allowed nickname length (for integrated nickname commands)").defineInRange("minimumNicknameLength", 1, 1, 500);
			maxPrefixesDisplayed = builder.comment("  Maximum number of prefixes to show (weighted order)").defineInRange("maxPrefixesDisplayed", 1, 0, 50);
			maxSuffixesDisplayed = builder.comment("  Maximum number of suffixes to show (weighted order)").defineInRange("maxSuffixesDisplayed", 1, 0, 50);
			enableAnnouncements = builder.comment("  Enable scheduled server announcements and announcement commands").define("enableAnnouncements", true);
			announcementIntervalSeconds = builder.comment("  Interval in seconds between announcements").define("announcementIntervalSeconds", 300);
			announcementUseRandomOrder = builder.comment("  When true, announcements are chosen randomly; otherwise in order").define("announcementUseRandomOrder", false);
			enableFilterSystem = builder.comment("  Enable the word filter system (/bfcrr filter commands)").define("enableFilterSystem", true);
			enableMessagingSystem = builder.comment("  Enable the private messaging system (/msg, /r, /tell, etc)").define("enableMessagingSystem", true);
			enableChatReplies = builder.comment("  Enable the chat reply system (/ans command, clickable messages, chat logging)").define("enableChatReplies", true);
			replySummaryLength = builder.comment("  Maximum length of message summary shown in reply headers (0 = no limit)").defineInRange("replySummaryLength", 50, 0, 500);
			enableHelpOp = builder.comment("  Enable the /helpop command for players to request help from operators").define("enableHelpOp", true);
			enableAdminChat = builder.comment("  Enable the admin chat system (/chat admin)").define("enableAdminChat", true);
			enableBannedItems = builder.comment("  Enable the banned items system (/banned commands)").define("enableBannedItems", true);
			enableBannedBlockScanning = builder.comment("  Enable scanning for banned blocks placed in the world (may affect TPS slightly)").define("enableBannedBlockScanning", true);
			bannedBlockScanRadius = builder.comment("  Radius around players to scan for banned blocks (smaller = less TPS impact)").defineInRange("bannedBlockScanRadius", 5, 1, 20);
			bannedBlockScanInterval = builder.comment("  How often to scan for banned blocks in ticks (200 = every 10 seconds, higher = less TPS impact)").defineInRange("bannedBlockScanInterval", 200, 20, 6000);
			enableMotdSystem = builder.comment("  Enable the MOTD system (/bfcrr motd commands)").define("enableMotdSystem", true);
			applyMotdOnStartup = builder.comment("  Automatically apply the configured MOTD when the server starts").define("applyMotdOnStartup", true);

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
			helpOpSentMsg = builder.comment("  Message when helpop is sent. Placeholder: $count").define("helpOpSentMsg", "&aYour help request has been sent to $count operator(s)");
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

			builder.pop(); // BetterForgeChatModConfig
		}
	}
}
