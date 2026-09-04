# Server Essentials Forge 2 Final Audit and Remediation Plan

> **Plan ID:** PLAN-MASTER
> **Plan status:** VALIDATED WITH KNOWN EXTERNAL BLOCKER
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
Original update-pass starting branch: envy/phase-001-security
Original update-pass starting commit: e203ece01df76cb076d9112a14a052cac6f4145d
Contract amendment evidence branch: envy/phase-001-security
Contract amendment evidence commit: 3fd6ce65e7a2610a04b1a0965e89a36cb6905fec
Starting branch: envy/phase-001-security
Starting commit: 3fd6ce65e7a2610a04b1a0965e89a36cb6905fec
Candidate lineage base commit: 0c75bf25c58622096dfa7cc65a5f4b32e6d60ac4
Authoritative remote:
origin
https://github.com/MCEnvision/Server-Essentials-Forge.git
Remote ref: origin/envy/phase-001-security
Remote commit observed for this amendment: 3fd6ce65e7a2610a04b1a0965e89a36cb6905fec
Remote commit: 3fd6ce65e7a2610a04b1a0965e89a36cb6905fec
Remote default branch at authoring: forge-1.20.1
Product version at authoring: 2.0.0
Minecraft: 1.21.1
NeoForge: 21.1.235
Java: 21
Parchment mappings: 2024.11.17
Requested artifact: authoritative_plan
```

Repository identity, package metadata, source namespace, and remote identity match the owner request. The original update pass started at `e203ece01df76cb076d9112a14a052cac6f4145d`; this contract amendment observes the same Phase 001 branch at `3fd6ce65e7a2610a04b1a0965e89a36cb6905fec`. Both revisions descend from the candidate lineage base selected by `DEC-006`. The older remote default branch is repository and security evidence, not an instruction to merge legacy platform work into this audit or to change the supported platform.

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
| SRC-009 | owner_request | Mandatory Linux, macOS, and Windows support, opened-object writer identity, native dependency packaging, and platform dependency alert closure | EnVy's direct Plan Creator update dated 2026-09-02 | Authoritative cross-platform decision, external prerequisites, blocker routing, and completion endpoint |
| SRC-010 | repository_evidence | Cross-platform audit writer and compile-only native API declarations on the active Phase 001 branch | /mnt/hermes/projects/SEFPORTED at 3fd6ce65e7a2610a04b1a0965e89a36cb6905fec | Observed implementation state and unresolved runtime and provenance evidence only |

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
7. Prove Java 21 and NeoForge 21.1.235 behavior on disposable Linux, macOS, and Windows systems, including provider-specific or native security-sensitive writes that bind validation and mutation to the opened object.
8. Rerun the complete clean checkout, multiplayer, compatibility, recovery, performance, artifact, dependency, and documentation matrix at one frozen revision.

The observable endpoint is defined by `DEC-003`, `DEC-009`, and Section 18. Linux, macOS, and Windows are all mandatory. Unknown availability of `EXT-001` or `EXT-002` leaves the plan explicitly blocked without narrowing the endpoint. The result is release readiness evidence, not authorization to publish or deploy.

## 4. Evidence Based Current State

| Area | Evidence class | Finding | Evidence |
|---|---|---|---|
| Project identity | OBSERVED | The repository builds a NeoForge 1.21.1 mod with mod id `sef`, Java 21, NeoForge 21.1.235, Parchment 2024.11.17, and artifact version 2.0.0. | `gradle.properties`, `build.gradle`, `settings.gradle`, generated mod metadata |
| Repository state | OBSERVED | The original update-pass starting branch was `envy/phase-001-security` at `e203ece01df76cb076d9112a14a052cac6f4145d`. The same branch is now observed at `3fd6ce65e7a2610a04b1a0965e89a36cb6905fec`, matches `origin/envy/phase-001-security`, descends from the `DEC-006` lineage base, and preserves the unrelated untracked `.playwright-mcp/` directory outside plan scope. | Read only local Git inspection on 2026-09-02 and prior SRC-007 remote evidence |
| Planning state | OBSERVED | The authoritative master, eight contiguous phase plans, plan index, and deterministic handoff existed before this amendment. This pass preserves all existing requirement, decision, future, non-goal, and phase IDs while adding `DEC-009`, `EXT-001`, and `EXT-002`; phase integration and deterministic handoff regeneration remain pending. | Existing registered plan set and locked update intake |
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
| Cross-platform audit writer | OBSERVED | The active Phase 001 branch contains Linux and macOS POSIX descriptor handling and Windows handle-based handling that inspect opened-object identity and link or reparse state. `jna` and `jna-platform` are declared compile only. Current documentation reports Linux evidence passing while macOS and Windows runtime evidence and authoritative NeoForge-supplied dependency closure remain open. | `NativeAuditFileProvider`, `build.gradle`, SRC-005, and SRC-010 |
| Automated evidence | OBSERVED | The newest acceptance ledger claims 493 unit tests and 41 required GameTests, while the prior full audit records 487 and 38. Both report passing builds at their respective snapshots. No command was rerun during plan authoring, so neither count is `VERIFIED` for plan completion. | [Acceptance ledger](../SEF2_ACCEPTANCE.md) and [prior audit](../../audit.md) |
| Interactive evidence | OBSERVED | macOS and Windows native-writer runtime proof, authoritative platform dependency closure, multiplayer, current LuckPerms, GUI visual and accessibility, InvSee, admission capacity, disguise animation, reconnect, and selected recovery rows remain incomplete or require renewal. | [Acceptance ledger](../SEF2_ACCEPTANCE.md), [manual test plan](../../test.md), [compatibility matrix](../COMPATIBILITY_MATRIX.md), and SRC-010 |
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
| Test system | external_prerequisite | SRC-004, SRC-009, EXT-001 | Verification Strategy | The repository has layered unit, GameTest, server, client, multiplayer, provider, failure-injection, performance, and artifact workflows, while mandatory real runtime proof additionally requires disposable Linux, macOS, and Windows environments. |
| Release lifecycle | external_prerequisite | SRC-005, SRC-009, EXT-001, EXT-002 | Documentation, Operations, and Release Gates | Build, packaging, cross-platform runtime evidence, dependency provenance, compatibility, documentation, rollback, and release-readiness gates are in scope, while public publication is excluded. |
| Generalization | external_prerequisite | SRC-002, SRC-009, EXT-001 | Supported Environments and Generalization | Linux, macOS, and Windows are mandatory, alongside dedicated server, enhanced and fallback clients, mixed multiplayer, supported command sources, and optional integrations present and absent. |
| Determinism | covered | SRC-004 | Determinism and Evidence Reproducibility | Inventories, generated references, normalized persistence, audit matrices, clean-checkout commands, and artifact digests must reproduce at the frozen revision. |

### Inputs, Outputs, and Observable Endpoint

In scope inputs include player, console, RCON, command block, function, scheduled, GUI, alias, bundle, sudo, and server profile command requests where the catalog permits them; typed enhanced client payloads; configuration and migration files; world and server persistent data; optional provider decisions; registry content; lifecycle events; dependency metadata; and release candidate source and resources. Every input is untrusted until the owning server boundary validates its type, size, source, permission, revision, and current policy.

In scope outputs include domain mutations, command feedback, suggestions, graphical screens, HUD state, audit events, redacted observation records, logs, exports, persistent files, migrations, backups, recovery states, generated references, build reports, test evidence, and the packaged JAR. Outputs must reveal only authorized information, identify failure without exposing secrets, and match the final documented contract.

The endpoint is one frozen candidate revision and one artifact digest that satisfy all mandatory requirements, every phase exit, and Section 18 on Linux, macOS, and Windows. A passing parse, registered command, present source file, historical audit, single-operating-system result, or headless startup alone cannot satisfy the endpoint. Unknown or unavailable external evidence remains an `EXT-001` or `EXT-002` blocker and never converts a mandatory operating system or dependency gate into an exclusion.

### State and Persistence Contract

The logical server owns all authoritative gameplay, administration, permission, session, and durable state. Clients own presentation caches only. Durable scope includes every `StorageRepository`, configuration document, world player data adapter, JSON, TOML, NBT, object store, queue, journal, receipt, migration marker, recovery record, backup, audit file, optional log, and durable cache that can change later behavior. An inventory based only on `StorageRepository` is incomplete.

Every durable owner must declare its path, schema or envelope version, identity and cardinality, size and record bounds, write and flush model, concurrency model, idempotency contract, migration rules, unsupported newer version behavior, corruption behavior, backup and restore path, privacy classification, retention, shutdown behavior, and evidence. Unsupported or damaged authoritative data must not become an empty successful state. A repair that changes a schema must preserve supported data through a versioned migration and recovery copy.

Security-sensitive file publication and append operations must bind validation and mutation to the same opened object. On Linux, macOS, and Windows, the owning writer must use a provider-specific or native descriptor or handle path that proves opened-object identity, regular-file or directory type, link or reparse state, and stable identity across the write and flush. Path-only checks, a pre-open identity check followed by an unrelated write, or an operating-system exclusion cannot satisfy this invariant. A missing native capability fails the affected operation closed and preserves prior valid state and recovery evidence.

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

Supply chain closure must evaluate the resolved candidate graph, the NeoForge-provided runtime graph on each mandatory operating system, and the packaged mod artifact as separate evidence, not only GitHub alert state. Every current critical or high alert must be repaired or shown inapplicable with exact dependency path, configuration, runtime packaging, affected API, reachability, authoritative advisory evidence, and compatible remediation evidence. Platform-owned alerts remain separate graph, packaged-artifact, reachability, advisory-applicability, and compatible-remediation gates. They cannot be suppressed, dismissed, or cleared by narrowing Linux, macOS, or Windows scope, by relying on a development-only override, or by observing that the mod JAR does not embed the platform library. A platform pin cannot be silently changed to clear an alert. If no compatible remedy exists and the vulnerability is applicable and exploitable, completion is blocked under `EXT-002`.

JNA or JNA Platform may be used as compile-only APIs only when the exact compatible versions are supplied by the pinned NeoForge 21.1.235 runtime on Linux, macOS, and Windows. The mod artifact must not embed a second JNA runtime, duplicate its native classes, or shadow the platform-owned copy. Compile success alone does not prove runtime presence, binary compatibility, native loading, or advisory closure; those facts require `EXT-001` and `EXT-002` evidence.

### Supported Environments and Generalization

Mandatory platform support is Minecraft Java on Linux, macOS, and Windows. All three operating systems must run Java 21 with Minecraft 1.21.1 and NeoForge 21.1.235. Windows is a supported mandatory target and must never be documented as unsupported merely because its runtime evidence is unavailable or blocked. `EXT-001` requires disposable native runtime evidence for every operating system; an unavailable environment leaves the relevant row blocked.

Mandatory evidence covers dedicated-server startup, operation, audit writing, save, bounded shutdown, and restart on Linux, macOS, and Windows. It also covers a matching enhanced client smoke workflow on every operating system, enhanced GUI disabled and enabled, a no SEF fallback client, an incompatible protocol fixture, and enhanced and fallback clients connected together. The complete functional matrix may share platform-independent proof only where the invariant and code path are demonstrably identical; native writer, filesystem, dependency loading, startup, shutdown, client presentation, input, and packaging behavior require direct evidence on each operating system. Evidence also covers clean and migrated synthetic data, fresh and restarted servers, multiple dimensions, online and known offline identities, registry content outside the `minecraft` namespace, and every catalog allowed command source. Current optional integrations must be tested present, absent, unavailable, and removed according to their declared ownership, including live permission provider behavior for the advertised LuckPerms contract.

Fixtures must not hard code one player, dimension, namespace, screen size, operating system, path syntax, filesystem behavior, or provider response in a way that hides general behavior. Permission tests include absent, explicit deny, explicit grant, inherited or wildcard state, refresh, reconnect, and outage. Target tests include self, online, offline, equal rank, higher rank, exempt, vanished, unknown, ambiguous, and stale identities where applicable. UI tests include graphical and command fallback paths. Persistence tests include empty, valid, legacy, unsupported newer, malformed, oversized, interrupted, concurrent, and recovered states on each platform-sensitive storage path.

### Determinism and Evidence Reproducibility

At one frozen revision, repeated generation must produce the same normalized command, permission, configuration, storage, UI, trust boundary, and dependency inventories. Tracked generated references must have zero unexplained drift. Identical initial semantic state and authorized input must reach the same canonical action, normalized domain result, permission decision class, audit action id, and durable semantic state after normalizing time, random identifiers, and environment specific paths.

Every evidence record must name the commit, branch, artifact path, SHA-256 and SHA-512 where applicable, operating system family and version, architecture, filesystem, Java and platform versions, resolved runtime dependency identities, fixture manifest, exact command or workflow, expected result, actual result, and disposition. Any implementation, configuration, dependency, schema, protocol, catalog, permission, generated reference, native provider, or test harness change invalidates affected evidence. The final phase reruns the complete required matrix after the last product change.

## 6. Mandatory Scope

- `SEFAUD-REQ-001` freezes the authoritative audit baseline and complete traceable inventories.
- `SEFAUD-REQ-002` closes security, privacy, sensitive data leak, and backdoor like authority risk.
- `SEFAUD-REQ-003` closes applicable dependency and supply chain risk, including platform-owned runtime dependencies without operating-system scope reduction.
- `SEFAUD-REQ-004` proves and repairs every executable administrator action.
- `SEFAUD-REQ-005` polishes and verifies every in scope graphical and textual operator UI.
- `SEFAUD-REQ-006` closes the full codebase persistence and database integrity audit.
- `SEFAUD-REQ-007` closes backend handling and cross channel integration behavior.
- `SEFAUD-REQ-008` supplies post change clean checkout, Linux, macOS, and Windows runtime, compatibility, recovery, and regression proof.
- `SEFAUD-REQ-009` reconciles documentation and evidence and closes the three-operating-system final endpoint.

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

### DEC-009 — Mandatory cross-platform Minecraft Java support and native dependency boundary

**Status:** RESOLVED
**Selected choice:** Minecraft Java support is cross-platform. Linux, macOS, and Windows are all mandatory, and Windows must not be documented as unsupported. Every security-sensitive writer whose safety depends on file identity must use a provider-specific or native implementation that proves the identity of the opened object on each operating system. JNA or JNA Platform may be compile only when the exact compatible API is supplied by the pinned NeoForge 21.1.235 runtime, and the mod must not embed a duplicate native runtime. Platform-owned dependency alerts remain separate graph, packaged-artifact, reachability, advisory-applicability, and compatible-remediation gates and cannot be suppressed or cleared by narrowing operating-system scope.
**Rationale:** Java portability does not make native filesystem identity, runtime dependency supply, binary compatibility, or advisory applicability portable by assertion. Release readiness requires direct proof on every supported operating system while retaining one pinned NeoForge boundary and one native runtime owner.
**Affected requirements:** SEFAUD-REQ-001, SEFAUD-REQ-002, SEFAUD-REQ-003, SEFAUD-REQ-004, SEFAUD-REQ-005, SEFAUD-REQ-006, SEFAUD-REQ-007, SEFAUD-REQ-008, SEFAUD-REQ-009
**Supersedes:** none

## 10. External Prerequisites

`EXT-001` and `EXT-002` are mandatory and currently `UNKNOWN`. The owner has locked a blocker-tolerant plan: all unblocked audit and remediation work may proceed in dependency order, but no affected requirement, owning phase, or final endpoint may pass until its prerequisite is satisfied. Partial Linux evidence does not satisfy a prerequisite that requires Linux, macOS, and Windows. Publishing, production credentials, production access, and destructive production verification remain excluded.

| ID | Prerequisite | Affected requirements | Availability | Authorization | Required external action |
|---|---|---|---|---|---|
| EXT-001 | Java 21 and NeoForge 21.1.235 runtime evidence on isolated Linux, macOS, and Windows systems for the exact candidate revision and artifact | `SEFAUD-REQ-002`, `SEFAUD-REQ-003`, `SEFAUD-REQ-004`, `SEFAUD-REQ-005`, `SEFAUD-REQ-006`, `SEFAUD-REQ-007`, `SEFAUD-REQ-008`, `SEFAUD-REQ-009` | unknown | not_required | Provide disposable environments or repository-owned runners for all three operating systems, without production credentials or data, and retain the required sanitized evidence packet. |
| EXT-002 | Authoritative provenance, exact runtime supply, advisory applicability, and compatible closure for NeoForge-owned dependencies, including any JNA and JNA Platform APIs used by the mod | `SEFAUD-REQ-003`, `SEFAUD-REQ-008`, `SEFAUD-REQ-009` | unknown | not_required | Obtain authoritative NeoForge artifact metadata and resolved runtime evidence plus authoritative advisory or upstream remediation evidence sufficient to close every required gate. If an applicable finding has no compatible remedy within `DEC-004`, keep the plan blocked and request an owner decision. |

### EXT-001 — Disposable Linux, macOS, and Windows runtime evidence

**Evidence contract:** For one exact candidate commit and matching SHA-256 and SHA-512 artifact digests, capture sanitized environment manifests for Linux, macOS, and Windows that name operating-system version, architecture, filesystem, Java 21 vendor and version, Minecraft 1.21.1, NeoForge 21.1.235, Gradle invocation, resolved runtime dependency identities, and fixture identity. On each operating system, pass clean dependency resolution and build, dedicated-server startup and bounded shutdown, matching-client startup and an enhanced UI smoke workflow, audit append, flush, rotation, restart, and provider failure behavior. Exercise the platform-native identity boundary against safe regular objects and synthetic symbolic-link, hard-link, reparse-point, object-swap, unsupported-metadata, and write-failure cases that apply to that operating system. Evidence must prove that validation and mutation used the same opened object and that failure preserved prior valid state. No production system or personal data may be used.

**Blocker routing:** `SEFAUD-PHASE-001` may complete unblocked analysis and remediation, but it cannot exit or transition while the cross-platform security and dependency runtime rows are unavailable or failed. Later operating-system-specific UI, persistence, lifecycle, and final runtime rows remain blocked as mapped above. A lower-fidelity unit test, mocked operating-system name, single-platform result, or documentation claim cannot satisfy this prerequisite.

### EXT-002 — Authoritative platform dependency provenance and advisory closure

**Evidence contract:** Capture the exact NeoForge 21.1.235 authoritative artifacts and metadata, repository origin, coordinates, versions, SHA-256 and SHA-512 digests, licenses or provenance, and resolved compile and runtime paths that supply every platform-owned dependency used by the candidate. For JNA and JNA Platform, prove that the exact compile-only APIs are present and binary compatible at runtime on Linux, macOS, and Windows and that the mod JAR embeds no duplicate JNA classes or native libraries. For every applicable alert, retain separate candidate graph, packaged-mod-artifact, installed-runtime-artifact, affected-API reachability, authoritative advisory applicability, and compatible remediation results. An upstream fixed artifact or other remedy must remain compatible with `DEC-004`; a development-only override, absence from the mod JAR, transitive ownership, or reduced operating-system scope is not closure.

**Blocker routing:** `SEFAUD-REQ-003` and `SEFAUD-PHASE-001` remain open until authoritative evidence closes every critical and high platform-owned alert or a compatible remedy is implemented and verified. `SEFAUD-REQ-008`, `SEFAUD-REQ-009`, and the completion endpoint remain open until the final runtime graph and artifact evidence reproduce that closure. Unknown or unavailable upstream evidence produces `PLAN_VALIDATED_WITH_KNOWN_EXTERNAL_BLOCKER`, never a suppressed alert or a narrower support claim.

Required test clients, synthetic identities, disposable worlds, local network interruption, graphical capture, failure injection, and optional provider fixtures remain execution inputs, not authority to use a production system. Lower-fidelity evidence cannot replace either prerequisite, and implementation completion cannot be claimed while either required evidence contract is unsatisfied.

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
| Platform-native audit writer | Audit writer and its Linux, macOS, and Windows provider implementations | Owned audit directories and files, opened descriptors or handles, identity and link metadata, bounded append bytes, flush result | Validation and mutation stay bound to the same opened object on every mandatory operating system. Unsupported identity or link metadata fails closed. |
| Configuration | `ModuleConfigRegistry`, `ModuleConfigService`, NeoForge bootstrap configuration | Typed module files, revisions, migration candidates, generated reference | Publication is transactional, secret fields are filtered, previous known good state survives failure, and reload invalidates dependent authority. |
| Audit and observation | `AuditService`, command journal, redaction policy, optional file sink, observer projections | Immutable action metadata, redacted parameters, health and retention state | Mandatory security audit cannot be disabled by observation filters, and no sink receives data beyond its authorization and retention contract. |
| Optional integrations | Runtime guarded providers and adapters for LuckPerms, FTB Essentials, Curios, and declared bridges | Provider data, optional capability, absent and failed states | Absence cannot block core startup or grant authority. Adapter scope is explicit, bounded, and revocable. |
| Build, tests, and generated references | Gradle build, source and test sets, GameTests, reference generators, CI | Source, resources, resolved dependencies, reports, JAR, evidence | Clean checkout results are reproducible, generated references match live contracts, and the artifact contains only intended content. |
| Platform dependency boundary | Pinned NeoForge 21.1.235 runtime and candidate Gradle declarations | Platform-owned compile and runtime artifacts, JNA APIs, native libraries, advisories, candidate JAR | Compile-only APIs resolve only from the compatible pinned runtime, the mod embeds no duplicate native runtime, and every alert retains separate graph, artifact, reachability, advisory, and remediation evidence. |

Dependency direction is server authority to presentation, catalog policy to domain mutation, and domain snapshots to persistence. The platform-native audit writer depends on the pinned NeoForge-supplied native API at runtime; the mod does not own or embed a second native runtime. Client presentation, generated documentation, logs, cached provider data, and recovery artifacts never become independent authority. Optional integrations sit behind runtime guards and bounded adapters. Dedicated server paths must not load client classes.

The primary trust boundaries are command parsing, source classification, permission providers, temporary or delegated authority, client payload decoding, GUI projection, stored command indirection, configuration and migration input, filesystem path ownership, opened-object identity, platform-native calls, persistence deserialization, archive and image import, optional mod reflection, mixins and access transformers, audit and export projection, NeoForge-owned runtime dependency supply, dependency resolution, and packaged artifacts. Every boundary appears in the Phase 000 matrix with an owner, input class, validation, failure behavior, downstream effect, and required evidence.

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
11. Linux, macOS, and Windows are mandatory Minecraft Java targets. A missing environment is a blocker, not an unsupported-platform declaration.
12. A security-sensitive writer validates type, link or reparse state, and identity on the same opened descriptor or handle used for mutation and proves stable opened-object identity after flush.
13. JNA and JNA Platform remain compile only and may be used only when supplied compatibly by the pinned NeoForge runtime; the candidate JAR contains no duplicate JNA classes or native runtime.
14. Platform-owned alerts retain independent graph, packaged-artifact, installed-runtime, reachability, advisory-applicability, and compatible-remediation gates on all three operating systems.

## 12. Requirements

### SEFAUD-REQ-001 — Authoritative audit baseline and traceability

**Behavior:** Freeze the exact execution revision and create complete, deduplicated inventories for security boundaries, data flows, live Brigadier routes, catalog actions, shortcuts, source classes, graphical and textual UI, `StorageRepository` and nonrepository durable state, backend lifecycle handlers, integrations, configuration, schemas, Linux, macOS, and Windows providers, platform-owned dependencies, tests, documentation, external prerequisites, and known evidence gaps. Every item has one owner, one audit disposition, and one evidence route.
**Owner:** Repository audit contract
**Contributors:** All source domains, build system, documentation, and remote security evidence
**Dependencies:** DEC-002, DEC-003, DEC-004, DEC-005, DEC-006, DEC-007, DEC-008, DEC-009
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
7. Linux, macOS, and Windows each have an explicit runtime, filesystem, native-writer, client, server, dependency, and evidence row. `EXT-001` and `EXT-002` availability and required evidence are recorded without converting an unavailable row into an exclusion.

**Required evidence**

1. CodeGraph and build derived inventory reports tied to the frozen commit.
2. Live catalog, dispatcher, shortcut, permission, descriptor, repository, configuration, source set, dependency, and documentation reconciliation results.
3. A sanitized baseline manifest and requirement to phase to evidence traceability matrix.
4. Drift checks that fail on an added or removed relevant surface without an owned row.
5. Cross-platform environment and native dependency prerequisite matrix with exact `EXT-001` and `EXT-002` blocker routing.

### SEFAUD-REQ-002 — Security, privacy, leak, and backdoor like path closure

**Behavior:** Threat model, inspect, repair, and adversarially verify every authority, command execution, payload, filesystem, persistence, platform-native writer, optional integration, logging, audit, export, projection, and packaged artifact boundary on Linux, macOS, and Windows. Confirmed authorization bypasses, sensitive data leaks, unintended authority routes, unsafe opened-object handling, and other endpoint relevant security defects receive regression proof.
**Owner:** Security audit boundary
**Contributors:** Command kernel, GUI protocol, persistence, configuration, optional integrations, domain services, packaging
**Dependencies:** SEFAUD-REQ-001, DEC-001, DEC-003, DEC-005, DEC-009, EXT-001
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
8. Every security-sensitive writer uses a provider-specific or native descriptor or handle on Linux, macOS, and Windows, proves the identity and safe type of the opened object before mutation, proves the same object remains open through flush, rejects unsafe link or reparse state, and fails closed when required identity metadata is unavailable.

**Required evidence**

1. Threat boundary matrix and source to sink review with stable finding ids and exact dispositions.
2. Targeted unit, integration, GameTest, payload, parser, permission, redaction, filesystem, and recovery regression results.
3. Read only code scanning and secret scanning results plus repository and JAR secret and path inspection.
4. Updated security review tied to the final repaired commit, with limitations and invalidation rules.
5. `EXT-001` runtime evidence for native writer success, object substitution, hard-link, symbolic-link or reparse-point, metadata-unavailable, write-failure, flush, rotation, and restart behavior on each mandatory operating system.

### SEFAUD-REQ-003 — Dependency and supply chain closure

**Behavior:** Resolve every current dependency alert against the actual release candidate dependency graph, NeoForge-provided runtime graph on Linux, macOS, and Windows, and packaged mod JAR. Repair applicable findings without changing the pinned platform boundary, and prove that no applicable critical or high exploitable dependency vulnerability remains. Platform ownership and operating-system scope do not waive any evidence gate.
**Owner:** Dependency graph
**Contributors:** NeoForge platform, optional integration declarations, CI, packaging, remote security state
**Dependencies:** SEFAUD-REQ-001, DEC-003, DEC-004, DEC-009, EXT-001, EXT-002
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
7. Every platform-owned alert has separate candidate graph, packaged-mod-artifact, installed-runtime-artifact, affected-API reachability, authoritative advisory applicability, and compatible remediation dispositions. None is suppressed or cleared by narrowing Linux, macOS, or Windows support.
8. JNA and JNA Platform remain compile only, the exact compatible APIs are supplied by the pinned NeoForge runtime on each mandatory operating system, native loading succeeds, and the candidate mod JAR contains no duplicate JNA classes or native runtime.

**Required evidence**

1. Gradle resolved compile and runtime dependency reports on Linux, macOS, and Windows, packaged mod and installed runtime class and metadata inspection, and artifact digests.
2. Alert disposition table linked to authoritative advisories and exact candidate graph, platform runtime, packaged artifact, affected API, and reachability evidence.
3. Compatibility tests for every changed or compile-only dependency and present and absent optional integration state on affected operating systems.
4. Final remote alert and security scan snapshot, with branch applicability clearly separated from repository default branch state.
5. `EXT-002` provenance ledger and compatible remediation closure, including exact NeoForge-owned JNA and JNA Platform supply and proof that no duplicate native runtime is embedded.

### SEFAUD-REQ-004 — Administrator command behavioral closure

**Behavior:** Complete one universal matrix row for every implemented, enabled, or otherwise executable administrator action. Verify registration, discovery, permission states, allowed and denied sources, targets, arguments, effects, failures, persistence, equivalent routes, feedback, audit, and redaction. Repair every confirmed defect. Intentionally unavailable control families remain an explicit negative contract and are never counted as working commands.
**Owner:** Command policy kernel
**Contributors:** Permission service, domain command owners, GUI protocol, aliases, bundles, sudo, server controls, audit service, persistence
**Dependencies:** SEFAUD-REQ-001, SEFAUD-REQ-002, SEFAUD-REQ-003, SEFAUD-REQ-006, DEC-002, DEC-008, DEC-009, EXT-001
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
11. A representative privileged mutation, denial, persistence, restart, feedback, and audit workflow passes through the same candidate artifact on Linux, macOS, and Windows, while the universal action matrix remains bound to the shared canonical semantics.

**Required evidence**

1. A machine checked command matrix with one row per executable administrative action and negative rows for unavailable families.
2. Unit and dispatcher tests, command contract GameTests, domain mutation GameTests, and disposable multi actor manual workflows.
3. Before and after state hashes or domain assertions, restart proof, feedback capture, and redacted audit capture for each mutation class.
4. Generated command and permission references reconciled with the live tree and final documentation.
5. `EXT-001` cross-platform representative command workflow evidence tied to the exact candidate artifact and each operating-system runtime manifest.

### SEFAUD-REQ-005 — UI polish and accessibility closure

**Behavior:** Inventory, polish, and verify all in scope `SefScreen` family screens, HUD surfaces, pause entry, administrative workflows, confirmation screens, pickers, item browsers, fallbacks, and administrator command feedback. The result is responsive, readable, accessible, state clear, privacy safe, and server authoritative.
**Owner:** GUI presentation
**Contributors:** GUI protocol, command and message services, domain workflow owners, client caches, accessibility and test harness
**Dependencies:** SEFAUD-REQ-001, SEFAUD-REQ-002, SEFAUD-REQ-004, DEC-008, DEC-009, EXT-001
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
9. A matching enhanced client on Linux, macOS, and Windows passes startup, navigation, input, narration, resize, disconnect, and command-fallback smoke workflows. Platform-specific rendering or input defects reopen this requirement rather than narrowing support.

**Required evidence**

1. Automated screen, layout helper, workflow compiler, payload codec, session, stale state, and permission tests.
2. Screenshot set for every screen class at required scales and representative resolutions, plus recordings for dynamic, focus, narration, revocation, reconnect, and animation workflows.
3. Enhanced and fallback client comparison with matching command results and server audit records.
4. Accessibility and visual review ledger tied to the frozen revision and client environment.
5. `EXT-001` client runtime manifests and sanitized captures for every mandatory operating system.

### SEFAUD-REQ-006 — Full codebase persistence and database integrity closure

**Behavior:** Audit and repair every durable store and cross store invariant for schema, bounds, atomicity, directory durability, concurrency, idempotency, lifecycle flush, corruption, recovery, migration, rollback, path safety, retention, and privacy. The term database includes file backed repositories, JSON, TOML, NBT, journals, queues, receipts, indexes, object stores, backups, and any other durable authority in this codebase.
**Owner:** Persistence layer
**Contributors:** Every domain with durable state, configuration service, audit and logging, offline adapters, lifecycle coordination
**Dependencies:** SEFAUD-REQ-001, SEFAUD-REQ-002, DEC-005, DEC-009, EXT-001
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
11. Every security-sensitive write path uses the operating-system provider required by `DEC-009` and passes normal, linked or reparse, object-swap, identity-metadata failure, write failure, flush, rotation, shutdown, and restart evidence on Linux, macOS, and Windows without silently falling back to a path-only writer.

**Required evidence**

1. Complete durable owner matrix and schema and path inventory.
2. Unit, integration, fault injection, concurrent write, process interruption, migration, rollback, corruption, recovery, shutdown, and restart results.
3. File and semantic state hashes before and after failure and recovery, with synthetic fixtures and exact operation ids.
4. Storage diagnostics, worker health, recovery artifacts, and updated persistence and migration documentation.
5. `EXT-001` native filesystem and opened-object identity evidence for all three mandatory operating systems.

### SEFAUD-REQ-007 — Backend handling and cross channel integration closure

**Behavior:** Verify and repair backend behavior across initialization, registration, startup, reload, runtime mode change, permission and policy revision, command and GUI convergence, persistence, optional provider loss, disconnect, reconnect, dimension change, shutdown, retry, partial failure, audit correlation, and client, common, and dedicated server boundaries.
**Owner:** Lifecycle integration boundary
**Contributors:** Mod lifecycle, command kernel, GUI network, persistence, configuration, optional integrations, all stateful domain services
**Dependencies:** SEFAUD-REQ-002, SEFAUD-REQ-003, SEFAUD-REQ-004, SEFAUD-REQ-005, SEFAUD-REQ-006, DEC-009, EXT-001
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
11. Dedicated server, matching enhanced client, fallback path, native audit provider, platform-owned runtime dependencies, reload, reconnect, save, bounded shutdown, and restart integrate successfully on Linux, macOS, and Windows.

**Required evidence**

1. Lifecycle and cross channel sequence matrix with failure injection at each boundary.
2. Dedicated server, enhanced client, fallback client, incompatible protocol, mixed multiplayer, reconnect, reload, provider outage, dimension, and shutdown workflows.
3. Correlated before and after state, audit events, diagnostics, thread safety assertions, and recovery records.
4. Dedicated server classloading and final JAR package boundary inspection.
5. `EXT-001` lifecycle and native dependency loading evidence for each mandatory operating system.

### SEFAUD-REQ-008 — Post change runtime, compatibility, recovery, and regression proof

**Behavior:** From a clean checkout of the final candidate revision, pass the complete maintained static, unit, generated reference, GameTest, build, Linux, macOS, and Windows server and client, mixed multiplayer, provider, UI, packet abuse, recovery, performance, dependency, artifact, secret, and diff verification matrix.
**Owner:** Release verification system
**Contributors:** All mandatory requirement owners, Gradle build, CI, staging harness, documentation evidence
**Dependencies:** SEFAUD-REQ-002, SEFAUD-REQ-003, SEFAUD-REQ-004, SEFAUD-REQ-005, SEFAUD-REQ-006, SEFAUD-REQ-007, DEC-009, EXT-001, EXT-002
**Lifecycle stage:** post_change
**Production verification:** none
**Release impact:** stable release

**Acceptance criteria**

1. The maintained Gradle `check` lifecycle aggregates a deterministic format gate, compiler warning zero growth gate, reviewed static analysis, unit tests, and risk based line and branch coverage for security and persistence critical code. Suppressions are narrow, justified, and reviewed.
2. A clean Java 21 checkout passes the operating-system-appropriate Gradle wrapper invocation for `check build compileFallbackRuntimeJava generateProjectReferences generatePerformanceReport` on Linux, macOS, and Windows, and zero unexplained tracked generated reference drift remains.
3. All required GameTests pass, including command contract, domain mutation, persistence, permission, integration, and regression coverage. Test counts and catalog counts match the frozen baseline manifest.
4. Dedicated server startup, steady operation, diagnostics, native audit writing, save, bounded shutdown, and restart pass on Linux, macOS, and Windows with enhanced GUI disabled and enabled as applicable.
5. Matching enhanced client startup and command fallback smoke pass on Linux, macOS, and Windows. The full matching enhanced, no SEF fallback, incompatible protocol, and mixed-client matrix connects, remains stable, exercises required workflows, reconnects, and clears state correctly.
6. Current permission provider and applicable optional integrations pass present, absent, outage, refresh, removal, and fallback matrices. Blocked provider specific evidence remains a blocker.
7. GUI visual, accessibility, command feedback, InvSee, admission capacity and FIFO, disguise animation, packet abuse, permission revocation, reconnect, and cross dimension workflows pass at the required fidelity.
8. Persistence process interruption, recovery, migration, rollback, shutdown timeout, and restart workflows pass from disposable snapshots.
9. Performance budgets cover deterministic metadata work and relevant server tick, memory, queue, scan, payload, rendering, and persistence hot paths without unbounded work or log spam.
10. The final JAR, dependency metadata, mixins, access transformer, resources, generated references, licenses, hashes, secrets, host paths, logs, caches, and duplicate entries pass inspection. The complete Git diff contains no unrelated change or user data.
11. Any final product change invalidates affected evidence and triggers the prescribed rerun. The complete full matrix runs after the last change.
12. The installed NeoForge runtime graph on Linux, macOS, and Windows supplies every required compile-only native API at the proven compatible version; native loading and behavior pass; the mod JAR embeds no duplicate JNA runtime; and every platform-owned critical or high alert satisfies the separate `EXT-002` closure gates.

**Required evidence**

1. Clean checkout command logs and CI results tied to the exact commit and Java environment.
2. Unit, coverage, static analysis, GameTest, server, client, multiplayer, provider, recovery, performance, and UI evidence manifests.
3. Final JAR SHA-256 and SHA-512, entry inventory, dependency manifest, and complete diff audit.
4. One final rerun ledger with no failed, incomplete, or improperly downgraded mandatory row.
5. `EXT-001` three-operating-system runtime packet and `EXT-002` authoritative platform dependency provenance and advisory closure packet.

### SEFAUD-REQ-009 — Documentation, evidence, and final endpoint closure

**Behavior:** Reconcile all affected documentation and sanitized evidence with the final implementation and artifact, then run a final cross matrix audit. Close only when every mandatory row and endpoint condition passes.
**Owner:** Documentation evidence
**Contributors:** All mandatory requirement owners, user and operator documentation, test and security records, release workflow
**Dependencies:** SEFAUD-REQ-001, SEFAUD-REQ-002, SEFAUD-REQ-003, SEFAUD-REQ-004, SEFAUD-REQ-005, SEFAUD-REQ-006, SEFAUD-REQ-007, SEFAUD-REQ-008, DEC-009, EXT-001, EXT-002
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
8. Compatibility, installation, troubleshooting, security, acceptance, and release documents identify Linux, macOS, and Windows as mandatory supported Minecraft Java targets. Missing evidence is labeled blocked, never translated into a claim that Windows or another mandatory operating system is unsupported.
9. Documentation records the opened-object identity writer contract, compile-only JNA ownership, absence of an embedded duplicate native runtime, platform dependency provenance, separate alert closure gates, and the exact status of `EXT-001` and `EXT-002`.

**Required evidence**

1. Documentation drift and link checks plus generated reference comparison.
2. Final requirement to phase to test to artifact traceability report.
3. Final security, command, UI, persistence, integration, compatibility, and release readiness audit tied to one commit and artifact digest.
4. Completed Definition of Done checklist and Goal Creator completion packet, without publication action.
5. Cross-platform documentation claim audit and resolved links to the `EXT-001` and `EXT-002` evidence packets.

## 13. Phased Roadmap

The master owns this global sequence, canonical requirement ownership, dependency topology, and completion authority. Each linked phase file owns the sole full phase declaration and detailed execution blueprint. Phase files must use `PLAN-PHASE-NNN`, stable `P<NNN>-TASK-###` task ids, and the exact scope frozen here.

| Phase ID | Objective | Owner | Dependencies | Canonical requirements | Entry summary | Exit summary | Next transition | Execution blueprint |
|---|---|---|---|---|---|---|---|---|
| `SEFAUD-PHASE-000` | Freeze one reproducible audit baseline and complete every authoritative inventory, operating-system prerequisite row, and traceability matrix. | Repository audit contract | `DEC-002`, `DEC-003`, `DEC-004`, `DEC-005`, `DEC-006`, `DEC-007`, `DEC-008`, `DEC-009` | `SEFAUD-REQ-001` | The validated plan set is integrated, repository identity matches, and the candidate lineage is available without losing unrelated user state. | Exact current counts and owners reconcile across source, runtime, Linux, macOS, Windows, native providers, generated references, persistence, UI, integrations, dependencies, tests, and docs. `EXT-001` and `EXT-002` are routed explicitly, and no mandatory surface is missing, duplicated, or unowned. | `SEFAUD-PHASE-001` | [Phase 000](phases/plan-phase-000.md) |
| `SEFAUD-PHASE-001` | Close security, privacy, backdoor like path, opened-object writer, and dependency supply chain risk with adversarial cross-platform regression evidence. | Security audit boundary | `SEFAUD-PHASE-000`, `SEFAUD-REQ-001`, `DEC-009`, `EXT-001`, `EXT-002` | `SEFAUD-REQ-002`, `SEFAUD-REQ-003` | Phase 000 matrices, frozen dependency graph, trust boundaries, data classes, operating-system rows, and evidence rules are complete. Unavailable external evidence is recorded before unblocked work proceeds. | Every boundary and alert has an evidence-based disposition; all confirmed mandatory findings are repaired; opened-object identity and native runtime supply pass on Linux, macOS, and Windows; platform-owned alert gates close; and no known prohibited security or applicable critical or high dependency finding remains. | `SEFAUD-PHASE-002` | [Phase 001](phases/plan-phase-001.md) |
| `SEFAUD-PHASE-002` | Close full codebase persistence and database integrity across normal, concurrent, corrupt, interrupted, migrated, recovered, and platform-sensitive filesystem states. | Persistence layer | `SEFAUD-PHASE-001`, `SEFAUD-REQ-002`, `SEFAUD-REQ-003`, `DEC-009`, `EXT-001` | `SEFAUD-REQ-006` | Security, platform-native writer, dependency, and filesystem boundaries are closed, and every durable owner has a Phase 000 row. | Every durable owner and cross-store invariant passes its matrix; platform-sensitive write and recovery behavior passes on Linux, macOS, and Windows; all confirmed integrity defects are repaired; and recovery evidence proves no silent loss or unsafe empty success. | `SEFAUD-PHASE-003` | [Phase 002](phases/plan-phase-002.md) |
| `SEFAUD-PHASE-003` | Prove every executable administrator action and every unavailable negative contract across authority, effects, failure, persistence, feedback, audit, and representative cross-platform runtime. | Command policy kernel | `SEFAUD-PHASE-002`, `SEFAUD-REQ-006`, `DEC-009`, `EXT-001` | `SEFAUD-REQ-004` | Security and persistence foundations pass, the authoritative executable action set is frozen, and all mandatory operating-system environments are available. | Every executable administrator action has a passed universal matrix row, representative privileged workflows pass on Linux, macOS, and Windows, unavailable families fail closed, every confirmed command defect is repaired, and generated references match the live tree. | `SEFAUD-PHASE-004` | [Phase 003](phases/plan-phase-003.md) |
| `SEFAUD-PHASE-004` | Deliver polished, accessible, responsive, state clear, privacy safe, and cross-platform graphical and textual operator UI. | GUI presentation | `SEFAUD-PHASE-003`, `SEFAUD-REQ-004`, `DEC-009`, `EXT-001` | `SEFAUD-REQ-005` | Command semantics, permissions, domain effects, unavailable states, and mandatory client environments are stable enough to serve as UI authority. | Every UI surface passes layout, input, accessibility, state, revocation, privacy, and fallback matrices, matching-client smoke passes on Linux, macOS, and Windows, and no known blocking or materially misleading defect remains. | `SEFAUD-PHASE-005` | [Phase 004](phases/plan-phase-004.md) |
| `SEFAUD-PHASE-005` | Close backend lifecycle and cross-channel integration under normal operation, revision change, provider failure, reconnect, partial failure, shutdown, and all mandatory operating systems. | Lifecycle integration boundary | `SEFAUD-PHASE-004`, `SEFAUD-REQ-005`, `DEC-009`, `EXT-001` | `SEFAUD-REQ-007` | Security, persistence, commands, and UI each pass their owned contract at the current integrated revision on the required environments. | Lifecycle and channel matrices prove consistent server authority, equivalence, failure, recovery, classloading, native dependency loading, and audit behavior on Linux, macOS, and Windows, with every confirmed integration defect repaired. | `SEFAUD-PHASE-006` | [Phase 005](phases/plan-phase-005.md) |
| `SEFAUD-PHASE-006` | Produce complete clean-checkout static, Linux, macOS, and Windows runtime, multiplayer, compatibility, recovery, performance, dependency, and artifact proof after all product changes. | Release verification system | `SEFAUD-PHASE-005`, `SEFAUD-REQ-007`, `DEC-009`, `EXT-001`, `EXT-002` | `SEFAUD-REQ-008` | All change-stage requirements are integrated, no known owned defect remains, both external prerequisites are satisfied, and the candidate revision is frozen for final verification. | Every mandatory automated and real workflow passes at the required fidelity on its required operating system, platform-owned dependencies close separately, the final artifact and diff pass inspection, and no result is stale, blocked, or replaced by lower-fidelity proof. | `SEFAUD-PHASE-007` | [Phase 006](phases/plan-phase-006.md) |
| `SEFAUD-PHASE-007` | Reconcile cross-platform documentation and evidence, audit the complete integrated plan endpoint, and close release readiness without publishing. | Documentation evidence | `SEFAUD-PHASE-006`, `SEFAUD-REQ-008`, `DEC-009`, `EXT-001`, `EXT-002` | `SEFAUD-REQ-009` | The final verified commit and artifact digest are immutable for closure, Linux, macOS, and Windows evidence is complete, platform dependency provenance and advisory closure are complete, and all Phase 006 evidence is complete. | Documentation identifies all three operating systems as supported, generated references, external-prerequisite evidence, requirement traceability, phase gates, and Section 18 all pass at one revision. No disallowed known defect or unclosed mandatory row remains. | Final plan-wide completion | [Phase 007](phases/plan-phase-007.md) |

Phase ids are contiguous from `SEFAUD-PHASE-000` through `SEFAUD-PHASE-007`. Dependencies move backward only. `EXT-001` and `EXT-002` are currently unknown external exit dependencies. The already active Phase 001 may continue work that does not require them, but it cannot pass, integrate, or transition to Phase 002 until both Phase 001 evidence contracts are satisfied. A later phase cannot begin until the prior phase implementation, evidence, pull request checks, integration, resulting candidate branch verification, and required signed phase tag satisfy the repository workflow. No phase may pass while a known mandatory phase-owned defect or prerequisite blocker remains.

## 14. Verification Strategy

| Requirement | Static or unit evidence | Integration evidence | Real behavior evidence | Security and negative evidence | Artifact or runtime evidence |
|---|---|---|---|---|---|
| `SEFAUD-REQ-001` | Inventory parsers, schema checks, duplicate and ownership checks | Live catalog, dispatcher, reference, repository, build, operating-system, native-provider, and dependency reconciliation | Baseline capture from a clean candidate checkout plus explicit `EXT-001` and `EXT-002` state | Missing, duplicate, stale, unowned, and silently excluded operating-system surface injection tests | Commit, environment, dependency, prerequisite, and evidence manifest |
| `SEFAUD-REQ-002` | Permission, redaction, parser, payload, path, archive, audit, native-writer, and policy tests | Cross-boundary authority, data-flow, and opened-object identity tests | Multi-actor adversarial workflows, provider failure, and native writer runtime on Linux, macOS, and Windows | Forgery, replay, revocation, indirect execution, path escape, object substitution, link or reparse abuse, sensitive output, and secret scans | Security report, `EXT-001` packet, scanning snapshot, JAR and export inspection |
| `SEFAUD-REQ-003` | Candidate and platform dependency graph, binary compatibility, and advisory applicability analysis | Changed and compile-only dependency build, runtime load, and optional integration tests | Dedicated-server and client startup with the resolved NeoForge runtime graph on Linux, macOS, and Windows | Separate graph, mod artifact, installed runtime, reachability, advisory, compatible-remediation, and duplicate-native-runtime gates | `EXT-002` provenance ledger, resolved graphs, metadata, licenses, JAR entries, hashes |
| `SEFAUD-REQ-004` | Dispatcher, catalog, permission, argument, policy, and domain unit tests | Command contract and mutation GameTests | Universal multi-actor administrator matrix plus representative mutation, denial, persistence, restart, feedback, and audit workflows on Linux, macOS, and Windows | Denied sources, permission states, targets, stale confirmation, failures, unavailable families, audit redaction | Generated command and permission references, `EXT-001` runtime manifests, state and audit evidence |
| `SEFAUD-REQ-005` | Screen, layout helper, workflow, session, codec, and state tests | Enhanced and fallback action equivalence | Full visual and accessibility matrix plus matching-client input, navigation, narration, resize, disconnect, and fallback smoke on Linux, macOS, and Windows | Revocation, stale state, hidden identity, unavailable control, invalid input, and platform-specific failure | Screenshot, recording, and `EXT-001` client manifest tied to commit |
| `SEFAUD-REQ-006` | Repository, schema, atomic store, native writer, worker, bounds, migration, and cross-store tests | Concurrent, opened-object identity, fault injection, interruption, shutdown, restore, and restart tests | Disposable world corruption, native filesystem substitution, migration, process termination, and recovery drills on each platform-sensitive path | Unsafe path, symlink, hard link, reparse point, object swap, missing identity metadata, malformed and oversized data, stale revision, duplicate side effect, privacy, and retention | `EXT-001` platform evidence, before and after hashes, journals, receipts, backups, diagnostics |
| `SEFAUD-REQ-007` | Lifecycle, revision, thread, classloading, native dependency load, provider, and correlation tests | Command, GUI, persistence, configuration, provider, and domain convergence | Dedicated server and matching-client smoke on Linux, macOS, and Windows plus mixed clients, reload, reconnect, dimension, outage, and shutdown | Partial failure, stale state, unavailable adapter, missing native API, client authority, side and thread misuse | `EXT-001` lifecycle traces, correlated audit, diagnostics, JAR boundary inspection |
| `SEFAUD-REQ-008` | Maintained format, warnings, static analysis, unit, coverage, and drift gates | Complete GameTest, cross-platform build, dependency, and CI matrix | Linux, macOS, and Windows server and client runtime, enhanced, fallback, multiplayer, provider, UI, recovery, and performance matrix | Packet abuse, native object substitution, secret scan, failure injection, platform alert closure, duplicate native runtime, artifact and diff audit | `EXT-001`, `EXT-002`, final clean-checkout logs, JAR hashes, dependency and evidence manifests |
| `SEFAUD-REQ-009` | Documentation links, generated reference drift, cross-platform terminology, support-claim, and blocker checks | Requirement to phase to prerequisite to evidence reconciliation | Operator procedures replayed against the final artifact on Linux, macOS, and Windows | False completion, stale claim, unsupported-Windows claim, suppressed platform alert, unavailable feature advertisement, secret and personal data review | Final evidence index, external-prerequisite closure, Definition of Done, release-readiness record |

Tests use synthetic identities and disposable worlds. Destructive cases are isolated from production. Expected and actual results are recorded for each row. A row marked blocked remains incomplete. Unit proof or an operating-system name mock cannot replace required GameTest, Linux, macOS, or Windows native runtime, dedicated server, client, multiplayer, graphical, provider, interruption, migration, recovery, dependency, or artifact proof.

The final maintained command order is:

1. Run the repository format and compiler warning gates through the operating-system-appropriate Gradle wrapper `check` invocation.
2. Run maintained static analysis, unit tests, and risk-based coverage through the same wrapper on the required clean environments.
3. Run generated reference and performance generation and require zero unexplained tracked drift.
4. Run all required GameTests.
5. Run `./gradlew build` and fallback runtime compilation from a clean checkout.
6. On Linux, macOS, and Windows, run clean dependency resolution, dedicated-server startup, operation, native audit writing, save, shutdown, and restart.
7. On Linux, macOS, and Windows, run matching enhanced-client startup and UI and fallback smoke; then run the complete enhanced, fallback, incompatible-protocol, and mixed-multiplayer matrix.
8. Run provider, GUI, command, native-writer substitution, persistence interruption, migration, rollback, and recovery matrices at every required platform fidelity.
9. Inspect performance, the final JAR, platform runtime graphs, JNA ownership, dependencies, resources, secrets, and complete diff; close `EXT-002` separately from packaged-mod inspection.
10. Rerun every invalidated row after the last product change, then run the final cross matrix audit.

## 15. Compatibility and Versioning, Migration, Rollout, Rollback, and Recovery

The supported platform remains Minecraft 1.21.1, NeoForge 21.1.235, Java 21, the checked-in Gradle wrapper, Parchment 2024.11.17, mod id `sef`, and artifact version lineage 2.0.0. NeoForge only is supported. Minecraft Java runtime support is mandatory on Linux, macOS, and Windows. Windows is not an optional target and must not be documented as unsupported. An unavailable `EXT-001` environment is a release blocker, not a compatibility exclusion. The universal JAR remains optional on clients. Enhanced GUI disablement, vanilla or no SEF clients, compatible enhanced clients, and incompatible enhanced protocol clients retain their documented behavior on every mandatory operating system.

Product, network protocol, configuration documentation, persistent schema, generated reference, and public identifier versions remain governed by their current source owners. A repair that changes a wire shape, schema, serialized field, config field, permission id, action id, resource location, or public route must either remain backward compatible or make the smallest appropriate versioned migration. Unsupported newer input fails explicitly. Unknown fields are preserved only where the existing owner contract permits them.

Optional integrations remain optional and runtime guarded. LuckPerms API 5.4, FTB Essentials, Curios, and other declared adapter contracts must be tested against compatible target artifacts where advertised. Absence or failure preserves the documented fallback. A dependency security remedy must not silently broaden or narrow the advertised platform range.

Security-sensitive writes require an operating-system provider that proves the opened object's identity. Linux and macOS may share a POSIX implementation only where each operating system receives direct runtime proof for its own flags, structure layouts, descriptor identity, link count, write, and flush behavior. Windows requires direct handle identity, reparse state, link count, object stability, write, and flush proof. No provider may silently fall back to a path-only check or call Windows unsupported.

JNA and JNA Platform may remain compile-only dependencies only when `EXT-002` proves that the pinned NeoForge 21.1.235 runtime supplies the exact compatible APIs and `EXT-001` proves native loading and behavior on Linux, macOS, and Windows. The mod JAR must not embed another JNA implementation or native runtime. Platform-owned dependency findings remain separately gated by resolved candidate graph, packaged mod artifact, installed runtime artifact, affected-API reachability, authoritative advisory applicability, and compatible remediation. No gate is cleared by narrowing operating-system scope.

No broad data migration is planned. If remediation changes a persistent schema or configuration contract, the owning phase must add source fingerprinting, version selection, bounded transformation, complete validation, exact recovery copy, failure restoration, forward migration fixtures, rollback procedure, and compatibility documentation. The prior approved JAR and data snapshot remain rollback material until final acceptance. A point of no return is not authorized.

Rollout is sequential through the registered phases and repository branch workflow. Each phase is integrated only after its tests, real behavior evidence, documentation, review, external prerequisites, and required checks pass. Phase 001 may continue unblocked work while `EXT-001` or `EXT-002` is unknown, but it cannot exit or integrate. Rollback for an unmerged phase is removal of that phase's isolated change. Rollback after integration uses the prior signed phase state and validated data recovery material. No plan phase deploys to production or publishes a marketplace artifact.

## 16. Documentation, Operations, and Release Gates

1. Keep `README.md` accurate for users and `DOCUMENTATION.md` accurate for developers and operators.
2. Keep command, permission, and configuration references generated from and reconciled with live contracts. A stale count or route is a failed gate.
3. Update compatibility, security, migration, installation, troubleshooting, performance, test, acceptance, and release documents for every verified behavior or limitation change.
4. Preserve intentionally unavailable control families as explicit unavailable surfaces. Do not advertise them as implemented, enabled, or release complete.
5. Document exact Linux, macOS, and Windows build, check, GameTest, server, enhanced client, fallback client, mixed client, provider, native writer, recovery, dependency, and artifact inspection commands and expected results. Missing evidence is labeled blocked; Windows and the other mandatory targets remain supported.
6. Store raw logs, screenshots, recordings, profiles, corrupted fixtures, and world snapshots in the approved sanitized evidence location outside tracked source unless a small synthetic fixture belongs in tests. Tracked evidence summaries must not contain credentials, personal data, host specific private paths, or unbounded logs.
7. Every phase completion packet names commits, checks, runtime proof, documentation, issues or pull requests, integration state, and evidence invalidation. The protected plan set is not a status diary.
8. Required pull request checks, a private independent-review capability preflight and either a passing supported review or an explicit optional unsupported disposition, merge integration, default or candidate branch verification, and a signed phase tag must pass before the next phase begins, consistent with repository instructions.
9. The release candidate JAR receives SHA-256 and SHA-512 hashes, dependency and provenance inspection, generated reference comparison, secret and content inspection, source commit binding, and proof that it contains no duplicate JNA classes or native runtime. NeoForge-provided runtime artifacts receive separate provenance and advisory closure under `EXT-002`.
10. Release readiness does not authorize publication. Marketplace preview, credentials, publishing, deployment, and production change remain outside this plan and require separate explicit owner authority.
11. Compatibility, installation, security, troubleshooting, acceptance, and release documents must state Linux, macOS, and Windows support consistently and must never convert an `EXT-001` or `EXT-002` blocker into an unsupported-platform claim or suppressed dependency finding.

## 17. Risks and Failure Boundaries

| Risk | Impact | Prevention | Detection | Recovery |
|---|---|---|---|---|
| Audit breadth hides an unowned surface | False confidence and an undiscovered defect | Deterministic Phase 000 inventories from source, runtime, generated references, and persistence paths | Missing owner and drift checks fail on additions, removals, duplicates, and count mismatch | Reopen Phase 000, assign ownership, and invalidate affected downstream evidence |
| Historical evidence is stale or contradictory | Incorrect completion claim | Label authoring evidence `OBSERVED`, freeze a new revision, rerun required proof | Count, hash, version, and generated reference reconciliation | Replace stale claims with new evidence and rerun dependent gates |
| Command matrix becomes impractically broad | Destructive gaps or superficial parser only proof | Generate rows, group shared policy proof only where the invariant is identical, and retain domain specific mutation proof | Coverage report identifies unexecuted sources, targets, mutations, and failure classes | Add fixtures and run missing rows before phase exit |
| Security review misses an indirect authority route | Authorization bypass, data leak, or command execution | Trace aliases, bundles, sudo, profiles, GUI, payloads, reflection, mixins, config, recovery, and lifecycle paths | Adversarial tests, source to sink review, runtime audit correlation, independent review | Repair, add regression proof, and rerun all affected security, command, and integration rows |
| Dependency alert cannot be fixed within platform pins | Applicable critical or high vulnerability remains | Evaluate exact runtime reachability early and test compatible constraints or exclusions | Resolved graph, packaged class inspection, advisory mapping | Block completion and request an owner authorized platform or scope decision, never suppress the alert |
| Platform-owned dependency alert is treated as outside the mod | Applicable risk is hidden by ownership or packaging assumptions | Preserve separate candidate graph, mod artifact, installed runtime, reachability, advisory, and remediation gates under `EXT-002` | Compare authoritative NeoForge provenance, installed runtime artifacts, affected APIs, and advisories on all three operating systems | Reopen `SEFAUD-REQ-003`, restore every separate disposition, and block completion until compatible closure exists |
| macOS or Windows environment is unavailable | Mandatory cross-platform behavior remains unproved | Secure `EXT-001` disposable environments before Phase 001 exit and keep fixtures synthetic | Environment manifest or missing-environment blocker for every required operating system | Continue only unblocked Phase 001 work, keep the phase and endpoint open, and never document the missing target as unsupported |
| Native writer ABI or identity assumptions differ by operating system | Unsafe object substitution, corrupt audit state, or runtime linkage failure | Use provider-specific opened-object identity and exact pinned runtime APIs, with no path-only fallback | Native success and failure fixtures, runtime linkage inspection, object identity before and after flush, and restart on Linux, macOS, and Windows | Fail the operation closed, preserve prior valid state, repair the provider, and rerun all affected platform evidence |
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
9. Every current dependency alert has a candidate-specific disposition. Platform-owned alerts separately pass candidate graph, packaged-mod-artifact, installed-runtime-artifact, affected-API reachability, authoritative advisory applicability, and compatible remediation gates on Linux, macOS, and Windows. No known applicable critical or high exploitable dependency or repository vulnerability remains.
10. No known authorization bypass, sensitive data leak, backdoor like authority path, executable administrator command defect, UI blocking defect, persistence integrity defect, or mandatory backend integration defect remains.
11. Clean checkout checks, unit tests, maintained static and coverage gates, generated reference drift, GameTests, dedicated server, matching enhanced client, command fallback, native audit writing, save, shutdown, and restart pass at the required fidelity on Linux, macOS, and Windows. The complete mixed multiplayer, provider, UI, packet abuse, recovery, performance, JAR, secret, and diff gates also pass at their required fidelity.
12. Documentation, generated references, compatibility claims, security review, test ledger, migration and recovery guidance, and release readiness evidence match the final artifact exactly.
13. No blocked, incomplete, stale, mocked, single-operating-system, or lower-fidelity result is represented as passed. Any unavailable required fixture is reported through `EXT-001` or `EXT-002` without weakening scope, suppressing an alert, or calling Windows or another mandatory operating system unsupported.
14. `FUT-001`, `FUT-002`, and `FUT-003` remain excluded unless explicitly promoted by the owner. All non goals remain intact.
15. Release readiness is recorded, but no public release, publication, deployment, production mutation, or destructive production verification occurs under this plan.
16. Every security-sensitive writer proves that validation and mutation use the same opened object on Linux, macOS, and Windows, rejects unsafe link or reparse state and object substitution, fails closed when identity metadata is unavailable, and preserves prior valid state. JNA and JNA Platform remain compile only, are supplied compatibly by the pinned NeoForge runtime, and are not duplicated in the mod artifact.
17. `EXT-001` and `EXT-002` are satisfied with their complete evidence contracts. Unknown, unavailable, partial, or failed external evidence keeps the plan incomplete under the locked blocker-tolerant endpoint.

Current closure state: NOT COMPLETE — EXTERNALLY BLOCKED until `EXT-001`, Java 21 and NeoForge 21.1.235 runtime evidence on isolated Linux, macOS, and Windows systems for the exact candidate revision and artifact, and `EXT-002`, Authoritative provenance, exact runtime supply, advisory applicability, and compatible closure for NeoForge-owned dependencies, including any JNA and JNA Platform APIs used by the mod, are satisfied.

Completion endpoint:

> At one frozen SEF 2 candidate revision and artifact digest, every mandatory audit and remediation matrix is complete; all confirmed in-scope defects are repaired; highest-fidelity clean-checkout, dedicated-server, matching-client, native-writer, dependency, shutdown, restart, and representative administrator workflow verification passes on Linux, macOS, and Windows; `EXT-001` and `EXT-002` are satisfied; documentation and evidence identify all three operating systems as supported and match the built artifact; the mod embeds no duplicate native runtime; every platform-owned dependency alert passes its separate closure gates; and no known critical or high exploitable vulnerability, authorization bypass, sensitive-data leak, executable administrator-command defect, UI-blocking defect, persistence-integrity defect, or mandatory backend-integration defect remains.

## 19. Goal Creator Handoff

```text
Planning subject: Server Essentials Forge 2 final security, administrator-command, UI, persistence, backend-handling, and integration audit with mandatory remediation closure
Mandatory boundary: SEFAUD-REQ-001 through SEFAUD-REQ-009, including remediation and regression proof for every confirmed in-scope defect.
Optional/future disposition: excluded
Locked owner decisions: DEC-001 through DEC-009. DEC-009 requires Linux, macOS, and Windows support, provider-specific or native opened-object identity proof on each operating system, compile-only JNA only from the pinned NeoForge runtime without an embedded duplicate, and separate platform-owned dependency closure gates that cannot be waived by narrowing operating-system scope.
Active phase: SEFAUD-PHASE-001
Active phase plan: phases/plan-phase-001.md.
Next executable action: P001-TASK-011 must execute every unblocked adversarial row and route the required Linux, macOS, and Windows native-writer and runtime evidence through EXT-001; P001-TASK-012 must close the authoritative platform dependency provenance and advisory gates through EXT-002 before Phase 001 can exit.
Known failing checks: macOS and Windows native-writer runtime evidence is incomplete; authoritative NeoForge-owned dependency provenance and advisory closure is incomplete; conflicting historical test and catalog counts require final reconciliation; multiplayer, current LuckPerms, GUI, InvSee, admission, disguise, reconnect, and recovery rows remain incomplete; maintained formatter, warning, static-analysis, and risk-based coverage gates remain to be closed; repository dependency alerts still require candidate and platform-specific dispositions.
Known external blockers: EXT-001, Java 21 and NeoForge 21.1.235 runtime evidence on isolated Linux, macOS, and Windows systems for the exact candidate revision and artifact, availability unknown. EXT-002, Authoritative provenance, exact runtime supply, advisory applicability, and compatible closure for NeoForge-owned dependencies, including any JNA and JNA Platform APIs used by the mod, availability unknown. Both are mandatory and blocker tolerant; neither weakens the endpoint.
Unavailable feature boundary: the sixteen named Phase 13 families remain explicit unavailable negative contracts and are not implementation scope.
Completion endpoint: At one frozen SEF 2 candidate revision and artifact digest, every mandatory audit and remediation matrix is complete; all confirmed in-scope defects are repaired; highest-fidelity clean-checkout, dedicated-server, matching-client, native-writer, dependency, shutdown, restart, and representative administrator workflow verification passes on Linux, macOS, and Windows; `EXT-001` and `EXT-002` are satisfied; documentation and evidence identify all three operating systems as supported and match the built artifact; the mod embeds no duplicate native runtime; every platform-owned dependency alert passes its separate closure gates; and no known critical or high exploitable vulnerability, authorization bypass, sensitive-data leak, executable administrator-command defect, UI-blocking defect, persistence-integrity defect, or mandatory backend-integration defect remains.
Required evidence gates: complete inventories, Linux, macOS, and Windows environment manifests, provider-specific opened-object identity proof, native runtime loading, authoritative platform dependency provenance, separate advisory closure, no duplicate native runtime, security and dependency closure, administrator command matrix, UI and accessibility matrix, persistence fault and recovery matrix, backend integration matrix, clean-checkout automated and runtime matrix, final artifact inspection, documentation parity, and final cross-matrix audit.
Execution rule: read this master and every registered phase plan through EOF, execute only the current contiguous phase, preserve the immutable saved goal, continue only unblocked Phase 001 work while EXT-001 or EXT-002 is unknown, and never pass or integrate Phase 001 or start a later phase before both external prerequisites and all current phase evidence gates pass.
```
