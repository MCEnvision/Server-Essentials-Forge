# Phase 016 Execution Plan

> **Plan ID:** PLAN-PHASE-016  
> **Phase ID:** SEF-PHASE-016  
> **Owner:** Delivery integration  
> **Classification:** MANDATORY  
> **Master plan:** [plan.md](../plan.md)  
> **Phase sequence:** 016 of 016

## Purpose and Ownership

This phase closes the operator delivery and approved paired artifact endpoint only after every implementation and assurance gate is complete. It owns the exact operator documentation rehearsal and sequential checked integration of the Forge Java 17 artifact on `forge-1.20.1` and the Velocity Java 25 artifact on `velocity-latest`. It introduces no new feature scope and does not redefine earlier acceptance. Mandatory in-scope defects discovered during final proof follow the current-phase correction and integration rules below. The master remains the authority for all 36 requirements, decisions, risks, interfaces, exclusions, and Definition of Done. This blueprint owns only the delivery evidence that proves final artifacts, documentation, and integrations still match that contract.

## Evidence-Based Entry State

| Evidence class | Area | Finding | Source or command | Freshness condition |
|---|---|---|---|---|
| OBSERVED | Destination baseline | Forge destination is Minecraft 1.20.1, Forge 47.3.12 and Java 17; the target README, Gradle files and declared source fingerprints are frozen evidence. | `SRC-002`, [research evidence](../research/evidence.json) | Any source, loader, dependency or artifact change requires a new final pair manifest and affected reruns. |
| OBSERVED | Product topology | One Java 25 Velocity proxy and three Forge backends are the approved topology; SEFPORTED is read only and no client component is permitted. | `SRC-001`, `SRC-004`, `DEC-001`, `DEC-002`, `DEC-004`, `DEC-005` | Branch, host, profile, adapter, server count or runtime identity drift invalidates topology and clean-install evidence. |
| REQUIRED AT ENTRY | Assurance dependency | Phase 015 must supply a complete assurance packet for SEF-AC-001 through SEF-AC-020 and SEF-AC-023 through SEF-AC-036, a complete 36-row catalog that marks SEF-AC-021 and SEF-AC-022 pending this phase, no unresolved mandatory product defect, cleanup registers, and Forge-first applicable integration receipt. | SEF-PHASE-015 exit packet | A missing, stale, candidate-bound, or cleanup-incomplete receipt blocks this phase. SEF-AC-021 and SEF-AC-022 are intentionally not entry-complete. |
| PROPOSED | Final documentation outputs | README, documentation index, technical overview, operations, migration, audit, RTP, lifecycle, compatibility, diagnostics and verification guides will describe only the merged, retested artifacts. | SEF-REQ-021, P016-TASK-001 | Every statement must be traced to final artifact or final runtime evidence, never a premerge candidate. |
| PROPOSED | Final provenance output | A retained paired manifest will bind Forge and Velocity source commits, artifact bytes, common and protocol digests, dependency and license inventory, SHA256, SHA512, SPDX SBOM and applicable supported attestations. | SEF-REQ-022, P016-TASK-002, P016-TASK-003 | Any byte, commit, common contract, protocol, dependency or license change invalidates the complete provenance packet. |

## Scope Boundaries

### Included Scope

- SEF-REQ-021 and SEF-AC-021: reconcile and rehearse exact operator and developer documentation against the final approved pair, including clean installation, registration, forwarding, secure bridge configuration, commands, permissions, configuration, diagnostics, migrations, backup, audit retention and loss handling, queries, preview, rollback, restore, undo, privileged give, compatible profile refusal, homes, moderation, vanish, lifecycle messages, local and proxy RTP, recovery, support and artifact pairing.
- SEF-REQ-022 and SEF-AC-022: conduct the required checked, sequential integrations and resulting branch verification, signed annotated tags, final rebuild and provenance capture for every applicable Forge and Velocity change.
- Final evidence binding for all prior requirements, all 16 preceding phase gates from P000 through P015, residual laptop claims, per-host cleanup, documentation to wiki correspondence after approved merges, and plan-wide Section 18 closure review. The global sequence has 17 phases including this terminal Phase 016.

### Explicit Exclusions

- This phase introduces no new feature scope, production migration, release upload, public GitHub release, production rollout, public announcement, new client or launcher project, goal authority or plan rewrite. If final verification finds a mandatory in-scope defect, it is corrected on the applicable current Phase 016 branch through ordinary checks, review, merge, final rebuild, tag and affected retests; it is neither deferred nor repaired on a historical phase branch. Disposable test configuration, isolated migration rehearsal and their protected cleanup are permitted.
- Economy, GUI, HUD, Fancy Tags, custom screens, client protocol, enhanced rendering, menu-dependent features and arbitrary incompatible modpack transfer remain excluded under SEF-REQ-003, SEF-REQ-004, DEC-001, DEC-002 and DEC-007.
- This final phase does not create, mutate, retire or advance a saved goal, active cursor, plan or a nonexistent next phase. It does not delete planning records or historical branches and tags.

## Phase Contract

### SEF-PHASE-016 — Complete Operator Delivery and Verify Final Approved Paired Artifacts

**Objective:** Produce operator documentation and a final, reproducible, paired delivery packet that proves the merged Forge and Velocity artifacts meet every required acceptance gate without public release or production rollout.  
**Owner:** Delivery integration  
**Dependencies:** SEF-PHASE-015, EXT-002  
**Canonical requirements:** SEF-REQ-021, SEF-REQ-022  
**Documentation and release impact:** Update only documentation that describes final verified behavior. Publish a matching wiki navigation and operator update only after each approved tracked-documentation merge. Retain artifacts and evidence for packaging and audit. No public release or production deployment occurs.  
**Next transition:** Final plan-wide closure under Section 18; no next phase exists.

**Entry criteria**

- Phase 015 task 001 through Phase 015 task 005 completion packet is complete and bound to exact candidates for SEF-AC-001 through SEF-AC-020 and SEF-AC-023 through SEF-AC-036. Its 36-row catalog explicitly retains SEF-AC-021 and SEF-AC-022 as pending P016-TASK-001 through P016-TASK-004, with no known mandatory product defect, failed required check, open required client gate, or cleanup failure.
- Every preceding phase P000 through P015 has an applicable checked merge receipt, resulting product branch verification, signed annotated tag receipt and required evidence. Forge canonical common work is checked and merged first where a pair depends on it.
- `EXT-002` account, GitHub scope, branch protections and registered EnVisione SSH signing capability are revalidated without handling credentials; missing authority blocks integration rather than substituting a direct product branch push.

**Implementation scope**

- Implement SEF-REQ-021, SEF-REQ-022 through the work packages and acceptance obligations below.

- P016-TASK-001 turns final proven behavior into accurate tracked documentation and rehearses it using the exact merged artifact pair, actual isolated database fixtures and actual no-GUI server evidence. Trace: SEF-PHASE-016.
- P016-TASK-002 performs the sequential Forge then exact Velocity integration closure, verifies merge commits and signed tags, and reruns affected final evidence on the actual merged pair rather than treating premerge evidence as proof. Trace: SEF-PHASE-016.
- P016-TASK-003 creates the reproducible paired manifest, checksum, license, SBOM and supported-attestation inventory from final bytes, then validates packaging and artifact exclusion boundaries. Trace: SEF-PHASE-016.
- P016-TASK-004 audits all prior gate receipts, final operational proof, residual client evidence, retained evidence and verified cleanup against Section 18. Trace: SEF-PHASE-016.

**Execution order**

1. `P016-TASK-001` reconciles final docs and runs clean-install and operator runbook rehearsals from final candidate inputs, with documentation statements rejected until proven. Trace: SEF-PHASE-016.
2. `P016-TASK-002` closes the required Forge canonical common integration first, confirms its merge result and tag, then builds, tests, checks, merges, verifies and tags the exact Velocity projection without stacking or direct base pushes. Trace: SEF-PHASE-016.
3. `P016-TASK-003` rebuilds both final artifacts after their resulting branch commits, produces final provenance and package inspection results, and retains deliverables through audit completion. Trace: SEF-PHASE-016.
4. `P016-TASK-004` performs the final Section 18 receipt review, publishes only postmerge wiki correspondence, verifies per-host cleanup, records remaining blockers if any, and closes tracking only when the owner-selected endpoint is proven. Trace: SEF-PHASE-016.

**Required evidence**

- Exact final commit and merge identities, checked PR results, signed-tag verification, final artifact bytes, common and protocol provenance, SHA256 and SHA512, license inventory, SPDX SBOM and applicable supported attestation records.
- Clean-install and runbook rehearsal evidence from exact final pair, real MySQL and real MariaDB fixtures, dedicated server and actual server evidence, with laptop evidence limited to named residual input, rendering and synchronization claims.
- A requirement and phase-gate closure matrix for SEF-REQ-001 through SEF-REQ-036 and P000 through P015, linked to final evidence or precise open blocker.
- Per-host process, audio-stream, temporary database, runtime, report and scratch cleanup receipts, plus retained sanitized evidence readability checks.

**Exit criteria**

- The complete plan-wide Definition of Done and owner-selected completion endpoint in master Section 18 pass, including all final evidence and cleanup gates.

- No known mandatory phase-owned defect remains. Every such defect must be fixed and its affected evidence rerun before phase closure.

- SEF-AC-021 documentation and clean-install/runbook rehearsals are complete against exact final artifacts, documentation link checks pass, and postmerge wiki correspondence agrees with tracked documentation.
- SEF-AC-022 applicable branch integrations are checked, merged through merge commits, fetched and verified on `forge-1.20.1` and `velocity-latest`, signed and tagged; no direct base push or stacked phase was used.
- Both final artifact manifests and all required provenance are complete, final retests bind to actual merged pair identities, all consumer audits have finished, and no known mandatory defect, unverified mandatory receipt or cleanup failure remains.

## Inputs and Upstream Contracts

| Input or contract | Provider | Required state | Validation | Failure behavior |
|---|---|---|---|---|
| Complete assurance packet | SEF-PHASE-015 | SEF-AC-001 through SEF-AC-020 and SEF-AC-023 through SEF-AC-036 are bound to a fixed candidate with cleanup records. The 36-row catalog leaves SEF-AC-021 and SEF-AC-022 pending this phase. | Read each receipt, candidate digest, host, config, engine and recovery matrix; reject any missing or stale completed entry. | Stop final closure, route an implementation defect to the current Phase 016 branch when in scope, or report the exact earlier owning phase blocker; do not patch documents around it. |
| Shared configuration and diagnostics | SEF-IF-002 from SEF-PHASE-001 | Default-off bounded captures and final command grammar are delivered and documented. | Self-test `sef debug on`, `sef debug status`, `sef debug off` from console against final backend; inspect capture limits and output. | Documentation and dependent rehearsal remain open until actual grammar and signals match. |
| Lifecycle template and visit contract | SEF-IF-015 from SEF-PHASE-006 | Network and local authority, visit ledger, claims, visibility and last-good reload semantics are finalized. | Rehearse all four families, variants, placeholders, UUID visits and claim recovery against final pair. | Preserve last-good documentation and report a requirement blocker; never imply a failed dispatch was delivered. |
| Paired common and protocol contract | SEF-PHASE-004 through SEF-PHASE-014 | Forge and Velocity artifacts report the same approved common and protocol digest and valid adapter and profile identity. | Final pair manifest compares source commits, common/protocol bytes, Forge 47.3.12 Java 17 and Velocity Java 25 runtime identities. | Refuse installation, transfer and closure evidence for a mismatched pair; rebuild and rerun affected gates. |
| External signing and integration capability | EXT-002 | EnVisione identity, allowed protected-branch workflow and SSH signing remain available. | Verify identity, repository rules, PR checks, merge commit containment and tag signature without exposing credentials. | Leave integration and closure open; never direct-push a product branch or use unsigned fallback. |

## Outputs and Downstream Contracts

| Output or contract | Consumer | Guaranteed state | Compatibility or versioning | Evidence |
|---|---|---|---|---|
| Final tracked operator and developer documentation | Operators, maintainers and postmerge wiki readers | Exact commands, defaults, limitations, recovery and evidence boundaries match final artifacts and rehearsals. | Each instruction names Forge Java 17 or Velocity Java 25 scope and rejects incompatible profiles before transfer. | Clean-install transcript, link check, final config and command outputs, postmerge wiki comparison. |
| Final paired provenance manifest | Operators, maintainers and audit reviewers | Binds each final artifact to source and merge commit, common/protocol digest, hashes, dependencies, licenses, SBOM and applicable attestations. | Forge and Velocity entries are separate, versioned and mutually cross-referenced; no floating latest selector is accepted. | Independent recomputation of hash and manifest fields from retained bytes. |
| Final plan-wide closure packet | Owner and repository tracking | All 36 requirement receipts and P000 through P015 gates are either final-pair verified or explicitly open. | Records final phase as terminal and never names a fabricated successor. | Section 18 audit, checked integration receipts, cleanup registers and issue or milestone state. |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
|---|---|---|---|---|---|---|
| P016-TASK-001 | SEF-REQ-021, SEF-REQ-019, SEF-REQ-020, SEF-REQ-030, SEF-REQ-036 | Reconcile final README, documentation index, technical overview and focused operations, migration, audit, restoration, RTP, lifecycle, compatibility and troubleshooting guides. Rehearse each documented clean install and recovery path from exact final Forge and Velocity artifacts. | P015 packet, SEF-IF-002, SEF-IF-015, final candidate manifests, EXT-001, EXT-006 through EXT-008. | Reviewed documentation change set, command and configuration catalog, clean-install and runbook rehearsal receipts, documented known coverage gaps and recovery limits. | Planned final `README.md`, `docs/README.md`, `DOCUMENTATION.md`, diagnostics, operations, audit, migration, RTP, lifecycle, compatibility and verification topics. | Final docs link check plus actual isolated installs and runbooks show documented behavior and failure handling. No source inspection alone closes this task. |
| P016-TASK-002 | SEF-REQ-022, SEF-REQ-020 | Close applicable Forge canonical common integration before Velocity work, then close the exact Velocity projection sequentially through checked PR merge commits. Use the owner-approved `/mnt/hermes/projects/SefVelocity` linked worktree only after its Phase 004 ownership, content and lineage rechecks. Verify resulting branch containment, signed annotated phase tags, and retest on final merged pair. | P016-TASK-001 documentation diff, Phase 015 task 001 through Phase 015 task 005, EXT-002, final review and check receipts, DEC-004 approved Velocity workspace. | Forge and Velocity checked merge receipts, final source commits, signed tags, postmerge rebuild and rerun receipts, branch containment proof. | `forge-1.20.1`, `velocity-latest`, owner-approved linked worktree, common/protocol provenance and phase milestone or issue tracking. | Fetch exact product branches, verify merge commits and signed tags, recheck the approved worktree as a linked worktree rather than a clone, rebuild final pair, then rerun clean-install, diagnostic self-test, topology and affected recovery scenarios. |
| P016-TASK-003 | SEF-REQ-021, SEF-REQ-022 | Generate and inspect final paired artifact delivery records after every final consumer of candidates is known. Capture SHA256, SHA512, source pair manifest, licenses, SPDX SBOM and applicable supported attestations. Reject excluded surfaces and accidental local material. | P016-TASK-002 resulting commits and artifacts; P015 artifact and removal inventories; dependency and license evidence. | Retained artifact pair, checksum files, source-pair manifest, license inventory, SPDX SBOM, supported attestation links or recorded unsupported rationale, package inspection receipt. | Forge server artifact, Velocity companion artifact, adapter declarations, documentation artifact references. | Recompute checksums from final bytes, inspect archives for no SEF client, economy, GUI, HUD, Fancy Tags, credentials, absolute paths, caches or unrelated files, and compare every manifest field. |
| P016-TASK-004 | SEF-REQ-021, SEF-REQ-022, SEF-REQ-020, SEF-REQ-027, SEF-REQ-035 | Audit final receipts against every requirement, the 16 preceding phase exits from P000 through P015, and Section 18. Publish wiki correspondence only after approved documentation merge. Close milestones, issues and tracking only after final branch and cleanup verification. | P016-TASK-001 through P016-TASK-003, all P000 through P015 completion packets, final cleanup registers. | Plan-wide closure matrix, postmerge wiki correspondence, final tracking record, retained sanitized evidence index and cleanup verification. | Requirement traceability, phase receipts, wiki, project tracking, release-facing documentation and final evidence store. | Independent review of 36 requirement rows, 16 predecessor phase rows, final Phase 016 gates, final pair identity and per-host cleanup. Any missing gate remains explicit and prevents closure. |

Task ordering is strict. P016-TASK-001 may prepare documentation from a candidate but cannot certify it before P016-TASK-002 final merged-pair retests. P016-TASK-003 retains all artifacts and decisive evidence until P016-TASK-004 completes its audit. A documentation-only correction after an integration changes the merged pair and requires the corresponding final rebuild, documentation rehearsal, checked merge, tag and affected retest; it is not an exception to sequential integration.

## Architecture and Implementation Boundaries

This phase does not add architecture. It documents and verifies the established separation: Forge Java 17 remains server-only and owns world mutation, safe teleport, signed-chat mute enforcement, local lifecycle scope and final RTP safety. Velocity Java 25 remains a small companion that owns proxy admission, authenticated presence, network authority and eligible routing. The mutually authenticated private bridge operates with empty backends, key rotation, bounded typed messages and no player carrier or arbitrary console relay. Proxy SQLite network authority remains separate from MySQL and MariaDB audit history, and neither may be represented as a shared database file. The approved `/mnt/hermes/projects/SefVelocity` path is a placement exception only: after Phase 004 rechecks exact contents and active ownership, it may be the linked worktree for the applicable `envy` branch rooted in `velocity-latest`; it is never a second repository, a recursive copy, a shared-worktree branch switch or disposable cleanup target. Forge remains canonical for common and protocol content, and Velocity consumes its exact-digest projection.

The documentation must preserve the live trust and data boundaries: qualified homes contain backend and world generation identity; incompatible or unknown profile pairs refuse before movement; transfer success requires a durable arrival receipt; signed-chat mute stays enforced at the backend; vanish filters every observer-facing surface; audit history states watermarks, visible gaps and redaction reason; restoration uses fixed selection, preview, confirmation, preimages, conservative conflicts, undo and distinct privileged give; and RTP only consumes durable turns after confirmed arrival while retaining strict distance, reservations, recent landings, leases and uncertainty quarantine. Documentation may not claim universal third-party pack support, invisible mod-internal audit capture, universal reversibility, unlimited storage, or a client component.

Final deliverables must distinguish verified values from configurable values. The main configuration describes all four independently enabled lifecycle families, one to 32 variants, typed placeholders including `{player_name}`, literal rendering, no-immediate-repeat or round-robin selection, UUID visits, previous completed visit semantics, network deduplication, visibility-filtered audiences, restricted preview and atomic last-good reload. It must not invent scripts, arbitrary console actions, a graphical editor, or a separate lifecycle authority.

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
|---|---|---|---|---|
| Final documentation refers to a premerge artifact or obsolete command | Candidate digest, command output or final config differs from the documentation receipt. | Mark the statement unverified and block SEF-AC-021. | Correct the tracked document, merge it through the required branch path, rebuild exact final pair and repeat its rehearsal. | P016-TASK-001 clean-install and `sef debug` self-test after final merge. |
| Forge common merge changes the projection consumed by Velocity | Common/protocol digest or final pair manifest mismatch after Forge result. | Do not build, tag or merge Velocity against old projection. | Rebase or update only from resulting Forge product branch, rebuild and rerun interface and topology receipts. | P016-TASK-002 source containment, pair digest comparison and compatible/refusal matrix. |
| Required check, review, signature or protected merge is unavailable | PR receipt is missing, check fails, merge type is not merge commit, signing verification fails, or branch containment fails. | Final integration remains open with no direct branch push, unsigned tag or stacked branch. | Resolve the check or signing issue in the applicable phase branch and rerun the exact integration gate. | P016-TASK-002 fetched result, `git verify-commit` and `git verify-tag` evidence. |
| Clean-install documentation conceals configuration or security error | Fresh fixture fails registration, forwarding, mTLS bridge, adapter identity, profile validation or private endpoint setup. | Fail before world traffic and expose safe localized operator reason; do not log secrets. | Correct documentation or configuration validation, retain sanitized decisive evidence and rehearse from a new empty fixture. | P016-TASK-001 final pair installation and zero-player bridge readiness. |
| Audit recovery runbook falsely promises data completeness | Real MySQL or MariaDB backup, migration, manual purge, emergency local rotation, spool loss or restoration job reports gap or uncertainty. | Documentation exposes watermarks, loss extent, engine scope, recovery steps and refusal boundaries. | Preserve source or backup, resume or restore only matched snapshot, quarantine uncertain restoration, and revise docs only after evidence. | P016-TASK-001 both-engine backup/migration/manual-purge/emergency-loss/alert rehearsal and P015 conservation sheets. |
| Final runbook weakens a privacy or safety invariant | Mute, vanish, lifecycle, profile, click token, redaction, restoration or RTP scenario contradicts document text. | Treat the document as unsafe, leave closure open and retain protection. | Correct the artifact or documentation at the owning scope, rerun named real path and all dependent final docs. | P016-TASK-001 actual signed chat, observer, lifecycle, transfer, restore and RTP evidence. |
| Evidence is stale due to final merge, host or artifact drift | Manifest, renderer, stream, Java, loader, DB driver, server profile, config generation or source commit differs. | Reject all affected premerge candidate evidence. | Recreate only exact required final fixture and rerun narrow plus dependent gates. | P016-TASK-002 and P016-TASK-004 binding audit. |
| Cleanup or retained evidence handling fails | Owned process or stream remains, temporary path exists, required final artifact is absent or unreadable. | Cleanup is incomplete and endpoint remains open. | Stop only identified owned processes, retain required deliverables, remove exact known disposable paths after final consumer, verify absence on each host. | P016-TASK-004 per-host cleanup register and retained evidence readback. |

## Diagnostics and Debugging

**Requirement IDs:** SEF-REQ-019, SEF-REQ-020, SEF-REQ-021, SEF-REQ-022, SEF-REQ-027, SEF-REQ-035, SEF-REQ-036
**Task IDs:** P016-TASK-001, P016-TASK-002, P016-TASK-003, P016-TASK-004
**Controls:** Use delivered `sef debug on <category> [target] [seconds]`, `sef debug status`, `sef debug off <capture-id>` and `sef debug off all`; console omits `/`, requires `sef.debug.manage`, uses an explicit authorized target where required, defaults to 60 seconds and caps at 300 seconds.
**Signals:** Bind SEF-IF-002 identity fields and `capture.status`, `command.decision`, `bridge.decision`, `authority.commit`, `transfer.transition`, `visibility.decision`, `audit.watermark`, `audit.loss`, `restore.step`, `rtp.allocation`, `rtp.safety`, `rtp.route`, `lifecycle.decision`, final artifact digest, branch commit, cleanup status and documentation revision to one correlation record.
**Collection procedure:** Follow the numbered final-pair collection procedure below, beginning with debug self-tests and ending with disabled-capture proof, sanitization, retained-evidence readback and exact resource cleanup.
**Headless verification:** On node-1 inspect actual Gradle task graphs before commands, then use final no-GUI dedicated Forge servers, final Velocity proxy, console controls and real MySQL and MariaDB fixtures for configuration, bridge, storage, audit, restoration, local and proxy RTP and final installation evidence. Wait at most 120 seconds for each server, 60 seconds for proxy or database readiness, and 30 seconds for owned shutdown. A server is ready before console stimuli. The default-off diagnostics self-test completes before feature capture, and a fresh capture begins only after all fixture and client setup.
**Client verification:** Laptop evidence is limited to actual signed-chat input, native rich command rendering and actions, observer tab/entity/suggestion visibility, private preview, lifecycle recipient visibility and count privacy, destination rendering, and reconnect or synchronization. After the verified private dedicated endpoint is ready, automatically connect the matching isolated silent laptop client using version-supported launch controls or authorized desktop input, wait at most 60 seconds for join, then prove the intended client and server world identity on both sides. Headless logs never close these residual claims.
**Client audio isolation:** Before every required laptop client launch, use the discovered isolated pinned-version instance and set master output to zero before startup. Verify the active desktop and discrete renderer, bind exact owned `hyprctl clients -j` address, class, title and PID to the launched process tree, correlate only its PipeWire or PulseAudio application stream, mute it with `wpctl` or `pactl`, and verify muted readback before assertion. Reapply after each replacement stream, reconnect, resource reload or device change. Never mute default, global or system audio. Teardown stops the owned watcher and client, removes temporary routing and instance state, and verifies its stream is gone; missing identity, renderer or mute proof stops the client and leaves its gate open.
**Budgets and privacy:** Diagnostics are default off and bounded to 200 events per second, 10,000 events, 8 MiB, 1024 queued events and two captures per process. Preserve the delivered p95 CPU ceilings of one percent disabled and five percent enabled, redact credentials, addresses, private content and opaque tokens, and keep mandatory audit unsampled with explicit watermarks and capacity gaps.
**Regression and support:** Rehearse `docs/troubleshooting/diagnostics.md` from the final artifact, including self-test, denial, absent target, timeout, reload or restart, output limit, redaction, disable verification and a sanitized support packet; attach its final evidence to the documentation and closure packets.

| Signal | Source and unit | Expected observation |
|---|---|---|
| `capture.status` | Forge or proxy console, capture ID, seconds, events and bytes | Exact default-off state; enable, status and off transitions succeed for an authorized console and denial is non-mutating. |
| `bridge.decision` and `authority.commit` | Final proxy and backend workers, frame, revision and reason | Registered empty backend, replay, stale, forged and rotated-key outcomes remain distinguishable with zero unauthorized mutation. |
| `audit.watermark`, `audit.loss` and `restore.step` | Final Forge audit workers, sequence, bytes, job step, fingerprint and engine | MySQL and MariaDB evidence shows durable order, explicit loss, redaction and conservation or quarantine, never silent success. |
| `rtp.allocation`, `rtp.safety`, `rtp.route` | Final backend and proxy, cell, blocks, tickets and milliseconds | Local and proxy paths show actual-arrival-only consumption, strict separation, lease and recovery reasons. |
| `lifecycle.decision` and native client transcript | Authority ledger and targeted laptop chat receipt, UUID visit and recipient count | Four families, variants, `{player_name}`, no duplicate transfer welcome, visibility filtering and last-good reload match docs. |
| `final.delivery.binding` | Manifest auditor, SHA256, SHA512, commits, tag fingerprints and cleanup status | Final docs, artifacts, signatures, branch containment and retained evidence refer to the same merged pair. |

1. Register final Forge and Velocity commit candidates, artifact paths, nested disposable runtime and database paths, server and proxy PIDs, final evidence destinations, client roles, fixture seed and cleanup owner. Inspect task graphs and verify that every node-1 workload starts no client, renderer or display. Configure each dedicated test runtime with `eula=true` and read it back before launch.
2. Start final no-GUI server fixtures and wait at most 120 seconds for explicit readiness, then start the final proxy and each database fixture and wait at most 60 seconds for each readiness signal. Before feature fixtures, prove diagnostics are default off with `sef debug status`; run the typed console self-test `sef debug on <category> [target] 60`, `sef debug status`, one harmless matching event, `sef debug off <capture-id>`, and `sef debug status`, proving the capture stopped. Also test denial, absent target and restart default-off behavior without bypassing the command dispatcher.
3. Create the complete fixture and configure client roles. For every residual client row, use version-supported launch controls or authorized desktop input to automatically connect the matching isolated silent laptop client to the verified ready private dedicated endpoint, wait at most 60 seconds for joined-world evidence on client and server, and verify the owned mute receipt before natural input. Only after all fixture and client setup, start a fresh `sef debug on <category> [target] 60` capture and confirm its status and capture ID.
4. Execute the documented real entry point. Console establishes fixture state only and never bypasses the player permission, signed-chat input, action token, transfer, restoration or landing behavior being proved. Inspect correlated server, proxy, database and independent-world or inventory evidence within the declared 10, 20, 30 or 60 second scenario wait.
5. Run the documented negative, restart, recovery or refusal path. For audit, use actual MySQL then actual MariaDB evidence for backup, migration, manual purge, explicitly activated scheduled retention, emergency rotation, loss ledger and alerts. For lifecycle verify all four families and UUID visits. For RTP verify local and proxy durable fairness, strict distance budgets, leases, recovery and unavailable paths. If the owned application stream changes, rebind and verify mute before continuing.
6. Run `sef debug off <capture-id>` and `sef debug status`, trigger one harmless matching event, and prove the fresh capture stopped. Sanitize retained excerpts, preserving only final evidence, package artifacts and support packet under their final evidence locations.
7. After packaging and audit consume artifacts, stop only owned server, proxy, database, client and audio-watcher processes and confirm each shutdown and stream disappearance within 30 seconds; remove exact disposable paths without symlink traversal; verify absence on every host. Retain final checksums, manifests, SBOMs, attestations, documentation receipts and sanitized decisive evidence, and report any leftover separately.

## Verification Matrix

| Requirement or task | Static or unit | Integration | Real workflow or runtime | Negative and recovery | Execution host and prerequisites | Evidence artifact |
|---|---|---|---|---|---|---|
| P016-TASK-001, SEF-AC-021 | Link, command, permission, config schema and manifest reference checks | Documentation instructions against final installed pair | Clean installation, registration, forwarding, bridge, configuration, diagnostics and operator runbooks with actual server and DB results | Bad config, missing key material, profile mismatch, unavailable backend, stale docs, invalid lifecycle reload and failure-safe recovery | node-1 final no-GUI pair and real MySQL/MariaDB; laptop only named residual client rows | Rehearsal transcript, final docs revision, config digest, command outputs and cleanup receipt. |
| P016-TASK-001, SEF-AC-001 through SEF-AC-009 | Retained/removal and no-client archive scans | Forge Java 17 final artifact installation | Retained commands, no economy or interface surfaces, no SEF client join requirement | Legacy data preserved without monetary activation and excluded class or resource causes closure failure | node-1 dedicated Forge; silent laptop only actual login proof | Final artifact inventory, local server receipt and client evidence where required. |
| P016-TASK-001, SEF-AC-010 through SEF-AC-018 | Profile, protocol, UUID, revision and home state checks | One proxy and three backends with bridge key rotation and zero-player bridge | Authenticated forwarding, signed mute, vanish, qualified homes, consent travel, compatible transfer and refusal | Forged, replayed, stale, revoked-key, offline, switch, restart, unknown and incompatible profile cases | node-1 final topology; silent laptop only signed input, visibility and synchronization | Final correlated proxy, backend and client matrix. |
| P016-TASK-001, SEF-AC-019, SEF-AC-030, SEF-AC-036 | Config compiler, literal component, token, UUID visit and variant self-tests | Console diagnostics and common renderer adapters | Every command state, native rich chat, lifecycle families, previews and final reload behavior | Permission denial, injection, stale token, unknown placeholder, invalid reload, duplicate callback, claim crash and vanish privacy | node-1 plus silent laptop for native visual and input evidence | Presentation inventory, transcript, screenshot only where rendering is material, visit ledger and recipient count receipt. |
| P016-TASK-001, SEF-AC-023 through SEF-AC-029 and SEF-AC-031 | Schema, redaction, parity, query and conservation checks | Real MySQL then MariaDB backup, migration, query, alert and recovery fixtures | Capture, query, export, manual purge, optional scheduled retention, consumer versus capture pause, emergency loss, preview, rollback, restore, undo and privileged give | Full disk, spool loss, outage, tail corruption, secret sentinel, duplicate callback, conflict, crash and item conservation | node-1 no-GUI dedicated services and isolated engine fixtures | Per-engine watermarks, gaps, backup hashes, migration report, conservation sheets and cleanup receipt. |
| P016-TASK-001, SEF-AC-032 through SEF-AC-035 | Partition, permutation, generation and capacity property checks | SQLite reservations and three-backend lease routing | Local and proxy real safe arrival with durable fairness and strict distance | Boundary, stale telemetry, zero capacity, lease loss, preparation expiry, restart and uncertain arrival | node-1 server paths; silent laptop only actual destination input or rendering | Allocation, safety, route, ticket, arrival and recovery evidence. |
| P016-TASK-002, SEF-AC-022 | Branch graph, signature and manifest validation | Required PR checks, resolved conversations, merge commits and fetched containment | Rebuild and retest actual merged Forge first and exact Velocity projection second | Failed check, review finding, unsigned tag, nonmerge result, stale common digest or branch drift | Authenticated EnVisione integration capability, no direct product branch push | PR links, check results, merge commit IDs, `verify-commit`, `verify-tag`, final build and rerun receipts. |
| P016-TASK-003 and P016-TASK-004 | SHA256, SHA512, SBOM, license and traceability validators | Pair manifest and artifact archive comparison | Postmerge package inspection and Section 18 audit | Missing attestation, mismatched hash, excluded content, unreadable retained evidence or leftover test resource | Exact final artifacts and per-host cleanup registers | Final source-pair manifest, checksums, license inventory, SPDX SBOM, attestation state, closure matrix and cleanup receipts. |

## Documentation, Operations, and Release

P016-TASK-001 updates documentation only after final behavior is known and must retain normal product documentation grammar. The final documentation set includes installation and version prerequisites, server registration and private endpoint placement, Velocity forwarding and adapter verification, bridge certificate and key-rotation procedure without secret values, compatible profile registry and explicit refusal, commands and permissions, diagnostics grammar and support capture, configuration and safe reload, SQLite authority versus MySQL and MariaDB audit separation, backup, restore, migration, manual-default purge, optional schedule configuration and confirmed activation, disable/cancel and interrupted-job recovery, dependency protection, consumer versus capture pause, emergency oldest-owned-segment rotation, alerts, query completeness, restoration and give boundaries, all rich command output states, lifecycle families and UUID semantics, qualified homes, signed mute and vanish, and local and proxy RTP fairness, strict separation, budgets, leases, recovery and limits.

The clean-install rehearsal starts from a new isolated final artifact fixture and the exact final pair manifest. It proves 120 second server readiness, 60 second proxy and database readiness, 60 second client join where a residual client claim exists, 30 second owned teardown, default-off diagnostic self-test, fresh capture after fixture and client setup, real SQL engine operation, final artifact configuration, and each documented recovery command. It does not close acceptance from document inspection. Missing, ambiguous or unsafe instruction is corrected in tracked documentation and then remerged and rehearsed against the new final pair.

P016-TASK-002 uses EnVy as sole author and committer with the registered SSH signing key. It creates no direct product branch update. It first confirms the applicable Forge common change has passed checks, private independent review subject to the established review-capability availability rule, merge-commit integration, fetched `forge-1.20.1` containment, resulting branch verification and signed annotated tag. Only then does it recheck `/mnt/hermes/projects/SefVelocity` for ownership and contents, use it as the approved linked worktree of the same repository for the applicable `envy` Velocity phase branch, build and retest the exact Forge canonical projection, merge it through its checked PR into `velocity-latest`, fetch and verify its resulting commit, and create its signed annotated tag. The directory, requested artifact outputs, source history and other worktrees remain protected; only exact owned disposable children are removed after final consumers. If a paired change is Forge-only or Velocity-only, the manifest explains applicability but never claims a nonexistent integration.

Postmerge wiki updates follow tracked documentation only after the corresponding approved merge and verification. Tracking, issues and milestones close only when their acceptance evidence, final pair result and cleanup records are complete. No public release, upload, production rollout or announcement is part of this phase.

## Risks and Evidence Invalidation

| Risk ID and owner task | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
|---|---|---|---|---|---|
| SEF-RISK-020, P016-TASK-002 | Forge-first canonical common integration, exact projection, final-pair manifest and postmerge rebuild | Commit, common/protocol digest, branch, Java, loader, profile, renderer and stream binding mismatch | Reject stale result, rebuild exact pair and rerun all dependent rows | Candidate topology, client and artifact evidence | Final exact row plus dependent matrix on actual merged pair. |
| SEF-RISK-001, P016-TASK-001 | Pin adapter and profile evidence, verify forwarding and empty bridge before user flows | Login, UUID, bridge and refusal diagnostics | Preserve matrix, correct exact compatibility setup and rerun before transfer | Network and clean-install documentation evidence | Final topology, handshake, signed chat and transfer matrix. |
| SEF-RISK-003 through SEF-RISK-007, P016-TASK-001 | Preserve bridge identity, key rotation, authority revisions, signed mute, visibility and arrival fences in instructions | Typed bridge, authority, transfer and visibility records with independent oracles | Stop unsafe action, fence or reconcile state, then correct docs or artifact | Security, moderation, vanish, home and travel evidence | Named negative, restart and recovery scenario on final pair. |
| SEF-RISK-008 through SEF-RISK-012 and SEF-RISK-018, P016-TASK-001 | Per-engine rehearsal, fixed selection and independent conservation counts | Watermarks, loss ledger, migration checkpoint, backup hash, restore conflict and source or sink counts | Replay valid journal prefix, restore matched backup, quarantine uncertain job and expose gap | Audit, parity, recovery and documentation evidence | Both actual engines and affected capture or restoration family. |
| SEF-RISK-014 through SEF-RISK-017, P016-TASK-001 | Preserve successful-only consumption, exact partition, strict distance, durable reservation and fresh leases | Allocation, safety, route, ticket and arrival-receipt records | Release only proven failure, quarantine uncertainty, reject stale routing or config | RTP operations and capacity claims | Full affected cycle, safety or route fixture on final pair. |
| SEF-RISK-019, P016-TASK-004 | Retain declared workload evidence and never use sampling or weakened invariants to meet budget | p95/p99, GC, queue, fsync, query, replay and cleanup records differ from final artifact | Re-run affected workload after correction with visible bounded admission only where permitted | Performance and capacity documentation | All affected final workload and dependent acceptance rows. |
| SEF-RISK-021, P016-TASK-001 | One lifecycle authority, durable UUID visits and claims, literal templates and last-good reload | `lifecycle.decision`, visit ledger, recipient count and native transcript | Suppress uncertain dispatch, retain known visits, correct config or behavior and rerun | Lifecycle docs, privacy and client presentation evidence | All four families through local and network final paths. |

## Phase Completion Packet

- Final Forge and Velocity artifact files retained through packaging and audit, with source commits, merge commits, common/protocol digest, dependency and adapter identities, SHA256, SHA512, license inventory, SPDX SBOM and applicable supported attestation state.
- SEF-AC-021 clean-install and final runbook rehearsal record that names exact final pair, configuration digest, database engine and driver, node-1 runtime, bounded waits, server readiness, console controls, final diagnostics self-test, independent oracle and cleanup outcome.
- Final documentation diff and link check, with README, documentation index, technical overview and affected operations, diagnostics, compatibility, audit, migration, restoration, lifecycle, RTP and verification topics agreeing with actual final behavior. Postmerge wiki correspondence is recorded after its approved merge.
- SEF-AC-022 checked PR, merge-commit, review, required check, branch containment, final rebuild, retest and signed annotated tag receipts for every applicable Forge and Velocity integration; the sequence demonstrates no direct base push and no stacked phase.
- Final traceability matrix for SEF-REQ-001 through SEF-REQ-036, including all acceptance IDs, and P000 through P015 exit gates. Each row identifies final-pair proof, explicit N/A rationale permitted by the master, or an open blocker. No mandatory row is silently omitted.
- Real-path final evidence for compatible-pack refusal, bridge key rotation and empty backends, qualified homes and transfer recovery, signed-chat mute and vanish privacy, lifecycle four-family semantics, dual-engine audit backup/migration/manual and opt-in scheduled purge/emergency loss/alerts, query/restoration/preview/undo/give conservation, rich presentation, and local/proxy RTP durable fairness, strict distance, budgets, leases and recovery.
- Per-host cleanup register showing all owned test processes, watchers, streams, database fixtures, runtimes, logs, screenshots, traces, reports, downloads and scratch files have either been retained as required sanitized deliverables or removed after their final consumer. Exact leftovers remain open evidence defects.

## Noncanonical Interface Projection

The following derived evidence is copied exactly from the frozen interface projection. It does not create a competing contract.

```json
{
  "phaseId": "SEF-PHASE-016",
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

This is the terminal phase. After P016-TASK-001 through P016-TASK-004, every final-pair retest, documentation rehearsal, checked integration, signature, tag, provenance item, postmerge wiki update, tracking closure and per-host cleanup receipt passes, conduct the plan-wide completion audit against Section 18 and the owner-selected endpoint. Do not advance a cursor to a nonexistent phase, delete the plan, goal or cursor, infer public release authority, or report completion while a mandatory row, integration, residual client gate, retained artifact, or cleanup item remains open.
