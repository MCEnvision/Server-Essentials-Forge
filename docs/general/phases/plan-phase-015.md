# Phase 015 Execution Plan

> **Plan ID:** PLAN-PHASE-015  
> **Phase ID:** SEF-PHASE-015  
> **Owner:** Integrated verification  
> **Classification:** MANDATORY  
> **Master plan:** [plan.md](../plan.md)  
> **Phase sequence:** 015 of 016

## Purpose and Ownership

This phase closes the integrated assurance evidence for the fixed Forge and Velocity candidates. The master retains all product scope, requirement ownership, interfaces, acceptance criteria, performance limits and topology. This blueprint defines only the ordered final verification work. It does not substitute framework existence, helper tests, simulated players, journal-derived counts, downloaded bytes, or an earlier phase result for final evidence. `SEF-REQ-020` and `SEF-REQ-027` are canonical here; every other requirement is revalidated as a dependent acceptance obligation.

## Evidence-Based Entry State

| Evidence class | Area | Finding | Source or command | Freshness condition |
|---|---|---|---|---|
| OBSERVED | baseline | Forge baseline is 1.20.1, Forge 47.3.12, Java 17; Velocity candidate is Java 25 and the pinned adapter trio requires real compatibility proof. | SRC-002, SRC-104 through SRC-109 | Any artifact, dependency, common/protocol digest, loader or pack change invalidates affected rows. |
| OBSERVED | audit boundary | Forge intent and item-handler APIs do not alone prove applied mutation or real transfer. | FIND-104, FIND-105, SRC-308 through SRC-315 | Any adapter, hook or supported-pack revision change invalidates its capture row. |
| OBSERVED | parity boundary | CE24.1 is Bukkit-specific and a behavioral reference only; its applicable functions require original Forge evidence. | FIND-106, FIND-107 | Any parity inventory revision or uncovered feasible row reopens closure. |
| PROPOSED | dual SQL proof | MySQL 8.4.11 and MariaDB 11.4.13 are isolated verification targets, not owner production databases. | FIND-110, FIND-111, EXT-006, EXT-007 | Engine, driver, schema, TLS, migration, backup or artifact hash change invalidates engine evidence. |
| OBSERVED | host boundary | `node-1` is headless only. Real chat, input, rendering, tab and entity visibility need silent owned laptop clients. | EXT-001, SRC-004 | Desktop, discrete renderer, exact client, window to stream mapping or private endpoint change invalidates client evidence. |

## Scope Boundaries

### Included Scope

- `SEF-REQ-020`: final normal, denial, forged, replayed, stale, concurrent, offline, empty-backend, switch, restart, partial-commit, storage failure and recovery proof on one proxy and three backends.
- `SEF-REQ-027`: final audit coverage, parity, conservation, both SQL engines, restoration, presentation, lifecycle, local and proxy RTP, performance and cleanup proof.
- Maintain the complete 36-row `SEF-AC-001` through `SEF-AC-036` catalog. This phase proves product acceptance `SEF-AC-001` through `SEF-AC-020` and `SEF-AC-023` through `SEF-AC-036`, including retained/removal inventory `SEF-AC-001` and lifecycle `SEF-AC-036`. `SEF-AC-021` and `SEF-AC-022` remain Phase 016 delivery/integration-owned pending its final rehearsal and merge proof.

### Explicit Exclusions

- `SEF-PHASE-016` documentation endpoint and final paired integration are future consumers. This phase produces its evidence packet but does not claim their completion.
- Production rollout, public publication, live database access, credential copying, firewall changes, a client project, economy, custom UI and arbitrary pack support are excluded. In-scope mandatory defect corrections and verification-harness work are permitted on the current Phase 015 branch or applicable paired branch, never a historical phase branch, and must preserve the unchanged product contract.

## Phase Contract

### SEF-PHASE-015 — Complete Assurance Matrix

**Objective:** Prove every mandatory acceptance criterion against fixed, checked paired candidates using independent oracles, actual supported hosts and complete cleanup.  
**Owner:** Integrated verification  
**Dependencies:** SEF-PHASE-014, EXT-001, EXT-003, EXT-004, EXT-005, EXT-006, EXT-007  
**Canonical requirements:** SEF-REQ-020, SEF-REQ-027  
**Documentation and release impact:** Record only implemented, measured final behavior under `docs/verification/phase-015/`, update existing test and troubleshooting procedures with reproducible facts, and hand the packet to Phase 016.  
**Next transition:** SEF-PHASE-016, close documentation and paired integration endpoint.

**Entry criteria**

- All prior phases have checked merge receipts, resulting product-branch verification and required signed phase tags. The Forge and Velocity candidates, common/protocol digest, loader, adapter, dependencies, configuration generations, supported profile records and source commits match on both hosts.
- Required fixture accounts and roles are available: console operator, ordinary observer, authorized observer with vanish visibility, muted signer, home owner, consent target, and separate RTP requester. Do not copy credentials or bypass authentication. Missing roles keep only their rows open.
- Before every dependent proof, `sef debug on <category> [target] [seconds]`, `sef debug status`, and `sef debug off <capture-id>` pass exact authorization, console-safe grammar, target-absence, timeout, reload and output-limit self-tests.
- Confirm the current Phase 015 Forge worktree is the canonical plan anchor. The owner-approved `/mnt/hermes/projects/SefVelocity` may be used only as the same-repository linked worktree for the applicable `envy` Velocity branch rooted in approved `velocity-latest`. Recheck its established Phase 004 registration, contents, active ownership and current branch before reuse. An unallocated directory must be empty and unclaimed before initial registration; an existing linked worktree must not be emptied. Preserve user content, retain Forge common/protocol ownership and clean only explicitly owned disposable children.

**Implementation scope**

- Implement SEF-REQ-020, SEF-REQ-027 through the work packages and acceptance obligations below.

- Consume the completed candidate interfaces and prove, rather than reimplement, all mandatory workflows, inventories, performance budgets, recoveries and cleanup gates described in this phase. Trace: SEF-PHASE-015.
- Use the fixed one-proxy three-backend topology, the actual MySQL and MariaDB fixtures, and isolated silent laptop clients only for residual input, presentation, rendering, tab and entity-visibility claims. Trace: SEF-PHASE-015.

**Required evidence**

- A complete row-level `SEF-AC-001` through `SEF-AC-036` matrix, independent source/sink/cursor/item-entity/world oracles, both actual SQL engines, exact candidate and host bindings, measured workload results, fault/recovery evidence and per-host cleanup receipts.

**Execution order**

1. `P015-TASK-003` freezes paired candidates and validates diagnostic producers before dependent proof. Trace: SEF-PHASE-015.
2. `P015-TASK-001` executes complete topology, presentation and lifecycle assurance. Trace: SEF-PHASE-015.
3. `P015-TASK-002` executes audit conservation, parity, restoration and load assurance on both engines. Trace: SEF-PHASE-015.
4. `P015-TASK-004` executes local and proxy RTP fairness, capacity and recovery matrices. Trace: SEF-PHASE-015.
5. `P015-TASK-005` closes the catalog, applies mandatory defect corrections and assembles the completion packet. Trace: SEF-PHASE-015.

**Exit criteria**

- No known mandatory phase-owned defect remains. Every such defect must be fixed and its affected evidence rerun before phase closure.

- Every product-owned `SEF-AC-001` through `SEF-AC-020` and `SEF-AC-023` through `SEF-AC-036` has a linked passing final row at required fidelity, including all feasible CoreProtect parity rows and every registered command/result presentation row. The catalog also identifies `SEF-AC-021` and `SEF-AC-022` as Phase 016 pending final rehearsal/merge proof.
- Both actual SQL engines pass all capture, query, migration, restoration, undo, privacy and capacity rows; all client claims have silent laptop evidence; cleanup receipts exist for every host and fixture.
- Mandatory defects are fixed and their affected matrix and dependent rows rerun. No scope is reduced or deferred to Phase 016.

## Inputs and Upstream Contracts

| Input or contract | Provider | Required state | Validation | Failure behavior |
|---|---|---|---|---|
| Paired artifact provenance | Phases 001 through 014 | Exact Forge and Velocity commits, SHA256/SHA512, common/protocol digest, pinned adapters and profiles | Reject mismatched candidate before fixture setup | Stop affected matrix and rebuild/reintegrate exact pair. |
| Diagnostics | SEF-IF-002 | Default off, bounded and console-safe `sef debug on`, `status`, `off` with 60 second default, 300 second maximum, 200 events per second, 10,000 events, 8 MiB, 1024 queue and two captures per process | Producer self-test before each dependent family | Missing producer signal blocks that family, never use unbounded logs. |
| Network and travel | SEF-IF-005 through SEF-IF-008 | Empty bridge, authority epochs, profile relation, qualified homes and durable arrival receipts | Exact typed result/error and independent session/world observations | Fence/reconcile, never treat a callback or timeout as arrival. |
| Audit and restoration | SEF-IF-009 through SEF-IF-011 | Four watermarks, immutable keys/cutoffs, strict authorization, fixed jobs and conflict ledger | Independent source/sink/cursor/item-entity/world oracle | Publish gaps or quarantine uncertainty, never synthesize expected counts from the journal. |
| RTP | SEF-IF-012 through SEF-IF-014 | Persistent grid, safe candidate, fresh telemetry and lease fencing | Ledger, world and client arrival receipts | Release only proven non-arrival; quarantine uncertainty. |
| Lifecycle | SEF-IF-015 | One authority, durable visits/claims, current visibility, atomic config generation | UUID ledger, recipient counters and actual native chat | Suppress uncertain or stale dispatch; never replay historical display. |

## Outputs and Downstream Contracts

| Output or contract | Consumer | Guaranteed state | Compatibility or versioning | Evidence |
|---|---|---|---|---|
| Phase 015 assurance packet | SEF-PHASE-016 | Requirement to AC matrix, candidate bindings, defects, reruns, measurements, limitations and cleanup receipts | Bound to immutable pair, profile, config, SQL engine and host identities | Sanitized `docs/verification/phase-015/` packet. |
| Final parity and presentation inventories | Documentation and integration | Every feasible parity row and every registered command result is closed or a documented platform/version inapplicability has evidence | Catalog and command registration revisions are binding | Row-level owner, fixture, result and screenshot or console transcript. |
| Performance envelope | Operators and Phase 016 | Measured workload units, sample size, p95/p99, GC, fsync, backlog and query latency, with no invented values | Candidate and fixture specific | Raw sanitized samples plus calculation script/result. |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
|---|---|---|---|---|---|---|
| P015-TASK-001 | SEF-REQ-020, SEF-REQ-001, SEF-REQ-009 through SEF-REQ-020, SEF-REQ-030, SEF-REQ-036 | Run one-proxy, three-backend topology, presentation and lifecycle closure. | P015-TASK-003, SEF-IF-004 through SEF-IF-008, SEF-IF-015 | Correlated topology, client, presentation and lifecycle evidence. | Proxy, Forge dispatcher/chat, presence, moderation, visibility, homes, transfer, renderer, lifecycle. | Matrix below, with actual server and client oracles. |
| P015-TASK-002 | SEF-REQ-027, SEF-REQ-023 through SEF-REQ-031 | Run full capture, query, privacy, parity, restoration, undo, conservation and load assurance separately on MySQL and MariaDB. | P015-TASK-003, EXT-006, EXT-007, SEF-IF-009 through SEF-IF-011 | Dual-engine row results, watermarks, inventory conservation, job-cut and workload matrix. | Journal, adapters, SQL, query/export, preview/restoration, parity API. | Independent source/sink/cursor/item-entity/world counts, actual-engine restoration and declared load samples. |
| P015-TASK-003 | SEF-REQ-020, SEF-REQ-027 | Freeze pair manifest, validate all diagnostic producer controls, create exact fixture registry and cleanup register. | SEF-PHASE-014, EXT-001, EXT-003 through EXT-007, SEF-IF-002 | Candidate, role, host and resource receipt. | Pair manifest, `sef debug`, bridge, SQL fixtures. | Wrong hash/profile/config is rejected before execution; producer controls, then ready-target on/status/off, denial, absent target, timeout and reload produce typed signals. |
| P015-TASK-004 | SEF-REQ-020, SEF-REQ-027, SEF-REQ-032 through SEF-REQ-035 | Prove local and proxy RTP fairness, strict safety, capacity, telemetry and recovery. | P015-TASK-003, P015-TASK-001, SEF-IF-012 through SEF-IF-014 | Allocation ledgers, arrival and route receipts, measurements. | Grid authority, safety service, proxy routing and leases. | 401 parent cycles, six-parent 20 by 20 grid, 720 parent landing orders, smaller child models, local/proxy arrivals and fault cuts. |
| P015-TASK-005 | SEF-REQ-020, SEF-REQ-027 | Publish measured limits, close retained/removal, parity and presentation inventories, identify Phase 016-owned AC021/AC022 as pending, repair required defects and rerun affected rows. | P015-TASK-001 through P015-TASK-004 | Complete assurance packet, pending delivery/integration handoff and explicit known limits. | All candidate artifacts and verification documentation. | No invented measurement, no open feasible product row, no known mandatory defect, verified teardown. |

## Architecture and Implementation Boundaries

The assurance harness is a consumer only. It drives real command dispatchers, administrative bridge endpoints, dedicated server state, SQL engines, native chat, private packet paths and client input. The oracle is separate from each claimed producer: source inventory for removed/retained items, signed client input plus server terminal outcome for chat, proxy and backend session ledgers for transfer, world snapshots for mutations, source and destination inventories plus cursor and item entities for transfers, independently counted event fixtures for audit, and persisted grid/reservation records plus actual arrival for RTP.

For every run, bind source commit, candidate digests, shared digest, loader/dependencies, SQL engine/driver, configuration generation, profile/world generation, seed, role, host and decisive diagnostic correlation. A change in any binding invalidates that row and all dependent results. In-scope mandatory corrections and verification-harness work occur on the current Phase 015 branch or its applicable paired branch, followed by Forge-first approved common projection where applicable, required checks, private additive review subject to the established review-capability availability rule, merge commit, resulting product-branch verification and signed tag before Phase 016. Do not reopen a historical phase or skip the current cursor. This phase never closes a defect by exclusion, framework presence or a pre-fix journal.

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
|---|---|---|---|---|
| Forged, replayed, stale or oversized bridge frame with zero players | `bridge.decision`, handler mutation counter and empty-backend state | Reject before dispatch; zero authority/world mutation. | Fence epoch/key and retry only valid typed frame. | P015-TASK-001, 10 second observation window. |
| Signed muted chat or alias/private route | Signed client input, `command.decision`, server chat outcome and client transcript | Mute is enforced without signed-chat corruption; expiry permits valid signed message. | Restart/switch preserves absolute expiry. | P015-TASK-001, 30 seconds per route. |
| Vanished subject and observer roles | Tab/entity/suggestions plus recipient packet and independent visible-count oracle | Ordinary observer sees no subject or count leak; authorized observer sees allowed surfaces only. | Revoke permission and resync; stale cache must clear. | P015-TASK-001, two laptop clients and 20 second state convergence. |
| Home/consent callback, disconnect or restart after arrival authorization | `transfer.transition`, proxy session ledger, destination world fingerprint | Never claim success without durable arrival; stale session cannot mutate destination. | Reconcile or quarantine; no guessed release. | P015-TASK-001, every state cut and 30 second bounded reconciliation. |
| Lifecycle duplicate, transfer, crash or invalid reload | `lifecycle.decision`, UUID visit ledger, recipient count and native chat capture | No false welcome on failed admission/switch, one genuine reconnect welcome, current visibility, last-good generation. | Expire uncertain claim without replay; preserve known visitor. | P015-TASK-001, 10 second claim expiry and all four families. |
| Canceled/simulated/same-tick audit action | World/inventory snapshot before and after, source fixture counter and journal event | Applied event only for committed mutation; attempts/cancelled/simulated outcomes stay distinct. | Correct adapter and mark coverage gap until rerun. | P015-TASK-002, one tick and two-opposing-change fixture. |
| SQL outage, torn tail, full spool or loss-ledger failure | Watermarks, independent input counter, disk fixture and SQL state | Explicit gap or unknown extent; no silent sampling, no authority/RTP deletion. | Replay valid prefix or restore backup; only owned closed segment rotates. | P015-TASK-002, 60 second outage/restart. |
| Preview/apply/undo conflict or crash | World/item preimage and postimage, `restore.step`, job ledger | Preview mutates nothing; conflict stops; duplicate item creation never occurs. | Resolve uncertainty, then undo only the exact proven applied subset of a full or partial job. | P015-TASK-002 on both engines. |
| RTP reservation, reload, capacity or telemetry failure | Allocation/route ledgers, final world distance and ticket counters | No consumed turn before successful arrival, no distance relaxation, stale telemetry excluded. | Proven nonarrival releases; ambiguity quarantines. | P015-TASK-004, local and proxy matrices. |

## Diagnostics and Debugging

**Requirement IDs:** SEF-REQ-019, SEF-REQ-020, SEF-REQ-024, SEF-REQ-027, SEF-REQ-030, SEF-REQ-035, SEF-REQ-036
**Task IDs:** P015-TASK-001, P015-TASK-002, P015-TASK-003, P015-TASK-004, P015-TASK-005
**Controls:** Use exact delivered grammar `sef debug on <category> [target] [seconds]`, `sef debug status`, `sef debug off <capture-id>`, and `sef debug off all`; `/sef` is client syntax and console omits `/`. Require `sef.debug.manage`, an explicit authorized target when needed, default 60 seconds and maximum 300 seconds.
**Signals:** Capture SEF-IF-002 identity plus `command.decision`, `bridge.decision`, `authority.commit`, `transfer.transition`, `visibility.decision`, `audit.watermark`, `audit.loss`, `restore.step`, `rtp.allocation`, `rtp.safety`, `rtp.route`, `lifecycle.decision` and `capture.status` with units, desired, actual, reason and correlation.
**Collection procedure:** Follow the seven numbered steps below; source/unit self-tests may run before launch, but ready-target on/status/off occurs only after readiness. Every capture is disabled, redacted and cleaned after its final consumer.
**Headless verification:** On node-1 inspect actual Gradle task graphs first, then use no-GUI dedicated servers, console commands and real server entry points for bridge, authority, audit, SQL, grid and server-observable world claims. This does not prove client input, rendered components, tab or entity visibility.
**Client verification:** Laptop clients are required only for actual signed-chat input, native rich chat/actions/hover, observer tab/entity and suggestion visibility, private preview rendering, destination rendering and reconnect. A harness or server log cannot close these rows.
**Client audio isolation:** Before launch, use the discovered disposable isolated pinned-version instance and set its master audio output to zero before launch. Verify laptop desktop and discrete renderer, bind the exact owned `hyprctl clients -j` address, class, title and PID to the candidate process tree, correlate only that tree to its PipeWire or PulseAudio playback stream, mute the verified per-application stream with `wpctl` or `pactl`, and read back that it is muted before assertions. Recheck and mute every replacement stream after reload, device change or reconnect. Teardown stops the owned watcher, route and client, confirms streams disappeared, and removes the disposable audio state. Missing identity or mute proof stops the client and leaves the gate open.
**Budgets and privacy:** Debug is default off, capped at 200 events per second, 10,000 events, 8 MiB, 1024 queued events and two captures per process; redacts secrets, private text and addresses. Mandatory audit remains unsampled. Disabled diagnostic p95 CPU overhead must be at most 1 percent and enabled overhead at most 5 percent at declared workload.
**Regression and support:** Rehearse `docs/troubleshooting/diagnostics.md` against the candidate, including permission denial, absent target, timeout, reload/restart, rate/output limit, redaction, disable verification and sanitized packet retention.

| Signal | Source and unit | Expected observation |
|---|---|---|
| `bridge.decision` | Proxy/backend worker, frames and bytes | Rejection before handler and zero mutation for replay, stale, forged and oversize input. |
| `audit.watermark` and `audit.loss` | Origin/SQL worker, events, bytes and milliseconds | Captured, local durable, central durable and query-visible remain ordered with explicit gaps. |
| `restore.step` | Backend executor, steps and digests | Matching preimage applies once; conflict or crash uncertainty quarantines. |
| `rtp.allocation`, `rtp.safety`, `rtp.route` | Serialized backend/proxy, blocks, tickets, milliseconds | Actual landing alone commits; exact distance and current telemetry explain outcome. |
| `lifecycle.decision` | Authority and dispatch, UUID event/visit/claim state | Duplicate/stale callbacks suppress, transfer preserves visit, uncertain display is not replayed. |

1. Resolve exact candidate hashes, runtime directories, exact private endpoint, fixture seed, authorized roles/accounts, SQL engine and cleanup targets. Inspect actual task graphs, create owned disposable resources, configure and read back `eula=true`, launch the no-GUI server and confirm server readiness within 120 seconds, then launch proxy and isolated SQL fixtures and confirm each within 60 seconds. No console command or dependent capture precedes its ready target.
2. Run source/unit producer self-tests before launch where applicable. After the exact server/proxy/SQL target is ready, run `sef debug on <category> [target] 60` from console, check `sef debug status`, and verify capture identity, limits, output path and target authorization. Trigger a harmless matching event, then run `sef debug off <capture-id>` and `sef debug status`, repeat that event and prove capture stopped. Test denial, missing target and bounded output before the feature scenario. Start the scenario capture only after Step 3 has completed any required client setup; durable recovery status is not a default-on trace.
3. For any client row, finish laptop setup before that fresh capture. Verify desktop and discrete renderer, isolated prelaunch master audio zero, exact owned Hyprland window/PID and muted application stream. Use the pinned launcher’s discovered supported direct-connect or authorized desktop control to automatically join that exact private endpoint within 60 seconds. Prove on both sides that the intended authorized player entered the intended server world. Never copy credentials, force an owner join or use a synthetic client for signed chat or Velocity switching; a missing role, account or control leaves that required gate open.
4. Execute one real entry point and fixture stimulus. Use only the named natural player action for a measured permission/input/render claim; console prepares state but never bypasses it. Use the per-stimulus bounded wait declared by its matrix row.
5. Inspect correlated typed records, server/proxy state and independent oracle. Record expected invariant, actual outcome and whether a client claim remains open. Run the negative, crash, recovery and restart variant; after each restart prove diagnostics default off, then enable a fresh bounded capture after readiness and client setup before the repeated stimulus.
6. Run `sef debug off <capture-id>` and `sef debug status`, trigger one harmless matching event and prove off means no new capture record. Retain only sanitized decisive excerpts under `docs/verification/phase-015/`.
7. Stop only owned clients, watchers, servers, proxy and database fixtures; each shutdown has a 30 second bound. Verify process and stream absence, remove exact disposable paths without symlink traversal and record cleanup independently from pass/fail.

## Verification Matrix

| Requirement or task | Static or unit | Integration | Real workflow or runtime | Negative and recovery | Execution host and prerequisites | Evidence artifact |
|---|---|---|---|---|---|---|
| P015-TASK-003 | Manifest/schema/hash and diagnostic self-tests | Pair/common/protocol/profile rejection | Console enable/status/off after target readiness | Permission, absent target, reload, timeout, limit | node-1 no-GUI; laptop only later; fixed pair | Candidate and cleanup register. |
| SEF-AC-001 through SEF-AC-009 | Retained/removal inventory and JAR scans | Dispatch and artifact loading | Natural retained command and no-SEF-client login | Excluded economy/UI linkage fails closure | node-1 server, laptop client for login only | Action/removal inventory with client receipt. |
| SEF-AC-010 through SEF-AC-018 | Codec/revision state tests | Empty bridge, profile registry, authority | Same-pack, compatible-different, unknown and incompatible pack; signed chat; ban/mute; vanish; homes; consent | Forged/replay/stale, direct identity, offline, switch, restart | one proxy plus three node-1 backends; silent laptop roles | Correlated proxy/backend/client matrix. |
| SEF-AC-019 and SEF-AC-030 | Config/component/token tests | Console and both render adapters | Every command success, denial, validation, empty, partial, unavailable, stale and confirmation state | Injection, token theft/replay, revocation | node-1 plus laptop native chat/action evidence | Presentation inventory and screenshots/transcripts. |
| SEF-AC-036 | Compiler, variant/visit/claim properties | Local and network authority/bridge | Join, leave, welcome, welcome-back, transfer silence, genuine reconnect, preview | Failed login, banned login, duplicate/reordered callback, claim crash, invalid reload, vanished count | local server and one proxy/three backend topology; silent laptop recipients | UUID ledger, recipient counters, actual chat evidence. |
| P015-TASK-002, SEF-AC-023 through SEF-AC-029, SEF-AC-031 | Schema, redaction, parity and job invariants | Real MySQL then real MariaDB, migration and backup | Every declared capture family, query/filter/export, preview/apply/undo/give | Canceled/simulated/duplicate, outage, disk full, tail corruption, secret, conflict/crash | node-1 no-GUI and isolated engine fixtures; laptop only inspection/preview display | Per-engine coverage/parity/conservation/load sheets. |
| P015-TASK-004, SEF-AC-032 through SEF-AC-035 | Partition/permutation/restart properties | SQLite ledger, telemetry and lease matrix | Local safe landing then proxy three-backend landing | boundary/concurrency, reload/migration, stale samples, full capacity, ambiguity | node-1 server paths; silent laptop only actual landing/render input | Ledger, world distance, route and cleanup receipts. |
| SEF-AC-021 and SEF-AC-022 | Catalog completeness check | Evidence handoff only | Pending Phase 016 clean rehearsal, checked merge and tag proof | Old candidate evidence cannot substitute for final delivery/integration | no new runtime here | Explicit Phase 016 pending rows. |
| P015-TASK-005 | Aggregate traceability validator | Defect rerun dependency closure | Measured workload runs | Deliberate candidate/config/hash drift rejects old evidence | exact paired hosts and resources | Final assurance packet. |

### Required scenario detail

For topology, perform same-pack transfer, direction-specific compatible-different transfer, unknown and incompatible refusal before source movement; validate installed pair identity and reject profile/adapter/registry evidence drift. Cover zero-player backend bridge, forged/replayed/stale/oversize frames, key rotation, authority loss/reconnect, offline target, concurrent mutation, ban/login rejection, signed muted/unmuted public and private/alias routes, ordinary and authorized vanish observers, observer permission revoke, tab/entity/selector/suggestion surfaces, and rapid reconnect versus stale session.

For qualified homes and consent, use three backends with duplicate home names and dimensions, explicit server selection, deleted/renamed and replaced worlds, unsafe destination, source/target switch, disconnect at each transfer state, token theft, expiry, consent change, restart and durable receipt recovery. The state oracle is proxy session plus destination world fingerprint, not callback count.

For lifecycle, test all four independently enabled families in local and network scope, one to 32 variants, literal braces, unknown placeholder, control/bidi/markup, unavailable/private placeholder, seeded no-immediate-repeat and round-robin, list reorder/removal, migration baseline/name change, first/returning and previous completed visit, failed admission, backend transfer, genuine reconnect, duplicate/reordered events, vanish recipients/count, reload failure/last-good, preview nonmutation, queue overflow/expiry and every claim/dispatch crash boundary. Use native actual chat capture for presentation and a UUID ledger plus independent recipient counter for semantics.

For audit, enumerate every capture family in the approved matrix: blocks/block entities/attached and multiblock, falling/piston/fire/explosion/liquid/bucket/growth/decay/portal/snow/sculk/version-applicable content, containers/player inventory/cursor/equipment, capability simulation versus actual transfer, hopper/pipes/dropper/dispenser, crafting/trading/smelting/remainders, item creation/merge/split/use/damage/destruction, entity spawn/damage/death/removal/passengers/drops, accepted movement/rotation/vehicle/portal/respawn, signs, command/full communication, identity/session/network lifecycle, moderation/vanish/permissions/configuration, query and restoration. Every feasible parity row is tested. Platform/version-inapplicable rows state exact reason and evidence; unknown hooks remain visible coverage gaps.

For each audit item flow, independently count source slots, destination slots, cursor, dropped item entities and world state before and after, then compare to a fixture-owned transaction ledger. Never calculate expected conservation from `AuditEvent` or journal counts. Cover partial insert, full destination, hopper and machine race, fake player, onward crafting, duplicate callback, canceled action and same-tick reversal. Preserve ordinary protected full content in privacy fixtures while removing the credential sentinel before memory, spool, SQL, export, alert and diagnostic sinks. Repeat capture, lookup, export, coverage/watermark, backup/restore, manual and explicitly activated scheduled purge blocked by actual durable job dependencies, consumer pause with continued capture and honest backlog, explicit capture pause with visible gaps, acknowledged redundant segment rotation, unreplicated loss and uncertain extent, loss ledger failure, migration in both directions, preview, rollback, restore, cancellation, offline resource, registry missing and multi-resource crash cuts on actual MySQL and actual MariaDB. Alerts at 75 and 90 percent alone never trigger emergency deletion. Repeat manual-default scheduling across restart, policy activation/revocation, invalid reload, two competing backends, expired lease fencing, migration exclusion, new restoration selection racing deletion, crash cuts, cancellation and intentional-purge boundaries on both engines. Prove scheduled retention also works in standalone Forge with no proxy installed. Reconcile individual feature and API rows against the Phase 010 parity families; category headings alone cannot close parity.

Restoration uses a fixed selection, fresh authorization at preview and apply, and private ghost-preview clearing on expiry, reload, chunk unload/reload, world replacement, transfer and reconnect. Verify preview changes no world state. Test `undo` against the exact proven applied subset after uncertainty resolution, including a partial job, rather than all selected rows or completed jobs only. Test privileged recorded-item give is denied even under the broad audit wildcard until `sef.audit.give` is explicitly granted.

For RTP, test six parents and 20 by 20 child cells for 401 complete parent cycles with deterministic seed and declared permutation assertions, crossing the 400-child boundary. Exhaust all 720 physical landing orders for six parents and use smaller declared child models for exhaustive child permutations; never attempt arbitrary factorial million-cell grids. Cover A lands first, B lands second but commits first, then A receipt reconciles last so B remains actual-last. Cover parent and child cycle-boundary barriers, successful-only consumption, self-reservation exclusion, quarantined spatial exclusions after recent-window expiry, policy reload without generation reset versus explicit generation migration, prepared candidate expiry, queue deadline, 100,000 recent ledger backpressure, actual strict XZ distances, online and vanished-player separation, claims/hazards/biomes/chunks, no-proxy local mode, and every routing term `0.35 * meanMspt/50 + 0.20 * spikeFraction + 0.25 * (players+incoming)/playerCapacity + 0.15 * chunkWork/chunkCapacity + 0.05 * (1-min(eligiblePrepared/preparedTarget,1))`. Test stale/forged/unready telemetry, zero capacity, unequal capacities, burst admission, lease renewal/failover before authorization only, destination restart and uncertain arrival quarantine. Do not relax the 128 block default or consume failed turns.

## Performance and Capacity Proof

Before measurement, freeze the fixture manifest’s numeric supported-pack workload rates and capacity envelope: authorized-role/player count, events per second by capture family, item transfers per second, simultaneous queries, query latency profile, world/chunk work, fixed seed and hardware identity. Do not call an unspecified workload representative. For every selected engine, mode and workload, run 10 minutes warmup followed by 20 measured minutes, retain all 1,200 one-second resource summaries and every 20 TPS tick sample, and repeat three seeded replicates. The raw fixture manifest and all sanitized samples are evidence, not invented measurements.

Use four matched runs with identical pack, hardware and workload rates: instrumentation-elided baseline, diagnostics disabled, scoped diagnostics enabled, and separate audit or RTP control. Compute each one-second and tick-series added cost as candidate minus its matched baseline; publish nearest-rank p50, p95 and p99 over every retained sample, with the sample count, GC/allocation, fsync, backlog, replay and query latency. Debug max 300 seconds cannot cover a 1,200-second run as one capture: use bounded diagnostic slices or summary metrics explicitly separated from the audit stream, without cap evasion or mandatory-audit loss. Disabled diagnostic p95 CPU overhead must be at most 1 percent; enabled scoped diagnostic p95 CPU overhead at most 5 percent. Audit capture and RTP each must add at most 2 ms p95 server-thread work per tick at the declared workload. A failure requires optimization or visible bounded admission that preserves audit/RTP invariants, then rerunning the affected workload and dependent matrix; no sampling mandatory audit, early recent-ledger eviction or reduced separation is permitted.

## Documentation, Operations, and Release

Update only behavior that the final candidate proves: `README.md`, `docs/README.md`, `DOCUMENTATION.md`, `docs/troubleshooting/diagnostics.md`, and relevant existing audit, RTP, lifecycle, migration and verification topics. Document candidate-specific supported pack relations, both SQL engine results, capture gaps, retention/loss boundaries, restore limitations, lifecycle display uncertainty, exact diagnostic controls, measured capacity envelope and cleanup procedure. Phase 016 owns final release-facing reconciliation and post-merge wiki publication.

## Risks and Evidence Invalidation

| Risk ID and owner task | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
|---|---|---|---|---|---|
| SEF-RISK-019, P015-TASK-005 | Declared workload and unsampled audit/RTP budgets | p95/p99, GC, backlog, fsync/query samples | Fix or bounded admission without weakened invariants | Measurement and dependent capacity claims | Full affected workload and aggregate metrics. |
| SEF-RISK-020, P015-TASK-003 | Pair/common/profile/host binding before fixture | Digest, renderer, stream, branch and cleanup receipts | Reject stale evidence and rebuild exact pair | All affected matrix rows | Exact row and dependent rows. |
| SEF-RISK-005/006/007/021, P015-TASK-001 | Current session/visibility/claim fences | Typed diagnostic plus independent client/ledger oracle | Fence, reconcile or suppress; fix and rerun | Network, lifecycle and presentation rows | Named scenario family with fresh client capture. |
| SEF-RISK-008 through SEF-RISK-012, P015-TASK-002 | Independent oracles, dual engine and fixed jobs | Watermarks, gaps, secret sentinel, job/world/inventory diffs | Quarantine, restore/replay, fix adapter | Engine/family and aggregate parity/conservation rows | Both engines and affected family matrix. |
| SEF-RISK-014 through SEF-RISK-017, P015-TASK-004 | Durable actual-arrival and strict safety fences | Allocation, safety and route records plus world oracle | Proven release or quarantine, no reset | RTP cycle, safety or route rows | Full cycle or affected fault matrix. |

## Phase Completion Packet

- Exact paired source commits, Forge/Velocity artifacts, SHA256/SHA512, common/protocol digest, dependency/license/SBOM records, pinned adapter and SQL engine/driver identity.
- Complete 36-row traceability matrix: passing Phase 015 product rows `SEF-AC-001` through `SEF-AC-020` and `SEF-AC-023` through `SEF-AC-036`, plus explicit Phase 016 pending rows `SEF-AC-021` and `SEF-AC-022`; retained/removal inventory, parity catalog and presentation coverage inventory with no uncovered feasible product row.
- Per-fixture host, runtime, seed, role, config/profile/world generation, diagnostic capture ID, independent oracle, bounded wait, result, defect/rerun chain and sanitized decisive evidence.
- MySQL and MariaDB separately: capture, query, privacy, migration, backup/restore, purge/rotation/loss, preview/apply/undo/give and conservation result sheets.
- Local and proxy RTP property/world/telemetry/capacity sheets; performance raw samples and calculations; published limitation/gap report.
- Per-host cleanup register showing exact owned server/proxy/database/client/watcher processes stopped, streams gone, retained sanitized evidence readable and disposable paths absent. Cleanup failure remains an open gate.
- Applicable Forge-first paired checked PR, merge-commit, resulting branch verification and signed tag receipts already required before Phase 016. Do not create forward completion cycles.

## Noncanonical Interface Projection

The following is derived evidence only. It is not a competing contract.

```json
{
  "phaseId": "SEF-PHASE-015",
  "interfaces": [
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
    },
    {
      "id": "SEF-IF-009",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Audit journal and coverage",
        "records": {
          "EventKey": {
            "originId": "string",
            "journalEpoch": "UUID",
            "sequence": "u64",
            "eventId": "UUID"
          },
          "AuditEvent": {
            "key": "EventKey",
            "world": "WorldRef?",
            "session": "Session?",
            "kind": "enum_string",
            "outcome": "ATTEMPTED|DENIED|CANCELLED|APPLIED|PARTIAL|FAILED|UNKNOWN",
            "actor": "Actor",
            "initiator": "Actor?",
            "confidence": "DIRECT|PROPAGATED|INFERRED|UNKNOWN",
            "causeId": "UUID?",
            "parentEvent": "EventKey?",
            "utc": "Instant",
            "tick": "u64?",
            "before": "Snapshot?",
            "after": "Snapshot?",
            "sensitivity": "set<PUBLIC_METADATA|PRIVATE_CONTENT|HIDDEN_ACTIVITY|RESTORATION_PAYLOAD>",
            "fieldPolicy": "map<field_path,set<permission_class>>",
            "reversibility": "EXACT|CONDITIONAL|NONE",
            "adapterId": "string",
            "adapterVersion": "u32",
            "redactions": "Redaction[]"
          },
          "Watermarks": {
            "captured": "u64",
            "localDurable": "u64",
            "centralDurable": "u64",
            "queryVisible": "u64",
            "gaps": "Gap[]",
            "uncertainTail": "bool"
          },
          "Gap": {
            "first": "u64?",
            "last": "u64?",
            "reason": "enum_string",
            "categories": "string[]",
            "acknowledged": "bool?",
            "utcStart": "Instant?",
            "utcEnd": "Instant?"
          }
        },
        "methods": [
          "append(event:AuditEvent) -> Result<LocalDurabilityReceipt>",
          "ingest(batch:AuditEvent[]) -> Result<CentralCommitReceipt>",
          "registerCoverage(adapter:CoverageDescriptor) -> Result<CoverageReceipt>",
          "health(actor:Actor) -> Result<AuditHealth>"
        ],
        "errors": [
          "JOURNAL_UNAVAILABLE",
          "CAPACITY_LOSS",
          "OVERSIZED_PAYLOAD",
          "REDACTED_NONREVERSIBLE",
          "SQL_UNAVAILABLE",
          "COVERAGE_GAP"
        ],
        "ownership": "Origin owns sequence and local journal; central transaction deduplicates event key and watermark; no debug sampling applies."
      },
      "acceptance_ids": [
        "SEF-AC-024",
        "SEF-AC-026",
        "SEF-AC-031"
      ]
    },
    {
      "id": "SEF-IF-010",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Audit query and extension API",
        "records": {
          "AuditQuery": {
            "queryId": "UUID",
            "actor": "Actor",
            "scope": "AuthorizedScope",
            "filters": "TypedFilter[]",
            "cutoffs": "map<origin_epoch,u64>",
            "pageSize": "u16",
            "cursor": "opaque128?",
            "includeSensitive": "bool"
          },
          "QueryPage": {
            "queryId": "UUID",
            "events": "AuditEventView[]",
            "nextCursor": "opaque128?",
            "counts": "TypedCounts",
            "watermarks": "map<origin_epoch,Watermarks>",
            "coverage": "CoverageDescriptor[]"
          }
        },
        "methods": [
          "lookup(query:AuditQuery) -> Result<QueryPage>",
          "export(query:AuditQuery, format:JSONL|CSV) -> Result<ExportReceipt>",
          "lookupPending(actor:Actor, world:WorldRef) -> Result<PendingView>",
          "registerAdapter(descriptor:CoverageDescriptor, adapter:AuditAdapterV1) -> Result<CoverageReceipt>"
        ],
        "errors": [
          "SCOPE_DENIED",
          "SENSITIVE_DENIED",
          "QUERY_LIMIT",
          "CURSOR_EXPIRED",
          "HISTORY_INCOMPLETE",
          "ADAPTER_UNSUPPORTED"
        ],
        "ownership": "Async bounded SQL service; every page, detail, hover and export reauthorizes; API rights separate append/query/restore."
      },
      "acceptance_ids": [
        "SEF-AC-025",
        "SEF-AC-029"
      ]
    },
    {
      "id": "SEF-IF-011",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Restoration job",
        "records": {
          "RestoreJob": {
            "jobId": "UUID",
            "actor": "Actor",
            "mode": "ROLLBACK|RESTORE|UNDO",
            "parentJob": "UUID?",
            "selectedEvents": "EventKey[]",
            "selectionDigest": "sha256",
            "cutoffs": "map<origin_epoch,u64>",
            "resources": "ResourceRef[]",
            "expectedCurrent": "map<ResourceRef,sha256>",
            "expires": "Instant",
            "conflictPolicy": "STOP",
            "state": "PREVIEW|CONFIRMED|RUNNING|PARTIAL|COMPLETED|CANCELLED|UNCERTAIN",
            "applied": "u64",
            "skipped": "u64",
            "failed": "u64",
            "unknown": "u64"
          },
          "RestoreStep": {
            "stepId": "UUID",
            "jobId": "UUID",
            "dependencies": "UUID[]",
            "beforeDigest": "sha256",
            "afterDigest": "sha256",
            "state": "INTENT|APPLIED|CONFLICT|FAILED|UNCERTAIN"
          }
        },
        "methods": [
          "preview(query:AuditQuery, mode:RestoreMode) -> Result<RestoreJob>",
          "apply(actor:Actor, jobId:UUID, selectionDigest:sha256, confirmation:opaque128) -> Result<RestoreJob>",
          "cancel(actor:Actor, jobId:UUID) -> Result<RestoreJob>",
          "undo(actor:Actor, completedJob:UUID) -> Result<RestoreJob>"
        ],
        "errors": [
          "PREIMAGE_MISSING",
          "LIVE_CONFLICT",
          "CONSERVATION_UNPROVEN",
          "REGISTRY_MISSING",
          "WORLD_REPLACED",
          "JOB_UNCERTAIN",
          "OFFLINE_RESOURCE"
        ],
        "ownership": "Durable job selection and step ledger; owning backend executes bounded causal groups; SQL and world are not one transaction."
      },
      "acceptance_ids": [
        "SEF-AC-028",
        "SEF-AC-029"
      ]
    },
    {
      "id": "SEF-IF-012",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "RTP allocation",
        "records": {
          "GridIdentity": {
            "gridId": "UUID",
            "generation": "u64",
            "world": "WorldRef",
            "minX": "i32",
            "maxX": "i32",
            "minZ": "i32",
            "maxZ": "i32",
            "parentRows": "u16",
            "parentColumns": "u16",
            "childRows": "u16",
            "childColumns": "u16",
            "policyRevision": "u64"
          },
          "Allocation": {
            "reservationId": "UUID",
            "operationId": "UUID",
            "session": "Session",
            "grid": "GridIdentity",
            "parentCycle": "u64",
            "parentId": "u32",
            "childCycle": "u64",
            "childId": "u32",
            "candidate": "Location?",
            "state": "RESERVED|PREPARED|ARRIVAL_AUTHORIZED|ARRIVAL_OBSERVED|COMMITTED|RELEASED|QUARANTINED",
            "expires": "Instant",
            "fencingToken": "u64"
          }
        },
        "methods": [
          "reserve(session:Session, gridId:UUID, operationId:UUID) -> Result<Allocation>",
          "prepare(reservationId:UUID, candidate:Location) -> Result<Allocation>",
          "commit(reservationId:UUID, receipt:ArrivalReceipt) -> Result<Allocation>",
          "release(reservationId:UUID, proof:NonArrivalProof) -> Result<Allocation>",
          "reconcile(reservationId:UUID) -> Result<Allocation>"
        ],
        "errors": [
          "INVALID_GRID",
          "CYCLE_BLOCKED",
          "NO_UNUSED_CELL",
          "PERSISTENCE_UNAVAILABLE",
          "FENCED",
          "ARRIVAL_UNCERTAIN"
        ],
        "ownership": "Backend serialized durable SQLite transaction; only committed successful landings consume parent and child turns."
      },
      "acceptance_ids": [
        "SEF-AC-032"
      ]
    },
    {
      "id": "SEF-IF-013",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "RTP safety and prepared candidates",
        "records": {
          "SafetyPolicy": {
            "revision": "u64",
            "minimumDistanceXZ": "finite f64",
            "recentWindowSeconds": "u32",
            "allowedBiomes": "resource_location[]?",
            "allowChunkGeneration": "bool"
          },
          "PreparedCandidate": {
            "id": "UUID",
            "gridGeneration": "u64",
            "parentId": "u32",
            "childId": "u32",
            "location": "Location",
            "policyRevision": "u64",
            "observedTick": "u64",
            "expires": "Instant",
            "chunkRevision": "u64"
          }
        },
        "methods": [
          "findCandidate(allocation:Allocation, policy:SafetyPolicy) -> Result<PreparedCandidate>",
          "validateAndLand(allocation:Allocation, candidate:PreparedCandidate, session:Session) -> Result<ArrivalReceipt>"
        ],
        "errors": [
          "TOO_CLOSE",
          "UNSAFE",
          "CLAIM_DENIED",
          "CHUNK_UNREADY",
          "CANDIDATE_EXPIRED",
          "QUEUE_TIMEOUT",
          "RECENT_LEDGER_FULL"
        ],
        "ownership": "Immutable prepared proposals spend no turns; final safety and actual teleport occur on backend thread and recheck all players including vanished players."
      },
      "acceptance_ids": [
        "SEF-AC-033"
      ]
    },
    {
      "id": "SEF-IF-014",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "RTP routing telemetry and leases",
        "records": {
          "LoadSample": {
            "backendId": "string",
            "backendBoot": "UUID",
            "sequence": "u64",
            "configGeneration": "u64",
            "sampleDurationMillis": "u32",
            "meanMspt": "finite f64",
            "p95Mspt": "finite f64",
            "spikeFraction": "finite f64",
            "players": "u32",
            "playerCapacity": "u32",
            "incoming": "u32",
            "chunkWork": "u32",
            "chunkCapacity": "u32",
            "eligiblePrepared": "u32",
            "preparedTarget": "u32",
            "ready": "bool",
            "profileDigest": "sha256"
          },
          "AdmissionLease": {
            "leaseId": "UUID",
            "operationId": "UUID",
            "session": "Session",
            "backendId": "string",
            "backendBoot": "UUID",
            "configGeneration": "u64",
            "expires": "Instant",
            "reservationId": "UUID?",
            "state": "HELD|ARRIVAL_AUTHORIZED|COMMITTED|RELEASED|UNCERTAIN"
          }
        },
        "methods": [
          "choose(session:Session, eligibleWorlds:string[]) -> Result<AdmissionLease>",
          "renew(lease:AdmissionLease) -> Result<AdmissionLease>",
          "finish(leaseId:UUID, receipt:ArrivalReceipt) -> Result<AdmissionLease>"
        ],
        "errors": [
          "TELEMETRY_STALE",
          "CAPACITY_FULL",
          "PROFILE_INCOMPATIBLE",
          "NO_ELIGIBLE_BACKEND",
          "LEASE_FENCED",
          "ARRIVAL_UNCERTAIN"
        ],
        "ownership": "Proxy selects backend first and serializes admission; destination owns allocation and final landing."
      },
      "acceptance_ids": [
        "SEF-AC-034",
        "SEF-AC-035"
      ]
    },
    {
      "id": "SEF-IF-015",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Lifecycle templates and durable visits",
        "records": {
          "LifecycleConfig": {
            "schema": "u16=1",
            "generation": "u64",
            "scope": "AUTO|LOCAL|NETWORK",
            "allowPlayerOverrides": "bool",
            "families": "map<JOIN|LEAVE|WELCOME|WELCOME_BACK,TemplateFamily>"
          },
          "TemplateFamily": {
            "enabled": "bool",
            "audience": "VISIBLE_PUBLIC|SUBJECT|AUTHORIZED_STAFF",
            "selection": "RANDOM_NO_IMMEDIATE_REPEAT|ROUND_ROBIN",
            "variants": "TemplateVariant[1..32]"
          },
          "TemplateVariant": {
            "id": "bounded_identifier",
            "lines": "TemplateLine[1..8]",
            "hoverLines": "TemplateLine[0..4]"
          },
          "TemplateLine": {
            "runs": "TemplateRun[1..16]"
          },
          "TemplateRun": {
            "template": "bounded_template_string",
            "role": "PRIMARY|LABEL|VALUE|SUCCESS|WARNING|ERROR|LOCATION|EDIT|METADATA",
            "bold": "bool"
          },
          "Visit": {
            "scopeId": "string",
            "playerId": "UUID",
            "firstAdmittedAt": "Instant?",
            "lastAdmittedAt": "Instant?",
            "lastCompletedAt": "Instant?",
            "activeVisitId": "UUID?",
            "activeSession": "Session?",
            "completedVisits": "u64",
            "baseline": "KNOWN_RETURNING|RECORDED_FIRST",
            "revision": "u64"
          },
          "LifecycleEvent": {
            "eventId": "UUID",
            "visitId": "UUID",
            "scopeId": "string",
            "session": "Session",
            "family": "JOIN|LEAVE|WELCOME|WELCOME_BACK",
            "readyBackend": "string",
            "world": "WorldRef?",
            "previousAdmittedAt": "Instant?",
            "previousCompletedAt": "Instant?",
            "configGeneration": "u64",
            "visibilityRevision": "u64",
            "cause": "ADMISSION_READY|DISCONNECT_CONFIRMED",
            "firstRecordedVisit": "bool"
          },
          "DeliveryClaim": {
            "claimId": "UUID",
            "eventId": "UUID",
            "family": "JOIN|LEAVE|WELCOME|WELCOME_BACK",
            "variantId": "string",
            "templateGeneration": "u64",
            "recipientId": "UUID",
            "recipientSession": "Session",
            "state": "CLAIMED|SUBMITTED|SUPPRESSED|EXPIRED|UNCERTAIN",
            "reason": "enum_string",
            "expires": "Instant"
          },
          "VisitDecision": {
            "visit": "Visit",
            "classification": "FIRST_RECORDED|RETURNING",
            "previousAdmittedAt": "Instant?",
            "previousCompletedAt": "Instant?",
            "events": "LifecycleEvent[]",
            "duplicate": "bool",
            "durableSequence": "u64"
          },
          "CompiledLifecycleSnapshot": {
            "generation": "u64",
            "effectiveScope": "LOCAL|NETWORK",
            "authorityId": "string",
            "configDigest": "sha256",
            "templates": "map<family_variant_id,BoundedLiteralTemplateAst>",
            "enabledFamilies": "set<JOIN|LEAVE|WELCOME|WELCOME_BACK>",
            "compiledAt": "Instant"
          }
        },
        "methods": [
          "compileLifecycle(config:LifecycleConfig) -> Result<CompiledLifecycleSnapshot>",
          "admitVisit(session:Session, readyBackend:string, world:WorldRef?, admissionId:UUID) -> Result<VisitDecision>",
          "closeVisit(visitId:UUID, session:Session, endedAt:Instant, reason:enum_string) -> Result<VisitDecision>",
          "claimDelivery(event:LifecycleEvent, recipient:Actor, recipientSession:Session) -> Result<DeliveryClaim>",
          "preview(actor:Actor, family:LifecycleFamily, variantId:string, subject:UUID?) -> Result<Message[]>"
        ],
        "errors": [
          "INVALID_TEMPLATE",
          "UNKNOWN_PLACEHOLDER",
          "TEMPLATE_LIMIT",
          "LIFECYCLE_AUTHORITY_UNAVAILABLE",
          "STALE_SESSION",
          "DUPLICATE_LIFECYCLE_EVENT",
          "VISIBILITY_DENIED",
          "DELIVERY_UNCERTAIN",
          "PREVIEW_DENIED",
          "MIGRATION_CONFLICT"
        ],
        "ownership": "Standalone backend owns LOCAL visits and delivery. NETWORK proxy owns one durable visit/claim ledger and emits once after backend readiness; backend suppresses duplicate lifecycle announcements. Commit claims before dispatch, never replay ambiguous client display."
      },
      "acceptance_ids": [
        "SEF-AC-036"
      ]
    }
  ]
}
```

## Next Transition

After every matrix row, defect rerun, measurement, evidence binding and cleanup receipt passes with no known mandatory defect, Phase 016 may begin its declared documentation and paired integration endpoint. It must revalidate the fixed evidence bindings and never use this blueprint as proof of unperformed integration.
