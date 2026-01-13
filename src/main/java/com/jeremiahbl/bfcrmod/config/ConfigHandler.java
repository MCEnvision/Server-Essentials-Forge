package com.jeremiahbl.bfcrmod.config;

import com.jeremiahbl.bfcrmod.BetterForgeChat;
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
        java.nio.file.Path path = FMLPaths.CONFIGDIR.get().resolve(BetterForgeChat.MODID + "-common.toml");
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
			enableFilterSystem = builder.comment("  Enable the word filter system (/bfcr filter commands)").define("enableFilterSystem", true);
			enableMessagingSystem = builder.comment("  Enable the private messaging system (/msg, /r, /tell, etc)").define("enableMessagingSystem", true);
			enableChatReplies = builder.comment("  Enable the chat reply system (/ans command, clickable messages, chat logging)").define("enableChatReplies", true);
			replySummaryLength = builder.comment("  Maximum length of message summary shown in reply headers (0 = no limit)").defineInRange("replySummaryLength", 50, 0, 500);
			builder.pop();
		}
	}
}
