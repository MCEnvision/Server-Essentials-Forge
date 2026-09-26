# Phase 014 Execution Plan

> **Plan ID:** PLAN-PHASE-014
> **Phase ID:** SEF-PHASE-014
> **Owner:** Network RTP
> **Classification:** MANDATORY
> **Master plan:** [plan.md](../plan.md)
> **Phase sequence:** 014 of 016

## Purpose and Ownership

This phase delivers optional proxy-first RTP routing, configuration generation migration, operations, and recovery for SEF-REQ-034 and SEF-REQ-035. It consumes the planned allocation authority, strict destination safety, and authenticated arrival receipt contracts. The master remains the only product-contract authority. This blueprint details Network RTP work only and does not claim the complete assurance matrix or final measured operating limits reserved for Phase 015.

## Evidence Based Entry State

| Evidence class | Area | Finding | Source or command | Freshness condition |
| --- | --- | --- | --- | --- |
| OBSERVED | Existing RTP | CoreTeleportCommands.randomTeleport is radius sampling with bounded loaded-chunk checks, not persistent grids or proxy admission. | SRC-501, CoreTeleportCommands.java:370 through 439, SHA256 41bc31fe7d9fd325b748838a23f6256d6aaf131ae0c4b7185911e9496296d659 | Invalidate if the source fingerprint or Phase 012 and 013 contracts change. |
| OBSERVED | Existing safe arrival | SafeTeleportService is the local safety dependency, not proxy routing or durable admission. | SRC-502, SHA256 561421f4101de6d9e4df94054c61d950f0218b052e836b09f8d64d071425670c | Invalidate if its API or arrival contract changes. |
| OBSERVED | Current Forge configuration | Target server configuration is config/sef/common.toml through ConfigHandler. sef-vanish-server.toml is a separate vanish-only spec. | Target ConfigHandler.java:15 and ServerEssentialsForge.java:76 | Reinspect before schema implementation. No independent Forge RTP config path is implied. |
| PROPOSED | Routing | Proxy-first routing filters eligible backends, uses the canonical normalized score across six routing inputs, serializes leases, and lets destination receipts decide success. | DEC-016, FIND-034, FIND-109, SEF-RISK-017 | Invalidate only after owner-approved amendment to SEF-REQ-034 or SEF-REQ-035. |
| OBSERVED | Compatibility | Selected Velocity and Forge adapters are available artifacts, but combined runtime behavior remains a real readiness gate. | EXT-003, EXT-004, EXT-005, FIND-101 | Refresh artifact, advisory, license, and real matrix evidence before runtime proof. |
| OBSERVED | Upstream contracts | Arrival, grid, safety, bridge, configuration, policy, presentation, audit, and lifecycle signatures are frozen producer contracts. | SEF-IF-001 through SEF-IF-015 | Stop on incompatible signature rather than narrowing it locally. |

## Scope Boundaries

### Included Scope

- SEF-REQ-034 and SEF-AC-034. Optional proxy mode filters compatible ready destinations and the observed client reset capability before grid use, authenticates and evaluates fresh load, serializes admission, reserves destination and backend capacity before transfer, and accepts only destination arrival receipts as success. Local mode requires no external client adapter.
- SEF-REQ-035 and SEF-AC-035. Validated RTP configuration in existing Forge main configuration, immutable generations and explicit migration, real SQLite recovery, bounded queue and work controls, diagnostics and operational presentation, both-mode documentation, and specified local and proxy acceptance fixtures.
- The proxy companion receives only its typed routing adapter and companion configuration. Local mode remains backend operation and never falls back to proxy mode.

### Explicit Exclusions

- Phase 012 remains sole authority for partition arithmetic, independent parent and child bag state, and committed-turn consumption. This phase cannot reset or reinterpret either bag.
- Phase 013 remains sole authority for finding and final validation of safe candidates, player separation, claims, hazards, ticket management, and local landing. Proxy routing cannot weaken its safety conditions.
- The complete cross-feature assurance matrix, measured default tuning, audit conservation proof, and final product claim belong to Phase 015.
- No production network, database, proxy, public port, account, client profile, release, or deployment is changed by this plan.

## Phase Contract

### SEF-PHASE-014 — Optional proxy routing, generations and RTP operations

**Objective:** Deliver a backend-first optional proxy RTP admission service that uses fresh authenticated telemetry, holds both required reservations before switching, preserves generation and recovery truth, and proves three-backend arrivals without compromising local mode.
**Owner:** Network RTP
**Dependencies:** SEF-PHASE-013, EXT-003, EXT-004, EXT-005, EXT-009, EXT-010
**Supporting contract and risk dependencies:** SEF-IF-001, SEF-IF-002, SEF-IF-003, SEF-IF-004, SEF-IF-005, SEF-IF-008, SEF-IF-009, SEF-IF-012, SEF-IF-013  
**Canonical requirements:** SEF-REQ-034, SEF-REQ-035  
**Documentation and release impact:** Update the root README, docs/README.md, DOCUMENTATION.md, and implemented RTP, configuration, operations, migration, diagnostics, and verification topics during implementation. Describe actual behavior only. No release, production rollout, or publication is in scope.
**Next transition:** SEF-PHASE-015 begins only after both applicable product branch integrations, post-merge checks, and signed resulting phase tags are verified.

**Entry criteria**

- Phase 013 is merged through checked merge commits into every applicable selected product branch, its resulting branch verification and signed tag are present, and current phase branches start from those verified bases.
- Phase 012 allocation and Phase 013 safety contracts are at registry/schema version 1, including durable receipts, confirmed non-arrival proof, quarantine, immutable generations, strict recent-ledger behavior, and final destination revalidation.
- Pinned Velocity 4.2.0 build 30, Ambassador 1.4.5, ProxyCompatibleForge 1.3.1, client reset file 4657349, Forge 47.3.12, Java 25 proxy, Java 17 backend, common/protocol digest, license notices, current advisories, and profile relations are refreshed before runtime work.
- An isolated private one-proxy three-Forge-backend fixture and exact nested host anchors exist. A gate remains open when private endpoint, laptop control, discrete renderer, or Trident instance ownership cannot be verified.

**Implementation scope**

- Implement SEF-REQ-034, SEF-REQ-035 through the work packages and acceptance obligations below.

- Add optional mode local or proxy to the existing Forge main configuration source config/sef/common.toml through the planned ConfigHandler successor. Backend destinations map stable backend IDs to world/grid IDs. Each grid's inclusive world bounds and topology validate separately from playerCapacity and chunkCapacity. World area, file size, bounds, rows, and columns never become capacity. Trace: SEF-PHASE-014.
- Require complete destination record, valid mode, unique backend/world IDs, compatible profile, registered peer, positive capacities and prepared target, canonical-default routing tuning or validated configured tuning, legal bounds/topology, and legal queue/work limits before atomic config generation swap. Retain last-good snapshot on invalid reload. Trace: SEF-PHASE-014.
- Bounds or topology change creates or selects immutable generation, never reinterprets old cell numbers. Preview affected generation IDs, active/quarantined reservations, the full recent history for the same world identity, and explicit fairness-reset confirmation. Apply only after drain proves every cancellable provisional reservation has confirmed non-arrival. Preview/apply rechecks actor, current permission, config digest, old generation, operation/session binding, and explicit confirmation. Retain uncertain reservations, old generation, active quarantine destination exclusions, full same-world recent history, and receipts for reconciliation. Ordinary safety/policy reload preserves bags and recent window with no eviction or relaxation. Trace: SEF-PHASE-014.
- Sample ready backend every proposed two seconds into rolling ten-second window. Proxy computes age from local authenticated receipt monotonic time and excludes sample older than six seconds. Reject missing, malformed, unauthenticated, expired, wrong-recipient, wrong-peer, boot-epoch changed, duplicate/nonmonotonic sequence, implausible counter, missing denominator, or incompatible-generation telemetry. Trace: SEF-PHASE-014.
- Apply non-score eligibility first. Reject unready backend, absent configured destination/grid, profile mismatch, authority/bridge failure, invalid grid generation, stale telemetry, p95Mspt over 50, or players plus incoming at or over playerCapacity. Then calculate `0.35 * meanMspt/50 + 0.20 * spikeFraction + 0.25 * (players+incoming)/playerCapacity + 0.15 * chunkWork/chunkCapacity + 0.05 * (1-min(eligiblePrepared/preparedTarget,1))`. These are five terms covering six routing inputs, not six independently weighted factors. Positive playerCapacity, chunkCapacity, and preparedTarget are required. Zero prepared supply is valid: its normalized shortage is one and its weighted contribution is 0.05. Reject zero or negative denominator, NaN, infinity, impossible count relation, or absent telemetry. Exact ties sort stable backend ID. Trace: SEF-PHASE-014.
- Ten-percent hysteresis is retry stability only, never eligibility. Retain prior selected backend only when it passes every current filter and the newly ranked eligible best score is not at least ten percent lower. It cannot revive stale telemetry, override capacity, reserve new destination, or apply to a new operation. Trace: SEF-PHASE-014.
- Serialize admission through the existing proxy serialized authority and persist its effective incoming/lease state locally. Before next ranking, synchronously record prior outcome and update selected backend incoming count. Reconcile backend telemetry with live proxy HELD and UNCERTAIN leases by operation/session identity before calculating effective players plus incoming pressure; stale telemetry cannot overwrite live leases or double-count joined occupants. Acquire an idempotent proxy admission lease and the Phase 012 backend-local grid reservation before Velocity switch. There is no distributed atomic claim: compensate a confirmed pre-authorization partial acquisition, persist uncertainty across both restarts, and gate mutation/admission on persistence. Lease starts at 15 seconds, renews every 5 seconds only while healthy, and never exists over 30 seconds before ARRIVAL_AUTHORIZED. Lease epoch, backend boot, config generation, session, operation, and reservation fence stale messages. Trace: SEF-PHASE-014.
- Connection callback is not arrival. Destination uses Phase 013 final owner-thread safety and Phase 007 arrival authorization/finalization. Confirmed refusal/non-arrival releases both reservation and capacity. Crash after one reservation, during switch, authorization, actual landing, receipt, or lease finalization, timeout, lost callback, contradictory receipt, or storage failure after authorization makes both UNCERTAIN. Capacity and grid reservation remain quarantined and never expiry-released. Failover only occurs before authorization after confirmed cancellation of old admission and grid resources followed by fresh eligibility/rank. There is no local-to-proxy fallback, incompatible fallback, or unsafe spawn. Trace: SEF-PHASE-014.
- Add native shared HuskHomes-inspired calm semantic output with check/cross indicators, bracketed click/suggest/hover actions, labels, literal values, localization, and console equivalents for player queue/result, config preview/apply status, backend eligibility/score breakdown, reservation/lease state, recovery/quarantine, and budgets. Separate config preview, config apply, status, queue, budgets, recovery, and operations permissions. Destructive/reset confirmation binds operation, digest, session, and current permissions. No arbitrary command relay or plugin UI is added. Trace: SEF-PHASE-014.

**Execution order**

1. P014-TASK-001 delivers routing telemetry producer, authentic validation, canonical five-term six-input scoring, serialized admission, lease state, and unit/self-tests before routing proof. Trace: SEF-PHASE-014.
2. P014-TASK-002 delivers existing-main-config schema, generation migration/recovery, real SQLite fixtures, and config diagnostics before reload/recovery proof. Trace: SEF-PHASE-014.
3. P014-TASK-004 delivers scoped diagnostics, rich status/queue/budget/recovery/operations presentation, permissions, runbook, and producer self-tests before any real proof. Trace: SEF-PHASE-014.
4. P014-TASK-003 connects backend reservation plus proxy lease to transfer/arrival contracts, then performs actual diagnostic-dependent headless proof of P014-TASK-001, P014-TASK-002, and P014-TASK-003 at declared server-policy fidelity. Trace: SEF-PHASE-014.
5. P014-TASK-005 runs smallest Trident laptop actual player switch, cross-server arrival, native chat rendering, reconnect, and residual client proof after laptop join/setup capture refresh. Trace: SEF-PHASE-014.
6. P014-TASK-006 completes documentation, paired integration evidence, and cleanup without claiming Phase 015 assurance. Trace: SEF-PHASE-014.

**Required evidence**

- Deterministic properties show all six routing inputs affect the exact canonical five-term score, denominators reject, zero prepared supply is valid low supply, ties are stable, ten-percent hysteresis cannot alter eligibility, capacity differs from bounds, and incoming increments before next ranking.
- Real consistent SQLite backup and isolated restore compare leases/incoming, generations, bags, reservations, receipts, quarantine, and recent records, then world/session reconciliation proves last-good config, no expiry release, and truthful central-audit degradation. Database work remains off world/proxy event threads and persists before admission/mutation gates.
- Headless typed harness and dedicated servers prove routing, admission, SQLite, backend reservation, and receipt behavior only. They do not claim a genuine Velocity player switch. Smallest laptop gate proves real private proxy switch with source and destination join evidence, endpoint readiness, automatic join, both-side joined identity, arrival, reconnect, client synchronization, and native rich-chat residual presentation.
- Every fixture records candidate/common/protocol digest, exact host/runtime anchor, numeric readiness/connect/shutdown result, and cleanup. Mismatch invalidates dependent runtime evidence.

**Exit criteria**

- No known mandatory phase-owned defect remains. Every such defect must be fixed and its affected evidence rerun before phase closure.

- SEF-AC-034 passes. Six routing inputs affect the canonical five-term score. World size is not capacity. Stale, forged, unready, capacity-full, and incompatible destinations refuse. Serialized admission prevents stampedes, and only durable destination receipt commits.
- SEF-AC-035 passes. Reload never resets bags or truncates live recent window. Bounds/topology migration is explicit, drained, confirmed, and generation fenced. Ambiguous arrivals remain quarantined. Real SQLite recovery, diagnostics, bounded work, both modes, and operator documentation are proven.
- Completion packet contains paired PR merge receipts, post-merge product-branch verification, exact approved common/protocol digest, signed annotated tags, and cleanup. No Phase 015 proof is asserted early.

## Inputs and Upstream Contracts

| Input or contract | Provider | Required state | Validation | Failure behavior |
| --- | --- | --- | --- | --- |
| Identity/session/world/location | SEF-IF-001 | Session epoch, backend boot, world generation, registry digest, and finite location immutable per operation. | Compare lease/reservation/authorization/receipt fields exactly. | Refuse or quarantine stale/world mismatch. |
| Config/diagnostic control | SEF-IF-002 | Atomic expected-generation swap and default-off capture. | Validate schema and diagnostic self-test before fixtures. | Retain last-good config. |
| Policy/presentation | SEF-IF-003 and SEF-IF-004 | Policy rechecks and native Forge/Velocity rendering. | Exercise player/console/revocation/literal paths. | Deny safely without location disclosure. |
| Bridge/compatibility | SEF-IF-005 and SEF-IF-006 | Registered mTLS peer, epoch/sequence, bounded type, observed client reset capability and fresh profile relation. | Reject bad peer, boot, sequence, auth, size, missing capability or incompatible profile. | Typed refusal before grid or capacity reservation. |
| Transfer/arrival | SEF-IF-008 | Destination owns final authorization and receipt. | Match operation/session/world/location receipt. | Confirmed failure releases, ambiguity quarantines. |
| Allocation/safety | SEF-IF-012 and SEF-IF-013 | Generation-fenced reservation and final strict safety backend-owned. | Reserve before switch and final validate after join. | No transfer/consume/release without proof. |
| Audit linkage | SEF-IF-009 | Terminal routing event independent of debug capture. | Correlate outcome/reason/generation. | Report central degradation without route invention. |
| Runtime dependencies | EXT-003, EXT-004, EXT-005, EXT-009 | Pinned artifacts/notices/advisories/host rules revalidated. | Hash/startup matrix/actual driver behavior. | Preserve exact failure matrix and open gate. |

## Outputs and Downstream Contracts

| Output or contract | Consumer | Guaranteed state | Compatibility or versioning | Evidence |
| --- | --- | --- | --- | --- |
| SEF-IF-014 routing telemetry and leases | Phase 015 | Proxy routes backend-first and holds fenced lease only with grid reservation. | Registry/schema version 1. Unknown generation/boot/sequence/lease state rejects. | Unit, three-backend, crash, rejection evidence. |
| Generation migration record | Phase 015/operators | Old grid/recent/reservation/receipt records reconstructible until reconciled. | Immutable grid plus config generation, no in-place reinterpretation. | SQLite restart/migration fixture. |
| Operations presentation/diagnostics | Operators/Phase 015 | Scoped status explains eligibility, queue, budgets, recovery, capture without private locations. | Shared policy/presentation/diagnostic contracts, audit unsampled. | Self-tests, denial tests, sanitized capture. |
| Both-mode docs/packet | Phase 015 | Local remains independent, proxy explicit/bounded/recoverable. | Exact artifact/common/protocol digest binds pair. | Documentation, merged PRs, tags, cleanup. |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
| --- | --- | --- | --- | --- | --- | --- |
| P014-TASK-001 | SEF-REQ-034, SEF-REQ-035 | Implement backend telemetry producer and proxy router before consumers. Authenticate through SEF-IF-005, retain boot/sequence/config generation, enforce 2-second sample and 10-second window, reject age over 6 seconds, calculate exact canonical five-term score across six inputs, serialize live proxy incoming, stable ties, and retry-only 10-percent hysteresis. | SEF-IF-001, SEF-IF-005, SEF-IF-006, SEF-IF-012, SEF-IF-013, DEC-016, SEF-RISK-017. | Planned LoadSample producer, proxy serialized-authority admission state, producer unit/self-test report. | Planned backend RTP telemetry adapter, Velocity router, existing serialized proxy authority, SEF-IF-014. | Properties isolate meanMspt, spikes, players, incoming, chunk work, prepared supply, unequal capacity, p95/capacity refusal, valid zero prepared supply, missing denominator, stale/auth/replay/epoch/sequence rejection, ties, burst order, no lease/occupant double count, and hysteresis. |
| P014-TASK-002 | SEF-REQ-035 | Extend config/sef/common.toml with mode, destinations, bounds/topology, capacity, canonical-default or validated tuning, telemetry, lease, queue/preparation/recent limits. Build preview/apply migration with actor/digest/old-generation/session confirmation and explicit fairness reset after confirmed cancellable drain. Preserve active-quarantine destination exclusions, full same-world recent history, old receipts, and bags across ordinary safety/policy reload. | P014-TASK-001, ConfigHandler, SEF-IF-002, SEF-IF-012, SEF-IF-013, EXT-009, SEF-RISK-015. | Planned schema migration, generation record, last-good snapshot, preview/status, diagnostics, consistent-backup restore procedure. | Existing Forge config path, planned validator, Phase 012 backend-local SQLite grid authority, SEF-IF-002/014. | Real consistent SQLite backup and isolated restore compare leases/incoming, generations, bags, reservations, receipts, quarantine, and recent records before world/session reconciliation. Verify invalid reload last-good, bounds/topology confirmation, drain/cancel, uncertainty retention, old-token refusal, restart, backpressure, no reset/relaxation. |
| P014-TASK-003 | SEF-REQ-034, SEF-REQ-035 | Bind persisted proxy serialized-authority lease to Phase 012 backend-local reservation before switch. Begin 15 seconds, healthy renew every 5, maximum 30 pre-authorization. Use idempotent paired acquisition with confirmed pre-authorization compensation, never distributed atomicity. Persist uncertainty across proxy/backend restarts. Invoke transfer only after both resources exist and recheck on final owner thread. | P014-TASK-001, P014-TASK-002, P014-TASK-004, SEF-IF-003, SEF-IF-008, SEF-IF-012, SEF-IF-013, SEF-IF-014. | Planned admission-to-arrival coordinator and durable lease/reservation reconciliation record. | Existing serialized proxy authority, Velocity transfer adapter, Phase 012 backend grid authority, backend arrival adapter, SEF-IF-008/012/013/014. | After P014-TASK-004 control/self-tests, node-1 typed harness and private no-GUI proxy plus three Forge backends prove routing/admission/SQLite/backend behavior at server-policy fidelity only. Cover crash after first reservation, switch, authorization, landing, receipt, finalization, confirmed-cancel failover, stale session, duplicate receipt, persistence loss, and no expiry release. It is not a real Velocity player-switch claim. |
| P014-TASK-004 | SEF-REQ-035 | Implement RTP diagnostic producers and native shared HuskHomes-style operation surfaces before any real proof. Exact grammar is sef debug on rtp <grid-or-operation> 60, sef debug status, and sef debug off <capture-id>. Permission is sef.debug.manage, not grammar. All reason codes are ALLCAPS. Separate config preview/status, queue, budgets, recovery, operations permissions and redact private locations. | P014-TASK-001, P014-TASK-002, SEF-IF-002, SEF-IF-003, SEF-IF-004, SEF-IF-009. | Planned rtp.route, rtp.lease, rtp.generation, rtp.recovery, rtp.budget signals, rich check/cross bracketed action output, command self-tests, support runbook. | Debug service, command policy, Forge/Velocity presentation, SEF-IF-002 through SEF-IF-004. | Test on/status/off exact grammar, console status, denial, absent target, 60-second default/300-second maximum, 200 events/second, 10,000 events, 8 MiB, 1,024 queue, two captures/process, expiry, config reload, restart, off-no-new-record, literal injection, stale action, redaction, disabled overhead, audit-unavailable degradation. Establish target and complete control test before each fresh capture; reenable immediately before each stimulus. |
| P014-TASK-005 | SEF-REQ-034, SEF-REQ-035 | Run smallest actual Trident laptop proof after the diagnostic-dependent headless proof. Use real player connection to switch from source to destination through private proxy, prove source and destination joined identity, destination arrival receipt, client synchronization, reconnect fencing, and native rich player output. | P014-TASK-003, P014-TASK-004, EXT-001, EXT-003, EXT-004, EXT-005, paired candidate/common/protocol digest. | Sanitized actual player-switch, client synchronization, receipt/render evidence or explicit open-gate record. | Laptop isolated client, private proxy endpoint, source and destination Forge backends, native chat. | Laptop only. Follow master Section 14 for the owned Trident instance. Verify candidate parity, discrete renderer and exact window address/class/title/PID with hyprctl clients -j. Wait 60 seconds proxy/private connection, 20 seconds receipt, 30 seconds shutdown. Refresh capture after laptop join/setup and reenable after restart. Stop client on instance/identity/renderer/endpoint/source/destination join failure. |
| P014-TASK-006 | SEF-REQ-034, SEF-REQ-035 | Update implemented docs and assemble paired integration/cleanup evidence. | P014-TASK-001 through P014-TASK-005, documentation and GitHub workflow policy. | Documentation, verification packet, paired provenance, Phase 015 handoff. | README, docs index, technical/RTP/config/operations/migration/diagnostics topics, PR artifacts. | Review command grammar and links, paired artifact/common/protocol digest, checked merge commits, resulting branch verification, signed tags, and exact disposable cleanup. |

## Architecture and Implementation Boundaries

Proxy flow is player command and policy, proxy eligibility filter, serialized score/admission lease, destination grid reservation, Velocity switch, destination final safety/arrival authorization, durable ArrivalReceipt, then finish or quarantine. The proxy selects and reserves incoming capacity through its existing serialized authority and local durable state. Phase 012 persists backend grids locally. Audit SQL is not lease, incoming, grid, or recovery authority. The proxy never owns world mutation, grid consumption, safety, or final success.

Telemetry is evaluated only after bridge authenticity, peer/channel epoch, advancing sequence, config generation, expiry, plausibility, and proxy-local receipt freshness. LoadSample is reporting data, not authority. Backend owns grid, prepared candidate, safety, final teleport, consumption, and receipt. All changing messages carry operation ID, session, backend ID/boot, config/grid generation, fence, and correlation ID. A failed calculation refuses route.

Admission transitions are HELD, ARRIVAL_AUTHORIZED, COMMITTED, RELEASED, and UNCERTAIN. Reconcile telemetry representation with persisted live leases by operation/session identity before changing effective player-plus-incoming pressure, so stale backend report cannot overwrite a HELD/UNCERTAIN lease or double count a joined player. Ordinary lease expiry cannot release uncertainty. Paired acquisition is idempotent with confirmed pre-authorization compensation, not distributed atomicity; crashes after first reservation, switch, authorization, landing, receipt, or finalization remain durable and reconciled across both restarts. Local mode does not use proxy admission and cannot fall back. Central audit records outcome through SEF-IF-009 but never becomes routing authority. Debug is default-off and bounded; audit remains unsampled.

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
| --- | --- | --- | --- | --- |
| Stale/forged/replayed/epoch-changed/incomplete telemetry | rtp.route reason, peer/boot/sequence, local receipt age, denominator fields | Exclude before scoring. | Require complete authenticated current stream. | P014-TASK-001 negative fixture. |
| p95 over 50 or capacity full | p95, players, incoming, capacity | Refuse regardless of the canonical meanMspt score/hysteresis. | Fresh eligible sample only. | P014-TASK-001 saturation cases. |
| Concurrent selection | Serialized sequence/incoming transition | Update incoming before next rank. | Confirmed release only, uncertainty retained. | P014-TASK-001 burst plus P014-TASK-003 real fixture. |
| Grid reservation failure | Lease/grid correlation | Player stays source and confirmed unused lease releases. | Unavailable or fresh full rank. | P014-TASK-003 exhausted grid fixture. |
| Missing reset capability or changed client profile | `admit` capability/profile decision before route and independent source session/world receipt | Refuse without grid turn, lease or backend switch; keep local RTP available. | Use a qualified client profile or local mode, then retry normally. | Headless admission fixture plus real Trident client refusal and unchanged-source evidence. |
| Crash after first reservation, switch, authorization, landing, receipt, or lease finalization | Missing receipt/restart/session/persisted paired state | Both resources UNCERTAIN, never timeout release/failover. | Reconcile durable proxy lease, backend reservation, receipt, session, and destination. | P014-TASK-003 crash-cut fixture. |
| Invalid reload/ordinary reload | Validation or expected-generation conflict | Keep last-good. No reset, eviction, or relaxation. | Correct config or explicit migration. | P014-TASK-002 SQLite suite. |
| Bounds/topology migration | Preview shows actor/config digest/old generation/reservations/reset | Confirm after proven drain, retain active-quarantine exclusions and full same-world recent history. | Reconcile old state. | P014-TASK-002 migration/restart. |
| Central audit unavailable | SEF-IF-009 health/watermark | Truthful degraded status only. | Audit recovers independently. | P014-TASK-004 degradation fixture. |
| Laptop instance/client identity fails | renderer/window/PID/Trident instance binding absent | Stop owned client and open only visual gate. | Continue headless proof, never Prism fallback or node-1 graphics. | P014-TASK-005 setup record. |

## Diagnostics and Debugging

**Requirement IDs:** SEF-REQ-034, SEF-REQ-035
**Task IDs:** P014-TASK-001, P014-TASK-002, P014-TASK-003, P014-TASK-004, P014-TASK-005
**Controls:** Exact diagnostic grammar is sef debug on rtp <grid-or-operation> 60, sef debug status, and sef debug off <capture-id>. Permission sef.debug.manage authorizes each but is not command grammar. The default duration is 60 seconds, maximum 300 seconds, with 200 events/second, 10,000 events, 8 MiB, 1,024 queue entries, and two captures/process. Preview/apply, status, queue, budgets, recovery, and operations retain separate policy permissions.
**Signals:** SEF-IF-002 events carry capture/correlation ID, side, boot, sequence, config generation, candidate digest, desired/actual, ALLCAPS reason, units, and redacted routing/lease/generation values.
**Collection procedure:** Use the numbered procedure below. P014-TASK-001/P014-TASK-002 unit tests and P014-TASK-004 diagnostic producer/self-tests precede P014-TASK-003 headless proof. Capture setup refresh follows fixture setup and laptop join/setup. Establish target, complete control test, and reenable fresh capture immediately before each stimulus. Persist default-off startup recovery as status rather than misrepresenting it as capture.
**Headless verification:** node-1 only runs inspected genuinely no-GUI Gradle tasks, typed harnesses, SQLite fixtures, proxy, and dedicated Forge servers. Write/read eula=true before each Forge launch. Server/restart readiness waits maximum 120 seconds, proxy/private connection waits maximum 60 seconds, and owned shutdown maximum 30 seconds. This proves routing/admission/SQLite/backend behavior, not player switching.
**Client verification:** P014-TASK-005 alone proves actual private-proxy player switch, source and destination joined identity, client synchronization, destination receipt, reconnect, and native rich-chat. Server logs, synthetic receipt, and injected server-only results do not replace it.
**Client audio isolation:** Leave audio settings unchanged. Every required client uses the master Section 14 Trident instance lifecycle on the verified laptop, with explicit owned instance, runtime, window/PID, discrete renderer and joined-world evidence. No Trident command or client runs on node-1. Do not create mute watchers or require a playback stream or mute proof. Server-only checks need no client. Verify exact owned process exit and instance cleanup after the final consumer.
**Budgets and privacy:** Default-off scoped capture is duration/cap bounded and redacted. Routing obeys configured queue/work/ticket limits with typed refusal, never relaxed freshness/capacity/grid/distance. Mandatory audit is unsampled separately.
**Regression and support:** P014-TASK-004 control self-tests run before real proof. Test denied/absent-target/expiry/cap/reload/restart and off-no-new-record behavior. Retain only sanitized correlation-filtered packet with candidate/common/protocol digest, startup status, and conclusion.

| Signal | Source and unit | Expected observation |
| --- | --- | --- |
| rtp.route.sample_age_ms | PROXY local receipt monotonic milliseconds | Eligible only at or below 6000 ms. Older/missing gives TELEMETRY_STALE. |
| rtp.route.score | PROXY unitless canonical five-term score across six inputs | 0.35 meanMspt/50 plus 0.20 spikeFraction plus 0.25 (players+incoming)/playerCapacity plus 0.15 chunkWork/chunkCapacity plus 0.05 (1-min(eligiblePrepared/preparedTarget,1)). Stable ID resolves tie. |
| rtp.route.reject | PROXY ALLCAPS reason/count | P95_MSPT_LIMIT, CAPACITY_FULL, MISSING_DENOMINATOR, PROFILE_INCOMPATIBLE, or authenticity/freshness refusal. |
| rtp.lease.transition | PROXY state/lease age seconds | 15-second creation, 5-second renewals, maximum 30 seconds pre-auth, never expiry-released uncertainty. |
| rtp.destination.reservation | BACKEND reservation/fence/generation | Present before switch and exact operation/session/boot/generation match. |
| rtp.arrival.receipt | BACKEND receipt durable sequence/outcome | Matching ARRIVED commits. REFUSED proves release. Ambiguity quarantines. |
| rtp.generation.migration | BACKEND/PROXY generation/affected counts | Preview, confirmation, cancellable drain, retention of old quarantine/recent/receipts. |
| rtp.budget.queue | BACKEND/PROXY RTP queue/work/ticket counts | Default RTP queue of 64 entries, queue deadline of 10 seconds, preparation deadline of 15 seconds, 32 attempts, 32 prepared candidates per world, four tickets per backend, and two ms or 16 checks per tick. The separate diagnostic queue cap is 1,024 events. No ledger eviction or distance relaxation. |
| rtp.audit.degraded | BACKEND SEF-IF-009 health | Truthful central degradation without fake success/free authority. |

1. Register exact fixture anchors, candidate/common/protocol digest, endpoint, processes, output paths, temporary SQLite, server/proxy logs, capture directory, and cleanup targets. Inspect task graphs to prove no node-1 client. Write/read eula=true per dedicated Forge runtime.
2. Run P014-TASK-001 and P014-TASK-002 unit tests, then P014-TASK-004 source-level producer/control self-tests. Start proxy and three dedicated Forge backends and confirm server/restart readiness within 120 seconds and private proxy connection within 60 seconds before any console command. Read durable recovery outcomes through the implemented restricted RTP status/recovery surface. Independently use `sef debug status` to prove capture is off; capture status does not substitute for recovery state.
3. Establish a valid grid-or-operation target, complete denied/status/on/off control test, then immediately before each stimulus enable a fresh capture with sef debug on rtp <grid-or-operation> 60. Recheck status. Exercise no-client normal/stale/forged/saturated/failure/migration/restart/recovery paths through typed harness and real server handlers. Correlate proxy admission, backend reservation, receipt, SQLite, and world state by operation. Reenable capture after any restart.
4. For residual gate only, launch task-owned Trident laptop client, join source backend through proxy, refresh capture after laptop join/setup, then cause actual player switch to destination. Wait maximum 20 seconds for destination receipt. Verify source and destination joins, client synchronization, arrival, rich rendering, reconnect behavior. Do not use console to bypass player behavior.
5. Execute sef debug off <capture-id> and confirm off-no-new-record behavior. Sanitize retained evidence of locations, identity, addresses, and secrets.
6. Stop owned client/proxy/servers/database fixtures, confirm process exit and owned instance removal, remove only registered temporary runtimes/logs/captures/databases/downloads/traces/build output, verify absence, and report leftovers.

## Verification Matrix

| Requirement or task | Static or unit | Integration | Real workflow or runtime | Negative and recovery | Execution host and prerequisites | Evidence artifact |
| --- | --- | --- | --- | --- | --- | --- |
| SEF-REQ-034, P014-TASK-001 | Exact canonical five-term score properties across meanMspt, spikes, players, incoming, chunk work, prepared supply; ties, denominators, p95/capacity, hysteresis. | Bridge/profile/grid eligibility plus persisted serialized incoming reconciliation. | Headless proxy/three backend admission only, not player switch. | Replay, forgery, boot change, missing sample, zero prepared supply, burst, saturation/double count. | node-1 after no-client task graph and pinned pair digest. | Sanitized score/admission traces and independent counts. |
| SEF-REQ-035, P014-TASK-002 | Schema, generation conflict, unchanged reload, reset confirmation. | Consistent SQLite backup and isolated restore. | Dedicated restart compares leases/incoming, generations, bags, reservations, receipts, quarantine, recent records, then world/session state. | Invalid config, topology/bounds, canceled drain, uncertainty, ledger backpressure. | node-1 no-GUI, Xerial identity, nested temporary DB. | Preview/database/restore/restart/cleanup evidence. |
| SEF-REQ-034, P014-TASK-003 | Lease/cancellation/crash state machine. | Transfer adapter with SEF-IF-008/012/013. | Headless proxy/three backend routing/admission/SQLite/backend policy only, not actual Velocity player switching. | First-reservation, switch, authorization, landing, receipt/finalization crash, confirmed pre-auth failover, uncertainty, duplicate receipt. | node-1 private endpoint, eula readback, readiness 120 seconds. | Correlated proxy/backend/receipt bundle. |
| SEF-REQ-035, P014-TASK-004 | Exact grammar, permissions, 60/300 duration, rate/event/byte/queue/capture caps, literal/redaction. | Forge/Velocity native shared presentation/audit health. | Console status/recovery and player-safe result after proxy/backend readiness. | Denied, absent target, expiry, config reload, restart, off-no-new-record, audit unavailable. | node-1 no-GUI, default-off durable status then fresh explicit capture. | Sanitized JSONL/presentation snapshots. |
| SEF-REQ-034, SEF-REQ-035, P014-TASK-005 | Candidate/digest and renderer/Trident instance identity. | Source/destination joins, actual proxy switch, client synchronization, receipt, reconnect. | Trident laptop client through real private proxy from source Forge backend to destination Forge backend with native chat. | Endpoint/instance/renderer/session/source/destination join failure stops client/open gate. | EnVy laptop isolated matched runtime, discrete renderer, verified Trident instance binding, authorized endpoint. | Sanitized client visual/log/source-destination receipt/instance teardown. |
| SEF-REQ-035, P014-TASK-006 | Documentation links/grammar. | Pair digest and PR checks. | Result branch verification/tags. | Digest/check/review/cleanup failure blocks transition. | Approved branches only, no base direct push/deploy. | Merged PR receipts, commit/tag, docs, cleanup inventory. |

## Documentation, Operations, and Release

Update existing README, docs index, general documentation, and actual RTP/configuration/operations/migration/diagnostics topics with local/proxy behavior, capacity versus bounds, profile relation, freshness/score, queue/budget, rich permissions, preview/apply, fairness reset, quarantine/recovery, and troubleshooting. Documentation clearly distinguishes config preview/status, player queue, budget, recovery, and operations permissions; documents no-fallback behavior and that audit degradation is not routing success.

Before implementation reconcile milestone/issues/project/checks. Forge common is canonical. First merge checked Forge work into forge-1.20.1, verify the resulting Forge commit, and create/push its signed annotated tag. Project the exact approved common/protocol digest into velocity-latest, then build/retest the proxy against that digest, merge its checked proxy PR, verify its resulting commit, and create/push its signed annotated tag before the next phase. This is Forge-first sequential integration, not two parallel common copies or a whole-Forge-tree cherry-pick. The owner-selected forge-1.20.1 and velocity-latest branch exception remains. No direct base push, public release, production deployment, or wiki publication pre-merge.

## Risks and Evidence Invalidation

| Risk ID and owner task | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
| --- | --- | --- | --- | --- | --- |
| SEF-RISK-017, P014-TASK-001/003 | Fresh auth telemetry, canonical score, serialized live incoming, idempotent dual reservation. | Age/auth/epoch/sequence, score, lease, reservation. | Exclude/quarantine, no unsafe failover. | Routing/arrival after sample/schema/adapter change. | Rerun producer and three-backend fixture. |
| SEF-RISK-015, P014-TASK-002 | Immutable generation, actor/digest/old-generation preview/confirmation, proven drain. | Generation/migration/old receipt/reservation/recent history. | Last-good, retain active quarantine and full same-world recent history, reconcile. | Bounds/topology/recent/migration evidence. | Rerun consistent SQLite backup/restore and restart. |
| SEF-RISK-016, P014-TASK-003 | Destination final safety mandatory. | Safety/receipt reason. | Proven failure release or uncertainty quarantine. | Arrival after policy/world change. | Rerun safe-arrival/no-proxy regression. |
| SEF-RISK-007, P014-TASK-003/005 | Session/receipt fence, durable paired uncertainty, pre-auth confirmed cancel only. | Session/boot/operation/receipt/source/destination join. | Preserve source/resources until resolved. | Transfer/reconnect client proof after bridge/session change. | Rerun server and actual laptop switch gates. |
| SEF-RISK-019, P014-TASK-004 | Default-off bounded diagnostics and queue/work/ticket caps. | Cap/work/queue/ticket/degraded signals. | Unavailable/backpressure and sanitized packet. | Diagnostics/budget after producer/config change. | Rerun self-tests/fixture. |
| SEF-RISK-020, P014-TASK-005/006 | Pair digest, candidates, host instance/renderer, branch tags. | Digest/endpoint/identity/cleanup/tag mismatch. | Stop/block/rerun exact gate. | Any mismatched artifact/host/merge result. | Rebuild pair and repeat host proof. |

## Phase Completion Packet

- Source commits, common/protocol manifest, SHA256/SHA512, dependency/license/advisory check, and candidate digest for paired Forge/Velocity outputs.
- P014-TASK-001 and P014-TASK-004 self-test results, durable default-off startup result, and refreshed post-setup capture result.
- Real SQLite configuration/generation migration, crash/restart, recovery, last-good, old-receipt, bag/recent, and central-audit-degraded evidence.
- Private proxy plus three Forge backend server-policy receipt proving readiness, selection, persisted proxy admission, Phase 012 local grid reservation, SQLite/recovery, failover boundary, and quarantine. It is marked headless server-policy evidence, not player switching.
- Laptop packet with renderer/Trident instance/window/PID binding, endpoint/autojoin, source and destination joins, actual player switch/client synchronization, receipt, reconnect, rich-chat residual, shutdown/instance cleanup, or exact open gate.
- Updated actual documentation, sanitized support packet, Forge-first paired milestone/PR/check/review results, Forge merge/result verification/tag, exact approved common/protocol projection to velocity-latest, proxy build/retest/merge/result verification/tag, and no competing common copy.
- Per-host resource/cleanup receipt covering runtimes, DB, processes, owned instances, logs, screenshots, captures, traces, downloads, generated config, bytecode, output. Exact removal after final use is verified. Cleanup defect blocks transition.

## Noncanonical Interface Projection

This machine-readable projection is derived evidence from the frozen master. It does not add canonical scope or alter producer/consumer contracts.

```json
{
  "phaseId": "SEF-PHASE-014",
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
    },
    {
      "id": "SEF-IF-005",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Authenticated bridge",
        "records": {
          "Envelope": {
            "protocolMajor": "u16",
            "protocolMinor": "u16",
            "senderId": "string",
            "recipientId": "string",
            "senderBoot": "UUID",
            "channelEpoch": "UUID",
            "sequence": "u64",
            "requestId": "UUID",
            "type": "allowlisted_enum",
            "actor": "Actor?",
            "session": "Session?",
            "expectedRevision": "u64?",
            "expires": "Instant",
            "body": "bounded_typed_bytes"
          }
        },
        "methods": [
          "send(envelope:Envelope) -> Result<TypedReply>",
          "rotatePeerKey(peerId:string, expectedKeyId:string, nextKeyId:string) -> Result<RotationReceipt>"
        ],
        "errors": [
          "AUTH_FAILED",
          "REPLAY",
          "STALE_EPOCH",
          "EXPIRED",
          "OVERSIZED",
          "RATE_LIMIT",
          "PROTOCOL_MISMATCH",
          "UNREGISTERED_SERVER",
          "QUEUE_FULL"
        ],
        "ownership": "TLS 1.3 mutually authenticated private direct socket; peer certificate maps to registered server; no player carrier or arbitrary command relay."
      },
      "acceptance_ids": [
        "SEF-AC-011"
      ]
    },
    {
      "id": "SEF-IF-006",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Network authority and compatibility",
        "records": {
          "StateChange": {
            "stateKind": "BAN|MUTE|VANISH|HOME|OPERATION|VISIT|LIFECYCLE_DELIVERY",
            "key": "string",
            "revision": "u64",
            "previousRevision": "u64",
            "operationId": "UUID",
            "tombstone": "bool",
            "value": "typed_record?"
          },
          "Presence": {
            "session": "Session",
            "profileId": "string",
            "profileDigest": "sha256",
            "state": "CONNECTING|READY|TRANSFERRING|DISCONNECTED"
          },
          "CompatibilityRelation": {
            "clientProfileDigest": "sha256_of_server_observable_negotiated_profile",
            "sourceProfileDigest": "sha256",
            "destinationProfileDigest": "sha256",
            "adapterDigest": "sha256",
            "evidenceDigest": "sha256",
            "allowed": "bool"
          }
        },
        "methods": [
          "compareAndSet(actor:Actor, change:StateChange) -> Result<CommitReceipt>",
          "snapshot(afterRevision:u64) -> Result<StateSnapshot>",
          "resolve(observer:Actor, targetId:UUID) -> Result<Presence>",
          "admit(session:Session, destinationId:string) -> Result<AdmissionDecision>"
        ],
        "errors": [
          "STALE_REVISION",
          "AUTHORITY_UNAVAILABLE",
          "RESYNC_REQUIRED",
          "TARGET_NOT_VISIBLE",
          "INCOMPATIBLE_PROFILE",
          "UNKNOWN_PROFILE"
        ],
        "ownership": "One SQLite writer; backend ordered immutable replicas; deny network mutation or unsafe admission while authority is unavailable. Visit and lifecycle-delivery kinds are reserved here and remain non-dispatchable until the typed Phase 006 handlers are registered; its implementation uses the existing serialized authority writer."
      },
      "acceptance_ids": [
        "SEF-AC-012",
        "SEF-AC-013",
        "SEF-AC-018"
      ]
    },
    {
      "id": "SEF-IF-007",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Qualified home",
        "records": {
          "Home": {
            "homeId": "UUID",
            "ownerId": "UUID",
            "backendId": "string",
            "normalizedName": "string",
            "displayName": "literal_string",
            "location": "Location",
            "revision": "u64",
            "deleted": "bool"
          }
        },
        "methods": [
          "setHome(context:CommandContext, home:Home) -> Result<Home>",
          "resolveHome(ownerId:UUID, currentBackend:string, name:string, explicitBackend:string?) -> Result<Home>"
        ],
        "errors": [
          "DUPLICATE_LOCAL_NAME",
          "HOME_NOT_FOUND",
          "AMBIGUOUS_REMOTE_NAME",
          "WORLD_REPLACED",
          "STALE_REVISION"
        ],
        "ownership": "Proxy stores network records; backend validates and captures locations. Unique key owner/backend/normalizedName; explicit server wins, otherwise local match, otherwise sole remote match, otherwise show permitted choices."
      },
      "acceptance_ids": [
        "SEF-AC-016"
      ]
    },
    {
      "id": "SEF-IF-008",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Transfer and arrival",
        "records": {
          "TransferIntent": {
            "operationId": "UUID",
            "session": "Session",
            "source": "string",
            "destination": "string",
            "target": "Location",
            "targetRevision": "u64",
            "consentId": "UUID?",
            "expires": "Instant",
            "reservationId": "UUID",
            "state": "REQUESTED|RESERVED|SWITCHING|JOINED|ARRIVAL_AUTHORIZED|ARRIVED|COMMITTED|FAILED|CANCELLED|UNCERTAIN"
          },
          "ArrivalReceipt": {
            "operationId": "UUID",
            "destinationSession": "Session",
            "location": "Location",
            "worldTick": "u64",
            "outcome": "ARRIVED|REFUSED|UNCERTAIN",
            "durableSequence": "u64"
          }
        },
        "methods": [
          "reserve(intent:TransferIntent) -> Result<TransferIntent>",
          "authorizeArrival(operationId:UUID, joinedSession:Session) -> Result<TransferIntent>",
          "finalizeArrival(receipt:ArrivalReceipt) -> Result<TransferIntent>",
          "reconcile(operationId:UUID) -> Result<TransferIntent>"
        ],
        "errors": [
          "SESSION_CHANGED",
          "CONSENT_EXPIRED",
          "DESTINATION_UNREADY",
          "SAFETY_CHANGED",
          "TRANSFER_FAILED",
          "ARRIVAL_UNCERTAIN"
        ],
        "ownership": "Proxy coordinates connection; destination owns final safety and durable receipt; connection callback alone is never success."
      },
      "acceptance_ids": [
        "SEF-AC-016",
        "SEF-AC-017"
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

Only after implementation, evidence, documentation, checked paired PR merges, resulting product branch verification, signed phase tags, and complete cleanup packet may SEF-PHASE-015 start its complete assurance matrix. Phase 015 consumes these routing, migration, diagnostics, and real-arrival contracts. It is not a prerequisite for finishing this phase and must not start early.
