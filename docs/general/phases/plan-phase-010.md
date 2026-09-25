# Phase 010 Execution Plan

> **Plan ID:** PLAN-PHASE-010  
> **Phase ID:** SEF-PHASE-010  
> **Owner:** Audit investigation service  
> **Classification:** MANDATORY  
> **Master plan:** [plan.md](../plan.md)  
> **Phase sequence:** 010 of 016

## Purpose and Ownership

This phase turns the Phase 008 durable audit journal and the Phase 009 actual-capture/coverage receipts into a permissioned Forge-native investigation surface. It delivers stable textual inspection, filters, pages, detail, hover, summaries, bounded export, the versioned extension/query API, parity registry, operational status and pause/resume behavior, and the dual-engine fenced migration procedure. It never creates a custom client screen or menu, and an inspection click is observational rather than a block interaction or world mutation.

SEF-REQ-025 and SEF-REQ-029 are owned here. The master remains the sole authority for product scope, global interfaces, phase order and requirements. This blueprint defines only this phase's executable work, evidence, recovery, and handoff to restoration in Phase 011 and full parity evidence in Phase 015.

## Evidence-Based Entry State

| Evidence class | Area | Finding | Source or command | Freshness condition |
|---|---|---|---|---|
| OBSERVED | Capture boundary | Phase 009 must provide actual terminal capture, coverage descriptors, EventKey ordering, four watermarks, explicit gaps and uncertain tails before a query may claim completeness. | SEF-IF-009, FIND-023 through FIND-025, Phase 009 packet | Any journal, redaction, coverage, schema, pack digest, or watermark contract change. |
| OBSERVED | Reference parity | CE24.1 has inspection, lookups, restore/rollback jobs, previews, granular permissions and a typed Bukkit API. Its types and platform APIs are not a Forge contract. | SRC-304, SRC-316 through SRC-322, FIND-106 and FIND-107 | Reference version, mapped row, or chosen Forge equivalence changes. |
| OBSERVED | Query safety | The reference distinguishes queued and stored lookup. Its historic queue/file sinks can drop under pressure. | SRC-304, SRC-314, SRC-315, FIND-104 | Any pending-view or durable-journal behavior changes. |
| PROPOSED | Investigation service | Every actor, server, world/dimension, location/radius, time, kind, material/item/entity, cause and correlation filter is available across offline-origin history, with explicit pending and completeness state. | FIND-025, SEF-REQ-025 | Phase 009 capture or Phase 008 central ingestion contract changes. |
| OBSERVED | SQL fixtures | MySQL Community 8.4.11 and MariaDB Community 11.4.13, using MariaDB Connector/J 3.5.10, are isolated verification pins only. Runtime compatibility remains unproven until actual fixtures pass. | SRC-601 through SRC-610, FIND-110 and FIND-111 | Artifact, driver, TLS/trust, host library, schema, or advisory change. |
| PROPOSED | Migration | Migration is original SEF work, not a claimed CoreProtect public feature. It uses portable schema, isolated empty target, write fencing plus spool, checkpointed copy, count/hash/watermark verification and source preservation. | Section 15, FIND-107, FIND-110 | Engine/schema/principal/fence/checkpoint design change. |
| OBSERVED | Client residue | Native click inspection, hover/detail and rich chat are client-received presentation claims; headless assertions cannot close them. | SEF-REQ-025, SEF-REQ-030, master host contract | Renderer, candidate, private endpoint, client path, window, or mute evidence changes. |

## Scope Boundaries

### Included Scope

- SEF-REQ-025. Permission-controlled native inspection click, stable filtered lookup pages, event/cause detail and hover, near/count/material summaries, pending state, completeness watermarks, gaps, uncertainty, and JSONL/CSV export.
- SEF-REQ-029. Versioned CE24.1/catalog/API parity registry, original Forge adapter API, queue versus central-history status, pause/resume/status controls, operator-safe manual purge contract, and bidirectional MySQL/MariaDB migration evidence.
- SEF-IF-010. Deliver its exact schema-versioned asynchronous records, methods, errors, ownership boundary, and adoption tests before Phase 011 consumes snapshots.
- Query performance and retention behavior needed to make investigation truthful: indexed immutable cutoffs, page and export budgets, redaction before every presentation sink, explicit coverage-gap states, and no hidden loss.
- Actual isolated MySQL and MariaDB fixtures, documentation, sequential Forge integration, and conditional complete exact-digest proxy integration where an applicable proxy artifact, projection or behavior changes.

### Explicit Exclusions

- SEF-REQ-028 restoration execution, preview application, undo, world mutation, item give, conservation proof, and durable RestoreJob/RestoreStep rows belong to Phase 011. This phase may bind their future input selection and verify that parity rows remain open; it must not claim their completion.
- SEF-REQ-027 complete audit conservation, representative load envelope, and final parity closure belong to Phase 015. This phase supplies bounded local proof only.
- Capture adapters, cause attribution, and loss-ledger creation remain Phase 009 and Phase 008 responsibilities. This phase reads their declared coverage and does not synthesize a false complete history.
- Custom GUI, screen, menu, client protocol, client-side audit storage, arbitrary console execution, automated normal purge, production database access, credential collection, and a public release are outside this phase.

## Phase Contract

### SEF-PHASE-010 — Scoped Investigation, Extension API, Parity Registry, and SQL Migration

**Objective:** Deliver a bounded, permission-rechecked, watermark-honest investigation and extension API over actual audit history, with a versioned parity registry and reversible, fenced MySQL/MariaDB migration procedure.  
**Owner:** Audit investigation service  
**Dependencies:** SEF-PHASE-009, EXT-006, EXT-007, EXT-008, DEC-008, DEC-009, DEC-010, DEC-011, DEC-012, DEC-013, DEC-014  
**Supporting contract and risk dependencies:** SEF-IF-002, SEF-IF-003, SEF-IF-004, SEF-IF-005, SEF-IF-006, SEF-IF-007, SEF-IF-009  
**Canonical requirements:** SEF-REQ-025, SEF-REQ-029  
**Documentation and release impact:** Update root README, docs/README.md, DOCUMENTATION.md, docs/features/audit/, docs/migrations/, docs/operations/backup-recovery.md, docs/troubleshooting/diagnostics.md, and docs/test/ only for implemented behavior. No public release or production rollout.  
**Next transition:** SEF-PHASE-011, implement fixed restoration jobs only after this phase has integrated, been tagged, and its frozen query/selection handoff is available.

**Entry criteria**

- Phase 009 is merged into forge-1.20.1, tagged, rebuilt from the resulting product commit, and provides actual capture coverage plus SEF-IF-009 watermarks/gaps.
- The shared diagnostics, command policy, rich native presentation, bridge/authority and qualified-home contracts pass their own integrated versions.
- The exact MySQL 8.4.11 and MariaDB 11.4.13 fixture artifact/driver hashes, isolated nonproduction runtime anchors, headless task graph, separate runtime/migration principals, and cleanup boundary are revalidated.
- Before any control is exercised, the audit diagnostic producer and its registered target self-test are available. A missing producer or unregistered target stops dependent acceptance.

**Implementation scope**

- Implement SEF-REQ-025, SEF-REQ-029 through the work packages and acceptance obligations below.

- Stable cutoff snapshots freeze query membership by origin epoch and sequence. A query uses a 15 second maximum execution budget, defaults to 10 rows per page and rejects a requested page size above 50, permits valid continuation beyond 10 pages, uses opaque cursor expiry at 10 minutes, and caps exports at 100000 rows and 64 MiB. Trace: SEF-PHASE-010.
- Every page, detail, hover, export continuation, action token redemption, and pause/resume/status response repeats command-policy and sensitive/hidden/retention authorization. A revoked permission cannot consume a prior cursor or token. Trace: SEF-PHASE-010.
- The native Forge inspector uses the ordinary interaction path, cancellation when inspection applies, literal rich chat messages and existing action tokens. It neither opens a custom GUI nor modifies the clicked block, inventory, world, audit record, or normal interaction when the inspector is inactive or denied. Trace: SEF-PHASE-010.
- The parity registry carries reference version, feature/action/API row, Forge-native owner, status, evidence and platform reason. Feasible rows cannot be marked unavailable for convenience. Phase 011 owns restoration-row implementation; Phase 015 owns full closure. Trace: SEF-PHASE-010.
- Pause explicitly creates a coverage state and visible watermarks/gaps for its exact scope and interval. Resume does not infer unrecorded activity or silently erase paused coverage. Trace: SEF-PHASE-010.
- Manual purge is an operator-only, audited preview/confirm flow: a preview freezes exact selected EventKeys and selection digest, shows resulting intentional gaps, requires a fresh confirmation, and refuses any selection when the Phase 011 restoration-dependency provider is unknown, unavailable, or reports unresolved dependencies. It never becomes automatic normal purge. Trace: SEF-PHASE-010.
- Migration preserves source authority and uses portable, versioned schema/data copies. New source writes fence at a durable watermark and continue into the local spool while copying an empty isolated target. Only equal source/target row counts, partition hashes, blobs, coverage/gaps and watermarks allow atomic connection selection. The current proof covers only phase-available central schema/data. Future restoration-job authority, backup and migration integration remain Phase 011 and Phase 015 gates. Interrupted work resumes from the exact checkpoint or rolls back selection while retaining the source intact. Trace: SEF-PHASE-010.
- The predicate compiler has fixtures for actor and pseudo-actor, origin, world, radius, box, selection, global scope, UTC and relative time, action and outcome, positive and negative registries, literal text prefix, confidence and correlation. Near/count/material summaries and permitted location jumps reuse the same authorization predicate and existing canonical typed command routes. Trace: SEF-PHASE-010.
- Export streams only to an operator-configured owned canonical directory. It rejects symlink escapes, writes to a protected temporary sibling, atomically publishes only complete output, removes incomplete output, and escapes or refuses CSV formula-leading cells. Authorized readers retain full allowed content, while credentials are redacted before every sink. Trace: SEF-PHASE-010.

**Execution order**

1. P010-TASK-001 establishes the parity registry, query API, permissions, stable cutoff model, all-filter predicate compiler, page/detail/hover/export reauthorization, and rich textual result model. Trace: SEF-PHASE-010.
2. P010-TASK-003 implements the original extension API, adapter registration/coverage receipt validation, pending versus central lookup, and API compatibility fixtures. Trace: SEF-PHASE-010.
3. P010-TASK-004 implements portable schema/version records, SQL indexes, query budgets, cursor expiry, export streaming and actual MySQL/MariaDB query-plan fixtures. Trace: SEF-PHASE-010.
4. P010-TASK-002 implements fenced, checkpointed MySQL-to-MariaDB and MariaDB-to-MySQL migration plus backup/rollback drills with separate principals after the portable schema outputs exist. Trace: SEF-PHASE-010.
5. P010-TASK-005 adds the natural click inspector, summary/status/pause/resume/purge controls, and operational failure semantics over the delivered query service. Trace: SEF-PHASE-010.
6. P010-TASK-006 delivers diagnostics producer coverage, command self-tests, failure fixtures, and the required silent-laptop inspection/click/chat receipt. Trace: SEF-PHASE-010.
7. P010-TASK-007 updates operator/API/migration documentation and runs the required Forge-first integration, exact approved shared-digest projection, and, when applicable, proxy retest, checked proxy PR merge, resulting branch verification and signed tag before post-merge verification. Trace: SEF-PHASE-010.

**Required evidence**

- Permission, filter and frozen-cutoff unit tests; actual database index plans and ingest-during-page tests on both exact SQL engines; offline-origin and pending-ingestion lookup evidence; full sensitive/hidden redaction sentinel scan of page, detail, hover and export.
- Natural Forge interaction evidence showing inspection cancellation without a block mutation, plus a required actual silent laptop click/hover/rich-chat receipt. Server logs are supporting evidence only.
- Actual two-direction isolated SQL migration, interruption at DDL/copy/fence/cutover points, exact checkpoint/resume or rollback, source preservation, count/hash/watermark/coverage/gap checks, and least-privilege principal tests. Future job-authority migration is not claimed.
- Parity matrix with every CE24.1/catalog/API row owned, versioned, evidentially mapped, and restoration rows explicitly assigned to Phase 011 and final closure to Phase 015.
- Sanitized diagnostics, documentation checks, candidate/provenance evidence, sequential integration evidence, and verified cleanup for every owned resource.

**Exit criteria**

- No known mandatory phase-owned defect remains. Every such defect must be fixed and its affected evidence rerun before phase closure.

- Inspector, every listed filter, page, detail, hover, summary, bounded export, pending/watermark/gap/uncertainty display, pause/status, extension registration, and dual-engine migration paths pass their required proof without custom GUI or unintended world mutation.
- The query service fails closed on stale/revoked authorization, expired cursor, scope/sensitive denial, incomplete history, unsupported adapter, limit breach, and unavailable SQL; it displays honest coverage rather than incomplete results as complete.
- Migration drills pass in both directions with source fence/spool, empty target, exact counts/hashes/watermarks, resume/rollback, original-source preservation, and separate runtime/migration principals.
- Restoration rows remain visibly unimplemented and bound to Phase 011; full parity/conservation/load remains open for Phase 015.
- No known mandatory phase-owned defect remains, all required Forge integration gates and signed tag pass, and cleanup is complete.

## Inputs and Upstream Contracts

| Input or contract | Provider | Required state | Validation | Failure behavior |
|---|---|---|---|---|
| Audit journal and coverage, SEF-IF-009 | Phase 008 and Phase 009 | Stable EventKey, origin epoch/sequence, redacted event, watermarks, gaps, uncertain tail and coverage descriptor are durable. | Schema/digest and actual Phase 009 capture receipt. | Refuse completeness claims, show explicit unavailable/incomplete state, and block Phase 011 snapshot adoption. |
| Diagnostics and configuration, SEF-IF-002 | Phase 001 | Default-off bounded capture, registered target resolver, enable/status/disable and typed event envelope work before investigation tests. | Source-unit self-test before any stimulus. | Stop dependent diagnostic acceptance; do not use unbounded logs as replacement. |
| Command policy and presentation, SEF-IF-003 and SEF-IF-004 | Phase 002 | Reauthorization and recipient/session/revision action tokens apply to all result surfaces. | Denial, revocation, token replay and literal-value fixtures. | Deny continuation and emit readable console/native-chat result. |
| Network and world scope | Phases 005 and 007 | Authority/session/world/home identities remain versioned and observer-aware. | Stale session/world and hidden activity queries. | Suppress or redact results; never resolve by display name. |
| SQL fixture contract | EXT-006, EXT-007, EXT-008 | Isolated MySQL 8.4.11 and MariaDB 11.4.13 with MariaDB Connector/J 3.5.10, validated TLS/trust and typed URLs. | Artifact hashes, non-root startup, driver handshake and separate principals. | Leave SQL gate open; no production fallback or arbitrary JDBC configuration. |

## Outputs and Downstream Contracts

| Output or contract | Consumer | Guaranteed state | Compatibility or versioning | Evidence |
|---|---|---|---|---|
| SEF-IF-010 AuditQuery, QueryPage, lookup, export, lookupPending and registerAdapter | Phase 011 and Phase 015 | Immutable cutoff and reauthorized page contract with watermarks, coverage and explicit typed failures. | Registry version 1, schema version 1; additive only until paired compatibility review. | Contract fixtures, API compile test, cursor and revocation evidence. |
| Fixed-selection query input | Phase 011 restoration | Query snapshots expose EventKeys, cutoffs, coverage, gaps and uncertainty but never execute restoration. | Selection digest is future RestoreJob input, not a Phase 010 job record. | Snapshot determinism and incomplete-history refusal. |
| Versioned parity registry | Phase 011 and Phase 015 | Every reference row has an owner, disposition, acceptance/evidence link and truthful platform/version reason. | Registry record versioned with CE24.1 source identity. | Row completeness validator and later closure gate. |
| Migration/backup operational contract | Phase 011, Phase 015 and operators | Fenced dual-engine data handoff, checkpoints, source retention, rollback and principal separation. | Versioned schema/migration checkpoints and exact engine/driver pins. | Both-direction interrupted migration and restore drill. |
| Native investigation presentation | Operators and Phase 015 | Literal rich chat with pagination/action token semantics; no GUI or client payload. | Existing SEF-IF-004 presentation version. | Laptop click/hover/chat receipt and artifact inspection. |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
|---|---|---|---|---|---|---|
| P010-TASK-001 | SEF-REQ-025, SEF-REQ-029 | Build the versioned CE24.1/catalog/API parity registry and query service. Compile every required typed filter, actor/scope authorization, frozen origin cutoff, stable sort and cursor; apply identical predicate and redaction policy to page, detail, hover, summary and export. Reauthorize every continuation, use literal native-chat values, and make missing/gap/uncertain coverage visible. | Phase 009 actual capture and coverage; SEF-IF-003, SEF-IF-004 and SEF-IF-009; DEC-008 through DEC-014. | SEF-IF-010 lookup/export model, parity rows, filter/index specification, permission matrix, cutoff/cursor policy. | Planned audit investigation service, parity registry, command adapters, native component renderer, SEF-IF-010. | Individually executable fixtures cover actor and pseudo-actor, origin, world, radius, box, selection, global scope, UTC and relative time, action/outcome, positive/negative registries, literal text prefix, confidence and correlation; summaries and permitted location jumps prove the same predicate and canonical typed routes. Test default 10 rows, valid continuation beyond page 10, rejection above 50, nested sensitive NBT/command sentinel scans, ingest-between-page snapshot, offline-origin query, and executable CE24.1 categories: inspection, lookup/filter/page/summary, queue/status, preview/apply/cancel, rollback/restore/undo, recorded-item give, purge, permissions and typed extension API. Each category has an owner, evidence, or explicit technical platform reason. |
| P010-TASK-002 | SEF-REQ-029 | Implement the original SQL migration/backup procedure in both directions. Fence source writes at a durable watermark while spooling new events, require an empty isolated target, copy phase-available versioned central schema/events/blobs/watermarks/coverage/gaps, verify count and partition hash, and atomically select the target only after complete verification. Record checkpoints so interruption resumes safely or rolls selection back while retaining source. | P010-TASK-004 pure schema/query implementation and Phase 008 journal/backup contracts; source/target runtime and temporary migration principals. Diagnostic-dependent real acceptance waits for P010-TASK-006 self-test, but pure schema/checkpoint tests may run when their producers exist. | Versioned migration checkpoint and rollback record, engine compatibility matrix, backup/restore runbook and principal matrix. | Planned audit migration coordinator, schema migrators, spool fence, checkpoint ledger, operator configuration. | Actual MySQL-to-MariaDB and MariaDB-to-MySQL drills after P010-TASK-006; DDL, copy, fence and cutover interruption fixtures; source write spool replay; empty-target refusal; counts/partition hashes/blobs/watermarks/gaps equality; original source remains usable after rollback; runtime account cannot perform DDL and migration account is revoked after completion. Safe synthetic dependency-provider fixtures prove unavailable/unknown/unresolved future restoration dependency refusal only; they do not claim future job storage or migration. |
| P010-TASK-003 | SEF-REQ-029, SEF-REQ-025 | Deliver the original Forge-native AuditAdapterV1 extension API and adapter registration path. Validate descriptor schema/version, declared family, supported pack digest, field policy and privilege boundary. Separate pending local spool lookup from central durable history, and expose status, coverage and unsupported adapter reason without executing external code on a SQL or server thread. | P010-TASK-001; SEF-IF-009; Phase 009 supported adapter receipts and world/cause identities. | registerAdapter and lookupPending implementation, API reference fixtures, adapter compatibility matrix and explicit unsupported state. | Planned audit API module, coverage registry, immutable worker snapshots, SEF-IF-010. | Original adapter contract tests for valid/invalid descriptor, duplicate/version mismatch, forged coverage, queue versus central divergence, offline origin, and worker ownership. No Bukkit type or copied CoreProtect implementation is accepted. |
| P010-TASK-004 | SEF-REQ-025, SEF-REQ-029 | Implement portable query schema evolution, parameterized index-backed predicates, immutable cutoff materialization, bounded worker/pool handoff, opaque ten-minute cursors, streaming JSONL/CSV export and exact result budgets. Set a 15 second query budget, default 10 rows per page, reject more than 50 rows, permit continuation beyond page 10, and cap export at 100000 rows or 64 MiB. Export resolves an operator-configured owned canonical directory, rejects symlink escape, protects partial output and escapes or refuses CSV formula cells. | P010-TASK-001 and P010-TASK-003; isolated engine pins and Connector/J; Phase 008 central schema/ingestion. | Portable query migrations, index-plan evidence, export receipt, limit errors and engine-specific query fixture. | Planned audit SQL repository, query worker, cursor/token store, SEF-IF-010 and diagnostics. | Actual MySQL 8.4.11 and MariaDB 11.4.13 explain/index-plan checks, SQL timeout/cancel test, long filter/Unicode/path injection denial, continuation beyond page 10/default 10-row/51st-row/cursor-expiry/export boundary tests, symlink escape and incomplete-file cleanup, CSV formula escape/refusal, simultaneous ingest snapshot and redacted export scan. |
| P010-TASK-005 | SEF-REQ-025, SEF-REQ-029 | Add ordinary Forge click inspection and textual controls for lookup, near/count/material summaries, permitted location jumps, status, queue/pending state, scoped pause/resume and manual purge preview/confirm. Inspection cancels only its active audited interaction and cannot change a block, container, item or audit event. Pause produces an explicit coverage interval. Purge freezes exact selection and refuses an unknown, unavailable or unresolved future restoration-dependency provider. | P010-TASK-001; Phase 009 terminal events; existing action-token policy; future Phase 011 dependency-provider contract and safe synthetic reference fixture only. | Inspector command/interaction contract, operator control grammar, pause coverage records, audited purge preview/confirmation contract. | Planned Forge audit interaction adapter, query controller, audit health/coverage view, SEF-IF-004 and SEF-IF-009. | Natural right/left interaction fixture through real Forge entry point with normal player permission, cancellation oracle and before/after world digest; inactive/denied click preserves normal action; stale token/revoked permission/expired preview refuse; pause/resume reports exact gap; unknown, unavailable or unresolved dependency provider blocks purge without consulting a central job table. |
| P010-TASK-006 | SEF-REQ-025, SEF-REQ-029 | Deliver investigation-specific typed diagnostics before dependent acceptance, including query decision, cutoff, page, export, inspector interaction, parity disposition, migration checkpoint and purge decision signals. Register auditable target types, default-off controls and source-unit self-test. Execute the required laptop-only residual inspector/click/hover/native-chat receipt after the headless proof is ready. | P010-TASK-001 through P010-TASK-005; SEF-IF-002; actual candidate and host identity. | Registered diagnostic categories, self-test, sanitized support packet and residual client evidence. | Planned diagnostics producers in investigation, migration and interaction services; docs/troubleshooting/diagnostics.md. | Console enable/status/off permission tests, absent target and output-unavailable test, self-test then disable then re-enable before the real stimulus, 60-second capture limit, and actual silent laptop click/hover/chat proof. |
| P010-TASK-007 | SEF-REQ-025, SEF-REQ-029 | Document implemented permissions, filters, watermarks, gaps, pause/purge, extension API, SQL pins, migration/rollback, backup, privacy and cleanup. After all local proof, integrate Forge first. Freeze exact approved common digest. If any applicable proxy artifact, interface projection or behavior changed, retest the proxy against that digest, merge its checked proxy PR, verify the resulting velocity-latest branch and create/push its signed phase tag before progression. | P010-TASK-001 through P010-TASK-006; master Section 11 branch/provenance contract. | Documentation/link evidence, checked Forge PR merge, signed tag, post-merge rebuild, and conditional complete proxy integration receipt. | README, docs index, audit/migration/operations/troubleshooting/test documentation and paired provenance manifest. | Documentation examples use limits/errors and no false restoration claim; required checks and private review subject to the established review-capability availability rule; fetched forge-1.20.1 containment and signed tag; exact digest comparison; applicable proxy retest, checked merge, resulting branch verification and signed tag; no proxy branch is invented when artifact, projection and behavior are unaffected. |

P010-TASK-001 establishes authorization and immutable selection before controls or restoration consumers. P010-TASK-003 follows the query model and interface freeze; P010-TASK-004 then consumes its API outputs. P010-TASK-002 begins migration implementation after P010-TASK-004 establishes portable schema/query primitives. P010-TASK-005 consumes the query contract for controlled inspection after P010-TASK-001. P010-TASK-006 depends on the implementation outputs of P010-TASK-001 through P010-TASK-005, not their completed real acceptance. After its producer self-test, run all dependent real-engine and interaction acceptance, followed by P010-TASK-007 documentation and integration. No future restoration implementation is an entry condition.

## Architecture and Implementation Boundaries

The Forge backend owns the interaction path, world-thread cancellation decision and final native message handoff. The investigation service owns typed query construction, authorization snapshots, immutable cutoff creation, bounded SQL work and response shaping. SQL workers receive immutable filter/value records and redacted snapshots only; they never dereference live entities, ItemStacks, levels, capabilities or permission providers. The owner executor rechecks actor scope, permission revision, recipient session, target revision and sensitive/hidden field policy before every response action.

A cutoff is a map from origin/journal epoch to maximum query-visible sequence at query creation. Ordering is deterministic by origin, epoch and sequence after the authorized predicate, so ingest after creation cannot slide an event into a later page. Cursors are opaque, recipient/authorization and cutoff bound, retain no full sensitive event payload, expire at ten minutes and fail with CURSOR_EXPIRED. Queries exceeding 15 seconds fail with QUERY_LIMIT rather than returning a partial result as complete. Query pages default to 10 rows and reject more than 50 rows, but valid cursors may continue beyond ten pages. Exports stream at most 100000 rows or 64 MiB into the configured canonical owned directory; action, field, content and resource filters cannot bypass these caps.

The permission model separates metadata lookup, scope/world/server access, private content, hidden activity, export, pause/status, retention/purge administration, adapter append/registration, and later restore. A field is returned only when its fieldPolicy and event sensitivity authorize the current actor. The same policy is applied before native hover, detail, rich chat literal, console fallback, CSV/JSONL cell and export receipt. An authorization revision change invalidates outstanding page/cursor/action state. Access decisions themselves are audit events with safe metadata, not duplicated sensitive content.

The parity registry is a local versioned record, not an imported or copied CoreProtect API. Each row contains pinned reference/version, category, requested function, Forge-native equivalent, canonical owner phase/task, implementation status, test evidence, limitations, and N/A reason. The public extension surface is original AuditAdapterV1 and SEF-IF-010 only. It validates descriptors and coverage receipts at registration; it does not load unknown plugins, run adapter code on the SQL worker, or accept Bukkit world/player/block types. Pause is a scoped coverage state with exact start/end and reason. It displays a gap/uncertainty to any query that intersects it and never silently changes EventKey ordering.

The SQL service uses the dedicated audit database, not proxy authority SQLite, and uses portable representation, binary identities, UTC and parameterized statements. Runtime query accounts have only their required permissions. Temporary migration accounts have separately scoped schema/copy capabilities and are revoked after the drill. Typed operator configuration constructs JDBC URLs, forces allowLocalInfile=false, uses sslMode=verify-full for remote fixtures with tested trust material, bounds properties/timeouts/pools, and rejects unknown or player-provided values. No production endpoint, credential, system service or shared cross-host database file is in scope.

Migration begins by taking a recoverable source backup. It creates an exact source fence at central durable/query-visible watermark and routes subsequent source events to the Phase 008 local spool. The target must be owned, isolated and empty. Copy uses ordered, idempotent chunks with a durable checkpoint recording source/target identity, schema version, range and partition hashes. Before cutover it verifies phase-available schema, rows, payload blobs, coverage descriptors, gaps and all four watermarks. Selection changes atomically only after replaying fenced spool data and rechecking the watermark. An interruption preserves the source and checkpoint; resume is bounded and idempotent, while rollback restores source selection and leaves the target isolated for forensic cleanup. Phase 011 owns restoration-job authority and later backup/migration integration. The migration never deletes, truncates or purges original source data.

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
|---|---|---|---|---|
| Permission revokes between page one and page two, detail, hover or export | query.decision includes permission revision, continuation type and denial reason. | Reauthorize each continuation and return SCOPE_DENIED or SENSITIVE_DENIED with no rows, hover values or export bytes. | Invalidate cursor/action binding and record safe access decision. | Role-revocation fixture across every sink, with secret/hidden sentinels absent. |
| Ingest advances while a user pages | query.cutoff and audit.watermark show fixed origin epoch/sequence versus current values. | All pages use original cutoff; later records appear only in a new query. | Preserve cursor until ten-minute expiry, then require new query. | Actual SQL concurrent-ingest fixture on both engines. |
| Empty/offline history has a gap, paused interval or uncertain tail | QueryPage coverage/watermark and audit.loss fields identify origin/range/reason. | Empty result displays coverage, pending status and uncertainty; no complete-history claim. | Resume capture only prospectively; retain gap descriptor and investigate journal health. | Offline origin, disk/SQL gap and scoped pause/resume fixtures. |
| Inspector click would normally alter a block or container | inspector.interaction reports active, cancellation, event key and before/after world digest. | Active authorized inspection cancels only the observed interaction, emits native result, and leaves world/inventory digest unchanged. Inactive/denied inspection preserves normal interaction. | Disable inspector token/state and retain no mutation. | Natural player click on block/container under real Forge entry point, including denied and expired token paths. |
| Cursor/filter/export abuse, Unicode/markup or oversized selection | query.decision reports filter class, normalized size, limit and reason. | Typed parser rejects unknown/ambiguous/injected filters; literal renderer cannot create actions; limits fail closed. Export resolves only an owned canonical directory, rejects symlink escape and atomically publishes complete files. | Remove incomplete temporary output and return a readable error. | Malicious filter/path/CSV formula injection, symlink escape, 15-second timeout, continuation beyond page 10, default 10-row page, 51st row, 100001st row and 64 MiB boundary tests. |
| Adapter declares unsupported or forged coverage | adapter.registration includes adapter/version/pack digest/state/reason. | Reject descriptor or mark explicit unsupported/partial coverage; no false parity row. | Keep previous valid descriptor and rerun only after compatible registration. | Unknown schema, duplicate, stale pack digest and forged coverage receipt fixture. |
| Pause or purge would silently erase evidence | audit.pause and purge.decision contain scope, selection digest, gaps, dependency-provider state and reason. | Pause creates visible coverage interval. Purge remains manual preview/confirm, exact-selection bound and refuses unknown, unavailable or unresolved Phase 011 dependency-provider state. | Expire preview without deletion; record intentional gap only after confirmed allowed purge. | Preview expiry, changed selection, confirmation replay and safe synthetic provider fixture for unknown, unavailable and unresolved dependency refusal. |
| SQL migration interruption or target contamination | migration.checkpoint includes direction, stage, source/target IDs, watermark, count/hash and reason. | Target must be empty; DDL/copy/fence/cutover interruption leaves source selected and writable through spool. | Resume from checkpoint or roll back selection and preserve original source/backup. | Both directions with interruption at each stage, hash/count/watermark verification and source recovery. |
| Runtime principal can migrate or migration principal remains active | migration.principal reports principal role/capability/revocation. | Runtime account lacks DDL/copy privilege; temporary migration account is scoped and revoked on all exits. | Refuse migration, revoke temporary principal and retain source. | Privilege-denial and post-run revocation fixture on both SQL engines. |
| Diagnostic control is missing, unregistered or output unavailable | capture.status and query.diagnostic fields report target, state and reason. | Do not begin dependent stimulus. Return target/output error without fallback to global logs. | Repair producer/target registration, rerun self-test, then re-enable capture before stimulus. | Missing target, denied permission, output unavailable, timeout and restart/reload tests. |

The phase has no general world restore rollback because it performs no world restoration. Its only click behavior is controlled inspection cancellation with an independent no-mutation oracle. Future RestoreJob dependencies are data references only; any unresolved job blocks purge rather than allowing it to manipulate job state.

## Diagnostics and Debugging

**Requirement IDs:** SEF-REQ-019, SEF-REQ-025, SEF-REQ-029, SEF-REQ-030  
**Task IDs:** P010-TASK-001, P010-TASK-002, P010-TASK-003, P010-TASK-004, P010-TASK-005, P010-TASK-006, P010-TASK-007  
**Controls:** Default-off console control is sef debug on audit <registered-target> 60, followed by sef debug status and sef debug off <capture-id>. It requires sef.debug.manage, accepts an audit registered target only, has a 60 second default bounded duration, and permits console use without an owner joining. The same grammar is available as slash command where a player is needed. Disable is idempotent.  
**Signals:** query.decision, query.cutoff, inspector.interaction, audit.pause, purge.decision, adapter.registration, parity.row and migration.checkpoint carry correlationId, candidateDigest, configuration generation, desired/actual typed maps, reason, units, logical side, boot and sequence through SEF-IF-002.  
**Collection procedure:** Use the numbered runbook below. First make the producer available, register a safe target, run its source-unit self-test, disable it, then create a new capture and re-enable it before the actual query/click/migration stimulus.  
**Headless verification:** node-1 runs only inspected genuinely headless task graphs, source-unit tests, actual isolated SQL fixtures and no-GUI dedicated Forge server paths. Server console proves policy, query, filter, watermarks, status, migration and no-mutation state, but cannot prove client-received hover or native click presentation.  
**Client verification:** The required residual evidence is a laptop client using the approved candidate against the private dedicated server: natural inspection click, rendered rich chat, and hover/detail action. Server-only or simulated-player evidence cannot replace it.  
**Client audio isolation:** Discover the laptop project and isolated client path without inferring it from node-1. Before launch set that disposable pinned-version instance master audio volume to zero. Verify active Hyprland and discrete renderer; bind the owned window address, class, title and PID from hyprctl clients -j to the launched candidate; correlate only that process tree to its per-application PipeWire or PulseAudio stream; mute it with wpctl or pactl and read back verified muted state before interaction. Recheck/mute a replacement stream after reload/device/reconnect. If renderer, ownership, stream identity or mute proof is absent, stop the owned client and leave this client gate open. Never mute a default sink, microphone, unrelated application or personal instance. At teardown stop the owned client and watcher, verify process/stream exit, and remove temporary audio state.  
**Budgets and privacy:** Inherit default 60 seconds, maximum 300 seconds, 200 events per second, 10000 events, 8 MiB per capture, 1024 queued diagnostic events and two simultaneous captures per process. Diagnostic outputs are default-off and separate from unsampled audit. Redact secrets, private content and addresses before output or retention. Query maximum is 15 seconds, pages default to 10 rows and reject more than 50, cursors expire at 10 minutes, and export is at most 100000 rows or 64 MiB.  
**Regression and support:** Test control permission, absent/removed target, output unavailable, timeout, reload/restart, self-test, off-mode/no-new-record, redaction and query/click/migration recovery before dependent acceptance. Update docs/troubleshooting/diagnostics.md, README and docs index with the sanitized collection and cleanup procedure.

| Signal | Source and unit | Expected observation |
|---|---|---|
| query.decision, filterCount:u16, permissionRevision:u64, continuation:enum, resultRows:u16 | Backend owner executor, one decision per lookup/page/detail/hover/export | Every continuation reauthorizes, applies all filters and reports scope/sensitive/limit denial before data emission. |
| query.cutoff, originEpochs:u16, cutoffSequence:u64, cursorAgeSeconds:u16, elapsedMs:u32 | Query worker, one snapshot/page | Stable membership across pages, cursor expiry at 10 minutes, and query stop at 15 seconds. |
| inspector.interaction, active:bool, cancelled:bool, blockBeforeDigest:sha256, blockAfterDigest:sha256 | Forge owner thread, one natural interaction | Authorized inspection is cancelled with equal digest; inactive or denied inspection leaves normal action available. |
| audit.pause, scope:string, first:u64?, last:u64?, reason:enum, resumed:bool | Audit coverage owner, one transition | Pause/resume yields explicit coverage state and never invents captured sequences. |
| purge.decision, selectionDigest:sha256, selected:u64, jobDependencies:u64, confirmation:enum | Audit retention controller, one preview/confirm decision | Exact preview selection is required; unresolved job dependencies refuse deletion. |
| adapter.registration, adapterId:string, adapterVersion:u32, packDigest:sha256, state:enum | API registry, one registration | Only compatible validated coverage receives a receipt; unsupported evidence remains explicit. |
| parity.row, referenceVersion:string, category:enum, ownerPhase:string, status:enum | Parity registry, one row validation | Restoration rows point to Phase 011 and full closure remains Phase 015. |
| migration.checkpoint, direction:enum, stage:enum, sourceWatermark:u64, targetRows:u64, partitionHash:sha256 | Migration coordinator, one durable stage | Empty target, fence/spool, count/hash/watermark verification and reversible source selection are visible. |
| capture.status, events:u32, bytes:u64, dropped:u64, remainingSeconds:u16, output:string | Diagnostic worker, status operation | Registered target is bounded, self-test passes, off stops capture, and re-enable precedes real stimulus. |

1. Resolve the exact approved Forge candidate, commit/common digest, Forge 47.3.12/Java 17 task graph, SQL engine/driver hashes, node-1 project anchor, isolated server/database/runtime paths, laptop path if needed, private endpoint, and disposable evidence destination. Register every owned process, database, server, client, watcher, log, temporary migration target and scratch output for cleanup. Before any server launch set the exact disposable eula.txt to eula=true and read it back.
2. Start only the no-GUI node-1 server after task-graph confirmation. Wait no more than 120 seconds for server readiness, then confirm the actual candidate is ready before using its console. Start each isolated SQL engine and wait no more than 60 seconds for readiness. Register an audit target from the parity/coverage registry. Only after readiness issue sef debug on audit <registered-target> 60 and sef debug status. Run a harmless source-unit producer self-test, inspect the typed JSONL correlation record under the discovered runtime logs path, and confirm no secret/live object/content entered it.
3. Issue sef debug off <capture-id>, check sef debug status, trigger one harmless matching action and prove no new record. Create a fresh capture with sef debug on audit <registered-target> 60, confirm ready status and capture identity, then run one real query, permission-revocation continuation, or isolated migration stage. Capture is re-enabled before the stimulus and is never treated as a substitute for the unsampled audit journal.
4. For the residual UI claim, before any natural input prepare the owned laptop client with isolated prelaunch master audio zero, verified discrete renderer, exact Hyprland window/PID identity and verified owned muted application stream. Confirm node-1 dedicated-server readiness and laptop reachability within 60 seconds of client connection attempt, use a supported automatic direct-connect or authorized desktop control, and prove the intended player joined the exact server world on both sides. After client setup, start a fresh bounded audit capture and verify status so setup time cannot exhaust the capture before its stimulus. Only then perform one permitted normal inspection click and hover/native-chat detail. Do not use console setup as proof of the player permission/action.
5. Correlate JSONL records by correlationId. Compare query cutoff/page/export output against independent SQL counts and watermarks, click outcome against before/after world or inventory digest, and migration checkpoint against source/target count/hash/watermark evidence. Run the named negative and recovery fixture. Stop diagnostics at 60 seconds, output cap or target failure and record the actual reason.
6. Issue sef debug off <capture-id>, verify status and no further matching record. Sanitize required excerpts, preserving no private text, credentials, addresses or unrelated identities. Keep only required evidence under docs/verification/phase-010/ after it exists; stop exact owned client, audio watcher, server and database processes within 30 seconds each, verify exit and stream removal; remove verified disposable runtimes, logs, target databases, downloads, scratch outputs and temporary audio state without symlink traversal; verify absence on every used host. Cleanup failure leaves the gate open.

## Verification Matrix

| Requirement or task | Static or unit | Integration | Real workflow or runtime | Negative and recovery | Execution host and prerequisites | Evidence artifact |
|---|---|---|---|---|---|---|
| P010-TASK-001 | Typed filter, cutoff, cursor, redaction and parity-row completeness tests. | SEF-IF-009/010 contract and renderer-policy fixtures. | Query actual offline-origin central history during concurrent ingestion. | Revocation between every surface, gap/uncertain-tail and filter injection. | node-1 headless after task-graph check, actual candidate and isolated SQL when required. | Cutoff/count comparison, parity matrix, sanitized permission trace. |
| P010-TASK-002 | Checkpoint state-machine and schema portability tests. | Backup, spool fence and principal permission tests. | Actual MySQL-to-MariaDB and reverse isolated migration/recovery drills after P010-TASK-006 diagnostics self-test. | Nonempty target, DDL/copy/fence/cutover interruption, rollback, resume and unavailable synthetic dependency provider. | node-1 isolated fixtures, no production endpoint, temporary migration account revoked. | Checkpoints, source preservation proof, counts/hashes/watermarks/gaps report and principal revocation receipt. |
| P010-TASK-003 | Descriptor/schema/version and queue-versus-central tests. | Coverage registry and API contract compilation. | Registered supported adapter over Phase 009 data. | Forged descriptor, duplicate/stale pack, unsupported adapter. | node-1 headless, no display/client. | API compatibility suite and coverage receipts. |
| P010-TASK-004 | Limit/cursor/export parser tests. | Actual index/timeout behavior on both engines. | MySQL 8.4.11 and MariaDB 11.4.13 isolated query workloads using Connector/J 3.5.10. | SQL unavailable, plan regression, 15-second cap, default 10-row/above-50/continuation-beyond-ten-pages boundaries, export directory/symlink/partial-file and CSV formula cases. | node-1 isolated non-root fixtures only, separate query account and cleanup registration. | Engine plans, result hashes, timeout/cancel receipt, owned output cleanup receipt and sink scan. |
| P010-TASK-005 | Inspector activation/cancellation and exact-purge selection tests. | Command/action token and coverage/pause integration. | Dedicated server natural interaction with independent no-mutation oracle. | Inactive/denied click, token expiry, pause interval, confirmation replay, unknown/unavailable/unresolved dependency provider. | node-1 no-GUI dedicated server for state proof, with laptop evidence separately required by P010-TASK-006. | Interaction trace, before/after digest, pause/purge receipts. |
| P010-TASK-006 | Diagnostic typed-field/limit/self-test tests. | Console enable/status/off and registered-target integration. | Laptop-only native inspection click, hover and rich-chat receipt against node-1 dedicated server. | Missing target, denied debug, output unavailable, timeout, stream recreation and client mute failure. | Split host only for presentation. Laptop requires verified desktop/discrete renderer, owned mute and joined-world proof. | Sanitized JSONL/status receipts, exact client visual/input evidence, per-host cleanup receipt. |
| P010-TASK-007 | Documentation/link and contract-digest checks. | Forge-first PR provenance and conditional proxy integration fixture. | Post-merge Forge rebuild and, when artifact/projection/behavior is applicable, proxy retest against exact approved digest followed by checked proxy PR merge, resulting velocity-latest verification and signed tag. | Applicable proxy change without complete proxy integration, required-check failure, cleanup failure. | Sequential product branches after all phase evidence. | Forge/proxy PR/check/merge/tag/rebuild evidence, digest comparison and documentation rehearsal. |

## Documentation, Operations, and Release

Document the actual inspection command/action behavior; actor/pseudo-actor, origin, world, radius, box, selection, global, UTC/relative-time, action/outcome, registry, literal-prefix, confidence and correlation filters; query default/maximum/continuation limits; canonical export directory, symlink, partial-file and CSV formula protections; scope/sensitive/hidden/export/pause/purge/adapter permissions; native chat/console presentation; coverage/watermark/gap/uncertain meanings; pending versus central history; and no-custom-GUI boundary. Document CE24.1 executable category mapping for inspection, lookup, pages, summaries, queue/status, preview/apply/cancel, rollback/restore/undo, recorded-item give, purge, permissions and typed API, explicitly separating Phase 011 restoration rows and Phase 015 closure.

Document MySQL 8.4.11, MariaDB 11.4.13 and Connector/J 3.5.10 as verification pins only. Include preflight backup, isolated empty target, source fence and spool, checkpoint/resume/rollback, source preservation, separate principal creation/revocation, TLS/trust configuration, allowLocalInfile=false, query limits, phase-available validation counts/hashes/watermarks/coverage/gaps, manual purge preview/confirm dependency-provider refusal, and cleanup. State that actual future restoration-job authority, backups and migration proof belong to Phases 011 and 015. Update diagnostics support steps and test-host/audio requirements. Wiki publication waits for the approved merge. No release, production migration, remote database change, webhook, credential or public compatibility claim is authorized.

## Risks and Evidence Invalidation

| Risk ID and owner task | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
|---|---|---|---|---|---|
| SEF-RISK-011, P010-TASK-001/P010-TASK-004/P010-TASK-005 | One field-policy/redaction predicate before every response sink, continuation reauthorization, literal rendering and owned-export path protection. | query.decision denial, secret/hidden sentinel, export-byte scan, formula/symlink refusal and permission revision mismatch. | Stop exposure, revoke output path, remove incomplete owned output, record scope without secret, repair then rerun. | Policy, renderer, cursor, export directory or permission model change. | Re-run all page/detail/hover/export/revocation and laptop presentation fixtures. |
| SEF-RISK-018, P010-TASK-002/P010-TASK-004 | Portable versioned schema, typed URLs, separate principals, empty target, source fence/spool and count/hash/watermark verification. | migration.checkpoint, engine error, target state, source/target mismatch or principal misuse. | Retain source, resume exact checkpoint or roll back selection, revoke migration principal. | SQL engine/driver/schema/DDL, migration coordinator, trust or artifact change. | Both-direction actual-engine migration, backup and query-plan drills. |
| SEF-RISK-008, P010-TASK-001/P010-TASK-002/P010-TASK-005 | Preserve Phase 008/009 gaps and watermarks; pause/purge are explicit coverage records. | audit.watermark, audit.loss, audit.pause and incomplete-history response. | Refuse completeness/restoration selection, retain gap and repair upstream capture/storage. | Journal/coverage/watermark or loss-policy change. | Re-run capture-to-query cutoffs, gap, pause, pending and migration watermark tests. |
| SEF-RISK-019, P010-TASK-004/P010-TASK-006 | Immutable bounded snapshots, indexed queries, worker cancellation and diagnostic caps. | query elapsedMs, pool depth, export bytes and capture.status limits. | Return bounded unavailable/limit result; do not block tick or silently trim results. | Workload, index, pool, query plan or diagnostic contract change. | Re-run dual-engine limits; Phase 015 retains full representative-load gate. |
| SEF-RISK-012, P010-TASK-002/P010-TASK-005 | Purge exact preview/confirm and dependency-provider refusal; no future restoration data is migrated or executed. | purge.decision provider state and migration validation. | Refuse purge or preserve source/checkpoint; Phase 011 handles job uncertainty. | Restore-job schema/selection dependency contract change. | Repeat purge refusal with synthetic provider states before Phase 011 adoption. |

## Phase Completion Packet

The packet contains the approved candidate/common/protocol and engine/driver hashes; parity registry and API compatibility receipts; all-filter authorization/redaction results; cutoff/default-ten-row/above-fifty/continuation-beyond-page-ten/offline/pending/gap/uncertainty evidence; actual MySQL and MariaDB index, limit and owned-export directory/symlink/partial/CSV-formula evidence; natural no-mutation inspector proof; required silent-laptop click/hover/rich-chat proof; pause/purge dependency-provider decision receipts; both-direction fence/spool/checkpoint/backup/rollback phase-available count/hash/watermark/coverage/gap evidence; diagnostics self-test/on/status/off/re-enable receipts; documentation/link checks; Forge merge/check/tag/post-merge rebuild and, when applicable, exact-digest proxy retest, checked proxy merge, resulting branch verification and signed tag evidence; and verified per-host cleanup receipts.

Every bounded check records its exact owned runtime, database, client, server, watcher, logs, downloads, scratch reports and temporary migration target before launch. It retains only sanitized required proof through its final consumer, then stops owned processes, verifies process/stream exit, removes exact disposable resources without broad deletion or symlink traversal, and verifies cleanup on all used hosts. A cleanup failure is recorded separately and keeps the phase open.

## Next Transition

After the checked Forge pull request merges into forge-1.20.1, the resulting commit is fetched and rebuilt and its signed annotated phase tag is verified. If an applicable proxy artifact, projection or behavior changed, the proxy must consume the exact approved digest, pass retest, merge through a checked proxy PR into velocity-latest, receive resulting branch verification and its signed phase tag before progression. If genuinely unaffected, do not invent proxy work. Then advance to SEF-PHASE-011. Its first action is to consume the frozen query-selection/cutoff/coverage contract and implement fixed restoration jobs with conservation and conflict recovery. Do not execute restoration early, and do not start Phase 011 while Phase 010 integration, tag, client receipt, SQL migration, applicable proxy integration, or cleanup gates remain open.

## Noncanonical Interface Projection

This derived projection is copied verbatim from the assigned interface evidence. It is not a second canonical contract.

```json
{
  "phaseId": "SEF-PHASE-010",
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
    }
  ]
}
```
