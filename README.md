# SEFPORTED

SEFPORTED is a server essentials mod for Minecraft 1.21.1 on NeoForge. It provides chat tools, moderation, vanish, announcements, virtual workstations, inventory administration, and other server management features through native Minecraft and NeoForge systems.

The project is the NeoForge 1.21.1 port and active development base for SEF 2. The current mod uses one universal JAR. With enhanced GUIs disabled it remains server only. With enhanced GUIs enabled, compatible clients may install the same JAR for optional screens and private HUD state. Vanilla clients and clients without SEF can still connect because the enhanced protocol is optional and the mod metadata uses `displayTest = "IGNORE_SERVER_VERSION"`.

The enhanced client protocol, universal catalog, administrative control surfaces, Fancy Tags editor, disguise projection, and modular configuration editor are present on the current phase branch. Command fallback remains authoritative for clients that do not negotiate the enhanced protocol. The branch is not yet complete against every SEF 2 requirement. Sixteen Phase 13 control families are deliberately reported as unavailable until their real runtime behavior exists.

## Current status

Current project metadata:

1. Minecraft `1.21.1`.
2. NeoForge `21.1.235`.
3. Java `21`.
4. Parchment mappings `2024.11.17`.
5. Mod id `sef`.
6. Artifact version `2.0.0`.

Treat this branch as a test build. The confirmed source security findings are repaired, and the audit writer now uses a platform native descriptor provider on Linux, macOS, and Windows through the JNA API supplied by the pinned NeoForge runtime. Hosted matrix run `33704780764` passed the same candidate artifact, native provider smoke, unit, GameTest, dependency, and artifact checks on all three operating systems for commit `02cf42bac17405daf16ec3a1036453e64e72232`. Full cross platform runtime evidence, interactive acceptance, and dependency closure remain open gates. The current worktree passes 530 unit tests, 41 required GameTests, complete route checks for 694 catalog actions and 315 shortcuts, 2,213 representative parser variants, and 358 safe read only live routes. Automated evidence and remaining gaps are recorded in [docs/SEF2_ACCEPTANCE.md](docs/SEF2_ACCEPTANCE.md), with detailed defect evidence and repairs in [audit.md](audit.md). Interactive release acceptance must also complete the staging matrix in [test.md](test.md). Do not advance the branch to `main` while any acceptance row remains incomplete or in progress.

## Current features

The current implementation includes:

1. Chat formatting, colors, styles, prefixes, suffixes, timestamps, and optional LuckPerms metadata.
2. Private messages, replies, clickable reply support, HelpOp, admin chat, and operator bulletins.
3. Integrated nickname and whois commands, configurable duplicate display names with authenticated username hover, and optional FTB Essentials nickname integration.
4. Scheduled text and command announcements, title announcements, per player announcement toggles, and countdowns.
5. Persistent mute and warning systems, freezing, inventory lock, building restrictions, alternate account checks, clear chat, and banned item controls. Mute countdown and banned item saves use coalesced background file writers with bounded shutdown flushing.
6. Live and offline inventory inspection with optional Curios support. Enhanced clients receive a target inventory above their own inventory, while fallback clients use the same server-authoritative container path.
7. Vanish levels, per observer visibility levels, trace support, sound suppression, tab hiding, and optional Discord bridge compatibility.
8. MOTD management and configurable word filters.
9. Virtual `/craft`, `/anvil`, `/enchantingtable`, `/superenchantingtable`, and `/repair` commands with aliases, permissions, and cooldowns.
10. Central NeoForge permission evaluation, structured provider decisions, a generated permission manifest, action specific administrative permissions, runtime command tree refresh, and permission revocation handling.
11. Versioned JSON storage envelopes with atomic replacement, migration backups, a migration journal, corruption quarantine, status diagnostics, and bounded background exports.
12. Opt in alternate account correlation with salted address hashing, retention, redacted display, separately permissioned raw display, purge, export operations, coalesced background persistence, and corrupt salt refusal.
13. Structured security audit JSONL with the Phase 2 event schema, bounded asynchronous writes, size rotation, retention, fail closed writer health, dropped event accounting, and `/sef doctor` reporting.
14. Permission filtered Brigadier projection for private messaging, replies, HelpOp, admin chat, and announcement toggles, with message bodies excluded from ordinary SEF log records.
15. A sealed command catalog with canonical routes, permission requirements, source classes, feature ownership, target behavior, cooldown identity, audit class, fallback metadata, and conflict policy.
16. Shared feature, permission, target hierarchy, cooldown, warmup, confirmation, cost, audit, quota, message, identity, alias compilation, bundle compilation, panel descriptor, and command wrapper contracts.
17. `/sef commands [page]`, `/sef conflicts`, and `/sef doctor` diagnostics with permission filtered output.
18. Every currently executable `/sef` catalog action uses the same runtime feature, source, permission, cooldown, warmup, cost, confirmation, execution, and audit pipeline. Workstation convenience roots and aliases resolve to their canonical action and cooldown identity.
19. Finite quota tiers and optional LuckPerms metadata for future homes, player warps, targets, mail, and user definitions, with hard ceilings and reservation based race protection.
20. Versioned `location-history.json` and `cooldowns.json` repositories with bounded records, atomic writes, recovery mode, and shutdown flushing.
21. A UUID authoritative player profile repository that imports existing nickname data, retains authenticated usernames separately from display nicknames, coalesces background persistence, and drains through a bounded shutdown flush.
22. Versioned homes with total and per-dimension quotas, overwrite confirmation, soft deletion, recovery, administrative inspection, and FTB Essentials ownership modes.
23. UUID based teleport requests with nickname aware, vanish safe targets, ambiguity safe acceptance, blocking, request toggles, auto accept relationships, expiry, warmups, movement and damage cancellation, logout invalidation, and one canonical execution for bounded `/tpaall` fan out. Request creation and acceptance use action specific permission controlled cooldowns.
24. Safe `/back`, layered spawn, server warp, player warp, RTP, and optional direct teleport commands with permissions, feature gates, cooldowns, warmups, costs, shared LuckPerms aware hierarchy, exemptions, destination revisions, world border checks, hazard checks, loaded chunk budgets, and bounded history. RTP is capped at 20,000 blocks, never selects water, and requires a surface position.
25. Player hosted warps with stable ids, `owner:name` lookup, private, shared, unlisted, and public access, favorites, reports, transfer offers, publication, moderation state, visit counts, home conversion, deletion recovery, and canonical kernel execution for every command mutation.
26. Hardened `/msg`, `/tell`, `/w`, `/whisper`, `/r`, and `/reply` routes with literal message bodies, nickname aware vanish safe targets, message and reply toggles, UUID ignore state, bounded input, existing configurable presentation, and metadata only ordinary audit records.
27. Permission gated `/socialspy` with everyone or selected player audiences, sender, recipient, or either matching, route filters, metadata and content scopes, exemption, hierarchy, and vanish checks at selection and delivery, per event permission revalidation, bounded recent state, duplicate suppression, delivery rate limits, auditable observer identity and redaction state, and a typed format preview.
28. UUID addressed offline mail with finite mailbox quotas, permission quota tiers, expiry, owner only read, archive, delete, and clear operations, login notification, indexed recipient lookup, and versioned persistence.
29. Real custom join and leave templates with typed placeholders, per player revisions, preview and inspection commands, shared target hierarchy and exemption checks, recipient specific vanish suppression, and bounded identity based packet correlation.
30. Welcome, onboarding, command fallback, and unread mail reminders with typed templates, repeat and delivery limits, acknowledgement revisions, dismissal state, manual delivery, permission backed definition quotas, and bounded scheduler work.
31. Persistent custom text pages through `/customtext`, `/booktext`, `/rules`, and `/info`, plus `/sef identity coverage` and `/sef identity refresh` diagnostics.
32. Nickname changes refresh tab projection immediately. Server projected chat, tab, display component, connection message, SEF resolution, suggestions, and feedback surfaces use the selected nickname provider while Brigadier authentication and signed chat remain truthful. Quoted display names are accepted by migrated SEF command targets without exposing vanished players.
33. Expanded permanent and temporary player bans, pardons, kicks, bounded mass kicks, self kick, address bans, address pardons, and shared address kicks through a fail safe connection address provider. Trusted proxy and external integrations can register bounded prioritized adapters without making an optional provider a startup dependency. Network addresses are fingerprinted or fully redacted outside their restricted authorization boundary.
34. Persistent warnings, mutes, freezes, inventory locks, build locks, jail definitions, jail sentences, expiry release locations, hierarchy, exemptions, and execution time authorization rechecks. Jail enforcement follows login, respawn, and dimension changes. Disabling expanded moderation clears runtime freeze mirrors without deleting persistent controls.
35. Permission-gated `/commandspy` with everyone and selected-player audiences, initiator or effective-actor matching, source and origin scopes, typed include and exclude filters, per-observer projection controls, bounded recent events, rate limits, deduplication, and live permission revalidation. Managing another observer also applies a distinct permission, target hierarchy, exemptions, and vanish visibility.
36. A correlated command-event journal and disabled-by-default file sink under `logs/sef`. The sink uses bounded records and queues, batched writes, rotation, retention previews with state-bound confirmation, health diagnostics, search, redacted export, connection-event streams, shutdown markers, incomplete-session recovery markers, and fixed-path protection.
37. Versioned kits with safe inventory snapshots, cooldowns, one time policy, per kit dynamic permissions, atomic capacity checks, optional bounded overflow dropping, administrative validation, metadata export, usage reset, and load time rejection of orphan or over limit use history. The repository rejects stale, deleted, cooling down, or already claimed definitions at commit time.
38. Hardened self and other-player inventory tools including `/clearinventory`, `/enderchest`, `/disposal`, `/more`, `/condense`, `/hat`, `/itemname`, `/itemlore`, `/itemdb`, `/book`, and `/recipe`. Live inventory and ender-chest menus close or downgrade when permission, feature, or policy revisions change. InvSee preserves preexisting Brigadier routes cooperatively instead of deleting another mod’s command node.
39. Player utilities for AFK state, feed, heal, fly, god mode, rest, speed, experience, personal time and weather, nearby players, position, compass, depth, top, bottom, and jump. `/feed` fills hunger with zero saturation and leaves health unchanged, while `/heal` remains the explicit recovery command. Long-lived fly, god, personal time, and personal weather state is rechecked after permission changes.
40. `/gm`, `/gmc`, `/gms`, `/gmsp`, and `/gma` self and target shortcuts, plus bounded self-only `/i`. Self and target gamemode routes use separate least-privilege permissions. Additional vanilla workstations include workbench, cartography table, grindstone, loom, smithing table, and stonecutter routes. Super enchanting enforces a configurable bounded safety ceiling, with level `1000` covered by GameTests, and closes stale menus after policy reload. Every shortcut inherits its canonical feature, permission, cooldown, audit, and collision policy.
41. Integer minor-unit economy storage with idempotent ledger mutations, crash-recoverable cost reservations, cached balance ranking, exact payment confirmation, account freezes, component-safe worth and sales, external provider ownership, import-once backup and reports, configurable fixed and scaled command costs, and all twelve strict vanilla economy sign types. Sign creation, use, ownership bypass, and management are separately permissioned and audited.
42. Optional enhanced client capability negotiation with versioned sessions, typed bounded payloads, replay protection, permission invalidation, command fallback, configurable reminders, vanilla styled dashboards and workflows, searchable known-player pickers with all, online, and offline filters, bounded multi-target give selection, creative-style item browsing with icon-only slots and one native tooltip, consistently sharp screen backgrounds, private HUD deltas, viewer-specific nickname projection, and a content-addressed static Fancy Tags prototype.
43. Hardened `/sudo`, `/run`, and `/silent` execution. Ordinary sudo preserves the target’s real permissions. Disabled by default delegated sudo can admit one exact reviewed command through an immutable, expiring, single use grant without changing operator state, permission provider data, groups, persistent player data, or the target command tree. Target-context suggestions, confirmation, revision binding, audit lifecycle, cleanup, root and profile permissions, and wildcard diagnostics are enforced.
44. Complete Fancy Tags registry, assignment, secure import and archive validation, content-addressed storage, publication recovery, bounded transfer, client cache, local projects, editor, glyph bridge, world rendering, cleanup, and command fallback.
45. Persistent disguise definitions and assignments with namespaced registered entity selection, player and entity projection, stable proxy interpolation, client-ticked mob idle and movement animation state, proxy identity, traits, abilities, truthful per-disguise capability feedback, target policy, expiry, client presentation, command workflows, and safe fallback when an adapter or enhanced client is absent.
46. Seventy-five typed server-control schemas spanning operations, maintenance, staff workflow, onboarding, recovery, governance, admission, world policy, diagnostics, privacy, markets, community knowledge, and unified display ownership. Fifty-nine currently have executable runtime handlers. Sixteen are visible as unavailable in `/sef doctor` and fail during preview instead of reporting false activation. Admission includes a bounded native login wait mode with FIFO release, timeout, duplicate cleanup, and separate admission and queue exemption permissions.
47. Offline inventory inspection with versioned backup and conflict protection, bounded multi-target item grants, and a persistent UUID-bound offline give queue that creates one independently revalidated login action for each selected offline target. Queued actions use immutable actor attribution, a restricted execution profile, durable claims and receipts, duplicate suppression, outcome-unknown recovery, and do not require the original actor to remain online. Administrative enchanting has distinct unsafe capabilities. Permission-derived canonical cooldowns and item escrow cover parcels, lost and found, trades, auctions, watches, blocks, claims, settlement, and recovery.
48. A 62-module configuration platform with a small bootstrap file, typed validation, transactional publication, migration backups, optimistic revisions, rollback, debounced watching, in-game workflows, command-only editing, secret filtering, and generated reference drift tests.

The full SEF 2 command and platform blueprint is documented in [sef2.md](sef2.md). The phase-by-phase implementation record is [docs/SEF2_ACCEPTANCE.md](docs/SEF2_ACCEPTANCE.md).

## Security defaults

Every exposed command path is expected to use a permission node. The current security foundation adds these important defaults:

1. `/sef` subcommands use separate nodes. `/sef reload` and `/sef test` default to denied.
2. `/sef filter` uses `sef.filter.manage` instead of a generic operator level.
3. `/nickfor` uses `sef.commands.nick.others` and defaults to denied.
4. `/sudo`, `/run`, and server-context `/silent` are high-risk Phase 11 routes. They use denied-by-default permissions, root policy, confirmation, observation, and audit.
5. Command announcements use a separate denied by default permission. Their command root must be explicitly allowlisted both when saved and when executed.
6. Persisted vanish state is removed or lowered when the player no longer has its required vanish permission.
7. Console and RCON sources require permission level `4` for permission service bypass. Command blocks do not receive a general bypass.
8. Moderation, MOTD, banned item administration, inventory tools, bulletins, and announcements use SEF permission nodes instead of generic operator level.
9. `/invsee` separates view, modify, Curios, offline, and other player ender chest capabilities. Enhanced and fallback screens use the same authoritative menu. Mutation permission is checked again on every click.
10. Vanish administration applies exemption and hierarchy checks, then rechecks active state after provider refreshes, configuration reloads, dimension changes, respawns, reconnects, and once per second.
11. Convenience roots cannot weaken their canonical action. Workstation aliases share the same action identifier and therefore share permissions and cooldowns.
12. Alias and bundle definitions are compiled against the catalog before publication. Alias root ownership includes catalog, shortcut, and preexisting Brigadier roots, and the selected conflict mode is enforced. Unknown actions, collisions, recursion, raw command steps, policy weakening, and unbounded expansion are rejected.
13. Future collection and fan out systems receive finite defaults and hard ceilings. An optional provider failure falls back safely and is exposed by `/sef doctor`.
14. Social spy defaults to denied. Metadata, content, everyone, selected-player, exempt-player, vanished-player, recent-event, route-filter, and format-preview capabilities use separate denied-by-default nodes.
15. Private message bodies remain outside ordinary kernel audit parameters and ordinary log statements. Social spy content is session only, permission filtered for every event, rate limited, and cleared on logout. Every delivered observation records its observer UUID, route, metadata or content scope, and redaction class without recording the message body.
16. Every Phase 6 and Phase 7 root and independently controllable subcommand has a registered permission. Administrative, other-player, hierarchy-bypass, exemption-bypass, raw-address, command-observation, logging, unsafe-item, and super-enchant capabilities default to denied.
17. Player and address moderation rechecks the active feature, permission, hierarchy, exemption, target visibility, provider policy, target cap, and confirmation revision immediately before mutation.
18. Password like roots, every private chat alias, moderation reasons, nested command wrappers, data command arguments, unknown roots, and every IP moderation alias are redacted before command spy, recent history, file logging, search, export, or audit projection. Newlines, control characters, and Unicode format controls cannot hide a sensitive root.
19. File logging is off by default. Disabled startup creates no `logs/sef` directory or writer. Enabled logging owns only fixed descendants of `logs/sef`, refuses symlink escapes, bounds its queue and record sizes, preserves mandatory security audit independently from capture filters, and remains degraded until an earlier incomplete-session marker is acknowledged.
20. Item grants, kit claims, inventory edits, live menus, and super enchanting validate capacity, registry state, configuration revision, and current authorization before committing a mutation. Inventory lock denies drop, swap, crafting, container click, creative slot, pickup, and item use paths on the logical server.
21. Economy values use exact integer minor units. Player transfers, administrative adjustments, command charges, imports, and sign transactions apply independent permissions, bounds, hierarchy, confirmation, idempotency, rollback, and audit policy. External mode never creates native shadow balances.
22. LuckPerms permission evaluation resolves the exact node, nearest matching wildcard, broader wildcards, and global `*` in that order. An explicit deny at the first defined level wins. A direct LuckPerms grant may safely bridge a transient NeoForge permission capability failure, but an unavailable provider without a real grant fails closed. Provider-only approval and access-grant checks use the same bridge while deliberately excluding access leases and one-execution delegation.
23. Deferred offline give actions execute when the target logs in, even when the authenticated issuer is offline. Queue-time authorization and immutable actor attribution are retained, execution uses only the stored restricted action profile, and feature, registry item, amount, canonical route, durable operation state, and duplicate receipts are revalidated. They never gain console authority.
24. Live server-control policies cannot be marked active or resolved through the generic state command. Their execution handler must succeed first. Missing runtime behavior is exposed before execution and cannot create active state.

Review [DOCUMENTATION.md](DOCUMENTATION.md) before enabling administrative commands.

## Requirements

Server requirements:

1. Minecraft server `1.21.1`.
2. NeoForge `21.1.235`. The packaged metadata requires this exact verified version. The recorded LuckPerms NeoForge `5.4.140` failure occurred on NeoForge `21.1.233` inside LuckPerms player placement. It does not establish LuckPerms compatibility or incompatibility on `21.1.235`.
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
5. Review the generated `config/sef/modules/*.toml` files. These typed module files cannot grant permissions or execute commands.
6. Install and configure LuckPerms when group based permission control is required.

Clients do not need the SEF JAR for command access. Compatible clients may install the same JAR for negotiated vanilla-style screens and HUDs. Vanilla and non-SEF clients remain supported through command fallback.

## Configuration

Primary configuration:

1. `config/sef/common.toml` is the retained bootstrap file. Ordinary settings live in typed module files after migration.
2. `<world>/serverconfig/sef-vanish-server.toml` contains detailed vanish behavior.
3. `<world>/serverconfig/sef/*.json` contains feature data such as announcements, filters, mutes, warnings, banned items, alternate account data, and bulletins.
4. `config/sef/motd.json` contains the dedicated server MOTD configuration.
5. `<world>/serverconfig/sef/permission-manifest.json` contains the deterministic runtime permission catalog.
6. `<world>/serverconfig/sef/audit/security-audit.jsonl` contains structured sensitive action audit events.
7. `<world>/serverconfig/sef/location-history.json` contains bounded UUID keyed location history.
8. `<world>/serverconfig/sef/cooldowns.json` contains only cooldowns whose remaining duration meets the configured persistence threshold.
9. `<world>/serverconfig/sef/teleports.json` contains versioned homes, spawn layers, server and player warps, teleport preferences, transfer offers, reports, and queued offline teleports.
10. `<world>/serverconfig/sef/social.json` contains versioned social preferences, mail, per-player connection templates, reminder definitions and states, and custom text pages.
11. `<world>/serverconfig/sef/moderation.json` contains versioned warnings, persistent controls, jails, and jail sentences. Vanilla player and IP ban lists remain the authority for ban enforcement.
12. `<world>/serverconfig/sef/command-spy.json` contains bounded observer profiles and filters. Recent command events remain runtime bounded state.
13. `<world>/serverconfig/sef/kits.json` contains versioned kit definitions and UUID-addressed claim history.
14. `<server>/logs/sef` is the optional fixed logging root. It is absent while file logging remains disabled.
15. `<world>/serverconfig/sef/economy.json` contains native accounts, preferences, ledger entries, pending cost reservations, worth definitions, and import records.
16. `<world>/serverconfig/sef/economy-signs.json` contains UUID-owned, side-specific economy sign definitions and text fingerprints.
17. `<world>/serverconfig/sef/economy-import-reports` contains aggregate import-once reports. The matching pre-import economy snapshot is stored under `<world>/serverconfig/sef/backups`.
18. `<world>/serverconfig/sef/aliases.json` contains alias drafts, immutable publications, and bounded revision history.
19. `<world>/serverconfig/sef/bundles.json` contains bundle drafts, publications, history, and recoverable runtime jobs.
20. `<world>/serverconfig/sef/command-profiles.json` contains reviewed actor, targeted-actor, and server command profiles.
21. `<world>/serverconfig/sef/fake-identities.json` contains fake identity profiles, scenes, schedules, and bounded history.
22. `<world>/serverconfig/sef/sudo-policy.json` contains UUID-addressed consent and administrative lock state.
23. `config/sef/modules/*.toml` contains the generated modular configuration documents. `sudo.toml` owns the delegated execution toggle, compatibility syntax, consent rule, self-delegation rule, temporary level ceiling, grant lifetime, confirmation, notification, indirection policy, and root policy.
24. `<world>/serverconfig/sef/fancy-tags.json` and its fixed object roots contain Fancy Tags metadata, content, journals, recovery state, and retained revisions.
25. `<world>/serverconfig/sef/disguises.json` contains versioned disguise definitions and UUID assignments.
26. `<world>/serverconfig/sef/inventory-recovery.json` and player-data recovery copies contain bounded recovery metadata for graves and offline inventory operations.
27. `<world>/serverconfig/sef/server-control.json` contains typed state for the 75 advanced server-control systems.
28. `<world>/serverconfig/sef/community-state.json` contains indexed community, workflow, watch, poll, event, knowledge, and display state.
29. `<world>/serverconfig/sef/approvals.json`, `access-leases.json`, and `admin-lock.json` keep approval and temporary authority separate from ordinary control records.
30. `<world>/serverconfig/sef/escrow.json` contains UUID-owned parcel, lost-and-found, trade, auction, claim, settlement, and recovery records.
31. `<world>/serverconfig/sef/offline-actions.json` contains bounded UUID-bound enhanced workflow actions awaiting an online target. The current reviewed persistent action is other-player item give. A batch freezes authorized UUIDs at preview and stores one independently revalidated record for each offline target.

`/sef storage status` reports every managed document. `/sef storage export` queues a bounded snapshot under `<world>/serverconfig/sef/exports`. Alternate account data is excluded unless the issuer has both its export and raw address permissions.

The `commandKernel` section sets hard limits for aliases, bundle steps, nested bundle depth, targets, expanded target steps, per player location history, and the minimum cooldown duration persisted across restarts. Teleport settings keep the default RTP radius at 5,000 blocks and allow operators to raise it to 20,000. See [DOCUMENTATION.md](DOCUMENTATION.md) for exact defaults, permission controlled cooldowns, and quota metadata.

Generated references:

1. [Configuration reference](docs/CONFIGURATION_REFERENCE.md)
2. [Command reference](docs/COMMAND_REFERENCE.md)
3. [Permission reference](docs/PERMISSION_REFERENCE.md)
4. [Installation guide](docs/INSTALLATION.md)
5. [Migration guide](docs/MIGRATION_GUIDE.md)
6. [Compatibility matrix](docs/COMPATIBILITY_MATRIX.md)
7. [Performance report](docs/PERFORMANCE_REPORT.md)
8. [Security review](docs/SECURITY_REVIEW.md)
9. [Release notes](docs/RELEASE_NOTES.md)
10. [Full code audit and remediation report](audit.md)

Run `./gradlew generateProjectReferences` after changing a module schema, command catalog entry, shortcut, GUI descriptor, or permission definition. Unit tests fail when tracked references drift from their runtime registries.

Run `./gradlew generateAuditInventory --rerun-tasks -Dsef.audit.evidenceRoot=/path/to/restricted-evidence` to execute the complete audit inventory and deterministic drift checks. The evidence root must be a fresh approved directory outside the repository. The task fails when the evidence root is omitted and does not write product files.

Run `./gradlew generateAuditDependencyManifest --no-configuration-cache -PsefAuditCandidateCommit=$(git rev-parse HEAD)` after building a candidate to capture the resolved compile, runtime, fallback, and test artifacts, dependency paths, SHA-256 and SHA-512 digests, NeoForge runtime ownership, and duplicate native runtime scan. Variant specific native artifacts retain their exact selected component identity when Gradle does not expose a full parent path. The sanitized manifest is written to `build/audit/platform-dependency-manifest.txt` and is evidence only when it is retained outside the repository or uploaded by a workflow. On Windows use `gradlew.bat generateAuditDependencyManifest --no-configuration-cache -PsefAuditCandidateCommit=$(git rev-parse HEAD)` in PowerShell. The task intentionally runs without Gradle configuration cache because it resolves and hashes the live dependency model.

NeoForge owns TOML loading and external reload notifications. `/sef reload` reapplies values already loaded by NeoForge. It does not force an arbitrary disk read.

Published module toggles gate command behavior and enhanced presentation. Commands that must survive live enable and disable transitions keep stable registration and fail closed while their module is disabled. Existing server configuration values are retained across upgrades, so review old values after new secure defaults are introduced.

`/sef config migrate dryrun` reports every legacy `common.toml` field that has a typed module destination and every field that must remain. `/sef config migrate apply <expected_revision>` issues an exact source-hash confirmation before staging and validating all module candidates. Publication retains `common.toml`, writes fixed-path recovery backups, restores module files on failure, and records mapped fields in `config/sef/modules/migration.toml`.

## Development

Clone the repository and use the checked in Gradle wrapper.

Linux and macOS:

```bash
./gradlew test
./gradlew generateProjectReferences
./gradlew build
./gradlew runServer
./gradlew runClient
./gradlew runCandidateGameTestServer -PsefCandidateGameDirectory=/path/to/fresh/candidate-runtime
```

Windows:

```powershell
gradlew.bat test
gradlew.bat generateProjectReferences
gradlew.bat build
gradlew.bat runServer
gradlew.bat runClient
gradlew.bat runCandidateGameTestServer -PsefCandidateGameDirectory=C:\path\to\fresh\candidate-runtime
```

The built JAR is written to `build/libs/`.

There is currently no dedicated formatter or static analysis task. Java changes must at minimum pass `test` and `build`, plus the relevant server or client smoke test.

The `runServer` task forwards terminal input to the dedicated server. Wait for the ready message, run operator diagnostics as needed, and type `stop` to exercise the normal bounded shutdown path.

## Repository layout

1. `src/main/java/com/enviouse/sef` contains common and server implementation.
2. `src/main/resources` contains mixin, language, and access transformer resources.
3. `src/main/templates` contains expanded NeoForge mod metadata.
4. `src/test/java` contains pure policy tests and NeoForge bootstrapped JUnit tests for Minecraft command, menu, permission, and lifecycle behavior.
5. `sef2.md` is the exhaustive SEF 2 product and architecture blueprint.
6. `DOCUMENTATION.md` contains maintainer and operator details.
7. `docs/PHASE_1_MANUAL_TESTS.md` contains the Phase 1 client verification history.
8. `docs/PHASE_2_3_MANUAL_TESTS.md` contains the Phase 2 and Phase 3 operator, permission, restart, and recovery verification history.
9. `docs/PHASE_4_TESTS.md` contains the Phase 4 teleport verification history.
10. `docs/PHASE_5_TESTS.md` contains the Phase 5 social, privacy, connection-message, reminder, and identity verification history.
11. `docs/PHASE_6_TESTS.md` contains the Phase 6 moderation, privacy, command-observation, and logging verification history.
12. `docs/PHASE_7_TESTS.md` contains the Phase 7 inventory, kits, workstation, shortcut, and player-utility verification history.
13. `docs/PHASE_11_TESTS.md` contains the completed Phase 11 alias, bundle, command-profile, fake-identity, sudo, run, and silent matrix.
14. `docs/SEF2_ACCEPTANCE.md` is the exact phase-by-phase completion ledger for `sef2.md`.
15. `docs/CONFIGURATION_REFERENCE.md`, `docs/COMMAND_REFERENCE.md`, and `docs/PERMISSION_REFERENCE.md` are registry-generated operator references guarded by drift tests.
16. `docs/INSTALLATION.md`, `docs/MIGRATION_GUIDE.md`, `docs/COMPATIBILITY_MATRIX.md`, `docs/PERFORMANCE_REPORT.md`, `docs/SECURITY_REVIEW.md`, and `docs/RELEASE_NOTES.md` contain release hardening evidence and operator procedures.

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
