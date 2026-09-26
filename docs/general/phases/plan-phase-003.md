# Phase 003 Execution Plan

> **Plan ID:** PLAN-PHASE-003  
> **Phase ID:** SEF-PHASE-003  
> **Owner:** Forge gameplay  
> **Classification:** MANDATORY  
> **Master plan:** [plan.md](../plan.md)  
> **Phase sequence:** 003 of 016

## Purpose and Ownership

This phase ports the frozen retained local gameplay catalog onto the approved Forge command, data, diagnostics, identity, and rich-presentation foundations. It owns detailed execution for SEF-REQ-007 and SEF-REQ-008 only. The master remains the authority for product scope, retained-action disposition, future network travel, and the global phase sequence. In particular, Phase 003 supplies local homes and safe local teleport behavior, but does not add server-qualified homes, proxy routing, transfer receipts, lifecycle authority, RTP, audit capture, economic policy, or any client interface.

## Evidence-Based Entry State

| Evidence class | Area | Finding | Source or command | Freshness condition |
|---|---|---|---|---|
| PROPOSED | Phase dependency | Phase 002 producer delivery is an unexecuted required entry gate: its canonical command policy and presentation/action interfaces must be approved before this phase registers retained actions. | Frozen master Section 13, SEF-PHASE-002 exit | Invalid after a source, interface, or approved Phase 002 change. |
| OBSERVED | Local locations | Reference home records have owner, revision, tombstone, dimension, coordinates, and facing but no backend identity. Safe teleport already identifies local dimension, loaded-chunk, claim, hazard, and border checks. | SRC-203, `HomeRecord.java`, `SavedLocation.java`, `SafeTeleportService.java` | Invalid if the pinned reference revision or fingerprints change. |
| OBSERVED | Backport boundary | Reference NeoForge payload, configuration-task, data-component, and registry-aware item paths cannot be copied into Forge 1.20.1. | SRC-207, repository map | Invalid if the source inventory changes. |
| PROPOSED | Retained gameplay | Implemented chat, social, mail, nickname, announcement, moderation support, kits, direct item/player utilities, and compatible server controls are ported only after action-level classification confirms no excluded dependency. | FIND-007, SEF-REQ-007 | Invalid if the frozen action inventory or owner exclusions change. |
| PROPOSED | Local travel | Homes, warps, spawn, back, and safe local movement retain quotas, permission, dimension, border, safety, warmup cancellation, and server-owned world operations. | FIND-008, SEF-REQ-008 | Invalid if the local command policy or backend data contract changes. |

## Scope Boundaries

### Included Scope

- SEF-REQ-007: Implement each Phase 000 classified retained local command action through the Phase 002 dispatcher, reusing command policy, literal rich feedback, local data, and backend ownership.
- SEF-REQ-008: Implement local homes, warps, spawn, back, and safety-checked movement using the local identity and world contract, including actual-movement-only back records and cancellation recovery.
- Adapt relevant retained inventory and recovery inputs to Forge 1.20.1 NBT and Forge server-side entry points without client payload negotiation.
- Extend the inherited bounded diagnostics category registry with local command and teleport signals before dependent integration fixtures, and document the delivered controls.

### Explicit Exclusions

- SEF-REQ-016 and SEF-REQ-017 are Phase 007 work. A local record is never presented as a qualified network home or a transfer receipt.
- SEF-REQ-014 and SEF-REQ-015 remain Phase 006 ownership. Local moderation helpers must not create a competing network authority.
- Economy, money, prices, shops, providers, and cost policy remain excluded by NG-001.
- Custom screens, menus, custom client networking, HUDs, Fancy Tags, menu-dependent utilities, rendering, and a required SEF client remain excluded by NG-002.
- RTP allocation, preparation, proxy routing, audit history, restoration, lifecycle-message authority, production rollout, and public publication belong to later phases or non-goals.

## Phase Contract

### SEF-PHASE-003 — Retained Local Gameplay and Safe Teleport Workflows

**Objective:** Complete every inventory-classified retained local command action and local safe teleport workflow on Forge 1.20.1 without excluded economy or interface dependencies.  
**Owner:** Forge gameplay  
**Dependencies:** SEF-PHASE-002, SEF-REQ-005, SEF-REQ-006, SEF-REQ-019, SEF-REQ-030  
**Canonical requirements:** SEF-REQ-007, SEF-REQ-008  
**Documentation and release impact:** Update the root README, docs index, technical overview, retained-command and teleport documentation, diagnostics support guide, test procedures, and Phase 003 verification record with behavior that exists in the merged candidate. No public release or production rollout is authorized.  
**Next transition:** SEF-PHASE-004, create proxy lineage and bridge after Phase 003 is integrated, checked, post-merge verified, and tagged.  

**Entry criteria**

- The Phase 002 branch is merged into `forge-1.20.1`, its resulting product commit is verified, and its signed phase tag and required checks are present.
- The local action inventory from Phase 000 is available with each candidate action classified retained, excluded, unavailable, or future.
- SEF-IF-001 through SEF-IF-004 and the Phase 001 diagnostic support controls are approved at their producer versions.
- The implementation host has inspected the exact Gradle task graph before selecting any headless verification task.

**Implementation scope**

- Implement SEF-REQ-007, SEF-REQ-008 through the work packages and acceptance obligations below.

- Register only inventory-classified retained local actions through SEF-IF-003, with canonical permissions, source/target hierarchy, cooldown, quota, warmup, confirmation, and literal SEF-IF-004 result rendering. Trace: SEF-PHASE-003.
- Build local command adapters for retained chat, messaging, nicknames, mail, announcements, moderation support, kits, direct item/player utilities, and compatible server controls, refusing missing provider implementations rather than inventing behavior. Trace: SEF-PHASE-003.
- Preserve local data revisions, dimensions, world generations, and corruption/migration safeguards from Phase 001; do not reinterpret unsupported records. Trace: SEF-PHASE-003.
- Make all local travel capture current `WorldRef` and `Location`, validate the final destination on the owning server thread, and write back history only after real successful movement. Trace: SEF-PHASE-003.

**Execution order**

1. `P003-TASK-001` validates the frozen action inventory against the Phase 002 catalog and publishes the actionable retained or refused matrix. Trace: SEF-PHASE-003.
2. `P003-TASK-002` ports retained non-teleport command adapters and their configuration, persistence, policy, and presentation bindings. Trace: SEF-PHASE-003.
3. `P003-TASK-003` ports local location records and home, warp, spawn, and back command adapters with versioned local-world identity. Trace: SEF-PHASE-003.
4. `P003-TASK-004` implements safe local movement, warmup cancellation, actual-arrival back history, and recovery. Trace: SEF-PHASE-003.
5. `P003-TASK-005` adds scoped local command and teleport diagnostics and the support runbook before real-path fixtures. Trace: SEF-PHASE-003.
6. `P003-TASK-006` executes server-first command, persistence, and teleport fixtures, then the minimal residual Trident laptop claim. Trace: SEF-PHASE-003.
7. `P003-TASK-007` completes documentation, artifact inspection, phase integration evidence, and cleanup receipts. Trace: SEF-PHASE-003.

**Required evidence**

- A retained-action matrix mapping every Phase 003 action to source locator, dispatcher entry point, exclusion status, permission policy, test, and user-visible message.
- Actual Forge handler and dedicated-world fixture results for normal, denial, missing-provider, reload, data-corruption, unsafe-destination, warmup cancellation, disconnect, and back-history paths.
- Independent destination and facing assertions after the actual world mutation, not a helper return value.
- Targeted laptop native-chat/input evidence only for the residual claim that real client command input receives readable rich vanilla feedback; server assertions remain sufficient for server-owned travel.
- Candidate/source/configuration/world-generation bindings, sanitized diagnostics excerpts, documentation updates, required checks, integration receipt, and per-host cleanup receipt.

**Exit criteria**

- No known mandatory phase-owned defect remains. Every such defect must be fixed and its affected evidence rerun before phase closure.

- Every inventory-classified retained Phase 003 action has an implemented Forge-native path or the frozen inventory explicitly marks it excluded, unavailable, or future, with no excluded economy or interface linkage.
- Local homes, warps, spawn, back, quota, permission, dimension, border, safety, warmup cancellation, and recovery fixtures pass through the real dispatcher and owning-server movement path.
- A back record changes only after observed successful movement at the validated final local destination and facing.
- The diagnostic controls, bounded signals, support procedure, and Phase 003 docs are delivered and verified.
- The phase branch is checked, independently reviewed subject to the established review-capability availability rule, merged through the required pull request, verified on `forge-1.20.1`, and tagged before Phase 004 starts.

## Inputs and Upstream Contracts

| Input or contract | Provider | Required state | Validation | Failure behavior |
|---|---|---|---|---|
| SEF-IF-001 identity and world | SEF-PHASE-001 | `Actor`, `WorldRef`, and `Location` records preserve UUID and local world generation. | Decode valid and replaced-world fixtures. | Refuse operation with typed safe error; do not use display names or directory names as identity. |
| SEF-IF-002 configuration and diagnostics | SEF-PHASE-001 | Atomic config snapshot and bounded default-off capture controls are available to console and authorized operators. | Validate generation, status, timeout, output-limit, and reload fixtures. | Retain last known valid configuration; refuse capture when target or output is unavailable. |
| SEF-IF-003 command policy | SEF-PHASE-002 | Every registration resolves one owner and invokes the policy before local mutation. | Dispatcher, hierarchy, cooldown, quota, and console-source tests. | Return policy reason with zero mutation and no alternate local handler. |
| SEF-IF-004 presentation and actions | SEF-PHASE-002 | Messages render literal untrusted values and action tokens reauthorize at redemption. | Semantic component snapshots, console rendering, stale or wrong-recipient action tests. | Render safe error or plain console result, never parse input as markup or command text. |
| Versioned backend data and safe storage | SEF-PHASE-001 | Local records are versioned, atomic, recoverable, and bound to the local world identity. | Migration, corrupt-record quarantine, backup and restart fixtures. | Quarantine unreadable state and refuse dependent mutation without destroying owner data. |

## Outputs and Downstream Contracts

| Output or contract | Consumer | Guaranteed state | Compatibility or versioning | Evidence |
|---|---|---|---|---|
| Retained local action matrix and Forge command registrations | SEF-PHASE-004 through SEF-PHASE-007, operators | Each local command has one policy owner, explicit disposition, safe feedback, and no excluded dependency. | Phase 002 command and presentation interfaces remain the sole cross-platform contracts. | Matrix, dispatcher tests, JAR and configuration inspection. |
| Local location and travel semantics | SEF-PHASE-007 | Local travel captures `WorldRef` and final local arrival; it does not claim backend-qualified network ownership. | Future qualified-home and transfer contracts must add their own server identity, revision, receipt, and recovery fields. | Local movement, restart, world-replacement, and back-history fixtures. |
| Local diagnostics categories and support procedure | SEF-PHASE-004 through SEF-PHASE-016 | Bounded command and teleport observations use SEF-IF-002 and remain independent from future unsampled audit. | Category additions are additive and obey the inherited capture limits. | On, status, off, denial, timeout, redaction, and disabled-overhead evidence. |
| Updated local operations documentation | Later implementers and support operators | Implemented local commands, limitations, data recovery, and troubleshooting steps are discoverable. | Documentation describes only merged behavior and links forward contracts without claiming their completion. | Documentation links and support rehearsal. |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
|---|---|---|---|---|---|---|
| `P003-TASK-001` | SEF-REQ-007, SEF-REQ-008 | Reconcile the Phase 000 source inventory with the Phase 002 catalog. Split every candidate into retained native action, explicit exclusion, missing-provider refusal, or future scope. Record the source entry, Forge dispatcher entry, permission, persistence/configuration dependency, and message key. | Phase 000 action inventory, SEF-IF-003, SEF-IF-004, NG-001, NG-002. | Checked action matrix and registration boundary. | Existing reference command families, proposed Forge gameplay adapters, command catalog. | Matrix completeness check, dependency/JAR/config scan, and a test that excluded menu/economy registrations cannot be resolved. |
| `P003-TASK-002` | SEF-REQ-007 | Port each retained non-teleport command family through one Forge-native adapter, including chat, messaging, nicknames, mail, announcements, moderation support, kits, direct item/player utilities, and compatible server controls. Adapt retained item state to Forge 1.20.1 NBT where needed. | P003-TASK-001, SEF-IF-001, SEF-IF-003, SEF-IF-004, backend data contract. | Policy-bound handlers, localized literal feedback, persistence/reload behavior, and explicit unavailable responses for absent providers. | Proposed Forge gameplay command adapters; `ChatEventHandler` and `InventoryUtilityCommands` are evidence locators, not copied APIs. | Real dispatcher tests for player and console, denied hierarchy, cooldown/quota, malicious literal input, reload, restart, and provider absence with zero mutation. |
| `P003-TASK-003` | SEF-REQ-008 | Adapt local home, warp, spawn, and back records and commands to bind every saved local destination to the current durable `WorldRef` plus location and facing. Preserve supported local revision/tombstone behavior. | P003-TASK-001, SEF-IF-001, SEF-IF-003, Phase 001 data migration/recovery. | Local-only travel record codec, quota-aware command adapters, and explicit `WORLD_REPLACED` or `DIMENSION_MISSING` refusal. | `HomeRecord` and `SavedLocation` concepts, planned local travel records, SEF-IF-001. | Codec, migration, duplicate-name, quota, dimension, tombstone, world-replacement, corruption, and restart fixtures. |
| `P003-TASK-004` | SEF-REQ-008 | Route local travel through a single safe movement executor. Revalidate dimension, world generation, border, loaded chunk, claim, hazard, footing, clearance, and facing on the owning server thread; bind warmup cancellation to movement, damage, disconnect, invalid target, and shutdown. Persist back only after actual success. | P003-TASK-003, SEF-IF-001, SEF-IF-003, observed `SafeTeleportService` boundary. | Safe local arrival result, no false success, actual-arrival back record, bounded recovery outcome. | `SafeTeleportService` concept, planned warmup controller, local world executor. | Dedicated-world natural command fixtures with independent position/facing oracle, border/hazard/claim/unloaded-chunk rejection, movement/damage/disconnect cancellation, restart recovery, and no stale back update. |
| `P003-TASK-005` | SEF-REQ-007, SEF-REQ-008 | Register local `command` and `teleport` diagnostic categories under SEF-IF-002, update support documentation, and test controls before dependent real-path fixtures. | SEF-IF-002, P003-TASK-002, P003-TASK-004. | Typed bounded events, status/on/off behavior, redacted support packet procedure. | Diagnostic worker, proposed command and teleport adapters, `docs/troubleshooting/diagnostics.md`. | Enable/status/disable, console operation, denied operator, absent target, timeout, reload/reset, output cap, redaction, disabled and enabled overhead, and unchanged action result. |
| `P003-TASK-006` | SEF-REQ-007, SEF-REQ-008 | Execute the dependency-ordered headless fixtures and the minimal residual client receipt. Use a no-GUI dedicated server for server-owned command and travel proof; use a laptop client only for native command input and rich feedback that headless evidence cannot prove. | P003-TASK-002 through P003-TASK-005, exact candidate hash, inspected task graph, registered cleanup. | Sanitized test reports, server logs, targeted client evidence, and per-host cleanup receipt. | Real Forge dispatcher, dedicated server/GameTest fixture, isolated laptop client only when required. | Bounded normal, negative, recovery, and residual visual checks; no laptop client substitutes for server assertions and no node-1 graphical client is permitted. |
| `P003-TASK-007` | SEF-REQ-007, SEF-REQ-008 | Update implemented-behavior documentation, inspect artifacts for excluded surfaces, prepare the phase completion packet, and complete sequential Forge integration evidence. | P003-TASK-001 through P003-TASK-006, required repository checks. | Documentation, verification record, review/check/merge/tag evidence, downstream handoff. | README, docs index, technical/topic docs, artifact manifest, phase branch and pull request. | Link checks, artifact inspection, required PR checks and resolved conversations, post-merge branch verification, signed annotated tag, and verified cleanup. |

Ordering is strict for inventory classification, policy registration, travel records, safety execution, diagnostics, and dependent proof. P003-TASK-002 through P003-TASK-004 may run their pure unit and implementation checks after P003-TASK-001, but their diagnostic-dependent real-path acceptance fixtures run only in P003-TASK-006 after P003-TASK-005 has delivered instrumentation and passed its control self-tests. This does not make P003-TASK-005 depend on its own acceptance fixtures. Independent command-family adapters may proceed in parallel only after P003-TASK-001 and only when they do not share a mutable configuration or persistence migration. A failure in an inherited interface, local data migration, or safe-movement invariant stops the affected path and preserves later network adoption as future work.

## Architecture and Implementation Boundaries

Phase 003 is Forge-backend ownership. It consumes the pure contracts from SEF-IF-001 through SEF-IF-004 but introduces no alternative command, presentation, identity, or diagnostics authority. A command starts with a typed `CommandContext`, invokes `authorize`, resolves only the specific local data and server state it needs, and rechecks policy, revision, and source/target state immediately before a world or inventory mutation. Output is a semantic `Message` rendered by the inherited adapter, with player supplied names, nicknames, text, item labels, and destination labels treated as literal values.

Local records preserve UUID identity, revision, deletion state, local `WorldRef`, coordinates, yaw, and pitch. A world folder or mutable display name never substitutes for world generation or actor identity. Existing reference home data lacks a server identity. This phase must therefore not stretch a local location into a cross-server claim. Phase 007 alone introduces server-qualified homes and durable transfer receipts.

All level, entity, inventory, chunk, claim, and teleport work runs on the owning server thread. Workers may process immutable validated record snapshots, file serialization, and bounded diagnostic output, but never live Forge objects. Config reload uses the Phase 001 atomic swap. A rejected or stale snapshot preserves the last valid configuration. Retained inventory utilities use Forge 1.20.1 NBT and native event/command APIs, never NeoForge components, payload registration, custom menus, or a client protocol.

Teleport state is local and serial: request, policy decision, warmup, final validation, attempted movement, observed arrival, durable back update, result. The back stack receives the pre-move local position only after arrival at the final validated destination. Cancellation, failure, invalidation, disconnect, shutdown, or uncertain result leaves the previous back state intact. There is no proxy, cross-server session, remote server identity, or arrival receipt in this phase.

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
|---|---|---|---|---|
| Excluded or absent-provider action resolves | Inventory matrix and `command.decision` reason identify excluded dependency or unavailable provider. | The command is absent when excluded, or returns a readable unavailable result when its frozen retained route lacks a provider. It does not construct a menu, economy service, or substitute behavior. | Keep source data inert and correct only the inventory/adapter binding. | P003-TASK-001 and P003-TASK-002 catalog and dispatcher fixture. |
| Permission, hierarchy, quota, or stale policy changes before mutation | Policy decision includes permission revision and actual terminal state. | Deny with one literal rich result and zero world, inventory, mail, or player-state mutation. | Reauthorize from current state; do not retry automatically. | P003-TASK-002 denial, target-revocation, cooldown, and console-source fixtures. |
| Corrupt, unknown-schema, tombstoned, or replaced-world local destination | Record decode and final local-world recheck emit `WORLD_REPLACED`, `DIMENSION_MISSING`, or safe corruption reason. | Refuse before warmup completion and before movement. | Quarantine unreadable record under Phase 001 rules; retain original owner data and require explicit repair/migration. | P003-TASK-003 migration/corruption/world-replacement fixtures. |
| Destination becomes unsafe during warmup | `teleport.decision` includes preflight/final validation reason, world generation, and actual state. | Final server-thread validation refuses border, unloaded chunk, claim, hazard, footing, clearance, or dimension failure. | Release the operation without changing back state; allow a fresh request only after normal policy checks. | P003-TASK-004 fixture changes border/claim/hazard/chunk state before the bounded final validation. |
| Movement, damage, disconnect, or shutdown interrupts warmup | Warmup correlation observes event source and cancellation reason. | Cancel exactly once with no teleport and no back write. | Clear pending warmup idempotently; reconnect does not resume it. | P003-TASK-004 natural command fixtures with bounded tick waits and restart inspection. |
| Teleport reports success but arrival or facing differs | Independent world query compares intended final local location and facing with observed player state after the real movement path. | No success result or back update without observed arrival matching the validated destination contract. | Return safe failure, preserve prior back state, and expose correlation for investigation. | P003-TASK-004 actual-arrival oracle and P003-TASK-006 dedicated server fixture. |
| Diagnostic target is absent, capture over limit, output unavailable, or reload occurs | SEF-IF-002 typed error and `capture.status` report reason, bounds, and output location. | Refuse or stop capture safely; diagnostics never alter the measured command or movement outcome. | Idempotent disable clears only the owned capture; restart/reload does not resume verbose capture. | P003-TASK-005 control, timeout, cap, reload, and absent-target fixtures. |

For the destination-race fixture, the actual entry point is the retained local home or warp command through the real Forge dispatcher. A disposable dedicated-world fixture starts an allowed warmup, changes the chosen destination's claim, hazard, border, or chunk readiness before the final server-thread check, and waits only the configured warmup plus a bounded server-tick allowance. The oracle is no observed player displacement and no new back record. The local `teleport.decision` event distinguishes `SAFETY_CHANGED` from policy denial or world replacement. Recovery is a cleared pending operation and an unchanged prior back record. The fixture never calls the teleport helper directly or grants a permission that the command path would not have.

## Diagnostics and Debugging

**Requirement IDs:** SEF-REQ-007, SEF-REQ-008, SEF-REQ-019, SEF-REQ-030  
**Task IDs:** P003-TASK-002, P003-TASK-003, P003-TASK-004, P003-TASK-005, P003-TASK-006  
**Controls:** Reuse console `sef debug on command <target> <seconds>` and `sef debug on teleport <target> <seconds>`, `sef debug status`, and `sef debug off <capture-id>` under `sef.debug.manage`; console resolves an explicit online UUID or operation target and never requires a joined owner. Disable is idempotent, captures default off, and reload or shutdown resets capture.  
**Signals:** `command.decision` records command ID, actor source, target count, permission revision, desired policy, actual terminal state, reason, and correlation ID. `teleport.decision` records operation ID, local world generation and dimension, desired location class, actual arrival state, warmup state, cancellation or safety reason, checked conditions, and server-tick units.  
**Collection procedure:** Follow the numbered Phase 003 procedure below to register a disposable fixture, enable one 60 second bounded category, reproduce the real dispatcher path, inspect only the matching JSONL correlation, disable, redact, retain the minimal proof, and clean owned resources.  
**Headless verification:** On node-1 only after the task graph proves no client, renderer, or display is launched, run the actual Forge server command dispatcher and dedicated server or server-only GameTest fixture against a disposable world. Assert policy denial, final local safety, observed position/facing, and actual-arrival-only back updates. These checks prove backend behavior but not graphical chat rendering or physical input.  
**Client verification:** One residual laptop check verifies a real player uses the retained command input and receives readable localized rich vanilla feedback. It does not substitute for dedicated-server safety proof. If desktop, renderer, Trident instance/window/PID binding is absent, stop the owned client and leave only this visual/input gate open.
**Client audio isolation:** Leave audio settings unchanged. Every required client uses the master Section 14 Trident instance lifecycle on the verified laptop, with explicit owned instance, runtime, window/PID, discrete renderer and joined-world evidence. No Trident command or client runs on node-1. Do not create mute watchers or require a playback stream or mute proof. Server-only checks need no client. Verify exact owned process exit and instance cleanup after the final consumer.
**Budgets and privacy:** Diagnostics remain default-off and use inherited 60 second default, 300 second maximum, 200 events per second, 10,000 events, 8 MiB output, 1,024 queued events, and two captures per process. Redact message text, private addresses, item NBT, and unrelated identity data. Disabled capture performs no expensive formatting or I/O; bounded diagnostic capture is separate from future mandatory unsampled audit.  
**Regression and support:** Test enable, status, disable, permission denial, absent/removed targets, timeout, reload/reset, rate/output limits, redaction, disabled and enabled overhead, and unchanged command/teleport behavior. Update `docs/troubleshooting/diagnostics.md` and link the local command and teleport packet procedure from README, docs index, and the affected topic documentation.  

| Signal | Source and unit | Expected observation |
|---|---|---|
| `command.decision` with `commandId`, `permissionRevision:u64`, `desired.policy`, `actual.state`, `reason` | Forge dispatcher, one record per scoped command decision | Denial and missing provider have zero mutation; allowed action has exactly one terminal outcome. |
| `teleport.decision` with `operationId`, `worldGeneration:UUID`, `warmupState`, `checks:u32`, `reason` | Owning server thread, one per local movement operation | Final success follows observed local arrival; cancelled or unsafe operation has no back write. |
| `teleport.arrival` with expected and actual position/facing, `worldTick:u64`, and `backRevision` | Independent dedicated-world fixture and durable local record | Position, facing, world generation, and back update agree only after actual movement. |
| `capture.status` with events, bytes, dropped, remaining seconds, and output path | Diagnostic worker, records and bytes | Timeout, cap, reload, or disable stops new records without affecting gameplay. |

1. Resolve the exact Phase 003 candidate commit/JAR digest, Forge 47.3.12 and Java 17 inputs, config generation, disposable runtime path nested below the verified project anchor, fixture seed, log/report destination, and every owned process, world, watcher, and cleanup target. Inspect the Gradle task graph first. For a dedicated server, create the exact disposable `eula.txt` with `eula=true` and read it back before launch.
2. From the owned server console, enable the smallest category with `sef debug on command <target> 60` or `sef debug on teleport <target> 60`, run `sef debug status`, and verify authorized actor, target, remaining duration, limits, and the discovered JSONL output under the disposable runtime. If no target or output exists, record the typed refusal and do not use unbounded logs.
3. Apply the real retained command or local home, warp, spawn, or back input through the dispatcher. For server-owned claims use the no-GUI dedicated fixture and do not bypass policy, warmup, or safety with a console teleport. For the residual client claim only, use the laptop after Trident instance/client identity, discrete renderer and joined-world requirements pass.
4. Filter the bounded JSONL by correlation ID and compare command/teleport decisions with an independent world and durable-record oracle. Assert normal success, permission denial, missing provider, invalid world, safety race, cancellation, and restart result according to the fixture. Record the exact bounded wait and whether a client-only claim remains unverified.
5. Disable with `sef debug off <capture-id>`, confirm status, invoke one harmless matching action, and confirm no further record appears. Redact private text, addresses, NBT, and unrelated identities from decisive excerpts.
6. Retain only the sanitized candidate identity, test result, decisive diagnostics excerpt, documentation proof, and required targeted client artifact under the Phase 003 verification destination. Stop exact owned server, client and fixture processes; verify their exit and owned instance removal; remove exact disposable runtime, world, logs, crash reports, reports, and incidental bytecode after their last consumer.
7. Verify cleanup on every used host without following symlinks. Preserve source, shared caches, personal instances, personal worlds, tracked fixtures, requested deliverables, and reusable diagnostics. Record exact leftovers and recovery action separately if cleanup cannot be confirmed.

## Verification Matrix

| Requirement or task | Static or unit | Integration | Real workflow or runtime | Negative and recovery | Execution host and prerequisites | Evidence artifact |
|---|---|---|---|---|---|---|
| SEF-REQ-007, P003-TASK-001 | Inventory, dependency, command catalog, resource, and JAR closure checks. | Registration to policy and renderer interface fixtures. | No runtime claim needed beyond downstream actions. | Excluded economy/menu interface registration is absent; missing provider returns safe unavailable. | node-1 or laptop only after task-graph inspection; no graphical client. | Action matrix and artifact-inspection report. |
| SEF-REQ-007, P003-TASK-002 | Message snapshots, NBT codec tests, policy and persistence units. | Real Forge dispatcher and reload/restart fixtures. | Dedicated no-GUI server command path for retained player and console actions. | Denied, stale, cooldown/quota, literal injection, provider absence, corrupt input, and restart recovery. | node-1 dedicated server only when graph confirms no client; disposable runtime with read-back `eula=true`. | Sanitized test report, dispatcher diagnostics, config digest, and cleanup receipt. |
| SEF-REQ-008, P003-TASK-003 | Local record codec, revision, quota, tombstone, migration, and world-generation units. | Data load/reload/backup/restart path. | Dedicated-world home, warp, spawn, and back commands through actual dispatcher. | Duplicate, missing, corrupt, replaced world, missing dimension, quota, and stale record refusal. | node-1 no-GUI dedicated server with exact candidate and disposable nested runtime. | Record fixtures, independent state assertions, diagnostics excerpt, cleanup receipt. |
| SEF-REQ-008, P003-TASK-004 | Warmup state and final-validation branch tests. | Real server-thread safety executor with claim, border, chunk, and hazard fixture controls. | Natural command stimulus, bounded warmup, observed local position/facing, actual-arrival back history. | Movement, damage, disconnect, shutdown, claim/border/hazard/chunk change during warmup; recovery leaves prior back intact. | node-1 server-only unless a residual input claim is separately selected; console prepares fixture but never bypasses the command path. | World oracle, durable back-record comparison, bounded trace, cleanup receipt. |
| P003-TASK-005 | Diagnostic schema, redaction, disabled-overhead, cap, and idempotent-control units. | Console on/status/off, reload/reset, target-removal, and output-unavailable fixtures. | Dedicated no-GUI server capture of a real local command/teleport operation. | Unauthorized enable, absent target, timeout, queue/output limit, malformed scope, and capture-output failure. | node-1 after graph inspection; no client is launched. | Sanitized JSONL, status records, support-runbook rehearsal, cleanup receipt. |
| P003-TASK-006 | Client adapter snapshot is not a substitute for this row. | Match laptop candidate to node-1 server artifact/config/world generation. | One targeted laptop native input and rich-feedback receipt after server readiness and joined-world proof. | Missing desktop/discrete renderer/Trident instance/window identity leaves only this gate unverified and stops the owned client. | Linux laptop for client only, node-1 dedicated private server. Isolated client uses matching candidate and verified Trident instance binding. | Trident instance/window/PID/renderer proof, joined-world correlation, targeted evidence, both-host cleanup receipt. |

## Documentation, Operations, and Release

Update `README.md`, `docs/README.md`, and `DOCUMENTATION.md` with only the implemented retained local command and travel behavior. Create or update focused command, teleport, configuration, migration/recovery, test, verification, and troubleshooting topics only where the subsystem now exists. Document the action dispositions, permission and console behavior, explicit unavailable outcomes, local-only home limits, world-generation refusal, warmup cancellation, safe destination checks, actual-arrival back semantics, and bounded diagnostics procedure. Prepare corresponding wiki changes from tracked documentation, but publish them only after the approved Forge merge. No proxy artifact, public release, or production deployment is produced in this phase.

## Risks and Evidence Invalidation

| Risk ID and owner task | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
|---|---|---|---|---|---|
| SEF-RISK-002, P003-TASK-001 and P003-TASK-002 | Action-level disposition and dependency closure before registration. | Catalog/config/JAR inspection finds economy, GUI, payload, or menu linkage. | Remove exact residual path and retain ordinary server behavior only. | Affected action matrix, adapter tests, and artifact inspection. | Rerun closure scan and every affected retained action fixture. |
| FIND-102, P003-TASK-003 and P003-TASK-004 | Bind local travel to `WorldRef` and refuse ambiguous/replaced local worlds. | Decode/final validation reports world generation or dimension mismatch. | Preserve record, quarantine unreadable data, and refuse without movement. | Local location migration, travel, and back-history proof. | Rerun migration and dedicated-world travel matrix. |
| SEF-RISK-013, P003-TASK-002 and P003-TASK-006 | Reuse literal SEF-IF-004 values and token reauthorization. | Malicious markup, separator, Unicode, stale recipient, or revoked-rights fixture. | Reject/reissue only after fresh authorization; no injected action executes. | Presentation snapshot and residual client evidence. | Rerun component, dispatcher, and targeted laptop receipt. |
| P003-RISK-001, P003-TASK-004 | Final owning-thread safety validation and actual-arrival-only back write. | Teleport trace or independent oracle disagrees on location, facing, or back revision. | Clear pending operation, preserve prior back state, and refuse success. | Safety, cancellation, and restart evidence. | Rerun normal, race, cancellation, and recovery fixtures. |
| P003-RISK-002, P003-TASK-005 and P003-TASK-006 | Default-off bounded capture, strict target resolution, Trident instance ownership and registered teardown. | Capture cap/status, redaction scan, owned instance/process absence checks. | Disable capture, stop owned client/server, remove only owned scratch, and report leftovers. | Diagnostic, client, and cleanup receipts. | Rerun control, real-path, and per-host cleanup procedure. |

## Phase Completion Packet

- The checked retained-action matrix, source locators, frozen dispositions, and explicit excluded or unavailable rows.
- Candidate source commit, Forge/JDK/dependency/configuration/world-generation identifiers, JAR SHA-256 and SHA-512, and artifact inspection showing no excluded client, menu, payload, or economy surface.
- Unit, integration, dedicated-world, reload/restart, safety-race, cancellation, corruption, and recovery results with independently observed final position, facing, and back state.
- Diagnostic control and bounded support-runbook evidence, sanitized JSONL excerpts, redaction check, and any residual laptop proof or exact open client gate.
- Documentation changes and link-check results for implemented behavior.
- Required issue/milestone/Project and pull-request state, deterministic check results, private independent review result when available, merged `forge-1.20.1` commit containment, post-merge verification, and signed annotated phase tag.
- Resource register and cleanup receipts: exact nested disposable runtime/world/log/report paths, `eula=true` readback when a server ran, owned process exit, owned Trident instance removal when used, retained sanitized evidence destinations, and verified absence of disposable outputs on each used host.

## Next Transition

After every Phase 003 exit and integration gate passes, fetch and verify the merged tagged `forge-1.20.1` commit, then begin only SEF-PHASE-004 from the approved product bases. Phase 004 first creates the Velocity lineage and exact shared-contract provenance, then builds the authenticated empty-backend bridge. No Phase 004 branch or proxy work begins while this phase has an open check, review, merge, tag, cleanup, or required client-evidence gate.

## Noncanonical Interface Projection

The following derived projection is copied exactly from the frozen interface input. It is evidence for producer and consumer drift checks and does not create another canonical contract.

```json
{
  "phaseId": "SEF-PHASE-003",
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
        "errors": [
          "WORLD_REPLACED",
          "DIMENSION_MISSING",
          "REGISTRY_MISMATCH",
          "STALE_SESSION"
        ],
        "ownership": "Backend creates durable world generation; authenticated proxy creates network connection epochs; display names confer no authority."
      },
      "acceptance_ids": [
        "SEF-AC-006",
        "SEF-AC-019"
      ]
    },
    {
      "id": "SEF-IF-002",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Configuration and diagnostics",
        "records": {
          "ConfigSnapshot": {
            "generation": "u64",
            "schema": "u16",
            "sanitizedDigest": "sha256",
            "source": "string"
          },
          "DiagnosticEvent": {
            "captureId": "UUID",
            "correlationId": "UUID",
            "event": "string",
            "side": "BACKEND|PROXY|HARNESS",
            "boot": "UUID",
            "sequence": "u64",
            "tick": "u64?",
            "monotonicNanos": "u64",
            "utc": "Instant",
            "desired": "typed_map",
            "actual": "typed_map",
            "reason": "enum_string",
            "units": "typed_map",
            "configGeneration": "u64",
            "candidateDigest": "sha256"
          }
        },
        "methods": [
          "validateAndSwap(expectedGeneration:u64, proposed:ConfigSnapshot) -> Result<ConfigSnapshot>",
          "enable(actor:Actor, scope:string, target:string?, durationSeconds:u16) -> Result<UUID>",
          "status(actor:Actor, captureId:UUID?) -> Result<CaptureStatus>",
          "disable(actor:Actor, captureId:UUID) -> Result<CaptureStatus>"
        ],
        "errors": [
          "INVALID_CONFIG",
          "STALE_REVISION",
          "DENIED",
          "TARGET_ABSENT",
          "CAPTURE_LIMIT",
          "OUTPUT_UNAVAILABLE"
        ],
        "ownership": "Atomic owner-executor config swap; default-off bounded diagnostic worker, independent from unsampled audit."
      },
      "acceptance_ids": [
        "SEF-AC-019"
      ]
    },
    {
      "id": "SEF-IF-003",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Command policy",
        "records": {
          "CommandContext": {
            "actor": "Actor",
            "session": "Session?",
            "commandId": "string",
            "operationId": "UUID",
            "targetIds": "UUID[]",
            "expectedRevision": "u64?",
            "configGeneration": "u64"
          },
          "PolicyDecision": {
            "allowed": "bool",
            "reason": "enum_string",
            "permissionRevision": "u64",
            "cooldownUntil": "Instant?",
            "confirmationRequired": "bool"
          }
        },
        "methods": [
          "authorize(context:CommandContext) -> Result<PolicyDecision>",
          "execute(context:CommandContext, typedArguments:typed_map) -> Result<OperationResult>"
        ],
        "errors": [
          "DENIED",
          "HIERARCHY_DENIED",
          "CONFLICTING_OWNER",
          "COOLDOWN",
          "QUOTA",
          "CANCELLED",
          "STALE_REVISION"
        ],
        "ownership": "Canonical catalog and policy; world owner rechecks before mutation; no monetary policy."
      },
      "acceptance_ids": [
        "SEF-AC-005"
      ]
    },
    {
      "id": "SEF-IF-004",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Presentation and actions",
        "records": {
          "Message": {
            "key": "string",
            "locale": "string",
            "severity": "SUCCESS|INFO|WARNING|ERROR",
            "values": "map<string,Literal>",
            "groups": "ActionGroup[]",
            "page": "PageInfo?",
            "templateId": "string?",
            "templateGeneration": "u64?"
          },
          "ActionBinding": {
            "token": "opaque128",
            "recipient": "UUID",
            "session": "Session",
            "operation": "enum_string",
            "targetId": "string",
            "targetRevision": "u64",
            "snapshotId": "UUID?",
            "expires": "Instant",
            "confirmationDigest": "sha256?",
            "idempotencyKey": "UUID"
          },
          "PageInfo": {
            "snapshotId": "UUID",
            "cursor": "opaque128",
            "pageSize": "u16",
            "pageNumber": "u32",
            "total": "u64?"
          }
        },
        "methods": [
          "render(message:Message, audience:AudienceContext) -> PlatformComponent",
          "redeem(actor:Actor, session:Session, token:opaque128) -> Result<OperationResult>"
        ],
        "errors": [
          "ACTION_EXPIRED",
          "WRONG_RECIPIENT",
          "STALE_SESSION",
          "STALE_REVISION",
          "DENIED",
          "ALREADY_APPLIED",
          "CONFIRMATION_REQUIRED"
        ],
        "ownership": "Server-issued token registry; Forge native and Velocity Adventure adapters; all untrusted values literal."
      },
      "acceptance_ids": [
        "SEF-AC-030"
      ]
    }
  ]
}
```
