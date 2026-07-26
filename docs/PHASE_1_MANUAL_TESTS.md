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
