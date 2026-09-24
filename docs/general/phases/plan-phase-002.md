# Phase 002 Execution Plan

> **Plan ID:** PLAN-PHASE-002  
> **Phase ID:** SEFAUD-PHASE-002  
> **Owner:** Persistence layer  
> **Classification:** MANDATORY  
> **Master plan:** [plan.md](../plan.md)  
> **Phase sequence:** 002 of 007

## Purpose and Ownership

Phase 002 is completed, merged historical work, not a new execution branch. It established the durable-owner audit, storage recovery controls, and persistence proof reused by the active audit only where its candidate, artifact, dependency, configuration, fixture, source, API, and assertion fingerprints still match. Its original reports and doctors remain the historical diagnostic baseline. A later Phase 003 task 010 creates any new bounded capture capability; this phase does not claim that old packets contained later control names or fields.

The master owns scope, decisions, topology, compatibility, and final acceptance. This file preserves the sole full SEFAUD-PHASE-002 blueprint and its completed P002-TASK-001 through P002-TASK-012 meanings. It owns no command-matrix, UI, provider, quality-gate, final-clean-checkout, release, or new-capture work. Under DEC-009, Minecraft Java is one portable client contract. Phase 002 uses node-1 only for no-GUI storage work; no Minecraft client is launched for this pure storage phase.

## Evidence-Based Entry State

| Evidence class | Area | Finding | Source or command | Freshness condition |
|---|---|---|---|---|
| OBSERVED | Historical integration | PR 11 merged Phase 002 into master at `ff17546fdd766d54ed5a01928a4ede2fb42c90de`, and signed tag `phase-002-persistence` targets that merge. | SRC-007, SRC-012 | Recheck remote ancestry and tag signature before relying on integration provenance. |
| OBSERVED | Historical owner closure | The retained completion packet records 30 production storage implementations, 27 runtime registrations, 52 writers, 8 noninterface owners, 24 security-sensitive writers, 19,312 reconciled rows, and 1,922 foreign-key checks. | SRC-012 `phase-002/task-012-completion-packet.md` | Any changed owner, registration, schema, primitive, configuration, fixture, or assertion reopens its row only. |
| OBSERVED | Historical storage proof | The packet records 538 units, 41 GameTests, recovery, migration, corruption, concurrency, native-writer, shutdown, and artifact results. These are historical observations, not a fresh final suite. | SRC-012 | Candidate, artifact, runtime, dependency, source, API, and test semantics must match. |
| OBSERVED | Current reuse boundary | Product and build inputs match retained candidate `1b2e61947cc733574bee377a8404f59f7b129d62`; only planning and command-matrix generator/test inputs changed. | FIND-002, SRC-005 | A changed command-to-store join or invariant requires targeted renewal, not a blanket restart. |
| OBSERVED | Native boundary | JNA 5.14.0 is compile-only, supplied by pinned NeoForge; retained Linux evidence covers opened-object append and no duplicate runtime. Other provider branches require source, API, ABI, and artifact analysis without invented foreign execution. | FIND-006, SRC-010 | Native provider, ABI, package, write path, fixture, or artifact change invalidates dependent evidence. |
| OBSERVED | Present gap routing | Command-specific store joins and changed cross-store invariants remain for active and final phases; historical Phase 002 controls stay reusable where compatible. | FIND-005 | Do not relabel later work as historical closure or move the cursor backward. |

## Scope Boundaries

### Included Scope

- `SEFAUD-REQ-006` historical audit and mandatory remediation of every durable store, database-like owner, schema, migration, atomic write, queue, journal, backup, and recovery path.
- `SEFAUD-REQ-006` reusable proof for shared primitives, every durable owner including nonrepository owners, cross-store protocols, native opened-object writers, interruption, recovery, retention, privacy, and shutdown.
- `SEFAUD-REQ-006` precise invalidation routing when a later command, lifecycle, source, artifact, configuration, provider, schema, native API, or assertion changes a retained row.

### Explicit Exclusions

- `FUT-001` intentionally unavailable command families remain unavailable and negatively tested by their owning work.
- `FUT-002` command-matrix completion, graphical UI, provider diagnosis, and backend convergence belong to later phases.
- `FUT-003` broad storage replacement or unrelated modernization is excluded.
- `NG-002` pinned Minecraft 1.21.1, NeoForge 21.1.235, Java 21, mappings, wrapper, protocol, and integration contracts remain unchanged.
- `NG-003` and `NG-005` prohibit production worlds, credentials, personal data, publication, and destructive production work.

## Phase Contract

### SEFAUD-PHASE-002 — Full Codebase Persistence and Database Integrity Closure

**Objective:** Preserve completed, candidate-bound proof that every durable owner and cross-store invariant passed schema, bounds, atomicity, directory durability, path safety, concurrency, idempotency, lifecycle, corruption, migration, rollback, recovery, retention, privacy, and interruption gates; route only invalidated rows to the current contiguous phase.
**Owner:** Persistence layer
**Dependencies:** SEFAUD-PHASE-001, SEFAUD-REQ-002, SEFAUD-REQ-003, DEC-001, DEC-005, DEC-006, DEC-009, EXT-001, EXT-002
**Canonical requirements:** SEFAUD-REQ-006
**Documentation and release impact:** Historical verified persistence, recovery, privacy, migration, operations, acceptance, and security documentation remains evidence. Later changes update README.md, DOCUMENTATION.md, docs/README.md, docs/MIGRATION_GUIDE.md, docs/SECURITY_REVIEW.md, docs/SEF2_ACCEPTANCE.md, and test.md only after their own evidence. Phase 002 does not publish or claim current release readiness.
**Next transition:** SEFAUD-PHASE-003

**Entry criteria**

- The historical Phase 001 base, Phase 002 PR 11 merge, required checks, independent review disposition, resulting master candidate, and signed tag are independently traceable.
- The retained completion packet is readable, sanitized, candidate-bound, and distinguishes original reports from later plans and captures.
- Each proposed reuse compares commit ancestry, JAR SHA-256 and SHA-512, loader, Java, dependency graph, configuration, provider, schema, native API or ABI, fixture, harness, assertion, and cleanup fingerprints.
- DEC-009 and EXT-001 permit canonical Linux source, artifact, and available-runtime proof without a foreign client or foreign native-runtime gate. EXT-002 supplies in-plan dependency provenance closure.
- Any changed durable owner, writer, primitive, protocol, operation, source, artifact, configuration, provider, schema, native boundary, or assertion opens its dependent row and directs recovery forward.

**Implementation scope**

- `SEFAUD-REQ-006` preserves the complete durable-owner matrix: coordinator repositories, noninterface owners, configuration, JSON, TOML, NBT, objects, caches, queues, journals, receipts, indexes, exports, logs, audit records, backups, migration markers, recovery copies, and optional adapters.
- `SEFAUD-REQ-006` preserves atomic publication, same-opened-object native mutation, path and link safety, directory forcing, bounded reads, backup, quarantine, restore, flush, timeout, shutdown, world reuse, and fail-closed recovery semantics.
- `SEFAUD-REQ-006` preserves schema, source and record identity, cardinality, bounds, duplicate and unknown-field policy, compatibility, migration, rollback, retention, privacy, and cross-store idempotency evidence.
- `SEFAUD-REQ-006` requires later work to rerun only invalidated owner, primitive, protocol, platform-sensitive, and command-to-store rows, retain compatible historical proof, and never infer current success from counts.

**Execution order**

1. `P002-TASK-001` executed SEFAUD-REQ-006 entry validation and complete durable-owner, runtime-registration, documentation, path, privacy, lifecycle, cross-store, and platform-sensitivity reconciliation.
2. `P002-TASK-002` executed SEFAUD-REQ-006 row contracts for path, schema, identity, bounds, compatibility, retention, references, tests, and evidence routes.
3. `P002-TASK-003` executed SEFAUD-REQ-006 shared publication, directory durability, fixed-root, export, link, backup, quarantine, restore, and opened-object writer audit.
4. `P002-TASK-004` executed SEFAUD-REQ-006 normal, boundary, malformed, future, duplicate, stale, deep, oversized, and corrupt schema and repository-state matrix.
5. `P002-TASK-005` executed SEFAUD-REQ-006 immutable snapshot, concurrency, coalescing, flush, timeout, shutdown, restart, world-reuse, and worker-failure matrix.
6. `P002-TASK-006` executed SEFAUD-REQ-006 nonidempotent, multi-store, receipt, journal, compensation, and outcome-unknown cut-point audit.
7. `P002-TASK-007` executed SEFAUD-REQ-006 supported migration, backup, restore, staged validation, collision refusal, failure restoration, and matching rollback rehearsal.
8. `P002-TASK-008` executed SEFAUD-REQ-006 nonrepository owner audit for configuration, audit, logs, exports, NBT, offline adapters, object stores, indexes, caches, and recovery records.
9. `P002-TASK-009` executed SEFAUD-REQ-006 synthetic process interruption, filesystem fault, restart, retention, export, and operator recovery workflows.
10. `P002-TASK-010` executed SEFAUD-REQ-006 focused repairs, regressions, blast-radius classification, and reruns for confirmed phase-owned defects.
11. `P002-TASK-011` executed SEFAUD-REQ-006 performance, bounded-memory, queue, retention, export, privacy, documentation, generated-reference, and operations review.
12. `P002-TASK-012` executed SEFAUD-REQ-006 final historical checks, review, PR merge, resulting-master verification, signed tag, completion packet, and forward handoff.

**Required evidence**

- Retained owner matrix, cross-store invariant table, Linux fixture and artifact manifests, native opened-object report, corruption, migration, interruption, recovery, shutdown, retention, privacy, and cleanup evidence.
- Exact historical PR, merge, tag, source and JAR identity, review and check results, plus a per-row reuse or invalidation ledger that names the current owner of renewed proof.
- Source, API, ABI, resolved-graph, package, and Linux fixture review for native paths; any foreign-platform statement is analysis, retained compatible evidence, or unexecuted scope, never fabricated runtime proof.
- Sanitized reports only. No raw credentials, personal data, private addresses, production worlds, complete logs, or absolute host paths are retained.

**Exit criteria**

- PR 11, master merge `ff17546fdd766d54ed5a01928a4ede2fb42c90de`, and signed `phase-002-persistence` tag remain verified historical provenance.
- Every original owner, nonrepository owner, shared primitive, native boundary, and cross-store protocol has a retained report or an explicit forward invalidation target; no incompatible report is reused.
- The completion packet states its historical candidate and limitation, and does not claim later diagnostics, command joins, quality gates, or final acceptance were completed by Phase 002.
- No known mandatory phase-owned defect remains.

## Inputs and Upstream Contracts

| Input or contract | Provider | Required state | Validation | Failure behavior |
|---|---|---|---|---|
| Durable-owner matrix | Phase 000 historical inventory | One stable row per present, disabled, failed, removed, and optional owner | Compare implementations, registrations, filesystem calls, adapters, resources, documentation, and matrix rows | Add missing owner to current matrix and reopen only its dependents |
| Authority and native boundary | Phase 001 historical proof | Safe path and opened-object contract; JNA supplied by pinned platform | Source, API, ABI, artifact, provider, and report-fingerprint comparison | Treat identity, linkage, package, or provider drift as invalidated |
| Durable outcome contract | SEFAUD-IF-003 | Explicit `not_committed`, `committed`, or `unknown` outcome and recovery locator | Receipt, journal, revision, semantic-hash and restart comparison | Reconcile or compensate before retry; never infer success |
| Available closure | EXT-001, EXT-002 | Linux fixture, artifact provenance, and applicable evidence are available in plan | Read manifests and exact provider, graph, and package records | Keep only affected current row open; do not create foreign prerequisite |

## Outputs and Downstream Contracts

| Output or contract | Consumer | Guaranteed state | Compatibility or versioning | Evidence |
|---|---|---|---|---|
| Closed historical durable-owner matrix | Active command, lifecycle, final, and release work | Stable row IDs, owners, paths, schemas, state, evidence, and invalidation dependencies | Additions and changed primitives preserve prior rows and reopen dependencies | Task 012 packet and current reuse ledger |
| Shared primitive proof | Every durable service | Atomic publication, bounded read, quarantine, restore, coalescing, and lifecycle facts are explicit | Existing supported data remains readable or uses versioned migration | Primitive reports, fault fixtures, hashes, and restart reports |
| Opened-object writer contract | Security and backend consumers | Validation, mutation, flush, and identity recheck bind one descriptor or handle | No path-only fallback or duplicate runtime; DEC-009 governs fidelity | Source, artifact, historical Linux report, and current analysis |
| Cross-store recovery contract | Command and lifecycle consumers | Receipts, journals, revisions, compensation, and unknown outcome are explicit | Stable operation IDs and formats are preserved or migrated | Cut-point and semantic-state evidence |
| Historical handoff | Phase 003 | Commands that persist or restore state must join to verified owner, operation, flush, recovery, audit, and rollback contracts | A newly discovered route opens its exact persistence row forward | Reuse or invalidation ledger |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
|---|---|---|---|---|---|---|
| P002-TASK-001 | SEFAUD-REQ-006 | Reconciled every durable owner and registration | Phase 000 matrix, Phase 001 packet, DEC-009, EXT-001 | Stable owner and cross-store matrix | StorageRepository, StorageCoordinator, KernelServices, config, audit, adapters | Implementation, registration, path, documentation, and OS-route reconciliation |
| P002-TASK-002 | SEFAUD-REQ-006 | Declared path, privacy, schema, identity, bounds, compatibility, retention, and test contracts | Task 001 owner rows, EXT-001 manifests | Per-owner row contracts | StorageService.Document, TOML, JSONL, NBT, object and adapter formats | Boundary fixtures, schema, reference, privacy, and retention review |
| P002-TASK-003 | SEFAUD-REQ-006 | Audited publication and same-opened-object writer safety | Task 002, Phase 001 native closure | Primitive and native assurance report | AtomicFileStore, native audit provider, FileLogSink, exports, object stores | Linux link, swap, write, flush, fallback, rotation, restart, and hash assertions |
| P002-TASK-004 | SEFAUD-REQ-006 | Executed data-state and repository-state matrix | Tasks 002 and 003 | Per-owner load, refusal, save, and recovery results | Repositories and noninterface stores | Corpus, integration, corrupt-source hash, recovery, and blocker assertions |
| P002-TASK-005 | SEFAUD-REQ-006 | Proved snapshots, concurrency, flush, worker, shutdown, restart, and reuse semantics | Tasks 003 and 004 | Lifecycle state-machine proof | Coordinator, worker, profiles, mute, audit, logs, watchers | Barriers, thread checks, timeout injection, restart, and settled-worker assertions |
| P002-TASK-006 | SEFAUD-REQ-006 | Tested nonidempotent and multi-store protocols | Tasks 002 through 005 | Cut-point, compensation, and unknown-outcome table | Economy, escrow, kits, Fancy Tags, offline actions, indexes | Retry, stale revision, crash, journal, receipt, and semantic-state checks |
| P002-TASK-007 | SEFAUD-REQ-006 | Rehearsed migrations, backups, restore, and rollback | Tasks 003 and 004, supported fixture list | Compatibility matrix and rollback proof | StorageService, ModuleConfigService, profile, grave, import owners | Golden fixtures, collision refusal, publish failure, restart, and matching-artifact rollback |
| P002-TASK-008 | SEFAUD-REQ-006 | Closed nonrepository durable owners and formats | Task 001 matrix, Tasks 002 through 007 controls | Nonrepository owner report | Config, security audit, logs, exports, NBT, offline, object, cache, recovery owners | Enable or disable lifecycle, native identity, privacy, rotation, recovery, and authority checks |
| P002-TASK-009 | SEFAUD-REQ-006 | Performed synthetic interruption, fault, retention, export, restore, and operator recovery | Tasks 003 through 008, isolated Linux fixture | High-fidelity interruption and recovery evidence | Dedicated server storage roots, audit, logs, objects, backups, migration, quarantine | Cut-point controller, `eula=true` readback, restart, hashes, doctor, status, and cleanup |
| P002-TASK-010 | SEFAUD-REQ-006 | Repaired confirmed defects and reran invalidated evidence | Findings from Tasks 002 through 009 | Focused fixes and regression record | Only implicated owners and primitives | Reproduction, narrow fix, blast radius, regression, and affected-row rerun |
| P002-TASK-011 | SEFAUD-REQ-006 | Audited performance, retention, export, privacy, documentation, and operations | Task 010 final historical revision | Budget report and documentation parity | Build, docs, generators, retention and export owners | Queue, heap, server-thread, drift, link, claim, and support-procedure checks |
| P002-TASK-012 | SEFAUD-REQ-006 | Closed historical verification, integration, tag, packet, and handoff | Tasks 001 through 011 | Completion packet and forward handoff | Entire persistence change set | Checks, server-only GameTests, build, artifact, secret, diff, review, merge, tag, and cleanup evidence |

Every work package records fixture identity, real entry point, expected invariant, failure oracle, bounded wait, recovery assertion, exact evidence destination, and cleanup boundary. A decisive failure yields a failed or blocked row, preserving independent rows but preventing phase closure until repaired and rerun. Historical package names map retrospectively to required new plan structure; they do not establish that historical packets contained literal later names.

## Architecture and Implementation Boundaries

The complete audit includes all `StorageRepository` implementations and every owner outside that interface. Reconcile coordinator registrations independently from interface dispatch and source declarations. Cover `StorageCoordinator`, `StorageService`, `AtomicFileStore`, `CoalescedPersistenceWorker`, storage envelopes, repositories, module and bootstrap TOML, world configuration, migration journals, backups, quarantine, recovery roots, `PlayerData`, player profiles, alternate-account tracking, persistent player NBT, vanilla ban adapters, security audit JSONL, optional fixed-root file logs, exports, permission manifests, Fancy Tags content-addressed objects and project data, offline player and inventory adapters, client project/cache representations, indexes, receipts, claims, queues, and provider caches.

Logical server state remains authoritative. Snapshot immutable state on its owning thread before background I/O; caches, client projects, exports, audit records, recovery copies, and generated files never become authority. Each publish operation carries owner, schema, source and target revision, snapshot identity, idempotency or receipt, publication stage, durable outcome, queue health, recovery locator, and last-valid semantic hash. `unknown` does not permit blind retry.

Shared primitives own safe root resolution, nonregular and link refusal, descriptor or handle identity, atomic replacement and fallback, file and directory force, bounded read, backup, quarantine, restore, queue bounds, flush, timeout, shutdown, and restart. Security-sensitive native audit writes validate safe type, identity, link-count or reparse state, mutate, flush, and revalidate the same opened object. Ambiguous metadata, substitution, unsafe link state, write or flush failure fail closed, preserve prior valid evidence, and never fall back to a re-resolved path.

The retained Linux fixture is canonical for available persistence execution. DEC-009 requires portable Java behavior and source, API, ABI, artifact, package, and retained-proof applicability scrutiny for every native or provider branch, plus available Linux boundary fixtures. Linux, macOS, and Windows remain supported claims under one portable contract. Foreign execution is recorded as not newly exercised; exact host identity describes only proof actually performed and never creates a mandatory foreign native-runtime row or EXT-001 blocker.

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
|---|---|---|---|---|
| Owner omitted or registration differs | Matrix count, registration, source, docs, path, and runtime-row mismatch | Reject false closure and assign one stable owner | Add owner and reopen only dependent rows | Task 001 reconciliation with no duplicate or unowned row |
| Unsafe path, link, reparse, object swap, or identity loss | Descriptor or handle trace, identity, link or reparse result, post-flush recheck | Fail closed before mutation; no path-only fallback | Preserve last-valid file, quarantine unsafe fixture, reconcile queue | Task 003 synthetic regular, symlink, hard-link, reparse, swap, metadata, write, flush, rotation, restart cases |
| Corrupt, future, oversized, stale, duplicate, or malformed data | Schema, bounds, domain, revision, semantic hash, and recovery state | Refuse mutation and expose explicit recovery or unsupported state | Restore validated backup or quarantine; never empty-success | Task 004 corpus with known before and after hashes |
| Concurrent or interrupted publication | Barrier, revision, queue depth, durable outcome, process cut point, worker state | Preserve immutable snapshot and one terminal committed, unknown, or recovery state | Resolve receipt or journal, compensate if declared, then retry safely | Tasks 005, 006, and 009 interruption and restart fixtures |
| Migration or rollback failure | Fingerprint, staged validation, journal order, backup, collision, and semantic hash mismatch | Stop before opening writes and refuse incompatible rollback | Restore source with matching artifact and exact backup | Task 007 supported legacy, unsupported-newer, collision, publish-failure, restart cases |
| Retention or export exposes private or active data | Ownership, reference, preview, redaction, path, and privacy scan | Refuse unsafe deletion or export; redact evidence | Restore synthetic backup, quarantine unsafe evidence, rerun sanitized case | Tasks 008, 009, and 011 preview, delete, export, and scan checks |
| A later action changes a durable invariant | Current command-store, API, schema, source, and artifact comparison | Mark historical row partial or open, never passed by inheritance | Renew current action join and affected recovery proof | Forward Phase 003 task 010 prose workflow and final regression |

## Diagnostics and Debugging

**Requirement IDs:** SEFAUD-REQ-006
**Task IDs:** P002-TASK-003, P002-TASK-004, P002-TASK-005, P002-TASK-006, P002-TASK-007, P002-TASK-008, P002-TASK-009, P002-TASK-010, P002-TASK-011, P002-TASK-012
**Controls:** Original repository doctors, repository health, `/sef doctor`, and `/sef storage status` are historical controls whose live dispatcher syntax and permission must be discovered before use. They report owner and recovery health without payload disclosure. Planned capture enable, status, and disable controls are owned by Phase 003 task 010 prose and are not backdated here.
**Signals:** The local ten-field report contract records candidate and artifact identity, owner or store ID, operation or correlation ID, schema and revision, publication stage, durable outcome, queue or worker state, recovery state, native identity or provider result, duration or bounded wait result, and sanitized error or reason code.
**Collection procedure:** Follow the numbered historical report replay below. It reads retained doctors and reports, correlates synthetic fixture results, records limits, and never calls an absent later capture command.
**Headless verification:** Inspect the configured task graph before each build or GameTest. Run storage unit, integration, process-controller, no-GUI dedicated-server, and server-only GameTest evidence on node-1 only after proving no client, renderer, or display launches. This proves storage paths, not client rendering or input.
**Client verification:** None for Phase 002. The covered claims are durable storage and server-side native writer paths; a client does not add proof and this phase starts no Minecraft client.
**Client audio isolation:** No Minecraft client is launched because this is pure storage, native-writer, and server-only historical evidence. If a later phase needs a client, it must use an isolated laptop instance, prelaunch master volume zero, `hyprctl clients -j` window and PID binding, owned PipeWire or PulseAudio stream mute and readback, recreation remute, watcher teardown, and stream-disappearance verification.
**Budgets and privacy:** Historical reports use synthetic UUIDs, names, addresses, messages, namespaced roots, bounded fixture corpora, report filters, and sanitized excerpts. Bounded waits are five seconds for shutdown only where the original owner declares it; every new measurement records its actual configured bound. Never retain raw data, credentials, chat, private addresses, absolute paths, broad logs, or production fixtures.
**Regression and support:** Reuse original doctors and reports only after fingerprint comparison. Current support documentation describes only verified controls. Later capture self-tests, permission denial, absent-target, timeout, reload, saturation, redaction, disabled overhead, and support replay are Phase 003 onward work.

| Signal | Source and unit | Expected observation |
|---|---|---|
| candidate and artifact identity | commit and SHA-256 or SHA-512 | Report matches retained source and packaged candidate |
| owner or store ID | stable matrix identifier | One declared owner and no duplicate registration |
| operation or correlation ID | receipt, journal, or synthetic fixture ID | One terminal outcome can be joined across stores |
| schema and revision | envelope or record integer | Compatible, stale, future, and recovery states distinguishable |
| publication stage | persistence state enum | Precommit, commit, recovery, and failure are explicit |
| durable outcome | `not_committed`, `committed`, or `unknown` | Retry never duplicates an effect |
| queue or worker state | count and lifecycle enum | Bounded queue settles or reports explicit failure |
| recovery state | repository health enum | Corruption is quarantined or restored, never empty-success |
| native identity or provider result | descriptor or handle result and API or ABI disposition | Same opened object through flush; unsafe state fails closed |
| duration, wait, and reason | milliseconds, configured bound, sanitized enum | Timeout, interruption, and error retain cause and recovery route |

1. Identify the retained report, candidate commit, artifact hashes, requirement and task, synthetic fixture, host, exact evidence locator, and any preexisting data. Register no new runtime as a report replay creates none; a fresh storage run allocates a unique nested disposable root, records its sentinel and teardown before launch, and reads back `eula=true` if it starts a server.
2. Compare source, API, ABI, dependency, loader, Java, configuration, schema, provider, fixture, harness, assertion, and cleanup fingerprints. Classify each row as compatible historical evidence, current analysis-only evidence, or invalidated forward work; do not alter the historical record.
3. For a renewed server-only row, discover the real configured entry point and task graph, run the authentic storage mutation or fault through its real owner, assert pre and post semantic hashes, durable outcome, journal or receipt, recovery state, native identity where relevant, and bounded wait. Never use a client or bypass the operation under test.
4. Inspect the precise doctor or stored report result by candidate, owner, operation, schema, stage, outcome, worker, recovery, native identity, duration, and reason. Missing fields leave the precise claim unproven. A native or package row uses source and artifact inspection plus available Linux fixture evidence, not a claimed foreign launch.
5. Exercise normal, denied or mutation-refused, boundary, corruption, timeout, interruption, restart, migration, rollback, retention, export, and recovery cases as applicable. On failure preserve the last-valid synthetic state, use declared backup, journal, receipt, or quarantine recovery, and rerun only the invalidated blast radius.
6. Preserve one sanitized packet with identities, minimal nonsecret configuration, fixture and sentinel, expected and actual state, bounded wait, decisive report lines, hashes, disposition, and cleanup result. Scan for secrets, private data, and absolute paths; quarantine and recreate unsafe evidence.
7. Stop only owned server, worker, controller, or fixture process; confirm exit; remove exact runtime, world, logs, crash reports, configuration, downloads, database, trace, coverage, bytecode, and test output after their final consumer. Verify no created resources remain. A read-only historical report replay records that it created no process or scratch path.

## Verification Matrix

| Requirement or task | Static or unit | Integration | Real workflow or runtime | Negative and recovery | Execution host and prerequisites | Evidence artifact |
|---|---|---|---|---|---|---|
| SEFAUD-REQ-006, P002-TASK-001 | Owner, registration, path, and docs inventory | Matrix join with coordinator and noninterface writers | Retained packet replay only | Missing, duplicate, disabled, removed, and stale owner rows | node-1 read-only source and report review | Owner matrix and reuse ledger |
| SEFAUD-REQ-006, P002-TASK-003 | Atomic and native unit suites | Safe-root, link, identity, flush, and rotation integration | Retained Linux native fixture; current source and artifact analysis | Symlink, hard-link, reparse, substitution, metadata, write, and flush refusal | node-1 no GUI, Linux fixture; no client | Native writer report and package disposition |
| SEFAUD-REQ-006, P002-TASK-004 | Corpus and schema suites | Repository state-machine tests | Canonical Linux storage fixture where invalidated | Future, corrupt, duplicate, stale, oversized, and recovery rows | node-1 after no-client task-graph check | Per-owner state matrix and hashes |
| SEFAUD-REQ-006, P002-TASK-005 | Worker and coordinator suites | Barrier, timeout, and lifecycle integration | No-GUI dedicated server only if actual storage owner needs it | Concurrent writer, worker failure, shutdown, restart, and world reuse | node-1, `eula=true` readback for server runtime | Lifecycle report and cleanup receipt |
| SEFAUD-REQ-006, P002-TASK-006 | Receipt and protocol suites | Cross-store semantic-state joins | Synthetic cut-point controller | Retry, unknown outcome, compensation, and restart | node-1 no GUI | Cut-point and invariant table |
| SEFAUD-REQ-006, P002-TASK-007 | Migration and codec suites | Backup, journal, restore, and rollback integration | Synthetic legacy fixture | Collision, unsupported-newer, publish failure, restore, and rollback | node-1 no GUI | Migration matrix and matching-artifact proof |
| SEFAUD-REQ-006, P002-TASK-008 | Format-specific tests | Config, audit, log, NBT, object, offline, cache, and export joins | Retained Linux or renewed server-only storage path | Disabled owner, redaction, rotation, authority, and recovery | node-1 no GUI | Nonrepository report and privacy scan |
| SEFAUD-REQ-006, P002-TASK-009 | Process-controller validation | Fault-injection and restart harness | Disposable no-GUI dedicated server or process fixture | Kill at cut point, forced I/O failure, retention, export, restore | node-1, synthetic root, `eula=true` if server | Interruption report, hashes, doctor, cleanup receipt |
| SEFAUD-REQ-006, P002-TASK-010 | Defect regression suite | Affected owner and primitive reruns | Only required server-only real path | Reproduce first, fix, invalidation, and recovery | node-1 no GUI | Defect record and rerun ledger |
| SEFAUD-REQ-006, P002-TASK-011, P002-TASK-012 | Formatting, static, unit, generated-reference, build inspection | Relevant server-only GameTests and docs checks | Retained historical final workflow or current exact invalidated subset | Secret, artifact, diff, stale-proof, path, and cleanup checks | node-1 after task-graph inspection; no client | Completion packet, tag, review, and handoff |

## Documentation, Operations, and Release

The historical packet records persistence documentation obligations for README.md, DOCUMENTATION.md, docs/README.md, docs/MIGRATION_GUIDE.md, docs/SECURITY_REVIEW.md, docs/SEF2_ACCEPTANCE.md, test.md, configuration and generated references. It documents only verified storage paths, schemas, recovery, rollback, privacy, retention, native opened-object behavior, bounded queues, and operator status without sensitive payloads or private paths. It never instructs an operator to point an old artifact at migrated data, discard quarantine evidence, or claim a foreign native runtime ran.

Later work updates these documents after implementation and evidence, preserves historical reports, and states a current open row honestly. Phase 002 prepares no publication, deployment, production mutation, or release-ready claim.

## Risks and Evidence Invalidation

| Risk ID and owner task | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
|---|---|---|---|---|---|
| RISK-004, P002-TASK-003, P002-TASK-008 | One opened descriptor or handle through validation, write, flush, and identity recheck | Link, reparse, swap, ABI, linkage, identity, or flush mismatch | Fail closed, preserve valid state, repair boundary, reconcile queue | Native audit, privacy, retention, rotation, shutdown, command audit | Affected native source, artifact, and Linux fixture rows |
| RISK-005, P002-TASK-001 through P002-TASK-009 | Stable owner rows, snapshots, receipts, journals, sentinels, and exact roots | Missing owner, divergent hash, unknown outcome, unsettled worker, or root mismatch | Reconcile, restore, compensate, or recreate fixture | Owner, cross-store, corruption, recovery, and interruption proof | Exact owner, primitive, protocol, and dependent action rows |
| RISK-009, P002-TASK-009, P002-TASK-012 | Synthetic-only fixtures, registered teardown, no client task graph, and cleanup receipts | Process, path, report, secret, or cleanup mismatch | Stop owned process, quarantine unsafe evidence, remove exact resources, report leftovers | Affected fixture and evidence packet | Recreate clean fixture and rerun affected workflow |
| RISK-008, P002-TASK-010 through P002-TASK-012 | Candidate, artifact, schema, provider, harness, and assertion fingerprint ledger | Changed source, package, config, API, primitive, protocol, or test semantics | Route forward, retain compatible proof, rerun narrowed blast radius | Only dependent historical rows | Current contiguous phase then final clean suite |

## Phase Completion Packet

The retained Phase 002 packet contains the Phase 001 base, Phase 002 head, PR 11, merge, master verification, signed tag, owner matrix, platform and reachability ledger, shared primitive report, native opened-object report, per-owner outcomes, cross-store cut points, migration and rollback proof, interruption evidence, performance and privacy results, defects and regressions, check outputs, documentation review, sanitized manifest, review disposition, and Phase 003 handoff.

For every historical or renewed check it records exact owned resources, fixture sentinel, evidence destination, process shutdown, and cleanup. It preserves branches, tags, source, tracked fixtures, user data, personal instances, shared caches, preexisting `.playwright-mcp/` content, and required sanitized evidence. It never uses blanket cleanup, broad process termination, or a broad repository delete. Any unreconciled leftover is reported separately and prevents closure.

## Next Transition

Phase 003 begins only after recognizing this historical phase is already merged and tagged. Its first task, task 010, reconciles current evidence and delivers bounded capture before its dependent command, provider, lifecycle, feedback, store, audit, and sink joins. It consumes compatible Phase 002 owner, operation, flush, recovery, audit, and rollback contracts, reopens exact persistence rows for new routes or changed invariants, and never rewrites Phase 002 history or moves the cursor backward.
