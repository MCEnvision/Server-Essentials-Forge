# AGENTS.md — Better Forge Chat Reborn Reworked

AI coding agent guide for `bfcrrmod` (Forge 1.20.1, Java 17).

---

## Project Overview

A server-side Forge chat mod (`mod_id = bfcrrmod`) that provides custom chat formatting, LuckPerms/FTB-Essentials integration, private messaging, announcements, word filtering, MOTD, banned items, and tab-list customization.

**Entry point:** `src/main/java/com/jeremiahbl/bfcrmod/BetterForgeChat.java`  
**Root package:** `com.jeremiahbl.bfcrmod`

---

## 🔴 Rule 1 — Nothing Hardcoded

**Every string, number, format, interval, toggle, message, or sound must be backed by a config value.** Never hardcode values like intervals, messages, format strings, or feature toggles directly in Java code.

- All config lives in `ConfigHandler.java` (`ForgeConfigSpec`) and is written to `config/bfcrr/common.toml` at runtime.
- Add every new value as a `ForgeConfigSpec.ConfigValue<T>` field inside `ConfigBuilder`, with a `.comment()` explaining placeholders and valid values.
- If `common.toml` grows too large, create additional config files under `config/bfcrr/` (e.g., `config/bfcrr/announcements.toml`, `config/bfcrr/filters.toml`) — see the "Multi-file config" section below.
- Any class that caches config values **must** implement `IReloadable` and be registered via `ConfigurationEventHandler.registerReloadable()` in the `BetterForgeChat` constructor, so that `/bfcrr reload` stays accurate.

### Adding a config value (example)
```java
// In ConfigHandler.ConfigBuilder constructor:
myNewInterval = builder
    .comment("Interval in seconds for X (0 = disabled)")
    .defineInRange("myNewInterval", 60, 0, 3600);

// In the consuming class, implement IReloadable:
@Override public void reloadConfigOptions() {
    this.interval = ConfigHandler.config.myNewInterval.get();
}
```

---

## 🔴 Rule 2 — Commit & Push Every Change

**Every code change must be committed to Git with a detailed message and pushed to GitHub before considering the task done.**

Commit message format:
```
<Short imperative summary (≤72 chars)>

- Bullet describing what changed and why
- Bullet for each file modified
- Note any config keys added/changed
- Note any permission nodes added/changed
```

Example:
```
Add configurable announcement prefix format

- ConfigHandler: added announcementPrefixFormat config key (default "&6[Announce] &r")
- AnnouncementManager: replaced hardcoded prefix with config value
- Implements IReloadable to hot-reload on /bfcrr reload
```

Commands to run after every change:
```powershell
git add -A
git commit -m "Your detailed message here"
git push
```

---

## Architecture

### Package Layout

| Package | Purpose |
|---|---|
| `bfcrmod` (root) | Main class, `TextFormatter`, `MarkdownFormatter`, `BitwiseStyling` |
| `bfcrmod.config` | `ConfigHandler`, `PermissionsHandler`, `PlayerData`, `IReloadable`, `ConfigurationEventHandler` |
| `bfcrmod.commands` | `BfcCommands` (`/bfcrr`), `NickCommands` (`/nick`, `/whois`) |
| `bfcrmod.events` | `ChatEventHandler`, `PlayerEventHandler`, `CommandRegistrationHandler`, `ExternalModLoadingEvent`, `ServerMessageEvent` |
| `bfcrmod.utils` | `BetterForgeChatUtilities`, `IMetadataProvider`, `INicknameProvider`, `IntegratedNicknameProvider`, `loader` |
| `bfcrmod.utils.moddeps` | `LuckPermsProvider`, `FTBNicknameProvider` — optional-mod adapters |

### Data Flow — Chat Message

```
ServerChatEvent (HIGHEST priority)
  → ChatEventHandler.onServerChat()
    → Word filter (FilterManager)
    → Admin-chat / private-chat intercept (cancel + re-route)
    → Build format: chatMessageFormat ($time, $name, $msg)
    → TextFormatter.stringToFormattedText() — &-codes → MutableComponent
    → MarkdownFormatter (if enabled + permission)
    → ChatMessageManager.recordMessage() (reply system)
    → ServerMessageEvent.broadcastMessage()
```

### Config Reload Chain

```
/bfcrr reload  OR  ModConfigEvent.Reloading
  → ConfigurationEventHandler.reloadConfigOptions()
    → calls reloadConfigOptions() on every registered IReloadable
      (ChatEventHandler, PlayerEventHandler, BetterForgeChatUtilities, NickCommands, …)
```

### External-Mod Provider Pattern

`BetterForgeChat` holds two nullable interfaces:
- `IMetadataProvider metadataProvider` — provides prefix/suffix (impl: `LuckPermsProvider`)
- `INicknameProvider nicknameProvider` — provides nicknames (impl: `FTBNicknameProvider` or `IntegratedNicknameProvider`)

Both are wired in `ExternalModLoadingEvent.onServerStarted()`. If neither optional mod is present and `autoIntegratedNicknames=true` (config), `IntegratedNicknameProvider` is used instead.

### Feature Gating

Every subsystem is behind a `ConfigHandler.config.enable*.get()` guard — commands, event listeners, tick handlers, and server-start init all check their flag. Always follow this pattern for new features:

```java
if (ConfigHandler.config.enableMyFeature.get()) {
    MyFeatureCommands.register(dispatcher);
}
```

---

## Config Files at Runtime

| Path | Contents |
|---|---|
| `config/bfcrr/common.toml` | Primary mod settings (ForgeConfigSpec) |
| `<world>/serverconfig/bfcrr/announcements.json` | Announcement entries |
| `<world>/serverconfig/bfcrr/filters.json` | Word filter rules |
| `<world>/serverconfig/bfcrr/banned_items.json` | Banned item list |
| `<world>/serverconfig/bfcrr/bulletin.json` | Op-bulletin entries |
| `config/bfcrr/motd.json` | MOTD settings |

### Multi-file Config Rule

If a feature requires more than ~5 new keys, create a dedicated JSON/TOML file under `config/bfcrr/` (named `bfcrr Config/` on the wiki) and load it with a `Manager` class (see `AnnouncementManager`, `FilterManager`, `MotdManager` as reference). Do **not** keep cramming everything into `common.toml`.

---

## Text & Color Conventions

- **`&` codes** — `&0`–`&9`, `&a`–`&f` for colors; `&l` bold, `&o` italic, `&n` underline, `&m` strikethrough, `&k` obfuscated, `&r` reset.
- **Hex colors** — `&#RRGGBB` (requires `bfcrrmod.chat.colors.hex` permission).
- **Format placeholders** — `$name`, `$prefix`, `$suffix`, `$time`, `$msg`, `$sender`, `$receiver`, `$message` (varies by feature). Always document them in the `.comment()` call.
- **`TextFormatter.stringToFormattedText(String)`** — converts an `&`-coded string to a `MutableComponent`. Use this for every user-facing output.

---

## Permissions

All nodes are defined in `PermissionsHandler.java` using the `ezyPermission()` helper:
```java
public static PermissionNode<Boolean> myNode =
    ezyPermission("category.node", defaultValue, "Display Name", "Description");
```
Nodes are auto-registered via reflection — just declare the `public static` field. Format: `bfcrrmod.<category>.<node>`.

---

## Build & Dev Commands

```powershell
# Build the mod JAR
./gradlew build          # output: build/libs/bfcrmod-1.20.1-4.0.0.jar

# Run test server (working dir: runServer/)
./gradlew runServer

# Run test client (working dir: runClient/)
./gradlew runClient

# Re-run setup after mapping changes
./gradlew --refresh-dependencies
```

Config path on a dev server: `runServer/config/bfcrr/common.toml`

---

## Key Files for New Features

1. `ConfigHandler.java` — add `ConfigValue` fields; update comments with placeholders
2. `PermissionsHandler.java` — add permission nodes
3. `CommandRegistrationHandler.java` — register commands behind feature flag
4. `BetterForgeChat.java` — wire `IReloadable`, server-start init, tick hooks
5. `DOCUMENTATION.md` — update config table and command list

