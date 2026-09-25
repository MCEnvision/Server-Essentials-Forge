# SEF Backport and Network Research Brief

## Product and source boundary

Backport implemented SEFPORTED behavior to the existing Forge 1.20.1 checkout. Remove the entire economy and interface, including custom client code, screens, HUDs, Fancy Tags and menu-dependent features. Ordinary textual commands and vanilla chat and visibility remain. The reference checkout and its existing plans and goal are read only. The target is server side; no SEF client installation or launcher is added.

Deliver a small Velocity companion on the requested `velocity-latest` product branch. The approved `/mnt/hermes/projects/SefVelocity` path may hold a same-repository linked worktree after Phase 004 safety checks, with the canonical plan retained in `/mnt/hermes/projects/Sef`. One Java 25 proxy coordinates three Forge backends. The exact installed proxy version is not yet verified. The target repository baseline remains Forge 47.3.12 and Java 17; changing runtime targets is not implicit.

## Accepted behavior

The proxy owns network identity, command routing, login bans, disconnections and network locations. Backends own signed-chat mute enforcement, permission-aware hiding and all world mutations. Durable shared state covers bans, mute expirations, vanish, server-qualified homes and operation outcomes.

Homes identify owner, backend, world generation, dimension, coordinates, rotation and revision. Transfer success requires the intended destination session to arrive and acknowledge its safe world action. Retries, timeouts and reconnects must not move the wrong session or report false success.

The owner approved compatible differing modpacks with validated transfer relations. Unknown or incompatible destinations must be refused before moving the player, with an explanation. Arbitrary client pack changes remain outside this server-only project.

The owner additionally requires comprehensive CoreProtect-style history and investigation. This is an original Forge-native module, not a Bukkit plugin dependency. Capture server-observable actions and actual changes, including natural and machine changes, item flows, entities, accepted movement, commands, communications, moderation and network events. Distinguish attempts from outcomes and direct attribution from inferred or unknown causes. Required coverage must not be silently sampled or disabled.

## Architecture recommendations and limits

Official Velocity documentation identifies Ambassador handshake support and ProxyCompatibleForge identity forwarding as separate Forge 1.20.1 dependencies. Compatibility of selected versions with the actual proxy is an early real-runtime gate, not a proven fact.

Use a typed authenticated direct bridge that works with empty backends. Bind every node, actor and session; reject forged, replayed, stale or oversized messages. Keep forwarding and administrative credentials separate. No arbitrary console relay.

Keep low-volume network authority in proxy-owned SQLite. For audit volume, the owner selected MySQL and MariaDB support, with MySQL the primary current engine and durable local backend journals. Keep this central dataset separate from low-volume network authority. Do not put all world history through the proxy event loop or reuse an unrelated application database.

The reference audit and file sinks can drop events when their queues fill. They cannot serve as the new comprehensive journal. Before/after capture, immutable event IDs, deduplicated replication, crash reconciliation and honest completeness watermarks are necessary. Forge does not provide an atomic transaction spanning world saves and audit storage; uncertain crash tails must be visible.

## Expanded Approved Workflows

CoreProtect-style rollback, restore, undo, preview and declared feature parity are mandatory. Capture full public/private communications and command arguments while retaining credential redaction and separately restricted sensitive reads. Retention is configurable and defaults to manual purge. An optional schedule must be explicitly activated for a previewed policy revision, use bounded fenced execution and preserve restoration dependencies. Consumer pause retains local capture while delaying ingestion; intentional capture pause is separate and reports its missing-history interval. On local storage exhaustion, continue service, rotate oldest SEF-owned audit segments, report any lost ranges and notify operators/admins through a restricted command display and configured Discord. This is an explicit emergency exception to manual normal retention, not permission to delete unrelated files or silently purge central history.

Redesign all SEF command output in the supplied HuskHomes style. Use calm semantic colors, checkmarks/crosses, brackets, bold values, grouped actions, hover details, click actions and pagination across backend and proxy. Rich vanilla chat is included; custom client interfaces remain excluded. The supplied screenshots are acceptance fixtures. Safe action authorization, session/target binding, confirmation, injection prevention, localization and console fallback remain necessary.

Hierarchical grid RTP supports local mode without Velocity and optional proxy mode. Each world has configured parent sections and independently persistent child grids. Parent and child bags do not repeat within their cycles, and a parent cycle cannot begin with the previous cycle's last section. Child cycles do not reset with parent reshuffles. Random coordinates remain inside the chosen subcell. Configurable separation from online players, active reservations and recent landings is independently enforced. No queue exhaustion, unsafe area or routing pressure may silently reduce distance or reuse an already consumed turn.

The owner delegated successful-landings-only consumption. Reserve durably during processing, release after confirmed failure/non-arrival and quarantine uncertain arrivals until recovery. Proxy routing chooses a suitable backend before using its grid, with fresh authenticated MSPT, spikes, capacity, incoming reservations, chunk work and prepared-location supply. World area and machine capacity remain separate settings.

The main configuration must also edit separate join, leave, first-time welcome and welcome-back messages, with variants and safe placeholders including `{player_name}`. Use the shared rich-chat renderer, durable first-visit identity, network versus local lifecycle ownership, visibility-filtered audiences and atomic validated reload. Backend switches must not impersonate new network logins. The lifecycle-message source record distinguishes existing local templates and reminders from this added contract.

## Research Question Map

- Network feasibility and isolation. SEF-REQ-009 through SEF-REQ-018, FIND-101. Exact downloaded artifact identities are bound; the combined Forge/proxy handshake and switching behavior still needs the early real-runtime gate.
- Audit capture versus intent. SEF-REQ-023 through SEF-REQ-029, FIND-104 through FIND-106. Existing dropping queues are insufficient, and cancellable callbacks alone do not prove applied state. Original Forge capture and conservative restoration need real conservation and recovery proof.
- Rich text across two platforms. SEF-REQ-030, SRC-007 and SRC-008. Downloaded HuskHomes Fabric resources and screenshots establish the visual reference. Focused research reconciles safe native component rendering without adding client code.
- RTP fairness, concurrency and capacity. SEF-REQ-032 through SEF-REQ-035, DEC-015 through DEC-017. Successful landing is the consumption boundary. Focused research defines persistence, reservations, strict failure and destination-owned safety before the master freezes shared contracts.
- Audit operation under finite storage. SEF-REQ-024, SEF-REQ-026 and SEF-REQ-031, DEC-010 through DEC-013. MySQL/MariaDB, manual-default normal retention with explicitly enabled scheduling and visible emergency oldest-segment rotation are resolved. No finite store can promise unbounded retained history.

All current material owner questions are answered. The source observations and resolved decisions form the supporting research record for the authoritative plan.

## Evidence and verification

The repository map contains CodeGraph relationships, source locators, fingerprints and scan limits. Source observations record primary upstream documentation and the audit coverage matrix. The intake is the single decision register. No source scan proves runtime behavior.

Acceptance requires real signed-chat, forged-message, server-transfer, visibility, persistence, audit conservation and failure paths. Verify modded adapters against a pinned first-release pack inventory. Unsupported or unhealthy coverage is explicit and cannot be counted as complete capture. Query results expose unreplicated, purged and uncertain intervals.

Use node-1 for headless builds, servers and proxy only. Use the verified owner laptop for graphical Minecraft assertions with the exact silent-client and per-stream mute procedure. Register exact disposable resources before tests and verify cleanup after their final consumer.

No product implementation, build, gameplay test or production mutation has occurred in this planning pass. The endpoint excludes production rollout and public publication. These observations establish planning inputs, not completed product verification.
