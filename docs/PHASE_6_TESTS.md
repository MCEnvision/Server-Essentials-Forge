# Phase 6 moderation and observation verification

## Scope

This matrix covers expanded player and address moderation, persistent warnings and controls, jails, command spy, correlated command-event journaling, optional file logging, connection-event logging, alternate-account privacy boundaries, and compatibility with the earlier protection managers.

Phase 6 remains server authoritative and server only. It registers no custom payload. Vanilla clients use the same commands and receive only server-approved text.

## Automated gate

Run with Java 21:

```bash
./gradlew test
./gradlew runGameTestServer
./gradlew build
```

Automated tests protect:

1. Phase 6 Brigadier root registration and denial without the matching permission.
2. Representative player, IP, kick, warning, mute, command-spy, logging, and confirmation grammar.
3. Command-spy typed filter persistence and legacy profile compatibility.
4. Player-filter transitions between include, exclude, neutral, and removed.
5. IPv4 and IPv6 redaction for every IP moderation alias, including namespaced roots.
6. Disabled file logging without directory creation or a writer thread.
7. Connection-event serialization without raw addresses.
8. Security-root capture immunity.
9. Fail-closed capture-filter overflow without replacing the previous valid snapshot.
10. Retention rejection when the archive set changes after preview.
11. Parent and nested symbolic-link refusal before any external path is created.
12. Shared-proxy fail-safe decisions and shared-session hard caps.
13. Moderation warning, control, jail, sentence, expiry, release location, and timestamp persistence.
14. Phase 6 catalog and shortcut ownership.

The GameTest server must pass every registered world fixture and exit normally. Current Phase 6 policy coverage is primarily JUnit and Brigadier based. The authenticated action matrix below remains required.

## Dedicated-server smoke test

Start a dedicated server with default Phase 6 modules, no optional integrations, and file logging disabled.

Verify:

1. Startup reaches the ready state without client class loading.
2. `moderation.json` and `command-spy.json` report a writable state.
3. `logs/sef` does not exist.
4. `/sef doctor` reports no catalog, policy, permission, repository, or recovery error.
5. `/sef storage status` reports moderation and command-spy domains.
6. Disabled legacy moderation owners do not process the same active control twice.
7. Normal `stop` flushes dirty repositories.
8. Restart restores warnings, controls, jails, sentences, and command-spy profiles.

Enable file logging in staging and restart. Verify:

1. Only `logs/sef` is created.
2. `commands/current.jsonl` and state markers remain within that root.
3. Connection files are absent until the connection stream is enabled.
4. `/sef logging status`, `stats`, `doctor`, `flush`, and `rotate` report truthful state.
5. Normal `stop` drains within the configured bound and removes the active-session marker.
6. A forced interruption leaves an incomplete-session marker that is reported on restart.

Run:

```text
sef doctor
sef storage status
sef logging status
sef logging stats
stop
```

## Permission and command-tree matrix

Use a denied player, a limited moderator, a senior moderator, and console.

1. Confirm each root is absent from suggestions without its root permission.
2. Grant only the root permission and verify separately controlled subcommands remain hidden.
3. Revoke a permission while the player is online and verify the refreshed tree removes it.
4. Revoke immediately before execution and verify the mutation fails.
5. Verify console behavior uses the explicit source policy and does not make player-only observation commands available.
6. Test hierarchy below, equal to, and above the issuer.
7. Test each target exemption and the separately permissioned exemption bypass.
8. Confirm a vanished target is unavailable to an issuer who cannot see that target.
9. Confirm failed eligibility uses the same unavailable result for hidden, exempt, and unknown targets.

## Player moderation matrix

1. Permanently ban an online player with a bounded reason.
2. Temporarily ban an online player and verify the exact expiry.
3. Ban an unambiguous known offline identity.
4. Attempt an unknown identity and an ambiguous nickname.
5. Pardon through `/pardon` and `/unban`.
6. Verify the vanilla user-ban list is the enforcement authority.
7. Attempt an empty, oversized, malformed-duration, zero-duration, duplicate-unit, and overflow duration.
8. Kick one eligible player with default and explicit reasons.
9. Use `/kickme` as a permitted player.
10. Use `/kickall`, inspect the target count, confirm with the returned token, and verify the issuer is excluded.
11. Change the target set, policy revision, or issuer before confirmation and verify the token is rejected.
12. Exceed `maximumMassTargets` and verify no player is disconnected.
13. Restart across temporary-ban expiry and verify vanilla enforcement remains correct.

## Address moderation and proxy matrix

Run only with documented test addresses.

1. Resolve an online player through the `direct` provider.
2. Ban, temporarily ban, pardon, and kick that resolved address.
3. Test `/ban-ip`, `/banip`, `/tempban-ip`, `/tempbanip`, `/pardon-ip`, `/unban-ip`, `/unbanip`, `/kick-ip`, and `/kickip`.
4. Disable player literal input and verify a literal address is refused even with the command root permission.
5. Enable literal input and verify the distinct literal permission is still required.
6. Test IPv4 and IPv6 normalization.
7. Resolve multiple sessions on one test address and verify the shared-address target preview.
8. Exceed `sharedAddressHardCap` and verify no action occurs.
9. Put multiple players behind an unconfigured shared proxy and verify `failOnSharedProxy` refuses the operation.
10. Select `disabled` and verify every address mutation fails closed.
11. Select unavailable `external` authority and verify no fallback to an untrusted socket address.
12. Inspect feedback, command spy, security audit, optional files, exports, `moderation.json`, and vanilla ban files. Ordinary surfaces must contain no raw address.

## Persistent control and jail matrix

1. Add, list, and clear warnings.
2. Apply permanent and temporary mute, freeze, inventory lock, and build lock.
3. Verify chat, movement, inventory, item use, block break, and block place enforcement.
4. Revoke the issuer permission during an open administrative menu and verify later mutation is refused.
5. Restart with every control active.
6. Advance beyond each expiry and verify one removal.
7. Define, list, and delete a jail.
8. Jail a player permanently and temporarily.
9. Verify the player remains at the jail through movement, login, respawn, and dimension attempts.
10. Unjail and verify safe return to the recorded release location.
11. Expire a jail sentence during restart and verify safe release.
12. Delete an occupied jail and verify the command refuses or safely handles active sentences without orphaning state.
13. Disable expanded moderation and verify legacy data remains intact without double enforcement.

## Command-spy matrix

Use two command actors and one observer.

1. Enable and disable observation for self.
2. Test everyone and selected-player audiences.
3. Add, remove, clear, and inspect selected UUIDs.
4. Test initiator, effective actor, and either matching.
5. Test player and nonplayer source scopes with player, console, RCON, command block, function, scheduler, panel, bundle, and integration origins where available.
6. Test root and canonical-action include and exclude filters.
7. Test source, player, result, world, and origin typed filters.
8. Toggle location and result projection separately.
9. Revoke metadata, argument, location, result, nonplayer, everyone, selected, exempt-view, and vanished-view permissions during active observation.
10. Verify the next event uses the new decision.
11. Execute a password-like command, private-message command, unknown root, and every address-moderation alias. No secret argument may appear.
12. Publish a duplicate correlated lifecycle event and verify it is not delivered twice.
13. Exceed the per-second delivery bound and verify observation drops remain bounded without affecting command execution.
14. Verify recent history never exceeds its configured limit.
15. Logout the observer and verify runtime delivery state is cleared while the persistent profile remains.

## Optional file-log matrix

1. Start with logging disabled and verify no directory or thread.
2. Enable logging through configuration and through the authorized runtime command.
3. Enable and disable command and connection streams independently.
4. Exercise typed capture and view filters.
5. Attempt to exclude security-critical metadata and verify mandatory security audit remains present.
6. Fill the queue beyond capacity and verify bounded drops and truthful health counters.
7. Submit an oversized record and verify it is rejected or safely bounded.
8. Rotate by size, age, and explicit command.
9. Preview retention, change an archive, and verify confirmation fails.
10. Preview again, confirm the unchanged set, and verify only owned matching archives are removed.
11. Test archive count, age, and total-byte ceilings together.
12. Create an unrelated file and a symbolic link under the test tree. Retention, search, export, and repair must not follow or delete them.
13. Force disk-full, permission-denied, and writer exceptions in a disposable environment.
14. Verify the sink enters a truthful degraded state without blocking the server thread.
15. Search, tail, and export records. No raw command, address, password, token, or private-message body may appear.
16. Run normal shutdown, bounded timeout, forced process termination, and restart diagnosis.

## Completion record

Automated and headless results must be recorded here after the final source commit, including:

1. Commit.
2. Artifact path and SHA-256.
3. Java, Minecraft, and NeoForge versions.
4. Optional integration set.
5. Configuration used.
6. Unit-test count and result.
7. GameTest count and result.
8. Build result.
9. Dedicated-server startup and shutdown result.
10. Diagnostics output summary.
11. JAR and dependency inspection.
12. Remaining manual rows.

Do not approve a public release while a required row is untested or failing.
