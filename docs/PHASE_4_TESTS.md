# Phase 4 teleport verification

## Scope

This matrix covers homes, teleport requests, back history, spawn layers, server warps, player-hosted warps, random teleportation, direct administrative teleportation, safe destination validation, persistence, and FTB Essentials ownership policy.

Phase 4 remains server authoritative and does not register a custom payload protocol. Vanilla clients use the same command routes as modded clients.

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

The GameTest server must report three required tests and must exit successfully. Current fixtures protect these contracts:

1. A loaded destination with solid support and clear feet and head space is accepted.
2. Magma support is rejected by the default hazard policy.
3. An unavailable dimension returns `dimension_missing` without generating chunks.

Unit tests protect:

1. Case-insensitive home names and display-case retention.
2. Home overwrite confirmation and quota-neutral replacement.
3. Total and per-dimension home quotas.
4. Soft deletion, recovery, and current-quota enforcement.
5. Unavailable dimensions surviving a repository round trip.
6. Recovery mode after malformed collections.
7. Player-warp publication, access, blocking, visits, transfer quota, stable identity, and home independence.
8. Ambiguous request acceptance failing closed.
9. Request completion being single-use.
10. Accepted requests remaining authoritative through warmup.
11. Expiry and logout invalidation.
12. Invalid dimension identifiers, non-finite coordinates, inverted RTP radii, and unsafe search bounds being rejected.

## Dedicated-server smoke test

Start the dedicated server with an empty `mods` directory except for the built SEF JAR. Wait for the ready line, run `/sef doctor`, then stop normally. Repeat with LuckPerms, FTB Essentials, and both together.

The server must:

1. Start without client-only class loading.
2. Register only the configured teleport command families.
3. Keep `/tp` owned by vanilla unless `ownVanillaTeleportRoot` is enabled.
4. Select one of `sef`, `external`, `coexist`, or `import_once` ownership modes.
5. Flush `teleports.json`, location history, and cooldowns during normal shutdown.
6. Reload the same record revisions after restart.

## Three-player request and visibility matrix

Use one operator, one ordinary player, and one vanished staff player.

1. Create simultaneous requests to the same recipient. Bare `/tpaccept` must fail as ambiguous.
2. Accept one request by sender or request id. It must not be accepted twice.
3. Move, take damage, disconnect, change dimension, and lose permission during warmup. Each case must cancel without teleporting or charging.
4. Block a sender and disable requests. Error text and suggestions must not disclose vanished players.
5. Test sender and recipient logout at every request state.
6. Test `/tpaall` target limits and visibility filtering.
7. Confirm social or Discord bridges receive no private teleport-request data.

## Destination policy matrix

Exercise homes, server warps, player warps, back, spawn, RTP, and direct teleportation against:

1. Overworld, Nether, End, and one removed dimension fixture.
2. World-border edge and outside-border positions.
3. Void, lava, fire, cactus, powder snow, magma, suffocation, missing support, and Nether-roof positions.
4. Loaded and unloaded chunks.
5. Search-check and chunk-budget exhaustion.
6. Claim adapter allow, deny, unavailable, and exception behavior.
7. Combat, vehicle, passenger, gliding, sleeping, and portal states.
8. Destination mutation during warmup.

No command may run an unbounded search or generate an uncontrolled distant chunk.

## Player-warp matrix

1. Create duplicate local names for different owners.
2. Confirm ambiguous unqualified lookup requires `owner:name`.
3. Test private, shared, unlisted, and public access.
4. Test trusted and blocked UUID sets.
5. Publish, unpublish, suspend, restore, relocate, report, favorite, and record visits.
6. Transfer at, below, and above the recipient quota.
7. Convert from a home and verify later deletion or movement never couples the records.
8. Delete and restore within the recovery policy.
9. Verify hidden, suspended, deleted, blocked, and vanished-owner records do not leak through lists or suggestions.

## Completion record

Record:

1. Commit SHA.
2. JAR SHA-256.
3. Java, Minecraft, NeoForge, LuckPerms, and FTB Essentials versions.
4. Configuration snapshot with secrets removed.
5. Unit-test count and result.
6. GameTest count and result.
7. Dedicated-server startup and shutdown result.
8. Client types and authenticated usernames used by the three-player matrix.
9. Every failed row with its log or crash-report path.

### Audit remediation record, 2026-07-26

The Phase 4 audit remediation code was recorded at source commit `2153eac86c725c4bb1652ce4d9d8b7ac303ca49b` on `envy/phase-5`.

1. Artifact: `build/libs/sef-1.0-SNAPSHOT.jar`.
2. JAR SHA 256: `93d6352e8b4a584ec6b4c73f5e83e3c087970d723c6fdf1271993b78cdb495fa`.
3. Unit tests: 156 passed through `./gradlew test --rerun-tasks`.
4. GameTests: all 3 required tests passed through `./gradlew runGameTestServer`.
5. Build: `./gradlew build --rerun-tasks` completed successfully.
6. Dedicated server: `./gradlew runServer` reached `Done`, accepted `sef doctor` and `sef identity coverage`, and stopped cleanly.
7. Diagnostics: 71 catalog entries, 242 capabilities, 78 shortcuts, 71 policies, 6 quotas, 4 repositories, no recovery mode, no import failures, no quota provider failures, and no kernel errors.
8. Command enforcement: home, server warp, player warp, spawn, random teleport center, offline teleport queue, and teleport request mutations use canonical kernel actions.
9. Target safety: migrated single player arguments use nickname aware and vanish safe identity resolution. `/homes <player>` uses shared hierarchy and exemption policy.
10. Request execution: `/tpaccept` has one accept action lease, `/tpaall` applies one bounded fan out lease, and an empty `/tprequests` read succeeds.
11. Artifact inspection found the expected mod metadata, mixin configuration, and remediation classes. No changed server package referenced `net.minecraft.client`.

This record does not complete the authenticated three player, destination, player warp, optional provider, forced shutdown, or profiler rows above. Those remain release gates.

Do not approve a public release while a required row is untested or failing.
