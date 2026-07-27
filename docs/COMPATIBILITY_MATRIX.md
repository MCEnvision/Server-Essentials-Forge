# Compatibility Matrix

## Platform

| Component | Supported value | Required | Current evidence |
| --- | --- | --- | --- |
| Minecraft | `1.21.1` | yes | Build and run configurations are pinned |
| NeoForge | `21.1.233` | yes | ModDevGradle compile, unit test, GameTest, client, and server configurations are pinned |
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
| LuckPerms API | `5.4` | no | Internal finite permission and quota fallbacks remain authoritative | Present and absent startup, mutation invalidation, malformed metadata, outage, removal, and finite fallback coverage pass |
| FTB Essentials | NeoForge `1.21.1`, CurseForge file `7608733` | no | SEF native ownership or documented fallback remains active | Present and absent startup, ownership selection, mutation boundary, removal, and failure coverage pass |
| Curios | NeoForge `1.21.1`, CurseForge file `6529130` | no | Optional inventory slots are omitted without startup failure | Present and absent startup, slot discovery, inventory authorization, removal, and failure coverage pass |

## Registry and content compatibility

Typed item, block, enchantment, effect, entity, dimension, recipe, component, and resource location fields must accept permitted registered content outside the `minecraft` namespace. Unknown or removed registry entries fail validation without mutating state.

Fancy Tags and disguise content use bounded negotiated payloads. Missing client resources, incompatible protocol versions, mod removal, and absent optional adapters must degrade to safe command or vanilla presentation.

## Storage compatibility

Versioned repositories use UUID ownership and retained recovery data. An unsupported newer schema, corrupt document, failed migration, or missing provider must not be interpreted as an empty successful state.

Every compatibility gate in [the acceptance ledger](SEF2_ACCEPTANCE.md) is complete on the current phase branch.
