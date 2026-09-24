# Remaining audit research
Observed 2026-09-24T20:06:46.273Z. Candidate branch `envy/phase-003-commands`, revision `e160a235b19c992b3a23c3a43754e92ad0147948`. This is an existing product audit and remediation program. The requested result is a complete execution plan for remaining checks, preserving integrated history and the saved goal.

## History and current evidence
Phase 000 PR10, Phase 001 PR8 and Phase 002 PR11 are merged into `master`. Signed annotated phase tags resolve to their merge commits and their signatures verified during this read-only review. Do not restart these phases. Preserve their evidence and reopen only changed, newly discovered or invalidated rows. GitHub's default `forge-1.20.1` is legacy; current audit integration uses `master`. No Phase 003 PR or branch workflow run was returned.

The latest broad retained Phase 003 runtime packet is tasks310 through318 for `1b2e61947cc733574bee377a8404f59f7b129d62`, artifact SHA-256 `8a94802dfcac9902300a5e8e6ab9e9d3d45b7280436602fed2e99f7484dd2bc3`. The main product source and build inputs have not changed from that revision to current HEAD. The matrix generator and its tests changed, alongside planning documents. Reuse product evidence only after checking its exact fixture, configuration, artifact and dependency inputs; regenerate the matrix with all compatible runtime joins. Final clean checkout remains mandatory.

Task310 reports 729 rows, 713 executable commands, 16 unavailable families, 324 partial and 389 open command rows, with 75 required GameTests passed. Task317 JSON instead contains 713 open, 16 partial and zero passed rows. This is a real evidence disagreement, not proof of product regression. Reconcile missing inputs and generator semantics before selecting action work. Older tracked claims of 694 or 708 actions, 724 rows, and 41 to 50 GameTests are superseded inventory snapshots.

## Concrete failures and work remaining
1. The laptop fixture rendered and joined the matching dedicated server. Its protected `/sef doctor` attempt failed in the client command tree before transmission. Operator status alone did not grant the default-denied node. Treat this as incomplete authorization/projection evidence; do not grant all operators or weaken the permission contract.
2. LuckPerms NeoForge 5.4.140 initialized on exact NeoForge 21.1.235 but player login failed with `Capability has not been initialised`, followed by `Invalid player data`. Forge-only 5.4.102 was rejected by the loader. Diagnose the exact current compatible provider lifecycle and fixture; test authentic grants, denies, revocation, refresh, reconnect, outage and fallback. This supersedes the older 21.1.233-only failure statement.
3. Shared command policy/audit tests and hundreds of domain effect observations exist. They do not close every action's mutation, domain failure cut point, persistence/restart, equivalent route, actor/source, feedback and sink join. Compare each action's before/after state, exact result and one correlated terminal audit event; denied paths must leave protected state unchanged.
4. Full screen/HUD/feedback polish, keyboard focus, narration, resize, GUI scales, hidden data, stale selections, revocation, InvSee, disguise animation, admission capacity/FIFO, mixed/fallback clients and reconnect remain separate real client gates.
5. Preserve Phase 002 durable-owner and corruption/interruption/migration proof. Reopen changed domain writers and command-to-store joins, including cross-store partial success, receipt/idempotency and recovery. Do not redo every unchanged primitive because the branch advanced.
6. Backend lifecycle and optional adapter outage, classloading, thread ownership, reload, logout, dimension change, queued work, shutdown and recovery need complete current sequence coverage.
7. Live GitHub has 28 open dependency alerts, including new critical #28 and medium #27 for Netty SNI handling. The old 26-alert external-blocker narrative is stale. Close each advisory through version, ownership, artifact presence, installed runtime, affected API/configuration reachability, provenance and compatible remedy. No blanket platform upgrade or zero-alert promise.
8. Final maintained formatting, warning, static analysis and risk-based coverage gates are absent from the inspected Gradle build. Deliver appropriate checks, then clean checkout build, fallback compilation, generated drift, GameTests, runtime, UI, provider, recovery, performance, artifact and documentation verification after the last change.
9. Existing `/sef doctor`, subsystem doctors and repository health are delivered. New `/sef debug on|status|off` capture controls described by the stale rebuild are absent. Deliver bounded, authorized, default-off capture with explicit enable/status/disable, expiry, counters, correlation, redaction and negligible disabled overhead as early P003-TASK-010 subwork before its dependent evidence. Preserve the stable entry task. Phase 003 produces the shared diagnostic capability for later consumers; Phase 006 reruns earlier security and storage checks with it. Earlier merged phases retain their original controls and proof and are not retroactively failed by this new capability.

## Question and decision map
| Question | Decision and evidence | Resolution |
| --- | --- | --- |
| Restart completed phases? | DEC-006, FIND-002 | No. Preserve merges/tags and validate evidence dependencies. |
| One or three clients? | DEC-009, FIND-001 | One portable Minecraft Java client contract; Linux laptop for graphics. |
| Need unavailable foreign native hosts? | DEC-009, FIND-006 | No. Native source/artifact scrutiny and available runtime tests, explicitly limited claims. |
| Must all hosted alerts disappear? | DEC-003/004/009, FIND-007 | No. Per-advisory defensible closure; repair applicable mod-owned exposure. |
| Is the command matrix complete? | FIND-003/004 | No. Reconcile candidate-bound inputs, then close remaining action dimensions. |
| Are diagnostics already implemented? | FIND-009 | No new capture system observed; reuse existing controls and implement needed missing signals first. |
| Optional feature expansion? | DEC-002/007, FUT-001..003 | Excluded. Keep all sixteen unavailable families negatively tested. |

No new material owner question is needed. EXT-001 and EXT-002 are available mandatory work, not outside-plan prerequisites. The absent active cursor is a legacy execution-state issue; this planning pass neither invents a cursor nor replaces the immutable saved goal.

## Risks and evidence limits
A broad shared callback test can hide untested domain mutations. A regenerated matrix can discard valid evidence when candidate inputs are missing. Provider login failure can be mistaken for SEF authorization failure. Native pointer/ABI branches need explicit source and packaged API review even when only Linux executes. Advisory ownership alone does not establish inapplicability. Old rendering proof lacks the new exact audio/window-stream verification, so future laptop sessions must enforce it from before startup.

This bounded review launched no product, build, server or client. Retained reports are observed historical evidence, not newly executed proof. It checked graph freshness, Git history/signatures, source relationships, matrix JSON, fingerprints and live alert metadata. Raw old runtimes and logs were not copied. See [repository map](repository-map.md), [source observations](sources/current-evidence.md), and [machine index](evidence.json).
