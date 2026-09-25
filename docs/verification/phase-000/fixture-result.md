# Phase 000 external fixture result

## Result

Retry `fixture-20260925-e` reached readiness on both Forge backends and the pinned Velocity proxy with the PCF `MODERN_DEFAULT` forwarding override enabled on both backends. A real Forge client connected through the private proxy, completed the Forge handshake, joined backend A, delivered signed chat, and submitted client input. An explicit `/server backend-b` request reached backend B, but the Forge connection reset during the switch and Velocity returned `Please reconnect`. Backend B did not admit the player to gameplay. Phase 000 remains open at `SEF-AC-010`.

The later `fixture-20260925-g` retry was a bounded reconnect follow up. It reached readiness and admitted the client to backend A, but the client sound engine failed before an owned playback stream existed. The client was stopped at that safety boundary, so this retry did not attempt a switch and does not change the open gate.

## candidate revalidation

| Candidate | SHA256 | Result |
| --- | --- | --- |
| Forge 1.20.1 installer 47.3.12 | `6117bf266bf8395cc216a9f9438c26f15cbede3b9b416857bbf70e07c15840f7` | Match |
| SEF target `sef-1.20.1-1.1.jar` | `e60e21350a8a9b0db9486c00aa9c76263c30e3dfec387dd777770dd36b070eb3` | Match |
| SEF target `sef-1.20.1-1.1.jar`, follow up build | `df5b59cfbb2435b60aaaaf55fbb9882ae4dfc03205ae3fc3cdf357e14b7827ad` | Built and launched in `fixture-20260925-g` |
| Velocity 4.2.0 build 30 | `35a5596a5468a035d8a32c8de5ebb0dc6b8d8f0cc3ff5169d514aca762af8aa8` | Match |
| Ambassador 1.4.5 | `7c57a649c3948672cbef35a3baa8f73b046ac509ea8ffa6ee3008890d36f2c17` | Match |
| ProxyCompatibleForge 1.3.1 | `ea541aff6276970d98506965c8e71bd4ad7329452f631b2f45eb08edea425271` | Match |
| MixinExtras Forge 0.5.3 | `89d60f6bf1f29664319acfa80e777abc03fde674370af52e94a9a2e452b98833` | Match |

## boot receipt

| field | value |
| --- | --- |
| host | `node-1` |
| runtime | Java 17.0.19, Forge 47.3.12, Minecraft 1.20.1 |
| backend A | `127.0.0.1:25566`, `Done` at `backend-a/logs/latest.log:100`, SEF initialized at lines 102 through 111 |
| backend B | `127.0.0.1:25567`, `Done` at `backend-b/logs/latest.log:99`, SEF initialized at lines 101 through 110 |
| EULA | `eula=true` read back before launch |
| PCF override | `debug.enabled = true` and `advanced.modernForwardingVersion = "MODERN_DEFAULT"` in both generated PCF configs |

Both servers initialized MixinExtras, ProxyCompatibleForge, and SEF. The production server jars were reobfuscated and launched without changing source, disabling mixins, or substituting a Forge version.

## proxy receipt

| field | value |
| --- | --- |
| runtime | Java 25.0.3, Velocity 4.2.0 build 30 |
| listener | `100.76.164.109:25565`, private Tailscale interface |
| plugin | Ambassador 1.4.5, loaded successfully |
| startup | `Done (0.88s)!`, `proxy/logs/latest.log:15` |
| legitimate login | player connected at line 16 and backend A connected at line 17 |
| switch attempt | backend B connected at line 18, then `Please reconnect` and both backend disconnects at lines 19 through 21 |

## real client receipt

The authorized `envision` laptop reported an NVIDIA GeForce RTX 5090 Laptop GPU and the private proxy was reachable. The disposable client instance was `/home/envy/.local/share/PrismLauncher/instances/sef-phase-000-client-20250925-e`. Its Forge client process was PID `4065055`, with window class `Minecraft* 1.20.1` and title `Minecraft* Forge 1.20.1 - Multiplayer (3rd-party Server)`. The exact PipeWire stream was node `151`, bound to process PID `4065055`, and was set to `Volume: 0.00 [MUTED]` before input. The personal FutureShops instance was preserved.

The client connected to `100.76.164.109:25565`. Backend A recorded the forwarded UUID `ee31040d-a3c6-4612-b0c2-8e1c60aa7eec` and admitted `EnVyOnMyMind` at `backend-a/logs/latest.log:132` through `134`, proving a legitimate forwarded login and gameplay admission.

## signed chat and command input receipt

Backend A recorded the focused client chat at `backend-a/logs/latest.log:135` as `[CHAT] 01:23 | EnVyOnMyMind: phaseechatexplicit`. The client also produced the ordinary server command response for the attempted slash input in its isolated `latest.log`. This proves real client input reached the Forge backend. It does not prove the complete future SEF command catalog or mute enforcement.

## switch receipt and blocker

The client submitted `/server backend-b` through the focused game window. Velocity recorded backend B connected, and backend B received the forwarded profile at `backend-b/logs/latest.log:130`, but the Forge connection reset before gameplay admission. Velocity returned `Please reconnect` and disconnected both backend B and backend A. Backend B logged only the disconnect and no gameplay join. The client displayed `Connection Lost` with `Please reconnect`.

The `MODERN_DEFAULT` retry did not change the switch result. A successful A to B gameplay transfer, a B to A transfer, and compatible reconnect behavior remain unverified. No root cause is inferred from this fixture.

## adapter source correlation

The pinned Ambassador 1.4.5 source explains the observed boundary. Its completed Forge connection phase sends a server redirect only when the client advertises `serverredirect` or `srvredirect:red`; otherwise it disconnects the player with the configured reset message. Its resettable path is enabled only when the client advertises `clientresetpacket`. See the pinned source commit `349ce41f5fba50653d0aec8ebb7d77f68a5d00e1` in `VelocityForgeClientConnectionPhase.java`, lines 126 through 141 and 203 through 208. The isolated client did not advertise either reset mod, so the observed `Please reconnect` result is consistent with the adapter's documented client reset boundary. This is a compatibility diagnosis, not a claim that the required no-client switching gate passes.

## direct forwarding negative

A fresh bounded protocol probe connected directly to backend A without the Velocity forwarding payload. Backend A exposed the `velocity:player_info` login request and rejected the profile with null UUID before gameplay admission. The decisive records are `backend-a/logs/latest.log:140-141`, which state `This server requires you to connect with Velocity`. This proves direct unauthenticated forwarding rejection. The probe did not use credentials or a production endpoint.

## post-negative legitimate control

After the direct negative, a fresh legitimate client relaunch was attempted from the same isolated instance. The Minecraft services endpoint returned transient `503 ServiceUnavailableError` and then an SSL trust failure, so no second legitimate login could be asserted. The earlier legitimate login occurred before the negative probe and is not reused as a post-negative control.

## userdev failed attempt

The earlier userdev launch stopped during Mixin application on a Java 25 class file. The fixture did not disable the failing mixin, remove the target mod, substitute a Forge version, or add a custom handshake, because such changes would no longer test the pinned target baseline.

## consequence

Phase 000 cannot close `SEF-AC-010`. Production backend and proxy startup, private routing, legitimate login, gameplay admission, signed chat delivery, client input, and direct forwarding rejection are proven. Backend switching fails at the Forge connection reset boundary, B to A and the fresh post-negative legitimate control remain unverified, and no SEF bridge compatibility, shared storage behavior, or production readiness is inferred.

## follow up reconnect retry

The `fixture-20260925-g` backends reached `Done` on Java 17 with Forge 47.3.12 and SEF initialized. The private Velocity proxy loaded Ambassador 1.4.5 and connected the disposable client to backend A. Backend A recorded two ordinary gameplay admissions during the launcher connection and retry at `backend-a/logs/latest.log:191-193` and `197-198`. Backend B recorded no player admission.

The disposable client log recorded `Error starting SoundSystem. Turning off sounds & music` at `minecraft/logs/latest.log:40`, and the exact PipeWire sink input query returned no client stream. The client was stopped before any further command input. Because the exact owned window to playback stream relationship could not be proven, the retry leaves the reconnect behavior unverified rather than treating a disconnected client as evidence of success.
