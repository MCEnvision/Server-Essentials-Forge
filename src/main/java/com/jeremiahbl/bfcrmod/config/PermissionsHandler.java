package com.jeremiahbl.bfcrmod.config;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.jeremiahbl.bfcrmod.BetterForgeChat;
import com.jeremiahbl.bfcrmod.TextFormatter;

import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.events.PermissionGatherEvent.Nodes;
import net.minecraftforge.server.permission.nodes.PermissionDynamicContext;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;

@EventBusSubscriber
public class PermissionsHandler {	
	public static PermissionNode<Boolean> coloredChatNode = 
			ezyPermission("chat.colors", true, "Chat colors", "Enables/Disables colors in chat");
	public static PermissionNode<Boolean> styledChatNode = 
			ezyPermission("chat.styles", true, "Chat styles", "Enables/Disables styles in chat");
	public static PermissionNode<Boolean> markdownChatNode = 
			ezyPermission("chat.styles.md", true, "Chat markdown styling", "Enables/Disables markdown styling in chat");
	public static PermissionNode<Boolean> tabListNicknameNode = 
			ezyPermission("tablist.nickname", true, "Tab list nicknames", "Enables/Disables nicknames showing in the tab list");
	public static PermissionNode<Boolean> tabListMetadataNode = 
			ezyPermission("tablist.metadata", true, "Tab list metadata", "Enables/Disables prefixes&suffixes showing in the tab list");
	
	public static PermissionNode<Boolean> colorsCommand =
			ezyPermission("commands.colors", true, "Colors command", "Enables/Disables the \"/colors\" command");
	public static PermissionNode<Boolean> bfcrmodCommand = 
			ezyPermission("commands.bfcr.allowed", true, "BetterForgeChat command", "Enables/Disables the \"/bfc\" command");
	public static PermissionNode<Boolean> bfcrmodCommandColorsSubCommand = 
			ezyPermission("commands.bfcr.colors", true, "BetterForgeChat colors sub-command", "Enables/Disables the \"/bfc colors\" sub-command");
	public static PermissionNode<Boolean> bfcrmodCommandInfoSubCommand = 
			ezyPermission("commands.bfcr.info", true, "BetterForgeChat info sub-command", "Enables/Disables the \"/bfc info\" sub-command");
	public static PermissionNode<Boolean> bfcrmodCommandReloadSubCommand =
			ezyPermission("commands.bfcr.reload", true, "BetterForgeChat reload sub-command", "Enables/Disables the \"/bfc reload\" sub-command");
	public static PermissionNode<Boolean> msgCommand =
			ezyPermission("commands.msg", true, "Private messages", "Allows using /msg, /r and aliases");

	public static PermissionNode<Boolean> whoisCommand = 
			ezyPermission("commands.whois", true, "Nickname", "Enables/Disables the \"/whois <nickname>\" command");
	public static PermissionNode<Boolean> nickCommand = 
			ezyPermission("commands.nick", true, "Nickname", "Enables/Disables the \"/nick <nickname>\" command");
	public static PermissionNode<Boolean> nickOthersCommand = 
			ezyPermission("commands.nick.others", true, "Modify nicknames", "Enables/Disables the \"/nick <username> <nickname>\" command");
	
	public static final Map<Character, PermissionNode<Boolean>> perColorChatNodes = new HashMap<>();
	public static PermissionNode<Boolean> hexChatNode =
			ezyPermission("chat.colors.hex", false, "Chat hex colors", "Allows usage of hex colors and gradients in chat");
	public static PermissionNode<Boolean> signColorNode =
			ezyPermission("sign.colors", false, "Sign colors", "Allows usage of colors on signs");

	static {
        // Allow hex by default? set to true for convenience
        hexChatNode = ezyPermission("chat.colors.hex", true, "Chat hex colors", "Allows usage of hex colors and gradients in chat");
        // Per-color nodes (&0 - &9, &a - &f)
        for(char c : "0123456789abcdef".toCharArray()) {
            perColorChatNodes.put(c, ezyPermission("chat.colors." + c, true, "Chat color &" + c, "Allows usage of &" + c + " in chat"));
        }
    }

	@SubscribeEvent public void registerPermissionNodes(Nodes pge) {
		for(Field fld : PermissionsHandler.class.getDeclaredFields()) {
			if(fld.getType() == PermissionNode.class) {
				try { // Fuck adding all these nodes manually
					pge.addNodes((PermissionNode<?>) fld.get(PermissionNode.class));
				} catch (Exception error) {
                    BetterForgeChat.LOGGER.trace("Exception: Caught on adding permission nodes", error);
				}
			}
		}
		// register per-color nodes
        for(PermissionNode<Boolean> node : perColorChatNodes.values()) {
            pge.addNodes(node);
        }
    }

	private static PermissionNode<Boolean> ezyPermission(String id, boolean defVal, String name, String desc) {
		PermissionNode<Boolean> node = new PermissionNode<>(BetterForgeChat.MODID, id, 
				PermissionTypes.BOOLEAN, (player, uuid, context) -> defVal);
		node.setInformation(Component.literal(name),TextFormatter.stringToFormattedText(desc));
		return node;
	}

	public static boolean playerHasPermission(UUID uuid, PermissionNode<Boolean> node) {
		boolean bool = false;
		try {
			bool = PermissionAPI.getOfflinePermission(uuid, node, new PermissionDynamicContext[0]);
			//bool = PermissionAPI.getPermission(player, node, new PermissionDynamicContext[0]);
		} catch(IllegalStateException ise) {
			BetterForgeChat.LOGGER.trace("IllegalStateException when getting player tab list permissions, assuming false",ise);
		}
		return bool;
	}
	public static boolean playerHasColorPermission(UUID uuid, char code) {
        PermissionNode<Boolean> node = perColorChatNodes.get(Character.toLowerCase(code));
        if(node == null) return false;
        return playerHasPermission(uuid, node);
    }
}
