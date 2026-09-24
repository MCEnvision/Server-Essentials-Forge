# Phase 001 Security and Supply Chain Closure Execution Plan

> **Plan ID:** PLAN-PHASE-001  
> **Phase ID:** SEFAUD-PHASE-001  
> **Owner:** Security audit boundary  
> **Classification:** MANDATORY, historically integrated  
> **Master plan:** [plan.md](../plan.md)  
> **Phase sequence:** 001 of 007  
> **Diagnostics contract:** 2

## Purpose and Ownership

This is the sole detailed declaration of `SEFAUD-PHASE-001`. It preserves the signed, merged Phase 001 security and supply-chain controls and their original task identities. It owns only `SEFAUD-REQ-002` and `SEFAUD-REQ-003`; it neither takes command, UI, persistence, lifecycle, final-verification, nor documentation-endpoint ownership from later phases.

The historic integration is PR 8, merge `844990d18f4a64e151f0ca5554e8f0ab5aab6035`, tag `phase-001-audit`, verified signed in the intake snapshot. That history stays valid at its recorded inputs. It is not fresh proof of the current candidate, and it is not rewound because Phase 003 later adds bounded capture. Existing doctors, reports, and the Phase 001 native and dependency manifests remain the historical controls. New advisory work, changed inputs, and current native regressions are repaired in the active contiguous Phase 003 and repeated at final Phase 006; they do not create a retroactive Phase 001 dependency on `SEFAUD-IF-004`.

`DEC-009` is controlling: Minecraft Java has one portable client contract for Linux, macOS, and Windows. The laptop client and node-1 dedicated server are execution facilities, not separate operating-system product contracts. Native/provider scrutiny is nevertheless exact: review actual branch selection, JNA API and ABI use, artifact contents, opened-object identity, error behavior, and retained-proof applicability; execute the available Linux fixtures. Do not invent a foreign client or native-runtime prerequisite, call a supported target unsupported, or represent source analysis as foreign runtime execution. `EXT-001` and `EXT-002` are available owned closure work, never external blockers.

## Evidence-Based Entry State

| Evidence class | Area | Finding | Source | Freshness and disposition |
|---|---|---|---|---|
| OBSERVED | Signed history | PR 8, its merge and signed `phase-001-audit` tag exist on `master`. | SRC-007, SRC-012 | Preserve; applicability requires the historical candidate, artifact, dependency, fixture, and assertion inputs. |
| OBSERVED | Current candidate | Current branch is `envy/phase-003-commands` at `e160a235b19c992b3a23c3a43754e92ad0147948`; product/build inputs match retained candidate `1b2e619` except planning and command-matrix generator inputs. | FIND-002, SRC-005, SRC-012 | Regenerate affected aggregates and inspect changed joins; do not infer final acceptance. |
| OBSERVED | Native writer | Linux task 311 records same-opened-object append, flush, swap, failure, rotation, restart, and queue checks. JNA 5.14.0 is compile-only and NeoForge supplies it. | FIND-006, SRC-010, SRC-012 | Reuse only after source/API/ABI/artifact and fixture comparison; changed native inputs invalidate dependent rows. |
| OBSERVED | Advisories | 28 alerts are observed: one critical, twelve high, fourteen medium, one low. Netty #28 and #27 require actual SNI pipeline and fragmented ClientHello reachability analysis. | FIND-007, SRC-007 | The old 26-alert disposition is stale. Every current alert needs its own record; version inclusion does not prove exposure. |
| OBSERVED | Historical diagnostics | Doctors and repository health exist; debug capture is absent and is a Phase 003 addition. | FIND-009, SRC-005 | Original doctor/report controls remain Phase 001 history; new capture has no backward prerequisite. |
| UNKNOWN | Current native/advisory closure | Historical results establish neither a new foreign execution nor final current closure. | DEC-009, EXT-001, EXT-002 | Phase 003 performs current scoped repair and evidence; Phase 006 repeats final regressions. |

## Scope Boundaries

### Included Scope

- `SEFAUD-REQ-002`: authority, privacy, filesystem, payload, native-writer, integration, reflection, mixin, audit, redaction, and backdoor-like-route security closure.
- `SEFAUD-REQ-003`: dependency graph, advisory, provenance, installed-runtime, packaging, JNA API/ABI, and duplicate-runtime closure.
- Historical controls, their exact proof reuse and invalidation rules, and the downstream security handoff.

### Explicit Exclusions

- `FUT-001`, `NG-001`: the sixteen unavailable families remain unavailable, unreachable, side-effect-free, and negatively tested. No new family is implemented.
- `FUT-002`, `FUT-003`, `NG-002`: no unrelated feature, broad redesign, or platform/version upgrade.
- `NG-003`, `NG-005`: no publication, production action, credentials, private data, or raw sensitive evidence.
- `SEFAUD-REQ-004` through `SEFAUD-REQ-009`: later phases own universal action proof, UI, durable-owner completeness, lifecycle convergence, final clean verification, and endpoint documentation. This phase supplies only security prerequisites and invalidation records.

## Phase Contract

### SEFAUD-PHASE-001 — Security, Privacy, and Supply Chain Closure

**Objective:** Preserve the completed Phase 001 security and supply-chain baseline, classify exactly what its evidence can still prove, and provide the security, native-writer, and dependency controls that current Phase 003 repairs and final Phase 006 regressions consume.

**Owner:** Security audit boundary

**Dependencies:** SEFAUD-PHASE-000, SEFAUD-REQ-001, DEC-001, DEC-004, DEC-005, DEC-009, EXT-001, EXT-002

**Canonical requirements:** SEFAUD-REQ-002, SEFAUD-REQ-003

**Documentation and release impact:** Historical Phase 001 documentation and evidence are preserved. Execution updates security, compatibility, migration, and release-workflow documents only when a confirmed repair changes a documented fact. No release is published.

**Next transition:** SEFAUD-PHASE-002

**Entry criteria**

- `SEFAUD-PHASE-000` provided the frozen candidate identity, trust-boundary inventory, data classification, resolved graph baseline, and safe synthetic fixtures required by `SEFAUD-REQ-002` and `SEFAUD-REQ-003`.
- `SEFAUD-IF-001` records the historical candidate, artifact, dependency, fixture, assertion, and cleanup identities for every claimed Phase 001 result.
- `DEC-009`, `EXT-001`, and `EXT-002` identify available portable/native and provenance closure work without making foreign runtime execution an entry prerequisite.
- PR 8, merge `844990d18f4a64e151f0ca5554e8f0ab5aab6035`, and signed `phase-001-audit` tag establish historic integration; a current reuse decision must still compare inputs before consumption.

**Implementation scope**

- `SEFAUD-REQ-002`: preserve and reuse the authority, privacy, trust-boundary, native-writer, and finding controls only when their exact historical inputs remain compatible.
- `SEFAUD-REQ-003`: preserve and reuse graph, advisory, provenance, JNA, installed-runtime, and artifact controls only when their seven-gate dispositions remain complete.
- `DEC-009`, `EXT-001`, and `EXT-002`: distinguish source/artifact analysis, retained proof, and available Linux execution; keep portable support and available in-plan closure intact.

**Execution order**

1. `P001-TASK-001` executed `SEFAUD-REQ-002` and `SEFAUD-REQ-003` evidence-context and prerequisite freezing.
2. `P001-TASK-002` executed `SEFAUD-REQ-002` threat, source-to-sink, abuse-case, and finding-ledger baseline work.
3. `P001-TASK-003` executed `SEFAUD-REQ-002` direct and indirect authority review.
4. `P001-TASK-004` executed `SEFAUD-REQ-002` payload, session, GUI-projection, and protocol review.
5. `P001-TASK-005` executed `SEFAUD-REQ-002` configuration, migration, recovery, durable-authority, and time-of-check review.
6. `P001-TASK-006` executed `SEFAUD-REQ-002` filesystem/content and native opened-object writer review.
7. `P001-TASK-007` executed `SEFAUD-REQ-002` and `SEFAUD-REQ-003` adapter, native API, reflection, mixin, access-transformer, and side-safety review.
8. `P001-TASK-008` executed `SEFAUD-REQ-002` sensitive-data, logging, audit, observation, export, and evidence-custody review.
9. `P001-TASK-009` executed `SEFAUD-REQ-003` graph capture and dependency/advisory dispositions.
10. `P001-TASK-010` executed `SEFAUD-REQ-002` and `SEFAUD-REQ-003` confirmed-finding repairs with focused regressions.
11. `P001-TASK-011` executed `SEFAUD-REQ-002` and `SEFAUD-REQ-003` adversarial, interruption, provider, writer, privacy, and resource-bound regressions.
12. `P001-TASK-012` executed `SEFAUD-REQ-002` and `SEFAUD-REQ-003` security scans, graph/artifact evidence renewal, and compatibility inspection.
13. `P001-TASK-013` executed `SEFAUD-REQ-002` and `SEFAUD-REQ-003` documentation, sanitized-evidence, and downstream-invalidation reconciliation.
14. `P001-TASK-014` executed `SEFAUD-REQ-002` and `SEFAUD-REQ-003` completion-packet and signed-integration work.

Tasks 003 through 009 historically investigated separate boundaries after Task 002. They converged through Task 010; Tasks 011 through 014 consumed the repaired candidate sequentially. Current reenactment is prohibited: use only a bounded current repair or final regression when the reuse matrix invalidates a row.

**Required evidence**

- `SEFAUD-REQ-002` retains source-to-sink, authority, payload, configuration, filesystem, native-writer, integration, privacy, and finding evidence with exact historical input identities and explicit current reuse or invalidation results.
- `SEFAUD-REQ-003` retains resolved candidate, packaged-mod, installed-runtime, affected-API, reachability, advisory, provenance, and compatible-remedy evidence for each current advisory; the observed 28 alerts are current work, not a historic closure claim.
- `SEFAUD-IF-005`, `DEC-009`, and `EXT-001` require source/API/ABI/artifact review plus compatible retained proof and available Linux fixture evidence, with source analysis explicitly distinct from execution.
- `EXT-002` requires JNA/JNA Platform compile-only and NeoForge-supplied runtime proof, duplicate artifact inspection, and a candidate-specific seven-gate advisory record.
- `SEFAUD-IF-006` retains PR, merge, signed tag, docs, evidence-limit, and cleanup receipts; a supplemental current record never rewrites the historical packet.

**Exit criteria**

- `SEFAUD-REQ-002` and `SEFAUD-REQ-003` historical closure remains traceable to its signed packet, and every current reuse row has an explicit compatibility or invalidation disposition.
- `DEC-009`, `EXT-001`, and `EXT-002` are represented with owned closure evidence without a foreign runtime prerequisite, false platform exclusion, or fabricated execution claim.
- No known mandatory phase-owned defect remains.

## Inputs and Upstream Contracts

| Input | Provider | Required state | Validation | Failure behavior |
|---|---|---|---|---|
| `SEFAUD-IF-001` matrix | Phase 000, reconciled by Phase 003 | Candidate, artifact, config, harness, fixture and cleanup identity accompany each reused row. | Reject duplicate, stale, unowned, missing-input, or incompatible-schema evidence. | Keep the exact row open for current Phase 003 or final Phase 006. |
| `SEFAUD-IF-002` | Phase 001 authority baseline | Decision, reason, actor/source, target, mutation, durable outcome, feedback class and terminal audit remain distinguishable. | Source/API/test review and real-path correlation. | Deny before mutation; record a current finding if evidence differs. |
| `SEFAUD-IF-005` | Phase 001 native writer baseline | Same opened object is validated, appended, and flushed; API/ABI/artifact supply is explicit. | Compare provider source, symbols, layouts, manifest, artifact hashes, and Linux fixture. | Fail closed; invalidation routes to Phase 003 repair and Phase 006 regression. |
| EXT-001 | Available facilities | Linux native fixture and portable client contract are available. | Host, artifact, runtime, fixture and cleanup manifest agree. | A missing capability leaves only its evidence row unverified; never adds a foreign prerequisite. |
| EXT-002 | NeoForge/runtime supply | Candidate, packaged, installed runtime, API reachability, advisory, provenance and remedy are distinct. | Seven-gate advisory record plus JAR/runtime inspection. | No alert is closed by ownership or JAR absence alone. |

## Outputs and Downstream Contracts

| Output | Consumer | Guaranteed state | Compatibility | Evidence |
|---|---|---|---|---|
| Historic security boundary and finding ledger | Phases 002 through 007 | Stable findings and historical controls have explicit applicability limits. | No public/wire identifier change implied. | Signed packet plus current reuse decision. |
| `SEFAUD-IF-002` authority contract | Phases 003 through 007 | Denial is non-mutating and terminal audit is correlated. | Existing identifiers remain compatible. | Allowed/denied attack fixtures. |
| `SEFAUD-IF-005` native supply contract | Phases 002, 003, 005, 006, 007 | Opened-object and supply evidence distinguish source review, retained proof, and Linux execution. | JNA stays compile-only, supplied by pinned NeoForge. | Source/API/artifact review and available Linux fixture. |
| Invalidated-row map | Phase 003 and Phase 006 | Exact changed input and required rerun are recorded. | No blanket restart. | Candidate-bound matrix disposition. |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
|---|---|---|---|---|---|---|
| P001-TASK-001 | SEFAUD-REQ-002, SEFAUD-REQ-003 | Freeze historic entry evidence and classify reusable versus stale Phase 001 rows. | Phase 000 packet, signed PR 8/tag, DEC-009, EXT-001/002. | Candidate/reuse header and invalidation map. | Git identity, Gradle inputs, `SEFAUD-IF-001`. | Hash, ancestry, artifact, fixture, dependency and cleanup comparison. |
| P001-TASK-002 | SEFAUD-REQ-002 | Build complete boundary, protected-data, attack-tree, and finding baseline. | Task 001, Phase 000 inventories. | Stable source-to-sink and abuse ledger. | Kernel, policy, GUI, storage, audit, build owners. | Missing/duplicate owner injection and independent reconciliation. |
| P001-TASK-003 | SEFAUD-REQ-002 | Audit direct/indirect authority, aliases, bundles, profiles, sudo, scheduling, grants, hierarchy and revocation. | Task 002, `SEFAUD-IF-002`. | Authority traces and finding regressions. | `KernelCommandExecutor`, `CommandExecutionService`, permissions, automation. | Forged, stale, revoked, nested and provider-failure requests have no effect. |
| P001-TASK-004 | SEFAUD-REQ-002 | Audit codec bounds, negotiation, session/revision/sequence, replay, projection, rate and logical-side controls. | Task 002, payload inventory. | Protocol boundary ledger. | `SefPayloads`, `SefSessionManager`, `PanelActionValidator`, `SefGuiServer`. | Bad nonce, stale panel, oversized request, replay and disconnect are non-mutating. |
| P001-TASK-005 | SEFAUD-REQ-002 | Audit configuration, migration, recovery, durable authority, watchers, revision and check-to-use security. | Task 002, config/store inventory. | Security finding rows and Phase 002 handoff. | `ModuleConfigService`, `StorageCoordinator`, security-sensitive writers. | Corrupt, stale, linked, swapped or unsupported input fails closed and preserves evidence. |
| P001-TASK-006 | SEFAUD-REQ-002 | Audit roots, archives, images, import/export and native opened-object append/flush. | Task 002, EXT-001, `SEFAUD-IF-005`. | Linux native manifest, retained-proof applicability and fixes. | `NativeAuditFileProvider`, `AtomicFileStore`, Fancy Tags, `FileLogSink`. | Traversal, link, swap, metadata, write, flush, rotation and restart fixtures. |
| P001-TASK-007 | SEFAUD-REQ-002, SEFAUD-REQ-003 | Audit adapters, native API loading, reflection, mixins, access transformers and dedicated-server safety. | Task 002, EXT-001/002. | Integration/native boundary dispositions. | Mod-dependency adapters, provider bridge, mixins, access transformer, JNA boundary. | Present/absent/outage/linkage/ABI/wrong-side tests fail closed. |
| P001-TASK-008 | SEFAUD-REQ-002 | Trace sensitive values through feedback, GUI, audit, logs, exceptions, export, docs, fixtures and artifacts. | Task 002, data classification. | Privacy matrix and sanitized custody record. | Audit, commandlog, GUI, social, exports, references. | Synthetic canaries appear only in authorized sinks. |
| P001-TASK-009 | SEFAUD-REQ-003 | Capture resolved graphs and make one evidence-backed disposition per advisory. | Task 001, refreshed alerts, EXT-002. | `P001-DEP-###` seven-gate ledger and JNA supply record. | `build.gradle`, wrapper, NeoForge artifacts, candidate JAR. | Deterministic graph renewal and candidate/packaged/installed/API/reachability/advisory/provenance/remedy review. |
| P001-TASK-010 | SEFAUD-REQ-002, SEFAUD-REQ-003 | Repair each confirmed finding in severity/dependency order with focused regression. | Tasks 003 through 009. | Repair commits, compatibility/recovery note, invalidation map. | Only proven affected owners/interfaces. | Before/after decisive proof, adjacent denial/recovery and no duplicate runtime. |
| P001-TASK-011 | SEFAUD-REQ-002, SEFAUD-REQ-003 | Run adversarial, concurrency, interruption, provider, writer, privacy and performance matrix. | Task 010, EXT-001/002. | Consolidated bounded regression ledger. | Command, payload, writer, dependency and integration owners. | Required fidelity and cleanup; no lower-fidelity substitution. |
| P001-TASK-012 | SEFAUD-REQ-002, SEFAUD-REQ-003 | Renew scans, graphs, artifact, provenance, licensing, JNA, mixin and access-transformer inspection. | Task 011, refreshed alert set. | Candidate/JAR/runtime closure packet. | Build, mod JAR, installed runtime and resources. | Secret/path/duplicate/native-library/advisory scan with exact hashes. |
| P001-TASK-013 | SEFAUD-REQ-002, SEFAUD-REQ-003 | Reconcile verified security, compatibility, recovery and evidence documentation. | Task 012. | Sanitized docs and downstream invalidation record. | Security review, acceptance, compatibility, migration and release workflow docs. | Link/drift/claim audit; no false platform or zero-alert claims. |
| P001-TASK-014 | SEFAUD-REQ-002, SEFAUD-REQ-003 | Assemble closure packet and complete sequential phase integration. | Tasks 001 through 013, `SEFAUD-IF-006`. | Signed historical completion packet and Phase 002 handoff. | Phase branch, PR, tag and evidence archive. | Checks, review, merge commit, tag signature, cleanup, then exact next phase. |

## Architecture and Implementation Boundaries

Authority flows through the catalog and `CommandExecutionService` to the domain mutation and mandatory terminal audit. Aliases, bundles, profiles, sudo, panels, and scheduled text carry intent only: they preserve real and effective actor, source, target, policy revision, confirmation, and correlation. Client state is presentation and server-side validation at mutation is authoritative.

`NativeAuditFileProvider` owns the identity-sensitive append boundary. Its POSIX descriptor and Windows handle branches require direct source/API/ABI review, but only available Linux execution is planned anew. Validation, append, flush, and post-flush verification bind to the same opened descriptor/handle; no path-only fallback is allowed. JNA/JNA Platform are compile-only declarations, provided by the pinned NeoForge runtime; the candidate JAR must not embed duplicate classes, service entries, or native libraries.

Advisory ownership is a fact to prove, not a safety conclusion. Each record identifies exact declared/resolved path, candidate artifact, installed runtime, affected API/configuration reachability, advisory condition, provenance, and compatible remedy. For Netty #28/#27, inspect actual SNI/default-context, mutual-TLS and fragmented ClientHello pipeline configuration before any applicability conclusion; this plan does not assert that the mod uses those paths.

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
|---|---|---|---|---|
| Forged, stale, revoked or nested authority | Decision reason, revisions, actor/source, before/after and audit correlation disagree. | Reject before mutation and preserve protected state. | Invalidate session/grant, repair owner, rerun affected route. | Task 003 real entry route and correlated `SEFAUD-IF-002` outcome. |
| Payload replay or stale panel | Session/sequence/panel revision is absent, duplicated or stale. | Reject on logical server without private projection/effect. | Close stale session and re-open authorized view. | Task 004 replay, out-of-order, disconnect and reconnect fixtures. |
| Native target swap/link/metadata failure | Object identity, safe type/link/reparse state, append/flush stage or provider error differs. | Fail closed, preserve prior valid evidence, no path fallback. | Retain recovery marker and repair the provider; re-run Linux fixture. | Task 006 swap, symlink, hard-link where detectable, write/flush, rotation/restart fixtures. |
| Advisory has ambiguous platform ownership | One of seven disposition fields is missing, or source/API reachability is unknown. | Remain open; never claim not-applicable/zero alerts from ownership. | Obtain source/artifact evidence or use compatible mod-owned remedy. | Task 009 ledger validation and Task 012 renewal. |
| Provider linkage/outage | Provider state, API/ABI/linkage and decision reason distinguish absent, incompatible, outage and denial. | Fail closed, preserve core availability, no implicit grant. | Restore compatible provider or documented fallback, then recheck current authority. | Task 007 present/absent/outage fixtures. |
| Sensitive marker reaches an unauthorized sink | Canary scan identifies sink, correlation and redaction order. | Quarantine disposable output; do not publish raw evidence. | Repair redaction order, remove owned scratch, recreate proof. | Task 008 source-to-sink canary matrix. |
| Interrupted append or resource bound | Bounded wait reaches timeout, queue count, bytes or flush result. | Explicit `not_committed`, `committed`, or `unknown`; no duplicate side effect. | Resolve receipt/marker before retry, stop owned fixture. | Task 011 interruption and restart fixture. |

## Diagnostics and Debugging

**Requirement IDs:** SEFAUD-REQ-002, SEFAUD-REQ-003  
**Task IDs:** P001-TASK-001, P001-TASK-002, P001-TASK-003, P001-TASK-004, P001-TASK-005, P001-TASK-006, P001-TASK-007, P001-TASK-008, P001-TASK-009, P001-TASK-010, P001-TASK-011, P001-TASK-012, P001-TASK-013, P001-TASK-014  
**Controls:** Historical evidence uses `/sef doctor` and delivered subsystem doctors and reports. It does not claim a capture control existed. `SEFAUD-IF-004` is a Phase 003 producer and its controls are consumed only by current Phase 003 and final Phase 006 work.  
**Signals:** Historical Phase 001 reports use their delivered doctor/report fields. `SEFAUD-IF-004` schema version 2 is not retroactively inserted into those packets and is not redefined here. Its `CaptureStatus` has schema version, enabled state, capture id, side, category, sanitized target, remaining milliseconds, accepted/dropped/truncated counters, remaining events/bytes, owned output locator, and stop reason. Its events carry those identities plus monotonic sequence, UTC timestamp, optional tick, correlation id, component/action/source enums, actor/effective-actor pseudonyms, decision/reason enums, expected/observed revision, duration milliseconds, and typed optional subsystem fields. Phase 001 consumes these frozen fields without local additions when a current reuse row needs a Phase 003 diagnostic correlation.  
**Collection procedure:** The historical procedure below uses only delivered doctors and retained reports. The separate current-consumer note identifies how later phases use `SEFAUD-IF-004`; it is not evidence that Phase 001 had capture.  
**Headless verification:** node-1 only runs unit, build, dedicated server, and configured no-GUI GameTest paths after task-graph inspection.  
**Client verification:** this phase has no standalone client acceptance claim. The one portable client contract is consumed by Phases 003/004/006 for their named GUI/command claims.  
**Client audio isolation:** no Minecraft client is launched by a Phase 001-only historical/reuse audit. If a current Phase 003 security row needs one, use its exact laptop procedure; node-1 is never a client host.  
**Budgets and privacy:** Historical reports retain their original redaction and custody rules. Later `SEFAUD-IF-004` capture defaults off, has bounded seconds/sample limit, counts saturation, pseudonymizes identifiers, rejects unsafe sink/authorization/absent target, and stops on timeout/reset/reload/shutdown/test teardown.  
**Regression and support:** Historical evidence is verified by report/provenance replay. Later Phase 003 self-tests enable/status/disable, expiry, unauthorized, absent-target, reload, and sink failure; it preserves only the minimum sanitized packet and updates support guidance when delivered.

If a named current security row requires the portable laptop client, the owned isolated instance sets prelaunch master audio to zero before launch. After its owned window opens, identify the exact owned window, PID, class, and title with `hyprctl clients -j`; correlate that PID tree to its PipeWire or PulseAudio stream; mute only the owned stream with `wpctl` or `pactl`; and verify mute before acceptance actions. Recheck every stream recreation after reload, reconnect, or device change, remute it immediately, and stop the owned client if identity or mute cannot be verified. Never mute a default sink, system-wide output, microphone, unrelated application, or personal instance. Stop any owned watcher and remove its temporary state during cleanup.

| Signal | Source and unit | Expected observation |
|---|---|---|
| authority decision | logical server, one event/request | actor/source/revision and reason match allowed or denied mutation state. |
| terminal audit | native writer, one event/outcome | correlation joins decision to same-object append/flush or a precise failure. |
| writer identity | provider, descriptor/handle identity | identity/type/link state remains stable through append and flush. |
| dependency disposition | graph/artifact review, one record/advisory | all seven gates and advisory precondition are explicit. |
| capture health | capture service, counters and bounded duration | default off, authorized, redacted, expiring and no silent resume. |

1. Read the delivered `/sef doctor` and subsystem doctor reports with the retained Phase 001 candidate, artifact, native-writer, and dependency manifest identifiers; record their report location and the exact historical claim under review.
2. Compare the historical candidate, artifact SHA-256/SHA-512, Java/NeoForge/dependency inputs, fixture, source/API/ABI fingerprints, assertion semantics, and cleanup receipt to the current row. Mark the row reusable only when every required input matches.
3. For the native writer, inspect the retained Linux fixture report for entry point, expected/actual same-opened-object state, identity/type/link results, append/flush outcome, rotation/restart outcome, and bounded timeout/failure oracle. For an advisory, inspect its seven gates and alert timestamp; a missing field leaves it open.
4. Preserve only a sanitized reuse/invalidation record. Do not start a runtime merely to replay historic evidence. If a current row is invalidated, hand it to Phase 003; Phase 006 supplies final regression. Verify no owned scratch/process was created, or clean the exact owned resource before closing the review.

**Current Phase 003 and final Phase 006 consumer note:** When a current reopened row needs `SEFAUD-IF-004`, Phase 003 performs the actual enable/status/disable capture procedure under its own task and joins the frozen schema fields to the real entry point. Phase 006 repeats final regressions after the last product change. Neither procedure changes this historical Phase 001 packet.

## Verification Matrix

| Requirement or task | Static or unit | Integration | Real workflow or runtime | Negative and recovery | Execution host and prerequisites | Evidence artifact |
|---|---|---|---|---|---|---|
| P001-TASK-003, SEFAUD-REQ-002 | Policy/permission/source unit suite. | Alias, bundle, sudo, panel and provider joins. | Dedicated-server real route only where server state is the claim. | Forge/revoke/stale/nested race leaves state unchanged. | node-1, no GUI, disposable fixture and exact candidate. | Correlated decision/audit ledger. |
| P001-TASK-004, SEFAUD-REQ-002 | Codec/session/validator tests. | Network handler and server projection. | Server-side payload path. | Replay, stale, oversized, absent target and reconnect. | node-1 headless. | Protocol boundary report. |
| P001-TASK-006, SEFAUD-REQ-002, EXT-001 | Provider/source/ABI and writer contract tests. | Packaged JAR plus NeoForge supplied API. | Available Linux native fixture. | Link, swap, metadata, write, flush, rotation, restart. | node-1 Linux no-GUI; no foreign host prerequisite. | Sanitized Linux native manifest and applicability record. |
| P001-TASK-007, SEFAUD-REQ-002, SEFAUD-REQ-003 | Adapter, mixin, access-transformer, classloading checks. | Present/absent/outage provider and linkage. | Dedicated-server startup if required. | ABI/symbol failure and wrong side fail closed. | node-1 headless. | Integration disposition. |
| P001-TASK-008, SEFAUD-REQ-002 | Redaction and canary tests. | Log/audit/export/GUIs server projection. | Real server sinks where helper tests cannot prove ordering. | Unauthorized sink, exception and retention cleanup. | node-1; client only if later UI claim requires it. | Sanitized privacy matrix. |
| P001-TASK-009/012, SEFAUD-REQ-003, EXT-002 | Deterministic graph/parser tests and artifact inspection. | Candidate, packaged and installed runtime resolution. | Linux native loading only for available fixture. | Missing provenance, duplicate class/native, stale alert, SNI condition unknown. | node-1, pinned wrapper/runtime. | Seven-gate `P001-DEP-###` ledger, SHA-256/SHA-512 reports. |
| P001-TASK-010/011 | Before/after focused regression and adjacent suite. | Cross-boundary security/writer/provider tests. | Exact failure workflow at required fidelity. | Interrupt, retry, recovery and cleanup assertions. | Per fixture; never substitute a lower-fidelity helper. | Finding ledger and invalidation map. |

## Documentation, Operations, and Release

When current repairs occur, update only affected facts in `README.md`, `DOCUMENTATION.md`, `docs/SECURITY_REVIEW.md`, `docs/SEF2_ACCEPTANCE.md`, `docs/COMPATIBILITY_MATRIX.md`, `docs/MIGRATION_GUIDE.md`, and `docs/RELEASE_WORKFLOW.md`. State portable support accurately: analysis and available Linux native evidence do not make an unsupported-target claim, and advisory closure is individual rather than a false zero-alert promise. Retain raw attack fixtures, private logs, and malformed inputs only in approved private evidence storage; tracked docs contain sanitized reproducible summaries.

## Risks and Evidence Invalidation

| Risk ID and owner task | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
|---|---|---|---|---|---|
| RISK-002, Tasks 002 to 004/008 | Complete source-to-sink rows and mutation-time authority recheck. | Actor/revision/reason or denied-state mismatch. | Repair route and rerun exact route/sink. | Authority, GUI, audit and privacy rows. | Current Phase 003 scoped regression, final Phase 006. |
| RISK-003, Tasks 009/012 | Seven independent advisory gates and artifact provenance. | Missing gate, changed alert, path, API or package/runtime hash. | Reopen only affected `P001-DEP-###`; use compatible remedy if mod-owned. | Dependency, packaging, runtime and docs evidence. | Refresh in Phase 003 and final Phase 006. |
| RISK-004, Tasks 006/007/011 | API/ABI/object-identity review and fail-closed same-opened-object writer. | Linkage, type/link identity, append or flush mismatch. | Preserve valid state, repair provider, rerun Linux fixture. | Native writer, persistence-security and artifact claims. | Scoped current proof then Phase 006 full regression. |
| Phase 003 capture is mistaken for historic proof, Task 001 | Separate historical doctor/report controls from new IF-004 capture. | Evidence timestamp/schema says capture v2 for old claim. | Correct provenance; do not rewind signed history. | Only false reuse record. | Reconcile `SEFAUD-IF-001` evidence identities. |
| Foreign execution is falsely required or claimed, Tasks 006/007 | DEC-009 portable contract and explicit evidence class. | Plan/evidence includes foreign requirement or labels source analysis executed. | Remove false gate/claim, retain source/artifact review and Linux evidence. | Affected native/support assertions. | Source/artifact review and available Linux fixture. |
| Test cleanup fails, Tasks 006/011/012 | Register exact owned paths/processes before launch. | Process/path remains after suite. | Stop owned resource and remove only verified scratch. | Cleanup receipt and affected suite result. | Repeat cleanup verification before next run. |

### Concrete Security Risk Workflows

- `SEFAUD-REQ-002`, RISK-002: use a synthetic denied actor and a protected target on the disposable dedicated-server fixture. Enter the real catalog route through `KernelCommandExecutor` and, where applicable, its alias or panel route; record the expected denied reason, unchanged before/after semantic state, policy revision, real/effective actor pseudonyms, terminal audit id, and actual outcome. A missing decision, changed protected state, wrong actor, or missing audit is the failure oracle. Wait only the configured request/command timeout, invalidate the request/session on timeout, stop the owned fixture, remove its exact logs and runtime after the sanitized record is retained, then route a confirmed defect to current Phase 003 and final Phase 006.
- `SEFAUD-REQ-002`, RISK-004: use the Linux `NativeAuditFileProvider` fixture to open a disposable regular audit object, then attempt target/parent swap, symbolic link, detectable hard link, unavailable metadata, append failure, flush failure, rotation, and restart through the provider's actual append entry point. Expected state is same descriptor identity, safe type/link state, explicit append/flush result, and preserved prior bytes on failure. Record provider/API/ABI identity, expected/actual descriptor identity, failure stage, duration, audit correlation, and recovery locator. The bounded writer timeout or an identity mismatch stops the owned fixture; retain one sanitized report, remove exact scratch, and rerun the affected Linux row after repair.
- `SEFAUD-REQ-003`, RISK-003: for each refreshed advisory, use the declared Gradle configuration, candidate JAR and installed NeoForge runtime as the actual entry points. Record advisory id/version range, dependency path, packaged and installed presence, affected API/configuration reachability, SNI/default-context/mutual-TLS and fragmented-ClientHello result for #28/#27 when applicable, provenance, remedy, and timestamp. Expected state is a complete seven-gate disposition, not an assumed safe version or zero-alert count. Missing gate, changed alert, unresolved reachability, or incompatible remedy leaves the row open; preserve only hashes and sanitized report, clean the owned resolution scratch, and rerun after the precise input changes.
- `SEFAUD-REQ-002`: use a synthetic secret canary through the delivered audit/log/export route, inspect only owned disposable sinks, and record source, authorized sink predicate, redaction class, expected absence/presence, actual match count, correlation id, and retention result. Any unauthorized match is the failure oracle. Quarantine the owned output, do not copy its raw content, repair the redaction order, recreate the fixture, and remove the exact scratch directory after the sanitized evidence is retained.

## Phase Completion Packet

The historical Phase 001 packet records: the frozen candidate and artifact identities; complete trust-boundary, source-to-sink, finding, privacy and dependency ledgers; `P001-DEP-###` seven-gate records; native source/API/ABI/artifact review and Linux writer manifest; focused repair and adversarial regressions; docs/invalidation records; exact cleanup; PR 8, merge `844990d18f4a64e151f0ca5554e8f0ab5aab6035`, and signed tag `phase-001-audit`.

For any current reuse or repair, a new supplemental record must name historical and current inputs, differences, unaffected invariants, exact reopened rows, expected/actual results, artifact SHA-256/SHA-512, host/runtime/config/fixture/harness, advisory snapshot, evidence limit, cleanup receipt, and downstream rerun. It must not rewrite the historic packet or assert the final endpoint.

## Exit Criteria

- `SEFAUD-REQ-002` and `SEFAUD-REQ-003` historical closure remains traceable to its signed packet, and every current reuse row has an explicit compatibility or invalidation disposition.
- `DEC-009`, `EXT-001`, and `EXT-002` are represented with owned closure evidence without a foreign runtime prerequisite, false platform exclusion, or fabricated execution claim.
- No known mandatory phase-owned defect remains.

## Next Transition

The historical transition to Phase 002 is complete. The active candidate is in Phase 003; only that phase may repair current advisory/native/security deltas, followed by final Phase 006 regression and Phase 007 endpoint closure. No Phase 001 branch, pull request, tag, capture prerequisite, foreign runtime gate, or historical restart is authorized by this plan.
