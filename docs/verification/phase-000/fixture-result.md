# Phase 000 external fixture result

## Result

Retry `fixture-20260925-e` reached readiness on both Forge backends and the pinned Velocity proxy with the PCF `MODERN_DEFAULT` forwarding override enabled on both backends. A real Forge client connected through the private proxy, completed the Forge handshake, joined backend A, delivered signed chat, and submitted client input. An explicit `/server backend-b` request reached backend B, but the Forge connection reset during the switch and Velocity returned `Please reconnect`. Backend B did not admit the player to gameplay. Phase 000 remains open at `SEF-AC-010`.

The later `fixture-20260925-g` retry was a bounded reconnect follow up. It reached readiness and admitted the client to backend A, but the client sound engine failed before an owned playback stream existed. The client was stopped at that safety boundary, so this retry did not attempt a switch and does not change the open gate.

The later `fixture-20260925-h` control reached backend A after the direct forwarding rejection and dispatched a normal SEF `/msg` command with a real client argument. It does not repair the missing correlation across the earlier positive and negative runs, and it does not prove a completed server switch. The fixture remains open at `SEF-AC-010`.

## candidate revalidation

| Candidate | SHA256 | Result |
| --- | --- | --- |
| Forge 1.20.1 installer 47.3.12 | `6117bf266bf8395cc216a9f9438c26f15cbede3b9b416857bbf70e07c15840f7` | Match |
| SEF target `sef-1.20.1-1.1.jar` | `e60e21350a8a9b0db9486c00aa9c76263c30e3dfec387dd777770dd36b070eb3` | Match |
| SEF target `sef-1.20.1-1.1.jar`, follow up build | `df5b59cfbb2435b60aaaaf55fbb9882ae4dfc03205ae3fc3cdf357e14b7827ad` | Built and launched in `fixture-20260925-g` |
| SEF target `sef-1.20.1-1.1.jar`, h build from `05fdad4` | `643cdd0933f7590c69344f2752c0fba6cebb16a93285edef4c83d45ad6bc7f0f` | Built and launched in `fixture-20260925-h` |
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

The `MODERN_DEFAULT` retry did not change the switch result. A successful A to B gameplay transfer, a B to A transfer, and compatible reconnect behavior remain unverified. No root cause is inferred from the runtime fixture alone.

## adapter source correlation

The pinned Ambassador 1.4.5 source explains the observed boundary. Its completed Forge connection phase sends a server redirect only when the client advertises `serverredirect` or `srvredirect:red` and has a virtual host; otherwise it disconnects the player with the configured reset message. Its resettable path is enabled only when the client advertises `clientresetpacket`. See the [pinned source](https://github.com/adde0109/Ambassador/blob/v1.4.5/src/main/java/org/adde0109/ambassador/forge/VelocityForgeClientConnectionPhase.java#L113-L128) and [reset capability check](https://github.com/adde0109/Ambassador/blob/v1.4.5/src/main/java/org/adde0109/ambassador/forge/VelocityForgeClientConnectionPhase.java#L183-L187). The isolated client advertised neither reset capability, so the observed `Please reconnect` result is consistent with the implementation.

The current [`non-api` README](https://github.com/adde0109/Ambassador/blob/non-api/README.md#L207-L218) says similar servers can switch without an extra client mod, while separately describing ServerRedirect and Client Reset Packet for switching. That wording does not establish an A to B and B to A transfer during one session for the exact pinned release and tested client. A manual reconnect remains a new login, not a completed switch under this phase's receipt contract.

This leaves a plan evidence discrepancy involving `SRC-108`, `DEC-007`, and `SEF-AC-010`. The phase prohibits adding a client dependency to make the pinned matrix pass. No reset mod was added to SEF or the fixture, and no acceptance criterion was changed. The discrepancy requires an explicit owner decision and authorized plan maintenance before selecting another client profile or adapter.

## direct forwarding negative

A fresh bounded protocol probe connected directly to backend A without the Velocity forwarding payload. Backend A exposed the `velocity:player_info` login request and rejected the profile with null UUID before gameplay admission. The decisive records are `backend-a/logs/latest.log:140-141`, which state `This server requires you to connect with Velocity`. This proves direct unauthenticated forwarding rejection. The probe did not use credentials or a production endpoint.

## post-negative legitimate control

After the direct negative, a fresh legitimate client relaunch in fixture e was attempted from the same isolated instance. The Minecraft services endpoint returned transient `503 ServiceUnavailableError` and then an SSL trust failure, so that run could not assert a second login. A later, separately registered fixture h then started clean backends and proxy with the same pinned candidate artifacts and PCF forwarding profile. Its fresh client login reached backend A with the expected UUID. This supplies a post-negative legitimate control across fixture runs, not a single correlated positive-negative sequence.

## userdev failed attempt

The earlier userdev launch stopped during Mixin application on a Java 25 class file. The fixture did not disable the failing mixin, remove the target mod, substitute a Forge version, or add a custom handshake, because such changes would no longer test the pinned target baseline.

## consequence

Phase 000 cannot close `SEF-AC-010`. Across the bounded runs, production backend and proxy startup, private routing, legitimate login, gameplay admission, signed chat delivery, client input, and direct forwarding rejection are proven. A later fresh legitimate control after the negative reached backend A and exercised the SEF `/msg` handler. Backend switching still resets the connection before destination gameplay admission; later manual reconnects admitted the same client to each backend but do not prove a completed A to B or B to A switch. The positive chat and switch attempts do not share one correlation UUID, and no SEF bridge compatibility, shared storage behavior, or production readiness is inferred.

## follow up reconnect retry

The `fixture-20260925-g` backends reached `Done` on Java 17 with Forge 47.3.12 and SEF initialized. The private Velocity proxy loaded Ambassador 1.4.5 and connected the disposable client to backend A. Backend A recorded two ordinary gameplay admissions during the launcher connection and retry at `backend-a/logs/latest.log:191-193` and `197-198`. Backend B recorded no player admission.

The disposable client log recorded `Error starting SoundSystem. Turning off sounds & music` at `minecraft/logs/latest.log:40`, and the exact PipeWire sink input query returned no client stream. The client was stopped before any further command input. Because the exact owned window to playback stream relationship could not be proven, the retry leaves the reconnect behavior unverified rather than treating a disconnected client as evidence of success.

## x11 fixture h command and fresh control

The later h retry used Forge installer 47.3.12, Velocity 4.2.0 build 30, Ambassador 1.4.5, ProxyCompatibleForge 1.3.1, MixinExtras Forge 0.5.3, and the target artifact built from commit `05fdad4`. Their SHA-256 values were rechecked from the registered disposable downloads: Forge `6117bf266bf8395cc216a9f9438c26f15cbede3b9b416857bbf70e07c15840f7`, Velocity `35a5596a5468a035d8a32c8de5ebb0dc6b8d8f0cc3ff5169d514aca762af8aa8`, Ambassador `7c57a649c3948672cbef35a3baa8f73b046ac509ea8ffa6ee3008890d36f2c17`, ProxyCompatibleForge `ea541aff6276970d98506965c8e71bd4ad7329452f631b2f45eb08edea425271`, MixinExtras `89d60f6bf1f29664319acfa80e777abc03fde674370af52e94a9a2e452b98833`, and SEF `643cdd0933f7590c69344f2752c0fba6cebb16a93285edef4c83d45ad6bc7f0f`. The SEF build jar and installed backend copies had the same digest.

Both no-GUI backends ran Minecraft 1.20.1, Forge 47.3.12, and Eclipse Adoptium Java 17.0.20.1. Backend A reached `Done` at `backend-a/logs/latest.log:108`; backend B reached `Done` at `backend-b/logs/latest.log:110`. Both logged SEF initialization immediately afterward. EULA acceptance was read back as `eula=true`. Both PCF configurations set `modernForwardingVersion = "MODERN_DEFAULT"`; each configuration digest was `7ad8bae9f491085770e4dae588c3873ebc90a9122913a0e8223dfd20bab311af`. Velocity started on Java 25.0.4.1 with the matching pinned proxy and Ambassador artifacts and reached `Done` at `proxy/logs/latest.log:10`. The non-secret `velocity.toml` digest was `67b35eb6ac3be7156a2d7636a68e18e03b2cf3efddaf12a4508eccd16cf2c2b1`.

The authorized `envision` laptop used the isolated h instance. Client PID `383976`, Prism PID `383206`, Hyprland address `0x55d1ef473c80`, X11 window `14680071`, class `Minecraft* 1.20.1`, and title `Minecraft* Forge 1.20.1 - Multiplayer (3rd-party Server)` were correlated. The client log reported `NVIDIA GeForce RTX 5090 Laptop GPU` at `minecraft/logs/latest.log:7`. PipeWire sink input `726` was bound to PID `383976`, read back muted, and at zero volume before client input. The exact private listener address is intentionally omitted.

After the earlier direct-forwarding negative, the fresh h client connected through the proxy at `proxy/logs/latest.log:11-12`. Backend A recorded the forwarded UUID `ee31040d-a3c6-4612-b0c2-8e1c60aa7eec` at `backend-a/logs/latest.log:139`, gameplay admission at lines 140 and 142, and the real command handler at line 143: `[MSG] EnVyOnMyMind -> EnVyOnMyMind: phase000_argument_probe`. The input was `/msg EnVyOnMyMind phase000_argument_probe`. This proves the selected existing command and its arguments reached the Forge handler from an actual player session. It does not prove the complete catalog, rich presentation, mute enforcement, or a signed chat receipt within this h control. The fixture register `h` is not a generated correlation UUID, so the required correlated evidence remains incomplete.

Earlier h switch attempts are preserved in `proxy/logs/2026-09-25-1.log.gz`. The A to B request disconnected the client with `Please reconnect` at line 40; a manual client reconnect later reached backend B at `backend-b/logs/2026-09-25-1.log.gz:142`. The B to A request disconnected with the same reset result at proxy line 46; a later manual reconnect reached backend A at `backend-a/logs/2026-09-25-1.log.gz:146`. These are separate login admissions, not successful in-session switches. No seamless transfer or matching signed-chat/switch correlation was asserted.

The client was closed after evidence collection. Its window-close and SIGINT attempts did not stop the owned process, so SIGTERM was sent to the exact client and launcher PIDs. The process and its playback stream were then verified absent. Both backend worlds were saved, both backends stopped cleanly, and the proxy was interrupted after client exit. Exact test-root cleanup is recorded separately.
