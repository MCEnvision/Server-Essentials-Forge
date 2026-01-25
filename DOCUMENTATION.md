# Better Forge Chat Reborn Reworked - Documentation

**Version:** 4.0.0  
**Author:** EnVy  
**Minecraft Version:** 1.20.1  
**Mod Loader:** Forge

---

## Table of Contents

1. [Overview](#overview)
2. [Installation](#installation)
3. [Configuration](#configuration)
4. [Commands](#commands)
5. [Permissions](#permissions)
6. [Features](#features)
7. [Data Storage](#data-storage)
8. [Changelog](#changelog)
9. [Quick Reference](#quick-reference)

---

## Overview

Better Forge Chat Reborn is a comprehensive chat management mod for Minecraft Forge servers. It provides:

- **Custom Chat Formatting** - Colors, styles, markdown support
- **LuckPerms Integration** - Prefixes, suffixes, and group-based permissions
- **FTB Essentials Integration** - Nickname support
- **Private Messaging System** - `/msg`, `/r`, and aliases
- **Announcement System** - Scheduled text and command announcements
- **Word Filter System** - Filter and replace words in chat
- **Chat Reply System** - Click-to-reply functionality with chat logging
- **Tab List Customization** - Custom headers, footers, and player display

---

## Installation

1. Download the mod JAR file
2. Place it in your server's `mods/` folder
3. Start the server to generate the config file
4. Configure `config/bfcrr/common.toml` as needed
5. Restart the server or use `/bfcrr reload`

### Optional Dependencies

- **LuckPerms** - For prefix/suffix and advanced permissions
- **FTB Essentials** - For nickname integration

---

## File Structure

### Server-Wide Config (`config/bfcrr/`)
Files that persist across all worlds:

| File | Description |
|------|-------------|
| `common.toml` | Main mod configuration |
| `motd.json` | Server MOTD settings |

### Per-World Data (`<world>/serverconfig/bfcrr/`)
Files that are specific to each world:

| File | Description |
|------|-------------|
| `announcements.json` | Text and command announcements |
| `announcement_prefs.json` | Player toggle preferences |
| `filters.json` | Word filter rules |
| `banned_items.json` | Banned item list |
| `banned_config.json` | Banned items scan settings |
| `bulletin.json` | Operator bulletin items |

### Logs (`logs/chat/`)
Chat logs for the reply system:

| File | Description |
|------|-------------|
| `chat_<timestamp>.log` | Chat log per server instance |

---

## Configuration

Configuration file: `config/bfcrrmod-common.toml`

### Chat Formatting

| Option | Default | Description |
|--------|---------|-------------|
| `playerNameFormat` | `$prefix$name$suffix` | Format for player names. Placeholders: `$prefix`, `$name`, `$suffix` |
| `chatMessageFormat` | `$time \| $name: $msg` | Format for chat messages. Placeholders: `$time`, `$name`, `$msg` |
| `chatMessageColor` | `WHITE` | Global chat message color |
| `timestampFormat` | `HH:mm` | Java SimpleDateFormat pattern for timestamps |
| `metaJoinSeparator` | ` ` (space) | Separator between multiple prefixes/suffixes |

### Message Formats

All message formats support `&` color codes and the listed placeholders.

| Option | Default | Placeholders | Description |
|--------|---------|--------------|-------------|
| `msgSentFormat` | `&d&lTo &d$receiver&7: &r&7$message` | `$sender`, `$receiver`, `$message` | Outgoing private message |
| `msgReceivedFormat` | `&d&lFrom &d$sender&7: &r&7$message` | `$sender`, `$receiver`, `$message` | Incoming private message |
| `replyHeaderFormat` | `    &f&l┌────&r &7Replying to $original_sender&7: &7$summary` | `$replier`, `$original_sender`, `$summary` | Reply header line |
| `helpOpRequestFormat` | `&l&cHelpOp &fFrom &e$sender&7:&r&7 $message` | `$sender`, `$message` | HelpOp request to operators |
| `helpOpReplyFormat` | `&l&cHelpOp &4OP&f Replied&7:&r&7 $message` | `$message` | HelpOp reply to player |
| `adminChatFormat` | `&4&lAdmin Chat &e$sender&7:&r $message` | `$sender`, `$message` | Admin chat message |
| `announcementConfirmFormat` | `&aAdded announcement: &e$id &7(every $interval)` | `$id`, `$interval`, `$message` | Announcement creation confirmation |

### Feature Toggles

| Option | Default | Description |
|--------|---------|-------------|
| `enableTimestamp` | `true` | Enable timestamps in chat |
| `useFtbEssentials` | `true` | Enable FTB Essentials nickname integration |
| `useLuckPerms` | `true` | Enable LuckPerms integration |
| `markdownEnabled` | `true` | Enable markdown styling in chat |
| `enableColorsCommand` | `true` | Enable the `/colors` command |
| `tabList` | `true` | Enable custom tab list integration |
| `tabListMetadata` | `true` | Show prefixes/suffixes in tab list |
| `tabListNicknames` | `true` | Show nicknames in tab list |
| `enableWhoisCommand` | `true` | Enable the `/whois` command |
| `enableIntegratedNicknames` | `false` | Enable built-in nickname commands |
| `autoIntegratedNicknames` | `true` | Auto-enable nickname commands if FTB Essentials is not present |
| `enableDiscordIntegration` | `false` | Enable Discord bot integration |
| `enableCustomTabHeaderFooter` | `false` | Enable custom tab header/footer |

### Module Toggles

| Option | Default | Description |
|--------|---------|-------------|
| `enableAnnouncements` | `true` | Enable the announcement system |
| `enableFilterSystem` | `true` | Enable the word filter system |
| `enableMessagingSystem` | `true` | Enable private messaging (`/msg`, `/r`, etc.) |
| `enableChatReplies` | `true` | Enable chat reply system (`/ans`) with chat logging |
| `replySummaryLength` | `50` | Maximum length of message summary in reply headers (0 = no limit) |
| `enableHelpOp` | `true` | Enable the `/helpop` command for player-to-operator communication |
| `enableAdminChat` | `true` | Enable the admin chat system (`/chat admin`, `/ac`) |
| `enableBannedItems` | `true` | Enable the banned items system (`/banned` commands) |

### Sound Notifications

| Option | Default | Description |
|--------|---------|-------------|
| `enableMsgSound` | `true` | Play sound when receiving a private message |
| `enableReplySound` | `true` | Play sound when someone replies to your message |
| `enableHelpOpSound` | `true` | Play sound for HelpOp notifications |
| `enableAdminChatSound` | `false` | Play sound for Admin Chat messages |

### System Messages

All system messages support `&` color codes.

| Option | Default | Description |
|--------|---------|-------------|
| `adminChatEnabledMsg` | `&aAdmin chat enabled...` | Message when admin chat is enabled |
| `adminChatDisabledMsg` | `&cAdmin chat disabled...` | Message when admin chat is disabled |
| `helpOpSentMsg` | `&aYour help request has been sent to $count operator(s)` | Confirmation message (placeholder: `$count`) |
| `helpOpReplySentMsg` | `&aReply sent to $player` | Reply sent confirmation (placeholder: `$player`) |
| `noReplyTargetMsg` | `&cNo one to reply to.` | Message when no reply target |
| `playerOfflineMsg` | `&cThat player is offline.` | Message when player is offline |
| `messageNotFoundMsg` | `&cMessage not found or too old to reply to.` | Message when reply target not found |
| `noPermissionMsg` | `&cYou don't have permission to do that.` | Generic permission denied message |

### Hover Text

| Option | Default | Description |
|--------|---------|-------------|
| `clickToReplyHover` | `&eClick to reply` | Hover text for click to reply |
| `clickToMessageHover` | `&dClick to message $player` | Hover text for click to message (placeholder: `$player`) |
| `helpOpReplyHover` | `&7Click to reply to $player` | Hover text for HelpOp reply (placeholder: `$player`) |

### Announcement Formatting

| Option | Default | Description |
|--------|---------|-------------|
| `announcementListHeaderText` | `&6━━━━━━━━ Text Announcements ━━━━━━━━` | Header for text announcement list |
| `announcementListHeaderCmd` | `&6━━━━━━━━ Command Announcements ━━━━━━━━` | Header for command announcement list |
| `toggleListHeader` | `&6━━━━━━ Toggleable Announcements ━━━━━━` | Header for toggle list |
| `toggleOnText` | `&a[ON]` | Text shown when toggle is ON |
| `toggleOffText` | `&c[OFF]` | Text shown when toggle is OFF |

### Tab List

| Option | Default | Description |
|--------|---------|-------------|
| `tabHeaderFormat` | `""` | Tab header format. Placeholders: `{server_ip}`, `{online}`, `{max}` |
| `tabFooterFormat` | `""` | Tab footer format. Placeholders: `{server_ip}`, `{online}`, `{max}` |

### Nicknames

| Option | Default | Description |
|--------|---------|-------------|
| `maximumNicknameLength` | `50` | Maximum nickname length (1-500) |
| `minimumNicknameLength` | `1` | Minimum nickname length (1-500) |
| `maxPrefixesDisplayed` | `1` | Maximum number of prefixes to show |
| `maxSuffixesDisplayed` | `1` | Maximum number of suffixes to show |

### Announcements (Legacy)

| Option | Default | Description |
|--------|---------|-------------|
| `announcementIntervalSeconds` | `300` | Default interval between announcements |
| `announcementUseRandomOrder` | `false` | Randomize announcement order |

---

## Commands

### Core Commands

#### `/bfcrr <subcommand>`
Main mod command.

| Subcommand | Permission | Description |
|------------|------------|-------------|
| `info` | `bfcrrmod.commands.bfcrr.info` | Show mod information and integration status |
| `colors` | `bfcrrmod.commands.bfcrr.colors` | Display color code reference |
| `test` | - | Test color and styling rendering |
| `reload` | `bfcrrmod.commands.bfcrr.reload` | Reload configuration from disk |

#### `/colors`
Display color code reference chart.
- **Permission:** `bfcrrmod.commands.colors`
- **Config:** `enableColorsCommand`

---

### Private Messaging

#### `/msg <player> <message>`
Send a private message to a player.
- **Aliases:** `/m`, `/tell`, `/w`, `/pm`
- **Permission:** `bfcrrmod.commands.msg`
- **Config:** `enableMessagingSystem`

#### `/msg <player>` (no message)
Toggle private chat mode with that player. All your messages will be sent only to them.
- **Permission:** `bfcrrmod.commands.msg`

#### `/msg` or `/r` (no arguments)
Disable private chat mode and return to public chat.
- **Permission:** `bfcrrmod.commands.msg`

#### `/r <message>`
Reply to the last person who messaged you.
- **Permission:** `bfcrrmod.commands.msg`
- **Config:** `enableMessagingSystem`
- **Note:** Only works after receiving a message (not just sending)

**Features:**
- Click on received messages to quick-reply with `/msg <sender>`
- Hover shows customizable tooltip (config: `clickToMessageHover`)
- Recipients hear the chicken egg plop sound when receiving (configurable via `enableMsgSound`)
- **Toggle Mode:** Use `/msg <player>` to start a private chat - all your messages go only to them until you use `/msg` or `/r` to exit
- **Discord Integration:** With Simple Discord Link installed, toggled private chats are hidden from Discord automatically

---

### Chat Reply System

#### `/ans <messageId> <message>`
Reply to a specific chat message.
- **Permission:** `bfcrrmod.commands.ans` (default: `true`)
- **Config:** `enableChatReplies`

**How it works:**
1. All chat messages AND announcements are clickable
2. Hover over any message to see the hover text (configurable via `clickToReplyHover`)
3. Click to auto-fill `/ans <id>` in chat
4. Type your reply

**Supported Message Types:**
- Player chat messages
- Announcements (text announcements)
- Any message sent through the mod's systems

**Reply Format:**
Configurable via `replyHeaderFormat`. Default:
```
    ┌──── Replying to [Rank] OriginalPlayer: message summary...
[Rank] YourName: Your reply message
```

**Sound Notifications:**
- When someone replies to your message (`/ans`), you hear the note block pling sound
  - Configurable via `enableReplySound`
  - Requires `bfcrrmod.ans.notify` permission (default: `true`)
- When you receive a private message (`/msg`), you hear the chicken egg plop sound
  - Configurable via `enableMsgSound`

**Chat Logging:**
- All messages are logged to `logs/chat/chat_YYYY-MM-DD_HH-mm-ss.log`
- New log file created per server instance
- Format: `[HH:mm:ss] [ID:123] [Rank] PlayerName: Message`

---

### HelpOp System

**Config:** `enableHelpOp`

The HelpOp system allows players to request help from server operators.

#### `/helpop <message>`
Send a help request to all online operators.
- **Permission:** `bfcrrmod.helpop.send` (default: `true`)
- **Format to operators:** Configurable via `helpOpRequestFormat`
- **Sound:** Chicken egg plop sound plays for operators (configurable via `enableHelpOpSound`)
- **Click to reply:** Operators can click the message to auto-fill `/helpopop <player>`
- **Who receives:** OP Level 2+ or players with `bfcrrmod.helpop.receive`

#### `/helpopop <player> <message>`
Operators reply to a player's help request anonymously.
- **Permission:** `bfcrrmod.helpop.reply` or OP Level 2+
- **Format to player:** Configurable via `helpOpReplyFormat`
- **Sound:** Chicken egg plop sound plays for the player (configurable via `enableHelpOpSound`)

---

### Admin Chat

**Config:** `enableAdminChat`

Private chat channel visible only to operators and players with admin chat permissions.

**Discord Integration:** When Simple Discord Link (SDLink) is installed, players with admin chat toggled are automatically hidden from Discord relay. Your admin messages stay completely private and won't appear on Discord at all.

#### `/chat admin`
Toggle admin chat mode on/off. When enabled, all your normal chat messages are intercepted and sent only to admin chat.
- **Permission:** `bfcrrmod.adminchat.use` or OP Level 2+
- **Alias:** `/ac`

#### `/chat admin <message>` or `/ac <message>`
Send a single message to admin chat without toggling.
- **Permission:** `bfcrrmod.adminchat.use` or OP Level 2+
- **Format:** Configurable via `adminChatFormat`
- **Sound:** Note block pling sound plays for all operators (configurable via `enableAdminChatSound`, default: off)
- **Who sees:** OP Level 2+ or players with `bfcrrmod.adminchat.see`

---

### Op Bulletin

A paginated bulletin board for operators to share notes and important information.

#### `/opbulletin [page]`
View the bulletin board.
- **Permission:** OP Level 2+
- **Features:**
  - Paginated display (5 items per page)
  - Click left/right arrows to navigate pages
  - Click items to remove them

#### `/opbulletin add <text>`
Add a new item to the bulletin.
- **Permission:** OP Level 2+
- **Supports:** Color codes and formatting

#### `/opbulletin remove <id>`
Remove an item by its number.
- **Permission:** OP Level 2+

**Display Format:**
```
-----------Op Bulletin-----------
1. First bulletin item
2. Second bulletin item
--------[◄]--1/2--[►]--------
```

---

### Banned Items System

**Config:** `enableBannedItems`

A system for banning items from players - automatically removes banned items from inventories, containers, and equipped slots.

#### `/banned [page]`
View all banned items with pagination.
- **Permission:** All players can view

**Display:**
- Hover over items to see description, who banned it, and when
- Click on items to get the remove command

#### `/banned add <description>`
Ban the item you're currently holding.
- **Permission:** OP Level 2+
- The item in your main hand will be banned
- Description is shown when viewing the ban list

#### `/banned remove <item_id>`
Unban an item.
- **Permission:** OP Level 2+
- Tab completion suggests banned item IDs

#### `/banned clear`
Remove all banned items.
- **Permission:** OP Level 2+

#### `/banned scantime [set <ticks>]`
View or set the scan interval for player inventory scans.
- **Permission:** OP Level 2+
- Default: 100 ticks (5 seconds)
- Lower values = more frequent scans but higher CPU usage

#### `/banned scantimeblocks [set <ticks>]`
View or set the scan interval for nearby block scans.
- **Permission:** OP Level 2+
- Default: 200 ticks (10 seconds)
- Controls how often the mod scans for banned blocks placed near players
- Lower values = more frequent scans but higher TPS impact

#### `/banned scan`
Manually scan yourself for banned items.
- **Permission:** OP Level 2+
- Skipped if in creative mode

**Features:**
- Scans player inventories at configurable intervals
- Scans open containers (chests, shulker boxes, etc.)
- Scans Curios mod slots if installed (rings, necklaces, etc.)
- Scans armor and offhand slots
- Scans cursor item (item being moved)
- **Block Placement Prevention:** Prevents placing banned blocks in the world
- **Block Scanning:** Periodically scans for banned blocks near players and removes them
- Creative/spectator players are skipped
- Removed items are announced to the player

**Block Scanning Config Options:**
| Config Key | Default | Description |
|------------|---------|-------------|
| `enableBannedBlockScanning` | `true` | Enable/disable scanning for placed banned blocks |
| `bannedBlockScanRadius` | `5` | Radius around players to scan (1-20 blocks) |
| `bannedBlockScanInterval` | `200` | Ticks between block scans (200 = every 10 seconds) |

---

### MOTD System

**Config:** `enableMotdSystem`, `applyMotdOnStartup`

Manage the server's Message of the Day (MOTD) that appears in the server list.

#### `/bfcrr motd list [page]`
View all configured MOTDs with pagination.
- **Permission:** OP Level 2+
- Click on an MOTD to edit it
- Click [Activate] to set it as the current MOTD
- Click [X] to remove it

#### `/bfcrr motd add <text>`
Add a new MOTD.
- **Permission:** OP Level 2+
- Supports color codes: `&a`, `&b`, `&c`, etc.
- Supports line breaks: `<br>`
- Example: `/bfcrr motd add &a&lMy Server<br>&7Welcome!`

#### `/bfcrr motd set <index> <text>`
Modify an existing MOTD.
- **Permission:** OP Level 2+
- If modifying the active MOTD, changes are applied immediately

#### `/bfcrr motd remove <index>`
Remove an MOTD by index.
- **Permission:** OP Level 2+

#### `/bfcrr motd activate <index>`
Set which MOTD is active and apply it to the server.
- **Permission:** OP Level 2+

#### `/bfcrr motd apply`
Force apply the current MOTD to the server.
- **Permission:** OP Level 2+

#### `/bfcrr motd preview <index>`
Preview how an MOTD will look.
- **Permission:** OP Level 2+

#### `/bfcrr motd clear`
Remove all MOTDs.
- **Permission:** OP Level 2+

**Supported Color Codes:**
- `&0`-`&9`, `&a`-`&f` - Colors
- `&l` - Bold
- `&o` - Italic
- `&n` - Underline
- `&m` - Strikethrough
- `&k` - Obfuscated
- `&r` - Reset

**Config Options:**
| Config Key | Default | Description |
|------------|---------|-------------|
| `enableMotdSystem` | `true` | Enable the MOTD system |
| `applyMotdOnStartup` | `true` | Auto-apply MOTD when server starts |

---

#### `/nick <nickname>`
Set your own nickname.
- **Permission:** `bfcrrmod.commands.nick`
- **Config:** `enableIntegratedNicknames` or `autoIntegratedNicknames`

#### `/nick <player> <nickname>`
Set another player's nickname.
- **Permission:** `bfcrrmod.commands.nick.others`

#### `/whois <nickname>`
Look up who owns a nickname.
- **Permission:** `bfcrrmod.commands.whois`
- **Config:** `enableWhoisCommand`

---

### Announcement System

**Config:** `enableAnnouncements`

**Timing Behavior:**
- Announcements use the server's clock time for scheduling
- If you create an announcement at **2:32 PM** with a **1H** interval, it will fire at **3:32 PM**, **4:32 PM**, etc.
- Using composite intervals like `1H3M` naturally prevents announcements from syncing
- Each announcement gets a small offset based on its ID to prevent same-interval announcements from firing together
- Minimum interval is **1 second** (`1S`)

#### `/textannouncement add <id> <interval> <toggle|notoggle> "<message>" <target>`
Create a scheduled text announcement. **The message is sent immediately when added**, then repeats at the specified interval.

| Parameter | Description |
|-----------|-------------|
| `id` | Unique identifier for the announcement |
| `interval` | Time between broadcasts (e.g., `1S`, `1H`, `30M`, `45S`, or combined `1H30M15S`) |
| `toggle\|notoggle` | Whether players can disable this announcement |
| `message` | The message to broadcast (quoted string, supports `&` color codes) |
| `target` | `@a` for all players, or a specific player name |

**Line Breaks:**
Use `<br>` in your message to create multiple lines:
```
/textannouncement add multiline 1H toggle "Line 1 <br> Line 2 <br> Line 3" @a
```
This will display as:
```
Line 1
Line 2
Line 3
```

**Example:**
```
/textannouncement add welcome 1H toggle "&aWelcome to the server!" @a
/textannouncement add reminder 1H30M toggle "&eRemember to vote!" @a
/textannouncement add test 5S notoggle "&cTest every 5 seconds" @a
/textannouncement add motd 2H toggle "&6Server MOTD <br> &7Join our Discord!" @a
```

#### `/textannouncement ontime "<message>" [target]`
Send a one-time announcement immediately (not scheduled/repeated).

| Parameter | Description |
|-----------|-------------|
| `message` | The message to broadcast (quoted string, supports `&` color codes and `<br>` for line breaks) |
| `target` | Optional. `@a` for all players (default), or a specific player name |

**Example:**
```
/textannouncement ontime "&c&lServer restart in 5 minutes!"
/textannouncement ontime "&aWelcome back!" Steve
/textannouncement ontime "&6Important! <br> &7Please read the rules." @a
```

#### `/textannouncement modify <id> <interval> <toggle|notoggle> "<message>" <target>`
Modify an existing text announcement. Use `-` to leave a field unchanged.

| Parameter | Description |
|-----------|-------------|
| `id` | Current identifier of the announcement |
| `interval` | New interval (use `-` to keep current) |
| `toggle\|notoggle` | New toggle setting (use `-` to keep current) |
| `message` | New message in quotes (use `-` to keep current) |
| `target` | New target (use `-` to keep current) |

**Examples:**
```
/textannouncement modify welcome 45M toggle "Welcome!" @a
/textannouncement modify welcome - - "-" -
/textannouncement modify test 2H notoggle "New message" @a
```

**Tip:** When viewing `/textannouncement list`, click on any announcement to auto-populate the modify command with its current values.

#### `/textannouncement remove <id>`
Remove a text announcement.

#### `/textannouncement list`
List all text announcements in a dedicated section (command announcements are listed separately).

---

#### `/commandannouncement add <id> <interval> <command>`
Create a scheduled command announcement.

| Parameter | Description |
|-----------|-------------|
| `id` | Unique identifier |
| `interval` | Time between executions (e.g., `1S`, `1H`, `30M`, `45S`, or combined `1H30M15S`) |
| `command` | The command to run (without leading `/`) |

**Example:**
```
/commandannouncement add backup 6H save-all
/commandannouncement add autosave 30M45S save-all
```

#### `/commandannouncement remove <id>`
Remove a command announcement.

#### `/commandannouncement list`
List all command announcements in a dedicated section.

**Note:** Text and command announcements are shown in separate sections when using the list command:
```
━━━━━━━━ Text Announcements ━━━━━━━━
  welcome (every 1H) [toggleable]
    → Message: &aWelcome!
    → Target: @a
━━━━━━━━ Command Announcements ━━━━━━━━
  backup (every 6H)
    → Command: save-all
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

---

#### `/titleannouncement <targets> "<title>" ["<subtitle>"]`
Send a title (and optional subtitle) to players.

| Parameter | Description |
|-----------|-------------|
| `targets` | Player selector: `@a` for all players, `@p` for nearest, or player name (no quotes needed) |
| `title` | The title text (quoted, supports `&` color codes) |
| `subtitle` | Optional subtitle text (quoted, supports `&` color codes) |

**Examples:**
```
/titleannouncement @a "&c&lWelcome!" "&eEnjoy your stay"
/titleannouncement Steve "&6Hello Steve!"
/titleannouncement @a "&bServer Restart" "&7in 5 minutes"
```

**Notes:**
- Target uses Minecraft's entity selector (like `/tp`) - no quotes needed
- Title and subtitle must be in quotes for color formatting
- Supports full color and style formatting with `&` codes
- Shows success message with player count

---

#### `/toggle [id]`
Toggle announcements on/off for yourself.
- Without arguments: List all toggleable announcements with their status
- With argument: Toggle specific announcement

**Features:**
- Hover over an announcement to see a preview of the message
- Click on an announcement to toggle it and refresh the list
- Shows `[ON]` (green) or `[OFF]` (red) status for each announcement

---

### Word Filter System

**Config:** `enableFilterSystem`

#### `/bfcrr filter add <id> <yes|no> "<word>" [replacement]`
Add a word filter.

| Parameter | Description |
|-----------|-------------|
| `id` | Unique identifier for the filter |
| `yes\|no` | Case sensitive? |
| `word` | The word/phrase to filter (quoted string) |
| `replacement` | Replacement text (optional - if omitted, word is removed) |

**Examples:**
```
/bfcrr filter add badword no "badword" "****"
/bfcrr filter add spam no "spamtext"
```

#### `/bfcrr filter remove <id>`
Remove a word filter.

#### `/bfcrr filter list`
List all configured word filters.

---

## Permissions

All permissions use the prefix `bfcrrmod.` and default to `true` unless otherwise noted.

### Chat Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `bfcrrmod.chat.colors` | `true` | Use color codes in chat |
| `bfcrrmod.chat.styles` | `true` | Use style codes in chat (bold, italic, etc.) |
| `bfcrrmod.chat.styles.md` | `true` | Use markdown styling in chat |
| `bfcrrmod.chat.colors.hex` | `true` | Use hex colors and gradients |
| `bfcrrmod.chat.colors.0-f` | `true` | Per-color permissions (0-9, a-f) |
| `bfcrrmod.sign.colors` | `false` | Use colors on signs |
| `bfcrrmod.sign.styles` | `false` | Use styles on signs |

### Tab List Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `bfcrrmod.tablist.nickname` | `true` | Show nickname in tab list |
| `bfcrrmod.tablist.metadata` | `true` | Show prefix/suffix in tab list |

### Command Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `bfcrrmod.commands.colors` | `true` | Use `/colors` command |
| `bfcrrmod.commands.bfcrr.allowed` | `true` | Use `/bfcrr` command |
| `bfcrrmod.commands.bfcrr.colors` | `true` | Use `/bfcrr colors` |
| `bfcrrmod.commands.bfcrr.info` | `true` | Use `/bfcrr info` |
| `bfcrrmod.commands.bfcrr.reload` | `true` | Use `/bfcrr reload` |
| `bfcrrmod.commands.msg` | `true` | Use `/msg`, `/r`, and aliases |
| `bfcrrmod.commands.whois` | `true` | Use `/whois` command |
| `bfcrrmod.commands.nick` | `true` | Use `/nick` on self |
| `bfcrrmod.commands.nick.others` | `true` | Use `/nick` on others |
| `bfcrrmod.commands.ans` | `true` | Use `/ans` reply command |

### Private Messaging Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `bfcrrmod.msg.receive` | `true` | Can receive private messages |
| `bfcrrmod.msg.sendoffline` | `false` | Can send messages to offline players (queued) |
| `bfcrrmod.ans.notify` | `true` | Receive sound when someone replies |

### Nickname Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `bfcrrmod.nick.colors` | `false` | Use color codes in nicknames |
| `bfcrrmod.nick.styles` | `false` | Use style codes in nicknames |

### HelpOp Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `bfcrrmod.helpop.send` | `true` | Can send /helpop requests |
| `bfcrrmod.helpop.receive` | `false` | Receives HelpOp messages (for non-OP staff) |
| `bfcrrmod.helpop.reply` | `false` | Can use /helpopop to reply |

### Admin Chat Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `bfcrrmod.adminchat.use` | `false` | Can use admin chat commands |
| `bfcrrmod.adminchat.see` | `false` | Can see admin chat messages |

### Announcement Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `bfcrrmod.announcements.manage` | `false` | Can add/remove announcements |
| `bfcrrmod.announcements.toggle` | `true` | Can toggle announcements on/off |
| `bfcrrmod.announcements.bypass` | `false` | Receives announcements even if toggled off |
| `bfcrrmod.announcements.title` | `false` | Can use /titleannouncement |

### Filter Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `bfcrrmod.filter.manage` | `false` | Can manage word filters |
| `bfcrrmod.filter.bypass` | `false` | Messages bypass word filter |

### Admin Commands

The following commands require **OP level 2** (or equivalent permission node):

- `/textannouncement` - requires `bfcrrmod.announcements.manage` or OP
- `/commandannouncement` - requires `bfcrrmod.announcements.manage` or OP
- `/titleannouncement` - requires `bfcrrmod.announcements.title` or OP
- `/bfcrr filter` - requires `bfcrrmod.filter.manage` or OP
- `/helpopop <player> <message>` - requires `bfcrrmod.helpop.reply` or OP
- `/chat admin` / `/ac` - requires `bfcrrmod.adminchat.use` or OP

---

## Features

### Color Codes

Use `&` followed by a code:

| Code | Color | Code | Style |
|------|-------|------|-------|
| `&0` | Black | `&l` | **Bold** |
| `&1` | Dark Blue | `&m` | ~~Strikethrough~~ |
| `&2` | Dark Green | `&n` | <u>Underline</u> |
| `&3` | Dark Aqua | `&o` | *Italic* |
| `&4` | Dark Red | `&k` | Obfuscated |
| `&5` | Dark Purple | `&r` | Reset |
| `&6` | Gold | | |
| `&7` | Gray | | |
| `&8` | Dark Gray | | |
| `&9` | Blue | | |
| `&a` | Green | | |
| `&b` | Aqua | | |
| `&c` | Red | | |
| `&d` | Light Purple | | |
| `&e` | Yellow | | |
| `&f` | White | | |

### Hex Colors

Use `&#RRGGBB` for hex colors:
```
&#FF5500Hello World
```

### Markdown Styling

When enabled, you can use:
- `**bold**` → **bold**
- `*italic*` → *italic*
- `__underline__` → <u>underline</u>
- `~~strikethrough~~` → ~~strikethrough~~

---

## Data Storage

### Announcements
- **Location:** `<world>/serverconfig/bfcrr_announcements/`
- **Files:**
  - `announcements.json` - Announcement definitions
  - `toggle_prefs.json` - Player toggle preferences

### Word Filters
- **Location:** `<world>/serverconfig/bfcrr_filters/`
- **Files:**
  - `filters.json` - Filter definitions

### Chat Logs
- **Location:** `logs/chat/`
- **Files:** `chat_YYYY-MM-DD_HH-mm-ss.log` (one per server instance)

### Player Data
- **Location:** `<world>/playerdata/` (standard Minecraft player data directory)

---

## Changelog

### Version 4.0.0
- **Renamed mod from BFCR to BFCRR** (Better Forge Chat Reborn Reworked)
  - All commands changed from `/bfcr` to `/bfcrr`
  - Mod ID: `bfcrrmod`
  - Config file: `bfcrrmod-common.toml`
- Added **Chat Reply System** (`/ans` command)
  - Click any chat message to reply
  - Shows formatted reply headers with player ranks
  - Chat logging to `logs/chat/`
  - Note block pling sound when someone replies to your message
  - Configurable reply summary length (`replySummaryLength`)
  - Strips "[Reply to X]" prefix from reply summaries
- Added **Word Filter System** (`/bfcrr filter` commands)
  - Case-sensitive or insensitive matching
  - Replace or remove filtered words
- Added **Private Messaging System** (`/msg`, `/r`)
  - Click-to-reply functionality
  - Only allows `/r` after receiving a message
  - Chicken egg plop sound when receiving a message
- Added **HelpOp System** (`/helpop`)
  - Players can request help from operators
  - Operators reply anonymously
  - Click-to-reply in chat
  - Sound notifications
- Added **Admin Chat** (`/chat admin`, `/ac`)
  - Private chat channel for operators only
  - Toggle mode or single-message mode
  - Note block pling sound notifications
- Added **Announcement System**
  - `/textannouncement add` - Scheduled text messages (fires immediately, then repeats)
  - `/textannouncement ontime` - One-time announcements
  - `/textannouncement modify` - Modify existing announcements
  - `/commandannouncement` - Scheduled commands with add, remove, list
  - `/titleannouncement` - Send titles to players (uses entity selectors like @a)
  - `/toggle` - Player-controlled announcement preferences with hover previews
  - Separate list views for text vs command announcements
  - Composite interval support (e.g., `1H30M15S`)
  - Minimum interval of 1 second (`1S`)
  - **Server clock-based scheduling** - Announcements fire based on when created
  - ID-based offset prevents same-interval announcements from syncing
- Added module toggle config options
- Added **Customizable Message Formats**
  - All message formats configurable via config file
  - Private message sent/received formats
  - Reply header format
  - HelpOp request/reply formats
  - Admin chat format
  - Announcement confirmation format
  - Support for color codes and placeholders
- Added **Sound Configuration**
  - Toggle sounds for private messages, replies, HelpOp, Admin Chat
- Added **Customizable System Messages**
  - All feedback messages configurable
  - Error messages, confirmation messages, etc.
- Added **Customizable Hover Text**
  - All tooltips configurable with placeholders
- Added **Announcement Formatting Options**
  - List headers, toggle display text
- Added **30+ LuckPerms Permission Nodes**
  - Chat permissions (colors, styles, markdown, per-color)
  - Private messaging permissions (send, receive, offline)
  - Reply system permissions (command, notifications)
  - HelpOp permissions (send, receive, reply)
  - Admin Chat permissions (use, see)
  - Announcement permissions (manage, toggle, bypass, title)
  - Filter permissions (manage, bypass)
  - Nickname permissions (colors, styles)
  - Sign permissions (colors, styles)
- Added **Op Bulletin System** (`/opbulletin`)
  - Paginated bulletin board for operators
  - Add/remove items with `/opbulletin add/remove`
  - Click items to remove, arrows to navigate
  - Supports color formatting
- Added **Private Chat Toggle Mode**
  - `/msg <player>` toggles private chat mode
  - All messages go only to that player
  - `/msg` or `/r` exits private chat mode
- Updated reply header format to use `┌────` (upward facing)
- Made `/textannouncement list` items clickable to populate modify command
- Updated `/textannouncement modify` to include all parameters
- Added `<br>` line break support in text announcements
- Added pagination with arrows to all list commands
  - `/textannouncement list [page]`
  - `/commandannouncement list [page]`
  - `/bfcrr filter list [page]`
  - `/opbulletin [page]`
  - `/banned [page]`
- Added **Banned Items System** (`/banned` commands)
  - Ban items from player inventories
  - Scans inventories, containers, armor, offhand, Curios slots
  - Configurable scan interval
  - Hover for details, click to remove
- Fixed announcement preview to strip `<br>` tags
- Fixed default message format for `/msg` (`&d&lTo/From &d<name>&7: &r&7<message>`)
- Set chat event priority to HIGHEST to prevent message leaking to other mods
- All new systems are configurable and can be disabled
- Code cleanup and warning fixes

### Version 3.0.0
- Initial reborn release
- LuckPerms integration
- FTB Essentials integration
- Tab list customization
- Markdown support

---

## Quick Reference

### All Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/bfcrr info` | Show mod info | `bfcrrmod.commands.bfcrr.info` |
| `/bfcrr colors` | Show color codes | `bfcrrmod.commands.bfcrr.colors` |
| `/bfcrr reload` | Reload config | `bfcrrmod.commands.bfcrr.reload` |
| `/colors` | Show color codes | `bfcrrmod.commands.colors` |
| `/msg <player> <msg>` | Private message | `bfcrrmod.commands.msg` |
| `/msg <player>` | Toggle private chat with player | `bfcrrmod.commands.msg` |
| `/msg` or `/r` | Exit private chat mode | `bfcrrmod.commands.msg` |
| `/r <msg>` | Reply to last message | `bfcrrmod.commands.msg` |
| `/ans <id> <msg>` | Reply to chat message | `bfcrrmod.commands.ans` |
| `/nick <name>` | Set your nickname | `bfcrrmod.commands.nick` |
| `/nick <player> <name>` | Set other's nickname | `bfcrrmod.commands.nick.others` |
| `/whois <nickname>` | Look up nickname | `bfcrrmod.commands.whois` |
| `/helpop <msg>` | Request help | `bfcrrmod.helpop.send` |
| `/helpopop <player> <msg>` | Reply to helpop | `bfcrrmod.helpop.reply` or OP |
| `/chat admin` | Toggle admin chat | `bfcrrmod.adminchat.use` or OP |
| `/ac <msg>` | Send admin chat | `bfcrrmod.adminchat.use` or OP |
| `/textannouncement add` | Add text announcement | OP Level 2 |
| `/textannouncement ontime` | One-time announcement | OP Level 2 |
| `/textannouncement modify` | Modify announcement | OP Level 2 |
| `/textannouncement remove` | Remove announcement | OP Level 2 |
| `/textannouncement list` | List text announcements | OP Level 2 |
| `/commandannouncement add` | Add command announcement | OP Level 2 |
| `/commandannouncement remove` | Remove command announcement | OP Level 2 |
| `/commandannouncement list` | List command announcements | OP Level 2 |
| `/titleannouncement` | Send title to players | OP Level 2 |
| `/toggle [id]` | Toggle announcement | `bfcrrmod.announcements.toggle` |
| `/bfcrr filter add` | Add word filter | OP Level 2 |
| `/bfcrr filter remove` | Remove word filter | OP Level 2 |
| `/bfcrr filter list` | List word filters | OP Level 2 |
| `/opbulletin` | View bulletin board | OP Level 2 |
| `/opbulletin add <text>` | Add bulletin item | OP Level 2 |
| `/opbulletin remove <id>` | Remove bulletin item | OP Level 2 |
| `/banned` | View banned items | All players |
| `/banned add <desc>` | Ban held item | OP Level 2 |
| `/banned remove <id>` | Unban item | OP Level 2 |
| `/banned clear` | Clear all bans | OP Level 2 |
| `/banned scantime` | View/set item scan interval | OP Level 2 |
| `/banned scantimeblocks` | View/set block scan interval | OP Level 2 |
| `/banned scan` | Manually scan self | OP Level 2 |

### All Config Sections

| Section | Description |
|---------|-------------|
| `BetterForgeChatModConfig` | Main configuration |
| `messageFormats` | Customizable message formats |
| `sounds` | Sound notification toggles |
| `systemMessages` | Customizable feedback messages |
| `hoverText` | Customizable hover tooltips |
| `announcementFormatting` | Announcement display formatting |

### All Permission Categories

| Category | Prefix | Description |
|----------|--------|-------------|
| Chat | `bfcrrmod.chat.*` | Chat colors, styles, markdown |
| Tab List | `bfcrrmod.tablist.*` | Tab list display |
| Commands | `bfcrrmod.commands.*` | Command access |
| Messaging | `bfcrrmod.msg.*` | Private messaging features |
| Nicknames | `bfcrrmod.nick.*` | Nickname features |
| HelpOp | `bfcrrmod.helpop.*` | HelpOp system |
| Admin Chat | `bfcrrmod.adminchat.*` | Admin chat features |
| Announcements | `bfcrrmod.announcements.*` | Announcement system |
| Filters | `bfcrrmod.filter.*` | Word filter system |
| Signs | `bfcrrmod.sign.*` | Sign formatting |

---

## Support

For issues and feature requests, please visit the GitHub repository.

**© 2022-2026 EnVy**

