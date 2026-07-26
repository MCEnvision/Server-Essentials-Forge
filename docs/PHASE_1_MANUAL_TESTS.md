# Phase 1 manual multiplayer tests

## Purpose

This matrix covers behavior that pure JUnit tests and a headless dedicated server cannot prove. Run it before approving a public release that contains Phase 1. It is not a substitute for `./gradlew test`, `./gradlew build`, or the dedicated integration matrix.

Record the SEF commit, JAR hash, Minecraft version, NeoForge version, Java version, LuckPerms version, optional mods, configuration archive, tester names, and result for every run.

## Test environment

Use a disposable dedicated server and at least three authenticated clients:

1. `owner`, with the test administration permissions.
2. `staff`, with selected lower hierarchy permissions.
3. `player`, with only ordinary player permissions.

Create a fourth offline profile that has joined at least once for known offline identity checks. Keep a copy of the world and configuration before every migration or corruption test.

Run the matrix once without optional integrations and once with LuckPerms. Run Curios inventory cases with Curios installed. Run nickname provider ownership cases with FTB Essentials and its dependencies installed.

## Permission and command projection

1. Give `player` `sef.commands.sef.allowed` and `sef.commands.sef.info`.
2. Confirm `/sef info` is suggested and executes.
3. Confirm `/sef reload`, `/sef test`, `/sef filter`, and `/sef storage` are not suggested and are denied when typed.
4. Grant one child permission at a time and confirm only that child becomes visible.
5. Revoke the child permission while the player remains online and refresh the command tree.
6. Confirm the child disappears and direct execution is denied.
7. Repeat after reconnect.
8. Confirm command blocks do not gain the player permission bypass.
9. Confirm console can run authorized administrative routes.
10. Repeat visibility, execution, live revocation, and reconnect checks for `/msg`, `/tell`, `/w`, `/r`, `/pchat`, `/ac`, `/chat admin`, `/helpop`, `/helpopop`, `/ans`, and `/toggle`.
11. Send unique test content through each permitted private or staff message route.
12. Confirm ordinary server logs contain route metadata and message length where applicable, but none of the unique message bodies.

Expected result: root access never exposes a child action, denied children are absent from suggestions, and execution repeats the permission decision.

## Sudo disablement

1. Set `modules.sudo = true`.
2. Restart the server.
3. Confirm startup reports that sudo remains unavailable.
4. Run `sudo say should_not_run` from console and every test player.
5. Confirm the command is unknown and no chat message is sent.
6. Confirm no alias, announcement, or `/sef` child reaches sudo behavior.

Expected result: no sudo route exists.

## Duration rejection

For announcements, countdowns, mutes, freezes, and warnings, try empty, zero, negative, duplicate-unit, unknown-unit, trailing-text, overflow, and permanent values where permanent is not allowed.

Expected result: every invalid value is rejected before state changes. Invalid moderation input never creates a permanent punishment.

## Nickname identity and ownership

1. Give `player` self nickname permission but not other-player, color, or style permissions.
2. Confirm a plain self nickname works.
3. Confirm changing another player, colors, styles, invisible formatting, unsafe Unicode, and over-limit visible names are denied.
4. Add the needed formatting permission and confirm only that formatting class becomes available.
5. Attempt collisions with every online username, online nickname, known offline username, and known offline nickname.
6. Confirm ambiguous normalized identities are rejected.
7. Restart and confirm `/whois` resolves the retained unambiguous identity.
8. With FTB Essentials selected, confirm SEF does not write integrated nickname state and does not register a conflicting nickname route.

Expected result: identity comparisons are normalization safe, persisted, unambiguous, and owned by exactly one provider.

## Vanish visibility and revocation

1. Configure `owner` above `staff` in hierarchy.
2. Give `owner` `sef.vanish.1` and `staff` `sef.vanish.3`.
3. Give each observer only the matching `sef.vanishsee.N` levels needed for the visibility matrix.
4. Vanish each actor and verify tab list, server status list, selectors, entity rendering, equipment, sounds, particles, chat, system messages, join or leave presentation, collision, mob targeting, and trace behavior from every observer.
5. Confirm lower staff cannot change or inspect a higher target.
6. Grant `sef.vanish.hierarchy.bypass` and confirm only the intended hierarchy restriction changes.
7. Grant and test exemption, then grant the exemption bypass separately.
8. Revoke the actor’s vanish permission while vanished.
9. Confirm immediate or next reconciliation safe unvanish, restored tab information, restored entity tracking, cleared action bar, and cleared persisted state.
10. Repeat across dimension change, death and respawn, reconnect, LuckPerms user data refresh, configuration reload, and full module disable.
11. Confirm each observer receives an independent player information result. One observer’s permission must never leak a vanished entry to another observer.

Expected result: visibility matches the tested matrix, target hierarchy fails closed, and state cannot survive permission or module loss.

## Inventory inspection

1. Grant only `sef.commands.invsee.view`.
2. Open another online player’s inventory and confirm every slot is read only.
3. Attempt pickup, shift click, swap, throw, quick craft, hotbar swap, and collect to cursor.
4. Grant `sef.commands.invsee.modify` and confirm authorized changes work.
5. Revoke modify while the menu remains open and confirm immediate read-only downgrade.
6. Revoke view and confirm the menu closes.
7. With Curios installed, confirm Curios pages are invisible without `sef.commands.invsee.curios`.
8. Grant Curios access, open a Curios page, then revoke it and confirm the menu closes.
9. Confirm modification audit events contain issuer, target, page, slot, and click type but no item NBT.
10. Confirm offline inventory and other-player ender chest routes are unavailable because those implementations remain reserved for later phases.

Expected result: viewing cannot imply mutation, revocation takes effect in an open menu, and Curios data is not queried without permission.

## Alternate account privacy

Use test addresses only. Do not copy production addresses into reports.

1. Leave `collectAddresses = false`, join from multiple test clients, and confirm no correlation records are collected.
2. Enable collection with `hashAddresses = true`.
3. Join again and confirm `alt_data.json` contains hashed keys rather than raw addresses.
4. Confirm local addresses are ignored.
5. Run `/checkalts <player>` without `sef.alts.ip.view` and confirm output is redacted.
6. Grant raw view. With hash storage still enabled, confirm the original address cannot be reconstructed.
7. Use a short retention value in the disposable environment and confirm expired records are removed.
8. Confirm purge and export are hidden and denied without their individual permissions.
9. Test `/checkalts purge expired`, `/checkalts purge confirm`, and `/checkalts export` with the exact permissions.
10. Confirm exports are redacted unless raw storage was explicitly selected and the issuer has raw view.
11. Confirm logs and audit events contain counts and operation metadata but no address.

Expected result: collection is opt in, retained data is bounded, sensitive display is independently authorized, and destructive operations are explicit.

## Storage and recovery

1. Start from each supported legacy document fixture.
2. Confirm a timestamped backup and migration journal entry are created before rewrite.
3. Confirm the migrated document has the expected domain and schema version.
4. Add an unknown field to a retained fixed record, mutate the record in game, and confirm the field survives.
5. Delete a dynamic record in game and confirm it does not reappear.
6. Test malformed JSON, a mismatched domain, an invalid schema, a future schema, an empty file, and a file over the size limit on copies of disposable data.
7. Confirm corrupt input is quarantined and not silently overwritten. Confirm future schema input is refused.
8. Run `/sef storage status` and compare every reported state with disk.
9. Run `/sef storage export` without alternate account export permissions and confirm alternate account data is excluded.
10. Grant both required alternate account permissions and confirm inclusion.
11. Stop during queued audit and export work.
12. Confirm the shutdown flush completes or reports an explicit bounded failure.

Expected result: migrations are recoverable, corruption cannot silently erase evidence, deletion semantics remain correct, and exports stay inside their managed directory.

## Performance observation

Use a profiler and a disposable populated world.

1. Populate banned entries, player inventories, Curios slots, announcements, alternate account records, cooldowns, and chat history near configured limits.
2. Leave banned block background scanning disabled and confirm event driven enforcement still works.
3. Enable scanning with a small budget and confirm unloaded chunks are not generated.
4. Confirm the configured position budget is not exceeded per tick.
5. Observe tab refresh cadence, LuckPerms metadata refresh, audit queue behavior, export queue rejection, cooldown pruning, and shutdown.
6. Confirm no per-tick filesystem access, network lookup, full-world scan, or unbounded collection growth appears.

Expected result: current hot paths remain bounded and configuration limits behave as documented.

## Completion record

For each section record:

1. Pass, fail, or blocked.
2. Exact commands and permission changes.
3. Relevant configuration.
4. Sanitized log excerpts.
5. Screenshots or recordings where packet visibility matters.
6. Issue link and retest result for every failure.

Do not approve a public release with a failed security, privacy, persistence, shutdown, or visibility case.

### Candidate verification record for 2026-07-26

This record applies to code commit `e48607d19a22d51e42a03d26c516ba08fc4de3a4`. It is a partial verification record, not public release approval.

| Field | Recorded value |
| --- | --- |
| Artifact | `build/libs/sef-1.0-SNAPSHOT.jar` |
| SHA-256 | `c6dfd651e20d6062c288a49e1c4b3ad012f14d5c88a6184c32187a2963996de2` |
| Minecraft | `1.21.1` |
| NeoForge | `21.1.233` |
| Java | OpenJDK `21.0.11` |
| Optional integrations | LuckPerms absent, Curios absent, FTB Essentials absent |
| Server mode | Disposable development server, offline mode, port `25577` |
| Tester clients | None available in this workspace |
| Configuration archive | Not produced because the authenticated client matrix did not begin |
| Screenshots and recordings | Not produced because the authenticated client matrix did not begin |

Automated and headless evidence:

1. `./gradlew test --rerun-tasks` passed 94 tests with zero failures, zero errors, and zero skipped tests.
2. `./gradlew build --rerun-tasks` completed successfully.
3. A direct ModDevGradle dedicated server launch reached `Done`, ran `sef doctor`, ran `sef storage status`, accepted `stop`, saved every dimension, and exited with code zero.
4. `sef doctor` reported 12 catalog entries, 133 capabilities, 10 shortcuts, 12 policies, 5 quotas, ready player profiles, zero import failures, zero quota provider failures, and no kernel errors.
5. `sef storage status` reported 13 managed documents and returned without an error.
6. The final direct launch log contained no `ERROR`, exception chain, failed mixin, or failed shutdown entry.
7. A separate `./gradlew runServer` attempt reached a ready server, but Gradle did not forward interactive console input in this terminal. The process was terminated with `SIGTERM`, Minecraft saved all dimensions, and Gradle correctly reported exit code `143`. This attempt is not counted as a passing smoke test.

Section status:

| Section | Status | Evidence and remaining work |
| --- | --- | --- |
| Permission and command projection | Blocked | Real Brigadier tests cover `/sef` projection, direct denied execution, and `/v queue <player>` dual permission gating. Authenticated suggestion, live revocation, reconnect, command block, console, and message body log checks remain manual. |
| Sudo disablement | Blocked | The dedicated server logged that sudo remains unavailable. Console and three client execution attempts remain manual. |
| Duration rejection | Blocked | Existing parser tests cover invalid duration classes. Live command state mutation checks remain manual. |
| Nickname identity and ownership | Blocked | Authenticated identity, reconnect, collision, and FTB Essentials provider ownership cases remain manual. |
| Vanish visibility and revocation | Blocked | Automated tests cover permission reconciliation, hierarchy policy, observer specific immutable player information projection, unloaded configuration packet guards, and server status shutdown short circuiting. Actual client tab, entity, sound, particle, selector, server list, reconnect, and LuckPerms refresh observations remain manual. |
| Inventory inspection | Blocked | A NeoForge bootstrapped menu test proves modify revocation downgrades an open menu and view revocation closes it. Authenticated click modes, audit output, and Curios cases remain manual. |
| Alternate account privacy | Blocked | Authenticated address collection, retention, display, purge, and export cases remain manual. |
| Storage and recovery | Blocked | Headless status and clean shutdown passed. Fixture migration, corruption, unknown field, deletion, export permission, and interrupted work cases remain manual. |
| Performance observation | Blocked | No populated world, profiler session, or three client load was available. |

The full matrix remains blocked because this workspace does not provide three authenticated Minecraft clients, tester controlled rendering and packet observation, or the required optional integration environments. Do not treat the automated results as a substitute for those cases, and do not approve a public release from this record.

### Superseding automated audit record for 2026-07-26

This record supersedes the automated and headless evidence above for code commit `b415bc3cc7647908862711672b5bc4681bbc4dc5`. It does not supersede any blocked manual section and is not public release approval.

| Field | Recorded value |
| --- | --- |
| Artifact | `build/libs/sef-1.0-SNAPSHOT.jar` |
| SHA-256 | `2e94f5c4a9263bf8f5271728f7be565c4e578b969b27ef423c81c77572a3f39c` |
| Minecraft | `1.21.1` |
| NeoForge | `21.1.233` |
| Gradle | `8.8` |
| Build Java | OpenJDK `21.0.11` |
| Operating system | Linux `6.12.63+deb13-amd64`, x86 64 |
| Optional integrations | LuckPerms absent, Curios absent, FTB Essentials absent |
| Authenticated tester clients | None available in this workspace |

Latest automated and headless evidence:

1. `./gradlew test --rerun-tasks` passed 117 tests with zero failures, zero errors, and zero skipped tests under Java 21.
2. `./gradlew build --rerun-tasks` completed successfully under Java 21.
3. The packaged JAR contains the shared command executor, coalesced persistence worker, player profile repository, storage coordinator, vanish command, server mixins, and NeoForge metadata.
4. The permission audit found direct NeoForge online and offline permission calls only inside `PermissionService`. Compatibility methods delegate to that facade.
5. No `/sudo` registration call exists. The dormant class remains packaged for later migration, but the command registration handler publishes no sudo Brigadier node.
6. A fresh no integration dedicated server reached `Done` in 0.843 seconds. The terminal used `SIGINT` because the Gradle run task did not forward a literal `stop`. Minecraft still entered its shutdown hook, saved players, saved all three dimensions, and logged no storage or persistence error. This is shutdown evidence, but it does not satisfy the normal `stop` or shutdown race cases.
7. The final diff passed `git diff --check`. Source scanning found no Bukkit or EssentialsX implementation dependencies and no common source reference to `net.minecraft.client`.

Manual status remains blocked for authenticated permission projection, packet visibility, InvSee interaction, nickname ownership with FTB Essentials, LuckPerms refresh behavior, Curios inventory behavior, address collection, migration fixtures, normal `stop`, shutdown races, and profiler observation.
