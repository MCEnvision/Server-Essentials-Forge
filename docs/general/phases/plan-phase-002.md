# Phase 002 Execution Plan

> **Plan ID:** PLAN-PHASE-002
> **Phase ID:** SEFAUD-PHASE-002
> **Owner:** Persistence layer
> **Classification:** MANDATORY
> **Master plan:** [plan.md](../plan.md)
> **Phase sequence:** 002 of 007

## Purpose and Ownership

This phase closes `SEFAUD-REQ-006` by auditing and repairing every durable owner and every cross-store invariant at the frozen execution revision. It proves that normal, concurrent, corrupt, interrupted, migrated, restored, and shutdown states preserve the declared authority, never silently discard valid data, never turn damaged enforcement data into empty success, and never duplicate a nonidempotent effect.

The master plan owns product scope, owner decisions, phase topology, compatibility boundaries, and completion authority. This execution blueprint owns only the detailed work for `SEFAUD-PHASE-002`. The Phase 000 durable-owner inventory is the enumeration authority. Source paths named here are evidence-backed starting points, not a replacement for that inventory and not permission to omit a newly discovered owner.

This phase is an audit plus mandatory in-scope remediation phase under `DEC-001`. Destructive and privacy-sensitive tests use only disposable synthetic fixtures under `DEC-005`. No work in this phase publishes a release, mutates production data, changes the pinned platform, or adds unrelated features.

## Evidence-Based Entry State

| Evidence class | Area | Finding | Source or command | Freshness condition |
|---|---|---|---|---|
| OBSERVED | Shared repository lifecycle | `StorageCoordinator` registers repositories before startup, loads them under one managed root, enters recovery mode for `RECOVERY`, `UNSUPPORTED`, or `ERROR`, schedules periodic flushes, serializes flush calls, and gives shutdown flush five seconds. | `src/main/java/com/enviouse/sef/storage/repository/StorageCoordinator.java`; CodeGraph exploration on 2026-09-01 | Invalidated by changes to coordinator registration, startup, flush, shutdown, repository state, or managed-root behavior. |
| OBSERVED | Repository interface breadth | CodeGraph reports 31 runtime implementations of `StorageRepository.audit`; repository documentation describes 27 startup-managed domains, so Phase 000 reconciliation is required before execution. | `StorageRepository` dynamic-dispatch result; `DOCUMENTATION.md` startup description | Invalidated by Phase 000 inventory changes or by added, removed, conditionally registered, or renamed repositories. |
| OBSERVED | Shared publication primitive | `AtomicFileStore` writes and forces a same-directory temporary file, prefers atomic replacement, forces the parent directory, maintains a `.previous` fallback path, supports bounded reads, backup, quarantine, and recovery, and rejects symbolic links and non-regular targets. | `src/main/java/com/enviouse/sef/storage/AtomicFileStore.java`; CodeGraph exploration on 2026-09-01 | Invalidated by filesystem primitive, exception, fallback, path, copy, backup, quarantine, or durability changes. |
| OBSERVED | Coalesced background writes | `CoalescedPersistenceWorker` retains the newest submitted operation on one daemon worker, tracks submitted, completed, successful, and failed revisions, exposes bounded flush and shutdown, and rejects work after shutdown. | `src/main/java/com/enviouse/sef/storage/CoalescedPersistenceWorker.java`; CodeGraph exploration on 2026-09-01 | Invalidated by worker scheduling, revision accounting, failure handling, timeout, or shutdown changes. |
| OBSERVED | Managed JSON contract | Current documentation declares versioned envelopes with `domain`, `schemaVersion`, and `data`; bounded 16 MiB documents; `.backups`; `migration-journal.jsonl`; `.corrupt`; explicit unsupported state; dirty-revision handling; and mutation refusal in unsafe repository states. | `DOCUMENTATION.md`, Section 13 | Invalidated by `StorageService`, envelope, limit, migration, quarantine, dirty-state, or documentation changes. |
| OBSERVED | Durable repository implementations | A source declaration inventory identifies 30 named classes implementing `StorageRepository`, while CodeGraph reports 31 runtime dispatch targets. The difference is an explicit Phase 000 reconciliation item, not authority to assume either count is complete. | `src/main/java/com/enviouse/sef/**`; CodeGraph followed by implementation inventory on 2026-09-01 | Invalidated by interface implementation or runtime registration changes. |
| OBSERVED | Durable owners outside the interface | Current documentation and source identify modular TOML configuration, the integrated player profile repository, coalesced mute, banned item, and alternate-account writers, security audit JSONL, optional file logs, exports, permission manifest, Fancy Tags object and recovery roots, offline player NBT adapters, client project/cache files, vanilla ban files, and persistent player NBT. | `README.md`; `DOCUMENTATION.md`; relevant source exploration | Invalidated by Phase 000 ownership rows, path construction, manager lifecycle, optional-feature, or adapter changes. |
| OBSERVED | Configuration transactions | `ModuleConfigService` owns bounded module TOML, staged publication, exact migration backups, conflict refusal, rollback, watcher debounce, and reconciliation. | `src/main/java/com/enviouse/sef/config/modules/ModuleConfigService.java`; `DOCUMENTATION.md` | Invalidated by module registry, migration, staging, backup, publication, watcher, revision, or rollback changes. |
| OBSERVED | High-risk durable protocols | Documentation identifies economy idempotency and cost reservations, bundle jobs, Fancy Tags publication journals and content-addressed objects, escrow settlement and recovery, offline action durable claims and receipts, and offline inventory backup with conflict protection. | `README.md`; `DOCUMENTATION.md`; `test.md` | Invalidated by operation-state, receipt, journal, identifier, ordering, retry, or recovery changes. |
| OBSERVED | Audit and optional logs | Security audit uses a bounded JSONL queue, rotation, retention, hash chaining, failure health, and bounded shutdown. Optional file logging independently owns fixed descendants of `logs/sef`, is absent while disabled, uses a bounded queue, and records incomplete-session markers. | `DOCUMENTATION.md`; `src/main/java/com/enviouse/sef/audit`; `src/main/java/com/enviouse/sef/commandlog` | Invalidated by event schema, redaction, queue, rotation, retention, path ownership, marker, or shutdown changes. |
| OBSERVED | Existing tests | Tests exist for the shared store and worker, coordinator lifecycle, many domain repositories, configuration, profiles, audit, file logging, alt data, recovery, economy, escrow, Fancy Tags, and offline-related services; CodeGraph reports no direct covering test for `InventoryRecoveryRepository`. | `src/test/java/com/enviouse/sef`; CodeGraph test linkage on 2026-09-01 | Invalidated by test discovery changes, source changes, fixture changes, or Phase 000 coverage reconciliation. |
| OBSERVED | Historical runtime claim | Existing documents claim prior clean persistence, migration, corruption, concurrency, and bounded shutdown evidence, but plan authoring did not rerun it. | `docs/SEF2_ACCEPTANCE.md`; `audit.md`; `test.md` | Historical evidence never becomes phase-closing proof. It must be rerun at the frozen Phase 002 revision. |

No entry-state row is `VERIFIED` for phase closure. Phase execution must bind all proof to the integrated Phase 001 revision and then to the final Phase 002 product commit.

## Scope Boundaries

### Included Scope

- `SEFAUD-REQ-006`: every Phase 000 durable-owner row, including all `StorageRepository` implementations and every durable JSON, TOML, NBT, object, configuration, audit, log, queue, journal, receipt, index, cache, export, backup, migration marker, recovery record, offline adapter, and other store that can affect later behavior.
- Shared persistence primitives and lifecycle coordination, including `StorageService`, `AtomicFileStore`, `CoalescedPersistenceWorker`, `StorageCoordinator`, repository state transitions, periodic and explicit flush, shutdown draining, timeout ownership, restart, and world reuse.
- Path and privacy classification, fixed-root ownership, path normalization, symbolic-link and detectable hard-link behavior, archive and object extraction boundaries, permissions where supported, directory durability, and safe export destinations.
- Schema and envelope versions, source identity, UUID and record identity, revisions, duplicate policy, record and document bounds, nested depth, cardinality, unknown-field policy, unsupported-newer behavior, retention, and compatibility.
- Publication atomicity, backup and rollback, migrations, recovery, corruption quarantine, cross-store invariants, nonidempotent operation outcomes, performance limits, and operator diagnostics.
- Mandatory remediation and regression tests for every confirmed in-scope persistence-integrity defect.
- Documentation and operational changes required to describe behavior actually delivered by this phase.

### Explicit Exclusions

- `FUT-001`: implementation of the sixteen intentionally unavailable control families remains feature expansion.
- `FUT-002`: unrelated commands, UI, integrations, controls, and gameplay features are not persistence remediation.
- `FUT-003`: broad storage-framework replacement or decomposition is excluded unless the smallest safe correction for a confirmed defect requires a bounded structural change.
- `NG-002`: Minecraft 1.21.1, NeoForge 21.1.235, Java 21, Parchment 2024.11.17, the checked-in Gradle wrapper, loader, protocol, and optional-integration contracts remain pinned.
- `NG-003` and `NG-005`: production worlds, production credentials, personal data, and destructive production verification are forbidden.
- Phase 003 command-matrix closure, Phase 004 UI polish, Phase 005 general backend convergence, Phase 006 final clean-checkout verification, and Phase 007 final release-readiness closure remain owned by their phases. This phase still verifies command, UI, and lifecycle paths when they are the real persistence boundary for a Phase 002 invariant.
- Public release, marketplace publication, deployment, credential handling, and irreversible remote operations are not authorized.

## Phase Contract

### SEFAUD-PHASE-002 — Full Codebase Persistence and Database Integrity Closure

**Objective:** At one integrated Phase 002 revision, every Phase 000 durable owner and cross-store invariant passes its schema, bounds, atomicity, directory durability, path safety, concurrency, idempotency, lifecycle, corruption, migration, rollback, recovery, retention, privacy, and real interruption matrix, with every confirmed integrity defect repaired and covered by regression proof.
**Owner:** Persistence layer
**Dependencies:** SEFAUD-PHASE-001, SEFAUD-REQ-002, SEFAUD-REQ-003
**Canonical requirements:** SEFAUD-REQ-006
**Documentation and release impact:** Update `README.md`, `DOCUMENTATION.md`, `docs/README.md` when applicable, `docs/MIGRATION_GUIDE.md`, `docs/SECURITY_REVIEW.md`, `docs/SEF2_ACCEPTANCE.md`, `test.md`, configuration references, and affected storage, recovery, troubleshooting, privacy, performance, and release documentation to match verified behavior. Release readiness remains blocked until later phases.
**Next transition:** `SEFAUD-PHASE-003`

**Entry criteria**

- `SEFAUD-PHASE-001` is merged through its approved pull request, its required checks and independent review are satisfied, the resulting candidate branch is verified, and its signed phase tag exists.
- The execution revision descends from the `DEC-006` candidate lineage and includes no unexplained platform, dependency, schema, configuration, or product-scope drift.
- Phase 000 supplies a complete durable-owner matrix with one stable row per owner, including conditional and disabled owners, plus paths, data class, schema, bounds, lifecycle, tests, and cross-store links.
- Phase 001 has closed filesystem and privacy trust boundaries needed by this phase. Any unresolved unsafe path, sensitive-data, applicable critical or high dependency, or storage-adjacent security finding blocks entry.
- Disposable fixture roots, synthetic identities, process-control harness, filesystem fault-injection mechanism, supported filesystem notes, and sanitized evidence location are available and proven unable to reach production paths.
- The baseline build, test inventory, generated references, current documentation, and repository dirty state are captured without modifying unrelated user work.

**Implementation scope**

- Execute all ten acceptance criteria and four required-evidence classes of `SEFAUD-REQ-006` across every Phase 000 row.
- Repair every confirmed integrity, path, privacy, recovery, migration, ordering, lifecycle, or performance defect without broadening scope or weakening an existing safety gate. This work is governed by `SEFAUD-REQ-001`.
- Keep logical-server state authoritative. Capture immutable snapshots on the owning thread before asynchronous I/O. Client, cache, export, audit, recovery, and generated representations never become independent authority. This work is governed by `SEFAUD-REQ-001`.
- Preserve supported data and compatibility. Any necessary schema change uses a versioned, bounded, validated migration with exact recovery material and a tested rollback path. This work is governed by `SEFAUD-REQ-001`.

**Execution order**

1. `P002-TASK-001` validates entry gates and reconciles the complete durable-owner and cross-store matrix against the integrated Phase 001 revision. This advances `SEFAUD-REQ-001`.
2. `P002-TASK-002` completes path, data-classification, schema, version, identity, bounds, cardinality, compatibility, retention, and test declarations for every owner. This advances `SEFAUD-REQ-001`.
3. `P002-TASK-003` audits and adversarially verifies shared filesystem publication, directory durability, path ownership, safe links, backups, quarantine, exports, and fixed object roots. This advances `SEFAUD-REQ-001`.
4. `P002-TASK-004` executes normal, boundary, malformed, unsupported, duplicate, stale, deeply nested, oversized, and corrupt load and save matrices for each schema family and repository state machine. This advances `SEFAUD-REQ-001`.
5. `P002-TASK-005` proves snapshot ownership, concurrent mutation, coalesced writes, periodic and explicit flush, worker failure, timeout, shutdown, world reuse, and restart behavior. This advances `SEFAUD-REQ-001`.
6. `P002-TASK-006` proves idempotency, durable outcome classification, compensation, and cross-store invariants for every multi-component and nonidempotent protocol. This advances `SEFAUD-REQ-001`.
7. `P002-TASK-007` rehearses every supported migration, backup, restore, and rollback path, including modular configuration and legacy adapters. This advances `SEFAUD-REQ-001`.
8. `P002-TASK-008` closes durable owners outside `StorageRepository`, including configuration, audit and logging, NBT and offline adapters, object stores, indexes, caches, queues, journals, receipts, exports, and recovery records. This advances `SEFAUD-REQ-001`.
9. `P002-TASK-009` executes process-interruption, filesystem-fault, restart, retention, export, restore, and operator-recovery workflows on disposable synthetic worlds. This advances `SEFAUD-REQ-001`.
10. `P002-TASK-010` remediates confirmed defects in smallest-safe dependency order and adds regression proof at each real failure boundary. This advances `SEFAUD-REQ-001`.
11. `P002-TASK-011` runs performance and scale budgets, reconciles documentation and operations, and prepares sanitized evidence. This advances `SEFAUD-REQ-001`.
12. `P002-TASK-012` runs phase-wide clean verification, integrates the phase through the repository workflow, verifies the resulting candidate branch, creates the signed phase tag, and assembles the completion packet. This advances `SEFAUD-REQ-001`.

Tasks 002 through 004 may partition audit rows by independent owner after Task 001 freezes the matrix. Tasks 005 through 009 may prepare independent synthetic fixtures in parallel, but any shared primitive change invalidates all dependent rows and must be integrated before their final rerun. Task 010 repeats until no mandatory defect remains. Tasks 011 and 012 occur only after the last product change.

**Required evidence**

- A complete durable-owner matrix, path and privacy inventory, schema inventory, cross-store invariant graph, and test-coverage map with no missing, duplicate, or unowned Phase 000 row.
- Unit and property-style boundary tests for shared primitives, schemas, repositories, workers, configuration, journals, receipts, indexes, caches, adapters, retention, and redaction.
- Integration and GameTest evidence for domain mutations, restart, concurrent writes, cross-store operations, offline and NBT adapters, recovery, and lifecycle behavior.
- Real disposable-world workflows for clean save and restart, corruption, unsupported data, migration, rollback, process termination at protocol cut points, worker timeout, recovery, retention, and export.
- Before and after file hashes and normalized semantic hashes, operation ids, revisions, journal and receipt states, backup manifests, quarantine paths, diagnostics, worker health, and exact expected and actual outcomes.
- Compatibility evidence for current data, every supported legacy input, unsupported newer input, optional owner enabled and disabled states, dedicated-server classloading, and an older approved artifact plus matching data rollback rehearsal where supported.
- Performance evidence at declared document, record, queue, index, and retention bounds, with no unbounded server-thread filesystem work or whole-store scan in documented hot operations.
- Updated documentation, migration and recovery procedures, sanitized evidence index, complete diff review, required pull request checks, a private independent-review capability preflight and either a passing supported review or an explicit optional unsupported disposition, merge evidence, resulting candidate-branch verification, and signed phase tag.

**Exit criteria**

- No known mandatory phase-owned defect remains.
- Every Phase 000 durable-owner row contains a passed result for path, privacy, schema, version, bounds, identity, cardinality, compatibility, concurrency, flush, corruption, recovery, migration, rollback, retention, and required test layers.
- Shared publication preserves the prior valid state until replacement is durable, rejects owned-root escapes and unsafe links, records platform-specific directory durability behavior, and never destroys recovery evidence.
- Every concurrent, coalesced, scheduled, explicit, startup, shutdown, timeout, and restarted write has deterministic ownership, immutable snapshot semantics, and a verified final outcome.
- Every nonidempotent side effect and cross-store protocol distinguishes not committed, committed, and outcome unknown, and proves retry, compensation, or operator recovery cannot duplicate or lose the effect.
- Unsupported, malformed, corrupt, stale, deep, oversized, duplicate, and semantically invalid data reaches the declared fail-closed state. Damaged enforcement or authorization data never becomes empty successful policy.
- Every supported migration and rollback validates staged output, retains exact nonconflicting recovery material, preserves supported data, and succeeds on representative synthetic fixtures. Failure restores the pre-operation state.
- Periodic, explicit, shutdown, and worker-timeout paths show no silent loss; real process interruption covers each shared primitive and each distinct high-risk commit protocol.
- Cross-store UUID ownership, revisions, references, indexes, claims, receipts, escrow, authorization, expiry, and configuration authority survive restart, repair, and recovery.
- Sensitive durable data is minimized, bounded, retained, protected, redacted or hashed as declared, and excluded from broad exports, fixtures, diagnostics, and evidence.
- Every confirmed phase-owned defect is repaired and its affected evidence rerun. No known persistence-integrity defect remains.
- The Phase 002 pull request is merged through GitHub with all required checks and review satisfied, the resulting candidate branch is verified, and the signed Phase 002 tag is pushed before Phase 003 begins.

## Inputs and Upstream Contracts

| Input or contract | Provider | Required state | Validation | Failure behavior |
|---|---|---|---|---|
| Frozen execution revision and evidence manifest | `SEFAUD-PHASE-000` | Exact candidate commit, environment, artifact lineage, counts, and evidence rules are recorded. | Recompute commit, branch, environment, and manifest identity before fixtures or edits. | Stop and reopen the baseline gate if identity or mandatory inventory is stale. |
| Durable-owner matrix | `SEFAUD-PHASE-000` | Every managed and unmanaged durable owner has one stable row, path class, data class, lifecycle owner, and evidence route. | Reconcile interface implementations, runtime registration, documented paths, filesystem calls, NBT adapters, config, logs, exports, caches, and optional owners. | Stop Task 001, add the missing row through the Phase 000 correction workflow, and invalidate dependent evidence. |
| Closed security and privacy boundaries | `SEFAUD-PHASE-001`, `SEFAUD-REQ-002` | Owned roots, sensitive classes, redaction, export, audit, and trust-boundary findings are closed. | Review the Phase 001 completion packet and rerun relevant path and privacy regressions. | Block Phase 002 entry for an unresolved path escape, unsafe-link, sensitive-data, or audit-integrity finding. |
| Closed dependency findings | `SEFAUD-PHASE-001`, `SEFAUD-REQ-003` | No known applicable critical or high dependency finding affects parsers, serializers, archives, images, compression, or filesystem behavior. | Bind the resolved dependency graph and packaged classes to the Phase 002 revision. | Stop if the graph changed or a persistence-relevant alert is unresolved. |
| Pinned platform contract | `DEC-004` | Minecraft, NeoForge, Java, mappings, Gradle, protocol, and optional-integration boundaries are unchanged. | Compare manifests, build metadata, lock or resolved graph, and compatibility inventory. | Reject silent boundary changes and request an authorized plan revision if a remedy requires one. |
| Safe data and environment contract | `DEC-005` | Only disposable synthetic staging data and isolated paths are used for destructive cases. | Preflight canonical fixture roots and synthetic manifests before every corruption or kill test. | Abort the workflow if any path, identity, or data provenance is ambiguous. |
| Existing runtime and documents | Repository source and tracked documentation | Current implementation and claims are evidence inputs, not proof. | CodeGraph and targeted raw documentation review at the integrated revision. | Treat contradictions as audit findings; do not select the weaker behavior silently. |

## Outputs and Downstream Contracts

| Output or contract | Consumer | Guaranteed state | Compatibility or versioning | Evidence |
|---|---|---|---|---|
| Closed durable-owner matrix | `SEFAUD-PHASE-003`, `SEFAUD-PHASE-005`, `SEFAUD-PHASE-006`, `SEFAUD-PHASE-007` | Every owner and cross-store invariant has current passed evidence or the phase remains open. | Stable row ids survive later evidence refresh; additions reopen inventory and affected proof. | Matrix with owner, path class, schema, bounds, state model, tests, commit, and disposition. |
| Verified persistence primitives | All domain services | Atomic publication, bounded read, path safety, directory durability, quarantine, backup, coalescing, and lifecycle behavior are explicit and tested. | Existing files and supported schemas remain readable; changes use smallest versioned migration. | Primitive unit tests, fault injection, process interruption, hashes, diagnostics, and filesystem notes. |
| Verified repositories and unmanaged stores | Command and integration phases | Domain state persists or fails closed exactly as declared across restart and recovery. | Unsupported newer input stays unsupported; unknown-field policy follows each owner contract. | Per-owner matrix, regression tests, clean and corrupt fixtures, restart evidence. |
| Durable outcome and cross-store contract | Command and backend phases | Retried or resumed operations cannot duplicate or silently lose nonidempotent effects; ambiguous outcomes are explicit. | Stable operation ids, receipt and journal semantics, and revisions are preserved or migrated. | Cut-point table, state hashes, journals, receipts, compensation and restart results. |
| Migration and rollback contract | Operators and later release phases | Supported forward migration and matching rollback are rehearsed with exact recovery material. | No older artifact is pointed at incompatible migrated data unless documented and tested. | Fixture fingerprints, backup hashes, staged validation, failure restoration, rollback logs. |
| Updated operator and developer documentation | Phase 006 and Phase 007 | Paths, schemas, privacy, retention, recovery, migration, diagnostics, commands, and limitations match implemented behavior. | Generated references show zero unexplained drift. | Documentation diff, link checks, generated-reference comparison, procedure replay. |
| Phase completion packet | `SEFAUD-PHASE-003` | Integrated commit, checks, runtime proof, review, merge, tag, invalidation map, and open blockers are complete. | Evidence applies only to named revisions, fixtures, environments, and artifact hashes. | Sanitized packet stored outside the protected plan set. |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
|---|---|---|---|---|---|---|
| `P002-TASK-001` | `SEFAUD-REQ-006` AC1 | Validate entry, enumerate every durable owner, reconcile runtime registration against implementations and docs, and freeze owner, store, path, schema, privacy, lifecycle, dependency, and evidence row ids. | Phase 000 matrix; Phase 001 completion packet; frozen revision | Reconciled durable-owner matrix and cross-store graph with no gaps or duplicates | `StorageRepository`, `StorageCoordinator`, `KernelServices`, config, audit, logs, NBT, managers, adapters, object stores, exports, caches | Deterministic inventory generation or reconciliation; implementation, registration, filesystem-call, resource, and documentation cross-check |
| `P002-TASK-002` | `SEFAUD-REQ-006` AC1, AC8, AC9 | Declare and verify for every row its owned path, data and privacy class, envelope or schema version, source identity, record identity, cardinality, byte and depth bounds, duplicate and unknown-field policy, retention, compatibility, references, and tests. | `P002-TASK-001`; Phase 001 data classes | Completed row contracts and missing-control findings | All owner rows; `StorageService.Document`; TOML, JSONL, NBT, object and adapter formats | Boundary-value fixtures, schema validation, privacy and retention review, cross-reference validation |
| `P002-TASK-003` | `SEFAUD-REQ-006` AC2, AC6, AC9 | Audit atomic replacement and fallback, file and directory forcing, copy, backup, quarantine, restore, fixed-root and export confinement, symbolic links, detectable hard links, path races, permissions, archive extraction, object roots, and recovery-artifact collision behavior. | `P002-TASK-002`; Phase 001 filesystem closure | Shared filesystem assurance record and remediations | `AtomicFileStore`, `StorageService`, config atomic writer, `FileLogSink`, `StorageExportService`, `FancyTagObjectStore`, project/archive stores, offline adapters | Unit tests on supported filesystems, link and path adversarial tests, forced fallback, injected failures before and after publication, hashes and directory-state capture |
| `P002-TASK-004` | `SEFAUD-REQ-006` AC1, AC5, AC10 | Execute the data-state matrix for every schema family and repository state machine: absent, empty, valid minimum and maximum, legacy, future, wrong domain, wrong version, malformed, truncated, deep, oversized, duplicate, stale, invalid reference, invalid enum, invalid UUID, invalid time, overflow, and mixed-validity records. | `P002-TASK-002`; `P002-TASK-003` | Per-owner load, mutation-refusal, save, and recovery results | Every reconciled `StorageRepository` implementation and every non-interface store in the Phase 000 matrix | Parameterized unit tests, corpus tests, repository integration tests, expected diagnostics and unchanged-source hashes |
| `P002-TASK-005` | `SEFAUD-REQ-006` AC3, AC7 | Prove immutable snapshot capture, revision handoff, dirty-state preservation, coalescing, scheduled and explicit flush, simultaneous owners, writer failure, timeout, interruption, shutdown, restart, world reuse, closed-state rejection, and worker replacement ownership. | Tasks 003 and 004 | Concurrency and lifecycle state-machine proof | `StorageCoordinator`, `CoalescedPersistenceWorker`, player profiles, mute, banned items, alt tracking, audit worker, log sink, export worker, watcher and other asynchronous owners | Deterministic latches or barriers, stress loops, thread-ownership assertions, timeout injection, clean shutdown and restart workflow |
| `P002-TASK-006` | `SEFAUD-REQ-006` AC4, AC8 | Map and test every multi-store or nonidempotent protocol, including economy holds and ledger, signs, command-cost refunds, bundle jobs, moderation transitions, kit claims, Fancy Tags publication, escrow settlement, offline action claims and receipts, offline inventory commit and backup, transfers, claims, indexes, and expiry. | Tasks 002 through 005; domain contracts | Durable cut-point table, operation-state invariants, compensation and outcome-unknown rules | Economy, escrow, automation, moderation, kits, Fancy Tags, offline actions, inventory adapters, configuration revisions, related indexes | Cut-point fault injection, duplicate retry, stale revision, crash and restart, journal and receipt reconciliation, exact semantic-state comparison |
| `P002-TASK-007` | `SEFAUD-REQ-006` AC5, AC6, AC7 | Rehearse all supported JSON, TOML, profile, grave, configuration, provider-import, and domain migrations; verify source fingerprints, staged validation, exact backups, journal order, collision refusal, failure restoration, forward migration, and matching rollback. | Tasks 003 and 004; supported fixture list | Migration compatibility matrix and rollback runbook proof | `StorageService`, `ModuleConfigService`, `PlayerData` and `PlayerProfileRepository`, `GraveRepository`, economy import, FTB teleport import, other Phase 000 migration rows | Golden fixtures, source mutation after preview, backup conflict, injected publish failure, restart, semantic hashes, old-artifact plus matching-data rollback where supported |
| `P002-TASK-008` | `SEFAUD-REQ-006` AC1 through AC10 | Apply the full matrix to owners outside coordinator registration and to distinct formats: bootstrap and module TOML, audit JSONL, optional logs, permission manifest, exports, NBT, vanilla ban adapters, Fancy Tags objects, inboxes, journals, client projects and caches, offline player data, recovery copies, indexes, and durable optional-provider caches. | Task 001 matrix; Tasks 002 through 007 controls | Closed non-repository-owner rows and format-specific remediations | `ModuleConfigService`, `SecurityAuditService`, `FileLogSink`, `StorageExportService`, `FancyTagObjectStore`, `FancyTagProjectStore`, `OfflinePlayerInventoryAdapter`, `PlayerData`, manager and adapter owners from Phase 000 | Format-specific unit and integration tests, enable and disable lifecycle, privacy scan, restart, recovery, retention, and cache-authority tests |
| `P002-TASK-009` | `SEFAUD-REQ-006` AC2 through AC9 | Run real disposable-world workflows, terminate the Java process at enumerated publication and protocol cut points, inject filesystem failures, restart, diagnose, restore, rerun retention and exports, and verify operator recovery. | Tasks 003 through 008; isolated synthetic environment | Highest-fidelity interruption, recovery, retention, export, and operator evidence | Dedicated server; world `serverconfig/sef`; `config/sef`; player data; fixed log and object roots | Process controller evidence, preflighted fixture identity, before and after SHA-256 and SHA-512, semantic comparisons, `/sef doctor`, `/sef storage status`, restart and restoration |
| `P002-TASK-010` | `SEFAUD-REQ-006` AC10 | Repair confirmed defects, add narrow regression tests, update necessary migrations, and rerun every directly and transitively invalidated row. | Findings from Tasks 002 through 009 | Focused implementation changes with no open phase-owned defect | Only evidence-backed owners and shared primitives implicated by findings | Defect reproduction first, fix, boundary regression, blast-radius tests, full affected matrix rerun |
| `P002-TASK-011` | `SEFAUD-REQ-006` AC1, AC7, AC9, AC10 | Test upper-bound performance and hot-path behavior, prove bounded memory and queues, reconcile retention and export semantics, update documentation and generated references, and inspect privacy and operational guidance. | Task 010 complete; final implementation revision | Performance report, documentation parity, sanitized evidence index | Build, tests, generators, docs, retention owners, indexes, exports, diagnostics | Declared budgets, heap and queue observations, server-thread checks, reference drift test, link and claim review, procedure replay |
| `P002-TASK-012` | `SEFAUD-REQ-006` all criteria | Run final phase checks after the last change, inspect the complete diff and artifacts, prepare review and pull request evidence, merge through GitHub, verify the resulting candidate branch, tag it, and hand off Phase 003. | Tasks 001 through 011 all passed | Phase completion packet and valid next-transition state | Entire Phase 002 change set and repository workflow | Formatting and static gates, unit tests, relevant GameTests, build, dedicated-server workflows, artifact and secret inspection, required checks, review, merge, post-merge verification, signed tag |

For every task, a decisive failure produces a failed or blocked matrix row with the exact revision, fixture, command or workflow, and sanitized error. A task may continue on independent rows, but the phase cannot close until the failure is repaired and invalidated evidence is rerun. Rollback during development restores the last integrated phase state and its matching synthetic fixture. Recovery of a tested store uses only its declared backup, journal, receipt, or quarantine contract.

## Architecture and Implementation Boundaries

### Durable owner groups

The Phase 000 matrix is authoritative. Current evidence requires at least the following groups to be reconciled:

1. Named `StorageRepository` implementations in `src/main/java/com/enviouse/sef`: `AliasService`, `BundleService`, `CommandProfileService`, `FakeIdentityService`, `SudoPolicyRepository`, `BannedItemsManager`, `CommandSpyRepository`, `AccessLeaseRepository`, `AdminLockRepository`, `ApprovalRepository`, `CommunityStateRepository`, `ServerControlRepository`, `DisguiseService`, `EconomyRepository`, `EconomySignRepository`, `EscrowRepository`, `FancyTagService`, `FilterDataStore`, `AdminPanelService`, `GuiPreferenceRepository`, `OfflineActionRepository`, `KitRepository`, `ModerationRepository`, `MuteManager`, `GraveRepository`, `InventoryRecoveryRepository`, `SocialRepository`, `CooldownRepository`, `LocationHistoryRepository`, and `TeleportRepository`. The execution inventory must identify the additional runtime dispatch target reported by CodeGraph and resolve every difference among named implementations, runtime targets, and coordinator registrations rather than assuming equivalence.
2. Shared storage infrastructure in `com.enviouse.sef.storage` and `com.enviouse.sef.storage.repository`, including atomic files, versioned documents, backup and migration artifacts, diagnostics, exports, coalesced writes, and lifecycle flush.
3. Configuration under `config/sef/common.toml`, `config/sef/modules/*.toml`, `config/sef/motd.json`, world server config, module history and migration state, watcher inputs, GUI overrides, and generated configuration references.
4. Integrated and external data owners outside ordinary repository registration, including `PlayerData`, `PlayerProfileRepository`, `AltTracker`, manager-owned JSON, permission-manifest output, vanilla player and IP ban files, persistent player NBT, and optional-integration imports or caches.
5. Append-only and rotating stores, including security audit JSONL, migration journal, optional `logs/sef` files, session and incomplete-session markers, command-event material, exports, and import reports.
6. Content-addressed, archive, staging, recovery, and cache stores, including Fancy Tags objects, inbox, publication journals, retained revisions, backup manifests, client projects, client cache, recovery staging, and garbage collection.
7. Durable transactional protocols, including economy ledger and reservations, economy signs, automation jobs, kit claims, moderation transitions, escrow records, offline action claims and receipts, offline inventory backups, and their indexes.

### Ownership and data flow

- The logical server owns authoritative state and record identity. Domain services validate and mutate state, capture an immutable snapshot while holding the appropriate owner lock or running on the server thread, then hand that snapshot to persistence. A writer must not traverse mutable game state off-thread.
- `StorageCoordinator` owns registration order, periodic and explicit flush serialization, aggregate recovery state, shutdown flush, and repository diagnostics. Individual repositories own schema validation, in-memory invariants, dirty revisions, mutation refusal, and snapshot serialization.
- `AtomicFileStore` and equivalent format-specific publishers own file and directory durability. A repository must not reimplement a weaker publication path without a documented distinct requirement and equivalent tests.
- Configuration publication is a multi-file transaction owned by `ModuleConfigService`. It must validate the full candidate graph before publication, preserve exact recovery material, restore replaced files on failure, and invalidate dependent authority only after successful publication.
- Append-only audit and log stores have different availability contracts. Mandatory security audit must fail visibly and preserve integrity. Optional file logging remains independent, disabled by default, fixed-path, redacted, bounded, and unable to weaken mandatory audit.
- Exports and caches are projections. They cannot be reimported as authority unless an explicit validated import contract exists. Client caches and local Fancy Tags projects never confer server authority.
- Offline player data and NBT adapters operate on stopped or conflict-checked copies, preserve unknown entries, bind the source revision, and publish only after revalidation. An offline copy cannot overwrite a newer online save.

### Schema, compatibility, and bounds

- Each owner declares schema or envelope version even if the underlying platform format supplies it. Unversioned legacy input must have an explicit fingerprint and migration route. Unsupported newer input fails explicitly and remains unmodified.
- Each collection has a hard cardinality bound and each record has field, string, byte, depth, list, map, time, numeric, and encoded-payload bounds appropriate to its format. Arithmetic uses checked operations where overflow could change authority or value.
- Identity and duplicate policy is explicit for UUIDs, resource locations, names, operation ids, revisions, fingerprints, hashes, receipts, claims, and index keys. Duplicate or ambiguous authoritative records never use silent last-write-wins unless the owner contract explicitly defines and tests that behavior.
- Current platform and public identifiers stay stable. A remediation that changes a persistent field or schema uses the smallest versioned migration and retains compatible reads where required. Unknown fields are preserved only where the existing owner contract promises it.

### Concurrency and performance

- A dirty revision captured for publication clears only if the corresponding immutable snapshot commits and no newer mutation exists. A failed or older write cannot mark newer state clean.
- Coalescing is permitted only for replaceable snapshots. Journals, receipts, audit events, nonidempotent operations, and transitions whose intermediate state matters cannot be discarded by latest-write coalescing.
- Flush and shutdown waits are bounded and observable. Timeout does not transfer ownership to a replacement worker while the original may still publish. World reuse cannot redirect a surviving writer into another world's paths.
- Hot server, tick, render, packet, and event paths perform no unbounded filesystem access, whole-store scan, archive expansion, or serialization. Indexes must remain consistent with authoritative records and be rebuilt or rejected safely after corruption.
- Retention and garbage collection are bounded, previewed where destructive, revision-bound, and limited to proven owned objects. Unknown files and active recovery evidence are preserved.

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
|---|---|---|---|---|
| Missing file or empty initial store | Existence and bounded decode | Enter declared `MISSING` or new state only where empty state is safe; do not misclassify a zero-byte corrupt file as absent. | Create on first valid mutation or restore declared baseline. | Per-owner absent and zero-byte fixtures. |
| Malformed, truncated, deeply nested, or oversized input | Bounded byte and parser-depth checks, schema validation | Stop before mutation, retain or quarantine evidence, enter explicit recovery or error, and emit sanitized diagnostics. | Restore validated recovery material, then revalidate before reopening writes. | Corpus fixtures and unchanged-source hash. |
| Wrong domain or unsupported newer schema | Envelope identity and version selection | Enter `UNSUPPORTED` or error without migration, mutation, or overwrite. | Install compatible code or restore matching data. | Wrong-domain and future-version fixtures across each schema family. |
| Duplicate, stale, or semantically invalid records | Identity, revision, reference, and invariant checks | Reject the affected document or complete transaction according to owner contract; never accept partial authoritative state. | Repair a stopped copy or restore known-good state. | Duplicate ids, stale revisions, broken references, invalid UUID and time fixtures. |
| Symbolic link, nonregular file, unsafe parent, detectable hard link, or path escape | No-follow attributes, canonical owned-root checks, link count where supported | Refuse access before reading, replacing, copying, exporting, or deleting outside policy. | Remove unsafe fixture link and recreate owned path. | Parent and leaf link tests, archive traversal, race and export-path tests. |
| Atomic move unavailable | Injected or platform fallback path | Preserve the last complete target or `.previous`, force file and directory where supported, and report durability limitations. | Recover the previous validated file if publication outcome is incomplete. | Forced fallback with interruption before and after each move. |
| Directory force unsupported | Explicit `IOException` or platform capability result | Surface unsupported durability; do not claim crash durability that the filesystem cannot provide. | Use a supported test filesystem or retain the row as blocked for that environment. | Capability-specific workflow and documentation review. |
| Backup, quarantine, or recovery-name collision | Existing recovery artifact with different hash | Allocate a unique owned name or refuse a conflicting fixed backup; never overwrite evidence. | Preserve both artifacts and require explicit operator selection. | Collision and 10,000-name exhaustion boundary where applicable. |
| Concurrent mutation during flush | Snapshot revision differs from live revision | Commit the captured snapshot, retain dirty state for the later mutation, and flush it in the next pass. | Run the next flush and compare semantic state. | Barrier-controlled mutation during serialization and publication. |
| Coalesced write fails and newer snapshot arrives | Worker revision and failure health | Report the failure, keep authority closed or degraded as declared, and permit a newer verified snapshot only when recovery semantics allow it. | Flush newer snapshot or require operator recovery. | Worker failure then newer submission test. |
| Shutdown flush timeout or interruption | Bounded wait expires or thread interruption | Mark failure, retain ownership of any live worker, prevent unsafe reuse, and record pending owners without reporting success. | Await termination or complete recovery before restarting affected owner. | Timeout, interrupt, surviving-writer, and world-reuse tests. |
| Process death before temporary file force | Cut-point process termination | Accept prior valid target only. Ignore or safely clean the uncommitted temporary file. | Restart and reconcile owned temporary artifacts. | External process kill and hash comparison. |
| Process death after rename but before directory force | Cut-point termination and filesystem result | Report platform-dependent outcome honestly; accept only a complete valid file and preserve recovery material. | Restore previous file when the outcome is invalid or unknown. | Real process kill on a supported durable filesystem. |
| Ambiguous nonidempotent side effect | Missing or incomplete receipt or journal transition | Mark outcome unknown, block blind retry, and reconcile authoritative external and durable state. | Resolve, compensate, or require operator decision according to protocol. | Kill at every transition for cost, bundle, tag, escrow, offline action, inventory, and moderation protocols. |
| Migration source changes after preview | Source hash or revision mismatch | Reject apply without writing candidates or backups beyond safe staging. | Re-preview current source. | Stale migration confirmation workflow. |
| Migration publication fails midway | Injected per-file failure and incomplete publication list | Restore every replaced file, preserve staging and exact backups, and keep old live configuration authoritative. | Validate restoration, fix cause, and rerun from a fresh preview. | Multi-file config failure at every publication ordinal. |
| Recovery backup conflicts with current input | Hash mismatch at fixed recovery destination | Refuse migration or restore; never replace a conflicting backup. | Operator selects or archives the conflicting synthetic fixture. | Backup-conflict test. |
| Retention or garbage collection sees unknown or referenced object | Ownership and reference graph | Preserve unknown, active, referenced, held, or recovery artifacts; delete only proven eligible owned records. | Rebuild preview from current revision. | Preview drift, reference race, legal-hold or equivalent, and unknown-file tests. |
| Export requests sensitive fields | Permission and privacy projection | Exclude sensitive classes unless explicitly and currently authorized; never expose host paths or raw fixture secrets. | Generate a new bounded sanitized export. | Multi-role export and evidence scans. |
| Audit or log queue fills or writer fails | Queue counters and health state | Follow declared fail-closed or degraded behavior, account for dropped work, preserve redaction, and expose health without leaking payloads. | Repair writer, acknowledge marker where required, and restart only after prior ownership ends. | Queue saturation, disk failure, rotation, marker, retention, and shutdown tests. |
| Offline or NBT source changes during edit | Source fingerprint, lock, or revision conflict | Refuse publication and preserve the newer source. | Reload and create a new transaction. | Online transition or concurrent save conflict test. |
| Cache or index is corrupt or stale | Content hash, revision, reference reconciliation | Rebuild from authority or reject it; never allow cache contents to mint authority. | Remove only the proven cache and rebuild. | Corrupt cache, stale index, missing object, and restart tests. |

## Verification Matrix

| Requirement or task | Static or unit | Integration | Real workflow or runtime | Negative and recovery | Evidence artifact |
|---|---|---|---|---|---|
| `P002-TASK-001` | Inventory schema, duplicate, and owner checks | Runtime registration and diagnostic reconciliation | Start a clean dedicated server and capture owners and paths | Inject a missing, duplicate, or conditional owner in test harness | Durable-owner matrix and inventory diff |
| `P002-TASK-002` | Bounds, identity, schema, privacy, and retention tests | Reference and index consistency | Populate minimum and declared-bound synthetic datasets | Duplicate, overflow, stale, invalid-reference, and privacy projection fixtures | Row contract ledger and schema corpus manifest |
| `P002-TASK-003` | `AtomicFileStoreTest`, `StorageServiceTest`, path and archive tests | Repository publication through each shared primitive | Real filesystem publication, backup, quarantine, export, and restore | Forced fallback, link, race, disk-full or injected I/O, directory-force failure | File hashes, directory listings, capability record, diagnostics |
| `P002-TASK-004` | Parameterized repository and format corpus | Coordinator recovery and domain mutation refusal | Start and restart copied worlds for each distinct schema family | Empty, future, malformed, truncated, deep, oversized, duplicate, stale, semantic corruption | Per-owner state and unchanged-source hash report |
| `P002-TASK-005` | Worker, revision, timeout, and state-machine tests | Concurrent multi-owner flush and shutdown | Periodic flush, explicit flush, normal stop, timeout, restart, world reuse | Writer failure, interrupt, late mutation, post-shutdown submit | Thread trace, worker health, revisions, before and after semantic hashes |
| `P002-TASK-006` | Idempotency-key, receipt, journal, compensation, and index tests | Domain protocol fault injection | Execute each high-risk workflow then restart at every durable transition | Retry collision, stale revision, partial success, outcome unknown, compensation failure | Operation cut-point table, journals, receipts, domain state hashes |
| `P002-TASK-007` | Migration codecs and fixture validation | Staged publish and restore across all supported versions | Forward migrate and roll back copied stopped worlds and configs | Source drift, future version, backup collision, corrupt staging, mid-publish failure | Fixture fingerprints, backup and journal hashes, rollback comparison |
| `P002-TASK-008` | Format-specific config, JSONL, NBT, object, cache, and adapter tests | Enable, disable, restart, rotation, reload, offline commit, and optional-provider ownership | Dedicated server plus copied player data and client cache workflows where required | Disabled-path creation, corrupt hash, stale cache, log failure, NBT conflict, raw export denial | Non-repository matrix, privacy scan, cache and adapter evidence |
| `P002-TASK-009` | Harness safety tests | External process and filesystem fault harness | Disposable-world Java termination, restore, retention, export, and operator procedure replay | Kill at each primitive and protocol cut point; damaged recovery material | Process logs, PID and cut-point record, SHA-256 and SHA-512, sanitized diagnostics |
| `P002-TASK-010` | Reproduction and focused regression | Affected owner and cross-store suite | Rerun the actual failed workflow | Original exploit or integrity failure no longer reproduces | Finding ledger, commit, test output, invalidation and rerun map |
| `P002-TASK-011` | Performance and generated-reference gates | Bound-scale repository and index runs | Dedicated-server save, restart, retention, and export at representative scale | Queue saturation, worst valid input, expiry race, large recovery set | Profiles, timing and memory summary, documentation and reference diff |
| `P002-TASK-012` | Full maintained checks | Required GameTests and build | Final dedicated-server persistence and recovery matrix after last change | Secret, artifact, diff, stale-evidence, and blocked-row audit | Completion packet, pull request checks, review, merge commit, signed tag |

### Fixtures and environments

- Use generated UUIDs, synthetic usernames, synthetic addresses that cannot identify a person, artificial messages, namespaced test resources, and disposable worlds. Do not copy production worlds, configuration, logs, player data, or credentials.
- Maintain one immutable valid baseline per schema family, one fixture per supported legacy version, one unsupported-newer fixture, and mutation corpora for malformed, truncated, deep, oversized, duplicate, stale, and semantically invalid states.
- Create each destructive run from a new baseline copy. Preflight the resolved root and a fixture sentinel before corruption, deletion, process termination, or retention cleanup. The harness must refuse broad roots and paths outside its disposable allocation.
- Normalize volatile timestamps, random operation ids, temporary roots, and archive names only for semantic comparison. Retain original sanitized hashes and identifiers in evidence.
- Exercise filesystems used by supported development and deployment environments where directory forcing and atomic replacement differ. A platform capability limitation is recorded, not hidden by a unit mock.

### Rerun order and failure interpretation

1. Run focused primitive, schema, owner, and regression tests.
2. Run all storage, configuration, audit, log, object-store, recovery, economy, escrow, automation, offline, profile, and adapter unit suites.
3. Run relevant GameTests and integration harnesses.
4. Run migration, rollback, concurrent-write, fault-injection, process-interruption, shutdown, restart, retention, export, and recovery workflows.
5. Run `./gradlew check`, maintained static and coverage gates, generated-reference drift checks, and `./gradlew build` in the repository-defined order.
6. Run the final dedicated-server save, stop, restart, and storage diagnostics workflow.
7. Inspect the JAR, generated resources, complete diff, secrets, absolute paths, fixture leakage, caches, logs, build output, and unrelated changes.

A failed or unavailable required workflow is a failed or blocked row. A unit simulation cannot replace a required real process termination, filesystem durability check, migration, rollback, dedicated-server restart, offline data conflict, or operator recovery drill. Any product change after evidence capture invalidates rows whose owner, primitive, schema, dependency, path, protocol, configuration, or harness changed.

## Documentation, Operations, and Release

During execution, update documentation only for verified implemented behavior:

- `README.md`: user-visible paths, supported persistence features, configuration locations, recovery warnings, privacy and retention behavior, commands, compatibility, and known limitations.
- `DOCUMENTATION.md`: complete durable-owner map, envelope and format contracts, shared primitive behavior, concurrency, lifecycle, cross-store invariants, diagnostics, performance limits, failure states, migrations, backups, and recovery.
- `docs/README.md`: navigation for any new or moved persistence, migration, test, verification, troubleshooting, security, or operations document.
- `docs/MIGRATION_GUIDE.md`: exact supported source and target versions, fingerprints, backups, journal behavior, staging, failure restoration, rollback compatibility, and point-of-no-return statement. This phase authorizes no point of no return.
- `docs/SECURITY_REVIEW.md`: verified path, link, privacy, redaction, retention, export, evidence, archive, parser, and recovery findings.
- `docs/SEF2_ACCEPTANCE.md` and `test.md`: exact current automated and real-workflow results, including failed or blocked rows without overstating completion.
- Generated configuration, command, and permission references when a remediation changes their owning metadata. Generation must be deterministic and unexplained drift is a failed gate.
- Focused topic documents under existing repository layout for storage schemas, recovery, verification, performance, or troubleshooting only when the existing documents cannot remain navigable at required depth.

Operational procedures must state stopped-server requirements, backup scope, exact owned paths, diagnostic commands, expected repository states, recovery selection, validation before reopening writes, rollback artifact matching, and evidence sanitization. `/sef doctor` and `/sef storage status` must identify affected owners without disclosing sensitive payloads or host-specific private paths. Recovery guidance must never instruct operators to rename a future schema into a current one, discard quarantine evidence, or point an older artifact at incompatible migrated data.

This phase may prepare release-facing evidence but cannot publish, deploy, mutate production, or mark the product release ready. Phase 006 owns final clean-checkout and artifact proof. Phase 007 owns final documentation reconciliation and endpoint closure.

## Risks and Evidence Invalidation

| Risk | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
|---|---|---|---|---|---|
| A durable owner exists outside the interface inventory | Reconcile Phase 000 rows against registrations, filesystem calls, adapters, config, NBT, logs, objects, and docs | Count, path, or owner mismatch | Reopen inventory, assign one owner, and add the row | All matrices that assumed complete scope | Task 001 and every affected downstream task |
| Interface implementation count differs from runtime registrations | Record implementation, instance, condition, and registration separately | CodeGraph, startup diagnostics, and source wiring mismatch | Correct inventory or registration defect | Coordinator and per-owner evidence | Tasks 001, 004, 005, and final runtime workflow |
| Shared primitive repair changes every store's behavior | Make primitive changes before final owner reruns and track blast radius | CodeGraph and dependency map | Rerun all consumers; rollback isolated unsafe change | Publication, corruption, recovery, migration, and interruption proof | Tasks 003 through 009 and affected performance rows |
| Filesystem mocks overstate crash durability | Require real filesystem and external-process tests | Unit result disagrees with process or directory-force evidence | Record platform limitation or repair protocol | Atomicity and crash-recovery proof | Task 003 and Task 009 on supported environments |
| Async snapshot reads mutable game state | Capture immutable state under owner rules and assert thread boundaries | Race detector, barrier tests, inconsistent semantic hash | Fix capture boundary and rerun owner plus consumers | Concurrency, lifecycle, and cross-store proof | Tasks 005, 006, and related runtime rows |
| Coalescing discards a meaningful transition | Restrict coalescing to replaceable snapshots | Missing journal, receipt, or state transition under burst test | Use ordered durable protocol or explicit reconciliation | Worker and domain outcome proof | Tasks 005 and 006 |
| Process kill test reaches valuable data | Isolated allocation, canonical-path and sentinel preflight, synthetic-only manifest | Root or provenance mismatch | Abort before destructive action; discard only fixture | No product evidence unless fixture integrity was uncertain | Recreate fixture and rerun affected row |
| Migration repair strands older supported data | Versioned migration, exact backup, staged full validation, rollback rehearsal | Semantic hash or compatibility mismatch | Restore source and matching artifact | Migration, repository, docs, release evidence | Task 007 and all changed-schema owners |
| A nonidempotent outcome is retried blindly | Durable operation id, receipt or journal, and outcome-unknown state | Duplicate effect or missing terminal state after cut-point restart | Reconcile, compensate, or block for operator recovery | Protocol, command, integration, and recovery proof | Task 006 plus later dependent command and backend rows |
| Privacy evidence contains sensitive or host-specific data | Synthetic fixtures, redaction, bounded captures, evidence scan | Secret, address, message, username, or absolute-path scan | Quarantine and delete unsafe evidence; recreate safely | The affected evidence artifact | Repeat workflow and evidence review |
| Retention deletes active, unknown, or recovery data | Revision-bound preview and proven ownership/reference checks | Preview drift or post-delete reference failure | Restore from validated synthetic backup and repair eligibility logic | Retention, object, recovery, and privacy proof | Tasks 008, 009, and 011 |
| Configuration watcher races transactional publication | Serialize ownership and bind revisions | Partial module graph, stale reload, or mixed revision | Restore prior module set and reconcile watcher state | Configuration, permission, UI, command, and persistence proof | Task 007, config suites, and dependent rows |
| Optional or disabled owner is omitted | Inventory present, absent, enabled, disabled, failed, and removed states | Path appears while disabled or owner has no lifecycle row | Add state coverage and repair conditional initialization | Owner inventory, path, startup, shutdown evidence | Tasks 001, 008, and final dedicated-server run |
| Later phase changes a schema, primitive, dependency, or store | Persist evidence dependency and invalidation metadata | Final evidence revision differs from product revision | Reopen affected Phase 002 rows before endpoint closure | All proof depending on the changed surface | Exact affected rows plus Phase 006 complete matrix |
| Documentation repeats historical counts or claims | Generate or reconcile claims from current evidence | Count, path, state, or version drift | Correct docs after implementation proof | Documentation and release-readiness evidence | Task 011 and Phase 007 reconciliation |

## Phase Completion Packet

The Phase 002 completion packet must contain:

1. The integrated Phase 001 base commit, Phase 002 head commit, merged pull request number and merge commit, resulting candidate-branch commit, and signed Phase 002 tag.
2. The final durable-owner matrix with stable row ids, implementation and runtime-registration reconciliation, path and privacy classes, schema and version, bounds, identity, cardinality, lifecycle, retention, cross-store links, test layers, disposition, and evidence locations.
3. The shared primitive report for atomic replacement and fallback, file and directory forcing, path and link safety, backup, quarantine, restore, bounded read, coalescing, flush, shutdown, timeout, and world reuse.
4. Per-owner normal, boundary, malformed, unsupported, corruption, mutation-refusal, recovery, restart, and compatibility results.
5. The cross-store invariant and nonidempotent cut-point table with operation ids, revisions, journals, receipts, claims, indexes, compensation, outcome-unknown resolution, and semantic before and after hashes.
6. Migration, backup, restore, and rollback evidence for every supported source family, including fixture fingerprints, exact recovery hashes, staged validation, conflict refusal, failure restoration, restart, and matching-artifact rollback.
7. External process-interruption evidence for each shared persistence primitive and each distinct high-risk commit protocol, including exact cut point, command, environment, result, hashes, diagnostics, and recovery.
8. Concurrency, coalescing, periodic flush, explicit flush, shutdown, timeout, worker failure, retention, export, offline adapter, NBT, audit, log, object-store, cache, and configuration results.
9. All defect records discovered in this phase, their reproduction, root cause, focused remediation commit, regression test, blast radius, invalidated evidence, and completed reruns. No phase-owned defect may remain open.
10. Exact outputs for formatting, static analysis, unit tests, coverage, generated references, relevant GameTests, build, dedicated-server save and restart, artifact inspection, secret scan, and complete diff review.
11. Updated `README.md`, `DOCUMENTATION.md`, documentation index and affected migration, security, acceptance, test, operations, recovery, troubleshooting, performance, and generated-reference artifacts, with link and claim checks.
12. The sanitized evidence manifest naming revision, branch, environment, Java and platform versions, fixture manifest, exact workflow, expected and actual results, disposition, SHA-256 and SHA-512 where applicable, and invalidation dependencies.
13. Required pull request checks, private independent-review capability and result disposition, resolution of all findings received from a supported review, merge evidence, post-merge candidate verification, and confirmation that unrelated user state and excluded `.playwright-mcp/` content were preserved.
14. A downstream handoff stating the verified persistence contracts on which `SEFAUD-PHASE-003` may rely and identifying any later evidence that must be rerun if commands expose a previously untested persistence route.

The completion packet is stored in the approved evidence locations outside the protected plan set. This file is not updated as a status diary.

## Next Transition

After every exit criterion passes, merge Phase 002 through the repository's pull request workflow, verify the resulting candidate branch and all required checks, and create and push the signed annotated Phase 002 tag on that integrated commit. Then reread the master plan and `phases/plan-phase-003.md` through EOF.

The first Phase 003 action must consume the closed durable-owner matrix and bind every executable administrator action that persists or restores state to the verified owner, operation, flush, recovery, audit, and rollback contracts. Phase 003 must reopen affected Phase 002 evidence if it discovers an executable persistence route, cross-store invariant, or owner absent from the matrix. Do not create or begin a Phase 003 branch while Phase 002 is open, queued, waiting for checks, unmerged, unverified after merge, or missing its signed phase tag.
