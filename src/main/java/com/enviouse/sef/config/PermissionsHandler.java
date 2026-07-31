package com.enviouse.sef.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.control.ServerControlCatalog;
import com.enviouse.sef.permissions.PermissionManifest;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.vanish.Vanishmod;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent.Nodes;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

@EventBusSubscriber(modid = ServerEssentialsForge.MODID)
public class PermissionsHandler {	
	// Chat Permissions
	public static PermissionNode<Boolean> coloredChatNode =
			ezyPermission("chat.colors", true, "Chat colors", "Enables/Disables colors in chat");
	public static PermissionNode<Boolean> styledChatNode = 
			ezyPermission("chat.styles", true, "Chat styles", "Enables/Disables styles in chat");
	public static PermissionNode<Boolean> markdownChatNode = 
			ezyPermission("chat.styles.md", true, "Chat markdown styling", "Enables/Disables markdown styling in chat");

	// Tab List Permissions
	public static PermissionNode<Boolean> tabListNicknameNode =
			ezyPermission("tablist.nickname", true, "Tab list nicknames", "Enables/Disables nicknames showing in the tab list");
	public static PermissionNode<Boolean> tabListMetadataNode = 
			ezyPermission("tablist.metadata", true, "Tab list metadata", "Enables/Disables prefixes&suffixes showing in the tab list");
	
	// Core Command Permissions
	public static PermissionNode<Boolean> colorsCommand =
			ezyPermission("commands.colors", true, "Colors command", "Enables/Disables the \"/colors\" command");
	public static PermissionNode<Boolean> sefCommand = 
			ezyPermission("commands.sef.allowed", true, "SEF command", "Enables/Disables the \"/sef\" command");
	public static PermissionNode<Boolean> sefCommandColorsSubCommand =
			ezyPermission("commands.sef.colors", true, "SEF colors sub-command", "Enables/Disables the \"/sef colors\" sub-command");
	public static PermissionNode<Boolean> sefCommandInfoSubCommand =
			ezyPermission("commands.sef.info", true, "SEF info sub-command", "Enables/Disables the \"/sef info\" sub-command");
	public static PermissionNode<Boolean> sefCommandReloadSubCommand =
			ezyPermission("commands.sef.reload", false, "SEF reload sub-command", "Enables/Disables the \"/sef reload\" sub-command");
	public static PermissionNode<Boolean> sefCommandTestSubCommand =
			ezyPermission("commands.sef.test", false, "SEF test sub-command", "Enables/Disables the \"/sef test\" sub-command");

	// Private Messaging Permissions
	public static PermissionNode<Boolean> msgCommand =
			ezyPermission("commands.msg", true, "Private messages", "Allows using /msg, /r and aliases");
	public static PermissionNode<Boolean> msgReceive =
			ezyPermission("msg.receive", true, "Receive messages", "Allows receiving private messages");
	public static PermissionNode<Boolean> msgSendToOffline =
			ezyPermission("msg.sendoffline", false, "Message offline", "Allows sending messages to offline players (queued)");

	// Nickname Permissions
	public static PermissionNode<Boolean> whoisCommand =
			ezyPermission("commands.whois", true, "Nickname", "Enables/Disables the \"/whois <nickname>\" command");
	public static PermissionNode<Boolean> nickCommand = 
			ezyPermission("commands.nick", true, "Nickname", "Enables/Disables the \"/nick <nickname>\" command");
	public static PermissionNode<Boolean> nickOthersCommand = 
			ezyPermission("commands.nick.others", false, "Modify nicknames", "Enables/Disables the \"/nickfor <username> <nickname>\" command");
	public static PermissionNode<Boolean> nickColorsAllowed =
			ezyPermission("nick.colors", false, "Nickname colors", "Allows using color codes in nicknames");
	public static PermissionNode<Boolean> nickStylesAllowed =
			ezyPermission("nick.styles", false, "Nickname styles", "Allows using style codes in nicknames");

	// Chat Reply Permissions
	public static PermissionNode<Boolean> ansCommand =
			ezyPermission("commands.ans", true, "Reply command", "Allows using the /ans reply command");
	public static PermissionNode<Boolean> ansReceiveNotification =
			ezyPermission("ans.notify", true, "Reply notifications", "Receive sound when someone replies to you");

	// HelpOp Permissions
	public static PermissionNode<Boolean> helpOpSend =
			ezyPermission("helpop.send", true, "Send HelpOp", "Allows sending /helpop requests");
	public static PermissionNode<Boolean> helpOpReceive =
			ezyPermission("helpop.receive", false, "Receive HelpOp", "Receives HelpOp messages (operators)");
	public static PermissionNode<Boolean> helpOpReply =
			ezyPermission("helpop.reply", false, "Reply HelpOp", "Allows using /helpopop to reply");

	// Admin Chat Permissions
	public static PermissionNode<Boolean> adminChatUse =
			ezyPermission("adminchat.use", false, "Use Admin Chat", "Allows using admin chat");
	public static PermissionNode<Boolean> adminChatSee =
			ezyPermission("adminchat.see", false, "See Admin Chat", "Can see admin chat messages");

	// Mute System Permissions
	public static PermissionNode<Boolean> muteSeeBlocked =
			ezyPermission("mute.seeblocked", false, "See muted messages", "Receives muted player messages relayed to operators");
	public static PermissionNode<Boolean> muteCommand =
			ezyPermission("commands.mute", false, "Mute command", "Allows using /mute to mute a player");
	public static PermissionNode<Boolean> unmuteCommand =
			ezyPermission("commands.unmute", false, "Unmute command", "Allows using /unmute to unmute a player");
	public static PermissionNode<Boolean> muteNotify =
			ezyPermission("mute.notify", false, "Mute notifications", "Receives notifications when players are muted/unmuted");

	// InvSee Permissions
	public static PermissionNode<Boolean> invSeeCommand =
			ezyPermission("commands.invsee", false, "InvSee command", "Allows using /invsee to view another player's inventory");
	public static PermissionNode<Boolean> invSeeView =
			ezyPermission("commands.invsee.view", false, "View inventories", "Allows read only viewing of another player's inventory");
	public static PermissionNode<Boolean> invSeeModify =
			ezyPermission("commands.invsee.modify", false, "Modify inventories", "Allows modifying another player's inventory");
	public static PermissionNode<Boolean> invSeeOffline =
			ezyPermission("commands.invsee.offline", false, "View offline inventories", "Allows opening supported offline inventory data");
	public static PermissionNode<Boolean> invSeeCurios =
			ezyPermission("commands.invsee.curios", false, "View Curios inventories", "Allows viewing another player's Curios inventory");
	public static PermissionNode<Boolean> enderChestOthers =
			ezyPermission("commands.enderchest.others", false, "View ender chests", "Allows viewing another player's ender chest");

	// virtual workstation permissions
	public static PermissionNode<Boolean> craftingTableCommand =
			ezyPermission("commands.craft", true, "Craft command", "Allows using /craft and /c");
	public static PermissionNode<Boolean> anvilCommand =
			ezyPermission("commands.anvil", true, "Anvil command", "Allows using /anvil and /av");
	public static PermissionNode<Boolean> enchantingTableCommand =
			ezyPermission("commands.enchantingtable", true, "Enchanting table command", "Allows using /enchantingtable and /et");
	public static PermissionNode<Boolean> superEnchantingTableCommand =
			ezyPermission("commands.superenchantingtable", false, "Super enchanting table command", "Allows using /superenchantingtable and /set");
	public static PermissionNode<Boolean> repairCommand =
			ezyPermission("commands.repair", false, "Repair command", "Allows using /repair on the held item");
	public static PermissionNode<Boolean> craftingTableCooldownBypass =
			ezyPermission("cooldowns.bypass.craft", false, "Craft cooldown bypass", "Bypasses the /craft cooldown");
	public static PermissionNode<Boolean> anvilCooldownBypass =
			ezyPermission("cooldowns.bypass.anvil", false, "Anvil cooldown bypass", "Bypasses the /anvil cooldown");
	public static PermissionNode<Boolean> enchantingTableCooldownBypass =
			ezyPermission("cooldowns.bypass.enchantingtable", false, "Enchanting table cooldown bypass", "Bypasses the /enchantingtable cooldown");
	public static PermissionNode<Boolean> superEnchantingTableCooldownBypass =
			ezyPermission("cooldowns.bypass.superenchantingtable", false, "Super enchanting table cooldown bypass", "Bypasses the super enchanting table cooldown");
	public static PermissionNode<Boolean> repairCooldownBypass =
			ezyPermission("cooldowns.bypass.repair", false, "Repair cooldown bypass", "Bypasses the /repair cooldown");

	public static PermissionNode<Boolean> homeCommand =
			ezyPermission("commands.home", true, "Home command", "Allows teleporting to an owned home");
	public static PermissionNode<Boolean> setHomeCommand =
			ezyPermission("commands.sethome", true, "Set home command", "Allows creating or replacing an owned home");
	public static PermissionNode<Boolean> deleteHomeCommand =
			ezyPermission("commands.delhome", true, "Delete home command", "Allows deleting an owned home");
	public static PermissionNode<Boolean> renameHomeCommand =
			ezyPermission("commands.renamehome", true, "Rename home command", "Allows renaming an owned home");
	public static PermissionNode<Boolean> homesCommand =
			ezyPermission("commands.homes", true, "Homes command", "Allows listing owned homes");
	public static PermissionNode<Boolean> homesOthersCommand =
			ezyPermission("commands.homes.others", false, "List other homes", "Allows listing another player's homes");
	public static PermissionNode<Boolean> homesCoordinates =
			ezyPermission("homes.coordinates", false, "Home coordinates", "Allows viewing stored home coordinates");
	public static PermissionNode<Boolean> homeAdminCommand =
			ezyPermission("commands.homeadmin", false, "Home administration", "Allows using home administration commands");
	public static PermissionNode<Boolean> homeAdminList =
			ezyPermission("home.admin.list", false, "Home administration list", "Allows listing another player's homes");
	public static PermissionNode<Boolean> homeAdminTeleport =
			ezyPermission("home.admin.teleport", false, "Home administration teleport", "Allows teleporting to another player's home");
	public static PermissionNode<Boolean> homeAdminSet =
			ezyPermission("home.admin.set", false, "Home administration set", "Allows setting another player's home");
	public static PermissionNode<Boolean> homeAdminDelete =
			ezyPermission("home.admin.delete", false, "Home administration delete", "Allows deleting another player's home");
	public static PermissionNode<Boolean> homeAdminRename =
			ezyPermission("home.admin.rename", false, "Home administration rename", "Allows renaming another player's home");
	public static PermissionNode<Boolean> homeAdminRestore =
			ezyPermission("home.admin.restore", false, "Home administration restore", "Allows restoring a deleted home");
	public static PermissionNode<Boolean> homeAdminLimit =
			ezyPermission("home.admin.limit", false, "Home administration limit", "Allows inspecting another player's effective home quota");
	public static PermissionNode<Boolean> homeAdminExport =
			ezyPermission("home.admin.export", false, "Home administration export", "Allows inspecting home repository export metadata");

	public static PermissionNode<Boolean> tpaCommand =
			ezyPermission("commands.tpa", true, "Teleport request command", "Allows requesting a teleport to another player");
	public static PermissionNode<Boolean> tpaHereCommand =
			ezyPermission("commands.tpahere", true, "Teleport here request command", "Allows requesting another player teleport to you");
	public static PermissionNode<Boolean> tpAcceptCommand =
			ezyPermission("commands.tpaccept", true, "Accept teleport request", "Allows accepting an incoming teleport request");
	public static PermissionNode<Boolean> tpDenyCommand =
			ezyPermission("commands.tpdeny", true, "Deny teleport request", "Allows denying an incoming teleport request");
	public static PermissionNode<Boolean> tpCancelCommand =
			ezyPermission("commands.tpcancel", true, "Cancel teleport request", "Allows cancelling an outgoing teleport request");
	public static PermissionNode<Boolean> tpRequestsCommand =
			ezyPermission("commands.tprequests", true, "List teleport requests", "Allows listing incoming and outgoing teleport requests");
	public static PermissionNode<Boolean> tpToggleCommand =
			ezyPermission("commands.tptoggle", true, "Toggle teleport requests", "Allows disabling incoming teleport requests");
	public static PermissionNode<Boolean> tpBlockCommand =
			ezyPermission("commands.tpblock", true, "Block teleport requests", "Allows blocking teleport requests from a player");
	public static PermissionNode<Boolean> tpAutoAcceptCommand =
			ezyPermission("commands.tpautoaccept", false, "Auto accept teleport requests", "Allows automatically accepting teleport requests");

	public static PermissionNode<Boolean> backCommand =
			ezyPermission("commands.back", true, "Back command", "Allows teleporting to the latest valid departure location");
	public static PermissionNode<Boolean> backDeathCommand =
			ezyPermission("commands.back.death", true, "Back death command", "Allows teleporting to the latest recorded death location");
	public static PermissionNode<Boolean> spawnCommand =
			ezyPermission("commands.spawn", true, "Spawn command", "Allows teleporting to the resolved server spawn");
	public static PermissionNode<Boolean> setSpawnCommand =
			ezyPermission("commands.setspawn", false, "Set spawn command", "Allows setting server spawn layers");
	public static PermissionNode<Boolean> spawnInfoCommand =
			ezyPermission("commands.spawninfo", false, "Spawn info command", "Allows inspecting configured spawn layers");

	public static PermissionNode<Boolean> warpCommand =
			ezyPermission("commands.warp", true, "Warp command", "Allows using permitted server warps");
	public static PermissionNode<Boolean> warpsCommand =
			ezyPermission("commands.warps", true, "Warps command", "Allows listing visible server warps");
	public static PermissionNode<Boolean> setWarpCommand =
			ezyPermission("commands.setwarp", false, "Set warp command", "Allows creating or relocating server warps");
	public static PermissionNode<Boolean> deleteWarpCommand =
			ezyPermission("commands.delwarp", false, "Delete warp command", "Allows deleting server warps");
	public static PermissionNode<Boolean> renameWarpCommand =
			ezyPermission("commands.renamewarp", false, "Rename warp command", "Allows renaming server warps");
	public static PermissionNode<Boolean> warpInfoCommand =
			ezyPermission("commands.warpinfo", false, "Warp info command", "Allows viewing server warp metadata and coordinates");
	public static PermissionNode<Boolean> warpManageCommand =
			ezyPermission("commands.warp.manage", false, "Warp management command", "Allows enabling and disabling server warps");
	public static PermissionNode<Boolean> warpHiddenView =
			ezyPermission("warps.hidden.view", false, "View hidden warps", "Allows listing hidden server warps");

	public static PermissionNode<Boolean> playerWarpCommand =
			ezyPermission("commands.pwarp", true, "Player warp command", "Allows visiting accessible player warps");
	public static PermissionNode<Boolean> playerWarpsCommand =
			ezyPermission("commands.pwarps", true, "Player warps command", "Allows listing accessible player warps");
	public static PermissionNode<Boolean> setPlayerWarpCommand =
			ezyPermission("commands.setpwarp", true, "Set player warp command", "Allows creating owned player warps");
	public static PermissionNode<Boolean> deletePlayerWarpCommand =
			ezyPermission("commands.delpwarp", true, "Delete player warp command", "Allows deleting owned player warps");
	public static PermissionNode<Boolean> renamePlayerWarpCommand =
			ezyPermission("commands.renamepwarp", true, "Rename player warp command", "Allows renaming owned player warps");
	public static PermissionNode<Boolean> playerWarpEdit =
			ezyPermission("playerwarps.edit", true, "Edit player warps", "Allows editing owned player warps");
	public static PermissionNode<Boolean> playerWarpPublish =
			ezyPermission("playerwarps.publish", true, "Publish player warps", "Allows publishing owned player warps");
	public static PermissionNode<Boolean> playerWarpAccess =
			ezyPermission("playerwarps.access", true, "Player warp access lists", "Allows managing owned player warp access lists");
	public static PermissionNode<Boolean> playerWarpTransfer =
			ezyPermission("playerwarps.transfer", true, "Transfer player warps", "Allows offering and accepting player warp transfers");
	public static PermissionNode<Boolean> playerWarpFavorite =
			ezyPermission("playerwarps.favorite", true, "Favorite player warps", "Allows managing favorite player warps");
	public static PermissionNode<Boolean> playerWarpReport =
			ezyPermission("playerwarps.report", true, "Report player warps", "Allows reporting a player warp to staff");
	public static PermissionNode<Boolean> playerWarpModerate =
			ezyPermission("playerwarps.moderate", false, "Moderate player warps", "Allows inspecting and moderating player warps");

	public static PermissionNode<Boolean> randomTeleportCommand =
			ezyPermission("commands.rtp", true, "Random teleport command", "Allows bounded random teleport in configured dimensions");
	public static PermissionNode<Boolean> setRandomTeleportCommand =
			ezyPermission("commands.settpr", false, "Set random teleport center", "Allows setting a random teleport center");
	public static PermissionNode<Boolean> directTeleportCommand =
			ezyPermission("commands.tp", false, "Direct teleport command", "Allows direct teleport targeting");
	public static PermissionNode<Boolean> teleportHereCommand =
			ezyPermission("commands.tphere", false, "Teleport here command", "Allows teleporting another player to the actor");
	public static PermissionNode<Boolean> teleportOverrideCommand =
			ezyPermission("commands.tpo", false, "Teleport override command", "Allows direct teleport with administrative policy");
	public static PermissionNode<Boolean> teleportOverrideHereCommand =
			ezyPermission("commands.tpohere", false, "Teleport override here command", "Allows teleporting another player to the actor with administrative policy");
	public static PermissionNode<Boolean> teleportOfflineCommand =
			ezyPermission("commands.tpoffline", false, "Offline teleport command", "Allows queuing a location for a player's next login");
	public static PermissionNode<Boolean> teleportPositionCommand =
			ezyPermission("commands.tppos", false, "Teleport position command", "Allows direct coordinate teleport");
	public static PermissionNode<Boolean> teleportAllCommand =
			ezyPermission("commands.tpall", false, "Teleport all command", "Allows teleporting all visible online players");
	public static PermissionNode<Boolean> teleportAskAllCommand =
			ezyPermission("commands.tpaall", false, "Request all command", "Allows sending bounded requests to all visible online players");
	public static PermissionNode<Boolean> teleportExempt =
			ezyPermission("teleport.exempt", false, "Teleport target exemption", "Prevents normal staff teleport targeting");
	public static PermissionNode<Boolean> teleportBypassExempt =
			ezyPermission("teleport.bypass.exempt", false, "Teleport exemption bypass", "Allows targeting players with teleport exemption");
	public static PermissionNode<Boolean> teleportHierarchyBypass =
			ezyPermission("teleport.hierarchy.bypass", false, "Teleport hierarchy bypass", "Allows targeting players regardless of hierarchy");
	public static PermissionNode<Boolean> teleportCooldownBypass =
			ezyPermission("cooldowns.bypass.teleport", false, "Teleport cooldown bypass", "Bypasses user teleport cooldowns");
	public static PermissionNode<Boolean> teleportWarmupBypass =
			ezyPermission("warmups.bypass.teleport", false, "Teleport warmup bypass", "Bypasses user teleport warmups");
	public static PermissionNode<Boolean> teleportSafetyBypass =
			ezyPermission("teleport.safety.bypass", false, "Teleport safety bypass", "Allows explicit administrative unsafe teleports");

	public static PermissionNode<Boolean> messageToggleCommand =
			ezyPermission("commands.msgtoggle", true, "Message toggle", "Allows toggling incoming private messages");
	public static PermissionNode<Boolean> replyToggleCommand =
			ezyPermission("commands.rtoggle", true, "Reply toggle", "Allows toggling private message replies");
	public static PermissionNode<Boolean> ignoreCommand =
			ezyPermission("commands.ignore", true, "Ignore command", "Allows managing the private interaction block list");
	public static PermissionNode<Boolean> ignoreListCommand =
			ezyPermission("commands.ignorelist", true, "Ignore list command", "Allows viewing the private interaction block list");
	public static PermissionNode<Boolean> socialSpyCommand =
			ezyPermission("commands.socialspy", false, "Social spy", "Allows requesting private message observation");
	public static PermissionNode<Boolean> socialSpyStatus =
			ezyPermission("commands.socialspy.status", false, "Social spy status", "Allows inspecting social spy state");
	public static PermissionNode<Boolean> socialSpyRecent =
			ezyPermission("commands.socialspy.recent", false, "Social spy recent", "Allows viewing bounded authorized recent observations");
	public static PermissionNode<Boolean> socialSpyEveryone =
			ezyPermission("commands.socialspy.everyone", false, "Social spy everyone", "Allows observing all eligible private message routes");
	public static PermissionNode<Boolean> socialSpyPlayer =
			ezyPermission("commands.socialspy.player", false, "Social spy player filter", "Allows observing eligible conversations for selected players");
	public static PermissionNode<Boolean> socialSpySelected =
			ezyPermission("commands.socialspy.selected", false, "Social spy selected filters", "Allows managing selected UUID filters");
	public static PermissionNode<Boolean> socialSpyScopeMetadata =
			ezyPermission("commands.socialspy.scope.metadata", false, "Social spy metadata scope", "Allows metadata only observation");
	public static PermissionNode<Boolean> socialSpyScopeContent =
			ezyPermission("commands.socialspy.scope.content", false, "Social spy content scope", "Allows requesting private message content");
	public static PermissionNode<Boolean> socialSpyFilter =
			ezyPermission("commands.socialspy.filter", false, "Social spy route filters", "Allows managing stable route filters");
	public static PermissionNode<Boolean> socialSpyFormatPreview =
			ezyPermission("commands.socialspy.format.preview", false, "Social spy format preview", "Allows previewing the current typed social spy format");
	public static PermissionNode<Boolean> socialSpyOthers =
			ezyPermission("commands.socialspy.others", false, "Manage other social spy state", "Allows managing another observer through the canonical SEF route");
	public static PermissionNode<Boolean> socialSpyViewMetadata =
			ezyPermission("socialspy.view.metadata", false, "View social metadata", "Allows receiving authorized social observation metadata");
	public static PermissionNode<Boolean> socialSpyViewContent =
			ezyPermission("socialspy.view.content", false, "View social content", "Allows receiving authorized private message content");
	public static PermissionNode<Boolean> socialSpyViewVanished =
			ezyPermission("socialspy.view.vanished", false, "View vanished social events", "Allows observing participants the viewer may otherwise see through vanish policy");
	public static PermissionNode<Boolean> socialSpyViewExempt =
			ezyPermission("socialspy.view.exempt", false, "Override social spy exemption", "Allows observing exempt participants with sensitive audit policy");
	public static PermissionNode<Boolean> socialSpyHierarchyBypass =
			ezyPermission("socialspy.hierarchy.bypass", false, "Social spy hierarchy bypass", "Allows observing participants regardless of hierarchy");
	public static PermissionNode<Boolean> socialSpyExempt =
			ezyPermission("socialspy.exempt", false, "Social spy exemption", "Prevents ordinary private message observation");
	public static PermissionNode<Boolean> mailCommand =
			ezyPermission("commands.mail", true, "Mail command", "Allows reading and managing owned mail");
	public static PermissionNode<Boolean> mailSendCommand =
			ezyPermission("commands.mail.send", true, "Send mail", "Allows sending bounded UUID addressed mail");
	public static PermissionNode<Boolean> joinMessageSet =
			ezyPermission("commands.joinmessage.set", false, "Set join message", "Allows setting a player's real join template");
	public static PermissionNode<Boolean> joinMessageClear =
			ezyPermission("commands.joinmessage.clear", false, "Clear join message", "Allows clearing a player's real join template");
	public static PermissionNode<Boolean> joinMessagePreview =
			ezyPermission("commands.joinmessage.preview", false, "Preview join message", "Allows previewing a player's join template");
	public static PermissionNode<Boolean> leaveMessageSet =
			ezyPermission("commands.leavemessage.set", false, "Set leave message", "Allows setting a player's real leave template");
	public static PermissionNode<Boolean> leaveMessageClear =
			ezyPermission("commands.leavemessage.clear", false, "Clear leave message", "Allows clearing a player's real leave template");
	public static PermissionNode<Boolean> leaveMessagePreview =
			ezyPermission("commands.leavemessage.preview", false, "Preview leave message", "Allows previewing a player's leave template");
	public static PermissionNode<Boolean> connectionMessageInspect =
			ezyPermission("commands.connectionmessage.inspect", false, "Inspect connection messages", "Allows inspecting connection message revisions");
	public static PermissionNode<Boolean> connectionMessageHierarchyBypass =
			ezyPermission("connectionmessage.hierarchy.bypass", false, "Connection message hierarchy bypass", "Allows managing connection messages regardless of hierarchy");
	public static PermissionNode<Boolean> connectionMessageExempt =
			ezyPermission("connectionmessage.exempt", false, "Connection message exemption", "Prevents ordinary connection message management");
	public static PermissionNode<Boolean> connectionMessageBypassExempt =
			ezyPermission("connectionmessage.bypass.exempt", false, "Connection message exemption bypass", "Allows managing exempt connection message targets");
	public static PermissionNode<Boolean> welcomePreview =
			ezyPermission("commands.welcome.preview", false, "Preview welcome message", "Allows previewing welcome definitions");
	public static PermissionNode<Boolean> welcomeSend =
			ezyPermission("commands.welcome.send", false, "Send welcome message", "Allows manually sending welcome definitions");
	public static PermissionNode<Boolean> reminderManage =
			ezyPermission("commands.reminder.manage", false, "Manage reminders", "Allows creating, editing, pausing, and deleting reminders");
	public static PermissionNode<Boolean> reminderSend =
			ezyPermission("commands.reminder.send", false, "Send reminders", "Allows manually sending reminders");
	public static PermissionNode<Boolean> remindersCommand =
			ezyPermission("commands.reminders", true, "List reminders", "Allows listing applicable reminders");
	public static PermissionNode<Boolean> reminderDismiss =
			ezyPermission("commands.reminder.dismiss", true, "Dismiss reminders", "Allows dismissing and restoring owned reminder state");
	public static PermissionNode<Boolean> customTextCommand =
			ezyPermission("commands.customtext", true, "Custom text", "Allows reading configured text pages");
	public static PermissionNode<Boolean> customTextManage =
			ezyPermission("commands.customtext.manage", false, "Manage custom text", "Allows changing versioned text pages");
	public static PermissionNode<Boolean> identityCoverage =
			ezyPermission("commands.sef.identity.coverage", false, "Identity coverage", "Allows inspecting nickname projection coverage");
	public static PermissionNode<Boolean> identityRefresh =
			ezyPermission("commands.sef.identity.refresh", false, "Identity refresh", "Allows refreshing server projected identity state");

	// Freeze Permissions
	public static PermissionNode<Boolean> freezeCommand =
			ezyPermission("commands.freeze", false, "Freeze command", "Allows using /freeze to freeze a player in place");
	public static PermissionNode<Boolean> unfreezeCommand =
			ezyPermission("commands.unfreeze", false, "Unfreeze command", "Allows using /unfreeze to unfreeze a player");
	public static PermissionNode<Boolean> freezeNotify =
			ezyPermission("freeze.notify", false, "Freeze notifications", "Receives notifications when players are frozen/unfrozen");

	// Clear Chat Permissions
	public static PermissionNode<Boolean> clearChatCommand =
			ezyPermission("commands.clearchat", false, "Clear Chat command", "Allows using /cc and /clearchat to clear chat");

	// Sudo Permissions
	public static PermissionNode<Boolean> sudoCommand =
			ezyPermission("commands.sudo", false, "Sudo command", "Allows using /sudo to force a player to execute a command");
	public static PermissionNode<Boolean> sudoExempt =
			ezyPermission("sudo.exempt", false, "Sudo exemption", "Prevents other players from targeting this player with /sudo");
	public static PermissionNode<Boolean> sudoBypassExempt =
			ezyPermission("sudo.bypass.exempt", false, "Sudo exemption bypass", "Allows targeting players with the sudo exemption");

	// Vanish Permissions
	public static PermissionNode<Boolean> vanishOthersCommand =
			ezyPermission("commands.vanish.others", false, "Vanish others", "Allows changing another player's vanish state");
	public static PermissionNode<Boolean> vanishQueueCommand =
			ezyPermission("commands.vanish.queue", false, "Vanish queue", "Allows managing the vanish queue");
	public static PermissionNode<Boolean> vanishGetOthersCommand =
			ezyPermission("commands.vanish.get.others", false, "Inspect vanish state", "Allows inspecting another player's vanish state");
	public static PermissionNode<Boolean> vanishExempt =
			ezyPermission("vanish.exempt", false, "Vanish exemption", "Prevents other players from changing this player's vanish state");
	public static PermissionNode<Boolean> vanishBypassExempt =
			ezyPermission("vanish.bypass.exempt", false, "Vanish exemption bypass", "Allows changing vanish state for exempt players");
	public static PermissionNode<Boolean> vanishHierarchyBypass =
			ezyPermission("vanish.hierarchy.bypass", false, "Vanish hierarchy bypass", "Allows changing vanish state regardless of target hierarchy");

	// Inventory Lock Permissions
	public static PermissionNode<Boolean> invLockCommand =
			ezyPermission("commands.invlock", false, "InvLock command", "Allows using /invlock to lock/unlock a player's inventory");

	// Disable Building Permissions
	public static PermissionNode<Boolean> disableBuildingCommand =
			ezyPermission("commands.disablebuilding", false, "Disable Building command", "Allows using /disablebuilding to toggle building restrictions");

	// Check Alts Permissions
	public static PermissionNode<Boolean> checkAltsCommand =
			ezyPermission("commands.checkalts", false, "Check Alts command", "Allows using /checkalts to list alternate accounts");
	public static PermissionNode<Boolean> checkAltsIpView =
			ezyPermission("alts.ip.view", false, "View alternate account addresses", "Allows viewing raw addresses in alternate account results");
	public static PermissionNode<Boolean> checkAltsPurge =
			ezyPermission("alts.purge", false, "Purge alternate account data", "Allows deleting retained alternate account records");
	public static PermissionNode<Boolean> checkAltsExport =
			ezyPermission("alts.export", false, "Export alternate account data", "Allows exporting retained alternate account records");

	// Warn System Permissions
	public static PermissionNode<Boolean> warnCommand =
			ezyPermission("commands.warn", false, "Warn command", "Allows using /warn to add/check/remove warnings on players");
	public static PermissionNode<Boolean> warnsSelfCommand =
			ezyPermission("commands.warns", true, "Warns self command", "Allows using /warns to check own warnings");

	// Announcement Permissions
	public static PermissionNode<Boolean> announcementManage =
			ezyPermission("announcements.manage", false, "Manage announcements", "Allows adding/removing announcements");
	public static PermissionNode<Boolean> commandAnnouncementManage =
			ezyPermission("announcements.command.manage", false, "Manage command announcements", "Allows adding and removing command announcements");
	public static PermissionNode<Boolean> announcementToggle =
			ezyPermission("announcements.toggle", true, "Toggle announcements", "Allows toggling announcements on/off");
	public static PermissionNode<Boolean> announcementBypass =
			ezyPermission("announcements.bypass", false, "Bypass announcements", "Receives announcements even if toggled off");
	public static PermissionNode<Boolean> titleAnnouncementUse =
			ezyPermission("announcements.title", false, "Title announcements", "Allows using /titleannouncement");

	// Filter Permissions
	public static PermissionNode<Boolean> filterManage =
			ezyPermission("filter.manage", false, "Manage filters", "Allows managing word filters");
	public static PermissionNode<Boolean> filterBypass =
			ezyPermission("filter.bypass", false, "Bypass filters", "Messages bypass word filter");

	// Bulletin and MOTD Permissions
	public static PermissionNode<Boolean> opBulletinManage =
			ezyPermission("opbulletin.manage", false, "Manage operator bulletins", "Allows managing operator bulletins");
	public static PermissionNode<Boolean> opBulletinReceive =
			ezyPermission("opbulletin.receive", false, "Receive operator bulletins", "Shows operator bulletins on login");
	public static PermissionNode<Boolean> motdManage =
			ezyPermission("motd.manage", false, "Manage MOTD", "Allows viewing and changing the server MOTD");

	// Banned Items Permissions
	public static PermissionNode<Boolean> bannedCommand =
			ezyPermission("commands.banned", false, "Banned command", "Allows using /banned subcommands (add/remove/etc)");
	public static PermissionNode<Boolean> bannedView =
			ezyPermission("banned.view", true, "View banned items", "Allows viewing the banned item list");
	public static PermissionNode<Boolean> bannedBypassNode =
			ezyPermission("banned.bypass", false, "Bypass banned items", "Player is exempt from banned-item confiscation and banned-block sweeps");

	// Countdown Permissions
	public static PermissionNode<Boolean> countdownCommand =
			ezyPermission("commands.countdown", false, "Countdown command", "Allows using /countdown to broadcast a timed countdown");

	// Sign and Misc Permissions
	public static PermissionNode<Boolean> signColorNode =
			ezyPermission("sign.colors", false, "Sign colors", "Allows usage of colors on signs");
	public static PermissionNode<Boolean> signStylesNode =
			ezyPermission("sign.styles", false, "Sign styles", "Allows usage of styles on signs");
	public static PermissionNode<Boolean> storageStatus =
			ezyPermission("storage.status", false, "Storage status", "Allows viewing SEF storage diagnostics");
	public static PermissionNode<Boolean> storageExport =
			ezyPermission("storage.export", false, "Storage export", "Allows creating a bounded SEF data export");
	public static PermissionNode<Boolean> sefCommandsCatalog =
			ezyPermission("commands.sef.commands", true, "Command catalog", "Allows viewing the permission filtered SEF command catalog");
	public static PermissionNode<Boolean> sefConflicts =
			ezyPermission("commands.sef.conflicts", false, "Command conflicts", "Allows viewing command root ownership and collision diagnostics");
	public static PermissionNode<Boolean> sefDoctor =
			ezyPermission("commands.sef.doctor", false, "SEF doctor", "Allows viewing kernel, provider, policy, and storage diagnostics");
	public static PermissionNode<Boolean> kernelGui =
			ezyPermission("kernel.gui.use", false, "Kernel GUI capability", "Allows receiving enhanced GUI descriptors when the client protocol is available");
	public static PermissionNode<Boolean> kernelHud =
			ezyPermission("kernel.hud.use", false, "Kernel HUD capability", "Allows receiving enhanced HUD descriptors when the client protocol is available");
	public static PermissionNode<Boolean> kernelPanel =
			ezyPermission("kernel.panel.use", false, "Kernel panel capability", "Allows opening permission filtered administrative panels");
	public static PermissionNode<Boolean> kernelTarget =
			ezyPermission("kernel.target.others", false, "Kernel target capability", "Allows selecting other players where the canonical action separately permits it");
	public static PermissionNode<Boolean> kernelAudience =
			ezyPermission("kernel.audience.broad", false, "Kernel audience capability", "Allows selecting bounded multi player audiences where the canonical action permits it");
	public static PermissionNode<Boolean> kernelEditor =
			ezyPermission("kernel.editor.use", false, "Kernel editor capability", "Allows using definition editors when a domain grants its action permission");
	public static PermissionNode<Boolean> kernelAlias =
			ezyPermission("kernel.alias.use", false, "Kernel alias capability", "Allows using published aliases in addition to their canonical action permissions");
	public static PermissionNode<Boolean> kernelBundle =
			ezyPermission("kernel.bundle.use", false, "Kernel bundle capability", "Allows using published bundles in addition to every underlying action permission");
	public static PermissionNode<Boolean> kernelProfile =
			ezyPermission("kernel.profile.use", false, "Kernel execution profile capability", "Allows using explicitly approved execution profiles");
	public static PermissionNode<Boolean> panelList =
			ezyPermission("commands.panel.list", false, "List administrative panels", "Allows listing permission filtered built in and published administrative panels");
	public static PermissionNode<Boolean> panelInspect =
			ezyPermission("commands.panel.inspect", false, "Inspect administrative panels", "Allows viewing typed panel controls and command fallbacks");
	public static PermissionNode<Boolean> panelPreview =
			ezyPermission("commands.panel.preview", false, "Preview administrative panels", "Allows dry run preview of a panel definition and its impact");
	public static PermissionNode<Boolean> panelRun =
			ezyPermission("commands.panel.run", false, "Run administrative panel controls", "Allows invoking a typed panel control while preserving the canonical action permission");
	public static PermissionNode<Boolean> panelDraft =
			ezyPermission("commands.panel.draft", false, "Edit administrative panel drafts", "Allows creating and changing unpublished administrative panel drafts");
	public static PermissionNode<Boolean> panelPublish =
			ezyPermission("commands.panel.publish", false, "Publish administrative panels", "Allows publishing a validated panel draft");
	public static PermissionNode<Boolean> panelRollback =
			ezyPermission("commands.panel.rollback", false, "Rollback administrative panels", "Allows restoring an immutable panel history revision");
	public static PermissionNode<Boolean> panelRunServerProfile =
			ezyPermission("kernel.panel.context.server", false, "Use server panel profiles", "Allows selecting a reviewed server execution profile for an eligible panel control");
	public static PermissionNode<Boolean> panelRunNativeBulk =
			ezyPermission("kernel.panel.context.bulk", false, "Use native panel bulk execution", "Allows bounded native bulk execution for an eligible panel control");
	public static PermissionNode<Boolean> panelRunAsParticipants =
			ezyPermission("kernel.panel.context.participants", false, "Use participant panel execution", "Allows independently authorized participant execution for an eligible panel control");
	public static PermissionNode<Boolean> guiPreferences =
			ezyPermission("commands.sef.client.preferences", true, "Manage personal GUI preferences", "Allows changing personal optional GUI, pause button, HUD, motion, and page size preferences");
	public static PermissionNode<Boolean> kernelBypass =
			ezyPermission("kernel.bypass.use", false, "Kernel bypass capability", "Allows specifically declared policy bypasses without bypassing canonical action permission");
	public static PermissionNode<Boolean> kernelSensitiveData =
			ezyPermission("kernel.sensitive.view", false, "Kernel sensitive data capability", "Allows specifically declared sensitive fields after per field authorization");
	public static final Map<String, PermissionNode<Boolean>> quotaTierNodes = new LinkedHashMap<>();
	public static final Map<String, PermissionNode<Boolean>> phaseSixSevenNodes = new LinkedHashMap<>();

	// Color Permissions
	public static final Map<Character, PermissionNode<Boolean>> perColorChatNodes = new LinkedHashMap<>();
	public static PermissionNode<Boolean> hexChatNode;

	static {
		registerPhasePermission("commands.ban", false, "Ban players", "Allows adding permanent player bans");
		registerPhasePermission("commands.tempban", false, "Temporarily ban players", "Allows adding expiring player bans");
		registerPhasePermission("commands.pardon", false, "Pardon players", "Allows removing player bans");
		registerPhasePermission("commands.unban", false, "Unban players", "Allows the unban alias for player pardons");
		registerPhasePermission("commands.banip", false, "Ban addresses", "Allows adding authoritative address bans");
		registerPhasePermission("commands.banip.literal", false, "Ban literal addresses", "Allows entering literal addresses for address bans");
		registerPhasePermission("commands.tempbanip", false, "Temporarily ban addresses", "Allows adding expiring authoritative address bans");
		registerPhasePermission("commands.pardonip", false, "Pardon addresses", "Allows removing authoritative address bans");
		registerPhasePermission("commands.pardonip.literal", false, "Pardon literal addresses", "Allows entering exact literal addresses for address pardons");
		registerPhasePermission("commands.unbanip", false, "Unban addresses", "Allows address pardon aliases");
		registerPhasePermission("commands.kick", false, "Kick players", "Allows disconnecting one eligible player");
		registerPhasePermission("commands.kickip", false, "Kick shared addresses", "Allows bounded shared address disconnects");
		registerPhasePermission("commands.kickip.literal", false, "Use literal kick addresses", "Allows entering literal addresses for shared address kicks");
		registerPhasePermission("commands.kickme", true, "Disconnect self", "Allows the self only kickme command");
		registerPhasePermission("commands.kickall", false, "Kick eligible players", "Allows bounded mass disconnects");
		registerPhasePermission("exempt.ban", false, "Ban exemption", "Prevents ordinary player ban targeting");
		registerPhasePermission("exempt.ipban", false, "Address ban exemption", "Prevents ordinary shared address ban targeting");
		registerPhasePermission("exempt.kick", false, "Kick exemption", "Prevents ordinary kick targeting");
		registerPhasePermission("exempt.kickip", false, "Shared address kick exemption", "Prevents ordinary shared address kick targeting");
		registerPhasePermission("moderation.hierarchy.bypass", false, "Moderation hierarchy bypass", "Allows moderation across target hierarchy");
		registerPhasePermission("moderation.bypass.exempt", false, "Moderation exemption bypass", "Allows targeting moderation exempt players");
		registerPhasePermission("privacy.ip.view_redacted", false, "View redacted addresses", "Allows viewing keyed address fingerprints");
		registerPhasePermission("privacy.ip.view_full", false, "View full addresses", "Allows owner approved full address diagnostics");

		registerPhasePermission("commands.setjail", false, "Set jails", "Allows defining named jail destinations");
		registerPhasePermission("commands.deljail", false, "Delete jails", "Allows deleting named jail destinations");
		registerPhasePermission("commands.jails", false, "List jails", "Allows listing named jail definitions");
		registerPhasePermission("commands.jail", false, "Jail players", "Allows sentencing eligible players");
		registerPhasePermission("commands.unjail", false, "Release jailed players", "Allows releasing jailed players");
		registerPhasePermission("commands.jailedplayers", false, "List jailed players", "Allows listing active jail sentences");
		registerPhasePermission("commands.togglejail", false, "Toggle jail sentences", "Allows compatibility jail toggling");
		registerPhasePermission("exempt.jail", false, "Jail exemption", "Prevents ordinary jail targeting");
		registerPhasePermission("exempt.warn", false, "Warning exemption", "Prevents ordinary warning targeting");
		registerPhasePermission("exempt.mute", false, "Mute exemption", "Prevents ordinary mute targeting");
		registerPhasePermission("exempt.freeze", false, "Freeze exemption", "Prevents ordinary freeze targeting");
		registerPhasePermission("exempt.invlock", false, "Inventory lock exemption", "Prevents ordinary inventory lock targeting");
		registerPhasePermission("exempt.disablebuilding", false, "Building restriction exemption", "Prevents ordinary building restriction targeting");

		registerPhasePermission("commands.commandspy", false, "Command spy", "Allows requesting redacted command observation");
		registerPhasePermission("commands.commandspy.status", false, "Command spy status", "Allows inspecting command observation state");
		registerPhasePermission("commands.commandspy.recent", false, "Recent command events", "Allows reading bounded authorized recent events");
		registerPhasePermission("commands.commandspy.everyone", false, "Observe everyone", "Allows observing all eligible player commands");
		registerPhasePermission("commands.commandspy.player", false, "Observe one player", "Allows selecting one eligible player");
		registerPhasePermission("commands.commandspy.selected", false, "Manage selected command actors", "Allows managing a bounded selected UUID set");
		registerPhasePermission("commands.commandspy.scope.player", false, "Observe player sources", "Allows player command source observation");
		registerPhasePermission("commands.commandspy.scope.nonplayer", false, "Observe non player sources", "Allows console and server source observation");
		registerPhasePermission("commands.commandspy.match", false, "Change actor relation", "Allows initiator, effective actor, or either matching");
		registerPhasePermission("commands.commandspy.filter", false, "Filter command observation", "Allows stable command root and action filters");
		registerPhasePermission("commands.commandspy.filter.source", false, "Filter command sources", "Allows source category filters");
		registerPhasePermission("commands.commandspy.filter.player", false, "Filter command players", "Allows selected player filter aliases");
		registerPhasePermission("commands.commandspy.filter.result", false, "Filter command results", "Allows lifecycle result filters");
		registerPhasePermission("commands.commandspy.filter.world", false, "Filter command worlds", "Allows dimension identifier filters");
		registerPhasePermission("commands.commandspy.filter.origin", false, "Filter command origins", "Allows execution origin filters");
		registerPhasePermission("commands.commandspy.location", false, "View command locations", "Allows authorized location projection");
		registerPhasePermission("commands.commandspy.results", false, "View command results", "Allows truthful result projection");
		registerPhasePermission("commands.commandspy.others", false, "Manage other command spies", "Allows managing another observer");
		registerPhasePermission("commandspy.view.metadata", false, "View command metadata", "Allows receiving redacted command observation metadata");
		registerPhasePermission("commandspy.view.arguments", false, "View safe command arguments", "Allows arguments classified safe by redaction policy");
		registerPhasePermission("commandspy.view.location", false, "View command locations", "Allows dimension and bounded block position fields");
		registerPhasePermission("commandspy.view.result", false, "View command results", "Allows command result and duration fields");
		registerPhasePermission("commandspy.view.exempt", false, "Override command spy exemption", "Allows sensitive observation of exempt actors");
		registerPhasePermission("commandspy.hierarchy.bypass", false, "Command spy hierarchy bypass", "Allows command observation across hierarchy");
		registerPhasePermission("commandspy.exempt", false, "Command spy exemption", "Prevents ordinary command observation");

		registerPhasePermission("commands.loggerspy", false, "Logger spy convenience root", "Allows the optional logger management root");
		registerPhasePermission("commands.logging.status", false, "Logging status", "Allows viewing optional logging state");
		registerPhasePermission("commands.logging.enable", false, "Enable logging", "Allows starting optional file logging");
		registerPhasePermission("commands.logging.disable", false, "Disable logging", "Allows stopping optional file logging");
		registerPhasePermission("commands.logging.stream.list", false, "List logging streams", "Allows listing optional logging streams");
		registerPhasePermission("commands.logging.stream.configure", false, "Configure logging streams", "Allows changing optional logging streams");
		registerPhasePermission("commands.logging.configure.commands", false, "Configure command logs", "Allows configuring command capture");
		registerPhasePermission("commands.logging.configure.connections", false, "Configure connection logs", "Allows configuring connection capture");
		registerPhasePermission("commands.logging.rotate", false, "Rotate logs", "Allows rotating owned log streams");
		registerPhasePermission("commands.logging.flush", false, "Flush logs", "Allows flushing the optional writer");
		registerPhasePermission("commands.logging.stats", false, "Logging statistics", "Allows viewing writer and queue statistics");
		registerPhasePermission("commands.logging.doctor", false, "Logging doctor", "Allows viewing optional logger diagnostics");
		registerPhasePermission("commands.logging.live", false, "Logging live view", "Allows mapping logger live mode to command spy");
		registerPhasePermission("commands.logging.recent", false, "Recent logs", "Allows bounded recent structured records");
		registerPhasePermission("commands.logging.session.current", false, "Current log session", "Allows current session inspection");
		registerPhasePermission("commands.logging.session.list", false, "List log sessions", "Allows bounded session listing");
		registerPhasePermission("commands.logging.filter.list", false, "List log filters", "Allows viewing typed capture and view filters");
		registerPhasePermission("commands.logging.filter.capture", false, "Change capture filters", "Allows changing typed capture filters");
		registerPhasePermission("commands.logging.filter.view", false, "Change view filters", "Allows changing typed personal view filters");
		registerPhasePermission("commands.logging.filter.root", false, "Filter log roots", "Allows stable root filters");
		registerPhasePermission("commands.logging.filter.action", false, "Filter log actions", "Allows stable action filters");
		registerPhasePermission("commands.logging.filter.source", false, "Filter log sources", "Allows source category filters");
		registerPhasePermission("commands.logging.filter.player", false, "Filter log players", "Allows UUID filters");
		registerPhasePermission("commands.logging.filter.result", false, "Filter log results", "Allows lifecycle result filters");
		registerPhasePermission("commands.logging.filter.world", false, "Filter log worlds", "Allows world identifier filters");
		registerPhasePermission("commands.logging.filter.origin", false, "Filter log origins", "Allows origin filters");
		registerPhasePermission("commands.logging.format.show", false, "Show log formats", "Allows viewing typed text mirror formats");
		registerPhasePermission("commands.logging.format.validate", false, "Validate log formats", "Allows validating typed text formats");
		registerPhasePermission("commands.logging.format.set", false, "Set log formats", "Allows changing typed text formats");
		registerPhasePermission("commands.logging.format.reset", false, "Reset log formats", "Allows resetting typed text formats");
		registerPhasePermission("commands.logging.tail", false, "Tail logs", "Allows bounded redacted log tail");
		registerPhasePermission("commands.logging.search", false, "Search logs", "Allows typed bounded log searches");
		registerPhasePermission("commands.logging.export", false, "Export logs", "Allows bounded redacted log export");
		registerPhasePermission("commands.logging.retention.preview", false, "Preview log retention", "Allows previewing owned archive cleanup");
		registerPhasePermission("commands.logging.retention.run", false, "Run log retention", "Allows deleting eligible owned archives");
		registerPhasePermission("commands.logging.repair", false, "Acknowledge log repair", "Allows acknowledging incomplete logger state");
		registerPhasePermission("logging.view.command.arguments", false, "View logged command arguments", "Allows safe argument fields in log queries");
		registerPhasePermission("logging.view.command.location", false, "View logged command locations", "Allows location fields in log queries");
		registerPhasePermission("logging.view.moderation", false, "View moderation logs", "Allows moderation metadata queries");
		registerPhasePermission("logging.view.failures", false, "View logger failures", "Allows failure detail diagnostics");
		registerPhasePermission("logging.exempt.live", false, "Live logging exemption", "Prevents ordinary live observation");
		registerPhasePermission("banned.hierarchy.bypass", false, "Banned item hierarchy bypass", "Allows banned item actions across target hierarchy");
		registerPhasePermission("banned.bypass.exempt", false, "Banned item exemption bypass", "Allows banned item actions against exempt players");
		registerPhasePermission("banned.exempt", false, "Banned item exemption", "Prevents ordinary targeted banned item actions");
		registerPhasePermission("checkalts.hierarchy.bypass", false, "Alternate account hierarchy bypass", "Allows alternate account inspection across target hierarchy");
		registerPhasePermission("checkalts.bypass.exempt", false, "Alternate account exemption bypass", "Allows alternate account inspection of exempt players");
		registerPhasePermission("checkalts.exempt", false, "Alternate account exemption", "Prevents ordinary alternate account inspection");

		registerPhasePermission("commands.cartographytable", true, "Cartography table", "Allows the virtual cartography table");
		registerPhasePermission("commands.grindstone", true, "Grindstone", "Allows the virtual grindstone");
		registerPhasePermission("commands.loom", true, "Loom", "Allows the virtual loom");
		registerPhasePermission("commands.smithingtable", true, "Smithing table", "Allows the virtual smithing table");
		registerPhasePermission("commands.stonecutter", true, "Stonecutter", "Allows the virtual stonecutter");
		registerPhasePermission("commands.workbench", true, "Workbench", "Allows the workbench alias");
		registerPhasePermission("commands.workstation.cooldown.bypass", false, "Bypass workstation cooldowns", "Allows bypassing additional workstation cooldowns");
		registerPhasePermission("commands.superenchantingtable.unsafe", false, "Unsafe super enchanting", "Allows configured incompatible item and enchantment combinations");
		registerPhasePermission("commands.kit", true, "Use kits", "Allows claiming accessible kits");
		registerPhasePermission("commands.kits", true, "List kits", "Allows listing accessible kits");
		registerPhasePermission("commands.showkit", true, "Preview kits", "Allows previewing accessible kit contents");
		registerPhasePermission("commands.createkit", false, "Create kits", "Allows creating bounded kits from inventory");
		registerPhasePermission("commands.delkit", false, "Delete kits", "Allows deleting kit definitions");
		registerPhasePermission("commands.kitreset", false, "Reset kit use", "Allows resetting another player's kit use state");
		registerPhasePermission("commands.kit.edit", false, "Edit kits", "Allows safe typed kit editing");
		registerPhasePermission("commands.kit.export", false, "Export kits", "Allows bounded kit export metadata");
		registerPhasePermission("commands.kit.validate", false, "Validate kits", "Allows kit repository validation");
		registerPhasePermission("commands.clearinventory", true, "Clear inventory", "Allows clearing the executing player's inventory");
		registerPhasePermission("commands.clearinventory.others", false, "Clear other inventories", "Allows clearing an eligible player's inventory");
		registerPhasePermission("commands.enderchest", true, "Open ender chest", "Allows opening the executing player's ender chest");
		phaseSixSevenNodes.put("sef.commands.enderchest.others", enderChestOthers);
		registerPhasePermission("commands.disposal", true, "Disposal", "Allows opening a transient disposal menu");
		registerPhasePermission("commands.more", false, "Fill held stack", "Allows filling the held stack to its maximum");
		registerPhasePermission("commands.condense", true, "Condense inventory", "Allows recipe based inventory condensation");
		registerPhasePermission("commands.hat", true, "Wear held item", "Allows swapping the held item with the head slot");
		registerPhasePermission("commands.itemname", false, "Name held items", "Allows bounded held item custom names");
		registerPhasePermission("commands.itemlore", false, "Edit item lore", "Allows bounded held item lore edits");
		registerPhasePermission("commands.book", false, "Edit books", "Allows bounded safe book editing");
		registerPhasePermission("commands.recipe", true, "View recipes", "Allows recipe lookup");
		registerPhasePermission("commands.itemdb", true, "Identify items", "Allows safe held item identification");
		registerPhasePermission("commands.item.give.self", false, "Give self items", "Allows the bounded self only item shortcut");
		registerPhasePermission("commands.item.give.others", false, "Give other players items", "Allows bounded item grants to eligible players");
		registerPhasePermission("exempt.inventory", false, "Inventory mutation exemption", "Prevents ordinary administrative inventory mutations");
		registerPhasePermission("inventory.hierarchy.bypass", false, "Inventory hierarchy bypass", "Allows inventory actions across target hierarchy");
		registerPhasePermission("inventory.bypass.exempt", false, "Inventory exemption bypass", "Allows inventory actions against exempt players");
		registerPhasePermission("inventory.cooldown.bypass", false, "Inventory cooldown bypass", "Allows bypassing inventory utility cooldowns");

		for (String utility : new String[]{"feed", "heal", "fly", "god", "speed", "exp", "ptime", "pweather", "rest"}) {
			registerPhasePermission("commands." + utility, false, utility + " self", "Allows the " + utility + " command for self");
			registerPhasePermission("commands." + utility + ".others", false, utility + " others", "Allows the " + utility + " command for eligible players");
		}
		for (String utility : new String[]{"afk", "suicide", "near", "getpos", "compass", "depth", "top", "bottom", "jump"}) {
			registerPhasePermission("commands." + utility, utility.equals("getpos") || utility.equals("compass") || utility.equals("depth"),
					utility + " command", "Allows the " + utility + " player utility command");
		}
		registerPhasePermission("commands.getpos.others", false, "View other positions", "Allows viewing an eligible player's position");
		registerPhasePermission("utilities.hierarchy.bypass", false, "Utility hierarchy bypass", "Allows player utility mutations across hierarchy");
		registerPhasePermission("utilities.bypass.exempt", false, "Utility exemption bypass", "Allows targeting utility exempt players");
		registerPhasePermission("exempt.utility", false, "Player utility exemption", "Prevents ordinary player utility targeting");
		registerPhasePermission("utilities.cooldown.bypass", false, "Utility cooldown bypass", "Allows bypassing player utility cooldowns");

		for (String mode : new String[]{"creative", "survival", "spectator", "adventure"}) {
			registerPhasePermission("commands.gamemode." + mode, false, mode + " mode", "Allows changing self to " + mode);
			registerPhasePermission("commands.gamemode." + mode + ".others", false, mode + " mode others", "Allows changing an eligible player to " + mode);
		}
		registerPhasePermission("commands.gamemode", false, "Gamemode command", "Allows the parsed gamemode command");
		registerPhasePermission("commands.gamemode.others", false, "Gamemode others", "Allows parsed gamemode changes for eligible players");
		registerPhasePermission("exempt.gamemode", false, "Gamemode exemption", "Prevents ordinary administrative gamemode changes");
		registerPhasePermission("gamemode.cooldown.bypass", false, "Gamemode cooldown bypass", "Allows bypassing gamemode shortcut cooldowns");
		registerPhasePermission("kits.cooldown.bypass", false, "Kit cooldown bypass", "Allows bypassing command policy cooldowns for kit actions");
		registerPhasePermission("kits.hierarchy.bypass", false, "Kit hierarchy bypass", "Allows kit administration across target hierarchy");
		registerPhasePermission("kits.bypass.exempt", false, "Kit exemption bypass", "Allows kit administration against exempt players");
		registerPhasePermission("kits.exempt", false, "Kit administration exemption", "Prevents ordinary targeted kit administration");

		registerPhasePermission("commands.balance", true, "View balance", "Allows viewing the executing account balance");
		registerPhasePermission("commands.balance.others", false, "View other balances", "Allows viewing an eligible account balance");
		registerPhasePermission("commands.pay", true, "Pay players", "Allows transferring funds to an eligible account");
		registerPhasePermission("commands.paytoggle", true, "Toggle payments", "Allows changing incoming payment preference");
		registerPhasePermission("commands.payconfirmtoggle", true, "Toggle payment confirmation", "Allows changing large payment confirmation preference");
		registerPhasePermission("commands.balancetop", true, "View balance top", "Allows viewing the cached balance ranking");
		registerPhasePermission("commands.worth", true, "View item worth", "Allows inspecting configured item worth");
		registerPhasePermission("commands.sell", true, "Sell items", "Allows atomic item sales to the active economy");
		registerPhasePermission("commands.eco.give", false, "Give economy funds", "Allows administrative deposits");
		registerPhasePermission("commands.eco.take", false, "Take economy funds", "Allows administrative withdrawals");
		registerPhasePermission("commands.eco.set", false, "Set economy balances", "Allows administrative balance replacement");
		registerPhasePermission("commands.eco.reset", false, "Reset economy balances", "Allows administrative balance reset");
		registerPhasePermission("commands.eco.freeze", false, "Freeze economy accounts", "Allows freezing eligible accounts");
		registerPhasePermission("commands.eco.unfreeze", false, "Unfreeze economy accounts", "Allows unfreezing eligible accounts");
		registerPhasePermission("commands.eco.history", false, "View economy history", "Allows viewing eligible account ledger history");
		registerPhasePermission("commands.eco.import", false, "Import economy accounts", "Allows previewing and confirming import once operations");
		registerPhasePermission("commands.setworth", false, "Set item worth", "Allows changing server defined item worth");
		registerPhasePermission("economy.hierarchy.bypass", false, "Economy hierarchy bypass", "Allows economy administration across target hierarchy");
		registerPhasePermission("economy.bypass.exempt", false, "Economy exemption bypass", "Allows economy administration against exempt accounts");
		registerPhasePermission("economy.exempt", false, "Economy administration exemption", "Prevents ordinary targeted economy administration");
		registerPhasePermission("economy.pay.bypass.toggle", false, "Bypass payment toggle", "Allows paying accounts that disabled incoming payments");
		registerPhasePermission("economy.pay.bypass.ignore", false, "Bypass payment ignore", "Allows paying accounts that ignore the sender");
		registerPhasePermission("economy.pay.offline", true, "Pay offline accounts", "Allows paying unambiguous known offline identities when configured");
		registerPhasePermission("economy.cost.bypass", false, "Bypass command costs", "Allows explicit command cost bypass");
		registerPhasePermission("economy.sign.manage", false, "Manage economy signs", "Allows inspecting and removing registered economy signs");
		registerPhasePermission("economy.sign.bypass.owner", false, "Bypass economy sign ownership", "Allows editing another creator's economy signs");
		for (String sign : new String[]{"balance", "buy", "sell", "trade", "free", "disposal", "kit", "heal", "repair", "time", "weather", "warp"}) {
			registerPhasePermission("economy.sign." + sign + ".use", true, "Use " + sign + " signs", "Allows using enabled " + sign + " economy signs");
			registerPhasePermission("economy.sign." + sign + ".create", false, "Create " + sign + " signs", "Allows creating enabled " + sign + " economy signs");
		}

		for (String action : new String[]{"list", "inspect", "create", "validate", "publish", "disable", "rollback", "delete", "run", "help"}) {
			registerPhasePermission("commands.alias." + action, false, "Alias " + action, "Allows the reviewed alias " + action + " lifecycle action");
		}
		for (String action : new String[]{"list", "inspect", "create", "edit", "preview", "publish", "run", "cancel", "recover", "disable", "rollback", "delete"}) {
			registerPhasePermission("commands.bundle." + action, false, "Bundle " + action, "Allows the reviewed bundle " + action + " lifecycle action");
		}
		for (String action : new String[]{"list", "inspect", "create", "validate", "test", "publish", "reference", "enable", "execute", "rollback", "delete"}) {
			registerPhasePermission("commands.profile." + action, false, "Command profile " + action, "Allows the reviewed command profile " + action + " lifecycle action");
		}
		registerPhasePermission("commands.profile.server", false, "Server command profiles", "Allows creating and enabling reviewed server source command profiles");
		registerPhasePermission("commands.profile.targeted", false, "Targeted actor command profiles", "Allows creating and enabling strict participant command profiles");
		registerPhasePermission("commands.fakejoin", false, "Fake join messages", "Allows audited unsigned fake join presentation");
		registerPhasePermission("commands.fakeleave", false, "Fake leave messages", "Allows audited unsigned fake leave presentation");
		registerPhasePermission("commands.fakemessage", false, "Fake chat messages", "Allows audited unsigned fake chat presentation");
		registerPhasePermission("commands.fakerankmessage", false, "Fake rank messages", "Allows audited unsigned synthetic rank chat presentation");
		registerPhasePermission("commands.fake.profile", false, "Fake identity profiles", "Allows managing bounded fake identity profiles");
		registerPhasePermission("commands.fake.scene", false, "Fake identity scenes", "Allows managing bounded fake message scenes");
		registerPhasePermission("commands.fake.schedule", false, "Fake identity schedules", "Allows scheduling bounded fake message scenes");
		registerPhasePermission("commands.sudo.run", false, "Sudo run", "Allows forcing an eligible consenting online player command source");
		registerPhasePermission("commands.sudo.chat", false, "Sudo chat", "Allows emitting audited unsigned system chat styled as an eligible online player");
		registerPhasePermission("commands.sudo.dryrun", false, "Sudo dry run", "Allows previewing sudo policy without execution");
		registerPhasePermission("commands.sudo.consent", true, "Sudo consent", "Allows controlling personal targeted actor consent");
		registerPhasePermission("commands.sudo.lock", false, "Sudo locks", "Allows locking eligible players from sudo execution");
		registerPhasePermission("commands.sudo.policy", false, "Sudo policy", "Allows inspecting targeted actor and delegated sudo policy");
		registerPhasePermission("commands.sudo.bypass.consent", false, "Bypass sudo consent", "Allows reviewed sudo execution without target consent");
		registerPhasePermission("commands.sudo.bypass.lock", false, "Bypass sudo lock", "Allows reviewed sudo execution against a locked target");
		registerPhasePermission("commands.sudo.ignore_permissions", false, "Delegated sudo permission mode", "Allows requesting an admitted bounded permission override");
		registerPhasePermission("commands.sudo.delegate", false, "One execution sudo delegation", "Allows one exact confirmed sudo command to use an in memory delegated permission scope");
		registerPhasePermission("commands.sudo.delegate.preview", false, "Delegated sudo preview", "Allows previewing a bounded delegated command");
		registerPhasePermission("commands.sudo.delegate.confirm", false, "Delegated sudo confirmation", "Allows confirming an exact delegated command");
		registerPhasePermission("commands.sudo.delegate.self", false, "Delegated sudo self target", "Allows configured delegated execution against the issuer");
		registerPhasePermission("commands.sudo.delegate.root.effect", false, "Delegated effect root", "Allows the effect root through a published delegation profile");
		registerPhasePermission("commands.sudo.delegate.profile.effect", false, "Delegated effect profile", "Allows the published effect delegation profile");
		registerPhasePermission("sudo.delegate.exempt", false, "Delegated sudo exemption", "Prevents delegated sudo from targeting this player");
		registerPhasePermission("sudo.hierarchy.bypass", false, "Sudo hierarchy bypass", "Allows sudo execution across target hierarchy");
		phaseSixSevenNodes.put("sef.sudo.bypass.exempt", sudoBypassExempt);
		phaseSixSevenNodes.put("sef.sudo.exempt", sudoExempt);
		registerPhasePermission("commands.run", false, "Run server commands", "Allows reviewed direct server source command execution");
		registerPhasePermission("commands.run.root.any", false, "Run any approved root", "Allows any root that passes the configured run policy");
		registerPhasePermission("commands.silent.actor", false, "Silent actor commands", "Allows direct actor source output suppression");
		registerPhasePermission("commands.silent.server", false, "Silent server commands", "Allows reviewed direct server source output suppression");
		registerPhasePermission("commands.silent.unsuppressible", false, "Accept unsuppressible output", "Allows execution when independent command output cannot be suppressed");

			for (String permission : java.util.List.of(
				"commands.tags",
				"commands.tags.status",
				"commands.tags.list",
				"commands.tags.view",
				"commands.tags.create",
				"commands.tags.duplicate",
				"commands.tags.edit",
				"commands.tags.validate",
				"commands.tags.publish",
				"commands.tags.hide",
				"commands.tags.archive",
				"commands.tags.restore",
				"commands.tags.delete",
				"commands.tags.revision.list",
				"commands.tags.revision.view",
				"commands.tags.revision.restore",
				"commands.tags.delete.finalize",
				"commands.tags.assign.player",
				"commands.tags.assign.offline",
				"commands.tags.assign.group",
				"commands.tags.assign.team",
				"commands.tags.assign.default",
				"commands.tags.assign.bulk",
				"commands.tags.unassign",
				"commands.tags.assignments.player",
				"commands.tags.assignments.tag",
				"commands.tags.assignments.group",
				"commands.tags.report",
				"commands.tags.moderation.queue",
				"commands.tags.moderation.suspend",
				"commands.tags.moderation.clear",
				"commands.tags.category.list",
				"commands.tags.category.create",
				"commands.tags.category.edit",
				"commands.tags.category.delete",
				"commands.tags.palette.list",
				"commands.tags.palette.create",
				"commands.tags.palette.edit",
				"commands.tags.palette.delete",
				"commands.tags.template.list",
				"commands.tags.template.create",
				"commands.tags.template.edit",
				"commands.tags.template.delete",
				"commands.tags.import.scan",
				"commands.tags.import.inspect",
				"commands.tags.import.approve",
				"commands.tags.import.reject",
				"commands.tags.import.client",
				"commands.tags.import.clipboard",
				"commands.tags.import.url",
				"commands.tags.export.png",
				"commands.tags.export.project",
				"commands.tags.export.manifest",
				"commands.tags.lease.view",
				"commands.tags.lease.acquire",
				"commands.tags.lease.renew",
				"commands.tags.lease.override",
				"commands.tags.integrity.check",
				"commands.tags.integrity.repair",
				"commands.tags.cache.status",
				"commands.tags.cache.invalidate",
				"commands.tags.transfer.status",
				"commands.tags.audit",
				"commands.tags.backup.preview",
				"commands.tags.backup.create",
				"commands.tags.backup.list",
				"commands.tags.backup.restore",
				"commands.tags.gc.preview",
				"commands.tags.gc.run",
				"commands.tags.reload",
				"commands.tags.doctor",
				"commands.cooldown.explain",
				"commands.cooldown.keys",
				"commands.enchant",
				"commands.enchant.self",
				"commands.enchant.others",
				"commands.enchant.bulk",
				"commands.enchant.unsafe_level",
				"commands.enchant.any_item",
				"commands.enchant.incompatible",
				"commands.enchant.remove",
				"commands.enchant.clear",
				"commands.enchant.hierarchy.override",
				"commands.enchant.exempt",
				"commands.enchant.exemption.override",
				"tags.manage.open",
				"tags.render.receive",
				"tags.local.overlay.connected",
				"tags.render.chat",
				"tags.render.nameplate",
				"tags.render.tab",
				"tags.render.hud",
				"tags.render.tooltip",
				"tags.view.draft",
				"tags.view.hidden",
				"tags.view.archived",
				"tags.view.creator",
				"tags.view.assignments",
				"tags.view.audit",
				"tags.view.storage",
				"tags.view.hash",
				"tags.assign.hierarchy.override",
				"tags.assign.exempt",
				"tags.assign.exemption.override",
				"tags.assign.vanished",
				"tags.assign.multiple",
				"tags.assign.all",
				"tags.limits.bypass",
				"tags.locks.bypass",
				"tags.delete.force",
				"tags.local.overlay")) {
			boolean safe = java.util.Set.of(
					"commands.tags",
					"commands.tags.status",
					"commands.tags.list",
					"commands.tags.view",
					"tags.render.receive",
					"tags.render.chat",
					"tags.render.nameplate",
					"tags.render.tab",
					"tags.render.hud",
					"tags.render.tooltip").contains(permission);
			registerPhasePermission(
					permission,
					safe,
					"Fancy Tags " + permission,
					"Controls the separately gated Fancy Tags action " + permission);
		}
		for (String permission : java.util.List.of(
				"commands.disguise",
				"commands.disguise.mob",
				"commands.disguise.player",
				"commands.disguise.preset",
				"commands.disguise.clear",
				"commands.disguise.status",
				"commands.disguise.status.others",
				"commands.disguise.list",
				"commands.disguise.preview",
				"commands.disguise.set.others",
				"commands.disguise.clear.others",
				"commands.disguise.ability",
				"commands.disguise.ability.cooldown.bypass",
				"commands.disguise.options",
				"commands.disguise.inspect",
				"commands.disguise.conflicts",
				"commands.disguise.preset.manage",
				"disguise.protected_identity",
				"disguise.traits",
				"disguise.trait.fire_resistance",
				"disguise.trait.water_breathing",
				"disguise.trait.climbing",
				"disguise.trait.reduced_fall_damage",
				"disguise.trait.controlled_flight",
				"disguise.trait.swim_speed",
				"disguise.trait.water_vulnerability",
				"disguise.trait.daylight_sensitivity",
				"disguise.abilities",
				"disguise.hierarchy.bypass",
				"disguise.exempt",
				"disguise.exemption.bypass",
				"disguise.options.equipment",
				"disguise.options.name",
				"disguise.options.hitbox",
				"disguise.persist",
				"disguise.ability.blaze.fireball",
				"disguise.ability.blaze.hover",
				"disguise.ability.blaze.fire_resistance",
				"disguise.type.minecraft.blaze",
				"disguise.type.minecraft.snow_golem",
				"disguise.type.minecraft.enderman",
				"disguise.type.minecraft.spider",
				"disguise.type.minecraft.bee",
				"disguise.type.minecraft.creeper",
				"disguise.type.minecraft.ghast",
				"disguise.type.minecraft.dolphin",
				"disguise.type.minecraft.wolf",
				"disguise.type.minecraft.witch",
				"disguise.type.minecraft.zombie",
				"disguise.type.minecraft.skeleton",
				"disguise.type.minecraft.cow",
				"disguise.type.minecraft.pig",
				"disguise.type.minecraft.sheep",
				"disguise.type.minecraft.chicken",
				"disguise.type.minecraft.slime",
				"disguise.type.minecraft.bat",
				"disguise.type.minecraft.phantom")) {
			boolean safe = java.util.Set.of(
					"commands.disguise",
					"commands.disguise.status",
					"commands.disguise.list").contains(permission);
				registerPhasePermission(
						permission,
						safe,
						"Disguise " + permission,
						"Controls the separately gated disguise action " + permission);
			}

			registerPhasePermission(
					"commands.control",
					false,
					"Server control center",
					"Allows opening the server control command namespace");
			registerPhasePermission(
					"commands.control.catalog",
					false,
					"Server control catalog",
					"Allows viewing the permission filtered server control catalog");
			registerPhasePermission(
					"commands.control.status",
					false,
					"Server control status",
					"Allows viewing bounded server control repository diagnostics");
			for (ServerControlCatalog.FeatureDefinition feature : ServerControlCatalog.FEATURES) {
				String prefix = "commands.control." + feature.id();
				registerPhasePermission(
						prefix + ".view",
						feature.playerCreate() && !feature.sensitive(),
						feature.title() + " view",
						"Allows viewing authorized " + feature.title() + " records");
				registerPhasePermission(
						prefix + ".create",
						feature.playerCreate(),
						feature.title() + " create",
						"Allows creating bounded " + feature.title() + " records");
				registerPhasePermission(
						prefix + ".manage",
						false,
						feature.title() + " manage",
						"Allows updating and transitioning eligible " + feature.title() + " records");
				registerPhasePermission(
						prefix + ".others",
						false,
						feature.title() + " others",
						"Allows creating or viewing " + feature.title() + " records for other players");
				registerPhasePermission(
						prefix + ".sensitive",
						false,
						feature.title() + " sensitive fields",
						"Allows viewing sensitive fields in " + feature.title() + " records");
				registerPhasePermission(
						prefix + ".hud",
						feature.playerCreate() && !feature.sensitive(),
						feature.title() + " hud",
						"Allows viewing privacy filtered " + feature.title() + " active state indicators");
				registerPhasePermission(
						prefix + ".exempt",
						false,
						feature.title() + " exemption",
						"Prevents ordinary targeting by " + feature.title() + " controls");
				registerPhasePermission(
						prefix + ".exemption.override",
						false,
						feature.title() + " exemption override",
						"Allows targeting exempt players through " + feature.title() + " controls");
				registerPhasePermission(
						prefix + ".hierarchy.override",
						false,
						feature.title() + " hierarchy override",
						"Allows targeting across hierarchy through " + feature.title() + " controls");
			}
			for (String permission : java.util.List.of(
					"commands.accessgrant.profiles",
					"commands.accessgrant.profile.inspect",
					"commands.accessgrant.profile.publish",
					"commands.accessgrant.profile.retire",
					"commands.accessgrant.preview",
					"commands.accessgrant.create",
					"commands.accessgrant.renew",
					"commands.accessgrant.suspend",
					"commands.accessgrant.resume",
					"commands.accessgrant.revoke",
					"commands.accessgrant.list",
					"commands.accessgrant.inspect.self",
					"commands.accessgrant.inspect.others",
					"commands.accessgrant.expiring",
					"commands.accessgrant.reconcile",
					"commands.accessgrant.history",
					"commands.accessgrant.protected",
					"commands.accessgrant.hierarchy.override",
					"commands.accessgrant.exemption.override",
					"commands.accessgrant.exempt")) {
				registerPhasePermission(
						permission,
						false,
						"Access grant " + permission,
						"Controls the separately gated access grant action " + permission);
			}
			for (String permission : java.util.List.of(
					"commands.approval.request",
					"commands.approval.approve",
					"commands.approval.revoke",
					"commands.approval.revoke.others",
					"commands.approval.inspect",
					"commands.approval.list",
					"commands.approval.history")) {
				registerPhasePermission(
						permission,
						false,
						"Approval " + permission,
						"Controls the separately gated two person approval action " + permission);
			}
			for (String permission : java.util.List.of(
					"commands.adminlock.status.self",
					"commands.adminlock.status.others",
					"commands.adminlock.lock",
					"commands.adminlock.unlock",
					"commands.adminlock.challenge",
					"commands.adminlock.session.open",
					"commands.adminlock.session.close",
					"commands.adminlock.require",
					"commands.adminlock.release",
					"commands.adminlock.invalidate",
					"commands.adminlock.breakglass.status",
					"commands.adminlock.breakglass.open",
					"commands.adminlock.breakglass.close",
					"commands.adminlock.breakglass.profile",
					"commands.adminlock.history.self",
					"commands.adminlock.history.others",
					"commands.adminlock.hierarchy.override",
					"commands.adminlock.exemption.override",
					"commands.adminlock.exempt")) {
				registerPhasePermission(
						permission,
						false,
						"Administrative lock " + permission,
						"Controls the separately gated administrative lock action " + permission);
			}
			for (String permission : java.util.List.of(
					"commands.config.modules",
					"commands.config.status",
					"commands.config.inspect",
					"commands.config.diff",
					"commands.config.validate",
					"commands.config.reload",
					"commands.config.history",
					"commands.config.rollback",
					"commands.config.explain",
					"commands.config.edit",
					"commands.config.migrate",
					"commands.config.documentation",
					"commands.config.sensitive",
					"commands.config.providers",
					"commands.guis.status",
					"commands.guis.enable",
					"commands.guis.disable",
					"commands.guis.auto",
					"commands.guis.module",
					"commands.guis.action",
					"commands.guis.sessions",
					"commands.guis.close",
					"commands.guis.reload",
					"commands.guis.doctor",
					"commands.guis.explain",
					"commands.guis.coverage",
					"commands.gui.preference",
					"gui.use",
					"gui.open",
					"gui.search",
					"gui.preview",
					"gui.confirm")) {
				boolean safe = java.util.Set.of(
						"commands.gui.preference",
						"gui.use",
						"gui.open",
						"gui.search",
						"gui.preview",
						"gui.confirm").contains(permission);
				registerPhasePermission(
						permission,
						safe,
						"Modular configuration " + permission,
						"Controls the separately gated modular configuration or GUI policy action " + permission);
			}

			quotaTierNodes.put("sef.homes.3", ezyPermission("homes.3", false, "Three homes", "Sets the finite home quota tier to three"));
		quotaTierNodes.put("sef.homes.5", ezyPermission("homes.5", false, "Five homes", "Sets the finite home quota tier to five"));
		quotaTierNodes.put("sef.homes.10", ezyPermission("homes.10", false, "Ten homes", "Sets the finite home quota tier to ten"));
		quotaTierNodes.put("sef.playerwarps.10", ezyPermission("playerwarps.10", false, "Ten player warps", "Sets the finite player warp quota tier to ten"));
		quotaTierNodes.put("sef.playerwarps.25", ezyPermission("playerwarps.25", false, "Twenty five player warps", "Sets the finite player warp quota tier to twenty five"));
		quotaTierNodes.put("sef.targets.10", ezyPermission("targets.10", false, "Ten targets", "Sets the finite command target quota tier to ten"));
		quotaTierNodes.put("sef.targets.100", ezyPermission("targets.100", false, "One hundred targets", "Sets the finite command target quota tier to one hundred"));
		quotaTierNodes.put("sef.mail.500", ezyPermission("mail.500", false, "Five hundred mail records", "Sets the finite mail quota tier to five hundred"));
		quotaTierNodes.put("sef.mail.1000", ezyPermission("mail.1000", false, "One thousand mail records", "Sets the finite mail quota tier to one thousand"));
		quotaTierNodes.put("sef.definitions.256", ezyPermission("definitions.256", false, "Two hundred fifty six definitions", "Sets the finite definition quota tier to two hundred fifty six"));
		quotaTierNodes.put("sef.definitions.512", ezyPermission("definitions.512", false, "Five hundred twelve definitions", "Sets the finite definition quota tier to five hundred twelve"));
        hexChatNode = ezyPermission("chat.colors.hex", true, "Chat hex colors", "Allows usage of hex colors and gradients in chat");
        for(char c : "0123456789abcdef".toCharArray()) {
            perColorChatNodes.put(c, ezyPermission("chat.colors." + c, true, "Chat color &" + c, "Allows usage of &" + c + " in chat"));
        }
		for (int level = 1; level <= 3; level++) {
			Vanishmod.VANISH_SEE_NODES.put(level, ezyPermission(
					"vanishsee." + level,
					false,
					"See vanish level " + level,
					"Allows seeing players vanished at level " + level));
			Vanishmod.VANISH_LEVEL_NODES.put(level, ezyPermission(
					"vanish." + level,
					false,
					"Use vanish level " + level,
					"Allows vanishing at level " + level));
		}
    }

	@SubscribeEvent public static void registerPermissionNodes(Nodes pge) {
		for (PermissionManifest.Definition definition : PermissionManifest.definitions())
			pge.addNodes(definition.node());
	}

	private static PermissionNode<Boolean> ezyPermission(String id, boolean defVal, String name, String desc) {
		return PermissionManifest.register(id, defVal, name, desc);
	}

	private static void registerPhasePermission(String id, boolean defaultValue, String name, String description) {
		phaseSixSevenNodes.put("sef." + id, ezyPermission(id, defaultValue, name, description));
	}

	public static PermissionNode<Boolean> phasePermission(String id) {
		String normalized = id.startsWith("sef.") ? id : "sef." + id;
		return phaseSixSevenNodes.get(normalized);
	}

	public static boolean playerHasPermission(UUID uuid, PermissionNode<Boolean> node) {
		return PermissionService.has(uuid, node);
	}
	public static boolean playerHasColorPermission(UUID uuid, char code) {
        PermissionNode<Boolean> node = perColorChatNodes.get(Character.toLowerCase(code));
        if(node == null) return false;
        return playerHasPermission(uuid, node);
    }
}
