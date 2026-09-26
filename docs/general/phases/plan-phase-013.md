# Phase 013 Execution Plan

> **Plan ID:** PLAN-PHASE-013  
> **Phase ID:** SEF-PHASE-013  
> **Owner:** Forge RTP service  
> **Classification:** MANDATORY  
> **Master plan:** [plan.md](../plan.md)  
> **Phase sequence:** 013 of 016

## Purpose and Ownership

This phase delivers strict separation, final world safety, bounded local candidate preparation, and a genuine standalone `/rtp` arrival path for SEF-REQ-033. The master owns scope and shared contracts. Phase 012 remains the only allocation and successful-turn-consumption authority. This phase must use its durable grid reservation and committed landing record, never recreate a bag, a proxy authority, or a network fallback. Phase 014 later consumes the local safety interface for proxy-first admission and routing.

## Evidence Based Entry State

| Evidence class | Area | Finding | Source or command | Freshness condition |
| --- | --- | --- | --- | --- |
| OBSERVED | Existing RTP | `CoreTeleportCommands.randomTeleport` is radius sampling with loaded-chunk checks rather than hierarchical allocation, distance ledger, or prepared supply. | SRC-501, `CoreTeleportCommands.java:370..439`, SHA256 `41bc31fe7d9fd325b748838a23f6256d6aaf131ae0c4b7185911e9496296d659` | Reinspect if the pinned source changes. |
| OBSERVED | Existing teleport safety | `SafeTeleportService` is the current local safety dependency and its real receipt route is the predecessor fixture, not proof of the new constraints. | SRC-502, `SafeTeleportService.java`, SHA256 `561421f4101de6d9e4df94054c61d950f0218b052e836b09f8d64d071425670c` | Reinspect if its API or fingerprint changes. |
| PROPOSED execution prerequisite | Grid reservation | Phase 012 is planned to provide serialized durable SQLite allocation, grid generations, committed landing records, release proof, quarantine, and a genuine local arrival receipt fixture. | SEF-PHASE-012, SEF-IF-012, `Phase 012 task 002` | Verify the merged signed Phase 012 receipt and exact interface projection at execution. |
| PROPOSED | Safety behavior | Exact XZ separation includes online and vanished players, active reservations, and all recent successful landings. Failed work never spends a turn; uncertainty quarantines. | DEC-015, DEC-017, FIND-033, FIND-109 | Invalidate only after a scoped amendment of SEF-REQ-033. |
| OBSERVED | Runtime boundary | `node-1` can perform headless builds, dedicated server and server-only world fixtures. Native client input, chat rendering, and visual destination evidence require the laptop. | EXT-001, runtime host policy | Recheck actual host, revision, artifacts, desktop, renderer, and stream identity at execution. |

## Scope Boundaries

### Included Scope

- SEF-REQ-033 and SEF-AC-033. Deliver local no-proxy RTP safety policy, exact separation, persistent recent-landing exclusion, active-reservation exclusion, owner-thread final revalidation, and bounded candidate preparation.
- Connect `/rtp` to the existing command policy, warmup, cancellation, and genuine local teleport receipt route. The operation reports only actual arrival or explicit typed refusal.
- Create the Phase 013 `SEF-IF-013` implementation contract, scoped diagnostics, real dedicated-server fixtures, support documentation, and cleanup evidence.

### Explicit Exclusions

- Parent and child permutations, grid partitioning, bag fairness, durable allocation transactions, and successful-turn consumption remain Phase 012. Preparation and rejected candidates never consume a turn.
- Proxy selection, telemetry, admission leases, bridge transport, backend transfer, and network routing are Phase 014. Local mode must work with no proxy process and must not silently fall back to network authority.
- Full proxy/local parity, final client presentation acceptance, measured tuning, and complete product proof remain Phases 014 and 015. This phase does not claim them.
- The RTP authority is local SQLite, not the Phase 005 network authority or audit SQL. Audit linkage is observational and cannot permit or deny a landing.

## Phase Contract

### SEF-PHASE-013 — Strict-distance safe local RTP with bounded preparation

**Objective:** Land a locally connected eligible player through `/rtp` only after a backend-owner-thread check proves the exact configured XZ separation and all live safety predicates, while keeping unused grid cells and tickets safe under queue, cancellation, restart, and world change.  
**Owner:** Forge RTP service  
**Dependencies:** SEF-PHASE-012, SEF-REQ-008  
**Supporting contract and risk dependencies:** SEF-IF-001, SEF-IF-002, SEF-IF-003, SEF-IF-004, SEF-IF-008, SEF-IF-009, SEF-IF-012  
**Canonical requirements:** SEF-REQ-033  
**Documentation and release impact:** During implementation, update the RTP configuration, command, operational, diagnostic, and troubleshooting topics, `README.md`, `docs/README.md`, and the technical index with only implemented local behavior. Prepare wiki updates after approved merge. No public release or deployment is in scope.  
**Next transition:** SEF-PHASE-014 begins only after this phase PR merge, resulting `forge-1.20.1` verification, and signed phase tag, then integrates admission routing and lifecycle.

**Entry criteria**

- Phase 012 is merged into its applicable product branch, the result is fetched and verified, its signed tag and completion receipt prove durable grid reservation plus the existing genuine local teleport receipt fixture.
- The exact candidate build, Forge 47.3.12, Java 17, Xerial SQLite identity, world identity, and `SEF-IF-012` projection are revalidated. SQLite authority opens locally or local RTP refuses new work.
- The Phase 001 diagnostics and Phase 002 command/presentation contracts work from the console, including the required manager permission and default-off behavior.
- The initial standalone fixture has no Velocity process, bridge, proxy configuration, or network authority dependency. Its configured local world and command policy are available.

**Implementation scope**

- Implement SEF-REQ-033 through the work packages and acceptance obligations below.

- Implement `SafetyPolicy` with default `minimumDistanceXZ=128`, `recentWindowSeconds=300`, and a per-world recent exclusion capacity of 100000. Link each successful landing and quarantine exclusion to the Phase 012 committed receipt, rather than independently committing a second ledger. The same persistent world identity is queried across ordinary policy revisions and grid generations. A ledger that cannot retain its complete live window returns `RECENT_LEDGER_FULL` admission backpressure rather than evicting early. Increasing a configured window refuses activation until retained history covers it or a documented safe wait has passed. Trace: SEF-PHASE-013.
- Evaluate horizontal Euclidean XZ distance by exact squared comparison in the supported finite coordinate domain. Reject nonfinite or overflowing arithmetic. A candidate is rejected exactly when `dx*dx + dz*dz < minimumDistanceXZ*minimumDistanceXZ`; equality is accepted. Candidate search and final landing inspect all online players in the target world, including vanished players, all active reservations, and every successful recent landing still inside the durable window. Exclude only the moving subject's current online position and this operation's own active safety reservation, so final recheck cannot reject itself at zero distance. Unused prepared-cache proposals are not reservations and do not reserve all cells or consume turns. Reason text and diagnostics never disclose a hidden identity or coordinate. Trace: SEF-PHASE-013.
- On the owning server thread, atomically check separation and record a short-lived safety reservation before allowing `prepare` or mutation, so concurrent requests cannot both pass distance checks for colliding destinations. The reservation carries allocation ID, fence, policy revision, world generation, candidate, expiry, and operation/session identity. Rejected or proven failed work releases it and Phase 012's allocation only through its required proof. Ambiguous arrival remains quarantined. Trace: SEF-PHASE-013.
- Prepared candidates are immutable 30 second proposals tagged with grid generation, parent and child IDs, policy revision, `WorldRef`, observed tick, and chunk revision. The pool target is 32 per world, but only candidates for still eligible unused cells count. Preparation never consumes a parent or child turn and a failed proposal does not burn one. Trace: SEF-PHASE-013.
- Use loaded chunks by default. Implement chunk load/generation as a configurable opt-in adapter and enable it only after proving the pinned supported Forge API and ticket cleanup. It has at most four concurrent tickets per backend, a 15 second preparation deadline, cancellation release, and no network, SQL, or arbitrary worker world access. Background work is pure coordinate calculation only; world, chunk, claim, and player access stays on the owner thread. Trace: SEF-PHASE-013.
- Enforce initial budgets as acceptance limits, not measured claims: two ms or 16 checks per tick, 64 queue entries, ten second queue deadline, 32 coordinate attempts per request, four tickets per backend, 32 prepared candidates per world, and 15 second preparation. Queue or explicitly return unavailable when a limit is reached. Fairness, separation, and distance are never relaxed or reused. Trace: SEF-PHASE-013.
- Before mutation, and again after warmup and any transfer boundary, the owner thread rechecks exact session, command authorization, permission and cooldown, grid and policy generations, world and dimension identity, border, build height, chunk identity and readiness, claim permission, biome policy, solid footing, two-block clearance, liquids, fire, cactus, magma, powder snow, and every registered mod hazard. A changed check produces `SAFETY_CHANGED`, `UNSAFE`, `CLAIM_DENIED`, `CHUNK_UNREADY`, or another safe typed refusal. No fallback spawn counts as RTP success. Trace: SEF-PHASE-013.

**Execution order**

1. `P013-TASK-003` adds bounded RTP diagnostic producers, source-unit self-test, command integration, redaction, and support procedure before any dependent safety stimulus. Trace: SEF-PHASE-013.
2. `P013-TASK-001` adds the local policy, Phase 012 receipt-linked recent exclusion, exact spatial predicate, and atomic safety reservation. It supplies the real target for the diagnostic control proof without creating a completion cycle. Trace: SEF-PHASE-013.
3. `P013-TASK-002` adds owner-thread candidate preparation, bounded queue, loaded-chunk default, configurable ticketed preparation adapter, and final safety revalidation. Trace: SEF-PHASE-013.
4. `P013-TASK-004` connects the natural standalone `/rtp` route to actual local teleport/receipt commit and executes the property, dedicated-world, recovery, documentation, and integration evidence matrix. Trace: SEF-PHASE-013.

**Required evidence**

- Property tests prove equality acceptance, strict-less-than rejection, negative coordinates, adjacent cell borders, active/reserved/recent collision prevention, no early ledger eviction, no fair-turn consumption by preparation/refusal, atomic simultaneous check/reserve, and 64 entry/10 second queue boundaries.
- Dedicated no-GUI server fixtures exercise natural `/rtp` with the real command policy and real local teleport route, no proxy process, actual movement plus independent location/count oracle, vanished player privacy, hazards, claims changes, chunk cancellation, and restart recovery.
- Required residual laptop evidence is limited to actual native chat presentation, `/rtp` input, and rendered local destination. It follows the Trident client and dedicated-server procedure and does not substitute for owner-thread world assertions.

**Exit criteria**

- No known mandatory phase-owned defect remains. Every such defect must be fixed and its affected evidence rerun before phase closure.

- SEF-AC-033 passes. No successful local landing violates online, vanished, active-reservation, or complete recent-window separation, or any final safety constraint; all exhausted or unsafe paths queue briefly or return explicit unavailable without reset, distance reduction, or burned turn.
- Local `/rtp` works without a proxy or network-authority fallback, produces a genuine arrival receipt, commits only that actual successful landing, and releases every owned ticket on each terminal outcome.
- Diagnostics precede dependent real proof. Required documentation, cleanup receipts, checked PR merge, resulting product branch verification, and signed tag are complete before Phase 014.

## Inputs and Upstream Contracts

| Input or contract | Provider | Required state | Validation | Failure behavior |
| --- | --- | --- | --- | --- |
| Allocation and committed landing record | SEF-IF-012, Phase 012 | Fenced durable reservation, immutable grid/world identity, actual-only consumption, and reconciliation are available. | Verify reservation ID, operation, session, grid generation, fence, and state before every safety action. | Do not search or land on `FENCED`, `CYCLE_BLOCKED`, persistence failure, or uncertainty. |
| Existing safe local teleport receipt | SEF-REQ-008, Phase 012 receipt fixture | Backend can observe real local movement and create a durable receipt; it is not merely a command acknowledgement. | Exercise actual server entry point and compare receipt location/session to an independent location oracle. | Refuse or quarantine; never treat elapsed time as nonarrival. |
| Command policy and presentation | SEF-IF-003, SEF-IF-004 | `/rtp` is authorized, warmup/cooldown cancellation is observed, and response values are literal. | Recheck policy at dispatch and immediately before mutation. | Safe `DENIED`, `CANCELLED`, or stale refusal; no action token bypass. |
| Diagnostics | SEF-IF-002 | Console-safe default-off capture exists with manager authorization and bounded output. | Complete self-test, off/readback, then fresh capture after server setup and before stimulus. | Do not make safety proof depend on an unavailable capture. |
| World and claim APIs | Forge 47.3.12 and installed supported claim/mod hazard adapters | Owner-thread capability and exact API support are established before enabling optional loading or a registered hazard. | Compile against pinned API and run owned server fixture. | Keep unsupported feature disabled or report explicit unsafe/unavailable, never guess safety. |

## Outputs and Downstream Contracts

| Output or contract | Consumer | Guaranteed state | Compatibility or versioning | Evidence |
| --- | --- | --- | --- | --- |
| SEF-IF-013 safety and prepared candidates | Phase 014 admission routing, Phase 015 verification | `findCandidate` returns an immutable unspent proposal. `validateAndLand` final-checks and returns only an actual receipt or typed refusal. | Registry/schema version 1. Mismatched grid, policy, world, chunk, fence, session, or expiry rejects. | Real local receipt, race, restart, and refusal matrix. |
| Durable recent landing exclusion | Phase 014 and operators | Every successful landing remains visible for its full policy window across restart, subject to capacity backpressure. Quarantined destination exclusions remain until evidence resolves them, regardless of window or lease expiry. | Same persistent world identity across ordinary policy revisions and grid generations. No early eviction, revision reset hiding, or identity leakage. | Restart, grid/policy revision, 100000 capacity/backpressure, and restore-comparison fixtures. |
| Bounded prepared supply | Phase 014 telemetry and Phase 015 | Supply counts only eligible unused proposals and all tickets are released on terminal outcomes. | Default loaded-chunk mode. Ticketed generation stays disabled until its support proof exists. | Ticket cancellation/timeout and candidate-expiry checks. |
| Local diagnostic categories and runbook | Operators and Phase 015 | Scoped, redacted causal evidence distinguishes eligibility, safety, queue, ticket, and receipt decisions. | Uses SEF-IF-002 caps and default-off reset semantics. | Console self-test and sanitized packet. |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
| --- | --- | --- | --- | --- | --- | --- |
| P013-TASK-003 | SEF-REQ-033 | Add planned RTP safety diagnostic producers before dependent tests. Register `rtp.safety`, `rtp.preparation`, `rtp.queue`, `rtp.ticket`, and `rtp.arrival` with correlation, allocation fence, grid/policy/world/chunk revisions, eligibility counts, exact squared-distance result category, queue age, ticket lifecycle, final reason, and receipt outcome. Integrate exact `sef.debug.manage` console-safe controls for `sef debug on rtp <grid-or-operation> 60`, `sef debug status`, and `sef debug off <capture-id>`. | SEF-IF-002, SEF-IF-003, Phase 001 diagnostics, Phase 012 RTP category. | Redacted typed signals, command self-test, and support runbook. | Planned backend RTP diagnostic adapter, SEF-IF-002. | Assert denied manager, absent target, output/capture cap, timeout, reload and restart reset, off/readback, self-test, and fresh post-setup capture before real stimulus. |
| P013-TASK-001 | SEF-REQ-033 | Implement versioned `SafetyPolicy`, Phase 012 committed-receipt-linked full-window recent exclusion, exact XZ spatial predicate, and atomic owner-thread safety reservation. Compare all online including vanished players, active reservations, and recent successful landings with strict `<` squared distance rejection and equality acceptance. Apply capacity backpressure at 100000 recent records per world, never early eviction or a leak. | P013-TASK-003, SEF-IF-012, SEF-IF-009, DEC-015, DEC-017, SEF-RISK-016. | `SafetyPolicy`, durable recent-window reader, safety-reservation state, and `findCandidate` eligibility basis. | Planned Forge RTP safety service, Phase 012 allocation repository, SEF-IF-013. | Property plus real SQLite fixtures cover equality, adjacent parent/child borders, negative coordinates, vertical separation, vanished obstacle without disclosure, active collision race, recent restart, 100000-full refusal, policy/grid revision continuity, nonfinite/overflow rejection, moving-subject self-exclusion, and repeated operation IDs. |
| P013-TASK-002 | SEF-REQ-033 | Implement immutable 30-second `PreparedCandidate` pool and bounded per-world preparation coordinator. Start loaded-chunk-only. Enforce 32 target, 32 attempts/request, 15 second preparation deadline, two ms or 16 checks/tick, 64 queue entries, ten second queue deadline, four tickets/backend, and eligible-unused-supply-only accounting. Implement the configurable opt-in load/generation adapter, then enable it only after pinned supported Forge API proof, cancellation, and exact ticket cleanup. | P013-TASK-003, P013-TASK-001, SEF-IF-012, pinned Forge API evidence, SEF-RISK-016. | Candidate expiry/revision fencing, queue outcomes, ticket lifecycle, and final-owner-thread validation input. | Planned preparation coordinator and ticket adapter, SEF-IF-013. | Dedicated fixture verifies loaded success, empty eligible queue/refusal, timeout, ticket cancellation, world/chunk revision change, candidate expiry, and every ticket/process cleanup path. |
| P013-TASK-004 | SEF-REQ-033 | Bind natural local `/rtp` to policy authorization, warmup/cancellation, allocation, preparation, rechecks after warmup and transfer boundary, actual movement, genuine arrival receipt, Phase 012 commit, and terminal release/quarantine. Final owner-thread gate checks session, permission, cooldown, border, build height, world/dimension/chunk identity, claims, biome, footing, clearance, liquid, fire, cactus, magma, powder snow, registered mod hazards, player/reservation/recent distances, and candidate revisions before mutation. SQLite intent persistence runs off the world executor, completion returns for this final owner-thread recheck, and no blocking fsync or wait runs on that executor. | P013-TASK-003, P013-TASK-001, P013-TASK-002, SEF-IF-003, SEF-IF-004, SEF-IF-008, SEF-IF-012. | Standalone local RTP execution path, genuine receipt evidence, docs, and phase completion packet. | Planned Forge RTP command/service, existing safe local teleport entry point, SEF-IF-013. | Actual dedicated-server landings have an independent world location/count oracle. Prove no proxy running, natural command path, claim/hazard/player movement races, terminal ticket release, confirmed-failure release, uncertainty quarantine, and exactly-once committed receipt. Laptop proves only native chat, input, and rendered destination. |

`P013-TASK-003` supplies producers and source-unit self-test before safety stimuli. `P013-TASK-001` owns policy and safety reservation semantics and supplies the real control target; its fresh on/status/off proof occurs before its safety stimulus, not as a completion barrier. `P013-TASK-002` produces a proposal only and cannot call teleport or allocation commit. `P013-TASK-004` is the sole natural-command integration consumer. This ordering is acyclic: diagnostic producer to eligibility to preparation to actual landing. Source implementation dependencies do not wait for later acceptance evidence, and Phase 014 consumes the completed interface rather than creating a reverse dependency.

## Architecture and Implementation Boundaries

The allocation database and coordinator from Phase 012 remain the only source of a cell reservation and only successful receipt commit consumes a parent or child turn. The Phase 013 safety service reads the Phase 012 receipt-linked persistent recent exclusion history across policy revisions and grid generations for the same world identity, and maintains an owner-thread-only active safety reservation index reconstructed from fenced allocation state on restart. It cannot silently release a Phase 012 allocation or independently commit a landing ledger. `findCandidate` accepts a durable allocation, works from immutable grid/policy snapshots, and proposes only coordinates in its unused reserved cell. `prepare` records a candidate without consumption. Durable allocation and intent persistence execute off the world executor. A persisted-before-mutation completion returns to the owner thread for a fresh live recheck; no SQLite fsync or blocking wait runs on the world executor. `validateAndLand` then performs actual safe local movement and hands the genuine `ArrivalReceipt` to the allocation authority for idempotent commit. Failure before mutation releases only with proven nonarrival; uncertainty after mutation stays quarantined. Independently delivered audit never gates this authority.

The atomic safety check/reserve covers exact XZ separation at the point competing requests enter eligibility. At final mutation the same candidate may be invalid because a player, claim, block, chunk, policy, permission, world, or reservation changed. The only permitted responses are a safely fenced retry using an unused eligible cell, a bounded queue, a confirmed release, or a typed unavailable. It must not reuse a committed cell, lower distance, fabricate a receipt, teleport to fallback spawn, or treat an old candidate as authority. Successful recent entries link to committed receipts. A quarantined exclusion instead links to the unresolved durable allocation and needs no fabricated successful receipt; its reservation and spatial exclusion persist until reconciliation proves the outcome.

No live `ServerLevel`, claim adapter, online player collection, chunk, or mod-hazard object crosses to SQL or preparation workers. Workers may derive deterministic coordinates and expired-proposal lists from immutable data. The backend owner thread reads world state and applies all mutations. The spatial index is an optimization only and cannot omit any online player, including vanished players, any other active safety reservation, or live recent landing. It excludes only the subject's current position and the same operation's reservation. Hidden player diagnostic values are category/count only. Receipt-linked recent history retains necessary world/location evidence while support output redacts exact locations and identities.

Default preparation is loaded chunks only. `allowChunkGeneration` is a configurable opt-in backed by an implemented pinned Forge 1.20.1 adapter. It cannot enable until that adapter proves bounded main-thread work, owner-fenced tickets, cancellation, deadline, shutdown, and all-terminal cleanup. A failed proof leaves the option visibly unavailable rather than silently omitting approved scope. This is neither a proxy feature nor a license to use unsupported chunk APIs.

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
| --- | --- | --- | --- | --- |
| Candidate exactly at minimum XZ distance | Exact squared metric signal with desired/actual distance squared. | Accept equality and continue normal final safety checks. | None required unless another gate fails. | Property and dedicated-world equality fixture. |
| Adjacent cell, negative coordinate, or vertical-near candidate | Spatial oracle includes raw calculation privately and safe category publicly. | Reject only strictly less horizontal Euclidean separation; vertical distance cannot bypass XZ separation. | Search another unused eligible candidate or bounded unavailable. | Adjacent-border, negative-coordinate, and stacked-world fixtures. |
| Vanished online player occupies exclusion radius | Owner-thread collection count differs from visible observer list. | Reject without target name or coordinate disclosure. | Release safety reservation with confirmed refusal; preserve allocation turn. | Vanished-obstacle fixture and message redaction scan. |
| Player enters proposed destination or another request races | Atomic check/reserve fence or final recheck detects collision. | At most one colliding candidate can proceed; neither may land too close. | Proven failed candidate releases. Ambiguous movement quarantines allocation. | Concurrent repeated `/rtp` fixture with independent location oracle. |
| Recent ledger fills or restart occurs inside window | Ledger count/window/capacity signal and persisted rows. | Backpressure with `RECENT_LEDGER_FULL`; retain every live-window row across restart. | Admit only after expiry frees space; no early eviction. | 100000 entry capacity and restart fixture. |
| Claim, hazard, border, block, biome, world, chunk, or permission changes during warmup | Final owner-thread recheck emits final reason and revision mismatch. | Do not mutate. No stale prepared candidate becomes safe. | Confirmed refusal releases; missing receipt or mutation ambiguity quarantines. | Claim-change, world-change, nether/end, liquid/fire/cactus/magma/powder-snow, mod-hazard, and chunk-revision fixtures. |
| Candidate expires, queue reaches 64, deadline reaches ten seconds, or attempts reach 32 | Queue/preparation signals contain age, attempts, and cap category. | Refuse `CANDIDATE_EXPIRED`, `QUEUE_TIMEOUT`, or unavailable without relaxed policy or fairness reuse. | Remove only owned proposal; release tickets; leave allocation according to confirmed proof. | Empty-supply, repeated-request, deadline, and queue-cap fixtures. |
| Ticketed preparation cancellation or shutdown | Ticket ownership/fence and terminal reason are recorded. | Release each owned ticket on cancellation, timeout, refusal, success, crash recovery, and server stop. | Reconcile fenced ticket state; never release unrelated tickets. | Explicit cancellation and shutdown cleanup fixture. |
| Arrival timeout or crash after authorization | Receipt/session/location evidence is incomplete or contradictory. | Never infer nonarrival from time alone. | Quarantine and reconcile through Phase 012 evidence; ordinary gameplay continues. | Crash cut and restart fixture. |

## Diagnostics and Debugging

**Requirement IDs:** SEF-REQ-033  
**Task IDs:** P013-TASK-003, P013-TASK-001, P013-TASK-002, P013-TASK-004  
**Controls:** `sef.debug.manage` is required. From console use `sef debug on rtp <grid-or-operation> 60`, then `sef debug status`, then `sef debug off <capture-id>` and read back stopped status. `on` is default-off, scoped, bounded, and never requires a joined owner.  
**Signals:** Typed backend events retain capture/correlation/boot/sequence/tick/time/config generation/candidate digest plus grid, policy, world, chunk revisions, session fence, distance category, candidate eligibility, queue/ticket units, desired versus actual final state, and safe reason.  
**Collection procedure:** The numbered runbook below first exercises producers and source-unit self-test, then establishes a real target for on/status/off control proof. It captures a fresh bounded local fixture after setup and before the natural `/rtp` stimulus, with explicit readiness, connection, receipt, and shutdown waits.  
**Headless verification:** `node-1` runs only inspected headless build/test graphs and an owned no-GUI dedicated server. Its world fixtures prove server policy, actual local movement, receipt, exact location/count oracle, ticket cleanup, and persistence. They do not prove native chat rendering or client input.  
**Client verification:** The laptop alone proves residual native `/rtp` entry, rendered localized feedback, and actual destination rendering against the exact private dedicated endpoint. This phase's server assertions remain independently required.  
**Client audio isolation:** Leave audio settings unchanged. Every required client uses the master Section 14 Trident instance lifecycle on the verified laptop, with explicit owned instance, runtime, window/PID, discrete renderer and joined-world evidence. No Trident command or client runs on node-1. Do not create mute watchers or require a playback stream or mute proof. Server-only checks need no client. Verify exact owned process exit and instance cleanup after the final consumer.
**Budgets and privacy:** Capture is default-off and inherits 60 seconds default, 300 seconds maximum, 200 events/second, 10000 events, 8 MiB, 1024 queued events, and two simultaneous captures/process. It stores no exact hidden-player coordinates, player names, private addresses, or secrets. Audit is separate and unsampled. RTP admission limits are two ms or 16 checks/tick, four tickets/backend, 64 queue entries, ten seconds queue, 32 attempts/request, 15 seconds preparation, 32 candidates/world, and 100000 recent records/world.  
**Regression and support:** Use 120 seconds startup/restart readiness, 60 seconds private-connection readiness, ten seconds local receipt wait, five seconds SQLite busy wait, and 30 seconds orderly shutdown. Run source-unit self-test first, then establish a real target for on/status/off control proof and verify it creates no new record. Take a fresh capture after setup before stimulus, refresh it after laptop join/setup, repeat after restart because debug is default-off, redact the retained packet, update support documentation, and rerun local proofs when source, candidate hash, Forge API, grid/policy schema, world/claim adapter, or artifact revision changes. Durable recovery status remains separate from traces.

| Signal | Source and unit | Expected observation |
| --- | --- | --- |
| `rtp.safety.distance` | Backend owner thread, one final candidate decision, squared XZ blocks. | Equality accepts, strict less rejects, and source category is online, vanished, reservation, or recent without private identity. |
| `rtp.safety.final` | Backend owner thread, one pre-mutation result. | Records all final checks, world/policy/chunk revisions, session fence, and a typed refusal or authorized mutation. |
| `rtp.preparation` | Coordinator, one proposal lifecycle. | Candidate has 30-second expiry and matching grid/policy/world/chunk revisions without consumed turn. |
| `rtp.queue` | Coordinator, queue entry or terminal removal. | Count never exceeds 64, age never exceeds ten seconds, and unavailable is explicit. |
| `rtp.ticket` | Owner-thread ticket adapter, ticket count and lifecycle. | Maximum four owned tickets/backend and exactly zero owned tickets after each terminal path. |
| `rtp.arrival` | Actual local teleport/receipt path, one operation state edge. | Real movement yields one matched durable receipt and commit, while refusal/uncertainty cannot claim arrival. |

1. Register exact disposable node-1 runtime, world, SQLite fixture, backup and isolated-restore comparison targets, logs, diagnostic output, candidate artifact, owned server PID, and cleanup targets beneath the verified project anchor. For the residual client gate also register the discovered laptop project/runtime anchor, isolated instance, and output targets. Before each server launch write and read back `eula=true` in that exact disposable runtime.
2. Inspect the selected Gradle/server task graph to prove no client starts on node-1. Run P013-TASK-003 producer/source-unit self-test. Start the no-GUI server and wait at most 120 seconds for ready state. Establish a real scoped target, run `sef debug on rtp <grid-or-operation> 60`, verify `sef debug status`, run `sef debug off <capture-id>`, verify stopped and no new record, then re-enable a fresh capture after fixture setup and before `/rtp`.
3. Run the natural permission-checked `/rtp` stimulus with no proxy process and capture independent before/after server locations, online count, exact distance oracle, allocation state, Phase 012 receipt-linked recent-exclusion count, ticket count, and actual receipt within ten seconds. Bound SQLite busy handling to five seconds. Execute unsafe, vanished, claim-race, player-race, expiration, queue, cancellation, and restart cases. Inspect correlation, revisions, reason, receipt sequence, and cleanup signals.
4. Create an actual consistent SQLite backup, restore it in an isolated owned target, and compare recent history, allocation state, quarantined exclusions, fences, and matching world/session evidence before source cleanup. For native presentation only, establish the private node-1 endpoint within 60 seconds, prove server readiness and laptop reachability, apply verified Trident client isolation, automatically join through a supported candidate-specific method, refresh diagnostic capture after laptop join/setup, and prove both server and client joined the same world before typing `/rtp`. Capture only the required native chat/input/rendering evidence; do not use console to bypass the command policy or gameplay action.
5. Run `sef debug off <capture-id>`, verify status stopped, sanitize the minimum packet, stop only owned client, server, and ticket work within 30 seconds, verify processes have exited and owned instances are removed, remove exact disposable resources and owned Trident instance residuals, and verify absence on every used host. Report any leftover separately and leave its gate open.

## Verification Matrix

| Requirement or task | Static or unit | Integration | Real workflow or runtime | Negative and recovery | Execution host and prerequisites | Evidence artifact |
| --- | --- | --- | --- | --- | --- | --- |
| P013-TASK-003, SEF-REQ-033 | Typed diagnostic schema and command permission tests. | Producer/source-unit self-test, then real-target on/status/off proof, cap, timeout, reload and restart. | Console scoped capture after no-GUI server readiness. | Manager denial, absent target, output unavailable, capture expiry, and no new record during control self-test. | node-1 only, inspected server-only graph, no client launched. | Sanitized diagnostic self-test packet and status transcript. |
| P013-TASK-001, SEF-REQ-033 | Exact squared-distance, equality, negative-coordinate, finite/overflow rejection, receipt-linked history continuity, and atomic check/reserve properties. | Real SQLite restart, backup/isolated restore comparison, and concurrent reservations. | Dedicated world records actual local landing and independent location/count oracle. | Adjacent border, vertical-near, vanished obstacle, subject self-exclusion, recent restart, policy/grid revision, simultaneous requests, full ledger. | node-1 no-GUI dedicated server with exact Phase 012 artifact and local SQLite. | Redacted distance matrix, backup/restore comparison, allocation receipt, and cleanup receipt. |
| P013-TASK-002, SEF-REQ-033 | Candidate expiry/revision and queue-budget properties. | Ticket lifecycle and world/chunk revision change fixture. | Loaded-chunk local preparation and configurable opt-in ticketed loading only after pinned adapter proof. | Empty eligible queue, 32 attempts, ten-second queue, 15-second preparation, cancellation and shutdown. | node-1 no-GUI server; no laptop for server-observable behavior. | Candidate/ticket timeline, pinned adapter proof, terminal counts, and verified ticket cleanup. |
| P013-TASK-004, SEF-REQ-033 | Session/policy/revision recheck and exactly-once receipt tests. | Claims and registered hazard adapters, warmup, restart, and receipt reconciliation. | No-proxy natural `/rtp`, local actual movement, nether/end hazards, final revalidation, and standalone receipt. | Claims/hazard/world/permission change, player enters candidate, timeout, confirmed failure, uncertainty quarantine. | node-1 dedicated server. EULA readback and exact runtime identity required. | Independent destination/location/count oracle, receipt ledger, server logs, and sanitized capture. |
| P013-TASK-004 residual client gate | Client log correlation only. | Matching laptop client to exact node-1 private dedicated endpoint. | Native chat feedback, typed `/rtp`, and destination rendering. | Missing renderer/window/instance identity stops client and leaves this gate unverified. | Laptop only after candidate hash, private endpoint, joined-world proof, discrete renderer, and verified Trident instance binding. | Targeted client evidence, Trident instance/Hyprland/PID record, and laptop cleanup receipt. |

## Documentation, Operations, and Release

Document the local-mode-only `/rtp` behavior, required permission and cooldown behavior, policy defaults and equality rule, queues and unavailable result, safety categories, claim and mod-hazard adapter limits, loaded-chunk default, configurable optional-preparation adapter enablement evidence, receipt-linked ledger backpressure, ticket cleanup, migration/restart recovery, diagnostic commands, redaction, no-proxy operation, and exact support collection procedure. Update README, `docs/README.md`, the technical documentation index, RTP configuration/operations/troubleshooting topics, and command presentation coverage only after implementation establishes each claim. Prepare corresponding wiki content after the approved merge. Phase integration uses an `envy` phase branch from verified `forge-1.20.1`, sole EnVy signed commits, required checks and private independent review subject to the established review-capability availability rule, a merge commit PR, resulting branch verification, then a signed annotated phase tag. Where the common/proxy projection or approved digest changes, perform the applicable Forge-first common/proxy projection integration, verify the exact approved digest, complete the checked proxy merge and resulting verification, and create its signed tag before transition. Unchanged artifacts and contracts do not invent proxy work. No direct base push, public release, or production deployment is authorized.

## Risks and Evidence Invalidation

| Risk ID and owner task | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
| --- | --- | --- | --- | --- | --- |
| SEF-RISK-016, P013-TASK-001 and P013-TASK-004 | Atomic check/reserve, Phase 012 receipt-linked full-window persistence, all-other-player inclusion, final owner-thread recheck. | `rtp.safety.distance`, `rtp.safety.final`, receipt, and independent location/count oracle. | Release only proven failure; quarantine uncertainty; queue/refuse without distance reduction. | Policy, grid, world, claim, hazard, teleport, or allocation changes. | Rerun equality, vanished, self-exclusion, race, claim/hazard, restart, restore, and actual-landing matrix. |
| SEF-RISK-014, P013-TASK-001 through P013-TASK-004 | Reuse only Phase 012 reservation/commit authority; preparation does not consume. | Allocation state/fence and receipt sequence. | Preserve turns on refusal; defer uncertainty to reconciliation. | Allocation schema, receipt semantics, or Phase 012 result changes. | Rerun genuine receipt and repeated/concurrent request matrix. |
| SEF-RISK-015, P013-TASK-002 and P013-TASK-004 | Candidate stores grid/world/policy/chunk revisions and final checks reject mismatch. | Candidate and final revision fields. | Expire/refuse stale proposals; do not reinterpret location. | Grid generation, bounds, world identity, or chunk API changes. | Rerun negative bounds, world replacement, chunk revision, and stale-candidate cases. |
| SEF-RISK-020, P013-TASK-004 | Pin candidate and host identity; keep client proof distinct from server proof. | Artifact hashes, server/client world identity, renderer/PID/stream records. | Rerun affected gate; do not substitute headless result for client evidence. | Artifact, pack, desktop, renderer, endpoint, or host change. | Rebuild and repeat exact local server and residual laptop gates. |

## Phase Completion Packet

Before closure retain the approved phase branch commit and PR state, required deterministic checks, private review result when available, merged product-branch commit, signed annotated phase tag, exact source/artifact hashes, SQLite migration and restart evidence, actual consistent SQLite backup and isolated restore comparison of recent history, allocations, quarantined exclusions and fences with matching world/session evidence, natural no-proxy `/rtp` receipt linkage, independent destination/location/count oracle, strict-distance and hidden-player matrix, claims/hazards/world-change matrix, ticket lifecycle cleanup evidence, recent-exclusion capacity/restart result, queue/preparation budgets, diagnostics source-unit and real-target self-tests with sanitized packet, documentation diffs, and per-host cleanup receipts. If the common/proxy projection or approved digest changed, retain the applicable Forge-first integration, checked proxy merge, resulting verification, and signed tag evidence. Every build, validator, unit suite, dedicated server, ticket fixture, client run, and audit must register exact owned temporary paths and processes before launch. Preserve only requested sanitized evidence after final consumption, stop owned processes, verify process exit and owned instance removal, and remove only known disposable runtimes, worlds, databases, logs, screenshots, traces, downloads, temporary configuration, and test output. Unverified cleanup or a missing residual client gate remains open and is not hidden by headless success.

## Next Transition

After all Phase 013 implementation, evidence, cleanup, PR merge, resulting `forge-1.20.1` verification, and signed tag gates pass, plus the conditional common/proxy projection integration and checked proxy merge/resulting verification/signed tag when the approved digest changed, the deterministic phase cursor may advance to SEF-PHASE-014. Its first action consumes the completed local safety interface to implement destination-first proxy admission and lifecycle without changing local no-proxy behavior. No Phase 014 branch or implementation begins before that integration.

## Noncanonical Interface Projection

This derived projection copies the registered producer and consumer interfaces. It is not a competing canonical contract.

```json
{
  "phaseId": "SEF-PHASE-013",
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
    }
  ]
}
```
