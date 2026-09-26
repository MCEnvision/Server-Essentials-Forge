# Phase 002 Execution Plan

> **Plan ID:** PLAN-PHASE-002  
> **Phase ID:** SEF-PHASE-002  
> **Owner:** Command platform  
> **Classification:** MANDATORY  
> **Master plan:** [plan.md](../plan.md)  
> **Phase sequence:** 002 of 016

## Purpose and Ownership

This phase delivers the common command policy and native rich-message/action contract required by every retained and later command. It owns the detailed work for canonical requirements SEF-REQ-005 and SEF-REQ-030, while the master remains the sole authority for product scope, phase ordering, interfaces, limits, and acceptance identifiers. It converts every command and result already present when this phase begins; later phases must adopt the same contracts and extend the coverage inventory, and Phase 015 alone closes the full product presentation proof.

## Evidence-Based Entry State

| Evidence class | Area | Finding | Source or command | Freshness condition |
|---|---|---|---|---|
| OBSERVED | Command kernel | Economy costs, GUI descriptors, storage registration, and ordinary command policy are coupled in `KernelServices`; monetary policy must not survive the shared kernel. | SRC-201, `repository-map.md`, reference revision `e160a235b19c992b3a23c3a43754e92ad0147948` | Reference revision or source fingerprint changes. |
| OBSERVED | Removal boundary | Module declarations couple retained features to removed GUI and economy surfaces; a policy implementation cannot restore those dependencies. | SRC-202 and SRC-207, `repository-map.md` | Removal inventory or upstream Phase 001 receipt changes. |
| PROPOSED | Command policy | One catalog resolves ownership, permissions, hierarchy, quotas, cooldowns, warmups, confirmations, identity, audit hooks, conflicts, and bounded aliases or bundles without monetary policy. | FIND-005, SEF-REQ-005, SEF-IF-003 | Master requirement or interface projection changes. |
| OBSERVED | Presentation reference | The supplied screenshots and HuskHomes observations show semantic teal success, red negative actions, gray labels, grouped actions, hover detail, pagination, and run or suggest actions using vanilla components. | SRC-007, SRC-008, SRC-401 through SRC-403, FIND-108 | Screenshot/reference observation or localized design contract changes. |
| PROPOSED | Shared rendering | Forge native and Velocity Adventure adapters consume the same semantic message model; untrusted values are literal and actions are server-issued recipient/session/revision-bound tokens. | SEF-IF-004 and SEF-RISK-013 | Adapter signatures, platform pins, or shared contract digest changes. |
| PROPOSED | Upstream foundation entry gate | Phase 001 must produce native identity, atomic configuration, and default-off diagnostic interfaces before this phase begins; no implementation or runtime receipt exists during this planning pass. | Required future SEF-PHASE-001 entry receipt and SEF-IF-001/SEF-IF-002 | Missing merge/tag, failed entry evidence, or changed producer contract. |

## Scope Boundaries

### Included Scope

- SEF-REQ-005 and SEF-AC-005: one catalog and policy pipeline for all then-existing command dispatch, including source and target authority, feature ownership, conflict reporting, bounded aliases or bundles, quotas, cooldowns, warmups, confirmations, terminal results, console-safe operations, and audit hook boundaries without money.
- SEF-REQ-030 and SEF-AC-030: original localized semantic messages, safe native Forge and Velocity adapter fixtures, literal hostile-value handling, token redemption, destructive confirmation, readable console/accessibility fallback, and an actionable presentation coverage inventory.
- The prior Phase 001 configuration/diagnostic interface is consumed, not replaced. This phase registers the policy and presentation diagnostic categories under SEF-IF-002.

### Explicit Exclusions

- SEF-REQ-007 and SEF-REQ-008 retained gameplay and teleport actions are Phase 003 consumers. This phase ports no future gameplay action beyond converting the then-existing dispatcher/result inventory.
- SEF-REQ-009 through SEF-REQ-018 network routing, authority, Velocity runtime, moderation, visibility, homes, transfer, and pack admission are later contracts. The Velocity adapter fixture is a common semantic contract fixture, not a proxy plugin or network deployment.
- SEF-REQ-036 lifecycle templates are Phase 006. Its future use of `templateId` and `templateGeneration` does not authorize lifecycle dispatch here.
- Economy, custom interfaces, screens, menus, HUDs, Fancy Tags, client protocol, client installation, client rendering, and monetary confirmation/cost logic remain excluded by SEF-REQ-003, SEF-REQ-004, NG-001, and NG-002.
- Phase 015 final all-command inventory, real final artifact screenshot comparison, and plan-wide performance closure are future gates. No task here claims them complete.

## Phase Contract

### SEF-PHASE-002 — Command Policy and Rich Component Actions

**Objective:** Deliver a common policy pipeline and safe, localized semantic presentation system that all then-existing SEF command/result paths use without reintroducing an excluded dependency.  
**Owner:** Command platform  
**Dependencies:** SEF-PHASE-001, SEF-REQ-001, SEF-REQ-003, SEF-REQ-004, SEF-REQ-006, SEF-REQ-019  
**Supporting contract and risk dependencies:** SEF-IF-001, SEF-IF-002  
**Canonical requirements:** SEF-REQ-005, SEF-REQ-030  
**Documentation and release impact:** Update `README.md`, `docs/README.md`, `DOCUMENTATION.md`, `docs/features/commands/`, `docs/features/commands/presentation-coverage.md`, and `docs/troubleshooting/diagnostics.md` only with implemented behavior and artifact-tested support steps. This phase produces no public release.  
**Next transition:** SEF-PHASE-003, port retained gameplay actions.

**Entry criteria**

- SEF-PHASE-001 is merged, its resulting applicable product base is verified and tagged, and its entry receipt proves native identity, configuration swap and diagnostic API availability.
- The action inventory from Phase 000 task 001 identifies every then-existing command/result path and its removal disposition. The implementation fixture records the applicable candidate commit, Java 17, Forge 47.3.12, config generation, and source fingerprints.
- The applicable phase milestone, issues, Project fields, product branch, signing configuration, required checks, and source checkout ownership are reconciled before implementation. No later global phase branch is open or used as a source.

**Implementation scope**

- Implement SEF-REQ-005, SEF-REQ-030 through the work packages and acceptance obligations below.

- P002-TASK-001 implements the canonical catalog and execution policy exposed by SEF-IF-003, including actual terminal outcome handling and console-safe authorization. Trace: SEF-PHASE-002.
- P002-TASK-002 implements the semantic message/action system exposed by SEF-IF-004, both platform adapter fixtures, and recipient-bound redemption. Trace: SEF-PHASE-002.
- P002-TASK-003 converts every then-existing dispatcher/result path and establishes coverage, localization, console, accessibility, diagnostics, and support artifacts. Trace: SEF-PHASE-002.
- P002-TASK-004 proves real Forge command/component behavior and only the named residual laptop rendering/action claim. It does not create a Velocity runtime or claim future consumers complete. Trace: SEF-PHASE-002.

**Execution order**

1. `P002-TASK-001` establishes command catalog ownership and policy before any dispatcher conversion. Trace: SEF-PHASE-002.
2. `P002-TASK-002` establishes semantic rendering and server-issued token/action behavior after policy decisions exist. Trace: SEF-PHASE-002.
3. `P002-TASK-003` converts the frozen then-existing inventory after both common contracts are available and blocks any new registration without coverage. Trace: SEF-PHASE-002.
4. `P002-TASK-004` runs dependency-ordered unit, dispatcher, dedicated-server, adapter, and residual client evidence after the implementation and documentation artifacts exist. Trace: SEF-PHASE-002.

**Required evidence**

- Typed policy tests and real dispatcher fixtures prove one owner, denial, hierarchy, bounded nested aliases, quota/cooldown, warmup cancellation, conflict, console safety, and exactly-once terminal effects.
- Semantic snapshots prove both adapters preserve text, color role, bold, hover, action type, page state, and plain console text. Hostile markup, brackets, command separators, Unicode controls, bidi controls, long values, stale token, stolen token, duplicate redemption, and revoked permission fixtures prove safe refusal.
- A dedicated no-GUI Forge fixture proves then-existing command dispatch uses the policy/result pipeline. A matching laptop fixture supplies the narrow native rich-chat click/hover rendering receipt only after all Trident client gates pass.

**Exit criteria**

- No known mandatory phase-owned defect remains. Every such defect must be fixed and its affected evidence rerun before phase closure.

- SEF-AC-005 and SEF-AC-030 phase-owned fixtures pass, including every then-existing inventory row, and subsequent command registration is mechanically rejected without a presentation coverage row.
- SEF-IF-003 and SEF-IF-004 signatures, errors, ownership, semantic snapshots, Forge adapter fixture, Velocity adapter contract fixture, and `sef action <opaque-token>` fixture match the frozen projections.
- Documentation, diagnostics, sanitized evidence, required checks, private independent review subject to the established review-capability availability rule, merge-commit PR integration, resulting base verification, signed annotated phase tag, and per-host cleanup receipt pass. No known phase-owned defect remains.

## Inputs and Upstream Contracts

| Input or contract | Provider | Required state | Validation | Failure behavior |
|---|---|---|---|---|
| Actor, Session, world identity | SEF-IF-001 from Phase 001 | `Actor` source and permission revision are authoritative; session is nullable only where the interface permits it. | Typed contract fixture and dispatcher source/target cases. | Reject with `DENIED` or `STALE_SESSION`; never infer player identity from display text. |
| Configuration and diagnostics | SEF-IF-002 from Phase 001 | Atomic configuration generation and default-off bounded console controls exist. | Config generation fixture, `sef debug` permission/enable/status/disable tests. | Preserve last valid config and return typed error; do not use unbounded logs. |
| Removal and action inventory | Phase 000 and Phase 001 | Economy and interface dispositions are current and every then-existing command/result is enumerated. | Cross-check catalog, registration scan, config and JAR fixture against inventory. | Stop conversion if an unclassified active entry or excluded dependency is found; route material scope conflict to plan maintenance. |
| Rich-chat observations | SRC-007, SRC-008, SRC-401 through SRC-403 | Visual references define presentation characteristics, not copied code, a client dependency, or product scope. | Semantic snapshots and focused native presentation receipt. | Keep original templates/components; do not import HuskHomes code or restore excluded interfaces. |

## Outputs and Downstream Contracts

| Output or contract | Consumer | Guaranteed state | Compatibility or versioning | Evidence |
|---|---|---|---|---|
| SEF-IF-003 command policy | Phases 003 through 007, 010, 011, 013, and 014 | Every caller receives typed `Result` and current policy outcome; world owner still rechecks before mutation. | Registry/schema version 1. No monetary policy. Additive policy data requires a compatible contract revision. | Policy/dispatcher fixtures and catalog coverage report. |
| SEF-IF-004 presentation/actions | Phases 003 through 007, 010, 011, 013, 014, and 015 | Both adapters render one semantic message; actions are literal-safe, recipient/session/revision-bound, expiring, atomic, and reauthorized. | Registry/schema version 1. Future commands add inventory rows and preserve the signature. | Component snapshots, token security fixtures, Forge fixture, Velocity adapter contract fixture. |
| Presentation coverage gate | Every future command registration | Command plus success, denial, validation, empty, partial, unavailable, stale, and confirmation results are registered before activation. | Inventory is versioned with candidate/config generation and source command ID. | Registration guard and coverage report. |
| Operator documentation and support procedure | Operators and Phase 016 documentation closure | Console-safe policy/action/diagnostic procedure, accessibility fallback, redaction and cleanup behavior are discoverable. | Documentation describes only merged behavior; wiki follows merge. | Documentation-link and runbook rehearsal evidence. |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
|---|---|---|---|---|---|---|
| P002-TASK-001 | SEF-REQ-005, SEF-AC-005 | Implement the canonical command catalog and `authorize` then `execute` pipeline. Assign one feature owner per command ID; resolve `Actor`, optional `Session`, current configuration generation, source/target permissions and hierarchy before executing. Enforce bounded alias/bundle expansion and recursion, quota/cooldown/warmup/confirmation states, conflict diagnosis, audit hook boundaries and actual terminal results. Remove or refuse any monetary policy path. Console-safe actions accept `CONSOLE` without fabricating a player. | SEF-IF-001, SEF-IF-002, Phase 000 action inventory, Phase 001 removals/config/data receipts, SRC-201 and SRC-202. | SEF-IF-003 producer, catalog schema and validation, policy reasons/errors, dispatcher adapter boundary, catalog coverage input. | Planned common command kernel; existing Forge dispatcher and command registration adapters identified by the action inventory; SEF-IF-003. | Unit/property fixtures prove one owner, unknown/conflicting owner refusal, hierarchy denial, missing/changed target, nested alias and bundle depth/expansion limits, quota/cooldown, warmup move/damage/disconnect cancellation, console safety, permission revision change between authorize/apply, and exactly-once terminal effect. |
| P002-TASK-002 | SEF-REQ-030, SEF-AC-030 | Implement original localized semantic `Message`, action groups, page state, literal values and component conversion. Build Forge native and Velocity Adventure adapter contract fixtures. Issue only opaque 128-bit `ActionBinding` tokens through `sef action <opaque-token>`; bind recipient, session, operation, immutable target/revision, optional snapshot/confirmation digest and idempotency key. Require fresh policy authorization and separate confirmation for delete, purge, apply, and give. | P002-TASK-001, SEF-IF-002, SRC-007, SRC-008, SRC-401 through SRC-403, DEC-014. | SEF-IF-004 producer, ephemeral token registry, adapter fixture set, localization/templates, plain console/accessibility rendering and action result mapping. | Planned common presentation and action-token services; Forge native component adapter; Velocity Adventure adapter fixture; `sef action` entry point; SEF-IF-004. | Semantic snapshots prove equivalent roles/colors/text/bold/hover/pages/actions for both adapters. Security fixtures inject markup, brackets, separators, Unicode/bidi controls, long literals and malformed token bytes; steal, replay, expired, stale-session, stale-revision, revoked-rights, repeated-click, restart/reload invalidation, and confirmation cases must reject or be idempotent without a new mutation. |
| P002-TASK-003 | SEF-REQ-005, SEF-REQ-030, SEF-AC-005, SEF-AC-030 | Convert every then-existing command/result inventory row to the policy and presentation contracts. Register success, denial, validation, empty, partial, unavailable, stale and confirmation outcomes before activation. Add console/ASCII equivalents, keyboard-enterable command alternatives, localization keys, bounded page and hover behavior, diagnostic categories, and support documentation. Establish a registration guard that refuses an uncovered new command. | P002-TASK-001, P002-TASK-002, frozen then-existing action inventory, SEF-IF-002. | `docs/features/commands/presentation-coverage.md`, command/result coverage fixture, registration guard, support and documentation updates, diagnostic category mappings. | Existing command dispatchers and result producers found by Phase 000 inventory; `docs/features/commands/`; `docs/troubleshooting/diagnostics.md`; SEF-IF-002. | Inventory-to-registration scan has no then-existing uncovered row. Console fixture proves labels/values and glyph words remain readable. Locale fallback, missing template, empty page, partial result, unavailable service, and rejected page cursor show safe localized feedback without raw markup or privileged hover data. |
| P002-TASK-004 | SEF-REQ-005, SEF-REQ-030, SEF-AC-005, SEF-AC-030 | Execute the phase evidence ladder, including task-graph inspection, unit/property tests, real Forge dispatcher fixture, dedicated server smoke, adapter contract fixture, and the minimal laptop visual/click evidence. Bind every result to the candidate hash, Phase 001 contract generation, Forge/platform versions and coverage inventory revision. | P002-TASK-001 through P002-TASK-003, EXT-001, runtime-host policy, diagnostic runbook. | Sanitized phase evidence packet, fixture reports, named unverified-client gate if needed, cleanup receipts and integration-ready PR evidence. | Actual Forge command dispatcher and native component delivery path; Velocity adapter contract fixture only; node-1 dedicated server; verified laptop only for native component render/click receipt. | Inspect Gradle task graph before every command. Headless fixture uses the real dispatcher and permission/action path with bounded waits. The laptop receipt is accepted only after exact artifact/profile, discrete renderer, private endpoint and per-application mute evidence; otherwise stop its owned client and leave only that residual gate open. |

For each task, failures return typed result codes and safe localized messages. P002-TASK-001 and P002-TASK-002 are ordered producers. P002-TASK-003 may convert independent inventory rows in parallel only after their common contracts are stable. P002-TASK-004 runs after conversion and never substitutes a direct helper call, mock action, server log, or console bypass for the policy/action being measured.

## Architecture and Implementation Boundaries

The planned common command kernel is Java 17 pure contract logic and has no Forge, Minecraft, Velocity, GUI, economy, world, or live-player dependency. It accepts the frozen `CommandContext`, performs policy work from bounded immutable inputs, returns the frozen typed `Result`, and leaves all world mutation to a platform owner that rechecks authority at application time. The catalog stores canonical command identity, feature owner, source/target policy, alias/bundle bounds, cooldown/warmup/confirmation policy, and terminal result classification. A command cannot be registered by two owners, delegate an unbounded alias/bundle chain, or return dispatch success as if the action succeeded. Auditing receives a typed outcome hook, but this phase neither implements the mandatory later audit journal nor assumes it is complete.

The shared presentation model carries localized message key, locale, semantic severity, typed literal value map, groups, optional pagination and optional future template metadata. It never accepts raw MiniMessage, legacy codes, arbitrary click commands, external placeholder evaluation, raw user components, or client-side translation as authority. Semantic roles preserve the master palette: teal `#00fb9a` success/primary action, gray labels, red `#ff3300` and soft red `#ff7e5e` errors, gold `#ffc43b` location/toggle, violet `#c160ff` edits, and blue `#3b76ff` server/world metadata. Bracketed actions include both glyph and words, for example `[✓ Accept]` and `[✕ Decline]`, plus ASCII/plain alternatives. Values are literal children, control and bidi override characters are removed or visibly escaped according to the shared sanitizer, and truncation is disclosed rather than silently changing identity.

The only generated executable route is `sef action <opaque-token>`. A token is 128-bit random opaque data held only in an ephemeral server registry, limited to 64 live entries per recipient and expiring after 60 seconds. The registry is invalidated on restart, reload, session replacement, target revision invalidation, terminal result, and expiry; no token is persisted, restored, replayed, or treated as authority after those boundaries. Redemption atomically validates recipient UUID, session, operation, target revision, snapshot/confirmation binding, current policy, and idempotency key. Operation idempotency remains owned by the action executor or, where required, the later durable operation producer, rather than by token persistence; an uncertain action is never replayed from a token. It returns the specified `ACTION_EXPIRED`, `WRONG_RECIPIENT`, `STALE_SESSION`, `STALE_REVISION`, `DENIED`, `ALREADY_APPLIED`, or `CONFIRMATION_REQUIRED` result rather than looking up a mutable display name. Suggestions may prefill editable safe commands, but still traverse normal policy and execution. Hover/action content obeys the same visibility and sensitive-data policy as the resulting operation.

No persistent player/gameplay migration or durable UI-token storage belongs to this phase. Configuration generation comes from SEF-IF-002; invalid configuration swap preserves the active generation. Concurrency uses owner-executor serialization around registry mutation/redemption and immutable snapshots for rendering. Render and diagnostic work must not block server, proxy, render, or tick threads. The master diagnostic budget remains: default off, 60 seconds default and 300 seconds maximum capture, 200 events per second, 10,000 events and 8 MiB per capture, 1,024 queued diagnostic events, and two captures per process. These are acceptance budgets, not measurements or permission to sample mandatory audit history.

## Noncanonical Interface Projection

The following machine-readable projection is derived evidence copied from the frozen phase input. It does not create another canonical contract.

```json
{
  "phaseId": "SEF-PHASE-002",
  "interfaces": [
    {
      "id": "SEF-IF-001",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Identity and world",
        "records": {
          "Actor": {
            "uuid": "UUID?",
            "source": "PLAYER|CONSOLE|SYSTEM|MACHINE|FAKE_PLAYER",
            "originId": "string",
            "permissionRevision": "u64"
          },
          "Session": {
            "playerId": "UUID",
            "proxyBoot": "UUID?",
            "connectionEpoch": "u64",
            "backendId": "string",
            "backendBoot": "UUID"
          },
          "WorldRef": {
            "backendId": "string",
            "worldGeneration": "UUID",
            "dimension": "resource_location",
            "registryDigest": "sha256"
          },
          "Location": {
            "world": "WorldRef",
            "x": "finite f64",
            "y": "finite f64",
            "z": "finite f64",
            "yaw": "finite f32",
            "pitch": "finite f32"
          }
        },
        "errors": [
          "WORLD_REPLACED",
          "DIMENSION_MISSING",
          "REGISTRY_MISMATCH",
          "STALE_SESSION"
        ],
        "ownership": "Backend creates durable world generation; authenticated proxy creates network connection epochs; display names confer no authority."
      },
      "acceptance_ids": [
        "SEF-AC-006",
        "SEF-AC-019"
      ]
    },
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
    }
  ]
}
```

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
|---|---|---|---|---|
| A command ID has zero or multiple owners, or points at an excluded economy/interface path. | Catalog validation returns `CONFLICTING_OWNER` or removal scan reports an excluded dependency. | Refuse registration before dispatcher exposure; do not select an arbitrary owner or reactivate excluded services. | Correct catalog/removal mapping, regenerate coverage, and rerun affected dispatch fixtures. | P002-TASK-001 catalog fixture and P002-TASK-003 inventory scan. |
| Source/target hierarchy or permission revision changes between decision and world application. | `command.decision` correlates current `permissionRevision` and target revision with `HIERARCHY_DENIED`, `DENIED`, or `STALE_REVISION`. | Produce no mutation and return safe localized result. The world owner rechecks immediately before mutation. | Reissue a fresh action only after current authorization succeeds. | P002-TASK-001 real dispatcher fixture with revocation race and no-mutation oracle. |
| Nested alias/bundle expands recursively, exceeds bounds, or loops. | Catalog expander records bounded depth/count and a deterministic reason. | Reject before executing any child action; do not partially execute, consume a quota, or recurse indefinitely. | Correct the catalog entry; preserve the caller state. | P002-TASK-001 property fixture with boundary, one-over-limit, and cycle inputs. |
| Warmup is cancelled by movement, damage, disconnect, target removal, reload, or stale session. | Real dispatcher and diagnostic event show `CANCELLED` with operation/session IDs. | Stop exactly once, consume no terminal effect, and issue a current readable result if a recipient remains valid. | Clear timer/reservation; a new request begins with new context. | P002-TASK-001 bounded dedicated-server fixture through normal policy/action path. |
| Untrusted literal contains markup, formatting codes, brackets, URL-like text, a command separator, Unicode controls, or bidi override. | Semantic snapshot and source-unit sanitizer fixture compare literal tree and visible escaped/elided result. | Render literal text only; create no click/hover/action child from input and do not reinterpret it after localization. | Retain sanitized diagnostic reason without raw sensitive input. | SEF-RISK-013 fixture in P002-TASK-002. |
| Token is stolen, expired, replayed, repeated, target-stale, session-stale, permission-revoked, restart/reload-invalidated, or confirmation-free. | `presentation.action` diagnostic records recipient/session/target revision, operation ID, and explicit rejection reason. | Reject with the frozen error; no old token gains authority after restart/reload; repeated successful redemption is idempotent or `ALREADY_APPLIED`; destructive operation returns `CONFIRMATION_REQUIRED`. | Expire/remove the ephemeral binding, preserve the executor or later durable producer's operation state without replaying uncertainty, and issue a fresh current action only after reauthorization. | P002-TASK-002 token fixture proves restart/reload rejects old tokens and P002-TASK-004 exercises the real dispatcher action path. |
| Config reload changes localized templates or a renderer adapter is unavailable. | Config generation check and adapter fixture report `INVALID_CONFIG`, `STALE_REVISION`, or `OUTPUT_UNAVAILABLE`. | Preserve prior valid generation; use bounded plain console fallback only when authorized and never send an unbound action. | Correct configuration/adapter, atomically swap, and rerun snapshots. | P002-TASK-002/P002-TASK-003 reload and fallback fixtures. |
| Component rendering is server-correct but client rendering/click behavior is unverified. | Dedicated server evidence proves semantic delivery only; laptop gate lacks renderer/window/stream evidence. | Keep only the residual client gate open. Do not use server logs, a mock adapter, software rendering, or node-1 as a substitute. | Stop the owned client, clean its resources, and retry on the verified laptop when prerequisites return. | P002-TASK-004 client receipt procedure. |

## Diagnostics and Debugging

**Requirement IDs:** SEF-REQ-005, SEF-REQ-019, SEF-REQ-030
**Task IDs:** P002-TASK-001, P002-TASK-002, P002-TASK-003, P002-TASK-004
**Controls:** Use the inherited default-off console-safe `sef debug on command <operation-id> 60`, `sef debug on presentation <recipient-uuid> 60`, `sef debug status`, and `sef debug off <capture-id>` controls under `sef.debug.manage`; console omits `/`, target absence returns `TARGET_ABSENT`, and disable is idempotent.
**Signals:** Emit SEF-IF-002 records for `command.decision`, `command.terminal`, `presentation.render`, and `presentation.action` with capture/correlation IDs, side, boot/sequence, desired and actual typed state, reason, units, config generation, and candidate digest.
**Collection procedure:** Follow the numbered local procedure below to register exact owned resources, enable one bounded category, reproduce the real dispatcher/action path, inspect correlated JSONL, disable, redact, retain minimal evidence, and verify cleanup.
**Headless verification:** On node-1 only after task-graph inspection proves no client/renderer/display path, run real Forge dispatcher fixtures and the no-GUI dedicated server with console policy/action commands and bounded waits; these prove policy and server component construction, not visual receipt.
**Client verification:** Only P002-TASK-004 needs a verified laptop receipt for native Forge rich-text hover/click and readable visual hierarchy; the Velocity adapter remains a source-unit contract fixture until its Phase 004 runtime exists.
**Client audio isolation:** Leave audio settings unchanged. Every required client uses the master Section 14 Trident instance lifecycle on the verified laptop, with explicit owned instance, runtime, window/PID, discrete renderer and joined-world evidence. No Trident command or client runs on node-1. Do not create mute watchers or require a playback stream or mute proof. Server-only checks need no client. Verify exact owned process exit and instance cleanup after the final consumer.
**Budgets and privacy:** Diagnostics are off by default, retain the inherited 60 second default and 300 second maximum duration, 200 events per second, 10,000 events, 8 MiB, 1,024 queued events and two captures per process; truncate/redact literal values, credentials, private addresses, private text and hidden identities, report drops, and never confuse this bounded capture with unsampled future audit history.
**Regression and support:** Test enable/status/disable, console and permission denial, absent/removed targets, timeout, reload/reset, disabled and bounded enabled overhead, output limit, redaction, and unchanged command effect; update and rehearse `docs/troubleshooting/diagnostics.md` with the sanitized support packet and command presentation coverage path.

| Signal | Source and unit | Expected observation |
|---|---|---|
| `command.decision`, `desired.commandId:string`, `actual.allowed:bool`, `permissionRevision:u64`, `reason:enum` | Owner dispatcher, one record per policy decision | Exactly one catalog owner; denial, hierarchy, quota, cooldown, conflict, cancellation, or stale state occurs before any effect. |
| `command.terminal`, `operationId:UUID`, `actual.result:enum`, `effectCount:u32`, `durationMs:f64` | World-owner application boundary, one terminal record | A claimed success corresponds to exactly one observed terminal effect; cancellation/denial has zero effects. |
| `presentation.render`, `messageKey:string`, `locale:string`, `groups:u16`, `pageNumber:u32?`, `serializedBytes:u64` | Forge adapter or Velocity adapter fixture | Semantic roles map identically and bounds/fallback are explicit without exposing literal sensitive values. |
| `presentation.action`, `recipient:UUID`, `targetRevision:u64`, `expires:Instant`, `actual.state:enum`, `reason:enum` | Token registry/redeem handler | Wrong recipient, stale session/revision, expiry, revocation, replay, and missing confirmation cause no new action. |
| `capture.status`, `events:u32`, `bytes:u64`, `dropped:u64`, `remainingSeconds:u16`, `output:string` | Inherited diagnostic worker | Enable/status/disable and limits are visible; disabled or stopped capture produces no further matching record. |

1. Register the candidate commit/JAR hash, Forge 47.3.12 and Java 17 versions, Phase 001 config generation, fixture seed, node-1 project anchor, verified cleanup paths, and expected `docs/verification/phase-002/` evidence destination. Inspect the exact Gradle task graph before launch. Create only ignored nested disposable server/test paths after confirming parent exclusions, and record every owned process, log, watcher, route, and fixture.
2. For a named operation, use the owned server console to enable `sef debug on command <operation-id> 60` or `sef debug on presentation <recipient-uuid> 60`, then run `sef debug status`. It must report scope, target, remaining duration, limits, and the discovered JSONL output location. An unavailable target/output or insufficient permission returns its typed error and the fixture stops rather than widening capture.
3. Exercise the real dispatcher entry point with a policy denial and an allowed test-only fixture action, then a recipient-bound `sef action <opaque-token>` redemption. For the negative case inject a literal hostile value and redeem as a different or revoked actor. Wait only the fixture's declared bounded tick/time window; inspect independent effect counters rather than the policy counter alone.
4. Filter the discovered JSONL by operation or correlation ID. Assert decision before terminal result, current config/permission/target revisions, literal-safe rendering, effect count, and explicit refusal reason. Retain a minimal sanitized excerpt plus candidate/fixture metadata; do not retain raw private text, addresses, tokens, or unrelated identities.
5. Run `sef debug off <capture-id>`, query status, trigger one harmless matching event, and assert no additional capture record. On all exit paths stop only owned processes/watchers, confirm they exited, preserve required sanitized evidence, remove exact disposable runtime/log/report and owned instance resources after final use without symlink traversal, and verify absence on each host. Cleanup failure remains an open phase gate.

## Verification Matrix

| Requirement or task | Static or unit | Integration | Real workflow or runtime | Negative and recovery | Execution host and prerequisites | Evidence artifact |
|---|---|---|---|---|---|---|
| P002-TASK-001, SEF-AC-005 | Catalog, policy, alias/bundle property, hierarchy, quota/cooldown/warmup, stale revision and exactly-once tests. | Real dispatcher wired to test-only effect oracle. | No-GUI dedicated Forge server exercises console-safe and player-context normal dispatch. | Unknown/multiple owner, removed dependency, target absent, changed permission/session, recursive expansion, cancellation and terminal failure preserve zero/one effect invariant. | node-1 only after task graph proves headless; exact candidate/runtime under discovered project anchor; `eula=true` read back before any server launch. | Sanitized unit report, dispatcher report, readiness/console log excerpt, cleanup receipt. |
| P002-TASK-002, SEF-AC-030 | Shared semantic tree and both adapter snapshot fixtures; literal sanitizer and token state-machine tests. | Forge adapter plus Velocity Adventure adapter contract fixture compare roles/text/actions without a proxy runtime. | Forge server emits native component; actual click handling is reserved for P002-TASK-004. | Malformed bytes, markup/bidi/separator literals, wrong recipient, expiry, stale session/revision, revoked authority, replay, duplicate and confirmation require safe frozen errors. | node-1 for non-rendering fixtures after graph inspection. No Minecraft client is launched for adapter unit fixtures. | Snapshot bundle, token decision report, redacted diagnostic excerpt, cleanup receipt. |
| P002-TASK-003, SEF-AC-005, SEF-AC-030 | Inventory-to-registration coverage scan, locale/console/ASCII/page/hover limit tests and documentation link check. | Convert each then-existing dispatcher/result row and verify guard rejects an uncovered new registration. | Dedicated server prints console-safe result and native system-message semantic payload. | Missing locale/template, empty/partial/unavailable/stale result, oversized page/hover and unavailable output retain readable safe fallback. | node-1 no-GUI only after task graph inspection; same candidate/config generation. | Coverage inventory, scan report, console output evidence, docs check, cleanup receipt. |
| P002-TASK-004, SEF-AC-030 | Reuse prior focused tests; inspect no accidental client/economy/interface payload in candidate JAR. | Correlate server action result with a direct client action receipt. | Minimal laptop client joins a ready private node-1 dedicated server and demonstrates native rich component hierarchy, hover and action click. | Missing desktop, discrete renderer, exact window/PID, Trident instance binding, server readiness, private join, or joined-world proof stops the owned client and leaves only this client gate open. | node-1 dedicated server plus verified laptop, matching candidate/platform/profile, supported automatic join/authorized controls, exact Trident instance lifecycle and both-side join proof. | Renderer/window/PID/Trident instance proof, targeted screenshot only when visual evidence is necessary, correlated server/client logs, and dual-host cleanup receipts. |

## Documentation, Operations, and Release

This phase documents the implemented shared `/sef` policy/action syntax, `sef action <opaque-token>` behavior, confirmation and expiry, permissions, console-safe behavior, aliases/bundles, result taxonomy, localization/accessibility fallback, and the presentation-coverage requirement. It documents no unimplemented network or lifecycle command. `docs/features/commands/presentation-coverage.md` lists the then-existing command ID and result states with source owner, localization key, semantic snapshot, console form, action/confirmation state, and verifier. `docs/troubleshooting/diagnostics.md` records the scoped console capture procedure, redaction, expected signal fields, support packet, and cleanup. README and docs index link only to merged artifacts; wiki updates occur only after the approved merge.

The phase uses the applicable `envy/phase-002-forge` branch from the latest approved `forge-1.20.1` product base after its milestone is reconciled. Commits/tags are SSH-signed by EnVy with the configured identity. A checked PR uses a merge commit into the applicable product base, required checks/resolved conversations and a private independent review subject to the established review-capability availability rule. After GitHub reports merge, verify the resulting base commit, run post-merge required checks, create/push the signed annotated phase tag, then and only then allow the contiguous Phase 003 entry action. No direct product-base push, proxy branch work, public release, deployment, or production message is authorized.

## Risks and Evidence Invalidation

| Risk ID and owner task | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
|---|---|---|---|---|---|
| SEF-RISK-013, P002-TASK-002 and P002-TASK-004, rich click acts on a new target or injected command | Literal components; opaque recipient/session/revision/snapshot token; 60-second expiry; atomic idempotency; fresh authority check; explicit destructive confirmation. | `presentation.action` rejection reason, independent no-mutation oracle, and token fixture. | Expire binding, reauthorize/reissue safe action, correct renderer/token implementation, and preserve no ambiguous effect. | Token tests, adapter snapshots, real action receipt, and coverage rows. | Rerun P002-TASK-002 plus P002-TASK-004 when token, adapter, session, policy, or target schema changes. |
| SEF-RISK-002 dependency residue, P002-TASK-001 and P002-TASK-003, command kernel restores excluded policy | Catalog rejects monetary/interface owner/config/dependency paths; inventory guard ties registration to removal disposition. | JAR/config/registration scan and `CONFLICTING_OWNER`/excluded-path fixture. | Remove exact residual mapping and rerun affected retained dispatcher results. | Removal scan, catalog report, and affected command coverage. | Rerun P002-TASK-001/P002-TASK-003 after any kernel, module registry, config, or inventory change. |
| SEF-RISK-020 evidence drift, P002-TASK-004, candidate/host/profile mismatch proves the wrong behavior | Bind every report to commit, JAR hash, source fingerprints, config generation, platform, adapter snapshot and host/runtime identity. | Receipt manifest mismatch, missing Trident instance/renderer proof, or changed upstream contract. | Mark only affected claim unverified, clean exact resources, rebuild/retest matching candidate. | Matching fixture, screenshot, server/client correlation, and cleanup receipt. | Narrow rerun for changed binding, then dependent integration checks after merge. |
| P002-RISK-001, P002-TASK-001, dispatch reports success before terminal state | Separate policy decision from terminal operation result and use independent effect oracle. | `command.terminal` lacks effect or duplicate effect count. | Return failed/unknown terminal state, repair owner boundary, and rerun real dispatcher fixture. | Policy terminal assertions and any converted result row. | Rerun P002-TASK-001 and impacted P002-TASK-003 rows. |

## Phase Completion Packet

The phase cannot close without the following external evidence packet.

- Applicable phase branch commit, exact candidate/JAR hash, source revision, Phase 001 contract/config generation, action inventory revision, SEF-IF-003/SEF-IF-004 projection check, and documentation revision.
- P002-TASK-001 through P002-TASK-004 reports with test names/counts, failed-case disposition, terminal-effect oracle, coverage inventory, adapter snapshots, token security fixture, and sanitized diagnostics.
- Dedicated server evidence with task-graph proof, `eula=true` readback, readiness, real dispatcher outcomes, exact runtime path, process exit, and cleanup receipt. No client, renderer, Xvfb, VNC, forwarding, offscreen path, software renderer, or integrated GPU is used on node-1.
- If the residual client gate is attempted, record the verified laptop Trident executable/home, owned instance key/runtime, matching candidate/profile, discrete renderer, exact Hyprland address/class/title/PID, private endpoint/readiness, both-side joined-world proof and targeted visual evidence. Follow master Section 14 for process exit, instance retirement and verified filesystem cleanup; leave audio unchanged. If any required evidence is missing, record the open gate rather than substituting server evidence.
- Updated implementation documentation, diagnostic support procedure, coverage inventory, README/docs-index links, and post-merge wiki update only after approved integration.
- Required checks, private independent review availability/result, phase PR/merge-commit state, resulting product base verification, signed annotated tag, milestone/issue/Project synchronization, and no known mandatory phase-owned defect.
- Before each test/build/check/audit, an owned-resource register. After final consumption, exact confirmed shutdown and removal of test-created runtimes, worlds, logs, crash reports, screenshots, traces, coverage, scratch reports, databases, fixture downloads, incidental bytecode and test-only output. Preserve source, tracked fixtures, personal instances/saves, shared caches, required sanitized evidence, historical branches/tags and unrelated resources. Verify absence on every used host and report exact leftover path/recovery if cleanup fails.

## Next Transition

After the complete Phase 002 packet is accepted, integrated through the required merge commit, verified on the resulting `forge-1.20.1` base, and tagged, advance only to SEF-PHASE-003. Its entry action is to read this shared policy/presentation coverage contract and begin `Phase 003 task 001` against the latest approved base, porting retained gameplay actions through SEF-IF-003 and SEF-IF-004. No later phase begins before these sequential gates pass.
