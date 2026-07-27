# Phase 11 Verification

## Scope

This record covers custom aliases, bundles, command profiles, fake identity presentation, sudo policy, `/run`, and `/silent`.

Phase 11 is complete on the current source revision. Its authorization, persistence, restart, recovery, failure, mixed-client, and performance gates pass through deterministic tests and runtime verification.

## Environment

- Minecraft `1.21.1`.
- NeoForge `21.1.233`.
- Java `21.0.11`.
- Gradle `8.8`.
- Linux dedicated server.
- Enhanced SEF client and no-SEF fallback client under Xvfb.

## Commands

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew test
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew runGameTestServer
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew build
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew runServer
```

## Results

- All 390 unit tests pass.
- All 29 required GameTests pass.
- The full build and fallback-runtime compilation pass.
- The dedicated server reaches `Done`, reports a healthy security audit and no kernel errors, then stops normally.
- The enhanced client negotiates with a GUI-enabled server and remains connected.
- The fallback client joins the same server without SEF classes, receives command fallback guidance, and remains connected.
- The final JAR SHA-256 is `a1d8e926bd65972ad40b282a341871b743d745da7484c45aef3d5667b6a5169f`.

## Verified behavior

1. Alias drafts validate before publication. Root ownership, conflicts, immutable revisions, rollback, disablement, persistence, restart activation, and canonical permissions remain stable.
2. Bundles use compiled action graphs, frozen bounded cohorts, per-tick pacing, cancellation, recovery, revision revalidation, duplicate-mutation prevention, and terminal job records.
3. Command profiles enforce actor, targeted-actor, and server contexts. Targeted profiles require a server-bound `{target}` placeholder. Published server profiles remain disabled until explicitly enabled.
4. Fake join, leave, message, and schedule presentation uses unsigned system output. Vanished identities are filtered per audience. Schedules persist and execute once.
5. Sudo consent defaults to denied. Administrative locks, hierarchy, exemptions, target visibility, self policy, exact bypasses, and optimistic revisions fail closed.
6. Respect mode runs with the target's real authority. Delegated mode admits only one reviewed, profile-bound, root-bound, command-digest-bound execution.
7. Delegated grants are immutable, expiring, one use, connection bound, thread bound, sequence bound, and consumed before command execution.
8. Selector expansion, redirect, fork, recursion, execute, function, schedule, alias, macro, bundle, wrapper, command block, external provider, asynchronous capture, and cross-thread reuse are rejected.
9. Target reconnect, permission refresh, policy reload, profile change, command-tree change, confirmation expiry, success, denial, and exception invalidate or clean temporary authority.
10. No delegated operation changes operator state, provider data, group membership, persistent player data, or the target's permanent command tree.
11. Admission, confirmation, dispatch, result, and cleanup produce structured audit events with separate issuer and effective actor identity, stable correlation, provenance, revision state, and no raw command body.
12. `/run` uses a real server source only for allowlisted roots after exact root authorization and confirmation.
13. `/silent` suppresses only the selected feedback surface. Security audit and authorized observation remain active. Unsuppressible output requires its exact capability.
14. Permission refresh stops pending and queued work. Broad wildcard diagnostics are visible without interpreting an exact grant as a wildcard.
15. Storage corruption, failed writes, incomplete jobs, and restart recovery fail closed without accepting partial state.
16. Queue, schedule, observation, profile, confirmation, and audit work remain inside configured hard bounds and generated performance budgets.

All Phase 11 acceptance rows are satisfied by the current automated and runtime matrix.
