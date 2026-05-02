# Changelog

## V1.1

### Fixed
- **Banned-items system rewritten.** The inventory scan was only walking the player's main `Inventory` container, so the cursor stack (`containerMenu.getCarried()`), any currently-open container's slots, and Curios slots were never checked — banned items survived in those locations until the player manually moved them back. The block sweep ran every 200 ticks (10 s) at radius 5 and was the *only* enforcement path, so banned blocks could sit placed for up to ten seconds before being destroyed, and there was no way to whitelist admin-placed copies.
  - `BannedItemsManager` now scans hotbar / main / armor / offhand + cursor + open container slots + Curios every tick for non-bypassed players.
  - `BlockEvent.EntityPlaceEvent` cancels banned placements at the source for instant feedback; non-player placers (dispensers, falling blocks) are also blocked.
  - Periodic block sweep is now a safety net for pre-existing / world-gen blocks. Default radius `6`, default interval `40 ticks` (2 s); both override-able at runtime via `/banned setradius` and `/banned setinterval`.
  - `PlayerInteractEvent.RightClickBlock` / `RightClickItem`, `LivingEntityUseItemEvent.Start`, and `EntityItemPickupEvent` now block use/pickup of banned items.

### Added
- **Per-entry ban metadata.** Each ban now carries a reason, banner name, duration (`infinite`, `30s`, `5m`, `1h30m`, `2d12h`), and an announce flag.
- **Pattern matching.** Bans accept exact registry ids (`minecraft:diamond`), mod-wide wildcards (`mekanism:*`), and tag references (`#forge:ores`) for both items and blocks.
- **Full `/banned` subcommand suite (op-only mutations):**
  - `/banned add <item> [duration] [announce] [reason...]`
  - `/banned addhand [duration] [announce] [reason...]` — bans the item currently in the executing player's main hand
  - `/banned remove <item>` (Brigadier suggests from the current ban list)
  - `/banned update <item> [duration] [announce] [reason...]`
  - `/banned list` / `/banned clear` / `/banned reload`
  - `/banned setradius <n>` / `/banned setinterval <ticks>`
  - `/banned toggle <items|blocks|drops|all>`
  - `/banned bypass <player> <on|off>`
  - `/banned scan <player>` (force-sweep one player's inventory now)
  - `/banned excepted` / `/banned excepted remove <index>` / `/banned excepted clear`
- **Read-only `/banned` for everyone.** Non-ops typing `/banned` see the ban list with hover tooltips showing reason, banner, duration, time-remaining, and announce flag.
- **Creative-placement exceptions.** When a player in creative (or with `sef.banned.bypass`) places a banned block, the position is auto-recorded as an exception and skipped by future sweeps. `/banned excepted` lists exceptions with click-to-tp links (`/execute in <dim> run tp @s x y z`).
- **Bypass system.**
  - Players in creative are always exempt from confiscation/sweeps.
  - New permission node `sef.banned.bypass` exempts staff regardless of game mode.
  - Manual list managed by `/banned bypass <player> on|off`, persisted to disk.
- **Curios write helper.** `CuriosInventoryHelper.clearMatching(player, predicate)` — reflection-safe and no-ops when Curios is absent.
- **Configurable confiscation/announce strings.** New config keys `bannedItemRemovedMsg` and `bannedAnnounceFormat` (placeholders: `$item`, `$reason`, `$by`, `$remaining`, `$player`). Blank values fall back to the built-in defaults so existing servers don't need a config edit.
- **New permission nodes.** `sef.commands.banned`, `sef.banned.bypass`.

### Changed
- `bannedBlockScanInterval` default lowered from `200` → `40` ticks (2 s).
- `bannedBlockScanRadius` default raised from `5` → `6`.
- `banned_items.json` schema upgraded to a structured object (entries / bypassed / excepted / settings). Old flat-array files are auto-migrated on first load.
