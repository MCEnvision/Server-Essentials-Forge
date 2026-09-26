# Phase 004 Execution Plan

> **Plan ID:** PLAN-PHASE-004  
> **Phase ID:** SEF-PHASE-004  
> **Owner:** Network transport  
> **Classification:** MANDATORY  
> **Master plan:** [plan.md](../plan.md)  
> **Phase sequence:** 004 of 016

## Purpose and Ownership

This phase creates the isolated Java 25 Velocity companion and the authenticated proxy to backend transport. It owns only `SEF-REQ-009` and `SEF-REQ-011`. The master remains the authority for product scope, protocol signatures, adapter selection, security limits, host rules, and the global phase order. This blueprint details implementation and proof without treating the early feasibility observation as proof that the paired product works.

## Evidence-Based Entry State

| Evidence class | Area | Finding | Source or command | Freshness condition |
|---|---|---|---|---|
| OBSERVED | Proxy compatibility | Velocity 4.2.0 build 30, Ambassador 1.4.5, ProxyCompatibleForge 1.3.1 and the approved Forge client reset file 4657349 are pinned inputs. Their combined runtime compatibility requires the Phase 000 real round-trip receipt. | SRC-101, SRC-102, SRC-104, SRC-106, SRC-107, SRC-109, SRC-118, SRC-119; FIND-101 | Recheck every artifact identity, advisory status, license notice, and Phase 000 runtime receipt before use. |
| PROPOSED | Companion isolation | The companion is a small Java 25 plugin on `velocity-latest`, separate from the Forge Java 17 product and without world logic. | FIND-009, SEF-REQ-009 | Invalidated by a divergent product branch, toolchain, dependency graph, or artifact manifest. |
| PROPOSED | Direct bridge | Registered proxy and backend peers communicate over mutually authenticated TLS 1.3 sockets, not plugin messages or player carriers. | FIND-011, SEF-IF-005 | Invalidated by a signature, certificate mapping, frame grammar, or policy change. |
| PROPOSED | Backend prerequisites | Phase 003 must supply local command, policy, presentation, and teleport behavior before this phase; it does not supply durable network authority. | SEF-PHASE-003, SEF-IF-001 through SEF-IF-004 | This future entry receipt is required before execution and is invalidated until Phase 003 is merged, tagged, and its resulting Forge branch receipt is verified. |
| INFERRED | Empty-backend proof | A transport fixture can bind a registered backend endpoint and exchange allowed typed requests with zero players; player traffic is not a substitute. | SEF-AC-011, SRC-111 | Invalidated by any fixture using a player carrier, console relay, or unregistered peer. |

## Scope Boundaries

### Included Scope

- `SEF-REQ-009` creates the isolated Velocity build, plugin lifecycle, dependency and license inventory, protocol and common digest provenance, and paired artifact receipt.
- `SEF-REQ-011` creates the TLS 1.3 direct bridge, registered identity mapping, bounded typed envelope codec, version negotiation, queue and worker limits, replay and epoch fences, reconnect behavior, and safe key rotation.
- The phase consumes Phase 001 diagnostics and configuration contracts, Phase 002 command and rendering contracts, and Phase 003 backend interfaces only to carry their typed values. It does not make proxy world decisions.

### Explicit Exclusions

- `SEF-REQ-010` adapter handshake, forwarded UUID, signed chat, command argument, and player transfer feasibility belongs to Phase 000. This phase reuses its passed evidence but cannot claim it anew.
- `SEF-REQ-012`, `SEF-REQ-013`, and `SEF-REQ-018` authority persistence, compatibility admission, sessions, and pack relation enforcement begin in Phase 005. This bridge therefore does not make state authority or travel decisions.
- Moderation, vanish, lifecycle messages, homes, audit, RTP, graphical presentation proof, release publication, and production rollout remain assigned to later phases or excluded scope.
- No arbitrary console relay, player-origin bridge channel, Minecraft world code, Forge dependency, custom client protocol, embedded adapter implementation, private-key material in logs, or direct product-base integration is allowed.

## Phase Contract

### SEF-PHASE-004 — Build Paired Velocity Artifact and Authenticated Direct Transport

**Objective:** Produce an isolated Java 25 Velocity plugin and a provenance-matched, mutually authenticated direct bridge that carries bounded allowlisted typed requests while no backend has a connected player.  
**Owner:** Network transport  
**Dependencies:** SEF-PHASE-003, EXT-002, EXT-003, EXT-004, EXT-005  
**Canonical requirements:** SEF-REQ-009, SEF-REQ-011  
**Documentation and release impact:** Update the root README, `docs/README.md`, technical network and operations documentation, bridge configuration reference, certificate rotation runbook, supported artifact inventory, and post-merge wiki only after each approved integration. No public release is produced.  
**Next transition:** SEF-PHASE-005, persist network authority.  

**Entry criteria**

- Phase 003 is fully integrated and tagged on its applicable product branch; its resulting branch verification and cleanup receipt are present.
- EXT-002 confirms the EnVisione account and signing capability; EXT-003 through EXT-005 match the recorded immutable artifact identities, SHA-256 and SHA-512 evidence, license notices, and current security review.
- The Phase 000 feasibility matrix still records a passed matching pinned matrix, including an observed reset-capable client and continuous-session round trip, or an explicit blocker. A blocker stops this phase before implementation rather than inviting a custom handshake fallback.
- The Forge-owned common sources and registry for `SEF-IF-001` through `SEF-IF-004`, plus Phase 001 default-off diagnostic controls, compile on their supplying branches. `SEF-IF-005` is this phase's planned contract and is generated and implemented by P004-TASK-001 and P004-TASK-002.

**Implementation scope**

- Implement SEF-REQ-009, SEF-REQ-011 through the work packages and acceptance obligations below.

- `SEF-REQ-009`: seed `velocity-latest` exactly once from the latest approved `forge-1.20.1` commit, then create `envy/phase-004-velocity` from that seeded base and `envy/phase-004-forge` from the approved Forge base. Forge owns the common sources and registry; Velocity receives only its generated digest projection. Isolate the Java 25 toolchain and Velocity API, and emit a reproducible companion artifact with that common digest.
- `SEF-REQ-011`: implement only the signed schema's direct TLS transport and its boundary validation. A handler receives a request only after peer identity, recipient, protocol, expiry, epoch, sequence, type, bounded body, rate, and queue checks pass.

**Execution order**

1. `P004-TASK-001` establishes paired module lineage, shared protocol provenance, isolated build rules, and a startup inspection fixture. Trace: SEF-PHASE-004.
2. `P004-TASK-002` implements and verifies the security boundary before any future authority consumer can register a state-mutating handler. Trace: SEF-PHASE-004.
3. `P004-TASK-003` exercises empty-backend reconnect and rotation recovery, writes operations evidence, and completes paired branch integration gates. Trace: SEF-PHASE-004.

**Required evidence**

- Java 25 proxy startup against the pinned Velocity artifact, API compilation, JAR inspection showing no Forge or Minecraft world classes, and protocol/common digest equality with the approved Forge candidate.
- A no-player direct integration fixture that proves successful allowed traffic, independently observed endpoint identity, bounded workers and queues, and typed replies.
- A separate Forge standalone startup and local command regression with proxy mode disabled and no Velocity, Ambassador, ProxyCompatibleForge or client reset mod installed. Network transport code and loader metadata must not make those adapters hard runtime dependencies for this profile.
- A configured Ambassador-network fixture with the SEF Velocity companion omitted, followed by an unreachable-companion control. Backend console startup and an authorized admin-only status command must identify the companion and its verified artifact location or safe installation instructions, state that network operations are unavailable, and avoid claiming proven absence when only connectivity is unknown. A verified HTTPS release location may become a clickable admin-only download action; no invented or unverified URL is allowed. Standalone mode emits no prompt.
- Captured forged, replayed, stale-epoch, expired, malformed-length, oversized, unknown-type, unregistered-peer, saturation, reconnect, and revoked-key fixtures with zero handler mutation on rejection.
- The Forge PR, checks, merge commit, resulting `forge-1.20.1` verification, and signed tag; proxy retest against that exact approved Forge common digest; then the Velocity PR, checks, merge commit, resulting `velocity-latest` verification, and signed tag. Preserve source and binary hashes, SBOM, license inventory, sanitised diagnostics, documentation changes, and verified cleanup.

**Exit criteria**

- No known mandatory phase-owned defect remains. Every such defect must be fixed and its affected evidence rerun before phase closure.

- The Java 25 proxy starts on the pinned Velocity target, has no Forge or Minecraft world code, and refuses a local artifact or common-manifest mismatch at startup. It checks a remote peer digest during TLS handshake and rejects mismatch before handler admission.
- An allowed, registered TLS 1.3 peer exchanges an allowlisted typed frame with no players connected. Each prohibited frame class returns a safe typed rejection and reaches neither a handler nor a state mutation counter.
- The Forge artifact still starts and serves local features without any proxy adapter. If network mode is explicitly selected without its configured adapter, network admission fails visibly and safely instead of silently changing the selected authority mode.
- A configured network with no verified SEF Velocity companion prompts only operators to install or check the matching plugin on Velocity, using the approved artifact manifest. No client-facing prompt, automatic download, fabricated URL, secret-bearing diagnostic or network-ready status is allowed.
- Reconnect fences the old channel epoch while retaining its process boot; a process restart separately fences the old boot. Rotation permits at most two explicit keys during overlap, accepts the recorded successor only after the expected key identifier check, revokes and fences the predecessor, and exposes no certificate or secret material.
- The Forge PR is checked and merged first, its resulting `forge-1.20.1` commit is verified and signed-tagged, and the proxy is retested against that exact approved common digest. Only then is the Velocity PR checked and merged, its resulting `velocity-latest` commit verified and signed-tagged, with all test-owned resources absent. Phase 015 remains responsible for full product proof.

## Inputs and Upstream Contracts

| Input or contract | Provider | Required state | Validation | Failure behavior |
|---|---|---|---|---|
| Product phase lineage | SEF-PHASE-003 | Forge predecessor merged, tagged, and clean; no stacked branch | Inspect resulting product branch commit and signed tag before branch creation | Stop phase entry and keep the integration gate open. |
| Pin and adapter matrix | EXT-003, EXT-004, EXT-005 and SEF-PHASE-000 | Exact Velocity and adapter bytes, checksums, notices, and feasibility receipt match | Hash, source URL, license, advisory, and Phase 000 receipt comparison | Stop on drift or failed feasibility; retain a sanitized matrix, no custom compatibility path. |
| Forge-owned identity, diagnostics, policy, presentation | SEF-IF-001 through SEF-IF-004 | Exact registry version and typed result contract are available from upstream producers | Compile the Forge-owned common sources and generated digest projection | Stop before paired build or bridge acceptance. |
| Planned bridge signature | P004-TASK-001 and P004-TASK-002 | `SEF-IF-005` registry version 1 and schema version 1 are generated and implemented in this phase | Generated digest fixture and codec compatibility test | Reject remote peer with `PROTOCOL_MISMATCH` during handshake before handler admission; do not negotiate an unregistered major version. |

## Outputs and Downstream Contracts

| Output or contract | Consumer | Guaranteed state | Compatibility or versioning | Evidence |
|---|---|---|---|---|
| Java 25 Velocity companion artifact | Operators and SEF-PHASE-005 | Isolated plugin with recorded source commit, hashes, licenses, SBOM, and no Forge world dependency | Fixed pinned Velocity API and shared protocol digest; rebuild required for any digest change | Startup and JAR dependency inspection receipt. |
| SEF-IF-005 bridge implementation | SEF-PHASE-005, SEF-PHASE-006, SEF-PHASE-007, SEF-PHASE-008, SEF-PHASE-010, SEF-PHASE-014 | Authenticated, bounded, typed request/reply delivery independent of player presence | Major mismatch rejects; minor negotiation only for registered compatible fields; every peer maps to a registered server | Empty-backend, malformed-frame, replay, reconnect, and rotation receipts. |
| Connection fence and peer health events | Phase 005 authority and future operators | Boot, epoch, sequence, peer, outcome, reason, queue, and worker facts are safe diagnostic signals | No secret, body, certificate, or player display-name field; diagnostics remain default-off | Signal table and support runbook test. |

## Work Packages

| Task ID | Requirement IDs | Work | Inputs and dependencies | Outputs | Affected components or interfaces | Verification |
|---|---|---|---|---|---|---|
| `P004-TASK-001` | SEF-REQ-009 | Seed `velocity-latest` once from the latest approved `forge-1.20.1` commit, then create `envy/phase-004-velocity` from that seed and `envy/phase-004-forge` from the approved Forge base. Use the approved SefVelocity linked worktree only after the exact ownership and content checks below. Make Forge the canonical owner of common sources and registry, generate the Velocity digest projection, isolate Java 25, and record provenance. A local artifact or manifest digest mismatch refuses startup; remote mismatch is deferred to handshake validation. | SEF-PHASE-003 integration receipt, EXT-002, EXT-003, Forge-owned SEF-IF-001 through SEF-IF-004 | Reproducible paired artifacts, common digest manifest and Velocity digest projection, startup refusal path, and paired provenance record | Planned Forge common registry, planned Velocity module, plugin bootstrap, build and artifact manifests | Compile Forge-owned sources and Java 25 companion after task-graph inspection; start pinned proxy on node-1; inspect JAR class and dependency inventory; prove local matching digest starts and local mismatch refuses. |
| `P004-TASK-002` | SEF-REQ-011 | Deliver the inherited diagnostic adapter before any bridge real-path acceptance. Then build the registered-peer TLS 1.3 listener and connector, `SEF-IF-005` codec, allowlist dispatcher, and fixed limits of 256 KiB per frame, 128 queued messages per peer, 64 in-flight requests, five second request deadline, and 30 second envelope lifetime. Validate peer, remote digest, protocol, type, expiry, sender, recipient, boot, channel epoch, sequence, request identifier, body size, rate, and backpressure before any handler. | P004-TASK-001, SEF-IF-002, Phase 001 diagnostic controls | Diagnostic bridge adapter, direct boundary, peer registry mapping, at-most-two-key rotation transition, safe rejection taxonomy, and mutation counters | Planned proxy bridge runtime and Forge bridge adapter, SEF-IF-005 `Envelope`, `send`, and `rotatePeerKey` | Run an isolated node-1 no-player harness. Assert each forgery, remote digest mismatch, replay, stale epoch, stale boot, expiry, malformed length, 256 KiB plus one byte body, unknown type, unregistered server, rate, 128 plus one queued item, 64 plus one in-flight request, and deadline fixture records the exact reason and leaves handler and mutation counters at zero. |
| `P004-TASK-003` | SEF-REQ-009, SEF-REQ-011 | Prove reconnect and bounded credential rotation recovery, preserve standalone startup without external proxy adapters, add configured-network companion guidance, enforce documentation and support controls, and integrate sequentially. Merge Forge first, verify its resulting `forge-1.20.1` commit and tag, retest proxy against that exact approved common digest, then merge and tag Velocity. The task waits for both resulting branch checks and tags before closure. | P004-TASK-001, P004-TASK-002, EXT-002, SEF-IF-005 | Reconnect and rotation receipts, standalone startup and loader metadata receipt, missing-companion operator transcript, operator runbook, documentation updates, ordered branch PR evidence, resulting branch receipts, signed tags, and cleanup ledger | Planned bridge lifecycle, peer-key configuration, operator documentation, README and documentation index | Close one channel and reconnect it with a fresh channel epoch under unchanged process boot; replay its prior frame. Restart one owned process to prove stale boot rejection separately. Rotate only from expected identifier to one successor while no more than two explicit keys overlap, then revoke predecessor and fence its old channels. Start Forge with proxy mode disabled and no external proxy adapter JARs; then configure network mode without the companion, inspect operator-only installation guidance and safe refusal, and distinguish unreachable from confirmed missing. |

`P004-TASK-002` must precede every registration of a future state authority handler. A failed boundary fixture blocks `P004-TASK-003` integration. No task may start Phase 005 or another global phase before both branches complete the required merge, tag, verification, and cleanup sequence.

## Architecture and Implementation Boundaries

The planned companion is a Java 25 Velocity plugin with a separate toolchain and artifact lineage from Forge Java 17. `velocity-latest` is seeded once from the latest approved `forge-1.20.1` commit, then `envy/phase-004-velocity` begins from that seed while `envy/phase-004-forge` begins from the approved Forge base. Forge owns the common sources and registry; the Velocity module consumes its generated digest projection and cannot become a competing registry owner. The companion contains proxy lifecycle wiring, registered peer configuration, direct transport, typed codec, diagnostics adapter, and renderer adapter usage only where an operational response needs an existing safe `SEF-IF-004` representation. Forge owns all world operations. The bridge is a direct private socket, not Velocity plugin messaging, an administrative channel, a player carrier, or a console command tunnel.

P004-TASK-001 first checks `/mnt/hermes/projects/SefVelocity` with exact realpath, contents, active task ownership and `git worktree list --porcelain`. The owner explicitly permits this otherwise exceptional sibling path for the companion. Register it as a linked worktree of the same repository on the applicable `envy/phase-004-velocity` branch only if the unallocated directory is empty and unclaimed; refuse conflicts without deleting, moving or overwriting user content. Reuse a correctly registered existing worktree only after verifying its common Git directory, branch and active ownership. Keep the authoritative plan and research anchored in the verified Forge checkout `/mnt/hermes/projects/Sef/forge-1.20.1/1.1`, not its container. Exclude exact owned disposable children from Git, build discovery, source indexing and packaging before allocation. Preserve the owner directory and source worktree; clean only verified test or build children after the last consumer. This changes placement only, not Forge common ownership or product branch lineage.

At connection establishment, TLS 1.3 authenticates both sides. The validated peer certificate maps to exactly one configured registered `senderId`; each new connection receives a fresh `channelEpoch`, while `senderBoot` changes only when that peer process restarts. The codec admits only the exact `SEF-IF-005` envelope grammar. A local artifact or common-manifest mismatch refuses startup. A remote peer digest is checked at TLS handshake and rejects before handler admission. The codec then checks protocol major and registered minor compatibility, peer mapping, sender and recipient, expiry, type allowlist, bounded typed body, sequence and request uniqueness, and fixed limits of 256 KiB per frame, 128 queued messages per peer, 64 in-flight requests, five second request deadline, and 30 second envelope lifetime before dispatch. A handler receives immutable decoded values and must still recheck its actor, session, expected revision, and downstream authority policy. Response codes are the declared `AUTH_FAILED`, `REPLAY`, `STALE_EPOCH`, `EXPIRED`, `OVERSIZED`, `RATE_LIMIT`, `PROTOCOL_MISMATCH`, `UNREGISTERED_SERVER`, and `QUEUE_FULL`, never a raw exception or secret.

The bridge has no durable authority state. It keeps only bounded, disposable connection and anti-replay state sufficient for a live channel. Reconnect produces a fresh channel epoch that fences the old channel without changing process boot. A process restart produces a new boot and separately fences frames from the old boot. Rotation uses `rotatePeerKey(peerId, expectedKeyId, nextKeyId)` as the only transition and requires atomic configuration validation. At most two explicitly registered keys may overlap for the configured bounded transition; an expected-key mismatch retains the predecessor, while a confirmed successor causes predecessor revocation and fences every old channel. Private key files, full frame bodies, certificates, and raw configuration never enter diagnostics, packet captures, support uploads, or Git.

Concurrency is bounded by a fixed transport executor and bounded per-peer and global queues, with work rejected rather than unbounded buffering. The bridge preserves per-channel sequence decisions; it does not impose a global authority order, which belongs to Phase 005. Connection close cancels pending work by correlation ID, releases only transport-owned resources, and leaves future authority recovery to its owner. Proposed budgets are configuration acceptance limits to be measured and finalized in Phase 015, not observed performance claims.

## Failure, Recovery, and Edge Cases

| Scenario | Detection | Required behavior | Recovery or rollback | Regression proof |
|---|---|---|---|---|
| Forged client or unknown certificate claims a registered sender | TLS peer mapping plus `bridge.decision` reason `AUTH_FAILED` or `UNREGISTERED_SERVER` | Close or reject before envelope decode and keep handler count and mutation counter at zero | Preserve sanitized peer identifier and correlation only, revoke incorrect registration, then retest with a valid registered fixture | `P004-TASK-002` opens a no-player rogue TLS socket against the real listener; bounded wait ends on safe rejection and zero dispatch. |
| Captured valid frame is replayed | Sequence and request identifier cache reports `REPLAY` | First valid delivery may proceed once; duplicate never reaches handler | Keep live replay fence until channel teardown, then require fresh channel epoch on reconnect | `P004-TASK-002` resends the exact captured fixture frame and asserts one handler invocation, one typed replay rejection, and zero later mutation. |
| Old channel sends after reconnect | Channel epoch mismatch reports `STALE_EPOCH` under unchanged process boot | Reject old traffic even if certificate and sequence are otherwise valid | Fence old channel, make new peer health current, and rerun valid frame through new channel | `P004-TASK-003` drops the first connection, reconnects under the same boot with a fresh epoch, submits old frame, then asserts old rejection and new allowed reply within the fixture timeout. |
| Old process frame arrives after restart | Sender boot mismatch reports `STALE_EPOCH` | Reject old process traffic before handler admission | Fence old boot, establish fresh process boot and channel epoch, and rerun one valid frame | `P004-TASK-003` restarts one owned process, submits a prior-boot frame, then asserts rejection and one new-process allowed reply. |
| Length prefix, type, expiry, or body exceeds declared boundary | Decoder rejects before allocation or dispatcher emits `OVERSIZED`, `EXPIRED`, or `PROTOCOL_MISMATCH` | No partial decode, body retention, handler call, or connection-wide authority mutation | Close malformed connection where framing cannot recover; retain bounded signal only | `P004-TASK-002` sends truncated length, a 256 KiB plus one byte body, expired envelope, and unregistered type; each has a reason, zero dispatch, and bounded memory assertion. |
| Peer queue or inflight budget is exhausted | Queue depth reaches 128 or inflight count reaches 64 | Reply `QUEUE_FULL`, do not grow queue or inflight work, and leave accepted work bounded | Drain or cancel owned pending work after fixture completion; reconnect only after normal admission is available | `P004-TASK-002` holds the controlled handler, submits 128 accepted queued items then one additional item, separately fills 64 inflight requests then one additional request, and verifies recovery with a new allowed request. |
| Rotation request names wrong expected key or successor is unusable | Rotation receipt is failed and peer health records safe reason | Retain current key mapping and do not accept successor | Correct the peer configuration atomically, retry with expected identifier, permit at most two explicit keys during overlap, then revoke predecessor and fence old channels | `P004-TASK-003` asserts mismatch preserves old valid peer, success has exactly predecessor and successor during overlap, revoked predecessor returns `AUTH_FAILED`, and logs contain no key material. |

All waits are bounded by the fixture's configured startup, connection, request, shutdown, and cleanup timeouts. A timeout is evidence of an unresolved transport result, never a success or a substitute for Phase 005 recovery semantics.

## Diagnostics and Debugging

**Requirement IDs:** SEF-REQ-009, SEF-REQ-011, SEF-REQ-019  
**Task IDs:** P004-TASK-001, P004-TASK-002, P004-TASK-003  
**Controls:** Use inherited `sef debug on bridge registered-peer 60`, `sef debug status`, and `sef debug off <capture-id>` or `sef debug off all`, guarded by `sef.debug.manage`, with console operation, registered-peer or correlation target, and idempotent disable. It is default-off, defaults to 60 seconds, permits at most 300 seconds, 200 events per second, 10,000 events and 8 MiB per capture, 1,024 queued diagnostic events, and two simultaneous captures per process. Console use requires no joined player.  
**Signals:** Emit `bridge.decision`, `bridge.connection`, `bridge.queue`, and `bridge.rotation` with captureId, correlationId, side, boot, sequence, monotonicNanos, configGeneration, candidateDigest, peerId, senderBoot, channelEpoch, protocolMajor, protocolMinor, requestType, desired decision, actual decision, reason, queueDepth, queueCapacity, workMs, and worker state; omit bodies, certificates, addresses beyond approved peer label, keys, and display names.  
**Collection procedure:** Use the numbered local procedure below to create an owned no-player fixture, enable a bounded peer-target capture, perform exactly one stimulus at a time through the actual TLS listener, inspect typed result and handler counter, disable capture, redact evidence, and remove owned resources.  
**Headless verification:** P004-TASK-002 first delivers and self-tests the bridge diagnostic adapter through `sef debug on bridge registered-peer 60`, status, timeout, off, unavailable-output, absent-target, and permission-denied paths. Only after that acceptance does node-1 run the Java 25 proxy and Forge bridge harness as no-GUI processes in exact nested disposable runtimes. Assert ready listener, registered peer identity, zero-player state, expected typed reply or rejection, handler and mutation counts, fixed limits, process exit, and absence of owned runtime outputs. No client or renderer is launched.  
**Client verification:** This phase has no residual input, rendering, chat, or synchronization claim and launches no Minecraft client. Phase 000 owns adapter client feasibility; Phase 015 owns final paired client proof. A server-only receipt cannot close either client gate.  
**Client audio isolation:** Leave audio settings unchanged. Every required client uses the master Section 14 Trident instance lifecycle on the verified laptop, with explicit owned instance, runtime, window/PID, discrete renderer and joined-world evidence. No Trident command or client runs on node-1. Do not create mute watchers or require a playback stream or mute proof. Server-only checks need no client. Verify exact owned process exit and instance cleanup after the final consumer.
**Budgets and privacy:** Diagnostics remain default-off, transport-scoped, target-scoped, redacted, and independently sampled from mandatory unsampled audit. A full capture or output-unavailable condition fails visibly with `CAPTURE_LIMIT` or `OUTPUT_UNAVAILABLE`; it neither logs secrets nor blocks gameplay. Bridge configuration fixes 256 KiB per frame, 128 queued messages per peer, 64 in-flight requests, a five second request deadline, and a 30 second envelope lifetime. Phase 015 measures and may tune only within the approved contract; it cannot waive these limits or their one-over fixtures.  
**Regression and support:** Self-test enable, status, expiry, output failure, disable, and absent-target behavior; rerun every security fixture after codec or configuration changes; document minimal sanitized report fields, safe log location, redaction, and support escalation. Re-run affected bridge and branch artifact inspection after any pinned input, common digest, or listener change.  

| Signal | Source and unit | Expected observation |
|---|---|---|
| `bridge.connection` | Proxy and backend harness, one event per connection transition | Registered peer becomes ready with a fresh channel epoch; process boot changes only after restart; unregistered identity has no ready transition. |
| `bridge.decision` | Decoder and dispatcher, one event per envelope decision | Allowed request has correlation and handler receipt; rejected fixture has declared reason and zero handler or mutation count. |
| `bridge.queue` | Transport executor, items and milliseconds | Depth never exceeds configured capacity; saturation emits `QUEUE_FULL` and subsequent admitted request proves drain recovery. |
| `bridge.rotation` | Registered peer configuration, one event per transition | Expected key transition succeeds without key material; mismatch retains predecessor and revocation rejects it after successor confirmation. |
| `bridge.protocol` | Companion startup and bridge handshake, digest and version values | Local artifact or manifest mismatch refuses startup; remote digest or major mismatch rejects at handshake before handler admission. |

1. Register exact node-1 fixture paths beneath the verified project anchor, proxy and backend harness PIDs, temporary certificate and configuration directory, diagnostics output directory, and final evidence destination. Inspect Gradle task graphs to confirm every invoked task is headless. Record pre-existing paths and protect them.
2. Build matching candidates, verify hashes and common digest, create only fixture-scoped peer identities without recording private material, first self-test `sef debug on bridge registered-peer 60`, `sef debug status`, denied actor, absent target, output failure, expiry, and `sef debug off <capture-id>`, then enable the authorized bounded transport capture and check `status` reports the expected capture ID and expiry.
3. Start the no-GUI proxy and no-GUI backend harness, wait only until their explicit readiness signals, confirm zero connected players, then issue one normal frame and each named negative fixture through the real TLS listener. Inspect typed reply, correlated signals, handler/mutation counter, queue and worker values, and process state. Exercise 256 KiB plus one byte, 128 plus one queued item, 64 plus one in-flight request, five second deadline, and 30 second lifetime as distinct fixtures.
4. For reconnect and rotation, close only the owned channel and confirm fresh epoch with unchanged process boot before stale-channel assertion. Restart one owned process separately to test stale boot. Permit only predecessor and successor during rotation, then revoke predecessor and confirm old-channel fence. Do not use console relay or a player carrier to simulate transport.
5. Disable capture, read `status` to confirm it stopped, redact the required signal fields, preserve only stated sanitized evidence and requested artifact manifests, stop all owned harnesses and watchers, confirm process exit, remove exact fixture runtime, logs, traces, downloaded test inputs, bytecode, and scratch reports, and verify every registered owned path is absent. Report leftovers separately and leave the gate open until removed.

## Verification Matrix

| Requirement or task | Static or unit | Integration | Real workflow or runtime | Negative and recovery | Execution host and prerequisites | Evidence artifact |
|---|---|---|---|---|---|---|
| SEF-REQ-009, P004-TASK-001 | Compile Forge-owned common registry and Java 25 digest projection; deterministic local manifest mismatch assertion | Pair backend and proxy candidates; inspect API and JAR dependency graph | Start pinned Velocity proxy with no Forge world code and matching local digest | Local mismatch refuses startup; remote mismatch is rejected only at handshake before handler admission; changed pin invalidates rerun | node-1 after headless task-graph inspection, exact Java 25, pinned Velocity bytes, nested disposable runtime | Startup log, dependency inventory, common digest manifest, SHA-256 and SHA-512, SBOM, cleanup receipt |
| SEF-REQ-011, P004-TASK-002 | First self-test `sef debug` bridge adapter and fixed codec limits, allowlist, expiry, sequence, and rotation state | Actual mutually authenticated TLS listener plus registered no-player backend harness | Exchange allowed request and typed reply with zero players | Rogue peer, remote digest mismatch, replay, stale channel, stale boot, expired, malformed, 256 KiB plus one byte, unknown type, 128 plus one queued, 64 plus one in-flight, and deadline each show zero handler mutation | node-1, no GUI, private loopback or existing authorized private fixture endpoint, protected ephemeral identities | Correlated sanitized diagnostic packet, handler counter record, fixed-limit fixture results, shutdown and cleanup receipt |
| SEF-REQ-011, P004-TASK-003 | Configuration atomicity and at-most-two-key transition tests | Restart proxy and backend harness with retained valid config and a new process boot | Reconnect registered peer with fresh channel epoch, successor rotation, revoked predecessor rejection | Timeout is unresolved, wrong expected key retains predecessor, reconnect fences old channel, restart fences old boot | node-1, same candidate manifests, bounded owned processes and exact cleanup registration | Rotation and reconnect receipt, safe reason matrix, documentation review, resulting integration and tag receipts |
| Phase integration | Diff and documentation link checks; signature verification | Required checks and private independent review subject to the established review-capability availability rule | Forge PR and merge first, resulting `forge-1.20.1` verification and signed tag, proxy exact-digest retest, then Velocity PR, resulting `velocity-latest` verification and signed tag | Pending, failed, unsigned, unresolved, wrong order, or cleanup-incomplete gate blocks phase transition | GitHub only after local proofs; no direct product-base push or stacked branch | Ordered PR URLs and checks, merge commits, signed annotated tags, branch hashes, proxy retest receipt, wiki publication receipt after merge |

## Documentation, Operations, and Release

Document the implemented companion topology, Java 25 versus Forge Java 17 separation, exact artifact identity and license inventory, protocol/common digest mismatch response, registered peer configuration without example secrets, TLS trust and rotation procedure, bridge error meanings, bounded diagnostics controls, no-player bridge verification, and cleanup rules. Update `README.md`, `docs/README.md`, `DOCUMENTATION.md`, a focused network transport topic, and an operations or troubleshooting topic with implemented facts only. Publish corresponding wiki navigation after approved merges. Attach paired artifact checksums, source commit manifest, SPDX SBOM, and supported attestations to phase evidence; do not publish a release or deploy production.

## Risks and Evidence Invalidation

| Risk ID and owner task | Prevention | Detection | Recovery | Evidence invalidated | Reverification |
|---|---|---|---|---|---|
| SEF-RISK-003, P004-TASK-002 | TLS 1.3 mutual identity, registered mapping, typed allowlist, epoch and sequence fence, bounded body and queue | `bridge.decision` safe reason plus zero handler and mutation counters | Revoke incorrect peer, fence epoch, correct configuration atomically, rerun exact fixture | Any codec, certificate mapping, allowlist, protocol, queue, or handler registration change | Full no-player forgery, replay, oversize, unknown type, saturation, reconnect, and rotation matrix |
| SEF-RISK-001, P004-TASK-001 | Preserve exact pinned proxy and adapter inputs, isolated toolchain, and Phase 000 feasibility boundary | Startup, digest, API, dependency, and JAR inspection results | Stop on drift or failed matrix; record incompatibility, do not create custom handshake path | Velocity, adapter, Forge, Java, dependency, artifact digest, or feasibility receipt change | Rehash and rerun Phase 000-required matrix before dependent bridge integration |
| SEF-RISK-020, P004-TASK-001 and P004-TASK-003 | Pair manifests, source and binary hashes, sequential PR receipts, host-specific cleanup register | Candidate digest, branch source, process, and cleanup checks | Rebuild from approved resulting branches and remove only owned leftovers | Merge, tag, source, toolchain, host, runtime, or cleanup change | Re-run affected build, fixture, branch, tag, and cleanup gate |

## Phase Completion Packet

The closing packet contains the exact source commits for both phase branches, signed local commit verification, the checked Forge PR and merge receipt, resulting `forge-1.20.1` verification and signed tag, the proxy retest against that exact approved Forge common digest, then the checked Velocity PR and merge receipt, resulting `velocity-latest` verification and signed tag. It also retains Java 25 proxy and paired Forge artifact SHA-256 and SHA-512 values, source manifest, common/protocol digest, dependency and license inventory, SPDX SBOM, supported attestation status, and focused documentation diff.

It also contains the Phase 000 feasibility recheck, startup and JAR inspection, no-player listener readiness, allowed delivery, every security fixture result and correlation, handler and mutation counter assertions, reconnect and key-rotation recovery receipt, diagnostic control self-test, bounded wait outcomes, and an explicit statement that no client gate was run in this phase. Preserve only sanitized logs and the requested manifests. Before closure, stop and confirm exit of every owned proxy, backend harness, diagnostic watcher, and fixture service; remove their exact nested runtimes, temporary identities, downloaded test inputs, logs, traces, reports, database files, bytecode; verify absence on each used host. EULA is set and read back as `eula=true` before any disposable dedicated Forge server launch under existing authorization. Cleanup failure leaves the phase packet and phase exit open.

## Noncanonical Interface Projection

This derived machine-readable projection is copied exactly from the phase interface evidence. It supports drift checks and does not create an additional canonical contract.

```json
{
  "phaseId": "SEF-PHASE-004",
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
    },
    {
      "id": "SEF-IF-005",
      "signature": {
        "registryVersion": 1,
        "schemaVersion": 1,
        "asyncResultContract": "CompletionStage<Result<T>>. Result is typed success or failure with code, retryable, correlationId and safe message key. Supporting response records and nullability are defined by the master.",
        "name": "Authenticated bridge",
        "records": {
          "Envelope": {
            "protocolMajor": "u16",
            "protocolMinor": "u16",
            "senderId": "string",
            "recipientId": "string",
            "senderBoot": "UUID",
            "channelEpoch": "UUID",
            "sequence": "u64",
            "requestId": "UUID",
            "type": "allowlisted_enum",
            "actor": "Actor?",
            "session": "Session?",
            "expectedRevision": "u64?",
            "expires": "Instant",
            "body": "bounded_typed_bytes"
          }
        },
        "methods": [
          "send(envelope:Envelope) -> Result<TypedReply>",
          "rotatePeerKey(peerId:string, expectedKeyId:string, nextKeyId:string) -> Result<RotationReceipt>"
        ],
        "errors": [
          "AUTH_FAILED",
          "REPLAY",
          "STALE_EPOCH",
          "EXPIRED",
          "OVERSIZED",
          "RATE_LIMIT",
          "PROTOCOL_MISMATCH",
          "UNREGISTERED_SERVER",
          "QUEUE_FULL"
        ],
        "ownership": "TLS 1.3 mutually authenticated private direct socket; peer certificate maps to registered server; no player carrier or arbitrary command relay."
      },
      "acceptance_ids": [
        "SEF-AC-011"
      ]
    }
  ]
}
```

## Next Transition

After P004-TASK-001 through P004-TASK-003 exit criteria, Forge-first then exact-digest proxy retest then Velocity integration, both resulting branch checks, signed phase tags, and all cleanup evidence pass, advance only to `SEF-PHASE-005`. Its entry action is to consume the verified `SEF-IF-005` bridge and pair provenance, then implement the proxy-owned SQLite authority, sessions, ordered replicas, and compatibility admission. Do not begin that work while either Phase 004 integration or cleanup gate remains open.
