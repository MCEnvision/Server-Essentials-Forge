# Phase 006 Execution Plan

> **Plan ID:** PLAN-PHASE-006  
> **Phase ID:** SEF-PHASE-006  
> **Owner:** Network enforcement  
> **Classification:** MANDATORY  
> **Master plan:** [plan.md](../plan.md)  
> **Phase sequence:** 006 of 016

## Purpose and Ownership

This phase delivers network moderation, observer-aware vanish, and the editable lifecycle-message contract. Its canonical requirements are SEF-REQ-014, SEF-REQ-015, and SEF-REQ-036. The master remains the sole authority for product scope, typed interfaces, default configuration, limits, and global sequencing. This blueprint owns only dependency-ordered implementation and proof for this phase. It does not implement qualified travel or arrival authorization from Phase 007.

## Evidence-Based Entry State

| Evidence class | Area | Finding | Source or command | Freshness condition |
|---|---|---|---|---|
| OBSERVED | Existing mute path | Reference `ChatEventHandler` checks mute and cancels current chat paths, but aliases and network behavior need real proof. | SRC-204, `research/repository-map.md` | Invalid if the pinned reference revision changes. |
| OBSERVED | Existing vanish path | Vanish currently uses NBT/runtime maps, observer checks, and selector/suggestion mixins. | SRC-205, SRC-206 | Invalid if the source fingerprints or Forge descriptors change. |
| OBSERVED | Lifecycle source concepts | The reference has local connection templates, bounded compilation, and reminders, not network visit ownership. | SRC-503, `research/sources/lifecycle-messages.md` | Invalid if the cited source fingerprints change. |
| PROPOSED | Authority prerequisites | Phase 005 supplies serialized SQLite authority, session epochs, presence, bridge and replica rules. | SEF-IF-006, SEF-PHASE-005 | Revalidate approved pair digest, provenance, and Phase 005 integration before work. |
| PROPOSED | Lifecycle contract | Main-config lifecycle schema, bounded typed templates, local/network authority and delivery claims are required new work. | SEF-IF-015, SEF-REQ-036 | Invalid if the frozen master or interface projection changes. |
| UNKNOWN | Runtime fidelity | No signed-chat, three-backend lifecycle, or client presentation test has run in this planning pass. | `research/brief.md` | Resolve only through the registered headless and silent-laptop evidence gates. |

## Scope Boundaries

### Included Scope

- SEF-REQ-014 moderation state, offline-target commands, proxy login refusal, and backend signed-chat and supported message-command enforcement.
- SEF-REQ-015 durable network vanish, one observer predicate, supported visibility surfaces, explicit third-party gap matrix, and visibility-before-lifecycle ordering.
- SEF-REQ-036 main-config join, leave, welcome and welcome-back families, bounded safe templates/variants, UUID visit records, local/network authority, migration, delivery claims, diagnostics, documentation, and real-path acceptance.

### Explicit Exclusions

- SEF-PHASE-007 qualified homes, transfer reservation, travel, and arrival receipts. This phase uses Phase 005 readiness and presence only; its backend-ready receipt is not a transfer-arrival protocol.
- Custom clients, screens, menus, HUDs, scripts, arbitrary actions, external placeholders, and economy remain excluded by DEC-002, DEC-014, and the global non-goals.
- Third-party independent announcers and unsupported maps/player lists are neither hidden nor blocked by an unsupported claim. Their coverage is documented as gaps.
- Phase 015 owns final paired-artifact, complete presentation, crash/reconnect, and system-wide revalidation. This phase supplies its complete local feature proof without claiming final product closure.

## Phase Contract

### SEF-PHASE-006 — Network Enforcement and Lifecycle Messages

**Objective:** Apply durable network moderation and observer visibility, then deliver one visibility-safe main-config lifecycle authority with truthful local/network delivery semantics.  
**Owner:** Network enforcement.  
**Dependencies:** SEF-PHASE-005, SEF-REQ-007, SEF-REQ-013, SEF-REQ-019, SEF-REQ-030, DEC-014, DEC-018  
**Canonical requirements:** SEF-REQ-014, SEF-REQ-015, SEF-REQ-036  
**Documentation and release impact:** Update `README.md`, `docs/README.md`, `DOCUMENTATION.md`, `docs/configuration/lifecycle-messages.md`, `docs/features/commands/presentation-coverage.md`, `docs/security/network.md`, and `docs/troubleshooting/diagnostics.md` when their described behavior exists. No public release or production rollout occurs.  
**Next transition:** SEF-PHASE-007 begins only after both phase integrations, resulting product-branch checks, and signed phase tags pass.

**Entry criteria**

- The Phase 005 Forge and Velocity integrations are merged and tagged; approved common-contract digest, proxy provenance lock, bridge, authority, presence, session, compatibility, configuration, renderer, policy, and diagnostic contracts are revalidated.
- The appropriate Forge and Velocity phase branches are created separately from the latest approved `forge-1.20.1` and `velocity-latest` bases. No branch is stacked.
- The pinned adapter/proxy and host-capability evidence remains valid. Future consumer entry requirements are PROPOSED handoffs, not prerequisites for this phase.

**Implementation scope**

- Implement SEF-REQ-014, SEF-REQ-015, SEF-REQ-036 through the work packages and acceptance obligations below.

- Deliver moderation before vanish, and vanish before lifecycle recipient resolution and dispatch. Trace: SEF-PHASE-006.
- Use native Forge/Velocity chat adapters and the shared HuskHomes-style renderer. Do not create a custom/client interface or treat an opaque presentation action token as lifecycle or chat authority. Trace: SEF-PHASE-006.
- Keep durable lifecycle claims as delivery/idempotency records only. They are not cryptographic chat signing, client-display proof, or a new durable chat-token authority. Trace: SEF-PHASE-006.

**Execution order**

1. `P006-TASK-001` establishes moderation enforcement and its diagnostics through the real backend/proxy routes. Trace: SEF-PHASE-006.
2. `P006-TASK-002` establishes the shared observer predicate and supported-surface enforcement before lifecycle work. Trace: SEF-PHASE-006.
3. `P006-TASK-003` implements lifecycle configuration, visit and delivery state, migration, and handlers using the preceding controls. Trace: SEF-PHASE-006.
4. `P006-TASK-004` proves real local/network behavior and completes operator/developer documentation and integration evidence. Trace: SEF-PHASE-006.

**Required evidence**

- Real signed chat and supported message-command mute paths, offline ban/login, absolute expiry, switches and restarts.
- Two real observer roles proving entity, tab, selector, suggestion, own list and lifecycle-recipient filtering, with explicit supported-integration gaps.
- Compiler/property, migration, restart, crash-cut, local and one-proxy/three-backend lifecycle tests plus residual silent laptop native-chat evidence.
- Paired source/artifact identity, common digest, checks, private review subject to the established review-capability availability rule, merge commits, resulting branch verification, signed tags, and confirmed cleanup.

**Exit criteria**

- No known mandatory phase-owned defect remains. Every such defect must be fixed and its affected evidence rerun before phase closure.

- SEF-AC-014, SEF-AC-015, and SEF-AC-036 phase-owned evidence passes with no known mandatory defect.
- All emitted lifecycle messages use the latest valid main-config snapshot or their committed in-flight generation, respect current visibility/permission state, and never make an ambiguous client display look delivered.
- Both branch integrations complete in the required order, first Forge then exact approved common digest proxy retest, with resulting checks and tags before Phase 007.

## Inputs and Upstream Contracts

| Input or contract | Provider | Required state | Validation | Failure behavior |
|---|---|---|---|---|
| SEF-IF-001 identity/world | Phase 001 | UUID, session and backend/world identities are stable typed inputs. | Contract fixture and stale-session negative case. | Reject stale identity; do not fall back to names. |
| SEF-IF-002 config/diagnostics | Phase 001 | Atomic config generation plus default-off bounded capture exists. | Command/root and schema self-tests. | Stop dependent capture/reload claims until repaired. |
| SEF-IF-003 policy and SEF-IF-004 presentation | Phase 002 | Permissions and literal native components are available on both adapters. | Denial and rendering snapshots. | Deny safely; no raw markup or executable config content. |
| SEF-IF-005 bridge | Phase 004 | Authenticated bounded allowlisted messages and session fencing operate with no players. | Pinned pair retest and negative frame fixtures. | Return typed unavailable/rejection; no player-carried relay. |
| SEF-IF-006 authority/presence | Phase 005 | One SQLite writer, immutable replicas, state CAS, ready presence, compatibility admission. | Revision, resync, restart and authority-outage fixture. | Fence network mutation/admission; local mode remains distinct. |
| Lifecycle source observations | SRC-503 | Legacy templates/reminders are candidates for explicit migration only. | Fingerprint and migration inventory. | Preserve originals and record inactive conflicts. |

## Outputs and Downstream Contracts

| Output or contract | Consumer | Guaranteed state | Compatibility or versioning | Evidence |
|---|---|---|---|---|
| SEF-IF-015 lifecycle templates and durable visits | Phases 007, 009, 014, 015, 016 | One LOCAL or NETWORK authority, durable UUID visits, claim-state truthfulness and safe rendering. | Schema 1, bounded literal template AST, existing Phase 005 writer. | Local/network recovery and contract fixtures. |
| Moderation/visibility enforcement | Phase 007 and later observers | Current-session permission/visibility recheck precedes target resolution and lifecycle delivery. | Existing identity/session revisions, no new client protocol. | Two-observer and stale-session proofs. |
| Lifecycle diagnostics and support material | Phase 015 and operators | Scoped lifecycle signals, explainable reason codes, bounded output, redaction, runbook. | SEF-IF-002 limits and command root. | Enable/status/disable and support-packet test. |
| Lifecycle docs and presentation inventory | Phase 016 and wiki after merge | Actual schema, migration, limits, authority and uncertainty behavior are documented. | Documentation follows merged behavior only. | Documentation checks and linked coverage rows. |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
|---|---|---|---|---|---|---|
| P006-TASK-001 | SEF-REQ-014 | Implement one revisioned moderation path for ban, pardon, kick, disconnect, mute and unmute. Make proxy reject a committed ban at login while Forge applies the same authority state to signed public chat and every supported private/message-command alias. Persist absolute mute expiry and distinguish a committed ban from a completed disconnect. Deliver and self-test the scoped `command.decision` diagnostic adapter before any real moderation acceptance. | SEF-IF-001, SEF-IF-003, SEF-IF-005, SEF-IF-006, SRC-204; diagnostic controls from Phase 001. | Typed moderation commands/state transitions, backend enforcement adapters, support matrix and self-tested `command.decision` diagnostics. | Existing `ChatEventHandler` concept, command policy, proxy login events, Phase 005 state/replica service, Forge signed-chat routes. | First self-test enable/status/disable, denial, absent target, expiry and output-cap behavior; then real signed muted/unmuted public/private/alias paths across restart, offline ban then login, switch/reconnect, expiry, denial, and forged/stale authority tests. A proxy-only chat cancellation does not pass. |
| P006-TASK-002 | SEF-REQ-015 | Apply durable vanish before public visibility. Centralize an observer predicate for entity tracking, tab, player lists, selectors, name resolution, suggestions, SEF target lists, moderation/lifecycle audiences and supported pinned integration hooks. Reapply on login, switch, restart and permission revision. Deliver and self-test the scoped `visibility.decision` diagnostic adapter before any real visibility acceptance. | P006-TASK-001, SEF-IF-001, SEF-IF-003, SEF-IF-005, SEF-IF-006, SRC-205, SRC-206. | Revisioned vanish adapter, supported-surface matrix, explicit third-party gaps, invalidation/resync path and self-tested `visibility.decision` diagnostics. | `VanishUtil` concept, `CommandSourceStackMixin` and `EntitySelectorMixin` descriptors, proxy resolvers, backend tracking/tab/list dispatch. | First self-test enable/status/disable, unauthorized control, target removal, redaction and output-cap behavior; then two actual observer roles show unauthorized non-discovery and authorized visibility across tab/entity/suggestions/tp completion; verify restart, switch and permission change; inspect recipient packets where supported. |
| P006-TASK-003 | SEF-REQ-036 | Add `messages.lifecycle` schema 1 in each verified canonical main config. Compile typed literal templates and variants atomically. Implement LOCAL and NETWORK authority modes with `AUTO` resolving only at startup. Deliver and self-test the scoped `lifecycle.decision` diagnostic adapter before diagnostic-dependent real lifecycle acceptance. | P006-TASK-002 and SEF-IF-001 through SEF-IF-006, SRC-503, DEC-018. | SEF-IF-015 producer, lifecycle config/visit/claim migration, local/backend and network/proxy handlers, `sef messages` administration, and self-tested `lifecycle.decision` diagnostics. | Main config serializer, shared renderer, proxy authority writer, Forge readiness/disconnect adapter, native platform delivery adapters, legacy templates/reminders. | First self-test enable/status/disable, preview/permission denial, target absence, reload/reset, redaction and bounded output; then compiler/property, authority, migration, duplicate/reorder, invalid reload/last-good, variant persistence, visibility and crash-cut matrix. |
| P006-TASK-004 | SEF-REQ-014, SEF-REQ-015, SEF-REQ-036 | Run phase-owned acceptance in disposable fixtures, publish accurate documentation and coverage records, then complete paired integration evidence. | P006-TASK-001 through P006-TASK-003, host policy, docs policy, phase integration gates. | Sanitized verification packet, documentation, support guide updates, branch/PR/check/tag receipts and proposed downstream handoff. | Dedicated fixture topology, docs, CI results, release metadata and pair-manifest records. | Real local and one-proxy/three-backend paths, silent laptop chat/visibility proof, cleanup verification, Forge-first integration, fixed common digest proxy retest, both resulting checks/tags. |

## Architecture and Implementation Boundaries

The proxy remains the NETWORK owner of one serialized SQLite visit/claim ledger and network moderation/vanish state. A standalone backend remains the LOCAL owner of its own visit ledger and lifecycle delivery. `AUTO` resolves once at startup: standalone backend to LOCAL, authenticated configured network mode to NETWORK. It must not fail over to LOCAL during a bridge outage. Explicit LOCAL while network mode exists is invalid, and explicit NETWORK without authority is invalid. Network backends retain their local settings for later standalone use but report them inactive and suppress SEF and vanilla lifecycle dispatch.

Moderation uses Phase 005 CAS revisions and immutable replicas. A backend authorizes again on its owner thread before enforcing chat or a supported message command. Mute is enforced at Forge because proxy cancellation cannot be used as signed-chat proof. Bans reject login at the proxy only after committed authority state; kick/disconnect reports separate state commitment from connection outcome. No new bridge authority, signing system, or durable chat credential is created.

Vanish has one observer-aware decision path. A subject state is applied before public tracking or tab/list distribution. Resolution/suggestion and lifecycle audiences use the same current observer rights plus a session/revision check. `sef.vanish.see` remains required; `sef.messages.staff` does not grant it. Diagnostics redact locations and hidden target data. Unsupported third-party output remains an explicit support-matrix gap.

`messages.lifecycle` is a text main-config editor, not an in-game editor. It has schema 1, `scope=AUTO`, `allow_player_overrides=false`, four enabled families, 1 to 32 ASCII ID variants each, 1 to 8 body lines, 0 to 4 hover lines, 1 to 16 typed runs per line, 4096 code points per variant, 1024 per run, 256 KiB subsection, and 16384 rendered code points or 64 KiB serialized component limit. Invalid configuration rejects before swap. Expansion overflow suppresses only that event and reports a bounded diagnostic.

Families use `VISIBLE_PUBLIC`, `SUBJECT`, or `AUTHORIZED_STAFF`, `RANDOM_NO_IMMEDIATE_REPEAT` or `ROUND_ROBIN`, and the master defaults. Selection and predecessor persist with the event before dispatch. A duplicate event advances neither policy. Templates use typed semantic runs and shared native components. Braces, replacement values, hover, literal fallback, placeholders, and access checks follow the master allowlist. There is no MiniMessage, legacy-code, URL, command, expression, external placeholder, or arbitrary click-action evaluation.

Admission occurs after moderation and visibility initialization. NETWORK additionally requires matching proxy authentication and authenticated backend-ready receipt for UUID, connection epoch, backend boot and READY presence. That receipt uses Phase 005 messages and is not a Phase 007 arrival receipt. A backend switch keeps the active visit, updates ready snapshot only, and produces no join, leave, welcome or welcome-back. A true new authenticated connection creates exactly one visit classified from prior committed UUID-plus-scope history or an imported `KNOWN_RETURNING` baseline: a previously unseen UUID receives FIRST_RECORDED and the welcome family, while a known UUID, including after a name change or imported baseline, receives RETURNING and the welcome-back family. Welcome and welcome-back are mutually exclusive for one visit. Failed login, failed handshake, incompatible profile, failed backend connection, pre-ready disconnect, bridge reconnect, or retry creates none.

The authority transaction consumes admission ID, snapshots prior fields, classifies UUID history, selects variants, records event IDs and active visit/session. One event has one chosen variant but recipient values are recalculated under current visibility. Closing only matches the active session, removes presence before leave fan-out, records confirmed completion and suppresses graceful-shutdown mass leaves. Unclean ends stay uncertain and cannot produce a completion timestamp.

Delivery claims are unique by event, family, recipient UUID and recipient session. Commit CLAIMED before platform submission. `SUBMITTED` means platform transport submitted, not client display/read. A pre-dispatch proven failure can retry inside the live event. Claim or dispatch ambiguity becomes `UNCERTAIN`, is never replayed as a fresh message, and cannot modify visit classification. Claims expire after ten seconds, detailed claims compact after 24 hours to fences, pending claims are capped at 4096 per process, and submissions at 128 per tick with at most one ms owner-thread work. This is durable idempotency accounting, not token issuance or cryptographic signing.

Reload compiles an immutable snapshot then atomically swaps generation. Bad reload retains last-good snapshot, selection and policy. On invalid startup it uses a persisted verified last-good snapshot, otherwise disables lifecycle sending without falling back to unfiltered vanilla announcements. In-flight events retain their chosen generation, but audience/visibility is current. A newly disabled family cancels all of its unsent claims during the reload transition; already submitted or uncertain claims retain their truthful terminal state and are not replayed. `sef messages validate`, `reload`, `status`, and restricted `preview` use shared policy. Preview is labeled, requester-only, non-mutating, plain for console, and never performs selection, visit creation, delivery, or broadcast.

Migration scans supported old UUID records off the owner thread, seeds proven records as `KNOWN_RETURNING`, preserves verified timestamps, leaves unknown timestamps null, imports validated backend inventories under the existing network fence, and saves source counts/digests/conflicts. Display names never identify visit history. Legacy placeholder mappings are explicit; unsupported source templates remain preserved inactive conflicts. Reminders retain separate event identity and do not duplicate welcome families.

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
|---|---|---|---|---|
| SEF-RISK-005 muted signed chat bypass | Signed public/private/alias fixture through actual Forge handler; `command.decision`, chat denial reason and independent receipt. | No message leaves supported backend route while mute absolute expiry is active; unmuted signed chat stays valid. | Preserve authority state, repair missed adapter route, resync replica and rerun all supported paths. | 60-second bounded capture, restart/switch/expiry matrix and residual laptop receipt. |
| SEF-RISK-006 vanish leak | Two-role fixture sends tracking/tab/selector/suggestion/list stimuli and observes recipient set plus `visibility.decision`. | Unauthorized observer receives no target or hover/count leak; authorized observer does. | Invalidate suggestions and resync tracking/tab before reopening public visibility. | Real observers, permission change/restart/switch and explicit pinned integration gaps. |
| SEF-RISK-021 duplicate/stale lifecycle callback | Same admission ID and reordered callbacks through readiness/disconnect adapters; independent visit table and event/claim counters. | Duplicate does not create visit, advance variant or dispatch; stale close cannot close newer session. | Fence by UUID/session/admission IDs, retain current record, suppress stale claim. | Dedicated fixture waits at most 60 seconds per event; assertions compare durable sequence and native recipient count. |
| Invalid template or reload | Config fixture breaches brace, placeholder, run, line, variant, byte or rendered limits; inspect validator key/line. | Reject candidate, preserve last valid generation, no unfiltered vanilla fallback. | Correct config and atomic retry; valid in-flight event remains on committed generation. | Compiler/property tests, disabled-family validation, status/preview, restart and replacement-removal tests. |
| Newly disabled lifecycle family | Valid reload disables one family after durable events/claims exist; inspect claim state and recipient counter. | Atomically cancel every unsent claim for that family. Submitted/uncertain claims retain state and no claim is replayed. | Re-enable through a new valid generation only; do not reconstruct a canceled historical delivery. | Reload-transition fixture covers pending, submitted, uncertain, expiry and duplicate callbacks. |
| Failed admission or switch | Denied login, handshake/profile failure, pre-ready disconnect, bridge reconnect and successful backend switch. | No visit/announcement until post-ready; switch emits none and preserves active visit/classification. | Keep absence as absence, update ready backend only on valid matching receipt. | One proxy/three backend fixture with independent visit oracle and bounded readiness wait. |
| Claim crash ambiguity or overload | Crash before claim, after claim, after submission; 4097 recipient claim or 129th tick submission fixture. | Claim ambiguity becomes UNCERTAIN with no replay; expired/overflow fan-out suppresses/degrades visibly without blocking login. | Compact after retention to fences, repair capacity/config, do not reclassify history. | Crash-cut matrix and owner-thread work budget assertion with no claimed client display. |
| Migration conflict/name change | Conflicting backend UUID inventory, unsupported legacy placeholder, renamed account and missing historical time. | UUID is authoritative; preserve original data, record inactive conflict, report unavailable history values. | Resolve source mapping explicitly under migration fence; never infer by display name. | Import count/digest fixture, source-preservation check and known-returning first-admission case. |

## Diagnostics and Debugging

**Requirement IDs:** SEF-REQ-014, SEF-REQ-015, SEF-REQ-036  
**Task IDs:** P006-TASK-001, P006-TASK-002, P006-TASK-003, P006-TASK-004  
**Controls:** Use inherited default-off console grammar `sef debug on <category> <target> 60`, `sef debug status`, `sef debug off <capture-id>`, and `sef debug off all` under `sef.debug.manage`; console equivalents omit `/` and must support `moderation`, `visibility`, and `lifecycle` scopes without a joined owner. The inherited caps remain 60-second default, 300-second maximum, 200 events/second, 10000 events, 8 MiB/capture, 1024 queued events, and two simultaneous captures/process. `sef messages validate`, `reload`, and `status` require `sef.config.reload`; preview requires `sef.messages.preview`; staff/history disclosure requires its separate declared permission and never bypasses `sef.vanish.see`.  
**Signals:** Emit SEF-IF-002 records with candidate digest, side, boot, sequence, correlation, config generation, desired/actual fields, typed reason and units. Use `command.decision`, `visibility.decision`, and `lifecycle.decision` fields listed below.  
**Collection procedure:** Use the numbered runbook below: register resources, enable one bounded scoped capture, apply an actual permission-respecting fixture, correlate independent oracles, disable, sanitize, retain only the packet, then verify cleanup.  
**Headless verification:** On node-1 only after inspecting the Gradle/task graph to confirm no client, renderer, display or graphical process starts, run unit/property and dedicated no-GUI server/real handler fixtures. Configure and read back `eula=true` before each owned server launch. Console fixtures never bypass the measured permission or chat/visibility path.  
**Client verification:** Only residual signed-chat receipt and native component/tab/entity/suggestion presentation need a matching silent laptop client. Server or simulated-player evidence cannot close those claims. Unavailable laptop identity, renderer, connection, mute or input evidence leaves only that gate open.  
**Client audio isolation:** Each required laptop client uses a discovered isolated pinned-version instance with master output zero before launch. Verify discrete GPU/desktop, bind its exact `hyprctl clients -j` address, class, title and PID to the owned process tree and only its PipeWire/PulseAudio stream, mute with `wpctl` or `pactl`, read back mute before actions, and reapply after stream recreation. If exact owned PID-to-stream identity or muted state cannot be verified, stop only the owned client, leave that affected client gate open, and do not use a global mute or software-renderer fallback. Teardown stops owned watcher/client, confirms stream disappearance, removes temporary audio state and never touches a default sink, microphone, another app, or personal instance.  
**Budgets and privacy:** Inherit default 60 seconds, maximum 300 seconds, 200 events/second, 10000 events, 8 MiB/capture, 1024 queued events, and two captures/process. Capture is off by default, resets on reload/restart/timeout, stops with explicit drop/truncation counts, avoids tick/proxy-thread I/O, filters private content/addresses/hidden locations, and is distinct from the future unsampled audit system.  
**Regression and support:** Test enable/status/disable, authorization denial, target absence/removal, timeout/reload/restart reset, rate/output limits, redaction, disabled overhead and unchanged behavior. Update `docs/troubleshooting/diagnostics.md` and retain its sanitized minimal support packet only after behavioral and client gates run.

| Signal | Source and unit | Expected observation |
|---|---|---|
| `command.decision` with `permissionRevision:u64`, `desired.commandId:string`, `actual.state:enum`, `reason:enum` | Backend/proxy command owner, one decision per scoped moderation command. | Denial changes no state; committed ban/mute revision and terminal disconnect result remain distinguishable. |
| `visibility.decision` with `observerRole:enum`, `surface:enum`, `actual.visible:bool`, `visibilityRevision:u64` | Backend/proxy distribution and resolution boundary, one result per target/surface. | Unauthorized result contains no target location or identity detail and receives no supported surface. |
| `lifecycle.decision` with `visitId:UUID`, `eventId:UUID`, `scope:enum`, `family:enum`, `variantId:string`, `templateGeneration:u64`, `claimState:enum`, `reason:enum`, `recipientCount:u32` | Serialized authority writer plus bounded dispatch batch. | Failed admission has no record, switch preserves visit, stale callback rejects, authorization suppresses recipient, uncertain never asserts display. |
| `capture.status` with `events:u32`, `bytes:u64`, `dropped:u64`, `remainingSeconds:u16`, `output:string` | Diagnostic worker. | Status makes output, bounds and automatic stop observable; post-disable stimulus adds no matching record. |

1. Resolve candidate artifact hashes, pair/common digest, configured product branches, selected fixture, host anchors, exact owned runtime/log/database paths, and cleanup targets. Inspect task graphs before Gradle and register teardown.
2. Launch only the approved node-1 no-GUI server/proxy fixture after the task graph proves no client, renderer or display starts. Set and read back each owned server runtime `eula=true`, start it, and prove readiness before any console control or fixture action.
3. On the ready owned console, self-test the active scoped adapter with exact grammar `sef debug on moderation <target-or-operation> 60`, `sef debug on visibility <target-or-operation> 60`, or `sef debug on lifecycle <current-uuid-or-operation> 60`; run `sef debug status`, confirm scope, side, target, duration, inherited caps, and output under `<runtime>/logs/sef/diagnostics/<capture-id>.jsonl`; test `sef debug off <capture-id>` and `sef debug status`. Absent targets, denial and unavailable output return typed errors rather than broad logging. Complete this self-test before its task's real acceptance.
4. For a residual client claim, first verify the authorized private endpoint and server readiness, then use supported authorized controls to autojoin the isolated matching laptop client. Confirm the intended player joined the intended test world on both server and client before actions. Complete discrete-renderer, exact window/PID, owned-stream mute/readback and recreation checks; if identity or mute fails, stop only that owned client and leave the gate open.
5. Enable a fresh bounded capture using the tested scope, verify active status, and apply the real fixture through the policy/dispatcher and adapter under test: signed chat or supported alias for mute, actual observer role for vanish, or matching ready/disconnect callback for lifecycle. Do not force state, invoke a helper directly, or use a console shortcut to bypass the permission/action under measurement.
6. Correlate the JSONL using capture/correlation ID and compare ordered revisions, visit/claim state, reason and recipient counts with an independent authority-table/packet/client oracle. Test a negative, timeout, reload including newly disabled-family cancellation, stale-session, first unseen UUID, renamed known UUID, imported known UUID and genuine reconnect case. Bound each scenario to 60 seconds; expiry/overload proves suppression or unavailable rather than a retry loop.
7. Run `sef debug off <capture-id>` and `sef debug status`, repeat one harmless matching stimulus and confirm no new capture event. Preserve candidate identity, expected/actual assertion, decisive sanitized excerpt, fixture instructions and unverified client claim only.
8. Disable any temporary lifecycle fixture/config state through its registered teardown. Redact full chat, names not needed for proof, private addresses, secrets and hidden location data. Keep only required evidence at its designated phase-verification destination and the support packet, without external upload.
9. Stop exact owned client watcher, client, server, proxy and fixture processes after their final consumer. Confirm process and owned stream exit, remove only verified disposable runtimes/worlds/logs/configs/downloads/traces/reports, confirm paths absent on every used host, and record exact leftovers separately if cleanup fails.

## Verification Matrix

| Requirement or task | Static or unit | Integration | Real workflow or runtime | Negative and recovery | Execution host and prerequisites | Evidence artifact |
|---|---|---|---|---|---|---|
| SEF-REQ-014, P006-TASK-001 | State transition/expiry/alias tests and command policy snapshots. | Proxy login decision plus Forge replica/backend enforcement with restart/resync. | Actual signed public and supported private/message-command paths, offline ban login, switch and absolute expiry. | Forged/replayed/stale update, denied actor, failed disconnect, authority outage and post-expiry recovery. | node-1 only for inspected no-GUI graph/server; laptop only for actual signed-chat receipt with full silence procedure. | Sanitized transition traces, candidate hashes, handler results, client/server correlation, cleanup receipt. |
| SEF-REQ-015, P006-TASK-002 | Observer predicate/mixin descriptor and target-list tests. | Authority replica, proxy/backend resolver, tracking/tab/list and supported hook tests. | Two real roles prove tab/entity/selector/suggestion/tp completion and lifecycle recipients. | Revoked permission, stale session, switch/restart, unsupported integration gap and resync. | node-1 server checks; silent matching laptop clients only for residual visual/tab/entity input evidence. | Surface matrix, packet recipient assertions, targeted visuals/logs, cleanup receipt. |
| SEF-REQ-036, P006-TASK-003 | Compiler/property tests for every schema/template/placeholder/bound, variant/reload/preview/migration state. | Local backend and proxy writer/bridge ready receipt, durable visit/claim/restart/compaction fixtures. | Dedicated LOCAL and real one-proxy/three-backend first unseen UUID, renamed known UUID, imported known UUID and genuine reconnect admission/leave/transfer-silence paths; first welcome and welcome-back are mutually exclusive; actual native chat/count privacy. | Invalid startup/reload, newly disabled-family unsent-claim cancellation, failed admission, duplicate/reordered callback, old session, authority outage, visibility denial, migration conflict, each claim crash cut/overflow/expiry. | node-1 genuine headless suite with private endpoint; laptop client only after endpoint/readiness, authorized autojoin, both-side intended-world confirmation and silence verification. | Main-config fixture, independent UUID-table counters, event/claim traces, native chat capture, cleanup receipt. |
| P006-TASK-004 | Documentation links, configuration examples and coverage inventory validation. | Paired artifact digest/provenance and required CI checks. | Rebuild exact approved common projection, then perform Forge-first and proxy retest workflow. | Wrong pair digest, missing signed tag/check, missing cleanup or client mute proof keeps phase open. | Separate phase branches from verified product bases; no production resources. | PR/check/merge/tag/branch-containment receipts, hashes/SBOM/license inventory, docs and cleanup packet. |

## Documentation, Operations, and Release

Document actual merged behavior only. Include the canonical main-config path discovery and `sef messages status`, schema/defaults, all four families, audiences, variant policies, safe placeholder catalog and unavailable values, literal braces, console/accessibility output, preview permissions, authority scopes, UUID classification, previous-completed-visit meaning, transfer silence, migration conflicts, claim/display ambiguity, expiry/queue limits, diagnostic collection and redaction. Update presentation coverage for every `sef messages` result and moderation/visibility result introduced here. Include visibility support/gap matrix, muted-path matrix, restart/outage recovery, isolated fixture setup, laptop audio procedure, and cleanup steps.

Forge integration occurs first: use its completed PR merge commit to freeze the exact approved common-source digest and provenance. Build and retest the Velocity branch against that digest, then merge it through its checked PR. Each side requires the applicable milestone/issue/Project reconciliation, required checks and resolved conversations, private independent review subject to the established review-capability availability rule, merge-commit integration, resulting product-branch verification, signed annotated phase tag, and retained pair manifest/hashes/licenses/SBOM. Do not direct-push product bases, publish a release, change production, or begin Phase 007 until both receipts exist.

## Risks and Evidence Invalidation

| Risk ID and owner task | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
|---|---|---|---|---|---|
| SEF-RISK-005, P006-TASK-001 | Backend signed-chat and supported-alias enforcement using committed replica state and absolute expiry. | Chat/command decision and independent native receipt. | Repair route, resync state, retain mute and rerun actual paths. | Any chat adapter, alias catalog, authority or pair digest change. | Unit, dedicated server and silent client signed-chat matrix. |
| SEF-RISK-006, P006-TASK-002 | One predicate at every supported distribution/resolution boundary before lifecycle fan-out. | Per-surface recipient/visibility decision and explicit gap record. | Invalidate suggestions, resync tracking/tab, recheck permissions. | Visibility code, mixin descriptor, permission catalog, platform or pack change. | Two-observer real matrix and supported-hook inspection. |
| SEF-RISK-021, P006-TASK-003, P006-TASK-004 | Serialized UUID visit/claim transactions, post-ready trigger, current visibility checks, last-good config. | Durable sequence, event/claim counters, diagnostics and independent recipient count. | Suppress stale/uncertain output, preserve known visits, correct config atomically. | Lifecycle schema/config, authority writer, bridge readiness, renderer, platform delivery or pair digest change. | Compiler, migration, local/network restart and all crash-cut/client-display cases. |
| SEF-RISK-020, P006-TASK-004 | Forge-first common-digest freeze, exact proxy projection retest, host/artifact identity and cleanup gates. | Digest/profile/renderer/stream identity, PR/tag/branch receipts. | Rerun affected exact pair path and leave gate open. | Any merge, artifact, dependency/pack, host/renderer or stream replacement. | Rebuild, retest, merge verification, signed tags and host cleanup. |

## Phase Completion Packet

- Forge and Velocity phase branch commits, signed merge commits/tags, exact source commits, Forge-first frozen common digest, proxy retest digest, SHA-256/SHA-512, license inventory, SBOM, and supported attestation evidence.
- Required checks, resolved conversation and private-review availability/result receipts, resulting `forge-1.20.1` and `velocity-latest` branch containment verification, milestone/issue/Project updates.
- Versioned main-config fixtures, compiler/property reports, migration counts/source digests/conflict report, independent UUID visit/claim counters, moderation/visibility matrices, crash-cut outcomes, and exact unsupported integration gaps.
- Sanitized node-1 server/proxy traces plus laptop native chat and visibility evidence only where client receipt/render/input is the residual claim. Record matching candidate/profile, desktop/discrete renderer, window/PID, stream mute/recreation and join correlation.
- Documentation links/coverage checks, diagnostics support packet, candidate identity, known limitations and remaining gate status.
- Before every check, named owned scratch/runtimes/worlds/logs/configs/databases/downloads/screenshots/traces/reports and teardown registration. After every exit path, exact owned process shutdown, stream/watch cleanup, required evidence retention, verified path removal and per-host cleanup confirmation. No cleanup failure is hidden by a passing test.

## Noncanonical Interface Projection

This machine-readable projection is derived from the frozen master. It is not a competing contract. Its producer and consumer integration is described above.

```json
{
  "phaseId": "SEF-PHASE-006",
  "interfaces": [
    {
      "id": "SEF-IF-001",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Identity and world",
        "records": {
          "Actor": {
            "uuid": "UUID?",
            "source": "PLAYER|CONSOLE|SYSTEM|MACHINE|FAKE_PLAYER",
            "originId": "string",
            "permissionRevision": "u64"
          },
          "Session": {
            "playerId": "UUID",
            "proxyBoot": "UUID?",
            "connectionEpoch": "u64",
            "backendId": "string",
            "backendBoot": "UUID"
          },
          "WorldRef": {
            "backendId": "string",
            "worldGeneration": "UUID",
            "dimension": "resource_location",
            "registryDigest": "sha256"
          },
          "Location": {
            "world": "WorldRef",
            "x": "finite f64",
            "y": "finite f64",
            "z": "finite f64",
            "yaw": "finite f32",
            "pitch": "finite f32"
          }
        },
        "errors": ["WORLD_REPLACED", "DIMENSION_MISSING", "REGISTRY_MISMATCH", "STALE_SESSION"],
        "ownership": "Backend creates durable world generation; authenticated proxy creates network connection epochs; display names confer no authority."
      },
      "acceptance_ids": ["SEF-AC-006", "SEF-AC-019"]
    },
    {
      "id": "SEF-IF-002",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Configuration and diagnostics",
        "records": {
          "ConfigSnapshot": {"generation": "u64", "schema": "u16", "sanitizedDigest": "sha256", "source": "string"},
          "DiagnosticEvent": {"captureId": "UUID", "correlationId": "UUID", "event": "string", "side": "BACKEND|PROXY|HARNESS", "boot": "UUID", "sequence": "u64", "tick": "u64?", "monotonicNanos": "u64", "utc": "Instant", "desired": "typed_map", "actual": "typed_map", "reason": "enum_string", "units": "typed_map", "configGeneration": "u64", "candidateDigest": "sha256"}
        },
        "methods": ["validateAndSwap(expectedGeneration:u64, proposed:ConfigSnapshot) -> Result<ConfigSnapshot>", "enable(actor:Actor, scope:string, target:string?, durationSeconds:u16) -> Result<UUID>", "status(actor:Actor, captureId:UUID?) -> Result<CaptureStatus>", "disable(actor:Actor, captureId:UUID) -> Result<CaptureStatus>"],
        "errors": ["INVALID_CONFIG", "STALE_REVISION", "DENIED", "TARGET_ABSENT", "CAPTURE_LIMIT", "OUTPUT_UNAVAILABLE"],
        "ownership": "Atomic owner-executor config swap; default-off bounded diagnostic worker, independent from unsampled audit."
      },
      "acceptance_ids": ["SEF-AC-019"]
    },
    {
      "id": "SEF-IF-003",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Command policy",
        "records": {"CommandContext": {"actor": "Actor", "session": "Session?", "commandId": "string", "operationId": "UUID", "targetIds": "UUID[]", "expectedRevision": "u64?", "configGeneration": "u64"}, "PolicyDecision": {"allowed": "bool", "reason": "enum_string", "permissionRevision": "u64", "cooldownUntil": "Instant?", "confirmationRequired": "bool"}},
        "methods": ["authorize(context:CommandContext) -> Result<PolicyDecision>", "execute(context:CommandContext, typedArguments:typed_map) -> Result<OperationResult>"],
        "errors": ["DENIED", "HIERARCHY_DENIED", "CONFLICTING_OWNER", "COOLDOWN", "QUOTA", "CANCELLED", "STALE_REVISION"],
        "ownership": "Canonical catalog and policy; world owner rechecks before mutation; no monetary policy."
      },
      "acceptance_ids": ["SEF-AC-005"]
    },
    {
      "id": "SEF-IF-004",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Presentation and actions",
        "records": {"Message": {"key": "string", "locale": "string", "severity": "SUCCESS|INFO|WARNING|ERROR", "values": "map<string,Literal>", "groups": "ActionGroup[]", "page": "PageInfo?", "templateId": "string?", "templateGeneration": "u64?"}, "ActionBinding": {"token": "opaque128", "recipient": "UUID", "session": "Session", "operation": "enum_string", "targetId": "string", "targetRevision": "u64", "snapshotId": "UUID?", "expires": "Instant", "confirmationDigest": "sha256?", "idempotencyKey": "UUID"}, "PageInfo": {"snapshotId": "UUID", "cursor": "opaque128", "pageSize": "u16", "pageNumber": "u32", "total": "u64?"}},
        "methods": ["render(message:Message, audience:AudienceContext) -> PlatformComponent", "redeem(actor:Actor, session:Session, token:opaque128) -> Result<OperationResult>"],
        "errors": ["ACTION_EXPIRED", "WRONG_RECIPIENT", "STALE_SESSION", "STALE_REVISION", "DENIED", "ALREADY_APPLIED", "CONFIRMATION_REQUIRED"],
        "ownership": "Server-issued token registry; Forge native and Velocity Adventure adapters; all untrusted values literal."
      },
      "acceptance_ids": ["SEF-AC-030"]
    },
    {
      "id": "SEF-IF-005",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Authenticated bridge",
        "records": {"Envelope": {"protocolMajor": "u16", "protocolMinor": "u16", "senderId": "string", "recipientId": "string", "senderBoot": "UUID", "channelEpoch": "UUID", "sequence": "u64", "requestId": "UUID", "type": "allowlisted_enum", "actor": "Actor?", "session": "Session?", "expectedRevision": "u64?", "expires": "Instant", "body": "bounded_typed_bytes"}},
        "methods": ["send(envelope:Envelope) -> Result<TypedReply>", "rotatePeerKey(peerId:string, expectedKeyId:string, nextKeyId:string) -> Result<RotationReceipt>"],
        "errors": ["AUTH_FAILED", "REPLAY", "STALE_EPOCH", "EXPIRED", "OVERSIZED", "RATE_LIMIT", "PROTOCOL_MISMATCH", "UNREGISTERED_SERVER", "QUEUE_FULL"],
        "ownership": "TLS 1.3 mutually authenticated private direct socket; peer certificate maps to registered server; no player carrier or arbitrary command relay."
      },
      "acceptance_ids": ["SEF-AC-011"]
    },
    {
      "id": "SEF-IF-006",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Network authority and compatibility",
        "records": {"StateChange": {"stateKind": "BAN|MUTE|VANISH|HOME|OPERATION|VISIT|LIFECYCLE_DELIVERY", "key": "string", "revision": "u64", "previousRevision": "u64", "operationId": "UUID", "tombstone": "bool", "value": "typed_record?"}, "Presence": {"session": "Session", "profileId": "string", "profileDigest": "sha256", "state": "CONNECTING|READY|TRANSFERRING|DISCONNECTED"}, "CompatibilityRelation": {"clientProfileDigest": "sha256_of_server_observable_negotiated_profile", "sourceProfileDigest": "sha256", "destinationProfileDigest": "sha256", "adapterDigest": "sha256", "evidenceDigest": "sha256", "allowed": "bool"}},
        "methods": ["compareAndSet(actor:Actor, change:StateChange) -> Result<CommitReceipt>", "snapshot(afterRevision:u64) -> Result<StateSnapshot>", "resolve(observer:Actor, targetId:UUID) -> Result<Presence>", "admit(session:Session, destinationId:string) -> Result<AdmissionDecision>"],
        "errors": ["STALE_REVISION", "AUTHORITY_UNAVAILABLE", "RESYNC_REQUIRED", "TARGET_NOT_VISIBLE", "INCOMPATIBLE_PROFILE", "UNKNOWN_PROFILE"],
        "ownership": "One SQLite writer; backend ordered immutable replicas; deny network mutation or unsafe admission while authority is unavailable. Visit and lifecycle-delivery kinds are reserved here and remain non-dispatchable until the typed Phase 006 handlers are registered; its implementation uses the existing serialized authority writer."
      },
      "acceptance_ids": ["SEF-AC-012", "SEF-AC-013", "SEF-AC-018"]
    },
    {
      "id": "SEF-IF-015",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Lifecycle templates and durable visits",
        "records": {"LifecycleConfig": {"schema": "u16=1", "generation": "u64", "scope": "AUTO|LOCAL|NETWORK", "allowPlayerOverrides": "bool", "families": "map<JOIN|LEAVE|WELCOME|WELCOME_BACK,TemplateFamily>"}, "TemplateFamily": {"enabled": "bool", "audience": "VISIBLE_PUBLIC|SUBJECT|AUTHORIZED_STAFF", "selection": "RANDOM_NO_IMMEDIATE_REPEAT|ROUND_ROBIN", "variants": "TemplateVariant[1..32]"}, "TemplateVariant": {"id": "bounded_identifier", "lines": "TemplateLine[1..8]", "hoverLines": "TemplateLine[0..4]"}, "TemplateLine": {"runs": "TemplateRun[1..16]"}, "TemplateRun": {"template": "bounded_template_string", "role": "PRIMARY|LABEL|VALUE|SUCCESS|WARNING|ERROR|LOCATION|EDIT|METADATA", "bold": "bool"}, "Visit": {"scopeId": "string", "playerId": "UUID", "firstAdmittedAt": "Instant?", "lastAdmittedAt": "Instant?", "lastCompletedAt": "Instant?", "activeVisitId": "UUID?", "activeSession": "Session?", "completedVisits": "u64", "baseline": "KNOWN_RETURNING|RECORDED_FIRST", "revision": "u64"}, "LifecycleEvent": {"eventId": "UUID", "visitId": "UUID", "scopeId": "string", "session": "Session", "family": "JOIN|LEAVE|WELCOME|WELCOME_BACK", "readyBackend": "string", "world": "WorldRef?", "previousAdmittedAt": "Instant?", "previousCompletedAt": "Instant?", "configGeneration": "u64", "visibilityRevision": "u64", "cause": "ADMISSION_READY|DISCONNECT_CONFIRMED", "firstRecordedVisit": "bool"}, "DeliveryClaim": {"claimId": "UUID", "eventId": "UUID", "family": "JOIN|LEAVE|WELCOME|WELCOME_BACK", "variantId": "string", "templateGeneration": "u64", "recipientId": "UUID", "recipientSession": "Session", "state": "CLAIMED|SUBMITTED|SUPPRESSED|EXPIRED|UNCERTAIN", "reason": "enum_string", "expires": "Instant"}, "VisitDecision": {"visit": "Visit", "classification": "FIRST_RECORDED|RETURNING", "previousAdmittedAt": "Instant?", "previousCompletedAt": "Instant?", "events": "LifecycleEvent[]", "duplicate": "bool", "durableSequence": "u64"}, "CompiledLifecycleSnapshot": {"generation": "u64", "effectiveScope": "LOCAL|NETWORK", "authorityId": "string", "configDigest": "sha256", "templates": "map<family_variant_id,BoundedLiteralTemplateAst>", "enabledFamilies": "set<JOIN|LEAVE|WELCOME|WELCOME_BACK>", "compiledAt": "Instant"}},
        "methods": ["compileLifecycle(config:LifecycleConfig) -> Result<CompiledLifecycleSnapshot>", "admitVisit(session:Session, readyBackend:string, world:WorldRef?, admissionId:UUID) -> Result<VisitDecision>", "closeVisit(visitId:UUID, session:Session, endedAt:Instant, reason:enum_string) -> Result<VisitDecision>", "claimDelivery(event:LifecycleEvent, recipient:Actor, recipientSession:Session) -> Result<DeliveryClaim>", "preview(actor:Actor, family:LifecycleFamily, variantId:string, subject:UUID?) -> Result<Message[]>"],
        "errors": ["INVALID_TEMPLATE", "UNKNOWN_PLACEHOLDER", "TEMPLATE_LIMIT", "LIFECYCLE_AUTHORITY_UNAVAILABLE", "STALE_SESSION", "DUPLICATE_LIFECYCLE_EVENT", "VISIBILITY_DENIED", "DELIVERY_UNCERTAIN", "PREVIEW_DENIED", "MIGRATION_CONFLICT"],
        "ownership": "Standalone backend owns LOCAL visits and delivery. NETWORK proxy owns one durable visit/claim ledger and emits once after backend readiness; backend suppresses duplicate lifecycle announcements. Commit claims before dispatch, never replay ambiguous client display."
      },
      "acceptance_ids": ["SEF-AC-036"]
    }
  ]
}
```

## Next Transition

After every Phase 006 acceptance, documentation, cleanup, PR merge, resulting product-branch verification and signed tag gate succeeds for both required branches, atomically advance only the execution cursor to SEF-PHASE-007. Its entry action is to reread `phases/plan-phase-007.md` and implement qualified homes, transfer reservations and arrival recovery from the verified Phase 006 visibility/lifecycle handoff. These future entry requirements remain PROPOSED until the current phase is fully integrated.
