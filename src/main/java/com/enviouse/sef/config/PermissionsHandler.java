package com.enviouse.sef.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.enviouse.sef.ServerEssentialsForge;
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
	public static PermissionNode<Boolean> kernelBypass =
			ezyPermission("kernel.bypass.use", false, "Kernel bypass capability", "Allows specifically declared policy bypasses without bypassing canonical action permission");
	public static PermissionNode<Boolean> kernelSensitiveData =
			ezyPermission("kernel.sensitive.view", false, "Kernel sensitive data capability", "Allows specifically declared sensitive fields after per field authorization");
	public static final Map<String, PermissionNode<Boolean>> quotaTierNodes = new LinkedHashMap<>();

	// Color Permissions
	public static final Map<Character, PermissionNode<Boolean>> perColorChatNodes = new LinkedHashMap<>();
	public static PermissionNode<Boolean> hexChatNode;

	static {
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

	public static boolean playerHasPermission(UUID uuid, PermissionNode<Boolean> node) {
		return PermissionService.has(uuid, node);
	}
	public static boolean playerHasColorPermission(UUID uuid, char code) {
        PermissionNode<Boolean> node = perColorChatNodes.get(Character.toLowerCase(code));
        if(node == null) return false;
        return playerHasPermission(uuid, node);
    }
}
