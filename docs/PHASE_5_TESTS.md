# Phase 5 social and identity verification

## Scope

This matrix covers hardened private messages, reply state, ignore state, social spy, UUID-addressed mail, real custom join and leave messages, welcome and scheduled reminders, custom text pages, nickname provider ownership, server-projected identity surfaces, and the explicit Phase 9 enhanced nametag boundary.

Phase 5 remains server authoritative and server only. It registers no custom payload protocol. Vanilla clients and clients without SEF use command fallback. Capability-aware delivery of the optional client reminder begins in Phase 9. Until that handshake exists, the configured login notice is delivered as an ordinary server message.

## Automated gate

Run with Java 21:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 \
PATH=/usr/lib/jvm/java-21-openjdk-amd64/bin:/usr/local/bin:/usr/bin:/bin \
./gradlew test

JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 \
PATH=/usr/lib/jvm/java-21-openjdk-amd64/bin:/usr/local/bin:/usr/bin:/bin \
./gradlew runGameTestServer

JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 \
PATH=/usr/lib/jvm/java-21-openjdk-amd64/bin:/usr/local/bin:/usr/bin:/bin \
./gradlew build
```

Unit tests protect:

1. Typed placeholder allowlists and rejection of unknown or missing values.
2. Literal insertion of player-controlled message values without formatting or placeholder reparsing.
3. Immutable compiled templates and hard template and rendered-output bounds.
4. Valid default join and leave placeholders.
5. Social preference, ignore, mail, connection-template, reminder, and custom-text persistence.
6. Corrupt social document recovery without partial state.
7. Id, route, template, text, mail, selected-player, and ignore-list bounds.
8. Recipient mailbox quotas and UUID ownership.
9. Archive, delete, and clear isolation between recipients.
10. Reminder delivery count and acknowledgement revision state.
11. Observation event deduplication and bounded event memory.
12. Per-observer social-spy rate limiting and second-window reset.
13. Social catalog ownership, exact subsystem feature ids, and nonempty permission contracts.

The NeoForge GameTest server continues to run the Phase 4 world-safety fixtures because Phase 5 does not add a world mutation. It must report all registered tests as passed and exit successfully.

## Dedicated-server smoke test

Start the dedicated server with the built SEF JAR. Test an empty optional-integration set, LuckPerms alone, FTB Essentials alone, and LuckPerms with FTB Essentials when those test artifacts are available.

The server must:

1. Start without loading a `net.minecraft.client` class.
2. Load `social.json` or create a new writable social repository.
3. Register only enabled Phase 5 command families.
4. Publish distinct runtime feature gates for social spy, mail, connection messages, reminders, and custom text.
5. Start when LuckPerms and FTB Essentials are absent.
6. Select exactly one nickname provider.
7. Write no private message body to ordinary log or audit records.
8. Stop normally and flush dirty social state.
9. Restart and load the same social, mail, reminder, and template revisions.
10. Preserve a malformed source under recovery handling instead of overwriting it.

Run:

```text
sef doctor
sef storage status
sef identity coverage
stop
```

## Three-player private-message and social-spy matrix

Use one sender, one recipient, and one staff observer. Repeat the visibility cases with a vanished sender, vanished recipient, and vanished observer.

1. Verify `/msg`, `/tell`, `/w`, `/whisper`, `/r`, and `/reply`.
2. Verify message text containing `&c`, section signs, braces, click-like text, newlines rejected by command validation, and the maximum configured length.
3. Confirm the configured sent and received formats remain active.
4. Confirm authenticated names remain available through hover or trusted command arguments when display nicknames differ.
5. Disable the recipient message toggle and verify the sender receives only the unavailable response.
6. Add and remove the sender from the recipient ignore list and verify the same unavailable response.
7. Disable reply state and verify both reply aliases fail.
8. Disconnect either participant and verify reply and private-chat session state is invalidated.
9. Enable social spy with metadata only. The observer must receive sender, recipient, route, and hidden-content presentation.
10. Grant content scope and verify content appears only after the content permission is also present.
11. Revoke root, metadata, content, everyone, selected-player, exempt-view, and vanished-view permissions during active observation. The next event must apply the new decision without reconnect.
12. Test everyone, one-player shorthand, multi-player selected sets, sender match, recipient match, either match, route add, route remove, and route reset.
13. Mark either participant exempt and verify ordinary observers receive nothing.
14. Verify an observer who cannot see either vanished participant receives nothing.
15. Publish the same adapter event UUID twice and verify one delivery.
16. Exceed `socialSpyEventsPerSecond` and verify delivery stops at the configured bound without disconnecting a player.
17. Verify `/socialspy recent` never exceeds `socialSpyRecentLimit` and clears on logout.
18. Verify `/socialspy format preview` rejects unknown placeholders and cannot cause sample values to be reparsed.
19. Inspect `latest.log`, security audit JSONL, exports, and `social.json`. Private message bodies must not appear except in the authorized live observer component. Mail bodies are a separate owned persistent feature.

## Mail matrix

1. Send mail to an online player by authenticated name.
2. Send mail to a known offline profile by an unambiguous identity.
3. Attempt an unknown and ambiguous identity. Both must fail without selecting a player.
4. Attempt self-mail and mail to a player who ignores the sender.
5. Fill a mailbox to its effective quota and verify the next send fails.
6. Grant a LuckPerms mail quota metadata value, invalidate permissions, and verify the new finite value.
7. List, read, archive, delete, and clear records as the owner.
8. Attempt each mutation using a different authenticated recipient.
9. Verify online notification and login unread count.
10. Advance beyond retention in a controlled fixture and verify expired records are excluded from list, unread count, and quota use.
11. Restart after every mutation class and verify UUID ownership, timestamps, read state, archive state, and body integrity.
12. Measure a recipient operation with a large unrelated mailbox population. It must use the recipient index rather than scan the global collection.

## Connection-message and vanish matrix

1. Set, preview, inspect, and clear join and leave templates.
2. Test `{player}`, `{username}`, `{uuid}`, and `{world}` separately and together.
3. Attempt an unknown placeholder, control character, and oversized template.
4. Verify each set, clear, preview, and inspect permission independently.
5. Test player hierarchy below, equal to, and above the issuer, plus console.
6. Join and disconnect normally. Exactly one real message must be broadcast for each event.
7. Join and disconnect while vanished. A recipient who cannot see the subject must receive no custom or vanilla connection message.
8. A recipient who can see the vanished subject must receive the configured component.
9. Confirm a custom message equal in visible text to an unrelated server message does not reveal a vanished player.
10. Disable connection messages at runtime and verify the shared feature gate denies management while vanilla presentation remains available.
11. Restart and verify per-player templates and revisions.

## Reminder and custom-text matrix

1. Create a reminder with every supported placeholder.
2. Attempt an unknown placeholder, invalid id, duplicate id, oversized message, negative repeat, excessive repeat, zero maximum, and excessive maximum.
3. Reach the effective definition quota and verify another create fails.
4. Change message, audience, repeat, and maximum deliveries. Verify acknowledgement revision advances.
5. Test all, first-join, command-fallback, and unread-mail audiences.
6. Verify repeat zero delivers once and finite repeats respect their interval and maximum.
7. Dismiss and restore an allowed reminder.
8. Update an acknowledged reminder and verify the new revision can be shown according to policy.
9. Manually send to one player and a bounded selector set. Exceed the target cap and verify no partial delivery.
10. Disable and delete definitions, restart, and verify definition and player state.
11. Set, show, list, replace, and clear custom text through both management roots.
12. Verify `/rules` and `/info` use their matching page ids.
13. Attempt path-like ids, control characters, and oversized content.
14. Disable reminders and custom text at runtime and verify the shared feature gates deny already-registered actions.

## Identity matrix

1. Set and clear an integrated nickname and verify immediate tab refresh.
2. Verify chat, tab, join, leave, death, advancement, SEF identity lookup, command feedback, and list output.
3. Verify Unicode normalization and collision rejection against authenticated usernames and known nicknames.
4. Enable duplicate display names with username hover and verify authenticated identity remains available.
5. Disable duplicate mode and verify the same collision is rejected.
6. Select FTB Essentials ownership and verify integrated `/nick` mutation is refused without duplicating state.
7. Trigger LuckPerms metadata recalculation and verify cache invalidation, tab refresh, command-tree refresh, and vanish reconciliation.
8. Verify `/sef identity coverage` reports Brigadier and signed chat as authenticated surfaces.
9. Verify no common or dedicated-server path claims enhanced in-world nametags before the Phase 9 client contract.

## Vanilla-client compatibility

1. Join with a vanilla client.
2. Join with a client that has unrelated mods but no SEF.
3. Use every player-facing Phase 5 feature through commands.
4. Verify no custom payload is required.
5. Verify the optional-client notice is an ordinary configurable message.
6. Verify disabling enhanced GUI roadmap settings does not affect Phase 5 server-only operation.

## Performance and recovery

1. Populate the hard selected-player and route limits.
2. Populate the configured recent-event limit and exceed the event deduplication capacity.
3. Populate mail for many unrelated recipients and profile recipient operation cost.
4. Populate reminder definitions to the effective hard quota and profile one scheduler pass.
5. Corrupt each top-level social collection independently.
6. Test normal shutdown, forced process termination, and restart with a dirty repository.
7. Confirm no filesystem work occurs in chat, packet-send, or tick hot paths.
8. Confirm runtime collections clear on logout and server shutdown.

## Completion record

Automated and headless verification was recorded on 2026-07-26:

1. Source commit: `bb71df7`.
2. Phase 4 source commit inherited by this branch: `5712ba3`.
3. Artifact: `build/libs/sef-1.0-SNAPSHOT.jar`.
4. JAR SHA-256: `16a5f523fbedb24cc48d03d30b7ba374dbca83fcdb9159476997f9a233f4c935`.
5. Java: OpenJDK `21.0.11`.
6. Minecraft: `1.21.1`.
7. NeoForge: `21.1.233`.
8. LuckPerms: absent for the recorded headless startup.
9. FTB Essentials: absent for the recorded headless startup.
10. Configuration: the ignored development `run` directory, offline mode, default Phase 5 module switches, and no optional integrations.
11. Unit tests: 149 passed through `./gradlew test`.
12. GameTests: 3 required tests passed through `./gradlew runGameTestServer`.
13. Build: `./gradlew build` completed successfully.
14. Dedicated server: four consecutive `./gradlew runServer` cycles reached `Done`, accepted console commands, and stopped cleanly.
15. Diagnostics: `sef doctor` reported 71 catalog entries, 238 capabilities, 78 shortcuts, 71 policies, 6 quotas, 4 coordinated repositories, no recovery mode, no import failures, no quota-provider failures, and no kernel errors.
16. Identity: `sef identity coverage` reported server-projected and authenticated boundaries without claiming the Phase 9 enhanced nametag contract.
17. Persistence: a reminder created from the console produced a ready 624-byte `social.json`, survived a clean restart, rejected a duplicate create, was deleted, and flushed on shutdown.
18. Artifact inspection: the JAR contains `META-INF/neoforge.mods.toml`, `sef.mixins.json`, and every Phase 5 social class. Dependency inspection found no `net.minecraft.client` reference from `com.enviouse.sef.social`.
19. Diff inspection: whitespace, generated-output, ignored-file, absolute-path, debug-output, credential-pattern, client-import, and private-message log scans passed.

### Audit remediation record, 2026-07-26

The Phase 5 audit remediation code was recorded at source commit `2153eac86c725c4bb1652ce4d9d8b7ac303ca49b` on `envy/phase-5`. This record supersedes the earlier artifact hash, unit count, and capability count for the remediated source.

1. Artifact: `build/libs/sef-1.0-SNAPSHOT.jar`.
2. JAR SHA 256: `93d6352e8b4a584ec6b4c73f5e83e3c087970d723c6fdf1271993b78cdb495fa`.
3. Unit tests: 156 passed through `./gradlew test --rerun-tasks`.
4. GameTests: all 3 required tests passed through `./gradlew runGameTestServer`.
5. Build: `./gradlew build --rerun-tasks` completed successfully.
6. Dedicated server: `./gradlew runServer` reached `Done`, accepted `sef doctor` and `sef identity coverage`, and stopped cleanly.
7. Diagnostics: 71 catalog entries, 242 capabilities, 78 shortcuts, 71 policies, 6 quotas, 4 repositories, no recovery mode, no import failures, no quota provider failures, and no kernel errors.
8. Identity: real Brigadier tests accept quoted display names, the selected nickname provider controls online suggestions and resolution, and vanished online UUIDs cannot reappear through known profile matching.
9. Hierarchy: LuckPerms primary group weight is consumed when available. Social spy and connection message targets use shared hierarchy and exemption controls.
10. Quotas: granted mail and definition tier nodes are passed into quota resolution.
11. Observation audit: every delivered social spy event records observer UUID, route, scope, audit class, and redaction metadata without the message body.
12. Connection correlation: equal looking components are separated by object identity, weak references are pruned, and the collection is bounded.
13. Artifact inspection found the expected mod metadata, mixin configuration, and remediation classes. No changed server package referenced `net.minecraft.client`.

The authenticated three-player matrix, vanished-player packet matrix, real LuckPerms and FTB Essentials combinations, mail-retention clock fixture, vanilla-client join, forced dirty shutdown, and profiler rows remain untested release gates. No authenticated usernames or client artifacts were used in the recorded headless run.

Do not approve a public release while a required row is untested or failing.
