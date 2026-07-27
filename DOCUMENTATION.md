# SEFPORTED technical documentation

## 1. Scope and source of truth

SEFPORTED is the NeoForge 1.21.1 server essentials implementation that will evolve into SEF 2. Current source and tests define shipped behavior. [sef2.md](sef2.md) defines unfinished product requirements, architecture decisions, security findings, command plans, GUI plans, and phase acceptance criteria.

Use this source order when requirements appear to conflict:

1. The current user approved request.
2. `sef2.md` for planned behavior and unfinished work.
3. `gradle.properties`, `build.gradle`, and generated mod metadata for pinned platform versions.
4. Current implementation and tests for shipped behavior.
5. This document and `README.md`.

Do not describe a roadmap item as implemented until code, configuration, tests, and operational documentation agree.

SEF 2 Phases 1 through 7 have implementation coverage in the current branch. The Phase 6 and Phase 7 implementation audit passes the automated unit, GameTest, build, artifact, and dedicated-server gates recorded in `docs/PHASE_6_TESTS.md` and `docs/PHASE_7_TESTS.md`. Release verification is not complete. Authenticated multiplayer, packet-visible behavior, live provider mutation, deliberate filesystem and shutdown failures, and profiler cases in the phase matrices remain required before approval. Economy, enhanced GUI networking, and other later roadmap families remain planned.

## 2. Platform and toolchain

The project currently pins:

1. Minecraft `1.21.1`.
2. NeoForge `21.1.233`.
3. Java toolchain `21`.
4. Gradle Wrapper `8.8`.
5. ModDevGradle `2.0.141`.
6. Parchment mappings `1.21.1`, export `2024.11.17`.
7. JUnit `5.10.2`.
8. Mockito `5.12.0` for NeoForge bootstrapped command and menu tests.
9. LuckPerms API `5.4` as compile only.
10. FTB Essentials `2101.1.9` as compile only.
11. Curios `9.5.1` compatible artifact as compile only.

The mod id is `sef`. Preserve it because configuration paths, permission nodes, mixin identifiers, and existing server data depend on that namespace.

The current artifact is server only. Common initialization must never reference `net.minecraft.client` classes. Optional enhanced clients and GUI networking are roadmap work.

## 3. Initialization and lifecycle

`com.enviouse.sef.ServerEssentialsForge` is the `@Mod` entry point.

Construction performs these operations:

1. Stores the singleton instance used by current integration providers.
2. Registers configuration reload callbacks.
3. Registers common configuration at `sef/common.toml`.
4. Registers server vanish configuration at `sef-vanish-server.toml`.
5. Registers stateful event handler instances on `NeoForge.EVENT_BUS`.
6. Registers vanish commands and vanish permission nodes.

Command registration initializes and seals the kernel catalog, captures existing command roots, and registers canonical and convenience routes. Server startup opens the location history, cooldown, teleport, social, command-spy, moderation, and kit repositories under `<server>/serverconfig/sef`, loads the integrated player profile repository from the world player data directory, starts security audit and export workers, and writes the permission manifest. It creates the optional file-log worker and `logs/sef` tree only when file logging is enabled. It then loads enabled managers for announcements, filters, chat replies, operator bulletins, banned items, MOTD, alternate account data, warnings, and mutes. Optional integration detection also occurs during server startup.

Server ticks update announcements, banned item scans, persistent moderation expiry, jail enforcement, long-lived player-state authorization, countdown state, teleport expiry, teleport warmups, tab presentation, and scheduled reminders when their modules are enabled. Reminder definitions are snapshotted once per scheduler pass rather than once per player. Moderation release teleports are validated through the shared safe-teleport service. Mute and banned item changes create in memory JSON snapshots and submit them to coalescing daemon writers, so their tick paths do not perform filesystem access. Managed repository snapshots are captured on the server thread and written asynchronously. Vanish permission reconciliation occurs once per second on each online player through `VanishEventListener`.

Server shutdown drains mute, banned item, alternate account, player profile, location history, persistent cooldown, teleport, social, command-spy, moderation, and kit writers with bounded waits. It then closes the optional file sink with its configured bounded drain, writes an incomplete-session marker when necessary, stops optional integrations, clears warmups and confirmations, and clears runtime cooldown, observation, player-state, and vanish state. Coordinated repository writes run on a dedicated shutdown worker. A timed out worker blocks repository reuse by another world until it ends. Shutdown flush failures are logged rather than silently treated as successful.

## 4. Package map

Important package ownership:

1. `com.enviouse.sef.commands` owns `/sef`, nicknames, whois, and private message command registration.
2. `com.enviouse.sef.permissions` owns the central permission facade.
3. `com.enviouse.sef.announcements` owns scheduled announcements and announcement commands.
4. `com.enviouse.sef.chat` owns chat replies, HelpOp, admin chat, and operator bulletins.
5. `com.enviouse.sef.vanish` owns vanish state, visibility rules, command handling, events, integrations, and mixins.
6. `com.enviouse.sef.mute`, `warn`, and `freeze` own moderation state and commands.
7. `com.enviouse.sef.banned` owns banned item policy and persistence.
8. `com.enviouse.sef.invsee`, `invlock`, and `disablebuilding` own inventory and interaction administration.
9. `com.enviouse.sef.workstations` owns virtual workstation commands and cooldown tracking.
10. `com.enviouse.sef.teleport` owns homes, teleport requests, back history, spawn layers, server warps, player-hosted warps, RTP, direct teleports, safe destination validation, warmup completion, lifecycle cancellation, persistence, and FTB ownership modes.
11. `com.enviouse.sef.util` owns shared strict parsing utilities.
12. `com.enviouse.sef.events.CommandRegistrationHandler` assembles enabled command modules.
13. `com.enviouse.sef.kernel.command` owns catalog, shortcut, alias, bundle, wrapper, capability, and panel contracts.
14. `com.enviouse.sef.kernel.policy` owns shared feature, quota, hierarchy, execution, cooldown, warmup, confirmation, and cost policy.
15. `com.enviouse.sef.kernel.observation` owns immutable observation and sink contracts.
16. `com.enviouse.sef.identity` owns UUID authoritative profile resolution.
17. `com.enviouse.sef.message` owns bounded typed message templates and literal field insertion.
18. `com.enviouse.sef.storage.repository` owns coordinated Phase 3 repositories and recovery states.
19. `com.enviouse.sef.social` owns social preferences, mail, observation delivery, connection messages, reminders, custom text, and identity diagnostics.
20. `com.enviouse.sef.commandlog` owns command redaction, correlated lifecycle records, command-spy profiles, observer projection, fixed-path file logging, rotation, retention, search, export, and logger diagnostics.
21. `com.enviouse.sef.moderation` owns expanded bans, kicks, authoritative connection-address resolution, persistent warnings and controls, jail definitions, jail sentences, expiry, and enforcement.
22. `com.enviouse.sef.kits` owns versioned kit definitions, item serialization, claim policy, cooldown history, per-kit permissions, validation, and administrative commands.
23. `com.enviouse.sef.inventory` owns self and target inventory utilities, live ender-chest authorization, transient disposal, and safe item editing.
24. `com.enviouse.sef.player` owns runtime player utility state, authorization reconciliation, personal time and weather, experience, movement, and gamemode shortcuts.

Logical server state is authoritative. The current project has no custom client payload protocol.

## 5. Command registration

`CommandRegistrationHandler.registerCommands` registers the normal command set. `registerLowPriorityCommands` registers `/invsee` and private message aliases after vanilla and optional mods so SEF can intentionally replace those roots.

Module values in `ConfigHandler.ConfigBuilder` decide whether feature commands are registered. A disabled module must not leave a working command mutation path behind.

The current top level command families include:

1. `/sef`, `/colors`, `/nick`, `/nickfor`, and `/whois`.
2. `/msg`, `/tell`, `/w`, `/r`, `/ans`, `/helpop`, `/helpopop`, `/chat`, and `/ac`.
3. `/textannouncement`, `/commandannouncement`, `/titleannouncement`, and `/countdown`.
4. `/mute`, `/unmute`, `/mutelist`, `/warn`, `/warns`, `/freeze`, and `/unfreeze`.
5. `/invsee`, `/invlock`, `/disablebuilding`, `/db`, `/checkalts`, `/cc`, and `/clearchat`.
6. `/banned`, `/motd`, and `/opbulletin`.
7. `/vanish`, `/v`, and vanish trace or queue subcommands.
8. `/craft`, `/c`, `/anvil`, `/av`, `/enchantingtable`, `/et`, `/superenchantingtable`, `/set`, and `/repair`.
9. `/sef storage status`, `/sef storage export`, `/checkalts purge expired`, `/checkalts purge confirm`, and `/checkalts export`.
10. `/sef commands [page]`, `/sef conflicts`, and `/sef doctor`.
11. `/sef workstation craft`, `/sef workstation anvil`, `/sef workstation enchantingtable`, `/sef workstation superenchantingtable`, and `/sef workstation repair`.
12. `/sethome`, `/home`, `/homes`, `/delhome`, `/renamehome`, and `/homeadmin`, or their `sef`-prefixed coexistence roots.
13. `/tpa`, `/tpahere`, `/tpaccept`, `/tpdeny`, `/tpcancel`, `/tprequests`, `/tptoggle`, `/tpblock`, `/tpunblock`, `/tpblocked`, `/tpautoaccept`, and `/tpaall`.
14. `/back`, `/spawn`, `/setspawn`, `/spawninfo`, `/rtp`, `/tpr`, `/settpr`, `/tphere`, `/tpo`, `/tpohere`, `/tppos`, `/tpall`, and `/tpoffline`. SEF registers `/tp` only when ownership is explicitly enabled.
15. `/warp`, `/warps`, `/setwarp`, `/delwarp`, `/renamewarp`, `/warpinfo`, and server-warp management routes.
16. `/pwarp`, `/pwarps`, `/setpwarp`, `/delpwarp`, `/renamepwarp`, player-warp access, transfer, favorite, report, visit, home-conversion, and moderation routes.
17. `/msgtoggle`, `/rtoggle`, `/ignore`, `/ignorelist`, and the hardened `/msg`, `/tell`, `/w`, `/whisper`, `/r`, `/reply`, and `/pchat` routes.
18. `/socialspy` with status, recent, everyone, selected-player, match, scope, route-filter, and format-preview actions.
19. `/mail` with list, read, send, clear, delete, and archive actions.
20. `/joinmessage`, `/leavemessage`, and `/connectionmessage` for real connection templates.
21. `/reminders`, `/reminder`, and `/welcome` for player state, definitions, scheduling, and manual delivery.
22. `/customtext`, `/booktext`, `/rules`, `/info`, `/sef identity coverage`, and `/sef identity refresh`.
23. `/ban`, `/tempban`, `/pardon`, `/unban`, `/ban-ip`, `/banip`, `/tempban-ip`, `/tempbanip`, `/pardon-ip`, `/unban-ip`, `/unbanip`, `/kick`, `/kick-ip`, `/kickip`, `/kickme`, and `/kickall`.
24. `/warn`, `/warns`, `/clearwarnings`, `/mute`, `/unmute`, `/mutelist`, `/freeze`, `/unfreeze`, `/freezelist`, `/invlock`, `/disablebuilding`, and `/db` through the persistent moderation control domain.
25. `/setjail`, `/deljail`, `/jails`, `/jail`, `/unjail`, and `/jailedplayers`.
26. `/commandspy`, canonical `/sef commandspy`, canonical `/sef logging`, and the collision-aware `/loggerspy` shortcut.
27. `/kit`, `/kits`, `/showkit`, `/createkit`, `/delkit`, and `/kitreset`, with validation, metadata export, and policy editing below `/kit`.
28. `/clearinventory`, `/ci`, `/enderchest`, `/ec`, `/disposal`, `/more`, `/condense`, `/hat`, `/itemname`, `/itemlore`, `/itemdb`, `/book`, `/recipe`, and the self-only `/i`.
29. `/afk`, `/feed`, `/heal`, `/fly`, `/god`, `/rest`, `/speed`, `/exp`, `/ptime`, `/pweather`, `/near`, `/getpos`, `/compass`, `/depth`, `/top`, `/bottom`, and `/jump`.
30. `/gm`, `/gmc`, `/gms`, `/gmsp`, and `/gma`, with explicit target support only where the matching other-player permission is granted.
31. `/cartographytable`, `/grindstone`, `/loom`, `/smithingtable`, `/stonecutter`, `/workbench`, and `/wb`, plus their canonical `/sef workstation` routes.

`/sudo` is intentionally not registered in Phase 1. An existing `modules.sudo = true` value produces a startup warning and does not expose an execution route.

Aliases must reach the same executor, permission, module toggle, and cooldown policy as their canonical command.

## 6. Permission architecture

### 6.1 Permission service

`PermissionService` is the central facade over NeoForge `PermissionAPI`.

Current source behavior:

1. Online players use `PermissionAPI.getPermission`.
2. UUID based checks use `PermissionAPI.getOfflinePermission`.
3. An unavailable permission service fails closed.
4. Console and RCON sources without an entity require vanilla permission level `4`.
5. Command blocks and other nonplayer level `2` sources do not receive a general bypass.
6. LuckPerms remains optional. NeoForge selects the installed permission provider.
7. Structured permission decisions retain the permission id, outcome, provider source, default use state, hierarchy state, exemption state, subject class, and reason without exposing player supplied command arguments.
8. Permission refresh invalidates cached optional provider data and quota policy revisions, reconciles vanish state, refreshes the tab name, and sends a newly filtered Brigadier tree to the affected online player.

`PermissionsHandler.playerHasPermission` remains as a compatibility method and delegates to `PermissionService`. New implementation should call `PermissionService` directly.

### 6.2 Current sensitive permission defaults

Permission node ids below receive the `sef.` namespace.

Public or low risk defaults:

1. `commands.sef.allowed`, allowed.
2. `commands.sef.info`, allowed.
3. `commands.sef.colors`, allowed.
4. `commands.nick`, allowed.
5. `commands.whois`, allowed.
6. `commands.craft`, allowed.
7. `commands.anvil`, allowed.
8. `commands.enchantingtable`, allowed.
9. `commands.msg`, `commands.msgtoggle`, `commands.rtoggle`, `commands.ignore`, and `commands.ignorelist`, allowed.
10. `commands.mail` and `commands.mail.send`, allowed.
11. `commands.reminders`, `commands.reminder.dismiss`, and `commands.customtext`, allowed.

Administrative defaults:

1. `commands.sef.reload`, denied.
2. `commands.sef.test`, denied.
3. `filter.manage`, denied.
4. `commands.nick.others`, denied.
5. `commands.sudo`, denied.
6. `sudo.exempt`, denied.
7. `sudo.bypass.exempt`, denied.
8. `commands.vanish.others`, denied.
9. `commands.vanish.queue`, denied.
10. `commands.vanish.get.others`, denied.
11. `vanish.exempt`, denied.
12. `vanish.bypass.exempt`, denied.
13. `announcements.command.manage`, denied.
14. `opbulletin.manage`, denied.
15. `opbulletin.receive`, denied.
16. `motd.manage`, denied.
17. `commands.banned`, denied.
18. `banned.view`, allowed.
19. `commands.superenchantingtable`, denied.
20. `commands.repair`, denied.
21. `commands.invsee.view`, denied.
22. `commands.invsee.modify`, denied.
23. `commands.invsee.offline`, denied.
24. `commands.invsee.curios`, denied.
25. `commands.enderchest.others`, denied.
26. `alts.ip.view`, denied.
27. `alts.purge`, denied.
28. `alts.export`, denied.
29. `storage.status`, denied.
30. `storage.export`, denied.
31. `vanish.hierarchy.bypass`, denied.
32. `commands.sef.commands`, allowed.
33. `commands.sef.conflicts`, denied.
34. `commands.sef.doctor`, denied.
35. `kernel.gui.use`, `kernel.hud.use`, `kernel.panel.use`, `kernel.target.others`, `kernel.audience.broad`, `kernel.editor.use`, `kernel.alias.use`, `kernel.bundle.use`, `kernel.profile.use`, `kernel.bypass.use`, and `kernel.sensitive.view`, denied.
36. Finite quota tier nodes under `sef.homes.*`, `sef.playerwarps.*`, `sef.targets.*`, `sef.mail.*`, and `sef.definitions.*`, denied.
37. Every `commands.socialspy.*` management, audience, scope, route, recent, status, and format-preview node, denied.
38. `socialspy.view.metadata`, `socialspy.view.content`, `socialspy.view.vanished`, `socialspy.view.exempt`, `socialspy.hierarchy.bypass`, and `socialspy.exempt`, denied.
39. Every `commands.joinmessage.*`, `commands.leavemessage.*`, and `commands.connectionmessage.inspect` node is denied. `connectionmessage.hierarchy.bypass`, `connectionmessage.exempt`, and `connectionmessage.bypass.exempt` are also denied.
40. `commands.welcome.preview`, `commands.welcome.send`, `commands.reminder.manage`, and `commands.reminder.send`, denied.
41. `commands.customtext.manage`, `commands.sef.identity.coverage`, and `commands.sef.identity.refresh`, denied.
42. Expanded player, IP, kick, warning, control, jail, hierarchy-bypass, exemption-bypass, literal-address, and sensitive address-view permissions are denied, except self `/kickme` and self `/warns`.
43. Every command-spy management, audience, scope, filter, location, result, exempt-view, vanished-view, and argument-view permission is denied.
44. Every optional logging status, enable, disable, stream, live, recent, filter, session, format, tail, search, export, retention, and repair permission is denied. File logging also defaults to disabled in configuration.
45. Player kit use and listing are allowed by default. Kit creation, deletion, editing, validation, export, reset, hierarchy bypass, exemption bypass, and cooldown bypass are denied. Optional per-kit permissions are evaluated through dynamic `sef.kits.<id>` nodes.
46. Self clear-inventory, ender-chest, disposal, condense, hat, item identification, recipe, and safe player utility nodes may default to allowed. Other-player inventory, unsafe item editing, bounded item granting, gamemode, long-lived state, bypass, and super-enchant nodes default to denied.

Vanish level nodes remain `sef.vanish.1`, `sef.vanish.2`, and `sef.vanish.3`. Observer nodes remain `sef.vanishsee.1`, `sef.vanishsee.2`, and `sef.vanishsee.3`. Lower numeric levels are more powerful.

The canonical `/sef logging` category is visible only when the source has at least one logging action permission. It has no implicit root action. Callers must select an explicitly permissioned subcommand such as `/sef logging status`.

### 6.3 `/sef` authorization

`/sef` uses Brigadier literal children rather than a greedy string argument.

Each child applies its permission in `.requires`, which protects both command execution and Brigadier suggestions. Root access never implies mutation access. Every executable current `/sef` action has catalog ownership, including filter management, storage diagnostics and export, MOTD management, kernel diagnostics, and workstation actions.

`/sef filter add`, `remove`, and `list` currently share `sef.filter.manage`. A future read only filter node may split list access.

### 6.4 Shared command and policy kernel

Every kernel catalog entry declares a stable action id, canonical route, convenience roots, translation and usage keys, owner, feature id, required permissions, access class, allowed source classes, target behavior, cooldown identity, confirmation requirement, audit class, command fallback, conflict policy, and explicit GUI, HUD, and quota applicability.

The runtime kernel is sealed after registration. Missing capabilities, descriptors, or required metadata prevent sealing. `/sef commands` shows only entries whose complete permission set is currently granted. `/sef conflicts` reports active roots, SEF overrides, canonical only fallbacks, conflicts, and restart required structural changes. `/sef doctor` reports catalog validation, capabilities, policies, quotas, repositories, import failures, optional quota provider failures, and recovery mode.

Every currently executable `/sef` catalog action enters the shared execution pipeline. Canonical and convenience roots revalidate policy at execution and apply this order:

1. Resolve the current action policy revision.
2. Recheck the feature gate, source class, and hard deny state.
3. Recheck every catalog and shortcut permission and capture provider decision context.
4. Inspect the current canonical cooldown without mutating it.
5. Start or validate a warmup when configured.
6. Atomically acquire the canonical action cooldown.
7. Reserve cost through the configured provider.
8. Validate and consume a confirmation token when configured.
9. Execute the action.
10. Commit or roll back cost and the newly acquired cooldown.
11. Emit the structured result and full audit lifecycle event.

`CostService.Disabled` is the active provider, so current actions cannot charge an economy. Warmup and confirmation services are active contracts, but current action policies use zero warmup and no confirmation. Rejection after cooldown acquisition clears that acquisition, and rejection after a cost reservation refunds it. Permission provider refresh invalidates quota decisions, active warmups, and confirmation tokens for the affected actor before refreshing vanish and Brigadier command state. Aliases such as `/c`, `/av`, `/et`, and `/set` resolve to the same canonical cooldown id as their long form.

Alias publication is not exposed as an operator command yet. The Phase 2 compiler and revision registry reject unknown targets, recursive ids, ambiguous roots, unsupported adapters, weaker source or access policy, weaker audit policy, missing additional capabilities, stale revisions, and definition limit overflow. Publication resolves ownership across canonical catalog roots, configured shortcuts, and roots captured from the Brigadier dispatcher, then applies the definition conflict mode. A custom alias cannot replace a SEF catalog or shortcut root. `PREFER_SEF` can claim an external root, while canonical only, prefer existing, fail, and restart required collisions remain unpublished. Bundle execution is also not exposed yet. Its compiler rejects raw command steps, unknown action or bundle targets, cycles, excessive nesting, excessive steps, excessive target fan out, and expansion beyond configured bounds. Operator commands and GUI editors for these contracts remain Phase 11 work.

The wrapper contract separates initiator, effective source, root policy, scoped output, silence capability, correlation id, recursion depth, target list, and normalized parameters. It rejects recursive wrapper roots and nested wrapper origins before any dispatcher execution. Phase 2 does not register `/run`, `/silent`, or `/sudo`.

Quota resolution uses this precedence:

1. LuckPerms metadata when LuckPerms is installed and the user is available.
2. Explicit context metadata supplied by an authorized internal caller.
3. The highest finite permission tier.
4. An internal finite override.
5. The finite default.

All results are clamped to the quota hard ceiling. Reservations are atomic per subject and quota and count against concurrent remaining capacity until committed or closed. Optional provider exceptions are isolated, remove no data, fall back to the next safe source, and appear in `/sef doctor`.

Current quota contracts:

| Quota | Default | Hard ceiling | Permission tiers | LuckPerms metadata |
| --- | ---: | ---: | --- | --- |
| `sef:homes` | 1 | 1000 | `sef.homes.3`, `sef.homes.5`, `sef.homes.10` | `sef.limit.homes.total` |
| `sef:player_warps` | 5 | 1000 | `sef.playerwarps.10`, `sef.playerwarps.25` | `sef.limit.player_warps.total` |
| `sef:targets` | 1 | 1000 | `sef.targets.10`, `sef.targets.100` | `sef.limit.targets` |
| `sef:mail` | 100 | 10000 | `sef.mail.500`, `sef.mail.1000` | `sef.limit.mail` |
| `sef:definitions` | 64 | 1024 | `sef.definitions.256`, `sef.definitions.512` | `sef.limit.definitions` |

LuckPerms metadata accepts a nonnegative integer. `unlimited` is accepted only for quota definitions that explicitly allow it, and the hard ceiling still applies. Missing, negative, malformed, or unavailable metadata falls through to a finite source. LuckPerms is never required for startup.

Command quota contexts include every currently granted finite tier node from the permission manifest. Mail sends therefore honor `sef.mail.500` and `sef.mail.1000`, and reminder definition creation honors `sef.definitions.256` and `sef.definitions.512`, even when LuckPerms metadata is absent. Permission refresh invalidates quota decisions before later mutations.

### 6.4 Phase 4 teleport command enforcement

Phase 4 command mutations enter the canonical kernel action before changing repository state. This includes home set, delete, rename, and restore operations, server warp administration, player warp ownership and publication changes, favorites, reports, transfer changes, moderation changes, spawn changes, random teleport center changes, offline teleport queue changes, and teleport request preference changes. The kernel applies the registered feature, source, permission, cooldown, warmup, cost, target, and audit policy for the canonical action.

All migrated single player arguments use the shared SEF identity argument. It accepts authenticated usernames and unambiguous quoted display nicknames, obtains suggestions from the active nickname provider, and removes a vanished online player from both online and known profile resolution when the viewer cannot see that player. Direct private messages, private chat selection, home administration, warp sharing, teleport requests, offline teleport targets, social selection, connection message management, manual welcome delivery, inventory inspection, player inventory clearing, alternate account inspection, freezing, inventory locking, building control, muting, warnings, banned item actions, and every single target vanish action use this route.

`/tpaccept` uses the teleport action lease as its single canonical execution. It does not wrap that action in a second lease with the same cooldown identity. `/tpaall` resolves at most 100 visible targets first, then runs the bounded fan out through one `sef:teleport.request.all` action. Empty `/tprequests` output is a successful read rather than a provider failure.

Shared player target decisions use the selected metadata provider. A LuckPerms primary group weight is authoritative when present. Operators use the maximum bounded weight, known group names fall back to the configured hierarchy snapshot, console bypasses hierarchy and target exemption, and player bypasses require their explicit hierarchy or exemption permission. `/homes <player>` now uses the same target policy as home administration.

### 6.5 Phase 6 moderation and observation enforcement

Phase 6 routes use the sealed catalog and the ordinary kernel execution pipeline. They do not grant authority from vanilla operator level alone.

Moderation applies these boundaries:

1. Online targets use shared hierarchy, exemption, vanish-visibility, and execution-time permission checks.
2. Offline identities use UUID-authoritative profile resolution and exemption checks. An ambiguous nickname or unknown identity fails without mutation.
3. Permanent and temporary player bans use the vanilla user-ban list as the enforcement authority. SEF does not maintain a second player-ban truth.
4. IP bans use the vanilla IP-ban list. SEF stores only moderation controls, warnings, jail definitions, and jail sentences in `moderation.json`.
5. `ConnectionAddressService` supports `direct`, `trusted_proxy`, `external`, and `disabled`. Trusted proxy and external integrations register a bounded `ConnectionAddressService.Adapter` with an id, provider mode, priority, and binary IPv4 or IPv6 result. The highest priority adapter for the selected mode is authoritative. Duplicate, malformed, direct mode, disabled mode, excessive priority, and excessive count registrations fail. Missing results, adapter exceptions, absent adapters, and shared proxy uncertainty fail closed. Player entered literal addresses require both configuration opt in and a distinct permission.
6. Raw addresses never enter ordinary command feedback, kernel parameters, command spy, optional file logs, or broad audit. IP action records use a keyed fingerprint or redacted provider reference.
7. Shared-address and mass-kick actions resolve a bounded target set, bind the actor and policy revision to a short-lived confirmation token, and recheck current targets before disconnecting them.
8. Persistent mute, freeze, inventory lock, build lock, and jail state is enforced by events and reconciled on login and tick. Jail enforcement also runs after respawn and dimension changes, and jailed players cannot interact with entities. Inventory lock blocks container clicks, recipe placement, creative slot updates, pick item, offhand swaps, item drops, pickup, and item use. Expired jail sentences return the player to the recorded release location through safe teleport validation.
9. The earlier legacy warning, mute, freeze, inventory lock, and build lock managers stop owning behavior while expanded moderation is enabled. Their files remain untouched for compatibility and rollback. Disabling expanded moderation immediately removes repository derived runtime freeze mirrors while retaining the persistent control records for later re enablement.

Address adapters are runtime integration points. An optional server mod registers one adapter during its common server lifecycle and unregisters the same id during shutdown or integration reload:

```java
ConnectionAddressService.registerAdapter(new ConnectionAddressService.Adapter() {
    public String id() {
        return "example:proxy";
    }

    public ConnectionAddressService.ProviderMode mode() {
        return ConnectionAddressService.ProviderMode.TRUSTED_PROXY;
    }

    public int priority() {
        return 100;
    }

    public Optional<ConnectionAddressService.ProvidedAddress> resolve(ServerPlayer player) {
        return Optional.of(new ConnectionAddressService.ProvidedAddress(authoritativeAddressBytes(player)));
    }
});
```

`resolve` runs on the logical server path and must be bounded, nonblocking, and free of filesystem or network access. It returns exactly four IPv4 bytes or sixteen IPv6 bytes. SEF copies the bytes, normalizes the address internally, and does not expose raw address strings through adapter health or normal command output. Integrations must call `ConnectionAddressService.unregisterAdapter("example:proxy")` when their provider becomes unavailable.

`CommandEventJournal` creates immutable redacted observations with correlation, source type, origin, actor, effective actor, canonical action, lifecycle stage, result, dimension, and optional bounded location. Each observer profile is UUID keyed and contains requested state, audience, selected players, actor relation, source scopes, root and action filters, typed source, player, result, world, and origin filters, and projection preferences. Selection, delivery, and every later event recheck root permission, scope permission, hierarchy, exemptions, vanished-player visibility, and metadata or sensitive-field permission.

`CommandRedactionPolicy` runs before any observation consumer. Password and token roots become secret records. Private message, HelpOp, admin chat, staff chat, and team chat roots hide their bodies. Command wrappers such as `/execute`, `/run`, `/silent`, `/sudo`, `/function`, and `/schedule` hide the complete nested command. `/data` hides its arguments. Every IP moderation alias hides the full address and reason, and unknown roots retain only the root. Newlines, ISO controls, and Unicode format controls normalize to bounded whitespace before root classification, so they cannot join a sensitive root to its body or disguise it as an unknown root. Raw command text is not a field in a journal or file record.

`FileLogSink` is optional and independent from mandatory security audit. When enabled, it owns only `<server>/logs/sef`, uses immutable redacted records, a bounded queue, batched writes, maximum record size, rotation by size or age, archive count and total-byte retention, and a bounded shutdown drain. Search, tail, and export operate on owned redacted records. Capture filters cannot suppress mandatory audit. Retention cleanup requires a preview and confirmation token bound to the exact archive set and policy revision. Filesystem operations normalize paths, refuse symbolic-link traversal, and never accept operator-supplied paths. A writer failure creates an incomplete-session marker. An existing marker keeps the sink degraded across enablement until an operator acknowledges repair.

### 6.6 Phase 7 inventory and player utility enforcement

Phase 7 inventory mutations are server authoritative and transactional where a partial change could lose or duplicate items.

1. Kit definitions serialize complete `ItemStack` state through the registry aware codec. Claims validate the definition, current registry, permission, optional per kit permission, cooldown, one time policy, and inventory capacity before mutation. The repository repeats stale definition, deletion, cooldown, and one time checks while holding its monitor at usage commit. Loading rejects invalid metadata, orphan use records, duplicate records, and per player use history above the configured hard bound.
2. With overflow dropping disabled, a kit claim is atomic and refuses insufficient capacity. With overflow dropping enabled, only the bounded remainder is created in the player world after inventory insertion. Inventory state is synchronized before the usage record commits. Any earlier failure restores the full inventory snapshot, discards overflow entities, and sends a full menu resynchronization. No rollback capable operation runs after the usage record commits.
3. Live InvSee and ender-chest menus capture their authorization and configuration revision. Each click rechecks current permission, feature state, target policy, and revision. Revoked modification access downgrades InvSee to read only. Revoked view access or a changed live policy closes the menu. InvSee registers cooperatively under an existing Brigadier root and never reflectively deletes another mod’s node.
4. `/disposal` uses a transient server menu whose contents are intentionally destroyed on close. It does not persist or write another inventory.
5. Item name, lore, and book mutations apply configured length and line bounds. `/more` respects the item stack maximum. `/condense` uses current recipe results and commits only validated replacements.
6. `/i` accepts an item id with or without the `minecraft` namespace, is strictly self only, applies `itemGiveMaximumAmount`, checks registry resolution and inventory insertion, and rolls back a failed grant.
7. Gamemode shortcuts and parsed `/gm` routes separate self and other-player permissions. An operator with only target permissions can reach target grammar without receiving self authority. Target mutations use the same eligible-target policy as other administrative actions.
8. Long-lived fly, god, personal time, and personal weather state is reconciled against current permissions after refreshes and during bounded event checks.
9. Virtual workstations use vanilla menu types, so the current implementation remains server only. Canonical and shortcut routes share one action id, permission, feature gate, cooldown identity, and audit policy.
10. Super enchanting snapshots the registry and configuration policy at open time, validates again before mutation, enforces `1 <= minimum <= maximum <= 255`, permits zero only as removal, and refuses missing enchantments, invalid targets, unsafe policy changes, invalid bounds, or stale menus.

## 7. Sudo stabilization boundary

Phase 1 does not register `/sudo`. The old implementation class and configuration keys remain only to preserve source and configuration compatibility while the later secured sudo phase is built.

Operational behavior:

1. No Brigadier command node is registered.
2. No alias, panel, announcement, or indirect SEF route can reach the old executor.
3. `modules.sudo = true` produces a warning explaining that the value is ignored.
4. Existing sudo permission nodes and allow or deny configuration values are retained for migration. They do not enable execution.
5. A dedicated server smoke test verifies that `sudo say should_not_run` returns an unknown command.

Do not expose the legacy `SudoCommand` class from new code. The later sudo phase must implement the split operation permissions, hierarchy, exemptions, confirmation, rate limits, source policy, structured audit, and Brigadier projection specified in `sef2.md`.

## 8. Command announcement containment

Text and command announcements use separate typed record collections in the versioned announcements document. Legacy mixed records are imported into the correct collection during migration.

Current controls:

1. Management uses `sef.announcements.command.manage`, separate from text announcement management.
2. `commandAnnouncementAllowedCommands` defaults to empty, which denies every command root.
3. `commandAnnouncementDeniedCommands` always wins over the allowlist.
4. The same policy runs when a definition is created and every time the scheduled command fires.
5. Legacy definitions that no longer pass policy remain stored but do not execute.
6. Commands execute from the server command source only after policy approval.
7. Audit output records definition id, root, and command length without arguments.
8. Invalid text announcement targets skip delivery instead of widening to every player.
9. Interval and offset conversion detects tick overflow.
10. Command records declare the server source policy explicitly. No player or elevated synthetic source is inferred.
11. Type specific modification and removal cannot cast a text record to a command record or the reverse.
12. Dispatch is not audited as success. The server command callback records success or failure, and a dispatch without a synchronous callback is recorded as outcome unknown until a callback arrives.

Future source profiles, hierarchy, and confirmation workflows remain later phase work.

## 9. Nickname policy

Integrated nickname mutation enforces:

1. Self nickname permission and separate other player permission.
2. Separate color and style permissions.
3. Visible length after stripping supported `&` and section sign formatting.
4. Legacy color, legacy style, and six digit hex formatting recognition.
5. Unicode NFKC normalization and locale independent lowercase comparison.
6. Rejection of control, Unicode format, surrogate, private use, and unassigned code points.
7. Collision checks against online and known offline usernames and nicknames.
8. Exact usernames take precedence during whois and target lookup.
9. Ambiguous legacy nickname matches do not select the first player silently.
10. When `nicknameAllowDuplicateWithUsernameHover` is enabled, duplicate display names are allowed and the vanilla text component hover identifies the authenticated username. Disabling the option restores collision rejection and removes the hover.

Integrated identities are stored by UUID in `sef.playerdata.json` with the last known username, nickname, and update time. Legacy `sef.playerdata` is backed up, journaled, parsed once, and migrated. `/whois` and `/nickfor` can resolve unambiguous known offline identities. The integrated provider refuses writes when FTB Essentials is selected as nickname owner. Existing explicit grants of `commands.nick.others` remain effective after its default changed, and startup emits a migration warning instead of silently revoking them.

`commands.nick.others` now defaults to denied.

Online resolution and suggestions obtain the nickname from the selected nickname provider instead of assuming the integrated repository is active. `/nickfor` and `/whois` use the same UUID authoritative identity service as other migrated command targets. This keeps FTB Essentials ownership singular while still supporting display name suggestions and quoted display name input.

### 9.1 Phase 5 social and identity services

`SocialRepository` owns one versioned `social.json` document. Its independent collections contain player social preferences, mail records, connection templates, reminder definitions, reminder acknowledgement state, and custom text pages. All player relationships and ownership fields use authenticated UUIDs. User-facing names are resolved only for presentation.

Private messaging preserves the existing `msgSentFormat` and `msgReceivedFormat` configuration. Legacy `$sender`, `$receiver`, and `$message` placeholders are converted to typed placeholders before compilation. The message body is inserted as a literal component and is never parsed as formatting or another placeholder. Ordinary kernel audit contains only route and length metadata. Ordinary log messages contain sender, recipient, and character count, never the body.

`/msgtoggle`, `/rtoggle`, `/ignore`, and `/ignorelist` persist owned preferences. A recipient message toggle or recipient-side ignore entry returns the same unavailable response so the sender cannot distinguish the policy. Reply relationships and private-chat mode remain session state and are cleared on logout.

`ObservationService` implements `PrivateMessageObservationAdapter`. External adapters must supply one stable event UUID, a stable route id, the authenticated sender and recipient, and a typed content component. Calls from outside the logical server thread are rescheduled and re-resolve both players by UUID. The service deduplicates event UUIDs for a bounded five-minute window, limits delivery per observer and second, and keeps only the configured number of already-authorized recent components.

Every social-spy delivery rechecks:

1. The social and social-spy feature switches.
2. The observer requested state.
3. Root and metadata permissions.
4. Everyone or selected-player scope permission.
5. Sender, recipient, or either matching.
6. Route filters.
7. Sender and recipient exemption state.
8. Observer-specific vanish visibility.
9. Sender and recipient hierarchy against the observer.
10. Content permission when content was requested.
11. Per-observer delivery rate.

Selected player configuration also checks vanish visibility, subject exemption, and hierarchy before storing a UUID. Unauthorized lookup uses the same unavailable response as an unknown player. Metadata only observers receive `[content hidden]`. Content never enters the persisted social profile or ordinary command audit. Every delivered observation emits a separate security audit event containing the observer UUID, sender UUID, recipient UUID, route, metadata or content scope, redaction class, and private content redaction rule id when applicable. The event never contains the private message body. `/socialspy recent` is session only and logout removes it. `/socialspy format preview` compiles the configured template against `{from}`, `{to}`, `{message}`, `{route}`, and `{timestamp}` and inserts typed sample values.

Mail is addressed by recipient UUID and indexed by recipient in memory. Sending applies ignore policy, configured length and retention, the `sef:mail` quota, every granted mail quota tier permission, a global hard ceiling, and a recipient mailbox count. List, read, archive, delete, and clear operations can mutate only the authenticated recipient’s records. Expired mail is omitted from views and quota use. Mail bodies remain inside the owned social repository and player facing delivery.

Connection message mixins replace the real vanilla join and leave broadcast components. Templates accept only `{player}`, `{username}`, `{uuid}`, and `{world}`. `{player}` is the selected provider’s formatted display component. Set, clear, preview, and inspect actions require separate permissions, hierarchy approval, and exemption approval. The generated component is associated with its subject by object identity, not structural component equality, so equal looking simultaneous messages cannot inherit another player’s vanish state. Correlation keys and player references are weak, stale entries are pruned, and the map has a 2048 entry hard bound. The outbound packet filter suppresses a correlated message for recipients who cannot see its vanished subject.

Reminder definitions use `{player}`, `{username}`, and `{unread_mail}`. Definitions have stable ids, enabled state, audience, repeat seconds, maximum deliveries, dismissal policy, acknowledgement revision, actor UUID, and update time. Updating message, audience, repeat, or maximum delivery advances the acknowledgement revision. Player state records the last delivery, count, dismissal, and acknowledged revision. Definition creation applies the `sef:definitions` quota and every granted definition quota tier permission. Scheduler passes snapshot definitions once and applies delivery state without filesystem access in the tick path.

Custom text ids and observation routes use normalized ids matching `[a-z0-9][a-z0-9_.-]{0,63}`. Text content, templates, mail, selected UUIDs, ignore UUIDs, routes, profiles, definitions, and state collections all have hard bounds.

Nickname ownership remains singular. FTB Essentials owns nickname mutation when its provider is selected. Otherwise the integrated UUID profile owns it. A successful nickname mutation refreshes the online tab projection immediately. Chat, tab, NeoForge display-name components, SEF identity resolution, connection messages, and SEF feedback use provider-approved display components. Vanilla Brigadier player arguments and signed-chat authentication continue to use authenticated identities. `/sef identity coverage` reports this boundary, and enhanced in-world nametags remain the explicit Phase 9 client contract.

## 10. Duration syntax

`com.enviouse.sef.util.DurationParser` is the canonical parser for announcements, countdowns, mutes, freezes, and warnings.

Accepted finite examples:

```text
90
30s
5m
1h30m
2d 4h
1w2d3h4m5s
```

Supported units are weeks, days, hours, minutes, and seconds. Bare positive integers remain seconds for compatibility.

Permanent values are accepted only when the caller enables them:

```text
permanent
perm
forever
infinite
inf
```

The parser rejects:

1. Empty input.
2. Zero and negative values.
3. Missing or unknown units.
4. Duplicate units.
5. Trailing garbage.
6. Parse overflow.
7. Tick or millisecond scale overflow.
8. Permanent values for finite only callers.

Invalid values use `DurationParser.INVALID_VALUE` at legacy manager boundaries. Commands must reject that value before mutating state. Invalid moderation input must never become a permanent punishment.

## 11. Vanish security and reconciliation

Vanish state uses a server map keyed by player UUID plus persisted player NBT fields named `Vanished` and `VanishLevel`.

`VanishPermissionPolicy.reconcileLevel` applies these rules:

1. Persisted unvanished state resolves to level `0`.
2. No current vanish permission resolves to level `0`.
3. Invalid stored levels resolve to the strongest currently allowed level.
4. Loss of a stronger permission lowers concealment to the strongest level still allowed.
5. Gaining a stronger permission does not silently increase concealment.

Online state is rechecked once per second. It is also rechecked on login, LuckPerms user data refresh, configuration reload, dimension change, and respawn. Permission removal clears persisted vanish or lowers its level and resynchronizes player information and entity visibility.

Administrative target operations have separate permissions for other players, queued targets, and inspecting other players. `/v queue <player>` requires both `sef.commands.vanish.queue` and `sef.commands.vanish.others` at Brigadier projection time and repeats the combined check before execution. Exempt targets require the bypass node. A player source cannot act on a higher privilege target unless it has `sef.vanish.hierarchy.bypass`. Console remains authoritative.

Visibility packet decisions remain per observer. `VanishVisibility` defines the core level matrix. `VanishListProjection` always produces an independent immutable recipient projection before player information or server status data is rebuilt. `VanishLifecyclePolicy` prevents server configuration reads during packet and status ping shutdown races.

The unsafe offline queue route is disabled. Queue requests for online targets are applied immediately through normal hierarchy and permission checks. A future persistent queue must store authenticated issuer context and revalidate it on application.

When the vanish module is disabled, runtime and persisted vanish state are actively cleared and player visibility is restored. Recipient specific tab filtering builds a new packet for each recipient instead of mutating a shared outbound packet.

### 11.1 Inventory inspection authorization

`/invsee` requires `sef.commands.invsee.view` or the retained legacy root node. The remaining capabilities are independent:

1. `sef.commands.invsee.modify` permits inventory mutation.
2. `sef.commands.invsee.curios` permits loading and displaying Curios slots.
3. `sef.commands.invsee.offline` reserves offline target access. Offline inventory loading is not implemented yet.
4. `sef.commands.enderchest.others` reserves access to another player’s ender chest. That route is not implemented yet.

The menu checks view and Curios permissions while open. It closes when view access is lost or when a Curios page is no longer authorized. Mutation permission is checked on every click, including collect to cursor behavior, and the menu downgrades to read only immediately after revocation. Audit entries identify issuer, target, page, slot, and click type without serializing item NBT.

`invSeeDisableFtbInvsee` controls cooperative collision behavior. When false, SEF leaves an existing `/invsee` root untouched. When true, SEF adds its `player` route beneath the existing root while preserving every existing child. It does not use reflection or remove another owner’s command.

## 12. Configuration

### 12.1 Common configuration

NeoForge loads `config/sef/common.toml`.

The `modules` section controls feature registration. Detailed sections control messages, cooldowns, chat formatting, nicknames, announcements, moderation, inventory tools, workstations, retained sudo migration values, privacy, audit retention, tab refresh rate, and bounded banned item scanning.

The `commandKernel` section supplies hard limits used by Phase 2 and Phase 3:

| Key | Default | Allowed range | Purpose |
| --- | ---: | ---: | --- |
| `maximumAliases` | 256 | 1 to 1024 | Maximum operator alias definitions retained by the revision registry |
| `maximumBundleSteps` | 64 | 1 to 256 | Maximum direct steps in one bundle |
| `maximumBundleDepth` | 4 | 1 to 8 | Maximum nested bundle depth |
| `maximumTargets` | 100 | 1 to 1000 | Maximum resolved targets in one bundle |
| `maximumTargetSteps` | 2000 | 1 to 100000 | Maximum expanded target and step operations |
| `locationHistoryEntries` | 20 | 1 to 100 | Maximum retained location records per player |
| `persistentCooldownMinimumSeconds` | 60 | 0 to 86400 | Minimum remaining cooldown written across restarts |

These values are defensive ceilings, not permission grants. Lowering a structural alias or bundle limit requires a restart because dispatcher shape does not mutate during a configuration reload. Current workstation cooldown durations continue to come from `virtualWorkstations`.

The `virtualWorkstations` section includes these super-enchanting bounds:

| Key | Default | Allowed range | Purpose |
| --- | ---: | ---: | --- |
| `superEnchantingMinLevel` | 1 | 1 to 255 | Lowest nonzero enchantment level applied |
| `superEnchantingMaxLevel` | 10 | 1 to 255 | Highest enchantment level applied |

The relationship `superEnchantingMinLevel <= superEnchantingMaxLevel` is validated before opening or mutating a menu. Invalid relational values fail closed even though each individual value is inside its NeoForge range.

The `socialEssentials` section controls Phase 5 presentation and bounds:

| Key | Default | Allowed range | Purpose |
| --- | --- | --- | --- |
| `socialSpyFormat` | `&8[&b{from}&8] &7-> &8[&d{to}&8]&7: &f{message}` | Typed template limit | Social-spy presentation |
| `socialSpyRecentLimit` | `50` | 0 to 500 | Authorized recent components retained per observer session |
| `socialSpyEventsPerSecond` | `100` | 1 to 1000 | Maximum observation deliveries per observer and second |
| `privateMessageMaximumLength` | `2048` | 1 to 16384 | Private-message input bound |
| `mailMaximumLength` | `2048` | 1 to 16384 | Mail input bound |
| `mailRetentionDays` | `30` | 1 to 3650 | Mail expiry |
| `defaultJoinMessage` | `&e{player} joined the game` | Typed template limit | Default real join component |
| `defaultLeaveMessage` | `&e{player} left the game` | Typed template limit | Default real leave component |
| `optionalClientReminder` | Configured command-fallback notice | Message template limit | Login notice until Phase 9 capability negotiation can target fallback clients |

Module keys `social_essentials`, `social_spy`, `mail`, `connection_messages`, `reminders`, and `custom_text` control registration at startup. The same values publish shared runtime feature gates, so an already-registered action is denied after a configuration reload disables its subsystem.

The `moderation` section controls Phase 6 authority and observation bounds:

| Key | Default | Allowed range or values | Purpose |
| --- | --- | --- | --- |
| `maximumReasonLength` | `512` | 1 to 2048 | Maximum persisted moderation reason |
| `maximumMassTargets` | `100` | 1 to 1000 | Hard bound for one mass action |
| `addressProvider` | `direct` | `direct`, `trusted_proxy`, `external`, or `disabled` | Authoritative connection address source, validated live reload |
| `allowLiteralPlayerAddresses` | `false` | Boolean | Enables separately permissioned player-entered addresses |
| `allowLiteralConsoleAddresses` | `true` | Boolean | Enables literal console address input |
| `sharedAddressHardCap` | `10` | 1 to 100 | Maximum sessions resolved from one address |
| `confirmationSeconds` | `30` | 10 to 300 | Lifetime of state-bound mass-action confirmations |
| `failOnSharedProxy` | `true` | Boolean | Rejects likely unconfigured shared-proxy actions |
| `commandSpyRecentLimit` | `4096` | 32 to 65536 | Maximum redacted journal records |
| `commandSpySelectedLimit` | `32` | 1 to 256 | Maximum selected UUIDs per observer |
| `commandSpyEventsPerSecond` | `100` | 1 to 1000 | Per-observer delivery limit |

The `fileLogging` section controls the optional fixed-path sink. `enabled`, `connectionEvents`, and `textMirror` default to `false`. Queue capacity defaults to `8192`, batch size to `128`, flush interval to `1000` milliseconds, maximum record size to `16384` bytes, active file size to `64` MiB, active file age to `24` hours, retention to `30` days, archive count to `100`, total retained bytes to `1024` MiB, and shutdown drain to `10` seconds. Every value has a NeoForge hard range. Logging filters, live state, and stream state are runtime controls; they do not alter mandatory security audit.

The `phaseSevenUtilities` section controls Phase 7:

| Key | Default | Allowed range | Purpose |
| --- | --- | --- | --- |
| `cartographyTable`, `grindstone`, `loom`, `smithingTable`, `stonecutter` | `true` | Boolean | Register each additional vanilla workstation |
| `cooldownSeconds` | `0` | 0 to 31536000 | Shared ordinary utility cooldown |
| `itemGiveMaximumAmount` | `64` | 1 to 6400 | Maximum `/i` amount |
| `maximumKits` | `128` | 1 to 1024 | Stored kit definition bound |
| `maximumKitItems` | `256` | 1 to 1024 | Item-stack bound per kit |
| `maximumKitUsesPerPlayer` | `256` | 1 to 1024 | Retained claim records per player |
| `kitDropOverflow` | `false` | Boolean | Allow bounded world drops instead of atomic capacity refusal |
| `requirePerKitPermission` | `false` | Boolean | Require dynamic `sef.kits.<id>` permission |
| `suicide` | `false` | Boolean | Register the self-only suicide route |
| `maximumFlySpeed`, `maximumWalkSpeed` | `10.0` | 0.1 to 10.0 | Maximum accepted speed multiplier |

Alternate account collection uses three important values under its section:

1. `collectAddresses`, default `false`.
2. `hashAddresses`, default `true`.
3. `retentionDays`, default `30`.

Banned block background scanning defaults to disabled. Event driven enforcement remains active. When enabled, `bannedBlockScanBudget` caps inspected positions per tick, `bannedBlockScanInterval` controls sweep cadence, and unloaded chunks are never forced.

`ConfigurationEventHandler` reacts to NeoForge loading and reloading events. `/sef reload` reapplies the values currently held by NeoForge. It cannot construct NeoForge internal loaded configuration objects and does not force a manual TOML read.

### 12.2 Vanish configuration

NeoForge loads `sef-vanish-server.toml` as a server configuration, normally under the active world `serverconfig` directory.

It controls player list hiding, world hiding, fake join and leave messages, sound behavior, chat behavior, integrations, trace behavior, and related vanish presentation.

The common `modules.vanish_system` value is the master kill switch. When false, vanish mixins treat players as visible, runtime state is cleared, persisted state is cleared for online players, and vanish state is not restored on login.

### 12.3 Validation and upgrades

NeoForge `defineInRange` validates numeric configuration values such as nickname limits, cooldowns, super enchanting level, scan interval, scan budget, privacy retention, audit retention, audit file size, and tab refresh interval.

Existing TOML entries override changed defaults. Operators must review `modules.sudo`, alternate account collection, banned block scanning, and sensitive feature values after upgrading an existing server. A true sudo value remains ignored in this phase.

Configuration reload publishes a new immutable feature and command policy revision, then invalidates quota decisions. Existing in flight confirmation tokens bind to the policy revision that created them and cannot approve a changed action policy.

The configuration system does not yet have the complete SEF 2 schema version and migration report framework. Phase 3 import outcomes are available in memory through `/sef doctor`.

## 13. Persistent data

Current stores include:

1. `<world>/serverconfig/sef/announcements.json`.
2. `<world>/serverconfig/sef/announcement_prefs.json`.
3. `<world>/serverconfig/sef/filters.json`.
4. `<world>/serverconfig/sef/mutes.json`.
5. `<world>/serverconfig/sef/warns.json`.
6. `<world>/serverconfig/sef/banned_items.json`.
7. `<world>/serverconfig/sef/alt_data.json`.
8. `<world>/serverconfig/sef/bulletin.json`.
9. `config/sef/motd.json`.
10. `sef.playerdata.json` under the player data directory used by the integrated nickname loader.
11. `<world>/serverconfig/sef/permission-manifest.json`.
12. `<world>/serverconfig/sef/audit/security-audit.jsonl`.
13. Vanish state in persistent player NBT.
14. `<world>/serverconfig/sef/location-history.json`.
15. `<world>/serverconfig/sef/cooldowns.json`.
16. `<world>/serverconfig/sef/teleports.json`.
17. `<world>/serverconfig/sef/social.json`.
18. `<world>/serverconfig/sef/command-spy.json`.
19. `<world>/serverconfig/sef/moderation.json`.
20. `<world>/serverconfig/sef/kits.json`.
21. Vanilla `banned-players.json` and `banned-ips.json` for authoritative player and address bans.
22. Optional `<server>/logs/sef` command, connection-event, archive, export, text-mirror, and session-state files.

Managed JSON documents use an envelope with `domain`, `schemaVersion`, and `data`. Unknown fixed record fields survive a load and save cycle. Dynamic maps preserve unknown fields on retained records without restoring records that were intentionally removed.

Storage guarantees:

1. Writes use a same directory temporary file, flush the file channel, and atomically replace the target when the filesystem supports it.
2. Legacy or older schema input is backed up under `.backups` before migration.
3. Migrations append a record to `migration-journal.jsonl`.
4. Oversized, malformed, mismatched, or unreadable envelopes are moved under `.corrupt` instead of being silently overwritten.
5. Unsupported future schema versions are refused.
6. Managed documents are limited to 16 MiB.
7. `/sef storage status` reports path, domain, state, and size.
8. `/sef storage export` queues a single worker snapshot with a bounded queue of eight.
9. Snapshot paths are confined under `serverconfig/sef/exports`.
10. Alternate account data is excluded unless the issuer has both `sef.alts.export` and `sef.alts.ip.view`.
11. Domain repositories expose `new`, `ready`, `missing`, `recovery`, `unsupported`, `error`, or `closed` state through the storage coordinator.
12. A repository in `recovery`, `unsupported`, or `error` state refuses persistence so damaged or newer data cannot be overwritten. Location history and player profile mutation also fail closed. Cooldowns may continue as runtime only state, but their repository refuses to replace the source file.
13. Location history is UUID keyed, capped at 100,000 players, and bounded per player by `commandKernel.locationHistoryEntries`.
14. Cooldown persistence is capped at 100,000 entries and writes only future expiries meeting `commandKernel.persistentCooldownMinimumSeconds`.
15. Repository dirty revisions are captured with each snapshot. A concurrent mutation that occurs while a snapshot is written remains dirty and is flushed by the next pass.
16. Migration backup or journal preparation failure leaves the valid source file untouched, records an error state, and does not misclassify the source as corrupt.
17. Player profile updates, mute countdown snapshots, and banned item snapshots use coalesced background persistence. Their workers retain only the latest queued snapshot and use bounded shutdown flushing.
18. Social mail uses an in-memory recipient index. Per-recipient list, unread, quota, archive, and clear operations do not scan the global mail collection.
19. Social repository recovery, unsupported, and error states reject mutation. Commands receive a safe failure through the kernel action boundary, and the damaged source is not overwritten.
20. Command-spy profiles, moderation controls, and kits use the same versioned envelope, corruption quarantine, unknown-field preservation, dirty revision, and recovery-mode mutation refusal.
21. `StorageCoordinator` periodically snapshots dirty managed repositories and writes them on its bounded worker. The server thread does not perform the JSON file write.
22. Kit definitions and usage history are bounded before load acceptance. Invalid serialized item stacks fail validation and cannot become a partial claim.
23. Optional file logging is not a managed JSON repository. It creates no path while disabled, owns only fixed descendants of `logs/sef`, refuses symlinks, and uses an incomplete-session marker to diagnose an interrupted drain.

The structured audit service writes bounded JSONL events through a 4096 entry queue. Each event persists schema version, event and session ids, timestamp, actor UUID and username, source type, action id, target UUIDs, normalized parameters, result, reason, duration, origin, job and step correlation ids, definition and policy revisions, provider context, redaction class and rules, observer UUID, previous hash, and audit class. It rotates at the configured maximum file size, prunes rotated files by retention, redacts command arguments from applicable sensitive events, and attempts a five second shutdown flush. A writer failure stops new acceptance, accounts for the failed batch and queued records, clears unwritable work, and exposes the failure through `/sef doctor`. A writer that exceeds the shutdown bound remains owned and prevents a replacement writer from starting until it exits. Legacy call sites are adapted into the same schema rather than a smaller JSON shape.

Do not let two integrations own the same nickname state. The current provider selection chooses FTB Essentials when enabled and available, otherwise the integrated provider when automatic integration is enabled.

The integrated profile repository uses authenticated UUID as its authority. Authenticated username, display nickname, and update time are separate fields. It loads from `sef.playerdata.json` under the world player data directory and imports the earlier `sef.playerdata` format once when the JSON file is absent. A successful migration is written through the storage envelope, recorded in import diagnostics, and leaves explicit permission grants unchanged. Failed persistence does not report a successful migration. Profile count, username length, nickname length, timestamps, and legacy import count are bounded. Normal profile and nickname updates mutate memory immediately and submit a constant time coalesced persistence request. The daemon writer captures the latest immutable snapshot, releases the global profile monitor, and then performs filesystem access. A background failure moves the profile repository into an error state so later mutations fail closed and diagnostics expose the failure. Player save events request persistence. Server shutdown drains the latest revision within a bounded wait and then unloads world scoped profile state. Quarantined, unsupported, or failed profile storage is visible in `/sef doctor` and cannot be recreated by a later nickname command.

### 13.1 Alternate account privacy

Address correlation is opt in and disabled by default. When enabled:

1. Local addresses are ignored.
2. The default storage value is a salted SHA 256 hash using a server local 32 byte salt in `alt_tracking.salt`.
3. Retention is enforced on load, login, and explicit purge.
4. Storage is capped at 100,000 address groups and 32 profiles per group.
5. Normal output is redacted. Raw display requires `sef.alts.ip.view` and is possible only if raw storage was explicitly selected.
6. `/checkalts purge expired` removes records outside retention.
7. `/checkalts purge confirm` deletes all retained correlation records.
8. `/checkalts export` requires `sef.alts.export`, runs on the bounded export worker, and includes raw addresses only with the raw view permission.
9. Audit and ordinary logs contain operation metadata and counts, not addresses.
10. Login updates mark a generation dirty and schedule one coalesced background writer. Login no longer serializes and writes the full correlation map on the server event thread.
11. Server shutdown drains the alternate account writer before the shared storage and audit workers stop.
12. The cached salt is cleared whenever a storage root loads. An existing salt must be exactly 32 bytes. Missing salt for existing hashed data and corrupt salt enter a fail closed load path and are never silently replaced.

Operators remain responsible for informing users and following applicable privacy law. Enabling collection should be a deliberate documented policy decision.

## 14. Optional integrations

All optional integrations must be guarded by runtime mod detection and isolated so their absence does not prevent startup.

Current compile only integrations:

1. LuckPerms for metadata and NeoForge permission provider use.
2. FTB Essentials for nickname and mute related compatibility.
3. Curios for inventory and banned item support.

Current vanish compatibility code also detects MC2Discord, Playtime, and SDLink by mod id.

LuckPerms prefix and suffix metadata uses a one second cache with a hard limit of 2048 players. Every insertion prunes expired entries, earliest expiry is evicted at the limit, values are defensively copied, logout removes the player entry, and integration shutdown or configuration reload clears the cache.

Never reference optional implementation classes on an unconditional common initialization path.

Required Phase 1 integration verification includes:

1. No optional integrations installed.
2. LuckPerms NeoForge installed alone.
3. Curios installed alone.
4. FTB Essentials with its required dependencies.
5. All three integration families installed together.

The headless dedicated startup matrix has passed with LuckPerms NeoForge `5.4.140`, Curios `9.5.1+1.21.1`, FTB Essentials `2101.1.9`, FTB Library `2101.1.30`, and Architectury `13.0.8`. Each integration family started alone, and the complete stack started together. Every run reached the ready state, `/sef doctor` reported no kernel errors, and normal `stop` saved every dimension. These results prove optional dependency isolation and startup compatibility only. Authenticated LuckPerms refresh and metadata behavior, FTB nickname ownership mutations, and Curios inventory interaction remain manual release gates recorded in `docs/PHASE_1_MANUAL_TESTS.md`.

## 15. Mixins and access transformation

Vanish relies on narrow mixins declared in `src/main/resources/sef.mixins.json`. They modify player list, entity tracking, chat, sound, combat, advancements, status response, and related visibility behavior.

`src/main/resources/META-INF/accesstransformer.cfg` exposes the minimum internals required by current entity tracking logic.

Changes to mixins or access transformers require:

1. Compilation and unit tests.
2. Dedicated server startup.
3. Two player visibility testing.
4. Login, logout, dimension change, death, respawn, and reconnect checks.
5. JAR inspection to confirm mixin and access transformer resources.

Prefer a NeoForge event or supported hook when it can provide the same behavior.

## 16. Build and verification

### 16.1 Commands

Linux and macOS:

```bash
./gradlew test
./gradlew build
./gradlew runServer
./gradlew runClient
./gradlew runGameTestServer
./gradlew runData
```

Windows:

```powershell
gradlew.bat test
gradlew.bat build
gradlew.bat runServer
gradlew.bat runClient
gradlew.bat runGameTestServer
gradlew.bat runData
```

There is no configured formatter, Checkstyle, SpotBugs, or Error Prone task. Do not claim those checks ran.

The `runServer` task forwards standard input. Use its terminal for `sef doctor`, `sef storage status`, and the literal `stop` command. Signal termination is useful only for an explicitly recorded crash test and does not replace a normal shutdown check.

### 16.2 Required verification by change type

Pure policy changes:

1. Add or update JUnit tests.
2. Run `test`.
3. Run `build`.

Command, permission, configuration, persistence, or common lifecycle changes:

1. Run JUnit tests.
2. Run `build`.
3. Start a dedicated server.
4. Verify command visibility for allowed and denied players.
5. Verify console behavior.
6. Inspect logs for exceptions and sensitive argument leakage.

Client, rendering, screen, asset, or future networking changes:

1. Complete common checks.
2. Start a client.
3. Test a client with SEF and a client without SEF.
4. Test protocol negotiation, disconnect, reconnect, and fallback behavior.

Resource or metadata changes:

1. Run data generation when providers are involved.
2. Inspect generated resource drift.
3. Open the final JAR and confirm required resources and metadata.

### 16.3 Automated test coverage

The ModDevGradle unit test environment boots Minecraft and NeoForge for tests that exercise Minecraft classes. Other tests remain pure JVM policy tests. Current coverage includes:

1. Vanish visibility matrix.
2. Vanish permission reconciliation.
3. Vanish target hierarchy.
4. Workstation cooldown independence, expiry, and stale entry pruning.
5. Strict duration syntax and overflow.
6. Command root allow and deny policy.
7. Nickname Unicode, formatting, and normalization policy.
8. Legacy nickname fixture migration parsing.
9. Permission manifest duplicate rejection and deterministic ordering.
10. Atomic storage replacement, backups, quarantine, and bounded recovery behavior.
11. Storage unknown field preservation without deleted dynamic record resurrection.
12. Alternate account hashing, local address handling, and redaction.
13. Catalog completeness, sealing, capability references, and bounded capability metadata.
14. Deterministic shortcut collision modes and canonical cooldown identity.
15. Alias unknown target, recursion, ambiguity, stale revision, quota, and policy weakening rejection.
16. Bundle cycle, raw command, nesting, step, target, and expanded operation rejection.
17. Wrapper origin and recursive root rejection.
18. Feature, command policy, hierarchy, cooldown, warmup, confirmation, cost, quota reservation, and provider failure behavior.
19. Optional LuckPerms quota metadata parsing and finite fallback behavior.
20. Location history and cooldown repository round trips, corruption recovery, bounded state, and concurrent dirty snapshot preservation.
21. Legacy nickname profile import through the versioned player profile repository.
22. Real Brigadier tree projection and direct execution denial for `/sef` child permissions.
23. Real Brigadier denial of `/v queue <player>` when the issuer has queue permission without `sef.commands.vanish.others`.
24. Open InvSee menu downgrade after modify revocation and closure after view revocation.
25. Recipient specific immutable vanish list projection used by player information and server status paths.
26. Server status and packet lifecycle guards that avoid unloaded configuration access during shutdown.
27. LuckPerms metadata cache expiry, defensive copies, logout invalidation, and the 2048 entry hard bound.
28. Full structured audit event persistence without field loss.
29. Username and nickname ambiguity rejection across known profiles.
30. Catalog ownership for every executable current `/sef` action.
31. Alias publication decisions for catalog and external root ownership.
32. Permission decision provider, default, hierarchy, exemption, and reason fields.
33. Coalesced alternate account background persistence and corrupt salt preservation.
34. Typed announcement separation, command root definition policy, execution policy, and callback based audit outcomes.
35. Migration preparation failure that preserves a valid source and avoids false corruption quarantine.
36. Authenticated username hover for the configurable duplicate nickname mode.
37. Permission provider refresh revocation of active actor warmups and confirmations.
38. Coalesced player profile persistence with bounded shutdown flushing.
39. Shared command pipeline ordering, cooldown rollback, cost refund, and execution time permission revocation.
40. Coalesced mute and banned item persistence worker behavior, latest snapshot retention, failure reporting, recovery, shutdown draining, and post shutdown rejection.
41. Background location history and persistent cooldown shutdown flushing outside the calling thread.
42. Typed message placeholder allowlists, literal field insertion, immutable compilation, and rendered-size rejection.
43. Social repository round trips, corruption recovery, unsafe id and content rejection, mailbox quota, recipient ownership, archive isolation, clear isolation, and reminder acknowledgement state.
44. Observation event deduplication, bounded event memory, per-observer rate limiting, and second-window reset.
45. Social action catalog ownership, exact subsystem feature gates, and nonempty permission contracts.
46. Active nickname provider resolution and nickname aware Brigadier target parsing, including quoted display names.
47. Vanished target removal from online and known profile identity resolution.
48. Granted finite quota tier propagation into mail and reminder quota contexts.
49. Connection message correlation by component object identity with bounded weak references.
50. Social spy delivery audit observer identity, scope, audit class, and redaction metadata without content persistence.
51. Real Brigadier registration, permission-gated roots, and representative Phase 6 and Phase 7 grammar.
52. `/i` parse rejection above the configured item amount maximum.
53. Command-spy typed filter persistence, legacy-profile compatibility, and include, exclude, neutral, and clear player-filter transitions.
54. Password, private-content, unknown-root, IPv4, IPv6, namespaced, and IP-alias redaction before observation.
55. File logging disabled startup, address-free connection serialization, parent and nested symlink refusal, security-root filter immunity, fail-closed filter overflow, and retention preview drift rejection.
56. Connection-address shared-proxy refusal and shared-session hard caps.
57. Moderation warning, control, jail, sentence, release-location, expiry, and `Instant` persistence.
58. Kit policy, cooldown, one-time use, bounded per-player history, dynamic permission validation, and `Instant` persistence.
59. InvSee permission downgrade and closure before interaction, plus live ender-chest closure after permission or configuration revision changes.
60. Catalog ownership and shortcut ownership for Phase 6 and Phase 7 actions.
61. Nested wrapper, moderation-reason, `/data`, password, private-content, unknown-root, and network-address redaction.
62. File logger recovery when an earlier session marker exists and marker creation after a writer failure.
63. Command-spy other-observer permission, hierarchy, exemption, and vanish policy.
64. Cooperative InvSee registration that preserves an existing Brigadier root and child.
65. Other-target `/getpos`, fixed gamemode shortcuts, and parsed `/gm` grammar without self-permission coupling.
66. Kit load-time rejection of orphan, malformed, and over-limit use history.
67. Super-enchanting minimum, maximum, removal, invalid-range, and stale-configuration behavior.
68. Nine required GameTests, including teleport safety, exact condensation totals, incomplete recipe nonmutation, persistent build and freeze enforcement, inventory lock item use and drop enforcement, and repository freeze mirror cleanup without persistent data deletion.

Rendering, client packet observation, authenticated multi-client behavior, optional integration behavior with real players, and profiler observation still require the [Phase 1 manual multiplayer matrix](docs/PHASE_1_MANUAL_TESTS.md). Phase 2 and Phase 3 permission mutation, player driven cooldown persistence, location history recovery, and dirty shutdown races remain in [the Phase 2 and 3 manual matrix](docs/PHASE_2_3_MANUAL_TESTS.md). Phase 4 teleport behavior remains in [the Phase 4 matrix](docs/PHASE_4_TESTS.md). Phase 5 social privacy, visibility, live permission revocation, connection packets, mail, reminders, and identity projection remain in [the Phase 5 matrix](docs/PHASE_5_TESTS.md). Phase 6 authenticated moderation, real proxy and provider integration, deliberate filesystem and shutdown failures, MaxLogger coexistence, and profiler behavior remain in [the Phase 6 matrix](docs/PHASE_6_TESTS.md). Phase 7 authenticated inventory transactions, live client menus, real Curios behavior, missing-registry fixtures, player-driven persistence, super-enchant client synchronization, dirty shutdown, and profiler behavior remain in [the Phase 7 matrix](docs/PHASE_7_TESTS.md). Run every applicable matrix before approving a public release.

## 17. Operations and recovery

Before an upgrade:

1. Stop the server cleanly.
2. Back up the world, `config/sef`, world `serverconfig`, and existing logs.
3. Record the SEF, NeoForge, Java, and integration versions.
4. Review changed permission defaults and module toggles.

After an upgrade:

1. Start in a staging environment.
2. Check startup logs for optional integration failures.
3. Verify `/sef info`.
4. Test denied and allowed command suggestions with representative groups.
5. Verify private messages, moderation duration rejection, nickname collision rejection, vanish permission removal, and workstation cooldowns.
6. Run `/sef commands`, `/sef conflicts`, `/sef doctor`, and `/sef storage status` with the intended administrator role.
7. Restart with a workstation cooldown longer than `persistentCooldownMinimumSeconds` and confirm its remaining duration survives.
8. Inspect saved files before promoting the build.

Rollback:

1. Stop the server.
2. Restore the previous JAR and matching configuration or data backup.
3. Do not mix newly migrated data with an older build unless the migration documentation explicitly permits it.

Managed JSON migrations retain timestamped backups and journal entries. Restore the matching backup only while the server is stopped. Audit JSONL and persistent player NBT remain separate recovery domains.

Repository recovery procedure:

1. Stop the server before editing or restoring any SEF data file.
2. Preserve the current file, its `.corrupt` quarantine copy, `.backups`, and `migration-journal.jsonl`.
3. Identify the affected repository with `/sef doctor`, `/sef storage status`, and the startup log.
4. Validate that the file domain and schema match the running build.
5. Restore the newest known good backup to the exact original path, or remove only the invalid new file when an empty repository is acceptable.
6. Start a staging copy and confirm the repository reports `ready` or `missing`.
7. Verify record counts, nickname resolution, cooldown expiry, and location ordering before returning the server to service.

Do not rename a future schema into the current schema or copy records between domains. Recovery mode is intentionally non writable. A crash can leave the previous complete target or an uncommitted temporary file, but atomic replacement prevents a partially written target from being accepted. On clean shutdown, profiles flush before the coordinated location and cooldown repository flush.

## 18. Troubleshooting

Permission appears denied:

1. Confirm the exact `sef.` node.
2. Confirm the module is enabled.
3. Confirm LuckPerms is loaded when it is the intended provider.
4. Test with `/lp user <name> permission check <node>` when LuckPerms is installed.
5. Remember that generic operator level `2` is not a permission service bypass.

`/sudo` is missing:

This is expected in Phase 1. The command is intentionally unregistered during stabilization. Changing `modules.sudo`, permissions, or the retained allow and deny lists cannot enable it.

Nickname is rejected:

1. Compare visible length with configured limits.
2. Check color and style nodes.
3. Remove control or invisible format characters.
4. Check online and known offline username and nickname collisions.

Vanish is removed after login or permission reload:

This is expected when the player no longer owns a permission compatible with the stored vanish level. Grant an appropriate `sef.vanish.N` node before vanishing again.

Configuration edit appears ignored:

Wait for NeoForge to emit its reload event or restart the server. `/sef reload` reapplies already loaded values and does not force a raw disk read.

## 19. Security and privacy

Trust boundaries include player command input, chat input, optional permission providers, JSON and TOML files, persistent player data, mixin packet filtering, and future client payloads.

Required security rules:

1. Validate every mutating command at execution time.
2. Apply the same permission to Brigadier suggestions.
3. Fail closed when a permission provider is unavailable.
4. Never trust client supplied state.
5. Never execute stored text as a command unless its type and execution policy explicitly allow it.
6. Protect high risk targets with exemption and hierarchy policy.
7. Redact secrets and command arguments from broad logs.
8. Do not write Discord tokens, credentials, raw IP history, or private messages to documentation or source.
9. Treat fake message, command announcement, sudo, silent execution, and console execution features as high risk.
10. Preserve evidence in audit logs without exposing it to ordinary players.

Private messaging, reply, HelpOp, admin chat, and announcement toggle roots use permission filtered Brigadier projection and repeat authorization at execution. Ordinary SEF log records for private messaging, replies, HelpOp, and admin chat retain actor, route metadata, and message length where applicable, but do not contain the message body.

Alternate account collection defaults to off. Enabling it activates retention bounded, salted hash storage by default. Raw display, purge, and export remain separately permissioned and audited. Chat related features can still process private content in memory, so operators must restrict access and follow applicable law.

Expanded moderation treats connection addresses as restricted data. Direct and provider-resolved addresses are normalized only inside `ConnectionAddressService`. Ordinary action parameters use fingerprints, feedback uses redacted labels, and command redaction removes address and reason arguments from every IP moderation alias. Shared-proxy uncertainty, disabled providers, excessive session matches, and unauthorized literal input fail closed.

Command observation and optional file logging consume only immutable redacted records. Permission removal takes effect on the next observation. Location, result, nonplayer source, vanished identity, exempt identity, and argument projection each require their own authorization. File capture filters cannot disable mandatory security audit or expose raw command input. Logging remains disabled by default and cannot be redirected outside `logs/sef`.

Inventory and item commands recheck permissions at mutation time. Live administrative menus bind to their authorization revision and close or downgrade after revocation. Kit and item transactions validate capacity and registry state before commit. The self-only item shortcut cannot select another target or exceed its configured amount bound.

## 20. Release process

No automated public release workflow is currently documented as complete.

Before an approved release:

1. Update version metadata.
2. Update `README.md`, this document, and the changelog.
3. Run all required verification.
4. Inspect `build/libs` and the complete JAR contents.
5. Inspect the complete Git diff for secrets, run output, generated caches, absolute paths, and unrelated changes.
6. Record compatibility and migration notes.
7. Publish only after explicit approval.

## 21. Roadmap

[sef2.md](sef2.md) remains the exhaustive roadmap. Phases 1 through 7 have implementation coverage, but their applicable authenticated multiplayer, player driven, packet visible, shutdown race, registry fixture, and profiler release gates remain open. Phase 8 is next and covers native economy and signs. GUI networking, fake message, sudo, disguise, alias publication, bundle execution, panel editors, and broader EssentialsX parity remain planned for their assigned later phases.
