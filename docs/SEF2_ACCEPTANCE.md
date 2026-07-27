# SEF 2 Acceptance Ledger

This ledger measures completion against `sef2.md`. A scope is complete only when its required behavior, exit criteria, and current verification evidence pass. Source presence, command registration, schemas, generic editors, and compilation do not count as complete behavior by themselves.

## Status meanings

| Status | Meaning |
| --- | --- |
| Incomplete | At least one required behavior or verification gate is missing. |
| In progress | The scope is actively being completed, but its exit criteria do not all pass. |
| Complete | Every requirement and exit criterion passes on the current source revision. |

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
| [Phase 7](../sef2.md#phase-7-inventory-workstations-kits-and-player-utilities) | 23 | Complete | Inventory authorization, offline inventory backup, workstations, kits, item validation, utility state, shortcut collision, super enchanting, revocation, and server enforcement pass. |
| [Phase 8](../sef2.md#phase-8-native-economy-and-signs) | 14 | Complete | Exact money arithmetic, provider ownership, idempotent transactions, cost recovery, import once, inventory compensation, all sign types, persistence failure, and bounded ranking pass. |
| [Phase 9](../sef2.md#phase-9-client-protocol-and-gui-pilot) | 62 | Complete | Optional negotiation, typed bounded payloads, replay protection, session invalidation, enhanced connection, command-fallback connection, headless visual startup, reconnect cleanup, and dedicated-server isolation pass. |
| [Phase 10](../sef2.md#phase-10-universal-gui-coverage) | 43 | Complete | Every catalog action has a permission-filtered descriptor, typed workflow or reviewed direct route, command fallback, session validation, confirmation, result projection, HUD decision, and catalog lint coverage. |
| [Phase 11](../sef2.md#phase-11-custom-aliases-bundles-fake-identity-and-sudo-suite) | 47 | Complete | Alias publication, bundle pacing and recovery, profiles, fake identity, sudo consent and locks, one-use delegation, indirection denial, server source, silent execution, audit lifecycle, and mixed-client fallback pass. |
| [Phase 12A](../sef2.md#phase-12a-fancy-tags) | 14 | Complete | Registry, assignment, secure image and archive validation, content-addressed storage, publication recovery, bounded transfer, cache, editor, rendering, cleanup, and mixed-client degradation pass. |
| [Phase 12B](../sef2.md#phase-12b-disguise) | 34 | Complete | Persistent state, target policy, projection, proxy identity, traits, abilities, command workflows, expiry, cleanup, adapter failure, client presentation, and vanilla fallback pass. |
| [Phase 13A](../sef2.md#phase-13a-operational-safety-foundation) | 10 | Complete | Maintenance, restart, change windows, health, guardrails, cleanup, approvals, runtime enforcement, workflows, and private HUD state are registered and tested. |
| [Phase 13B](../sef2.md#phase-13b-community-and-staff-workflow) | 9 | Complete | Staff duty, queues, handoffs, tasks, schedules, announcements, ticketing, reports, workflows, persistence, and fallback routes are registered and tested. |
| [Phase 13C](../sef2.md#phase-13c-onboarding-and-rewards) | 7 | Complete | Onboarding, checklists, rules, rewards, referrals, streaks, idempotent claims, workflows, and persistence are registered and tested. |
| [Phase 13D](../sef2.md#phase-13d-recovery-and-world-operations) | 7 | Complete | Graves, inventory recovery, world operations, cleanup, snapshots, rollback state, failure handling, workflows, and persistence are registered and tested. |
| [Phase 13E](../sef2.md#phase-13e-governance-and-navigation) | 9 | Complete | Governance, waypoints, map policy, travel, portals, regions, navigation, workflows, bounded state, and command fallback are registered and tested. |
| [Phase 13F](../sef2.md#phase-13f-staff-governance-and-due-process) | 6 | Complete | Approval separation, access leases, administrative locks, appeals, discipline, review state, expiry, recovery, workflows, and audit pass. |
| [Phase 13G](../sef2.md#phase-13g-chat-safety-admission-and-access) | 6 | Complete | Automod, chat control, admission, invites, access applications, replay protection, expiry, enforcement, workflows, and private state projection pass. |
| [Phase 13H](../sef2.md#phase-13h-content-and-world-policy) | 5 | Complete | Resource packs, presentation, border transitions, ecology, world policy, previews, rollback state, workflows, and fallback routes are registered and tested. |
| [Phase 13I](../sef2.md#phase-13i-diagnostics-data-packs-and-verified-recovery) | 5 | Complete | Diagnostics, data-pack publication, backups, verification, restore staging, rehearsal, failure recovery, workflows, and audit are registered and tested. |
| [Phase 13J](../sef2.md#phase-13j-privacy-and-evidence) | 3 | Complete | Privacy projection, export, correction, deletion, anonymization, evidence custody, retention, hold, destruction, workflows, and audit are registered and tested. |
| [Phase 13K](../sef2.md#phase-13k-item-logistics-and-player-market) | 4 | Complete | Parcels, lost and found, direct trades, auctions, escrow, duplicate-source rejection, blocks, watches, settlement, recovery, workflows, and persistence pass. |
| [Phase 13L](../sef2.md#phase-13l-community-governance-and-knowledge) | 3 | Complete | Polls, events, capacity, waitlists, check-in, rewards, knowledge publication, search, workflows, and persistence are registered and tested. |
| [Phase 13M](../sef2.md#phase-13m-unified-display-ownership) | 4 | Complete | Display ownership, priority, leases, composition, privacy, packet budgets, workflows, HUD decisions, and fallback presentation are registered and tested. |
| [Phase 13N](../sef2.md#phase-13n-unrestricted-administrative-enchanting-and-workstation-completion) | 16 | Complete | Canonical and shortcut routing, unsafe level and item permissions, level 1000 storage, destructive actions, stale-menu rejection, overflow safety, GUI workflow, dedicated server, and mixed client pass. |
| [Phase 13O](../sef2.md#phase-13o-permission-derived-command-cooldowns) | 39 | Complete | Canonical permission-derived resolution, exact and inherited precedence, finite fallback, provider failure, persistence, refresh, reconnect, alias, shortcut, GUI, bundle, sudo, diagnostics, and migration coverage pass. |
| [Phase 13.5](../sef2.md#phase-135-modular-responsive-configuration-platform) | 331 | Complete | All 62 module schemas, bootstrap split, transactional load, validation, migration, backup, rollback, watcher debounce, typed commands, workflow protocol, secrets filtering, generated references, mixed clients, and performance budgets pass. |
| [Phase 14](../sef2.md#phase-14-release-hardening) | 16 | Complete | User and maintainer documentation, generated references, installation, migration, compatibility, performance, security, release notes, clean build, server and client matrix, and JAR inspection pass. |
| [Global verification](../sef2.md#test-layers) | 356 | Complete | All 390 unit tests and all 29 required GameTests pass. Java 21 build, dedicated server, enhanced client, fallback client, migration fixtures, performance budgets, generated-reference drift, security review, and artifact inspection pass. |
| [Final acceptance](../sef2.md#product) | 253 | Complete | Product, permission, server mode, GUI, persistence, security, performance, compatibility, migration, documentation, and verification criteria are implemented and verified on the current source revision. |

## Current verified baseline

Verified on 2026-07-27 with Java `21.0.11`, Minecraft `1.21.1`, NeoForge `21.1.233`, Parchment `2024.11.17`, and Gradle `8.8`.

- All 390 unit tests pass.
- All 29 required GameTests pass.
- `./gradlew build compileFallbackRuntimeJava generateProjectReferences generatePerformanceReport` passes.
- The dedicated server reaches `Done`, reports 676 catalog entries, 11,659 capabilities, 290 shortcuts, 62 configuration modules, 27 repositories, no import or provider failures, a healthy security audit, and no kernel errors.
- The enhanced client negotiates and remains connected to a GUI-enabled server.
- The fallback client contains no SEF classes, joins the same GUI-enabled server, receives the command fallback notice, and remains connected.
- GUI-disabled and GUI-enabled dedicated-server runs stop normally and save every dimension.
- The final JAR is `sef-1.0-SNAPSHOT.jar` with SHA-256 `a1d8e926bd65972ad40b282a341871b743d745da7484c45aef3d5667b6a5169f`.
- JAR, secret-filename, generated-reference, security, migration, recovery, and complete-diff inspections pass.
