# SEF 2 Acceptance Ledger

This ledger measures completion against `sef2.md`. A scope is complete only when its required behavior, exit criteria, and current verification evidence pass. Source presence, command registration, schemas, generic editors, and compilation do not count as complete behavior by themselves.

## Status meanings

| Status | Meaning |
| --- | --- |
| Incomplete | At least one required behavior or verification gate is missing. |
| In progress | The scope is actively being completed, but its exit criteria do not all pass. |
| Complete | Every requirement and exit criterion passes on the current source revision. |

## Current audit phase status

The current Phase 003 candidate is product commit `31388263b1ef95dd27ef1362bb603d8eef73c552` on `envy/phase-003-commands`. The shared execution gate now covers the confirmed legacy moderation, privacy, identity, announcement, inventory, vanish, banned item, and module configuration adapters, while custom or asynchronous adapters revalidate server control and audit availability. The clean check and build pass with 583 tests, zero failures, zero errors, and zero skips. The current adapter rerun passes all 45 required GameTests, including 302 server-control metadata routes and the non-server-control console rejection routes through the shared execution and audit boundary. The new kernel audit regressions prove successful and failed execution persistence with bounded metadata, actor and target correlation, callback detail redaction, and bounded rejection events for player-only console invocations. The candidate JAR is `sef-2.0.0.jar`, SHA-256 `a5607653488aef923722406944fbe915816a98927373f69aac6494056f4fb494`, and SHA-512 `00f89016130a396e67903de42bfed05e8f448f3e4a9a01a6695963c1728586c4009b454bc18ec405799c745bca0811edce2d36f8c4fa2efb911f6660d571fa9f`, bound by the current build. The current universal matrix contains 724 rows, 708 executable rows open, 16 unavailable rows passed, and `complete=false`. Full action effect, persistence, audit, and route joins remain open. The external platform supplied dependency alert ownership and advisory closure record remains open. This is a current Phase 003 progress record, not final acceptance.

The legacy phase ledger below remains useful for SEF 2 feature acceptance, but it is not the authoritative status for the current security audit sequence. Phase 001 security, privacy, and supply chain closure is being verified on `envy/phase-001-security`; each exact candidate revision, artifact digest, and completed run is recorded in the matching restricted evidence packet. Confirmed repairs include admin chat logging, bounded Fancy Tags reads, audit symlink handling, configuration parent path handling, and the cross platform native audit descriptor provider. The current candidate is required to pass 574 unit tests, 41 required GameTests, the Java 21 build and fallback compilation, the Linux, macOS, and Windows native writer probes, and the disposable dedicated server smoke. The current pull request matrix is the required evidence gate for candidate build, Linux runtime, macOS runtime, Windows runtime, artifact inspection, and sanitized evidence upload. The Minecraft Java client fixture on Ubuntu is the sole client evidence surface and must pass from the same candidate artifact. The current remaining Phase 001 record is `EXT-002`: all 26 upstream platform supplied alerts have separate candidate, packaged artifact, installed runtime, affected API, advisory, provenance, and compatible remediation dispositions in the restricted evidence packet, with no direct mod owned exposure found. Phase 001 remains open until that external acceptance record and pull request integration gate are closed. Later persistence, universal GUI, UI polish, lifecycle, clean checkout, and final documentation matrices remain open.

The owner desktop probe reached the real Minecraft 1.21.1 LWJGL client, but its installed NeoForge 21.1.248 runtime rejected the candidate's exact NeoForge 21.1.235 requirement. The temporary candidate and logs were removed. The result confirms the desktop display path and records the exact `EXT-001` runtime fixture mismatch without weakening Linux, macOS, or Windows support.

A second disposable owner desktop probe cloned the Prism instance, downloaded exact NeoForge 21.1.235, and launched the historical candidate artifact with SHA-256 `da76826dd757e9f52b1cfc61fb9902812a77c328321edae14428f329c2a16eb0`. The graphical Linux client reached Minecraft 1.21.1, LWJGL 3.3.3, SEF initialization, configuration loading, and the GUI atlas. The clone, candidate, logs, and process were removed. This remains historical Linux client fixture evidence. The current contract uses the Minecraft Java client fixture as the sole client evidence surface and does not require separate macOS or Windows client captures. `EXT-001` remains conditional for a changed or exercised non client host specific path.

Hosted matrix run `33732626666` reran the complete candidate audit from historical commit `2cb01b8ecf91eaa90d0f0284e6f1bc2b52058323`. Linux, macOS, and Windows passed the native writer, opened object identity, failure preservation, dedicated server, dependency, packaged artifact, and sanitized evidence gates. Linux verified packaged client startup and the GUI atlas. macOS and Windows recorded explicit hosted display blockers and remained supported targets. The Windows packaged client reader now waits for transient `latest.log` locks to clear after process tree shutdown. This run is historical; the current candidate and current hosted runtime result are recorded above.

A prior merge-revision matrix run `33738411285` tested synthetic merge commit `646234240649a3552c50970a32b9beae9a0313a3` for branch head `830fb9af4cbe79694314cfebd2b6863311a1f55c`. Linux, macOS, and Windows all passed native writer, opened-object identity, object-swap, failure-preservation, rotation, restart, dedicated-server, dependency, packaged-artifact, and sanitized-evidence gates at that revision. The server process had to remain alive after `Done` until the workflow sent `stop` and then exit with code zero. Ubuntu passed packaged client startup and a five-second liveness gate after the GUI atlas marker. macOS and Windows recorded only their known hosted graphics signatures, `NSGL` and `WGL`, and remain supported targets. The independent review reported no actionable findings. This run is historical and is retained for comparison only.

## Phase ledger

| Scope | Requirement bullets | Status | Current evidence |
| --- | ---: | --- | --- |
| [Phase 0](../sef2.md#phase-0-plan-inventory-and-governance) | 17 | Complete | The roadmap, pinned environment, clean-room rules, ownership inventory, 153-row EssentialsX core matrix, 4-row module matrix, risk register, data paths, conflict policy, documentation, and licensing review are present and internally consistent. |
| [Phase 1](../sef2.md#phase-1-stabilization-and-security-repair) | 17 | Complete | Startup, shutdown, optional-integration isolation, redaction, persistence failure, bounded worker, dedicated-server, and client-classloading gates pass. |
| [Phase 2](../sef2.md#phase-2-shared-command-and-policy-kernel) | 35 | Complete | The sealed catalog, canonical execution pipeline, permission decisions, hierarchy, quotas, cooldowns, confirmations, costs, observation, diagnostics, refresh invalidation, and route-equivalence tests pass. |
| [Phase 3](../sef2.md#phase-3-storage-foundation-and-player-profile) | 13 | Complete | Atomic persistence, version envelopes, backup, migration, corruption quarantine, recovery mode, profile import, location history, cooldown persistence, concurrent writes, and bounded shutdown pass. |
| [Phase 4](../sef2.md#phase-4-homes-teleports-spawn-warps-and-rtp) | 22 | Complete | Home, request, history, spawn, warp, player-warp, RTP, safety, world-border, hazard, missing-dimension, race, quota, and recovery behavior pass unit and GameTest coverage. |
| [Phase 5](../sef2.md#phase-5-social-identity-mail-and-connection-messages) | 23 | Complete | Private messaging, observation privacy, mail, identity, nickname projection, connection messages, reminders, retention, provider failure, and vanished-player suppression pass. |
| [Phase 6](../sef2.md#phase-6-moderation-and-protection) | 35 | Complete | Moderation, address policy, command observation, absolute redaction, file ownership, symlink rejection, queue failure, rotation, retention, shutdown recovery, controls, and protection enforcement pass. |
| [Phase 7](../sef2.md#phase-7-inventory-workstations-kits-and-player-utilities) | 24 | In progress | Automated inventory authorization, revocation, and zero-saturation `/feed` tests pass. The revised target-above-viewer InvSee layout and Curios paging still require the multiplayer matrix in `test.md`. |
| [Phase 8](../sef2.md#phase-8-native-economy-and-signs) | 14 | Complete | Exact money arithmetic, provider ownership, idempotent transactions, cost recovery, import once, inventory compensation, all sign types, persistence failure, and bounded ranking pass. |
| [Phase 9](../sef2.md#phase-9-client-protocol-and-gui-pilot) | 62 | In progress | Protocol and headless startup tests pass. Sharp world backgrounds, pause button visibility, reconnect, and mixed-client behavior require renewed interactive verification. |
| [Phase 10](../sef2.md#phase-10-universal-gui-coverage) | 43 | In progress | Descriptor and workflow lint pass. Player filtering, multi-target selection, item browsing, one-tooltip rendering, homes actions, and GUI scale behavior require renewed interactive verification. |
| [Phase 11](../sef2.md#phase-11-custom-aliases-bundles-fake-identity-and-sudo-suite) | 47 | Complete | Alias publication, bundle pacing and recovery, profiles, fake identity, sudo consent and locks, one-use delegation, indirection denial, server source, silent execution, audit lifecycle, and mixed-client fallback pass. |
| [Phase 12A](../sef2.md#phase-12a-fancy-tags) | 14 | Complete | Registry, assignment, secure image and archive validation, content-addressed storage, publication recovery, bounded transfer, cache, editor, rendering, cleanup, and mixed-client degradation pass. |
| [Phase 12B](../sef2.md#phase-12b-disguise) | 34 | In progress | Permission, state, syntax, and adapter tests pass. Bat, Enderman, movement, swing, hurt, reconnect, and mixed-client animation require renewed multiplayer visual verification. |
| [Phase 13A](../sef2.md#phase-13a-operational-safety-foundation) | 10 | In progress | `resource_governor` has no executable runtime. Its preview now fails honestly and manual activation is denied. |
| [Phase 13B](../sef2.md#phase-13b-community-and-staff-workflow) | 9 | In progress | `chat_channels` and `staff_duty` have no executable runtime. Their previews fail honestly and manual activation is denied. |
| [Phase 13C](../sef2.md#phase-13c-onboarding-and-rewards) | 7 | Complete | Onboarding, checklists, rules, rewards, referrals, streaks, idempotent claims, workflows, and persistence are registered and tested. |
| [Phase 13D](../sef2.md#phase-13d-recovery-and-world-operations) | 7 | In progress | `resource_worlds` has no executable runtime. Existing recovery behavior remains separately testable. |
| [Phase 13E](../sef2.md#phase-13e-governance-and-navigation) | 9 | In progress | `waypoints`, `portal_policy`, and `player_warp_review` have no executable runtime. |
| [Phase 13F](../sef2.md#phase-13f-staff-governance-and-due-process) | 6 | In progress | `approvals` and `capability_leases` control records are not connected to their dedicated authorization repositories. |
| [Phase 13G](../sef2.md#phase-13g-chat-safety-admission-and-access) | 6 | In progress | Native queue enforcement and explicit full-server exemption are implemented. Multi-client capacity, FIFO, timeout, reconnect, and denial-boundary verification remains required. |
| [Phase 13H](../sef2.md#phase-13h-content-and-world-policy) | 5 | In progress | `server_presentation` and `spawn_ecology` have no executable runtime. |
| [Phase 13I](../sef2.md#phase-13i-diagnostics-data-packs-and-verified-recovery) | 5 | Complete | Diagnostics, data-pack publication, backups, verification, restore staging, rehearsal, failure recovery, workflows, and audit are registered and tested. |
| [Phase 13J](../sef2.md#phase-13j-privacy-and-evidence) | 3 | Complete | Privacy projection, export, correction, deletion, anonymization, evidence custody, retention, hold, destruction, workflows, and audit are registered and tested. |
| [Phase 13K](../sef2.md#phase-13k-item-logistics-and-player-market) | 4 | Complete | Parcels, lost and found, direct trades, auctions, escrow, duplicate-source rejection, blocks, watches, settlement, recovery, workflows, and persistence pass. |
| [Phase 13L](../sef2.md#phase-13l-community-governance-and-knowledge) | 3 | Complete | Polls, events, capacity, waitlists, check-in, rewards, knowledge publication, search, workflows, and persistence are registered and tested. |
| [Phase 13M](../sef2.md#phase-13m-unified-display-ownership) | 4 | In progress | `display_profiles` and `display_ownership` have no executable runtime. |
| [Phase 13N](../sef2.md#phase-13n-unrestricted-administrative-enchanting-and-workstation-completion) | 16 | Complete | Canonical and shortcut routing, unsafe level and item permissions, level 1000 storage, destructive actions, stale-menu rejection, overflow safety, GUI workflow, dedicated server, and mixed client pass. |
| [Phase 13O](../sef2.md#phase-13o-permission-derived-command-cooldowns) | 39 | Complete | Canonical permission-derived resolution, exact and inherited precedence, finite fallback, provider failure, persistence, refresh, reconnect, alias, shortcut, GUI, bundle, sudo, diagnostics, and migration coverage pass. |
| [Phase 13.5](../sef2.md#phase-135-modular-responsive-configuration-platform) | 331 | Complete | All 62 module schemas, bootstrap split, transactional load, validation, migration, backup, rollback, watcher debounce, typed commands, workflow protocol, secrets filtering, generated references, mixed clients, and performance budgets pass. |
| [Phase 14](../sef2.md#phase-14-release-hardening) | 16 | In progress | Documentation, automated tests, generated references, build, GameTests, dedicated-server startup, headless client startup, and artifact inspection pass on the current worktree. Multiplayer and interactive visual matrices remain required. |
| [Global verification](../sef2.md#test-layers) | 356 | In progress | The current automated, build, server, client-startup, security-scan, and artifact gates pass. Multiplayer, LuckPerms staging, and interactive GUI and disguise verification remain incomplete. |
| [Final acceptance](../sef2.md#product) | 253 | Incomplete | Sixteen Phase 13 runtime families remain unavailable and interactive regression testing is outstanding. |

## Current verified worktree

The current worktree has the following verified evidence:

- All 583 unit tests pass with zero failures, zero errors, and zero skipped tests.
- All 45 required GameTests pass.
- Required GameTests inspect all 708 catalog actions and 315 shortcuts, compile 2,213 representative parser variants, and execute 358 safe read only routes against the live dispatcher.
- All twenty confirmed source findings in `audit.md` are repaired and have regression coverage appropriate to their failure boundary. The independent review has two additional filesystem portability blockers recorded in `docs/SECURITY_REVIEW.md`.
- NeoForge `21.1.235` resolves through ModDevGradle, passes compilation, the unit suite, all GameTests, the clean build, dedicated server startup and shutdown, headless client startup, and exact packaged dependency metadata inspection.
- `./gradlew build compileFallbackRuntimeJava generateProjectReferences generatePerformanceReport` passes.
- The dedicated server reaches `Done` with the admission mixin loaded, then stops and saves every dimension through the timeout shutdown path.
- The headless Xvfb client initializes the render thread, resources, textures, and GUI atlas and remains running until the bounded timeout. The expected headless OpenAL device error is not an SEF failure.
- `git diff --check` passes.
- The changed source and documentation contain no detected private-key material, credentials, local absolute paths, debug printing, or TODO markers.
- The built JAR contains the permission hierarchy resolver, admission mixin, offline action executor, server-control execution service, disguise proxy service, and mixin configuration. It contains no log, run, environment, PEM, or key paths.
- The live dispatcher `/feed <player>` regression test proves food becomes `20`, saturation becomes `0.0F`, and health remains unchanged.
- The current Phase 003 JAR is `sef-2.0.0.jar` with SHA-256 `a5607653488aef923722406944fbe915816a98927373f69aac6494056f4fb494` and SHA-512 `00f89016130a396e67903de42bfed05e8f448f3e4a9a01a6695963c1728586c4009b454bc18ec405799c745bca0811edce2d36f8c4fa2efb911f6660d571fa9f`. It is bound to product commit `31388263b1ef95dd27ef1362bb603d8eef73c552` in the current build packet. The current candidate check, build, focused unavailable-family suite, and 45 GameTests pass, while the universal matrix remains open.
- The Linux native writer probe verifies append, native flush, opened-object identity across path replacement, object-swap control, failure preservation, rotation, restart, and writer shutdown. The clean dependency manifest binds JNA `5.14.0` runtime coordinates to NeoForge `minecraft-dependencies:1.21.1`, records compile-only declarations, and reports no duplicate native runtime in the candidate JAR. These are task 010 evidence inputs and do not close the open per-action matrix.
- The current pull request matrix is the required gate for candidate build, native writer and opened object identity checks, failure preservation, rotation, restart, dependency insight, ZIP and duplicate runtime scans, packaged dedicated server workflows, and sanitized evidence upload on Linux, macOS, and Windows. The Minecraft Java client fixture on Ubuntu is the sole client evidence surface and must use the same candidate artifact. No separate operating system client evidence is required by the current audit contract. `EXT-002` retains separate platform supplied alert dispositions in the restricted Phase 001 evidence packet.
- The latest regression coverage proves that cross-dimension home replacement respects the destination dimension quota and that secondary player-warp inspection requires the management permission.

This evidence does not complete the multiplayer, LuckPerms, GUI visual, InvSee, admission-capacity, or disguise-animation matrices in `test.md`.

## Previous verified baseline

The values below describe the previous source revision and artifact. They are retained for comparison and must not be treated as acceptance evidence for the current worktree.

- All 390 unit tests pass.
- All 29 required GameTests pass.
- `./gradlew build compileFallbackRuntimeJava generateProjectReferences generatePerformanceReport` passes.
- The dedicated server reaches `Done`, reports 676 catalog entries, 11,659 capabilities, 290 shortcuts, 62 configuration modules, 27 repositories, no import or provider failures, a healthy security audit, and no kernel errors.
- The enhanced client negotiates and remains connected to a GUI-enabled server.
- The fallback client contains no SEF classes, joins the same GUI-enabled server, receives the command fallback notice, and remains connected.
- GUI-disabled and GUI-enabled dedicated-server runs stop normally and save every dimension.
- The final JAR is `sef-1.0-SNAPSHOT.jar` with SHA-256 `a1d8e926bd65972ad40b282a341871b743d745da7484c45aef3d5667b6a5169f`.
- JAR, secret-filename, generated-reference, security, migration, recovery, and complete-diff inspections pass.
