# Virtual Workstations

## Purpose

Virtual workstations let a player open common utility screens without placing the matching block. The feature is designed for a NeoForge 1.21.1 server. Players can connect without installing SEFPORTED on their client because every screen uses a vanilla Minecraft menu type.

The `/craft` command and its `/c` alias open a crafting table.

The `/anvil` command and its `/av` alias open an anvil.

The `/enchantingtable` command and its `/et` alias open a vanilla enchanting table.

The `/superenchantingtable` command and its `/set` alias open the super enchanting table for the held item.

The `/repair` command fully repairs the held item.

## Prerequisites

The server must run NeoForge 1.21.1 and SEFPORTED. LuckPerms is optional. When LuckPerms is installed and enabled through the existing `useLuckPerms` setting, LuckPerms can assign every permission listed in this guide.

## Configuration

Start the server once after installing the updated mod. Open `config/sef/common.toml` and find `[ServerEssentialsForgeModConfig.modules]`.

The following values control whether each command group is registered.

```toml
crafting_table = true
anvil = true
enchanting_table = true
super_enchanting_table = true
repair = true
```

Changing one of these values requires a server restart because Minecraft builds its command tree while the server starts.

Find `[ServerEssentialsForgeModConfig.virtualWorkstations]` to configure aliases, cooldowns, messages, and super enchanting behavior. A cooldown value of `0` disables that cooldown. Cooldowns are measured in seconds and are tracked separately for each player and command. An alias and its full command share the same cooldown.

The super enchanting maximum accepts values from `1` through `255`. The default is `10`. Minecraft stores enchantment levels up to `255`.

When `superEnchantingAllowUnsafe` is `false`, the menu only permits enchantments that support the held item and do not conflict with its existing enchantments. Levels can still exceed the normal vanilla maximum. When the value is `true`, every registered enchantment is available and incompatible combinations are allowed.

## Permissions

Crafting, anvil, and vanilla enchanting access are allowed by default. Repair and super enchanting access are denied by default. Operators receive access and cooldown bypasses when LuckPerms is absent. When LuckPerms is installed, its permission values are authoritative, so assign the nodes your groups need.

```text
sef.commands.craft
sef.commands.anvil
sef.commands.enchantingtable
sef.commands.superenchantingtable
sef.commands.repair
```

Each cooldown has an independent bypass permission.

```text
sef.cooldowns.bypass.craft
sef.cooldowns.bypass.anvil
sef.cooldowns.bypass.enchantingtable
sef.cooldowns.bypass.superenchantingtable
sef.cooldowns.bypass.repair
```

The following example grants the default LuckPerms group access to crafting and grants the admin group access to super enchanting without a cooldown.

```text
/lp group default permission set sef.commands.craft true
/lp group admin permission set sef.commands.superenchantingtable true
/lp group admin permission set sef.cooldowns.bypass.superenchantingtable true
```

## Super Enchanting Table

Hold the target item in the main hand before running `/superenchantingtable` or `/set`. Hold only one book when enchanting a normal book. The first applied enchantment converts it into an enchanted book. The menu locks normal inventory clicks while it is open so the selected target cannot be moved accidentally.

Left click an enchanted book to add one level. Right click it to remove one level. Hold shift while clicking to set the enchantment to the configured maximum. Changes apply immediately and do not consume experience or lapis.

The arrow buttons change pages when more enchantments are available. The paper shows the current page and configured maximum. The item in the center of the control row is a display copy of the target.

## Verification

1. Restart the server after changing module or alias settings.

2. Join with a player that has all five command permissions.

3. Run `/craft` and `/c`. Confirm that both open a working three by three crafting grid.

4. Run `/anvil` and `/av`. Rename or combine an item and confirm that the normal experience cost is applied.

5. Stand near bookshelves, then run `/enchantingtable` and `/et`. Confirm that vanilla offers appear and consume experience and lapis normally.

6. Damage a tool, hold it in the main hand, and run `/repair`. Confirm that its durability is full.

7. Hold an enchantable item and run `/superenchantingtable`. Increase an enchantment beyond its vanilla maximum, close the menu, and confirm that the item retains the selected level.

8. Set a cooldown to a nonzero value, restart the server, and run the matching command twice. Confirm that the second attempt reports the remaining time.

9. Grant the matching cooldown bypass permission and repeat the command. Confirm that both attempts succeed.

10. Remove one command permission and reconnect. Confirm that the command is absent from suggestions and cannot be run.

## Common Problems

If a command is missing, confirm that its module value and alias value are enabled, then restart the server. Also confirm that the player has the matching permission.

If LuckPerms changes do not affect these commands, confirm that LuckPerms is loaded on NeoForge and that `useLuckPerms` remains enabled in `config/sef/common.toml`. Reconnect the player after changing permissions so the client receives a refreshed command tree.

If the vanilla enchanting table shows weak offers, place bookshelves around the player before opening the virtual table. It reads enchanting power around the location where the menu was opened.

If the super enchanting table does not list an enchantment, confirm that the enchantment supports the held item and is compatible with its current enchantments. Enable `superEnchantingAllowUnsafe` only when the server intentionally permits unsafe combinations.

If an item changes unexpectedly while the super enchanting menu is open, close the menu and reopen it while holding the intended item. The menu always edits the hotbar slot that was selected when it opened.

## Recovery

To disable a problematic command, set its module value to `false` and restart the server. Existing items keep any enchantments that were already applied.

To remove an unwanted enchantment, open the super enchanting table with the affected item and right click the matching book until its current level reaches `0`. If the enchantment is incompatible with the item, it still appears while its current level is greater than `0` so it can be removed.
