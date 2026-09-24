# Server Essentials Forge 2 Final Audit and Remediation Plan

> **Plan ID:** PLAN-MASTER
> **Plan status:** VALIDATED
> **Project state:** EXISTING
> **Planning subject:** Server Essentials Forge 2 final security, administrator-command, UI, persistence, backend-handling, and integration audit with mandatory remediation closure
> **Requested artifact:** authoritative_plan
> **Plan profile:** software_product
> **Evidence date:** 2026-09-24
> **Diagnostics contract:** 2

This master freezes the product contract, exclusive requirement ownership, global phase sequence, shared interfaces, and completion endpoint. The linked phase files contain the sole full phase declarations and detailed execution blueprints. Plan validation is not product acceptance. Existing integrated work and its scoped evidence remain part of the audit.

## 1. Project Identity

```text
Project: Server Essentials Forge 2, SEFPORTED
Requested artifact: authoritative_plan
Repository root: /mnt/hermes/projects/SEFPORTED
Starting branch: envy/phase-003-commands
Starting commit: e160a235b19c992b3a23c3a43754e92ad0147948
Authoritative remote:
origin
https://github.com/MCEnvision/Server-Essentials-Forge.git
Remote ref: origin/envy/phase-003-commands
Remote commit: e160a235b19c992b3a23c3a43754e92ad0147948
Approved candidate integration branch: master
Observed GitHub default branch: forge-1.20.1, legacy
Mod id: sef
Product version: 2.0.0
Primary package: com.enviouse.sef
Minecraft: 1.21.1
NeoForge: 21.1.235
Java: 21
Gradle Wrapper: 8.8
Parchment: 2024.11.17
```

Repository, remote, package, and candidate lineage match the request. `DEC-006` selects the SEF 2 lineage beginning at `envy/sef2_complete`; the observed approved integrations use `master`. Verify that exact remote target before each integration. The legacy default is neither an integration target nor permission to change platform versions. Do not invent a `main` branch or silently merge legacy work.

## 2. Planning Subject and Source Roles

| ID | Role | Subject | Source | Intended use |
|---|---|---|---|---|
| SRC-001 | owner_request | Full SEF 2 audit and deliberate plan rebuild | EnVy direct current plan rebuild request on 2026-09-24 | Authoritative scope, portability correction, and rebuild authorization. |
| SRC-002 | requirements | SEF 2 intended behavior and compatibility | /mnt/hermes/projects/SEFPORTED/sef2.md | Product behavior and compatibility contract. |
| SRC-003 | audit_evidence | Historical full codebase audit findings | /mnt/hermes/projects/SEFPORTED/audit.md | Prior findings and evidence gaps requiring renewed proof. |
| SRC-004 | requirements | Manual, multiplayer, integration, UI, persistence, and release verification matrix | /mnt/hermes/projects/SEFPORTED/test.md | Highest-fidelity workflow and acceptance coverage. |
| SRC-005 | repository_evidence | Current implementation and technical documentation | /mnt/hermes/projects/SEFPORTED at the inspected candidate revision | Observed architecture, state, commands, UI, integrations, and build constraints. |
| SRC-006 | repository_evidence | Build, manifests, tests, resources, and CodeGraph inventory | /mnt/hermes/projects/SEFPORTED build metadata and .codegraph | Package identity, dependency direction, test inventory, and reproducibility constraints. |
| SRC-007 | audit_evidence | Remote repository and dependency security state | Read-only GitHub preflight and retained evidence packets | Remote identity and dependency risk context only. |
| SRC-008 | reference | Historical porting and inventory audit | /mnt/hermes/projects/SEFPORTED/SEFAudit.md | Legacy risk cross-check only. |
| SRC-009 | owner_request | Minecraft Java portability and native dependency boundary | EnVy direct current request and prior locked platform correction | One portable client contract, current source and artifact native analysis plus available runtime proof, and available in-plan closure without foreign-host prerequisites. |
| SRC-010 | repository_evidence | Audit writer and compile-only native API implementation | NativeAuditFileProvider, build.gradle, and retained Phase 001 evidence | Opened-object identity, JNA packaging, and provider evidence requirements. |
| SRC-011 | status | Current phase and goal continuity | /mnt/hermes/projects/SEFPORTED/docs/plan/goal.md and supplied checkpoint brief | Active phase and next task context; never authority over the rebuilt product contract. |
| SRC-012 | audit_evidence | Retained phase integration and runtime packets | Retained phase002 completion packet and phase003 tasks310 through318 | Historical executed results, failures and candidate identity, not current execution. |

The subject is the existing product's final audit and remediation. Historical audits supply findings; test specifications supply acceptance requirements; status and saved execution state supply continuity. None is proof of current behavior. Source relationships and fingerprints are in [the research map](research/repository-map.md), [brief](research/brief.md), and [observations](research/sources/current-evidence.md). Current owner decisions govern contradictions, followed by this master, the deterministic handoff, registered execution blueprints, and scoped evidence.

## 3. Purpose and Intended Outcome

Administrators need privileged actions that make exactly the intended change, deny unauthorized requests, preserve data, and explain success or failure accurately. Players need private information protected and optional enhanced screens that remain responsive and accessible. Operators need predictable recovery, optional integrations that fail safely, and a support procedure that reveals causes without disclosing private data.

The smallest complete outcome includes the entire existing executable surface: discover and configure a capability, invoke it through its allowed source, observe its real effect and truthful feedback, survive reload or restart, recover from interruption, and diagnose failure. Completing only parsers, a few read commands, or shared policy callbacks cannot establish this experience.

Required engineering additions are bounded diagnostics, missing test fixtures, maintained quality gates, and fixes for confirmed defects. They enable the declared audit. New gameplay, unavailable runtime families, unrelated integrations, and broad modernization remain excluded under `FUT-001` through `FUT-003`.

**Completion endpoint:** At one frozen candidate revision and artifact, close every mandatory audit and remediation matrix, repair confirmed in-scope defects, pass required final verification and integration, reconcile documentation, and record release readiness without publication or production mutation.

## 4. Evidence-Based Current State

The following observations were collected at 2026-09-24T20:06:46.273Z. No product runtime was launched during this research. Retained reports describe historical execution at their named inputs; they are not fresh final verification.

| Area | Evidence class | Finding and consequence | Evidence |
|---|---|---|---|
| Integrated history | OBSERVED | Phase 000 PR 10, Phase 001 PR 8, and Phase 002 PR 11 are merged into `master`; annotated tag signatures were verified. Preserve that history. | FIND-002, SRC-007, SRC-012 |
| Active work | OBSERVED | Phase 003 branch is current; no Phase 003 PR or branch workflow run was returned. Remaining entry is `P003-TASK-010`. | FIND-002, FIND-010 |
| Candidate reuse | OBSERVED | Main product and build inputs match retained candidate `1b2e61947cc733574bee377a8404f59f7b129d62`; only planning and the command matrix generator/tests changed. Reuse requires matching all other evidence inputs. | FIND-002, SRC-005 |
| Inventory disagreement | OBSERVED | Task 310 has 729 rows: 713 executable actions/routes and 16 unavailable families; 324 executable rows partial, 389 open, 16 unavailable passed. Task 317 JSON has 713 open, 16 partial, zero passed. Reconcile joins before selecting missing action work. | FIND-003, task 310 and task 317 locators in SRC-012 |
| Retained execution | OBSERVED | Task 310 reports 75 required GameTests, 504 console rows, 116 argument routes, 35 player routes, and 817 effect records. These are dimensions, not 713 completed action contracts. | SRC-012 |
| Authorization fixture | OBSERVED | Matching laptop client rendered and joined, but protected `/sef doctor` was rejected by the client command tree before transmission. Operator status did not grant the default denied permission. Diagnose authentic authority and projection. | FIND-004, task 316 |
| Provider fixture | OBSERVED | LuckPerms NeoForge 5.4.140 initialized on exact NeoForge 21.1.235, then login failed with `Capability has not been initialised` and `Invalid player data`. Forge 5.4.102 was rejected. This is not yet a proven SEF implementation defect. | FIND-004, task 318 |
| Persistence | OBSERVED | Phase 002 packet records 30 production implementations, 27 runtime registrations, 52 writers, 8 noninterface owners, 24 sensitive writers, 19,312 reconciled rows, and 1,922 foreign key checks. Reopen changed owners and command/store joins, not every unchanged primitive. | FIND-005 |
| Native supply | OBSERVED | Task 311 Linux manifest records opened-object writer checks. Task 312 records 285 artifacts and no duplicate runtime. JNA/JNA Platform 5.14.0 are compile only. Analysis and native execution remain distinct evidence classes. | FIND-006 |
| Security advisories | OBSERVED | 28 live alerts: 1 critical, 12 high, 14 medium, 1 low. New Netty SNI advisories 28 and 27 supersede the old 26-alert inventory. Actual pipeline reachability is unresolved. | FIND-007 |
| UI and lifecycle | UNKNOWN | Complete accessibility, privacy, InvSee, admission capacity/FIFO, disguise animation, mixed/fallback clients, provider outage, reconnect, and lifecycle sequence evidence remains open. | FIND-008 |
| Diagnostics and quality | OBSERVED | Doctors and repository health exist. Proposed debug capture controls and maintained formatter, warning, static analysis, and risk coverage gates are absent from inspected source/build. Deliver them before dependent verification. | FIND-009 |
| Execution state | OBSERVED | Saved goal SHA-256 is `4079eb7b872b2c76d8591a1a2d1642e6ad88179c8fda0add802b56c996eb7499`; `docs/plan/active_phase.md` is absent. Planning changes neither. Pre-existing dirty files and browser state remain protected. | FIND-010, SRC-011 |

## 4a. Execution Continuity and Remaining Work

| Phase | Retained integration | Remaining execution treatment |
|---|---|---|
| 000 | PR 10, merge `a71840c96ea507b438213be4967ae5af4398c0c1`, tag `phase-000-audit` | Preserve inventory framework, original doctors, and evidence. Current Phase 003 reconciles changed matrix inputs. |
| 001 | PR 8, merge `844990d18f4a64e151f0ca5554e8f0ab5aab6035`, tag `phase-001-audit` | Preserve security/native implementation and scoped proof. Current advisory and native-input changes reopen exact rows; Phase 006 closes final regressions. |
| 002 | PR 11, merge `ff17546fdd766d54ed5a01928a4ede2fb42c90de`, tag `phase-002-persistence` | Preserve owner audit, shared fault/recovery fixtures, and proof. Recheck domain joins and changed invariants under 003, 005, and final 006. |
| 003 | In progress, no observed PR | Start `P003-TASK-010` with task 310/317 reconciliation, then capture enablement, authentic authorization/provider diagnosis, feedback/effect/store/audit joins, and remaining exit work. |
| 004 | Not accepted | Full current UI and feedback polish and real input/accessibility proof. |
| 005 | Not accepted | Complete backend lifecycle, optional integrations, and failure/recovery convergence. |
| 006 | Not accepted | Implement missing quality gates first, freeze final product candidate, then run complete clean verification. |
| 007 | Not accepted | Documentation, artifact and evidence parity, final integration, signed tag, cleanup, and endpoint audit. |

This map records state, not alternative requirement ownership. A newly discovered defect is repaired in the current contiguous phase with its canonical requirement owner recorded. It does not reverse the execution cursor, create a stacked historical branch, erase a prior merge, or excuse the final regression. Historical blueprints retain the controls and proof actually delivered at that time.

### Proof reuse and invalidation

Every reuse decision compares source/API fingerprints, commit ancestry, artifact hashes, loader and dependency identities, configuration, provider, schema, fixture, harness, assertion semantics, and actual environment. A document edit alone does not invalidate unchanged behavior. A generator change requires regenerating its aggregate with every compatible input and reviewing changed classification semantics; it does not erase valid underlying runtime observations.

The retained broad artifact SHA-256 is `8a94802dfcac9902300a5e8e6ab9e9d3d45b7280436602fed2e99f7484dd2bc3`. Explicit reuse records identify historical and current inputs, differences, unaffected invariants, and residual claims. A changed policy, mutation, store, protocol, native API, dependency, or assertion invalidates its dependent rows. Old laptop proof does not establish the newly required application-stream mute procedure for future launches. Final Phase 006 executes the complete final suite after the last product change; history alone never substitutes for that gate.

Research alternatives are resolved as follows. Reuse valid proof instead of a blanket restart because unchanged primitives and signed integrations have bounded provenance. Use one portable Java client contract under `DEC-009`; native review uses actual source, API/ABI, artifact, historical proof, and Linux fixtures, without foreign runtime prerequisites. Extend existing doctors with bounded capture because the remaining failures need correlated decisions and effects that static health cannot explain. Diagnose provider login and rejected discovery before changing authorization. Disposition every advisory against the actual candidate instead of forcing a platform upgrade or assuming ownership proves safety.

## 5. Product Contract and Profile Coverage

| Profile area | Status | Source | Contract location | Rationale |
|---|---|---|---|---|
| Component architecture | covered | SRC-005 | Architecture and Ownership Boundaries | Lifecycle, command, policy, UI, persistence, native writer, integrations, and build ownership are defined. |
| Determinism | covered | SRC-004 | Determinism and Evidence Reproducibility | Inventories, generated references, normalized state, evidence manifests, and artifact hashes must reproduce. |
| Failure taxonomy | covered | SRC-003 | Failure Taxonomy and Recovery | Invalid input, authorization, dependency, persistence, lifecycle, partial success, and evidence failures are classified. |
| Generalization | covered | SRC-009 | Supported Environments and Generalization | One portable client contract across Linux, macOS and Windows; native branch analysis and available runtime proof without foreign-host requirements. |
| Inputs and outputs | covered | SRC-001 | Inputs, Outputs, and Observable Endpoint | Commands, payloads, configuration, durable data, evidence, and artifact outputs are defined. |
| Release lifecycle | covered | SRC-009 | Documentation, Operations, and Release Gates | Clean checkout, packaging, compatibility, provenance, evidence, rollback, and release readiness are defined without publication. |
| Security | covered | SRC-001 | Security, Privacy, and Supply Chain Contract | Leak, backdoor, trust-boundary, native writer, redaction, dependency, and artifact closure are mandatory. |
| State and persistence | covered | SRC-002 | State and Persistence Contract | All durable and transient state, schema, recovery, concurrency, and migration obligations are in scope. |
| Test system | covered | SRC-004 | Verification Strategy | Unit, GameTest, dedicated server, Minecraft Java client fixture, integration, recovery, performance, security, and artifact evidence are required. |
| Versioning | covered | SRC-006 | Compatibility and Versioning | Minecraft, NeoForge, Java, protocol, configuration, persistence, and artifact boundaries are pinned. |

### Inputs, Outputs, and Observable Endpoint

Inputs include every catalog-permitted player, console, RCON, command block, function, scheduled, GUI, shortcut, alias, bundle, sudo, panel, and server profile request; network payloads; configuration; durable state; registries; optional providers; lifecycle events; dependencies; and packaging inputs. Outputs include domain effects, feedback, suggestions, screens, HUDs, audit and observation, exports, persistent state, recovery material, references, evidence, and the JAR. Validate type, size, source, current authority, target, revision, and policy at the owning server boundary. Outputs disclose only authorized information and report exact outcomes.

### State and Persistence Contract

The server owns authoritative identity, policy, permissions, gameplay, and durable state. Client caches are presentation. Durable scope covers every repository and nonrepository JSON, TOML, NBT, configuration, journal, receipt, queue, index, object store, backup, recovery marker, audit/log file, offline adapter, and durable cache.

Each owner declares path ownership, schema/version, identity, bounds, write/flush/concurrency model, idempotency, migration, unsupported-version behavior, corruption handling, backup/restore, retention/privacy, lifecycle, and evidence. Damaged enforcement data cannot load as empty successful policy. Security-sensitive publication or append binds validation and mutation to the same opened descriptor or handle, verifies safe type and link/reparse state, and preserves identity through flush. Missing required identity metadata fails closed; path-only fallback is forbidden.

Sessions, sequence counters, confirmations, warmups, leases, GUI selections, projections, transfers, rate limits, and pending work expire or settle under reload, revocation, disconnect, dimension change, and shutdown. They cannot mint lasting authority.

### Failure Taxonomy and Recovery

| Failure | Observable result | Recovery and invariant |
|---|---|---|
| Invalid, oversized, unknown or ambiguous input | Bounded validation rejection before mutation | Correct input; no state rollback should be necessary. |
| Denied, revoked, stale or confused authority | Rejection with safe reason and current revision | New authorized request; old confirmations and leases remain invalid. |
| Provider absent, failed, stale or incompatible | Explicit provider/fallback state | Restore compatible provider or documented internal fallback; never implicit grant. |
| Corrupt, unsupported or semantically invalid storage | Recovery, unsupported or error state | Preserve original evidence and restore validated state before writes resume. |
| Publication/interruption failure | `not_committed`, `committed`, or `unknown` outcome | Resolve receipt/journal before retry; no duplicated nonidempotent side effect. |
| Cross-component partial success | Per-component outcomes and truthful aggregate | Declared compensation or explicit recovery, exactly once. |
| Replay, stale session or incompatible protocol | Bounded reject/drop before effect | New negotiation or supported command fallback. |
| Lifecycle/thread/queue failure | Bounded health failure with unsettled count | Settle owned work or retain recovery marker; never silently discard. |
| Evidence or environment failure | Precise failed or unverified row | Repair fixture/product and rerun invalidated scope without substituting weaker proof. |

### Security, Privacy, and Supply Chain Contract

Audit unintended authority or protected-data routes without presuming malicious intent: direct and nested commands, aliases, bundles, profiles, GUI, payloads, reflection, mixins, access transformers, configuration, migration, recovery, integrations, filesystem, and packaging. Trace every sensitive source to all possible sinks. Sensitive classes include credentials, tokens, private messages, addresses, hidden identities, restricted moderation reasons, command arguments, provider metadata, host paths, security evidence, and private projections.

Review source/actor confusion, hierarchy and exemption bypass, persistent grants, parser/codec bounds, archive/image/deserialization inputs, concurrency, path traversal, links and object substitution, audit independence, optional provider failure, and exports. Confirmed bypasses and leaks require repair at every severity.

Every live advisory, including low and medium entries, receives a candidate-specific record for resolved graph, declared owner, packaged presence, installed runtime, affected API/configuration reachability, authoritative advisory conditions, provenance, and compatible remedy. Platform ownership alone never closes applicability. Preserve pinned versions and exhaust safe compatible constraints, exclusions, removal of unsafe use, or guarded alternatives for mod-owned exposure. A proven irreconcilable applicable exposure is a concrete unresolved product decision, not an invented unavailable `EXT` or a silent pin change. Hosted alert count need not reach zero.

JNA and JNA Platform remain compile only, supplied compatibly by NeoForge 21.1.235. Prove API/ABI and installed artifact identities, native loading in the Linux fixture, and absence of duplicate JNA classes, service entries, or native runtime in the mod JAR.

### Supported Environments and Generalization

Minecraft Java behavior is one portable contract across Linux, macOS and Windows. The silent Linux laptop supplies client graphics and input; `node-1` supplies headless compute and dedicated servers. There are no separate macOS or Windows client or native runtime prerequisites. For native/provider branches, review actual source, branch conditions, ABI types, calling conventions, error handling, object identity and packaged/runtime API supply. Reuse compatible historical proof and execute the available Linux native fixtures. Record foreign runtime as not newly exercised, distinct from analysis, without marking Windows unsupported or leaving an invented foreign-host gate open.

Fixtures vary source, actor, online/offline identity, hierarchy, exemption, vanished target, namespace, dimension, registry content, resolution, GUI scale, permission state, provider response, and storage state. Shared proof requires an identical invariant; a different action's real domain effect cannot be inferred from a common callback.

### Determinism and Evidence Reproducibility

Inventories and references regenerate identically after normalizing explicit nondeterministic metadata. Semantic state and outcomes are reproducible from the same fixture and request. Record candidate, artifact SHA-256/SHA-512, versions, host/runtime, filesystem, dependencies, configuration and fixture fingerprints, exact stimulus, expected/actual result, evidence kind and limits, reuse/invalidation decision, and cleanup. Correlation IDs join processes without assuming synchronized clocks.

## 6. Mandatory Scope

- `SEFAUD-REQ-001`. Freeze and reconcile complete audit inventories, evidence, diagnostics, and traceability at one candidate revision.
- `SEFAUD-REQ-002`. Find and remediate security, privacy, leak, backdoor-like, authority, trust-boundary, and unsafe native-writer defects.
- `SEFAUD-REQ-003`. Close dependency, supply-chain, platform-runtime ownership, advisory, provenance, packaging, and native API compatibility risk.
- `SEFAUD-REQ-004`. Verify every executable administrator command and shortcut through its complete authority, effect, failure, persistence, feedback, and audit contract.
- `SEFAUD-REQ-005`. Polish and verify every graphical and textual operator UI with accessibility, responsive, fallback, privacy, and server-authority proof.
- `SEFAUD-REQ-006`. Audit every durable store, database-like owner, schema, migration, atomic write, queue, journal, backup, and recovery path across the codebase.
- `SEFAUD-REQ-007`. Verify backend lifecycle, logical-side handling, networking, optional integrations, reload, revocation, reconnect, partial failure, shutdown, and cross-channel convergence.
- `SEFAUD-REQ-008`. Run final clean-checkout, automated, GameTest, Minecraft Java client fixture, compatibility, recovery, performance, dependency, artifact, and regression verification.
- `SEFAUD-REQ-009`. Reconcile documentation and evidence and close release readiness at the frozen candidate without publishing.

Every confirmed in-scope defect remains mandatory until repaired and covered by a regression at its real failure boundary.

## 7. Optional / Future Scope

- `FUT-001`, excluded. Implement and activate the sixteen intentionally unavailable Phase 13 runtime families.
- `FUT-002`, excluded. Add unrelated commands, UI features, integrations, control families, or gameplay capabilities.
- `FUT-003`, excluded. Perform broad architecture modernization or class decomposition beyond confirmed defect remediation or mandatory evidence enablement.

## 8. Non-Goals

- `NG-001`. Do not implement new features, including intentionally unavailable control family runtimes.
- `NG-002`. Do not upgrade or expand Minecraft, NeoForge, mappings, Java, Gradle, loader, or protocol boundaries.
- `NG-003`. Do not publish a release, deploy to production, or run destructive production tests.
- `NG-004`. Do not weaken tests, permissions, redaction, recovery, compatibility, or acceptance gates, and do not mark blocked or unexecuted evidence as passed.
- `NG-005`. Do not use real credentials, private messages, personal addresses, production worlds, or unrelated user data in fixtures or evidence.
- `NG-006`. Do not claim mathematical absence of defects. Completion means the exhaustive declared matrices and adversarial evidence reveal no known disallowed finding at the frozen revision.

## 9. Owner Decisions

### DEC-001 — Does the audit include remediation and regression proof?

**Status:** RESOLVED
**Selected choice:** Audit plus mandatory in-scope remediation and regression proof.
**Rationale:** The requested endpoint includes fixing confirmed defects and proving the repair.
**Affected requirements:** SEFAUD-REQ-001, SEFAUD-REQ-002, SEFAUD-REQ-003, SEFAUD-REQ-004, SEFAUD-REQ-005, SEFAUD-REQ-006, SEFAUD-REQ-007, SEFAUD-REQ-008, SEFAUD-REQ-009
**Supersedes:** none

### DEC-002 — How are intentionally unavailable controls handled?

**Status:** RESOLVED
**Selected choice:** Keep the sixteen named families unavailable, unreachable, side-effect-free, and negatively tested.
**Rationale:** Unavailable families are explicit negative contracts, not unimplemented promises to add during this audit.
**Affected requirements:** SEFAUD-REQ-001, SEFAUD-REQ-002, SEFAUD-REQ-003, SEFAUD-REQ-004, SEFAUD-REQ-005, SEFAUD-REQ-006, SEFAUD-REQ-007, SEFAUD-REQ-008, SEFAUD-REQ-009
**Supersedes:** none

### DEC-003 — What is the completion defect boundary?

**Status:** RESOLVED
**Selected choice:** No known applicable critical or high exploitable vulnerability, authorization bypass, sensitive leak, executable admin-command defect, UI-blocking defect, persistence-integrity defect, or mandatory backend defect at the frozen revision.
**Rationale:** Exhaustive declared proof supports a bounded completion claim, not mathematical absence of all possible future defects.
**Affected requirements:** SEFAUD-REQ-001, SEFAUD-REQ-002, SEFAUD-REQ-003, SEFAUD-REQ-004, SEFAUD-REQ-005, SEFAUD-REQ-006, SEFAUD-REQ-007, SEFAUD-REQ-008, SEFAUD-REQ-009
**Supersedes:** none

### DEC-004 — What compatibility versions are fixed?

**Status:** RESOLVED
**Selected choice:** Preserve Minecraft 1.21.1, NeoForge 21.1.235, Java 21, checked-in Gradle, Parchment 2024.11.17, and current protocol and integration contracts.
**Rationale:** Audit remedies must preserve the existing supported platform and public contracts.
**Affected requirements:** SEFAUD-REQ-001, SEFAUD-REQ-002, SEFAUD-REQ-003, SEFAUD-REQ-004, SEFAUD-REQ-005, SEFAUD-REQ-006, SEFAUD-REQ-007, SEFAUD-REQ-008, SEFAUD-REQ-009
**Supersedes:** none

### DEC-005 — What verification environments and data are safe?

**Status:** RESOLVED
**Selected choice:** Use disposable synthetic staging fixtures only and exclude production mutation, credentials, personal data, and production worlds.
**Rationale:** Fault and recovery cases require disposable synthetic state, never production data.
**Affected requirements:** SEFAUD-REQ-001, SEFAUD-REQ-002, SEFAUD-REQ-003, SEFAUD-REQ-004, SEFAUD-REQ-005, SEFAUD-REQ-006, SEFAUD-REQ-007, SEFAUD-REQ-008, SEFAUD-REQ-009
**Supersedes:** none

### DEC-006 — What candidate lineage is authoritative?

**Status:** RESOLVED
**Selected choice:** Use the SEF 2 candidate lineage beginning at the selected envy/sef2_complete base and preserve branch evidence without silent legacy integration.
**Rationale:** Signed SEF 2 integrations establish the candidate lineage; the legacy default cannot silently replace it.
**Affected requirements:** SEFAUD-REQ-001, SEFAUD-REQ-002, SEFAUD-REQ-003, SEFAUD-REQ-004, SEFAUD-REQ-005, SEFAUD-REQ-006, SEFAUD-REQ-007, SEFAUD-REQ-008, SEFAUD-REQ-009
**Supersedes:** none

### DEC-007 — What optional work is included?

**Status:** RESOLVED
**Selected choice:** Exclude all optional and future scope unless explicitly promoted later.
**Rationale:** Optional expansion would change the owner's selected endpoint.
**Affected requirements:** none
**Supersedes:** none

### DEC-008 — What UI scope is mandatory?

**Status:** RESOLVED
**Selected choice:** Include all in-scope graphical screens, HUDs, and administrator command feedback.
**Rationale:** Graphical and textual interfaces both communicate privileged outcomes and need full verification.
**Affected requirements:** SEFAUD-REQ-001, SEFAUD-REQ-002, SEFAUD-REQ-003, SEFAUD-REQ-004, SEFAUD-REQ-005, SEFAUD-REQ-006, SEFAUD-REQ-007, SEFAUD-REQ-008, SEFAUD-REQ-009
**Supersedes:** none

### DEC-009 — What is the platform and native dependency evidence contract?

**Status:** RESOLVED
**Selected choice:** Minecraft Java behavior is one portable contract across Linux, macOS and Windows. Use the silent Linux laptop client and node-1 headless server. Analyze actual native source and artifacts, reuse valid historical evidence and execute available runtime checks, without foreign-host client or native runtime prerequisites. EXT-001 and EXT-002 remain available mandatory in-plan closure. JNA is compile-only and supplied by pinned NeoForge, with no embedded duplicate runtime.
**Rationale:** Portable Java behavior and actual native implementation need different evidence claims. Available Linux execution plus explicit source/API/artifact and historical analysis satisfy the selected native audit fidelity without foreign-host dependencies.
**Affected requirements:** SEFAUD-REQ-001, SEFAUD-REQ-002, SEFAUD-REQ-003, SEFAUD-REQ-004, SEFAUD-REQ-005, SEFAUD-REQ-006, SEFAUD-REQ-007, SEFAUD-REQ-008, SEFAUD-REQ-009
**Supersedes:** Earlier conditional foreign runtime prerequisites and external-blocker interpretations; the stable decision ID is retained.


The sixteen unavailable families are `admin_journal`, `afk_zones`, `approvals`, `capability_leases`, `chat_channels`, `display_ownership`, `display_profiles`, `player_warp_review`, `portal_policy`, `resource_governor`, `resource_worlds`, `rollouts`, `server_presentation`, `spawn_ecology`, `staff_duty`, and `waypoints`.

## 10. External Prerequisites

The stable `EXT` IDs name available mandatory in-plan inputs and closure work. They do not imply unavailable facilities, external owner action, or already passed evidence.

| ID | Prerequisite | Affected requirements | Availability | Authorization | Required external action |
|---|---|---|---|---|---|
| EXT-001 | Available canonical runtime, portable client and native analysis closure | SEFAUD-REQ-002, SEFAUD-REQ-003, SEFAUD-REQ-004, SEFAUD-REQ-005, SEFAUD-REQ-006, SEFAUD-REQ-007, SEFAUD-REQ-008, SEFAUD-REQ-009 | available | not_required | None. Complete the owned evidence contract below within this plan. |
| EXT-002 | NeoForge-owned dependency provenance, runtime supply, reachability, advisory applicability, and mod-owned remediation closure | SEFAUD-REQ-003, SEFAUD-REQ-008, SEFAUD-REQ-009 | available | not_required | None. Complete the owned evidence contract below within this plan. |

### EXT-001 — Available canonical runtime, portable client and native analysis closure

**Kind:** environment
**Required evidence:** Pinned candidate commit and artifact identity; one silent Linux Minecraft Java client fixture for portable client graphics and input; dedicated server and headless evidence on `node-1` where appropriate; opened-object identity and provider behavior for native paths; exact runtime, cleanup, and sanitized evidence manifest.

Record actual executed OS, architecture, filesystem, Java/loader/dependency versions and configuration. Analyze every supported native branch using source, API/ABI and artifact evidence; distinguish analysis, compatible retained runtime proof, and new Linux execution. No foreign runtime is required. Missing laptop capability leaves its exact client row unverified while independent work continues. Execution discovers paths and verifies capabilities before launch; availability never replaces that check.

### EXT-002 — Dependency provenance and advisory closure

**Kind:** artifact
**Required evidence:** Declared, resolved, runtime, test, tooling, and packaged graphs; coordinates, exact versions, authoritative source, SHA-256, SHA-512, licenses/provenance, compatibility and security review; NeoForge supplied JNA API and binary compatibility; no duplicate JNA classes/native runtime in the JAR; separate mod-owned remedy and platform-owned disposition records.

Reconcile all 28 observed advisories and refresh the set at final verification. For alerts 28 (`GHSA-c4c3-7fpv-j4q5`) and 27 (`GHSA-fccg-mwvh-qqg4`), explicitly trace Netty SNI routing, per-SNI/default context and mutual TLS behavior, and fragmented ClientHello handling against the actual installed pipeline. The observed graph resolves Netty 4.1.97.Final; the advisories identify versions through 4.1.136.Final and first patched 4.1.137.Final. These facts start analysis, not an automatic upgrade instruction or reachability conclusion.

`NOT_MOD_RESOLVABLE` records upstream ownership only after direct declaration, packaging and affected API exposure have been assessed with concrete provenance. It never means no risk by definition. No known applicable critical/high exploitable mod-owned exposure may remain. A newly found issue updates affected requirement evidence under the current phase and final Phase 006; historical Phase 001 is not replayed wholesale.

## 11. Architecture and Ownership Boundaries

| Boundary | Canonical component owner | Data flow and invariant |
|---|---|---|
| Lifecycle/service graph | `ServerEssentialsForge`, `KernelServices`, lifecycle handlers | Construction, registration, startup, ticks, reload and stop own resources and ordering; common paths never load client classes. |
| Command admission | `CommandCatalog`, `KernelCommandExecutor`, `CommandExecutionService` | One canonical action enters feature, source, permission, hierarchy, target, cooldown, quota, cost, warmup and confirmation checks before the domain mutation. |
| Authority | `PermissionService`, provider bridge, hierarchy/exemption and revision owners | Provider decisions and actor/source context are current and fail closed; temporary authority cannot mint lasting grants. |
| Indirection | Alias, bundle, sudo, run, silent, profile, panel and control services | Stored text carries intent, never authority. Preserve real/effective actor, source, target, policy, revision and audit context. |
| GUI/network | `SefNetwork`, `SefSessionManager`, `SefGuiServer`, `PanelActionValidator`, client screens | Server projects only authorized data. Session, panel, entry and policy are revalidated before mutation. Client caches remain presentation. |
| Domain effects | Teleport, moderation, inventory, economy, controls, escrow, social, tags, disguise and other registered domains | Exactly one mutation, explicit partial failure, domain-specific recovery and truthful feedback. |
| Persistence | `StorageCoordinator`, `StorageRepository`, `AtomicFileStore`, workers and nonrepository owners | Server snapshots flow into ordered publication; valid data survives failure and unresolved durable outcomes block unsafe retry. |
| Configuration | `ModuleConfigRegistry`, `ModuleConfigService`, NeoForge bootstrap settings | Typed bounded validation, transactional publication and revision invalidation; secrets remain filtered. |
| Audit/native writer | `SecurityAuditService`, `NativeAuditFileProvider`, redaction and optional sinks | Mandatory terminal audit is independent of observation filters. Same opened object is checked, written and flushed. |
| Optional integrations | Runtime guarded LuckPerms, FTB Essentials, Curios and declared adapters | Explicit present/absent/error state and bounded fallback; no absent provider grant or client classloading leak. |
| Build/evidence | Gradle, test and GameTest source sets, inventory/reference generators | Exact candidate, source inputs, deterministic inventories and packaged output form one proof chain. |

Dependency direction is authority to domain effect to durable result and authorized presentation. Logs, caches, generated documentation and recovery files do not become independent authority. Reflection, native calls, runtime dispatch and external evidence joins require bounded direct review beyond the static graph.

### Frozen shared interfaces

These are logical contracts for existing boundaries and planned evidence/capture additions. Signatures specify typed inputs and outputs, not invented existing Java methods or a required class decomposition. Existing source owners implement the behavior using repository conventions. Each interface has one producer phase; downstream phases consume it without competing definitions. All use version 1 unless explicitly stated. Additive optional fields require decoder tests; incompatible changes require explicit schema/version handling and affected proof renewal.

| ID | Producer and consumers | Signature and fields | Errors and version behavior | Acceptance |
|---|---|---|---|---|
| SEFAUD-IF-001 | Producer Phase 000. Consumers 001 through 007; Phase 003 reconciles aggregate joins. | `reconcile(candidate: CandidateIdentity, inventory: Surface[], proofs: EvidenceRecord[]) -> Matrix`. Candidate fields: commit, SHA-256/SHA-512, loader/JVM/dependency/config/fixture/harness fingerprints. Surface: stable id, owner, requirement, routes, dimensions. Evidence: id, kind enum, expected/actual, exact inputs, source locator, result enum, limits, cleanup. Matrix row: surface, dimension, evidence ids, disposition. | Reject duplicate, unowned, missing input, stale identity and incompatible schema. `open`, `partial`, `failed`, `passed`, and `not_applicable` are distinct; not applicable needs a predicate proof. Never silently discard a compatible input or infer a pass from count equality. | SEFAUD-AC-001, SEFAUD-AC-010, SEFAUD-AC-024 |
| SEFAUD-IF-002 | Producer Phase 001 authority contract, domain completion in 003. Consumers 002 through 007. | `execute(request: ActionRequest, authority: AuthoritySnapshot) -> ActionOutcome`. Request: canonical action, route/source, real/effective actor, target, sanitized argument identity, expected revision, confirmation/correlation. Outcome: decision/reason, before/after semantic state, mutation status, durable outcome, feedback key/class, terminal audit id. | Invalid, denied, stale, expired, unavailable, domain error and partial/unknown outcomes remain distinguishable. Recheck at mutation; denial leaves protected state unchanged. Existing wire/public identifiers remain compatible. | SEFAUD-AC-004, SEFAUD-AC-011, SEFAUD-AC-012 |
| SEFAUD-IF-003 | Producer Phase 002. Consumers 003 through 007. | `publish(operation: DurableOperation, snapshot: VersionedSnapshot) -> DurableOutcome`. Fields: store/operation id, source/target revisions, schema, snapshot identity, idempotency/receipt, publication stage, committed state, recovery locator, flush/queue health. | `not_committed`, `committed`, `unknown`; explicit ready/recovery/unsupported/error/closed state. Corruption never becomes empty success. Restore and retry depend on verified outcome. | SEFAUD-AC-016, SEFAUD-AC-017, SEFAUD-AC-018 |
| SEFAUD-IF-004 | Producer Phase 003, early `P003-TASK-010`. Consumers remaining 003 and Phases 004 through 007. No historical 000 through 002 dependency. | `enable(category: Category, target: Scope, seconds: int, sampleLimit: int, source: AuthorizedSource) -> CaptureStatus`; `status(source) -> CaptureStatus`; `disable(source) -> CaptureStatus`; event schema version 2 and typed signal fields in Diagnostics below. | Unauthorized, invalid scope/limit, busy, missing target and unsafe sink reject; saturation drops/counts; timeout/reset stops. No domain mutation or silent resumed capture. Old doctors remain independent. | SEFAUD-AC-012, SEFAUD-AC-021, SEFAUD-AC-024 |
| SEFAUD-IF-005 | Producer Phase 001. Consumers 002, 003, 005, 006, 007. | `append(opened: VerifiedAuditObject, event: SanitizedAuditEvent) -> AppendOutcome`. Object: provider, identity, safe type/link state, descriptor/handle ownership. Outcome: same-object check, bytes, append/flush state, safe error, recovery state. Supply record: API/version/ABI, platform origin, hashes, JAR duplicate result. | Unknown identity, unsafe link/reparse, substitution, API/linkage, write and flush failures fail closed, preserve prior valid state and report exact outcome. Native evidence uses DEC-009 fidelity. | SEFAUD-AC-006, SEFAUD-AC-009, SEFAUD-AC-018 |
| SEFAUD-IF-006 | Producer Phase 000 packet schema. Consumers 001 through 007. | `closePhase(phase: PhaseId, candidate: CandidateIdentity, gates: GateReceipt[]) -> CompletionPacket`. Fields: requirements/tasks, evidence/invalidation, docs, checks, review, PR/merge commit, resulting candidate verification, signed tag/signature, cleanup and exact next phase. | Incomplete checks, queued/unmerged PR, stale artifact, missing tag, contradictory evidence or cleanup failure reject closure. Final packet also requires the exact endpoint and all nine requirements. | SEFAUD-AC-003, SEFAUD-AC-026, SEFAUD-AC-027 |

Later defects reopen affected acceptance claims and downstream evidence, not requirement ownership or completed Git history. Shared capture is deliberately a Phase 003 addition under command evidence enablement. Original Phase 000 through 002 proof uses delivered doctors, health, test reports and native manifests. Phase 006 repeats earlier security and storage boundaries using the new capture; no backward dependency exists.

## 12. Requirements

### SEFAUD-REQ-001 — Authoritative baseline and complete traceability

**Behavior:** Freeze and reconcile the complete audit surface, evidence identities, ownership, diagnostic observations and input joins.
**Owner:** Repository audit contract
**Contributors:** All source domains, build, reference generators and evidence owners
**Dependencies:** DEC-002, DEC-003, DEC-004, DEC-005, DEC-006, DEC-007, DEC-008, DEC-009
**Lifecycle stage:** readiness
**Production verification:** none
**Release impact:** stable release

**Acceptance criteria**

- SEFAUD-AC-001. One manifest binds every live dispatcher action, shortcut, permission, configuration, UI surface, store/writer, schema, lifecycle handler, adapter, trust boundary, native branch, dependency, test, document and packaged resource to a unique owner, requirement and proof route. Deterministic reconciliation rejects missing, duplicate, stale or unowned rows.
- SEFAUD-AC-002. Counts come from source/build/runtime joins, not historic prose. Each proof identifies inputs, fidelity, result and reuse/invalidation. Inventory of native branches and available facilities follows DEC-009 without foreign runtime gates.
- SEFAUD-AC-003. Evidence and phase-packet schemas retain expected/actual results, false-completion checks, safe retention and exact cleanup. Original diagnostics are inventoried; planned Phase 003 capture is identified as proposed, not an unmet historical capability.

**Required evidence**

- CodeGraph and bounded source/build inventory, generated/reference/live dispatcher reconciliation and mutation tests that inject omitted, duplicate or stale rows.
- Baseline manifest and requirement to phase to task to evidence report using SEFAUD-IF-001 and SEFAUD-IF-006, with signed historical integration provenance.
- Current task 310/317 aggregate reconciliation in P003-TASK-010 and final Phase 006 regeneration supplement the retained baseline without restarting Phase 000.

### SEFAUD-REQ-002 — Security, privacy and authority closure

**Behavior:** Inspect, threat model, repair and adversarially verify authority, protected data, payload, filesystem, native writer, persistence, integration and artifact boundaries.
**Owner:** Security audit boundary
**Contributors:** Commands, GUI/network, storage, configuration, domain services, optional adapters and packaging
**Dependencies:** SEFAUD-REQ-001, DEC-001, DEC-003, DEC-005, DEC-009, EXT-001
**Lifecycle stage:** change
**Production verification:** none
**Release impact:** stable release

**Acceptance criteria**

- SEFAUD-AC-004. Every direct and indirect route respects current actor/source, hierarchy, exemption, target, permission, policy, revision and confirmation at mutation. Forged, replayed, revoked, expired, stale, nested and provider-failure paths produce no unauthorized effect.
- SEFAUD-AC-005. Sensitive data is absent from unauthorized discovery, suggestions, feedback, GUI/HUD, audit/observation, logs, errors, exports, fixtures, documentation and artifacts. Mandatory audit remains correlated and independent of optional filters.
- SEFAUD-AC-006. Paths, bounds, archive/image/deserialization, reflection, mixins and native calls preserve their trust boundary. Same opened object is validated, appended and flushed; unsafe link/reparse, replacement, missing metadata and write failure preserve prior state and fail closed. No known bypass, leak, unintended authority or applicable critical/high exploitable repository vulnerability remains.

**Required evidence**

- Complete boundary and source-to-sink matrix, adversarial unit/integration/GameTest tests and actual allowed/denied entry routes with before/after state and audit.
- Native source/API/ABI/artifact review, retained proof applicability and Linux object-swap, link, metadata, append/flush, rotation, restart and failure fixtures under DEC-009.
- Candidate-bound security review, scan/artifact inspection, current-phase regressions for changed claims and final Phase 006 repetition.

### SEFAUD-REQ-003 — Dependency and native supply closure

**Behavior:** Resolve every advisory against the candidate and installed runtime; remedy applicable mod-owned exposure compatibly and retain defensible platform dispositions.
**Owner:** Dependency graph
**Contributors:** Build, NeoForge runtime, optional adapters, packaging and security evidence
**Dependencies:** SEFAUD-REQ-001, DEC-003, DEC-004, DEC-009, EXT-001, EXT-002
**Lifecycle stage:** change
**Production verification:** none
**Release impact:** stable release

**Acceptance criteria**

- SEFAUD-AC-007. Every observed and refreshed advisory has exact candidate version/path, owner, packaged and installed presence, affected API/configuration reachability, advisory conditions, authoritative provenance and remedy/disposition. All 28 observed alerts, including critical 28, are addressed individually.
- SEFAUD-AC-008. No known applicable critical/high exploitable mod-owned exposure remains. Compatible remedies preserve pins and optional integration behavior. No alert closes solely from ownership, transitivity, legacy branch provenance, JAR absence or lack of foreign execution.
- SEFAUD-AC-009. Declared/compile/runtime/test/tooling/packaged graphs and installed platform match their hashes and licenses. JNA/JNA Platform remain compile only; pinned NeoForge supplies compatible APIs/ABI; Linux load and native behavior pass; no duplicate runtime/classes/services are embedded.

**Required evidence**

- EXT-002 graph, installed runtime, SHA-256/SHA-512, advisory, license/provenance and reachability ledgers, including actual Netty SNI/ClientHello pipeline analysis.
- Compatible remedy regressions, present/absent optional dependency tests, Linux load/native fixtures and DEC-009 source/artifact analysis for other branches.
- Current advisory refresh and final JAR inspection. Supersede the old 26-alert ownership packet with these predicates rather than a zero-alert requirement.

### SEFAUD-REQ-004 — Every administrator action works end to end

**Behavior:** Close one complete behavioral contract for every executable administrator action and shortcut, with explicit negative contracts for unavailable families.
**Owner:** Command policy kernel
**Contributors:** Domain commands, permissions, GUI, indirection, persistence, audit and capture
**Dependencies:** SEFAUD-REQ-001, SEFAUD-REQ-002, SEFAUD-REQ-003, SEFAUD-REQ-006, DEC-002, DEC-008, DEC-009, EXT-001
**Lifecycle stage:** change
**Production verification:** none
**Release impact:** stable release

**Acceptance criteria**

- SEFAUD-AC-010. The reconciled live tree, catalog, shortcuts and module state define the exact executable set. Every action has complete discovery, authority, source, target, argument, effect, failure, persistence, feedback, audit and route dimensions; every exclusion states why the dimension is inapplicable. No parser or shared callback alone closes a domain effect.
- SEFAUD-AC-011. Absent, denied, granted, inherited/wildcard, revoked, refreshed, reloaded and reconnected permission states obey source/hierarchy/exemption/feature/cooldown/quota/cost/warmup/confirmation policy. All allowed and forbidden sources, relevant self/online/offline/equal/higher/exempt/vanished/unknown/ambiguous/stale/bulk targets, and missing/malformed/minimum/maximum/outside-bound/namespaced/long/injection-like arguments have decisive results. Success changes exact state once; denial leaves protected state unchanged; domain failure and partial completion have truthful recovery.
- SEFAUD-AC-012. Canonical/shortcut/GUI/fallback/alias/bundle/sudo/panel/profile/integration routes preserve action, effective actor, cooldown, store outcome, feedback and one terminal audit event. Preview binds actor/source/target/arguments/policy/revision/expiry. Every unavailable family remains unreachable and side-effect-free. Phase 003 delivers and self-tests SEFAUD-IF-004 before dependent captures, including authentic console use and default denied player access. No known executable command defect remains.

**Required evidence**

- Reconciled 729-row intake with live regeneration, candidate-compatible task 310 and task 317 inputs, per-dimension evidence and explicit delta routing if live counts change.
- Unit/dispatcher and real-path server GameTests plus synthetic multi-actor dedicated-server workflows, action-specific before/after effects, failure cut points, persistence/restart and audit/sink joins.
- Real laptop command-tree/feedback and necessary GUI-route observations; exact provider login/grant/deny/revoke/refresh/reconnect/outage fixtures. Diagnose observed failures without broad operator grants.
- Capture authorization, bounds, expiry/reset, redaction, dropped counters, sink failure, disabled and enabled overhead and unchanged-outcome tests.

### SEFAUD-REQ-005 — Complete graphical and textual UI quality

**Behavior:** Polish and verify all screens, HUDs, workflow/picker/item browsers, pause entry, confirmations, InvSee, Fancy Tags, overlays, fallbacks and administrator command feedback.
**Owner:** GUI presentation
**Contributors:** Server projections, sessions, command messages, domain workflows, client caches and accessibility
**Dependencies:** SEFAUD-REQ-001, SEFAUD-REQ-002, SEFAUD-REQ-004, DEC-008, DEC-009, EXT-001
**Lifecycle stage:** change
**Production verification:** none
**Release impact:** stable release

**Acceptance criteria**

- SEFAUD-AC-013. Every surface has intentional hierarchy, labels/values, spacing, contrast, localized grammar/plurals, primary action and loading/empty/validation/failure/success/unavailable states. Required content remains reachable at GUI scales 1 through 4, 854 by 480 and 1280 by 720, narrow/wide aspect ratios and live resize, including long translations and values.
- SEFAUD-AC-014. Mouse and keyboard-only operation, focus order and restoration, Escape/back, narration, tooltip and error announcement work. Search/pagination/rapid refresh, icons and bounded native tooltips remain accurate. Visual or recorded evidence proves only named presentation/interaction claims, with inaudible audio capture for narration.
- SEFAUD-AC-015. Unauthorized/private data is absent; stale selection, permission loss, module disablement, revision change, disconnect and dimension change reject or refresh before mutation. Enhanced and fallback paths preserve domain result and audit identity. Unavailable controls never imply readiness. No known blocking or materially misleading defect remains.

**Required evidence**

- Full surface/state inventory, layout/workflow/session/codec/authority tests and equivalent enhanced/fallback domain results.
- Silent Linux laptop input/rendering/accessibility ledger and targeted captures per screen class/state/scale, including narration and dynamic invalidation.
- Server decision, client receipt, actual presentation and resulting mutation/audit joined separately. Simulated players or screenshots alone cannot replace this evidence.

### SEFAUD-REQ-006 — Every durable owner preserves integrity

**Behavior:** Audit and repair every durable store and database-like owner and every cross-store invariant.
**Owner:** Persistence layer
**Contributors:** Domains, configuration, audit/logging, workers, offline adapters and lifecycle
**Dependencies:** SEFAUD-REQ-001, SEFAUD-REQ-002, DEC-005, DEC-009, EXT-001
**Lifecycle stage:** change
**Production verification:** none
**Release impact:** stable release

**Acceptance criteria**

- SEFAUD-AC-016. Complete repository and nonrepository owner rows specify schema/version/path/bounds/privacy/retention/identity/cardinality/concurrency/flush/migration/recovery/rollback. Empty, valid, legacy, newer unsupported, malformed, truncated, oversized, deep, duplicate, stale and invalid data produce explicit correct state.
- SEFAUD-AC-017. Snapshot and async publication ownership are race safe. Concurrent, coalesced, periodic, explicit and shutdown writes preserve last valid data and directory durability limitations are truthful. Each primitive and distinct high-risk commit protocol survives fault injection and process interruption. Nonidempotent effects resolve journal/receipt/unknown outcome before retry.
- SEFAUD-AC-018. Migration validates staged outputs and original fingerprint, preserves exact recovery material, rejects conflicting backups and proves restoration. Cross-store UUID/revision/reference/index/claim/escrow/authorization/expiry/configuration invariants survive restart. Sensitive writers satisfy SEFAUD-IF-005. No known integrity defect remains.

**Required evidence**

- Retained Phase 002 owner/fault packet with input matching; current changed-owner and command/store delta coverage.
- Synthetic corruption, concurrency, interruption, migration/rollback, shutdown/timeout, restart, native Linux filesystem and recovery fixtures with operation IDs and before/after semantic/file hashes.
- Explicit source/API/artifact analysis under DEC-009, storage health, recovery evidence and final Phase 006 full regression.

### SEFAUD-REQ-007 — Lifecycle and integration converge safely

**Behavior:** Verify backend ordering, current authority, logical side/thread ownership, optional providers and exact cross-channel outcomes through normal and failed lifecycle transitions.
**Owner:** Lifecycle integration boundary
**Contributors:** Commands, GUI/network, storage, configuration, adapters and all stateful domains
**Dependencies:** SEFAUD-REQ-002, SEFAUD-REQ-003, SEFAUD-REQ-004, SEFAUD-REQ-005, SEFAUD-REQ-006, DEC-009, EXT-001
**Lifecycle stage:** change
**Production verification:** none
**Release impact:** stable release

**Acceptance criteria**

- SEFAUD-AC-019. Construction/manifest/registration/start/world-load/login/tick/reload/module publication/provider refresh/logout/stopping/stopped sequences have tested ordering and ownership. Feature, policy, permission, command-tree, target, session/panel/record and storage revisions invalidate stale work.
- SEFAUD-AC-020. Enhanced enabled/disabled, matching/no-SEF/incompatible/mixed clients, disconnect/reconnect, death/respawn/dimension change and optional provider absent/startup failure/outage/removal/stale cache/malformed/recovery states preserve documented fallback and clear or settle owned sessions, transfers, menus, grants, drafts, warmups and queues.
- SEFAUD-AC-021. Cross-component game effect/store/cost/cooldown/escrow/audit/adapter partial failure reports exact outcome and compensates or recovers once. Server mutations use the correct thread; common code is client-class safe; native/runtime linkage is explicit. Bounded diagnostics explain state without granting authority. No known mandatory integration defect remains.

**Required evidence**

- Lifecycle/channel matrix with actual entry points, fault cut points, before/after state, correlation and bounded timing.
- Dedicated-server startup/shutdown/restart, provider states and native load, plus real laptop mixed-client, reconnect, UI and synchronization evidence.
- Classloading/package inspection, thread assertions and unchanged authority under failure, with final Phase 006 repetition.

### SEFAUD-REQ-008 — Final clean candidate passes all verification

**Behavior:** Deliver missing maintained checks, freeze the final product revision, and execute complete clean-checkout regression, runtime, compatibility, recovery, performance and artifact proof.
**Owner:** Release verification system
**Contributors:** All requirement owners, Gradle, CI and staging harness
**Dependencies:** SEFAUD-REQ-002, SEFAUD-REQ-003, SEFAUD-REQ-004, SEFAUD-REQ-005, SEFAUD-REQ-006, SEFAUD-REQ-007, DEC-009, EXT-001, EXT-002
**Lifecycle stage:** post_change
**Production verification:** none
**Release impact:** stable release

**Acceptance criteria**

- SEFAUD-AC-022. Before freezing the final verification candidate, maintained `check` includes deterministic formatting, zero-growth compiler warnings, reviewed static analysis, units and risk-based line/branch coverage for critical authorization and persistence paths. No unreviewed blanket suppression; changed critical branches and their negative/recovery paths are exercised, with explicit per-package measured thresholds and justified exclusions committed before the final run.
- SEFAUD-AC-023. Clean Java 21 wrapper checks, build, `compileFallbackRuntimeJava`, `generateProjectReferences`, `generatePerformanceReport`, all required server-only GameTests and zero unexplained tracked drift pass. Real Linux server startup/operation/native audit/save/bounded shutdown/restart, provider/fallback/mixed/incompatible clients, full UI/accessibility, InvSee, admission capacity/FIFO, disguise animation, packet abuse, revocation, reconnect, dimensions and persistence recovery pass at required fidelity.
- SEFAUD-AC-024. Performance, source/diff, secrets, dependencies, licenses, metadata, mixins/access transformer, resources, JAR entries, hashes and duplicate-native gates pass. The complete mandatory suite runs after the last product change. All final claims bind one candidate/artifact with accurate analysis/runtime distinctions and verified cleanup.

**Required evidence**

- Early Phase 006 quality-gate implementation and self-checks, followed by clean checkout reports, GameTest counts, drift and full runtime manifests.
- Final security/storage regressions using Phase 003 capture, refreshed EXT-001/EXT-002 packets, provider and native supply proof, bounded performance results.
- Candidate JAR SHA-256/SHA-512, complete contents/diff audit, final rerun ledger with no incomplete mandatory row and exact cleanup receipts.

### SEFAUD-REQ-009 — Documentation and integrated endpoint close

**Behavior:** Reconcile all user/operator/developer documentation and evidence with the final artifact, integrate verified phases, and record release readiness without publication.
**Owner:** Documentation evidence
**Contributors:** All requirement owners, repository tracking, technical documentation and release workflow
**Dependencies:** SEFAUD-REQ-001, SEFAUD-REQ-002, SEFAUD-REQ-003, SEFAUD-REQ-004, SEFAUD-REQ-005, SEFAUD-REQ-006, SEFAUD-REQ-007, SEFAUD-REQ-008, DEC-009, EXT-001, EXT-002
**Lifecycle stage:** post_change
**Production verification:** none
**Release impact:** stable release

**Acceptance criteria**

- SEFAUD-AC-025. README, DOCUMENTATION, documentation index, generated command/permission/configuration references, compatibility, installation, security, troubleshooting, migration/recovery, test/acceptance, performance and release guidance match verified behavior and actual evidence limits. All supported OS claims preserve DEC-009; unavailable families stay unavailable.
- SEFAUD-AC-026. Each requirement, task, action/dimension, store, UI, lifecycle, native and advisory row has an authoritative sanitized proof and invalidation decision. Final clean artifact and documentation/source comparison reject stale counts, dropped evidence, unexecuted passes, incomplete cleanup and source-presence completion.
- SEFAUD-AC-027. Every phase's required checks, review disposition, PR merge into the verified candidate integration branch, resulting-branch verification and signed annotated phase tag pass. Final endpoint and Definition of Done close at one candidate/artifact. No known disallowed defect, unclosed mandatory row, publication or production action remains.

**Required evidence**

- Documentation/link/drift checks, final support-procedure replay and all-nine-requirement traceability.
- Final clean checkout and artifact comparison, integrated commit and tag evidence, completed phase packets and cleaned resources.
- Exact endpoint checklist and release-readiness record; platform-owned alerts retain their complete dispositions rather than a promise of zero hosted alerts.

## 13. Phased Roadmap

The complete sequence is frozen below. Each linked file owns its sole full phase declaration and detailed ordered tasks. Historical acceptance is interpreted using Section 4; current execution starts in Phase 003, not a blanket Phase 000 rerun. Requirement ownership is exclusive for contract closure; later phases contribute regressions and correct newly discovered failures.

| Phase ID | Objective | Owner | Dependencies | Canonical requirements | Entry summary | Exit summary | Next transition | Execution blueprint |
|---|---|---|---|---|---|---|---|---|
| SEFAUD-PHASE-000 | Establish reproducible inventories and evidence ownership. | Repository audit contract | DEC-002, DEC-003, DEC-004, DEC-005, DEC-006, DEC-007, DEC-008, DEC-009 | SEFAUD-REQ-001 | Candidate lineage and complete source requirements identified. | Surface/owner/evidence inventories reconcile; original controls and packet schemas are usable; no missing or duplicate mandatory owner. | SEFAUD-PHASE-001 | [Phase 000](phases/plan-phase-000.md) |
| SEFAUD-PHASE-001 | Close security boundaries, native writer and dependency exposure. | Security audit boundary | SEFAUD-PHASE-000, SEFAUD-REQ-001, EXT-001, EXT-002 | SEFAUD-REQ-002, SEFAUD-REQ-003 | Inventory, trust boundaries and dependency inputs accepted. | Every boundary and advisory has required disposition, confirmed defects repaired, native identity/supply and adversarial proof pass under DEC-009. | SEFAUD-PHASE-002 | [Phase 001](phases/plan-phase-001.md) |
| SEFAUD-PHASE-002 | Prove complete durable ownership and integrity. | Persistence layer | SEFAUD-PHASE-001, SEFAUD-REQ-002, SEFAUD-REQ-003, EXT-001 | SEFAUD-REQ-006 | Security foundations and durable-owner inventory accepted. | All stores and cross-store invariants pass normal/fault/migration/recovery matrix; no known integrity defect. | SEFAUD-PHASE-003 | [Phase 002](phases/plan-phase-002.md) |
| SEFAUD-PHASE-003 | Complete every executable action and unavailable negative contract. | Command policy kernel | SEFAUD-PHASE-002, SEFAUD-REQ-006, EXT-001, EXT-002 | SEFAUD-REQ-004 | Signed 000 through 002 history preserved; exact remaining evidence joins selected by P003-TASK-010. | Universal action dimensions, feedback/store/audit joins, authentic authority/provider prerequisites and capture self-tests pass; no known command or discovered prerequisite defect. | SEFAUD-PHASE-004 | [Phase 003](phases/plan-phase-003.md) |
| SEFAUD-PHASE-004 | Complete UI polish, accessibility and privacy proof. | GUI presentation | SEFAUD-PHASE-003, SEFAUD-REQ-004, EXT-001 | SEFAUD-REQ-005 | Command semantics and Phase 003 capture stable and integrated. | Every graphical/text surface passes layout, input, narration, state, privacy, revocation and fallback evidence on the portable client fixture. | SEFAUD-PHASE-005 | [Phase 004](phases/plan-phase-004.md) |
| SEFAUD-PHASE-005 | Close lifecycle, providers and channel convergence. | Lifecycle integration boundary | SEFAUD-PHASE-004, SEFAUD-REQ-005, EXT-001, EXT-002 | SEFAUD-REQ-007 | Security, persistence, command and UI contracts integrated with valid affected proof. | Full lifecycle/provider/partial-failure/thread/classloading/reconnect matrix passes with exact outcomes and no known integration defect. | SEFAUD-PHASE-006 | [Phase 005](phases/plan-phase-005.md) |
| SEFAUD-PHASE-006 | Deliver missing quality checks and prove one final clean candidate. | Release verification system | SEFAUD-PHASE-005, SEFAUD-REQ-007, EXT-001, EXT-002 | SEFAUD-REQ-008 | Change-stage work integrated; remaining quality enablement identified before final freeze. | Maintained checks and full final automated/runtime/UI/provider/native/recovery/performance/artifact suite pass after last product change, including earlier boundary regressions. | SEFAUD-PHASE-007 | [Phase 006](phases/plan-phase-006.md) |
| SEFAUD-PHASE-007 | Close documentation, integration and release readiness. | Documentation evidence | SEFAUD-PHASE-006, SEFAUD-REQ-008, EXT-001, EXT-002 | SEFAUD-REQ-009 | Final product artifact and Phase 006 proof complete and immutable for closure. | Documentation/evidence parity, final clean checkout, PR integration, resulting candidate verification, signed tag, cleanup and all plan-wide endpoint gates pass. | Final plan-wide completion | [Phase 007](phases/plan-phase-007.md) |

### Frozen phase briefs and task identity

| Phase | Stable tasks | Detailed authoring brief |
|---|---|---|
| 000 | P000-TASK-001 through P000-TASK-014 | Preserve delivered baseline work and original task identities. Explain inventory provenance, commands/permissions/UI/stores/lifecycle/trust/dependencies/docs ownership, deterministic joins, original doctor/test diagnostics, exact cleanup and signed completion. No new capture dependency on Phase 003; fresh aggregate changes belong to current Phase 003. |
| 001 | P001-TASK-001 through P001-TASK-014 | Preserve delivered authority, redaction, payload, filesystem, native and dependency controls. Blueprint all original inspection and adversarial proof, with current native analysis fidelity and per-advisory predicates. State which later changes invalidate rows and route current repair/regression without rewinding history. |
| 002 | P002-TASK-001 through P002-TASK-012 | Preserve complete durable-owner and shared primitive audit, fault/migration/rollback/recovery evidence and signed integration. Keep every nonrepository owner, sensitive writer and cross-store protocol covered. Current command/store joins and changed writers trigger focused renewed evidence, not a new foundational implementation. |
| 003 | P003-TASK-001 through P003-TASK-015 | Preserve prior catalog/policy/source/target/domain work. P003-TASK-010 retains feedback/audit/redaction and native-writer joins, first reconciling task 310/317 inputs; its necessary prerequisite subwork delivers missing shared capture and authentic authorization/provider fixtures before dependent evidence. Then complete every action's unproven dimensions, unavailable negatives, documentation and tasks 011 through 015 in their existing order. Shared callbacks only group identical invariants. Resolve any current security, storage or advisory finding needed for exit on this phase branch. |
| 004 | P004-TASK-001 through P004-TASK-010 | Enumerate every actual screen/HUD/feedback state, polish failed criteria, then prove scales/resolutions, keyboard, narration, resize, InvSee/Fancy Tags, privacy, stale selection and fallback equivalence using integrated command semantics and capture. Keep source/project conventions and avoid unrelated redesign. |
| 005 | P005-TASK-001 through P005-TASK-014 | Test service lifecycle, revision invalidation, channel convergence, adapters and native loading, thread/class safety, queued work, partial failure and recovery. Diagnose exact provider lifecycle rather than assuming its login error is a permission defect. Include logout, reconnect, dimension, shutdown and restart, with client evidence only for residual client claims. |
| 006 | P006-TASK-001 through P006-TASK-015 | Make missing formatter/warning/static/risk-coverage checks an explicit early prerequisite within the existing quality-gate tasks, then freeze the candidate. Execute complete clean build/fallback/reference/GameTest/runtime/provider/UI/admission/disguise/packet/store/performance/native/dependency/artifact matrices. Recheck earlier security and storage boundaries with Phase 003 capture. Any repair restarts affected proof and the complete final suite after the last product change. |
| 007 | P007-TASK-001 through P007-TASK-016 | Freeze closure identities, reconcile every documentation/evidence claim, replay support/operations, audit all requirement joins and false-completion risks, verify final clean checkout/artifact parity, integrate through PR, verify resulting candidate branch, sign the final phase tag, synchronize approved docs/tracking and close the exact endpoint. Never publish. |

The detailed files retain existing task IDs and meanings; new prerequisite subwork refines its owning task rather than renumbering or repurposing unrelated tasks. They declare exact local stimuli, assertions, limits, evidence paths, invalidation and teardown. They cannot defer design to another planning pass.

Read master, manifest, every registered plan, immutable goal and valid separate cursor before execution. The missing cursor at intake is a legacy state compatibility issue, not a plan-validation failure or permission to replace a goal. This plan does not create state files. When a valid cursor is present, advance it only one contiguous phase using the protected transition helper after implementation, evidence, review, required checks, GitHub merge, resulting candidate verification and signed phase tag pass. Preserve historical branches/tags. No stacked future branch, skipped phase or direct integration push is allowed.

## 14. Verification Strategy

### Execution Hosts

| Workload or gate | Execution host | Required capabilities and launch configuration | Candidate identity and runtime directory | Evidence |
|---|---|---|---|---|
| Build, format, analysis, units, generation, artifact review | Verified node-1 headless compute | Java 21 and checked-in wrapper. Inspect configured dependencies/task graph before launch; no indirect client or renderer. | Reuse current checkout; discover and register exact nested disposable worktree/output only if necessary. | Command, task graph, revision, inputs, reports and cleanup. |
| Dedicated server, real-path server GameTests, provider/native/storage fixtures | node-1 | Verified no-GUI server task, authorized console, synthetic data and private connectivity. No client/display/renderer. | Record exact runtime under verified project anchor, artifact/dependencies/config/fixture and EULA readback. | Startup readiness, actual entry point, assertions, native/provider observations, shutdown and cleanup. |
| Graphics, input, narration, animation and desktop interactions | Owner Linux laptop envision | Discover actual host, active desktop/control connection and discrete GPU; confirm actual renderer. Isolated instance, prelaunch zero audio and exact application-stream mute. | Discover laptop project anchor and runtime independently; never infer from node-1 path. Match candidate and hashes. | Client receipt/input/rendering or captured inaudible narration, window/process/stream identity, limits and cleanup. |
| Real in-world synchronization/mixed clients | Laptop client and node-1 dedicated server | Exact private endpoint, ready server, supported automatic direct-connect or authorized desktop controls, correct player/world confirmed on both sides. | One manifest binds both runtimes, dependency/config identities and actual joined sessions. | Console fixture and server result plus actual client interaction/receipt/rendering/reconnect. |
| Native/provider branch analysis | node-1 source/artifact review and available Linux native fixtures | Actual implementation and API/ABI inspection, installed library provenance, synthetic filesystem cases and retained proof checks. | Fingerprints distinguish branch analysis from historical/new execution. | DEC-009 evidence with honest foreign-runtime limits; no additional OS host prerequisite. |

Never launch a Minecraft client on `node-1`, including Xvfb, virtual displays, VNC, X11 forwarding, offscreen mode, software rendering, llvmpipe or integrated GPU. A task's name is not proof of being headless. Server-observable claims use console/logs/real server paths without asking the owner to join. Server-only simulated players prove only those server paths.

For every laptop launch, set the isolated pinned-version master audio option to zero before process creation. After the owned window appears, use `hyprctl clients -j` to record exact address, class, title and PID, correlate only that process tree to PipeWire/PulseAudio, mute only its stream with `wpctl` or `pactl`, and verify muted state before acceptance. Recheck after resource reload, device changes, reconnect and stream recreation; immediately mute replacements. If identity, renderer or mute cannot be proven, stop the owned client and leave only its exact gate unverified. Never mute system output, default sink, microphone, another application or personal instance. Narration/audio tests use an inaudible capture route. Teardown removes the owned watcher/route and verifies the stream disappeared.

For in-world client work, resolve both directories and private endpoint, register cleanup, configure and read back `eula=true`, start the no-GUI server and verify readiness. Inspect pinned launch options for automatic connection; otherwise use authorized desktop controls. Verify the intended player entered the correct world on both sides before assertions. Console fixtures must not bypass the authority or gameplay under test. Use multiple real clients only for claims that require them. Do not expose ports, alter firewall/authentication, copy credentials, or modify personal server lists.

Singleplayer is permitted only for an identified integrated-server/singleplayer bug, with requirement/bug ID and reproduction rationale, on the laptop under the same isolation rules. Missing connectivity or control never authorizes that fallback. Startup/menu checks need no world. EULA consent is already granted for exact disposable servers; configure missing or false acceptance files and verify them without asking again.

### Evidence Coverage

| Requirement | Unit/static | Integration | Real behavior | Negative/security | Artifact/runtime |
|---|---|---|---|---|---|
| SEFAUD-REQ-001 | Inventory/schema and drift | Dispatcher/catalog/reference joins | Baseline and current aggregate capture | Missing/stale/duplicate/unowned inputs | Candidate and cleanup manifests |
| SEFAUD-REQ-002 | Policy/redaction/codec/path | Indirect routes, native identity | Actual allowed/denied multi-actor paths | Replay, leaks, object swap, malformed data | Source-to-sink and Linux native packet |
| SEFAUD-REQ-003 | Graph/API/ABI/advisory | Platform supply and optional adapters | Installed runtime and native load | Reachability, SNI conditions, duplicate runtime | EXT-002 provenance and all-alert ledger |
| SEFAUD-REQ-004 | Dispatcher/policy/domain | Every action's domain and store joins | Actual effects/restart/feedback/audit | Sources, targets, invalid args, denial/failure | Complete dimension matrix and references |
| SEFAUD-REQ-005 | Layout/session/workflow | GUI/fallback equivalence | Laptop input/rendering/narration/resize | Private/stale/revoked/unavailable state | Targeted captures and UI ledger |
| SEFAUD-REQ-006 | Schema/atomic/worker | Concurrency/fault/cross-store | Interruption/migration/recovery/restart | Corrupt, unsafe path, unknown outcome | State hashes, receipts and native proof |
| SEFAUD-REQ-007 | Lifecycle/thread/adapter | Sequence and partial-failure matrix | Login/reload/reconnect/dimension/stop | Provider outage/stale state/class leak | Correlation and classloading results |
| SEFAUD-REQ-008 | Maintained quality gates | Complete final regression | Full dedicated/client/provider suite | Packet abuse/recovery/security regressions | Clean candidate/JAR/dependency/performance |
| SEFAUD-REQ-009 | Docs/link/drift | All matrix and phase joins | Operator support replay | False completion and sensitive evidence | Final artifact, PR, resulting branch and tag |

Use actual configured tasks. Phase 006 first implements missing quality gates and verifies their failures are meaningful, then runs `./gradlew check build compileFallbackRuntimeJava generateProjectReferences generatePerformanceReport` from the clean candidate. Discover the repository's configured server GameTest task and inspect its graph before executing all required tests. Do not invent a task name or claim a task ran because a report file exists. Record exact commands and decisive reports in completion evidence.

Performance proof binds deterministic metadata budgets and actual tick, memory, queue, scan, payload, persistence and rendering behavior. Before comparing, record fixture cardinalities, JVM/host and baseline. Exercise empty/normal/declared-limit/over-limit inputs and repeated reload/reconnect/flush. Require bounded configured queues and payloads, no monotonic retained sessions or workers after cleanup, no unbounded per-tick scans or log streams, and no unexplained regression beyond repository-pinned budgets. For hot paths lacking a numeric repository budget, the owning phase commits a measured baseline and justified threshold before final candidate freeze; final results cannot set their own pass threshold. Diagnostics has fixed numeric budgets below.

### Cleanup and retained evidence

Before every test, verification build, check or audit, identify exact owned resources and pre-existing content and register teardown for success, failure, timeout, cancellation and interruption. Read the workspace policy before allocation/removal. Reuse existing checkout; necessary actual runtimes/worktrees belong inside each host's verified project anchor with parent Git/build/index/package exclusions. Private planning drafts are not product clones.

Keep files only through the bounded suite's last consumer. Preserve required sanitized evidence and requested artifacts once outside scratch, verify readability, then stop exact owned processes and confirm exit. Remove only rechecked owned runtime/world/log/crash/config/download/database/fixture/trace/coverage/screenshot/bytecode/test-output paths, without symlink traversal. Never blanket-clean Git, broadly kill processes, remove shared caches, erase source/tracked fixtures or personal data, or force-remove a worktree. Use Git-aware worktree removal after active-use checks and preserve branches/tags. Confirm paths and processes are gone on each host. A read-only check reports that no resources were created. Cleanup failure names exact leftovers and keeps the workflow cleanup-incomplete until reconciled.

## Diagnostics and Debugging

**Requirement IDs:** SEFAUD-REQ-004, SEFAUD-REQ-002, SEFAUD-REQ-005, SEFAUD-REQ-006, SEFAUD-REQ-007, SEFAUD-REQ-008, SEFAUD-REQ-009
**Task IDs:** P003-TASK-010, P004-TASK-001, P005-TASK-001, P006-TASK-001, P007-TASK-001
**Controls:** Planned SEFAUD-IF-004 uses `/sef debug on <category> <target> <seconds> <sample_limit>`, `/sef debug status`, and `/sef debug off`; dedicated console omits the slash and needs no player. Player control requires explicit `sef.commands.sef.debug`, default denied, with current target authorization. Off is idempotent; absent, unknown or removed targets reject or stop.
**Signals:** Version 2 typed capture/event schema below joins candidate, side, operation, expected/actual state, decision/reason, revision, duration, queues and limits across command, security, dependency, storage, GUI, network, lifecycle and performance observations.
**Collection procedure:** Follow the numbered local runbook below; status reports exact capture output location and bounds. Historical Phases 000 through 002 use existing doctors/test reports and do not call these absent controls.
**Headless verification:** Use real dispatcher console/player-source fixtures, existing unit/dispatcher tests and inspected server-only GameTest task, then owned dedicated console. Assert authority, domain/store/audit joins, capture bounds/reset/redaction and no change to gameplay outcomes.
**Client verification:** Use the silent laptop only for real command-tree projection, client receipt, screens/HUD/input/narration/animation and synchronization/reconnect claims; join logs to targeted visuals where presentation requires them.
**Client audio isolation:** Set the disposable instance's master audio to zero before launch as the prelaunch layer. Bind exact address/class/title/PID with `hyprctl clients -j`, correlate only owned descendants to PipeWire/PulseAudio, and verify per-application mute before acceptance. On owned stream recreation, immediately mute and verify every replacement stream. Stop the client if identity or mute fails. At cleanup, stop the owned watcher and client, remove temporary routing, and verify client and stream exit; never touch global or unrelated audio.
**Budgets and privacy:** Default off, one active server capture, 60-second default and 300-second maximum, 1,000-event default and 10,000-event maximum, 4 MiB total sanitized output, 100 accepted events/second and 20/tick maximum, at most 1 ms aggregate collection/tick. Drop with counters instead of blocking; no disabled event allocation or file/network I/O.
**Regression and support:** P003-TASK-010 verifies controls and delivers the exact support procedure in `DOCUMENTATION.md`, linked from `README.md` and generated command/permission references. Later phases test their local observations; final Phase 006 reruns earlier boundaries and Phase 007 replays support against the packaged artifact.

Existing `/sef doctor`, subsystem doctors and repository health are observed source capabilities. Their actual permissions and exact command syntax are read from the live dispatcher before use; their presence is not evidence that a denied player can invoke them. The capture commands above are proposed until Phase 003 implements and verifies them. They do not grant test privileges, change authentication, bypass policy, alter game outcomes or replace mandatory audit.

Categories are `command`, `security`, `dependency`, `storage`, `gui`, `network`, `lifecycle`, `performance`. Target syntax is `action:<canonical-id>`, `actor:<synthetic-or-authorized-uuid>`, or `component:<registered-id>`, validated against category and source authorization. Server capture contains server-owned state only. Client observations use local authorized bounded logs and inaudible capture; a server command never requests arbitrary private client state. One active capture rejects another enable with `busy`.

CaptureStatus fields are schema_version:int, enabled:boolean, capture_id:opaque string or absent while off, side:enum, category:enum, sanitized_target:string, remaining_ms:nonnegative long, accepted/dropped/truncated:nonnegative counters, remaining_events:int, remaining_bytes:long, output:owned relative locator, and stop_reason:enum. Events include those identities plus sequence:long, timestamp:UTC, tick:optional long, correlation_id:string, component/action/source enums, actor/effective_actor pseudonyms, decision/reason enums, expected/observed revision, duration_ms:number and typed optional subsystem fields. Absent fields mean not observed/not applicable, never zero or successful by default. Capture manifest binds full artifact and environment identity once; events reference it.

Use an owned safe runtime directory `logs/sef/diagnostics/<capture_id>/` for bounded sanitized `events.jsonl` and `manifest.json`. This is a planned delivered sink, not an invented existing output. Resolve the runtime and report relative location; ordinary players never see private absolute paths. Validate ownership and safe opened-object writing using the existing native boundary. Background I/O uses immutable sanitized snapshots and a bounded queue; never inspect unsafe live game state off-thread. Sink failure stops optional capture with a safe error while mandatory audit keeps its own fail-closed semantics. Stop on expiry, target removal/disconnect, reload, shutdown and restart; capture never resumes from persisted enablement. Status remains available after stop for bounded counters and reason.

| Signal | Source and unit | Expected observation |
|---|---|---|
| `capture_id, schema_version, sequence, correlation_id` | Capture service and operation context, opaque IDs/integers | Unique capture, monotonic sequence, shared operation join; no clock assumption. |
| `action_id, source, actor, effective_actor, decision, reason_code` | Server command/policy owner, stable IDs/enums/pseudonyms | Exact action and actors; denial before effect; no raw restricted arguments. |
| `expected_revision, observed_revision, provider_state` | Server permission/config/session owners, integers/enums | Revocation, stale tree or provider failure distinguishable; no outage grant. |
| `store_id, operation_id, schema, publication_stage, durable_outcome, queue_depth` | Server snapshot and persistence worker, IDs/enums/count | Exact commit or recovery, no duplicate side effect, bounded queue. |
| `provider_id, api_version, identity_result, append_state, flush_state` | Native writer/runtime supply boundary, IDs/enums | Same opened object; unsafe or unavailable metadata fails closed. |
| `screen_id, selection_revision, focus_id, presentation_state` | Server projection and separately captured client presentation | Receipt and actual display distinguished; stale/private state absent. |
| `session_id, sequence, direction, payload_bytes, rejection_reason` | Network codec/session owner, pseudonym/count/bytes/enums | Replay/oversize/stale rejects before mutation, bounded resync. |
| `lifecycle_stage, thread_class, outstanding, recovery_state` | Lifecycle/worker owners, enums/count | Correct side/thread, settled or explicitly recoverable work. |
| `duration_ms, tick_ms, capture_work_ms, dropped_events, truncated_events` | Monotonic timer and capture counters, milliseconds/count | Normal limits hold; saturation explicitly counted; no hidden stall or log growth. |

1. Record requirement/task, candidate/artifact, input fingerprints and exact test-owned runtime/capture/output paths. Register teardown and synthetic actors. Inspect actual dispatcher and test graph. For a server set/read back `eula=true` and verify readiness; for client work follow the host/audio/join procedure. If capture is absent, complete P003-TASK-010 enablement before its dependent capture.
2. Record baseline doctor/health and domain state. For example, from owned console run `sef debug on command action:<verified-catalog-id> 60 1000`, then `sef debug status`. Verify permission/scope, capture ID, relative output, expiry and limits. Substitute the action ID resolved from the current catalog, not an invented action. Unauthorized enable must fail; console needs no connected owner.
3. Perform the real selected action, denial, domain failure or recovery stimulus through its authentic entry point. Capture before/after effects and terminal audit alongside diagnostic decisions. Use client actions only for the stated residual claim; verify mute first.
4. Inspect the exact resolved `events.jsonl` with `jq -c 'select(.capture_id == "<returned-id>" and .correlation_id == "<operation-id>")'` and join its manifest to command/store/audit/client records. Assert precise expected sequence, revisions, one terminal result, unchanged protected state on denial, correct commit/recovery and truthful counters. A missing field or event leaves its claim unproven.
5. Run `sef debug off`, then status, repeat off, and issue a matching stimulus to prove capture stopped. Separately test timeout, reload, removed target, shutdown/restart, malformed limits, output/rate saturation and sink failure. Compare diagnostics-off/on action results and bounded overhead; no capture-induced gameplay mutation is allowed.
6. Retain one sanitized support packet: project/loader/JVM/artifact identities, minimal relevant nonsecret configuration, fixture/steps, expected/actual, capture/correlation/window, decisive bounded server and relevant client excerpts, test summary, limits and disposition. Review for secrets, private chat/addresses/paths and unrelated records. No automatic external upload.
7. Stop and verify exact owned client, watcher, stream, server and workers; remove captures/runtime/scratch after the final consumer while preserving reusable diagnostics and required proof. Confirm cleanup on both hosts and record any exact leftover as incomplete.

Illustrative event, not executed evidence:

```json
{"schema_version":2,"capture_id":"capture_example","sequence":1,"correlation_id":"operation_example","side":"server","category":"command","component":"command_execution","action_id":"fixture.action","actor":"fixture_actor_1","decision":"rejected","reason_code":"permission_denied","expected_revision":7,"observed_revision":7,"duration_ms":0.2,"queue_depth":0,"dropped_events":0,"truncated_events":0}
```

## 15. Compatibility, Migration, Rollout, and Recovery

### Compatibility and Versioning

Preserve Minecraft 1.21.1, NeoForge 21.1.235, Java 21, Gradle Wrapper 8.8, Parchment 2024.11.17, mod id `sef`, and current public protocol/integration contracts. Do not introduce Fabric or legacy Forge APIs. The universal JAR remains optional on clients; enhanced disabled, matching enhanced, no-SEF fallback and incompatible-protocol states keep their documented behavior. All three desktop OS targets remain supported under the single DEC-009 evidence contract.

Preserve canonical actions, permission IDs, configuration fields, resource locations, wire records and schema owners. A necessary defect repair affecting serialization or configuration uses the smallest compatible versioned change with bounded validation and migration/rollback proof. Unsupported newer data fails explicitly. Unknown fields are preserved only where their actual owner contract permits them.

LuckPerms API 5.4, FTB Essentials, Curios and declared bridges remain optional and runtime guarded. Phase 003 first isolates the exact 21.1.235/5.4.140 login failure: capture provider initialization and capability availability, synthetic identity/world validity, actual exception origin and lifecycle order, then compare a provider-only disposable fixture and a matching SEF fixture. Preserve pins and select only a demonstrated compatible NeoForge artifact if the fixture artifact is wrong. A confirmed SEF defect receives an in-scope fix/regression; a fixture defect is repaired without attributing it to SEF. Authentic permissions, client tree refresh and reconnect are proved after login works. No operator-wide grant or authentication weakening is permitted.

### Migration and Recovery

No broad migration is requested. For a repair that changes durable format, bind original identity/version/revision, stage bounded output, validate the entire result, retain an exact recovery copy, refuse conflicting backups and restore on failure. Exercise forward migration and rollback from synthetic fixtures. Retain the prior approved artifact and necessary synthetic recovery inputs through their last verification consumer; remove disposable copies afterward. No irreversible production point of no return is authorized.

Partial game/store/cost/audit/provider outcomes use explicit receipts, journals, idempotency or compensation at the actual boundary. Do not retry unknown outcomes blindly. Missing native identity capability rejects the affected operation and preserves state; unsupported foreign-runtime proof is not fabricated.

### Sequential Integration

Use the verified `master` candidate integration lineage under DEC-006. After each current phase satisfies implementation, proof, documentation and cleanup, use its existing `envy/` branch and matching milestone. Commit and push with the owner's identity and registered SSH signing key, create/update the authorized phase PR, run all required checks and private independent review where the repository provides it, and resolve actionable findings. Record an unavailable review capability explicitly; it never waives deterministic checks.

Integrate with GitHub's merge-commit function. Pending checks or queued auto merge are not merged. Do not bypass required checks, push integration directly, or substitute squash/rebase without owner authority. Verify the resulting approved branch contains the merge, rerun required resulting-commit checks, and create/push a signed annotated phase tag on that merge commit. Only then advance the valid cursor and create the next contiguous phase branch from fetched approved history. Historical tags/branches remain. If repository settings prevent required merge method or signing fails, report the concrete restriction.

Rollback of an unmerged defect uses a focused corrective change without discarding unrelated work. Post-integration rollback uses the known signed prior state and validated recovery procedure through normal PR workflow, never history erasure. Publication and production deployment remain outside this plan.

## 16. Documentation, Operations, and Release Gates

Update `README.md` and `DOCUMENTATION.md` for actual behavior, commands, permissions, configuration, architecture, failure and recovery changes. Maintain `docs/README.md` navigation when documentation paths change. Reconcile command/permission/configuration references with the live dispatcher and catalog, and update affected compatibility, installation, security, troubleshooting, migration, acceptance, performance and release documents. Counts must name their candidate and dimension.

Document the delivered debug procedure in `DOCUMENTATION.md`: exact enable/status/off syntax, authorization, target discovery, output location, limits, safe failures, collection, redaction, retention and cleanup. Keep default-off support capability in the artifact. Do not advertise unavailable families, interpret analysis as foreign execution, claim Windows unsupported, repeat stale 26-alert state, or call a denied doctor invocation a successful command.

Every phase completion packet uses SEFAUD-IF-006 and names source and integration commits, artifact identities, exact checks/results, runtime/analysis/reuse evidence, invalidation, documentation, issue/milestone/PR state, review disposition, merge/signature/tag and cleanup. The plan and immutable goal are not status diaries.

Prepare wiki changes from tracked verified documentation and publish only after the corresponding merge, so it never advertises unmerged behavior. Synchronize linked issues, PRs, project fields and milestones with actual in-review/merged state. Do not close unsatisfied work or imply a production release.

The final JAR receives SHA-256 and SHA-512, full entry/dependency/license/provenance/native-duplicate inspection and source binding. Resolve aggregate matrix contradictions, stale counts, raw secrets, private paths, incidental build/cache output and unrelated diffs before closure. Retain one readable sanitized evidence set and requested artifact at intended destinations. No marketplace upload, production mutation, credential operation, billing or public release is authorized.

## 17. Risks and Failure Boundaries

The catalog distinguishes observed uncertainty from plausible failures. Each risk has prevention, a decisive detection signal, safe recovery and owned proof. Local phase risks may refine these cases without inventing a new product requirement.

| Risk ID and causal scenario | Affected requirements or interfaces | Likelihood and impact rationale | Prevention | Detection signals | Recovery | Owning phase and tasks | Required proof |
|---|---|---|---|---|---|---|---|
| RISK-001. Inventory omission or incompatible evidence joins create false passes or erase valid observations. | REQ-001, REQ-004, REQ-009; IF-001 | Observed task 310/317 disagreement; high completion impact. | Deterministic surface ownership and complete candidate-compatible input set. | Missing/duplicate owner, dropped input, count and per-dimension mismatch. | Repair aggregate semantics and regenerate; preserve underlying compatible proof. | Phase 000 retained P000-TASK-001 through P000-TASK-014; current P003-TASK-010; P007-TASK-001 through P007-TASK-016. | Inject missing, stale and duplicate evidence; generator rejects false closure and explains both historical matrices. |
| RISK-002. Indirect route or observation exposes authority/private data. | REQ-002, REQ-004; IF-002, IF-004 | Many aliases, projections and actors increase plausible bypass paths; impact is unauthorized effect or disclosure. | Recheck current authority at mutation; central redaction and mandatory audit separation. | Wrong actor/revision/reason, protected-state change after denial, sensitive sink marker. | Fail closed, repair route/sanitizer and rerun affected channels. | Phase 001 P001-TASK-001 through P001-TASK-014; current P003-TASK-010 and remaining command proof. | Forged/stale/revoked/nested requests and synthetic secret markers cause no protected mutation or disclosure; one correct terminal audit. |
| RISK-003. Platform ownership hides applicable dependency exposure or duplicate native supply. | REQ-003, REQ-008; IF-005 | Observed expanded 28-alert set and unresolved SNI reachability; potentially critical. | Per-advisory graph, artifact, installed runtime, API/config and provenance predicates. | Disposition lacks affected conditions; duplicate classes; mismatched runtime/API hash. | Compatible mod-owned remedy; retain defensible upstream disposition; escalate only proven pin conflict. | Phase 001 P001-TASK-001 through P001-TASK-014 retained foundations; current P003-TASK-010 prerequisite delta; Phase 006 P006-TASK-001 through P006-TASK-015 final refresh. | All 28 records including 28/27, actual SNI pipeline analysis, rejected unsupported dispositions and final native duplicate inspection. |
| RISK-004. Native ABI or opened-object assumptions permit substitution or linkage failure. | REQ-002, REQ-006, REQ-007; IF-005 | Native calls cross static-graph coverage and platform branches; severity high even when probability unknown. | Exact API/ABI/calling convention review, object identity ownership and no path-only fallback. | Identity/type/link/reparse mismatch, metadata unavailable, linkage or flush error. | Fail affected write closed, preserve state, repair boundary and renew changed proof. | Phase 001 and 002 retained native/storage tasks; P003-TASK-010 sink join; Phase 005 P005-TASK-001 through P005-TASK-014. | Linux substitution/link/metadata/write/flush/rotation/restart fixtures plus current branch/API/artifact review and scoped retained proof, honestly labeled under DEC-009. |
| RISK-005. Ambiguous durable order duplicates effects, or destructive fixtures escape ownership. | REQ-006; IF-003 | Crash and concurrency cut points are credible; impact is loss or duplication. | Owned synthetic paths, snapshots, explicit receipts/journals and ordered publication. | Unknown outcome, divergent hashes/references, unexpected write outside fixture, unsettled queue. | Restore validated state or resolve durable receipt before retry; quarantine owned unsafe evidence. | Phase 002 P002-TASK-001 through P002-TASK-012; current command/store deltas; final Phase 006 recovery tasks. | Terminate at each distinct commit boundary, corrupt inputs and race writers; exactly-once state and unrelated-data preservation survive restart. |
| RISK-006. Shared callback tests or screenshots substitute for actual command/UI behavior. | REQ-004, REQ-005; IF-001, IF-002 | Observed hundreds of partial command rows and unproven client claims. | Per-action dimensions, actual entry points and separate state/receipt/presentation assertions. | Missing domain effect/failure/store/audit join, clipped controls, focus/narration/stale-state failure. | Add precise fixture, repair confirmed defect and rerun affected workflow. | P003-TASK-010 through P003-TASK-015; P004-TASK-001 through P004-TASK-010. | Every executable row closes domain dimensions; every screen/state passes actual laptop interaction and accessibility with matching server effect. |
| RISK-007. Provider login, lifecycle or channel failures broaden authority or lose work. | REQ-007, REQ-004; IF-002, IF-003, IF-004 | Exact 21.1.235/5.4.140 failure and rejected doctor discovery observed; root cause unknown. | Diagnose authentic fixture/provider lifecycle before changes; fail closed; current revisions and bounded queue ownership. | Capability/login stage, command-tree/server decision divergence, stale provider/session, unsettled work. | Repair compatible fixture or confirmed in-scope defect; restore provider/fallback and reauthenticate without weakening policy. | P003-TASK-010 prerequisite fixtures; P005-TASK-001 through P005-TASK-014. | Provider-only and matching SEF comparison, grants/denies/revocation/refresh/outage/reconnect, partial failure and shutdown show exact safe outcomes. |
| RISK-008. Late change or wrong integration branch invalidates final proof. | REQ-008, REQ-009; IF-001, IF-006 | Legacy default differs from candidate master; future fixes can stale evidence. | Verify lineage and all fingerprints, sequential PR integration, final freeze after quality enablement. | Wrong merge target, unmatched JAR, pending check/tag, changed source/fixture, stale matrix. | Stop integration; correct focused branch, rerun invalidated proof and complete final suite after last product change. | P006-TASK-001 through P006-TASK-015; P007-TASK-001 through P007-TASK-016. | False-completion checks reject wrong lineage, stale proof, queued merge and missing tag; final clean artifact and resulting branch verify. |
| RISK-009. Host/control/audio error or leaked test resources harms the owner environment. | REQ-001, REQ-005, REQ-008; IF-006 | Old rendering packet lacks current mute proof; clients and fault fixtures need exact isolation. | Verify host/renderer/window/process/stream, prelaunch zero audio, registered exact teardown. | Unmatched or recreated unmuted stream, forbidden renderer, leftover owned process/path. | Stop owned client, retain exact gate unverified; reconcile owned leftovers before next run. | P000-TASK-001 through P000-TASK-014 resource rules; P004-TASK-001 through P004-TASK-010; P006-TASK-001 through P006-TASK-015. | Preflight and resource-recreation checks enforce inaudibility; teardown on success/failure/interruption confirms exit and exact path removal. |

All shortened REQ/IF references in this table mean the corresponding SEFAUD-REQ/SEFAUD-IF IDs. Capture failure, saturation and leak risks use RISK-002, RISK-006 and RISK-009; P003-TASK-010 owns their control self-tests. Disabled capture must avoid event allocation and I/O, enabled capture must stay within fixed budgets, and normal gameplay outcomes must match with capture off/on. An old control cannot be assumed to expose a newly required diagnostic signal.

## 18. Definition of Done

1. Every acceptance criterion and required evidence item for SEFAUD-REQ-001 through SEFAUD-REQ-009 passes at the final candidate, using explicitly valid historical proof only where allowed and fresh final verification where required.
2. All contiguous phase exits, PR integrations, resulting candidate-branch verification and signed annotated phase tags are complete. Historical 000 through 002 records remain intact; the final endpoint is not one phase's success.
3. Inventories and all matrix dimensions have no missing, duplicate, stale or unowned mandatory row. The task 310/317 disagreement is resolved with input-level provenance.
4. Every executable administrator action has full allowed/denied/source/target/argument/effect/failure/store/restart/feedback/audit/route proof. All sixteen unavailable families remain unavailable, unreachable, side-effect-free and negatively tested.
5. Every UI/HUD/text surface passes layout, mouse/keyboard, focus, narration, state, privacy, revocation and fallback gates using actual silent laptop evidence for client claims.
6. All durable owners and cross-store invariants pass bounds/schema/atomicity/concurrency/interruption/migration/rollback/recovery/flush/retention/privacy proof. Backend lifecycle, providers, revisions, channels, partial failure, side/thread and shutdown pass.
7. Every current advisory has its candidate-specific disposition. No known applicable critical/high exploitable mod-owned or repository vulnerability, bypass, leak, unintended authority, executable command defect, UI-blocking defect, integrity defect or mandatory backend defect remains.
8. Native source/API/ABI/artifact analysis, compatible retained proof and actual Linux runtime fixtures meet DEC-009. JNA is compile only and compatibly supplied by pinned NeoForge; the JAR embeds no duplicate runtime. Foreign execution is not claimed or required.
9. Missing quality gates are delivered before the final freeze. The complete clean-checkout automated/GameTest/server/client/provider/UI/packet/recovery/performance/dependency/artifact suite passes after the last product change, including earlier security/storage regressions.
10. Diagnostics is delivered by Phase 003 before its consumers, default off, authorized, bounded, redacted, nonmutating, reset-safe and measured. Packaged-artifact support instructions are replayed; original historical proof has no backward capture dependency.
11. Every launched test client remains inaudible through exact application-stream verification from startup to exit. Every test/build/check/audit completes owned-resource cleanup on all used hosts; required sanitized evidence and requested deliverables remain readable.
12. Documentation, generated references, compatibility/support claims, security/test/acceptance and release-readiness records match the final artifact. EXT-001 and EXT-002 are complete available in-plan closure packets, not assumed external blockers.
13. No failed, blocked, incomplete, unexecuted, stale, analysis-only or lower-fidelity row is mislabeled passed. A missing capability leaves only its precise mandatory claim incomplete and independent work continues.
14. Optional/future scope remains excluded. No production data, credentials, production mutation, public release, marketplace publication or platform upgrade occurs.
15. The immutable saved goal remains unchanged. Execution state follows its separate valid cursor and protected transitions; planning does not create a replacement goal.

**Completion endpoint:** At one frozen candidate revision and artifact, close every mandatory audit and remediation matrix, repair confirmed in-scope defects, pass required final verification and integration, reconcile documentation, and record release readiness without publication or production mutation.

## 19. Goal Creator Handoff

This is authoring intake for continuity, not invocation authority, an execution ledger, or permission to edit the saved goal/cursor. The observed missing cursor is recorded faithfully; this plan neither creates it nor makes a replacement goal a prerequisite for plan validation.

```text
Mandatory boundary: SEFAUD-REQ-001 through SEFAUD-REQ-009, including repair and real-boundary regression for every confirmed in-scope defect.
Optional/future disposition: excluded
Locked owner decisions: DEC-001 through DEC-009, with exact choices in Section 9.
Active phase: SEFAUD-PHASE-003
Active phase plan: phases/plan-phase-003.md
Next executable action: P003-TASK-010 first reconciles the 729-row task 310 and task 317 evidence inputs, then delivers required capture and authentic authorization/provider fixture prerequisites before remaining feedback, effect, persistence, audit and sink joins.
Known failing checks: Task 310 and task 317 disagree; universal action dimensions remain open. Protected doctor was rejected before transmission. LuckPerms NeoForge 5.4.140 login failed on exact NeoForge 21.1.235 with capability initialization and invalid player data errors. UI, mixed/fallback, InvSee, admission, disguise, reconnect, lifecycle, quality-gate, final regression, refreshed 28-advisory and final artifact/documentation evidence remain incomplete.
Known external blockers: none
Completion endpoint: At one frozen candidate revision and artifact, close every mandatory audit and remediation matrix, repair confirmed in-scope defects, pass required final verification and integration, reconcile documentation, and record release readiness without publication or production mutation.
Required evidence gates: complete inventories and joins, security/privacy/native analysis and Linux runtime proof, all-advisory EXT-002 dispositions, every command dimension, full UI/accessibility, all durable owners/recovery, lifecycle/provider convergence, bounded Phase 003 diagnostics, final clean automated and real runtime matrix, artifact/documentation parity, PR integration, resulting candidate verification, signed phase tags and exact cleanup.
Execution continuity: Preserve signed merged Phases 000 through 002 and current Phase 003 work. Repair newly discovered prerequisite failures in the current contiguous phase and rerun affected proof; never move cursor backward or restart completed history. Final Phase 006 includes earlier boundary regressions.
Saved goal: docs/plan/goal.md remains unchanged.
Cursor observation: docs/plan/active_phase.md was absent at intake. No state-file mutation occurs in this planning pass.
```
