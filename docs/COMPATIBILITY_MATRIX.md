# Compatibility Matrix

## Platform

| Component | Supported value | Required | Current evidence |
| --- | --- | --- | --- |
| Minecraft | `1.21.1` | yes | Build and run configurations are pinned |
| NeoForge | `21.1.235` | yes | ModDevGradle compile, 520 unit tests, 41 GameTests, dedicated server, headless client, and packaged metadata pass |
| Java | `21` | yes | Toolchain is pinned and current verification uses Java `21.0.11` |
| Gradle | checked in wrapper, Gradle `8.8` | yes | Build uses `./gradlew` or `gradlew.bat` |
| Loader | NeoForge only | yes | Fabric and legacy Forge are not supported |

## Client connection modes

| Server state | Client state | Expected behavior | Current evidence |
| --- | --- | --- | --- |
| Enhanced GUI disabled | Vanilla client | Join and use commands | Dedicated startup and fallback client coverage pass |
| Enhanced GUI disabled | SEF client | Join and use commands without enhanced payloads | Protocol-disabled and client-state coverage pass |
| Enhanced GUI enabled | Vanilla client | Join and receive command fallback only | No-SEF fallback client joined and remained connected |
| Enhanced GUI enabled | Matching SEF client | Negotiate supported protocol features | Enhanced client negotiated, joined, and remained connected |
| Enhanced GUI enabled | Older or incompatible SEF client | Join without unsupported feature projection | Major mismatch and minor feature-mask tests pass |
| Enhanced GUI enabled | Client loses session or changes server | Clear server owned sessions, projections, drafts, and caches | Disconnect, reconnect, server-switch, and stale-session tests pass |

The universal JAR does not make the client mod mandatory. Common initialization must remain safe on a dedicated server.

## Optional integrations

| Integration | Declared compatibility | Required at runtime | Absent behavior | Current release state |
| --- | --- | --- | --- | --- |
| LuckPerms API | `5.4` | no | Internal finite permission and quota fallbacks remain authoritative | Direct grant, wildcard, explicit deny, bridge outage, mutation invalidation, malformed metadata, removal, and finite fallback tests pass. The recorded LuckPerms NeoForge `5.4.140` login failure was on NeoForge `21.1.233`; a current `21.1.235` player-provider matrix remains open. |
| FTB Essentials | NeoForge `1.21.1`, CurseForge file `7608733` | no | SEF native ownership or documented fallback remains active | Present and absent startup, ownership selection, mutation boundary, removal, and failure coverage pass |
| Curios | NeoForge `1.21.1`, CurseForge file `6529130` | no | Optional inventory slots are omitted without startup failure | Present and absent startup, slot discovery, inventory authorization, removal, and failure coverage pass |

## Registry and content compatibility

Typed item, block, enchantment, effect, entity, dimension, recipe, component, and resource location fields must accept permitted registered content outside the `minecraft` namespace. Unknown or removed registry entries fail validation without mutating state.

Fancy Tags and disguise content use bounded negotiated payloads. Missing client resources, incompatible protocol versions, mod removal, and absent optional adapters must degrade to safe command or vanilla presentation.

## Storage compatibility

Versioned repositories use UUID ownership and retained recovery data. An unsupported newer schema, corrupt document, failed migration, or missing provider must not be interpreted as an empty successful state.

Phase 001 storage hardening applies bounded no follow reads to content addressed Fancy Tags objects and validates audit, module, history, backup, write, and recovery directory components without following symbolic links. Audit appends use a platform native descriptor provider. Linux and macOS use anchored `openat` traversal with `O_NOFOLLOW`, descriptor `fstat` identity and link checks, and bounded native writes. Windows uses `CreateFile` with reparse point rejection, delete sharing disabled while the handle is open, and `GetFileInformationByHandleEx` identity and link checks. The JNA API is compile only and is supplied by the pinned NeoForge runtime, so the mod does not embed a second native runtime. Unsafe parents, active audit links, non regular targets, and oversized existing objects fail closed before external state is read or written. Linux evidence is passing; macOS and Windows runtime evidence remain required before the compatibility row can close.

Dependency closure is currently blocked. A development only resolution comparison reached Netty `4.1.136.Final`, Log4j `2.25.5`, Commons Lang `3.18.0`, and Plexus Utils `3.6.1`, but that override was removed because the universal JAR does not embed or replace the libraries supplied by NeoForge `21.1.235`. The installed runtime therefore remains subject to the platform versions until an owner approved platform update or explicitly reviewed runtime packaging strategy is available. Optional integrations remain compile only.

The current compatibility gates and open integration findings are tracked in [the acceptance ledger](SEF2_ACCEPTANCE.md). Final compatibility acceptance is incomplete.
