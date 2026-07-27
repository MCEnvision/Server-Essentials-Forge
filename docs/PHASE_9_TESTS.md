# Phase 9 Verification Record

Final status, 2026-07-27: Complete. This file preserves earlier verification history. Pending or blocked labels below describe the earlier run and are superseded by the final matrix in `SEF2_ACCEPTANCE.md`.

This record covers the optional enhanced client protocol and GUI pilot on NeoForge 1.21.1.

## Environment

| Field | Value |
|---|---|
| Date | 2026-07-26 |
| Branch | `envy/sef2_complete` |
| Java | OpenJDK 21.0.11 |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.233 |
| Display harness | Xvfb, 1280 by 720 |
| Enhanced client game directory | Isolated temporary directory |
| Dedicated server directory | Repository `run` directory |

| Phase 9 implementation commit | `9ef8c8f` |
| Phase 9 artifact SHA-256 | `62b3d14fccc1486a4867a50d4dedd82ec88f7ff2f5c5095cfc6b971170bd9433` |

## Automated verification

| Check | Result | Evidence |
|---|---|---|
| Unit tests | Passed | 255 tests, no failures or errors |
| GUI protocol tests | Passed | Version, handshake, feature, replay, stale state, bounds, component size, and PNG header tests |
| Panel tests | Passed | Forged controls, stale revisions, target revisions, pagination, and descriptor contracts |
| GameTests | Passed | All 11 required GameTests |
| Build | Passed before the final documentation update | Repeated after the final source and documentation update |
| Dedicated server with GUI disabled | Passed | Reached `Done`, protocol disabled, zero configuration drift, and no kernel errors |
| Dedicated server with GUI enabled | Passed | Reached `Done`, protocol active, and no client classloading failure |
| Headless enhanced client | Passed | Reached the title and accessibility flow and rendered through Xvfb |
| Enhanced multiplayer session | Passed | Client negotiated 10 features and joined the GUI enabled server |
| Non-SEF multiplayer session | Passed | `runFallbackClient` excluded the SEF source output, joined the GUI enabled server, received the optional reminder, and remained connected |
| Non-SEF session isolation | Passed | `/sef doctor` reported zero active and zero pending enhanced sessions while the fallback client was online |

## Live GUI review

The enhanced client joined a GUI enabled server and opened the dashboard through the permission filtered pause screen entry. Review covered:

- The pause screen button at the normal GUI scale.
- The dashboard at 854 by 480.
- Live resize to 1200 by 600.
- Search, refresh, two column entries, item icons, pagination controls, status text, and close navigation.
- Home, warp, teleport request, help, and staff overview links.
- Session loss through disconnect.

Captured review images are `/tmp/sef-phase9-pause.png`, `/tmp/sef-phase9-dashboard.png`, and `/tmp/sef-phase9-dashboard-wide.png`. They are local verification evidence and are intentionally not repository artifacts.

## Security and protocol coverage

Automated tests cover:

- Optional configuration handshake acceptance and rejection.
- Major protocol incompatibility fallback.
- Negotiation nonce and identifier mismatch.
- Unsupported feature removal.
- Session identifier and sequence validation.
- Replay and out of order rejection.
- Permission and feature loss.
- Delayed HUD and identity payload rejection after permission loss.
- Per player identity revision ordering.
- Hidden player filtering and UUID bound target revisions.
- Forged panel ids, control ids, entry ids, and revisions.
- Panel expiry and action revalidation.
- Maximum panel, HUD, identity, query, string, component, and image bounds.
- PNG signature, IHDR ordering, dimensions, pixel count, truncation, and oversized image rejection before native decode.
- Fancy Tags content hash mismatch and reconnect cache behavior.

## Dedicated server classloading

The GUI enabled dedicated server reached `Done` while all screen, renderer, keybind, texture, and client cache classes remained isolated in `com.enviouse.sef.gui.client`. The final JAR inspection repeats the package and constant pool checks.

## Optional integration finding

LuckPerms NeoForge `5.4.140` starts on NeoForge `21.1.233`, but an actual player login fails inside LuckPerms with:

```text
java.lang.IllegalStateException: Capability has not been initialised
```

The exception originates in `UserCapabilityImpl.getQueryOptionsCache` during LuckPerms `NeoForgeConnectionListener.onPlayerLoggedIn`. The same server accepts the enhanced client when the affected external build is absent. SEF does not catch or suppress another mod's login event exception. Operators must use a LuckPerms build verified against their exact NeoForge version. This external compatibility failure is tracked in the integration matrix and does not weaken SEF's fail closed permission behavior.

## Phase 14 release repetition

Phase 14 repeats the full mixed client matrix using release artifacts:

- The already passing client with no SEF development mod loaded through `runFallbackClient`.
- A compatible enhanced client.
- A deliberately incompatible GUI protocol fixture.
- GUI disabled and GUI enabled server configurations.
- Multiple GUI scales, narrow and wide aspect ratios, narration, and long translated strings.

Phase 9 is accepted. The deliberately incompatible protocol path is covered by the configuration negotiation and session tests. Phase 14 repeats it with a release fixture and repeats the complete connection and visual matrix against the final artifact.
