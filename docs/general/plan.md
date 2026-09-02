# Server Essentials Forge 2 Final Audit and Remediation Plan

> **Plan ID:** PLAN-MASTER
> **Plan status:** VALIDATED
> **Planning mode:** EXISTING_PLAN_UPDATE
> **Project state:** EXISTING
> **Planning subject:** Server Essentials Forge 2 final security, administrator-command, UI, persistence, backend-handling, and integration audit with mandatory remediation closure
> **Requested artifact:** authoritative_plan
> **Plan profile:** software_product
> **Authoring evidence date:** 2026-09-02

This master plan is the authoritative product contract for the final audit and remediation program. It freezes global scope, requirement ownership, decisions, phase order, and completion authority. Each linked file under `phases/` will own the detailed execution blueprint for exactly one phase. Planning completion does not mean that the product audit or remediation is complete.

## 1. Project Identity

```text
Project: Server Essentials Forge 2, displayed as SEFPORTED
Mod id: sef
Primary package: com.enviouse.sef
Repository root: /mnt/hermes/projects/SEFPORTED
Starting branch: envy/phase-001-security
Starting commit: e203ece01df76cb076d9112a14a052cac6f4145d
Candidate lineage base commit: 0c75bf25c58622096dfa7cc65a5f4b32e6d60ac4
Authoritative remote:
origin
https://github.com/MCEnvision/Server-Essentials-Forge.git
Remote ref: origin/envy/phase-001-security
Remote commit: e203ece01df76cb076d9112a14a052cac6f4145d
Remote default branch at authoring: forge-1.20.1
Product version at authoring: 2.0.0
Minecraft: 1.21.1
NeoForge: 21.1.235
Java: 21
Parchment mappings: 2024.11.17
Requested artifact: authoritative_plan
```

Repository identity, package metadata, source namespace, and remote identity match the owner request. This update pass starts from the named starting commit, which matches the named remote commit and descends from the candidate lineage base selected by `DEC-006`. The older remote default branch is repository and security evidence, not an instruction to merge legacy platform work into this audit or to change the supported platform.

## 2. Planning Subject and Source Roles

| ID | Role | Subject | Source | Intended use |
|---|---|---|---|---|
| SRC-001 | owner_request | The final SEF 2 audit and remediation endpoint | EnVy's direct Plan Creator request dated 2026-09-01 | Authoritative mandatory scope, audit priorities, and requested plan artifact |
| SRC-002 | requirements | SEF 2 product, architecture, command, persistence, GUI, integration, security, and delivery contract | /mnt/hermes/projects/SEFPORTED/sef2.md | Current intended behavior and compatibility boundaries for audit expectations |
| SRC-003 | audit_evidence | Prior full codebase SEF audit findings, repairs, limitations, and release blockers | /mnt/hermes/projects/SEFPORTED/audit.md | Historical findings and evidence gaps that must be independently revalidated |
| SRC-004 | requirements | SEF 2 manual, multiplayer, integration, GUI, persistence, and release verification matrix | /mnt/hermes/projects/SEFPORTED/test.md | Existing highest fidelity runtime workflows and unresolved manual evidence |
| SRC-005 | status | Current documented implementation, security, acceptance, compatibility, and release state | /mnt/hermes/projects/SEFPORTED/README.md, /mnt/hermes/projects/SEFPORTED/DOCUMENTATION.md, and /mnt/hermes/projects/SEFPORTED/docs | Evidence based current state only, never authority over the owner request or intended contract |
| SRC-006 | repository_evidence | Current SEF source, tests, resources, manifests, build, configuration, persistence, commands, GUI, integrations, and workflows | /mnt/hermes/projects/SEFPORTED at e203ece01df76cb076d9112a14a052cac6f4145d with current CodeGraph index | Observed architecture, ownership, trust boundaries, test inventory, and planning constraints |
| SRC-007 | audit_evidence | GitHub repository, branch, pull request, Actions, ruleset, dependency alert, and security scanning state | read-only GitHub preflight for MCEnvision/Server-Essentials-Forge on 2026-09-01 | Remote identity, current dependency and verification risk, and branch evidence |
| SRC-008 | reference | Historical Forge to NeoForge porting audit and inventories | /mnt/hermes/projects/SEFPORTED/SEFAudit.md | Legacy risk history and completeness cross check only |

The planning subject is the existing SEF 2 product at its current release candidate lineage, not the production of another audit document in isolation. Existing audits are evidence and risk inputs. Existing status documents describe current claims and gaps. The plan requires independent proof against the frozen execution revision before any historical result can close a gate.

Authority follows this order: current owner decisions, this master plan, validated deterministic handoff, registered phase plans for execution detail, verified repository and runtime evidence, then status, audits, references, and inference. Current implementation cannot silently weaken intended behavior. A generated reference proves only what its generator declared unless it is reconciled with the live dispatcher, runtime, and artifact.

## 3. Purpose and Intended Outcome

Server owners and administrators depend on SEF for privileged commands, moderation, stateful administration, optional enhanced clients, and durable server data. A defect at those boundaries can disclose private information, create unintended authority, corrupt state, mislead an operator, or fail only during a restart, disconnect, provider outage, or mixed client session. Existing automated coverage and prior audits are substantial, but current status documents still record incomplete multiplayer and interactive evidence, intentionally unavailable control families, divergent historical counts, and unresolved dependency risk.

This plan produces one release candidate whose declared surfaces have been exhaustively inventoried, audited, remediated, and verified at the highest required fidelity. Primary workflows are:

1. Freeze a traceable audit baseline and map every relevant source, state, command, UI, integration, and evidence surface to one owner.
2. Find and repair sensitive data leaks, backdoor like authority paths, authorization failures, dependency risk, and unsafe trust boundary behavior.
3. Execute the complete administrator command contract, including negative and failure behavior, rather than treating registration or parsing as functional proof.
4. Audit every durable store and database like boundary through corruption, concurrency, migration, shutdown, and recovery scenarios.
5. Polish graphical and textual operator UI and prove accessibility, responsiveness, and server authority.
6. Prove backend lifecycle and cross channel handling under reload, revocation, reconnect, partial failure, and shutdown.
7. Rerun the complete clean checkout, multiplayer, compatibility, recovery, performance, artifact, and documentation matrix at one frozen revision.

The observable endpoint is defined by `DEC-003` and Section 18. The result is release readiness evidence, not authorization to publish or deploy.

## 4. Evidence Based Current State

| Area | Evidence class | Finding | Evidence |
|---|---|---|---|
| Project identity | OBSERVED | The repository builds a NeoForge 1.21.1 mod with mod id `sef`, Java 21, NeoForge 21.1.235, Parchment 2024.11.17, and artifact version 2.0.0. | `gradle.properties`, `build.gradle`, `settings.gradle`, generated mod metadata |
| Repository state | OBSERVED | The update-pass starting branch is `envy/phase-001-security` at `e203ece01df76cb076d9112a14a052cac6f4145d`, matches `origin/envy/phase-001-security`, has open phase pull request 8, descends from the `DEC-006` lineage base, and preserves the unrelated untracked `.playwright-mcp/` directory outside plan scope. | Read only Git and GitHub inspection on 2026-09-02 |
| Planning state | OBSERVED | The authoritative master, eight contiguous phase plans, plan index, and deterministic handoff already existed at the update-pass starting commit and are being integrated without changing stable scope or owner decisions. | Existing registered plan set and locked update intake |
| Source inventory | OBSERVED | The current CodeGraph index covers 512 files and was current during bounded planning inspection. | `.codegraph/` state and CodeGraph exploration on 2026-09-01 |
| Command architecture | OBSERVED | `KernelServices` prepares and seals `CommandCatalog`, registers canonical actions and shortcuts, and constructs `CommandExecutionService` over feature, permission, cooldown, cost, warmup, confirmation, execution, and audit policies. | CodeGraph exploration of [KernelServices](../../src/main/java/com/enviouse/sef/kernel/KernelServices.java), [CommandCatalog](../../src/main/java/com/enviouse/sef/kernel/command/CommandCatalog.java), and [CommandExecutionService](../../src/main/java/com/enviouse/sef/kernel/policy/CommandExecutionService.java); [technical documentation](../../DOCUMENTATION.md) |
| Command reference and coverage | OBSERVED | Current generated and status documents report 694 catalog actions and 315 shortcuts, but some older UI test prose still names 676 entries. Prior GameTests compiled representative routes and directly executed safe read only routes, but did not execute every mutating administrative workflow across every source and state. | [Command reference](../COMMAND_REFERENCE.md), [manual test plan](../../test.md), [prior audit](../../audit.md), and [acceptance ledger](../SEF2_ACCEPTANCE.md) |
| Intentionally unavailable controls | OBSERVED | Fifty nine of 75 server control schemas have runtime handlers. Sixteen named Phase 13 families are deliberately unavailable, report unavailable during preview, and cannot activate or resolve through generic state changes. | [Technical documentation](../../DOCUMENTATION.md), [README](../../README.md), [acceptance ledger](../SEF2_ACCEPTANCE.md), and CodeGraph exploration of server control execution |
| Persistence architecture | OBSERVED | `StorageCoordinator` registers versioned repositories and coordinates periodic and shutdown flush. CodeGraph reports 31 runtime implementations of `StorageRepository.audit`. Other managers and workers also own durable JSON, TOML, NBT, journals, queues, receipts, backups, and caches outside that interface. | CodeGraph exploration, [technical documentation](../../DOCUMENTATION.md), and [storage tests](../../src/test/java/com/enviouse/sef/storage) |
| Storage primitives | OBSERVED | `AtomicFileStore` and `CoalescedPersistenceWorker` provide shared publication and coalescing behavior, while repositories expose recovery, unsupported, error, ready, and closed states. | CodeGraph exploration and [storage tests](../../src/test/java/com/enviouse/sef/storage) |
| GUI and network architecture | OBSERVED | `SefNetwork`, `SefSessionManager`, `SefGuiServer`, typed payload records, `PanelActionValidator`, server projected action sets, and the `SefScreen` family provide optional enhanced UI. Server validation checks session, sequence, feature, panel, record, target, policy, and permission state. | CodeGraph exploration, [technical documentation](../../DOCUMENTATION.md), and [GUI protocol tests](../../src/test/java/com/enviouse/sef/gui/protocol) |
| Audit and redaction | OBSERVED | `AuditService` is called across kernel and policy code. `CommandRedactionPolicy` feeds command journal projection and file logging. Existing tests cover selected redaction, audit, privacy, and logging boundaries. | CodeGraph exploration; `AuditServiceTest`, `CommandRedactionPolicyTest`, `FileLogSinkTest` |
| Prior security result | OBSERVED | The tracked security review reports no unresolved finding in its reviewed scope, while the broader audit records limitations that require controlled multiplayer, provider, UI, packet, process interruption, scale, and dependency environments. These are historical claims, not proof for the future frozen execution revision. | [Security review](../SECURITY_REVIEW.md) and [prior audit](../../audit.md) |
| Dependency risk | OBSERVED | The repository had 26 open Dependabot alerts on 2026-09-01, comprising 12 high, 13 medium, and one low severity alert across transitive Maven dependencies. Applicability to the current candidate branch and packaged artifact was not established. No open code scanning or secret scanning alert was returned by the same preflight. | SRC-007 |
| Automated evidence | OBSERVED | The newest acceptance ledger claims 493 unit tests and 41 required GameTests, while the prior full audit records 487 and 38. Both report passing builds at their respective snapshots. No command was rerun during plan authoring, so neither count is `VERIFIED` for plan completion. | [Acceptance ledger](../SEF2_ACCEPTANCE.md) and [prior audit](../../audit.md) |
| Interactive evidence | OBSERVED | Multiplayer, current LuckPerms, GUI visual and accessibility, InvSee, admission capacity, disguise animation, reconnect, and selected recovery rows remain incomplete or require renewal. | [Acceptance ledger](../SEF2_ACCEPTANCE.md), [manual test plan](../../test.md), and [compatibility matrix](../COMPATIBILITY_MATRIX.md) |
| Maintained quality gates | OBSERVED | The existing Gradle build runs unit tests and packaging but the prior audit reports no maintained formatter, warning budget, static analysis, or risk based coverage gate in the build. | [Gradle build](../../build.gradle) and [prior audit](../../audit.md) |
| Release state | OBSERVED | The branch is documented as a test or release candidate preparation build. Public release acceptance remains incomplete. | [README](../../README.md), [acceptance ledger](../SEF2_ACCEPTANCE.md), and [release workflow](../RELEASE_WORKFLOW.md) |

No current behavior is labeled `VERIFIED` by this planning pass. Phase 000 must resolve conflicting counts and stale claims by freezing a new execution revision and evidence manifest.

## 5. Product Contract and Profile Coverage

| Profile area | Status | Source | Contract location | Rationale |
|---|---|---|---|---|
| Inputs and outputs | covered | SRC-001 | Inputs, Outputs, and Observable Endpoint | The owner request and existing command, GUI, storage, integration, evidence, and artifact surfaces define observable audit inputs and outputs. |
| Component architecture | covered | SRC-006 | Architecture and Ownership Boundaries | Current source establishes common, client, server, command, GUI, storage, audit, control, integration, and release component ownership. |
| State and persistence | covered | SRC-002 | State and Persistence Contract | The product requirements and repository evidence define durable stores, schemas, migrations, recovery, concurrency, and compatibility. |
| Failure taxonomy | covered | SRC-003 | Failure Taxonomy and Recovery | Prior findings and current result and repository states identify invalid input, authorization, provider, storage, recovery, concurrency, and partial-success failures. |
| Versioning | covered | SRC-006 | Compatibility and Versioning | Pinned platform, protocol, configuration, schema, mod, and artifact versions are present in build metadata and current contracts. |
| Security | covered | SRC-001 | Security, Privacy, and Supply Chain Contract | Leak, backdoor-like bypass, trust-boundary, privacy, dependency, redaction, permission, and artifact inspection are mandatory. |
| Test system | covered | SRC-004 | Verification Strategy | The repository has layered unit, GameTest, server, client, multiplayer, provider, failure-injection, performance, and artifact workflows that the plan extends and closes. |
| Release lifecycle | covered | SRC-005 | Documentation, Operations, and Release Gates | Build, packaging, evidence, compatibility, documentation, rollback, and release-readiness gates are in scope, while public publication is excluded. |
| Generalization | covered | SRC-002 | Supported Environments and Generalization | Evidence must cover dedicated server, enhanced and fallback clients, mixed multiplayer, console and other supported sources, and optional integrations present and absent. |
| Determinism | covered | SRC-004 | Determinism and Evidence Reproducibility | Inventories, generated references, normalized persistence, audit matrices, clean-checkout commands, and artifact digests must reproduce at the frozen revision. |

### Inputs, Outputs, and Observable Endpoint

In scope inputs include player, console, RCON, command block, function, scheduled, GUI, alias, bundle, sudo, and server profile command requests where the catalog permits them; typed enhanced client payloads; configuration and migration files; world and server persistent data; optional provider decisions; registry content; lifecycle events; dependency metadata; and release candidate source and resources. Every input is untrusted until the owning server boundary validates its type, size, source, permission, revision, and current policy.

In scope outputs include domain mutations, command feedback, suggestions, graphical screens, HUD state, audit events, redacted observation records, logs, exports, persistent files, migrations, backups, recovery states, generated references, build reports, test evidence, and the packaged JAR. Outputs must reveal only authorized information, identify failure without exposing secrets, and match the final documented contract.

The endpoint is one frozen candidate revision and one artifact digest that satisfy all mandatory requirements, every phase exit, and Section 18. A passing parse, registered command, present source file, historical audit, or headless startup alone cannot satisfy the endpoint.

### State and Persistence Contract

The logical server owns all authoritative gameplay, administration, permission, session, and durable state. Clients own presentation caches only. Durable scope includes every `StorageRepository`, configuration document, world player data adapter, JSON, TOML, NBT, object store, queue, journal, receipt, migration marker, recovery record, backup, audit file, optional log, and durable cache that can change later behavior. An inventory based only on `StorageRepository` is incomplete.

Every durable owner must declare its path, schema or envelope version, identity and cardinality, size and record bounds, write and flush model, concurrency model, idempotency contract, migration rules, unsupported newer version behavior, corruption behavior, backup and restore path, privacy classification, retention, shutdown behavior, and evidence. Unsupported or damaged authoritative data must not become an empty successful state. A repair that changes a schema must preserve supported data through a versioned migration and recovery copy.

Transient state includes sessions, sequence counters, confirmations, warmups, temporary permission grants, selected GUI targets, open menu authorization, projections, transfers, rate limits, and in flight persistence work. Disconnect, reload, provider refresh, world change, and shutdown must invalidate or settle transient state according to its owner contract. Transient state must not mint durable authority or survive beyond its declared lifecycle.

### Failure Taxonomy and Recovery

| Failure class | Required behavior | Operator evidence | Recovery contract |
|---|---|---|---|
| Invalid or oversized input | Reject before mutation or unsafe decode. Return bounded, actionable feedback where disclosure is safe. | Sanitized validation result and stable action or field identifier | Correct the input and retry. No rollback is needed because no mutation occurred. |
| Authorization or policy failure | Fail closed at discovery and execution boundaries. Recheck immediately before mutation. | Decision source, permission or policy identifier, actor class, and correlation id without sensitive arguments | Restore valid authority or policy, refresh revisions, and submit a new request. Stale confirmations are never reused. |
| Optional provider absent or failed | Preserve documented internal fallback or explicitly disable the provider owned capability. Never grant because a provider failed. | Provider state and bounded diagnostic | Restore the provider and refresh, or continue through the documented fallback. |
| Local dependency or registry failure | Reject unsupported content or adapter capability without corrupting unrelated state. | Namespaced identifier and dependency state | Install or restore a compatible dependency, or use supported content. |
| Persistence validation or corruption failure | Enter explicit recovery, unsupported, or error state. Block writes that could destroy evidence. | Store id, schema state, recovery path, and sanitized cause | Quarantine or restore from validated recovery material, then revalidate before reopening writes. |
| Persistence publication failure | Preserve or restore the last known good state. Distinguish not committed, committed, and outcome unknown. | Revision, operation id, journal or receipt state, and flush health | Retry only idempotent work. Resolve outcome unknown records before another side effect. |
| Concurrency or stale revision | Reject stale mutation and preserve the newer state. | Expected and observed revisions without sensitive payloads | Reload current state and create a new preview or transaction. |
| Partial multi component success | Stop dependent work, preserve compensation or recovery state, and never report full success. | Per component result under one correlation id | Execute the declared compensation or recovery path, then rerun the complete invariant check. |
| Network, session, or replay failure | Drop or reject stale, forged, replayed, incompatible, or out of order requests before mutation. | Session safe rejection and abuse counters | Negotiate a new session or fall back to commands. Old identifiers remain invalid. |
| Lifecycle interruption | Bound shutdown and background work, preserve incomplete markers, and fail startup closed where state is ambiguous. | Worker, repository, and marker status | Complete recovery before enabling the affected domain. |
| Verification or evidence failure | Mark the row failed or blocked. Do not substitute a lower fidelity result. | Exact command or workflow, environment, revision, and decisive failure | Repair the product or environment and rerun every invalidated row. |

### Security, Privacy, and Supply Chain Contract

A backdoor like path means any undocumented or unintended mechanism that grants authority, executes a command, reads or mutates protected data, escapes an owned filesystem path, persists privilege, bypasses hierarchy or exemption, weakens confirmation, reaches a server context, or exposes a hidden identity outside the declared contract. The audit does not presume malicious intent. It must nevertheless search for these paths across direct calls, reflection, mixins, aliases, bundles, profiles, GUI actions, payload handlers, lifecycle callbacks, integrations, configuration, migration, recovery, and packaging.

Sensitive data includes credentials, tokens, private messages, raw or linkable network addresses, hidden identities, moderation reasons where restricted, security evidence, command arguments covered by redaction, private GUI projections, filesystem paths that reveal host details, and provider metadata beyond the viewer contract. Sensitive data must not leak through feedback, suggestions, chat, HUD, GUI, audit, command spy, optional file logs, exports, exceptions, metrics, generated documentation, test fixtures, or the JAR.

Security review must include source to sink tracing, permission and source matrices, bypass inventory, payload and parser bounds, filesystem ownership, symlink and detectable hard link handling, archive and image parsing, deserialization depth, concurrency and time of check behavior, persistent grants, audit integrity, optional provider failure, reflection containment, mixin and access transformer scope, dependency reachability, artifact contents, secret shape scans, and adversarial runtime tests. Any confirmed authorization bypass, sensitive data leak, or backdoor like route is mandatory remediation regardless of severity label.

Supply chain closure must evaluate the resolved candidate graph and the packaged runtime artifact, not only GitHub alert state. Every current critical or high alert must be repaired or shown inapplicable with exact dependency path, configuration, runtime packaging, affected API, reachability, and authoritative advisory evidence. A platform pin cannot be silently changed to clear an alert. If no compatible remedy exists and the vulnerability is applicable and exploitable, completion is blocked.

### Supported Environments and Generalization

Mandatory evidence covers NeoForge 21.1.235 on Minecraft 1.21.1 and Java 21, a dedicated server, enhanced GUI disabled and enabled, a matching enhanced client, a no SEF fallback client, an incompatible protocol fixture, and enhanced and fallback clients connected together. It covers clean and migrated synthetic data, fresh and restarted servers, multiple dimensions, online and known offline identities, registry content outside the `minecraft` namespace, and every catalog allowed command source. Current optional integrations must be tested present, absent, unavailable, and removed according to their declared ownership, including live permission provider behavior for the advertised LuckPerms contract.

Fixtures must not hard code one player, dimension, namespace, screen size, operating path, or provider response in a way that hides general behavior. Permission tests include absent, explicit deny, explicit grant, inherited or wildcard state, refresh, reconnect, and outage. Target tests include self, online, offline, equal rank, higher rank, exempt, vanished, unknown, ambiguous, and stale identities where applicable. UI tests include graphical and command fallback paths. Persistence tests include empty, valid, legacy, unsupported newer, malformed, oversized, interrupted, concurrent, and recovered states.

### Determinism and Evidence Reproducibility

At one frozen revision, repeated generation must produce the same normalized command, permission, configuration, storage, UI, trust boundary, and dependency inventories. Tracked generated references must have zero unexplained drift. Identical initial semantic state and authorized input must reach the same canonical action, normalized domain result, permission decision class, audit action id, and durable semantic state after normalizing time, random identifiers, and environment specific paths.

Every evidence record must name the commit, branch, artifact path, SHA-256 and SHA-512 where applicable, Java and platform versions, fixture manifest, exact command or workflow, expected result, actual result, and disposition. Any implementation, configuration, dependency, schema, protocol, catalog, permission, generated reference, or test harness change invalidates affected evidence. The final phase reruns the complete required matrix after the last product change.

## 6. Mandatory Scope

- `SEFAUD-REQ-001` freezes the authoritative audit baseline and complete traceable inventories.
- `SEFAUD-REQ-002` closes security, privacy, sensitive data leak, and backdoor like authority risk.
- `SEFAUD-REQ-003` closes applicable dependency and supply chain risk.
- `SEFAUD-REQ-004` proves and repairs every executable administrator action.
- `SEFAUD-REQ-005` polishes and verifies every in scope graphical and textual operator UI.
- `SEFAUD-REQ-006` closes the full codebase persistence and database integrity audit.
- `SEFAUD-REQ-007` closes backend handling and cross channel integration behavior.
- `SEFAUD-REQ-008` supplies post change clean checkout, runtime, compatibility, recovery, and regression proof.
- `SEFAUD-REQ-009` reconciles documentation and evidence and closes the final endpoint.

Confirmed defects discovered by a mandatory audit row are part of that requirement until repaired and covered by regression proof. A phase cannot exit while one of its owned mandatory defects remains known.

## 7. Optional or Future Scope

- `FUT-001`, excluded. Implement and activate the sixteen intentionally unavailable Phase 13 runtime families.
- `FUT-002`, excluded. Add unrelated commands, UI features, integrations, control families, or gameplay capabilities.
- `FUT-003`, excluded. Perform broad architecture modernization or class decomposition beyond confirmed defect remediation or mandatory evidence enablement.

Future work does not count toward completion and must not be introduced through opportunistic refactoring. Promotion requires a new explicit owner decision and an authorized plan revision.

## 8. Non Goals

- `NG-001`. Do not implement new features, including intentionally unavailable control family runtimes.
- `NG-002`. Do not upgrade or expand Minecraft, NeoForge, mappings, Java, Gradle, loader, or protocol boundaries.
- `NG-003`. Do not publish a release, deploy to production, or run destructive production tests.
- `NG-004`. Do not weaken tests, permissions, redaction, recovery, compatibility, or acceptance gates, and do not mark blocked or unexecuted evidence as passed.
- `NG-005`. Do not use real credentials, private messages, personal addresses, production worlds, or unrelated user data in fixtures or evidence.
- `NG-006`. Do not claim mathematical absence of defects. Completion means the exhaustive declared matrices and adversarial evidence reveal no known disallowed finding at the frozen revision.

## 9. Owner Decisions

### DEC-001 — Audit includes remediation

**Status:** RESOLVED
**Selected choice:** Audit plus mandatory in-scope remediation and regression proof.
**Rationale:** A verified endpoint cannot close while the audit has confirmed an in scope defect.
**Affected requirements:** SEFAUD-REQ-002, SEFAUD-REQ-003, SEFAUD-REQ-004, SEFAUD-REQ-005, SEFAUD-REQ-006, SEFAUD-REQ-007, SEFAUD-REQ-008, SEFAUD-REQ-009
**Supersedes:** none

### DEC-002 — Unavailable control families remain unavailable

**Status:** RESOLVED
**Selected choice:** No. Their implementation is feature expansion. They must remain clearly unavailable, unreachable, permission-safe, side-effect-free, and negatively tested.
**Rationale:** Their runtime implementation is feature expansion, while current repository evidence defines explicit unavailability as the safe contract. They cannot be represented as working commands or release functionality.
**Affected requirements:** SEFAUD-REQ-001, SEFAUD-REQ-004, SEFAUD-REQ-005, SEFAUD-REQ-007, SEFAUD-REQ-008, SEFAUD-REQ-009
**Supersedes:** none

The named families are `admin_journal`, `afk_zones`, `approvals`, `capability_leases`, `chat_channels`, `display_ownership`, `display_profiles`, `player_warp_review`, `portal_policy`, `resource_governor`, `resource_worlds`, `rollouts`, `server_presentation`, `spawn_ecology`, `staff_duty`, and `waypoints`.

### DEC-003 — Completion defect boundary

**Status:** RESOLVED
**Selected choice:** No known applicable critical or high exploitable vulnerability and no authorization bypass, sensitive-data leak, executable admin-command defect, UI-blocking defect, persistence-integrity defect, or mandatory backend-integration defect at the frozen revision.
**Rationale:** This converts the owner's audit goal into an observable closure gate without claiming impossible mathematical certainty.
**Affected requirements:** SEFAUD-REQ-001, SEFAUD-REQ-002, SEFAUD-REQ-003, SEFAUD-REQ-004, SEFAUD-REQ-005, SEFAUD-REQ-006, SEFAUD-REQ-007, SEFAUD-REQ-008, SEFAUD-REQ-009
**Supersedes:** none

### DEC-004 — Pinned compatibility boundary

**Status:** RESOLVED
**Selected choice:** No. Preserve Minecraft 1.21.1, NeoForge 21.1.235, Java 21, the checked-in Gradle wrapper, Parchment 2024.11.17, the optional enhanced protocol, fallback clients, and current optional-integration contracts.
**Rationale:** The request is an audit and remediation program, not a platform migration.
**Affected requirements:** SEFAUD-REQ-003, SEFAUD-REQ-005, SEFAUD-REQ-006, SEFAUD-REQ-007, SEFAUD-REQ-008
**Supersedes:** none

### DEC-005 — Safe verification environments and data

**Status:** RESOLVED
**Selected choice:** Disposable synthetic staging fixtures only. Production mutation, production credentials, personal data, and production worlds are excluded.
**Rationale:** Existing workflows intentionally corrupt files, interrupt processes, and exercise destructive administration. These operations need isolation, not weakened evidence.
**Affected requirements:** SEFAUD-REQ-001, SEFAUD-REQ-002, SEFAUD-REQ-003, SEFAUD-REQ-004, SEFAUD-REQ-005, SEFAUD-REQ-006, SEFAUD-REQ-007, SEFAUD-REQ-008, SEFAUD-REQ-009
**Supersedes:** none

### DEC-006 — Candidate lineage

**Status:** RESOLVED
**Selected choice:** The SEF 2 current candidate lineage beginning on envy/sef2_complete at 0c75bf25c58622096dfa7cc65a5f4b32e6d60ac4. The older default branch is identity and remote-security evidence only.
**Rationale:** The current branch contains the SEF 2 implementation described by the owner request. Branch divergence is recorded rather than silently reconciled.
**Affected requirements:** SEFAUD-REQ-001, SEFAUD-REQ-002, SEFAUD-REQ-003, SEFAUD-REQ-004, SEFAUD-REQ-005, SEFAUD-REQ-006, SEFAUD-REQ-007, SEFAUD-REQ-008, SEFAUD-REQ-009
**Supersedes:** none

### DEC-007 — Optional and future disposition

**Status:** RESOLVED
**Selected choice:** All optional and future work is excluded unless the owner later promotes it.
**Rationale:** Completion must remain measurable and cannot absorb unrelated expansion.
**Affected requirements:** none
**Supersedes:** none

### DEC-008 — UI scope

**Status:** RESOLVED
**Selected choice:** All in-scope graphical screens and HUDs plus administrator command feedback, because both are operator-facing product UI.
**Rationale:** An administrator must receive usable, accurate, accessible state and failure information regardless of enhanced client availability.
**Affected requirements:** SEFAUD-REQ-004, SEFAUD-REQ-005, SEFAUD-REQ-007, SEFAUD-REQ-008, SEFAUD-REQ-009
**Supersedes:** none

## 10. External Prerequisites

No external prerequisite is currently required. Mandatory verification uses repository owned code and disposable staging environments that the execution phases establish. Publishing, production credentials, production access, and destructive production verification are excluded.

| ID | Prerequisite | Affected requirements | Availability | Authorization | Required external action |
|---|---|---|---|---|---|
| none | No external prerequisite identified | none | available | not required | none |

Required test clients, synthetic identities, disposable worlds, local network interruption, graphical capture, failure injection, and optional provider fixtures are execution inputs, not authority to use a production system. If a required third party fixture or compatible dependency remedy proves unobtainable, the affected requirement becomes explicitly blocked. Lower fidelity evidence cannot replace it, and completion cannot be claimed.

## 11. Architecture and Ownership Boundaries

| Component or boundary | Canonical owner | Inputs and outputs | Required invariant |
|---|---|---|---|
| Mod lifecycle and service graph | `ServerEssentialsForge`, `KernelServices`, lifecycle handlers | Construction, command registration, server start, ticks, reload, stop, repository and worker state | Initialization is idempotent where declared, common code remains dedicated server safe, and teardown settles or marks every owned resource. |
| Command catalog and execution | `com.enviouse.sef.kernel.command`, `com.enviouse.sef.kernel.policy`, command registrars | Brigadier requests, catalog metadata, policy decisions, domain effects, feedback, audit events | Every executable route maps to one canonical action and one policy pipeline. Convenience routes cannot weaken policy. |
| Permission and authority | `com.enviouse.sef.permissions`, permission manifest, hierarchy and exemption services | Player and nonplayer sources, provider decisions, grants, leases, revisions | Logical server authority is current, least privilege, fail closed, and rechecked before mutation. Temporary authority cannot mint persistent authority. |
| Administrative execution | Sudo, run, silent, alias, bundle, profile, panel, and server control services | Stored or nested command intent, previews, confirmations, exact execution context | Stored text is not executable authority by itself. The effective actor, source, root, target, revision, and audit correlation remain bound. |
| GUI and protocol | `com.enviouse.sef.gui`, `com.enviouse.sef.gui.protocol`, client screen package | Negotiated sessions, bounded typed payloads, server projected choices, screens, HUD, command fallback | The client is presentation only. Every action is selected from server authority and revalidated before mutation. Missing or incompatible clients retain supported commands. |
| Domain services | Teleport, social, moderation, inventory, economy, controls, escrow, tags, disguise, configuration, and other feature packages | Validated domain requests, durable and transient state, game effects | Each domain owns its invariants and exposes mutation only through current policy and explicit failure behavior. |
| Persistence coordination | `StorageCoordinator`, `StorageRepository`, `AtomicFileStore`, coalesced workers, domain adapters | Snapshots, files, migrations, journals, receipts, backups, recovery state | Authoritative data never silently degrades to empty success. Writes are bounded, recoverable, and ordered with side effects. |
| Configuration | `ModuleConfigRegistry`, `ModuleConfigService`, NeoForge bootstrap configuration | Typed module files, revisions, migration candidates, generated reference | Publication is transactional, secret fields are filtered, previous known good state survives failure, and reload invalidates dependent authority. |
| Audit and observation | `AuditService`, command journal, redaction policy, optional file sink, observer projections | Immutable action metadata, redacted parameters, health and retention state | Mandatory security audit cannot be disabled by observation filters, and no sink receives data beyond its authorization and retention contract. |
| Optional integrations | Runtime guarded providers and adapters for LuckPerms, FTB Essentials, Curios, and declared bridges | Provider data, optional capability, absent and failed states | Absence cannot block core startup or grant authority. Adapter scope is explicit, bounded, and revocable. |
| Build, tests, and generated references | Gradle build, source and test sets, GameTests, reference generators, CI | Source, resources, resolved dependencies, reports, JAR, evidence | Clean checkout results are reproducible, generated references match live contracts, and the artifact contains only intended content. |

Dependency direction is server authority to presentation, catalog policy to domain mutation, and domain snapshots to persistence. Client presentation, generated documentation, logs, cached provider data, and recovery artifacts never become independent authority. Optional integrations sit behind runtime guards and bounded adapters. Dedicated server paths must not load client classes.

The primary trust boundaries are command parsing, source classification, permission providers, temporary or delegated authority, client payload decoding, GUI projection, stored command indirection, configuration and migration input, filesystem path ownership, persistence deserialization, archive and image import, optional mod reflection, mixins and access transformers, audit and export projection, dependency resolution, and packaged artifacts. Every boundary appears in the Phase 000 matrix with an owner, input class, validation, failure behavior, downstream effect, and required evidence.

Cross cutting invariants are:

1. The logical server is authoritative for identity, permission, policy, target selection, validation, and mutation.
2. Permission, feature, hierarchy, exemption, source, target, confirmation, and revision checks repeat immediately before privileged mutation.
3. One canonical action id carries equivalent command, shortcut, GUI, alias, bundle, panel, and approved integration paths.
4. A failed or unavailable provider never grants access and never turns unsupported state into success.
5. Sensitive content remains out of broad feedback, suggestions, logs, audit parameters, exports, generated references, and artifacts.
6. A nonidempotent side effect is never blindly retried from ambiguous persistence state.
7. Unsupported, malformed, corrupt, or stale persistent data blocks unsafe mutation and retains recovery evidence.
8. Reload, revocation, disconnect, dimension change, and shutdown invalidate or settle dependent transient state.
9. Disabled modules, unavailable control handlers, and incompatible clients cannot retain an alternate mutation path.
10. Every final claim is tied to the frozen revision, environment, exact workflow, and artifact digest.

## 12. Requirements

### SEFAUD-REQ-001 — Authoritative audit baseline and traceability

**Behavior:** Freeze the exact execution revision and create complete, deduplicated inventories for security boundaries, data flows, live Brigadier routes, catalog actions, shortcuts, source classes, graphical and textual UI, `StorageRepository` and nonrepository durable state, backend lifecycle handlers, integrations, configuration, schemas, dependencies, tests, documentation, and known evidence gaps. Every item has one owner, one audit disposition, and one evidence route.
**Owner:** Repository audit contract
**Contributors:** All source domains, build system, documentation, and remote security evidence
**Dependencies:** DEC-002, DEC-003, DEC-004, DEC-005, DEC-006, DEC-007, DEC-008
**Lifecycle stage:** readiness
**Production verification:** none
**Release impact:** stable release

**Acceptance criteria**

1. One baseline manifest records commit, branch, dirty state, remote state, platform versions, dependency graph, artifact inputs, and evidence environment without including secrets.
2. The live dispatcher, sealed catalog, shortcuts, generated command reference, permissions, GUI descriptors, configuration reference, and tests reconcile to exact counts with every discrepancy resolved as a defect, documented exclusion, or stale evidence correction.
3. Every persistent owner is inventoried, including paths outside `StorageRepository`, with schema, lifecycle, concurrency, recovery, privacy, and test ownership.
4. Every graphical screen, HUD, command feedback surface, privileged payload, authority bypass, optional adapter, lifecycle transition, mixin, access transformer, filesystem root, log, export, and packaged resource is assigned exactly one audit row.
5. Historical `VERIFIED` claims are either reexecuted at the frozen revision or labeled stale. No missing, duplicate, or unowned mandatory row remains.
6. The inventory and requirement traceability regenerate deterministically and form the fixed input to all later phases.

**Required evidence**

1. CodeGraph and build derived inventory reports tied to the frozen commit.
2. Live catalog, dispatcher, shortcut, permission, descriptor, repository, configuration, source set, dependency, and documentation reconciliation results.
3. A sanitized baseline manifest and requirement to phase to evidence traceability matrix.
4. Drift checks that fail on an added or removed relevant surface without an owned row.

### SEFAUD-REQ-002 — Security, privacy, leak, and backdoor like path closure

**Behavior:** Threat model, inspect, repair, and adversarially verify every authority, command execution, payload, filesystem, persistence, optional integration, logging, audit, export, projection, and packaged artifact boundary. Confirmed authorization bypasses, sensitive data leaks, unintended authority routes, and other endpoint relevant security defects receive regression proof.
**Owner:** Security audit boundary
**Contributors:** Command kernel, GUI protocol, persistence, configuration, optional integrations, domain services, packaging
**Dependencies:** SEFAUD-REQ-001, DEC-001, DEC-003, DEC-005
**Lifecycle stage:** change
**Production verification:** none
**Release impact:** stable release

**Acceptance criteria**

1. The threat model covers every Phase 000 trust boundary, normal and privileged flow, hidden or indirect entry route, data classification, abuse case, and failure state.
2. No route grants or retains authority through missing permission, provider failure, stale revision, source confusion, target confusion, delegation, alias, bundle, profile, GUI, payload, reflection, mixin, configuration, migration, recovery, or lifecycle race.
3. No sensitive value or hidden identity is exposed through any in scope output, error, observation, export, fixture, generated reference, or packaged file.
4. Owned filesystem paths reject traversal, symbolic link escape, detectable hard link substitution, unsafe archive content, unbounded input, and overwrite of recovery evidence according to their contract.
5. Security audit is complete, correlated, bounded, tamper evident within the repository contract, and independent from optional observation filters.
6. Adversarial tests cover malformed, forged, stale, replayed, oversized, concurrent, interrupted, absent provider, revoked permission, and indirect execution scenarios.
7. Every confirmed finding is repaired or the phase remains open. No known authorization bypass or sensitive data leak remains at any severity, and no known applicable critical or high exploitable vulnerability remains in repository owned code.

**Required evidence**

1. Threat boundary matrix and source to sink review with stable finding ids and exact dispositions.
2. Targeted unit, integration, GameTest, payload, parser, permission, redaction, filesystem, and recovery regression results.
3. Read only code scanning and secret scanning results plus repository and JAR secret and path inspection.
4. Updated security review tied to the final repaired commit, with limitations and invalidation rules.

### SEFAUD-REQ-003 — Dependency and supply chain closure

**Behavior:** Resolve every current dependency alert against the actual release candidate dependency graph and packaged JAR, repair applicable findings without changing the pinned platform boundary, and prove that no applicable critical or high exploitable dependency vulnerability remains.
**Owner:** Dependency graph
**Contributors:** NeoForge platform, optional integration declarations, CI, packaging, remote security state
**Dependencies:** SEFAUD-REQ-001, DEC-003, DEC-004
**Lifecycle stage:** change
**Production verification:** none
**Release impact:** stable release

**Acceptance criteria**

1. The resolved compile, runtime, test, tooling, and packaged dependency graphs are captured at the frozen revision with authoritative sources and versions.
2. Every open critical or high remote alert receives an evidence based disposition for the candidate branch. Inapplicability names the dependency path, configuration, packaged presence, affected API, reachability, and advisory basis.
3. Applicable findings are fixed through a compatible direct, constrained, excluded, or upstream dependency resolution that preserves `DEC-004` and passes runtime compatibility.
4. Dependency declarations preserve license and provenance expectations, optional integrations remain optional, and the final JAR contains no unintended embedded dependency or duplicate class set.
5. Clean resolution, dependency submission or equivalent graph capture, build, dedicated server, client, and artifact inspection pass after the last dependency change.
6. No critical or high alert is dismissed merely because it is transitive, inherited from the default branch, or absent from a direct declaration.

**Required evidence**

1. Gradle resolved dependency reports, packaged class and metadata inspection, and artifact digests.
2. Alert disposition table linked to authoritative advisories and exact candidate graph evidence.
3. Compatibility tests for every changed dependency and present and absent optional integration state.
4. Final remote alert and security scan snapshot, with branch applicability clearly separated from repository default branch state.

### SEFAUD-REQ-004 — Administrator command behavioral closure

**Behavior:** Complete one universal matrix row for every implemented, enabled, or otherwise executable administrator action. Verify registration, discovery, permission states, allowed and denied sources, targets, arguments, effects, failures, persistence, equivalent routes, feedback, audit, and redaction. Repair every confirmed defect. Intentionally unavailable control families remain an explicit negative contract and are never counted as working commands.
**Owner:** Command policy kernel
**Contributors:** Permission service, domain command owners, GUI protocol, aliases, bundles, sudo, server controls, audit service, persistence
**Dependencies:** SEFAUD-REQ-001, SEFAUD-REQ-002, SEFAUD-REQ-003, SEFAUD-REQ-006, DEC-002, DEC-008
**Lifecycle stage:** change
**Production verification:** none
**Release impact:** stable release

**Acceptance criteria**

1. The authoritative executable administrator action set is derived from the frozen live dispatcher, sealed catalog, active shortcuts, module state, and command ownership, not from a stale documentation count.
2. Each action passes absent, explicit deny, explicit grant, revocation before mutation, provider refresh, reload, and reconnect permission states, including hierarchy, exemption, feature, source, target, cooldown, quota, cost, warmup, confirmation, and audit policy where applicable.
3. Each action passes every documented allowed source and rejects every undocumented or denied source without mutation. Source coverage includes player, console, RCON, command block, function, scheduled, GUI, alias, bundle, sudo, and server profile contexts where declared.
4. Applicable target coverage includes self, online, offline, equal rank, higher rank, exempt, vanished, unknown, ambiguous, stale, and bulk targets. Applicable arguments cover missing, malformed, minimum, maximum, just below, just above, namespaced registry, long text, and injection like input.
5. Successful commands produce the exact domain effect once, persist and recover as declared, and provide accurate localized or component safe feedback. Denied and failed commands produce zero unauthorized side effect and an actionable safe result.
6. Canonical, shortcut, enhanced GUI, fallback, alias, bundle, panel, and approved integration routes converge on one action id, policy, cooldown identity, effect, and audit class.
7. Preview and confirmation bind actor, source, target, arguments, policy, revision, and expiry. Modification, replay, stale state, and expiry fail closed.
8. Audit records preserve real actor and effective actor attribution, stable metadata, result, and correlation without restricted content.
9. Each of the sixteen unavailable families is named as unavailable in diagnostics and UI, cannot preview as ready, cannot activate or resolve, performs no mutation, and is excluded from release capability claims.
10. No executable administrator command defect remains known. A parser only or safe read only result never substitutes for required mutating workflow proof.

**Required evidence**

1. A machine checked command matrix with one row per executable administrative action and negative rows for unavailable families.
2. Unit and dispatcher tests, command contract GameTests, domain mutation GameTests, and disposable multi actor manual workflows.
3. Before and after state hashes or domain assertions, restart proof, feedback capture, and redacted audit capture for each mutation class.
4. Generated command and permission references reconciled with the live tree and final documentation.

### SEFAUD-REQ-005 — UI polish and accessibility closure

**Behavior:** Inventory, polish, and verify all in scope `SefScreen` family screens, HUD surfaces, pause entry, administrative workflows, confirmation screens, pickers, item browsers, fallbacks, and administrator command feedback. The result is responsive, readable, accessible, state clear, privacy safe, and server authoritative.
**Owner:** GUI presentation
**Contributors:** GUI protocol, command and message services, domain workflow owners, client caches, accessibility and test harness
**Dependencies:** SEFAUD-REQ-001, SEFAUD-REQ-002, SEFAUD-REQ-004, DEC-008
**Lifecycle stage:** change
**Production verification:** none
**Release impact:** stable release

**Acceptance criteria**

1. Every in scope screen and command feedback surface has an explicit purpose, state hierarchy, primary action, navigation, loading state, empty state, validation state, failure state, success state, and fallback contract where applicable.
2. At GUI scales 1 through 4, 854 by 480, 1280 by 720, representative narrow and wide aspect ratios, and live resize, no required control or content is clipped, overlapped, illegible, unreachable, or hidden behind another layer.
3. Mouse, keyboard only, focus order, escape and back behavior, narration, tooltip, and error announcement are complete. Long translated strings, long values, empty results, pagination, search, and rapid refresh remain usable.
4. Visual hierarchy, spacing, contrast, backgrounds, labels, values, destructive emphasis, confirmation wording, and state badges are consistent across the current design language. Item browsing shows correct icons and bounded native tooltips.
5. Unauthorized entries and private data are absent, not merely disabled. Stale selections, revoked permissions, changed revisions, module disablement, disconnect, and dimension change close or refresh the surface before mutation.
6. Enhanced GUI and command fallback reach equivalent domain results and audit action ids. GUI preferences never alter a complete command's semantics.
7. Intentionally unavailable controls communicate unavailable status and never present a ready or successful mutation affordance.
8. Every UI blocking or materially misleading defect found by the matrix is repaired, and no such known defect remains.

**Required evidence**

1. Automated screen, layout helper, workflow compiler, payload codec, session, stale state, and permission tests.
2. Screenshot set for every screen class at required scales and representative resolutions, plus recordings for dynamic, focus, narration, revocation, reconnect, and animation workflows.
3. Enhanced and fallback client comparison with matching command results and server audit records.
4. Accessibility and visual review ledger tied to the frozen revision and client environment.

### SEFAUD-REQ-006 — Full codebase persistence and database integrity closure

**Behavior:** Audit and repair every durable store and cross store invariant for schema, bounds, atomicity, directory durability, concurrency, idempotency, lifecycle flush, corruption, recovery, migration, rollback, path safety, retention, and privacy. The term database includes file backed repositories, JSON, TOML, NBT, journals, queues, receipts, indexes, object stores, backups, and any other durable authority in this codebase.
**Owner:** Persistence layer
**Contributors:** Every domain with durable state, configuration service, audit and logging, offline adapters, lifecycle coordination
**Dependencies:** SEFAUD-REQ-001, SEFAUD-REQ-002, DEC-005
**Lifecycle stage:** change
**Production verification:** none
**Release impact:** stable release

**Acceptance criteria**

1. Every Phase 000 durable owner has a completed audit row for path, data classification, schema, version, bounds, identity, cardinality, compatibility, concurrency, flush, recovery, migration, rollback, retention, and tests.
2. Atomic publication preserves the previous valid state until replacement is durable, rejects owned path escape and unsafe links, reports directory synchronization limitations explicitly, and never overwrites recovery evidence unexpectedly.
3. Concurrent, coalesced, scheduled, startup, periodic, shutdown, and timed out writes have deterministic ownership. Snapshot capture and asynchronous publication do not race mutable game state.
4. Nonidempotent domain side effects use durable ordering, idempotency keys, journals, receipts, compensation, or outcome unknown resolution sufficient to prevent duplication or loss after interruption.
5. Empty, valid, legacy, unsupported newer, malformed, truncated, oversized, deeply nested, duplicate, stale, and semantically invalid data produce the declared state. Damaged enforcement data never becomes an empty successful policy.
6. Migration binds source identity and revision, validates all staged output, retains exact recovery material, refuses conflicting backup state, restores on failure, and proves forward migration and rollback on representative fixtures.
7. Startup, recovery mode, periodic flush, explicit flush, world reuse, shutdown, and worker timeout behavior pass with no silent data loss. Process interruption tests cover each shared persistence primitive and each distinct higher risk commit protocol.
8. Cross store invariants preserve UUID ownership, revisions, references, indexes, claims, receipts, escrow, authorization, expiry, and configuration authority across restart and repair.
9. Sensitive persisted data is minimized, bounded, redacted or hashed as declared, access controlled, retained for the documented period, and excluded from broad exports and fixtures.
10. No known persistence integrity defect remains in any inventoried owner.

**Required evidence**

1. Complete durable owner matrix and schema and path inventory.
2. Unit, integration, fault injection, concurrent write, process interruption, migration, rollback, corruption, recovery, shutdown, and restart results.
3. File and semantic state hashes before and after failure and recovery, with synthetic fixtures and exact operation ids.
4. Storage diagnostics, worker health, recovery artifacts, and updated persistence and migration documentation.

### SEFAUD-REQ-007 — Backend handling and cross channel integration closure

**Behavior:** Verify and repair backend behavior across initialization, registration, startup, reload, runtime mode change, permission and policy revision, command and GUI convergence, persistence, optional provider loss, disconnect, reconnect, dimension change, shutdown, retry, partial failure, audit correlation, and client, common, and dedicated server boundaries.
**Owner:** Lifecycle integration boundary
**Contributors:** Mod lifecycle, command kernel, GUI network, persistence, configuration, optional integrations, all stateful domain services
**Dependencies:** SEFAUD-REQ-002, SEFAUD-REQ-003, SEFAUD-REQ-004, SEFAUD-REQ-005, SEFAUD-REQ-006
**Lifecycle stage:** change
**Production verification:** none
**Release impact:** stable release

**Acceptance criteria**

1. Construction, manifest preparation, command registration, server startup, world load, player login, tick, configuration reload, module publication, provider refresh, player logout, server stopping, and server stopped flows have explicit owners and verified ordering.
2. Feature, permission, policy, configuration, command tree, target, session, panel, record, and persistence revisions invalidate dependent work before an unauthorized or stale mutation.
3. Command, GUI, shortcut, alias, bundle, sudo, panel, scheduled, and approved integration paths converge on one domain mutation and one correlated audit lifecycle.
4. Enhanced GUI disabled, enhanced GUI enabled, fallback client, incompatible protocol, reconnect, and mixed client states preserve command availability and clear client scoped sessions, projections, transfers, and drafts correctly.
5. Optional provider absence, startup failure, runtime outage, removal, malformed data, stale cache, and recovery preserve the declared fallback and never broaden authority or block unrelated core functionality.
6. Partial failure across game effect, persistent state, cost, cooldown, escrow, audit, queue, and external adapter boundaries reports the correct outcome and executes compensation or recovery exactly once.
7. Common and dedicated server paths load no client only class. Client handlers mutate presentation only and schedule work on the correct thread. Server mutations run on the logical server thread or through a proven snapshot and publication boundary.
8. Disconnect, death, respawn, dimension change, module disablement, reload, permission loss, and shutdown settle or invalidate warmups, menus, grants, projections, moderation state, schedules, workers, and pending actions as declared.
9. Runtime diagnostics expose actionable component health without secrets, false success, or hidden unavailable handlers.
10. No mandatory backend integration defect remains known.

**Required evidence**

1. Lifecycle and cross channel sequence matrix with failure injection at each boundary.
2. Dedicated server, enhanced client, fallback client, incompatible protocol, mixed multiplayer, reconnect, reload, provider outage, dimension, and shutdown workflows.
3. Correlated before and after state, audit events, diagnostics, thread safety assertions, and recovery records.
4. Dedicated server classloading and final JAR package boundary inspection.

### SEFAUD-REQ-008 — Post change runtime, compatibility, recovery, and regression proof

**Behavior:** From a clean checkout of the final candidate revision, pass the complete maintained static, unit, generated reference, GameTest, build, server, client, mixed multiplayer, provider, UI, packet abuse, recovery, performance, artifact, secret, and diff verification matrix.
**Owner:** Release verification system
**Contributors:** All mandatory requirement owners, Gradle build, CI, staging harness, documentation evidence
**Dependencies:** SEFAUD-REQ-002, SEFAUD-REQ-003, SEFAUD-REQ-004, SEFAUD-REQ-005, SEFAUD-REQ-006, SEFAUD-REQ-007
**Lifecycle stage:** post_change
**Production verification:** none
**Release impact:** stable release

**Acceptance criteria**

1. The maintained Gradle `check` lifecycle aggregates a deterministic format gate, compiler warning zero growth gate, reviewed static analysis, unit tests, and risk based line and branch coverage for security and persistence critical code. Suppressions are narrow, justified, and reviewed.
2. A clean Java 21 checkout passes `./gradlew check build compileFallbackRuntimeJava generateProjectReferences generatePerformanceReport` and zero unexplained tracked generated reference drift remains.
3. All required GameTests pass, including command contract, domain mutation, persistence, permission, integration, and regression coverage. Test counts and catalog counts match the frozen baseline manifest.
4. Dedicated server startup, steady operation, diagnostics, save, bounded shutdown, and restart pass with enhanced GUI disabled and enabled.
5. Matching enhanced, no SEF fallback, incompatible protocol, and mixed clients connect, remain stable, exercise required workflows, reconnect, and clear state correctly.
6. Current permission provider and applicable optional integrations pass present, absent, outage, refresh, removal, and fallback matrices. Blocked provider specific evidence remains a blocker.
7. GUI visual, accessibility, command feedback, InvSee, admission capacity and FIFO, disguise animation, packet abuse, permission revocation, reconnect, and cross dimension workflows pass at the required fidelity.
8. Persistence process interruption, recovery, migration, rollback, shutdown timeout, and restart workflows pass from disposable snapshots.
9. Performance budgets cover deterministic metadata work and relevant server tick, memory, queue, scan, payload, rendering, and persistence hot paths without unbounded work or log spam.
10. The final JAR, dependency metadata, mixins, access transformer, resources, generated references, licenses, hashes, secrets, host paths, logs, caches, and duplicate entries pass inspection. The complete Git diff contains no unrelated change or user data.
11. Any final product change invalidates affected evidence and triggers the prescribed rerun. The complete full matrix runs after the last change.

**Required evidence**

1. Clean checkout command logs and CI results tied to the exact commit and Java environment.
2. Unit, coverage, static analysis, GameTest, server, client, multiplayer, provider, recovery, performance, and UI evidence manifests.
3. Final JAR SHA-256 and SHA-512, entry inventory, dependency manifest, and complete diff audit.
4. One final rerun ledger with no failed, incomplete, or improperly downgraded mandatory row.

### SEFAUD-REQ-009 — Documentation, evidence, and final endpoint closure

**Behavior:** Reconcile all affected documentation and sanitized evidence with the final implementation and artifact, then run a final cross matrix audit. Close only when every mandatory row and endpoint condition passes.
**Owner:** Documentation evidence
**Contributors:** All mandatory requirement owners, user and operator documentation, test and security records, release workflow
**Dependencies:** SEFAUD-REQ-001, SEFAUD-REQ-002, SEFAUD-REQ-003, SEFAUD-REQ-004, SEFAUD-REQ-005, SEFAUD-REQ-006, SEFAUD-REQ-007, SEFAUD-REQ-008
**Lifecycle stage:** post_change
**Production verification:** none
**Release impact:** stable release

**Acceptance criteria**

1. `README.md`, `DOCUMENTATION.md`, command, permission, configuration, compatibility, security, migration, troubleshooting, test, acceptance, performance, installation, and release documents describe only behavior verified at the final commit.
2. Generated command, permission, and configuration references match the final live contracts exactly, including executable and intentionally unavailable control status.
3. Documentation names setup, permissions, failure behavior, recovery, migration, rollback, enhanced and fallback clients, optional integrations, known limitations, and evidence commands needed by users and operators.
4. The sanitized evidence manifest links every requirement and matrix row to the final commit, artifact hashes, environment, exact proof, and result. Raw logs, recordings, and fixtures remain in the approved evidence location and contain no secrets or personal data.
5. The final audit finds no missing requirement owner, phase, matrix row, highest fidelity proof, documentation obligation, or release readiness gate.
6. Every confirmed in scope defect is repaired with regression proof. No known critical or high exploitable vulnerability, authorization bypass, sensitive data leak, executable administrator command defect, UI blocking defect, persistence integrity defect, or mandatory backend integration defect remains.
7. Optional and future scope remains excluded, unavailable controls are not advertised as features, no blocked row is called passed, and no production release or deployment occurs under this plan.

**Required evidence**

1. Documentation drift and link checks plus generated reference comparison.
2. Final requirement to phase to test to artifact traceability report.
3. Final security, command, UI, persistence, integration, compatibility, and release readiness audit tied to one commit and artifact digest.
4. Completed Definition of Done checklist and Goal Creator completion packet, without publication action.

## 13. Phased Roadmap

The master owns this global sequence, canonical requirement ownership, dependency topology, and completion authority. Each linked phase file owns the sole full phase declaration and detailed execution blueprint. Phase files must use `PLAN-PHASE-NNN`, stable `P<NNN>-TASK-###` task ids, and the exact scope frozen here.

| Phase ID | Objective | Owner | Dependencies | Canonical requirements | Entry summary | Exit summary | Next transition | Execution blueprint |
|---|---|---|---|---|---|---|---|---|
| `SEFAUD-PHASE-000` | Freeze one reproducible audit baseline and complete every authoritative inventory and traceability matrix. | Repository audit contract | `DEC-002`, `DEC-003`, `DEC-004`, `DEC-005`, `DEC-006`, `DEC-007`, `DEC-008` | `SEFAUD-REQ-001` | The validated plan set is integrated, repository identity matches, and the candidate lineage is available without losing unrelated user state. | Exact current counts and owners reconcile across source, runtime, generated references, persistence, UI, integrations, dependencies, tests, and docs. No mandatory surface is missing, duplicated, or unowned. | `SEFAUD-PHASE-001` | [Phase 000](phases/plan-phase-000.md) |
| `SEFAUD-PHASE-001` | Close security, privacy, backdoor like path, and dependency supply chain risk with adversarial regression evidence. | Security audit boundary | `SEFAUD-PHASE-000`, `SEFAUD-REQ-001` | `SEFAUD-REQ-002`, `SEFAUD-REQ-003` | Phase 000 matrices, frozen dependency graph, trust boundaries, data classes, and evidence rules are complete. | Every boundary and alert has an evidence based disposition, all confirmed mandatory findings are repaired, and no known prohibited security or applicable critical or high dependency finding remains. | `SEFAUD-PHASE-002` | [Phase 001](phases/plan-phase-001.md) |
| `SEFAUD-PHASE-002` | Close full codebase persistence and database integrity across normal, concurrent, corrupt, interrupted, migrated, and recovered states. | Persistence layer | `SEFAUD-PHASE-001`, `SEFAUD-REQ-002`, `SEFAUD-REQ-003` | `SEFAUD-REQ-006` | Security and filesystem boundaries are closed, and every durable owner has a Phase 000 row. | Every durable owner and cross store invariant passes its matrix, all confirmed integrity defects are repaired, and recovery evidence proves no silent loss or unsafe empty success. | `SEFAUD-PHASE-003` | [Phase 002](phases/plan-phase-002.md) |
| `SEFAUD-PHASE-003` | Prove every executable administrator action and every unavailable negative contract across authority, effects, failure, persistence, feedback, and audit. | Command policy kernel | `SEFAUD-PHASE-002`, `SEFAUD-REQ-006` | `SEFAUD-REQ-004` | Security and persistence foundations pass, and the authoritative executable action set is frozen. | Every executable administrator action has a passed universal matrix row, unavailable families fail closed, every confirmed command defect is repaired, and generated references match the live tree. | `SEFAUD-PHASE-004` | [Phase 003](phases/plan-phase-003.md) |
| `SEFAUD-PHASE-004` | Deliver polished, accessible, responsive, state clear, and privacy safe graphical and textual operator UI. | GUI presentation | `SEFAUD-PHASE-003`, `SEFAUD-REQ-004` | `SEFAUD-REQ-005` | Command semantics, permissions, domain effects, and unavailable states are stable enough to serve as UI authority. | Every UI surface passes layout, input, accessibility, state, revocation, privacy, and fallback matrices, with no known blocking or materially misleading defect. | `SEFAUD-PHASE-005` | [Phase 004](phases/plan-phase-004.md) |
| `SEFAUD-PHASE-005` | Close backend lifecycle and cross channel integration under normal operation, revision change, provider failure, reconnect, partial failure, and shutdown. | Lifecycle integration boundary | `SEFAUD-PHASE-004`, `SEFAUD-REQ-005` | `SEFAUD-REQ-007` | Security, persistence, commands, and UI each pass their owned contract at the current integrated revision. | Lifecycle and channel matrices prove consistent server authority, equivalence, failure, recovery, classloading, and audit behavior, with every confirmed integration defect repaired. | `SEFAUD-PHASE-006` | [Phase 005](phases/plan-phase-005.md) |
| `SEFAUD-PHASE-006` | Produce complete clean checkout static, runtime, multiplayer, compatibility, recovery, performance, and artifact proof after all product changes. | Release verification system | `SEFAUD-PHASE-005`, `SEFAUD-REQ-007` | `SEFAUD-REQ-008` | All change stage requirements are integrated, no known owned defect remains, and the candidate revision is frozen for final verification. | Every mandatory automated and real workflow passes at the required fidelity, the final artifact and diff pass inspection, and no result is stale, blocked, or replaced by lower fidelity proof. | `SEFAUD-PHASE-007` | [Phase 006](phases/plan-phase-006.md) |
| `SEFAUD-PHASE-007` | Reconcile documentation and evidence, audit the complete integrated plan endpoint, and close release readiness without publishing. | Documentation evidence | `SEFAUD-PHASE-006`, `SEFAUD-REQ-008` | `SEFAUD-REQ-009` | The final verified commit and artifact digest are immutable for closure, and all Phase 006 evidence is complete. | Documentation, generated references, evidence, requirement traceability, phase gates, and Section 18 all pass at one revision. No disallowed known defect or unclosed mandatory row remains. | Final plan wide completion | [Phase 007](phases/plan-phase-007.md) |

Phase ids are contiguous from `SEFAUD-PHASE-000` through `SEFAUD-PHASE-007`. Dependencies move backward only. A later phase cannot begin until the prior phase implementation, evidence, pull request checks, integration, resulting candidate branch verification, and required signed phase tag satisfy the repository workflow. No phase may pass while a known mandatory phase owned defect remains.

## 14. Verification Strategy

| Requirement | Static or unit evidence | Integration evidence | Real behavior evidence | Security and negative evidence | Artifact or runtime evidence |
|---|---|---|---|---|---|
| `SEFAUD-REQ-001` | Inventory parsers, schema checks, duplicate and ownership checks | Live catalog, dispatcher, reference, repository, and build reconciliation | Baseline capture from a clean candidate checkout | Missing, duplicate, stale, and unowned surface injection tests | Commit, environment, dependency, and evidence manifest |
| `SEFAUD-REQ-002` | Permission, redaction, parser, payload, path, archive, audit, and policy tests | Cross boundary authority and data flow tests | Multi actor adversarial workflows and provider failure | Forgery, replay, revocation, indirect execution, path escape, sensitive output, and secret scans | Security report, scanning snapshot, JAR and export inspection |
| `SEFAUD-REQ-003` | Dependency graph and advisory applicability analysis | Changed dependency build and optional integration tests | Dedicated server and client startup with resolved graph | Critical and high alert disposition and vulnerable API reachability | Resolved graphs, metadata, licenses, JAR entries, hashes |
| `SEFAUD-REQ-004` | Dispatcher, catalog, permission, argument, policy, and domain unit tests | Command contract and mutation GameTests | Universal multi actor administrator matrix for every executable action | Denied sources, permission states, targets, stale confirmation, failures, unavailable families, audit redaction | Generated command and permission references, state and audit evidence |
| `SEFAUD-REQ-005` | Screen, layout helper, workflow, session, codec, and state tests | Enhanced and fallback action equivalence | Scale, resolution, resize, keyboard, mouse, narration, long text, dynamic and visual matrix | Revocation, stale state, hidden identity, unavailable control, invalid input | Screenshot and recording manifest tied to client and commit |
| `SEFAUD-REQ-006` | Repository, schema, atomic store, worker, bounds, migration, and cross store tests | Concurrent, fault injection, interruption, shutdown, restore, and restart tests | Disposable world corruption, migration, process termination, and recovery drills | Unsafe path, malformed and oversized data, stale revision, duplicate side effect, privacy and retention | Before and after hashes, journals, receipts, backups, diagnostics |
| `SEFAUD-REQ-007` | Lifecycle, revision, thread, classloading, provider, and correlation tests | Command, GUI, persistence, configuration, provider, and domain convergence | Dedicated server, mixed clients, reload, reconnect, dimension, outage, shutdown | Partial failure, stale state, unavailable adapter, client authority, side and thread misuse | Lifecycle traces, correlated audit, diagnostics, JAR boundary inspection |
| `SEFAUD-REQ-008` | Maintained format, warnings, static analysis, unit, coverage, and drift gates | Complete GameTest and CI matrix | Server, enhanced, fallback, multiplayer, provider, UI, recovery, and performance matrix | Packet abuse, secret scan, failure injection, artifact and diff audit | Final clean checkout logs, JAR hashes, dependency and evidence manifests |
| `SEFAUD-REQ-009` | Documentation links, generated reference drift, terminology and claim checks | Requirement to phase to evidence reconciliation | Operator procedures replayed against final artifact | False completion, stale claim, unavailable feature advertisement, secret and personal data review | Final evidence index, Definition of Done, release readiness record |

Tests use synthetic identities and disposable worlds. Destructive cases are isolated from production. Expected and actual results are recorded for each row. A row marked blocked remains incomplete. Unit proof cannot replace required GameTest, dedicated server, client, multiplayer, graphical, provider, interruption, migration, recovery, or artifact proof.

The final maintained command order is:

1. Run the repository format and compiler warning gates through `./gradlew check`.
2. Run maintained static analysis, unit tests, and risk based coverage through `./gradlew check`.
3. Run generated reference and performance generation and require zero unexplained tracked drift.
4. Run all required GameTests.
5. Run `./gradlew build` and fallback runtime compilation from a clean checkout.
6. Run dedicated server startup, operation, save, shutdown, and restart.
7. Run enhanced, fallback, incompatible protocol, and mixed multiplayer clients.
8. Run provider, GUI, command, persistence interruption, migration, rollback, and recovery matrices.
9. Inspect performance, the final JAR, dependencies, resources, secrets, and complete diff.
10. Rerun every invalidated row after the last product change, then run the final cross matrix audit.

## 15. Compatibility and Versioning, Migration, Rollout, Rollback, and Recovery

The supported platform remains Minecraft 1.21.1, NeoForge 21.1.235, Java 21, the checked in Gradle wrapper, Parchment 2024.11.17, mod id `sef`, and artifact version lineage 2.0.0. NeoForge only is supported. The universal JAR remains optional on clients. Enhanced GUI disablement, vanilla or no SEF clients, compatible enhanced clients, and incompatible enhanced protocol clients retain their documented behavior.

Product, network protocol, configuration documentation, persistent schema, generated reference, and public identifier versions remain governed by their current source owners. A repair that changes a wire shape, schema, serialized field, config field, permission id, action id, resource location, or public route must either remain backward compatible or make the smallest appropriate versioned migration. Unsupported newer input fails explicitly. Unknown fields are preserved only where the existing owner contract permits them.

Optional integrations remain optional and runtime guarded. LuckPerms API 5.4, FTB Essentials, Curios, and other declared adapter contracts must be tested against compatible target artifacts where advertised. Absence or failure preserves the documented fallback. A dependency security remedy must not silently broaden the advertised platform range.

No broad data migration is planned. If remediation changes a persistent schema or configuration contract, the owning phase must add source fingerprinting, version selection, bounded transformation, complete validation, exact recovery copy, failure restoration, forward migration fixtures, rollback procedure, and compatibility documentation. The prior approved JAR and data snapshot remain rollback material until final acceptance. A point of no return is not authorized.

Rollout is sequential through the registered phases and repository branch workflow. Each phase is integrated only after its tests, real behavior evidence, documentation, review, and required checks pass. Rollback for an unmerged phase is removal of that phase's isolated change. Rollback after integration uses the prior signed phase state and validated data recovery material. No plan phase deploys to production or publishes a marketplace artifact.

## 16. Documentation, Operations, and Release Gates

1. Keep `README.md` accurate for users and `DOCUMENTATION.md` accurate for developers and operators.
2. Keep command, permission, and configuration references generated from and reconciled with live contracts. A stale count or route is a failed gate.
3. Update compatibility, security, migration, installation, troubleshooting, performance, test, acceptance, and release documents for every verified behavior or limitation change.
4. Preserve intentionally unavailable control families as explicit unavailable surfaces. Do not advertise them as implemented, enabled, or release complete.
5. Document exact build, check, GameTest, server, enhanced client, fallback client, mixed client, provider, recovery, and artifact inspection commands and expected results.
6. Store raw logs, screenshots, recordings, profiles, corrupted fixtures, and world snapshots in the approved sanitized evidence location outside tracked source unless a small synthetic fixture belongs in tests. Tracked evidence summaries must not contain credentials, personal data, host specific private paths, or unbounded logs.
7. Every phase completion packet names commits, checks, runtime proof, documentation, issues or pull requests, integration state, and evidence invalidation. The protected plan set is not a status diary.
8. Required pull request checks, a private independent-review capability preflight and either a passing supported review or an explicit optional unsupported disposition, merge integration, default or candidate branch verification, and a signed phase tag must pass before the next phase begins, consistent with repository instructions.
9. The release candidate JAR receives SHA-256 and SHA-512 hashes, dependency and provenance inspection, generated reference comparison, secret and content inspection, and source commit binding.
10. Release readiness does not authorize publication. Marketplace preview, credentials, publishing, deployment, and production change remain outside this plan and require separate explicit owner authority.

## 17. Risks and Failure Boundaries

| Risk | Impact | Prevention | Detection | Recovery |
|---|---|---|---|---|
| Audit breadth hides an unowned surface | False confidence and an undiscovered defect | Deterministic Phase 000 inventories from source, runtime, generated references, and persistence paths | Missing owner and drift checks fail on additions, removals, duplicates, and count mismatch | Reopen Phase 000, assign ownership, and invalidate affected downstream evidence |
| Historical evidence is stale or contradictory | Incorrect completion claim | Label authoring evidence `OBSERVED`, freeze a new revision, rerun required proof | Count, hash, version, and generated reference reconciliation | Replace stale claims with new evidence and rerun dependent gates |
| Command matrix becomes impractically broad | Destructive gaps or superficial parser only proof | Generate rows, group shared policy proof only where the invariant is identical, and retain domain specific mutation proof | Coverage report identifies unexecuted sources, targets, mutations, and failure classes | Add fixtures and run missing rows before phase exit |
| Security review misses an indirect authority route | Authorization bypass, data leak, or command execution | Trace aliases, bundles, sudo, profiles, GUI, payloads, reflection, mixins, config, recovery, and lifecycle paths | Adversarial tests, source to sink review, runtime audit correlation, independent review | Repair, add regression proof, and rerun all affected security, command, and integration rows |
| Dependency alert cannot be fixed within platform pins | Applicable critical or high vulnerability remains | Evaluate exact runtime reachability early and test compatible constraints or exclusions | Resolved graph, packaged class inspection, advisory mapping | Block completion and request an owner authorized platform or scope decision, never suppress the alert |
| Persistence fault testing damages valuable data | Irrecoverable data loss | Use only disposable synthetic snapshots and isolated directories | Fixture identity and path preflight before destructive action | Discard the fixture and recreate it from the retained baseline snapshot |
| Async write or side effect ordering is ambiguous | Duplicate, missing, or corrupt state after crash | Explicit state machine, durable operation id, idempotency, journal, receipt, or compensation | Process interruption and outcome unknown recovery tests | Restore last known good state or resolve the durable outcome before retry |
| UI polish becomes subjective or expands scope | Inconsistent result and schedule drift | Use fixed layout, accessibility, state, privacy, and equivalence criteria across all surfaces | Screenshot, narration, interaction, and state review ledger | Repair only failed criteria and keep new feature concepts in future scope |
| Optional provider fixture is incompatible or unavailable | Advertised integration lacks highest fidelity evidence | Select a compatible target artifact, record provenance, and test present and absent states | Real login, permission mutation, refresh, outage, removal, and fallback matrix | Keep the row blocked and do not claim compatibility until a valid fixture is tested |
| Branch divergence contaminates the audit baseline | Legacy or unrelated changes enter the candidate | Follow `DEC-006`, record exact ancestry, and prohibit silent merge or platform drift | Git ancestry, diff, and artifact manifest checks | Stop, restore the candidate lineage, and rebaseline before continuing |
| Final fixes invalidate earlier proof | Evidence no longer applies | Track invalidation by component, interface, dependency, schema, and generated reference | Final evidence audit compares proof commit with product commit | Rerun affected rows and the complete Phase 006 matrix after the last change |
| Evidence captures secrets or personal data | Privacy breach in logs or artifacts | Synthetic identities, redaction, bounded captures, and evidence review | Secret, address, message, host path, and artifact scans | Quarantine and delete unsafe evidence, rotate any exposed secret through its owner, recreate sanitized proof |

Unknown facts remain failures to prove, not permission to infer success. A mandatory defect found after its owner phase reopens that phase's gate and invalidates downstream evidence that depended on the defective behavior.

## 18. Definition of Done

The plan is complete only when all conditions below hold at one frozen candidate revision and artifact digest:

1. Every mandatory requirement from `SEFAUD-REQ-001` through `SEFAUD-REQ-009` satisfies every acceptance criterion and required evidence item.
2. Every phase from `SEFAUD-PHASE-000` through `SEFAUD-PHASE-007` satisfies its linked execution blueprint, exit gate, integration workflow, and next transition rule.
3. The audit baseline contains no missing, duplicate, stale, or unowned mandatory security, command, UI, persistence, backend, integration, dependency, test, documentation, or artifact surface.
4. Every confirmed in scope defect is repaired and has regression proof at its real failure boundary.
5. Every executable administrator action passes the universal matrix. The sixteen intentionally unavailable families remain explicit, unreachable, side effect free, negatively verified, and absent from capability claims.
6. Every in scope graphical and textual operator UI passes responsive layout, input, accessibility, state, privacy, revocation, and fallback gates, with no known blocking or materially misleading defect.
7. Every durable owner and cross store invariant passes schema, bounds, atomicity, concurrency, interruption, migration, rollback, recovery, lifecycle, retention, and privacy gates, with no known integrity defect.
8. Backend lifecycle, command and GUI convergence, optional providers, revision invalidation, reconnect, dimension, partial failure, shutdown, logical side, and audit correlation pass their required matrices.
9. Every current dependency alert has a candidate specific disposition, and no known applicable critical or high exploitable dependency or repository vulnerability remains.
10. No known authorization bypass, sensitive data leak, backdoor like authority path, executable administrator command defect, UI blocking defect, persistence integrity defect, or mandatory backend integration defect remains.
11. Clean checkout checks, unit tests, maintained static and coverage gates, generated reference drift, GameTests, dedicated server, enhanced and fallback clients, mixed multiplayer, provider, UI, packet abuse, recovery, performance, JAR, secret, and complete diff gates all pass at the required fidelity.
12. Documentation, generated references, compatibility claims, security review, test ledger, migration and recovery guidance, and release readiness evidence match the final artifact exactly.
13. No blocked, incomplete, stale, mocked, or lower fidelity result is represented as passed. Any unavailable required fixture is reported as a blocker without weakening scope.
14. `FUT-001`, `FUT-002`, and `FUT-003` remain excluded unless explicitly promoted by the owner. All non goals remain intact.
15. Release readiness is recorded, but no public release, publication, deployment, production mutation, or destructive production verification occurs under this plan.

Completion endpoint:

> At one frozen SEF 2 candidate revision, every mandatory audit and remediation matrix is complete, all confirmed in-scope defects are repaired, highest-fidelity clean-checkout and runtime verification passes, documentation and evidence match the built artifact, and no known critical or high exploitable vulnerability, authorization bypass, sensitive-data leak, executable administrator-command defect, UI-blocking defect, persistence-integrity defect, or mandatory backend-integration defect remains.

## 19. Goal Creator Handoff

```text
Planning subject: Server Essentials Forge 2 final security, administrator-command, UI, persistence, backend-handling, and integration audit with mandatory remediation closure
Mandatory boundary: SEFAUD-REQ-001 through SEFAUD-REQ-009, including remediation and regression proof for every confirmed in-scope defect.
Optional/future disposition: excluded
Locked owner decisions: DEC-001 through DEC-008.
Active phase: SEFAUD-PHASE-000
Active phase plan: phases/plan-phase-000.md.
Next executable action: P000-TASK-001 must freeze the execution revision and produce the authoritative baseline manifest before any audit remediation begins.
Known failing checks: conflicting historical test and catalog counts; incomplete multiplayer, current LuckPerms, GUI, InvSee, admission, disguise, reconnect, and recovery rows; absent maintained formatter, warning, static-analysis, and risk-based coverage gates; open repository dependency alerts whose candidate applicability is unresolved.
Known external blockers: none
Unavailable feature boundary: the sixteen named Phase 13 families remain explicit unavailable negative contracts and are not implementation scope.
Completion endpoint: At one frozen SEF 2 candidate revision, every mandatory audit and remediation matrix is complete, all confirmed in-scope defects are repaired, highest-fidelity clean-checkout and runtime verification passes, documentation and evidence match the built artifact, and no known critical or high exploitable vulnerability, authorization bypass, sensitive-data leak, executable administrator-command defect, UI-blocking defect, persistence-integrity defect, or mandatory backend-integration defect remains.
Required evidence gates: complete inventories, security and dependency closure, administrator command matrix, UI and accessibility matrix, persistence fault and recovery matrix, backend integration matrix, clean-checkout automated and runtime matrix, final artifact inspection, documentation parity, and final cross-matrix audit.
Execution rule: read this master and every registered phase plan through EOF, execute only the current contiguous phase, preserve the immutable saved goal, and never start a later phase before current phase integration and evidence gates pass.
```
