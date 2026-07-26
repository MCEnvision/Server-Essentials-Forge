# SEF 2 Phase 2 and Phase 3 manual acceptance matrix

This matrix verifies behavior that pure JUnit tests cannot prove without a real NeoForge dedicated server, permission provider, player connection, restart, and filesystem.

Do not run destructive recovery cases against a production world. Use a staging copy and keep the original files.

## 1. Test environment

Record:

1. SEF commit and JAR SHA 256.
2. Minecraft version.
3. NeoForge version.
4. Java version.
5. Operating system and filesystem.
6. LuckPerms version, when used.
7. Other installed command owning mods.
8. `config/sef/common.toml`.
9. Test player UUIDs and assigned groups.

Use at least these actors:

1. A player with only default permissions.
2. A staff player with workstation and catalog access.
3. An administrator with conflict and doctor access.
4. A target player with no administrative permissions.
5. The dedicated server console.

Keep these artifacts:

1. `logs/latest.log`.
2. `<world>/serverconfig/sef/permission-manifest.json`.
3. `<world>/serverconfig/sef/location-history.json`, when created.
4. `<world>/serverconfig/sef/cooldowns.json`, when created.
5. `<world>/playerdata/sef.playerdata.json`.
6. Relevant `.backups` and `.corrupt` entries.
7. Screenshots of permission filtered suggestions and diagnostic output.

## 2. Clean startup and server only compatibility

1. Start a dedicated server with SEF and no optional integrations.
2. Confirm the server reaches the ready state without client class loading errors.
3. Join with a vanilla compatible client that does not have SEF.
4. Confirm the player can remain connected and use authorized command mode features.
5. Stop the server with `stop`.

Pass conditions:

1. No `net.minecraft.client` class loads on the dedicated server.
2. No optional integration is required.
3. The player without SEF can join.
4. Shutdown completes without a repository, configuration lifecycle, packet, or off thread exception.

## 3. Catalog and diagnostic permissions

Grant only `sef.commands.sef.allowed` and `sef.commands.sef.commands` to the default test player.

1. Type `/sef ` and inspect suggestions.
2. Run `/sef commands`.
3. Run `/sef conflicts`.
4. Run `/sef doctor`.
5. Repeat as the administrator with `sef.commands.sef.conflicts` and `sef.commands.sef.doctor`.

Pass conditions:

1. The default player sees `/sef commands`.
2. Catalog entries requiring permissions the player lacks are absent.
3. The default player cannot discover or execute `conflicts` or `doctor`.
4. The administrator can use both diagnostic commands.
5. `/sef doctor` reports catalog, capability, policy, quota, profile, repository, import, provider, and recovery status.
6. Diagnostic use produces metadata only audit output.

## 4. Canonical workstation routes and aliases

Configure a nonzero cooldown for each enabled workstation. Grant only the matching workstation permission for each case.

Test these route sets:

1. `/sef workstation craft`, `/craft`, and `/c`.
2. `/sef workstation anvil`, `/anvil`, and `/av`.
3. `/sef workstation enchantingtable`, `/enchantingtable`, and `/et`.
4. `/sef workstation superenchantingtable`, `/superenchantingtable`, and `/set`.
5. `/sef workstation repair` and `/repair`.

For each set:

1. Execute one route successfully.
2. Immediately execute every other route in the same set.
3. Remove the canonical permission while the player remains online.
4. Attempt every route again.
5. Restore permission and wait for the cooldown to expire.
6. Execute the canonical route.

Pass conditions:

1. All forms reach the same action.
2. A cooldown acquired through one form blocks the other forms.
3. Runtime permission removal blocks every form without reconnecting.
4. No shortcut weakens feature, permission, source, cooldown, or audit policy.
5. Repair precondition failures do not consume a cooldown.

## 5. Shortcut configuration and conflicts

1. Disable one workstation alias in `config/sef/common.toml`.
2. Restart the server.
3. Confirm the long form and canonical route remain available while the disabled alias is absent.
4. Install a staging mod that owns one convenience root.
5. Restart and run `/sef conflicts`.
6. Remove the conflict mod and restart.

Pass conditions:

1. Structural root changes occur only after restart.
2. A disabled alias does not disable its canonical action.
3. Conflict output identifies the root, action id, and effective status.
4. Canonical routes remain available when a convenience root cannot be owned safely.

## 6. LuckPerms optional quota integration

First run without LuckPerms:

1. Start the server.
2. Run `/sef doctor`.
3. Confirm startup and diagnostics show no required provider failure.

Then install LuckPerms:

1. Start with no SEF metadata.
2. Confirm finite defaults remain available to the kernel.
3. Set one supported metadata key to a valid nonnegative integer.
4. Reload LuckPerms data and reconnect the player.
5. Set the same key to a negative number, malformed text, and a number above its hard ceiling in separate runs.
6. Remove LuckPerms and start the same world again.

Supported keys are:

1. `sef.limit.homes.total`.
2. `sef.limit.player_warps.total`.
3. `sef.limit.targets`.
4. `sef.limit.mail`.
5. `sef.limit.definitions`.

Pass conditions:

1. LuckPerms absence never prevents startup.
2. Missing or malformed metadata falls through to a finite permission tier or default.
3. Negative metadata is ignored.
4. Valid metadata is selected before finite permission tiers.
5. No value exceeds the hard ceiling.
6. Provider exceptions appear in `/sef doctor` without breaking the server.
7. Removing LuckPerms restores finite fallback behavior.

## 7. Permission manifest and finite quota tiers

Open `<world>/serverconfig/sef/permission-manifest.json`.

Pass conditions:

1. Entries are deterministic and contain no duplicate ids.
2. Kernel GUI, HUD, panel, target, audience, editor, alias, bundle, profile, bypass, and sensitive data capabilities are present and denied by default.
3. `commands.sef.commands` is present and allowed by default.
4. `commands.sef.conflicts` and `commands.sef.doctor` are present and denied by default.
5. Finite home, player warp, target, mail, and definition tier nodes are present and denied by default.
6. No finite tier is interpreted as unlimited.

## 8. Cooldown persistence and clean shutdown

Set a workstation cooldown to at least 120 seconds and leave `commandKernel.persistentCooldownMinimumSeconds` at 60.

1. Use the workstation as a player.
2. Stop the server cleanly before the cooldown expires.
3. Confirm `<world>/serverconfig/sef/cooldowns.json` exists and uses the `command cooldowns` schema 1 envelope.
4. Restart immediately.
5. Attempt the long form and an alias.
6. Wait for expiry and retry.
7. Repeat with a cooldown below the persistence threshold.

Pass conditions:

1. The qualifying cooldown survives restart.
2. Long and short forms share the restored expiry.
3. An expired cooldown is discarded.
4. A cooldown below the persistence threshold is not retained.
5. The stored file contains UUID, canonical action id, and epoch expiry, not a nickname or display name.

## 9. Player profile and legacy nickname migration

On a stopped staging server with no `sef.playerdata.json`:

1. Place a valid legacy `sef.playerdata` file in the world player data directory.
2. Keep a copy of the source.
3. Start the server.
4. Resolve imported identities with `/whois`.
5. Change one nickname and stop the server.
6. Inspect the new JSON envelope and `.backups`.
7. Restart and resolve the identity again.

Pass conditions:

1. Import occurs only when the JSON store is absent.
2. The source is backed up before migration.
3. The JSON file uses domain `integrated player identities` and schema 1.
4. UUID remains authoritative.
5. Authenticated username and display nickname remain separate.
6. Existing explicit permission grants are unchanged.
7. `/sef doctor` reports the profile count and a ready state.
8. Restart preserves the result.

## 10. Corrupt profile recovery

1. Stop the staging server.
2. Replace `sef.playerdata.json` with malformed JSON.
3. Start the server.
4. Run `/sef doctor` and `/sef storage status`.
5. Attempt to set a nickname.
6. Stop the server.

Pass conditions:

1. The malformed file moves under `.corrupt`.
2. `/sef doctor` reports profile recovery state and returns attention.
3. The nickname command fails without changing memory.
4. Shutdown does not recreate or overwrite the original path.
5. The quarantined evidence remains available for recovery.

## 11. Corrupt and future repository recovery

Run this separately for `location-history.json` and `cooldowns.json`.

1. Stop the staging server.
2. Preserve the valid file.
3. Test malformed JSON, a mismatched domain, and a future schema version.
4. Start the server after each fixture.
5. Run `/sef doctor` and `/sef storage status`.
6. Stop the server without editing the file in game.

Pass conditions:

1. Malformed and mismatched files are quarantined.
2. A future schema remains in place and reports unsupported.
3. The storage coordinator enters recovery mode.
4. The repository refuses to replace the source during shutdown.
5. Location history mutation fails closed.
6. Runtime cooldown checks may continue, but persistent cooldown recovery remains non writable.
7. No fixture causes silent data loss or server startup failure.

## 12. Crash simulation

1. Start from valid repository files.
2. Trigger a qualifying cooldown.
3. Terminate the staging server process without the normal `stop` command.
4. Preserve the world before restarting.
5. Inspect the target, temporary files, backups, journal, and log.
6. Restart.

Pass conditions:

1. A partially written target is never accepted.
2. The previous complete target remains readable, or the damaged target is quarantined.
3. Temporary files do not replace a valid target automatically.
4. Startup produces an actionable state.
5. Restoring a known good backup while stopped returns the repository to ready state.

## 13. Final evidence review

Before approval:

1. Run `./gradlew test`.
2. Run `./gradlew build`.
3. Confirm the dedicated server starts and stops cleanly.
4. Search `logs/latest.log` for exceptions, client class loading, private message bodies, raw addresses, tokens, and credentials.
5. Inspect the final JAR metadata and resources.
6. Inspect the complete Git diff.

Approval requires every applicable pass condition, no unresolved security regression, and no claim that a later phase feature is already available.

## 14. Candidate automated audit record for 2026-07-26

This record applies to code commit `b415bc3cc7647908862711672b5bc4681bbc4dc5`. It is automated and headless evidence only. The manual acceptance sections above remain required.

| Field | Recorded value |
| --- | --- |
| Artifact | `build/libs/sef-1.0-SNAPSHOT.jar` |
| SHA-256 | `2e94f5c4a9263bf8f5271728f7be565c4e578b969b27ef423c81c77572a3f39c` |
| Minecraft | `1.21.1` |
| NeoForge | `21.1.233` |
| Gradle | `8.8` |
| Build Java | OpenJDK `21.0.11` |
| Operating system | Linux `6.12.63+deb13-amd64`, x86 64 |
| Optional permission provider | NeoForge default handler. LuckPerms was absent. |

Verified automated coverage:

1. The current suite passed 117 tests with zero failures, zero errors, and zero skipped tests.
2. Real Brigadier tests cover child projection, denied direct execution after parse, execution time permission revocation, and the vanish queue requirement for both queue and other target permissions.
3. Catalog contract tests cover complete descriptors, capabilities, shortcuts, alias ownership decisions, bundle limits, wrapper recursion, quotas, hierarchy, cooldowns, warmups, confirmations, cost rollback, and structured outcomes.
4. Every currently executable `/sef` action has catalog ownership and enters the shared policy and audit pipeline.
5. Storage tests cover atomic replacement, backups, quarantine, future schema refusal, unknown field preservation, dynamic deletion semantics, migration preparation failure, bounded profile import, concurrent dirty revisions, location history bounds, cooldown persistence, and shutdown flushing outside the calling thread.
6. Coalesced persistence tests cover queue bounds, latest snapshot retention, failure reporting, recovery after a newer successful write, bounded shutdown draining, and post shutdown rejection.
7. The no integration dedicated server reached ready state and saved all dimensions through its shutdown hook without a repository, storage, persistence, optional integration, or client class loading error.
8. The final JAR metadata declares server compatibility through `IGNORE_SERVER_VERSION`, contains no client mixin entries, and keeps LuckPerms, FTB Essentials, and Curios optional.

Manual status:

| Section | Status | Remaining evidence |
| --- | --- | --- |
| Clean startup and server only compatibility | Blocked | A client without SEF must join, use command fallback, and observe a normal `stop`. |
| Catalog and diagnostic permissions | Blocked | Authenticated suggestion trees and operator output must be captured. |
| Workstation routes and aliases | Blocked | Live cooldown sharing, permission removal, and repair preconditions must be exercised. |
| Shortcut conflicts | Blocked | Restart based structural changes and an external command owner must be tested. |
| LuckPerms quotas | Blocked | Provider metadata, malformed values, refresh, removal, and provider failure behavior must be exercised. |
| Permission manifest | Partially verified | Determinism and duplicate rejection are automated. The generated staging artifact still needs operator review. |
| Cooldown persistence | Blocked | A qualifying live cooldown must survive an actual restart. |
| Player profile migration | Blocked | A staging legacy fixture, backup, nickname change, stop, and restart must be recorded. |
| Corrupt recovery | Blocked | Profile, location, and cooldown recovery must be exercised on disposable files. |
| Crash simulation | Blocked | A forced process termination and recovery inspection must be recorded. |

Phases 2 and 3 have implementation coverage, but they are not release approved from this record.

## 15. Superseding headless operator and recovery record for 2026-07-26

This record applies to source commit `48f0f4fe670fee3ff7d13e15225ff4d585a85005` and artifact `build/libs/sef-1.0-SNAPSHOT.jar`, SHA-256 `41ca28dafb3495cfb6cdb1aa05150f4969da4986808150c4184d2551a61aeff4`. It expands headless evidence without replacing authenticated player acceptance.

Verification results:

1. `./gradlew test --rerun-tasks` passed 117 tests with zero failures, zero errors, and zero skipped tests.
2. `./gradlew build --rerun-tasks` completed successfully under OpenJDK `21.0.11`.
3. The packaged JAR contains NeoForge metadata, server mixins, `CommandExecutionService`, `PlayerProfileRepository`, `StorageCoordinator`, and `VanishCommand`.
4. The dedicated server reached the ready state without optional integrations, accepted `sef doctor`, `sef storage status`, and literal `stop`, saved every dimension, and returned a successful Gradle result.
5. Immediate clean restart loaded ready player profiles and cooldown storage. `/sef doctor` reported 20 catalog entries, 133 capabilities, 10 shortcuts, 20 policies, 5 quotas, zero import failures, zero quota provider failures, inactive recovery mode, and no kernel errors.
6. LuckPerms NeoForge `5.4.140` started alone and in the full integration stack. SEF used the LuckPerms permission handler and reported no provider failure. No metadata mutation, malformed value, live refresh, or provider exception was injected.
7. The generated permission manifest was readable at 25,117 bytes during the operator status check. Determinism, duplicate rejection, capability coverage, default access, and finite tier behavior remain covered by automated tests.
8. A valid legacy `sef.playerdata` fixture migrated only when the JSON target was absent. The migration wrote a version 0 backup, a journal entry, a schema 1 identity envelope, and two UUID authoritative records. Both nickname identities resolved and survived restart.
9. Malformed profile JSON was quarantined. Profile state changed to recovery, operator diagnostics returned attention, and shutdown refused to overwrite the source. Restoring the byte identical known good file returned the next startup to ready state.
10. Malformed cooldown JSON was quarantined. Repository recovery became active, operator diagnostics identified the cooldown domain, and shutdown did not recreate the source. Restoring the byte identical known good file returned the next startup to ready state.
11. A ready dedicated server was terminated with `SIGKILL`. Gradle reported the expected exit value `137`. The valid profile and cooldown targets retained their original hashes, no partial or temporary target was found, and the next startup was clean. A player generated qualifying cooldown was not dirty during this test.
12. The SEF configuration evidence archive is `sef-config-audit.tar.gz`, SHA-256 `1d7c765501c26141df1344aa5a801e66f72e3f68b7b285527b8970209ec8cf71`.
13. `git diff --check` passed before the implementation commit. The development `run` tree and optional integration JARs remain ignored and were not committed.

Updated manual status:

| Section | Status | Remaining evidence |
| --- | --- | --- |
| Clean startup and server only compatibility | Partially verified | Dedicated startup and normal stop pass. A client without SEF must still join and use command fallback. |
| Catalog and diagnostic permissions | Partially verified | Console diagnostics and automated Brigadier policy pass. Authenticated filtered suggestions and denied direct execution remain. |
| Workstation routes and aliases | Blocked | Live player cooldown sharing, permission removal, and repair precondition behavior remain. |
| Shortcut configuration and conflicts | Blocked | Restart based alias changes and a real external command owner remain. |
| LuckPerms quota integration | Partially verified | Provider absence, startup presence, and diagnostics pass. Metadata precedence, malformed values, live refresh, hard ceiling, exception, and removal behavior remain. |
| Permission manifest and finite tiers | Partially verified | File generation and automated contract checks pass. Staging group behavior still needs operator review. |
| Cooldown persistence | Partially verified | Clean repository restart passes. A qualifying player cooldown, alias check, expiry, and below threshold case remain. |
| Player profile and legacy migration | Partially verified | Import, backup, journal, envelope, resolution, and restart pass. Authenticated username capture and a live nickname change remain. |
| Corrupt profile recovery | Partially verified | Quarantine, recovery diagnostics, non overwrite, restore, and restart pass. An authenticated nickname mutation attempt during recovery remains. |
| Corrupt and future repository recovery | Partially verified | Malformed cooldown recovery passes. Mismatched domain, future schema, and location history cases remain. |
| Crash simulation | Partially verified | Basic forced termination preserves valid targets and restarts. A dirty qualifying cooldown and write in progress crash remain. |
| Final evidence review | Partially verified | Tests, build, JAR inspection, normal stop, restart, and diff checks pass. Authenticated log privacy and every remaining manual section still block approval. |

Phases 2 and 3 remain unapproved until the remaining authenticated, player driven, location history, future schema, external conflict, and dirty shutdown cases pass.
