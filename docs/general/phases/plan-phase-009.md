# Phase 009 Execution Plan

> **Plan ID:** PLAN-PHASE-009  
> **Phase ID:** SEF-PHASE-009  
> **Owner:** Forge audit adapters  
> **Classification:** MANDATORY  
> **Master plan:** [plan.md](../plan.md)  
> **Phase sequence:** 009 of 016

## Purpose and Ownership

This phase attaches Forge native capture adapters to the durable, privacy-aware audit journal delivered by Phase 008. It owns only comprehensive applied-action capture for SEF-REQ-023. The master remains the canonical authority for requirement scope, interfaces, global acceptance, risk ownership, integration topology, and the final Phase 015 parity proof. This blueprint details the adapters, evidence, failure boundaries, and Phase 009 proof only.

## Evidence-Based Entry State

| Evidence class | Area | Finding | Source or command | Freshness condition |
|---|---|---|---|---|
| OBSERVED | Forge event semantics | The cited 1.20.x event sources show cancellable or pre-mutation callbacks, simulated item handling, nontransactional containers, and mutable explosion candidate lists. They do not prove the exact Forge 47.3.12 symbols or an applied mutation. | SRC-308 through SRC-313 | Invalid until the exact 47.3.12 dependency source, mappings, compilation, and real fixture resolve each selected hook. |
| OBSERVED | Existing audit sinks | Existing bounded queues and file logging may drop records. They are not a comprehensive lossless world journal. | SRC-314, SRC-315 | Invalid if the referenced baseline changes. Phase 008 journal receipts remain the required sink boundary. |
| PROPOSED | Capture boundary | Original Forge native adapters will record declared server-observable committed actions, their snapshots, confidence, and supported-mod coverage. | FIND-023, FIND-105, DEC-008 | Invalidated by exact-hook, pack, or real-path proof that exposes an uncovered mandatory family. |
| RESOLVED | Storage and privacy | Phase 008 supplies SEF-IF-009 journal, redaction, watermarks, gaps, MySQL/MariaDB ingestion, and the no-sampling audit boundary. | SEF-PHASE-008, SEF-IF-009 | Must be revalidated from its approved integration and interface digest before this phase begins. |
| OBSERVED | Reference boundary | CoreProtect APIs use Bukkit types and its current pinned license is Artistic License 2.0. It is behavioral reference only. | SRC-304, SRC-305, SRC-316 through SRC-322 | Invalid if a planned adapter imports, copies, or advertises Bukkit/CoreProtect implementation. |

## Scope Boundaries

### Included Scope

- SEF-REQ-023: Capture every declared server-observable family at an applied, partial, failed, cancelled, denied, attempted, or unknown outcome as appropriate, with immutable before and after snapshots, confidence, causal links, and explicit coverage.
- Forge 47.3.12 hook resolution, real mutation-boundary adapters, server-thread snapshotting, the coverage registry, supported pack adapter pins, accepted authoritative movement capture, and capture-specific diagnostics.
- Independent real-path vanilla and pinned supported-pack fixtures. A fixture observes world, inventory, entity, or journal state outside the adapter under test.

### Explicit Exclusions

- Journal durability, SQL ingestion, credential-redaction policy, retention, rotation, and incident delivery remain Phase 008 work. This phase invokes its approved pre-sink policy and journal API; it does not replace either.
- Lookup, inspection, export, public API, parity catalogue, migrations, rollback, restore, undo, and item give remain Phase 010 and Phase 011 work. Their later availability is not an entry gate.
- The dual-engine end-to-end conservation, load, capacity, restoration, and complete functional-parity closure is Phase 015. A capture oracle here is not a claim that all parity is complete.
- No Bukkit runtime, Bukkit types, universal capability hook, copied CoreProtect source, client protocol, client UI, or claim to observe opaque third-party mod internals.

## Phase Contract

### SEF-PHASE-009 — Capture comprehensive applied actions with explicit coverage

**Objective:** Deliver original Forge 47.3.12 capture adapters that append only classified actual outcomes to SEF-IF-009, preserve same-tick distinctions and causal attribution, record every accepted declared movement without sampling, and make supported, partial, and unsupported coverage observable.  
**Owner:** Forge audit adapters  
**Dependencies:** SEF-PHASE-008  
**Supporting contract and risk dependencies:** SEF-IF-001, SEF-IF-002, SEF-IF-009, SEF-RISK-009, SEF-RISK-010  
**Canonical requirements:** SEF-REQ-023  
**Documentation and release impact:** Add the capture-family matrix, exact supported-pack adapter pins, opaque-mod limitation, cause-confidence semantics, operator diagnostic runbook, and troubleshooting links. No public release occurs.  
**Next transition:** SEF-PHASE-010, expose scoped investigation and API.

**Entry criteria**

- Phase 008 is integrated, tagged, and its approved journal/redaction/watermark interface digest is revalidated before any capture adapter is enabled.
- Exact Forge 47.3.12 artifact, mappings, loader, Java 17 candidate, and the selected supported-pack artifacts/digests are recorded; 1.20.x source observations alone are explicitly insufficient.
- A node-1 no-GUI task graph that starts no client or renderer is inspected, and exact owned verification paths, processes, database fixtures, and cleanup handlers are registered. A server must reach readiness within 120 seconds, an owned SQL engine fixture within 60 seconds when used, and fixture shutdown within 30 seconds; a failed readiness diagnosis ends the attempt and cleans it, never waits indefinitely.

**Implementation scope**

- Implement SEF-REQ-023 through the work packages and acceptance obligations below.

- SEF-REQ-023 owns the ordered capture matrix: blocks and block entities, containers and item flows, natural and automated changes, players and entities, interactions, commands and communications, administration and network lifecycle, and accepted authoritative movement changes.
- Applied capture is committed-state observation. Attempt, event dispatch, cancellation, simulation, planned mutation, container open/close, or candidate explosion list is never emitted as `APPLIED` without the family-specific terminal oracle. Trace: SEF-PHASE-009.

**Execution order**

1. `P009-TASK-001` resolves exact Forge 47.3.12 hooks and implements the applied-state boundary before any family claim. Its initial proof is source/compile and source-unit only. Trace: SEF-PHASE-009.
2. `P009-TASK-002` implements scoped causal attribution and supported-pack adapter contracts on top of the journal boundary. Its initial proof is source-unit only. Trace: SEF-PHASE-009.
3. `P009-TASK-003` registers the complete action/coverage matrix and explicit unsupported or partial rows. Its initial proof is structural completeness only. Trace: SEF-PHASE-009.
4. `P009-TASK-004` implements unsampled accepted authoritative movement capture and immutable snapshot rules. Its initial proof is source-unit only. Trace: SEF-PHASE-009.
5. `P009-TASK-005` adds cause groups, confidence, fake-player and asynchronous isolation rules. Its initial proof is source-unit only. Trace: SEF-PHASE-009.
6. `P009-TASK-006` implements diagnostics, reaches a ready runtime, and passes the mandatory `on`/`status`/`off` self-test before any P009-TASK-001 through P009-TASK-005 table row receives real-path verification. Trace: SEF-PHASE-009.
7. `P009-TASK-007` proves vanilla real entry points and failure fixtures with independent oracles. Trace: SEF-PHASE-009.
8. `P009-TASK-008` proves each pinned supported-pack adapter or retains an honest blocked/partial coverage row. Trace: SEF-PHASE-009.
9. `P009-TASK-009` updates operator and developer documentation from verified behavior. Trace: SEF-PHASE-009.
10. `P009-TASK-010` completes Forge-first phase integration and, whenever proxy capture, projection, or behavior changes, the applicable exact-digest proxy artifact integration and network-origin fixture required by the master. Trace: SEF-PHASE-009.

**Required evidence**

- Exact Forge 47.3.12 hook-resolution register, compiled candidate identity, and a versioned coverage matrix with family, entry point, terminal oracle, supported pack pin, adapter ID/version, outcome classes, and limitation.
- Real dedicated-server mutation fixtures with independently inspected world, inventory, item-handler, entity, command-result, and journal evidence; cancellation, simulation, same-tick reversal, changed explosion list, fake-player, and delayed-work counterfixtures.
- A sanitized audit diagnostic packet, `status` and `off` receipts, coverage registrations, cleanup receipts, and all retained source/digest evidence.

**Exit criteria**

- No known mandatory phase-owned defect remains. Every such defect must be fixed and its affected evidence rerun before phase closure.

- Every declared capture-family matrix row is `SUPPORTED`, `PARTIAL`, or `UNSUPPORTED` with a testable reason. No family is silently omitted, sampled, or called applied from intent alone.
- Each `SUPPORTED` vanilla and pinned-mod row has an independent real-path oracle. Any inability to resolve an actual mutation boundary remains an explicit coverage gap and blocks a false supported claim.
- All accepted authoritative movement records are emitted without sampling, same-tick state transitions remain distinct journal events, and capture never bypasses the approved Phase 008 redaction and durability pipeline.
- No known mandatory Phase 009 defect remains. This does not close SEF-REQ-027, restoration work, or whole-product parity.

## Inputs and Upstream Contracts

| Input or contract | Provider | Required state | Validation | Failure behavior |
|---|---|---|---|---|
| SEF-IF-009 journal and coverage | SEF-PHASE-008 | Versioned `append` and `registerCoverage` preserve EventKey, field policy, redactions, watermarks and gaps. | Exact interface digest and real append receipt. | Stop capture enablement and report `JOURNAL_UNAVAILABLE` or `COVERAGE_GAP`; never fall back to existing lossy sinks. |
| SEF-IF-001 identity/world | SEF-PHASE-001 | Actor, Session, WorldRef, and generation remain authoritative. | World-generation and actor-source fixture. | Emit no false location/actor; classify unknown or refuse affected entry. |
| SEF-IF-002 diagnostics | SEF-PHASE-001 | Default-off scoped capture can be enabled, inspected, and disabled by console. | Registered target, `on audit <target> 60`, `status`, `off` self-test. | Do not begin diagnostic-dependent proof until its local signal is delivered. |
| Exact Forge and pack artifacts | EXT artifact registers and Phase 000 inventory | Forge 47.3.12, Java 17, mappings, and each supported-pack pin/digest match the candidate. | SHA256/SHA512 and resolved dependency source/class/member records. | Mark source-only hook evidence unverified; do not infer a universal hook or adapter compatibility. |

## Outputs and Downstream Contracts

| Output or contract | Consumer | Guaranteed state | Compatibility or versioning | Evidence |
|---|---|---|---|---|
| Capture adapter registry and coverage matrix | SEF-PHASE-010 investigation and SEF-PHASE-015 verification | Every declared family has a versioned coverage state, adapter identity, boundary, and gap reason. | `adapterId` and `adapterVersion` are immutable per event; a changed adapter registers a new version. | Matrix-to-real-fixture trace and `registerCoverage` receipt. |
| Applied AuditEvent stream | Phase 010 lookup and Phase 011 restoration planning | Outcome, before/after, actor, initiator, confidence, cause and redactions reflect only observed terminal state. | SEF-IF-009 schema/version and EventKey ordering. | Independent oracle matches ordered events and same-tick distinct keys. |
| Cause-group model | Phase 011 fixed restoration jobs | Causal groups and parent links are bounded metadata, not restoration authorization. | Unknown attribution is explicit and never coerced to a player. | Fake-player/concurrent-machine/delayed-work fixture. |
| Support documentation | Operators and Phase 016 documentation rehearsal | Exact capture limits, coverage limitations, and sanitized collection procedure are documented. | Documentation follows approved implementation and phase merge. | Link check and runbook rehearsal. |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
|---|---|---|---|---|---|---|
| P009-TASK-001 | SEF-REQ-023 | Resolve every proposed family hook against the exact Forge 47.3.12 dependency source, mappings, and compiled candidate. Implement only terminal mutation-boundary adapters and immutable snapshot handoff. | SEF-IF-001, SEF-IF-009, SRC-308 through SRC-313, exact artifact register. | Hook-resolution register and original Forge adapter boundary. | Planned Forge audit adapter component, SEF-IF-009. | Late-cancelled break/place, simulated handler transfer, changed explosion list, and terminal command failure have no false `APPLIED` event. |
| P009-TASK-002 | SEF-REQ-023 | Build family adapter contracts, cause scopes, and supported mod adapter registration. Require exact adapter/pack pins and no live game object crosses worker boundaries. | P009-TASK-001, SEF-IF-001, SEF-IF-009. | `adapterId`/version contract, confidence/cause policy, adapter inventory. | Planned capture registry and `AuditEvent` causal fields. | Concurrent fake-player and delayed callback fixtures cannot inherit a stale player cause. |
| P009-TASK-003 | SEF-REQ-023 | Publish the complete matrix for blocks/block entities, containers/item flows, natural/automated changes, players/entities, interactions, commands/communications, administration/network lifecycle, and movement. | P009-TASK-001, P009-TASK-002. | Versioned `SUPPORTED`/`PARTIAL`/`UNSUPPORTED` matrix with terminal oracle and opaque-mod gap. | CoverageDescriptor, SEF-IF-009 registration. | Registry receipt exists for every row; unknown mod internal state has visible `UNSUPPORTED` or `PARTIAL` reason, never an implied universal hook. |
| P009-TASK-004 | SEF-REQ-023 | Capture each accepted authoritative movement change without sampling and retain per-transition before/after snapshots, including same-tick reversals as distinct events. | P009-TASK-001, P009-TASK-003, exact 47.3.12 resolution. | Movement adapter and same-tick event-key rule. | Planned movement capture adapter, `AuditEvent.before/after`, EventKey. | Two accepted distinct same-tick movements produce ordered distinct keys; denied/cancelled movement is not `APPLIED`; no rate or sampling gate suppresses a declared accepted movement. |
| P009-TASK-005 | SEF-REQ-023 | Implement bounded cause groups for player, console, system, machine, and fake-player actions; make direct, propagated, inferred, and unknown confidence mechanically distinct. | P009-TASK-002, P009-TASK-004. | Cause context lifecycle, parent links, and clear-on-boundary rules. | Actor/initiator/confidence/causeId/parentEvent in SEF-IF-009. | Parallel machines, nested action, fake player, and delayed task fixtures preserve correct cause or explicitly `UNKNOWN`. |
| P009-TASK-006 | SEF-REQ-023 | Add typed capture diagnostics, target registration, source-unit self-test, and recovery controls before real fixture capture. It depends only on the implementation outputs and declared targets of P009-TASK-001 through P009-TASK-005, not their real-world acceptance. | P009-TASK-001 through P009-TASK-005 implementation outputs, SEF-IF-002. | `audit.capture` signals and support self-test. | SEF debug category and planned troubleshooting guide. | Ready runtime then `sef debug on audit <registered-target> 60`, `sef debug status`, harmless self-test, `sef debug off <capture-id>`, status/no-new-event check, then re-enable a new capture for actual stimulus. |
| P009-TASK-007 | SEF-REQ-023 | Exercise vanilla real entry points through a no-GUI dedicated server and independent state oracles. | P009-TASK-001 through P009-TASK-006, Phase 008 journal. | Sanitized real-path evidence and failure fixtures. | Forge event adapters, journal, coverage matrix. | World, inventory, entity, and command oracles cover cancellation, simulation, partial flow, changed explosion candidates, terminal command outcome, protected authorized full-content retention, credential redaction, and same-tick reversal. |
| P009-TASK-008 | SEF-REQ-023 | Exercise each selected pinned supported-mod adapter using its exact pack artifact and declared capability boundary. | P009-TASK-002, P009-TASK-003, P009-TASK-006. | Per-pack adapter proof or explicit partial/unsupported record. | Supported adapter registry and coverage descriptors. | Independent machine/container/item oracle proves actual non-simulated transfer; opaque internal mutation makes no claim and carries its reason. |
| P009-TASK-009 | SEF-REQ-023 | Document verified capture families, confidence, cause groups, adapter pins, movement coverage, content/redaction limits, and visible gaps. | P009-TASK-003, P009-TASK-006 through P009-TASK-008. | README/docs index/topic and troubleshooting updates. | `README.md`, `docs/README.md`, audit and diagnostics documentation, planned paths only until discovered. | Documentation link check and runbook replay against verified candidate; unsupported hooks are described as gaps, not promises. |
| P009-TASK-010 | SEF-REQ-023 | Prepare sequential Forge phase integration evidence and downstream contract handoff. Perform applicable proxy artifact build, verification, and integration whenever this phase changes proxy capture, projection, or behavior, including network-origin lifecycle or administration capture; a signature-only condition is insufficient. | P009-TASK-007 through P009-TASK-009, existing proxy protocol harness, master integration rules. | Signed Forge evidence and, when behavior/projection changed, paired proxy digest and integration receipt. | Forge product branch; velocity product branch only for an applicable proxy artifact change. | Checked PR merge commit, fetched product-base containment, signed tag, post-merge candidate rebuild, and network-origin proxy-harness/backend-receipt fixture when applicable. No proxy modification is invented where no proxy artifact behavior changes. |

For every work package, a failed adapter or oracle keeps its matrix row partial or unsupported and stops its supported claim. Pure serialization, diff, and redaction tests may run before real fixtures, but never replace the real world/item/mod entry-point oracle. P009-TASK-001 through P009-TASK-005 may implement and source-unit test before diagnostics exist, but every real table fixture and its acceptance belongs after P009-TASK-006. Tasks P009-TASK-007 and P009-TASK-008 may execute by family only after P009-TASK-006 completes; the final evidence packet is ordered by EventKey and fixture.

### Actionable Coverage Fixture Table

Every row below is mandatory vanilla behavior or a selected pinned supported-pack behavior. `UNSUPPORTED` is permitted only for opaque third-party internals after the stated actual adapter boundary is absent; it is never a relabeling escape for mandatory vanilla or selected supported-pack behavior. Each real action and its independent terminal oracle completes within 200 server ticks or 10 seconds after ready state, whichever is first. Timeout records the reason, ends the fixture, and runs cleanup.

| Family and declared action | Real entry point | Independent terminal oracle | Negative counterfixture | Owning task |
|---|---|---|---|---|
| Blocks and block entities, break/place/modify and attached or multiblock changes | Exact resolved Forge 47.3.12 player action and block-entity mutation boundary. | Direct post-action world scan plus block-entity serialized state, separate from adapter snapshot. | Cancelled break/place and one partially completed attached/multiblock change. | P009-TASK-001, P009-TASK-007 |
| Falling blocks and piston movement | Exact falling-entity landing and piston committed movement boundary. | Post-tick world positions/states at source and destination. | Cancel piston or remove landing support before settlement. | P009-TASK-001, P009-TASK-007 |
| Fire and explosion | Exact fire committed change and reconciled explosion detonation boundary. | Post-action affected-block world scan. | Extinguish/cancel fire and mutate the explosion affected list before terminal reconciliation. | P009-TASK-001, P009-TASK-007 |
| Liquid and bucket flows | Exact fluid/bucket accepted mutation boundary. | Source/destination fluid-state scan and player inventory delta. | Cancel bucket use or use an invalid destination. | P009-TASK-001, P009-TASK-007 |
| Growth, decay, crops, and trees | Exact Forge natural tick/growth/decay post boundary. | Scheduled post-tick block-state and crop/tree shape scan. | Prevent growth/decay before its terminal tick. | P009-TASK-001, P009-TASK-007 |
| Portals, snow, and sculk | Exact accepted portal/snow/sculk state-change boundary. | Destination world/dimension or changed block-state scan. | Denied portal transition or cancelled/prevented state change. | P009-TASK-001, P009-TASK-007 |
| Version-applicable eggs, archaeology, and decorated pots | Exact 1.20.1 capability/event boundary only when the vanilla feature is present in the pinned runtime. | Independent entity/block/inventory scan appropriate to the resolved feature. | Invalid use or cancelled resolution. | P009-TASK-001, P009-TASK-007 |
| Player container, cursor, and equipment | Actual menu slot/cursor/equipment commit boundary, not open/close. | Server inventory plus menu/cursor/equipment state snapshot after commit. | Open/close without slot mutation and rejected click. | P009-TASK-001, P009-TASK-007 |
| Hopper, pipe, dropper, and dispenser | Non-simulated exact selected handler/adapter transfer boundary. | Both endpoint inventories and count delta. | `simulate=true`, blocked endpoint, and partial transfer. | P009-TASK-001, P009-TASK-008 |
| Crafting, trading, smelting, and remainders | Exact accepted player or supported automation output extraction boundary. | Input, output, cursor, merchant, furnace, and remainder inventories. | Recipe/offer rejection, full output slot, and no automated-output claim from player hook alone. | P009-TASK-001, P009-TASK-007, P009-TASK-008 |
| Item create, merge, split, use, damage, and destruction | Exact committed item-stack mutation boundary. | Before/after server inventory or entity-item counts and serialized item state. | Simulated/failed use, cancelled destruction, and merge/split reversal. | P009-TASK-001, P009-TASK-007 |
| Entity spawn, damage, death, removal, passengers, and drops | Exact post-spawn, terminal damage/death/removal, passenger, and drop boundary. | Entity existence/health/passenger graph and independent drop inventory/entity scan. | Cancelled damage, nondeath removal, and removed drop before pickup. | P009-TASK-001, P009-TASK-007 |
| Accepted movement, rotation, vehicle, portal, and respawn | Exact accepted authoritative movement/rotation/vehicle/portal/respawn boundary. | Server authoritative location, rotation, vehicle state, world/dimension, and respawn result. | Rejected/cancelled transition and two same-tick reverse transitions. | P009-TASK-004, P009-TASK-007 |
| Both sign faces and interactions | Exact committed front/back sign edit and interaction boundary. | Independent serialized front/back sign text and target state scan. | Cancelled edit and interaction with no mutation. | P009-TASK-001, P009-TASK-007 |
| Full commands and public/private communications | Terminal command result and accepted public/private communication boundary. | Command result plus protected journal read under authorized fixture principal. | Parse/dispatch cancellation, terminal failure, unauthorized private read, and credential sentinel. | P009-TASK-001, P009-TASK-007 |
| Identity, network, moderation, vanish, permissions, configuration, and lifecycle | Existing proxy protocol harness and backend handler/authority receipt for genuine network-origin events; native backend entry for local events. | Backend independent receipt plus authority revision/session/visibility/config/visit record. | Forged/stale epoch, denied moderator/permission action, hidden recipient, invalid config, and duplicate lifecycle callback. | P009-TASK-002, P009-TASK-007, P009-TASK-010 |
| Selected pinned supported-pack behavior | Exact adapter hook declared by the selected pack pin and adapter contract. | Mod-owned observable endpoint state plus independent inventory/world/entity oracle. | Digest mismatch, simulated/partial path, and opaque internal mutation. | P009-TASK-002, P009-TASK-008 |
| Investigation and restoration consumers | Capture event and causal-group contract only. | Phase 010 query contract and Phase 011 restoration job prerequisites consume the recorded event later. | Verify this phase does not invoke lookup, preview, apply, or undo. | P009-TASK-003, P009-TASK-010 |

## Architecture and Implementation Boundaries

The Forge server thread owns live levels, block entities, capabilities, entities, commands, and player state. An adapter takes an immutable bounded before snapshot at its family-specific pre-boundary and an immutable terminal after snapshot at its committed boundary, then passes only those snapshots to the Phase 008 journal. A pre-event, simulation, cancellation, or planned mutation never becomes `APPLIED`; it is recorded only with its truthful nonapplied outcome where declared. A worker never reads or mutates a live `ItemStack`, entity, level, or capability. `append` is the only audit sink; credential schemas and typed sensitive fields are redacted before every sink. DEC-009 authorized public/private content is retained as protected journal data under its field policy, while diagnostics, alerts, and unauthorized reads receive a redacted or denied representation.

Each adapter emits the full SEF-IF-009 identity: actual outcome, `adapterId`, `adapterVersion`, world/session when available, actor and initiator, confidence, cause ID/parent event, snapshots, reversibility, sensitivity, field policy, and redactions. A causal group associates related observed steps but neither proves reversibility nor authorizes a later restoration action. Same tick is not a deduplication key: every independently committed transition receives its own ordered EventKey. De-duplicate only the same mutation observation when the family contract supplies a stable source identity; otherwise retain the observed distinction and classify uncertainty.

The matrix distinguishes `SUPPORTED` when exact pin plus independent actual mutation oracle exists, `PARTIAL` when the observable boundary is narrower than the family, and `UNSUPPORTED` when the state is opaque or there is no safe exact hook. Each partial/unsupported row names family, pack/profile digest, adapter ID/version where present, missing boundary, effect on snapshots/attribution, and user-visible gap. Container-open/close cannot masquerade as slot transaction capture. `IItemHandler` simulation, pre-event intent, and mutable explosion candidate lists cannot become applied capture. The exact 47.3.12 source/javadocs/mappings and a real compiled fixture select actual entry points; the cited 1.20.x source remains preliminary evidence only.

For item flow, both accepted non-simulated end states and count deltas are captured. Partial insertion/extraction is `PARTIAL` only when the terminal observed delta proves partial movement; simulation creates no applied event. Full item content is snapshotted only through the pre-sink redaction contract. Known credential schemas and typed sensitive fields are redacted before every sink; unknown unsafe content carries an explicit reason. This phase does not weaken DEC-009's full-content retention policy, and it never preserves a secret sentinel merely because a test needs evidence.

For movement, the coverage matrix declares the accepted authoritative movement classes resolved for Forge 47.3.12 and captures every accepted change in those classes. Diagnostic caps may limit debug output only, never mandatory audit capture. The phase makes no claim to capture every internal position calculation or invisible mod simulation.

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
|---|---|---|---|---|
| Late cancellation after a pre-event | Independent world state remains unchanged and `audit.capture.outcome` is `CANCELLED` or absent applied record. | Never classify intent as `APPLIED`. | Correct the family boundary, preserve matrix gap until rerun. | P009-TASK-007 break/place cancellation fixture with a 60 second capture window. |
| Simulated item transfer | Handler return and independent inventories show no committed delta. | Emit no applied transfer for `simulate=true`. | Clear any provisional cause group and rerun actual transfer. | P009-TASK-007 and P009-TASK-008 simulated then real transfer fixture. |
| Same-tick reversal | Two terminal state observations have different EventKeys/sequence and independent before/after values. | Keep both events ordered; never collapse by position/tick. | Fix deduplication boundary and replay fixture from fresh journal. | P009-TASK-004 real same-tick two-transition fixture. |
| Explosion list changes | Candidate list differs from actual post-detonation blocks. | Capture only reconciled committed block changes; unobserved candidates are not destroyed events. | Mark affected row partial if exact reconciliation cannot be established. | P009-TASK-007 mutable-list fixture and independent world scan. |
| Opaque mod internal mutation | Adapter has no exact terminal boundary or independent state oracle. | Register a visible partial/unsupported coverage gap with the pin and reason. | Do not invent a universal hook; add an adapter only after a pinned contract and proof. | P009-TASK-008 absence fixture and coverage receipt. |
| Stale cause leaks to machine/fake player | Signal actor/initiator/confidence disagrees with isolated fixture identity. | Clear scopes at thread, tick, callback, and asynchronous boundary; use `UNKNOWN` when no direct proof remains. | Drop the stale association, correct scope lifecycle, rerun concurrent fixture. | P009-TASK-005 concurrent machines, fake player, delayed callback fixture. |
| Credential or private-content leak | Credential sentinel scan of journal, SQL fixture, diagnostic output, alert mock, and retained packet, plus authorized protected-content and unauthorized-read fixtures. | Redact credentials before every sink. Retain DEC-009 authorized public/private text only in protected journal fields and deny/redact it for diagnostics, alerts, and unauthorized reads. | Stop fixture, remove exact owned evidence, rotate test data, repair pre-sink policy, rerun. | P009-TASK-007 source-unit and real communication/command fixture. |
| Diagnostic timeout or disabled capture | `capture.status` shows timeout/disabled and harmless subsequent action has no diagnostic record. | Stop debug records while mandatory audit remains unsampled and independent. | Re-enable a new registered capture ID only for the next bounded fixture. | P009-TASK-006 self-test and re-enable sequence. |

## Diagnostics and Debugging

**Requirement IDs:** SEF-REQ-023  
**Task IDs:** P009-TASK-001, P009-TASK-002, P009-TASK-003, P009-TASK-004, P009-TASK-005, P009-TASK-006, P009-TASK-007, P009-TASK-008, P009-TASK-009, P009-TASK-010  
**Controls:** Console-only authorized `sef debug on audit <registered-target> 60`, `sef debug status`, and `sef debug off <capture-id>` use the inherited SEF-IF-002 manager. Target registration precedes enablement; denied, absent, unavailable, stale, or over-limit requests return their typed error.  
**Signals:** `audit.capture` includes captureId, correlationId, EventKey, adapterId/version, family, outcome, actor source, initiator, confidence, causeId, parentEvent, tick, before/after digest, coverage state, reason, units, configGeneration, and candidateDigest.  
**Collection procedure:** The numbered runbook below registers cleanup, runs a source-unit diagnostic self-test, performs `on`/`status`/`off`, sanitizes it, then re-enables a fresh capture only for real stimulus and independent oracle inspection.  
**Headless verification:** On node-1 only after inspecting a no-GUI task graph, launch the exact Forge 47.3.12 dedicated-server/GameTest or server fixture with no client, display, renderer, or owner join. Its console drives fixture setup but never bypasses the action or permission being tested.  
**Client verification:** No Phase 009 capture claim requires a client. If a later uncovered residual claim requires actual input, rendering, or synchronization, it remains open for the verified laptop under the master Trident client policy; server evidence does not close it.
**Client audio isolation:** Leave audio settings unchanged. Every required client uses the master Section 14 Trident instance lifecycle on the verified laptop, with explicit owned instance, runtime, window/PID, discrete renderer and joined-world evidence. No Trident command or client runs on node-1. Do not create mute watchers or require a playback stream or mute proof. Server-only checks need no client. Verify exact owned process exit and instance cleanup after the final consumer.
**Budgets and privacy:** Debug is default-off and separate from unsampled audit: default 60 seconds, maximum 300 seconds, 200 events/second, 10,000 events, 8 MiB per capture, 1024 queued diagnostic events, and two simultaneous captures per process. Output stops with visible drop/truncation counts. No diagnostic SQL/I/O runs on the tick thread. Credential sentinels and unsafe fields are redacted before every sink. DEC-009 authorized public/private fixture text is retained only in the protected journal and authorized read fixture, while diagnostics, alerts, and unauthorized reads redact or deny it.  
**Regression and support:** Source-unit diagnostics precede real fixtures; regression includes denied/absent target, timeout, reload/restart, output bound, pre-sink credential sentinel, off-mode behavior, and independent-oracle capture. Update `docs/troubleshooting/diagnostics.md`, README, and docs index with sanitized collection and cleanup.

| Signal | Source and unit | Expected observation |
|---|---|---|
| `audit.capture.boundary`, `family`, `hook`, `outcome`, `beforeDigest`, `afterDigest` | Backend owner-thread adapter, one per observed terminal boundary | `APPLIED` has an independent committed-state oracle; cancellation/simulation cannot produce it. |
| `audit.capture.cause`, `actorSource`, `initiator`, `confidence`, `causeId`, `parentEvent` | Backend cause scope, one per emitted event | Concurrent fake player and delayed work are direct/propagated/inferred/unknown truthfully, never stale-player attribution. |
| `audit.capture.coverage`, `adapterId`, `adapterVersion`, `packDigest`, `state`, `reason` | Coverage registry, one per matrix registration/change | Supported pin has proof; partial/unsupported opaque path remains visible. |
| `audit.capture.movement`, `eventKey`, `tick`, `beforeDigest`, `afterDigest`, `accepted` | Accepted authoritative movement adapter, one per accepted declared change | No sampling; same-tick reversals retain distinct ordered keys. |
| `capture.status`, `events`, `bytes`, `dropped`, `remainingSeconds`, `output` | Diagnostic worker, per status operation | The 60 second self-test and disabled state expose actual bounds; mandatory audit remains independent. |

1. Before execution, discover the verified node-1 project anchor and exact candidate/runtime path. Record Forge 47.3.12, Java 17, mappings, pack artifact hashes, source commit, task graph, all fixture processes, database fixture, log/diagnostic paths, and exact disposable outputs. Register final cleanup before launch. Configure only the disposable server runtime with `eula=true`, read it back, start no GUI server, and confirm readiness within 120 seconds. When an owned SQL engine is used, require readiness within 60 seconds. On either timeout collect the bounded readiness diagnostic, stop owned processes, clean exact outputs, and leave the gate open.
2. Resolve a registered audit target from the matrix. From the owned server console run `sef debug on audit <registered-target> 60`, then `sef debug status`; assert default-off became one bounded backend capture with the inherited limits and write the returned capture ID/path under `<runtime>/logs/sef/diagnostics/<capture-id>.jsonl`.
3. Run a harmless source-unit diagnostic self-test that emits a nonsecret `audit.capture.coverage` record and checks typed fields/units. Inspect it by correlation ID and validate that no live game object or credential escaped, and that diagnostic output contains no protected fixture content. Run `sef debug off <capture-id>` followed by `sef debug status`, trigger one harmless matching observation, and prove no additional diagnostic record was written.
4. Sanitize and retain only the decisive self-test excerpt. Create a fresh registered capture with `sef debug on audit <registered-target> 60`, check `status`, then reproduce one real fixture through its genuine Forge entry point. The fixture must use normal permissions/actions rather than a console shortcut for the behavior under test.
5. Compare ordered journal records with an independent world, inventory, item-handler, entity, or command-result oracle within 200 server ticks or 10 seconds after action acceptance. Inspect `audit.capture` fields, EventKey sequence, before/after digests, outcome, cause confidence, coverage state, and Phase 008 watermarks. Perform the family negative/recovery fixture, including cancellation, simulation, timeout, and absent-target where applicable.
6. Run `sef debug off <capture-id>` and `sef debug status`; preserve only sanitized candidate hashes, matrix row, command, expected/actual, decisive excerpts, and coverage receipt. Stop only owned server/database/test processes within 30 seconds, verify their exit, remove exact disposable runtime/world/log/diagnostic/database outputs after their last consumer without symlink traversal, and verify absence. Record cleanup failures separately.

## Verification Matrix

| Requirement or task | Static or unit | Integration | Real workflow or runtime | Negative and recovery | Execution host and prerequisites | Evidence artifact |
|---|---|---|---|---|---|---|
| P009-TASK-001 | Exact 47.3.12 symbol/mapping compilation and immutable snapshot contract tests. | Journal append schema test against Phase 008 digest. | No-GUI server uses resolved real Forge entry points. | Late cancellation, simulation, command terminal failure, changed explosion list. | node-1 after inspected headless graph; exact Forge artifact and cleanup registration. | Hook register, compile log, sanitized event/oracle comparison. |
| P009-TASK-002 and P009-TASK-005 | Cause-scope stack/clear and confidence classification tests. | Coverage registration and actor/world contract test. | Concurrent machine, fake-player, nested and delayed work fixture. | Stale scope must become `UNKNOWN`, not a previous player. | node-1 no client; exact source/pack fixtures. | Causal trace and independent fixture identity record. |
| P009-TASK-003 and P009-TASK-004 | Matrix completeness and EventKey distinctness tests. | Adapter registry/version and movement append tests. | Accepted movement and same-tick reversal fixture. | Denied/cancelled movement, opaque state, duplicate observation. | node-1 no-GUI dedicated server; no laptop claim. | Coverage receipts, ordered journal segment, independent state scan. |
| P009-TASK-006 | Typed diagnostic field/limit/denial tests. | `on audit`/status/off manager integration. | Ready runtime, self-test, off/no-new-event, re-enable then real capture. | Absent target, denied permission, timeout, limit, reload/restart, output unavailable. | node-1 console; inherited 60 second default and caps. | Sanitized JSONL, status receipts, cleanup receipt. |
| P009-TASK-007 | Snapshot/redaction source-unit tests. | Journal and SQL fixture checks through Phase 008 sink. | Every mandatory vanilla row in the actionable coverage table, each with a 200-tick or 10-second terminal oracle. | Credential sentinel all-sink scan, protected authorized-content read, unauthorized diagnostic/alert/read denial, partial transfer, cancellation, simulation, reverse and explosion mutation. | node-1 dedicated server readiness within 120 seconds and isolated DB readiness within 60 seconds when used; no GUI client. | Independent world/inventory/entity/command oracles, protected read receipt, safe sink scan, watermarks. |
| P009-TASK-008 | Adapter pin/digest and declared capability contract tests. | Supported mod adapter registry integration. | Each selected pinned-pack machine/container transfer or declared opaque boundary. | Missing/changed digest and opaque internal mutation become visible partial/unsupported. | node-1 no-GUI, exact pack artifact/configuration; no client. | Pin manifest, coverage row, real oracle or explicit gap receipt. |
| P009-TASK-009 and P009-TASK-010 | Documentation/link and matrix trace checks. | Forge-first PR evidence plus applicable proxy artifact build/harness/integration when proxy capture, projection, or behavior changes. | Post-merge Forge rebuild and required re-run of affected headless fixture; network-origin backend receipt where applicable. | Changed proxy behavior/projection requires exact proxy retest; otherwise no fabricated proxy work. | Product branches follow master sequential integration after all phase proof and cleanup. | Merge/check/tag/rebuild evidence, documentation rehearsal, conditional proxy digest receipt. |

## Documentation, Operations, and Release

Update the audit documentation with a versioned capture coverage matrix and an explicit distinction between declared supported, partial, and unsupported observations. Document exact Forge/pack pins, adapter identifiers, outcome and confidence semantics, no-sampling accepted movement policy, causal groups, opaque-mod gaps, full-content/privacy behavior, and the fact that capture does not itself provide lookup or restoration. Update `docs/troubleshooting/diagnostics.md` with the exact registered-target audit sequence, caps, sink-safe sanitization, and cleanup. Link verified behavior from README and `docs/README.md`. Wiki changes wait for the approved merge. No release, production deployment, credential collection, or public compatibility claim is part of this phase.

## Risks and Evidence Invalidation

| Risk ID and owner task | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
|---|---|---|---|---|---|
| SEF-RISK-009, P009-TASK-001/P009-TASK-004/P009-TASK-007 | Exact terminal boundaries and independent oracles. | Outcome/snapshot mismatch, cancellation, simulation, same-tick reversal, changed explosion list. | Correct adapter or retain a visible coverage gap. | A hook/mapping/artifact change or any oracle mismatch. | Recompile exact 47.3.12 and rerun family real fixture. |
| SEF-RISK-010, P009-TASK-002/P009-TASK-005 | Scoped context, clear-on-boundary, explicit confidence. | Actor/initiator mismatch in concurrent fake-player/delayed fixture. | Clear association and emit unknown until corrected. | Scheduler/cause contract or adapter version change. | Rerun concurrency and cause-isolation fixtures. |
| SEF-RISK-008, P009-TASK-006/P009-TASK-007 | Use only Phase 008 journal and loss records. | Journal receipt/watermark/gap differs from expected. | Stop supported capture claim and repair upstream boundary. | Journal/interface/redaction digest change. | Revalidate Phase 008 integration and sink-path fixture. |
| SEF-RISK-011, P009-TASK-006/P009-TASK-007 | Pre-sink credential policy, protected journal access, and separate diagnostic/alert/read policy. | Any credential sentinel in any sink, or authorized fixture content visible in diagnostics, alerts, or unauthorized reads. | Stop, remove exact owned test evidence, repair and rerun; authorized protected-journal content is retained only for its authorized read fixture. | Policy/schema/diagnostic formatter change. | Repeat credential all-sink scan, protected full-content fixture, and unauthorized-read denial. |
| SEF-RISK-019, P009-TASK-006/P009-TASK-007 | Bounded immutable handoff and diagnostic caps; mandatory audit never samples. | `workMs`, backlog, bytes, drop/truncation fields. | Degrade visibly only through approved gap semantics, never silent sample/drop. | Workload/pack/candidate change. | Phase 015 representative-load evidence remains mandatory. |

## Phase Completion Packet

The phase packet contains the exact candidate and pack hashes, Forge 47.3.12 hook register, matrix and coverage receipts, independent-oracle results, source-unit diagnostic self-test, real-capture status/on/off receipts, sanitized sink scan, documentation/link checks, conditional shared-digest result, and per-host cleanup receipt.

## Next Transition

The Forge branch follows the master sequence: checked PR merge commit, fetched `forge-1.20.1` containment, signed annotated phase tag and post-merge rebuild. If this phase changes proxy capture, projection, or behavior, including a genuine network-origin lifecycle or administration capture path, Forge integrates first and the proxy artifact is built, harness-tested, integrated by checked PR merge commit, and retested against the exact approved digest. Verify the resulting `velocity-latest` commit and its signed annotated phase tag. Only after every applicable branch gate and cleanup passes may the cursor advance to Phase 010. No proxy modification is invented when no proxy artifact behavior changes. No Phase 010 lookup/restoration implementation is an entry gate or executed by this phase.

## Noncanonical Interface Projection

This derived projection is copied verbatim from the assigned interface evidence. It is not a second canonical contract.

```json
{
  "phaseId": "SEF-PHASE-009",
  "interfaces": [
    {
      "id": "SEF-IF-001",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Identity and world",
        "records": {
          "Actor": {"uuid": "UUID?", "source": "PLAYER|CONSOLE|SYSTEM|MACHINE|FAKE_PLAYER", "originId": "string", "permissionRevision": "u64"},
          "Session": {"playerId": "UUID", "proxyBoot": "UUID?", "connectionEpoch": "u64", "backendId": "string", "backendBoot": "UUID"},
          "WorldRef": {"backendId": "string", "worldGeneration": "UUID", "dimension": "resource_location", "registryDigest": "sha256"},
          "Location": {"world": "WorldRef", "x": "finite f64", "y": "finite f64", "z": "finite f64", "yaw": "finite f32", "pitch": "finite f32"}
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
      "id": "SEF-IF-009",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Audit journal and coverage",
        "records": {
          "EventKey": {"originId": "string", "journalEpoch": "UUID", "sequence": "u64", "eventId": "UUID"},
          "AuditEvent": {"key": "EventKey", "world": "WorldRef?", "session": "Session?", "kind": "enum_string", "outcome": "ATTEMPTED|DENIED|CANCELLED|APPLIED|PARTIAL|FAILED|UNKNOWN", "actor": "Actor", "initiator": "Actor?", "confidence": "DIRECT|PROPAGATED|INFERRED|UNKNOWN", "causeId": "UUID?", "parentEvent": "EventKey?", "utc": "Instant", "tick": "u64?", "before": "Snapshot?", "after": "Snapshot?", "sensitivity": "set<PUBLIC_METADATA|PRIVATE_CONTENT|HIDDEN_ACTIVITY|RESTORATION_PAYLOAD>", "fieldPolicy": "map<field_path,set<permission_class>>", "reversibility": "EXACT|CONDITIONAL|NONE", "adapterId": "string", "adapterVersion": "u32", "redactions": "Redaction[]"},
          "Watermarks": {"captured": "u64", "localDurable": "u64", "centralDurable": "u64", "queryVisible": "u64", "gaps": "Gap[]", "uncertainTail": "bool"},
          "Gap": {"first": "u64?", "last": "u64?", "reason": "enum_string", "categories": "string[]", "acknowledged": "bool?", "utcStart": "Instant?", "utcEnd": "Instant?"}
        },
        "methods": ["append(event:AuditEvent) -> Result<LocalDurabilityReceipt>", "ingest(batch:AuditEvent[]) -> Result<CentralCommitReceipt>", "registerCoverage(adapter:CoverageDescriptor) -> Result<CoverageReceipt>", "health(actor:Actor) -> Result<AuditHealth>"],
        "errors": ["JOURNAL_UNAVAILABLE", "CAPACITY_LOSS", "OVERSIZED_PAYLOAD", "REDACTED_NONREVERSIBLE", "SQL_UNAVAILABLE", "COVERAGE_GAP"],
        "ownership": "Origin owns sequence and local journal; central transaction deduplicates event key and watermark; no debug sampling applies."
      },
      "acceptance_ids": ["SEF-AC-024", "SEF-AC-026", "SEF-AC-031"]
    },
    {
      "id": "SEF-IF-015",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Lifecycle templates and durable visits",
        "records": {
          "LifecycleConfig": {"schema": "u16=1", "generation": "u64", "scope": "AUTO|LOCAL|NETWORK", "allowPlayerOverrides": "bool", "families": "map<JOIN|LEAVE|WELCOME|WELCOME_BACK,TemplateFamily>"},
          "TemplateFamily": {"enabled": "bool", "audience": "VISIBLE_PUBLIC|SUBJECT|AUTHORIZED_STAFF", "selection": "RANDOM_NO_IMMEDIATE_REPEAT|ROUND_ROBIN", "variants": "TemplateVariant[1..32]"},
          "TemplateVariant": {"id": "bounded_identifier", "lines": "TemplateLine[1..8]", "hoverLines": "TemplateLine[0..4]"},
          "TemplateLine": {"runs": "TemplateRun[1..16]"},
          "TemplateRun": {"template": "bounded_template_string", "role": "PRIMARY|LABEL|VALUE|SUCCESS|WARNING|ERROR|LOCATION|EDIT|METADATA", "bold": "bool"},
          "Visit": {"scopeId": "string", "playerId": "UUID", "firstAdmittedAt": "Instant?", "lastAdmittedAt": "Instant?", "lastCompletedAt": "Instant?", "activeVisitId": "UUID?", "activeSession": "Session?", "completedVisits": "u64", "baseline": "KNOWN_RETURNING|RECORDED_FIRST", "revision": "u64"},
          "LifecycleEvent": {"eventId": "UUID", "visitId": "UUID", "scopeId": "string", "session": "Session", "family": "JOIN|LEAVE|WELCOME|WELCOME_BACK", "readyBackend": "string", "world": "WorldRef?", "previousAdmittedAt": "Instant?", "previousCompletedAt": "Instant?", "configGeneration": "u64", "visibilityRevision": "u64", "cause": "ADMISSION_READY|DISCONNECT_CONFIRMED", "firstRecordedVisit": "bool"},
          "DeliveryClaim": {"claimId": "UUID", "eventId": "UUID", "family": "JOIN|LEAVE|WELCOME|WELCOME_BACK", "variantId": "string", "templateGeneration": "u64", "recipientId": "UUID", "recipientSession": "Session", "state": "CLAIMED|SUBMITTED|SUPPRESSED|EXPIRED|UNCERTAIN", "reason": "enum_string", "expires": "Instant"},
          "VisitDecision": {"visit": "Visit", "classification": "FIRST_RECORDED|RETURNING", "previousAdmittedAt": "Instant?", "previousCompletedAt": "Instant?", "events": "LifecycleEvent[]", "duplicate": "bool", "durableSequence": "u64"},
          "CompiledLifecycleSnapshot": {"generation": "u64", "effectiveScope": "LOCAL|NETWORK", "authorityId": "string", "configDigest": "sha256", "templates": "map<family_variant_id,BoundedLiteralTemplateAst>", "enabledFamilies": "set<JOIN|LEAVE|WELCOME|WELCOME_BACK>", "compiledAt": "Instant"}
        },
        "methods": ["compileLifecycle(config:LifecycleConfig) -> Result<CompiledLifecycleSnapshot>", "admitVisit(session:Session, readyBackend:string, world:WorldRef?, admissionId:UUID) -> Result<VisitDecision>", "closeVisit(visitId:UUID, session:Session, endedAt:Instant, reason:enum_string) -> Result<VisitDecision>", "claimDelivery(event:LifecycleEvent, recipient:Actor, recipientSession:Session) -> Result<DeliveryClaim>", "preview(actor:Actor, family:LifecycleFamily, variantId:string, subject:UUID?) -> Result<Message[]>"],
        "errors": ["INVALID_TEMPLATE", "UNKNOWN_PLACEHOLDER", "TEMPLATE_LIMIT", "LIFECYCLE_AUTHORITY_UNAVAILABLE", "STALE_SESSION", "DUPLICATE_LIFECYCLE_EVENT", "VISIBILITY_DENIED", "DELIVERY_UNCERTAIN", "PREVIEW_DENIED", "MIGRATION_CONFLICT"],
        "ownership": "Standalone backend owns LOCAL visits and delivery. NETWORK proxy owns one durable visit/claim ledger and emits once after backend readiness; backend suppresses duplicate lifecycle announcements. Commit claims before dispatch, never replay ambiguous client display."
      },
      "acceptance_ids": ["SEF-AC-036"]
    }
  ]
}
```
