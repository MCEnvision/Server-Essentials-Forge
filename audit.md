# SEF Full Code Audit and Remediation Report

## Initial audit snapshot

This report audits the repository state on branch `envy/sef2_complete` as it existed on July 28, 2026. The table below records the initial audit baseline before the confirmed defects were repaired. The detailed findings preserve the original evidence, failure modes, repair instructions, and acceptance tests.

The audit covered the active repository, current untracked Java source, build configuration, manifests, resources, tests, workflows, and documentation. Ignored runtime directories, build output, caches, logs, and `SourceCodeOld` were excluded from source scope. Runtime output was used only as verification evidence.

| Measure | Result |
| --- | ---: |
| Active repository files inventoried | 600 |
| Java files | 491 |
| Main Java files | 372 |
| Test Java files | 119 |
| Main Java lines | 115,022 |
| Test Java lines | 14,472 |
| Markdown documents | 28 |
| Unit tests executed | 433 |
| Required GameTests executed | 29 |
| Packaged JAR entries inspected | 1,163 |

Every in scope file was opened or mechanically inspected. All expected text files were valid UTF 8, all JSON and TOML files parsed, all configured mixins resolved to source classes, and no unexpected null bytes or symbolic links were present in the repository scope. The audit also traced the initialization, storage, permission, command, GUI protocol, networking, moderation, vanish, chat, economy, teleport, recovery, automation, disguise, logging, and shutdown paths.

## Remediation verification snapshot

All twenty confirmed audit findings are repaired in the current worktree. The current verification baseline is:

| Measure | Result |
| --- | ---: |
| Confirmed findings repaired | 20 of 20 |
| Unit tests executed | 487 |
| Required GameTests executed | 38 |
| Catalog actions checked in GameTests | 694 |
| Shortcuts checked in GameTests | 315 |
| Representative parser variants checked in GameTests | 2,213 |
| Safe read only live routes executed in GameTests | 358 |
| Packaged JAR entries inspected | 1,193 |

The repaired source passes the Java 21 unit suite, required GameTests, generated reference checks, fallback compilation check, performance report, clean build, dedicated server startup and shutdown, headless client startup, and final JAR structure inspection. The client reached stable resource and texture loading with every SEF mixin applied. OpenAL could not open an audio device in the headless container, which is an environment limitation rather than a mod failure.

## Executive verdict

The twenty confirmed audit defects are closed. No confirmed high, medium, or low audit finding remains open in the current worktree.

This does not make the complete `sef2.md` product production ready. Sixteen planned Phase 13 runtime families remain deliberately unavailable, and the multiplayer, LuckPerms, GUI visual, InvSee, admission capacity, and disguise animation matrices still require controlled interactive environments. Those two release blockers are product scope and acceptance work, not unresolved instances of the twenty repaired defects.

| Severity | Count | Meaning |
| --- | ---: | --- |
| Critical | 0 | No confirmed unauthenticated remote code execution, direct credential exposure, or certain total world loss was found. |
| High | 0 open | Eight confirmed high findings were repaired and regression tested. |
| Medium | 0 open | Nine confirmed medium findings were repaired and regression tested. |
| Low | 0 open | Three confirmed low findings were repaired and regression tested. |
| Release blockers | 2 | Acceptance and quality gates remain incomplete independently of individual defects. |

## Remediation status

| Finding | Status | Implemented repair and regression evidence |
| --- | --- | --- |
| `SEF-AUD-001` | Closed | Server controls now durably prepare operations before side effects, carry stable operation identifiers, persist terminal receipts, block ambiguous retry, and reconcile incomplete operations. Repository and execution service fault tests cover stale revisions, write failures, duplicate requests, and recovery. |
| `SEF-AUD-002` | Closed | Offline actions use durable claim, execution, success, failure, and outcome unknown states. Execution no longer depends on the original actor being online, authority is constrained to the queued profile, duplicate delivery is idempotent, and restart recovery is tested. |
| `SEF-AUD-003` | Closed | Banned items, mutes, filters, warnings, MOTD, op bulletins, alternate accounts, announcements, and player profiles distinguish first run from damaged storage. They load into temporary state, validate complete documents, enter recovery on failure, and refuse unsafe writes. |
| `SEF-AUD-004` | Closed | Public player chat remains on the signed player chat path. Formatting is projected without converting the message into an unsigned system message. Signed chat regression coverage verifies the delivery contract. |
| `SEF-AUD-005` | Closed | `/ans` uses recipient bound opaque reply tokens with expiry, one recipient scope, one use behavior, and indistinguishable unavailable responses. Vanished identity, message content, UUIDs, and presence are not exposed. |
| `SEF-AUD-006` | Closed | Managed JSON reads enforce byte, depth, node, object member, array element, and string limits. Managed paths reject symbolic links throughout traversal and are revalidated at the open boundary. Export paths use the same ownership checks. |
| `SEF-AUD-007` | Closed | Jail and release use durable preparing transitions, safe destination validation, rollback or outcome unknown handling, event finalization ordering, and restart reconciliation. GameTests cover exact safe teleport destinations and state transitions. |
| `SEF-AUD-008` | Closed | Economy sign interactions reserve inventory and funds, wait for the final noncancelled event outcome, commit once, and compensate on cancellation or failure. Tests cover later cancellation and rollback. |
| `SEF-AUD-009` | Closed | Mute expiry uses absolute epoch timestamps, accepts bounded durations, rejects malformed records, migrates supported legacy values, and cannot be extended by process restart. |
| `SEF-AUD-010` | Closed | Atomic file publication flushes file content, performs the atomic replacement, and flushes the containing directory where supported. Failures retain recoverable state and are fault tested. |
| `SEF-AUD-011` | Closed | Disguise projection indexes viewer and subject state, sends only changed projections, and bounds work per synchronization cycle. Regression tests enforce operation and time budgets. |
| `SEF-AUD-012` | Closed | Performance diagnostics use cached incremental snapshots with a bounded refresh rate instead of synchronously scanning every entity per request. |
| `SEF-AUD-013` | Closed | Private chat content is excluded from broad logging and durable social state. Observation uses scoped authorization, redaction, bounded session retention, and metadata only security audit events. |
| `SEF-AUD-014` | Closed | Dead active entity context hooks were removed. Vanish selector and world behavior query authoritative state directly, reducing thread local and mixin ordering risk. |
| `SEF-AUD-015` | Closed | Packaged NeoForge dependency metadata is narrowed to the verified exact version range `[21.1.235]`. |
| `SEF-AUD-016` | Closed | Fancy Tag upload ownership and revision are validated before temporary upload removal. Failed authorization leaves the upload available to its owner. |
| `SEF-AUD-017` | Closed | Markdown escape parsing is single pass and preserves escaped delimiters, trailing escapes, nested styles, and literal backslashes. Dedicated parser tests cover the prior corruption cases. |
| `SEF-AUD-018` | Closed | Decisive exception paths now log bounded operation context and preserve the distinction between invalid input, unavailable dependencies, recovery state, and internal failure. |
| `SEF-AUD-019` | Closed | The sealed catalog, shortcuts, GUI descriptors, and live Brigadier dispatcher agree. Required GameTests inspect all 694 catalog actions and 315 shortcuts, compile 2,213 representative parser variants, and execute 358 safe read only live routes. |
| `SEF-AUD-020` | Closed | `/setworth` accepts namespaced resource locations such as `minecraft:stone`, validates the item and exact amount, and preserves the repository on invalid input. |

## Confirmed findings

### SEF-AUD-001, high, server control side effects occur before durable state transition

#### Evidence

`src/main/java/com/enviouse/sef/control/ServerControlExecutionService.java`, lines 106 through 187, executes the selected handler at lines 159 through 165. It calls `transitionExecuted` only afterward at lines 170 through 179.

`src/main/java/com/enviouse/sef/control/ServerControlRepository.java`, lines 126 through 178, performs repository writability and revision checks during the later transition.

#### Failure mode

The handler can change the live server and then fail to record that outcome. This happens if the repository becomes unwritable, its revision is stale, persistence fails, or the process stops between the handler and the state transition. The record can remain eligible for retry even though its effect already occurred.

The reverse inconsistency is also possible after a delayed persistence failure. Memory may report execution while the durable file still describes the prior state. Nonidempotent actions can then run twice after restart.

Handler exceptions are converted to a generic failure without recording the stack and operation context in the server log. This makes outcome ambiguity harder to reconcile.

#### Required repair

1. Define an operation state machine such as `DRAFT`, `VALIDATED`, `PREPARED`, `EXECUTING`, `EXECUTED`, `FAILED`, and `OUTCOME_UNKNOWN`.
2. Assign a stable operation UUID and idempotency key before any live side effect.
3. Validate permissions, revision, schema, and repository health before preparing the operation.
4. Persist and durably flush the `PREPARED` or `EXECUTING` claim before invoking a handler.
5. Pass the operation UUID to handlers and require handlers to be idempotent or to keep their own durable receipt.
6. Commit the terminal outcome after the side effect. If the commit fails, mark the operation `OUTCOME_UNKNOWN` and block automatic retry.
7. Add startup reconciliation for incomplete operations. Reversible operations need explicit compensation logic. Irreversible operations need an operator decision path.
8. Log the operation UUID, feature ID, record ID, revision, and exception. Do not log sensitive free form payload values.

#### Acceptance tests

1. Stop the process before handler execution, after handler execution, and before terminal commit.
2. Inject write failures at every repository transition.
3. Execute two requests with the same operation UUID and prove the effect occurs once.
4. Execute with a stale revision and prove no handler runs.
5. Restart with every incomplete state and verify deterministic reconciliation.

### SEF-AUD-002, high, offline queued actions are not exactly once and are not truly offline

#### Evidence

`src/main/java/com/enviouse/sef/gui/protocol/OfflineActionService.java`, lines 38 through 50, requires both the target and the original actor to be online. The target login path silently skips the queued action while the actor is offline.

The same service dispatches the action at lines 75 through 127 and resolves the in memory record only after dispatch.

`src/main/java/com/enviouse/sef/gui/protocol/OfflineActionRepository.java`, lines 157 through 175, returns pending actions without a durable claim. Its resolution path at lines 177 through 207 changes in memory state and marks it dirty.

The GUI describes these operations as queued for login, including the workflow in `src/main/java/com/enviouse/sef/gui/protocol/GuiWorkflowService.java`, around lines 490 through 511.

#### Failure mode

A target can log in repeatedly without execution if the original staff member is offline. The action can eventually expire even though the documented trigger occurred.

If the action succeeds and the process stops before resolution is durably written, it executes again after restart. This is dangerous for inventory, economy, punishment, teleport, or other nonidempotent actions.

#### Required repair

1. Give every queued action a stable operation UUID.
2. Add durable `PENDING`, `CLAIMED`, `EXECUTING`, `SUCCEEDED`, `FAILED`, `CANCELED`, and `OUTCOME_UNKNOWN` states.
3. Claim and flush the action before dispatch. Use a bounded lease only when crash recovery can safely determine whether the effect happened.
4. Decide the authorization contract explicitly. A recommended contract is authorization at queue time, immutable actor attribution, an optional revocation check at execution, and execution through a restricted server owned profile. Do not require the actor player object to be online.
5. Make dispatchers accept the operation UUID and behave idempotently.
6. Commit and flush the terminal result before removing an action from the active queue.
7. Store a durable target notification and operator visible failure reason.
8. Keep expired and failed records in bounded history rather than silently discarding them.

#### Acceptance tests

1. Queue while the actor is online, log the actor out, then log the target in.
2. Stop the process immediately before dispatch, immediately after dispatch, and during terminal persistence.
3. Deliver the same repository record on two ticks and prove one effect.
4. Revoke the actor permission before execution and verify the chosen policy.
5. Exercise target disconnect, expiry, restart, and invalid target data.

### SEF-AUD-003, high, legacy enforcement storage fails open after damaged data

#### Evidence

`src/main/java/com/enviouse/sef/banned/BannedItemsManager.java`, lines 79 through 145, clears live state before loading. A missing read result causes an empty save. Parsing writes directly into live collections, and failure does not place the manager into a read only recovery state. Invalid bypass UUID values are silently ignored at lines 120 through 124.

`src/main/java/com/enviouse/sef/mute/MuteManager.java`, lines 113 through 137, clears live state and writes an empty store when no document is returned. Loaded fields are not comprehensively bounded or validated.

`src/main/java/com/enviouse/sef/filter/FilterDataStore.java`, lines 34 through 56, clears the active filter list and treats an absent read result as empty writable state.

`src/main/java/com/enviouse/sef/storage/StorageService.java`, lines 57 through 148, returns an empty optional for a missing document, quarantined document, unsupported version, migration preparation failure, and other read failures. Legacy callers therefore cannot distinguish first run from recovery.

#### Failure mode

A corrupt, oversized, malformed, or unsupported file can silently disable banned item, mute, or chat filter enforcement. A later save can make the empty state canonical and conceal the original failure behind quarantine or backup files.

Partially parsed collections can also expose a mixed state containing only the records read before an exception.

#### Required repair

1. Migrate every legacy manager to the `StorageRepository` lifecycle with explicit `READY`, `RECOVERY`, `UNSUPPORTED`, `ERROR`, and `CLOSED` states.
2. Parse into a temporary immutable snapshot. Validate the full document before publishing it to live state.
3. Treat a missing file as first run only when the canonical path truly did not exist.
4. Treat an existing unreadable, malformed, quarantined, or unsupported file as recovery. Reject mutations until an explicit restore, migrate, reset, or operator acknowledgement completes.
5. Enforcement data must fail safely. Existing loaded state should remain active when a reload fails. On cold startup, the server should clearly block the affected feature or startup according to policy instead of silently disabling enforcement.
6. Remove automatic empty saves from failed read paths.
7. Bound collection sizes, string sizes, numeric values, UUIDs, identifiers, and nested structures.
8. Apply the same review to warn, alternate account, bulletin, and other managers still using ambiguous `StorageService.read` results.

#### Acceptance tests

1. Test missing, empty, corrupt, oversized, unsupported, and partially malformed files separately.
2. Prove the previous live snapshot survives a failed reload.
3. Prove recovery state rejects normal writes.
4. Prove restore and reset require explicit operator actions and leave audit evidence.
5. Prove a single invalid record cannot publish a partial collection.

### SEF-AUD-004, high, public chat is converted from signed player chat to system messages

#### Evidence

`src/main/java/com/enviouse/sef/events/ChatEventHandler.java`, lines 72 through 83 and 195 through 269, handles public chat at highest priority, cancels the original event at line 239, formats it, and manually distributes it later.

`src/main/java/com/enviouse/sef/events/ServerMessageEvent.java`, lines 16 through 20 and 38 through 54, distributes the replacement through the common message helper and sends it with `sendSystemMessage`.

#### Failure mode

The original `PlayerChatMessage` signature and trust chain are discarded. Player authored content is presented through the server message channel. This undermines vanilla authenticity indicators and message reporting expectations. It can also create incompatibility with moderation, proxy, chat signing, and client mods that depend on the signed chat pipeline.

#### Required repair

1. Preserve the event's original `PlayerChatMessage` and signed body.
2. Use the supported Minecraft 1.21.1 and NeoForge signed delivery path for recipient filtering and `ChatType.Bound` presentation.
3. Implement tag and display decoration without replacing the signed message body.
4. If a feature changes user supplied content, mark the result clearly as modified or unsigned according to the protocol. Make that behavior explicit and configurable.
5. Keep vanish filtering at recipient selection, not by converting the message into a system component.
6. Do not use `sendSystemMessage` for ordinary player chat.

#### Acceptance tests

1. Send chat between two vanilla clients and inspect the signature and reportability state.
2. Repeat with enhanced clients, tags, markdown, nicknames, and vanish.
3. Verify a hidden player is omitted only from unauthorized recipients.
4. Verify chat chain continuity across consecutive messages and reconnects.
5. Verify other chat listeners receive the expected signed event exactly once.

### SEF-AUD-005, high, `/ans` exposes vanished chat identity, content, and presence

#### Evidence

`src/main/java/com/enviouse/sef/chat/ChatMessageManager.java`, lines 15 through 37, assigns sequential global message IDs and retains the sender name and message. Records do not contain an authorized audience, visibility decision, expiry, or recipient bound capability.

`src/main/java/com/enviouse/sef/events/ChatEventHandler.java`, lines 243 through 268, records the formatted message before vanish aware distribution. Authorized viewers receive a clickable `/ans` ID, but the ID itself is globally guessable.

`src/main/java/com/enviouse/sef/chat/ChatReplyHandler.java`, lines 51 through 98, looks up an ID, resolves the sender directly from the server player list, and builds a reply summary from the original message and formatted sender name. It returns a different response when the hidden sender is offline.

`src/main/java/com/enviouse/sef/config/PermissionsHandler.java`, lines 69 through 70, grants the answer permission by default.

#### Failure mode

An ordinary player can enumerate recent sequential IDs. A guessed ID can reveal the formatted identity and message summary of a vanished sender that the player was not allowed to see. The distinct online and offline paths also disclose the hidden player's presence.

The retained collection holds up to 10,000 full chat records with no time based expiry.

#### Required repair

1. Replace sequential public IDs with cryptographically opaque, short lived reply tokens.
2. Bind each token to the exact recipient UUID that received the original message.
3. Store the immutable visibility context or authorized audience with the record.
4. Reauthorize the sender through `VanishUtil.playerAllowedToSeeOther` at reply time.
5. Do not reveal the sender name, message body, or online state before authorization.
6. Return the same unavailable response for unknown, expired, unauthorized, offline, and hidden targets.
7. Apply a short time to live and a strict per recipient capacity.
8. Clear tokens on permission context changes where required.

#### Acceptance tests

1. Enumerate valid and invalid tokens as an unauthorized player.
2. Test every vanish level and permission combination.
3. Disconnect and reconnect the hidden sender.
4. Transfer a token to a different player and prove it fails without disclosure.
5. Expire tokens and verify a uniform response.

### SEF-AUD-006, high, storage accepts stack exhausting JSON and follows managed file symbolic links

#### Evidence

`src/main/java/com/enviouse/sef/storage/StorageService.java`, lines 57 through 148, checks file size and reads the path in separate operations. These operations follow symbolic links. The method calls Gson `deepCopy` at lines 95, 116, and 143 and catches `Exception`, not `StackOverflowError`.

The recursive unknown field merger at lines 267 through 305 also has no depth limit.

The export path at lines 209 through 240 calls `Files.isRegularFile`, `Files.size`, and `Files.readAllBytes` without `NOFOLLOW_LINKS`. A symbolic link placed at a known managed file path can therefore copy an external file into a storage export if its status is already registered.

The audit executed Gson 2.10.1 against deeply nested valid JSON. Parsing remained iterative at 50,000 levels, but `JsonElement.deepCopy` threw `StackOverflowError` at 5,000 levels. A payload of this shape can be much smaller than the 16 MiB document limit.

#### Failure mode

A locally writable or compromised managed document can terminate the server startup or storage path with `StackOverflowError`. This bypasses quarantine because the failure is an `Error`.

Path replacement between size check and read can bypass the intended byte limit. Symbolic links at fixed managed paths can cross the configured storage boundary and expose external file contents through an export.

#### Required repair

1. Reject symbolic links and nonregular files using `NOFOLLOW_LINKS` for every managed file and parent directory.
2. Open a single file handle and enforce the byte limit while reading from that handle. Do not use separate stat and unbounded read operations.
3. Add streaming JSON validation with maximum nesting depth, maximum members, maximum array length, and maximum string length.
4. Replace recursive deep copy and merge operations with bounded iterative traversal.
5. Preserve the original file and enter recovery state when structural limits are exceeded.
6. For export, revalidate the source through the same no follow policy and copy from a bounded open handle.
7. Verify the export destination and every created parent remain inside the configured root.
8. Do not rely on catching broad `Error` values as the primary defense. Prevent stack exhaustion before materializing or copying the tree.

#### Acceptance tests

1. Test valid JSON at the maximum permitted depth and one level beyond it.
2. Test a 5,000 level payload and prove it enters recovery without stopping the server.
3. Replace a managed file with a symbolic link to an external secret fixture.
4. Grow or replace a file after validation and prove the read remains bounded.
5. Test malformed UTF 8, FIFO, directory, device, and other nonregular paths.

### SEF-AUD-007, high, jail persistence and teleport order can strand players

#### Evidence

`src/main/java/com/enviouse/sef/moderation/ModerationCommands.java`, lines 603 through 650, teleports a player into jail at lines 629 through 639 and records the sentence afterward at lines 640 through 646.

The unjail path at lines 653 through 680 releases the sentence at lines 663 through 667 and then attempts the teleport at lines 668 through 676. The teleport result is not used to determine whether release succeeded.

`src/main/java/com/enviouse/sef/moderation/ModerationEvents.java`, lines 114 through 142, removes expired sentences before release. It returns without restoring the sentence when the player is offline, the location is missing, or teleport fails.

#### Failure mode

A player can be moved into jail with no durable sentence if recording fails or the process stops after teleport. An unjail or expiry can delete the sentence while leaving the player at the jail location. An offline player whose sentence expires can later reconnect in jail with no release record to process.

#### Required repair

1. Model `JAILING`, `ACTIVE`, `RELEASE_PENDING`, `RELEASING`, `RELEASED`, and `OUTCOME_UNKNOWN`.
2. Persist and flush the intended jail sentence and original return location before teleporting.
3. Activate the sentence only after a successful teleport.
4. Mark release pending before the release teleport. Remove the active sentence only after confirmed success.
5. Keep offline releases pending and enforce them on login.
6. Handle every `TeleportResult`, including missing dimensions, unloaded destinations, disconnects, and policy denial.
7. Reconcile incomplete states on startup.

#### Acceptance tests

1. Inject repository failure before and after each teleport.
2. Expire a sentence while the player is offline.
3. Remove or unload the return dimension.
4. Disconnect during jail and release transitions.
5. Restart in every intermediate state and verify recovery.

### SEF-AUD-008, medium, economy sign effects can survive later event cancellation

#### Evidence

`src/main/java/com/enviouse/sef/economy/EconomySignHandler.java`, lines 53 through 90, records placement ownership and removes sign records from place, break, and explosion events without a final cancellation check.

The right click handler at lines 92 through 133 runs at `EventPriority.LOW`. It checks the current cancellation state, cancels the event, and can execute an economic transaction before a later `LOWEST` handler makes its final decision.

`commandWithCharge`, lines 440 through 460, refunds only for `CommandSyntaxException` or a nonpositive command result. A runtime exception escaping command execution can leave the charge applied.

#### Failure mode

Another protection mod can cancel a placement, break, explosion, or interaction after SEF has mutated ownership or executed an economic action. The world and SEF repository can disagree. A linked command runtime failure can retain a payment for an action that did not finish.

#### Required repair

1. Move destructive world event bookkeeping to the final applicable event phase and set the subscriber to ignore canceled events.
2. Revalidate the actual block entity and fingerprint after the event, preferably on the next server task when the platform does not expose a final commit hook.
3. Treat right click authorization and economic mutation as one transaction after final policy approval.
4. Replace ad hoc charge and refund logic with the existing reservation interface. Use `try` and `finally` so every noncommitted reservation refunds.
5. Make transaction IDs stable across retries.

#### Acceptance tests

1. Register a test listener that cancels each event at `LOWEST`.
2. Test explosion list removal and full explosion cancellation.
3. Throw a runtime exception from a linked command after withdrawal.
4. Verify block state, sign ownership, inventory, and balance remain consistent.

### SEF-AUD-009, medium, mute duration uses uptime and loaded records are weakly validated

#### Evidence

`src/main/java/com/enviouse/sef/mute/MuteManager.java`, lines 52 through 86 and 254 through 303, stores remaining ticks and decrements them only while the server is running. It persists periodically at 6,000 ticks.

The tick path converts loaded UUID text without isolating invalid records. A syntactically valid JSON document containing an invalid UUID can throw on the server tick.

#### Failure mode

Server downtime pauses temporary mutes. A crash can roll back up to the persistence interval and extend a mute. Invalid loaded data can interrupt the moderation tick rather than entering a bounded recovery path.

#### Required repair

1. Store an absolute UTC `expiresAt` instant.
2. Migrate remaining ticks once using the migration timestamp and document the conversion.
3. Validate and bound UUIDs, names, reasons, actor values, collection size, and timestamps before publishing.
4. Quarantine or reject invalid records according to a documented whole document policy.
5. Calculate remaining display duration from the absolute expiry.

#### Acceptance tests

1. Restart after simulated downtime.
2. Stop the process immediately before a periodic save.
3. Load invalid UUID, negative duration, extreme duration, and oversized fields.
4. Verify permanent mute migration remains permanent.

### SEF-AUD-010, medium, atomic writes do not fully guarantee rename durability

#### Evidence

`src/main/java/com/enviouse/sef/storage/AtomicFileStore.java`, lines 21 through 54, forces the temporary file and performs an atomic move when supported. It does not force the parent directory after rename. Its unsupported fallback uses a replacing move that is not atomic.

#### Failure mode

On filesystems where directory metadata is not durable without a directory sync, a power loss can lose a completed rename. On a filesystem without atomic move support, the fallback can expose a missing or incomplete canonical state during failure.

#### Required repair

1. Force the parent directory after creating and renaming files where the platform supports it.
2. Implement a two name journal or durable previous generation fallback for filesystems without atomic moves.
3. Keep the old generation until the new generation and directory entry are durable.
4. Record the exact durability guarantees and platform limitations.

#### Acceptance tests

1. Use fault injection around create, write, force, rename, and directory force.
2. Force the atomic move unsupported path.
3. Restart after every injected failure and prove either the old or new complete generation is selected.

### SEF-AUD-011, medium, disguise synchronization has quadratic tick behavior

#### Evidence

`src/main/java/com/enviouse/sef/disguise/DisguiseProxyService.java`, lines 52 through 71 and 158 through 199, iterates viewers and active disguises on synchronization ticks. Visibility and permission work is repeated within the nested path.

The configured limits allow hundreds of viewers and disguises. `shouldSuppressRealSpawn`, lines 74 through 79, scans the global proxy mapping for outbound spawn decisions.

#### Failure mode

The path approaches `viewers × disguises` work per synchronization cycle and adds global scans to entity spawn handling. At documented upper bounds this can consume the server tick budget and cause burst latency during joins, dimension changes, or mass disguise updates.

#### Required repair

1. Index disguises by dimension and spatial region.
2. Keep a per viewer visibility cache and update only deltas.
3. Index real entity IDs directly to viewer proxy state.
4. Cache permission decisions for a bounded tick generation and invalidate on permission events.
5. Spread nonurgent reconciliation across ticks with a strict time or operation budget.

#### Acceptance tests

1. Benchmark 100, 200, and 300 viewers with 64, 128, and 256 disguises.
2. Measure median, p95, and worst tick contribution.
3. Exercise join bursts, dimension changes, permission changes, and proxy removal.
4. Define and enforce a maximum allowed tick budget.

### SEF-AUD-012, medium, performance diagnostics synchronously scan all entities

#### Evidence

`src/main/java/com/enviouse/sef/control/MinecraftServerControlRuntime.java`, lines 1393 through 1413, builds performance output by iterating every server level and every loaded entity on the command or server thread.

#### Failure mode

An operator diagnostic can itself stall a large server. Repeated calls can become an easy accidental denial of service for any role granted the command.

#### Required repair

1. Use cached per level counters when available.
2. Collect bounded snapshots over multiple ticks when a full scan is required.
3. Rate limit the command per actor and globally.
4. Include snapshot age in output.

#### Acceptance tests

1. Populate large synthetic entity counts and measure command tick time.
2. Repeat the command rapidly and verify rate limiting.
3. Verify partial snapshots never block a full tick.

### SEF-AUD-013, medium, chat content retention and logging exceed a clear privacy boundary

#### Evidence

`src/main/java/com/enviouse/sef/events/ChatEventHandler.java`, around lines 131 and 156, logs the full content of blocked muted messages regardless of whether muted content is forwarded to operators.

The same class logs full admin chat near line 167 and public chat near line 267.

`src/main/java/com/enviouse/sef/chat/ChatMessageManager.java` retains up to 10,000 message bodies without a time based retention limit.

#### Failure mode

Secrets pasted into chat, private moderation content, and messages the system explicitly blocked can enter long lived server logs and memory. The behavior is not represented as a single explicit privacy and retention policy.

#### Required repair

1. Default to metadata only logs containing actor UUID, channel, result, message length, and operation ID.
2. Put content logging behind an explicit configuration with a warning, access policy, retention duration, and redaction contract.
3. Separate admin channel retention from public chat retention.
4. Apply short time based expiry to reply records.
5. Never log authentication material or known secret patterns.

#### Acceptance tests

1. Send secret shaped fixtures through public, admin, muted, filtered, and denied paths.
2. Inspect console, file logs, command journals, memory records, and exports.
3. Verify retention expiry and configuration reload.

### SEF-AUD-014, medium, required mixin hooks add risk without active behavior

#### Evidence

`src/main/java/com/enviouse/sef/vanish/VanishUtil.java`, lines 31 through 32, declares `ACTIVE_ENTITY`.

`src/main/java/com/enviouse/sef/vanish/mixin/world/ServerLevelMixin.java`, lines 41 through 59, and `ServerGamePacketListenerImplMixin.java`, lines 54 through 131, set and remove the thread local around tick and packet hooks. No active code reads it.

The cleanup is attached to later injection points rather than guaranteed by a `finally` block. An exception between the hooks can retain the entity or player on the server thread.

`src/main/java/com/enviouse/sef/vanish/mixin/interaction/VanishEntitySelectorMixins.java`, lines 31 through 46, calls level entity queries directly instead of invoking the wrapped original operation. This can bypass another mod's wrapper.

#### Failure mode

Required mixins run on hot paths and increase compatibility risk while providing no current behavior. Exceptional paths can retain objects in thread local state. Directly replacing wrapped calls can break composition with protection or entity selection mods.

#### Required repair

1. Remove `ACTIVE_ENTITY` and its hooks if no consumer is required.
2. If context is required, use a wrapper that restores the previous value in `finally`.
3. Call the supplied original operation and filter its returned list rather than reissuing the level query.
4. Minimize required mixins and document why every remaining hook has no supported event or API replacement.

#### Acceptance tests

1. Force an exception inside the wrapped tick and packet operation and prove context is cleared.
2. Install a test wrapper around the same entity query and prove both transformations execute.
3. Measure tick overhead with the context feature disabled and enabled.

### SEF-AUD-015, medium, dependency metadata accepts a wider range than the implementation proves

#### Evidence

The project pins NeoForge `21.1.233` for Minecraft `1.21.1`, but packaged `META-INF/neoforge.mods.toml` declares NeoForge version range `[21,)`. The Minecraft range is `[1.21.1,1.22)`.

`src/main/resources/sef.mixins.json` is required, contains 34 common and 3 client top level mixins, and uses a default required injection count of one. The packaged JAR contains 39 mixin class entries including nested mixin classes.

#### Failure mode

The loader can accept untested NeoForge versions whose internal targets or lifecycle behavior differ. Required mixin movement can then turn an advertised compatible version into a startup failure.

#### Required repair

1. Constrain the NeoForge range to the versions actually tested and supported.
2. Establish a compatibility matrix and test the lowest and highest accepted versions.
3. Run mixin application and dedicated server startup for every advertised release target.
4. Widen the range only after evidence demonstrates compatibility.

#### Acceptance tests

1. Launch client and dedicated server on every accepted NeoForge version.
2. Verify all required injections and optional integrations.
3. Confirm out of range versions are rejected with a clear loader message.

### SEF-AUD-016, low, FancyTag upload ownership is checked after removal

#### Evidence

`src/main/java/com/enviouse/sef/fancytags/FancyTagTransferService.java`, lines 121 through 140, removes an upload from the map before checking whether the requesting player owns it.

#### Failure mode

A player who learns another active upload UUID can cancel that upload even though the finish request is rejected. UUID guessing is difficult, which limits practical severity, but the authorization order is incorrect.

#### Required repair

1. Read the upload without removal.
2. Verify ownership and transfer state.
3. Remove with a conditional `remove(id, expectedUpload)` operation only after authorization.
4. Keep timeout cleanup separate from client requested finish.

#### Acceptance tests

1. Attempt cross owner finish with a known valid UUID.
2. Race owner finish, attacker finish, and timeout cleanup.
3. Prove exactly one authorized terminal result.

### SEF-AUD-017, low, markdown escape handling can corrupt formatting

#### Evidence

`src/main/java/com/enviouse/sef/MarkdownFormatter.java`, lines 3 through 84, sets `escapeNext` after a backslash. When the following character is not a style marker, the normal character path does not clear the flag. A later marker is therefore treated as escaped and the original backslash disappears.

The result is trimmed, which alters intentional leading and trailing spaces. The parser uses repeated string concatenation and contains a commented debug block at lines 86 through 103. No direct test class covers this formatter.

#### Failure mode

Messages with ordinary backslashes can lose characters or apply incorrect style behavior later in the message. Whitespace is changed even when no formatting requires it.

#### Required repair

1. Replace the parser with a small explicit state machine and `StringBuilder`.
2. Define escape behavior for marker, backslash, and ordinary character cases.
3. Define unmatched delimiter behavior.
4. Preserve whitespace unless a documented chat rule removes it.
5. Remove dead debug code.

#### Acceptance tests

1. Cover every marker, escaped marker, escaped backslash, ordinary backslash, unmatched marker, nested marker, and whitespace boundary.
2. Add property tests proving plain text without supported markers is unchanged.
3. Fuzz the parser with bounded random Unicode input.

### SEF-AUD-018, low, selected exception paths hide decisive diagnostics

#### Evidence

`src/main/java/com/enviouse/sef/control/ServerControlExecutionService.java`, lines 159 through 165, converts handler runtime and linkage failures to a generic result without logging the exception.

`src/main/java/com/enviouse/sef/banned/BannedItemsManager.java` silently skips malformed bypass UUID values and has broad failure handling around load.

#### Failure mode

Operators see a failed feature or altered enforcement state without the exception, operation ID, record, or damaged field needed to diagnose it.

#### Required repair

1. Log unexpected exceptions once at the subsystem boundary.
2. Include stable operation and record identifiers, repository state, feature ID, and exception stack.
3. Avoid logging message bodies, secrets, or unrestricted document content.
4. Return a short correlation ID to the command source.
5. Distinguish rejected input, unavailable dependency, recovery state, and internal failure.

#### Acceptance tests

Inject representative exceptions and verify both safe player output and actionable operator logs.

### SEF-AUD-019, high, the command catalog and live dispatcher materially disagree

#### Evidence

`src/main/java/com/enviouse/sef/gui/GuiWorkflowGameTests.java` now contains required runtime coverage for every catalog route, every active shortcut, every SEF owned live root, and every enabled action that declares console support.

The current catalog contains 676 actions and 290 shortcuts. The expanded 34 test GameTest run found:

1. 107 catalog actions whose declared canonical route does not exist as an executable route in the live dispatcher.
2. 100 actions that declare console support but cannot compile for the real console source.
3. 26 shortcut or live root ownership mismatches.
4. An active `suicide` shortcut with no registered command root.

The 107 absent routes consist of all 75 `sef:control.*.manage` actions, 24 `sef:economy.sign.*` actions, and eight additional actions covering GUI enable or disable, FancyTag bulk or lease operations, player warp management, suicide, and super enchant mutation.

The 25 noncontrol console mismatches include player inventory utilities, kit operations, jail setup, FancyTag operations, player warp management, and player movement utilities. Their catalog source policy includes console even though the live command requirements reject the console source.

The unowned roots include nickname commands, announcements, chat administration, banned items, clear chat, alternate account checks, countdown, vanish, approvals, and other legacy roots.

#### Failure mode

Generated command documentation, GUI fallbacks, source policy, shortcut diagnostics, and runtime behavior describe different products. Operators can be told that a canonical command exists when Brigadier cannot execute it. A GUI workflow can be classified as covered while its documented fallback route is absent. Security and audit policy cannot be proven for roots that bypass catalog ownership.

#### Required repair

1. Decide whether each catalog entry represents an executable command or an internal action. Internal actions must not advertise a canonical command route or command usage.
2. Add real dispatcher routes for every executable catalog action, or remove the phantom entry and connect the owning GUI directly to a typed internal workflow.
3. Derive source types from actual command contracts. Do not apply `PLAYER`, `CONSOLE`, and `RCON` as a broad default when a route requires a player.
4. Register every legacy root in the catalog with permissions, source policy, audit class, feature gate, collision policy, and GUI fallback, or remove the root.
5. Make shortcut activation depend on successful dispatcher registration.
6. Keep the new GameTests required. Release must remain blocked until all four mismatch counts are zero.

#### Acceptance tests

1. Every catalog action resolves to at least one executable live route.
2. Every declared source compiles the route with its real `CommandSourceStack`.
3. Every active shortcut exists in the dispatcher.
4. Every root added after vanilla registration has exactly one catalog owner.
5. Generated command documentation matches the postregistration dispatcher.

### SEF-AUD-020, medium, `/setworth` rejects normal namespaced item identifiers

#### Evidence

`src/main/java/com/enviouse/sef/economy/EconomyCommands.java`, lines 93 through 100, registers the item argument with `StringArgumentType.word()`.

The new representative argument GameTest renders the registry value `minecraft:stone`, producing `setworth minecraft:stone 1`. Brigadier leaves input unread because a word argument does not accept the colon in a namespaced resource location.

The command implementation at lines 978 through 1000 expects a resource location and validates it through the item registry.

#### Failure mode

The canonical Minecraft item identifier format cannot pass the command parser. GUI registry values and normal operator input use namespaced identifiers, so a valid item is rejected before `setWorth` runs.

#### Required repair

Use the Minecraft item argument type or a resource location argument with registry validation. Keep the parsed item typed through execution instead of converting it back from an unrestricted string.

#### Acceptance tests

1. `setworth minecraft:stone 1` parses and sets the expected worth.
2. An unknown item fails without changing the repository.
3. Invalid, negative, zero, maximum, and overflowing amounts follow the documented policy.
4. The value persists across repository flush and reload.

## Release blockers

### SEF-RB-001, the product scope is not complete

The repository documents 75 server control schemas. Fifty nine are executable and sixteen are deliberately unavailable. The unavailable handlers are honestly represented and are not being reported as hidden runtime bugs, but they prevent the full planned surface from being considered complete.

`docs/SEF2_ACCEPTANCE.md`, especially lines 31 through 49, still marks multiple phase 13 sections and global verification as in progress. Multiplayer, LuckPerms, GUI, InvSee, admission, and disguise matrices remain open.

Required decision:

1. Select the minimum production feature set.
2. Remove or clearly mark every surface outside that set.
3. Do not advertise incomplete controls as release functionality.
4. Finish acceptance for the selected set before resuming expansion.

### SEF-RB-002, maintained quality gates do not match the codebase size

The maintained Gradle build has no formatter, lint, coverage, or static analysis task. The repository CI runs the Java build and CodeQL, but it does not enforce formatting, warning budgets, test coverage, dependency compatibility, dedicated server startup, or client startup.

The audit found 45 top level main source areas. The remediation suite now has 38 top level test areas. The following main areas still have no corresponding top level unit test package:

`clearchat`, `countdown`, `disablebuilding`, `events`, `gametest`, `invlock`, `player`, and `tab`.

The initial textual coverage heuristic found 154 of 372 main classes with no reference from unit tests or GameTests. This historical heuristic is not a branch or line coverage measurement. A maintained coverage task is still required to replace it with current evidence.

Several classes are too large to review and fault isolate safely:

| Class | Approximate lines |
| --- | ---: |
| `AutomationCommands` | 3,349 |
| `SefGuiServer` | 3,153 |
| `KernelServices` | 3,105 |
| `FancyTagService` | 2,482 |
| `ModuleConfigService` | 2,244 |
| `ConfigHandler` | 2,239 |
| `PermissionsHandler` | 2,145 |
| `SefPayloads` | 2,043 |

Required quality gate:

1. Add a repository formatter or format check.
2. Enable compiler warnings intentionally and reduce them to an agreed baseline.
3. Add maintained static analysis with reviewed suppressions.
4. Add line and branch coverage reporting. Use risk based thresholds rather than chasing a single global percentage.
5. Run unit tests, GameTests, dedicated server startup, and relevant client startup in CI.
6. Add fault injection tests for durable operations and storage.
7. Split large classes along existing domain boundaries without changing behavior during the split.

## Verification evidence

All Java verification was repeated with `/usr/lib/jvm/java-21-openjdk-amd64`, matching the project requirement. The interactive shell itself defaulted to Java 25, so Java 25 results were not used as the primary build evidence.

| Check | Result | Notes |
| --- | --- | --- |
| `./gradlew test --no-daemon` | Passed | 487 tests, 0 failures, 0 errors, 0 skipped. |
| `./gradlew runGameTestServer --no-daemon` | Passed | All 38 required GameTests passed. Command coverage checked 694 catalog actions, 315 shortcuts, 2,213 representative parser variants, and 358 safe read only live routes. |
| `./gradlew build --rerun-tasks --no-daemon` | Passed | Clean Java 21 build, tests, resources, generated metadata, and packaging completed. |
| `./gradlew compileFallbackRuntimeJava generatePerformanceReport --no-daemon` | Passed | Fallback source compatibility and deterministic performance budgets completed. |
| `./gradlew runServer --no-daemon` | Passed | Dedicated server reached `Done`, loaded SEF on NeoForge `21.1.235`, accepted console input, saved every dimension, and shut down cleanly through `stop`. |
| `xvfb-run -a ./gradlew runClient --no-daemon` | Startup passed | Client loaded SEF, applied all configured mixins, initialized OpenGL, reloaded resources, and created all texture atlases. It was intentionally terminated after stable startup, so the interrupted Gradle process is not treated as a build failure. |
| Headless audio | Environment limitation | OpenAL could not open an audio device. No modloading, classloading, or mixin failure occurred. |
| Java `-Xlint:all` audit compile | Completed | 57 warnings, mainly deprecation, removal, unchecked reflection, raw arrays, serial IDs, constructor escape warnings, and redundant casts. |
| SpotBugs audit run | Completed with findings | 677 raw reports were generated. Most were false positives involving public registration holders, mixin casts, intentionally exposed immutable data, and framework patterns. Only manually confirmed issues are included in this report. |
| Repository JSON and TOML parsing | Passed | All in scope JSON and TOML documents parsed. |
| GitHub workflow review | Passed syntax review | Three YAML files were manually inspected because no repository YAML parser task exists. |
| Secret shape scan | Passed | No private key, GitHub token, AWS access key, Discord token, or credential shaped value was found. |
| `git diff --check` | Passed | No whitespace errors in the existing working diff. |
| JAR structure inspection | Passed | 1,193 entries, exact NeoForge range `[21.1.235]`, no duplicate entries, and no runtime logs, environment files, or key files. |

The packaged artifact was `build/libs/sef-1.0-SNAPSHOT.jar`, 3,364,411 bytes, with SHA 256:

`a099b6664a385b36e707972ec8672b9b1cc1ad67f815e8ca4735c8c4599cce3b`

## Command execution coverage

The generated command registry contains 694 catalog actions and 315 shortcuts. Required GameTests now inspect every catalog action and shortcut against the live Brigadier dispatcher. They verify route ownership, registered roots, source compatibility, canonical and shortcut policy sharing, GUI fallback ownership, typed workflow compilation, help paths, and representative argument construction.

The GameTest command matrix compiled 2,213 representative parser variants without a missing route, parser mismatch, ownership mismatch, or shortcut mismatch. It also directly executed 358 unique safe read only console routes against the live server dispatcher. Domain GameTests execute selected mutating routes and verify their effects for sudo delegation, economy, moderation, jail transitions, teleport safety, inventory transactions, server controls, storage, escrow, and administrative enchanting.

This is strong evidence that every registered command is reachable and has a valid parser contract. It is not a false claim that every mutating command was executed with every possible source and world fixture. Generic execution of destructive administration commands would alter the shared GameTest server without a command specific rollback contract. The remaining manual matrix in `test.md` therefore still covers:

1. Every mutating command with concrete players, dimensions, inventories, permissions, and cleanup.
2. Player, RCON, command block, function, scheduled, GUI, sudo, bundle, and server profile sources where permitted.
3. Live LuckPerms grant, deny, inheritance, wildcard, hierarchy, exemption, and reload behavior.
4. Persistence, restart, disconnect, rollback, and message presentation for each applicable mutation.
5. Multiplayer, visual, optional integration, invalid dependency, and cross dimension scenarios.

The required automated matrix prevents the original catalog mismatch from returning. A catalog action or shortcut cannot pass merely because documentation exists or an incomplete invocation returns usage.

## Verified strengths

The audit also confirmed several sound foundations that should be preserved:

1. Modern repository implementations expose explicit ready, recovery, unsupported, error, and closed states with write gates.
2. GUI protocol sessions and payload handlers use bounded fields, sequence checks, replay protection, and server side authorization.
3. Permission resolution covers exact nodes, wildcard hierarchy, dynamic providers, and explicit LuckPerms denial behavior.
4. Sudo delegation uses a bounded one execution scope with `finally` cleanup.
5. Escrow uses preparing and settling markers with stronger recovery ordering than the affected server control and offline action paths.
6. Grave capture performs durable repository work before suppressing normal drops.
7. The file logging subsystem rejects symbolic links in its owned directory structure and bounds queues, record sizes, retention, and export counts.
8. No active credential, private key, environment file, or runtime log was packaged in the JAR.
9. Documentation does not falsely claim that the open acceptance work is complete.

## Static warning debt

The 57 `-Xlint:all` warnings are not release blockers individually, but they should become a tracked zero growth baseline. The important categories are:

1. Deprecated or removal marked GameTest helper and event bus APIs.
2. Unchecked reflective integration calls in optional compatibility code.
3. Raw generic array creation and possible heap pollution.
4. Missing `serialVersionUID` values on serializable exception or UI types.
5. `this` escape warnings during construction in `InvSeeContainer` and `ServerEssentialsForge`.
6. Deprecated network buffer APIs and redundant casts.
7. Lossy compound assignment in `MarkdownFormatter`.

Do not enable warning failure globally until the current baseline is either repaired or explicitly and narrowly suppressed. New warnings should fail CI immediately.

## Main source coverage map

The following table accounts for all 372 main Java files. Line totals are physical source lines and include comments and blank lines.

| Source area | Files | Lines |
| --- | ---: | ---: |
| Root package | 4 | 606 |
| `alts` | 3 | 685 |
| `announcements` | 11 | 1,186 |
| `audit` | 2 | 681 |
| `automation` | 10 | 8,495 |
| `banned` | 5 | 1,469 |
| `chat` | 4 | 478 |
| `clearchat` | 1 | 88 |
| `commandlog` | 8 | 4,114 |
| `commands` | 5 | 1,290 |
| `config` | 12 | 7,436 |
| `control` | 19 | 11,804 |
| `countdown` | 2 | 279 |
| `disablebuilding` | 3 | 185 |
| `disguise` | 7 | 3,056 |
| `economy` | 12 | 5,134 |
| `escrow` | 3 | 2,207 |
| `events` | 5 | 882 |
| `fancytags` | 10 | 6,424 |
| `filter` | 2 | 112 |
| `freeze` | 3 | 557 |
| `gametest` | 3 | 322 |
| `gui` | 48 | 18,291 |
| `identity` | 3 | 506 |
| `inventory` | 3 | 805 |
| `invlock` | 3 | 205 |
| `invsee` | 6 | 1,451 |
| `kernel` | 24 | 8,165 |
| `kits` | 2 | 1,004 |
| `message` | 1 | 114 |
| `moderation` | 5 | 2,519 |
| `motd` | 2 | 198 |
| `mute` | 2 | 506 |
| `permissions` | 11 | 1,524 |
| `player` | 4 | 1,116 |
| `recovery` | 6 | 2,011 |
| `social` | 12 | 2,945 |
| `storage` | 11 | 1,620 |
| `sudo` | 1 | 86 |
| `tab` | 2 | 86 |
| `teleport` | 15 | 7,291 |
| `util` | 1 | 157 |
| `utils` | 12 | 726 |
| `vanish` | 55 | 4,015 |
| `warn` | 2 | 384 |
| `workstations` | 7 | 1,808 |
| **Total** | **372** | **115,022** |

## Repair program status

### Wave 0, scope freeze, complete for the audit repair

The repair remained limited to the confirmed findings, their regression coverage, generated references, and affected documentation. Incomplete runtime families remain explicitly unavailable instead of being presented as implemented.

### Wave 1, durable operations and storage, complete

Server controls, offline actions, jail transitions, enforcement storage, managed JSON boundaries, symbolic link checks, atomic publication, recovery states, and fault tests are implemented.

### Wave 2, chat, vanish, moderation, and economy, complete

Signed chat, opaque reply tokens, private content boundaries, economy event finalization, jail transitions, and absolute mute expiry are implemented and covered by regression tests.

### Wave 3, compatibility and performance, complete

Dead active entity hooks were removed, disguise synchronization and performance diagnostics are bounded, and dependency metadata is narrowed. Large class decomposition remains maintainability work under release blocker `SEF-RB-002`; it was not mixed into behavioral repairs.

### Wave 4, release acceptance, open

1. Add formatter, warning baseline, static analysis, and coverage gates.
2. Run the full multiplayer and permission provider matrix.
3. Complete GUI, InvSee, admission, disguise, reconnect, and failure recovery testing.
4. Run dedicated server and client smoke tests on every advertised version.
5. Inspect the final JAR, generated resources, dependency metadata, and complete Git diff.
6. Update `README.md`, `DOCUMENTATION.md`, compatibility documentation, and release notes only from verified outcomes.

## Audit limitations

This audit provides full repository inventory and static inspection plus available automated and startup verification. It does not claim that every possible runtime branch was executed.

The following still require controlled environments:

1. Real two or more player multiplayer behavior.
2. A live LuckPerms provider with grant, deny, inheritance, and reload changes.
3. Proxy and optional mod integration matrices.
4. Interactive GUI, InvSee, and accessibility checks.
5. Real network interruption, reconnect, and packet disorder tests.
6. Process kill and power loss fault injection at persistence boundaries.
7. Large production scale tick and memory profiling.
8. Dependency vulnerability database review beyond the repository's existing CodeQL workflow.

These limitations do not weaken the confirmed findings. They identify additional evidence required before release approval.

## Completion criteria

SEF should not be called production ready until all of the following are true:

1. Every high severity finding is fixed with its listed failure tests.
2. No enforcement store can silently become empty after a damaged read.
3. No nonidempotent side effect can be retried from an ambiguous durable state.
4. Public chat retains the expected signed message provenance.
5. Vanished identity, content, and presence cannot be discovered through reply tokens or alternate paths.
6. Jail and offline actions recover deterministically across disconnect and restart.
7. The advertised compatibility range matches tested versions.
8. The selected production feature set has completed automated and manual acceptance evidence.
9. Required CI, server startup, client startup, and packaging checks pass from a clean checkout.
