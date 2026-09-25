# Phase 012 Execution Plan

> **Plan ID:** PLAN-PHASE-012  
> **Phase ID:** SEF-PHASE-012  
> **Owner:** Backend RTP authority  
> **Classification:** MANDATORY  
> **Master plan:** [plan.md](../plan.md)  
> **Phase sequence:** 012 of 016

## Purpose and Ownership

This phase delivers the backend owned, durable allocation authority for SEF-REQ-032. It creates exact immutable grid generations, independently persistent parent and child shuffle state, and fenced reservation and arrival reconciliation semantics. The master remains the sole authority for product scope, shared interfaces, and later safety or routing work. This blueprint only details the Phase 012 implementation and proof. It does not claim the Phase 013 safety, distance, chunk preparation, or actual player presentation acceptance that consumes this allocation foundation.

## Evidence Based Entry State

| Evidence class | Area | Finding | Source or command | Freshness condition |
| --- | --- | --- | --- | --- |
| OBSERVED | Existing RTP | `CoreTeleportCommands.randomTeleport` uses radius sampling and bounded loaded chunk checks, not hierarchical allocation. | SRC-501, `CoreTeleportCommands.java:370..439`, SHA256 `41bc31fe7d9fd325b748838a23f6256d6aaf131ae0c4b7185911e9496296d659` | Invalidate if the pinned source fingerprint changes. |
| OBSERVED | Existing safety | `SafeTeleportService` and its GameTests remain an existing local teleport dependency, not a reservation or fairness implementation. | SRC-502, SHA256 `561421f4101de6d9e4df94054c61d950f0218b052e836b09f8d64d071425670c` | Invalidate if the service API or fingerprint changes. |
| PROPOSED | Allocation behavior | Parents and independent child bags consume only after authentic successful arrival, with uncertainty quarantined. | DEC-015, DEC-017, FIND-032, FIND-109 | Invalidate if a scoped owner amendment changes SEF-REQ-032. |
| OBSERVED | Persistence dependency | Xerial SQLite JDBC 3.53.4.0 is the selected local authority dependency, with runtime loading already required by Phase 001. | EXT-009, SRC-604, FIND-111 | Revalidate artifact identity, runtime loading, and advisories before execution. |
| OBSERVED | Shared contracts | `WorldRef`, `Session`, diagnostics, audit linkage, and the RTP allocation signature are frozen upstream contracts. | SEF-IF-001, SEF-IF-002, SEF-IF-009, SEF-IF-012 | Invalidate if the approved shared contract projection changes. |

## Scope Boundaries

### Included Scope

- SEF-REQ-032 and SEF-AC-032: validated per world grid generations, exact integer partitions, persistent randomized cycles, actual landing order barriers, durable allocation state, idempotent arrival reconciliation, and their property, crash, concurrency, and real local receipt fixtures.
- The minimal allocation owned durable recent landing record written atomically with successful consumption. Phase 013 owns use of that record for distance policy, hazard checks, and safe landing decisions.
- Audit events for allocation terminal state through SEF-IF-009 without making audit availability, audit segment rotation, or debug capture a second allocation authority.

### Explicit Exclusions

- SEF-REQ-033 safety, claims, hazards, online and vanished player distance, chunk tickets, bounded preparation, and safe destination selection belong to SEF-PHASE-013. A successful existing local teleport receipt fixture proves only this phase's receipt and consumption path.
- SEF-REQ-034 and SEF-REQ-035 proxy routing, telemetry, admission leases, externally visible configuration migration, and final RTP operations belong to SEF-PHASE-014.
- Client rendering, input, GUI, or client synchronization evidence is not needed for this server observable allocation authority. No Minecraft client launches in this phase. Later named client gates remain open.
- No audit spool rotation may touch grid generations, reservation journals, SQLite authority data, or central audit history.

## Phase Contract

### SEF-PHASE-012 — Exact partitions, independent cycles and durable reservations

**Objective:** Produce a serialized backend allocation authority that gives every valid world grid exact nonoverlapping cells, preserves independent random without replacement cycles across restart, and consumes turns only after an authentic durable landing receipt.  
**Owner:** Backend RTP authority  
**Dependencies:** SEF-PHASE-011, EXT-009  
**Supporting contract and risk dependencies:** SEF-IF-001, SEF-IF-002, SEF-IF-009  
**Canonical requirements:** SEF-REQ-032  
**Documentation and release impact:** Add planned allocation schema, recovery, configuration validation, diagnostics, and test runbook material to the existing RTP and troubleshooting documentation during implementation. No release or production action is in scope.  
**Next transition:** SEF-PHASE-013, integrate local safety and preparation only after this phase is integrated and tagged.

**Entry criteria**

- Phase 011 is merged, its required branch verification and signed tag are present, and the current backend branch derives from that approved base.
- EXT-009 byte identity, Java 17 native SQLite loading, and the existing safe local teleport receipt entry point are revalidated in isolated fixtures.
- SEF-IF-001, SEF-IF-002, and SEF-IF-009 implementations are available, and the frozen declaration of SEF-IF-012 is available without signature changes. This phase creates the SEF-IF-012 implementation.

**Implementation scope**

- Implement SEF-REQ-032 through the work packages and acceptance obligations below.

- Validate a `GridIdentity` before persistence. Interpret configured inclusive `minX..maxX` and `minZ..maxZ` through checked wide arithmetic as half open internal domains `[minX, maxX + 1)` and `[minZ, maxZ + 1)`. Reject overflow, reversed bounds, zero width, a partition with an empty interval, and layouts with fewer than two parent cells, fewer than two child cells per parent, or more than 1,000,000 leaf cells. Rows and columns remain positive `u16` values. This rejects impossible layouts rather than changing their semantics. Trace: SEF-PHASE-012.
- Derive each interval with checked quotient and remainder arithmetic, not floating point. Every integer coordinate has exactly one parent and one child owner, including negative and uneven ranges. The required fixture is six parent cells, each with a 20 by 20 child layout, yielding 2,400 distinct child cells. Trace: SEF-PHASE-012.
- Persist a Fisher Yates parent permutation, epoch, committed set, reserved set, and actual physical last landed parent. Persist a separate permutation, epoch, committed set, reserved set, and actual physical last landed child for every parent. Parent reshuffles never reset child state. RNG is injected only for deterministic testing, while complete permutations and state, not a seed or draw count, are durable authority. A policy revision or ordinary configuration reload may invalidate or revalidate proposals but retains bags, reservations, quarantines, and recent history; it must not reset fairness. Trace: SEF-PHASE-012.
- Derive a new bag boundary from actual physical successful landing order. Do not use selection, request launch, preparation, receipt arrival, durable commit, or retry completion order as the last entry. The owning backend executor persists a monotonic local physical landing sequence from the existing operation and receipt evidence before asynchronous reconciliation. For example, if A physically lands first, B physically lands second and commits first, then A reconciles last, B remains the actual last entry; reconciliation cannot move the last actual pointer backward. The next parent and child cycle cannot open until every old cycle entry is committed and no reservation is unresolved. Generate the boundary safe next bag so its first entry differs from the prior actual committed last entry. Before ordinary new cycle concurrency opens, only that boundary safe first reservation may proceed to commit. It must have an authentic receipt and commit; then subsequent new cycle reservations may open. There is no speculative future cycle allocation while a prior turn is uncommitted, released only pending proof, or quarantined. Trace: SEF-PHASE-012.
- Give `reserve` a single serializable SQLite transaction with `operationId`, exact fenced `Session`, immutable `GridIdentity`, parent and child epoch/IDs, state, expiry, and monotonically increasing fencing token. A reservation fences concurrent reuse but does not consume a turn. Duplicate operation IDs return their same durable allocation or a typed terminal result. A parent has at most one consuming reservation in its current parent cycle and a child is never simultaneously reserved twice. Trace: SEF-PHASE-012.
- Persist durable intent before `ARRIVAL_AUTHORIZED` and require the supporting `ArrivalReceipt` type at this phase's SEF-IF-012 commit boundary to match the reservation operation, exact session epoch, world generation, dimension, selected location, outcome, and durable sequence. The real local teleport service is the fixture producer. Reconcile receipts idempotently using a durable operation and receipt uniqueness key before accepting arrival. Only `ARRIVED` evidence from the owned local teleport entry point, followed by the same transaction's durable `COMMITTED` state, consumes parent and child turns and writes the allocation owned recent landing record atomically. The transaction also persists an allocation owned audit linkage or outbox row; a separate idempotent SEF-IF-009 append is delivered afterward and cannot roll back a successful arrival or make authority depend on audit capacity. A successful receipt can therefore be replayed without duplicate consumption. Trace: SEF-PHASE-012.
- `release` accepts only `NonArrivalProof` bound to the same fenced operation and session and returns the turn to availability only for confirmed refusal or independently reconciled nonarrival. A timeout, coordinator crash, lost callback, stale session, or unavailable storage after authorization is `QUARANTINED`, never a clock based release. Reconciliation compares durable intent, receipt, and exact session/location evidence. New allocations fail closed with `PERSISTENCE_UNAVAILABLE`, `FENCED`, `CYCLE_BLOCKED`, or `ARRIVAL_UNCERTAIN` as appropriate. Trace: SEF-PHASE-012.

**Execution order**

1. `P012-TASK-003` delivers allocation diagnostic producers and their command, permission, caps, and support procedure before any proof relies on emitted allocation evidence. Trace: SEF-PHASE-012.
2. `P012-TASK-001` delivers checked partitioning and persistent parent and child cycle state, then proves mathematical ownership, unbiased shuffle mechanics, boundary rules, and generation fencing. Trace: SEF-PHASE-012.
3. `P012-TASK-002` delivers SQLite backed reservations, actual receipt commit, release and quarantine recovery, then proves interleavings, restart cuts, and the real local teleport receipt fixture. Trace: SEF-PHASE-012.

**Required evidence**

- Deterministic property suites cover negative, uneven, extreme and overflow bounds, exact one owner coverage, the 6 by 20 by 20 fixture, all 720 parent completion orders for that six parent fixture, a smaller child model's exhaustive orders, seeded larger property traces, more than 400 parent cycles, restart equivalence, duplicate IDs, physical landing versus receipt or commit interleavings, and negative cases. They do not claim factorial enumeration for a million cell grid.
- Isolated real Xerial SQLite JDBC fixtures, not mocks alone, cover WAL recovery, process restart, durable transaction cuts at every state transition, receipt replay, corruption or unavailable authority refusal, supported API or verified quiesced checkpoint backup with required WAL state, isolated restore comparison, and cleanup.
- The real existing local teleport service fixture obtains its authentic durable `ArrivalReceipt` from the actual server path. It proves idempotent allocation commit and exact session/location fencing only. It does not close Phase 013 safety or any client receipt gate.

**Exit criteria**

- No known mandatory phase-owned defect remains. Every such defect must be fixed and its affected evidence rerun before phase closure.

- SEF-AC-032 passes with 2,400 distinct leaf cells for the required fixture, all partition and cycle properties, actual landing order barriers, child persistence, invalid layout rejection, and durable restart recovery.
- A reservation never consumes a turn. Only an authenticated successful local arrival receipt atomically consumes parent and child state and records the recent landing foundation. Confirmed failures release and every ambiguous case remains quarantined until reconciliation.
- Required backend diagnostics, operator documentation, isolated SQLite proof, cleanup receipts, phase PR merge, resulting product branch verification, and signed phase tag are complete. No known Phase 012 defect or uncertainty is hidden as a Phase 013 safety result.

## Inputs and Upstream Contracts

| Input or contract | Provider | Required state | Validation | Failure behavior |
| --- | --- | --- | --- | --- |
| `WorldRef`, `Session`, `Location` | SEF-IF-001, Phase 001 | Immutable world generation and exact session identity are available. | Reject a receipt whose backend, boot, epoch, world generation, dimension, registry digest, or finite location differs. | `WORLD_REPLACED`, `DIMENSION_MISSING`, `REGISTRY_MISMATCH`, or `STALE_SESSION`; preserve allocation state. |
| Diagnostic control and event envelope | SEF-IF-002, Phase 001 | Default off, authorized, bounded capture API is available. | Control and diagnostic self tests run before allocation fixture tests. | Refuse capture with typed error; allocation correctness remains independent of capture. |
| Audit append and watermarks | SEF-IF-009, Phase 008 | Terminal allocation audit link can append independently. | Audit linkage has an allocation operation correlation and committed outcome. | Never rotate or couple the SQLite authority to audit storage. Report audit failure separately without inventing arrival success. |
| Supporting receipt and nonarrival types | Frozen master supporting response semantics | `ArrivalReceipt` and `NonArrivalProof` supply the SEF-IF-012 commit and release boundary; this phase implements those allocation methods. | The real local teleport fixture verifies matching operation, fenced session, world and location evidence. | Quarantine if the receipt or nonarrival proof is absent, contradictory, stale, or ambiguous. |
| SQLite driver | EXT-009 | Pinned Xerial driver loads in an isolated Java 17 fixture. | Verify driver byte identity and actual transaction, WAL, restart behavior. | Deny new allocations with `PERSISTENCE_UNAVAILABLE`; ordinary gameplay continues. |

## Outputs and Downstream Contracts

| Output or contract | Consumer | Guaranteed state | Compatibility or versioning | Evidence |
| --- | --- | --- | --- | --- |
| SEF-IF-012 allocation authority | Phase 013 safety and preparation | Reservation has immutable grid/session/fence state; only committed actual arrivals consume turns. | This phase implements frozen registry and schema version 1; unknown state or generation rejects. | SQLite restart and genuine local arrival receipt fixture. |
| Immutable grid generations | Phase 014 migration and routing | Bounds, topology, or world identity changes create a new generation and do not reinterpret old cells or tokens. | Ordinary policy revision keeps the existing partition generation, bags, reservations, quarantines, and recent history; Phase 014 owns explicit drain and confirmation for generation changes. | Generation change, stale token, policy reload, and world replacement tests. |
| Committed landing record | Phase 013 recent distance ledger | A committed authentic arrival writes a durable date, location, session, grid generation, parent and child identity atomically with consumption. | Phase 013 may apply distance retention policy but cannot rewrite allocation history. | Atomic commit and restart verification. |
| Allocation audit linkage | Phase 015 assurance | Terminal records carry correlation, state, reason, cycles, fence, and generation without private location leakage in diagnostics. | Allocation transaction persists an idempotent outbox record. SEF-IF-009 remains a separate audit authority. | Correlation, delayed append, retry, and redaction checks. |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
| --- | --- | --- | --- | --- | --- |
| P012-TASK-003 | SEF-REQ-032 | Add the planned `rtp` diagnostic category to the existing SEF debug framework before allocation tests. Enforce `sef.debug.manage`, console safe target selection by grid or operation ID, default off behavior, expiry, caps, redaction, and idempotent off. Deliver source and unit self tests first, then exercise actual on, status, off, no further record, and re enable only once a real grid target exists. | SEF-IF-002, Phase 001 diagnostic service, master diagnostics contract. | Typed `rtp.allocation` and `capture.status` signals plus the RTP support runbook. | Planned backend RTP authority diagnostic adapter, SEF-IF-002. | Command self tests cover on, status, off, denial, absent target, timeout, restart/reset, capture cap, redaction, and no behavioral change while disabled. Actual control acceptance follows a real grid target and precedes diagnostic dependent allocation proof. |
| P012-TASK-001 | SEF-REQ-032 | Implement checked inclusive to half open coordinate partitioning, immutable `GridIdentity` validation, per parent independent durable permutations, unbiased injected RNG, persisted committed and reserved sets, physical landing order boundary barriers, and generation fencing. | P012-TASK-003 source producer, SEF-IF-001, EXT-009, DEC-015, SEF-RISK-014, SEF-RISK-015. | Validated grid and generation schema, exact parent and child interval mapping, persistent cycles. | Planned backend grid authority, SEF-IF-012, `WorldRef`. | Property tests prove negative and uneven ownership, no empty cells, overflow rejection, parent and child minimums, maximum 1,000,000 leaf cells, 6 by 20 by 20 equals 2,400, all 720 six parent completion orders, smaller child exhaustive orders, seeded larger traces, more than 400 cycles, parent and child boundary nonrepeat, and child persistence across parent reshuffles/restart. |
| P012-TASK-002 | SEF-REQ-032 | Implement serialized SQLite reservations, durable intents, exact arrival receipt reconciliation, atomic successful consumption and landing record, separately delivered audit outbox, proven release, uncertainty quarantine, consistent backup and isolated restore recovery. | P012-TASK-003 actual control producer, P012-TASK-001, SEF-IF-001, SEF-IF-009, EXT-009, DEC-017, frozen supporting `ArrivalReceipt` and `NonArrivalProof` semantics. | Durable reservation state machine and idempotent `reserve`, `prepare`, `commit`, `release`, `reconcile` operations. | Planned backend allocation repository and coordinator, SEF-IF-012, existing local teleport receipt entry point. | Real SQLite driver tests and server fixture execute reservation races, actual physical landing versus receipt or commit interleavings, duplicate operation and receipt replay, confirmed failure, timeout, crash cuts, stale session/location, unavailable storage, restart, supported backup or verified quiesced checkpoint restore, and actual local teleport receipt commit. |

`P012-TASK-003` source and unit producer work completes before data proof but does not wait for a real grid target. Its actual control acceptance runs when `P012-TASK-001` creates that target, turns capture off and proves it stopped, then re enables capture before `P012-TASK-002` allocation, arrival, and crash stimuli. This avoids circular completion dependencies. `P012-TASK-001` must finish schema and barrier semantics before `P012-TASK-002` opens concurrent reservation proof. No task proceeds in parallel with a conflicting schema migration. No task changes safety policy, proxy routing, or a future grid generation migration workflow.

## Architecture and Implementation Boundaries

The backend coordinator is the sole writer for allocation state. It serializes SQLite transactions off the world executor and gives workers immutable snapshots only. It does not dereference live world state from a persistence worker. The owning server thread supplies the authentic local teleport arrival observation, physical landing sequence, session and world evidence. Durable intent completes before authorization, followed by an owner-thread identity recheck. The immutable landing observation is persisted asynchronously without blocking that thread; missing durable physical-order evidence after a crash remains uncertain and cannot open a cycle boundary. The transaction rechecks the immutable evidence before commitment. `Location` must be finite and match the reservation's selected candidate exactly. Candidate safety selection is a Phase 013 concern, but a receipt is never accepted solely because a selection was made or a teleport was launched.

Grid identity is immutable: `{gridId, generation, WorldRef, inclusive bounds, parent rows and columns, child rows and columns, policyRevision}`. A changed bound, topology, world identity, generation, or dimension creates a new grid generation under Phase 014's explicit drain and confirmation workflow rather than remapping row or child IDs. A policy revision or ordinary configuration reload is recorded in a grid snapshot and revalidates proposals, but retains the partition generation, bag state, reservations, quarantines, and recent history. Historical state stays keyed by its generation for reconciliation. Existing reservations retain their original generation and reject rather than cross world identity. The allocation authority is a separate local SQLite database or schema with its own ownership, migration and backup rules. It is never an audit spool segment, and audit rotation, central SQL maintenance, or audit capacity pressure cannot delete, truncate, or restore it.

For a span `s = max - min + 1` computed in checked wide arithmetic and count `n`, interval `i` is `[min + floor(i*s/n), min + floor((i+1)*s/n))`. The actual implementation must use overflow checked integer products or quotient and remainder decomposition. It validates every interval nonempty and derives child bounds within its selected parent through the same rule. Rows and columns may be uneven but no coordinate belongs to zero or two cells. A configuration with a one cell parent or child cycle is rejected because it cannot satisfy the cross cycle nonrepeat rule.

State transitions are `RESERVED`, `PREPARED`, `ARRIVAL_AUTHORIZED`, `ARRIVAL_OBSERVED`, `COMMITTED`, `RELEASED`, and `QUARANTINED`. `prepare` cannot consume. `ARRIVAL_OBSERVED` is durable evidence, not yet an implicit successful completion. `commit` requires a matching authentic `ArrivalReceipt`; its SQLite transaction writes the receipt dedupe record, consumes parent and child committed sets, updates the actual physical last landed parent and child only if its persisted backend local physical landing sequence is newer, inserts the durable recent landing foundation, persists an allocation audit outbox linkage, and marks terminal committed. Retrying that receipt returns the same committed allocation. The later idempotent audit append is independent and failures remain visible without rolling back the allocation transaction. Failure proof may release only the same fenced reservation. Every other terminal uncertainty remains quarantined and blocks the affected cycle boundary.

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
| --- | --- | --- | --- | --- |
| Negative or uneven world bounds | Grid validator logs checked span and each derived half open interval. | Every valid coordinate has exactly one parent and child; empty or overflowed cells reject. | Keep previous immutable generation unchanged. | Exhaustive small domains and extreme integer boundary properties. |
| Impossible layout | Parent or child count is below two, leaf count exceeds 1,000,000, or an interval is empty. | Return `INVALID_GRID`; do not round, shrink, or silently substitute values. | Operator supplies a valid new generation. | Validation matrix including single parent, single child, zero width, and 1,000,001 leaves. |
| Parent or child cycle boundary | `rtp.allocation` records actual physical last landing, proposed first, committed and reserved sets. | First of a new cycle differs from actual last successful prior physical landing. The boundary safe first receipt commits before ordinary next cycle concurrency. | Do not open future cycle reservations while any old turn is unresolved or physical outcome is unknown. | All 720 six parent completion orders, smaller child permutations, more than 400 cycles, and parent and child boundary properties. |
| Physical landing versus receipt or commit order | Durable backend local physical landing sequences, receipt arrival, and transaction completion differ. | In delayed receipt fixture A lands first, B lands second and commits first, then A reconciles last. B remains actual last and the pointer never moves backward. Selection, launch, receipt arrival, and retry completion never control the boundary. | Fence stale writers, retain sets and physical sequence, and reconcile receipt records without changing newer last actual state. | Deterministic interleavings plus restart after each receipt and commit cut. |
| Crash after authorization | Durable intent exists but receipt is missing or contradictory after restart. | Mark `QUARANTINED`; timeout does not release. | Reconcile exact operation/session/world/location evidence, then commit or release only with proven nonarrival. | Process restart at every transition and forced transaction cut fixture. |
| Duplicate request or receipt | Operation or receipt uniqueness conflict. | Return original allocation and never consume twice. | Retain immutable result for idempotent caller retry. | Concurrent duplicate ID and receipt replay tests against real SQLite. |
| World or session replacement | `WorldRef` or `Session` differs from durable intent. | Reject stale receipt and preserve or quarantine the old allocation. | Do not map to same named dimension or new connection epoch. | World generation and session epoch replacement fixture. |
| Authority disk, WAL, or driver failure | Actual SQLite write/load error and diagnostic reason. | Refuse new allocation with `PERSISTENCE_UNAVAILABLE`; no in memory claim of reservation. | Recover database from its authority specific procedure and rerun reconciliation. | Isolated real driver WAL, locked database with a 5 second busy deadline, corrupt tail, restart, consistent backup, and isolated restore fixtures. |
| Audit rotation or unavailable audit sink | Audit health differs from authority database health and the allocation outbox shows delivery state. | Allocation transaction remains independent. It records an allocation owned linkage or outbox row, then delivers idempotent append later. No authority file is rotated or consumed. | Retry audit delivery independently; do not roll back a confirmed arrival merely because audit is delayed. | Separate path ownership, outbox retry, and rotation guard test. |

## Diagnostics and Debugging

**Requirement IDs:** SEF-REQ-032  
**Task IDs:** P012-TASK-003, P012-TASK-001, P012-TASK-002  
**Controls:** Planned console and authorized operator controls are `sef debug on rtp <grid-id-or-operation-id> 60`, `sef debug status`, and `sef debug off <capture-id>`, with `sef.debug.manage`; `/sef` is the player spelling only when command policy permits it. Capture is default off, idempotent to disable, uses an explicit grid or operation target, defaults to 60 seconds, and may not exceed 300 seconds.  
**Signals:** `rtp.allocation` has SEF-IF-002 identity fields plus grid generation, parent cycle and ID, child cycle and ID, reservation state, fencing token, receipt outcome, actual previous committed IDs, and typed reason. `capture.status` reports events, bytes, drops, remaining seconds, and exact output path.  
**Collection procedure:** Use the numbered procedure below with the actual property seed or isolated SQLite fixture, a correlation ID, bounded capture, receipt replay or crash stimulus, explicit status/off checks, sanitized evidence, and verified teardown.  
**Headless verification:** On node-1 only after inspecting the selected Gradle task graph for no client or renderer, start and confirm ready the disposable no GUI server before console use, then run the real server side allocation handler and existing local teleport receipt fixture against that server or a server only test harness. Startup or restart has a proposed 120 second maximum, local receipt oracle has 10 seconds, SQLite lock or busy has 5 seconds, and owned shutdown has 30 seconds. It proves backend allocation transactions, receipt reconciliation, and persistence. It does not prove client rendering or player input.  
**Client verification:** This phase launches no Minecraft client because its covered allocation and receipt claims are server observable. Phase 013 retains the residual actual safe landing and client synchronization gates, and Phase 015 retains final product assertions. Server evidence does not close them.  
**Client audio isolation:** No Minecraft client is launched in this phase. If a later dependency adds one, it must use an isolated instance with master output zero before launch, exact Hyprland window and PID binding, verified owned PipeWire or Pulse stream mute including recreation, and teardown of its watcher and temporary audio state.  
**Budgets and privacy:** Preserve master caps of two captures per process, 200 events per second, 10,000 events, 8 MiB, 1,024 queued diagnostic events, default 60 seconds and maximum 300 seconds. Stop and report truncation rather than expanding capture. Capture emits no private location, chat, addresses, credentials, or full session identity beyond sanitized correlation and safe diagnostics. Audit capture is unsampled and independent.  
**Regression and support:** `P012-TASK-003` updates the existing planned `docs/troubleshooting/diagnostics.md` procedure before dependent fixtures. Test enable, status, disable, denied actor, absent target, timeout, restart reset, output cap, redaction, disabled overhead, and unchanged allocation result, then retain only a sanitized diagnostic packet.

| Signal | Source and unit | Expected observation |
| --- | --- | --- |
| `rtp.allocation` | Backend serialized authority, one event per state transition. | `reservationId`, `operationId`, `gridGeneration`, cycles, IDs, committed and reserved set summary, physical landing sequence, fence, desired and actual state, and reason identify why a turn did or did not consume. |
| `rtp.partition` | Grid validator, one event per grid validation. | Inclusive inputs and checked half open partitions prove nonempty coverage or report exact `INVALID_GRID` reason without raw player data. |
| `rtp.boundary` | Parent and child cycle coordinator, one event per boundary reservation or commit. | Previous actual physical landing ID differs from boundary first ID, concurrency remains closed until the first authentic boundary commit, and late reconciliation cannot move the pointer backward. |
| `rtp.reconcile` | SQLite receipt reconciler, one event per durable reconciliation. | Exact session, world generation, location match state and receipt dedupe decision distinguish commit, release, and quarantine. |
| `capture.status` | Diagnostic worker, events, bytes, drops, and seconds. | Status matches active bounded capture; disable or timeout stops new records promptly. |

1. Resolve candidate commit and JAR hashes, Java 17 and Xerial driver identity, configuration generation, grid ID, fixture seed, node-1 runtime directory, result directory, and exact disposable SQLite, WAL, SHM, backup, restore target, log, and server test paths beneath the verified project anchor. Register a `finally` teardown before launch. Inspect the Gradle task graph and confirm it cannot start a client. For a disposable server fixture, write and read back `eula=true`, start no GUI, and confirm readiness within 120 seconds before using console.
2. Run P012-TASK-003 source and unit controls first. Once P012-TASK-001 has created a real grid target, as an authorized console actor enable `sef debug on rtp <grid-id-or-operation-id> 60`, run `sef debug status`, and verify backend side, target, caps, remaining life, and the canonical path `<runtime>/logs/sef/diagnostics/<capture-id>.jsonl`. Run `sef debug off <capture-id>` and status, produce one harmless matching allocation event, and assert no further capture line. Re enable a fresh capture before allocation, arrival, or crash stimulus. An unknown target, denied actor, or unavailable output must return its typed error and not start broad capture.
3. Stimulate the real allocation entry path with the fixed partition fixture, then the genuine existing local teleport service receipt fixture, whose oracle deadline is 10 seconds. In separate disposable runs inject delayed physical landing and receipt order where A lands first, B lands second and commits first, then A reconciles last; duplicate receipt replay; confirmed failure; restart after each durable state; an authorization timeout without receipt; and a SQLite lock or busy case bounded to 5 seconds. Debug resets at restart, so read durable recovery through real status and then enable a new trace only for later stimulus.
4. Create a consistent authority backup through the supported SQLite backup API or a verified quiesced checkpoint that includes required WAL state. Restore it into an isolated target, independently compare grid generations, complete permutations, committed and reserved sets, fences, receipt dedupe rows, quarantine, and recent landing linkage, then reconcile against matching world and session evidence. No central SQL or production target is used.
5. Filter the enabled capture by operation correlation ID and compare transaction rows, durable receipt rows, physical landing sequences, committed and reserved sets, actual last entries, and independent server world/session/location evidence. Assert reserve alone did not consume, a matching successful receipt consumed exactly once, confirmed failure released, and timeout or ambiguity quarantined. Record each exact bounded wait result and failure oracle.
6. Run `sef debug off <capture-id>` and `sef debug status` after the final stimulus. Retain only candidate identity, test counts, sanitized decisive excerpts, configuration digest, seed, and declared unverified Phase 013 gates under the phase verification destination. Redact locations and identities not needed for the proof.
7. Stop only the owned server, test harness, database connection, and any diagnostic worker within 30 seconds, verify their processes and lock holders exited, then remove exact disposable SQLite, WAL, SHM, backup, restored target, server runtime, world, logs, crash reports, coverage, traces, and temporary output after their final consumer. Verify absence without symlink traversal. Preserve source, tracked fixtures, shared caches, personal instances, and required sanitized evidence. Cleanup failure remains a separate open gate.

## Verification Matrix

| Requirement or task | Static or unit | Integration | Real workflow or runtime | Negative and recovery | Execution host and prerequisites | Evidence artifact |
| --- | --- | --- | --- | --- | --- | --- |
| SEF-REQ-032, P012-TASK-003 | Diagnostic command and permission self tests. | Capture status, cap, timeout, restart, absent target, and redaction tests. | Real backend allocation handler emits bounded source signals. | Output unavailable and disabled mode preserve allocation result. | node-1, inspect task graph first, no client, owned nested scratch. | Sanitized capture/status excerpts and cleanup receipt. |
| SEF-REQ-032, P012-TASK-001 | Exhaustive small grid properties, all 720 six parent completion orders, smaller child model exhaustive orders, and seeded larger shuffle properties. | Persisted permutation, committed set, reserved set, physical landing sequence, policy reload, and generation change tests using real SQLite. | Six parent by 20 by 20 child fixture confirms 2,400 distinct leaf cells and more than 400 parent cycles cross child boundaries. | Negative, uneven, extreme, overflow, one cell, and over 1,000,000 layouts reject. | node-1, Java 17, verified EXT-009, no GUI task. | Property seed, order count, schema digest, and sanitized partition report. |
| SEF-REQ-032, P012-TASK-002 | State machine, receipt matching, outbox, and backup comparison properties. | Actual SQLite WAL restart, crash cut, five second lock or busy, duplicate, concurrency, supported backup or verified quiesced checkpoint, and isolated restore suite. | Existing local teleport service produces the genuine durable receipt consumed by allocation commit within 10 seconds. | Confirmed refusal releases. Timeout, crash, stale receipt, location mismatch, and unknown physical outcome quarantine. Audit append failure leaves the successful allocation committed and reports pending or failed audit delivery separately. | node-1, dedicated no GUI server or actual server only harness after task graph review, `eula=true` readback when server fixture is used, readiness within 120 seconds, owned shutdown within 30 seconds. | Durable transaction and outbox rows, receipt correlation, independent restore comparison, server logs, test report, and cleanup receipt. |
| SEF-RISK-014, P012-TASK-001 and P012-TASK-002 | Six parent order enumeration, smaller child permutation properties, physical landing sequence, serializable reservations, and no speculative cycle. | Concurrent reservation threads against one SQLite authority. | A lands first, B lands second and commits first, then A reconciles last. B remains actual last. | Future cycle allocation remains blocked until all old commits resolve, no physical outcome is unknown, and boundary first commits. | node-1, isolated authority database, bounded worker count, five second SQLite busy limit, and 10 second local receipt oracle. | Ordered physical landing and receipt trace, fence values, committed and reserved sets, actual last IDs, and restart result. |
| SEF-RISK-015, P012-TASK-001 | Checked integer arithmetic, policy reload, and generation token tests. | Old database generation is read after new generation creation. | No client or player action is needed because identity mapping is server observable. | World replacement, dimension deletion, topology change, stale token, and overflow reject without reinterpretation. Policy revision retains the generation and all allocation state. | node-1, no GUI, immutable world fixture identities. | Generation map, reload result, rejection reasons, and cleanup receipt. |

## Documentation, Operations, and Release

Implementation updates the repository's existing RTP configuration and operations documentation with inclusive bound semantics, validation limits, immutable generations, policy reload behavior, quarantine recovery, no audit rotation overlap, backup and restore rehearsal, and operator actions for `INVALID_GRID`, `CYCLE_BLOCKED`, `PERSISTENCE_UNAVAILABLE`, `FENCED`, and `ARRIVAL_UNCERTAIN`. It updates the existing diagnostics troubleshooting document and links it through the documentation index and README if command or operational behavior changes. Documentation describes only implemented and verified behavior. No release, production deployment, proxy routing claim, or public publication occurs in this phase. This phase makes no proxy artifact or common projection behavior change. If implementation does change either, merge and verify the approved Forge common digest first, then run the checked proxy merge against that exact digest, verify the resulting `velocity-latest` product branch, and sign both required tags before Phase 013; it must not invent a proxy change merely to satisfy this conditional gate.

## Risks and Evidence Invalidation

| Risk ID and owner task | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
| --- | --- | --- | --- | --- | --- |
| SEF-RISK-014, P012-TASK-001 and P012-TASK-002 | Physical landing sequence, committed and reserved sets, serializable reservations, no speculative cycle. | `rtp.boundary`, receipt dedupe state, physical sequence, sets, and fencing token. | Quarantine ambiguous work and reconcile before a future cycle opens. | Any change to state transitions, receipt schema, SQLite isolation, scheduler, or physical landing evidence. | Rerun all 720 six parent orders, smaller child properties, restart cuts, delayed receipt fixture, and real receipt fixture. |
| SEF-RISK-015, P012-TASK-001 | Checked interval math, immutable world/grid generation, and nonresetting policy reload. | `rtp.partition`, validation reason, generation mismatch, and policy reload result. | Reject invalid generation change and retain prior state for reconciliation. | Any bounds, topology, `WorldRef`, generation migration, or policy reload behavior change. | Rerun exhaustive partition, overflow, stale token, world replacement, and policy reload properties. |
| SEF-RISK-019, P012-TASK-003 | Default off capped diagnostics separate from allocation and audit. | `capture.status`, event drop and duration counters. | Disable idempotently, retain safe status, and rerun allocation with diagnostics off. | Changes to diagnostic controls, caps, or emitted field schema. | Rerun control, overhead, redaction, and unchanged behavior suites. |
| EXT-009, P012-TASK-002 | Isolated pinned real SQLite driver and authority owned files. | Driver/version load, actual WAL transaction, filesystem and lock failure signals. | Deny new allocation and retain unresolved state rather than substituting a mock or in memory store. | Driver bytes, native loading, Java version, JDBC properties, filesystem behavior, or backup/checkpoint behavior changes. | Rerun native load, restart, crash, corruption, consistent backup restore comparison, and cleanup fixtures. |

## Phase Completion Packet

- A focused Phase 012 implementation commit on the sequential Forge phase branch, with signed authorship, conventional required checks, and no unrelated product changes.
- Exact candidate, driver, common contract, configuration, schema, and source hashes; property seeds and counts including all 720 six parent completion orders, smaller child order properties, more than 400 parent cycles, and the 2,400 cell fixture.
- Sanitized real SQLite driver and WAL restart results, state transition crash matrix, physical landing versus receipt or commit order boundary trace, duplicate receipt evidence, local teleport receipt fixture evidence, allocation outbox delivery state, consistent backup and isolated restore comparison, and explicit unresolved quarantine records if any test exposes them.
- Diagnostic command self test, bounded source signal sample, support procedure, redaction evidence, and disabled behavior comparison.
- Documentation diff and links for RTP allocation and diagnostics behavior, without claiming future Phase 013 or Phase 014 proof.
- Exact disposable resource manifest, process shutdown confirmations, retained evidence locations, and verified cleanup absence on each used host. Any leftover keeps the packet incomplete.
- Phase milestone and tracking synchronization, checked Forge PR merge into the applicable product base, resulting product branch verification, required private independent review subject to the established review-capability availability rule, and signed annotated Phase 012 tag before Phase 013 begins. If and only if the approved common projection or proxy artifact behavior changed, the packet also contains the exact approved Forge digest, checked proxy merge, resulting `velocity-latest` verification, and its signed tag.

## Noncanonical Interface Projection

This derived projection is copied from the frozen interface package. It is machine readable evidence, not a second canonical contract.

```json
{
  "phaseId": "SEF-PHASE-012",
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
    }
  ]
}
```

## Next Transition

After every Phase 012 exit gate, PR merge, resulting product branch verification, required signed tag, and cleanup receipt pass, the next executable action is `Phase 013 task 001`: consume SEF-IF-012 through the Forge RTP safety service for strict distance and final safe landing validation. Phase 013 may not weaken allocation fences, consume provisional turns, reinterpret an old generation, or treat this phase's server evidence as its client or safety proof.
