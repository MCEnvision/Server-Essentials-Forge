# Phase 000 external fixture result

## Result

Retry `fixture-20260925-d` reached readiness on both Forge backends and the pinned Velocity proxy. A real Forge client launched from the isolated laptop instance, connected through the private proxy, completed the Forge handshake, joined backend A, delivered signed chat, and submitted a command with arguments. An explicit `/server backend-b` request reached backend B, but the Forge connection reset during the switch and Velocity returned `Please reconnect`. Backend B did not admit the player to gameplay, and no return transfer was possible. Phase 000 therefore remains open at `SEF-AC-010`.

## candidate revalidation

| Candidate | SHA256 | Result |
| --- | --- | --- |
| Velocity 4.2.0 build 30 | `35a5596a5468a035d8a32c8de5ebb0dc6b8d8f0cc3ff5169d514aca762af8aa8` | Match |
| Ambassador 1.4.5 | `7c57a649c3948672cbef35a3baa8f73b046ac509ea8ffa6ee3008890d36f2c17` | Match |
| ProxyCompatibleForge 1.3.1 | `ea541aff6276970d98506965c8e71bd4ad7329452f631b2f45eb08edea425271` | Match |
| MixinExtras Forge 0.5.3 | `89d60f6bf1f29664319acfa80e777abc03fde674370af52e94a9a2e452b98833` | Match |

The pinned jar metadata was inspected for license and notice entries. This is notice and byte evidence only, not a compatibility or security approval. Public advisory lookups previously returned no advisory records for Ambassador or ProxyCompatibleForge, so no advisory clean claim is made.

## boot receipt

| field | value |
| --- | --- |
| host | `node-1` |
| runtime | Java 17.0.19, Forge 47.3.12, Minecraft 1.20.1 |
| target build | `sef-1.20.1-1.1.jar`, Gradle `jar`, successful |
| target SHA256 | `b63902d0d414c8c596bff5ceea785fc61e98e8b8daa381f8c66bd82a66480370` |
| Forge installer SHA256 | `6117bf266bf8395cc216a9f9438c26f15cbede3b9b416857bbf70e07c15840f7` |
| MixinExtras Forge 0.5.3 SHA256 | `89d60f6bf1f29664319acfa80e777abc03fde674370af52e94a9a2e452b98833` |
| ProxyCompatibleForge SHA256 | `ea541aff6276970d98506965c8e71bd4ad7329452f631b2f45eb08edea425271` |
| backend A | `backend-a`, `127.0.0.1:25566`, `Done`, SEF initialized, log line 50 |
| backend B | `backend-b`, `127.0.0.1:25567`, `Done`, SEF initialized, log line 51 |
| EULA | `eula=true` read back before launch |

Both servers initialized MixinExtras, ProxyCompatibleForge, and SEF. The Forge runtime requires the Forge MixinExtras artifact. The plain common artifact is not sufficient for this dedicated server distribution.

## proxy receipt

| field | value |
| --- | --- |
| runtime | Java 25.0.3, Velocity 4.2.0 build 30 |
| listener | `100.76.164.109:25565`, private Tailscale interface |
| plugin | Ambassador 1.4.5, loaded successfully |
| plugin SHA256 | `7c57a649c3948672cbef35a3baa8f73b046ac509ea8ffa6ee3008890d36f2c17` |
| backend forwarding | modern forwarding secret configured in both backend PCF files |
| startup | `Done (0.64s)!`, proxy log line 15 |

## real client receipt

The authorized `envision` laptop was reachable over Tailscale and reported an NVIDIA GeForce RTX 5090 Laptop GPU. The exact isolated instance was `/home/envy/.local/share/PrismLauncher/instances/sef-phase-000-client-20250925-d`. Its Forge client process was PID `3909330`, with window class `Minecraft* 1.20.1` and title `Minecraft* Forge 1.20.1 - Multiplayer (3rd-party Server)`. The exact PipeWire stream was node `148`, bound to process PID `3909330`, and was set to `Volume: 0.00 [MUTED]`. The personal FutureShops instance and its streams were not touched.

The client connected to `100.76.164.109:25565`. Backend A recorded the forwarded UUID `ee31040d-a3c6-4612-b0c2-8e1c60aa7eec` and admitted `EnVyOnMyMind`, proving a legitimate forwarded login. The client completed the modded handshake and entered the world.

## signed chat and command argument receipt

Backend A recorded the real client messages:

* `backend-a/logs/latest.log:67`, signed chat text `phase 000 signed chat`
* `backend-a/logs/latest.log:68`, command argument text `sethome phasehome`

These lines prove client-originated input reached the Forge backend. They do not prove the complete future SEF command catalog or the later mute enforcement requirement.

## switch receipt and blocker

The client submitted `/server backend-b` through the focused game window. Velocity recorded `backend-b` connected at `proxy/logs/latest.log:22`. Backend B received the forwarded UUID at `backend-b/logs/latest.log:63`, but the client connection reset during the Forge handshake. Velocity recorded `Please reconnect` and disconnected both backend B and backend A at `proxy/logs/latest.log:23-25`; backend B recorded the disconnect at line 64 and never logged a gameplay join. The client displayed `Connection Lost`, `Please reconnect`.

This is a real switch attempt and a useful failure receipt. A successful A to B gameplay transfer, a B to A transfer, and a compatible reconnect path remain unverified. The failure may be caused by the current external Ambassador and ProxyCompatibleForge combination, but no root cause is inferred from this fixture.

## direct forwarding negative

The earlier bounded protocol probe sent a Forge 1.20.1 login without the Velocity forwarding payload to backend A. The server exposed the `velocity:player_info` login plugin request and then disconnected the profile with a null UUID before gameplay admission. This remains negative forwarding evidence only. A fresh post-client negative control was not run after the switch failure.

## userdev failed attempt

The earlier userdev launch stopped during Mixin application on a Java 25 class file. The fixture did not disable the failing mixin, remove the target mod, substitute a Forge version, or add a custom handshake, because any such change would no longer test the pinned target baseline.

## consequence

Phase 000 cannot close `SEF-AC-010`. Production server and proxy startup, private routing, legitimate login, signed chat delivery, and a real command argument are proven. Backend switching fails at the Forge connection reset boundary, B to A and the fresh post negative control remain unverified, and no SEF bridge compatibility, shared storage behavior, or production readiness is inferred.
