# Phase 008 Execution Plan

> **Plan ID:** PLAN-PHASE-008  
> **Phase ID:** SEF-PHASE-008  
> **Owner:** Audit storage  
> **Classification:** MANDATORY  
> **Master plan:** [plan.md](../plan.md)  
> **Phase sequence:** 008 of 016

## Purpose and Ownership

This phase establishes the durable, privacy-preserving audit journal and central ingest boundary before any real mutation adapter is allowed to append events. The master owns audit product scope, all requirements, global limits, shared interfaces, and the later coverage, investigation, restoration, migration, and plan-wide verification gates. This blueprint owns only the Phase 008 implementation, operational proof, and paired integration detail.

## Evidence-Based Entry State

| Evidence class | Area | Finding | Source or command | Freshness condition |
|---|---|---|---|---|
| OBSERVED | existing sinks | The reference 4096-entry queue and file sink may drop events, so neither is a lossless history journal. | FIND-104, SRC-314, SRC-315 | Referenced source revision or sink behavior changes. |
| PROPOSED | durable model | Origin-owned `(originId, journalEpoch, sequence)` records, framed local segments, four separate watermarks, and idempotent central transactions are the audit storage contract. | FIND-024, SEF-IF-009 | Interface schema, framing, or storage policy changes. |
| OBSERVED | external fixtures | MySQL 8.4.11, MariaDB 11.4.13, and MariaDB Connector/J 3.5.10 were retrieved and hashed, but no runtime compatibility is proven. | FIND-111, EXT-006 through EXT-008, SRC-601 through SRC-610 | Artifact hash, advisory, driver, host-library, or Java runtime change. |
| PROPOSED | capacity and privacy | Normal retention defaults to manual scoped purge and permits only explicitly authorized scheduled retention; capacity exhaustion keeps gameplay running, rotates only a verified oldest closed SEF local segment, and reports a known gap or unknown extent. | FIND-026, FIND-110, DEC-009 through DEC-013 | Retention decision, capacity policy, or redaction schema change. |
| PROPOSED | operator delivery | Restricted local health and incident views remain authoritative; optional Discord receives operational summaries only and acceptance uses an owned mock endpoint. | FIND-031, SRC-324 | Alert endpoint policy, permission mapping, or response-limit evidence changes. |

## Scope Boundaries

### Included Scope

- SEF-REQ-024 durable origin journal, isolated central MySQL and MariaDB ingest, recovery, four watermarks, and isolated pools.
- SEF-REQ-026 pre-sink credential and sensitive-field redaction, configurable manual-default normal retention policy, emergency local-only rotation, honest gaps, health, and recovery foundations.
- SEF-REQ-031 restricted local health and incident display plus optional mock-verified Discord operational alert delivery.

### Explicit Exclusions

- SEF-REQ-023 real Forge mutation hooks, capture-family adapters, attribution propagation, and coverage matrix entries belong to Phase 009. This phase exposes `append`, but must not claim that gameplay actions are captured.
- SEF-REQ-025 textual inspection, queries, export, API exposure, and manual and scheduled purge execution belong to Phase 010. This phase validates the policy but starts no scheduler and performs no SQL retention deletion; it does not add a query or purge authority.
- SEF-REQ-028 restoration jobs and SEF-REQ-029 migration/parity work are later owners. Local emergency rotation must never delete authority state, restoration jobs, RTP state, or central SQL rows.
- Phase 015 alone closes representative workload, complete capture, presentation, and plan-wide audit assurance. Current dual-engine fixtures do not certify those future consumers.

## Phase Contract

### SEF-PHASE-008 — Establish journal, dual SQL ingest, privacy and incident delivery

**Objective:** Deliver the audited storage API before capture, durable local journal and recovery with actual isolated MySQL and MariaDB targets, pre-sink privacy policy, safe local loss handling, and locally visible operational incidents.  
**Owner:** Audit storage  
**Dependencies:** SEF-PHASE-007, EXT-006, EXT-007, EXT-008  
**Canonical requirements:** SEF-REQ-024, SEF-REQ-026, SEF-REQ-031  
**Documentation and release impact:** Update implemented audit architecture, configuration, privacy, storage-full, backup/recovery, diagnostics, permissions, and alert runbooks with README and docs-index links. No production database, production webhook, public release, or deployment occurs.  
**Next transition:** SEF-PHASE-009, attach real mutation adapters.

**Entry criteria**

- Phase 007 has a checked paired integration receipt, resulting product-branch verification, signed tags, and verified cleanup. Identity, diagnostics, authenticated transport, and authority isolation are available under their registered contracts.
- Before any implementation fixture, revalidate the exact candidate commits, common/protocol digest, MySQL 8.4.11 and MariaDB 11.4.13 hashes, Connector/J 3.5.10 hash, current advisories, licenses, task graph, and non-production isolated fixture capability.

**Implementation scope**

- Implement SEF-REQ-024, SEF-REQ-026, SEF-REQ-031 through the work packages and acceptance obligations below.

- Define `SEF-IF-009` as the sole audit append, ingest, coverage-registration, and health contract; local append remains usable while SQL is unavailable and never blocks the server tick. Trace: SEF-PHASE-008.
- Persist framed, checksummed, versioned local segments, a reserved loss ledger, separate durability watermarks, replay checkpointing, deduplicated central ingest, and explicit corrupt, torn, missing, and uncertain intervals. Payloads above 1 MiB use bounded ordered chunked blobs only through 8 MiB; larger or over-depth state records explicit omission and nonreversibility rather than a partial exact snapshot claim. Trace: SEF-PHASE-008.
- Enforce typed schema and command redaction at `append`, before any raw serialization, local frame creation, SQL binding, diagnostic construction, health rendering, or alert construction. Unknown unsafe content is redacted with a reason, never passed through because it is unknown. Trace: SEF-PHASE-008.
- Default normal retention to manual purge and leave scheduled execution inactive until Phase 010 implements its explicitly confirmed policy and guards. Never use SQL purge as a capacity response. Emergency capacity rotation considers only the oldest verified closed SEF-owned segment in the canonical spool after gap metadata is durable where possible. Trace: SEF-PHASE-008.

**Execution order**

1. `P008-TASK-001` defines the durable journal, diagnostic signals, configuration, and isolated worker boundary before any capture consumer or storage proof. Trace: SEF-PHASE-008.
2. `P008-TASK-004` implements and proves dual-engine ingest and recovery against actual MySQL and MariaDB fixtures. Trace: SEF-PHASE-008.
3. `P008-TASK-003` proves journal replay, crash-tail quarantine, watermarks, and isolated queue behavior. Trace: SEF-PHASE-008.
4. `P008-TASK-002` implements pre-sink privacy and constrained retention and rotation policy. Trace: SEF-PHASE-008.
5. `P008-TASK-005` implements restricted local health and mock-only Discord incident delivery. Trace: SEF-PHASE-008.
6. `P008-TASK-006` performs complete headless failure and recovery verification after scoped diagnostics exist. Trace: SEF-PHASE-008.
7. `P008-TASK-007` publishes implemented operations material and completes the required paired integration packet. Trace: SEF-PHASE-008.

**Required evidence**

- Actual, non-production MySQL 8.4.11 and MariaDB 11.4.13 processes prove schema creation, duplicate and deadlock handling, transaction commit-before-ack, outage/replay, one ingest pool with at most four connections, separate query pool with at most two connections, consistent backup/restore of the phase-available schema, events, blobs, watermarks, and gaps, and restart recovery. Unit or mock SQL does not substitute.
- Local journal fixtures prove group-fsync uncertainty, frame and chunk checksums, torn/corrupt tail quarantine, idempotent replay, watermarks, transport cleanup versus emergency-loss versus central-unverified uncertainty, capacity loss, crash loss-ledger unavailability, symlink rejection, and continued gameplay-path availability.
- A secret sentinel proves no credential or protected nested field reaches memory capture output, spool, SQL parameters, diagnostics, local health display, mock alert payload, or retained support evidence.

**Exit criteria**

- No known mandatory phase-owned defect remains. Every such defect must be fixed and its affected evidence rerun before phase closure.

- `SEF-IF-009` writes durable local receipts before later adapters may rely on it, distinguishes captured, local-durable, central-durable, and query-visible state, and preserves explicit gaps or uncertain tails across restart.
- Both real SQL engines pass the phase-specific ingest/recovery matrix without sharing authority files or blocking tick work. SQL failure is locally visible and isolated from gameplay and durable local truth.
- Capacity loss, redaction, manual normal retention, local restricted health, and mock alert failure are falsifiably safe. No known mandatory Phase 008 defect remains.

## Inputs and Upstream Contracts

| Input or contract | Provider | Required state | Validation | Failure behavior |
|---|---|---|---|---|
| SEF-IF-001 identity and world | Phases 001 and 005 | `Actor`, `Session`, `WorldRef`, origin, and boot identity retain their established semantics. | Validate source identity and optional world/session fields before serialization. | Append structured unknown or refuse malformed identity, never invent attribution. |
| SEF-IF-002 diagnostics | Phase 001 | Default-off bounded capture supports console authorization, target validation, status, and idempotent disable. | Exercise exact audit control after dedicated-server readiness. | Return typed denial or target/output error; mandatory audit does not become sampled debug. |
| SEF-IF-005 authenticated bridge | Phase 004 | Registered peers and bounded transport exist but are not an audit database or queue. | Confirm health/alert transport has no player carrier or raw secret payload. | Keep local incident truth and bounded local outbox when remote delivery fails. |
| EXT-006 through EXT-008 | external prerequisites | Exact hashes and isolated non-root fixture versions are available. | Rehash, inspect runtime prerequisites and open real authenticated TCP fixtures. | Do not use a mock or production database; leave engine gate open. |

## Outputs and Downstream Contracts

| Output or contract | Consumer | Guaranteed state | Compatibility or versioning | Evidence |
|---|---|---|---|---|
| SEF-IF-009 audit journal and coverage | Phase 009 capture adapters | Typed `append` assigns stable origin epoch sequence and returns local durability or an explicit loss/degraded result. | Registry and schema version 1. Phase 009 must not weaken redaction, watermarks, or gaps. | Actual SQL and local-recovery matrix plus contract tests. |
| Watermark and gap ledger | Phases 009 through 015 and operators | Four watermarks and known or unknown gap semantics survive restart; no completeness inference from enqueue. | Versioned frames and gap records; `unknown extent` remains distinct from an empty gap. | Corrupt-tail, storage-full, and ledger-failure fixtures. |
| Audit health and incident boundary | Phase 010 and operators | Local restricted health remains authoritative; optional alert delivery is sanitized, coalesced, bounded, and non-blocking. | No query/purge authority is conveyed by health. | Permission, mock-outage, and rate-limit fixture evidence. |
| Proposed storage configuration and runbooks | Phase 010 and Phase 016 | Exact existing config paths are discovered before write; future entry is proposed, not verified implementation fact. | Validated generation swap; no arbitrary JDBC URL or driver properties. | Documentation review and config-validation tests. |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
|---|---|---|---|---|---|---|
| `P008-TASK-001` | SEF-REQ-024, SEF-REQ-026 | Create the planned audit storage module boundary, typed `AuditEvent`, `EventKey`, `Watermarks`, `Gap`, framed and per-chunk checksummed segment format, configuration validation, separate append, spool, ingest, query, and alert queues, and source-unit audit diagnostics before any dependent fixture. `append` applies typed sensitive policy and default unknown-unsafe redaction before any raw serialization. Assign origin epoch and sequence before enqueue. Use group fsync at most 100 ms or 1 MiB, whichever occurs first, and label that bounded crash window as uncertain rather than lossless. | SEF-IF-001, SEF-IF-002, Phase 007 identity/transport isolation, EXT-008 | Planned `SEF-IF-009` implementation, validated config generation, local durability receipt, typed reason codes, and audit diagnostic registry. | Planned Forge audit storage service, segment codec, loss ledger, JDBC configuration adapter, SEF-IF-009, SEF-IF-002. | Source-unit framing, sequence monotonicity, 1 MiB boundary and ordered chunk checksums through 8 MiB, over-depth/over-8 MiB explicit omission and nonreversibility, disabled diagnostic overhead, permission denial, absent target, 60-second expiry, 200 events/second, 10,000 events, 8 MiB capture, 1024 diagnostic queued events, and two concurrent capture limits. |
| `P008-TASK-004` | SEF-REQ-024 | Implement central schema migration and idempotent ingest with the MariaDB driver using explicit `jdbc:mariadb:` URLs, `allowLocalInfile=false`, `sslMode=verify-full` for remote targets, least-privilege fixture accounts, a 5-second connection timeout, 15-second read, ingest, and inherited Phase 010 query deadlines, one ingest pool with at most four connections, and a separate query pool with at most two connections per process. Use an initial 250-millisecond retry, a 5-second retry ceiling, and at most eight attempts within 60 seconds. Commit event rows and central watermark in one InnoDB transaction before acknowledgement. | P008-TASK-001, EXT-006, EXT-007, EXT-008 | Actual MySQL and MariaDB schema receipts, central commit receipt, deduplication key, pool-isolation configuration, and consistent isolated backup/restore receipt for phase-available schema, events, blobs, watermarks, and gaps. | Planned SQL dialect adapter, migration runner, ingest worker, separate connection pools, backup runner, SEF-IF-009 `ingest`. | Against each actual isolated engine, inject duplicate batch, duplicate event key, deadlock, remote commit before client ack, restart, SQL outage, and offline-origin replay. Back up to an isolated target, compare independent row counts, ordered event-key sequence and partition hashes, restart and replay with no duplicates, and preserve the original fixture. Future restoration jobs are not claimed backed up yet. |
| `P008-TASK-003` | SEF-REQ-024, SEF-REQ-026 | Implement recovery scanner and replay: verify length/schema/frame checksum plus chunk count, order, and whole-payload checksum, quarantine torn or corrupt tails, replay valid prefixes idempotently, preserve uncertain tail and gap state, and isolate a full or stalled central queue from local append. Maintain 64 MiB capture memory, 64 MiB segments, 4 GiB local spool, 16 MiB reserved loss ledger, and ingest batches limited to 512 events or 1 MiB. | P008-TASK-001, P008-TASK-004 | Recovery receipt, replay checkpoint, quarantine manifest, watermarks, explicit omission/nonreversibility record, and explicit `SQL_UNAVAILABLE`, `JOURNAL_UNAVAILABLE`, and `CAPACITY_LOSS` reasons. | Planned journal manager, recovery scanner, spool queue, loss ledger, chunk codec, SEF-IF-009. | Kill only the owned fixture at each fsync and commit cut, truncate or checksum-corrupt one owned segment or chunk, then restart and assert valid-prefix replay, quarantined tail, no duplicate SQL row, explicit `uncertainTail`, no partial payload represented as exact, and continued append or explicit loss. |
| `P008-TASK-002` | SEF-REQ-026 | Implement declarative credential schemas and typed sensitive-field paths before serialization. Apply redaction at `append` before any raw frame, SQL binding, diagnostic event, health result, support packet, or alert rendering, with default unknown-unsafe treatment. Implement the canonical Phase 010 retention schema with manual as the default, no activated schedule, atomic validation and last-valid-snapshot reload recovery. Scheduled execution remains a Phase 010 responsibility and cannot run here. Reject capacity-triggered SQL deletion. Distinguish central-acknowledged recovery-window-expired local transport cleanup from emergency unacknowledged loss and central-unverified local loss. | P008-TASK-001, P008-TASK-003, DEC-009, DEC-010, DEC-012 | Redaction decision record, protected field policy, local transport-cleanup record without central-history gap, emergency-loss gap or central-unverified uncertainty, and safe refusal reasons. | Planned redaction policy, segment ownership validator, loss ledger, config validation, SEF-IF-009 `AuditEvent.redactions`. | Place secret sentinels in public/private text, command arguments, nested NBT, unknown schema, and alert candidate. Scan every phase sink. Prove default manual policy, absent legacy fields, invalid age/scope/time rejection and failed reload preservation without any scheduled executor. Fill only the owned spool; prove active segment, symlink, non-SEF file, proxy authority database, restoration-job store, RTP state, and central SQL rows are never candidates. |
| `P008-TASK-005` | SEF-REQ-031, SEF-REQ-026 | Implement permission-gated console and actor health and incidents views, local coalescing episode state, 75 percent warning and 90 percent critical thresholds, bounded outbox of 128 summaries, at most eight retries per episode, and server-directed rate-limit delay. Report separate local cleanup count, actual unacknowledged-loss range/count, and central-unverified uncertainty count with distinct incident classes. Validate Discord origin, disable redirects, retain webhook URL only in protected configuration, and deliver to an owned mock endpoint only. | P008-TASK-001, P008-TASK-002, SEF-IF-002, SEF-IF-005 | Restricted `sef audit health` and `sef audit incidents` operational results, sanitized alert envelope, bounded local outbox, local incident persistence, and distinct cleanup/loss/uncertainty counters. | Planned audit health command adapter, incident service, optional webhook sender, configuration validator, SEF-IF-009 `health`. | Denied reader, authorized console, acknowledged transport cleanup, unacknowledged spool loss, central-unverified local loss, spool warning, SQL outage, full spool, webhook DNS/timeout/429/5xx, redirect, and mock recovery fixtures assert local truth persists, incident counts/classes differ, no secret/text is emitted, and gameplay is not stopped. |
| `P008-TASK-006` | SEF-REQ-024, SEF-REQ-026, SEF-REQ-031 | Run the dependency-ordered source-unit, actual SQL, dedicated-server, recovery, backup/restore, and security matrix using the delivered diagnostics. Establish server readiness before any console operation, enable audit capture before stimulus, and retain only sanitized evidence after assertions. | P008-TASK-001 through P008-TASK-005 | Per-engine reports, diagnostic packet, isolated backup/restore and replay evidence, fault matrix, cleanup receipts, and open-gate record for future capture consumers. | Planned test harnesses, no-GUI dedicated server, actual SQL fixtures, audit health and diagnostic interfaces. | Verify each fixture with a bounded 60-second diagnostic capture, 60-second engine readiness, 120-second server readiness, and 30-second fixture shutdown. Mocks may cover isolated parser branches only; they never replace actual MySQL/MariaDB, filesystem, or mock-HTTP operational acceptance. |
| `P008-TASK-007` | SEF-REQ-024, SEF-REQ-026, SEF-REQ-031 | Document only implemented storage behavior and execute phase integration evidence. Record artifacts, hashes, SQL fixture versions, sensitive-data rules, capacity gaps, manual purge boundary, backup/recovery limits, alerts, support capture, and exact cleanup. Merge Forge first, create its signed tag, retest the exact common digest on the proxy artifact, then merge Velocity and create its signed tag. | P008-TASK-001 through P008-TASK-006, paired branch rules | Updated tracked documentation, phase completion packet, Forge merge/tag receipt, exact-digest proxy retest receipt, Velocity merge/tag receipt, and wiki material for post-merge publication. | README, docs README, documentation overview, audit operations/security/troubleshooting topics, phase milestone and integration evidence. | Required checks and private independent review subject to the established review-capability availability rule pass before each merge. Fetch and verify branch containment after every merge. A changed common/protocol digest invalidates the proxy retest. No direct product-base push, production message, or release publication. |

For each task, background workers may proceed only after the bounded record or immutable snapshot is handed off. World mutation, future capture adapters, central query, purge, restoration, authority, and RTP stores are not transferred to this phase. Failed or interrupted tests retain the source and evidence needed by their current bounded consumer, then follow the registered cleanup path.

## Architecture and Implementation Boundaries

The origin owns an immutable `EventKey` and local journal epoch. `append` validates an event and field policy, redacts before any sink, frames it to an owned local segment, and reports local durability honestly. It does not require central SQL success. The journal manager emits `captured`, `localDurable`, `centralDurable`, and `queryVisible` as separate ordered watermarks, with `Gap` records and `uncertainTail` rather than collapsing them into a success counter. Later Phase 009 adapters obtain a local receipt or a typed degraded/loss result and retain their own coverage truth.

The ingest worker owns SQL interaction. It accepts bounded immutable batches, uses one ingest pool with at most four connections and one separate query pool with at most two connections per process, and never performs JDBC work on the Forge tick or proxy owner thread. It deduplicates only by immutable event key, commits rows and the watermark together, and acknowledges after the transaction commits. Proposed acceptance limits are a 5-second connection timeout, 15-second read, ingest, and inherited Phase 010 query deadline, 250-millisecond initial retry, 5-second retry ceiling, and at most eight attempts within 60 seconds. These are conservative limits to verify, not measured performance claims. MySQL 8.4.11 is the primary target and MariaDB 11.4.13 is required secondary support. Both run only as isolated, non-root, disposable test processes. Driver URL and properties are assembled from validated operator settings, never player content. Remote fixtures use authenticated TLS with `sslMode=verify-full`; a trust-all or arbitrary driver-property fallback is prohibited.

The spool is owned by SEF and has a canonical directory discovered from the actual configuration, not an assumed path. Fsync limits bound exposure but do not promise zero loss. Payloads over 1 MiB are ordered, bounded, checksummed chunks through 8 MiB; payloads over 8 MiB or beyond the configured structural depth are explicit omissions with nonreversible status, never partial payloads represented as exact snapshots. Recovery keeps valid prefixes, quarantines malformed tails, and records a visible gap or `unknown extent` when a ledger cannot be persisted. The durable last-known loss state and unclean-start marker detect a crash while the loss ledger was unavailable, yielding unknown extent rather than zero loss after restart. Capacity warnings start at 75 percent and critical at 90 percent. Mandatory audit capture is never sampled, disabled, or replaced by bounded debug capture to meet a limit.

All sensitive content receives a typed policy before local frame creation, SQL binding, diagnostics, health, alert formatting, export preparation, or support evidence. Full authorized message and command content remains a later read-policy concern; redaction is not a later query filter. Separate permissions are planned for ordinary audit metadata, sensitive content, hidden activity, export, and retention administration. Phase 008 only implements the health/incident restrictions and policy boundary needed by its owned surfaces; Phase 010 adds queries and purge commands.

Normal retention defaults to manual scoped purge. Validate the Phase 010 optional schedule schema, but perform no timed SQL deletion in this storage phase. Phase 010 alone implements explicit policy activation and guarded scheduled execution. Capacity pressure never authorizes SQL purge. A fully centrally acknowledged local segment may be removed as normal redundant transport cleanup only after its configured local recovery window elapses. That cleanup records its local rotation count and creates no central-history gap. Emergency rotation is a storage availability mechanism, not retention: at exhausted capacity it validates a closed SEF-owned local segment, ownership, directory boundary, and absence of symlink traversal, persists loss metadata when possible, and selects only the oldest eligible segment without preferring unacknowledged history. Deleting an unacknowledged segment creates an actual loss range. Deleting an acknowledged segment with an independently verified central copy creates no central-history gap. If an acknowledged local segment is lost but its central copy is independently unverified, record uncertainty until checked, not an actual loss or confirmed transport cleanup. Warning and critical thresholds alone do not authorize emergency deletion. No case removes active segments, central SQL history, proxy authority SQLite, transfer records, restoration jobs, RTP grids/reservations, or unrelated files. If the loss ledger fails, in-memory degraded counters, durable last-known state, and an unknown restart interval remain visible. Gameplay can continue while administrative durability is degraded, but that condition is never represented as complete history.

`sef audit health` and `sef audit incidents` are the authoritative restricted local operational surfaces. Optional Discord delivery sends only an incident class, severity, timestamp, bounded watermarks, and safe recovery state. It never sends chat, command arguments, NBT, identities beyond the permitted operational key, credentials, a webhook URL, private addresses, or mass mentions. The endpoint is operator-configured, origin-validated, redirect-disabled, rate-limit-aware, and tested exclusively with an owned mock endpoint. Webhook failures are non-blocking, bounded, coalesced, and locally visible.

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
|---|---|---|---|---|
| SQL committed before acknowledgement | Commit receipt exists but caller retries after owned connection cut. | Deduplicate by event key and advance central watermark once. | Replay same immutable batch and preserve one row. | Actual MySQL and MariaDB cut-after-commit fixture with row and watermark oracle. |
| SQL outage or deadlock | Bounded timeout, SQL state, retry classification, and pending age. | Local journal remains truthful and tick work remains non-blocking; health distinguishes central outage. | Retry bounded ingest after engine readiness; retain local spool until acknowledged. | Stop only owned SQL fixture, inject deadlock, restore it, and replay. |
| Torn, corrupt, or chunk-incomplete local tail | Length, schema, frame or whole-payload checksum, chunk order, or count fails during recovery scan. | Replay verified prefix only, quarantine the exact tail, and mark payload omission/nonreversibility rather than claiming a partial exact snapshot. | Retain manifest and never silently repair, renumber, or synthesize omitted state. | Truncate, reorder, or checksum-corrupt an owned closed segment/chunk before restart. |
| Fully acknowledged local transport cleanup | Central commit receipt is durable and the configured recovery window has elapsed. | Remove only the closed local redundant segment, increment local cleanup count, and create no central-history gap. | Independently verify central event-key range before recording cleanup complete. | Actual both-engine acknowledged-range and recovery-window fixture. |
| Full spool whose oldest eligible segment is unacknowledged | Capacity is exhausted, the oldest eligible closed owned segment is unacknowledged, and the loss-ledger path is available. | Persist actual loss range then rotate only that oldest eligible segment; gameplay continues with `CAPACITY_LOSS`. Warning and critical thresholds alone never trigger deletion. | Ingest/recover health without erasing central rows; report watermarks and actual gap. | Fill isolated spool with deterministic records and assert exact selected path and loss range. Also prove that an older acknowledged eligible segment is selected before newer unacknowledged history and independently classify its central-copy state. |
| Acknowledged local segment lost while central copy is unverified | Local segment absence conflicts with unverified central receipt/range. | Report uncertainty, not transport cleanup or actual loss. | Check central range before resolving the uncertainty. | Delete only owned fixture segment after acknowledgement and force independent central verification outage. |
| Full spool with loss-ledger unavailable or crash during ledger unavailability | Ledger write failure, unclean marker, and durable last-known loss state disagree at recovery. | Maintain bounded in-memory degraded counters and report unknown extent, never zero loss. | On restart expose unknown interval until durable reconciliation. | Deny ledger path and crash the owned fixture during capacity handling, then restart. |
| Secret, unknown sensitive field, or nested NBT | Redaction rule or unknown-unsafe classifier emits reason. | Replace before every sink; marked nonreversible where exact data cannot be held safely. | Preserve redaction reason without reproducing secret in evidence. | Sentinel scan across frames, SQL bindings, diagnostics, health, mock payload, and retained packet. |
| Unowned, active, symlinked, authority, job, or RTP candidate | Ownership, closed-state, canonical-path, and type checks fail. | Refuse rotation and emit capacity incident, never delete a different store. | Operator resolves storage; later manual purge remains a Phase 010 action. | Symlink, active segment, foreign file, authority database, job store, and RTP ledger fixtures. |
| Webhook timeout, redirect, rate limit, or failure | Bounded sender response classification and outbox episode key. | No gameplay effect; retain authoritative local incident and obey delay/retry budget. | Coalesce, retry at most eight times, then mark delivery unavailable locally. | Owned mock endpoint returns timeout, 302, 429, 5xx, then recovery response. |

## Diagnostics and Debugging

**Requirement IDs:** SEF-REQ-024, SEF-REQ-026, SEF-REQ-031  
**Task IDs:** P008-TASK-001, P008-TASK-004, P008-TASK-003, P008-TASK-002, P008-TASK-005, P008-TASK-006  
**Controls:** Console or authorized `sef.debug.manage` actor runs `sef debug on audit registered-target 60`, then `sef debug status`, then `sef debug off capture-id`; the registered target is mandatory, status shows side, categories, remaining seconds, limits, and discovered owned output, and disable is idempotent.  
**Signals:** `audit.append`, `audit.watermark`, `audit.ingest`, `audit.recovery`, `audit.loss`, `audit.redaction`, `audit.health`, and `audit.alert` contain the SEF-IF-002 capture, correlation, side, boot, sequence, tick, desired, actual, reason, units, configuration generation, and candidate digest fields.  
**Collection procedure:** The numbered runbook below registers resources, reaches readiness before console input, enables one bounded audit capture before the stimulus, inspects correlated fields, disables, sanitizes, retains, and cleans.  
**Headless verification:** node-1 runs only inspected no-GUI dedicated-server tasks and isolated non-root MySQL or MariaDB fixture processes. It proves journal, queue, transaction, privacy, health, and recovery behavior, not future rendered query or gameplay capture claims.  
**Client verification:** This storage phase launches no Minecraft client because all owned acceptance is server-observable. Phase 009 and later phases retain any residual real mutation, input, presentation, or query client gates.  
**Client audio isolation:** Leave audio settings unchanged. Every required client uses the master Section 14 Trident instance lifecycle on the verified laptop, with explicit owned instance, runtime, window/PID, discrete renderer and joined-world evidence. No Trident command or client runs on node-1. Do not create mute watchers or require a playback stream or mute proof. Server-only checks need no client. Verify exact owned process exit and instance cleanup after the final consumer.
**Budgets and privacy:** Diagnostics are default-off, registered-target scoped, 60 seconds here, and subject to the master 200 events/second, 10,000 events, 8 MiB output, 1024 queued diagnostic events, and two simultaneous captures. Audit remains unsampled with 64 MiB capture memory, 64 MiB segments, 4 GiB spool, 16 MiB loss ledger, 512 events or 1 MiB batches, one ingest pool of at most four connections, separate query pool of at most two connections, 5-second connect, 15-second read, ingest, and inherited Phase 010 query deadlines, 250-millisecond initial retry, 5-second ceiling, eight attempts within 60 seconds, 60-second engine readiness, 120-second server readiness, 30-second owned-fixture shutdown, 75 percent warning, and 90 percent critical thresholds. These are proposed acceptance limits, not measured capacity claims. Secrets and unknown-unsafe fields are redacted at append before every raw serialization and sink.  
**Regression and support:** Test enable, status, disable, unauthorized actor, absent target, timeout, reload/restart reset, limits, redaction, disabled overhead, and unchanged server behavior. Update `docs/troubleshooting/diagnostics.md` and audit operations documentation with a minimal sanitized packet and rerun affected evidence after candidate, driver, schema, configuration, or interface changes.

| Signal | Source and unit | Expected observation |
|---|---|---|
| `audit.append` | Forge audit storage service, one event or refusal | Stable event key precedes enqueue; local receipt or typed reason, never enqueue-as-durable. |
| `audit.watermark` | Journal and ingest workers, `u64` stages and pending milliseconds | `captured >= localDurable >= centralDurable >= queryVisible` only where defined, with gaps and uncertainty explicit. |
| `audit.ingest` | SQL worker, batch count, bytes, retry count, transaction duration milliseconds | Commit occurs before acknowledgement; duplicate key does not create a second row. |
| `audit.recovery` | Recovery scanner, segment sequence, valid bytes, chunk checksum/order, quarantine reason | Valid prefix replays idempotently; torn or chunk-incomplete tail is quarantined and not hidden. |
| `audit.loss` | Spool manager, bytes, free bytes, first/last sequence, loss class, reason | Transport cleanup, actual unacknowledged loss, and central-unverified uncertainty remain distinct; ledger crash reports unknown extent. |
| `audit.redaction` | Pre-sink policy, field path, sensitivity, redaction reason | No sentinel survives any sink; protected payload may be marked nonreversible. |
| `audit.health` | Restricted command service, local and central state, cleanup/loss/uncertainty and outbox counts | Local spool and central SQL failures, redundant transport cleanup, actual loss, and central-unverified uncertainty remain distinct and visible. |
| `audit.alert` | Mock sender, episode key, retry count, response class | Coalesced safe summary obeys rate delay and never leaks webhook or content. |

1. Discover the candidate, actual Forge no-GUI task, current configuration path, and node-1 project anchor. Register exact temporary server runtime, SQL fixture directories, ports, logs, spool, loss ledger, diagnostic output, mock endpoint, process IDs, and post-test evidence destinations under verified host anchors. Confirm no target is a personal instance, production database, authority database, job store, RTP store, or shared cache. For a dedicated-server fixture, write and read back `eula=true` before launch.
2. Inspect the task graph to confirm it starts no client, renderer, or display. Start the owned no-GUI server and isolated database fixture, require engine readiness within 60 seconds and server readiness within 120 seconds, then run console `sef debug on audit registered-target 60` and `sef debug status`. If the target is absent, output is unavailable, or authorization fails, record the typed refusal and do not substitute broad logging.
3. Keep capture active before the real stimulus: append controlled typed events through the delivered audit entry point, force ingest, inject the named SQL, fsync, corruption, capacity, redaction, and alert faults, and use the owned console health command only after startup readiness. Do not call private helpers in place of the append, recovery, redaction, or alert boundary being measured.
4. Correlate the capture ID with event key, journal epoch, watermarks, SQL receipt, gap, redaction, and episode key. Assert independently against segment bytes, database rows, mock request recording, filesystem ownership, and server responsiveness. Record the known limitation that Phase 008 does not prove a gameplay mutation adapter or a later query consumer.
5. Run `sef debug off capture-id`, run `sef debug status`, and append one harmless matching fixture event to prove diagnostic capture stopped while mandatory audit behavior remains independent. Redact the retained packet, preserving only candidate/driver/configuration hashes, safe reason codes, watermarks, bounded counts, and decisive excerpts.
6. Stop owned server, database fixtures, mock endpoint, and any watcher within a 30-second shutdown deadline. Verify process exit, close fixture connections, retain only required sanitized evidence under the eventual `docs/verification/phase-008/` destination, and remove the exact disposable runtimes, logs, spool, databases, downloaded extraction, native extraction, bytecode, trace, and mock output after their final consumer. Confirm absence on every used host. Cleanup failure remains an open phase gate.

## Verification Matrix

| Requirement or task | Static or unit | Integration | Real workflow or runtime | Negative and recovery | Execution host and prerequisites | Evidence artifact |
|---|---|---|---|---|---|---|
| P008-TASK-001, SEF-REQ-024 | Frame, sequence, config, and queue-boundary unit tests | Contract tests against SEF-IF-001, SEF-IF-002, and SEF-IF-009 | No-GUI server reaches ready state before console audit capture | Bad schema, stale config, absent target, limit and disabled-mode checks | node-1 after graph inspection, owned nested runtime, no client | Source-unit report and sanitized diagnostic self-test. |
| P008-TASK-004, SEF-REQ-024 | Dialect, transaction, timeout, pool-ceiling, and backup manifest property tests | Actual MariaDB driver connection, migration, backup and restore per engine | Actual isolated MySQL 8.4.11 and MariaDB 11.4.13 ingest, restore, restart, and replay | Duplicate, deadlock, commit-before-ack, 5-second connect, 15-second read/ingest/query deadline, retry ceiling, outage, replay | node-1 non-root fixture only, EXT-006 through EXT-008 hashes and refreshed advisories | Per-engine SQL receipt, independent counts/hashes/ordered keys, backup/restore/replay record, and cleanup receipt. |
| P008-TASK-003, SEF-REQ-024, SEF-REQ-026 | Frame and chunk checksum, payload boundary, omission, and watermark invariants | Spool to real SQL replay | Dedicated-server append and restart recovery | Fsync cut, truncation, chunk reorder/corruption, over-8 MiB/over-depth omission, full queue, SQL loss | node-1, actual filesystem and owned server; no client | Quarantine manifest, replay result, omission record, watermark and gap packet. |
| P008-TASK-002, SEF-REQ-026 | Redaction-at-append and ownership predicate units | End-to-end all-sink sentinel scan | Real owned spool capacity, transport cleanup, emergency loss, and central-unverified recovery paths | Unknown field, nested NBT, symlink, active/foreign file, ledger crash, unacknowledged loss, central-unverified local loss | node-1 with isolated exact paths and no production data | Sanitized scan report, cleanup/loss/uncertainty records, and protected-path refusal evidence. |
| P008-TASK-005, SEF-REQ-031 | Permission, coalescing, retry, redirect, schema, and incident-count tests | Local command and owned mock sender integration | Console health after ready server, all three local cleanup/loss/uncertainty fixtures, mock 429 and recovery | Unauthorized access, SQL versus spool incident, timeout, redirect, eight retry ceiling | node-1 no-GUI server and owned mock endpoint only | Local incident view with distinct counts/classes, sanitized request assertion, outbox and cleanup evidence. |
| P008-TASK-006, all phase requirements | Aggregated regression and support-procedure tests | Both actual engines plus filesystem and mock endpoint | Headless dedicated-server, capture active during all stimuli | Fault matrix and restart recovery rerun | node-1 only, candidate identity and cleanup registration required | Phase verification index with open future gates. |

## Documentation, Operations, and Release

Update the root README, `docs/README.md`, and `DOCUMENTATION.md` only with behavior merged by this phase. Add or update audit architecture, storage configuration, privacy, manual retention, redundant local transport cleanup, emergency-loss and central-unverified uncertainty, backup/recovery, SQL support matrix, permissions, incident, and diagnostics topics at repository-conventional paths discovered during implementation. Explain that MySQL 8.4.11 and MariaDB 11.4.13 are verified fixture targets, that this phase backs up/restores only phase-available schema, events, blobs, watermarks, and gaps to isolated targets, that future restoration jobs are not yet backed up, that full source/runtime compatibility evidence binds each result, that normal purge defaults to manual, optional scheduling requires explicit activation, and both execution paths belong to Phase 010, and that alert tests never use a production webhook. Prepare wiki changes only after approved merge. No public release or production rollout is included.

## Risks and Evidence Invalidation

| Risk ID and owner task | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
|---|---|---|---|---|---|
| SEF-RISK-008, P008-TASK-001 through P008-TASK-002 | Dedicated spool, isolated queues, checksum frames, reserved ledger, bounded capacity and explicit rotation boundary | Audit watermarks, loss reason, recovery quarantine, and health state | Replay valid prefix, expose known or unknown interval, restore capacity without unrelated deletion | Segment framing, disk policy, engine, driver, config, or candidate digest change | Rerun actual engine outage, full spool, corrupt tail, ledger-failure, and restart matrix. |
| SEF-RISK-011, P008-TASK-002 and P008-TASK-005 | Typed pre-sink sensitive paths, default unknown-unsafe redaction, separate rights, safe alert renderer | Sentinel scan and redaction reason across all phase sinks | Revoke sink or config, retain exposure scope without secret repetition | Field policy, command schema, serializer, display, or alert changes | Rerun nested content, unknown-field, health, and mock-payload scans. |
| SEF-RISK-018, P008-TASK-004 and P008-TASK-006 | Exact pins, portable transaction contract, separate dialect execution, backup/recovery inputs | Driver/engine version, schema receipt, transaction reason, and fixture health | Keep source spool, restore isolated fixture, replay idempotently | Engine patch, driver, TLS, schema, migration, or host-library change | Repeat both real engine initialization, transaction, outage, and recovery suites. |
| SEF-RISK-019, P008-TASK-001 and P008-TASK-006 | Off-thread bounded queues and no tick-thread JDBC/fsync; audit not sampled | Queue depth, work milliseconds, pending age, disabled and enabled control counters | Apply explicit admission/degraded state and retain gaps, never silently drop | Queue policy, workload, collector, or candidate change | Phase 015 later performs representative load proof; rerun narrow Phase 008 nonblocking tests now. |
| SEF-RISK-020, P008-TASK-007 | Exact paired artifact/common digest and ordered merge receipts | Commit, tag, candidate, engine, host, and cleanup identities | Retest the changed exact pair before downstream work | Either product artifact, common digest, SQL fixture identity, or host boundary changes | Repeat affected server/engine tests and Forge-first then proxy integration sequence. |

## Noncanonical Interface Projection

This derived projection copies the registered producer and consumer interfaces. It is not a competing canonical contract.

```json
{
  "phaseId": "SEF-PHASE-008",
  "interfaces": [
    {
      "id": "SEF-IF-001",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Identity and world",
        "records": {
          "Actor": {"uuid":"UUID?","source":"PLAYER|CONSOLE|SYSTEM|MACHINE|FAKE_PLAYER","originId":"string","permissionRevision":"u64"},
          "Session": {"playerId":"UUID","proxyBoot":"UUID?","connectionEpoch":"u64","backendId":"string","backendBoot":"UUID"},
          "WorldRef": {"backendId":"string","worldGeneration":"UUID","dimension":"resource_location","registryDigest":"sha256"},
          "Location": {"world":"WorldRef","x":"finite f64","y":"finite f64","z":"finite f64","yaw":"finite f32","pitch":"finite f32"}
        },
        "errors": ["WORLD_REPLACED","DIMENSION_MISSING","REGISTRY_MISMATCH","STALE_SESSION"],
        "ownership": "Backend creates durable world generation; authenticated proxy creates network connection epochs; display names confer no authority."
      },
      "acceptance_ids": ["SEF-AC-006","SEF-AC-019"]
    },
    {
      "id": "SEF-IF-002",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Configuration and diagnostics",
        "records": {
          "ConfigSnapshot": {"generation":"u64","schema":"u16","sanitizedDigest":"sha256","source":"string"},
          "DiagnosticEvent": {"captureId":"UUID","correlationId":"UUID","event":"string","side":"BACKEND|PROXY|HARNESS","boot":"UUID","sequence":"u64","tick":"u64?","monotonicNanos":"u64","utc":"Instant","desired":"typed_map","actual":"typed_map","reason":"enum_string","units":"typed_map","configGeneration":"u64","candidateDigest":"sha256"}
        },
        "methods": ["validateAndSwap(expectedGeneration:u64, proposed:ConfigSnapshot) -> Result<ConfigSnapshot>","enable(actor:Actor, scope:string, target:string?, durationSeconds:u16) -> Result<UUID>","status(actor:Actor, captureId:UUID?) -> Result<CaptureStatus>","disable(actor:Actor, captureId:UUID) -> Result<CaptureStatus>"],
        "errors": ["INVALID_CONFIG","STALE_REVISION","DENIED","TARGET_ABSENT","CAPTURE_LIMIT","OUTPUT_UNAVAILABLE"],
        "ownership": "Atomic owner-executor config swap; default-off bounded diagnostic worker, independent from unsampled audit."
      },
      "acceptance_ids": ["SEF-AC-019"]
    },
    {
      "id": "SEF-IF-005",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Authenticated bridge",
        "records": {"Envelope":{"protocolMajor":"u16","protocolMinor":"u16","senderId":"string","recipientId":"string","senderBoot":"UUID","channelEpoch":"UUID","sequence":"u64","requestId":"UUID","type":"allowlisted_enum","actor":"Actor?","session":"Session?","expectedRevision":"u64?","expires":"Instant","body":"bounded_typed_bytes"}},
        "methods": ["send(envelope:Envelope) -> Result<TypedReply>","rotatePeerKey(peerId:string, expectedKeyId:string, nextKeyId:string) -> Result<RotationReceipt>"],
        "errors": ["AUTH_FAILED","REPLAY","STALE_EPOCH","EXPIRED","OVERSIZED","RATE_LIMIT","PROTOCOL_MISMATCH","UNREGISTERED_SERVER","QUEUE_FULL"],
        "ownership": "TLS 1.3 mutually authenticated private direct socket; peer certificate maps to registered server; no player carrier or arbitrary command relay."
      },
      "acceptance_ids": ["SEF-AC-011"]
    },
    {
      "id": "SEF-IF-009",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Audit journal and coverage",
        "records": {
          "EventKey": {"originId":"string","journalEpoch":"UUID","sequence":"u64","eventId":"UUID"},
          "AuditEvent": {"key":"EventKey","world":"WorldRef?","session":"Session?","kind":"enum_string","outcome":"ATTEMPTED|DENIED|CANCELLED|APPLIED|PARTIAL|FAILED|UNKNOWN","actor":"Actor","initiator":"Actor?","confidence":"DIRECT|PROPAGATED|INFERRED|UNKNOWN","causeId":"UUID?","parentEvent":"EventKey?","utc":"Instant","tick":"u64?","before":"Snapshot?","after":"Snapshot?","sensitivity":"set<PUBLIC_METADATA|PRIVATE_CONTENT|HIDDEN_ACTIVITY|RESTORATION_PAYLOAD>","fieldPolicy":"map<field_path,set<permission_class>>","reversibility":"EXACT|CONDITIONAL|NONE","adapterId":"string","adapterVersion":"u32","redactions":"Redaction[]"},
          "Watermarks": {"captured":"u64","localDurable":"u64","centralDurable":"u64","queryVisible":"u64","gaps":"Gap[]","uncertainTail":"bool"},
          "Gap": {"first":"u64?","last":"u64?","reason":"enum_string","categories":"string[]","acknowledged":"bool?","utcStart":"Instant?","utcEnd":"Instant?"}
        },
        "methods": ["append(event:AuditEvent) -> Result<LocalDurabilityReceipt>","ingest(batch:AuditEvent[]) -> Result<CentralCommitReceipt>","registerCoverage(adapter:CoverageDescriptor) -> Result<CoverageReceipt>","health(actor:Actor) -> Result<AuditHealth>"],
        "errors": ["JOURNAL_UNAVAILABLE","CAPACITY_LOSS","OVERSIZED_PAYLOAD","REDACTED_NONREVERSIBLE","SQL_UNAVAILABLE","COVERAGE_GAP"],
        "ownership": "Origin owns sequence and local journal; central transaction deduplicates event key and watermark; no debug sampling applies."
      },
      "acceptance_ids": ["SEF-AC-024","SEF-AC-026","SEF-AC-031"]
    }
  ]
}
```

## Phase Completion Packet

Require source commits and exact paired candidate hashes; common and protocol digest; configuration and schema revisions; MySQL 8.4.11 and MariaDB 11.4.13 fixture identity, JDBC hash, license/advisory recheck, and actual transaction/recovery reports; journal corruption and capacity fixtures; secret-sentinel scan; mock alert evidence; restricted health evidence; documentation changes; milestone and issue state; deterministic checks; private review subject to the established review-capability availability rule; Forge merged receipt and signed tag; exact-common-digest proxy retest; Velocity merged receipt and signed tag. Every suite records exact owned disposable paths, ports, processes, and evidence destination, then confirms shutdown and cleanup. Cleanup incomplete keeps the phase open.

## Next Transition

Only after both phase branches are merged by checked merge commits, resulting product branches are verified, required signed tags exist, and cleanup is complete may execution advance to SEF-PHASE-009. Its entry action is to attach actual mutation adapters to the verified `SEF-IF-009` journal and register explicit coverage. That future attachment is proposed here, not verified or completed by Phase 008.
