# Phase 11 Verification

## Scope

This record covers custom aliases, bundles, command profiles, fake identity presentation, sudo policy, `/run`, and `/silent`.

Phase 11 has automated implementation coverage. Authenticated multiplayer presentation, live LuckPerms hierarchy changes, mixed-client GUI presentation, external command ownership changes, deliberate storage failure, dirty shutdown, and profiler observation remain release gates.

## Automated verification

Environment:

- Minecraft 1.21.1.
- NeoForge 21.1.233.
- Java 21.
- Linux dedicated server.

Commands:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew test
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew build
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew runServer
```

Results:

- Unit tests passed.
- Full build passed.
- The dedicated server reached the ready state twice against the same world and stopped cleanly both times.
- Built JAR SHA-256 was `07cf0f699971d0aff79ff8ef6af4bdb63c02011cfdfa866f6706c7cb0f334b25`.

Focused regression coverage verifies:

1. Alias draft validation, publication, root collision rejection, persistence, and restart activation.
2. Command-profile actor, targeted-actor, and server context rules.
3. Server profiles publish disabled.
4. Targeted profiles require a server-bound `{target}` placeholder.
5. Profile rendering requires the exact typed placeholder set.
6. Referenced profile deletion is denied.
7. Bundle publication and frozen-cohort admission.
8. A paced multi-target step resumes without executing a successful target twice.
9. Sudo consent defaults to denied.
10. Sudo locks, bypass decisions, optimistic revisions, and persistence.
11. Duplicate legacy and Phase 11 sudo permission registration is rejected by startup and no longer occurs.
12. Full NeoForge mod initialization with the Phase 11 command catalog and permission manifest.

## Manual release matrix

Run these cases with an owner, a staff player, an ordinary player, and a compatible enhanced client. Repeat the command-fallback cases with a vanilla client.

| Case | Required result | Status |
|---|---|---|
| Published alias restart | The same published alias appears after a clean restart and retains its canonical action permission | Pending |
| Alias collision | A preexisting external root follows its declared collision policy and never silently changes owner | Pending |
| Alias revocation | Disabling an alias removes execution and suggestions after command tree refresh | Pending |
| Bundle pacing | A large approved cohort progresses within its action-per-tick budget without duplicate mutation | Pending |
| Bundle cancellation | Issuer cancellation stops future steps and records the terminal job state | Pending |
| Bundle recovery | A queued job restored after restart remains in recovery until explicitly resumed | Pending |
| Bundle revalidation | Permission, hierarchy, exemption, feature, target, or profile revision loss blocks the next step | Pending |
| Fake identity | Fake join, leave, and message output is visibly unsigned system presentation | Pending |
| Fake identity visibility | Vanished identities are never resolved for an unauthorized issuer or audience | Pending |
| Fake schedule | A scene fires once at the saved time and survives a clean restart | Pending |
| Sudo consent | An ordinary target is denied until consent is enabled | Pending |
| Sudo lock | A staff lock wins unless the exact lock bypass is granted | Pending |
| Sudo hierarchy | Equal or higher targets, exempt targets, and vanished targets fail closed | Pending |
| Sudo chat | Output is unsigned presentation and never appears as signed player chat | Pending |
| Run source | An allowlisted root runs with a real server source only after confirmation | Pending |
| Run root permissions | A root-specific permission is required unless the exact any-root permission is granted | Pending |
| Silent actor | Only source feedback is suppressed. Command spy, file logging, and security audit remain visible | Pending |
| Silent server | Independent-output commands warn and require the exact unsuppressible-output capability | Pending |
| Wrapper recursion | Alias, bundle, profile, schedule, sudo, run, and silent wrapper indirection is rejected | Pending |
| LuckPerms refresh | Permission loss invalidates pending confirmation and stops queued execution | Pending |
| Mixed client | Enhanced clients use vanilla-style controls while vanilla clients retain every command fallback | Pending |
| Storage failure | Alias, bundle, profile, fake, and sudo repositories enter recovery and reject writes | Pending |
| Dirty shutdown | No partially written managed document is accepted after forced termination | Pending |
| Performance | Queue, fake schedule, and command observation work stays within configured tick and memory bounds | Pending |

Phase 11 is not approved for a public release until every applicable pending row has a recorded build, configuration, player roles, result, and log reference.
