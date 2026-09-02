# SEF 2 Complete Manual Test Plan

This plan tests the current SEF 2 implementation against Phases 1 through 14 of `sef2.md`. It is intended for bug discovery, regression testing, and release approval. It supplements the automated suite. It does not replace `docs/SEF2_ACCEPTANCE.md`, `docs/COMMAND_REFERENCE.md`, `docs/PERMISSION_REFERENCE.md`, or the focused phase records under `docs/`.

The current reference build targets Minecraft `1.21.1`, NeoForge `21.1.235`, Parchment `2024.11.17`, Java `21`, and Gradle `8.8`.

## Safety rules

Run this plan only on a disposable staging server.

Several tests intentionally:

- Corrupt copied repository files.
- Interrupt the Java process.
- Ban, mute, jail, freeze, kick, or hide test players.
- Change permissions while actions are active.
- Fill inventories and storage limits.
- Stage cleanup, rollback, data-pack, world-border, backup, auction, and evidence operations.
- Exercise destructive confirmations.

Never point these tests at a production world. Never use real player addresses, private messages, credentials, evidence, or personal data. Keep the original test snapshot until the entire run is accepted.

Do not execute a destructive preview unless this plan explicitly says to execute it. For Phase 13 control systems, a passing preview does not authorize an action against a real server.

## Source of truth

Use these files during testing:

- `sef2.md` defines the required behavior and phase exit criteria.
- `docs/SEF2_ACCEPTANCE.md` records the currently verified baseline.
- `docs/COMMAND_REFERENCE.md` lists all 694 catalog actions, routes, source classes, permissions, confirmation rules, GUI descriptors, and cooldown keys.
- `docs/PERMISSION_REFERENCE.md` lists all 11,937 capabilities.
- `docs/CONFIGURATION_REFERENCE.md` documents all 62 module schemas and setting bounds.
- `docs/COMPATIBILITY_MATRIX.md` records supported and tested integration combinations.
- `docs/SECURITY_REVIEW.md` lists trust boundaries and release findings.
- `docs/MIGRATION_GUIDE.md` defines supported upgrade and rollback procedures.
- `docs/PERFORMANCE_REPORT.md` defines measured budgets.

If a command in this plan differs from the current Brigadier suggestions, stop that row and compare it with `docs/COMMAND_REFERENCE.md`. Record the difference as a documentation or command-tree bug.

## Result codes

Mark every row with one result:

| Mark | Meaning |
| --- | --- |
| `pass` | The observed result exactly matches the expected result and no related error appears in logs. |
| `fail` | Behavior, feedback, persistence, security, GUI, or logs differ from the expected result. |
| `blocked` | A required client, integration, fixture, permission provider, or operating-system capability is unavailable. |
| `not applicable` | The row is excluded by an explicit product decision in `sef2.md`. Include the exact section link or heading. |

Never convert `blocked` to `pass`.

## Test run record

Copy and complete this table for every full run:

| Field | Value |
| --- | --- |
| Date and timezone | |
| Git commit | |
| Branch | |
| JAR path | |
| JAR SHA-256 | |
| Java version | |
| Minecraft version | |
| NeoForge version | |
| Operating system | |
| Filesystem | |
| Server mode | GUI off, GUI on, or both |
| Permission provider | Native fallback or provider and version |
| Optional integrations | |
| Owner player and UUID | |
| Senior staff player and UUID | |
| Junior staff player and UUID | |
| Player A and UUID | |
| Player B and UUID | |
| Offline profile and UUID | |
| Enhanced client build | |
| Fallback client build | |
| Configuration archive | |
| Initial world archive | |
| Final world archive | |
| Profiler and version | |
| Result summary | |

Record one sanitized evidence folder outside the repository. Include:

- `latest.log` for every server start.
- Client logs for enhanced and fallback clients.
- Configuration copies before and after each change.
- Repository file hashes before and after recovery tests.
- Screenshots of every GUI class at each required scale.
- A short recording for vanish, disguise, proxy, menu-revocation, and race tests.
- Profiler output for performance rows.
- A bug report for every failure.

## Required test actors

Use at least these identities:

| Actor | Purpose |
| --- | --- |
| `owner` | All explicitly reviewed administration permissions. Highest hierarchy. |
| `seniorstaff` | Moderation and control management. Below `owner`. |
| `juniorstaff` | Narrow view and create permissions. Below `seniorstaff`. |
| `playera` | Ordinary player permissions. |
| `playerb` | Ordinary player permissions and the second side of trades, messages, teleports, and visibility tests. |
| `offlineplayer` | A profile that joined once and is currently offline. |
| `console` | Dedicated server console. |
| `commandblock` | Command-block source tests in a disposable area. |
| `rcon` | Optional remote source tests when RCON is enabled in staging. |

Use a permission provider that can add, deny, unset, and refresh exact nodes while players remain online. A compatible LuckPerms build is suitable. LuckPerms NeoForge `5.4.140` failed player login in the recorded NeoForge `21.1.233` environment. That historical result does not prove behavior on `21.1.235`. Use a build verified for the server, or mark provider-specific rows blocked.

For every permission row, test all three states:

1. Node absent.
2. Node explicitly denied.
3. Node explicitly granted.

After each change, test before refresh, after provider refresh, after SEF reload, and after reconnect.

## Staging preparation

### Repository and Java

From the repository root:

```bash
pwd
git status --short
git rev-parse HEAD
java -version
./gradlew --version
```

Expected:

- Only known local evidence is untracked or modified.
- Java used by Gradle is Java 21.
- No production server directory is referenced.

Use this Java 21 prefix for every Gradle command on the reference Linux environment:

```bash
env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 PATH=/usr/lib/jvm/java-21-openjdk-amd64/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
```

### Snapshot

Create a unique backup directory:

```bash
SEF_TEST_BACKUP_DIR="$(mktemp -d)"
cp -a run/config "$SEF_TEST_BACKUP_DIR/config"
cp -a run/world "$SEF_TEST_BACKUP_DIR/world"
cp -a run/server.properties "$SEF_TEST_BACKUP_DIR/server.properties"
sha256sum build/libs/sef-2.0.0.jar
```

If `run/world` does not exist, start and stop the server once, then create the snapshot. Record `SEF_TEST_BACKUP_DIR`. Do not delete it until testing is accepted.

The development server uses port `25577` and is intentionally configured for local offline-mode testing. Do not expose it to an untrusted network.

### Build and automated gate

Run:

```bash
env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 PATH=/usr/lib/jvm/java-21-openjdk-amd64/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin ./gradlew clean test runGameTestServer build compileFallbackRuntimeJava generateProjectReferences generatePerformanceReport --rerun-tasks
```

Expected:

- 523 unit tests pass.
- 41 required GameTests pass.
- The command GameTests inspect all 694 catalog actions and 315 shortcuts, compile 2,213 representative parser variants, and execute 358 safe read only live routes.
- The build, fallback runtime, command reference, permission reference, configuration reference, and performance report complete.
- No generated reference changes remain after generation.
- `build/libs/sef-2.0.0.jar` is a valid ZIP.

Inspect the result:

```bash
unzip -t build/libs/sef-2.0.0.jar
sha256sum build/libs/sef-2.0.0.jar
git status --short
git diff --check
```

For the current audited worktree, the expected JAR SHA-256 is:

```text
262eb8dff9b7745ac07aa54a603398a0bb0feaa8dc73556f8d0e56b9cf98e9a0
```

A different hash is not automatically a bug after any source or resource change. It requires a new artifact inspection and recorded expected hash.

### Dedicated server

Start the server:

```bash
env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 PATH=/usr/lib/jvm/java-21-openjdk-amd64/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin ./gradlew runServer
```

At the server console, run:

```text
sef doctor
sef storage status
sef commands
sef conflicts
sef config status
sef config validate
sef control status
```

Current baseline:

- Server reaches `Done`.
- Catalog reports 694 entries.
- Capability manifest reports 11,937 capabilities.
- Shortcut registry reports 315 shortcuts.
- Configuration registry reports 62 modules.
- Storage coordinator reports 27 repositories.
- Security audit is healthy.
- Recovery mode is inactive on clean data.
- No kernel error is reported.

Finish with:

```text
stop
```

Expected:

- Every dimension saves.
- Every dirty managed repository either flushes or reports a bounded explicit failure.
- Gradle exits successfully.
- No client class is loaded by the dedicated server.

### GUI-off, enhanced-client, and fallback-client matrix

Run the complete matrix twice:

1. `run/config/sef/modules/gui.toml` with `runtime.enable_enhanced_gui = false`.
2. A copied staging configuration with `runtime.enable_enhanced_gui = true`.

Do not leave the repository development configuration changed after testing.

Start the GUI-enabled server, then start an enhanced client:

```bash
SEF_ENHANCED_DIR="$(mktemp -d)"
env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 PATH=/usr/lib/jvm/java-21-openjdk-amd64/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin ./gradlew runClient -PsefClientGameDirectory="$SEF_ENHANCED_DIR" -PsefClientQuickPlayServer=127.0.0.1:25577 -PsefClientUsername=SEFEnhanced
```

Start a no-SEF fallback client in a separate terminal:

```bash
SEF_FALLBACK_DIR="$(mktemp -d)"
env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 PATH=/usr/lib/jvm/java-21-openjdk-amd64/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin ./gradlew runFallbackClient -PsefFallbackClientGameDirectory="$SEF_FALLBACK_DIR" -PsefFallbackClientQuickPlayServer=127.0.0.1:25577 -PsefFallbackClientUsername=SEFFallback
```

Expected:

- Enhanced client negotiates the compatible protocol and remains connected.
- Fallback client contains no SEF classes, remains connected, and receives command-fallback guidance.
- GUI disabled does not prevent any command.
- GUI enabled never requires the client mod.
- Protocol mismatch falls back instead of kicking the player.
- Disconnect clears every server session, transfer, privileged draft, and client-scoped projection.

## Universal command test matrix

Apply this matrix to every action in `docs/COMMAND_REFERENCE.md`, not only the examples below.

Generate a review list:

```bash
sed -n '/^## Commands$/,/^## Shortcuts$/p' docs/COMMAND_REFERENCE.md | rg '^### `sef:'
sed -n '/^## Commands$/,/^## Shortcuts$/p' docs/COMMAND_REFERENCE.md | rg '^\\* Usage:'
sed -n '/^## Commands$/,/^## Shortcuts$/p' docs/COMMAND_REFERENCE.md | rg '^\\* Permissions:'
```

For each catalog entry:

1. Verify the documented canonical route exists.
2. Verify the documented convenience roots match `/sef conflicts`.
3. Verify the route is absent from suggestions without discovery permission.
4. Type the full route without permission and verify denial with no mutation.
5. Grant only the exact permission and verify only that action becomes available.
6. Explicitly deny the exact permission under a broader grant and verify the denial wins.
7. Revoke permission after suggestions arrive and immediately before execution.
8. Test all documented source classes. Deny undocumented sources.
9. Test missing arguments, invalid types, minimum values, maximum values, one below minimum, and one above maximum.
10. Test online, offline, self, equal-rank, higher-rank, exempt, vanished, unknown, and ambiguous targets where applicable.
11. Test the documented confirmation. Change an argument after preview, replay the token, and wait for expiry.
12. Test the canonical route, every active shortcut, the enhanced GUI, command fallback, alias, bundle, panel, and approved integration route. They must reach the same action id.
13. Confirm one shared cooldown, cost, quota, hierarchy, exemption, confirmation, and audit decision.
14. Disable the feature after admission but before mutation. Verify the documented fail-closed or finish-under-admitted-policy rule.
15. Restart with persistent state and confirm the state, revision, owner UUID, and expiry remain correct.
16. Inspect audit output. It must include stable metadata and real actor identity without secrets or private content.

A phase cannot pass if one of its catalog actions has no completed universal matrix row.

## Phase 1. Stabilization and security repair

### Command authorization

Test `/sef info`, `/sef reload`, `/sef test`, `/sef filter`, `/sef storage`, `/sef doctor`, `/sef commands`, and `/sef conflicts`.

Actions:

1. Give `playera` only the root discovery permission and the permission for `/sef info`.
2. Confirm `/sef info` is suggested and executes.
3. Confirm all ungranted children are absent and denied when typed.
4. Grant one child at a time.
5. Revoke it while the player remains online.
6. Confirm suggestion removal and direct denial after refresh.
7. Repeat after reconnect.
8. Repeat from console, RCON, and a command block.

Expected:

- Root visibility never grants child authority.
- Vanilla operator status does not create an unregistered SEF permission.
- A stale command tree cannot bypass execution-time permission checks.

### LuckPerms wildcard and provider bridge regression

Use a non-operator test player. Remove every inherited group grant before starting.

```text
/lp user playera permission unset sef.*
/lp user playera permission unset *
/lp user playera permission set sef.* true
/lp user playera permission check sef.commands.enchant.unsafe_level
/lp user playera permission check sef.commands.disguise.mob
/lp user playera permission check sef.commands.disguise.ability
```

Actions:

1. With only `sef.*` true, reconnect and verify the authorized SEF command tree appears.
2. Run an ordinary SEF command, `/disguise status`, and an unsafe enchant action that specifically requires `sef.commands.enchant.unsafe_level`.
3. Set `sef.commands.enchant.unsafe_level` false while `sef.*` remains true. Refresh LuckPerms data and verify the exact deny wins in suggestions and execution.
4. Unset the exact deny. Set `sef.commands.enchant.*` false. Verify that nearest wildcard deny wins over `sef.*`.
5. Set `sef.commands.enchant.unsafe_level` true while `sef.commands.enchant.*` is false. Verify the exact grant wins.
6. Repeat the same precedence test with `sef.commands.disguise.ability` and one exact trait node from `docs/PERMISSION_REFERENCE.md`.
7. Replace `sef.*` with global `*` and repeat one allowed and one explicitly denied action.
8. Trigger a permission refresh while a GUI, InvSee menu, warmup, confirmation, disguise, or delegated command is active. Verify stale authority is removed before mutation.
9. In a disposable compatibility environment, reproduce a transient NeoForge permission capability initialization failure while LuckPerms has a loaded direct grant. Verify the granted action continues, `/sef doctor` identifies the provider, and an explicit direct deny still wins.
10. Repeat the provider failure with no direct grant. The action must fail closed.
11. Repeat the bridge failure while creating an approval profile or access grant that delegates an authorized permission. A real exact or wildcard LuckPerms grant must work, an exact deny must win, and an access lease or one-execution sudo scope must not authorize delegation.

Expected:

- Resolution order is exact node, nearest wildcard, broader wildcards, then global `*`.
- The first defined false or true result wins.
- A transient NeoForge bridge failure never erases a real direct LuckPerms grant.
- Provider failure without a real grant never falls back to operator status or an internal permissive default.

### Announcement typing and duration parsing

Exercise text announcements, command announcements, countdowns, warnings, mutes, freezes, bans, reminders, and every other duration-bearing route.

Try:

```text
0
-1
+1
1.5s
1e3s
1ss
1x
999999999999999999999d
1s trailing
permanent
```

Also test empty input, repeated units, leading and trailing whitespace, control characters, and the exact valid minimum and maximum.

Expected:

- Invalid durations fail before creating or mutating state.
- `permanent` works only on actions that explicitly allow it.
- Text is never parsed as a command and command input is never broadcast as trusted text.

### Sudo stabilization regression

Phase 11 now owns the enabled sudo system. For the Phase 1 regression:

1. Remove all sudo permissions from every player.
2. Attempt `/sudo`, aliases to sudo, bundles containing sudo, profiles containing sudo, `/run sudo ...`, and `/silent sudo ...`.
3. Confirm none execute.
4. Enable and disable the sudo module through a staging configuration revision.
5. Confirm disablement removes execution authority, invalidates pending consent and grants, and leaves no delegated permission.

Expected:

- No legacy or unreviewed sudo path exists.
- Phase 11 is the only sudo implementation.

### Nickname authorization and collision

Use `/nick`, `/whois`, `/realname`, and every identity picker.

1. Grant self nickname permission but not other-player, color, style, or unsafe-format permissions.
2. Set a plain nickname.
3. Attempt another player, colors, styles, invisible formatting, unsafe Unicode, excessive visible length, and control characters.
4. Grant each formatting class separately.
5. Attempt collisions with online usernames, online nicknames, known offline usernames, and known offline nicknames.
6. Test case folding, Unicode normalization, lookalike characters, leading spaces, trailing spaces, and repeated whitespace.
7. Restart and resolve the retained nickname with `/whois`.
8. Select external nickname ownership and confirm SEF does not register or mutate a conflicting provider route.

Expected:

- Signed chat and authenticated UUID remain truthful.
- Ambiguous normalized identities are rejected.
- Exactly one provider owns nickname state.

### Vanish packet and revocation matrix

Use `/vanish`, `/v`, and administrative target forms.

1. Give `owner` a higher hierarchy than both staff actors.
2. Assign different `sef.vanish.N` and `sef.vanishsee.N` levels.
3. Observe every vanished actor from every other actor.
4. Check tab list, server-list sample, selectors, entity rendering, equipment, sounds, particles, chat, system messages, join and leave messages, collision, mob targeting, traces, nickname, Fancy Tags, and disguise projection.
5. Test hierarchy, exemption, hierarchy bypass, exemption bypass, and vanished-target permission independently.
6. Revoke vanish permission while active.
7. Repeat across dimension change, death, respawn, reconnect, permission refresh, configuration reload, and module disablement.
8. Confirm each observer receives an independent projection.

Expected:

- Unauthorized observers receive no real or synthetic artifact.
- Revocation safely restores tracking, tab state, action-bar state, and persistence.
- One observer's permission never leaks visibility to another observer.

### Inventory inspection split

Use `/invsee` and the online inventory menu.

1. Grant view only.
2. Attempt pickup, shift-click, number-key swap, throw, quick craft, hotbar swap, collect-to-cursor, drag, double-click, and creative clone actions.
3. Grant modify and perform one audited change.
4. Revoke modify while the menu remains open.
5. Revoke view while the menu remains open.
6. Test target logout, death, dimension change, respawn, and reconnect.
7. Test Curios absent, present without Curios permission, and present with Curios permission.

Expected:

- View never implies mutation.
- Permission loss downgrades or closes before the next click mutates state.
- Audit records issuer, target, page, slot, and click class without item NBT.

### Alternate-account privacy

Use test addresses only.

1. Keep address collection disabled and connect multiple clients.
2. Confirm no correlation records are created.
3. Enable hashed collection and reconnect.
4. Confirm raw addresses are absent from managed data, feedback, audit, logs, and exports.
5. Test `/checkalts <player>` without and with raw-view authority.
6. Test local-address exclusion, retention expiry, purge preview, purge confirmation, and export permissions.
7. Test `/checkalts purge expired`, `/checkalts purge confirm`, and `/checkalts export`.
8. Confirm an explicitly denied sensitive permission overrides broad administrative grants.

Expected:

- Collection is opt-in, retention is bounded, and raw values cannot be reconstructed from hashes.

### Phase 1 storage and performance regressions

1. Run `/sef storage status`.
2. Test one clean legacy fixture, malformed JSON, wrong domain, invalid schema, future schema, empty file, oversized file, and unknown retained fields.
3. Confirm backup before migration, migration journal, corruption quarantine, and no silent overwrite.
4. Populate configured limits for announcements, banned items, profiles, cooldowns, alternate accounts, and inventory integration.
5. Profile tab refresh, metadata refresh, audit queue, export queue, cooldown pruning, banned-item enforcement, and shutdown.

Expected:

- No unbounded scan, per-tick filesystem access, client classloading, or silent data loss.

## Phase 2. Shared command and policy kernel

### Catalog, permissions, conflicts, and diagnostics

Run:

```text
/sef commands
/sef commands 2
/sef conflicts
/sef doctor
/sef cooldown keys
/sef cooldown explain playera sef:workstation.craft
```

Actions:

1. Compare catalog count, action ids, permissions, sources, confirmations, GUI descriptors, quotas, and cooldown keys with the generated references.
2. Install a test mod that owns several convenience roots.
3. Confirm conflict diagnostics select one owner deterministically.
4. Confirm every canonical `/sef` route remains reachable.
5. Change structural shortcut policy and verify truthful restart-required reporting.

Expected:

- No action lacks a policy, permission, audit class, GUI or reviewed fallback, quota decision, cooldown decision, and source policy.

### Canonical pipeline

Select one action from every phase and run this order:

1. Feature gate.
2. Source check.
3. Permission decision.
4. Target resolution.
5. Hierarchy and exemption.
6. Quota reservation.
7. Cooldown decision.
8. Warmup.
9. Cost reservation.
10. Confirmation.
11. Mutation.
12. Commit or rollback.
13. Audit and observation.

Force a failure at every step. Record which later steps did not run.

Expected:

- No denied action mutates state, consumes quota, charges cost, or starts a committed cooldown.
- A failed mutation rolls back reservations and records a truthful result.

### Quotas

Test homes, player warps, mail, definitions, tag assignments, control records, parcels, auctions, and GUI sessions.

1. Test no provider, provider present, provider outage, finite tiers, zero, unlimited where supported, invalid metadata, multiple grants, and exact denial.
2. Fill each soft limit and one hard limit.
3. Run two concurrent admissions for the final available slot.
4. Cancel and fail reservations.
5. Refresh permissions during reservation.

Expected:

- Exactly one concurrent final-slot admission commits.
- Provider failure follows documented fallback and never silently becomes unlimited.
- Hard ceilings cannot be bypassed.

### Cooldowns, warmups, confirmations, and costs

1. Execute one action by canonical command, shortcut, GUI, alias, bundle, and approved sudo participant context.
2. Verify all routes share the canonical action cooldown.
3. Trigger movement, damage, logout, destination change, permission loss, feature disablement, and server stop during warmup.
4. Test confirmation expiry, replay, changed target, changed amount, changed revision, and wrong actor.
5. Test fixed, per-use, per-target, per-distance, and per-item costs.
6. Force failure after cost reservation and before mutation.

Expected:

- Cooldown and cost are committed only under documented success policy.
- Confirmation binds actor, action, arguments, target, revision, and expiry.
- A cancelled default teleport warmup refunds its reservation.

### Observation and redaction

Generate unique test markers through:

- Private messaging.
- Password-like and token-like arguments.
- Unknown command roots.
- Sudo, run, silent, bundle, alias, panel, console, RCON, command block, scheduler, and integration origins.

Expected:

- One correlated top-level lifecycle exists per command.
- Private bodies and secrets are absent from ordinary logs and audit.
- Unknown roots fail to a safe redaction class.
- Authorized observation remains active during `/silent`.

## Phase 3. Storage foundation and player profile

### Clean persistence

1. Create a nickname, home, cooldown, location history entry, GUI preference, and at least one record in every persistent phase used later.
2. Run `/sef storage status`.
3. Stop normally and hash managed files.
4. Restart and verify state and revisions.
5. Stop again without changes and verify stable files are not needlessly rewritten.

### Migration

For each supported legacy fixture:

1. Copy the fixture into a fresh staging world while the server is stopped.
2. Start the server.
3. Verify a timestamped pre-migration backup.
4. Verify the migration journal records source and target versions.
5. Verify normalized UUID ownership and unknown-field retention.
6. Mutate one migrated record, restart, and verify it.
7. Run `/sef storage status` and `/sef doctor`.

### Corruption and recovery

Repeat on copies of every repository class:

1. Malformed JSON.
2. Empty file.
3. Wrong domain.
4. Invalid schema.
5. Future schema.
6. Oversized document.
7. Truncated write.
8. Valid document with one unknown field.

Expected:

- Corrupt input is quarantined.
- Future data is refused.
- Recovery mode blocks unsafe mutation.
- The original evidence is never silently overwritten.
- Restoring the known-good file returns the next startup to ready state.

### Concurrency, crash, and shutdown

1. Cause multiple domains to become dirty at once.
2. Trigger periodic write while another mutation arrives.
3. Confirm the later mutation appears in a subsequent snapshot.
4. Kill the staging Java process after admission but before the next periodic flush.
5. Restart and verify only committed state is present.
6. Repeat during cost hold, bundle job, tag publication, parcel settlement, auction settlement, and backup checkpoint.
7. Stop normally while queues contain work.

Expected:

- Atomic replacement leaves no partial target.
- Restart recovery is idempotent.
- Bounded shutdown either flushes or reports the exact failure.

## Phase 4. Homes, teleports, spawn, warps, and RTP

### Homes

Run:

```text
/sethome
/sethome base
/sethome base confirm
/home
/home base
/homes
/homes playera
/renamehome base main
/delhome main
/delhome main confirm
/homeadmin list playera
/homeadmin teleport playera base
/homeadmin set playera base
/homeadmin delete playera base
/homeadmin rename playera base main
/homeadmin restore <record_id>
/homeadmin limit playera
/homeadmin export
```

Actions:

1. Test default names, normalized names, duplicate names, invalid names, quota, dimension quota, overwrite confirmation, rename collision, delete confirmation, and recovery.
2. Test an online owner, known offline owner where supported, ambiguous nickname, vanished owner, exempt owner, equal rank, and higher rank.
3. Test cross-dimension homes, missing dimensions, outside-border positions, unloaded chunks, void, lava, fire, cactus, suffocation, and obstructed arrival.
4. Trigger movement, damage, combat, permission loss, record revision change, and deletion during warmup.

Expected:

- Arrival is safe and within budget.
- Stale or deleted records cannot complete a warmup.
- Quota races create no extra home.

#### Enhanced homes screen

Use an enhanced client with the homes module enabled and grant only the permissions required by each row.

1. Open `/sef`, choose `Homes`, and confirm an `Add home` row appears when `sef.commands.sethome` is allowed.
2. Select `Add home`, enter a new unique name, preview it, and submit it. Confirm the home appears without reconnecting.
3. Select that home. Confirm the detail screen contains `Visit`, `Update location`, `Rename`, `Delete`, and `Back to homes` only when their matching command permissions are present.
4. Walk to a different block, select `Update location`, review the confirmation, and confirm. Teleport away and use `Visit`; arrival must use the new location.
5. Select `Rename`, enter another valid unique name, preview, submit, and confirm the list and direct `/home <new_name>` route use the new name.
6. Select `Delete`, cancel the first confirmation, then repeat and confirm. The canceled attempt must preserve the record; the confirmed attempt must remove it from the active list.
7. Revoke set, rename, delete, and use permissions one at a time while the detail screen is open. Refresh and attempt the stale control.
8. Create enough homes for multiple pages. Test search, previous, next, refresh, resize, GUI scale 1 through 4, keyboard focus, narration, and empty-state rendering.
9. Change or delete a home by command while its detail screen remains open, then press an old control.

Expected:

- Every GUI action reaches the same canonical home command and audit action as its command equivalent.
- Stale revisions and revoked permissions fail without changing a home.
- The empty screen still offers `Add home` when creation is authorized.
- No home name, location, or control belonging to another player is projected.

### Teleport requests

Run and test:

```text
/tpa playerb
/tpahere playerb
/tpaccept
/tpaccept playera
/tpdeny
/tpcancel
/tprequests
/tptoggle on
/tptoggle off
/tpblock playera
/tpunblock playera
/tpblocked
/tpautoaccept playera
/tpautoaccept off
```

Race actions:

1. Send opposite-direction requests at the same time.
2. Accept and deny the same request from separate inputs.
3. Cancel while the recipient accepts.
4. Logout, die, change dimension, enter combat, move, revoke permission, or block during warmup.
5. Expire the request and replay its identifier.
6. Test ignored, blocked, vanished, exempt, and higher-rank identities.

Expected:

- One terminal state wins.
- No stale request teleports a player.
- Request privacy never reveals a vanished identity.

### Direct teleport, back, spawn, and warps

Test the documented forms of:

```text
/tp
/tphere
/tpo
/tpohere
/tppos
/back
/spawn
/setspawn
/warp
/warps
/setwarp
/delwarp
/pwarp
/rtp
```

For each:

1. Test self and other target permission separately.
2. Test hierarchy, exemption, vanish, unknown world, missing dimension, world border, unsafe block, and unloaded chunk budget.
3. Create location history through death, direct teleport, home, warp, spawn, and RTP.
4. Use `/back` repeatedly and verify history order and bounded retention.
5. Create, publish, hide, suspend, report, transfer, restore, and delete player warps through their documented subcommands.
6. Test `owner:name` disambiguation, favorites, access policy, visit counting, conversion to home, and ownership transfer races.
7. Test RTP in every allowed dimension, near borders, with no valid destination, under claim-adapter denial, and under chunk budget exhaustion.

Expected:

- Canonical services own all mutations.
- A failed safe-teleport search leaves the player unchanged.
- FTB Essentials external and coexist modes do not create duplicate owners or routes.

## Phase 5. Social, identity, mail, and connection messages

### Private messaging and ignore

Test:

```text
/msg playerb phase5-private-marker
/tell playerb phase5-tell-marker
/w playerb phase5-whisper-marker
/r phase5-reply-marker
/ignore playerb
/ignorelist
/msgtoggle
```

Actions:

1. Test online, vanished, ignored, message-disabled, exempt, ambiguous, and offline targets.
2. Test reply after sender logout, identity rename, vanish, ignore, and reconnect.
3. Confirm ordinary logs, audit, profiles, exports, and unrelated observers do not contain message bodies.
4. Confirm signed chat identity is not forged.

### Social spy

Exercise `/socialspy` and `/sef socialspy`:

1. Toggle everyone, one-player, and multi-player UUID audiences.
2. Select sender, recipient, and either matching.
3. Test metadata-only and content scopes.
4. Add and remove route filters.
5. Test own conversation, exemptions, hierarchy, vanish, duplicate adapter events, permission revocation, and rate limits.
6. Inject legacy color markers, placeholder-like text, newlines, and control characters.

Expected:

- Each eligible event is delivered once.
- Formatting cannot escape its typed placeholder.
- Content is visible only with the exact content permission.

### Mail

Use the current `/mail` subcommands shown by tab completion and the command reference.

1. Send to online and known offline players.
2. Read, mark, page, delete, and clear mail.
3. Fill mailbox and message-size limits.
4. Test ignore, retention, expiry, unknown identity, ambiguous identity, and vanished identity.
5. Force persistence failure before commit.
6. Restart after send and after delete.

Expected:

- Mail is UUID-owned and bounded.
- A failed write creates no phantom delivery or deletion.
- Bodies stay out of ordinary audit.

### Identity, connection messages, reminders, and custom text

1. Repeat the Phase 1 nickname matrix across chat, tab, join, leave, death, advancement, suggestions, target resolution, lists, GUI, and external adapter surfaces.
2. Configure custom join and leave templates.
3. Join normally, while vanished, after a kick, and after a failed login.
4. Create one-time and repeating reminders for each audience predicate.
5. Test acknowledgement, dismissal, maximum deliveries, interval, restart, and permission loss.
6. Create a custom text command using safe typed text.
7. Attempt command placeholders, executable text, control characters, over-limit text, and collision with an owned root.

Expected:

- Join and leave output describes real connection state.
- Vanished players do not leak.
- Reminders do not spam after acknowledgement or configured maximum.
- Custom text cannot become command execution.

## Phase 6. Moderation and protection

### Warning, mute, ban, and kick matrix

Test the current forms of:

```text
/warn
/warns
/clearwarnings
/mute
/unmute
/mutelist
/ban
/tempban
/pardon
/unban
/ban-ip
/banip
/tempban-ip
/pardon-ip
/unban-ip
/kick
/kick-ip
/kickip
/kickme
/kickall
```

For each other-player action:

1. Test self, equal rank, higher rank, exempt target, exemption bypass, hierarchy bypass, vanished target, unknown player, known offline player, and ambiguous nickname.
2. Test invalid duration classes and the exact maximum.
3. Restart before and after expiry.
4. Attempt duplicate punishments and conflicting expiry updates.
5. Confirm vanilla ban lists remain authoritative where specified.
6. For `/kickall`, verify target preview, cap, confirmation, changed online cohort, protected-player exclusion, and replay rejection.

Address tests:

1. Use only staging addresses.
2. Test direct, trusted-proxy, external, and disabled address modes.
3. Send invalid forwarded data, an untrusted proxy header, and a likely shared proxy address.
4. Verify fail-safe behavior and redaction.

Expected:

- Invalid or untrusted address evidence cannot punish a shared proxy population.
- IP data is redacted in feedback, GUI, audit, and files.

### Persistent controls

Test jail, freeze, inventory lock, building disablement, and banned-item enforcement.

Use:

```text
/setjail
/deljail
/jails
/jail
/unjail
/jailedplayers
/freeze
/unfreeze
/freezelist
/invlock
/disablebuilding
/db
```

1. Apply, inspect, expire, revoke, and restart each control.
2. Test movement, teleport, command, interaction, item use, item toss, inventory packets, block break, block place, bucket, entity interaction, and menu behavior as applicable.
3. Test higher rank, exemption, bypass, vanish, death, respawn, dimension change, reconnect, and permission loss.
4. Disable each module while its control is active.
5. Corrupt the moderation repository and confirm fail-closed recovery.

Expected:

- Enforcement is server-side.
- Client packet variants do not bypass the control.
- Expiry and revocation restore normal state exactly once.

### Command spy

Use `/commandspy` and `/sef commandspy` to test:

- Everyone and selected-player audiences.
- Initiator, effective actor, and either relation.
- Console, player, RCON, command block, function, panel, bundle, sudo, scheduler, profile, and integration sources.
- Root, action, result, location, and source filters.
- Hidden identities, hierarchy, exemptions, last-selection removal, rate limits, and deduplication.

Send unique secret markers through private, authentication-like, unknown, and sensitive roots.

Expected:

- Authorized observers receive one truthful correlated result.
- Denied, private, and secret fields are redacted before filtering and storage.

### Optional file logging

Keep `runtime.file_logging_enabled = false` for the disabled-state test:

1. Start and stop the server.
2. Confirm no writer starts and no new `logs/sef` directory is created by SEF.

Then enable it only in a copied staging configuration and test:

```text
/sef logging status
/sef logging enable
/sef logging disable
/sef logging stream list
/sef logging filter list
/sef logging recent
/sef logging search
/sef logging stats
/sef logging rotate
/sef logging flush
/sef logging retention preview
/sef logging retention run
/sef logging export
/sef logging doctor
/sef logging repair
```

Also test:

1. Queue overflow.
2. Disk-full or write-denied fixture.
3. Invalid filename.
4. Symlink inside the owned logging path.
5. Rotation boundary.
6. Retention confirmation bound to current state.
7. Incomplete shutdown marker and restart recovery.
8. MaxLogger present and absent.

Expected:

- SEF writes only under its fixed owned path.
- Symlinks and traversal are rejected.
- Logging failure does not silently claim success.
- MaxLogger state and files are never mutated.

## Phase 7. Inventory, workstations, kits, and player utilities

### Kits

Use `/kit` and `/kits`:

1. Create a kit from an inventory containing vanilla items, damage, enchantments, names, lore, books, containers, and components.
2. List, show, validate, edit, export metadata, claim, reset one player, and delete.
3. Restart and compare exact item state.
4. Claim with enough space, no space, and bounded overflow dropping enabled.
5. Test cooldown, one-time use, per-kit `sef.kits.<id>` permission, live revocation, definition deletion, and stale revision.
6. Fill definition, stack, history, and serialized-size limits.
7. Test corrupt items, missing registry ids, future schema, and repository recovery.

Expected:

- Every successful claim delivers each stack once.
- Failed admission changes neither inventory nor claim history.

### Inventory and item utilities

Test:

```text
/clearinventory
/ci
/enderchest
/ec
/invsee
/invsee playerb
/disposal
/more
/condense
/hat
/itemname
/itemlore
/itemdb
/book
/recipe
/i cobblestone 64
/i minecraft:cobblestone 64
```

Actions:

1. Test self and other-player permissions separately.
2. Repeat the live-menu revocation matrix from Phase 1.
3. Test empty hand, stackable, unstackable, damaged, named, enchanted, container-bearing, and component-bearing items.
4. Test safe swaps, capacity failure, missing recipes, invalid ids, zero, negative, nonnumeric, and one above configured amount.
5. Verify `/i` is self-only and rejects appended player or selector arguments.
6. Test console and command-block source refusal for player-only routes.
7. Force close disposal during server stop and verify no duplication.
8. With an enhanced client, run bare `/invsee`. Select `playerb` from the player picker instead of typing a name.
9. Confirm the upper six-row area shows `playerb` inventory, armor, offhand, and navigation controls, while the lower three rows and hotbar show the viewer inventory.
10. Install Curios on both sides, equip several slot types on `playerb`, grant `sef.commands.invsee.curios`, and use the arrow controls to visit every Curios page.
11. Remove `sef.commands.invsee.curios` while a Curios page is open. The menu must close or return to an authorized page.
12. Test view-only mode without `sef.commands.invsee.modify`. Attempt pickup, quick move, number-key swap, offhand swap, drag, double-click collect, drop, and creative manipulation.
13. Grant modify permission and repeat valid moves between target slots and the viewer inventory. Confirm the target sees each live change once.
14. Revoke modify permission while holding a target stack on the cursor. Attempt another click and close the screen. Confirm no duplication, deletion, or unauthorized commit.
15. Disconnect the target, kill the target, change the target dimension, reload the inventory module, and change policy revision while the menu is open.
16. Test an enhanced viewer, a command-only viewer, and a client without SEF. Enhanced clients receive the two-inventory screen; fallback clients retain the authoritative container command path.
17. Test minimum practical window size, GUI scales 1 through 4 and Auto, JEI present and absent, resize, long target names, and all pages. The lower inventory and hotbar must stay inside the panel.

Expected:

- Failure preserves exact inventory slots and components.
- Disposal destroys only items intentionally placed into its server-owned menu.
- The target inventory is always above the viewer inventory and every displayed slot remains server authoritative.
- Curios slots are absent without their permission and available only when the adapter is healthy.
- A stale or unauthorized click changes neither inventory.

### Player utilities

Test:

```text
/afk
/feed
/heal
/fly
/god
/rest
/speed
/exp
/ptime
/pweather
/near
/getpos
/compass
/depth
/top
/bottom
/jump
/suicide
```

Actions:

1. Damage a player, set food below `20`, and give that player nonzero saturation. Run both self and eligible other-player `/feed` forms. Inspect the state immediately after each command.
2. Test self and eligible other-player forms for the remaining utilities.
3. Revoke permissions while fly, god, personal time, or personal weather is active.
4. Test speed and experience minimum, maximum, underflow, overflow, and invalid type.
5. Test utility routes in Overworld, Nether, and End.
6. Test unsafe top and bottom destinations, unloaded chunks, vanished targets, and protected targets.
7. Keep suicide disabled by default. If enabled in staging, verify it is separately permissioned and self-only.

Expected:

- `/feed` immediately leaves the target at food level `20`, saturation `0.0F`, and the exact pre-command health value. Any later natural regeneration must come only from normal vanilla rules and ticks.
- `/heal` remains the separate explicit recovery command.
- Long-lived states reconcile after permission or feature loss.
- Navigation utilities never teleport into a rejected hazard.

### Gamemode, workstations, and super enchanting

Test:

```text
/gm creative
/gm survival
/gm spectator
/gm adventure
/gmc
/gms
/gmsp
/gma
/anvil
/cartographytable
/grindstone
/loom
/smithingtable
/stonecutter
/workbench
/wb
/superenchantingtable
/set
```

Actions:

1. Test gamemode self and other-player permission separately.
2. Test names, initials, and supported numeric compatibility values.
3. Test hierarchy, exemption, vanish, ambiguous nickname, selector cap, console target, and live revocation.
4. Open and use each virtual workstation with an unmodified client.
5. Verify item return behavior matches its vanilla menu.
6. Confirm canonical and shortcut routes share action id and cooldown.
7. Create a root collision for `/set`. Confirm only `/set` disables and `/superenchantingtable` remains.
8. Change configuration, item, selected slot, registry, permission, and menu revision while super enchanting is open.

Expected:

- Stale menus cannot mutate.
- A shortcut never weakens canonical policy.

## Phase 8. Native economy and signs

### Provider ownership

Run the matrix in `native`, `external`, `disabled`, and `import_once` modes.

1. Native mode creates and persists native accounts.
2. External mode creates no native shadow balance.
3. Missing or failed configured external provider fails clearly.
4. Disabled economy exposes no mutating economy route or charge.
5. Import-once without an importer fails.
6. Successful import makes native the only owner.
7. A second import and later synchronization fail.

### Money and ledger

Test `/balance`, `/bal`, `/pay`, `/baltop`, `/eco`, `/worth`, and `/sell`.

Inputs:

- Zero.
- Minimum unit.
- Maximum amount.
- One unit above maximum.
- Excess fractional precision.
- Exponent notation.
- Leading plus and minus signs.
- Whitespace.
- Non-numeric input.

Actions:

1. Deposit, withdraw, transfer, set, reset, freeze, and unfreeze.
2. Restart after every mutation type.
3. Replay an identical idempotency key.
4. Replay the key with different account, amount, currency, reason, or metadata.
5. Fill account, ledger, pending-hold, worth, and transaction limits.
6. Test online, offline, unknown, ambiguous, vanished, exempt, higher-rank, equal-rank, and self targets.
7. Test payment toggle, ignore, offline payment, self payment, and each bypass independently.
8. Cross the confirmation threshold, then alter target or amount.
9. Inspect paged history.

Expected:

- Arithmetic uses exact minor units.
- Formatting never affects arithmetic.
- One idempotency key creates at most one matching mutation.
- A rejected mutation changes no account, ledger, cache revision, or hold.

### Worth, sale, and command costs

1. Set and remove worth for namespaced items.
2. Quote hand, explicit item, and complete inventory.
3. Sell plain items and attempt damaged, named, enchanted, container-bearing, written-book, and component-bearing stacks.
4. Change inventory after quote.
5. Fill the destination balance.
6. Force provider failure after validation.
7. Configure fixed, per-use, per-target, per-distance, and per-item cost components separately and together.
8. Kill the server with an uncommitted native cost hold.
9. Restart and verify one refund.

Expected:

- Failed sale restores exact slots and components and credits nothing.
- Successful sale removes each eligible item once and records one exact credit.
- Cost failure releases cooldown and reservation under documented policy.

### Economy signs

Create and use both sides of each sign:

```text
balance
buy
sell
trade
free
disposal
kit
heal
repair
time
weather
warp
```

Actions:

1. Test type disabled, create denied, use denied, owner mismatch, owner bypass, and expired placement claim.
2. Edit each line and verify fingerprint invalidation.
3. Break, replace, piston-move where supported, and explode the sign.
4. Unload the chunk during interaction.
5. Test invalid ids, invalid options, control characters, extra lines, excessive quantity, excessive value, zero, and negative values.
6. Fill inventory before buy, trade, and free.
7. Remove required items before sell and trade.
8. Exhaust balance before every charged sign.
9. Force provider and linked-command failure.
10. Use `/eco sign list`, `/eco sign info`, `/eco sign remove <id> confirm`, and `/eco sign adopt`.
11. Restart and retest registered signs.

Expected:

- Inventory and balance commit atomically.
- Failed use restores both deterministically.
- Sign-side position, creator UUID, fingerprint, and revision persist.

### Import and performance

1. Preview an external fixture with minimum, maximum, ordinary, duplicate, invalid, and excessive accounts.
2. Compare preview count and total with export.
3. Change the export after preview.
4. Force pre-import persistence, backup, report, and final persistence failures separately.
5. Confirm failed commit restores empty in-memory state and retains the backup.
6. Populate 100,000 accounts.
7. Profile first and cached balance-top reads.
8. Rapidly use one sign from multiple players.
9. Stop normally and force termination with dirty economy state.

Expected:

- Import requires an unchanged reviewed source.
- Successful import writes one aggregate report and one ownership transition.
- Cached ranking does not sort the live account map until its revision changes.

## Phase 9. Client protocol and GUI pilot

Run this phase with:

- GUI disabled.
- GUI enabled with a compatible enhanced client.
- GUI enabled with the no-SEF fallback client.
- GUI enabled with a deliberately incompatible protocol fixture.
- Enhanced and fallback clients connected at the same time.

### Negotiation and session lifecycle

1. Connect the fallback client.
2. Confirm it receives no SEF play payload and remains connected.
3. Connect the enhanced client.
4. Run `/sef gui status` and `/sef gui client status`.
5. Confirm the server reports a connection-bound session, negotiated feature mask, session UUID, revision, and current preference.
6. Disconnect and reconnect.
7. Confirm the old session identifier, sequence, panels, transfers, and projections are invalid.
8. Change permission and module policy while connected.
9. Confirm feature masks and active screens update or close.
10. Attempt a protocol-major mismatch.
11. Attempt an unsupported minor feature.

Expected:

- Major incompatibility uses command fallback.
- Unsupported optional features are removed from negotiation.
- No client is kicked only because enhanced GUI is missing or incompatible.
- Session state never survives disconnect.

### Forged, stale, replayed, and oversized payloads

Use the protocol test fixture or focused automated tests to send:

- Wrong negotiation nonce.
- Wrong player identity.
- Wrong session UUID.
- Replayed sequence.
- Out-of-order sequence.
- Unknown panel id.
- Unknown control id.
- Unknown entry id.
- Stale panel revision.
- Stale target revision.
- Stale configuration revision.
- Excess string, component, page, panel, HUD, identity, query, image, or chunk sizes.
- Invalid PNG signature, invalid IHDR order, oversized dimensions, excessive pixel count, truncation, and hash mismatch.

Expected:

- Every invalid payload is rejected before mutation or native image decode.
- Repeated abuse is bounded and audited.
- Another player's session, target, hidden identity, or transfer cannot be referenced.

### Pilot screens

Open:

```text
/sef gui
/sef gui homes
/sef gui warps
/sef gui teleport_requests
/sef gui help
/sef gui staff
/sef gui players
```

Also use the pause-screen entry and configured keybind.

For each screen:

1. Test GUI scales 1 through 4.
2. Test 854 by 480, 1280 by 720, a narrow aspect ratio, and a wide aspect ratio.
3. Resize while open.
4. Use keyboard only.
5. Use mouse only.
6. Use narration.
7. Test long translated strings and empty results.
8. Test search, page forward, page backward, refresh, selection, detail, back, close, and confirmation.
9. Revoke permission while open.
10. Delete or revise the selected target while open.
11. Change dimension and disconnect while open.

Expected:

- Controls stay on screen, do not overlap, and have readable focus.
- Narration identifies the screen, focused control, validation error, and result.
- Every submitted action is revalidated by the server.
- Command fallback can perform the same action.

### Private HUD and identity

Test HUD tiles for:

- Vanish.
- Social spy.
- Command spy.
- AFK.
- Flight.
- God mode.
- Teleport warmup.
- Maintenance and Phase 13 state where applicable.

Actions:

1. Toggle each state and inspect revisioned deltas.
2. Revoke the underlying permission.
3. Disable the owning module.
4. Disconnect and switch servers.
5. Combine scoreboard, boss bar, action bar, Fancy Tags, nickname, disguise, and HUD output.

Expected:

- HUD shows private state only to the owning authorized player.
- Old deltas cannot restore revoked state.
- Non-enhanced clients receive documented action-bar, boss-bar, text, or no-HUD fallback.

## Phase 10. Universal GUI coverage

Phase 10 is complete only when every player-facing catalog action has a dedicated typed workflow or a reviewed `command_only` reason.

### Coverage audit

Run:

```text
/sef guis coverage
/sef guis doctor
/sef guis status
/sef commands
```

Compare output with all 694 entries in `docs/COMMAND_REFERENCE.md`.

For every player-facing action:

1. Confirm a GUI descriptor exists.
2. Confirm the descriptor contains a real feature workflow, not an unrestricted command string.
3. Confirm a command fallback exists.
4. Confirm a HUD decision or explicit no-HUD rationale exists.
5. Confirm unauthorized entries and controls are absent.
6. Confirm an open-screen permission does not grant a control permission.

Expected:

- No catalog action is silently omitted.
- No generic execute button substitutes for a required feature workflow.
- No GUI request carries raw command text.

### Dedicated workflow matrix

At minimum, fully test workflows for:

- Homes.
- Teleport requests.
- Warps and player warps.
- Private messages.
- Mail.
- Item give.
- Kits.
- Inventory inspection.
- Normal enchantment.
- Super enchantment.
- Gamemode.
- Moderation.
- Economy.
- Aliases and bundles.
- Fancy Tags.
- Disguise.
- All Phase 13 control features.
- Configuration.

For each:

1. Open from the bare command.
2. Open from dashboard and category navigation.
3. Enter valid typed values.
4. Enter invalid values and boundary values.
5. Search registry-driven values.
6. Preview.
7. Confirm.
8. Change target, revision, item, permission, or policy before submit.
9. Inspect progress and result.
10. Retry a recoverable failure.
11. Complete the equivalent explicit command.

Expected:

- Command and GUI produce the same domain result and audit action id.
- The server owns suggestions, validation, preview, confirmation, and mutation.

### Bare command and player picker regression

Use two enhanced players, one known offline profile, one never-seen name, and one vanished player.

Run each bare command:

```text
/msg
/give
/enchant
/invsee
/disguise
```

Then run complete forms:

```text
/msg playerb hello
/give playerb minecraft:stone 3
/enchant @s minecraft:sharpness 5
/invsee playerb
/disguise mob minecraft:blaze
```

Actions:

1. Confirm each bare command opens its dedicated typed workflow only for an authorized compatible client.
2. Confirm each complete form executes immediately and does not open a workflow.
3. Set the effective GUI policy to `command_only`, then `off`, and repeat. Bare commands must show their established usage or command fallback rather than `This action has no valid typed workflow`.
4. Repeat from console, RCON, command block, function, a client without SEF, and a client with an incompatible protocol minor. Player-only GUI roots must not alter non-player command behavior.
5. In each player field, open the picker. Confirm it lists every bounded known profile the viewer may see, marks online and offline state, supports authenticated username and nickname search, and never reveals an unauthorized vanished profile.
6. Cycle the filter through `all players`, `online`, and `offline`. Confirm each result set is correct, current state refreshes when the picker opens, paging remains stable, and an individual selection uses the authenticated command name.
7. Revoke the action permission, target visibility, hierarchy access, or enhanced feature while the picker or workflow is open. Submit the stale selection.
8. Enter a never-seen name. It must not be treated as an authenticated offline profile.

Expected:

- No bare route reports `This action has no valid typed workflow`.
- A GUI preference never changes the behavior of a complete command.
- Search and filter happen against bounded server-projected profiles, not a client-owned authority list.
- Permission or visibility loss invalidates the workflow before execution.

### Give picker and batch regression

Prepare three visible online players, two known offline profiles, one unauthorized vanished player, and at least one modded item.

1. Enter bare `/give`. Confirm the first mode is `one item` and the alternate mode is `custom amount`.
2. Open the target picker. Cycle `all players`, `online`, and `offline`; search by authenticated username and nickname in each view.
3. Check two individual online players and one offline player, uncheck one row, change pages, return, and confirm the remaining checks persist.
4. Press `use selected`. Preview and confirm the displayed route summarizes the selected count instead of exposing an oversized comma-separated command.
5. Run the batch. Confirm each currently online selected UUID receives exactly one grant and the offline UUID creates exactly one pending login action.
6. Repeat with `all online`. Confirm only visible players online at preview are frozen into the batch. A player who joins after preview must not be added.
7. Repeat with `everyone`. Confirm every visible known UUID within the `1000` target ceiling is frozen. Online targets run now and offline targets queue independently.
8. Vanish a target after opening the picker but before preview. Preview must reject or remove that stale target.
9. Disconnect one frozen target after preview but before submit. Confirm that UUID changes from immediate execution to one login queue record without changing identity.
10. Revoke the issuer’s give permission after preview. Submit must invalidate without giving or queueing anything.
11. Forge the bulk token, a hidden username, an unknown username, a selector, an empty list, duplicate names with different case, and `1001` distinct targets. Confirm hidden, unknown, selector, empty, and oversized submissions fail. Confirm duplicates collapse to one UUID.
12. Open the item picker. Confirm it shows `All items`, vanilla creative tabs, item icons, normal item tooltips, tab paging, and item paging. Confirm no item or tab name is drawn over an icon and hovering an item produces exactly one tooltip.
13. Search by localized item name, `minecraft:diamond`, a namespace fragment, and a modded namespaced id. Confirm clicking an entry returns its canonical registry id.
14. Remove the selected mod or registry item between preview and submit. The server must reject the stale id without substituting another item.
15. In custom amount mode, use `-10`, `-1`, `+1`, and `+10`. Confirm the value clamps to the compiled minimum and maximum and no typed amount is required.
16. Resize at GUI scales 1 through 4 while the player picker, item picker, and confirmation view are open. Confirm buttons do not overlap, selection remains intact, search retains focus, and tooltips remain inside the screen.
17. Navigate every control by keyboard and narration. Confirm selected rows, filter state, item identity, amount, page counts, preview, and result are understandable without color alone.

Expected:

- The client never supplies an authoritative UUID, permission result, item object, target set, or final mutation.
- Preview freezes only server-visible UUIDs. Submit rechecks the action, permission, visibility, target, item registry entry, amount, and canonical route.
- Batch results separately report grants run now, actions queued for login, and failures.
- Each target is processed once. A partial target failure does not duplicate successful grants.

### Offline give queue

This queue is intentionally limited to the reviewed enhanced `/give` workflow. Direct command text and unrelated workflows are not persisted. A multi-target give stores one UUID-bound record for each selected offline player.

1. Ensure `playeroffline` has joined at least once, then disconnect that player.
2. Run bare `/give`, select `playeroffline`, select `minecraft:diamond`, set amount `2`, preview, and run.
3. Verify the result reports one queued target and `<world>/serverconfig/sef/offline-actions.json` contains one bounded pending record.
4. Restart the server before the target joins. Verify the record remains pending.
5. Join as `playeroffline` while the issuer remains online. Verify exactly two diamonds are granted and the queue record reaches its terminal state.
6. Queue another action, disconnect the issuer, then join the target. Verify execution waits until both authenticated players are online.
7. Queue another action, revoke `sef.commands.item.give.others` from the issuer, and join the target. Verify the recheck refuses the grant.
8. Queue another action, disable the items module or action route, and join the target. Verify no item is granted.
9. Queue another action, change the target nickname and authenticated username projection, and join. Verify execution binds the stored UUID and substitutes the current authenticated command name.
10. Queue a mixed selection containing two online players and two offline players. Confirm two grants run immediately and exactly two independent records persist.
11. Attempt to queue `/msg`, `/invsee`, `/enchant`, `/disguise`, a selector, an unknown profile, excessive amount, invalid item, and a forged workflow payload.
12. Advance a copied record beyond its seven-day expiry and restart.
13. Corrupt, oversize, duplicate, or schema-upgrade a copied `offline-actions.json`.

Expected:

- A queued give re-runs workflow compilation, permission, feature, policy, field, target, registry, amount, and canonical command validation at execution time.
- Permission or policy loss fails closed.
- Only one successful grant occurs after reconnect or restart.
- Unsupported actions never enter persistent storage.
- Repository corruption enters the documented recovery state instead of executing untrusted data.

### Panel drafts and publication

Use the current admin-panel commands and GUI:

1. Create two drafts from one published revision.
2. Edit controls, layout, target selectors, execution context, and visibility.
3. Attempt overlap, invalid span, unknown action, unknown permission, unrestricted command, and excessive control count.
4. Publish the first draft.
5. Attempt to publish the stale second draft.
6. Roll back to an older immutable revision.
7. Restart and inspect publication history.
8. Attempt forged panel, control, entry, revision, and target references.
9. Test list, inspect, run, draft, publish, and rollback permissions independently.

Expected:

- Optimistic conflicts prevent lost updates.
- Publication is immutable and rollback creates a reviewed new state.
- Runtime requests cannot add command text or authority.

### Batch and participant execution

1. Select a small authorized cohort for same-tick admission.
2. Select an oversized cohort.
3. Test paced execution.
4. Test strict actor, strict participant, approved delegated participant, and server profile contexts separately.
5. Remove one participant's permission before admission and during pacing.
6. Change target membership after preview.
7. Disconnect the issuer.

Expected:

- Target cohort freezes at the documented boundary.
- Oversized all-or-nothing admission changes nothing.
- Each participant retains separate permission and audit identity.

## Phase 11. Aliases, bundles, profiles, fake identity, sudo, run, and silent

### Alias lifecycle

Use:

```text
/sef alias list
/sef alias create
/sef alias validate
/sef alias inspect
/sef alias publish
/sef alias run
/sef alias disable
/sef alias rollback
/sef alias delete
/sef alias help
```

Use tab completion and the generated reference for required ids, revisions, schemas, and arguments.

Test:

1. Literal alias with no argument.
2. Typed player, integer, enum, resource id, and greedy-text arguments.
3. Direct-target alias.
4. Unknown action.
5. Unknown permission.
6. Recursive self-reference.
7. Multi-alias cycle.
8. Collision with SEF, vanilla, and another mod.
9. Ambiguous target.
10. Draft validation, publication, activation after restart, disablement, rollback, and deletion.
11. Live permission revocation.

Expected:

- Alias arguments compile to typed bindings.
- An alias cannot weaken canonical policy or create raw command forwarding.
- Structural activation and conflict behavior are truthful.

### Bundle lifecycle

Use:

```text
/sef bundle list
/sef bundle create
/sef bundle edit
/sef bundle inspect
/sef bundle preview
/sef bundle publish
/sef bundle run
/sef bundle cancel
/sef bundle recover
/sef bundle rollback
/sef bundle disable
/sef bundle delete
```

Test:

1. One-step strict-actor bundle.
2. Multi-step bundle with a bounded delay.
3. Conditional branch.
4. Frozen target cohort.
5. Small paced fan-out.
6. Cycle, excessive depth, excessive fan-out, invalid binding, unknown action, and unsupported compensation.
7. Permission, hierarchy, feature, policy, target, and profile revision changes between steps.
8. Issuer disconnect, server stop, forced termination, recovery, cancellation, and deadline.
9. Replay recovery after a committed step.

Expected:

- Every step revalidates at execution time.
- A completed mutation is never duplicated during recovery.
- Cancellation and compensation follow the published plan only.

### Command profiles

Use:

```text
/sef profile list
/sef profile create
/sef profile validate
/sef profile test
/sef profile inspect
/sef profile publish
/sef profile reference
/sef profile execute
/sef profile rollback
/sef profile enable
/sef profile delete
```

Test actor, targeted-actor, and server contexts.

Expected:

- A targeted profile requires a server-bound `{target}` placeholder.
- Server profiles remain disabled until explicitly enabled.
- Unsupported redirects, forks, and unrestricted text cannot publish.
- References prevent unsafe deletion.

### Fake identity

Use:

```text
/fakejoin
/fakeleave
/fakemessage
/fakerankmessage
/sef fake profile
/sef fake scene
/sef fake schedule
```

Test:

1. Real online identity metadata.
2. Known offline identity metadata.
3. Unknown synthetic identity defaults.
4. Vanished identities and mixed viewer permissions.
5. Join, leave, message, rank message, scene, and schedule.
6. Schedule restart, cancellation, duplicate firing, and expiry.
7. Text containing click-like, hover-like, formatting, newline, and command-like content.

Expected:

- Output is unsigned system presentation and never signed chat.
- Real authenticated state, online count, player profile, balance, permissions, homes, bans, and statistics remain unchanged.
- Each schedule executes at most once.

### Sudo

Use:

```text
/sudo consent
/sudo policy
/sudo lock
/sudo dryrun
/sudo chat
/sudo run
```

Also test the compatibility form:

```text
/sudo playera false <command>
/sudo playera true <command>
```

Actions:

1. Verify consent defaults to denied.
2. Test target allow, deny, revoke, reconnect, and expiry.
3. Test self, hierarchy, exemption, vanish, administrative lock, and target notification.
4. Run `false` mode against a command the target lacks.
5. Run `false` mode against a command the target has.
6. Test `true` mode without a reviewed delegation.
7. Create an exact reviewed grant bound to profile, root, command digest, target connection, thread, sequence, and expiry.
8. Execute it once.
9. Replay it, alter one byte, switch target, reconnect, change profile, refresh permission, change command tree, and wait for expiry.
10. Attempt selectors, redirects, forks, recursion, `execute`, functions, schedules, aliases, bundles, panels, wrappers, command blocks, external adapters, and asynchronous reuse.
11. Force denial and exception after grant consumption.

Expected:

- `false` uses the target's real authority.
- `true` works only for one exact reviewed execution.
- The grant is consumed before dispatch and removed in cleanup.
- Operator state, provider data, groups, persistent player data, and permanent command trees never change.

### Run and silent

Use:

```text
/run server <command>
/silent actor <command>
/silent server <command>
```

Actions:

1. Test an allowlisted harmless root.
2. Test a denied root.
3. Test a root missing its exact root permission.
4. Test required confirmation and changed nested command.
5. Test player target preflight.
6. Attempt nested run, silent, sudo, alias, bundle, panel, profile, function, schedule, redirect, and fork paths.
7. For `/silent`, test success feedback, failure feedback, broadcast output, independent mod output, security failure, command spy, file logging, and audit.
8. Test a command whose output cannot be safely suppressed.

Expected:

- `/run` uses a real server source and only reviewed roots.
- `/silent` suppresses only documented command-source feedback.
- Security audit, command journal, authorized observation, and enabled SEF logging remain active.
- Unsuppressible output produces a truthful refusal or warning.

## Phase 12A. Fancy Tags

Use a compatible enhanced client, a fallback client, and a second enhanced viewer.

### Module authority and command availability

1. Set `[module].enabled = true` in `config/sef/modules/fancy_tags.toml`.
2. Run `/sef config reload fancy_tags`, `/sef config inspect fancy_tags`, `/sef tags status`, and `/sef tags doctor`.
3. Confirm the inspected module is enabled and the tag commands do not answer `That feature is currently disabled`.
4. Set the legacy Fancy Tags bootstrap value to the opposite state on a copied configuration, reload through the supported path, and confirm the published module state remains authoritative.
5. Disable the module through its typed file, reload, and confirm commands fail closed with a clear module-state response.
6. Re-enable it and confirm command visibility, dashboard entry, manager screens, and transfer feature return after session refresh.
7. Repeat with `sef.*`, only the status permission, and no permission.

Expected:

- Registration does not disappear because of a stale legacy boolean.
- Module publication synchronizes compatibility fields instead of allowing two conflicting authorities.
- Disabled behavior is explicit and re-enabling does not require a different JAR.

### Record lifecycle

Use:

```text
/sef tags status
/sef tags list
/sef tags view <tag>
/sef tags create <resource_key>
/sef tags duplicate <tag> <new_resource_key>
/sef tags edit <tag>
/sef tags validate <tag_or_draft>
/sef tags publish <tag>
/sef tags hide <tag>
/sef tags archive <tag>
/sef tags restore <tag>
/sef tags delete <tag>
/sef tags revision list <tag>
/sef tags revision view <tag> <revision>
/sef tags revision restore <tag> <revision>
```

Test:

1. Valid and invalid resource keys.
2. Draft, publication, hidden, archive, restore, and deletion states.
3. Two concurrent edit leases.
4. Stale revision and stale lease.
5. Permission loss while editing.
6. Immutable revision history and restore.
7. Restart after each state.

Expected:

- UUID and resource key remain stable.
- Publication never mutates an existing immutable artwork revision.
- Lease override requires its exact authority and audit.

### Assignment and visibility

Use:

```text
/sef tags assign player playera <tag> <slot>
/sef tags assign group <group> <tag> <slot>
/sef tags assign team <team> <tag> <slot>
/sef tags assign default <tag> <slot>
/sef tags unassign <assignment_id>
/sef tags assignments player playera
/sef tags assignments tag <tag>
/sef tags assignments group <group>
/sef tags report <tag> <reason>
/sef tags moderation queue
/sef tags moderation suspend <tag> <reason>
/sef tags moderation clear <tag>
```

Test:

1. Priority, duration, expiry, slot cap, assignment cap, and duplicate assignment.
2. Online, offline, higher-rank, exempt, vanished, team, group, and default targets.
3. LuckPerms absent, healthy, refreshed, and failed.
4. Viewer policies for draft, hidden, archived, creator, assignment, audit, storage, and hash fields.
5. Permission revocation after artwork delivery.

Expected:

- Viewer-specific manifests contain only authorized metadata and hashes.
- Revocation removes active projection and sends supported cache invalidation.
- Group provider failure invalidates group targeting without preventing startup.

### Secure import and export

Place test files only in the fixed owned import inbox. Use:

```text
/sef tags import scan
/sef tags import inspect <candidate_id>
/sef tags import approve <candidate_id> <resource_key>
/sef tags import reject <candidate_id>
/sef tags import url <https_url> <resource_key>
/sef tags export <tag> png
/sef tags export <tag> project
/sef tags export <tag> manifest
```

Fixtures:

- Valid PNG.
- Valid JPEG.
- Metadata-bearing image.
- Invalid signature.
- Truncated image.
- Oversized encoded file.
- Oversized dimensions.
- Excess pixel count.
- Decompression bomb.
- Symlink.
- Path traversal name.
- File that changes during settle interval.
- Duplicate canonical content.
- Unsupported type.
- HTTP URL.
- HTTPS URL to allowed and denied hosts.
- Redirect chain and private-network target.

Expected:

- Only stable regular safe-name files become opaque candidates.
- No player receives a filesystem path.
- Canonicalization strips metadata and creates deterministic PNG bytes and SHA-256 identity.
- Duplicate bytes reuse content safely.
- URL import is separately permissioned and protected from SSRF.

### Transfer, cache, rendering, and editor

Use:

```text
/sef tags transfer status
/sef tags cache status
/sef tags cache invalidate <tag_or_hash>
/sef tags integrity check all
/sef tags audit
```

Actions:

1. Download one tag, disconnect mid-transfer, reconnect, and request again.
2. Send duplicate, replayed, skipped, reordered, late, and hash-mismatched chunks through the protocol fixture.
3. Fill per-player sessions, byte budget, decoded memory, GPU memory, and disk cache limits.
4. Switch between two servers that publish the same resource key with different hashes.
5. Render tags in chat, nameplate, tab, HUD, tooltip, gallery, and detail screens.
6. Combine nickname, prefix, suffix, team, vanish, and disguise.
7. Test chat wrapping, baseline, maximum rendered width, missing object, corrupt cache, and unsupported artwork.
8. Test editor pencil, eraser, fill, line, rectangle, selection, move, palette, text, layers, frames, undo, redo, autosave, crash recovery, import, export, and preview.
9. Repeat scales, resize, keyboard, mouse, narration, high contrast, reduced motion, and maximum canvas.

Expected:

- Cache keys include server identity and content hash.
- Hash verification happens before atomic cache publication.
- Render thread owns texture lifecycle.
- Local-only projects never transmit or impersonate server-published tags.
- Vanilla clients receive configured alternative text or no tag and remain connected.

### Integrity, backup, restore, and garbage collection

Use:

```text
/sef tags integrity check all
/sef tags integrity repair <repair_id>
/sef tags backup preview
/sef tags backup create
/sef tags gc preview
/sef tags gc run
/sef tags doctor
/sef tags reload
```

Test missing object, corrupt object, publication interruption, missing journal, orphan referenced object, orphan unreferenced object, unknown file, backup failure, restore staging failure, and retention.

Expected:

- Preview and execution are revision bound.
- Garbage collection deletes only proven unreferenced owned objects.
- Unknown files are preserved.
- Restore is staged and verified before publication.

## Phase 12B. Disguise

Enable disguise only in staging. Test:

```text
/disguise mob minecraft:blaze
/disguise mob minecraft:bat
/disguise player playerb
/disguise preset <preset_id>
/disguise clear
/undisguise
/disguise status
/disguise status playera
/disguise list
/disguise preview minecraft:blaze
/disguise set playera minecraft:blaze
/disguise clear playera
/disguise options
/disguise inspect playera
/disguise conflicts
/disguise ability primary
/disguise ability secondary
/disguise ability utility
/dability primary
```

Before projection testing:

1. Set `[module].enabled = true` in `config/sef/modules/disguise.toml`.
2. Run `/sef config reload disguise`, `/sef config inspect disguise`, `/disguise`, and `/disguise status`.
3. Grant `sef.*` through LuckPerms, refresh the user, and reconnect.
4. Confirm the root and authorized subcommands appear in tab completion.
5. At `/disguise `, confirm only authorized subcommands appear. Entity ids must not be mixed into the root suggestions.
6. At `/disguise mob `, `/disguise preview `, `/disguise set playera `, and `/disguise presets create test `, confirm canonical namespaced entity ids are suggested.
7. Execute both `minecraft:bat` and an installed mod’s `namespace:path` entity id. Confirm the colon parses as part of one resource location and does not produce trailing-data syntax.
8. Remove only `sef.commands.disguise.mob` and confirm the mob route disappears while independently authorized status and clear routes remain.
9. Set the module false, reload, and confirm behavior reports the module state rather than an unknown command caused by skipped registration.
10. Re-enable it and confirm a command-tree refresh restores the routes without replacing the JAR.

Expected:

- The module file is authoritative. A stale legacy bootstrap boolean cannot keep disguise disabled after a successful module publication.
- `sef.*` is honored through the active LuckPerms provider.
- Command registration remains stable across enabled and disabled states; execution still fails closed while disabled.

### State and authority

1. Apply every registered mob adapter.
2. Test player profile and preset modes.
3. Test self and other-player authority, hierarchy, exemption, protected profile, persistence, and expiry.
4. Repeat on death, logout, reconnect, restart, dimension change, tracking-range exit and entry, permission loss, and feature disablement.
5. Test nickname, Fancy Tags, team, tab, chat, vanish, equipment, and label precedence.
6. Verify real UUID, permissions, signed chat, homes, balance, statistics, bans, and audit owner never change.

### Enhanced and vanilla projection

1. Observe one subject from enhanced, fallback, and incompatible clients together.
2. Test proxy entity-id allocation and collision.
3. Attack and interact with the proxy at valid range.
4. Attempt stale id, wrong observer, wrong dimension, excessive distance, blocked line of sight, vanished subject, protected team, stale revision, and disconnected subject.
5. Move in and out of tracking range.
6. Disconnect observer and subject.
7. Test player-profile cache failure and untrusted texture URL.
8. Apply bat and Enderman disguises. Walk, sprint, crouch, swim, jump, rotate slowly, snap 180 degrees, attack, and stand still while observing at low and high frame rates.
9. Confirm the proxy follows the real current and previous position, pitch, body rotation, head rotation, pose, swing, and animation state without rapid left-right oscillation. Confirm walking and sprinting animate limbs, the bat flight animation runs, idle animations advance, and returning to rest does not freeze the model in a movement frame.
10. Repeat after reconnect, dimension change, tracking-range exit, and disguise revision replacement.

Expected:

- Proxy maps only to the current real subject and revision.
- Invalid interaction never forwards.
- All proxy and temporary profile-list state is removed during cleanup.

### Traits and abilities

1. Test each allowed trait alone and in combination.
2. Confirm default real-player hitbox and physics remain.
3. Test Blaze primary, secondary, and utility.
4. Test permission, cooldown, rate, cost, aim, range, PvP, grief, fire, explosion, world, combat, and protection policies.
5. Activate with enhanced keybind and fallback command.
6. Change disguise revision between input and handling.
7. Force projectile or effect creation failure.
8. Undisguise while an effect is active.
9. Apply bat and Enderman, then trigger `primary`. Confirm the response says the disguise has no primary ability. It must not say the global ability system is disabled while the module setting is enabled.
10. Disable `runtime.disguise_abilities_enabled`, reapply a disguise that has a registered ability, and confirm the global-disabled response appears only in that case.

Expected:

- The server calculates aim and owns damage and effects.
- Cooldown commits only after successful activation.
- Trait and ability cleanup is atomic.
- No ability is enhanced-client exclusive.

## Phase 13. Server control and remaining systems

The current server-control catalog contains 75 feature families. Test every family. A schema, record, generic editor, or command route is not proof of runtime behavior.

The current source deliberately classifies these sixteen families as unavailable: `admin_journal`, `afk_zones`, `approvals`, `capability_leases`, `chat_channels`, `display_ownership`, `display_profiles`, `player_warp_review`, `portal_policy`, `resource_governor`, `resource_worlds`, `rollouts`, `server_presentation`, `spawn_ecology`, `staff_duty`, and `waypoints`.

For each unavailable family, complete field validation and record tests, then confirm preview reports unavailable, execution returns provider error, generic `state ... active` and `state ... resolved` are denied, and the record remains unchanged. Mark the feature incomplete. Do not attempt the feature-specific success scenario until a real runtime implementation replaces that classification.

### Common control workflow

For each feature id in the tables below, run:

```text
/sef control <feature>
/sef control <feature> list
/sef control <feature> fields
/sef control <feature> create "test <feature>" phase13-test
/sef control view <record_id>
/sef control history <record_id>
/sef control <feature> configure <record_id> <revision> <field> <value>
/sef control <feature> unset <record_id> <revision> <optional_field>
/sef control <feature> preview <record_id> <revision>
/sef control <feature> execute <record_id> <revision>
/sef control <feature> execute <record_id> <revision> confirm <token>
/sef control <feature> state <record_id> <state> <revision> phase13-test
/sef control <feature> update <record_id> <revision> "updated <feature>" phase13-updated
```

Use tab completion for valid fields, values, states, and confirmation tokens. Do not invent field values. Record every returned revision and use it on the next operation.

Apply these tests to each family:

1. View, create, create-for-other, and manage permissions independently.
2. Player, console, RCON, and denied command-block sources.
3. Valid minimum, valid maximum, invalid enum, invalid UUID, invalid duration, invalid resource id, excessive text, and missing required field.
4. Stale revision after another actor updates.
5. Preview, changed record after preview, expired confirmation, wrong actor token, replayed token, and successful confirmation.
6. Every supported state transition and one unsupported transition.
7. Restart with open, active, paused, approved, denied, resolved, cancelled, archived, and expired records where supported.
8. Provider failure, persistence failure, permission refresh, feature disablement, and server shutdown.
9. Enhanced GUI and command fallback parity.
10. Private or sensitive data visibility.
11. Audit correlation and history.
12. Bound and rate-limit behavior.

Expected:

- Record revisions increase monotonically.
- Stale updates never overwrite current state.
- Dangerous execution requires its current confirmation.
- Runtime failure does not report a completed action.
- A live policy, scheduled job, or integration cannot become active or resolved through the generic state command.
- `/sef doctor` reports exactly 59 executable and 16 unavailable server controls until the listed implementations exist.
- GUI and command use the same record and revision.

### Phase 13A. Operational safety foundation

| Feature id | Required scenario |
| --- | --- |
| `maintenance` | Schedule, activate, pause, cancel, restore access, reconnect players, and verify fallback announcements. |
| `policy_lab` | Evaluate a harmless policy change without publishing it, compare decisions, then reject unsafe input. |
| `config_drift` | Change a copied config file, detect drift, classify it, restore known-good state, and verify no per-tick scan. |
| `guardrails` | Trigger one soft and one hard guardrail, verify block, override permission, expiry, and rollback. |
| `change_windows` | Open, close, expire, and supersede a window. Attempt a protected action outside the window. |
| `permission_impact` | Preview grant, denial, wildcard, and revocation impact without changing provider state. |
| `dependency_graph` | Inspect dependencies, disable a prerequisite, detect dependents, and reject a cycle. |
| `player_impact` | Simulate a change for authorized, denied, hidden, exempt, and offline players without mutation. |
| `resource_governor` | Cross warning and hard-pressure thresholds, admit bounded work, reject excess work, then recover. |
| `operational_snapshots` | Capture, compare, expire, and inspect snapshots without exposing secrets. |

### Phase 13B. Community and staff workflow

| Feature id | Required scenario |
| --- | --- |
| `reports` | Submit, assign, update, resolve, reopen, rate-limit, and hide sensitive reporter data. |
| `tickets` | Submit, queue, assign, hand off, reply, resolve, and recover an interrupted ticket. |
| `staff_notes` | Create private notes, enforce audience, revise, retain, and audit access. |
| `chat_channels` | Join, leave, route, mute, permission-revoke, and restore a channel without signed-chat confusion. |
| `mentions` | Enable, disable, rate-limit, ignore, vanish-filter, and test offline notification. |
| `friends` | Request, accept, reject, remove, race two responses, and enforce privacy. |
| `interaction_blocks` | Block, unblock, test message, teleport, trade, parcel, and invite enforcement. |
| `session_quarantine` | Quarantine, restrict, inspect, release, expire, reconnect, and use emergency recovery. |
| `player_warp_review` | Report, hide, review, approve, suspend, restore, and preserve owner data. |

### Phase 13C. Onboarding and rewards

| Feature id | Required scenario |
| --- | --- |
| `rules` | Publish a revision, accept it once, change revision, require reacceptance, and preserve history. |
| `onboarding` | Complete, dismiss where allowed, resume after reconnect, and avoid duplicate completion. |
| `playtime_rewards` | Reach threshold, claim once, restart, and reject duplicate or clock-shift claim. |
| `daily_rewards` | Claim by period id, reconnect, restart, cross day boundary, and reject replay. |
| `weekly_rewards` | Claim by week id, cross week boundary, and reject duplicate or timezone manipulation. |
| `sleep_vote` | Vote, withdraw, disconnect, change dimension, reach threshold, and reset after result. |
| `death_compass` | Record death, locate, clear, cross dimension, expire, and hide protected location details. |
| `afk_zones` | Enter, leave, reconnect, move, receive state, and prevent reward or state duplication. |

### Phase 13D. Recovery and world operations

| Feature id | Required scenario |
| --- | --- |
| `graves` | Die with mixed inventory, create one grave, locate, unlock, claim, partially fail capacity, expire, and recover after restart. |
| `inventory_recovery` | Capture snapshots, browse authorized history, restore to staged inventory, reject stale target state, and audit every slot mutation without leaking NBT. |
| `restart_coordinator` | Schedule, warn, pause, resume, cancel, stop, recover interrupted state, and preserve fallback messaging. |
| `resource_worlds` | Create lifecycle record, schedule reset, preview impact, block unsafe execution, and recover after failure. |
| `chunk_pregen` | Start bounded work, pause, resume, cancel, enforce world border and budget, and avoid generating unauthorized chunks. |
| `cleanup` | Preview entities and items, exclude protected objects, confirm exact revision, execute bounded batches, and verify rollback state. |
| `performance` | Capture tick metrics, display private diagnostics, compare thresholds, and avoid adding profiler overhead in idle state. |

Grave and inventory recovery details:

1. Use armor, offhand, hotbar, containers, damaged items, custom components, Curse of Binding, and optional inventory slots.
2. Die during another inventory write.
3. Fill claimant inventory before claim.
4. Attempt two simultaneous claims.
5. Break or unload the grave location.
6. Corrupt the recovery repository on a copy.

Expected:

- Each item has one authoritative source and one terminal destination.
- No failed claim duplicates or silently destroys items.

### Phase 13E. Governance and navigation

| Feature id | Required scenario |
| --- | --- |
| `admin_journal` | Record reversible action, inspect before and after state, reverse once, reject stale reversal, and preserve immutable history. |
| `command_anomaly` | Establish baseline, trigger bounded anomaly, classify it, suppress a false positive, and protect private commands. |
| `incidents` | Open, assign, add timeline entries, link evidence, resolve, reopen, and enforce audience. |
| `rollouts` | Canary, expand, pause, roll back, detect unhealthy state, and preserve target cohort. |
| `server_calendar` | Create, subscribe, notify, reschedule, cancel, handle timezone, and prevent duplicate reminder. |
| `waypoints` | Set, list, go, remove, enforce dimension and safety, and hide protected locations. |
| `portal_policy` | Admit and deny portal travel, test loops, cooldown, world policy, and missing destination. |
| `alias_diagnostics` | Detect collision, recursion, disabled target, policy weakness, stale publication, and safe remediation. |

### Phase 13F. Staff governance and due process

| Feature id | Required scenario |
| --- | --- |
| `staff_duty` | Start and end shift, claim queue work, hand off, interrupt, reconnect, recover, and inspect private HUD. |
| `approvals` | Request, preview, approve by a different actor, revoke, expire, supersede, execute once, and inspect history. |
| `appeals` | Submit, test eligibility, reviewer conflict, evidence disclosure, decision, adjustment, and external-provider failure. |
| `discipline` | Create case, apply reviewed policy, add candidate response, explain points, decay, revise, and close. |
| `capability_leases` | Grant, overlap, renew, suspend, resume, revoke, expire, refresh provider, and reconcile. |
| `admin_lock` | Lock, challenge, unlock, open and close privileged session, invalidate, test protected action, and use console break-glass recovery. |

Use the specialized commands:

```text
/approval request
/approval approve
/approval revoke
/accessgrant create
/accessgrant renew
/accessgrant suspend
/accessgrant resume
/accessgrant revoke
/accessgrant reconcile
/adminlock lock
/adminlock challenge
/adminlock unlock
/adminlock session open
/adminlock session close
/adminlock invalidate
/adminlock breakglass open
/adminlock breakglass close
```

Expected:

- Requester cannot satisfy required separation alone.
- Approval binds immutable preview and revision.
- Lease and privileged-session expiry revoke authority promptly.
- Break glass is console-controlled, narrow, time-bound, and audited.

### Phase 13G. Chat safety, admission, and access

| Feature id | Required scenario |
| --- | --- |
| `automod` | Validate rules, detect duplicate, burst, URL, mention, and caps cases, test exemption, review queue, reload, and safe enforcement. |
| `chat_control` | Apply slow, read-only, staff-only, and lockdown modes, schedule restoration, overlap states, and reconnect. |
| `admission` | Reach rate and capacity limits, test reserved capacity, retry, reconnect, and restart. |
| `queue` | Enter, leave, expire, preserve order, use restricted lobby or deny-and-retry proof, and reject replay token. |
| `access_applications` | Submit, review, approve, deny, expire grant, and handle whitelist-provider failure. |
| `invites` | Create, redeem once, alter digest, replay, expire, revoke, and test guest restrictions. |

Expected:

- Automod cannot execute arbitrary actions.
- Admission and invite tokens are bounded, expiring, and replay-resistant.
- Queue limitations are described truthfully for the active platform.

#### Native capacity queue and bypass

Use at least three clients. Test once with `server.properties` `max-players` above the SEF admission limit, then again at the exact vanilla capacity. SEF intentionally clears Minecraft’s exact full-server denial only for profiles with the explicit admission or queue exemption.

Create and activate an admission policy:

```text
/sef control admission create "staging admission" phase13g
/sef control admission configure <admission_id> <revision> maximum_players 2
/sef control admission configure <admission_id> <revision> reserved_slots 0
/sef control admission configure <admission_id> <revision> joins_per_minute 100
/sef control admission configure <admission_id> <revision> denial_message The server is at its SEF admission limit.
/sef control admission preview <admission_id> <revision>
/sef control admission execute <admission_id> <revision>
/sef control admission state <admission_id> active <revision> phase13g
```

Create and activate the native waiting policy:

```text
/sef control queue create "staging queue" phase13g
/sef control queue configure <queue_id> <revision> mode native_wait
/sef control queue configure <queue_id> <revision> retry_seconds 5
/sef control queue configure <queue_id> <revision> maximum_entries 2
/sef control queue configure <queue_id> <revision> maximum_wait_seconds 60
/sef control queue configure <queue_id> <revision> status_message The server is full. Your login is waiting.
/sef control queue preview <queue_id> <revision>
/sef control queue execute <queue_id> <revision>
/sef control queue state <queue_id> active <revision> phase13g
/queue
```

Use the revision printed after every command. If the feature requires a confirmation token, run the exact confirmation command returned by the server before changing state.

Actions:

1. Join `playera` and `playerb`. Start `playerc`; its connection must remain in negotiation and `/queue` must report one waiting entry.
2. Disconnect `playera`. Verify the first waiting connection is released, completes login once, and is removed from the queue.
3. Queue two clients in order, free one slot, and confirm FIFO release.
4. Attempt a third queued connection. It must be disconnected with the bounded queue-full response.
5. Queue one profile twice. The newer login must replace the older one, and only one entry may remain.
6. Close a queued client before release. Verify cleanup within the one-second queue tick and no reserved slot leak.
7. Wait past `maximum_wait_seconds`. Verify disconnect with an expiry message and no later login.
8. Restart or deactivate the queue policy while clients wait. Verify every gate is completed, every connection receives a truthful stop response, and no entry survives as a ghost reservation.
9. Grant `sef.commands.control.admission.exempt` to `admina`. Fill the SEF limit with ordinary players, then join `admina`. The exempt player must bypass the SEF admission cap.
10. Remove the admission exemption and grant `sef.commands.control.queue.exempt`; repeat the SEF-cap test.
11. Change `reserved_slots` to `1`. With `maximum_players` still `2`, verify one ordinary player fills ordinary capacity, the next ordinary player queues, and an exempt administrator can use the reserved headroom.
12. Set `reserved_slots` equal to and above `maximum_players`. Verify ordinary admission capacity becomes zero without arithmetic underflow while exempt access remains available.
13. Fill the actual vanilla `max-players` cap and attempt the exempt login. The exempt profile must pass the exact full-server denial. Remove both exemptions and repeat. The ordinary profile must receive the normal full-server result.
14. Set mode to `deny_retry`, then `restricted_lobby`, then `proxy_adapter`. Verify native wait is used only for `native_wait`; unavailable proxy mode reports provider failure and does not claim a working proxy queue.
15. Set `maximum_players` to `0` on a disposable copy, test the documented unlimited behavior, then restore a bounded positive limit.
16. Race a disconnect, two queued releases, an exemption refresh, and a policy revision in the same second.

Expected:

- The queue is bounded by entry count and wait time and holds only negotiation futures, never joined player state.
- Release is FIFO and reservations prevent more than the configured number of simultaneous admissions.
- `reserved_slots` is subtracted from ordinary capacity and never from exempt administrative headroom.
- Disconnect, timeout, policy change, replacement login, successful login, and shutdown remove their entries.
- Exemption nodes bypass the matching SEF admission or queue policy and Minecraft’s exact full-server denial. They do not bypass bans, whitelist denial, incompatible protocol, authentication, maintenance, or unrelated login failures.

### Phase 13H. Content and world policy

| Feature id | Required scenario |
| --- | --- |
| `resource_packs` | Stage owned pack, verify hash, assign, prompt, accept, decline, fail download, roll out, and roll back. |
| `server_presentation` | Publish MOTD and icon profile, schedule it, filter vanished sample, test ping ownership, and restore previous profile. |
| `world_policy` | Preview gamerule and field ownership changes, apply, detect drift, schedule, and roll back. |
| `world_border` | Preview, transition, pause, resume, cancel, evacuate safely, and roll back transition. |
| `spawn_ecology` | Apply profile, inspect source coverage and caps, schedule, and report unsupported spawn sources. |

Expected:

- Fixed inboxes reject links and traversal.
- World changes have a reviewed preview and recoverable previous state.
- Unsupported sources are explicit, not silently ignored.

### Phase 13I. Diagnostics, data packs, and verified recovery

| Feature id | Required scenario |
| --- | --- |
| `chunk_tickets` | List and classify tickets, infer associations, release only SEF-owned eligible tickets, and preserve external tickets. |
| `block_activity` | Start bounded sampling, identify hot positions, use reviewed non-destructive intervention, and stop cleanly. |
| `datapacks` | Scan fixed inbox, validate archive and dependencies, stage, publish, reload, fail, select rollback, and recover. |
| `mod_health` | Inventory mods, providers, adapters, conflicts, baseline health, failure, recovery, and privacy-safe export. |
| `backups` | Flush, checkpoint, verify, schedule, retain, stage restore, hand off restart, rehearse, and recover a failed provider. |

Backup test:

1. Mutate several repositories and world state.
2. Request backup.
3. Confirm the save and storage flush barrier.
4. Verify checkpoint contents and provider result.
5. Change state after backup.
6. Stage restore while stopped.
7. Start the restored copy.
8. Verify world and SEF repository consistency.
9. Rehearse rollback without touching the original staging source.

Expected:

- Backup never reports success before verification.
- Restore never overwrites the active source before staging validation.

### Phase 13J. Privacy and evidence

| Feature id | Required scenario |
| --- | --- |
| `privacy` | Show domain summary, change preference and consent, request bounded export, correction, and deletion, then resolve shared and protected records. |
| `evidence` | Create typed references, capture bounded snapshot, transfer custody, seal, disclose redacted revision, hold, expire, approve destruction, and verify immutable history. |

Expected:

- Player access cannot reveal another player's private state.
- Protected retention and shared-record decisions are explicit.
- Evidence content, seal, custody, redaction, disclosure, hold, and destruction are revisioned and auditable.

### Phase 13K. Item logistics and player market

| Feature id | Required scenario |
| --- | --- |
| `parcels` | Send item and currency parcel, deliver offline, block sender, expire, return, freeze, claim with no space, restart, and recover settlement. |
| `lost_found` | Register typed source once, reject duplicate source, claim, preserve missing registry item, expire, freeze, and recover. |
| `trades` | Request, accept, open escrow, revise offers, ready both sides, invalidate readiness, confirm both sides, settle, fail capacity, disconnect, and recover. |
| `auctions` | List, buy now, bid, outbid, reserve currency, watch, expire, claim, return, charge fee, freeze, restart, and recover settlement. |

Special actions:

1. Have both players submit the same item source.
2. Change one inventory after escrow admission.
3. Fill recipient inventory and balance.
4. Kill the process during parcel, trade, and auction settlement.
5. Restart twice.
6. Reconnect from both sides and inspect ownership.
7. Test unified interaction blocks against parcel, trade, auction watch, and invite flows.

Expected:

- One item or currency unit has one source and one owner at every committed state.
- Crash recovery is idempotent.
- No settlement duplicates, loses, or double-charges assets.

### Phase 13L. Community governance and knowledge

| Feature id | Required scenario |
| --- | --- |
| `polls` | Create typed ballot, apply eligibility, vote once, preserve privacy, schedule close, recount, and publish according to result policy. |
| `community_events` | Create, register, fill capacity, waitlist, withdraw, promote, assign team, check in, grant reward once, and record result. |
| `knowledge` | Create article, safe-format, translate fallback, publish, search, bookmark, bind context, import from fixed inbox, revise, and roll back. |

Expected:

- Real polls are never mixed with fake synthetic polls.
- Event rewards are idempotent.
- Knowledge formatting cannot execute commands or unsafe links.

### Phase 13M. Unified display ownership

| Feature id | Required scenario |
| --- | --- |
| `display_profiles` | Define scoreboard, tab, boss bar, action bar, HUD tile, objective, and toast content with typed values and audience. |
| `display_ownership` | Acquire, renew, expire, preempt, and release leases by priority while preserving external objectives. |

Combine:

- Nicknames.
- Fancy Tags.
- Vanish.
- Disguise.
- Maintenance.
- Queue.
- Community event.
- Performance warning.
- Another mod's scoreboard objective.

Expected:

- Priority and leases produce one deterministic owner per surface.
- Privacy filters run before rendering.
- External objectives are preserved according to coexistence policy.
- Packet and refresh budgets remain bounded.

### Phase 13N. Administrative enchanting

Test:

```text
/enchant @s minecraft:sharpness 5
/enchant @s minecraft:knockback 255
/enchant @s minecraft:sharpness 1000
/sef enchant
/superenchantingtable
/set
```

Use tab completion for self shorthand, remove-one, and clear-all forms.

LuckPerms checks:

```text
/lp user playera permission check sef.commands.enchant.unsafe_level
/lp user playera permission check sef.commands.enchant.any_item
/lp user playera permission check sef.commands.enchant.incompatible
/lp user playera permission check sef.commands.enchant.remove
/lp user playera permission check sef.commands.enchant.clear
/lp user playera permission set sef.* true
```

Actions:

1. Run bare `/enchant` on an enhanced client and confirm the typed enchantment workflow opens. Run the complete form and confirm it executes directly.
2. Apply ordinary compatible enchantment.
3. Apply unsafe level without and with `sef.commands.enchant.unsafe_level`.
4. Apply to dirt without and with `sef.commands.enchant.any_item`.
5. Add normally incompatible enchantments without and with `sef.commands.enchant.incompatible`.
6. Test self, other, and bounded multi-target permissions.
7. Test higher-rank, exempt, vanished, unknown, and disconnected targets.
8. Test zero, negative, maximum ceiling, one above ceiling, integer overflow, and nonnumeric level.
9. Preserve an existing higher level.
10. Remove one enchantment without and with `sef.commands.enchant.remove`, including preview and confirmation.
11. Clear all enchantments without and with `sef.commands.enchant.clear`, including its separate preview and confirmation.
12. Change selected slot, stack identity, item count, registry entry, permission, feature, cost, cooldown, and menu revision immediately before mutation.
13. Inspect level 1000 item on enhanced and fallback clients, in inventory, tooltip, drop, pickup, container, save, restart, and reconnect.
14. Create a `/set` collision.
15. Grant only `sef.*`, refresh LuckPerms, and repeat unsafe level, arbitrary item, incompatible, remove, and clear tests. Each exact check must resolve true without a generic operator fallback.
16. Remove `sef.*`, grant only ordinary enchant permission, and verify every unsafe error names the exact missing node.

Expected:

- Registry and numeric safety always apply.
- Vanilla applicability and maximum level are bypassed only with exact unsafe permissions.
- Multi-target admission is atomic where required.
- Extreme levels do not overflow component, codec, packet, tooltip, or renderer.
- `/superenchantingtable` always remains available even when `/set` collides.

### Phase 13O. Permission-derived cooldowns

Use:

```text
/sef cooldown keys
/sef cooldown keys 2
/sef cooldown explain playera sef:workstation.craft
```

For one action in every command family, test nodes shaped like:

```text
sef.cooldown.craft.100
sef.cooldown.craft.0
```

Also use each action's exact generated key from `docs/PERMISSION_REFERENCE.md`.

Matrix:

1. No matching node.
2. Exact zero.
3. Exact positive.
4. Exact maximum.
5. One above maximum.
6. Multiple inherited grants.
7. Direct player grant against inherited grants.
8. Explicit denial.
9. Wildcard grant.
10. Generic OP fallback.
11. Provider absent.
12. Provider outage.
13. Provider unable to discover arbitrary numeric suffixes.
14. Malformed suffix with sign, decimal, exponent, whitespace, overflow, control character, and text.
15. Dedicated bypass permission.

Actions:

1. Run canonical command, shortcut, GUI, panel, alias, bundle, sudo participant, and approved integration path.
2. Confirm the lowest valid inherited duration wins.
3. Confirm a safe direct assignment takes precedence when origin is available.
4. Start a cooldown, then refresh permission.
5. Reconnect, restart, change alias, change shortcut ownership, and use the GUI.
6. Run two concurrent admissions.
7. Inspect legacy `common.toml` cooldown diagnostics.

Expected:

- All routes use one canonical action lease.
- Reconnect and restart do not reset it.
- Provider failure and malformed permissions never create silent zero cooldown.
- Existing leases keep their admitted duration.
- Legacy configuration durations are reported and ignored.

## Phase 13.5. Modular responsive configuration

### Module inventory

Verify all 62 module schema files and the bootstrap `index.toml` exist under `run/config/sef/modules`:

```text
admin_panels
aliases
anvil
audit
back
backups
bans
building_control
bundles
command_spy
commands
community
connection_messages
core
craft
direct_teleport
disguise
displays
economy
economy_signs
enchanting
fake_actions
fancy_tags
freeze
gamemode
gui
homes
hud
index
integrations
inventory
inventory_lock
items
jails
kicks
kits
logger
mail
messages
moderation
mutes
nicknames
performance
permissions
player_utilities
player_warps
privacy
private_messages
random_teleport
reminders
repair
run_and_silent
server_control
social
social_spy
spawn
sudo
super_enchanting
teleport_requests
vanish
warnings
warps
workstations
```

Run:

```text
/sef config status
/sef config modules
/sef config inspect <module>
/sef config explain <module> <setting>
/sef config validate
/sef config diff <module>
/sef config history <module>
/sef config documentation generate
```

Expected:

- Runtime reports exactly the registered files and schemas.
- Generated documentation matches active schemas.
- No module defines operator-selected command cooldown duration.
- Sensitive settings are redacted.

### Transactional editing and reload

Use:

```text
/sef config set <module> <setting> <expected_revision> <value>
/sef config reload
/sef config reload <module>
/sef config rollback <module> <revision>
```

For every module:

1. Edit one valid live setting at its minimum.
2. Edit it at its maximum.
3. Try wrong type, invalid enum, one under minimum, one above maximum, excessive string, unknown setting, unknown module, and stale revision.
4. Test dependency disablement and conflict.
5. Test `fail_closed`, `previous_known_good`, and `read_only` behavior where supported.
6. Modify two files before reload, one valid and one invalid.
7. Confirm no partial publication.
8. Restore both and reload.
9. Test watcher debounce through rapid saves.
10. Confirm no per-tick filesystem polling.

Expected:

- Validation is all-or-nothing.
- Invalid state keeps the previous known-good revision.
- Live changes apply within the documented target.
- Restart-required settings never claim to be live.

### Migration, backup, and rollback

Use:

```text
/sef config migrate dryrun
/sef config migrate apply <expected_revision>
/sef config rollback <module> <revision>
```

Actions:

1. Start from a copied legacy `common.toml`.
2. Dry-run and inspect every mapped, ignored, unsupported, and cooldown field.
3. Change the source after dry-run.
4. Attempt apply with stale revision.
5. Apply valid migration.
6. Confirm timestamped backup and journal.
7. Restart.
8. Roll back through the typed service.
9. Corrupt one module file and restart.

Expected:

- Migration never silently turns cooldown values into permissions.
- Changed sources invalidate the reviewed plan.
- Rollback is revisioned and recoverable.

### GUI policy

Use:

```text
/sef guis status
/sef guis on
/sef guis off
/sef guis auto
/sef guis module <module> inherit
/sef guis module <module> on
/sef guis module <module> off
/sef guis module <module> command_only
/sef guis module <module> gui_preferred
/sef guis action <action_id> <mode>
/sef guis sessions
/sef guis close <player_or_all>
/sef guis reload
/sef guis doctor
/sef guis explain playera <action_id>
/sef gui on
/sef gui off
/sef gui auto
/sef gui reset
/sef gui status
/sef client status
/sef client preference blur off
/sef client preference blur on
```

Actions:

1. Test global `off`, `on`, and `auto`.
2. Apply every module and action override.
3. Apply every player preference.
4. Test incompatible and fallback clients.
5. Turn global mode off with a screen, preview, lease, and privileged draft open.
6. Disable a module with its screen open.
7. Revoke screen and control permissions separately.
8. Test world change and disconnect.
9. Start with no GUI preference record and open the dashboard, homes, player picker, item picker, suggestion picker, typed workflow, control editor, Fancy Tags studio, and InvSee. Confirm the world remains sharp behind every SEF screen. Confirm panels and buttons are not darkened by a second superclass background pass.
10. Run `/sef client preference blur on`. Confirm it is rejected and `/sef client status` still reports `background: sharp`.
11. Run `/sef client preference blur off`, reopen every screen, reconnect, and restart. Confirm the world remains sharp.
12. Inspect `<world>/serverconfig/sef/gui-preferences.json`. Test copied legacy records with `backgroundBlur` missing and with it set to true. Restart each copy and confirm both normalize to false and are flushed safely.
13. Confirm the server’s effective feature set never includes the reserved background-blur bit even if an older compatible client advertises it.
14. Open every SEF screen over moving water, entities, weather, and particles. Confirm the scene remains live and sharp with no blur pass, dark full-screen dirt background, or accidental pause.

Expected:

- Effective mode resolves from hard safety, global, module, action, client capability, permission, preference, and state in that order.
- Turning GUI off closes and invalidates server-owned enhanced state without removing command access.
- Administrators cannot force a screen onto a client that did not negotiate it.
- SEF backgrounds are always transparent and sharp. Legacy blur state cannot re-enable a blur pass or grant authority.

### Module enabled-state consistency

Test `fancy_tags`, `disguise`, and five ordinary command modules:

```text
/sef config inspect fancy_tags
/sef config inspect disguise
/sef config validate fancy_tags
/sef config validate disguise
/sef tags doctor
/disguise status
/sef doctor
```

Actions:

1. Set `[module].enabled = true`, reload the module, and record the published revision.
2. Confirm direct commands, catalog actions, GUI rows, and `/sef doctor` agree that the module is enabled.
3. Set any retained legacy bootstrap toggle to the opposite value in a disposable migrated copy. Publish the module and repeat the checks.
4. Set `[module].enabled = false`, publish, and repeat.
5. Make the TOML invalid. Confirm the previous known-good enabled state remains active and diagnostics name the parse failure.
6. Change the file back, reload, and confirm command tree and active enhanced sessions refresh.

Expected:

- One published module snapshot is authoritative after dependency validation.
- Compatibility booleans mirror the publication and cannot independently disable Fancy Tags or disguise.
- An invalid candidate never partially changes feature gates, command visibility, or GUI projection.

## Phase 14. Release hardening

### Documentation

Verify:

1. `README.md` describes current purpose, status, versions, installation, development, configuration, compatibility, limits, and support.
2. `DOCUMENTATION.md` describes architecture, initialization, services, persistence, networking, configuration, commands, permissions, integrations, testing, recovery, and release.
3. Generated command, permission, and configuration references match runtime.
4. Installation covers server-only, GUI-off, enhanced, and fallback clients.
5. Migration covers backup, dry-run, apply, verification, rollback, and unsupported source data.
6. Compatibility records optional integrations present and absent.
7. Performance report contains current generated budgets and measurements.
8. Security review covers current trust boundaries.
9. Release notes describe actual implemented behavior and limitations.

Run:

```bash
env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 PATH=/usr/lib/jvm/java-21-openjdk-amd64/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin ./gradlew generateProjectReferences generatePerformanceReport
git diff --exit-code -- docs/COMMAND_REFERENCE.md docs/PERMISSION_REFERENCE.md docs/CONFIGURATION_REFERENCE.md docs/PERFORMANCE_REPORT.md
```

Expected:

- No generated-reference drift.
- Planned behavior is not described as implemented unless it exists.

### Full clean release matrix

Repeat:

1. Clean build.
2. All unit tests.
3. All GameTests.
4. GUI-off dedicated server.
5. GUI-on dedicated server.
6. Enhanced client join.
7. Fallback client join.
8. Incompatible protocol fallback.
9. Permission provider absent.
10. Compatible permission provider present.
11. Optional integrations individually.
12. Supported integration stack.
13. Upgrade from every supported fixture.
14. Rollback on a copied stopped server.
15. Normal shutdown.
16. Forced termination and recovery.
17. Performance profile.

The dedicated tick profile target recorded for the accepted baseline is approximately 20 TPS under the reference smoke load. Compare measured operation budgets with `docs/PERFORMANCE_REPORT.md`, not only average TPS.

### JAR inspection

Run:

```bash
unzip -t build/libs/sef-2.0.0.jar
jar tf build/libs/sef-2.0.0.jar
jdeps --multi-release 21 build/libs/sef-2.0.0.jar
sha256sum build/libs/sef-2.0.0.jar
```

Inspect for:

- `META-INF/neoforge.mods.toml`.
- Mixin configuration and required mixin classes.
- Main implementation classes.
- No `run/` directory.
- No build cache.
- No IDE files.
- No crash reports.
- No logs.
- No `.env`.
- No credentials, tokens, keystores, or private keys.
- No test world.
- No accidental optional-integration JAR.
- No client-only class reference from common dedicated-server initialization.

Also run:

```bash
git status --short
git diff --check
git ls-files AGENTS.md
```

Expected:

- Only intended source and documentation changes exist.
- `AGENTS.md` is ignored and untracked.
- No evidence, cache, secret, run directory, or unrelated change is staged.

### Upgrade and rollback

1. Copy a supported old server and all managed data.
2. Start the new JAR.
3. Verify backup and migration journal before mutation.
4. Run `/sef doctor`, `/sef storage status`, `/sef config status`, and domain diagnostics.
5. Exercise representative state from every phase.
6. Stop normally.
7. Restore the pre-upgrade backup to a different staging directory.
8. Start the old compatible JAR only against the restored copy.
9. Verify rollback instructions and retained data.

Expected:

- Upgrade is recoverable.
- Rollback never points an older JAR at already-migrated active data unless the migration guide explicitly supports it.

## Final acceptance checklist

Do not approve the release until every item is `pass`:

- [ ] Automated build and all tests pass.
- [ ] Dedicated server starts and stops cleanly.
- [ ] GUI-off server remains command complete.
- [ ] Enhanced client negotiates and remains connected.
- [ ] No-SEF fallback client remains connected and command complete.
- [ ] Incompatible protocol falls back without a kick.
- [ ] All 694 command actions completed the universal command matrix.
- [ ] All 11,937 capabilities are generated and independently enforceable where applicable.
- [ ] All 315 shortcuts preserve canonical policy.
- [ ] All 27 repositories pass clean, migration, corruption, crash, and shutdown checks.
- [ ] All 62 module schemas pass transactional validation and rollback.
- [ ] All 75 server-control feature families pass the common workflow and their required scenario.
- [ ] All Fancy Tags import, transfer, cache, render, editor, backup, and recovery tests pass.
- [ ] All disguise projection, proxy, trait, ability, and cleanup tests pass.
- [ ] Administrative enchant levels and arbitrary items remain bounded and permission-separated.
- [ ] Permission-derived cooldowns persist and cannot be bypassed by route or reconnect.
- [ ] Privacy and redaction review finds no sensitive disclosure.
- [ ] Performance budgets pass.
- [ ] Upgrade and rollback pass on copies.
- [ ] Final JAR inspection passes.
- [ ] Documentation and generated references have no drift.
- [ ] Every failure has a fixed commit and a passing retest.

## Bug report template

Use one report per distinct failure:

```text
title:
phase:
test section:
catalog action id:
command or gui path:
expected result:
actual result:
reproduction rate:
first bad step:
server commit:
jar sha-256:
minecraft:
neoforge:
java:
operating system:
server gui mode:
client type and protocol:
permission provider and version:
exact granted nodes:
exact denied nodes:
actor uuid:
target uuid:
configuration revision:
record revision:
sanitized server log:
sanitized client log:
screenshots or recording:
repository state before:
repository state after:
restart result:
security or data-loss impact:
workaround:
retest commit:
retest result:
```

Before attaching logs, remove test addresses, private message bodies, tokens, filesystem usernames, and unrelated player data. Do not remove the command action id, reason code, correlation id, revision, or sanitized stack trace needed to reproduce the defect.
