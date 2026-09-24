# Phase 004 Execution Plan

> **Plan ID:** PLAN-PHASE-004  
> **Phase ID:** SEFAUD-PHASE-004  
> **Owner:** GUI presentation  
> **Classification:** MANDATORY  
> **Master plan:** [plan.md](../plan.md)  
> **Phase sequence:** 004 of 007  
> **Diagnostics contract:** 2

## Purpose and Ownership

This phase closes the single canonical UI requirement. It turns the integrated Phase 003 action contract and capture interface into a complete graphical and textual operator experience: every actual screen, HUD, fallback and feedback state is intentional, readable, accessible, private, and truthful about server authority. The master owns scope, shared interfaces, acceptance, host policy, and the final endpoint. This file owns only detailed Phase 004 execution and preserves the existing P004-TASK-001 through P004-TASK-010 meanings.

## Evidence-Based Entry State

| Evidence class | Area | Finding | Source or command | Freshness condition |
|---|---|---|---|---|
| OBSERVED | scope | Phase 004 exclusively owns UI quality after Phase 003 command closure. | master Sections 12 and 13 | master requirement or phase revision |
| OBSERVED | surfaces | Seven SefScreen subclasses, SefInvSeeScreen, pause entry, HUD, Fancy Tags render paths, and command feedback are observed presentation owners. | research repository map and prior Phase 004 blueprint | source, registry, or protocol change |
| OBSERVED | authority | SefGuiServer, SefSessionManager, SefNetwork and PanelActionValidator preserve server projection and revalidate mutation. | research repository map | session, payload, permission, or action change |
| OBSERVED | diagnostic prerequisite | Phase 003 task 010 is the producer of SEFAUD-IF-004. Existing doctors are not substitute capture proof. | master SEFAUD-IF-004 | Phase 003 integration or capture-schema change |
| UNKNOWN | client evidence | Current UI evidence does not establish all real input, rendering, narration, state or fallback claims. | FIND-008 | exact candidate, fixture, or client result |

## Scope Boundaries

### Included Scope

- SEFAUD-REQ-005 covers every enhanced screen, SefInvSeeScreen, pause entry, HUD and overlay, Fancy Tags surface, negotiated fallback, and Phase 003 administrator feedback row.
- SEFAUD-REQ-005 covers hierarchy, labels and values, localization, scale, viewport, resize, mouse, keyboard, focus, narration, tooltips, privacy, stale state, authority receipt, and actual presentation separately.

### Explicit Exclusions

- FUT-001 and NG-001 exclude implementing unavailable control families. Their presentation remains unavailable, nonmisleading, nonmutating, and negatively tested.
- FUT-002 and FUT-003 exclude new UI product scope and framework replacement. Repair only confirmed in-scope defects.
- NG-002 and NG-003 exclude platform changes, production data, publication, credentials, and production mutation.
- Phase 005 owns lifecycle and provider closure. Phase 006 owns final clean candidate verification. This phase executes only focused lifecycle or provider cases needed to prove its UI claim.

## Phase Contract

### SEFAUD-PHASE-004 — GUI and Operator Presentation

**Objective:** Deliver a complete responsive accessible private server-authoritative UI and feedback surface with no known blocking or materially misleading defect.  
**Owner:** GUI presentation  
**Dependencies:** SEFAUD-PHASE-003, SEFAUD-REQ-004, EXT-001, DEC-008, DEC-009  
**Canonical requirements:** SEFAUD-REQ-005  
**Documentation and release impact:** Update only affected README.md, DOCUMENTATION.md, test.md, acceptance, compatibility, Fancy Tags, InvSee, protocol, accessibility and troubleshooting documents after verified changes. No release publication.  
**Next transition:** SEFAUD-PHASE-005

**Entry criteria**

- Phase 003 is merged, its resulting candidate is verified, its signed tag exists, and its completion packet records a stable action matrix, capture controls, feedback expectations, and negative unavailable rows.
- SEFAUD-IF-001, SEFAUD-IF-002, SEFAUD-IF-004 and retained SEFAUD-IF-006 provenance are candidate-compatible. Missing capture capability blocks only capture-dependent rows.
- A clean phase branch begins from approved candidate history. Exact artifacts, dependency and configuration fingerprints, preexisting work, owned paths, and cleanup boundary are recorded.
- The node-1 dedicated-server fixture and laptop client fixture are available under EXT-001. This is one portable Minecraft Java client contract, not macOS or Windows client gates.

**Implementation scope**

- SEFAUD-REQ-005 inventory and classify every UI and feedback state before editing, then bind each confirmed defect to a matrix row, smallest repair, regression, invalidation and recovery.
- SEFAUD-REQ-005 preserve authoritative server decisions, audit identity and fallback domain semantics. Client caches, drafts and preferences never mint authority.
- SEFAUD-REQ-005 use only the portable Minecraft Java client fixture for graphical client claims. Analyze source/artifact reachability for native or platform claims without inventing foreign runtime tests.

**Execution order**

1. P004-TASK-001 executes SEFAUD-REQ-005 by freezing surface, state, feedback and evidence matrices from integrated Phase 003.
2. P004-TASK-002 executes SEFAUD-REQ-005 by repairing shared layout and accessibility primitives.
3. P004-TASK-003 executes SEFAUD-REQ-005 by closing panel, workflow, picker, control-editor and InvSee states.
4. P004-TASK-004 executes SEFAUD-REQ-005 by closing Fancy Tags studio and presentation states.
5. P004-TASK-005 executes SEFAUD-REQ-005 by closing pause, HUD, overlay and cleanup behavior.
6. P004-TASK-006 executes SEFAUD-REQ-005 by proving privacy, invalidation, unavailable states and enhanced or fallback equivalence.
7. P004-TASK-007 executes SEFAUD-REQ-005 by remediating all textual administrator feedback rows.
8. P004-TASK-008 executes SEFAUD-REQ-005 by adding deterministic focused regressions.
9. P004-TASK-009 executes SEFAUD-REQ-005 by collecting real portable client rendering, input and accessibility evidence.
10. P004-TASK-010 executes SEFAUD-REQ-005 by reconciling documentation, evidence, integration and phase closure.

**Required evidence**

- Stable surface and state inventory, Phase 003 feedback/action join, defects and invalidation ledger, server decision records, client receipts, and distinct targeted presentation evidence.
- Focused unit, codec, session, GameTest and dedicated-server proof, plus silent laptop evidence for every named residual client claim.
- Candidate-bound documentation, artifact and completion packet evidence using SEFAUD-IF-006, including exact owned-resource cleanup.

**Exit criteria**

- SEFAUD-AC-013, SEFAUD-AC-014 and SEFAUD-AC-015 pass for every owned matrix row at the integrated candidate.
- All ten work packages, tests, real-path rows, documentation, review, PR merge, resulting candidate verification, signed tag, and cleanup are evidenced.
- No known mandatory phase-owned defect remains.

## Inputs and Upstream Contracts

| Input or contract | Provider | Required state | Validation | Failure behavior |
|---|---|---|---|---|
| UI inventory | Phase 000 and P004-TASK-001 | unique screen, HUD, feedback and state owner | reconcile source, catalog, payload and documentation rows | stop affected row and repair inventory |
| action outcome | Phase 003 SEFAUD-IF-002 | canonical action, decision, mutation, feedback key and audit id | paired enhanced and fallback fixture assertions | stop semantic comparison, do not invent UI behavior |
| capture | Phase 003 SEFAUD-IF-004 | authorized bounded schema version 2 capture | enable status disable and schema self test | keep capture-dependent proof open |
| persistence result | Phase 002 SEFAUD-IF-003 | actual durable outcome and recovery state when UI action writes | join receipt and server effect | show truthful partial or recovery state, block unsafe retry |

## Outputs and Downstream Contracts

| Output or contract | Consumer | Guaranteed state | Compatibility or versioning | Evidence |
|---|---|---|---|---|
| UI ledger | Phase 005 and Phase 006 | every owned surface state has result, fidelity, artifact and invalidation | stable identifiers and candidate fingerprints | matrix reconciliation |
| presentation boundary | Phase 005 | client receipt and observed presentation remain separate from server decision | SEFAUD-IF-002 and existing wire identifiers | paired protocol and client records |
| support procedure | Phase 007 and operators | bounded capture and safe reproduction steps | SEFAUD-IF-004 schema version 2 | documentation replay |
| completion packet | Phase 005 | closed only with merged candidate, signed tag and cleanup | SEFAUD-IF-006 | packet validator and postmerge proof |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
|---|---|---|---|---|---|---|
| P004-TASK-001 | SEFAUD-REQ-005 | Enumerate every screen, HUD, overlay, fallback, feedback class and state including loading empty validation denial stale partial recovery success unavailable and closed. | Phase 000 inventory, Phase 003 task010 capture and action matrix. | Stable UI ledger and invalidation graph. | UniversalGuiCatalog, client screens, SefClientEvents, message owners. | Inject missing or duplicate surface and state rows, then reconcile source and generated inventories. |
| P004-TASK-002 | SEFAUD-REQ-005 | Establish and repair shared hierarchy, contrast, localization, panel bounds, wrapping, paging, safe region, background, focus, narration, tooltip and resize rules. | Task001 and shared primitives. | Design-system ledger and shared primitive repairs. | SefScreen, SefScreenBackground, SefVanillaTheme, vanilla widgets. | Bounds tests and targeted capture at scales 1 through 4, 854 by 480, 1280 by 720, narrow wide and live resize. |
| P004-TASK-003 | SEFAUD-REQ-005 | Audit panels, workflow fields and variants, preview and confirmation, pickers, search, pagination, modded items, native tooltip, control editor and InvSee. | Tasks001 and 002, server projection, SEFAUD-IF-002. | Per-state screen results and repairs. | SefPanelScreen, SefWorkflowScreen, SefControlEditorScreen, player suggestion item pickers, SefInvSeeScreen. | Actual route, mouse and keyboard, focus, error, stale selection, server receipt and mutation proof. |
| P004-TASK-004 | SEFAUD-REQ-005 | Audit Fancy Tags gallery, manager, editor, local preview, upload, transfer, cache, glyph, nameplate, HUD and missing texture states. | Tasks001 and 002, server policy and session revision. | Provenance-safe Fancy Tags ledger and repairs. | FancyTagStudioScreen, local overlay, world renderer, glyph bridge, client cache. | Gallery to local preview to publish or reject, reconnect, stale lease, missing texture, denied upload and safe unsaved close. |
| P004-TASK-005 | SEFAUD-REQ-005 | Audit pause entry, dashboard, HUD tiles, progress, overlays, disguise-facing state, reduced motion, safe regions and cleanup. | Tasks001 and 002, client events and HUD deltas. | HUD coexistence and lifecycle cleanup ledger. | SefClientEvents, HUD contracts, local overlay. | Pause insertion, combined overlays, resize, overflow, revocation, delta replay, disconnect and server-switch proof. |
| P004-TASK-006 | SEFAUD-REQ-005 | Close omission of private data, permission and module revocation, stale revision, disconnect, dimension change, unsupported protocol, enhanced disabled, no SEF fallback, exact matching enhanced and incompatible protocol states. | Tasks003 through 005, SEFAUD-IF-002, SEFAUD-IF-004. | Zero mutation authority matrix and paired fallback ledger. | SefGuiServer, SefSessionManager, PanelActionValidator, ClientProtocolState, codecs. | Forged replay, hidden identity, target swap, stale selection, revoke before click, module disable, reconnect and unavailable-family negative tests. |
| P004-TASK-007 | SEFAUD-REQ-005 | Audit every Phase 003 feedback row for header, labels, values, counts, paging, success, no change, validation, denial, partial, degraded, recovery, unavailable and fatal meaning. | Task001 and Phase 003 matrix. | Feedback hierarchy and redaction ledger. | KernelCommandExecutor, TextFormatter, MessageService, command registrars. | Source-class feedback tests, long locale and value, hidden data, console width, enhanced and fallback semantic comparison. |
| P004-TASK-008 | SEFAUD-REQ-005 | Add deterministic layout, screen inventory, workflow, payload, session, projection, focus, narration metadata, tooltip, performance and invalidation tests. | Tasks002 through 007. | Maintained regression suite and row fixtures. | GUI, protocol, command, Fancy Tags and applicable GameTest source sets. | Tests fail for clipping, missing ownership, unsafe projection, stale mutation, parity loss, unbounded work or client class leakage. |
| P004-TASK-009 | SEFAUD-REQ-005 | Run the complete real portable client ledger for rendering, actual input, dynamic states, narration, feedback and fallback. | Tasks001 through 008, EXT-001 and silent split-host fixture. | Sanitized screenshots, recordings, narration capture and joined server-client ledger. | Matching enhanced client, no SEF fallback client, GUI disabled, incompatible protocol behavior. | Server readiness and actual join on both hosts, then named client claims only; server console never substitutes for client proof. |
| P004-TASK-010 | SEFAUD-REQ-005 | Reconcile repairs, tests, docs, generated references, diagnostics, artifact boundaries, packet, review, merge and signed tag. | All prior tasks, SEFAUD-IF-006, EXT-001, EXT-002 comparison. | Integrated completion packet and Phase 005 handoff. | Documentation, build outputs, artifact and repository workflow. | Focused suite after last change, resulting candidate verification, signed phase tag, exact cleanup and evidence readability. |

Task ordering is strict because a shared primitive or protocol repair invalidates dependent captures. Independent source inventory and deterministic unit work may run together after Task001, but no visual claim closes before its matching server result, client receipt and presentation evidence agree.

## Architecture and Implementation Boundaries

Server code owns identity, visibility, filtered pages, permissions, action availability, confirmation, revisions and every mutation. Client code owns rendering, navigation, local preferences and bounded unsent drafts only. Server decision, client receipt and rendered state are three distinct records. A server console can prove server decision and fixture state, never pixels, input, focus, narration or rendering.

Layouts must derive from current GUI-space dimensions. Required controls may wrap, page or scroll with keyboard equivalence, but may not clip, overlap, require wheel-only access, use color as sole meaning, or hide an authorized full value without accessible disclosure. Focus follows deterministic semantic order and restores only a safe current control. Escape and Back close or return once, retain only permitted local drafts, and cannot leave a server session alive.

All sensitive rows are omitted server-side. Unknown and unauthorized identities share safe meaning where a distinction leaks vanish or membership. Suggestions and searches operate only on authorized projection. Any policy, target, module, session or revision change refreshes or rejects before mutation. Enhanced, enhanced-disabled, no-SEF fallback and incompatible-protocol connections retain documented command access and equivalent domain outcome without sending negotiated GUI payloads to incompatible peers.

Client render, input, narration and tick paths stay client-thread confined and do not block on filesystem or network work. Background cache and texture work publishes immutable validated results. Bounded pages, coalesced refresh, one tooltip, bounded texture work and revision checks prevent stale data from overwriting newer state. Fancy Tags local work is visibly local; publication, provenance, authorization and audit remain server-owned.

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
|---|---|---|---|---|
| UI004-RISK-001 fixed layout clips at scale or resize | bounds and focus reachability fixture at each viewport | control remains reachable with hierarchy intact | repair shared or local layout then invalidate dependent captures | named surface capture and bounds test |
| UI004-RISK-002 stale or revoked selection mutates | server expected and observed revision plus decision event | reject before mutation and clear or refresh presentation | reproject safe state and compare zero mutation | click after revision, permission or module change |
| UI004-RISK-003 enhanced and fallback diverge | paired action id, server outcome, audit id and meaning | same authority and durable result, presentation may differ | repair route without altering policy | paired fixture with matching inputs |
| UI004-RISK-004 Fancy Tags local work appears published | local provenance badge and server receipt mismatch | label local state and deny unapproved publication | discard stale cache or restore validated server projection | missing texture, stale lease and denied transfer fixture |
| UI004-RISK-005 capture or audio isolation fails | capture counters, mute readback or stream identity missing | stop owned client or capture and leave exact row open | remove owned resources, repair fixture and rerun | timeout, stream recreation and cleanup assertion |

Every fixture specifies the authentic entry point, synthetic identity, expected state, bounded wait and failure oracle. UI polling is not evidence: wait only for the named server-ready marker, client join confirmation, revision or capture event. Timeout returns a failed or open row with diagnostic fields and owns teardown; it never turns into a synthetic pass.

## Diagnostics and Debugging

**Requirement IDs:** SEFAUD-REQ-005  
**Task IDs:** P004-TASK-001, P004-TASK-002, P004-TASK-003, P004-TASK-004, P004-TASK-005, P004-TASK-006, P004-TASK-007, P004-TASK-008, P004-TASK-009, P004-TASK-010  
**Controls:** Use Phase 003 SEFAUD-IF-004 only. Verify the live dispatcher before use, then use `sef debug on gui component:<registered-id> 60 1000`, `sef debug status`, and `sef debug off` from the owned server console. Player invocation requires current explicit debug permission and remains default denied. Unknown category, invalid target, busy capture and unauthorized request reject without capture.  
**Signals:** Add the ten bounded typed Phase 004 signal assertions in the table below to the unchanged shared schema version 2 event and CaptureStatus contract. They distinguish server decision, client receipt, rendering and cleanup without raw private content.  
**Collection procedure:** The numbered local runbook below binds a single row to a single candidate, bounded capture and exact cleanup.  
**Headless verification:** On node-1 inspect the configured task graph first and run only no-GUI unit, codec, server GameTest and dedicated-server classloading paths. They prove layout logic, server projection and authority, not client rendering or interaction.  
**Client verification:** Named residual claims are screen pixels, actual mouse and keyboard input, focus, narration, tooltip, resize, HUD coexistence, Fancy Tags rendering, receipt clearing and fallback presentation. Verify them only on the owner laptop against the private node-1 dedicated server.  
**Client audio isolation:** Before launch verify the owner laptop host, desktop session and discrete GPU capability, create the isolated test instance, set its master audio to zero and read the setting back. After the owned window appears, use hyprctl clients -j to bind its exact address class title and PID, correlate only the owned process tree to its PipeWire or PulseAudio stream, verify the actual discrete renderer, mute that stream with wpctl or pactl, and verify muted state before connection acceptance actions. Recheck resource reload, device change, reconnect and stream recreation, immediately mute each replacement stream. Stop the owned client if identity, renderer or mute cannot be proven. Cleanup stops watcher and client, verifies stream exit, and removes only owned routing and instance state. Never mute a default sink, system audio, microphone, unrelated application or personal client.  
**Budgets and privacy:** Capture is default off. One active server capture uses 60 seconds default and 300 seconds maximum, 1000 events default and 10000 maximum, 4 MiB output, 100 accepted events per second, 20 per tick and at most 1 ms aggregate collection per tick. Disabled capture allocates no event and performs no I O. Redact identities, messages, addresses, arguments, artwork bytes and absolute paths.  
**Regression and support:** Test enable status off and idempotent off, authorization, missing target, timeout, reload, disconnect, sink failure, saturation, reset and absent target. Update the verified operator capture procedure only after delivery, with sanitized packet and replay evidence.

| Signal | Source and unit | Expected observation |
|---|---|---|
| capture_id schema_version sequence correlation_id | capture service opaque ids integer sequence | one bounded capture and monotonic join |
| screen_id presentation_state gui_scale logical_bounds framebuffer_bounds | client observation ids and pixels | required control bounds remain visible |
| focus_id input_method navigation_result | client input enum and stable id | mouse and keyboard reach equivalent safe result |
| narration_event announcement_kind tooltip_bounds | client accessibility event and pixels | current authorized state is announced once and tooltip stays visible |
| session_id expected_revision observed_revision | server session and projection integers | stale state rejects or refreshes before mutation |
| action_id decision reason_code durable_outcome | server action and persistence enums | exact allowed or denied result and one outcome |
| client_receipt_sequence protocol_state payload_bytes | client receipt and network count | negotiated client receives current bounded projection only |
| authorized_row_count filtered_row_count privacy_class | server projection counts and enum | hidden data is absent rather than disabled |
| render_duration_ms handler_duration_ms queue_depth dropped_events | client and server timing milliseconds counts | bounded work and truthful saturation counters |
| cleanup_state window_pid stream_muted artifact_hash | host teardown enum pid boolean hash | owned resources exit and evidence matches candidate |

1. Record candidate commit and artifact hashes, selected stable UI row, synthetic fixture, server and laptop runtime paths, owned process ids, capture location and cleanup targets. Inspect the Gradle graph. Configure exact disposable server eula true and read it back. Before any client launch, verify the owner laptop host, desktop session and discrete GPU capability, create the isolated client instance, set master audio to zero and read the setting back.
2. Start the no-GUI node-1 dedicated server, wait only for its readiness marker and resolve its authorized private endpoint. Launch the isolated matching laptop client without acceptance actions.
3. After the owned window appears, bind its exact Hyprland address class title and PID with hyprctl clients -j, verify the owned process tree and actual discrete renderer, correlate only that tree to its PipeWire or PulseAudio application stream, mute only that stream and verify the muted readback. If identity, renderer or mute cannot be proven, stop the owned client and leave only its affected row open. Keep the watcher active to remute and verify each stream recreation.
4. Only after step 3 passes, connect through supported controls or verify the automatic connection, then confirm the exact player joined the correct world on both server and client. Start the bounded gui capture, verify capture status, category, target, limits and relative output. For a negative control, first prove unauthorized enable rejects.
5. Run the row's authentic mouse or keyboard stimulus and its paired command fallback when required. Join server decision and audit to client receipt and targeted screenshot, short dynamic recording or inaudible narration capture. Execute the row-specific failure stimulus, bounded wait and recovery assertion. Use stale revision, revocation, disconnect, incompatible protocol, missing asset or absent target as applicable. Inspect all ten diagnostic fields; missing evidence leaves the claim open.
6. Disable capture, verify status and idempotent off, then repeat an action to prove capture stopped. Retain one sanitized minimum packet after redaction. Stop exact owned server, client, watcher and workers, verify processes and streams are gone, remove exact disposable outputs and report leftovers separately.

## Verification Matrix

| Requirement or task | Static or unit | Integration | Real workflow or runtime | Negative and recovery | Execution host and prerequisites | Evidence artifact |
|---|---|---|---|---|---|---|
| P004-TASK-001 SEFAUD-REQ-005 | inventory and duplicate tests | catalog and feedback joins | none needed | missing or unowned row rejects | node-1 no-GUI | matrix and invalidation ledger |
| P004-TASK-002 SEFAUD-REQ-005 | layout focus narration metadata | shared primitive harness | targeted scale resize capture | long locale and rapid rebuild | laptop only for visual claim | bounds and capture ledger |
| P004-TASK-003 SEFAUD-REQ-005 | workflow picker codec tests | server projection and receipt | panels workflows pickers InvSee | invalid field stale selection hidden target | split host joined fixture | paired state audit presentation record |
| P004-TASK-004 SEFAUD-REQ-005 | cache lease texture tests | server policy and transfer | Fancy Tags gallery editor overlay | missing texture stale lease denied publish | split host joined fixture | sanitized visual provenance ledger |
| P004-TASK-005 SEFAUD-REQ-005 | HUD safe region tests | delta and cleanup integration | pause HUD overlay coexistence | replay revoke disconnect switch | laptop graphics plus node-1 server | dynamic capture and teardown |
| P004-TASK-006 SEFAUD-REQ-005 | request guard session codec | enhanced fallback parity | matching enhanced, enhanced disabled, no SEF fallback, incompatible protocol | replay revoke module disable dimension change unavailable family | split host, no foreign client gate | zero mutation and paired audit ledger |
| P004-TASK-007 SEFAUD-REQ-005 | component redaction pagination | action feedback join | success denial recovery feedback | long values hidden data partial failure | node-1 then laptop named claim | feedback transcript and capture |
| P004-TASK-008 SEFAUD-REQ-005 | focused suite | applicable server GameTests | no graphical substitution | bounds payload stale and performance faults | node-1 confirmed headless | reports and fixture list |
| P004-TASK-009 SEFAUD-REQ-005 | capture schema checks | server client correlation | full silent input rendering narration matrix | failure and recovery states | node-1 server ready and laptop GPU audio join verified | screenshots recordings narration and manifest |
| P004-TASK-010 SEFAUD-REQ-005 | docs link diff artifact scan | build fallback classloading | postmerge smoke only after merge | stale evidence and cleanup failure | exact host rows used above | SEFAUD-IF-006 completion packet |

## Documentation, Operations, and Release

Document only verified behavior: surface inventory, fallback semantics, accessibility and focus behavior, stale or revoked recovery, Fancy Tags provenance, HUD ownership, debug capture, evidence redaction and cleanup. Keep Linux macOS and Windows support claims under the one portable client contract. Do not create separate macOS or Windows client acceptance requirements or claim foreign execution. Record unavailable client capability as its precise open row, never as broad support removal.

## Risks and Evidence Invalidation

| Risk ID and owner task | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
|---|---|---|---|---|---|
| RISK-006 P004-TASK-001 through P004-TASK-010 | actual input and separate server receipt presentation evidence | missing state dimension, clipped control or absent action join | repair exact owner and rerun dependent rows | changed surface, feedback or action captures | focused tests then named client row |
| RISK-009 P004-TASK-009 | isolated audio and exact owned teardown | unmatched window, GPU, stream or mute field | stop owned client and keep only row open | all client evidence from bad run | clean silent fixture rerun |
| UI004-RISK-002 P004-TASK-006 | revision bound server revalidation | revision decision mismatch or mutation | clear projection and repair guard | privacy, session and fallback rows | forge stale and revocation fixture |
| UI004-RISK-003 P004-TASK-006 | action and audit identity pairing | semantic mismatch | repair presentation route | enhanced and fallback rows | paired exact action rerun |

## Phase Completion Packet

The SEFAUD-IF-006 packet contains candidate and artifact identity, all ten task receipts, stable UI matrix, defect and invalidation ledger, unit and GameTest reports, server readiness and EULA readback, silent laptop window GPU stream mute and join evidence, all targeted client captures, server decisions, client receipts, presentation observations, enhanced disabled, no SEF fallback, exact matching enhanced and incompatible protocol records, all sixteen unavailable results, documentation and generated drift result, EXT-002 unchanged or reopened disposition, PR review and merge evidence, resulting candidate verification, signed tag, and cleanup proof.

It records only one portable Minecraft Java client fixture. It must state exact runtime directories, commands, expected and actual results, proof limits, owned processes, retained sanitized artifacts and verified deletion of scratch. It does not claim server-console assertions as client evidence and does not create a separate macOS or Windows client gate.

## Next Transition

After the Phase 004 pull request merges, verify the candidate contains its merge, perform required postmerge silent matching-client and fallback smoke, create and push the signed annotated tag, validate the completion packet and cleanup, then read Phase 005. Do not create a Phase 005 branch while any Phase 004 check, review, merge, tag, exact client gate or cleanup remains incomplete.
