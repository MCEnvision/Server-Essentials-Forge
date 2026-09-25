# Phase 005 Execution Plan

> **Plan ID:** PLAN-PHASE-005  
> **Phase ID:** SEF-PHASE-005  
> **Owner:** Proxy authority  
> **Classification:** MANDATORY  
> **Master plan:** [plan.md](../plan.md)  
> **Phase sequence:** 005 of 016

## Purpose and Ownership

This phase makes the authenticated transport from Phase 004 into the only durable network authority. It owns the SQLite writer, immutable replica sequence, UUID session epochs and profile admission relation. The master remains the authority for product scope and shared signatures. This blueprint owns only Phase 005 implementation detail and its evidence. A future Phase 006 consumer may receive the reserved `VISIT` and `LIFECYCLE_DELIVERY` state kinds, but they are non-dispatchable here and are not proof of lifecycle delivery.

## Evidence-Based Entry State

| Evidence class | Area | Finding | Source or command | Freshness condition |
|---|---|---|---|---|
| OBSERVED | source topology | Proxy SQLite authority and separate high-volume audit storage are selected; no owner database may be changed. | FIND-012, SRC-001, SRC-115, SRC-116 | Invalidate when storage decision or SQLite artifact identity changes. |
| PROPOSED | authority behavior | Ordered replicas, commit-before-publication, snapshots and degraded refusal are required behavior, not existing implementation. | FIND-012, SEF-REQ-012 | Invalidate on signed contract or schema change. |
| PROPOSED | session authority | UUID, backend boot and connection epoch fence presence and delegated routing. | FIND-013, SRC-102, SRC-114 | Invalidate on bridge or identity-forwarding contract change. |
| OBSERVED | compatibility limit | Adapter availability and a valid clientresetpacket archive do not prove Velocity 4.2.0 runtime compatibility or arbitrary pack switching. | FIND-101, EXT-003, EXT-004, EXT-005, EXT-010 | Recheck each candidate, observed client capability and directed profile matrix. |
| PROPOSED | pack admission | Only evidence-backed same or direction-specific compatible profiles admit; unknown and incompatible profiles refuse before movement. | FIND-018, DEC-006, DEC-007 | Invalidate on profile, adapter, registry, config, or evidence digest change. |
| OBSERVED | SQLite artifact | Xerial SQLite JDBC 3.53.4.0 is content-hashed but Java 25 loading and recovery remain unverified. | FIND-111, EXT-009 | Recheck artifact, license, advisories and runtime loading before use. |

## Scope Boundaries

### Included Scope

- SEF-REQ-012: one proxy-owned SQLite writer for bans, absolute mute expiration, vanish, qualified homes and operations, with commit-first replication, snapshots, restart recovery and defined outage refusal.
- SEF-REQ-013: authenticated UUID presence, current session epochs and permission-aware routing for player, console and delegated sources.
- SEF-REQ-018: versioned operator-tested directed relation evidence and admission that preserves source state on missing reset capability, unknown or incompatible requests. The external reset mod is installed in a supported client profile only, never bundled with SEF or installed on a backend.

### Explicit Exclusions

- SEF-REQ-014, SEF-REQ-015 and SEF-REQ-036 remain Phase 006 work. This phase only establishes their authority substrate and never dispatches moderation, visibility, visit or lifecycle behavior.
- SEF-REQ-016 and SEF-REQ-017 remain Phase 007 work. No cross-server home mutation or transfer success contract is delivered here.
- No SEF client or launcher project, arbitrary pack switching, shared SQLite file, multiple proxy authority, economy, custom interface, production rollout or public release is included. The owner-approved external client compatibility mod does not change this product boundary.

## Phase Contract

### SEF-PHASE-005 — Durable network state, sessions and pack admission

**Objective:** Deliver the single proxy writer and ordered replicas that durably fence UUID sessions and admit only validated profile relations.  
**Owner:** Proxy authority  
**Dependencies:** SEF-PHASE-004, EXT-009, EXT-010, SEF-REQ-005, SEF-REQ-006, SEF-REQ-010, SEF-REQ-011
**Canonical requirements:** SEF-REQ-012, SEF-REQ-013, SEF-REQ-018  
**Documentation and release impact:** Add authority schema, backup and recovery, profile relation and refusal, session fencing, support diagnostics and migration documentation. No release or production deployment occurs.  
**Next transition:** SEF-PHASE-006, enforce moderation and visibility.  

**Entry criteria**

- Phase 004 is integrated and tagged on both applicable product branches, the paired candidate provenance and `SEF-IF-005` bridge security receipt pass, Phase 000 pinned real-client Forge feasibility evidence remains valid, and EXT-009 still matches its recorded hashes, notices and advisory gate.
- The Phase 001 identity, configuration and diagnostics contracts, Phase 002 policy contract, and Phase 004 bridge are available at their declared signatures; the three test packs, exact client reset artifact and profile evidence fixtures are pinned. Phase 000 must have observed the launched client's `clientresetpacket` capability and continuous-session round trip, not merely a manual reconnect.

**Implementation scope**

- Implement SEF-REQ-012, SEF-REQ-013, SEF-REQ-018 through the work packages and acceptance obligations below.

- Implement `SEF-IF-006` exactly: one serialized SQLite authority writer, immutable ordered replica apply, typed `StateChange`, presence and admission methods, and its listed safe failures. Trace: SEF-PHASE-005.
- Implement profile relations as versioned durable operator-tested evidence records. A relation is direction-specific and binds the server-observable negotiated client profile, source, destination, adapter and evidence digests. It is not a cryptographic signing or PKI feature. Trace: SEF-PHASE-005.

**Execution order**

1. `P005-TASK-001` first supplies authority and session diagnostics, schema, writer, replicas and recovery controls before any dependent real-path evidence. Trace: SEF-PHASE-005.
2. `P005-TASK-002` then implements profile evidence validation and admission on top of the committed session and authority path. Trace: SEF-PHASE-005.
3. `P005-TASK-003` runs bounded headless recovery, then real matching-profile and direction-specific differing-compatible client switches through the existing external Velocity path and the implemented admission policy. This does not depend on Phase 007 coordinate teleport. Trace: SEF-PHASE-005.
4. `P005-TASK-004` completes documentation, paired integration evidence and cleanup after all preceding local gates pass. Trace: SEF-PHASE-005.

**Required evidence**

- Concurrent CAS, gap, reorder, duplicate, corrupt snapshot, crash cut, Java 25 native-load, and SQLite backup API or verified quiesced checkpoint recovery with every required WAL component.
- Rapid reconnect, backend switch, offline target, revoked permission, delegated actor and console source fixtures with correlated session epochs.
- Three matching profile admission, direction-specific compatible differing profile admission, incompatible and unknown refusal before movement, stale evidence refusal and source-state preservation through actual server admission handlers. Retain these isolated no-player policy tests, then prove matching-profile and differing-compatible switching with a real client through the external Velocity/Ambassador/PCF path and SEF admission. Phase 015 repeats this proof against the final candidates; it is not the first real compatibility proof. Phase 007 coordinate teleport and durable arrival receipts remain outside this phase.

**Exit criteria**

- No known mandatory phase-owned defect remains. Every such defect must be fixed and its affected evidence rerun before phase closure.

- `SEF-AC-012`, `SEF-AC-013` and `SEF-AC-018` have required evidence at their stated fidelity. The single writer restart and replica resync, UUID epochs and compatible relations are proven; incompatible transfer is refused.
- The Forge PR/check/merge/result/tag sequence completes first. Then the proxy is retested against that exact approved common digest, followed by the proxy PR/check/merge/result/tag sequence. No known phase-owned defect, open cleanup item or missing required evidence remains.

## Inputs and Upstream Contracts

| Input or contract | Provider | Required state | Validation | Failure behavior |
|---|---|---|---|---|
| `SEF-IF-001` Actor and Session | Phase 001 | UUID, backend boot and connection epoch are typed and display names have no authority. | Codec and stale-session fixture. | Reject stale or malformed identity; do not infer a session. |
| `SEF-IF-002` diagnostics | Phase 001 | The inherited console `sef debug` control resolves a registered authority target before control, requires `sef.debug.manage`, defaults to 60 seconds and permits at most 300 seconds, 200 events per second, 10,000 events, 8 MiB, a 1024-event queue and two captures per process. | `sef debug on authority registered-target 60`, `sef debug status`, then `sef debug off <capture-id>` self-test. | Deliver missing phase signals before dependent acceptance. |
| `SEF-IF-003` policy | Phase 002 | Console and delegated sources retain explicit actor and permission revision. | Revocation-between-dispatch/apply fixture. | Return `DENIED` or `STALE_REVISION`; never impersonate a player. |
| `SEF-IF-005` bridge | Phase 004 | Mutually authenticated typed frames, peer identity and replay fence pass. | Pair provenance and zero-player bridge receipt. | Stop network mutation and admission on transport failure. |
| EXT-009 | Xerial SQLite JDBC 3.53.4.0 | Exact hash, notice and current advisory recheck. | Candidate dependency and Java 25 isolated load receipt. | Leave SQLite-dependent gate open; no driver substitution. |

## Outputs and Downstream Contracts

| Output or contract | Consumer | Guaranteed state | Compatibility or versioning | Evidence |
|---|---|---|---|---|
| `SEF-IF-006` StateChange and snapshots | Phase 006, 007, 010, 014 | Serialized commits and atomic snapshots are revision-fenced; authority outage refuses unsafe change. | Registry and schema version 1, typed result/error contract. | CAS, crash and resync suite. |
| Presence and session registry | Phase 006 and 007 | UUID, proxy boot, backend boot and epoch identify current presence. | A reconnect replaces earlier epoch; stale observers receive no action. | Reconnect/switch/offline suite. |
| CompatibilityRelation registry | Phase 006, 007 and 014 | Admission is allowed only for matching recorded relation and live digests. | Direction-specific digest binding; unknown data is not compatible. | Same, compatible-different, incompatible and stale evidence matrix. |
| Reserved lifecycle state kinds | Phase 006 | Stored kinds remain non-dispatchable until Phase 006 typed handlers register. | No persistent UI-token authority is created or implied. | Static interface and negative dispatch assertion. |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
|---|---|---|---|---|---|---|
| `P005-TASK-001` | SEF-REQ-012, SEF-REQ-013 | Build the proxy SQLite authority schema and guarded migration, serialized writer, `StateChange` CAS/idempotency ledger, supported SQLite backup API or verified quiesced checkpoint backup containing database and any WAL state required by the selected consistent backup method, commit-first ordered replica publisher, atomic snapshot install, UUID presence/session epoch fence, and authority diagnostics. | SEF-PHASE-004, EXT-009, SEF-IF-001, SEF-IF-002, SEF-IF-003, SEF-IF-005. | Versioned authority store, immutable replica stream, snapshot/resync, recovery and registered-target diagnostic controls. | Proxy authority service, backend replica adapter, `SEF-IF-006`. | First self-test `sef debug on authority registered-target 60`, `status` and `off` before dependent acceptance. Then prove concurrent CAS and duplicate operation IDs commit exactly once, gap/reorder/duplicate/corrupt snapshot and restart converge, unknown future schema rejects, and unavailable writer yields `AUTHORITY_UNAVAILABLE`. |
| `P005-TASK-002` | SEF-REQ-013, SEF-REQ-018 | Build durable profile records and direction-specific operator-tested compatibility relation evidence, validate server-observable negotiated client reset capability, source, destination, adapter and evidence digests, and make `admit` decide before any connection move. | P005-TASK-001, SEF-REQ-010, DEC-006, DEC-007, EXT-003, EXT-004, EXT-005, EXT-010, valid Phase 000 pinned real-client Forge feasibility evidence. | Typed `Presence`, `CompatibilityRelation`, `AdmissionDecision` and safe explanation keys. | Proxy admission coordinator and `SEF-IF-006`. | Actual server admission-handler fixtures with no player prove same and explicitly compatible different profile decisions; missing reset capability, unknown, incompatible and stale-evidence fixtures refuse with source session/world unchanged. They do not claim simulated profiles attest full client files or complete real client switching. |
| `P005-TASK-003` | SEF-REQ-012, SEF-REQ-013, SEF-REQ-018 | Execute bounded headless recovery and empty-backend convergence, then qualify actual directed compatibility relations and test real client switching through the implemented admission policy. Use the existing external switch path, not future SEF coordinate travel. | P005-TASK-001, P005-TASK-002, EXT-001, exact candidate/client/profile/adapter hashes and authorized isolated fixture. | Headless recovery receipt, real matching and differing-compatible directional switch matrix, refusal/source-state receipts and deployable evidence bindings. | Dedicated node-1 topology, SQLite authority, real admission listener, external Velocity switch path and silent isolated laptop client. | Inject writer crash, replica gap and reconnect. Prove real same-profile and differing-compatible switches, reversed direction only with its own proof, unknown/incompatible/stale refusal before movement, and rejection of synthetic fixture evidence as deployable qualification. |
| `P005-TASK-004` | SEF-REQ-012, SEF-REQ-013, SEF-REQ-018 | Document authority ownership, schema migration, backup, degraded operation, profile evidence and refusal support; package phase evidence and perform ordered paired branch integration gates. | P005-TASK-001 through P005-TASK-003, tracked documentation conventions, checked PR requirements. | Updated authoritative documentation, phase receipt, hashes and cleanup receipt. | README, docs index, technical authority and operations topics, Forge and proxy phase PRs. | Forge PR, checks, merge, resulting-branch verification and signed tag complete first; retest proxy against that exact approved common digest; then proxy PR, checks, merge, resulting-branch verification and signed tag; finish verified post-merge cleanup. |

Each package must use only its task-owned disposable fixtures. Migration preserves the original source, rejects an unknown future schema before mutation, and restores only into an isolated target. A restore verifies independent counts and latest revisions before reuse; backup is a supported SQLite backup API result or a verified quiesced checkpoint containing the database and any WAL state required by the selected consistent backup method, never a live main-file-only copy. The replica installer may retry an idempotent snapshot, but no handler may apply a noncontiguous revision. `P005-TASK-002` is blocked only on the actual `P005-TASK-001` contract, not on any future consumer.

## Architecture and Implementation Boundaries

The proxy is the single authority and sole SQLite writer. It commits a state mutation and idempotency record before publishing an immutable revision. Backend replicas never write the database file, only apply contiguous revisions or atomically replace their state with a validated snapshot. A failed write, missing authority, noncontiguous stream, failed checksum, unavailable native driver or unknown peer fences new unsafe operations and routes to the typed error.

`StateChange` preserves current master signatures and valid state kinds. Bans, absolute mute expiry, vanish, qualified homes and operations are durable substrates, not Phase 006 or Phase 007 feature dispatch. `VISIT` and `LIFECYCLE_DELIVERY` remain reserved and non-dispatchable until Phase 006; no action tokens, templates, UI authority or delivery claim behavior is invented here.

Presence is keyed by canonical UUID and a complete `Session`. Proxy boot and monotonically issued connection epoch make reconnects replace old sessions; backend boot fences stale backend reports. `resolve` applies observer authorization before resolution and suggestion exposure; command routing rechecks source and target at apply time. Console and delegated actors remain their own `Actor.source` and `originId`, with no implicit player privileges.

Compatibility is a registry of versioned operator-tested directed relation evidence, not a cryptographic signing scheme or a best-effort heuristic. Each relation binds the server-observable negotiated client profile digest, including the observed `clientresetpacket` capability and exact qualified adapter identity, to source/destination profile, adapter and evidence digests, and is direction-specific. `admit` reads the current session, reset capability and relation before a move, including the actual Velocity pre-connect route used by an ordinary authorized server-switch request. Capability is not accepted from player-supplied administrative messages. Synthetic admission fixtures are isolated test-only state. Their evidence cannot be imported or accepted as deployable compatibility qualification.

P005-TASK-003 first uses an isolated authorized external compatibility fixture to establish real matching-profile and differing-compatible directed switching evidence with exact client, source, destination, profile, reset and adapter identities. That qualification fixture does not override SEF admission in the acceptance or deployed topology. It then registers only the actual evidence through the restricted operator path and proves those same directions through the implemented SEF admission listener. No request payload may self-certify its evidence. A changed binding invalidates the relation. Missing observed reset capability, unknown, stale, malformed, merely simulated or incompatible evidence refuses before movement with `UNKNOWN_PROFILE` or `INCOMPATIBLE_PROFILE`, preserves the source session/world and gives a safe client-pack explanation. The running client remains unchanged during each differing-compatible transfer; the external reset mod only resets client connection state and does not supply missing content. No SEF client, pack installation or launcher feature is introduced. This phase proves external server-switch compatibility, not Phase 007 coordinate teleport or its durable arrival contract; Phase 015 supplies final candidate regression.

SQLite runs only inside an isolated proxy-owned runtime. The schema has explicit versioning and guarded migrations that preserve source, reject unknown future schema and restore into an isolated target before count/revision verification and reuse. Backups use a supported SQLite backup API or a verified quiesced checkpoint with every component required by the selected consistent backup method. The inherited authority diagnostic limits are fixed controls, not proposed measurements: default 60 seconds, maximum 300 seconds, 200 events per second, 10,000 events, 8 MiB, 1024 queued events and two captures per process. Authority diagnostics are default-off, `sef.debug.manage` permission checked, concrete-registered-target resolved before activation, bounded and separate from later mandatory unsampled audit history.

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
|---|---|---|---|---|
| Writer crashes after WAL commit but before publish | `authority.commit` has durable receipt without publish sequence. | Do not publish a fabricated result or accept later out-of-order mutation. | Restart writer, recover WAL, reconcile commit ledger, publish once or return typed uncertainty. | Crash-cut fixture at commit/publish boundary proves latest durable truth. |
| Backup, migration or restore is incomplete | Backup manifest lacks a required component required by the selected consistent backup method, count/revision comparison differs, or schema is unknown future. | Preserve original source and reject future schema before mutation; never reuse an unverified target. | Use supported backup API or verified quiesced checkpoint, restore to isolated target, verify independent counts/latest revisions, then permit reuse. | Backup/restore/migration fixture proves a live main-file-only copy is rejected. |
| Replica receives revision `n+2` before `n+1` | `authority.replica` reports expected and received revision. | Reject noncontiguous apply and return `RESYNC_REQUIRED`. | Fetch checksum-validated atomic snapshot, install then resume only from contiguous next revision. | Reorder and duplicate stream fixture. |
| Snapshot payload is corrupt or wrong schema | Digest, schema and revision validation fail. | Preserve last known replica; do not partially install state. | Retry a fresh snapshot while mutations remain fenced. | Corrupt snapshot fixture proves no state resurrection. |
| Rapid reconnect or backend restart | `presence.epoch` has old/new proxy or backend boot and epoch. | New UUID session replaces old; stale source/target cannot receive action. | Re-register current session and invalidate old routing entries. | Switch/reconnect and offline target fixture. |
| Permission revision changes between routing and apply | `authority.route` reports expected and current revisions. | Reauthorize source and target; return `STALE_REVISION` or `DENIED`. | Caller refreshes policy/session, never escalates console/delegated identity. | Revocation-between-dispatch/apply fixture. |
| Profile relation unknown, incompatible, stale or reversed | `admission.decision` reports all five digest fields and reason. | Refuse before any move, keep source state unchanged and show a safe pack-change explanation. | Operator refreshes only validated relation evidence; user changes client pack outside SEF. | Unknown/incompatible/stale/reversed relation matrix. |

## Diagnostics and Debugging

**Requirement IDs:** SEF-REQ-012, SEF-REQ-013, SEF-REQ-018
**Task IDs:** P005-TASK-001, P005-TASK-002, P005-TASK-003, P005-TASK-004
**Controls:** Resolve a concrete registered authority target before control. An actor with `sef.debug.manage` uses inherited console `sef debug on authority registered-target 60`, confirms `sef debug status`, and invokes `sef debug off <capture-id>`. Default duration is 60 seconds, maximum duration is 300 seconds, rate is 200 events per second, total is 10,000 events, output is 8 MiB, queue is 1024 events and no more than two captures run per process. Reject absent or unregistered targets, denied actors, excess duration, rate, event, byte, queue or capture limits, and verify disabled status before teardown.
**Signals:** Emit typed `DiagnosticEvent` records for `authority.commit`, `authority.replica`, `authority.snapshot`, `presence.epoch`, `authority.route` and `admission.decision` with captureId, correlationId, side, boot, sequence, monotonicNanos, desired, actual, reason, units, configGeneration and candidateDigest.
**Collection procedure:** First self-test the registered-target console control and its limits, then use the numbered local runbook below to enable one capture, apply the fixture through the bridge or admission entry point, inspect matched source-unit signals, disable, sanitize and remove exact disposable outputs.
**Headless verification:** On node-1 only after inspecting task graphs to prove no client, renderer or display starts, run the isolated no-GUI proxy and backend fixture with bounded startup waits, `eula=true` readback for each Minecraft server runtime, source-unit assertions and the declared recovery matrix; logs alone do not close a residual client gate.
**Client verification:** P005-TASK-003 requires a real isolated laptop client for matching and direction-specific differing-compatible switches, resulting world/session identity, and native refusal while remaining on the source. Verify the laptop desktop and discrete GPU renderer, exact artifact/profile identities, authorized account and ready private dedicated endpoint, then automatically connect using supported launch controls or authorized desktop input. Prove the intended joined world on both client and server. No synthetic player, node-1 graphical client, global authentication weakening or singleplayer fallback may replace this gate.
**Client audio isolation:** Before launch set the disposable isolated pinned-version instance master output to zero. Use `hyprctl clients -j` to identify only the owned Hyprland address, class, title and PID, correlate its process tree to its PipeWire or PulseAudio playback stream, mute that per-application stream with `wpctl` or `pactl` and verify muted readback before input or assertions. Remute every replacement stream after transfer, reconnect, reload or device change. Never mute a default sink, another application, microphone or personal client. Missing renderer, exact identity or mute proof stops the owned client and leaves the gate open. Teardown stops its watcher/client, removes temporary audio state and verifies its stream is gone.
**Budgets and privacy:** Capture is default-off, correlation scoped and limited to 60 seconds by default, 300 seconds maximum, 200 events per second, 10,000 events, 8 MiB, 1024 queued events and two captures per process. Record reason codes and hashes rather than secrets, player content or certificate material. Diagnostic sampling is never a substitute for mandatory unsampled audit history. Treat authority unavailable, output unavailable and every control limit as visible failures; retain only sanitized evidence.
**Regression and support:** Before dependent authority acceptance, self-test `sef debug on authority registered-target 60`, `status`, `off`, denied/absent-target and every duration/rate/event/byte/queue/capture-limit rejection; rerun affected CAS, epoch and admission matrices after changes; update the authority recovery guide with exact support collection and minimal sanitized packet.

| Signal | Source and unit | Expected observation |
|---|---|---|
| `authority.commit` | Proxy writer, one state change and revision. | Commit receipt exists before matching publish; duplicate operation preserves one revision. |
| `authority.replica` | Backend replica, expected/actual u64 revision. | Only contiguous revision applies; gap triggers `RESYNC_REQUIRED`. |
| `authority.snapshot` | Proxy/backend snapshot, schema, digest and revision. | Valid atomic replacement only; corrupt payload leaves prior replica intact. |
| `presence.epoch` | Proxy presence registry, UUID, boots and u64 epoch. | Latest session wins; old session is rejected. |
| `authority.route` | Proxy route gate, source/target permission revisions. | Console/delegated actor remains distinct and revoked authority is denied. |
| `admission.decision` | Proxy admission, five profile/evidence digests and reason. | Matching or proven compatible relation admits; otherwise source remains unchanged. |

1. Register exact node-1 fixture paths, no-GUI process IDs, isolated SQLite/WAL/SHM/backup paths, laptop runtime and audio watcher where needed, logs, captures and teardown commands. Confirm the correct host project anchors, exact candidate hashes, headless task graph and concrete registered authority target. Respect the DEC-004 Velocity workspace exception and preserve owner databases. Configure and read back eula=true in each dedicated runtime; wait at most 120 seconds for each backend and 60 seconds for proxy readiness before console controls.
2. Before any dependent acceptance, use `sef.debug.manage` to run `sef debug on authority registered-target 60`, inspect `sef debug status`, invoke `sef debug off <capture-id>` and prove it stopped. Run denied, absent target and all stated cap fixtures before proceeding.
3. For the headless rows, enable a fresh capture with `sef debug on authority registered-target 60`, confirm `sef debug status`, then stimulate real bridge handlers for CAS, replica gap, snapshot corruption, migration, backup/restore, reconnect, permission revocation and isolated profile-policy fixtures. Inspect matching correlation IDs and the independent source session/world oracle. For the real compatibility rows, complete the silent laptop setup and automatically join the verified ready private endpoint within 60 seconds, prove both-side world identity, then start a fresh capture. Through actual authorized player input exercise ordinary external server switching, first matching profiles and then each independently qualified differing-compatible direction, keeping that running client unchanged. Allow at most 60 seconds for each accepted switch and 10 seconds to observe refusal with unchanged source state. Test unknown, incompatible, reversed-unproven, stale and simulated-evidence refusal. Console prepares fixtures only and cannot bypass the admission or player permission under test. After each restart prove diagnostics default off before a new capture, and after every stream recreation verify mute before continuing.
4. Restore only to an isolated target using a supported backup API result or verified quiesced checkpoint containing the database and every required WAL component. Verify independent counts and latest revisions before reuse, preserve original source, then disable any capture, preserve the required sanitized evidence packet and stop only owned processes.
5. Stop only owned clients, watchers, proxy and backend processes within a 30 second shutdown bound and verify their exit and stream disappearance. Remove exact disposable runtimes, SQLite/WAL/SHM files, backups, logs, downloads, traces, audio state and temporary databases after their final consumer; verify absence on both hosts while preserving required evidence, shared caches, personal worlds and unrelated paths.

## Verification Matrix

| Requirement or task | Static or unit | Integration | Real workflow or runtime | Negative and recovery | Execution host and prerequisites | Evidence artifact |
|---|---|---|---|---|---|---|
| SEF-REQ-012, P005-TASK-001 | State codec/schema/CAS/idempotency, migration guard and snapshot checksum tests. | Proxy writer with backend ordered replica and empty backend. | Isolated Java 25 SQLite native-load, WAL restart, supported backup API or verified quiesced checkpoint with every required WAL component, isolated restore, count/revision verification and convergence. | Crash cuts, duplicate, gap, reorder, corrupt snapshot, unknown future schema, live main-file-only copy and unavailable writer fence. | node-1 headless only, inspected graph, isolated runtime, EXT-009 and `eula=true` readback where server fixture is used. | Correlated commit/replica/snapshot and backup/restore receipt, hash and cleanup receipt. |
| SEF-REQ-013, P005-TASK-001 | UUID/session and policy-revision state-machine tests. | Bridge-backed proxy/backend presence updates. | Rapid reconnect/backend switch and offline target route through real handler entry points. | Old epoch, stale target, delegated source and revocation-between-dispatch/apply deny. | node-1 headless only; Phase 004 bridge provenance passes. | Epoch/route correlation trace and independent session oracle. |
| SEF-REQ-018, P005-TASK-002 | Relation codec, direction, reset capability and evidence-expiry validation. | Proxy admission with recorded three matching and compatible differing fixtures. | Actual server admission handler requests before any move, with independently observed source session/world on refusal and no player required. | Missing reset mod/capability, unknown, incompatible, reversed or stale evidence returns a safe, clear refusal. | node-1 headless only; Phase 000 pinned real-client feasibility remains valid, while this test claims no real-client cross-pack switching; adapter/profile fixtures and EXT-003 through EXT-005 plus EXT-010 remain revalidated. | Admission matrix, digests, source-state oracle and cleanup receipt. |
| P005-TASK-003 | `sef debug` registered-target controls and synthetic-evidence rejection. | Combined authority, presence, admission and external switch route. | Headless empty-backend convergence plus real same-profile and direction-specific differing-compatible client switches through SEF admission. | Writer loss, unknown/incompatible/reversed-unproven/stale profiles and synthetic qualification refuse; no source displacement or false support claim. | node-1 headless topology and silent laptop on the verified private endpoint, exact client/adapter/profile evidence, no graphical workaround. | Correlated policy decisions, actual client/source/destination world and session identities, refused-source receipts, directed evidence bindings and both-host cleanup. |

## Documentation, Operations, and Release

Update the root README when installation, compatibility or configuration changes, `docs/README.md`, `DOCUMENTATION.md`, and focused authority, operations, security, configuration, migration and troubleshooting topics. Document the exact proxy-only SQLite ownership, schema versions, guarded future-schema rejection, supported backup API or quiesced checkpoint with every required WAL component, isolated restore count/revision verification, authority-unavailable refusal, operator-tested directed profile relation evidence, client-pack refusal wording, session epoch troubleshooting, `sef debug` registered-target 60/status/off controls and limits, and cleanup limits. Documentation describes only implemented behavior. Wiki content follows approved merges. No public release, deployment, client project or credential collection is authorized.

## Risks and Evidence Invalidation

| Risk ID and owner task | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
|---|---|---|---|---|---|
| SEF-RISK-004, P005-TASK-001 | Commit-first CAS, contiguous replicas and atomic snapshot. | `authority.commit`, expected/actual revisions and snapshot checksum. | Fence writes, install validated snapshot and reconcile writer ledger. | Any writer, schema, bridge, SQLite artifact or candidate digest change. | Repeat crash, reorder, duplicate and corrupt-snapshot matrix. |
| SEF-RISK-003, P005-TASK-001 | Retain Phase 004 mutual identity, typed bridge and actor recheck. | `authority.route` denial reasons and zero mutation oracle. | Fence peer/session and resync known state. | Peer key, protocol, policy or identity-forwarding change. | Repeat forged/stale route and delegated actor cases. |
| SEF-LOCAL-RISK-005-A, P005-TASK-002 | Bind direction-specific server-observable profile, adapter and operator-tested evidence digests before admission. | `admission.decision` reason and independent source-state oracle. | Refuse, retain source, refresh evidence only after validation. | Pack, adapter, registry/config, profile or evidence digest change. | Repeat matching, compatible-different, unknown, incompatible and stale actual-handler matrix. |
| SEF-RISK-020, P005-TASK-004 | Pair manifest, candidate hashes, phased PR gates and per-host cleanup. | Digest/profile mismatch and cleanup receipt. | Rerun affected gate, preserve blocker as open. | Merge, artifact, host or runtime identity change. | Rebuild exact pair and rerun required headless evidence. |

## Phase Completion Packet

The completion packet contains the schema and migration review, preserved-source and unknown-future-schema rejection receipt, exact dependency hashes/licenses/advisory recheck, source commits and common/protocol provenance, CAS and replica property results, Java 25 SQLite load, supported backup API or verified quiesced checkpoint with all required WAL state, isolated restore count/revision verification and crash-cut receipts, UUID epoch/routing matrix, isolated actual-handler policy matrix plus the real directed client-switch qualification and SEF admission matrix, including exact client/source/destination/adapter/profile identities and refusal evidence, `sef debug` registered-target 60/status/off and cap self-test, sanitized diagnostics and support runbook, documentation links and artifact inspection. It then records Forge PR/check/merge/result/tag first, the proxy retest against the exact approved common digest, and proxy PR/check/merge/result/tag second, plus private review subject to the established review-capability availability rule and cleanup receipts.

Before closing, enumerate every test-owned resource: candidate build output, disposable node-1 runtimes, dedicated worlds, SQLite database/WAL/SHM, backups, logs, captures, traces, downloads, coverage and temporary configurations. Stop confirmed owned processes, remove exact resources after their last consumer without symlink traversal, verify their absence on each used host and report any leftover separately. A failed or interrupted check still receives this cleanup. Source, tracked fixtures, shared caches, owner databases, personal instances, saves, branches and tags remain protected.

## Noncanonical Interface Projection

The following is derived evidence copied verbatim from `phase-005-interfaces.json`. It does not create another canonical contract. Phase 005 consumes identity, diagnostics, policy, presentation and bridge contracts, then produces `SEF-IF-006` for future Phase 006, 007, 010 and 014 adoption.

```json
{
  "phaseId": "SEF-PHASE-005",
  "interfaces": [
    {
      "id": "SEF-IF-001",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Identity and world",
        "records": {
          "Actor": {"uuid": "UUID?", "source": "PLAYER|CONSOLE|SYSTEM|MACHINE|FAKE_PLAYER", "originId": "string", "permissionRevision": "u64"},
          "Session": {"playerId": "UUID", "proxyBoot": "UUID?", "connectionEpoch": "u64", "backendId": "string", "backendBoot": "UUID"},
          "WorldRef": {"backendId": "string", "worldGeneration": "UUID", "dimension": "resource_location", "registryDigest": "sha256"},
          "Location": {"world": "WorldRef", "x": "finite f64", "y": "finite f64", "z": "finite f64", "yaw": "finite f32", "pitch": "finite f32"}
        },
        "errors": ["WORLD_REPLACED", "DIMENSION_MISSING", "REGISTRY_MISMATCH", "STALE_SESSION"],
        "ownership": "Backend creates durable world generation; authenticated proxy creates network connection epochs; display names confer no authority."
      },
      "acceptance_ids": ["SEF-AC-006", "SEF-AC-019"]
    },
    {
      "id": "SEF-IF-002",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Configuration and diagnostics",
        "records": {
          "ConfigSnapshot": {"generation": "u64", "schema": "u16", "sanitizedDigest": "sha256", "source": "string"},
          "DiagnosticEvent": {"captureId": "UUID", "correlationId": "UUID", "event": "string", "side": "BACKEND|PROXY|HARNESS", "boot": "UUID", "sequence": "u64", "tick": "u64?", "monotonicNanos": "u64", "utc": "Instant", "desired": "typed_map", "actual": "typed_map", "reason": "enum_string", "units": "typed_map", "configGeneration": "u64", "candidateDigest": "sha256"}
        },
        "methods": ["validateAndSwap(expectedGeneration:u64, proposed:ConfigSnapshot) -> Result<ConfigSnapshot>", "enable(actor:Actor, scope:string, target:string?, durationSeconds:u16) -> Result<UUID>", "status(actor:Actor, captureId:UUID?) -> Result<CaptureStatus>", "disable(actor:Actor, captureId:UUID) -> Result<CaptureStatus>"],
        "errors": ["INVALID_CONFIG", "STALE_REVISION", "DENIED", "TARGET_ABSENT", "CAPTURE_LIMIT", "OUTPUT_UNAVAILABLE"],
        "ownership": "Atomic owner-executor config swap; default-off bounded diagnostic worker, independent from unsampled audit."
      },
      "acceptance_ids": ["SEF-AC-019"]
    },
    {
      "id": "SEF-IF-003",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Command policy",
        "records": {
          "CommandContext": {"actor": "Actor", "session": "Session?", "commandId": "string", "operationId": "UUID", "targetIds": "UUID[]", "expectedRevision": "u64?", "configGeneration": "u64"},
          "PolicyDecision": {"allowed": "bool", "reason": "enum_string", "permissionRevision": "u64", "cooldownUntil": "Instant?", "confirmationRequired": "bool"}
        },
        "methods": ["authorize(context:CommandContext) -> Result<PolicyDecision>", "execute(context:CommandContext, typedArguments:typed_map) -> Result<OperationResult>"],
        "errors": ["DENIED", "HIERARCHY_DENIED", "CONFLICTING_OWNER", "COOLDOWN", "QUOTA", "CANCELLED", "STALE_REVISION"],
        "ownership": "Canonical catalog and policy; world owner rechecks before mutation; no monetary policy."
      },
      "acceptance_ids": ["SEF-AC-005"]
    },
    {
      "id": "SEF-IF-004",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Presentation and actions",
        "records": {
          "Message": {"key": "string", "locale": "string", "severity": "SUCCESS|INFO|WARNING|ERROR", "values": "map<string,Literal>", "groups": "ActionGroup[]", "page": "PageInfo?", "templateId": "string?", "templateGeneration": "u64?"},
          "ActionBinding": {"token": "opaque128", "recipient": "UUID", "session": "Session", "operation": "enum_string", "targetId": "string", "targetRevision": "u64", "snapshotId": "UUID?", "expires": "Instant", "confirmationDigest": "sha256?", "idempotencyKey": "UUID"},
          "PageInfo": {"snapshotId": "UUID", "cursor": "opaque128", "pageSize": "u16", "pageNumber": "u32", "total": "u64?"}
        },
        "methods": ["render(message:Message, audience:AudienceContext) -> PlatformComponent", "redeem(actor:Actor, session:Session, token:opaque128) -> Result<OperationResult>"],
        "errors": ["ACTION_EXPIRED", "WRONG_RECIPIENT", "STALE_SESSION", "STALE_REVISION", "DENIED", "ALREADY_APPLIED", "CONFIRMATION_REQUIRED"],
        "ownership": "Server-issued token registry; Forge native and Velocity Adventure adapters; all untrusted values literal."
      },
      "acceptance_ids": ["SEF-AC-030"]
    },
    {
      "id": "SEF-IF-005",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Authenticated bridge",
        "records": {
          "Envelope": {"protocolMajor": "u16", "protocolMinor": "u16", "senderId": "string", "recipientId": "string", "senderBoot": "UUID", "channelEpoch": "UUID", "sequence": "u64", "requestId": "UUID", "type": "allowlisted_enum", "actor": "Actor?", "session": "Session?", "expectedRevision": "u64?", "expires": "Instant", "body": "bounded_typed_bytes"}
        },
        "methods": ["send(envelope:Envelope) -> Result<TypedReply>", "rotatePeerKey(peerId:string, expectedKeyId:string, nextKeyId:string) -> Result<RotationReceipt>"],
        "errors": ["AUTH_FAILED", "REPLAY", "STALE_EPOCH", "EXPIRED", "OVERSIZED", "RATE_LIMIT", "PROTOCOL_MISMATCH", "UNREGISTERED_SERVER", "QUEUE_FULL"],
        "ownership": "TLS 1.3 mutually authenticated private direct socket; peer certificate maps to registered server; no player carrier or arbitrary command relay."
      },
      "acceptance_ids": ["SEF-AC-011"]
    },
    {
      "id": "SEF-IF-006",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Network authority and compatibility",
        "records": {
          "StateChange": {"stateKind": "BAN|MUTE|VANISH|HOME|OPERATION|VISIT|LIFECYCLE_DELIVERY", "key": "string", "revision": "u64", "previousRevision": "u64", "operationId": "UUID", "tombstone": "bool", "value": "typed_record?"},
          "Presence": {"session": "Session", "profileId": "string", "profileDigest": "sha256", "state": "CONNECTING|READY|TRANSFERRING|DISCONNECTED"},
          "CompatibilityRelation": {"clientProfileDigest": "sha256_of_server_observable_negotiated_profile", "sourceProfileDigest": "sha256", "destinationProfileDigest": "sha256", "adapterDigest": "sha256", "evidenceDigest": "sha256", "allowed": "bool"}
        },
        "methods": ["compareAndSet(actor:Actor, change:StateChange) -> Result<CommitReceipt>", "snapshot(afterRevision:u64) -> Result<StateSnapshot>", "resolve(observer:Actor, targetId:UUID) -> Result<Presence>", "admit(session:Session, destinationId:string) -> Result<AdmissionDecision>"],
        "errors": ["STALE_REVISION", "AUTHORITY_UNAVAILABLE", "RESYNC_REQUIRED", "TARGET_NOT_VISIBLE", "INCOMPATIBLE_PROFILE", "UNKNOWN_PROFILE"],
        "ownership": "One SQLite writer; backend ordered immutable replicas; deny network mutation or unsafe admission while authority is unavailable. Visit and lifecycle-delivery kinds are reserved here and remain non-dispatchable until the typed Phase 006 handlers are registered; its implementation uses the existing serialized authority writer."
      },
      "acceptance_ids": ["SEF-AC-012", "SEF-AC-013", "SEF-AC-018"]
    }
  ]
}
```

## Next Transition

After all Phase 005 implementation, evidence, documentation, Forge PR/check/merge/result/tag, proxy retest against the exact approved common digest, proxy PR/check/merge/result/tag and cleanup gates pass, advance sequentially to SEF-PHASE-006. Its required entry action is to consume the verified authority and presence substrate before implementing moderation, observer-aware visibility, and only then its own lifecycle handlers. This blueprint is a proposed execution contract, not executed verification or a whole-product completion claim.
