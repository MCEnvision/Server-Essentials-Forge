# SEFPORTED

SEFPORTED is a server essentials mod for Minecraft 1.21.1 on NeoForge. It provides chat tools, moderation, vanish, announcements, virtual workstations, inventory administration, and other server management features through native Minecraft and NeoForge systems.

The project is the NeoForge 1.21.1 port and active development base for SEF 2. The current mod remains server only. Vanilla clients and clients without SEF can connect because the mod metadata uses `displayTest = "IGNORE_SERVER_VERSION"`.

Enhanced client GUIs described in [sef2.md](sef2.md) are planned and are not implemented yet.

## Current status

Current project metadata:

1. Minecraft `1.21.1`.
2. NeoForge `21.1.233`.
3. Java `21`.
4. Parchment mappings `2024.11.17`.
5. Mod id `sef`.
6. Artifact version `1.0-SNAPSHOT`.

This branch is under active development. Treat builds as test builds until a release is approved.

SEF 2 Phases 1 through 3 have implementation coverage in this branch. Public release acceptance remains pending until the authenticated multiplayer, optional integration, restart, crash recovery, and profiler cases in the manual matrices are completed. Homes, teleportation, later command families, enhanced client GUIs, and the wider EssentialsX parity roadmap remain planned.

## Current features

The current implementation includes:

1. Chat formatting, colors, styles, prefixes, suffixes, timestamps, and optional LuckPerms metadata.
2. Private messages, replies, clickable reply support, HelpOp, admin chat, and operator bulletins.
3. Integrated nickname and whois commands, configurable duplicate display names with authenticated username hover, and optional FTB Essentials nickname integration.
4. Scheduled text and command announcements, title announcements, per player announcement toggles, and countdowns.
5. Persistent mute and warning systems, freezing, inventory lock, building restrictions, alternate account checks, clear chat, and banned item controls. Mute countdown and banned item saves use coalesced background file writers with bounded shutdown flushing.
6. Inventory inspection with optional Curios support.
7. Vanish levels, per observer visibility levels, trace support, sound suppression, tab hiding, and optional Discord bridge compatibility.
8. MOTD management and configurable word filters.
9. Virtual `/craft`, `/anvil`, `/enchantingtable`, `/superenchantingtable`, and `/repair` commands with aliases, permissions, and cooldowns.
10. Central NeoForge permission evaluation, structured provider decisions, a generated permission manifest, action specific administrative permissions, runtime command tree refresh, and permission revocation handling.
11. Versioned JSON storage envelopes with atomic replacement, migration backups, a migration journal, corruption quarantine, status diagnostics, and bounded background exports.
12. Opt in alternate account correlation with salted address hashing, retention, redacted display, separately permissioned raw display, purge, export operations, coalesced background persistence, and corrupt salt refusal.
13. Structured security audit JSONL with the Phase 2 event schema, bounded asynchronous writes, size rotation, and retention.
14. Permission filtered Brigadier projection for private messaging, replies, HelpOp, admin chat, and announcement toggles, with message bodies excluded from ordinary SEF log records.
15. A sealed command catalog with canonical routes, permission requirements, source classes, feature ownership, target behavior, cooldown identity, audit class, fallback metadata, and conflict policy.
16. Shared feature, permission, target hierarchy, cooldown, warmup, confirmation, cost, audit, quota, message, identity, alias compilation, bundle compilation, panel descriptor, and command wrapper contracts.
17. `/sef commands [page]`, `/sef conflicts`, and `/sef doctor` diagnostics with permission filtered output.
18. Every currently executable `/sef` catalog action uses the same runtime feature, source, permission, cooldown, warmup, cost, confirmation, execution, and audit pipeline. Workstation convenience roots and aliases resolve to their canonical action and cooldown identity.
19. Finite quota tiers and optional LuckPerms metadata for future homes, player warps, targets, mail, and user definitions, with hard ceilings and reservation based race protection.
20. Versioned `location-history.json` and `cooldowns.json` repositories with bounded records, atomic writes, recovery mode, and shutdown flushing.
21. A UUID authoritative player profile repository that imports existing nickname data, retains authenticated usernames separately from display nicknames, coalesces background persistence, and drains through a bounded shutdown flush.

The full SEF 2 command and platform roadmap is documented in [sef2.md](sef2.md). Planned features must not be treated as available until they appear in this README and in [DOCUMENTATION.md](DOCUMENTATION.md).

## Security defaults

Every exposed command path is expected to use a permission node. The current security foundation adds these important defaults:

1. `/sef` subcommands use separate nodes. `/sef reload` and `/sef test` default to denied.
2. `/sef filter` uses `sef.filter.manage` instead of a generic operator level.
3. `/nickfor` uses `sef.commands.nick.others` and defaults to denied.
4. `/sudo` is not registered during stabilization, even when an older configuration still enables its module. The server warns about the ignored value.
5. Command announcements use a separate denied by default permission. Their command root must be explicitly allowlisted both when saved and when executed.
6. Persisted vanish state is removed or lowered when the player no longer has its required vanish permission.
7. Console and RCON sources require permission level `4` for permission service bypass. Command blocks do not receive a general bypass.
8. Moderation, MOTD, banned item administration, inventory tools, bulletins, and announcements use SEF permission nodes instead of generic operator level.
9. `/invsee` separates view, modify, Curios, offline, and other player ender chest capabilities. Mutation permission is checked again on every click.
10. Vanish administration applies exemption and hierarchy checks, then rechecks active state after provider refreshes, configuration reloads, dimension changes, respawns, reconnects, and once per second.
11. Convenience roots cannot weaken their canonical action. Workstation aliases share the same action identifier and therefore share permissions and cooldowns.
12. Alias and bundle definitions are compiled against the catalog before publication. Alias root ownership includes catalog, shortcut, and preexisting Brigadier roots, and the selected conflict mode is enforced. Unknown actions, collisions, recursion, raw command steps, policy weakening, and unbounded expansion are rejected.
13. Future collection and fan out systems receive finite defaults and hard ceilings. An optional provider failure falls back safely and is exposed by `/sef doctor`.

Review [DOCUMENTATION.md](DOCUMENTATION.md) before enabling administrative commands.

## Requirements

Server requirements:

1. Minecraft server `1.21.1`.
2. NeoForge `21.1.233`, or a compatible version within the declared NeoForge range.
3. Java `21`.

Optional integrations:

1. LuckPerms `5.4` or newer for permission provider and metadata integration.
2. FTB Essentials for nickname integration.
3. Curios for Curios inventory slots and banned item scanning.
4. MC2Discord, Playtime, or SDLink for supported vanish integrations.

The mod must start without optional integrations.

## Installation

1. Install NeoForge for Minecraft `1.21.1` on the server.
2. Place the built SEF JAR in the server `mods` directory.
3. Start the server once to create configuration files.
4. Stop the server and review `config/sef/common.toml` and the world server configuration before opening the server to players.
5. Install and configure LuckPerms when group based permission control is required.

Clients do not need the current SEF JAR.

## Configuration

Primary configuration:

1. `config/sef/common.toml` contains module toggles, messages, nickname limits, chat options, cooldowns, retained sudo policy for future migration, privacy settings, storage behavior, and performance limits.
2. `<world>/serverconfig/sef-vanish-server.toml` contains detailed vanish behavior.
3. `<world>/serverconfig/sef/*.json` contains feature data such as announcements, filters, mutes, warnings, banned items, alternate account data, and bulletins.
4. `config/sef/motd.json` contains the dedicated server MOTD configuration.
5. `<world>/serverconfig/sef/permission-manifest.json` contains the deterministic runtime permission catalog.
6. `<world>/serverconfig/sef/audit/security-audit.jsonl` contains structured sensitive action audit events.
7. `<world>/serverconfig/sef/location-history.json` contains bounded UUID keyed location history.
8. `<world>/serverconfig/sef/cooldowns.json` contains only cooldowns whose remaining duration meets the configured persistence threshold.

`/sef storage status` reports every managed document. `/sef storage export` queues a bounded snapshot under `<world>/serverconfig/sef/exports`. Alternate account data is excluded unless the issuer has both its export and raw address permissions.

The `commandKernel` section sets hard limits for aliases, bundle steps, nested bundle depth, targets, expanded target steps, per player location history, and the minimum cooldown duration persisted across restarts. See [DOCUMENTATION.md](DOCUMENTATION.md) for exact defaults and quota metadata.

NeoForge owns TOML loading and external reload notifications. `/sef reload` reapplies values already loaded by NeoForge. It does not force an arbitrary disk read.

Module toggles prevent their command registration or behavior when disabled. Existing server configuration values are retained across upgrades, so review old values after new secure defaults are introduced.

## Development

Clone the repository and use the checked in Gradle wrapper.

Linux and macOS:

```bash
./gradlew test
./gradlew build
./gradlew runServer
./gradlew runClient
```

Windows:

```powershell
gradlew.bat test
gradlew.bat build
gradlew.bat runServer
gradlew.bat runClient
```

The built JAR is written to `build/libs/`.

There is currently no dedicated formatter or static analysis task. Java changes must at minimum pass `test` and `build`, plus the relevant server or client smoke test.

## Repository layout

1. `src/main/java/com/enviouse/sef` contains common and server implementation.
2. `src/main/resources` contains mixin, language, and access transformer resources.
3. `src/main/templates` contains expanded NeoForge mod metadata.
4. `src/test/java` contains pure policy tests and NeoForge bootstrapped JUnit tests for Minecraft command, menu, permission, and lifecycle behavior.
5. `sef2.md` is the source of truth for unfinished SEF 2 work.
6. `DOCUMENTATION.md` contains maintainer and operator details.
7. `docs/PHASE_1_MANUAL_TESTS.md` contains the real client release approval matrix for Phase 1 behavior.
8. `docs/PHASE_2_3_MANUAL_TESTS.md` contains the operator, permission, restart, and recovery approval matrix for Phases 2 and 3.

## Support

Include these details with a reproducible issue:

1. Minecraft, NeoForge, Java, and SEF versions.
2. Installed optional integrations and their versions.
3. The relevant module configuration with secrets removed.
4. `logs/latest.log` and the complete crash report when present.
5. Exact commands, permissions, player roles, and reproduction steps.

Never publish Discord tokens, credentials, IP history, private message contents, or unredacted administrative logs.

## License

All Rights Reserved.
