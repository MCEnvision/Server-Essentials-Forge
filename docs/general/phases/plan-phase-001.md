# Phase 001 Execution Plan

> **Plan ID:** PLAN-PHASE-001  
> **Phase ID:** SEF-PHASE-001  
> **Owner:** Forge backend  
> **Classification:** MANDATORY  
> **Master plan:** [plan.md](../plan.md)  
> **Phase sequence:** 001 of 016

## Purpose and Ownership

This phase establishes the server-only Forge foundation on which all retained adapters will be built. It removes owner-excluded economy and custom-interface surfaces at their construction, registration, configuration, resource, dependency, and test boundaries; creates the versioned backend data and world-identity foundation; and delivers the common configuration and diagnostics contract before any dependent feature verification.

The master plan owns the product scope, the full retained-action inventory, the global topology, and all requirements. This blueprint owns only the dependency-ordered execution and proof for SEF-PHASE-001. It does not claim the final retained-inventory result required by SEF-AC-001, which remains Phase 015 work after the retained adapters exist.

## Evidence-Based Entry State

| Evidence class | Area | Finding | Source or command | Freshness condition |
|---|---|---|---|---|
| OBSERVED | Forge target | The target baseline is Forge 47.3.12, Java 17, commit `1e8bab26d9d6ff6b1bf1d5ef41eb8d6c1a51ad98`. | `gradle.properties`, `build.gradle`, and SRC-002 fingerprints | Any source commit, loader, Java, Gradle, or dependency revision change invalidates build and runtime evidence. |
| OBSERVED | Removal closure | `KernelServices` constructs economy, cost, storage, schedule, and GUI dependencies. `ModuleConfigRegistry` and `ModuleConfigService` expose economy, GUI, HUD, and Fancy Tags declarations. | SRC-201, SRC-202, FIND-103 | Any inventory correction, configuration registry change, or new transitive reference reruns the removal graph and JAR scan. |
| OBSERVED | Client boundary | The reference uses NeoForge payload/configuration paths and 1.21 components that cannot be copied to the Forge 1.20.1 target. | SRC-207, FIND-001, FIND-004 | Any retained action requiring payload negotiation, a screen, HUD, menu, or client class is removed or returned to inventory classification. |
| OBSERVED | Backend persistence gap | Existing home records lack a server and world-generation identity; safe teleport validates local dimensions and hazards only. | SRC-203 and repository map fingerprints | Any world replacement, schema revision, or location migration changes invalidates migration and recovery evidence. |
| OBSERVED | SQLite prerequisite | Xerial SQLite JDBC 3.53.4.0 is pinned as EXT-009, but Java 17 native loading, WAL behavior, backup, and recovery remain unproven. | EXT-009, SRC-604, FIND-111 | Any artifact hash, license/advisory result, runtime classloader, operating system, or JDBC configuration change reruns the exact runtime drill. |
| PROPOSED | Diagnostics | The common `sef debug` diagnostic contract is default-off, console-safe, bounded, and independent from unsampled audit capture. | SEF-IF-002 and master diagnostics contract | Contract signature, command root, permission, budget, or output ownership change invalidates dependent phase fixtures. |

## Scope Boundaries

### Included Scope

- SEF-REQ-001 foundation work: create the native Forge 1.20.1 and Java 17 server-only integration boundary, retain only inventory-approved behavior, and prove dedicated startup without SEF client protocol or classes.
- SEF-REQ-003: remove all economic policy and surfaces, including construction, commands, permissions, configuration, persistence contracts, resources, dependencies, and tests, while preserving existing owner files unchanged and inert.
- SEF-REQ-004: remove custom screens, menus and menu-dependent utilities, HUD, Fancy Tags, client rendering, client payload/configuration negotiation, interface-only disguise paths, related configuration/resources/tests, and client linkage.
- SEF-REQ-006: deliver versioned retained backend data, durable world generation, typed identity/location records, atomic migration, corruption quarantine, SQLite WAL backup/recovery, and bounded shutdown.
- SEF-REQ-019: deliver validated modular configuration, atomic last-good reload, the initial category registry, and the shared bounded diagnostic facility and support procedure.

### Explicit Exclusions

- SEF-REQ-005 command policy and SEF-REQ-030 rendering/action tokens begin in Phase 002. This phase may supply typed `Actor`, `Session`, configuration, and diagnostics inputs but does not register the catalog or presentation implementation.
- Retained gameplay, local homes and safe teleports, network authority, Velocity, audit, RTP, and lifecycle messages belong to their registered later phases. No future consumer is a prerequisite to publishing this phase's interfaces.
- Full retained action inventory closure and final no-unresolved-1.21-dependency proof are Phase 015 gates. This phase uses the approved inventory to establish a foundation and reports any newly observed discrepancy without claiming final parity.
- There is no custom replacement screen, HUD, client payload, client mod, launcher, economy compatibility shim, data deletion, world downgrade, production migration, public release, or production rollout.

## Phase Contract

### SEF-PHASE-001 — Establish native server foundation, removals, safe data and diagnostics

**Objective:** Produce a Forge 1.20.1, Java 17, server-only foundation with excluded economy and interface closure, preserved/migratable backend data, stable identity and world records, and a reusable diagnostics/configuration contract.  
**Owner:** Forge backend  
**Dependencies:** SEF-PHASE-000, SEF-REQ-002, SEF-REQ-010, EXT-009, DEC-001, DEC-002, DEC-003, DEC-004, DEC-005, DEC-006, DEC-007, DEC-008, DEC-009, DEC-010, DEC-011, DEC-012, DEC-013, DEC-014, DEC-015, DEC-016, DEC-017, DEC-018  
**Canonical requirements:** SEF-REQ-001, SEF-REQ-003, SEF-REQ-004, SEF-REQ-006, SEF-REQ-019  
**Documentation and release impact:** Update implemented behavior in `README.md`, `docs/README.md`, `DOCUMENTATION.md`, `docs/configuration/`, `docs/migrations/`, `docs/operations/backup-recovery.md`, `docs/test/`, `docs/verification/phase-001/`, and `docs/troubleshooting/diagnostics.md` only when each described artifact exists. No public release occurs.  
**Next transition:** SEF-PHASE-002, implement command policy and renderer after the checked Phase 001 integration, resulting `forge-1.20.1` verification, and signed phase tag.

**Entry criteria**

- Phase 000 is merged and tagged on the applicable product branch; its action disposition inventory and external feasibility receipt are present, their exact candidate identities are still valid, and no compatibility blocker is being hidden by this phase.
- The target/reflection revision, exact Forge task graph, Java 17 toolchain, EXT-009 artifact hash/license/advisory state, and no-GUI server command are rediscovered and recorded before changes or tests.
- A phase milestone, issue/Project state, branch rules, required checks, and signing capability are reconciled before phase implementation. This planning blueprint itself performs no remote action.

**Implementation scope**

- Implement SEF-REQ-001, SEF-REQ-003, SEF-REQ-004, SEF-REQ-006, SEF-REQ-019 through the work packages and acceptance obligations below.

- Establish planned native foundation components around observed `ServerEssentialsForge`, `KernelServices`, `ModuleConfigRegistry`, `ModuleConfigService`, `HomeRecord`, `SavedLocation`, and `SafeTeleportService`; do not import NeoForge 1.21 payload/configuration types or 1.21 data components. Trace: SEF-PHASE-001.
- Produce SEF-IF-001 and SEF-IF-002 exactly as frozen below. Backend creates durable world generations; no network session source becomes authoritative in this phase. Trace: SEF-PHASE-001.
- Remove excluded code only after its dependency closure is mapped. Preserve nonmonetary permissions, cooldown/confirmation concepts, textual command capability, ordinary vanilla chat/tab/visibility behavior, and legacy data files without parsing monetary values into active state. Trace: SEF-PHASE-001.
- Run the data migration through a new owned destination after preflight counts/checksums and a verified backup. Unsupported schema, item, registry, or world input is refused and quarantined without destructive reinterpretation. Trace: SEF-PHASE-001.

**Execution order**

1. `P001-TASK-001` delivers SEF-IF-002 configuration and diagnostics before any dependent migration, removal, or startup proof. Trace: SEF-PHASE-001.
2. `P001-TASK-002` applies the data/configuration/removal foundation, including the complete economy and interface dependency closure. Trace: SEF-PHASE-001.
3. `P001-TASK-003` delivers SEF-IF-001 durable identity/world/location records and executes migration, WAL backup, corruption, interruption, and recovery fixtures against the real SQLite driver. Trace: SEF-PHASE-001.
4. `P001-TASK-004` performs dependency-aware build, class/resource/configuration scans, dedicated-server readiness, no-client requirement proof, documentation, cleanup, and phase handoff verification. Trace: SEF-PHASE-001.

**Required evidence**

- Source/disposition and transitive-reference reports for all removed economy/interface surfaces, with no active command, permission, configuration key, module, service registration, resource, test, dependency, or JAR linkage remaining.
- Exact candidate commit/JAR hashes, Java/Forge/SQLite dependency identity, Gradle task graph, unit and integration reports, and class/resource inspection results.
- Real Java 17 SQLite native load, WAL checkpoint/backup/restore/restart evidence; preflight counts/checksums; interrupted write, corrupt/unknown schema, unsupported data, and recovery/quarantine outcomes.
- Dedicated no-GUI Forge server startup with `eula=true` readback, console diagnostics control proof, and an actual compatible vanilla client login only for the residual no-SEF-client claim. The latter must use the laptop protocol below and remains open if it cannot be performed silently and with exact identity.
- Sanitized support runbook rehearsal and per-host cleanup receipts. Server-only results never substitute for the client login/receipt gate.

**Exit criteria**

- No known mandatory phase-owned defect remains. Every such defect must be fixed and its affected evidence rerun before phase closure.

- P001-TASK-001 diagnostics controls and typed signals are available before every phase-owned runtime fixture; invalid reload preserves the last-good configuration and all required control negative cases pass.
- The dedicated artifact loads without client classes/payload negotiation, starts with an ordinary compatible client path, and no active economy/interface surface survives. Legacy files are unchanged and inert.
- Supported retained data migrates with matching counts/checksums/revisions; corrupt, unknown, unsupported, interrupted, and stale inputs are quarantined/refused without deletion; backup/recovery proves durable identity and world-generation preservation.
- Phase integration uses the sequential checked PR merge workflow, verifies the resulting `forge-1.20.1` commit, creates the required signed annotated tag, preserves evidence binding, and confirms cleanup. The final all-inventory SEF-AC-001 closure remains explicitly open for Phase 015.
- No known mandatory Phase 001 defect, cleanup failure, or falsified foundation assumption remains unreported.

## Inputs and Upstream Contracts

| Input or contract | Provider | Required state | Validation | Failure behavior |
|---|---|---|---|---|
| Retained/removed action matrix | SEF-PHASE-000, SEF-REQ-002 | Every observed action has a disposition and source locator. | Reconcile task inputs to the approved matrix and record new nonconforming source references. | Stop the affected removal/port package and return the inventory conflict; do not silently retain ambiguous interface or economy code. |
| Proxy feasibility receipt | SEF-PHASE-000, SEF-REQ-010 | Exact adapter matrix and candidate identity remain current. | Check the receipt's pins before treating network consumers as future-only. | Preserve the phase blocker; do not add a handshake/client fallback in Forge foundation work. |
| SQLite JDBC | EXT-009 | Xerial SQLite JDBC 3.53.4.0 has the recorded artifact identity and notices. | Recheck checksum, advisory/license evidence, Java 17 classloading, native extraction, and WAL fixture. | Keep SEF-REQ-006 gates open; do not substitute an arbitrary driver or uploaded database file. |
| Forge native entry points | Existing target and observed reference locators | `ServerEssentialsForge` is the server bootstrap; `KernelServices` and module registry are the removal roots; teleport records are data-migration inputs. | Compile against Forge 47.3.12 and inspect actual task graph/entry points before implementation. | Adapt only to verified Forge 1.20.1 APIs or return an implementation blocker; do not copy NeoForge code blindly. |
| Diagnostic policy | Master SEF-IF-002 and diagnostics contract | Console-safe, default-off controls with the stated limits and redaction. | P001-TASK-001 control tests and signal-schema validation. | Dependent runtime tests do not begin until control/output failures are fixed or the affected gate remains open. |

## Outputs and Downstream Contracts

| Output or contract | Consumer | Guaranteed state | Compatibility or versioning | Evidence |
|---|---|---|---|---|
| SEF-IF-001 identity and world records | Phases 003 through 014 as registered | `Actor`, `Session`, `WorldRef`, and `Location` are typed; backend durable world generation is authoritative; display names do not confer authority. | Registry/schema version 1; future extensions are additive and never reinterpret a world generation. | Migration/restart/WorldRef refusal fixtures and contract compilation. |
| SEF-IF-002 configuration and diagnostics | Phases 002 through 016 as registered | Atomic owner-executor `ConfigSnapshot` swap, bounded diagnostic controls, typed `DiagnosticEvent`, and last-good behavior. | Registry/schema version 1; categories extend one registry and preserve field meanings/budgets. | Console enable/status/disable/reload/reset/limit/redaction fixtures. |
| Server-only removal boundary | Phases 002, 003, and release integration | No SEF client protocol, custom screens/HUDs/Fancy Tags/menu utilities, or economic policy can be referenced by new work. | New code must be rejected by dependency/JAR/configuration scans if it crosses this boundary. | Static graph, JAR, config, command/permission, and runtime startup reports. |
| Versioned retained backend store | Phases 003, 005, 007, 012, and 014 | New owned destination retains supported records and explicit revision/world identity; failures are quarantined. | Explicit schema version and migration ledger; unknown data never coerces or deletes source. | Counts/checksums, backup/restore, corruption and interrupted-write reports. |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
|---|---|---|---|---|---|---|
| P001-TASK-001 | SEF-REQ-019 | Implement the common configuration loader/validator and the sole diagnostics facility before phase-owned tests. Provide planned console grammar `sef debug on foundation fixture-001 60`, `sef debug status`, `sef debug off 550e8400-e29b-41d4-a716-446655440000`, and `sef debug off all`, permission `sef.debug.manage`, default-off state, idempotent disable, owner-executor validation/swap, last-good preservation, bounded worker/output path, and category registration for `foundation`, `config`, `migration`, `removal`, and `startup`. | Approved inventory, master SEF-IF-002, existing command root discovery, Phase 000 task 001 and Phase 000 task 002 evidence. | SEF-IF-002 implementation, validated modular config snapshot, diagnostics command/control tests, initial support guide and structured output contract. | Planned common foundation/configuration/diagnostic components; observed `ServerEssentialsForge` bootstrap and existing command dispatcher after discovery; SEF-IF-002. | Console enable/status/disable, unauthorized denial, absent target, invalid config/reload, timeout/restart reset, capture/output/queue limits, secret/content/address redaction, disabled and enabled bounded-overhead tests. |
| P001-TASK-002 | SEF-REQ-001, SEF-REQ-003, SEF-REQ-004, SEF-REQ-006, SEF-REQ-019 | Build one removal/data/configuration dependency closure from `KernelServices`, module registry/service, entrypoint, payload/protocol, inventory utility, recovery, resources, commands, permissions, dependencies, generated configuration, and tests. Remove economy and custom-interface graph roots and transitive registrations. Rebuild retained module dependencies and generated config. Preserve legacy files byte-for-byte until explicit migration writes a separate owned destination; never load inactive monetary data into active state. | P001-TASK-001; SEF-REQ-002 inventory; SRC-201, SRC-202, SRC-207; actual Gradle/source graph. | Removal manifest with each disposition and proof, absence scan rules, updated retained configuration schema, inert legacy-file compatibility policy, server-only bootstrap boundary. | `KernelServices`, `ModuleConfigRegistry`, `ModuleConfigService`, `ServerEssentialsForge`, `gui/protocol/SefNetwork`, `InventoryUtilityCommands`, recovery codec import path, related resources/dependencies/tests. | Compile and dependency graph; exact command/config/permission/resource/JAR scans; dedicated startup; legacy economy/interface file fixture remains unchanged; retained nonmonetary config and ordinary vanilla chat/tab/visibility smoke paths still load. |
| P001-TASK-003 | SEF-REQ-001, SEF-REQ-006, SEF-REQ-019 | Define versioned backend persistence and the identity/world records used later. Give every local persistent world a generated durable `WorldRef`; migrate supported configuration/player/location records to a separate owned store with preflight counts/checksums, atomic writes, revision preservation, migration ledger, corruption quarantine, bounded shutdown, SQLite WAL checkpoint/backup/restore, and explicit refusal of unsupported 1.21 item/world data. | P001-TASK-001 configuration/diagnostic service; P001-TASK-002 removal boundary; EXT-009; observed `HomeRecord`, `SavedLocation`, `SafeTeleportService`. | SEF-IF-001 contract implementation, versioned schema and migration/backup/recovery services, quarantine records, recovery runbook and migration report format. | Planned backend persistence and world-identity components; observed home/location/teleport record boundaries; SEF-IF-001. | Real Java 17 SQLite native/WAL fixture: empty bootstrap, supported import, interrupted write before/after commit, corrupt/unknown schema, unsupported registry/item/world data, world replacement, backup including WAL state, restore/restart, and bounded shutdown. Assert source preservation, counts/checksums, IDs/revisions, and typed error codes. |
| P001-TASK-004 | SEF-REQ-001, SEF-REQ-003, SEF-REQ-004, SEF-REQ-006, SEF-REQ-019 | Execute the phase evidence ladder, package inspection, dedicated-server readiness, residual vanilla-client compatibility check, documentation updates, and integration handoff. Bind every result to exact candidate/source/driver/configuration/world-generation identities. | P001-TASK-001 through P001-TASK-003; checked no-GUI task graph; disposable host anchors; applicable repository checks. | Sanitized phase evidence packet, removal/migration/configuration reports, JAR manifest/class/resource report, docs, cleanup receipts, PR and tag handoff material. | Forge artifact, server runtime, diagnostics output, `docs/verification/phase-001/`, `docs/test/`, `docs/migrations/`, `docs/troubleshooting/diagnostics.md`. | Formatter/static/unit/integration/build order; real dedicated server `eula=true` readiness; JAR inspection; laptop-only actual compatible client login if and only if exact isolated audio/window/stream proof is available; post-merge candidate rebuild and cleanup verification. |

P001-TASK-001 must finish before P001-TASK-002 or P001-TASK-003 runs a runtime fixture. P001-TASK-002 supplies the removal boundary before P001-TASK-004 inspects the artifact. P001-TASK-003 may implement its typed records after the configuration contract but before removal verification completes; it must consume the P001-TASK-002 data policy before importing any legacy record. No package may proceed with an unresolved source-disposition conflict, failed diagnostic control, unverified JDBC identity, or incomplete cleanup.

## Architecture and Implementation Boundaries

Forge backend remains the owner of local worlds, local persistence, safe-world references, and every world mutation. The later proxy may issue authenticated connection epochs, but Phase 001 does not introduce a proxy or let network input write a backend record. `Actor` carries explicit source and permission revision; `Session` remains a typed future-consumer value, not a new shared authority store. `WorldRef.backendId`, durable `worldGeneration`, dimension, and registry digest prevent a same-named replacement world from being confused with historical data. `Location` always uses finite coordinates/rotation and a `WorldRef`; display/world names are never authority.

Configuration is parsed and validated into immutable `ConfigSnapshot` values off a world/tick path, then atomically swapped on the owner executor only when `expectedGeneration` matches. Invalid proposals yield `INVALID_CONFIG` and preserve active configuration. Reload never resurrects removed economy/interface keys as active policy. Config provenance and a sanitized digest appear in diagnostics; secrets and full private content do not. New schemas are explicit versions. Unknown mandatory schema/data is refused, copied into an owned quarantine area with reason/metadata, and remains recoverable from the source/backup rather than being rewritten.

Local backend storage is separate from future proxy SQLite authority, audit journals, SQL history, RTP ledgers, and worlds. SQLite uses the pinned driver only, one bounded persistence writer per local store, parameterized typed access, WAL-aware backup/checkpoint procedure, atomic temporary-file replacement where files are written, and bounded shutdown. A backup is never a lone live database main file. No worker reads a live Minecraft object; workers receive bounded immutable record/NBT snapshots. Returning to the owner thread rechecks the world generation/configuration before a game-side action. This phase does not promise arbitrary external database paths, trust-all properties, or unbounded queues.

Economy removal is architectural, not a disabled module flag. Delete or sever account/balance/payment/price/worth/sale/shop/sign/market/trade/cost reservation/provider/storage/configuration/command/permission/test/resource/dependency roots. Preserve ordinary authorization, cooldown, confirmation, command routing concepts, and data that later policy requires. Interface removal likewise eliminates custom payload/config tasks, client initializers, screens, menu services and menu-dependent utilities, HUD/Fancy Tags/rendering, interface-only disguise, client resources, and their tests/config keys. Retained server behavior uses vanilla chat/tab/visibility paths only. Any ambiguity discovered by a source scan returns to the Phase 000 inventory classification instead of being reintroduced through a replacement UI.

Diagnostic events are emitted on a bounded worker after the owner thread captures minimal typed state. They carry SEF-IF-002 identity fields and must include candidate/config generation, side, boot, sequence, correlation, desired and actual typed maps, reason, and units. Diagnostic I/O, SQLite I/O, formatting, and long scans never block a tick thread. Capture is independent from the later mandatory unsampled audit system and cannot change the behavior under test.

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
|---|---|---|---|---|
| SEF-RISK-002, transitive economy/interface reference survives removal | `removal.closure` diagnostic includes root, referencer, surface, and disposition; JAR/class/resource/config/command scans identify an active reference. | Fail the package and startup gate. A disabled module, unused config key, or dormant service is not acceptable. | Remove the exact remaining graph edge, regenerate retained configuration, rerun closure and retained smoke paths. Legacy owner files remain untouched. | Fixture starts from a reference-shaped config/module set; invoke the real `ServerEssentialsForge` bootstrap; assert no excluded service/command/key/class/resource is reachable and nonmonetary module startup remains valid. |
| Invalid or stale configuration generation | `config.validate` records expected/actual generation, schema, sanitized digest, error path, and `INVALID_CONFIG` or `STALE_REVISION`. | Reject the proposal without partial activation. Existing snapshot remains observable by status. | Correct only the invalid proposed file, revalidate, and CAS against the current generation. | Console `sef debug`/configuration reload fixture submits malformed, unknown mandatory, stale, and removed-key configurations; assert last-good settings and unchanged command behavior. |
| SQLite native load, WAL, or checkpoint failure | `migration.store` records driver/version, native-load classification, journal mode, checkpoint duration, source/target IDs, and error reason without paths/secrets. | Mark storage unavailable and refuse migration/unsafe persistence. Do not select another driver or silently use a non-WAL copy. | Retain source, close owned store, restore only a verified backup including WAL state, and retry in a fresh disposable runtime after prerequisite correction. | Java 17 isolated fixture exercises load/open/checkpoint/backup/restore/restart; fault injection fails native load/checkpoint and asserts no source deletion or success report. |
| Crash/interruption around durable write | `migration.transition` records operation ID, state, source count/checksum, target count/checksum, transaction/backup boundary, and recovery reason. | On restart, recover committed state exactly once or quarantine an uncertain attempt. Never infer success from a partial file. | Read migration ledger and atomic state; preserve source/backup; reconcile matching counts/revisions or retain quarantine for operator recovery. | Terminate the owned store before and after durable commit boundaries, restart with same fixture, and assert no duplicate/missing supported record and explicit uncertain/quarantine outcome. |
| Corrupt, unknown, unsupported 1.21 item/world record | `migration.refusal` includes source kind, schema/registry digest, field category, reason, and quarantine ID. | Refuse destructive interpretation and do not downgrade worlds/components or deserialize unsafe objects. | Preserve input and quarantine a bounded copy/metadata; permit supported records to continue only when counts distinguish them. | Feed corrupt header, unknown schema, oversized/deep/unknown NBT, registry mismatch, and 1.21 component fixture through the real migration entry point; assert source bytes unchanged and no target record. |
| World directory/dimension name reused after replacement | `world.identity` records backend ID, world generation, dimension, registry digest, expected/actual generation, and `WORLD_REPLACED`. | Reject old record reconstruction against the new world even if names match. | Create a new durable generation; retain old record as historical/quarantined data for later explicit mapping. | Replace a disposable world directory with the same name/dimension and invoke real location resolution; assert refusal, no teleport/world mutation, and a fresh generation record. |
| Diagnostic abuse, missing target, output failure, or timeout | `capture.status` includes capture ID, scope, target state, remaining seconds, events, bytes, dropped count, output classification, and reason. | Deny unauthorized control, reject absent target, stop at duration/event/byte/queue limits, and report `OUTPUT_UNAVAILABLE` without gameplay mutation. | Idempotent console disable; cleanup only capture-owned output after evidence consumption; fix filesystem/config permission and create a new capture. | Real console handler fixture covers permission denial, absent/removed target, unwritable output, 60-second default/300-second maximum, 200 events/second, 10,000 events, 8 MiB, 1024 queue, two captures, reload/restart reset, and unchanged gameplay. |
| Dedicated startup exposes a client dependency | Gradle task graph and JAR report identify `net.minecraft.client`, screen/HUD/payload/config-task linkage; server startup log and diagnostic `startup.boundary` record classloader reason. | Fail server-only readiness before accepting the artifact. Do not load a software client/server hybrid or add a required client install. | Remove/relocate the client dependency and rerun server startup plus relevant retained path smoke proof. | No-GUI dedicated Forge runtime reaches readiness with a non-SEF client able to connect under the separate laptop gate; class/resource scan has zero prohibited linkage. |

Each failure fixture invokes a real planned entry point: the server console/config reload handler for configuration and capture controls; `ServerEssentialsForge` bootstrap for module construction/removal; the versioned backend migration service for source-to-owned-store transitions; and local location resolution through the `HomeRecord`/`SavedLocation`/`SafeTeleportService` boundary for generation refusal. Unit fixtures may test pure codecs/checksums, but they cannot replace the native SQLite, dedicated-server, configuration, migration, or residual client compatibility gates. Every wait is bounded: driver/bootstrap readiness follows the discovered task timeout, captures default to 60 seconds and maximum 300 seconds, shutdown follows the discovered bounded lifecycle timeout, and restart recovery waits only until the owned server/store reports ready or the registered deadline expires.

## Diagnostics and Debugging

**Requirement IDs:** SEF-REQ-001, SEF-REQ-003, SEF-REQ-004, SEF-REQ-006, SEF-REQ-019  
**Task IDs:** P001-TASK-001, P001-TASK-002, P001-TASK-003, P001-TASK-004  
**Controls:** Planned console-safe controls include `sef debug on migration fixture-001 60`, `sef debug status`, `sef debug off 550e8400-e29b-41d4-a716-446655440000`, and `sef debug off all`; `sef.debug.manage` is required, all controls are default-off and idempotent, a target is explicit when needed, and `sef debug off all` only stops captures owned by this process.  
**Signals:** SEF-IF-002 records `captureId`, `correlationId`, `side`, `boot`, `sequence`, `tick`, `monotonicNanos`, `utc`, `desired`, `actual`, `reason`, `units`, `configGeneration`, and `candidateDigest`; local categories add typed fields in the table below.  
**Collection procedure:** Use the numbered Phase 001 runbook below to register resources, enable one category, reproduce a real native entry path, inspect the correlation, disable, sanitize, retain minimal evidence, and clean every owned resource.  
**Headless verification:** On `node-1`, first inspect the exact Gradle graph to verify it starts no client, renderer, or display. Run the real console handler, migration service, and no-GUI dedicated Forge startup in nested owned fixtures; configure/read back `eula=true`, assert readiness, and use console fixtures without a joined owner. This proves server state and not client receipt.  
**Client verification:** Only the residual SEF-AC-001 compatible vanilla-client login/receipt requires a client. It runs on the verified Linux laptop against the exact private dedicated endpoint after both sides prove the intended player joined. If laptop control, artifact match, private endpoint, discrete renderer, window identity, or mute proof is unavailable, stop the owned client and leave only this client gate open while retaining headless results.  
**Client audio isolation:** P001-TASK-004 launches no client on `node-1`. Before launch of any required laptop client, create a discovered isolated pinned-version instance and set its master output to zero, verify the laptop desktop and discrete renderer, resolve the exact owned Hyprland window address/class/title/PID with `hyprctl clients -j`, correlate only that owned PID tree to its PipeWire or PulseAudio application stream, mute it with `wpctl` or `pactl`, and verify the owned stream is muted before login. Immediately mute every replacement stream after reload, device change, reconnect, or recreation; never mute a default sink, microphone, unrelated client, or other application. On uncertain window, PID, stream identity, or mute readback, stop the owned client and leave the gate open. Teardown stops the owned client and watcher, confirms process and stream exit, removes the disposable isolated-instance audio state and temporary routing, and verifies their absence.  
**Budgets and privacy:** Captures reset off on reload/restart, default to 60 seconds and maximum 300 seconds, permit 200 events/second, 10,000 events, 8 MiB, 1024 queued events, and two simultaneous captures per process. Emit truncation/drop counts; redact credentials, private content, private addresses, full records, and external database properties; no diagnostic or SQLite I/O occurs on a tick thread. Mandatory audit capture remains a separate unsampled later system.  
**Regression and support:** P001-TASK-001 adds enable/status/disable, denial, absent target, timeout/reload/reset, queue/output limit, redaction, off-mode, and unchanged-behavior coverage. P001-TASK-004 rehearses `docs/troubleshooting/diagnostics.md`, links it from README/docs index when implemented, and retains a minimal sanitized candidate/config/capture/reproduction/cleanup packet.

| Signal | Source and unit | Expected observation |
|---|---|---|
| `config.validate` | Forge owner executor, one event per validation/swap; generation:u64, schema:u16, errorPath:string? | Successful CAS increments generation once; malformed/stale proposal reports reason and leaves active digest unchanged. |
| `removal.closure` | Foundation scanner, one event per graph root/reference; references:u32 and surface:enum | Every excluded root reaches zero active registrations, resources, config keys, commands, permissions, dependencies, and tests. |
| `startup.boundary` | Dedicated server bootstrap; classCount:u32, prohibitedReferenceCount:u32, readinessMs:f64 | Prohibited client/payload/interface linkage is zero before readiness; any reference fails with its source path/class reason. |
| `migration.transition` | Backend persistence writer; records:u64, checksum:sha256, durationMs:f64, state:enum | Import/commit/restart preserves supported IDs/revisions/counts exactly once, or records an explicit quarantine/recovery state. |
| `migration.refusal` | Migration validator; schema:u16?, registryDigest:sha256?, bytes:u64, quarantineId:UUID? | Unknown/corrupt/unsupported input has a typed refusal and no destructive target write. |
| `world.identity` | World identity store; backendId:string, worldGeneration:UUID, dimension:resource_location, registryDigest:sha256 | Same named replacement produces a new generation and old location resolution returns `WORLD_REPLACED`. |
| `capture.status` | Diagnostic worker; events:u32, bytes:u64, dropped:u64, remainingSeconds:u16, output:string | Status reports scoped limits/path; timeout/disable stops matching events and output failures are explicit. |

1. Discover the exact candidate commit/JAR hashes, Forge/Java/driver versions, no-GUI task graph, server command, disposable runtime path under the verified project anchor, test-owned database/world/log/capture paths, and cleanup owners. Register teardown before launch. For each server fixture, write/read back that fixture's `eula=true`; do not alter a personal instance.
2. Start no client on `node-1`. Enable the smallest console-scoped category, for example `sef debug on migration fixture-001 60`, then run `sef debug status` and confirm the capture ID, side, target, limits, output under the owned runtime `logs/sef/diagnostics` directory, candidate digest, and sanitized configuration generation. Missing target, denial, or output failure is itself a falsifiable negative result.
3. Apply one real entry-path stimulus: boot `ServerEssentialsForge` against a removal fixture, request atomic config reload, migrate the prepared legacy record set, cut the owned process before/after the registered commit boundary, or resolve a location against a replacement world generation. Wait only to the registered readiness/shutdown deadline. Assert independent source bytes, preflight counts/checksums, resulting schema/revisions, class/resource absence, and dedicated readiness rather than the implementation's success text alone.
4. Filter the JSONL by the capture correlation/operation ID, inspect ordered states/reasons/units and compare them with the independent fixture oracle. Save only decisive redacted fields with candidate, fixture, source and target hashes. Then run affected unit/integration/build checks in their discovered order.
5. Run `sef debug off 550e8400-e29b-41d4-a716-446655440000` followed by `sef debug status`, trigger a harmless matching operation, and assert no later record appears. On timeout/reload/restart, assert capture reset. Preserve the minimum sanitized support packet in the phase evidence location and record any residual client gate accurately.
6. After the final consumer, stop only owned server/store/watchers, confirm process exit, remove the exact disposable database, WAL/shm files, worlds, logs, captures, reports, downloads, and temporary test output without following symlinks, and verify their absence on every used host. Preserve source, tracked fixtures, shared caches, personal worlds/instances, and required sanitized evidence. Cleanup failure keeps the phase gate open.

## Verification Matrix

| Requirement or task | Static or unit | Integration | Real workflow or runtime | Negative and recovery | Execution host and prerequisites | Evidence artifact |
|---|---|---|---|---|---|---|
| SEF-REQ-019, P001-TASK-001 | Config schema/codec, generation CAS, permission/limit/redaction tests. | Actual console dispatcher with bounded diagnostic worker and atomic reload. | No-GUI dedicated server console enables/status/disables capture and preserves last-good config. | Invalid/stale/reload/reset/timeout/absent target/output failure/limit exhaustion. | `node-1` only after task graph proves headless; owned nested runtime, eula readback, no joined player. | Sanitized config/capture reports and support-runbook rehearsal. |
| SEF-REQ-003 and SEF-REQ-004, P001-TASK-002 | Dependency/module/configuration/command/permission/resource graph and forbidden package scans. | Build plus generated configuration and bootstrap integration. | Dedicated no-GUI startup and retained vanilla chat/tab/visibility smoke path. | Legacy economy/interface files present but inert; a synthetic surviving registration/class/config key fails closure. | `node-1` headless, exact candidate/Gradle graph, isolated runtime. | Removal matrix, JAR/class/resource scan, generated config snapshots, startup log. |
| SEF-REQ-006, P001-TASK-003 | Pure record/schema/checksum/finite-location codec tests. | Real Xerial SQLite JDBC Java 17 native/WAL/checkpoint/backup/restore/restart fixture. | Migration service processes supported legacy records into an owned destination and resolves current world identity. | Corrupt/unknown/oversized/unsupported input, interrupted write, source/target mismatch, same-name replacement world, WAL/backup failure. | `node-1` headless, EXT-009 revalidated, non-root nested database/world fixture, eula only when dedicated server is used. | Driver identity, migration ledger/count/hash report, quarantine/recovery report, cleanup receipt. |
| SEF-REQ-001, P001-TASK-004 | Java 17/Forge compile, forbidden client/payload linkage and final JAR inspection. | Dedicated bootstrap against merged phase candidate. | Compatible vanilla client login only for residual no-SEF-client proof; both sides confirm joined world. | Client class/payload linkage fails server gate; missing laptop identity/audio/GPU/private endpoint leaves client gate open. | Dedicated server on `node-1`; matching laptop client only after exact isolated-path, GPU/window/PID/stream mute procedure and private endpoint readiness. | Candidate hashes, task graph, JAR report, server log, laptop evidence only if complete, per-host cleanup receipts. |

## Documentation, Operations, and Release

P001-TASK-004 updates documentation only for delivered artifacts: supported Forge/Java baseline, server-only and removal boundary, modular configuration and diagnostics controls/permissions/limits/redaction, backend data schema/migration/quarantine, SQLite WAL-aware backup/restore, and explicit retained legacy-file behavior. The diagnostics guide provides console grammar, status output location, bounded reproduction, redaction, and cleanup procedure. Migration and backup documentation warns that unsupported 1.21 data/worlds are refused, source files are retained, and a live SQLite main file alone is not a backup. README and docs index link only to created topics. Wiki content follows an approved merge; release/publication does not occur.

Before implementation, reconcile the phase milestone/issues/Project fields and required checks. After all local gates pass, use the phase branch from the latest approved `forge-1.20.1` base, signed EnVy commits, a checked PR merge commit, resulting base verification, and a signed annotated phase tag. Git/GitHub text follows the owner lowercase punctuation rule. No direct base push, stacked branch, public review trigger, release upload, or production mutation is authorized by this phase plan.

## Risks and Evidence Invalidation

| Risk ID and owner task | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
|---|---|---|---|---|---|
| SEF-RISK-002, P001-TASK-002 and P001-TASK-004 | Remove roots plus transitive construction/config/dependency/resource/test closure. | `removal.closure`, forbidden linkage/config/command scans, startup boundary failure. | Remove exact residual edge and regenerate retained config without touching legacy owner files. | Removal closure, dedicated startup, JAR/class/resource, and client-boundary proof. | Rerun closure to artifact inspection and affected retained startup smoke paths. |
| SQLite readiness risk, P001-TASK-003 | Pin EXT-009, typed store, WAL-aware backup, preflight/ledger/quarantine. | Native-load, checkpoint, migration transition/refusal, checksum mismatch signals. | Preserve source and backup, close owned store, restore/reconcile or keep quarantine. | Migration, restart, backup/restore, data-identity evidence. | Rerun exact Java/driver/OS fixture and dependent persistence checks. |
| SEF-RISK-020, P001-TASK-004 | Bind evidence to candidate/source/driver/config/world generation/host; use post-merge rebuild. | Candidate digest/profile/host/task graph mismatch. | Reject stale evidence and rebuild/retest the narrow impacted path, then dependencies. | Every result tied to changed binding. | Repeat static, runtime, residual client, and cleanup gates affected by the changed binding. |
| Diagnostic observability risk, P001-TASK-001 | Deliver single bounded console facility before dependent runtime tests. | Missing required fields, unbounded queue/output, status/control failure, redaction leak. | Disable capture, correct facility, sanitize affected evidence, rerun tests with a new capture. | Any diagnostic-dependent migration/removal/startup proof. | Rerun control self-tests then the exact phase fixture using the corrected schema. |

## Phase Completion Packet

- The merged source change, signed commit, checked PR merge receipt, resulting `forge-1.20.1` commit verification, and signed annotated Phase 001 tag.
- Approved inventory link plus Phase 001 removal closure report listing every removed root and residual scan result; final JAR hash/class/resource/dependency/configuration/command/permission evidence showing no economy or custom-interface/client payload surface.
- Exact Forge/Java/Gradle/EXT-009 identities, discovered task graph, formatter/static/unit/integration/build results, dedicated-server readiness logs, and actual compatible-client receipt only if its laptop gate fully passes.
- Schema/configuration contract evidence, SEF-IF-001 and SEF-IF-002 compilation/fixture proof, migration ledger with source/target counts/checksums, source-preservation evidence, corrupt/unknown/unsupported/refusal records, WAL-aware backup/restore/restart/interruption recovery results, and bounded-shutdown proof.
- `docs/troubleshooting/diagnostics.md` runbook rehearsal, sanitized diagnostic packet, relevant README/docs index/general/migration/operations/test/verification updates, and post-merge wiki update only after approval.
- Exact test-owned resource register and cleanup receipts for `node-1` and, if used, laptop: process exit, audio watcher/stream disappearance, disposable fixtures/logs/captures/worlds/databases/WAL files removed, required sanitized evidence retained, and any leftover named as an open gate.

## Next Transition

Only after all Phase 001 implementation, evidence, cleanup, PR merge, resulting product-branch verification, and signed-tag gates complete may the execution cursor move contiguously to SEF-PHASE-002. Its first action is Phase 002 task 001, using the merged SEF-IF-001/SEF-IF-002 contracts and removal boundary to implement shared command policy. It must not start from an unmerged Phase 001 branch or treat the final retained-inventory proof as already complete.

## Noncanonical Interface Projection

This derived projection is copied from the frozen phase interface packet. It supports drift checks only and does not create a second canonical contract.

```json
{
  "phaseId": "SEF-PHASE-001",
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
    }
  ]
}
```
